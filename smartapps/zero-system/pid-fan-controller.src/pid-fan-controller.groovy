/**
 *  PID Fan Controller
 *
 *  Copyright 2018 Zero_System
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

definition(
		name: "PID Fan Controller" ,
		namespace: "zero-system" ,
		author: "Zero_System" ,
		description: "PID Fan Control. \nThis app varies a fan(s) speed, by using a dimmer. \nThe app is able to maintain a constant room temperature even if a heat source present, e.g. a computer." ,
		category: "My Apps" ,
		iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png" ,
		iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png" ,
		iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png" )


preferences
{
    section()
    {
        paragraph "Choose a temperature sensor. If multiple temp sensors are choosen, the average will be taken"
        input( title: "Temperature Sensor(s)" , name: "tempSensors" , type: "capability.temperatureMeasurement" , multiple: true , required: true )

        paragraph "Select the cooling fan controller (dimmer)"
        input( title: "Fan Controller(s)" , name: "fans" , type: "capability.switchLevel" , multiple: true , required: true )

        paragraph "Set the desired target temperature"
        input( title: "Target Temp" , name: "targetTemp" , type: "decimal" , required: true , description: "70 (deg)" , defaultValue: 70 )

        paragraph "Set the minimum fan speed. This prevents the fan from turning on and off at low speeds"
        input( title: "Minimum Fan Speed" , name: "minFanLevel" , type: "decimal" , required: true , description: "10%" , range: "0..100" , defaultValue: 10 )

        paragraph "Sampling Time. It is the time between each measurement. Lower time means a faster rate of adjustment"
        input( title: "Sampling Time" , name: "samplingTime" , type: "enum" , required: true , options: ["1-Minute" , "5-Minutes" , "10-Minutes" , "15-Minutes"] , defaultValue: "1-Minute" )
        
        paragraph "Reverse control direction. Enable to reverse the direction of control. For example, heating."
        input( title: "Reverse Control Direction" , name: "reverseDirection" , type: "bool" , required: true , defaultValue: false )
        
        paragraph "Enable PID Control during time frame. Set time frame below. For example, 09:00 - 17:00"
        input( title: "Enable Time Frame" , name: "enableTimeFrame" , type: "bool" , required: true , defaultValue: false )
    }

    section( "Time frame." , hideable: true , hidden: true )
    {
        input( title: "Start Time" , name: "startTime" , type: "time" , required: false , discription: "09:00" , defaultValue: "09:00" )
        input( title: "Stop Time" , name: "stopTime" , type: "time" , required: false , discription: "17:00" , defaultValue: "17:00")
    }

    section( "Set PID variables." , hideable: true , hidden: true )
    {
        input( title: "P Variable" , name: "pVar" , type: "decimal" , required: false , description: "20" , defaultValue: 20 )
        input( title: "I Variable" , name: "iVar" , type: "decimal" , required: false , description: "1" , defaultValue: 1 )
        input( title: "D Variable" , name: "dVar" , type: "decimal" , required: false , description: "10" , defaultValue: 10 )
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
	/*working variables*/
	state.numTempSensors = tempSensors.size()
	
	state.iValue = 0.0
	state.lastTemp = getTemp()
	state.lastTime = getTime()
    
	state.fanLevel = setFan( 0 )
    
    setPID()
	
	runPID()
}

void runPID()
{
	switch ( samplingTime )
	{
		case "1-Minute":
			runEvery1Minute( scheduledHandler )
			break
		
		case "1":
			runEvery5Minutes( scheduledHandler )
			break
		
		case "2":
			runEvery10Minutes( scheduledHandler )
			break
		
		case "3":
			runEvery15Minutes( scheduledHandler )
			break
		
		default:
			log.error "runPID: switch($samplingTime) - Unmached Case."
			runEvery1Minute( scheduledHandler )
			break
	}
}

void scheduledHandler()
{
	log.debug "=========================================="
	
    if ( timeFrameEnabled )
    {
    	Date currentTime = new Date()
		boolean withinTimeFrame = timeOfDayIsBetween( startTime , stopTime , currentTime , location.timeZone )
		//log.debug "scheduledHandler: TIME( start: $startTime, stop: $stopTime, time: $currentTime, value: $withinTimeFrame )"
    
        if ( withinTimeFrame ) calculatePID()

        else
        {
            log.debug "OUTSIDE TIME FRAME"
            state.iValue = 0.0
            state.lastTemp = getTemp()
            state.lastTime = now()
            setFan( 0 )
        }
    }
    else
    	calculatePID()
}

void calculatePID()
{
	double currentTemp = getTemp()
	
	/*How long since we last calculated*/
	long currentTime = getTime()
	long timeChange = ( currentTime - state.lastTime ) / 1000.0
	log.debug "PID: TIME( timeChange: $timeChange (sec) )"
	
	/*Compute all the working error variables*/
	double pValue = targetTemp - currentTemp
	
	state.iValue = ( state.iValue + ( state.ki * pValue ) )
	if ( state.iValue < minFanLevel )
	{
		state.iValue = minFanLevel
	}
	else if ( state.iValue > 100 )
	{
		state.iValue = 100
	}
	
	double dValue = currentTemp - state.lastTemp
	
	log.debug "PID: ERROR( pValue: $pValue , iValue: $state.iValue , dValue: $dValue )"
	
	/*Compute PID Output*/
	double p = state.kp * pValue
	double i = state.iValue
	double d = state.kd * dValue
	double level = p + i - d
	log.debug "PID: COMPUTE( P: $p , I: $i , D: $d )"
	
	setFan( level )
	
	/*Remember some variables for next time*/
	state.lastTemp = currentTemp
	state.lastTime = currentTime
}

int setFan( double rawLevel )
{
	int boundedLevel
	
	if ( rawLevel < 0 )
	{
		boundedLevel = 0
	}
	else if ( rawLevel >= 0 && rawLevel < minFanLevel )
	{
		boundedLevel = minFanLevel
	} // If calculated output is below the min threshold, set level to minLevelOutput.
	else if ( rawLevel > 100 )
	{
		boundedLevel = 100
	}
	else
	{
		boundedLevel = ( int ) Math.round( rawLevel )
	} // Else the output should be rounded to integer.

//	fans.setLevel( boundedLevel) // TODO: see if it sets all fan levels
	for ( fan in fans )
	{
		fan.setLevel( boundedLevel )
	}
	
	log.debug "OUTPUT: ( rawLevel: $rawLevel , boundedLevel: $boundedLevel )"
	state.fanLevel = boundedLevel
	return boundedLevel
}

double getTemp()
{
	double temp
	
	if ( state.numTempSensors == 1 )
	{
		temp = tempSensors.get( 0 ).currentValue( "temperature" )
	}
	
	else
	{
		double sum = 0.0
		
		for ( sensor in tempSensors )
		{
			sum += sensor.currentValue( "temperature" )
		}
		
		temp = sum / state.numTempSensors
	}
	
	log.debug "TEMP: ( temp: $temp )"
	return temp
}

long getTime()
{
	return now()
}

void setPID()
{
	if (settings.reverseDirection)
    {
    	state.kp = pVar
		state.ki = iVar
		state.kd = dVar
    }
    else
    {
        state.kp = 0 - pVar
        state.ki = 0 - iVar
        state.kd = 0 - dVar
    }
}
