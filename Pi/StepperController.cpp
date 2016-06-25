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
   posState = PositionState::LOST;
   initState = InitState::INIT_NONE;
}

PositionState StepperController::getPosition()
{
   // Return the current position of the hand on the face of the clock
   return posState;
}

InitState StepperController::getInitState()
{
   // Return the current state of the controller
   return initState;
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
      }
      initState = InitState::INIT_FAIL;
      return initState;
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

   initState = InitState::INIT_GOOD;
   return initState;
}


void StepperController::setPosition(PositionState pos)
{
   // Calculate the difference in positions
   int positionsToTurn = (PositionState::POSITION_MAX + pos - posState) % PositionState::POSITION_MAX;
   // Calculate the number of ticks to step to reach position
   int ticksToTurn = positionsToTurn * POS2TICKS;
   // Move forward or backward, one of the cool parts of a magic clock
   // Difference:   (1)-(6)  +12 (13)-(18) %12 (1)-(6)  then clockwise
   // Difference:   (7)-(11) +12 (19)-(23) %12 (7)-(11) then counter-clockwise
   // Difference:  (-5)-(-1) +12  (7)-(11) %12 (7)-(11) then counter-clockwise
   // Difference: (-11)-(-6) +12  (1)-(6)  %12 (1)-(6)  then clockwise
   if ((positionsToTurn > 0) && (positionsToTurn <= 6))
   {
      moveTicks(ticksToTurn, Direction::CLOCKWISE);
   }
   else if ((positionsToTurn > 6) && (positionsToTurn < 12))
   {
      moveTicks(ticksToTurn, Direction::COUNTER_CLOCKWISE);
   }
   // Set our new position once complete
   posState = pos;
}


void StepperController::moveTicks(int ticks, Direction dir)
{
   // Loop over the amount of ticks input to turn to the next position
   for (int i = 0; i < ticks; ++i)
   {
      switch (stepState)
      {
      case StepperState::STEP_OFF:
      case StepperState::STEP_4:
         setStepperState(Windows::Devices::Gpio::GpioPinValue::High, Windows::Devices::Gpio::GpioPinValue::Low,
                         Windows::Devices::Gpio::GpioPinValue::High, Windows::Devices::Gpio::GpioPinValue::Low);
         if (dir == Direction::CLOCKWISE) 
         {
            stepState = StepperState::STEP_1;
         }
         else 
         {
            stepState = StepperState::STEP_3;
         }
         break;
      case StepperState::STEP_1:
         setStepperState(Windows::Devices::Gpio::GpioPinValue::Low, Windows::Devices::Gpio::GpioPinValue::High,
                         Windows::Devices::Gpio::GpioPinValue::High, Windows::Devices::Gpio::GpioPinValue::Low);
         if (dir == Direction::CLOCKWISE)
         {
            stepState = StepperState::STEP_2;
         }
         else
         {
            stepState = StepperState::STEP_4;
         }
         break;
      case StepperState::STEP_2:
         setStepperState(Windows::Devices::Gpio::GpioPinValue::Low, Windows::Devices::Gpio::GpioPinValue::High,
                         Windows::Devices::Gpio::GpioPinValue::Low, Windows::Devices::Gpio::GpioPinValue::High);
         if (dir == Direction::CLOCKWISE)
         {
            stepState = StepperState::STEP_3;
         }
         else
         {
            stepState = StepperState::STEP_1;
         }
         break;
      case StepperState::STEP_3:
         setStepperState(Windows::Devices::Gpio::GpioPinValue::High, Windows::Devices::Gpio::GpioPinValue::Low,
                         Windows::Devices::Gpio::GpioPinValue::Low, Windows::Devices::Gpio::GpioPinValue::High);
         if (dir == Direction::CLOCKWISE)
         {
            stepState = StepperState::STEP_4;
         }
         else
         {
            stepState = StepperState::STEP_2;
         }
         break;
      }
      //TODO: determine if a delay will actually help the rotation
      Sleep(TICK_DELAY);
   }
   // Turn off the motor after completion
   setStepperState(Windows::Devices::Gpio::GpioPinValue::Low, Windows::Devices::Gpio::GpioPinValue::Low,
                   Windows::Devices::Gpio::GpioPinValue::Low, Windows::Devices::Gpio::GpioPinValue::Low);
}


void StepperController::setStepperState(Windows::Devices::Gpio::GpioPinValue val1, Windows::Devices::Gpio::GpioPinValue val2,
                                        Windows::Devices::Gpio::GpioPinValue val3, Windows::Devices::Gpio::GpioPinValue val4)
{
   // Read in the values pass in
   //TODO: improve numbers being passed, generalize
   pinValues[0] = val1;
   pinValues[1] = val2;
   pinValues[2] = val3;
   pinValues[3] = val4;
   // Write the values to the pins
   for (int i = 0; i < NUM_PINS; ++i)
   {
      pins[i]->Write(pinValues[i]);
   }
}