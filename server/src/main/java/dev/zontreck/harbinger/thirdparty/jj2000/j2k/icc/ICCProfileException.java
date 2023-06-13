/*****************************************************************************
 *
 * $Id: ICCProfileException.java,v 1.2 2002/08/08 14:08:13 grosbois Exp $
 *
 * Copyright Eastman Kodak Company, 343 State Street, Rochester, NY 14650
 * $Date $
 ****************************************************************************/
package dev.zontreck.harbinger.thirdparty.jj2000.j2k.icc;

/**
 * This exception is thrown when the content of a profile is incorrect.
 *
 * @author Bruce A. Kern
 * @version 1.0
 * @see ICCProfile
 */
public class ICCProfileException extends Exception {
	private static final long serialVersionUID = 1L;

	/**
	 * Contruct with message
	 *
	 * @param msg returned by getMessage()
	 */
	public ICCProfileException ( final String msg ) {
		super ( msg );
	}

	/**
	 * Empty constructor
	 */
	public ICCProfileException ( ) {
	}
}
