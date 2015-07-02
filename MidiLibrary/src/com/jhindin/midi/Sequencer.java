package com.jhindin.midi;

import java.util.concurrent.CopyOnWriteArrayList;

public class Sequencer {
	boolean running = false;
	Sequence sequence;

	int currentTrack = 0;
	
	TrackThread trackThreads[];
	
	CopyOnWriteArrayList<StateListener> stateListeners = new CopyOnWriteArrayList<>();
	
	public Sequencer(Sequence sequence) throws MidiException {
		this.sequence = sequence;
		if (sequence.format == 2) { 
			trackThreads = new TrackThread[sequence.nTracks];
			for (int i = 0; i < trackThreads.length; i++) {
				trackThreads[i] = new TrackThread(this, sequence.tracks[i]);
			}
		} else {
			trackThreads = new TrackThread[1];
			trackThreads[0] = new TrackThread(this);
		}
	}

	public Sequence getSequence() {
		return sequence;
	}

	
	public synchronized void start() {
		running = true;
		if (sequence.format == 2) {
			long currentTime = System.nanoTime();
			for (TrackThread tt : trackThreads) {
				tt.startTime = currentTime;
				tt.t = new Thread(tt);
				tt.t.start();
			}
		} else {
			currentTrack = 0;
			trackThreads[currentTrack].startTime = System.nanoTime();
			trackThreads[currentTrack].t = new Thread(trackThreads[currentTrack]);
			trackThreads[currentTrack].t.start();
		}
	}
	
	public synchronized void stop() {
		running = false;
		this.notifyAll();
		if (sequence.format == 2) {
			for (int i = 0; i < trackThreads.length; i++) {
				trackThreads[i].t = null;;
			}
		} else {
			trackThreads[currentTrack].t = null;
		}
	}
	
	synchronized boolean getRunning() {
		return running;
	}
	
	public void addMessageListener(int track, EventListener l) {
		if (sequence.format == 2) {
			trackThreads[track].listeners.add(l);
		} else {
			trackThreads[0].listeners.add(l);
		}
	}

	public void removeMessageListener(int track, EventListener l) {
		if (sequence.format == 2) {
			trackThreads[track].listeners.remove(l);
		} else {
			trackThreads[0].listeners.remove(l);
		}
	}
	
	public void addStateListener(StateListener l) {
		stateListeners.add(l);
	}

	public void removeStateListener(StateListener l) {
		stateListeners.remove(l);
	}
	
	public Track nextTrack() {
		switch (sequence.format) {
		case 0:
		case 2:
			return null;
		case 1:
			if (currentTrack == sequence.nTracks)
				return null;
			return sequence.tracks[++currentTrack];
		default:
			return null;
		}
	}
	
}
