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

#include <Max3421e.h>
#include <Usb.h>
#include <AndroidAccessory.h>

AndroidAccessory acc("Google, Inc.",
"DemoKit",
"DemoKit Arduino Board",
"1.0",
"http://www.android.com",
"0000000012345678");

// pins for the LEDs:
const int redPin = 3;
const int greenPin = 5;
const int bluePin = 6;

void setup(void)
{
  Serial.begin(115200);

  // make the pins outputs:
  pinMode(redPin, OUTPUT);
  pinMode(greenPin, OUTPUT);
  pinMode(bluePin, OUTPUT);

  acc.powerOn();
}

struct SendBuf {
  void Reset() { 
    pos = 0; 
    memset(buf, 0, sizeof(buf)); 
  }
  void Append(int val) { 
    buf[pos++] = val; 
  }
  void Append(uint8_t val) { 
    buf[pos++] = val; 
  }
  void Append(uint16_t val) { 
    buf[pos++] = val >> 8; 
    buf[pos++] = val; 
  }
  void Append(uint32_t val) { 
    buf[pos++] = val >> 24; 
    buf[pos++] = val >> 16; 
    buf[pos++] = val >> 8; 
    buf[pos++] = val; 
  }

  int Send() { 
    Serial.println('######### Print #########');
    //    Serial.println(buf);
    return acc.write(buf, pos); 
  }

  uint8_t buf[128];
  int pos;
};

void loop()
{
  Serial.println('Loop');
  char returnChar = 13;
  uint8_t msg[64];
  if (acc.isConnected()) {
    int recvLen = acc.read(msg, sizeof(msg));
    int red=-1;
    int green=-1;
    int blue=-1;
    Serial.println('######### Receive #########');
    //    Serial.println(msg);

    if (recvLen >= 4) {
      // look for the next valid integer in the incoming usb stream:
      red = msg[0];
      // do it again:
      green = msg[1];
      // do it again:
      blue = msg[2];
      // look for the newline. That's the end of your
      // sentence:

      if (msg[3] == returnChar) {
        // constrain the values to 0 - 255 and invert
        // if you're using a common-cathode LED, just use "constrain(color, 0, 255);"
        red = 255 - constrain(red, 0, 255);
        green = 255 - constrain(green, 0, 255);
        blue = 255 - constrain(blue, 0, 255);

        // fade the red, green, and blue legs of the LED:
        analogWrite(redPin, red);
        analogWrite(greenPin, green);
        analogWrite(bluePin, blue);
      }
      SendBuf outBuf;
      outBuf.Reset();

      outBuf.Append(red);
      outBuf.Append(green);
      outBuf.Append(blue);
      outBuf.Append(returnChar);
      outBuf.Send();
    }
    // print the three numbers in one string as hexadecimal:

    //        Serial.print(red, HEX);
    //        Serial.print(green, HEX);
    //        Serial.println(blue, HEX);


    //    L.accessorySend(outmsg, outmsgLen);
  }
  else{
    //set the accessory to its default state
  }
}







