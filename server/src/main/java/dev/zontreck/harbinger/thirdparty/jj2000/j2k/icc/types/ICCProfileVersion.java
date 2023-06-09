/*****************************************************************************
 *
 * $Id: ICCProfileVersion.java,v 1.1 2002/07/25 14:56:31 grosbois Exp $
 *
 * Copyright Eastman Kodak Company, 343 State Street, Rochester, NY 14650
 * $Date $
 ****************************************************************************/

package dev.zontreck.harbinger.thirdparty.jj2000.j2k.icc.types;

import dev.zontreck.harbinger.thirdparty.jj2000.j2k.icc.ICCProfile;

import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * This class describes the ICCProfile Version as contained in the header of the
 * ICC Profile.
 *
 * @author Bruce A. Kern
 * @version 1.0
 * @see dev.zontreck.harbinger.thirdparty.jj2000.j2k.icc.ICCProfile
 * @see dev.zontreck.harbinger.thirdparty.jj2000.j2k.icc.types.ICCProfileHeader
 */
public class ICCProfileVersion {
	/**
	 * Field size
	 */
	public static final int size = 4 * ICCProfile.byte_size;
	private final byte reserved1;
	private final byte reserved2;
	/**
	 * Major revision number in binary coded decimal
	 */
	public byte uMajor;
	/**
	 * Minor revision in high nibble, bug fix revision in low nibble, both in
	 * binary coded decimal
	 */
	public byte uMinor;

	/**
	 * Construct from constituent parts.
	 */
	public ICCProfileVersion ( final byte major , final byte minor , final byte res1 , final byte res2 ) {
		this.uMajor = major;
		this.uMinor = minor;
		this.reserved1 = res1;
		this.reserved2 = res2;
	}

	/**
	 * Construct from file content.
	 */
	public void write ( final RandomAccessFile raf ) throws IOException {
		raf.write ( this.uMajor );
		raf.write ( this.uMinor );
		raf.write ( this.reserved1 );
		raf.write ( this.reserved2 );
	}

	/**
	 * String representation of class instance.
	 */
	@Override
	public String toString ( ) {
		return "Version " + this.uMajor + "." + this.uMinor;
	}

	/* end class ICCProfileVersion */
}
