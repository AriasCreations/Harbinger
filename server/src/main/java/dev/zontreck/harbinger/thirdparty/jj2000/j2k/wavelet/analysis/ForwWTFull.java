/*
 * CVS identifier:
 *
 * $Id: ForwWTFull.java,v 1.30 2001/09/20 12:42:59 grosbois Exp $
 *
 * Class:                   ForwWTFull
 *
 * Description:             This class implements the full page
 *                          forward wavelet transform for both integer
 *                          and floating point implementations.
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
package dev.zontreck.harbinger.thirdparty.jj2000.j2k.wavelet.analysis;

import dev.zontreck.harbinger.thirdparty.jj2000.j2k.IntegerSpec;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.ModuleSpec;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.codestream.Markers;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.encoder.EncoderSpecs;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.entropy.CBlkSizeSpec;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.entropy.PrecinctSizeSpec;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.image.*;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.util.MathUtil;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.wavelet.Subband;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.wavelet.WaveletTransform;

/**
 * This class implements the ForwardWT abstract class with the full-page
 * approach to be used either with integer or floating-point filters
 *
 * @see ForwardWT
 */
public class ForwWTFull extends ForwardWT {
	/**
	 * Boolean to know if one are currently dealing with int or float data.
	 */
	private boolean intData;

	/**
	 * The subband trees of each tile-component. The array is allocated by the
	 * constructor of this class and updated by the getAnSubbandTree() method
	 * when needed. The first index is the tile index (in lexicographical order)
	 * and the second index is the component index.
	 *
	 * <p>
	 * The subband tree for a component in the current tile is created on the
	 * first call to getAnSubbandTree() for that component, in the current tile.
	 * Before that, the element in 'subbTrees' is null.
	 */
	private final SubbandAn[][] subbTrees;

	/**
	 * The source of image data
	 */
	private final BlkImgDataSrc src;

	/**
	 * The horizontal coordinate of the code-block partition origin on the
	 * reference grid
	 */
	private final int cb0x;

	/**
	 * The vertical coordinate of the code-block partition on the reference grid
	 */
	private final int cb0y;

	/**
	 * The number of decomposition levels specification
	 */
	private final IntegerSpec dls;

	/**
	 * Wavelet filters for all components and tiles
	 */
	private final AnWTFilterSpec filters;

	/**
	 * The code-block size specifications
	 */
	private final CBlkSizeSpec cblks;

	/**
	 * The precinct partition specifications
	 */
	private final PrecinctSizeSpec pss;

	/**
	 * Block storing the full band decomposition for each component.
	 */
	private final DataBlk[] decomposedComps;

	/**
	 * The horizontal index of the last "sent" code-block in the current subband
	 * in each component. It should be -1 if none have been sent yet.
	 */
	private final int[] lastn;

	/**
	 * The vertical index of the last "sent" code-block in the current subband
	 * in each component. It should be 0 if none have been sent yet.
	 */
	private final int[] lastm;

	/**
	 * The subband being dealt with in each component
	 */
	SubbandAn[] currentSubband;

	/**
	 * Cache object to avoid excessive allocation/deallocation. This variable
	 * makes the class inheritently thread unsafe.
	 */
	Coord ncblks;

	/**
	 * Initializes this object with the given source of image data and with all
	 * the decompositon parameters
	 *
	 * @param src     From where the image data should be obtained.
	 * @param encSpec The encoder specifications
	 * @param pox     The horizontal coordinate of the cell and code-block partition
	 *                origin with respect to the canvas origin, on the reference grid.
	 * @param poy     The vertical coordinate of the cell and code-block partition
	 *                origin with respect to the canvas origin, on the reference grid.
	 * @see ForwardWT
	 */
	public ForwWTFull(final BlkImgDataSrc src, final EncoderSpecs encSpec, final int pox, final int poy) {
		super(src);
		this.src = src;
		cb0x = pox;
		cb0y = poy;
		dls = encSpec.dls;
		filters = encSpec.wfs;
		cblks = encSpec.cblks;
		pss = encSpec.pss;

		final int ncomp = src.getNumComps();
		final int ntiles = src.getNumTiles();

		this.currentSubband = new SubbandAn[ncomp];
		this.decomposedComps = new DataBlk[ncomp];
		this.subbTrees = new SubbandAn[ntiles][ncomp];
		this.lastn = new int[ncomp];
		this.lastm = new int[ncomp];
	}

	/**
	 * Returns the implementation type of this wavelet transform, WT_IMPL_FULL
	 * (full-page based transform). All components return the same.
	 *
	 * @param c The index of the component.
	 * @return WT_IMPL_FULL
	 */
	@Override
	public int getImplementationType(final int c) {
		return WaveletTransform.WT_IMPL_FULL;
	}

	/**
	 * Returns the number of decomposition levels that are applied to the LL
	 * band, in the specified tile-component. A value of 0 means that no wavelet
	 * transform is applied.
	 *
	 * @param t The tile index
	 * @param c The index of the component.
	 * @return The number of decompositions applied to the LL band (0 for no
	 * wavelet transform).
	 */
	@Override
	public int getDecompLevels(final int t, final int c) {
		return ((Integer) this.dls.getTileCompVal(t, c)).intValue();
	}

	/**
	 * Returns the wavelet tree decomposition. Actually JPEG 2000 part 1 only
	 * supports WT_DECOMP_DYADIC decomposition.
	 *
	 * @param t The tile-index
	 * @param c The index of the component.
	 * @return The wavelet decomposition.
	 */
	@Override
	public int getDecomp(final int t, final int c) {
		return ForwardWT.WT_DECOMP_DYADIC;
	}

	/**
	 * Returns the horizontal analysis wavelet filters used in each level, for
	 * the specified component and tile. The first element in the array is the
	 * filter used to obtain the lowest resolution (resolution level 0) subbands
	 * (i.e. lowest frequency LL subband), the second element is the one used to
	 * generate the resolution level 1 subbands, and so on. If there are less
	 * elements in the array than the number of resolution levels, then the last
	 * one is assumed to repeat itself.
	 *
	 * <p>
	 * The returned filters are applicable only to the specified component and
	 * in the current tile.
	 *
	 * <p>
	 * The resolution level of a subband is the resolution level to which a
	 * subband contributes, which is different from its decomposition level.
	 *
	 * @param t The index of the tile for which to return the filters.
	 * @param c The index of the component for which to return the filters.
	 * @return The horizontal analysis wavelet filters used in each level.
	 */
	@Override
	public AnWTFilter[] getHorAnWaveletFilters(final int t, final int c) {
		return this.filters.getHFilters(t, c);
	}

	/**
	 * Returns the vertical analysis wavelet filters used in each level, for the
	 * specified component and tile. The first element in the array is the
	 * filter used to obtain the lowest resolution (resolution level 0) subbands
	 * (i.e. lowest frequency LL subband), the second element is the one used to
	 * generate the resolution level 1 subbands, and so on. If there are less
	 * elements in the array than the number of resolution levels, then the last
	 * one is assumed to repeat itself.
	 *
	 * <p>
	 * The returned filters are applicable only to the specified component and
	 * in the current tile.
	 *
	 * <p>
	 * The resolution level of a subband is the resolution level to which a
	 * subband contributes, which is different from its decomposition level.
	 *
	 * @param t The index of the tile for which to return the filters.
	 * @param c The index of the component for which to return the filters.
	 * @return The vertical analysis wavelet filters used in each level.
	 */
	@Override
	public AnWTFilter[] getVertAnWaveletFilters(final int t, final int c) {
		return this.filters.getVFilters(t, c);
	}

	/**
	 * Returns the reversibility of the wavelet transform for the specified
	 * component and tile. A wavelet transform is reversible when it is suitable
	 * for lossless and lossy-to-lossless compression.
	 *
	 * @param t The index of the tile.
	 * @param c The index of the component.
	 * @return true is the wavelet transform is reversible, false if not.
	 */
	@Override
	public boolean isReversible(final int t, final int c) {
		return this.filters.isReversible(t, c);
	}

	/**
	 * Returns the horizontal offset of the code-block partition. Allowable
	 * values are 0 and 1, nothing else.
	 */
	@Override
	public int getCbULX() {
		return this.cb0x;
	}

	/**
	 * Returns the vertical offset of the code-block partition. Allowable values
	 * are 0 and 1, nothing else.
	 */
	@Override
	public int getCbULY() {
		return this.cb0y;
	}

	/**
	 * Returns the position of the fixed point in the specified component. This
	 * is the position of the least significant integral (i.e. non-fractional)
	 * bit, which is equivalent to the number of fractional bits. For instance,
	 * for fixed-point values with 2 fractional bits, 2 is returned. For
	 * floating-point data this value does not apply and 0 should be returned.
	 * Position 0 is the position of the least significant bit in the data.
	 *
	 * @param c The index of the component.
	 * @return The position of the fixed-point, which is the same as the number
	 * of fractional bits. For floating-point data 0 is returned.
	 */
	@Override
	public int getFixedPoint(final int c) {
		return this.src.getFixedPoint(c);
	}

	/**
	 * Returns the next code-block in the current tile for the specified
	 * component. The order in which code-blocks are returned is not specified.
	 * However each code-block is returned only once and all code-blocks will be
	 * returned if the method is called 'N' times, where 'N' is the number of
	 * code-blocks in the tile. After all the code-blocks have been returned for
	 * the current tile calls to this method will return 'null'.
	 *
	 * <p>
	 * When changing the current tile (through 'setTile()' or 'nextTile()') this
	 * method will always return the first code-block, as if this method was
	 * never called before for the new current tile.
	 *
	 * <p>
	 * The data returned by this method is the data in the internal buffer of
	 * this object, and thus can not be modified by the caller. The 'offset' and
	 * 'scanw' of the returned data have, in general, some non-zero value. The
	 * 'magbits' of the returned data is not set by this method and should be
	 * ignored. See the 'CBlkWTData' class.
	 *
	 * <p>
	 * The 'ulx' and 'uly' members of the returned 'CBlkWTData' object contain
	 * the coordinates of the top-left corner of the block, with respect to the
	 * tile, not the subband.
	 *
	 * @param c    The component for which to return the next code-block.
	 * @param cblk If non-null this object will be used to return the new
	 *             code-block. If null a new one will be allocated and returned.
	 * @return The next code-block in the current tile for component 'n', or
	 * null if all code-blocks for the current tile have been returned.
	 * @see CBlkWTData
	 */
	@Override
	public CBlkWTData getNextInternCodeBlock(final int c, CBlkWTData cblk) {
		final int cbm;
		int cbn;
		int cn;
		final int cm;
		int acb0x, acb0y;
		final SubbandAn sb;
		this.intData = (DataBlk.TYPE_INT == filters.getWTDataType(tIdx, c));

		// If the source image has not been decomposed
		if (null == decomposedComps[c]) {
			int k;
			int w;
			final int h;
			DataBlk bufblk;
			final Object dst_data;

			w = this.getTileCompWidth(this.tIdx, c);
			h = this.getTileCompHeight(this.tIdx, c);

			// Get the source image data
			if (this.intData) {
				this.decomposedComps[c] = new DataBlkInt(0, 0, w, h);
				bufblk = new DataBlkInt();
			} else {
				this.decomposedComps[c] = new DataBlkFloat(0, 0, w, h);
				bufblk = new DataBlkFloat();
			}

			// Get data from source line by line (this diminishes the memory
			// requirements on the data source)
			dst_data = this.decomposedComps[c].getData();
			final int lstart = this.getCompULX(c);
			bufblk.ulx = lstart;
			bufblk.w = w;
			bufblk.h = 1;
			int kk = this.getCompULY(c);
			for (k = 0; k < h; k++, kk++) {
				bufblk.uly = kk;
				bufblk.ulx = lstart;
				bufblk = this.src.getInternCompData(bufblk, c);
				System.arraycopy(bufblk.getData(), bufblk.offset, dst_data, k * w, w);
			}

			// Decompose source image
			this.waveletTreeDecomposition(this.decomposedComps[c], this.getAnSubbandTree(this.tIdx, c), c);

			// Make the first subband the current one
			this.currentSubband[c] = this.getNextSubband(c);

			this.lastn[c] = -1;
			this.lastm[c] = 0;
		}

		// Get the next code-block to "send"
		do {
			// Calculate number of code-blocks in current subband
			this.ncblks = this.currentSubband[c].numCb;
			// Goto next code-block
			this.lastn[c]++;
			if (this.lastn[c] == this.ncblks.x) { // Got to end of this row of
				// code-blocks
				this.lastn[c] = 0;
				this.lastm[c]++;
			}
			if (this.lastm[c] < this.ncblks.y) {
				// Not past the last code-block in the subband, we can return
				// this code-block
				break;
			}
			// If we get here we already sent all code-blocks in this subband,
			// goto next subband
			this.currentSubband[c] = this.getNextSubband(c);
			this.lastn[c] = -1;
			this.lastm[c] = 0;
			if (null == currentSubband[c]) {
				// We don't need the transformed data any more (a priori)
				this.decomposedComps[c] = null;
				// All code-blocks from all subbands in the current
				// tile have been returned so we return a null
				// reference
				return null;
			}
			// Loop to find the next code-block
		} while (true);

		// Project code-block partition origin to subband. Since the origin is
		// always 0 or 1, it projects to the low-pass side (throught the ceil
		// operator) as itself (i.e. no change) and to the high-pass side
		// (through the floor operator) as 0, always.
		acb0x = this.cb0x;
		acb0y = this.cb0y;
		switch (this.currentSubband[c].sbandIdx) {
			case Subband.WT_ORIENT_LL:
				// No need to project since all low-pass => nothing to do
				break;
			case Subband.WT_ORIENT_HL:
				acb0x = 0;
				break;
			case Subband.WT_ORIENT_LH:
				acb0y = 0;
				break;
			case Subband.WT_ORIENT_HH:
				acb0x = 0;
				acb0y = 0;
				break;
			default:
				throw new Error("Internal JJ2000 error");
		}
		// Initialize output code-block
		if (null == cblk) {
			if (this.intData) {
				cblk = new CBlkWTDataInt();
			} else {
				cblk = new CBlkWTDataFloat();
			}
		}
		cbn = this.lastn[c];
		cbm = this.lastm[c];
		sb = this.currentSubband[c];
		cblk.n = cbn;
		cblk.m = cbm;
		cblk.sb = sb;
		// Calculate the indexes of first code-block in subband with respect
		// to the partitioning origin, to then calculate the position and size
		// NOTE: when calculating "floor()" by integer division the dividend
		// and divisor must be positive, we ensure that by adding the divisor
		// to the dividend and then substracting 1 to the result of the
		// division
		cn = (sb.ulcx - acb0x + sb.nomCBlkW) / sb.nomCBlkW - 1;
		cm = (sb.ulcy - acb0y + sb.nomCBlkH) / sb.nomCBlkH - 1;
		if (0 == cbn) { // Left-most code-block, starts where subband starts
			cblk.ulx = sb.ulx;
		} else {
			// Calculate starting canvas coordinate and convert to subb. coords
			cblk.ulx = (cn + cbn) * sb.nomCBlkW - (sb.ulcx - acb0x) + sb.ulx;
		}
		if (0 == cbm) { // Bottom-most code-block, starts where subband starts
			cblk.uly = sb.uly;
		} else {
			cblk.uly = (cm + cbm) * sb.nomCBlkH - (sb.ulcy - acb0y) + sb.uly;
		}
		if (cbn < this.ncblks.x - 1) {
			// Calculate where next code-block starts => width
			cblk.w = (cn + cbn + 1) * sb.nomCBlkW - (sb.ulcx - acb0x) + sb.ulx - cblk.ulx;
		} else { // Right-most code-block, ends where subband ends
			cblk.w = sb.ulx + sb.w - cblk.ulx;
		}
		if (cbm < this.ncblks.y - 1) {
			// Calculate where next code-block starts => height
			cblk.h = (cm + cbm + 1) * sb.nomCBlkH - (sb.ulcy - acb0y) + sb.uly - cblk.uly;
		} else { // Bottom-most code-block, ends where subband ends
			cblk.h = sb.uly + sb.h - cblk.uly;
		}
		cblk.wmseScaling = 1.0f;

		// Since we are in getNextInternCodeBlock() we can return a
		// reference to the internal buffer, no need to copy. Just initialize
		// the 'offset' and 'scanw'
		cblk.offset = cblk.uly * this.decomposedComps[c].w + cblk.ulx;
		cblk.scanw = this.decomposedComps[c].w;

		// For the data just put a reference to our buffer
		cblk.setData(this.decomposedComps[c].getData());
		// Return code-block
		return cblk;
	}

	/**
	 * Returns the next code-block in the current tile for the specified
	 * component, as a copy (see below). The order in which code-blocks are
	 * returned is not specified. However each code-block is returned only once
	 * and all code-blocks will be returned if the method is called 'N' times,
	 * where 'N' is the number of code-blocks in the tile. After all the
	 * code-blocks have been returned for the current tile calls to this method
	 * will return 'null'.
	 *
	 * <p>
	 * When changing the current tile (through 'setTile()' or 'nextTile()') this
	 * method will always return the first code-block, as if this method was
	 * never called before for the new current tile.
	 *
	 * <p>
	 * The data returned by this method is always a copy of the internal data of
	 * this object, and it can be modified "in place" without any problems after
	 * being returned. The 'offset' of the returned data is 0, and the 'scanw'
	 * is the same as the code-block width. The 'magbits' of the returned data
	 * is not set by this method and should be ignored. See the 'CBlkWTData'
	 * class.
	 *
	 * <p>
	 * The 'ulx' and 'uly' members of the returned 'CBlkWTData' object contain
	 * the coordinates of the top-left corner of the block, with respect to the
	 * tile, not the subband.
	 *
	 * @param c    The component for which to return the next code-block.
	 * @param cblk If non-null this object will be used to return the new
	 *             code-block. If null a new one will be allocated and returned.
	 *             If the "data" array of the object is non-null it will be
	 *             reused, if possible, to return the data.
	 * @return The next code-block in the current tile for component 'c', or
	 * null if all code-blocks for the current tile have been returned.
	 * @see CBlkWTData
	 */
	@Override
	public CBlkWTData getNextCodeBlock(final int c, CBlkWTData cblk) {
		// We can not directly use getNextInternCodeBlock() since that returns
		// a reference to the internal buffer, we have to copy that data

		int j, k;
		final int w;
		Object dst_data; // a int[] or float[] object
		final int[] dst_data_int;
		final float[] dst_data_float;
		final Object src_data; // a int[] or float[] object

		this.intData = (DataBlk.TYPE_INT == filters.getWTDataType(tIdx, c));

		dst_data = null;

		// Cache the data array, if any
		if (null != cblk) {
			dst_data = cblk.getData();
		}

		// Get the next code-block
		cblk = this.getNextInternCodeBlock(c, cblk);

		if (null == cblk) {
			return null; // No more code-blocks in current tile for component
			// c
		}

		// Ensure size of output buffer
		if (this.intData) { // int data
			dst_data_int = (int[]) dst_data;
			if (null == dst_data_int || dst_data_int.length < cblk.w * cblk.h) {
				dst_data = new int[cblk.w * cblk.h];
			}
		} else { // float data
			dst_data_float = (float[]) dst_data;
			if (null == dst_data_float || dst_data_float.length < cblk.w * cblk.h) {
				dst_data = new float[cblk.w * cblk.h];
			}
		}

		// Copy data line by line
		src_data = cblk.getData();
		w = cblk.w;
		for (j = w * (cblk.h - 1), k = cblk.offset + (cblk.h - 1) * cblk.scanw; 0 <= j; j -= w, k -= cblk.scanw) {
			System.arraycopy(src_data, k, dst_data, j, w);
		}
		cblk.setData(dst_data);
		cblk.offset = 0;
		cblk.scanw = w;

		return cblk;
	}

	/**
	 * Return the data type of this CBlkWTDataSrc. Its value should be either
	 * DataBlk.TYPE_INT or DataBlk.TYPE_FLOAT but can change according to the
	 * current tile-component.
	 *
	 * @param t The index of the tile for which to return the data type.
	 * @param c The index of the component for which to return the data type.
	 * @return Current data type
	 */
	@Override
	public int getDataType(final int t, final int c) {
		return this.filters.getWTDataType(t, c);
	}

	/**
	 * Returns the next subband that will be used to get the next code-block to
	 * return by the getNext[Intern]CodeBlock method.
	 *
	 * @param c The component
	 * @return Its returns the next subband that will be used to get the next
	 * code-block to return by the getNext[Intern]CodeBlock method.
	 */
	private SubbandAn getNextSubband(final int c) {
		final int down = 1;
		final int up = 0;
		int direction = down;
		SubbandAn nextsb;

		nextsb = this.currentSubband[c];
		// If it is the first call to this method
		if (null == nextsb) {
			nextsb = this.getAnSubbandTree(this.tIdx, c);
			// If there is no decomposition level then send the whole image
			if (!nextsb.isNode) {
				return nextsb;
			}
		}

		// Find the next subband to send
		do {
			// If the current subband is a leaf then select the next leaf to
			// send or go up in the decomposition tree if the leaf was a LL
			// one.
			if (!nextsb.isNode) {
				switch (nextsb.orientation) {
					case Subband.WT_ORIENT_HH:
						nextsb = (SubbandAn) nextsb.getParent().getLH();
						direction = down;
						break;
					case Subband.WT_ORIENT_LH:
						nextsb = (SubbandAn) nextsb.getParent().getHL();
						direction = down;
						break;
					case Subband.WT_ORIENT_HL:
						nextsb = (SubbandAn) nextsb.getParent().getLL();
						direction = down;
						break;
					case Subband.WT_ORIENT_LL:
						nextsb = (SubbandAn) nextsb.getParent();
						direction = up;
						break;
					default:
						throw new IllegalArgumentException("unhandled orientation " + nextsb.orientation);
				}
			}

			// Else if the current subband is a node
			else if (nextsb.isNode) {
				// If the direction is down the select the HH subband of the
				// current node.
				if (direction == down) {
					nextsb = (SubbandAn) nextsb.getHH();
				}
				// Else the direction is up the select the next node to cover
				// or still go up in the decomposition tree if the node is a LL
				// subband
				else if (direction == up) {
					switch (nextsb.orientation) {
						case Subband.WT_ORIENT_HH:
							nextsb = (SubbandAn) nextsb.getParent().getLH();
							direction = down;
							break;
						case Subband.WT_ORIENT_LH:
							nextsb = (SubbandAn) nextsb.getParent().getHL();
							direction = down;
							break;
						case Subband.WT_ORIENT_HL:
							nextsb = (SubbandAn) nextsb.getParent().getLL();
							direction = down;
							break;
						case Subband.WT_ORIENT_LL:
							nextsb = (SubbandAn) nextsb.getParent();
							direction = up;
							break;
						default:
							throw new IllegalArgumentException("unhandled orientation " + nextsb.orientation);
					}
				}
			}
		} while (null != nextsb && nextsb.isNode);
		return nextsb;
	}

	/**
	 * Performs the forward wavelet transform on the whole band. It iteratively
	 * decomposes the subbands from the top node to the leaves.
	 *
	 * @param band    The band containing the float data to decompose
	 * @param subband The structure containing the coordinates of the current
	 *                subband in the whole band to decompose.
	 * @param c       The index of the current component to decompose
	 */
	private void waveletTreeDecomposition(final DataBlk band, final SubbandAn subband, final int c) {
		// If the current subband is a leaf then nothing to be done (a leaf is
		// not decomposed).
		if (!subband.isNode) {
			return;
		}
		// Perform the 2D wavelet decomposition of the current subband
		this.wavelet2DDecomposition(band, subband, c);

		// Perform the decomposition of the four resulting subbands
		this.waveletTreeDecomposition(band, (SubbandAn) subband.getHH(), c);
		this.waveletTreeDecomposition(band, (SubbandAn) subband.getLH(), c);
		this.waveletTreeDecomposition(band, (SubbandAn) subband.getHL(), c);
		this.waveletTreeDecomposition(band, (SubbandAn) subband.getLL(), c);
	}

	/**
	 * Performs the 2D forward wavelet transform on a subband of the initial
	 * band. This method will successively perform 1D filtering steps on all
	 * lines and then all columns of the subband. In this class only filters
	 * with floating point implementations can be used.
	 *
	 * @param band    The band containing the float data to decompose
	 * @param subband The structure containing the coordinates of the subband in the
	 *                whole band to decompose.
	 * @param c       The index of the current component to decompose
	 */
	private void wavelet2DDecomposition(final DataBlk band, final SubbandAn subband, final int c) {

		final int ulx;
		int uly;
		int w;
		final int h;
		final int band_w /*, band_h*/;

		// If subband is empty (i.e. zero size) nothing to do
		if (0 == subband.w || 0 == subband.h) {
			return;
		}

		ulx = subband.ulx;
		uly = subband.uly;
		w = subband.w;
		h = subband.h;
		band_w = this.getTileCompWidth(this.tIdx, c);
//		band_h = getTileCompHeight(tIdx, c);

		if (this.intData) {
			// Perform the decompositions if the filter is implemented with an
			// integer arithmetic.
			int i, j;
			int offset;
			final int[] tmpVector = new int[java.lang.Math.max(w, h)];
			final int[] data = ((DataBlkInt) band).getDataInt();

			// Perform the vertical decomposition
			if (0 == subband.ulcy % 2) { // Even start index => use LPF
				for (j = 0; j < w; j++) {
					offset = uly * band_w + ulx + j;
					for (i = 0; i < h; i++)
						tmpVector[i] = data[offset + (i * band_w)];
					subband.vFilter.analyze_lpf(tmpVector, 0, h, 1, data, offset, band_w, data, offset + ((h + 1) / 2)
							* band_w, band_w);
				}
			} else { // Odd start index => use HPF
				for (j = 0; j < w; j++) {
					offset = uly * band_w + ulx + j;
					for (i = 0; i < h; i++)
						tmpVector[i] = data[offset + (i * band_w)];
					subband.vFilter.analyze_hpf(tmpVector, 0, h, 1, data, offset, band_w, data, offset + (h / 2)
							* band_w, band_w);
				}
			}

			// Perform the horizontal decomposition.
			if (0 == subband.ulcx % 2) { // Even start index => use LPF
				for (i = 0; i < h; i++) {
					offset = (uly + i) * band_w + ulx;
					for (j = 0; j < w; j++)
						tmpVector[j] = data[offset + j];
					subband.hFilter.analyze_lpf(tmpVector, 0, w, 1, data, offset, 1, data, offset + (w + 1) / 2, 1);
				}
			} else { // Odd start index => use HPF
				for (i = 0; i < h; i++) {
					offset = (uly + i) * band_w + ulx;
					for (j = 0; j < w; j++)
						tmpVector[j] = data[offset + j];
					subband.hFilter.analyze_hpf(tmpVector, 0, w, 1, data, offset, 1, data, offset + w / 2, 1);
				}
			}
		} else {
			// Perform the decompositions if the filter is implemented with a
			// float arithmetic.
			int i, j;
			int offset;
			final float[] tmpVector = new float[java.lang.Math.max(w, h)];
			final float[] data = ((DataBlkFloat) band).getDataFloat();

			// Perform the vertical decomposition.
			if (0 == subband.ulcy % 2) { // Even start index => use LPF
				for (j = 0; j < w; j++) {
					offset = uly * band_w + ulx + j;
					for (i = 0; i < h; i++)
						tmpVector[i] = data[offset + (i * band_w)];
					subband.vFilter.analyze_lpf(tmpVector, 0, h, 1, data, offset, band_w, data, offset + ((h + 1) / 2)
							* band_w, band_w);
				}
			} else { // Odd start index => use HPF
				for (j = 0; j < w; j++) {
					offset = uly * band_w + ulx + j;
					for (i = 0; i < h; i++)
						tmpVector[i] = data[offset + (i * band_w)];
					subband.vFilter.analyze_hpf(tmpVector, 0, h, 1, data, offset, band_w, data, offset + (h / 2)
							* band_w, band_w);
				}
			}
			// Perform the horizontal decomposition.
			if (0 == subband.ulcx % 2) { // Even start index => use LPF
				for (i = 0; i < h; i++) {
					offset = (uly + i) * band_w + ulx;
					for (j = 0; j < w; j++)
						tmpVector[j] = data[offset + j];
					subband.hFilter.analyze_lpf(tmpVector, 0, w, 1, data, offset, 1, data, offset + (w + 1) / 2, 1);
				}
			} else { // Odd start index => use HPF
				for (i = 0; i < h; i++) {
					offset = (uly + i) * band_w + ulx;
					for (j = 0; j < w; j++)
						tmpVector[j] = data[offset + j];
					subband.hFilter.analyze_hpf(tmpVector, 0, w, 1, data, offset, 1, data, offset + w / 2, 1);
				}
			}
		}
	}

	/**
	 * Changes the current tile, given the new coordinates.
	 *
	 * <p>
	 * This method resets the 'subbTrees' array, and recalculates the values of
	 * the 'reversible' array. It also resets the decomposed component buffers.
	 *
	 * @param x The horizontal coordinate of the tile.
	 * @param y The vertical coordinate of the new tile.
	 * @returns The new tile index
	 */
	@Override
	public int setTile(final int x, final int y) {
		// Reset the decomposed component buffers.
		if (null != decomposedComps) {
			for (int i = this.decomposedComps.length - 1; 0 <= i; i--) {
				this.decomposedComps[i] = null;
				this.currentSubband[i] = null;
			}
		}
		// Change tile
		return super.setTile(x, y);
	}

	/**
	 * Advances to the next tile, in standard scan-line order (by rows then
	 * columns). An NoNextElementException is thrown if the current tile is the
	 * last one (i.e. there is no next tile).
	 *
	 * <p>
	 * This method resets the 'subbTrees' array, and recalculates the values of
	 * the 'reversible' array. It also resets the decomposed component buffers.
	 *
	 * @returns The new tile index
	 */
	@Override
	public int nextTile() {
		// Reset the decomposed component buffers
		if (null != decomposedComps) {
			for (int i = this.decomposedComps.length - 1; 0 <= i; i--) {
				this.decomposedComps[i] = null;
				this.currentSubband[i] = null;
			}
		}
		// Change tile
		return super.nextTile();
	}

	/**
	 * Returns a reference to the subband tree structure representing the
	 * subband decomposition for the specified tile-component of the source.
	 *
	 * @param t The index of the tile.
	 * @param c The index of the component.
	 * @return The subband tree structure, see Subband.
	 * @see SubbandAn
	 * @see Subband
	 */
	@Override
	public SubbandAn getAnSubbandTree(final int t, final int c) {
		if (null == subbTrees[t][c]) {
			this.subbTrees[t][c] = new SubbandAn(this.getTileCompWidth(t, c), this.getTileCompHeight(t, c), this.getCompULX(c),
					this.getCompULY(c), this.getDecompLevels(t, c), this.getHorAnWaveletFilters(t, c), this.getVertAnWaveletFilters(t, c));
			this.initSubbandsFields(t, c, this.subbTrees[t][c]);
		}
		return this.subbTrees[t][c];
	}

	/**
	 * Initialises subbands fields, such as number of code-blocks and
	 * code-blocks dimension, in the subband tree. The nominal code-block
	 * width/height depends on the precincts dimensions if used.
	 *
	 * @param t  The tile index of the subband
	 * @param c  The component index
	 * @param sb The subband tree to be initialised.
	 */
	private void initSubbandsFields(final int t, final int c, final Subband sb) {
		final int cbw = this.cblks.getCBlkWidth(ModuleSpec.SPEC_TILE_COMP, t, c);
		final int cbh = this.cblks.getCBlkHeight(ModuleSpec.SPEC_TILE_COMP, t, c);

		if (!sb.isNode) {
			// Code-blocks dimension
			final int ppx;
			final int ppy;
			final int ppxExp;
			int ppyExp;
			int cbwExp;
			final int cbhExp;
			ppx = this.pss.getPPX(t, c, sb.resLvl);
			ppy = this.pss.getPPY(t, c, sb.resLvl);

			if (Markers.PRECINCT_PARTITION_DEF_SIZE != ppx || Markers.PRECINCT_PARTITION_DEF_SIZE != ppy) {

				ppxExp = MathUtil.log2(ppx);
				ppyExp = MathUtil.log2(ppy);
				cbwExp = MathUtil.log2(cbw);
				cbhExp = MathUtil.log2(cbh);

				// Precinct partition is used
				if (0 == sb.resLvl) {
					sb.nomCBlkW = (cbwExp < ppxExp ? (1 << cbwExp) : (1 << ppxExp));
					sb.nomCBlkH = (cbhExp < ppyExp ? (1 << cbhExp) : (1 << ppyExp));
				} else {
					sb.nomCBlkW = (cbwExp < ppxExp - 1 ? (1 << cbwExp) : (1 << (ppxExp - 1)));
					sb.nomCBlkH = (cbhExp < ppyExp - 1 ? (1 << cbhExp) : (1 << (ppyExp - 1)));
				}
			} else {
				sb.nomCBlkW = cbw;
				sb.nomCBlkH = cbh;
			}

			// Number of code-blocks
			if (null == sb.numCb)
				sb.numCb = new Coord();
			if (0 != sb.w && 0 != sb.h) {
				int acb0x = this.cb0x;
				int acb0y = this.cb0y;
				int tmp;

				// Project code-block partition origin to subband. Since the
				// origin is always 0 or 1, it projects to the low-pass side
				// (throught the ceil operator) as itself (i.e. no change) and
				// to the high-pass side (through the floor operator) as 0,
				// always.
				switch (sb.sbandIdx) {
					case Subband.WT_ORIENT_LL:
						// No need to project since all low-pass => nothing to
						// do
						break;
					case Subband.WT_ORIENT_HL:
						acb0x = 0;
						break;
					case Subband.WT_ORIENT_LH:
						acb0y = 0;
						break;
					case Subband.WT_ORIENT_HH:
						acb0x = 0;
						acb0y = 0;
						break;
					default:
						throw new Error("Internal JJ2000 error");
				}
				if (0 > sb.ulcx - acb0x || 0 > sb.ulcy - acb0y) {
					throw new IllegalArgumentException("Invalid code-blocks partition origin or "
							+ "image offset in the reference grid.");
				}
				// NOTE: when calculating "floor()" by integer division the
				// dividend and divisor must be positive, we ensure that by
				// adding the divisor to the dividend and then substracting 1
				// to the result of the division
				tmp = sb.ulcx - acb0x + sb.nomCBlkW;
				sb.numCb.x = (tmp + sb.w - 1) / sb.nomCBlkW - (tmp / sb.nomCBlkW - 1);
				tmp = sb.ulcy - acb0y + sb.nomCBlkH;
				sb.numCb.y = (tmp + sb.h - 1) / sb.nomCBlkH - (tmp / sb.nomCBlkH - 1);
			} else {
				sb.numCb.x = sb.numCb.y = 0;
			}
		} else {
			this.initSubbandsFields(t, c, sb.getLL());
			this.initSubbandsFields(t, c, sb.getHL());
			this.initSubbandsFields(t, c, sb.getLH());
			this.initSubbandsFields(t, c, sb.getHH());
		}
	}
}
