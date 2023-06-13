/*****************************************************************************
 *
 * $Id: RestrictedICCProfile.java,v 1.1 2002/07/25 14:56:56 grosbois Exp $
 *
 * Copyright Eastman Kodak Company, 343 State Street, Rochester, NY 14650
 * $Date $
 ****************************************************************************/

package dev.zontreck.harbinger.thirdparty.jj2000.j2k.icc;

import dev.zontreck.harbinger.thirdparty.jj2000.j2k.icc.tags.ICCCurveType;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.icc.tags.ICCXYZType;

/**
 * This profile is constructed by parsing an ICCProfile and is the profile
 * actually applied to the image.
 *
 * @author Bruce A. Kern
 * @version 1.0
 */
public abstract class RestrictedICCProfile {

	/**
	 * input type enumerator
	 */
	public static final int kMonochromeInput = 0;
	/**
	 * input type enumerator
	 */
	public static final int kThreeCompInput = 1;
	protected static final String eol = System.getProperty ( "line.separator" );
	/**
	 * Component index
	 */
	protected static final int GRAY = ICCProfile.GRAY;
	/**
	 * Component index
	 */
	protected static final int RED = ICCProfile.RED;
	/**
	 * Component index
	 */
	protected static final int GREEN = ICCProfile.GREEN;
	/**
	 * Component index
	 */
	protected static final int BLUE = ICCProfile.BLUE;
	/**
	 * Curve data
	 */
	public ICCCurveType[] trc;
	/**
	 * Colorant data
	 */
	public ICCXYZType[] colorant;

	/**
	 * Construct the common state of all gray RestrictedICCProfiles
	 *
	 * @param gcurve curve data
	 */
	protected RestrictedICCProfile ( final ICCCurveType gcurve ) {
		this.trc = new ICCCurveType[ 1 ];
		this.colorant = null;
		this.trc[ RestrictedICCProfile.GRAY ] = gcurve;
	}

	/**
	 * Construct the common state of all 3 component RestrictedICCProfiles
	 *
	 * @param rcurve    red curve
	 * @param gcurve    green curve
	 * @param bcurve    blue curve
	 * @param rcolorant red colorant
	 * @param gcolorant green colorant
	 * @param bcolorant blue colorant
	 */
	protected RestrictedICCProfile (
			final ICCCurveType rcurve , final ICCCurveType gcurve , final ICCCurveType bcurve , final ICCXYZType rcolorant ,
			final ICCXYZType gcolorant , final ICCXYZType bcolorant
	) {
		this.trc = new ICCCurveType[ 3 ];
		this.colorant = new ICCXYZType[ 3 ];

		this.trc[ RestrictedICCProfile.RED ] = rcurve;
		this.trc[ RestrictedICCProfile.GREEN ] = gcurve;
		this.trc[ RestrictedICCProfile.BLUE ] = bcurve;

		this.colorant[ RestrictedICCProfile.RED ] = rcolorant;
		this.colorant[ RestrictedICCProfile.GREEN ] = gcolorant;
		this.colorant[ RestrictedICCProfile.BLUE ] = bcolorant;
	}

	/**
	 * Factory method for creating a RestrictedICCProfile from 3 component curve
	 * and colorant data.
	 *
	 * @param rcurve    red curve
	 * @param gcurve    green curve
	 * @param bcurve    blue curve
	 * @param rcolorant red colorant
	 * @param gcolorant green colorant
	 * @param bcolorant blue colorant
	 * @return MatrixBasedRestrictedProfile
	 */
	public static RestrictedICCProfile createInstance (
			final ICCCurveType rcurve , final ICCCurveType gcurve , final ICCCurveType bcurve ,
			final ICCXYZType rcolorant , final ICCXYZType gcolorant , final ICCXYZType bcolorant
	) {

		return MatrixBasedRestrictedProfile.createInstance ( rcurve , gcurve , bcurve , rcolorant , gcolorant , bcolorant );
	}

	/**
	 * Factory method for creating a RestrictedICCProfile from gray curve data.
	 *
	 * @param gcurve gray curve
	 * @return MonochromeInputRestrictedProfile
	 */
	public static RestrictedICCProfile createInstance ( final ICCCurveType gcurve ) {
		return MonochromeInputRestrictedProfile.createInstance ( gcurve );
	}

	/**
	 * Returns the appropriate input type enum.
	 */
	public abstract int getType ( );

	/* end class RestrictedICCProfile */
}
