package com.jhindin.midi;

import java.util.concurrent.CopyOnWriteArrayList;

public class Sequencer {
	boolean running = false;
	Sequence sequence;

	int currentTrack = 0;
	
	PlayThread playThread;
	
	CopyOnWriteArrayList<StateListener> stateListeners = new CopyOnWriteArrayList<>();
	
	public Sequencer(Sequence sequence) throws MidiException {
		this.sequence = sequence;
		playThread = new PlayThread(this);
		
	}

	public Sequence getSequence() {
		return sequence;
	}

	
	public synchronized void start() {
		running = true;
		playThread.t.start();
	}
	
	public synchronized void stop() {
		running = false;
		this.notifyAll();
		playThread = null;
	}
	
	synchronized boolean getRunning() {
		return running;
	}
	
	public void addMessageListener(int track, EventListener l) {
		playThread.listeners.add(l);
	}

	public void removeMessageListener(int track, EventListener l) {
		playThread.listeners.remove(l);
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
		case 1:
			return null;
		case 2:
			if (currentTrack == sequence.tracks.length)
				return null;
			return sequence.tracks[++currentTrack];
		default:
			return null;
		}
	}
	
}
