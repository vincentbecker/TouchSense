function export_data(data_filename, participants, featureDataSet, labelDataSet)
    if (exist(data_filename, 'file'))
            delete(data_filename);
    end
    eval(strcat(participants.get(0), '_', 'samples = [];'));
    save(data_filename, strcat(participants.get(0), '_samples'));
    for i = 1 : length(featureDataSet)
        eval(sprintf(strcat(participants.get(i - 1), '_samples = featureDataSet{%d};'), i));
        eval(sprintf(strcat(participants.get(i - 1), '_labels = labelDataSet{%d};'), i));
        eval(strcat('assert(length(', participants.get(i - 1), '_samples) == length(', participants.get(i - 1), '_labels))'))
        name = strcat(participants.get(i - 1), '_', 'samples');
        save(data_filename, name, '-append');
        name = strcat(participants.get(i - 1), '_', 'labels');
        save(data_filename, name, '-append');
        eval(sprintf('clear samples%d', i))
        eval(sprintf('clear labels%d', i))
    end
end

