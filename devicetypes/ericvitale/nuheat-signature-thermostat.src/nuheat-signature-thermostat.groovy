/**
 *  NuHeat Signature Thermostat
 *
 *  Copyright 2016 ericvitale@gmail.com
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
 *  You can find the latest version of this device handler @ https://github.com/ericvitale/ST-NuHeat-Signature-Thermostat
 *  You can find my other device handlers & SmartApps @ https://github.com/ericvitale
 *
 *  Note: You will need an account which can only be setup on a thermostat.
 *
 **/
 
 import groovy.time.TimeCategory
 
 public static String version() { return "v0.0.001.20170413" }
    /*
     * 04/11/2017 >>> v0.0.001.20170413 - Added hold / resume functionality.
     * 04/11/2017 >>> v0.0.001.20170411 - Initial build.
     */

metadata {
	definition (name: "NuHeat Signature Thermostat", namespace: "ericvitale", author: "ericvitale@gmail.com") {
		capability "Actuator"
		capability "Temperature Measurement"
        capability "Thermostat Heating Setpoint"
		capability "Thermostat"
		capability "Configuration"
		capability "Refresh"
		capability "Sensor"
        
        command "setTemp"
        command "currentMode"
        command "power"
        command "resume"
        command "hold"
	}

	preferences() {
    	input "tStatName", "text", title: "Name", required: true
        input "tStatSerialNumber", "text", title: "Thermostat Serial Number", required: true
        input "defaultHoldTime", "number", title: "Default Hold Time (hours)", required: true, defaultValue: 1
        input "powerUsage", "number", title: "Power Usage (watts)", required: true, defaultValue: 1200
        input "autoRefresh", "bool", title: "Auto Refresh (5 mins)", required: true, defaultValue: false
        input "theUser", "text", title: "Username", description: "Your Nuheat email", required: true
		input "thePassword", "text", title: "Password", description: "Your Nuheat password", required: true
    	input "logging", "enum", title: "Log Level", required: false, defaultValue: "INFO", options: ["TRACE", "DEBUG", "INFO", "WARN", "ERROR"]
        
    }
    
	tiles(scale: 2) {
		valueTile("temperature", "device.temperature", width: 6, height: 4, canChangeIcon: true) {
			state("temperature", label:'${currentValue}°', unit:"F",
				backgroundColors:[
					[value: 60, color: "#153591"],
					[value: 65, color: "#1e9cbb"],
					[value: 70, color: "#90d2a7"],
					[value: 75, color: "#44b621"],
					[value: 80, color: "#f1d801"],
					[value: 90, color: "#d04e00"],
					[value: 95, color: "#bc2323"]
				]
			)
		}
        
		standardTile("mode", "device.thermostatMode", inactiveLabel: false, decoration: "flat") {
			state "off", label:'${name}', action:"thermostat.setThermostatMode"
			state "heat", label:'${name}', action:"thermostat.setThermostatMode"
		}
		
		controlTile("heatSliderControl", "device.heatingSetpoint", "slider", height: 2, width: 4, inactiveLabel: false, range:"(62..100)") {
			state "setHeatingSetpoint", action:"thermostat.setHeatingSetpoint", backgroundColor:"#e86d13"
		}
		
        valueTile("heatingSetpoint", "device.heatingSetpoint", height: 2, width: 2, inactiveLabel: false, decoration: "flat") {
			state "heat", label:'${currentValue}°', unit:"F", backgroundColor:"#ffffff"
		}
        
        valueTile("currentMode", "device.currentMode", height: 1, width: 2, inactiveLabel: false, decoration: "flat") {
			state "default", label:'${currentValue}', backgroundColor:"#ffffff"
		}
        
        valueTile("power", "device.power", height: 1, width: 2, inactiveLabel: false, decoration: "flat") {
			state "default", label:'${currentValue} W', backgroundColor:"#ffffff"
		}
		
		standardTile("refresh", "device.temperature", height: 2, width: 2, inactiveLabel: false, decoration: "flat") {
			state "default", action:"refresh.refresh", icon:"st.secondary.refresh"
		}
        
        standardTile("setTemp", "device.setTemp", inactiveLabel: false, decoration: "flat") {
			state "default", action:"setTemp", label:"Set"//, icon:"st.secondary.refresh"
		}
        
        standardTile("resume", "device.resume", height: 2, width: 2, inactiveLabel: false, decoration: "flat") {
			state "resume", label:'Resume', action:"resume", icon:"st.Office.office7"
		}
        
        standardTile("hold", "device.hold", height: 2, width: 2, inactiveLabel: false, decoration: "flat") {
			state "hold", label:'Hold', action:"hold", icon:"st.Office.office13"
		}
		
        main (["temperature", "power"])
        details(["temperature", "heatSliderControl", "heatingSetpoint", "hold", "resume", "currentMode", "power", "refresh"])        
	}
}

def logPrefix() {
	return "NuHeatSig"
}

private determineLogLevel(data) {
    switch (data?.toUpperCase()) {
        case "TRACE":
            return 0
            break
        case "DEBUG":
            return 1
            break
        case "INFO":
            return 2
            break
        case "WARN":
            return 3
            break
        case "ERROR":
        	return 4
            break
        default:
            return 1
    }
}

def log(data, type) {
    data = "${logPrefix()} -- ${device.label} -- ${data ?: ''}"
    //data = "NuHeatSig -- ${device.label} -- ${data ?: ''}"
        
    if (determineLogLevel(type) >= determineLogLevel(settings?.logging ?: "INFO")) {
        switch (type?.toUpperCase()) {
            case "TRACE":
                log.trace "${data}"
                break
            case "DEBUG":
                log.debug "${data}"
                break
            case "INFO":
                log.info "${data}"
                break
            case "WARN":
                log.warn "${data}"
                break
            case "ERROR":
                log.error "${data}"
                break
            default:
                log.error "NuHeatSig -- ${device.label} -- Invalid Log Setting"
        }
    }
}

def installed() {
	log("Begin installed().", "INFO")
	initialize()
    log("End installed().", "INFO")
}

def updated() {
	log("Begin updated().", "INFO")
	initialize()
    log("End updated().", "INFO")
}

def initialize() {

	log("DH Version = ${version()}.", "INFO")
	log("Thermostat Name = ${tStatName}.", "INFO")
    log("Thermostat Serial Number = ${tStatSerialNumber}.", "INFO")
    log("Username = ${theUser}.", "INFO")
    log("Uncomment the line of code following this statement if  you really want to print your password.", "INFO")
    //log("Password = ${thePassword} minutes.", "INFO")
    log("Logging Level = ${logging}.", "INFO")
    log("Power Usage KWH = ${powerUsage}.", "INFO")
    log("Default Hold Time = ${defaultHoldTime}.", "INFO")
    log("Auto Refresh = ${autoRefresh}.", "INFO")
    
    setSerialNumber(tStatSerialNumber)
    setUsername(theUser)
    setPassword(thePassword)
    setPowerUsage(powerUsage)
    setAutoRefresh(autoRefresh)
    
    unschedule()
    
    if(getAutoRefresh()) {
    	runEvery5Minutes(getStatus)
    }
    
    runIn(5, sendConfig)
}

def sendConfig() {
	
	log("No configuration to send.", "DEBUG")
}

def parse(String description) {
	log("Parse() description = ${description}.", "DEBUG")
}

def refresh() {
	log("Device is being refreshed.", "INFO")
    getStatus()
}

def poll() {
	log("Device is being polled.", "INFO")
	refresh()
}

def getTemperature(value) {
	if (value != null) {
		def celsius = Integer.parseInt(value, 16) / 100
		if (getTemperatureScale() == "C") {
			return celsius
		} else {
			return Math.round(celsiusToFahrenheit(celsius))
		}
	}
}

/* Sets the heating setpoint to the specificed temperature for the default time stored in setting */
def setHeatingSetpoint(degrees) {
    setHeatingSetpointAndHold(degrees, defaultHoldTime)
}

/* Sets the heating setpoint to the specificed temperature for the default time stored in setting */
def setHeatingSetpointAndHold(degrees) {
	setHeatingSetpointAndHold(degrees, defaultHoldTime)
}

def setHeatingSetpointAndHold(degrees, duration) {
	log("Setting HeatingSetpoint to ${degrees} for ${duration} hours.", "INFO")
    
    /*def isHeating = "false"
    
    if(degrees > getTemp()) {
    	isHeating = "true"
    }*/
   
    /* Calculate Hold Time */
    def date = new Date()
    
    use( TimeCategory ) {
        date = date + getDuration().hours
    }
   
    date = date.format("yyyy-MM-dd'T'HH:mm:ssZ")
     
 	def values = ["SetPointTemp": temperatureToSetpoint(degrees), "ScheduleMode": "2", "HoldSetPointDateTime": date]
    
    setThermostat(values)
    
    def temperatureScale = getTemperatureScale()
    
    sendEvent("name": "heatingSetpoint", "value": degrees, "unit": temperatureScale)
 
    setSelectedTemperature(degrees)
    setDuration(duration)
    scheduleGetStatus()
}

def setCoolingSetpoint(degrees) {
	log("The method setCoolingSetpoint(...) is not supported by this device.", "ERROR")
}

def hold() {
	log("Holding the current set temperature of ${getSelectedTemperature()} indefinitely.", "INFO")
	def values = ["SetPointTemp": temperatureToSetpoint(getSelectedTemperature()), "ScheduleMode": "3"]
    
    setThermostat(values)
    scheduleGetStatus()
}

def setThermostatMode() {
	log("The method setThermostatMode() is not supported by this device.", "ERROR")
}

def setThermostatMode(String value) {
	log("The method setThermostatMode(...) is not supported by this device.", "ERROR")
}

def off() {
	log("The method off() is not supported by this device.", "ERROR")
}

def cool() {
	log("The method cool() is not supported by this device.", "ERROR")
}

def heat() {
	log("The method heat() is not supported by this device.", "ERROR")
}

def on() {
	log("The method on() is not supported by this device.", "ERROR")
}

//--------------------------------------------------------

def setSelectedTemperature(value) {
	state.selectedTemperature = value
}

def getSelectedTemperature() {
	if(state.selectedTemperature == null) {
    	return 65
    } else {
    	return state.selectedTemperature
    }
}

def setDuration(value) {
	state.duration = value
}

def getDuration() {
	if(state.duration == null) {
    	return 1
    } else {
    	return state.duration
    }
}

def setSessionID(value) {
	state.theSessionID = value
}

def getSessionID() {
	if(state.theSessionID == null) {
    	return -1
    } else {
    	return state.theSessionID
    }
}

def setSerialNumber(value) {
	state.serialNumber = value
}

def getSerialNumber() {
	if(state.serialNumber == null) {
    	return -1
    } else {
    	return state.serialNumber
    }
}

def setUsername(value) {
	state.username = value
}

def getUsername() {
	return state.username
}

def setPassword(value) {
	state.password = value
}

def getPassword() {
	return state.password
}

def setMode(value) {
	state.tStatMode = value
}

def getMode() {
	if(state.tStatMode == null) {
    	state.tStateMode = "Off"
    }
    
    return state.tStatMode
}

def setTemp(value) {
	state.theTemp = value
}

def getTemp() {
	if(state.theTemp == null) {
    	state.theTemp = 65
    }
    
    return state.theTemp
}

def temperatureToSetpoint(value) {
    /****
    Formula f(x) = ((x-33)*56)+33
    ****/
    
    log("temperatureToSetpoint(${value}) evoked.", "DEBUG")
    
    def sp = ((value - 33) * 56) + 33
    
    log("Setpoint for temperature ${value} is ${sp}.", "DEBUG")
    
    return sp
}

def setpointToTemperature(value) {
    /*****
   	Formula f(x) = ((x - 33) / 56) + 33
    *****/
    
    def theTemp = ((value - 33) / 56) + 33
	
    log("Temperature for Setpoint ${value} is ${theTemp}.", "DEBUG")
    
    theTemp = Math.round(theTemp)
    
    log("Rounded temperature for Setpoint ${value} is ${theTemp}.", "DEBUG")
	
    return theTemp
}

def getDeviceStatus() {
	log("Requesting device update from NuHeat.", "INFO")
    log("Method getDeviceStatus() is not yet implemented but should be.", "ERROR")
}

def setDeviceTemperature(value) {
	log("Setting device temperature to ${value}.", "INFO")
    log("Method setDeviceTemperature() is not yet implemented but should be.", "ERROR")
}

def setPowerUsage(value) {
	state.thePowerUsage = value
}

def getPowerUsage() {
	if(state.thePowerUsage == null) {
    	state.thePowerUsage = 1200
    }
    
    return state.thePowerUsage
}

def setAutoRefresh(value) {
	state.isAutoRefreshEnabled = value
}

def getAutoRefresh() {
	if(state.isAutoRefreshEnabled == null) {
    	state.isAutoRefreshEnabled = false
    }
    
    return state.isAutoRefreshEnabled
}

//--------------------------------------------------------

def scheduleGetStatus() {
	log("Scheduling a status update in 30 seconds...", "INFO")
    runIn(30, getStatus)
}

def getStatus() {
	log("Updating the status of the ${getSerialNumber()} thermostat.", "INFO")
    
    if(!isUserAuthenticated()) {
    	authenticateUser()
        
        if(!isUserAuthenticated()) {
        	log("Failed to authenticate user.", "ERROR")
            return
        }
    }
    
    def params = [
		uri: "https://www.mynuheat.com/api/thermostat?sessionid=${getSessionID()}&serialnumber=${getSerialNumber()}",
        body: []
	] 
    
    try {
    
        httpGet(params) {resp ->

            log("Response: ${resp}.", "TRACE")

            resp.headers.each {
               log("header ${it.name} : ${it.value}", "TRACE")
            }

            log("response contentType: ${resp.contentType}", "TRACE")
            log("response data: ${resp.data}", "TRACE")
            log("WPerSquareUnit = ${resp.data['WPerSquareUnit']}", "DEBUG")
            log("FloorArea = ${resp.data['FloorArea']}", "DEBUG")
            log("Temperature = ${resp.data['Temperature']}", "DEBUG")
            log("Heating = ${resp.data['Heating']}", "DEBUG")
            log("Setpoint = ${resp.data['SetPointTemp']}", "DEBUG")
            
            def theTemp = setpointToTemperature(resp.data['Temperature'])
            def power = 0
            def setPoint = setpointToTemperature(resp.data['SetPointTemp'])

            setTemp(theTemp)
            setSelectedTemperature(setPoint)

            if(resp.data['Heating']) {
                setMode("Heat")
                power = getPowerUsage()
            } else {
                setMode("Off")
                power = 0
            }

            log("Converted Temperature: ${theTemp}.", "DEBUG")
            log("Calculated power usage: ${power} watts.", "DEBUG")
            
            def temperatureScale = getTemperatureScale()

            sendEvent("name":"temperature", "value": theTemp)
            sendEvent("name":"currentMode", "value": getMode())
            sendEvent("name":"power", "value": power)
            sendEvent("name": "heatingSetpoint", "value": setPoint, "unit": temperatureScale)
            sendEvent("name":"thermostatMode", "value": getMode())

            }
            
		} catch (groovyx.net.http.HttpResponseException e) {

            log("User is not authenticated, authenticating.", "ERROR")

            if(e.getMessage() == "Unauthorized") {
                authenticateUser()
        }
             
	}
}

def setThermostat(value_map) {
	log("Attempting to set values ${value_map}.", "INFO")
    
    def params = [
		uri: "https://www.mynuheat.com/api/thermostat?sessionid=${getSessionID()}&serialnumber=${getSerialNumber()}",
        body: value_map
	] 
    
	try {
        httpPost(params) {resp ->

            log("Response: ${resp}.", "DEBUG")

            resp.headers.each {
               log("header ${it.name} : ${it.value}", "DEBUG")
            }

            log("response contentType: ${resp.contentType}", "TRACE")
            log("response data: ${resp.data}", "TRACE")


        }
    } catch (groovyx.net.http.HttpResponseException e) {

        log("User is not authenticated, authenticating.", "ERROR")
        
        if(e.getMessage() == "Unauthorized") {
        	authenticateUser()
        }
    }
    
}

def resume() {
	log("Resuming the schedule.", "INFO")
    
    def values = ["ScheduleMode": "1"]
    setThermostat(values)
    scheduleGetStatus()
}

//--------------------------------------------------------

def authenticateUser() {

	log("Attempting to authenticate user ${getUsername()}.", "INFO")
    
    setSessionID("")
    
	def result = {}
    
    def params = [
		uri: 'https://www.mynuheat.com/api/authenticate/user',
		body: ["Email": "${getUsername()}", "password": "${getPassword()}", "application": "0"]
	] 
    
    httpPost(params) {resp ->
    
    	log("Response: ${resp}.", "DEBUG")
        
        resp.headers.each {
           log("header ${it.name} : ${it.value}", "DEBUG")
        }
        
        log("response contentType: ${resp.contentType}", "DEBUG")
        log("response data: ${resp.data}", "DEBUG")
        log("SessionID: ${resp.data["SessionId"]}", "DEBUG")
        
        setSessionID(resp.data["SessionId"])
	}
    
    if(getSessionID() != "") {
	    userAuthenticated(true)
        log("User has been authenticated.", "INFO")
    } else {
    	userAuthenticated(false)
        log("User failed to authenticate.", "ERROR")
    }
}

def isUserAuthenticated() {
	if(state.authenticatedUser == null) {
    	return false
    } else {
		return state.authenticatedUser
    }
}

def userAuthenticated(value) {
	state.authenticatedUser = value
}