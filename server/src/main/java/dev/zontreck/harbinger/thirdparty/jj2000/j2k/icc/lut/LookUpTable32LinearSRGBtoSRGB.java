/*****************************************************************************
 *
 * $Id: LookUpTable32LinearSRGBtoSRGB.java,v 1.1 2002/07/25 14:56:47 grosbois Exp $
 *
 * Copyright Eastman Kodak Company, 343 State Street, Rochester, NY 14650
 * $Date $
 ****************************************************************************/

package dev.zontreck.harbinger.thirdparty.jj2000.j2k.icc.lut;

/**
 * A Linear 32 bit SRGB to SRGB lut
 *
 * @author Bruce A. Kern
 * @version 1.0
 */
public class LookUpTable32LinearSRGBtoSRGB extends LookUpTable32 {

	/**
	 * Construct the lut
	 */
	protected LookUpTable32LinearSRGBtoSRGB (
			final int inMax , final int outMax , final double shadowCutoff , double shadowSlope ,
			double scaleAfterExp , final double exponent , double reduceAfterExp
	) {

		super ( inMax + 1 , outMax );

		int i = - 1;
		// Normalization factor for i.
		final double normalize = 1.0 / inMax;

		// Generate the final linear-sRGB to non-linear sRGB LUT

		// calculate where shadow portion of lut ends.
		final int cutOff = ( int ) Math.floor ( shadowCutoff * inMax );

		// Scale to account for output
		shadowSlope *= outMax;

		// Our output needs to be centered on zero so we shift it down.
		final int shift = ( outMax + 1 ) / 2;

		for ( i = 0; i <= cutOff ; i++ )
			this.lut[ i ] = ( int ) ( Math.floor ( shadowSlope * ( i * normalize ) + 0.5 ) - shift );

		// Scale values for output.
		scaleAfterExp *= outMax;
		reduceAfterExp *= outMax;

		// Now calculate the rest
		for ( ; i <= inMax ; i++ )
			this.lut[ i ] = ( int ) ( Math.floor ( scaleAfterExp * Math.pow ( i * normalize , exponent ) - reduceAfterExp + 0.5 ) - shift );
	}

	/**
	 * Factory method for creating the lut.
	 *
	 * @return the lut
	 */
	public static LookUpTable32LinearSRGBtoSRGB createInstance (
			final int inMax , final int outMax , final double shadowCutoff ,
			final double shadowSlope , final double scaleAfterExp , final double exponent , final double reduceAfterExp
	) {
		return new LookUpTable32LinearSRGBtoSRGB ( inMax , outMax , shadowCutoff , shadowSlope , scaleAfterExp , exponent ,
				reduceAfterExp
		);
	}

	@Override
	public String toString ( ) {
		return "[LookUpTable32LinearSRGBtoSRGB:" + "]";
	}

	/* end class LookUpTable32LinearSRGBtoSRGB */
}
