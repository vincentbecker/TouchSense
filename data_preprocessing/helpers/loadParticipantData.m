function [ EMGData, EMGRestData, FSRData, FSRRestData ] = loadParticipantData( participant, chosen_path )

    folder_path = strcat(chosen_path, filesep, participant, filesep);
    % Load EMG and FSR data of participant.
    EMGFiles = dir(char(strcat(folder_path, 'EMG_', participant, '_*_*.csv')));
    FSRFiles = dir(char(strcat(folder_path, 'FSR_', participant, '_*_*.csv')));
    %FSRFiles = dir(char(strcat('data/FSR_', participant,'_*_*.csv')));
   
    % Check we have all the files.
    assert(length(EMGFiles) == length(FSRFiles));
    
    restEMGFile = dir(char(strcat(folder_path, 'EMG_', participant , '_rest.csv')));
    restFSRFile = dir(char(strcat(folder_path, 'FSR_', participant , '_rest.csv')));
    
    %restEMGFile = dir(char(strcat('data/EMG_', participant ,'_rest.csv')));
    %restFSRFile = dir(char(strcat('data/FSR_', participant ,'_rest.csv')));
    
    EMGData = [];
    FSRData = [];
    EMGRestData = [];
    FSRRestData = [];
    for i = 1 : length(EMGFiles)
       currentEMGData = csvread(strcat(folder_path, EMGFiles(i).name));
       currentFSRData = csvread(strcat(folder_path, FSRFiles(i).name));
       
       %currentEMGData = zscore(currentEMGData);
       
       EMGData = vertcat(EMGData, currentEMGData);
       FSRData = vertcat(FSRData, currentFSRData);
    end
    
    EMGRestData = csvread(strcat(folder_path, restEMGFile.name));
    FSRRestData = csvread(strcat(folder_path, restFSRFile.name));
    
end

