package com.jhindin.midi.parsing;

import java.io.IOException;
import java.io.InputStream;

public class MidiEvent {
	long tick;
	byte message[];
	
	public MidiEvent(long tick, byte[] message) {
		this.tick = tick;
		this.message = message;
	}
	
	public MidiEvent() {
		tick = -1;
		message = null;
	}

	public long getTick() {
		return tick;
	}

	public byte[] getMessage() {
		return message;
	}
	
	
	
	public static MidiEvent read(InputStream is)  throws IOException, MidiException {
		return null;
	}
	
}
