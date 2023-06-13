/*****************************************************************************
 *
 * $Id: ICCXYZType.java,v 1.1 2002/07/25 14:56:37 grosbois Exp $
 *
 * Copyright Eastman Kodak Company, 343 State Street, Rochester, NY 14650
 * $Date $
 ****************************************************************************/

package dev.zontreck.harbinger.thirdparty.jj2000.j2k.icc.tags;

import java.io.IOException;
import java.io.RandomAccessFile;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.icc.ICCProfile;

/**
 * A tag containing a triplet.
 * 
 * @see dev.zontreck.harbinger.thirdparty.jj2000.j2k.icc.tags.ICCXYZTypeReverse
 * @see dev.zontreck.harbinger.thirdparty.jj2000.j2k.icc.types.XYZNumber
 * @version 1.0
 * @author Bruce A. Kern
 */
public class ICCXYZType extends ICCTag
{

	/** x component */
	public final long x;
	/** y component */
	public final long y;
	/** z component */
	public final long z;

	/** Normalization utility */
	public static long DoubleToXYZ(final double x)
	{
		return (long) Math.floor(x * 65536.0 + 0.5);
	}

	/** Normalization utility */
	public static double XYZToDouble(final long x)
	{
		return x / 65536.0;
	}

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
	protected ICCXYZType(final int signature, final byte[] data, final int offset, final int length)
	{
		super(signature, data, offset, length);
		this.x = ICCProfile.getInt(data, offset + 2 * ICCProfile.int_size);
		this.y = ICCProfile.getInt(data, offset + 3 * ICCProfile.int_size);
		this.z = ICCProfile.getInt(data, offset + 4 * ICCProfile.int_size);
	}

	/** Return the string rep of this tag. */
	@Override
	public String toString()
	{
		return "[" + super.toString() + "(" + this.x + ", " + this.y + ", " + this.z + ")]";
	}

	/** Write to a file. */
	public void write(final RandomAccessFile raf) throws IOException
	{
		final byte[] xb = ICCProfile.setLong(this.x);
		final byte[] yb = ICCProfile.setLong(this.y);
		final byte[] zb = ICCProfile.setLong(this.z);

		raf.write(xb, ICCProfile.int_size, 0);
		raf.write(yb, ICCProfile.int_size, 0);
		raf.write(zb, ICCProfile.int_size, 0);
	}

	/* end class ICCXYZType */
}
