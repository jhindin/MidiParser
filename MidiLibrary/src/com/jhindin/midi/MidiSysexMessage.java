package com.jhindin.midi;

public class MidiSysexMessage extends MidiVariableLengthMessage {
	int status;
	
	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		b.append("Sysex: status  " + Integer.toHexString(status & 0xff));
		appendByteArrayAsHex(b, data, dataOffset, data.length - dataOffset);
		return b.toString();
	}
}
