/*****************************************************************************
 *
 * $Id: LookUpTable32Interp.java,v 1.1 2002/07/25 14:56:47 grosbois Exp $
 *
 * Copyright Eastman Kodak Company, 343 State Street, Rochester, NY 14650
 * $Date $
 ****************************************************************************/

package dev.zontreck.harbinger.thirdparty.jj2000.j2k.icc.lut;

import dev.zontreck.harbinger.thirdparty.jj2000.j2k.icc.tags.ICCCurveType;

/**
 * An interpolated 32 bit lut
 * 
 * @version 1.0
 * @author Bruce A.Kern
 */

public class LookUpTable32Interp extends LookUpTable32
{

	/**
	 * Construct the lut from the curve data
	 * 
	 * @oaram curve the data
	 * @oaram dwNumInput the lut size
	 * @oaram dwMaxOutput the lut max value
	 */
	public LookUpTable32Interp(final ICCCurveType curve, // Pointer to the curve data
							   final int dwNumInput, // Number of input values in created LUT
							   final int dwMaxOutput // Maximum output value of the LUT
	)
	{
		super(curve, dwNumInput, dwMaxOutput);

		int dwLowIndex, dwHighIndex; // Indices of interpolation points
		double dfLowIndex, dfHighIndex; // FP indices of interpolation points
		double dfTargetIndex; // Target index into interpolation table
		final double dfRatio; // Ratio of LUT input points to curve values
		double dfLow, dfHigh; // Interpolation values
		double dfOut; // Output LUT value

		dfRatio = (double) (curve.count - 1) / (dwNumInput - 1);

		for (int i = 0; i < dwNumInput; i++)
		{
			dfTargetIndex = i * dfRatio;
			dfLowIndex = Math.floor(dfTargetIndex);
			dwLowIndex = (int) dfLowIndex;
			dfHighIndex = Math.ceil(dfTargetIndex);
			dwHighIndex = (int) dfHighIndex;

			if (dwLowIndex == dwHighIndex)
				dfOut = ICCCurveType.CurveToDouble(curve.entry(dwLowIndex));
			else
			{
				dfLow = ICCCurveType.CurveToDouble(curve.entry(dwLowIndex));
				dfHigh = ICCCurveType.CurveToDouble(curve.entry(dwHighIndex));
				dfOut = dfLow + (dfHigh - dfLow) * (dfTargetIndex - dfLowIndex);
			}

			this.lut[i] = (int) Math.floor(dfOut * dwMaxOutput + 0.5);
		}
	}

	/* end class LookUpTable32Interp */
}
