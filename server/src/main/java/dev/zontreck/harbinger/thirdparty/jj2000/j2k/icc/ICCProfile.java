/*****************************************************************************
 *
 * $Id: ICCProfile.java,v 1.1 2002/07/25 14:56:55 grosbois Exp $
 *
 * Copyright Eastman Kodak Company, 343 State Street, Rochester, NY 14650
 * $Date $
 ****************************************************************************/

package dev.zontreck.harbinger.thirdparty.jj2000.j2k.icc;

import dev.zontreck.harbinger.thirdparty.jj2000.j2k.colorspace.ColorSpace;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.icc.tags.ICCCurveType;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.icc.tags.ICCTagTable;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.icc.tags.ICCXYZType;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.icc.types.ICCDateTime;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.icc.types.ICCProfileHeader;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.icc.types.ICCProfileVersion;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.icc.types.XYZNumber;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.util.FacilityManager;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.util.MsgLogger;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;

/**
 * This class models the ICCProfile file. This file is a binary file which is
 * divided into two parts, an ICCProfileHeader followed by an ICCTagTable. The
 * header is a straightforward list of descriptive parameters such as profile
 * size, version, date and various more esoteric parameters. The tag table is a
 * structured list of more complexly aggragated data describing things such as
 * ICC curves, copyright information, descriptive text blocks, etc.
 * <p>
 * Classes exist to model the header and tag table and their various constituent
 * parts the developer is refered to these for further information on the
 * structure and contents of the header and tag table.
 *
 * @author Bruce A. Kern
 * @version 1.0
 * @see ICCProfileHeader
 * @see ICCTagTable
 */

public abstract class ICCProfile {

	/**
	 * Gray index.
	 */
	public static final int GRAY = 0;

	// Renamed for convenience:
	/**
	 * RGB index.
	 */
	public static final int RED = 0;
	/**
	 * RGB index.
	 */
	public static final int GREEN = 1;
	/**
	 * RGB index.
	 */
	public static final int BLUE = 2;
	/**
	 * Size of native type
	 */
	public static final int boolean_size = 1;
	/**
	 * Size of native type
	 */
	public static final int byte_size = 1;
	/**
	 * Size of native type
	 */
	public static final int char_size = 2;
	/**
	 * Size of native type
	 */
	public static final int short_size = 2;
	/**
	 * Size of native type
	 */
	public static final int int_size = 4;
	/**
	 * Size of native type
	 */
	public static final int float_size = 4;
	/**
	 * Size of native type
	 */
	public static final int long_size = 8;
	/**
	 * Size of native type
	 */
	public static final int double_size = 8;
	/* Bit twiddling constant for integral types. */public static final int BITS_PER_BYTE = 8;
	/* Bit twiddling constant for integral types. */public static final int BITS_PER_SHORT = 16;
	/* Bit twiddling constant for integral types. */public static final int BITS_PER_INT = 32;
	/* Bit twiddling constant for integral types. */public static final int BITS_PER_LONG = 64;
	/* Bit twiddling constant for integral types. */public static final int BYTES_PER_SHORT = 2;
	/* Bit twiddling constant for integral types. */public static final int BYTES_PER_INT = 4;
	/* Bit twiddling constant for integral types. */public static final int BYTES_PER_LONG = 8;
	/**
	 * signature
	 */
	public static final int kdwProfileSignature = getInt ( "acsp".getBytes ( StandardCharsets.UTF_8 ) , 0 );
	/**
	 * signature
	 */
	public static final int kdwProfileSigReverse = getInt ( "psca".getBytes ( StandardCharsets.UTF_8 ) , 0 );
	/**
	 * profile type
	 */
	public static final int kdwInputProfile = getInt ( "scnr".getBytes ( StandardCharsets.UTF_8 ) , 0 );
	/**
	 * tag type
	 */
	public static final int kdwDisplayProfile = getInt ( "mntr".getBytes ( StandardCharsets.UTF_8 ) , 0 );
	/**
	 * tag type
	 */
	public static final int kdwRGBData = getInt ( "RGB ".getBytes ( StandardCharsets.UTF_8 ) , 0 );
	/**
	 * tag type
	 */
	public static final int kdwGrayData = getInt ( "GRAY".getBytes ( StandardCharsets.UTF_8 ) , 0 );
	/**
	 * tag type
	 */
	public static final int kdwXYZData = getInt ( "XYZ ".getBytes ( StandardCharsets.UTF_8 ) , 0 );
	/**
	 * input type
	 */
	public static final int kMonochromeInput = 0;
	/**
	 * input type
	 */
	public static final int kThreeCompInput = 1;
	/**
	 * tag signature
	 */
	public static final int kdwGrayTRCTag = getInt ( "kTRC".getBytes ( StandardCharsets.UTF_8 ) , 0 );
	/**
	 * tag signature
	 */
	public static final int kdwRedColorantTag = getInt ( "rXYZ".getBytes ( StandardCharsets.UTF_8 ) , 0 );
	/**
	 * tag signature
	 */
	public static final int kdwGreenColorantTag = getInt ( "gXYZ".getBytes ( StandardCharsets.UTF_8 ) , 0 );
	/**
	 * tag signature
	 */
	public static final int kdwBlueColorantTag = getInt ( "bXYZ".getBytes ( StandardCharsets.UTF_8 ) , 0 );
	/**
	 * tag signature
	 */
	public static final int kdwRedTRCTag = getInt ( "rTRC".getBytes ( StandardCharsets.UTF_8 ) , 0 );
	/**
	 * tag signature
	 */
	public static final int kdwGreenTRCTag = getInt ( "gTRC".getBytes ( StandardCharsets.UTF_8 ) , 0 );

	// Define the set of standard signature and type values
	// Because of the endian issues and byte swapping, the profile codes must
	// be stored in memory and be addressed by address. As such, only those
	// codes required for Restricted ICC use are defined here
	/**
	 * tag signature
	 */
	public static final int kdwBlueTRCTag = getInt ( "bTRC".getBytes ( StandardCharsets.UTF_8 ) , 0 );
	/**
	 * tag signature
	 */
	public static final int kdwCopyrightTag = getInt ( "cprt".getBytes ( StandardCharsets.UTF_8 ) , 0 );
	/**
	 * tag signature
	 */
	public static final int kdwMediaWhiteTag = getInt ( "wtpt".getBytes ( StandardCharsets.UTF_8 ) , 0 );
	/**
	 * tag signature
	 */
	public static final int kdwProfileDescTag = getInt ( "desc".getBytes ( StandardCharsets.UTF_8 ) , 0 );
	private static final String eol = System.getProperty ( "line.separator" );
	private final byte[] profile;
	private ICCProfileHeader header;
	private ICCTagTable tags;

	protected ICCProfile ( ) throws ICCProfileException {
		throw new ICCProfileException ( "illegal to invoke empty constructor" );
	}

	/**
	 * ParameterList constructor
	 *
	 * @param csm provides dev.zontreck.harbinger.thirdparty.jj2000.j2k.colorspace information
	 */
	protected ICCProfile ( final ColorSpace csm ) throws ICCProfileInvalidException {
		this.profile = csm.getICCProfile ( );
		this.initProfile ( this.profile );
	}

	/**
	 * Creates an int from a 4 character String
	 *
	 * @param fourChar string representation of an integer
	 * @return the integer which is denoted by the input String.
	 */
	public static int getIntFromString ( final String fourChar ) {
		final byte[] bytes = fourChar.getBytes ( StandardCharsets.UTF_8 );
		return ICCProfile.getInt ( bytes , 0 );
	}

	/**
	 * Create an XYZNumber from byte [] input
	 *
	 * @param data   array containing the XYZNumber representation
	 * @param offset start of the rep in the array
	 * @return the created XYZNumber
	 */
	public static XYZNumber getXYZNumber ( final byte[] data , final int offset ) {
		final int x;
		int y;
		final int z;
		x = ICCProfile.getInt ( data , offset );
		y = ICCProfile.getInt ( data , offset + ICCProfile.int_size );
		z = ICCProfile.getInt ( data , offset + 2 * ICCProfile.int_size );
		return new XYZNumber ( x , y , z );
	}

	/**
	 * Create an ICCProfileVersion from byte [] input
	 *
	 * @param data   array containing the ICCProfileVersion representation
	 * @param offset start of the rep in the array
	 * @return the created ICCProfileVersion
	 */
	public static ICCProfileVersion getICCProfileVersion ( final byte[] data , final int offset ) {
		final byte major = data[ offset ];
		final byte minor = data[ offset + ICCProfile.byte_size ];
		final byte resv1 = data[ offset + 2 * ICCProfile.byte_size ];
		final byte resv2 = data[ offset + 3 * ICCProfile.byte_size ];
		return new ICCProfileVersion ( major , minor , resv1 , resv2 );
	}

	/**
	 * Create an ICCDateTime from byte [] input
	 *
	 * @param data   array containing the ICCProfileVersion representation
	 * @param offset start of the rep in the array
	 * @return the created ICCProfileVersion
	 */
	public static ICCDateTime getICCDateTime ( final byte[] data , final int offset ) {
		final short wYear = ICCProfile.getShort ( data , offset ); // Number of the actual year (i.e.
		// 1994)
		final short wMonth = ICCProfile.getShort ( data , offset + short_size ); // Number
		// of
		// the
		// month
		// (1-12)
		final short wDay = ICCProfile.getShort ( data , offset + 2 * short_size ); // Number
		// of
		// the
		// day
		final short wHours = ICCProfile.getShort ( data , offset + 3 * short_size ); // Number
		// of
		// hours
		// (0-23)
		final short wMinutes = ICCProfile.getShort ( data , offset + 4 * short_size ); // Number
		// of
		// minutes
		// (0-59)
		final short wSeconds = ICCProfile.getShort ( data , offset + 5 * short_size ); // Number
		// of
		// seconds
		// (0-59)
		return new ICCDateTime ( wYear , wMonth , wDay , wHours , wMinutes , wSeconds );
	}

	/**
	 * Create a String from a byte []. Optionally swap adjacent byte pairs.
	 * Intended to be used to create integer String representations allowing for
	 * endian translations.
	 *
	 * @param bfr    data array
	 * @param offset start of data in array
	 * @param length length of data in array
	 * @param swap   swap adjacent bytes?
	 * @return String rep of data
	 */
	public static String getString ( final byte[] bfr , final int offset , final int length , final boolean swap ) {

		final byte[] result = new byte[ length ];
		final int incr = swap ? - 1 : 1;
		final int start = swap ? offset + length - 1 : offset;
		for ( int i = 0, j = start ; i < length ; ++ i ) {
			result[ i ] = bfr[ j ];
			j += incr;
		}
		return new String ( result , StandardCharsets.UTF_8 );
	}

	/**
	 * Create a short from a two byte [], with optional byte swapping.
	 *
	 * @param bfr  data array
	 * @param off  start of data in array
	 * @param swap swap bytes?
	 * @return native type from representation.
	 */
	public static short getShort ( final byte[] bfr , final int off , final boolean swap ) {

		final int tmp0 = bfr[ off ] & 0xff; // Clear the sign extended bits in the int.
		final int tmp1 = bfr[ off + 1 ] & 0xff;

		return ( short ) ( swap ? ( tmp1 << ICCProfile.BITS_PER_BYTE | tmp0 ) : ( tmp0 << ICCProfile.BITS_PER_BYTE | tmp1 ) );
	}

	/**
	 * Create a short from a two byte [].
	 *
	 * @param bfr data array
	 * @param off start of data in array
	 * @return native type from representation.
	 */
	public static short getShort ( final byte[] bfr , final int off ) {
		final int tmp0 = bfr[ off ] & 0xff; // Clear the sign extended bits in the int.
		final int tmp1 = bfr[ off + 1 ] & 0xff;
		return ( short ) ( tmp0 << ICCProfile.BITS_PER_BYTE | tmp1 );
	}

	/**
	 * Separate bytes in an int into a byte array lsb to msb order.
	 *
	 * @param d integer to separate
	 * @return byte [] containing separated int.
	 */
	public static byte[] setInt ( final int d ) {
		return ICCProfile.setInt ( d , new byte[ ICCProfile.BYTES_PER_INT ] );
	}

	/**
	 * Separate bytes in an int into a byte array lsb to msb order. Return the
	 * result in the provided array
	 *
	 * @param d integer to separate
	 * @param b return output here.
	 * @return reference to output.
	 */
	public static byte[] setInt ( int d , byte[] b ) {
		if ( null == b )
			b = new byte[ ICCProfile.BYTES_PER_INT ];
		for ( int i = 0 ; BYTES_PER_INT > i ; ++ i ) {
			b[ i ] = ( byte ) ( d & 0x0ff );
			d = d >> ICCProfile.BITS_PER_BYTE;
		}
		return b;
	}

	/**
	 * Separate bytes in a long into a byte array lsb to msb order.
	 *
	 * @param d long to separate
	 * @return byte [] containing separated int.
	 */
	public static byte[] setLong ( final long d ) {
		return ICCProfile.setLong ( d , new byte[ ICCProfile.BYTES_PER_INT ] );
	}

	/**
	 * Separate bytes in a long into a byte array lsb to msb order. Return the
	 * result in the provided array
	 *
	 * @param d long to separate
	 * @param b return output here.
	 * @return reference to output.
	 */
	public static byte[] setLong ( long d , byte[] b ) {
		if ( null == b )
			b = new byte[ ICCProfile.BYTES_PER_LONG ];
		for ( int i = 0 ; BYTES_PER_LONG > i ; ++ i ) {
			b[ i ] = ( byte ) ( d & 0x0ff );
			d = d >> ICCProfile.BITS_PER_BYTE;
		}
		return b;
	}

	/**
	 * Create an int from a byte [4], with optional byte swapping.
	 *
	 * @param bfr  data array
	 * @param off  start of data in array
	 * @param swap swap bytes?
	 * @return native type from representation.
	 */
	public static int getInt ( final byte[] bfr , final int off , final boolean swap ) {

		final int tmp0 = ICCProfile.getShort ( bfr , off , swap ) & 0xffff; // Clear the sign extended
		// bits in the int.
		final int tmp1 = ICCProfile.getShort ( bfr , off + 2 , swap ) & 0xffff;

		return ( swap ? ( tmp1 << ICCProfile.BITS_PER_SHORT | tmp0 ) : ( tmp0 << ICCProfile.BITS_PER_SHORT | tmp1 ) );
	}

	/**
	 * Create an int from a byte [4].
	 *
	 * @param bfr data array
	 * @param off start of data in array
	 * @return native type from representation.
	 */
	public static int getInt ( final byte[] bfr , final int off ) {

		final int tmp0 = ICCProfile.getShort ( bfr , off ) & 0xffff; // Clear the sign extended bits
		// in the int.
		final int tmp1 = ICCProfile.getShort ( bfr , off + 2 ) & 0xffff;

		return ( tmp0 << ICCProfile.BITS_PER_SHORT | tmp1 );
	}

	/**
	 * Create an long from a byte [8].
	 *
	 * @param bfr data array
	 * @param off start of data in array
	 * @return native type from representation.
	 */
	public static long getLong ( final byte[] bfr , final int off ) {

		final long tmp0 = ICCProfile.getInt ( bfr , off ) & 0xffffffff; // Clear the sign extended
		// bits in the int.
		final long tmp1 = ICCProfile.getInt ( bfr , off + 4 ) & 0xffffffff;

		return ( tmp0 << ICCProfile.BITS_PER_INT | tmp1 );
	}

	/**
	 * Create a two character hex representation of a byte
	 *
	 * @param i byte to represent
	 * @return representation
	 */
	public static String toHexString ( final byte i ) {
		String rep = ( 0 <= i && 16 > i ? "0" : "" ) + Integer.toHexString ( i );
		if ( 2 < rep.length ( ) )
			rep = rep.substring ( rep.length ( ) - 2 );
		return rep;
	}

	/**
	 * Create a 4 character hex representation of a short
	 *
	 * @param i short to represent
	 * @return representation
	 */
	public static String toHexString ( final short i ) {
		String rep;

		if ( 0 <= i && 0x10 > i )
			rep = "000" + Integer.toHexString ( i );
		else if ( 0 <= i && 0x100 > i )
			rep = "00" + Integer.toHexString ( i );
		else if ( 0 <= i && 0x1000 > i )
			rep = "0" + Integer.toHexString ( i );
		else
			rep = Integer.toHexString ( i );

		if ( 4 < rep.length ( ) )
			rep = rep.substring ( rep.length ( ) - 4 );
		return rep;
	}

	/**
	 * Create a 8 character hex representation of a int
	 *
	 * @param i int to represent
	 * @return representation
	 */
	public static String toHexString ( final int i ) {
		String rep;

		if ( 0 <= i && 0x10 > i )
			rep = "0000000" + Integer.toHexString ( i );
		else if ( 0 <= i && 0x100 > i )
			rep = "000000" + Integer.toHexString ( i );
		else if ( 0 <= i && 0x1000 > i )
			rep = "00000" + Integer.toHexString ( i );
		else if ( 0 <= i && 0x10000 > i )
			rep = "0000" + Integer.toHexString ( i );
		else if ( 0 <= i && 0x100000 > i )
			rep = "000" + Integer.toHexString ( i );
		else if ( 0 <= i && 0x1000000 > i )
			rep = "00" + Integer.toHexString ( i );
		else if ( 0 <= i && 0x10000000 > i )
			rep = "0" + Integer.toHexString ( i );
		else
			rep = Integer.toHexString ( i );

		if ( 8 < rep.length ( ) )
			rep = rep.substring ( rep.length ( ) - 8 );
		return rep;
	}

	public static String toString ( final byte[] data ) {

		int i;
		int row;
		int col;
		int rem;
		int rows;
		final int cols;

		final StringBuffer rep = new StringBuffer ( );
		StringBuffer rep0 = null;
		StringBuffer rep1 = null;
		StringBuffer rep2 = null;

		cols = 16;
		rows = data.length / cols;
		rem = data.length % cols;

		final byte[] lbytes = new byte[ 8 ];
		for ( row = 0 , i = 0; row < rows ; ++ row ) {
			rep1 = new StringBuffer ( );
			rep2 = new StringBuffer ( );

			for ( i = 0; 8 > i ; ++ i )
				lbytes[ i ] = 0;
			final byte[] tbytes = Integer.toHexString ( row * 16 ).getBytes ( StandardCharsets.UTF_8 );
			for ( int t = 0, l = lbytes.length - tbytes.length ; t < tbytes.length ; ++ l , ++ t )
				lbytes[ l ] = tbytes[ t ];

			rep0 = new StringBuffer ( new String ( lbytes , StandardCharsets.UTF_8 ) );

			for ( col = 0; col < cols ; ++ col ) {
				final byte b = data[ i ];
				i++;
				rep1.append ( ICCProfile.toHexString ( b ) ).append ( 0 == i % 2 ? " " : "" );
				if ( Character.isJavaIdentifierStart ( ( char ) b ) )
					rep2.append ( ( char ) b );
				else
					rep2.append ( "." );
			}
			rep.append ( rep0 ).append ( " :  " ).append ( rep1 ).append ( ":  " ).append ( rep2 ).append ( ICCProfile.eol );
		}

		rep1 = new StringBuffer ( );
		rep2 = new StringBuffer ( );

		for ( i = 0; 8 > i ; ++ i )
			lbytes[ i ] = 0;
		final byte[] tbytes = Integer.toHexString ( row * 16 ).getBytes ( StandardCharsets.UTF_8 );
		for ( int t = 0, l = lbytes.length - tbytes.length ; t < tbytes.length ; ++ l , ++ t )
			lbytes[ l ] = tbytes[ t ];

		rep0 = new StringBuffer ( new String ( lbytes , StandardCharsets.UTF_8 ) );

		for ( col = 0; col < rem ; ++ col ) {
			final byte b = data[ i ];
			i++;
			rep1.append ( ICCProfile.toHexString ( b ) ).append ( 0 == i % 2 ? " " : "" );
			if ( Character.isJavaIdentifierStart ( ( char ) b ) )
				rep2.append ( ( char ) b );
			else
				rep2.append ( "." );
		}
		for ( col = rem; 16 > col ; ++ col )
			rep1.append ( "  " ).append ( 0 == col % 2 ? " " : "" );

		rep.append ( rep0 ).append ( " :  " ).append ( rep1 ).append ( ":  " ).append ( rep2 ).append ( ICCProfile.eol );

		return rep.toString ( );
	}

	private int getProfileClass ( ) {
		return this.header.dwProfileClass;
	}

	private int getPCSType ( ) {
		return this.header.dwPCSType;
	}

	private int getProfileSignature ( ) {
		return this.header.dwProfileSignature;
	}

	/**
	 * Read the header and tags into memory and verify that the correct type of
	 * profile is being used. for encoding.
	 *
	 * @param data ICCProfile
	 * @throws ICCProfileInvalidException for bad signature and class and bad type
	 */
	private void initProfile ( final byte[] data ) throws ICCProfileInvalidException {
		this.header = new ICCProfileHeader ( data );
		this.tags = ICCTagTable.createInstance ( data );

		// Verify that the data pointed to by dev.zontreck.harbinger.thirdparty.jj2000.j2k.icc is indeed a valid profile
		// and that it is possibly of one of the Restricted ICC types. The
		// simplest way to check
		// this is to verify that the profile signature is correct, that it is
		// an input profile,
		// and that the PCS used is XYX.

		// However, a common error in profiles will be to create Monitor
		// profiles rather
		// than input profiles. If this is the only error found, it's still
		// useful to let this
		// go through with an error written to stderr.

		if ( this.getProfileClass ( ) == ICCProfile.kdwDisplayProfile ) {
			final String message = "NOTE!! Technically, this profile is a Display profile, not an"
					+ " Input Profile, and thus is not a valid Restricted ICC profile."
					+ " However, it is quite possible that this profile is usable as"
					+ " a Restricted ICC profile, so this code will ignore this state"
					+ " and proceed with processing.";

			FacilityManager.getMsgLogger ( ).printmsg ( MsgLogger.WARNING , message );
		}

		if ( ( this.getProfileSignature ( ) != ICCProfile.kdwProfileSignature )
				|| ( ( this.getProfileClass ( ) != ICCProfile.kdwInputProfile ) && ( this.getProfileClass ( ) != ICCProfile.kdwDisplayProfile ) )
				|| ( this.getPCSType ( ) != ICCProfile.kdwXYZData ) ) {
			throw new ICCProfileInvalidException ( );
		}
	}

	/**
	 * Provide a suitable string representation for the class
	 */
	@Override
	public String toString ( ) {
		final StringBuffer body = new StringBuffer ( );
		body.append ( ICCProfile.eol ).append ( this.header );
		body.append ( ICCProfile.eol ).append ( ICCProfile.eol ).append ( this.tags );
		final String rep = "[ICCProfile:" + ColorSpace.indent ( "  " , body ) +
				"]";
		return rep;
	}

	/**
	 * Access the profile header
	 *
	 * @return ICCProfileHeader
	 */
	public ICCProfileHeader getHeader ( ) {
		return this.header;
	}

	/**
	 * Access the profile tag table
	 *
	 * @return ICCTagTable
	 */
	public ICCTagTable getTagTable ( ) {
		return this.tags;
	}

	/**
	 * Parse this ICCProfile into a RestrictedICCProfile which is appropriate to
	 * the data in this profile. Either a MonochromeInputRestrictedProfile or
	 * MatrixBasedRestrictedProfile is returned
	 *
	 * @return RestrictedICCProfile
	 * @throws ICCProfileInvalidException no curve data
	 */
	public RestrictedICCProfile parse ( ) throws ICCProfileInvalidException {

		// The next step is to determine which Restricted ICC type is used by
		// this profile.
		// Unfortunately, the only way to do this is to look through the tag
		// table for
		// the tags required by the two types.

		// First look for the gray TRC tag. If the profile is indeed an input
		// profile, and this
		// tag exists, then the profile is a Monochrome Input profile

		final ICCCurveType grayTag = ( ICCCurveType ) this.tags.get ( Integer.valueOf ( ICCProfile.kdwGrayTRCTag ) );
		if ( null != grayTag ) {
			return RestrictedICCProfile.createInstance ( grayTag );
		}

		// If it wasn't a Monochrome Input profile, look for the Red Colorant
		// tag. If that
		// tag is found and the profile is indeed an input profile, then this
		// profile is
		// a Three-Component Matrix-Based Input profile

		final ICCCurveType rTRCTag = ( ICCCurveType ) this.tags.get ( Integer.valueOf ( ICCProfile.kdwRedTRCTag ) );

		if ( null != rTRCTag ) {
			final ICCCurveType gTRCTag = ( ICCCurveType ) this.tags.get ( Integer.valueOf ( ICCProfile.kdwGreenTRCTag ) );
			final ICCCurveType bTRCTag = ( ICCCurveType ) this.tags.get ( Integer.valueOf ( ICCProfile.kdwBlueTRCTag ) );
			final ICCXYZType rColorantTag = ( ICCXYZType ) this.tags.get ( Integer.valueOf ( ICCProfile.kdwRedColorantTag ) );
			final ICCXYZType gColorantTag = ( ICCXYZType ) this.tags.get ( Integer.valueOf ( ICCProfile.kdwGreenColorantTag ) );
			final ICCXYZType bColorantTag = ( ICCXYZType ) this.tags.get ( Integer.valueOf ( ICCProfile.kdwBlueColorantTag ) );
			return RestrictedICCProfile.createInstance ( rTRCTag , gTRCTag , bTRCTag , rColorantTag , gColorantTag ,
					bColorantTag
			);
		}

		throw new ICCProfileInvalidException ( "curve data not found in profile" );
	}

	/**
	 * Output this ICCProfile to a RandomAccessFile
	 *
	 * @param os output file
	 */
	public void write ( final RandomAccessFile os ) throws IOException {
		header.write ( os );
		tags.write ( os );
	}

	/* end class ICCProfile */
}
