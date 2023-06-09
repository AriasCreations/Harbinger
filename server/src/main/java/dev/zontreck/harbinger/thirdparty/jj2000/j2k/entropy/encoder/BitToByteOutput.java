/*
 * CVS identifier:
 *
 * $Id: BitToByteOutput.java,v 1.16 2001/10/17 16:56:59 grosbois Exp $
 *
 * Class:                   BitToByteOutput
 *
 * Description:             Adapter to perform bit based output on a byte
 *                          based one.
 *
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
 * This class provides an adapter to perform bit based output on byte based
 * output objects that inherit from a 'ByteOutputBuffer' class. This class
 * implements the bit stuffing policy needed for the 'selective arithmetic
 * coding bypass' mode of the entropy coder. This class also delays the output
 * of a trailing 0xFF, since they are synthetized be the decoder.
 */
class BitToByteOutput {
	/**
	 * The alternating sequence of 0's and 1's used for byte padding
	 */
	static final int PAD_SEQ = 0x2A;
	/**
	 * Flag that indicates if an FF has been delayed
	 */
	boolean delFF;
	/**
	 * The byte based output
	 */
	ByteOutputBuffer out;
	/**
	 * The bit buffer
	 */
	int bbuf;
	/**
	 * The position of the next bit to put in the bit buffer. When it is 7 the
	 * bit buffer 'bbuf' is empty. The value should always be between 7 and 0
	 * (i.e. if it gets to -1, the bit buffer should be immediately written to
	 * the byte output).
	 */
	int bpos = 7;
	/**
	 * The number of written bytes (excluding the bit buffer)
	 */
	int nb;
	/**
	 * Whether or not predictable termination is requested. This value is
	 * important when the last byte before termination is an 0xFF
	 */
	private boolean isPredTerm;

	/**
	 * Instantiates a new 'BitToByteOutput' object that uses 'out' as the
	 * underlying byte based output.
	 *
	 * @param out The underlying byte based output
	 */
	BitToByteOutput ( final ByteOutputBuffer out ) {
		this.out = out;
	}

	/**
	 * Writes to the bit stream the symbols contained in the 'symbuf' buffer.
	 * The least significant bit of each element in 'symbuf'is written.
	 *
	 * @param symbuf The symbols to write
	 * @param nsym   The number of symbols in symbuf
	 */
	final void writeBits ( final int[] symbuf , final int nsym ) {
		int i;
		int bbuf, bpos;
		bbuf = this.bbuf;
		bpos = this.bpos;
		// Write symbol by symbol to bit buffer
		for ( i = 0; i < nsym ; i++ ) {
			bbuf |= ( symbuf[ i ] & 0x01 ) << ( bpos );
			bpos--;
			if ( 0 > bpos ) { // Bit buffer is full, write it
				if ( 0xFF != bbuf ) { // No bit-stuffing needed
					if ( this.delFF ) { // Output delayed 0xFF if any
						this.out.write ( 0xFF );
						this.nb++;
						this.delFF = false;
					}
					this.out.write ( bbuf );
					this.nb++;
					bpos = 7;
				}
				else { // We need to do bit stuffing on next byte
					this.delFF = true;
					bpos = 6; // One less bit in next byte
				}
				bbuf = 0;
			}
		}
		this.bbuf = bbuf;
		this.bpos = bpos;
	}

	/**
	 * Write a bit to the output. The least significant bit of 'bit' is written
	 * to the output.
	 *
	 * @param bit
	 */
	final void writeBit ( final int bit ) {
		this.bbuf |= ( bit & 0x01 ) << ( this.bpos );
		this.bpos--;
		if ( 0 > bpos ) {
			if ( 0xFF != bbuf ) { // No bit-stuffing needed
				if ( this.delFF ) { // Output delayed 0xFF if any
					this.out.write ( 0xFF );
					this.nb++;
					this.delFF = false;
				}
				// Output the bit buffer
				this.out.write ( this.bbuf );
				this.nb++;
				this.bpos = 7;
			}
			else { // We need to do bit stuffing on next byte
				this.delFF = true;
				this.bpos = 6; // One less bit in next byte
			}
			this.bbuf = 0;
		}
	}

	/**
	 * Writes the contents of the bit buffer and byte aligns the output by
	 * filling bits with an alternating sequence of 0's and 1's.
	 */
	void flush ( ) {
		if ( this.delFF ) { // There was a bit stuffing
			if ( 6 != bpos ) { // Bit buffer is not empty
				// Output delayed 0xFF
				this.out.write ( 0xFF );
				this.nb++;
				this.delFF = false;
				// Pad to byte boundary with an alternating sequence of 0's
				// and 1's.
				this.bbuf |= ( BitToByteOutput.PAD_SEQ >>> ( 6 - this.bpos ) );
				// Output the bit buffer
				this.out.write ( this.bbuf );
				this.nb++;
				this.bpos = 7;
				this.bbuf = 0;
			}
			else if ( this.isPredTerm ) {
				this.out.write ( 0xFF );
				this.nb++;
				this.out.write ( 0x2A );
				this.nb++;
				this.bpos = 7;
				this.bbuf = 0;
				this.delFF = false;
			}
		}
		else { // There was no bit stuffing
			if ( 7 != bpos ) { // Bit buffer is not empty
				// Pad to byte boundary with an alternating sequence of 0's and
				// 1's.
				this.bbuf |= ( BitToByteOutput.PAD_SEQ >>> ( 6 - this.bpos ) );
				// Output the bit buffer (bbuf can not be 0xFF)
				this.out.write ( this.bbuf );
				this.nb++;
				this.bpos = 7;
				this.bbuf = 0;
			}
		}
	}

	/**
	 * Terminates the bit stream by calling 'flush()' and then 'reset()'.
	 * Finally, it returns the number of bytes effectively written.
	 *
	 * @return The number of bytes effectively written.
	 */
	public int terminate ( ) {
		this.flush ( );
		final int savedNb = this.nb;
		this.reset ( );
		return savedNb;
	}

	/**
	 * Resets the bit buffer to empty, without writing anything to the
	 * underlying byte output, and resets the byte count. The underlying byte
	 * output is NOT reset.
	 */
	void reset ( ) {
		this.delFF = false;
		this.bpos = 7;
		this.bbuf = 0;
		this.nb = 0;
	}

	/**
	 * Returns the length, in bytes, of the output bit stream as written by this
	 * object. If the output bit stream does not have an integer number of bytes
	 * in length then it is rounded to the next integer.
	 *
	 * @return The length, in bytes, of the output bit stream.
	 */
	int length ( ) {
		if ( this.delFF ) {
			// If bit buffer is empty we just need 'nb' bytes. If not we need
			// the delayed FF and the padded bit buffer.
			return this.nb + 2;
		}
		// If the bit buffer is empty, we just need 'nb' bytes. If not, we
		// add length of the padded bit buffer
		return this.nb + ( ( 7 == bpos ) ? 0 : 1 );
	}

	/**
	 * Set the flag according to whether or not the predictable termination is
	 * requested.
	 *
	 * @param isPredTerm Whether or not predictable termination is requested.
	 */
	void setPredTerm ( final boolean isPredTerm ) {
		this.isPredTerm = isPredTerm;
	}
}
