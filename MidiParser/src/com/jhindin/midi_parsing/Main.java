package com.jhindin.midi_parsing;

import java.io.File;
import java.io.IOException;
import java.util.Formatter;

import javax.sound.midi.ControllerEventListener;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaEventListener;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Receiver;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;
import javax.sound.midi.Transmitter;

import clioptions.CliOptions;
import clioptions.exceptions.parsing.ParsingException;

public class Main {
	
	static Sequencer sequencer;
	static Receiver receiver;

	static final int MILLION = 1000000;
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String midiFileName = null;
		CliOptions options;
		
		try {
			options = new CliOptions("h", new String[]{ "javaplay" });
			options.parse(args);
			String rargs[] = options.getRemaningArgs();
			if (rargs.length == 0) {
				System.err.println("No args");
				usage();
				return;
			}
			if (rargs.length > 1) {
				System.err.println("Extra args");
				usage();
				return;
			}
			
			midiFileName = rargs[0];
		} catch (ParsingException ex) {
			System.err.println("Bad options " + ex);
			return;
		}
		
		try {
			Sequence sequence = MidiSystem.getSequence(new File(midiFileName));
			System.out.println("File " + midiFileName +" parsed");
			

			if (options.isOptionSet("javaplay")) {
				System.out.println("Play with java");
				try {
					sequencer = MidiSystem.getSequencer();
					sequencer.open();
					sequencer.setSequence(sequence);
					Transmitter t = sequencer.getTransmitter();
					t.setReceiver(new LogReceiver());
					sequencer.start();
				} catch (Exception ex) {
					System.err.println("Playing failed: " + ex);
				}
			} else {

				System.out.println("Division type " + sequence.getDivisionType());
				System.out.println("Resolution " + sequence.getResolution());
				if (sequence.getDivisionType() == Sequence.PPQ) {
					System.out.println("PPQ mode");
				} else {
					System.err.println("Division type not yet supported");
					return;
				}
				System.out.println("Length " + sequence.getMicrosecondLength() / 1000 + "ms");
				
				Track tracks[] = sequence.getTracks();
				try {
					receiver = MidiSystem.getReceiver();

					long currentTime = System.currentTimeMillis();
					Thread trackThreads[] = new Thread[tracks.length];
					
					for (int i = 0; i < tracks.length; i++) {
						trackThreads[i] = playTrack(receiver, tracks[i], 
								sequence.getDivisionType(), sequence.getResolution(), currentTime);
					} 
					for (int i = 0; i < trackThreads.length; i++) {
						trackThreads[i].join();
					}
					receiver.close();
				} catch (Exception ex) {
					System.err.println("Playing failed: " + ex);
					ex.printStackTrace();
				}
			}
		} catch (InvalidMidiDataException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	static void usage()
	{
		System.err.println("MidiParser [-h] [--javaplay] <midi file>");
	}
	
	static Thread playTrack(Receiver receiver, Track track, float division, long resolution,
			long currentTime) {
		Thread t = new Thread(new TrackPlayer(receiver, track, division, resolution,
				currentTime));
		t.start();
		return t;
	}
	
	static class TrackPlayer implements Runnable {
		Receiver receiver;
		Track track;
		int eventIndex = 0;
		float division;
		long resolution;
		PreciseTime tempo = new PreciseTime(500, 0);
		PreciseTime startTime;
		
		PreciseTime tickDuration = new PreciseTime(0, 0);

		PreciseTime eventTime = new PreciseTime(0, 0);
		
		TrackPlayer(Receiver receiver, Track track, float division, long resolution,
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
					System.out.println("Event at " + event.getTick() + " ticks: " + Printout.eventToString(event));
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
}
