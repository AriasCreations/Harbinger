/*****************************************************************************
 *
 * $Id: ICCProfileInvalidException.java,v 1.1 2002/07/25 14:56:55 grosbois Exp $
 *
 * Copyright Eastman Kodak Company, 343 State Street, Rochester, NY 14650
 * $Date $
 ****************************************************************************/

package dev.zontreck.harbinger.thirdparty.jj2000.j2k.icc;

/**
 * This exception is thrown when the content of an an dev.zontreck.harbinger.thirdparty.jj2000.j2k.icc profile is in someway
 * incorrect.
 * 
 * @see ICCProfile
 * @version 1.0
 * @author Bruce A. Kern
 */

public class ICCProfileInvalidException extends ICCProfileException
{
	private static final long serialVersionUID = 1L;

	/**
	 * Contruct with message
	 * 
	 * @param msg
	 *            returned by getMessage()
	 */
	ICCProfileInvalidException(final String msg)
	{
		super(msg);
	}

	/**
	 * Empty constructor
	 */
	ICCProfileInvalidException()
	{
		super("dev.zontreck.harbinger.thirdparty.jj2000.j2k.icc profile is invalid");
	}

	/* end class ICCProfileInvalidException */
}
