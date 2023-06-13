/*
 * CVS identifier:
 *
 * $Id: CBlkSizeSpec.java,v 1.11 2001/02/14 10:38:18 grosbois Exp $
 *
 * Class:                   CBlkSizeSpec
 *
 * Description:             Specification of the code-blocks size
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
package jj2000.j2k.entropy;

import jj2000.j2k.util.*;
import jj2000.j2k.*;

import java.util.*;

/**
 * This class extends ModuleSpec class for code-blocks sizes holding purposes.
 * 
 * <P>
 * It stores the size a of code-block.
 */
public class CBlkSizeSpec extends ModuleSpec
{
	/** Name of the option */
	private static final String optName = "Cblksiz";

	/** The maximum code-block width */
	private int maxCBlkWidth;

	/** The maximum code-block height */
	private int maxCBlkHeight;

	/**
	 * Creates a new CBlkSizeSpec object for the specified number of tiles and
	 * components.
	 * 
	 * @param nt
	 *            The number of tiles
	 * 
	 * @param nc
	 *            The number of components
	 * 
	 * @param type
	 *            the type of the specification module i.e. tile specific,
	 *            component specific or both.
	 */
	public CBlkSizeSpec(final int nt, final int nc, final byte type)
	{
		super(nt, nc, type);
	}

	/**
	 * Creates a new CBlkSizeSpec object for the specified number of tiles and
	 * components and the ParameterList instance.
	 * 
	 * @param nt
	 *            The number of tiles
	 * 
	 * @param nc
	 *            The number of components
	 * 
	 * @param type
	 *            the type of the specification module i.e. tile specific,
	 *            component specific or both.
	 *
	 * 
	 * @param pl
	 *            The ParameterList instance
	 */
	public CBlkSizeSpec(final int nt, final int nc, final byte type, final ParameterList pl)
	{
		super(nt, nc, type);

		boolean firstVal = true;
		final String param = pl.getParameter(CBlkSizeSpec.optName);

		// Precinct partition is used : parse arguments
		final StringTokenizer stk = new StringTokenizer(param);
		byte curSpecType = ModuleSpec.SPEC_DEF; // Specification type of the
										// current parameter
		boolean[] tileSpec = null; // Tiles concerned by the specification
		boolean[] compSpec = null; // Components concerned by the specification
		int ci, ti;
		String word = null; // current word
		String errMsg = null;

		while (stk.hasMoreTokens())
		{
			word = stk.nextToken();

			switch (word.charAt(0))
			{

				case 't': // Tiles specification
					tileSpec = ModuleSpec.parseIdx(word, this.nTiles);
					if (SPEC_COMP_DEF == curSpecType)
					{
						curSpecType = ModuleSpec.SPEC_TILE_COMP;
					}
					else
					{
						curSpecType = ModuleSpec.SPEC_TILE_DEF;
					}
					break;

				case 'c': // Components specification
					compSpec = ModuleSpec.parseIdx(word, this.nComp);
					if (SPEC_TILE_DEF == curSpecType)
					{
						curSpecType = ModuleSpec.SPEC_TILE_COMP;
					}
					else
					{
						curSpecType = ModuleSpec.SPEC_COMP_DEF;
					}
					break;

				default:
					if (!Character.isDigit(word.charAt(0)))
					{
						errMsg = "Bad construction for parameter: " + word;
						throw new IllegalArgumentException(errMsg);
					}
					final Integer[] dim = new Integer[2];
					// Get code-block's width
					try
					{
						dim[0] = Integer.valueOf(word);
						// Check that width is not >
						// StdEntropyCoderOptions.MAX_CB_DIM
						if (StdEntropyCoderOptions.MAX_CB_DIM < dim[0].intValue())
						{
							errMsg = "'" + CBlkSizeSpec.optName + "' option : the code-block's width cannot be greater than "
									+ StdEntropyCoderOptions.MAX_CB_DIM;
							throw new IllegalArgumentException(errMsg);
						}
						// Check that width is not <
						// StdEntropyCoderOptions.MIN_CB_DIM
						if (StdEntropyCoderOptions.MIN_CB_DIM > dim[0].intValue())
						{
							errMsg = "'" + CBlkSizeSpec.optName + "' option : the code-block's width cannot be less than "
									+ StdEntropyCoderOptions.MIN_CB_DIM;
							throw new IllegalArgumentException(errMsg);
						}
						// Check that width is a power of 2
						if (dim[0].intValue() != (1 << MathUtil.log2(dim[0].intValue())))
						{
							errMsg = "'" + CBlkSizeSpec.optName + "' option : the code-block's width must be a power of 2";
							throw new IllegalArgumentException(errMsg);
						}
					}
					catch (final NumberFormatException e)
					{
						errMsg = "'" + CBlkSizeSpec.optName + "' option : the code-block's width could not be parsed.";
						throw new IllegalArgumentException(errMsg);
					}
					// Get the next word in option
					try
					{
						word = stk.nextToken();
					}
					catch (final NoSuchElementException e)
					{
						errMsg = "'" + CBlkSizeSpec.optName + "' option : could not parse the code-block's height";
						throw new IllegalArgumentException(errMsg);

					}
					// Get the code-block's height
					try
					{
						dim[1] = Integer.valueOf(word);
						// Check that height is not >
						// StdEntropyCoderOptions.MAX_CB_DIM
						if (StdEntropyCoderOptions.MAX_CB_DIM < dim[1].intValue())
						{
							errMsg = "'" + CBlkSizeSpec.optName + "' option : the code-block's height cannot be greater than "
									+ StdEntropyCoderOptions.MAX_CB_DIM;
							throw new IllegalArgumentException(errMsg);
						}
						// Check that height is not <
						// StdEntropyCoderOptions.MIN_CB_DIM
						if (StdEntropyCoderOptions.MIN_CB_DIM > dim[1].intValue())
						{
							errMsg = "'" + CBlkSizeSpec.optName + "' option : the code-block's height cannot be less than "
									+ StdEntropyCoderOptions.MIN_CB_DIM;
							throw new IllegalArgumentException(errMsg);
						}
						// Check that height is a power of 2
						if (dim[1].intValue() != (1 << MathUtil.log2(dim[1].intValue())))
						{
							errMsg = "'" + CBlkSizeSpec.optName + "' option : the code-block's height must be a power of 2";
							throw new IllegalArgumentException(errMsg);
						}
						// Check that the code-block 'area' (i.e. width*height)
						// is
						// not greater than StdEntropyCoderOptions.MAX_CB_AREA
						if (StdEntropyCoderOptions.MAX_CB_AREA < dim[0].intValue() * dim[1].intValue())
						{
							errMsg = "'" + CBlkSizeSpec.optName + "' option : The code-block's area (i.e. width*height) "
									+ "cannot be greater than " + StdEntropyCoderOptions.MAX_CB_AREA;
							throw new IllegalArgumentException(errMsg);
						}
					}
					catch (final NumberFormatException e)
					{
						errMsg = "'" + CBlkSizeSpec.optName + "' option : the code-block's height could not be parsed.";
						throw new IllegalArgumentException(errMsg);
					}

					// Store the maximum dimensions if necessary
					if (dim[0].intValue() > this.maxCBlkWidth)
					{
						this.maxCBlkWidth = dim[0].intValue();
					}

					if (dim[1].intValue() > this.maxCBlkHeight)
					{
						this.maxCBlkHeight = dim[1].intValue();
					}

					if (firstVal)
					{
						// This is the first time a value is given so we set it
						// as
						// the default one
						this.setDefault(dim);
						firstVal = false;
					}

					switch (curSpecType)
					{
						case ModuleSpec.SPEC_DEF:
							this.setDefault(dim);
							break;
						case ModuleSpec.SPEC_TILE_DEF:
							for (ti = tileSpec.length - 1; 0 <= ti; ti--)
							{
								if (tileSpec[ti])
								{
									this.setTileDef(ti, dim);
								}
							}
							break;
						case ModuleSpec.SPEC_COMP_DEF:
							for (ci = compSpec.length - 1; 0 <= ci; ci--)
							{
								if (compSpec[ci])
								{
									this.setCompDef(ci, dim);
								}
							}
							break;
						default:
							for (ti = tileSpec.length - 1; 0 <= ti; ti--)
							{
								for (ci = compSpec.length - 1; 0 <= ci; ci--)
								{
									if (tileSpec[ti] && compSpec[ci])
									{
										this.setTileCompVal(ti, ci, dim);
									}
								}
							}
							break;
					}
			} // end switch
		}
	}

	/**
	 * Returns the maximum code-block's width
	 * 
	 */
	public int getMaxCBlkWidth()
	{
		return this.maxCBlkWidth;
	}

	/**
	 * Returns the maximum code-block's height
	 * 
	 */
	public int getMaxCBlkHeight()
	{
		return this.maxCBlkHeight;
	}

	/**
	 * Returns the code-block width :
	 * 
	 * <ul>
	 * <li>for the specified tile/component</li>
	 * <li>for the specified tile</li>
	 * <li>for the specified component</li>
	 * <li>default value</li>
	 * </ul>
	 * 
	 * The value returned depends on the value of the variable 'type' which can
	 * take the following values :<br>
	 * 
	 * <ul>
	 * <li>SPEC_DEF -> Default value is returned. t and c values are ignored</li>
	 * <li>SPEC_COMP_DEF -> Component default value is returned. t value is
	 * ignored</li>
	 * <li>SPEC_TILE_DEF -> Tile default value is returned. c value is ignored</li>
	 * <li>SPEC_TILE_COMP -> Tile/Component value is returned.</li>
	 * </ul>
	 * 
	 * @param type
	 *            The type of the value we want to be returned
	 * 
	 * @param t
	 *            The tile index
	 * 
	 * @param c
	 *            the component index
	 * 
	 * @return The code-block width for the specified tile and component
	 */
	public int getCBlkWidth(final byte type, final int t, final int c)
	{
		Integer[] dim = null;
		switch (type)
		{
			case ModuleSpec.SPEC_DEF:
				dim = (Integer[]) this.getDefault();
				break;
			case ModuleSpec.SPEC_COMP_DEF:
				dim = (Integer[]) this.getCompDef(c);
				break;
			case ModuleSpec.SPEC_TILE_DEF:
				dim = (Integer[]) this.getTileDef(t);
				break;
			case ModuleSpec.SPEC_TILE_COMP:
				dim = (Integer[]) this.getTileCompVal(t, c);
				break;
			default:
				return 0;
		}
		return dim[0].intValue();
	}

	/**
	 * Returns the code-block height:
	 * 
	 * <ul>
	 * <li>for the specified tile/component</li>
	 * <li>for the specified tile</li>
	 * <li>for the specified component</li>
	 * <li>default value</li>
	 * </ul>
	 * 
	 * The value returned depends on the value of the variable 'type' which can
	 * take the following values :
	 * 
	 * <ul>
	 * <li>SPEC_DEF -> Default value is returned. t and c values are ignored</li>
	 * <li>SPEC_COMP_DEF -> Component default value is returned. t value is
	 * ignored</li>
	 * <li>SPEC_TILE_DEF -> Tile default value is returned. c value is ignored</li>
	 * <li>SPEC_TILE_COMP -> Tile/Component value is returned.</li>
	 * </ul>
	 * 
	 * @param type
	 *            The type of the value we want to be returned
	 * 
	 * @param t
	 *            The tile index
	 * 
	 * @param c
	 *            the component index
	 * 
	 * @return The code-block height for the specified tile and component
	 */
	public int getCBlkHeight(final byte type, final int t, final int c)
	{
		Integer[] dim = null;
		switch (type)
		{
			case ModuleSpec.SPEC_DEF:
				dim = (Integer[]) this.getDefault();
				break;
			case ModuleSpec.SPEC_COMP_DEF:
				dim = (Integer[]) this.getCompDef(c);
				break;
			case ModuleSpec.SPEC_TILE_DEF:
				dim = (Integer[]) this.getTileDef(t);
				break;
			case ModuleSpec.SPEC_TILE_COMP:
				dim = (Integer[]) this.getTileCompVal(t, c);
				break;
			default:
				return 0;
		}
		return dim[1].intValue();
	}

	/**
	 * Sets default value for this module
	 * 
	 * @param value
	 *            Default value
	 */
	@Override
	public void setDefault(final Object value)
	{
		super.setDefault(value);

		// Store the biggest code-block dimensions
		this.storeHighestDims((Integer[]) value);
	}

	/**
	 * Sets default value for specified tile and specValType tag if allowed by
	 * its priority.
	 * 
	 * @param c
	 *            Tile index.
	 * 
	 * @param value
	 *            Tile's default value
	 */
	@Override
	public void setTileDef(final int t, final Object value)
	{
		super.setTileDef(t, value);

		// Store the biggest code-block dimensions
		this.storeHighestDims((Integer[]) value);
	}

	/**
	 * Sets default value for specified component and specValType tag if allowed
	 * by its priority.
	 * 
	 * @param c
	 *            Component index
	 * 
	 * @param value
	 *            Component's default value
	 */
	@Override
	public void setCompDef(final int c, final Object value)
	{
		super.setCompDef(c, value);

		// Store the biggest code-block dimensions
		this.storeHighestDims((Integer[]) value);
	}

	/**
	 * Sets value for specified tile-component.
	 * 
	 * @param t
	 *            Tie index
	 * 
	 * @param c
	 *            Component index
	 * 
	 * @param value
	 *            Tile-component's value
	 */
	@Override
	public void setTileCompVal(final int t, final int c, final Object value)
	{
		super.setTileCompVal(t, c, value);

		// Store the biggest code-block dimensions
		this.storeHighestDims((Integer[]) value);
	}

	/**
	 * Stores the highest code-block width and height
	 * 
	 * @param dim
	 *            The 2 elements array that contains the code-block width and
	 *            height.
	 */
	private void storeHighestDims(final Integer[] dim)
	{
		// Store the biggest code-block dimensions
		if (dim[0].intValue() > this.maxCBlkWidth)
		{
			this.maxCBlkWidth = dim[0].intValue();
		}
		if (dim[1].intValue() > this.maxCBlkHeight)
		{
			this.maxCBlkHeight = dim[1].intValue();
		}
	}
}
