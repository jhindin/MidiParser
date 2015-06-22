package com.jhindin.midi;

import javax.sound.midi.MidiMessage;
import javax.sound.midi.Receiver;;

public class LogReceiver implements Receiver {
	long startTime = -1;
	
	@Override
	public void send(MidiMessage message, long timeStamp) {
		if (startTime == -1)
			startTime = System.currentTimeMillis();
		
		long currentTime = System.currentTimeMillis();
		
		System.out.println(Printout.messageToString(message) + " at " + timeStamp +
				"/" + (currentTime - startTime));
	}

	@Override
	public void close() {
	}

}
