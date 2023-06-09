/*
 * CVS identifier:
 *
 * $Id: ByteOutputBuffer.java,v 1.10 2001/05/17 15:21:16 grosbois Exp $
 *
 * Class:                   ByteOutputBuffer
 *
 * Description:             Provides buffering for byte based output, similar
 *                          to the standard class ByteArrayOutputStream
 *
 *                          the old dev.zontreck.harbinger.thirdparty.jj2000.j2k.io.ByteArrayOutput class by
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
package dev.zontreck.harbinger.thirdparty.jj2000.j2k.entropy.encoder;

/**
 * This class provides a buffering output stream similar to
 * ByteArrayOutputStream, with some additional methods.
 *
 * <p>
 * Once an array has been written to an output stream or to a byte array, the
 * object can be reused as a new stream if the reset() method is called.
 *
 * <p>
 * Unlike the ByteArrayOutputStream class, this class is not thread safe.
 *
 * @see #reset
 */
public class ByteOutputBuffer {
	/**
	 * The buffer increase size
	 */
	public static final int BUF_INC = 512;
	/**
	 * The default initial buffer size
	 */
	public static final int BUF_DEF_LEN = 256;
	/**
	 * The buffer where the data is stored
	 */
	byte[] buf;
	/**
	 * The number of valid bytes in the buffer
	 */
	int count;

	/**
	 * Creates a new byte array output stream. The buffer capacity is initially
	 * BUF_DEF_LEN bytes, though its size increases if necessary.
	 */
	public ByteOutputBuffer ( ) {
		this.buf = new byte[ ByteOutputBuffer.BUF_DEF_LEN ];
	}

	/**
	 * Creates a new byte array output stream, with a buffer capacity of the
	 * specified size, in bytes.
	 *
	 * @param size the initial size.
	 */
	public ByteOutputBuffer ( final int size ) {
		this.buf = new byte[ size ];
	}

	/**
	 * Writes the specified byte to this byte array output stream. The
	 * functionality provided by this implementation is the same as for the one
	 * in the superclass, however this method is not synchronized and therefore
	 * not safe thread, but faster.
	 *
	 * @param b The byte to write
	 */
	public final void write ( final int b ) {
		if ( this.count == this.buf.length ) { // Resize buffer
			final byte[] tmpbuf = this.buf;
			this.buf = new byte[ this.buf.length + ByteOutputBuffer.BUF_INC ];
			System.arraycopy ( tmpbuf , 0 , this.buf , 0 , this.count );
		}
		this.buf[ this.count ] = ( byte ) b;
		this.count++;
	}

	/**
	 * Copies the specified part of the stream to the 'outbuf' byte array.
	 *
	 * @param off    The index of the first element in the stream to copy.
	 * @param len    The number of elements of the array to copy
	 * @param outbuf The destination array
	 * @param outoff The index of the first element in 'outbuf' where to write the
	 *               data.
	 */
	public void toByteArray ( final int off , final int len , final byte[] outbuf , final int outoff ) {
		// Copy the data
		System.arraycopy ( this.buf , off , outbuf , outoff , len );
	}

	/**
	 * Returns the number of valid bytes in the output buffer (count class
	 * variable).
	 *
	 * @return The number of bytes written to the buffer
	 */
	public int size ( ) {
		return this.count;
	}

	/**
	 * Discards all the buffered data, by resetting the counter of written bytes
	 * to 0.
	 */
	public void reset ( ) {
		this.count = 0;
	}

	/**
	 * Returns the byte buffered at the given position in the buffer. The
	 * position in the buffer is the index of the 'write()' method call after
	 * the last call to 'reset()'.
	 *
	 * @param pos The position of the byte to return
	 * @return The value (betweeb 0-255) of the byte at position 'pos'.
	 */
	public int getByte ( final int pos ) {
		if ( pos >= this.count ) {
			throw new IllegalArgumentException ( );
		}
		return this.buf[ pos ] & 0xFF;
	}
}
