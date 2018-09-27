function [ flatteredFeatureDataSet ] = flatterDataSet( featureDataSet )
    % Flatter a raw feature data set to be thrown in a neural network.
    % The input dataset is a cell array with 3d matrices for each
    % participant.
    flatteredFeatureDataSet = {};
    for p = 1 : length(featureDataSet)
        participantData = featureDataSet{1, p};
        flatteredParticipantData = reshape(participantData, size(participantData, 1), size(participantData, 2) * size(participantData, 3));
        flatteredFeatureDataSet{end+1} = flatteredParticipantData;
    end
end

