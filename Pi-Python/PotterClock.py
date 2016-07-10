import sys
import time
import json
import boto3
import decimal
from boto3.dynamodb.conditions import Key, Attr
from botocore.exceptions import ClientError
from enum import IntEnum
from random import randint

class Output(IntEnum):
   NONE = 0
   FUNC = 1
   VERB = 2

verbose = Output.FUNC

if verbose > Output.NONE:
   print("*** Platform being used: " + sys.platform)
# Import based on operating system
if sys.platform == "win32":
   import _wingpio as gpio
elif sys.platform == "linux":
   import RPi.GPIO as gpio

# DEFINES for execution
NUM_INDEXES = 12
STEP_DELAY = 0.01
STEP_INDEX = 43
FAIL_COUNT = 10
SLEEP_USER = 1
SLEEP_LONG = 10
BACKWARD = 0
FORWARD = 1
# Pins for stepper 1
STEP1_PIN1 = 4;
STEP1_PIN2 = 17;
STEP1_PIN3 = 27;
STEP1_PIN4 = 18;
# Pins for stepper 2
STEP2_PIN1 = 22;
STEP2_PIN2 = 23;
STEP2_PIN3 = 24;
STEP2_PIN4 = 25;


# Helper class to convert a DynamoDB item to JSON.
class DecimalEncoder(json.JSONEncoder):
   def default(self, o):
      if isinstance(o, decimal.Decimal):
         if o % 1 > 0:
            return float(o)
         else:
            return int(o)
      return super(DecimalEncoder, self).default(o)

# User class for reading items from the CurrentLocationTable
class User:
   numUsers = 0
   def __init__(self, name, index):
      self.name = name
      self.index = index
      User.numUsers += 1
   def calcIndexChange(self, newIndex):
      direction = randint(BACKWARD, FORWARD)
      change = 0
      if direction == FORWARD:
         change = ((NUM_INDEXES - self.index + newIndex) % NUM_INDEXES)
      else:
         change = ((NUM_INDEXES + self.index - newIndex) % NUM_INDEXES)
      if verbose > Output.NONE:
         print("*** Old Index: " + str(self.index) + "  New Index: " + str(newIndex) + "  Change: " + str(change))
      self.index = newIndex
      return { 'change':change, 'direction':direction }

# Stepper class for controlling a stepper motor through the GPIO
class Stepper:
   numSteppers = 0
   def __init__(self, p1, p2, p3, p4):
      self.pin1 = p1
      self.pin2 = p2
      self.pin3 = p3
      self.pin4 = p4
      if sys.platform == "win32":
         gpio.setup(self.pin1, gpio.OUT, gpio.PUD_OFF, gpio.LOW)
         gpio.setup(self.pin2, gpio.OUT, gpio.PUD_OFF, gpio.LOW)
         gpio.setup(self.pin3, gpio.OUT, gpio.PUD_OFF, gpio.LOW)
         gpio.setup(self.pin4, gpio.OUT, gpio.PUD_OFF, gpio.LOW)
      elif sys.platform == "linux":
         gpio.setup(self.pin1, gpio.OUT)
         gpio.setup(self.pin2, gpio.OUT)
         gpio.setup(self.pin3, gpio.OUT)
         gpio.setup(self.pin4, gpio.OUT)
         self.setStep(0, 0, 0, 0)
      Stepper.numSteppers += 1
   def setStep(self, v1, v2, v3, v4):
      gpio.output(self.pin1, v1)
      gpio.output(self.pin2, v2)
      gpio.output(self.pin3, v3)
      gpio.output(self.pin4, v4)
   def forward(self, steps):
      if verbose > Output.NONE:
         print("*** Moving stepper FORWARD " + str(steps) + " indexes...")
      for i in range(0, (steps * STEP_INDEX)):
         self.setStep(1, 0, 0, 0)
         time.sleep(STEP_DELAY)
         self.setStep(0, 1, 0, 0)
         time.sleep(STEP_DELAY)
         self.setStep(0, 0, 1, 0)
         time.sleep(STEP_DELAY)
         self.setStep(0, 0, 0, 1)
         time.sleep(STEP_DELAY)
      self.setStep(0, 0, 0, 0)
      if verbose > Output.NONE:
         print("*** Stepper movement complete!")
   def backward(self, steps):
      if verbose > Output.NONE:
         print("*** Moving stepper BACKWARD " + str(steps) + " indexes...")
      for i in range(0, (steps * STEP_INDEX)):
         self.setStep(0, 0, 0, 1)
         time.sleep(STEP_DELAY)
         self.setStep(0, 0, 1, 0)
         time.sleep(STEP_DELAY)
         self.setStep(0, 1, 0, 0)
         time.sleep(STEP_DELAY)
         self.setStep(1, 0, 0, 0)
         time.sleep(STEP_DELAY)
      self.setStep(0, 0, 0, 0)
      if verbose > Output.NONE:
         print("*** Stepper movement complete!")


# Setup the gpio bus for stepper motors on linux only
if sys.platform == "linux":
   gpio.setmode(gpio.BCM)

# Instance of DynamoDB used to communicate with the CurrentLocationTable
db = boto3.resource("dynamodb", region_name="us-west-2", endpoint_url="https://dynamodb.us-west-2.amazonaws.com")

# Instance of the CurrentLocationTable from DynamoDB
table = db.Table('CurrentLocationTable')

# Create a list of users
users = []
users.append(User("Joe", 0))
users.append(User("Hannah", 0))

# Create a list of stepper motors
steppers = []
steppers.append(Stepper(STEP1_PIN1, STEP1_PIN2, STEP1_PIN3, STEP1_PIN4))
steppers.append(Stepper(STEP2_PIN1, STEP2_PIN2, STEP2_PIN3, STEP2_PIN4))


# Set any final initialized variables
failedConnCount = 0
continueLoop = True

# Continue while connection is good
if verbose > Output.NONE:
   print("*** Beginning loop to run the Potter Clock...")
while continueLoop:
   # Loop through all users
   for i in range(0, User.numUsers):
      currUser = users[i]
      currStepper = steppers[i]
      try:
         # Try to access the user data from the table
         if verbose > Output.NONE:
            print("\n*** Making call for user " + currUser.name)
         response = table.get_item( Key={ 'userid': currUser.name } )
      except ClientError as e:
         # DB call was unsuccessful, print a message and increment counter
         if verbose > Output.NONE:
            print("*** Connection to AWS failed to establish")
         if verbose == Output.VERB:
            print(e.response['Error']['Message'])
         failedConnCount += 1
      else:
         # Connection successful, reset the count and process the item
         if verbose > Output.NONE:
            print("*** Item retrieved from AWS DB!")
         failedConnCount = 0
         item = response['Item']
         if verbose == Output.VERB:
            print(json.dumps(item, indent=4, cls=DecimalEncoder))
         # Grab the index from the response and calculate the change from current user index
         newIndex = item['index']
         result = currUser.calcIndexChange(newIndex)
         # Move the stepper motors according to the change and apply a brief sleep
         if result['direction'] == FORWARD:
            currStepper.forward(int(result['change']))
         else:
            currStepper.backward(int(result['change']))   
         time.sleep(SLEEP_USER)

   if failedConnCount >= FAIL_COUNT:
      # Stop execution if too many failures occur in a row
      if verbose > Output.NONE:
         print("\n*** Max connection failures reached, exiting...\n")
      continueLoop = False
   else:
      # Sleep for a longer period after
      if verbose > Output.NONE:
         print("\n*** Waiting " + str(SLEEP_LONG) + " seconds for next poll...\n")
      time.sleep(SLEEP_LONG)

# Cleanup at end of run
if verbose > Output.NONE:
   print("\n*** Goodbye!\n")
gpio.cleanup()
