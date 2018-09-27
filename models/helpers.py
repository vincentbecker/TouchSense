import numpy as np

def shuffle_balance_samples(balance_data, train_sample_sequences, train_label_sequences):
    number_train_sequences = len(train_sample_sequences)
    
    train_indices = np.arange(number_train_sequences)
    
    # Randomly shuffle data.
    np.random.shuffle(train_indices)
    
    train_samples = train_sample_sequences[train_indices]
    train_labels_one_hot = train_label_sequences[train_indices]
    
    if balance_data:
        classes_counts = np.sum(train_labels_one_hot, 0)
        min_class_count = int(min(classes_counts))
        output_length = train_label_sequences.shape[1]
        
        samples_shape = train_samples.shape
        samples_shape = list(samples_shape)
        samples_shape[0] = int(min_class_count * output_length)
        
        labels_shape = train_labels_one_hot.shape
        labels_shape = list(labels_shape)
        labels_shape[0] = int(min_class_count * output_length)
        
        balanced_samples = np.empty(samples_shape)
        balanced_labels = np.empty(labels_shape)
        
        for j in range(output_length):
            class_indices = train_labels_one_hot == 1
            class_indices = np.where(class_indices[:, j] == 1)[0]
            class_samples = train_samples[class_indices]
            class_samples = class_samples[:min_class_count]
            class_labels = train_labels_one_hot[class_indices]
            class_labels = class_labels[:min_class_count]
            balanced_samples[j * min_class_count : (j + 1) * min_class_count, :] = class_samples
            balanced_labels[j * min_class_count: (j + 1) * min_class_count, :] = class_labels
        
        train_samples = balanced_samples
        train_labels_one_hot = balanced_labels
    
    number_train_sequences = len(train_samples)

    train_indices = np.arange(number_train_sequences)

    # Randomly shuffle data after balancing it.
    np.random.shuffle(train_indices)
    
    train_samples = train_samples[train_indices]
    train_labels_one_hot = train_labels_one_hot[train_indices]

    return train_samples, train_labels_one_hot
