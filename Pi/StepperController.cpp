#include "pch.h"
#include "StepperController.h"

using namespace BlinkyCpp;
using namespace Windows::Devices::Gpio;

StepperController::StepperController()
{
   // Initialize pin values and states
   for (int i = 0; i < NUM_PINS; ++i)
   {
      pinValues[i] = Windows::Devices::Gpio::GpioPinValue::Low;
   }
   stepState = StepperState::STEP_OFF;
   posState = PositionState::WORK;
}

InitState StepperController::initStepper(int pin1, int pin2, int pin3, int pin4)
{
   // Grab the default GPIO controller
   auto gpio = GpioController::GetDefault();

   // If null, set to null to avoid issues and exit
   if (gpio == nullptr)
   {
      for (int i = 0; i < NUM_PINS; ++i)
      {
         pins[i] = nullptr;
         return InitState::INIT_FAIL;
      }
   }

   // GPIO should be good at this point, set the pins and output modes on the GPIO
   pins[0] = gpio->OpenPin(pin1);
   pins[1] = gpio->OpenPin(pin2);
   pins[2] = gpio->OpenPin(pin3);
   pins[3] = gpio->OpenPin(pin4);
   for (int i = 0; i < NUM_PINS; ++i)
   {
      pins[i]->Write(pinValues[i]);
      pins[i]->SetDriveMode(GpioPinDriveMode::Output);
   }

   return InitState::INIT_GOOD;
}

void StepperController::setPosition(PositionState pos)
{
   //TODO: implement movement to next position
}

PositionState StepperController::getPosition()
{
   // Return the current position of the hand on the face of the clock
   return posState;
}