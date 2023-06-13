/*****************************************************************************
 *
 * $Id: SYccColorSpaceMapper.java,v 1.1 2002/07/25 14:52:01 grosbois Exp $
 *
 * Copyright Eastman Kodak Company, 343 State Street, Rochester, NY 14650
 * $Date $
 ****************************************************************************/

package dev.zontreck.harbinger.thirdparty.jj2000.j2k.colorspace;

import dev.zontreck.harbinger.thirdparty.jj2000.j2k.image.BlkImgDataSrc;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.image.DataBlk;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.image.DataBlkInt;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.image.DataBlkFloat;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.util.FacilityManager;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.util.MsgLogger;

/**
 * This decodes maps which are defined in the sYCC dev.zontreck.harbinger.thirdparty.jj2000.j2k.colorspace into the sRGB
 * colorspadce.
 * 
 * @see dev.zontreck.harbinger.thirdparty.jj2000.j2k.colorspace.ColorSpace
 * @version 1.0
 * @author Bruce A. Kern
 */
public class SYccColorSpaceMapper extends ColorSpaceMapper
{
	/* sYCC dev.zontreck.harbinger.thirdparty.jj2000.j2k.colorspace matrix */

	/** Matrix component for ycc transform. */
	protected static float Matrix00 = 1;
	/** Matrix component for ycc transform. */
	protected static float Matrix01;
	/** Matrix component for ycc transform. */
	protected static float Matrix02 = 1.402f;
	/** Matrix component for ycc transform. */
	protected static float Matrix10 = 1;
	/** Matrix component for ycc transform. */
	protected static float Matrix11 = -0.34413f;
	/** Matrix component for ycc transform. */
	protected static float Matrix12 = -0.71414f;
	/** Matrix component for ycc transform. */
	protected static float Matrix20 = 1;
	/** Matrix component for ycc transform. */
	protected static float Matrix21 = 1.772f;
	/** Matrix component for ycc transform. */
	protected static float Matrix22;

	/**
	 * Factory method for creating instances of this class.
	 * 
	 * @param src
	 *            -- source of image data
	 * @param csMap
	 *            -- provides dev.zontreck.harbinger.thirdparty.jj2000.j2k.colorspace info
	 * @return SYccColorSpaceMapper instance
	 */
	public static BlkImgDataSrc createInstance(final BlkImgDataSrc src, final ColorSpace csMap) throws ColorSpaceException
	{
		return new SYccColorSpaceMapper(src, csMap);
	}

	/**
	 * Ctor which creates an ICCProfile for the image and initializes all data
	 * objects (input, working, and output).
	 * 
	 * @param src
	 *            -- Source of image data
	 * @param csMap
	 *            -- provides dev.zontreck.harbinger.thirdparty.jj2000.j2k.colorspace info
	 */
	protected SYccColorSpaceMapper(final BlkImgDataSrc src, final ColorSpace csMap) throws ColorSpaceException
	{
		super(src, csMap);
		this.initialize();
		/* end SYccColorSpaceMapper ctor */}

	/** General utility used by ctors */
	private void initialize() throws ColorSpaceException
	{

		if (1 != ncomps && 3 != ncomps)
		{
			final String msg = "SYccColorSpaceMapper: ycc transformation _not_ applied to " + this.ncomps + " component image";
			FacilityManager.getMsgLogger().printmsg(MsgLogger.ERROR, msg);
			throw new ColorSpaceException(msg);
		}
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
	 * @see #getInternCompData
	 */
	@Override
	public DataBlk getCompData(final DataBlk outblk, final int c)
	{

		final int type = outblk.getDataType();

		int i;

		// Calculate all components:
		for (i = 0; i < this.ncomps; ++i)
		{

			// Set up the working DataBlk geometry.
			ColorSpaceMapper.copyGeometry(this.workInt[i], outblk);
			ColorSpaceMapper.copyGeometry(this.workFloat[i], outblk);
			ColorSpaceMapper.copyGeometry(this.inInt[i], outblk);
			ColorSpaceMapper.copyGeometry(this.inFloat[i], outblk);

			// Request data from the source.
			this.inInt[i] = (DataBlkInt) this.src.getInternCompData(this.inInt[i], i);
		}

		if (DataBlk.TYPE_INT == type)
		{
			if (1 == ncomps)
				this.workInt[c] = this.inInt[c];
			else
				this.workInt = SYccColorSpaceMapper.mult(this.inInt);
			outblk.progressive = this.inInt[c].progressive;
			outblk.setData(this.workInt[c].getData());
		}

		if (DataBlk.TYPE_FLOAT == type)
		{
			if (1 == ncomps)
				this.workFloat[c] = this.inFloat[c];
			else
				this.workFloat = SYccColorSpaceMapper.mult(this.inFloat);
			outblk.progressive = this.inFloat[c].progressive;
			outblk.setData(this.workFloat[c].getData());
		}

		// Initialize the output block geometry and set the profiled
		// data into the output block.
		outblk.offset = 0;
		outblk.scanw = outblk.w;

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
		return this.getCompData(out, c);
	}

	/**
	 * Output a DataBlkFloat array where each sample in each component is the
	 * product of the YCC matrix * the vector of samples across the input
	 * components.
	 * 
	 * @param inblk
	 *            input DataBlkFloat array
	 * @return output DataBlkFloat array
	 */
	private static DataBlkFloat[] mult(final DataBlkFloat[] inblk)
	{

		if (3 != inblk.length)
			throw new IllegalArgumentException("bad input array size");

		int i, j;
		final int length = inblk[0].h * inblk[0].w;
		final DataBlkFloat[] outblk = new DataBlkFloat[3];
		final float[][] out = new float[3][];
		final float[][] in = new float[3][];

		for (i = 0; 3 > i; ++i)
		{
			in[i] = inblk[i].getDataFloat();
			outblk[i] = new DataBlkFloat();
			ColorSpaceMapper.copyGeometry(outblk[i], inblk[i]);
			outblk[i].offset = inblk[i].offset;
			out[i] = new float[length];
			outblk[i].setData(out[i]);
		}

		for (j = 0; j < length; ++j)
		{
			out[0][j] = (SYccColorSpaceMapper.Matrix00 * in[0][inblk[0].offset + j] + SYccColorSpaceMapper.Matrix01 * in[1][inblk[1].offset + j] + SYccColorSpaceMapper.Matrix02
					* in[2][inblk[2].offset + j]);

			out[1][j] = (SYccColorSpaceMapper.Matrix10 * in[0][inblk[0].offset + j] + SYccColorSpaceMapper.Matrix11 * in[1][inblk[1].offset + j] + SYccColorSpaceMapper.Matrix12
					* in[2][inblk[2].offset + j]);

			out[2][j] = (SYccColorSpaceMapper.Matrix20 * in[0][inblk[0].offset + j] + SYccColorSpaceMapper.Matrix21 * in[1][inblk[1].offset + j] + SYccColorSpaceMapper.Matrix22
					* in[2][inblk[2].offset + j]);
		}

		return outblk;
	}

	/**
	 * Output a DataBlkInt array where each sample in each component is the
	 * product of the YCC matrix * the vector of samples across the input
	 * components.
	 * 
	 * @param inblk
	 *            input DataBlkInt array
	 * @return output DataBlkInt array
	 */
	private static DataBlkInt[] mult(final DataBlkInt[] inblk)
	{

		if (3 != inblk.length)
			throw new IllegalArgumentException("bad input array size");

		int i, j;
		final int length = inblk[0].h * inblk[0].w;
		final DataBlkInt[] outblk = new DataBlkInt[3];
		final int[][] out = new int[3][];
		final int[][] in = new int[3][];

		for (i = 0; 3 > i; ++i)
		{
			in[i] = inblk[i].getDataInt();
			outblk[i] = new DataBlkInt();
			ColorSpaceMapper.copyGeometry(outblk[i], inblk[i]);
			outblk[i].offset = inblk[i].offset;
			out[i] = new int[length];
			outblk[i].setData(out[i]);
		}

		for (j = 0; j < length; ++j)
		{
			out[0][j] = (int) (SYccColorSpaceMapper.Matrix00 * in[0][inblk[0].offset + j] + SYccColorSpaceMapper.Matrix01 * in[1][inblk[1].offset + j] + SYccColorSpaceMapper.Matrix02
					* in[2][inblk[2].offset + j]);

			out[1][j] = (int) (SYccColorSpaceMapper.Matrix10 * in[0][inblk[0].offset + j] + SYccColorSpaceMapper.Matrix11 * in[1][inblk[1].offset + j] + SYccColorSpaceMapper.Matrix12
					* in[2][inblk[2].offset + j]);

			out[2][j] = (int) (SYccColorSpaceMapper.Matrix20 * in[0][inblk[0].offset + j] + SYccColorSpaceMapper.Matrix21 * in[1][inblk[1].offset + j] + SYccColorSpaceMapper.Matrix22
					* in[2][inblk[2].offset + j]);
		}

		return outblk;
	}

	/** Return a suitable String representation of the class instance. */
	@Override
	public String toString()
	{
		int i;

		final String rep_nComps = "ncomps= " + this.ncomps;
		final StringBuffer rep_comps = new StringBuffer();

		for (i = 0; i < this.ncomps; ++i)
		{
			rep_comps.append("  ").append("component[").append(i).append("] height, width = (")
					.append(this.src.getCompImgHeight(i)).append(", ").append(this.src.getCompImgWidth(i)).append(")")
					.append(ColorSpaceMapper.eol);
		}

		final String rep = "[SYccColorSpaceMapper " + rep_nComps + ColorSpaceMapper.eol +
				rep_comps + "  " +
				"]";

		return rep;
	}

	/* end class SYccColorSpaceMapper */
}
