/*****************************************************************************
 *
 * $Id: MonochromeTransformTosRGB.java,v 1.1 2002/07/25 14:56:50 grosbois Exp $
 *
 * Copyright Eastman Kodak Company, 343 State Street, Rochester, NY 14650
 * $Date $
 ****************************************************************************/

package icc.lut;

import colorspace.ColorSpace;
import icc.ICCProfile;
import icc.RestrictedICCProfile;
import jj2000.j2k.image.DataBlkInt;
import jj2000.j2k.image.DataBlkFloat;

/**
 * 
 * This class constructs a LookUpTableFP from a RestrictedICCProfile. The values
 * in this table are used to calculate a second lookup table (simply a short
 * []). table. When this transform is applied to an input DataBlk, an output
 * data block is constructed by using the input samples as indices into the
 * lookup table, whose values are used to populate the output DataBlk.
 * 
 * @see icc.RestrictedICCProfile
 * @see icc.lut.LookUpTableFP
 * @version 1.0
 * @author Bruce A. Kern
 */

public class MonochromeTransformTosRGB
{

	private static final String eol = System.getProperty("line.separator");

	/** Transform parameter. */
	public static final double ksRGBShadowCutoff = 0.0031308;
	/** Transform parameter. */
	public static final double ksRGBShadowSlope = 12.92;
	/** Transform parameter. */
	public static final double ksRGB8ShadowSlope = (255 * MonochromeTransformTosRGB.ksRGBShadowSlope);
	/** Transform parameter. */
	public static final double ksRGBExponent = (1.0 / 2.4);
	/** Transform parameter. */
	public static final double ksRGB8ScaleAfterExp = 269.025;
	/** Transform parameter. */
	public static final double ksRGB8ReduceAfterExp = 14.025;

	private short[] lut;
	private int dwInputMaxValue;
	private LookUpTableFP fLut;

	/**
	 * String representation of class
	 * 
	 * @return suitable representation for class
	 */
	@Override
	public String toString()
	{
		final StringBuffer rep = new StringBuffer("[MonochromeTransformTosRGB ");
		final StringBuffer body = new StringBuffer("  ");

		// Print the parameters:
		body.append(MonochromeTransformTosRGB.eol).append("ksRGBShadowSlope= ").append(MonochromeTransformTosRGB.ksRGBShadowSlope);
		body.append(MonochromeTransformTosRGB.eol).append("ksRGBShadowCutoff= ").append(MonochromeTransformTosRGB.ksRGBShadowCutoff);
		body.append(MonochromeTransformTosRGB.eol).append("ksRGBShadowSlope= ").append(MonochromeTransformTosRGB.ksRGBShadowSlope);
		body.append(MonochromeTransformTosRGB.eol).append("ksRGB8ShadowSlope= ").append(MonochromeTransformTosRGB.ksRGB8ShadowSlope);
		body.append(MonochromeTransformTosRGB.eol).append("ksRGBExponent= ").append(MonochromeTransformTosRGB.ksRGBExponent);
		body.append(MonochromeTransformTosRGB.eol).append("ksRGB8ScaleAfterExp= ").append(MonochromeTransformTosRGB.ksRGB8ScaleAfterExp);
		body.append(MonochromeTransformTosRGB.eol).append("ksRGB8ReduceAfterExp= ").append(MonochromeTransformTosRGB.ksRGB8ReduceAfterExp);
		body.append(MonochromeTransformTosRGB.eol).append("dwInputMaxValue= ").append(this.dwInputMaxValue);

		// Print the LinearSRGBtoSRGB lut.
		body.append(MonochromeTransformTosRGB.eol).append("[lut = [short[" + this.lut.length + "]]]");

		// Print the FP luts.
		body.append(MonochromeTransformTosRGB.eol).append("fLut=  " + this.fLut.toString());

		rep.append(ColorSpace.indent("  ", body));
		return rep.append("]").toString();
	}

	/**
	 * Construct the lut from the RestrictedICCProfile.
	 * 
	 * @param ricc
	 *            input RestrictedICCProfile
	 * @param dwInputMaxValue
	 *            size of the output lut.
	 * @param dwInputShiftValue
	 *            value used to shift samples to positive
	 */
	public MonochromeTransformTosRGB(final RestrictedICCProfile ricc, final int dwInputMaxValue, final int dwInputShiftValue)
	{

		if (RestrictedICCProfile.kMonochromeInput != ricc.getType())
			throw new IllegalArgumentException("MonochromeTransformTosRGB: wrong type ICCProfile supplied");

		this.dwInputMaxValue = dwInputMaxValue;
		this.lut = new short[dwInputMaxValue + 1];
		this.fLut = LookUpTableFP.createInstance(ricc.trc[ICCProfile.GRAY], dwInputMaxValue + 1);

		// First calculate the value for the shadow region
		int i;
		for (i = 0; ((i <= dwInputMaxValue) && (ksRGBShadowCutoff >= fLut.lut[i])); i++)
			this.lut[i] = (short) (Math.floor(MonochromeTransformTosRGB.ksRGB8ShadowSlope * this.fLut.lut[i] + 0.5) - dwInputShiftValue);

		// Now calculate the rest
		for (; i <= dwInputMaxValue; i++)
			this.lut[i] = (short) (Math.floor(MonochromeTransformTosRGB.ksRGB8ScaleAfterExp * Math.pow(this.fLut.lut[i], MonochromeTransformTosRGB.ksRGBExponent)
					- MonochromeTransformTosRGB.ksRGB8ReduceAfterExp + 0.5) - dwInputShiftValue);
	}

	/**
	 * Populate the output block by looking up the values in the lut, using the
	 * input as lut indices.
	 * 
	 * @param inb
	 *            input samples
	 * @param outb
	 *            output samples.
	 * @exception MonochromeTransformException
	 */
	public void apply(final DataBlkInt inb, final DataBlkInt outb) throws MonochromeTransformException
	{

		int i, j;

		final int[] in = (int[]) inb.getData();
		int[] out = (int[]) outb.getData();

		if (null == out || out.length < in.length)
		{
			out = new int[in.length];
			outb.setData(out);
		}

		outb.uly = inb.uly;
		outb.ulx = inb.ulx;
		outb.h = inb.h;
		outb.w = inb.w;
		outb.offset = inb.offset;
		outb.scanw = inb.scanw;

		for (i = 0; i < inb.h * inb.w; ++i)
		{
			j = in[i];
			if (0 > j)
				j = 0;
			else if (j > this.dwInputMaxValue)
				j = this.dwInputMaxValue;
			out[i] = this.lut[j];
		}
	}

	/**
	 * Populate the output block by looking up the values in the lut, using the
	 * input as lut indices.
	 * 
	 * @param inb
	 *            input samples
	 * @param outb
	 *            output samples.
	 * @exception MonochromeTransformException
	 */
	public void apply(final DataBlkFloat inb, final DataBlkFloat outb) throws MonochromeTransformException
	{

		int i, j;

		final float[] in = (float[]) inb.getData();
		float[] out = (float[]) outb.getData();

		if (null == out || out.length < in.length)
		{
			out = new float[in.length];
			outb.setData(out);

			outb.uly = inb.uly;
			outb.ulx = inb.ulx;
			outb.h = inb.h;
			outb.w = inb.w;
			outb.offset = inb.offset;
			outb.scanw = inb.scanw;
		}

		for (i = 0; i < inb.h * inb.w; ++i)
		{
			j = (int) in[i];
			if (0 > j)
				j = 0;
			else if (j > this.dwInputMaxValue)
				j = this.dwInputMaxValue;
			out[i] = this.lut[j];
		}
	}

	/* end class MonochromeTransformTosRGB */
}
