/*
 * CVS identifier:
 *
 * $Id: ImgWriterPPM.java,v 1.16 2002/07/25 15:10:14 grosbois Exp $
 *
 * Class:                   ImgWriterRawPPM
 *
 * Description:             Image writer for unsigned 8 bit data in
 *                          PPM file format.
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
package dev.zontreck.harbinger.thirdparty.jj2000.j2k.image.output;

import dev.zontreck.harbinger.thirdparty.jj2000.j2k.image.BlkImgDataSrc;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.image.DataBlk;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.image.DataBlkInt;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;

/**
 * This class writes 3 components from an image in 8 bit unsigned data to a
 * binary PPM file.
 *
 * <p>
 * The size of the image that is written is the size of the source image. No
 * component subsampling is allowed in any of the components that are written to
 * the file.
 *
 * <p>
 * Before writing, all coefficients are inversly level-shifted and then
 * "saturated" (they are limited * to the nominal dynamic range).<br>
 *
 * <u>Ex:</u> if the nominal range is 0-255, the following algorithm is applied:
 * <br>
 *
 * <tt>if coeff<0, output=0<br>
 * <p>
 * if coeff>255, output=255<br>
 * <p>
 * else output=coeff</tt>
 * <p>
 * The write() methods of an object of this class may not be called concurrently
 * from different threads.
 *
 * <p>
 * NOTE: This class is not thread safe, for reasons of internal buffering.
 */
public class ImgWriterPPM extends ImgWriter {

	/**
	 * Value used to inverse level shift. One for each component
	 */
	private final int[] levShift = new int[ 3 ];
	/**
	 * The array of indexes of the components from where to get the data
	 */
	private final int[] cps = new int[ 3 ];
	/**
	 * The array of the number of fractional bits in the components of the
	 * source data
	 */
	private final int[] fb = new int[ 3 ];
	/**
	 * Where to write the data
	 */
	private RandomAccessFile out;
	/**
	 * A DataBlk, just used to avoid allocating a new one each time it is needed
	 */
	private DataBlkInt db = new DataBlkInt ( );

	/**
	 * The offset of the raw pixel data in the PPM file
	 */
	private int offset;

	/**
	 * The line buffer.
	 */
	// This makes the class not thrad safe
	// (but it is not the only one making it so)
	private byte[] buf;

	/**
	 * Creates a new writer to the specified File object, to write data from the
	 * specified component.
	 *
	 * <p>
	 * The three components that will be written as R, G and B must be specified
	 * through the b1, b2 and b3 arguments.
	 *
	 * @param out    The file where to write the data
	 * @param imgSrc The source from where to get the image data to write.
	 * @param n1     The index of the first component from where to get the data,
	 *               that will be written as the red channel.
	 * @param n2     The index of the second component from where to get the data,
	 *               that will be written as the green channel.
	 * @param n3     The index of the third component from where to get the data,
	 *               that will be written as the green channel.
	 * @see DataBlk
	 */
	public ImgWriterPPM ( final File out , final BlkImgDataSrc imgSrc , final int n1 , final int n2 , final int n3 ) throws IOException {
		// Check that imgSrc is of the correct type
		// Check that the component index is valid
		if ( ( 0 > n1 ) || ( n1 >= imgSrc.getNumComps ( ) ) || ( 0 > n2 ) || ( n2 >= imgSrc.getNumComps ( ) ) || ( 0 > n3 )
				|| ( n3 >= imgSrc.getNumComps ( ) ) || ( 8 < imgSrc.getNomRangeBits ( n1 ) ) || ( 8 < imgSrc.getNomRangeBits ( n2 ) )
				|| ( 8 < imgSrc.getNomRangeBits ( n3 ) ) ) {
			throw new IllegalArgumentException ( "Invalid component indexes" );
		}
		// Initialize
		this.w = imgSrc.getCompImgWidth ( n1 );
		this.h = imgSrc.getCompImgHeight ( n1 );
		// Check that all components have same width and height
		if ( this.w != imgSrc.getCompImgWidth ( n2 ) || this.w != imgSrc.getCompImgWidth ( n3 ) || this.h != imgSrc.getCompImgHeight ( n2 )
				|| this.h != imgSrc.getCompImgHeight ( n3 ) ) {
			throw new IllegalArgumentException ( "All components must have the" + " same dimensions and no"
					+ " subsampling" );
		}
		this.w = imgSrc.getImgWidth ( );
		this.h = imgSrc.getImgHeight ( );

		// Continue initialization
		if ( out.exists ( ) && ! out.delete ( ) ) {
			throw new IOException ( "Could not reset file" );
		}
		this.out = new RandomAccessFile ( out , "rw" );
		this.src = imgSrc;
		this.cps[ 0 ] = n1;
		this.cps[ 1 ] = n2;
		this.cps[ 2 ] = n3;
		this.fb[ 0 ] = imgSrc.getFixedPoint ( n1 );
		this.fb[ 1 ] = imgSrc.getFixedPoint ( n2 );
		this.fb[ 2 ] = imgSrc.getFixedPoint ( n3 );

		this.levShift[ 0 ] = 1 << ( imgSrc.getNomRangeBits ( n1 ) - 1 );
		this.levShift[ 1 ] = 1 << ( imgSrc.getNomRangeBits ( n2 ) - 1 );
		this.levShift[ 2 ] = 1 << ( imgSrc.getNomRangeBits ( n3 ) - 1 );

		this.writeHeaderInfo ( );
	}

	/**
	 * Creates a new writer to the specified file, to write data from the
	 * specified component.
	 *
	 * <p>
	 * The three components that will be written as R, G and B must be specified
	 * through the b1, b2 and b3 arguments.
	 *
	 * @param fname  The name of the file where to write the data
	 * @param imgSrc The source from where to get the image data to write.
	 * @param n1     The index of the first component from where to get the data,
	 *               that will be written as the red channel.
	 * @param n2     The index of the second component from where to get the data,
	 *               that will be written as the green channel.
	 * @param n3     The index of the third component from where to get the data,
	 *               that will be written as the green channel.
	 * @see DataBlk
	 */
	public ImgWriterPPM ( final String fname , final BlkImgDataSrc imgSrc , final int n1 , final int n2 , final int n3 ) throws IOException {
		this ( new File ( fname ) , imgSrc , n1 , n2 , n3 );
	}

	/**
	 * Closes the underlying file or netwrok connection to where the data is
	 * written. Any call to other methods of the class become illegal after a
	 * call to this one.
	 *
	 * @throws IOException If an I/O error occurs.
	 */
	@Override
	public void close ( ) throws IOException {
		int i;
		// Finish writing the file, writing 0s at the end if the data at end
		// has not been written.
		if ( this.out.length ( ) != 3L * this.w * this.h + this.offset ) {
			// Goto end of file
			this.out.seek ( this.out.length ( ) );
			// Fill with 0s n all the components
			for ( i = 3 * this.w * this.h + this.offset - ( int ) this.out.length ( ); 0 < i ; i-- ) {
				this.out.writeByte ( 0 );
			}
		}
		this.out.close ( );
		this.src = null;
		this.out = null;
		this.db = null;
	}

	/**
	 * Writes all buffered data to the file or resource.
	 *
	 * @throws IOException If an I/O error occurs.
	 */
	@Override
	public void flush ( ) throws IOException {
		// No flush needed here since we are using a RandomAccessFile Get rid
		// of line buffer (is this a good choice?)
		this.buf = null;
	}

	/**
	 * Writes the data of the specified area to the file, coordinates are
	 * relative to the current tile of the source. Before writing, the
	 * coefficients are limited to the nominal range.
	 *
	 * <p>
	 * This method may not be called concurrently from different threads.
	 *
	 * <p>
	 * If the data returned from the BlkImgDataSrc source is progressive, then
	 * it is requested over and over until it is not progressive anymore.
	 *
	 * @param ulx The horizontal coordinate of the upper-left corner of the area
	 *            to write, relative to the current tile.
	 * @param uly The vertical coordinate of the upper-left corner of the area
	 *            to write, relative to the current tile.
	 * @param w   The width of the area to write.
	 * @param h   The height of the area to write.
	 * @throws IOException If an I/O error occurs.
	 */
	@Override
	public void write ( final int ulx , final int uly , final int w , final int h ) throws IOException {
		int k, j, i, c;
		// In local variables for faster access
		int fracbits;
		// variables used during coeff saturation
		int shift, tmp, maxVal;
		final int tOffx;  // Active tile offset in the X and Y direction
		final int tOffy;

		// Active tiles in all components have same offset since they are at
		// same resolution (PPM does not support anything else)
		tOffx = this.src.getCompULX ( this.cps[ 0 ] ) - ( int ) Math.ceil ( this.src.getImgULX ( ) / ( double ) this.src.getCompSubsX ( this.cps[ 0 ] ) );
		tOffy = this.src.getCompULY ( this.cps[ 0 ] ) - ( int ) Math.ceil ( this.src.getImgULY ( ) / ( double ) this.src.getCompSubsY ( this.cps[ 0 ] ) );

		// Check the array size
		if ( null != db.data && this.db.data.length < w ) {
			// A new one will be allocated by getInternCompData()
			this.db.data = null;
		}

		// Check the line buffer
		if ( null == buf || this.buf.length < 3 * w ) {
			this.buf = new byte[ 3 * w ];
		}

		// Write the data to the file
		// Write line by line
		for ( i = 0; i < h ; i++ ) {
			// Write into buffer first loop over the three components and
			// write for each
			for ( c = 0; 3 > c ; c++ ) {
				maxVal = ( 1 << this.src.getNomRangeBits ( this.cps[ c ] ) ) - 1;
				shift = this.levShift[ c ];

				// Initialize db
				this.db.ulx = ulx;
				this.db.uly = uly + i;
				this.db.w = w;
				this.db.h = 1;

				// Request the data and make sure it is not progressive
				do {
					this.db = ( DataBlkInt ) this.src.getInternCompData ( this.db , this.cps[ c ] );
				} while ( this.db.progressive );
				// Get the fracbits value
				fracbits = this.fb[ c ];
				// Write all bytes in the line
				if ( 0 == fracbits ) {
					for ( k = this.db.offset + w - 1 , j = 3 * w - 1 + c - 2; 0 <= j ; k-- ) {
						tmp = this.db.data[ k ] + shift;
						this.buf[ j ] = ( byte ) ( ( 0 > tmp ) ? 0 : ( ( tmp > maxVal ) ? maxVal : tmp ) );
						j -= 3;
					}
				}
				else {
					for ( k = this.db.offset + w - 1 , j = 3 * w - 1 + c - 2; 0 <= j ; k-- ) {
						tmp = ( this.db.data[ k ] >>> fracbits ) + shift;
						this.buf[ j ] = ( byte ) ( ( 0 > tmp ) ? 0 : ( ( tmp > maxVal ) ? maxVal : tmp ) );
						j -= 3;
					}
				}
			}
			// Write buffer into file
			this.out.seek ( this.offset + 3 * ( ( long ) this.w * ( uly + tOffy + i ) + ulx + tOffx ) );
			this.out.write ( this.buf , 0 , 3 * w );
		}
	}

	/**
	 * Writes the source's current tile to the output. The requests of data
	 * issued to the source BlkImgDataSrc object are done by strips, in order to
	 * reduce memory usage.
	 *
	 * <p>
	 * If the data returned from the BlkImgDataSrc source is progressive, then
	 * it is requested over and over until it is not progressive any more.
	 *
	 * @throws IOException If an I/O error occurs.
	 */
	@Override
	public void write ( ) throws IOException {
		int i;
		final int tIdx = this.src.getTileIdx ( );
		final int tw = this.src.getTileCompWidth ( tIdx , 0 ); // Tile width
		final int th = this.src.getTileCompHeight ( tIdx , 0 ); // Tile height
		// Write in strips
		for ( i = 0; i < th ; i += ImgWriter.DEF_STRIP_HEIGHT ) {
			this.write ( 0 , i , tw , ( ImgWriter.DEF_STRIP_HEIGHT > ( th - i ) ) ? th - i : ImgWriter.DEF_STRIP_HEIGHT );
		}
	}

	/**
	 * Writes the header info of the PPM file :
	 * <p>
	 * P6<br>
	 * <p>
	 * width height<br>
	 * <p>
	 * 255<br>
	 *
	 * @throws IOException If there is an I/O Error
	 */
	private void writeHeaderInfo ( ) throws IOException {
		byte[] byteVals;
		int i;
		String val;

		// write 'P6' to file
		this.out.seek ( 0 );
		this.out.write ( 80 );
		this.out.write ( 54 );
		this.out.write ( 10 ); // new line
		this.offset = 3;
		// Write width in ASCII
		val = String.valueOf ( this.w );
		byteVals = val.getBytes ( StandardCharsets.UTF_8 );
		for ( i = 0; i < byteVals.length ; i++ ) {
			this.out.write ( byteVals[ i ] );
			this.offset++;
		}
		this.out.write ( 32 ); // blank
		this.offset++;
		// Write height in ASCII
		val = String.valueOf ( this.h );
		byteVals = val.getBytes ( StandardCharsets.UTF_8 );
		for ( i = 0; i < byteVals.length ; i++ ) {
			this.out.write ( byteVals[ i ] );
			this.offset++;
		}

		this.out.write ( 10 ); // newline
		this.out.write ( 50 ); // '2'
		this.out.write ( 53 ); // '5'
		this.out.write ( 53 ); // '5'
		this.out.write ( 10 ); // newline
		this.offset += 5;
	}

	/**
	 * Returns a string of information about the object, more than 1 line long.
	 * The information string includes information from the underlying
	 * RandomAccessFile (its toString() method is called in turn).
	 *
	 * @return A string of information about the object.
	 */
	@Override
	public String toString ( ) {
		return "ImgWriterPPM: WxH = " + this.w + "x" + this.h + ", Components = " + this.cps[ 0 ] + "," + this.cps[ 1 ] + "," + this.cps[ 2 ]
				+ "\nUnderlying RandomAccessFile:\n" + this.out.toString ( );
	}
}
