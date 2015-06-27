package com.jhindin.midi;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class ChannelInputStream extends InputStream {
	FileChannel fc;
	ByteBuffer buf = ByteBuffer.allocate(1024);
	
	public ChannelInputStream(FileChannel fc) throws IOException {
		super();
		this.fc = fc;
		fc.read(buf);
		buf.flip();
	}

	@Override
	public int read() throws IOException {
		if (buf.remaining() == 0) {
			buf.clear();
			int rc = fc.read(buf);
			if (rc == -1)
				return -1;
			buf.flip();
		}
		
		return buf.get();
	}

	@Override
	public int read(byte[] b) throws IOException {
		return super.read(b, 0, b.length);
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		int ret = len;
		do {
			if (buf.remaining() == 0) {
				buf.clear();
				int rc = fc.read(buf);
				if (rc == -1)
					return -1;
				buf.flip();
			}
			int r = buf.remaining();
			int bytesToCopy = (r <= len) ? r : len;
			buf.get(b, off, bytesToCopy);
			off += bytesToCopy;
			len -= bytesToCopy;
		} while (len > 0);
		return ret;
	}

	@Override
	public long skip(long n) throws IOException {
		long pos = fc.position();
		fc.position(pos + n);
		return n;
	}

	@Override
	public void close() throws IOException {
		fc.close();
	}
	
	

}
