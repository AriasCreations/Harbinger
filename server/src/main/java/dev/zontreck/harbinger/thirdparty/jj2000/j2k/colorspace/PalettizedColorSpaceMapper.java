/*****************************************************************************
 *
 * $Id: PalettizedColorSpaceMapper.java,v 1.2 2002/08/08 14:07:16 grosbois Exp $
 *
 * Copyright Eastman Kodak Company, 343 State Street, Rochester, NY 14650
 * $Date $
 ****************************************************************************/
package dev.zontreck.harbinger.thirdparty.jj2000.j2k.colorspace;

import dev.zontreck.harbinger.thirdparty.jj2000.j2k.image.*;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.util.*;

import dev.zontreck.harbinger.thirdparty.jj2000.j2k.colorspace.boxes.*;

/**
 * This class provides decoding of images with palettized colorspaces. Here each
 * sample in the input is treated as an index into a color palette of triplet
 * sRGB output values.
 * 
 * @see dev.zontreck.harbinger.thirdparty.jj2000.j2k.colorspace.ColorSpace
 * @version 1.0
 * @author Bruce A. Kern
 */
public class PalettizedColorSpaceMapper extends ColorSpaceMapper
{
	int[] outShiftValueArray;
	int srcChannel;

	/** Access to the palette box information. */
	private final PaletteBox /* final */pbox;

	/**
	 * Factory method for creating instances of this class.
	 * 
	 * @param src
	 *            -- source of image data
	 * @param csMap
	 *            -- provides dev.zontreck.harbinger.thirdparty.jj2000.j2k.colorspace info
	 * @return PalettizedColorSpaceMapper instance
	 */
	public static BlkImgDataSrc createInstance(final BlkImgDataSrc src, final ColorSpace csMap) throws ColorSpaceException
	{
		return new PalettizedColorSpaceMapper(src, csMap);
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
	protected PalettizedColorSpaceMapper(final BlkImgDataSrc src, final ColorSpace csMap) throws ColorSpaceException
	{
		super(src, csMap);
		this.pbox = csMap.getPaletteBox();
		this.initialize();
	}

	/** General utility used by ctors */
	private void initialize() throws ColorSpaceException
	{
		if (1 != ncomps && 3 != ncomps)
			throw new ColorSpaceException("wrong number of components (" + this.ncomps + ") for palettized image");

		final int outComps = this.getNumComps();
		this.outShiftValueArray = new int[outComps];

		for (int i = 0; i < outComps; i++)
		{
			this.outShiftValueArray[i] = 1 << (this.getNomRangeBits(i) - 1);
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
	 * @param out
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
	public DataBlk getCompData(final DataBlk out, final int c)
	{

		if (null == pbox)
			return this.src.getCompData(out, c);

		if (1 != ncomps)
		{
			final String msg = "PalettizedColorSpaceMapper: color palette _not_ applied, incorrect number ("
					+ this.ncomps + ") of components";
			FacilityManager.getMsgLogger().printmsg(MsgLogger.WARNING, msg);
			return this.src.getCompData(out, c);
		}

		// Initialize general input and output indexes
		int leftedgeOut = -1; // offset to the start of the output scanline
		// scanline + 1
		int leftedgeIn = -1; // offset to the start of the input scanline
		int rightedgeIn = -1; // offset to the end of the input
		// scanline + 1
		int kOut = -1;
		int kIn = -1;

		// Assure a properly sized data buffer for output.
		ColorSpaceMapper.setInternalBuffer(out);

		switch (out.getDataType())
		{ // Int and Float data only

			case DataBlk.TYPE_INT:

				ColorSpaceMapper.copyGeometry(this.inInt[0], out);

				// Request data from the source.
				this.inInt[0] = (DataBlkInt) this.src.getInternCompData(this.inInt[0], 0);
				this.dataInt[0] = (int[]) this.inInt[0].getData();
				final int[] outdataInt = ((DataBlkInt) out).getDataInt();

				// The nitty-gritty.

				for (int row = 0; row < out.h; ++row)
				{
					leftedgeIn = this.inInt[0].offset + row * this.inInt[0].scanw;
					rightedgeIn = leftedgeIn + this.inInt[0].w;
					leftedgeOut = out.offset + row * out.scanw;

					for (kOut = leftedgeOut, kIn = leftedgeIn; kIn < rightedgeIn; ++kIn, ++kOut)
					{
						outdataInt[kOut] = this.pbox.getEntry(c, this.dataInt[0][kIn] + this.shiftValueArray[0])
								- this.outShiftValueArray[c];
					}
				}
				out.progressive = this.inInt[0].progressive;
				break;

			case DataBlk.TYPE_FLOAT:

				ColorSpaceMapper.copyGeometry(this.inFloat[0], out);

				// Request data from the source.
				this.inFloat[0] = (DataBlkFloat) this.src.getInternCompData(this.inFloat[0], 0);
				this.dataFloat[0] = (float[]) this.inFloat[0].getData();
				final float[] outdataFloat = ((DataBlkFloat) out).getDataFloat();

				// The nitty-gritty.

				for (int row = 0; row < out.h; ++row)
				{
					leftedgeIn = this.inFloat[0].offset + row * this.inFloat[0].scanw;
					rightedgeIn = leftedgeIn + this.inFloat[0].w;
					leftedgeOut = out.offset + row * out.scanw;

					for (kOut = leftedgeOut, kIn = leftedgeIn; kIn < rightedgeIn; ++kIn, ++kOut)
					{
						outdataFloat[kOut] = this.pbox.getEntry(c, (int) this.dataFloat[0][kIn] + this.shiftValueArray[0])
								- this.outShiftValueArray[c];
					}
				}
				out.progressive = this.inFloat[0].progressive;
				break;

			case DataBlk.TYPE_SHORT:
			case DataBlk.TYPE_BYTE:
			default:
				// Unsupported output type.
				throw new IllegalArgumentException("invalid source datablock type");
		}

		// Initialize the output block geometry and set the profiled
		// data into the output block.
		out.offset = 0;
		out.scanw = out.w;
		return out;
	}

	/**
	 * Return a suitable String representation of the class instance, e.g.
	 * <p>
	 * [PalettizedColorSpaceMapper ncomps= 3, scomp= 1, nentries= 1024 column=0,
	 * 7 bit signed entry column=1, 7 bit unsigned entry column=2, 7 bit signed
	 * entry]
	 * <p>
	 */
	@Override
	public String toString()
	{

		int c;
		final StringBuffer rep = new StringBuffer("[PalettizedColorSpaceMapper ");
		final StringBuffer body = new StringBuffer("  " + ColorSpaceMapper.eol);

		if (null != pbox)
		{
			body.append("ncomps= ").append(this.getNumComps()).append(", scomp= ").append(this.srcChannel);
			for (c = 0; c < this.getNumComps(); ++c)
			{
				body.append(ColorSpaceMapper.eol).append("column= ").append(c).append(", ").append(this.pbox.getBitDepth(c)).append(" bit ")
						.append(this.pbox.isSigned(c) ? "signed entry" : "unsigned entry");
			}
		}
		else
		{
			body.append("image does not contain a palette box");
		}

		rep.append(ColorSpace.indent("  ", body));
		return rep.append("]").toString();
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
	 * Returns the number of bits, referred to as the "range bits",
	 * corresponding to the nominal range of the image data in the specified
	 * component. If this number is <i>n</b> then for unsigned data the nominal
	 * range is between 0 and 2^b-1, and for signed data it is between -2^(b-1)
	 * and 2^(b-1)-1. In the case of transformed data which is not in the image
	 * domain (e.g., wavelet coefficients), this method returns the "range bits"
	 * of the image data that generated the coefficients.
	 * 
	 * @param c
	 *            The index of the component.
	 * 
	 * @return The number of bits corresponding to the nominal range of the
	 *         image data (in the image domain).
	 */
	@Override
	public int getNomRangeBits(final int c)
	{
		return null == pbox ? this.src.getNomRangeBits(c) : this.pbox.getBitDepth(c);
	}

	/**
	 * Returns the number of components in the image.
	 * 
	 * @return The number of components in the image.
	 */
	@Override
	public int getNumComps()
	{
		return null == pbox ? this.src.getNumComps() : this.pbox.getNumColumns();
	}

	/**
	 * Returns the component subsampling factor in the horizontal direction, for
	 * the specified component. This is, approximately, the ratio of dimensions
	 * between the reference grid and the component itself, see the 'ImgData'
	 * interface desription for details.
	 * 
	 * @param c
	 *            The index of the component (between 0 and N-1)
	 * 
	 * @return The horizontal subsampling factor of component 'c'
	 * 
	 * @see dev.zontreck.harbinger.thirdparty.jj2000.j2k.image.ImgData
	 */
	@Override
	public int getCompSubsX(final int c)
	{
		return this.imgdatasrc.getCompSubsX(this.srcChannel);
	}

	/**
	 * Returns the component subsampling factor in the vertical direction, for
	 * the specified component. This is, approximately, the ratio of dimensions
	 * between the reference grid and the component itself, see the 'ImgData'
	 * interface desription for details.
	 * 
	 * @param c
	 *            The index of the component (between 0 and N-1)
	 * 
	 * @return The vertical subsampling factor of component 'c'
	 * 
	 * @see dev.zontreck.harbinger.thirdparty.jj2000.j2k.image.ImgData
	 */
	@Override
	public int getCompSubsY(final int c)
	{
		return this.imgdatasrc.getCompSubsY(this.srcChannel);
	}

	/**
	 * Returns the width in pixels of the specified tile-component
	 * 
	 * @param t
	 *            Tile index
	 * 
	 * @param c
	 *            The index of the component, from 0 to N-1.
	 * 
	 * @return The width in pixels of component <tt>c</tt> in tile<tt>t</tt>.
	 */
	@Override
	public int getTileCompWidth(final int t, final int c)
	{
		return this.imgdatasrc.getTileCompWidth(t, this.srcChannel);
	}

	/**
	 * Returns the height in pixels of the specified tile-component.
	 * 
	 * @param t
	 *            The tile index.
	 * 
	 * @param c
	 *            The index of the component, from 0 to N-1.
	 * 
	 * @return The height in pixels of component <tt>c</tt> in tile <tt>t</tt>.
	 */
	@Override
	public int getTileCompHeight(final int t, final int c)
	{
		return this.imgdatasrc.getTileCompHeight(t, this.srcChannel);
	}

	/**
	 * Returns the width in pixels of the specified component in the overall
	 * image.
	 * 
	 * @param c
	 *            The index of the component, from 0 to N-1.
	 * 
	 * @return The width in pixels of component <tt>c</tt> in the overall image.
	 */
	@Override
	public int getCompImgWidth(final int c)
	{
		return this.imgdatasrc.getCompImgWidth(this.srcChannel);
	}

	/**
	 * Returns the number of bits, referred to as the "range bits",
	 * corresponding to the nominal range of the image data in the specified
	 * component. If this number is <i>n</b> then for unsigned data the nominal
	 * range is between 0 and 2^b-1, and for signed data it is between -2^(b-1)
	 * and 2^(b-1)-1. In the case of transformed data which is not in the image
	 * domain (e.g., wavelet coefficients), this method returns the "range bits"
	 * of the image data that generated the coefficients.
	 * 
	 * @param c
	 *            The index of the component.
	 * 
	 * @return The number of bits corresponding to the nominal range of the
	 *         image data (in the image domain).
	 */
	@Override
	public int getCompImgHeight(final int c)
	{
		return this.imgdatasrc.getCompImgHeight(this.srcChannel);
	}

	/**
	 * Returns the horizontal coordinate of the upper-left corner of the
	 * specified component in the current tile.
	 * 
	 * @param c
	 *            The index of the component.
	 */
	@Override
	public int getCompULX(final int c)
	{
		return this.imgdatasrc.getCompULX(this.srcChannel);
	}

	/**
	 * Returns the vertical coordinate of the upper-left corner of the specified
	 * component in the current tile.
	 * 
	 * @param c
	 *            The index of the component.
	 */
	@Override
	public int getCompULY(final int c)
	{
		return this.imgdatasrc.getCompULY(this.srcChannel);
	}

	/* end class PalettizedColorSpaceMapper */
}
