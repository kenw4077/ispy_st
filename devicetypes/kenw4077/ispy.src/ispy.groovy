/**
 *  ispy
 *
 *  Copyright 2016 Ken Williams
 *
 */
metadata {
	definition (name: "ispy", namespace: "kenw4077", author: "Ken Williams") {
		capability "Switch"
		attribute "hubactionMode", "string"
   
        
	}

    preferences {
    input("iSpyIP", "string", title:"iSpy Server Local IP Address", description: "Please enter your Gate Controller's IP Address", required: true, displayDuringSetup: true)
    input("iSpyPort", "string", title:"iSpy Server Port", description: "Please enter your Gate Controller's Port", defaultValue: 80 , required: true, displayDuringSetup: true)
 	}
    
	simulator {
    
	}
 tiles {
        multiAttributeTile(name:"switch", type: "lighting", width: 6, height: 4, canChangeIcon: true){
            tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
                attributeState "on", label:'${name}', action:"switch.off", icon:"st.camera.camera", backgroundColor:"#EE0000" , nextState:"off"
                attributeState "off", label:'${name}', action:"switch.on", icon:"st.camera.camera", backgroundColor:"#ffffff", nextState:"on"
		}}
        main "switch"
        details(["switch","on","off"])
    }
}



                    
                    
def parse(String description) {
    log.debug "Parsing '${description}'"
    def map = [:]
	def retResult = []
	def descMap = parseDescriptionAsMap(description)
    def msg = parseLanMessage(description)
    log.debug "status ${msg.status}"
    log.debug "data ${msg.data}"
    sendEvent(name: "Status", value: msg.status)
}

// handle commands


def cameraCmd(int command)
{

    def host = iSpyIP 
    def hosthex = convertIPtoHex(host)
    def porthex = convertPortToHex(iSpyPort)
    device.deviceNetworkId = "$hosthex:$porthex" 
    
    log.debug "The device id configured is: $device.deviceNetworkId"
    
    def headers = [:] 
    headers.put("HOST", "$host:$iSpyPort")
    
    log.debug "The Header is $headers"
    
    if(command == 1){
  def path = "/recordondetecton"
  log.debug "path is: $path"
  try {
    def hubAction = new physicalgraph.device.HubAction(
    	method: "GET",
    	path: path,
    	headers: headers
        )
        	
   
    log.debug hubAction
    return hubAction
    
    }
    catch (Exception e) {
    	log.debug "Hit Exception $e on $hubAction"
    }
    }
    else if(command == 0)
    {
    def path = "/recordingoff"
  log.debug "path is: $path"
  try {
    def hubAction = new physicalgraph.device.HubAction(
    	method: "GET",
    	path: path,
    	headers: headers
        )
        	
   
    log.debug hubAction
    return hubAction
    
    }
    catch (Exception e) {
    	log.debug "Hit Exception $e on $hubAction"
    }
    }
  }
  
def parseDescriptionAsMap(description) {
	description.split(",").inject([:]) { map, param ->
		def nameAndValue = param.split(":")
		map += [(nameAndValue[0].trim()):nameAndValue[1].trim()]
	}
}

private String convertIPtoHex(ipAddress) { 
    String hex = ipAddress.tokenize( '.' ).collect {  String.format( '%02x', it.toInteger() ) }.join()
    log.debug "IP address entered is $ipAddress and the converted hex code is $hex"
    return hex

}

private String convertPortToHex(port) {
	String hexport = port.toString().format( '%04x', port.toInteger() )
    log.debug hexport
    return hexport
}

private Integer convertHexToInt(hex) {
	Integer.parseInt(hex,16)
}


private String convertHexToIP(hex) {
	log.debug("Convert hex to ip: $hex") 
	[convertHexToInt(hex[0..1]),convertHexToInt(hex[2..3]),convertHexToInt(hex[4..5]),convertHexToInt(hex[6..7])].join(".")
}

private getHostAddress() {
    def parts = device.deviceNetworkId.split(":")
    log.debug device.deviceNetworkId
    def ip = convertHexToIP(parts[0])
    def port = convertHexToInt(parts[1])
    return ip + ":" + port
}


def on() {
    log.info "Recording On"
    sendEvent(name: "switch", value: "on", isStateChange: true)
    return cameraCmd(1)    
}

def off() {
    log.info "Recording Off"
    sendEvent(name: "switch", value: "off", isStateChange: true)
    return cameraCmd(0)    
}

