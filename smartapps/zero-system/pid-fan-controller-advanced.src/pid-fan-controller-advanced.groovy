/**
 *  PID Fan Controller Advanced
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
		name: "PID Fan Controller Advanced" ,
		namespace: "zero-system" ,
		author: "Zero_System" ,
		description: "PID Fan Control. \r\nThis app varies a fan(s) speed, by using a dimmer. \r\nThe app is able to maintain a constant room temperature even if a heat source present, e.g. a computer.\r\nAdditional Functions: \r\n  1. Forced Heating and Cooling Control\r\n  2. Temperature Alarm" ,
		category: "My Apps" ,
		iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png" ,
		iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png" ,
		iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png" )


// @formatter:off
preferences
{
	page( name: "settingsTemp" , title: "Temperature Settings" , nextPage: "settingsPID" )
	{
		section()
		{
	        paragraph "Choose a temperature sensor. If multiple temp sensors are chosen, the average will be taken"
	        input( title: "Temperature Sensor(s)" , name: "tempSensors" , type: "capability.temperatureMeasurement" , multiple: true , required: true )
			
	        paragraph "Set the desired target temperature"
	        input( title: "Target Temp" , name: "targetTemp" , type: "decimal" , required: true , description: "70 (deg)" , defaultValue: 70 )
		
		    paragraph "Set the minimum temperature. If the temperature reaches the set minimum, the fan will be turned off and an alert is triggered (if enabled)"
		    input( title: "Minimum Temperature" , name: "minTemp" , type: "decimal" , required: true , description: "60 (deg)" , defaultValue: 60 )
			
			paragraph "Set the maximum temperature. If the temperature reaches the set maximum, an alert is triggered (if enabled)"
		    input( title: "Maximum Temperature" , name: "maxTemp" , type: "decimal" , required: true , description: "90 (deg)" , defaultValue: 90 )
			
			paragraph "Enable alerts for max/min temperature alarms?"
	        input( title: "Enable Alerts" , name: "sendPush" , type: "bool" , required: true , defaultValue: false )
		}
	}
	
	page( name: "settingsPID" , title: "PID Control Settings" , nextPage: "settingsScheduling" )
	{
	    section()
	    {
	        paragraph "Select the fan controller (dimmer)"
	        input( title: "Fan Controller(s)" , name: "fans" , type: "capability.switchLevel" , multiple: true , required: true )
	
	        paragraph "Set the minimum fan speed. This prevents the fan from turning on and off at low speeds"
	        input( title: "Minimum Fan Speed" , name: "minFanLevel" , type: "decimal" , required: true , description: "10%" , range: "0..100" , defaultValue: 10 )
	        
	        paragraph "Reverse control direction. Enable to reverse the direction of control. For example, heating"
	        input( title: "Reverse Control Direction" , name: "reverseDirection" , type: "bool" , required: true , defaultValue: false )
        }
	
	    section( "Set PID variables" , hideable: true , hidden: true )
	    {
	        input( title: "P Variable" , name: "pVar" , type: "decimal" , required: false , description: "20" , defaultValue: 20 )
	        input( title: "I Variable" , name: "iVar" , type: "decimal" , required: false , description: "1" , defaultValue: 1 )
	        input( title: "D Variable" , name: "dVar" , type: "decimal" , required: false , description: "10" , defaultValue: 10 )
	    }
	}
	
	page( name: "settingsScheduling" , title: "Scheduling Settings" , nextPage: "settingsForced" )
	{
		section()
		{
	        paragraph "Sampling Time. It is the time between each measurement. Lower time means a faster rate of adjustment"
	        input( title: "Sampling Time" , name: "samplingTime" , type: "enum" , required: true , options: ["1-Minute" , "5-Minutes" , "10-Minutes" , "15-Minutes"] , defaultValue: "1-Minute" )
		
	        paragraph "Enable PID Control during time frame. Set time frame below. For example, 09:00 - 17:00"
	        input( title: "Enable Time Frame" , name: "enableTimeFrame" , type: "bool" , required: true , defaultValue: false )
		}
	
	    section( "Time Frame" , hideable: true , hidden: true )
	    {
	        input( title: "Start Time" , name: "startTime" , type: "time" , required: false , discription: "09:00" , defaultValue: "09:00" )
	        input( title: "Stop Time" , name: "stopTime" , type: "time" , required: false , discription: "17:00" , defaultValue: "17:00")
	    }
	}
	
	page( name: "settingsForced" , title: "Forced Temperature Control Settings" , install: true )
	{
		section( "Enable forced temperature control when outside \"PID Time Frame\"" )
		{
			paragraph "When enabled, the controller will turn on a switch that controls a forced temperature device. For example, a smart switch connected to an Air Conditioner."
			input( title: "Enable Forced Temperature Control" , name: "enableForced" , type: "bool" , required: true , defaultValue: false )
		}
		
		section( "Forced Cooling Temperature Control Settings" , hideable: true , hidden: true)
		{
			paragraph "Select the cooling device(s) (switch), that will cool room the room to Target Temperature"
			input( title: "Cooling Device(s)" , name: "forcedCoolingDevices" , type: "capability.switch" , multiple: true , required: false )
		
			paragraph "Enable ON/OFF control? Some A/C have a built in thermostat and just need to be powered on and not turned ON/OFF. Will be turned off if temperature reaches Minimum Temperature"
			input( title: "Enable ON/OFF Control" , name: "enableCoolingControl" , type: "bool" , required: false , defaultValue: false )
		}
	}
}
// @formatter:on

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
	state.numTempSensors = settings.tempSensors.size() // int
	
	state.iValue = 0.0 // double
	
	state.lastTemp = getTemp() // double
	state.lastTime = getTime() // double
	state.lastAlertTime = getTime() // long
	
	state.fanState = true // boolean
	state.lastFanLevel = 0 // int
	state.maxFanLevel = 99.0 // double
	
	state.coolingState = false // boolean
	
	setPID()
	
	runPID()
}

// ========================================================================
// ============================== SCHEDULERS ==============================
// ========================================================================

void runPID()
{
	switch ( samplingTime ) // Weird case values are smartthings enum weirdness
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
	
	log.debug "scheduledHandler: TIME( timeFrameEnabled: $settings.enableTimeFrame )"
	if ( settings.enableTimeFrame )
	{
		Date currentTime = new Date()
		boolean withinTimeFrame = timeOfDayIsBetween( settings.startTime , settings.stopTime , currentTime , location.timeZone )
		
		log.debug "scheduledHandler: TIME( withinTimeFrame: $withinTimeFrame )"
		if ( withinTimeFrame )
		{
			forcedTempControlOFF( true )
			pidControlON()
		}
		else
		{
			pidControlOFF( true )
			forcedTempControlON()
		}
	}
	else
	{
		pidControlON()
	}
}

// =========================================================================
// ============================== CONTROLLERS ==============================
// =========================================================================

void pidControlOFF( boolean logging = false )
{
	if ( logging ) log.debug "pidControlOFF"
	state.iValue = 0.0
	state.lastTemp = getTemp()
	state.lastTime = getTime()
	setFan( 0.0 )
}

void pidControlON()
{
	double currentTemp = getTemp( true )
	
	/*How long since we last calculated*/
	long currentTime = getTime()
	long timeChange = ( currentTime - state.lastTime ) / 1000.0
	log.debug "PID: TIME( timeChange: $timeChange (sec) )"
	
	/*Compute all the working error variables*/
	double pValue = settings.targetTemp - currentTemp
	
	state.iValue = ( state.iValue + ( state.ki * pValue ) )
	
	// @formatter:off //Windup elimination. Clamps I value between min and 100
	if ( state.iValue < settings.minFanLevel )	 state.iValue = settings.minFanLevel
	else if ( state.iValue > state.maxFanLevel ) state.iValue = state.maxFanLevel
	// @formatter:on
	
	double dValue = currentTemp - state.lastTemp
	
	log.debug "PID: ERROR( pValue: $pValue , iValue: $state.iValue , dValue: $dValue )"
	
	/*Compute PID Output*/
	double p = state.kp * pValue
	double i = state.iValue
	double d = state.kd * dValue
	double level = p + i - d
	log.debug "PID: COMPUTE( P: $p , I: $i , D: $d )"
	
	setFan( level , true )
	
	/*Remember some variables for next time*/
	state.lastTemp = currentTemp
	state.lastTime = currentTime
}

void forcedTempControlOFF( boolean logging = false )
{
	if ( logging ) log.debug "forcedTempControlOFF"
	setCooling( false , true)
}

void forcedTempControlON()
{
	log.debug "forcedTempControlON: settings.enableForced( $settings.enableForced )"
	
	if ( settings.enableForced )
	{
		forcedCoolingControl( true )
	}
	
}

// @formatter:off
void forcedCoolingControl( boolean logging = false )
{
	// min temp shutoff
	if ( getTemp() <= settings.minTemp )
	{
		log.warn "forcedCoolingControl: minTemp - triggered"
		setCooling( false , true )
	}
		
	// cooling control is enabled
	else if ( settings.enableCoolingControl )
	{
		if( logging ) log.debug "forcedCoolingControl: settings.enableCoolingControl( $settings.enableCoolingControl )"
		
		// turn OFF cooling device when currentTemp reaches targetTemp
		if ( getTemp() <= settings.targetTemp )
			setCooling( false )
			
		// turn ON cooling device when currentTemp is above targetTemp
		else if ( getTemp() > settings.targetTemp )
			setCooling( true )
	}
	
	// when cooling control is disabled, just turn on and leave on cooling. Unless minTemp was reached.
	else
	{
		if( logging ) log.debug "forcedCoolingControl: settings.enableCoolingControl( $settings.enableCoolingControl )"
		setCooling( true , true )
	}
}
// @formatter:on

// =====================================================================
// ============================== GETTERS ==============================
// =====================================================================

// @formatter:off
double getTemp( boolean logging = false )
{
	double temp
	
	if ( state.numTempSensors == 1 ) temp = settings.tempSensors.get( 0 ).currentValue( "temperature" )
	else
	{
		double sum = 0.0
		
		for ( sensor in settings.tempSensors )
			sum += sensor.currentValue( "temperature" )

		temp = sum / state.numTempSensors
	}
	
    // if min/max alerts are enabled, will trigger an every alert hour
    if ( settings.sendPush )
    {
		// alerts user if temp is below min temp and 
        if ( temp < settings.minTemp )
			triggerAlert( "Minimum Temperature ($settings.minTemp) Alarm Triggered. Current Temperature: $temp" as String , "getTemp" )

        // alerts user if temp is below max temp and user has alerts enabled. Will trigger every alert hour
        if ( temp > settings.maxTemp )
			triggerAlert( "Maximum Temperature (settings.maxTemp) Alarm Triggered. Current Temperature: $temp" as String , "getTemp" )
    }
	
	if ( logging ) log.debug "TEMP: ( temp: $temp )"
	
	return temp
}
// @formatter:on

long getTime( boolean logging = false )
{
	long currentTime = now()
	if ( logging ) log.debug( "getTime: $currentTime" )
	return currentTime
}

boolean getFanState()
{
	if ( !state.fanState ) log.debug( "FAN_STATE: OFF" )
	return state.fanState
}

boolean afterAlertTime( boolean logging = false )
{
	boolean alert = false
	long currentTime = getTime()
	
	if ( state.lastAlertTime < currentTime ) alert = true
	
	if ( logging ) log.info( "afterAlertTime: lastAlertTime($state.lastAlertTime) , currentTime($currentTime) , alert($alert)" )
	
	return alert
}

// =====================================================================
// ============================== SETTERS ==============================
// =====================================================================

void setPID()
{
	if ( settings.reverseDirection )
	{
		state.kp = settings.pVar
		state.ki = settings.iVar
		state.kd = settings.dVar
	}
	else
	{
		state.kp = 0 - settings.pVar
		state.ki = 0 - settings.iVar
		state.kd = 0 - settings.dVar
	}
}

// @formatter:off
int setFan( double rawLevel , boolean logging = false)
{
	int boundedLevel = state.lastFanLevel
	
	if      ( getTemp() < settings.minTemp )    boundedLevel = 0    // Min temp cutoff
	else if ( !getFanState() ) 			        boundedLevel = 0    // Sentry value. If fan needs to be turned off
	else if ( rawLevel < settings.minFanLevel )	boundedLevel = settings.minFanLevel  	   // Min
	else if ( rawLevel > state.maxFanLevel ) 	boundedLevel = ( int ) state.maxFanLevel   		   // Max
	else 										boundedLevel = ( int ) Math.round( rawLevel ) // Calculated

    // Prevent constant commands being sent to controller if no change is detected. If the level has changed (manually) reset it
	for ( fan in settings.fans )
    	if ( boundedLevel != state.lastFanLevel || fan.currentValue( "level" ) != boundedLevel )  
        	fan.setLevel( boundedLevel )
	
    state.lastFanLevel = boundedLevel
    
	int currentLevel = settings.fans.get(0).currentValue( "level" )
	if ( logging ) log.debug "OUTPUT: ( rawLevel: $rawLevel , boundedLevel: $boundedLevel , currentLevel: $currentLevel )"
	
	return boundedLevel
}
// @formatter:on

// @formatter:off
boolean setCooling( boolean on , boolean logging = false )
{
	// prevent repeated commands being sent or if state changed
	for ( device in settings.forcedCoolingDevices )
	{
		def test = device.currentValue( "value" )
		log.debug "test: $test"
		if ( state.coolingState && !on )        // turn off cooling.
		{
			device.off()
			state.coolingState = false
		}

		else if ( !state.coolingState && on )   // turn on cooling
		{
			device.on()
			state.coolingState = true
		}
	}
	
	if ( logging ) log.debug "setCooling: COOL( coolingState: $state.coolingState )"
	
	return state.coolingState
}
// @formatter:on

boolean disableFan() {return state.fanState = true}

boolean enableFan() {return state.fanState = false}

void triggerAlert( String alertMessage , String thrownFrom )
{
	if ( afterAlertTime( ) ) sendPush( alertMessage )
	log.warn( thrownFrom + ": " + alertMessage )
	state.lastAlertTime = ( getTime() + ( 3600 * 1000 ) ) // next alert time set for hour in future
}