/*****************************************************************************
 *
 * $Id: ChannelDefinitionBox.java,v 1.1 2002/07/25 14:50:46 grosbois Exp $
 *
 * Copyright Eastman Kodak Company, 343 State Street, Rochester, NY 14650
 * $Date $
 ****************************************************************************/

package dev.zontreck.harbinger.thirdparty.jj2000.j2k.colorspace.boxes;

import dev.zontreck.harbinger.thirdparty.jj2000.j2k.colorspace.ColorSpaceException;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.icc.ICCProfile;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.io.RandomAccessIO;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Iterator;

/**
 * This class maps the components in the codestream to channels in the image. It
 * models the Component Mapping box in the JP2 header.
 * 
 * @version 1.0
 * @author Bruce A. Kern
 */
public final class ChannelDefinitionBox extends JP2Box
{
	static
	{
		JP2Box.type = 0x63646566;
	}

	private int ndefs;
	private final Hashtable<Integer, int[]> definitions = new Hashtable<Integer, int[]>();

	/**
	 * Construct a ChannelDefinitionBox from an input image.
	 * 
	 * @param in
	 *            RandomAccessIO jp2 image
	 * @param boxStart
	 *            offset to the start of the box in the image
	 * @exception IOException
	 *                , ColorSpaceException
	 */
	public ChannelDefinitionBox(final RandomAccessIO in, final int boxStart) throws IOException, ColorSpaceException
	{
		super(in, boxStart);
		this.readBox();
	}

	/** Analyze the box content. */
	private void readBox() throws IOException
	{

		final byte[] bfr = new byte[8];

		this.in.seek(this.dataStart);
		this.in.readFully(bfr, 0, 2);
		this.ndefs = ICCProfile.getShort(bfr, 0) & 0x0000ffff;

		final int offset = this.dataStart + 2;
		this.in.seek(offset);
		for (int i = 0; i < this.ndefs; ++i)
		{
			this.in.readFully(bfr, 0, 6);
			final int[] channel_def = new int[3];
			channel_def[0] = this.getCn(bfr);
			channel_def[1] = this.getTyp(bfr);
			channel_def[2] = this.getAsoc(bfr);
			this.definitions.put(Integer.valueOf(channel_def[0]), channel_def);
		}
	}

	/* Return the number of channel definitions. */
	public int getNDefs()
	{
		return this.ndefs;
	}

	/* Return the channel association. */
	public int getCn(final int asoc)
	{
		Iterator<Integer> iterator = definitions.keySet().iterator();
		while (iterator.hasNext())
		{
			final int[] bfr = this.definitions.get(iterator.next());
			if (asoc == this.getAsoc(bfr))
				return this.getCn(bfr);
		}
		return asoc;
	}

	/* Return the channel type. */
	public int getTyp(final int channel)
	{
		final int[] bfr = this.definitions.get(Integer.valueOf(channel));
		return this.getTyp(bfr);
	}

	/* Return the associated channel of the association. */
	public int getAsoc(final int channel)
	{
		final int[] bfr = this.definitions.get(Integer.valueOf(channel));
		return this.getAsoc(bfr);
	}

	/** Return a suitable String representation of the class instance. */
	@Override
	public String toString()
	{
		final StringBuffer rep = new StringBuffer("[ChannelDefinitionBox ").append(JP2Box.eol).append("  ");
		rep.append("ndefs= ").append(this.ndefs);

		Iterator<Integer> iterator = definitions.keySet().iterator();
		while (iterator.hasNext())
		{
			final int[] bfr = this.definitions.get(iterator.next());
			rep.append(JP2Box.eol).append("  ").append("Cn= ").append(this.getCn(bfr)).append(", ").append("Typ= ")
					.append(this.getTyp(bfr)).append(", ").append("Asoc= ")
					.append(this.getAsoc(bfr));
		}

		rep.append("]");
		return rep.toString();
	}

	/** Return the channel from the record. */
	private int getCn(final byte[] bfr)
	{
		return ICCProfile.getShort(bfr, 0);
	}

	/** Return the channel type from the record. */
	private int getTyp(final byte[] bfr)
	{
		return ICCProfile.getShort(bfr, 2);
	}

	/** Return the associated channel from the record. */
	private int getAsoc(final byte[] bfr)
	{
		return ICCProfile.getShort(bfr, 4);
	}

	private int getCn(final int[] bfr)
	{
		return bfr[0];
	}

	private int getTyp(final int[] bfr)
	{
		return bfr[1];
	}

	private int getAsoc(final int[] bfr)
	{
		return bfr[2];
	}

	/* end class ChannelDefinitionBox */
}
