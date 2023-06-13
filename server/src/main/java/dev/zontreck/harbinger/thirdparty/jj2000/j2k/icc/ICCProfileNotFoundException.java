/*****************************************************************************
 *
 * $Id: ICCProfileNotFoundException.java,v 1.1 2002/07/25 14:56:55 grosbois Exp $
 *
 * Copyright Eastman Kodak Company, 343 State Street, Rochester, NY 14650
 * $Date $
 ****************************************************************************/

package dev.zontreck.harbinger.thirdparty.jj2000.j2k.icc;

/**
 * This exception is thrown when an image contains no dev.zontreck.harbinger.thirdparty.jj2000.j2k.icc profile. is incorrect.
 *
 * @author Bruce A. Kern
 * @version 1.0
 */

public class ICCProfileNotFoundException extends ICCProfileException {
	private static final long serialVersionUID = 1L;

	/**
	 * Contruct with message
	 *
	 * @param msg returned by getMessage()
	 */
	ICCProfileNotFoundException ( final String msg ) {
		super ( msg );
	}

	/**
	 * Empty constructor
	 */
	ICCProfileNotFoundException ( ) {
		super ( "no dev.zontreck.harbinger.thirdparty.jj2000.j2k.icc profile in image" );
	}

	/* end class ICCProfileNotFoundException */
}
