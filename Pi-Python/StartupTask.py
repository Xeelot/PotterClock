""" Copyright (c) Microsoft. All rights reserved."""

import _wingpio as gpio
import time
import json
import urllib.request


# Pins for stepper 1
step1_pin1 = 4;
step1_pin2 = 17;
step1_pin3 = 27;
step1_pin4 = 18;

# Pins for stepper 2
step2_pin1 = 22;
step2_pin2 = 23;
step2_pin3 = 24;
step2_pin4 = 25;

# Setup the gpio bus for stepper 1
gpio.setup(step1_pin1, gpio.OUT, gpio.PUD_OFF, gpio.LOW)
gpio.setup(step1_pin2, gpio.OUT, gpio.PUD_OFF, gpio.LOW)
gpio.setup(step1_pin3, gpio.OUT, gpio.PUD_OFF, gpio.LOW)
gpio.setup(step1_pin4, gpio.OUT, gpio.PUD_OFF, gpio.LOW)

# Setup the gpio bus for stepper 2
gpio.setup(step2_pin1, gpio.OUT, gpio.PUD_OFF, gpio.LOW)
gpio.setup(step2_pin2, gpio.OUT, gpio.PUD_OFF, gpio.LOW)
gpio.setup(step2_pin3, gpio.OUT, gpio.PUD_OFF, gpio.LOW)
gpio.setup(step2_pin4, gpio.OUT, gpio.PUD_OFF, gpio.LOW)

# Function for moving the first stepper
def forward1(delay, steps):
   for i in range(0, steps):
      setStep1(1, 0, 1, 0)
      time.sleep(delay)
      setStep1(0, 1, 1, 0)
      time.sleep(delay)
      setStep1(0, 1, 0, 1)
      time.sleep(delay)
      setStep1(1, 0, 0, 1)
      time.sleep(delay)

# Function for moving the second stepper
def forward2(delay, steps):
   for i in range(0, steps):
      setStep2(1, 0, 1, 0)
      time.sleep(delay)
      setStep2(0, 1, 1, 0)
      time.sleep(delay)
      setStep2(0, 1, 0, 1)
      time.sleep(delay)
      setStep2(1, 0, 0, 1)
      time.sleep(delay)

# Function for applying the steps on stepper 1
def setStep1(p1, p2, p3, p4):
   gpio.output(step1_pin1, p1)
   gpio.output(step1_pin2, p2)
   gpio.output(step1_pin3, p3)
   gpio.output(step1_pin4, p4)

# Function for applying the steps on stepper 2
def setStep2(p1, p2, p3, p4):
   gpio.output(step2_pin1, p1)
   gpio.output(step2_pin2, p2)
   gpio.output(step2_pin3, p3)
   gpio.output(step2_pin4, p4)

# Function for converting position text to int
def posStringToInt(str):
   return {
      'Lost'    : 0,
      'Peril'   : 1,
      'Park'    : 2,
      'Texas'   : 3,
      'Holiday' : 4,
      'Work'    : 5,
      'Transit' : 6,
      'Grocery' : 7,
      'Florida' : 8,
      'Pub'     : 9,
      'Barn'    : 10,
      'Home'    : 11
      }.get(str, 0)

# Set any final initialized variables
joePosition = 0
joeChange = 0
hannahPosition = 0
hannahChange = 0

# Continue forever
while True:
   # Random code for now
   #test = 1
   #forward1(0.01, (test * 43))
   #setStep1(0, 0, 0, 0)
   #forward2(0.02, (test * 43))
   #setStep2(0, 0, 0, 0)
   # URL GET call to retrieve locations
   response = urllib.request.urlopen("http://192.168.0.126:3000/api/currentlocation")
   output = json.loads(response.read().decode('utf8'))

   # Reset players
   joeFound = False
   hannahFound = False
   # Search through response for names
   for i in range(len(output)-1, -1, -1):
      name = output[i]['userid']
      pos = posStringToInt(output[i]['position'])
      if(name == 'Joe' and joeFound == False):
         # Found Joe, calculate from original position
         joeFound = True
         joeChange = (joePosition + 12 - pos) % 12
         joePosition = pos
         print('Joe: ' + str(pos) + ' change: ' + str(joeChange))
      if(name == 'Hannah' and hannahFound == False):
         # Found Hannah, calculate from original position
         hannahFound = True
         hannahChange = (hannahPosition + 12 - pos) % 12
         hannahPosition = pos
         print('Hannah: ' + str(pos) + ' change: ' + str(hannahChange))
   
   # Handle movement as needed
   forward1(0.01, (joeChange * 43))
   setStep1(0, 0, 0, 0)
   time.sleep(0.5)
   forward2(0.02, (hannahChange * 43))
   setStep2(0, 0, 0, 0)

   # Sleep until next call
   time.sleep(10)

# Cleanup at end of run
gpio.cleanup()
