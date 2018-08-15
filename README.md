# TouchSense
Repository containing the code for the ISWC paper: "TouchSense: Classifying Finger Touches and Measuring their Force with an Electromyography Armband"

Contact by [email](mailto:vincent.becker@inf.ethz.ch).

The goal of this project is to classify finger touches and estimate their force only by using an EMG armband. We wanted the method to be wireless, 
inexpensive, and to run in real time. Our method classifies touches with the thumb, the forefinger, and the middle finger. The EMG data is gathered with a [Thalmic Labs Myo](https://www.myo.com/). 
We classify the finger used with a neural network designed for EMG data processing which we trained in Tensorflow. The network runs in inference mode on an Android smartphone (10 ms inference execution time per window on an LG Nexus 5X). 
The strength estimation follows a simpler approach and also runs on the Android smartphone. 

We built a hardware setup consisting of three force-sensitive resistors in order to measure the actual pressure applied by the fingers during data collection. This way it is possible to evaluate the quality of our force estimation and also to train personalized force regressors.  
<p align="center"><img src="images/measurement_setup.jpg" alt="Hardware setup" height="200"></p>

### Possible applications
We built four applications which use our method to make surfaces interactive in order to add functionality. 
1. Using any surface to control a smart lamp. The user can press with his / her forefinger and middle finger to increase or decrease the brightness, respectively.  
<p align="center"><img src="images/Demo_lamp_new.png" alt="Smart lamp demo" height="200"></p>
2. Adding functionality to a text marking application. The space around can be used as a virtual color palette where the color and width of the strokes can be changed and also strokes may be reverted.  
<p align="center"><img src="images/Demo_text_marking.png" alt="Text marking demo" height="200"></p>
3. We extend the functionality of a tablet stylus by allowing the user to change the writing color by pressing against the stylus with the thumb.  
<p align="center"><img src="images/Demo_stylus2.png" alt="Stylus demo" height="200"></p>
4. A bicycle map application which lets the user change the map type and zoom in and out without letting go of the handlebar.  
<p align="center"><img src="images/Demo_bike.jpg" alt="Bike demo" height="200"></p>

## Dataset and Code
Coming soon. 
