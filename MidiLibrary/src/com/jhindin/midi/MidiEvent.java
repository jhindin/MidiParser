package com.jhindin.midi;

import java.io.IOException;
import java.io.InputStream;

public class MidiEvent implements Comparable<MidiEvent> {
	long deltaTick;
	long tick;
	MidiMessage message;

	public MidiEvent(long deltaTick, MidiMessage message) {
		this.deltaTick = deltaTick;
		this.message = message;
	}
	
	public MidiEvent() {
		deltaTick = -1;
		tick = 0;
		message = null;
	}

	public long getDeltaTick() {
		return deltaTick;
	}

	public long getTick() {
		return tick;
	}

	public MidiMessage getMessage() {
		return message;
	}
	
	public static MidiEvent read(InputStream is, ParsingContext context)
			throws IOException, MidiException {

		long deltaTick = Utils.readVariableLength(is, null);
		if (deltaTick < 0)
			return null;
			
		MidiEvent event = new MidiEvent();
		event.deltaTick = deltaTick;
		context.ticks +=  event.deltaTick;
		event.tick = context.ticks;
		is.mark(1);

		int status = is.read();
		if (status < 0) 
			throw new MidiException("Unexpected EOF");
		
		
		event.message = MidiMessage.read(is, (byte)status, context);
		return event;
	}

	@Override
	public String toString() {
		return "At " + Long.toString(tick) + ":" + message;
	}
	
	public static class ParsingContext {
		byte runningStatus = 0;
		long ticks = 0;
	}

	@Override
	public int compareTo(MidiEvent o) {
		return Long.compare(tick, o.tick);
	}
}
