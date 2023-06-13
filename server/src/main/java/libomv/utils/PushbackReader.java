/**
 * Copyright 2002-2004 The Apache Software Foundation.
 * Copyright (c) 2009-2017, Frederick Martian
 *  
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package libomv.utils;

import java.io.FilterReader;
import java.io.IOException;
import java.io.Reader;

/**
 * Wraps an existing {@link Reader} and adds functionality to "push back"
 * characters that have been read, so that they can be read again. Parsers may
 * find this useful. The number of characters which may be pushed back can be
 * specified during construction. If the buffer of pushed back bytes is empty,
 * characters are read from the underlying reader.
 */
public class PushbackReader extends FilterReader
{
	/**
	 * The {@code char} array containing the chars to read.
	 */
	char[] buf;

	/**
	 * The current position within the char array {@code buf}. A value equal to
	 * buf.length indicates no chars available. A value of 0 indicates the
	 * buffer is full.
	 */
	int pos;

	int bytes;

	public int getBytePosition()
	{
		return this.bytes;
	}

	/**
	 * Constructs a new {@code PushbackReader} with {@code in} as source reader.
	 * The size of the pushback buffer is set to the default value of 1 character.
	 * 
	 * @param in
	 *            the source reader.
	 */
	public PushbackReader(final Reader in)
	{
		this(in, 0, 1);
	}

	/**
	 * Constructs a new {@code PushbackReader} with {@code in} as source reader.
	 * The size of the pushback buffer is set to the default value of 1 character.
	 * 
	 * @param in
	 *            the source reader.
	 * @param offset
	 *            the offset in characters into the original reader data
	 * @throws IllegalArgumentException
	 *            if {@code offset} is negative..
	 */
	public PushbackReader(final Reader in, final int offset)
	{
		this(in, offset, 1);
	}
	
	/**
	 * Constructs a new {@code PushbackReader} with {@code in} as source reader.
	 * The size of the pushback buffer is set to {@code size}.
	 * 
	 * @param in
	 *            the source reader.
	 * @param offset
	 *            the offset in characters into the original reader data
	 * @param size
	 *            the size of the pushback buffer.
	 * @throws IllegalArgumentException
	 *            if {@code size} is zero or negative or if {@code offset} is negative..
	 */
	public PushbackReader(final Reader in, final int offset, final int size)
	{
		super(in);
		if (0 >= size)
		{
			throw new IllegalArgumentException("size <= 0");
		}
		if (0 > offset)
		{
			throw new IllegalArgumentException("offset < 0");
		}
		this.buf = new char[size];
		this.pos = size;
		this.bytes = offset;
	}

	/**
	 * Closes this reader. This implementation closes the source reader and
	 * releases the pushback buffer.
	 * 
	 * @throws IOException
	 *             if an error occurs while closing this reader.
	 */
	@Override
	public void close() throws IOException
	{
		synchronized (this.lock)
		{
			this.buf = null;
			super.close();
		}
	}

	/**
	 * Marks the current position in this stream. Setting a mark is not
	 * supported in this class; this implementation always throws an
	 * {@code IOException}.
	 * 
	 * @param readAheadLimit
	 *            the number of character that can be read from this reader
	 *            before the mark is invalidated; this parameter is ignored.
	 * @throws IOException
	 *             if this method is called.
	 */
	@Override
	public void mark(final int readAheadLimit) throws IOException
	{
		throw new IOException("Not Supported");
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
	public boolean markSupported()
	{
		return false;
	}

	/**
	 * Reads a single character from this reader and returns it as an integer
	 * with the two higher-order bytes set to 0. Returns -1 if the end of the
	 * reader has been reached. If the pushback buffer does not contain any
	 * available characters then a character from the source reader is returned.
	 * Blocks until one character has been read, the end of the source reader is
	 * detected or an exception is thrown.
	 * 
	 * @return the character read or -1 if the end of the source reader has been
	 *         reached.
	 * @throws IOException
	 *             if this reader is closed or an I/O error occurs while reading
	 *             from this reader.
	 */
	@Override
	public int read() throws IOException
	{
		synchronized (this.lock)
		{
			if (null == buf)
			{
				throw new IOException("Reader closed");
			}
			/* Is there a pushback character available? */
			if (this.pos < this.buf.length)
			{
				this.bytes++;
				char c = buf[this.pos];
				this.pos++;
				return c;
			}
			/**
			 * Assume read() in the InputStream will return 2 lowest-order bytes
			 * or -1 if end of stream.
			 */
			final int read = super.read();
			if (0 < read)
				this.bytes += read;
			return read;
		}
	}

	/**
	 * Reads at most {@code length} bytes from this reader and stores them in
	 * byte array {@code buffer} starting at {@code offset}. Characters are read
	 * from the pushback buffer first, then from the source reader if more bytes
	 * are required. Blocks until {@code count} characters have been read, the
	 * end of the source reader is detected or an exception is thrown.
	 * 
	 * @param buffer
	 *            the array in which to store the characters read from this
	 *            reader.
	 * @param offset
	 *            the initial position in {@code buffer} to store the characters
	 *            read from this reader.
	 * @param count
	 *            the maximum number of bytes to store in {@code buffer}.
	 * @return the number of bytes read or -1 if the end of the source reader
	 *         has been reached.
	 * @throws IndexOutOfBoundsException
	 *             if {@code offset < 0} or {@code count < 0}, or if
	 *             {@code offset + count} is greater than the length of
	 *             {@code buffer}.
	 * @throws IOException
	 *             if this reader is closed or another I/O error occurs while
	 *             reading from this reader.
	 */
	@Override
	public int read(final char[] buffer, final int offset, final int count) throws IOException
	{
		synchronized (this.lock)
		{
			if (null == this.buf)
			{
				throw new IOException("Reader closed");
			}

			// avoid int overflow
			if (0 > offset || 0 > count || offset > buffer.length - count)
			{
				throw new IndexOutOfBoundsException();
			}

			int copiedChars = 0;
			int copyLength = 0;
			int newOffset = offset;
			/* Are there pushback chars available? */
			if (this.pos < this.buf.length)
			{
				copyLength = (this.buf.length - this.pos >= count) ? count : this.buf.length - this.pos;
				System.arraycopy(this.buf, this.pos, buffer, newOffset, copyLength);
				newOffset += copyLength;
				copiedChars += copyLength;
				/* Use up the chars in the local buffer */
				this.pos += copyLength;
				this.bytes += copyLength;
			}
			/* Have we copied enough? */
			if (copyLength == count)
			{
				return count;
			}
			final int inCopied = super.read(buffer, newOffset, count - copiedChars);
			if (0 < inCopied)
			{
				this.bytes += copiedChars;
				return inCopied + copiedChars;
			}
			if (0 == copiedChars)
			{
				return inCopied;
			}
			return copiedChars;
		}
	}

	/**
	 * Indicates whether this reader is ready to be read without blocking.
	 * Returns {@code true} if this reader will not block when {@code read} is
	 * called, {@code false} if unknown or blocking will occur.
	 * 
	 * @return {@code true} if the receiver will not block when {@code read()}
	 *         is called, {@code false} if unknown or blocking will occur.
	 * @throws IOException
	 *             if this reader is closed or some other I/O error occurs.
	 * @see #read()
	 * @see #read(char[], int, int)
	 */
	@Override
	public boolean ready() throws IOException
	{
		synchronized (this.lock)
		{
			if (null == buf)
			{
				throw new IOException("Reader closed");
			}
			return (0 < buf.length - pos || super.ready());
		}
	}

	/**
	 * Resets this reader to the last marked position. Resetting the reader is
	 * not supported in this class; this implementation always throws an
	 * {@code IOException}.
	 * 
	 * @throws IOException
	 *             if this method is called.
	 */
	@Override
	public void reset() throws IOException
	{
		throw new IOException("mark/reset not supported");
	}

	/**
	 * Pushes all the characters in {@code buffer} back to this reader. The
	 * characters are pushed back in such a way that the next character read
	 * from this reader is buffer[0], then buffer[1] and so on.
	 * <p>
	 * If this reader's internal pushback buffer cannot store the entire
	 * contents of {@code buffer}, an {@code IOException} is thrown.
	 * 
	 * @param buffer
	 *            the buffer containing the characters to push back to this
	 *            reader.
	 * @throws IOException
	 *             if this reader is closed or the free space in the internal
	 *             pushback buffer is not sufficient to store the contents of
	 *             {@code buffer}.
	 */
	public void unread(final char[] buffer) throws IOException
	{
		this.unread(buffer, 0, buffer.length);
	}

	/**
	 * Pushes a subset of the characters in {@code buffer} back to this reader.
	 * The subset is defined by the start position {@code offset} within
	 * {@code buffer} and the number of characters specified by {@code length}.
	 * The characters are pushed back in such a way that the next byte read from
	 * this reader is {@code buffer[offset]}, then {@code buffer[1]} and so on.
	 * <p>
	 * If this reader's internal pushback buffer cannot store the selected
	 * subset of {@code buffer}, an {@code IOException} is thrown.
	 * 
	 * @param buffer
	 *            the buffer containing the characters to push back to this
	 *            reader.
	 * @param offset
	 *            the index of the first character in {@code buffer} to push
	 *            back.
	 * @param length
	 *            the number of bytes to push back.
	 * @throws IndexOutOfBoundsException
	 *             if {@code offset < 0} or {@code count < 0}, or if
	 *             {@code offset + count} is greater than the length of
	 *             {@code buffer}.
	 * @throws IOException
	 *             if this reader is closed or the free space in the internal
	 *             pushback buffer is not sufficient to store the selected
	 *             contents of {@code buffer}.
	 * @throws NullPointerException
	 *             if {@code buffer} is {@code null}.
	 */
	public void unread(final char[] buffer, final int offset, final int length) throws IOException
	{
		synchronized (this.lock)
		{
			if (null == buf)
			{
				throw new IOException("Stream closed");
			}

			if (length > this.pos)
			{
				throw new IOException("Push back buffer is full");
			}

			// Force buffer null check first!
			if (offset > buffer.length - length || 0 > offset)
			{
				throw new ArrayIndexOutOfBoundsException("Offset out of bounds");
			}

			if (0 > length)
			{
				throw new ArrayIndexOutOfBoundsException("Length out of bounds");
			}

			if (length > this.pos)
			{
				throw new IOException("Push back buffer is full");
			}

			for (int i = offset + length - 1; i >= offset; i--)
			{
				--this.pos;
				this.buf[this.pos] = buffer[i];
			}
			this.pos -= length;
			this.bytes -= length;
		}
	}

	/**
	 * Pushes the specified character {@code oneChar} back to this reader. This
	 * is done in such a way that the next character read from this reader is
	 * {@code (char) oneChar}.
	 * <p>
	 * If this reader's internal pushback buffer cannot store the character, an
	 * {@code IOException} is thrown.
	 * 
	 * @param oneChar
	 *            the character to push back to this stream.
	 * @throws IOException
	 *             if this reader is closed or the internal pushback buffer is
	 *             full.
	 */
	public void unread(final int oneChar) throws IOException
	{
		synchronized (this.lock)
		{
			if (null == buf)
			{
				throw new IOException("Stream closed");
			}

			if (0 == pos)
			{
				throw new IOException("Push back buffer is full");
			}

			--this.pos;
			this.buf[this.pos] = (char) oneChar;
			this.bytes--;
		}
	}

	/**
	 * Skips {@code count} characters in this reader. This implementation skips
	 * characters in the pushback buffer first and then in the source reader if
	 * necessary.
	 * 
	 * @param count
	 *            the number of characters to skip.
	 * @return the number of characters actually skipped.
	 * @throws IllegalArgumentException
	 *             if {@code count < 0}.
	 * @throws IOException
	 *             if this reader is closed or another I/O error occurs.
	 */
	@Override
	public long skip(long count) throws IOException
	{
		if (0 > count)
		{
			throw new IllegalArgumentException();
		}

		synchronized (this.lock)
		{
			if (null == buf)
			{
				throw new IOException("Stream closed");
			}
			if (0 == count)
			{
				return 0;
			}

			long pskip = this.buf.length - this.pos;
			if (0 < pskip)
			{
				if (count < pskip)
				{
					pskip = count;
				}
				this.pos += pskip;
				count -= pskip;
			}
			if (0 < count)
			{
				pskip += super.skip(count);
			}
			this.bytes -= pskip;
			return pskip;
		}
	}
}