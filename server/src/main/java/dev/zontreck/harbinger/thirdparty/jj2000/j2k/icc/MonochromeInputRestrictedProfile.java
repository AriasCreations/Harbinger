/*****************************************************************************
 *
 * $Id: MonochromeInputRestrictedProfile.java,v 1.1 2002/07/25 14:56:56 grosbois Exp $
 *
 * Copyright Eastman Kodak Company, 343 State Street, Rochester, NY 14650
 * $Date $
 ****************************************************************************/

package dev.zontreck.harbinger.thirdparty.jj2000.j2k.icc;

import dev.zontreck.harbinger.thirdparty.jj2000.j2k.icc.tags.ICCCurveType;

/**
 * This class is a 1 component RestrictedICCProfile
 *
 * @author Bruce A Kern
 * @version 1.0
 */
public class MonochromeInputRestrictedProfile extends RestrictedICCProfile {

	/**
	 * Construct a 1 component RestrictedICCProfile
	 *
	 * @param c Gray TRC curve
	 */
	private MonochromeInputRestrictedProfile ( final ICCCurveType c ) {
		super ( c );
	}

	/**
	 * Factory method which returns a 1 component RestrictedICCProfile
	 *
	 * @param c Gray TRC curve
	 * @return the RestrictedICCProfile
	 */
	public static RestrictedICCProfile createInstance ( final ICCCurveType c ) {
		return new MonochromeInputRestrictedProfile ( c );
	}

	/**
	 * Get the type of RestrictedICCProfile for this object
	 *
	 * @return kMonochromeInput
	 */
	@Override
	public int getType ( ) {
		return RestrictedICCProfile.kMonochromeInput;
	}

	/**
	 * @return String representation of a MonochromeInputRestrictedProfile
	 */
	@Override
	public String toString ( ) {

		return "Monochrome Input Restricted ICC profile" + RestrictedICCProfile.eol + "trc[GRAY]:" + RestrictedICCProfile.eol + this.trc[ RestrictedICCProfile.GRAY ] + RestrictedICCProfile.eol;
	}

	/* end class MonochromeInputRestrictedProfile */
}
