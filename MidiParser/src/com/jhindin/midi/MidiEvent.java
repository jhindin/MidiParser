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
	
	public static MidiEvent read(InputStream is, byte runningStatus)
			throws IOException, MidiException {
		MidiEvent event = new MidiEvent();
		
		event.deltaTick = readVariableLength(is, null);
		
		is.mark(1);

		int status = is.read();
		if (status < 0) 
			throw new MidiException("Unexpected EOF");
		
		
		readMessage(is, event, (byte)status, runningStatus);
		return event;
	}
	
	static void readMessage(InputStream is, MidiEvent event, byte status, byte runningStatus)
			throws IOException, MidiException {
		Prefix prefix = new Prefix();
		long length;
		int rc;

		switch ((byte)(status & 0xf0)) {
		case (byte)0xf0:
			switch ((byte)(status & 0xff)) {
			case MidiMessage.SYSEX_START:
			case MidiMessage.SYSEX_ESCAPE:
				prefix.data[0] = (byte)(status & 0xff);
				prefix.pos = 1;
				
				MidiSysexMessage sysexMesage = new MidiSysexMessage();
				length = readVariableLength(is, prefix);
				sysexMesage.data = new byte[(int)length + prefix.pos];
				sysexMesage.status = status;
				rc = is.read(sysexMesage.data, prefix.pos, (int)length);
				if (rc < 0) 
					throw new MidiException("Unexpected EOF");
				System.arraycopy(prefix.data, 0, sysexMesage.data, 0, prefix.pos);
				sysexMesage.dataOffset = prefix.pos;

				event.message = sysexMesage;
				break;
			case MidiMessage.META:

				int type = is.read();
				if (type < 0)
					throw new MidiException("Unexpected EOF");

				prefix.data[0] = (byte)(status & 0xff);
				prefix.data[1] = (byte)(type & 0xff);
				prefix.pos = 2;
				
				MidiMetaMessage metaMessage = new MidiMetaMessage();
				
				length = readVariableLength(is, prefix);
				
				metaMessage.type = type;
				metaMessage.data = new byte[(int)length + prefix.pos];
				rc = is.read(metaMessage.data, prefix.pos, (int)length);
				if (rc < 0) 
					throw new MidiException("Unexpected EOF");

				System.arraycopy(prefix.data, 0, metaMessage.data, 0, prefix.pos);
				metaMessage.dataOffset = prefix.pos;

				event.message = metaMessage;
				break;
			default:
				throw new MidiException("Unexpected event type " + (status & 0xff));
			}
			break;
		case MidiMessage.NOTE_ON:
		case MidiMessage.NOTE_OFF:
		case MidiMessage.POLYPHN_PRESSURE:
		case MidiMessage.CNTRL_CHANGE:
		case MidiMessage.PROGRAM_CHANGE:
		case MidiMessage.CHNL_PRESSURE:
		case MidiMessage.PITCH_BEND:
			int len = messageLength[((status & 0xf0) >> 4) - 8];
			MidiMessage m = new ShortMessage();
			m.data = new byte[len];
			m.data[0] = (byte)status;
			
			rc = is.read(m.data, 1, len - 1);
			if (rc < 0) 
				throw new MidiException("Unexpected EOF");
			event.message = m;
			break;
		default:
			if (status == runningStatus) 
				throw new MidiException("Invalid internal state");
			is.reset();
			readMessage(is, event, runningStatus, runningStatus);
		}
	}

	static final long readVariableLength(InputStream is, Prefix prefix)
			throws IOException, MidiException {
		int c;
		int i;
		long length = 0;;
		
		for (i = 0; i < 4; i++) {
			c = is.read();
			if (c < 0) 
				throw new MidiException("Unexpected EOF");
			
			if (prefix != null)
				prefix.data[prefix.pos++] = (byte)(c & 0xff);
			
			length <<= 8;
			length |= (c & 0x7f);
			if ((c & 0x80) == 0)
				break;
		}
		
		return length;
	}

	@Override
	public String toString() {
		return "At " + Long.toString(deltaTick) + ":" + message;
	}
	
	static class Prefix {
		byte data[] = new byte[6];
		int pos = 0;
	}
	
}
