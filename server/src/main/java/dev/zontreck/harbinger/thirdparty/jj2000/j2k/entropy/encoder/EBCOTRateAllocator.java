/*
 * CVS identifier:
 *
 * $Id: EBCOTRateAllocator.java,v 1.97 2002/05/22 14:59:44 grosbois Exp $
 *
 * Class:                   EBCOTRateAllocator
 *
 * Description:             Generic interface for post-compression
 *                          rate allocator.
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
package dev.zontreck.harbinger.thirdparty.jj2000.j2k.entropy.encoder;

import dev.zontreck.harbinger.thirdparty.jj2000.j2k.codestream.Markers;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.codestream.PrecInfo;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.codestream.ProgressionType;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.codestream.writer.BitOutputBuffer;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.codestream.writer.CodestreamWriter;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.codestream.writer.PktEncoder;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.encoder.EncoderSpecs;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.entropy.Progression;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.image.Coord;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.util.*;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.wavelet.analysis.SubbandAn;

import java.io.IOException;

/**
 * This implements the EBCOT post compression rate allocation algorithm. This
 * algorithm finds the most suitable truncation points for the set of
 * code-blocks, for each layer target bitrate. It works by first collecting the
 * rate distortion info from all code-blocks, in all tiles and all components,
 * and then running the rate-allocation on the whole image at once, for each
 * layer.
 *
 * <p>
 * This implementation also provides some timing features. They can be enabled
 * by setting the 'DO_TIMING' constant of this class to true and recompiling.
 * The timing uses the 'System.currentTimeMillis()' Java API call, which returns
 * wall clock time, not the actual CPU time used. The timing results will be
 * printed on the message output. Since the times reported are wall clock times
 * and not CPU usage times they can not be added to find the total used time
 * (i.e. some time might be counted in several places). When timing is disabled
 * ('DO_TIMING' is false) there is no penalty if the compiler performs some
 * basic optimizations. Even if not the penalty should be negligeable.
 *
 * @see PostCompRateAllocator
 * @see CodedCBlkDataSrcEnc
 * @see dev.zontreck.harbinger.thirdparty.jj2000.j2k.codestream.writer.CodestreamWriter
 */
public class EBCOTRateAllocator extends PostCompRateAllocator {
	/**
	 * Whether to collect timing information or not: false. Used as a compile
	 * time directive.
	 */
	private static final boolean DO_TIMING = false;
	/**
	 * The log of 2, natural base
	 */
	private static final double LOG2 = Math.log ( 2 );
	/**
	 * The normalization offset for the R-D summary table
	 */
	private static final int RD_SUMMARY_OFF = 24;
	/**
	 * The size of the summary table
	 */
	private static final int RD_SUMMARY_SIZE = 64;
	/**
	 * The relative precision for float data. This is the relative tolerance up
	 * to which the layer slope thresholds are calculated.
	 */
	private static final float FLOAT_REL_PRECISION = 1.0e-4f;
	/**
	 * The precision for float data type, in an absolute sense. Two float
	 * numbers are considered "equal" if they are within this precision.
	 */
	private static final float FLOAT_ABS_PRECISION = 1.0e-10f;
	/**
	 * Minimum average size of a packet. If layer has less bytes than the this
	 * constant multiplied by number of packets in the layer, then the layer is
	 * skipped.
	 */
	private static final int MIN_AVG_PACKET_SZ = 32;
	/**
	 * 5D Array containing all the coded code-blocks:
	 *
	 * <ul>
	 * <li>1st index: tile index</li>
	 * <li>2nd index: component index</li>
	 * <li>3rd index: resolution level index</li>
	 * <li>4th index: subband index</li>
	 * <li>5th index: code-block index</li>
	 * </ul>
	 */
	private final CBlkRateDistStats[][][][][] cblks;
	/**
	 * 6D Array containing the indices of the truncation points. It actually
	 * contains the index of the element in CBlkRateDistStats.truncIdxs that
	 * gives the real truncation point index.
	 *
	 * <ul>
	 * <li>1st index: tile index</li>
	 * <li>2nd index: layer index</li>
	 * <li>3rd index: component index</li>
	 * <li>4th index: resolution level index</li>
	 * <li>5th index: subband index</li>
	 * <li>6th index: code-block index</li>
	 * </ul>
	 */
	private final int[][][][][][] truncIdxs;
	/**
	 * The R-D summary information collected from the coding of all code-blocks.
	 * For each entry it contains the accumulated length of all truncation
	 * points that have a slope not less than '2*(k-RD_SUMMARY_OFF)', where 'k'
	 * is the entry index.
	 *
	 * <p>
	 * Therefore, the length at entry 'k' is the total number of bytes of
	 * code-block data that would be obtained if the truncation slope was chosen
	 * as '2*(k-RD_SUMMARY_OFF)', without counting the overhead associated with
	 * the packet heads.
	 *
	 * <p>
	 * This summary is used to estimate the relation of the R-D slope to coded
	 * length, and to obtain absolute minimums on the slope given a length.
	 */
	private final int[] RDSlopesRates;
	/**
	 * Packet encoder.
	 */
	private final PktEncoder pktEnc;
	/**
	 * The layer specifications
	 */
	private final LayersInfo lyrSpec;
	/**
	 * The wall time for the initialization.
	 */
	private long initTime;
	/**
	 * The wall time for the building of layers.
	 */
	private long buildTime;
	/**
	 * The wall time for the writing of layers.
	 */
	private long writeTime;
	/**
	 * Number of precincts in each resolution level:
	 *
	 * <ul>
	 * <li>1st dim: tile index.</li>
	 * <li>2nd dim: component index.</li>
	 * <li>3nd dim: resolution level index.</li>
	 * </ul>
	 */
	private Coord[][][] numPrec;
	/**
	 * Array containing the layers information.
	 */
	private EBCOTLayer[] layers;
	/**
	 * The maximum slope accross all code-blocks and truncation points.
	 */
	private float maxSlope;

	/**
	 * The minimum slope accross all code-blocks and truncation points.
	 */
	private float minSlope;

	/**
	 * Initializes the EBCOT rate allocator of entropy coded data. The layout of
	 * layers, and their bitrate constraints, is specified by the 'lyrs'
	 * parameter.
	 *
	 * @param src    The source of entropy coded data.
	 * @param lyrs   The layers layout specification.
	 * @param writer The bit stream writer.
	 * @see ProgressionType
	 */
	@SuppressWarnings ("deprecation")
	public EBCOTRateAllocator (
			final CodedCBlkDataSrcEnc src , final LayersInfo lyrs , final CodestreamWriter writer , final EncoderSpecs encSpec ,
			final ParameterList pl
	) {

		super ( src , lyrs.getTotNumLayers ( ) , writer , encSpec );

		int minsbi, maxsbi;
		int i;
		SubbandAn sb, sb2;
		Coord ncblks = null;

		// If we do timing create necessary structures
		if ( EBCOTRateAllocator.DO_TIMING ) {
			// If we are timing make sure that 'finalize' gets called.
			//System.runFinalizersOnExit(true);
			// The System.runFinalizersOnExit() method is deprecated in Java
			// 1.2 since it can cause a deadlock in some cases. However, here
			// we use it only for profiling purposes and is disabled in
			// production code.
			this.initTime = 0L;
			this.buildTime = 0L;
			this.writeTime = 0L;
		}

		// Save the layer specs
		this.lyrSpec = lyrs;

		// Initialize the size of the RD slope rates array
		this.RDSlopesRates = new int[ EBCOTRateAllocator.RD_SUMMARY_SIZE ];

		// Get number of tiles, components
		final int nt = src.getNumTiles ( );
		final int nc = this.getNumComps ( );

		// Allocate the coded code-blocks and truncation points indexes arrays
		this.cblks = new CBlkRateDistStats[ nt ][ nc ][][][];
		this.truncIdxs = new int[ nt ][ this.numLayers ][ nc ][][][];

		int cblkPerSubband; // Number of code-blocks per subband
		int mrl; // Number of resolution levels
		int l; // layer index
		int s; // subband index

		// Used to compute the maximum number of precincts for each resolution
		// level
		int tx0, ty0, tx1, ty1; // Current tile position in the reference grid
		int tcx0, tcy0, tcx1, tcy1; // Current tile position in the domain of
		// the image component
		int trx0, try0, trx1, try1; // Current tile position in the reduced
		// resolution image domain
		int xrsiz, yrsiz; // Component sub-sampling factors
		Coord tileI = null;
		Coord nTiles = null;
		int xsiz, ysiz, x0siz, y0siz;
		int xt0siz, yt0siz;
		int xtsiz, ytsiz;

		final int cb0x = src.getCbULX ( );
		final int cb0y = src.getCbULY ( );

		src.setTile ( 0 , 0 );
		for ( int t = 0 ; t < nt ; t++ ) { // Loop on tiles
			nTiles = src.getNumTiles ( nTiles );
			tileI = src.getTile ( tileI );
			x0siz = this.getImgULX ( );
			y0siz = this.getImgULY ( );
			xsiz = x0siz + this.getImgWidth ( );
			ysiz = y0siz + this.getImgHeight ( );
			xt0siz = src.getTilePartULX ( );
			yt0siz = src.getTilePartULY ( );
			xtsiz = src.getNomTileWidth ( );
			ytsiz = src.getNomTileHeight ( );

			// Tile's coordinates on the reference grid
			tx0 = ( 0 == tileI.x ) ? x0siz : xt0siz + tileI.x * xtsiz;
			ty0 = ( 0 == tileI.y ) ? y0siz : yt0siz + tileI.y * ytsiz;
			tx1 = ( tileI.x != nTiles.x - 1 ) ? xt0siz + ( tileI.x + 1 ) * xtsiz : xsiz;
			ty1 = ( tileI.y != nTiles.y - 1 ) ? yt0siz + ( tileI.y + 1 ) * ytsiz : ysiz;

			for ( int c = 0 ; c < nc ; c++ ) { // loop on components

				// Get the number of resolution levels
				sb = src.getAnSubbandTree ( t , c );
				mrl = sb.resLvl + 1;

				// Initialize maximum number of precincts per resolution array
				if ( null == numPrec ) {
					this.numPrec = new Coord[ nt ][ nc ][];
				}
				if ( null == numPrec[ t ][ c ] ) {
					this.numPrec[ t ][ c ] = new Coord[ mrl ];
				}

				// Subsampling factors
				xrsiz = src.getCompSubsX ( c );
				yrsiz = src.getCompSubsY ( c );

				// Tile's coordinates in the image component domain
				tcx0 = ( int ) Math.ceil ( tx0 / ( double ) ( xrsiz ) );
				tcy0 = ( int ) Math.ceil ( ty0 / ( double ) ( yrsiz ) );
				tcx1 = ( int ) Math.ceil ( tx1 / ( double ) ( xrsiz ) );
				tcy1 = ( int ) Math.ceil ( ty1 / ( double ) ( yrsiz ) );

				this.cblks[ t ][ c ] = new CBlkRateDistStats[ mrl ][][];

				for ( l = 0; l < this.numLayers ; l++ ) {
					this.truncIdxs[ t ][ l ][ c ] = new int[ mrl ][][];
				}

				for ( int r = 0 ; r < mrl ; r++ ) { // loop on resolution levels

					// Tile's coordinates in the reduced resolution image
					// domain
					trx0 = ( int ) Math.ceil ( tcx0 / ( double ) ( 1 << ( mrl - 1 - r ) ) );
					try0 = ( int ) Math.ceil ( tcy0 / ( double ) ( 1 << ( mrl - 1 - r ) ) );
					trx1 = ( int ) Math.ceil ( tcx1 / ( double ) ( 1 << ( mrl - 1 - r ) ) );
					try1 = ( int ) Math.ceil ( tcy1 / ( double ) ( 1 << ( mrl - 1 - r ) ) );

					// Calculate the maximum number of precincts for each
					// resolution level taking into account tile specific
					// options.
					final double twoppx = encSpec.pss.getPPX ( t , c , r );
					final double twoppy = encSpec.pss.getPPY ( t , c , r );
					this.numPrec[ t ][ c ][ r ] = new Coord ( );
					if ( trx1 > trx0 ) {
						this.numPrec[ t ][ c ][ r ].x = ( int ) Math.ceil ( ( trx1 - cb0x ) / twoppx )
								- ( int ) Math.floor ( ( trx0 - cb0x ) / twoppx );
					}
					else {
						this.numPrec[ t ][ c ][ r ].x = 0;
					}
					if ( try1 > try0 ) {
						this.numPrec[ t ][ c ][ r ].y = ( int ) Math.ceil ( ( try1 - cb0y ) / twoppy )
								- ( int ) Math.floor ( ( try0 - cb0y ) / twoppy );
					}
					else {
						this.numPrec[ t ][ c ][ r ].y = 0;
					}

					minsbi = ( 0 == r ) ? 0 : 1;
					maxsbi = ( 0 == r ) ? 1 : 4;

					this.cblks[ t ][ c ][ r ] = new CBlkRateDistStats[ maxsbi ][];
					for ( l = 0; l < this.numLayers ; l++ ) {
						this.truncIdxs[ t ][ l ][ c ][ r ] = new int[ maxsbi ][];
					}

					for ( s = minsbi; s < maxsbi ; s++ ) { // loop on subbands
						// Get the number of blocks in the current subband
						sb2 = ( SubbandAn ) sb.getSubbandByIdx ( r , s );
						ncblks = sb2.numCb;
						cblkPerSubband = ncblks.x * ncblks.y;
						this.cblks[ t ][ c ][ r ][ s ] = new CBlkRateDistStats[ cblkPerSubband ];

						for ( l = 0; l < this.numLayers ; l++ ) {
							this.truncIdxs[ t ][ l ][ c ][ r ][ s ] = new int[ cblkPerSubband ];
							for ( i = 0; i < cblkPerSubband ; i++ ) {
								this.truncIdxs[ t ][ l ][ c ][ r ][ s ][ i ] = - 1;
							}
						}
					} // End loop on subbands
				} // End lopp on resolution levels
			} // End loop on components
			if ( t != nt - 1 ) {
				src.nextTile ( );
			}
		} // End loop on tiles

		// Initialize the packet encoder
		this.pktEnc = new PktEncoder ( src , encSpec , this.numPrec , pl );

		// The layers array has to be initialized after the constructor since
		// it is needed that the bit stream header has been entirely written
	}

	/**
	 * Returns the index of a slope for the summary table, limiting to the
	 * admissible values. The index is calculated as RD_SUMMARY_OFF plus the
	 * maximum exponent, base 2, that yields a value not larger than the slope
	 * itself.
	 *
	 * <p>
	 * If the value to return is lower than 0, 0 is returned. If it is larger
	 * than the maximum table index, then the maximum is returned.
	 *
	 * @param slope The slope value
	 * @return The index for the summary table of the slope.
	 */
	private static int getLimitedSIndexFromSlope ( final float slope ) {
		final int idx;

		idx = ( int ) Math.floor ( Math.log ( slope ) / EBCOTRateAllocator.LOG2 ) + EBCOTRateAllocator.RD_SUMMARY_OFF;

		if ( 0 > idx ) {
			return 0;
		}
		else if ( RD_SUMMARY_SIZE <= idx ) {
			return EBCOTRateAllocator.RD_SUMMARY_SIZE - 1;
		}
		else {
			return idx;
		}
	}

	/**
	 * Returns the minimum slope value associated with a summary table index.
	 * This minimum slope is just 2^(index-RD_SUMMARY_OFF).
	 *
	 * @param index The summary index value.
	 * @return The minimum slope value associated with a summary table index.
	 */
	private static float getSlopeFromSIndex ( final int index ) {
		return ( float ) Math.pow ( 2 , ( index - EBCOTRateAllocator.RD_SUMMARY_OFF ) );
	}

	/**
	 * Prints the timing information, if collected, and calls 'finalize' on the
	 * super class.
	 */
	@Override
	protected void finalize ( ) throws Throwable {
		if ( EBCOTRateAllocator.DO_TIMING ) {
			final StringBuffer sb;

			sb = new StringBuffer ( "EBCOTRateAllocator wall clock times:\n" );
			sb.append ( "  initialization: " );
			sb.append ( this.initTime );
			sb.append ( " ms\n" );
			sb.append ( "  layer building: " );
			sb.append ( this.buildTime );
			sb.append ( " ms\n" );
			sb.append ( "  final writing:  " );
			sb.append ( this.writeTime );
			sb.append ( " ms" );
			FacilityManager.getMsgLogger ( ).printmsg ( MsgLogger.INFO , sb.toString ( ) );
		}
		super.finalize ( );
	}

	/**
	 * Runs the rate allocation algorithm and writes the data to the bit stream
	 * writer object provided to the constructor.
	 */
	@Override
	public void runAndWrite ( ) throws IOException {
		// Now, run the rate allocation
		this.buildAndWriteLayers ( );
	}

	/**
	 * Initializes the layers array. This must be called after the main header
	 * has been entirely written or simulated, so as to take its overhead into
	 * account. This method will get all the code-blocks and then initialize the
	 * target bitrates for each layer, according to the specifications.
	 */
	@Override
	public void initialize ( ) throws IOException {
		int n, i, l;
		int ho; // The header overhead (in bytes)
		final float np;// The number of pixels divided by the number of bits per byte
		double ls; // Step for log-scale
		double basebytes;
		int lastbytes, newbytes, nextbytes;
		int loopnlyrs;
		int minlsz; // The minimum allowable number of bytes in a layer
		int totenclength;
		int maxpkt;
		final int numTiles = this.src.getNumTiles ( );
		final int numComps = this.src.getNumComps ( );
		int numLvls;
		int avgPktLen;

		long stime = 0L;

		// Start by getting all the code-blocks, we need this in order to have
		// an idea of the total encoded bitrate.
		this.getAllCodeBlocks ( );

		if ( EBCOTRateAllocator.DO_TIMING )
			stime = System.currentTimeMillis ( );

		// Now get the total encoded length
		totenclength = this.RDSlopesRates[ 0 ]; // all the encoded data
		// Make a rough estimation of the packet head overhead, as 2 bytes per
		// packet in average (plus EPH / SOP) , and add that to the total
		// encoded length
		for ( int t = 0 ; t < numTiles ; t++ ) {
			avgPktLen = 2;
			// Add SOP length if set
			if ( "on".equalsIgnoreCase ( ( String ) encSpec.sops.getTileDef ( t ) ) ) {
				avgPktLen += Markers.SOP_LENGTH;
			}
			// Add EPH length if set
			if ( "on".equalsIgnoreCase ( ( String ) encSpec.ephs.getTileDef ( t ) ) ) {
				avgPktLen += Markers.EPH_LENGTH;
			}

			for ( int c = 0 ; c < numComps ; c++ ) {
				numLvls = this.src.getAnSubbandTree ( t , c ).resLvl + 1;
				if ( ! this.src.precinctPartitionUsed ( c , t ) ) {
					// Precinct partition is not used so there is only
					// one packet per resolution level/layer
					totenclength += this.numLayers * avgPktLen * numLvls;
				}
				else {
					// Precinct partition is used so for each
					// component/tile/resolution level, we get the maximum
					// number of packets
					for ( int rl = 0 ; rl < numLvls ; rl++ ) {
						maxpkt = this.numPrec[ t ][ c ][ rl ].x * this.numPrec[ t ][ c ][ rl ].y;
						totenclength += this.numLayers * avgPktLen * maxpkt;
					}
				}
			} // End loop on components
		} // End loop on tiles

		// If any layer specifies more than 'totenclength' as its target
		// length then 'totenclength' is used. This is to prevent that
		// estimated layers get excessively large target lengths due to an
		// excessively large target bitrate. At the end the last layer is set
		// to the target length corresponding to the overall target
		// bitrate. Thus, 'totenclength' can not limit the total amount of
		// encoded data, as intended.

		ho = this.headEnc.getLength ( );
		np = this.src.getImgWidth ( ) * this.src.getImgHeight ( ) / 8.0f;

		// SOT marker must be taken into account
		for ( int t = 0 ; t < numTiles ; t++ ) {
			this.headEnc.reset ( );
			this.headEnc.encodeTilePartHeader ( 0 , t );
			ho += this.headEnc.getLength ( );
		}

		this.layers = new EBCOTLayer[ this.numLayers ];
		for ( n = this.numLayers - 1; 0 <= n ; n-- ) {
			this.layers[ n ] = new EBCOTLayer ( );
		}

		minlsz = 0; // To keep compiler happy
		for ( int t = 0 ; t < numTiles ; t++ ) {
			for ( int c = 0 ; c < numComps ; c++ ) {
				numLvls = this.src.getAnSubbandTree ( t , c ).resLvl + 1;

				if ( ! this.src.precinctPartitionUsed ( c , t ) ) {
					// Precinct partition is not used
					minlsz += EBCOTRateAllocator.MIN_AVG_PACKET_SZ * numLvls;
				}
				else {
					// Precinct partition is used
					for ( int rl = 0 ; rl < numLvls ; rl++ ) {
						maxpkt = this.numPrec[ t ][ c ][ rl ].x * this.numPrec[ t ][ c ][ rl ].y;
						minlsz += EBCOTRateAllocator.MIN_AVG_PACKET_SZ * maxpkt;
					}
				}
			} // End loop on components
		} // End loop on tiles

		// Initialize layers
		n = 0;
		i = 0;
		lastbytes = 0;

		while ( n < this.numLayers - 1 ) {
			// At an optimized layer
			basebytes = Math.floor ( this.lyrSpec.getTargetBitrate ( i ) * np );
			if ( i < this.lyrSpec.getNOptPoints ( ) - 1 ) {
				nextbytes = ( int ) ( this.lyrSpec.getTargetBitrate ( i + 1 ) * np );
				// Limit target length to 'totenclength'
				if ( nextbytes > totenclength )
					nextbytes = totenclength;
			}
			else {
				nextbytes = 1;
			}
			loopnlyrs = this.lyrSpec.getExtraLayers ( i ) + 1;
			ls = Math.exp ( Math.log ( nextbytes / basebytes ) / loopnlyrs );
			this.layers[ n ].optimize = true;
			for ( l = 0; l < loopnlyrs ; l++ ) {
				newbytes = ( int ) basebytes - lastbytes - ho;
				if ( newbytes < minlsz ) { // Skip layer (too small)
					basebytes *= ls;
					this.numLayers--;
					continue;
				}
				lastbytes = ( int ) basebytes - ho;
				this.layers[ n ].maxBytes = lastbytes;
				basebytes *= ls;
				n++;
			}
			i++; // Goto next optimization point
		}

		// Ensure minimum size of last layer (this one determines overall
		// bitrate)
		n = this.numLayers - 2;
		nextbytes = ( int ) ( this.lyrSpec.getTotBitrate ( ) * np ) - ho;
		newbytes = nextbytes - ( ( 0 <= n ) ? this.layers[ n ].maxBytes : 0 );
		while ( newbytes < minlsz ) {
			if ( 1 == numLayers ) {
				if ( 0 >= newbytes ) {
					throw new IllegalArgumentException ( "Overall target bitrate too low, given the current "
							+ "bit stream header overhead" );
				}
				break;
			}
			// Delete last layer
			this.numLayers--;
			n--;
			newbytes = nextbytes - ( ( 0 <= n ) ? this.layers[ n ].maxBytes : 0 );
		}
		// Set last layer to the overall target bitrate
		n++;
		this.layers[ n ].maxBytes = nextbytes;
		this.layers[ n ].optimize = true;

		// Re-initialize progression order changes if needed Default values
		Progression[] prog1;
		prog1 = ( Progression[] ) this.encSpec.pocs.getDefault ( );
		int nValidProg = prog1.length;
		for ( int prg = 0 ; prg < prog1.length ; prg++ ) {
			if ( prog1[ prg ].lye > this.numLayers ) {
				prog1[ prg ].lye = this.numLayers;
			}
		}
		if ( 0 == nValidProg ) {
			throw new Error ( "Unable to initialize rate allocator: No default progression type has been defined." );
		}

		// Tile specific values
		for ( int t = 0 ; t < numTiles ; t++ ) {
			if ( this.encSpec.pocs.isTileSpecified ( t ) ) {
				prog1 = ( Progression[] ) this.encSpec.pocs.getTileDef ( t );
				nValidProg = prog1.length;
				for ( int prg = 0 ; prg < prog1.length ; prg++ ) {
					if ( prog1[ prg ].lye > this.numLayers ) {
						prog1[ prg ].lye = this.numLayers;
					}
				}
				if ( 0 == nValidProg ) {
					throw new Error ( "Unable to initialize rate allocator: No "
							+ "progression type has been defined for tile " + t );
				}
			}
		} // End loop on tiles

		if ( EBCOTRateAllocator.DO_TIMING )
			this.initTime += System.currentTimeMillis ( ) - stime;
	}

	/**
	 * This method gets all the coded code-blocks from the EBCOT entropy coder
	 * for every component and every tile. Each coded code-block is stored in a
	 * 5D array according to the component, the resolution level, the tile, the
	 * subband it belongs and its position in the subband.
	 *
	 * <p>
	 * For each code-block, the valid slopes are computed and converted into the
	 * mantissa-exponent representation.
	 */
	private void getAllCodeBlocks ( ) {

		final int numComps;
		final int numTiles;
		int c, r, t, s, sidx, k;
		SubbandAn subb;
		CBlkRateDistStats ccb = null;
		Coord ncblks = null;
		int last_sidx;
		float fslope;

		long stime = 0L;

		this.maxSlope = 0.0f;
		this.minSlope = Float.MAX_VALUE;

		// Get the number of components and tiles
		numComps = this.src.getNumComps ( );
		numTiles = this.src.getNumTiles ( );

		SubbandAn root, sb;
		int cblkToEncode = 0;
		int nEncCblk = 0;
		final ProgressWatch pw = FacilityManager.getProgressWatch ( );

		// Get all coded code-blocks Goto first tile
		this.src.setTile ( 0 , 0 );
		for ( t = 0; t < numTiles ; t++ ) { // loop on tiles
			nEncCblk = 0;
			cblkToEncode = 0;
			for ( c = 0; c < numComps ; c++ ) {
				root = this.src.getAnSubbandTree ( t , c );
				for ( r = 0; r <= root.resLvl ; r++ ) {
					if ( 0 == r ) {
						sb = ( SubbandAn ) root.getSubbandByIdx ( 0 , 0 );
						if ( null != sb )
							cblkToEncode += sb.numCb.x * sb.numCb.y;
					}
					else {
						sb = ( SubbandAn ) root.getSubbandByIdx ( r , 1 );
						if ( null != sb )
							cblkToEncode += sb.numCb.x * sb.numCb.y;
						sb = ( SubbandAn ) root.getSubbandByIdx ( r , 2 );
						if ( null != sb )
							cblkToEncode += sb.numCb.x * sb.numCb.y;
						sb = ( SubbandAn ) root.getSubbandByIdx ( r , 3 );
						if ( null != sb )
							cblkToEncode += sb.numCb.x * sb.numCb.y;
					}
				}
			}
			if ( null != pw ) {
				pw.initProgressWatch ( 0 , cblkToEncode , "Encoding tile " + t + "..." );
			}

			for ( c = 0; c < numComps ; c++ ) { // loop on components

				// Get next coded code-block coordinates
				while ( null != ( ccb = src.getNextCodeBlock ( c , ccb ) ) ) {
					if ( EBCOTRateAllocator.DO_TIMING )
						stime = System.currentTimeMillis ( );

					if ( null != pw ) {
						nEncCblk++;
						pw.updateProgressWatch ( nEncCblk , null );
					}

					subb = ccb.sb;

					// Get the coded code-block resolution level index
					r = subb.resLvl;

					// Get the coded code-block subband index
					s = subb.sbandIdx;

					// Get the number of blocks in the current subband
					ncblks = subb.numCb;

					// Add code-block contribution to summary R-D table
					// RDSlopesRates
					last_sidx = - 1;
					for ( k = ccb.nVldTrunc - 1; 0 <= k ; k-- ) {
						fslope = ccb.truncSlopes[ k ];
						if ( fslope > this.maxSlope )
							this.maxSlope = fslope;
						if ( fslope < this.minSlope )
							this.minSlope = fslope;
						sidx = EBCOTRateAllocator.getLimitedSIndexFromSlope ( fslope );
						for ( ; sidx > last_sidx ; sidx-- ) {
							this.RDSlopesRates[ sidx ] += ccb.truncRates[ ccb.truncIdxs[ k ] ];
						}
						last_sidx = EBCOTRateAllocator.getLimitedSIndexFromSlope ( fslope );
					}

					// Fills code-blocks array
					this.cblks[ t ][ c ][ r ][ s ][ ( ccb.m * ncblks.x ) + ccb.n ] = ccb;
					ccb = null;

					if ( EBCOTRateAllocator.DO_TIMING )
						this.initTime += System.currentTimeMillis ( ) - stime;
				}
			}

			if ( null != pw ) {
				pw.terminateProgressWatch ( );
			}

			// Goto next tile
			if ( t < numTiles - 1 ) // not at last tile
				this.src.nextTile ( );
		}
	}

	/**
	 * This method builds all the bit stream layers and then writes them to the
	 * output bit stream. Firstly it builds all the layers by computing the
	 * threshold according to the layer target bit-rate, and then it writes the
	 * layer bit streams according to the progressive type.
	 */
	private void buildAndWriteLayers ( ) throws IOException {
		int nPrec = 0;
		int maxBytes, actualBytes;
		float rdThreshold;
		SubbandAn sb;
		BitOutputBuffer hBuff = null;
		final byte[] bBuff = null;
		final int[] tileLengths; // Length of each tile
		int tmp;
		boolean sopUsed; // Should SOP markers be used ?
		boolean ephUsed; // Should EPH markers be used ?
		final int nc = this.src.getNumComps ( );
		final int nt = this.src.getNumTiles ( );
		int mrl;

		long stime = 0L;

		if ( EBCOTRateAllocator.DO_TIMING )
			stime = System.currentTimeMillis ( );

		// Start with the maximum slope
		rdThreshold = this.maxSlope;

		tileLengths = new int[ nt ];
		actualBytes = 0;

		// +------------------------------+
		// | First we build the layers |
		// +------------------------------+
		// Bitstream is simulated to know tile length
		for ( int l = 0 ; l < this.numLayers ; l++ ) { // loop on layers

			maxBytes = this.layers[ l ].maxBytes;
			if ( this.layers[ l ].optimize ) {
				rdThreshold = this.optimizeBitstreamLayer ( l , rdThreshold , maxBytes , actualBytes );
			}
			else {
				if ( 0 >= l || l >= this.numLayers - 1 ) {
					throw new IllegalArgumentException ( "The first and the last layer thresholds must be optimized" );
				}
				rdThreshold = this.estimateLayerThreshold ( maxBytes , this.layers[ l - 1 ] );
			}

			for ( int t = 0 ; t < nt ; t++ ) { // loop on tiles
				if ( 0 == l ) {
					// Tile header
					this.headEnc.reset ( );
					this.headEnc.encodeTilePartHeader ( 0 , t );
					tileLengths[ t ] += this.headEnc.getLength ( );
				}

				for ( int c = 0 ; c < nc ; c++ ) { // loop on components

					// set boolean sopUsed here (SOP markers)
					sopUsed = "on".equalsIgnoreCase ( ( String ) encSpec.sops.getTileDef ( t ) );
					// set boolean ephUsed here (EPH markers)
					ephUsed = "on".equalsIgnoreCase ( ( String ) encSpec.ephs.getTileDef ( t ) );

					// Go to LL band
					sb = this.src.getAnSubbandTree ( t , c );
					mrl = sb.resLvl + 1;

					while ( null != sb.subb_LL ) {
						sb = sb.subb_LL;
					}

					for ( int r = 0 ; r < mrl ; r++ ) { // loop on resolution levels

						nPrec = this.numPrec[ t ][ c ][ r ].x * this.numPrec[ t ][ c ][ r ].y;
						for ( int p = 0 ; p < nPrec ; p++ ) { // loop on precincts

							this.findTruncIndices ( l , c , r , t , sb , rdThreshold , p );

							hBuff = this.pktEnc.encodePacket ( l + 1 , c , r , t , this.cblks[ t ][ c ][ r ] , this.truncIdxs[ t ][ l ][ c ][ r ] , hBuff ,
									bBuff , p
							);
							if ( this.pktEnc.isPacketWritable ( ) ) {
								tmp = this.bsWriter.writePacketHead ( hBuff.getBuffer ( ) , hBuff.getLength ( ) , true , sopUsed ,
										ephUsed
								);
								tmp += this.bsWriter.writePacketBody ( this.pktEnc.getLastBodyBuf ( ) , this.pktEnc.getLastBodyLen ( ) , true ,
										this.pktEnc.isROIinPkt ( ) , this.pktEnc.getROILen ( )
								);
								actualBytes += tmp;
								tileLengths[ t ] += tmp;
							}
						} // End loop on precincts
						sb = sb.parent;
					} // End loop on resolution levels
				} // End loop on components
			} // end loop on tiles
			this.layers[ l ].rdThreshold = rdThreshold;
			this.layers[ l ].actualBytes = actualBytes;
		} // end loop on layers

		if ( EBCOTRateAllocator.DO_TIMING )
			this.buildTime += System.currentTimeMillis ( ) - stime;

		// The bit-stream was not yet generated (only simulated).

		if ( EBCOTRateAllocator.DO_TIMING )
			stime = System.currentTimeMillis ( );

		// +--------------------------------------------------+
		// | Write tiles according to their Progression order |
		// +--------------------------------------------------+
		// Reset the packet encoder before writing all packets
		this.pktEnc.reset ( );
		Progression[] prog; // Progression(s) in each tile
		int cs, ce, rs, re, lye;

		final int[] mrlc = new int[ nc ];
		for ( int t = 0 ; t < nt ; t++ ) { // loop on tiles
			// resolution level
			final int[][] lys = new int[ nc ][];
			for ( int c = 0 ; c < nc ; c++ ) {
				mrlc[ c ] = this.src.getAnSubbandTree ( t , c ).resLvl;
				lys[ c ] = new int[ mrlc[ c ] + 1 ];
			}

			// Tile header
			this.headEnc.reset ( );
			this.headEnc.encodeTilePartHeader ( tileLengths[ t ] , t );
			this.bsWriter.commitBitstreamHeader ( this.headEnc );
			prog = ( Progression[] ) this.encSpec.pocs.getTileDef ( t );

			for ( int prg = 0 ; prg < prog.length ; prg++ ) { // Loop on progression
				lye = prog[ prg ].lye;
				cs = prog[ prg ].cs;
				ce = prog[ prg ].ce;
				rs = prog[ prg ].rs;
				re = prog[ prg ].re;

				switch ( prog[ prg ].type ) {
					case ProgressionType.RES_LY_COMP_POS_PROG:
						this.writeResLyCompPos ( t , rs , re , cs , ce , lys , lye );
						break;
					case ProgressionType.LY_RES_COMP_POS_PROG:
						this.writeLyResCompPos ( t , rs , re , cs , ce , lys , lye );
						break;
					case ProgressionType.POS_COMP_RES_LY_PROG:
						this.writePosCompResLy ( t , rs , re , cs , ce , lys , lye );
						break;
					case ProgressionType.COMP_POS_RES_LY_PROG:
						this.writeCompPosResLy ( t , rs , re , cs , ce , lys , lye );
						break;
					case ProgressionType.RES_POS_COMP_LY_PROG:
						this.writeResPosCompLy ( t , rs , re , cs , ce , lys , lye );
						break;
					default:
						throw new Error ( "Unsupported bit stream progression type" );
				} // switch on progression

				// Update next first layer index
				for ( int c = cs ; c < ce ; c++ )
					for ( int r = rs ; r < re ; r++ ) {
						if ( r > mrlc[ c ] )
							continue;
						lys[ c ][ r ] = lye;
					}
			} // End loop on progression
		} // End loop on tiles

		if ( EBCOTRateAllocator.DO_TIMING )
			this.writeTime += System.currentTimeMillis ( ) - stime;
	}

	/**
	 * Write a piece of bit stream according to the RES_LY_COMP_POS_PROG
	 * progression mode and between given bounds
	 *
	 * @param t   Tile index.
	 * @param rs  First resolution level index.
	 * @param re  Last resolution level index.
	 * @param cs  First component index.
	 * @param ce  Last component index.
	 * @param lys First layer index for each component and resolution.
	 * @param lye Index of the last layer.
	 */
	public void writeResLyCompPos ( final int t , final int rs , final int re , final int cs , final int ce , final int[][] lys , final int lye ) throws IOException {

		boolean sopUsed; // Should SOP markers be used ?
		boolean ephUsed; // Should EPH markers be used ?
		final int nc = this.src.getNumComps ( );
		final int[] mrl = new int[ nc ];
		SubbandAn sb;
		float threshold;
		BitOutputBuffer hBuff = null;
		final byte[] bBuff = null;
		int nPrec = 0;

		// Max number of resolution levels in the tile
		int maxResLvl = 0;
		for ( int c = 0 ; c < nc ; c++ ) {
			mrl[ c ] = this.src.getAnSubbandTree ( t , c ).resLvl;
			if ( mrl[ c ] > maxResLvl )
				maxResLvl = mrl[ c ];
		}

		int minlys; // minimum layer start index of each component

		for ( int r = rs ; r < re ; r++ ) { // loop on resolution levels
			if ( r > maxResLvl )
				continue;

			minlys = 100000;
			for ( int c = cs ; c < ce ; c++ ) {
				if ( r < lys[ c ].length && lys[ c ][ r ] < minlys ) {
					minlys = lys[ c ][ r ];
				}
			}

			for ( int l = minlys ; l < lye ; l++ ) { // loop on layers
				for ( int c = cs ; c < ce ; c++ ) {// loop on components
					if ( r >= lys[ c ].length )
						continue;
					if ( l < lys[ c ][ r ] )
						continue;

					// If no more decomposition levels for this component
					if ( r > mrl[ c ] )
						continue;

					nPrec = this.numPrec[ t ][ c ][ r ].x * this.numPrec[ t ][ c ][ r ].y;
					for ( int p = 0 ; p < nPrec ; p++ ) { // loop on precincts

						// set boolean sopUsed here (SOP markers)
						sopUsed = this.encSpec.sops.getTileDef ( t ).equals ( "on" );
						// set boolean ephUsed here (EPH markers)
						ephUsed = this.encSpec.ephs.getTileDef ( t ).equals ( "on" );

						sb = this.src.getAnSubbandTree ( t , c );
						for ( int i = mrl[ c ] ; i > r ; i-- ) {
							sb = sb.subb_LL;
						}

						threshold = this.layers[ l ].rdThreshold;
						this.findTruncIndices ( l , c , r , t , sb , threshold , p );

						hBuff = this.pktEnc.encodePacket ( l + 1 , c , r , t , this.cblks[ t ][ c ][ r ] , this.truncIdxs[ t ][ l ][ c ][ r ] , hBuff ,
								bBuff , p
						);

						if ( this.pktEnc.isPacketWritable ( ) ) {
							this.bsWriter.writePacketHead ( hBuff.getBuffer ( ) , hBuff.getLength ( ) , false , sopUsed , ephUsed );
							this.bsWriter.writePacketBody ( this.pktEnc.getLastBodyBuf ( ) , this.pktEnc.getLastBodyLen ( ) , false ,
									this.pktEnc.isROIinPkt ( ) , this.pktEnc.getROILen ( )
							);
						}

					} // End loop on precincts
				} // End loop on components
			} // End loop on layers
		} // End loop on resolution levels
	}

	/**
	 * Write a piece of bit stream according to the LY_RES_COMP_POS_PROG
	 * progression mode and between given bounds
	 *
	 * @param t   Tile index.
	 * @param rs  First resolution level index.
	 * @param re  Last resolution level index.
	 * @param cs  First component index.
	 * @param ce  Last component index.
	 * @param lys First layer index for each component and resolution.
	 * @param lye Index of the last layer.
	 */
	public void writeLyResCompPos ( final int t , final int rs , final int re , final int cs , final int ce , final int[][] lys , final int lye ) throws IOException {

		boolean sopUsed; // Should SOP markers be used ?
		boolean ephUsed; // Should EPH markers be used ?
		int mrl;
		SubbandAn sb;
		float threshold;
		BitOutputBuffer hBuff = null;
		final byte[] bBuff = null;
		int nPrec = 0;

		int minlys = 100000; // minimum layer start index of each component
		for ( int c = cs ; c < ce ; c++ ) {
			for ( int r = 0 ; r < lys.length ; r++ ) {
				if ( null != lys[ c ] && r < lys[ c ].length && lys[ c ][ r ] < minlys ) {
					minlys = lys[ c ][ r ];
				}
			}
		}

		for ( int l = minlys ; l < lye ; l++ ) { // loop on layers
			for ( int r = rs ; r < re ; r++ ) { // loop on resolution level
				for ( int c = cs ; c < ce ; c++ ) { // loop on components
					mrl = this.src.getAnSubbandTree ( t , c ).resLvl;
					if ( r > mrl )
						continue;
					if ( r >= lys[ c ].length )
						continue;
					if ( l < lys[ c ][ r ] )
						continue;

					nPrec = this.numPrec[ t ][ c ][ r ].x * this.numPrec[ t ][ c ][ r ].y;
					for ( int p = 0 ; p < nPrec ; p++ ) { // loop on precincts

						// set boolean sopUsed here (SOP markers)
						sopUsed = this.encSpec.sops.getTileDef ( t ).equals ( "on" );
						// set boolean ephUsed here (EPH markers)
						ephUsed = this.encSpec.ephs.getTileDef ( t ).equals ( "on" );

						sb = this.src.getAnSubbandTree ( t , c );
						for ( int i = mrl ; i > r ; i-- ) {
							sb = sb.subb_LL;
						}

						threshold = this.layers[ l ].rdThreshold;
						this.findTruncIndices ( l , c , r , t , sb , threshold , p );

						hBuff = this.pktEnc.encodePacket ( l + 1 , c , r , t , this.cblks[ t ][ c ][ r ] , this.truncIdxs[ t ][ l ][ c ][ r ] , hBuff ,
								bBuff , p
						);

						if ( this.pktEnc.isPacketWritable ( ) ) {
							this.bsWriter.writePacketHead ( hBuff.getBuffer ( ) , hBuff.getLength ( ) , false , sopUsed , ephUsed );
							this.bsWriter.writePacketBody ( this.pktEnc.getLastBodyBuf ( ) , this.pktEnc.getLastBodyLen ( ) , false ,
									this.pktEnc.isROIinPkt ( ) , this.pktEnc.getROILen ( )
							);
						}
					} // end loop on precincts
				} // end loop on components
			} // end loop on resolution levels
		} // end loop on layers
	}

	/**
	 * Write a piece of bit stream according to the COMP_POS_RES_LY_PROG
	 * progression mode and between given bounds
	 *
	 * @param t   Tile index.
	 * @param rs  First resolution level index.
	 * @param re  Last resolution level index.
	 * @param cs  First component index.
	 * @param ce  Last component index.
	 * @param lys First layer index for each component and resolution.
	 * @param lye Index of the last layer.
	 */
	public void writePosCompResLy ( final int t , final int rs , final int re , final int cs , final int ce , final int[][] lys , final int lye ) throws IOException {

		boolean sopUsed; // Should SOP markers be used ?
		boolean ephUsed; // Should EPH markers be used ?
		int mrl;
		SubbandAn sb;
		float threshold;
		BitOutputBuffer hBuff = null;
		final byte[] bBuff = null;

		// Computes current tile offset in the reference grid
		final Coord nTiles = this.src.getNumTiles ( null );
		final Coord tileI = this.src.getTile ( null );
		final int x0siz = this.src.getImgULX ( );
		final int y0siz = this.src.getImgULY ( );
		final int xsiz = x0siz + this.src.getImgWidth ( );
		final int ysiz = y0siz + this.src.getImgHeight ( );
		final int xt0siz = this.src.getTilePartULX ( );
		final int yt0siz = this.src.getTilePartULY ( );
		final int xtsiz = this.src.getNomTileWidth ( );
		final int ytsiz = this.src.getNomTileHeight ( );
		final int tx0 = ( 0 == tileI.x ) ? x0siz : xt0siz + tileI.x * xtsiz;
		final int ty0 = ( 0 == tileI.y ) ? y0siz : yt0siz + tileI.y * ytsiz;
		final int tx1 = ( tileI.x != nTiles.x - 1 ) ? xt0siz + ( tileI.x + 1 ) * xtsiz : xsiz;
		final int ty1 = ( tileI.y != nTiles.y - 1 ) ? yt0siz + ( tileI.y + 1 ) * ytsiz : ysiz;

		// Get precinct information (number,distance between two consecutive
		// precincts in the reference grid) in each component and resolution
		// level
		PrecInfo prec; // temporary variable
		int p; // Current precinct index
		int gcd_x = 0; // Horiz. distance between 2 precincts in the ref. grid
		int gcd_y = 0; // Vert. distance between 2 precincts in the ref. grid
		int nPrec = 0; // Total number of found precincts
		final int[][] nextPrec = new int[ ce ][]; // Next precinct index in each
		// component and resolution level
		int minlys = 100000; // minimum layer start index of each component
		int minx = tx1; // Horiz. offset of the second precinct in the
		// reference grid
		int miny = ty1; // Vert. offset of the second precinct in the
		// reference grid.
		int maxx = tx0; // Max. horiz. offset of precincts in the ref. grid
		int maxy = ty0; // Max. vert. offset of precincts in the ref. grid
		for ( int c = cs ; c < ce ; c++ ) {
			mrl = this.src.getAnSubbandTree ( t , c ).resLvl;
			nextPrec[ c ] = new int[ mrl + 1 ];
			for ( int r = rs ; r < re ; r++ ) {
				if ( r > mrl )
					continue;
				if ( r < lys[ c ].length && lys[ c ][ r ] < minlys ) {
					minlys = lys[ c ][ r ];
				}
				p = this.numPrec[ t ][ c ][ r ].y * this.numPrec[ t ][ c ][ r ].x - 1;
				for ( ; 0 <= p ; p-- ) {
					prec = this.pktEnc.getPrecInfo ( t , c , r , p );
					if ( prec.rgulx != tx0 ) {
						if ( prec.rgulx < minx )
							minx = prec.rgulx;
						if ( prec.rgulx > maxx )
							maxx = prec.rgulx;
					}
					if ( prec.rguly != ty0 ) {
						if ( prec.rguly < miny )
							miny = prec.rguly;
						if ( prec.rguly > maxy )
							maxy = prec.rguly;
					}

					if ( 0 == nPrec ) {
						gcd_x = prec.rgw;
						gcd_y = prec.rgh;
					}
					else {
						gcd_x = MathUtil.gcd ( gcd_x , prec.rgw );
						gcd_y = MathUtil.gcd ( gcd_y , prec.rgh );
					}
					nPrec++;
				} // precincts
			} // resolution levels
		} // components

		if ( 0 == nPrec ) {
			throw new Error ( "Image cannot have no precinct" );
		}

		final int pyend = ( maxy - miny ) / gcd_y + 1;
		final int pxend = ( maxx - minx ) / gcd_x + 1;
		int y = ty0;
		int x = tx0;
		for ( int py = 0 ; py <= pyend ; py++ ) { // Vertical precincts
			for ( int px = 0 ; px <= pxend ; px++ ) { // Horiz. precincts
				for ( int c = cs ; c < ce ; c++ ) { // Components
					mrl = this.src.getAnSubbandTree ( t , c ).resLvl;
					for ( int r = rs ; r < re ; r++ ) { // Resolution levels
						if ( r > mrl )
							continue;
						if ( nextPrec[ c ][ r ] >= this.numPrec[ t ][ c ][ r ].x * this.numPrec[ t ][ c ][ r ].y ) {
							continue;
						}
						prec = this.pktEnc.getPrecInfo ( t , c , r , nextPrec[ c ][ r ] );
						if ( ( prec.rgulx != x ) || ( prec.rguly != y ) ) {
							continue;
						}
						for ( int l = minlys ; l < lye ; l++ ) { // Layers
							if ( r >= lys[ c ].length )
								continue;
							if ( l < lys[ c ][ r ] )
								continue;

							// set boolean sopUsed here (SOP markers)
							sopUsed = this.encSpec.sops.getTileDef ( t ).equals ( "on" );
							// set boolean ephUsed here (EPH markers)
							ephUsed = this.encSpec.ephs.getTileDef ( t ).equals ( "on" );

							sb = this.src.getAnSubbandTree ( t , c );
							for ( int i = mrl ; i > r ; i-- ) {
								sb = sb.subb_LL;
							}

							threshold = this.layers[ l ].rdThreshold;
							this.findTruncIndices ( l , c , r , t , sb , threshold , nextPrec[ c ][ r ] );

							hBuff = this.pktEnc.encodePacket ( l + 1 , c , r , t , this.cblks[ t ][ c ][ r ] , this.truncIdxs[ t ][ l ][ c ][ r ] , hBuff ,
									bBuff , nextPrec[ c ][ r ]
							);

							if ( this.pktEnc.isPacketWritable ( ) ) {
								this.bsWriter.writePacketHead ( hBuff.getBuffer ( ) , hBuff.getLength ( ) , false , sopUsed , ephUsed );
								this.bsWriter.writePacketBody ( this.pktEnc.getLastBodyBuf ( ) , this.pktEnc.getLastBodyLen ( ) , false ,
										this.pktEnc.isROIinPkt ( ) , this.pktEnc.getROILen ( )
								);
							}
						} // layers
						nextPrec[ c ][ r ]++;
					} // Resolution levels
				} // Components
				if ( px != pxend ) {
					x = minx + px * gcd_x;
				}
				else {
					x = tx0;
				}
			} // Horizontal precincts
			if ( py != pyend ) {
				y = miny + py * gcd_y;
			}
			else {
				y = ty0;
			}
		} // Vertical precincts

		// Check that all precincts have been written
		for ( int c = cs ; c < ce ; c++ ) {
			mrl = this.src.getAnSubbandTree ( t , c ).resLvl;
			for ( int r = rs ; r < re ; r++ ) {
				if ( r > mrl )
					continue;
				if ( nextPrec[ c ][ r ] < this.numPrec[ t ][ c ][ r ].x * this.numPrec[ t ][ c ][ r ].y - 1 ) {
					throw new Error ( "JJ2000 bug: One precinct at least has not been written for resolution level "
							+ r + " of component " + c + " in tile " + t + "." );
				}
			}
		}
	}

	/**
	 * Write a piece of bit stream according to the COMP_POS_RES_LY_PROG
	 * progression mode and between given bounds
	 *
	 * @param t   Tile index.
	 * @param rs  First resolution level index.
	 * @param re  Last resolution level index.
	 * @param cs  First component index.
	 * @param ce  Last component index.
	 * @param lys First layer index for each component and resolution.
	 * @param lye Index of the last layer.
	 */
	public void writeCompPosResLy ( final int t , final int rs , final int re , final int cs , final int ce , final int[][] lys , final int lye ) throws IOException {

		boolean sopUsed; // Should SOP markers be used ?
		boolean ephUsed; // Should EPH markers be used ?
		int mrl;
		SubbandAn sb;
		float threshold;
		BitOutputBuffer hBuff = null;
		final byte[] bBuff = null;

		// Computes current tile offset in the reference grid
		final Coord nTiles = this.src.getNumTiles ( null );
		final Coord tileI = this.src.getTile ( null );
		final int x0siz = this.src.getImgULX ( );
		final int y0siz = this.src.getImgULY ( );
		final int xsiz = x0siz + this.src.getImgWidth ( );
		final int ysiz = y0siz + this.src.getImgHeight ( );
		final int xt0siz = this.src.getTilePartULX ( );
		final int yt0siz = this.src.getTilePartULY ( );
		final int xtsiz = this.src.getNomTileWidth ( );
		final int ytsiz = this.src.getNomTileHeight ( );
		final int tx0 = ( 0 == tileI.x ) ? x0siz : xt0siz + tileI.x * xtsiz;
		final int ty0 = ( 0 == tileI.y ) ? y0siz : yt0siz + tileI.y * ytsiz;
		final int tx1 = ( tileI.x != nTiles.x - 1 ) ? xt0siz + ( tileI.x + 1 ) * xtsiz : xsiz;
		final int ty1 = ( tileI.y != nTiles.y - 1 ) ? yt0siz + ( tileI.y + 1 ) * ytsiz : ysiz;

		// Get precinct information (number,distance between two consecutive
		// precincts in the reference grid) in each component and resolution
		// level
		PrecInfo prec; // temporary variable
		int p; // Current precinct index
		int gcd_x = 0; // Horiz. distance between 2 precincts in the ref. grid
		int gcd_y = 0; // Vert. distance between 2 precincts in the ref. grid
		int nPrec = 0; // Total number of found precincts
		final int[][] nextPrec = new int[ ce ][]; // Next precinct index in each
		// component and resolution level
		int minlys = 100000; // minimum layer start index of each component
		int minx = tx1; // Horiz. offset of the second precinct in the
		// reference grid
		int miny = ty1; // Vert. offset of the second precinct in the
		// reference grid.
		int maxx = tx0; // Max. horiz. offset of precincts in the ref. grid
		int maxy = ty0; // Max. vert. offset of precincts in the ref. grid
		for ( int c = cs ; c < ce ; c++ ) {
			mrl = this.src.getAnSubbandTree ( t , c ).resLvl;
			for ( int r = rs ; r < re ; r++ ) {
				if ( r > mrl )
					continue;
				nextPrec[ c ] = new int[ mrl + 1 ];
				if ( r < lys[ c ].length && lys[ c ][ r ] < minlys ) {
					minlys = lys[ c ][ r ];
				}
				p = this.numPrec[ t ][ c ][ r ].y * this.numPrec[ t ][ c ][ r ].x - 1;
				for ( ; 0 <= p ; p-- ) {
					prec = this.pktEnc.getPrecInfo ( t , c , r , p );
					if ( prec.rgulx != tx0 ) {
						if ( prec.rgulx < minx )
							minx = prec.rgulx;
						if ( prec.rgulx > maxx )
							maxx = prec.rgulx;
					}
					if ( prec.rguly != ty0 ) {
						if ( prec.rguly < miny )
							miny = prec.rguly;
						if ( prec.rguly > maxy )
							maxy = prec.rguly;
					}

					if ( 0 == nPrec ) {
						gcd_x = prec.rgw;
						gcd_y = prec.rgh;
					}
					else {
						gcd_x = MathUtil.gcd ( gcd_x , prec.rgw );
						gcd_y = MathUtil.gcd ( gcd_y , prec.rgh );
					}
					nPrec++;
				} // precincts
			} // resolution levels
		} // components

		if ( 0 == nPrec ) {
			throw new Error ( "Image cannot have no precinct" );
		}

		final int pyend = ( maxy - miny ) / gcd_y + 1;
		final int pxend = ( maxx - minx ) / gcd_x + 1;
		int y;
		int x;
		for ( int c = cs ; c < ce ; c++ ) { // Loop on components
			y = ty0;
			x = tx0;
			mrl = this.src.getAnSubbandTree ( t , c ).resLvl;
			for ( int py = 0 ; py <= pyend ; py++ ) { // Vertical precincts
				for ( int px = 0 ; px <= pxend ; px++ ) { // Horiz. precincts
					for ( int r = rs ; r < re ; r++ ) { // Resolution levels
						if ( r > mrl )
							continue;
						if ( nextPrec[ c ][ r ] >= this.numPrec[ t ][ c ][ r ].x * this.numPrec[ t ][ c ][ r ].y ) {
							continue;
						}
						prec = this.pktEnc.getPrecInfo ( t , c , r , nextPrec[ c ][ r ] );
						if ( ( prec.rgulx != x ) || ( prec.rguly != y ) ) {
							continue;
						}

						for ( int l = minlys ; l < lye ; l++ ) { // Layers
							if ( r >= lys[ c ].length )
								continue;
							if ( l < lys[ c ][ r ] )
								continue;

							// set boolean sopUsed here (SOP markers)
							sopUsed = this.encSpec.sops.getTileDef ( t ).equals ( "on" );
							// set boolean ephUsed here (EPH markers)
							ephUsed = this.encSpec.ephs.getTileDef ( t ).equals ( "on" );

							sb = this.src.getAnSubbandTree ( t , c );
							for ( int i = mrl ; i > r ; i-- ) {
								sb = sb.subb_LL;
							}

							threshold = this.layers[ l ].rdThreshold;
							this.findTruncIndices ( l , c , r , t , sb , threshold , nextPrec[ c ][ r ] );

							hBuff = this.pktEnc.encodePacket ( l + 1 , c , r , t , this.cblks[ t ][ c ][ r ] , this.truncIdxs[ t ][ l ][ c ][ r ] , hBuff ,
									bBuff , nextPrec[ c ][ r ]
							);

							if ( this.pktEnc.isPacketWritable ( ) ) {
								this.bsWriter.writePacketHead ( hBuff.getBuffer ( ) , hBuff.getLength ( ) , false , sopUsed , ephUsed );
								this.bsWriter.writePacketBody ( this.pktEnc.getLastBodyBuf ( ) , this.pktEnc.getLastBodyLen ( ) , false ,
										this.pktEnc.isROIinPkt ( ) , this.pktEnc.getROILen ( )
								);
							}

						} // Layers
						nextPrec[ c ][ r ]++;
					} // Resolution levels
					if ( px != pxend ) {
						x = minx + px * gcd_x;
					}
					else {
						x = tx0;
					}
				} // Horizontal precincts
				if ( py != pyend ) {
					y = miny + py * gcd_y;
				}
				else {
					y = ty0;
				}
			} // Vertical precincts
		} // components

		// Check that all precincts have been written
		for ( int c = cs ; c < ce ; c++ ) {
			mrl = this.src.getAnSubbandTree ( t , c ).resLvl;
			for ( int r = rs ; r < re ; r++ ) {
				if ( r > mrl )
					continue;
				if ( nextPrec[ c ][ r ] < this.numPrec[ t ][ c ][ r ].x * this.numPrec[ t ][ c ][ r ].y - 1 ) {
					throw new Error ( "JJ2000 bug: One precinct at least has not been written for resolution level "
							+ r + " of component " + c + " in tile " + t + "." );
				}
			}
		}
	}

	/**
	 * Write a piece of bit stream according to the RES_POS_COMP_LY_PROG
	 * progression mode and between given bounds
	 *
	 * @param t   Tile index.
	 * @param rs  First resolution level index.
	 * @param re  Last resolution level index.
	 * @param cs  First component index.
	 * @param ce  Last component index.
	 * @param lys First layer index for each component and resolution.
	 * @param lye Last layer index.
	 */
	public void writeResPosCompLy ( final int t , final int rs , final int re , final int cs , final int ce , final int[][] lys , final int lye ) throws IOException {

		boolean sopUsed; // Should SOP markers be used ?
		boolean ephUsed; // Should EPH markers be used ?
		int mrl;
		SubbandAn sb;
		float threshold;
		BitOutputBuffer hBuff = null;
		final byte[] bBuff = null;

		// Computes current tile offset in the reference grid
		final Coord nTiles = this.src.getNumTiles ( null );
		final Coord tileI = this.src.getTile ( null );
		final int x0siz = this.src.getImgULX ( );
		final int y0siz = this.src.getImgULY ( );
		final int xsiz = x0siz + this.src.getImgWidth ( );
		final int ysiz = y0siz + this.src.getImgHeight ( );
		final int xt0siz = this.src.getTilePartULX ( );
		final int yt0siz = this.src.getTilePartULY ( );
		final int xtsiz = this.src.getNomTileWidth ( );
		final int ytsiz = this.src.getNomTileHeight ( );
		final int tx0 = ( 0 == tileI.x ) ? x0siz : xt0siz + tileI.x * xtsiz;
		final int ty0 = ( 0 == tileI.y ) ? y0siz : yt0siz + tileI.y * ytsiz;
		final int tx1 = ( tileI.x != nTiles.x - 1 ) ? xt0siz + ( tileI.x + 1 ) * xtsiz : xsiz;
		final int ty1 = ( tileI.y != nTiles.y - 1 ) ? yt0siz + ( tileI.y + 1 ) * ytsiz : ysiz;

		// Get precinct information (number,distance between two consecutive
		// precincts in the reference grid) in each component and resolution
		// level
		PrecInfo prec; // temporary variable
		int p; // Current precinct index
		int gcd_x = 0; // Horiz. distance between 2 precincts in the ref. grid
		int gcd_y = 0; // Vert. distance between 2 precincts in the ref. grid
		int nPrec = 0; // Total number of found precincts
		final int[][] nextPrec = new int[ ce ][]; // Next precinct index in each
		// component and resolution level
		int minlys = 100000; // minimum layer start index of each component
		int minx = tx1; // Horiz. offset of the second precinct in the
		// reference grid
		int miny = ty1; // Vert. offset of the second precinct in the
		// reference grid.
		int maxx = tx0; // Max. horiz. offset of precincts in the ref. grid
		int maxy = ty0; // Max. vert. offset of precincts in the ref. grid
		for ( int c = cs ; c < ce ; c++ ) {
			mrl = this.src.getAnSubbandTree ( t , c ).resLvl;
			nextPrec[ c ] = new int[ mrl + 1 ];
			for ( int r = rs ; r < re ; r++ ) {
				if ( r > mrl )
					continue;
				if ( r < lys[ c ].length && lys[ c ][ r ] < minlys ) {
					minlys = lys[ c ][ r ];
				}
				p = this.numPrec[ t ][ c ][ r ].y * this.numPrec[ t ][ c ][ r ].x - 1;
				for ( ; 0 <= p ; p-- ) {
					prec = this.pktEnc.getPrecInfo ( t , c , r , p );
					if ( prec.rgulx != tx0 ) {
						if ( prec.rgulx < minx )
							minx = prec.rgulx;
						if ( prec.rgulx > maxx )
							maxx = prec.rgulx;
					}
					if ( prec.rguly != ty0 ) {
						if ( prec.rguly < miny )
							miny = prec.rguly;
						if ( prec.rguly > maxy )
							maxy = prec.rguly;
					}

					if ( 0 == nPrec ) {
						gcd_x = prec.rgw;
						gcd_y = prec.rgh;
					}
					else {
						gcd_x = MathUtil.gcd ( gcd_x , prec.rgw );
						gcd_y = MathUtil.gcd ( gcd_y , prec.rgh );
					}
					nPrec++;
				} // precincts
			} // resolution levels
		} // components

		if ( 0 == nPrec ) {
			throw new Error ( "Image cannot have no precinct" );
		}

		final int pyend = ( maxy - miny ) / gcd_y + 1;
		final int pxend = ( maxx - minx ) / gcd_x + 1;
		int x, y;
		for ( int r = rs ; r < re ; r++ ) { // Resolution levels
			y = ty0;
			x = tx0;
			for ( int py = 0 ; py <= pyend ; py++ ) { // Vertical precincts
				for ( int px = 0 ; px <= pxend ; px++ ) { // Horiz. precincts
					for ( int c = cs ; c < ce ; c++ ) { // Components
						mrl = this.src.getAnSubbandTree ( t , c ).resLvl;
						if ( r > mrl )
							continue;
						if ( nextPrec[ c ][ r ] >= this.numPrec[ t ][ c ][ r ].x * this.numPrec[ t ][ c ][ r ].y ) {
							continue;
						}
						prec = this.pktEnc.getPrecInfo ( t , c , r , nextPrec[ c ][ r ] );
						if ( ( prec.rgulx != x ) || ( prec.rguly != y ) ) {
							continue;
						}
						for ( int l = minlys ; l < lye ; l++ ) {
							if ( r >= lys[ c ].length )
								continue;
							if ( l < lys[ c ][ r ] )
								continue;

							// set boolean sopUsed here (SOP markers)
							sopUsed = this.encSpec.sops.getTileDef ( t ).equals ( "on" );
							// set boolean ephUsed here (EPH markers)
							ephUsed = this.encSpec.ephs.getTileDef ( t ).equals ( "on" );

							sb = this.src.getAnSubbandTree ( t , c );
							for ( int i = mrl ; i > r ; i-- ) {
								sb = sb.subb_LL;
							}

							threshold = this.layers[ l ].rdThreshold;
							this.findTruncIndices ( l , c , r , t , sb , threshold , nextPrec[ c ][ r ] );

							hBuff = this.pktEnc.encodePacket ( l + 1 , c , r , t , this.cblks[ t ][ c ][ r ] , this.truncIdxs[ t ][ l ][ c ][ r ] , hBuff ,
									bBuff , nextPrec[ c ][ r ]
							);

							if ( this.pktEnc.isPacketWritable ( ) ) {
								this.bsWriter.writePacketHead ( hBuff.getBuffer ( ) , hBuff.getLength ( ) , false , sopUsed , ephUsed );
								this.bsWriter.writePacketBody ( this.pktEnc.getLastBodyBuf ( ) , this.pktEnc.getLastBodyLen ( ) , false ,
										this.pktEnc.isROIinPkt ( ) , this.pktEnc.getROILen ( )
								);
							}

						} // layers
						nextPrec[ c ][ r ]++;
					} // Components
					if ( px != pxend ) {
						x = minx + px * gcd_x;
					}
					else {
						x = tx0;
					}
				} // Horizontal precincts
				if ( py != pyend ) {
					y = miny + py * gcd_y;
				}
				else {
					y = ty0;
				}
			} // Vertical precincts
		} // Resolution levels

		// Check that all precincts have been written
		for ( int c = cs ; c < ce ; c++ ) {
			mrl = this.src.getAnSubbandTree ( t , c ).resLvl;
			for ( int r = rs ; r < re ; r++ ) {
				if ( r > mrl )
					continue;
				if ( nextPrec[ c ][ r ] < this.numPrec[ t ][ c ][ r ].x * this.numPrec[ t ][ c ][ r ].y - 1 ) {
					throw new Error ( "JJ2000 bug: One precinct at least has not been written for resolution level "
							+ r + " of component " + c + " in tile " + t + "." );
				}
			}
		}
	}

	/**
	 * This function implements the rate-distortion optimization algorithm. It
	 * saves the state of any previously generated bit-stream layers and then
	 * simulate the formation of a new layer in the bit stream as often as
	 * necessary to find the smallest rate-distortion threshold such that the
	 * total number of bytes required to represent the layer does not exceed
	 * `maxBytes' minus `prevBytes'. It then restores the state of any
	 * previously generated bit-stream layers and returns the threshold.
	 *
	 * @param layerIdx  The index of the current layer
	 * @param fmaxt     The maximum admissible slope value. Normally the threshold
	 *                  slope of the previous layer.
	 * @param maxBytes  The maximum number of bytes that can be written. It includes
	 *                  the length of the current layer bistream length and all the
	 *                  previous layers bit streams.
	 * @param prevBytes The number of bytes of all the previous layers.
	 * @return The value of the slope threshold.
	 */
	private float optimizeBitstreamLayer ( final int layerIdx , float fmaxt , final int maxBytes , final int prevBytes ) throws IOException {

		final int nt; // The total number of tiles
		final int nc; // The total number of components
		int numLvls; // The total number of resolution levels
		int actualBytes; // Actual number of bytes for a layer
		float fmint; // Minimum of the current threshold interval
		float ft; // Current threshold
		SubbandAn sb; // Current subband
		BitOutputBuffer hBuff;// The packet head buffer
		byte[] bBuff; // The packet body buffer
		int sidx; // The index in the summary table
		boolean sopUsed; // Should SOP markers be used ?
		boolean ephUsed; // Should EPH markers be used ?
		int nPrec; // Number of precincts in the current resolution level

		// Save the packet encoder state
		this.pktEnc.save ( );

		nt = this.src.getNumTiles ( );
		nc = this.src.getNumComps ( );
		hBuff = null;
		bBuff = null;

		// Estimate the minimum slope to start with from the summary
		// information in 'RDSlopesRates'. This is a real minimum since it
		// does not include the packet head overhead, which is always
		// non-zero.

		// Look for the summary entry that gives 'maxBytes' or more data
		for ( sidx = EBCOTRateAllocator.RD_SUMMARY_SIZE - 1; 0 < sidx ; sidx-- ) {
			if ( this.RDSlopesRates[ sidx ] >= maxBytes ) {
				break;
			}
		}
		// Get the corresponding minimum slope
		fmint = EBCOTRateAllocator.getSlopeFromSIndex ( sidx );
		// Ensure that it is smaller the maximum slope
		if ( fmint >= fmaxt ) {
			sidx--;
			fmint = EBCOTRateAllocator.getSlopeFromSIndex ( sidx );
		}
		// If we are using the last entry of the summary, then that
		// corresponds to all the data, Thus, set the minimum slope to 0.
		if ( 0 >= sidx )
			fmint = 0;

		// We look for the best threshold 'ft', which is the lowest threshold
		// that generates no more than 'maxBytes' code bytes.

		// The search is done iteratively using a binary split algorithm. We
		// start with 'fmaxt' as the maximum possible threshold, and 'fmint'
		// as the minimum threshold. The threshold 'ft' is calculated as the
		// middle point of 'fmaxt'-'fmint' interval. The 'fmaxt' or 'fmint'
		// bounds are moved according to the number of bytes obtained from a
		// simulation, where 'ft' is used as the threshold.

		// We stop whenever the interval is sufficiently small, and thus
		// enough precision is achieved.

		// Initialize threshold as the middle point of the interval.
		ft = ( fmaxt + fmint ) / 2.0f;
		// If 'ft' reaches 'fmint' it means that 'fmaxt' and 'fmint' are so
		// close that the average is 'fmint', due to rounding. Force it to
		// 'fmaxt' instead, since 'fmint' is normally an exclusive lower
		// bound.
		if ( ft <= fmint )
			ft = fmaxt;

		do {
			// Get the number of bytes used by this layer, if 'ft' is the
			// threshold, by simulation.
			actualBytes = prevBytes;
			this.src.setTile ( 0 , 0 );

			for ( int t = 0 ; t < nt ; t++ ) {
				for ( int c = 0 ; c < nc ; c++ ) {
					// set boolean sopUsed here (SOP markers)
					sopUsed = "on".equalsIgnoreCase ( ( String ) encSpec.sops.getTileDef ( t ) );
					// set boolean ephUsed here (EPH markers)
					ephUsed = "on".equalsIgnoreCase ( ( String ) encSpec.ephs.getTileDef ( t ) );

					// Get LL subband
					sb = this.src.getAnSubbandTree ( t , c );
					numLvls = sb.resLvl + 1;
					sb = ( SubbandAn ) sb.getSubbandByIdx ( 0 , 0 );
					// loop on resolution levels
					for ( int r = 0 ; r < numLvls ; r++ ) {

						nPrec = this.numPrec[ t ][ c ][ r ].x * this.numPrec[ t ][ c ][ r ].y;
						for ( int p = 0 ; p < nPrec ; p++ ) {

							this.findTruncIndices ( layerIdx , c , r , t , sb , ft , p );
							hBuff = this.pktEnc.encodePacket ( layerIdx + 1 , c , r , t , this.cblks[ t ][ c ][ r ] ,
									this.truncIdxs[ t ][ layerIdx ][ c ][ r ] , hBuff , bBuff , p
							);

							if ( this.pktEnc.isPacketWritable ( ) ) {
								bBuff = this.pktEnc.getLastBodyBuf ( );
								actualBytes += this.bsWriter.writePacketHead ( hBuff.getBuffer ( ) , hBuff.getLength ( ) , true ,
										sopUsed , ephUsed
								);
								actualBytes += this.bsWriter.writePacketBody ( bBuff , this.pktEnc.getLastBodyLen ( ) , true ,
										this.pktEnc.isROIinPkt ( ) , this.pktEnc.getROILen ( )
								);
							}
						} // end loop on precincts
						sb = sb.parent;
					} // End loop on resolution levels
				} // End loop on components
			} // End loop on tiles

			// Move the interval bounds according to simulation result
			if ( actualBytes > maxBytes ) {
				// 'ft' is too low and generates too many bytes, make it the
				// new minimum.
				fmint = ft;
			}
			else {
				// 'ft' is too high and does not generate as many bytes as we
				// are allowed too, make it the new maximum.
				fmaxt = ft;
			}

			// Update 'ft' for the new iteration as the middle point of the
			// new interval.
			ft = ( fmaxt + fmint ) / 2.0f;
			// If 'ft' reaches 'fmint' it means that 'fmaxt' and 'fmint' are
			// so close that the average is 'fmint', due to rounding. Force it
			// to 'fmaxt' instead, since 'fmint' is normally an exclusive
			// lower bound.
			if ( ft <= fmint )
				ft = fmaxt;

			// Restore previous packet encoder state
			this.pktEnc.restore ( );

			// We continue to iterate, until the threshold reaches the upper
			// limit of the interval, within a FLOAT_REL_PRECISION relative
			// tolerance, or a FLOAT_ABS_PRECISION absolute tolerance. This is
			// the sign that the interval is sufficiently small.
		} while ( ft < fmaxt * ( 1.0f - EBCOTRateAllocator.FLOAT_REL_PRECISION ) && ft < ( fmaxt - EBCOTRateAllocator.FLOAT_ABS_PRECISION ) );

		// If we have a threshold which is close to 0, set it to 0 so that
		// everything is taken into the layer. This is to avoid not sending
		// some least significant bit-planes in the lossless case. We use the
		// FLOAT_ABS_PRECISION value as a measure of "close" to 0.
		if ( FLOAT_ABS_PRECISION >= ft ) {
			ft = 0.0f;
		}
		else {
			// Otherwise make the threshold 'fmaxt', just to be sure that we
			// will not send more bytes than allowed.
			ft = fmaxt;
		}
		return ft;
	}

	/**
	 * This function attempts to estimate a rate-distortion slope threshold
	 * which will achieve a target number of code bytes close the `targetBytes'
	 * value.
	 *
	 * @param targetBytes The target number of bytes for the current layer
	 * @param lastLayer   The previous layer information.
	 * @return The value of the slope threshold for the estimated layer
	 */
	private float estimateLayerThreshold ( final int targetBytes , final EBCOTLayer lastLayer ) {
		float log_sl1; // The log of the first slope used for interpolation
		float log_sl2; // The log of the second slope used for interpolation
		float log_len1; // The log of the first length used for interpolation
		float log_len2; // The log of the second length used for interpolation
		float log_isl; // The log of the interpolated slope
		float log_ilen; // Log of the interpolated length
		final float log_ab; // Log of actual bytes in last layer
		int sidx; // Index into the summary R-D info array
		float log_off; // The log of the offset proportion
		final int tlen; // The corrected target layer length
		float lthresh; // The threshold of the last layer
		float eth; // The estimated threshold

		// In order to estimate the threshold we base ourselves in the summary
		// R-D info in RDSlopesRates. In order to use it we must compensate
		// for the overhead of the packet heads. The proportion of overhead is
		// estimated using the last layer simulation results.

		// NOTE: the model used in this method is that the slope varies
		// linearly with the log of the rate (i.e. length).

		// NOTE: the model used in this method is that the distortion is
		// proprotional to a power of the rate. Thus, the slope is also
		// proportional to another power of the rate. This translates as the
		// log of the slope varies linearly with the log of the rate, which is
		// what we use.

		// 1) Find the offset of the length predicted from the summary R-D
		// information, to the actual length by using the last layer.

		// We ensure that the threshold we use for estimation actually
		// includes some data.
		lthresh = lastLayer.rdThreshold;
		if ( lthresh > this.maxSlope )
			lthresh = this.maxSlope;
		// If the slope of the last layer is too small then we just include
		// all the rest (not possible to do better).
		if ( FLOAT_ABS_PRECISION > lthresh )
			return 0.0f;
		sidx = EBCOTRateAllocator.getLimitedSIndexFromSlope ( lthresh );
		// If the index is outside of the summary info array use the last two,
		// or first two, indexes, as appropriate
		if ( RD_SUMMARY_SIZE - 1 <= sidx )
			sidx = EBCOTRateAllocator.RD_SUMMARY_SIZE - 2;

		// Get the logs of the lengths and the slopes

		if ( 0 == RDSlopesRates[ sidx + 1 ] ) {
			// Pathological case, we can not use log of 0. Add
			// RDSlopesRates[sidx]+1 bytes to the rates (just a crude simple
			// solution to this rare case)
			log_len1 = ( float ) Math.log ( ( this.RDSlopesRates[ sidx ] << 1 ) + 1 );
			log_len2 = ( float ) Math.log ( this.RDSlopesRates[ sidx ] + 1 );
			log_ab = ( float ) Math.log ( lastLayer.actualBytes + this.RDSlopesRates[ sidx ] + 1 );
		}
		else {
			log_len1 = ( float ) Math.log ( this.RDSlopesRates[ sidx ] );
			log_len2 = ( float ) Math.log ( this.RDSlopesRates[ sidx + 1 ] );
			log_ab = ( float ) Math.log ( lastLayer.actualBytes );
		}

		log_sl1 = ( float ) Math.log ( EBCOTRateAllocator.getSlopeFromSIndex ( sidx ) );
		log_sl2 = ( float ) Math.log ( EBCOTRateAllocator.getSlopeFromSIndex ( sidx + 1 ) );

		log_isl = ( float ) Math.log ( lthresh );

		log_ilen = log_len1 + ( log_isl - log_sl1 ) * ( log_len1 - log_len2 ) / ( log_sl1 - log_sl2 );

		log_off = log_ab - log_ilen;

		// Do not use negative offsets (i.e. offset proportion larger than 1)
		// since that is probably a sign that our model is off. To be
		// conservative use an offset of 0 (i.e. offset proportiojn 1).
		if ( 0 > log_off )
			log_off = 0.0f;

		// 2) Correct the target layer length by the offset.

		tlen = ( int ) ( targetBytes / ( float ) Math.exp ( log_off ) );

		// 3) Find, from the summary R-D info, the thresholds that generate
		// lengths just above and below our corrected target layer length.

		// Look for the index in the summary info array that gives the largest
		// length smaller than the target length
		for ( sidx = EBCOTRateAllocator.RD_SUMMARY_SIZE - 1; 0 <= sidx ; sidx-- ) {
			if ( this.RDSlopesRates[ sidx ] >= tlen )
				break;
		}
		sidx++;
		// Correct if out of the array
		if ( RD_SUMMARY_SIZE <= sidx )
			sidx = EBCOTRateAllocator.RD_SUMMARY_SIZE - 1;
		if ( 0 >= sidx )
			sidx = 1;

		// Get the log of the lengths and the slopes that are just above and
		// below the target length.

		if ( 0 == RDSlopesRates[ sidx ] ) {
			// Pathological case, we can not use log of 0. Add
			// RDSlopesRates[sidx-1]+1 bytes to the rates (just a crude simple
			// solution to this rare case)
			log_len1 = ( float ) Math.log ( this.RDSlopesRates[ sidx - 1 ] + 1 );
			log_len2 = ( float ) Math.log ( ( this.RDSlopesRates[ sidx - 1 ] << 1 ) + 1 );
			log_ilen = ( float ) Math.log ( tlen + this.RDSlopesRates[ sidx - 1 ] + 1 );
		}
		else {
			// Normal case, we can safely take the logs.
			log_len1 = ( float ) Math.log ( this.RDSlopesRates[ sidx ] );
			log_len2 = ( float ) Math.log ( this.RDSlopesRates[ sidx - 1 ] );
			log_ilen = ( float ) Math.log ( tlen );
		}

		log_sl1 = ( float ) Math.log ( EBCOTRateAllocator.getSlopeFromSIndex ( sidx ) );
		log_sl2 = ( float ) Math.log ( EBCOTRateAllocator.getSlopeFromSIndex ( sidx - 1 ) );

		// 4) Interpolate the two thresholds to find the target threshold.

		log_isl = log_sl1 + ( log_ilen - log_len1 ) * ( log_sl1 - log_sl2 ) / ( log_len1 - log_len2 );

		eth = ( float ) Math.exp ( log_isl );

		// Correct out of bounds results
		if ( eth > lthresh )
			eth = lthresh;
		if ( FLOAT_ABS_PRECISION > eth )
			eth = 0.0f;

		// Return the estimated threshold
		return eth;
	}

	/**
	 * This function finds the new truncation points indices for a packet. It
	 * does so by including the data from the code-blocks in the component,
	 * resolution level and tile, associated with a R-D slope which is larger
	 * than or equal to 'fthresh'.
	 *
	 * @param layerIdx The index of the current layer
	 * @param compIdx  The index of the current component
	 * @param lvlIdx   The index of the current resolution level
	 * @param tileIdx  The index of the current tile
	 * @param subb     The LL subband in the resolution level lvlIdx, which is parent
	 *                 of all the subbands in the packet. Except for resolution level
	 *                 0 this subband is always a node.
	 * @param fthresh  The value of the rate-distortion threshold
	 */
	private void findTruncIndices (
			final int layerIdx , final int compIdx , final int lvlIdx , final int tileIdx , final SubbandAn subb , final float fthresh ,
			final int precinctIdx
	) {
		final int minsbi;
		final int maxsbi;
		int b;
		int n;
		SubbandAn sb;
		CBlkRateDistStats cur_cblk;
		final PrecInfo prec = this.pktEnc.getPrecInfo ( tileIdx , compIdx , lvlIdx , precinctIdx );
		Coord cbCoord;

		sb = subb;
		while ( null != sb.subb_HH ) {
			sb = sb.subb_HH;
		}
		minsbi = ( 0 == lvlIdx ) ? 0 : 1;
		maxsbi = ( 0 == lvlIdx ) ? 1 : 4;

		int yend, xend;

		sb = ( SubbandAn ) subb.getSubbandByIdx ( lvlIdx , minsbi );
		for ( int s = minsbi ; s < maxsbi ; s++ ) { // loop on subbands
			yend = ( null != prec.cblk[ s ] ) ? prec.cblk[ s ].length : 0;
			for ( int y = 0 ; y < yend ; y++ ) {
				xend = ( null != prec.cblk[ s ][ y ] ) ? prec.cblk[ s ][ y ].length : 0;
				for ( int x = 0 ; x < xend ; x++ ) {
					cbCoord = prec.cblk[ s ][ y ][ x ].idx;
					b = cbCoord.x + cbCoord.y * sb.numCb.x;

					// Get the current code-block
					cur_cblk = this.cblks[ tileIdx ][ compIdx ][ lvlIdx ][ s ][ b ];
					for ( n = 0; n < cur_cblk.nVldTrunc ; n++ ) {
						if ( cur_cblk.truncSlopes[ n ] < fthresh ) {
							break;
						}
						continue;
					}
					// Store the index in the code-block truncIdxs that gives
					// the real truncation index.
					this.truncIdxs[ tileIdx ][ layerIdx ][ compIdx ][ lvlIdx ][ s ][ b ] = n - 1;

				} // End loop on horizontal code-blocks
			} // End loop on vertical code-blocks
			sb = ( SubbandAn ) sb.nextSubband ( );
		} // End loop on subbands
	}
}
