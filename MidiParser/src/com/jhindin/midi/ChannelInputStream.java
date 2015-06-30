package com.jhindin.midi;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;

public class ChannelInputStream extends InputStream {
	SeekableByteChannel sc;
	ByteBuffer buf = ByteBuffer.allocate(1024);
	
	enum  MarkState { CLEAR, SET_WITHIN_BUFFER, SET_SAVED,
		RESET_WITHIN_BUFFER, RESET_SAVED, EXHAUSTED };
	MarkState markState = MarkState.CLEAR;
	
	byte saved[];
	int saveReadPos, savedWritePos;
	
	
	public ChannelInputStream(SeekableByteChannel sc) throws IOException {
		super();
		this.sc = sc;
		sc.read(buf);
		buf.flip();
	}

	@Override
	public int read() throws IOException {
		int rc;
		byte b;
		
		switch (markState) {
		case CLEAR:
		case RESET_WITHIN_BUFFER:
		case SET_WITHIN_BUFFER:
		case EXHAUSTED:
			rc = readBuf();
			if (rc < 0)
				return -1;
			
			return buf.get() & 0xff;
		case SET_SAVED:
			rc = readBuf();
			if (rc < 0)
				return -1;
			
			b = buf.get();
			saved[savedWritePos++] = b;
			if (savedWritePos == saved.length) {
				saved = null;
				markState = MarkState.EXHAUSTED;
			}
			return b & 0xff;
		case RESET_SAVED:
			b = saved[saveReadPos++];
			if (saveReadPos == savedWritePos) {
				saved = null;
				markState = MarkState.CLEAR;
			}
			return b & 0xff;
		}
		throw new IOException("Invalid internal state " + markState);
	}

	@Override
	public int read(byte[] b) throws IOException {
		return read(b, 0, b.length);
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		switch (markState) {
		case CLEAR:
		case RESET_WITHIN_BUFFER:
		case SET_WITHIN_BUFFER:
		case EXHAUSTED:
			int ret = len;
			do {
				int rc = readBuf();
				int bytesToCopy = (rc <= len) ? rc : len;
				buf.get(b, off, bytesToCopy);
				off += bytesToCopy;
				len -= bytesToCopy;
			} while (len > 0);
			return ret;
		case SET_SAVED:
			return -1;
		case RESET_SAVED:
			return -1;
		}
		throw new IOException("Invalid internal state " + markState);
	}

	@Override
	public long skip(long n) throws IOException {
		long pos = sc.position();
		sc.position(pos + n);
		buf.clear();
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
			markState = MarkState.SET_SAVED;
			saved = new byte[readlimit];
			saveReadPos = savedWritePos = 0;;
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
		case RESET_WITHIN_BUFFER:
			buf.reset();
			break;
		case SET_SAVED:
		case RESET_SAVED:
			saveReadPos = 0;
			break;
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
