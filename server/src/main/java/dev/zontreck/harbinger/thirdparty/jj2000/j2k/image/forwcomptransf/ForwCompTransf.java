/*
 * CVS Identifier:
 *
 * $Id: ForwCompTransf.java,v 1.20 2001/09/14 09:14:57 grosbois Exp $
 *
 * Class:               ForwCompTransf
 *
 * Description:         Component transformations applied to tiles
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
package dev.zontreck.harbinger.thirdparty.jj2000.j2k.image.forwcomptransf;

import dev.zontreck.harbinger.thirdparty.jj2000.j2k.ModuleSpec;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.encoder.EncoderSpecs;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.image.*;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.util.MathUtil;

/**
 * This class apply component transformations to the tiles depending on user
 * specifications. These transformations can be used to improve compression
 * efficiency but are not related to colour transforms used to map colour values
 * for display purposes. JPEG 2000 part I defines 2 component transformations:
 * RCT (Reversible Component Transformation) and ICT (Irreversible Component
 * Transformation).
 *
 * @see ModuleSpec
 */
public class ForwCompTransf extends ImgDataAdapter implements BlkImgDataSrc {
	/**
	 * Identifier for no component transformation. Value is 0.
	 */
	public static final int NONE = 0;

	/**
	 * Identifier for the Forward Reversible Component Transformation
	 * (FORW_RCT). Value is 1.
	 */
	public static final int FORW_RCT = 1;

	/**
	 * Identifier for the Forward Irreversible Component Transformation
	 * (FORW_ICT). Value is 2
	 */
	public static final int FORW_ICT = 2;
	/**
	 * The prefix for component transformation type: 'M'
	 */
	public static final char OPT_PREFIX = 'M';
	/**
	 * The list of parameters that is accepted by the forward component
	 * transformation module. Options start with an 'M'.
	 */
	private static final String[][] pinfo = { {
			"Mct" ,
			"[<tile index>] [on|off] ..." ,
			"Specifies in which tiles to use a multiple component transform. "
					+ "Note that this multiple component transform can only be applied "
					+ "in tiles that contain at least three components and whose "
					+ "components are processed with the same wavelet filters and quantization type. "
					+ "If the wavelet transform is reversible (w5x3 filter), the "
					+ "Reversible Component Transformation (RCT) is applied. If not "
					+ "(w9x7 filter), the Irreversible Component Transformation (ICT) is used." , null } , };

	/** The wavelet filter specifications */
//	private AnWTFilterSpec wfs;
	/**
	 * The source of image data
	 */
	private final BlkImgDataSrc src;
	/**
	 * The component transformations specifications
	 */
	private final CompTransfSpec cts;
	/**
	 * The type of the current component transformation. JPEG 2000 part 1
	 * supports only NONE, FORW_RCT and FORW_ICT types
	 */
	private int transfType = ForwCompTransf.NONE;
	/**
	 * The bit-depths of transformed components
	 */
	private int[] tdepth;
	/**
	 * Output block used instead of the one provided as an argument if the later
	 * is DataBlkFloat.
	 */
	private DataBlk outBlk;
	/**
	 * Block used to request component with index 0
	 */
	private DataBlkInt block0;
	/**
	 * Block used to request component with index 1
	 */
	private DataBlkInt block1;
	/**
	 * Block used to request component with index 2
	 */
	private DataBlkInt block2;

	/**
	 * Constructs a new ForwCompTransf object that operates on the specified
	 * source of image data.
	 *
	 * @param imgSrc  The source from where to get the data to be transformed
	 * @param encSpec The encoder specifications
	 * @see BlkImgDataSrc
	 */
	public ForwCompTransf ( final BlkImgDataSrc imgSrc , final EncoderSpecs encSpec ) {
		super ( imgSrc );
		cts = encSpec.cts;
//		this.wfs = encSpec.wfs;
		this.src = imgSrc;
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
	 * if no options are supported.
	 */
	public static String[][] getParameterInfo ( ) {
		return ForwCompTransf.pinfo;
	}

	/**
	 * Calculates the bitdepths of the transformed components, given the
	 * bitdepth of the un-transformed components and the component
	 * transformation type.
	 *
	 * @param ntdepth The bitdepth of each non-transformed components.
	 * @param ttype   The type ID of the component transformation.
	 * @param tdepth  If not null the results are stored in this array, otherwise a
	 *                new array is allocated and returned.
	 * @return The bitdepth of each transformed component.
	 */
	public static int[] calcMixedBitDepths ( final int[] ntdepth , final int ttype , int[] tdepth ) {

		if ( 3 > ntdepth.length && NONE != ttype ) {
			throw new IllegalArgumentException ( );
		}

		if ( null == tdepth ) {
			tdepth = new int[ ntdepth.length ];
		}

		switch ( ttype ) {
			case ForwCompTransf.FORW_RCT:
				if ( 3 < ntdepth.length ) {
					System.arraycopy ( ntdepth , 3 , tdepth , 3 , ntdepth.length - 3 );
				}
				// The formulas are:
				// tdepth[0] = ceil(log2(2^(ntdepth[0])+2^ntdepth[1]+
				// 2^(ntdepth[2])))-2+1
				// tdepth[1] = ceil(log2(2^(ntdepth[1])+2^(ntdepth[2])-1))+1
				// tdepth[2] = ceil(log2(2^(ntdepth[0])+2^(ntdepth[1])-1))+1
				// The MathUtil.log2(x) function calculates floor(log2(x)), so
				// we
				// use 'MathUtil.log2(2*x-1)+1', which calculates ceil(log2(x))
				// for any x>=1, x integer.
				tdepth[ 0 ] = MathUtil.log2 ( ( 1 << ntdepth[ 0 ] ) + ( 2 << ntdepth[ 1 ] ) + ( 1 << ntdepth[ 2 ] ) - 1 ) - 2 + 1;
				tdepth[ 1 ] = MathUtil.log2 ( ( 1 << ntdepth[ 2 ] ) + ( 1 << ntdepth[ 1 ] ) - 1 ) + 1;
				tdepth[ 2 ] = MathUtil.log2 ( ( 1 << ntdepth[ 0 ] ) + ( 1 << ntdepth[ 1 ] ) - 1 ) + 1;
				break;
			case ForwCompTransf.FORW_ICT:
				if ( 3 < ntdepth.length ) {
					System.arraycopy ( ntdepth , 3 , tdepth , 3 , ntdepth.length - 3 );
				}
				// The MathUtil.log2(x) function calculates floor(log2(x)), so
				// we
				// use 'MathUtil.log2(2*x-1)+1', which calculates ceil(log2(x))
				// for any x>=1, x integer.
				tdepth[ 0 ] = MathUtil.log2 ( ( int ) Math.floor ( ( 1 << ntdepth[ 0 ] ) * 0.299072 + ( 1 << ntdepth[ 1 ] ) * 0.586914
						+ ( 1 << ntdepth[ 2 ] ) * 0.114014 ) - 1 ) + 1;
				tdepth[ 1 ] = MathUtil.log2 ( ( int ) Math.floor ( ( 1 << ntdepth[ 0 ] ) * 0.168701 + ( 1 << ntdepth[ 1 ] ) * 0.331299
						+ ( 1 << ntdepth[ 2 ] ) * 0.5 ) - 1 ) + 1;
				tdepth[ 2 ] = MathUtil.log2 ( ( int ) Math.floor ( ( 1 << ntdepth[ 0 ] ) * 0.5 + ( 1 << ntdepth[ 1 ] ) * 0.418701
						+ ( 1 << ntdepth[ 2 ] ) * 0.081299 ) - 1 ) + 1;
				break;
			case ForwCompTransf.NONE:
			default:
				System.arraycopy ( ntdepth , 0 , tdepth , 0 , ntdepth.length );
				break;
		}
		return tdepth;
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
	 * This default implementation assumes that the number of fractional bits is
	 * not modified by the component mixer.
	 *
	 * @param c The index of the component.
	 * @return The value of the fixed point position of the source since the
	 * color transform does not affect it.
	 */
	@Override
	public int getFixedPoint ( final int c ) {
		return this.src.getFixedPoint ( c );
	}

	/**
	 * Initialize some variables used with RCT. It must be called, at least, at
	 * the beginning of each new tile.
	 */
	private void initForwRCT ( ) {
		int i;
		final int tIdx = this.getTileIdx ( );

		if ( 3 > src.getNumComps ( ) ) {
			throw new IllegalArgumentException ( );
		}
		// Check that the 3 components have the same dimensions
		if ( this.src.getTileCompWidth ( tIdx , 0 ) != this.src.getTileCompWidth ( tIdx , 1 )
				|| this.src.getTileCompWidth ( tIdx , 0 ) != this.src.getTileCompWidth ( tIdx , 2 )
				|| this.src.getTileCompHeight ( tIdx , 0 ) != this.src.getTileCompHeight ( tIdx , 1 )
				|| this.src.getTileCompHeight ( tIdx , 0 ) != this.src.getTileCompHeight ( tIdx , 2 ) ) {
			throw new IllegalArgumentException ( "Can not use RCT on components with different dimensions" );
		}
		// Initialize bitdepths
		final int[] utd; // Premix bitdepths
		utd = new int[ this.src.getNumComps ( ) ];
		for ( i = utd.length - 1; 0 <= i ; i-- ) {
			utd[ i ] = this.src.getNomRangeBits ( i );
		}
		this.tdepth = ForwCompTransf.calcMixedBitDepths ( utd , ForwCompTransf.FORW_RCT , null );
	}

	/**
	 * Initialize some variables used with ICT. It must be called, at least, at
	 * the beginning of a new tile.
	 */
	private void initForwICT ( ) {
		int i;
		final int tIdx = this.getTileIdx ( );

		if ( 3 > src.getNumComps ( ) ) {
			throw new IllegalArgumentException ( );
		}
		// Check that the 3 components have the same dimensions
		if ( this.src.getTileCompWidth ( tIdx , 0 ) != this.src.getTileCompWidth ( tIdx , 1 )
				|| this.src.getTileCompWidth ( tIdx , 0 ) != this.src.getTileCompWidth ( tIdx , 2 )
				|| this.src.getTileCompHeight ( tIdx , 0 ) != this.src.getTileCompHeight ( tIdx , 1 )
				|| this.src.getTileCompHeight ( tIdx , 0 ) != this.src.getTileCompHeight ( tIdx , 2 ) ) {
			throw new IllegalArgumentException ( "Can not use ICT on components with different dimensions" );
		}
		// Initialize bitdepths
		final int[] utd; // Premix bitdepths
		utd = new int[ this.src.getNumComps ( ) ];
		for ( i = utd.length - 1; 0 <= i ; i-- ) {
			utd[ i ] = this.src.getNomRangeBits ( i );
		}
		this.tdepth = ForwCompTransf.calcMixedBitDepths ( utd , ForwCompTransf.FORW_ICT , null );
	}

	/**
	 * Returns a string with a descriptive text of which forward component
	 * transformation is used. This can be either "Forward RCT" or "Forward
	 * ICT" or "No component transformation" depending on the current tile.
	 *
	 * @return A descriptive string
	 */
	@Override
	public String toString ( ) {
		switch ( this.transfType ) {
			case ForwCompTransf.FORW_RCT:
				return "Forward RCT";
			case ForwCompTransf.FORW_ICT:
				return "Forward ICT";
			case ForwCompTransf.NONE:
				return "No component transformation";
			default:
				throw new IllegalArgumentException ( "Non JPEG 2000 part I component transformation" );
		}
	}

	/**
	 * Returns the number of bits, referred to as the "range bits",
	 * corresponding to the nominal range of the data in the specified component
	 * and in the current tile. If this number is <i>b</i> then for unsigned
	 * data the nominal range is between 0 and 2^b-1, and for signed data it is
	 * between -2^(b-1) and 2^(b-1)-1. Note that this value can be affected by
	 * the multiple component transform.
	 *
	 * @param c The index of the component.
	 * @return The bitdepth of component 'c' after mixing.
	 */
	@Override
	public int getNomRangeBits ( final int c ) {
		switch ( this.transfType ) {
			case ForwCompTransf.FORW_RCT:
			case ForwCompTransf.FORW_ICT:
				return this.tdepth[ c ];
			case ForwCompTransf.NONE:
				return this.src.getNomRangeBits ( c );
			default:
				throw new IllegalArgumentException ( "Non JPEG 2000 part I component transformation" );
		}
	}

	/**
	 * Returns true if this transform is reversible in current tile. Reversible
	 * component transformations are those which operation can be completely
	 * reversed without any loss of information (not even due to rounding).
	 *
	 * @return Reversibility of component transformation in current tile
	 */
	public boolean isReversible ( ) {
		switch ( this.transfType ) {
			case ForwCompTransf.NONE:
			case ForwCompTransf.FORW_RCT:
				return true;
			case ForwCompTransf.FORW_ICT:
				return false;
			default:
				throw new IllegalArgumentException ( "Non JPEG 2000 part I component transformation" );
		}
	}

	/**
	 * Apply forward component transformation associated with the current tile.
	 * If no component transformation has been requested by the user, data are
	 * not modified.
	 *
	 * <p>
	 * This method calls the getInternCompData() method, but respects the
	 * definitions of the getCompData() method defined in the BlkImgDataSrc
	 * interface.
	 *
	 * @param blk Determines the rectangular area to return, and the data is
	 *            returned in this object.
	 * @param c   Index of the output component.
	 * @return The requested DataBlk
	 */
	@Override
	public DataBlk getCompData ( final DataBlk blk , final int c ) {
		// If requesting a component whose index is greater than 3 or there is
		// no transform return a copy of data (getInternCompData returns the
		// actual data in those cases)
		if ( 3 <= c || NONE == transfType ) {
			return this.src.getCompData ( blk , c );
		}
		// We can use getInternCompData (since data is a copy anyways)
		return this.getInternCompData ( blk , c );
	}

	/**
	 * Apply the component transformation associated with the current tile. If
	 * no component transformation has been requested by the user, data are not
	 * modified. Else, appropriate method is called (forwRCT or forwICT).
	 *
	 * @param blk Determines the rectangular area to return.
	 * @param c   Index of the output component.
	 * @return The requested DataBlk
	 * @see #forwRCT
	 * @see #forwICT
	 */
	@Override
	public DataBlk getInternCompData ( final DataBlk blk , final int c ) {
		switch ( this.transfType ) {
			case ForwCompTransf.NONE:
				return this.src.getInternCompData ( blk , c );
			case ForwCompTransf.FORW_RCT:
				return this.forwRCT ( blk , c );
			case ForwCompTransf.FORW_ICT:
				return this.forwICT ( blk , c );
			default:
				throw new IllegalArgumentException ( "Non JPEG 2000 part 1 component transformation for tile: " + this.tIdx );
		}
	}

	/**
	 * Apply forward component transformation to obtain requested component from
	 * specified block of data. Whatever the type of requested DataBlk, it
	 * always returns a DataBlkInt.
	 *
	 * @param blk Determine the rectangular area to return
	 * @param c   The index of the requested component
	 * @return Data of requested component
	 */
	private DataBlk forwRCT ( DataBlk blk , final int c ) {
		int k, k0, k1, k2, mink, i;
		final int w = blk.w; // width of output block
		final int h = blk.h; // height of ouput block
		int[] outdata; // array of output data

		// If asking for Yr, Ur or Vr do transform
		if ( 0 <= c && 2 >= c ) {
			// Check that request data type is int
			if ( DataBlk.TYPE_INT != blk.getDataType ( ) ) {
				if ( null == outBlk || DataBlk.TYPE_INT != outBlk.getDataType ( ) ) {
					this.outBlk = new DataBlkInt ( );
				}
				this.outBlk.w = w;
				this.outBlk.h = h;
				this.outBlk.ulx = blk.ulx;
				this.outBlk.uly = blk.uly;
				blk = this.outBlk;
			}

			// Reference to output block data array
			outdata = ( int[] ) blk.getData ( );

			// Create data array of blk if necessary
			if ( null == outdata || outdata.length < h * w ) {
				outdata = new int[ h * w ];
				blk.setData ( outdata );
			}

			// Block buffers for input RGB data
			final int[] data0;  // input data arrays
			int[] data1;
			final int[] bdata;

			if ( null == block0 )
				this.block0 = new DataBlkInt ( );
			if ( null == block1 )
				this.block1 = new DataBlkInt ( );
			if ( null == block2 )
				this.block2 = new DataBlkInt ( );
			this.block0.w = this.block1.w = this.block2.w = blk.w;
			this.block0.h = this.block1.h = this.block2.h = blk.h;
			this.block0.ulx = this.block1.ulx = this.block2.ulx = blk.ulx;
			this.block0.uly = this.block1.uly = this.block2.uly = blk.uly;

			// Fill in buffer blocks (to be read only)
			// Returned blocks may have different size and position
			this.block0 = ( DataBlkInt ) this.src.getInternCompData ( this.block0 , 0 );
			data0 = ( int[] ) this.block0.getData ( );
			this.block1 = ( DataBlkInt ) this.src.getInternCompData ( this.block1 , 1 );
			data1 = ( int[] ) this.block1.getData ( );
			this.block2 = ( DataBlkInt ) this.src.getInternCompData ( this.block2 , 2 );
			bdata = ( int[] ) this.block2.getData ( );

			// Set the progressiveness of the output data
			blk.progressive = this.block0.progressive || this.block1.progressive || this.block2.progressive;
			blk.offset = 0;
			blk.scanw = w;

			// Perform conversion

			// Initialize general indexes
			k = w * h - 1;
			k0 = this.block0.offset + ( h - 1 ) * this.block0.scanw + w - 1;
			k1 = this.block1.offset + ( h - 1 ) * this.block1.scanw + w - 1;
			k2 = this.block2.offset + ( h - 1 ) * this.block2.scanw + w - 1;

			switch ( c ) {
				case 0: // RGB to Yr conversion
					for ( i = h - 1; 0 <= i ; i-- ) {
						for ( mink = k - w; k > mink ; k-- , k0-- , k1-- , k2-- ) {
							// Use int arithmetic with 12 fractional bits
							// and rounding
							outdata[ k ] = ( data0[ k ] + 2 * data1[ k ] + bdata[ k ] ) >> 2; // Same
							// as
							// /
							// 4
						}
						// Jump to beggining of previous line in input
						k0 -= this.block0.scanw - w;
						k1 -= this.block1.scanw - w;
						k2 -= this.block2.scanw - w;
					}
					break;

				case 1: // RGB to Ur conversion
					for ( i = h - 1; 0 <= i ; i-- ) {
						for ( mink = k - w; k > mink ; k-- , k1-- , k2-- ) {
							// Use int arithmetic with 12 fractional bits
							// and rounding
							outdata[ k ] = bdata[ k2 ] - data1[ k1 ];
						}
						// Jump to beggining of previous line in input
						k1 -= this.block1.scanw - w;
						k2 -= this.block2.scanw - w;
					}
					break;

				case 2: // RGB to Vr conversion
					for ( i = h - 1; 0 <= i ; i-- ) {
						for ( mink = k - w; k > mink ; k-- , k0-- , k1-- ) {
							// Use int arithmetic with 12 fractional bits
							// and rounding
							outdata[ k ] = data0[ k0 ] - data1[ k1 ];
						}
						// Jump to beggining of previous line in input
						k0 -= this.block0.scanw - w;
						k1 -= this.block1.scanw - w;
					}
					break;
				default:
					/* Not really possible */
			}
		}
		else if ( 3 <= c ) {
			// Requesting a component which is not Y, Ur or Vr =>
			// just pass the data
			return this.src.getInternCompData ( blk , c );
		}
		else {
			// Requesting a non valid component index
			throw new IllegalArgumentException ( );
		}
		return blk;

	}

	/**
	 * Apply forward irreversible component transformation to obtain requested
	 * component from specified block of data. Whatever the type of requested
	 * DataBlk, it always returns a DataBlkFloat.
	 *
	 * @param blk Determine the rectangular area to return
	 * @param c   The index of the requested component
	 * @return Data of requested component
	 */
	private DataBlk forwICT ( DataBlk blk , final int c ) {
		int k, k0, k1, k2, mink, i;
		final int w = blk.w; // width of output block
		final int h = blk.h; // height of ouput block
		float[] outdata; // array of output data

		if ( DataBlk.TYPE_FLOAT != blk.getDataType ( ) ) {
			if ( null == outBlk || DataBlk.TYPE_FLOAT != outBlk.getDataType ( ) ) {
				this.outBlk = new DataBlkFloat ( );
			}
			this.outBlk.w = w;
			this.outBlk.h = h;
			this.outBlk.ulx = blk.ulx;
			this.outBlk.uly = blk.uly;
			blk = this.outBlk;
		}

		// Reference to output block data array
		outdata = ( float[] ) blk.getData ( );

		// Create data array of blk if necessary
		if ( null == outdata || outdata.length < w * h ) {
			outdata = new float[ h * w ];
			blk.setData ( outdata );
		}

		// If asking for Y, Cb or Cr do transform
		if ( 0 <= c && 2 >= c ) {

			final int[] data0;  // input data arrays
			int[] data1;
			final int[] data2;

			if ( null == block0 ) {
				this.block0 = new DataBlkInt ( );
			}
			if ( null == block1 ) {
				this.block1 = new DataBlkInt ( );
			}
			if ( null == block2 ) {
				this.block2 = new DataBlkInt ( );
			}
			this.block0.w = this.block1.w = this.block2.w = blk.w;
			this.block0.h = this.block1.h = this.block2.h = blk.h;
			this.block0.ulx = this.block1.ulx = this.block2.ulx = blk.ulx;
			this.block0.uly = this.block1.uly = this.block2.uly = blk.uly;

			// Returned blocks may have different size and position
			this.block0 = ( DataBlkInt ) this.src.getInternCompData ( this.block0 , 0 );
			data0 = ( int[] ) this.block0.getData ( );
			this.block1 = ( DataBlkInt ) this.src.getInternCompData ( this.block1 , 1 );
			data1 = ( int[] ) this.block1.getData ( );
			this.block2 = ( DataBlkInt ) this.src.getInternCompData ( this.block2 , 2 );
			data2 = ( int[] ) this.block2.getData ( );

			// Set the progressiveness of the output data
			blk.progressive = this.block0.progressive || this.block1.progressive || this.block2.progressive;
			blk.offset = 0;
			blk.scanw = w;

			// Perform conversion

			// Initialize general indexes
			k = w * h - 1;
			k0 = this.block0.offset + ( h - 1 ) * this.block0.scanw + w - 1;
			k1 = this.block1.offset + ( h - 1 ) * this.block1.scanw + w - 1;
			k2 = this.block2.offset + ( h - 1 ) * this.block2.scanw + w - 1;

			switch ( c ) {
				case 0:
					// RGB to Y conversion
					for ( i = h - 1; 0 <= i ; i-- ) {
						for ( mink = k - w; k > mink ; k-- , k0-- , k1-- , k2-- ) {
							outdata[ k ] = 0.299f * data0[ k0 ] + 0.587f * data1[ k1 ] + 0.114f * data2[ k2 ];
						}
						// Jump to beggining of previous line in input
						k0 -= this.block0.scanw - w;
						k1 -= this.block1.scanw - w;
						k2 -= this.block2.scanw - w;
					}
					break;

				case 1:
					// RGB to Cb conversion
					for ( i = h - 1; 0 <= i ; i-- ) {
						for ( mink = k - w; k > mink ; k-- , k0-- , k1-- , k2-- ) {
							outdata[ k ] = - 0.16875f * data0[ k0 ] - 0.33126f * data1[ k1 ] + 0.5f * data2[ k2 ];
						}
						// Jump to beggining of previous line in input
						k0 -= this.block0.scanw - w;
						k1 -= this.block1.scanw - w;
						k2 -= this.block2.scanw - w;
					}
					break;

				case 2:
					// RGB to Cr conversion
					for ( i = h - 1; 0 <= i ; i-- ) {
						for ( mink = k - w; k > mink ; k-- , k0-- , k1-- , k2-- ) {
							outdata[ k ] = 0.5f * data0[ k0 ] - 0.41869f * data1[ k1 ] - 0.08131f * data2[ k2 ];
						}
						// Jump to beggining of previous line in input
						k0 -= this.block0.scanw - w;
						k1 -= this.block1.scanw - w;
						k2 -= this.block2.scanw - w;
					}
					break;
				default:
					/* Not really possible */
			}
		}
		else if ( 3 <= c ) {
			// Requesting a component which is not Y, Cb or Cr =>
			// just pass the data

			// Variables
			final DataBlkInt indb = new DataBlkInt ( blk.ulx , blk.uly , w , h );
			final int[] indata; // input data array

			// Get the input data
			// (returned block may be larger than requested one)
			this.src.getInternCompData ( indb , c );
			indata = ( int[] ) indb.getData ( );

			// Copy the data converting from int to float
			k = w * h - 1;
			k0 = indb.offset + ( h - 1 ) * indb.scanw + w - 1;
			for ( i = h - 1; 0 <= i ; i-- ) {
				for ( mink = k - w; k > mink ; k-- , k0-- ) {
					outdata[ k ] = indata[ k0 ];
				}
				// Jump to beggining of next line in input
				k0 += indb.w - w;
			}

			// Set the progressivity
			blk.progressive = indb.progressive;
			blk.offset = 0;
			blk.scanw = w;
			return blk;
		}
		else {
			// Requesting a non valid component index
			throw new IllegalArgumentException ( );
		}
		return blk;

	}

	/**
	 * Changes the current tile, given the new indexes. An
	 * IllegalArgumentException is thrown if the indexes do not correspond to a
	 * valid tile.
	 *
	 * <p>
	 * This default implementation changes the tile in the source and
	 * re-initializes properly component transformation variables..
	 *
	 * @param x The horizontal index of the tile.
	 * @param y The vertical index of the new tile.
	 * @return The new tile index
	 */
	@Override
	public int setTile ( final int x , final int y ) {
		this.tIdx = this.src.setTile ( x , y );

		// initializations
		final String str = ( String ) this.cts.getTileDef ( this.tIdx );
		if ( "none".equals ( str ) ) {
			this.transfType = ForwCompTransf.NONE;
		}
		else if ( "rct".equals ( str ) ) {
			this.transfType = ForwCompTransf.FORW_RCT;
			this.initForwRCT ( );
		}
		else if ( "ict".equals ( str ) ) {
			this.transfType = ForwCompTransf.FORW_ICT;
			this.initForwICT ( );
		}
		else {
			throw new IllegalArgumentException ( "Component transformation not recognized" );
		}
		return this.tIdx;
	}

	/**
	 * Goes to the next tile, in standard scan-line order (by rows then by
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
	public int nextTile ( ) {
		this.tIdx = this.src.nextTile ( );

		// initializations
		final String str = ( String ) this.cts.getTileDef ( this.tIdx );
		if ( "none".equals ( str ) ) {
			this.transfType = ForwCompTransf.NONE;
		}
		else if ( "rct".equals ( str ) ) {
			this.transfType = ForwCompTransf.FORW_RCT;
			this.initForwRCT ( );
		}
		else if ( "ict".equals ( str ) ) {
			this.transfType = ForwCompTransf.FORW_ICT;
			this.initForwICT ( );
		}
		else {
			throw new IllegalArgumentException ( "Component transformation not recognized" );
		}
		return this.tIdx;
	}
}
