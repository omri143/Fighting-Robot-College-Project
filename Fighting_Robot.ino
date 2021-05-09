/***
 * The main code of the robot.
 * Please compile the sketch with borad version 1.8.6 and MKR Motor Carrier library version 1.0.5.
 * The code was written by Omri Abramovich & Omri Globerman during October 2020 - March 2021
 * Fixes:
 *  05/01/2021: Firebase response time was long. Fixed by Omri Abramovich on the same day
 *  09/03/2021: Conveted the ultrasonic duration to m/s.
 * */
#include <WiFiNINA.h>
#include "Firebase_Arduino_WiFiNINA.h"
#include <MKRMotorCarrier.h>

//Firebase and Wifi Constants
#define FIREBASE_HOST "fighting-robot.firebaseio.com"
#define FIREBASE_AUTH "8foa9a8mXQJPgG0eaJbAKLC2TCQsjChqrk0SfGrm"
#define WIFI_SSID "Kinneret College"
#define WIFI_PASSWORD ""

//Sensors pins
#define TRIG_PIN A1
#define ECHO_PIN A5
#define LASER_DETECT_PIN A2
#define BUZZER_PIN 13
#define STATUS_LED_PIN 14
#define LASER_DIODE_PIN A6 

//HC-SR04 Constants
#define THRESHOLD_DISTANCE 0.2 // the maximum distance that counts as detected [m]
#define SPEED_OF_SOUND 343 // Speed of sound [m/s]

//Serial Communication Constants
#define SERIAL_COMM_BAUD_RATE 9600 //Baud rate [bps]

//Firebase data
bool motorForward;
bool motorBackward;
bool motorLeft;
bool motorRight;
bool overrideLaserEnable;
bool overrideLaserOn;

int laserServoXAxis = 120;
int laserServoYAxis = 0;
int motorDutyCycle;

//Program variables
float currentMeasuredDistance;
volatile bool hitFlag; // flag to know when the robot gets hit. Volatile  ensures that changes to the flag variable are immediately visible in loop()

FirebaseData firebaseData; // An object that saves the data about the value that has been read from the database or uploaded

void setup() {
  initPins();
  Serial.begin(SERIAL_COMM_BAUD_RATE);
  connectToMotorCarrier();
  initRobot();
  //Connect the Arduino to Wifi and Firebase
  connectToWifiAndFB();
  attachInterrupt(digitalPinToInterrupt(LASER_DETECT_PIN),robotRecognizedHit ,RISING); // Attaching an interrupt to the laser detector module. When the pin is on the rising state (transition 0->1), the code will execute the function robotRecognizedHit.
  openStreamOnDatabase(); // Open a stream on the database
  digitalWrite(STATUS_LED_PIN, HIGH); //Visual representation to a successful WIFI connection.
}

void loop()
{
  readFirebaseData();  
  robotMovement(motorDutyCycle);
  changeLaserServosAngle(laserServoXAxis, laserServoYAxis);
  currentMeasuredDistance = measureDistance();
  Serial.println(currentMeasuredDistance);
  if(robotDetected(currentMeasuredDistance,overrideLaserEnable))
  {
    Serial.println("Case 1: Robot detected");
    changeLaserState(HIGH);
  }
  else if(overrideLaserEnable) //the user override the laser control
  {
    Serial.println("Case 2: Laser override");
    switch(overrideLaserOn)
    {
      case true:
          changeLaserState(HIGH);
          break;
      case false:
          changeLaserState(LOW);
          break;
    }
  }
  else // If the user didn't override laser control and robot not detected
  {
    Serial.println("Case 3: Robot not detected and override disabled");
    changeLaserState(LOW);
  }
  if(hitFlag) //the user override the laser control
  {
    robotHit();
    hitFlag = false;
  }
  controller.ping();
}
/*
 * Initialize I/O Pins
 */
void initPins()
{
  pinMode(TRIG_PIN ,OUTPUT);
  pinMode(ECHO_PIN, INPUT);
  pinMode(LASER_DETECT_PIN ,INPUT);
  pinMode(BUZZER_PIN ,OUTPUT);
  pinMode(STATUS_LED_PIN, OUTPUT);
  pinMode(LASER_DIODE_PIN, OUTPUT);
}

/**
 * The function connects the arduino to WIFI and Firebase RTDB
 */
void connectToWifiAndFB()
{
  Serial.print("Connecting to Wi-Fi");
  int status = WL_IDLE_STATUS;
  while (status != WL_CONNECTED)
  {
    status = WiFi.begin(WIFI_SSID);
    delay(300);
  }
  
  //Connect to Firebase
  Firebase.begin(FIREBASE_HOST, FIREBASE_AUTH,WIFI_SSID,WIFI_PASSWORD );
  Firebase.reconnectWiFi(true);
}
/**
 * Open stream on the database. 
 */
void openStreamOnDatabase()
{
  if(!Firebase.beginStream(firebaseData , "/"))
  {
    Serial.println("Failed to open stream on the database");
  }
}
/**
 * Download the data from Firebase
 */
void readFirebaseData()
{
  //Checking if the stream is no corrupted
  if (!Firebase.readStream(firebaseData))
  {
    Serial.println("Can't read stream data");
    Serial.println("REASON: " + firebaseData.errorReason());
    Serial.println();
  }
  //Check if the stream was timedout 
  if (firebaseData.streamTimeout())
  {
    Serial.println("Couldn't find new information on the database.\nPlease use the Android app to re-open the stream");
    Serial.println();
  }
  //If stream is available and readable then printing the information of the stream. In addition, change the value on the correct variable.
  if(firebaseData.streamAvailable())
  {
    Serial.println("-------Stream Data available-------");
    Serial.println("STREAM PATH: " + firebaseData.streamPath());
    Serial.println("EVENT PATH: " + firebaseData.dataPath());
    Serial.println("DATA TYPE: " + firebaseData.dataType());
    Serial.println("EVENT TYPE: " + firebaseData.eventType());
    if(firebaseData.dataPath() == "/Movement/Forward")
    {
      motorForward = firebaseData.boolData();
    }
    else if(firebaseData.dataPath() == "/Movement/Backward")
    {
      motorBackward = firebaseData.boolData();
    }
    else if(firebaseData.dataPath() == "/Movement/Right")
    {
      motorRight = firebaseData.boolData();
    }
    else if(firebaseData.dataPath() == "/Movement/Left")
    {
      motorLeft = firebaseData.boolData();
    }
    else if(firebaseData.dataPath() == "/Laser/Override_ctrl/Enabled")
    {
      overrideLaserEnable = firebaseData.boolData();
    }
    else if(firebaseData.dataPath() == "/Laser/Override_ctrl/LASER_ON")
    {
      overrideLaserOn = firebaseData.boolData();
    }
    else if(firebaseData.dataPath() == "/Laser/X_axis")
    {
      laserServoXAxis = firebaseData.intData();
    }
    else if(firebaseData.dataPath() == "/Laser/Y_axis")
    {
      laserServoYAxis = firebaseData.intData();
    }
    else if(firebaseData.dataPath() == "/Movement/DC")
    {
      motorDutyCycle = firebaseData.intData();
    }
    else
    {
      Serial.println("Sensor data changed... ignoring the event");
    }
  }
}
/**
 * The function triggers the HC-SR04 to start a measurement 
 * vsound: speed of sound [m/s]
 * Returns:
 *  distance: the distance between the sensor to an object [m]
 */
float measureDistance()
{
  long duration;
  digitalWrite(TRIG_PIN, HIGH);
  delayMicroseconds(10);
  digitalWrite(TRIG_PIN, LOW);
  duration = pulseIn(ECHO_PIN, HIGH);
  float distance = SPEED_OF_SOUND*duration*pow(10,-6)/2;

  return distance;
}
/*
 * The function checks if currentDistance is lower from the threshold.
 * Returns:
 *  true: if the distance is lower or equal to the threshold and override is disabled
 *  false: any other case
 * 
 * Parameters:
 *  distance: the measured distance
 *  overrideEnable: if the user picked to override laser control
 */
bool robotDetected(float distance, bool overrideEnable)
{
  return distance <= THRESHOLD_DISTANCE  && !overrideEnable; // The !overrideEnable condition disables the function so the user could override the laser state.
}
/**
 * The function sets the direction and speed of the DC motors
 * duty: the dutycycle from the user
 */
void robotMovement(int duty)
{
  if(motorForward)
  {
    robotMove(duty, duty, duty, duty);
  }
  else if(motorBackward)
  {
    robotMove(-duty, -duty, -duty, -duty);
  }
  else if(motorLeft)
  {
    robotMove(-duty, duty, -duty, duty);
  }
  else if (motorRight)
  {
    robotMove(duty, -duty, duty, -duty);
  }
  else 
  {
    robotMove(0, 0, 0, 0);
  }
}
/**
 * The function changes the duty cycle of the motors
 * duty_M1: duty cycle motor 1
 * duty_M2: duty cycle motor 2
 * duty_M3: duty cycle motor 3
 * duty_M4: duty cycle motor 4
 */
void robotMove(int duty_M1 ,int duty_M2, int duty_M3, int duty_M4)
{
    M1.setDuty(duty_M1);
    M2.setDuty(duty_M2);
    M3.setDuty(duty_M3);
    M4.setDuty(duty_M4);
}
/**
 * Interrupt function. 
 * When laser detected, the function sets flag to true
 */
void robotRecognizedHit()
{
  hitFlag = true;
}

/**
 * Notifies the user by starting a buzzer and through the Android application when the robot gets hit. 
 */
void robotHit()
{
  robotMove(0,0,0,0); 
  Firebase.setBool(firebaseData, "/Sensors/Laser_Detect", true);
  buzzerState(HIGH);
  delay(100);
  buzzerState(LOW);
}
/**
 * The function changes the state of the buzzer 
 * level: HIGH or LOW
 */
void buzzerState(int level)
{
 digitalWrite(BUZZER_PIN, level);
}
/**
 * The function changes the state of the laser diode 
 * level: HIGH or LOW
 */
void changeLaserState(int level)
{
  digitalWrite(LASER_DIODE_PIN, level);
}
/**
 * The function changes the angle for servo4 and servo2  
 * angle_x: angle on the x axis
 * angle_y: angle on the y axis
 */
void changeLaserServosAngle(int angle_x, int angle_y)
{
  servo4.setAngle(angle_x);//Laser servo X axis
  servo2.setAngle(angle_y);//Laser servo Y axis
}
/**
 * The function initializes the robot
 */
void initRobot()
{
  changeLaserServosAngle(laserServoXAxis, laserServoYAxis);
  robotMove(0,0,0,0);// puts the robot on idle mode
}
/**
 * The function connects the MKR Motor Carrier to Arduino.  
 */ 
void connectToMotorCarrier()
{
  if (controller.begin()) 
  {
      Serial.print("MKR Motor Shield connected, firmware version ");
      Serial.println(controller.getFWVersion());
  } 
  else 
  {
    Serial.println("Couldn't connect! Please update the firmware");
    while (1);
  }

  // Reboot the motor controller; brings every value back to default
  Serial.println("reboot");
  controller.reboot();
  delay(500);
}
