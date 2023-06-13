/*****************************************************************************
 *
 * $Id: LookUpTable16.java,v 1.1 2002/07/25 14:56:47 grosbois Exp $
 *
 * Copyright Eastman Kodak Company, 343 State Street, Rochester, NY 14650
 * $Date $
 ****************************************************************************/

package icc.lut;

import icc.tags.ICCCurveType;

/**
 * Toplevel class for a short [] lut.
 * 
 * @version 1.0
 * @author Bruce A. Kern
 */
public abstract class LookUpTable16 extends LookUpTable
{

	/** Maximum output value of the LUT */
	protected final int dwMaxOutput;
	/** The lut values. */
	protected final short[] lut;

	/**
	 * Create an abbreviated string representation of a 16 bit lut.
	 * 
	 * @return the lut as a String
	 */
	@Override
	public String toString()
	{
		final String rep = "[LookUpTable16 " + "max= " + this.dwMaxOutput +
				", nentries= " + this.dwMaxOutput +
				"]";
		return rep;
	}

	/**
	 * Create a full string representation of a 16 bit lut.
	 * 
	 * @return the lut as a String
	 */
	public String toStringWholeLut()
	{
		final StringBuffer rep = new StringBuffer("[LookUpTable16" + LookUpTable.eol);
		int row, col;

		rep.append("max output = " + this.dwMaxOutput + LookUpTable.eol);
		for (row = 0; row < this.dwNumInput / 10; ++row)
		{
			rep.append("lut[" + 10 * row + "] : ");
			for (col = 0; 10 > col; ++col)
			{
				rep.append(this.lut[10 * row + col]).append(" ");
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
	 * Factory method for getting a 16 bit lut from a given curve.
	 * 
	 * @param curve
	 *            the data
	 * @param dwNumInput
	 *            the size of the lut
	 * @param dwMaxOutput
	 *            max output value of the lut
	 * @return the lookup table
	 */
	public static LookUpTable16 createInstance(final ICCCurveType curve, final int dwNumInput, final int dwMaxOutput)
	{

		if (1 == curve.count)
			return new LookUpTable16Gamma(curve, dwNumInput, dwMaxOutput);
		return new LookUpTable16Interp(curve, dwNumInput, dwMaxOutput);
	}

	/**
	 * Construct an empty 16 bit lut
	 * 
	 * @param dwNumInput
	 *            the size of the lut t lut.
	 * @param dwMaxOutput
	 *            max output value of the lut
	 */
	protected LookUpTable16(final int dwNumInput, final int dwMaxOutput)
	{

		super(null, dwNumInput);
		this.lut = new short[dwNumInput];
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
	protected LookUpTable16(final ICCCurveType curve, final int dwNumInput, final int dwMaxOutput)
	{

		super(curve, dwNumInput);
		this.dwMaxOutput = dwMaxOutput;
		this.lut = new short[dwNumInput];
	}

	/**
	 * lut accessor
	 * 
	 * @param index
	 *            of the element
	 * @return the lut [index]
	 */
	public final short elementAt(final int index)
	{
		return this.lut[index];
	}

	/* end class LookUpTable16 */
}
