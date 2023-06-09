/*
 * CVS identifier:
 *
 * $Id: QuantTypeSpec.java,v 1.18 2001/10/24 12:05:18 grosbois Exp $
 *
 * Class:                   QuantTypeSpec
 *
 * Description:             Quantization type specifications
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
package dev.zontreck.harbinger.thirdparty.jj2000.j2k.quantization;

import dev.zontreck.harbinger.thirdparty.jj2000.j2k.ModuleSpec;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.util.ParameterList;

import java.util.StringTokenizer;

/**
 * This class extends ModuleSpec class in order to hold specifications about the
 * quantization type to use in each tile-component. Supported quantization type
 * are:<br>
 *
 * <ul>
 * <li>Reversible (no quantization)</li>
 * <li>Derived (the quantization step size is derived from the one of the
 * LL-subband)</li>
 * <li>Expounded (the quantization step size of each subband is signalled in the
 * codestream headers)</li>
 * </ul>
 *
 * @see ModuleSpec
 */
public class QuantTypeSpec extends ModuleSpec {

	/**
	 * Constructs an empty 'QuantTypeSpec' with the specified number of tiles
	 * and components. This constructor is called by the decoder.
	 *
	 * @param nt   Number of tiles
	 * @param nc   Number of components
	 * @param type the type of the allowed specifications for this module i.e.
	 *             tile specific, component specific or both.
	 */
	public QuantTypeSpec ( final int nt , final int nc , final byte type ) {
		super ( nt , nc , type );
	}

	/**
	 * Constructs a new 'QuantTypeSpec' for the specified number of components
	 * and tiles and the arguments of "-Qtype" option. This constructor is
	 * called by the encoder.
	 *
	 * @param nt   The number of tiles
	 * @param nc   The number of components
	 * @param type the type of the specification module i.e. tile specific,
	 *             component specific or both.
	 * @param pl   The ParameterList
	 */
	public QuantTypeSpec ( final int nt , final int nc , final byte type , final ParameterList pl ) {
		super ( nt , nc , type );

		final String param = pl.getParameter ( "Qtype" );
		if ( null == param ) {
			if ( pl.getBooleanParameter ( "lossless" ) ) {
				this.setDefault ( "reversible" );
			}
			else {
				this.setDefault ( "expounded" );
			}
			return;
		}

		// Parse argument
		final StringTokenizer stk = new StringTokenizer ( param );
		String word; // current word
		byte curSpecValType = ModuleSpec.SPEC_DEF; // Specification type of the
		// current parameter
		boolean[] tileSpec = null; // Tiles concerned by the specification
		boolean[] compSpec = null; // Components concerned by the specification

		while ( stk.hasMoreTokens ( ) ) {
			word = stk.nextToken ( ).toLowerCase ( );

			switch ( word.charAt ( 0 ) ) {
				case 't': // Tiles specification
					tileSpec = ModuleSpec.parseIdx ( word , this.nTiles );
					if ( ModuleSpec.SPEC_COMP_DEF == curSpecValType ) {
						curSpecValType = ModuleSpec.SPEC_TILE_COMP;
					}
					else {
						curSpecValType = ModuleSpec.SPEC_TILE_DEF;
					}
					break;
				case 'c': // Components specification
					compSpec = ModuleSpec.parseIdx ( word , this.nComp );
					if ( ModuleSpec.SPEC_TILE_DEF == curSpecValType ) {
						curSpecValType = ModuleSpec.SPEC_TILE_COMP;
					}
					else {
						curSpecValType = ModuleSpec.SPEC_COMP_DEF;
					}
					break;
				case 'r': // reversible specification
				case 'd': // derived quantization step size specification
				case 'e': // expounded quantization step size specification
					if ( ! "reversible".equalsIgnoreCase ( word ) && ! "derived".equalsIgnoreCase ( word ) && ! "expounded".equalsIgnoreCase ( word ) ) {
						throw new IllegalArgumentException ( "Unknown parameter for '-Qtype' option: " + word );
					}

					if ( pl.getBooleanParameter ( "lossless" ) && ( "derived".equalsIgnoreCase ( word ) || "expounded".equalsIgnoreCase ( word ) ) ) {
						throw new IllegalArgumentException ( "Cannot use non reversible quantization with " + "'-lossless' option" );
					}

					if ( ModuleSpec.SPEC_DEF == curSpecValType ) {
						// Default specification
						this.setDefault ( word );
					}
					else if ( ModuleSpec.SPEC_TILE_DEF == curSpecValType ) {
						// Tile default specification
						for ( int i = tileSpec.length - 1 ; 0 <= i ; i-- ) {
							if ( tileSpec[ i ] ) {
								this.setTileDef ( i , word );
							}
						}
					}
					else if ( ModuleSpec.SPEC_COMP_DEF == curSpecValType ) {
						// Component default specification
						for ( int i = compSpec.length - 1 ; 0 <= i ; i-- )
							if ( compSpec[ i ] ) {
								this.setCompDef ( i , word );
							}
					}
					else {
						// Tile-component specification
						for ( int i = tileSpec.length - 1 ; 0 <= i ; i-- ) {
							for ( int j = compSpec.length - 1 ; 0 <= j ; j-- ) {
								if ( tileSpec[ i ] && compSpec[ j ] ) {
									this.setTileCompVal ( i , j , word );
								}
							}
						}
					}

					// Re-initialize
					curSpecValType = ModuleSpec.SPEC_DEF;
					tileSpec = null;
					compSpec = null;
					break;

				default:
					throw new IllegalArgumentException ( "Unknown parameter for '-Qtype' option: " + word );
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

			// If some tile-component have received no specification, the
			// quantization type is 'reversible' (if '-lossless' is specified)
			// or 'expounded' (if not).
			if ( 0 != ndefspec ) {
				if ( pl.getBooleanParameter ( "lossless" ) ) {
					this.setDefault ( "reversible" );
				}
				else {
					this.setDefault ( "expounded" );
				}
			}
			else {
				// All tile-component have been specified, takes arbitrarily
				// the first tile-component value as default and modifies the
				// specification type of all tile-component sharing this
				// value.
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
						throw new IllegalArgumentException ( "unhandled spec tile type " + this.specValType[ 0 ][ 0 ] + " in quantization" );
				}
			}
		}
	}

	/**
	 * Returns true if given tile-component uses derived quantization step size.
	 *
	 * @param t Tile index
	 * @param c Component index
	 * @return True if derived quantization step size
	 */
	public boolean isDerived ( final int t , final int c ) {
		return this.getTileCompVal ( t , c ).equals ( "derived" );
	}

	/**
	 * Check the reversibility of the given tile-component.
	 *
	 * @param t The index of the tile
	 * @param c The index of the component
	 * @return Whether or not the tile-component is reversible
	 */
	public boolean isReversible ( final int t , final int c ) {
		return this.getTileCompVal ( t , c ).equals ( "reversible" );
	}

	/**
	 * Check the reversibility of the whole image.
	 *
	 * @return Whether or not the whole image is reversible
	 */
	public boolean isFullyReversible ( ) {
		// The whole image is reversible if default specification is
		// rev and no tile default, component default and
		// tile-component value has been specificied
		if ( this.getDefault ( ).equals ( "reversible" ) ) {
			for ( int t = this.nTiles - 1 ; 0 <= t ; t-- )
				for ( int c = this.nComp - 1 ; 0 <= c ; c-- )
					if ( ModuleSpec.SPEC_DEF != specValType[ t ][ c ] )
						return false;
			return true;
		}

		return false;
	}

	/**
	 * Check the irreversibility of the whole image.
	 *
	 * @return Whether or not the whole image is reversible
	 */
	public boolean isFullyNonReversible ( ) {
		// The whole image is irreversible no tile-component is reversible
		for ( int t = this.nTiles - 1 ; 0 <= t ; t-- )
			for ( int c = this.nComp - 1 ; 0 <= c ; c-- )
				if ( this.getSpec ( t , c ).equals ( "reversible" ) )
					return false;
		return true;
	}

}
