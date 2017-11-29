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
definition
(
    name : "Virtual Thermostat Variable Speed" ,
    namespace: "zero-system" ,
    author: "Zero System" ,
    description: "This is an extension of Virtual Thermostat. It is for cooling a room with a fan connected to a dimmer. " ,
    category: "" ,
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png" ,
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png" ,
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png"
)


preferences
    {
        section("Choose a temperature sensor(s).")
        {
            pargraph "If multiple sensors are used, the average temperature will be calculated."
            input( title: "Temperature Sensor(s)", name:"tempSensors", type: "capability.temperatureMeasurement", multiple: true, required: true )
        }

        section("Select the cooling fan dimmer outlet(s).")
        {
            pargraph "These outlet(s) will change their level from (0% - 100%) based on room temperature."
            input( title: "Dimmer Outlet(s)", name: "outlets", type: "capability.switchLevel", multiple: true, required: true)
        }

        section("Set the desired target temperature and temperature range.")
        {
            pargraph "(0%) Min Temp -> TARGET <- Max Temp(100%)"
            input(title: "Min Temp",    name: "minTemp",    type: "decimal", required: true, description: "65 (deg)")
            input(title: "Target Temp", name: "targetTemp", type: "decimal", required: true, description: "70 (deg)")
            input(title: "Max Temp",    name: "maxTemp",    type: "decimal", required: true, description: "75 (deg)")
        }

        section("Step time, in minutes.")
        {
            pargraph( "If temp is over target temp, how many min(s) between corrections. Default: 15 mins" )
            input( title: "Step Time (min)", name: "stepMinutes", type: "number", range: "1..*", defaultValue: 15, required: false, description: "15 (min)" )
        }

        section("Step size percentage.")
        {
            pargraph( " When the room temperature is over target temperature, how large of a correction to be taken. Default: 10%" )
            input ( title: "Step Size", name: "stepSize", type: "number", range: "1..100", defaultValue: 10, required: false , discription: "10%")
        }

        section( "Time frame." )
        {
            paragraph( "The time frame the fan(s) will run." )
            input( title: "Start Time", name: "startTime", type: "time", required: true, discription: "9:00 AM" )
            input( title: "Stop Time",  name: "stopTime",  type: "time", required: true, discription: "6:00 PM" )
        }

    }

def installed() {
    log.debug "Installed with settings: ${settings}"

    initialize()
}

def updated() {
    log.debug "Updated with settings: ${settings}"

    unsubscribe()
    initialize()
}

def initialize() {
    // TODO: subscribe to attributes, devices, locations, etc.
}

/**
def installed() {
    subscribe(sensor, "temperature", temperatureHandler)
    if (motion) {
        subscribe(motion, "motion", motionHandler)
    }
}

def updated() {
    unsubscribe()
    subscribe(sensor, "temperature", temperatureHandler)
    if (motion) {
        subscribe(motion, "motion", motionHandler)
    }
}

def temperatureHandler(evt) {
    def isActive = hasBeenRecentMotion()
    if (isActive || emergencySetpoint) {
        evaluate(evt.doubleValue, isActive ? setPoint : emergencySetpoint)
    } else {
        outlets.off()
    }
}

def motionHandler(evt) {
    if (evt.value == "active") {
        def lastTemp = sensor.currentTemperature
        if (lastTemp != null) {
            evaluate(lastTemp, setPoint)
        }
    } else if (evt.value == "inactive") {
        def isActive = hasBeenRecentMotion()
        log.debug "INACTIVE($isActive)"
        if (isActive || emergencySetpoint) {
            def lastTemp = sensor.currentTemperature
            if (lastTemp != null) {
                evaluate(lastTemp, isActive ? setPoint : emergencySetpoint)
            }
        } else {
            outlets.off()
        }
    }
}

private evaluate(currentTemp, desiredTemp) {
    log.debug "EVALUATE($currentTemp, $desiredTemp)"
    def threshold = 1.0
    if (mode == "cool") {
        // air conditioner
        if (currentTemp - desiredTemp >= threshold) {
            outlets.on()
        } else if (desiredTemp - currentTemp >= threshold) {
            outlets.off()
        }
    } else {
        // heater
        if (desiredTemp - currentTemp >= threshold) {
            outlets.on()
        } else if (currentTemp - desiredTemp >= threshold) {
            outlets.off()
        }
    }
}

private hasBeenRecentMotion() {
    def isActive = false
    if (motion && minutes) {
        def deltaMinutes = minutes as Long
        if (deltaMinutes) {
            def motionEvents = motion.eventsSince(new Date(now() - (60000 * deltaMinutes)))
            log.trace "Found ${motionEvents?.size() ?: 0} events in the last $deltaMinutes minutes"
            if (motionEvents.find { it.value == "active" }) {
                isActive = true
            }
        }
    } else {
        isActive = true
    }
    isActive
}

*/