/*
 * CVS identifier:
 *
 * $Id: RectROIMaskGenerator.java,v 1.4 2001/02/28 15:33:44 grosbois Exp $
 *
 * Class:                   RectROIMaskGenerator
 *
 * Description:             Generates masks when only rectangular ROIs exist
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
package dev.zontreck.harbinger.thirdparty.jj2000.j2k.roi.encoder;

import dev.zontreck.harbinger.thirdparty.jj2000.j2k.image.DataBlkInt;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.wavelet.Subband;

/**
 * This class generates the ROI masks when there are only rectangular ROIs in
 * the image. The ROI mask generation can then be simplified by only calculating
 * the boundaries of the ROI mask in the particular subbands
 *
 * <p>
 * The values are calculated from the scaling factors of the ROIs. The values
 * with which to scale are equal to u-umin where umin is the lowest scaling
 * factor within the block. The umin value is sent to the entropy coder to be
 * used for scaling the distortion values.
 *
 * <p>
 * To generate and to store the boundaries of the ROIs, the class
 * SubbandRectROIMask is used. There is one tree of SubbandMasks for each
 * component.
 *
 * @see SubbandRectROIMask
 * @see ROIMaskGenerator
 * @see ArbROIMaskGenerator
 */
public class RectROIMaskGenerator extends ROIMaskGenerator {
	/**
	 * Number of ROIs
	 */
	private final int[] nrROIs;
	/**
	 * The tree of subbandmask. One for each component
	 */
	private final SubbandRectROIMask[] sMasks;
	/**
	 * The upper left xs of the ROIs
	 */
	private int[] ulxs;
	/**
	 * The upper left ys of the ROIs
	 */
	private int[] ulys;
	/**
	 * The lower right xs of the ROIs
	 */
	private int[] lrxs;
	/**
	 * The lower right ys of the ROIs
	 */
	private int[] lrys;

	/**
	 * The constructor of the mask generator. The constructor is called with the
	 * ROI data. This data is stored in arrays that are used to generate the
	 * SubbandRectROIMask trees for each component.
	 *
	 * @param ROIs The ROI info.
	 * @param nrc  number of components.
	 */
	public RectROIMaskGenerator ( final ROI[] ROIs , final int nrc ) {
		super ( ROIs , nrc );
		final int nr = ROIs.length;
		int r;
		this.nrROIs = new int[ nrc ];
		this.sMasks = new SubbandRectROIMask[ nrc ];

		// Count number of ROIs per component
		for ( r = nr - 1; 0 <= r ; r-- ) {
			this.nrROIs[ ROIs[ r ].comp ]++;
		}
	}

	/**
	 * This functions gets a DataBlk the size of the current code-block and
	 * fills this block with the ROI mask.
	 *
	 * <p>
	 * In order to get the mask for a particular Subband, the subband tree is
	 * traversed and at each decomposition, the ROI masks are computed. The roi
	 * bondaries for each subband are stored in the SubbandRectROIMask tree.
	 *
	 * @param db      The data block that is to be filled with the mask
	 * @param sb      The root of the subband tree to which db belongs
	 * @param magbits The max number of magnitude bits in any code-block
	 * @param c       The component for which to get the mask
	 * @return Whether or not a mask was needed for this tile
	 */
	@Override
	public boolean getROIMask ( final DataBlkInt db , final Subband sb , final int magbits , final int c ) {
		int x = db.ulx;
		int y = db.uly;
		final int w = db.w;
		final int h = db.h;
		final int[] mask = db.getDataInt ( );
		int i, j, k, r, maxk, maxj;
		int ulx = 0, uly = 0, lrx = 0, lry = 0;
		int wrap;
		final int maxROI;
		final int[] culxs;
		final int[] culys;
		final int[] clrxs;
		final int[] clrys;
		final SubbandRectROIMask srm;

		// If the ROI bounds have not been calculated for this tile and
		// component, do so now.
		if ( ! this.tileMaskMade[ c ] ) {
			this.makeMask ( sb , magbits , c );
			this.tileMaskMade[ c ] = true;
		}

		if ( ! this.roiInTile ) {
			return false;
		}

		// Find relevant subband mask and get ROI bounds
		srm = ( SubbandRectROIMask ) this.sMasks[ c ].getSubbandRectROIMask ( x , y );
		culxs = srm.ulxs;
		culys = srm.ulys;
		clrxs = srm.lrxs;
		clrys = srm.lrys;
		maxROI = culxs.length - 1;
		// Make sure that only parts of ROIs within the code-block are used
		// and make the bounds local to this block the LR bounds are counted
		// as the distance from the lower right corner of the block
		x -= srm.ulx;
		y -= srm.uly;
		for ( r = maxROI; 0 <= r ; r-- ) {
			ulx = culxs[ r ] - x;
			if ( 0 > ulx ) {
				ulx = 0;
			}
			else if ( ulx >= w ) {
				ulx = w;
			}

			uly = culys[ r ] - y;
			if ( 0 > uly ) {
				uly = 0;
			}
			else if ( uly >= h ) {
				uly = h;
			}

			lrx = clrxs[ r ] - x;
			if ( 0 > lrx ) {
				lrx = - 1;
			}
			else if ( lrx >= w ) {
				lrx = w - 1;
			}

			lry = clrys[ r ] - y;
			if ( 0 > lry ) {
				lry = - 1;
			}
			else if ( lry >= h ) {
				lry = h - 1;
			}

			// Add the masks of the ROI
			i = w * lry + lrx;
			maxj = ( lrx - ulx );
			wrap = w - maxj - 1;
			maxk = lry - uly;

			for ( k = maxk; 0 <= k ; k-- ) {
				for ( j = maxj; 0 <= j ; j-- , i-- )
					mask[ i ] = magbits;
				i -= wrap;
			}
		}
		return true;
	}

	/**
	 * This function returns the relevant data of the mask generator
	 */
	@Override
	public String toString ( ) {
		return ( "Fast rectangular ROI mask generator" );
	}

	/**
	 * This function generates the ROI mask for the entire tile. The mask is
	 * generated for one component. This method is called once for each tile and
	 * component.
	 *
	 * @param sb The root of the subband tree used in the decomposition
	 * @param n  component number
	 */
	@Override
	public void makeMask ( final Subband sb , final int magbits , final int n ) {
		int nr = this.nrROIs[ n ];
		int r;
		int ulx, uly, lrx, lry;
		final int tileulx = sb.ulcx;
		final int tileuly = sb.ulcy;
		final int tilew = sb.w;
		final int tileh = sb.h;
		final ROI[] ROIs = this.rois; // local copy

		this.ulxs = new int[ nr ];
		this.ulys = new int[ nr ];
		this.lrxs = new int[ nr ];
		this.lrys = new int[ nr ];

		nr = 0;

		for ( r = ROIs.length - 1; 0 <= r ; r-- ) {
			if ( ROIs[ r ].comp == n ) {
				ulx = ROIs[ r ].ulx;
				uly = ROIs[ r ].uly;
				lrx = ROIs[ r ].w + ulx - 1;
				lry = ROIs[ r ].h + uly - 1;

				if ( ulx > ( tileulx + tilew - 1 ) || uly > ( tileuly + tileh - 1 ) || lrx < tileulx || lry < tileuly ) // no
					// part
					// of
					// ROI
					// in
					// tile
					continue;

				// Check bounds
				ulx -= tileulx;
				lrx -= tileulx;
				uly -= tileuly;
				lry -= tileuly;

				ulx = ( 0 > ulx ) ? 0 : ulx;
				uly = ( 0 > uly ) ? 0 : uly;
				lrx = ( lrx > ( tilew - 1 ) ) ? tilew - 1 : lrx;
				lry = ( lry > ( tileh - 1 ) ) ? tileh - 1 : lry;

				this.ulxs[ nr ] = ulx;
				this.ulys[ nr ] = uly;
				this.lrxs[ nr ] = lrx;
				this.lrys[ nr ] = lry;
				nr++;
			}
		}
		this.roiInTile = 0 != nr;
		this.sMasks[ n ] = new SubbandRectROIMask ( sb , this.ulxs , this.ulys , this.lrxs , this.lrys , nr );
	}
}
