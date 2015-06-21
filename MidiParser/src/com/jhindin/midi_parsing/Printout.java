package com.jhindin.midi_parsing;

import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.ShortMessage;

public class Printout {

	static String eventToString(MidiEvent event)
	{
		return messageToString(event.getMessage());
	}
	
	static String messageToString(MidiMessage message) {
		byte messageBytes[] = message.getMessage();
		StringBuilder b = new StringBuilder();
	
	
		switch (messageBytes[0] & 0xff) {
		case ShortMessage.SYSTEM_RESET:
			b.append("Meta event: ");
			switch (messageBytes[1]) {
			case 0:
				b.append(" sequence number");
				break;
			case 1:
				b.append(" text event");
				break;
			case 2:
				b.append(" copyright notice");
				break;
			case 3:
				b.append(" sequence/track name");
				break;
			case 4:
				b.append(" instrument name");
				break;
			case 5:
				b.append(" lyric");
				break;
			case 6:
				b.append(" marker");
				break;
			case 7:
				b.append(" cue point");
				break;
			case 0x20:
				b.append(" channel prefix");
				break;
			case 0x2f:
				b.append(" end of track");
				break;
			case 0x51:
				b.append(" set tempo ");
				b.append(Integer.toString(
						((messageBytes[3] & 0xff ) << 16) | 
						((messageBytes[4] & 0xff ) << 8) |   
						(messageBytes[5] & 0xff )  
						));
				break;
			case 0x54:
				b.append(" SMTPE offset");
				break;
			case 0x58:
				b.append(" time signature ");
				b.append("nn 0x");
				b.append(Integer.toString(messageBytes[3] & 0xff));
				b.append(" dd 0x");
				b.append(Integer.toString(messageBytes[4] & 0xff));
				b.append(" cc 0x");
				b.append(Integer.toString(messageBytes[5] & 0xff));
				b.append(" bb 0x");
				b.append(Integer.toString(messageBytes[6] & 0xff));
				
				break;
			case 0x59:
				b.append(" key signature");
				break;
			case 0x7f:
				b.append(" sequencer-specific meta event");
				break;
			default:
				b.append(" not yet recognized 0x" + Integer.toHexString(messageBytes[1] &0xff));
			}
			break;
		case ShortMessage.START:
			b.append("Start");
			break;
		case ShortMessage.STOP:
			b.append("Stop");
			break;
		default:
			switch (messageBytes[0] & 0xf0) {
			case ShortMessage.NOTE_ON:
				b.append("Note on ");
				b.append(Integer.toString(messageBytes[0] & 0x0f));
				break;
			case ShortMessage.NOTE_OFF:
				b.append("Note off ");
				b.append(Integer.toString(messageBytes[0] & 0x0f));
				break;
			case ShortMessage.PROGRAM_CHANGE:
				b.append("Program change ");
				b.append(Integer.toString(messageBytes[0] & 0x0f));
				break;
			case ShortMessage.CONTROL_CHANGE:
				b.append("Control change");
				break;
			default:
				b.append("Unknown 0x");
				b.append(Integer.toHexString(messageBytes[0]));
			}
		}
			
		return b.toString();
	}

}
