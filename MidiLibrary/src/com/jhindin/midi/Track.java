package com.jhindin.midi;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import com.jhindin.midi.time.PreciseTime;

class Track implements Iterable<MidiEvent> {
	int index;
	TrackStreamChunk chunk;
	ArrayList<MidiEvent> events = new ArrayList<MidiEvent>();
	Sequence sequence;
	long microSecondLength = -1;

	Track(Sequence sequence, int index, TrackStreamChunk chunk)
			throws IOException, MidiException {
		this.index = index;
		this.chunk = chunk;
		this.sequence = sequence;

		MidiEvent.ParsingContext context = new MidiEvent.ParsingContext();
		MidiEvent event;
		do {
			event = MidiEvent.read(chunk.is, context);
			if (event != null) {
				events.add(event);
			}
		} while (event != null);
	}

	@Override
	public Iterator<MidiEvent> iterator() {
		// TODO Auto-generated method stub
		return events.iterator();
	}

	public long getTickLength() {
		return events.get(events.size() - 1).tick;
	}

	public long getMicroSecondLength() {
		synchronized (this) {
			if (microSecondLength != -1)
				return microSecondLength;
		}
		
		long ms;

		if (sequence.divisionMode == Sequence.DivisionMode.PPQ_DIVISION) {
			PreciseTime length = new PreciseTime();
			MidiEvent lastNonTempoEvent = null;

			PreciseTime quaterNoteDuration = new PreciseTime(500, 0);
			PreciseTime tickDuration = new PreciseTime();

			PreciseTime.div(quaterNoteDuration, sequence.resolution,
					tickDuration);
			for (MidiEvent event : events) {
				if (event.getMessage() instanceof MidiMetaMessage) {

					MidiMetaMessage metaMessage = (MidiMetaMessage) event
							.getMessage();
					if (metaMessage.type == MidiMetaMessage.TEMPO) {
						addLastEventTickToLength(lastNonTempoEvent, tickDuration,
								length);

						Utils.tempoToQuaterNoteLength(metaMessage,
								quaterNoteDuration);
						PreciseTime.div(quaterNoteDuration,
								sequence.resolution, tickDuration);
						lastNonTempoEvent = null;
					}
				} else {
					lastNonTempoEvent = event;
				}
			}

			addLastEventTickToLength(lastNonTempoEvent, tickDuration,
					length);

			ms = length.millis * 1000 + length.nanos / 1000;
		} else {
			double ticksPerSecond = (double)(sequence.fps * sequence.resolution);
			double seconds = getTickLength()/ticksPerSecond;
			ms = (long)(10000000 * seconds);
		}


		synchronized (this) {
			microSecondLength = ms;
		}
		return microSecondLength;
	}

	void addLastEventTickToLength(MidiEvent lastEvent,
			PreciseTime tickDuration, PreciseTime length) {
		if (lastEvent != null) {
			PreciseTime tickTime = new PreciseTime();
			PreciseTime.mult(tickDuration, lastEvent.tick, tickTime);
			PreciseTime.add(tickTime, length, length);
		}
	}
}