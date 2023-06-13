/*****************************************************************************
 *
 * $Id: LookUpTable8.java,v 1.1 2002/07/25 14:56:48 grosbois Exp $
 *
 * Copyright Eastman Kodak Company, 343 State Street, Rochester, NY 14650
 * $Date $
 ****************************************************************************/

package dev.zontreck.harbinger.thirdparty.jj2000.j2k.icc.lut;

import dev.zontreck.harbinger.thirdparty.jj2000.j2k.icc.tags.ICCCurveType;

/**
 * Toplevel class for a byte [] lut.
 *
 * @author Bruce A. Kern
 * @version 1.0
 */
public abstract class LookUpTable8 extends LookUpTable {

	/**
	 * Maximum output value of the LUT
	 */
	protected final byte dwMaxOutput; // Maximum output value of the LUT
	/**
	 * The lut values.
	 */
	protected final byte[] lut;

	protected LookUpTable8 (
			final int dwNumInput , // Number of i nput values in created
			// LUT
			final byte dwMaxOutput // Maximum output value of the LUT
	) {
		super ( null , dwNumInput );
		this.lut = new byte[ dwNumInput ];
		this.dwMaxOutput = dwMaxOutput;
	}

	/**
	 * Create the string representation of a 16 bit lut.
	 *
	 * @return the lut as a String
	 */
	protected LookUpTable8 (
			final ICCCurveType curve , // Pointer to the curve data
			final int dwNumInput , // Number of input values in created LUT
			final byte dwMaxOutput // Maximum output value of the LUT
	) {
		super ( curve , dwNumInput );
		this.dwMaxOutput = dwMaxOutput;
		this.lut = new byte[ dwNumInput ];
	}

	/**
	 * Create an abbreviated string representation of a 16 bit lut.
	 *
	 * @return the lut as a String
	 */
	@Override
	public String toString ( ) {
		final String rep = "[LookUpTable8 " + "max= " + this.dwMaxOutput +
				", nentries= " + this.dwMaxOutput +
				"]";
		return rep;
	}

	public String toStringWholeLut ( ) {
		final StringBuffer rep = new StringBuffer ( "LookUpTable8" + LookUpTable.eol );
		rep.append ( "maxOutput = " + this.dwMaxOutput + LookUpTable.eol );
		for ( int i = 0 ; i < this.dwNumInput ; ++ i )
			rep.append ( "lut[" + i + "] = " + this.lut[ i ] + LookUpTable.eol );
		return rep.append ( "]" ).toString ( );
	}

	/**
	 * lut accessor
	 *
	 * @param index of the element
	 * @return the lut [index]
	 */
	public final byte elementAt ( final int index ) {
		return this.lut[ index ];
	}

	/* end class LookUpTable8 */
}
