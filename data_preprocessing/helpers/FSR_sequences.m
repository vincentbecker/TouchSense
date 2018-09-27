function [EMGSequences] = FSR_sequences(FSRData, windowLength, windowOverlap, sequenceLength, sequenceOverlap)
    totalSequenceLength = windowLength * sequenceLength;
    numberSequences = length(FSRData) - totalSequenceLength;
    numberOutputs = size(FSRData, 2);
    FSRSequences = zeros(numberSequences, numberOutputs);
    
    for i = 1 : numberSequences
        sequenceWindows = FSR_windows(FSRData(i : (i + totalSequenceLength), :), windowLength, windowOverlap);
        assert(size(sequenceWindows, 1) == sequenceLength);
        
        majorLabel = sum(sequenceWindows, 1)
        
        FSRSequences(i, :, :) = sequenceWindows;
    end
end

