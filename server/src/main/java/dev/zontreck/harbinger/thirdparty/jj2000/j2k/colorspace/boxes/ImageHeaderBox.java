/*****************************************************************************
 *
 * $Id: ImageHeaderBox.java,v 1.1 2002/07/25 14:50:47 grosbois Exp $
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
 * This class models the Image Header box contained in a JP2 image. It is a stub
 * class here since for colormapping the knowlege of the existance of the box in
 * the image is sufficient.
 *
 * @author Bruce A. Kern
 * @version 1.0
 */
public final class ImageHeaderBox extends JP2Box {
	static {
		JP2Box.type = 69686472;
	}

	long height;
	long width;
	int nc;
	short bpc;
	short c;
	boolean unk;
	boolean ipr;

	/**
	 * Construct an ImageHeaderBox from an input image.
	 *
	 * @param in       RandomAccessIO jp2 image
	 * @param boxStart offset to the start of the box in the image
	 * @throws IOException , ColorSpaceException
	 */
	public ImageHeaderBox ( final RandomAccessIO in , final int boxStart ) throws IOException, ColorSpaceException {
		super ( in , boxStart );
		this.readBox ( );
	}

	/**
	 * Return a suitable String representation of the class instance.
	 */
	@Override
	public String toString ( ) {

		final String rep = "[ImageHeaderBox " + JP2Box.eol + "  " +
				"height= " + this.height + ", " +
				"width= " + this.width + JP2Box.eol + "  " +
				"nc= " + this.nc + ", " +
				"bpc= " + String.valueOf ( this.bpc ) + ", " +
				"c= " + String.valueOf ( this.c ) + JP2Box.eol + "  " +
				"image dev.zontreck.harbinger.thirdparty.jj2000.j2k.colorspace is " + ( this.unk ? "known" : "unknown" ) +
				", the image " + ( this.ipr ? "contains " : "does not contain " ) +
				"intellectual property" + "]";

		return rep;
	}

	/**
	 * Analyze the box content.
	 */
	void readBox ( ) throws IOException {
		final byte[] bfr = new byte[ 14 ];
		this.in.seek ( this.dataStart );
		this.in.readFully ( bfr , 0 , 14 );

		this.height = ICCProfile.getInt ( bfr , 0 );
		this.width = ICCProfile.getInt ( bfr , 4 );
		this.nc = ICCProfile.getShort ( bfr , 8 );
		this.bpc = ( short ) ( bfr[ 10 ] & 0x00ff );
		this.c = ( short ) ( bfr[ 11 ] & 0x00ff );
		this.unk = 0 == bfr[ 12 ];
		this.ipr = 1 == bfr[ 13 ];
	}

	/* end class ImageHeaderBox */
}
