package com.jhindin.midi.parsing;

import java.io.IOException;
import java.io.InputStream;

public class MidiContext {
	InputStream is;
	short format, nTracks, division;
	enum DivisionMode { PPQ_DIVISION, SMTPE_DIVISION } ;
	DivisionMode divisionMode;
	short ticksPerPPQ; // for PPQ division
	int ticksPerFrame, fps; // for SMTPE division;
	
	public MidiContext(InputStream is) throws IOException, MidiException {
		this.is = is;
		Chunk header = Chunk.getInMemoryChunk(is);
		
		if (header.body.length != 6) {
			throw new MidiException("Unexpected header length " + header.body.length);
		}
		
		format = bytes2Short(header.body, 0);
		if (format != 0 && format != 1 && format != 2) {
			throw new MidiException("Unexpected format " + format);
		}
		
		nTracks = bytes2Short(header.body, 2);
		
		division = bytes2Short(header.body, 4);
		if ((division & 0x8000) == 0) {
			divisionMode = DivisionMode.PPQ_DIVISION;
			ticksPerPPQ = division;
		} else {
			divisionMode = DivisionMode.SMTPE_DIVISION;
			ticksPerFrame = division & 0xff;
			fps = division & 0x7f00;
		}
	}

	short bytes2Short(byte raw[], int offset) {
		return (short)(((raw[offset] & 0xff) << 8) | (raw[offset + 1] & 0xff));
	}



}
