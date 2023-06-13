/* 
 * CVS identifier:
 * 
 * $Id: WTDecompSpec.java,v 1.9 2000/09/05 09:26:06 grosbois Exp $
 * 
 * Class:                   WTDecompSpec
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
 * 
 * 
 * 
 */

package dev.zontreck.harbinger.thirdparty.jj2000.j2k.wavelet;

import dev.zontreck.harbinger.thirdparty.jj2000.j2k.*;

/**
 * This class holds the decomposition type to be used in each part of the image;
 * the default one, the component specific ones, the tile default ones and the
 * component-tile specific ones.
 * 
 * <P>
 * The decomposition type identifiers values are the same as in the codestream.
 * 
 * <P>
 * The hierarchy is:<br>
 * - Tile and component specific decomposition<br>
 * - Tile specific default decomposition<br>
 * - Component main default decomposition<br>
 * - Main default decomposition<br>
 * 
 * <P>
 * At the moment tiles are not supported by this class.
 */
public class WTDecompSpec
{
	/**
	 * ID for the dyadic wavelet tree decomposition (also called "Mallat" in
	 * JPEG 2000): 0x00.
	 */
	public static final int WT_DECOMP_DYADIC = 0;

	/**
	 * ID for the SPACL (as defined in JPEG 2000) wavelet tree decomposition (1
	 * level of decomposition in the high bands and some specified number for
	 * the lowest LL band): 0x02.
	 */
	public static final int WT_DECOMP_SPACL = 2;

	/**
	 * ID for the PACKET (as defined in JPEG 2000) wavelet tree decomposition (2
	 * levels of decomposition in the high bands and some specified number for
	 * the lowest LL band): 0x01.
	 */
	public static final int WT_DECOMP_PACKET = 1;

	/** The identifier for "main default" specified decomposition */
	public static final byte DEC_SPEC_MAIN_DEF = 0;

	/** The identifier for "component default" specified decomposition */
	public static final byte DEC_SPEC_COMP_DEF = 1;

	/** The identifier for "tile specific default" specified decomposition */
	public static final byte DEC_SPEC_TILE_DEF = 2;

	/**
	 * The identifier for "tile and component specific" specified decomposition
	 */
	public static final byte DEC_SPEC_TILE_COMP = 3;

	/**
	 * The spec type for each tile and component. The first index is the
	 * component index, the second is the tile index. NOTE: The tile specific
	 * things are not supported yet.
	 */
	// Use byte to save memory (no need for speed here).
	private final byte[] specValType;

	/** The main default decomposition */
	private final int mainDefDecompType;

	/** The main default number of decomposition levels */
	private final int mainDefLevels;

	/** The component main default decomposition, for each component. */
	private int[] compMainDefDecompType;

	/** The component main default decomposition levels, for each component */
	private int[] compMainDefLevels;

	/**
	 * Constructs a new 'WTDecompSpec' for the specified number of components
	 * and tiles, with the given main default decomposition type and number of
	 * levels.
	 * 
	 * <P>
	 * NOTE: The tile specific things are not supported yet
	 * 
	 * @param nc
	 *            The number of components
	 * 
	 * @param nt
	 *            The number of tiles
	 * 
	 * @param dec
	 *            The main default decomposition type
	 * 
	 * @param lev
	 *            The main default number of decomposition levels
	 * 
	 * 
	 */
	public WTDecompSpec(final int nc, final int dec, final int lev)
	{
		this.mainDefDecompType = dec;
		this.mainDefLevels = lev;
		this.specValType = new byte[nc];
	}

	/**
	 * Sets the "component main default" decomposition type and number of levels
	 * for the specified component. Both 'dec' and 'lev' can not be negative at
	 * the same time.
	 * 
	 * @param n
	 *            The component index
	 * 
	 * @param dec
	 *            The decomposition type. If negative then the main default is
	 *            used.
	 * 
	 * @param lev
	 *            The number of levels. If negative then the main defaul is
	 *            used.
	 * 
	 * 
	 */
	public void setMainCompDefDecompType(final int n, final int dec, final int lev)
	{
		if (0 > dec && 0 > lev)
		{
			throw new IllegalArgumentException();
		}
		// Set spec type and decomp
		this.specValType[n] = WTDecompSpec.DEC_SPEC_COMP_DEF;
		if (null == compMainDefDecompType)
		{
			this.compMainDefDecompType = new int[this.specValType.length];
			this.compMainDefLevels = new int[this.specValType.length];
		}
		this.compMainDefDecompType[n] = (0 <= dec) ? dec : this.mainDefDecompType;
		this.compMainDefLevels[n] = (0 <= lev) ? lev : this.mainDefLevels;
		// For the moment disable it since other parts of JJ2000 do not
		// support this
		throw new NotImplementedError("Currently, in JJ2000, all components and tiles must have the same "
				+ "decomposition type and number of levels");
	}

	/**
	 * Returns the type of specification for the decomposition in the specified
	 * component and tile. The specification type is one of:
	 * 'DEC_SPEC_MAIN_DEF', 'DEC_SPEC_COMP_DEF', 'DEC_SPEC_TILE_DEF',
	 * 'DEC_SPEC_TILE_COMP'.
	 * 
	 * <P>
	 * NOTE: The tile specific things are not supported yet
	 * 
	 * @param n
	 *            The component index
	 * 
	 * @param t
	 *            The tile index, in raster scan order.
	 * 
	 * @return The specification type for component 'n' and tile 't'.
	 * 
	 * 
	 */
	public byte getDecSpecType(final int n)
	{
		return this.specValType[n];
	}

	/**
	 * Returns the main default decomposition type.
	 * 
	 * @return The main default decomposition type.
	 * 
	 * 
	 */
	public int getMainDefDecompType()
	{
		return this.mainDefDecompType;
	}

	/**
	 * Returns the main default decomposition number of levels.
	 * 
	 * @return The main default decomposition number of levels.
	 * 
	 * 
	 */
	public int getMainDefLevels()
	{
		return this.mainDefLevels;
	}

	/**
	 * Returns the decomposition type to be used in component 'n' and tile 't'.
	 * 
	 * <P>
	 * NOTE: The tile specific things are not supported yet
	 * 
	 * @param n
	 *            The component index.
	 * 
	 * @param t
	 *            The tile index, in raster scan order
	 * 
	 * @return The decomposition type to be used.
	 * 
	 * 
	 */
	public int getDecompType(final int n)
	{
		switch (this.specValType[n])
		{
			case WTDecompSpec.DEC_SPEC_MAIN_DEF:
				return this.mainDefDecompType;
			case WTDecompSpec.DEC_SPEC_COMP_DEF:
				return this.compMainDefDecompType[n];
			case WTDecompSpec.DEC_SPEC_TILE_DEF:
				throw new NotImplementedError();
			case WTDecompSpec.DEC_SPEC_TILE_COMP:
				throw new NotImplementedError();
			default:
				throw new Error("Internal JJ2000 error");
		}
	}

	/**
	 * Returns the decomposition number of levels in component 'n' and tile 't'.
	 * 
	 * <P>
	 * NOTE: The tile specific things are not supported yet
	 * 
	 * @param n
	 *            The component index.
	 * 
	 * @param t
	 *            The tile index, in raster scan order
	 * 
	 * @return The decomposition number of levels.
	 * 
	 * 
	 */
	public int getLevels(final int n)
	{
		switch (this.specValType[n])
		{
			case WTDecompSpec.DEC_SPEC_MAIN_DEF:
				return this.mainDefLevels;
			case WTDecompSpec.DEC_SPEC_COMP_DEF:
				return this.compMainDefLevels[n];
			case WTDecompSpec.DEC_SPEC_TILE_DEF:
				throw new NotImplementedError();
			case WTDecompSpec.DEC_SPEC_TILE_COMP:
				throw new NotImplementedError();
			default:
				throw new Error("Internal JJ2000 error");
		}
	}
}
