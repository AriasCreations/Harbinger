/*****************************************************************************
 *
 * $Id: ColorSpaceException.java,v 1.1 2002/07/25 14:52:00 grosbois Exp $
 *
 * Copyright Eastman Kodak Company, 343 State Street, Rochester, NY 14650
 * $Date $
 ****************************************************************************/

package dev.zontreck.harbinger.thirdparty.jj2000.j2k.colorspace;

/**
 * This exception is thrown when the content of an image contains an incorrect
 * dev.zontreck.harbinger.thirdparty.jj2000.j2k.colorspace box
 * 
 * @see dev.zontreck.harbinger.thirdparty.jj2000.j2k.colorspace.ColorSpaceMapper
 * @version 1.0
 * @author Bruce A. Kern
 */

public class ColorSpaceException extends Exception
{
	private static final long serialVersionUID = 1L;

	/**
	 * Contruct with message
	 * 
	 * @param msg
	 *            returned by getMessage()
	 */
	public ColorSpaceException(final String msg)
	{
		super(msg);
	}

	/**
	 * Empty constructor
	 */
	public ColorSpaceException()
	{
	}

	/* end class ColorSpaceException */
}
