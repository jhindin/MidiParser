package com.jhindin.midi.parsing;

public class MidiSysexMessage extends MidiMessage {
	int status;
	
	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		b.append("Sysex: status  " + Integer.toHexString(status & 0xff));
		appendByteArrayAsHex(b, data);
		return b.toString();
	}
}
