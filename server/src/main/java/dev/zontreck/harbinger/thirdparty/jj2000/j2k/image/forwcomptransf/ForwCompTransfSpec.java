/*
 * CVS identifier:
 *
 * $Id: ForwCompTransfSpec.java,v 1.7 2001/05/08 16:10:18 grosbois Exp $
 *
 * Class:                   ForwCompTransfSpec
 *
 * Description:             Component Transformation specification for encoder
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
package dev.zontreck.harbinger.thirdparty.jj2000.j2k.image.forwcomptransf;

import dev.zontreck.harbinger.thirdparty.jj2000.j2k.ModuleSpec;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.image.CompTransfSpec;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.util.ParameterList;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.wavelet.FilterTypes;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.wavelet.analysis.AnWTFilter;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.wavelet.analysis.AnWTFilterSpec;

import java.util.StringTokenizer;

/**
 * This class extends CompTransfSpec class in order to hold encoder specific
 * aspects of CompTransfSpec.
 *
 * @see CompTransfSpec
 */
public class ForwCompTransfSpec extends CompTransfSpec implements FilterTypes {
	/**
	 * Constructs a new 'ForwCompTransfSpec' for the specified number of
	 * components and tiles, the wavelet filters type and the parameter of the
	 * option 'Mct'. This constructor is called by the encoder. It also checks
	 * that the arguments belong to the recognized arguments list.
	 *
	 * <p>
	 * This constructor chose the component transformation type depending on the
	 * wavelet filters : RCT with w5x3 filter and ICT with w9x7 filter. Note:
	 * All filters must use the same data type.
	 *
	 * @param nt   The number of tiles
	 * @param nc   The number of components
	 * @param type the type of the specification module i.e. tile specific,
	 *             component specific or both.
	 * @param wfs  The wavelet filter specifications
	 * @param pl   The ParameterList
	 */
	public ForwCompTransfSpec ( final int nt , final int nc , final byte type , final AnWTFilterSpec wfs , final ParameterList pl ) {
		super ( nt , nc , type );

		final String param = pl.getParameter ( "Mct" );

		if ( null == param ) { // The option has not been specified

			// If less than three component, do not use any component
			// transformation
			if ( 3 > nc ) {
				this.setDefault ( "none" );
				return;
			}
			// If the compression is lossless, uses RCT
			else if ( pl.getBooleanParameter ( "lossless" ) ) {
				this.setDefault ( "rct" );
				return;
			}
			else {
				AnWTFilter[][] anfilt;
				final int[] filtType = new int[ this.nComp ];
				for ( int c = 0 ; 3 > c ; c++ ) {
					anfilt = ( AnWTFilter[][] ) wfs.getCompDef ( c );
					filtType[ c ] = anfilt[ 0 ][ 0 ].getFilterType ( );
				}

				// Check that the three first components use the same filters
				boolean reject = false;
				for ( int c = 1 ; 3 > c ; c++ ) {
					if ( filtType[ c ] != filtType[ 0 ] ) {
						reject = true;
						break;
					}
				}

				if ( reject ) {
					this.setDefault ( "none" );
				}
				else {
					anfilt = ( AnWTFilter[][] ) wfs.getCompDef ( 0 );
					if ( FilterTypes.W9X7 == anfilt[ 0 ][ 0 ].getFilterType ( ) ) {
						this.setDefault ( "ict" );
					}
					else {
						this.setDefault ( "rct" );
					}
				}
			}

			// Each tile receives a component transform specification
			// according the type of wavelet filters that are used by the
			// three first components
			for ( int t = 0 ; t < nt ; t++ ) {
				AnWTFilter[][] anfilt;
				final int[] filtType = new int[ this.nComp ];
				for ( int c = 0 ; 3 > c ; c++ ) {
					anfilt = ( AnWTFilter[][] ) wfs.getTileCompVal ( t , c );
					filtType[ c ] = anfilt[ 0 ][ 0 ].getFilterType ( );
				}

				// Check that the three components use the same filters
				boolean reject = false;
				for ( int c = 1 ; c < this.nComp ; c++ ) {
					if ( filtType[ c ] != filtType[ 0 ] ) {
						reject = true;
						break;
					}
				}

				if ( reject ) {
					this.setTileDef ( t , "none" );
				}
				else {
					anfilt = ( AnWTFilter[][] ) wfs.getTileCompVal ( t , 0 );
					if ( FilterTypes.W9X7 == anfilt[ 0 ][ 0 ].getFilterType ( ) ) {
						this.setTileDef ( t , "ict" );
					}
					else {
						this.setTileDef ( t , "rct" );
					}
				}
			}
			return;
		}

		// Parse argument
		final StringTokenizer stk = new StringTokenizer ( param );
		String word; // current word
		byte curSpecType = ModuleSpec.SPEC_DEF; // Specification type of the
		// current parameter
		boolean[] tileSpec = null; // Tiles concerned by the

		while ( stk.hasMoreTokens ( ) ) {
			word = stk.nextToken ( );

			switch ( word.charAt ( 0 ) ) {
				case 't': // Tiles specification
					tileSpec = ModuleSpec.parseIdx ( word , this.nTiles );
					if ( ModuleSpec.SPEC_COMP_DEF == curSpecType ) {
						curSpecType = ModuleSpec.SPEC_TILE_COMP;
					}
					else {
						curSpecType = ModuleSpec.SPEC_TILE_DEF;
					}
					break;
				case 'c': // Components specification
					throw new IllegalArgumentException ( "Component specific parameters not allowed with "
							+ "'-Mct' option" );
				default:
					if ( "off".equals ( word ) ) {
						if ( ModuleSpec.SPEC_DEF == curSpecType ) {
							this.setDefault ( "none" );
						}
						else if ( ModuleSpec.SPEC_TILE_DEF == curSpecType ) {
							for ( int i = tileSpec.length - 1 ; 0 <= i ; i-- )
								if ( tileSpec[ i ] ) {
									this.setTileDef ( i , "none" );
								}
						}
					}
					else if ( "on".equals ( word ) ) {
						if ( 3 > nc ) {
							throw new IllegalArgumentException ( "Cannot use component transformation on a "
									+ "image with less than three components" );
						}

						if ( ModuleSpec.SPEC_DEF == curSpecType ) { // Set arbitrarily the default
							// value to RCT (later will be found the suitable
							// component transform for each tile)
							this.setDefault ( "rct" );
						}
						else if ( ModuleSpec.SPEC_TILE_DEF == curSpecType ) {
							for ( int i = tileSpec.length - 1 ; 0 <= i ; i-- ) {
								if ( tileSpec[ i ] ) {
									if ( FilterTypes.W5X3 == getFilterType ( i , wfs ) ) {
										this.setTileDef ( i , "rct" );
									}
									else {
										this.setTileDef ( i , "ict" );
									}
								}
							}
						}
					}
					else {
						throw new IllegalArgumentException ( "Default parameter of option Mct not recognized: " + param );
					}

					// Re-initialize
					curSpecType = ModuleSpec.SPEC_DEF;
					tileSpec = null;
					break;
			}
		}

		// Check that default value has been specified
		if ( null == getDefault ( ) ) {
			// If not, set arbitrarily the default value to 'none' but
			// specifies explicitely a default value for each tile depending
			// on the wavelet transform that is used
			this.setDefault ( "none" );

			for ( int t = 0 ; t < nt ; t++ ) {
				if ( this.isTileSpecified ( t ) ) {
					continue;
				}

				AnWTFilter[][] anfilt;
				final int[] filtType = new int[ this.nComp ];
				for ( int c = 0 ; 3 > c ; c++ ) {
					anfilt = ( AnWTFilter[][] ) wfs.getTileCompVal ( t , c );
					filtType[ c ] = anfilt[ 0 ][ 0 ].getFilterType ( );
				}

				// Check that the three components use the same filters
				boolean reject = false;
				for ( int c = 1 ; c < this.nComp ; c++ ) {
					if ( filtType[ c ] != filtType[ 0 ] ) {
						reject = true;
						break;
					}
				}

				if ( reject ) {
					this.setTileDef ( t , "none" );
				}
				else {
					anfilt = ( AnWTFilter[][] ) wfs.getTileCompVal ( t , 0 );
					if ( FilterTypes.W9X7 == anfilt[ 0 ][ 0 ].getFilterType ( ) ) {
						this.setTileDef ( t , "ict" );
					}
					else {
						this.setTileDef ( t , "rct" );
					}
				}
			}
		}

		// Check validity of component transformation of each tile compared to
		// the filter used.
		for ( int t = nt - 1 ; 0 <= t ; t-- ) {

			if ( this.getTileDef ( t ).equals ( "none" ) ) {
				// No comp. transf is used. No check is needed
				continue;
			}
			else if ( this.getTileDef ( t ).equals ( "rct" ) ) {
				// Tile is using Reversible component transform
				final int filterType = this.getFilterType ( t , wfs );
				switch ( filterType ) {
					case FilterTypes.W5X3: // OK
						break;
					case FilterTypes.W9X7: // Must use ICT
						if ( this.isTileSpecified ( t ) ) {
							// User has requested RCT -> Error
							throw new IllegalArgumentException ( "Cannot use RCT with 9x7 filter in tile " + t );
						}
						// Specify ICT for this tile
						this.setTileDef ( t , "ict" );
						break;
					default:
						throw new IllegalArgumentException ( "Default filter is not JPEG 2000 part I compliant" );
				}
			}
			else { // ICT
				final int filterType = this.getFilterType ( t , wfs );
				switch ( filterType ) {
					case FilterTypes.W5X3: // Must use RCT
						if ( this.isTileSpecified ( t ) ) {
							// User has requested ICT -> Error
							throw new IllegalArgumentException ( "Cannot use ICT with filter 5x3 in tile " + t );
						}
						this.setTileDef ( t , "rct" );
						break;
					case FilterTypes.W9X7: // OK
						break;
					default:
						throw new IllegalArgumentException ( "Default filter is not JPEG 2000 part I compliant" );

				}
			}
		}
	}

	/**
	 * Get the filter type common to all component of a given tile. If the tile
	 * index is -1, it searches common filter type of default specifications.
	 *
	 * @param t   The tile index
	 * @param wfs The analysis filters specifications
	 * @return The filter type common to all the components
	 */
	private int getFilterType ( final int t , final AnWTFilterSpec wfs ) {
		AnWTFilter[][] anfilt;
		final int[] filtType = new int[ this.nComp ];
		for ( int c = 0 ; c < this.nComp ; c++ ) {
			if ( - 1 == t ) {
				anfilt = ( AnWTFilter[][] ) wfs.getCompDef ( c );
			}
			else {
				anfilt = ( AnWTFilter[][] ) wfs.getTileCompVal ( t , c );
			}
			filtType[ c ] = anfilt[ 0 ][ 0 ].getFilterType ( );
		}

		// Check that all filters are the same one
		boolean reject = false;
		for ( int c = 1 ; c < this.nComp ; c++ ) {
			if ( filtType[ c ] != filtType[ 0 ] ) {
				reject = true;
				break;
			}
		}
		if ( reject ) {
			throw new IllegalArgumentException ( "Can not use component transformation when "
					+ "components do not use the same filters" );
		}
		return filtType[ 0 ];
	}
}
