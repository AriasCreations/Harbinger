/*
 * CVS identifier:
 *
 * $Id: BitstreamReaderAgent.java,v 1.27 2002/07/25 14:59:32 grosbois Exp $
 *
 * Class:                   BitstreamReaderAgent
 *
 * Description:             The generic interface for bit stream
 *                          transport agents.
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
package dev.zontreck.harbinger.thirdparty.jj2000.j2k.codestream.reader;

import dev.zontreck.harbinger.thirdparty.jj2000.j2k.ModuleSpec;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.codestream.HeaderInfo;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.decoder.DecoderSpecs;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.entropy.decoder.CodedCBlkDataSrcDec;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.image.Coord;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.io.RandomAccessIO;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.quantization.dequantizer.StdDequantizerParams;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.util.MathUtil;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.util.ParameterList;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.wavelet.Subband;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.wavelet.synthesis.SubbandSyn;

import java.io.IOException;

/**
 * This is the generic interface for bit stream reader agents. A bit stream
 * reader agent is an entity that allows reading from a bit stream and
 * requesting compressed code-blocks. It can be a simple file reader, or a
 * network connection, or anything else.
 *
 * <p>
 * The bit stream reader agent allows to make request for compressed block data
 * in any order. The amount of data returned would normally depend on the data
 * available at the time of the request, be it from a file or from a network
 * connection.
 *
 * <p>
 * The bit stream reader agent has the notion of a current tile, and coordinates
 * are relative to the current tile, where applicable.
 *
 * <p>
 * Resolution level 0 is the lowest resolution level, i.e. the LL subband alone.
 */
public abstract class BitstreamReaderAgent implements CodedCBlkDataSrcDec {

	/**
	 * The prefix for bit stream reader options: 'B'
	 */
	public static final char OPT_PREFIX = 'B';
	/**
	 * The list of parameters that is accepted by the bit stream readers. They
	 * start with 'B'.
	 */
	private static final String[][] pinfo = null;
	/**
	 * The number of components
	 */
	protected final int nc;
	/**
	 * The image width on the hi-res reference grid
	 */
	protected final int imgW;
	/**
	 * The image width on the hi-res reference grid
	 */
	protected final int imgH;
	/**
	 * The horizontal coordinate of the image origin in the canvas system, on
	 * the reference grid.
	 */
	protected final int ax;
	/**
	 * The vertical coordinate of the image origin in the canvas system, on the
	 * reference grid.
	 */
	protected final int ay;
	/**
	 * The horizontal coordinate of the tiling origin in the canvas system, on
	 * the reference grid.
	 */
	protected final int px;
	/**
	 * The vertical coordinate of the tiling origin in the canvas system, on the
	 * reference grid.
	 */
	protected final int py;
	/**
	 * The horizontal offsets of the upper-left corner of the current tile (not
	 * active tile) with respect to the canvas origin, in the component hi-res
	 * grid, for each component.
	 */
	protected final int[] offX;
	/**
	 * The vertical offsets of the upper-left corner of the current tile (not
	 * active tile) with respect to the canvas origin, in the component hi-res
	 * grid, for each component.
	 */
	protected final int[] offY;
	/**
	 * The horizontal coordinates of the upper-left corner of the active tile,
	 * with respect to the canvas origin, in the component hi-res grid, for each
	 * component.
	 */
	protected final int[] culx;
	/**
	 * The vertical coordinates of the upper-left corner of the active tile,
	 * with respect to the canvas origin, in the component hi-res grid, for each
	 * component.
	 */
	protected final int[] culy;
	/**
	 * The nominal tile width, in the hi-res reference grid
	 */
	protected final int ntW;
	/**
	 * The nominal tile height, in the hi-res reference grid
	 */
	protected final int ntH;
	/**
	 * The number of tile in the horizontal direction
	 */
	protected final int ntX;
	/**
	 * The number of tiles in the vertical direction
	 */
	protected final int ntY;
	/**
	 * The total number of tiles
	 */
	protected final int nt;
	/**
	 * The decoded bit stream header
	 */
	protected final HeaderDecoder hd;
	/**
	 * The decoder specifications
	 */
	protected DecoderSpecs decSpec;
	/**
	 * Whether or not the components in the current tile uses a derived
	 * quantization step size (only relevant in non reversible quantization
	 * mode). This field is actualized by the setTile method in
	 * FileBitstreamReaderAgent.
	 *
	 * @see FileBitstreamReaderAgent#initSubbandsFields
	 */
	protected boolean[] derived;
	/**
	 * Number of guard bits off all component in the current tile. This field is
	 * actualized by the setTile method in FileBitstreamReaderAgent.
	 *
	 * @see FileBitstreamReaderAgent#initSubbandsFields
	 */
	protected int[] gb;
	/**
	 * Dequantization parameters of all subbands and all components in the
	 * current tile. The value is actualized by the setTile method in
	 * FileBitstreamReaderAgent.
	 *
	 * @see FileBitstreamReaderAgent#initSubbandsFields
	 */
	protected StdDequantizerParams[] params;
	/**
	 * The maximum number of decompostion levels for each component of the
	 * current tile. It means that component c has mdl[c]+1 resolution levels
	 * (indexed from 0 to mdl[c])
	 */
	protected int[] mdl;
	/**
	 * Image resolution level to generate
	 */
	protected int targetRes;
	/**
	 * The subband trees for each component in the current tile. Each element in
	 * the array is the root element of the subband tree for a component. The
	 * number of magnitude bits in each subband (magBits member variable) is not
	 * initialized.
	 */
	protected SubbandSyn[] subbTrees;
	/**
	 * The current tile horizontal index
	 */
	protected int ctX;
	/**
	 * The current tile vertical index
	 */
	protected int ctY;
	/**
	 * Number of bytes targeted to be read
	 */
	protected int tnbytes;

	/**
	 * Actual number of read bytes
	 */
	protected int anbytes;

	/**
	 * Target decoding rate in bpp
	 */
	protected float trate;

	/**
	 * Actual decoding rate in bpp
	 */
	protected float arate;

	/**
	 * Initializes members of this class. This constructor takes a HeaderDecoder
	 * object. This object must be initialized by the constructor of the
	 * implementing class from the header of the bit stream.
	 *
	 * @param hd      The decoded header of the bit stream from where to initialize
	 *                the values.
	 * @param decSpec The decoder specifications
	 */
	protected BitstreamReaderAgent ( final HeaderDecoder hd , final DecoderSpecs decSpec ) {
		final Coord co;

		this.decSpec = decSpec;
		this.hd = hd;

		// Number of components
		this.nc = hd.getNumComps ( );
		this.offX = new int[ this.nc ];
		this.offY = new int[ this.nc ];
		this.culx = new int[ this.nc ];
		this.culy = new int[ this.nc ];

		// Image size and origin
		this.imgW = hd.getImgWidth ( );
		this.imgH = hd.getImgHeight ( );
		this.ax = hd.getImgULX ( );
		this.ay = hd.getImgULY ( );

		// Tiles
		co = hd.getTilingOrigin ( null );
		this.px = co.x;
		this.py = co.y;
		this.ntW = hd.getNomTileWidth ( );
		this.ntH = hd.getNomTileHeight ( );
		this.ntX = ( this.ax + this.imgW - this.px + this.ntW - 1 ) / this.ntW;
		this.ntY = ( this.ay + this.imgH - this.py + this.ntH - 1 ) / this.ntH;
		this.nt = this.ntX * this.ntY;
	}

	/**
	 * Creates a bit stream reader of the correct type that works on the
	 * provided RandomAccessIO, with the special parameters from the parameter
	 * list.
	 *
	 * @param in        The RandomAccessIO source from which to read the bit stream.
	 * @param hd        Header of the codestream.
	 * @param pl        The parameter list containing parameters applicable to the bit
	 *                  stream read (other parameters may also be present).
	 * @param decSpec   The decoder specifications
	 * @param cdstrInfo Whether or not to print information found in codestream.
	 * @param hi        Reference to the HeaderInfo instance.
	 * @throws IOException              If an I/O error occurs while reading initial data from the
	 *                                  bit stream.
	 * @throws IllegalArgumentException If an unrecognised bit stream reader option is present.
	 */
	public static BitstreamReaderAgent createInstance (
			final RandomAccessIO in , final HeaderDecoder hd , final ParameterList pl ,
			final DecoderSpecs decSpec , final boolean cdstrInfo , final HeaderInfo hi
	) throws IOException {

		// Check parameters
		pl.checkList (
				OPT_PREFIX ,
				ParameterList.toNameArray ( getParameterInfo ( ) )
		);

		return new FileBitstreamReaderAgent ( hd , in , decSpec , pl , cdstrInfo , hi );
	}

	/**
	 * Returns the parameters that are used in this class and implementing
	 * classes. It returns a 2D String array. Each of the 1D arrays is for a
	 * different option, and they have 3 elements. The first element is the
	 * option name, the second one is the synopsis and the third one is a long
	 * description of what the parameter is. The synopsis or description may be
	 * 'null', in which case it is assumed that there is no synopsis or
	 * description of the option, respectively. Null may be returned if no
	 * options are supported.
	 *
	 * @return the options name, their synopsis and their explanation, or null
	 * if no options are supported.
	 */
	public static String[][] getParameterInfo ( ) {
		return BitstreamReaderAgent.pinfo;
	}

	/**
	 * Returns the horizontal code-block partition origin. Allowable values are
	 * 0 and 1, nothing else.
	 */
	@Override
	public final int getCbULX ( ) {
		return this.hd.getCbULX ( );
	}

	/**
	 * Returns the vertical code-block partition origin. Allowable values are 0
	 * and 1, nothing else.
	 */
	@Override
	public int getCbULY ( ) {
		return this.hd.getCbULY ( );
	}

	/**
	 * Returns the number of components in the image.
	 *
	 * @return The number of components in the image.
	 */
	@Override
	public final int getNumComps ( ) {
		return this.nc;
	}

	/**
	 * Returns the component subsampling factor in the horizontal direction, for
	 * the specified component. This is, approximately, the ratio of dimensions
	 * between the reference grid and the component itself, see the 'ImgData'
	 * interface desription for details.
	 *
	 * @param c The index of the component (between 0 and N-1)
	 * @return The horizontal subsampling factor of component 'c'
	 * @see dev.zontreck.harbinger.thirdparty.jj2000.j2k.image.ImgData
	 */
	@Override
	public final int getCompSubsX ( final int c ) {
		return this.hd.getCompSubsX ( c );
	}

	/**
	 * Returns the component subsampling factor in the vertical direction, for
	 * the specified component. This is, approximately, the ratio of dimensions
	 * between the reference grid and the component itself, see the 'ImgData'
	 * interface desription for details.
	 *
	 * @param c The index of the component (between 0 and C-1)
	 * @return The vertical subsampling factor of component 'c'
	 * @see dev.zontreck.harbinger.thirdparty.jj2000.j2k.image.ImgData
	 */
	@Override
	public int getCompSubsY ( final int c ) {
		return this.hd.getCompSubsY ( c );
	}

	/**
	 * Returns the overall width of the current tile in pixels for the given
	 * (tile) resolution level. This is the tile's width without accounting for
	 * any component subsampling.
	 *
	 * <p>
	 * Note: Tile resolution level indexes may be different from tile-component
	 * resolution index. They are indeed indexed starting from the lowest number
	 * of decomposition levels of each component of the tile.
	 *
	 * <p>
	 * For an image (1 tile) with 2 components (component 0 having 2
	 * decomposition levels and component 1 having 3 decomposition levels), the
	 * first (tile-)component has 3 resolution levels and the second one has 4
	 * resolution levels, whereas the tile has only 3 resolution levels
	 * available.
	 *
	 * @param rl The (tile) resolution level.
	 * @return The current tile's width in pixels.
	 */
	@Override
	public int getTileWidth ( final int rl ) {
		// The minumum number of decomposition levels between all the
		// components
		final int mindl = this.decSpec.dls.getMinInTile ( this.getTileIdx ( ) );
		if ( rl > mindl ) {
			throw new IllegalArgumentException ( "Requested resolution level" + " is not available for, at "
					+ "least, one component in " + "tile: " + this.ctX + "x" + this.ctY );
		}
		final int ctulx;
		final int ntulx;
		final int dl = mindl - rl; // Number of decomposition to obtain this
		// resolution

		// Calculate starting X of current tile at hi-res
		ctulx = ( 0 == ctX ) ? this.ax : this.px + this.ctX * this.ntW;
		// Calculate starting X of next tile X-wise at hi-res
		ntulx = ( this.ctX < this.ntX - 1 ) ? this.px + ( this.ctX + 1 ) * this.ntW : this.ax + this.imgW;

		// The difference at the rl resolution level is the width
		return ( ntulx + ( 1 << dl ) - 1 ) / ( 1 << dl ) - ( ctulx + ( 1 << dl ) - 1 ) / ( 1 << dl );
	}

	/**
	 * Returns the overall height of the current tile in pixels, for the given
	 * resolution level. This is the tile's height without accounting for any
	 * component subsampling.
	 *
	 * <p>
	 * Note: Tile resolution level indexes may be different from tile-component
	 * resolution index. They are indeed indexed starting from the lowest number
	 * of decomposition levels of each component of the tile.
	 *
	 * <p>
	 * For an image (1 tile) with 2 components (component 0 having 2
	 * decomposition levels and component 1 having 3 decomposition levels), the
	 * first (tile-)component has 3 resolution levels and the second one has 4
	 * resolution levels, whereas the tile has only 3 resolution levels
	 * available.
	 *
	 * @param rl The (tile) resolution level.
	 * @return The total current tile's height in pixels.
	 */
	@Override
	public int getTileHeight ( final int rl ) {
		// The minumum number of decomposition levels between all the
		// components
		final int mindl = this.decSpec.dls.getMinInTile ( this.getTileIdx ( ) );
		if ( rl > mindl ) {
			throw new IllegalArgumentException ( "Requested resolution level" + " is not available for, at "
					+ "least, one component in" + " tile: " + this.ctX + "x" + this.ctY );
		}

		final int ctuly;
		final int ntuly;
		final int dl = mindl - rl; // Number of decomposition to obtain this
		// resolution

		// Calculate starting Y of current tile at hi-res
		ctuly = ( 0 == ctY ) ? this.ay : this.py + this.ctY * this.ntH;
		// Calculate starting Y of next tile Y-wise at hi-res
		ntuly = ( this.ctY < this.ntY - 1 ) ? this.py + ( this.ctY + 1 ) * this.ntH : this.ay + this.imgH;
		// The difference at the rl level is the height
		return ( ntuly + ( 1 << dl ) - 1 ) / ( 1 << dl ) - ( ctuly + ( 1 << dl ) - 1 ) / ( 1 << dl );
	}

	/**
	 * Returns the overall width of the image in pixels, for the given (image)
	 * resolution level. This is the image's width without accounting for any
	 * component subsampling or tiling.
	 *
	 * <p>
	 * Note: Image resolution level indexes may differ from tile-component
	 * resolution index. They are indeed indexed starting from the lowest number
	 * of decomposition levels of each component of each tile.
	 *
	 * <p>
	 * Example: For an image (1 tile) with 2 components (component 0 having 2
	 * decomposition levels and component 1 having 3 decomposition levels), the
	 * first (tile-) component has 3 resolution levels and the second one has 4
	 * resolution levels, whereas the image has only 3 resolution levels
	 * available.
	 *
	 * @param rl The image resolution level.
	 * @return The total image's width in pixels.
	 */
	@Override
	public int getImgWidth ( final int rl ) {
		// The minimum number of decomposition levels of each
		// tile-component
		final int mindl = this.decSpec.dls.getMin ( );
		if ( rl > mindl ) {
			throw new IllegalArgumentException ( "Requested resolution level" + " is not available for, at "
					+ "least, one tile-component" );
		}
		// Retrieve number of decomposition levels corresponding to
		// this resolution level
		final int dl = mindl - rl;
		return ( this.ax + this.imgW + ( 1 << dl ) - 1 ) / ( 1 << dl ) - ( this.ax + ( 1 << dl ) - 1 ) / ( 1 << dl );
	}

	/**
	 * Returns the overall height of the image in pixels, for the given
	 * resolution level. This is the image's height without accounting for any
	 * component subsampling or tiling.
	 *
	 * <p>
	 * Note: Image resolution level indexes may differ from tile-component
	 * resolution index. They are indeed indexed starting from the lowest number
	 * of decomposition levels of each component of each tile.
	 *
	 * <p>
	 * Example: For an image (1 tile) with 2 components (component 0 having 2
	 * decomposition levels and component 1 having 3 decomposition levels), the
	 * first (tile-) component has 3 resolution levels and the second one has 4
	 * resolution levels, whereas the image has only 3 resolution levels
	 * available.
	 *
	 * @param rl The image resolution level, from 0 to L.
	 * @return The total image's height in pixels.
	 */
	@Override
	public int getImgHeight ( final int rl ) {
		final int mindl = this.decSpec.dls.getMin ( );
		if ( rl > mindl ) {
			throw new IllegalArgumentException ( "Requested resolution level" + " is not available for, at "
					+ "least, one tile-component" );
		}
		// Retrieve number of decomposition levels corresponding to this
		// resolution level
		final int dl = mindl - rl;
		return ( this.ay + this.imgH + ( 1 << dl ) - 1 ) / ( 1 << dl ) - ( this.ay + ( 1 << dl ) - 1 ) / ( 1 << dl );
	}

	/**
	 * Returns the horizontal coordinate of the image origin, the top-left
	 * corner, in the canvas system, on the reference grid at the specified
	 * resolution level.
	 *
	 * <p>
	 * Note: Image resolution level indexes may differ from tile-component
	 * resolution index. They are indeed indexed starting from the lowest number
	 * of decomposition levels of each component of each tile.
	 *
	 * <p>
	 * Example: For an image (1 tile) with 2 components (component 0 having 2
	 * decomposition levels and component 1 having 3 decomposition levels), the
	 * first (tile-) component has 3 resolution levels and the second one has 4
	 * resolution levels, whereas the image has only 3 resolution levels
	 * available.
	 *
	 * @param rl The resolution level, from 0 to L.
	 * @return The horizontal coordinate of the image origin in the canvas
	 * system, on the reference grid.
	 */
	@Override
	public int getImgULX ( final int rl ) {
		final int mindl = this.decSpec.dls.getMin ( );
		if ( rl > mindl ) {
			throw new IllegalArgumentException ( "Requested resolution level" + " is not available for, at "
					+ "least, one tile-component" );
		}
		// Retrieve number of decomposition levels corresponding to this
		// resolution level
		final int dl = mindl - rl;
		return ( this.ax + ( 1 << dl ) - 1 ) / ( 1 << dl );
	}

	/**
	 * Returns the vertical coordinate of the image origin, the top-left corner,
	 * in the canvas system, on the reference grid at the specified resolution
	 * level.
	 *
	 * <p>
	 * Note: Image resolution level indexes may differ from tile-component
	 * resolution index. They are indeed indexed starting from the lowest number
	 * of decomposition levels of each component of each tile.
	 *
	 * <p>
	 * Example: For an image (1 tile) with 2 components (component 0 having 2
	 * decomposition levels and component 1 having 3 decomposition levels), the
	 * first (tile-) component has 3 resolution levels and the second one has 4
	 * resolution levels, whereas the image has only 3 resolution levels
	 * available.
	 *
	 * @param rl The resolution level, from 0 to L.
	 * @return The vertical coordinate of the image origin in the canvas system,
	 * on the reference grid.
	 */
	@Override
	public int getImgULY ( final int rl ) {
		final int mindl = this.decSpec.dls.getMin ( );
		if ( rl > mindl ) {
			throw new IllegalArgumentException ( "Requested resolution level" + " is not available for, at "
					+ "least, one tile-component" );
		}
		// Retrieve number of decomposition levels corresponding to this
		// resolution level
		final int dl = mindl - rl;
		return ( this.ay + ( 1 << dl ) - 1 ) / ( 1 << dl );
	}

	/**
	 * Returns the width in pixels of the specified tile-component for the given
	 * (tile-component) resolution level.
	 *
	 * @param t  The tile index
	 * @param c  The index of the component, from 0 to N-1.
	 * @param rl The resolution level, from 0 to L.
	 * @return The width in pixels of component <tt>c</tt> in tile <tt>t</tt>
	 * for resolution level <tt>rl</tt>.
	 */
	@Override
	public final int getTileCompWidth ( final int t , final int c , final int rl ) {
		final int tIdx = this.getTileIdx ( );
		if ( t != tIdx ) {
			throw new Error ( "Asking the tile-component width of a tile " + "different  from the current one." );
		}
		int ntulx;
		final int dl = this.mdl[ c ] - rl;
		// Calculate starting X of next tile X-wise at reference grid hi-res
		ntulx = ( this.ctX < this.ntX - 1 ) ? this.px + ( this.ctX + 1 ) * this.ntW : this.ax + this.imgW;
		// Convert reference grid hi-res to component grid hi-res
		ntulx = ( ntulx + this.hd.getCompSubsX ( c ) - 1 ) / this.hd.getCompSubsX ( c );
		// Starting X of current tile at component grid hi-res is culx[c]
		// The difference at the rl level is the width
		return ( ntulx + ( 1 << dl ) - 1 ) / ( 1 << dl ) - ( this.culx[ c ] + ( 1 << dl ) - 1 ) / ( 1 << dl );
	}

	/**
	 * Returns the height in pixels of the specified tile-component for the
	 * given (tile-component) resolution level.
	 *
	 * @param t  The tile index.
	 * @param c  The index of the component, from 0 to N-1.
	 * @param rl The resolution level, from 0 to L.
	 * @return The height in pixels of component <tt>c</tt> in the current tile.
	 */
	@Override
	public final int getTileCompHeight ( final int t , final int c , final int rl ) {
		final int tIdx = this.getTileIdx ( );
		if ( t != tIdx ) {
			throw new Error ( "Asking the tile-component width of a tile " + "different  from the current one." );
		}
		int ntuly;
		final int dl = this.mdl[ c ] - rl; // Revert level indexation (0 is hi-res)
		// Calculate starting Y of next tile Y-wise at reference grid hi-res
		ntuly = ( this.ctY < this.ntY - 1 ) ? this.py + ( this.ctY + 1 ) * this.ntH : this.ay + this.imgH;
		// Convert reference grid hi-res to component grid hi-res
		ntuly = ( ntuly + this.hd.getCompSubsY ( c ) - 1 ) / this.hd.getCompSubsY ( c );
		// Starting Y of current tile at component grid hi-res is culy[c]
		// The difference at the rl level is the height
		return ( ntuly + ( 1 << dl ) - 1 ) / ( 1 << dl ) - ( this.culy[ c ] + ( 1 << dl ) - 1 ) / ( 1 << dl );
	}

	/**
	 * Returns the width in pixels of the specified component in the overall
	 * image, for the given (component) resolution level.
	 *
	 * <p>
	 * Note: Component resolution level indexes may differ from tile-component
	 * resolution index. They are indeed indexed starting from the lowest number
	 * of decomposition levels of same component of each tile.
	 *
	 * <p>
	 * Example: For an image (2 tiles) with 1 component (tile 0 having 2
	 * decomposition levels and tile 1 having 3 decomposition levels), the first
	 * tile(-component) has 3 resolution levels and the second one has 4
	 * resolution levels, whereas the component has only 3 resolution levels
	 * available.
	 *
	 * @param c  The index of the component, from 0 to N-1.
	 * @param rl The resolution level, from 0 to L.
	 * @return The width in pixels of component <tt>c</tt> in the overall image.
	 */
	@Override
	public final int getCompImgWidth ( final int c , final int rl ) {
		final int sx;
		final int ex;
		final int dl = this.decSpec.dls.getMinInComp ( c ) - rl;
		// indexation (0 is hi-res)
		// Calculate image starting x at component hi-res grid
		sx = ( this.ax + this.hd.getCompSubsX ( c ) - 1 ) / this.hd.getCompSubsX ( c );
		// Calculate image ending (excluding) x at component hi-res grid
		ex = ( this.ax + this.imgW + this.hd.getCompSubsX ( c ) - 1 ) / this.hd.getCompSubsX ( c );
		// The difference at the rl level is the width
		return ( ex + ( 1 << dl ) - 1 ) / ( 1 << dl ) - ( sx + ( 1 << dl ) - 1 ) / ( 1 << dl );
	}

	/**
	 * Returns the height in pixels of the specified component in the overall
	 * image, for the given (component) resolution level.
	 *
	 * <p>
	 * Note: Component resolution level indexes may differ from tile-component
	 * resolution index. They are indeed indexed starting from the lowest number
	 * of decomposition levels of same component of each tile.
	 *
	 * <p>
	 * Example: For an image (2 tiles) with 1 component (tile 0 having 2
	 * decomposition levels and tile 1 having 3 decomposition levels), the first
	 * tile(-component) has 3 resolution levels and the second one has 4
	 * resolution levels, whereas the component has only 3 resolution levels
	 * available.
	 *
	 * @param c  The index of the component, from 0 to N-1.
	 * @param rl The resolution level, from 0 to L.
	 * @return The height in pixels of component <tt>c</tt> in the overall
	 * image.
	 */
	@Override
	public final int getCompImgHeight ( final int c , final int rl ) {
		final int sy;
		final int ey;
		final int dl = this.decSpec.dls.getMinInComp ( c ) - rl;
		// indexation (0 is hi-res)
		// Calculate image starting x at component hi-res grid
		sy = ( this.ay + this.hd.getCompSubsY ( c ) - 1 ) / this.hd.getCompSubsY ( c );
		// Calculate image ending (excluding) x at component hi-res grid
		ey = ( this.ay + this.imgH + this.hd.getCompSubsY ( c ) - 1 ) / this.hd.getCompSubsY ( c );
		// The difference at the rl level is the width
		return ( ey + ( 1 << dl ) - 1 ) / ( 1 << dl ) - ( sy + ( 1 << dl ) - 1 ) / ( 1 << dl );
	}

	/**
	 * Changes the current tile, given the new indexes. An
	 * IllegalArgumentException is thrown if the indexes do not correspond to a
	 * valid tile.
	 *
	 * @param x The horizontal indexes the tile.
	 * @param y The vertical indexes of the new tile.
	 * @return The new tile index
	 */
	@Override
	public abstract int setTile ( int x , int y );

	/**
	 * Advances to the next tile, in standard scan-line order (by rows then
	 * columns). An NoNextElementException is thrown if the current tile is the
	 * last one (i.e. there is no next tile).
	 *
	 * @return The new tile index
	 */
	@Override
	public abstract int nextTile ( );

	/**
	 * Returns the indexes of the current tile. These are the horizontal and
	 * vertical indexes of the current tile.
	 *
	 * @param co If not null this object is used to return the information. If
	 *           null a new one is created and returned.
	 * @return The current tile's indexes (vertical and horizontal indexes).
	 */
	@Override
	public final Coord getTile ( final Coord co ) {
		if ( null != co ) {
			co.x = this.ctX;
			co.y = this.ctY;
			return co;
		}
		return new Coord ( this.ctX , this.ctY );
	}

	/**
	 * Returns the index of the current tile, relative to a standard scan-line
	 * order.
	 *
	 * @return The current tile's index (starts at 0).
	 */
	@Override
	public final int getTileIdx ( ) {
		return this.ctY * this.ntX + this.ctX;
	}

	/**
	 * Returns the horizontal coordinate of the upper-left corner of the
	 * specified resolution in the given component of the current tile.
	 *
	 * @param c  The component index.
	 * @param rl The resolution level index.
	 */
	@Override
	public final int getResULX ( final int c , final int rl ) {
		final int dl = this.mdl[ c ] - rl;
		if ( 0 > dl ) {
			throw new IllegalArgumentException ( "Requested resolution level" + " is not available for, at "
					+ "least, one component in " + "tile: " + this.ctX + "x" + this.ctY );
		}
		final int tx0 = Math.max ( this.px + this.ctX * this.ntW , this.ax );
		final int tcx0 = ( int ) Math.ceil ( tx0 / ( double ) this.getCompSubsX ( c ) );
		return ( int ) Math.ceil ( tcx0 / ( double ) ( 1 << dl ) );
	}

	/**
	 * Returns the vertical coordinate of the upper-left corner of the specified
	 * component in the given component of the current tile.
	 *
	 * @param c  The component index.
	 * @param rl The resolution level index.
	 */
	@Override
	public final int getResULY ( final int c , final int rl ) {
		final int dl = this.mdl[ c ] - rl;
		if ( 0 > dl ) {
			throw new IllegalArgumentException ( "Requested resolution level" + " is not available for, at "
					+ "least, one component in " + "tile: " + this.ctX + "x" + this.ctY );
		}
		final int ty0 = Math.max ( this.py + this.ctY * this.ntH , this.ay );
		final int tcy0 = ( int ) Math.ceil ( ty0 / ( double ) this.getCompSubsY ( c ) );
		return ( int ) Math.ceil ( tcy0 / ( double ) ( 1 << dl ) );
	}

	/**
	 * Returns the number of tiles in the horizontal and vertical directions.
	 *
	 * @param co If not null this object is used to return the information. If
	 *           null a new one is created and returned.
	 * @return The number of tiles in the horizontal (Coord.x) and vertical
	 * (Coord.y) directions.
	 */
	@Override
	public final Coord getNumTiles ( final Coord co ) {
		if ( null != co ) {
			co.x = this.ntX;
			co.y = this.ntY;
			return co;
		}
		return new Coord ( this.ntX , this.ntY );
	}

	/**
	 * Returns the total number of tiles in the image.
	 *
	 * @return The total number of tiles in the image.
	 */
	@Override
	public final int getNumTiles ( ) {
		return this.ntX * this.ntY;
	}

	/**
	 * Returns the subband tree, for the specified tile-component. This method
	 * returns the root element of the subband tree structure, see Subband and
	 * SubbandSyn. The tree comprises all the available resolution levels.
	 *
	 * <p>
	 * Note: this method is not able to return subband tree for a tile different
	 * than the current one.
	 *
	 * <p>
	 * The number of magnitude bits ('magBits' member variable) for each subband
	 * is not initialized.
	 *
	 * @param t The tile index
	 * @param c The index of the component, from 0 to C-1.
	 * @return The root of the tree structure.
	 */
	@Override
	public final SubbandSyn getSynSubbandTree ( final int t , final int c ) {
		if ( t != this.getTileIdx ( ) ) {
			throw new IllegalArgumentException ( "Can not request subband" + " tree of a different tile"
					+ " than the current one" );
		}
		if ( 0 > c || c >= this.nc ) {
			throw new IllegalArgumentException ( "Component index out of range" );
		}
		return this.subbTrees[ c ];
	}

	/**
	 * Returns the precinct partition width for the specified tile-component and
	 * (tile-component) resolution level.
	 *
	 * @param t  the tile index
	 * @param c  The index of the component (between 0 and N-1)
	 * @param rl The resolution level, from 0 to L.
	 * @return the precinct partition width for the specified component,
	 * resolution level and tile.
	 */
	public final int getPPX ( final int t , final int c , final int rl ) {
		return this.decSpec.pss.getPPX ( t , c , rl );
	}

	/**
	 * Returns the precinct partition height for the specified tile-component
	 * and (tile-component) resolution level.
	 *
	 * @param t  The tile index
	 * @param c  The index of the component (between 0 and N-1)
	 * @param rl The resolution level, from 0 to L.
	 * @return The precinct partition height in the specified component, for the
	 * specified resolution level, for the current tile.
	 */
	public final int getPPY ( final int t , final int c , final int rl ) {
		return this.decSpec.pss.getPPY ( t , c , rl );
	}

	/**
	 * Initialises subbands fields, such as number of code-blocks, code-blocks
	 * dimension and number of magnitude bits, in the subband tree. The nominal
	 * code-block width/height depends on the precincts dimensions if used. The
	 * way the number of magnitude bits is computed depends on the quantization
	 * type (reversible, derived, expounded).
	 *
	 * @param c  The component index
	 * @param sb The subband tree to be initialised.
	 */
	protected void initSubbandsFields ( final int c , final SubbandSyn sb ) {
		final int t = this.getTileIdx ( );
		final int rl = sb.resLvl;
		final int cbw;
		final int cbh;

		cbw = this.decSpec.cblks.getCBlkWidth ( ModuleSpec.SPEC_TILE_COMP , t , c );
		cbh = this.decSpec.cblks.getCBlkHeight ( ModuleSpec.SPEC_TILE_COMP , t , c );

		if ( ! sb.isNode ) {
			// Code-block dimensions
			if ( this.hd.precinctPartitionUsed ( ) ) {
				// The precinct partition is used
				final int ppxExp;
				int ppyExp;
				int cbwExp;
				final int cbhExp;

				// Get exponents
				ppxExp = MathUtil.log2 ( this.getPPX ( t , c , rl ) );
				ppyExp = MathUtil.log2 ( this.getPPY ( t , c , rl ) );
				cbwExp = MathUtil.log2 ( cbw );
				cbhExp = MathUtil.log2 ( cbh );

				if ( 0 == sb.resLvl ) {
					sb.nomCBlkW = ( cbwExp < ppxExp ? ( 1 << cbwExp ) : ( 1 << ppxExp ) );
					sb.nomCBlkH = ( cbhExp < ppyExp ? ( 1 << cbhExp ) : ( 1 << ppyExp ) );
				}
				else {
					sb.nomCBlkW = ( cbwExp < ppxExp - 1 ? ( 1 << cbwExp ) : ( 1 << ( ppxExp - 1 ) ) );
					sb.nomCBlkH = ( cbhExp < ppyExp - 1 ? ( 1 << cbhExp ) : ( 1 << ( ppyExp - 1 ) ) );
				}
			}
			else {
				sb.nomCBlkW = cbw;
				sb.nomCBlkH = cbh;
			}

			// Number of code-blocks
			if ( null == sb.numCb )
				sb.numCb = new Coord ( );
			if ( 0 == sb.w || 0 == sb.h ) {
				sb.numCb.x = 0;
				sb.numCb.y = 0;
			}
			else {
				final int cb0x = this.getCbULX ( );
				final int cb0y = this.getCbULY ( );
				int tmp;

				// Projects code-block partition origin to subband. Since the
				// origin is always 0 or 1, it projects to the low-pass side
				// (throught the ceil operator) as itself (i.e. no change) and
				// to the high-pass side (through the floor operator) as 0,
				// always.
				int acb0x = cb0x;
				int acb0y = cb0y;

				switch ( sb.sbandIdx ) {
					case Subband.WT_ORIENT_LL:
						// No need to project since all low-pass => nothing to
						// do
						break;
					case Subband.WT_ORIENT_HL:
						acb0x = 0;
						break;
					case Subband.WT_ORIENT_LH:
						acb0y = 0;
						break;
					case Subband.WT_ORIENT_HH:
						acb0x = 0;
						acb0y = 0;
						break;
					default:
						throw new Error ( "Internal JJ2000 error" );
				}
				if ( 0 > sb.ulcx - acb0x || 0 > sb.ulcy - acb0y ) {
					throw new IllegalArgumentException ( "Invalid code-blocks " + "partition origin or "
							+ "image offset in the " + "reference grid." );
				}

				// NOTE: when calculating "floor()" by integer division the
				// dividend and divisor must be positive, we ensure that by
				// adding the divisor to the dividend and then substracting 1
				// to the result of the division

				tmp = sb.ulcx - acb0x + sb.nomCBlkW;
				sb.numCb.x = ( tmp + sb.w - 1 ) / sb.nomCBlkW - ( tmp / sb.nomCBlkW - 1 );

				tmp = sb.ulcy - acb0y + sb.nomCBlkH;
				sb.numCb.y = ( tmp + sb.h - 1 ) / sb.nomCBlkH - ( tmp / sb.nomCBlkH - 1 );
			}

			// Number of magnitude bits
			if ( this.derived[ c ] ) {
				sb.magbits = this.gb[ c ] + ( this.params[ c ].exp[ 0 ][ 0 ] - ( this.mdl[ c ] - sb.level ) ) - 1;
			}
			else {
				sb.magbits = this.gb[ c ] + this.params[ c ].exp[ sb.resLvl ][ sb.sbandIdx ] - 1;
			}
		}
		else {
			this.initSubbandsFields ( c , ( SubbandSyn ) sb.getLL ( ) );
			this.initSubbandsFields ( c , ( SubbandSyn ) sb.getHL ( ) );
			this.initSubbandsFields ( c , ( SubbandSyn ) sb.getLH ( ) );
			this.initSubbandsFields ( c , ( SubbandSyn ) sb.getHH ( ) );
		}
	}

	/**
	 * Returns the image resolution level to reconstruct from the codestream.
	 * This value cannot be computed before every main and tile headers are
	 * read.
	 *
	 * @return The image resolution level
	 */
	public int getImgRes ( ) {
		return this.targetRes;
	}

	/**
	 * Return the target decoding rate in bits per pixel.
	 *
	 * @return Target decoding rate in bpp.
	 */
	public float getTargetRate ( ) {
		return this.trate;
	}

	/**
	 * Return the actual decoding rate in bits per pixel.
	 *
	 * @return Actual decoding rate in bpp.
	 */
	public float getActualRate ( ) {
		this.arate = this.anbytes * 8.0f / this.hd.getMaxCompImgWidth ( ) / this.hd.getMaxCompImgHeight ( );
		return this.arate;
	}

	/**
	 * Return the target number of read bytes.
	 *
	 * @return Target decoding rate in bytes.
	 */
	public int getTargetNbytes ( ) {
		return this.tnbytes;
	}

	/**
	 * Return the actual number of read bytes.
	 *
	 * @return Actual decoding rate in bytes.
	 */
	public int getActualNbytes ( ) {
		return this.anbytes;
	}

	/**
	 * Returns the horizontal offset of tile partition
	 */
	@Override
	public int getTilePartULX ( ) {
		return this.hd.getTilingOrigin ( null ).x;
	}

	/**
	 * Returns the vertical offset of tile partition
	 */
	@Override
	public int getTilePartULY ( ) {
		return this.hd.getTilingOrigin ( null ).y;
	}

	/**
	 * Returns the nominal tile width
	 */
	@Override
	public int getNomTileWidth ( ) {
		return this.hd.getNomTileWidth ( );
	}

	/**
	 * Returns the nominal tile height
	 */
	@Override
	public int getNomTileHeight ( ) {
		return this.hd.getNomTileHeight ( );
	}
}
