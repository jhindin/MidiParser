package com.jhindin.midi.parsing;

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;

public class StreamChunk extends Chunk {
	InputStream is;

	static StreamChunk getChunk(InputStream is) throws IOException,
			MidiException {
		StreamChunk ret = new StreamChunk();

		ret.getTypeAndLength(is);

		ret.is = is;
		return ret;
	}
}
