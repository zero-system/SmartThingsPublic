/**
 *  Virtual Thermostat Variable Speed
 *
 *  Copyright 2017 Zero System
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */


import java.io.*
import java.lang.*
import java.math.BigDecimal
import java.math.BigInteger
import java.net.*
import java.util.*
import groovy.lang.*
import groovy.util.*


//definition
//(
//    name : "Virtual Thermostat Variable Speed" ,
//    namespace: "zero-system" ,
//    author: "Zero System" ,
//    description: "This is an extension of Virtual Thermostat. It is for cooling a room with a fan connected to a dimmer. " ,
//    category: "" ,
//    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png" ,
//    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png" ,
//    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png"
//)


preferences
		{
			section( "Choose a temperature sensor(s)." )
					{
						pargraph "If multiple sensors are used, the average temperature will be calculated."
						input( title: "Temperature Sensor" , name: "tempSensor" , type: "capability.temperatureMeasurement" , multiple: false , required: true )
					}
			
			section( "Select the cooling fan dimmer outlet." )
					{
						pargraph "These outlet will change their level from (0% - 100%) based on room temperature."
						input( title: "Dimmer Outlet" , name: "outlet" , type: "capability.switchLevel" , multiple: false , required: true )
					}
			
			section( "Set the desired target temperature." )
					{
						pargraph "The PID control will attempt to cool to set temperature."
						input( title: "Target Temp" , name: "targetTemp" , type: "decimal" , required: true , description: "70 (deg)" )
					}

//			section( "Step time, in minutes." )
//					{
//						pargraph( "If temp is over target temp, how many min(s) between corrections. Default: 15 mins" )
//						input( title: "Step Time (min)" , name: "stepMinutes" , type: "number" , range: "1..*" , defaultValue: 15 , required: false , description: "15 (min)" )
//					}
//
//			section( "Step size percentage." )
//					{
//						pargraph( " When the room temperature is over target temperature, how large of a correction to be taken. Default: 10%" )
//						input( title: "Step Size" , name: "stepSize" , type: "number" , range: "1..100" , defaultValue: 10 , required: false , discription: "10%" )
//					}
			
			section( "Time frame." )
					{
						paragraph( "The time frame the PID will run." )
						input( title: "Start Time" , name: "startTime" , type: "time" , required: true , discription: "9:00 AM" )
						input( title: "Stop Time" , name: "stopTime" , type: "time" , required: true , discription: "6:00 PM" )
					}
			
		}

def installed()
{
	log.debug "Installed with settings: ${settings}"
	
	initialize()
}

def updated()
{
	log.debug "Updated with settings: ${settings}"
	
	unsubscribe()
	initialize()
}

def initialize()
{
	// TODO: subscribe to attributes, devices, locations, etc.
	
	/*working variables*/
	state.errorSum = 0.0
	state.lastError = 0.0
	
	state.kp = 0.0
	state.ki = 0.0
	state.kd = 0.0
	
	schedule( "30 0 0 ? * * *" , scheduledHandler )
}
// TODO: Loop
void scheduledHandler()
{
	if ( run() )
	{
		PID()
	}
}

// TODO: State (Run or Not)
boolean run()
{
	Date time = new Date()
	// TODO: test to see if time could be used in-place of "new Date()"
	boolean withinTimeFrame = timeOfDayIsBetween( startTime , stopTime , time , location.timeZone )
	
	log.debug "run: TIME( start: $startTime, stop: $stopTime, time: $time, value: $withinTimeFrame )"
	
	return withinTimeFrame
}

// TODO: PID Control
void PID()
{
	/*How long since we last calculated*/
	long currentTime = now()
	double timeChange = ( double ) ( currentTime - state.lastTime )
	log.debug "PID: TIME( currentTime: $currentTime, timeChange: $timeChange )"
	
	/*Compute all the working error variables*/
	double error = targetTemp - tempSensor.currentTemperature
	state.errorSum += ( error * timeChange )
	double dError = ( error - state.lastError ) / timeChange
	log.debug "PID: ERROR( error: $error, errorSum: $state.errorSum, dError: $dError )"
	
	/*Compute PID Output*/
	double output = kp * error + ki * state.errorSum + kd * dError
	int outputLevel = ( int ) Math.round( output )
	outlet.setLevel( outputLevel )
	log.debug "PID: OUTPUT( output: $output, ouputLevel: $outputLevel )"
	
	/*Remember some variables for next time*/
	state.lastError = error
	state.lastTime = currentTime
	log.debug "PID: TEMP( currentTemp: $tempSensor.currentTemperature )"
}

// tempSensor.currentTemperature
// outlet.setLevel(outputLevel)