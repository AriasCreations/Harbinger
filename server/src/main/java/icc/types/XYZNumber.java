/*****************************************************************************
 *
 * $Id: XYZNumber.java,v 1.1 2002/07/25 14:56:31 grosbois Exp $
 *
 * Copyright Eastman Kodak Company, 343 State Street, Rochester, NY 14650
 * $Date $
 ****************************************************************************/

package icc.types;

import java.io.IOException;
import java.io.RandomAccessFile;
import icc.ICCProfile;

/**
 * A convientient representation for the contents of the ICCXYZTypeTag class.
 * 
 * @see icc.tags.ICCXYZType
 * @version 1.0
 * @author Bruce A. Kern
 */
public class XYZNumber
{
	public static final int size = 3 * ICCProfile.int_size;

	/** x value */
	public int dwX; // X tristimulus value
	/** y value */
	public int dwY; // Y tristimulus value
	/** z value */
	public int dwZ; // Z tristimulus value

	/** Construct from constituent parts. */
	public XYZNumber(final int x, final int y, final int z)
	{
		this.dwX = x;
		this.dwY = y;
		this.dwZ = z;
	}

	/** Normalization utility */
	public static int DoubleToXYZ(final double x)
	{
		return (int) Math.floor(x * 65536.0 + 0.5);
	}

	/** Normalization utility */
	public static double XYZToDouble(final int x)
	{
		return x / 65536.0;
	}

	/** Write to a file */
	public void write(final RandomAccessFile raf) throws IOException
	{
		raf.writeInt(this.dwX);
		raf.writeInt(this.dwY);
		raf.writeInt(this.dwZ);
	}

	/** String representation of class instance. */
	@Override
	public String toString()
	{
		return "[" + this.dwX + ", " + this.dwY + ", " + this.dwZ + "]";
	}

	/* end class XYZNumber */
}
