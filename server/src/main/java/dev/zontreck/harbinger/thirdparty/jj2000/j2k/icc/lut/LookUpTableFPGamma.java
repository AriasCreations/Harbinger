/*****************************************************************************
 *
 * $Id: LookUpTableFPGamma.java,v 1.1 2002/07/25 14:56:48 grosbois Exp $
 *
 * Copyright Eastman Kodak Company, 343 State Street, Rochester, NY 14650
 * $Date $
 ****************************************************************************/

package dev.zontreck.harbinger.thirdparty.jj2000.j2k.icc.lut;

import dev.zontreck.harbinger.thirdparty.jj2000.j2k.icc.tags.ICCCurveType;

/**
 * Class Description
 * 
 * @version 1.0
 * @author Bruce A. Kern
 */

public class LookUpTableFPGamma extends LookUpTableFP
{

	double dfE = -1;

	// private static final String eol = System.getProperty("line.separator");

	public LookUpTableFPGamma(final ICCCurveType curve, // Pointer to the curve data
							  final int dwNumInput // Number of input values in created LUT
	)
	{
		super(curve, dwNumInput);

		// Gamma exponent for inverse transformation
		this.dfE = ICCCurveType.CurveGammaToDouble(curve.entry(0));
		for (int i = 0; i < dwNumInput; i++)
			this.lut[i] = (float) Math.pow((double) i / (dwNumInput - 1), this.dfE);
	}

	/**
	 * Create an abbreviated string representation of a 16 bit lut.
	 * 
	 * @return the lut as a String
	 */
	@Override
	public String toString()
	{
		final String rep = "[LookUpTableGamma " + "dfe= " + this.dfE +
				", nentries= " + this.lut.length +
				"]";
		return rep;
	}

	/* end class LookUpTableFPGamma */
}
