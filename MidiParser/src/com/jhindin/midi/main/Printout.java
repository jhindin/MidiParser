package com.jhindin.midi.main;

import java.io.ByteArrayInputStream;

import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;

public class Printout {

	static String messageToString(MidiMessage msg)
	{
		try {
			byte msgBytes[] = msg.getMessage();
			com.jhindin.midi.MidiMessage jm = 
				com.jhindin.midi.MidiMessage.read(
						new ByteArrayInputStream(msgBytes, 1, msgBytes.length -1),
						msgBytes[0], null);
						
			return jm.toString();
		} catch (Exception ex) {
			return "Parsing failed : " + ex.getMessage();
		}
	}

	static String eventToString(MidiEvent ev) {
		try {
			byte msgBytes[] = ev.getMessage().getMessage();
			com.jhindin.midi.MidiMessage jm = 
				com.jhindin.midi.MidiMessage.read(
						new ByteArrayInputStream(msgBytes, 1, msgBytes.length -1),
						msgBytes[0], null);
						
			com.jhindin.midi.MidiEvent jev = new com.jhindin.midi.MidiEvent(ev.getTick(), jm);
			return jev.toString();
		} catch (Exception ex) {
			return "Parsing failed : " + ex.getMessage();
		}
	}
	

}
