package com.jhindin.midi;

public class ShortMessage extends MidiMessage {
	@Override
	public String toString() {
		switch ((byte)(data[0] & 0xf0)) {
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
}
