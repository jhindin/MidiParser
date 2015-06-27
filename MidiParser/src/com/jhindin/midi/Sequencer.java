package com.jhindin.midi;

import java.util.concurrent.CopyOnWriteArrayList;

public class Sequencer {
	boolean running = false;
	Sequence sequence;
	int currentTrack = 0;
	
	TrackThread trackThreads[];
	
	CopyOnWriteArrayList<StateListener> stateListeners = new CopyOnWriteArrayList<>();
	
	public Sequencer(Sequence sequence) {
		this.sequence = sequence;
		trackThreads = new TrackThread[sequence.nTracks];
		for (int i = 0; i < trackThreads.length; i++) {
			trackThreads[i] = new TrackThread(sequence.tracks[i]);
		}
	}
	
	public synchronized void start() {
		running = true;
		if (sequence.format == 2) {
			for (int i = 0; i < trackThreads.length; i++) {
				trackThreads[i].t = new Thread(trackThreads[i]);
				trackThreads[i].t.start();
			}
		} else {
			currentTrack = 0;
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
	
	public void addMessageListener(int track, MessageListener l) {
		if (sequence.format == 2) {
			trackThreads[track].listeners.add(l);
		} else {
			trackThreads[0].listeners.add(l);
		}
	}

	public void removeMessageListener(int track, MessageListener l) {
		if (sequence.format == 2) {
			trackThreads[track].listeners.remove(l);
		} else {
			trackThreads[0].listeners.remove(l);
		}
	}

	class TrackThread implements Runnable{
		Thread t;
		Exception exception;
		CopyOnWriteArrayList<MessageListener> listeners = new CopyOnWriteArrayList<>();
		int index;
		Sequence.Track sequenceTrack;
		
		public TrackThread(Sequence.Track sequenceTrack) {
			this.sequenceTrack = sequenceTrack;
		}

		void fireMessageListeners(byte message[]) {
			for (MessageListener l : listeners) {
				l.receiveMessage(index, message);
			}
		}

		@Override
		public void run() {
			try {
				MidiEvent event = MidiEvent.read(sequenceTrack.chunk.is);
				if (event == null)
					return;
				
			} catch (Exception ex) {
				for (StateListener l : stateListeners) {
					l.exceptionRaised(index, ex);
				}
				return;
			}
		}
	}
}
