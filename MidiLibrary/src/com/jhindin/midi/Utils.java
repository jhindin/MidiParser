package com.jhindin.midi;

import java.io.IOException;
import java.io.InputStream;

import com.jhindin.midi.time.PreciseTime;

public class Utils {
	static final long readVariableLength(InputStream is, Prefix prefix)
			throws IOException, MidiException {
		int c;
		int i;
		long length = 0;;
		
		for (i = 0; i < 4; i++) {
			c = is.read();
			if (c < 0) 
				return -1;
			
			if (prefix != null)
				prefix.data[prefix.pos++] = (byte)(c & 0xff);
			
			length <<= 7;
			length |= (c & 0x7f);
			if ((c & 0x80) == 0)
				break;
		}
		
		return length;
	}

	static class Prefix {
		byte data[] = new byte[6];
		int pos = 0;
	}
	
	static void tempoToQuaterNoteLength(MidiMetaMessage message, PreciseTime quaterNoteDuration) {
		switch (message.type) {
		case MidiMetaMessage.TEMPO:
			long t = ((message.data[message.dataOffset] & 0xff) << 16) |
					((message.data[message.dataOffset + 1] & 0xff) << 8) |
					(message.data[message.dataOffset + 2] & 0xff);
			
			PreciseTime.set(quaterNoteDuration, t / 1000, 
					(int)((t % 1000) * 1000));
			
			break;
		default:
			break;
		}
		
	}
}
