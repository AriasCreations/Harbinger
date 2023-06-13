/*****************************************************************************
 *
 * $Id: LookUpTableFPInterp.java,v 1.1 2002/07/25 14:56:48 grosbois Exp $
 *
 * Copyright Eastman Kodak Company, 343 State Street, Rochester, NY 14650
 * $Date $
 ****************************************************************************/

package dev.zontreck.harbinger.thirdparty.jj2000.j2k.icc.lut;

import dev.zontreck.harbinger.thirdparty.jj2000.j2k.icc.tags.ICCCurveType;

/**
 * An interpolated floating point lut
 *
 * @author Bruce A.Kern
 * @version 1.0
 */
public class LookUpTableFPInterp extends LookUpTableFP {

	/**
	 * Construct the lut from the curve data
	 *
	 * @oaram curve the data
	 * @oaram dwNumInput the lut size
	 */
	public LookUpTableFPInterp (
			final ICCCurveType curve , // Pointer to the curve data
			final int dwNumInput // Number of input values in created LUT
	) {
		super ( curve , dwNumInput );

		int dwLowIndex, dwHighIndex; // Indices of interpolation points
		double dfLowIndex, dfHighIndex; // FP indices of interpolation points
		double dfTargetIndex; // Target index into interpolation table
		final double dfRatio; // Ratio of LUT input points to curve values
		double dfLow, dfHigh; // Interpolation values

		dfRatio = ( double ) ( curve.nEntries - 1 ) / ( dwNumInput - 1 );

		for ( int i = 0 ; i < dwNumInput ; i++ ) {
			dfTargetIndex = i * dfRatio;
			dfLowIndex = Math.floor ( dfTargetIndex );
			dwLowIndex = ( int ) dfLowIndex;
			dfHighIndex = Math.ceil ( dfTargetIndex );
			dwHighIndex = ( int ) dfHighIndex;
			if ( dwLowIndex == dwHighIndex )
				this.lut[ i ] = ( float ) ICCCurveType.CurveToDouble ( curve.entry ( dwLowIndex ) );
			else {
				dfLow = ICCCurveType.CurveToDouble ( curve.entry ( dwLowIndex ) );
				dfHigh = ICCCurveType.CurveToDouble ( curve.entry ( dwHighIndex ) );
				this.lut[ i ] = ( float ) ( dfLow + ( dfHigh - dfLow ) * ( dfTargetIndex - dfLowIndex ) );
			}
		}
	}

	/**
	 * Create an abbreviated string representation of a 16 bit lut.
	 *
	 * @return the lut as a String
	 */
	@Override
	public String toString ( ) {
		final String rep = "[LookUpTable32 " + " nentries= " + this.lut.length +
				"]";
		return rep;
	}

	/* end class LookUpTableFPInterp */
}
