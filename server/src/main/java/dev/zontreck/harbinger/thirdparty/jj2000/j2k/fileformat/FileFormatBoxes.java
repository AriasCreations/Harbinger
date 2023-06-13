/*
 * cvs identifier:
 *
 * $Id: FileFormatBoxes.java,v 1.10 2001/02/14 12:22:20 qtxjoas Exp $
 *
 * Class:                   FileFormatMarkers
 *
 * Description:             Contains definitions of boxes used in jp2 files
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
package dev.zontreck.harbinger.thirdparty.jj2000.j2k.fileformat;

/**
 * This class contains all the markers used in the JPEG 2000 Part I file format
 *
 * @see dev.zontreck.harbinger.thirdparty.jj2000.j2k.fileformat.writer.FileFormatWriter
 * @see dev.zontreck.harbinger.thirdparty.jj2000.j2k.fileformat.reader.FileFormatReader
 */
public interface FileFormatBoxes {
	/**
	 * Main boxes
	 */

	int JP2_SIGNATURE_BOX = 0x6a502020;

	int FILE_TYPE_BOX = 0x66747970;

	int JP2_HEADER_BOX = 0x6a703268;

	int CONTIGUOUS_CODESTREAM_BOX = 0x6a703263;

	int INTELLECTUAL_PROPERTY_BOX = 0x64703269;

	int XML_BOX = 0x786d6c20;

	int UUID_BOX = 0x75756964;

	int UUID_INFO_BOX = 0x75696e66;

	/**
	 * JP2 Header boxes
	 */
	int IMAGE_HEADER_BOX = 0x69686472;

	int BITS_PER_COMPONENT_BOX = 0x62706363;

	int COLOUR_SPECIFICATION_BOX = 0x636f6c72;

	int PALETTE_BOX = 0x70636c72;

	int COMPONENT_MAPPING_BOX = 0x636d6170;

	int CHANNEL_DEFINITION_BOX = 0x63646566;

	int RESOLUTION_BOX = 0x72657320;

	int CAPTURE_RESOLUTION_BOX = 0x72657363;

	int DEFAULT_DISPLAY_RESOLUTION_BOX = 0x72657364;

	/** End of JP2 Header boxes */

	/**
	 * UUID Info Boxes
	 */
	int UUID_LIST_BOX = 0x75637374;

	int URL_BOX = 0x75726c20;
	/** end of UUID Info boxes */

	/**
	 * Image Header Box Fields
	 */
	int IMB_VERS = 0x0100;

	byte IMB_C = 7;

	byte IMB_UnkC = 1;

	byte IMB_IPR = 0;
	/** end of Image Header Box Fields */

	/**
	 * Colour Specification Box Fields
	 */
	byte CSB_METH = 1;

	byte CSB_PREC = 0;

	byte CSB_APPROX = 0;

	int CSB_ENUM_SRGB = 16;

	int CSB_ENUM_GREY = 17;

	int CSB_ENUM_YCC = 18;
	/** en of Colour Specification Box Fields */

	/**
	 * File Type Fields
	 */
	int FT_BR = 0x6a703220;
}
