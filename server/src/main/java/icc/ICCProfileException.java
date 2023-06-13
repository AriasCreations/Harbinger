/*****************************************************************************
 *
 * $Id: ICCProfileException.java,v 1.2 2002/08/08 14:08:13 grosbois Exp $
 *
 * Copyright Eastman Kodak Company, 343 State Street, Rochester, NY 14650
 * $Date $
 ****************************************************************************/
package icc;

/**
 * This exception is thrown when the content of a profile is incorrect.
 * 
 * @see ICCProfile
 * @version 1.0
 * @author Bruce A. Kern
 */
public class ICCProfileException extends Exception
{
	private static final long serialVersionUID = 1L;

	/**
	 * Contruct with message
	 * 
	 * @param msg
	 *            returned by getMessage()
	 */
	public ICCProfileException(final String msg)
	{
		super(msg);
	}

	/**
	 * Empty constructor
	 */
	public ICCProfileException()
	{
	}
}
