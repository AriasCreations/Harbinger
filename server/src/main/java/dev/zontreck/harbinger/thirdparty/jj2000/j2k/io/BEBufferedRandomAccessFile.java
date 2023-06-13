/*
 * CVS Identifier:
 *
 * $Id: BEBufferedRandomAccessFile.java,v 1.18 2001/07/17 13:13:35 grosbois Exp $
 *
 * Interface:           RandomAccessIO.java
 *
 * Description:         Class for random access I/O (big-endian ordering).
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
package dev.zontreck.harbinger.thirdparty.jj2000.j2k.io;

import java.io.File;
import java.io.IOException;

/**
 * This class defines a Buffered Random Access File, where all I/O is considered
 * to be big-endian. It extends the <tt>BufferedRandomAccessFile</tt> class.
 *
 * @see RandomAccessIO
 * @see BinaryDataOutput
 * @see BinaryDataInput
 * @see BufferedRandomAccessFile
 */
public class BEBufferedRandomAccessFile extends BufferedRandomAccessFile {

	/**
	 * Constructor. Always needs a size for the buffer.
	 *
	 * @param file       The file associated with the buffer
	 * @param mode       "r" for read, "rw" or "rw+" for read and write mode ("rw+"
	 *                   opens the file for update whereas "rw" removes it before. So
	 *                   the 2 modes are different only if the file already exists).
	 * @param bufferSize The number of bytes to buffer
	 * @throws java.io.IOException If an I/O error occurred.
	 */
	public BEBufferedRandomAccessFile ( final File file , final String mode , final int bufferSize ) throws IOException {
		super ( file , mode , bufferSize );
		this.byteOrdering = EndianType.BIG_ENDIAN;
	}

	/**
	 * Constructor. Uses the default value for the byte-buffer size (512 bytes).
	 *
	 * @param file The file associated with the buffer
	 * @param mode "r" for read, "rw" or "rw+" for read and write mode ("rw+"
	 *             opens the file for update whereas "rw" removes it before. So
	 *             the 2 modes are different only if the file already exists).
	 * @throws java.io.IOException If an I/O error occurred.
	 */
	public BEBufferedRandomAccessFile ( final File file , final String mode ) throws IOException {
		super ( file , mode );
		this.byteOrdering = EndianType.BIG_ENDIAN;
	}

	/**
	 * Constructor. Always needs a size for the buffer.
	 *
	 * @param name       The name of the file associated with the buffer
	 * @param mode       "r" for read, "rw" or "rw+" for read and write mode ("rw+"
	 *                   opens the file for update whereas "rw" removes it before. So
	 *                   the 2 modes are different only if the file already exists).
	 * @param bufferSize The number of bytes to buffer
	 * @throws java.io.IOException If an I/O error occurred.
	 */
	public BEBufferedRandomAccessFile ( final String name , final String mode , final int bufferSize ) throws IOException {
		super ( name , mode , bufferSize );
		this.byteOrdering = EndianType.BIG_ENDIAN;
	}

	/**
	 * Constructor. Uses the default value for the byte-buffer size (512 bytes).
	 *
	 * @param name The name of the file associated with the buffer
	 * @param mode "r" for read, "rw" or "rw+" for read and write mode ("rw+"
	 *             opens the file for update whereas "rw" removes it before. So
	 *             the 2 modes are different only if the file already exists).
	 * @throws java.io.IOException If an I/O error occurred.
	 */
	public BEBufferedRandomAccessFile ( final String name , final String mode ) throws IOException {
		super ( name , mode );
		this.byteOrdering = EndianType.BIG_ENDIAN;
	}

	/**
	 * Writes the short value of <tt>v</tt> (i.e., 16 least significant bits) to
	 * the output. Prior to writing, the output should be realigned at the byte
	 * level.
	 *
	 * <p>
	 * Signed or unsigned data can be written. To write a signed value just pass
	 * the <tt>short</tt> value as an argument. To write unsigned data pass the
	 * <tt>int</tt> value as an argument (it will be automatically casted, and
	 * only the 16 least significant bits will be written).
	 *
	 * @param v The value to write to the output
	 * @throws java.io.IOException If an I/O error occurred.
	 */
	@Override
	public final void writeShort ( final int v ) throws IOException {
		this.write ( v >>> 8 );
		this.write ( v );
	}

	/**
	 * Writes the int value of <tt>v</tt> (i.e., the 32 bits) to the output.
	 * Prior to writing, the output should be realigned at the byte level.
	 *
	 * @param v The value to write to the output
	 * @throws java.io.IOException If an I/O error occurred.
	 */
	@Override
	public final void writeInt ( final int v ) throws IOException {
		this.write ( v >>> 24 );
		this.write ( v >>> 16 );
		this.write ( v >>> 8 );
		this.write ( v );
	}

	/**
	 * Writes the long value of <tt>v</tt> (i.e., the 64 bits) to the output.
	 * Prior to writing, the output should be realigned at the byte level.
	 *
	 * @param v The value to write to the output
	 * @throws java.io.IOException If an I/O error occurred.
	 */
	@Override
	public final void writeLong ( final long v ) throws IOException {
		this.write ( ( int ) ( v >>> 56 ) );
		this.write ( ( int ) ( v >>> 48 ) );
		this.write ( ( int ) ( v >>> 40 ) );
		this.write ( ( int ) ( v >>> 32 ) );
		this.write ( ( int ) ( v >>> 24 ) );
		this.write ( ( int ) ( v >>> 16 ) );
		this.write ( ( int ) ( v >>> 8 ) );
		this.write ( ( int ) v );
	}

	/**
	 * Writes the IEEE float value <tt>v</tt> (i.e., 32 bits) to the output.
	 * Prior to writing, the output should be realigned at the byte level.
	 *
	 * @param v The value to write to the output
	 * @throws java.io.IOException If an I/O error occurred.
	 */
	@Override
	public final void writeFloat ( final float v ) throws IOException {
		final int intV = Float.floatToIntBits ( v );

		this.write ( intV >>> 24 );
		this.write ( intV >>> 16 );
		this.write ( intV >>> 8 );
		this.write ( intV );
	}

	/**
	 * Writes the IEEE double value <tt>v</tt> (i.e., 64 bits) to the output.
	 * Prior to writing, the output should be realigned at the byte level.
	 *
	 * @param v The value to write to the output
	 * @throws java.io.IOException If an I/O error occurred.
	 */
	@Override
	public final void writeDouble ( final double v ) throws IOException {
		final long longV = Double.doubleToLongBits ( v );

		this.write ( ( int ) ( longV >>> 56 ) );
		this.write ( ( int ) ( longV >>> 48 ) );
		this.write ( ( int ) ( longV >>> 40 ) );
		this.write ( ( int ) ( longV >>> 32 ) );
		this.write ( ( int ) ( longV >>> 24 ) );
		this.write ( ( int ) ( longV >>> 16 ) );
		this.write ( ( int ) ( longV >>> 8 ) );
		this.write ( ( int ) ( longV ) );
	}

	/**
	 * Reads a signed short (i.e. 16 bit) from the input. Prior to reading, the
	 * input should be realigned at the byte level.
	 *
	 * @return The next byte-aligned signed short (16 bit) from the input.
	 * @throws java.io.EOFException If the end-of file was reached before getting all the
	 *                              necessary data.
	 * @throws java.io.IOException  If an I/O error occurred.
	 */
	@Override
	public final short readShort ( ) throws IOException {
		return ( short ) ( ( this.read ( ) << 8 ) | ( this.read ( ) ) );
	}

	/**
	 * Reads an unsigned short (i.e., 16 bit) from the input. It is returned as
	 * an <tt>int</tt> since Java does not have an unsigned short type. Prior to
	 * reading, the input should be realigned at the byte level.
	 *
	 * @return The next byte-aligned unsigned short (16 bit) from the input, as
	 * an <tt>int</tt>.
	 * @throws java.io.EOFException If the end-of file was reached before getting all the
	 *                              necessary data.
	 * @throws java.io.IOException  If an I/O error occurred.
	 */
	@Override
	public final int readUnsignedShort ( ) throws IOException {
		return ( ( this.read ( ) << 8 ) | this.read ( ) );
	}

	/**
	 * Reads a signed int (i.e., 32 bit) from the input. Prior to reading, the
	 * input should be realigned at the byte level.
	 *
	 * @return The next byte-aligned signed int (32 bit) from the input.
	 * @throws java.io.EOFException If the end-of file was reached before getting all the
	 *                              necessary data.
	 * @throws java.io.IOException  If an I/O error occurred.
	 */
	@Override
	public final int readInt ( ) throws IOException {
		return ( ( this.read ( ) << 24 ) | ( this.read ( ) << 16 ) | ( this.read ( ) << 8 ) | this.read ( ) );
	}

	/**
	 * Reads an unsigned int (i.e., 32 bit) from the input. It is returned as a
	 * <tt>long</tt> since Java does not have an unsigned short type. Prior to
	 * reading, the input should be realigned at the byte level.
	 *
	 * @return The next byte-aligned unsigned int (32 bit) from the input, as a
	 * <tt>long</tt>.
	 * @throws java.io.EOFException If the end-of file was reached before getting all the
	 *                              necessary data.
	 * @throws java.io.IOException  If an I/O error occurred.
	 */
	@Override
	public final long readUnsignedInt ( ) throws IOException {
		return ( ( ( long ) this.read ( ) << 24 ) | ( ( long ) this.read ( ) << 16 ) | ( ( long ) this.read ( ) << 8 ) | this.read ( ) );
	}

	/**
	 * Reads a signed long (i.e., 64 bit) from the input. Prior to reading, the
	 * input should be realigned at the byte level.
	 *
	 * @return The next byte-aligned signed long (64 bit) from the input.
	 * @throws java.io.EOFException If the end-of file was reached before getting all the
	 *                              necessary data.
	 * @throws java.io.IOException  If an I/O error occurred.
	 */
	@Override
	public final long readLong ( ) throws IOException {
		return (
				( ( long ) this.read ( ) << 56 ) | ( ( long ) this.read ( ) << 48 ) | ( ( long ) this.read ( ) << 40 ) | ( ( long ) this.read ( ) << 32 )
						| ( ( long ) this.read ( ) << 24 ) | ( ( long ) this.read ( ) << 16 ) | ( ( long ) this.read ( ) << 8 ) | this.read ( )
		);
	}

	/**
	 * Reads an IEEE single precision (i.e., 32 bit) floating-point number from
	 * the input. Prior to reading, the input should be realigned at the byte
	 * level.
	 *
	 * @return The next byte-aligned IEEE float (32 bit) from the input.
	 * @throws java.io.EOFException If the end-of file was reached before getting all the
	 *                              necessary data.
	 * @throws java.io.IOException  If an I/O error occurred.
	 */
	@Override
	public final float readFloat ( ) throws IOException {
		return Float.intBitsToFloat ( ( this.read ( ) << 24 ) | ( this.read ( ) << 16 ) | ( this.read ( ) << 8 ) | ( this.read ( ) ) );
	}

	/**
	 * Reads an IEEE double precision (i.e., 64 bit) floating-point number from
	 * the input. Prior to reading, the input should be realigned at the byte
	 * level.
	 *
	 * @return The next byte-aligned IEEE double (64 bit) from the input.
	 * @throws java.io.EOFException If the end-of file was reached before getting all the
	 *                              necessary data.
	 * @throws java.io.IOException  If an I/O error occurred.
	 */
	@Override
	public final double readDouble ( ) throws IOException {
		return Double.longBitsToDouble ( ( ( long ) this.read ( ) << 56 ) | ( ( long ) this.read ( ) << 48 ) | ( ( long ) this.read ( ) << 40 )
				| ( ( long ) this.read ( ) << 32 ) | ( ( long ) this.read ( ) << 24 ) | ( ( long ) this.read ( ) << 16 ) | ( ( long ) this.read ( ) << 8 )
				| ( this.read ( ) ) );
	}

	/**
	 * Returns a string of information about the file and the endianess
	 */
	@Override
	public String toString ( ) {
		return super.toString ( ) + "\nBig-Endian ordering";
	}
}
