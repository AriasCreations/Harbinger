/*
 * cvs identifier:
 *
 * $Id: FileFormatWriter.java,v 1.13 2001/02/16 11:53:54 qtxjoas Exp $
 *
 * Class:                   FileFormatWriter
 *
 * Description:             Writes the file format
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
package dev.zontreck.harbinger.thirdparty.jj2000.j2k.fileformat.writer;

import dev.zontreck.harbinger.thirdparty.jj2000.j2k.fileformat.FileFormatBoxes;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;


/**
 * This class writes the file format wrapper that may or may not exist around a
 * valid JPEG 2000 codestream. This class writes the simple possible legal
 * fileformat
 *
 * @see dev.zontreck.harbinger.thirdparty.jj2000.j2k.fileformat.reader.FileFormatReader
 */
public class FileFormatWriter implements FileFormatBoxes {
	/**
	 * Length of Colour Specification Box
	 */
	private static final int CSB_LENGTH = 15;
	/**
	 * Length of File Type Box
	 */
	private static final int FTB_LENGTH = 20;
	/**
	 * Length of Image Header Box
	 */
	private static final int IHB_LENGTH = 22;
	/**
	 * base length of Bits Per Component box
	 */
	private static final int BPC_LENGTH = 8;
	/**
	 * The byte buffer to which to write the fileformat header
	 */
	private final DataOutputStream out;
	private final ByteArrayOutputStream baos;
	/**
	 * Image height
	 */
	private final int height;
	/**
	 * Image width
	 */
	private final int width;
	/**
	 * Number of components
	 */
	private final int nc;
	/**
	 * Bits per component
	 */
	private final int[] bpc;
	/**
	 * Flag indicating whether number of bits per component varies
	 */
	private boolean bpcVaries;

	/**
	 * The constructor of the FileFormatWriter. It receives all the information
	 * necessary about a codestream to generate a legal JP2 file
	 *
	 * @param os      The output stream to write the file format header into
	 * @param height  The height of the image
	 * @param width   The width of the image
	 * @param nc      The number of components
	 * @param bpc     The number of bits per component
	 * @param clength Length of codestream
	 * @throws IOException
	 */
	public FileFormatWriter ( final OutputStream os , final int height , final int width , final int nc , final int[] bpc , final int clength ) throws IOException {
		this.height = height;
		this.width = width;
		this.nc = nc;
		this.bpc = bpc;
		baos = new ByteArrayOutputStream ( );
		out = new DataOutputStream ( this.baos );

		this.bpcVaries = false;
		final int fixbpc = bpc[ 0 ];
		for ( int i = nc - 1 ; 0 < i ; i-- ) {
			if ( bpc[ i ] != fixbpc ) {
				this.bpcVaries = true;
				break;
			}
		}

		// Write the JP2_SIGNATURE_BOX
		this.out.writeInt ( 0x0000000c );
		this.out.writeInt ( FileFormatBoxes.JP2_SIGNATURE_BOX );
		this.out.writeInt ( 0x0d0a870a );

		// Write File Type box
		this.writeFileTypeBox ( );

		// Write JP2 Header box
		this.writeJP2HeaderBox ( );

		// Write JP2 Codestream header
		this.writeContiguousCodeStreamBoxHeader ( clength );

		os.write ( this.baos.toByteArray ( ) );
	}

	public int length ( ) {
		return this.baos.size ( );
	}

	/**
	 * This method writes the File Type box
	 *
	 * @throws java.io.IOException If an I/O error occurred.
	 */
	private void writeFileTypeBox ( ) throws IOException {
		// Write box length (LBox)
		// LBox(4) + TBox (4) + BR(4) + MinV(4) + CL(4) = 20
		this.out.writeInt ( FileFormatWriter.FTB_LENGTH );

		// Write File Type box (TBox)
		this.out.writeInt ( FileFormatBoxes.FILE_TYPE_BOX );

		// Write File Type data (DBox)
		// Write Brand box (BR)
		this.out.writeInt ( FileFormatBoxes.FT_BR );

		// Write Minor Version
		this.out.writeInt ( 0 );

		// Write Compatibility list
		this.out.writeInt ( FileFormatBoxes.FT_BR );

	}

	/**
	 * This method writes the JP2Header box
	 *
	 * @throws java.io.IOException If an I/O error occurred.
	 */
	private void writeJP2HeaderBox ( ) throws IOException {

		// Write box length (LBox)
		// if the number of bits per components varies, a bpcc box is written
		if ( this.bpcVaries )
			this.out.writeInt ( 8 + FileFormatWriter.IHB_LENGTH + FileFormatWriter.CSB_LENGTH + FileFormatWriter.BPC_LENGTH + this.nc );
		else
			this.out.writeInt ( 8 + FileFormatWriter.IHB_LENGTH + FileFormatWriter.CSB_LENGTH );

		// Write a JP2Header (TBox)
		this.out.writeInt ( FileFormatBoxes.JP2_HEADER_BOX );       // 4 bytes

		// Write image header box
		this.writeImageHeaderBox ( );             // 22 bytes

		// Write Colour Bpecification Box
		this.writeColourSpecificationBox ( );     // 15 Bytes

		// if the number of bits per components varies write bpcc box
		if ( this.bpcVaries )
			this.writeBitsPerComponentBox ( );    // 8 Byte + nc Bytes
	}

	/**
	 * This method writes the Bits Per Component box
	 *
	 * @throws java.io.IOException If an I/O error occurred.
	 */
	private void writeBitsPerComponentBox ( ) throws IOException {
		// Write box length (LBox)
		this.out.writeInt ( FileFormatWriter.BPC_LENGTH + this.nc );

		// Write a Bits Per Component box (TBox)
		this.out.writeInt ( FileFormatBoxes.BITS_PER_COMPONENT_BOX );

		// Write bpc fields
		for ( int i = 0 ; i < this.nc ; i++ ) {
			this.out.writeByte ( this.bpc[ i ] - 1 );
		}
	}

	/**
	 * This method writes the Colour Specification box
	 *
	 * @throws java.io.IOException If an I/O error occurred.
	 */
	private void writeColourSpecificationBox ( ) throws IOException {
		// Write box length (LBox)
		this.out.writeInt ( FileFormatWriter.CSB_LENGTH );

		// Write a Bits Per Component box (TBox)
		this.out.writeInt ( FileFormatBoxes.COLOUR_SPECIFICATION_BOX );

		// Write METH field
		this.out.writeByte ( FileFormatBoxes.CSB_METH );

		// Write PREC field
		this.out.writeByte ( FileFormatBoxes.CSB_PREC );

		// Write APPROX field
		this.out.writeByte ( FileFormatBoxes.CSB_APPROX );

		// Write EnumCS field
		if ( 1 < nc )
			this.out.writeInt ( FileFormatBoxes.CSB_ENUM_SRGB );
		else
			this.out.writeInt ( FileFormatBoxes.CSB_ENUM_GREY );
	}

	/**
	 * This method writes the Image Header box
	 *
	 * @throws java.io.IOException If an I/O error occurred.
	 */
	private void writeImageHeaderBox ( ) throws IOException {

		// Write box length
		this.out.writeInt ( FileFormatWriter.IHB_LENGTH );

		// Write ihdr box name
		this.out.writeInt ( FileFormatBoxes.IMAGE_HEADER_BOX );

		// Write HEIGHT field
		this.out.writeInt ( this.height );

		// Write WIDTH field
		this.out.writeInt ( this.width );

		// Write NC field
		this.out.writeShort ( this.nc );

		// Write BPC field
		// if the number of bits per component varies write 0xff else write
		// number of bits per components
		if ( this.bpcVaries )
			this.out.writeByte ( 0xff );
		else
			this.out.writeByte ( this.bpc[ 0 ] - 1 );

		// Write C field
		this.out.writeByte ( FileFormatBoxes.IMB_C );

		// Write UnkC field
		this.out.writeByte ( FileFormatBoxes.IMB_UnkC );

		// Write IPR field
		this.out.writeByte ( FileFormatBoxes.IMB_IPR );
	}

	/**
	 * This method writes the Contiguous codestream box header which is directly followed by the codestream data
	 * Call this function with the actual number of codestream data bytes after the codestream has been written
	 *
	 * @param clength The contiguous codestream length
	 * @throws java.io.IOException If an I/O error occurred.
	 */
	private void writeContiguousCodeStreamBoxHeader ( final int clength ) throws IOException {
		// Write box length (LBox)
		this.out.writeInt ( clength + 8 );

		// Write contiguous codestream box name (TBox)
		this.out.writeInt ( FileFormatBoxes.CONTIGUOUS_CODESTREAM_BOX );
	}
}
