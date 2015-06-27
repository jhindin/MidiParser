package com.jhindin.midi;

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

import com.jhindin.midi.time.PreciseTime;

public class Sequence {
	short format, nTracks, division;
	public enum DivisionMode { PPQ_DIVISION, SMTPE_DIVISION } ;
	DivisionMode divisionMode;

	short ticksPerPPQ; // for PPQ division
	int ticksPerFrame, fps; // for SMTPE division;
	
	Track tracks[];
	
	PreciseTime quaterNoteDuration = new PreciseTime(500, 0);
	
	public Sequence(RandomAccessFile raf) throws IOException, MidiException {
		InputStream fcis = new ChannelInputStream(raf.getChannel());
		parseHeader(fcis);
		if (format == 2) {
			tracks = new Track[nTracks];
			
			tracks[0] = new Track(0, StreamChunk.getChunk(fcis));
			long pos = raf.getFilePointer();
			pos += tracks[0].chunk.length + 8;
			raf.seek(pos);
		
			for (int i = 1; i < nTracks; i++) {
				tracks[i] = new Track(i, StreamChunk.getChunk(new ChannelInputStream(raf.getChannel())));
				pos += tracks[i].chunk.length + 8;
				raf.seek(pos);
			}
		} else {
			setupSingleTrack(fcis);
		}
	}
	
	void parseHeader(InputStream is) throws IOException, MidiException {
		MemoryChunk header = MemoryChunk.getChunk(is);
		
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
	
	void setupSingleTrack(InputStream is) throws IOException, MidiException {
		tracks = new Track[1];
		tracks[0] = new Track(0, StreamChunk.getChunk(is));
	}
	
	public int getFormat() {
		return format;
	}

	public DivisionMode getDivisionMode() {
		return divisionMode;
	}

	public short getNTracks() {
		return nTracks;
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
	


	
	class Track {
		int index;
		StreamChunk chunk;
		
		Track(int index, StreamChunk chunk) {
			this.index = index;
			this.chunk = chunk;
		}
	}

}
