package com.jhindin.midi;

import java.io.IOException;
import java.io.InputStream;

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
	
	public static MidiMessage read(InputStream is,  byte status, MidiEvent.ParsingContext context)
			throws IOException, MidiException {
		Utils.Prefix prefix = new Utils.Prefix();
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
				length = Utils.readVariableLength(is, prefix);
				if (length < 0)
					throw new MidiException("Unexpected EOF");
				
				sysexMesage.data = new byte[(int)length + prefix.pos];
				sysexMesage.status = status;
				rc = is.read(sysexMesage.data, prefix.pos, (int)length);
				if (rc < 0) 
					throw new MidiException("Unexpected EOF");
				System.arraycopy(prefix.data, 0, sysexMesage.data, 0, prefix.pos);
				sysexMesage.dataOffset = prefix.pos;

				context.runningStatus = status;
				return sysexMesage;
			case MidiMessage.META:

				int type = is.read();
				if (type < 0)
					throw new MidiException("Unexpected EOF");

				prefix.data[0] = (byte)(status & 0xff);
				prefix.data[1] = (byte)(type & 0xff);
				prefix.pos = 2;
				
				MidiMetaMessage metaMessage = new MidiMetaMessage();
				
				length = Utils.readVariableLength(is, prefix);
				
				metaMessage.type = type;
				metaMessage.data = new byte[(int)length + prefix.pos];
				if (length > 0) {
					rc = is.read(metaMessage.data, prefix.pos, (int)length);
					if (rc < 0) 
						throw new MidiException("Unexpected EOF");
				}

				System.arraycopy(prefix.data, 0, metaMessage.data, 0, prefix.pos);
				metaMessage.dataOffset = prefix.pos;

				context.runningStatus = status;
				return metaMessage;
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
			int len = messageLength[((status & 0xf0) >> 4) - 8];
			MidiMessage m = new ShortMessage();
			m.data = new byte[len];
			m.data[0] = (byte)status;
			
			rc = is.read(m.data, 1, len - 1);
			if (rc < 0) 
				throw new MidiException("Unexpected EOF");
			context.runningStatus = status;
			return m;
		default:
			if (context != null) {
				if (status == context.runningStatus) 
					throw new MidiException("Invalid internal state");
				is.reset();
				return read(is, context.runningStatus, context);
			} else {
				throw new MidiException("Unexpected status "
						+ Integer.toHexString(status));
			}
		}
	}

}
