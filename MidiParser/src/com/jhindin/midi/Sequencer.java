package com.jhindin.midi;

import java.util.concurrent.CopyOnWriteArrayList;

public class Sequencer {
	boolean running = false;
	Sequence sequence;
	int currentTrack = 0;
	
	TrackThread trackThreads[]; 
	
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
				trackThreads[i].t = new Thread(new ParserTrackPlayer(trackThreads[i]));
				trackThreads[i].t.start();
			}
		} else {
			currentTrack = 0;
			trackThreads[currentTrack].t = new Thread(new ParserTrackPlayer(trackThreads[currentTrack]));
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
	
	class TrackThread {
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

	}

	class ParserTrackPlayer implements Runnable {
		TrackThread track;
		
		public ParserTrackPlayer(TrackThread track) {
			this.track = track;
		}
		@Override
		public void run() {
			try {
				MidiEvent event = MidiEvent.read(track.sequenceTrack.chunk.is);
				if (event == null)
					return;
				
			} catch (Exception ex) {
				track.exception = ex;
				return;
			}
			
		}
	}

}
