package dev.zontreck.harbinger.utils;

public class MathF {

	public static float Sqrt(float f) {
		return (float) Math.sqrt(f);
	}

	public static int Sqrt(int f) {
		return (int) Math.sqrt(f);
	}

	public static float Cos(float s) {
		return (float) Math.cos(s);
	}

	public static float Sin(float s) {
		return (float) Math.sin(s);
	}

	public static float Abs(float A) {
		return Math.abs(A);
	}

	public static float Clamp(float A, float B, float C) {
		return Math.max(B, Math.min(C, A));
	}

	public static int Clamp(int A, int B, int C) {
		return Math.max(B, Math.min(C, A));
	}

	public static float Min(float f, float f2) {
		return Math.min(f, f2);
	}

	public static int Min(int f, int f2) {
		return Math.min(f, f2);
	}

	public static float Max(float f, float f2) {
		return Math.max(f, f2);
	}

	public static int Max(int f, int f2) {
		return Math.max(f, f2);
	}

	public static float Atan2(float a, float b) {
		return (float) Math.atan2(a, b);
	}

	public static float Asin(float a) {
		return (float) Math.asin(a);
	}

	public static float Pow(float a, float b) {
		return (float) Math.pow(a, b);
	}
}
