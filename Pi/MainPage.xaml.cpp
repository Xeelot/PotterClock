// Copyright (c) Microsoft. All rights reserved.

//
// MainPage.xaml.cpp
// Implementation of the MainPage class.
//

#include "pch.h"
#include "MainPage.xaml.h"
#include <string>

using namespace BlinkyCpp;

using namespace Platform;
using namespace Windows::Foundation;
using namespace Windows::Foundation::Collections;
using namespace Windows::UI::Xaml;
using namespace Windows::UI::Xaml::Controls;
using namespace Windows::UI::Xaml::Controls::Primitives;
using namespace Windows::UI::Xaml::Data;
using namespace Windows::UI::Xaml::Input;
using namespace Windows::UI::Xaml::Media;
using namespace Windows::UI::Xaml::Navigation;
using namespace Windows::Devices::Enumeration;
using namespace Windows::Devices::Gpio;
using namespace concurrency;


MainPage::MainPage()
{
    InitializeComponent();

    InitGPIO();
	if ((step1pin1 != nullptr) && (step1pin2 != nullptr) && (step1pin3 != nullptr) && (step1pin4 != nullptr))
	{
		timer_ = ref new DispatcherTimer();
		TimeSpan interval;
		interval.Duration = 100;
		timer_->Interval = interval;
		timer_->Tick += ref new EventHandler<Object ^>(this, &MainPage::OnTick);
		timer_->Start();
	}
}


void MainPage::InitGPIO()
{
	auto gpio = GpioController::GetDefault();

	if (gpio == nullptr)
	{
		step1pin1 = nullptr;
      step1pin2 = nullptr;
      step1pin3 = nullptr;
      step1pin4 = nullptr;
		GpioStatus->Text = "There is no GPIO controller on this device.";
		return;
	}

   stepperState = StepperState::STEP_OFF;
	step1pin1 = gpio->OpenPin(STEP1_PIN1);
	step1pin1->Write(step1pin1Value);
	step1pin1->SetDriveMode(GpioPinDriveMode::Output);
   step1pin2 = gpio->OpenPin(STEP1_PIN2);
   step1pin2->Write(step1pin2Value);
   step1pin2->SetDriveMode(GpioPinDriveMode::Output);
   step1pin3 = gpio->OpenPin(STEP1_PIN3);
   step1pin3->Write(step1pin3Value);
   step1pin3->SetDriveMode(GpioPinDriveMode::Output);
   step1pin4 = gpio->OpenPin(STEP1_PIN4);
   step1pin4->Write(step1pin4Value);
   step1pin4->SetDriveMode(GpioPinDriveMode::Output);

	GpioStatus->Text = "GPIO pin initialized correctly.";
}


void MainPage::writePins()
{
   step1pin1->Write(step1pin1Value);
   step1pin2->Write(step1pin2Value);
   step1pin3->Write(step1pin3Value);
   step1pin4->Write(step1pin4Value);
}


void MainPage::OnTick(Object ^sender, Object ^args)
{
   switch (stepperState)
   {
   case StepperState::STEP_OFF:
      step1pin1Value = GpioPinValue::High;
      step1pin2Value = GpioPinValue::Low;
      step1pin3Value = GpioPinValue::High;
      step1pin4Value = GpioPinValue::Low;
      stepperState = StepperState::STEP_1;
      LED->Fill = redBrush_;
      break;
   case StepperState::STEP_1:
      step1pin1Value = GpioPinValue::Low;
      step1pin2Value = GpioPinValue::High;
      step1pin3Value = GpioPinValue::High;
      step1pin4Value = GpioPinValue::Low;
      stepperState = StepperState::STEP_2;
      LED->Fill = blueBrush_;
      break;
   case StepperState::STEP_2:
      step1pin1Value = GpioPinValue::Low;
      step1pin2Value = GpioPinValue::High;
      step1pin3Value = GpioPinValue::Low;
      step1pin4Value = GpioPinValue::High;
      stepperState = StepperState::STEP_3;
      LED->Fill = greenBrush_;
      break;
   case StepperState::STEP_3:
      step1pin1Value = GpioPinValue::High;
      step1pin2Value = GpioPinValue::Low;
      step1pin3Value = GpioPinValue::Low;
      step1pin4Value = GpioPinValue::High;
      stepperState = StepperState::STEP_4;
      LED->Fill = yellowBrush_;
      break;
   case StepperState::STEP_4:
   default:
      step1pin1Value = GpioPinValue::Low;
      step1pin2Value = GpioPinValue::Low;
      step1pin3Value = GpioPinValue::Low;
      step1pin4Value = GpioPinValue::Low;
      stepperState = StepperState::STEP_OFF;
      LED->Fill = grayBrush_;
      break;
   }

   writePins();
}




