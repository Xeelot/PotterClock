/*
    Copyright(c) Microsoft Open Technologies, Inc. All rights reserved.

    The MIT License(MIT)

    Permission is hereby granted, free of charge, to any person obtaining a copy
    of this software and associated documentation files(the "Software"), to deal
    in the Software without restriction, including without limitation the rights
    to use, copy, modify, merge, publish, distribute, sublicense, and / or sell
    copies of the Software, and to permit persons to whom the Software is
    furnished to do so, subject to the following conditions :

    The above copyright notice and this permission notice shall be included in
    all copies or substantial portions of the Software.

    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
    IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
    LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
    THE SOFTWARE.
*/

//
// MainPage.xaml.h
// Declaration of the MainPage class.
//

#pragma once

#include "MainPage.g.h"
#include "StepperController.h"

namespace BlinkyCpp
{
   // ***** Stepper motor 1 pins used
   const int STEP1_PIN1 = 4;
   const int STEP1_PIN2 = 17;
   const int STEP1_PIN3 = 27;
   const int STEP1_PIN4 = 18;
   // ***** Stepper motor 2 pins used
   const int STEP2_PIN1 = 22;
   const int STEP2_PIN2 = 23;
   const int STEP2_PIN3 = 24;
   const int STEP2_PIN4 = 25;


   public ref class MainPage sealed
   {
   public:
      MainPage();

   private:
      void InitGPIO();
      void OnTick(Platform::Object ^sender, Platform::Object ^args);

      StepperController control1;
      StepperController control2;


      Windows::UI::Xaml::DispatcherTimer ^timer_;
      //Windows::Devices::Gpio::GpioPinValue step1pin1Value = Windows::Devices::Gpio::GpioPinValue::Low;
      //Windows::Devices::Gpio::GpioPinValue step1pin2Value = Windows::Devices::Gpio::GpioPinValue::Low;
      //Windows::Devices::Gpio::GpioPinValue step1pin3Value = Windows::Devices::Gpio::GpioPinValue::Low;
      //Windows::Devices::Gpio::GpioPinValue step1pin4Value = Windows::Devices::Gpio::GpioPinValue::Low;
      //StepperState stepperState;
      //Windows::Devices::Gpio::GpioPin ^step1pin1;
      //Windows::Devices::Gpio::GpioPin ^step1pin2;
      //Windows::Devices::Gpio::GpioPin ^step1pin3;
      //Windows::Devices::Gpio::GpioPin ^step1pin4;
      Windows::UI::Xaml::Media::SolidColorBrush ^redBrush_ = ref new Windows::UI::Xaml::Media::SolidColorBrush(Windows::UI::Colors::Red);
      Windows::UI::Xaml::Media::SolidColorBrush ^blueBrush_ = ref new Windows::UI::Xaml::Media::SolidColorBrush(Windows::UI::Colors::Blue);
      Windows::UI::Xaml::Media::SolidColorBrush ^greenBrush_ = ref new Windows::UI::Xaml::Media::SolidColorBrush(Windows::UI::Colors::Green);
      Windows::UI::Xaml::Media::SolidColorBrush ^yellowBrush_ = ref new Windows::UI::Xaml::Media::SolidColorBrush(Windows::UI::Colors::Yellow);
      Windows::UI::Xaml::Media::SolidColorBrush ^grayBrush_ = ref new Windows::UI::Xaml::Media::SolidColorBrush(Windows::UI::Colors::LightGray);
   };
}
