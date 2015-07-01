package com.jhindin.midi;

import java.io.IOException;
import java.io.InputStream;

public abstract class Chunk {
	int type;
	int length;
	
	static protected int readInt(InputStream is) throws IOException, MidiException {
		byte b3 = readByte(is);
		byte b2 = readByte(is);
		byte b1 = readByte(is);
		byte b0 = readByte(is);

		return ((b3 & 0xff) << 24) | ((b2 & 0xff) << 16) | ((b1 & 0xff) << 8)
				| (b0 & 0xff);
	}

	static protected byte readByte(InputStream is) throws IOException, MidiException {
		int c = is.read();
		if (c == -1) {
			throw new MidiException("Unexpected EOF");
		}
		return (byte) c;
	}
	
	protected void getTypeAndLength(InputStream is) throws IOException, MidiException
	{
		type = readInt(is);
		length = readInt(is);
	}


}
