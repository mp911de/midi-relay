Midi relay controller
=====================

This tiny project allows you, to control a [ETHRLY16](http://www.robot-electronics.co.uk/htm/eth_rly16tech.htm) relay via Midi commands. 
Just start the class `de.paluch.midi.relay.Server` and adjust the config `config.xml`

Then you can call following URL's to control the whole machine:

* http://localhost:9595/player/play to play
* http://localhost:9595/player/stop to stop
* http://localhost:9595/player/ to get the current state (running/stopped)
* http://localhost:9595/player/device?id&state set MIDI receiver state
* http://localhost:9595/player/schedule?cronExpression to add a schedule

This code is experimental.