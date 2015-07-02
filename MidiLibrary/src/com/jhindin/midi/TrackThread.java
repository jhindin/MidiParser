package com.jhindin.midi;

import java.util.concurrent.CopyOnWriteArrayList;

import com.jhindin.midi.time.PreciseTime;

class TrackThread implements Runnable {
	/**
	 * 
	 */
	private Sequencer sequencer;
	Thread t;
	Exception exception;
	CopyOnWriteArrayList<EventListener> listeners = new CopyOnWriteArrayList<>();
	int index;
	Sequence.Track sequenceTrack;

	PreciseTime quaterNoteDuration = new PreciseTime(500, 0);
	PreciseTime tickDuration = new PreciseTime();
	
	long startTime;
	
	public TrackThread(Sequencer sequencer, Sequence.Track sequenceTrack) {
		this.sequencer = sequencer;
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

		for (StateListener l : this.sequencer.stateListeners) {
			l.trackStarts(index);
		}
		
		try {
			setTickDuration();

			for (;;) {
				MidiEvent event = MidiEvent.read(sequenceTrack.chunk.is, runningStatus);
				while (event == null) {
					for (StateListener l : this.sequencer.stateListeners) {
						l.trackEnds(index);
					}

					sequenceTrack = sequencer.nextTrack();
					if (sequenceTrack == null)
						break;
					
					elapsedTicks = 0;
					runningStatus = 0;
					PreciseTime.set(quaterNoteDuration, 500, 0);
					event = MidiEvent.read(sequenceTrack.chunk.is, runningStatus);
				}
					
				
				runningStatus = event.message.data[0];
				
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

	void setTickDuration()  {
		if (this.sequencer.sequence.divisionMode == Sequence.DivisionMode.PPQ_DIVISION) {
			PreciseTime.div(quaterNoteDuration, this.sequencer.sequence.ticksPerPPQ, tickDuration);
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