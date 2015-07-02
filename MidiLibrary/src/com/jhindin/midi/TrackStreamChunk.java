package com.jhindin.midi;

import java.io.IOException;
import java.io.InputStream;

public class TrackStreamChunk extends Chunk {
	InputStream is;

	static TrackStreamChunk getChunk(InputStream is) throws IOException,
			MidiException {
		TrackStreamChunk ret = new TrackStreamChunk();

		ret.getTypeAndLength(is);

		ret.is = new BoundInputStream(is, ret.length);
		return ret;
	}
}
