/*****************************************************************************
 *
 * $Id: JP2Box.java,v 1.1 2002/07/25 14:50:47 grosbois Exp $
 *
 * Copyright Eastman Kodak Company, 343 State Street, Rochester, NY 14650
 * $Date $
 ****************************************************************************/

package dev.zontreck.harbinger.thirdparty.jj2000.j2k.colorspace.boxes;

import dev.zontreck.harbinger.thirdparty.jj2000.j2k.colorspace.ColorSpaceException;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.fileformat.FileFormatBoxes;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.icc.ICCProfile;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.io.RandomAccessIO;

import java.io.IOException;
import java.util.Hashtable;

/**
 * The abstract super class modeling the aspects of a JP2 box common to all such
 * boxes.
 *
 * @author Bruce A. Kern
 * @version 1.0
 */
public abstract class JP2Box {
	/**
	 * Platform dependant line terminator
	 */
	public static final String eol = System.getProperty ( "line.separator" );
	/**
	 * Box type
	 */
	public static int type;
	/**
	 * Length of the box.
	 */
	public int length;
	/**
	 * input file
	 */
	protected RandomAccessIO in;
	/**
	 * offset to start of box
	 */
	protected int boxStart;
	/**
	 * offset to end of box
	 */
	protected int boxEnd;
	/**
	 * offset to start of data in box
	 */
	protected int dataStart;

	protected JP2Box ( ) throws ColorSpaceException {
		try {
			throw new ColorSpaceException ( "JP2Box empty ctor called!!" );
		} catch ( final ColorSpaceException e ) {
			e.printStackTrace ( );
			throw e;
		}
	}

	/**
	 * Construct a JP2Box from an input image.
	 *
	 * @param in       RandomAccessIO jp2 image
	 * @param boxStart offset to the start of the box in the image
	 * @throws IOException , ColorSpaceException
	 */
	protected JP2Box ( final RandomAccessIO in , final int boxStart ) throws IOException, ColorSpaceException {
		final byte[] boxHeader = new byte[ 16 ];

		this.in = in;
		this.boxStart = boxStart;

		this.in.seek ( this.boxStart );
		this.in.readFully ( boxHeader , 0 , 8 );

		dataStart = boxStart + 8;
		length = ICCProfile.getInt ( boxHeader , 0 );
		boxEnd = boxStart + this.length;
		if ( 1 == length )
			throw new ColorSpaceException ( "extended length boxes not supported" );
	}

	/**
	 * Return a String representation of the Box type.
	 */
	public static String getTypeString ( final int t ) {
		return BoxType.get ( t );
	}

	/**
	 * Return the box type as a String.
	 */
	public String getTypeString ( ) {
		return BoxType.get ( JP2Box.type );
	}

	/**
	 * JP2 Box structure analysis help
	 */
	protected static class BoxType extends Hashtable<Integer, String> {
		private static final long serialVersionUID = 1L;

		private static final Hashtable<Integer, String> map = new Hashtable<Integer, String> ( );

		static {
			BoxType.put ( FileFormatBoxes.BITS_PER_COMPONENT_BOX , "BITS_PER_COMPONENT_BOX" );
			BoxType.put ( FileFormatBoxes.CAPTURE_RESOLUTION_BOX , "CAPTURE_RESOLUTION_BOX" );
			BoxType.put ( FileFormatBoxes.CHANNEL_DEFINITION_BOX , "CHANNEL_DEFINITION_BOX" );
			BoxType.put ( FileFormatBoxes.COLOUR_SPECIFICATION_BOX , "COLOUR_SPECIFICATION_BOX" );
			BoxType.put ( FileFormatBoxes.COMPONENT_MAPPING_BOX , "COMPONENT_MAPPING_BOX" );
			BoxType.put ( FileFormatBoxes.CONTIGUOUS_CODESTREAM_BOX , "CONTIGUOUS_CODESTREAM_BOX" );
			BoxType.put ( FileFormatBoxes.DEFAULT_DISPLAY_RESOLUTION_BOX , "DEFAULT_DISPLAY_RESOLUTION_BOX" );
			BoxType.put ( FileFormatBoxes.FILE_TYPE_BOX , "FILE_TYPE_BOX" );
			BoxType.put ( FileFormatBoxes.IMAGE_HEADER_BOX , "IMAGE_HEADER_BOX" );
			BoxType.put ( FileFormatBoxes.INTELLECTUAL_PROPERTY_BOX , "INTELLECTUAL_PROPERTY_BOX" );
			BoxType.put ( FileFormatBoxes.JP2_HEADER_BOX , "JP2_HEADER_BOX" );
			BoxType.put ( FileFormatBoxes.JP2_SIGNATURE_BOX , "JP2_SIGNATURE_BOX" );
			BoxType.put ( FileFormatBoxes.PALETTE_BOX , "PALETTE_BOX" );
			BoxType.put ( FileFormatBoxes.RESOLUTION_BOX , "RESOLUTION_BOX" );
			BoxType.put ( FileFormatBoxes.URL_BOX , "URL_BOX" );
			BoxType.put ( FileFormatBoxes.UUID_BOX , "UUID_BOX" );
			BoxType.put ( FileFormatBoxes.UUID_INFO_BOX , "UUID_INFO_BOX" );
			BoxType.put ( FileFormatBoxes.UUID_LIST_BOX , "UUID_LIST_BOX" );
			BoxType.put ( FileFormatBoxes.XML_BOX , "XML_BOX" );
		}

		private static void put ( final int type , final String desc ) {
			BoxType.map.put ( Integer.valueOf ( type ) , desc );
		}

		public static String get ( final int type ) {
			return BoxType.map.get ( Integer.valueOf ( type ) );
		}

		/* end class BoxType */
	}

	/* end class JP2Box */
}
