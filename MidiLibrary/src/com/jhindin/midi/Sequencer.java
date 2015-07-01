package com.jhindin.midi;

import java.util.concurrent.CopyOnWriteArrayList;
import com.jhindin.midi.time.PreciseTime;

public class Sequencer {
	boolean running = false;
	Sequence sequence;
	int currentTrack = 0;
	
	TrackThread trackThreads[];
	
	CopyOnWriteArrayList<StateListener> stateListeners = new CopyOnWriteArrayList<>();
	
	public Sequencer(Sequence sequence) throws MidiException {
		this.sequence = sequence;
		trackThreads = new TrackThread[sequence.nTracks];
		for (int i = 0; i < trackThreads.length; i++) {
			trackThreads[i] = new TrackThread(sequence.tracks[i]);
		}
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

	class TrackThread implements Runnable{
		Thread t;
		Exception exception;
		CopyOnWriteArrayList<EventListener> listeners = new CopyOnWriteArrayList<>();
		int index;
		Sequence.Track sequenceTrack;

		PreciseTime quaterNoteDuration = new PreciseTime(500, 0);
		PreciseTime tickDuration = new PreciseTime();
		
		long startTime;
		
		public TrackThread(Sequence.Track sequenceTrack) {
			this.sequenceTrack = sequenceTrack;
		}

		void fireMessageListeners(MidiEvent event) throws Exception {
			for (EventListener l : listeners) {
				l.receiveEvent(index, event);
			}
		}

		@Override
		public void run() {
			PreciseTime currentTime = new PreciseTime();
			PreciseTime sequenceTime = new PreciseTime();
			PreciseTime delta = new PreciseTime();
			byte runningStatus = 0;
			
			long elapsedTicks = 0;

			for (StateListener l : stateListeners) {
				l.sequenceStarts(index);
			}
			
			try {
				setTickDuration();

				for (;;) {
					MidiEvent event = MidiEvent.read(sequenceTrack.chunk.is, runningStatus);
					if (event == null)
						break;
					
					runningStatus = event.message.data[0];
					
					PreciseTime.set(currentTime, 0, System.nanoTime() - startTime);
					elapsedTicks += event.deltaTick;
					
					PreciseTime.mult(tickDuration, elapsedTicks, sequenceTime);
					
					if (!getRunning())
						break;
					
					if (PreciseTime.greater(sequenceTime, currentTime)) {
						PreciseTime.substract(sequenceTime, currentTime, delta);
						synchronized (this) {
							wait(delta.millis, delta.nanos);
						}
						if (!getRunning())
							return;
					}

					if (event.message.data[0] == (byte)0xff) 
						processMetaEvent(event);
					
					fireMessageListeners(event);
					
				}
			}  catch (Exception ex) {
				for (StateListener l : stateListeners) {
					l.exceptionRaised(index, ex);
				}
				return;
			}
			
			for (StateListener l : stateListeners) {
				l.sequenceEnds(index);
			}

		}

		void setTickDuration()  {
			if (sequence.divisionMode == Sequence.DivisionMode.PPQ_DIVISION) {
				PreciseTime.div(quaterNoteDuration, sequence.ticksPerPPQ, tickDuration);
			} else {
				throw new Error("SMTPE not yet supported");
			}
		}

		void processMetaEvent(MidiEvent event)
		{
			MidiMetaMessage message = (MidiMetaMessage)event.message;
			
			switch (message.type) {
			case MidiMetaMessage.TEMPO:
				long t = ((message.data[message.dataOffset] & 0xff) << 16) |
						((message.data[message.dataOffset + 1] & 0xff) << 8) |
						(message.data[message.dataOffset + 2] & 0xff);
				
				PreciseTime.set(quaterNoteDuration, t / 1000, 
						(int)((t % 1000) * 1000));
				
				setTickDuration();
				break;
			default:
				break;
			}
		}
	}
	
}
