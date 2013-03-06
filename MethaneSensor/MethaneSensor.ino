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
  int green = 255*(constrain(sensorValue, 0,512)/512);
  int red =255*(constrain(sensorValue-512, 0,512)/512);

  // constrain the values to 0 - 255 and invert
  // if you're using a common-cathode LED, just use "constrain(color, 0, 255);"
  red = constrain(red, 0, 255);
  green = 255 - constrain(green, 0, 255);

  // fade the red, green, and blue legs of the LED: 
  analogWrite(redPin, red);
  analogWrite(greenPin, green);

  // print the three numbers in one string as hexadecimal:
  Serial.print('Sensor:'); 
  Serial.print(sensorValue);
  Serial.print("\t"); 
  Serial.print('Red:'); 
  Serial.print(red);
  Serial.print("\t");
  Serial.print('Green:'); 
  Serial.println(green);
}











