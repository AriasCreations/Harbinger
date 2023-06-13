/*****************************************************************************
 *
 * $Id: ICCProfiler.java,v 1.2 2002/08/08 14:08:27 grosbois Exp $
 *
 * Copyright Eastman Kodak Company, 343 State Street, Rochester, NY 14650
 * $Date $
 ****************************************************************************/
package dev.zontreck.harbinger.thirdparty.jj2000.j2k.icc;

import java.io.*;

import dev.zontreck.harbinger.thirdparty.jj2000.j2k.image.*;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.util.*;

import dev.zontreck.harbinger.thirdparty.jj2000.j2k.colorspace.*;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.icc.lut.*;

/**
 * This class provides ICC Profiling API for the dev.zontreck.harbinger.thirdparty.jj2000.j2k imaging chain by
 * implementing the BlkImgDataSrc interface, in particular the getCompData and
 * getInternCompData methods.
 *
 * @version 1.0
 * @author Bruce A. Kern
 */
public class ICCProfiler extends ColorSpaceMapper
{

	/** The prefix for ICC Profiler options */
	public static final char OPT_PREFIX = 'I';

	/** Platform dependant end of line String. */
	protected static final String eol = System.getProperty("line.separator");

	// ICCProfiles.
	RestrictedICCProfile ricc;
	ICCProfile icc;

	// Temporary variables needed during profiling.
	private DataBlkInt[] tempInt; // Holds the results of the transform.
	private DataBlkFloat[] tempFloat; // Holds the results of the transform.

	private Object xform;

	/** The image's ICC profile. */
	private RestrictedICCProfile iccp;

	/**
	 * Factory method for creating instances of this class.
	 * 
	 * @param src
	 *            -- source of image data
	 * @param csMap
	 *            -- provides dev.zontreck.harbinger.thirdparty.jj2000.j2k.colorspace info
	 * @return ICCProfiler instance
	 * @exception IOException
	 *                profile access exception
	 * @exception ICCProfileException
	 *                profile content exception
	 */
	public static BlkImgDataSrc createInstance(final BlkImgDataSrc src, final ColorSpace csMap) throws IOException,
			ICCProfileException, ColorSpaceException
	{
		return new ICCProfiler(src, csMap);
	}

	/**
	 * Ctor which creates an ICCProfile for the image and initializes all data
	 * objects (input, working, output).
	 * 
	 * @param src
	 *            -- Source of image data
	 * 
	 * @exception IOException
	 * @exception ICCProfileException
	 * @exception IllegalArgumentException
	 */
	protected ICCProfiler(final BlkImgDataSrc src, final ColorSpace csMap) throws ColorSpaceException, IOException,
			ICCProfileException, IllegalArgumentException
	{
		super(src, csMap);
		this.initialize();

		this.iccp = this.getICCProfile(csMap);
		if (1 == ncomps)
		{
			this.xform = new MonochromeTransformTosRGB(this.iccp, this.maxValueArray[0], this.shiftValueArray[0]);
		}
		else
		{
			this.xform = new MatrixBasedTransformTosRGB(this.iccp, this.maxValueArray, this.shiftValueArray);
		}

		/* end ICCProfiler ctor */}

	/** General utility used by ctors */
	private void initialize()
	{

		this.tempInt = new DataBlkInt[this.ncomps];
		this.tempFloat = new DataBlkFloat[this.ncomps];

		/*
		 * For each component, get the maximum data value, a reference to the
		 * pixel data and set up working and temporary DataBlks for both integer
		 * and float output.
		 */
		for (int i = 0; i < this.ncomps; ++i)
		{
			this.tempInt[i] = new DataBlkInt();
			this.tempFloat[i] = new DataBlkFloat();
		}
	}

	/**
	 * Get the ICCProfile information JP2 ColorSpace
	 * 
	 * @param csm
	 *            provides all necessary info about the dev.zontreck.harbinger.thirdparty.jj2000.j2k.colorspace
	 * @return ICCMatrixBasedInputProfile for 3 component input and
	 *         ICCMonochromeInputProfile for a 1 component source. Returns null
	 *         if exceptions were encountered.
	 * @exception ColorSpaceException
	 * @exception ICCProfileException
	 * @exception IllegalArgumentException
	 */
	private RestrictedICCProfile getICCProfile(final ColorSpace csm) throws ColorSpaceException, ICCProfileException,
			IllegalArgumentException
	{

		switch (this.ncomps)
		{
			case 1:
				this.icc = ICCMonochromeInputProfile.createInstance(csm);
				this.ricc = this.icc.parse();
				if (RestrictedICCProfile.kMonochromeInput != ricc.getType())
					throw new IllegalArgumentException("wrong ICCProfile type for image");
				break;
			case 3:
				this.icc = ICCMatrixBasedInputProfile.createInstance(csm);
				this.ricc = this.icc.parse();
				if (RestrictedICCProfile.kThreeCompInput != ricc.getType())
					throw new IllegalArgumentException("wrong ICCProfile type for image");
				break;
			default:
				throw new IllegalArgumentException("illegal number of components (" + this.ncomps + ") in image");
		}
		return this.ricc;
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

		try
		{
			if (1 != ncomps && 3 != ncomps)
			{
				final String msg = "ICCProfiler: dev.zontreck.harbinger.thirdparty.jj2000.j2k.icc profile _not_ applied to " + this.ncomps + " component image";
				FacilityManager.getMsgLogger().printmsg(MsgLogger.WARNING, msg);
				return this.src.getCompData(outblk, c);
			}

			final int type = outblk.getDataType();

			int leftedgeOut = -1; // offset to the start of the output scanline
			// scanline + 1
			int leftedgeIn = -1; // offset to the start of the input scanline
			int rightedgeIn = -1; // offset to the end of the input
			// scanline + 1

			// Calculate all components:
			for (int i = 0; i < this.ncomps; ++i)
			{

				final int fixedPtBits = this.src.getFixedPoint(i);
				final int shiftVal = this.shiftValueArray[i];
				final int maxVal = this.maxValueArray[i];

				// Initialize general input and output indexes
				int kOut = -1;
				int kIn = -1;

				switch (type)
				{ // Int and Float data only

					case DataBlk.TYPE_INT:

						// Set up the DataBlk geometry
						ColorSpaceMapper.copyGeometry(this.workInt[i], outblk);
						ColorSpaceMapper.copyGeometry(this.tempInt[i], outblk);
						ColorSpaceMapper.copyGeometry(this.inInt[i], outblk);
						ColorSpaceMapper.setInternalBuffer(outblk);

						// Reference the output array
						this.workDataInt[i] = (int[]) this.workInt[i].getData();

						// Request data from the source.
						this.inInt[i] = (DataBlkInt) this.src.getInternCompData(this.inInt[i], i);
						this.dataInt[i] = this.inInt[i].getDataInt();

						// The nitty-gritty.

						for (int row = 0; row < outblk.h; ++row)
						{
							leftedgeIn = this.inInt[i].offset + row * this.inInt[i].scanw;
							rightedgeIn = leftedgeIn + this.inInt[i].w;
							leftedgeOut = outblk.offset + row * outblk.scanw;

							for (kOut = leftedgeOut, kIn = leftedgeIn; kIn < rightedgeIn; ++kIn, ++kOut)
							{
								final int tmpInt = (this.dataInt[i][kIn] >> fixedPtBits) + shiftVal;
								this.workDataInt[i][kOut] = ((0 > tmpInt) ? 0 : ((tmpInt > maxVal) ? maxVal : tmpInt));
							}
						}
						break;

					case DataBlk.TYPE_FLOAT:

						// Set up the DataBlk geometry
						ColorSpaceMapper.copyGeometry(this.workFloat[i], outblk);
						ColorSpaceMapper.copyGeometry(this.tempFloat[i], outblk);
						ColorSpaceMapper.copyGeometry(this.inFloat[i], outblk);
						ColorSpaceMapper.setInternalBuffer(outblk);

						// Reference the output array
						this.workDataFloat[i] = (float[]) this.workFloat[i].getData();

						// Request data from the source.
						this.inFloat[i] = (DataBlkFloat) this.src.getInternCompData(this.inFloat[i], i);
						this.dataFloat[i] = this.inFloat[i].getDataFloat();

						// The nitty-gritty.

						for (int row = 0; row < outblk.h; ++row)
						{
							leftedgeIn = this.inFloat[i].offset + row * this.inFloat[i].scanw;
							rightedgeIn = leftedgeIn + this.inFloat[i].w;
							leftedgeOut = outblk.offset + row * outblk.scanw;

							for (kOut = leftedgeOut, kIn = leftedgeIn; kIn < rightedgeIn; ++kIn, ++kOut)
							{
								final float tmpFloat = this.dataFloat[i][kIn] / (1 << fixedPtBits) + shiftVal;
								this.workDataFloat[i][kOut] = ((0 > tmpFloat) ? 0
										: ((tmpFloat > maxVal) ? maxVal : tmpFloat));
							}
						}
						break;

					case DataBlk.TYPE_SHORT:
					case DataBlk.TYPE_BYTE:
					default:
						// Unsupported output type.
						throw new IllegalArgumentException("Invalid source datablock type");
				}
			}

			switch (type)
			{ // Int and Float data only

				case DataBlk.TYPE_INT:

					if (1 == ncomps)
					{
						((MonochromeTransformTosRGB) this.xform).apply(this.workInt[c], this.tempInt[c]);
					}
					else
					{ // ncomps == 3
						((MatrixBasedTransformTosRGB) this.xform).apply(this.workInt, this.tempInt);
					}

					outblk.progressive = this.inInt[c].progressive;
					outblk.setData(this.tempInt[c].getData());
					break;

				case DataBlk.TYPE_FLOAT:

					if (1 == ncomps)
					{
						((MonochromeTransformTosRGB) this.xform).apply(this.workFloat[c], this.tempFloat[c]);
					}
					else
					{ // ncomps == 3
						((MatrixBasedTransformTosRGB) this.xform).apply(this.workFloat, this.tempFloat);
					}

					outblk.progressive = this.inFloat[c].progressive;
					outblk.setData(this.tempFloat[c].getData());
					break;

				case DataBlk.TYPE_SHORT:
				case DataBlk.TYPE_BYTE:
				default:
					// Unsupported output type.
					throw new IllegalArgumentException("invalid source datablock type");
			}

			// Initialize the output block geometry and set the profiled
			// data into the output block.
			outblk.offset = 0;
			outblk.scanw = outblk.w;
		}
		catch (final MatrixBasedTransformException e)
		{
			FacilityManager.getMsgLogger().printmsg(MsgLogger.ERROR, "matrix transform problem:\n" + e.getMessage());
			if ("on".equals(pl.getParameter("debug")))
			{
				e.printStackTrace();
			}
			else
			{
				FacilityManager.getMsgLogger().printmsg(MsgLogger.ERROR, "Use '-debug' option for more details");
			}
			return null;
		}
		catch (final MonochromeTransformException e)
		{
			FacilityManager.getMsgLogger()
					.printmsg(MsgLogger.ERROR, "monochrome transform problem:\n" + e.getMessage());
			if ("on".equals(pl.getParameter("debug")))
			{
				e.printStackTrace();
			}
			else
			{
				FacilityManager.getMsgLogger().printmsg(MsgLogger.ERROR, "Use '-debug' option for more details");
			}
			return null;
		}

		return outblk;
	}

	/**
	 * Returns, in the blk argument, a block of image data containing the
	 * specifed rectangular area, in the specified component. The data is
	 * returned, as a reference to the internal data, if any, instead of as a
	 * copy, therefore the returned data should not be modified.
	 * 
	 * <P>
	 * The rectangular area to return is specified by the 'ulx', 'uly', 'w' and
	 * 'h' members of the 'blk' argument, relative to the current tile. These
	 * members are not modified by this method. The 'offset' and 'scanw' of the
	 * returned data can be arbitrary. See the 'DataBlk' class.
	 * 
	 * <P>
	 * This method, in general, is more efficient than the 'getCompData()'
	 * method since it may not copy the data. However if the array of returned
	 * data is to be modified by the caller then the other method is probably
	 * preferable.
	 * 
	 * <P>
	 * If possible, the data in the returned 'DataBlk' should be the internal
	 * data itself, instead of a copy, in order to increase the data transfer
	 * efficiency. However, this depends on the particular implementation (it
	 * may be more convenient to just return a copy of the data). This is the
	 * reason why the returned data should not be modified.
	 * 
	 * <P>
	 * If the data array in <tt>blk</tt> is <tt>null</tt>, then a new one is
	 * created if necessary. The implementation of this interface may choose to
	 * return the same array or a new one, depending on what is more efficient.
	 * Therefore, the data array in <tt>blk</tt> prior to the method call should
	 * not be considered to contain the returned data, a new array may have been
	 * created. Instead, get the array from <tt>blk</tt> after the method has
	 * returned.
	 * 
	 * <P>
	 * The returned data may have its 'progressive' attribute set. In this case
	 * the returned data is only an approximation of the "final" data.
	 *
	 * 
	 * @param c
	 *            The index of the component from which to get the data.
	 * 
	 * @return The requested DataBlk
	 * 
	 * @see #getCompData
	 */
	@Override
	public DataBlk getInternCompData(final DataBlk out, final int c)
	{
		return this.getCompData(out, c);
	}

	/** Return a suitable String representation of the class instance. */
	@Override
	public String toString()
	{
		final StringBuffer rep = new StringBuffer("[ICCProfiler:");
		final StringBuffer body = new StringBuffer();
		if (null != icc)
			body.append(ICCProfiler.eol).append(ColorSpace.indent("  ", this.icc.toString()));
		if (null != xform)
			body.append(ICCProfiler.eol).append(ColorSpace.indent("  ", this.xform.toString()));
		rep.append(ColorSpace.indent("  ", body));
		return rep.append("]").toString();
	}

	/* end class ICCProfiler */
}
