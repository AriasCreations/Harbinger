/*****************************************************************************
 *
 * $Id: ICCMonochromeInputProfile.java,v 1.1 2002/07/25 14:56:54 grosbois Exp $
 *
 * Copyright Eastman Kodak Company, 343 State Street, Rochester, NY 14650
 * $Date $
 ****************************************************************************/

package dev.zontreck.harbinger.thirdparty.jj2000.j2k.icc;

import dev.zontreck.harbinger.thirdparty.jj2000.j2k.colorspace.ColorSpace;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.colorspace.ColorSpaceException;

/**
 * The monochrome ICCProfile.
 *
 * @author Bruce A. Kern
 * @version 1.0
 */
public class ICCMonochromeInputProfile extends ICCProfile {

	/**
	 * Construct a ICCMonochromeInputProfile corresponding to the profile file
	 */
	protected ICCMonochromeInputProfile ( final ColorSpace csm ) throws ColorSpaceException, ICCProfileInvalidException {
		super ( csm );
	}

	/**
	 * Return the ICCProfile embedded in the input image
	 */
	public static ICCMonochromeInputProfile createInstance ( final ColorSpace csm ) throws ColorSpaceException,
			ICCProfileInvalidException {
		return new ICCMonochromeInputProfile ( csm );
	}

	/* end class ICCMonochromeInputProfile */
}
