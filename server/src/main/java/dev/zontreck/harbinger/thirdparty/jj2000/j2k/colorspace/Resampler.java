/*****************************************************************************
 *
 * $Id: Resampler.java,v 1.2 2002/08/08 14:07:31 grosbois Exp $
 *
 * Copyright Eastman Kodak Company, 343 State Street, Rochester, NY 14650
 * $Date $
 ****************************************************************************/
package dev.zontreck.harbinger.thirdparty.jj2000.j2k.colorspace;

import dev.zontreck.harbinger.thirdparty.jj2000.j2k.image.*;

/**
 * This class resamples the components of an image so that all have the same
 * number of samples. The current implementation only handles the case of 2:1
 * upsampling.
 * 
 * @see dev.zontreck.harbinger.thirdparty.jj2000.j2k.colorspace.ColorSpace
 * @version 1.0
 * @author Bruce A. Kern
 */
public class Resampler extends ColorSpaceMapper
{
	final int wspan = 0, hspan = 0;

	/**
	 * Factory method for creating instances of this class.
	 * 
	 * @param src
	 *            -- source of image data
	 * @param csMap
	 *            -- provides dev.zontreck.harbinger.thirdparty.jj2000.j2k.colorspace info
	 * @return Resampler instance
	 */
	public static BlkImgDataSrc createInstance(final BlkImgDataSrc src, final ColorSpace csMap) throws ColorSpaceException
	{
		return new Resampler(src, csMap);
	}

	/**
	 * Ctor resamples a BlkImgDataSrc so that all components have the same
	 * number of samples.
	 * 
	 * Note the present implementation does only two to one respampling in
	 * either direction (row, column).
	 * 
	 * @param src
	 *            -- Source of image data
	 * @param csMap
	 *            -- provides dev.zontreck.harbinger.thirdparty.jj2000.j2k.colorspace info
	 */
	protected Resampler(final BlkImgDataSrc src, final ColorSpace csMap) throws ColorSpaceException
	{
		super(src, csMap);

		int c;

		// Calculate the minimum and maximum subsampling factor
		// across all channels.

		int minX = src.getCompSubsX(0);
		int minY = src.getCompSubsY(0);
		int maxX = minX;
		int maxY = minY;

		for (c = 1; c < this.ncomps; ++c)
		{
			minX = Math.min(minX, src.getCompSubsX(c));
			minY = Math.min(minY, src.getCompSubsY(c));
			maxX = Math.max(maxX, src.getCompSubsX(c));
			maxY = Math.max(maxY, src.getCompSubsY(c));
		}

		// Throw an exception for other than 2:1 sampling.
		if ((1 != maxX && 2 != maxX) || (1 != maxY && 2 != maxY))
		{
			throw new ColorSpaceException("Upsampling by other than 2:1 not supported");
		}
		/* end Resampler ctor */
	}

	/**
	 * Return a DataBlk containing the requested component upsampled by the
	 * scale factor applied to the particular scaling direction
	 * 
	 * Returns, in the blk argument, a block of image data containing the
	 * specifed rectangular area, in the specified component. The data is
	 * returned, as a copy of the internal data, therefore the returned data can
	 * be modified "in place".
	 * 
	 * <P>
	 * The rectangular area to return is specified by the 'ulx', 'uly', 'w' and
	 * 'h' members of the 'blk' argument, relative to the current tile. These
	 * members are not modified by this method. The 'offset' of the returned
	 * data is 0, and the 'scanw' is the same as the block's width. See the
	 * 'DataBlk' class.
	 * 
	 * <P>
	 * If the data array in 'blk' is 'null', then a new one is created. If the
	 * data array is not 'null' then it is reused, and it must be large enough
	 * to contain the block's data. Otherwise an 'ArrayStoreException' or an
	 * 'IndexOutOfBoundsException' is thrown by the Java system.
	 * 
	 * <P>
	 * The returned data has its 'progressive' attribute set to that of the
	 * input data.
	 * 
	 * @param outblk
	 *            Its coordinates and dimensions specify the area to return. If
	 *            it contains a non-null data array, then it must have the
	 *            correct dimensions. If it contains a null data array a new one
	 *            is created. The fields in this object are modified to return
	 *            the data.
	 * 
	 * @param c
	 *            The index of the component from which to get the data. Only 0
	 *            and 3 are valid.
	 * 
	 * @return The requested DataBlk
	 * 
	 * @see #getCompData
	 */
	@Override
	public DataBlk getInternCompData(final DataBlk outblk, final int c)
	{

		// If the scaling factor of this channel is 1 in both
		// directions, simply return the source DataBlk.

		if (1 == src.getCompSubsX(c) && 1 == src.getCompSubsY(c))
			return this.src.getInternCompData(outblk, c);

		final int wfactor = this.src.getCompSubsX(c);
		final int hfactor = this.src.getCompSubsY(c);
		if ((2 != wfactor && 1 != wfactor) || (2 != hfactor && 1 != hfactor))
			throw new IllegalArgumentException("Upsampling by other than 2:1 not supported");

		int leftedgeOut = -1; // offset to the start of the output scanline
		int rightedgeOut = -1; // offset to the end of the output
		// scanline + 1
		int leftedgeIn = -1; // offset to the start of the input scanline

		final int y0In;
		int y1In;
		int y0Out;
		final int y1Out;
		final int x0In;
		int x1In;
		int x0Out;
		final int x1Out;

		y0Out = outblk.uly;
		y1Out = y0Out + outblk.h - 1;

		x0Out = outblk.ulx;
		x1Out = x0Out + outblk.w - 1;

		y0In = y0Out / hfactor;
		y1In = y1Out / hfactor;

		x0In = x0Out / wfactor;
		x1In = x1Out / wfactor;

		// Calculate the requested height and width, requesting an extra
		// row and or for upsampled channels.
		final int reqW = x1In - x0In + 1;
		final int reqH = y1In - y0In + 1;

		// Initialize general input and output indexes
		int kOut = -1;
		int kIn = -1;
		int yIn;

		switch (outblk.getDataType())
		{

			case DataBlk.TYPE_INT:

				DataBlkInt inblkInt = new DataBlkInt(x0In, y0In, reqW, reqH);
				inblkInt = (DataBlkInt) this.src.getInternCompData(inblkInt, c);
				this.dataInt[c] = inblkInt.getDataInt();

				// Reference the working array
				int[] outdataInt = (int[]) outblk.getData();

				// Create data array if necessary
				if (null == outdataInt || outdataInt.length != outblk.w * outblk.h)
				{
					outdataInt = new int[outblk.h * outblk.w];
					outblk.setData(outdataInt);
				}

				// The nitty-gritty.

				for (int yOut = y0Out; yOut <= y1Out; ++yOut)
				{

					yIn = yOut / hfactor;

					leftedgeIn = inblkInt.offset + (yIn - y0In) * inblkInt.scanw;
					leftedgeOut = outblk.offset + (yOut - y0Out) * outblk.scanw;
					rightedgeOut = leftedgeOut + outblk.w;

					kIn = leftedgeIn;
					kOut = leftedgeOut;

					if (1 == (x0Out & 0x1))
					{ // first is odd do the pixel once.
						outdataInt[kOut] = this.dataInt[c][kIn];
						kOut++;
						kIn++;
					}

					if (0 == (x1Out & 0x1))
					{ // last is even adjust loop bounds
						rightedgeOut--;
					}

					while (kOut < rightedgeOut)
					{
						outdataInt[kOut] = this.dataInt[c][kIn];
						kOut++;
						outdataInt[kOut] = this.dataInt[c][kIn];
						kOut++;
						kIn++;
					}

					if (0 == (x1Out & 0x1))
					{ // last is even do the pixel once.
						outdataInt[kOut] = this.dataInt[c][kIn];
						kOut++;
					}
				}

				outblk.progressive = inblkInt.progressive;
				break;

			case DataBlk.TYPE_FLOAT:

				DataBlkFloat inblkFloat = new DataBlkFloat(x0In, y0In, reqW, reqH);
				inblkFloat = (DataBlkFloat) this.src.getInternCompData(inblkFloat, c);
				this.dataFloat[c] = inblkFloat.getDataFloat();

				// Reference the working array
				float[] outdataFloat = (float[]) outblk.getData();

				// Create data array if necessary
				if (null == outdataFloat || outdataFloat.length != outblk.w * outblk.h)
				{
					outdataFloat = new float[outblk.h * outblk.w];
					outblk.setData(outdataFloat);
				}

				// The nitty-gritty.

				for (int yOut = y0Out; yOut <= y1Out; ++yOut)
				{

					yIn = yOut / hfactor;

					leftedgeIn = inblkFloat.offset + (yIn - y0In) * inblkFloat.scanw;
					leftedgeOut = outblk.offset + (yOut - y0Out) * outblk.scanw;
					rightedgeOut = leftedgeOut + outblk.w;

					kIn = leftedgeIn;
					kOut = leftedgeOut;

					if (1 == (x0Out & 0x1))
					{ // first is odd do the pixel once.
						outdataFloat[kOut] = this.dataFloat[c][kIn];
						kOut++;
						kIn++;
					}

					if (0 == (x1Out & 0x1))
					{ // last is even adjust loop bounds
						rightedgeOut--;
					}

					while (kOut < rightedgeOut)
					{
						outdataFloat[kOut] = this.dataFloat[c][kIn];
						kOut++;
						outdataFloat[kOut] = this.dataFloat[c][kIn];
						kOut++;
						kIn++;
					}

					if (0 == (x1Out & 0x1))
					{ // last is even do the pixel once.
						outdataFloat[kOut] = this.dataFloat[c][kIn];
						kOut++;
					}
				}

				outblk.progressive = inblkFloat.progressive;
				break;

			case DataBlk.TYPE_SHORT:
			case DataBlk.TYPE_BYTE:
			default:
				// Unsupported output type.
				throw new IllegalArgumentException("invalid source datablock type");
		}

		return outblk;
	}

	/**
	 * Return an appropriate String representation of this Resampler instance.
	 */
	@Override
	public String toString()
	{
		final StringBuffer rep = new StringBuffer("[Resampler: ncomps= " + this.ncomps);
		final StringBuffer body = new StringBuffer("  ");
		for (int i = 0; i < this.ncomps; ++i)
		{
			body.append(ColorSpaceMapper.eol);
			body.append("comp[");
			body.append(i);
			body.append("] xscale= ");
			body.append(this.imgdatasrc.getCompSubsX(i));
			body.append(", yscale= ");
			body.append(this.imgdatasrc.getCompSubsY(i));
		}

		rep.append(ColorSpace.indent("  ", body));
		return rep.append("]").toString();
	}

	/**
	 * Returns, in the blk argument, a block of image data containing the
	 * specifed rectangular area, in the specified component. The data is
	 * returned, as a copy of the internal data, therefore the returned data can
	 * be modified "in place".
	 * 
	 * <P>
	 * The rectangular area to return is specified by the 'ulx', 'uly', 'w' and
	 * 'h' members of the 'blk' argument, relative to the current tile. These
	 * members are not modified by this method. The 'offset' of the returned
	 * data is 0, and the 'scanw' is the same as the block's width. See the
	 * 'DataBlk' class.
	 * 
	 * <P>
	 * If the data array in 'blk' is 'null', then a new one is created. If the
	 * data array is not 'null' then it is reused, and it must be large enough
	 * to contain the block's data. Otherwise an 'ArrayStoreException' or an
	 * 'IndexOutOfBoundsException' is thrown by the Java system.
	 * 
	 * <P>
	 * The returned data has its 'progressive' attribute set to that of the
	 * input data.
	 * 
	 * @param outblk
	 *            Its coordinates and dimensions specify the area to return. If
	 *            it contains a non-null data array, then it must have the
	 *            correct dimensions. If it contains a null data array a new one
	 *            is created. The fields in this object are modified to return
	 *            the data.
	 * 
	 * @param c
	 *            The index of the component from which to get the data. Only 0
	 *            and 3 are valid.
	 * 
	 * @return The requested DataBlk
	 * 
	 * @see #getInternCompData
	 */
	@Override
	public DataBlk getCompData(final DataBlk outblk, final int c)
	{
		return this.getInternCompData(outblk, c);
	}

	/**
	 * Returns the height in pixels of the specified component in the overall
	 * image.
	 */
	@Override
	public int getCompImgHeight(final int c)
	{
		return this.src.getCompImgHeight(c) * this.src.getCompSubsY(c);
	}

	/**
	 * Returns the width in pixels of the specified component in the overall
	 * image.
	 */
	@Override
	public int getCompImgWidth(final int c)
	{
		return this.src.getCompImgWidth(c) * this.src.getCompSubsX(c);
	}

	/**
	 * Returns the component subsampling factor in the horizontal direction, for
	 * the specified component.
	 */
	@Override
	public int getCompSubsX(final int c)
	{
		return 1;
	}

	/**
	 * Returns the component subsampling factor in the vertical direction, for
	 * the specified component.
	 */
	@Override
	public int getCompSubsY(final int c)
	{
		return 1;
	}

	/**
	 * Returns the height in pixels of the specified tile-component.
	 */
	@Override
	public int getTileCompHeight(final int t, final int c)
	{
		return this.src.getTileCompHeight(t, c) * this.src.getCompSubsY(c);
	}

	/**
	 * Returns the width in pixels of the specified tile-component..
	 */
	@Override
	public int getTileCompWidth(final int t, final int c)
	{
		return this.src.getTileCompWidth(t, c) * this.src.getCompSubsX(c);
	}

	/* end class Resampler */
}
