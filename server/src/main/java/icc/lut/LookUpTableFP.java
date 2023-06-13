/*****************************************************************************
 *
 * $Id: LookUpTableFP.java,v 1.1 2002/07/25 14:56:49 grosbois Exp $
 *
 * Copyright Eastman Kodak Company, 343 State Street, Rochester, NY 14650
 * $Date $
 ****************************************************************************/

package icc.lut;

import icc.tags.ICCCurveType;

/**
 * Toplevel class for a float [] lut.
 * 
 * @version 1.0
 * @author Bruce A. Kern
 */
public abstract class LookUpTableFP extends LookUpTable
{

	/** The lut values. */
	public final float[] lut;

	/**
	 * Factory method for getting a lut from a given curve.
	 * 
	 * @param curve
	 *            the data
	 * @param dwNumInput
	 *            the size of the lut
	 * @return the lookup table
	 */

	public static LookUpTableFP createInstance(final ICCCurveType curve, // Pointer to
											   // the curve
											   // data
											   final int dwNumInput // Number of input values in created LUT
	)
	{

		if (1 == curve.nEntries)
			return new LookUpTableFPGamma(curve, dwNumInput);
		return new LookUpTableFPInterp(curve, dwNumInput);
	}

	/**
	 * Construct an empty lut
	 * 
	 * @param dwNumInput
	 *            the size of the lut t lut.
	 */
	protected LookUpTableFP(final ICCCurveType curve, // Pointer to the curve data
							final int dwNumInput // Number of input values in created LUT
	)
	{
		super(curve, dwNumInput);
		this.lut = new float[dwNumInput];
	}

	/**
	 * lut accessor
	 * 
	 * @param index
	 *            of the element
	 * @return the lut [index]
	 */
	public final float elementAt(final int index)
	{
		return this.lut[index];
	}

	/* end class LookUpTableFP */
}
