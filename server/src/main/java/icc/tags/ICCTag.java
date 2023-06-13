/*****************************************************************************
 *
 * $Id: ICCTag.java,v 1.1 2002/07/25 14:56:37 grosbois Exp $
 *
 * Copyright Eastman Kodak Company, 343 State Street, Rochester, NY 14650
 * $Date $
 ****************************************************************************/

package icc.tags;

import icc.ICCProfile;

import java.nio.charset.StandardCharsets;

/**
 * An ICC profile contains a 128-byte header followed by a variable number of
 * tags contained in a tag table. Each tag is a structured block of ints. The
 * tags share a common format on disk starting with a signature, an offset to
 * the tag data, and a length of the tag data. The tag data itself is found at
 * the given offset in the file and consists of a tag type int, followed by a
 * reserved int, followed by a data block, the structure of which is unique to
 * the tag type.
 * <p>
 * This class is the abstract super class of all tags. It models that part of
 * the structure which is common among tags of all types.
 * <p>
 * It also contains the definitions of the various tag types.
 * 
 * 
 * @see icc.tags.ICCTagTable
 * @version 1.0
 * @author Bruce A. Kern
 */
public abstract class ICCTag
{

	// Tag Signature Strings
	private static final String sdwCprtSignature = "cprt";
	private static final String sdwDescSignature = "desc";
	private static final String sdwWtPtSignature = "wtpt";
	private static final String sdwBkPtSignature = "bkpt";
	private static final String sdwRXYZSignature = "rXYZ";
	private static final String sdwGXYZSignature = "gXYZ";
	private static final String sdwBXYZSignature = "bXYZ";
	private static final String sdwKXYZSignature = "kXYZ";
	private static final String sdwRTRCSignature = "rTRC";
	private static final String sdwGTRCSignature = "gTRC";
	private static final String sdwBTRCSignature = "bTRC";
	private static final String sdwKTRCSignature = "kTRC";
	private static final String sdwDmndSignature = "dmnd";
	private static final String sdwDmddSignature = "dmdd";

	// Tag Signatures
	private static final int kdwCprtSignature = ICCProfile.getInt(ICCTag.sdwCprtSignature.getBytes(StandardCharsets.UTF_8), 0);
	private static final int kdwDescSignature = ICCProfile.getInt(ICCTag.sdwDescSignature.getBytes(StandardCharsets.UTF_8), 0);
	private static final int kdwWtPtSignature = ICCProfile.getInt(ICCTag.sdwWtPtSignature.getBytes(StandardCharsets.UTF_8), 0);
	private static final int kdwBkPtSignature = ICCProfile.getInt(ICCTag.sdwBkPtSignature.getBytes(StandardCharsets.UTF_8), 0);
	private static final int kdwRXYZSignature = ICCProfile.getInt(ICCTag.sdwRXYZSignature.getBytes(StandardCharsets.UTF_8), 0);
	private static final int kdwGXYZSignature = ICCProfile.getInt(ICCTag.sdwGXYZSignature.getBytes(StandardCharsets.UTF_8), 0);
	private static final int kdwBXYZSignature = ICCProfile.getInt(ICCTag.sdwBXYZSignature.getBytes(StandardCharsets.UTF_8), 0);
	private static final int kdwKXYZSignature = ICCProfile.getInt(ICCTag.sdwKXYZSignature.getBytes(StandardCharsets.UTF_8), 0);
	private static final int kdwRTRCSignature = ICCProfile.getInt(ICCTag.sdwRTRCSignature.getBytes(StandardCharsets.UTF_8), 0);
	private static final int kdwGTRCSignature = ICCProfile.getInt(ICCTag.sdwGTRCSignature.getBytes(StandardCharsets.UTF_8), 0);
	private static final int kdwBTRCSignature = ICCProfile.getInt(ICCTag.sdwBTRCSignature.getBytes(StandardCharsets.UTF_8), 0);
	private static final int kdwKTRCSignature = ICCProfile.getInt(ICCTag.sdwKTRCSignature.getBytes(StandardCharsets.UTF_8), 0);
	private static final int kdwDmndSignature = ICCProfile.getInt(ICCTag.sdwDmndSignature.getBytes(StandardCharsets.UTF_8), 0);
	private static final int kdwDmddSignature = ICCProfile.getInt(ICCTag.sdwDmddSignature.getBytes(StandardCharsets.UTF_8), 0);

	// Tag Type Strings
	private static final String sdwTextDescType = "desc";
	private static final String sdwTextType = "text";
	private static final String sdwCurveType = "curv";
	private static final String sdwCurveTypeReverse = "vruc";
	private static final String sdwXYZType = "XYZ ";
	private static final String sdwXYZTypeReverse = " ZYX";

	// Tag Types
	private static final int kdwTextDescType = ICCProfile.getInt(ICCTag.sdwTextDescType.getBytes(StandardCharsets.UTF_8), 0);
	private static final int kdwTextType = ICCProfile.getInt(ICCTag.sdwTextType.getBytes(StandardCharsets.UTF_8), 0);
	private static final int kdwCurveType = ICCProfile.getInt(ICCTag.sdwCurveType.getBytes(StandardCharsets.UTF_8), 0);
	private static final int kdwCurveTypeReverse = ICCProfile.getInt(ICCTag.sdwCurveTypeReverse.getBytes(StandardCharsets.UTF_8), 0);
	private static final int kdwXYZType = ICCProfile.getInt(ICCTag.sdwXYZType.getBytes(StandardCharsets.UTF_8), 0);
	private static final int kdwXYZTypeReverse = ICCProfile.getInt(ICCTag.sdwXYZTypeReverse.getBytes(StandardCharsets.UTF_8), 0);

	/** Tag id */
	public final int signature; // Tag signature
	/** Tag type */
	public final int type;
	/** Tag data */
	public final byte[] data; // Tag type
	/** offset to tag data in the array */
	public final int offset;
	/** size of the tag data in the array */
	public final int count;

	/**
	 * Create a string representation of the tag type
	 * 
	 * @param type
	 *            input
	 * @return String representation of the type
	 */
	public static String typeString(final int type)
	{

		if (type == ICCTag.kdwTextDescType)
			return ICCTag.sdwTextDescType;
		else if (type == ICCTag.kdwTextType)
			return ICCTag.sdwTextDescType;
		else if (type == ICCTag.kdwCurveType)
			return ICCTag.sdwCurveType;
		else if (type == ICCTag.kdwCurveTypeReverse)
			return ICCTag.sdwCurveTypeReverse;
		else if (type == ICCTag.kdwXYZType)
			return ICCTag.sdwXYZType;
		else if (type == ICCTag.kdwXYZTypeReverse)
			return ICCTag.sdwXYZTypeReverse;
		else
			return "bad tag type";
	}

	/**
	 * Create a string representation of the signature
	 * 
	 * @param signature
	 *            input
	 * @return String representation of the signature
	 */
	public static String signatureString(final int signature)
	{
		if (signature == ICCTag.kdwCprtSignature)
			return ICCTag.sdwCprtSignature;
		else if (signature == ICCTag.kdwDescSignature)
			return ICCTag.sdwDescSignature;
		else if (signature == ICCTag.kdwWtPtSignature)
			return ICCTag.sdwWtPtSignature;
		else if (signature == ICCTag.kdwBkPtSignature)
			return ICCTag.sdwBkPtSignature;
		else if (signature == ICCTag.kdwRXYZSignature)
			return ICCTag.sdwRXYZSignature;
		else if (signature == ICCTag.kdwGXYZSignature)
			return ICCTag.sdwGXYZSignature;
		else if (signature == ICCTag.kdwBXYZSignature)
			return ICCTag.sdwBXYZSignature;
		else if (signature == ICCTag.kdwKXYZSignature)
			return ICCTag.sdwKXYZSignature;
		else if (signature == ICCTag.kdwRTRCSignature)
			return ICCTag.sdwRTRCSignature;
		else if (signature == ICCTag.kdwGTRCSignature)
			return ICCTag.sdwGTRCSignature;
		else if (signature == ICCTag.kdwBTRCSignature)
			return ICCTag.sdwBTRCSignature;
		else if (signature == ICCTag.kdwKTRCSignature)
			return ICCTag.sdwKTRCSignature;
		else if (signature == ICCTag.kdwDmndSignature)
			return ICCTag.sdwDmndSignature;
		else if (signature == ICCTag.kdwDmddSignature)
			return ICCTag.sdwDmddSignature;
		else
			return "bad tag signature";
	}

	/**
	 * Factory method for creating a tag of a specific type.
	 * 
	 * @param signature
	 *            tag to create
	 * @param data
	 *            byte array containg embedded tag data
	 * @param offset
	 *            to tag data in the array
	 * @param count
	 *            size of tag data in bytes
	 * @return specified ICCTag
	 */
	public static ICCTag createInstance(final int signature, final byte[] data, final int offset, final int count)
	{

		final int type = ICCProfile.getInt(data, offset);

		if (type == ICCTag.kdwTextDescType)
			return new ICCTextDescriptionType(signature, data, offset, count);
		else if (type == ICCTag.kdwTextType)
			return new ICCTextType(signature, data, offset, count);
		else if (type == ICCTag.kdwXYZType)
			return new ICCXYZType(signature, data, offset, count);
		else if (type == ICCTag.kdwXYZTypeReverse)
			return new ICCXYZTypeReverse(signature, data, offset, count);
		else if (type == ICCTag.kdwCurveType)
			return new ICCCurveType(signature, data, offset, count);
		else if (type == ICCTag.kdwCurveTypeReverse)
			return new ICCCurveTypeReverse(signature, data, offset, count);
		else
			throw new IllegalArgumentException("bad tag type");
	}

	/**
	 * Ued by subclass initialization to store the state common to all tags
	 * 
	 * @param signature
	 *            tag being created
	 * @param data
	 *            byte array containg embedded tag data
	 * @param offset
	 *            to tag data in the array
	 * @param count
	 *            size of tag data in bytes
	 */
	protected ICCTag(final int signature, final byte[] data, final int offset, final int count)
	{
		this.signature = signature;
		this.data = data;
		this.offset = offset;
		this.count = count;
		type = ICCProfile.getInt(data, offset);
	}

	@Override
	public String toString()
	{
		return ICCTag.signatureString(this.signature) + ":" + ICCTag.typeString(this.type);
	}
	/* end class ICCTag */
}
