package com.jhindin.midi;

import java.util.concurrent.CopyOnWriteArrayList;

import com.jhindin.midi.time.PreciseTime;

class PlayThread implements Runnable {
	/**
	 * 
	 */
	private Sequencer sequencer;
	Thread t;
	Exception exception;
	CopyOnWriteArrayList<EventListener> listeners = new CopyOnWriteArrayList<>();
	int index;
	Track trackToPlay;

	PreciseTime quaterNoteDuration = new PreciseTime(500, 0);
	PreciseTime tickDuration = new PreciseTime();
	
	long startTime;
	
	public PlayThread(Sequencer sequencer, Track track) {
		// Format 2 - play given track
		this.sequencer = sequencer;
		this.trackToPlay = track;
	}

	public PlayThread(Sequencer sequencer) {
		// Format 0 and 1 - play tracks in succession
		this.sequencer = sequencer;
	}
	
	void fireMessageListeners(MidiEvent event) throws Exception {
		for (EventListener l : listeners) {
			l.receiveEvent(index, event);
		}
	}

	@Override
	public void run() {
		if (sequencer.sequence.divisionMode == Sequence.DivisionMode.PPQ_DIVISION)
			PreciseTime.div(quaterNoteDuration,sequencer.sequence.resolution, tickDuration);

		if (trackToPlay != null) {
			playTrack(trackToPlay);
		} else {
			for (Track track : sequencer.getSequence()) {
				playTrack(track);
			}
		}
		
	}
	
	void playTrack(Track track) {
		PreciseTime currentTime = new PreciseTime();
		PreciseTime sequenceTime = new PreciseTime();
		PreciseTime delta = new PreciseTime();
		
		long elapsedTicks = 0;

		for (StateListener l : this.sequencer.stateListeners) {
			l.trackStarts(index);
		}
		
		try {
			for (MidiEvent event : track) {
				
				PreciseTime.set(currentTime, 0, System.nanoTime() - startTime);
				elapsedTicks += event.deltaTick;
				
				PreciseTime.mult(tickDuration, elapsedTicks, sequenceTime);
				
				if (!this.sequencer.getRunning())
					break;
				
				if (PreciseTime.greater(sequenceTime, currentTime)) {
					PreciseTime.substract(sequenceTime, currentTime, delta);
					synchronized (this) {
						wait(delta.millis, delta.nanos);
					}
					if (!this.sequencer.getRunning())
						return;
				}

				if (event.message.data[0] == (byte)0xff) 
					processMetaEvent(event);
				
				fireMessageListeners(event);
				
			}
		}  catch (Exception ex) {
			for (StateListener l : this.sequencer.stateListeners) {
				l.exceptionRaised(index, ex);
			}
			return;
		}
	}

	

	void processMetaEvent(MidiEvent event)
	{
		MidiMetaMessage message = (MidiMetaMessage)event.message;
		
		switch (message.type) {
		case MidiMetaMessage.TEMPO:
			Utils.tempoToQuaterNoteLength(message, quaterNoteDuration);
			if (sequencer.sequence.divisionMode == Sequence.DivisionMode.PPQ_DIVISION)
				PreciseTime.div(quaterNoteDuration, sequencer.sequence.resolution, tickDuration);
			break;
		default:
			break;
		}
	}
}