package com.jhindin.midi;

public interface MessageListener {
	public void receiveMessage(int track, byte[] message);
}
