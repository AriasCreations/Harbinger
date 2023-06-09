/*
 * CVS identifier:
 *
 * $Id: HeaderDecoder.java,v 1.61 2002/07/25 15:01:00 grosbois Exp $
 *
 * Class:                   HeaderDecoder
 *
 * Description:             Reads main and tile-part headers.
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
package dev.zontreck.harbinger.thirdparty.jj2000.j2k.codestream.reader;

import dev.zontreck.harbinger.thirdparty.jj2000.j2k.ModuleSpec;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.NotImplementedError;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.codestream.CorruptedCodestreamException;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.codestream.HeaderInfo;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.codestream.Markers;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.codestream.ProgressionType;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.colorspace.*;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.decoder.Decoder;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.decoder.DecoderSpecs;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.entropy.StdEntropyCoderOptions;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.entropy.decoder.CodedCBlkDataSrcDec;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.entropy.decoder.EntropyDecoder;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.entropy.decoder.StdEntropyDecoder;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.icc.ICCProfileException;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.image.BlkImgDataSrc;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.image.Coord;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.io.RandomAccessIO;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.quantization.dequantizer.CBlkQuantDataSrcDec;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.quantization.dequantizer.Dequantizer;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.quantization.dequantizer.StdDequantizer;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.quantization.dequantizer.StdDequantizerParams;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.roi.MaxShiftSpec;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.roi.ROIDeScaler;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.util.FacilityManager;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.util.MsgLogger;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.util.ParameterList;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.wavelet.FilterTypes;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.wavelet.synthesis.SynWTFilter;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.wavelet.synthesis.SynWTFilterFloatLift9x7;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.wavelet.synthesis.SynWTFilterIntLift5x3;

import java.io.*;
import java.util.Hashtable;
import java.util.Vector;

/**
 * This class reads main and tile-part headers from the codestream given a
 * RandomAccessIO instance located at the beginning of the codestream (i.e just
 * before the SOC marker) or at the beginning of a tile-part (i.e. just before a
 * SOT marker segment) respectively.
 *
 * <p>
 * A marker segment includes a marker and eventually marker segment parameters.
 * It is designed by the three letters code of the marker associated with the
 * marker segment. JPEG 2000 part 1 defines 6 types of markers segments:
 *
 * <ul>
 * <li>Delimiting : SOC, SOT, SOD, EOC</li>
 *
 * <li>Fixed information: SIZ.</li>
 *
 * <li>Functional: COD, COC, RGN, QCD, QCC,POC.</li>
 *
 * <li>In bit-stream: SOP, EPH.</li>
 *
 * <li>Pointer: TLM, PLM, PLT, PPM, PPT.</li>
 *
 * <li>Informational: CRG, COM.</li>
 * </ul>
 *
 * <p>
 * The main header is read when the constructor is called whereas tile-part
 * headers are read when the FileBitstreamReaderAgent instance is created. The
 * reading is done in 2 passes:
 *
 * <ol>
 * <li>All marker segments are buffered and their corresponding flag is
 * activated (extractMainMarkSeg and extractTilePartMarkSeg methods).</li>
 *
 * <li>Buffered marker segment are analyzed in a logical way and specifications
 * are stored in appropriate member of DecoderSpecs instance
 * (readFoundMainMarkSeg and readFoundTilePartMarkSeg methods).</li>
 * </ol>
 *
 * <p>
 * Whenever a marker segment is not recognized a warning message is displayed
 * and its length parameter is used to skip it.
 *
 * <p>
 * The information found in this header is stored in HeaderInfo and DecoderSpecs
 * instances.
 *
 * @see DecoderSpecs
 * @see HeaderInfo
 * @see Decoder
 * @see FileBitstreamReaderAgent
 */
public class HeaderDecoder implements ProgressionType, Markers, StdEntropyCoderOptions {

	/**
	 * The prefix for header decoder options: 'H'
	 */
	public static final char OPT_PREFIX = 'H';
	/**
	 * Flag bit for SOD marker segment found
	 */
	public static final int SOD_FOUND = 1 << 13;
	/**
	 * Flag bit for SOD marker segment found
	 */
	public static final int PPM_FOUND = 1 << 14;
	/**
	 * Flag bit for SOD marker segment found
	 */
	public static final int PPT_FOUND = 1 << 15;
	/**
	 * Flag bit for CRG marker segment found
	 */
	public static final int CRG_FOUND = 1 << 16;
	/**
	 * The list of parameters that is accepted for quantization. Options for
	 * quantization start with 'Q'.
	 */
	private static final String[][] pinfo = null;
	/**
	 * Flag bit for SIZ marker segment found
	 */
	private static final int SIZ_FOUND = 1;
	/**
	 * Flag bit for COD marker segment found
	 */
	private static final int COD_FOUND = 1 << 1;
	/**
	 * Flag bit for COC marker segment found
	 */
	private static final int COC_FOUND = 1 << 2;
	/**
	 * Flag bit for QCD marker segment found
	 */
	private static final int QCD_FOUND = 1 << 3;
	/**
	 * Flag bit for TLM marker segment found
	 */
	private static final int TLM_FOUND = 1 << 4;
	/**
	 * Flag bit for PLM marker segment found
	 */
	private static final int PLM_FOUND = 1 << 5;
	/**
	 * Flag bit for SOT marker segment found
	 */
	private static final int SOT_FOUND = 1 << 6;
	/**
	 * Flag bit for QCC marker segment found
	 */
	private static final int QCC_FOUND = 1 << 8;
	/**
	 * Flag bit for RGN marker segment found
	 */
	private static final int RGN_FOUND = 1 << 9;
	/**
	 * Flag bit for POC marker segment found
	 */
	private static final int POC_FOUND = 1 << 10;
	/**
	 * Flag bit for COM marker segment found
	 */
	private static final int COM_FOUND = 1 << 11;
	/**
	 * The reference to the HeaderInfo instance holding the information found in
	 * headers
	 */
	private final HeaderInfo hi;
	/**
	 * Current header information in a string
	 */
	private final String hdStr = "";
	/**
	 * The number of tile parts in each tile
	 */
	public int[] nTileParts;

	/** Flag bit for PLT marker segment found */
//	private static final int PLT_FOUND = 1 << 7;
	/**
	 * The offset of the main header in the input stream
	 */
	public int mainHeadOff;
	/**
	 * Vector containing info as to which tile each tilepart belong
	 */
	public Vector<Integer> tileOfTileParts;
	/**
	 * Is the precinct partition used
	 */
	boolean precinctPartitionIsUsed;
	/**
	 * The number of tiles within the image
	 */
	private int nTiles;
	/**
	 * Used to store which markers have been already read, by using flag bits.
	 * The different markers are marked with XXX_FOUND flags, such as SIZ_FOUND
	 */
	private int nfMarkSeg;
	/**
	 * Counts number of COC markers found in the header
	 */
	private int nCOCMarkSeg;
	/**
	 * Counts number of QCC markers found in the header
	 */
	private int nQCCMarkSeg;
	/**
	 * Counts number of COM markers found in the header
	 */
	private int nCOMMarkSeg;

	/** The reset mask for new tiles */
//	private static final int TILE_RESET = ~(PLM_FOUND | SIZ_FOUND | RGN_FOUND);
	/**
	 * Counts number of RGN markers found in the header
	 */
	private int nRGNMarkSeg;
	/**
	 * Counts number of PPM markers found in the header
	 */
	private int nPPMMarkSeg;
	/**
	 * Counts number of PPT markers found in the header
	 */
	private int[][] nPPTMarkSeg;
	/**
	 * HashTable used to store temporary marker segment byte buffers
	 */
	private Hashtable<String, byte[]> ht;
	/**
	 * The number of components in the image
	 */
	private int nComp;
	/**
	 * The horizontal code-block partition origin
	 */
	private int cb0x = - 1;
	/**
	 * The vertical code-block partition origin
	 */
	private int cb0y = - 1;
	/**
	 * The decoder specifications
	 */
	private DecoderSpecs decSpec;
	/**
	 * Array containing the Nppm and Ippm fields of the PPM marker segments
	 */
	private byte[][] pPMMarkerData;

	/**
	 * Array containing the Ippm fields of the PPT marker segments
	 */
	private byte[][][][] tilePartPkdPktHeaders;

	/**
	 * The packed packet headers if the PPM or PPT markers are used
	 */
	private ByteArrayOutputStream[] pkdPktHeaders;

	/**
	 * Creates a HeaderDecoder instance and read in two passes the main header
	 * of the codestream. The first and last marker segments shall be
	 * respectively SOC and SOT.
	 *
	 * @param ehs The encoded header stream where marker segments are extracted.
	 * @param pl  The ParameterList object of the decoder
	 * @param hi  The HeaderInfo holding information found in marker segments
	 * @throws IOException                  If an I/O error occurs while reading from the encoded
	 *                                      header stream.
	 * @throws EOFException                 If the end of the encoded header stream is reached before
	 *                                      getting all the data.
	 * @throws CorruptedCodestreamException If invalid data is found in the codestream main header.
	 */
	public HeaderDecoder ( final RandomAccessIO ehs , final ParameterList pl , final HeaderInfo hi ) throws IOException {

		this.hi = hi;

		pl.checkList ( HeaderDecoder.OPT_PREFIX , ParameterList.toNameArray ( HeaderDecoder.pinfo ) );

		this.mainHeadOff = ehs.getPos ( );
		if ( Markers.SOC != ( ehs.readShort ( ) ) ) {
			throw new CorruptedCodestreamException ( "SOC marker segment not  found at the beginning of the "
					+ "codestream." );
		}

		// First Pass: Decode and store main header information until the SOT
		// marker segment is found
		this.nfMarkSeg = 0;
		do {
			this.extractMainMarkSeg ( ehs.readShort ( ) , ehs );
		} while ( 0 == ( nfMarkSeg & SOT_FOUND ) ); // Stop when SOT is found
		ehs.seek ( ehs.getPos ( ) - 2 ); // Realign codestream on SOT marker

		// Second pass: Read each marker segment previously found
		this.readFoundMainMarkSeg ( );
	}

	/**
	 * Returns the parameters that are used in this class. It returns a 2D
	 * String array. Each of the 1D arrays is for a different option, and they
	 * have 3 elements. The first element is the option name, the second one is
	 * the synopsis and the third one is a long description of what the
	 * parameter is. The synopsis or description may be 'null', in which case it
	 * is assumed that there is no synopsis or description of the option,
	 * respectively.
	 *
	 * @return the options name, their synopsis and their explanation.
	 */
	public static String[][] getParameterInfo ( ) {
		return HeaderDecoder.pinfo;
	}

	/**
	 * Return the maximum height among all components
	 *
	 * @return Maximum component height
	 */
	public int getMaxCompImgHeight ( ) {
		return this.hi.siz.getMaxCompHeight ( );
	}

	/**
	 * Return the maximum width among all components
	 *
	 * @return Maximum component width
	 */
	public int getMaxCompImgWidth ( ) {
		return this.hi.siz.getMaxCompWidth ( );
	}

	/**
	 * Returns the image width in the reference grid.
	 *
	 * @return The image width in the reference grid
	 */
	public final int getImgWidth ( ) {
		return this.hi.siz.xsiz - this.hi.siz.x0siz;
	}

	/**
	 * Returns the image height in the reference grid.
	 *
	 * @return The image height in the reference grid
	 */
	public final int getImgHeight ( ) {
		return this.hi.siz.ysiz - this.hi.siz.y0siz;
	}

	/**
	 * Return the horizontal upper-left coordinate of the image in the reference
	 * grid.
	 *
	 * @return The horizontal coordinate of the image origin.
	 */
	public final int getImgULX ( ) {
		return this.hi.siz.x0siz;
	}

	/**
	 * Return the vertical upper-left coordinate of the image in the reference
	 * grid.
	 *
	 * @return The vertical coordinate of the image origin.
	 */
	public final int getImgULY ( ) {
		return this.hi.siz.y0siz;
	}

	/**
	 * Returns the nominal width of the tiles in the reference grid.
	 *
	 * @return The nominal tile width, in the reference grid.
	 */
	public final int getNomTileWidth ( ) {
		return this.hi.siz.xtsiz;
	}

	/**
	 * Returns the nominal width of the tiles in the reference grid.
	 *
	 * @return The nominal tile width, in the reference grid.
	 */
	public final int getNomTileHeight ( ) {
		return this.hi.siz.ytsiz;
	}

	/**
	 * Returns the tiling origin, referred to as '(Px,Py)' in the 'ImgData'
	 * interface.
	 *
	 * @param co If not null this object is used to return the information. If
	 *           null a new one is created and returned.
	 * @return The coordinate of the tiling origin, in the canvas system, on the
	 * reference grid.
	 * @see dev.zontreck.harbinger.thirdparty.jj2000.j2k.image.ImgData
	 */
	public final Coord getTilingOrigin ( final Coord co ) {
		if ( null != co ) {
			co.x = this.hi.siz.xt0siz;
			co.y = this.hi.siz.yt0siz;
			return co;
		}
		return new Coord ( this.hi.siz.xt0siz , this.hi.siz.yt0siz );
	}

	/**
	 * Returns true if the original data of the specified component was signed.
	 * If the data was not signed a level shift has to be applied at the end of
	 * the decompression chain.
	 *
	 * @param c The index of the component
	 * @return True if the original image component was signed.
	 */
	public final boolean isOriginalSigned ( final int c ) {
		return this.hi.siz.isOrigSigned ( c );
	}

	/**
	 * Returns the original bitdepth of the specified component.
	 *
	 * @param c The index of the component
	 * @return The bitdepth of the component
	 */
	public final int getOriginalBitDepth ( final int c ) {
		return this.hi.siz.getOrigBitDepth ( c );
	}

	/**
	 * Returns the number of components in the image.
	 *
	 * @return The number of components in the image.
	 */
	public final int getNumComps ( ) {
		return this.nComp;
	}

	/**
	 * Returns the component sub-sampling factor, with respect to the reference
	 * grid, along the horizontal direction for the specified component.
	 *
	 * @param c The index of the component
	 * @return The component sub-sampling factor X-wise.
	 */
	public final int getCompSubsX ( final int c ) {
		return this.hi.siz.xrsiz[ c ];
	}

	/**
	 * Returns the component sub-sampling factor, with respect to the reference
	 * grid, along the vertical direction for the specified component.
	 *
	 * @param c The index of the component
	 * @return The component sub-sampling factor Y-wise.
	 */
	public final int getCompSubsY ( final int c ) {
		return this.hi.siz.yrsiz[ c ];
	}

	/**
	 * Returns the dequantizer parameters. Dequantizer parameters normally are
	 * the quantization step sizes, see DequantizerParams.
	 *
	 * @param src      The source of data for the dequantizer.
	 * @param rb       The number of range bits for each component. Must be the
	 *                 number of range bits of the mixed components.
	 * @param decSpec2 The DecoderSpecs instance after any image manipulation.
	 * @return The dequantizer
	 */
	public final Dequantizer createDequantizer ( final CBlkQuantDataSrcDec src , final int[] rb , final DecoderSpecs decSpec2 ) {
		return new StdDequantizer ( src , rb , decSpec2 );
	}

	/**
	 * Returns the horizontal code-block partition origin.Allowable values are 0
	 * and 1, nothing else.
	 */
	public final int getCbULX ( ) {
		return this.cb0x;
	}

	/**
	 * Returns the vertical code-block partition origin. Allowable values are 0
	 * and 1, nothing else.
	 */
	public final int getCbULY ( ) {
		return this.cb0y;
	}

	/**
	 * Returns the precinct partition width for the specified tile-component and
	 * resolution level.
	 *
	 * @param c  the component index
	 * @param t  the tile index
	 * @param rl the resolution level
	 * @return The precinct partition width for the specified tile-component and
	 * resolution level
	 */
	public final int getPPX ( final int t , final int c , final int rl ) {
		return this.decSpec.pss.getPPX ( t , c , rl );
	}

	/**
	 * Returns the precinct partition height for the specified tile-component
	 * and resolution level.
	 *
	 * @param c  the component index
	 * @param t  the tile index
	 * @param rl the resolution level
	 * @return The precinct partition height for the specified tile-component
	 * and resolution level
	 */
	public final int getPPY ( final int t , final int c , final int rl ) {
		return this.decSpec.pss.getPPY ( t , c , rl );
	}

	/**
	 * Returns the boolean used to know if the precinct partition is used
	 */
	public final boolean precinctPartitionUsed ( ) {
		return this.precinctPartitionIsUsed;
	}

	/**
	 * Reads a wavelet filter from the codestream and returns the filter object
	 * that implements it.
	 *
	 * @param ehs     The encoded header stream from where to read the info
	 * @param filtIdx Int array of one element to return the type of the wavelet
	 *                filter.
	 */
	private SynWTFilter readFilter ( final DataInputStream ehs , final int[] filtIdx ) throws IOException {
		final int kid; // the filter id

		kid = filtIdx[ 0 ] = ehs.readUnsignedByte ( );
		if ( ( 1 << 7 ) <= kid ) {
			throw new NotImplementedError ( "Custom filters not supported" );
		}
		// Return filter based on ID
		switch ( kid ) {
			case FilterTypes.W9X7:
				return new SynWTFilterFloatLift9x7 ( );
			case FilterTypes.W5X3:
				return new SynWTFilterIntLift5x3 ( );
			default:
				throw new CorruptedCodestreamException ( "Specified wavelet filter not JPEG 2000 part I compliant" );
		}
	}

	/**
	 * Checks that the marker segment length is correct.
	 *
	 * @param ehs The encoded header stream
	 * @param str The string identifying the marker, such as "SIZ marker"
	 * @throws IOException If an I/O error occurs
	 */
	public void checkMarkerLength ( final DataInputStream ehs , final String str ) throws IOException {
		if ( 0 != ehs.available ( ) ) {
			FacilityManager.getMsgLogger ( )
					.printmsg ( MsgLogger.WARNING , str + " length was short, attempting to resync." );
		}
	}

	/**
	 * Reads the SIZ marker segment and realigns the codestream at the point
	 * where the next marker segment should be found.
	 *
	 * <p>
	 * SIZ is a fixed information marker segment containing informations about
	 * image and tile sizes. It is required in the main header immediately after
	 * SOC.
	 *
	 * @param ehs The encoded header stream
	 * @throws IOException If an I/O error occurs while reading from the encoded
	 *                     header stream
	 */
	private void readSIZ ( final DataInputStream ehs ) throws IOException {
		final HeaderInfo.SIZ ms = this.hi.getNewSIZ ( );
		this.hi.siz = ms;

		// Read the length of SIZ marker segment (Lsiz)
		ms.lsiz = ehs.readUnsignedShort ( );

		// Read the capability of the codestream (Rsiz)
		ms.rsiz = ehs.readUnsignedShort ( );
		if ( 2 < ms.rsiz ) {
			throw new Error ( "Codestream capabiities not JPEG 2000 - Part I compliant" );
		}

		// Read image size
		ms.xsiz = ehs.readInt ( );
		ms.ysiz = ehs.readInt ( );
		if ( 0 >= ms.xsiz || 0 >= ms.ysiz ) {
			throw new IOException ( "JJ2000 does not support images whose width and/or height not in the "
					+ "range: 1 -- (2^31)-1" );
		}

		// Read image offset
		ms.x0siz = ehs.readInt ( );
		ms.y0siz = ehs.readInt ( );
		if ( 0 > ms.x0siz || 0 > ms.y0siz ) {
			throw new IOException ( "JJ2000 does not support images offset not in the range: 0 -- (2^31)-1" );
		}

		// Read size of tile
		ms.xtsiz = ehs.readInt ( );
		ms.ytsiz = ehs.readInt ( );
		if ( 0 >= ms.xtsiz || 0 >= ms.ytsiz ) {
			throw new IOException ( "JJ2000 does not support tiles whose width and/or height are not in  "
					+ "the range: 1 -- (2^31)-1" );
		}

		// Read upper-left tile offset
		ms.xt0siz = ehs.readInt ( );
		ms.yt0siz = ehs.readInt ( );
		if ( 0 > ms.xt0siz || 0 > ms.yt0siz ) {
			throw new IOException ( "JJ2000 does not support tiles whose offset is not in  " + "the range: 0 -- (2^31)-1" );
		}

		// Read number of components and initialize related arrays
		this.nComp = ms.csiz = ehs.readUnsignedShort ( );
		if ( 1 > nComp || 16384 < nComp ) {
			throw new IllegalArgumentException ( "Number of component out of range 1--16384: " + this.nComp );
		}

		ms.ssiz = new int[ this.nComp ];
		ms.xrsiz = new int[ this.nComp ];
		ms.yrsiz = new int[ this.nComp ];

		// Read bit-depth and down-sampling factors of each component
		for ( int i = 0 ; i < this.nComp ; i++ ) {
			ms.ssiz[ i ] = ehs.readUnsignedByte ( );
			ms.xrsiz[ i ] = ehs.readUnsignedByte ( );
			ms.yrsiz[ i ] = ehs.readUnsignedByte ( );
		}

		// Check marker length
		this.checkMarkerLength ( ehs , "SIZ marker" );

		// Create needed ModuleSpec
		this.nTiles = ms.getNumTiles ( );

		// Finish initialization of decSpec
		this.decSpec = new DecoderSpecs ( this.nTiles , this.nComp );
	}

	/**
	 * Reads a CRG marker segment and checks its length. CRG is an informational
	 * marker segment that allows specific registration of components with
	 * respect to each other.
	 *
	 * @param ehs The encoded header stream
	 */
	private void readCRG ( final DataInputStream ehs ) throws IOException {
		final HeaderInfo.CRG ms = this.hi.getNewCRG ( );
		this.hi.crg = ms;

		ms.lcrg = ehs.readUnsignedShort ( );
		ms.xcrg = new int[ this.nComp ];
		ms.ycrg = new int[ this.nComp ];

		FacilityManager.getMsgLogger ( ).printmsg (
				MsgLogger.WARNING ,
				"Information in CRG marker segment not taken into account. This may affect the display "
						+ "of the decoded image."
		);
		for ( int c = 0 ; c < this.nComp ; c++ ) {
			ms.xcrg[ c ] = ehs.readUnsignedShort ( );
			ms.ycrg[ c ] = ehs.readUnsignedShort ( );
		}

		// Check marker length
		this.checkMarkerLength ( ehs , "CRG marker" );
	}

	/**
	 * Reads a COM marker segments and realigns the bit stream at the point
	 * where the next marker segment should be found. COM is an informational
	 * marker segment that allows to include unstructured data in the main and
	 * tile-part headers.
	 *
	 * @param ehs     The encoded header stream
	 * @param mainh   Flag indicating whether or not this marker segment is read
	 *                from the main header.
	 * @param tileIdx The index of the current tile
	 * @param comIdx  Occurence of this COM marker in eith main or tile-part header
	 * @throws IOException If an I/O error occurs while reading from the encoded
	 *                     header stream
	 */
	private void readCOM ( final DataInputStream ehs , final boolean mainh , final int tileIdx , final int comIdx ) throws IOException {
		final HeaderInfo.COM ms = this.hi.getNewCOM ( );

		// Read length of COM field
		ms.lcom = ehs.readUnsignedShort ( );

		// Read the registration value of the COM marker segment
		ms.rcom = ehs.readUnsignedShort ( );
		switch ( ms.rcom ) {
			case Markers.RCOM_BINARY:
			case Markers.RCOM_LATIN:
				ms.ccom = new byte[ ms.lcom - 4 ];
				ehs.read ( ms.ccom , 0 , ms.lcom - 4 );
				break;
			default:
				// --- Unknown or unsupported markers ---
				// (skip them and see if we can get way with it)
				FacilityManager.getMsgLogger ( ).printmsg (
						MsgLogger.WARNING ,
						"COM marker registered as 0x" + Integer.toHexString ( ms.rcom )
								+ " unknown, ignoring (this might crash the "
								+ "decoder or decode a quality degraded or even useless image)"
				);
				ehs.skipBytes ( ms.lcom - 4 ); // Ignore this field for the moment
				break;
		}

		if ( mainh ) {
			this.hi.com.put ( "main_" + comIdx , ms );
		}
		else {
			this.hi.com.put ( "t" + tileIdx + "_" + comIdx , ms );
		}

		// Check marker length
		this.checkMarkerLength ( ehs , "COM marker" );
	}

	/**
	 * Reads a QCD marker segment and realigns the codestream at the point where
	 * the next marker should be found. QCD is a functional marker segment that
	 * describes the quantization default.
	 *
	 * @param ehs     The encoded stream.
	 * @param mainh   Flag indicating whether or not this marker segment is read
	 *                from the main header.
	 * @param tileIdx The index of the current tile
	 * @param tpIdx   Tile-part index
	 * @throws IOException If an I/O error occurs while reading from the encoded
	 *                     header stream.
	 */
	private void readQCD ( final DataInputStream ehs , final boolean mainh , final int tileIdx , final int tpIdx ) throws IOException {
		final StdDequantizerParams qParms;
		final int guardBits;
		final int[][] exp;
		float[][] nStep = null;
		final HeaderInfo.QCD ms = this.hi.getNewQCD ( );

		// Lqcd (length of QCD field)
		ms.lqcd = ehs.readUnsignedShort ( );

		// Sqcd (quantization style)
		ms.sqcd = ehs.readUnsignedByte ( );

		guardBits = ms.getNumGuardBits ( );
		final int qType = ms.getQuantType ( );

		if ( mainh ) {
			this.hi.qcd.put ( "main" , ms );
			// If the main header is being read set default value of
			// dequantization spec
			switch ( qType ) {
				case Markers.SQCX_NO_QUANTIZATION:
					this.decSpec.qts.setDefault ( "reversible" );
					break;
				case Markers.SQCX_SCALAR_DERIVED:
					this.decSpec.qts.setDefault ( "derived" );
					break;
				case Markers.SQCX_SCALAR_EXPOUNDED:
					this.decSpec.qts.setDefault ( "expounded" );
					break;
				default:
					throw new CorruptedCodestreamException ( "Unknown or unsupported quantization style "
							+ "in Sqcd field, QCD marker main header" );
			}
		}
		else {
			this.hi.qcd.put ( "t" + tileIdx , ms );
			// If the tile header is being read set default value of
			// dequantization spec for tile
			switch ( qType ) {
				case Markers.SQCX_NO_QUANTIZATION:
					this.decSpec.qts.setTileDef ( tileIdx , "reversible" );
					break;
				case Markers.SQCX_SCALAR_DERIVED:
					this.decSpec.qts.setTileDef ( tileIdx , "derived" );
					break;
				case Markers.SQCX_SCALAR_EXPOUNDED:
					this.decSpec.qts.setTileDef ( tileIdx , "expounded" );
					break;
				default:
					throw new CorruptedCodestreamException ( "Unknown or unsupported quantization style "
							+ "in Sqcd field, QCD marker, tile header" );
			}
		}

		qParms = new StdDequantizerParams ( );

		if ( Markers.SQCX_NO_QUANTIZATION == qType ) {
			final int maxrl = (
					mainh ? ( ( Integer ) this.decSpec.dls.getDefault ( ) ).intValue ( ) : (
							( Integer ) this.decSpec.dls
									.getTileDef ( tileIdx )
					).intValue ( )
			);
			int j, rl;
			int minb, maxb, hpd;
			int tmp;

			exp = qParms.exp = new int[ maxrl + 1 ][];
			ms.spqcd = new int[ maxrl + 1 ][ 4 ];

			for ( rl = 0; rl <= maxrl ; rl++ ) { // Loop on resolution levels
				// Find the number of subbands in the resolution level
				if ( 0 == rl ) { // Only the LL subband
					minb = 0;
					maxb = 1;
				}
				else {
					// Dyadic decomposition
					hpd = 1;

					// Adapt hpd to resolution level
					if ( hpd > maxrl - rl ) {
						hpd -= maxrl - rl;
					}
					else {
						hpd = 1;
					}
					// Determine max and min subband index
					minb = 1 << ( ( hpd - 1 ) << 1 ); // minb = 4^(hpd-1)
					maxb = 1 << ( hpd << 1 ); // maxb = 4^hpd
				}
				// Allocate array for subbands in resolution level
				exp[ rl ] = new int[ maxb ];

				for ( j = minb; j < maxb ; j++ ) {
					tmp = ms.spqcd[ rl ][ j ] = ehs.readUnsignedByte ( );
					exp[ rl ][ j ] = ( tmp >> Markers.SQCX_EXP_SHIFT ) & Markers.SQCX_EXP_MASK;
				}
			}// end for rl
		}
		else {
			final int maxrl = ( Markers.SQCX_SCALAR_DERIVED == qType ) ? 0 : (
					mainh ? ( ( Integer ) this.decSpec.dls.getDefault ( ) ).intValue ( )
							: ( ( Integer ) this.decSpec.dls.getTileDef ( tileIdx ) ).intValue ( )
			);
			int j, rl;
			int minb, maxb, hpd;
			int tmp;

			exp = qParms.exp = new int[ maxrl + 1 ][];
			nStep = qParms.nStep = new float[ maxrl + 1 ][];
			ms.spqcd = new int[ maxrl + 1 ][ 4 ];

			for ( rl = 0; rl <= maxrl ; rl++ ) { // Loop on resolution levels
				// Find the number of subbands in the resolution level
				if ( 0 == rl ) { // Only the LL subband
					minb = 0;
					maxb = 1;
				}
				else {
					// Dyadic decomposition
					hpd = 1;

					// Adapt hpd to resolution level
					if ( hpd > maxrl - rl ) {
						hpd -= maxrl - rl;
					}
					else {
						hpd = 1;
					}
					// Determine max and min subband index
					minb = 1 << ( ( hpd - 1 ) << 1 ); // minb = 4^(hpd-1)
					maxb = 1 << ( hpd << 1 ); // maxb = 4^hpd
				}
				// Allocate array for subbands in resolution level
				exp[ rl ] = new int[ maxb ];
				nStep[ rl ] = new float[ maxb ];

				for ( j = minb; j < maxb ; j++ ) {
					tmp = ms.spqcd[ rl ][ j ] = ehs.readUnsignedShort ( );
					exp[ rl ][ j ] = ( tmp >> 11 ) & 0x1f;
					// NOTE: the formula below does not support more than 5
					// bits for the exponent, otherwise (-1<<exp) might
					// overflow (the - is used to be able to represent 2**31)
					nStep[ rl ][ j ] = ( - 1.0f - ( ( float ) ( tmp & 0x07ff ) ) / ( 1 << 11 ) ) / ( - 1 << exp[ rl ][ j ] );
				}
			}// end for rl
		} // end if (qType != SQCX_NO_QUANTIZATION)

		// Fill qsss, gbs
		if ( mainh ) {
			this.decSpec.qsss.setDefault ( qParms );
			this.decSpec.gbs.setDefault ( Integer.valueOf ( guardBits ) );
		}
		else {
			this.decSpec.qsss.setTileDef ( tileIdx , qParms );
			this.decSpec.gbs.setTileDef ( tileIdx , Integer.valueOf ( guardBits ) );
		}

		// Check marker length
		this.checkMarkerLength ( ehs , "QCD marker" );
	}

	/**
	 * Reads a QCC marker segment and realigns the codestream at the point where
	 * the next marker should be found. QCC is a functional marker segment that
	 * describes the quantization of one component.
	 *
	 * @param ehs     The encoded stream.
	 * @param mainh   Flag indicating whether or not this marker segment is read
	 *                from the main header.
	 * @param tileIdx The index of the current tile
	 * @param tpIdx   Tile-part index
	 * @throws IOException If an I/O error occurs while reading from the encoded
	 *                     header stream.
	 */
	private void readQCC ( final DataInputStream ehs , final boolean mainh , final int tileIdx , final int tpIdx ) throws IOException {
		final int cComp; // current component
		int tmp;
		final StdDequantizerParams qParms;
		final int[][] expC;
		float[][] nStepC = null;
		final HeaderInfo.QCC ms = this.hi.getNewQCC ( );

		// Lqcc (length of QCC field)
		ms.lqcc = ehs.readUnsignedShort ( );

		// Cqcc
		if ( 257 > nComp ) {
			cComp = ms.cqcc = ehs.readUnsignedByte ( );
		}
		else {
			cComp = ms.cqcc = ehs.readUnsignedShort ( );
		}
		if ( cComp >= this.nComp ) {
			throw new CorruptedCodestreamException ( "Invalid component index in QCC marker" );
		}

		// Sqcc (quantization style)
		ms.sqcc = ehs.readUnsignedByte ( );
		final int guardBits = ms.getNumGuardBits ( );
		final int qType = ms.getQuantType ( );

		if ( mainh ) {
			this.hi.qcc.put ( "main_c" + cComp , ms );
			// If main header is being read, set default for component in all
			// tiles
			switch ( qType ) {
				case Markers.SQCX_NO_QUANTIZATION:
					this.decSpec.qts.setCompDef ( cComp , "reversible" );
					break;
				case Markers.SQCX_SCALAR_DERIVED:
					this.decSpec.qts.setCompDef ( cComp , "derived" );
					break;
				case Markers.SQCX_SCALAR_EXPOUNDED:
					this.decSpec.qts.setCompDef ( cComp , "expounded" );
					break;
				default:
					throw new CorruptedCodestreamException ( "Unknown or unsupported quantization style "
							+ "in Sqcd field, QCD marker, main header" );
			}
		}
		else {
			this.hi.qcc.put ( "t" + tileIdx + "_c" + cComp , ms );
			// If tile header is being read, set value for component in
			// this tiles
			switch ( qType ) {
				case Markers.SQCX_NO_QUANTIZATION:
					this.decSpec.qts.setTileCompVal ( tileIdx , cComp , "reversible" );
					break;
				case Markers.SQCX_SCALAR_DERIVED:
					this.decSpec.qts.setTileCompVal ( tileIdx , cComp , "derived" );
					break;
				case Markers.SQCX_SCALAR_EXPOUNDED:
					this.decSpec.qts.setTileCompVal ( tileIdx , cComp , "expounded" );
					break;
				default:
					throw new CorruptedCodestreamException ( "Unknown or unsupported quantization style "
							+ "in Sqcd field, QCD marker, main header" );
			}
		}

		// Decode all dequantizer params
		qParms = new StdDequantizerParams ( );

		if ( Markers.SQCX_NO_QUANTIZATION == qType ) {
			final int maxrl = (
					mainh ? ( ( Integer ) this.decSpec.dls.getCompDef ( cComp ) ).intValue ( ) : (
							( Integer ) this.decSpec.dls
									.getTileCompVal ( tileIdx , cComp )
					).intValue ( )
			);
			int j, rl;
			int minb, maxb, hpd;

			expC = qParms.exp = new int[ maxrl + 1 ][];
			ms.spqcc = new int[ maxrl + 1 ][ 4 ];

			for ( rl = 0; rl <= maxrl ; rl++ ) { // Loop on resolution levels
				// Find the number of subbands in the resolution level
				if ( 0 == rl ) { // Only the LL subband
					minb = 0;
					maxb = 1;
				}
				else {
					// Dyadic decomposition
					hpd = 1;

					// Adapt hpd to resolution level
					if ( hpd > maxrl - rl ) {
						hpd -= maxrl - rl;
					}
					else {
						hpd = 1;
					}
					// Determine max and min subband index
					minb = 1 << ( ( hpd - 1 ) << 1 ); // minb = 4^(hpd-1)
					maxb = 1 << ( hpd << 1 ); // maxb = 4^hpd
				}
				// Allocate array for subbands in resolution level
				expC[ rl ] = new int[ maxb ];

				for ( j = minb; j < maxb ; j++ ) {
					tmp = ms.spqcc[ rl ][ j ] = ehs.readUnsignedByte ( );
					expC[ rl ][ j ] = ( tmp >> Markers.SQCX_EXP_SHIFT ) & Markers.SQCX_EXP_MASK;
				}
			}// end for rl
		}
		else {
			final int maxrl = ( Markers.SQCX_SCALAR_DERIVED == qType ) ? 0 : (
					mainh ? ( ( Integer ) this.decSpec.dls.getCompDef ( cComp ) )
							.intValue ( ) : ( ( Integer ) this.decSpec.dls.getTileCompVal ( tileIdx , cComp ) ).intValue ( )
			);
			int j, rl;
			int minb, maxb, hpd;

			nStepC = qParms.nStep = new float[ maxrl + 1 ][];
			expC = qParms.exp = new int[ maxrl + 1 ][];
			ms.spqcc = new int[ maxrl + 1 ][ 4 ];

			for ( rl = 0; rl <= maxrl ; rl++ ) { // Loop on resolution levels
				// Find the number of subbands in the resolution level
				if ( 0 == rl ) { // Only the LL subband
					minb = 0;
					maxb = 1;
				}
				else {
					// Dyadic decomposition
					hpd = 1;

					// Adapt hpd to resolution level
					if ( hpd > maxrl - rl ) {
						hpd -= maxrl - rl;
					}
					else {
						hpd = 1;
					}
					// Determine max and min subband index
					minb = 1 << ( ( hpd - 1 ) << 1 ); // minb = 4^(hpd-1)
					maxb = 1 << ( hpd << 1 ); // maxb = 4^hpd
				}
				// Allocate array for subbands in resolution level
				expC[ rl ] = new int[ maxb ];
				nStepC[ rl ] = new float[ maxb ];

				for ( j = minb; j < maxb ; j++ ) {
					tmp = ms.spqcc[ rl ][ j ] = ehs.readUnsignedShort ( );
					expC[ rl ][ j ] = ( tmp >> 11 ) & 0x1f;
					// NOTE: the formula below does not support more than 5
					// bits for the exponent, otherwise (-1<<exp) might
					// overflow (the - is used to be able to represent 2**31)
					nStepC[ rl ][ j ] = ( - 1.0f - ( ( float ) ( tmp & 0x07ff ) ) / ( 1 << 11 ) ) / ( - 1 << expC[ rl ][ j ] );
				}
			}// end for rl
		} // end if (qType != SQCX_NO_QUANTIZATION)

		// Fill qsss, gbs
		if ( mainh ) {
			this.decSpec.qsss.setCompDef ( cComp , qParms );
			this.decSpec.gbs.setCompDef ( cComp , Integer.valueOf ( guardBits ) );
		}
		else {
			this.decSpec.qsss.setTileCompVal ( tileIdx , cComp , qParms );
			this.decSpec.gbs.setTileCompVal ( tileIdx , cComp , Integer.valueOf ( guardBits ) );
		}

		// Check marker length
		this.checkMarkerLength ( ehs , "QCC marker" );
	}

	/**
	 * Reads a COD marker segment and realigns the codestream where the next
	 * marker should be found.
	 *
	 * @param ehs     The encoder header stream.
	 * @param mainh   Flag indicating whether or not this marker segment is read
	 *                from the main header.
	 * @param tileIdx The index of the current tile
	 * @param tpIdx   Tile-part index
	 * @throws IOException If an I/O error occurs while reading from the encoder
	 *                     header stream
	 */
	private void readCOD ( final DataInputStream ehs , final boolean mainh , final int tileIdx , final int tpIdx ) throws IOException {
		int cstyle; // The block style
		final SynWTFilter[] hfilters;
		final SynWTFilter[] vfilters;
		final Integer[] cblk;
		final String errMsg;
		final HeaderInfo.COD ms = this.hi.getNewCOD ( );

		// Lcod (marker length)
		ms.lcod = ehs.readUnsignedShort ( );

		// Scod (block style)
		// We only support wavelet transformed data
		cstyle = ms.scod = ehs.readUnsignedByte ( );

		if ( 0 != ( cstyle & Markers.SCOX_PRECINCT_PARTITION ) ) {
			this.precinctPartitionIsUsed = true;
			// Remove flag
			cstyle &= ~ ( Markers.SCOX_PRECINCT_PARTITION );
		}
		else {
			this.precinctPartitionIsUsed = false;
		}

		// SOP markers
		if ( mainh ) {
			this.hi.cod.put ( "main" , ms );

			if ( 0 != ( cstyle & Markers.SCOX_USE_SOP ) ) {
				// SOP markers are used
				this.decSpec.sops.setDefault ( Boolean.TRUE );
				// Remove flag
				cstyle &= ~ ( Markers.SCOX_USE_SOP );
			}
			else {
				// SOP markers are not used
				this.decSpec.sops.setDefault ( Boolean.FALSE );
			}
		}
		else {
			this.hi.cod.put ( "t" + tileIdx , ms );

			if ( 0 != ( cstyle & Markers.SCOX_USE_SOP ) ) {
				// SOP markers are used
				this.decSpec.sops.setTileDef ( tileIdx , Boolean.TRUE );
				// Remove flag
				cstyle &= ~ ( Markers.SCOX_USE_SOP );
			}
			else {
				// SOP markers are not used
				this.decSpec.sops.setTileDef ( tileIdx , Boolean.FALSE );
			}
		}

		// EPH markers
		if ( mainh ) {
			if ( 0 != ( cstyle & Markers.SCOX_USE_EPH ) ) {
				// EPH markers are used
				this.decSpec.ephs.setDefault ( Boolean.TRUE );
				// Remove flag
				cstyle &= ~ ( Markers.SCOX_USE_EPH );
			}
			else {
				// EPH markers are not used
				this.decSpec.ephs.setDefault ( Boolean.FALSE );
			}
		}
		else {
			if ( 0 != ( cstyle & Markers.SCOX_USE_EPH ) ) {
				// EPH markers are used
				this.decSpec.ephs.setTileDef ( tileIdx , Boolean.TRUE );
				// Remove flag
				cstyle &= ~ ( Markers.SCOX_USE_EPH );
			}
			else {
				// EPH markers are not used
				this.decSpec.ephs.setTileDef ( tileIdx , Boolean.FALSE );
			}
		}

		// Code-block partition origin
		if ( 0 != ( cstyle & ( Markers.SCOX_HOR_CB_PART | Markers.SCOX_VER_CB_PART ) ) ) {
			FacilityManager.getMsgLogger ( ).printmsg (
					MsgLogger.WARNING ,
					"Code-block partition origin different from (0,0). This is defined in JPEG 2000"
							+ " part 2 and may not be supported by all JPEG 2000 decoders."
			);
		}
		if ( 0 != ( cstyle & Markers.SCOX_HOR_CB_PART ) ) {
			if ( - 1 != cb0x && 0 == cb0x ) {
				throw new IllegalArgumentException ( "Code-block partition origin redefined in new"
						+ " COD marker segment. Not supported by JJ2000" );
			}
			this.cb0x = 1;
			cstyle &= ~ ( Markers.SCOX_HOR_CB_PART );
		}
		else {
			if ( - 1 != cb0x && 1 == cb0x ) {
				throw new IllegalArgumentException ( "Code-block partition origin redefined in new"
						+ " COD marker segment. Not supported by JJ2000" );
			}
			this.cb0x = 0;
		}
		if ( 0 != ( cstyle & Markers.SCOX_VER_CB_PART ) ) {
			if ( - 1 != cb0y && 0 == cb0y ) {
				throw new IllegalArgumentException ( "Code-block partition origin redefined in new"
						+ " COD marker segment. Not supported by JJ2000" );
			}
			this.cb0y = 1;
			cstyle &= ~ ( Markers.SCOX_VER_CB_PART );
		}
		else {
			if ( - 1 != cb0y && 1 == cb0y ) {
				throw new IllegalArgumentException ( "Code-block partition origin redefined in new"
						+ " COD marker segment. Not supported by JJ2000" );
			}
			this.cb0y = 0;
		}

		// SGcod
		// Read the progressive order
		ms.sgcod_po = ehs.readUnsignedByte ( );

		// Read the number of layers
		ms.sgcod_nl = ehs.readUnsignedShort ( );
		if ( 0 >= ms.sgcod_nl || 65535 < ms.sgcod_nl ) {
			throw new CorruptedCodestreamException ( "Number of layers out of range: 1--65535" );
		}

		// Multiple component transform
		ms.sgcod_mct = ehs.readUnsignedByte ( );

		// SPcod
		// decomposition levels
		final int mrl = ms.spcod_ndl = ehs.readUnsignedByte ( );
		if ( 32 < mrl ) {
			throw new CorruptedCodestreamException ( "Number of decomposition levels out of range: 0--32" );
		}

		// Read the code-blocks dimensions
		cblk = new Integer[ 2 ];
		ms.spcod_cw = ehs.readUnsignedByte ( );
		cblk[ 0 ] = Integer.valueOf ( 1 << ( ms.spcod_cw + 2 ) );
		if ( StdEntropyCoderOptions.MIN_CB_DIM > cblk[ 0 ].intValue ( )
				|| StdEntropyCoderOptions.MAX_CB_DIM < cblk[ 0 ].intValue ( ) ) {
			errMsg = "Non-valid code-block width in SPcod field, COD marker";
			throw new CorruptedCodestreamException ( errMsg );
		}
		ms.spcod_ch = ehs.readUnsignedByte ( );
		cblk[ 1 ] = Integer.valueOf ( 1 << ( ms.spcod_ch + 2 ) );
		if ( StdEntropyCoderOptions.MIN_CB_DIM > cblk[ 1 ].intValue ( )
				|| StdEntropyCoderOptions.MAX_CB_DIM < cblk[ 1 ].intValue ( ) ) {
			errMsg = "Non-valid code-block height in SPcod field, COD marker";
			throw new CorruptedCodestreamException ( errMsg );
		}
		if ( StdEntropyCoderOptions.MAX_CB_AREA < ( cblk[ 0 ].intValue ( ) * cblk[ 1 ].intValue ( ) ) ) {
			errMsg = "Non-valid code-block area in SPcod field, COD marker";
			throw new CorruptedCodestreamException ( errMsg );
		}
		if ( mainh ) {
			this.decSpec.cblks.setDefault ( cblk );
		}
		else {
			this.decSpec.cblks.setTileDef ( tileIdx , cblk );
		}

		// Style of the code-block coding passes
		final int ecOptions = ms.spcod_cs = ehs.readUnsignedByte ( );
		if ( 0 != ( ecOptions & ~ ( StdEntropyCoderOptions.OPT_BYPASS | StdEntropyCoderOptions.OPT_RESET_MQ | StdEntropyCoderOptions.OPT_TERM_PASS | StdEntropyCoderOptions.OPT_VERT_STR_CAUSAL | StdEntropyCoderOptions.OPT_PRED_TERM | StdEntropyCoderOptions.OPT_SEG_SYMBOLS ) ) ) {
			throw new CorruptedCodestreamException ( "Unknown \"code-block style\" in SPcod field, " + "COD marker: 0x"
					+ Integer.toHexString ( ecOptions ) );
		}

		// Read wavelet filter for tile or image
		hfilters = new SynWTFilter[ 1 ];
		vfilters = new SynWTFilter[ 1 ];
		hfilters[ 0 ] = this.readFilter ( ehs , ms.spcod_t );
		vfilters[ 0 ] = hfilters[ 0 ];

		// Fill the filter spec
		// If this is the main header, set the default value, if it is the
		// tile header, set default for this tile
		final SynWTFilter[][] hvfilters = new SynWTFilter[ 2 ][];
		hvfilters[ 0 ] = hfilters;
		hvfilters[ 1 ] = vfilters;

		// Get precinct partition sizes
		@SuppressWarnings ("unchecked") final Vector<Integer>[] v = new Vector[ 2 ];
		v[ 0 ] = new Vector<Integer> ( );
		v[ 1 ] = new Vector<Integer> ( );
		int val = Markers.PRECINCT_PARTITION_DEF_SIZE;
		if ( ! this.precinctPartitionIsUsed ) {
			final Integer w;
			final Integer h;
			w = Integer.valueOf ( 1 << ( val & 0x000F ) );
			v[ 0 ].addElement ( w );
			h = Integer.valueOf ( 1 << ( ( ( val & 0x00F0 ) >> 4 ) ) );
			v[ 1 ].addElement ( h );
		}
		else {
			ms.spcod_ps = new int[ mrl + 1 ];
			for ( int rl = mrl ; 0 <= rl ; rl-- ) {
				final Integer w;
				final Integer h;
				val = ms.spcod_ps[ mrl - rl ] = ehs.readUnsignedByte ( );
				w = Integer.valueOf ( 1 << ( val & 0x000F ) );
				v[ 0 ].insertElementAt ( w , 0 );
				h = Integer.valueOf ( 1 << ( ( ( val & 0x00F0 ) >> 4 ) ) );
				v[ 1 ].insertElementAt ( h , 0 );
			}
		}
		if ( mainh ) {
			this.decSpec.pss.setDefault ( v );
		}
		else {
			this.decSpec.pss.setTileDef ( tileIdx , v );
		}
		this.precinctPartitionIsUsed = true;

		// Check marker length
		this.checkMarkerLength ( ehs , "COD marker" );

		// Store specifications in decSpec
		if ( mainh ) {
			this.decSpec.wfs.setDefault ( hvfilters );
			this.decSpec.dls.setDefault ( Integer.valueOf ( mrl ) );
			this.decSpec.ecopts.setDefault ( Integer.valueOf ( ecOptions ) );
			this.decSpec.cts.setDefault ( Integer.valueOf ( ms.sgcod_mct ) );
			this.decSpec.nls.setDefault ( Integer.valueOf ( ms.sgcod_nl ) );
			this.decSpec.pos.setDefault ( Integer.valueOf ( ms.sgcod_po ) );
		}
		else {
			this.decSpec.wfs.setTileDef ( tileIdx , hvfilters );
			this.decSpec.dls.setTileDef ( tileIdx , Integer.valueOf ( mrl ) );
			this.decSpec.ecopts.setTileDef ( tileIdx , Integer.valueOf ( ecOptions ) );
			this.decSpec.cts.setTileDef ( tileIdx , Integer.valueOf ( ms.sgcod_mct ) );
			this.decSpec.nls.setTileDef ( tileIdx , Integer.valueOf ( ms.sgcod_nl ) );
			this.decSpec.pos.setTileDef ( tileIdx , Integer.valueOf ( ms.sgcod_po ) );
		}
	}


	/**
	 * Reads TLM marker segment and realigns the codestream where the next
	 * marker should be found. Informations stored in these fields are currently
	 * NOT taken into account.
	 *
	 * @param ehs
	 *            The encoder header stream.
	 *
	 * @exception IOException
	 *                If an I/O error occurs while reading from the encoder
	 *                header stream
	 */
/*	private void readTLM(DataInputStream ehs) throws IOException
	{
		int length;

		length = ehs.readUnsignedShort();
		// Ignore all informations contained
		ehs.skipBytes(length - 2);

		FacilityManager.getMsgLogger().printmsg(MsgLogger.INFO, "Skipping unsupported TLM marker");
	}
*/
	/**
	 * Reads PLM marker segment and realigns the codestream where the next
	 * marker should be found. Informations stored in these fields are currently
	 * not taken into account.
	 *
	 * @param ehs
	 *            The encoder header stream.
	 *
	 * @exception IOException
	 *                If an I/O error occurs while reading from the encoder
	 *                header stream
	 */
/*	private void readPLM(DataInputStream ehs) throws IOException
	{
		int length;

		length = ehs.readUnsignedShort();
		// Ignore all informations contained
		ehs.skipBytes(length - 2);

		FacilityManager.getMsgLogger().printmsg(MsgLogger.INFO, "Skipping unsupported PLM marker");
	}
*/
	/**
	 * Reads the PLT fields and realigns the codestream where the next marker
	 * should be found. Informations stored in these fields are currently NOT
	 * taken into account.
	 *
	 * @param ehs
	 *            The encoder header stream.
	 *
	 * @exception IOException
	 *                If an I/O error occurs while reading from the encoder
	 *                header stream
	 */
/*	private void readPLTFields(DataInputStream ehs) throws IOException
	{
		int length;

		length = ehs.readUnsignedShort();
		// Ignore all informations contained
		ehs.skipBytes(length - 2);

		FacilityManager.getMsgLogger().printmsg(MsgLogger.INFO, "Skipping unsupported PLT marker");
	}
*/

	/**
	 * Reads the COC marker segment and realigns the codestream where the next
	 * marker should be found.
	 *
	 * @param ehs     The encoder header stream.
	 * @param mainh   Flag indicating whether or not this marker segment is read
	 *                from the main header.
	 * @param tileIdx The index of the current tile
	 * @param tpIdx   Tile-part index
	 * @throws IOException If an I/O error occurs while reading from the encoder
	 *                     header stream
	 */
	private void readCOC ( final DataInputStream ehs , final boolean mainh , final int tileIdx , final int tpIdx ) throws IOException {
		final int cComp; // current component
		final SynWTFilter[] hfilters;
		final SynWTFilter[] vfilters;
		final int ecOptions;
		final Integer[] cblk;
		final String errMsg;
		final HeaderInfo.COC ms = this.hi.getNewCOC ( );

		// Lcoc (marker length)
		ms.lcoc = ehs.readUnsignedShort ( );

		// Ccoc
		if ( 257 > nComp ) {
			cComp = ms.ccoc = ehs.readUnsignedByte ( );
		}
		else {
			cComp = ms.ccoc = ehs.readUnsignedShort ( );
		}
		if ( cComp >= this.nComp ) {
			throw new CorruptedCodestreamException ( "Invalid component index in QCC marker" );
		}

		// Scoc (block style)
		int cstyle = ms.scoc = ehs.readUnsignedByte ( );
		if ( 0 != ( cstyle & Markers.SCOX_PRECINCT_PARTITION ) ) {
			this.precinctPartitionIsUsed = true;
			// Remove flag
			cstyle &= ~ ( Markers.SCOX_PRECINCT_PARTITION );
		}
		else {
			this.precinctPartitionIsUsed = false;
		}

		// SPcoc

		// decomposition levels
		final int mrl = ms.spcoc_ndl = ehs.readUnsignedByte ( );

		// Read the code-blocks dimensions
		cblk = new Integer[ 2 ];
		ms.spcoc_cw = ehs.readUnsignedByte ( );
		cblk[ 0 ] = Integer.valueOf ( 1 << ( ms.spcoc_cw + 2 ) );
		if ( StdEntropyCoderOptions.MIN_CB_DIM > cblk[ 0 ].intValue ( )
				|| StdEntropyCoderOptions.MAX_CB_DIM < cblk[ 0 ].intValue ( ) ) {
			errMsg = "Non-valid code-block width in SPcod field, COC marker";
			throw new CorruptedCodestreamException ( errMsg );
		}
		ms.spcoc_ch = ehs.readUnsignedByte ( );
		cblk[ 1 ] = Integer.valueOf ( 1 << ( ms.spcoc_ch + 2 ) );
		if ( StdEntropyCoderOptions.MIN_CB_DIM > cblk[ 1 ].intValue ( )
				|| StdEntropyCoderOptions.MAX_CB_DIM < cblk[ 1 ].intValue ( ) ) {
			errMsg = "Non-valid code-block height in SPcod field, COC marker";
			throw new CorruptedCodestreamException ( errMsg );
		}
		if ( StdEntropyCoderOptions.MAX_CB_AREA < ( cblk[ 0 ].intValue ( ) * cblk[ 1 ].intValue ( ) ) ) {
			errMsg = "Non-valid code-block area in SPcod field, COC marker";
			throw new CorruptedCodestreamException ( errMsg );
		}
		if ( mainh ) {
			this.decSpec.cblks.setCompDef ( cComp , cblk );
		}
		else {
			this.decSpec.cblks.setTileCompVal ( tileIdx , cComp , cblk );
		}

		// Read entropy block mode options
		// NOTE: currently OPT_SEG_SYMBOLS is not included here
		ecOptions = ms.spcoc_cs = ehs.readUnsignedByte ( );
		if ( 0 != ( ecOptions & ~ ( StdEntropyCoderOptions.OPT_BYPASS | StdEntropyCoderOptions.OPT_RESET_MQ | StdEntropyCoderOptions.OPT_TERM_PASS | StdEntropyCoderOptions.OPT_VERT_STR_CAUSAL | StdEntropyCoderOptions.OPT_PRED_TERM | StdEntropyCoderOptions.OPT_SEG_SYMBOLS ) ) ) {
			throw new CorruptedCodestreamException ( "Unknown \"code-block context\" in SPcoc field, " + "COC marker: 0x"
					+ Integer.toHexString ( ecOptions ) );
		}

		// Read wavelet filter for tile or image
		hfilters = new SynWTFilter[ 1 ];
		vfilters = new SynWTFilter[ 1 ];
		hfilters[ 0 ] = this.readFilter ( ehs , ms.spcoc_t );
		vfilters[ 0 ] = hfilters[ 0 ];

		// Fill the filter spec
		// If this is the main header, set the default value, if it is the
		// tile header, set default for this tile
		final SynWTFilter[][] hvfilters = new SynWTFilter[ 2 ][];
		hvfilters[ 0 ] = hfilters;
		hvfilters[ 1 ] = vfilters;

		// Get precinct partition sizes
		@SuppressWarnings ("unchecked") final Vector<Integer>[] v = new Vector[ 2 ];
		v[ 0 ] = new Vector<Integer> ( );
		v[ 1 ] = new Vector<Integer> ( );
		int val = Markers.PRECINCT_PARTITION_DEF_SIZE;
		if ( ! this.precinctPartitionIsUsed ) {
			final Integer w;
			final Integer h;
			w = Integer.valueOf ( 1 << ( val & 0x000F ) );
			v[ 0 ].addElement ( w );
			h = Integer.valueOf ( 1 << ( ( ( val & 0x00F0 ) >> 4 ) ) );
			v[ 1 ].addElement ( h );
		}
		else {
			ms.spcoc_ps = new int[ mrl + 1 ];
			for ( int rl = mrl ; 0 <= rl ; rl-- ) {
				final Integer w;
				final Integer h;
				val = ms.spcoc_ps[ rl ] = ehs.readUnsignedByte ( );
				w = Integer.valueOf ( 1 << ( val & 0x000F ) );
				v[ 0 ].insertElementAt ( w , 0 );
				h = Integer.valueOf ( 1 << ( ( ( val & 0x00F0 ) >> 4 ) ) );
				v[ 1 ].insertElementAt ( h , 0 );
			}
		}
		if ( mainh ) {
			this.decSpec.pss.setCompDef ( cComp , v );
		}
		else {
			this.decSpec.pss.setTileCompVal ( tileIdx , cComp , v );
		}
		this.precinctPartitionIsUsed = true;

		// Check marker length
		this.checkMarkerLength ( ehs , "COD marker" );

		if ( mainh ) {
			this.hi.coc.put ( "main_c" + cComp , ms );
			this.decSpec.wfs.setCompDef ( cComp , hvfilters );
			this.decSpec.dls.setCompDef ( cComp , Integer.valueOf ( mrl ) );
			this.decSpec.ecopts.setCompDef ( cComp , Integer.valueOf ( ecOptions ) );
		}
		else {
			this.hi.coc.put ( "t" + tileIdx + "_c" + cComp , ms );
			this.decSpec.wfs.setTileCompVal ( tileIdx , cComp , hvfilters );
			this.decSpec.dls.setTileCompVal ( tileIdx , cComp , Integer.valueOf ( mrl ) );
			this.decSpec.ecopts.setTileCompVal ( tileIdx , cComp , Integer.valueOf ( ecOptions ) );
		}
	}

	/**
	 * Reads the POC marker segment and realigns the codestream where the next
	 * marker should be found.
	 *
	 * @param ehs   The encoder header stream.
	 * @param mainh Flag indicating whether or not this marker segment is read
	 *              from the main header.
	 * @param t     The index of the current tile
	 * @param tpIdx Tile-part index
	 * @throws IOException If an I/O error occurs while reading from the encoder
	 *                     header stream
	 */
	private void readPOC ( final DataInputStream ehs , final boolean mainh , final int t , final int tpIdx ) throws IOException {

		final boolean useShort = 256 <= nComp;
		int tmp;
		int nOldChg = 0;
		final HeaderInfo.POC ms;
		if ( mainh || null == hi.poc.get ( "t" + t ) ) {
			ms = this.hi.getNewPOC ( );
		}
		else {
			ms = this.hi.poc.get ( "t" + t );
			nOldChg = ms.rspoc.length;
		}

		// Lpoc
		ms.lpoc = ehs.readUnsignedShort ( );

		// Compute the number of new progression changes
		// newChg = (lpoc - Lpoc(2)) / (RSpoc(1) + CSpoc(2) +
		// LYEpoc(2) + REpoc(1) + CEpoc(2) + Ppoc (1) )
		final int newChg = ( ms.lpoc - 2 ) / ( 5 + ( useShort ? 4 : 2 ) );
		final int ntotChg = nOldChg + newChg;

		final int[][] change;
		if ( 0 != nOldChg ) {
			// Creates new arrays
			change = new int[ ntotChg ][ 6 ];
			final int[] tmprspoc = new int[ ntotChg ];
			final int[] tmpcspoc = new int[ ntotChg ];
			final int[] tmplyepoc = new int[ ntotChg ];
			final int[] tmprepoc = new int[ ntotChg ];
			final int[] tmpcepoc = new int[ ntotChg ];
			final int[] tmpppoc = new int[ ntotChg ];

			// Copy old values
			final int[][] prevChg = ( int[][] ) this.decSpec.pcs.getTileDef ( t );
			for ( int chg = 0 ; chg < nOldChg ; chg++ ) {
				change[ chg ] = prevChg[ chg ];
				tmprspoc[ chg ] = ms.rspoc[ chg ];
				tmpcspoc[ chg ] = ms.cspoc[ chg ];
				tmplyepoc[ chg ] = ms.lyepoc[ chg ];
				tmprepoc[ chg ] = ms.repoc[ chg ];
				tmpcepoc[ chg ] = ms.cepoc[ chg ];
				tmpppoc[ chg ] = ms.ppoc[ chg ];
			}
			ms.rspoc = tmprspoc;
			ms.cspoc = tmpcspoc;
			ms.lyepoc = tmplyepoc;
			ms.repoc = tmprepoc;
			ms.cepoc = tmpcepoc;
			ms.ppoc = tmpppoc;
		}
		else {
			change = new int[ newChg ][ 6 ];
			ms.rspoc = new int[ newChg ];
			ms.cspoc = new int[ newChg ];
			ms.lyepoc = new int[ newChg ];
			ms.repoc = new int[ newChg ];
			ms.cepoc = new int[ newChg ];
			ms.ppoc = new int[ newChg ];
		}

		for ( int chg = nOldChg ; chg < ntotChg ; chg++ ) {
			// RSpoc
			change[ chg ][ 0 ] = ms.rspoc[ chg ] = ehs.readUnsignedByte ( );

			// CSpoc
			if ( useShort ) {
				change[ chg ][ 1 ] = ms.cspoc[ chg ] = ehs.readUnsignedShort ( );
			}
			else {
				change[ chg ][ 1 ] = ms.cspoc[ chg ] = ehs.readUnsignedByte ( );
			}

			// LYEpoc
			change[ chg ][ 2 ] = ms.lyepoc[ chg ] = ehs.readUnsignedShort ( );
			if ( 1 > change[ chg ][ 2 ] ) {
				throw new CorruptedCodestreamException ( "LYEpoc value must be greater than 1 in POC marker "
						+ "segment of tile " + t + ", tile-part " + tpIdx );
			}

			// REpoc
			change[ chg ][ 3 ] = ms.repoc[ chg ] = ehs.readUnsignedByte ( );
			if ( change[ chg ][ 3 ] <= change[ chg ][ 0 ] ) {
				throw new CorruptedCodestreamException ( "REpoc value must be greater than RSpoc in POC marker "
						+ "segment of tile " + t + ", tile-part " + tpIdx );
			}

			// CEpoc
			if ( useShort ) {
				change[ chg ][ 4 ] = ms.cepoc[ chg ] = ehs.readUnsignedShort ( );
			}
			else {
				tmp = ms.cepoc[ chg ] = ehs.readUnsignedByte ( );
				change[ chg ][ 4 ] = tmp;
			}
			if ( change[ chg ][ 4 ] <= change[ chg ][ 1 ] ) {
				throw new CorruptedCodestreamException ( "CEpoc value must be greater than CSpoc in POC marker "
						+ "segment of tile " + t + ", tile-part " + tpIdx );
			}

			// Ppoc
			change[ chg ][ 5 ] = ms.ppoc[ chg ] = ehs.readUnsignedByte ( );
		}

		// Check marker length
		this.checkMarkerLength ( ehs , "POC marker" );

		// Register specifications
		if ( mainh ) {
			this.hi.poc.put ( "main" , ms );
			this.decSpec.pcs.setDefault ( change );
		}
		else {
			this.hi.poc.put ( "t" + t , ms );
			this.decSpec.pcs.setTileDef ( t , change );
		}
	}

	/**
	 * Reads the RGN marker segment of the codestream header.
	 *
	 * <p>
	 * May be used in tile or main header. If used in main header, it refers to
	 * the maxshift value of a component in all tiles. When used in tile header,
	 * only the particular tile-component is affected.
	 *
	 * @param ehs     The encoder header stream.
	 * @param mainh   Flag indicating whether or not this marker segment is read
	 *                from the main header.
	 * @param tileIdx The index of the current tile
	 * @param tpIdx   Tile-part index
	 * @throws IOException If an I/O error occurs while reading from the encoder
	 *                     header stream
	 */
	private void readRGN ( final DataInputStream ehs , final boolean mainh , final int tileIdx , final int tpIdx ) throws IOException {
		final int comp; // ROI component
		final HeaderInfo.RGN ms = this.hi.getNewRGN ( );

		// Lrgn (marker length)
		ms.lrgn = ehs.readUnsignedShort ( );

		// Read component
		ms.crgn = comp = ( 257 > nComp ) ? ehs.readUnsignedByte ( ) : ehs.readUnsignedShort ( );
		if ( comp >= this.nComp ) {
			throw new CorruptedCodestreamException ( "Invalid component index in RGN marker" + comp );
		}

		// Read type of RGN.(Srgn)
		ms.srgn = ehs.readUnsignedByte ( );

		// Check that we can handle it.
		if ( Markers.SRGN_IMPLICIT != ms.srgn )
			throw new CorruptedCodestreamException ( "Unknown or unsupported Srgn parameter in ROI marker" );

		if ( null == decSpec.rois ) { // No maxshift spec defined
			// Create needed ModuleSpec
			this.decSpec.rois = new MaxShiftSpec ( this.nTiles , this.nComp , ModuleSpec.SPEC_TYPE_TILE_COMP );
		}

		// SPrgn
		ms.sprgn = ehs.readUnsignedByte ( );

		if ( mainh ) {
			this.hi.rgn.put ( "main_c" + comp , ms );
			this.decSpec.rois.setCompDef ( comp , Integer.valueOf ( ms.sprgn ) );
		}
		else {
			this.hi.rgn.put ( "t" + tileIdx + "_c" + comp , ms );
			this.decSpec.rois.setTileCompVal ( tileIdx , comp , Integer.valueOf ( ms.sprgn ) );
		}

		// Check marker length
		this.checkMarkerLength ( ehs , "RGN marker" );
	}

	/**
	 * Reads the PPM marker segment of the main header.
	 *
	 * @param ehs The encoder header stream.
	 * @throws IOException If an I/O error occurs while reading from the encoder
	 *                     header stream
	 */
	private void readPPM ( final DataInputStream ehs ) throws IOException {
		final int curMarkSegLen;
		final int indx;
		final int remSegLen;

		// If first time readPPM method is called allocate arrays for packed
		// packet data
		if ( null == pPMMarkerData ) {
			this.pPMMarkerData = new byte[ this.nPPMMarkSeg ][];
			this.tileOfTileParts = new Vector<Integer> ( );
			this.decSpec.pphs.setDefault ( Boolean.TRUE );
		}

		// Lppm (marker length)
		curMarkSegLen = ehs.readUnsignedShort ( );
		remSegLen = curMarkSegLen - 3;

		// Zppm (index of PPM marker)
		indx = ehs.readUnsignedByte ( );

		// Read Nppm and Ippm data
		this.pPMMarkerData[ indx ] = new byte[ remSegLen ];
		ehs.read ( this.pPMMarkerData[ indx ] , 0 , remSegLen );

		// Check marker length
		this.checkMarkerLength ( ehs , "PPM marker" );
	}

	/**
	 * Reads the PPT marker segment of the main header.
	 *
	 * @param ehs   The encoder header stream.
	 * @param tile  The tile to which the current tile part belongs
	 * @param tpIdx Tile-part index
	 * @throws IOException If an I/O error occurs while reading from the encoder
	 *                     header stream
	 */
	private void readPPT ( final DataInputStream ehs , final int tile , final int tpIdx ) throws IOException {
		final int curMarkSegLen;
		final int indx;
		final byte[] temp;

		if ( null == tilePartPkdPktHeaders ) {
			this.tilePartPkdPktHeaders = new byte[ this.nTiles ][][][];
		}

		if ( null == tilePartPkdPktHeaders[ tile ] ) {
			this.tilePartPkdPktHeaders[ tile ] = new byte[ this.nTileParts[ tile ] ][][];
		}

		if ( null == tilePartPkdPktHeaders[ tile ][ tpIdx ] ) {
			this.tilePartPkdPktHeaders[ tile ][ tpIdx ] = new byte[ this.nPPTMarkSeg[ tile ][ tpIdx ] ][];
		}

		// Lppt (marker length)
		curMarkSegLen = ehs.readUnsignedShort ( );

		// Zppt (index of PPT marker)
		indx = ehs.readUnsignedByte ( );

		// Ippt (packed packet headers)
		temp = new byte[ curMarkSegLen - 3 ];
		ehs.read ( temp );
		this.tilePartPkdPktHeaders[ tile ][ tpIdx ][ indx ] = temp;

		// Check marker length
		this.checkMarkerLength ( ehs , "PPT marker" );

		this.decSpec.pphs.setTileDef ( tile , Boolean.TRUE );
	}

	/**
	 * This method extract a marker segment from the main header and stores it
	 * into a byte buffer for the second pass. The marker segment is first
	 * identified. Then its flag is activated. Finally, its content is buffered
	 * into a byte array stored in an hashTable.
	 *
	 * <p>
	 * If the marker is not recognized, it prints a warning and skips it
	 * according to its length.
	 *
	 * <p>
	 * SIZ marker segment shall be the first encountered marker segment.
	 *
	 * @param marker The marker segment to process
	 * @param ehs    The encoded header stream
	 */
	private void extractMainMarkSeg ( final short marker , final RandomAccessIO ehs ) throws IOException {
		if ( 0 == nfMarkSeg ) { // First non-delimiting marker of the header
			// JPEG 2000 part 1 specify that it must be SIZ
			if ( Markers.SIZ != marker ) {
				throw new CorruptedCodestreamException ( "First marker after SOC must be SIZ "
						+ Integer.toHexString ( marker ) );
			}
		}

		String htKey = ""; // Name used as a key for the hash-table
		if ( null == ht ) {
			this.ht = new Hashtable<String, byte[]> ( );
		}

		switch ( marker ) {
			case Markers.SIZ:
				if ( 0 != ( nfMarkSeg & SIZ_FOUND ) ) {
					throw new CorruptedCodestreamException ( "More than one SIZ marker segment found in main header" );
				}
				this.nfMarkSeg |= HeaderDecoder.SIZ_FOUND;
				htKey = "SIZ";
				break;
			case Markers.SOD:
				throw new CorruptedCodestreamException ( "SOD found in main header" );
			case Markers.EOC:
				throw new CorruptedCodestreamException ( "EOC found in main header" );
			case Markers.SOT:
				if ( 0 != ( nfMarkSeg & SOT_FOUND ) ) {
					throw new CorruptedCodestreamException ( "More than one SOT marker found right after "
							+ "main or tile header" );
				}
				this.nfMarkSeg |= HeaderDecoder.SOT_FOUND;
				return;
			case Markers.COD:
				if ( 0 != ( nfMarkSeg & COD_FOUND ) ) {
					throw new CorruptedCodestreamException ( "More than one COD marker found in main header" );
				}
				this.nfMarkSeg |= HeaderDecoder.COD_FOUND;
				htKey = "COD";
				break;
			case Markers.COC:
				this.nfMarkSeg |= HeaderDecoder.COC_FOUND;
				htKey = "COC" + ( this.nCOCMarkSeg );
				this.nCOCMarkSeg++;
				break;
			case Markers.QCD:
				if ( 0 != ( nfMarkSeg & QCD_FOUND ) ) {
					throw new CorruptedCodestreamException ( "More than one QCD marker found in main header" );
				}
				this.nfMarkSeg |= HeaderDecoder.QCD_FOUND;
				htKey = "QCD";
				break;
			case Markers.QCC:
				this.nfMarkSeg |= HeaderDecoder.QCC_FOUND;
				htKey = "QCC" + ( this.nQCCMarkSeg );
				this.nQCCMarkSeg++;
				break;
			case Markers.RGN:
				this.nfMarkSeg |= HeaderDecoder.RGN_FOUND;
				htKey = "RGN" + ( this.nRGNMarkSeg );
				this.nRGNMarkSeg++;
				break;
			case Markers.COM:
				this.nfMarkSeg |= HeaderDecoder.COM_FOUND;
				htKey = "COM" + ( this.nCOMMarkSeg );
				this.nCOMMarkSeg++;
				break;
			case Markers.CRG:
				if ( 0 != ( nfMarkSeg & CRG_FOUND ) ) {
					throw new CorruptedCodestreamException ( "More than one CRG marker found in main header" );
				}
				this.nfMarkSeg |= HeaderDecoder.CRG_FOUND;
				htKey = "CRG";
				break;
			case Markers.PPM:
				this.nfMarkSeg |= HeaderDecoder.PPM_FOUND;
				htKey = "PPM" + ( this.nPPMMarkSeg );
				this.nPPMMarkSeg++;
				break;
			case Markers.TLM:
				if ( 0 != ( nfMarkSeg & TLM_FOUND ) ) {
					throw new CorruptedCodestreamException ( "More than one TLM marker found in main header" );
				}
				this.nfMarkSeg |= HeaderDecoder.TLM_FOUND;
				break;
			case Markers.PLM:
				if ( 0 != ( nfMarkSeg & PLM_FOUND ) ) {
					throw new CorruptedCodestreamException ( "More than one PLM marker found in main header" );
				}
				FacilityManager.getMsgLogger ( ).printmsg (
						MsgLogger.WARNING ,
						"PLM marker segment found but not used by by JJ2000 decoder."
				);
				this.nfMarkSeg |= HeaderDecoder.PLM_FOUND;
				htKey = "PLM";
				break;
			case Markers.POC:
				if ( 0 != ( nfMarkSeg & POC_FOUND ) ) {
					throw new CorruptedCodestreamException ( "More than one POC marker segment found " + "in main header" );
				}
				this.nfMarkSeg |= HeaderDecoder.POC_FOUND;
				htKey = "POC";
				break;
			case Markers.PLT:
				throw new CorruptedCodestreamException ( "PLT found in main header" );
			case Markers.PPT:
				throw new CorruptedCodestreamException ( "PPT found in main header" );
			default:
				htKey = "UNKNOWN";
				FacilityManager.getMsgLogger ( ).printmsg (
						MsgLogger.WARNING ,
						"Non recognized marker segment (0x" + Integer.toHexString ( marker ) + ") in main header!"
				);
				break;
		}

		if ( 0xffffff30 > marker || 0xffffff3f < marker ) {
			// Read marker segment length and create corresponding byte buffer
			final int markSegLen = ehs.readUnsignedShort ( );
			final byte[] buf = new byte[ markSegLen ];

			// Copy data (after re-insertion of the marker segment length);
			buf[ 0 ] = ( byte ) ( ( markSegLen >> 8 ) & 0xFF );
			buf[ 1 ] = ( byte ) ( markSegLen & 0xFF );
			ehs.readFully ( buf , 2 , markSegLen - 2 );

			if ( ! "UNKNOWN".equals ( htKey ) ) {
				// Store array in hashTable
				this.ht.put ( htKey , buf );
			}
		}
	}

	/**
	 * This method extracts a marker segment in a tile-part header and stores it
	 * into a byte buffer for the second pass. The marker is first recognized,
	 * then its flag is activated and, finally, its content is buffered in an
	 * element of byte arrays accessible thanks to a hashTable. If a marker
	 * segment is not recognized, it prints a warning and skip it according to
	 * its length.
	 *
	 * @param marker      The marker to process
	 * @param ehs         The encoded header stream
	 * @param tileIdx     The index of the current tile
	 * @param tilePartIdx The index of the current tile part
	 */
	public void extractTilePartMarkSeg ( final short marker , final RandomAccessIO ehs , final int tileIdx , final int tilePartIdx )
			throws IOException {

		String htKey = ""; // Name used as a hash-table key
		if ( null == ht ) {
			this.ht = new Hashtable<String, byte[]> ( );
		}

		switch ( marker ) {
			case Markers.SOT:
				throw new CorruptedCodestreamException ( "Second SOT marker segment found in tile-part header" );
			case Markers.SIZ:
				throw new CorruptedCodestreamException ( "SIZ found in tile-part header" );
			case Markers.EOC:
				throw new CorruptedCodestreamException ( "EOC found in tile-part header" );
			case Markers.TLM:
				throw new CorruptedCodestreamException ( "TLM found in tile-part header" );
			case Markers.PLM:
				throw new CorruptedCodestreamException ( "PLM found in tile-part header" );
			case Markers.PPM:
				throw new CorruptedCodestreamException ( "PPM found in tile-part header" );
			case Markers.COD:
				if ( 0 != ( nfMarkSeg & COD_FOUND ) ) {
					throw new CorruptedCodestreamException ( "More than one COD marker found in tile-part" + " header" );
				}
				this.nfMarkSeg |= HeaderDecoder.COD_FOUND;
				htKey = "COD";
				break;
			case Markers.COC:
				this.nfMarkSeg |= HeaderDecoder.COC_FOUND;
				htKey = "COC" + ( this.nCOCMarkSeg );
				this.nCOCMarkSeg++;
				break;
			case Markers.QCD:
				if ( 0 != ( nfMarkSeg & QCD_FOUND ) ) {
					throw new CorruptedCodestreamException ( "More than one QCD marker found in tile-part" + " header" );
				}
				this.nfMarkSeg |= HeaderDecoder.QCD_FOUND;
				htKey = "QCD";
				break;
			case Markers.QCC:
				this.nfMarkSeg |= HeaderDecoder.QCC_FOUND;
				htKey = "QCC" + ( this.nQCCMarkSeg );
				this.nQCCMarkSeg++;
				break;
			case Markers.RGN:
				this.nfMarkSeg |= HeaderDecoder.RGN_FOUND;
				htKey = "RGN" + ( this.nRGNMarkSeg );
				this.nRGNMarkSeg++;
				break;
			case Markers.COM:
				this.nfMarkSeg |= HeaderDecoder.COM_FOUND;
				htKey = "COM" + ( this.nCOMMarkSeg );
				this.nCOMMarkSeg++;
				break;
			case Markers.CRG:
				throw new CorruptedCodestreamException ( "CRG marker found in tile-part header" );
			case Markers.PPT:
				this.nfMarkSeg |= HeaderDecoder.PPT_FOUND;
				if ( null == nPPTMarkSeg ) {
					this.nPPTMarkSeg = new int[ this.nTiles ][];
				}
				if ( null == nPPTMarkSeg[ tileIdx ] ) {
					this.nPPTMarkSeg[ tileIdx ] = new int[ this.nTileParts[ tileIdx ] ];
				}
				htKey = "PPT" + ( this.nPPTMarkSeg[ tileIdx ][ tilePartIdx ] );
				this.nPPTMarkSeg[ tileIdx ][ tilePartIdx ]++;
				break;
			case Markers.SOD:
				this.nfMarkSeg |= HeaderDecoder.SOD_FOUND;
				return;
			case Markers.POC:
				if ( 0 != ( nfMarkSeg & POC_FOUND ) )
					throw new CorruptedCodestreamException ( "More than one POC marker segment found "
							+ "in tile-part header" );
				this.nfMarkSeg |= HeaderDecoder.POC_FOUND;
				htKey = "POC";
				break;
			case Markers.PLT:
				if ( 0 != ( nfMarkSeg & PLM_FOUND ) ) {
					throw new CorruptedCodestreamException ( "PLT marker found eventhough PLM marker "
							+ "found in main header" );
				}
				FacilityManager.getMsgLogger ( ).printmsg (
						MsgLogger.WARNING ,
						"PLT marker segment found but not used by JJ2000 decoder."
				);
				htKey = "UNKNOWN";
				break;
			default:
				htKey = "UNKNOWN";
				FacilityManager.getMsgLogger ( ).printmsg (
						MsgLogger.WARNING ,
						"Non recognized marker segment (0x" + Integer.toHexString ( marker ) + ") in tile-part header"
								+ " of tile " + tileIdx + " !"
				);
				break;
		}

		// Read marker segment length and create corresponding byte buffer
		final int markSegLen = ehs.readUnsignedShort ( );
		final byte[] buf = new byte[ markSegLen ];

		// Copy data (after re-insertion of marker segment length);
		buf[ 0 ] = ( byte ) ( ( markSegLen >> 8 ) & 0xFF );
		buf[ 1 ] = ( byte ) ( markSegLen & 0xFF );
		ehs.readFully ( buf , 2 , markSegLen - 2 );

		if ( ! "UNKNOWN".equals ( htKey ) ) {
			// Store array in hashTable
			this.ht.put ( htKey , buf );
		}
	}

	/**
	 * Retrieves and reads all marker segments found in the main header during
	 * the first pass.
	 */
	private void readFoundMainMarkSeg ( ) throws IOException {
		ByteArrayInputStream bais;

		// SIZ marker segment
		if ( 0 != ( nfMarkSeg & SIZ_FOUND ) ) {
			bais = new ByteArrayInputStream ( ( this.ht.get ( "SIZ" ) ) );
			this.readSIZ ( new DataInputStream ( bais ) );
		}

		// COM marker segments
		if ( 0 != ( nfMarkSeg & COM_FOUND ) ) {
			for ( int i = 0 ; i < this.nCOMMarkSeg ; i++ ) {
				bais = new ByteArrayInputStream ( ( this.ht.get ( "COM" + i ) ) );
				this.readCOM ( new DataInputStream ( bais ) , true , 0 , i );
			}
		}

		// CRG marker segment
		if ( 0 != ( nfMarkSeg & CRG_FOUND ) ) {
			bais = new ByteArrayInputStream ( ( this.ht.get ( "CRG" ) ) );
			this.readCRG ( new DataInputStream ( bais ) );
		}

		// COD marker segment
		if ( 0 != ( nfMarkSeg & COD_FOUND ) ) {
			bais = new ByteArrayInputStream ( ( this.ht.get ( "COD" ) ) );
			this.readCOD ( new DataInputStream ( bais ) , true , 0 , 0 );
		}

		// COC marker segments
		if ( 0 != ( nfMarkSeg & COC_FOUND ) ) {
			for ( int i = 0 ; i < this.nCOCMarkSeg ; i++ ) {
				bais = new ByteArrayInputStream ( ( this.ht.get ( "COC" + i ) ) );
				this.readCOC ( new DataInputStream ( bais ) , true , 0 , 0 );
			}
		}

		// RGN marker segment
		if ( 0 != ( nfMarkSeg & RGN_FOUND ) ) {
			for ( int i = 0 ; i < this.nRGNMarkSeg ; i++ ) {
				bais = new ByteArrayInputStream ( ( this.ht.get ( "RGN" + i ) ) );
				this.readRGN ( new DataInputStream ( bais ) , true , 0 , 0 );
			}
		}

		// QCD marker segment
		if ( 0 != ( nfMarkSeg & QCD_FOUND ) ) {
			bais = new ByteArrayInputStream ( ( this.ht.get ( "QCD" ) ) );
			this.readQCD ( new DataInputStream ( bais ) , true , 0 , 0 );
		}

		// QCC marker segments
		if ( 0 != ( nfMarkSeg & QCC_FOUND ) ) {
			for ( int i = 0 ; i < this.nQCCMarkSeg ; i++ ) {
				bais = new ByteArrayInputStream ( ( this.ht.get ( "QCC" + i ) ) );
				this.readQCC ( new DataInputStream ( bais ) , true , 0 , 0 );
			}
		}

		// POC marker segment
		if ( 0 != ( nfMarkSeg & POC_FOUND ) ) {
			bais = new ByteArrayInputStream ( ( this.ht.get ( "POC" ) ) );
			this.readPOC ( new DataInputStream ( bais ) , true , 0 , 0 );
		}

		// PPM marker segments
		if ( 0 != ( nfMarkSeg & PPM_FOUND ) ) {
			for ( int i = 0 ; i < this.nPPMMarkSeg ; i++ ) {
				bais = new ByteArrayInputStream ( ( this.ht.get ( "PPM" + i ) ) );
				this.readPPM ( new DataInputStream ( bais ) );
			}
		}

		// Reset the hashtable
		this.ht = null;
	}

	/**
	 * Retrieves and reads all marker segments previously found in the tile-part
	 * header.
	 *
	 * @param tileIdx The index of the current tile
	 * @param tpIdx   Index of the current tile-part
	 */
	public void readFoundTilePartMarkSeg ( final int tileIdx , final int tpIdx ) throws IOException {
		ByteArrayInputStream bais;

		// COD marker segment
		if ( 0 != ( nfMarkSeg & COD_FOUND ) ) {
			bais = new ByteArrayInputStream ( ( this.ht.get ( "COD" ) ) );
			this.readCOD ( new DataInputStream ( bais ) , false , tileIdx , tpIdx );
		}

		// COC marker segments
		if ( 0 != ( nfMarkSeg & COC_FOUND ) ) {
			for ( int i = 0 ; i < this.nCOCMarkSeg ; i++ ) {
				bais = new ByteArrayInputStream ( ( this.ht.get ( "COC" + i ) ) );
				this.readCOC ( new DataInputStream ( bais ) , false , tileIdx , tpIdx );
			}
		}

		// RGN marker segment
		if ( 0 != ( nfMarkSeg & RGN_FOUND ) ) {
			for ( int i = 0 ; i < this.nRGNMarkSeg ; i++ ) {
				bais = new ByteArrayInputStream ( ( this.ht.get ( "RGN" + i ) ) );
				this.readRGN ( new DataInputStream ( bais ) , false , tileIdx , tpIdx );
			}
		}

		// QCD marker segment
		if ( 0 != ( nfMarkSeg & QCD_FOUND ) ) {
			bais = new ByteArrayInputStream ( ( this.ht.get ( "QCD" ) ) );
			this.readQCD ( new DataInputStream ( bais ) , false , tileIdx , tpIdx );
		}

		// QCC marker segments
		if ( 0 != ( nfMarkSeg & QCC_FOUND ) ) {
			for ( int i = 0 ; i < this.nQCCMarkSeg ; i++ ) {
				bais = new ByteArrayInputStream ( ( this.ht.get ( "QCC" + i ) ) );
				this.readQCC ( new DataInputStream ( bais ) , false , tileIdx , tpIdx );
			}
		}

		// POC marker segment
		if ( 0 != ( nfMarkSeg & POC_FOUND ) ) {
			bais = new ByteArrayInputStream ( ( this.ht.get ( "POC" ) ) );
			this.readPOC ( new DataInputStream ( bais ) , false , tileIdx , tpIdx );
		}

		// COM marker segments
		if ( 0 != ( nfMarkSeg & COM_FOUND ) ) {
			for ( int i = 0 ; i < this.nCOMMarkSeg ; i++ ) {
				bais = new ByteArrayInputStream ( ( this.ht.get ( "COM" + i ) ) );
				this.readCOM ( new DataInputStream ( bais ) , false , tileIdx , i );
			}
		}

		// PPT marker segments
		if ( 0 != ( nfMarkSeg & PPT_FOUND ) ) {
			for ( int i = 0 ; i < this.nPPTMarkSeg[ tileIdx ][ tpIdx ] ; i++ ) {
				bais = new ByteArrayInputStream ( ( this.ht.get ( "PPT" + i ) ) );
				this.readPPT ( new DataInputStream ( bais ) , tileIdx , tpIdx );
			}
		}

		// Reset ht
		this.ht = null;
	}

	/**
	 * Return the DecoderSpecs instance filled when reading the headers
	 *
	 * @return The DecoderSpecs of the decoder
	 */
	public DecoderSpecs getDecoderSpecs ( ) {
		return this.decSpec;
	}

	/**
	 * Creates and returns the entropy decoder corresponding to the information
	 * read from the codestream header and with the special additional
	 * parameters from the parameter list.
	 *
	 * @param src The bit stream reader agent where to get code-block data from.
	 * @param pl  The parameter list containing parameters applicable to the
	 *            entropy decoder (other parameters can also be present).
	 * @return The entropy decoder
	 */
	public EntropyDecoder createEntropyDecoder ( final CodedCBlkDataSrcDec src , final ParameterList pl ) {
		final boolean doer;
		final boolean verber;
		final int mMax;

		// Check parameters
		pl.checkList ( EntropyDecoder.OPT_PREFIX , ParameterList.toNameArray ( EntropyDecoder.getParameterInfo ( ) ) );
		// Get error detection option
		doer = pl.getBooleanParameter ( "Cer" );
		// Get verbose error detection option
		verber = pl.getBooleanParameter ( "Cverber" );

		// Get maximum number of bit planes from m quit condition
		mMax = pl.getIntParameter ( "m_quit" );
		return new StdEntropyDecoder ( src , this.decSpec , doer , verber , mMax );

	}

	/**
	 * Creates and returns the EnumeratedColorSpaceMapper corresponding to the
	 * information read from the JP2 image file via the ColorSpace parameter.
	 *
	 * @param src   The bit stream reader agent where to get code-block data from.
	 * @param csMap provides color space information from the image file
	 * @return The color space mapping object
	 * @throws IOException         image access exception
	 * @throws ICCProfileException if image contains a bad dev.zontreck.harbinger.thirdparty.jj2000.j2k.icc profile
	 * @throws ColorSpaceException if image contains a bad dev.zontreck.harbinger.thirdparty.jj2000.j2k.colorspace box
	 */
	public BlkImgDataSrc createColorSpaceMapper ( final BlkImgDataSrc src , final ColorSpace csMap ) throws IOException,
			ICCProfileException, ColorSpaceException {
		return ColorSpaceMapper.createInstance ( src , csMap );
	}

	/**
	 * Creates and returns the ChannelDefinitonMapper which maps the input
	 * channels to the channel definition for the appropriate dev.zontreck.harbinger.thirdparty.jj2000.j2k.colorspace.
	 *
	 * @param src   The bit stream reader agent where to get code-block data from.
	 * @param csMap provides color space information from the image file
	 * @return The channel definition mapping object
	 * @throws IOException         image access exception
	 * @throws ColorSpaceException if image contains a bad dev.zontreck.harbinger.thirdparty.jj2000.j2k.colorspace box
	 */
	public BlkImgDataSrc createChannelDefinitionMapper ( final BlkImgDataSrc src , final ColorSpace csMap ) throws IOException,
			ColorSpaceException {
		return ChannelDefinitionMapper.createInstance ( src , csMap );
	}

	/**
	 * Creates and returns the PalettizedColorSpaceMapper which uses the input
	 * samples as indicies into a sample palette to construct the output.
	 *
	 * @param src   The bit stream reader agent where to get code-block data from.
	 * @param csMap provides color space information from the image file
	 * @return a PalettizedColorSpaceMapper instance
	 * @throws IOException         image access exception
	 * @throws ColorSpaceException if image contains a bad dev.zontreck.harbinger.thirdparty.jj2000.j2k.colorspace box
	 */
	public BlkImgDataSrc createPalettizedColorSpaceMapper ( final BlkImgDataSrc src , final ColorSpace csMap ) throws IOException,
			ColorSpaceException {
		return PalettizedColorSpaceMapper.createInstance ( src , csMap );
	}

	/**
	 * Creates and returns the Resampler which converts the input source to one
	 * in which all channels have the same number of samples. This is required
	 * for dev.zontreck.harbinger.thirdparty.jj2000.j2k.colorspace conversions.
	 *
	 * @param src   The bit stream reader agent where to get code-block data from.
	 * @param csMap provides color space information from the image file
	 * @return The resampled BlkImgDataSrc
	 * @throws IOException         image access exception
	 * @throws ColorSpaceException if image contains a bad dev.zontreck.harbinger.thirdparty.jj2000.j2k.colorspace box
	 */
	public BlkImgDataSrc createResampler ( final BlkImgDataSrc src , final ColorSpace csMap ) throws IOException, ColorSpaceException {
		return Resampler.createInstance ( src , csMap );
	}

	/**
	 * Creates and returns the ROIDeScaler corresponding to the information read
	 * from the codestream header and with the special additional parameters
	 * from the parameter list.
	 *
	 * @param src      The bit stream reader agent where to get code-block data from.
	 * @param pl       The parameter list containing parameters applicable to the
	 *                 entropy decoder (other parameters can also be present).
	 * @param decSpec2 The DecoderSpecs instance after any image manipulation.
	 * @return The ROI descaler.
	 */
	public ROIDeScaler createROIDeScaler ( final CBlkQuantDataSrcDec src , final ParameterList pl , final DecoderSpecs decSpec2 ) {
		return ROIDeScaler.createInstance ( src , pl , decSpec2 );
	}

	/**
	 * Method that resets members indicating which markers have already been
	 * found
	 */
	public void resetHeaderMarkers ( ) {
		// The found status of PLM remains since only PLM OR PLT allowed
		// Same goes for PPM and PPT
		this.nfMarkSeg = this.nfMarkSeg & ( HeaderDecoder.PLM_FOUND | HeaderDecoder.PPM_FOUND );
		this.nCOCMarkSeg = 0;
		this.nQCCMarkSeg = 0;
		this.nCOMMarkSeg = 0;
		this.nRGNMarkSeg = 0;
	}

	/**
	 * Print information about the current header.
	 *
	 * @return Information in a String
	 */
	@Override
	public String toString ( ) {
		return this.hdStr;
	}

	/**
	 * Return the number of tiles in the image
	 *
	 * @return The number of tiles
	 */
	public int getNumTiles ( ) {
		return this.nTiles;
	}

	/**
	 * Return the packed packet headers for a given tile.
	 *
	 * @return An input stream containing the packed packet headers for a
	 * particular tile
	 * @throws IOException If an I/O error occurs while reading from the encoder
	 *                     header stream
	 */
	public ByteArrayInputStream getPackedPktHead ( final int tile ) throws IOException {

		if ( null == pkdPktHeaders ) {
			int i, t;
			this.pkdPktHeaders = new ByteArrayOutputStream[ this.nTiles ];
			for ( i = this.nTiles - 1; 0 <= i ; i-- ) {
				this.pkdPktHeaders[ i ] = new ByteArrayOutputStream ( );
			}
			if ( 0 != nPPMMarkSeg ) {
				// If this is first time packed packet headers are requested,
				// create packed packet headers from Nppm and Ippm fields
				int nppm;
				final int nTileParts = this.tileOfTileParts.size ( );
				byte[] temp;
				final ByteArrayInputStream pph;
				final ByteArrayOutputStream allNppmIppm = new ByteArrayOutputStream ( );

				// Concatenate all Nppm and Ippm fields
				for ( i = 0; i < this.nPPMMarkSeg ; i++ ) {
					allNppmIppm.write ( this.pPMMarkerData[ i ] );
				}
				pph = new ByteArrayInputStream ( allNppmIppm.toByteArray ( ) );

				// Read all packed packet headers and concatenate for each
				// tile part
				for ( i = 0; i < nTileParts ; i++ ) {
					t = ( this.tileOfTileParts.elementAt ( i ) ).intValue ( );
					// get Nppm value
					nppm = ( pph.read ( ) << 24 ) | ( pph.read ( ) << 16 ) | ( pph.read ( ) << 8 ) | ( pph.read ( ) );

					temp = new byte[ nppm ];
					// get ippm field
					pph.read ( temp );
					this.pkdPktHeaders[ t ].write ( temp );
				}
			}
			else {
				int tp;
				// Write all packed packet headers to pkdPktHeaders
				for ( t = this.nTiles - 1; 0 <= t ; t-- ) {
					for ( tp = 0; tp < this.nTileParts[ t ] ; tp++ ) {
						for ( i = 0; i < this.nPPTMarkSeg[ t ][ tp ] ; i++ ) {
							this.pkdPktHeaders[ t ].write ( this.tilePartPkdPktHeaders[ t ][ tp ][ i ] );
						}
					}
				}
			}
		}

		return new ByteArrayInputStream ( this.pkdPktHeaders[ tile ].toByteArray ( ) );
	}

	/**
	 * Sets the tile of each tile part in order. This information is needed for
	 * identifying which packet header belongs to which tile when using the PPM
	 * marker.
	 *
	 * @param tile The tile number that the present tile part belongs to.
	 */
	public void setTileOfTileParts ( final int tile ) {
		if ( 0 != nPPMMarkSeg ) {
			this.tileOfTileParts.addElement ( Integer.valueOf ( tile ) );
		}
	}

	/**
	 * Returns the number of found marker segments in the current header.
	 *
	 * @return The number of marker segments found in the current header.
	 */
	public int getNumFoundMarkSeg ( ) {
		return this.nfMarkSeg;
	}
}
