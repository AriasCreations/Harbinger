/*****************************************************************************
 *
 * $Id: ColorSpaceMapper.java,v 1.2 2002/07/25 16:30:55 grosbois Exp $
 *
 * Copyright Eastman Kodak Company, 343 State Street, Rochester, NY 14650
 * $Date $
 ************************************************************************/

package dev.zontreck.harbinger.thirdparty.jj2000.j2k.colorspace;

import java.io.*;

import dev.zontreck.harbinger.thirdparty.jj2000.j2k.image.*;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.util.*;

import dev.zontreck.harbinger.thirdparty.jj2000.j2k.icc.*;

/**
 * This is the base class for all modules in the dev.zontreck.harbinger.thirdparty.jj2000.j2k.colorspace and dev.zontreck.harbinger.thirdparty.jj2000.j2k.icc profiling
 * steps of the decoding chain. It is responsible for the allocation and
 * iniitialization of all working storage. It provides several utilities which
 * are of generic use in preparing DataBlks for use and provides default
 * implementations for the getCompData and getInternCompData methods.
 * 
 * @see dev.zontreck.harbinger.thirdparty.jj2000.j2k.colorspace.ColorSpace
 * @version 1.0
 * @author Bruce A. Kern
 */
public abstract class ColorSpaceMapper extends ImgDataAdapter implements BlkImgDataSrc
{

	/** The prefix for ICC Profiler options */
	public static final char OPT_PREFIX = 'I';

	/** Platform dependant end of line String. */
	protected static final String eol = System.getProperty("line.separator");

	// Temporary data buffers needed during profiling.
	protected DataBlkInt[] inInt; // Integer input data.
	protected DataBlkFloat[] inFloat; // Floating point input data.
	protected DataBlkInt[] workInt; // Input data shifted to zero-offset
	protected DataBlkFloat[] workFloat; // Input data shifted to zero-offset.
	protected int[][] dataInt; // Points to input data.
	protected float[][] dataFloat; // Points to input data.
	protected float[][] workDataFloat;// References working data pixels.
	protected int[][] workDataInt; // References working data pixels.

	/* input data parameters by component */
	protected int[] shiftValueArray;
	protected int[] maxValueArray;
	protected int[] fixedPtBitsArray;

	/** The list of parameters that are accepted for ICC profiling. */
	private static final String[][] pinfo = { { "IcolorSpacedebug", null,
			"Print debugging messages during dev.zontreck.harbinger.thirdparty.jj2000.j2k.colorspace mapping.", "off" } };

	/** Parameter Specs */
	protected ParameterList pl;

	/** ColorSpace info */
	protected ColorSpace csMap;

	/** Number of image components */
	protected int ncomps;

	/** The image source. */
	protected BlkImgDataSrc src;

	/** The image source data per component. */
	protected DataBlk[] srcBlk;

	protected class ComputedComponents
	{
		private int h = -1;
		private int w = -1;
		private int ulx = -1;
		private int uly = -1;
		private int offset = -1;
		private int scanw = -1;

		public ComputedComponents()
		{
			this.clear();
		}

		public ComputedComponents(final DataBlk db)
		{
			this.set(db);
		}

		public void set(final DataBlk db)
		{
			this.h = db.h;
			this.w = db.w;
			this.ulx = db.ulx;
			this.uly = db.uly;
			this.offset = db.offset;
			this.scanw = db.scanw;
		}

		public void clear()
		{
			this.h = this.w = this.ulx = this.uly = this.offset = this.scanw = -1;
		}

		public boolean equals(final ComputedComponents cc)
		{
			return (this.h == cc.h && this.w == cc.w && this.ulx == cc.ulx && this.uly == cc.uly && this.offset == cc.offset && this.scanw == cc.scanw);
		}

		/* end class ComputedComponents */
	}

	protected ComputedComponents computed = new ComputedComponents();

	/**
	 * Returns the parameters that are used in this class and implementing
	 * classes. It returns a 2D String array. Each of the 1D arrays is for a
	 * different option, and they have 3 elements. The first element is the
	 * option name, the second one is the synopsis and the third one is a long
	 * description of what the parameter is. The synopsis or description may be
	 * 'null', in which case it is assumed that there is no synopsis or
	 * description of the option, respectively. Null may be returned if no
	 * options are supported.
	 * 
	 * @return the options name, their synopsis and their explanation, or null
	 *         if no options are supported.
	 */
	public static String[][] getParameterInfo()
	{
		return ColorSpaceMapper.pinfo;
	}

	/**
	 * Arrange for the input DataBlk to receive an appropriately sized and typed
	 * data buffer
	 * 
	 * @param db
	 *            input DataBlk
	 * @see dev.zontreck.harbinger.thirdparty.jj2000.j2k.image.DataBlk
	 */
	protected static void setInternalBuffer(final DataBlk db)
	{
		switch (db.getDataType())
		{

			case DataBlk.TYPE_INT:
				if (null == db.getData() || ((int[]) db.getData()).length < db.w * db.h)
					db.setData(new int[db.w * db.h]);
				break;

			case DataBlk.TYPE_FLOAT:
				if (null == db.getData() || ((float[]) db.getData()).length < db.w * db.h)
				{
					db.setData(new float[db.w * db.h]);
				}
				break;

			default:
				throw new IllegalArgumentException("Invalid output datablock type");
		}
	}

	/**
	 * Copy the DataBlk geometry from source to target DataBlk and assure that
	 * the target has an appropriate data buffer.
	 * 
	 * @param tgt
	 *            has its geometry set.
	 * @param src
	 *            used to get the new geometric parameters.
	 */
	protected static void copyGeometry(final DataBlk tgt, final DataBlk src)
	{
		tgt.offset = 0;
		tgt.h = src.h;
		tgt.w = src.w;
		tgt.ulx = src.ulx;
		tgt.uly = src.uly;
		tgt.scanw = src.w;

		// Create data array if necessary

		ColorSpaceMapper.setInternalBuffer(tgt);
	}

	/**
	 * Factory method for creating instances of this class.
	 * 
	 * @param src
	 *            -- source of image data
	 * @param csMap
	 *            -- provides dev.zontreck.harbinger.thirdparty.jj2000.j2k.colorspace info
	 * @return ColorSpaceMapper instance
	 * @exception IOException
	 *                profile access exception
	 */
	public static BlkImgDataSrc createInstance(final BlkImgDataSrc src, final ColorSpace csMap) throws IOException,
			ColorSpaceException, ICCProfileException
	{

		// Check parameters
		csMap.pl.checkList(ColorSpaceMapper.OPT_PREFIX, ParameterList.toNameArray(ColorSpaceMapper.pinfo));

		// Perform ICCProfiling or ColorSpace tranfsormation.
		if (csMap.getMethod() == ColorSpace.ICC_PROFILED)
		{
			return ICCProfiler.createInstance(src, csMap);
		}

		final ColorSpace.CSEnum colorspace = csMap.getColorSpace();

		if (colorspace == ColorSpace.sRGB)
		{
			return EnumeratedColorSpaceMapper.createInstance(src, csMap);
		}
		else if (colorspace == ColorSpace.GreyScale)
		{
			return EnumeratedColorSpaceMapper.createInstance(src, csMap);
		}
		else if (colorspace == ColorSpace.sYCC)
		{
			return SYccColorSpaceMapper.createInstance(src, csMap);
		}
		else if (colorspace == ColorSpace.Unknown)
		{
			return null;
		}
		else
		{
			throw new ColorSpaceException("Bad color space specification in image");
		}
	}

	/**
	 * Ctor which creates an ICCProfile for the image and initializes all data
	 * objects (input, working, and output).
	 * 
	 * @param src
	 *            -- Source of image data
	 * @param csMap
	 *            -- provides dev.zontreck.harbinger.thirdparty.jj2000.j2k.colorspace info
	 * 
	 */
	protected ColorSpaceMapper(final BlkImgDataSrc src, final ColorSpace csMap)
	{
		super(src);
		this.src = src;
		this.csMap = csMap;
		this.initialize();
		/* end ColorSpaceMapper ctor */}

	/** General utility used by ctors */
	private void initialize()
	{

		pl = this.csMap.pl;
		ncomps = this.src.getNumComps();

		this.shiftValueArray = new int[this.ncomps];
		this.maxValueArray = new int[this.ncomps];
		this.fixedPtBitsArray = new int[this.ncomps];

		this.srcBlk = new DataBlk[this.ncomps];
		this.inInt = new DataBlkInt[this.ncomps];
		this.inFloat = new DataBlkFloat[this.ncomps];
		this.workInt = new DataBlkInt[this.ncomps];
		this.workFloat = new DataBlkFloat[this.ncomps];
		this.dataInt = new int[this.ncomps][];
		this.dataFloat = new float[this.ncomps][];
		this.workDataInt = new int[this.ncomps][];
		this.workDataFloat = new float[this.ncomps][];
		this.dataInt = new int[this.ncomps][];
		this.dataFloat = new float[this.ncomps][];

		/*
		 * For each component, get a reference to the pixel data and set up
		 * working DataBlks for both integer and float output.
		 */
		for (int i = 0; i < this.ncomps; ++i)
		{

			this.shiftValueArray[i] = 1 << (this.src.getNomRangeBits(i) - 1);
			this.maxValueArray[i] = (1 << this.src.getNomRangeBits(i)) - 1;
			this.fixedPtBitsArray[i] = this.src.getFixedPoint(i);

			this.inInt[i] = new DataBlkInt();
			this.inFloat[i] = new DataBlkFloat();
			this.workInt[i] = new DataBlkInt();
			this.workInt[i].progressive = this.inInt[i].progressive;
			this.workFloat[i] = new DataBlkFloat();
			this.workFloat[i].progressive = this.inFloat[i].progressive;
		}
	}

	/**
	 * Returns the number of bits, referred to as the "range bits",
	 * corresponding to the nominal range of the data in the specified
	 * component. If this number is <i>b</b> then for unsigned data the nominal
	 * range is between 0 and 2^b-1, and for signed data it is between -2^(b-1)
	 * and 2^(b-1)-1. For floating point data this value is not applicable.
	 * 
	 * @param c
	 *            The index of the component.
	 * 
	 * @return The number of bits corresponding to the nominal range of the
	 *         data. Fro floating-point data this value is not applicable and
	 *         the return value is undefined.
	 */
	@Override
	public int getFixedPoint(final int c)
	{
		return this.src.getFixedPoint(c);
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
	 * This method, in general, is less efficient than the 'getInternCompData()'
	 * method since, in general, it copies the data. However if the array of
	 * returned data is to be modified by the caller then this method is
	 * preferable.
	 * 
	 * <P>
	 * If the data array in 'blk' is 'null', then a new one is created. If the
	 * data array is not 'null' then it is reused, and it must be large enough
	 * to contain the block's data. Otherwise an 'ArrayStoreException' or an
	 * 'IndexOutOfBoundsException' is thrown by the Java system.
	 * 
	 * <P>
	 * The returned data may have its 'progressive' attribute set. In this case
	 * the returned data is only an approximation of the "final" data.
	 * 
	 * @param out
	 *            Its coordinates and dimensions specify the area to return,
	 *            relative to the current tile. If it contains a non-null data
	 *            array, then it must be large enough. If it contains a null
	 *            data array a new one is created. Some fields in this object
	 *            are modified to return the data.
	 * 
	 * @param c
	 *            The index of the component from which to get the data.
	 * 
	 * @see #getInternCompData
	 */
	@Override
	public DataBlk getCompData(final DataBlk out, final int c)
	{
		return this.src.getCompData(out, c);
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
	 * @param out
	 *            Its coordinates and dimensions specify the area to return,
	 *            relative to the current tile. Some fields in this object are
	 *            modified to return the data.
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
		return this.src.getInternCompData(out, c);
	}

	/* end class ColorSpaceMapper */
}
