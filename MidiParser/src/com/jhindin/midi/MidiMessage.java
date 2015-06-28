package com.jhindin.midi;

public abstract class MidiMessage {
	byte data[];

	public static final byte SYSEX_START      = (byte)0xf0;
	public static final byte SYSEX_ESCAPE     = (byte)0xf7;
	public static final byte META             = (byte)0xff;
	
	// Most significant half-byte
	public static final byte NOTE_OFF         = (byte)0x80;
	public static final byte NOTE_ON          = (byte)0x90;
	public static final byte POLYPHN_PRESSURE = (byte)0xA0;
	public static final byte CNTRL_CHANGE     = (byte)0xB0;
	public static final byte PROGRAM_CHANGE   = (byte)0xC0;
	public static final byte CHNL_PRESSURE    = (byte)0xD0;
	public static final byte PITCH_BEND       = (byte)0xE0;
	
	
	@Override
	public String toString() {
		switch (data[0] & 0xf0) {
		case NOTE_ON:
			return "Note on: channel " + getChannel(data[0]) + " note " + Byte.toString(data[1]) +
					" velocity " + Byte.toString(data[2]);
		case NOTE_OFF:
			return "Note off: channel " + getChannel(data[0]) + " note " + Byte.toString(data[1]) +
					" velocity " + Byte.toString(data[2]);
		case POLYPHN_PRESSURE:
			return "Polyphonic pressure: channel " + getChannel(data[0]) + " note " + Byte.toString(data[1]) +
					" pressure " + Byte.toString(data[2]);
		case CNTRL_CHANGE:
			return "Control change: channel " + getChannel(data[0]) + " control " + Byte.toString(data[1]) +
					" value " + Byte.toString(data[2]);
		case PROGRAM_CHANGE:
			return "Program change: channel " + getChannel(data[0]) + " instrument " + Byte.toString(data[1]);
		case CHNL_PRESSURE:
			return "Channel pressure: channel " + getChannel(data[0]) + " pressure " + Byte.toString(data[1]);
		case PITCH_BEND:
			return "Pitch bend: channel " + getChannel(data[0]) + " value " +
				Integer.toString((data[1] << 8) | data[2]);
		default:
			return "Unknown status 0x" + Integer.toHexString(data[0]);
			
		}
	}

	public final byte[] getBytes() {
		return data;
	}
	
	static final int getChannel(byte b) { return b & 0xf; }

	
	static final void appendByteArrayAsHex(StringBuilder b, byte data[]) {
		for (int i = 0; i < data.length; i++) {
			b.append(Integer.toHexString(data[i] & 0xff));
			if (i != data.length - 1) {
				b.append(" ");
			}
		}
	}
}
