/*****************************************************************************
 *
 * $Id: ICCCurveTypeReverse.java,v 1.1 2002/07/25 14:56:36 grosbois Exp $
 *
 * Copyright Eastman Kodak Company, 343 State Street, Rochester, NY 14650
 * $Date $
 ****************************************************************************/

package dev.zontreck.harbinger.thirdparty.jj2000.j2k.icc.tags;

import dev.zontreck.harbinger.thirdparty.jj2000.j2k.icc.ICCProfile;

/**
 * The ICCCurveReverse tag
 *
 * @author Bruce A. Kern
 * @version 1.0
 */
public class ICCCurveTypeReverse extends ICCTag {

	private static final String eol = System.getProperty ( "line.separator" );
	/**
	 * Tag fields
	 */
	public final int type;
	/**
	 * Tag fields
	 */
	public final int reserved;
	/**
	 * Tag fields
	 */
	public final int nEntries;
	/**
	 * Tag fields
	 */
	public final int[] entry;

	/**
	 * Construct this tag from its constituant parts
	 *
	 * @param signature tag id
	 * @param data      array of bytes
	 * @param offset    to data in the data array
	 * @param length    of data in the data array
	 */
	protected ICCCurveTypeReverse ( final int signature , final byte[] data , final int offset , final int length ) {
		super ( signature , data , offset , offset + 2 * ICCProfile.int_size );
		this.type = ICCProfile.getInt ( data , offset );
		this.reserved = ICCProfile.getInt ( data , offset + ICCProfile.int_size );
		this.nEntries = ICCProfile.getInt ( data , offset + 2 * ICCProfile.int_size );
		this.entry = new int[ this.nEntries ];
		for ( int i = 0 ; i < this.nEntries ; ++ i )
			// Reverse the storage order.
			this.entry[ this.nEntries - 1 + i ] = ICCProfile.getShort ( data , offset + 3 * ICCProfile.int_size + i
					* ICCProfile.short_size ) & 0xFFFF;
	}

	/**
	 * Normalization utility
	 */
	public static double CurveToDouble ( final int entry ) {
		return ICCCurveType.CurveToDouble ( entry );
	}

	/**
	 * Normalization utility
	 */
	public static short DoubleToCurve ( final int entry ) {
		return ICCCurveType.DoubleToCurve ( entry );
	}

	/**
	 * Normalization utility
	 */
	public static double CurveGammaToDouble ( final int entry ) {
		return ICCCurveType.CurveGammaToDouble ( entry );
	}

	/**
	 * Return the string rep of this tag.
	 */
	@Override
	public String toString ( ) {
		final StringBuffer rep = new StringBuffer ( "[" ).append ( super.toString ( ) ).append ( ICCCurveTypeReverse.eol );
		rep.append ( "num entries = " + this.nEntries + ICCCurveTypeReverse.eol );
		rep.append ( "data length = " + this.entry.length + ICCCurveTypeReverse.eol );
		for ( int i = 0 ; i < this.nEntries ; ++ i )
			rep.append ( ICCProfile.toHexString ( this.entry[ i ] ) + ICCCurveTypeReverse.eol );
		return rep.append ( "]" ).toString ( );
	}

	/**
	 * Accessor for curve entry at index.
	 */
	public final int entry ( final int i ) {
		return this.entry[ i ];
	}

	/* end class ICCCurveTypeReverse */
}
