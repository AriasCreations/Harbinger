/*****************************************************************************
 *
 * $Id: ColorSpecificationBox.java,v 1.3 2002/08/08 14:07:53 grosbois Exp $
 *
 * Copyright Eastman Kodak Company, 343 State Street, Rochester, NY 14650
 * $Date $
 ****************************************************************************/
package dev.zontreck.harbinger.thirdparty.jj2000.j2k.colorspace.boxes;

import dev.zontreck.harbinger.thirdparty.jj2000.j2k.colorspace.ColorSpace;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.colorspace.ColorSpaceException;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.icc.ICCProfile;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.io.RandomAccessIO;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.util.FacilityManager;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.util.MsgLogger;

import java.io.IOException;

/**
 * This class models the Color Specification Box in a JP2 image.
 *
 * @author Bruce A. Kern
 * @version 1.0
 */
public final class ColorSpecificationBox extends JP2Box {
	static {
		JP2Box.type = 0x636f6c72;
	}

	private ColorSpace.MethodEnum method;
	private ColorSpace.CSEnum colorSpace;
	private byte[] iccProfile;

	/**
	 * Construct a ColorSpecificationBox from an input image.
	 *
	 * @param in       RandomAccessIO jp2 image
	 * @param boxStart offset to the start of the box in the image
	 * @throws IOException , ColorSpaceException
	 */
	public ColorSpecificationBox ( final RandomAccessIO in , final int boxStart ) throws IOException, ColorSpaceException {
		super ( in , boxStart );
		this.readBox ( );
	}

	/**
	 * Analyze the box content.
	 */
	private void readBox ( ) throws IOException, ColorSpaceException {
		final byte[] boxHeader = new byte[ 256 ];
		this.in.seek ( this.dataStart );
		this.in.readFully ( boxHeader , 0 , 11 );
		switch ( boxHeader[ 0 ] ) {
			case 1:
				this.method = ColorSpace.ENUMERATED;
				final int cs = ICCProfile.getInt ( boxHeader , 3 );
				switch ( cs ) {
					case 16:
						this.colorSpace = ColorSpace.sRGB;
						break; // from switch (cs)...
					case 17:
						this.colorSpace = ColorSpace.GreyScale;
						break; // from switch (cs)...
					case 18:
						this.colorSpace = ColorSpace.sYCC;
						break; // from switch (cs)...
					default:
						FacilityManager.getMsgLogger ( ).printmsg (
								MsgLogger.WARNING ,
								"Unknown enumerated dev.zontreck.harbinger.thirdparty.jj2000.j2k.colorspace (" + cs + ") in color specification box"
						);
						this.colorSpace = ColorSpace.Unknown;
				}
				break; // from switch (boxHeader[0])...
			case 2:
				this.method = ColorSpace.ICC_PROFILED;
				final int size = ICCProfile.getInt ( boxHeader , 3 );
				this.iccProfile = new byte[ size ];
				this.in.seek ( this.dataStart + 3 );
				this.in.readFully ( this.iccProfile , 0 , size );
				break; // from switch (boxHeader[0])...
			default:
				throw new ColorSpaceException ( "Bad specification method (" + boxHeader[ 0 ] + ") in " + this );
		}
	}

	/* Return an enumeration for the dev.zontreck.harbinger.thirdparty.jj2000.j2k.colorspace method. */
	public ColorSpace.MethodEnum getMethod ( ) {
		return this.method;
	}

	/* Return an enumeration for the dev.zontreck.harbinger.thirdparty.jj2000.j2k.colorspace. */
	public ColorSpace.CSEnum getColorSpace ( ) {
		return this.colorSpace;
	}

	/* Return a String representation of the dev.zontreck.harbinger.thirdparty.jj2000.j2k.colorspace. */
	public String getColorSpaceString ( ) {
		return this.colorSpace.value;
	}

	/* Return a String representation of the dev.zontreck.harbinger.thirdparty.jj2000.j2k.colorspace method. */
	public String getMethodString ( ) {
		return this.method.value;
	}

	/* Retrieve the ICC Profile from the image as a byte []. */
	public byte[] getICCProfile ( ) {
		return this.iccProfile;
	}

	/**
	 * Return a suitable String representation of the class instance.
	 */
	@Override
	public String toString ( ) {
		final String rep = "[ColorSpecificationBox " + "method= " + this.method + ", " +
				"dev.zontreck.harbinger.thirdparty.jj2000.j2k.colorspace= " + this.colorSpace + "]";
		return rep;
	}

	/* end class ColorSpecificationBox */
}
