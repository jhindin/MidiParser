package com.jhindin.midi;

public abstract class MidiVariableLengthMessage extends MidiMessage {
	int dataOffset;
	
	public int getDataOffset() {
		return dataOffset;
	}
}
