package com.jhindin.midi;

import java.io.IOException;
import java.io.InputStream;

public class MidiEvent {
	long deltaTick;
	MidiMessage message;

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
	
	public static MidiEvent read(InputStream is)  throws IOException, MidiException {
		MidiEvent event = new MidiEvent();
		int rc;
		
		event.deltaTick = readVariableLength(is);

		long length;
		
		int status = is.read();
		if (status < 0) 
			throw new MidiException("Unexpected EOF");

		switch (status & 0xf0) {
		case 0xf0:
			switch (status) {
			case MidiMessage.SYSEX_START:
			case MidiMessage.SYSEX_ESCAPE:
				MidiSysexMessage sysexMesage = new MidiSysexMessage();
				length = readVariableLength(is);
				sysexMesage.data = new byte[(int)length];
				sysexMesage.status = status;
				rc = is.read(sysexMesage.data);
				if (rc < 0) 
					throw new MidiException("Unexpected EOF");
				event.message = sysexMesage;
				break;
			case MidiMessage.META:
				int type = is.read();
				if (type < 0)
					throw new MidiException("Unexpected EOF");
				MidiMetaMessage metaMessage = new MidiMetaMessage();
				
				length = readVariableLength(is);
				
				metaMessage.type = type;
				metaMessage.data = new byte[(int)length];
				rc = is.read(metaMessage.data);
				if (rc < 0) 
					throw new MidiException("Unexpected EOF");
				event.message = metaMessage;
				break;
			default:
				throw new MidiException("Unexpected event type " + (status & 0xff));
			}
		case MidiMessage.NOTE_ON:
		case MidiMessage.NOTE_OFF:
		case MidiMessage.POLYPHN_PRESSURE:
		case MidiMessage.CNTRL_CHANGE:
		case MidiMessage.PROGRAM_CHANGE:
		case MidiMessage.CHNL_PRESSURE:
		case MidiMessage.PITCH_BEND:
			int len = messageLength[(status & 0xf0) >> 4 + 8];
			MidiMessage m = new MidiMessage();
			m.data = new byte[len];
			m.data[0] = (byte)status;
			
			rc = is.read(m.data, 1, len - 1);
			if (rc < 0) 
				throw new MidiException("Unexpected EOF");
			break;
		default:
			throw new MidiException("Unexpected event type " + (status & 0xff));
		}

		return event;
	}

	static final long readVariableLength(InputStream is)
			throws IOException, MidiException {
		int c;
		int i;
		long length = 0;;
		
		for (i = 0; i < 4; i++) {
			c = is.read();
			if (c < 0) 
				throw new MidiException("Unexpected EOF");
			
			length |= (c & 0x7f) << ( 24 - i * 8);
			if ((c & 0x80) == 0)
				break;
		}
		
		return length;
	}

	@Override
	public String toString() {
		return "At " + Long.toString(deltaTick) + ":" + message;
	}
	
}
