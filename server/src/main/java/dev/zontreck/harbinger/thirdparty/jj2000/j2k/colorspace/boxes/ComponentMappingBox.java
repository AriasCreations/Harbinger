/*****************************************************************************
 *
 * $Id: ComponentMappingBox.java,v 1.1 2002/07/25 14:50:46 grosbois Exp $
 *
 * Copyright Eastman Kodak Company, 343 State Street, Rochester, NY 14650
 * $Date $
 ****************************************************************************/

package dev.zontreck.harbinger.thirdparty.jj2000.j2k.colorspace.boxes;

import dev.zontreck.harbinger.thirdparty.jj2000.j2k.colorspace.ColorSpaceException;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.icc.ICCProfile;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.io.RandomAccessIO;

import java.io.IOException;
import java.util.Iterator;
import java.util.Vector;

/**
 * This class maps the components in the codestream to channels in the image. It
 * models the Component Mapping box in the JP2 header.
 * 
 * @version 1.0
 * @author Bruce A. Kern
 */
public final class ComponentMappingBox extends JP2Box
{
	static
	{
		JP2Box.type = 0x636d6170;
	}

	private int nChannels;
	private final Vector<byte[]> map = new Vector<byte[]>();

	/**
	 * Construct a ComponentMappingBox from an input image.
	 * 
	 * @param in
	 *            RandomAccessIO jp2 image
	 * @param boxStart
	 *            offset to the start of the box in the image
	 * @exception IOException
	 *                , ColorSpaceException
	 */
	public ComponentMappingBox(final RandomAccessIO in, final int boxStart) throws IOException, ColorSpaceException
	{
		super(in, boxStart);
		this.readBox();
	}

	/** Analyze the box content. */
	void readBox() throws IOException
	{
		this.nChannels = (this.boxEnd - this.dataStart) / 4;
		this.in.seek(this.dataStart);
		for (int offset = this.dataStart; offset < this.boxEnd; offset += 4)
		{
			final byte[] mapping = new byte[4];
			this.in.readFully(mapping, 0, 4);
			this.map.addElement(mapping);
		}
	}

	/* Return the number of mapped channels. */
	public int getNChannels()
	{
		return this.nChannels;
	}

	/* Return the component mapped to the channel. */
	public int getCMP(final int channel)
	{
		final byte[] mapping = this.map.elementAt(channel);
		return ICCProfile.getShort(mapping, 0) & 0x0000ffff;
	}

	/** Return the channel type. */
	public short getMTYP(final int channel)
	{
		final byte[] mapping = this.map.elementAt(channel);
		return (short) (mapping[2] & 0x00ff);
	}

	/** Return the palette index for the channel. */
	public short getPCOL(final int channel)
	{
		final byte[] mapping = this.map.elementAt(channel);
		return (short) (mapping[3] & 0x000ff);
	}

	/** Return a suitable String representation of the class instance. */
	@Override
	public String toString()
	{
		final StringBuffer rep = new StringBuffer("[ComponentMappingBox ").append("  ");
		rep.append("nChannels= ").append(this.nChannels);
		Iterator<byte[]> iterator = map.iterator();
		while (iterator.hasNext())
		{
			final byte[] bfr = iterator.next();
			rep.append(JP2Box.eol).append("  ").append("CMP= ").append(this.getCMP(bfr)).append(", ");
			rep.append("MTYP= ").append(String.valueOf(this.getMTYP(bfr))).append(", ");
			rep.append("PCOL= ").append(String.valueOf(this.getPCOL(bfr)));
		}
		rep.append("]");
		return rep.toString();
	}

	private int getCMP(final byte[] mapping)
	{
		return ICCProfile.getShort(mapping, 0) & 0x0000ffff;
	}

	private short getMTYP(final byte[] mapping)
	{
		return (short) (mapping[2] & 0x00ff);
	}

	private short getPCOL(final byte[] mapping)
	{
		return (short) (mapping[3] & 0x000ff);
	}

	/* end class ComponentMappingBox */
}
