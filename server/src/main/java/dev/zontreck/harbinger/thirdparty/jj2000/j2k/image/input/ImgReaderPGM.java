/*
 *
 * Class:                   ImageWriterRawPGM
 *
 * Description:             Image writer for unsigned 8 bit data in
 *                          PGM files.
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
package dev.zontreck.harbinger.thirdparty.jj2000.j2k.image.input;

import dev.zontreck.harbinger.thirdparty.jj2000.j2k.JJ2KExceptionHandler;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.image.DataBlk;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.image.DataBlkInt;

import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * This class implements the ImgData interface for reading 8 bit unsigned data
 * from a binary PGM file.
 *
 * <p>
 * After being read the coefficients are level shifted by subtracting 2^(nominal
 * bit range-1)
 *
 * <p>
 * The TransferType (see ImgData) of this class is TYPE_INT.
 *
 * <p>
 * NOTE: This class is not thread safe, for reasons of internal buffering.
 *
 * @see dev.zontreck.harbinger.thirdparty.jj2000.j2k.image.ImgData
 */
public class ImgReaderPGM extends ImgReader {

	/**
	 * DC offset value used when reading image
	 */
	public static int DC_OFFSET = 128;
	/**
	 * The number of bits that determine the nominal dynamic range
	 */
	private final int rb;
	/**
	 * Where to read the data from
	 */
	private RandomAccessFile in;
	/**
	 * The offset of the raw pixel data in the PGM file
	 */
	private int offset;
	/**
	 * The line buffer.
	 */
	// This makes the class not thrad safe
	// (but it is not the only one making it so)
	private byte[] buf;

	/**
	 * Temporary DataBlkInt object (needed when encoder uses floating-point
	 * filters). This avoid allocating new DataBlk at each time
	 */
	private DataBlkInt intBlk;

	/**
	 * Creates a new PGM file reader from the specified file.
	 *
	 * @param file The input file.
	 * @throws IOException If an error occurs while opening the file.
	 */
	public ImgReaderPGM ( final File file ) throws IOException {
		this ( new RandomAccessFile ( file , "r" ) );
	}

	/**
	 * Creates a new PGM file reader from the specified file name.
	 *
	 * @param fname The input file name.
	 * @throws IOException If an error occurs while opening the file.
	 */
	public ImgReaderPGM ( final String fname ) throws IOException {
		this ( new RandomAccessFile ( fname , "r" ) );
	}

	/**
	 * Creates a new PGM file reader from the specified RandomAccessFile object.
	 * The file header is read to acquire the image size.
	 *
	 * @param in From where to read the data
	 * @throws EOFException if an EOF is read
	 * @throws IOException  if an error occurs when opening the file
	 */
	public ImgReaderPGM ( final RandomAccessFile in ) throws IOException {
		this.in = in;

		this.confirmFileType ( );
		this.skipCommentAndWhiteSpace ( );
		w = this.readHeaderInt ( );
		this.skipCommentAndWhiteSpace ( );
		h = this.readHeaderInt ( );
		this.skipCommentAndWhiteSpace ( );
		/* Read the highest pixel value from header (not used) */
		this.readHeaderInt ( );
		nc = 1;
		rb = 8;
	}

	/**
	 * Closes the underlying RandomAccessFile from where the image data is being
	 * read. No operations are possible after a call to this method.
	 *
	 * @throws IOException If an I/O error occurs.
	 */
	@Override
	public void close ( ) throws IOException {
		this.in.close ( );
		this.in = null;
	}

	/**
	 * Returns the number of bits corresponding to the nominal range of the data
	 * in the specified component. This is the value rb (range bits) that was
	 * specified in the constructor, which normally is 8 for non bilevel data,
	 * and 1 for bilevel data.
	 *
	 * <p>
	 * If this number is <i>b</b> then the nominal range is between -2^(b-1) and
	 * 2^(b-1)-1, since unsigned data is level shifted to have a nominal average
	 * of 0.
	 *
	 * @param c The index of the component.
	 * @return The number of bits corresponding to the nominal range of the
	 * data. Fro floating-point data this value is not applicable and
	 * the return value is undefined.
	 */
	@Override
	public int getNomRangeBits ( final int c ) {
		// Check component index
		if ( 0 != c )
			throw new IllegalArgumentException ( );

		return this.rb;
	}

	/**
	 * Returns the position of the fixed point in the specified component (i.e.
	 * the number of fractional bits), which is always 0 for this ImgReader.
	 *
	 * @param c The index of the component.
	 * @return The position of the fixed-point (i.e. the number of fractional
	 * bits). Always 0 for this ImgReader.
	 */
	@Override
	public int getFixedPoint ( final int c ) {
		// Check component index
		if ( 0 != c )
			throw new IllegalArgumentException ( );
		return 0;
	}

	/**
	 * Returns, in the blk argument, the block of image data containing the
	 * specifed rectangular area, in the specified component. The data is
	 * returned, as a reference to the internal data, if any, instead of as a
	 * copy, therefore the returned data should not be modified.
	 *
	 * <p>
	 * After being read the coefficients are level shifted by subtracting
	 * 2^(nominal bit range - 1)
	 *
	 * <p>
	 * The rectangular area to return is specified by the 'ulx', 'uly', 'w' and
	 * 'h' members of the 'blk' argument, relative to the current tile. These
	 * members are not modified by this method. The 'offset' and 'scanw' of the
	 * returned data can be arbitrary. See the 'DataBlk' class.
	 *
	 * <p>
	 * If the data array in <tt>blk</tt> is <tt>null</tt>, then a new one is
	 * created if necessary. The implementation of this interface may choose to
	 * return the same array or a new one, depending on what is more efficient.
	 * Therefore, the data array in <tt>blk</tt> prior to the method call should
	 * not be considered to contain the returned data, a new array may have been
	 * created. Instead, get the array from <tt>blk</tt> after the method has
	 * returned.
	 *
	 * <p>
	 * The returned data always has its 'progressive' attribute unset (i.e.
	 * false).
	 *
	 * <p>
	 * When an I/O exception is encountered the JJ2KExceptionHandler is used.
	 * The exception is passed to its handleException method. The action that is
	 * taken depends on the action that has been registered in
	 * JJ2KExceptionHandler. See JJ2KExceptionHandler for details.
	 *
	 * @param blk Its coordinates and dimensions specify the area to return.
	 *            Some fields in this object are modified to return the data.
	 * @param c   The index of the component from which to get the data. Only 0
	 *            is valid.
	 * @return The requested DataBlk
	 * @see #getCompData
	 * @see JJ2KExceptionHandler
	 */
	@Override
	public final DataBlk getInternCompData ( DataBlk blk , final int c ) {
		int k;
		int j;
		int i;
		final int mi;
		int[] barr;

		// Check component index
		if ( 0 != c )
			throw new IllegalArgumentException ( );

		// Check type of block provided as an argument
		if ( DataBlk.TYPE_INT != blk.getDataType ( ) ) {
			if ( null == intBlk )
				this.intBlk = new DataBlkInt ( blk.ulx , blk.uly , blk.w , blk.h );
			else {
				this.intBlk.ulx = blk.ulx;
				this.intBlk.uly = blk.uly;
				this.intBlk.w = blk.w;
				this.intBlk.h = blk.h;
			}
			blk = this.intBlk;
		}

		// Get data array
		barr = ( int[] ) blk.getData ( );
		if ( null == barr || barr.length < blk.w * blk.h ) {
			barr = new int[ blk.w * blk.h ];
			blk.setData ( barr );
		}

		// Check line buffer
		if ( null == buf || this.buf.length < blk.w ) {
			this.buf = new byte[ blk.w ];
		}

		try {
			// Read line by line
			mi = blk.uly + blk.h;
			for ( i = blk.uly; i < mi ; i++ ) {
				// Reposition in input
				this.in.seek ( this.offset + ( long ) i * this.w + blk.ulx );
				this.in.read ( this.buf , 0 , blk.w );
				for ( k = ( i - blk.uly ) * blk.w + blk.w - 1 , j = blk.w - 1; 0 <= j ; j-- , k-- ) {
					barr[ k ] = ( this.buf[ j ] & 0xFF ) - ImgReaderPGM.DC_OFFSET;
				}
			}
		} catch ( final IOException e ) {
			JJ2KExceptionHandler.handleException ( e );
		}

		// Turn off the progressive attribute
		blk.progressive = false;
		// Set buffer attributes
		blk.offset = 0;
		blk.scanw = blk.w;
		return blk;
	}

	/**
	 * Returns, in the blk argument, a block of image data containing the
	 * specifed rectangular area, in the specified component. The data is
	 * returned, as a copy of the internal data, therefore the returned data can
	 * be modified "in place".
	 *
	 * <p>
	 * After being read the coefficients are level shifted by subtracting
	 * 2^(nominal bit range - 1)
	 *
	 * <p>
	 * The rectangular area to return is specified by the 'ulx', 'uly', 'w' and
	 * 'h' members of the 'blk' argument, relative to the current tile. These
	 * members are not modified by this method. The 'offset' of the returned
	 * data is 0, and the 'scanw' is the same as the block's width. See the
	 * 'DataBlk' class.
	 *
	 * <p>
	 * If the data array in 'blk' is 'null', then a new one is created. If the
	 * data array is not 'null' then it is reused, and it must be large enough
	 * to contain the block's data. Otherwise an 'ArrayStoreException' or an
	 * 'IndexOutOfBoundsException' is thrown by the Java system.
	 *
	 * <p>
	 * The returned data has its 'progressive' attribute unset (i.e. false).
	 *
	 * <p>
	 * This method just calls 'getInternCompData(blk, n)'.
	 *
	 * <p>
	 * When an I/O exception is encountered the JJ2KExceptionHandler is used.
	 * The exception is passed to its handleException method. The action that is
	 * taken depends on the action that has been registered in
	 * JJ2KExceptionHandler. See JJ2KExceptionHandler for details.
	 *
	 * @param blk Its coordinates and dimensions specify the area to return. If
	 *            it contains a non-null data array, then it must have the
	 *            correct dimensions. If it contains a null data array a new one
	 *            is created. The fields in this object are modified to return
	 *            the data.
	 * @param c   The index of the component from which to get the data. Only 0
	 *            is valid.
	 * @return The requested DataBlk
	 * @see #getInternCompData
	 * @see JJ2KExceptionHandler
	 */
	@Override
	public DataBlk getCompData ( final DataBlk blk , final int c ) {
		return this.getInternCompData ( blk , c );
	}

	/**
	 * Returns a byte read from the RandomAccessIO. The number of read byted are
	 * counted to keep track of the offset of the pixel data in the PGM file
	 *
	 * @return One byte read from the header of the PGM file.
	 * @throws IOException  If an I/O error occurs.
	 * @throws EOFException If an EOF is read
	 */
	private byte countedByteRead ( ) throws IOException {
		this.offset++;
		return this.in.readByte ( );
	}

	/**
	 * Checks that the RandomAccessIO begins with 'P5'
	 *
	 * @throws IOException  If an I/O error occurs.
	 * @throws EOFException If an EOF is read
	 */
	private void confirmFileType ( ) throws IOException {
		final byte[] type = { 80 , 53 }; // 'P5'
		int i;
		byte b;

		for ( i = 0; 2 > i ; i++ ) {
			b = this.countedByteRead ( );
			if ( b != type[ i ] ) {
				if ( 1 == i && 50 == b ) { // i.e 'P2'
					throw new IllegalArgumentException ( "JJ2000 does not support" + " ascii-PGM files. Use "
							+ " raw-PGM file instead. " );
				}
				throw new IllegalArgumentException ( "Not a raw-PGM file" );
			}
		}
	}

	/**
	 * Skips any line in the header starting with '#' and any space, tab, line
	 * feed or carriage return.
	 *
	 * @throws IOException  If an I/O error occurs.
	 * @throws EOFException if an EOF is read
	 */
	private void skipCommentAndWhiteSpace ( ) throws IOException {

		boolean done = false;
		byte b;

		while ( ! done ) {
			b = this.countedByteRead ( );
			if ( 35 == b ) { // Comment start
				while ( 10 != b && 13 != b ) { // Comment ends in end of line
					b = this.countedByteRead ( );
				}
			}
			else if ( ! ( 9 == b || 10 == b || 13 == b || 32 == b ) ) { // If not whitespace
				done = true;
			}
		}
		// Put last valid byte in
		this.offset--;
		this.in.seek ( this.offset );
	}

	/**
	 * Returns an int read from the header of the PGM file.
	 *
	 * @return One int read from the header of the PGM file.
	 * @throws IOException  If an I/O error occurs.
	 * @throws EOFException If an EOF is read
	 */
	private int readHeaderInt ( ) throws IOException {
		int res = 0;
		byte b = 0;

		b = this.countedByteRead ( );
		while ( 32 != b && 10 != b && 9 != b && 13 != b ) { // While not whitespace
			res = res * 10 + b - 48; // Covert ASCII to numerical value
			b = this.countedByteRead ( );
		}
		return res;
	}

	/**
	 * Returns true if the data read was originally signed in the specified
	 * component, false if not. This method returns always false since PGM data
	 * is always unsigned.
	 *
	 * @param c The index of the component, from 0 to N-1.
	 * @return always false, since PGM data is always unsigned.
	 */
	@Override
	public boolean isOrigSigned ( final int c ) {
		// Check component index
		if ( 0 != c )
			throw new IllegalArgumentException ( );
		return false;
	}

	/**
	 * Returns a string of information about the object, more than 1 line long.
	 * The information string includes information from the underlying
	 * RandomAccessIO (its toString() method is called in turn).
	 *
	 * @return A string of information about the object.
	 */
	@Override
	public String toString ( ) {
		return "ImgReaderPGM: WxH = " + this.w + "x" + this.h + ", Component = 0" + "\nUnderlying RandomAccessIO:\n"
				+ this.in.toString ( );
	}
}
