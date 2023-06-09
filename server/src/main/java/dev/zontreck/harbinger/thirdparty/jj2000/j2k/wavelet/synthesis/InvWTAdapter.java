/*
 * CVS identifier:
 *
 * $Id: InvWTAdapter.java,v 1.14 2002/07/25 15:11:03 grosbois Exp $
 *
 * Class:                   InvWTAdapter
 *
 * Description:             <short description of class>
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
package dev.zontreck.harbinger.thirdparty.jj2000.j2k.wavelet.synthesis;

import dev.zontreck.harbinger.thirdparty.jj2000.j2k.decoder.DecoderSpecs;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.image.Coord;

/**
 * This class provides default implementation of the methods in the 'InvWT'
 * interface. The source is always a 'MultiResImgData', which is a
 * multi-resolution image. The default implementation is just to return the
 * value of the source at the current image resolution level, which is set by
 * the 'setImgResLevel()' method.
 *
 * <p>
 * This abstract class can be used to facilitate the development of other
 * classes that implement the 'InvWT' interface, because most of the trivial
 * methods are already implemented.
 *
 * <p>
 * If the default implementation of a method provided in this class does not
 * suit a particular implementation of the 'InvWT' interface, the method can be
 * overriden to implement the proper behaviour.
 *
 * <p>
 * If the 'setImgResLevel()' method is overriden then it is very important that
 * the one of this class is called from the overriding method, so that the other
 * methods in this class return the correct values.
 *
 * @see InvWT
 */
public abstract class InvWTAdapter implements InvWT {
	/**
	 * The decoder specifications
	 */
	protected DecoderSpecs decSpec;

	/**
	 * The 'MultiResImgData' source
	 */
	protected MultiResImgData mressrc;

	/**
	 * The resquested image resolution level for reconstruction.
	 */
	protected int reslvl;

	/**
	 * The maximum available image resolution level
	 */
	protected int maxImgRes;

	/**
	 * Instantiates the 'InvWTAdapter' object using the specified
	 * 'MultiResImgData' source. The reconstruction resolution level is set to
	 * full resolution (i.e. the maximum resolution level).
	 *
	 * @param src     From where to obtain the values to return
	 * @param decSpec The decoder specifications
	 */
	protected InvWTAdapter ( final MultiResImgData src , final DecoderSpecs decSpec ) {
		this.mressrc = src;
		this.decSpec = decSpec;
		this.maxImgRes = decSpec.dls.getMin ( );
	}

	/**
	 * Sets the image reconstruction resolution level. A value of 0 means
	 * reconstruction of an image with the lowest resolution (dimension)
	 * available.
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
	 * @return The vertical coordinate of the image origin in the canvas system,
	 * on the reference grid.
	 */
	@Override
	public void setImgResLevel ( final int rl ) {
		if ( 0 > rl ) {
			throw new IllegalArgumentException ( "Resolution level index cannot be negative." );
		}
		this.reslvl = rl;
	}

	/**
	 * Returns the overall width of the current tile in pixels. This is the
	 * tile's width without accounting for any component subsampling. This is
	 * also referred as the reference grid width in the current tile.
	 *
	 * <p>
	 * This default implementation returns the value of the source at the
	 * current reconstruction resolution level.
	 *
	 * @return The total current tile's width in pixels.
	 */
	@Override
	public int getTileWidth ( ) {
		// Retrieves the tile maximum resolution level index and request the
		// width from the source module.
		final int tIdx = this.getTileIdx ( );
		int rl = 10000;
		int mrl;
		final int nc = this.mressrc.getNumComps ( );
		for ( int c = 0 ; c < nc ; c++ ) {
			mrl = this.mressrc.getSynSubbandTree ( tIdx , c ).resLvl;
			if ( mrl < rl )
				rl = mrl;
		}
		return this.mressrc.getTileWidth ( rl );
	}

	/**
	 * Returns the overall height of the current tile in pixels. This is the
	 * tile's height without accounting for any component subsampling. This is
	 * also referred as the reference grid height in the current tile.
	 *
	 * <p>
	 * This default implementation returns the value of the source at the
	 * current reconstruction resolution level.
	 *
	 * @return The total current tile's height in pixels.
	 */
	@Override
	public int getTileHeight ( ) {
		// Retrieves the tile maximum resolution level index and request the
		// height from the source module.
		final int tIdx = this.getTileIdx ( );
		int rl = 10000;
		int mrl;
		final int nc = this.mressrc.getNumComps ( );
		for ( int c = 0 ; c < nc ; c++ ) {
			mrl = this.mressrc.getSynSubbandTree ( tIdx , c ).resLvl;
			if ( mrl < rl )
				rl = mrl;
		}
		return this.mressrc.getTileHeight ( rl );
	}

	/**
	 * Returns the nominal width of tiles
	 */
	@Override
	public int getNomTileWidth ( ) {
		return this.mressrc.getNomTileWidth ( );
	}

	/**
	 * Returns the nominal height of tiles
	 */
	@Override
	public int getNomTileHeight ( ) {
		return this.mressrc.getNomTileHeight ( );
	}

	/**
	 * Returns the overall width of the image in pixels. This is the image's
	 * width without accounting for any component subsampling or tiling.
	 *
	 * @return The total image's width in pixels.
	 */
	@Override
	public int getImgWidth ( ) {
		return this.mressrc.getImgWidth ( this.reslvl );
	}

	/**
	 * Returns the overall height of the image in pixels. This is the image's
	 * height without accounting for any component subsampling or tiling.
	 *
	 * @return The total image's height in pixels.
	 */
	@Override
	public int getImgHeight ( ) {
		return this.mressrc.getImgHeight ( this.reslvl );
	}

	/**
	 * Returns the number of components in the image.
	 *
	 * @return The number of components in the image.
	 */
	@Override
	public int getNumComps ( ) {
		return this.mressrc.getNumComps ( );
	}

	/**
	 * Returns the component subsampling factor in the horizontal direction, for
	 * the specified component. This is, approximately, the ratio of dimensions
	 * between the reference grid and the component itself, see the 'ImgData'
	 * interface desription for details.
	 *
	 * @param c The index of the component (between 0 and N-1).
	 * @return The horizontal subsampling factor of component 'c'.
	 * @see dev.zontreck.harbinger.thirdparty.jj2000.j2k.image.ImgData
	 */
	@Override
	public int getCompSubsX ( final int c ) {
		return this.mressrc.getCompSubsX ( c );
	}

	/**
	 * Returns the component subsampling factor in the vertical direction, for
	 * the specified component. This is, approximately, the ratio of dimensions
	 * between the reference grid and the component itself, see the 'ImgData'
	 * interface desription for details.
	 *
	 * @param c The index of the component (between 0 and N-1).
	 * @return The vertical subsampling factor of component 'c'.
	 * @see dev.zontreck.harbinger.thirdparty.jj2000.j2k.image.ImgData
	 */
	@Override
	public int getCompSubsY ( final int c ) {
		return this.mressrc.getCompSubsY ( c );
	}

	/**
	 * Returns the width in pixels of the specified tile-component
	 *
	 * @param t Tile index
	 * @param c The index of the component, from 0 to N-1.
	 * @return The width in pixels of component <tt>n</tt> in tile <tt>t</tt>.
	 */
	@Override
	public int getTileCompWidth ( final int t , final int c ) {
		// Retrieves the tile-component maximum resolution index and gets the
		// width from the source.
		final int rl = this.mressrc.getSynSubbandTree ( t , c ).resLvl;
		return this.mressrc.getTileCompWidth ( t , c , rl );
	}

	/**
	 * Returns the height in pixels of the specified tile-component.
	 *
	 * <p>
	 * This default implementation returns the value of the source at the
	 * current reconstruction resolution level.
	 *
	 * @param t The tile index.
	 * @param c The index of the component, from 0 to N-1.
	 * @return The height in pixels of component <tt>n</tt> in tile <tt>t</tt>.
	 */
	@Override
	public int getTileCompHeight ( final int t , final int c ) {
		// Retrieves the tile-component maximum resolution index and gets the
		// height from the source.
		final int rl = this.mressrc.getSynSubbandTree ( t , c ).resLvl;
		return this.mressrc.getTileCompHeight ( t , c , rl );
	}

	/**
	 * Returns the width in pixels of the specified component in the overall
	 * image.
	 *
	 * @param c The index of the component, from 0 to N-1.
	 * @return The width in pixels of component <tt>c</tt> in the overall image.
	 */
	@Override
	public int getCompImgWidth ( final int c ) {
		// Retrieves the component maximum resolution index and gets the width
		// from the source module.
		final int rl = this.decSpec.dls.getMinInComp ( c );
		return this.mressrc.getCompImgWidth ( c , rl );
	}

	/**
	 * Returns the height in pixels of the specified component in the overall
	 * image.
	 *
	 * <p>
	 * This default implementation returns the value of the source at the
	 * current reconstruction resolution level.
	 *
	 * @param c The index of the component, from 0 to N-1.
	 * @return The height in pixels of component <tt>n</tt> in the overall
	 * image.
	 */
	@Override
	public int getCompImgHeight ( final int c ) {
		// Retrieves the component maximum resolution index and gets the
		// height from the source module.
		final int rl = this.decSpec.dls.getMinInComp ( c );
		return this.mressrc.getCompImgHeight ( c , rl );
	}

	/**
	 * Changes the current tile, given the new indices. An
	 * IllegalArgumentException is thrown if the coordinates do not correspond
	 * to a valid tile.
	 *
	 * <p>
	 * This default implementation calls the same method on the source.
	 *
	 * @param x The horizontal index of the tile.
	 * @param y The vertical index of the new tile.
	 * @returns The new tile index
	 */
	@Override
	public int setTile ( final int x , final int y ) {
		return this.mressrc.setTile ( x , y );
	}

	/**
	 * Advances to the next tile, in standard scan-line order (by rows then
	 * columns). An NoNextElementException is thrown if the current tile is the
	 * last one (i.e. there is no next tile).
	 *
	 * <p>
	 * This default implementation calls the same method on the source.
	 *
	 * @returns The new tile index
	 */
	@Override
	public int nextTile ( ) {
		return this.mressrc.nextTile ( );
	}

	/**
	 * Returns the indixes of the current tile. These are the horizontal and
	 * vertical indexes of the current tile.
	 *
	 * <p>
	 * This default implementation returns the value of the source.
	 *
	 * @param co If not null this object is used to return the information. If
	 *           null a new one is created and returned.
	 * @return The current tile's indices (vertical and horizontal indexes).
	 */
	@Override
	public Coord getTile ( final Coord co ) {
		return this.mressrc.getTile ( co );
	}

	/**
	 * Returns the index of the current tile, relative to a standard scan-line
	 * order.
	 *
	 * <p>
	 * This default implementation returns the value of the source.
	 *
	 * @return The current tile's index (starts at 0).
	 */
	@Override
	public int getTileIdx ( ) {
		return this.mressrc.getTileIdx ( );
	}

	/**
	 * Returns the horizontal coordinate of the upper-left corner of the
	 * specified component in the current tile.
	 *
	 * @param c The component index.
	 */
	@Override
	public int getCompULX ( final int c ) {
		// Find tile-component maximum resolution index and gets information
		// from the source module.
		final int tIdx = this.getTileIdx ( );
		final int rl = this.mressrc.getSynSubbandTree ( tIdx , c ).resLvl;
		return this.mressrc.getResULX ( c , rl );
	}

	/**
	 * Returns the vertical coordinate of the upper-left corner of the specified
	 * component in the current tile.
	 *
	 * @param c The component index.
	 */
	@Override
	public int getCompULY ( final int c ) {
		// Find tile-component maximum resolution index and gets information
		// from the source module.
		final int tIdx = this.getTileIdx ( );
		final int rl = this.mressrc.getSynSubbandTree ( tIdx , c ).resLvl;
		return this.mressrc.getResULY ( c , rl );
	}

	/**
	 * Returns the horizontal coordinate of the image origin, the top-left
	 * corner, in the canvas system, on the reference grid.
	 *
	 * <p>
	 * This default implementation returns the value of the source at the
	 * current reconstruction resolution level.
	 *
	 * @return The horizontal coordinate of the image origin in the canvas
	 * system, on the reference grid.
	 */
	@Override
	public int getImgULX ( ) {
		return this.mressrc.getImgULX ( this.reslvl );
	}

	/**
	 * Returns the vertical coordinate of the image origin, the top-left corner,
	 * in the canvas system, on the reference grid.
	 *
	 * <p>
	 * This default implementation returns the value of the source at the
	 * current reconstruction resolution level.
	 *
	 * @return The vertical coordinate of the image origin in the canvas system,
	 * on the reference grid.
	 */
	@Override
	public int getImgULY ( ) {
		return this.mressrc.getImgULY ( this.reslvl );
	}

	/**
	 * Returns the horizontal tile partition offset in the reference grid
	 */
	@Override
	public int getTilePartULX ( ) {
		return this.mressrc.getTilePartULX ( );
	}

	/**
	 * Returns the vertical tile partition offset in the reference grid
	 */
	@Override
	public int getTilePartULY ( ) {
		return this.mressrc.getTilePartULY ( );
	}

	/**
	 * Returns the number of tiles in the horizontal and vertical directions.
	 *
	 * <p>
	 * This default implementation returns the value of the source.
	 *
	 * @param co If not null this object is used to return the information. If
	 *           null a new one is created and returned.
	 * @return The number of tiles in the horizontal (Coord.x) and vertical
	 * (Coord.y) directions.
	 */
	@Override
	public Coord getNumTiles ( final Coord co ) {
		return this.mressrc.getNumTiles ( co );
	}

	/**
	 * Returns the total number of tiles in the image.
	 *
	 * <p>
	 * This default implementation returns the value of the source.
	 *
	 * @return The total number of tiles in the image.
	 */
	@Override
	public int getNumTiles ( ) {
		return this.mressrc.getNumTiles ( );
	}

	/**
	 * Returns the specified synthesis subband tree
	 *
	 * @param t Tile index.
	 * @param c Component index.
	 */
	public SubbandSyn getSynSubbandTree ( final int t , final int c ) {
		return this.mressrc.getSynSubbandTree ( t , c );
	}
}
