package com.jhindin.midi;

public interface EventListener {
	public void receiveEvent(int track, MidiEvent event) throws Exception;
}
