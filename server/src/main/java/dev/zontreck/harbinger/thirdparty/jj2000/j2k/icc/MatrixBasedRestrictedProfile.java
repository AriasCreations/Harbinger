/*****************************************************************************
 *
 * $Id: MatrixBasedRestrictedProfile.java,v 1.1 2002/07/25 14:56:56 grosbois Exp $
 *
 * Copyright Eastman Kodak Company, 343 State Street, Rochester, NY 14650
 * $Date $
 ****************************************************************************/

package dev.zontreck.harbinger.thirdparty.jj2000.j2k.icc;

import dev.zontreck.harbinger.thirdparty.jj2000.j2k.icc.tags.ICCCurveType;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.icc.tags.ICCXYZType;

/**
 * This class is a 3 component RestrictedICCProfile
 *
 * @author Bruce A Kern
 * @version 1.0
 */
public class MatrixBasedRestrictedProfile extends RestrictedICCProfile {

	/**
	 * Construct a 3 component RestrictedICCProfile
	 *
	 * @param rcurve    Red TRC curve
	 * @param gcurve    Green TRC curve
	 * @param bcurve    Blue TRC curve
	 * @param rcolorant Red colorant
	 * @param gcolorant Green colorant
	 * @param bcolorant Blue colorant
	 */
	protected MatrixBasedRestrictedProfile (
			final ICCCurveType rcurve , final ICCCurveType gcurve , final ICCCurveType bcurve ,
			final ICCXYZType rcolorant , final ICCXYZType gcolorant , final ICCXYZType bcolorant
	) {
		super ( rcurve , gcurve , bcurve , rcolorant , gcolorant , bcolorant );
	}

	/**
	 * Factory method which returns a 3 component RestrictedICCProfile
	 *
	 * @param rcurve    Red TRC curve
	 * @param gcurve    Green TRC curve
	 * @param bcurve    Blue TRC curve
	 * @param rcolorant Red colorant
	 * @param gcolorant Green colorant
	 * @param bcolorant Blue colorant
	 * @return the RestrictedICCProfile
	 */
	public static RestrictedICCProfile createInstance (
			final ICCCurveType rcurve , final ICCCurveType gcurve , final ICCCurveType bcurve ,
			final ICCXYZType rcolorant , final ICCXYZType gcolorant , final ICCXYZType bcolorant
	) {
		return new MatrixBasedRestrictedProfile ( rcurve , gcurve , bcurve , rcolorant , gcolorant , bcolorant );
	}

	/**
	 * Get the type of RestrictedICCProfile for this object
	 *
	 * @return kThreeCompInput
	 */
	@Override
	public int getType ( ) {
		return RestrictedICCProfile.kThreeCompInput;
	}

	/**
	 * @return String representation of a MatrixBasedRestrictedProfile
	 */
	@Override
	public String toString ( ) {

		final String rep = "[Matrix-Based Input Restricted ICC profile" + RestrictedICCProfile.eol +
				"trc[RED]:" + RestrictedICCProfile.eol + this.trc[ RestrictedICCProfile.RED ] + RestrictedICCProfile.eol +
				"trc[RED]:" + RestrictedICCProfile.eol + this.trc[ RestrictedICCProfile.GREEN ] + RestrictedICCProfile.eol +
				"trc[RED]:" + RestrictedICCProfile.eol + this.trc[ RestrictedICCProfile.BLUE ] + RestrictedICCProfile.eol +
				"Red colorant:  " + this.colorant[ RestrictedICCProfile.RED ] + RestrictedICCProfile.eol +
				"Red colorant:  " + this.colorant[ RestrictedICCProfile.GREEN ] + RestrictedICCProfile.eol +
				"Red colorant:  " + this.colorant[ RestrictedICCProfile.BLUE ] + RestrictedICCProfile.eol +
				"]";

		return rep;
	}

	/* end class MatrixBasedRestrictedProfile */
}
