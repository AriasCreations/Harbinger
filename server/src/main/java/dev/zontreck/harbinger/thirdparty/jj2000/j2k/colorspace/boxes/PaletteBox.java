/*****************************************************************************
 *
 * $Id: PaletteBox.java,v 1.1 2002/07/25 14:50:47 grosbois Exp $
 *
 * Copyright Eastman Kodak Company, 343 State Street, Rochester, NY 14650
 * $Date $
 ****************************************************************************/

package dev.zontreck.harbinger.thirdparty.jj2000.j2k.colorspace.boxes;

import dev.zontreck.harbinger.thirdparty.jj2000.j2k.colorspace.ColorSpaceException;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.icc.ICCProfile;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.io.RandomAccessIO;

import java.io.IOException;

/**
 * This class models the palette box contained in a JP2 image.
 * 
 * @version 1.0
 * @author Bruce A. Kern
 */
public final class PaletteBox extends JP2Box
{
	static
	{
		JP2Box.type = 0x70636c72;
	}

	private int nentries;
	private int ncolumns;
	private short[] bitdepth;
	private int[][] entries;

	/**
	 * Construct a PaletteBox from an input image.
	 * 
	 * @param in
	 *            RandomAccessIO jp2 image
	 * @param boxStart
	 *            offset to the start of the box in the image
	 * @exception IOException
	 *                , ColorSpaceException
	 */
	public PaletteBox(final RandomAccessIO in, final int boxStart) throws IOException, ColorSpaceException
	{
		super(in, boxStart);
		this.readBox();
	}

	/** Analyze the box content. */
	void readBox() throws IOException, ColorSpaceException
	{
		byte[] bfr = new byte[4];
		int i, j, b, m;

		// Read the number of palette entries and columns per entry.
		this.in.seek(this.dataStart);
		this.in.readFully(bfr, 0, 3);
		this.nentries = ICCProfile.getShort(bfr, 0) & 0x0000ffff;
		this.ncolumns = bfr[2] & 0x0000ffff;

		// Read the bitdepths for each column
		this.bitdepth = new short[this.ncolumns];
		bfr = new byte[this.ncolumns];
		this.in.readFully(bfr, 0, this.ncolumns);
		for (i = 0; i < this.ncolumns; ++i)
		{
			this.bitdepth[i] = (short) (bfr[i] & 0x00fff);
		}

		this.entries = new int[this.nentries * this.ncolumns][];

		bfr = new byte[2];
		for (i = 0; i < this.nentries; ++i)
		{
			this.entries[i] = new int[this.ncolumns];

			for (j = 0; j < this.ncolumns; ++j)
			{

				final int bd = this.getBitDepth(j);
				final boolean signed = this.isSigned(j);

				switch (this.getEntrySize(j))
				{
					case 1: // 8 bit entries
						this.in.readFully(bfr, 0, 1);
						b = bfr[0];
						break;

					case 2: // 16 bits
						this.in.readFully(bfr, 0, 2);
						b = ICCProfile.getShort(bfr, 0);
						break;

					default:
						throw new ColorSpaceException("palettes greater than 16 bits deep not supported");
				}

				if (signed)
				{
					// Do sign extension if high bit is set.
					if (0 == (b & (1 << (bd - 1))))
					{
						// high bit not set.
						m = (1 << bd) - 1;
						this.entries[i][j] = m & b;
					}
					else
					{
						// high bit set.
						m = 0xffffffff << bd;
						this.entries[i][j] = m | b;
					}
				}
				else
				{
					// Clear all high bits.
					m = (1 << bd) - 1;
					this.entries[i][j] = m & b;
				}
			}
		}
	}

	/** Return the number of palette entries. */
	public int getNumEntries()
	{
		return this.nentries;
	}

	/** Return the number of palette columns. */
	public int getNumColumns()
	{
		return this.ncolumns;
	}

	/** Are entries signed predicate. */
	public boolean isSigned(final int column)
	{
		return 1 == (bitdepth[column] & 0x80);
	}

	/** Are entries unsigned predicate. */
	public boolean isUnSigned(final int column)
	{
		return !this.isSigned(column);
	}

	/** Return the bitdepth of palette entries. */
	public short getBitDepth(final int column)
	{
		return (short) ((this.bitdepth[column] & 0x7f) + 1);
	}

	/** Return an entry for a given index and column. */
	public int getEntry(final int column, final int entry)
	{
		return this.entries[entry][column];
	}

	/** Return a suitable String representation of the class instance. */
	@Override
	public String toString()
	{
		final StringBuffer rep = new StringBuffer("[PaletteBox ").append("nentries= ").append(this.nentries)
				.append(", ncolumns= ").append(this.ncolumns).append(", bitdepth per column= (");
		for (int i = 0; i < this.ncolumns; ++i)
			rep.append(this.getBitDepth(i)).append(this.isSigned(i) ? "S" : "U").append(i < this.ncolumns - 1 ? ", " : "");
		return rep.append(")]").toString();
	}

	private int getEntrySize(final int column)
	{
		final int bd = this.getBitDepth(column);
		return 0 == bd / 8 + (bd % 8) ? 0 : 1;
	}

	/* end class PaletteBox */
}
