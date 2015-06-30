package com.jhindin.test;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.*;

import org.junit.*;
import org.junit.runner.*;
import org.junit.runners.*;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import com.jhindin.midi.ChannelInputStream;

@RunWith(Parameterized.class)
public class FileChannelPrmMark {
	static byte srcArray[];
	
	SeekableInMemoryByteChannel bc;
	ChannelInputStream is;


	@Parameters
	public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                 { 10240, 0, 10, 20 },
                 { 10240, 100, 2000, 3000 },   
                 { 10240, 300, 2000, 2000 },  
                 { 10240, 200, 100, 100 },
                 { 10240, 3100, 200, 180 },
                 { 10240, 10240 - 100, 50, 50 },
                 { 10240, 10240 - 100, 1000, 50 },

                 { 10250, 0, 10, 20 },
                 { 10250, 100, 2000, 3000 },   
                 { 10250, 300, 2000, 2000 },  
                 { 10250, 200, 100, 100 },
                 { 10250, 3100, 200, 180 },
                 { 10250, 10240 - 100, 50, 50 },
                 { 10250, 10240 - 100, 1000, 50 },
                 
                 { 100, 10, 120, 10 },
           });
    }
    
    @Parameter
	public int sourceSize;

    @Parameter(value = 1)
	public int offset;
    
    @Parameter(value = 2)
	public int markLength;

    @Parameter(value = 3)
	public int readAfterLength;

    
	@Test
	public void test() throws IOException {
		int c;
		int i = 0;
		
        srcArray = new byte[sourceSize];
        for (i = 0; i < sourceSize/10; i++) {
        	System.arraycopy("0123456789".getBytes(), 0, srcArray, i * 10, 10);
        }

        bc = new SeekableInMemoryByteChannel(srcArray);
        is = new ChannelInputStream(bc);

		for (i = 0; i < offset; i++)
			is.read();
		
		is.mark(markLength);

		byte readAfterMark[] = new byte[readAfterLength];
		for (i = 0; i < readAfterMark.length; i++) 
			readAfterMark[i] = (byte)is.read();
		
		try {
			is.reset();
			byte readAfterReset[] = new byte[readAfterMark.length];
			for (i = 0; i < readAfterReset.length; i++) 
				readAfterReset[i] = (byte)is.read();
			
			assertArrayEquals(readAfterMark, readAfterReset);
		} catch (IOException ex) {
			if (ex.getMessage().equals("Resetting beyond mark read limit") &&
					readAfterLength > markLength)
				return;
			
			throw ex;
		}
	}
}
