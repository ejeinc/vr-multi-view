# VR Multi View

## Prepare

Prepare 2 (or more) Android devices.
One device is Gear VR compatible and another one is controller.

These devices have to be in the same local network.

## How to use

1. Create your [osig](https://developer.oculus.com/osig/) files.
2. Put your osig files into `gearvr/src/main/assets/` directory.
3. Open project with Android Studio.
4. Build.
5. Run gearvr module with Gear VR device and controller module with other Android device.

## Basic messaging flow

1. Gear VR : Broadcast own IP address to network by UDP
2. Controller : Receive broadcast and establish WebSocket connection to IP address
3. Gear VR : On receive WebSocket request, stop broadcasting
4. Controller : Send media information by WebSocket
5. Gear VR : Receive media information and process (load, seek, play and pause).
6. Controller : Shutdown. Close WebSocket connection
7. return to 1.

## Config

Configurations are in common/src/main/res/values/common_config.xml

```xml
<resources>
    <integer name="broadcastPort">50122</integer>
    <integer name="websocketPort">50123</integer>
    <string name="websocketProtocol">vrmultiview</string>
</resources>
```

### broadcast_port

UDP broadcast port. UDP Broadcast is used in device searching phase.
Gear VR sends its IP address, WebSocket port, and protocol at regular intervals by UDP broadcast.
When controller receives this packet, controller will create WebSocket connection to Gear VR.
Broadcast message is a JSON string which represents GearVRDeviceInfo object.
This can be changed independently from websocket_port.

### websocket_port

WebSocket port. WebSocket is used to control Gear VR from controller.
WebSocket message is a JSON string which represents ControlMessage object.
This can be changed independently from broadcast_port.

### websocket_protocol

WebSocket protocol can be changed to any string.
Change this to prevent connection from malicious programs.


## Icons

Icons are licensed by Creative Commons 3.0
https://creativecommons.org/licenses/by/3.0/us/

### Auther links

Controller app icon
https://thenounproject.com/esteves_emerick

Gear VR app icon
https://thenounproject.com/Aliriza/
