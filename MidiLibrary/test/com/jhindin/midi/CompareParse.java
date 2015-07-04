package com.jhindin.midi;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiSystem;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class CompareParse {
	@Parameters
	public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
        		{ "../MidiParser/ants.mid" }
        });
	}
	
	@Parameter
	public String fileName;
	
	@Test
	public void test() throws InvalidMidiDataException, IOException, MidiException {
		javax.sound.midi.Sequence bseq = MidiSystem.getSequence(new File(fileName));
		
		com.jhindin.midi.Sequence jseq = 
				new com.jhindin.midi.Sequence(new FileInputStream(fileName));
		
		assertEquals(bseq.getTracks().length, jseq.getNTracks()); 
		
		
	}

}
