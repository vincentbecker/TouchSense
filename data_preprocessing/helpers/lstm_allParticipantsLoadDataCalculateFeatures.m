function [featureDataSet, labelDataSet] = lstm_allParticipantsLoadDataCalculateFeatures(participants, chosen_path, withForefinger, windowLength, windowOverlap, sequenceLength, higherThreshold, lowerThreshold)
    numberParticipants = participants.size();
    featureDataSet = cell(1, numberParticipants);
    labelDataSet = cell(1, numberParticipants);

    for j = 1 : numberParticipants
        p = participants.get(j - 1);
        % Load data.
        disp(strcat("Loading session ", p, " (", string(j), "/", string(numberParticipants), ")"));
        [EMGData, ~, FSRData, ~] = loadParticipantData(p, chosen_path);

        % Calculate corresponding samples (windowing).
        [EMGSequences, labelSequences] = sequences(EMGData, FSRData, withForefinger, windowLength, windowOverlap, sequenceLength, higherThreshold, lowerThreshold);

        % Concatenate the result in the depth dimension.
        featureDataSet{1, j} =  EMGSequences;
        labelDataSet{1, j} = labelSequences;
    end
end

