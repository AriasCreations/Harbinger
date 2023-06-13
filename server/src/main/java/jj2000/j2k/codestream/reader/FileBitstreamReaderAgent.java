/*
 * CVS identifier:
 *
 * $Id: FileBitstreamReaderAgent.java,v 1.68 2002/07/19 12:34:38 grosbois Exp $
 *
 * Class:                   FileBitstreamReaderAgent
 *
 * Description:             Retrieve code-blocks codewords in the bit stream
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
package jj2000.j2k.codestream.reader;

import jj2000.j2k.quantization.dequantizer.*;
import jj2000.j2k.wavelet.synthesis.*;
import jj2000.j2k.entropy.decoder.*;
import jj2000.j2k.codestream.*;
import jj2000.j2k.decoder.*;
import jj2000.j2k.entropy.*;
import jj2000.j2k.image.*;
import jj2000.j2k.util.*;
import jj2000.j2k.io.*;
import jj2000.j2k.*;

import java.util.*;
import java.io.*;

/**
 * This class reads the bit stream (with the help of HeaderDecoder for tile
 * headers and PktDecoder for packets header and body) and retrives location of
 * all code-block's codewords.
 * 
 * <p>
 * Note: All tile-parts headers are read by the constructor whereas packets are
 * processed when decoding related tile (when setTile method is called).
 * 
 * <p>
 * In parsing mode, the reader simulates a virtual layer-resolution progressive
 * bit stream with the same truncation points in each code-block, whereas in
 * truncation mode, only the first bytes are taken into account (it behaves like
 * if it is a real truncated codestream).
 * 
 * @see HeaderDecoder
 * @see PktDecoder
 */
public class FileBitstreamReaderAgent extends BitstreamReaderAgent implements Markers, ProgressionType,
		StdEntropyCoderOptions
{

	/**
	 * Whether or not the last read Psot value was zero. Only the Psot in the
	 * last tile-part in the codestream can have such a value.
	 */
	private boolean isPsotEqualsZero = true;

	/** Reference to the PktDecoder instance */
	public PktDecoder pktDec;

	/** The RandomAccessIO where to get data from */
	private final RandomAccessIO in;

	/** Offset of the first packet in each tile-part in each tile */
	private final int[][] firstPackOff;

	/**
	 * Returns the number of tile-part found for a given tile
	 * 
	 * @param t
	 *            Tile index
	 * 
	 */
	public int getNumTileParts(final int t)
	{
		if (null == firstPackOff || null == firstPackOff[t])
		{
			throw new Error("Tile " + t + " not found in input codestream.");
		}
		return this.firstPackOff[t].length;
	}

	/**
	 * Number of bytes allocated to each tile. In parsing mode, this number is
	 * related to the tile length in the codestream whereas in truncation mode
	 * all the rate is affected to the first tiles.
	 */
	private final int[] nBytes;

	/** Whether or not to print information found in codestream */
	private boolean printInfo;

	/**
	 * Backup of the number of bytes allocated to each tile. This array is used
	 * to restore the number of bytes to read in each tile when the codestream
	 * is read several times (for instance when decoding an R,G,B image to three
	 * output files)
	 */
	private final int[] baknBytes;

	/** Length of each tile-part (written in Psot) */
	private final int[][] tilePartLen;

	/** Total length of each tile */
	private final int[] totTileLen;

	/** Total length of tiles' header */
	private final int[] totTileHeadLen;

	/** First tile part header length */
//	private int firstTilePartHeadLen;

	/** Total length of all tile parts in all tiles */
	private double totAllTileLen;

	/** Length of main header */
	private final int mainHeadLen;

	/** Length of main and tile-parts headers */
	private int headLen;

	/** Length of all tile-part headers */
	private final int[][] tilePartHeadLen;

	/** Length of each packet head found in the tile */
	private Vector<Integer> pktHL;

	/** True if truncation mode is used. False if parsing mode */
	private final boolean isTruncMode;

	/** The number of tile-parts that remain to read */
	private int remainingTileParts;

	/** The number of tile-parts read so far for each tile */
	private final int[] tilePartsRead;

	/** Thetotal number of tile-parts read so far */
	private int totTilePartsRead;

	/** The number of found tile-parts in each tile. */
	private final int[] tileParts;

	/** The current tile part being used */
	private int curTilePart;

	/**
	 * The number of the tile-parts found in the codestream after reading the
	 * tp'th tile-part of tile t
	 */
	private final int[][] tilePartNum;

	/** Whether or not a EOC marker has been found instead of a SOT */
	private boolean isEOCFound;

	/**
	 * Reference to the HeaderInfo instance (used when reading SOT marker
	 * segments)
	 */
	private final HeaderInfo hi;

	/**
	 * Array containing information for all the code-blocks:
	 * 
	 * <ul>
	 * <li>1st dim: component index.</li>
	 * <li>2nd dim: resolution level index.</li>
	 * <li>3rd dim: subband index.</li>
	 * <li>4th/5th dim: code-block index (vert. and horiz.).</li>
	 * </ul>
	 */
	private CBlkInfo[][][][][] cbI;

	/** Gets the reference to the CBlkInfo array */
	public CBlkInfo[][][][][] getCBlkInfo()
	{
		return this.cbI;
	}

	/** The maximum number of layers to decode for any code-block */
	private final int lQuit;

	/** Whether or not to use only first progression order */
	private boolean usePOCQuit;

	/**
	 * Reads all tiles headers and keep offset of their first packet. Finally it
	 * calls the rate allocation method.
	 * 
	 * @param hd
	 *            HeaderDecoder of the codestream.
	 * 
	 * @param ehs
	 *            The input stream where to read bit-stream.
	 * 
	 * @param decSpec
	 *            The decoder specifications
	 * 
	 * @param pl
	 *            The ParameterList instance created from the command-line
	 *            arguments.
	 * 
	 * @param cdstrInfo
	 *            Whether or not to print information found in codestream.
	 * 
	 * @see #allocateRate
	 */
	public FileBitstreamReaderAgent(final HeaderDecoder hd, final RandomAccessIO ehs, final DecoderSpecs decSpec, final ParameterList pl,
									final boolean cdstrInfo, final HeaderInfo hi) throws IOException
	{
		super(hd, decSpec);

		printInfo = cdstrInfo;
		this.hi = hi;
		String strInfo = "Codestream elements information in bytes (offset, total length, header length):\n\n";

		// Check whether quit conditiosn used
		this.usePOCQuit = pl.getBooleanParameter("poc_quit");

		// Get decoding rate
		final boolean rateInBytes;
		try
		{
			this.trate = pl.getFloatParameter("rate");
			if (-1 == trate)
			{
				this.trate = Float.MAX_VALUE;
			}
		}
		catch (final NumberFormatException e)
		{
			throw new Error("Invalid value in 'rate' option: " + pl.getParameter("rate"));
		}
		catch (final IllegalArgumentException e)
		{
			throw new Error("'rate' option is missing");
		}

		try
		{
			this.tnbytes = pl.getIntParameter("nbytes");
		}
		catch (final NumberFormatException e)
		{
			throw new Error("Invalid value in 'nbytes' option: " + pl.getParameter("nbytes"));
		}
		catch (final IllegalArgumentException e)
		{
			throw new Error("'nbytes' option is missing");
		}

		// Check that '-rate' and '-nbytes' are not used at the same time
		final ParameterList defaults = pl.getDefaultParameterList();
		rateInBytes = this.tnbytes != defaults.getFloatParameter("nbytes");

		if (rateInBytes)
		{
			this.trate = this.tnbytes * 8.0f / hd.getMaxCompImgWidth() / hd.getMaxCompImgHeight();
		}
		else
		{
			this.tnbytes = (int) (this.trate * hd.getMaxCompImgWidth() * hd.getMaxCompImgHeight()) / 8;
		}
		this.isTruncMode = !pl.getBooleanParameter("parsing");

		// Check if quit conditions are being used
		final int ncbQuit;
		try
		{
			ncbQuit = pl.getIntParameter("ncb_quit");
		}
		catch (final NumberFormatException e)
		{
			throw new Error("Invalid value in 'ncb_quit' option: " + pl.getParameter("ncb_quit"));
		}
		catch (final IllegalArgumentException e)
		{
			throw new Error("'ncb_quit' option is missing");
		}
		if (-1 != ncbQuit && !this.isTruncMode)
		{
			throw new Error("Cannot use -parsing and -ncb_quit condition at the same time.");
		}

		try
		{
			this.lQuit = pl.getIntParameter("l_quit");
		}
		catch (final NumberFormatException e)
		{
			throw new Error("Invalid value in 'l_quit' option: " + pl.getParameter("l_quit"));
		}
		catch (final IllegalArgumentException e)
		{
			throw new Error("'l_quit' option is missing");
		}

		// initializations
		this.in = ehs;
		this.pktDec = new PktDecoder(decSpec, hd, ehs, this, this.isTruncMode, ncbQuit);

		this.tileParts = new int[this.nt];
		this.totTileLen = new int[this.nt];
		this.tilePartLen = new int[this.nt][];
		this.tilePartNum = new int[this.nt][];
		this.firstPackOff = new int[this.nt][];
		this.tilePartsRead = new int[this.nt];
		this.totTileHeadLen = new int[this.nt];
		this.tilePartHeadLen = new int[this.nt][];
		this.nBytes = new int[this.nt];
		this.baknBytes = new int[this.nt];
		hd.nTileParts = new int[this.nt];

		int t = 0, pos, tp = 0 /*, tptot = 0 */;

		// Keeps main header's length, takes file format overhead into account
		final int cdstreamStart = hd.mainHeadOff; // Codestream offset in the file
		this.mainHeadLen = this.in.getPos() - cdstreamStart;
		this.headLen = this.mainHeadLen;

		// If ncb and lbody quit conditions are used, headers are not counted
		if (-1 == ncbQuit)
		{
			this.anbytes = this.mainHeadLen;
		}
		else
		{
			this.anbytes = 0;
		}

		strInfo += "Main header length    : " + cdstreamStart + ", " + this.mainHeadLen + ", " + this.mainHeadLen + "\n";

		// If cannot even read the first tile-part
		if (this.anbytes > this.tnbytes)
		{
			throw new Error("Requested bitrate is too small.");
		}

		// Read all tile-part headers from all tiles.
		int tilePartStart;
		boolean rateReached = false;
		final int mdl;
		this.totAllTileLen = 0;
		this.remainingTileParts = this.nt; // at least as many tile-parts as tiles
//		int maxTP = nt; // If maximum 1 tile part per tile specified

		try
		{
			while (0 != remainingTileParts)
			{

				tilePartStart = this.in.getPos();
				// Read tile-part header
				try
				{
					t = this.readTilePartHeader();
					if (this.isEOCFound)
					{ // Some tiles are missing but the
						// codestream is OK
						break;
					}
					tp = this.tilePartsRead[t];
					if (this.isPsotEqualsZero)
					{ // Psot may equals zero for the
						// last tile-part: it is assumed that this tile-part
						// contain all data until EOC
						this.tilePartLen[t][tp] = this.in.length() - 2 - tilePartStart;
					}
				}
				catch (final EOFException e)
				{
					this.firstPackOff[t][tp] = this.in.length();
					throw e;
				}

				pos = this.in.getPos();

				// In truncation mode, if target decoding rate is reached in
				// tile-part header, skips the tile-part and stop reading
				// unless the ncb and lbody quit condition is in use
				if (this.isTruncMode && -1 == ncbQuit)
				{
					if ((pos - cdstreamStart) > this.tnbytes)
					{
						this.firstPackOff[t][tp] = this.in.length();
						rateReached = true;
						break;
					}
				}

				// Set tile part position and header length
				this.firstPackOff[t][tp] = pos;
				this.tilePartHeadLen[t][tp] = (pos - tilePartStart);

				strInfo += "Tile-part " + tp + " of tile " + t + " : " + tilePartStart + ", " + this.tilePartLen[t][tp]
						+ ", " + this.tilePartHeadLen[t][tp] + "\n";

				// Update length counters
				this.totTileLen[t] += this.tilePartLen[t][tp];
				this.totTileHeadLen[t] += this.tilePartHeadLen[t][tp];
				this.totAllTileLen += this.tilePartLen[t][tp];
				if (this.isTruncMode)
				{
					if (this.anbytes + this.tilePartLen[t][tp] > this.tnbytes)
					{
						this.anbytes += this.tilePartHeadLen[t][tp];
						this.headLen += this.tilePartHeadLen[t][tp];
						rateReached = true;
						this.nBytes[t] += (this.tnbytes - this.anbytes);
						break;
					}
					this.anbytes += this.tilePartHeadLen[t][tp];
					this.headLen += this.tilePartHeadLen[t][tp];
					this.nBytes[t] += (this.tilePartLen[t][tp] - this.tilePartHeadLen[t][tp]);
				}
				else
				{
					if (this.anbytes + this.tilePartHeadLen[t][tp] > this.tnbytes)
					{
						break;
					}
					this.anbytes += this.tilePartHeadLen[t][tp];
					this.headLen += this.tilePartHeadLen[t][tp];
				}

				// If this is first tile-part, remember header length
//				if (tptot == 0)
//					firstTilePartHeadLen = tilePartHeadLen[t][tp];

				// Go to the beginning of next tile part
				this.tilePartsRead[t]++;
				this.in.seek(tilePartStart + this.tilePartLen[t][tp]);
				this.remainingTileParts--;
//				maxTP--;
//				tptot++;

				// If Psot of the current tile-part was equal to zero, it is
				// assumed that it contains all data until the EOC marker
				if (this.isPsotEqualsZero)
				{
					if (0 != remainingTileParts)
					{
						FacilityManager.getMsgLogger().printmsg(MsgLogger.WARNING,
								"Some tile-parts have not been found. The codestream may be corrupted.");
					}
					break;
				}
			}
		}
		catch (final EOFException e)
		{
			if (this.printInfo)
			{
				FacilityManager.getMsgLogger().printmsg(MsgLogger.INFO, strInfo);
			}
			FacilityManager.getMsgLogger().printmsg(MsgLogger.WARNING, "Codestream truncated in tile " + t);

			// Set specified rate to end of file if valid
			final int fileLen = this.in.length();
			if (fileLen < this.tnbytes)
			{
				this.tnbytes = fileLen;
				this.trate = this.tnbytes * 8.0f / hd.getMaxCompImgWidth() / hd.getMaxCompImgHeight();
			}

			// Bit-rate allocation
			if (!this.isTruncMode)
			{
				this.allocateRate();
			}

			// Update 'res' value once all tile-part headers are read
			if (null == pl.getParameter("res"))
			{
				this.targetRes = decSpec.dls.getMin();
			}
			else
			{
				try
				{
					this.targetRes = pl.getIntParameter("res");
					if (0 > targetRes)
					{
						throw new IllegalArgumentException("Specified negative resolution level index: " + this.targetRes);
					}
				}
				catch (final NumberFormatException f)
				{
					throw new IllegalArgumentException("Invalid resolution level index ('-res' option) "
							+ pl.getParameter("res"));
				}
			}

			// Verify reduction in resolution level
			mdl = decSpec.dls.getMin();
			if (this.targetRes > mdl)
			{
				FacilityManager.getMsgLogger().printmsg(
						MsgLogger.WARNING,
						"Specified resolution level (" + this.targetRes + ") is larger"
								+ " than the maximum value. Setting it to " + mdl + " (maximum value)");
				this.targetRes = mdl;
			}

			// Backup nBytes
			System.arraycopy(this.nBytes, 0, this.baknBytes, 0, this.nt);

			return;
		}
		this.remainingTileParts = 0;

		// Update 'res' value once all tile-part headers are read
		if (null == pl.getParameter("res"))
		{
			this.targetRes = decSpec.dls.getMin();
		}
		else
		{
			try
			{
				this.targetRes = pl.getIntParameter("res");
				if (0 > targetRes)
				{
					throw new IllegalArgumentException("Specified negative resolution level index: " + this.targetRes);
				}
			}
			catch (final NumberFormatException e)
			{
				throw new IllegalArgumentException("Invalid resolution level index ('-res' option) "
						+ pl.getParameter("res"));
			}
		}

		// Verify reduction in resolution level
		mdl = decSpec.dls.getMin();
		if (this.targetRes > mdl)
		{
			FacilityManager.getMsgLogger().printmsg(
					MsgLogger.WARNING,
					"Specified resolution level (" + this.targetRes + ") is larger"
							+ " than the maximum possible. Setting it to " + mdl + " (maximum possible)");
			this.targetRes = mdl;
		}

		if (this.printInfo)
		{
			FacilityManager.getMsgLogger().printmsg(MsgLogger.INFO, strInfo);
		}

		// Check presence of EOC marker is decoding rate not reached or if
		// this marker has not been found yet
		if (!this.isEOCFound && !this.isPsotEqualsZero)
		{
			try
			{
				if (!rateReached && !this.isPsotEqualsZero && EOC != in.readShort())
				{
					FacilityManager.getMsgLogger().printmsg(MsgLogger.WARNING,
							"EOC marker not found. Codestream is corrupted.");
				}
			}
			catch (final EOFException e)
			{
				FacilityManager.getMsgLogger().printmsg(MsgLogger.WARNING, "EOC marker is missing");
			}
		}

		// Bit-rate allocation
		if (!this.isTruncMode)
		{
			this.allocateRate();
		}
		else
		{
			// Take EOC into account if rate is not reached
			if (this.in.getPos() >= this.tnbytes)
				this.anbytes += 2;
		}

		// Backup nBytes
		for (int tIdx = 0; tIdx < this.nt; tIdx++)
		{
			this.baknBytes[tIdx] = this.nBytes[tIdx];
			if (this.printInfo)
			{
				FacilityManager.getMsgLogger()
						.println(hi.toStringTileHeader(tIdx, this.tilePartLen[tIdx].length), 2, 2);
			}
		}
	}

	/**
	 * Allocates output bit-rate for each tile in parsing mode: The allocator
	 * simulates the truncation of a virtual layer-resolution progressive
	 * codestream.
	 */
	private void allocateRate()
	{
		final int stopOff = this.tnbytes;

		// In parsing mode, the bitrate is allocated related to each tile's
		// length in the bit stream

		// EOC marker's length
		this.anbytes += 2;

		// If there are too few bytes to read the tile part headers throw an
		// error
		if (this.anbytes > stopOff)
		{
			throw new Error("Requested bitrate is too small for parsing");
		}

		// Calculate bitrate for each tile
		int rem = stopOff - this.anbytes;
		final int totnByte = rem;
		for (int t = this.nt - 1; 0 < t; t--)
		{
			rem -= this.nBytes[t] = (int) (totnByte * (this.totTileLen[t] / this.totAllTileLen));
		}
		this.nBytes[0] = rem;
	}

	/**
	 * Reads SOT marker segment of the tile-part header and calls related
	 * methods of the HeaderDecoder to read other markers segments. The
	 * tile-part header is entirely read when a SOD marker is encountered.
	 * 
	 * @return The tile number of the tile part that was read
	 */
	private int readTilePartHeader() throws IOException
	{
		final HeaderInfo.SOT ms = this.hi.getNewSOT();

		// SOT marker
		final short marker = this.in.readShort();
		if (SOT != marker)
		{
			if (EOC == marker)
			{
				this.isEOCFound = true;
				return -1;
			}
			throw new CorruptedCodestreamException("SOT tag not found in tile-part start");
		}
		this.isEOCFound = false;

		// Lsot (shall equals 10)
		final int lsot = this.in.readUnsignedShort();
		ms.lsot = lsot;
		if (10 != lsot)
			throw new CorruptedCodestreamException("Wrong length for SOT marker segment: " + lsot);

		// Isot
		final int tile = this.in.readUnsignedShort();
		ms.isot = tile;
		if (65534 < tile)
		{
			throw new CorruptedCodestreamException("Tile index too high in tile-part.");
		}

		// Psot
		final int psot = this.in.readInt();
		ms.psot = psot;
		this.isPsotEqualsZero = 0 == psot;
		if (0 > psot)
		{
			throw new NotImplementedError("Tile length larger than maximum supported");
		}
		// TPsot
		final int tilePart = this.in.read();
		ms.tpsot = tilePart;
		if (tilePart != this.tilePartsRead[tile] || 0 > tilePart || 254 < tilePart)
		{
			throw new CorruptedCodestreamException("Out of order tile-part");
		}
		// TNsot
		int nrOfTileParts = this.in.read();
		ms.tnsot = nrOfTileParts;
		this.hi.sot.put("t" + tile + "_tp" + tilePart, ms);
		if (0 == nrOfTileParts)
		{ // The number of tile-part is not specified in
			// this tile-part header.

			// Assumes that there will be another tile-part in the codestream
			// that will indicate the number of tile-parts for this tile)
			final int nExtraTp;
			if (0 == tileParts[tile] || this.tileParts[tile] == this.tilePartLen.length)
			{
				// Then there are two tile-parts (one is the current and the
				// other will indicate the number of tile-part for this tile)
				nExtraTp = 2;
				this.remainingTileParts += 1;
			}
			else
			{
				// There is already one scheduled extra tile-part. In this
				// case just add place for the current one
				nExtraTp = 1;
			}

			this.tileParts[tile] += nExtraTp;
			nrOfTileParts = this.tileParts[tile];
			FacilityManager.getMsgLogger().printmsg(
					MsgLogger.WARNING,
					"Header of tile-part " + tilePart + " of tile " + tile + ", does not indicate the total"
							+ " number of tile-parts. Assuming that there are " + nrOfTileParts
							+ " tile-parts for this tile.");

			// Increase and re-copy tilePartLen array
			int[] tmpA = this.tilePartLen[tile];
			this.tilePartLen[tile] = new int[nrOfTileParts];
			if (0 <= nrOfTileParts - nExtraTp)
				System.arraycopy(tmpA, 0, this.tilePartLen[tile], 0, nrOfTileParts - nExtraTp);

			// Increase and re-copy tilePartNum array
			tmpA = this.tilePartNum[tile];
			this.tilePartNum[tile] = new int[nrOfTileParts];
			if (0 <= nrOfTileParts - nExtraTp)
				System.arraycopy(tmpA, 0, this.tilePartNum[tile], 0, nrOfTileParts - nExtraTp);

			// Increase and re-copy firsPackOff array
			tmpA = this.firstPackOff[tile];
			this.firstPackOff[tile] = new int[nrOfTileParts];
			if (0 <= nrOfTileParts - nExtraTp)
				System.arraycopy(tmpA, 0, this.firstPackOff[tile], 0, nrOfTileParts - nExtraTp);

			// Increase and re-copy tilePartHeadLen array
			tmpA = this.tilePartHeadLen[tile];
			this.tilePartHeadLen[tile] = new int[nrOfTileParts];
			if (0 <= nrOfTileParts - nExtraTp)
				System.arraycopy(tmpA, 0, this.tilePartHeadLen[tile], 0, nrOfTileParts - nExtraTp);
		}
		else
		{ // The number of tile-parts is specified in the tile-part
			// header

			// Check if it is consistant with what was found in previous
			// tile-part headers

			if (0 == tileParts[tile])
			{ // First tile-part: OK
				this.remainingTileParts += nrOfTileParts - 1;
				this.tileParts[tile] = nrOfTileParts;
				this.tilePartLen[tile] = new int[nrOfTileParts];
				this.tilePartNum[tile] = new int[nrOfTileParts];
				this.firstPackOff[tile] = new int[nrOfTileParts];
				this.tilePartHeadLen[tile] = new int[nrOfTileParts];
			}
			else if (this.tileParts[tile] > nrOfTileParts)
			{
				// Already found more tile-parts than signaled here
				throw new CorruptedCodestreamException("Invalid number of tile-parts in tile " + tile + ": "
						+ nrOfTileParts);
			}
			else
			{ // Signaled number of tile-part fits with number of
				// previously found tile-parts
				this.remainingTileParts += nrOfTileParts - this.tileParts[tile];

				if (this.tileParts[tile] != nrOfTileParts)
				{

					// Increase and re-copy tilePartLen array
					int[] tmpA = this.tilePartLen[tile];
					this.tilePartLen[tile] = new int[nrOfTileParts];
					if (0 <= tileParts[tile] - 1)
						System.arraycopy(tmpA, 0, this.tilePartLen[tile], 0, this.tileParts[tile] - 1);

					// Increase and re-copy tilePartNum array
					tmpA = this.tilePartNum[tile];
					this.tilePartNum[tile] = new int[nrOfTileParts];
					if (0 <= tileParts[tile] - 1)
						System.arraycopy(tmpA, 0, this.tilePartNum[tile], 0, this.tileParts[tile] - 1);

					// Increase and re-copy firstPackOff array
					tmpA = this.firstPackOff[tile];
					this.firstPackOff[tile] = new int[nrOfTileParts];
					if (0 <= tileParts[tile] - 1)
						System.arraycopy(tmpA, 0, this.firstPackOff[tile], 0, this.tileParts[tile] - 1);

					// Increase and re-copy tilePartHeadLen array
					tmpA = this.tilePartHeadLen[tile];
					this.tilePartHeadLen[tile] = new int[nrOfTileParts];
					if (0 <= tileParts[tile] - 1)
						System.arraycopy(tmpA, 0, this.tilePartHeadLen[tile], 0, this.tileParts[tile] - 1);
				}
			}
		}

		// Other markers
		this.hd.resetHeaderMarkers();
		this.hd.nTileParts[tile] = nrOfTileParts;
		// Decode and store the tile-part header (i.e. until a SOD marker is
		// found)
		do
		{
			this.hd.extractTilePartMarkSeg(this.in.readShort(), this.in, tile, tilePart);
		} while (0 == (hd.getNumFoundMarkSeg() & HeaderDecoder.SOD_FOUND));

		// Read each marker segment previously found
		this.hd.readFoundTilePartMarkSeg(tile, tilePart);

		this.tilePartLen[tile][tilePart] = psot;

		this.tilePartNum[tile][tilePart] = this.totTilePartsRead;
		this.totTilePartsRead++;

		// Add to list of which tile each successive tile-part belongs.
		// This list is needed if there are PPM markers used
		this.hd.setTileOfTileParts(tile);

		return tile;
	}

	/**
	 * Reads packets of the current tile according to the
	 * layer-resolution-component-position progressiveness.
	 * 
	 * @param lys
	 *            Index of the first layer for each component and resolution.
	 * 
	 * @param lye
	 *            Index of the last layer.
	 * 
	 * @param ress
	 *            Index of the first resolution level.
	 * 
	 * @param rese
	 *            Index of the last resolution level.
	 * 
	 * @param comps
	 *            Index of the first component.
	 * 
	 * @param compe
	 *            Index of the last component.
	 * 
	 * @return True if rate has been reached.
	 */
	private boolean readLyResCompPos(final int[][] lys, final int lye, final int ress, final int rese, final int comps, final int compe) throws IOException
	{

		int minlys = 10000;
		for (int c = comps; c < compe; c++)
		{ // loop on components
			// Check if this component exists
			if (c >= this.mdl.length)
				continue;

			for (int r = ress; r < rese; r++)
			{// loop on resolution levels
				if (null != lys[c] && r < lys[c].length && lys[c][r] < minlys)
				{
					minlys = lys[c][r];
				}
			}
		}

		final int t = this.getTileIdx();
		int start;
		boolean status = false;
		int lastByte = this.firstPackOff[t][this.curTilePart] + this.tilePartLen[t][this.curTilePart] - 1 - this.tilePartHeadLen[t][this.curTilePart];
		final int numLayers = ((Integer) this.decSpec.nls.getTileDef(t)).intValue();
		int nPrec = 1;
		int hlen, plen;
		String strInfo = "Tile " + this.getTileIdx() + " (tile-part:" + this.curTilePart + "): offset, length, header length\n";
		final boolean pph = ((Boolean) this.decSpec.pphs.getTileDef(t)).booleanValue();
		for (int l = minlys; l < lye; l++)
		{ // loop on layers
			for (int r = ress; r < rese; r++)
			{ // loop on resolution levels
				for (int c = comps; c < compe; c++)
				{ // loop on components
					// Checks if component exists
					if (c >= this.mdl.length)
						continue;
					// Checks if resolution level exists
					if (r >= lys[c].length)
						continue;
					if (r > this.mdl[c])
						continue;
					// Checks if layer exists
					if (l < lys[c][r] || l >= numLayers)
						continue;

					nPrec = this.pktDec.getNumPrecinct(c, r);
					for (int p = 0; p < nPrec; p++)
					{ // loop on precincts
						start = this.in.getPos();

						// If packed packet headers are used, there is no need
						// to check that there are bytes enough to read header
						if (pph)
						{
							this.pktDec.readPktHead(l, r, c, p, this.cbI[c][r], this.nBytes);
						}

						// If we are about to read outside of tile-part,
						// skip to next tile-part
						if (start > lastByte && this.curTilePart < this.firstPackOff[t].length - 1)
						{
							this.curTilePart++;
							this.in.seek(this.firstPackOff[t][this.curTilePart]);
							lastByte = this.in.getPos() + this.tilePartLen[t][this.curTilePart] - 1 - this.tilePartHeadLen[t][this.curTilePart];
						}

						// Read SOP marker segment if necessary
						status = this.pktDec.readSOPMarker(this.nBytes, p, c, r);

						if (status)
						{
							if (this.printInfo)
							{
								FacilityManager.getMsgLogger().printmsg(MsgLogger.INFO, strInfo);
							}
							return true;
						}

						if (!pph)
						{
							status = this.pktDec.readPktHead(l, r, c, p, this.cbI[c][r], this.nBytes);
						}

						if (status)
						{
							if (this.printInfo)
							{
								FacilityManager.getMsgLogger().printmsg(MsgLogger.INFO, strInfo);
							}
							return true;
						}

						// Store packet's head length
						hlen = this.in.getPos() - start;
						this.pktHL.addElement(Integer.valueOf(hlen));

						// Reads packet's body
						status = this.pktDec.readPktBody(l, r, c, p, this.cbI[c][r], this.nBytes);
						plen = this.in.getPos() - start;
						strInfo += " Pkt l=" + l + ",r=" + r + ",c=" + c + ",p=" + p + ": " + start + ", " + plen
								+ ", " + hlen + "\n";

						if (status)
						{
							if (this.printInfo)
							{
								FacilityManager.getMsgLogger().printmsg(MsgLogger.INFO, strInfo);
							}
							return true;
						}

					} // end loop on precincts
				} // end loop on components
			} // end loop on resolution levels
		} // end loop on layers

		if (this.printInfo)
		{
			FacilityManager.getMsgLogger().printmsg(MsgLogger.INFO, strInfo);
		}
		return false; // Decoding rate was not reached
	}

	/**
	 * Reads packets of the current tile according to the
	 * resolution-layer-component-position progressiveness.
	 * 
	 * @param lys
	 *            Index of the first layer for each component and resolution.
	 * 
	 * @param lye
	 *            Index of the last layer.
	 * 
	 * @param ress
	 *            Index of the first resolution level.
	 * 
	 * @param rese
	 *            Index of the last resolution level.
	 * 
	 * @param comps
	 *            Index of the first component.
	 * 
	 * @param compe
	 *            Index of the last component.
	 * 
	 * @return True if rate has been reached.
	 */
	private boolean readResLyCompPos(final int[][] lys, final int lye, final int ress, final int rese, final int comps, final int compe) throws IOException
	{

		final int t = this.getTileIdx(); // Current tile index
		boolean status = false; // True if decoding rate is reached when
		int lastByte = this.firstPackOff[t][this.curTilePart] + this.tilePartLen[t][this.curTilePart] - 1 - this.tilePartHeadLen[t][this.curTilePart];
		int minlys = 10000;
		for (int c = comps; c < compe; c++)
		{ // loop on components
			// Check if this component exists
			if (c >= this.mdl.length)
				continue;

			for (int r = ress; r < rese; r++)
			{// loop on resolution levels
				if (r > this.mdl[c])
					continue;
				if (null != lys[c] && r < lys[c].length && lys[c][r] < minlys)
				{
					minlys = lys[c][r];
				}
			}
		}

		String strInfo = "Tile " + this.getTileIdx() + " (tile-part:" + this.curTilePart + "): offset, length, header length\n";
		final int numLayers = ((Integer) this.decSpec.nls.getTileDef(t)).intValue();
		final boolean pph = ((Boolean) this.decSpec.pphs.getTileDef(t)).booleanValue();
		int nPrec = 1;
		int start;
		int hlen, plen;
		for (int r = ress; r < rese; r++)
		{ // loop on resolution levels
			for (int l = minlys; l < lye; l++)
			{ // loop on layers
				for (int c = comps; c < compe; c++)
				{ // loop on components
					// Checks if component exists
					if (c >= this.mdl.length)
						continue;
					// Checks if resolution level exists
					if (r > this.mdl[c])
						continue;
					if (r >= lys[c].length)
						continue;
					// Checks if layer exists
					if (l < lys[c][r] || l >= numLayers)
						continue;

					nPrec = this.pktDec.getNumPrecinct(c, r);

					for (int p = 0; p < nPrec; p++)
					{ // loop on precincts
						start = this.in.getPos();

						// If packed packet headers are used, there is no need
						// to check that there are bytes enough to read header
						if (pph)
						{
							this.pktDec.readPktHead(l, r, c, p, this.cbI[c][r], this.nBytes);
						}

						// If we are about to read outside of tile-part,
						// skip to next tile-part
						if (start > lastByte && this.curTilePart < this.firstPackOff[t].length - 1)
						{
							this.curTilePart++;
							this.in.seek(this.firstPackOff[t][this.curTilePart]);
							lastByte = this.in.getPos() + this.tilePartLen[t][this.curTilePart] - 1 - this.tilePartHeadLen[t][this.curTilePart];
						}

						// Read SOP marker segment if necessary
						status = this.pktDec.readSOPMarker(this.nBytes, p, c, r);

						if (status)
						{
							if (this.printInfo)
							{
								FacilityManager.getMsgLogger().printmsg(MsgLogger.INFO, strInfo);
							}
							return true;
						}

						if (!pph)
						{
							status = this.pktDec.readPktHead(l, r, c, p, this.cbI[c][r], this.nBytes);
						}

						if (status)
						{
							if (this.printInfo)
							{
								FacilityManager.getMsgLogger().printmsg(MsgLogger.INFO, strInfo);
							}
							// Output rate of EOF reached
							return true;
						}

						// Store packet's head length
						hlen = this.in.getPos() - start;
						this.pktHL.addElement(Integer.valueOf(hlen));

						// Reads packet's body
						status = this.pktDec.readPktBody(l, r, c, p, this.cbI[c][r], this.nBytes);
						plen = this.in.getPos() - start;
						strInfo += " Pkt l=" + l + ",r=" + r + ",c=" + c + ",p=" + p + ": " + start + ", " + plen
								+ ", " + hlen + "\n";

						if (status)
						{
							if (this.printInfo)
							{
								FacilityManager.getMsgLogger().printmsg(MsgLogger.INFO, strInfo);
							}
							// Output rate or EOF reached
							return true;
						}

					} // end loop on precincts
				} // end loop on components
			} // end loop on layers
		} // end loop on resolution levels

		if (this.printInfo)
		{
			FacilityManager.getMsgLogger().printmsg(MsgLogger.INFO, strInfo);
		}
		return false; // Decoding rate was not reached
	}

	/**
	 * Reads packets of the current tile according to the
	 * resolution-position-component-layer progressiveness.
	 * 
	 * @param lys
	 *            Index of the first layer for each component and resolution.
	 * 
	 * @param lye
	 *            Index of the last layer.
	 * 
	 * @param ress
	 *            Index of the first resolution level.
	 * 
	 * @param rese
	 *            Index of the last resolution level.
	 * 
	 * @param comps
	 *            Index of the first component.
	 * 
	 * @param compe
	 *            Index of the last component.
	 * 
	 * @return True if rate has been reached.
	 */
	private boolean readResPosCompLy(final int[][] lys, final int lye, final int ress, final int rese, final int comps, final int compe) throws IOException
	{
		// Computes current tile offset in the reference grid

		final Coord nTiles = this.getNumTiles(null);
		final Coord tileI = this.getTile(null);
		final int x0siz = this.hd.getImgULX();
		final int y0siz = this.hd.getImgULY();
		final int xsiz = x0siz + this.hd.getImgWidth();
		final int ysiz = y0siz + this.hd.getImgHeight();
		final int xt0siz = this.getTilePartULX();
		final int yt0siz = this.getTilePartULY();
		final int xtsiz = this.getNomTileWidth();
		final int ytsiz = this.getNomTileHeight();
		final int tx0 = (0 == tileI.x) ? x0siz : xt0siz + tileI.x * xtsiz;
		final int ty0 = (0 == tileI.y) ? y0siz : yt0siz + tileI.y * ytsiz;
		final int tx1 = (tileI.x != nTiles.x - 1) ? xt0siz + (tileI.x + 1) * xtsiz : xsiz;
		final int ty1 = (tileI.y != nTiles.y - 1) ? yt0siz + (tileI.y + 1) * ytsiz : ysiz;

		// Get precinct information (number,distance between two consecutive
		// precincts in the reference grid) in each component and resolution
		// level
		final int t = this.getTileIdx(); // Current tile index
		PrecInfo prec; // temporary variable
		int p; // Current precinct index
		int gcd_x = 0; // Horiz. distance between 2 precincts in the ref. grid
		int gcd_y = 0; // Vert. distance between 2 precincts in the ref. grid
		int nPrec = 0; // Total number of found precincts
		final int[][] nextPrec = new int[compe][]; // Next precinct index in each
		// component and resolution level
		int minlys = 100000; // minimum layer start index of each component
		int minx = tx1; // Horiz. offset of the second precinct in the
		// reference grid
		int miny = ty1; // Vert. offset of the second precinct in the
		// reference grid.
		int maxx = tx0; // Max. horiz. offset of precincts in the ref. grid
		int maxy = ty0; // Max. vert. offset of precincts in the ref. grid
		for (int c = comps; c < compe; c++)
		{ // components
			for (int r = ress; r < rese; r++)
			{ // resolution levels
				if (c >= this.mdl.length)
					continue;
				if (r > this.mdl[c])
					continue;
				nextPrec[c] = new int[this.mdl[c] + 1];
				if (null != lys[c] && r < lys[c].length && lys[c][r] < minlys)
				{
					minlys = lys[c][r];
				}
				p = this.pktDec.getNumPrecinct(c, r) - 1;
				for (; 0 <= p; p--)
				{
					prec = this.pktDec.getPrecInfo(c, r, p);
					if (prec.rgulx != tx0)
					{
						if (prec.rgulx < minx)
							minx = prec.rgulx;
						if (prec.rgulx > maxx)
							maxx = prec.rgulx;
					}
					if (prec.rguly != ty0)
					{
						if (prec.rguly < miny)
							miny = prec.rguly;
						if (prec.rguly > maxy)
							maxy = prec.rguly;
					}

					if (0 == nPrec)
					{
						gcd_x = prec.rgw;
						gcd_y = prec.rgh;
					}
					else
					{
						gcd_x = MathUtil.gcd(gcd_x, prec.rgw);
						gcd_y = MathUtil.gcd(gcd_y, prec.rgh);
					}
					nPrec++;
				} // precincts
			} // resolution levels
		} // components

		if (0 == nPrec)
		{
			throw new Error("Image cannot have no precinct");
		}

		final int pyend = (maxy - miny) / gcd_y + 1;
		final int pxend = (maxx - minx) / gcd_x + 1;
		int x, y;
		int hlen, plen;
		int start;
		boolean status = false;
		int lastByte = this.firstPackOff[t][this.curTilePart] + this.tilePartLen[t][this.curTilePart] - 1 - this.tilePartHeadLen[t][this.curTilePart];
		final int numLayers = ((Integer) this.decSpec.nls.getTileDef(t)).intValue();
		String strInfo = "Tile " + this.getTileIdx() + " (tile-part:" + this.curTilePart + "): offset, length, header length\n";
		final boolean pph = ((Boolean) this.decSpec.pphs.getTileDef(t)).booleanValue();
		for (int r = ress; r < rese; r++)
		{ // loop on resolution levels
			y = ty0;
			x = tx0;
			for (int py = 0; py <= pyend; py++)
			{ // Vertical precincts
				for (int px = 0; px <= pxend; px++)
				{ // Horiz. precincts
					for (int c = comps; c < compe; c++)
					{ // Components
						if (c >= this.mdl.length)
							continue;
						if (r > this.mdl[c])
							continue;
						if (nextPrec[c][r] >= this.pktDec.getNumPrecinct(c, r))
						{
							continue;
						}
						prec = this.pktDec.getPrecInfo(c, r, nextPrec[c][r]);
						if ((prec.rgulx != x) || (prec.rguly != y))
						{
							continue;
						}
						for (int l = minlys; l < lye; l++)
						{ // layers
							if (r >= lys[c].length)
								continue;
							if (l < lys[c][r] || l >= numLayers)
								continue;

							start = this.in.getPos();

							// If packed packet headers are used, there is no
							// need to check that there are bytes enough to
							// read header
							if (pph)
							{
								this.pktDec.readPktHead(l, r, c, nextPrec[c][r], this.cbI[c][r], this.nBytes);
							}

							// If we are about to read outside of tile-part,
							// skip to next tile-part
							if (start > lastByte && this.curTilePart < this.firstPackOff[t].length - 1)
							{
								this.curTilePart++;
								this.in.seek(this.firstPackOff[t][this.curTilePart]);
								lastByte = this.in.getPos() + this.tilePartLen[t][this.curTilePart] - 1
										- this.tilePartHeadLen[t][this.curTilePart];
							}

							// Read SOP marker segment if necessary
							status = this.pktDec.readSOPMarker(this.nBytes, nextPrec[c][r], c, r);

							if (status)
							{
								if (this.printInfo)
								{
									FacilityManager.getMsgLogger().printmsg(MsgLogger.INFO, strInfo);
								}
								return true;
							}

							if (!pph)
							{
								status = this.pktDec.readPktHead(l, r, c, nextPrec[c][r], this.cbI[c][r], this.nBytes);
							}

							if (status)
							{
								if (this.printInfo)
								{
									FacilityManager.getMsgLogger().printmsg(MsgLogger.INFO, strInfo);
								}
								return true;
							}

							// Store packet's head length
							hlen = this.in.getPos() - start;
							this.pktHL.addElement(Integer.valueOf(hlen));

							// Reads packet's body
							status = this.pktDec.readPktBody(l, r, c, nextPrec[c][r], this.cbI[c][r], this.nBytes);
							plen = this.in.getPos() - start;
							strInfo += " Pkt l=" + l + ",r=" + r + ",c=" + c + ",p=" + nextPrec[c][r] + ": " + start
									+ ", " + plen + ", " + hlen + "\n";

							if (status)
							{
								if (this.printInfo)
								{
									FacilityManager.getMsgLogger().printmsg(MsgLogger.INFO, strInfo);
								}
								return true;
							}
						} // layers
						nextPrec[c][r]++;
					} // Components
					if (px != pxend)
					{
						x = minx + px * gcd_x;
					}
					else
					{
						x = tx0;
					}
				} // Horizontal precincts
				if (py != pyend)
				{
					y = miny + py * gcd_y;
				}
				else
				{
					y = ty0;
				}
			} // Vertical precincts
		} // end loop on resolution levels

		if (this.printInfo)
		{
			FacilityManager.getMsgLogger().printmsg(MsgLogger.INFO, strInfo);
		}
		return false; // Decoding rate was not reached
	}

	/**
	 * Reads packets of the current tile according to the
	 * position-component-resolution-layer progressiveness.
	 * 
	 * @param lys
	 *            Index of the first layer for each component and resolution.
	 * 
	 * @param lye
	 *            Index of the last layer.
	 * 
	 * @param ress
	 *            Index of the first resolution level.
	 * 
	 * @param rese
	 *            Index of the last resolution level.
	 * 
	 * @param comps
	 *            Index of the first component.
	 * 
	 * @param compe
	 *            Index of the last component.
	 * 
	 * @return True if rate has been reached.
	 */
	private boolean readPosCompResLy(final int[][] lys, final int lye, final int ress, final int rese, final int comps, final int compe) throws IOException
	{
		final Coord nTiles = this.getNumTiles(null);
		final Coord tileI = this.getTile(null);
		final int x0siz = this.hd.getImgULX();
		final int y0siz = this.hd.getImgULY();
		final int xsiz = x0siz + this.hd.getImgWidth();
		final int ysiz = y0siz + this.hd.getImgHeight();
		final int xt0siz = this.getTilePartULX();
		final int yt0siz = this.getTilePartULY();
		final int xtsiz = this.getNomTileWidth();
		final int ytsiz = this.getNomTileHeight();
		final int tx0 = (0 == tileI.x) ? x0siz : xt0siz + tileI.x * xtsiz;
		final int ty0 = (0 == tileI.y) ? y0siz : yt0siz + tileI.y * ytsiz;
		final int tx1 = (tileI.x != nTiles.x - 1) ? xt0siz + (tileI.x + 1) * xtsiz : xsiz;
		final int ty1 = (tileI.y != nTiles.y - 1) ? yt0siz + (tileI.y + 1) * ytsiz : ysiz;

		// Get precinct information (number,distance between two consecutive
		// precincts in the reference grid) in each component and resolution
		// level
		final int t = this.getTileIdx(); // Current tile index
		PrecInfo prec; // temporary variable
		int p; // Current precinct index
		int gcd_x = 0; // Horiz. distance between 2 precincts in the ref. grid
		int gcd_y = 0; // Vert. distance between 2 precincts in the ref. grid
		int nPrec = 0; // Total number of found precincts
		final int[][] nextPrec = new int[compe][]; // Next precinct index in each
		// component and resolution level
		int minlys = 100000; // minimum layer start index of each component
		int minx = tx1; // Horiz. offset of the second precinct in the
		// reference grid
		int miny = ty1; // Vert. offset of the second precinct in the
		// reference grid.
		int maxx = tx0; // Max. horiz. offset of precincts in the ref. grid
		int maxy = ty0; // Max. vert. offset of precincts in the ref. grid
		for (int c = comps; c < compe; c++)
		{ // components
			for (int r = ress; r < rese; r++)
			{ // resolution levels
				if (c >= this.mdl.length)
					continue;
				if (r > this.mdl[c])
					continue;
				nextPrec[c] = new int[this.mdl[c] + 1];
				if (null != lys[c] && r < lys[c].length && lys[c][r] < minlys)
				{
					minlys = lys[c][r];
				}
				p = this.pktDec.getNumPrecinct(c, r) - 1;
				for (; 0 <= p; p--)
				{
					prec = this.pktDec.getPrecInfo(c, r, p);
					if (prec.rgulx != tx0)
					{
						if (prec.rgulx < minx)
							minx = prec.rgulx;
						if (prec.rgulx > maxx)
							maxx = prec.rgulx;
					}
					if (prec.rguly != ty0)
					{
						if (prec.rguly < miny)
							miny = prec.rguly;
						if (prec.rguly > maxy)
							maxy = prec.rguly;
					}

					if (0 == nPrec)
					{
						gcd_x = prec.rgw;
						gcd_y = prec.rgh;
					}
					else
					{
						gcd_x = MathUtil.gcd(gcd_x, prec.rgw);
						gcd_y = MathUtil.gcd(gcd_y, prec.rgh);
					}
					nPrec++;
				} // precincts
			} // resolution levels
		} // components

		if (0 == nPrec)
		{
			throw new Error("Image cannot have no precinct");
		}

		final int pyend = (maxy - miny) / gcd_y + 1;
		final int pxend = (maxx - minx) / gcd_x + 1;
		int hlen, plen;
		int start;
		boolean status = false;
		int lastByte = this.firstPackOff[t][this.curTilePart] + this.tilePartLen[t][this.curTilePart] - 1 - this.tilePartHeadLen[t][this.curTilePart];
		final int numLayers = ((Integer) this.decSpec.nls.getTileDef(t)).intValue();
		String strInfo = "Tile " + this.getTileIdx() + " (tile-part:" + this.curTilePart + "): offset, length, header length\n";
		final boolean pph = ((Boolean) this.decSpec.pphs.getTileDef(t)).booleanValue();

		int y = ty0;
		int x = tx0;
		for (int py = 0; py <= pyend; py++)
		{ // Vertical precincts
			for (int px = 0; px <= pxend; px++)
			{ // Horiz. precincts
				for (int c = comps; c < compe; c++)
				{ // Components
					if (c >= this.mdl.length)
						continue;
					for (int r = ress; r < rese; r++)
					{ // Resolution levels
						if (r > this.mdl[c])
							continue;
						if (nextPrec[c][r] >= this.pktDec.getNumPrecinct(c, r))
						{
							continue;
						}
						prec = this.pktDec.getPrecInfo(c, r, nextPrec[c][r]);
						if ((prec.rgulx != x) || (prec.rguly != y))
						{
							continue;
						}
						for (int l = minlys; l < lye; l++)
						{ // Layers
							if (r >= lys[c].length)
								continue;
							if (l < lys[c][r] || l >= numLayers)
								continue;

							start = this.in.getPos();

							// If packed packet headers are used, there is no
							// need to check that there are bytes enough to
							// read header
							if (pph)
							{
								this.pktDec.readPktHead(l, r, c, nextPrec[c][r], this.cbI[c][r], this.nBytes);
							}

							// If we are about to read outside of tile-part,
							// skip to next tile-part
							if (start > lastByte && this.curTilePart < this.firstPackOff[t].length - 1)
							{
								this.curTilePart++;
								this.in.seek(this.firstPackOff[t][this.curTilePart]);
								lastByte = this.in.getPos() + this.tilePartLen[t][this.curTilePart] - 1
										- this.tilePartHeadLen[t][this.curTilePart];
							}

							// Read SOP marker segment if necessary
							status = this.pktDec.readSOPMarker(this.nBytes, nextPrec[c][r], c, r);

							if (status)
							{
								if (this.printInfo)
								{
									FacilityManager.getMsgLogger().printmsg(MsgLogger.INFO, strInfo);
								}
								return true;
							}

							if (!pph)
							{
								status = this.pktDec.readPktHead(l, r, c, nextPrec[c][r], this.cbI[c][r], this.nBytes);
							}

							if (status)
							{
								if (this.printInfo)
								{
									FacilityManager.getMsgLogger().printmsg(MsgLogger.INFO, strInfo);
								}
								return true;
							}

							// Store packet's head length
							hlen = this.in.getPos() - start;
							this.pktHL.addElement(Integer.valueOf(hlen));

							// Reads packet's body
							status = this.pktDec.readPktBody(l, r, c, nextPrec[c][r], this.cbI[c][r], this.nBytes);
							plen = this.in.getPos() - start;
							strInfo += " Pkt l=" + l + ",r=" + r + ",c=" + c + ",p=" + nextPrec[c][r] + ": " + start
									+ ", " + plen + ", " + hlen + "\n";

							if (status)
							{
								if (this.printInfo)
								{
									FacilityManager.getMsgLogger().printmsg(MsgLogger.INFO, strInfo);
								}
								return true;
							}

						} // layers
						nextPrec[c][r]++;
					} // Resolution levels
				} // Components
				if (px != pxend)
				{
					x = minx + px * gcd_x;
				}
				else
				{
					x = tx0;
				}
			} // Horizontal precincts
			if (py != pyend)
			{
				y = miny + py * gcd_y;
			}
			else
			{
				y = ty0;
			}
		} // Vertical precincts

		if (this.printInfo)
		{
			FacilityManager.getMsgLogger().printmsg(MsgLogger.INFO, strInfo);
		}
		return false; // Decoding rate was not reached
	}

	/**
	 * Reads packets of the current tile according to the
	 * component-position-resolution-layer progressiveness.
	 * 
	 * @param lys
	 *            Index of the first layer for each component and resolution.
	 * 
	 * @param lye
	 *            Index of the last layer.
	 * 
	 * @param ress
	 *            Index of the first resolution level.
	 * 
	 * @param rese
	 *            Index of the last resolution level.
	 * 
	 * @param comps
	 *            Index of the first component.
	 * 
	 * @param compe
	 *            Index of the last component.
	 * 
	 * @return True if rate has been reached.
	 */
	private boolean readCompPosResLy(final int[][] lys, final int lye, final int ress, final int rese, final int comps, final int compe) throws IOException
	{
		final Coord nTiles = this.getNumTiles(null);
		final Coord tileI = this.getTile(null);
		final int x0siz = this.hd.getImgULX();
		final int y0siz = this.hd.getImgULY();
		final int xsiz = x0siz + this.hd.getImgWidth();
		final int ysiz = y0siz + this.hd.getImgHeight();
		final int xt0siz = this.getTilePartULX();
		final int yt0siz = this.getTilePartULY();
		final int xtsiz = this.getNomTileWidth();
		final int ytsiz = this.getNomTileHeight();
		final int tx0 = (0 == tileI.x) ? x0siz : xt0siz + tileI.x * xtsiz;
		final int ty0 = (0 == tileI.y) ? y0siz : yt0siz + tileI.y * ytsiz;
		final int tx1 = (tileI.x != nTiles.x - 1) ? xt0siz + (tileI.x + 1) * xtsiz : xsiz;
		final int ty1 = (tileI.y != nTiles.y - 1) ? yt0siz + (tileI.y + 1) * ytsiz : ysiz;

		// Get precinct information (number,distance between two consecutive
		// precincts in the reference grid) in each component and resolution
		// level
		final int t = this.getTileIdx(); // Current tile index
		PrecInfo prec; // temporary variable
		int p; // Current precinct index
		int gcd_x = 0; // Horiz. distance between 2 precincts in the ref. grid
		int gcd_y = 0; // Vert. distance between 2 precincts in the ref. grid
		int nPrec = 0; // Total number of found precincts
		final int[][] nextPrec = new int[compe][]; // Next precinct index in each
		// component and resolution level
		int minlys = 100000; // minimum layer start index of each component
		int minx = tx1; // Horiz. offset of the second precinct in the
		// reference grid
		int miny = ty1; // Vert. offset of the second precinct in the
		// reference grid.
		int maxx = tx0; // Max. horiz. offset of precincts in the ref. grid
		int maxy = ty0; // Max. vert. offset of precincts in the ref. grid
		for (int c = comps; c < compe; c++)
		{ // components
			for (int r = ress; r < rese; r++)
			{ // resolution levels
				if (c >= this.mdl.length)
					continue;
				if (r > this.mdl[c])
					continue;
				nextPrec[c] = new int[this.mdl[c] + 1];
				if (null != lys[c] && r < lys[c].length && lys[c][r] < minlys)
				{
					minlys = lys[c][r];
				}
				p = this.pktDec.getNumPrecinct(c, r) - 1;
				for (; 0 <= p; p--)
				{
					prec = this.pktDec.getPrecInfo(c, r, p);
					if (prec.rgulx != tx0)
					{
						if (prec.rgulx < minx)
							minx = prec.rgulx;
						if (prec.rgulx > maxx)
							maxx = prec.rgulx;
					}
					if (prec.rguly != ty0)
					{
						if (prec.rguly < miny)
							miny = prec.rguly;
						if (prec.rguly > maxy)
							maxy = prec.rguly;
					}

					if (0 == nPrec)
					{
						gcd_x = prec.rgw;
						gcd_y = prec.rgh;
					}
					else
					{
						gcd_x = MathUtil.gcd(gcd_x, prec.rgw);
						gcd_y = MathUtil.gcd(gcd_y, prec.rgh);
					}
					nPrec++;
				} // precincts
			} // resolution levels
		} // components

		if (0 == nPrec)
		{
			throw new Error("Image cannot have no precinct");
		}

		final int pyend = (maxy - miny) / gcd_y + 1;
		final int pxend = (maxx - minx) / gcd_x + 1;
		int hlen, plen;
		int start;
		boolean status = false;
		int lastByte = this.firstPackOff[t][this.curTilePart] + this.tilePartLen[t][this.curTilePart] - 1 - this.tilePartHeadLen[t][this.curTilePart];
		// int numLayers = ((Integer) decSpec.nls.getTileDef(t)).intValue();
		String strInfo = "Tile " + this.getTileIdx() + " (tile-part:" + this.curTilePart + "): offset, length, header length\n";
		final boolean pph = ((Boolean) this.decSpec.pphs.getTileDef(t)).booleanValue();

		int x, y;
		for (int c = comps; c < compe; c++)
		{ // components
			if (c >= this.mdl.length)
				continue;
			y = ty0;
			x = tx0;
			for (int py = 0; py <= pyend; py++)
			{ // Vertical precincts
				for (int px = 0; px <= pxend; px++)
				{ // Horiz. precincts
					for (int r = ress; r < rese; r++)
					{ // Resolution levels
						if (r > this.mdl[c])
							continue;
						if (nextPrec[c][r] >= this.pktDec.getNumPrecinct(c, r))
						{
							continue;
						}
						prec = this.pktDec.getPrecInfo(c, r, nextPrec[c][r]);
						if ((prec.rgulx != x) || (prec.rguly != y))
						{
							continue;
						}

						for (int l = minlys; l < lye; l++)
						{ // Layers
							if (r >= lys[c].length)
								continue;
							if (l < lys[c][r])
								continue;

							start = this.in.getPos();

							// If packed packet headers are used, there is no
							// need to check that there are bytes enough to
							// read header
							if (pph)
							{
								this.pktDec.readPktHead(l, r, c, nextPrec[c][r], this.cbI[c][r], this.nBytes);
							}

							// If we are about to read outside of tile-part,
							// skip to next tile-part
							if (start > lastByte && this.curTilePart < this.firstPackOff[t].length - 1)
							{
								this.curTilePart++;
								this.in.seek(this.firstPackOff[t][this.curTilePart]);
								lastByte = this.in.getPos() + this.tilePartLen[t][this.curTilePart] - 1
										- this.tilePartHeadLen[t][this.curTilePart];
							}

							// Read SOP marker segment if necessary
							status = this.pktDec.readSOPMarker(this.nBytes, nextPrec[c][r], c, r);

							if (status)
							{
								if (this.printInfo)
								{
									FacilityManager.getMsgLogger().printmsg(MsgLogger.INFO, strInfo);
								}
								return true;
							}

							if (!pph)
							{
								status = this.pktDec.readPktHead(l, r, c, nextPrec[c][r], this.cbI[c][r], this.nBytes);
							}

							if (status)
							{
								if (this.printInfo)
								{
									FacilityManager.getMsgLogger().printmsg(MsgLogger.INFO, strInfo);
								}
								return true;
							}

							// Store packet's head length
							hlen = this.in.getPos() - start;
							this.pktHL.addElement(Integer.valueOf(hlen));

							// Reads packet's body
							status = this.pktDec.readPktBody(l, r, c, nextPrec[c][r], this.cbI[c][r], this.nBytes);
							plen = this.in.getPos() - start;
							strInfo += " Pkt l=" + l + ",r=" + r + ",c=" + c + ",p=" + nextPrec[c][r] + ": " + start
									+ ", " + plen + ", " + hlen + "\n";

							if (status)
							{
								if (this.printInfo)
								{
									FacilityManager.getMsgLogger().printmsg(MsgLogger.INFO, strInfo);
								}
								return true;
							}

						} // layers
						nextPrec[c][r]++;
					} // Resolution levels
					if (px != pxend)
					{
						x = minx + px * gcd_x;
					}
					else
					{
						x = tx0;
					}
				} // Horizontal precincts
				if (py != pyend)
				{
					y = miny + py * gcd_y;
				}
				else
				{
					y = ty0;
				}
			} // Vertical precincts
		} // components

		if (this.printInfo)
		{
			FacilityManager.getMsgLogger().printmsg(MsgLogger.INFO, strInfo);
		}
		return false; // Decoding rate was not reached
	}

	/**
	 * Finish initialization of members for specified tile, reads packets head
	 * of each tile and keeps location of each code-block's codewords. The last
	 * 2 tasks are done by calling specific methods of PktDecoder.
	 * 
	 * <p>
	 * Then, if a parsing output rate is defined, it keeps information of first
	 * layers only. This operation simulates a creation of a
	 * layer-resolution-component progressive bit-stream which will be next
	 * truncated and decoded.
	 * 
	 * @param t
	 *            Tile index
	 * 
	 * @see PktDecoder
	 */
	private void readTilePkts(final int t) throws IOException
	{
		this.pktHL = new Vector<Integer>();

		// Number of layers
		final int nl = ((Integer) this.decSpec.nls.getTileDef(t)).intValue();

		// If packed packet headers was used, get the packet headers for this
		// tile
		if (((Boolean) this.decSpec.pphs.getTileDef(t)).booleanValue())
		{
			// Gets packed headers as separate input stream
			final ByteArrayInputStream pphbais = this.hd.getPackedPktHead(t);

			// Restarts PktDecoder instance
			this.cbI = this.pktDec.restart(this.nc, this.mdl, nl, this.cbI, true, pphbais);
		}
		else
		{
			// Restarts PktDecoder instance
			this.cbI = this.pktDec.restart(this.nc, this.mdl, nl, this.cbI, false, null);
		}

		// Reads packets of the tile according to the progression order
		final int[][] pocSpec = ((int[][]) this.decSpec.pcs.getTileDef(t));
		final int nChg = (null == pocSpec) ? 1 : pocSpec.length;

		// Create an array containing information about changes (progression
		// order type, layers index start, layer index end, resolution level
		// start, resolution level end, component index start, component index
		// end). There is one row per progresion order
		final int[][] change = new int[nChg][6];
		int idx = 0; // Index of the current progression order

		change[0][1] = 0; // layer start

		if (null == pocSpec)
		{
			change[idx][0] = ((Integer) this.decSpec.pos.getTileDef(t)).intValue();
			// Progression type found in COx marker segments
			change[idx][1] = nl; // Layer index end
			change[idx][2] = 0; // resolution level start
			change[idx][3] = this.decSpec.dls.getMaxInTile(t) + 1; // res. level end
			change[idx][4] = 0; // Component index start
			change[idx][5] = this.nc; // Component index end
		}
		else
		{
			for (idx = 0; idx < nChg; idx++)
			{
				change[idx][0] = pocSpec[idx][5];
				change[idx][1] = pocSpec[idx][2]; // layer end
				change[idx][2] = pocSpec[idx][0]; // res. lev. start
				change[idx][3] = pocSpec[idx][3]; // res. lev. end
				change[idx][4] = pocSpec[idx][1]; // Comp. index start
				change[idx][5] = pocSpec[idx][4]; // Comp. index end
			}
		}

		// Seeks to the first packet of the first tile-part
		try
		{
			// If in truncation mode, the first tile-part may be beyond the
			// target decoding rate. In this case, the offset of the first
			// packet is not defined.
			if (this.isTruncMode && null == firstPackOff || null == firstPackOff[t])
			{
				return;
			}
			this.in.seek(this.firstPackOff[t][0]);
		}
		catch (final EOFException e)
		{
			FacilityManager.getMsgLogger().printmsg(MsgLogger.WARNING, "Codestream truncated in tile " + t);
			return;
		}

		this.curTilePart = 0;

		// Start and end indexes for layers, resolution levels and components.
		int lye, ress, rese, comps, compe;
		boolean status = false;
		final int nb = this.nBytes[t];
		final int[][] lys = new int[this.nc][];
		for (int c = 0; c < this.nc; c++)
		{
			lys[c] = new int[((Integer) this.decSpec.dls.getTileCompVal(t, c)).intValue() + 1];
		}

		try
		{
			for (int chg = 0; chg < nChg; chg++)
			{

				lye = change[chg][1];
				ress = change[chg][2];
				rese = change[chg][3];
				comps = change[chg][4];
				compe = change[chg][5];

				switch (change[chg][0])
				{
					case ProgressionType.LY_RES_COMP_POS_PROG:
						status = this.readLyResCompPos(lys, lye, ress, rese, comps, compe);
						break;
					case ProgressionType.RES_LY_COMP_POS_PROG:
						status = this.readResLyCompPos(lys, lye, ress, rese, comps, compe);
						break;
					case ProgressionType.RES_POS_COMP_LY_PROG:
						status = this.readResPosCompLy(lys, lye, ress, rese, comps, compe);
						break;
					case ProgressionType.POS_COMP_RES_LY_PROG:
						status = this.readPosCompResLy(lys, lye, ress, rese, comps, compe);
						break;
					case ProgressionType.COMP_POS_RES_LY_PROG:
						status = this.readCompPosResLy(lys, lye, ress, rese, comps, compe);
						break;
					default:
						throw new IllegalArgumentException("Not recognized progression type");
				}

				// Update next first layer index
				for (int c = comps; c < compe; c++)
				{
					if (c >= lys.length)
						continue;
					for (int r = ress; r < rese; r++)
					{
						if (r >= lys[c].length)
							continue;
						lys[c][r] = lye;
					}
				}

				if (status || this.usePOCQuit)
				{
					break;
				}
			}
		}
		catch (final EOFException e)
		{
			// Should never happen. Truncated codestream are normally found by
			// the class constructor
			throw e;
		}

		// In truncation mode, update the number of read bytes
		if (this.isTruncMode)
		{
			this.anbytes += nb - this.nBytes[t];

			// If truncation rate is reached
			if (status)
			{
				this.nBytes[t] = 0;
			}
		}
		else if (this.nBytes[t] < (this.totTileLen[t] - this.totTileHeadLen[t]))
		{
			// In parsing mode, if there is not enough rate to entirely read the
			// tile. Then, parses the bit stream so as to create a virtual
			// layer-resolution-component progressive bit stream that will be
			// truncated and decoded afterwards.
			CBlkInfo cb;

			// Systematicaly reject all remaining code-blocks if one
			// code-block, at least, is refused.
			boolean reject;
			// Stop reading any data from the bit stream
			boolean stopCount = false;
			// Length of each packet's head (in an array)
			final int[] pktHeadLen = new int[this.pktHL.size()];
			for (int i = this.pktHL.size() - 1; 0 <= i; i--)
			{
				pktHeadLen[i] = this.pktHL.elementAt(i).intValue();
			}

			// Parse each code-block, layer per layer until nBytes[t] is
			// reached
			reject = false;
			for (int l = 0; l < nl; l++)
			{ // layers
				if (null == cbI)
					continue;
				final int nc = this.cbI.length;

				int mres = 0;
				for (int c = 0; c < nc; c++)
				{
					if (null != cbI[c] && this.cbI[c].length > mres)
						mres = this.cbI[c].length;
				}
				for (int r = 0; r < mres; r++)
				{ // resolutions

					int msub = 0;
					for (int c = 0; c < nc; c++)
					{
						if (null != cbI[c] && null != cbI[c][r] && this.cbI[c][r].length > msub)
							msub = this.cbI[c][r].length;
					}
					for (int s = 0; s < msub; s++)
					{ // subbands
						// Only LL subband resolution level 0
						if (0 == r && 0 != s)
						{
							continue;
						}
						else if (0 != r && 0 == s)
						{
							// No LL subband in resolution level > 0
							continue;
						}

						int mnby = 0;
						for (int c = 0; c < nc; c++)
						{
							if (null != cbI[c] && null != cbI[c][r] && null != cbI[c][r][s]
									&& this.cbI[c][r][s].length > mnby)
								mnby = this.cbI[c][r][s].length;
						}
						for (int m = 0; m < mnby; m++)
						{

							int mnbx = 0;
							for (int c = 0; c < nc; c++)
							{
								if (null != cbI[c] && null != cbI[c][r] && null != cbI[c][r][s]
										&& null != cbI[c][r][s][m] && this.cbI[c][r][s][m].length > mnbx)
									mnbx = this.cbI[c][r][s][m].length;
							}
							for (int n = 0; n < mnbx; n++)
							{

								for (int c = 0; c < nc; c++)
								{

									if (null == cbI[c] || null == cbI[c][r] || null == cbI[c][r][s]
											|| null == cbI[c][r][s][m] || null == cbI[c][r][s][m][n])
									{
										continue;
									}
									cb = this.cbI[c][r][s][m][n];

									// If no code-block has been refused until
									// now
									if (!reject)
									{
										// Rate is to low to allow reading of
										// packet's head
										if (this.nBytes[t] < pktHeadLen[cb.pktIdx[l]])
										{
											// Stop parsing
											stopCount = true;
											// Reject all next
											// code-blocks
											reject = true;
										}
										else
										{
											// Rate is enough to read packet's
											// head
											if (!stopCount)
											{
												// If parsing was not stopped
												// Takes into account packet's
												// head length
												this.nBytes[t] -= pktHeadLen[cb.pktIdx[l]];
												this.anbytes += pktHeadLen[cb.pktIdx[l]];
												// Set packet's head length to
												// 0, so that it won't be
												// taken into account next
												// time
												pktHeadLen[cb.pktIdx[l]] = 0;
											}
										}
									}
									// Code-block has no data in this layer
									if (0 == cb.len[l])
									{
										continue;
									}

									// Accepts code-block if length is enough,
									// if this code-block was not refused in a
									// previous layer and if no code-block was
									// refused in current component
									if (cb.len[l] < this.nBytes[t] && !reject)
									{
										this.nBytes[t] -= cb.len[l];
										this.anbytes += cb.len[l];
									}
									else
									{
										// Refuses code-block
										// Forgets code-block's data
										cb.len[l] = cb.off[l] = cb.ntp[l] = 0;
										// Refuses all other code-block in
										// current and next component
										reject = true;
									}

								} // End loop on components
							} // End loop on horiz. code-blocks
						} // End loop on vert. code-blocks
					} // End loop on subbands
				} // End loop on resolutions
			} // End loop on layers
		}
		else
		{
			// No parsing for this tile, adds tile's body to the total
			// number of read bytes.
			this.anbytes += this.totTileLen[t] - this.totTileHeadLen[t];
			if (t < this.getNumTiles() - 1)
			{
				this.nBytes[t + 1] += this.nBytes[t] - (this.totTileLen[t] - this.totTileHeadLen[t]);
			}
		}
	}

	/**
	 * Changes the current tile, given the new indexes. An
	 * IllegalArgumentException is thrown if the indexes do not correspond to a
	 * valid tile.
	 * 
	 * @param x
	 *            The horizontal indexes the tile.
	 * 
	 * @param y
	 *            The vertical indexes of the new tile.
	 * 
	 * @return The new tile index
	 */
	@Override
	public int setTile(final int x, final int y)
	{

		int i; // counter
		// Check validity of tile indexes
		if (0 > x || 0 > y || x >= this.ntX || y >= this.ntY)
		{
			throw new IllegalArgumentException();
		}
		final int t = (y * this.ntX + x);

		// Reset number of read bytes if needed
		if (0 == t)
		{
			this.anbytes = this.headLen;
			if (!this.isTruncMode)
			{
				this.anbytes += 2;
			}
			// Restore values of nBytes
			if (0 <= nt)
				System.arraycopy(this.baknBytes, 0, this.nBytes, 0, this.nt);
		}

		// Set the new current tile
		this.ctX = x;
		this.ctY = y;
		// Calculate tile relative points
		final int ctox = (0 == x) ? this.ax : this.px + x * this.ntW;
		final int ctoy = (0 == y) ? this.ay : this.py + y * this.ntH;
		for (i = this.nc - 1; 0 <= i; i--)
		{
			this.culx[i] = (ctox + this.hd.getCompSubsX(i) - 1) / this.hd.getCompSubsX(i);
			this.culy[i] = (ctoy + this.hd.getCompSubsY(i) - 1) / this.hd.getCompSubsY(i);
			this.offX[i] = (this.px + x * this.ntW + this.hd.getCompSubsX(i) - 1) / this.hd.getCompSubsX(i);
			this.offY[i] = (this.py + y * this.ntH + this.hd.getCompSubsY(i) - 1) / this.hd.getCompSubsY(i);
		}

		// Initialize subband tree and number of resolution levels
		this.subbTrees = new SubbandSyn[this.nc];
		this.mdl = new int[this.nc];
		this.derived = new boolean[this.nc];
		this.params = new StdDequantizerParams[this.nc];
		this.gb = new int[this.nc];

		for (int c = 0; c < this.nc; c++)
		{
			this.derived[c] = this.decSpec.qts.isDerived(t, c);
			this.params[c] = (StdDequantizerParams) this.decSpec.qsss.getTileCompVal(t, c);
			this.gb[c] = ((Integer) this.decSpec.gbs.getTileCompVal(t, c)).intValue();
			this.mdl[c] = ((Integer) this.decSpec.dls.getTileCompVal(t, c)).intValue();

			this.subbTrees[c] = new SubbandSyn(this.getTileCompWidth(t, c, this.mdl[c]), this.getTileCompHeight(t, c, this.mdl[c]), this.getResULX(c,
					this.mdl[c]), this.getResULY(c, this.mdl[c]), this.mdl[c], this.decSpec.wfs.getHFilters(t, c), this.decSpec.wfs.getVFilters(t, c));
			this.initSubbandsFields(c, this.subbTrees[c]);
		}

		// Read tile's packets
		try
		{
			this.readTilePkts(t);
		}
		catch (final IOException e)
		{
			e.printStackTrace();
			throw new Error("IO Error when reading tile " + x + " x " + y);
		}
		return this.getTileIdx();
	}

	/**
	 * Advances to the next tile, in standard scan-line order (by rows then
	 * columns). A NoNextElementException is thrown if the current tile is the
	 * last one (i.e. there is no next tile).
	 * 
	 * @return The new tile index
	 */
	@Override
	public int nextTile()
	{
		int tIdx = 0;
		if (this.ctX == this.ntX - 1 && this.ctY == this.ntY - 1)
		{ // Already at last tile
			throw new NoNextElementException();
		}
		else if (this.ctX < this.ntX - 1)
		{ // If not at end of current tile line
			tIdx = this.setTile(this.ctX + 1, this.ctY);
		}
		else
		{ // Go to first tile at next line
			tIdx = this.setTile(0, this.ctY + 1);
		}
		return tIdx;
	}

	/**
	 * Returns the specified coded code-block, for the specified component, in
	 * the current tile. The first layer to return is indicated by 'fl'. The
	 * number of layers that is returned depends on 'nl' and the amount of
	 * available data.
	 * 
	 * <p>
	 * The argument 'fl' is to be used by subsequent calls to this method for
	 * the same code-block. In this way supplemental data can be retrieved at a
	 * later time. The fact that data from more than one layer can be returned
	 * means that several packets from the same code-block, of the same
	 * component, and the same tile, have been concatenated.
	 * 
	 * <p>
	 * The returned compressed code-block can have its progressive attribute
	 * set. If this attribute is set it means that more data can be obtained by
	 * subsequent calls to this method (subject to transmission delays, etc). If
	 * the progressive attribute is not set it means that the returned data is
	 * all the data that can be obtained for the specified code-block.
	 * 
	 * <p>
	 * The compressed code-block is uniquely specified by the current tile, the
	 * component (identified by 'c'), the subband (indentified by 'sb') and the
	 * code-block vertical and horizontal indexes 'n' and 'm'.
	 * 
	 * <p>
	 * The 'ulx' and 'uly' members of the returned 'DecLyrdCBlk' object contain
	 * the coordinates of the top-left corner of the block, with respect to the
	 * tile, not the subband.
	 * 
	 * @param c
	 *            The index of the component, from 0 to N-1.
	 * 
	 * @param m
	 *            The vertical index of the code-block to return, in the
	 *            specified subband.
	 * 
	 * @param n
	 *            The horizontal index of the code-block to return, in the
	 *            specified subband.
	 * 
	 * @param sb
	 *            The subband in whic the requested code-block is.
	 * 
	 * @param fl
	 *            The first layer to return.
	 * 
	 * @param nl
	 *            The number of layers to return, if negative all available
	 *            layers are returned, starting at 'fl'.
	 * 
	 * @param ccb
	 *            If not null this object is used to return the compressed
	 *            code-block. If null a new object is created and returned. If
	 *            the data array in ccb is not null then it can be reused to
	 *            return the compressed data.
	 * 
	 * @return The compressed code-block, with a certain number of layers
	 *         determined by the available data and 'nl'.
	 */
	@Override
	public DecLyrdCBlk getCodeBlock(final int c, final int m, final int n, final SubbandSyn sb, final int fl, int nl, DecLyrdCBlk ccb)
	{

		final int t = this.getTileIdx();
		final CBlkInfo rcb; // requested code-block
		final int r = sb.resLvl; // Resolution level
		final int s = sb.sbandIdx; // Subband index
		int tpidx;
		int passtype;

		// Number of layers
		final int numLayers = ((Integer) this.decSpec.nls.getTileDef(t)).intValue();
		final int options = ((Integer) this.decSpec.ecopts.getTileCompVal(t, c)).intValue();
		if (0 > nl)
		{
			nl = numLayers - fl + 1;
		}

		// If the l quit condition is used, Make sure that no layer
		// after lquit is returned
		if (-1 != lQuit && fl + nl > this.lQuit)
		{
			nl = this.lQuit - fl;
		}

		// Check validity of resquested resolution level (according to the
		// "-res" option).
		final int maxdl = this.getSynSubbandTree(t, c).resLvl;
		if (r > this.targetRes + maxdl - this.decSpec.dls.getMin())
		{
			throw new Error("JJ2000 error: requesting a code-block disallowed by the '-res' option.");
		}

		// Check validity of all the arguments
		try
		{
			rcb = this.cbI[c][r][s][m][n];

			if (1 > fl || fl > numLayers || fl + nl - 1 > numLayers)
			{
				throw new IllegalArgumentException();
			}
		}
		catch (final ArrayIndexOutOfBoundsException e)
		{
			throw new IllegalArgumentException("Code-block (t:" + t + ", c:" + c + ", r:" + r + ", s:" + s + ", " + m
					+ "x" + +n + ") not found in codestream");
		}
		catch (final NullPointerException e)
		{
			throw new IllegalArgumentException("Code-block (t:" + t + ", c:" + c + ", r:" + r + ", s:" + s + ", " + m
					+ "x" + n + ") not found in bit stream");
		}

		// Create DecLyrdCBlk object if necessary
		if (null == ccb)
		{
			ccb = new DecLyrdCBlk();
		}
		ccb.m = m;
		ccb.n = n;
		ccb.nl = 0;
		ccb.dl = 0;
		ccb.nTrunc = 0;

		if (null == rcb)
		{
			// This code-block was skipped when reading. Returns no data
			ccb.skipMSBP = 0;
			ccb.prog = false;
			ccb.w = ccb.h = ccb.ulx = ccb.uly = 0;
			return ccb;
		}

		// ccb initialization
		ccb.skipMSBP = rcb.msbSkipped;
		ccb.ulx = rcb.ulx;
		ccb.uly = rcb.uly;
		ccb.w = rcb.w;
		ccb.h = rcb.h;
		ccb.ftpIdx = 0;

		// Search for index of first truncation point (first layer where
		// length of data is not zero)
		int l = 0;
		while ((l < rcb.len.length) && (0 == rcb.len[l]))
		{
			ccb.ftpIdx += rcb.ntp[l];
			l++;
		}

		// Calculate total length, number of included layer and number of
		// truncation points
		for (l = fl - 1; l < fl + nl - 1; l++)
		{
			ccb.nl++;
			ccb.dl += rcb.len[l];
			ccb.nTrunc += rcb.ntp[l];
		}

		// Calculate number of terminated segments
		int nts;
		if (0 != (options & OPT_TERM_PASS))
		{
			// Regular termination in use One segment per pass
			// (i.e. truncation point)
			nts = ccb.nTrunc - ccb.ftpIdx;
		}
		else if (0 != (options & OPT_BYPASS))
		{
			// Selective arithmetic coding bypass mode in use, but no regular
			// termination: 1 segment upto the end of the last pass of the 4th
			// most significant bit-plane, and, in each following bit-plane,
			// one segment upto the end of the 2nd pass and one upto the end
			// of the 3rd pass.

			if (FIRST_BYPASS_PASS_IDX >= ccb.nTrunc)
			{
				nts = 1;
			}
			else
			{
				nts = 1;
				// Adds one for each terminated pass
				for (tpidx = ccb.ftpIdx; tpidx < ccb.nTrunc; tpidx++)
				{
					if (FIRST_BYPASS_PASS_IDX - 1 <= tpidx)
					{
						passtype = (tpidx + StdEntropyCoderOptions.NUM_EMPTY_PASSES_IN_MS_BP) % StdEntropyCoderOptions.NUM_PASSES;
						if (1 == passtype || 2 == passtype)
						{
							// lazy pass just before MQ pass or MQ pass just
							// before lazy pass => terminated
							nts++;
						}
					}
				}
			}
		}
		else
		{
			// Nothing special in use, just one terminated segment
			nts = 1;
		}

		// ccb.data creation
		if (null == ccb.data || ccb.data.length < ccb.dl)
		{
			ccb.data = new byte[ccb.dl];
		}

		// ccb.tsLengths creation
		if (1 < nts && (null == ccb.tsLengths || ccb.tsLengths.length < nts))
		{
			ccb.tsLengths = new int[nts];
		}
		else if (1 < nts && OPT_BYPASS == (options & (OPT_BYPASS | OPT_TERM_PASS)))
		{
			ArrayUtil.intArraySet(ccb.tsLengths, 0);
		}

		// Fill ccb with compressed data
		int dataIdx = -1;
		tpidx = ccb.ftpIdx;
		int ctp = ccb.ftpIdx; // Cumulative number of truncation
		// point for the current layer layer
		int tsidx = 0;
		int j;

		for (l = fl - 1; l < fl + nl - 1; l++)
		{
			ctp += rcb.ntp[l];
			// No data in this layer
			if (0 == rcb.len[l])
				continue;

			// Read data
			// NOTE: we should never get an EOFException here since all
			// data is checked to be within the file.
			try
			{
				this.in.seek(rcb.off[l]);
				this.in.readFully(ccb.data, dataIdx + 1, rcb.len[l]);
				dataIdx += rcb.len[l];
			}
			catch (final IOException e)
			{
				JJ2KExceptionHandler.handleException(e);
			}

			// Get the terminated segment lengths, if any
			if (1 == nts)
				continue;
			if (0 != (options & OPT_TERM_PASS))
			{
				// Regular termination => each pass is terminated
				for (j = 0; tpidx < ctp; j++, tpidx++)
				{
					if (null != rcb.segLen[l])
					{
						ccb.tsLengths[tsidx] = rcb.segLen[l][j];
						tsidx++;
					}
					else
					{ // Only one terminated segment in packet
						ccb.tsLengths[tsidx] = rcb.len[l];
						tsidx++;
					}
				}
			}
			else
			{
				// Lazy coding without regular termination
				for (j = 0; tpidx < ctp; tpidx++)
				{
					if (FIRST_BYPASS_PASS_IDX - 1 <= tpidx)
					{
						passtype = (tpidx + StdEntropyCoderOptions.NUM_EMPTY_PASSES_IN_MS_BP) % StdEntropyCoderOptions.NUM_PASSES;
						if (0 != passtype)
						{
							// lazy pass just before MQ pass or MQ
							// pass just before lazy pass =>
							// terminated
							if (null != rcb.segLen[l])
							{
								ccb.tsLengths[tsidx] += rcb.segLen[l][j];
								tsidx++;
								j++;
								rcb.len[l] -= rcb.segLen[l][j - 1];
							}
							else
							{ // Only one terminated segment in packet
								ccb.tsLengths[tsidx] += rcb.len[l];
								tsidx++;
								rcb.len[l] = 0;
							}
						}

					}
				}

				// Last length in packet always in (either terminated segment
				// or contribution to terminated segment)
				if (null != rcb.segLen[l] && j < rcb.segLen[l].length)
				{
					ccb.tsLengths[tsidx] += rcb.segLen[l][j];
					rcb.len[l] -= rcb.segLen[l][j];
				}
				else
				{ // Only one terminated segment in packet
					if (tsidx < nts)
					{
						ccb.tsLengths[tsidx] += rcb.len[l];
						rcb.len[l] = 0;
					}
				}
			}
		}
		if (1 == nts && null != ccb.tsLengths)
		{
			ccb.tsLengths[0] = ccb.dl;
		}

		// Set the progressive flag
		final int lastlayer = fl + nl - 1;
		if (lastlayer < numLayers - 1)
		{
			for (l = lastlayer + 1; l < numLayers; l++)
			{
				// It remains data for this code-block in the bit stream
				if (0 != rcb.len[l])
				{
					ccb.prog = true;
				}
			}
		}

		return ccb;
	}
}
