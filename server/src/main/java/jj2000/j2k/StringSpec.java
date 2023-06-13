/*
 * CVS identifier:
 *
 * $Id: StringSpec.java,v 1.17 2000/11/30 13:14:07 grosbois Exp $
 *
 * Class:                   StringSpec
 *
 * Description:             String specification for an option
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
 * This class extends ModuleSpec class in order to hold tile-component
 * specifications using Strings.
 *
 * @see ModuleSpec
 */
public class StringSpec extends ModuleSpec {
	/**
	 * Constructs an empty 'StringSpec' with specified number of tile and
	 * components. This constructor is called by the decoder.
	 *
	 * @param nt   Number of tiles
	 * @param nc   Number of components
	 * @param type the type of the specification module i.e. tile specific,
	 *             component specific or both.
	 */
	public StringSpec(final int nt, final int nc, final byte type) {
		super(nt, nc, type);
	}

	/**
	 * Constructs a new 'StringSpec' for the specified number of
	 * components:tiles and the arguments of <tt>optName</tt> option. This
	 * constructor is called by the encoder. It also checks that the arguments
	 * belongs to the recognized arguments list.
	 *
	 * <p>
	 * <u>Note:</u> The arguments must not start with 't' or 'c' since it is
	 * reserved for respectively tile and components indexes specification.
	 *
	 * @param nt   The number of tiles
	 * @param nc   The number of components
	 * @param type the type of the specification module i.e. tile specific,
	 *             component specific or both.
	 * @param optName of the option using boolean spec.
	 * @param list The list of all recognized argument in a String array
	 * @param pl   The ParameterList
	 */
	public StringSpec(final int nt, final int nc, final byte type, final String optName, final String[] list, final ParameterList pl) {
		super(nt, nc, type);

		String param = pl.getParameter(optName);
		boolean recognized = false;

		if (null == param) {
			param = pl.getDefaultParameterList().getParameter(optName);
			for (int i = list.length - 1; 0 <= i; i--)
				if (param.equalsIgnoreCase(list[i])) {
					recognized = true;
					break;
				}
			if (!recognized)
				throw new IllegalArgumentException("Default parameter of option -" + optName + " not" + " recognized: "
						+ param);
			this.setDefault(param);
			return;
		}

		// Parse argument
		final StringTokenizer stk = new StringTokenizer(param);
		String word; // current word
		byte curSpecType = ModuleSpec.SPEC_DEF; // Specification type of the
		// current parameter
		boolean[] tileSpec = null; // Tiles concerned by the
		// specification
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
					} else
						curSpecType = ModuleSpec.SPEC_COMP_DEF;
					break;
				default:
					recognized = false;

					for (int i = list.length - 1; 0 <= i; i--)
						if (word.equalsIgnoreCase(list[i])) {
							recognized = true;
							break;
						}
					if (!recognized)
						throw new IllegalArgumentException("Default parameter of option -" + optName + " not"
								+ " recognized: " + word);

					if (SPEC_DEF == curSpecType) {
						this.setDefault(word);
					} else if (SPEC_TILE_DEF == curSpecType) {
						for (int i = tileSpec.length - 1; 0 <= i; i--)
							if (tileSpec[i]) {
								this.setTileDef(i, word);
							}
					} else if (SPEC_COMP_DEF == curSpecType) {
						for (int i = compSpec.length - 1; 0 <= i; i--)
							if (compSpec[i]) {
								this.setCompDef(i, word);
							}
					} else {
						for (int i = tileSpec.length - 1; 0 <= i; i--) {
							for (int j = compSpec.length - 1; 0 <= j; j--) {
								if (tileSpec[i] && compSpec[j]) {
									this.setTileCompVal(i, j, word);
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
				for (int i = list.length - 1; 0 <= i; i--)
					if (param.equalsIgnoreCase(list[i])) {
						recognized = true;
						break;
					}
				if (!recognized)
					throw new IllegalArgumentException("Default parameter of option -" + optName + " not"
							+ " recognized: " + param);
				this.setDefault(param);
			} else {
				// All tile-component have been specified, takes the first
				// tile-component value as default.
				this.setDefault(this.getSpec(0, 0));
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
}
