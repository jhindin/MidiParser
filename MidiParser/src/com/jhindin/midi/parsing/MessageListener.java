package com.jhindin.midi.parsing;

public interface MessageListener {
	public void receiveMessage(int track, byte[] message);
}
