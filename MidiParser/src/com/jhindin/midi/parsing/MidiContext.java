package com.jhindin.midi.parsing;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.concurrent.CopyOnWriteArrayList;

public class MidiContext {
	RandomAccessFile raf;
	short format, nTracks, division;
	public enum DivisionMode { PPQ_DIVISION, SMTPE_DIVISION } ;
	DivisionMode divisionMode;

	short ticksPerPPQ; // for PPQ division
	int ticksPerFrame, fps; // for SMTPE division;
	
	Track tracks[];
	
	boolean running = false;
	
	public MidiContext(RandomAccessFile raf) throws IOException, MidiException {
		this.raf = raf;
		MemoryChunk header = MemoryChunk.getChunk(raf);
		
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
		
		if (format == 2) {
			tracks = new Track[nTracks];
		
			for (int i = 0; i < nTracks; i++) {
				long pos = raf.getFilePointer();
				tracks[i].chunk = RandomAccessChunk.getChunk(raf);
				pos += tracks[i].chunk.length + 8;
				raf.seek(pos);
			}
		} else {
			// TODO - type 0 and 1 should work with both random access file and stream
		}
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
	
	public synchronized void start() {
		running = true;
		Thread t = new Thread(new ParserTrackPlayer());
		t.start();
	}
	
	public synchronized void stop() {
		running = false;
		this.notifyAll();
	}
	
	public void addMessageListener(int track, MessageListener l) {
		// TODO - listeners are bound to track for format 2 and context for 0 and 1
	}

	public void removeMessageListener(int track, MessageListener l) {
		// TODO - listeners are bound to track for format 2 and context for 0 and 1
	}

	void fireMessageListeners(int track, byte message[]) {
	}

	class ParserTrackPlayer implements Runnable {
		@Override
		public void run() {
			
		}
	}
	
	class Track {
		RandomAccessChunk chunk;
		CopyOnWriteArrayList<MessageListener> listeners = new CopyOnWriteArrayList<>();
	}

}
