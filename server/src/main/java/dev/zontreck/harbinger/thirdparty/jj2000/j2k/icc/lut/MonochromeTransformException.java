/*****************************************************************************
 *
 * $Id: MonochromeTransformException.java,v 1.1 2002/07/25 14:56:49 grosbois Exp $
 *
 * Copyright Eastman Kodak Company, 343 State Street, Rochester, NY 14650
 * $Date $
 ****************************************************************************/

package dev.zontreck.harbinger.thirdparty.jj2000.j2k.icc.lut;

/**
 * Exception thrown by MonochromeTransformTosRGB.
 * 
 * @see dev.zontreck.harbinger.thirdparty.jj2000.j2k.icc.lut.MonochromeTransformTosRGB
 * @version 1.0
 * @author Bruce A. Kern
 */

public class MonochromeTransformException extends Exception
{
	private static final long serialVersionUID = 1L;

	/**
	 * Contruct with message
	 * 
	 * @param msg
	 *            returned by getMessage()
	 */
	MonochromeTransformException(final String msg)
	{
		super(msg);
	}

	/**
	 * Empty constructor
	 */
	MonochromeTransformException()
	{
	}

	/* end class MonochromeTransformException */
}
