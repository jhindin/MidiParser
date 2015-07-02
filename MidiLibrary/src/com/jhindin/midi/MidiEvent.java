package com.jhindin.midi;

import java.io.IOException;
import java.io.InputStream;

public class MidiEvent {
	long deltaTick;
	MidiMessage message;

	public MidiEvent(long deltaTick, MidiMessage message) {
		this.deltaTick = deltaTick;
		this.message = message;
	}
	
	public MidiEvent() {
		deltaTick = -1;
		message = null;
	}

	public long getDeltaTick() {
		return deltaTick;
	}

	public MidiMessage getMessage() {
		return message;
	}
	
	public static MidiEvent read(InputStream is, byte runningStatus)
			throws IOException, MidiException {

		long deltaTick = Utils.readVariableLength(is, null);
		if (deltaTick < 0)
			return null;
			
		MidiEvent event = new MidiEvent();
		event.deltaTick = deltaTick; 
		
		is.mark(1);

		int status = is.read();
		if (status < 0) 
			throw new MidiException("Unexpected EOF");
		
		
		event.message = MidiMessage.read(is, (byte)status, runningStatus);
		return event;
	}

	@Override
	public String toString() {
		return "At " + Long.toString(deltaTick) + ":" + message;
	}
}
