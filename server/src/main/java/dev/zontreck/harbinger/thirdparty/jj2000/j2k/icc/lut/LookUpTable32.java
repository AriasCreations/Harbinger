/*****************************************************************************
 *
 * $Id: LookUpTable32.java,v 1.1 2002/07/25 14:56:47 grosbois Exp $
 *
 * Copyright Eastman Kodak Company, 343 State Street, Rochester, NY 14650
 * $Date $
 ****************************************************************************/

package dev.zontreck.harbinger.thirdparty.jj2000.j2k.icc.lut;

import dev.zontreck.harbinger.thirdparty.jj2000.j2k.icc.tags.ICCCurveType;

/**
 * Toplevel class for a int [] lut.
 * 
 * @version 1.0
 * @author Bruce A. Kern
 */
abstract class LookUpTable32 extends LookUpTable
{

	/** Maximum output value of the LUT */
	protected final int dwMaxOutput;
	/** the lut values. */
	public final int[] lut;

	/**
	 * Create an abbreviated string representation of a 16 bit lut.
	 * 
	 * @return the lut as a String
	 */
	@Override
	public String toString()
	{
		final String rep = "[LookUpTable32 " + "max= " + this.dwMaxOutput +
				", nentries= " + this.dwNumInput +
				"]";
		return rep;
	}

	/**
	 * Create the string representation of a 32 bit lut.
	 * 
	 * @return the lut as a String
	 */
	public String toStringWholeLut()
	{
		final StringBuffer rep = new StringBuffer("[LookUpTable32" + LookUpTable.eol);
		int row, col;
		rep.append("max output = " + this.dwMaxOutput + LookUpTable.eol);
		for (row = 0; row < this.dwNumInput / 10; ++row)
		{
			rep.append("lut[" + 10 * row + "] : ");
			for (col = 0; 10 > col; ++col)
			{
				rep.append(this.lut[10 * row + col] + " ");
			}
			rep.append(LookUpTable.eol);
		}
		// Partial row.
		rep.append("lut[" + 10 * row + "] : ");
		for (col = 0; col < this.dwNumInput % 10; ++col)
			rep.append(this.lut[10 * row + col] + " ");
		rep.append(LookUpTable.eol + LookUpTable.eol);
		return rep.toString();
	}

	/**
	 * Factory method for getting a 32 bit lut from a given curve.
	 * 
	 * @param curve
	 *            the data
	 * @param dwNumInput
	 *            the size of the lut
	 * @param dwMaxOutput
	 *            max output value of the lut
	 * @return the lookup table
	 */
	public static LookUpTable32 createInstance(final ICCCurveType curve, // Pointer to
											   // the curve
											   // data
											   final int dwNumInput, // Number of input values in created LUT
											   final int dwMaxOutput // Maximum output value of the LUT
	)
	{
		if (1 == curve.count)
			return new LookUpTable32Gamma(curve, dwNumInput, dwMaxOutput);
		return new LookUpTable32Interp(curve, dwNumInput, dwMaxOutput);
	}

	/**
	 * Construct an empty 32 bit
	 * 
	 * @param dwNumInput
	 *            the size of the lut t lut.
	 * @param dwMaxOutput
	 *            max output value of the lut
	 */
	protected LookUpTable32(final int dwNumInput, // Number of i nput values in
							// created LUT
							final int dwMaxOutput // Maximum output value of the LUT
	)
	{
		super(null, dwNumInput);
		this.lut = new int[dwNumInput];
		this.dwMaxOutput = dwMaxOutput;
	}

	/**
	 * Construct a 16 bit lut from a given curve.
	 * 
	 * @param curve
	 *            the data
	 * @param dwNumInput
	 *            the size of the lut t lut.
	 * @param dwMaxOutput
	 *            max output value of the lut
	 */
	protected LookUpTable32(final ICCCurveType curve, // Pointer to the curve data
							final int dwNumInput, // Number of input values in created LUT
							final int dwMaxOutput // Maximum output value of the LUT
	)
	{
		super(curve, dwNumInput);
		this.dwMaxOutput = dwMaxOutput;
		this.lut = new int[dwNumInput];
	}

	/**
	 * lut accessor
	 * 
	 * @param index
	 *            of the element
	 * @return the lut [index]
	 */
	public final int elementAt(final int index)
	{
		return this.lut[index];
	}

	/* end class LookUpTable32 */
}
