/*****************************************************************************
 *
 * $Id: ICCProfileHeader.java,v 1.1 2002/07/25 14:56:31 grosbois Exp $
 *
 * Copyright Eastman Kodak Company, 343 State Street, Rochester, NY 14650
 * $Date $
 ****************************************************************************/

package dev.zontreck.harbinger.thirdparty.jj2000.j2k.icc.types;

import java.io.RandomAccessFile;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import dev.zontreck.harbinger.thirdparty.jj2000.j2k.icc.ICCProfile;

/**
 * An ICC profile contains a 128-byte header followed by a variable number of
 * tags contained in a tag table. This class models the header portion of the
 * profile. Most fields in the header are ints. Some, such as data and version
 * are aggregations of ints. This class provides an api to those fields as well
 * as the definition of standard constants which are used in the header.
 * 
 * @see dev.zontreck.harbinger.thirdparty.jj2000.j2k.icc.ICCProfile
 * @version 1.0
 * @author Bruce A. Kern
 */

public class ICCProfileHeader
{
	private static final String eol = System.getProperty("line.separator");

	/*
	 * Define the set of standard signature and type values. Only those codes
	 * required for Restricted ICC use are defined here.
	 */

	/** Profile header signature */
	public static int kdwProfileSignature;
	static
	{
		ICCProfileHeader.kdwProfileSignature = ICCProfile.getInt("acsp".getBytes(StandardCharsets.UTF_8), 0);
	}

	/** Profile header signature */
	public static int kdwProfileSigReverse;
	static
	{
		ICCProfileHeader.kdwProfileSigReverse = ICCProfile.getInt("psca".getBytes(StandardCharsets.UTF_8), 0);
	}

	/* Offsets into ICCProfile header byte array. */
	private static final int offProfileSize = 0;
	private static final int offCMMTypeSignature = ICCProfileHeader.offProfileSize + ICCProfile.int_size;
	private static final int offProfileVersion = ICCProfileHeader.offCMMTypeSignature + ICCProfile.int_size;
	private static final int offProfileClass = ICCProfileHeader.offProfileVersion + ICCProfileVersion.size;
	private static final int offColorSpaceType = ICCProfileHeader.offProfileClass + ICCProfile.int_size;
	private static final int offPCSType = ICCProfileHeader.offColorSpaceType + ICCProfile.int_size;
	private static final int offDateTime = ICCProfileHeader.offPCSType + ICCProfile.int_size;
	private static final int offProfileSignature = ICCProfileHeader.offDateTime + ICCDateTime.size;
	private static final int offPlatformSignature = ICCProfileHeader.offProfileSignature + ICCProfile.int_size;
	private static final int offCMMFlags = ICCProfileHeader.offPlatformSignature + ICCProfile.int_size;
	private static final int offDeviceManufacturer = ICCProfileHeader.offCMMFlags + ICCProfile.int_size;
	private static final int offDeviceModel = ICCProfileHeader.offDeviceManufacturer + ICCProfile.int_size;
	private static final int offDeviceAttributes1 = ICCProfileHeader.offDeviceModel + ICCProfile.int_size;
	private static final int offDeviceAttributesReserved = ICCProfileHeader.offDeviceAttributes1 + ICCProfile.int_size;
	private static final int offRenderingIntent = ICCProfileHeader.offDeviceAttributesReserved + ICCProfile.int_size;
	private static final int offPCSIlluminant = ICCProfileHeader.offRenderingIntent + ICCProfile.int_size;
	private static final int offCreatorSig = ICCProfileHeader.offPCSIlluminant + XYZNumber.size;
	private static final int offReserved = ICCProfileHeader.offCreatorSig + ICCProfile.int_size;
	/** Size of the header */
	public static final int size = ICCProfileHeader.offReserved + 44 * ICCProfile.byte_size;

	/* Header fields mapped to primitive types. */
	/** Header field */
	public int dwProfileSize; // Size of the entire profile in bytes
	/** Header field */
	public int dwCMMTypeSignature; // The preferred CMM for this profile
	/** Header field */
	public int dwProfileClass; // Profile/Device class signature
	/** Header field */
	public int dwColorSpaceType; // Colorspace signature
	/** Header field */
	public int dwPCSType; // PCS type signature
	/** Header field */
	public int dwProfileSignature; // Must be 'acsp' (0x61637370)
	/** Header field */
	public int dwPlatformSignature; // Primary platform for which this profile
									// was created
	/** Header field */
	public int dwCMMFlags; // Flags to indicate various hints for the CMM
	/** Header field */
	public int dwDeviceManufacturer; // Signature of device manufacturer
	/** Header field */
	public int dwDeviceModel; // Signature of device model
	/** Header field */
	public int dwDeviceAttributes1; // Attributes of the device
	/** Header field */
	public int dwDeviceAttributesReserved;
	/** Header field */
	public int dwRenderingIntent; // Desired rendering intent for this profile
	/** Header field */
	public int dwCreatorSig; // Profile creator signature

	/** Header field */
	public byte[] reserved = new byte[44]; //

	/* Header fields mapped to ggregate types. */
	/** Header field */
	public ICCProfileVersion profileVersion; // Version of the profile format on
												// which
	/** Header field */
	public ICCDateTime dateTime; // Date and time of profile creation// this
									// profile is based
	/** Header field */
	public XYZNumber PCSIlluminant; // Illuminant used for this profile

	/** Construct and empty header */
	public ICCProfileHeader()
	{
	}

	/**
	 * Construct a header from a complete ICCProfile
	 * 
	 * @param data [] -- holds ICCProfile contents
	 */
	public ICCProfileHeader(final byte[] data)
	{

		this.dwProfileSize = ICCProfile.getInt(data, ICCProfileHeader.offProfileSize);
		this.dwCMMTypeSignature = ICCProfile.getInt(data, ICCProfileHeader.offCMMTypeSignature);
		this.dwProfileClass = ICCProfile.getInt(data, ICCProfileHeader.offProfileClass);
		this.dwColorSpaceType = ICCProfile.getInt(data, ICCProfileHeader.offColorSpaceType);
		this.dwPCSType = ICCProfile.getInt(data, ICCProfileHeader.offPCSType);
		this.dwProfileSignature = ICCProfile.getInt(data, ICCProfileHeader.offProfileSignature);
		this.dwPlatformSignature = ICCProfile.getInt(data, ICCProfileHeader.offPlatformSignature);
		this.dwCMMFlags = ICCProfile.getInt(data, ICCProfileHeader.offCMMFlags);
		this.dwDeviceManufacturer = ICCProfile.getInt(data, ICCProfileHeader.offDeviceManufacturer);
		this.dwDeviceModel = ICCProfile.getInt(data, ICCProfileHeader.offDeviceModel);
		this.dwDeviceAttributes1 = ICCProfile.getInt(data, ICCProfileHeader.offDeviceAttributesReserved);
		this.dwDeviceAttributesReserved = ICCProfile.getInt(data, ICCProfileHeader.offDeviceAttributesReserved);
		this.dwRenderingIntent = ICCProfile.getInt(data, ICCProfileHeader.offRenderingIntent);
		this.dwCreatorSig = ICCProfile.getInt(data, ICCProfileHeader.offCreatorSig);
		this.profileVersion = ICCProfile.getICCProfileVersion(data, ICCProfileHeader.offProfileVersion);
		this.dateTime = ICCProfile.getICCDateTime(data, ICCProfileHeader.offDateTime);
		this.PCSIlluminant = ICCProfile.getXYZNumber(data, ICCProfileHeader.offPCSIlluminant);

		System.arraycopy(data, 84, this.reserved, 0, this.reserved.length);
	}

	/**
	 * Write out this ICCProfile header to a RandomAccessFile
	 * 
	 * @param raf
	 *            sink for data
	 * @exception IOException
	 */
	public void write(final RandomAccessFile raf) throws IOException
	{

		raf.seek(ICCProfileHeader.offProfileSize);
		raf.write(this.dwProfileSize);
		raf.seek(ICCProfileHeader.offCMMTypeSignature);
		raf.write(this.dwCMMTypeSignature);
		raf.seek(ICCProfileHeader.offProfileVersion);
		this.profileVersion.write(raf);
		raf.seek(ICCProfileHeader.offProfileClass);
		raf.write(this.dwProfileClass);
		raf.seek(ICCProfileHeader.offColorSpaceType);
		raf.write(this.dwColorSpaceType);
		raf.seek(ICCProfileHeader.offPCSType);
		raf.write(this.dwPCSType);
		raf.seek(ICCProfileHeader.offDateTime);
		this.dateTime.write(raf);
		raf.seek(ICCProfileHeader.offProfileSignature);
		raf.write(this.dwProfileSignature);
		raf.seek(ICCProfileHeader.offPlatformSignature);
		raf.write(this.dwPlatformSignature);
		raf.seek(ICCProfileHeader.offCMMFlags);
		raf.write(this.dwCMMFlags);
		raf.seek(ICCProfileHeader.offDeviceManufacturer);
		raf.write(this.dwDeviceManufacturer);
		raf.seek(ICCProfileHeader.offDeviceModel);
		raf.write(this.dwDeviceModel);
		raf.seek(ICCProfileHeader.offDeviceAttributes1);
		raf.write(this.dwDeviceAttributes1);
		raf.seek(ICCProfileHeader.offDeviceAttributesReserved);
		raf.write(this.dwDeviceAttributesReserved);
		raf.seek(ICCProfileHeader.offRenderingIntent);
		raf.write(this.dwRenderingIntent);
		raf.seek(ICCProfileHeader.offPCSIlluminant);
		this.PCSIlluminant.write(raf);
		raf.seek(ICCProfileHeader.offCreatorSig);
		raf.write(this.dwCreatorSig);
		raf.seek(ICCProfileHeader.offReserved);
		raf.write(this.reserved);
	}

	/** String representation of class */
	@Override
	public String toString()
	{

		final String rep = "[ICCProfileHeader: " + ICCProfileHeader.eol + "         ProfileSize: " + Integer.toHexString(this.dwProfileSize) +
				ICCProfileHeader.eol + "    CMMTypeSignature: " + Integer.toHexString(this.dwCMMTypeSignature) +
				ICCProfileHeader.eol + "        ProfileClass: " + Integer.toHexString(this.dwProfileClass) +
				ICCProfileHeader.eol + "      ColorSpaceType: " + Integer.toHexString(this.dwColorSpaceType) +
				ICCProfileHeader.eol + "           dwPCSType: " + Integer.toHexString(this.dwPCSType) +
				ICCProfileHeader.eol + "  dwProfileSignature: " + Integer.toHexString(this.dwProfileSignature) +
				ICCProfileHeader.eol + " dwPlatformSignature: " + Integer.toHexString(this.dwPlatformSignature) +
				ICCProfileHeader.eol + "          dwCMMFlags: " + Integer.toHexString(this.dwCMMFlags) +
				ICCProfileHeader.eol + "dwDeviceManufacturer: " + Integer.toHexString(this.dwDeviceManufacturer) +
				ICCProfileHeader.eol + "       dwDeviceModel: " + Integer.toHexString(this.dwDeviceModel) +
				ICCProfileHeader.eol + " dwDeviceAttributes1: " + Integer.toHexString(this.dwDeviceAttributes1) +
				ICCProfileHeader.eol + "   dwRenderingIntent: " + Integer.toHexString(this.dwRenderingIntent) +
				ICCProfileHeader.eol + "        dwCreatorSig: " + Integer.toHexString(this.dwCreatorSig) +
				ICCProfileHeader.eol + "      profileVersion: " + this.profileVersion +
				ICCProfileHeader.eol + "            dateTime: " + this.dateTime +
				ICCProfileHeader.eol + "       PCSIlluminant: " + this.PCSIlluminant +
				"]";
		return rep;
	}

	/* end class ICCProfileHeader */
}
