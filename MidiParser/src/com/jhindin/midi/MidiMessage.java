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
	
	public final byte[] getBytes() {
		return data;
	}
	
	static final int getChannel(byte b) { return b & 0xf; }

	
	static final void appendByteArrayAsHex(StringBuilder b, byte data[], int offset, int length) {
		for (int i = 0; i < length; i++) {
			b.append(Integer.toHexString(data[i + offset] & 0xff));
			if (i != length - 1) {
				b.append(" ");
			}
		}
	}
}
