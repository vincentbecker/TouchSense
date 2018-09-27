function [ features ] = featureCalculation( samples, singleChannelFeatures, multiChannelFeatures)
    % Using the provided windows extract features.
    % Features are implemented in a toolbox-fashion with functions.
    % OUTPUT: matrix holding features for each sample. Features are
    % calculated for each channel and concatenated horizontally.
    % [feat1_ch1 feat2_ch1, ..., featn_ch1, ..., feat1_chn, ..., featn_chn]
    
    numberFeatures = size(singleChannelFeatures, 2);

    [numberSamples, numberChannels, ~] = size(samples);
    features = []; %zeros(numberSamples, numberFeatures * numberChannels + numberMultiChannelFeatures);
    
    for i = 1 : numberSamples
        currentSampleFeatures = [];
        % Single-channel features.
        for j = 1 : numberChannels
           for k = 1 : numberFeatures
            % Remember: window is stored in the third dimension (depth).
            % Window for the current channel.
            window = reshape(samples(i, j, :), size(samples, 3), 1);
            f = str2func(strcat('calculate', singleChannelFeatures{1, k}));
            %features(i, numberFeatures * j - (k - 1)) = featureHandles{1, k}(window); 
            %features(i, numberFeatures * j - (k - 1)) = f(window); 
            currentSampleFeatures = [currentSampleFeatures f(window)];
           end
        end
        
        % Multi-channel features.
        if (length(multiChannelFeatures) > 0)
            multiFeatures = [];
            windowedChannels = samples(i, :, :);  
            % 2D matrix, each column is a channel of window length.
            windowedChannels = reshape(permute(windowedChannels,[2,1,3]),size(windowedChannels,2),[])';

            for j = 1 : length(multiChannelFeatures)
                f = str2func(strcat('multiCalculate', multiChannelFeatures{1, j}));
                feature = f(windowedChannels);
                multiFeatures = horzcat(multiFeatures, feature);
            end
            %features(i, (end - numberMultiChannelFeatures + 1) : end) = multiFeatures;
            currentSampleFeatures = [currentSampleFeatures multiFeatures];
        end
        if (isempty(features))
            features = zeros(numberSamples, size(currentSampleFeatures, 2));
        end
        features(i, :) = currentSampleFeatures;
    end
end

