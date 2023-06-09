/*****************************************************************************
 *
 * $Id: LookUpTable16LinearSRGBtoSRGB.java,v 1.1 2002/07/25 14:56:47 grosbois Exp $
 *
 * Copyright Eastman Kodak Company, 343 State Street, Rochester, NY 14650
 * $Date $
 ****************************************************************************/

package dev.zontreck.harbinger.thirdparty.jj2000.j2k.icc.lut;

/**
 * A Linear 16 bit SRGB to SRGB lut
 *
 * @author Bruce A. Kern
 * @version 1.0
 */
public class LookUpTable16LinearSRGBtoSRGB extends LookUpTable16 {

	/**
	 * Construct the lut
	 *
	 * @param wShadowCutoff        size of shadow region
	 * @param dfShadowSlope        shadow region parameter
	 * @param ksRGBLinearMaxValue  size of lut
	 * @param ksRGB8ScaleAfterExp  post shadow region parameter
	 * @param ksRGBExponent        post shadow region parameter
	 * @param ksRGB8ReduceAfterExp post shadow region parameter
	 */
	protected LookUpTable16LinearSRGBtoSRGB (
			final int wShadowCutoff , final double dfShadowSlope , final int ksRGBLinearMaxValue ,
			final double ksRGB8ScaleAfterExp , final double ksRGBExponent , final double ksRGB8ReduceAfterExp
	) {

		super ( ksRGBLinearMaxValue + 1 , 0 );

		int i = - 1;
		final double dfNormalize = 1.0 / ksRGBLinearMaxValue;

		// Generate the final linear-sRGB to non-linear sRGB LUT
		for ( i = 0; i <= wShadowCutoff ; i++ )
			this.lut[ i ] = ( byte ) Math.floor ( dfShadowSlope * i + 0.5 );

		// Now calculate the rest
		for ( ; i <= ksRGBLinearMaxValue ; i++ )
			this.lut[ i ] = ( byte ) Math.floor ( ksRGB8ScaleAfterExp * Math.pow ( i * dfNormalize , ksRGBExponent )
					- ksRGB8ReduceAfterExp + 0.5 );
	}

	/**
	 * Factory method for creating the lut.
	 *
	 * @param wShadowCutoff       size of shadow region
	 * @param dfShadowSlope       shadow region parameter
	 * @param ksRGBLinearMaxValue size of lut
	 * @param ksRGB8ScaleAfterExp post shadow region parameter
	 * @param ksRGBExponent       post shadow region parameter
	 * @param ksRGB8ReduceAfterEx post shadow region parameter
	 * @return the lut
	 */
	public static LookUpTable16LinearSRGBtoSRGB createInstance (
			final int wShadowCutoff , final double dfShadowSlope ,
			final int ksRGBLinearMaxValue , final double ksRGB8ScaleAfterExp , final double ksRGBExponent , final double ksRGB8ReduceAfterEx
	) {
		return new LookUpTable16LinearSRGBtoSRGB ( wShadowCutoff , dfShadowSlope , ksRGBLinearMaxValue ,
				ksRGB8ScaleAfterExp , ksRGBExponent , ksRGB8ReduceAfterEx
		);
	}

	/* end class LookUpTable16LinearSRGBtoSRGB */
}
