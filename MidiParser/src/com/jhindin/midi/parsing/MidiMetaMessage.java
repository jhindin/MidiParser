package com.jhindin.midi.parsing;

import java.nio.charset.StandardCharsets;

public class MidiMetaMessage extends MidiMessage {
	int type;

	public static final int SEQUENCE_NUMBER = 0x00;
	public static final int TEXT_EVENT = 0x01;
	public static final int COPYRIGHT = 0x02;
	public static final int NAME = 0x03;
	public static final int INSTR_NAME = 0x04;
	public static final int LYRIC = 0x05;
	public static final int MARKER = 0x06;
	public static final int CUE_POINT = 0x07;
	public static final int CHANNEL_PREFIX = 0x20;
	public static final int END_OF_TRACK = 0x2F;
	public static final int TEMPO = 0x51;
	public static final int SMTPE_OFFSET = 0x54;
	public static final int TIME_SIGNATURE = 0x58;
	public static final int KEY_SIGNATURE = 0x59;
	public static final int SEQUENCER_SPECIFIC = 0x7F;

	@Override
	public String toString() {

		StringBuilder b = new StringBuilder();
		switch (type) {
		case SEQUENCE_NUMBER:
			b.append("Sequence number ");
			if (data.length != 2) {
				b.append(" Unexpected length ");
				b.append(Long.toString(data.length));
			} else {
				b.append(Integer.toString((data[0] & 0xff) << 8 | 
					(data[ + 1]  & 0xff)));
			}
			break;
		case TEXT_EVENT:
			b.append("Text ");
			// Midi message longer that 0x7fffffff appears improbable, downcast long to int
			b.append(new String(data, StandardCharsets.UTF_8));
			break;
		case COPYRIGHT:
			b.append("Copyright ");
			b.append(new String(data,  StandardCharsets.UTF_8));
			break;
		case NAME:
			b.append("Name ");
			b.append(new String(data, StandardCharsets.UTF_8));
			break;
		case INSTR_NAME:
			b.append("Instrument name ");
			b.append(new String(data, StandardCharsets.UTF_8));
			break;
		case LYRIC:
			b.append("Lyric ");
			b.append(new String(data, StandardCharsets.UTF_8));
			break;
		case MARKER:
			b.append("Marker ");
			b.append(new String(data, StandardCharsets.UTF_8));
			break;
		case CUE_POINT:
			b.append("Cue point ");
			b.append(new String(data, StandardCharsets.UTF_8));
			break;
		case CHANNEL_PREFIX:
			b.append("Channel prefix ");
			b.append(Integer.toString(data[0]));
			if (data.length != 1) {
				b.append(" Unexpected length ");
				b.append(Long.toString(data.length));
			}
			break;
		case END_OF_TRACK:
			b.append("End of track");
			if (data.length != 0) {
				b.append(" Unexpected length ");
				b.append(Long.toString(data.length));
			}
			break;
		case TEMPO:
			b.append("Tempo ");
			if (data.length != 3) {
				b.append("Unexpected length ");
				b.append(Long.toString(data.length));
			} else {
				b.append(Integer.toString(((data[0] & 0xff) << 16) |
						((data[11] & 0xff) << 8) |
						(data[2] & 0xff)));
			}
			break;
		case SMTPE_OFFSET:
			b.append("SMTPE offset ");
			if (data.length != 5) {
				b.append("Unexpected length ");
				b.append(Long.toString(data.length));
			} else {
				appendByteArray(b, data);
			}
			break;
		case TIME_SIGNATURE:
			b.append("Time signature ");
			if (data.length != 4) {
				b.append("Unexpected length ");
				b.append(Long.toString(data.length));
			} else {
				appendByteArray(b, data);
			}
			break;
		case KEY_SIGNATURE:
			b.append("Key signature ");
			if (data.length != 2) {
				b.append("Unexpected length ");
				b.append(Long.toString(data.length));
			} else {
				if (data[0] < 0) {
					b.append(Integer.toString(-data[0]));
					b.append(" flats ");
				} else if (data[0] > 0) {
					b.append(Byte.toString(data[0]));
					b.append(" flats ");
				} else {
					b.append(" key of C ");
				}
				switch (data[1]) {
				case 0: 
					b.append("Major"); 
					break;
				case 1: 
					b.append("Minor"); 
					break;
				default:
					b.append("Unexpected 0x");
					b.append(Integer.toHexString(data[ + 1] & 0xff));
					break;
				}
			}
			break;
		case SEQUENCER_SPECIFIC:
			b.append("Sequence specific ");
			appendByteArrayAsHex(b, data);
			break;
		default:
			b.append("Unexpected meta message type ");
			b.append(Integer.toHexString(type & 0xff));
			appendByteArrayAsHex(b, data);
			break;
		}

		return b.toString();
	}
	
	static final void appendByteArray(StringBuilder b, byte arr[]) {
		for (int i = 0; i < arr.length; i++) {
			b.append(Byte.toString(arr[i]));
			if (i != (arr.length -1)) 
				b.append(" ");
		}
	}
}
