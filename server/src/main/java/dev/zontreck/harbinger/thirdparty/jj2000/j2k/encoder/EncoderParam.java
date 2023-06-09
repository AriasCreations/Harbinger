/*
 * Copyright (c) 2005 Sun Microsystems, Inc. All  Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * - Redistribution of source code must retain the above copyright
 *   notice, this  list of conditions and the following disclaimer.
 *
 * - Redistribution in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in
 *   the documentation and/or other materials provided with the
 *   distribution.
 *
 * Neither the name of Sun Microsystems, Inc. or the names of
 * contributors may be used to endorse or promote products derived
 * from this software without specific prior written permission.
 *
 * This software is provided "AS IS," without a warranty of any
 * kind. ALL EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND
 * WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE HEREBY
 * EXCLUDED. SUN MIDROSYSTEMS, INC. ("SUN") AND ITS LICENSORS SHALL
 * NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF
 * USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS
 * DERIVATIVES. IN NO EVENT WILL SUN OR ITS LICENSORS BE LIABLE FOR
 * ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL,
 * CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED AND
 * REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF OR
 * INABILITY TO USE THIS SOFTWARE, EVEN IF SUN HAS BEEN ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGES.
 *
 * You acknowledge that this software is not designed or intended for
 * use in the design, construction, operation or maintenance of any
 * nuclear facility.
 *
 * $Revision: 1.2 $
 * $Date: 2006/09/20 23:23:30 $
 * $State: Exp $
 */
package dev.zontreck.harbinger.thirdparty.jj2000.j2k.encoder;

import dev.zontreck.harbinger.thirdparty.jj2000.j2k.ModuleSpec;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.roi.MaxShiftSpec;

import javax.imageio.ImageWriteParam;
import java.util.Locale;

/**
 * A subclass of {@code ImageWriteParam} for writing images in
 * the JPEG 2000 format.
 *
 * <p>JPEG 2000 plugin supports to losslessly or lossy compress gray-scale,
 * RGB, and RGBA images with byte, unsigned short or short data type.  It also
 * supports losslessly compress bilevel, and 8-bit color indexed images.  The
 * result data is in the of JP2 format -- JPEG 2000 Part 1 or baseline format.
 *
 * <p>The parameters for encoding JPEG 2000 are listed in the following table:
 *
 * <p><table border=1>
 * <caption><b>JPEG 2000 Plugin Decoding Parameters</b></caption>
 * <tr><th>Parameter Name</th> <th>Description</th></tr>
 * <tr>
 * <td>numDecompositionLevels</td>
 * <td> The number of decomposition levels to generate. This value must
 * be in the range
 * {@code 0&nbsp;&le;&nbsp;numDecompositionLevels&nbsp;&le;&nbsp;32
 * }. The default value is {@code 5}. Note that the number
 * of resolution levels is
 * {@code numDecompositionLevels&nbsp;+&nbsp;1}.
 * The number of decomposition levels is constant across
 * all components and all tiles.
 * </td>
 * </tr>
 * <tr>
 * <td>encodingRate</td>
 * <td> The bitrate in bits-per-pixel for encoding.  Should be set when
 * lossy compression scheme is used.  With the default value
 * {@code Double.MAX_VALUE}, a lossless compression will be done.
 * </td>
 * </tr>
 * <tr>
 * <td>lossless</td>
 * <td> Indicates using the lossless scheme or not.  It is equivalent to
 * use reversible quantization and 5x3 integer wavelet filters.  The
 * default is {@code true}.
 * </td>
 * </tr>
 * <tr>
 * <td>componentTransformation</td>
 * <td> Specifies to utilize the component transformation on some tiles.
 * If the wavelet transform is reversible (w5x3 filter), the Reversible
 * Component Transformation (RCT) is applied. If not reversible
 * (w9x7 filter), the Irreversible Component Transformation (ICT) is used.
 * </td>
 * </tr>
 * <tr>
 * <td>filters</td>
 * <td> Specifies which wavelet filters to use for the specified
 * tile-components.  JPEG 2000 part I only supports w5x3 and w9x7 filters.
 * </td>
 * </tr>
 * <tr>
 * <td>codeBlockSize</td>
 * <td> Specifies the maximum code-block size to use for tile-component.
 * The maximum width and height is 1024, however the block size
 * (i.e. width x height) must not exceed 4096.  The minimum width and
 * height is 4.  The default values are (64, 64).
 * </td>
 * </tr>
 * <tr>
 * <td>progressionType</td>
 * <td> Specifies which type of progression should be used when generating
 * the codestream.
 * <p> The format is ont of the progression types defined below:
 *
 * <p> res : Resolution-Layer-Component-Position
 * <p> layer: Layer-Resolution-Component-Position
 * <p> res-pos: Resolution-Position-Component-Layer
 * <p> pos-comp: Position-Component-Resolution-Layer
 * <p> comp-pos: Component-Position-Resolution-Layer
 * </td>
 * </tr>
 * <tr>
 * <td>SOP</td>
 * <td>Specifies whether start of packet (SOP) markers should be used.
 * true enables, false disables it.  The default value is false.
 * </td>
 * </tr>
 * <tr>
 * <td>EPH</td>
 * <td>Specifies whether end of packet header (EPH) markers should be used.
 * true enables, false disables it.  The default value is false.
 * </td>
 * </tr>
 * <tr>
 * <td>writeCodeStreamOnly</td>
 * <td>Specifies whether write only the jpeg2000 code stream, i.e, no any
 * box is written.  The default value is false.
 * </td>
 * </tr>
 * </table>
 */
public class EncoderParam extends ImageWriteParam {
	/**
	 * The filter for lossy compression.
	 */
	public static final String FILTER_97 = "w9x7";

	/**
	 * The filter for lossless compression.
	 */
	public static final String FILTER_53 = "w5x3";

	/**
	 * The number of decomposition levels.
	 */
	private int numDecompositionLevels = 5;

	/**
	 * Indicates that the packet headers are packed in the tiles' headers.
	 */
	private boolean packPacketHeaderInTile;

	/**
	 * Indicates that the packet headers are packed in the main header.
	 */
	private boolean packPacketHeaderInMain;

	/**
	 * Specifies the maximum number of packets to be put into one tile-part.
	 * Zero means include all packets in first tile-part of each tile.
	 */
	private int packetPerTilePart;

	/**
	 * The bitrate in bits-per-pixel for encoding.  Should be set when lossy
	 * compression scheme is used.  The default is
	 * {@code Double.MAX_VALUE}.
	 */
	private double encodingRate = Double.MAX_VALUE;

	/**
	 * Indicates using the lossless scheme or not.  It is equivalent to
	 * use reversible quantization and 5x3 integer wavelet filters.
	 */
	private boolean lossless = true;

	/**
	 * Specifies to utilize the component transformation with some tiles.
	 * If the wavelet transform is reversible (w5x3 filter), the
	 * Reversible Component Transformation (RCT) is applied. If not reversible
	 * (w9x7 filter), the Irreversible Component Transformation (ICT)
	 * is used.
	 */
	private boolean componentTransformation = true;

	/**
	 * Specifies which filters to use for the specified tile-components.
	 * JPEG 2000 part I only supports w5x3 and w9x7 filters.
	 */
	private String filter = EncoderParam.FILTER_53;

	/**
	 * Specifies the maximum code-block size to use for tile-component.
	 * The maximum width and height is 1024, however the image area
	 * (i.e. width x height) must not exceed 4096. The minimum
	 * width and height is 4.  Default: 64 64.
	 */
	private int[] codeBlockSize = { 64 , 64 };

	/**
	 * See above.
	 */
	private String progressionType = "layer";

	/**
	 * The specified (tile-component) progression.  Will be used to generate
	 * the progression type.
	 */
	private String progressionName;

	/**
	 * Explicitly specifies the codestream layer formation parameters.
	 * The rate (double) parameter specifies the bitrate to which the first
	 * layer should be optimized.  The layers (int) parameter, if present,
	 * specifies the number of extra layers that should be added for
	 * scalability.  These extra layers are not optimized.  Any extra rate
	 * and layers parameters add more layers, in the same way.  An
	 * additional layer is always added at the end, which is optimized
	 * to the overall target bitrate of the bit stream. Any layers
	 * (optimized or not) whose target bitrate is higher that the
	 * overall target bitrate are silently ignored. The bitrates of
	 * the extra layers that are added through the layers parameter
	 * are approximately log-spaced between the other target bitrates.
	 * If several (rate, layers) constructs appear the rate parameters
	 * must appear in increasing order. The rate allocation algorithm
	 * ensures that all coded layers have a minimal reasonable size,
	 * if not these layers are silently ignored.  Default: 0.015 +20 2.0 +10.
	 */
	private String layers = "0.015 +20 2.0 +10";

	/**
	 * Specifies whether end of packet header (EPH) markers should be used.
	 * true enables, false disables it.  Default: false.
	 */
	private boolean EPH;

	/**
	 * Specifies whether start of packet (SOP) markers should be used.
	 * true enables, false disables it. Default: false.
	 */
	private boolean SOP;

	/**
	 * Specifies whether write only the jpeg2000 code stream, i.e, no any
	 * box is written.  The default value is false.
	 */
	private boolean writeCodeStreamOnly;

	/**
	 * This parameter defines the lowest resolution levels to belong to
	 * the ROI.  By doing this, it is possible to avoid only getting
	 * information for the ROI at an early stage of transmission.
	 * startLevelROI = 0 means the lowest resolution level belongs to
	 * the ROI, 1 means the second lowest etc.  The default values, -1,
	 * deactivates this parameter.
	 */
	private int startLevelROI = - 1;

	/**
	 * By specifying this parameter, the ROI mask will be limited to
	 * covering only entire code-blocks. The ROI coding can then be
	 * performed without any actual scaling of the coefficients but
	 * by instead scaling the distortion estimates.
	 */
	private boolean alignROI;

	/**
	 * Specifies ROIs shape and location. The component index specifies
	 * which components contain the ROI.  If this parameter is used, the
	 * codestream is layer progressive by default unless it is
	 * overridden by the {@code progressionType}.
	 */
	private MaxShiftSpec ROIs;

	private int numTiles;
	private int numComponents;

	private int minX;
	private int minY;

	/**
	 * Constructor which sets the {@code Locale}.
	 *
	 * @param locale a {@code Locale} to be used to localize
	 *               compression type names and quality descriptions, or
	 *               {@code null}.
	 */
	public EncoderParam ( final Locale locale ) {
		super ( locale );
		this.setDefaults ( );
	}

	/**
	 * Constructs a {@code J2KImageWriteParam} object with default
	 * values for all parameters.
	 */
	public EncoderParam ( ) {
		this.setDefaults ( );
	}

	/**
	 * Set source
	 */
	private void setDefaults ( ) {
		// override the params in the super class
		this.canOffsetTiles = true;
		this.canWriteTiles = true;
		this.canOffsetTiles = true;
		this.compressionTypes = new String[]{ "JPEG2000" };
		this.canWriteCompressed = true;
		this.tilingMode = ImageWriteParam.MODE_EXPLICIT;
	}

	/**
	 * Gets {@code numDecompositionLevels}.
	 *
	 * @return the number of decomposition levels.
	 * @see #setNumDecompositionLevels
	 */
	public int getNumDecompositionLevels ( ) {
		return this.numDecompositionLevels;
	}

	/**
	 * Sets {@code numDecompositionLevels}.
	 *
	 * @param numDecompositionLevels the number of decomposition levels.
	 * @throws IllegalArgumentException if {@code numDecompositionLevels}
	 *                                  is negative or greater than 32.
	 * @see #getNumDecompositionLevels
	 */
	public void setNumDecompositionLevels ( final int numDecompositionLevels ) {
		if ( 0 > numDecompositionLevels || 32 < numDecompositionLevels ) {
			throw new IllegalArgumentException
					( "numDecompositionLevels < 0 || numDecompositionLevels > 32" );
		}
		this.numDecompositionLevels = numDecompositionLevels;
	}

	/**
	 * Gets {@code encodingRate}.
	 *
	 * @return the encoding rate in bits-per-pixel.
	 * @see #setEncodingRate(double)
	 */
	public double getEncodingRate ( ) {
		return this.encodingRate;
	}

	/**
	 * Sets {@code encodingRate}.
	 *
	 * @param rate the encoding rate in bits-per-pixel.
	 * @see #getEncodingRate()
	 */
	public void setEncodingRate ( final double rate ) {
		encodingRate = rate;
		if ( Double.MAX_VALUE != encodingRate ) {
			this.lossless = false;
			this.filter = EncoderParam.FILTER_97;
		}
		else {
			this.lossless = true;
			this.filter = EncoderParam.FILTER_53;
		}
	}

	/**
	 * Gets {@code lossless}.
	 *
	 * @return whether the compression scheme is lossless.
	 * @see #setLossless(boolean)
	 */
	public boolean getLossless ( ) {
		return this.lossless;
	}

	/**
	 * Sets {@code lossless}.
	 *
	 * @param lossless whether the compression scheme is lossless.
	 * @see #getLossless()
	 */
	public void setLossless ( final boolean lossless ) {
		this.lossless = lossless;
	}

	/**
	 * Gets {@code filters}.
	 *
	 * @return which wavelet filters to use for the specified
	 * tile-components.
	 * @see #setFilter(String)
	 */
	public String getFilter ( ) {
		return this.filter;
	}

	/**
	 * Sets {@code filter}.
	 *
	 * @param value which wavelet filters to use for the specified
	 *              tile-components.
	 * @see #getFilter()
	 */
	public void setFilter ( final String value ) {
		this.filter = value;
	}

	/**
	 * Gets {@code componentTransformation}.
	 *
	 * @return whether to utilize the component transformation.
	 * @see #setComponentTransformation(boolean)
	 */
	public boolean getComponentTransformation ( ) {
		return this.componentTransformation;
	}

	/**
	 * Sets {@code componentTransformation}.
	 *
	 * @param value whether to utilize the component transformation.
	 * @see #getComponentTransformation()
	 */
	public void setComponentTransformation ( final boolean value ) {
		this.componentTransformation = value;
	}

	/**
	 * Gets {@code codeBlockSize}.
	 *
	 * @return the maximum code-block size to use per tile-component.
	 * @see #setCodeBlockSize(int[])
	 */
	public int[] getCodeBlockSize ( ) {
		return this.codeBlockSize;
	}

	/**
	 * Sets {@code codeBlockSize}.
	 *
	 * @param value the maximum code-block size to use per tile-component.
	 * @see #getCodeBlockSize()
	 */
	public void setCodeBlockSize ( final int[] value ) {
		this.codeBlockSize = value;
	}

	/**
	 * Gets {@code SOP}.
	 *
	 * @return whether start of packet (SOP) markers should be used.
	 * @see #setSOP(boolean)
	 */
	public boolean getSOP ( ) {
		return this.SOP;
	}

	/**
	 * Sets {@code SOP}.
	 *
	 * @param value whether start of packet (SOP) markers should be used.
	 * @see #getSOP()
	 */
	public void setSOP ( final boolean value ) {
		this.SOP = value;
	}

	/**
	 * Gets {@code EPH}.
	 *
	 * @return whether end of packet header (EPH) markers should be used.
	 * @see #setEPH(boolean)
	 */
	public boolean getEPH ( ) {
		return this.EPH;
	}

	/**
	 * Sets {@code EPH}.
	 *
	 * @param value whether end of packet header (EPH) markers should be used.
	 * @see #getEPH()
	 */
	public void setEPH ( final boolean value ) {
		this.EPH = value;
	}

	/**
	 * Gets {@code progressionType}
	 */
	public String getProgressionName ( ) {
		return this.progressionName;
	}

	/**
	 * Sets {@code progressionName}
	 */
	public void setProgressionName ( final String values ) {
		this.progressionName = values;
	}

	/**
	 * Gets {@code progressionType}.
	 *
	 * @return which type of progression should be used when generating
	 * the codestream.
	 * @see #setProgressionType(String)
	 */
	public String getProgressionType ( ) {
		return this.progressionType;
	}

	/**
	 * Sets {@code progressionType}.
	 *
	 * @param value which type of progression should be used when generating
	 *              the codestream.
	 * @see #getProgressionType()
	 */
	public void setProgressionType ( final String value ) {
		this.progressionType = value;
	}

	/**
	 * Gets {@code writeCodeStreamOnly}.
	 *
	 * @return whether the jpeg2000 code stream only or the jp2 format
	 * will be written into the output.
	 * @see #setWriteCodeStreamOnly(boolean)
	 */
	public boolean getWriteCodeStreamOnly ( ) {
		return this.writeCodeStreamOnly;
	}

	/**
	 * Sets {@code writeCodeStreamOnly}.
	 *
	 * @param value Whether the jpeg2000 code stream only or the jp2 format
	 *              will be written into the output.
	 * @see #getWriteCodeStreamOnly()
	 */
	public void setWriteCodeStreamOnly ( final boolean value ) {
		this.writeCodeStreamOnly = value;
	}

	/**
	 * Gets {@code startLevel}
	 */
	public int getStartLevelROI ( ) {
		return this.startLevelROI;
	}

	/**
	 * Sets the {@code startLevelROI}
	 */
	public void setStartLevelROI ( final int value ) {
		this.startLevelROI = value;
	}

	/**
	 * Gets {@code layers}
	 */
	public String getLayers ( ) {
		return this.layers;
	}

	/**
	 * Sets the {@code layers}
	 */
	public void setLayers ( final String value ) {
		this.layers = value;
	}

	/**
	 * Gets {@code minX}
	 */
	public int getMinX ( ) {
		return this.minX;
	}

	/**
	 * Sets {@code minX}
	 */
	public void setMinX ( final int minX ) {
		this.minX = minX;
	}

	/**
	 * Gets {@code minY}
	 */
	public int getMinY ( ) {
		return this.minY;
	}

	/**
	 * Sets {@code minY}
	 */
	public void setMinY ( final int minY ) {
		this.minY = minY;
	}

	/**
	 * Gets {@code packetPerTilePart}
	 */
	public int getPacketPerTilePart ( ) {
		return this.packetPerTilePart;
	}

	/**
	 * Sets {@code packetPerTilePart}
	 */
	public void setPacketPerTilePart ( final int packetPerTilePart ) {
		if ( 0 > packetPerTilePart )
			throw new IllegalArgumentException ( "packetPerTilePart" );

		this.packetPerTilePart = packetPerTilePart;
		if ( 0 < packetPerTilePart ) {
			SOP = true;
			EPH = true;
		}
	}

	/**
	 * Gets {@code packPacketHeaderInTile}
	 */
	public boolean getPackPacketHeaderInTile ( ) {
		return this.packPacketHeaderInTile;
	}

	/**
	 * Sets {@code packPacketHeaderInTile}
	 */
	public void setPackPacketHeaderInTile ( final boolean packPacketHeaderInTile ) {
		this.packPacketHeaderInTile = packPacketHeaderInTile;
		if ( packPacketHeaderInTile ) {
			SOP = true;
			EPH = true;
		}
	}

	/**
	 * Gets {@code packPacketHeaderInMain}
	 */
	public boolean getPackPacketHeaderInMain ( ) {
		return this.packPacketHeaderInMain;
	}

	/**
	 * Sets {@code packPacketHeaderInMain}
	 */
	public void setPackPacketHeaderInMain ( final boolean packPacketHeaderInMain ) {
		this.packPacketHeaderInMain = packPacketHeaderInMain;
		if ( packPacketHeaderInMain ) {
			SOP = true;
			EPH = true;
		}
	}

	/**
	 * Gets {@code alignROI}
	 */
	public boolean getAlignROI ( ) {
		return this.alignROI;
	}

	/**
	 * Sets {@code alignROI}
	 */
	public void setAlignROI ( final boolean align ) {
		this.alignROI = align;
	}

	/**
	 * Gets {@code ROIs}
	 */
	public MaxShiftSpec getROIs ( ) {
		return this.ROIs;
	}

	/**
	 * Sets {@code ROIs}
	 */
	public void setROIs ( final String values ) {
		this.ROIs = new MaxShiftSpec ( this.numTiles , this.numComponents , ModuleSpec.SPEC_TYPE_TILE_COMP , values );
	}
}
