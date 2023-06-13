/*
 * CVS identifier:
 *
 * $Id: PostCompRateAllocator.java,v 1.53 2001/08/28 09:24:47 grosbois Exp $
 *
 * Class:                   PostCompRateAllocator
 *
 * Description:             Generic interface for post-compression
 *                          rate allocator.
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
package dev.zontreck.harbinger.thirdparty.jj2000.j2k.entropy.encoder;

import dev.zontreck.harbinger.thirdparty.jj2000.j2k.ModuleSpec;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.codestream.ProgressionType;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.codestream.writer.CodestreamWriter;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.codestream.writer.HeaderEncoder;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.encoder.EncoderSpecs;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.entropy.ProgressionSpec;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.image.ImgDataAdapter;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.util.ParameterList;

import java.io.IOException;
import java.io.StreamTokenizer;
import java.io.StringReader;

/**
 * This is the abstract class from which post-compression rate allocators which
 * generate layers should inherit. The source of data is a 'CodedCBlkDataSrcEnc'
 * which delivers entropy coded blocks with rate-distortion statistics.
 *
 * <p>
 * The post compression rate allocator implementation should create the layers,
 * according to a rate allocation policy, and send the packets to a
 * CodestreamWriter. Since the rate allocator sends the packets to the bit
 * stream then it should output the packets to the bit stream in the order
 * imposed by the bit stream profiles.
 *
 * @see CodedCBlkDataSrcEnc
 * @see dev.zontreck.harbinger.thirdparty.jj2000.j2k.codestream.writer.CodestreamWriter
 */
public abstract class PostCompRateAllocator extends ImgDataAdapter {
	/**
	 * The prefix for rate allocation options: 'A'
	 */
	public static final char OPT_PREFIX = 'A';

	/**
	 * The list of parameters that is accepted for entropy coding. Options for
	 * entropy coding start with 'R'.
	 */
	private static final String[][] pinfo = {
			{
					"Aptype" ,
					"[<tile idx>] res|layer|res-pos|pos-comp|comp-pos [res_start comp_start layer_end res_end "
							+ "comp_end prog] [[res_start comp_start ly_end res_end comp_end prog] ...] ["
							+ "[<tile-component idx>] ...]" ,
					"Specifies which type of progression should be used when generating "
							+ "the codestream. The 'res' value generates a resolution "
							+ "progressive codestream with the number of layers specified by "
							+ "'Alayers' option. The 'layer' value generates a layer progressive "
							+ "codestream with multiple layers. In any case the rate-allocation "
							+ "algorithm optimizes for best quality in each layer. The quality "
							+ "measure is mean squared error (MSE) or a weighted version of it "
							+ "(WMSE). If no progression type is specified or imposed by other "
							+ "modules, the default value is 'layer'.\n"
							+ "It is also possible to describe progression order changes. In "
							+ "this case, 'res_start' is the index (from 0) of the first resolution "
							+ "level, 'comp_start' is the index (from 0) of the first component, "
							+ "'ly_end' is the index (from 0) of the first layer not included, "
							+ "'res_end' is the index (from 0) of the first resolution level not "
							+ "included, 'comp_end' is index (from 0) of the first component not "
							+ "included and 'prog' is the progression type to be used "
							+ "for the rest of the tile/image. Several progression order changes "
							+ "can be specified, one after the other." , null } ,
			{
					"Alayers" ,
					"[<rate> [+<layers>] [<rate [+<layers>] [...]] | sl]" ,
					"Explicitly specifies the codestream layer formation parameters. "
							+ "The <rate> parameter specifies the bitrate to which the first "
							+ "layer should be optimized. The <layers> parameter, if present, "
							+ "specifies the number of extra layers that should be added for "
							+ "scalability. These extra layers are not optimized. "
							+ "Any extra <rate> and <layers> parameters add more layers, in the "
							+ "same way. An additional layer is always added at the end, which is "
							+ "optimized to the overall target bitrate of the bit stream. Any "
							+ "layers (optimized or not) whose target bitrate is higher that the "
							+ "overall target bitrate are silently ignored. The bitrates of the "
							+ "extra layers that are added through the <layers> parameter are "
							+ "approximately log-spaced between the other target bitrates. If "
							+ "several <rate> [+<layers>] constructs appear the <rate> parameters "
							+ "must appear in increasing order. The rate allocation algorithm "
							+ "ensures that all coded layers have a minimal reasonable size, if "
							+ "not these layers are silently ignored.\n"
							+ "If the 'sl' (i.e. 'single layer') argument is specified, the "
							+ "generated codestream will"
							+ " only contain one layer (with a bit rate specified thanks to the"
							+ " '-rate' or 'nbytes' options)." , "0.015 +20 2.0 +10" } };

	/**
	 * The source of entropy coded data
	 */
	protected CodedCBlkDataSrcEnc src;

	/**
	 * The source of entropy coded data
	 */
	protected EncoderSpecs encSpec;

	/**
	 * The number of layers.
	 */
	protected int numLayers;

	/**
	 * The bit-stream writer
	 */
	CodestreamWriter bsWriter;

	/**
	 * The header encoder
	 */
	HeaderEncoder headEnc;

	/**
	 * Initializes the source of entropy coded data.
	 *
	 * @param src The source of entropy coded data.
	 * @param bw  The packet bit stream writer.
	 * @see ProgressionType
	 */
	protected PostCompRateAllocator ( final CodedCBlkDataSrcEnc src , final int nl , final CodestreamWriter bw , final EncoderSpecs encSpec ) {
		super ( src );
		this.src = src;
		this.encSpec = encSpec;
		this.numLayers = nl;
		this.bsWriter = bw;
	}

	/**
	 * Returns the parameters that are used in this class and implementing
	 * classes. It returns a 2D String array. Each of the 1D arrays is for a
	 * different option, and they have 3 elements. The first element is the
	 * option name, the second one is the synopsis, the third one is a long
	 * description of what the parameter is and the fourth is its default value.
	 * The synopsis or description may be 'null', in which case it is assumed
	 * that there is no synopsis or description of the option, respectively.
	 * Null may be returned if no options are supported.
	 *
	 * @return the options name, their synopsis and their explanation, or null
	 * if no options are supported.
	 */
	public static String[][] getParameterInfo ( ) {
		return PostCompRateAllocator.pinfo;
	}

	/**
	 * Creates a PostCompRateAllocator object for the appropriate rate
	 * allocation parameters in the parameter list 'pl', having 'src' as the
	 * source of entropy coded data, 'rate' as the target bitrate and 'bw' as
	 * the bit stream writer object.
	 *
	 * @param src  The source of entropy coded data.
	 * @param pl   The parameter lis (or options).
	 * @param rate The target bitrate for the rate allocation
	 * @param bw   The bit stream writer object, where the bit stream data will
	 *             be written.
	 */
	public static PostCompRateAllocator createInstance (
			final CodedCBlkDataSrcEnc src , final ParameterList pl , final float rate ,
			final CodestreamWriter bw , final EncoderSpecs encSpec
	) {
		// Check parameters
		pl.checkList ( PostCompRateAllocator.OPT_PREFIX , ParameterList.toNameArray ( PostCompRateAllocator.pinfo ) );

		// Construct the layer specification from the 'Alayers' option
		final LayersInfo lyrs = PostCompRateAllocator.parseAlayers ( pl.getParameter ( "Alayers" ) , rate );

		final int nTiles = encSpec.nTiles;
		final int nComp = encSpec.nComp;
		final int numLayers = lyrs.getTotNumLayers ( );

		// Parse the progressive type
		encSpec.pocs = new ProgressionSpec ( nTiles , nComp , numLayers , encSpec.dls , ModuleSpec.SPEC_TYPE_TILE_COMP , pl );

		return new EBCOTRateAllocator ( src , lyrs , bw , encSpec , pl );
	}

	/**
	 * Convenience method that parses the 'Alayers' option.
	 *
	 * @param params The parameters of the 'Alayers' option
	 * @param rate   The overall target bitrate
	 * @return The layer specification.
	 */
	private static LayersInfo parseAlayers ( final String params , final float rate ) {
		final LayersInfo lyrs;
		final StreamTokenizer stok;
		boolean islayer, ratepending;
		float r;

		lyrs = new LayersInfo ( rate );
		stok = new StreamTokenizer ( new StringReader ( params ) );
		stok.eolIsSignificant ( false );

		try {
			stok.nextToken ( );
		} catch ( final IOException e ) {
			throw new Error ( "An IOException has occurred where it should never occur" );
		}
		ratepending = false;
		islayer = false;
		r = 0; // to keep compiler happy
		while ( StreamTokenizer.TT_EOF != stok.ttype ) {
			switch ( stok.ttype ) {
				case StreamTokenizer.TT_NUMBER:
					if ( islayer ) { // layer parameter
						try {
							lyrs.addOptPoint ( r , ( int ) stok.nval );
						} catch (
								final
								IllegalArgumentException e ) {
							throw new IllegalArgumentException ( "Error in 'Alayers' option: " + e.getMessage ( ) );
						}
						ratepending = false;
						islayer = false;
					}
					else { // rate parameter
						if ( ratepending ) { // Add pending rate parameter
							try {
								lyrs.addOptPoint ( r , 0 );
							} catch (
									final
									IllegalArgumentException e ) {
								throw new IllegalArgumentException ( "Error in 'Alayers' option: " + e.getMessage ( ) );
							}
						}
						// Now store new rate parameter
						r = ( float ) stok.nval;
						ratepending = true;
					}
					break;
				case '+':
					if ( ! ratepending || islayer ) {
						throw new IllegalArgumentException ( "Layer parameter without previous rate parameter "
								+ "in 'Alayers' option" );
					}
					islayer = true; // Next number is layer parameter
					break;
				case StreamTokenizer.TT_WORD:
					try {
						stok.nextToken ( );
					} catch ( final IOException e ) {
						throw new Error ( "An IOException has occurred where it should never occur" );
					}
					if ( StreamTokenizer.TT_EOF != stok.ttype ) {
						throw new IllegalArgumentException ( "'sl' argument of '-Alayers' option must be "
								+ "used alone." );
					}
					break;
				default:
					throw new IllegalArgumentException ( "Error parsing 'Alayers' option" );
			}
			try {
				stok.nextToken ( );
			} catch ( final IOException e ) {
				throw new Error ( "An IOException has occurred where it should never occur" );
			}
		}
		if ( islayer ) {
			throw new IllegalArgumentException ( "Error parsing 'Alayers' option" );
		}
		if ( ratepending ) {
			try {
				lyrs.addOptPoint ( r , 0 );
			} catch ( final IllegalArgumentException e ) {
				throw new IllegalArgumentException ( "Error in 'Alayers' option: " + e.getMessage ( ) );
			}
		}
		return lyrs;
	}

	/**
	 * Keep a reference to the header encoder.
	 *
	 * @param headEnc The header encoder
	 */
	public void setHeaderEncoder ( final HeaderEncoder headEnc ) {
		this.headEnc = headEnc;
	}

	/**
	 * Initializes the rate allocation points, taking into account header
	 * overhead and such. This method must be called after the header has been
	 * simulated but before calling the runAndWrite() one. The header must be
	 * rewritten after a call to this method since the number of layers may
	 * change.
	 *
	 * @see #runAndWrite
	 */
	public abstract void initialize ( ) throws IOException;

	/**
	 * Runs the rate allocation algorithm and writes the data to the bit stream.
	 * This must be called after the initialize() method.
	 *
	 * @see #initialize
	 */
	public abstract void runAndWrite ( ) throws IOException;

	/**
	 * Returns the number of layers that are actually generated.
	 *
	 * @return The number of layers generated.
	 */
	public int getNumLayers ( ) {
		return this.numLayers;
	}

}
