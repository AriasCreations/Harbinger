/**
 * Copyright (c) 2009-2017, Frederick Martian
 * All rights reserved.
 * <p>
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * <p>
 * - Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * - Neither the name of the openmetaverse.org or dev.zontreck.harbinger.thirdparty.libomv-java project nor the
 * names of its contributors may be used to endorse or promote products derived
 * from this software without specific prior written permission.
 * <p>
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package dev.zontreck.harbinger.thirdparty.libomv.utils;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

public class PushbackInputStream extends FilterInputStream {
	/**
	 * The pushback buffer.
	 */
	private byte[] buf;

	/**
	 * The position within the pushback buffer from which the next byte will be
	 * read. When the buffer is empty, {@code bufSpace} is equal to
	 * {@code buf.length}; when the buffer is full, {@code bufSpace} is
	 * equal to zero.
	 */
	private int bufStart;
	private int bufEnd;

	/**
	 * The number of bytes read so far
	 */
	private long bytesRead;

	/**
	 * Constructs a new {@code PushbackInputStream} with {@code in} as source stream.
	 * The size of the pushback buffer is set to 1.
	 *
	 * @param in the source stream.
	 */
	public PushbackInputStream ( final InputStream in ) {
		this ( in , 0 , 1 );
	}

	/**
	 * Constructs a new {@code PushbackInputStream} with {@code in} as source stream.
	 * The size of the pushback buffer is set to 1.
	 *
	 * @param in     the source stream.
	 * @param offset the offset in bytes into the original source data
	 * @throws IllegalArgumentException if {@code offset} is negative..
	 */
	public PushbackInputStream ( final InputStream in , final int offset ) {
		this ( in , offset , 1 );
	}

	/**
	 * Constructs a new {@code PushbackInputStream} with {@code in} as source stream.
	 * The size of the pushback buffer is set to {@code size}.
	 *
	 * @param in     the source stream.
	 * @param offset the offset in bytes into the original source data
	 * @param size   the size of the pushback buffer.
	 * @throws IllegalArgumentException if {@code size} is zero or negative or if {@code offset} is negative..
	 */
	public PushbackInputStream ( final InputStream in , final int offset , final int size ) {
		super ( in );
		if ( 0 >= size ) {
			throw new IllegalArgumentException ( "size <= 0" );
		}
		if ( 0 > offset ) {
			throw new IllegalArgumentException ( "offset < 0" );
		}
		buf = new byte[ size ];
		bufStart = 0;
		bufEnd = 0;
		bytesRead = offset;
	}

	public long getBytePosition ( ) {
		return this.bytesRead;
	}

	@Override
	public int available ( ) throws IOException {
		if ( null == buf )
			throw new IOException ( "Stream closed" );

		return ( this.bufEnd - this.bufStart ) + super.available ( );
	}

	/**
	 * Closes this input stream and releases any system resources associated
	 * with the stream.
	 *
	 * @throws IOException if an error occurs while closing this reader.
	 */
	@Override
	public synchronized void close ( ) throws IOException {
		if ( null == buf )
			return;
		super.close ( );
		this.in = null;
		this.buf = null;
	}

	/**
	 * Indicates whether this reader supports the {@code mark(int)} and
	 * {@code reset()} methods. {@code PushbackReader} does not support them, so
	 * it returns {@code false}.
	 *
	 * @return always {@code false}.
	 * @see #mark(int)
	 * @see #reset()
	 */
	@Override
	public boolean markSupported ( ) {
		return false;
	}

	/**
	 * Marks the current position in this stream. Setting a mark is not
	 * supported in this class; this implementation always throws an
	 * {@code IOException}.
	 *
	 * @param readLimit the number of character that can be read from this reader
	 *                  before the mark is invalidated; this parameter is ignored.
	 */
	@Override
	public synchronized void mark ( final int readLimit ) {
	}

	/**
	 * Reads a single byte from this stream and returns it as an integer.
	 * Returns -1 if the end of the reader has been reached. If the pushback
	 * buffer does not contain any available characters then a character from
	 * the source reader is returned. Blocks until one character has been read,
	 * the end of the source reader is detected or an exception is thrown.
	 *
	 * @return the byte read or -1 if the end of the source reader has been
	 * reached.
	 * @throws IOException if this stream is closed or an I/O error occurs while reading
	 *                     from this stream.
	 */
	@Override
	public int read ( ) throws IOException {
		if ( null == buf )
			throw new IOException ( "Stream closed" );

		if ( this.bufEnd > this.bufStart ) {
			this.bytesRead++;
			int i = buf[ this.bufStart ] & 0xff;
			this.bufStart++;
			return i;
		}
		final int read = super.read ( );
		if ( 0 < read )
			this.bytesRead++;
		return read;
	}

	/**
	 * Reads at most {@code length} bytes from this stream and stores them in
	 * the byte array {@code buffer} starting at {@code offset}. Characters are
	 * read from the pushback buffer first, then from the source reader if more
	 * bytes are required. Blocks until {@code count} characters have been read,
	 * the end of the source reader is detected or an exception is thrown.
	 *
	 * @param buffer the array in which to store the characters read from this
	 *               reader.
	 * @param offset the initial position in {@code buffer} to store the characters
	 *               read from this reader.
	 * @param length the maximum number of bytes to store in {@code buffer}.
	 * @return the number of bytes read or -1 if the end of the source reader
	 * has been reached.
	 * @throws IndexOutOfBoundsException if {@code offset < 0} or {@code count < 0}, or if
	 *                                   {@code offset + count} is greater than the length of
	 *                                   {@code buffer}.
	 * @throws IllegalArgumentException  when a null byte buffer has been passed in
	 * @throws IOException               if this reader is closed or another I/O error occurs while
	 *                                   reading from this reader.
	 */
	@Override
	public int read ( final byte[] buffer , int offset , int length ) throws IOException {
		if ( null == buf )
			throw new IOException ( "Stream closed" );

		if ( null == buffer )
			throw new IllegalArgumentException ( "Null buffer" );

		if ( ( 0 > offset ) || ( offset > buffer.length ) || ( 0 > length ) || ( ( offset + length ) > buffer.length ) || ( 0 > ( offset + length ) ) ) {
			throw new IndexOutOfBoundsException ( );
		}

		if ( 0 == length ) {
			return 0;
		}

		int bufAvail = this.bufEnd - this.bufStart;
		if ( 0 < bufAvail ) {
			if ( length < bufAvail ) {
				bufAvail = length;
			}
			System.arraycopy ( this.buf , this.bufStart , buffer , offset , bufAvail );
			this.bufStart += bufAvail;
			if ( this.bufStart == this.bufEnd ) {
				this.bufStart = 0;
				this.bufEnd = 0;
			}
			this.bytesRead += bufAvail;
			offset += bufAvail;
			length -= bufAvail;
		}

		if ( 0 < length ) {
			length = super.read ( buffer , offset , length );
			if ( - 1 == length ) {
				return 0 == bufAvail ? - 1 : bufAvail;
			}
			this.bytesRead += length;
		}
		return bufAvail + length;
	}

	/**
	 * Resets this stream to the last marked position. Resetting the stream is
	 * not supported in this class; this implementation always throws an
	 * {@code IOException}.
	 *
	 * @throws IOException if this method is called.
	 */
	@Override
	public synchronized void reset ( ) throws IOException {
		throw new IOException ( "mark/reset not supported" );
	}

	/**
	 * Pushes the specified character {@code oneChar} back to this reader. This
	 * is done in such a way that the next character read from this reader is
	 * {@code (char) oneChar}.
	 * <p>
	 * If this reader's internal pushback buffer cannot store the character, an
	 * {@code IOException} is thrown.
	 *
	 * @param b the character to push back to this stream.
	 * @throws IOException if this reader is closed or the internal pushback buffer is
	 *                     full.
	 */
	public void unread ( final int b ) throws IOException {
		if ( null == buf )
			throw new IOException ( "Stream closed" );

		if ( this.buf.length == this.bufEnd ) {
			if ( 0 < bufStart ) {
				System.arraycopy ( this.buf , this.bufStart , this.buf , 0 , this.bufEnd - this.bufStart );
				this.bufEnd -= this.bufStart;
				this.bufStart = 0;
			}
			else {
				throw new IOException ( "Push back buffer is full" );
			}
		}
		this.buf[ this.bufEnd ] = ( byte ) b;
		this.bufEnd++;
		this.bytesRead--;
	}

	/**
	 * Pushes a subset of the bytes in {@code buffer} back to this stream. The
	 * subset is defined by the start position {@code offset} within
	 * {@code buffer} and the number of characters specified by {@code length}.
	 * The bytes are pushed back in such a way that the next byte read from this
	 * stream is {@code buffer[offset]}, then {@code buffer[1]} and so on.
	 * <p>
	 * If this stream's internal pushback buffer cannot store the selected
	 * subset of {@code buffer}, an {@code IOException} is thrown.
	 *
	 * @param buffer the buffer containing the characters to push back to this
	 *               reader.
	 * @param offset the index of the first byte in {@code buffer} to push back.
	 * @param length the number of bytes to push back.
	 * @throws IndexOutOfBoundsException if {@code offset < 0} or {@code count < 0}, or if
	 *                                   {@code offset + count} is greater than the length of
	 *                                   {@code buffer}.
	 * @throws IOException               if this reader is closed or the free space in the internal
	 *                                   pushback buffer is not sufficient to store the selected
	 *                                   contents of {@code buffer}.
	 * @throws NullPointerException      if {@code buffer} is {@code null}.
	 */
	public void unread ( final byte[] buffer , final int offset , final int length ) throws IOException {
		if ( null == buf )
			throw new IOException ( "Stream closed" );

		// Force buffer null check first!
		if ( offset > buffer.length - length || 0 > offset ) {
			throw new ArrayIndexOutOfBoundsException ( "Offset out of bounds" );
		}

		if ( 0 > length ) {
			throw new ArrayIndexOutOfBoundsException ( "Length out of bounds" );
		}

		if ( length > this.buf.length - this.bufEnd + this.bufStart ) {
			throw new IOException ( "Push back buffer is full" );
		}

		if ( length > this.buf.length - this.bufEnd ) {
			System.arraycopy ( this.buf , this.bufStart , this.buf , 0 , this.bufEnd - this.bufStart );
			this.bufEnd -= this.bufStart;
			this.bufStart = 0;
		}
		System.arraycopy ( buffer , offset , this.buf , this.bufEnd , length );
		this.bufEnd += length;
		this.bytesRead -= length;
	}

	/**
	 * Pushes all the bytes in {@code buffer} back to this reader. The bytes are
	 * pushed back in such a way that the next character read from this stream
	 * is buffer[0], then buffer[1] and so on.
	 * <p>
	 * If this streams's internal pushback buffer cannot store the entire
	 * contents of {@code buffer}, an {@code IOException} is thrown.
	 *
	 * @param buffer the buffer containing the characters to push back to this
	 *               reader.
	 * @throws IOException if this reader is closed or the free space in the internal
	 *                     pushback buffer is not sufficient to store the contents of
	 *                     {@code buffer}.
	 */
	public void unread ( final byte[] buffer ) throws IOException {
		this.unread ( buffer , 0 , buffer.length );
	}

	/**
	 * Skips {@code count} bytes in this stream. This implementation skips bytes
	 * in the pushback buffer first and then in the source reader if necessary.
	 *
	 * @param count the number of characters to skip.
	 * @return the number of characters actually skipped.
	 * @throws IllegalArgumentException if {@code count < 0}.
	 * @throws IOException              if this reader is closed or another I/O error occurs.
	 */
	@Override
	public long skip ( long count ) throws IOException {
		if ( 0 > count ) {
			throw new IllegalArgumentException ( );
		}

		if ( null == buf )
			throw new IOException ( "Stream closed" );

		if ( 0 == count ) {
			return 0;
		}

		int bufAvail = this.bufEnd - this.bufStart;
		if ( 0 < bufAvail ) {
			if ( count < bufAvail ) {
				this.bufStart += count;
				count = 0;
			}
			else {
				this.bufStart = 0;
				this.bufEnd = 0;
				count -= bufAvail;
			}
		}
		if ( 0 < count ) {
			bufAvail += super.skip ( count );
		}
		this.bytesRead += bufAvail;
		return bufAvail;
	}
}
