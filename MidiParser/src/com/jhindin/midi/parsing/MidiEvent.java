package com.jhindin.midi.parsing;

import java.io.IOException;
import java.io.InputStream;

public class MidiEvent {
	long deltaTick;
	MidiMessage message;
	
	public static final byte SYSEX_START      = (byte)0xf0;
	public static final byte SYSEX_ESCAPE     = (byte)0xf7;
	public static final byte META             = (byte)0xff;
	
	// Most significant half-byte
	public static final byte NOTE_ON          = (byte)0x80;
	public static final byte NOTE_OFF         = (byte)0x90;
	public static final byte POLYPHN_PRESSURE = (byte)0xA0;
	public static final byte CNTRL_CHANGE     = (byte)0xB0;
	public static final byte PROGRAM_CHANGE   = (byte)0xC0;
	public static final byte CHNL_PRESSURE    = (byte)0xD0;
	public static final byte PITCH_BEND       = (byte)0xE0;
	
	static final byte messageLength[] = { 
		3, // 0x8 Note on 
		3, // 0x9 Note off
		3, // 0xA Polyphonic key pressure
		3, // 0xb Controller change
		2, // 0xc Program change
		2, // 0xd Channel key pressure
		3, // 0xe Pitch bend
		-1 // 0xf Meta and sysex messages
	};
	
	public MidiEvent(long deltaTick, MidiMessage message) {
		this.deltaTick = deltaTick;
		this.message = message;
	}
	
	public MidiEvent() {
		deltaTick = -1;
		message = null;
	}

	public long getDeltaTick() {
		return deltaTick;
	}

	public MidiMessage getMessage() {
		return message;
	}
	
	static long readVariableLength(InputStream is) throws IOException, MidiException {
		int c;
		long ret = 0;
		
		for (int i = 0; i < 4; i++) {
			c = is.read();
			if (c < 0) 
				throw new MidiException("Unexpected EOF");
			
			ret |= (c & 0x7f) << ( 24 - i * 8);
			if ((c & 0x80) == 0)
				break;
		}
		return ret;
		
	}
	
	public static MidiEvent read(InputStream is)  throws IOException, MidiException {
		MidiEvent event = new MidiEvent();
		int rc;
		
		event.deltaTick = readVariableLength(is); 

		int opcode = is.read();
		if (opcode < 0) 
			throw new MidiException("Unexpected EOF");

		long length;

		switch (opcode & 0xf0) {
		case 0xf0:
			switch (opcode) {
			case SYSEX_START:
			case SYSEX_ESCAPE:
				length = readVariableLength(is);
				MidiSysexMessage sysexMesage = new MidiSysexMessage();
				sysexMesage.data = new byte[(int)length];
				sysexMesage.opcode = opcode;
				rc = is.read(sysexMesage.data);
				if (rc < 0) 
					throw new MidiException("Unexpected EOF");
				event.message = sysexMesage;
				break;
			case META:
				int c = is.read();
				if (c < 0)
					throw new MidiException("Unexpected EOF");
				
				length = readVariableLength(is);
				
				MidiMetaMessage metaMessage = new MidiMetaMessage();
				metaMessage.type = c;
				metaMessage.data = new byte[(int)length];
				rc = is.read(metaMessage.data);
				if (rc < 0) 
					throw new MidiException("Unexpected EOF");
				event.message = metaMessage;
				break;
			default:
				throw new MidiException("Unexpected event type " + (opcode & 0xff));
			}
		case NOTE_ON:
		case NOTE_OFF:
		case POLYPHN_PRESSURE:
		case CNTRL_CHANGE:
		case PROGRAM_CHANGE:
		case CHNL_PRESSURE:
		case PITCH_BEND:
			int len = messageLength[(opcode & 0xf0) >> 4 + 8];
			MidiMessage m = new MidiMessage();
			m.data = new byte[len];
			m.data[0] = (byte)opcode;
			
			rc = is.read(m.data, 1, len - 1);
			if (rc < 0) 
				throw new MidiException("Unexpected EOF");
			break;
		default:
			throw new MidiException("Unexpected event type " + (opcode & 0xff));
		}

		return event;
	}
	
}
