#pragma once

namespace BlinkyCpp
{
   // ***** Defined values and constants used for controlling the stepper motors
   #define NUM_PINS 4
   #define POS2TICKS 170
   #define TICK_DELAY 10

   // ***** Enumeration for the 5 states of the stepper motor
   enum StepperState : int {
      STEP_OFF = 0,
      STEP_1 = 1,
      STEP_2 = 2,
      STEP_3 = 3,
      STEP_4 = 4
   };

   // ***** Enumeration for the position of the hand on the clock face
   enum PositionState : int {
      LOST = 0,
      PERIL = 1,
      PARK = 2,
      TEXAS = 3,
      HOLIDAY = 4,
      WORK = 5,
      TRANSIT = 6,
      GROCERY = 7,
      FLORIDA = 8,
      PUB = 9,
      BARN = 10,
      HOME = 11,
      POSITION_MAX = 12
   };

   // ***** Enumeration for the state of the controller
   enum InitState : int {
      INIT_NONE = 0,
      INIT_GOOD = 1,
      INIT_FAIL = 2
   };

   // ***** Enumeration on which way to turn
   enum Direction : int {
      CLOCKWISE = 0,
      COUNTER_CLOCKWISE = 1
   };


   // ***** Class used for controlling a stepper motor
   class StepperController sealed
   {
   public:
      StepperController();
      // Initialize the stepper controller with the 4 pins to be used
      InitState initStepper(int pin1, int pin2, int pin3, int pin4);
      // Set the state to a position on the clock
      void setPosition(PositionState pos);
      // Retrieve the position of the stepper
      PositionState getPosition();
      // Retrieve the state of the controller
      InitState getInitState();

   private:
      // Internal call to turn the stepper motor
      void moveTicks(int ticks, Direction dir);
      // Internal call to set and write the pins for a specific state
      void setStepperState(Windows::Devices::Gpio::GpioPinValue val1, Windows::Devices::Gpio::GpioPinValue val2,
                           Windows::Devices::Gpio::GpioPinValue val3, Windows::Devices::Gpio::GpioPinValue val4);
      // Container for all the pin values
      Windows::Devices::Gpio::GpioPinValue pinValues[NUM_PINS];
      // Container for all the pins
      Windows::Devices::Gpio::GpioPin ^pins[NUM_PINS];
      // Current state of the stepper motor
      StepperState stepState;
      // Current position of the stepper motor
      PositionState posState;
      // Current state of initialization
      InitState initState;
   };
}

