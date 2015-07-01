package com.jhindin.midi;

public interface StateListener {
	public void sequenceStarts(int index);
	public void sequenceEnds(int index);
	public void exceptionRaised(int index, Exception ex);
}
