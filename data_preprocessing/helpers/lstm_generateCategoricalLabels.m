function [classificationWindows, classificationLabels] = lstm_generateCategoricalLabels(higherThreshold, lowerThreshold, withForefinger, allWindowsSamples, allWindowsLabels)
    % Create clsasification label.
    
    for i = 1 : size(allWindowsLabels, 1)   
        for j = 1 : size(allWindowsLabels, 2)
            % TODO: TWO THRESHOLDS
            if (allWindowsLabels(i, j) >= higherThreshold) 
                allWindowsLabels(i, j) = 1;
                
            elseif allWindowsLabels(i, j) >= lowerThreshold 
                 allWindowsLabels(i, :) = 0;
            else
                allWindowsLabels(i, j) = 0;
            end
         end       
        
        sumLabels = sum(allWindowsLabels, 2);
        indices = find(sumLabels == 1);
        
        classificationLabels = allWindowsLabels(indices, :);
        classificationWindows = allWindowsSamples(indices, :, :);
        
        if (~withForefinger && size(allWindowsLabels, 2) >= 3)
            classificationLabels(:, 2) = [];
            
            sumLabels = sum(classificationLabels, 2);
            indices = find(sumLabels == 1);
        
            classificationLabels = classificationLabels(indices, :);
            classificationWindows = classificationWindows(indices, :, :);
        
        end
        
        %numberSamples = length(samples);

        %ix = randperm(numberSamples);
        %labels = labels(ix, :);
        %samples = samples(ix, :);

        %svmLabels = cell(length(labels), 1);
        %indices = find(labels(:, 1));
        %svmLabels(indices) = {'thumb'};
        %indices = find(labels(:, 2));
        %svmLabels(indices) = {'middlefinger'};
        %indices = find(labels(:,3));
        %svmLabels(indices) = {'middlefinger'};
    end
end

