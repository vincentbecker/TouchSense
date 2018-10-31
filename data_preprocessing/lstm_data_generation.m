global jCBList;
global chosen_path;

addpath('helpers');

%% Choose participants.
% Load data of all participans into a cell array. Each entry is of a
% different participant.

chosen_path = uigetdir;

files = dir(chosen_path);
dir_flags = [files.isdir];
sub_folders = files(dir_flags);
jList = java.util.ArrayList;
participants_sessions = [];
for k = 1 : length(sub_folders)
    s = char(strcat(sub_folders(k).folder, filesep, sub_folders(k).name, filesep, "*.csv"));
    if (~isempty(dir(s)))
        participants_sessions(end+1) = str2double(strrep(strrep(sub_folders(k).name, "p", ""), "_s", "."));
    end
end

participants_sessions = sort(participants_sessions);

for k = 1 : length(participants_sessions) 
    jList.add(strcat("p", strrep(string(participants_sessions(k)), ".", "_s")));
end

hFig = figure;
jCBList = com.mathworks.mwswing.checkboxlist.CheckBoxList(jList);
jScrollPane = com.mathworks.mwswing.MJScrollPane(jCBList);
[jhCBList,hContainer] = javacomponent(jScrollPane, [10, 50, 540, 360], gcf);
jCBModel = jCBList.getCheckModel;
jCBModel.checkAll;
ok_button = uicontrol('Parent',hFig,'Style','pushbutton','String','OK', 'FontSize', 13, 'FontWeight', 'bold', 'Position',[10 10 100 30],'Visible','on', 'Callback', @ok_callback);
select_all_button = uicontrol('Parent',hFig,'Style','pushbutton','String','Select All','Position',[120 10 100 30],'Visible','on', 'Callback', @select_callback);
deselect_all_button = uicontrol('Parent',hFig,'Style','pushbutton','String','Deselect All','Position',[230 10 100 30],'Visible','on', 'Callback', @deselect_callback);

function data = normalize(data, minX, maxX)
    data = (data - minX) / (maxX - minX)  - 0.5;
end

function ok_callback(src, ~)
    global jCBList;
    global chosen_path;
    
    singleChannelFeatures = {'raw'};
    multiChannelFeatures = {};

    windowLength = 10;
    windowOverlap = 0;
    sequenceLength = 5;
    higherThreshold = 200;
    lowerThreshold = 100;
    % For normalization
    minX = -128;
    maxX = 127;
    
    participants = jCBList.getCheckedValues;
    close(src.Parent);
    
    disp('Preprocessing data with three fingers...');
    
    %% Generate categorical labels for all three fingers.
    % Load data and calculate features for every participant.
    % Resulting dataset is a cell array. Each item is a single participant.
    [featureDataSet, labelDataSet] = lstm_allParticipantsLoadDataCalculateFeatures(participants, chosen_path, true, windowLength, windowOverlap, sequenceLength, higherThreshold, lowerThreshold);

    %{
    % Data normalization.
    for i = 1 : length(featureDataSet)
        featureDataSet{i} = normalize(featureDataSet{i}, minX, maxX);
    end
    %}
    
    disp('Exporting data...');
    vname = @(x) inputname(1);

    export_data('lstm_data_users.mat', participants, featureDataSet, labelDataSet);
    
    disp('Preprocessing data without forefinger...');

    %% Generate categorical labels for all three fingers.
    % Load data and calculate features for every participant.
    % Resulting dataset is a cell array. Each item is a single participant.
    [featureDataSet, labelDataSet] = lstm_allParticipantsLoadDataCalculateFeatures(participants, chosen_path, false, windowLength, windowOverlap, sequenceLength, higherThreshold, lowerThreshold);
    
    %{
    % Data normalization.
    for i = 1 : length(featureDataSet)
        featureDataSet{i} = normalize(featureDataSet{i}, minX, maxX);
    end
    %}
    
    disp('Exporting data...');
    
    export_data('lstm_data_users_nofore.mat', participants, featureDataSet, labelDataSet);
    
    disp('Done!');
end

function select_callback(src, ~)
    global jCBList;
    jCBList.getCheckModel.checkAll;
end

function deselect_callback(src, ~)
    global jCBList;
    jCBList.getCheckModel.uncheckAll;
end