/*****************************************************************************
 *
 * $Id: LookUpTable.java,v 1.1 2002/07/25 14:56:49 grosbois Exp $
 *
 * Copyright Eastman Kodak Company, 343 State Street, Rochester, NY 14650
 * $Date $
 ****************************************************************************/

package dev.zontreck.harbinger.thirdparty.jj2000.j2k.icc.lut;

import dev.zontreck.harbinger.thirdparty.jj2000.j2k.icc.tags.ICCCurveType;

/**
 * Toplevel class for a lut. All lookup tables must extend this class.
 *
 * @author Bruce A. Kern
 * @version 1.0
 */
public abstract class LookUpTable {

	/**
	 * End of line string.
	 */
	protected static final String eol = System.getProperty ( "line.separator" );
	/**
	 * The curve data
	 */
	protected ICCCurveType curve;
	/**
	 * Number of values in created lut
	 */
	protected int dwNumInput;

	/**
	 * For subclass usage.
	 *
	 * @param curve      The curve data
	 * @param dwNumInput Number of values in created lut
	 */
	protected LookUpTable ( final ICCCurveType curve , final int dwNumInput ) {
		this.curve = curve;
		this.dwNumInput = dwNumInput;
	}

	/* end class LookUpTable */
}
