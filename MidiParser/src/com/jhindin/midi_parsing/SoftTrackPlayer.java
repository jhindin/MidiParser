package com.jhindin.midi_parsing;

import javax.sound.midi.MidiEvent;
import javax.sound.midi.Receiver;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;

class SoftTrackPlayer implements Runnable {
	Receiver receiver;
	Track track;
	int eventIndex = 0;
	float division;
	long resolution;
	PreciseTime tempo = new PreciseTime(500, 0);
	PreciseTime startTime;
	
	PreciseTime tickDuration = new PreciseTime(0, 0);

	PreciseTime eventTime = new PreciseTime(0, 0);
	
	SoftTrackPlayer(Receiver receiver, Track track, float division, long resolution,
			long startTime) {
		this.receiver = receiver;
		this.track = track;
		this.division = division;
		this.resolution = resolution;
		this.startTime = new PreciseTime(startTime, 0);
		
		setTickDuration();
	}
	
	void setTickDuration() {
		if (division == Sequence.PPQ) {
			PreciseTime.div(tempo, resolution, tickDuration);
		}
	}
	
	void tickToTime(long tick, PreciseTime time) {
		PreciseTime i = new PreciseTime(0, 0);
		PreciseTime.mult(tickDuration, tick, i);
		PreciseTime.add(i, startTime, time);
	}
	
	void sleepTillTime(PreciseTime wakeupTime) throws InterruptedException {
		long currentTime = System.currentTimeMillis();
		long wakeupMillis = wakeupTime.millis - currentTime;

		if (wakeupTime.millis >= currentTime) 
			Thread.sleep(wakeupMillis, wakeupTime.nanos);
	}
	
	void processMetaEvent(byte message[]) {
		if (message.length == 6 && 
				message[0] == (byte)ShortMessage.SYSTEM_RESET &&
				message[1] == (byte)0x51) {
			int newTempo = ((message[3] & 0xff) << 16) |
					((message[4] & 0xff) << 8) |
					(message[5] & 0xff);
			tempo.millis = newTempo / 1000;
			tempo.nanos = (newTempo % 1000) * 1000;
			setTickDuration();
			{
				PreciseTime trackDuration = new PreciseTime(0, 0);
				PreciseTime.mult(tickDuration, track.ticks(), trackDuration);
				System.out.println("track duration " + trackDuration + " for " +
						track.ticks() + " ticks");
			}
		}
	}
	
	public void run() {
		try {
			while (eventIndex < track.size()) {
				MidiEvent event = track.get(eventIndex++);
				byte messageBytes[] = event.getMessage().getMessage(); 
				if ((messageBytes[0] & 0xff) == ShortMessage.SYSTEM_RESET) {
					processMetaEvent(messageBytes);
				}
				//System.out.println("Event at " + event.getTick() + " ticks: " + Printout.eventToString(event));
				tickToTime(event.getTick(), eventTime);
				sleepTillTime(eventTime);
				receiver.send(event.getMessage(), 0);
			}
			/* Delay for receiver latency */
			Thread.sleep(300);
		} catch (Exception ex) {
			System.err.println("Playing failed: " + ex);
		}

	}
}