/*
 * CVS identifier:
 *
 * $Id: FileFormatReader.java,v 1.16 2002/07/25 14:04:08 grosbois Exp $
 *
 * Class:                   FileFormatReader
 *
 * Description:             Reads the file format
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
package dev.zontreck.harbinger.thirdparty.jj2000.j2k.fileformat.reader;

import dev.zontreck.harbinger.thirdparty.jj2000.j2k.codestream.Markers;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.fileformat.FileFormatBoxes;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.io.RandomAccessIO;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.util.FacilityManager;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.util.MsgLogger;

import java.io.EOFException;
import java.io.IOException;
import java.util.Vector;

/**
 * This class reads the file format wrapper that may or may not exist around a
 * valid JPEG 2000 codestream. Since no information from the file format is used
 * in the actual decoding, this class simply goes through the file and finds the
 * first valid codestream.
 *
 * @see dev.zontreck.harbinger.thirdparty.jj2000.j2k.fileformat.writer.FileFormatWriter
 */
public class FileFormatReader implements FileFormatBoxes {

	/**
	 * The random access from which the file format boxes are read
	 */
	private final RandomAccessIO in;
	/**
	 * Flag indicating whether or not the JP2 file format is used
	 */
	public boolean JP2FFUsed;
	/**
	 * The positions of the codestreams in the fileformat
	 */
	private Vector<Integer> codeStreamPos;
	/**
	 * The lengths of the codestreams in the fileformat
	 */
	private Vector<Integer> codeStreamLength;

	/**
	 * The constructor of the FileFormatReader
	 *
	 * @param in The RandomAccessIO from which to read the file format
	 */
	public FileFormatReader ( final RandomAccessIO in ) {
		this.in = in;
	}

	/**
	 * This method checks whether the given RandomAccessIO is a valid JP2 file
	 * and if so finds the first codestream in the file. Currently, the
	 * information in the codestream is not used
	 *
	 * @throws java.io.IOException  If an I/O error occurred.
	 * @throws java.io.EOFException If end of file is reached
	 */
	public void readFileFormat ( ) throws IOException {

		// int foundCodeStreamBoxes=0;
		int box;
		int length;
		long longLength = 0;
		int pos;
		final short marker;
		boolean jp2HeaderBoxFound = false;
		boolean lastBoxFound = false;

		try {

			// Go through the randomaccessio and find the first contiguous
			// codestream box. Check also that the File Format is correct

			// Make sure that the first 12 bytes is the JP2_SIGNATURE_BOX or
			// if not that the first 2 bytes is the SOC marker
			if ( 0x0000000c != in.readInt ( ) || FileFormatBoxes.JP2_SIGNATURE_BOX != in.readInt ( ) || 0x0d0a870a != in.readInt ( ) ) { // Not a JP2 file
				this.in.seek ( 0 );

				marker = this.in.readShort ( );
				if ( Markers.SOC != marker ) // Standard syntax marker found
					throw new Error ( "File is neither valid JP2 file nor valid JPEG 2000 codestream" );
				this.JP2FFUsed = false;
				this.in.seek ( 0 );
				return;
			}

			// The JP2 File format is being used
			this.JP2FFUsed = true;

			// Read File Type box
			if ( ! this.readFileTypeBox ( ) ) {
				// Not a valid JP2 file or codestream
				throw new Error ( "Invalid JP2 file: File Type box missing" );
			}

			// Read all remaining boxes
			while ( ! lastBoxFound ) {
				pos = this.in.getPos ( );
				length = this.in.readInt ( );
				if ( ( pos + length ) == this.in.length ( ) )
					lastBoxFound = true;

				box = this.in.readInt ( );
				if ( 0 == length ) {
					lastBoxFound = true;
					length = this.in.length ( ) - this.in.getPos ( );
				}
				else if ( 1 == length ) {
					longLength = this.in.readLong ( );
					throw new IOException ( "File too long." );
				}
				else
					longLength = 0;

				switch ( box ) {
					case FileFormatBoxes.CONTIGUOUS_CODESTREAM_BOX:
						if ( ! jp2HeaderBoxFound ) {
							throw new Error ( "Invalid JP2 file: JP2Header box not "
									+ "found before Contiguous codestream box " );
						}
						this.readContiguousCodeStreamBox ( pos , length , longLength );
						break;
					case FileFormatBoxes.JP2_HEADER_BOX:
						if ( jp2HeaderBoxFound )
							throw new Error ( "Invalid JP2 file: Multiple JP2Header boxes found" );
						this.readJP2HeaderBox ( pos , length , longLength );
						jp2HeaderBoxFound = true;
						break;
					case FileFormatBoxes.INTELLECTUAL_PROPERTY_BOX:
						this.readIntPropertyBox ( length );
						break;
					case FileFormatBoxes.XML_BOX:
						this.readXMLBox ( length );
						break;
					case FileFormatBoxes.UUID_BOX:
						this.readUUIDBox ( length );
						break;
					case FileFormatBoxes.UUID_INFO_BOX:
						this.readUUIDInfoBox ( length );
						break;
					default:
						FacilityManager.getMsgLogger ( ).printmsg (
								MsgLogger.WARNING ,
								"Unknown box-type: 0x" + Integer.toHexString ( box )
						);
				}
				if ( ! lastBoxFound )
					this.in.seek ( pos + length );
			}
		} catch ( final EOFException e ) {
			throw new Error ( "EOF reached before finding Contiguous Codestream Box" );
		}

		if ( 0 == codeStreamPos.size ( ) ) {
			// Not a valid JP2 file or codestream
			throw new Error ( "Invalid JP2 file: Contiguous codestream box missing" );
		}

	}

	/**
	 * This method reads the File Type box.
	 *
	 * @return false if the File Type box was not found or invalid else true
	 * @throws java.io.IOException  If an I/O error occurred.
	 * @throws java.io.EOFException If the end of file was reached
	 */
	public boolean readFileTypeBox ( ) throws IOException {
		final int length;
		final int nComp;
		boolean foundComp = false;

		// Read box length (LBox)
		length = this.in.readInt ( );
		if ( 0 == length ) { // This can not be last box
			throw new Error ( "Zero-length of Profile Box" );
		}

		// Check that this is a File Type box (TBox)
		if ( FileFormatBoxes.FILE_TYPE_BOX != in.readInt ( ) ) {
			return false;
		}

		// Check for XLBox
		if ( 1 == length ) { // Box has 8 byte length;
			throw new IOException ( "File too long." );
		}

		// Read Brand field
		this.in.readInt ( );

		// Read MinV field
		this.in.readInt ( );

		// Check that there is at least one FT_BR entry in in
		// compatibility list
		nComp = ( length - 16 ) / 4; // Number of compatibilities.
		for ( int i = nComp ; 0 < i ; i-- ) {
			if ( FileFormatBoxes.FT_BR == in.readInt ( ) )
				foundComp = true;
		}
		return foundComp;
	}

	/**
	 * This method reads the JP2Header box
	 *
	 * @param pos    The position in the file
	 * @param length The length of the JP2Header box
	 * @param long   length The length of the JP2Header box if greater than 1<<32
	 * @return false if the JP2Header box was not found or invalid else true
	 * @throws java.io.IOException  If an I/O error occurred.
	 * @throws java.io.EOFException If the end of file was reached
	 */
	public boolean readJP2HeaderBox ( final long pos , final int length , final long longLength ) throws IOException {

		if ( 0 == length ) { // This can not be last box
			throw new Error ( "Zero-length of JP2Header Box" );
		}

		// Here the JP2Header data (DBox) would be read if we were to use it

		return true;
	}

	/**
	 * This method skips the Contiguous codestream box and adds position of
	 * contiguous codestream to a vector
	 *
	 * @param pos    The position in the file
	 * @param length The length of the JP2Header box
	 * @param long   length The length of the JP2Header box if greater than 1<<32
	 * @return false if the Contiguous codestream box was not found or invalid
	 * else true
	 * @throws java.io.IOException  If an I/O error occurred.
	 * @throws java.io.EOFException If the end of file was reached
	 */
	public boolean readContiguousCodeStreamBox ( final long pos , final int length , final long longLength ) throws IOException {

		// Add new codestream position to position vector
		final int ccpos = this.in.getPos ( );

		if ( null == codeStreamPos )
			this.codeStreamPos = new Vector<Integer> ( );
		this.codeStreamPos.addElement ( Integer.valueOf ( ccpos ) );

		// Add new codestream length to length vector
		if ( null == codeStreamLength )
			this.codeStreamLength = new Vector<Integer> ( );
		this.codeStreamLength.addElement ( Integer.valueOf ( length ) );

		return true;
	}

	/**
	 * This method reads the contents of the Intellectual property box
	 */
	public void readIntPropertyBox ( final int length ) {
	}

	/**
	 * This method reads the contents of the XML box
	 */
	public void readXMLBox ( final int length ) {
	}

	/**
	 * This method reads the contents of the Intellectual property box
	 */
	public void readUUIDBox ( final int length ) {
	}

	/**
	 * This method reads the contents of the Intellectual property box
	 */
	public void readUUIDInfoBox ( final int length ) {
	}

	/**
	 * This method creates and returns an array of positions to contiguous
	 * codestreams in the file
	 *
	 * @return The positions of the contiguous codestreams in the file
	 */
	public long[] getCodeStreamPos ( ) {
		final int size = this.codeStreamPos.size ( );
		final long[] pos = new long[ size ];
		for ( int i = 0 ; i < size ; i++ )
			pos[ i ] = this.codeStreamPos.elementAt ( i ).longValue ( );
		return pos;
	}

	/**
	 * This method returns the position of the first contiguous codestreams in
	 * the file
	 *
	 * @return The position of the first contiguous codestream in the file
	 */
	public int getFirstCodeStreamPos ( ) {
		return this.codeStreamPos.elementAt ( 0 ).intValue ( );
	}

	/**
	 * This method returns the length of the first contiguous codestreams in the
	 * file
	 *
	 * @return The length of the first contiguous codestream in the file
	 */
	public int getFirstCodeStreamLength ( ) {
		return this.codeStreamLength.elementAt ( 0 ).intValue ( );
	}

}
