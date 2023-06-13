/*
 * CVS identifier:
 *
 * $Id: AnWTFilterSpec.java,v 1.27 2001/05/08 16:11:37 grosbois Exp $
 *
 * Class:                   AnWTFilterSpec
 *
 * Description:             Analysis filters specification
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
package jj2000.j2k.wavelet.analysis;

import jj2000.j2k.ModuleSpec;
import jj2000.j2k.quantization.QuantTypeSpec;
import jj2000.j2k.util.ParameterList;

import java.util.StringTokenizer;

/**
 * This class extends ModuleSpec class for analysis filters specification
 * holding purpose.
 *
 * @see ModuleSpec
 */
public class AnWTFilterSpec extends ModuleSpec {
	/**
	 * The reversible default filter
	 */
	private static final String REV_FILTER_STR = "w5x3";

	/**
	 * The non-reversible default filter
	 */
	private static final String NON_REV_FILTER_STR = "w9x7";

	/**
	 * Constructs a new 'AnWTFilterSpec' for the specified number of components
	 * and tiles.
	 *
	 * @param nt   The number of tiles
	 * @param nc   The number of components
	 * @param type the type of the specification module i.e. tile specific,
	 *             component specific or both.
	 * @param qts  Quantization specifications
	 * @param pl   The ParameterList
	 */
	public AnWTFilterSpec(final int nt, final int nc, final byte type, final QuantTypeSpec qts, final ParameterList pl) {
		super(nt, nc, type);

		// Check parameters
		pl.checkList(AnWTFilter.OPT_PREFIX, ParameterList.toNameArray(AnWTFilter.getParameterInfo()));

		final String param = pl.getParameter("Ffilters");
		boolean isFilterSpecified = true;

		// No parameter specified
		if (null == param) {
			isFilterSpecified = false;

			// If lossless compression, uses the reversible filters in each
			// tile-components
			if (pl.getBooleanParameter("lossless")) {
				this.setDefault(this.parseFilters(AnWTFilterSpec.REV_FILTER_STR));
				return;
			}

			// If no filter is specified through the command-line, use
			// REV_FILTER_STR or NON_REV_FILTER_STR according to the
			// quantization type
			for (int t = nt - 1; 0 <= t; t--) {
				for (int c = nc - 1; 0 <= c; c--) {
					switch (qts.getSpecValType(t, c)) {
						case ModuleSpec.SPEC_DEF:
							if (null == getDefault()) {
								if (pl.getBooleanParameter("lossless"))
									this.setDefault(this.parseFilters(AnWTFilterSpec.REV_FILTER_STR));
								if (qts.getDefault().equals("reversible")) {
									this.setDefault(this.parseFilters(AnWTFilterSpec.REV_FILTER_STR));
								} else {
									this.setDefault(this.parseFilters(AnWTFilterSpec.NON_REV_FILTER_STR));
								}
							}
							this.specValType[t][c] = ModuleSpec.SPEC_DEF;
							break;
						case ModuleSpec.SPEC_COMP_DEF:
							if (!this.isCompSpecified(c)) {
								if (qts.getCompDef(c).equals("reversible")) {
									this.setCompDef(c, this.parseFilters(AnWTFilterSpec.REV_FILTER_STR));
								} else {
									this.setCompDef(c, this.parseFilters(AnWTFilterSpec.NON_REV_FILTER_STR));
								}
							}
							this.specValType[t][c] = ModuleSpec.SPEC_COMP_DEF;
							break;
						case ModuleSpec.SPEC_TILE_DEF:
							if (!this.isTileSpecified(t)) {
								if (qts.getTileDef(t).equals("reversible")) {
									this.setTileDef(t, this.parseFilters(AnWTFilterSpec.REV_FILTER_STR));
								} else {
									this.setTileDef(t, this.parseFilters(AnWTFilterSpec.NON_REV_FILTER_STR));
								}
							}
							this.specValType[t][c] = ModuleSpec.SPEC_TILE_DEF;
							break;
						case ModuleSpec.SPEC_TILE_COMP:
							if (!this.isTileCompSpecified(t, c)) {
								if (qts.getTileCompVal(t, c).equals("reversible")) {
									this.setTileCompVal(t, c, this.parseFilters(AnWTFilterSpec.REV_FILTER_STR));
								} else {
									this.setTileCompVal(t, c, this.parseFilters(AnWTFilterSpec.NON_REV_FILTER_STR));
								}
							}
							this.specValType[t][c] = ModuleSpec.SPEC_TILE_COMP;
							break;
						default:
							throw new IllegalArgumentException("Unsupported specification ype");
					}
				}
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
		AnWTFilter[][] filter;

		while (stk.hasMoreTokens()) {
			word = stk.nextToken();

			switch (word.charAt(0)) {
				case 't': // Tiles specification
				case 'T': // Tiles specification
					tileSpec = ModuleSpec.parseIdx(word, this.nTiles);
					if (SPEC_COMP_DEF == curSpecType)
						curSpecType = ModuleSpec.SPEC_TILE_COMP;
					else
						curSpecType = ModuleSpec.SPEC_TILE_DEF;
					break;
				case 'c': // Components specification
				case 'C': // Components specification
					compSpec = ModuleSpec.parseIdx(word, this.nComp);
					if (SPEC_TILE_DEF == curSpecType)
						curSpecType = ModuleSpec.SPEC_TILE_COMP;
					else
						curSpecType = ModuleSpec.SPEC_COMP_DEF;
					break;
				case 'w': // WT filters specification
				case 'W': // WT filters specification
					if (pl.getBooleanParameter("lossless") && "w9x7".equalsIgnoreCase(word)) {
						throw new IllegalArgumentException("Cannot use non reversible wavelet transform with"
								+ " '-lossless' option");

					}

					filter = this.parseFilters(word);
					if (SPEC_DEF == curSpecType) {
						this.setDefault(filter);
					} else if (SPEC_TILE_DEF == curSpecType) {
						for (int i = tileSpec.length - 1; 0 <= i; i--)
							if (tileSpec[i]) {
								this.setTileDef(i, filter);
							}
					} else if (SPEC_COMP_DEF == curSpecType) {
						for (int i = compSpec.length - 1; 0 <= i; i--)
							if (compSpec[i]) {
								this.setCompDef(i, filter);
							}
					} else {
						for (int i = tileSpec.length - 1; 0 <= i; i--) {
							for (int j = compSpec.length - 1; 0 <= j; j--) {
								if (tileSpec[i] && compSpec[j]) {
									this.setTileCompVal(i, j, filter);
								}
							}
						}
					}

					// Re-initialize
					curSpecType = ModuleSpec.SPEC_DEF;
					tileSpec = null;
					compSpec = null;
					break;

				default:
					throw new IllegalArgumentException("Bad construction for parameter: " + word);
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
				if (qts.getDefault().equals("reversible"))
					this.setDefault(this.parseFilters(AnWTFilterSpec.REV_FILTER_STR));
				else
					this.setDefault(this.parseFilters(AnWTFilterSpec.NON_REV_FILTER_STR));
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
						throw new IllegalArgumentException("unhandled spec tile type " + this.specValType[0][0] + " in transformation");
				}
			}
		}

		// Check consistency between filter and quantization type
		// specification
		for (int t = nt - 1; 0 <= t; t--) {
			for (int c = nc - 1; 0 <= c; c--) {
				// Reversible quantization
				if (qts.getTileCompVal(t, c).equals("reversible")) {
					// If filter is reversible, it is OK
					if (this.isReversible(t, c))
						continue;

					// If no filter has been defined, use reversible filter
					if (!isFilterSpecified) {
						this.setTileCompVal(t, c, this.parseFilters(AnWTFilterSpec.REV_FILTER_STR));
					} else {
						// Non reversible filter specified -> Error
						throw new IllegalArgumentException("Filter of tile-component (" + t + "," + c
								+ ") does not allow reversible quantization. Specify '-Qtype "
								+ "expounded' or '-Qtype derived' in the command line.");
					}
				} else { // No reversible quantization
					// No reversible filter -> OK
					if (!this.isReversible(t, c))
						continue;

					// If no filter has been specified, use non-reversible
					// filter
					if (!isFilterSpecified) {
						this.setTileCompVal(t, c, this.parseFilters(AnWTFilterSpec.NON_REV_FILTER_STR));
					} else {
						// Reversible filter specified -> Error
						throw new IllegalArgumentException("Filter of tile-component (" + t + "," + c
								+ ") does not allow non-reversible quantization. Specify '-Qtype "
								+ "reversible' in the command line");
					}
				}
			}
		}
	}

	/**
	 * Parse filters from the given word
	 *
	 * @param word String to parse
	 * @return Analysis wavelet filter (first dimension: by direction, second
	 * dimension: by decomposition levels)
	 */
	private AnWTFilter[][] parseFilters(final String word) {
		final AnWTFilter[][] filt = new AnWTFilter[2][1];
		if ("w5x3".equalsIgnoreCase(word)) {
			filt[0][0] = new AnWTFilterIntLift5x3();
			filt[1][0] = new AnWTFilterIntLift5x3();
			return filt;
		} else if ("w9x7".equalsIgnoreCase(word)) {
			filt[0][0] = new AnWTFilterFloatLift9x7();
			filt[1][0] = new AnWTFilterFloatLift9x7();
			return filt;
		} else {
			throw new IllegalArgumentException("Non JPEG 2000 part I filter: " + word);
		}
	}

	/**
	 * Returns the data type used by the filters in this object, as defined in
	 * the 'DataBlk' interface for specified tile-component.
	 *
	 * @param t Tile index
	 * @param c Component index
	 * @return The data type of the filters in this object
	 * @see jj2000.j2k.image.DataBlk
	 */
	public int getWTDataType(final int t, final int c) {
		final AnWTFilter[][] an = (AnWTFilter[][]) this.getSpec(t, c);
		return an[0][0].getDataType();
	}

	/**
	 * Returns the horizontal analysis filters to be used in component 'n' and
	 * tile 't'.
	 *
	 * <p>
	 * The horizontal analysis filters are returned in an array of AnWTFilter.
	 * Each element contains the horizontal filter for each resolution level
	 * starting with resolution level 1 (i.e. the analysis filter to go from
	 * resolution level 1 to resolution level 0). If there are less elements
	 * than the maximum resolution level, then the last element is assumed to be
	 * repeated.
	 *
	 * @param t The tile index, in raster scan order
	 * @param c The component index.
	 * @return The array of horizontal analysis filters for component 'n' and
	 * tile 't'.
	 */
	public AnWTFilter[] getHFilters(final int t, final int c) {
		final AnWTFilter[][] an = (AnWTFilter[][]) this.getSpec(t, c);
		return an[0];
	}

	/**
	 * Returns the vertical analysis filters to be used in component 'n' and
	 * tile 't'.
	 *
	 * <p>
	 * The vertical analysis filters are returned in an array of AnWTFilter.
	 * Each element contains the vertical filter for each resolution level
	 * starting with resolution level 1 (i.e. the analysis filter to go from
	 * resolution level 1 to resolution level 0). If there are less elements
	 * than the maximum resolution level, then the last element is assumed to be
	 * repeated.
	 *
	 * @param t The tile index, in raster scan order
	 * @param c The component index.
	 * @return The array of horizontal analysis filters for component 'n' and
	 * tile 't'.
	 */
	public AnWTFilter[] getVFilters(final int t, final int c) {
		final AnWTFilter[][] an = (AnWTFilter[][]) this.getSpec(t, c);
		return an[1];
	}

	/**
	 * Debugging method
	 */
	@Override
	public String toString() {
		String str = "";
		AnWTFilter[][] an;

		str += "nTiles=" + this.nTiles + "\nnComp=" + this.nComp + "\n\n";

		for (int t = 0; t < this.nTiles; t++) {
			for (int c = 0; c < this.nComp; c++) {
				an = (AnWTFilter[][]) this.getSpec(t, c);

				str += "(t:" + t + ",c:" + c + ")\n";

				// Horizontal filters
				str += "\tH:";
				for (int i = 0; i < an[0].length; i++)
					str += " " + an[0][i];
				// Horizontal filters
				str += "\n\tV:";
				for (int i = 0; i < an[1].length; i++)
					str += " " + an[1][i];
				str += "\n";
			}
		}

		return str;
	}

	/**
	 * Check the reversibility of filters contained is the given tile-component.
	 *
	 * @param t The index of the tile
	 * @param c The index of the component
	 */
	public boolean isReversible(final int t, final int c) {
		// Note: no need to buffer the result since this method is
		// normally called once per tile-component.
		final AnWTFilter[] hfilter = this.getHFilters(t, c);
		final AnWTFilter[] vfilter = this.getVFilters(t, c);

		// As soon as a filter is not reversible, false can be returned
		for (int i = hfilter.length - 1; 0 <= i; i--)
			if (!hfilter[i].isReversible() || !vfilter[i].isReversible())
				return false;
		return true;
	}

}
