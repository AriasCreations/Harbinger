/**
 * CVS identifier:
 *
 * $Id: PktDecoder.java,v 1.46 2002/07/19 12:35:14 grosbois Exp $
 *
 * Class:                   PktDecoder
 *
 * Description:             Reads packets heads and keeps location of
 *                          code-blocks' codewords
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

import jj2000.j2k.wavelet.synthesis.*;
import jj2000.j2k.codestream.*;
import jj2000.j2k.entropy.*;
import jj2000.j2k.decoder.*;
import jj2000.j2k.image.*;
import jj2000.j2k.util.*;
import jj2000.j2k.io.*;

import java.util.*;
import java.io.*;

/**
 * This class is used to read packet's head and body. All the members must be
 * re-initialized at the beginning of each tile thanks to the restart() method.
 */
public class PktDecoder implements StdEntropyCoderOptions
{

	/** Reference to the codestream reader agent */
	private final BitstreamReaderAgent src;

	/** Flag indicating whether packed packet header was used for this tile */
	private boolean pph;

	/** The packed packet header if it was used */
	private ByteArrayInputStream pphbais;

	/** Reference to decoder specifications */
	private final DecoderSpecs decSpec;

	/** Reference to the HeaderDecoder */
	private final HeaderDecoder hd;

	/**
	 * Initial value of the state variable associated with code-block length.
	 */
	private final int INIT_LBLOCK = 3;

	/** The wrapper to read bits for the packet heads */
	private final PktHeaderBitReader bin;

	/** Reference to the stream where to read from */
	private final RandomAccessIO ehs;

	/**
	 * Maximum number of precincts :
	 * 
	 * <ul>
	 * <li>1st dim: component index.</li>
	 * <li>2nd dim: resolution level index.</li>
	 * </ul>
	 */
	private Coord[][] numPrec;

	/** Index of the current tile */
	private int tIdx;

	/**
	 * Array containing the coordinates, width, height, indexes, ... of the
	 * precincts in the current tile:
	 * 
	 * <ul>
	 * <li>1st dim: component index.</li>
	 * <li>2nd dim: resolution level index.</li>
	 * <li>3rd dim: precinct index.</li>
	 * </ul>
	 */
	private PrecInfo[][][] ppinfo;

	/**
	 * Lblock value used to read code size information in each packet head:
	 * 
	 * <ul>
	 * <li>1st dim: component index.</li>
	 * <li>2nd dim: resolution level index.</li>
	 * <li>3rd dim: subband index.</li>
	 * <li>4th/5th dim: code-block index (vert. and horiz.).</li>
	 * </ul>
	 */
	private int[][][][][] lblock;

	/**
	 * Tag tree used to read inclusion informations in packet's head:
	 * 
	 * <ul>
	 * <li>1st dim: component index.</li>
	 * <li>2nd dim: resolution level index.</li>
	 * <li>3rd dim: precinct index.</li>
	 * <li>4th dim: subband index.</li>
	 */
	private TagTreeDecoder[][][][] ttIncl;

	/**
	 * Tag tree used to read bit-depth information in packet's head:
	 * 
	 * <ul>
	 * <li>1st dim: component index.</li>
	 * <li>2nd dim: resolution level index.</li>
	 * <li>3rd dim: precinct index.</li>
	 * <li>4th dim: subband index.</li>
	 * </ul>
	 */
	private TagTreeDecoder[][][][] ttMaxBP;

	/** Number of layers in t he current tile */
	private int nl;

	/** The number of components */
	// private int nc;

	/** Whether or not SOP marker segment are used */
	private boolean sopUsed;

	/** Whether or not EPH marker are used */
	private boolean ephUsed;

	/**
	 * Index of the current packet in the tile. Used with SOP marker segment
	 */
	private int pktIdx;

	/**
	 * List of code-blocks found in last read packet head (one list per subband)
	 */
	private Vector<CBlkCoordInfo>[] cblks;

	/** Number of codeblocks encountered. used for ncb quit condition */
	private int ncb;

	/**
	 * Maximum number of codeblocks to read before ncb quit condition is reached
	 */
	private final int maxCB;

	/** Flag indicating whether ncb quit condition has been reached */
	private boolean ncbQuit;

	/** The tile in which the ncb quit condition was reached */
	private int tQuit;

	/** The component in which the ncb quit condition was reached */
	private int cQuit;

	/** The subband in which the ncb quit condition was reached */
	private int sQuit;

	/** The resolution in which the ncb quit condition was reached */
	private int rQuit;

	/** The x position of the last code block before ncb quit reached */
	private int xQuit;

	/** The y position of the last code block before ncb quit reached */
	private int yQuit;

	/** True if truncation mode is used. False if it is parsing mode */
	private final boolean isTruncMode;

	/**
	 * Creates an empty PktDecoder object associated with given decoder
	 * specifications and HeaderDecoder. This object must be initialized thanks
	 * to the restart method before being used.
	 * 
	 * @param decSpec
	 *            The decoder specifications.
	 * 
	 * @param hd
	 *            The HeaderDecoder instance.
	 * 
	 * @param ehs
	 *            The stream where to read data from.
	 * 
	 * @param src
	 *            The bit stream reader agent.
	 * 
	 * @param isTruncMode
	 *            Whether or not truncation mode is required.
	 * 
	 * @param maxCB
	 *            The maximum number of code-blocks to read before ncbquit
	 * 
	 */
	public PktDecoder(final DecoderSpecs decSpec, final HeaderDecoder hd, final RandomAccessIO ehs, final BitstreamReaderAgent src,
					  final boolean isTruncMode, final int maxCB)
	{
		this.decSpec = decSpec;
		this.hd = hd;
		this.ehs = ehs;
		this.isTruncMode = isTruncMode;
		this.bin = new PktHeaderBitReader(ehs);
		this.src = src;
		this.ncb = 0;
		this.ncbQuit = false;
		this.maxCB = maxCB;
	}

	/**
	 * Re-initialize the PktDecoder instance at the beginning of a new tile.
	 * 
	 * @param nc
	 *            The number of components in this tile
	 * 
	 * @param mdl
	 *            The maximum number of decomposition level in each component of
	 *            this tile
	 * 
	 * @param nl
	 *            The number of layers in this tile
	 * 
	 * @param cbI
	 *            The code-blocks array
	 * 
	 * @param pph
	 *            Flag indicating whether packed packet headers was used
	 * 
	 * @param pphbais
	 *            Stream containing the packed packet headers
	 */
	public CBlkInfo[][][][][] restart(final int nc, final int[] mdl, final int nl, CBlkInfo[][][][][] cbI, final boolean pph,
									  final ByteArrayInputStream pphbais)
	{
		// this.nc = nc;
		this.nl = nl;
		tIdx = this.src.getTileIdx();
		this.pph = pph;
		this.pphbais = pphbais;

		this.sopUsed = ((Boolean) this.decSpec.sops.getTileDef(this.tIdx)).booleanValue();
		this.pktIdx = 0;
		this.ephUsed = ((Boolean) this.decSpec.ephs.getTileDef(this.tIdx)).booleanValue();

		cbI = new CBlkInfo[nc][][][][];
		this.lblock = new int[nc][][][][];
		this.ttIncl = new TagTreeDecoder[nc][][][];
		this.ttMaxBP = new TagTreeDecoder[nc][][][];
		this.numPrec = new Coord[nc][];
		this.ppinfo = new PrecInfo[nc][][];

		// Used to compute the maximum number of precincts for each resolution
		// level
		int tcx0, tcy0, tcx1, tcy1; // Current tile position in the domain of
		// the image component
		int trx0, try0, trx1, try1; // Current tile position in the reduced

		SubbandSyn root, sb;
		int mins, maxs;
		Coord nBlk = null;
		final int cb0x = this.src.getCbULX();
		final int cb0y = this.src.getCbULY();

		for (int c = 0; c < nc; c++)
		{
			cbI[c] = new CBlkInfo[mdl[c] + 1][][][];
			this.lblock[c] = new int[mdl[c] + 1][][][];
			this.ttIncl[c] = new TagTreeDecoder[mdl[c] + 1][][];
			this.ttMaxBP[c] = new TagTreeDecoder[mdl[c] + 1][][];
			this.numPrec[c] = new Coord[mdl[c] + 1];
			this.ppinfo[c] = new PrecInfo[mdl[c] + 1][];

			// Get the tile-component coordinates on the reference grid
			tcx0 = this.src.getResULX(c, mdl[c]);
			tcy0 = this.src.getResULY(c, mdl[c]);
			tcx1 = tcx0 + this.src.getTileCompWidth(this.tIdx, c, mdl[c]);
			tcy1 = tcy0 + this.src.getTileCompHeight(this.tIdx, c, mdl[c]);

			for (int r = 0; r <= mdl[c]; r++)
			{

				// Tile's coordinates in the reduced resolution image domain
				trx0 = (int) Math.ceil(tcx0 / (double) (1 << (mdl[c] - r)));
				try0 = (int) Math.ceil(tcy0 / (double) (1 << (mdl[c] - r)));
				trx1 = (int) Math.ceil(tcx1 / (double) (1 << (mdl[c] - r)));
				try1 = (int) Math.ceil(tcy1 / (double) (1 << (mdl[c] - r)));

				// Calculate the maximum number of precincts for each
				// resolution level taking into account tile specific options.
				final double twoppx = this.getPPX(this.tIdx, c, r);
				final double twoppy = this.getPPY(this.tIdx, c, r);
				this.numPrec[c][r] = new Coord();
				if (trx1 > trx0)
				{
					this.numPrec[c][r].x = (int) Math.ceil((trx1 - cb0x) / twoppx)
							- (int) Math.floor((trx0 - cb0x) / twoppx);
				}
				else
				{
					this.numPrec[c][r].x = 0;
				}
				if (try1 > try0)
				{
					this.numPrec[c][r].y = (int) Math.ceil((try1 - cb0y) / twoppy)
							- (int) Math.floor((try0 - cb0y) / twoppy);
				}
				else
				{
					this.numPrec[c][r].y = 0;
				}

				// First and last subbands indexes
				mins = (0 == r) ? 0 : 1;
				maxs = (0 == r) ? 1 : 4;

				final int maxPrec = this.numPrec[c][r].x * this.numPrec[c][r].y;

				this.ttIncl[c][r] = new TagTreeDecoder[maxPrec][maxs + 1];
				this.ttMaxBP[c][r] = new TagTreeDecoder[maxPrec][maxs + 1];
				cbI[c][r] = new CBlkInfo[maxs + 1][][];
				this.lblock[c][r] = new int[maxs + 1][][];

				this.ppinfo[c][r] = new PrecInfo[maxPrec];
				this.fillPrecInfo(c, r, mdl[c]);

				root = this.src.getSynSubbandTree(this.tIdx, c);
				for (int s = mins; s < maxs; s++)
				{
					sb = (SubbandSyn) root.getSubbandByIdx(r, s);
					nBlk = sb.numCb;

					cbI[c][r][s] = new CBlkInfo[nBlk.y][nBlk.x];
					this.lblock[c][r][s] = new int[nBlk.y][nBlk.x];

					for (int i = nBlk.y - 1; 0 <= i; i--)
					{
						ArrayUtil.intArraySet(this.lblock[c][r][s][i], this.INIT_LBLOCK);
					}
				} // loop on subbands
			} // End loop on resolution levels
		} // End loop on components

		return cbI;
	}

	/**
	 * Retrives precincts and code-blocks coordinates in the given resolution,
	 * level and component. Finishes TagTreeEncoder initialization as well.
	 * 
	 * @param c
	 *            Component index.
	 * 
	 * @param r
	 *            Resolution level index.
	 * 
	 * @param mdl
	 *            Number of decomposition level in component <tt>c</tt>.
	 */
	private void fillPrecInfo(final int c, final int r, final int mdl)
	{
		if (0 == ppinfo[c][r].length)
			return; // No precinct in this
		// resolution level

		final Coord tileI = this.src.getTile(null);
		// Coord nTiles = src.getNumTiles(null);

		// int xsiz, ysiz;
		final int x0siz;
		final int y0siz;
		final int xt0siz;
		final int yt0siz;
		final int xtsiz;
		final int ytsiz;

		xt0siz = this.src.getTilePartULX();
		yt0siz = this.src.getTilePartULY();
		xtsiz = this.src.getNomTileWidth();
		ytsiz = this.src.getNomTileHeight();
		x0siz = this.hd.getImgULX();
		y0siz = this.hd.getImgULY();
		// xsiz = hd.getImgWidth();
		// ysiz = hd.getImgHeight();

		final int tx0 = (0 == tileI.x) ? x0siz : xt0siz + tileI.x * xtsiz;
		final int ty0 = (0 == tileI.y) ? y0siz : yt0siz + tileI.y * ytsiz;
		// int tx1 = (tileI.x != nTiles.x - 1) ? xt0siz + (tileI.x + 1) * xtsiz
		// : xsiz;
		// int ty1 = (tileI.y != nTiles.y - 1) ? yt0siz + (tileI.y + 1) * ytsiz
		// : ysiz;

		final int xrsiz = this.hd.getCompSubsX(c);
		final int yrsiz = this.hd.getCompSubsY(c);

		final int tcx0 = this.src.getResULX(c, mdl);
		final int tcy0 = this.src.getResULY(c, mdl);
		final int tcx1 = tcx0 + this.src.getTileCompWidth(this.tIdx, c, mdl);
		final int tcy1 = tcy0 + this.src.getTileCompHeight(this.tIdx, c, mdl);

		final int ndl = mdl - r;
		final int trx0 = (int) Math.ceil(tcx0 / (double) (1 << ndl));
		final int try0 = (int) Math.ceil(tcy0 / (double) (1 << ndl));
		final int trx1 = (int) Math.ceil(tcx1 / (double) (1 << ndl));
		final int try1 = (int) Math.ceil(tcy1 / (double) (1 << ndl));

		final int cb0x = this.src.getCbULX();
		final int cb0y = this.src.getCbULY();

		final double twoppx = this.getPPX(this.tIdx, c, r);
		final double twoppy = this.getPPY(this.tIdx, c, r);
		final int twoppx2 = (int) (twoppx / 2);
		final int twoppy2 = (int) (twoppy / 2);

		// Precincts are located at (cb0x+i*twoppx,cb0y+j*twoppy)
		// Valid precincts are those which intersect with the current
		// resolution level
		// int maxPrec = ppinfo[c][r].length;
		int nPrec = 0;

		final int istart = (int) Math.floor((try0 - cb0y) / twoppy);
		final int iend = (int) Math.floor((try1 - 1 - cb0y) / twoppy);
		final int jstart = (int) Math.floor((trx0 - cb0x) / twoppx);
		final int jend = (int) Math.floor((trx1 - 1 - cb0x) / twoppx);

		int acb0x, acb0y;

		final SubbandSyn root = this.src.getSynSubbandTree(this.tIdx, c);
		SubbandSyn sb = null;

		int p0x, p0y, p1x, p1y; // Precinct projection in subband
		int s0x, s0y, s1x, s1y; // Active subband portion
		int cw, ch;
		int kstart, kend, lstart, lend, k0, l0;
		int prg_ulx, prg_uly;
		final int prg_w = (int) twoppx << ndl;
		final int prg_h = (int) twoppy << ndl;
		int tmp1, tmp2;

		CBlkCoordInfo cb;

		for (int i = istart; i <= iend; i++)
		{ // Vertical precincts
			for (int j = jstart; j <= jend; j++, nPrec++)
			{ // Horizontal precincts
				if (j == jstart && 0 != (trx0 - cb0x) % (xrsiz * ((int) twoppx)))
				{
					prg_ulx = tx0;
				}
				else
				{
					prg_ulx = cb0x + j * xrsiz * ((int) twoppx << ndl);
				}
				if (i == istart && 0 != (try0 - cb0y) % (yrsiz * ((int) twoppy)))
				{
					prg_uly = ty0;
				}
				else
				{
					prg_uly = cb0y + i * yrsiz * ((int) twoppy << ndl);
				}

				this.ppinfo[c][r][nPrec] = new PrecInfo(r, (int) (cb0x + j * twoppx), (int) (cb0y + i * twoppy),
						(int) twoppx, (int) twoppy, prg_ulx, prg_uly, prg_w, prg_h);

				if (0 == r)
				{ // LL subband
					acb0x = cb0x;
					acb0y = cb0y;

					p0x = acb0x + j * (int) twoppx;
					p1x = p0x + (int) twoppx;
					p0y = acb0y + i * (int) twoppy;
					p1y = p0y + (int) twoppy;

					sb = (SubbandSyn) root.getSubbandByIdx(0, 0);
					s0x = (p0x < sb.ulcx) ? sb.ulcx : p0x;
					s1x = (p1x > sb.ulcx + sb.w) ? sb.ulcx + sb.w : p1x;
					s0y = (p0y < sb.ulcy) ? sb.ulcy : p0y;
					s1y = (p1y > sb.ulcy + sb.h) ? sb.ulcy + sb.h : p1y;

					// Code-blocks are located at (acb0x+k*cw,acb0y+l*ch)
					cw = sb.nomCBlkW;
					ch = sb.nomCBlkH;
					k0 = (int) Math.floor((sb.ulcy - acb0y) / (double) ch);
					kstart = (int) Math.floor((s0y - acb0y) / (double) ch);
					kend = (int) Math.floor((s1y - 1 - acb0y) / (double) ch);
					l0 = (int) Math.floor((sb.ulcx - acb0x) / (double) cw);
					lstart = (int) Math.floor((s0x - acb0x) / (double) cw);
					lend = (int) Math.floor((s1x - 1 - acb0x) / (double) cw);

					if (0 >= s1x - s0x || 0 >= s1y - s0y)
					{
						this.ppinfo[c][0][nPrec].nblk[0] = 0;
						this.ttIncl[c][r][nPrec][0] = new TagTreeDecoder(0, 0);
						this.ttMaxBP[c][r][nPrec][0] = new TagTreeDecoder(0, 0);
					}
					else
					{
						this.ttIncl[c][r][nPrec][0] = new TagTreeDecoder(kend - kstart + 1, lend - lstart + 1);
						this.ttMaxBP[c][r][nPrec][0] = new TagTreeDecoder(kend - kstart + 1, lend - lstart + 1);
						this.ppinfo[c][r][nPrec].cblk[0] = new CBlkCoordInfo[kend - kstart + 1][lend - lstart + 1];
						this.ppinfo[c][r][nPrec].nblk[0] = (kend - kstart + 1) * (lend - lstart + 1);

						for (int k = kstart; k <= kend; k++)
						{ // Vertical cblks
							for (int l = lstart; l <= lend; l++)
							{ // Horiz. cblks
								cb = new CBlkCoordInfo(k - k0, l - l0);
								if (l == l0)
								{
									cb.ulx = sb.ulx;
								}
								else
								{
									cb.ulx = sb.ulx + l * cw - (sb.ulcx - acb0x);
								}
								if (k == k0)
								{
									cb.uly = sb.uly;
								}
								else
								{
									cb.uly = sb.uly + k * ch - (sb.ulcy - acb0y);
								}
								tmp1 = acb0x + l * cw;
								tmp1 = (tmp1 > sb.ulcx) ? tmp1 : sb.ulcx;
								tmp2 = acb0x + (l + 1) * cw;
								tmp2 = (tmp2 > sb.ulcx + sb.w) ? sb.ulcx + sb.w : tmp2;
								cb.w = tmp2 - tmp1;
								tmp1 = acb0y + k * ch;
								tmp1 = (tmp1 > sb.ulcy) ? tmp1 : sb.ulcy;
								tmp2 = acb0y + (k + 1) * ch;
								tmp2 = (tmp2 > sb.ulcy + sb.h) ? sb.ulcy + sb.h : tmp2;
								cb.h = tmp2 - tmp1;
								this.ppinfo[c][r][nPrec].cblk[0][k - kstart][l - lstart] = cb;
							} // Horizontal code-blocks
						} // Vertical code-blocks
					}
				}
				else
				{ // HL, LH and HH subbands
					// HL subband
					acb0x = 0;
					acb0y = cb0y;

					p0x = acb0x + j * twoppx2;
					p1x = p0x + twoppx2;
					p0y = acb0y + i * twoppy2;
					p1y = p0y + twoppy2;

					sb = (SubbandSyn) root.getSubbandByIdx(r, 1);
					s0x = (p0x < sb.ulcx) ? sb.ulcx : p0x;
					s1x = (p1x > sb.ulcx + sb.w) ? sb.ulcx + sb.w : p1x;
					s0y = (p0y < sb.ulcy) ? sb.ulcy : p0y;
					s1y = (p1y > sb.ulcy + sb.h) ? sb.ulcy + sb.h : p1y;

					// Code-blocks are located at (acb0x+k*cw,acb0y+l*ch)
					cw = sb.nomCBlkW;
					ch = sb.nomCBlkH;
					k0 = (int) Math.floor((sb.ulcy - acb0y) / (double) ch);
					kstart = (int) Math.floor((s0y - acb0y) / (double) ch);
					kend = (int) Math.floor((s1y - 1 - acb0y) / (double) ch);
					l0 = (int) Math.floor((sb.ulcx - acb0x) / (double) cw);
					lstart = (int) Math.floor((s0x - acb0x) / (double) cw);
					lend = (int) Math.floor((s1x - 1 - acb0x) / (double) cw);

					if (0 >= s1x - s0x || 0 >= s1y - s0y)
					{
						this.ppinfo[c][r][nPrec].nblk[1] = 0;
						this.ttIncl[c][r][nPrec][1] = new TagTreeDecoder(0, 0);
						this.ttMaxBP[c][r][nPrec][1] = new TagTreeDecoder(0, 0);
					}
					else
					{
						this.ttIncl[c][r][nPrec][1] = new TagTreeDecoder(kend - kstart + 1, lend - lstart + 1);
						this.ttMaxBP[c][r][nPrec][1] = new TagTreeDecoder(kend - kstart + 1, lend - lstart + 1);
						this.ppinfo[c][r][nPrec].cblk[1] = new CBlkCoordInfo[kend - kstart + 1][lend - lstart + 1];
						this.ppinfo[c][r][nPrec].nblk[1] = (kend - kstart + 1) * (lend - lstart + 1);

						for (int k = kstart; k <= kend; k++)
						{ // Vertical cblks
							for (int l = lstart; l <= lend; l++)
							{ // Horiz. cblks
								cb = new CBlkCoordInfo(k - k0, l - l0);
								if (l == l0)
								{
									cb.ulx = sb.ulx;
								}
								else
								{
									cb.ulx = sb.ulx + l * cw - (sb.ulcx - acb0x);
								}
								if (k == k0)
								{
									cb.uly = sb.uly;
								}
								else
								{
									cb.uly = sb.uly + k * ch - (sb.ulcy - acb0y);
								}
								tmp1 = acb0x + l * cw;
								tmp1 = (tmp1 > sb.ulcx) ? tmp1 : sb.ulcx;
								tmp2 = acb0x + (l + 1) * cw;
								tmp2 = (tmp2 > sb.ulcx + sb.w) ? sb.ulcx + sb.w : tmp2;
								cb.w = tmp2 - tmp1;
								tmp1 = acb0y + k * ch;
								tmp1 = (tmp1 > sb.ulcy) ? tmp1 : sb.ulcy;
								tmp2 = acb0y + (k + 1) * ch;
								tmp2 = (tmp2 > sb.ulcy + sb.h) ? sb.ulcy + sb.h : tmp2;
								cb.h = tmp2 - tmp1;
								this.ppinfo[c][r][nPrec].cblk[1][k - kstart][l - lstart] = cb;
							} // Horizontal code-blocks
						} // Vertical code-blocks
					}

					// LH subband
					acb0x = cb0x;
					acb0y = 0;

					p0x = acb0x + j * twoppx2;
					p1x = p0x + twoppx2;
					p0y = acb0y + i * twoppy2;
					p1y = p0y + twoppy2;

					sb = (SubbandSyn) root.getSubbandByIdx(r, 2);
					s0x = (p0x < sb.ulcx) ? sb.ulcx : p0x;
					s1x = (p1x > sb.ulcx + sb.w) ? sb.ulcx + sb.w : p1x;
					s0y = (p0y < sb.ulcy) ? sb.ulcy : p0y;
					s1y = (p1y > sb.ulcy + sb.h) ? sb.ulcy + sb.h : p1y;

					// Code-blocks are located at (acb0x+k*cw,acb0y+l*ch)
					cw = sb.nomCBlkW;
					ch = sb.nomCBlkH;
					k0 = (int) Math.floor((sb.ulcy - acb0y) / (double) ch);
					kstart = (int) Math.floor((s0y - acb0y) / (double) ch);
					kend = (int) Math.floor((s1y - 1 - acb0y) / (double) ch);
					l0 = (int) Math.floor((sb.ulcx - acb0x) / (double) cw);
					lstart = (int) Math.floor((s0x - acb0x) / (double) cw);
					lend = (int) Math.floor((s1x - 1 - acb0x) / (double) cw);

					if (0 >= s1x - s0x || 0 >= s1y - s0y)
					{
						this.ppinfo[c][r][nPrec].nblk[2] = 0;
						this.ttIncl[c][r][nPrec][2] = new TagTreeDecoder(0, 0);
						this.ttMaxBP[c][r][nPrec][2] = new TagTreeDecoder(0, 0);
					}
					else
					{
						this.ttIncl[c][r][nPrec][2] = new TagTreeDecoder(kend - kstart + 1, lend - lstart + 1);
						this.ttMaxBP[c][r][nPrec][2] = new TagTreeDecoder(kend - kstart + 1, lend - lstart + 1);
						this.ppinfo[c][r][nPrec].cblk[2] = new CBlkCoordInfo[kend - kstart + 1][lend - lstart + 1];
						this.ppinfo[c][r][nPrec].nblk[2] = (kend - kstart + 1) * (lend - lstart + 1);

						for (int k = kstart; k <= kend; k++)
						{ // Vertical cblks
							for (int l = lstart; l <= lend; l++)
							{ // Horiz cblks
								cb = new CBlkCoordInfo(k - k0, l - l0);
								if (l == l0)
								{
									cb.ulx = sb.ulx;
								}
								else
								{
									cb.ulx = sb.ulx + l * cw - (sb.ulcx - acb0x);
								}
								if (k == k0)
								{
									cb.uly = sb.uly;
								}
								else
								{
									cb.uly = sb.uly + k * ch - (sb.ulcy - acb0y);
								}
								tmp1 = acb0x + l * cw;
								tmp1 = (tmp1 > sb.ulcx) ? tmp1 : sb.ulcx;
								tmp2 = acb0x + (l + 1) * cw;
								tmp2 = (tmp2 > sb.ulcx + sb.w) ? sb.ulcx + sb.w : tmp2;
								cb.w = tmp2 - tmp1;
								tmp1 = acb0y + k * ch;
								tmp1 = (tmp1 > sb.ulcy) ? tmp1 : sb.ulcy;
								tmp2 = acb0y + (k + 1) * ch;
								tmp2 = (tmp2 > sb.ulcy + sb.h) ? sb.ulcy + sb.h : tmp2;
								cb.h = tmp2 - tmp1;
								this.ppinfo[c][r][nPrec].cblk[2][k - kstart][l - lstart] = cb;
							} // Horizontal code-blocks
						} // Vertical code-blocks
					}

					// HH subband
					acb0x = 0;
					acb0y = 0;

					p0x = acb0x + j * twoppx2;
					p1x = p0x + twoppx2;
					p0y = acb0y + i * twoppy2;
					p1y = p0y + twoppy2;

					sb = (SubbandSyn) root.getSubbandByIdx(r, 3);
					s0x = (p0x < sb.ulcx) ? sb.ulcx : p0x;
					s1x = (p1x > sb.ulcx + sb.w) ? sb.ulcx + sb.w : p1x;
					s0y = (p0y < sb.ulcy) ? sb.ulcy : p0y;
					s1y = (p1y > sb.ulcy + sb.h) ? sb.ulcy + sb.h : p1y;

					// Code-blocks are located at (acb0x+k*cw,acb0y+l*ch)
					cw = sb.nomCBlkW;
					ch = sb.nomCBlkH;
					k0 = (int) Math.floor((sb.ulcy - acb0y) / (double) ch);
					kstart = (int) Math.floor((s0y - acb0y) / (double) ch);
					kend = (int) Math.floor((s1y - 1 - acb0y) / (double) ch);
					l0 = (int) Math.floor((sb.ulcx - acb0x) / (double) cw);
					lstart = (int) Math.floor((s0x - acb0x) / (double) cw);
					lend = (int) Math.floor((s1x - 1 - acb0x) / (double) cw);

					if (0 >= s1x - s0x || 0 >= s1y - s0y)
					{
						this.ppinfo[c][r][nPrec].nblk[3] = 0;
						this.ttIncl[c][r][nPrec][3] = new TagTreeDecoder(0, 0);
						this.ttMaxBP[c][r][nPrec][3] = new TagTreeDecoder(0, 0);
					}
					else
					{
						this.ttIncl[c][r][nPrec][3] = new TagTreeDecoder(kend - kstart + 1, lend - lstart + 1);
						this.ttMaxBP[c][r][nPrec][3] = new TagTreeDecoder(kend - kstart + 1, lend - lstart + 1);
						this.ppinfo[c][r][nPrec].cblk[3] = new CBlkCoordInfo[kend - kstart + 1][lend - lstart + 1];
						this.ppinfo[c][r][nPrec].nblk[3] = (kend - kstart + 1) * (lend - lstart + 1);

						for (int k = kstart; k <= kend; k++)
						{ // Vertical cblks
							for (int l = lstart; l <= lend; l++)
							{ // Horiz cblks
								cb = new CBlkCoordInfo(k - k0, l - l0);
								if (l == l0)
								{
									cb.ulx = sb.ulx;
								}
								else
								{
									cb.ulx = sb.ulx + l * cw - (sb.ulcx - acb0x);
								}
								if (k == k0)
								{
									cb.uly = sb.uly;
								}
								else
								{
									cb.uly = sb.uly + k * ch - (sb.ulcy - acb0y);
								}
								tmp1 = acb0x + l * cw;
								tmp1 = (tmp1 > sb.ulcx) ? tmp1 : sb.ulcx;
								tmp2 = acb0x + (l + 1) * cw;
								tmp2 = (tmp2 > sb.ulcx + sb.w) ? sb.ulcx + sb.w : tmp2;
								cb.w = tmp2 - tmp1;
								tmp1 = acb0y + k * ch;
								tmp1 = (tmp1 > sb.ulcy) ? tmp1 : sb.ulcy;
								tmp2 = acb0y + (k + 1) * ch;
								tmp2 = (tmp2 > sb.ulcy + sb.h) ? sb.ulcy + sb.h : tmp2;
								cb.h = tmp2 - tmp1;
								this.ppinfo[c][r][nPrec].cblk[3][k - kstart][l - lstart] = cb;
							} // Horizontal code-blocks
						} // Vertical code-blocks
					}

				}
			} // Horizontal precincts
		} // Vertical precincts
	}

	/**
	 * Gets the number of precincts in a given component and resolution level.
	 * 
	 * @param c
	 *            Component index
	 * 
	 * @param r
	 *            Resolution index
	 */
	public int getNumPrecinct(final int c, final int r)
	{
		return this.numPrec[c][r].x * this.numPrec[c][r].y;
	}

	/**
	 * Read specified packet head and found length of each code-block's piece of
	 * codewords as well as number of skipped most significant bit-planes.
	 * 
	 * @param l
	 *            layer index
	 * 
	 * @param r
	 *            Resolution level index
	 * 
	 * @param c
	 *            Component index
	 * 
	 * @param p
	 *            Precinct index
	 * 
	 * @param cbI
	 *            CBlkInfo array of relevant component and resolution level.
	 * 
	 * @param nb
	 *            The number of bytes to read in each tile before reaching
	 *            output rate (used by truncation mode)
	 * 
	 * @return True if specified output rate or EOF is reached.
	 */
	@SuppressWarnings("unchecked")
	public boolean readPktHead(final int l, final int r, final int c, final int p, final CBlkInfo[][][] cbI, final int[] nb) throws IOException
	{

		CBlkInfo ccb;
		int nSeg; // number of segment to read
		int cbLen; // Length of cblk's code-words
		int ltp; // last truncation point index
		int passtype; // coding pass type
		TagTreeDecoder tdIncl, tdBD;
		int tmp, tmp2, totnewtp, lblockCur, tpidx;
//		int sumtotnewtp = 0;
		Coord cbc;
		final int startPktHead = this.ehs.getPos();
		if (startPktHead >= this.ehs.length())
		{
			// EOF reached at the beginning of this packet head
			return true;
		}
		final int tIdx = this.src.getTileIdx();
		final PktHeaderBitReader bin;
		int mend, nend;
		// int b;
		// SubbandSyn sb;
		// SubbandSyn root = src.getSynSubbandTree(tIdx, c);

		// If packed packet headers was used, use separate stream for reading
		// of packet headers
		if (this.pph)
		{
			bin = new PktHeaderBitReader(this.pphbais);
		}
		else
		{
			bin = this.bin;
		}

		final int mins = (0 == r) ? 0 : 1;
		final int maxs = (0 == r) ? 1 : 4;

		boolean precFound = false;
		for (int s = mins; s < maxs; s++)
		{
			if (p < this.ppinfo[c][r].length) {
				precFound = true;
				break;
			}
		}
		if (!precFound)
		{
			return false;
		}

		final PrecInfo prec = this.ppinfo[c][r][p];

		// Synchronize for bit reading
		bin.sync();

		// If packet is empty there is no info in it (i.e. no code-blocks)
		if (0 == bin.readBit())
		{
			// No code-block is included
			this.cblks = new Vector[maxs + 1];
			for (int s = mins; s < maxs; s++)
			{
				this.cblks[s] = new Vector<CBlkCoordInfo>();
			}
			this.pktIdx++;

			// If truncation mode, checks if output rate is reached
			// unless ncb quit condition is used in which case headers
			// are not counted
			if (this.isTruncMode && -1 == maxCB)
			{
				tmp = this.ehs.getPos() - startPktHead;
				if (tmp > nb[tIdx])
				{
					nb[tIdx] = 0;
					return true;
				}
				nb[tIdx] -= tmp;
			}

			// Read EPH marker if needed
			if (this.ephUsed)
			{
				this.readEPHMarker(bin);
			}
			return false;
		}

		// Packet is not empty => decode info
		// Loop on each subband in this resolution level
		if (null == cblks || this.cblks.length < maxs + 1)
		{
			this.cblks = new Vector[maxs + 1];
		}

		for (int s = mins; s < maxs; s++)
		{
			if (null == cblks[s])
			{
				this.cblks[s] = new Vector<CBlkCoordInfo>();
			}
			else
			{
				this.cblks[s].removeAllElements();
			}
			// sb = (SubbandSyn) root.getSubbandByIdx(r, s);
			// No code-block in this precinct
			if (0 == prec.nblk[s])
			{
				// Go to next subband
				continue;
			}

			tdIncl = this.ttIncl[c][r][p][s];
			tdBD = this.ttMaxBP[c][r][p][s];

			mend = (null == prec.cblk[s]) ? 0 : prec.cblk[s].length;
			for (int m = 0; m < mend; m++)
			{ // Vertical code-blocks
				nend = (null == prec.cblk[s][m]) ? 0 : prec.cblk[s][m].length;
				for (int n = 0; n < nend; n++)
				{ // Horizontal code-blocks
					cbc = prec.cblk[s][m][n].idx;
					// b = cbc.x + cbc.y * sb.numCb.x;

					ccb = cbI[s][cbc.y][cbc.x];

					try
					{
						// If code-block not included in previous layer(s)
						if (null == ccb || 0 == ccb.ctp)
						{
							if (null == ccb)
							{
								ccb = cbI[s][cbc.y][cbc.x] = new CBlkInfo(prec.cblk[s][m][n].ulx,
										prec.cblk[s][m][n].uly, prec.cblk[s][m][n].w, prec.cblk[s][m][n].h, this.nl);
							}
							ccb.pktIdx[l] = this.pktIdx;

							// Read inclusion using tag-tree
							tmp = tdIncl.update(m, n, l + 1, bin);
							if (tmp > l)
							{ // Not included
								continue;
							}

							// Read bitdepth using tag-tree
							tmp = 1;// initialization
							for (tmp2 = 1; tmp >= tmp2; tmp2++)
							{
								tmp = tdBD.update(m, n, tmp2, bin);
							}
							ccb.msbSkipped = tmp2 - 2;

							// New code-block => at least one truncation point
							totnewtp = 1;
							ccb.addNTP(l, 0);

							// Check whether ncb quit condition is reached
							this.ncb++;

							if (-1 != maxCB && !this.ncbQuit && this.ncb == this.maxCB)
							{
								// ncb quit contidion reached
								this.ncbQuit = true;
								this.tQuit = tIdx;
								this.cQuit = c;
								this.sQuit = s;
								this.rQuit = r;
								this.xQuit = cbc.x;
								this.yQuit = cbc.y;
							}

						}
						else
						{ // If code-block already included in one of
							// the previous layers.

							ccb.pktIdx[l] = this.pktIdx;

							// If not inclused
							if (1 != bin.readBit())
							{
								continue;
							}

							// At least 1 more truncation point than
							// prev. packet
							totnewtp = 1;
						}

						// Read new truncation points
						if (1 == bin.readBit())
						{// if bit is 1
							totnewtp++;

							// if next bit is 0 do nothing
							if (1 == bin.readBit())
							{// if is 1
								totnewtp++;

								tmp = bin.readBits(2);
								totnewtp += tmp;

								// If next 2 bits are not 11 do nothing
								if (0x3 == tmp)
								{ // if 11
									tmp = bin.readBits(5);
									totnewtp += tmp;

									// If next 5 bits are not 11111 do nothing
									if (0x1F == tmp)
									{ // if 11111
										totnewtp += bin.readBits(7);
									}
								}
							}
						}
						ccb.addNTP(l, totnewtp);
//						sumtotnewtp += totnewtp;
						this.cblks[s].addElement(prec.cblk[s][m][n]);

						// Code-block length

						// -- Compute the number of bit to read to obtain
						// code-block length.
						// numBits = betaLamda + log2(totnewtp);

						// The length is signalled for each segment in
						// addition to the final one. The total length is the
						// sum of all segment lengths.

						// If regular termination in use, then there is one
						// segment per truncation point present. Otherwise, if
						// selective arithmetic bypass coding mode is present,
						// then there is one termination per bypass/MQ and
						// MQ/bypass transition. Otherwise the only
						// termination is at the end of the code-block.
						final int options = ((Integer) this.decSpec.ecopts.getTileCompVal(tIdx, c)).intValue();

						if (0 != (options & OPT_TERM_PASS))
						{
							// Regular termination in use, one segment per new
							// pass (i.e. truncation point)
							nSeg = totnewtp;
						}
						else if (0 != (options & OPT_BYPASS))
						{
							// Selective arithmetic coding bypass coding mode
							// in use, but no regular termination 1 segment up
							// to the end of the last pass of the 4th most
							// significant bit-plane, and, in each following
							// bit-plane, one segment upto the end of the 2nd
							// pass and one upto the end of the 3rd pass.

							if (FIRST_BYPASS_PASS_IDX >= ccb.ctp)
							{
								nSeg = 1;
							}
							else
							{
								nSeg = 1; // One at least for last pass
								// And one for each other terminated pass
								for (tpidx = ccb.ctp - totnewtp; tpidx < ccb.ctp - 1; tpidx++)
								{
									if (FIRST_BYPASS_PASS_IDX - 1 <= tpidx)
									{
										passtype = (tpidx + StdEntropyCoderOptions.NUM_EMPTY_PASSES_IN_MS_BP) % StdEntropyCoderOptions.NUM_PASSES;
										if (1 == passtype || 2 == passtype)
										{
											// bypass coding just before MQ
											// pass or MQ pass just before
											// bypass coding => terminated
											nSeg++;
										}
									}
								}
							}
						}
						else
						{
							// Nothing special in use, just one segment
							nSeg = 1;
						}

						// Reads lblock increment (common to all segments)
						while (0 != bin.readBit())
						{
							this.lblock[c][r][s][cbc.y][cbc.x]++;
						}

						if (1 == nSeg)
						{ // Only one segment in packet
							cbLen = bin.readBits(this.lblock[c][r][s][cbc.y][cbc.x] + MathUtil.log2(totnewtp));
						}
						else
						{
							// We must read one length per segment
							ccb.segLen[l] = new int[nSeg];
							cbLen = 0;
							int j;
							if (0 != (options & OPT_TERM_PASS))
							{
								// Regular termination: each pass is terminated
								for (tpidx = ccb.ctp - totnewtp, j = 0; tpidx < ccb.ctp; tpidx++, j++)
								{

									lblockCur = this.lblock[c][r][s][cbc.y][cbc.x];

									tmp = bin.readBits(lblockCur);
									ccb.segLen[l][j] = tmp;
									cbLen += tmp;
								}
							}
							else
							{
								// Bypass coding: only some passes are
								// terminated
								ltp = ccb.ctp - totnewtp - 1;
								for (tpidx = ccb.ctp - totnewtp, j = 0; tpidx < ccb.ctp - 1; tpidx++)
								{
									if (FIRST_BYPASS_PASS_IDX - 1 <= tpidx)
									{
										passtype = (tpidx + StdEntropyCoderOptions.NUM_EMPTY_PASSES_IN_MS_BP) % StdEntropyCoderOptions.NUM_PASSES;
										if (0 == passtype)
											continue;

										lblockCur = this.lblock[c][r][s][cbc.y][cbc.x];
										tmp = bin.readBits(lblockCur + MathUtil.log2(tpidx - ltp));
										ccb.segLen[l][j] = tmp;
										cbLen += tmp;
										ltp = tpidx;
										j++;
									}
								}
								// Last pass has always the length sent
								lblockCur = this.lblock[c][r][s][cbc.y][cbc.x];
								tmp = bin.readBits(lblockCur + MathUtil.log2(tpidx - ltp));
								cbLen += tmp;
								ccb.segLen[l][j] = tmp;
							}
						}
						ccb.len[l] = cbLen;

						// If truncation mode, checks if output rate is reached
						// unless ncb and lbody quit contitions used.
						if (this.isTruncMode && -1 == maxCB)
						{
							tmp = this.ehs.getPos() - startPktHead;
							if (tmp > nb[tIdx])
							{
								nb[tIdx] = 0;
								// Remove found information in this code-block
								if (0 == l)
								{
									cbI[s][cbc.y][cbc.x] = null;
								}
								else
								{
									ccb.off[l] = ccb.len[l] = 0;
									ccb.ctp -= ccb.ntp[l];
									ccb.ntp[l] = 0;
									ccb.pktIdx[l] = -1;
								}
								return true;
							}
						}

					}
					catch (final EOFException e)
					{
						// Remove found information in this code-block
						if (0 == l)
						{
							cbI[s][cbc.y][cbc.x] = null;
						}
						else
						{
							ccb.off[l] = ccb.len[l] = 0;
							ccb.ctp -= ccb.ntp[l];
							ccb.ntp[l] = 0;
							ccb.pktIdx[l] = -1;
						}
						// throw new EOFException();
						return true;
					}
				} // End loop on horizontal code-blocks
			} // End loop on vertical code-blocks
		} // End loop on subbands

		// Read EPH marker if needed
		if (this.ephUsed)
		{
			this.readEPHMarker(bin);
		}

		this.pktIdx++;

		// If truncation mode, checks if output rate is reached
		if (this.isTruncMode && -1 == maxCB)
		{
			tmp = this.ehs.getPos() - startPktHead;
			if (tmp > nb[tIdx])
			{
				nb[tIdx] = 0;
				return true;
			}
			nb[tIdx] -= tmp;
		}
		return false;
	}

	/**
	 * Reads specificied packet body in order to find offset of each
	 * code-block's piece of codeword. This use the list of found code-blocks in
	 * previous red packet head.
	 * 
	 * @param l
	 *            layer index
	 * 
	 * @param r
	 *            Resolution level index
	 * 
	 * @param c
	 *            Component index
	 * 
	 * @param p
	 *            Precinct index
	 * 
	 * @param cbI
	 *            CBlkInfo array of relevant component and resolution level.
	 * 
	 * @param nb
	 *            The remainding number of bytes to read from the bit stream in
	 *            each tile before reaching the decoding rate (in truncation
	 *            mode)
	 * 
	 * @return True if decoding rate is reached
	 */
	public boolean readPktBody(final int l, final int r, final int c, final int p, final CBlkInfo[][][] cbI, final int[] nb) throws IOException
	{
		int curOff = this.ehs.getPos();
		CBlkInfo ccb;
		boolean stopRead = false;
		final int tIdx = this.src.getTileIdx();
		Coord cbc;

		boolean precFound = false;
		final int mins = (0 == r) ? 0 : 1;
		final int maxs = (0 == r) ? 1 : 4;
		for (int s = mins; s < maxs; s++)
		{
			if (p < this.ppinfo[c][r].length) {
				precFound = true;
				break;
			}
		}
		if (!precFound)
		{
			return false;
		}

		for (int s = mins; s < maxs; s++)
		{
			for (int numCB = 0; numCB < this.cblks[s].size(); numCB++)
			{
				cbc = this.cblks[s].elementAt(numCB).idx;
				ccb = cbI[s][cbc.y][cbc.x];
				ccb.off[l] = curOff;
				curOff += ccb.len[l];
				try
				{
					this.ehs.seek(curOff);
				}
				catch (final EOFException e)
				{
					if (0 == l)
					{
						cbI[s][cbc.y][cbc.x] = null;
					}
					else
					{
						ccb.off[l] = ccb.len[l] = 0;
						ccb.ctp -= ccb.ntp[l];
						ccb.ntp[l] = 0;
						ccb.pktIdx[l] = -1;
					}
					throw new EOFException();
				}

				// If truncation mode
				if (this.isTruncMode)
				{
					if (stopRead || ccb.len[l] > nb[tIdx])
					{
						// Remove found information in this code-block
						if (0 == l)
						{
							cbI[s][cbc.y][cbc.x] = null;
						}
						else
						{
							ccb.off[l] = ccb.len[l] = 0;
							ccb.ctp -= ccb.ntp[l];
							ccb.ntp[l] = 0;
							ccb.pktIdx[l] = -1;
						}
						stopRead = true;
					}
					if (!stopRead)
					{
						nb[tIdx] -= ccb.len[l];
					}
				}
				// If ncb quit condition reached
				if (this.ncbQuit && r == this.rQuit && s == this.sQuit && cbc.x == this.xQuit && cbc.y == this.yQuit && tIdx == this.tQuit
						&& c == this.cQuit)
				{
					cbI[s][cbc.y][cbc.x] = null;
					stopRead = true;
				}
			} // Loop on code-blocks
		} // End loop on subbands

		// Seek to the end of the packet
		this.ehs.seek(curOff);

		return stopRead;
	}

	/**
	 * Returns the precinct partition width for the specified component,
	 * resolution level and tile.
	 * 
	 * @param t
	 *            the tile index
	 * 
	 * @param c
	 *            The index of the component (between 0 and C-1)
	 * 
	 * @param r
	 *            The resolution level, from 0 to L.
	 * 
	 * @return the precinct partition width for the specified component,
	 *         resolution level and tile.
	 */
	public final int getPPX(final int t, final int c, final int r)
	{
		return this.decSpec.pss.getPPX(t, c, r);
	}

	/**
	 * Returns the precinct partition height for the specified component,
	 * resolution level and tile.
	 * 
	 * @param t
	 *            the tile index
	 * 
	 * @param c
	 *            The index of the component (between 0 and C-1)
	 * 
	 * @param rl
	 *            The resolution level, from 0 to L.
	 * 
	 * @return the precinct partition height in the specified component, for the
	 *         specified resolution level, for the current tile.
	 */
	public final int getPPY(final int t, final int c, final int rl)
	{
		return this.decSpec.pss.getPPY(t, c, rl);
	}

	/**
	 * Try to read a SOP marker and check that its sequence number if not out of
	 * sequence. If so, an error is thrown.
	 * 
	 * @param nBytes
	 *            The number of bytes left to read from each tile
	 * 
	 * @param p
	 *            Precinct index
	 * 
	 * @param r
	 *            Resolution level index
	 * 
	 * @param c
	 *            Component index
	 */
	public boolean readSOPMarker(final int[] nBytes, final int p, final int c, final int r) throws IOException
	{
		int val;
		final byte[] sopArray = new byte[6];
		final int tIdx = this.src.getTileIdx();
		final int mins = (0 == r) ? 0 : 1;
		final int maxs = (0 == r) ? 1 : 4;
		boolean precFound = false;
		for (int s = mins; s < maxs; s++)
		{
			if (p < this.ppinfo[c][r].length) {
				precFound = true;
				break;
			}
		}
		if (!precFound)
		{
			return false;
		}

		// If SOP markers are not used, return
		if (!this.sopUsed)
		{
			return false;
		}

		// Check if SOP is used for this packet
		final int pos = this.ehs.getPos();
		if (Markers.SOP != (short) ((ehs.read() << 8) | ehs.read()))
		{
			this.ehs.seek(pos);
			return false;
		}
		this.ehs.seek(pos);

		// If length of SOP marker greater than remaining bytes to read for
		// this tile return true
		if (6 > nBytes[tIdx])
		{
			return true;
		}
		nBytes[tIdx] -= 6;

		// Read marker into array 'sopArray'
		this.ehs.readFully(sopArray, 0, Markers.SOP_LENGTH);

		// Check if this is the correct marker
		val = sopArray[0];
		val <<= 8;
		val |= sopArray[1];
		if (Markers.SOP != val)
		{
			throw new Error("Corrupted Bitstream: Could not parse SOP marker !");
		}

		// Check if length is correct
		val = (sopArray[2] & 0xff);
		val <<= 8;
		val |= (sopArray[3] & 0xff);
		if (4 != val)
		{
			throw new Error("Corrupted Bitstream: Corrupted SOP marker !");
		}

		// Check if sequence number if ok
		val = (sopArray[4] & 0xff);
		val <<= 8;
		val |= (sopArray[5] & 0xff);

		if (!this.pph && val != this.pktIdx)
		{
			throw new Error("Corrupted Bitstream: SOP marker out of sequence !");
		}
		if (this.pph && val != this.pktIdx - 1)
		{
			// if packed packet headers are used, packet header was read
			// before SOP marker segment
			throw new Error("Corrupted Bitstream: SOP marker out of sequence !");
		}
		return false;
	}

	/**
	 * Try to read an EPH marker. If it is not possible then an Error is thrown.
	 * 
	 * @param bin
	 *            The packet header reader to read the EPH marker from
	 */
	public void readEPHMarker(final PktHeaderBitReader bin) throws IOException
	{
		int val;
		final byte[] ephArray = new byte[2];

		if (bin.usebais)
		{
			bin.bais.read(ephArray, 0, Markers.EPH_LENGTH);
		}
		else
		{
			bin.in.readFully(ephArray, 0, Markers.EPH_LENGTH);
		}

		// Check if this is the correct marker
		val = ephArray[0];
		val <<= 8;
		val |= ephArray[1];
		if (Markers.EPH != val)
		{
			throw new Error("Corrupted Bitstream: Could not parse EPH marker ! ");
		}
	}

	/**
	 * Get PrecInfo instance of the specified resolution level, component and
	 * precinct.
	 * 
	 * @param c
	 *            Component index.
	 * 
	 * @param r
	 *            Resolution level index.
	 * 
	 * @param p
	 *            Precinct index.
	 */
	public PrecInfo getPrecInfo(final int c, final int r, final int p)
	{
		return this.ppinfo[c][r][p];
	}
}
