/*****************************************************************************
 *
 * $Id: MatrixBasedTransformTosRGB.java,v 1.1 2002/07/25 14:56:49 grosbois Exp $
 *
 * Copyright Eastman Kodak Company, 343 State Street, Rochester, NY 14650
 * $Date $
 ****************************************************************************/

package icc.lut;

import colorspace.ColorSpace;
import icc.ICCProfile;
import icc.RestrictedICCProfile;
import icc.tags.ICCXYZType;
import jj2000.j2k.image.DataBlkInt;
import jj2000.j2k.image.DataBlkFloat;

/**
 * Transform for applying ICCProfiling to an input DataBlk
 * 
 * @see jj2000.j2k.image.DataBlkInt
 * @see jj2000.j2k.image.DataBlkFloat
 * @version 1.0
 * @author Bruce A. Kern
 */
public class MatrixBasedTransformTosRGB
{

	private static final String eol = System.getProperty("line.separator");

	// Start of contant definitions:

	private static final int // Convenience
			RED = ICCProfile.RED,
			GREEN = ICCProfile.GREEN, BLUE = ICCProfile.BLUE;

	private static final double // Define the PCS to linear sRGB matrix
								// coefficients
			SRGB00 = 3.1337,
			SRGB01 = -1.6173, SRGB02 = -0.4907, SRGB10 = -0.9785, SRGB11 = 1.9162,
			SRGB12 = 0.0334,
			SRGB20 = 0.0720,
			SRGB21 = -0.2290, SRGB22 = 1.4056;

	// Define constants representing the indices into the matrix array
	private static final int M00 = 0;
	private static final int M01 = 1;
	private static final int M02 = 2;
	private static final int M10 = 3;
	private static final int M11 = 4;
	private static final int M12 = 5;
	private static final int M20 = 6;
	private static final int M21 = 7;
	private static final int M22 = 8;

	private static final double ksRGBExponent = (1.0 / 2.4);
	private static final double ksRGBScaleAfterExp = 1.055;
	private static final double ksRGBReduceAfterExp = 0.055;
	private static final double ksRGBShadowCutoff = 0.0031308;
	private static final double ksRGBShadowSlope = 12.92;

	// End of contant definitions:

	private final double[] matrix; // Matrix coefficients

	private final LookUpTableFP[] fLut = new LookUpTableFP[3];
	private final LookUpTable32LinearSRGBtoSRGB lut; // Linear sRGB to sRGB LUT

	private final int[] dwMaxValue;
	private final int[] dwShiftValue;

	private float[][] fBuf; // Intermediate output of the first LUT
									// operation.

	/**
	 * String representation of class
	 * 
	 * @return suitable representation for class
	 */
	@Override
	public String toString()
	{
		int i, j;

		final StringBuffer rep = new StringBuffer("[MatrixBasedTransformTosRGB: ");

		final StringBuffer body = new StringBuffer("  ");
		body.append(MatrixBasedTransformTosRGB.eol).append("ksRGBExponent= ").append(MatrixBasedTransformTosRGB.ksRGBExponent);
		body.append(MatrixBasedTransformTosRGB.eol).append("ksRGBScaleAfterExp= ").append(MatrixBasedTransformTosRGB.ksRGBScaleAfterExp);
		body.append(MatrixBasedTransformTosRGB.eol).append("ksRGBReduceAfterExp= ").append(MatrixBasedTransformTosRGB.ksRGBReduceAfterExp);

		body.append(MatrixBasedTransformTosRGB.eol).append("dwMaxValues= ").append(this.dwMaxValue[0]).append(", ")
				.append(this.dwMaxValue[1]).append(", ").append(this.dwMaxValue[2]);

		body.append(MatrixBasedTransformTosRGB.eol).append("dwShiftValues= ").append(this.dwShiftValue[0]).append(", ")
				.append(this.dwShiftValue[1]).append(", ").append(this.dwShiftValue[2]);

		body.append(MatrixBasedTransformTosRGB.eol).append(MatrixBasedTransformTosRGB.eol).append("fLut= ").append(MatrixBasedTransformTosRGB.eol)
				.append(ColorSpace.indent("  ", "fLut[RED]=  " + this.fLut[0].toString())).append(MatrixBasedTransformTosRGB.eol)
				.append(ColorSpace.indent("  ", "fLut[GRN]=  " + this.fLut[1].toString())).append(MatrixBasedTransformTosRGB.eol)
				.append(ColorSpace.indent("  ", "fLut[BLU]=  " + this.fLut[2].toString()));

		// Print the matrix
		body.append(MatrixBasedTransformTosRGB.eol).append(MatrixBasedTransformTosRGB.eol).append("[matrix ");
		for (i = 0; 3 > i; ++i)
		{
			body.append(MatrixBasedTransformTosRGB.eol).append("  ");
			for (j = 0; 3 > j; ++j)
			{
				body.append(this.matrix[3 * i + j] + "   ");
			}
		}
		body.append("]");

		// Print the LinearSRGBtoSRGB lut.
		body.append(MatrixBasedTransformTosRGB.eol).append(MatrixBasedTransformTosRGB.eol).append(this.lut.toString());

		rep.append(ColorSpace.indent("  ", body)).append("]");
		return rep.append("]").toString();
	}

	/**
	 * Construct a 3 component transform based on an input RestricedICCProfile
	 * This transform will pass the input throught a floating point lut
	 * (LookUpTableFP), apply a matrix to the output and finally pass the
	 * intermediate buffer through a 8-bit lut (LookUpTable8). This operation
	 * will be designated (LFP*M*L8) * Data The operators (LFP*M*L8) are
	 * constructed here. Although the data for only one component is returned,
	 * the transformation must be done for all components, because the matrix
	 * application involves a linear combination of component input to produce
	 * the output.
	 * 
	 * @param ricc
	 *            input profile
	 * @param dwMaxValue
	 *            clipping value for output.
	 */
	public MatrixBasedTransformTosRGB(final RestrictedICCProfile ricc, final int[] dwMaxValue, final int[] dwShiftValue)
	{

		// Assure the proper type profile for this xform.
		if (RestrictedICCProfile.kThreeCompInput != ricc.getType())
			throw new IllegalArgumentException("MatrixBasedTransformTosRGB: wrong type ICCProfile supplied");

		int c; // component index.
		this.dwMaxValue = dwMaxValue;
		this.dwShiftValue = dwShiftValue;

		// Create the LUTFP from the input profile.
		for (c = 0; 3 > c; ++c)
		{
			this.fLut[c] = LookUpTableFP.createInstance(ricc.trc[c], dwMaxValue[c] + 1);
		}

		// Create the Input linear to PCS matrix
		this.matrix = this.createMatrix(ricc, dwMaxValue); // Create and matrix from the
													// ICC profile.

		// Create the final LUT32
		this.lut = LookUpTable32LinearSRGBtoSRGB.createInstance(dwMaxValue[0], dwMaxValue[0], MatrixBasedTransformTosRGB.ksRGBShadowCutoff,
				MatrixBasedTransformTosRGB.ksRGBShadowSlope, MatrixBasedTransformTosRGB.ksRGBScaleAfterExp, MatrixBasedTransformTosRGB.ksRGBExponent, MatrixBasedTransformTosRGB.ksRGBReduceAfterExp);
	}

	private double[] createMatrix(final RestrictedICCProfile ricc, final int[] maxValues)
	{

		// Coefficients from the input linear to PCS matrix
		final double dfPCS00 = ICCXYZType.XYZToDouble(ricc.colorant[MatrixBasedTransformTosRGB.RED].x);
		final double dfPCS01 = ICCXYZType.XYZToDouble(ricc.colorant[MatrixBasedTransformTosRGB.GREEN].x);
		final double dfPCS02 = ICCXYZType.XYZToDouble(ricc.colorant[MatrixBasedTransformTosRGB.BLUE].x);
		final double dfPCS10 = ICCXYZType.XYZToDouble(ricc.colorant[MatrixBasedTransformTosRGB.RED].y);
		final double dfPCS11 = ICCXYZType.XYZToDouble(ricc.colorant[MatrixBasedTransformTosRGB.GREEN].y);
		final double dfPCS12 = ICCXYZType.XYZToDouble(ricc.colorant[MatrixBasedTransformTosRGB.BLUE].y);
		final double dfPCS20 = ICCXYZType.XYZToDouble(ricc.colorant[MatrixBasedTransformTosRGB.RED].z);
		final double dfPCS21 = ICCXYZType.XYZToDouble(ricc.colorant[MatrixBasedTransformTosRGB.GREEN].z);
		final double dfPCS22 = ICCXYZType.XYZToDouble(ricc.colorant[MatrixBasedTransformTosRGB.BLUE].z);

		final double[] matrix = new double[9];
		matrix[MatrixBasedTransformTosRGB.M00] = maxValues[0] * (MatrixBasedTransformTosRGB.SRGB00 * dfPCS00 + MatrixBasedTransformTosRGB.SRGB01 * dfPCS10 + MatrixBasedTransformTosRGB.SRGB02 * dfPCS20);
		matrix[MatrixBasedTransformTosRGB.M01] = maxValues[0] * (MatrixBasedTransformTosRGB.SRGB00 * dfPCS01 + MatrixBasedTransformTosRGB.SRGB01 * dfPCS11 + MatrixBasedTransformTosRGB.SRGB02 * dfPCS21);
		matrix[MatrixBasedTransformTosRGB.M02] = maxValues[0] * (MatrixBasedTransformTosRGB.SRGB00 * dfPCS02 + MatrixBasedTransformTosRGB.SRGB01 * dfPCS12 + MatrixBasedTransformTosRGB.SRGB02 * dfPCS22);
		matrix[MatrixBasedTransformTosRGB.M10] = maxValues[1] * (MatrixBasedTransformTosRGB.SRGB10 * dfPCS00 + MatrixBasedTransformTosRGB.SRGB11 * dfPCS10 + MatrixBasedTransformTosRGB.SRGB12 * dfPCS20);
		matrix[MatrixBasedTransformTosRGB.M11] = maxValues[1] * (MatrixBasedTransformTosRGB.SRGB10 * dfPCS01 + MatrixBasedTransformTosRGB.SRGB11 * dfPCS11 + MatrixBasedTransformTosRGB.SRGB12 * dfPCS21);
		matrix[MatrixBasedTransformTosRGB.M12] = maxValues[1] * (MatrixBasedTransformTosRGB.SRGB10 * dfPCS02 + MatrixBasedTransformTosRGB.SRGB11 * dfPCS12 + MatrixBasedTransformTosRGB.SRGB12 * dfPCS22);
		matrix[MatrixBasedTransformTosRGB.M20] = maxValues[2] * (MatrixBasedTransformTosRGB.SRGB20 * dfPCS00 + MatrixBasedTransformTosRGB.SRGB21 * dfPCS10 + MatrixBasedTransformTosRGB.SRGB22 * dfPCS20);
		matrix[MatrixBasedTransformTosRGB.M21] = maxValues[2] * (MatrixBasedTransformTosRGB.SRGB20 * dfPCS01 + MatrixBasedTransformTosRGB.SRGB21 * dfPCS11 + MatrixBasedTransformTosRGB.SRGB22 * dfPCS21);
		matrix[MatrixBasedTransformTosRGB.M22] = maxValues[2] * (MatrixBasedTransformTosRGB.SRGB20 * dfPCS02 + MatrixBasedTransformTosRGB.SRGB21 * dfPCS12 + MatrixBasedTransformTosRGB.SRGB22 * dfPCS22);

		return matrix;
	}

	/**
	 * Performs the transform. Pass the input throught the LookUpTableFP, apply
	 * the matrix to the output and finally pass the intermediate buffer through
	 * the LookUpTable8. This operation is designated (LFP*M*L8) * Data are
	 * already constructed. Although the data for only one component is
	 * returned, the transformation must be done for all components, because the
	 * matrix application involves a linear combination of component input to
	 * produce the output.
	 *
	 * @param inb
	 *            input data block
	 * @param outb
	 *            output data block
	 * @exception MatrixBasedTransformException
	 */
	public void apply(final DataBlkInt[] inb, final DataBlkInt[] outb) throws MatrixBasedTransformException
	{
		final int[][] in = new int[3][]; // data references.
		final int[][] out = new int[3][];

		final int nrows = inb[0].h;
		final int ncols = inb[0].w;

		if ((null == fBuf) || (this.fBuf[0].length < ncols * nrows))
		{
			this.fBuf = new float[3][ncols * nrows];
		}

		// for each component (rgb)
		for (int c = 0; 3 > c; ++c)
		{

			// Reference the input and output samples.
			in[c] = (int[]) inb[c].getData();
			out[c] = (int[]) outb[c].getData();

			// Assure a properly sized output buffer.
			if (null == out[c] || out[c].length < in[c].length)
			{
				out[c] = new int[in[c].length];
				outb[c].setData(out[c]);
			}

			// The first thing to do is to process the input into a standard
			// form
			// and through the first input LUT, producing floating point output
			// values
			MatrixBasedTransformTosRGB.standardizeMatrixLineThroughLut(inb[c], this.fBuf[c], this.dwMaxValue[c], this.fLut[c]);
		}

		// For each row and column
		final float[] ra = this.fBuf[MatrixBasedTransformTosRGB.RED];
		final float[] ga = this.fBuf[MatrixBasedTransformTosRGB.GREEN];
		final float[] ba = this.fBuf[MatrixBasedTransformTosRGB.BLUE];

		final int[] ro = out[MatrixBasedTransformTosRGB.RED];
		final int[] go = out[MatrixBasedTransformTosRGB.GREEN];
		final int[] bo = out[MatrixBasedTransformTosRGB.BLUE];
		final int[] lut32 = this.lut.lut;

		double r, g, b;
		int val, index = 0;
		for (int y = 0; y < inb[0].h; ++y)
		{
			final int end = index + inb[0].w;
			while (index < end)
			{
				// Calculate the rgb pixel indices for this row / column
				r = ra[index];
				g = ga[index];
				b = ba[index];

				// Apply the matrix to the intermediate floating point data in
				// order to index the
				// final LUT.
				val = (int) (this.matrix[MatrixBasedTransformTosRGB.M00] * r + this.matrix[MatrixBasedTransformTosRGB.M01] * g + this.matrix[MatrixBasedTransformTosRGB.M02] * b + 0.5);
				// Clip the calculated value if necessary..
				if (0 > val)
					ro[index] = lut32[0];
				else if (val >= lut32.length)
					ro[index] = lut32[lut32.length - 1];
				else
					ro[index] = lut32[val];

				val = (int) (this.matrix[MatrixBasedTransformTosRGB.M10] * r + this.matrix[MatrixBasedTransformTosRGB.M11] * g + this.matrix[MatrixBasedTransformTosRGB.M12] * b + 0.5);
				// Clip the calculated value if necessary..
				if (0 > val)
					go[index] = lut32[0];
				else if (val >= lut32.length)
					go[index] = lut32[lut32.length - 1];
				else
					go[index] = lut32[val];

				val = (int) (this.matrix[MatrixBasedTransformTosRGB.M20] * r + this.matrix[MatrixBasedTransformTosRGB.M21] * g + this.matrix[MatrixBasedTransformTosRGB.M22] * b + 0.5);
				// Clip the calculated value if necessary..
				if (0 > val)
					bo[index] = lut32[0];
				else if (val >= lut32.length)
					bo[index] = lut32[lut32.length - 1];
				else
					bo[index] = lut32[val];

				index++;
			}
		}
	}

	/**
	 * Performs the transform. Pass the input throught the LookUpTableFP, apply
	 * the matrix to the output and finally pass the intermediate buffer through
	 * the LookUpTable8. This operation is designated (LFP*M*L8) * Data are
	 * already constructed. Although the data for only one component is
	 * returned, the transformation must be done for all components, because the
	 * matrix application involves a linear combination of component input to
	 * produce the output.
	 *
	 * @param inb
	 *            input data block
	 * @param outb
	 *            output data block
	 * @exception MatrixBasedTransformException
	 */
	public void apply(final DataBlkFloat[] inb, final DataBlkFloat[] outb) throws MatrixBasedTransformException
	{

		final float[][] in = new float[3][]; // data references.
		final float[][] out = new float[3][];

		final int nrows = inb[0].h;
		final int ncols = inb[0].w;

		if ((null == fBuf) || (this.fBuf[0].length < ncols * nrows))
		{
			this.fBuf = new float[3][ncols * nrows];
		}

		// for each component (rgb)
		for (int c = 0; 3 > c; ++c)
		{

			// Reference the input and output pixels.
			in[c] = (float[]) inb[c].getData();
			out[c] = (float[]) outb[c].getData();

			// Assure a properly sized output buffer.
			if (null == out[c] || out[c].length < in[c].length)
			{
				out[c] = new float[in[c].length];
				outb[c].setData(out[c]);
			}

			// The first thing to do is to process the input into a standard
			// form
			// and through the first input LUT, producing floating point output
			// values
			MatrixBasedTransformTosRGB.standardizeMatrixLineThroughLut(inb[c], this.fBuf[c], this.dwMaxValue[c], this.fLut[c]);
		}

		final int[] lut32 = this.lut.lut;

		// For each row and column
		int index = 0, val;
		for (int y = 0; y < inb[0].h; ++y)
		{
			final int end = index + inb[0].w;
			while (index < end)
			{
				// Calculate the rgb pixel indices for this row / column

				// Apply the matrix to the intermediate floating point data
				// inorder to index the
				// final LUT.
				val = (int) (this.matrix[MatrixBasedTransformTosRGB.M00] * this.fBuf[MatrixBasedTransformTosRGB.RED][index] + this.matrix[MatrixBasedTransformTosRGB.M01] * this.fBuf[MatrixBasedTransformTosRGB.GREEN][index] + this.matrix[MatrixBasedTransformTosRGB.M02]
						* this.fBuf[MatrixBasedTransformTosRGB.BLUE][index] + 0.5);
				// Clip the calculated value if necessary..
				if (0 > val)
					out[0][index] = lut32[0];
				else if (val >= lut32.length)
					out[0][index] = lut32[lut32.length - 1];
				else
					out[0][index] = lut32[val];

				val = (int) (this.matrix[MatrixBasedTransformTosRGB.M10] * this.fBuf[MatrixBasedTransformTosRGB.RED][index] + this.matrix[MatrixBasedTransformTosRGB.M11] * this.fBuf[MatrixBasedTransformTosRGB.GREEN][index] + this.matrix[MatrixBasedTransformTosRGB.M12]
						* this.fBuf[MatrixBasedTransformTosRGB.BLUE][index] + 0.5);
				// Clip the calculated value if necessary..
				if (0 > val)
					out[1][index] = lut32[0];
				else if (val >= lut32.length)
					out[1][index] = lut32[lut32.length - 1];
				else
					out[1][index] = lut32[val];

				val = (int) (this.matrix[MatrixBasedTransformTosRGB.M20] * this.fBuf[MatrixBasedTransformTosRGB.RED][index] + this.matrix[MatrixBasedTransformTosRGB.M21] * this.fBuf[MatrixBasedTransformTosRGB.GREEN][index] + this.matrix[MatrixBasedTransformTosRGB.M22]
						* this.fBuf[MatrixBasedTransformTosRGB.BLUE][index] + 0.5);
				// Clip the calculated value if necessary..
				if (0 > val)
					out[2][index] = lut32[0];
				else if (val >= lut32.length)
					out[2][index] = lut32[lut32.length - 1];
				else
					out[2][index] = lut32[val];

				index++;
			}
		}
	}

	private static void standardizeMatrixLineThroughLut(final DataBlkInt inb, // input
														// datablock
														final float[] out, // output data reference
														final int dwInputMaxValue, // Maximum value of the input for clipping
														final LookUpTableFP lut // Inital input LUT
	)
	{
		int wTemp, j = 0;
		final int[] in = (int[]) inb.getData(); // input pixel reference
		final float[] lutFP = lut.lut;
		for (int y = inb.uly; y < inb.uly + inb.h; ++y)
		{
			for (int x = inb.ulx; x < inb.ulx + inb.w; ++x)
			{
				final int i = inb.offset + (y - inb.uly) * inb.scanw + (x - inb.ulx); // pixel
																				// index.
				if (in[i] > dwInputMaxValue)
					wTemp = dwInputMaxValue;
				else if (0 > in[i])
					wTemp = 0;
				else
					wTemp = in[i];
				out[j] = lutFP[wTemp];
				j++;
			}
		}
	}

	private static void standardizeMatrixLineThroughLut(final DataBlkFloat inb, // input
														// datablock
														final float[] out, // output data reference
														final float dwInputMaxValue, // Maximum value of the input for clipping
														final LookUpTableFP lut // Inital input LUT
	)
	{
		int j = 0;
		float wTemp;
		final float[] in = (float[]) inb.getData(); // input pixel reference
		final float[] lutFP = lut.lut;

		for (int y = inb.uly; y < inb.uly + inb.h; ++y)
		{
			for (int x = inb.ulx; x < inb.ulx + inb.w; ++x)
			{
				final int i = inb.offset + (y - inb.uly) * inb.scanw + (x - inb.ulx); // pixel
																				// index.
				if (in[i] > dwInputMaxValue)
					wTemp = dwInputMaxValue;
				else if (0 > in[i])
					wTemp = 0;
				else
					wTemp = in[i];
				out[j] = lutFP[(int) wTemp];
				j++;
			}
		}
	}

	/* end class MatrixBasedTransformTosRGB */
}
