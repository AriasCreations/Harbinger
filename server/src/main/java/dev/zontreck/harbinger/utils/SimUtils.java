package dev.zontreck.harbinger.utils;

import dev.zontreck.harbinger.simulator.types.osUTF8ByteCount;

import java.io.*;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Arrays;
import java.util.Random;

public class SimUtils {


	public static final boolean LITTLE_ENDIAN = ! SimUtils.IsBigEndian ( );
	public static final boolean BIG_ENDIAN = SimUtils.IsBigEndian ( );
	public static final Instant Epoch = Instant.EPOCH;
	private static final byte ASCIIzero = ( byte ) '0';
	private static final byte ASCIIminus = ( byte ) '-';

	public static boolean IsBigEndian ( ) {
		return ( ByteOrder.nativeOrder ( ) ) != ByteOrder.LITTLE_ENDIAN;
	}

	public static boolean ApproxEqual ( float a , float b , final float tolerance ) {
		final float diff = Math.abs ( a - b );
		if ( diff <= tolerance )
			return true;

		a = Math.abs ( a );
		b = Math.abs ( b );
		if ( b > a )
			a = b;

		return diff <= a;
	}

	public static boolean ApproxZero ( final float a , final float tolerance ) {
		return Math.abs ( a ) <= tolerance;
	}

	public static boolean ApproxZero ( final float a ) {
		return 1.0e-6 >= Math.abs ( a );
	}

	public static boolean ApproxEqual ( final float a , final float b ) {
		return SimUtils.ApproxEqual ( a , b , 1.0e-6f );
	}

	public static int CombineHash ( final int a , final int b ) {
		return 65599 * a + b;
	}

	public static short BytesToInt16 ( final byte[] bytes ) {
		//if (bytes.Length < 2 ) return 0;
		return ( short ) ( bytes[ 0 ] | ( bytes[ 1 ] << 8 ) );
	}

	public static String IntToHexString ( final int i ) {
		return String.format ( "%08x" , i );
	}

	public static String BytesToString ( final byte[] arr ) {
		return SimUtils.BytesToString ( arr , 0 , arr.length );
	}

	public static float BytesToFloatSafepos ( final byte[] arr , final int pos ) {
		float fTotal = 0.0f;
		for ( int i = pos ; i < arr.length ; i++ ) {
			fTotal += arr[ i ];
		}

		return fTotal;
	}

	public static String BytesToString ( final byte[] arr , final int index , final int count ) {
		int ptr = 0;
		final byte[] splice = new byte[ count ];
		for ( int i = index ; i < count ; i++ ) {
			splice[ i ] = arr[ ptr ];

			ptr++;
		}
		return new String ( splice , StandardCharsets.UTF_8 );
	}

	public static String BytesToHexString ( final byte[] arr , final String field ) {
		return SimUtils.BytesToHexString ( arr , arr.length , field );
	}

	public static String BytesToHexString ( final byte[] arr , final int len , final String field ) {
		final StringBuilder builder = new StringBuilder ( );

		for ( int i = 0 ; i < len ; i += 16 ) {
			if ( 0 != i ) builder.append ( "\n" );

			if ( null == field || field.isEmpty ( ) ) {
				builder.append ( field );
				builder.append ( ": " );
			}

			for ( int j = 0, k = i ; 16 > j ; ++ j , ++ k ) {
				if ( k >= len ) break;

				if ( 0 != j ) builder.append ( " " );

				builder.append ( String.format ( "%02X" , arr[ k ] ) );
			}
		}

		return builder.toString ( );
	}

	public static byte[] StringToBytesNoTerm ( final String str ) {
		return SimUtils.StringToBytesNoTerm ( str , str.length ( ) );
	}

	public static byte[] StringToBytesNoTerm ( final String str , final int max ) {
		if ( null == str || str.isEmpty ( ) )
			return new byte[ 0 ];

		final osUTF8ByteCount Bytes = SimUtils.osUTF8GetBytesCount ( str );
		if ( 0 == Bytes.length ) return new byte[ 0 ];

		final byte[] dest = SimUtils.osUTF8GetBytes ( str , Bytes.srcLen );
		return dest;
	}

	public static osUTF8ByteCount osUTF8GetBytesCount ( final String str ) {
		return SimUtils.osUTF8GetBytesCount ( str , str.length ( ) );
	}

	public static osUTF8ByteCount osUTF8GetBytesCount ( final String str , final int max ) {
		final osUTF8ByteCount count = new osUTF8ByteCount ( );
		count.length = SimUtils.osUTF8GetBytes ( str , str.length ( ) ).length;
		count.srcLen = max;
		return count;
	}

	public static byte[] osUTF8GetBytes ( final String src , final int strLen ) {
		byte[] arr = new byte[ 0 ];
		arr = src.substring ( 0 , strLen ).getBytes ( StandardCharsets.UTF_8 );

		return arr;
	}

	public static byte[] UIntToStrBytes_reversed ( int v ) {
		int n = 0;
		final byte[] arr = new byte[ 32 ];
		do {
			byte a = SimUtils.ASCIIzero;
			a += ( byte ) ( v % 10 );
			arr[ n ] = a;
			n++;
			v /= 10;
		} while ( 0 < v );

		return Arrays.copyOf ( arr , n );
	}

	public static byte[] IntToByteString ( final int v ) {
		final byte[] tmp = SimUtils.UIntToStrBytes_reversed ( ( ( 0 < v ) ? v : - v ) );
		final byte[] dst = new byte[ 16 ];

		if ( 0 < v ) {
			for ( int i = 0, j = tmp.length - 1 ; i < tmp.length ; i++ , j-- ) {
				dst[ i ] = tmp[ j ];
			}
		}
		else {
			for ( int i = 1, j = tmp.length - 1 ; i < tmp.length + 1 ; i++ , j-- ) {
				dst[ i ] = tmp[ j ];
			}
		}

		return dst;
	}

	public static boolean IsFinite ( final float value ) {
		return Float.isFinite ( value );
	}

	public static boolean IsFinite ( final double value ) {
		return Double.isFinite ( value );
	}

	public static float Distance ( final float value1 , final float value2 ) {
		return MathF.Abs ( value1 - value2 );
	}


	public static float Hermite ( final float value1 , final float tangent1 , final float value2 , final float tangent2 , final float amount ) {
		if ( 0.0f >= amount )
			return value1;
		if ( 1.0f <= amount )
			return value2;

		// All transformed to double not to lose precission
		// Otherwise, for high numbers of param:amount the result is NaN instead of Infinity
		final double v1 = value1;
		final double v2 = value2;
		final double t1 = tangent1;
		final double t2 = tangent2;
		final double s = amount;
		final var sSquared = s * s;
		final var sCubed = sSquared * s;

		return ( float ) (
				( 2.0d * v1 - 2.0d * v2 + t2 + t1 ) * sCubed +
						( 3.0d * v2 - 3.0d * v1 - 2.0d * t1 - t2 ) * sSquared +
						t1 * s + v1
		);
	}

	public static int Hermite ( final int value1 , final int tangent1 , final int value2 , final int tangent2 , final int amount ) {
		if ( 0 >= amount )
			return value1;
		if ( 1 <= amount )
			return value2;

		// All transformed to double not to lose precission
		// Otherwise, for high numbers of param:amount the result is NaN instead of Infinity
		final double v1 = value1;
		final double v2 = value2;
		final double t1 = tangent1;
		final double t2 = tangent2;
		final double s = amount;
		final var sSquared = s * s;
		final var sCubed = sSquared * s;

		return ( int ) (
				( 2 * v1 - 2 * v2 + t2 + t1 ) * sCubed +
						( 3 * v2 - 3 * v1 - 2 * t1 - t2 ) * sSquared +
						t1 * s + v1
		);
	}

	public static double Hermite ( final double value1 , final double tangent1 , final double value2 , final double tangent2 , final double amount ) {
		if ( 0.0d >= amount )
			return value1;
		if ( 1.0f <= amount )
			return value2;

		// All transformed to double not to lose precission
		// Otherwise, for high numbers of param:amount the result is NaN instead of Infinity
		final double v1 = value1;
		final double v2 = value2;
		final double t1 = tangent1;
		final double t2 = tangent2;
		final double s = amount;
		final var sSquared = s * s;
		final var sCubed = sSquared * s;

		return ( 2.0d * v1 - 2.0d * v2 + t2 + t1 ) * sCubed +
				( 3.0d * v2 - 3.0d * v1 - 2.0d * t1 - t2 ) * sSquared +
				t1 * s + v1;
	}

	public static float Lerp ( final float value1 , final float value2 , final float amount ) {
		return value1 + ( value2 - value1 ) * amount;
	}

	public static int Lerp ( final int value1 , final int value2 , final int amount ) {
		return value1 + ( value2 - value1 ) * amount;
	}

	public static double Lerp ( final double value1 , final double value2 , final double amount ) {
		return value1 + ( value2 - value1 ) * amount;
	}

	public static float SmoothStep ( final float value1 , final float value2 , final float amount ) {
		return SimUtils.Hermite ( value1 , 0.0f , value2 , 0.0f , amount );
	}

	public static int SmoothStep ( final int value1 , final int value2 , final int amount ) {
		return SimUtils.Hermite ( value1 , 0 , value2 , 0 , amount );
	}

	public static double SmoothStep ( final double value1 , final double value2 , final double amount ) {
		return SimUtils.Hermite ( value1 , 0.0f , value2 , 0.0f , amount );
	}

	public static float ToDegrees ( final float radians ) {
		// This method uses double precission internally,
		// though it returns single float
		// Factor = 180 / pi
		return ( float ) ( radians * 57.295779513082320876798154814105 );
	}

	public static float ToRadians ( final float degrees ) {
		// This method uses double precission internally,
		// though it returns single float
		// Factor = pi / 180
		return ( float ) ( degrees * 0.017453292519943295769236907684886 );
	}

	public static byte[] MakeHash ( final String algo , final byte[] data ) throws NoSuchAlgorithmException {
		final MessageDigest md = MessageDigest.getInstance ( algo );
		md.update ( data );
		return md.digest ( );
	}

	public static byte[] MD5 ( final byte[] data ) throws NoSuchAlgorithmException {
		return SimUtils.MakeHash ( "MD5" , data );

	}

	public static byte[] SHA1 ( final byte[] data ) throws NoSuchAlgorithmException {
		return SimUtils.MakeHash ( "SHA1" , data );
	}

	public static String HexString ( final byte[] arr ) {

		String finalStr = "";
		for ( final byte b :
				arr ) {
			finalStr += String.format ( "%02x" , b );
		}

		return finalStr;
	}

	public static String SHA1String ( final String value ) {
		try {
			final byte[] arr = SimUtils.SHA1 ( value.getBytes ( StandardCharsets.UTF_8 ) );
			return SimUtils.HexString ( arr );
		} catch ( final NoSuchAlgorithmException e ) {
			throw new RuntimeException ( e );
		}
	}

	public static byte[] SHA256 ( final byte[] data ) throws NoSuchAlgorithmException {
		return SimUtils.MakeHash ( "SHA256" , data );
	}

	public static String SHA256String ( final String value ) {
		try {
			final byte[] arr = SimUtils.SHA256 ( value.getBytes ( StandardCharsets.UTF_8 ) );
			return SimUtils.HexString ( arr );
		} catch ( final Exception e ) {
			throw new RuntimeException ( );
		}
	}

	/// $1$???
	public static String MD5 ( final String password ) {
		try {
			final byte[] arr = SimUtils.MD5 ( password.getBytes ( StandardCharsets.UTF_8 ) );
			return "$1$" + SimUtils.HexString ( arr );
		} catch ( final Exception e ) {
			throw new RuntimeException ( );
		}
	}

	public static String MD5String ( final String value ) {
		try {
			final byte[] arr = SimUtils.MD5 ( value.getBytes ( StandardCharsets.UTF_8 ) );
			return SimUtils.HexString ( arr );
		} catch ( final Exception e ) {
			throw new RuntimeException ( );
		}
	}

	public static double RandomDouble ( ) {
		final Random rng = new Random ( );
		return rng.nextDouble ( );
	}

	public static byte[] IntToBytesBig ( final int value ) {
		final var bytes = new byte[ 4 ];

		bytes[ 0 ] = ( byte ) ( value >> 24 );
		bytes[ 1 ] = ( byte ) ( value >> 16 );
		bytes[ 2 ] = ( byte ) ( value >> 8 );
		bytes[ 3 ] = ( byte ) value;

		return bytes;
	}

	public static byte FloatZeroOneToByte ( final float val ) {
		if ( 0 >= val )
			return 0;
		if ( 1.0f <= val )
			return ( byte ) 255;

		return ( byte ) ( 255 * val );
	}

	public static byte[] FloatToBytesSafepos ( final float value ) {
		final ByteArrayOutputStream baos = new ByteArrayOutputStream ( );
		final DataOutputStream dos = new DataOutputStream ( baos );

		try {
			dos.writeFloat ( value );
			return baos.toByteArray ( );
		} catch ( final IOException e ) {
			throw new RuntimeException ( e );
		}
	}

	public static int BytesToIntBig ( final byte[] bytes ) {
		return ( bytes[ 0 ] << 24 ) |
				( bytes[ 1 ] << 16 ) |
				( bytes[ 2 ] << 8 ) |
				bytes[ 3 ];
	}

	public static double BytesToDouble ( final byte[] bytes ) {
		final ByteArrayInputStream bis = new ByteArrayInputStream ( bytes );
		final var dis = new DataInputStream ( bis );

		try {
			return dis.readDouble ( );
		} catch ( final IOException e ) {
			throw new RuntimeException ( e );
		}
	}

	public static double BytesToDoubleBig ( final byte[] bytes ) {
		return SimUtils.BytesToDouble ( bytes );
	}

	public static long BytesToLong ( final byte[] arr ) {

		final ByteArrayInputStream bis = new ByteArrayInputStream ( arr );
		final var dis = new DataInputStream ( bis );

		try {
			return dis.readLong ( );
		} catch ( final IOException e ) {
			throw new RuntimeException ( e );
		}
	}

}
