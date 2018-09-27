import tensorflow as tf

# Define convolution for the graph.
def conv2d(x, W):
    return tf.nn.conv2d(x, W, strides=[1, 1, 1, 1], padding='VALID')

def CNN_Network(_X, config):
    # Shape of data here is [n_sequences, n_steps, n_inputs, window_length, 1].
    
    # Concatenate periodic boundary for the multi-channel features.
    _X = tf.concat([_X, _X[:, :, 1:(config.channels_per_feature), :]], 2)
    
    # Reshape to be able to multiply with matrices for convolution and hidden activation.
    _X = tf.reshape(_X, [-1, (config.n_inputs + config.channels_per_feature - 1), config.window_length, 1])
    
    # Shape of data here is [n_sequences * n_steps, (n_inputs + channels_per_feature - 1), window_length, 1].
    # The order of samples is [Sequence1_sample1, Sequence1_sample2, .... Sequence2_sample1, Sequence2_sample2, ...].
    
    # First layer: convolution per channel.
    h_conv1 = tf.nn.relu(conv2d(_X, config.W['conv1']) + config.biases['conv1'])
    
    # Shape here is [n_sequences * n_steps, n_inputs, 1, number_filters_1].
    
    # Second layer: 1x1 convolution to reduce parameters.
    h_conv2 = tf.nn.relu(conv2d(h_conv1, config.W['conv2']) + config.biases['conv2'])
    
    # Shape here is [n_sequences * n_steps, n_inputs, 1, number_filters_2].
    
    # We calculated features for each channel, so we have number_filters2 * number_channels = number_flat_2 features.
    h_conv2_flat = tf.reshape(h_conv2, [-1, config.number_flat_2])
    
    # Shape here is [n_sequences * n_steps, number_flat_2].
    
    return h_conv2_flat

def LSTM_Network(_X, keep_prob, config):
    """
        Returns a RNN with two stacked LSTM cells implemented in Tensorflow.
        When you have multiple LSTM cells, they are stacked vertically as it usually happens with connected layers.
        So having multiple cells adds deepness to the network.
        """
    
    # Shape here is [n_sequences * n_steps, number_flat_2].
    
    # Third layer: hidden activation.
    _X = tf.nn.relu(tf.matmul(_X, config.W['hidden']) + config.biases['hidden'])
    
    drop_out = tf.nn.dropout(_X, keep_prob)
    
    # Restore sequences as required for the LSTM.
    _X = tf.reshape(drop_out, [-1, config.n_steps, config.n_hidden])
    
    # Shape here is [n_sequences, n_steps, n_hidden].
    
    # Transpose data as required for the LSTM cells.
    _X = tf.transpose(_X, [1, 0, 2])
    
    cell_layers = []
    for l in range (0, config.num_lstm_cells):
        cell_layers.append(tf.contrib.rnn.BasicLSTMCell(config.n_hidden, forget_bias=1.0, state_is_tuple=True))

    lstm_cells = tf.contrib.rnn.MultiRNNCell(cell_layers, state_is_tuple=True)
    outputs, states = tf.nn.dynamic_rnn(lstm_cells, _X, dtype=tf.float32, time_major=True)
    
    # "Many-to-one" style for the classifier. We get only the last output of the layer.
    lstm_last_output = outputs[-1]
    
    # Linear activation to get the outputs for the classification.
    pred = tf.add(tf.matmul(lstm_last_output, config.W['output']), config.biases['output'], name="output")

    s = tf.nn.softmax(pred, name="softmax")

    #drop_out = tf.nn.dropout(pred, keep_prob)
    return pred
