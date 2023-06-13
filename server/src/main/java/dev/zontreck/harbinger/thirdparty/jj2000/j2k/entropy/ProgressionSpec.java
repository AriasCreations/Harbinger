/*
 * CVS identifier:
 *
 * $Id: ProgressionSpec.java,v 1.19 2001/05/02 14:08:42 grosbois Exp $
 *
 * Class:                   ProgressionSpec
 *
 * Description:             Specification of the progression(s) type(s) and
 *                          changes of progression.
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
package dev.zontreck.harbinger.thirdparty.jj2000.j2k.entropy;

import dev.zontreck.harbinger.thirdparty.jj2000.j2k.IntegerSpec;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.ModuleSpec;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.codestream.ProgressionType;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.encoder.EncoderParam;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.util.ParameterList;

import java.util.StringTokenizer;
import java.util.Vector;

/**
 * This class extends ModuleSpec class for progression type(s) and progression
 * order changes holding purposes.
 *
 * <p>
 * It stores the progression type(s) used in the codestream. There can be
 * several progression type(s) if progression order changes are used (POC
 * markers).
 */
public class ProgressionSpec extends ModuleSpec {
	/**
	 * Creates a new ProgressionSpec object for the specified number of tiles
	 * and components.
	 *
	 * @param nt   The number of tiles
	 * @param nc   The number of components
	 * @param type the type of the specification module i.e. tile specific,
	 *             component specific or both. The ProgressionSpec class should
	 *             only be used only with the type ModuleSpec.SPEC_TYPE_TILE.
	 */
	public ProgressionSpec ( final int nt , final int nc , final byte type ) {
		super ( nt , nc , type );
		if ( ModuleSpec.SPEC_TYPE_TILE != type ) {
			throw new Error ( "Illegal use of class ProgressionSpec !" );
		}
	}

	/**
	 * Creates a new ProgressionSpec object for the specified number of tiles,
	 * components and the ParameterList instance.
	 *
	 * @param nt   The number of tiles
	 * @param nc   The number of components
	 * @param nl   The number of layer
	 * @param dls  The number of decomposition levels specifications
	 * @param type the type of the specification module. The ProgressionSpec
	 *             class should only be used only with the type
	 *             ModuleSpec.SPEC_TYPE_TILE.
	 * @param ep   The Parameters
	 */
	public ProgressionSpec ( final int nt , final int nc , final int nl , final IntegerSpec dls , final byte type , final EncoderParam ep , final String params ) {
		super ( nt , nc , type );
		this.initialize ( nt , nc , nl , dls , type , null == ep.getROIs ( ) , params );
	}

	/**
	 * Creates a new ProgressionSpec object for the specified number of tiles,
	 * components and the ParameterList instance.
	 *
	 * @param nt   The number of tiles
	 * @param nc   The number of components
	 * @param nl   The number of layer
	 * @param dls  The number of decomposition levels specifications
	 * @param type the type of the specification module. The ProgressionSpec
	 *             class should only be used only with the type
	 *             ModuleSpec.SPEC_TYPE_TILE.
	 * @param pl   The ParameterList instance
	 */
	public ProgressionSpec ( final int nt , final int nc , final int nl , final IntegerSpec dls , final byte type , final ParameterList pl ) {
		super ( nt , nc , type );
		this.initialize ( nt , nc , nl , dls , type , null == pl.getParameter ( "rois" ) , pl.getParameter ( "Aptype" ) );
	}

	private void initialize ( final int nt , final int nc , final int nl , final IntegerSpec dls , final byte type , final boolean nr , final String params ) {

		Progression[] prog;
		int mode = - 1;

		if ( null == params ) { // No parameter specified
			if ( nr ) {
				mode = this.checkProgMode ( "res" );
			}
			else {
				mode = this.checkProgMode ( "layer" );
			}

			if ( - 1 == mode ) {
				final String errMsg = "Unknown progression type : '" + params + "'";
				throw new IllegalArgumentException ( errMsg );
			}
			prog = new Progression[ 1 ];
			prog[ 0 ] = new Progression ( mode , 0 , nc , 0 , dls.getMax ( ) + 1 , nl );
			this.setDefault ( prog );
			return;
		}

		final StringTokenizer stk = new StringTokenizer ( params );
		byte curSpecType = ModuleSpec.SPEC_DEF; // Specification type of the
		// current parameter
		boolean[] tileSpec = null; // Tiles concerned by the specification
		String word = null; // current word
		String errMsg = null; // Error message
		boolean needInteger = false; // True if an integer value is expected
		int intType = 0; // Type of read integer value (0=index of first
		// resolution level, 1= index of first component, 2=index of first
		// layer not included, 3= index of first resolution level not
		// included, 4= index of first component not included
		final Vector<Progression> progression = new Vector<Progression> ( );
		int tmp = 0;
		Progression curProg = null;

		while ( stk.hasMoreTokens ( ) ) {
			word = stk.nextToken ( );

			if ( 't' == word.charAt ( 0 ) ) {// If progression were previously found, store them
				if ( 0 < progression.size ( ) ) {
					// Ensure that all information has been taken
					curProg.ce = nc;
					curProg.lye = nl;
					curProg.re = dls.getMax ( ) + 1;
					prog = new Progression[ progression.size ( ) ];
					progression.copyInto ( prog );
					if ( ModuleSpec.SPEC_DEF == curSpecType ) {
						this.setDefault ( prog );
					}
					else if ( ModuleSpec.SPEC_TILE_DEF == curSpecType ) {
						for ( int i = tileSpec.length - 1 ; 0 <= i ; i-- )
							if ( tileSpec[ i ] ) {
								this.setTileDef ( i , prog );
							}
					}
				}
				progression.removeAllElements ( );
				intType = - 1;
				needInteger = false;

				// Tiles specification
				tileSpec = ModuleSpec.parseIdx ( word , this.nTiles );
				curSpecType = ModuleSpec.SPEC_TILE_DEF;
			}
			else {// Here, words is either a Integer (progression bound index)
				// or a String (progression order type). This is determined
				// by the value of needInteger.
				if ( needInteger ) { // Progression bound info
					try {
						tmp = ( Integer.valueOf ( word ) ).intValue ( );
					} catch (
							final NumberFormatException e ) {
						// Progression has missing parameters
						throw new IllegalArgumentException ( "Progression order specification "
								+ "has missing parameters: " + params );
					}

					switch ( intType ) {
						case 0: // cs
							if ( 0 > tmp || tmp > ( dls.getMax ( ) + 1 ) )
								throw new IllegalArgumentException ( "Invalid res_start in '-Aptype' option: " + tmp );
							curProg.rs = tmp;
							break;
						case 1: // rs
							if ( 0 > tmp || tmp > nc ) {
								throw new IllegalArgumentException ( "Invalid comp_start in '-Aptype' option: " + tmp );
							}
							curProg.cs = tmp;
							break;
						case 2: // lye
							if ( 0 > tmp )
								throw new IllegalArgumentException ( "Invalid layer_end in '-Aptype' option: " + tmp );
							if ( tmp > nl ) {
								tmp = nl;
							}
							curProg.lye = tmp;
							break;
						case 3: // ce
							if ( 0 > tmp )
								throw new IllegalArgumentException ( "Invalid res_end in '-Aptype' option: " + tmp );
							if ( tmp > ( dls.getMax ( ) + 1 ) ) {
								tmp = dls.getMax ( ) + 1;
							}
							curProg.re = tmp;
							break;
						case 4: // re
							if ( 0 > tmp )
								throw new IllegalArgumentException ( "Invalid comp_end in '-Aptype'" + " option: " + tmp );
							if ( tmp > nc ) {
								tmp = nc;
							}
							curProg.ce = tmp;
							break;
						default:
							throw new IllegalArgumentException ( "unhandled integer type " + intType );
					}

					if ( 4 > intType ) {
						intType++;
						needInteger = true;
						continue;
					}
					else if ( 4 == intType ) {
						intType = 0;
						needInteger = false;
						continue;
					}
					else {
						throw new Error ( "Error in usage of 'Aptype' option: " + params );
					}
				}

				if ( ! needInteger ) { // Progression type info
					mode = this.checkProgMode ( word );
					if ( - 1 == mode ) {
						errMsg = "Unknown progression type : '" + word + "'";
						throw new IllegalArgumentException ( errMsg );
					}
					needInteger = true;
					intType = 0;
					if ( 0 == progression.size ( ) ) {
						curProg = new Progression ( mode , 0 , nc , 0 , dls.getMax ( ) + 1 , nl );
					}
					else {
						curProg = new Progression ( mode , 0 , nc , 0 , dls.getMax ( ) + 1 , nl );
					}
					progression.addElement ( curProg );
				}
			} // switch
		} // while

		if ( 0 == progression.size ( ) ) { // No progression defined
			if ( nr ) {
				mode = this.checkProgMode ( "res" );
			}
			else {
				mode = this.checkProgMode ( "layer" );
			}
			if ( - 1 == mode ) {
				errMsg = "Unknown progression type : '" + params + "'";
				throw new IllegalArgumentException ( errMsg );
			}
			prog = new Progression[ 1 ];
			prog[ 0 ] = new Progression ( mode , 0 , nc , 0 , dls.getMax ( ) + 1 , nl );
			this.setDefault ( prog );
			return;
		}

		// Ensure that all information has been taken
		curProg.ce = nc;
		curProg.lye = nl;
		curProg.re = dls.getMax ( ) + 1;

		// Store found progression
		prog = new Progression[ progression.size ( ) ];
		progression.copyInto ( prog );

		if ( ModuleSpec.SPEC_DEF == curSpecType ) {
			this.setDefault ( prog );
		}
		else if ( ModuleSpec.SPEC_TILE_DEF == curSpecType ) {
			for ( int i = tileSpec.length - 1 ; 0 <= i ; i-- )
				if ( tileSpec[ i ] ) {
					this.setTileDef ( i , prog );
				}
		}

		// Check that default value has been specified
		if ( null == getDefault ( ) ) {
			int ndefspec = 0;
			for ( int t = nt - 1 ; 0 <= t ; t-- ) {
				for ( int c = nc - 1 ; 0 <= c ; c-- ) {
					if ( ModuleSpec.SPEC_DEF == specValType[ t ][ c ] ) {
						ndefspec++;
					}
				}
			}

			// If some tile-component have received no specification, they
			// receive the default progressiveness.
			if ( 0 != ndefspec ) {
				if ( nr ) {
					mode = this.checkProgMode ( "res" );
				}
				else {
					mode = this.checkProgMode ( "layer" );
				}
				if ( - 1 == mode ) {
					errMsg = "Unknown progression type : '" + params + "'";
					throw new IllegalArgumentException ( errMsg );
				}
				prog = new Progression[ 1 ];
				prog[ 0 ] = new Progression ( mode , 0 , nc , 0 , dls.getMax ( ) + 1 , nl );
				this.setDefault ( prog );
			}
			else {
				// All tile-component have been specified, takes the first
				// tile-component value as default.
				this.setDefault ( this.getTileCompVal ( 0 , 0 ) );
				switch ( this.specValType[ 0 ][ 0 ] ) {
					case ModuleSpec.SPEC_TILE_DEF:
						for ( int c = nc - 1 ; 0 <= c ; c-- ) {
							if ( ModuleSpec.SPEC_TILE_DEF == specValType[ 0 ][ c ] )
								this.specValType[ 0 ][ c ] = ModuleSpec.SPEC_DEF;
						}
						this.tileDef[ 0 ] = null;
						break;
					case ModuleSpec.SPEC_COMP_DEF:
						for ( int t = nt - 1 ; 0 <= t ; t-- ) {
							if ( ModuleSpec.SPEC_COMP_DEF == specValType[ t ][ 0 ] )
								this.specValType[ t ][ 0 ] = ModuleSpec.SPEC_DEF;
						}
						this.compDef[ 0 ] = null;
						break;
					case ModuleSpec.SPEC_TILE_COMP:
						this.specValType[ 0 ][ 0 ] = ModuleSpec.SPEC_DEF;
						this.tileCompVal.put ( "t0c0" , null );
						break;
					default:
						throw new IllegalArgumentException ( "unhandled spec tile type " + this.specValType[ 0 ][ 0 ] + " in transformation" );
				}
			}
		}
	}

	/**
	 * Check if the progression mode exists and if so, return its integer value.
	 * It returns -1 otherwise.
	 *
	 * @param mode The progression mode stored in a string
	 * @return The integer value of the progression mode or -1 if the
	 * progression mode does not exist.
	 * @see ProgressionType
	 */
	private int checkProgMode ( final String mode ) {
		if ( "res".equals ( mode ) ) {
			return ProgressionType.RES_LY_COMP_POS_PROG;
		}
		else if ( "layer".equals ( mode ) ) {
			return ProgressionType.LY_RES_COMP_POS_PROG;
		}
		else if ( "pos-comp".equals ( mode ) ) {
			return ProgressionType.POS_COMP_RES_LY_PROG;
		}
		else if ( "comp-pos".equals ( mode ) ) {
			return ProgressionType.COMP_POS_RES_LY_PROG;
		}
		else if ( "res-pos".equals ( mode ) ) {
			return ProgressionType.RES_POS_COMP_LY_PROG;
		}
		else {
			// No corresponding progression mode, we return -1.
			return - 1;
		}
	}
}
