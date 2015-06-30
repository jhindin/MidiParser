package com.jhindin.midi;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;

public class ChannelInputStream extends InputStream {
	SeekableByteChannel sc;
	ByteBuffer buf = ByteBuffer.allocate(1024);
	
	enum  MarkState { CLEAR, SET_WITHIN_BUFFER, SET_POS, EXHAUSTED, FAILURE };
	MarkState markState = MarkState.CLEAR;
	long markPosition;
	
	public ChannelInputStream(SeekableByteChannel sc) throws IOException {
		super();
		this.sc = sc;
		sc.read(buf);
		buf.flip();
	}

	@Override
	public int read() throws IOException {
		int rc;
		
		rc = readBuf();
		if (rc < 0)
			return -1;
		
		return buf.get() & 0xff;
	}

	@Override
	public int read(byte[] b) throws IOException {
		return read(b, 0, b.length);
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		do {
			int rc = readBuf();
			if (rc < 0)
				return rc;

			int bytesToCopy = (rc <= len) ? rc : len;
			buf.get(b, off, bytesToCopy);
			off += bytesToCopy;
			len -= bytesToCopy;
		} while (len > 0);
		return len;
	}

	@Override
	public long skip(long n) throws IOException {
		if ((buf.position() + n) < buf.remaining()) 
			buf.position((int)(buf.position() + n));
				
		long pos = sc.position() + buf.position() - buf.capacity();
		sc.position(pos + n);
		buf.limit(0);
		if (markState == MarkState.SET_WITHIN_BUFFER)
			markState = MarkState.EXHAUSTED;
		return n;
	}

	@Override
	public void close() throws IOException {
		sc.close();
	}

	@Override
	public synchronized void mark(int readlimit)  {
		
		if (readlimit <= buf.remaining()) {
			markState = MarkState.SET_WITHIN_BUFFER;
			buf.mark();
		} else {
			markState = MarkState.SET_POS;
			try {
				markPosition = sc.position() + buf.position() - buf.capacity();
			} catch (IOException ex) {
				markState = MarkState.FAILURE;
			}
		}
	}

	@Override
	public synchronized void reset() throws IOException {
		switch (markState) {
		case CLEAR:
			throw new IOException("Resetting without mark");
		case EXHAUSTED:
			throw new IOException("Resetting beyond mark read limit");
		case SET_WITHIN_BUFFER:
			buf.reset();
			break;
		case SET_POS:
			sc.position(markPosition);
			buf.limit(0);
			break;
		case FAILURE:
			throw new IOException("Mark failed");
		}
	}

	@Override
	public boolean markSupported() {
		return true;
	}
	
	int readBuf() throws IOException {
		if (buf.remaining() == 0) {
			if (markState == MarkState.SET_WITHIN_BUFFER)
				markState = MarkState.EXHAUSTED;
			
			buf.clear();
			int rc = sc.read(buf);
			if (rc > 0) {
				buf.flip();
				return rc;
			} else { 
				return -1;
			}
		}
		return buf.remaining();
	}
}
