package com.jhindin.midi;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

class Track implements Iterable<MidiEvent> {
	int index;
	TrackStreamChunk chunk;
	ArrayList<MidiEvent> events = new ArrayList<MidiEvent>();
	
	Track(int index, TrackStreamChunk chunk) throws IOException, MidiException {
		this.index = index;
		this.chunk = chunk;
		byte runningStatus = 0;
		
		MidiEvent event;
		do {
			event = MidiEvent.read(chunk.is, runningStatus);
			if (event != null) {
				runningStatus = event.message.data[0];
				events.add(event);
			} 
		} while (event != null);
	}

	@Override
	public Iterator<MidiEvent> iterator() {
		// TODO Auto-generated method stub
		return events.iterator();
	}
}