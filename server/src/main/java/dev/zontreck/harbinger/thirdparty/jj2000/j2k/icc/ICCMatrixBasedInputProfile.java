/*****************************************************************************
 *
 * $Id: ICCMatrixBasedInputProfile.java,v 1.1 2002/07/25 14:56:54 grosbois Exp $
 *
 * Copyright Eastman Kodak Company, 343 State Street, Rochester, NY 14650
 * $Date $
 ****************************************************************************/

package dev.zontreck.harbinger.thirdparty.jj2000.j2k.icc;

import dev.zontreck.harbinger.thirdparty.jj2000.j2k.colorspace.ColorSpace;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.colorspace.ColorSpaceException;

/**
 * This class enables an application to construct an 3 component ICCProfile
 *
 * @author Bruce A. Kern
 * @version 1.0
 */

public class ICCMatrixBasedInputProfile extends ICCProfile {

	/**
	 * Construct an ICCMatrixBasedInputProfile based on a suppled profile file.
	 *
	 * @throws ColorSpaceException
	 * @throws ICCProfileInvalidException
	 */
	protected ICCMatrixBasedInputProfile ( final ColorSpace csm ) throws ColorSpaceException, ICCProfileInvalidException {
		super ( csm );
	}

	/**
	 * Factory method to create ICCMatrixBasedInputProfile based on a suppled
	 * profile file.
	 *
	 * @return the ICCMatrixBasedInputProfile
	 * @throws ICCProfileInvalidException
	 * @throws ColorSpaceException
	 */
	public static ICCMatrixBasedInputProfile createInstance ( final ColorSpace csm ) throws ColorSpaceException,
			ICCProfileInvalidException {
		return new ICCMatrixBasedInputProfile ( csm );
	}

	/* end class ICCMatrixBasedInputProfile */
}
