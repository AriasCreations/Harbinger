/*
 * CVS identifier:
 *
 * $Id: ParameterList.java,v 1.18 2001/07/17 16:21:35 grosbois Exp $
 *
 * Class:                   ParameterList
 *
 * Description:             Class to hold parameters.
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
package dev.zontreck.harbinger.thirdparty.jj2000.j2k.util;

import java.util.Enumeration;
import java.util.Properties;

/**
 * This class holds modules options and parameters as they are provided to the
 * encoder or the decoder. Each option and its associated parameters are stored
 * as strings.
 *
 * <p>
 * This class is built on the standard Java Properties class. Consequently, it
 * offers facilities to load and write parameters from/to a file. In the
 * meantime, a ParameterList object can also handle default parameters for each
 * option.
 *
 * <p>
 * Each parameter can be retrieved as a string or as an specific primitive type
 * (int, float, etc).
 *
 * <p>
 * For more details see the Properties class.
 *
 * <p>
 * Note that this class does not support multiple occurrences of parameters (for
 * a parameter name, only one value is possible). Also there is no particular
 * order of the parameters.
 *
 * @see Properties
 */
public class ParameterList extends Properties {
	private static final long serialVersionUID = 1L;

	/**
	 * Constructs an empty ParameterList object. It can be later completed by
	 * adding elements one by one, by loading them from a file, or by
	 * initializing them from an argument string.
	 */
	public ParameterList ( ) {
	}

	/**
	 * Constructs an empty ParameterList object with the provided default
	 * parameters. The list can be later updated by adding elements one by one,
	 * by loading them from a file, or by initializing them from an argument
	 * string.
	 *
	 * @param def The defaults parameters
	 */
	public ParameterList ( final ParameterList def ) {
		super ( def );
	}

	/**
	 * Converts the usage information to a list of parameter names in a single
	 * array. The usage information appears in a 2D array of String. The first
	 * dimensions contains the different options, the second dimension contains
	 * the name of the option (first element), the synopsis and the explanation.
	 * This method takes the names of the different options in 'pinfo' and
	 * returns them in a single array of String.
	 *
	 * @param pinfo The list of options and their usage info (see above).
	 * @return An array with the names of the options in pinfo. If pinfo is
	 * null, null is returned.
	 */
	public static String[] toNameArray ( final String[][] pinfo ) {
		final String[] pnames;

		if ( null == pinfo ) {
			return null;
		}

		pnames = new String[ pinfo.length ];

		for ( int i = pinfo.length - 1 ; 0 <= i ; i-- ) {
			pnames[ i ] = pinfo[ i ][ 0 ];
		}
		return pnames;
	}

	/**
	 * Returns the default ParameterList.
	 *
	 * @return Default ParameterList
	 */
	public ParameterList getDefaultParameterList ( ) {
		return ( ParameterList ) this.defaults;
	}

	/**
	 * Parses the parameters from an argument list, such as as the one in the
	 * command line, and integrates them in this parameter list.
	 *
	 * <p>
	 * All options must be preceded by '-' and then followed by one or more
	 * words, which constitues the values. The name of the options constitute
	 * the name of the parameters. The only exception is for boolean options, in
	 * which case if they are preceded by '-' they will be turned on, and if
	 * preceded by '+' they will be turned off. The string value of a boolean
	 * option is "on" or "off". Note that the '-' and '+' characters can not
	 * precede any word which would be a value for an option unless they are
	 * numeric values (otherwise it would be considered as a boolean option).
	 * Note also that the name of an option can not start with a number.
	 *
	 * <p>
	 * No option can appear more than once. If so happens an exception is
	 * thrown.
	 *
	 * <p>
	 * For instance the string:
	 *
	 * <quote> "-Ffilters w5x3 -Wlev 5 -Qtype reversible </quote>
	 *
	 * <p>
	 * will create the following parameter list:
	 *
	 * <pre>
	 * Ffilers  w5x3
	 * Wlev     5
	 * Qtype    reversible
	 * </pre>
	 *
	 * @param argv The argument list.
	 * @throws StringFormatException if there are invalid arguments in 'argv'
	 */
	public void parseArgs ( final String[] argv ) {
		int k;
		char c, c2;
		String pname;
		final StringBuffer pvalue;

		// Read options
		k = - 1;
		// Skip empty arguments
		do {
			k++;
			if ( k >= argv.length ) {
				// Nothing to put in parameters
				return;
			}
		} while ( 0 >= argv[ k ].length ( ) );

		// Check that we start with an option and that its is not a number
		c = argv[ k ].charAt ( 0 );
		if ( '-' != c && '+' != c ) { // It's not an option
			throw new StringFormatException ( "Argument list does not" + " start with an option: " + argv[ k ] );
		}
		if ( 2 <= argv[ k ].length ( ) && Character.isDigit ( argv[ k ].charAt ( 1 ) ) ) {
			throw new StringFormatException ( "Numeric option name: " + argv[ k ] );
		}
		pvalue = new StringBuffer ( );
		while ( k < argv.length ) {
			// Read parameter name
			if ( 1 >= argv[ k ].length ( ) ) {
				throw new StringFormatException ( "Option \"" + argv[ k ] + "\" is too short." );
			}
			c = argv[ k ].charAt ( 0 );
			pname = argv[ k ];
			k++;
			pvalue.setLength ( 0 );
			// Are there any more arguments?
			if ( k >= argv.length ) {
				// No more words in argument list => must be boolean
				pvalue.append ( ( '-' == c ) ? "on" : "off" );
			}
			else {
				c2 = argv[ k ].charAt ( 0 );
				// Is next word an option or a value?
				if ( '-' == c2 || '+' == c2 ) { // Next word could be an option
					if ( 1 >= argv[ k ].length ( ) ) {
						throw new StringFormatException ( "Option or argument \"" + argv[ k ] + "\" too short" );
					}
					if ( ! Character.isDigit ( argv[ k ].charAt ( 1 ) ) ) {
						// Not a number => we have a boolean option in pname
						pvalue.append ( ( '-' == c ) ? "on" : "off" );
					}
				}
				if ( 0 == pvalue.length ( ) ) { // No value yet
					// It should not a boolean option, read the values
					if ( '+' == c ) {
						throw new StringFormatException ( "Boolean option \"" + pname + "\" has a value" );
					}
					// We have at least one value
					pvalue.append ( argv[ k ] );
					k++;
					while ( k < argv.length ) {
						// If empty string skip it
						if ( 0 == argv[ k ].length ( ) ) {
							k++;
							continue;
						}
						c = argv[ k ].charAt ( 0 );
						if ( '-' == c || '+' == c ) {
							// Next word could be an option
							if ( 1 >= argv[ k ].length ( ) ) {
								throw new StringFormatException ( "Option or " + "argument \"" + argv[ k ] + "\" too short" );
							}
							if ( ! Character.isDigit ( argv[ k ].charAt ( 1 ) ) ) {
								// It's an option => stop
								break;
							}
						}
						pvalue.append ( ' ' ); // Add a space
						pvalue.append ( argv[ k ] );
						k++;
					}
				}
			}
			// Now put parameter and value in the list
			if ( null != getProperty ( pname.substring ( 1 ) ) ) {
				// Option is repeated => ERROR
				throw new StringFormatException ( "Option \"" + pname + "\" appears more than once" );
			}
			this.put ( pname.substring ( 1 ) , pvalue.toString ( ) );
		}
	}

	/**
	 * Returns the value of the named parameter, as a string. The value can come
	 * from teh defaults, if there are.
	 *
	 * @param pname The parameter name.
	 * @return the value of the parameter as a string, or null if there is no
	 * parameter with the name 'pname'.
	 */
	public String getParameter ( final String pname ) {
		String pval;

		pval = this.getProperty ( pname );
		if ( null == pval && null != defaults ) { // if parameter is not there
			// Look in defaults
			pval = this.defaults.getProperty ( pname );
		}
		return pval;
	}

	/**
	 * Returns the value of the named parameter as a boolean. The value "on" is
	 * interpreted as 'true', while the value "off" is interpreted as 'false'.
	 * If the parameter has another value then an StringFormatException is
	 * thrown. If the parameter 'pname' is not in the parameter list, an
	 * IllegalArgumentException is thrown.
	 *
	 * @param pname The parameter name.
	 * @return the value of the parameter as a boolean.
	 * @throws StringFormatException    If the parameter has a value which is neither "on" nor
	 *                                  "off".
	 * @throws IllegalArgumentException If there is no parameter with the name 'pname' in the
	 *                                  parameter list.
	 */
	public boolean getBooleanParameter ( final String pname ) {
		final String s = this.getParameter ( pname );

		if ( null == s ) {
			throw new IllegalArgumentException ( "No parameter with name " + pname );
		}
		else if ( "on".equals ( s ) ) {
			return true;
		}
		else if ( "off".equals ( s ) ) {
			return false;
		}
		else {
			throw new StringFormatException ( "Parameter \"" + pname + "\" is not boolean: " + s );
		}
	}

	/**
	 * Returns the value of the named parameter as an int. If the parameter has
	 * a non-numeric value a NumberFormatException is thrown. If the parameter
	 * has a multiple word value than the first word is returned as an int,
	 * others are ignored. If the parameter 'pname' is not in the parameter
	 * list, an IllegalArgumentException is thrown.
	 *
	 * @param pname The parameter name.
	 * @return the value of the parameter as an int.
	 * @throws NumberFormatException    If the parameter has a non-numeric value.
	 * @throws IllegalArgumentException If there is no parameter with the name 'pname' in the
	 *                                  parameter list.
	 */
	public int getIntParameter ( final String pname ) {
		final String s = this.getParameter ( pname );

		if ( null == s ) {
			throw new IllegalArgumentException ( "No parameter with name " + pname );
		}
		try {
			return Integer.parseInt ( s );
		} catch ( final NumberFormatException e ) {
			throw new NumberFormatException ( "Parameter \"" + pname + "\" is not integer: " + e.getMessage ( ) );
		}
	}

	/**
	 * Returns the value of the named parameter as a float. If the parameter has
	 * a non-numeric value a NumberFormatException is thrown. If the parameter
	 * has a multiple word value than the first word is returned as an int,
	 * others are ignored. If the parameter 'pname' is not in the parameter
	 * list, an IllegalArgumentException is thrown.
	 *
	 * @param pname The parameter name.
	 * @return the value of the parameter as a float.
	 * @throws NumberFormatException    If the parameter has a non-numeric value.
	 * @throws IllegalArgumentException If there is no parameter with the name 'pname' in the
	 *                                  parameter list.
	 */
	public float getFloatParameter ( final String pname ) {
		final String s = this.getParameter ( pname );

		if ( null == s ) {
			throw new IllegalArgumentException ( "No parameter with name " + pname );
		}
		try {
			// Unfortunately there is no method to convert from a string
			// directly to a float
			return ( new Float ( s ) ).floatValue ( );
		} catch ( final NumberFormatException e ) {
			throw new NumberFormatException ( "Parameter \"" + pname + "\" is not floating-point: " + e.getMessage ( ) );
		}
	}

	/**
	 * Checks if the parameters which name starts with the prefix 'prfx' in the
	 * parameter list are all in the list of valid parameter names 'plist'. If
	 * there is a parameter that is not in 'plist' an IllegalArgumentException
	 * is thrown with an explanation message. The default parameters are also
	 * included in the check.
	 *
	 * @param prfx  The prefix of parameters to check.
	 * @param plist The list of valid parameter names for the 'prfx' prefix. If
	 *              null it is considered that no names are valid.
	 * @throws IllegalArgumentException If there's a parameter name starting with 'prfx' which is
	 *                                  not in the valid list of parameter names.
	 */
	@SuppressWarnings ("unchecked")
	public void checkList ( final char prfx , final String[] plist ) {
		final Enumeration<String> args;
		String val;
		int i;
		boolean isvalid;

		args = ( Enumeration<String> ) this.propertyNames ( );

		while ( args.hasMoreElements ( ) ) {
			val = args.nextElement ( );
			if ( 0 < val.length ( ) && val.charAt ( 0 ) == prfx ) {
				isvalid = false;
				if ( null != plist ) {
					for ( i = plist.length - 1; 0 <= i ; i-- ) {
						if ( val.equals ( plist[ i ] ) ) {
							isvalid = true;
							break;
						}
					}
				}
				if ( ! isvalid ) { // Did not find valid flag
					throw new IllegalArgumentException ( "Option '" + val + "' is " + "not a valid one." );
				}
			}
		}
	}

	/**
	 * Checks if the parameters which names do not start with any of the
	 * prefixes in 'prfxs' in this ParameterList are all in the list of valid
	 * parameter names 'plist'. If there is a parameter that is not in 'plist'
	 * an IllegalArgumentException is thrown with an explanation message. The
	 * default parameters are also included in the check.
	 *
	 * @param prfxs The prefixes of parameters to ignore.
	 * @param plist The list of valid parameter names. If null it is considered
	 *              that no names are valid.
	 * @throws IllegalArgumentException If there's a parameter name not starting with 'prfx' which
	 *                                  is not in the valid list of parameter names.
	 */
	@SuppressWarnings ("unchecked")
	public void checkList ( final char[] prfxs , final String[] plist ) {
		final Enumeration<String> args;
		String val;
		final String strprfxs;
		int i;
		boolean isvalid;

		args = ( Enumeration<String> ) this.propertyNames ( );
		strprfxs = new String ( prfxs );

		while ( args.hasMoreElements ( ) ) {
			val = args.nextElement ( );
			if ( 0 < val.length ( ) && - 1 == strprfxs.indexOf ( val.charAt ( 0 ) ) ) {
				isvalid = false;
				if ( null != plist ) {
					for ( i = plist.length - 1; 0 <= i ; i-- ) {
						if ( val.equals ( plist[ i ] ) ) {
							isvalid = true;
							break;
						}
					}
				}
				if ( ! isvalid ) {
					throw new IllegalArgumentException ( "Option '" + val + "' is " + "not a valid one." );
				}
			}
		}
	}
}
