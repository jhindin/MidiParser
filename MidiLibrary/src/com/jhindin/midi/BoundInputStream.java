package com.jhindin.midi;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

public class BoundInputStream extends InputStream {
	boolean eof = false;
	boolean eofReached = false;
	long pos = 0;
	long length;
	long markPos = -1;
	InputStream is;
	
	public BoundInputStream(InputStream is, long length ) {
		this.is = is;
		this.length = length;
	}
	

	@Override
	public int read() throws IOException {
		if (eofReached)
			throw new EOFException("Read after eof");
		
		if (eof) {
			eofReached = true;
			return -1;
		}
		
		int  c = is.read();
		if (c < 0) {
			eof = true;
			return c;
		}
		
		if (++pos == length) 
			eof = true;
		
		return c;
	}


	@Override
	public int read(byte[] b) throws IOException {
		return read(b, 0, b.length);
	}


	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		if (eofReached)
			throw new EOFException("Read after eof");
		
		if (eof) {
			eofReached = true;
			return -1;
		}
		
		int rc = is.read(b, off, len);
		if (rc < 0) {
			eof = true;
			return rc;
		}
		pos += rc;
		if (pos == length) 
			eof = true;
		
		return rc;
	}


	@Override
	public long skip(long n) throws IOException {
		if (eofReached)
			throw new EOFException("Read after eof");
		
		if (eof) {
			eofReached = true;
			return -1;
		}
		
		long rc = is.skip(n);
		if (rc < 0) {
			eof = true;
			return rc;
		}
		pos += rc;
		if (pos == length) 
			eof = true;
		
		return rc;
	}


	@Override
	public int available() throws IOException {
		long delegateAvailable = is.available();
		
		long minAvailable =  Math.min(delegateAvailable, length - pos);
		
		return (int)Math.min(Integer.MAX_VALUE, minAvailable);
	}

	@Override
	public void close() throws IOException {
	}

	@Override
	public synchronized void mark(int readlimit) {
		is.mark(readlimit);
		markPos = pos;
	}

	@Override
	public synchronized void reset() throws IOException {
		if (markPos == -1) {
			throw new IOException("reset without mark");
		}
		is.reset();
		pos = markPos;
	}

	@Override
	public boolean markSupported() {
		return true;
	}

}
