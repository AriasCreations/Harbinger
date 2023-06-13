/*
 * CVS identifier:
 *
 * $Id: IntegerSpec.java,v 1.14 2001/09/20 12:31:08 grosbois Exp $
 *
 * Class:                   IntegerSpec
 *
 * Description:             Holds specs corresponding to an Integer
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
package jj2000.j2k;

import jj2000.j2k.util.ParameterList;

import java.security.InvalidParameterException;
import java.util.StringTokenizer;

/**
 * This class extends ModuleSpec and is responsible of Integer specifications
 * for each tile-component.
 *
 * @see ModuleSpec
 */
public class IntegerSpec extends ModuleSpec {

	/**
	 * The largest value of type int
	 */
	protected static int MAX_INT = Integer.MAX_VALUE;

	/**
	 * Constructs a new 'IntegerSpec' for the specified number of tiles and
	 * components and with allowed type of specifications. This constructor is
	 * normally called at decoder side.
	 *
	 * @param nt   The number of tiles
	 * @param nc   The number of components
	 * @param type The type of allowed specifications
	 */
	public IntegerSpec(final int nt, final int nc, final byte type) {
		super(nt, nc, type);
	}

	/**
	 * Constructs a new 'IntegerSpec' for the specified number of tiles and
	 * components, the allowed specifications type and the ParameterList
	 * instance. This constructor is normally called at encoder side and parse
	 * arguments of specified option.
	 *
	 * @param nt      The number of tiles
	 * @param nc      The number of components
	 * @param type    The allowed specifications type
	 * @param pl      The ParameterList instance
	 * @param optName The name of the option to process
	 */
	public IntegerSpec(final int nt, final int nc, final byte type, final ParameterList pl, final String optName) {
		super(nt, nc, type);

		Integer value;
		String param = pl.getParameter(optName);

		if (null == param) { // No parameter specified
			param = pl.getDefaultParameterList().getParameter(optName);
			try {
				this.setDefault(Integer.valueOf(param));
			} catch (final NumberFormatException e) {
				throw new IllegalArgumentException("Non recognized value for option -" + optName + ": " + param);
			}
			return;
		}

		// Parse argument
		final StringTokenizer stk = new StringTokenizer(param);
		String word; // current word
		byte curSpecType = ModuleSpec.SPEC_DEF; // Specification type of the
		// current parameter
		boolean[] tileSpec = null; // Tiles concerned by the specification
		boolean[] compSpec = null; // Components concerned by the specification

		while (stk.hasMoreTokens()) {
			word = stk.nextToken();

			switch (word.charAt(0)) {
				case 't': // Tiles specification
					tileSpec = ModuleSpec.parseIdx(word, this.nTiles);
					if (SPEC_COMP_DEF == curSpecType) {
						curSpecType = ModuleSpec.SPEC_TILE_COMP;
					} else {
						curSpecType = ModuleSpec.SPEC_TILE_DEF;
					}
					break;
				case 'c': // Components specification
					compSpec = ModuleSpec.parseIdx(word, this.nComp);
					if (SPEC_TILE_DEF == curSpecType) {
						curSpecType = ModuleSpec.SPEC_TILE_COMP;
					} else {
						curSpecType = ModuleSpec.SPEC_COMP_DEF;
					}
					break;
				default:
					try {
						value = Integer.valueOf(word);
					} catch (final NumberFormatException e) {
						throw new IllegalArgumentException("Non recognized value for option -" + optName + ": " + word);
					}

					if (SPEC_DEF == curSpecType) {
						this.setDefault(value);
					} else if (SPEC_TILE_DEF == curSpecType) {
						for (int i = tileSpec.length - 1; 0 <= i; i--)
							if (tileSpec[i]) {
								this.setTileDef(i, value);
							}
					} else if (SPEC_COMP_DEF == curSpecType) {
						for (int i = compSpec.length - 1; 0 <= i; i--)
							if (compSpec[i]) {
								this.setCompDef(i, value);
							}
					} else {
						for (int i = tileSpec.length - 1; 0 <= i; i--) {
							for (int j = compSpec.length - 1; 0 <= j; j--) {
								if (tileSpec[i] && compSpec[j]) {
									this.setTileCompVal(i, j, value);
								}
							}
						}
					}

					// Re-initialize
					curSpecType = ModuleSpec.SPEC_DEF;
					tileSpec = null;
					compSpec = null;
					break;
			}
		}

		// Check that default value has been specified
		if (null == getDefault()) {
			int ndefspec = 0;
			for (int t = nt - 1; 0 <= t; t--) {
				for (int c = nc - 1; 0 <= c; c--) {
					if (SPEC_DEF == specValType[t][c]) {
						ndefspec++;
					}
				}
			}

			// If some tile-component have received no specification, it takes
			// the default value defined in ParameterList
			if (0 != ndefspec) {
				param = pl.getDefaultParameterList().getParameter(optName);
				try {
					this.setDefault(Integer.valueOf(param));
				} catch (final NumberFormatException e) {
					throw new IllegalArgumentException("Non recognized value for option -" + optName + ": " + param);
				}
			} else {
				// All tile-component have been specified, takes the first
				// tile-component value as default.
				this.setDefault(this.getTileCompVal(0, 0));
				switch (this.specValType[0][0]) {
					case ModuleSpec.SPEC_TILE_DEF:
						for (int c = nc - 1; 0 <= c; c--) {
							if (SPEC_TILE_DEF == specValType[0][c])
								this.specValType[0][c] = ModuleSpec.SPEC_DEF;
						}
						this.tileDef[0] = null;
						break;
					case ModuleSpec.SPEC_COMP_DEF:
						for (int t = nt - 1; 0 <= t; t--) {
							if (SPEC_COMP_DEF == specValType[t][0])
								this.specValType[t][0] = ModuleSpec.SPEC_DEF;
						}
						this.compDef[0] = null;
						break;
					case ModuleSpec.SPEC_TILE_COMP:
						this.specValType[0][0] = ModuleSpec.SPEC_DEF;
						this.tileCompVal.put("t0c0", null);
						break;
					default:
						throw new InvalidParameterException("Invalid spec Type: " + this.specValType[0][0]);
				}
			}
		}

	}

	/**
	 * Gets the maximum value of all tile-components.
	 *
	 * @return The maximum value
	 */
	public int getMax() {
		int max = ((Integer) this.def).intValue();
		int tmp;

		for (int t = 0; t < this.nTiles; t++) {
			for (int c = 0; c < this.nComp; c++) {
				tmp = ((Integer) this.getSpec(t, c)).intValue();
				if (max < tmp)
					max = tmp;
			}
		}

		return max;
	}

	/**
	 * Get the minimum value of all tile-components.
	 *
	 * @return The minimum value
	 */
	public int getMin() {
		int min = ((Integer) this.def).intValue();
		int tmp;

		for (int t = 0; t < this.nTiles; t++) {
			for (int c = 0; c < this.nComp; c++) {
				tmp = ((Integer) this.getSpec(t, c)).intValue();
				if (min > tmp)
					min = tmp;
			}
		}

		return min;
	}

	/**
	 * Gets the maximum value of each tile for specified component
	 *
	 * @param c The component index
	 * @return The maximum value
	 */
	public int getMaxInComp(final int c) {
		int max = 0;
		int tmp;

		for (int t = 0; t < this.nTiles; t++) {
			tmp = ((Integer) this.getSpec(t, c)).intValue();
			if (max < tmp)
				max = tmp;
		}

		return max;
	}

	/**
	 * Gets the minimum value of all tiles for the specified component.
	 *
	 * @param c The component index
	 * @return The minimum value
	 */
	public int getMinInComp(final int c) {
		int min = IntegerSpec.MAX_INT; // Big value
		int tmp;

		for (int t = 0; t < this.nTiles; t++) {
			tmp = ((Integer) this.getSpec(t, c)).intValue();
			if (min > tmp)
				min = tmp;
		}

		return min;
	}

	/**
	 * Gets the maximum value of all components in the specified tile.
	 *
	 * @param t The tile index
	 * @return The maximum value
	 */
	public int getMaxInTile(final int t) {
		int max = 0;
		int tmp;

		for (int c = 0; c < this.nComp; c++) {
			tmp = ((Integer) this.getSpec(t, c)).intValue();
			if (max < tmp)
				max = tmp;
		}

		return max;
	}

	/**
	 * Gets the minimum value of each component in specified tile
	 *
	 * @param t The tile index
	 * @return The minimum value
	 */
	public int getMinInTile(final int t) {
		int min = IntegerSpec.MAX_INT; // Big value
		int tmp;

		for (int c = 0; c < this.nComp; c++) {
			tmp = ((Integer) this.getSpec(t, c)).intValue();
			if (min > tmp)
				min = tmp;
		}

		return min;
	}
}
