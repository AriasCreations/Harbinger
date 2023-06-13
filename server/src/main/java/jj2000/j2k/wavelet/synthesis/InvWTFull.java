/*
 * CVS identifier:
 *
 * $Id: InvWTFull.java,v 1.20 2002/05/22 15:01:32 grosbois Exp $
 *
 * Class:                   InvWTFull
 *
 * Description:             This class implements a full page inverse DWT for
 *                          int and float data.
 *
 *                          the InvWTFullInt and InvWTFullFloat
 *                          classes by Bertrand Berthelot, Apr-19-1999
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
package jj2000.j2k.wavelet.synthesis;

import jj2000.j2k.decoder.DecoderSpecs;
import jj2000.j2k.image.Coord;
import jj2000.j2k.image.DataBlk;
import jj2000.j2k.image.DataBlkFloat;
import jj2000.j2k.image.DataBlkInt;
import jj2000.j2k.util.FacilityManager;
import jj2000.j2k.util.ProgressWatch;
import jj2000.j2k.wavelet.Subband;
import jj2000.j2k.wavelet.WaveletTransform;

import java.security.InvalidParameterException;

/**
 * This class implements the InverseWT with the full-page approach for int and
 * float data.
 *
 * <p>
 * The image can be reconstructed at different (image) resolution levels indexed
 * from the lowest resolution available for each tile-component. This is
 * controlled by the setImgResLevel() method.
 *
 * <p>
 * Note: Image resolution level indexes may differ from tile-component
 * resolution index. They are indeed indexed starting from the lowest number of
 * decomposition levels of each component of each tile.
 *
 * <p>
 * Example: For an image (1 tile) with 2 components (component 0 having 2
 * decomposition levels and component 1 having 3 decomposition levels), the
 * first (tile-) component has 3 resolution levels and the second one has 4
 * resolution levels, whereas the image has only 3 resolution levels available.
 *
 * <p>
 * This implementation does not support progressive data, all data is considered
 * to be non-progressive (i.e. "final" data) and the 'progressive' attribute of
 * the 'DataBlk' class is always set to false, see the 'DataBlk' class.
 *
 * @see DataBlk
 */
public class InvWTFull extends InverseWT {
	/**
	 * Reference to the ProgressWatch instance if any
	 */
	private ProgressWatch pw;

	/**
	 * The total number of code-blocks to decode
	 */
	private int cblkToDecode;

	/**
	 * The number of already decoded code-blocks
	 */
	private int nDecCblk;

	/**
	 * the code-block buffer's source i.e. the quantizer
	 */
	private final CBlkWTDataSrcDec src;

	/**
	 * Current data type
	 */
	private int dtype;

	/**
	 * Block storing the reconstructed image for each component
	 */
	private final DataBlk[] reconstructedComps;

	/**
	 * Number of decomposition levels in each component
	 */
	private final int[] ndl;

	/**
	 * The reversible flag for each component in each tile. The first index is
	 * the tile index, the second one is the component index. The reversibility
	 * of the components for each tile are calculated on a as needed basis.
	 */
	private boolean[][] reversible;

	/**
	 * Initializes this object with the given source of wavelet coefficients. It
	 * initializes the resolution level for full resolutioin reconstruction.
	 *
	 * @param src     from where the wavelet coefficinets should be obtained.
	 * @param decSpec The decoder specifications
	 */
	public InvWTFull(final CBlkWTDataSrcDec src, final DecoderSpecs decSpec) {
		super(src, decSpec);
		this.src = src;
		final int nc = src.getNumComps();
		this.reconstructedComps = new DataBlk[nc];
		this.ndl = new int[nc];
		this.pw = FacilityManager.getProgressWatch();
	}

	/**
	 * Returns the reversibility of the current subband. It computes iteratively
	 * the reversibility of the child subbands. For each subband it tests the
	 * reversibility of the horizontal and vertical synthesis filters used to
	 * reconstruct this subband.
	 *
	 * @param subband The current subband.
	 * @return true if all the filters used to reconstruct the current subband
	 * are reversible
	 */
	private boolean isSubbandReversible(final Subband subband) {
		if (subband.isNode) {
			// It's reversible if the filters to obtain the 4 subbands are
			// reversible and the ones for this one are reversible too.
			return this.isSubbandReversible(subband.getLL()) && this.isSubbandReversible(subband.getHL())
					&& this.isSubbandReversible(subband.getLH()) && this.isSubbandReversible(subband.getHH())
					&& ((SubbandSyn) subband).hFilter.isReversible() && ((SubbandSyn) subband).vFilter.isReversible();
		}
		// Leaf subband. Reversibility of data depends on source, so say
		// it's true
		return true;
	}

	/**
	 * Returns the reversibility of the wavelet transform for the specified
	 * component, in the current tile. A wavelet transform is reversible when it
	 * is suitable for lossless and lossy-to-lossless compression.
	 *
	 * @param t The index of the tile.
	 * @param c The index of the component.
	 * @return true is the wavelet transform is reversible, false if not.
	 */
	@Override
	public boolean isReversible(final int t, final int c) {
		if (null == reversible[t]) {
			// Reversibility not yet calculated for this tile
			this.reversible[t] = new boolean[this.getNumComps()];
			for (int i = this.reversible.length - 1; 0 <= i; i--) {
				this.reversible[t][i] = this.isSubbandReversible(this.src.getSynSubbandTree(t, i));
			}
		}
		return this.reversible[t][c];
	}

	/**
	 * Returns the number of bits, referred to as the "range bits",
	 * corresponding to the nominal range of the data in the specified
	 * component.
	 *
	 * <p>
	 * The returned value corresponds to the nominal dynamic range of the
	 * reconstructed image data, as long as the getNomRangeBits() method of the
	 * source returns a value corresponding to the nominal dynamic range of the
	 * image data and not not of the wavelet coefficients.
	 *
	 * <p>
	 * If this number is <i>b</b> then for unsigned data the nominal range is
	 * between 0 and 2^b-1, and for signed data it is between -2^(b-1) and
	 * 2^(b-1)-1.
	 *
	 * @param c The index of the component.
	 * @return The number of bits corresponding to the nominal range of the
	 * data.
	 */
	@Override
	public int getNomRangeBits(final int c) {
		return this.src.getNomRangeBits(c);
	}

	/**
	 * Returns the position of the fixed point in the specified component. This
	 * is the position of the least significant integral (i.e. non-fractional)
	 * bit, which is equivalent to the number of fractional bits. For instance,
	 * for fixed-point values with 2 fractional bits, 2 is returned. For
	 * floating-point data this value does not apply and 0 should be returned.
	 * Position 0 is the position of the least significant bit in the data.
	 *
	 * <p>
	 * This default implementation assumes that the wavelet transform does not
	 * modify the fixed point. If that were the case this method should be
	 * overriden.
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
	 * Returns a block of image data containing the specifed rectangular area,
	 * in the specified component, as a reference to the internal buffer (see
	 * below). The rectangular area is specified by the coordinates and
	 * dimensions of the 'blk' object.
	 *
	 * <p>
	 * The area to return is specified by the 'ulx', 'uly', 'w' and 'h' members
	 * of the 'blk' argument. These members are not modified by this method.
	 *
	 * <p>
	 * The data returned by this method can be the data in the internal buffer
	 * of this object, if any, and thus can not be modified by the caller. The
	 * 'offset' and 'scanw' of the returned data can be arbitrary. See the
	 * 'DataBlk' class.
	 *
	 * <p>
	 * The returned data has its 'progressive' attribute unset (i.e. false).
	 *
	 * @param blk Its coordinates and dimensions specify the area to return.
	 * @param c   The index of the component from which to get the data.
	 * @return The requested DataBlk
	 */
	@Override
	public final DataBlk getInternCompData(DataBlk blk, final int c) {
		final int tIdx = this.getTileIdx();
		if (null == src.getSynSubbandTree(tIdx, c).getHorWFilter()) {
			this.dtype = DataBlk.TYPE_INT;
		} else {
			this.dtype = this.src.getSynSubbandTree(tIdx, c).getHorWFilter().getDataType();
		}

		// If the source image has not been decomposed
		if (null == reconstructedComps[c]) {
			// Allocate component data buffer
			switch (this.dtype) {
				case DataBlk.TYPE_FLOAT:
					this.reconstructedComps[c] = new DataBlkFloat(0, 0, this.getTileCompWidth(tIdx, c),
							this.getTileCompHeight(tIdx, c));
					break;
				case DataBlk.TYPE_INT:
					this.reconstructedComps[c] = new DataBlkInt(0, 0, this.getTileCompWidth(tIdx, c), this.getTileCompHeight(tIdx, c));
					break;
				default:
					throw new InvalidParameterException("Invalid data block type: " + this.dtype);
			}
			// Reconstruct source image
			this.waveletTreeReconstruction(this.reconstructedComps[c], this.src.getSynSubbandTree(tIdx, c), c);
			if (null != pw && c == this.src.getNumComps() - 1) {
				this.pw.terminateProgressWatch();
			}
		}

		if (blk.getDataType() != this.dtype) {
			if (DataBlk.TYPE_INT == dtype) {
				blk = new DataBlkInt(blk.ulx, blk.uly, blk.w, blk.h);
			} else {
				blk = new DataBlkFloat(blk.ulx, blk.uly, blk.w, blk.h);
			}
		}
		// Set the reference to the internal buffer
		blk.setData(this.reconstructedComps[c].getData());
		blk.offset = this.reconstructedComps[c].w * blk.uly + blk.ulx;
		blk.scanw = this.reconstructedComps[c].w;
		blk.progressive = false;
		return blk;
	}

	/**
	 * Returns a block of image data containing the specifed rectangular area,
	 * in the specified component, as a copy (see below). The rectangular area
	 * is specified by the coordinates and dimensions of the 'blk' object.
	 *
	 * <p>
	 * The area to return is specified by the 'ulx', 'uly', 'w' and 'h' members
	 * of the 'blk' argument. These members are not modified by this method.
	 *
	 * <p>
	 * The data returned by this method is always a copy of the internal data of
	 * this object, if any, and it can be modified "in place" without any
	 * problems after being returned. The 'offset' of the returned data is 0,
	 * and the 'scanw' is the same as the block's width. See the 'DataBlk'
	 * class.
	 *
	 * <p>
	 * If the data array in 'blk' is <tt>null</tt>, then a new one is created.
	 * If the data array is not <tt>null</tt> then it must be big enough to
	 * contain the requested area.
	 *
	 * <p>
	 * The returned data always has its 'progressive' attribute unset (i.e
	 * false)
	 *
	 * @param blk Its coordinates and dimensions specify the area to return. If
	 *            it contains a non-null data array, then it must be large
	 *            enough. If it contains a null data array a new one is created.
	 *            The fields in this object are modified to return the data.
	 * @param c   The index of the component from which to get the data.
	 * @return The requested DataBlk
	 */
	@Override
	public DataBlk getCompData(DataBlk blk, final int c) {
		Object dst_data;
		int[] dst_data_int;
		float[] dst_data_float;

		// To keep compiler happy
		dst_data = null;

		// Ensure output buffer
		switch (blk.getDataType()) {
			case DataBlk.TYPE_INT:
				dst_data_int = (int[]) blk.getData();
				if (null == dst_data_int || dst_data_int.length < blk.w * blk.h) {
					dst_data_int = new int[blk.w * blk.h];
				}
				dst_data = dst_data_int;
				break;
			case DataBlk.TYPE_FLOAT:
				dst_data_float = (float[]) blk.getData();
				if (null == dst_data_float || dst_data_float.length < blk.w * blk.h) {
					dst_data_float = new float[blk.w * blk.h];
				}
				dst_data = dst_data_float;
				break;
			default:
				throw new InvalidParameterException("Invalid data block type: " + blk.getDataType());
		}

		// Use getInternCompData() to get the data, since getInternCompData()
		// returns reference to internal buffer, we must copy it.
		blk = this.getInternCompData(blk, c);

		// Copy the data
		blk.setData(dst_data);
		blk.offset = 0;
		blk.scanw = blk.w;
		return blk;
	}

	/**
	 * Performs the 2D inverse wavelet transform on a subband of the image, on
	 * the specified component. This method will successively perform 1D
	 * filtering steps on all columns and then all lines of the subband.
	 *
	 * @param db the buffer for the image/wavelet data.
	 * @param sb The subband to reconstruct.
	 * @param c  The index of the component to reconstruct
	 */
	private void wavelet2DReconstruction(final DataBlk db, final SubbandSyn sb, final int c) {
		final Object data;
		Object buf;
		final int ulx;
		int uly;
		int w;
		final int h;
		int i, j, k;
		int offset;

		// If subband is empty (i.e. zero size) nothing to do
		if (0 == sb.w || 0 == sb.h) {
			return;
		}

		data = db.getData();

		ulx = sb.ulx;
		uly = sb.uly;
		w = sb.w;
		h = sb.h;

		buf = null; // To keep compiler happy

		switch (sb.getHorWFilter().getDataType()) {
			case DataBlk.TYPE_INT:
				buf = new int[(w >= h) ? w : h];
				break;
			case DataBlk.TYPE_FLOAT:
				buf = new float[(w >= h) ? w : h];
				break;
			default:
				throw new InvalidParameterException("Invalid data block type: " + sb.getHorWFilter().getDataType());
		}

		// Perform the horizontal reconstruction
		offset = (uly - db.uly) * db.w + ulx - db.ulx;
		if (0 == sb.ulcx % 2) { // start index is even => use LPF
			for (i = 0; i < h; i++, offset += db.w) {
				System.arraycopy(data, offset, buf, 0, w);
				sb.hFilter.synthetize_lpf(buf, 0, (w + 1) / 2, 1, buf, (w + 1) / 2, w / 2, 1, data, offset, 1);
			}
		} else { // start index is odd => use HPF
			for (i = 0; i < h; i++, offset += db.w) {
				System.arraycopy(data, offset, buf, 0, w);
				sb.hFilter.synthetize_hpf(buf, 0, w / 2, 1, buf, w / 2, (w + 1) / 2, 1, data, offset, 1);
			}
		}

		// Perform the vertical reconstruction
		offset = (uly - db.uly) * db.w + ulx - db.ulx;
		switch (sb.getVerWFilter().getDataType()) {
			case DataBlk.TYPE_INT:
				final int[] data_int;
				final int[] buf_int;
				data_int = (int[]) data;
				buf_int = (int[]) buf;
				if (0 == sb.ulcy % 2) { // start index is even => use LPF
					for (j = 0; j < w; j++, offset++) {
						for (i = h - 1, k = offset + i * db.w; 0 <= i; i--, k -= db.w)
							buf_int[i] = data_int[k];
						sb.vFilter.synthetize_lpf(buf, 0, (h + 1) / 2, 1, buf, (h + 1) / 2, h / 2, 1, data, offset,
								db.w);
					}
				} else { // start index is odd => use HPF
					for (j = 0; j < w; j++, offset++) {
						for (i = h - 1, k = offset + i * db.w; 0 <= i; i--, k -= db.w)
							buf_int[i] = data_int[k];
						sb.vFilter.synthetize_hpf(buf, 0, h / 2, 1, buf, h / 2, (h + 1) / 2, 1, data, offset, db.w);
					}
				}
				break;
			case DataBlk.TYPE_FLOAT:
				final float[] data_float;
				final float[] buf_float;
				data_float = (float[]) data;
				buf_float = (float[]) buf;
				if (0 == sb.ulcy % 2) { // start index is even => use LPF
					for (j = 0; j < w; j++, offset++) {
						for (i = h - 1, k = offset + i * db.w; 0 <= i; i--, k -= db.w)
							buf_float[i] = data_float[k];
						sb.vFilter.synthetize_lpf(buf, 0, (h + 1) / 2, 1, buf, (h + 1) / 2, h / 2, 1, data, offset,
								db.w);
					}
				} else { // start index is odd => use HPF
					for (j = 0; j < w; j++, offset++) {
						for (i = h - 1, k = offset + i * db.w; 0 <= i; i--, k -= db.w)
							buf_float[i] = data_float[k];
						sb.vFilter.synthetize_hpf(buf, 0, h / 2, 1, buf, h / 2, (h + 1) / 2, 1, data, offset, db.w);
					}
				}
				break;
			default:
				throw new InvalidParameterException("Invalid data block type: " + sb.getVerWFilter().getDataType());
		}
	}

	/**
	 * Performs the inverse wavelet transform on the whole component. It
	 * iteratively reconstructs the subbands from leaves up to the root node.
	 * This method is recursive, the first call to it the 'sb' must be the root
	 * of the subband tree. The method will then process the entire subband tree
	 * by calling itslef recursively.
	 *
	 * @param img The buffer for the image/wavelet data.
	 * @param sb  The subband to reconstruct.
	 * @param c   The index of the component to reconstruct
	 */
	private void waveletTreeReconstruction(final DataBlk img, final SubbandSyn sb, final int c) {

		DataBlk subbData;

		// If the current subband is a leaf then get the data from the source
		if (!sb.isNode) {
			int i, m, n;
			Object src_data;
			final Object dst_data;
			final Coord ncblks;

			if (0 == sb.w || 0 == sb.h) {
				return; // If empty subband do nothing
			}

			// Get all code-blocks in subband
			if (DataBlk.TYPE_INT == dtype) {
				subbData = new DataBlkInt();
			} else {
				subbData = new DataBlkFloat();
			}
			ncblks = sb.numCb;
			dst_data = img.getData();
			for (m = 0; m < ncblks.y; m++) {
				for (n = 0; n < ncblks.x; n++) {
					subbData = this.src.getInternCodeBlock(c, m, n, sb, subbData);
					src_data = subbData.getData();
					if (null != pw) {
						this.nDecCblk++;
						this.pw.updateProgressWatch(this.nDecCblk, null);
					}
					// Copy the data line by line
					for (i = subbData.h - 1; 0 <= i; i--) {
						System.arraycopy(src_data, subbData.offset + i * subbData.scanw, dst_data, (subbData.uly + i)
								* img.w + subbData.ulx, subbData.w);
					}
				}
			}
		} else if (sb.isNode) {
			// Reconstruct the lower resolution levels if the current subbands
			// is a node

			// Perform the reconstruction of the LL subband
			this.waveletTreeReconstruction(img, (SubbandSyn) sb.getLL(), c);

			if (sb.resLvl <= this.reslvl - this.maxImgRes + this.ndl[c]) {
				// Reconstruct the other subbands
				this.waveletTreeReconstruction(img, (SubbandSyn) sb.getHL(), c);
				this.waveletTreeReconstruction(img, (SubbandSyn) sb.getLH(), c);
				this.waveletTreeReconstruction(img, (SubbandSyn) sb.getHH(), c);

				// Perform the 2D wavelet decomposition of the current subband
				this.wavelet2DReconstruction(img, sb, c);
			}
		}
	}

	/**
	 * Returns the implementation type of this wavelet transform, WT_IMPL_FULL
	 * (full-page based transform). All components return the same.
	 *
	 * @param c The index of the component.
	 * @return WT_IMPL_FULL
	 * @see WaveletTransform#WT_IMPL_FULL
	 */
	@Override
	public int getImplementationType(final int c) {
		return WaveletTransform.WT_IMPL_FULL;
	}

	/**
	 * Changes the current tile, given the new indexes. An
	 * IllegalArgumentException is thrown if the indexes do not correspond to a
	 * valid tile.
	 *
	 * @param x The horizontal index of the tile.
	 * @param y The vertical index of the new tile.
	 * @returns The new tile index
	 */
	@Override
	public int setTile(final int x, final int y) {
		// Change tile
		super.setTile(x, y);

		final int nc = this.src.getNumComps();
		final int tIdx = this.src.getTileIdx();
		for (int c = 0; c < nc; c++) {
			this.ndl[c] = this.src.getSynSubbandTree(tIdx, c).resLvl;
		}

		// Reset the decomposed component buffers.
		if (null != reconstructedComps) {
			for (int i = this.reconstructedComps.length - 1; 0 <= i; i--) {
				this.reconstructedComps[i] = null;
			}
		}

		this.cblkToDecode = 0;
		SubbandSyn root, sb;
		for (int c = 0; c < nc; c++) {
			root = this.src.getSynSubbandTree(tIdx, c);
			for (int r = 0; r <= this.reslvl - this.maxImgRes + root.resLvl; r++) {
				if (0 == r) {
					sb = (SubbandSyn) root.getSubbandByIdx(0, 0);
					if (null != sb)
						this.cblkToDecode += sb.numCb.x * sb.numCb.y;
				} else {
					sb = (SubbandSyn) root.getSubbandByIdx(r, 1);
					if (null != sb)
						this.cblkToDecode += sb.numCb.x * sb.numCb.y;
					sb = (SubbandSyn) root.getSubbandByIdx(r, 2);
					if (null != sb)
						this.cblkToDecode += sb.numCb.x * sb.numCb.y;
					sb = (SubbandSyn) root.getSubbandByIdx(r, 3);
					if (null != sb)
						this.cblkToDecode += sb.numCb.x * sb.numCb.y;
				}
			} // Loop on resolution levels
		} // Loop on components
		this.nDecCblk = 0;

		if (null != pw) {
			this.pw.initProgressWatch(0, this.cblkToDecode, "Decoding tile " + tIdx + "...");
		}
		return getTileIdx();
	}

	/**
	 * Advances to the next tile, in standard scan-line order (by rows then
	 * columns). An 'NoNextElementException' is thrown if the current tile is
	 * the last one (i.e. there is no next tile).
	 *
	 * @returns The new tile index
	 */
	@Override
	public int nextTile() {
		// Change tile
		super.nextTile();

		final int nc = this.src.getNumComps();
		final int tIdx = this.src.getTileIdx();
		for (int c = 0; c < nc; c++) {
			this.ndl[c] = this.src.getSynSubbandTree(tIdx, c).resLvl;
		}

		// Reset the decomposed component buffers.
		if (null != reconstructedComps) {
			for (int i = this.reconstructedComps.length - 1; 0 <= i; i--) {
				this.reconstructedComps[i] = null;
			}
		}
		return getTileIdx();
	}
}
