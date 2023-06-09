/*
 * CVS identifier:
 *
 * $Id: MultiResImgDataAdapter.java,v 1.10 2002/07/25 15:11:55 grosbois Exp $
 *
 * Class:                   MultiResImgDataAdapter
 *
 * Description:             A default implementation of the MultiResImgData
 *                          interface that has and MultiResImgData source
 *                          and just returns the values of the source.
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

import dev.zontreck.harbinger.thirdparty.jj2000.j2k.image.Coord;

/**
 * This class provides a default implementation for the methods of the
 * 'MultiResImgData' interface. The default implementation consists just in
 * returning the value of the source, where the source is another
 * 'MultiResImgData' object.
 *
 * <p>
 * This abstract class can be used to facilitate the development of other
 * classes that implement 'MultiResImgData'. For example a dequantizer can
 * inherit from this class and all the trivial methods do not have to be
 * reimplemented.
 *
 * <p>
 * If the default implementation of a method provided in this class does not
 * suit a particular implementation of the 'MultiResImgData' interface, the
 * method can be overriden to implement the proper behaviour.
 *
 * @see MultiResImgData
 */
public abstract class MultiResImgDataAdapter implements MultiResImgData {
	/**
	 * Index of the current tile
	 */
	protected int tIdx;

	/**
	 * The MultiResImgData source
	 */
	protected MultiResImgData mressrc;

	/**
	 * Instantiates the MultiResImgDataAdapter object specifying the
	 * MultiResImgData source.
	 *
	 * @param src From where to obrtain the MultiResImgData values.
	 */
	protected MultiResImgDataAdapter ( final MultiResImgData src ) {
		this.mressrc = src;
	}

	/**
	 * Returns the overall width of the current tile in pixels, for the given
	 * resolution level. This is the tile's width without accounting for any
	 * component subsampling.
	 *
	 * <p>
	 * This default implementation returns the value of the source.
	 *
	 * @param rl The resolution level, from 0 to L.
	 * @return The total current tile's width in pixels.
	 */
	@Override
	public int getTileWidth ( final int rl ) {
		return this.mressrc.getTileWidth ( rl );
	}

	/**
	 * Returns the overall height of the current tile in pixels, for the given
	 * resolution level. This is the tile's height without accounting for any
	 * component subsampling.
	 *
	 * <p>
	 * This default implementation returns the value of the source.
	 *
	 * @param rl The resolution level, from 0 to L.
	 * @return The total current tile's height in pixels.
	 */
	@Override
	public int getTileHeight ( final int rl ) {
		return this.mressrc.getTileHeight ( rl );
	}

	/**
	 * Returns the nominal tiles width
	 */
	@Override
	public int getNomTileWidth ( ) {
		return this.mressrc.getNomTileWidth ( );
	}

	/**
	 * Returns the nominal tiles height
	 */
	@Override
	public int getNomTileHeight ( ) {
		return this.mressrc.getNomTileHeight ( );
	}

	/**
	 * Returns the overall width of the image in pixels, for the given
	 * resolution level. This is the image's width without accounting for any
	 * component subsampling or tiling.
	 *
	 * <p>
	 * This default implementation returns the value of the source.
	 *
	 * @param rl The resolution level, from 0 to L.
	 * @return The total image's width in pixels.
	 */
	@Override
	public int getImgWidth ( final int rl ) {
		return this.mressrc.getImgWidth ( rl );
	}

	/**
	 * Returns the overall height of the image in pixels, for the given
	 * resolution level. This is the image's height without accounting for any
	 * component subsampling or tiling.
	 *
	 * <p>
	 * This default implementation returns the value of the source.
	 *
	 * @param rl The resolution level, from 0 to L.
	 * @return The total image's height in pixels.
	 */
	@Override
	public int getImgHeight ( final int rl ) {
		return this.mressrc.getImgHeight ( rl );
	}

	/**
	 * Returns the number of components in the image.
	 *
	 * <p>
	 * This default implementation returns the value of the source.
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
	 * <p>
	 * This default implementation returns the value of the source.
	 *
	 * @param c The index of the component (between 0 and N-1)
	 * @return The horizontal subsampling factor of component 'c'
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
	 * <p>
	 * This default implementation returns the value of the source.
	 *
	 * @param c The index of the component (between 0 and N-1)
	 * @return The vertical subsampling factor of component 'c'
	 * @see dev.zontreck.harbinger.thirdparty.jj2000.j2k.image.ImgData
	 */
	@Override
	public int getCompSubsY ( final int c ) {
		return this.mressrc.getCompSubsY ( c );
	}

	/**
	 * Returns the width in pixels of the specified tile-component for the given
	 * resolution level.
	 *
	 * <p>
	 * This default implementation returns the value of the source.
	 *
	 * @param t  Tile index.
	 * @param c  The index of the component, from 0 to N-1.
	 * @param rl The resolution level, from 0 to L.
	 * @return The width in pixels of component <tt>c</tt> in tile <tt>t</tt>
	 * for resolution level <tt>rl</tt>.
	 */
	@Override
	public int getTileCompWidth ( final int t , final int c , final int rl ) {
		return this.mressrc.getTileCompWidth ( t , c , rl );
	}

	/**
	 * Returns the height in pixels of the specified tile-component for the
	 * given resolution level.
	 *
	 * <p>
	 * This default implementation returns the value of the source.
	 *
	 * @param t  The tile index.
	 * @param c  The index of the component, from 0 to N-1.
	 * @param rl The resolution level, from 0 to L.
	 * @return The height in pixels of component <tt>c</tt> in tile <tt>t</tt>.
	 */
	@Override
	public int getTileCompHeight ( final int t , final int c , final int rl ) {
		return this.mressrc.getTileCompHeight ( t , c , rl );
	}

	/**
	 * Returns the width in pixels of the specified component in the overall
	 * image, for the given resolution level.
	 *
	 * <p>
	 * This default implementation returns the value of the source.
	 *
	 * @param c  The index of the component, from 0 to N-1.
	 * @param rl The resolution level, from 0 to L.
	 * @return The width in pixels of component <tt>c</tt> in the overall image.
	 */
	@Override
	public int getCompImgWidth ( final int c , final int rl ) {
		return this.mressrc.getCompImgWidth ( c , rl );
	}

	/**
	 * Returns the height in pixels of the specified component in the overall
	 * image, for the given resolution level.
	 *
	 * <p>
	 * This default implementation returns the value of the source.
	 *
	 * @param c  The index of the component, from 0 to N-1.
	 * @param rl The resolution level, from 0 to L.
	 * @return The height in pixels of component <tt>c</tt> in the overall
	 * image.
	 */
	@Override
	public int getCompImgHeight ( final int c , final int rl ) {
		return this.mressrc.getCompImgHeight ( c , rl );
	}

	/**
	 * Changes the current tile, given the new indexes. An
	 * IllegalArgumentException is thrown if the indexes do not correspond to a
	 * valid tile.
	 *
	 * <p>
	 * This default implementation just changes the tile in the source.
	 *
	 * @param x The horizontal indexes the tile.
	 * @param y The vertical indexes of the new tile.
	 * @return The new tile index
	 */
	@Override
	public int setTile ( final int x , final int y ) {
		this.tIdx = this.mressrc.setTile ( x , y );
		return this.tIdx;
	}

	/**
	 * Advances to the next tile, in standard scan-line order (by rows then
	 * columns). An NoNextElementException is thrown if the current tile is the
	 * last one (i.e. there is no next tile).
	 *
	 * <p>
	 * This default implementation just changes the tile in the source.
	 *
	 * @return The new tile index
	 */
	@Override
	public int nextTile ( ) {
		this.tIdx = this.mressrc.nextTile ( );
		return this.tIdx;
	}

	/**
	 * Returns the indexes of the current tile. These are the horizontal and
	 * vertical indexes of the current tile.
	 *
	 * <p>
	 * This default implementation returns the value of the source.
	 *
	 * @param co If not null this object is used to return the information. If
	 *           null a new one is created and returned.
	 * @return The current tile's indexes (vertical and horizontal indexes).
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
	 * specified resolution level in the given component of the current tile.
	 *
	 * <p>
	 * This default implementation returns the value of the source.
	 *
	 * @param c  The component index.
	 * @param rl The resolution level index.
	 */
	@Override
	public int getResULX ( final int c , final int rl ) {
		return this.mressrc.getResULX ( c , rl );
	}

	/**
	 * Returns the vertical coordinate of the upper-left corner of the specified
	 * resolution in the given component of the current tile.
	 *
	 * <p>
	 * This default implementation returns the value of the source.
	 *
	 * @param c  The component index.
	 * @param rl The resolution level index.
	 */
	@Override
	public int getResULY ( final int c , final int rl ) {
		return this.mressrc.getResULY ( c , rl );
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
	 * Returns the horizontal coordinate of the image origin, the top-left
	 * corner, in the canvas system, on the reference grid at the specified
	 * resolution level.
	 *
	 * <p>
	 * This default implementation returns the value of the source.
	 *
	 * @param rl The resolution level, from 0 to L.
	 * @return The horizontal coordinate of the image origin in the canvas
	 * system, on the reference grid.
	 */
	@Override
	public int getImgULX ( final int rl ) {
		return this.mressrc.getImgULX ( rl );
	}

	/**
	 * Returns the vertical coordinate of the image origin, the top-left corner,
	 * in the canvas system, on the reference grid at the specified resolution
	 * level.
	 *
	 * <p>
	 * This default implementation returns the value of the source.
	 *
	 * @param rl The resolution level, from 0 to L.
	 * @return The vertical coordinate of the image origin in the canvas system,
	 * on the reference grid.
	 */
	@Override
	public int getImgULY ( final int rl ) {
		return this.mressrc.getImgULY ( rl );
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
}
