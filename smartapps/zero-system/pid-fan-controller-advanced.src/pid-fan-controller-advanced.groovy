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
									paragraph "Choose a temperature sensor(s). If multiple temp sensors are chosen, the average will be taken"
									input( title: "Temperature Sensor(s)" , name: "tempSensors" , type: "capability.temperatureMeasurement" , multiple: true , required: true )

									paragraph "Set the desired target temperature"
									input( title: "Target Temp" , name: "targetTemp" , type: "decimal" , required: true , description: "70 (deg)" , defaultValue: 70 )

									paragraph "If the temperature reaches the set minimum, an alert is triggered (if enabled)"
									input( title: "Minimum Temperature" , name: "minTemp" , type: "decimal" , required: true , description: "60 (deg)" , defaultValue: 60 )

									paragraph "If the temperature reaches the set maximum, an alert is triggered (if enabled)"
									input( title: "Maximum Temperature" , name: "maxTemp" , type: "decimal" , required: true , description: "90 (deg)" , defaultValue: 90 )
								}
					}

			page( name: "settingsPID" , title: "PID Control Settings" , nextPage: "settingsScheduling" )
					{
						section()
								{
									paragraph "Select the fan controller (dimmer)"
									input( title: "Fan Controller(s)" , name: "fans" , type: "capability.switchLevel" , multiple: true , required: true )

									paragraph "To prevent the fan from turning on and off, a minimum fan speed can be set"
									input( title: "Minimum Fan Speed" , name: "minFanLevel" , type: "decimal" , required: true , description: "10%" , range: "0..100" , defaultValue: 10 )

									paragraph "If enabled it reverses the direction of control. For example, heating"
									input( title: "Reverse Control Direction" , name: "reverseDirection" , type: "bool" , required: true , defaultValue: false )
								}

						section( "Set PID variables" , hideable: true , hidden: true )
								{
									input( title: "P Variable" , name: "pVar" , type: "decimal" , required: false , description: "20" , defaultValue: 20 )
									input( title: "I Variable" , name: "iVar" , type: "decimal" , required: false , description: "1" , defaultValue: 1 )
									input( title: "D Variable" , name: "dVar" , type: "decimal" , required: false , description: "10" , defaultValue: 10 )
								}
					}

			page( name: "settingsScheduling" , title: "Scheduling Settings" , nextPage: "settingsActive" )
					{
						section("Sampling Time")
								{
									paragraph "Is the time between each measurement. Lower time, a faster rate of adjustment"
									input( title: "Sampling Time" , name: "samplingTime" , type: "enum" , required: true , options: ["1-Minute" , "5-Minutes" , "10-Minutes" , "15-Minutes"] , defaultValue: "1-Minute" )
								}

						section( "Time Frame" , hideable: true , hidden: true )
								{
									paragraph "Enable PID Control during time frame. Set time frame below. For example, 09:00 - 17:00"

									input( title: "Enable Time Frame" , name: "enableTimeFrame" , type: "bool" , required: false , defaultValue: false )
									input( title: "Start Time" , name: "startTime" , type: "time" , required: false , discription: "09:00" , defaultValue: "09:00" )
									input( title: "Stop Time" , name: "stopTime" , type: "time" , required: false , discription: "17:00" , defaultValue: "17:00")
								}

						section( "Luminace Control" , hideable: true , hidden: true )
								{
									paragraph "Enable PID Control during light level trigger. For example, if light level detected is above 100 lux."

									input( title: "Enable Luminacne Control" , name: "enableLuxControl" , type: "bool" , required: false , defaultValue: false )
									input( title: "Luminace Level" , name: "luxLevel" , type: "number" , required: false , description: "100 lux" , defaultValue: 100 )
									input( title: "Luminace Sensor" , name: "luxSensor" , type: "capability.illuminanceMeasurement" , multiple: false , required: false )
								}
					}

			page( name: "settingsActive" , title: "Active Temperature Control Settings" , nextPage: "settingsSafeguard" )
					{
						section( "Enable active temperature control when outside \"PID Time Frame\"" )
								{
									paragraph "If enabled, the app will switch on an active temperature controlling device. For example, an Air Conditioner or Heater. When the PID control is OFF"
									input( title: "Enable Active Temperature Control" , name: "enableActive" , type: "bool" , required: true , defaultValue: false )
								}

						section( "Active Cooling Control Settings" , hideable: true , hidden: true)
								{
									paragraph "Select the cooling device(s) (switch), that will cool the room to the Target Temperature"
									input( title: "Cooling Device(s)" , name: "activeCoolingDevices" , type: "capability.switch" , multiple: true , required: false )

									paragraph "Some A/C have a built in thermostat and just need to be powered on and not turned ON/OFF. Will be turned off if temperature reaches Minimum Temperature"
									input( title: "Enable ON/OFF Control" , name: "enableCoolingControl" , type: "bool" , required: false , defaultValue: false )
								}
					}

			page( name: "settingsSafeguard" , title: "Safeguard Settings", nextPage: "finalPage")
					{
						section()
								{
									paragraph "Enable alerts for max/min temperature alarms?"
									input( title: "Enable Alerts" , name: "sendPush" , type: "bool" , required: true , defaultValue: false )
								}

						section( "Overheat Protection" )
								{
									paragraph "If enabled, if the Max Temperature is reached the app will turn off the device until temperatures return to Target Temperature or the Time Allotment has passed"
									input( title: "Enable Overheat Protection" , name: "overheatProtectionEnabled" , type: "bool" , required: true , defaultValue: false )

									paragraph "Select the device(s) to turn OFF if overheat protection is triggered"
									input( title: "Overheat Device(s)" , name: "overheatDevices" , type: "capability.switch" , multiple: true , required: false )

									paragraph "If the temperature has not returned to the Target Temperature after set amount of time has passed, the devices are turned back ON"
									input( title: "Shutoff Duration" , name: "overheatOFFDuration" , type: "enum" , required: false , options: ["15-Minutes" , "30-Minutes" , "1-Hour" , "2-Hours"] , defaultValue: "15-Minutes" )
								}
					}
			page(name: "finalPage", title: "Name App and Pick Configure Modes", install: true, uninstall: true)
					{
						section()
								{
									label title: "Assign a name", required: false
									mode title: "Set for specific mode(s)", required: false
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

boolean OFF() {return false}

boolean ON() {return true}

def initialize()
{
	/*working variables*/
	state.numTempSensors = settings.tempSensors.size() // int

	state.iValue = 0.0 // double

	state.lastTemp = getTemp() // double
	state.lastTime = getTime() // double

	state.lastAlertTime = getTime() // long


	state.lastFanLevel = 0 // int
	state.maxFanLevel = 99.0 // double

	state.lastCoolingState = false // boolean. IF coolingDevices ARE or ARE NOT - ON
	state.lastHeatingState = false // boolean. IF heatingDevices ARE or ARE NOT - ON

	state.maxTempFlag = false // boolean
	state.minTempFlag = false // boolean

	state.lastOverheatState = true // boolean. IF overheatDevices ARE or ARE NOT - ON (ASSUMED ARE ON)
	state.fanState = true           // boolean. IF fans CAN or CANNOT - RUN (FreezingState)

	state.overheatDuration = 900000 // int
	state.overheatLastTime = getTime() // long

	setOverheatShutoffDuration()

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

	log.debug "> scheduledHandler: TIME( timeFrameEnabled: $settings.enableTimeFrame )"
	if ( settings.enableTimeFrame )
	{
		Date currentTime = new Date()
		boolean withinTimeFrame = timeOfDayIsBetween( settings.startTime , settings.stopTime , currentTime , location.timeZone )

		log.debug "> scheduledHandler: TIME( withinTimeFrame: $withinTimeFrame )"
		if ( withinTimeFrame )
		{
			if ( settings.enableActive ) activeTempControlOFF()
			pidControlON()
		}
		else
		{
			pidControlOFF()
			if ( settings.enableActive ) activeTempControlON()
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

void pidControlOFF()
{
	log.info "-> pidControlOFF"

	log.info "--> pid(OFF)"
	pid( false )
}

void pidControlON()
{
	log.info "-> pidControlON"
	if ( withinTempBounds() )
	{
		log.info "--> pid(ON)"
		pid( true )
	}
	else if ( state.maxTempFlag )
	{
		log.warn "--> pidOverheatProtection"
		pidOverheatProtection()

		log.info "--> setFanLevel($state.maxFanLevel)"
		setFanLevel( state.maxFanLevel )
	}
	else if ( state.minTempFlag  )
	{
		// TODO: pidControl min temp case
	}
}

void activeTempControlOFF()
{
	log.info "-> activeTempControlOFF"

	log.info "--> activeCooling(OFF)"
	activeCooling( false )

	// TODO: activeHeating(OFF)
}

void activeTempControlON()
{
	log.info "-> activeTempControlON"

	log.info "--> activeCooling(ON)"
	activeCooling( true )

	// TODO: activeHeating(ON)

}

// =================================================================
// ============================== PID ==============================
// =================================================================

void pid( boolean on , boolean logging = false )
{
	if ( on )
	{
		double currentTemp = getTemp( true )

		/*How long since we last calculated*/
		long currentTime = getTime()
		long timeChange = ( currentTime - state.lastTime ) / 1000.0
		if ( logging ) log.debug "pid(ON): timeChange( $timeChange (sec) )"

		/*Compute all the working error variables*/
		double pValue = settings.targetTemp - currentTemp

		state.iValue = ( state.iValue + ( state.ki * pValue ) )

		// @formatter:off //Windup elimination. Clamps I value between min and 100
		if ( state.iValue < settings.minFanLevel )	 state.iValue = settings.minFanLevel
		else if ( state.iValue > state.maxFanLevel ) state.iValue = state.maxFanLevel
		// @formatter:on

		double dValue = currentTemp - state.lastTemp

		if ( logging ) log.debug "pid(ON): ERRORS( pValue: $pValue , iValue: $state.iValue , dValue: $dValue )"

		/*Compute PID Output*/
		double p = state.kp * pValue
		double i = state.iValue
		double d = state.kd * dValue
		double level = p + i - d
		if ( logging ) log.debug "pid(ON): COMPUTE( P: $p , I: $i , D: $d )"

		setFanLevel( level , true )

		/*Remember some variables for next time*/
		state.lastTemp = currentTemp
		state.lastTime = currentTime
	}
	else
	{
		log.debug "pid(OFF)"
		state.iValue = 0.0
		state.lastTemp = getTemp()
		state.lastTime = getTime()
		setFanLevel( settings.minFanLevel )
	}
}

// =========================================================================================
// ============================== ACTIVE HEATING AND COOLING  ==============================
// =========================================================================================

void activeCooling( boolean on , boolean logging = false )
{
	// turn ON active cooling
	if ( on )
	{
		// min temp shutoff
		if ( getTemp() <= settings.minTemp )
		{
			// TODO: Make this more robust. Needs to be OFF until temp reaches target
			log.warn "activeCooling(ON): minTemp - triggered"
			state.lastCoolingState = setSwitch( OFF() , settings.activeCoolingDevices , state.lastCoolingState )
		}
		// cooling control is enabled
		else if ( settings.enableCoolingControl )
		{
			// turn OFF cooling device when currentTemp reaches targetTemp
			if ( getTemp() <= settings.targetTemp )
			{
				if ( logging ) log.debug "activeCooling(ON) -> enableCoolingControl(TRUE): activeCooling(OFF)"
				state.lastCoolingState = setSwitch( OFF() , settings.activeCoolingDevices , state.lastCoolingState )
			}

			// turn ON cooling device when currentTemp is above targetTemp
			else if ( getTemp() > settings.targetTemp )
			{
				if ( logging ) log.debug "activeCooling(ON) -> enableCoolingControl(TRUE): activeCooling(ON)"
				state.lastCoolingState = setSwitch( ON() , settings.activeCoolingDevices , state.lastCoolingState )
			}
		}

		// when cooling control is disabled, just turn on and leave on cooling. Unless minTemp was reached.
		else
		{
			if ( logging ) log.debug "activeCooling(ON): activeCooling(ON)"
			state.lastCoolingState = setSwitch( ON() , settings.activeCoolingDevices , state.lastCoolingState )
		}
	}
	// turn OFF active cooling
	else
	{
		if ( logging ) log.debug "activeCooling(OFF): activeCooling(OFF)"
		state.lastCoolingState = setSwitch( OFF() , settings.activeCoolingDevices , state.lastCoolingState )
	}

}

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

	if ( logging ) log.debug "getTemp: temp( $temp )"

	return temp
}
// @formatter:on

long getTime( boolean logging = false )
{
	long currentTime = now()
	if ( logging ) log.debug "getTime: currentTime( $currentTime )"
	return currentTime
}

boolean getFanState()
{
	if ( !state.fanState ) log.debug( "getFanState: OFF" )
	return state.fanState
}

boolean afterTime( long lastTime , int duration , boolean logging = false )
{
	boolean after = false
	long currentTime = getTime()
	long lastTimeDurationAdded = lastTime + duration

	if ( currentTime > lastTimeDurationAdded ) after = true

	if ( logging ) log.debug "afterTime: lastTime($lastTime) , currentTime($currentTime) , after($after)"

	return after
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

int setOverheatShutoffDuration()
{
	switch ( overheatOFFDuration ) // Weird case values are smartthings enum weirdness
	{
		case "15-Minutes":
			state.overheatDuration = 900000
			break

		case "1":
			state.overheatDuration = 1800000
			break

		case "2":
			state.overheatDuration = 3600000
			break

		case "3":
			state.overheatDuration = 7200000
			break

		default:
			log.error "setOverheatShutoffDuration: switch($overheatOFFDuration) - Unmached Case."
			state.overheatDuration = 900000
			break
	}
}

// @formatter:off
int setFanLevel( double rawLevel , boolean logging = false)
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
	if ( logging ) log.debug "setFanLevel:  rawLevel($rawLevel) , boundedLevel($boundedLevel) , currentLevel($currentLevel)"

	return boundedLevel
}
// @formatter:on

boolean setSwitch( boolean on , def devices , boolean lastState , boolean logging = false )
{
	if ( logging ) log.debug "setSwitch: lastState( $lastState )"

	boolean newState = lastState

	// turn OFF if lastState was ON
	boolean turnOFF_lastStateON = !on && lastState
	// turn ON if lastState was OFF
	boolean turnON_lastStateOFF = on && !lastState
	// prevent repeated commands being sent or if state changed

	for ( device in devices )
	{
		// state was manually changed, reset state
		boolean currentState = device.currentSwitch == "on"
		// turn OFF if currentState does not equal lastState set
		boolean turnOFF_currentStateON_diff_lastStateOFF = !on && ( currentState != lastState )
		// turn ON if currentState does not equal lastState set
		boolean turnON_currentStateOFF_diff_lastStateON = on && ( currentState != lastState )

		if ( logging )
		{
			log.debug "setSwitch: on($on)"
			log.debug "setSwitch: \"$device\" currentState($currentState)"
			log.debug "setSwitch: OFF_LOGIC($turnOFF_lastStateON , $turnOFF_currentStateON_diff_lastStateOFF)"
			log.debug "setSwitch: ON_LOGIC($turnON_lastStateOFF , $turnON_currentStateOFF_diff_lastStateON)"
		}

		if ( turnOFF_lastStateON || turnOFF_currentStateON_diff_lastStateOFF ) // turn OFF
		{
			device.off()
			newState = false
		}
		else if ( turnON_lastStateOFF || turnON_currentStateOFF_diff_lastStateON ) // turn ON
		{
			device.on()
			newState = true
		}
	}

	if ( logging ) log.debug "setSwitch: newState( $newState )"

	return newState
}

boolean disableFan() {return state.fanState = true}

boolean enableFan() {return state.fanState = false}

// ========================================================================
// ============================== SAFEGUARDS ==============================
// ========================================================================

void triggerAlert( String alertMessage , String thrownFrom )
{
	// if min/max alerts are enabled, will trigger an every alert hour
	if ( afterTime( state.lastAlertTime , 3600000 ) && settings.sendPush )
	{
		sendPush( alertMessage )
		state.lastAlertTime = getTime()
	}
	log.warn( thrownFrom + ": " + alertMessage )

}

// Once temp falls out-of-bounds secondary method must correct the FLAG before withinBounds will return TRUE
boolean withinTempBounds()
{
	boolean inBounds = true
	double currentTemp = getTemp()

	if ( state.minTempFlag || state.maxTempFlag )
		inBounds = false

	else if ( currentTemp <= settings.minTemp )
	{
		triggerAlert( "Minimum Temperature ($settings.minTemp) Alarm Triggered. Current Temperature: $temp" as String , "getTemp" )
		state.minTempFlag = true
		inBounds = false
	}

	else if ( currentTemp >= settings.maxTemp )
	{
		triggerAlert( "Maximum Temperature ($settings.maxTemp) Alarm Triggered. Current Temperature: $temp" as String , "getTemp" )
		state.maxTempFlag = true
		inBounds = false
	}

	return inBounds
}

// TODO: if outside hotter than inside and active cooling is enabled turn on active cooling and turn off PID control
// if overheating protection occurs, it is because the PID fans could not cool enough. Turn off heat source
void pidOverheatProtection( )
{
	boolean returnToTargetTemp = getTemp() < settings.targetTemp
	boolean afterTimeDuration = afterTime( state.overheatLastTime , state.overheatDuration )

	long timeRemaing =  ( getTime() - state.overheatLastTime )

	// WARNING: ASSUMES THAT "overheatDevices" ARE ON
	// first time protection has run. IF flag is set (TRUE) and devices are ON. Turn OFF devices and set timer
	if ( state.maxTempFlag && state.lastOverheatState )
	{
		// turn off heat source (lights)
		state.lastOverheatState = setSwitch( OFF() , settings.overheatDevices , state.lastOverheatState )

		// set timer
		state.overheatLastTime = getTime()

		log.warn "pidOverheatingProtection -> enableProtection(TRUE) - > maxTempFlag($state.maxTempFlag): lastOverheatState($state.lastOverheatState) , overheatLastTime($state.overheatLastTime)"
	}

	// if temperatures return to normal or the time-limit to be off is reached. turn devices back ON.
	else if ( returnToTargetTemp || afterTimeDuration )
	{
		// turn on heat source (lights)
		state.lastOverheatState = setSwitch( ON() , settings.overheatDevices , state.lastOverheatState )

		// reset flag
		state.maxTempFlag = false
		log.warn "pidOverheatingProtection -> enableProtection(FALSE) - > maxTempFlag(FALSE): overheatDevices(ON)"
	}
	else
		log.warn "pidOverheatingProtection: returnToTargetTemp($returnToTargetTemp) , afterTimeDuration($afterTimeDuration)"
}