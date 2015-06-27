package com.jhindin.midi.main;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Receiver;
import javax.sound.midi.Sequencer;
import javax.sound.midi.Track;
import javax.sound.midi.Transmitter;

import com.jhindin.midi.MessageListener;
import com.jhindin.midi.MidiException;

import clioptions.CliOptions;
import clioptions.exceptions.parsing.ParsingException;

public class Main {
	
	static Sequencer sequencer;
	static Receiver receiver;

	static enum  PlayMode { JAVA, SOFT, PARSE };
	static PlayMode playMode;
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String midiFileName = null;
		CliOptions options;
		
		try {
			options = new CliOptions("hp:");
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
			
			String arg = options.getOptionValue("p");
			if (arg == null) {
				playMode = PlayMode.PARSE;
			} else if (arg.equals("java")) {
				playMode = PlayMode.JAVA;
			} else if (arg.equals("soft")) {
				playMode = PlayMode.SOFT;
			} else if (arg.equals("parse")) {
				playMode = PlayMode.PARSE;
			} else {
				System.err.println("Unexpected value for 'p' option");
				usage();
				return;
			}
		} catch (ParsingException ex) {
			System.err.println("Bad options " + ex);
			return;
		}
		
		try {
			javax.sound.midi.Sequence sequence = MidiSystem.getSequence(new File(midiFileName));
			System.out.println("File " + midiFileName +" parsed");
			

			switch (playMode) {
			case JAVA:
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
				break;
			case SOFT:
				System.out.println("Play midi with Java parser and custom sequencer");
				System.out.println("Division type " + sequence.getDivisionType());
				System.out.println("Resolution " + sequence.getResolution());
				if (sequence.getDivisionType() == javax.sound.midi.Sequence.PPQ) {
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
				break;
			case PARSE:
				RandomAccessFile raf = new RandomAccessFile(new File(midiFileName), "r");
				com.jhindin.midi.Sequence mc = new com.jhindin.midi.Sequence(raf);
				com.jhindin.midi.Sequencer sequencer = new com.jhindin.midi.Sequencer(mc);
				sequencer.addMessageListener(0, new MessageListener() {
					
					@Override
					public void receiveMessage(int track, byte[] message) {
						// TODO Auto-generated method stub
						
					}
				});
					
				break;
			}
		} catch (InvalidMidiDataException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (MidiException e) {
			e.printStackTrace();
		}

	}

	static void usage()
	{
		System.err.println("MidiParser [-h] [-p java|soft|parse] <midi file>");
	}
	
	static Thread playTrack(Receiver receiver, Track track, float division, long resolution,
			long currentTime) {
		Thread t = new Thread(new SoftTrackPlayer(receiver, track, division, resolution,
				currentTime));
		t.start();
		return t;
	}
}
