package com.jhindin.midi;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Iterator;

public class Sequence implements Iterable<Track>{
	short format, division;
	public enum DivisionMode { PPQ_DIVISION, SMTPE_DIVISION };
	DivisionMode divisionMode;

	public static final float PPQ          = 0.0f;
	public static final float SMPTE_24     = 24.0f;
	public static final float SMPTE_25     = 25.0f;
	public static final float SMPTE_30     = 30.0f;
    public static final float SMPTE_30DROP = 29.97f;
    
	short ticksPerPPQ; // for PPQ division
	int ticksPerFrame, fps; // for SMTPE division;
	
	Track tracks[];
	
	public Sequence() {
		format = 0;
		division = 0;
		divisionMode = DivisionMode.PPQ_DIVISION;
		ticksPerPPQ = 0;
		ticksPerFrame = 0;
		fps = 0;
	}
	
	public Sequence(InputStream is) throws IOException, MidiException {
		if (!is.markSupported()) 
			is = new BufferedInputStream(is);
		
		ParsingContext context = new ParsingContext();
		parseHeader(is, context);

		tracks = new Track[context.nTracks];

		for (int i = 0; i < context.nTracks; i++) {
			tracks[i] = new Track(i, TrackStreamChunk.getChunk(is));
		}
	}
	
	void parseHeader(InputStream is, ParsingContext context) throws IOException, MidiException {
		MemoryChunk header = MemoryChunk.getChunk(is);
		
		if (header.body.length != 6) {
			throw new MidiException("Unexpected header length " + header.body.length);
		}
		
		format = bytes2Short(header.body, 0);
		if (format != 0 && format != 1 && format != 2) {
			throw new MidiException("Unexpected format " + format);
		}
		
		context.nTracks = bytes2Short(header.body, 2);
		
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
	
	public int getFormat() {
		return format;
	}

	public DivisionMode getDivisionMode() {
		return divisionMode;
	}
	
	public float getDivisionType() {
		if (divisionMode == DivisionMode.PPQ_DIVISION) { 
			return PPQ;
		} else {
			switch (fps) {
			case 24:
				return SMPTE_24;
			case 25:
				return SMPTE_25;
			case 29:
				return SMPTE_30DROP;
			case 30:
				return SMPTE_30;
			default:
				return -1.0f;
			}
		}
	}

	public short getTicksPerPPQ() {
		return ticksPerPPQ;
	}

	public int getTicksPerFrame() {
		return ticksPerFrame;
	}

	public int getFps() {
		return fps;
	}

	short bytes2Short(byte raw[], int offset) {
		return (short)(((raw[offset] & 0xff) << 8) | (raw[offset + 1] & 0xff));
	}

	@Override
	public Iterator<Track> iterator() {
		return Arrays.asList(tracks).iterator();
	}
	
	public Track[] getTracks() {
		return tracks;
	}
	
	
	protected class ParsingContext {
		int nTracks = 0;
	}

}
