package dev.zontreck.harbinger.thirdparty.jj2000.j2k.util;

import dev.zontreck.harbinger.thirdparty.jj2000.j2k.io.EndianType;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.io.RandomAccessIO;

import java.io.EOFException;
import java.io.IOException;
import java.io.OutputStream;

public class OSRandomAccessIO implements RandomAccessIO {
	/*
	 * Tha maximum size, in bytes, of the in memory buffer. The maximum size
	 * includes the EOF.
	 */
	private final int maxsize;

	/* The in-memory buffer to cache received data */
	private byte[] buf;

	/* The position of the next byte to be read/write in the memory buffer */
	private int pos;

	/* The position beyond the maximum written data */
	private int length;

	public OSRandomAccessIO ( ) {
		this ( 1 << 18 , Integer.MAX_VALUE );
	}

	public OSRandomAccessIO ( int size , int maxsize ) {
		if ( 0 > size || 0 >= maxsize ) {
			throw new IllegalArgumentException ( );
		}
		// Increase size by one to count in EOF
		if ( Integer.MAX_VALUE > size )
			size++;
		this.buf = new byte[ size ];
		// The maximum size is one byte more, to allow reading the EOF.
		if ( Integer.MAX_VALUE > maxsize )
			maxsize++;
		this.maxsize = maxsize;
		this.pos = 0;
		this.length = 0;
	}

	public OSRandomAccessIO ( final byte[] data , int maxsize ) {
		if ( null == data ) {
			throw new IllegalArgumentException ( );
		}

		this.buf = data;
		// The maximum size is one byte more, to allow reading the EOF.
		if ( Integer.MAX_VALUE > maxsize )
			maxsize++;
		this.maxsize = maxsize;
		this.pos = 0;
		this.length = data.length;
	}

	/**
	 * Checks if the cache buffer can accept 'inc' bytes and if that is not the case, it grows
	 * the cache buffer by doubling the buffer size, upto a maximum of 'maxsize', making sure
	 * that at least 'inc' bytes are available after the growing of the buffer.
	 *
	 * @throws IOException If the maximum cache size is reached or if not enough
	 *                     memory is available to grow the buffer.
	 */
	private void growBuffer ( final int inc ) throws IOException {
		if ( this.pos + inc > this.buf.length ) {
			final byte[] newbuf;
			int effinc = Math.max ( this.buf.length << 1 , inc );
			if ( this.buf.length + effinc > this.maxsize )
				effinc = this.maxsize - this.buf.length;
			if ( effinc <= inc ) {
				throw new IOException ( "Reached maximum cache size (" + this.maxsize + ")" );
			}
			try {
				newbuf = new byte[ this.buf.length + effinc ];
			} catch ( final OutOfMemoryError e ) {
				throw new IOException ( "Out of memory to cache input data" );
			}
			System.arraycopy ( this.buf , 0 , newbuf , 0 , this.length );
			this.buf = newbuf;
		}

		if ( this.pos + inc > this.length ) {
			this.length = this.pos + inc;
		}
	}

	/**
	 * Reads a signed byte (8 bit) from the internal buffer.
	 *
	 * @return The next byte-aligned signed byte (8 bit) from the input.
	 * @throws EOFException If the end-of file was reached before getting
	 *                      all the necessary data.
	 */
	@Override
	public byte readByte ( ) throws EOFException {
		if ( this.pos >= this.length ) {
			throw new EOFException ( );
		}
		byte b = buf[ this.pos ];
		this.pos++;
		return b;
	}

	/**
	 * Reads an unsigned byte (8 bit) from the internal buffer.
	 *
	 * @return The next byte-aligned unsigned byte (8 bit) from the input.
	 * @throws EOFException If the end-of file was reached before getting
	 *                      all the necessary data.
	 */
	@Override
	public int readUnsignedByte ( ) throws EOFException {
		if ( this.pos >= this.length ) {
			throw new EOFException ( );
		}
		int i = 0xFF & buf[ this.pos ];
		this.pos++;
		return i;
	}

	/**
	 * Reads a signed short (16 bit) from the internal buffer.
	 *
	 * @return The next byte-aligned signed short (16 bit) from the input.
	 * @throws EOFException If the end-of file was reached before getting
	 *                      all the necessary data.
	 */
	@Override
	public short readShort ( ) throws EOFException {
		if ( this.pos + 1 >= this.length ) {
			throw new EOFException ( );
		}
		short i = ( short ) ( ( buf[ pos ] << 8 ) | ( 0xFF & buf[ this.pos ] ) );
		pos++;
		this.pos++;
		return i;
	}

	/**
	 * Reads an unsigned short (16 bit) from the internal buffer.
	 *
	 * @return The next byte-aligned unsigned short (16 bit) from the input.
	 * @throws EOFException If the end-of file was reached before getting
	 *                      all the necessary data.
	 */
	@Override
	public int readUnsignedShort ( ) throws EOFException {
		if ( this.pos + 1 >= this.length ) {
			throw new EOFException ( );
		}
		int i = ( ( 0xFF & buf[ pos ] ) << 8 ) | ( 0xFF & buf[ this.pos ] );
		pos++;
		this.pos++;
		return i;
	}

	/**
	 * Reads a signed int (32 bit) from the internal buffer.
	 *
	 * @return The next byte-aligned signed int (32 bit) from the input.
	 * @throws EOFException If the end-of file was reached before getting
	 *                      all the necessary data.
	 */
	@Override
	public int readInt ( ) throws EOFException {
		if ( this.pos + 3 >= this.length ) {
			throw new EOFException ( );
		}
		int i = ( ( buf[ pos ] << 24 ) | ( ( 0xFF & buf[ pos ] ) << 16 ) | ( ( 0xFF & buf[ pos ] ) << 8 ) | ( 0xFF & buf[ this.pos ] ) );
		pos++;
		pos++;
		pos++;
		this.pos++;
		return i;
	}

	/**
	 * Reads a unsigned int (32 bit) from the internal buffer.
	 *
	 * @return The next byte-aligned unsigned int (32 bit) from the input.
	 * @throws EOFException If the end-of file was reached before getting
	 *                      all the necessary data.
	 */
	@Override
	public long readUnsignedInt ( ) throws EOFException {
		if ( this.pos + 3 >= this.length ) {
			throw new EOFException ( );
		}
		long l = ( 0xFFFFFFFFL & ( ( buf[ pos ] << 24 ) | ( ( 0xFF & buf[ pos ] ) << 16 ) | ( ( 0xFF & buf[ pos ] ) << 8 ) | ( 0xFF & buf[ this.pos ] ) ) );
		pos++;
		pos++;
		pos++;
		this.pos++;
		return l;
	}

	/**
	 * Reads a signed long (64 bit) from the internal buffer.
	 *
	 * @return The next byte-aligned signed long (64 bit) from the input.
	 * @throws EOFException If the end-of file was reached before getting
	 *                      all the necessary data.
	 */
	@Override
	public long readLong ( ) throws EOFException {
		if ( this.pos + 7 >= this.length ) {
			throw new EOFException ( );
		}
		long l = (
				( ( long ) buf[ pos ] << 56 ) | ( ( long ) ( 0xFF & buf[ pos ] ) << 48 ) | ( ( long ) ( 0xFF & buf[ pos ] ) << 40 )
						| ( ( long ) ( 0xFF & buf[ pos ] ) << 32 ) | ( ( long ) ( 0xFF & buf[ pos ] ) << 24 )
						| ( ( long ) ( 0xFF & buf[ pos ] ) << 16 ) | ( ( long ) ( 0xFF & buf[ pos ] ) << 8 ) | ( 0xFF & buf[ this.pos ] )
		);
		pos++;
		pos++;
		pos++;
		pos++;
		pos++;
		pos++;
		pos++;
		this.pos++;
		return l;
	}

	/**
	 * Reads an IEEE single precision (i.e., 32 bit) floating-point number from
	 * the internal buffer.
	 *
	 * @return The next byte-aligned IEEE float (32 bit) from the input.
	 * @throws EOFException If the end-of file was reached before getting
	 *                      all the necessary data.
	 */
	@Override
	public float readFloat ( ) throws EOFException {
		return Float.intBitsToFloat ( this.readInt ( ) );
	}

	/**
	 * Reads an IEEE double precision (i.e., 64 bit) floating-point number from
	 * the input.
	 *
	 * @return The next byte-aligned IEEE double (64 bit) from the input.
	 * @throws EOFException If the end-of file was reached before getting
	 *                      all the necessary data.
	 */
	@Override
	public double readDouble ( ) throws EOFException {
		return Double.longBitsToDouble ( this.readLong ( ) );
	}

	/**
	 * Returns the endianess (i.e., byte ordering) of multi-byte I/O operations.
	 * Always EndianType.BIG_ENDIAN since this class implements only big-endian.
	 *
	 * @return Always EndianType.BIG_ENDIAN.
	 * @see EndianType
	 */
	@Override
	public int getByteOrdering ( ) {
		return EndianType.BIG_ENDIAN;
	}

	/**
	 * Skips 'n' bytes from the input.
	 *
	 * @param n The number of bytes to skip
	 * @return Always n.
	 * @throws IOException If an I/O error occurred.
	 */
	@Override
	public int skipBytes ( final int n ) throws IOException {
		this.growBuffer ( n );
		this.pos += n;
		return n;
	}

	/**
	 * Write a signed byte (8 bit) to the output.
	 *
	 * @param v next byte-aligned signed byte (8 bit).
	 * @throws IOException If an I/O error occurred.
	 */
	@Override
	public void writeByte ( final int v ) throws IOException {
		this.growBuffer ( 1 );
		this.buf[ this.pos ] = ( byte ) v;
		this.pos++;
	}

	/**
	 * Write a signed short (16 bit) to the output.
	 *
	 * @param v next byte-aligned signed short (16 bit).
	 * @throws IOException If an I/O error occurred.
	 */
	@Override
	public void writeShort ( final int v ) throws IOException {
		this.growBuffer ( 2 );
		this.buf[ this.pos ] = ( byte ) ( v >> 8 );
		this.pos++;
		this.buf[ this.pos ] = ( byte ) ( 0xFF & v );
		this.pos++;
	}

	/**
	 * Write a signed int (32 bit) to the output.
	 *
	 * @param v next byte-aligned signed int (32 bit).
	 * @throws IOException If an I/O error occurred.
	 */
	@Override
	public void writeInt ( final int v ) throws IOException {
		this.growBuffer ( 4 );
		this.buf[ this.pos ] = ( byte ) ( 0xFF & ( v >> 24 ) );
		this.pos++;
		this.buf[ this.pos ] = ( byte ) ( 0xFF & ( v >> 16 ) );
		this.pos++;
		this.buf[ this.pos ] = ( byte ) ( 0xFF & ( v >> 8 ) );
		this.pos++;
		this.buf[ this.pos ] = ( byte ) ( 0xFF & v );
		this.pos++;
	}

	/**
	 * Write a signed long (64 bit) to the output.
	 *
	 * @param v next byte-aligned signed long (64 bit).
	 * @throws IOException If an I/O error occurred.
	 */
	@Override
	public void writeLong ( final long v ) throws IOException {
		this.growBuffer ( 8 );
		this.buf[ this.pos ] = ( byte ) ( 0xFF & ( v >> 56 ) );
		this.pos++;
		this.buf[ this.pos ] = ( byte ) ( 0xFF & ( v >> 48 ) );
		this.pos++;
		this.buf[ this.pos ] = ( byte ) ( 0xFF & ( v >> 40 ) );
		this.pos++;
		this.buf[ this.pos ] = ( byte ) ( 0xFF & ( v >> 32 ) );
		this.pos++;
		this.buf[ this.pos ] = ( byte ) ( 0xFF & ( v >> 24 ) );
		this.pos++;
		this.buf[ this.pos ] = ( byte ) ( 0xFF & ( v >> 16 ) );
		this.pos++;
		this.buf[ this.pos ] = ( byte ) ( 0xFF & ( v >> 8 ) );
		this.pos++;
		this.buf[ this.pos ] = ( byte ) ( 0xFF & v );
		this.pos++;
	}

	/**
	 * Write a 32 bit floating point to the output.
	 *
	 * @param v next byte-aligned signed long (64 bit).
	 * @throws IOException If an I/O error occurred.
	 */
	@Override
	public void writeFloat ( final float v ) throws IOException {
		this.writeInt ( Float.floatToIntBits ( v ) );
	}

	/**
	 * Write a 64 bit floating point to the output.
	 *
	 * @param v next byte-aligned signed long (64 bit).
	 * @throws IOException If an I/O error occurred.
	 */
	@Override
	public void writeDouble ( final double v ) throws IOException {
		this.writeLong ( Double.doubleToLongBits ( v ) );
	}

	/**
	 * Flush the output. Does nothing since this class stores all the data internally.
	 */
	@Override
	public void flush ( ) {
	}

	/**
	 * Closes this object for reading and writing. The memory used by the cache is released.
	 */
	@Override
	public void close ( ) {
		this.buf = null;
	}

	/**
	 * Returns the current position in the stream, which is the position from
	 * where the next byte of data would be read or written to. The first byte
	 * in the stream is in position 0.
	 */
	@Override
	public int getPos ( ) {
		return this.pos;
	}

	/**
	 * Returns the current length of the stream, that is the position just beyond
	 * the furthest byte written to it so far.
	 *
	 * @return The length of the stream, in bytes.
	 */
	@Override
	public int length ( ) {
		return this.length;
	}

	/**
	 * Moves the current position for the next read/write operation to offset. The
	 * offset is measured from the beginning of the stream. If the offset is set
	 * beyond the currently cached data, the missing data will be uninitialized.
	 * Setting the offset beyond the end of the internal buffer will cause this
	 * buffer to be grown accordingly.
	 *
	 * @param off The offset where to move to.
	 * @throws IOException If an I/O error occurred.
	 */
	@Override
	public void seek ( final int off ) throws IOException {
		if ( off > this.pos )
			this.growBuffer ( off - this.pos );
		this.pos = off;
	}

	/**
	 * Reads one byte of data from the internal buffer.
	 *
	 * @return the byte read
	 * @throws EOFException If the end-of file was reached before getting all the
	 *                      necessary data.
	 */
	@Override
	public int read ( ) throws IOException {
		if ( this.pos >= this.length ) {
			throw new EOFException ( );
		}
		int i = 0xFF & buf[ this.pos ];
		this.pos++;
		return i;
	}

	/**
	 * Reads 'n' bytes of data from the internal buffer into an array of bytes.
	 *
	 * @param b   The buffer into which the data is to be read. It must be long
	 *            enough.
	 * @param off The index in 'b' where to place the first byte read.
	 * @param len The number of bytes to read.
	 * @throws EOFException     If the end-of currently defined data was reached
	 *                          before gettingall the requested data.
	 * @throws IOException      If an I/O error occurred.
	 * @throws RuntimeException If an error occurred during array copy.
	 */
	@Override
	public void readFully ( final byte[] b , final int off , final int len ) throws IOException, RuntimeException {
		if ( this.pos + len > this.length ) {
			throw new EOFException ( );
		}
		System.arraycopy ( this.buf , this.pos , b , off , len );
		this.pos += len;
	}

	/**
	 * Same as writeByte()
	 */
	@Override
	public void write ( final int b ) throws IOException {
		this.growBuffer ( 1 );
		this.buf[ this.pos ] = ( byte ) b;
		this.pos++;
	}

	/**
	 * Write a byte array to the internal buffer.
	 *
	 * @param b The byte array buffer
	 * @throws IOException      If an I/O error occurred.
	 * @throws RuntimeException If an error occurred during array copy.
	 */
	@Override
	public void write ( final byte[] b ) throws IOException, RuntimeException {
		this.growBuffer ( b.length );
		System.arraycopy ( b , 0 , this.buf , this.pos , b.length );
	}

	/**
	 * Write a byte array to the internal buffer.
	 *
	 * @param b   The byte array buffer
	 * @param off The offset into the byte array buffer from which to start
	 * @param len The number of bytes to copy to the internal buffer
	 * @throws IOException      If an I/O error occurred.
	 * @throws RuntimeException If an error occurred during array copy.
	 */
	@Override
	public void write ( final byte[] b , final int off , final int len ) throws IOException, RuntimeException {
		this.growBuffer ( len );
		System.arraycopy ( b , off , this.buf , this.pos , len );
	}

	/**
	 * Write the entire output buffer to the output stream
	 *
	 * @param b The byte array buffer
	 * @throws IOException If an I/O error occurred.
	 */
	public int writeTo ( final OutputStream os ) throws IOException {
		os.write ( this.buf , 0 , this.length );
		return this.length;
	}

	public byte[] toByteArray ( ) {
		return this.buf;
	}

}
