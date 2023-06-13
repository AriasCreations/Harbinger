/*****************************************************************************
 *
 * $Id: LookUpTableFP.java,v 1.1 2002/07/25 14:56:49 grosbois Exp $
 *
 * Copyright Eastman Kodak Company, 343 State Street, Rochester, NY 14650
 * $Date $
 ****************************************************************************/

package dev.zontreck.harbinger.thirdparty.jj2000.j2k.icc.lut;

import dev.zontreck.harbinger.thirdparty.jj2000.j2k.icc.tags.ICCCurveType;

/**
 * Toplevel class for a float [] lut.
 *
 * @author Bruce A. Kern
 * @version 1.0
 */
public abstract class LookUpTableFP extends LookUpTable {

	/**
	 * The lut values.
	 */
	public final float[] lut;

	/**
	 * Construct an empty lut
	 *
	 * @param dwNumInput the size of the lut t lut.
	 */
	protected LookUpTableFP (
			final ICCCurveType curve , // Pointer to the curve data
			final int dwNumInput // Number of input values in created LUT
	) {
		super ( curve , dwNumInput );
		this.lut = new float[ dwNumInput ];
	}

	/**
	 * Factory method for getting a lut from a given curve.
	 *
	 * @param curve      the data
	 * @param dwNumInput the size of the lut
	 * @return the lookup table
	 */

	public static LookUpTableFP createInstance (
			final ICCCurveType curve , // Pointer to
			// the curve
			// data
			final int dwNumInput // Number of input values in created LUT
	) {

		if ( 1 == curve.nEntries )
			return new LookUpTableFPGamma ( curve , dwNumInput );
		return new LookUpTableFPInterp ( curve , dwNumInput );
	}

	/**
	 * lut accessor
	 *
	 * @param index of the element
	 * @return the lut [index]
	 */
	public final float elementAt ( final int index ) {
		return this.lut[ index ];
	}

	/* end class LookUpTableFP */
}
