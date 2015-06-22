package com.jhindin.midi.parsing;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

public class MemoryChunk extends Chunk {
	byte body[];

	static MemoryChunk getChunk(InputStream is) throws IOException, MidiException {
		MemoryChunk ret = new MemoryChunk();

		ret.getTypeAndLength(is);

		ret.body = new byte[ret.length];
		try {
			is.read(ret.body);
		} catch (EOFException ex) {
			throw new MidiException("Unexpected EOF");
		}

		return ret;
	}

}
