package com.jhindin.midi;

public class MidiSysexMessage extends MidiVariableLengthMessage {
	int status;
	
	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		b.append("Sysex: status  ");
		b.append(Integer.toHexString(status & 0xff));
		b.append(" rest of message: ");
		appendByteArrayAsHex(b, data, dataOffset, data.length - dataOffset);
		return b.toString();
	}
}
