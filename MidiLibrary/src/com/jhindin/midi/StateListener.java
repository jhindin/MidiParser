package com.jhindin.midi;

public interface StateListener {
	public void sequenceStarts();
	public void sequenceEnds();
	public void trackStarts(int index);
	public void trackEnds(int index);
	public void exceptionRaised(int index, Exception ex);
}
