package com.jhindin.midi;

import java.io.IOException;
import java.io.InputStream;

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
			
			length <<= 8;
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

}
