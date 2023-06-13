/*
 * CVS identifier:
 *
 * $Id: ModuleSpec.java,v 1.24 2001/10/26 16:30:11 grosbois Exp $
 *
 * Class:                   ModuleSpec
 *
 * Description:             Generic class for storing module specs
 *
 *                           from WTFilterSpec (Diego Santa Cruz)
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
package dev.zontreck.harbinger.thirdparty.jj2000.j2k;

import dev.zontreck.harbinger.thirdparty.jj2000.j2k.image.Coord;

import java.security.InvalidParameterException;
import java.util.Hashtable;
import java.util.Iterator;

/**
 * This generic class is used to handle values to be used by a module for each
 * tile and component. It uses attribute to determine which value to use. It
 * should be extended by each module needing this feature.
 * <p>
 * This class might be used for values that are only tile specific or component
 * specific but not both.
 *
 * <p>
 * The attributes to use are defined by a hierarchy. The hierarchy is:
 *
 * <ul>
 * <li>Tile and component specific attribute</li>
 * <li>Tile specific default attribute</li>
 * <li>Component main default attribute</li>
 * <li>Main default attribute</li>
 * </ul>
 */
public class ModuleSpec implements Cloneable {

	/**
	 * The identifier for a specification module that applies only to components
	 */
	public static final byte SPEC_TYPE_COMP = 0;

	/**
	 * The identifier for a specification module that applies only to tiles
	 */
	public static final byte SPEC_TYPE_TILE = 1;

	/**
	 * The identifier for a specification module that applies both to tiles and
	 * components
	 */
	public static final byte SPEC_TYPE_TILE_COMP = 2;

	/**
	 * The identifier for default specification
	 */
	public static final byte SPEC_DEF = 0;

	/**
	 * The identifier for "component default" specification
	 */
	public static final byte SPEC_COMP_DEF = 1;

	/**
	 * The identifier for "tile default" specification
	 */
	public static final byte SPEC_TILE_DEF = 2;

	/**
	 * The identifier for a "tile-component" specification
	 */
	public static final byte SPEC_TILE_COMP = 3;

	/**
	 * The type of the specification module
	 */
	protected int specType;

	/**
	 * The number of tiles
	 */
	protected int nTiles;

	/**
	 * The number of components
	 */
	protected int nComp;

	/**
	 * The spec type for each tile-component. The first index is the tile index,
	 * the second is the component index.
	 */
	protected byte[][] specValType;

	/**
	 * Default value for each tile-component
	 */
	protected Object def;

	/**
	 * The default value for each component. Null if no component specific value
	 * is defined
	 */
	protected Object[] compDef;

	/**
	 * The default value for each tile. Null if no tile specific value is
	 * defined
	 */
	protected Object[] tileDef;

	/**
	 * The specific value for each tile-component. Value of tile 16 component 3
	 * is accessible through the hash value "t16c3". Null if no tile-component
	 * specific value is defined
	 */
	protected Hashtable<String, Object> tileCompVal;

	/**
	 * The specified value in string format
	 */
	protected String specified;

	/**
	 * Constructs a 'ModuleSpec' object, initializing all the components and
	 * tiles to the 'SPEC_DEF' spec val type, for the specified number of
	 * components and tiles.
	 *
	 * @param nt   The number of tiles
	 * @param nc   The number of components
	 * @param type the type of the specification module i.e. tile specific,
	 *             component specific or both.
	 */
	public ModuleSpec ( final int nt , final int nc , final byte type ) {
		this.nTiles = nt;
		this.nComp = nc;
		this.specValType = new byte[ nt ][ nc ];
		switch ( type ) {
			case ModuleSpec.SPEC_TYPE_TILE:
				this.specType = ModuleSpec.SPEC_TYPE_TILE;
				break;
			case ModuleSpec.SPEC_TYPE_COMP:
				this.specType = ModuleSpec.SPEC_TYPE_COMP;
				break;
			case ModuleSpec.SPEC_TYPE_TILE_COMP:
				this.specType = ModuleSpec.SPEC_TYPE_TILE_COMP;
				break;
			default:
				throw new InvalidParameterException ( "Invalid spec Type: " + type );
		}
	}

	/**
	 * This method is responsible of parsing tile indexes set and component
	 * indexes set for an option. Such an argument must follow the following
	 * policy:<br>
	 *
	 * <tt>t\<indexes set\></tt> or <tt>c\<indexes set\></tt> where tile or
	 * component indexes are separated by commas or a dashes.
	 *
	 * <p>
	 * <u>Example:</u><br>
	 * <li> <tt>t0,3,4</tt> means tiles with indexes 0, 3 and 4.<br>
	 * <li> <tt>t2-4</tt> means tiles with indexes 2,3 and 4.<br>
	 * <p>
	 * It returns a boolean array skteching which tile or component are
	 * concerned by the next parameters.
	 *
	 * @param word   The word to parse.
	 * @param maxIdx Maximum authorized index
	 * @return Indexes concerned by this parameter.
	 */
	public static final boolean[] parseIdx ( final String word , final int maxIdx ) {
		final int nChar = word.length ( ); // Number of characters
		char c = word.charAt ( 0 ); // current character
		int idx = - 1; // Current (tile or component) index
		int lastIdx = - 1; // Last (tile or component) index
		boolean isDash = false; // Whether or not last separator was a dash

		final boolean[] idxSet = new boolean[ maxIdx ];
		int i = 1; // index of the current character

		while ( i < nChar ) {
			c = word.charAt ( i );
			if ( Character.isDigit ( c ) ) {
				if ( - 1 == idx ) {
					idx = 0;
				}
				idx = idx * 10 + ( c - '0' );
			}
			else {
				if ( - 1 == idx || ( ',' != c && '-' != c ) ) {
					throw new IllegalArgumentException ( "Bad construction for parameter: " + word );
				}
				if ( 0 > idx || idx >= maxIdx ) {
					throw new IllegalArgumentException ( "Out of range index in parameter `" + word + "' : " + idx );
				}

				// Found a comma
				if ( ',' == c ) {
					if ( isDash ) { // Previously found a dash, fill idxSet
						for ( int j = lastIdx + 1 ; j < idx ; j++ ) {
							idxSet[ j ] = true;
						}
					}
					isDash = false;
				}
				else {// Found a dash
					isDash = true;
				}

				// Udate idxSet
				idxSet[ idx ] = true;
				lastIdx = idx;
				idx = - 1;
			}
			i++;
		}

		// Process last found index
		if ( 0 > idx || idx >= maxIdx ) {
			throw new IllegalArgumentException ( "Out of range index in parameter `" + word + "' : " + idx );
		}
		if ( isDash ) {
			for ( int j = lastIdx + 1 ; j < idx ; j++ ) {
				idxSet[ j ] = true;
			}
		}
		idxSet[ idx ] = true;

		return idxSet;
	}

	public ModuleSpec getCopy ( ) {
		return ( ModuleSpec ) clone ( );
	}

	@Override
	protected Object clone ( ) {
		final ModuleSpec ms;
		try {
			ms = ( ModuleSpec ) super.clone ( );
		} catch ( final CloneNotSupportedException e ) {
			throw new Error ( "Error when cloning ModuleSpec instance" );
		}
		// Create a copy of the specValType array
		ms.specValType = new byte[ this.nTiles ][ this.nComp ];
		for ( int t = 0 ; t < this.nTiles ; t++ ) {
			System.arraycopy ( this.specValType[ t ] , 0 , ms.specValType[ t ] , 0 , this.nComp );
		}
		// Create a copy of tileDef
		if ( null != tileDef ) {
			ms.tileDef = new Object[ this.nTiles ];
			System.arraycopy ( this.tileDef , 0 , ms.tileDef , 0 , this.nTiles );
		}
		// Create a copy of tileCompVal
		if ( null != tileCompVal ) {
			ms.tileCompVal = new Hashtable<String, Object> ( );
			String tmpKey;
			Object tmpVal;
			for ( Iterator<String> iterator = tileCompVal.keySet ( ).iterator ( ) ; iterator.hasNext ( ) ; ) {
				tmpKey = iterator.next ( );
				tmpVal = this.tileCompVal.get ( tmpKey );
				ms.tileCompVal.put ( tmpKey , tmpVal );
			}
		}
		return ms;
	}

	/**
	 * Rotate the ModuleSpec instance by 90 degrees (this modifies only tile and
	 * tile-component specifications).
	 *
	 * @param anT Number of tiles along horizontal and vertical axis after
	 *            rotation.
	 */
	public void rotate90 ( final Coord anT ) {
		// Rotate specValType
		final byte[][] tmpsvt = new byte[ this.nTiles ][];
		int ax, ay;
		final Coord bnT = new Coord ( anT.y , anT.x );
		for ( int by = 0 ; by < bnT.y ; by++ ) {
			for ( int bx = 0 ; bx < bnT.x ; bx++ ) {
				ay = bx;
				ax = bnT.y - by - 1;
				tmpsvt[ ay * anT.x + ax ] = this.specValType[ by * bnT.x + bx ];
			}
		}
		this.specValType = tmpsvt;

		// Rotate tileDef
		if ( null != tileDef ) {
			final Object[] tmptd = new Object[ this.nTiles ];
			for ( int by = 0 ; by < bnT.y ; by++ ) {
				for ( int bx = 0 ; bx < bnT.x ; bx++ ) {
					ay = bx;
					ax = bnT.y - by - 1;
					tmptd[ ay * anT.x + ax ] = this.tileDef[ by * bnT.x + bx ];
				}
			}
			this.tileDef = tmptd;
		}

		// Rotate tileCompVal
		if ( null != tileCompVal && 0 < tileCompVal.size ( ) ) {
			final Hashtable<String, Object> tmptcv = new Hashtable<String, Object> ( );
			String tmpKey;
			Object tmpVal;
			int btIdx, atIdx;
			int i1, i2;
			int bx, by;
			for ( Iterator<String> iterator = tileCompVal.keySet ( ).iterator ( ) ; iterator.hasNext ( ) ; ) {
				tmpKey = iterator.next ( );
				tmpVal = this.tileCompVal.get ( tmpKey );
				i1 = tmpKey.indexOf ( 't' );
				i2 = tmpKey.indexOf ( 'c' );
				btIdx = ( Integer.valueOf ( tmpKey.substring ( i1 + 1 , i2 ) ) ).intValue ( );
				bx = btIdx % bnT.x;
				by = btIdx / bnT.x;
				ay = bx;
				ax = bnT.y - by - 1;
				atIdx = ax + ay * anT.x;
				tmptcv.put ( "t" + atIdx + tmpKey.substring ( i2 ) , tmpVal );
			}
			this.tileCompVal = tmptcv;
		}
	}

	/**
	 * Gets default value for this module.
	 *
	 * @return The default value (Must be casted before use)
	 */
	public Object getDefault ( ) {
		return this.def;
	}

	/**
	 * Sets default value for this module
	 */
	public void setDefault ( final Object value ) {
		this.def = value;
	}

	/**
	 * Sets default value for specified component and specValType tag if allowed
	 * by its priority.
	 *
	 * @param c Component index
	 */
	public void setCompDef ( final int c , final Object value ) {
		if ( SPEC_TYPE_TILE == specType ) {
			final String errMsg = "Option whose value is '" + value + "' cannot be "
					+ "specified for components as it is a 'tile only' specific option";
			throw new Error ( errMsg );
		}
		if ( null == compDef ) {
			this.compDef = new Object[ this.nComp ];
		}
		for ( int i = 0 ; i < this.nTiles ; i++ ) {
			if ( SPEC_COMP_DEF > specValType[ i ][ c ] ) {
				this.specValType[ i ][ c ] = ModuleSpec.SPEC_COMP_DEF;
			}
		}
		this.compDef[ c ] = value;
	}

	/**
	 * Gets default value of the specified component. If no specification have
	 * been entered for this component, returns default value.
	 *
	 * @param c Component index
	 * @return The default value for this component (Must be casted before use)
	 * @see #setCompDef
	 */
	public Object getCompDef ( final int c ) {
		if ( SPEC_TYPE_TILE == specType ) {
			throw new Error ( "Illegal use of ModuleSpec class" );
		}
		if ( null == compDef || null == compDef[ c ] ) {
			return def;
		}
		return this.compDef[ c ];
	}

	/**
	 * Sets default value for specified tile and specValType tag if allowed by
	 * its priority.
	 *
	 * @param t Tile index.
	 */
	public void setTileDef ( final int t , final Object value ) {
		if ( SPEC_TYPE_COMP == specType ) {
			final String errMsg = "Option whose value is '" + value + "' cannot be "
					+ "specified for tiles as it is a 'component only' specific option";
			throw new Error ( errMsg );
		}
		if ( null == tileDef ) {
			this.tileDef = new Object[ this.nTiles ];
		}
		for ( int i = 0 ; i < this.nComp ; i++ ) {
			if ( SPEC_TILE_DEF > specValType[ t ][ i ] ) {
				this.specValType[ t ][ i ] = ModuleSpec.SPEC_TILE_DEF;
			}
		}
		this.tileDef[ t ] = value;
	}

	/**
	 * Gets default value of the specified tile. If no specification has been
	 * entered, it returns the default value.
	 *
	 * @param t Tile index
	 * @return The default value for this tile (Must be casted before use)
	 * @see #setTileDef
	 */
	public Object getTileDef ( final int t ) {
		if ( SPEC_TYPE_COMP == specType ) {
			throw new Error ( "Illegal use of ModuleSpec class" );
		}
		if ( null == tileDef || null == tileDef[ t ] ) {
			return def;
		}
		return this.tileDef[ t ];
	}

	/**
	 * Sets value for specified tile-component.
	 *
	 * @param t Tie index
	 * @param c Component index
	 */
	public void setTileCompVal ( final int t , final int c , final Object value ) {
		if ( SPEC_TYPE_TILE_COMP != specType ) {
			String errMsg = "Option whose value is '" + value + "' cannot be specified for ";
			switch ( this.specType ) {
				case ModuleSpec.SPEC_TYPE_TILE:
					errMsg += "components as it is a 'tile only' specific option";
					break;
				case ModuleSpec.SPEC_TYPE_COMP:
					errMsg += "tiles as it is a 'component only' specific option";
					break;
				default:
					throw new InvalidParameterException ( "Invalid spec Type: " + this.specType );
			}
			throw new Error ( errMsg );
		}
		if ( null == tileCompVal )
			this.tileCompVal = new Hashtable<String, Object> ( );
		this.specValType[ t ][ c ] = ModuleSpec.SPEC_TILE_COMP;
		this.tileCompVal.put ( "t" + t + "c" + c , value );
	}

	/**
	 * Gets value of specified tile-component. This method calls getSpec but has
	 * a public access.
	 *
	 * @param t Tile index
	 * @param c Component index
	 * @return The value of this tile-component (Must be casted before use)
	 * @see #setTileCompVal
	 * @see #getSpec
	 */
	public Object getTileCompVal ( final int t , final int c ) {
		if ( SPEC_TYPE_TILE_COMP != specType ) {
			throw new Error ( "Illegal use of ModuleSpec class" );
		}
		return this.getSpec ( t , c );
	}

	/**
	 * Gets value of specified tile-component without knowing if a specific
	 * tile-component value has been previously entered. It first check if a
	 * tile-component specific value has been entered, then if a tile specific
	 * value exist, then if a component specific value exist. If not the default
	 * value is returned.
	 *
	 * @param t Tile index
	 * @param c Component index
	 * @return Value for this tile component.
	 */
	protected Object getSpec ( final int t , final int c ) {
		switch ( this.specValType[ t ][ c ] ) {
			case ModuleSpec.SPEC_DEF:
				return def;
			case ModuleSpec.SPEC_COMP_DEF:
				return this.getCompDef ( c );
			case ModuleSpec.SPEC_TILE_DEF:
				return this.getTileDef ( t );
			case ModuleSpec.SPEC_TILE_COMP:
				return this.tileCompVal.get ( "t" + t + "c" + c );
			default:
				throw new IllegalArgumentException ( "Not recognized spec type" );
		}
	}

	/**
	 * Return the spec type of the given tile-component.
	 *
	 * @param t Tile index
	 * @param c Component index
	 */
	public byte getSpecValType ( final int t , final int c ) {
		return this.specValType[ t ][ c ];
	}

	/**
	 * Whether or not specifications have been entered for the given component.
	 *
	 * @param c Index of the component
	 * @return True if component specification has been defined
	 */
	public boolean isCompSpecified ( final int c ) {
		return null != compDef && null != compDef[ c ];
	}

	/**
	 * Whether or not specifications have been entered for the given tile.
	 *
	 * @param t Index of the tile
	 * @return True if tile specification has been entered
	 */
	public boolean isTileSpecified ( final int t ) {
		return null != tileDef && null != tileDef[ t ];
	}

	/**
	 * Whether or not a tile-component specification has been defined
	 *
	 * @param t Tile index
	 * @param c Component index
	 * @return True if a tile-component specification has been defined.
	 */
	public boolean isTileCompSpecified ( final int t , final int c ) {
		return null != tileCompVal && null != tileCompVal.get ( "t" + t + "c" + c );
	}
}
