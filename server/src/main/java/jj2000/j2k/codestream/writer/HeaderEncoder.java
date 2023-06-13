/*
 * CVS identifier:
 *
 * $Id: HeaderEncoder.java,v 1.43 2001/10/12 09:02:14 grosbois Exp $
 *
 * Class:                   HeaderEncoder
 *
 * Description:             Write codestream headers.
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
package jj2000.j2k.codestream.writer;

import jj2000.j2k.quantization.quantizer.*;
import jj2000.j2k.wavelet.analysis.*;
import jj2000.j2k.entropy.encoder.*;
import jj2000.j2k.roi.encoder.*;
import jj2000.j2k.codestream.*;
import jj2000.j2k.encoder.*;
import jj2000.j2k.entropy.*;
import jj2000.j2k.image.*;
import jj2000.j2k.util.*;
import jj2000.j2k.*;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.io.*;

/**
 * This class writes most of the markers and marker segments in main header and
 * in tile-part headers. It is created by the run() method of the Encoder
 * instance.
 * 
 * <p>
 * A marker segment includes a marker and eventually marker segment parameters.
 * It is designed by the three letter code of the marker associated with the
 * marker segment. JPEG 2000 part I defines 6 types of markers:
 * <ul>
 * <li>Delimiting : SOC,SOT,SOD,EOC (written in FileCodestreamWriter).</li>
 * <li>Fixed information: SIZ.</li>
 * <li>Functional: COD,COC,RGN,QCD,QCC,POC.</li>
 * <li>In bit-stream: SOP,EPH.</li>
 * <li>Pointer: TLM,PLM,PLT,PPM,PPT.</li>
 * <li>Informational: CRG,COM.</li>
 * </ul>
 * 
 * <p>
 * Main Header is written when Encoder instance calls encodeMainHeader whereas
 * tile-part headers are written when the EBCOTRateAllocator instance calls
 * encodeTilePartHeader.
 * 
 * @see Encoder
 * @see Markers
 * @see EBCOTRateAllocator
 */
public class HeaderEncoder implements Markers, StdEntropyCoderOptions
{

	/** The prefix for the header encoder options: 'H' */
	public static final char OPT_PREFIX = 'H';

	/**
	 * The list of parameters that are accepted for the header encoder module.
	 * Options for this modules start with 'H'.
	 */
	private static final String[][] pinfo = {
			{ "Hjj2000_COM", null, "Writes or not the JJ2000 COM marker in the codestream", "on" },
			{
					"HCOM",
					"<Comment 1>[#<Comment 2>[#<Comment3...>]]",
					"Adds COM marker segments in the codestream. Comments must be "
							+ "separated with '#' and are written into distinct maker segments.", null } };

	/**
	 * Nominal range bit of the component defining default values in QCD for
	 * main header
	 */
	private int defimgn;

	/**
	 * Nominal range bit of the component defining default values in QCD for
	 * tile headers
	 */
	private int deftilenr;

	/** The number of components in the image */
	private final int nComp;

	/** Whether or not to write the JJ2000 COM marker segment */
	private boolean enJJ2KMarkSeg = true;

	/** Other COM marker segments specified in the command line */
	private String otherCOMMarkSeg;

	/**
	 * The ByteArrayOutputStream to store header data. This handler is kept in
	 * order to use methods not accessible from a general DataOutputStream. For
	 * the other methods, it's better to use variable hbuf.
	 * 
	 * @see #hbuf
	 */
	protected ByteArrayOutputStream baos;

	/**
	 * The DataOutputStream to store header data. This kind of object is useful
	 * to write short, int, .... It's constructor takes baos as parameter.
	 * 
	 * @see #baos
	 */
	protected DataOutputStream hbuf;

	/** The image data reader. Source of original data info */
	protected ImgData origSrc;

	/**
	 * An array specifying, for each component,if the data was signed or not
	 */
	protected boolean[] isOrigSig;

	/** Reference to the rate allocator */
	protected PostCompRateAllocator ralloc;

	/** Reference to the DWT module */
	protected ForwardWT dwt;

	/** Reference to the tiler module */
	protected Tiler tiler;

	/** Reference to the ROI module */
	protected ROIScaler roiSc;

	/** The encoder specifications */
	protected EncoderSpecs encSpec;

	/**
	 * Returns the parameters that are used in this class and implementing
	 * classes. It returns a 2D String array. Each of the 1D arrays is for a
	 * different option, and they have 3 elements. The first element is the
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
		return HeaderEncoder.pinfo;
	}

	/**
	 * Initializes the header writer with the references to the coding chain.
	 * 
	 * @param origsrc
	 *            The original image data (before any component mixing, tiling,
	 *            etc.)
	 * 
	 * @param isorigsig
	 *            An array specifying for each component if it was originally
	 *            signed or not.
	 * 
	 * @param dwt
	 *            The discrete wavelet transform module.
	 * 
	 * @param tiler
	 *            The tiler module.
	 * 
	 * @param encSpec
	 *            The encoder specifications
	 * 
	 * @param roiSc
	 *            The ROI scaler module.
	 * 
	 * @param ralloc
	 *            The post compression rate allocator.
	 * 
	 * @param pl
	 *            ParameterList instance.
	 */
	public HeaderEncoder(final ImgData origsrc, final boolean[] isorigsig, final ForwardWT dwt, final Tiler tiler, final EncoderSpecs encSpec,
						 final ROIScaler roiSc, final PostCompRateAllocator ralloc, final ParameterList pl)
	{
		pl.checkList(HeaderEncoder.OPT_PREFIX, ParameterList.toNameArray(HeaderEncoder.pinfo));
		if (origsrc.getNumComps() != isorigsig.length)
		{
			throw new IllegalArgumentException();
		}
		origSrc = origsrc;
		isOrigSig = isorigsig;
		this.dwt = dwt;
		this.tiler = tiler;
		this.encSpec = encSpec;
		this.roiSc = roiSc;
		this.ralloc = ralloc;

		this.baos = new ByteArrayOutputStream();
		this.hbuf = new DataOutputStream(this.baos);
		this.nComp = origsrc.getNumComps();
		this.enJJ2KMarkSeg = pl.getBooleanParameter("Hjj2000_COM");
		this.otherCOMMarkSeg = pl.getParameter("HCOM");
	}

	/**
	 * Resets the contents of this HeaderEncoder to its initial state. It erases
	 * all the data in the header buffer and reactualizes the headerLength field
	 * of the bit stream writer.
	 */
	public void reset()
	{
		this.baos.reset();
		this.hbuf = new DataOutputStream(this.baos);
	}

	/**
	 * Returns the byte-buffer used to store the codestream header.
	 * 
	 * @return A byte array countaining codestream header
	 */
	protected byte[] getBuffer()
	{
		return this.baos.toByteArray();
	}

	/**
	 * Returns the length of the header.
	 * 
	 * @return The length of the header in bytes
	 */
	public int getLength()
	{
		return this.hbuf.size();
	}

	/**
	 * Writes the header to the specified BinaryDataOutput.
	 * 
	 * @param out
	 *            Where to write the header.
	 */
	public int writeTo(final OutputStream out) throws IOException
	{
		final byte[] buf;

		buf = this.getBuffer();

		out.write(buf, 0, this.getLength());
		return this.getLength();
	}

	/**
	 * Returns the number of bytes used in the codestream header's buffer.
	 * 
	 * @return Header length in buffer (without any header overhead)
	 */
	protected int getBufferLength()
	{
		return this.baos.size();
	}

	/**
	 * Start Of Codestream marker (SOC) signalling the beginning of a
	 * codestream.
	 */
	private void writeSOC() throws IOException
	{
		this.hbuf.writeShort(Markers.SOC);
	}

	/**
	 * Writes SIZ marker segment of the codestream header. It is a fixed
	 * information marker segment containing informations about image and tile
	 * sizes. It is required in the main header immediately after SOC marker
	 * segment.
	 */
	private void writeSIZ() throws IOException
	{
		int tmp;

		// SIZ marker
		this.hbuf.writeShort(Markers.SIZ);

		// Lsiz (Marker length) corresponding to
		// Lsiz(2 bytes)+Rsiz(2)+Xsiz(4)+Ysiz(4)+XOsiz(4)+YOsiz(4)+
		// XTsiz(4)+YTsiz(4)+XTOsiz(4)+YTOsiz(4)+Csiz(2)+
		// (Ssiz(1)+XRsiz(1)+YRsiz(1))*nComp
		// markSegLen = 38 + 3*nComp;
		final int markSegLen = 38 + 3 * this.nComp;
		this.hbuf.writeShort(markSegLen);

		// Rsiz (codestream capabilities)
		this.hbuf.writeShort(0); // JPEG 2000 - Part I

		// Xsiz (original image width)
		this.hbuf.writeInt(this.tiler.getImgWidth() + this.tiler.getImgULX());

		// Ysiz (original image height)
		this.hbuf.writeInt(this.tiler.getImgHeight() + this.tiler.getImgULY());

		// XOsiz (horizontal offset from the origin of the reference
		// grid to the left side of the image area)
		this.hbuf.writeInt(this.tiler.getImgULX());

		// YOsiz (vertical offset from the origin of the reference
		// grid to the top side of the image area)
		this.hbuf.writeInt(this.tiler.getImgULY());

		// XTsiz (nominal tile width)
		this.hbuf.writeInt(this.tiler.getNomTileWidth());

		// YTsiz (nominal tile height)
		this.hbuf.writeInt(this.tiler.getNomTileHeight());

		final Coord torig = this.tiler.getTilingOrigin(null);
		// XTOsiz (Horizontal offset from the origin of the reference
		// grid to the left side of the first tile)
		this.hbuf.writeInt(torig.x);

		// YTOsiz (Vertical offset from the origin of the reference
		// grid to the top side of the first tile)
		this.hbuf.writeInt(torig.y);

		// Csiz (number of components)
		this.hbuf.writeShort(this.nComp);

		// Bit-depth and downsampling factors.
		for (int c = 0; c < this.nComp; c++)
		{ // Loop on each component

			// Ssiz bit-depth before mixing
			tmp = this.origSrc.getNomRangeBits(c) - 1;

			tmp |= ((this.isOrigSig[c] ? 1 : 0) << Markers.SSIZ_DEPTH_BITS);
			this.hbuf.write(tmp);

			// XRsiz (component sub-sampling value x-wise)
			this.hbuf.write(this.tiler.getCompSubsX(c));

			// YRsiz (component sub-sampling value y-wise)
			this.hbuf.write(this.tiler.getCompSubsY(c));

		} // End loop on each component

	}

	/**
	 * Writes COD marker segment. COD is a functional marker segment containing
	 * the code style default (coding style, decomposition, layering) used for
	 * compressing all the components in an image.
	 * 
	 * <p>
	 * The values can be overriden for an individual component by a COC marker
	 * in either the main or the tile header.
	 * 
	 * @param mh
	 *            Flag indicating whether this marker belongs to the main header
	 * 
	 * @param tileIdx
	 *            Tile index if the marker belongs to a tile-part header
	 * 
	 * @see #writeCOC
	 */
	@SuppressWarnings("unchecked")
	protected void writeCOD(final boolean mh, final int tileIdx) throws IOException
	{
		final AnWTFilter[][] filt;
		final boolean precinctPartitionUsed;
		int tmp;
		int mrl = 0, a = 0;
		int ppx = 0, ppy = 0;
		final Progression[] prog;

		if (mh)
		{
			mrl = ((Integer) this.encSpec.dls.getDefault()).intValue();
			// get default precinct size
			ppx = this.encSpec.pss.getPPX(-1, -1, mrl);
			ppy = this.encSpec.pss.getPPY(-1, -1, mrl);
			prog = (Progression[]) (this.encSpec.pocs.getDefault());
		}
		else
		{
			mrl = ((Integer) this.encSpec.dls.getTileDef(tileIdx)).intValue();
			// get precinct size for specified tile
			ppx = this.encSpec.pss.getPPX(tileIdx, -1, mrl);
			ppy = this.encSpec.pss.getPPY(tileIdx, -1, mrl);
			prog = (Progression[]) (this.encSpec.pocs.getTileDef(tileIdx));
		}

		precinctPartitionUsed = PRECINCT_PARTITION_DEF_SIZE != ppx || PRECINCT_PARTITION_DEF_SIZE != ppy;

		if (precinctPartitionUsed)
		{
			// If precinct partition is used we add one byte per resolution
			// level i.e. mrl+1 (+1 for resolution 0).
			a = mrl + 1;
		}

		// Write COD marker
		this.hbuf.writeShort(Markers.COD);

		// Lcod (marker segment length (in bytes)) Basic : Lcod(2
		// bytes)+Scod(1)+SGcod(4)+SPcod(5+a) where:
		// a=0 if no precinct partition is used
		// a=mrl+1 if precinct partition used
		final int markSegLen = 12 + a;
		this.hbuf.writeShort(markSegLen);

		// Scod (coding style parameter)
		tmp = 0;
		if (precinctPartitionUsed)
		{
			tmp = Markers.SCOX_PRECINCT_PARTITION;
		}

		// Are SOP markers used ?
		if (mh)
		{
			if ("on".equalsIgnoreCase(encSpec.sops.getDefault().toString()))
			{
				tmp |= Markers.SCOX_USE_SOP;
			}
		}
		else
		{
			if ("on".equalsIgnoreCase(encSpec.sops.getTileDef(tileIdx).toString()))
			{
				tmp |= Markers.SCOX_USE_SOP;
			}
		}

		// Are EPH markers used ?
		if (mh)
		{
			if ("on".equalsIgnoreCase(encSpec.ephs.getDefault().toString()))
			{
				tmp |= Markers.SCOX_USE_EPH;
			}
		}
		else
		{
			if ("on".equalsIgnoreCase(encSpec.ephs.getTileDef(tileIdx).toString()))
			{
				tmp |= Markers.SCOX_USE_EPH;
			}
		}
		if (0 != dwt.getCbULX())
			tmp |= Markers.SCOX_HOR_CB_PART;
		if (0 != dwt.getCbULY())
			tmp |= Markers.SCOX_VER_CB_PART;
		this.hbuf.write(tmp);

		// SGcod
		// Progression order
		this.hbuf.write(prog[0].type);

		// Number of layers
		this.hbuf.writeShort(this.ralloc.getNumLayers());

		// Multiple component transform
		// CSsiz (Color transform)
		String str = null;
		if (mh)
		{
			str = (String) this.encSpec.cts.getDefault();
		}
		else
		{
			str = (String) this.encSpec.cts.getTileDef(tileIdx);
		}

		if ("none".equals(str))
		{
			this.hbuf.write(0);
		}
		else
		{
			this.hbuf.write(1);
		}

		// SPcod
		// Number of decomposition levels
		this.hbuf.write(mrl);

		// Code-block width and height
		if (mh)
		{
			// main header, get default values
			tmp = this.encSpec.cblks.getCBlkWidth(ModuleSpec.SPEC_DEF, -1, -1);
			this.hbuf.write(MathUtil.log2(tmp) - 2);
			tmp = this.encSpec.cblks.getCBlkHeight(ModuleSpec.SPEC_DEF, -1, -1);
			this.hbuf.write(MathUtil.log2(tmp) - 2);
		}
		else
		{
			// tile header, get tile default values
			tmp = this.encSpec.cblks.getCBlkWidth(ModuleSpec.SPEC_TILE_DEF, tileIdx, -1);
			this.hbuf.write(MathUtil.log2(tmp) - 2);
			tmp = this.encSpec.cblks.getCBlkHeight(ModuleSpec.SPEC_TILE_DEF, tileIdx, -1);
			this.hbuf.write(MathUtil.log2(tmp) - 2);
		}

		// Style of the code-block coding passes
		tmp = 0;
		if (mh)
		{ // Main header
			// Selective arithmetic coding bypass ?
			if (this.encSpec.bms.getDefault().equals("on"))
			{
				tmp |= StdEntropyCoderOptions.OPT_BYPASS;
			}
			// MQ reset after each coding pass ?
			if (this.encSpec.mqrs.getDefault().equals("on"))
			{
				tmp |= StdEntropyCoderOptions.OPT_RESET_MQ;
			}
			// MQ termination after each arithmetically coded coding pass ?
			if (this.encSpec.rts.getDefault().equals("on"))
			{
				tmp |= StdEntropyCoderOptions.OPT_TERM_PASS;
			}
			// Vertically stripe-causal context mode ?
			if (this.encSpec.css.getDefault().equals("on"))
			{
				tmp |= StdEntropyCoderOptions.OPT_VERT_STR_CAUSAL;
			}
			// Predictable termination ?
			if (this.encSpec.tts.getDefault().equals("predict"))
			{
				tmp |= StdEntropyCoderOptions.OPT_PRED_TERM;
			}
			// Error resilience segmentation symbol insertion ?
			if (this.encSpec.sss.getDefault().equals("on"))
			{
				tmp |= StdEntropyCoderOptions.OPT_SEG_SYMBOLS;
			}
		}
		else
		{ // Tile header
			// Selective arithmetic coding bypass ?
			if (this.encSpec.bms.getTileDef(tileIdx).equals("on"))
			{
				tmp |= StdEntropyCoderOptions.OPT_BYPASS;
			}
			// MQ reset after each coding pass ?
			if (this.encSpec.mqrs.getTileDef(tileIdx).equals("on"))
			{
				tmp |= StdEntropyCoderOptions.OPT_RESET_MQ;
			}
			// MQ termination after each arithmetically coded coding pass ?
			if (this.encSpec.rts.getTileDef(tileIdx).equals("on"))
			{
				tmp |= StdEntropyCoderOptions.OPT_TERM_PASS;
			}
			// Vertically stripe-causal context mode ?
			if (this.encSpec.css.getTileDef(tileIdx).equals("on"))
			{
				tmp |= StdEntropyCoderOptions.OPT_VERT_STR_CAUSAL;
			}
			// Predictable termination ?
			if (this.encSpec.tts.getTileDef(tileIdx).equals("predict"))
			{
				tmp |= StdEntropyCoderOptions.OPT_PRED_TERM;
			}
			// Error resilience segmentation symbol insertion ?
			if (this.encSpec.sss.getTileDef(tileIdx).equals("on"))
			{
				tmp |= StdEntropyCoderOptions.OPT_SEG_SYMBOLS;
			}
		}
		this.hbuf.write(tmp);

		// Wavelet transform
		// Wavelet Filter
		if (mh)
		{
			filt = ((AnWTFilter[][]) this.encSpec.wfs.getDefault());
			this.hbuf.write(filt[0][0].getFilterType());
		}
		else
		{
			filt = ((AnWTFilter[][]) this.encSpec.wfs.getTileDef(tileIdx));
			this.hbuf.write(filt[0][0].getFilterType());
		}

		// Precinct partition
		if (precinctPartitionUsed)
		{
			// Write the precinct size for each resolution level + 1
			// (resolution 0) if precinct partition is used.
			Vector<Integer>[] v = null;
			if (mh)
			{
				v = (Vector<Integer>[]) this.encSpec.pss.getDefault();
			}
			else
			{
				v = (Vector<Integer>[]) this.encSpec.pss.getTileDef(tileIdx);
			}
			for (int r = mrl; 0 <= r; r--)
			{
				if (r >= v[1].size())
				{
					tmp = v[1].elementAt(v[1].size() - 1).intValue();
				}
				else
				{
					tmp = v[1].elementAt(r).intValue();
				}
				final int yExp = (MathUtil.log2(tmp) << 4) & 0x00F0;

				if (r >= v[0].size())
				{
					tmp = v[0].elementAt(v[0].size() - 1).intValue();
				}
				else
				{
					tmp = v[0].elementAt(r).intValue();
				}
				final int xExp = MathUtil.log2(tmp) & 0x000F;
				this.hbuf.write(yExp | xExp);
			}
		}
	}

	/**
	 * Writes COC marker segment . It is a functional marker containing the
	 * coding style for one component (coding style, decomposition, layering).
	 * 
	 * <p>
	 * Its values overrides any value previously set in COD in the main header
	 * or in the tile header.
	 * 
	 * @param mh
	 *            Flag indicating whether the main header is to be written.
	 * 
	 * @param tileIdx
	 *            Tile index.
	 * 
	 * @param compIdx
	 *            index of the component which need use of the COC marker
	 *            segment.
	 * 
	 * @see #writeCOD
	 */
	@SuppressWarnings("unchecked")
	protected void writeCOC(final boolean mh, final int tileIdx, final int compIdx) throws IOException
	{
		final AnWTFilter[][] filt;
		final boolean precinctPartitionUsed;
		int tmp;
		int mrl = 0, a = 0;
		int ppx = 0, ppy = 0;
		// Progression[] prog;

		if (mh)
		{
			mrl = ((Integer) this.encSpec.dls.getCompDef(compIdx)).intValue();
			// Get precinct size for specified component
			ppx = this.encSpec.pss.getPPX(-1, compIdx, mrl);
			ppy = this.encSpec.pss.getPPY(-1, compIdx, mrl);
			// prog = (Progression[]) (encSpec.pocs.getCompDef(compIdx));
		}
		else
		{
			mrl = ((Integer) this.encSpec.dls.getTileCompVal(tileIdx, compIdx)).intValue();
			// Get precinct size for specified component/tile
			ppx = this.encSpec.pss.getPPX(tileIdx, compIdx, mrl);
			ppy = this.encSpec.pss.getPPY(tileIdx, compIdx, mrl);
			// prog = (Progression[]) (encSpec.pocs.getTileCompVal(tileIdx,
			// compIdx));
		}

		precinctPartitionUsed = Markers.PRECINCT_PARTITION_DEF_SIZE != ppx || Markers.PRECINCT_PARTITION_DEF_SIZE != ppy;
		if (precinctPartitionUsed)
		{
			// If precinct partition is used we add one byte per resolution
			// level i.e. mrl+1 (+1 for resolution 0).
			a = mrl + 1;
		}

		// COC marker
		this.hbuf.writeShort(Markers.COC);

		// Lcoc (marker segment length (in bytes))
		// Basic: Lcoc(2 bytes)+Scoc(1)+ Ccoc(1 or 2)+SPcod(5+a)
		final int markSegLen = 8 + ((257 > nComp) ? 1 : 2) + a;

		// Rounded to the nearest even value greater or equals
		this.hbuf.writeShort(markSegLen);

		// Ccoc
		if (257 > nComp)
		{
			this.hbuf.write(compIdx);
		}
		else
		{
			this.hbuf.writeShort(compIdx);
		}

		// Scod (coding style parameter)
		tmp = 0;
		if (precinctPartitionUsed)
		{
			tmp = Markers.SCOX_PRECINCT_PARTITION;
		}
		this.hbuf.write(tmp);

		// SPcoc

		// Number of decomposition levels
		this.hbuf.write(mrl);

		// Code-block width and height
		if (mh)
		{
			// main header, get component default values
			tmp = this.encSpec.cblks.getCBlkWidth(ModuleSpec.SPEC_COMP_DEF, -1, compIdx);
			this.hbuf.write(MathUtil.log2(tmp) - 2);
			tmp = this.encSpec.cblks.getCBlkHeight(ModuleSpec.SPEC_COMP_DEF, -1, compIdx);
			this.hbuf.write(MathUtil.log2(tmp) - 2);
		}
		else
		{
			// tile header, get tile component values
			tmp = this.encSpec.cblks.getCBlkWidth(ModuleSpec.SPEC_TILE_COMP, tileIdx, compIdx);
			this.hbuf.write(MathUtil.log2(tmp) - 2);
			tmp = this.encSpec.cblks.getCBlkHeight(ModuleSpec.SPEC_TILE_COMP, tileIdx, compIdx);
			this.hbuf.write(MathUtil.log2(tmp) - 2);
		}

		// Entropy coding mode options
		tmp = 0;
		if (mh)
		{ // Main header
			// Lazy coding mode ?
			if (this.encSpec.bms.getCompDef(compIdx).equals("on"))
			{
				tmp |= StdEntropyCoderOptions.OPT_BYPASS;
			}
			// MQ reset after each coding pass ?
			if ("on".equalsIgnoreCase((String) encSpec.mqrs.getCompDef(compIdx)))
			{
				tmp |= StdEntropyCoderOptions.OPT_RESET_MQ;
			}
			// MQ termination after each arithmetically coded coding pass ?
			if (this.encSpec.rts.getCompDef(compIdx).equals("on"))
			{
				tmp |= StdEntropyCoderOptions.OPT_TERM_PASS;
			}
			// Vertically stripe-causal context mode ?
			if (this.encSpec.css.getCompDef(compIdx).equals("on"))
			{
				tmp |= StdEntropyCoderOptions.OPT_VERT_STR_CAUSAL;
			}
			// Predictable termination ?
			if (this.encSpec.tts.getCompDef(compIdx).equals("predict"))
			{
				tmp |= StdEntropyCoderOptions.OPT_PRED_TERM;
			}
			// Error resilience segmentation symbol insertion ?
			if (this.encSpec.sss.getCompDef(compIdx).equals("on"))
			{
				tmp |= StdEntropyCoderOptions.OPT_SEG_SYMBOLS;
			}
		}
		else
		{ // Tile Header
			if (this.encSpec.bms.getTileCompVal(tileIdx, compIdx).equals("on"))
			{
				tmp |= StdEntropyCoderOptions.OPT_BYPASS;
			}
			// MQ reset after each coding pass ?
			if (this.encSpec.mqrs.getTileCompVal(tileIdx, compIdx).equals("on"))
			{
				tmp |= StdEntropyCoderOptions.OPT_RESET_MQ;
			}
			// MQ termination after each arithmetically coded coding pass ?
			if (this.encSpec.rts.getTileCompVal(tileIdx, compIdx).equals("on"))
			{
				tmp |= StdEntropyCoderOptions.OPT_TERM_PASS;
			}
			// Vertically stripe-causal context mode ?
			if (this.encSpec.css.getTileCompVal(tileIdx, compIdx).equals("on"))
			{
				tmp |= StdEntropyCoderOptions.OPT_VERT_STR_CAUSAL;
			}
			// Predictable termination ?
			if (this.encSpec.tts.getTileCompVal(tileIdx, compIdx).equals("predict"))
			{
				tmp |= StdEntropyCoderOptions.OPT_PRED_TERM;
			}
			// Error resilience segmentation symbol insertion ?
			if (this.encSpec.sss.getTileCompVal(tileIdx, compIdx).equals("on"))
			{
				tmp |= StdEntropyCoderOptions.OPT_SEG_SYMBOLS;
			}
		}
		this.hbuf.write(tmp);

		// Wavelet transform
		// Wavelet Filter
		if (mh)
		{
			filt = ((AnWTFilter[][]) this.encSpec.wfs.getCompDef(compIdx));
			this.hbuf.write(filt[0][0].getFilterType());
		}
		else
		{
			filt = ((AnWTFilter[][]) this.encSpec.wfs.getTileCompVal(tileIdx, compIdx));
			this.hbuf.write(filt[0][0].getFilterType());
		}

		// Precinct partition
		if (precinctPartitionUsed)
		{
			// Write the precinct size for each resolution level + 1
			// (resolution 0) if precinct partition is used.
			Vector<Integer>[] v = null;
			if (mh)
			{
				v = (Vector<Integer>[]) this.encSpec.pss.getCompDef(compIdx);
			}
			else
			{
				v = (Vector<Integer>[]) this.encSpec.pss.getTileCompVal(tileIdx, compIdx);
			}
			for (int r = mrl; 0 <= r; r--)
			{
				if (r >= v[1].size())
				{
					tmp = v[1].elementAt(v[1].size() - 1).intValue();
				}
				else
				{
					tmp = v[1].elementAt(r).intValue();
				}
				final int yExp = (MathUtil.log2(tmp) << 4) & 0x00F0;

				if (r >= v[0].size())
				{
					tmp = v[0].elementAt(v[0].size() - 1).intValue();
				}
				else
				{
					tmp = v[0].elementAt(r).intValue();
				}
				final int xExp = MathUtil.log2(tmp) & 0x000F;
				this.hbuf.write(yExp | xExp);
			}
		}

	}

	/**
	 * Writes QCD marker segment in main header. QCD is a functional marker
	 * segment countaining the quantization default used for compressing all the
	 * components in an image. The values can be overriden for an individual
	 * component by a QCC marker in either the main or the tile header.
	 */
	protected void writeMainQCD() throws IOException
	{
		final int mrl;
		final int qstyle;

		float step;

		final String qType = (String) this.encSpec.qts.getDefault();
		final float baseStep = ((Float) this.encSpec.qsss.getDefault()).floatValue();
		final int gb = ((Integer) this.encSpec.gbs.getDefault()).intValue();

		final boolean isDerived = "derived".equals(qType);
		final boolean isReversible = "reversible".equals(qType);

		mrl = ((Integer) this.encSpec.dls.getDefault()).intValue();

		final int nt = this.dwt.getNumTiles();
		final int nc = this.dwt.getNumComps();
		int tmpI;
		final int[] tcIdx = new int[2];
		String tmpStr;
		boolean notFound = true;
		for (int t = 0; t < nt && notFound; t++)
		{
			for (int c = 0; c < nc && notFound; c++)
			{
				tmpI = ((Integer) this.encSpec.dls.getTileCompVal(t, c)).intValue();
				tmpStr = (String) this.encSpec.qts.getTileCompVal(t, c);
				if (tmpI == mrl && tmpStr.equals(qType))
				{
					tcIdx[0] = t;
					tcIdx[1] = c;
					notFound = false;
				}
			}
		}
		if (notFound)
		{
			throw new Error("Default representative for quantization type "
					+ " and number of decomposition levels not found in main QCD marker segment. "
					+ "You have found a JJ2000 bug.");
		}
		SubbandAn sb;
		SubbandAn csb;
		final SubbandAn sbRoot = this.dwt.getAnSubbandTree(tcIdx[0], tcIdx[1]);
		this.defimgn = this.dwt.getNomRangeBits(tcIdx[1]);

		int nqcd; // Number of quantization step-size to transmit

		// Get the quantization style
		qstyle = (isReversible) ? Markers.SQCX_NO_QUANTIZATION : ((isDerived) ? Markers.SQCX_SCALAR_DERIVED : Markers.SQCX_SCALAR_EXPOUNDED);

		// QCD marker
		this.hbuf.writeShort(Markers.QCD);

		// Compute the number of steps to send
		switch (qstyle)
		{
			case Markers.SQCX_SCALAR_DERIVED:
				nqcd = 1; // Just the LL value
				break;
			case Markers.SQCX_NO_QUANTIZATION:
			case Markers.SQCX_SCALAR_EXPOUNDED:
				// One value per subband
				nqcd = 0;

				sb = sbRoot;

				// Get the subband at first resolution level
				sb = (SubbandAn) sb.getSubbandByIdx(0, 0);

				// Count total number of subbands
				for (int j = 0; j <= mrl; j++)
				{
					csb = sb;
					while (null != csb)
					{
						nqcd++;
						csb = (SubbandAn) csb.nextSubband();
					}
					// Go up one resolution level
					sb = (SubbandAn) sb.getNextResLevel();
				}
				break;
			default:
				throw new Error("Internal JJ2000 error");
		}

		// Lqcd (marker segment length (in bytes))
		// Lqcd(2 bytes)+Sqcd(1)+ SPqcd (2*Nqcd)
		final int markSegLen = 3 + ((isReversible) ? nqcd : 2 * nqcd);

		// Rounded to the nearest even value greater or equals
		this.hbuf.writeShort(markSegLen);

		// Sqcd
		this.hbuf.write(qstyle + (gb << Markers.SQCX_GB_SHIFT));

		// SPqcd
		switch (qstyle)
		{
			case Markers.SQCX_NO_QUANTIZATION:
				sb = sbRoot;
				sb = (SubbandAn) sb.getSubbandByIdx(0, 0);

				// Output one exponent per subband
				for (int j = 0; j <= mrl; j++)
				{
					csb = sb;
					while (null != csb)
					{
						final int tmp = (this.defimgn + csb.anGainExp);
						this.hbuf.write(tmp << Markers.SQCX_EXP_SHIFT);

						csb = (SubbandAn) csb.nextSubband();
						// Go up one resolution level
					}
					sb = (SubbandAn) sb.getNextResLevel();
				}
				break;
			case Markers.SQCX_SCALAR_DERIVED:
				sb = sbRoot;
				sb = (SubbandAn) sb.getSubbandByIdx(0, 0);

				// Calculate subband step (normalized to unit
				// dynamic range)
				step = baseStep / (1 << sb.level);

				// Write exponent-mantissa, 16 bits
				this.hbuf.writeShort(StdQuantizer.convertToExpMantissa(step));
				break;
			case Markers.SQCX_SCALAR_EXPOUNDED:
				sb = sbRoot;
				sb = (SubbandAn) sb.getSubbandByIdx(0, 0);

				// Output one step per subband
				for (int j = 0; j <= mrl; j++)
				{
					csb = sb;
					while (null != csb)
					{
						// Calculate subband step (normalized to unit
						// dynamic range)
						step = baseStep / (csb.l2Norm * (1 << csb.anGainExp));

						// Write exponent-mantissa, 16 bits
						this.hbuf.writeShort(StdQuantizer.convertToExpMantissa(step));

						csb = (SubbandAn) csb.nextSubband();
					}
					// Go up one resolution level
					sb = (SubbandAn) sb.getNextResLevel();
				}
				break;
			default:
				throw new Error("Internal JJ2000 error");
		}
	}

	/**
	 * Writes QCC marker segment in main header. It is a functional marker
	 * segment countaining the quantization used for compressing the specified
	 * component in an image. The values override for the specified component
	 * what was defined by a QCC marker in either the main or the tile header.
	 * 
	 * @param compIdx
	 *            Index of the component which needs QCC marker segment.
	 */
	protected void writeMainQCC(final int compIdx) throws IOException
	{

		int mrl;
		final int qstyle;
		int tIdx = 0;
		float step;

		SubbandAn sb, sb2;
		final SubbandAn sbRoot;

		final int imgnr = this.dwt.getNomRangeBits(compIdx);
		final String qType = (String) this.encSpec.qts.getCompDef(compIdx);
		final float baseStep = ((Float) this.encSpec.qsss.getCompDef(compIdx)).floatValue();
		final int gb = ((Integer) this.encSpec.gbs.getCompDef(compIdx)).intValue();

		final boolean isReversible = "reversible".equals(qType);
		final boolean isDerived = "derived".equals(qType);

		mrl = ((Integer) this.encSpec.dls.getCompDef(compIdx)).intValue();

		final int nt = this.dwt.getNumTiles();
		final int nc = this.dwt.getNumComps();
		int tmpI;
		String tmpStr;
		boolean notFound = true;
		for (int t = 0; t < nt && notFound; t++)
		{
			for (int c = 0; c < nc && notFound; c++)
			{
				tmpI = ((Integer) this.encSpec.dls.getTileCompVal(t, c)).intValue();
				tmpStr = (String) this.encSpec.qts.getTileCompVal(t, c);
				if (tmpI == mrl && tmpStr.equals(qType))
				{
					tIdx = t;
					notFound = false;
				}
			}
		}
		if (notFound)
		{
			throw new Error("Default representative for quantization type "
					+ " and number of decomposition levels not found in main QCC (c=" + compIdx
					+ ") marker segment. You have found a JJ2000 bug.");
		}
		sbRoot = this.dwt.getAnSubbandTree(tIdx, compIdx);

		int nqcc; // Number of quantization step-size to transmit

		// Get the quantization style
		if (isReversible)
		{
			qstyle = Markers.SQCX_NO_QUANTIZATION;
		}
		else if (isDerived)
		{
			qstyle = Markers.SQCX_SCALAR_DERIVED;
		}
		else
		{
			qstyle = Markers.SQCX_SCALAR_EXPOUNDED;
		}

		// QCC marker
		this.hbuf.writeShort(Markers.QCC);

		// Compute the number of steps to send
		switch (qstyle)
		{
			case Markers.SQCX_SCALAR_DERIVED:
				nqcc = 1; // Just the LL value
				break;
			case Markers.SQCX_NO_QUANTIZATION:
			case Markers.SQCX_SCALAR_EXPOUNDED:
				// One value per subband
				nqcc = 0;

				sb = sbRoot;
				mrl = sb.resLvl;

				// Get the subband at first resolution level
				sb = (SubbandAn) sb.getSubbandByIdx(0, 0);

				// Find root element for LL subband
				while (0 != sb.resLvl)
				{
					sb = sb.subb_LL;
				}

				// Count total number of subbands
				for (int j = 0; j <= mrl; j++)
				{
					sb2 = sb;
					while (null != sb2)
					{
						nqcc++;
						sb2 = (SubbandAn) sb2.nextSubband();
					}
					// Go up one resolution level
					sb = (SubbandAn) sb.getNextResLevel();
				}
				break;
			default:
				throw new Error("Internal JJ2000 error");
		}

		// Lqcc (marker segment length (in bytes))
		// Lqcc(2 bytes)+Cqcc(1 or 2)+Sqcc(1)+ SPqcc (2*Nqcc)
		final int markSegLen = 3 + ((257 > nComp) ? 1 : 2) + ((isReversible) ? nqcc : 2 * nqcc);
		this.hbuf.writeShort(markSegLen);

		// Cqcc
		if (257 > nComp)
		{
			this.hbuf.write(compIdx);
		}
		else
		{
			this.hbuf.writeShort(compIdx);
		}

		// Sqcc (quantization style)
		this.hbuf.write(qstyle + (gb << Markers.SQCX_GB_SHIFT));

		// SPqcc
		switch (qstyle)
		{
			case Markers.SQCX_NO_QUANTIZATION:
				// Get resolution level 0 subband
				sb = sbRoot;
				sb = (SubbandAn) sb.getSubbandByIdx(0, 0);

				// Output one exponent per subband
				for (int j = 0; j <= mrl; j++)
				{
					sb2 = sb;
					while (null != sb2)
					{
						final int tmp = (imgnr + sb2.anGainExp);
						this.hbuf.write(tmp << Markers.SQCX_EXP_SHIFT);

						sb2 = (SubbandAn) sb2.nextSubband();
					}
					// Go up one resolution level
					sb = (SubbandAn) sb.getNextResLevel();
				}
				break;
			case Markers.SQCX_SCALAR_DERIVED:
				// Get resolution level 0 subband
				sb = sbRoot;
				sb = (SubbandAn) sb.getSubbandByIdx(0, 0);

				// Calculate subband step (normalized to unit
				// dynamic range)
				step = baseStep / (1 << sb.level);

				// Write exponent-mantissa, 16 bits
				this.hbuf.writeShort(StdQuantizer.convertToExpMantissa(step));
				break;
			case Markers.SQCX_SCALAR_EXPOUNDED:
				// Get resolution level 0 subband
				sb = sbRoot;
				mrl = sb.resLvl;

				sb = (SubbandAn) sb.getSubbandByIdx(0, 0);

				for (int j = 0; j <= mrl; j++)
				{
					sb2 = sb;
					while (null != sb2)
					{
						// Calculate subband step (normalized to unit
						// dynamic range)
						step = baseStep / (sb2.l2Norm * (1 << sb2.anGainExp));

						// Write exponent-mantissa, 16 bits
						this.hbuf.writeShort(StdQuantizer.convertToExpMantissa(step));
						sb2 = (SubbandAn) sb2.nextSubband();
					}
					// Go up one resolution level
					sb = (SubbandAn) sb.getNextResLevel();
				}
				break;
			default:
				throw new Error("Internal JJ2000 error");
		}
	}

	/**
	 * Writes QCD marker segment in tile header. QCD is a functional marker
	 * segment countaining the quantization default used for compressing all the
	 * components in an image. The values can be overriden for an individual
	 * component by a QCC marker in either the main or the tile header.
	 * 
	 * @param tIdx
	 *            Tile index
	 */
	protected void writeTileQCD(final int tIdx) throws IOException
	{
		final int mrl;
		final int qstyle;

		float step;
		SubbandAn sb;
		SubbandAn csb;
		final SubbandAn sbRoot;

		final String qType = (String) this.encSpec.qts.getTileDef(tIdx);
		final float baseStep = ((Float) this.encSpec.qsss.getTileDef(tIdx)).floatValue();
		mrl = ((Integer) this.encSpec.dls.getTileDef(tIdx)).intValue();

		final int nc = this.dwt.getNumComps();
		int tmpI;
		String tmpStr;
		boolean notFound = true;
		int compIdx = 0;
		for (int c = 0; c < nc && notFound; c++)
		{
			tmpI = ((Integer) this.encSpec.dls.getTileCompVal(tIdx, c)).intValue();
			tmpStr = (String) this.encSpec.qts.getTileCompVal(tIdx, c);
			if (tmpI == mrl && tmpStr.equals(qType))
			{
				compIdx = c;
				notFound = false;
			}
		}
		if (notFound)
		{
			throw new Error("Default representative for quantization type "
					+ " and number of decomposition levels not found in tile QCD (t=" + tIdx
					+ ") marker segment. You have found a JJ2000 bug.");
		}

		sbRoot = this.dwt.getAnSubbandTree(tIdx, compIdx);
		this.deftilenr = this.dwt.getNomRangeBits(compIdx);
		final int gb = ((Integer) this.encSpec.gbs.getTileDef(tIdx)).intValue();

		final boolean isDerived = "derived".equals(qType);
		final boolean isReversible = "reversible".equals(qType);

		int nqcd; // Number of quantization step-size to transmit

		// Get the quantization style
		qstyle = (isReversible) ? Markers.SQCX_NO_QUANTIZATION : ((isDerived) ? Markers.SQCX_SCALAR_DERIVED : Markers.SQCX_SCALAR_EXPOUNDED);

		// QCD marker
		this.hbuf.writeShort(Markers.QCD);

		// Compute the number of steps to send
		switch (qstyle)
		{
			case Markers.SQCX_SCALAR_DERIVED:
				nqcd = 1; // Just the LL value
				break;
			case Markers.SQCX_NO_QUANTIZATION:
			case Markers.SQCX_SCALAR_EXPOUNDED:
				// One value per subband
				nqcd = 0;

				sb = sbRoot;

				// Get the subband at first resolution level
				sb = (SubbandAn) sb.getSubbandByIdx(0, 0);

				// Count total number of subbands
				for (int j = 0; j <= mrl; j++)
				{
					csb = sb;
					while (null != csb)
					{
						nqcd++;
						csb = (SubbandAn) csb.nextSubband();
					}
					// Go up one resolution level
					sb = (SubbandAn) sb.getNextResLevel();
				}
				break;
			default:
				throw new Error("Internal JJ2000 error");
		}

		// Lqcd (marker segment length (in bytes))
		// Lqcd(2 bytes)+Sqcd(1)+ SPqcd (2*Nqcd)
		final int markSegLen = 3 + ((isReversible) ? nqcd : 2 * nqcd);

		// Rounded to the nearest even value greater or equals
		this.hbuf.writeShort(markSegLen);

		// Sqcd
		this.hbuf.write(qstyle + (gb << Markers.SQCX_GB_SHIFT));

		// SPqcd
		switch (qstyle)
		{
			case Markers.SQCX_NO_QUANTIZATION:
				sb = sbRoot;
				sb = (SubbandAn) sb.getSubbandByIdx(0, 0);

				// Output one exponent per subband
				for (int j = 0; j <= mrl; j++)
				{
					csb = sb;
					while (null != csb)
					{
						final int tmp = (this.deftilenr + csb.anGainExp);
						this.hbuf.write(tmp << Markers.SQCX_EXP_SHIFT);

						csb = (SubbandAn) csb.nextSubband();
						// Go up one resolution level
					}
					sb = (SubbandAn) sb.getNextResLevel();
				}
				break;
			case Markers.SQCX_SCALAR_DERIVED:
				sb = sbRoot;
				sb = (SubbandAn) sb.getSubbandByIdx(0, 0);

				// Calculate subband step (normalized to unit
				// dynamic range)
				step = baseStep / (1 << sb.level);

				// Write exponent-mantissa, 16 bits
				this.hbuf.writeShort(StdQuantizer.convertToExpMantissa(step));
				break;
			case Markers.SQCX_SCALAR_EXPOUNDED:
				sb = sbRoot;
				sb = (SubbandAn) sb.getSubbandByIdx(0, 0);

				// Output one step per subband
				for (int j = 0; j <= mrl; j++)
				{
					csb = sb;
					while (null != csb)
					{
						// Calculate subband step (normalized to unit
						// dynamic range)
						step = baseStep / (csb.l2Norm * (1 << csb.anGainExp));

						// Write exponent-mantissa, 16 bits
						this.hbuf.writeShort(StdQuantizer.convertToExpMantissa(step));

						csb = (SubbandAn) csb.nextSubband();
					}
					// Go up one resolution level
					sb = (SubbandAn) sb.getNextResLevel();
				}
				break;
			default:
				throw new Error("Internal JJ2000 error");
		}
	}

	/**
	 * Writes QCC marker segment in tile header. It is a functional marker
	 * segment countaining the quantization used for compressing the specified
	 * component in an image. The values override for the specified component
	 * what was defined by a QCC marker in either the main or the tile header.
	 * 
	 * @param t
	 *            Tile index
	 * 
	 * @param compIdx
	 *            Index of the component which needs QCC marker segment.
	 */
	protected void writeTileQCC(final int t, final int compIdx) throws IOException
	{

		int mrl;
		final int qstyle;
		float step;

		SubbandAn sb, sb2;
		int nqcc; // Number of quantization step-size to transmit

		final SubbandAn sbRoot = this.dwt.getAnSubbandTree(t, compIdx);
		final int imgnr = this.dwt.getNomRangeBits(compIdx);
		final String qType = (String) this.encSpec.qts.getTileCompVal(t, compIdx);
		final float baseStep = ((Float) this.encSpec.qsss.getTileCompVal(t, compIdx)).floatValue();
		final int gb = ((Integer) this.encSpec.gbs.getTileCompVal(t, compIdx)).intValue();

		final boolean isReversible = "reversible".equals(qType);
		final boolean isDerived = "derived".equals(qType);

		mrl = ((Integer) this.encSpec.dls.getTileCompVal(t, compIdx)).intValue();

		// Get the quantization style
		if (isReversible)
		{
			qstyle = Markers.SQCX_NO_QUANTIZATION;
		}
		else if (isDerived)
		{
			qstyle = Markers.SQCX_SCALAR_DERIVED;
		}
		else
		{
			qstyle = Markers.SQCX_SCALAR_EXPOUNDED;
		}

		// QCC marker
		this.hbuf.writeShort(Markers.QCC);

		// Compute the number of steps to send
		switch (qstyle)
		{
			case Markers.SQCX_SCALAR_DERIVED:
				nqcc = 1; // Just the LL value
				break;
			case Markers.SQCX_NO_QUANTIZATION:
			case Markers.SQCX_SCALAR_EXPOUNDED:
				// One value per subband
				nqcc = 0;

				sb = sbRoot;
				mrl = sb.resLvl;

				// Get the subband at first resolution level
				sb = (SubbandAn) sb.getSubbandByIdx(0, 0);

				// Find root element for LL subband
				while (0 != sb.resLvl)
				{
					sb = sb.subb_LL;
				}

				// Count total number of subbands
				for (int j = 0; j <= mrl; j++)
				{
					sb2 = sb;
					while (null != sb2)
					{
						nqcc++;
						sb2 = (SubbandAn) sb2.nextSubband();
					}
					// Go up one resolution level
					sb = (SubbandAn) sb.getNextResLevel();
				}
				break;
			default:
				throw new Error("Internal JJ2000 error");
		}

		// Lqcc (marker segment length (in bytes))
		// Lqcc(2 bytes)+Cqcc(1 or 2)+Sqcc(1)+ SPqcc (2*Nqcc)
		final int markSegLen = 3 + ((257 > nComp) ? 1 : 2) + ((isReversible) ? nqcc : 2 * nqcc);
		this.hbuf.writeShort(markSegLen);

		// Cqcc
		if (257 > nComp)
		{
			this.hbuf.write(compIdx);
		}
		else
		{
			this.hbuf.writeShort(compIdx);
		}

		// Sqcc (quantization style)
		this.hbuf.write(qstyle + (gb << Markers.SQCX_GB_SHIFT));

		// SPqcc
		switch (qstyle)
		{
			case Markers.SQCX_NO_QUANTIZATION:
				// Get resolution level 0 subband
				sb = sbRoot;
				sb = (SubbandAn) sb.getSubbandByIdx(0, 0);

				// Output one exponent per subband
				for (int j = 0; j <= mrl; j++)
				{
					sb2 = sb;
					while (null != sb2)
					{
						final int tmp = (imgnr + sb2.anGainExp);
						this.hbuf.write(tmp << Markers.SQCX_EXP_SHIFT);

						sb2 = (SubbandAn) sb2.nextSubband();
					}
					// Go up one resolution level
					sb = (SubbandAn) sb.getNextResLevel();
				}
				break;
			case Markers.SQCX_SCALAR_DERIVED:
				// Get resolution level 0 subband
				sb = sbRoot;
				sb = (SubbandAn) sb.getSubbandByIdx(0, 0);

				// Calculate subband step (normalized to unit
				// dynamic range)
				step = baseStep / (1 << sb.level);

				// Write exponent-mantissa, 16 bits
				this.hbuf.writeShort(StdQuantizer.convertToExpMantissa(step));
				break;
			case Markers.SQCX_SCALAR_EXPOUNDED:
				// Get resolution level 0 subband
				sb = sbRoot;
				mrl = sb.resLvl;

				sb = (SubbandAn) sb.getSubbandByIdx(0, 0);

				for (int j = 0; j <= mrl; j++)
				{
					sb2 = sb;
					while (null != sb2)
					{
						// Calculate subband step (normalized to unit
						// dynamic range)
						step = baseStep / (sb2.l2Norm * (1 << sb2.anGainExp));

						// Write exponent-mantissa, 16 bits
						this.hbuf.writeShort(StdQuantizer.convertToExpMantissa(step));
						sb2 = (SubbandAn) sb2.nextSubband();
					}
					// Go up one resolution level
					sb = (SubbandAn) sb.getNextResLevel();
				}
				break;
			default:
				throw new Error("Internal JJ2000 error");
		}
	}

	/**
	 * Writes POC marker segment. POC is a functional marker segment containing
	 * the bounds and progression order for any progression order other than
	 * default in the codestream.
	 * 
	 * @param mh
	 *            Flag indicating whether the main header is to be written
	 * 
	 * @param tileIdx
	 *            Tile index
	 */
	protected void writePOC(final boolean mh, final int tileIdx) throws IOException
	{
		int markSegLen = 0; // Segment marker length
		final int lenCompField; // Holds the size of any component field as
							// this size depends on the number of
							// components
		Progression[] prog = null; // Holds the progression(s)
		final int npoc; // Number of progression order changes

		// Get the progression order changes, their number and checks
		// if it is ok
		if (mh)
		{
			prog = (Progression[]) (this.encSpec.pocs.getDefault());
		}
		else
		{
			prog = (Progression[]) (this.encSpec.pocs.getTileDef(tileIdx));
		}

		// Calculate the length of a component field (depends on the number of
		// components)
		lenCompField = (257 > nComp ? 1 : 2);

		// POC marker
		this.hbuf.writeShort(Markers.POC);

		// Lpoc (marker segment length (in bytes))
		// Basic: Lpoc(2 bytes) + npoc * [ RSpoc(1) + CSpoc(1 or 2) +
		// LYEpoc(2) + REpoc(1) + CEpoc(1 or 2) + Ppoc(1) ]
		npoc = prog.length;
		markSegLen = 2 + npoc * (1 + lenCompField + 2 + 1 + lenCompField + 1);
		this.hbuf.writeShort(markSegLen);

		// Write each progression order change
		for (int i = 0; i < npoc; i++)
		{
			// RSpoc(i)
			this.hbuf.write(prog[i].rs);
			// CSpoc(i)
			if (2 == lenCompField)
			{
				this.hbuf.writeShort(prog[i].cs);
			}
			else
			{
				this.hbuf.write(prog[i].cs);
			}
			// LYEpoc(i)
			this.hbuf.writeShort(prog[i].lye);
			// REpoc(i)
			this.hbuf.write(prog[i].re);
			// CEpoc(i)
			if (2 == lenCompField)
			{
				this.hbuf.writeShort(prog[i].ce);
			}
			else
			{
				this.hbuf.write(prog[i].ce);
			}
			// Ppoc(i)
			this.hbuf.write(prog[i].type);
		}
	}

	/**
	 * Write main header. JJ2000 main header corresponds to the following
	 * sequence of marker segments:
	 * 
	 * <ol>
	 * <li>SOC</li>
	 * <li>SIZ</li>
	 * <li>COD</li>
	 * <li>COC (if needed)</li>
	 * <li>QCD</li>
	 * <li>QCC (if needed)</li>
	 * <li>POC (if needed)</li>
	 * </ol>
	 */
	public void encodeMainHeader() throws IOException
	{
		int i;

		// +---------------------------------+
		// | SOC marker segment |
		// +---------------------------------+
		this.writeSOC();

		// +---------------------------------+
		// | Image and tile SIZe (SIZ) |
		// +---------------------------------+
		this.writeSIZ();

		// +-------------------------------+
		// | COding style Default (COD) |
		// +-------------------------------+
		final boolean isEresUsed = this.encSpec.tts.getDefault().equals("predict");
		this.writeCOD(true, 0);

		// +---------------------------------+
		// | COding style Component (COC) |
		// +---------------------------------+
		for (i = 0; i < this.nComp; i++)
		{
			final boolean isEresUsedinComp = this.encSpec.tts.getCompDef(i).equals("predict");
			if (this.encSpec.wfs.isCompSpecified(i) || this.encSpec.dls.isCompSpecified(i) || this.encSpec.bms.isCompSpecified(i)
					|| this.encSpec.mqrs.isCompSpecified(i) || this.encSpec.rts.isCompSpecified(i)
					|| this.encSpec.sss.isCompSpecified(i) || this.encSpec.css.isCompSpecified(i)
					|| this.encSpec.pss.isCompSpecified(i) || this.encSpec.cblks.isCompSpecified(i)
					|| (isEresUsed != isEresUsedinComp))
				// Some component non-default stuff => need COC
				this.writeCOC(true, 0, i);
		}

		// +-------------------------------+
		// | Quantization Default (QCD) |
		// +-------------------------------+
		this.writeMainQCD();

		// +-------------------------------+
		// | Quantization Component (QCC) |
		// +-------------------------------+
		// Write needed QCC markers
		for (i = 0; i < this.nComp; i++)
		{
			if (this.dwt.getNomRangeBits(i) != this.defimgn || this.encSpec.qts.isCompSpecified(i) || this.encSpec.qsss.isCompSpecified(i)
					|| this.encSpec.dls.isCompSpecified(i) || this.encSpec.gbs.isCompSpecified(i))
			{
				this.writeMainQCC(i);
			}
		}

		// +--------------------------+
		// | POC maker segment |
		// +--------------------------+
		final Progression[] prog = (Progression[]) (this.encSpec.pocs.getDefault());
		if (1 < prog.length)
			this.writePOC(true, 0);

		// +---------------------------+
		// | Comments (COM) |
		// +---------------------------+
		this.writeCOM();
	}

	/**
	 * Write COM marker segment(s) to the codestream.
	 * 
	 * <p>
	 * This marker is currently written in main header and indicates the JJ2000
	 * encoder's version that has created the codestream.
	 */
	private void writeCOM() throws IOException
	{
		// JJ2000 COM marker segment
		if (this.enJJ2KMarkSeg)
		{
			final String str = "Created by: JJ2000 version " + JJ2KInfo.version;
			final int markSegLen; // the marker segment length

			// COM marker
			this.hbuf.writeShort(Markers.COM);

			// Calculate length: Lcom(2) + Rcom (2) + string's length;
			markSegLen = 2 + 2 + str.length();
			this.hbuf.writeShort(markSegLen);

			// Rcom
			this.hbuf.writeShort(1); // General use (IS 8859-15:1999(Latin) values)

			final byte[] chars = str.getBytes(StandardCharsets.UTF_8);
			for (int i = 0; i < chars.length; i++)
			{
				this.hbuf.writeByte(chars[i]);
			}
		}
		// other COM marker segments
		if (null != otherCOMMarkSeg)
		{
			final StringTokenizer stk = new StringTokenizer(this.otherCOMMarkSeg, "#");
			while (stk.hasMoreTokens())
			{
				final String str = stk.nextToken();
				final int markSegLen; // the marker segment length

				// COM marker
				this.hbuf.writeShort(Markers.COM);

				// Calculate length: Lcom(2) + Rcom (2) + string's length;
				markSegLen = 2 + 2 + str.length();
				this.hbuf.writeShort(markSegLen);

				// Rcom
				this.hbuf.writeShort(1); // General use (IS 8859-15:1999(Latin)
				// values)

				final byte[] chars = str.getBytes(StandardCharsets.UTF_8);
				for (int i = 0; i < chars.length; i++)
				{
					this.hbuf.writeByte(chars[i]);
				}
			}
		}
	}

	/**
	 * Writes the RGN marker segment in the tile header. It describes the
	 * scaling value in each tile component
	 * 
	 * <p>
	 * May be used in tile or main header. If used in main header, it refers to
	 * a ROI of the whole image, regardless of tiling. When used in tile header,
	 * only the particular tile is affected.
	 * 
	 * @param tIdx
	 *            The tile index
	 * 
	 * @exception IOException
	 *                If an I/O error occurs while reading from the encoder
	 *                header stream
	 */
	private void writeRGN(final int tIdx) throws IOException
	{
		int i;
		int markSegLen; // the marker length

		// Write one RGN marker per component
		for (i = 0; i < this.nComp; i++)
		{
			// RGN marker
			this.hbuf.writeShort(Markers.RGN);

			// Calculate length (Lrgn)
			// Basic: Lrgn (2) + Srgn (1) + SPrgn + one byte
			// or two for component number
			markSegLen = 4 + ((257 > nComp) ? 1 : 2);
			this.hbuf.writeShort(markSegLen);

			// Write component (Crgn)
			if (257 > nComp)
			{
				this.hbuf.writeByte(i);
			}
			else
			{
				this.hbuf.writeShort(i);
			}

			// Write type of ROI (Srgn)
			this.hbuf.writeByte(Markers.SRGN_IMPLICIT);

			// Write ROI info (SPrgn)
			this.hbuf.writeByte(((Integer) (this.encSpec.rois.getTileCompVal(tIdx, i))).intValue());
		}
	}

	/**
	 * Writes tile-part header. JJ2000 tile-part header corresponds to the
	 * following sequence of marker segments:
	 * 
	 * <ol>
	 * <li>SOT</li>
	 * <li>COD (if needed)</li>
	 * <li>COC (if needed)</li>
	 * <li>QCD (if needed)</li>
	 * <li>QCC (if needed)</li>
	 * <li>RGN (if needed)</li>
	 * <li>POC (if needed)</li>
	 * <li>SOD</li>
	 * </ol>
	 * 
	 * @param tileLength
	 *            The length of the current tile-part.
	 * 
	 * @param tileIdx
	 *            Index of the tile to write
	 */
	public void encodeTilePartHeader(final int tileLength, final int tileIdx) throws IOException
	{

		final int tmp;
		final Coord numTiles = this.ralloc.getNumTiles(null);
		this.ralloc.setTile(tileIdx % numTiles.x, tileIdx / numTiles.x);

		// +--------------------------+
		// | SOT maker segment |
		// +--------------------------+
		// SOT marker
		this.hbuf.writeByte(Markers.SOT >> 8);
		this.hbuf.writeByte(Markers.SOT);

		// Lsot (10 bytes)
		this.hbuf.writeByte(0);
		this.hbuf.writeByte(10);

		// Isot
		if (65534 < tileIdx)
		{
			throw new IllegalArgumentException("Trying to write a tile-part header whose tile index is too high");
		}
		this.hbuf.writeByte(tileIdx >> 8);
		this.hbuf.writeByte(tileIdx);

		// Psot
		tmp = tileLength;
		this.hbuf.writeByte(tmp >> 24);
		this.hbuf.writeByte(tmp >> 16);
		this.hbuf.writeByte(tmp >> 8);
		this.hbuf.writeByte(tmp);

		// TPsot
		this.hbuf.writeByte(0); // Only one tile-part currently supported !

		// TNsot
		this.hbuf.writeByte(1); // Only one tile-part currently supported !

		// +--------------------------+
		// | COD maker segment |
		// +--------------------------+
		final boolean isEresUsed = this.encSpec.tts.getDefault().equals("predict");
		final boolean isEresUsedInTile = this.encSpec.tts.getTileDef(tileIdx).equals("predict");
		boolean tileCODwritten = false;
		if (this.encSpec.wfs.isTileSpecified(tileIdx) || this.encSpec.cts.isTileSpecified(tileIdx)
				|| this.encSpec.dls.isTileSpecified(tileIdx) || this.encSpec.bms.isTileSpecified(tileIdx)
				|| this.encSpec.mqrs.isTileSpecified(tileIdx) || this.encSpec.rts.isTileSpecified(tileIdx)
				|| this.encSpec.css.isTileSpecified(tileIdx) || this.encSpec.pss.isTileSpecified(tileIdx)
				|| this.encSpec.sops.isTileSpecified(tileIdx) || this.encSpec.sss.isTileSpecified(tileIdx)
				|| this.encSpec.pocs.isTileSpecified(tileIdx) || this.encSpec.ephs.isTileSpecified(tileIdx)
				|| this.encSpec.cblks.isTileSpecified(tileIdx) || (isEresUsed != isEresUsedInTile))
		{
			this.writeCOD(false, tileIdx);
			tileCODwritten = true;
		}

		// +--------------------------+
		// | COC maker segment |
		// +--------------------------+
		for (int c = 0; c < this.nComp; c++)
		{
			final boolean isEresUsedInTileComp = this.encSpec.tts.getTileCompVal(tileIdx, c).equals("predict");

			if (this.encSpec.wfs.isTileCompSpecified(tileIdx, c) || this.encSpec.dls.isTileCompSpecified(tileIdx, c)
					|| this.encSpec.bms.isTileCompSpecified(tileIdx, c) || this.encSpec.mqrs.isTileCompSpecified(tileIdx, c)
					|| this.encSpec.rts.isTileCompSpecified(tileIdx, c) || this.encSpec.css.isTileCompSpecified(tileIdx, c)
					|| this.encSpec.pss.isTileCompSpecified(tileIdx, c) || this.encSpec.sss.isTileCompSpecified(tileIdx, c)
					|| this.encSpec.cblks.isTileCompSpecified(tileIdx, c) || (isEresUsedInTileComp != isEresUsed))
			{
				this.writeCOC(false, tileIdx, c);
			}
			else if (tileCODwritten)
			{
				if (this.encSpec.wfs.isCompSpecified(c) || this.encSpec.dls.isCompSpecified(c) || this.encSpec.bms.isCompSpecified(c)
						|| this.encSpec.mqrs.isCompSpecified(c) || this.encSpec.rts.isCompSpecified(c)
						|| this.encSpec.sss.isCompSpecified(c) || this.encSpec.css.isCompSpecified(c)
						|| this.encSpec.pss.isCompSpecified(c) || this.encSpec.cblks.isCompSpecified(c)
						|| (this.encSpec.tts.isCompSpecified(c) && this.encSpec.tts.getCompDef(c).equals("predict")))
				{
					this.writeCOC(false, tileIdx, c);
				}
			}
		}

		// +--------------------------+
		// | QCD maker segment |
		// +--------------------------+
		boolean tileQCDwritten = false;
		if (this.encSpec.qts.isTileSpecified(tileIdx) || this.encSpec.qsss.isTileSpecified(tileIdx)
				|| this.encSpec.dls.isTileSpecified(tileIdx) || this.encSpec.gbs.isTileSpecified(tileIdx))
		{
			this.writeTileQCD(tileIdx);
			tileQCDwritten = true;
		}
		else
		{
			this.deftilenr = this.defimgn;
		}

		// +--------------------------+
		// | QCC maker segment |
		// +--------------------------+
		for (int c = 0; c < this.nComp; c++)
		{
			if (this.dwt.getNomRangeBits(c) != this.deftilenr || this.encSpec.qts.isTileCompSpecified(tileIdx, c)
					|| this.encSpec.qsss.isTileCompSpecified(tileIdx, c) || this.encSpec.dls.isTileCompSpecified(tileIdx, c)
					|| this.encSpec.gbs.isTileCompSpecified(tileIdx, c))
			{
				this.writeTileQCC(tileIdx, c);
			}
			else if (tileQCDwritten)
			{
				if (this.encSpec.qts.isCompSpecified(c) || this.encSpec.qsss.isCompSpecified(c) || this.encSpec.dls.isCompSpecified(c)
						|| this.encSpec.gbs.isCompSpecified(c))
				{
					this.writeTileQCC(tileIdx, c);
				}
			}
		}

		// +--------------------------+
		// | RGN maker segment |
		// +--------------------------+
		if (this.roiSc.useRoi() && (!this.roiSc.getBlockAligned()))
			this.writeRGN(tileIdx);

		// +--------------------------+
		// | POC maker segment |
		// +--------------------------+
		final Progression[] prog;
		if (this.encSpec.pocs.isTileSpecified(tileIdx))
		{
			prog = (Progression[]) (this.encSpec.pocs.getTileDef(tileIdx));
			if (1 < prog.length)
				this.writePOC(false, tileIdx);
		}

		// +--------------------------+
		// | SOD maker |
		// +--------------------------+
		this.hbuf.writeByte(Markers.SOD >> 8);
		this.hbuf.writeByte(Markers.SOD);
	}
}
