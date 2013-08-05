/*
  Reading a serial ASCII-encoded string.
 
 This sketch demonstrates the Serial parseInt() function.
 It looks for an ASCII string of comma-separated values.
 It parses them into ints, and uses those to fade an RGB LED.
 
 Circuit: Common-anode RGB LED wired like so:
 * Red cathode: digital pin 3
 * Green cathode: digital pin 5
 * blue cathode: digital pin 6
 * anode: +5V
 
 created 13 Apr 2012
 by Tom Igoe
 
 This example code is in the public domain.
 */

// pins for the LEDs:
const int redPin = 3;
const int greenPin = 5;

const int maxVal = 1024;
const int minVal = 250;

int sensor =0;

void setup() {
  // initialize serial:
  Serial.begin(9600);
  // make the pins outputs:
  pinMode(redPin, OUTPUT); 
  pinMode(greenPin, OUTPUT); 

  analogWrite(redPin, 0);
  analogWrite(greenPin, 0);
}

void loop() {
  int sensorValue = analogRead(sensor)+1;

  float delta = maxVal - minVal;
  float mid = (delta/2.0) + minVal;
  sensorValue =constrain(sensorValue, minVal,maxVal);
  float valOne =constrain( float( sensorValue - minVal) / float(mid - minVal), 0, 1);
  float valTwo = constrain( float( sensorValue - mid) / float(maxVal - mid), 0, 1);

  //  Serial.print("Delta: "); 
  //  Serial.print(delta);
  //  Serial.print("\t"); 
  //  Serial.print("Mid: "); 
  //  Serial.println(mid);

  //  int greenInitial = constrain(sensorValue - 512, 0,512);
  //  int redInitial = constrain(sensorValue, 0,512);

  //  int green = int(255.0 * ( greenInitial / 512.0));
  //  int red = int(255.0 * (redInitial / 512.0)); 

  int green = int(255.0 * valTwo);
  int red = int(255.0 * valOne);


  Serial.print("Sensor: "); 
  Serial.print(sensorValue);
  Serial.print("\t"); 
  Serial.print("Red Initial: "); 
  Serial.print(valOne);
  Serial.print("\t");
  Serial.print("Green Initial: "); 
  Serial.print(valTwo);

  Serial.print("\t");
  Serial.print("Red: "); 
  Serial.print(red);
  Serial.print("\t");
  Serial.print("Green: "); 
  Serial.println(green);

  // constrain the values to 0 - 255 and invert
  // if you're using a common-cathode LED, just use "constrain(color, 0, 255);"
  red = 255 - constrain(red, 0, 255);
  green = constrain(green, 0, 255);

  // fade the red, green, and blue legs of the LED: 
  analogWrite(redPin, red);
  analogWrite(greenPin, green);

  //  analogWrite(redPin,255);
  //  analogWrite(greenPin,255);

  // print the three numbers in one string as hexadecimal:

}






















