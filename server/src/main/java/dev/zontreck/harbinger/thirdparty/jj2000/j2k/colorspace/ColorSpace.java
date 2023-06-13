/*****************************************************************************
 *
 * $Id: ColorSpace.java,v 1.2 2002/07/25 16:31:11 grosbois Exp $
 *
 * Copyright Eastman Kodak Company, 343 State Street, Rochester, NY 14650
 * $Date $
 ****************************************************************************/

package dev.zontreck.harbinger.thirdparty.jj2000.j2k.colorspace;

import dev.zontreck.harbinger.thirdparty.jj2000.j2k.codestream.reader.HeaderDecoder;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.colorspace.boxes.*;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.fileformat.FileFormatBoxes;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.icc.ICCProfile;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.io.RandomAccessIO;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.util.ParameterList;

import java.io.IOException;

/**
 * This class analyzes the image to provide dev.zontreck.harbinger.thirdparty.jj2000.j2k.colorspace information for the
 * decoding chain. It does this by examining the box structure of the JP2 image.
 * It also provides access to the parameter list information, which is stored as
 * a public final field.
 *
 * @author Bruce A. Kern
 * @version 1.0
 * @see dev.zontreck.harbinger.thirdparty.jj2000.j2k.icc.ICCProfile
 */
public class ColorSpace {
	public static final String eol = System.getProperty ( "line.separator" );
	/**
	 * method enumeration
	 */
	public static final MethodEnum ICC_PROFILED = new MethodEnum ( "profiled" );
	/**
	 * method enumeration
	 */
	public static final MethodEnum ENUMERATED = new MethodEnum ( "enumerated" );
	/**
	 * dev.zontreck.harbinger.thirdparty.jj2000.j2k.colorspace enumeration
	 */
	public static final CSEnum sRGB = new CSEnum ( "sRGB" );
	/**
	 * dev.zontreck.harbinger.thirdparty.jj2000.j2k.colorspace enumeration
	 */
	public static final CSEnum GreyScale = new CSEnum ( "GreyScale" );
	/**
	 * dev.zontreck.harbinger.thirdparty.jj2000.j2k.colorspace enumeration
	 */
	public static final CSEnum sYCC = new CSEnum ( "sYCC" );
	/**
	 * dev.zontreck.harbinger.thirdparty.jj2000.j2k.colorspace enumeration
	 */
	public static final CSEnum Illegal = new CSEnum ( "Illegal" );
	/**
	 * dev.zontreck.harbinger.thirdparty.jj2000.j2k.colorspace enumeration
	 */
	public static final CSEnum Unknown = new CSEnum ( "Unknown" );
	// Renamed for convenience:
	static final int GRAY = 0;
	static final int RED = 1;
	static final int GREEN = 2;
	static final int BLUE = 3;
	/**
	 * Input image
	 */
	private final RandomAccessIO in;
	/**
	 * Parameter Specs
	 */
	public ParameterList pl;
	/**
	 * Parameter Specs
	 */
	public HeaderDecoder hd;
	/* Image box structure as pertains to colorspacees. */
	private PaletteBox pbox;
	private ComponentMappingBox cmbox;
	private ColorSpecificationBox csbox;
	private ChannelDefinitionBox cdbox;
	private ImageHeaderBox ihbox;

	/**
	 * public constructor which takes in the image, parameterlist and the image
	 * header decoder as args.
	 *
	 * @param in input RandomAccess image file.
	 * @param hd provides information about the image header.
	 * @param pl provides parameters from the default and commandline lists.
	 * @throws IOException , ColorSpaceException
	 */
	public ColorSpace ( final RandomAccessIO in , final HeaderDecoder hd , final ParameterList pl ) throws IOException, ColorSpaceException {
		this.pl = pl;
		this.in = in;
		this.hd = hd;
		this.getBoxes ( );
	}

	/**
	 * Indent a String that contains newlines.
	 */
	public static String indent ( final String ident , final StringBuffer instr ) {
		return ColorSpace.indent ( ident , instr.toString ( ) );
	}

	/**
	 * Indent a String that contains newlines.
	 */
	public static String indent ( final String ident , final String instr ) {
		final StringBuffer tgt = new StringBuffer ( instr );
		final char eolChar = ColorSpace.eol.charAt ( 0 );
		int i = tgt.length ( );
		-- i;
		while ( 0 < i ) {
			if ( tgt.charAt ( i ) == eolChar )
				tgt.insert ( i + 1 , ident );
			-- i;
		}
		return ident + tgt;
	}

	/**
	 * Retrieve the ICC profile from the images as a byte array.
	 *
	 * @return the ICC Profile as a byte [].
	 */
	public byte[] getICCProfile ( ) {
		return this.csbox.getICCProfile ( );
	}

	/**
	 * Retrieve the various boxes from the JP2 file.
	 *
	 * @throws ColorSpaceException , IOException
	 */
	protected final void getBoxes ( ) throws ColorSpaceException, IOException {
		int type;
		long len = 0;
		int boxStart = 0;
		final byte[] boxHeader = new byte[ 16 ];
		int i = 0;

		// Search the toplevel boxes for the header box
		while ( true ) {
			this.in.seek ( boxStart );
			this.in.readFully ( boxHeader , 0 , 16 );
			len = ICCProfile.getInt ( boxHeader , 0 );
			if ( 1 == len )
				len = ICCProfile.getLong ( boxHeader , 8 ); // Extended
			// length
			type = ICCProfile.getInt ( boxHeader , 4 );

			// Verify the contents of the file so far.
			if ( 0 == i && FileFormatBoxes.JP2_SIGNATURE_BOX != type ) {
				throw new ColorSpaceException ( "first box in image not signature" );
			}
			else if ( 1 == i && FileFormatBoxes.FILE_TYPE_BOX != type ) {
				throw new ColorSpaceException ( "second box in image not file" );
			}
			else if ( FileFormatBoxes.CONTIGUOUS_CODESTREAM_BOX == type ) {
				throw new ColorSpaceException ( "header box not found in image" );
			}
			else if ( FileFormatBoxes.JP2_HEADER_BOX == type ) {
				break;
			}

			// Progress to the next box.
			++ i;
			boxStart += len;
		}

		// boxStart indexes the start of the JP2_HEADER_BOX,
		// make headerBoxEnd index the end of the box.
		final long headerBoxEnd = boxStart + len;

		if ( 1 == len )
			boxStart += 8; // Extended length header

		for ( boxStart += 8; boxStart < headerBoxEnd ; boxStart += len ) {
			this.in.seek ( boxStart );
			this.in.readFully ( boxHeader , 0 , 16 );
			len = ICCProfile.getInt ( boxHeader , 0 );
			if ( 1 == len )
				throw new ColorSpaceException ( "Extended length boxes not supported" );
			type = ICCProfile.getInt ( boxHeader , 4 );

			switch ( type ) {
				case FileFormatBoxes.IMAGE_HEADER_BOX:
					this.ihbox = new ImageHeaderBox ( this.in , boxStart );
					break;
				case FileFormatBoxes.COLOUR_SPECIFICATION_BOX:
					this.csbox = new ColorSpecificationBox ( this.in , boxStart );
					break;
				case FileFormatBoxes.CHANNEL_DEFINITION_BOX:
					this.cdbox = new ChannelDefinitionBox ( this.in , boxStart );
					break;
				case FileFormatBoxes.COMPONENT_MAPPING_BOX:
					this.cmbox = new ComponentMappingBox ( this.in , boxStart );
					break;
				case FileFormatBoxes.PALETTE_BOX:
					this.pbox = new PaletteBox ( this.in , boxStart );
					break;
				default:
					break;
			}
		}

		if ( null == ihbox )
			throw new ColorSpaceException ( "image header box not found" );

		if ( ( null == pbox && null != cmbox ) || ( null != pbox && null == cmbox ) )
			throw new ColorSpaceException ( "palette box and component mapping box inconsistency" );
	}

	/**
	 * Return the channel definition of the input component.
	 */
	public int getChannelDefinition ( final int c ) {
		if ( null == cdbox )
			return c;
		return this.cdbox.getCn ( c + 1 );
	}

	/**
	 * Return the dev.zontreck.harbinger.thirdparty.jj2000.j2k.colorspace method (Profiled, enumerated, or palettized).
	 */
	public MethodEnum getMethod ( ) {
		return this.csbox.getMethod ( );
	}

	/**
	 * Return the dev.zontreck.harbinger.thirdparty.jj2000.j2k.colorspace (sYCC, sRGB, sGreyScale).
	 */
	public CSEnum getColorSpace ( ) {
		return this.csbox.getColorSpace ( );
	}

	/**
	 * Return number of channels in the palette.
	 */
	public/* final */PaletteBox getPaletteBox ( ) {
		return this.pbox;
	}

	/* Enumeration Class */

	/**
	 * Return number of channels in the palette.
	 */
	public int getPaletteChannels ( ) {
		return null == pbox ? 0 : this.pbox.getNumColumns ( );
	}

	/**
	 * Return bitdepth of the palette entries.
	 */
	public int getPaletteChannelBits ( final int c ) {
		return null == pbox ? 0 : this.pbox.getBitDepth ( c );
	}

	/**
	 * Return a palettized sample
	 *
	 * @param channel requested
	 * @param index   of entry
	 * @return palettized sample
	 */
	public int getPalettizedSample ( final int channel , final int index ) {
		return null == pbox ? 0 : this.pbox.getEntry ( channel , index );
	}

	/**
	 * Is palettized predicate.
	 */
	public boolean isPalettized ( ) {
		return null != pbox;
	}

	/**
	 * Signed output predicate.
	 */
	public boolean isOutputSigned ( final int channel ) {
		return ( null != pbox ) ? this.pbox.isSigned ( channel ) : this.hd.isOriginalSigned ( channel );
	}

	/**
	 * Return a suitable String representation of the class instance.
	 */
	@Override
	public String toString ( ) {
		final StringBuffer rep = new StringBuffer ( "[ColorSpace is " ).append ( this.csbox.getMethodString ( ) )
				.append ( this.isPalettized ( ) ? "  and palettized " : " " )
				.append ( this.getMethod ( ) == ColorSpace.ENUMERATED ? this.csbox.getColorSpaceString ( ) : "" );
		if ( null != ihbox )
			rep.append ( ColorSpace.eol ).append ( ColorSpace.indent ( "    " , this.ihbox.toString ( ) ) );
		if ( null != cdbox )
			rep.append ( ColorSpace.eol ).append ( ColorSpace.indent ( "    " , this.cdbox.toString ( ) ) );
		if ( null != csbox )
			rep.append ( ColorSpace.eol ).append ( ColorSpace.indent ( "    " , this.csbox.toString ( ) ) );
		if ( null != pbox )
			rep.append ( ColorSpace.eol ).append ( ColorSpace.indent ( "    " , this.pbox.toString ( ) ) );
		if ( null != cmbox )
			rep.append ( ColorSpace.eol ).append ( ColorSpace.indent ( "    " , this.cmbox.toString ( ) ) );
		return rep.append ( "]" ).toString ( );
	}

	/**
	 * Are profiling diagnostics turned on
	 *
	 * @return yes or no
	 */
	public boolean debugging ( ) {
		return null != pl.getProperty ( "colorspace_debug" ) && "on".equalsIgnoreCase ( pl.getProperty ( "colorspace_debug" ) );
	}

	/**
	 * Typesafe enumeration class
	 *
	 * @author Bruce A Kern
	 * @version 1.0
	 */
	public static class Enumeration {
		public final String value;

		public Enumeration ( final String value ) {
			this.value = value;
		}

		@Override
		public String toString ( ) {
			return this.value;
		}
	}

	/**
	 * Method enumeration class
	 *
	 * @author Bruce A Kern
	 * @version 1.0
	 */
	public static class MethodEnum extends Enumeration {
		public MethodEnum ( final String value ) {
			super ( value );
		}
	}

	/**
	 * Colorspace enumeration class
	 *
	 * @author Bruce A Kern
	 * @version 1.0
	 */
	public static class CSEnum extends Enumeration {
		public CSEnum ( final String value ) {
			super ( value );
		}
	}

	/* end class ColorSpace */
}
