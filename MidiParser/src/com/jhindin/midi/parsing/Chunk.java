package com.jhindin.midi.parsing;

import java.io.IOException;
import java.io.RandomAccessFile;

public abstract class Chunk {
	int type;
	int length;
	
	static protected int readInt(RandomAccessFile raf) throws IOException, MidiException {
		byte b3 = readByte(raf);
		byte b2 = readByte(raf);
		byte b1 = readByte(raf);
		byte b0 = readByte(raf);

		return ((b3 & 0xff) << 24) | ((b2 & 0xff) << 16) | ((b1 & 0xff) << 8)
				| (b0 & 0xff);

	}

	static protected byte readByte(RandomAccessFile raf) throws IOException, MidiException {
		int c = raf.read();
		if (c == -1) {
			throw new MidiException("Unexpected EOF");
		}
		return (byte) c;
	}
	
	protected void getTypeAndLength(RandomAccessFile raf) throws IOException, MidiException
	{
		type = readInt(raf);
		length = readInt(raf);
	}


}
