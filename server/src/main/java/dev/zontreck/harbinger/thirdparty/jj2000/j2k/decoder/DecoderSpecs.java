/*
 * CVS identifier:
 *
 * $Id: DecoderSpecs.java,v 1.25 2002/07/25 15:06:17 grosbois Exp $
 *
 * Class:                   DecoderSpecs
 *
 * Description:             Hold all decoder specifications
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
package dev.zontreck.harbinger.thirdparty.jj2000.j2k.decoder;

import dev.zontreck.harbinger.thirdparty.jj2000.j2k.IntegerSpec;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.ModuleSpec;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.entropy.CBlkSizeSpec;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.entropy.PrecinctSizeSpec;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.image.CompTransfSpec;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.quantization.GuardBitsSpec;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.quantization.QuantStepSizeSpec;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.quantization.QuantTypeSpec;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.roi.MaxShiftSpec;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.wavelet.synthesis.SynWTFilterSpec;

/**
 * This class holds references to each module specifications used in the
 * decoding chain. This avoid big amount of arguments in method calls. A
 * specification contains values of each tile-component for one module. All
 * members must be instance of ModuleSpec class (or its children).
 *
 * @see ModuleSpec
 */
public class DecoderSpecs implements Cloneable {
	/**
	 * ICC Profiling specifications
	 */
	public ModuleSpec iccs;

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
	public SynWTFilterSpec wfs;

	/**
	 * Number of decomposition levels specifications
	 */
	public IntegerSpec dls;

	/**
	 * Number of layers specifications
	 */
	public IntegerSpec nls;

	/**
	 * Progression order specifications
	 */
	public IntegerSpec pos;

	/**
	 * The Entropy decoder options specifications
	 */
	public ModuleSpec ecopts;

	/**
	 * The component transformation specifications
	 */
	public CompTransfSpec cts;

	/**
	 * The progression changes specifications
	 */
	public ModuleSpec pcs;

	/**
	 * The error resilience specifications concerning the entropy decoder
	 */
	public ModuleSpec ers;

	/**
	 * Precinct partition specifications
	 */
	public PrecinctSizeSpec pss;

	/**
	 * The Start Of Packet (SOP) markers specifications
	 */
	public ModuleSpec sops;

	/**
	 * The End of Packet Headers (EPH) markers specifications
	 */
	public ModuleSpec ephs;

	/**
	 * Code-blocks sizes specification
	 */
	public CBlkSizeSpec cblks;

	/**
	 * Packed packet header specifications
	 */
	public ModuleSpec pphs;

	/**
	 * Initialize all members with the given number of tiles and components.
	 *
	 * @param nt Number of tiles
	 * @param nc Number of components
	 */
	public DecoderSpecs ( final int nt , final int nc ) {
		// Quantization
		this.qts = new QuantTypeSpec ( nt , nc , ModuleSpec.SPEC_TYPE_TILE_COMP );
		this.qsss = new QuantStepSizeSpec ( nt , nc , ModuleSpec.SPEC_TYPE_TILE_COMP );
		this.gbs = new GuardBitsSpec ( nt , nc , ModuleSpec.SPEC_TYPE_TILE_COMP );

		// Wavelet transform
		this.wfs = new SynWTFilterSpec ( nt , nc , ModuleSpec.SPEC_TYPE_TILE_COMP );
		this.dls = new IntegerSpec ( nt , nc , ModuleSpec.SPEC_TYPE_TILE_COMP );

		// Component transformation
		this.cts = new CompTransfSpec ( nt , nc , ModuleSpec.SPEC_TYPE_TILE_COMP );

		// Entropy decoder
		this.ecopts = new ModuleSpec ( nt , nc , ModuleSpec.SPEC_TYPE_TILE_COMP );
		this.ers = new ModuleSpec ( nt , nc , ModuleSpec.SPEC_TYPE_TILE_COMP );
		this.cblks = new CBlkSizeSpec ( nt , nc , ModuleSpec.SPEC_TYPE_TILE_COMP );

		// Precinct partition
		this.pss = new PrecinctSizeSpec ( nt , nc , ModuleSpec.SPEC_TYPE_TILE_COMP , this.dls );

		// Codestream
		this.nls = new IntegerSpec ( nt , nc , ModuleSpec.SPEC_TYPE_TILE );
		this.pos = new IntegerSpec ( nt , nc , ModuleSpec.SPEC_TYPE_TILE );
		this.pcs = new ModuleSpec ( nt , nc , ModuleSpec.SPEC_TYPE_TILE );
		this.sops = new ModuleSpec ( nt , nc , ModuleSpec.SPEC_TYPE_TILE );
		this.ephs = new ModuleSpec ( nt , nc , ModuleSpec.SPEC_TYPE_TILE );
		this.pphs = new ModuleSpec ( nt , nc , ModuleSpec.SPEC_TYPE_TILE );
		this.iccs = new ModuleSpec ( nt , nc , ModuleSpec.SPEC_TYPE_TILE );
		this.pphs.setDefault ( Boolean.FALSE );
	}

	/**
	 * Returns a copy of the current object.
	 */
	public DecoderSpecs getCopy ( ) {
		final DecoderSpecs decSpec2;
		try {
			decSpec2 = ( DecoderSpecs ) clone ( );
		} catch ( final CloneNotSupportedException e ) {
			throw new Error ( "Cannot clone the DecoderSpecs instance" );
		}
		// Quantization
		decSpec2.qts = ( QuantTypeSpec ) this.qts.getCopy ( );
		decSpec2.qsss = ( QuantStepSizeSpec ) this.qsss.getCopy ( );
		decSpec2.gbs = ( GuardBitsSpec ) this.gbs.getCopy ( );
		// Wavelet transform
		decSpec2.wfs = ( SynWTFilterSpec ) this.wfs.getCopy ( );
		decSpec2.dls = ( IntegerSpec ) this.dls.getCopy ( );
		// Component transformation
		decSpec2.cts = ( CompTransfSpec ) this.cts.getCopy ( );
		// ROI
		if ( null != rois ) {
			decSpec2.rois = ( MaxShiftSpec ) this.rois.getCopy ( );
		}
		return decSpec2;
	}
}
