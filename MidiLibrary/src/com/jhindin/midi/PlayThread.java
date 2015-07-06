package com.jhindin.midi;

import java.util.Iterator;
import java.util.PriorityQueue;
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
	
	Track tracks[];

	PreciseTime quaterNoteDuration = new PreciseTime(500, 0);
	PreciseTime tickDuration = new PreciseTime();
	
	PriorityQueue<ScheduleEvent> queue;
	
	public PlayThread(Sequencer sequencer) {
		// Format 0 and 1 - play tracks in succession
		this.sequencer = sequencer;

		tracks = sequencer.sequence.tracks;
		t = new Thread(this);
		queue = new PriorityQueue<>();
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

		long startTime = System.nanoTime();
		
		PreciseTime currentTime = new PreciseTime();
		PreciseTime sequenceTime = new PreciseTime();
		PreciseTime delta = new PreciseTime();

		try {
			for (Track track : sequencer.sequence) {
				consumeEvents(track, 0);
			}

			while (!queue.isEmpty()) {
				ScheduleEvent event = queue.remove();
				MidiEvent midiEvent = event.midiEvent;
				
				PreciseTime.set(currentTime, 0, System.nanoTime() - startTime);
				
				PreciseTime.mult(tickDuration, event.tick, sequenceTime);
				
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
				dispatchEvent(midiEvent);
				consumeEvents(event, midiEvent.tick);
			}
		}  catch (Exception ex) {
			for (StateListener l : this.sequencer.stateListeners) {
				l.exceptionRaised(index, ex);
			}
			return;
		}
	}

	void consumeEvents(Track track, long tick) throws Exception
	{
		consumeEvents(new ScheduleEvent(track), tick);
	}

	void consumeEvents(ScheduleEvent e, long tick) throws Exception {
		MidiEvent midiEvent = null;
		while (e.it.hasNext() && (midiEvent = e.it.next()).tick <= tick) {
			dispatchEvent(midiEvent);
			midiEvent = null;
		}
		if (midiEvent != null) {
			e.midiEvent = midiEvent;
			e.tick = midiEvent.tick;
			queue.add(e);
		}
		
	}

	void dispatchEvent(MidiEvent event) throws Exception {
		if (event.message.data[0] == (byte)0xff) 
			processMetaEvent(event);
		
		fireMessageListeners(event);
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
	
	class ScheduleEvent implements Comparable<ScheduleEvent>{
		MidiEvent midiEvent;
		Iterator<MidiEvent> it;
		long tick;
		
		public ScheduleEvent(Track t) {
			it = t.iterator();
		}

		@Override
		public int compareTo(ScheduleEvent o) {
			// TODO Auto-generated method stub
			return Long.compare(midiEvent.tick, o.midiEvent.tick);
		}
		
	}
}