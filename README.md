<h1> Fighting Robot College Project</h1>
**My partner and I built a robot prototype that can be controlled remotely via an Android application we developed.**  
The robot was built as part of our final project in a practical engineering program during the Winter of 2021.

## Overview

Fighting Robot is a fully functional IoT-based prototype designed for remote-controlled robotic combat.  
The robot has an ultrasonic sensor for obstacle detection, a laser diode for hit simulation, and servo/DC motors for motion. The system uses a real-time Firebase Realtime Database to transfer control data between the Android app and the robot controller.

## Technologies Used

- **Arduino MKR WiFi 1010**
- **MKR Motor Carrier**
- **Firebase Arduino Library**
- **Ultrasonic Distance Sensor**
- **Laser Diode with Photoresistor Receiver**
- **Android (Java) + Firebase Realtime Database**
- **Interrupt-based detection**
- **PWM motor control**

---

## System Architecture

The system consists of 3 main parts:

### 1. Android Application
- Controls movement (direction + speed via slider)
- Controls laser position using servos
- Supports manual override of laser state
- Communicates with Firebase in real-time

### 2. Firebase Realtime Database
- Two-way communication layer between robot and app
- Used for sending and receiving control & feedback data

### 3. Arduino Robot Unit
- Reads control data using stream-based Firebase reading
- Moves using DC motors + servo motors
- Laser control logic based on override state or sensor feedback
- Uses an interrupt to detect "laser hit" and set a flag for handling
