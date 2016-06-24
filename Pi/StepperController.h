#pragma once

namespace BlinkyCpp
{
   // ***** Defined values and constants used for controlling the stepper motors
   #define NUM_PINS 4
   #define STEPPER1 1

   // ***** Constant brushes used for debugging
   //Windows::UI::Xaml::Media::SolidColorBrush ^redBrush_ = ref new Windows::UI::Xaml::Media::SolidColorBrush(Windows::UI::Colors::Red);
   //Windows::UI::Xaml::Media::SolidColorBrush ^blueBrush_ = ref new Windows::UI::Xaml::Media::SolidColorBrush(Windows::UI::Colors::Blue);
   //Windows::UI::Xaml::Media::SolidColorBrush ^greenBrush_ = ref new Windows::UI::Xaml::Media::SolidColorBrush(Windows::UI::Colors::Green);
   //Windows::UI::Xaml::Media::SolidColorBrush ^yellowBrush_ = ref new Windows::UI::Xaml::Media::SolidColorBrush(Windows::UI::Colors::Yellow);
   //Windows::UI::Xaml::Media::SolidColorBrush ^grayBrush_ = ref new Windows::UI::Xaml::Media::SolidColorBrush(Windows::UI::Colors::LightGray);

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
      WORK = 0,
      HOME = 1,
      PARK = 2,
      BARN = 3,
      LOST = 4,
      PERIL = 5,
      FLORIDA = 6,
      TEXAS = 7,
      TRANSIT = 8,
      HOLIDAY = 9,
      PUB = 10,
      GROCERY = 11
   };

   // ***** Enumeration for the state of the controller
   enum InitState : int {
      INIT_NONE = 0,
      INIT_GOOD = 1,
      INIT_FAIL = 2
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

   private:
      // Container for all the pin values
      Windows::Devices::Gpio::GpioPinValue pinValues[NUM_PINS];
      // Container for all the pins
      Windows::Devices::Gpio::GpioPin ^pins[NUM_PINS];
      // Current state of the stepper motor
      StepperState stepState;
      // Current position of the stepper motor
      PositionState posState;
   };
}

