/*
 * CVS identifier:
 *
 * $Id: Tiler.java,v 1.34 2001/09/14 09:16:09 grosbois Exp $
 *
 * Class:                   Tiler
 *
 * Description:             An object to create TiledImgData from
 *                          ImgData
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
package dev.zontreck.harbinger.thirdparty.jj2000.j2k.image;

import dev.zontreck.harbinger.thirdparty.jj2000.j2k.util.*;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.*;

/**
 * This class places an image in the canvas coordinate system, tiles it, if so
 * specified, and performs the coordinate conversions transparently. The source
 * must be a 'BlkImgDataSrc' which is not tiled and has a the image origin at
 * the canvas origin (i.e. it is not "canvased"), or an exception is thrown by
 * the constructor. A tiled and "canvased" output is given through the
 * 'BlkImgDataSrc' interface. See the 'ImgData' interface for a description of
 * the canvas and tiling.
 * 
 * <p>
 * All tiles produced are rectangular, non-overlapping and their union covers
 * all the image. However, the tiling may not be uniform, depending on the
 * nominal tile size, tiling origin, component subsampling and other factors.
 * Therefore it might not be assumed that all tiles are of the same width and
 * height.
 * 
 * <p>
 * The nominal dimension of the tiles is the maximal one, in the reference grid.
 * All the components of the image have the same number of tiles.
 * 
 * @see ImgData
 * @see BlkImgDataSrc
 */
public class Tiler extends ImgDataAdapter implements BlkImgDataSrc
{

	/** The source of image data */
	private BlkImgDataSrc src;

	/** Horizontal coordinate of the upper left hand reference grid point. */
	private final int x0siz;

	/** Vertical coordinate of the upper left hand reference grid point. */
	private final int y0siz;

	/**
	 * The horizontal coordinate of the tiling origin in the canvas system, on
	 * the reference grid.
	 */
	private int xt0siz;

	/**
	 * The vertical coordinate of the tiling origin in the canvas system, on the
	 * reference grid.
	 */
	private int yt0siz;

	/**
	 * The nominal width of the tiles, on the reference grid. If 0 then there is
	 * no tiling in that direction.
	 */
	private int xtsiz;

	/**
	 * The nominal height of the tiles, on the reference grid. If 0 then there
	 * is no tiling in that direction.
	 */
	private int ytsiz;

	/** The number of tiles in the horizontal direction. */
	private final int ntX;

	/** The number of tiles in the vertical direction. */
	private final int ntY;

	/** The component width in the current active tile, for each component */
	private int[] compW;

	/** The component height in the current active tile, for each component */
	private int[] compH;

	/**
	 * The horizontal coordinates of the upper-left corner of the components in
	 * the current tile
	 */
	private int[] tcx0;

	/**
	 * The vertical coordinates of the upper-left corner of the components in
	 * the current tile.
	 */
	private int[] tcy0;

	/** The horizontal index of the current tile */
	private int tx;

	/** The vertical index of the current tile */
	private int ty;

	/** The width of the current tile, on the reference grid. */
	private int tileW;

	/** The height of the current tile, on the reference grid. */
	private int tileH;

	/**
	 * Constructs a new tiler with the specified 'BlkImgDataSrc' source, image
	 * origin, tiling origin and nominal tile size.
	 * 
	 * @param src
	 *            The 'BlkImgDataSrc' source from where to get the image data.
	 *            It must not be tiled and the image origin must be at '(0,0)'
	 *            on its canvas.
	 * 
	 * @param ax
	 *            The horizontal coordinate of the image origin in the canvas
	 *            system, on the reference grid (i.e. the image's top-left
	 *            corner in the reference grid).
	 * 
	 * @param ay
	 *            The vertical coordinate of the image origin in the canvas
	 *            system, on the reference grid (i.e. the image's top-left
	 *            corner in the reference grid).
	 * 
	 * @param px
	 *            The horizontal tiling origin, in the canvas system, on the
	 *            reference grid. It must satisfy 'px<=ax'.
	 * 
	 * @param py
	 *            The vertical tiling origin, in the canvas system, on the
	 *            reference grid. It must satisfy 'py<=ay'.
	 * 
	 * @param nw
	 *            The nominal tile width, on the reference grid. If 0 then there
	 *            is no tiling in that direction.
	 * 
	 * @param nh
	 *            The nominal tile height, on the reference grid. If 0 then
	 *            there is no tiling in that direction.
	 * 
	 * @exception IllegalArgumentException
	 *                If src is tiled or "canvased", or if the arguments do not
	 *                satisfy the specified constraints.
	 */
	public Tiler(final BlkImgDataSrc src, final int ax, final int ay, final int px, final int py, final int nw, final int nh)
	{
		super(src);

		// Initialize
		this.src = src;
		x0siz = ax;
		y0siz = ay;
		xt0siz = px;
		yt0siz = py;
		xtsiz = nw;
		ytsiz = nh;

		// Verify that input is not tiled
		if (1 != src.getNumTiles())
		{
			throw new IllegalArgumentException("Source is tiled");
		}
		// Verify that source is not "canvased"
		if (0 != src.getImgULX() || 0 != src.getImgULY())
		{
			throw new IllegalArgumentException("Source is \"canvased\"");
		}
		// Verify that arguments satisfy trivial requirements
		if (0 > x0siz || 0 > y0siz || 0 > xt0siz || 0 > yt0siz || 0 > xtsiz || 0 > ytsiz || this.xt0siz > this.x0siz
				|| this.yt0siz > this.y0siz)
		{
			throw new IllegalArgumentException("Invalid image origin, tiling origin or nominal tile size");
		}

		// If no tiling has been specified, creates a unique tile with maximum
		// dimension.
		if (0 == xtsiz)
			this.xtsiz = this.x0siz + src.getImgWidth() - this.xt0siz;
		if (0 == ytsiz)
			this.ytsiz = this.y0siz + src.getImgHeight() - this.yt0siz;

		// Automatically adjusts xt0siz,yt0siz so that tile (0,0) always
		// overlaps with the image.
		if (this.x0siz - this.xt0siz >= this.xtsiz)
		{
			this.xt0siz += ((this.x0siz - this.xt0siz) / this.xtsiz) * this.xtsiz;
		}
		if (this.y0siz - this.yt0siz >= this.ytsiz)
		{
			this.yt0siz += ((this.y0siz - this.yt0siz) / this.ytsiz) * this.ytsiz;
		}
		if (this.x0siz - this.xt0siz >= this.xtsiz || this.y0siz - this.yt0siz >= this.ytsiz)
		{
			FacilityManager.getMsgLogger().printmsg(
					MsgLogger.INFO,
					"Automatically adjusted tiling origin to equivalent one (" + this.xt0siz + "," + this.yt0siz
							+ ") so that first tile overlaps the image");
		}

		// Calculate the number of tiles
		this.ntX = (int) Math.ceil((this.x0siz + src.getImgWidth()) / (double) this.xtsiz);
		this.ntY = (int) Math.ceil((this.y0siz + src.getImgHeight()) / (double) this.ytsiz);
	}

	/**
	 * Returns the overall width of the current tile in pixels. This is the
	 * tile's width without accounting for any component subsampling.
	 * 
	 * @return The total current tile width in pixels.
	 */
	@Override
	public final int getTileWidth()
	{
		return this.tileW;
	}

	/**
	 * Returns the overall height of the current tile in pixels. This is the
	 * tile's width without accounting for any component subsampling.
	 * 
	 * @return The total current tile height in pixels.
	 */
	@Override
	public final int getTileHeight()
	{
		return this.tileH;
	}

	/**
	 * Returns the width in pixels of the specified tile-component.
	 * 
	 * @param t
	 *            Tile index
	 * 
	 * @param c
	 *            The index of the component, from 0 to N-1.
	 * 
	 * @return The width of specified tile-component.
	 */
	@Override
	public final int getTileCompWidth(final int t, final int c)
	{
		if (t != this.getTileIdx())
		{
			throw new Error("Asking the width of a tile-component which is "
					+ "not in the current tile (call setTile() or nextTile() methods before).");
		}
		return this.compW[c];
	}

	/**
	 * Returns the height in pixels of the specified tile-component.
	 * 
	 * @param t
	 *            The tile index.
	 * 
	 * @param c
	 *            The index of the component, from 0 to N-1.
	 * 
	 * @return The height of specified tile-component.
	 */
	@Override
	public final int getTileCompHeight(final int t, final int c)
	{
		if (t != this.getTileIdx())
		{
			throw new Error("Asking the width of a tile-component which is "
					+ "not in the current tile (call setTile() or nextTile() methods before).");
		}
		return this.compH[c];
	}

	/**
	 * Returns the position of the fixed point in the specified component. This
	 * is the position of the least significant integral (i.e. non-fractional)
	 * bit, which is equivalent to the number of fractional bits. For instance,
	 * for fixed-point values with 2 fractional bits, 2 is returned. For
	 * floating-point data this value does not apply and 0 should be returned.
	 * Position 0 is the position of the least significant bit in the data.
	 * 
	 * @param c
	 *            The index of the component.
	 * 
	 * @return The position of the fixed-point, which is the same as the number
	 *         of fractional bits. For floating-point data 0 is returned.
	 */
	@Override
	public int getFixedPoint(final int c)
	{
		return this.src.getFixedPoint(c);
	}

	/**
	 * Returns, in the blk argument, a block of image data containing the
	 * specifed rectangular area, in the specified component. The data is
	 * returned, as a reference to the internal data, if any, instead of as a
	 * copy, therefore the returned data should not be modified.
	 * 
	 * <p>
	 * The rectangular area to return is specified by the 'ulx', 'uly', 'w' and
	 * 'h' members of the 'blk' argument, relative to the current tile. These
	 * members are not modified by this method. The 'offset' and 'scanw' of the
	 * returned data can be arbitrary. See the 'DataBlk' class.
	 * 
	 * <p>
	 * This method, in general, is more efficient than the 'getCompData()'
	 * method since it may not copy the data. However if the array of returned
	 * data is to be modified by the caller then the other method is probably
	 * preferable.
	 * 
	 * <p>
	 * If the data array in <tt>blk</tt> is <tt>null</tt>, then a new one is
	 * created if necessary. The implementation of this interface may choose to
	 * return the same array or a new one, depending on what is more efficient.
	 * Therefore, the data array in <tt>blk</tt> prior to the method call should
	 * not be considered to contain the returned data, a new array may have been
	 * created. Instead, get the array from <tt>blk</tt> after the method has
	 * returned.
	 * 
	 * <p>
	 * The returned data may have its 'progressive' attribute set. In this case
	 * the returned data is only an approximation of the "final" data.
	 * 
	 * @param blk
	 *            Its coordinates and dimensions specify the area to return,
	 *            relative to the current tile. Some fields in this object are
	 *            modified to return the data.
	 * 
	 * @param c
	 *            The index of the component from which to get the data.
	 * 
	 * @return The requested DataBlk
	 * 
	 * @see #getCompData
	 */
	@Override
	public final DataBlk getInternCompData(DataBlk blk, final int c)
	{
		// Check that block is inside tile
		if (0 > blk.ulx || 0 > blk.uly || blk.w > this.compW[c] || blk.h > this.compH[c])
		{
			throw new IllegalArgumentException("Block is outside the tile");
		}
		// Translate to the sources coordinates
		final int incx = (int) Math.ceil(this.x0siz / (double) this.src.getCompSubsX(c));
		final int incy = (int) Math.ceil(this.y0siz / (double) this.src.getCompSubsY(c));
		blk.ulx -= incx;
		blk.uly -= incy;
		blk = this.src.getInternCompData(blk, c);
		// Translate back to the tiled coordinates
		blk.ulx += incx;
		blk.uly += incy;
		return blk;
	}

	/**
	 * Returns, in the blk argument, a block of image data containing the
	 * specifed rectangular area, in the specified component. The data is
	 * returned, as a copy of the internal data, therefore the returned data can
	 * be modified "in place".
	 * 
	 * <p>
	 * The rectangular area to return is specified by the 'ulx', 'uly', 'w' and
	 * 'h' members of the 'blk' argument, relative to the current tile. These
	 * members are not modified by this method. The 'offset' of the returned
	 * data is 0, and the 'scanw' is the same as the block's width. See the
	 * 'DataBlk' class.
	 * 
	 * <p>
	 * This method, in general, is less efficient than the 'getInternCompData()'
	 * method since, in general, it copies the data. However if the array of
	 * returned data is to be modified by the caller then this method is
	 * preferable.
	 * 
	 * <p>
	 * If the data array in 'blk' is 'null', then a new one is created. If the
	 * data array is not 'null' then it is reused, and it must be large enough
	 * to contain the block's data. Otherwise an 'ArrayStoreException' or an
	 * 'IndexOutOfBoundsException' is thrown by the Java system.
	 * 
	 * <p>
	 * The returned data may have its 'progressive' attribute set. In this case
	 * the returned data is only an approximation of the "final" data.
	 * 
	 * @param blk
	 *            Its coordinates and dimensions specify the area to return,
	 *            relative to the current tile. If it contains a non-null data
	 *            array, then it must be large enough. If it contains a null
	 *            data array a new one is created. Some fields in this object
	 *            are modified to return the data.
	 * 
	 * @param c
	 *            The index of the component from which to get the data.
	 * 
	 * @return The requested DataBlk
	 * 
	 * @see #getInternCompData
	 */
	@Override
	public final DataBlk getCompData(DataBlk blk, final int c)
	{
		// Check that block is inside tile
		if (0 > blk.ulx || 0 > blk.uly || blk.w > this.compW[c] || blk.h > this.compH[c])
		{
			throw new IllegalArgumentException("Block is outside the tile");
		}
		// Translate to the source's coordinates
		final int incx = (int) Math.ceil(this.x0siz / (double) this.src.getCompSubsX(c));
		final int incy = (int) Math.ceil(this.y0siz / (double) this.src.getCompSubsY(c));
		blk.ulx -= incx;
		blk.uly -= incy;
		blk = this.src.getCompData(blk, c);
		// Translate back to the tiled coordinates
		blk.ulx += incx;
		blk.uly += incy;
		return blk;
	}

	/**
	 * Changes the current tile, given the new tile indexes. An
	 * IllegalArgumentException is thrown if the coordinates do not correspond
	 * to a valid tile.
	 * 
	 * @param x
	 *            The horizontal index of the tile.
	 * 
	 * @param y
	 *            The vertical index of the new tile.
	 * 
	 * @return The new tile index
	 */
	@Override
	public final int setTile(final int x, final int y)
	{
		// Check tile indexes
		if (0 > x || 0 > y || x >= this.ntX || y >= this.ntY)
		{
			throw new IllegalArgumentException("Tile's indexes out of bounds");
		}

		// Set new current tile
		this.tx = x;
		this.ty = y;
		// Calculate tile origins
		final int tx0 = (0 != x) ? this.xt0siz + x * this.xtsiz : this.x0siz;
		final int ty0 = (0 != y) ? this.yt0siz + y * this.ytsiz : this.y0siz;
		final int tx1 = (x != this.ntX - 1) ? (this.xt0siz + (x + 1) * this.xtsiz) : (this.x0siz + this.src.getImgWidth());
		final int ty1 = (y != this.ntY - 1) ? (this.yt0siz + (y + 1) * this.ytsiz) : (this.y0siz + this.src.getImgHeight());
		// Set general variables
		this.tileW = tx1 - tx0;
		this.tileH = ty1 - ty0;
		// Set component specific variables
		final int nc = this.src.getNumComps();
		if (null == compW)
			this.compW = new int[nc];
		if (null == compH)
			this.compH = new int[nc];
		if (null == tcx0)
			this.tcx0 = new int[nc];
		if (null == tcy0)
			this.tcy0 = new int[nc];
		for (int i = 0; i < nc; i++)
		{
			this.tcx0[i] = (int) Math.ceil(tx0 / (double) this.src.getCompSubsX(i));
			this.tcy0[i] = (int) Math.ceil(ty0 / (double) this.src.getCompSubsY(i));
			this.compW[i] = (int) Math.ceil(tx1 / (double) this.src.getCompSubsX(i)) - this.tcx0[i];
			this.compH[i] = (int) Math.ceil(ty1 / (double) this.src.getCompSubsY(i)) - this.tcy0[i];
		}
		return this.getTileIdx();
	}

	/**
	 * Advances to the next tile, in standard scan-line order (by rows then
	 * columns). An NoNextElementException is thrown if the current tile is the
	 * last one (i.e. there is no next tile).
	 * 
	 * @return The new tile index
	 */
	@Override
	public final int nextTile()
	{
		int tIdx = 0;
		if (this.tx == this.ntX - 1 && this.ty == this.ntY - 1)
		{ // Already at last tile
			throw new NoNextElementException();
		}
		else if (this.tx < this.ntX - 1)
		{ // If not at end of current tile line
			tIdx = this.setTile(this.tx + 1, this.ty);
		}
		else
		{ // First tile at next line
			tIdx = this.setTile(0, this.ty + 1);
		}
		return tIdx;
	}

	/**
	 * Returns the horizontal and vertical indexes of the current tile.
	 * 
	 * @param co
	 *            If not null this object is used to return the information. If
	 *            null a new one is created and returned.
	 * 
	 * @return The current tile's horizontal and vertical indexes..
	 */
	@Override
	public final Coord getTile(final Coord co)
	{
		if (null != co)
		{
			co.x = this.tx;
			co.y = this.ty;
			return co;
		}
		return new Coord(this.tx, this.ty);
	}

	/**
	 * Returns the index of the current tile, relative to a standard scan-line
	 * order.
	 * 
	 * @return The current tile's index (starts at 0).
	 */
	@Override
	public final int getTileIdx()
	{
		return this.ty * this.ntX + this.tx;
	}

	/**
	 * Returns the horizontal coordinate of the upper-left corner of the
	 * specified component in the current tile.
	 * 
	 * @param c
	 *            The component index.
	 */
	@Override
	public final int getCompULX(final int c)
	{
		return this.tcx0[c];
	}

	/**
	 * Returns the vertical coordinate of the upper-left corner of the specified
	 * component in the current tile.
	 * 
	 * @param c
	 *            The component index.
	 */
	@Override
	public final int getCompULY(final int c)
	{
		return this.tcy0[c];
	}

	/** Returns the horizontal tile partition offset in the reference grid */
	@Override
	public int getTilePartULX()
	{
		return this.xt0siz;
	}

	/** Returns the vertical tile partition offset in the reference grid */
	@Override
	public int getTilePartULY()
	{
		return this.yt0siz;
	}

	/**
	 * Returns the horizontal coordinate of the image origin, the top-left
	 * corner, in the canvas system, on the reference grid.
	 * 
	 * @return The horizontal coordinate of the image origin in the canvas
	 *         system, on the reference grid.
	 */
	@Override
	public final int getImgULX()
	{
		return this.x0siz;
	}

	/**
	 * Returns the vertical coordinate of the image origin, the top-left corner,
	 * in the canvas system, on the reference grid.
	 * 
	 * @return The vertical coordinate of the image origin in the canvas system,
	 *         on the reference grid.
	 */
	@Override
	public final int getImgULY()
	{
		return this.y0siz;
	}

	/**
	 * Returns the number of tiles in the horizontal and vertical directions.
	 * 
	 * @param co
	 *            If not null this object is used to return the information. If
	 *            null a new one is created and returned.
	 * 
	 * @return The number of tiles in the horizontal (Coord.x) and vertical
	 *         (Coord.y) directions.
	 */
	@Override
	public final Coord getNumTiles(final Coord co)
	{
		if (null != co)
		{
			co.x = this.ntX;
			co.y = this.ntY;
			return co;
		}
		return new Coord(this.ntX, this.ntY);
	}

	/**
	 * Returns the total number of tiles in the image.
	 * 
	 * @return The total number of tiles in the image.
	 */
	@Override
	public final int getNumTiles()
	{
		return this.ntX * this.ntY;
	}

	/**
	 * Returns the nominal width of the tiles in the reference grid.
	 * 
	 * @return The nominal tile width, in the reference grid.
	 */
	@Override
	public final int getNomTileWidth()
	{
		return this.xtsiz;
	}

	/**
	 * Returns the nominal width of the tiles in the reference grid.
	 * 
	 * @return The nominal tile width, in the reference grid.
	 */
	@Override
	public final int getNomTileHeight()
	{
		return this.ytsiz;
	}

	/**
	 * Returns the tiling origin, referred to as '(xt0siz,yt0siz)' in the
	 * codestream header (SIZ marker segment).
	 * 
	 * @param co
	 *            If not null this object is used to return the information. If
	 *            null a new one is created and returned.
	 * 
	 * @return The coordinate of the tiling origin, in the canvas system, on the
	 *         reference grid.
	 * 
	 * @see ImgData
	 */
	public final Coord getTilingOrigin(final Coord co)
	{
		if (null != co)
		{
			co.x = this.xt0siz;
			co.y = this.yt0siz;
			return co;
		}
		return new Coord(this.xt0siz, this.yt0siz);
	}

	/**
	 * Returns a String object representing Tiler's informations
	 * 
	 * @return Tiler's infos in a string
	 */
	@Override
	public String toString()
	{
		return "Tiler: source= " + this.src + "\n" + this.getNumTiles() + " tile(s), nominal width=" + this.xtsiz
				+ ", nominal height=" + this.ytsiz;
	}
}
