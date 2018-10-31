function [ samples ] = windows( data, windowLength, windowOverlap )
    % Breaks raw data into overlapped windows which will be used to extract
    % features and also predict.
    
    [rows, cols] = size(data);
    
    % How much the window will move forward depending on chosen overlap.
    % More overlap means more samples and also capturing more infos.
    jump = windowLength - windowOverlap;
    
    % A sample is a complete window coming from raw data.
    numberSamples = floor(rows / jump) - floor(windowLength / jump) + 1;
    
    % The resulting matrix is three-dimensional.
    % Height (rows) = samples (windows) in time.
    % Width (cols) = data channels.
    % Depth = window data.
    samples = zeros(numberSamples, cols, windowLength);
    
    % Generating all samples (windowing).
    sampleIndex = 1;
    for i = 1 : jump : (rows - windowLength)
        s = data(i : (i + windowLength - 1), :);
        samples(sampleIndex, :, :) = s';
        sampleIndex = sampleIndex + 1;
    end
end

