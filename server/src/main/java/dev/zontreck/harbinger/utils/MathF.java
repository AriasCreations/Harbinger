package dev.zontreck.harbinger.utils;

public class MathF {


	public static float Sqrt ( final float f ) {
		return ( float ) Math.sqrt ( f );
	}

	public static int Sqrt ( final int f ) {
		return ( int ) Math.sqrt ( f );
	}

	public static float Cos ( final float s ) {
		return ( float ) Math.cos ( s );
	}

	public static float Sin ( final float s ) {
		return ( float ) Math.sin ( s );
	}

	public static float Abs ( final float A ) {
		return Math.abs ( A );
	}

	public static float Clamp ( final float A , final float B , final float C ) {
		return Math.max ( B , Math.min ( C , A ) );
	}

	public static double Clamp ( final double A , final double B , final double C ) {
		return Math.max ( B , Math.min ( C , A ) );
	}

	public static int Clamp ( final int A , final int B , final int C ) {
		return Math.max ( B , Math.min ( C , A ) );
	}

	public static float Min ( final float f , final float f2 ) {
		return Math.min ( f , f2 );
	}

	public static int Min ( final int f , final int f2 ) {
		return Math.min ( f , f2 );
	}

	public static float Max ( final float f , final float f2 ) {
		return Math.max ( f , f2 );
	}

	public static int Max ( final int f , final int f2 ) {
		return Math.max ( f , f2 );
	}

	public static float Atan2 ( final float a , final float b ) {
		return ( float ) Math.atan2 ( a , b );
	}

	public static float Asin ( final float a ) {
		return ( float ) Math.asin ( a );
	}

	public static float Pow ( final float a , final float b ) {
		return ( float ) Math.pow ( a , b );
	}

	public static float Floor ( final float a ) {
		return ( float ) Math.floor ( a );
	}

	public static double Floor ( final double a ) {
		return Math.floor ( a );
	}
}
