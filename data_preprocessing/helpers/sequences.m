function [EMGSequences, labelSequences] = sequences(EMGData, FSRData, withForefinger, windowLength, windowOverlap, sequenceLength, higherThreshold, lowerThreshold)
    totalSequenceLength = windowLength * sequenceLength;
    numberSequences = length(EMGData) - totalSequenceLength;
    numberChannels = size(EMGData, 2);
    
    if (withForefinger)
        numberOutputs = 3;
    else
        numberOutputs = 2;
    end
    
    % Contain all sequences flattened in the first dimension.
    allWindowsSamples = zeros(totalSequenceLength * numberSequences, numberChannels, windowLength);
    allWindowsLabels = zeros(totalSequenceLength * numberSequences, numberOutputs);
    
    % Creating all windows for all sequences and filtering using
    % thresholds.
    index_start= 1;
    index_end = 1;
    for i = 1 : numberSequences
        sequenceWindowsSamples = windows(EMGData(i : (i + totalSequenceLength), :), windowLength, windowOverlap);
        assert(size(sequenceWindowsSamples, 1) == sequenceLength);
        
        sequenceWindowsLabels = FSR_windows(FSRData(i : (i + totalSequenceLength), :), windowLength, windowOverlap);
        assert(size(sequenceWindowsLabels, 1) == sequenceLength);
        
        % Clean out windows where we press more than one finger.
        [classificationWindows, classificationLabels] = lstm_generateCategoricalLabels(higherThreshold, lowerThreshold, withForefinger, sequenceWindowsSamples, sequenceWindowsLabels);
   
        newLength = size(classificationWindows, 1);
        
        %if (newLength == sequenceLength)
        if (newLength > 0)
            index_end = index_start + newLength;
            allWindowsSamples(index_start:(index_end-1), :, :) = classificationWindows;
            allWindowsLabels(index_start:(index_end-1), :) = classificationLabels;
        end
        
        index_start = index_end;
    end
    
    allWindowsSamples = allWindowsSamples(1:(index_end - 1), :, :);
    allWindowsLabels = allWindowsLabels(1:(index_end - 1), :);
    
    numberSequences = floor(length(allWindowsSamples) / sequenceLength) - sequenceLength;
    
    EMGSequences = zeros(numberSequences, sequenceLength, numberChannels, windowLength);
    labelSequences = zeros(numberSequences, numberOutputs);
    
    % Using all the windows to generate the sequences.
    for j = 1 : numberSequences
        start = (j-1) * sequenceLength + 1;
        finish = j * sequenceLength;

        EMGSequences(j, :, :, :) = allWindowsSamples(start:finish, :, :);
        
        currentLabels = allWindowsLabels(start:finish, :);
        
        counts = sum(currentLabels, 1);
        [M, ~] = max(counts);
        
        % Break ties, use last label with most occurrences.
        indexes = find(counts == M);
        last_index = zeros(size(indexes));
        for t = 1 : length(last_index)
            label = zeros(size(currentLabels(1, :)));
            label(1, indexes(t)) = 1;
            t_indexes = find(ismember(currentLabels, label, 'rows'));
            last_index(t) = t_indexes(end);
        end
        [t_last, ~] = max(last_index);
        majorLabel = currentLabels(t_last, :);
        
        % Label for a single sequence is the majority one.
        labelSequences(j, :) = majorLabel; 
    end
end

