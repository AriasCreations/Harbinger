/**
 * CVS identifier:
 * <p>
 * $Id: ISRandomAccessIO.java,v 1.2 2001/04/09 16:58:15 grosbois Exp $
 * <p>
 * Class:                   ISRandomAccessIO
 * <p>
 * Description:             Turns an InsputStream into a read-only
 * RandomAccessIO, using buffering.
 * <p>
 * <p>
 * <p>
 * COPYRIGHT:
 * <p>
 * This software module was originally developed by Rapha�l Grosbois and
 * Diego Santa Cruz (Swiss Federal Institute of Technology-EPFL); Joel
 * Askel�f (Ericsson Radio Systems AB); and Bertrand Berthelot, David
 * Bouchard, F�lix Henry, Gerard Mozelle and Patrice Onno (Canon Research
 * Centre France S.A) in the course of development of the JPEG2000
 * standard as specified by ISO/IEC 15444 (JPEG 2000 Standard). This
 * software module is an implementation of a part of the JPEG 2000
 * Standard. Swiss Federal Institute of Technology-EPFL, Ericsson Radio
 * Systems AB and Canon Research Centre France S.A (collectively JJ2000
 * Partners) agree not to assert against ISO/IEC and users of the JPEG
 * 2000 Standard (Users) any of their rights under the copyright, not
 * including other intellectual property rights, for this software module
 * with respect to the usage by ISO/IEC and Users of this software module
 * or modifications thereof for use in hardware or software products
 * claiming conformance to the JPEG 2000 Standard. Those intending to use
 * this software module in hardware or software products are advised that
 * their use may infringe existing patents. The original developers of
 * this software module, JJ2000 Partners and ISO/IEC assume no liability
 * for use of this software module or modifications thereof. No license
 * or right to this software module is granted for non JPEG 2000 Standard
 * conforming products. JJ2000 Partners have full right to use this
 * software module for his/her own purpose, assign or donate this
 * software module to any third party and to inhibit third parties from
 * using this software module for non JPEG 2000 Standard conforming
 * products. This copyright notice must be included in all copies or
 * derivative works of this software module.
 * <p>
 * Copyright (c) 1999/2000 JJ2000 Partners.
 */
package dev.zontreck.harbinger.thirdparty.jj2000.j2k.util;

import dev.zontreck.harbinger.thirdparty.jj2000.j2k.io.EndianType;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.io.RandomAccessIO;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

/**
 * This class implements a wrapper to turn an InputStream into a RandomAccessIO.
 * To provide random access, the input data from the InputStream is cached in an
 * in-memory buffer. The in-memory buffer size can be limited to a specified
 * size. The data is read into the cache on a as needed basis, blocking only
 * when necessary.
 *
 * <p>
 * The cache grows automatically as necessary. However, if the data length is
 * known prior to the creation of a ISRandomAccessIO object, it is best to
 * specify that as the initial in-memory buffer size. That will minimize data
 * copying and multiple allocation.
 *
 * <p>
 * Multi-byte data is read in big-endian order. The in-memory buffer storage is
 * released when 'close()' is called. This class can only be used for data
 * input, not output. The wrapped InputStream is closed when all the input data
 * is cached or when 'close()' is called.
 *
 * <p>
 * If an out of memory condition is encountered when growing the in-memory
 * buffer an IOException is thrown instead of an OutOfMemoryError. The exception
 * message is "Out of memory to cache input data".
 *
 * <p>
 * This class is intended for use as a "quick and dirty" way to give network
 * connectivity to RandomAccessIO based classes. It is not intended as an
 * efficient means of implementing network connectivity. Doing such requires
 * reimplementing the RandomAccessIO based classes to directly use network
 * connections.
 *
 * <p>
 * This class does not use temporary files as buffers, because that would
 * preclude the use in unsigned applets.
 */
public class ISRandomAccessIO implements RandomAccessIO {
	/**
	 * The InputStream that is wrapped
	 */
	private InputStream is;

	/*
	 * Tha maximum size, in bytes, of the in memory buffer. The maximum size
	 * includes the EOF.
	 */
	private final int maxsize;

	/* The increment, in bytes, for the in-memory buffer size */
	private final int inc;

	/* The in-memory buffer to cache received data */
	private byte[] buf;

	/* The length of the already received data */
	private int len;

	/* The position of the next byte to be read from the in-memory buffer */
	private int pos;

	/*
	 * Flag to indicate if all the data has been received. That is, if the EOF
	 * has been reached.
	 */
	private boolean complete;

	/**
	 * Creates a new RandomAccessIO wrapper for the given InputStream 'is'. The
	 * internal cache buffer will have size 'size' and will increment by 'inc'
	 * each time it is needed. The maximum buffer size is limited to 'maxsize'.
	 *
	 * @param is      The input from where to get the data.
	 * @param size    The initial size for the cache buffer, in bytes.
	 * @param inc     The size increment for the cache buffer, in bytes.
	 * @param maxsize The maximum size for the cache buffer, in bytes.
	 */
	public ISRandomAccessIO(final InputStream is, int size, final int inc, int maxsize) {
		if (0 > size || 0 >= inc || 0 >= maxsize || null == is) {
			throw new IllegalArgumentException();
		}
		this.is = is;
		// Increase size by one to count in EOF
		if (Integer.MAX_VALUE > size) size++;
		this.buf = new byte[size];
		this.inc = inc;
		// The maximum size is one byte more, to allow reading the EOF.
		if (Integer.MAX_VALUE > maxsize) maxsize++;
		this.maxsize = maxsize;
		this.pos = 0;
		this.len = 0;
		this.complete = false;
	}

	/**
	 * Creates a new RandomAccessIO wrapper for the given InputStream 'is'. The
	 * internal cache buffer size and increment is to to 256 kB. The maximum
	 * buffer size is set to Integer.MAX_VALUE (2 GB).
	 *
	 * @param is The input from where to get the data.
	 */
	public ISRandomAccessIO(final InputStream is) {
		this(is, 1 << 18, 1 << 18, Integer.MAX_VALUE);
	}

	/**
	 * Grows the cache buffer by 'inc', upto a maximum of 'maxsize'. The buffer
	 * size will be increased by at least one byte, if no exception is thrown.
	 *
	 * @throws IOException If the maximum cache size is reached or if not enough
	 *                     memory is available to grow the buffer.
	 */
	private void growBuffer() throws IOException {
		final byte[] newbuf;
		int effinc; // effective increment

		effinc = this.inc;
		if (this.buf.length + effinc > this.maxsize)
			effinc = this.maxsize - this.buf.length;
		if (0 >= effinc) {
			throw new IOException("Reached maximum cache size (" + this.maxsize + ")");
		}
		try {
			newbuf = new byte[this.buf.length + this.inc];
		} catch (final OutOfMemoryError e) {
			throw new IOException("Out of memory to cache input data");
		}
		System.arraycopy(this.buf, 0, newbuf, 0, this.len);
		this.buf = newbuf;
	}

	/**
	 * Reads data from the wrapped InputStream and places it in the cache
	 * buffer. Reads all input data that will not cause it to block, but at
	 * least on byte is read (even if it blocks), unless EOF is reached. This
	 * method can not be called if EOF has been already reached (i.e. 'complete'
	 * is true). The wrapped InputStream is closed if the EOF is reached.
	 *
	 * @throws IOException An I/O error occurred, out of meory to grow cache or
	 *                     maximum cache size reached.
	 */
	private void readInput() throws IOException {
		int n;
		int k;

		if (this.complete) {
			throw new IllegalArgumentException("Already reached EOF");
		}
		n = this.is.available(); /* how much can we read without blocking? */
		if (0 == n)
			n = 1; /* read at least one byte (even if it blocks) */
		while (this.len + n > this.buf.length) { /* Ensure buffer size */
			this.growBuffer();
		}
		/* Read the data. Loop to be sure that we do read 'n' bytes */
		do {
			k = this.is.read(this.buf, this.len, n);
			if (0 < k) { /* Some data was read */
				this.len += k;
				n -= k;
			}
		} while (0 < n && 0 < k);
		if (0 >= k) { /* we reached EOF */
			this.complete = true;
			this.is.close();
			this.is = null;
		}
	}

	/**
	 * Closes this object for reading as well as the wrapped InputStream, if not
	 * already closed. The memory used by the cache is released.
	 *
	 * @throws IOException If an I/O error occurs while closing the underlying
	 *                     InputStream.
	 */
	@Override
	public void close() throws IOException {
		this.buf = null;
		if (!this.complete) {
			this.is.close();
			this.is = null;
		}
	}

	/**
	 * Returns the current position in the stream, which is the position from
	 * where the next byte of data would be read. The first byte in the stream
	 * is in position 0.
	 *
	 * @throws IOException If an I/O error occurred.
	 */
	@Override
	public int getPos() throws IOException {
		return this.pos;
	}

	/**
	 * Moves the current position for the next read operation to offset. The
	 * offset is measured from the beginning of the stream. If the offset is set
	 * beyond the currently cached data, the missing data will be read only when
	 * a read operation is performed. Setting the offset beyond the end of the
	 * data will cause an EOFException only if the data length is currently
	 * known, otherwise an IOException will occur when a read operation is
	 * attempted at that position.
	 *
	 * @param off The offset where to move to.
	 * @throws EOFException If seeking beyond EOF and the data length is known.
	 * @throws IOException  If an I/O error occurred.
	 */
	@Override
	public void seek(final int off) throws IOException {
		if (this.complete) { /* we know the length, check seek is within length */
			if (off > this.len) {
				throw new EOFException();
			}
		}
		this.pos = off;
	}

	/**
	 * Returns the length of the stream. This will cause all the data to be
	 * read. This method will block until all the data is read, which can be
	 * lengthy across the network.
	 *
	 * @return The length of the stream, in bytes.
	 * @throws IOException If an I/O error occurred.
	 */
	@Override
	public int length() throws IOException {
		while (!this.complete) { /* read until we reach EOF */
			this.readInput();
		}
		return this.len;
	}

	/**
	 * Reads a byte of data from the stream.
	 *
	 * @return The byte read, as an int in the range [0-255].
	 * @throws EOFException If the end-of file was reached.
	 * @throws IOException  If an I/O error occurred.
	 */
	@Override
	public int read() throws IOException {
		if (this.pos < this.len) { // common, fast case
			int i = 0xFF & buf[this.pos];
			this.pos++;
			return i;
		}
		// general case
		while (!this.complete && this.pos >= this.len) {
			this.readInput();
		}
		if (this.pos == this.len) {
			throw new EOFException();
		} else if (this.pos > this.len) {
			throw new IOException("Position beyond EOF");
		}
		int i = 0xFF & buf[this.pos];
		this.pos++;
		return i;
	}

	/**
	 * Reads 'len' bytes of data from this file into an array of bytes. This
	 * method reads repeatedly from the stream until all the bytes are read.
	 * This method blocks until all the bytes are read, the end of the stream is
	 * detected, or an exception is thrown.
	 *
	 * @param b   The buffer into which the data is to be read. It must be long
	 *            enough.
	 * @param off The index in 'b' where to place the first byte read.
	 * @param n The number of bytes to read.
	 * @throws EOFException If the end-of file was reached before getting all the
	 *                      necessary data.
	 * @throws IOException  If an I/O error occurred.
	 */
	@Override
	public void readFully(final byte[] b, final int off, final int n) throws IOException {
		if (this.pos + n <= this.len) { // common, fast case
			System.arraycopy(this.buf, this.pos, b, off, n);
			this.pos += n;
			return;
		}
		// general case
		while (!this.complete && this.pos + n > this.len) {
			this.readInput();
		}
		if (this.pos + n > this.len) {
			throw new EOFException();
		}
		System.arraycopy(this.buf, this.pos, b, off, n);
		this.pos += n;
	}

	/**
	 * Returns the endianess (i.e., byte ordering) of multi-byte I/O operations.
	 * Always EndianType.BIG_ENDIAN since this class implements only big-endian.
	 *
	 * @return Always EndianType.BIG_ENDIAN.
	 * @see EndianType
	 */
	@Override
	public int getByteOrdering() {
		return EndianType.BIG_ENDIAN;
	}

	/**
	 * Reads a signed byte (8 bit) from the input.
	 *
	 * @return The next byte-aligned signed byte (8 bit) from the input.
	 * @throws EOFException If the end-of file was reached before getting all the
	 *                      necessary data.
	 * @throws IOException  If an I/O error occurred.
	 */
	@Override
	public byte readByte() throws IOException {
		if (this.pos < this.len) { // common, fast case
			byte b = buf[this.pos];
			this.pos++;
			return b;
		}
		// general case
		return (byte) this.read();
	}

	/**
	 * Reads an unsigned byte (8 bit) from the input.
	 *
	 * @return The next byte-aligned unsigned byte (8 bit) from the input.
	 * @throws EOFException If the end-of file was reached before getting all the
	 *                      necessary data.
	 * @throws IOException  If an I/O error occurred.
	 */
	@Override
	public int readUnsignedByte() throws IOException {
		if (this.pos < this.len) { // common, fast case
			int i = 0xFF & buf[this.pos];
			this.pos++;
			return i;
		}
		// general case
		return this.read();
	}

	/**
	 * Reads a signed short (16 bit) from the input.
	 *
	 * @return The next byte-aligned signed short (16 bit) from the input.
	 * @throws EOFException If the end-of file was reached before getting all the
	 *                      necessary data.
	 * @throws IOException  If an I/O error occurred.
	 */
	@Override
	public short readShort() throws IOException {
		if (this.pos + 1 < this.len) { // common, fast case
			short i = (short) ((buf[pos++] << 8) | (0xFF & buf[this.pos]));
			this.pos++;
			return i;
		}
		// general case
		return (short) ((this.read() << 8) | this.read());
	}

	/**
	 * Reads an unsigned short (16 bit) from the input.
	 *
	 * @return The next byte-aligned unsigned short (16 bit) from the input.
	 * @throws EOFException If the end-of file was reached before getting all the
	 *                      necessary data.
	 * @throws IOException  If an I/O error occurred.
	 */
	@Override
	public int readUnsignedShort() throws IOException {
		if (this.pos + 1 < this.len) { // common, fast case
			int i = ((0xFF & buf[pos++]) << 8) | (0xFF & buf[this.pos]);
			this.pos++;
			return i;
		}
		// general case
		return (this.read() << 8) | this.read();
	}

	/**
	 * Reads a signed int (32 bit) from the input.
	 *
	 * @return The next byte-aligned signed int (32 bit) from the input.
	 * @throws EOFException If the end-of file was reached before getting all the
	 *                      necessary data.
	 * @throws IOException  If an I/O error occurred.
	 */
	@Override
	public int readInt() throws IOException {
		if (this.pos + 3 < this.len) { // common, fast case
			int i = ((buf[pos++] << 24) | ((0xFF & buf[pos++]) << 16) | ((0xFF & buf[pos++]) << 8) | (0xFF & buf[this.pos]));
			this.pos++;
			return i;
		}
		// general case
		return (this.read() << 24) | (this.read() << 16) | (this.read() << 8) | this.read();
	}

	/**
	 * Reads a unsigned int (32 bit) from the input.
	 *
	 * @return The next byte-aligned unsigned int (32 bit) from the input.
	 * @throws EOFException If the end-of file was reached before getting all the
	 *                      necessary data.
	 * @throws IOException  If an I/O error occurred.
	 */
	@Override
	public long readUnsignedInt() throws IOException {
		if (this.pos + 3 < this.len) { // common, fast case
			long l = (0xFFFFFFFFL & ((buf[pos++] << 24) | ((0xFF & buf[pos++]) << 16) | ((0xFF & buf[pos++]) << 8) | (0xFF & buf[this.pos])));
			this.pos++;
			return l;
		}
		// general case
		return (0xFFFFFFFFL & (((long) this.read() << 24) | ((long) this.read() << 16) | ((long) this.read() << 8) | this.read()));
	}

	/**
	 * Reads a signed long (64 bit) from the input.
	 *
	 * @return The next byte-aligned signed long (64 bit) from the input.
	 * @throws EOFException If the end-of file was reached before getting all the
	 *                      necessary data.
	 * @throws IOException  If an I/O error occurred.
	 */
	@Override
	public long readLong() throws IOException {
		if (this.pos + 7 < this.len) { // common, fast case
			long l = (((long) buf[pos++] << 56) | ((long) (0xFF & buf[pos++]) << 48) | ((long) (0xFF & buf[pos++]) << 40) | ((long) (0xFF & buf[pos++]) << 32) | ((long) (0xFF & buf[pos++]) << 24) | ((long) (0xFF & buf[pos++]) << 16) | ((long) (0xFF & buf[pos++]) << 8) | (0xFF & buf[this.pos]));
			this.pos++;
			return l;
		}
		// general case
		return (((long) this.read() << 56) | ((long) this.read() << 48) | ((long) this.read() << 40) | ((long) this.read() << 32) | ((long) this.read() << 24) | ((long) this.read() << 16) | ((long) this.read() << 8) | this.read());
	}

	/**
	 * Reads an IEEE single precision (i.e., 32 bit) floating-point number from
	 * the input.
	 *
	 * @return The next byte-aligned IEEE float (32 bit) from the input.
	 * @throws EOFException If the end-of file was reached before getting all the
	 *                      necessary data.
	 * @throws IOException  If an I/O error occurred.
	 */
	@Override
	public float readFloat() throws IOException {
		if (this.pos + 3 < this.len) { // common, fast case
			float v = Float.intBitsToFloat((buf[pos++] << 24) | ((0xFF & buf[pos++]) << 16) | ((0xFF & buf[pos++]) << 8) | (0xFF & buf[this.pos]));
			this.pos++;
			return v;
		}
		// general case
		return Float.intBitsToFloat((this.read() << 24) | (this.read() << 16) | (this.read() << 8) | this.read());
	}

	/**
	 * Reads an IEEE double precision (i.e., 64 bit) floating-point number from
	 * the input.
	 *
	 * @return The next byte-aligned IEEE double (64 bit) from the input.
	 * @throws EOFException If the end-of file was reached before getting all the
	 *                      necessary data.
	 * @throws IOException  If an I/O error occurred.
	 */
	@Override
	public double readDouble() throws IOException {
		if (this.pos + 7 < this.len) { // common, fast case
			double v = Double.longBitsToDouble(((long) buf[pos++] << 56) | ((long) (0xFF & buf[pos++]) << 48) | ((long) (0xFF & buf[pos++]) << 40) | ((long) (0xFF & buf[pos++]) << 32) | ((long) (0xFF & buf[pos++]) << 24) | ((long) (0xFF & buf[pos++]) << 16) | ((long) (0xFF & buf[pos++]) << 8) | (0xFF & buf[this.pos]));
			this.pos++;
			return v;
		}
		// general case
		return Double.longBitsToDouble(((long) this.read() << 56) | ((long) this.read() << 48) | ((long) this.read() << 40) | ((long) this.read() << 32) | ((long) this.read() << 24) | ((long) this.read() << 16) | ((long) this.read() << 8) | this.read());
	}

	/**
	 * Skips 'n' bytes from the input.
	 *
	 * @param n The number of bytes to skip
	 * @return Always n.
	 * @throws EOFException If the end-of file was reached before all the bytes could
	 *                      be skipped.
	 * @throws IOException  If an I/O error occurred.
	 */
	@Override
	public int skipBytes(final int n) throws IOException {
		if (this.complete) { /* we know the length, check skip is within length */
			if (this.pos + n > this.len) {
				throw new EOFException();
			}
		}
		this.pos += n;
		return n;
	}

	/**
	 * Does nothing since this class does not implement data output.
	 */
	@Override
	public void flush() { /* no-op */
	}

	/**
	 * Throws an IOException since this class does not implement data output.
	 */
	@Override
	public void write(final int b) throws IOException {
		throw new IOException("read-only");
	}

	/**
	 * Throws an IOException since this class does not implement data output.
	 */
	@Override
	public void writeByte(final int v) throws IOException {
		throw new IOException("read-only");
	}

	/**
	 * Throws an IOException since this class does not implement data output.
	 */
	@Override
	public void writeShort(final int v) throws IOException {
		throw new IOException("read-only");
	}

	/**
	 * Throws an IOException since this class does not implement data output.
	 */
	@Override
	public void writeInt(final int v) throws IOException {
		throw new IOException("read-only");
	}

	/**
	 * Throws an IOException since this class does not implement data output.
	 */
	@Override
	public void writeLong(final long v) throws IOException {
		throw new IOException("read-only");
	}

	/**
	 * Throws an IOException since this class does not implement data output.
	 */
	@Override
	public void writeFloat(final float v) throws IOException {
		throw new IOException("read-only");
	}

	/**
	 * Throws an IOException since this class does not implement data output.
	 */
	@Override
	public void writeDouble(final double v) throws IOException {
		throw new IOException("read-only");
	}

	/**
	 * Throws an IOException since this class does not implement data output.
	 */
	@Override
	public void write(final byte[] b) throws IOException {
		throw new IOException("read-only");
	}

	/**
	 * Throws an IOException since this class does not implement data output.
	 */
	@Override
	public void write(final byte[] b, final int off, final int len) throws IOException {
		throw new IOException("read-only");
	}
}
