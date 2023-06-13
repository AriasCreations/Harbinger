/*
 * CVS identifier:
 *
 * $Id: HeaderInfo.java,v 1.3 2001/10/26 16:30:33 grosbois Exp $
 *
 * Class:                   HeaderInfo
 *
 * Description:             Holds information found in main and tile-part
 *                          headers 
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
package jj2000.j2k.codestream;

import jj2000.j2k.wavelet.*;

import java.nio.charset.StandardCharsets;
import java.security.InvalidParameterException;
import java.util.*;

/**
 * Classe that holds information found in the marker segments of the main and
 * tile-part headers. There is one inner-class per marker segment type found in
 * these headers.
 */
public class HeaderInfo implements Markers, ProgressionType, FilterTypes, Cloneable
{

	/** Internal class holding information found in the SIZ marker segment */
	public class SIZ implements Cloneable
	{
		public int lsiz;
		public int rsiz;
		public int xsiz;
		public int ysiz;
		public int x0siz;
		public int y0siz;
		public int xtsiz;
		public int ytsiz;
		public int xt0siz;
		public int yt0siz;
		public int csiz;
		public int[] ssiz;
		public int[] xrsiz;
		public int[] yrsiz;

		/** Component widths */
		private int[] compWidth;
		/** Maximum width among all components */
		private int maxCompWidth = -1;
		/** Component heights */
		private int[] compHeight;
		/** Maximum height among all components */
		private int maxCompHeight = -1;

		/**
		 * Width of the specified tile-component
		 *
		 * 
		 * @param c
		 *            Component index
		 */
		public int getCompImgWidth(final int c)
		{
			if (null == compWidth)
			{
				this.compWidth = new int[this.csiz];
				for (int cc = 0; cc < this.csiz; cc++)
				{
					this.compWidth[cc] = (int) (Math.ceil((this.xsiz) / (double) this.xrsiz[cc]) - Math.ceil(this.x0siz
							/ (double) this.xrsiz[cc]));
				}
			}
			return this.compWidth[c];
		}

		public int getMaxCompWidth()
		{
			if (null == compWidth)
			{
				this.compWidth = new int[this.csiz];
				for (int cc = 0; cc < this.csiz; cc++)
				{
					this.compWidth[cc] = (int) (Math.ceil((this.xsiz) / (double) this.xrsiz[cc]) - Math.ceil(this.x0siz
							/ (double) this.xrsiz[cc]));
				}
			}
			if (-1 == maxCompWidth)
			{
				for (int c = 0; c < this.csiz; c++)
				{
					if (this.compWidth[c] > this.maxCompWidth)
					{
						this.maxCompWidth = this.compWidth[c];
					}
				}
			}
			return this.maxCompWidth;
		}

		public int getCompImgHeight(final int c)
		{
			if (null == compHeight)
			{
				this.compHeight = new int[this.csiz];
				for (int cc = 0; cc < this.csiz; cc++)
				{
					this.compHeight[cc] = (int) (Math.ceil((this.ysiz) / (double) this.yrsiz[cc]) - Math.ceil(this.y0siz
							/ (double) this.yrsiz[cc]));
				}
			}
			return this.compHeight[c];
		}

		public int getMaxCompHeight()
		{
			if (null == compHeight)
			{
				this.compHeight = new int[this.csiz];
				for (int cc = 0; cc < this.csiz; cc++)
				{
					this.compHeight[cc] = (int) (Math.ceil((this.ysiz) / (double) this.yrsiz[cc]) - Math.ceil(this.y0siz
							/ (double) this.yrsiz[cc]));
				}
			}
			if (-1 == maxCompHeight)
			{
				for (int c = 0; c < this.csiz; c++)
				{
					if (this.compHeight[c] != this.maxCompHeight)
					{
						this.maxCompHeight = this.compHeight[c];
					}
				}
			}
			return this.maxCompHeight;
		}

		private int numTiles = -1;

		public int getNumTiles()
		{
			if (-1 == numTiles)
			{
				this.numTiles = ((this.xsiz - this.xt0siz + this.xtsiz - 1) / this.xtsiz) * ((this.ysiz - this.yt0siz + this.ytsiz - 1) / this.ytsiz);
			}
			return this.numTiles;
		}

		private boolean[] origSigned;

		public boolean isOrigSigned(final int c)
		{
			if (null == origSigned)
			{
				this.origSigned = new boolean[this.csiz];
				for (int cc = 0; cc < this.csiz; cc++)
				{
					this.origSigned[cc] = (1 == (ssiz[cc] >>> SSIZ_DEPTH_BITS));
				}
			}
			return this.origSigned[c];
		}

		private int[] origBitDepth;

		public int getOrigBitDepth(final int c)
		{
			if (null == origBitDepth)
			{
				this.origBitDepth = new int[this.csiz];
				for (int cc = 0; cc < this.csiz; cc++)
				{
					this.origBitDepth[cc] = (this.ssiz[cc] & ((1 << Markers.SSIZ_DEPTH_BITS) - 1)) + 1;
				}
			}
			return this.origBitDepth[c];
		}

		public SIZ getCopy()
		{
			SIZ ms = null;
			try
			{
				ms = (SIZ) clone();
			}
			catch (final CloneNotSupportedException e)
			{
				throw new Error("Cannot clone SIZ marker segment");
			}
			return ms;
		}

		/** Display information found in SIZ marker segment */
		@Override
		public String toString()
		{
			String str = "\n --- SIZ (" + this.lsiz + " bytes) ---\n";
			str += " Capabilities : " + this.rsiz + "\n";
			str += " Image dim.   : " + (this.xsiz - this.x0siz) + "x" + (this.ysiz - this.y0siz) + ", (off=" + this.x0siz + "," + this.y0siz + ")\n";
			str += " Tile dim.    : " + this.xtsiz + "x" + this.ytsiz + ", (off=" + this.xt0siz + "," + this.yt0siz + ")\n";
			str += " Component(s) : " + this.csiz + "\n";
			str += " Orig. depth  : ";
			for (int i = 0; i < this.csiz; i++)
			{
				str += this.getOrigBitDepth(i) + " ";
			}
			str += "\n";
			str += " Orig. signed : ";
			for (int i = 0; i < this.csiz; i++)
			{
				str += this.isOrigSigned(i) + " ";
			}
			str += "\n";
			str += " Subs. factor : ";
			for (int i = 0; i < this.csiz; i++)
			{
				str += this.xrsiz[i] + "," + this.yrsiz[i] + " ";
			}
			str += "\n";
			return str;
		}
	}

	/** Returns a new instance of SIZ */
	public SIZ getNewSIZ()
	{
		return new SIZ();
	}

	/** Internal class holding information found in the SOt marker segments */
	public class SOT
	{
		public int lsot;
		public int isot;
		public int psot;
		public int tpsot;
		public int tnsot;

		/** Display information found in this SOT marker segment */
		@Override
		public String toString()
		{
			String str = "\n --- SOT (" + this.lsot + " bytes) ---\n";
			str += "Tile index         : " + this.isot + "\n";
			str += "Tile-part length   : " + this.psot + " bytes\n";
			str += "Tile-part index    : " + this.tpsot + "\n";
			str += "Num. of tile-parts : " + this.tnsot + "\n";
			str += "\n";
			return str;
		}
	}

	/** Returns a new instance of SOT */
	public SOT getNewSOT()
	{
		return new SOT();
	}

	/** Internal class holding information found in the COD marker segments */
	public class COD implements Cloneable
	{
		public int lcod;
		public int scod;
		public int sgcod_po; // Progression order
		public int sgcod_nl; // Number of layers
		public int sgcod_mct; // Multiple component transformation
		public int spcod_ndl; // Number of decomposition levels
		public int spcod_cw; // Code-blocks width
		public int spcod_ch; // Code-blocks height
		public int spcod_cs; // Code-blocks style
		public int[] spcod_t = new int[1]; // Transformation
		public int[] spcod_ps; // Precinct size

		public COD getCopy()
		{
			COD ms = null;
			try
			{
				ms = (COD) clone();
			}
			catch (final CloneNotSupportedException e)
			{
				throw new Error("Cannot clone SIZ marker segment");
			}
			return ms;
		}

		/** Display information found in this COD marker segment */
		@Override
		public String toString()
		{
			String str = "\n --- COD (" + this.lcod + " bytes) ---\n";
			str += " Coding style   : ";
			if (0 == scod)
			{
				str += "Default";
			}
			else
			{
				if (0 != (scod & SCOX_PRECINCT_PARTITION))
					str += "Precints ";
				if (0 != (scod & SCOX_USE_SOP))
					str += "SOP ";
				if (0 != (scod & SCOX_USE_EPH))
					str += "EPH ";
				final int cb0x = (0 != (scod & SCOX_HOR_CB_PART)) ? 1 : 0;
				final int cb0y = (0 != (scod & SCOX_VER_CB_PART)) ? 1 : 0;
				if (0 != cb0x || 0 != cb0y)
				{
					str += "Code-blocks offset";
					str += "\n Cblk partition : " + cb0x + "," + cb0y;
				}
			}
			str += "\n";
			str += " Cblk style     : ";
			if (0 == spcod_cs)
			{
				str += "Default";
			}
			else
			{
				if (0 != (spcod_cs & 0x1))
					str += "Bypass ";
				if (0 != (spcod_cs & 0x2))
					str += "Reset ";
				if (0 != (spcod_cs & 0x4))
					str += "Terminate ";
				if (0 != (spcod_cs & 0x8))
					str += "Vert_causal ";
				if (0 != (spcod_cs & 0x10))
					str += "Predict ";
				if (0 != (spcod_cs & 0x20))
					str += "Seg_symb ";
			}
			str += "\n";
			str += " Num. of levels : " + this.spcod_ndl + "\n";
			switch (this.sgcod_po)
			{
				case ProgressionType.LY_RES_COMP_POS_PROG:
					str += " Progress. type : LY_RES_COMP_POS_PROG\n";
					break;
				case ProgressionType.RES_LY_COMP_POS_PROG:
					str += " Progress. type : RES_LY_COMP_POS_PROG\n";
					break;
				case ProgressionType.RES_POS_COMP_LY_PROG:
					str += " Progress. type : RES_POS_COMP_LY_PROG\n";
					break;
				case ProgressionType.POS_COMP_RES_LY_PROG:
					str += " Progress. type : POS_COMP_RES_LY_PROG\n";
					break;
				case ProgressionType.COMP_POS_RES_LY_PROG:
					str += " Progress. type : COMP_POS_RES_LY_PROG\n";
					break;
				default:
					throw new InvalidParameterException("Invalid Progress. Type: " + this.sgcod_po);
			}
			str += " Num. of layers : " + this.sgcod_nl + "\n";
			str += " Cblk dimension : " + (1 << (this.spcod_cw + 2)) + "x" + (1 << (this.spcod_ch + 2)) + "\n";
			switch (this.spcod_t[0])
			{
				case FilterTypes.W9X7:
					str += " Filter         : 9-7 irreversible\n";
					break;
				case FilterTypes.W5X3:
					str += " Filter         : 5-3 reversible\n";
					break;
				default:
					throw new InvalidParameterException("Invalid Filter Type: " + this.spcod_t[0]);
			}
			str += " Multi comp tr. : " + (1 == sgcod_mct) + "\n";
			if (null != spcod_ps)
			{
				str += " Precincts      : ";
				for (int i = 0; i < this.spcod_ps.length; i++)
				{
					str += (1 << (this.spcod_ps[i] & 0x000F)) + "x" + (1 << (((this.spcod_ps[i] & 0x00F0) >> 4))) + " ";
				}
			}
			str += "\n";
			return str;
		}
	}

	/** Returns a new instance of COD */
	public COD getNewCOD()
	{
		return new COD();
	}

	/** Internal class holding information found in the COC marker segments */
	public class COC
	{
		public int lcoc;
		public int ccoc;
		public int scoc;
		public int spcoc_ndl; // Number of decomposition levels
		public int spcoc_cw;
		public int spcoc_ch;
		public int spcoc_cs;
		public int[] spcoc_t = new int[1];
		public int[] spcoc_ps;

		/** Display information found in this COC marker segment */
		@Override
		public String toString()
		{
			String str = "\n --- COC (" + this.lcoc + " bytes) ---\n";
			str += " Component      : " + this.ccoc + "\n";
			str += " Coding style   : ";
			if (0 == scoc)
			{
				str += "Default";
			}
			else
			{
				if (0 != (scoc & 0x1))
					str += "Precints ";
				if (0 != (scoc & 0x2))
					str += "SOP ";
				if (0 != (scoc & 0x4))
					str += "EPH ";
			}
			str += "\n";
			str += " Cblk style     : ";
			if (0 == spcoc_cs)
			{
				str += "Default";
			}
			else
			{
				if (0 != (spcoc_cs & 0x1))
					str += "Bypass ";
				if (0 != (spcoc_cs & 0x2))
					str += "Reset ";
				if (0 != (spcoc_cs & 0x4))
					str += "Terminate ";
				if (0 != (spcoc_cs & 0x8))
					str += "Vert_causal ";
				if (0 != (spcoc_cs & 0x10))
					str += "Predict ";
				if (0 != (spcoc_cs & 0x20))
					str += "Seg_symb ";
			}
			str += "\n";
			str += " Num. of levels : " + this.spcoc_ndl + "\n";
			str += " Cblk dimension : " + (1 << (this.spcoc_cw + 2)) + "x" + (1 << (this.spcoc_ch + 2)) + "\n";
			switch (this.spcoc_t[0])
			{
				case FilterTypes.W9X7:
					str += " Filter         : 9-7 irreversible\n";
					break;
				case FilterTypes.W5X3:
					str += " Filter         : 5-3 reversible\n";
					break;
				default:
					throw new InvalidParameterException("Invalid Filter Type: " + this.spcoc_t[0]);
			}
			if (null != spcoc_ps)
			{
				str += " Precincts      : ";
				for (int i = 0; i < this.spcoc_ps.length; i++)
				{
					str += (1 << (this.spcoc_ps[i] & 0x000F)) + "x" + (1 << (((this.spcoc_ps[i] & 0x00F0) >> 4))) + " ";
				}
			}
			str += "\n";
			return str;
		}
	}

	/** Returns a new instance of COC */
	public COC getNewCOC()
	{
		return new COC();
	}

	/** Internal class holding information found in the RGN marker segments */
	public class RGN
	{
		public int lrgn;
		public int crgn;
		public int srgn;
		public int sprgn;

		/** Display information found in this RGN marker segment */
		@Override
		public String toString()
		{
			String str = "\n --- RGN (" + this.lrgn + " bytes) ---\n";
			str += " Component : " + this.crgn + "\n";
			if (0 == srgn)
			{
				str += " ROI style : Implicit\n";
			}
			else
			{
				str += " ROI style : Unsupported\n";
			}
			str += " ROI shift : " + this.sprgn + "\n";
			str += "\n";
			return str;
		}
	}

	/** Returns a new instance of RGN */
	public RGN getNewRGN()
	{
		return new RGN();
	}

	/** Internal class holding information found in the QCD marker segments */
	public class QCD
	{
		public int lqcd;
		public int sqcd;
		public int[][] spqcd;

		private int qType = -1;

		public int getQuantType()
		{
			if (-1 == qType)
			{
				this.qType = this.sqcd & ~(Markers.SQCX_GB_MSK << Markers.SQCX_GB_SHIFT);
			}
			return this.qType;
		}

		private int gb = -1;

		public int getNumGuardBits()
		{
			if (-1 == gb)
			{
				this.gb = (this.sqcd >> Markers.SQCX_GB_SHIFT) & Markers.SQCX_GB_MSK;
			}
			return this.gb;
		}

		/** Display information found in this QCD marker segment */
		@Override
		public String toString()
		{
			String str = "\n --- QCD (" + this.lqcd + " bytes) ---\n";
			str += " Quant. type    : ";
			final int qt = this.getQuantType();
			if (SQCX_NO_QUANTIZATION == qt)
				str += "No quantization \n";
			else if (SQCX_SCALAR_DERIVED == qt)
				str += "Scalar derived\n";
			else if (SQCX_SCALAR_EXPOUNDED == qt)
				str += "Scalar expounded\n";
			str += " Guard bits     : " + this.getNumGuardBits() + "\n";
			if (SQCX_NO_QUANTIZATION == qt)
			{
				str += " Exponents   :\n";
				int exp;
				for (int i = 0; i < this.spqcd.length; i++)
				{
					for (int j = 0; j < this.spqcd[i].length; j++)
					{
						if (0 == i && 0 == j)
						{
							exp = (this.spqcd[0][0] >> Markers.SQCX_EXP_SHIFT) & Markers.SQCX_EXP_MASK;
							str += "\tr=0 : " + exp + "\n";
						}
						else if (0 != i && 0 < j)
						{
							exp = (this.spqcd[i][j] >> Markers.SQCX_EXP_SHIFT) & Markers.SQCX_EXP_MASK;
							str += "\tr=" + i + ",s=" + j + " : " + exp + "\n";
						}
					}
				}
			}
			else
			{
				str += " Exp / Mantissa : \n";
				int exp;
				double mantissa;
				for (int i = 0; i < this.spqcd.length; i++)
				{
					for (int j = 0; j < this.spqcd[i].length; j++)
					{
						if (0 == i && 0 == j)
						{
							exp = (this.spqcd[0][0] >> 11) & 0x1f;
							mantissa = (-1.0f - ((float) (this.spqcd[0][0] & 0x07ff)) / (1 << 11)) / (-1 << exp);
							str += "\tr=0 : " + exp + " / " + mantissa + "\n";
						}
						else if (0 != i && 0 < j)
						{
							exp = (this.spqcd[i][j] >> 11) & 0x1f;
							mantissa = (-1.0f - ((float) (this.spqcd[i][j] & 0x07ff)) / (1 << 11)) / (-1 << exp);
							str += "\tr=" + i + ",s=" + j + " : " + exp + " / " + mantissa + "\n";
						}
					}
				}
			}
			str += "\n";
			return str;
		}
	}

	/** Returns a new instance of QCD */
	public QCD getNewQCD()
	{
		return new QCD();
	}

	/** Internal class holding information found in the QCC marker segments */
	public class QCC
	{
		public int lqcc;
		public int cqcc;
		public int sqcc;
		public int[][] spqcc;

		private int qType = -1;

		public int getQuantType()
		{
			if (-1 == qType)
			{
				this.qType = this.sqcc & ~(Markers.SQCX_GB_MSK << Markers.SQCX_GB_SHIFT);
			}
			return this.qType;
		}

		private int gb = -1;

		public int getNumGuardBits()
		{
			if (-1 == gb)
			{
				this.gb = (this.sqcc >> Markers.SQCX_GB_SHIFT) & Markers.SQCX_GB_MSK;
			}
			return this.gb;
		}

		/** Display information found in this QCC marker segment */
		@Override
		public String toString()
		{
			String str = "\n --- QCC (" + this.lqcc + " bytes) ---\n";
			str += " Component      : " + this.cqcc + "\n";
			str += " Quant. type    : ";
			final int qt = this.getQuantType();
			if (SQCX_NO_QUANTIZATION == qt)
				str += "No quantization \n";
			else if (SQCX_SCALAR_DERIVED == qt)
				str += "Scalar derived\n";
			else if (SQCX_SCALAR_EXPOUNDED == qt)
				str += "Scalar expounded\n";
			str += " Guard bits     : " + this.getNumGuardBits() + "\n";
			if (SQCX_NO_QUANTIZATION == qt)
			{
				str += " Exponents   :\n";
				int exp;
				for (int i = 0; i < this.spqcc.length; i++)
				{
					for (int j = 0; j < this.spqcc[i].length; j++)
					{
						if (0 == i && 0 == j)
						{
							exp = (this.spqcc[0][0] >> Markers.SQCX_EXP_SHIFT) & Markers.SQCX_EXP_MASK;
							str += "\tr=0 : " + exp + "\n";
						}
						else if (0 != i && 0 < j)
						{
							exp = (this.spqcc[i][j] >> Markers.SQCX_EXP_SHIFT) & Markers.SQCX_EXP_MASK;
							str += "\tr=" + i + ",s=" + j + " : " + exp + "\n";
						}
					}
				}
			}
			else
			{
				str += " Exp / Mantissa : \n";
				int exp;
				double mantissa;
				for (int i = 0; i < this.spqcc.length; i++)
				{
					for (int j = 0; j < this.spqcc[i].length; j++)
					{
						if (0 == i && 0 == j)
						{
							exp = (this.spqcc[0][0] >> 11) & 0x1f;
							mantissa = (-1.0f - ((float) (this.spqcc[0][0] & 0x07ff)) / (1 << 11)) / (-1 << exp);
							str += "\tr=0 : " + exp + " / " + mantissa + "\n";
						}
						else if (0 != i && 0 < j)
						{
							exp = (this.spqcc[i][j] >> 11) & 0x1f;
							mantissa = (-1.0f - ((float) (this.spqcc[i][j] & 0x07ff)) / (1 << 11)) / (-1 << exp);
							str += "\tr=" + i + ",s=" + j + " : " + exp + " / " + mantissa + "\n";
						}
					}
				}
			}
			str += "\n";
			return str;
		}
	}

	/** Returns a new instance of QCC */
	public QCC getNewQCC()
	{
		return new QCC();
	}

	/** Internal class holding information found in the POC marker segments */
	public class POC
	{
		public int lpoc;
		public int[] rspoc;
		public int[] cspoc;
		public int[] lyepoc;
		public int[] repoc;
		public int[] cepoc;
		public int[] ppoc;

		/** Display information found in this POC marker segment */
		@Override
		public String toString()
		{
			String str = "\n --- POC (" + this.lpoc + " bytes) ---\n";
			str += " Chg_idx RSpoc CSpoc LYEpoc REpoc CEpoc Ppoc\n";
			for (int chg = 0; chg < this.rspoc.length; chg++)
			{
				str += "   " + chg + "      " + this.rspoc[chg] + "     " + this.cspoc[chg] + "     " + this.lyepoc[chg] + "      "
						+ this.repoc[chg] + "     " + this.cepoc[chg];
				switch (this.ppoc[chg])
				{
					case ProgressionType.LY_RES_COMP_POS_PROG:
						str += "  LY_RES_COMP_POS_PROG\n";
						break;
					case ProgressionType.RES_LY_COMP_POS_PROG:
						str += "  RES_LY_COMP_POS_PROG\n";
						break;
					case ProgressionType.RES_POS_COMP_LY_PROG:
						str += "  RES_POS_COMP_LY_PROG\n";
						break;
					case ProgressionType.POS_COMP_RES_LY_PROG:
						str += "  POS_COMP_RES_LY_PROG\n";
						break;
					case ProgressionType.COMP_POS_RES_LY_PROG:
						str += "  COMP_POS_RES_LY_PROG\n";
						break;
					default:
						throw new InvalidParameterException("Invalid Progress. Type: " + this.ppoc[chg]);
				}
			}
			str += "\n";
			return str;
		}
	}

	/** Returns a new instance of POC */
	public POC getNewPOC()
	{
		return new POC();
	}

	/** Internal class holding information found in the CRG marker segment */
	public class CRG
	{
		public int lcrg;
		public int[] xcrg;
		public int[] ycrg;

		/** Display information found in the CRG marker segment */
		@Override
		public String toString()
		{
			String str = "\n --- CRG (" + this.lcrg + " bytes) ---\n";
			for (int c = 0; c < this.xcrg.length; c++)
			{
				str += " Component " + c + " offset : " + this.xcrg[c] + "," + this.ycrg[c] + "\n";
			}
			str += "\n";
			return str;
		}
	}

	/** Returns a new instance of CRG */
	public CRG getNewCRG()
	{
		return new CRG();
	}

	/** Internal class holding information found in the COM marker segments */
	public class COM
	{
		public int lcom;
		public int rcom;
		public byte[] ccom;

		/** Display information found in the COM marker segment */
		@Override
		public String toString()
		{
			String str = "\n --- COM (" + this.lcom + " bytes) ---\n";
			if (0 == rcom)
			{
				str += " Registration : General use (binary values)\n";
			}
			else if (1 == rcom)
			{
				str += " Registration : General use (IS 8859-15:1999 " + "(Latin) values)\n";
				str += " Text         : " + (new String(this.ccom, StandardCharsets.UTF_8)) + "\n";
			}
			else
			{
				str += " Registration : Unknown\n";
			}
			str += "\n";
			return str;
		}
	}

	/** Returns a new instance of COM */
	public COM getNewCOM()
	{
		this.ncom++;
		return new COM();
	}

	/** Returns the number of found COM marker segments */
	public int getNumCOM()
	{
		return this.ncom;
	}

	/** Reference to the SIZ marker segment found in main header */
	public SIZ siz;

	/**
	 * Reference to the SOT marker segments found in tile-part headers. The kwy
	 * is given by "t"+tileIdx"_tp"+tilepartIndex.
	 */
	public Hashtable<String, SOT> sot = new Hashtable<String, SOT>();

	/**
	 * Reference to the COD marker segments found in main and first tile-part
	 * header. The key is either "main" or "t"+tileIdx.
	 */
	public Hashtable<String, COD> cod = new Hashtable<String, COD>();

	/**
	 * Reference to the COC marker segments found in main and first tile-part
	 * header. The key is either "main_c"+componentIndex or
	 * "t"+tileIdx+"_c"+component_index.
	 */
	public Hashtable<String, COC> coc = new Hashtable<String, COC>();

	/**
	 * Reference to the RGN marker segments found in main and first tile-part
	 * header. The key is either "main_c"+componentIndex or
	 * "t"+tileIdx+"_c"+component_index.
	 */
	public Hashtable<String, RGN> rgn = new Hashtable<String, RGN>();

	/**
	 * Reference to the QCD marker segments found in main and first tile-part
	 * header. The key is either "main" or "t"+tileIdx.
	 */
	public Hashtable<String, QCD> qcd = new Hashtable<String, QCD>();

	/**
	 * Reference to the QCC marker segments found in main and first tile-part
	 * header. They key is either "main_c"+componentIndex or
	 * "t"+tileIdx+"_c"+component_index.
	 */
	public Hashtable<String, QCC> qcc = new Hashtable<String, QCC>();

	/**
	 * Reference to the POC marker segments found in main and first tile-part
	 * header. They key is either "main" or "t"+tileIdx.
	 */
	public Hashtable<String, POC> poc = new Hashtable<String, POC>();

	/** Reference to the CRG marker segment found in main header */
	public CRG crg;

	/**
	 * Reference to the COM marker segments found in main and tile-part headers.
	 * The key is either "main_"+comIdx or "t"+tileIdx+"_"+comIdx.
	 */
	public Hashtable<String, COM> com = new Hashtable<String, COM>();

	/** Number of found COM marker segment */
	private int ncom;

	/**
	 * Display information found in the different marker segments of the main
	 * header
	 */
	public String toStringMainHeader()
	{
		final int nc = this.siz.csiz;
		// SIZ
		String str = String.valueOf(this.siz);
		// COD
		if (null != cod.get("main"))
		{
			str += String.valueOf(this.cod.get("main"));
		}
		// COCs
		for (int c = 0; c < nc; c++)
		{
			if (null != coc.get("main_c" + c))
			{
				str += String.valueOf(this.coc.get("main_c" + c));
			}
		}
		// QCD
		if (null != qcd.get("main"))
		{
			str += String.valueOf(this.qcd.get("main"));
		}
		// QCCs
		for (int c = 0; c < nc; c++)
		{
			if (null != qcc.get("main_c" + c))
			{
				str += String.valueOf(this.qcc.get("main_c" + c));
			}
		}
		// RGN
		for (int c = 0; c < nc; c++)
		{
			if (null != rgn.get("main_c" + c))
			{
				str += String.valueOf(this.rgn.get("main_c" + c));
			}
		}
		// POC
		if (null != poc.get("main"))
		{
			str += String.valueOf(this.poc.get("main"));
		}
		// CRG
		if (null != crg)
		{
			str += String.valueOf(this.crg);
		}
		// COM
		for (int i = 0; i < this.ncom; i++)
		{
			if (null != com.get("main_" + i))
			{
				str += String.valueOf(this.com.get("main_" + i));
			}
		}
		return str;
	}

	/**
	 * Returns information found in the tile-part headers of a given tile.
	 * 
	 * @param t
	 *            index of the tile
	 * 
	 * @param ntp
	 *            Number of tile-parts
	 */
	public String toStringTileHeader(final int t, final int ntp)
	{
		final int nc = this.siz.csiz;
		String str = "";
		// SOT
		for (int i = 0; i < ntp; i++)
		{
			str += "Tile-part " + i + ", tile " + t + ":\n";
			str += String.valueOf(this.sot.get("t" + t + "_tp" + i));
		}
		// COD
		if (null != cod.get("t" + t))
		{
			str += String.valueOf(this.cod.get("t" + t));
		}
		// COCs
		for (int c = 0; c < nc; c++)
		{
			if (null != coc.get("t" + t + "_c" + c))
			{
				str += String.valueOf(this.coc.get("t" + t + "_c" + c));
			}
		}
		// QCD
		if (null != qcd.get("t" + t))
		{
			str += String.valueOf(this.qcd.get("t" + t));
		}
		// QCCs
		for (int c = 0; c < nc; c++)
		{
			if (null != qcc.get("t" + t + "_c" + c))
			{
				str += String.valueOf(this.qcc.get("t" + t + "_c" + c));
			}
		}
		// RGN
		for (int c = 0; c < nc; c++)
		{
			if (null != rgn.get("t" + t + "_c" + c))
			{
				str += String.valueOf(this.rgn.get("t" + t + "_c" + c));
			}
		}
		// POC
		if (null != poc.get("t" + t))
		{
			str += String.valueOf(this.poc.get("t" + t));
		}
		return str;
	}

	/**
	 * Returns information found in the tile-part headers of a given tile
	 * exception the SOT marker segment.
	 * 
	 * @param t
	 *            index of the tile
	 * 
	 * @param ntp
	 *            Number of tile-parts
	 */
	public String toStringThNoSOT(final int t, final int ntp)
	{
		final int nc = this.siz.csiz;
		String str = "";
		// COD
		if (null != cod.get("t" + t))
		{
			str += String.valueOf(this.cod.get("t" + t));
		}
		// COCs
		for (int c = 0; c < nc; c++)
		{
			if (null != coc.get("t" + t + "_c" + c))
			{
				str += String.valueOf(this.coc.get("t" + t + "_c" + c));
			}
		}
		// QCD
		if (null != qcd.get("t" + t))
		{
			str += String.valueOf(this.qcd.get("t" + t));
		}
		// QCCs
		for (int c = 0; c < nc; c++)
		{
			if (null != qcc.get("t" + t + "_c" + c))
			{
				str += String.valueOf(this.qcc.get("t" + t + "_c" + c));
			}
		}
		// RGN
		for (int c = 0; c < nc; c++)
		{
			if (null != rgn.get("t" + t + "_c" + c))
			{
				str += String.valueOf(this.rgn.get("t" + t + "_c" + c));
			}
		}
		// POC
		if (null != poc.get("t" + t))
		{
			str += String.valueOf(this.poc.get("t" + t));
		}
		return str;
	}

	/** Returns a copy of this object */
	public HeaderInfo getCopy(final int nt)
	{
		HeaderInfo nhi = null;
		// SIZ
		try
		{
			nhi = (HeaderInfo) this.clone();
		}
		catch (final CloneNotSupportedException e)
		{
			throw new Error("Cannot clone HeaderInfo instance");
		}
		nhi.siz = this.siz.getCopy();
		// COD
		if (null != cod.get("main"))
		{
			final COD ms = this.cod.get("main");
			nhi.cod.put("main", ms.getCopy());
		}
		for (int t = 0; t < nt; t++)
		{
			if (null != cod.get("t" + t))
			{
				final COD ms = this.cod.get("t" + t);
				nhi.cod.put("t" + t, ms.getCopy());
			}
		}
		return nhi;
	}
}
