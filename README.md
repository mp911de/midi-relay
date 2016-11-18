Midi relay controller
=====================

This tiny project allows you, to control a [ETHRLY16](http://www.robot-electronics.co.uk/htm/eth_rly16tech.htm) relay via Midi commands. 
Just start the class `de.paluch.midi.relay.Application` with the local profile and adopt `application-local.properties`

Then you can call following URL's to control the whole machine:

* GET http://localhost:9595/player/ to retrieve the current status
* GET http://localhost:9595/player/play to play
* PUT http://localhost:9595/player/play to play uploaded midi data
* GET http://localhost:9595/player/stop to stop
* GET http://localhost:9595/player/ to get the current state (running/stopped)
* GET http://localhost:9595/player/port/{port:0-8}/{state:ON|OFF} to control port state
* GET http://localhost:9595/player/devices get a list of all devices
* GET http://localhost:9595/player/devices/{id}/to/device to connect an in-device to the out device (relay)

This code is experimental and uses Spring Boot.

Resources
---------
* [Controlling Christmas Lights using Midi](https://www.paluch.biz/blog/65-controlling-christmas-lights-using-midi.html)
* [Controlling a relay via Midi](https://www.paluch.biz/blog/64-controlling-a-relay-via-midi.html)

License
-------
* [Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0)