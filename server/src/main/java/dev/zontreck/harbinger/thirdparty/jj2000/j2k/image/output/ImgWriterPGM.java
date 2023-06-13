/*
 * CVS identifier:
 *
 * $Id: ImgWriterPGM.java,v 1.14 2002/07/19 14:13:38 grosbois Exp $
 *
 * Class:                   ImgWriterRawPGM
 *
 * Description:             Image writer for unsigned 8 bit data in
 *                          PGM file format.
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
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.util.FacilityManager;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;

/**
 * This class writes a component from an image in 8 bit unsigned data to a
 * binary PGM file. The size of the image that is written to the file is the
 * size of the component from which to get the data, not the size of the source
 * image (they differ if there is some sub-sampling).
 *
 * <p>
 * Before writing, all coefficients are inversly level shifted and then
 * "saturated" (they are limited to the nominal dynamic range).<br>
 * <u>Ex:</u> if the nominal range is 0-255, the following algorithm is applied:
 * <br>
 * <tt>if coeff<0, output=0<br> if coeff>255, output=255<br> else
 * output=coeff</tt>
 *
 * <p>
 * The write() methods of an object of this class may not be called concurrently
 * from different threads.
 *
 * <p>
 * NOTE: This class is not thread safe, for reasons of internal buffering.
 */
public class ImgWriterPGM extends ImgWriter {

	/**
	 * Value used to inverse level shift
	 */
	private final int levShift;
	/**
	 * The index of the component from where to get the data
	 */
	private final int c;
	/**
	 * The number of fractional bits in the source data
	 */
	private final int fb;
	/**
	 * Where to write the data
	 */
	private RandomAccessFile out;
	/**
	 * A DataBlk, just used to avoid allocating a new one each time it is needed
	 */
	private DataBlkInt db = new DataBlkInt ( );

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
	 * Creates a new writer to the specified File object, to write data from the
	 * specified component.
	 *
	 * <p>
	 * The size of the image that is written to the file is the size of the
	 * component from which to get the data, specified by b, not the size of the
	 * source image (they differ if there is some sub-sampling).
	 *
	 * @param out    The file where to write the data
	 * @param imgSrc The source from where to get the image data to write.
	 * @param c      The index of the component from where to get the data.
	 */
	public ImgWriterPGM ( final File out , final BlkImgDataSrc imgSrc , final int c ) throws IOException {
		// Check that imgSrc is of the correct type
		// Check that the component index is valid
		if ( 0 > c || c >= imgSrc.getNumComps ( ) ) {
			throw new IllegalArgumentException ( "Invalid number of components" );
		}

		// Check that imgSrc is of the correct type
		if ( 8 < imgSrc.getNomRangeBits ( c ) ) {
			FacilityManager.getMsgLogger ( ).println (
					"Warning: Component " + c + " has nominal bitdepth " + imgSrc.getNomRangeBits ( c )
							+ ". Pixel values will be " + "down-shifted to fit bitdepth of 8 for PGM file" , 8 , 8 );
		}

		// Initialize
		if ( out.exists ( ) && ! out.delete ( ) ) {
			throw new IOException ( "Could not reset file" );
		}
		this.out = new RandomAccessFile ( out , "rw" );
		this.src = imgSrc;
		this.c = c;
		this.w = imgSrc.getImgWidth ( );
		this.h = imgSrc.getImgHeight ( );
		this.fb = imgSrc.getFixedPoint ( c );
		this.levShift = 1 << ( imgSrc.getNomRangeBits ( c ) - 1 );

		this.writeHeaderInfo ( );
	}

	/**
	 * Creates a new writer to the specified file, to write data from the
	 * specified component.
	 *
	 * <p>
	 * The size of the image that is written to the file is the size of the
	 * component from which to get the data, specified by b, not the size of the
	 * source image (they differ if there is some sub-sampling).
	 *
	 * @param fname  The name of the file where to write the data
	 * @param imgSrc The source from where to get the image data to write.
	 * @param c      The index of the component from where to get the data.
	 */
	public ImgWriterPGM ( final String fname , final BlkImgDataSrc imgSrc , final int c ) throws IOException {
		this ( new File ( fname ) , imgSrc , c );
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
		if ( this.out.length ( ) != ( long ) this.w * this.h + this.offset ) {
			// Goto end of file
			this.out.seek ( this.out.length ( ) );
			// Fill with 0s
			for ( i = this.offset + this.w * this.h - ( int ) this.out.length ( ); 0 < i ; i-- ) {
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
		int k, i, j;
		final int fracbits = this.fb; // In local variable for faster access
		final int tOffx;  // Active tile offset in the X and Y direction
		final int tOffy;

		// Initialize db
		this.db.ulx = ulx;
		this.db.uly = uly;
		this.db.w = w;
		this.db.h = h;
		// Get the current active tile offset
		tOffx = this.src.getCompULX ( this.c ) - ( int ) Math.ceil ( this.src.getImgULX ( ) / ( double ) this.src.getCompSubsX ( this.c ) );
		tOffy = this.src.getCompULY ( this.c ) - ( int ) Math.ceil ( this.src.getImgULY ( ) / ( double ) this.src.getCompSubsY ( this.c ) );
		// Check the array size
		if ( null != db.data && this.db.data.length < w * h ) {
			// A new one will be allocated by getInternCompData()
			this.db.data = null;
		}
		// Request the data and make sure it is not
		// progressive
		do {
			this.db = ( DataBlkInt ) this.src.getInternCompData ( this.db , this.c );
		} while ( this.db.progressive );

		// variables used during coeff saturation
		int tmp;
		final int maxVal = ( 1 << this.src.getNomRangeBits ( this.c ) ) - 1;

		// If nominal bitdepth greater than 8, calculate down shift
		int downShift = this.src.getNomRangeBits ( this.c ) - 8;
		if ( 0 > downShift ) {
			downShift = 0;
		}

		// Check line buffer
		if ( null == buf || this.buf.length < w ) {
			this.buf = new byte[ w ]; // Expand buffer
		}

		// Write line by line
		for ( i = 0; i < h ; i++ ) {
			// Skip to beggining of line in file
			this.out.seek ( offset + ( long ) this.w * ( uly + tOffy + i ) + ulx + tOffx );
			// Write all bytes in the line
			if ( 0 == fracbits ) {
				for ( k = this.db.offset + i * this.db.scanw + w - 1 , j = w - 1; 0 <= j ; j-- , k-- ) {
					tmp = this.db.data[ k ] + this.levShift;
					this.buf[ j ] = ( byte ) ( ( ( 0 > tmp ) ? 0 : ( ( tmp > maxVal ) ? maxVal : tmp ) ) >> downShift );
				}
			}
			else {
				for ( k = this.db.offset + i * this.db.scanw + w - 1 , j = w - 1; 0 <= j ; j-- , k-- ) {
					tmp = ( this.db.data[ k ] >> fracbits ) + this.levShift;
					this.buf[ j ] = ( byte ) ( ( ( 0 > tmp ) ? 0 : ( ( tmp > maxVal ) ? maxVal : tmp ) ) >> downShift );
				}
			}
			this.out.write ( this.buf , 0 , w );
		}
	}

	/**
	 * Writes the source's current tile to the output. The requests of data
	 * issued to the source BlkImgDataSrc object are done by strips, in order to
	 * reduce memory usage.
	 *
	 * <p>
	 * If the data returned from the BlkImgDataSrc source is progressive, then
	 * it is requested over and over until it is not progressive anymore.
	 *
	 * @throws IOException If an I/O error occurs.
	 * @see DataBlk
	 */
	@Override
	public void write ( ) throws IOException {
		int i;
		final int tIdx = this.src.getTileIdx ( );
		final int tw = this.src.getTileCompWidth ( tIdx , this.c ); // Tile width
		final int th = this.src.getTileCompHeight ( tIdx , this.c ); // Tile height
		// Write in strips
		for ( i = 0; i < th ; i += ImgWriter.DEF_STRIP_HEIGHT ) {
			this.write ( 0 , i , tw , ( ImgWriter.DEF_STRIP_HEIGHT > th - i ) ? th - i : ImgWriter.DEF_STRIP_HEIGHT );
		}
	}

	/**
	 * Writes the header info of the PGM file :
	 * <p>
	 * P5 width height 255
	 *
	 * @throws IOException If there is an IOException
	 */
	private void writeHeaderInfo ( ) throws IOException {
		byte[] byteVals;
		int i;
		String val;

		// write 'P5' to file
		this.out.writeByte ( 'P' ); // 'P'
		this.out.write ( '5' ); // '5'
		this.out.write ( '\n' ); // newline
		this.offset = 3;
		// Write width in ASCII
		val = String.valueOf ( this.w );
		byteVals = val.getBytes ( StandardCharsets.UTF_8 );
		for ( i = 0; i < byteVals.length ; i++ ) {
			this.out.writeByte ( byteVals[ i ] );
			this.offset++;
		}
		this.out.write ( ' ' ); // blank
		this.offset++;
		// Write height in ASCII
		val = String.valueOf ( this.h );
		byteVals = val.getBytes ( StandardCharsets.UTF_8 );
		for ( i = 0; i < byteVals.length ; i++ ) {
			this.out.writeByte ( byteVals[ i ] );
			this.offset++;
		}
		// Write maxval
		this.out.write ( '\n' ); // newline
		this.out.write ( '2' ); // '2'
		this.out.write ( '5' ); // '5'
		this.out.write ( '5' ); // '5'
		this.out.write ( '\n' ); // newline
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
		return "ImgWriterPGM: WxH = " + this.w + "x" + this.h + ", Component=" + this.c + "\nUnderlying RandomAccessFile:\n"
				+ this.out.toString ( );
	}
}
