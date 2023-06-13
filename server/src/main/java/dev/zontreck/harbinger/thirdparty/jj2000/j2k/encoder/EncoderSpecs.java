/*
 * CVS identifier:
 *
 * $Id: EncoderSpecs.java,v 1.35 2001/05/08 16:10:40 grosbois Exp $
 *
 * Class:                   EncoderSpecs
 *
 * Description:             Hold all encoder specifications
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
package dev.zontreck.harbinger.thirdparty.jj2000.j2k.encoder;

import dev.zontreck.harbinger.thirdparty.jj2000.j2k.IntegerSpec;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.ModuleSpec;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.StringSpec;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.entropy.CBlkSizeSpec;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.entropy.PrecinctSizeSpec;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.entropy.ProgressionSpec;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.image.BlkImgDataSrc;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.image.CompTransfSpec;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.image.forwcomptransf.ForwCompTransfSpec;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.quantization.GuardBitsSpec;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.quantization.QuantStepSizeSpec;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.quantization.QuantTypeSpec;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.quantization.quantizer.Quantizer;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.roi.MaxShiftSpec;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.util.ParameterList;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.wavelet.analysis.AnWTFilterSpec;

/**
 * This class holds references to each module specifications used in the
 * encoding chain. This avoid big amount of arguments in method calls. A
 * specification contains values of each tile-component for one module. All
 * members must be instance of ModuleSpec class (or its children).
 *
 * @see ModuleSpec
 */
public class EncoderSpecs {

	/**
	 * ROI maxshift value specifications
	 */
	public MaxShiftSpec rois;

	/**
	 * Quantization type specifications
	 */
	public QuantTypeSpec qts;

	/**
	 * Quantization normalized base step size specifications
	 */
	public QuantStepSizeSpec qsss;

	/**
	 * Number of guard bits specifications
	 */
	public GuardBitsSpec gbs;

	/**
	 * Analysis wavelet filters specifications
	 */
	public AnWTFilterSpec wfs;

	/**
	 * Component transformation specifications
	 */
	public CompTransfSpec cts;

	/**
	 * Number of decomposition levels specifications
	 */
	public IntegerSpec dls;

	/**
	 * The length calculation specifications
	 */
	public StringSpec lcs;

	/**
	 * The termination type specifications
	 */
	public StringSpec tts;

	/**
	 * Error resilience segment symbol use specifications
	 */
	public StringSpec sss;

	/**
	 * Causal stripes specifications
	 */
	public StringSpec css;

	/**
	 * Regular termination specifications
	 */
	public StringSpec rts;

	/**
	 * MQ reset specifications
	 */
	public StringSpec mqrs;

	/**
	 * By-pass mode specifications
	 */
	public StringSpec bms;

	/**
	 * Precinct partition specifications
	 */
	public PrecinctSizeSpec pss;

	/**
	 * Start of packet (SOP) marker use specification
	 */
	public StringSpec sops;

	/**
	 * End of packet header (EPH) marker use specification
	 */
	public StringSpec ephs;

	/**
	 * Code-blocks sizes specification
	 */
	public CBlkSizeSpec cblks;

	/**
	 * Progression/progression changes specification
	 */
	public ProgressionSpec pocs;

	/**
	 * The number of tiles within the image
	 */
	public int nTiles;

	/**
	 * The number of components within the image
	 */
	public int nComp;

	/**
	 * Initialize all members with the given number of tiles and components and
	 * the command-line arguments stored in a ParameterList instance
	 *
	 * @param nt     Number of tiles
	 * @param nc     Number of components
	 * @param imgsrc The image source (used to get the image size)
	 * @param pl     The ParameterList instance
	 */
	public EncoderSpecs ( final int nt , final int nc , final BlkImgDataSrc imgsrc , final ParameterList pl ) {
		this.nTiles = nt;
		this.nComp = nc;

		// ROI
		this.rois = new MaxShiftSpec ( nt , nc , ModuleSpec.SPEC_TYPE_TILE_COMP );

		// Quantization
		pl.checkList ( Quantizer.OPT_PREFIX , ParameterList.toNameArray ( Quantizer.getParameterInfo ( ) ) );
		this.qts = new QuantTypeSpec ( nt , nc , ModuleSpec.SPEC_TYPE_TILE_COMP , pl );
		this.qsss = new QuantStepSizeSpec ( nt , nc , ModuleSpec.SPEC_TYPE_TILE_COMP , pl );
		this.gbs = new GuardBitsSpec ( nt , nc , ModuleSpec.SPEC_TYPE_TILE_COMP , pl );

		// Wavelet transform
		this.wfs = new AnWTFilterSpec ( nt , nc , ModuleSpec.SPEC_TYPE_TILE_COMP , this.qts , pl );
		this.dls = new IntegerSpec ( nt , nc , ModuleSpec.SPEC_TYPE_TILE_COMP , pl , "Wlev" );

		// Component transformation
		this.cts = new ForwCompTransfSpec ( nt , nc , ModuleSpec.SPEC_TYPE_TILE , this.wfs , pl );

		// Entropy coder
		final String[] strLcs = { "near_opt" , "lazy_good" , "lazy" };
		this.lcs = new StringSpec ( nt , nc , ModuleSpec.SPEC_TYPE_TILE_COMP , "Clen_calc" , strLcs , pl );
		final String[] strTerm = { "near_opt" , "easy" , "predict" , "full" };
		this.tts = new StringSpec ( nt , nc , ModuleSpec.SPEC_TYPE_TILE_COMP , "Cterm_type" , strTerm , pl );
		final String[] strBoolean = { "on" , "off" };
		this.sss = new StringSpec ( nt , nc , ModuleSpec.SPEC_TYPE_TILE_COMP , "Cseg_symbol" , strBoolean , pl );
		this.css = new StringSpec ( nt , nc , ModuleSpec.SPEC_TYPE_TILE_COMP , "Ccausal" , strBoolean , pl );
		this.rts = new StringSpec ( nt , nc , ModuleSpec.SPEC_TYPE_TILE_COMP , "Cterminate" , strBoolean , pl );
		this.mqrs = new StringSpec ( nt , nc , ModuleSpec.SPEC_TYPE_TILE_COMP , "CresetMQ" , strBoolean , pl );
		this.bms = new StringSpec ( nt , nc , ModuleSpec.SPEC_TYPE_TILE_COMP , "Cbypass" , strBoolean , pl );
		this.cblks = new CBlkSizeSpec ( nt , nc , ModuleSpec.SPEC_TYPE_TILE_COMP , pl );

		// Precinct partition
		this.pss = new PrecinctSizeSpec ( nt , nc , ModuleSpec.SPEC_TYPE_TILE_COMP , imgsrc , this.dls , pl );

		// Codestream
		this.sops = new StringSpec ( nt , nc , ModuleSpec.SPEC_TYPE_TILE , "Psop" , strBoolean , pl );
		this.ephs = new StringSpec ( nt , nc , ModuleSpec.SPEC_TYPE_TILE , "Peph" , strBoolean , pl );

	}
}
