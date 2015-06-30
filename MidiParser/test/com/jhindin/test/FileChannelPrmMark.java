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
                 { 0, 10, 20 },
                 { 100, 2000, 3000 },  
                 { 300, 2000, 2000 },  
           });
    }
    
    @Parameter
	public int offset;
    
    @Parameter(value = 1)
	public int markLength;

    @Parameter(value = 2)
	public int readAfterLength;
    
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
        srcArray = new byte[10240];
        for (int i = 0; i < 1024; i++) {
        	System.arraycopy("0123456789".getBytes(), 0, srcArray, i * 10, 10);
        }
	}

	@Before
	public void setUp() throws Exception {
        bc = new SeekableInMemoryByteChannel(srcArray);
        is = new ChannelInputStream(bc);
	}

	@Test
	public void test() throws IOException {
		int c;
		int i = 0;
		
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
