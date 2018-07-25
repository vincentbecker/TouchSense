# TouchSense
Repository containing the code for the paper: "TouchSense: Classifying Finger Touches and Measuring their Force with an Electromyography Armband"

The goal of this project is to classify finger touches and estimate their force only by using an EMG armband. We wanted the method to be wireless, 
inexpensive, and to run in real time. Our method classifies touches with the thumb, the forefinger, and the middle finger. The EMG data is gathered with a [Thalmic Labs Myo](www.myo.com). 
We classify the finger used with a neural network designed for EMG data processing which we trained in Tensorflow. The network runs in inference mode on an Android smartphone (10 ms inference execution time per window on an LG Nexus 5X). 
The strength estimation follows a simpler approach and also runs on the Android smartphone. 

We built a hardware setup consisting of three force-sensitive resistors in order to measure the actual pressure applied by the fingers during data collection. This way it is possible to evaluate the quality of our force estimation and also to train personalized force regressors. 
![Hardware setup](https://github.com/vincentbecker/TouchSense/images/measurement_setup.jpg)

For details on the method please find the full paper [here](https://people.inf.ethz.ch/vbecker/). TODO

## Dataset
We collected a dataset from 18 participants. For access to the dataset please send us an [email](vincent.becker@inf.ethz.ch).

## Gathering the data
*Requirements*:
- The hardware setup as in the paper (the paper supplement contains a circuit diagram)
- An Arduino Yún
- The script in the *Arduino* folder
- A Myo armband

Time synchronisation etc. TODO

## Data preparation
*Requirements*:
- A [Matlab](www.mathworks.com/products/matlab.html) installation
- The files csv files containg the EMG and FSR values. 
TODO

## Training the network
*Requirements*:
- A [Tensorflow](https://www.tensorflow.org/) installation
- The mat-file resulting form the previous step
TODO

## Network inference in Android
*Requirements*:
- An [AndroidStudio](https://developer.android.com/studio/) installation
- An Android smartphone
TODO