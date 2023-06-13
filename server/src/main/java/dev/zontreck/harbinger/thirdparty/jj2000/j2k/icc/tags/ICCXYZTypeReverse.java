/*****************************************************************************
 *
 * $Id: ICCXYZTypeReverse.java,v 1.1 2002/07/25 14:56:38 grosbois Exp $
 *
 * Copyright Eastman Kodak Company, 343 State Street, Rochester, NY 14650
 * $Date $
 ****************************************************************************/

package dev.zontreck.harbinger.thirdparty.jj2000.j2k.icc.tags;

import dev.zontreck.harbinger.thirdparty.jj2000.j2k.icc.ICCProfile;

/**
 * A tag containing a triplet.
 * 
 * @see dev.zontreck.harbinger.thirdparty.jj2000.j2k.icc.tags.ICCXYZType
 * @see dev.zontreck.harbinger.thirdparty.jj2000.j2k.icc.types.XYZNumber
 * @version 1.0
 * @author Bruce A. Kern
 */
public class ICCXYZTypeReverse extends ICCXYZType
{

	/** x component */
	public final long x;
	/** y component */
	public final long y;
	/** z component */
	public final long z;

	/**
	 * Construct this tag from its constituant parts
	 * 
	 * @param signature
	 *            tag id
	 * @param data
	 *            array of bytes
	 * @param offset
	 *            to data in the data array
	 * @param length
	 *            of data in the data array
	 */
	protected ICCXYZTypeReverse(final int signature, final byte[] data, final int offset, final int length)
	{
		super(signature, data, offset, length);
		this.z = ICCProfile.getInt(data, offset + 2 * ICCProfile.int_size);
		this.y = ICCProfile.getInt(data, offset + 3 * ICCProfile.int_size);
		this.x = ICCProfile.getInt(data, offset + 4 * ICCProfile.int_size);
	}

	/** Return the string rep of this tag. */
	@Override
	public String toString()
	{
		return "[" + super.toString() + "(" + this.x + ", " + this.y + ", " + this.z + ")]";
	}

	/* end class ICCXYZTypeReverse */
}
