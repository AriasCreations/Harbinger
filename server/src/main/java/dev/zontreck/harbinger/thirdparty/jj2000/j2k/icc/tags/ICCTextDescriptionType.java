/*****************************************************************************
 *
 * $Id: ICCTextDescriptionType.java,v 1.1 2002/07/25 14:56:37 grosbois Exp $
 *
 * Copyright Eastman Kodak Company, 343 State Street, Rochester, NY 14650
 * $Date $
 ****************************************************************************/

package dev.zontreck.harbinger.thirdparty.jj2000.j2k.icc.tags;

import dev.zontreck.harbinger.thirdparty.jj2000.j2k.icc.ICCProfile;

import java.nio.charset.StandardCharsets;

/**
 * A text based ICC tag
 * 
 * @version 1.0
 * @author Bruce A. Kern
 */
public class ICCTextDescriptionType extends ICCTag
{

	/** Tag fields */
	public final int type;
	/** Tag fields */
	public final int reserved;
	/** Tag fields */
	public final int size;
	/** Tag fields */
	public final byte[] ascii;

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
	protected ICCTextDescriptionType(final int signature, final byte[] data, int offset, final int length)
	{

		super(signature, data, offset, length);

		this.type = ICCProfile.getInt(data, offset);
		offset += ICCProfile.int_size;

		this.reserved = ICCProfile.getInt(data, offset);
		offset += ICCProfile.int_size;

		this.size = ICCProfile.getInt(data, offset);
		offset += ICCProfile.int_size;

		this.ascii = new byte[this.size - 1];
		System.arraycopy(data, offset, this.ascii, 0, this.size - 1);
	}

	/** Return the string rep of this tag. */
	@Override
	public String toString()
	{
		return "[" + super.toString() + " \"" + new String(this.ascii, StandardCharsets.UTF_8) + "\"]";
	}

	/* end class ICCTextDescriptionType */
}
