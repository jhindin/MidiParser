package com.jhindin.test;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.junit.*;

import com.jhindin.midi.ChannelInputStream;

public class FileChannelStreamTest {

	static byte srcArray[];
	
	SeekableInMemoryByteChannel bc;
	ChannelInputStream is;

	@BeforeClass
    public static void beforeClass() {
        srcArray = new byte[10240];
        for (int i = 0; i < 1024; i++) {
        	System.arraycopy("0123456789".getBytes(), 0, srcArray, i * 10, 10);
        }
        
    }
  
    @AfterClass
    public static void afterClass() {
        System.out.println("@AfterClass");
    }
    
	@Before
	public void setUp() throws IOException {
        bc = new SeekableInMemoryByteChannel(srcArray);
        is = new ChannelInputStream(bc);
	}

	@Test
	public void simpleRead() throws IOException {
		ByteArrayOutputStream obs = new ByteArrayOutputStream();
		
		int c;
		while ((c = is.read()) != -1) {
			obs.write((byte)c);
		}
		byte res[] = obs.toByteArray();
		
		assertArrayEquals(srcArray, res);;
	}

	@Test
	public void shortMarkAndReset() throws IOException {
		
		int i = 0;
		
		for (i = 0; i < 2000; i++)
			is.read();
		
		is.mark(10);

		byte readAfterMark[] = new byte[10];
		for (i = 0; i < readAfterMark.length; i++) 
			readAfterMark[i] = (byte)is.read();
		
		is.reset();
		byte readAfterReset[] = new byte[readAfterMark.length];
		for (i = 0; i < readAfterReset.length; i++) 
			readAfterReset[i] = (byte)is.read();
		
		assertArrayEquals(readAfterMark, readAfterReset);
	}
}
