/*
 * CVS identifier:
 *
 * $Id: ByteInputBuffer.java,v 1.13 2001/10/17 17:01:57 grosbois Exp $
 *
 * Class:                   ByteInputBuffer
 *
 * Description:             Provides buffering for byte based input, similar
 *                          to the standard class ByteArrayInputStream
 *
 *                          the old dev.zontreck.harbinger.thirdparty.jj2000.j2k.io.ByteArrayInput class by
 *                          Diego SANTA CRUZ, Apr-26-1999
 *
 *
 * COPYRIGHT:
 *
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
 *
 * Copyright (c) 1999/2000 JJ2000 Partners.
 */
package dev.zontreck.harbinger.thirdparty.jj2000.j2k.entropy.decoder;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

/**
 * This class provides a byte input facility from byte buffers. It is similar to
 * the ByteArrayInputStream class, but adds the possibility to add data to the
 * stream after the creation of the object.
 *
 * <p>
 * Unlike the ByteArrayInputStream this class is not thread safe (i.e. no two
 * threads can use the same object at the same time, but different objects may
 * be used in different threads).
 *
 * <p>
 * This class can modify the contents of the buffer given to the constructor,
 * when the addByteArray() method is called.
 *
 * @see InputStream
 */
public class ByteInputBuffer {

	/**
	 * The byte array containing the data
	 */
	private byte[] buf;

	/**
	 * The index one greater than the last valid character in the input stream
	 * buffer
	 */
	private int count;

	/**
	 * The index of the next character to read from the input stream buffer
	 */
	private int pos;

	/**
	 * Creates a new byte array input stream that reads data from the specified
	 * byte array. The byte array is not copied.
	 *
	 * @param buf the input buffer.
	 */
	public ByteInputBuffer ( final byte[] buf ) {
		this.buf = buf;
		this.count = buf.length;
	}

	/**
	 * Creates a new byte array input stream that reads data from the specified
	 * byte array. Up to length characters are to be read from the byte array,
	 * starting at the indicated offset.
	 *
	 * <p>
	 * The byte array is not copied.
	 *
	 * @param buf    the input buffer.
	 * @param offset the offset in the buffer of the first byte to read.
	 * @param length the maximum number of bytes to read from the buffer.
	 */
	public ByteInputBuffer ( final byte[] buf , final int offset , final int length ) {
		this.buf = buf;
		this.pos = offset;
		this.count = offset + length;
	}

	/**
	 * Sets the underlying buffer byte array to the given one, with the given
	 * offset and length. If 'buf' is null then the current byte buffer is
	 * assumed. If 'offset' is negative, then it will be assumed to be
	 * 'off+len', where 'off' and 'len' are the offset and length of the current
	 * byte buffer.
	 *
	 * <p>
	 * The byte array is not copied.
	 *
	 * @param buf    the input buffer. If null it is the current input buffer.
	 * @param offset the offset in the buffer of the first byte to read. If
	 *               negative it is assumed to be the byte just after the end of
	 *               the current input buffer, only permitted if 'buf' is null.
	 * @param length the maximum number of bytes to read frmo the buffer.
	 */
	public void setByteArray ( final byte[] buf , final int offset , final int length ) {
		// In same buffer?
		if ( null == buf ) {
			if ( 0 > length || this.count + length > this.buf.length ) {
				throw new IllegalArgumentException ( );
			}
			if ( 0 > offset ) {
				this.pos = this.count;
				this.count += length;
			}
			else {
				this.count = offset + length;
				this.pos = offset;
			}
		}
		else { // New input buffer
			if ( 0 > offset || 0 > length || offset + length > buf.length ) {
				throw new IllegalArgumentException ( );
			}
			this.buf = buf;
			this.count = offset + length;
			this.pos = offset;
		}
	}

	/**
	 * Adds the specified data to the end of the byte array stream. This method
	 * modifies the byte array buffer. It can also discard the already read
	 * input.
	 *
	 * @param data The data to add. The data is copied.
	 * @param off  The index, in data, of the first element to add to the stream.
	 * @param len  The number of elements to add to the array.
	 */
	public synchronized void addByteArray ( final byte[] data , final int off , final int len ) {
		// Check integrity
		if ( 0 > len || 0 > off || len + off > this.buf.length ) {
			throw new IllegalArgumentException ( );
		}
		// Copy new data
		if ( this.count + len <= this.buf.length ) { // Enough place in 'buf'
			System.arraycopy ( data , off , this.buf , this.count , len );
			this.count += len;
		}
		else {
			if ( this.count - this.pos + len <= this.buf.length ) {
				// Enough place in 'buf' if we move input data
				// Move buffer
				System.arraycopy ( this.buf , this.pos , this.buf , 0 , this.count - this.pos );
			}
			else { // Not enough place in 'buf', use new buffer
				final byte[] oldbuf = this.buf;
				this.buf = new byte[ this.count - this.pos + len ];
				// Copy buffer
				System.arraycopy ( oldbuf , this.count , this.buf , 0 , this.count - this.pos );
			}
			this.count -= this.pos;
			this.pos = 0;
			// Copy new data
			System.arraycopy ( data , off , this.buf , this.count , len );
			this.count += len;
		}
	}

	/**
	 * Reads the next byte of data from this input stream. The value byte is
	 * returned as an int in the range 0 to 255. If no byte is available because
	 * the end of the stream has been reached, the EOFException exception is
	 * thrown.
	 *
	 * <p>
	 * This method is not synchronized, so it is not thread safe.
	 *
	 * @return The byte read in the range 0-255.
	 * @throws EOFException If the end of the stream is reached.
	 */
	public int readChecked ( ) throws IOException {
		if ( this.pos < this.count ) {
			int i = buf[ this.pos ] & 0xFF;
			this.pos++;
			return i;
		}
		throw new EOFException ( );
	}

	/**
	 * Reads the next byte of data from this input stream. The value byte is
	 * returned as an int in the range 0 to 255. If no byte is available because
	 * the end of the stream has been reached, -1 is returned.
	 *
	 * <p>
	 * This method is not synchronized, so it is not thread safe.
	 *
	 * @return The byte read in the range 0-255, or -1 if the end of stream has
	 * been reached.
	 */
	public int read ( ) {
		if ( this.pos < this.count ) {
			int i = buf[ this.pos ] & 0xFF;
			this.pos++;
			return i;
		}
		return - 1;
	}
}
