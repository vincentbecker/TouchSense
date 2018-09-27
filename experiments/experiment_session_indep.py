import tensorflow as tf
import numpy as np
import math
import scipy.io as sio
from sklearn import metrics
from sklearn.metrics import confusion_matrix
import os
import sys
import re
from model import CNN_Network, LSTM_Network
from helpers import shuffle_balance_samples

# Specify wether to use all fingers or exclude the forefinger.
all_fingers = True

# Toggle cross-validation test (turn off for Android model creation).
test = True

# Choose whether to balance the samples for each of the classes.
balance_data = True

filePath = './'

os.environ['TF_CPP_MIN_LOG_LEVEL']='2'

if len(sys.argv) < 2:
    print("error")
    print("Usage: " + str(sys.argv[0]) + " participant_name")
    sys.exit()

participant_name = str(sys.argv[1])

# Read the mat file containing samples and labels. Use data_users for all fingers, data_users_nofore for thumb + middle.
if all_fingers:
    mat_file = sio.loadmat('lstm_data_users.mat')
    global_confusion = np.zeros([3, 3])
    global_smoothed_confusion = np.zeros([3, 3])
    log_dir = './confusion_lstm_sessions_all/'
    log_type = '_all_'
else:
    mat_file = sio.loadmat('lstm_data_users_nofore.mat')
    global_confusion = np.zeros([2, 2])
    global_smoothed_confusion = np.zeros([2, 2])
    log_dir = './confusion_lstm_sessions_nofore/'
    log_type = '_nofore_'

if not os.path.isdir(log_dir):
    os.makedirs(log_dir)

# Distinguish mixed data and users' data for pre-training and proper training.
mat_keys = mat_file.keys()
mat_keys = list(mat_keys)
regex = re.compile('^(p[0-9]+_s[0-9]+)(_samples)$')
session_names = [m.group(1) for l in mat_keys for m in [regex.search(l)] if m]
session_names = list(set(session_names))

participant_names = [re.sub("_s[0-9]+", "", s) for s in session_names]

if participant_name not in participant_names:
    print("Unknown participant.")
    sys.exit()

session_names.sort()
participant_sessions = [s for s in session_names if (participant_name + "_") in s]

if len(participant_sessions) < 2:
    print(participant_name + " only has " + str(len(participant_sessions)) + " sessions. Cross-validation with session not possible.")
    sys.exit()
else:
    print(participant_name + " has " + str(len(participant_sessions)) + " sessions.")

mixed_sessions = [s for s in session_names if (participant_name + "_") not in s]

# The mat file contains 3 headers created by MATLAB other than samples and labels.
number_sessions = int(len(participant_sessions))
number_mixed_sessions = int(len(mixed_sessions))

log_confusion_matrix = True

# Model configuration and parameters.
class Config(object):
    def __init__(self, train_data, test_data, labels):
        train_data_shape = train_data[0].shape
        train_label_shape = labels.shape

        # Input data set.
        self.train_count = len(train_data)
        self.test_count = len(test_data)
        self.window_length = train_data_shape[2]
        self.n_steps = train_data_shape[0]          # How many windows we feed into the LSTM as a sequence.
        self.n_inputs = train_data_shape[1]         # Number of EMG channels.

        # Training parameters.
        self.learning_rate = 0.0025
        self.lambda_loss_amount = 0.0015
        self.training_epochs = 20
        self.batch_size = 100

        # CNN structure.
        self.channels_per_feature = 1               # Include multiple channels for each learned feature.
        self.number_filters_1 = 32                  # Size of first convolution.
        self.number_filters_2 = 4                   # Size of second convolution.
        self.number_flat_2 = self.n_inputs * \
                             self.number_filters_2  # Flattened size of the features of each channel after convolution.
        self.dropout_prob = 0.5

        # LSTM structure.
        self.n_hidden = 32                          # Number of hidden units before LSTM cells.
        self.num_lstm_cells = 3                     # Number of LSTM cells ("layers").
        self.n_classes = train_label_shape[1]       # Number of classes for output.

        # Structure of the hidden layers before the LSTM cells and the output layer.
        self.W = {
            'hidden': tf.Variable(tf.truncated_normal([self.n_inputs * self.number_filters_2, self.n_hidden], stddev=0.1), name="Wh"),
            'conv1': tf.Variable(tf.truncated_normal([self.channels_per_feature, self.window_length, 1, self.number_filters_1], stddev=0.1), name="Wc1"),
            'conv2': tf.Variable(tf.truncated_normal([1, 1, self.number_filters_1, self.number_filters_2], stddev=0.1), name="Wc2"),
            'output': tf.Variable(tf.truncated_normal([self.n_hidden, self.n_classes], stddev=0.1), name="Wo")
        }
        self.biases = {
            'hidden': tf.Variable(tf.constant(0.1, shape=[self.n_hidden]), name="Bh"),
            'conv1': tf.Variable(tf.constant(0.1, shape=[self.number_filters_1]), name="Bc1"),
            'conv2': tf.Variable(tf.constant(0.1, shape=[self.number_filters_2]), name="Bc2"),
            'output': tf.Variable(tf.constant(0.1, shape=[self.n_classes]), name="Bo")
        }

def train_model(train_samples, train_labels_one_hot, test_samples, test_labels_one_hot):
    global global_confusion
    global global_smoothed_confusion

    X_train = train_samples
    Y_train = train_labels_one_hot
    if test:
        X_test = test_samples
        Y_test = test_labels_one_hot
    else:
        X_test = np.empty(0)
        Y_test = np.empty(0)

    config = Config(X_train, X_test, Y_train)

    # ==================
    # Build the network.
    # ==================

    # Length is the window size, width the number of channels, and 1 the single "colour" channel
    X = tf.placeholder(tf.float32, shape=[None, config.n_steps, config.n_inputs, config.window_length, 1], name="input")
    Y = tf.placeholder(tf.float32, shape=[None, config.n_classes])
    Y_index = tf.argmax(Y, axis=1)

    # Probability of keeping a dropout weight.
    keep_prob = tf.placeholder(tf.float32)

    # CNN, feature calculation.
    features = CNN_Network(X, config)

    # LSTM prediction.
    pred_Y = LSTM_Network(features, keep_prob, config)

    l2 = config.lambda_loss_amount * \
            sum(tf.nn.l2_loss(tf_var) for tf_var in tf.trainable_variables())

    cost = tf.reduce_mean(
            tf.nn.sparse_softmax_cross_entropy_with_logits(labels=Y_index, logits=pred_Y)) + l2

    optimizer = tf.train.AdamOptimizer(
            learning_rate=config.learning_rate).minimize(cost)

    predictions_class = tf.argmax(pred_Y, 1)

    correct_pred = tf.equal(predictions_class, tf.argmax(Y, 1))
    accuracy = tf.reduce_mean(tf.cast(correct_pred, dtype=tf.float32))

    # Note that log_device_placement can be turned ON but will cause console spam with RNNs.
    sess = tf.InteractiveSession(config=tf.ConfigProto(log_device_placement=False))
    init = tf.global_variables_initializer()
    sess.run(init)
    saver = tf.train.Saver()

    # ==================
    # Train the network.
    # ==================
    training_data_count = train_samples.shape[0]
    test_data_count = test_samples.shape[0]
    print("Data shape: " + repr(train_samples.shape))
    if test:
        print('\n\r======= Session ' + participant_sessions[i] + ' =======')
        print('Train data size: ' + repr(training_data_count))
        print('Label distribution in training data: ' + str(np.sum(train_labels_one_hot, 0)))
        print('Test set size: ' + str(test_data_count))
        print('Label distribution in test data: ' + str(np.sum(test_labels_one_hot, 0)))
    else:
        print('\n\r======= Model training =======')
        print('Train data size: ' + repr(training_data_count))
        print('Label distribution in training data: ' + str(np.sum(train_labels_one_hot, 0)))

    best_accuracy = 0.0
    best_epoch = 1

    for j in range(1, config.training_epochs + 1):
        for start, end in zip(range(0, config.train_count, config.batch_size),
                              range(config.batch_size, config.train_count + 1, config.batch_size)):
            
            sess.run(optimizer, feed_dict={X: X_train[start:end], Y: Y_train[start:end], keep_prob: config.dropout_prob})

        # Test at every epoch, using the full set.
        if test:
            pred_classes, pred_out, accuracy_out, loss_out = sess.run(
                [predictions_class, pred_Y, accuracy, cost],
                feed_dict={X: X_test, Y: Y_test, keep_prob: 1.0}
            )
            print('Training epoch {}/{} - Test accuracy: {}'.format(j, config.training_epochs, accuracy_out))
            # Do not consider the first epoch as best result to rule out random results.
            if accuracy_out > best_accuracy and j > 5:
                best_accuracy = accuracy_out
                best_epoch = j
        else:
            print('Done with epoch {}/{}.'.format(j, config.training_epochs))

    # Finished optimization, print last test results.
    if test:
        print('\nFinal test accuracy: {}'.format(accuracy_out))

        # Calculate smoothed accuracy.
        pred_classes = np.argmax(pred_out, 1)
        last_predictions = np.full([5], pred_classes[0])
        smoothed_predictions = np.zeros(pred_classes.shape)
        for pred in range(len(smoothed_predictions)):
            last_predictions[pred % len(last_predictions)] = pred_classes[pred]
            unique, counts = np.unique(last_predictions, return_counts=True)
            smoothed_predictions[pred] = last_predictions[np.argmax(counts)]

        print('Best test accuracy: {} at epoch {}'.format(best_accuracy, best_epoch))

        pred_linearized = np.argmax(pred_out, 1)
        true_labels_linearized = np.argmax(Y_test, 1)
        confusion = confusion_matrix(true_labels_linearized, pred_linearized)

        smoothed_confusion = confusion_matrix(true_labels_linearized, smoothed_predictions)

        smoothed_accuracy = np.trace(smoothed_confusion / np.sum(smoothed_confusion))

        print(confusion)

        print('Smoothed accuracy: ' + str(smoothed_accuracy))

        print(smoothed_confusion)

        if log_confusion_matrix:
            global_confusion = global_confusion + confusion
            global_smoothed_confusion = global_smoothed_confusion + smoothed_confusion
            np.save(log_dir + 'confusion_test_' + participant_name + '_' + str(i + 1), confusion)
            np.save(log_dir + 'confusion_test_smoothed_' + participant_name + '_' + str(i + 1), smoothed_confusion)

        sess.close()
        tf.reset_default_graph()

    else:
        # Save the graph
        tf.train.write_graph(sess.graph_def, filePath, 'model_participant_' + participant_name'.pbtxt')
        # Save a checkpoint file, which will store the above assignment
        saver.save(sess, filePath + '/' + 'model_participant_' + participant_name'.ckpt')

if __name__ == "__main__":
    # If we don't want to test, we train on everything.
    if test:
        start_index = 0
    else:
        start_index = number_sessions - 1

    # Test on session i, train on others.
    for i in range(start_index, number_sessions):
        # ==============================================================
        # Import MATLAB data, balance it and generate eventual test set.
        # ==============================================================
        test_samples = mat_file[participant_sessions[i] + '_samples']
        test_label_sequences = mat_file[participant_sessions[i] + '_labels']

        if not test:
            i = number_sessions

        s = test_samples.shape
        sequence_length = s[1]
        num_features = s[2]
        window_length = s[3]
        output_length = test_label_sequences.shape[1]

        train_samples = np.empty([0, sequence_length, num_features, window_length])
        train_label_sequences = np.empty([0, output_length])

        for j in range(0, i):
            train_samples = np.concatenate([train_samples, mat_file[participant_sessions[j] + '_samples']])
            train_label_sequences = np.concatenate([train_label_sequences, mat_file[participant_sessions[j] + '_labels']])

        for j in range(i+1, number_sessions):
            train_samples = np.concatenate([train_samples, mat_file[participant_sessions[j] + '_samples']])
            train_label_sequences = np.concatenate([train_label_sequences, mat_file[participant_sessions[j] + '_labels']])

        # Expand data to 4D array, adding the fake "color" channel for CNN to work.
        train_sample_sequences = np.expand_dims(train_samples, axis=4)
        test_sample_sequences = np.expand_dims(test_samples, axis=4)

        # Data shape here is [n_sequences, n_steps, n_channels, window_length, 1].

        train_samples, train_labels_one_hot = shuffle_balance_samples(balance_data, train_sample_sequences, train_label_sequences)

        train_model(train_samples, train_labels_one_hot, test_sample_sequences, test_label_sequences)

    if log_confusion_matrix:
        np.save(log_dir + 'confusion_test_' + participant_name + '_global', global_confusion)
        np.save(log_dir + 'confusion_test_smoothed_' + participant_name + '_global', global_smoothed_confusion)
