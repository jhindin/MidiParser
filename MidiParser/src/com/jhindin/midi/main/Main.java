package com.jhindin.midi.main;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Receiver;
import javax.sound.midi.Sequencer;
import javax.sound.midi.Track;
import javax.sound.midi.Transmitter;

import com.jhindin.midi.MidiException;
import com.jhindin.midi.StateListener;

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
		final boolean verbose;
		
		try {
			options = new CliOptions("hp:v");
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
			
			verbose = options.isOptionSet("v");
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
					if (verbose) 
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
				InputStream is = new FileInputStream(new File(midiFileName));
				com.jhindin.midi.Sequence mc = new com.jhindin.midi.Sequence(is);
				com.jhindin.midi.Sequencer sequencer = new com.jhindin.midi.Sequencer(mc);

				try {
					final Receiver receiver = MidiSystem.getReceiver();
					sequencer.addMessageListener(0, new com.jhindin.midi.EventListener() {
						
						@Override
						public void receiveEvent(int track, com.jhindin.midi.MidiEvent e) throws Exception {
							com.jhindin.midi.MidiMessage m = e.getMessage();
							if (verbose)
								System.out.println("Message " + m);
							if (m instanceof com.jhindin.midi.ShortMessage) {
								javax.sound.midi.ShortMessage sm;
								switch (m.getBytes().length) {
								case 1:
									sm = new javax.sound.midi.ShortMessage(m.getBytes()[0]);
									break;
								case 2:
									sm = new javax.sound.midi.ShortMessage(m.getBytes()[0],
											m.getBytes()[1], 0);
									break;
								case 3:
									sm = new javax.sound.midi.ShortMessage(m.getBytes()[0],
											m.getBytes()[1], m.getBytes()[2]);
									break;
								default:
									throw new MidiException("Unexpected message length");
								}
								receiver.send(sm, -1);
							}
						}
					});
					
					sequencer.addStateListener(new StateListener() {
						
						@Override
						public void sequenceStarts() {
							// TODO Auto-generated method stub
							
						}
						
						@Override
						public void sequenceEnds() {
							try {
								Thread.sleep(300);
							} catch (Exception ex) {}
						}
						
						@Override
						public void exceptionRaised(int index, Exception ex) {
							System.err.println("Exception while playing: " + ex);
							ex.printStackTrace();
						}

						@Override
						public void trackStarts(int index) {
							// TODO Auto-generated method stub
							
						}

						@Override
						public void trackEnds(int index) {
							// TODO Auto-generated method stub
							
						}
					});
					sequencer.start();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
					
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
		System.err.println("MidiParser [-hv] [-p java|soft|parse] <midi file>\n" + 
				"-v   verbose");
	}
	
	static Thread playTrack(Receiver receiver, Track track, float division, long resolution,
			long currentTime) {
		Thread t = new Thread(new SoftTrackPlayer(receiver, track, division, resolution,
				currentTime));
		t.start();
		return t;
	}
}
