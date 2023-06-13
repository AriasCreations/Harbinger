/*
 * CVS Identifier:
 *
 * $Id: InvCompTransf.java,v 1.19 2001/10/29 20:06:35 qtxjoas Exp $
 *
 * Class:               InvCompTransf
 *
 * Description:         Inverse Component transformations applied to tiles
 *
 *
 *
 * COPYRIGHT:
 * 
 * This software module was originally developed by Rapha�l Grosbois and
 * Diego Santa Cruz (Swiss Federal Institute of Technology-EPFL); Joel
 * Askel�f (Ericsson Radio Systems AB); and Bertrand Berthelot, David
 * Bouchard, F�lix Henry, Gerard Mozelle and Patrice Onno (Canon Research
 * Centre France S.A) in the course of development of the JPEG2000
 * standard as specified by ISO/IEC 15444 (JPEG 2000 Standard). This
 * software module is an implementation of a part of the JPEG 2000
 * Standard. Swiss Federal Institute of Technology-EPFL, Ericsson Radio
 * Systems AB and Canon Research Centre France S.A (collectively JJ2000
 * Partners) agree not to assert against ISO/IEC and users of the JPEG
 * 2000 Standard (Users) any of their rights under the copyright, not
 * including other intellectual property rights, for this software module
 * with respect to the usage by ISO/IEC and Users of this software module
 * or modifications thereof for use in hardware or software products
 * claiming conformance to the JPEG 2000 Standard. Those intending to use
 * this software module in hardware or software products are advised that
 * their use may infringe existing patents. The original developers of
 * this software module, JJ2000 Partners and ISO/IEC assume no liability
 * for use of this software module or modifications thereof. No license
 * or right to this software module is granted for non JPEG 2000 Standard
 * conforming products. JJ2000 Partners have full right to use this
 * software module for his/her own purpose, assign or donate this
 * software module to any third party and to inhibit third parties from
 * using this software module for non JPEG 2000 Standard conforming
 * products. This copyright notice must be included in all copies or
 * derivative works of this software module.
 * 
 * Copyright (c) 1999/2000 JJ2000 Partners.
 */
package jj2000.j2k.image.invcomptransf;

import jj2000.j2k.wavelet.synthesis.*;
import jj2000.j2k.decoder.*;
import jj2000.j2k.image.*;
import jj2000.j2k.util.*;
import jj2000.j2k.*;

/**
 * This class apply inverse component transformations to the tiles depending on
 * specification read from the codestream header. These transformations can be
 * used to improve compression efficiency but are not related to colour
 * transforms used to map colour values for display purposes. JPEG 2000 part I
 * defines 2 component transformations: RCT (Reversible Component
 * Transformation) and ICT (Irreversible Component Transformation).
 * 
 * @see ModuleSpec
 */
public class InvCompTransf extends ImgDataAdapter implements BlkImgDataSrc
{

	/** Identifier for no component transformation. Value is 0. */
	public static final int NONE = 0;

	/** The prefix for inverse component transformation options: 'M' */
	public static final char OPT_PREFIX = 'M';

	/**
	 * The list of parameters that is accepted by the inverse component
	 * transformation module. They start with 'M'.
	 */
	private static final String[][] pinfo = null;

	/**
	 * Identifier for the Inverse Reversible Component Transformation (INV_RCT).
	 * Value is 1.
	 */
	public static final int INV_RCT = 1;

	/**
	 * Identifier for the Inverse Irreversible Component Transformation
	 * (INV_ICT). Value is 2
	 */
	public static final int INV_ICT = 2;

	/** The source of image data */
	private final BlkImgDataSrc src;

	/** The component transformations specifications */
	private final CompTransfSpec cts;

	/** The wavelet filter specifications */
	private final SynWTFilterSpec wfs;

	/**
	 * The type of the current component transformation JPEG 2000 part I only
	 * support NONE, FORW_RCT and FORW_ICT types
	 */
	private int transfType = InvCompTransf.NONE;

	/** Buffer for each component of output data */
	private final int[][] outdata = new int[3][];

	/** Block used to request component 0 */
	private DataBlk block0;

	/** Block used to request component 1 */
	private DataBlk block1;

	/** Block used to request component 2 */
	private DataBlk block2;

	/**
	 * Data block used only to store coordinates and progressiveness of the
	 * buffered blocks
	 */
	private final DataBlkInt dbi = new DataBlkInt();

	/** The bit-depths of un-transformed components */
	private final int[] utdepth;

	/**
	 * Flag indicating whether the decoder should skip the component transform
	 */
	private boolean noCompTransf;

	/**
	 * Constructs a new ForwCompTransf object that operates on the specified
	 * source of image data.
	 * 
	 * @param imgSrc
	 *            The source from where to get the data to be transformed
	 * 
	 * @param decSpec
	 *            The decoder specifications
	 * 
	 * @param utdepth
	 *            The bit depth of the un-transformed components
	 * 
	 * @param pl
	 *            The command line optinons of the decoder
	 * 
	 * @see BlkImgDataSrc
	 */
	public InvCompTransf(final BlkImgDataSrc imgSrc, final DecoderSpecs decSpec, final int[] utdepth, final ParameterList pl)
	{
		super(imgSrc);
		cts = decSpec.cts;
		wfs = decSpec.wfs;
		this.src = imgSrc;
		this.utdepth = utdepth;
		this.noCompTransf = !(pl.getBooleanParameter("comp_transf"));
	}

	/**
	 * Returns the parameters that are used in this class and implementing
	 * classes. It returns a 2D String array. Each of the 1D arrays is for a
	 * different option, and they have 4 elements. The first element is the
	 * option name, the second one is the synopsis, the third one is a long
	 * description of what the parameter is and the fourth is its default value.
	 * The synopsis or description may be 'null', in which case it is assumed
	 * that there is no synopsis or description of the option, respectively.
	 * Null may be returned if no options are supported.
	 * 
	 * @return the options name, their synopsis and their explanation, or null
	 *         if no options are supported.
	 */
	public static String[][] getParameterInfo()
	{
		return InvCompTransf.pinfo;
	}

	/**
	 * Returns a string with a descriptive text of which inverse component
	 * transformation is used. This can be either "Inverse RCT" or "Inverse
	 * ICT" or "No component transformation" depending on the current tile.
	 * 
	 * @return A descriptive string
	 */
	@Override
	public String toString()
	{
		switch (this.transfType)
		{
			case InvCompTransf.INV_RCT:
				return "Inverse RCT";
			case InvCompTransf.INV_ICT:
				return "Inverse ICT";
			case InvCompTransf.NONE:
				return "No component transformation";
			default:
				throw new IllegalArgumentException("Non JPEG 2000 part I component transformation");
		}
	}

	/**
	 * Returns true if this transform is reversible in current tile. Reversible
	 * component transformations are those which operation can be completely
	 * reversed without any loss of information (not even due to rounding).
	 * 
	 * @return Reversibility of component transformation in current tile
	 */
	public boolean isReversible()
	{
		switch (this.transfType)
		{
			case InvCompTransf.NONE:
			case InvCompTransf.INV_RCT:
				return true;
			case InvCompTransf.INV_ICT:
				return false;
			default:
				throw new IllegalArgumentException("Non JPEG 2000 part I component transformation");
		}
	}

	/**
	 * Returns the position of the fixed point in the specified component. This
	 * is the position of the least significant integral (i.e. non-fractional)
	 * bit, which is equivalent to the number of fractional bits. For instance,
	 * for fixed-point values with 2 fractional bits, 2 is returned. For
	 * floating-point data this value does not apply and 0 should be returned.
	 * Position 0 is the position of the least significant bit in the data.
	 * 
	 * <P>
	 * This default implementation assumes that the number of fractional bits is
	 * not modified by the component mixer.
	 * 
	 * @param c
	 *            The index of the component.
	 * 
	 * @return The value of the fixed point position of the source since the
	 *         color transform does not affect it.
	 */
	@Override
	public int getFixedPoint(final int c)
	{
		return this.src.getFixedPoint(c);
	}

	/**
	 * Calculates the bitdepths of the transformed components, given the
	 * bitdepth of the un-transformed components and the component tranformation
	 * type.
	 * 
	 * @param utdepth
	 *            The bitdepth of each un-transformed component
	 * 
	 * @param ttype
	 *            The type ID of the inverse component transformation
	 * 
	 * @param tdepth
	 *            If not null the results are stored in this array, otherwise a
	 *            new array is allocated and returned.
	 * 
	 * @return The bitdepth of each transformed component.
	 */
	public static int[] calcMixedBitDepths(final int[] utdepth, final int ttype, int[] tdepth)
	{

		if (3 > utdepth.length && NONE != ttype)
		{
			throw new IllegalArgumentException();
		}

		if (null == tdepth)
		{
			tdepth = new int[utdepth.length];
		}

		switch (ttype)
		{
			case InvCompTransf.NONE:
				System.arraycopy(utdepth, 0, tdepth, 0, utdepth.length);
				break;
			case InvCompTransf.INV_RCT:
				if (3 < utdepth.length)
				{
					System.arraycopy(utdepth, 3, tdepth, 3, utdepth.length - 3);
				}
				// The formulas are:
				// tdepth[0] = ceil(log2(2^(utdepth[0])+2^utdepth[1]+
				// 2^(utdepth[2])))-2+1
				// tdepth[1] = ceil(log2(2^(utdepth[0])+2^(utdepth[1])-1))+1
				// tdepth[2] = ceil(log2(2^(utdepth[1])+2^(utdepth[2])-1))+1
				// The MathUtil.log2(x) function calculates floor(log2(x)), so
				// we
				// use 'MathUtil.log2(2*x-1)+1', which calculates ceil(log2(x))
				// for any x>=1, x integer.
				tdepth[0] = MathUtil.log2((1 << utdepth[0]) + (2 << utdepth[1]) + (1 << utdepth[2]) - 1) - 2 + 1;
				tdepth[1] = MathUtil.log2((1 << utdepth[2]) + (1 << utdepth[1]) - 1) + 1;
				tdepth[2] = MathUtil.log2((1 << utdepth[0]) + (1 << utdepth[1]) - 1) + 1;
				break;
			case InvCompTransf.INV_ICT:
				if (3 < utdepth.length)
				{
					System.arraycopy(utdepth, 3, tdepth, 3, utdepth.length - 3);
				}
				// The MathUtil.log2(x) function calculates floor(log2(x)), so
				// we
				// use 'MathUtil.log2(2*x-1)+1', which calculates ceil(log2(x))
				// for any x>=1, x integer.
				tdepth[0] = MathUtil.log2((int) Math.floor((1 << utdepth[0]) * 0.299072 + (1 << utdepth[1]) * 0.586914
						+ (1 << utdepth[2]) * 0.114014) - 1) + 1;
				tdepth[1] = MathUtil.log2((int) Math.floor((1 << utdepth[0]) * 0.168701 + (1 << utdepth[1]) * 0.331299
						+ (1 << utdepth[2]) * 0.5) - 1) + 1;
				tdepth[2] = MathUtil.log2((int) Math.floor((1 << utdepth[0]) * 0.5 + (1 << utdepth[1]) * 0.418701
						+ (1 << utdepth[2]) * 0.081299) - 1) + 1;
				break;
			default:
				throw new IllegalArgumentException("unhandled tranfomation type " + ttype);
		}

		return tdepth;
	}

	/**
	 * Returns the number of bits, referred to as the "range bits",
	 * corresponding to the nominal range of the data in the specified
	 * component. If this number is <i>b</b> then for unsigned data the nominal
	 * range is between 0 and 2^b-1, and for signed data it is between -2^(b-1)
	 * and 2^(b-1)-1.
	 * 
	 * @param c
	 *            The index of the component.
	 * 
	 * @return The bitdepth of un-transformed component 'c'.
	 */
	@Override
	public int getNomRangeBits(final int c)
	{
		return this.utdepth[c];
	}

	/**
	 * Apply inverse component transformation associated with the current tile.
	 * If no component transformation has been requested by the user, data are
	 * not modified.
	 * 
	 * <P>
	 * This method calls the getInternCompData() method, but respects the
	 * definitions of the getCompData() method defined in the BlkImgDataSrc
	 * interface.
	 * 
	 * @param blk
	 *            Determines the rectangular area to return, and the data is
	 *            returned in this object.
	 * 
	 * @param c
	 *            Index of the output component.
	 * 
	 * @return The requested DataBlk
	 *
	 */
	@Override
	public DataBlk getCompData(final DataBlk blk, final int c)
	{
		// If requesting a component whose index is greater than 3 or there is
		// no transform return a copy of data (getInternCompData returns the
		// actual data in those cases)
		if (3 <= c || NONE == transfType || this.noCompTransf)
		{
			return this.src.getCompData(blk, c);
		}
		// We can use getInternCompData (since data is a copy anyways)
		return this.getInternCompData(blk, c);
	}

	/**
	 * Apply the inverse component transformation associated with the current
	 * tile. If no component transformation has been requested by the user, data
	 * are not modified. Else, appropriate method is called (invRCT or invICT).
	 * 
	 * @see #invRCT
	 * 
	 * @see #invICT
	 * 
	 * @param blk
	 *            Determines the rectangular area to return.
	 * 
	 * @param c
	 *            Index of the output component.
	 * 
	 * @return The requested DataBlk
	 */
	@Override
	public DataBlk getInternCompData(final DataBlk blk, final int c)
	{
		// if specified in the command line that no component transform should
		// be made, return original data
		if (this.noCompTransf)
			return this.src.getInternCompData(blk, c);

		switch (this.transfType)
		{
			case InvCompTransf.NONE:
				return this.src.getInternCompData(blk, c);

			case InvCompTransf.INV_RCT:
				return this.invRCT(blk, c);
			case InvCompTransf.INV_ICT:
				return this.invICT(blk, c);
			default:
				throw new IllegalArgumentException("Non JPEG 2000 part I component transformation");
		}
	}

	/**
	 * Apply inverse component transformation to obtain requested component from
	 * specified block of data. Whatever the type of requested DataBlk, it
	 * always returns a DataBlkInt.
	 * 
	 * @param blk
	 *            Determine the rectangular area to return
	 * 
	 * @param c
	 *            The index of the requested component
	 * 
	 * @return Data of requested component
	 */
	private DataBlk invRCT(final DataBlk blk, final int c)
	{
		// If the component number is three or greater, return original data
		if (3 <= c && c < this.getNumComps())
		{
			// Requesting a component whose index is greater than 3
			return this.src.getInternCompData(blk, c);
		}
		// If asking a component for the first time for this block,
		// do transform for the 3 components
		else if ((null == outdata[c]) || (this.dbi.ulx > blk.ulx) || (this.dbi.uly > blk.uly)
				|| (this.dbi.ulx + this.dbi.w < blk.ulx + blk.w) || (this.dbi.uly + this.dbi.h < blk.uly + blk.h))
		{
			int k, k0, k1, k2, mink, i;
			final int w = blk.w; // width of output block
			final int h = blk.h; // height of ouput block

			// Reference to output block data array
			this.outdata[c] = (int[]) blk.getData();

			// Create data array of blk if necessary
			if (null == outdata[c] || this.outdata[c].length != h * w)
			{
				this.outdata[c] = new int[h * w];
				blk.setData(this.outdata[c]);
			}

			this.outdata[(c + 1) % 3] = new int[this.outdata[c].length];
			this.outdata[(c + 2) % 3] = new int[this.outdata[c].length];

			if (null == block0 || DataBlk.TYPE_INT != block0.getDataType())
				this.block0 = new DataBlkInt();
			if (null == block1 || DataBlk.TYPE_INT != block1.getDataType())
				this.block1 = new DataBlkInt();
			if (null == block2 || DataBlk.TYPE_INT != block2.getDataType())
				this.block2 = new DataBlkInt();
			this.block0.w = this.block1.w = this.block2.w = blk.w;
			this.block0.h = this.block1.h = this.block2.h = blk.h;
			this.block0.ulx = this.block1.ulx = this.block2.ulx = blk.ulx;
			this.block0.uly = this.block1.uly = this.block2.uly = blk.uly;

			final int[] data0;  // input data arrays
			int[] data1;
			final int[] data2;

			// Fill in buffer blocks (to be read only)
			// Returned blocks may have different size and position
			this.block0 = this.src.getInternCompData(this.block0, 0);
			data0 = (int[]) this.block0.getData();
			this.block1 = this.src.getInternCompData(this.block1, 1);
			data1 = (int[]) this.block1.getData();
			this.block2 = this.src.getInternCompData(this.block2, 2);
			data2 = (int[]) this.block2.getData();

			// Set the progressiveness of the output data
			blk.progressive = this.block0.progressive || this.block1.progressive || this.block2.progressive;
			blk.offset = 0;
			blk.scanw = w;

			// set attributes of the DataBlk used for buffering
			this.dbi.progressive = blk.progressive;
			this.dbi.ulx = blk.ulx;
			this.dbi.uly = blk.uly;
			this.dbi.w = blk.w;
			this.dbi.h = blk.h;

			// Perform conversion

			// Initialize general indexes
			k = w * h - 1;
			k0 = this.block0.offset + (h - 1) * this.block0.scanw + w - 1;
			k1 = this.block1.offset + (h - 1) * this.block1.scanw + w - 1;
			k2 = this.block2.offset + (h - 1) * this.block2.scanw + w - 1;

			for (i = h - 1; 0 <= i; i--)
			{
				for (mink = k - w; k > mink; k--, k0--, k1--, k2--)
				{
					this.outdata[1][k] = (data0[k0] - ((data1[k1] + data2[k2]) >> 2));
					this.outdata[0][k] = data2[k2] + this.outdata[1][k];
					this.outdata[2][k] = data1[k1] + this.outdata[1][k];
				}
				// Jump to beggining of previous line in input
				k0 -= this.block0.scanw - w;
				k1 -= this.block1.scanw - w;
				k2 -= this.block2.scanw - w;
			}
			this.outdata[c] = null;
		}
		else if ((0 <= c) && (3 > c))
		{ // Asking for the 2nd or 3rd block component
			blk.setData(this.outdata[c]);
			blk.progressive = this.dbi.progressive;
			blk.offset = (blk.uly - this.dbi.uly) * this.dbi.w + blk.ulx - this.dbi.ulx;
			blk.scanw = this.dbi.w;
			this.outdata[c] = null;
		}
		else
		{
			// Requesting a non valid component index
			throw new IllegalArgumentException();
		}
		return blk;
	}

	/**
	 * Apply inverse irreversible component transformation to obtain requested
	 * component from specified block of data. Whatever the type of requested
	 * DataBlk, it always returns a DataBlkFloat.
	 * 
	 * @param blk
	 *            Determine the rectangular area to return
	 * 
	 * @param c
	 *            The index of the requested component
	 * 
	 * @return Data of requested component
	 */
	private DataBlk invICT(final DataBlk blk, final int c)
	{
		if (3 <= c && c < this.getNumComps())
		{
			// Requesting a component whose index is greater than 3
			int k, k0, mink, i;
			final int w = blk.w; // width of output block
			final int h = blk.h; // height of ouput block

			int[] outdata; // array of output data

			// Reference to output block data array
			outdata = (int[]) blk.getData();

			// Create data array of blk if necessary
			if (null == outdata)
			{
				outdata = new int[h * w];
				blk.setData(outdata);
			}

			// Variables
			final DataBlkFloat indb = new DataBlkFloat(blk.ulx, blk.uly, w, h);
			final float[] indata; // input data array

			// Get the input data
			// (returned block may be larger than requested one)
			this.src.getInternCompData(indb, c);
			indata = (float[]) indb.getData();

			// Copy the data converting from int to int
			k = w * h - 1;
			k0 = indb.offset + (h - 1) * indb.scanw + w - 1;
			for (i = h - 1; 0 <= i; i--)
			{
				for (mink = k - w; k > mink; k--, k0--)
				{
					outdata[k] = (int) (indata[k0]);
				}
				// Jump to beggining of previous line in input
				k0 -= indb.scanw - w;
			}

			// Set the progressivity and offset
			blk.progressive = indb.progressive;
			blk.offset = 0;
			blk.scanw = w;
		}

		// If asking a component for the first time for this block,
		// do transform for the 3 components
		else if ((null == outdata[c]) || (this.dbi.ulx > blk.ulx) || (this.dbi.uly > blk.uly)
				|| (this.dbi.ulx + this.dbi.w < blk.ulx + blk.w) || (this.dbi.uly + this.dbi.h < blk.uly + blk.h))
		{
			int k, k0, k1, k2, mink, i;
			final int w = blk.w; // width of output block
			final int h = blk.h; // height of ouput block

			// Reference to output block data array
			this.outdata[c] = (int[]) blk.getData();

			// Create data array of blk if necessary
			if (null == outdata[c] || this.outdata[c].length != w * h)
			{
				this.outdata[c] = new int[h * w];
				blk.setData(this.outdata[c]);
			}

			this.outdata[(c + 1) % 3] = new int[this.outdata[c].length];
			this.outdata[(c + 2) % 3] = new int[this.outdata[c].length];

			if (null == block0 || DataBlk.TYPE_FLOAT != block0.getDataType())
				this.block0 = new DataBlkFloat();
			if (null == block2 || DataBlk.TYPE_FLOAT != block2.getDataType())
				this.block2 = new DataBlkFloat();
			if (null == block1 || DataBlk.TYPE_FLOAT != block1.getDataType())
				this.block1 = new DataBlkFloat();
			this.block0.w = this.block2.w = this.block1.w = blk.w;
			this.block0.h = this.block2.h = this.block1.h = blk.h;
			this.block0.ulx = this.block2.ulx = this.block1.ulx = blk.ulx;
			this.block0.uly = this.block2.uly = this.block1.uly = blk.uly;

			final float[] data0;  // input data arrays
			float[] data1;
			final float[] data2;

			// Fill in buffer blocks (to be read only)
			// Returned blocks may have different size and position
			this.block0 = this.src.getInternCompData(this.block0, 0);
			data0 = (float[]) this.block0.getData();
			this.block2 = this.src.getInternCompData(this.block2, 1);
			data2 = (float[]) this.block2.getData();
			this.block1 = this.src.getInternCompData(this.block1, 2);
			data1 = (float[]) this.block1.getData();

			// Set the progressiveness of the output data
			blk.progressive = this.block0.progressive || this.block1.progressive || this.block2.progressive;
			blk.offset = 0;
			blk.scanw = w;

			// set attributes of the DataBlk used for buffering
			this.dbi.progressive = blk.progressive;
			this.dbi.ulx = blk.ulx;
			this.dbi.uly = blk.uly;
			this.dbi.w = blk.w;
			this.dbi.h = blk.h;

			// Perform conversion

			// Initialize general indexes
			k = w * h - 1;
			k0 = this.block0.offset + (h - 1) * this.block0.scanw + w - 1;
			k2 = this.block2.offset + (h - 1) * this.block2.scanw + w - 1;
			k1 = this.block1.offset + (h - 1) * this.block1.scanw + w - 1;

			for (i = h - 1; 0 <= i; i--)
			{
				for (mink = k - w; k > mink; k--, k0--, k2--, k1--)
				{
					this.outdata[0][k] = (int) (data0[k0] + 1.402f * data1[k1] + 0.5f);
					this.outdata[1][k] = (int) (data0[k0] - 0.34413f * data2[k2] - 0.71414f * data1[k1] + 0.5f);
					this.outdata[2][k] = (int) (data0[k0] + 1.772f * data2[k2] + 0.5f);
				}
				// Jump to beggining of previous line in input
				k0 -= this.block0.scanw - w;
				k2 -= this.block2.scanw - w;
				k1 -= this.block1.scanw - w;
			}
			this.outdata[c] = null;
		}
		else if ((0 <= c) && (3 >= c))
		{// Asking for the 2nd or 3rd block component
			blk.setData(this.outdata[c]);
			blk.progressive = this.dbi.progressive;
			blk.offset = (blk.uly - this.dbi.uly) * this.dbi.w + blk.ulx - this.dbi.ulx;
			blk.scanw = this.dbi.w;
			this.outdata[c] = null;
		}
		else
		{
			// Requesting a non valid component index
			throw new IllegalArgumentException();
		}
		return blk;
	}

	/**
	 * Changes the current tile, given the new indexes. An
	 * IllegalArgumentException is thrown if the indexes do not correspond to a
	 * valid tile.
	 * 
	 * <P>
	 * This default implementation changes the tile in the source and
	 * re-initializes properly component transformation variables..
	 * 
	 * @param x
	 *            The horizontal index of the tile.
	 * 
	 * @param y
	 *            The vertical index of the new tile.
	 * 
	 * @return The new tile index
	 */
	@Override
	public int setTile(final int x, final int y)
	{
		this.tIdx = this.src.setTile(x, y);

		// initializations
		if (NONE == ((Integer) cts.getTileDef(tIdx)).intValue())
			this.transfType = InvCompTransf.NONE;
		else
		{
			final int nc = 3 < src.getNumComps() ? 3 : this.src.getNumComps();
			int rev = 0;
			for (int c = 0; c < nc; c++)
			{
				rev += (this.wfs.isReversible(this.tIdx, c) ? 1 : 0);
			}
			if (3 == rev)
			{
				// All WT are reversible
				this.transfType = InvCompTransf.INV_RCT;
			}
			else if (0 == rev)
			{
				// All WT irreversible
				this.transfType = InvCompTransf.INV_ICT;
			}
			else
			{
				// Error
				throw new IllegalArgumentException("Wavelet transformation and component transformation"
						+ " not coherent in tile" + this.tIdx);
			}
		}
		return this.tIdx;
	}

	/**
	 * Advances to the next tile, in standard scan-line order (by rows then
	 * columns). An NoNextElementException is thrown if the current tile is the
	 * last one (i.e. there is no next tile).
	 * 
	 * <p>
	 * This default implementation just advances to the next tile in the source
	 * and re-initializes properly component transformation variables.
	 * 
	 * @return The new tile index
	 */
	@Override
	public int nextTile()
	{
		this.tIdx = this.src.nextTile();

		// initializations
		if (NONE == ((Integer) cts.getTileDef(tIdx)).intValue())
			this.transfType = InvCompTransf.NONE;
		else
		{
			final int nc = 3 < src.getNumComps() ? 3 : this.src.getNumComps();
			int rev = 0;
			for (int c = 0; c < nc; c++)
			{
				rev += (this.wfs.isReversible(this.tIdx, c) ? 1 : 0);
			}
			if (3 == rev)
			{
				// All WT are reversible
				this.transfType = InvCompTransf.INV_RCT;
			}
			else if (0 == rev)
			{
				// All WT irreversible
				this.transfType = InvCompTransf.INV_ICT;
			}
			else
			{
				// Error
				throw new IllegalArgumentException("Wavelet transformation and component transformation"
						+ " not coherent in tile" + this.tIdx);
			}
		}
		return this.tIdx;
	}

}
