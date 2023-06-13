/*****************************************************************************
 *
 * $Id: ICCDateTime.java,v 1.1 2002/07/25 14:56:31 grosbois Exp $
 *
 * Copyright Eastman Kodak Company, 343 State Street, Rochester, NY 14650
 * $Date $
 ****************************************************************************/

package icc.types;

import java.io.IOException;
import java.io.RandomAccessFile;
import icc.ICCProfile;

/**
 * Date Time format for tags
 * 
 * @version 1.0
 * @author Bruce A. Kern
 */
public class ICCDateTime
{
	public static final int size = 6 * ICCProfile.short_size;

	/** Year datum. */
	public short wYear; // Number of the actual year (i.e. 1994)
	/** Month datum. */
	public short wMonth; // Number of the month (1-12)
	/** Day datum. */
	public short wDay; // Number of the day
	/** Hour datum. */
	public short wHours; // Number of hours (0-23)
	/** Minute datum. */
	public short wMinutes; // Number of minutes (0-59)
	/** Second datum. */
	public short wSeconds; // Number of seconds (0-59)

	/** Construct an ICCDateTime from parts */
	public ICCDateTime(final short year, final short month, final short day, final short hour, final short minute, final short second)
	{
		this.wYear = year;
		this.wMonth = month;
		this.wDay = day;
		this.wHours = hour;
		this.wMinutes = minute;
		this.wSeconds = second;
	}

	/** Write an ICCDateTime to a file. */
	public void write(final RandomAccessFile raf) throws IOException
	{
		raf.writeShort(this.wYear);
		raf.writeShort(this.wMonth);
		raf.writeShort(this.wDay);
		raf.writeShort(this.wHours);
		raf.writeShort(this.wMinutes);
		raf.writeShort(this.wSeconds);
	}

	/** Return a ICCDateTime representation. */
	@Override
	public String toString()
	{
		return String.valueOf(this.wYear) + "/" + String.valueOf(this.wMonth) + "/" + String.valueOf(this.wDay) + " "
				+ String.valueOf(this.wHours) + ":" + String.valueOf(this.wMinutes) + ":" + String.valueOf(this.wSeconds);
	}

	/* end class ICCDateTime */
}
