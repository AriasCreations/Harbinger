package dev.zontreck.harbinger.simulator.types;

import dev.zontreck.harbinger.utils.MathF;
import dev.zontreck.harbinger.utils.Matrix4;
import dev.zontreck.harbinger.utils.SimUtils;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class Vector4 implements Comparable<Vector4> {
	/// <summary>X value</summary>
	public float X;

	/// <summary>Y value</summary>
	public float Y;

	/// <summary>Z value</summary>
	public float Z;

	/// <summary>W value</summary>
	public float W;

	public Vector4(float x, float y, float z, float w) {
		X = x;
		Y = y;
		Z = z;
		W = w;
	}

	public Vector4(Vector2 value, float z, float w) {
		X = value.X;
		Y = value.Y;
		Z = z;
		W = w;
	}

	public Vector4(Vector3 value, float w) {
		X = value.X;
		Y = value.Y;
		Z = value.Z;
		W = w;
	}

	public Vector4(float value) {
		X = value;
		Y = value;
		Z = value;
		W = value;
	}

	public Vector4() {
		initV4();
	}

	private void initV4() {

		X = 0;
		Y = 0;
		Z = 0;
		W = 0;
	}

	/// <summary>
///     Constructor, builds a vector from a byte array
/// </summary>
/// <param name="byteArray">Byte array containing four four-byte floats</param>
/// <param name="pos">Beginning position in the byte array</param>
	public Vector4(byte[] byteArray, int pos) {
		X = SimUtils.BytesToFloatSafepos(byteArray, pos);
		Y = SimUtils.BytesToFloatSafepos(byteArray, pos + 4);
		Z = SimUtils.BytesToFloatSafepos(byteArray, pos + 8);
		W = SimUtils.BytesToFloatSafepos(byteArray, pos + 12);
	}

	public Vector4(Vector4 value) {
		X = value.X;
		Y = value.Y;
		Z = value.Z;
		W = value.W;
	}

	public void Abs() {
		X = MathF.Abs(X);
		Y = MathF.Abs(Y);
		Z = MathF.Abs(Z);
		W = MathF.Abs(W);
	}

	public void Add(Vector4 v) {
		X += v.X;
		Y += v.Y;
		Z += v.Z;
		W += v.W;
	}

	public void Sub(Vector4 v) {
		X -= v.X;
		Y -= v.Y;
		Z -= v.Z;
		W -= v.W;
	}

	public void Clamp(float min, float max) {
		if (X < min) X = min;
		else if (X > max) X = max;

		if (Y < min) Y = min;
		else if (Y > max) Y = max;

		if (Z < min) Z = min;
		else if (Z > max) Z = max;

		if (W < min) W = min;
		else if (W > max) W = max;
	}

	public void Min(Vector4 v) {
		if (v.X < X) X = v.X;
		if (v.Y < Y) Y = v.Y;
		if (v.Z < Z) Z = v.Z;
		if (v.W < W) W = v.W;
	}

	public void Max(Vector4 v) {
		if (v.X > X) X = v.X;
		if (v.Y > Y) Y = v.Y;
		if (v.Z > Z) Z = v.Z;
		if (v.W > W) W = v.W;
	}

	public float Length() {
		return MathF.Sqrt(X * X + Y * Y + Z * Z + W * W);
	}

	public float LengthSquared() {
		return X * X + Y * Y + Z * Z + W * W;
	}

	public void Normalize() {
		var factor = LengthSquared();
		if (factor > 1e-6) {
			factor = 1f / MathF.Sqrt(factor);
			X *= factor;
			Y *= factor;
			Z *= factor;
			W *= factor;
		} else {
			initV4();
		}
	}

	public boolean ApproxEquals(Vector4 vec, float tolerance) {
		return SimUtils.ApproxEqual(X, vec.X, tolerance) &&
				SimUtils.ApproxEqual(Y, vec.Y, tolerance) &&
				SimUtils.ApproxEqual(Z, vec.Z, tolerance) &&
				SimUtils.ApproxEqual(W, vec.W, tolerance);
	}

	public boolean ApproxEquals(Vector4 vec) {
		return SimUtils.ApproxEqual(X, vec.X) &&
				SimUtils.ApproxEqual(Y, vec.Y) &&
				SimUtils.ApproxEqual(Z, vec.Z) &&
				SimUtils.ApproxEqual(W, vec.W);
	}

	public boolean IsZero() {
		if (X != 0)
			return false;
		if (Y != 0)
			return false;
		if (Z != 0)
			return false;
		if (W != 0)
			return false;
		return true;
	}

	public boolean IsNotZero() {
		if (X != 0)
			return true;
		if (Y != 0)
			return true;
		if (Z != 0)
			return true;
		if (W != 0)
			return true;
		return false;
	}

	public float Dot(Vector4 value2) {
		return X * value2.X + Y * value2.Y + Z * value2.Z + W * value2.W;
	}

	/// <summary>
///     Test if this vector is composed of all finite numbers
/// </summary>
	public boolean IsFinite() {
		return SimUtils.IsFinite(X) && SimUtils.IsFinite(Y) && SimUtils.IsFinite(Z) && SimUtils.IsFinite(W);
	}

	/// <summary>
///     Builds a vector from a byte array
/// </summary>
/// <param name="byteArray">Byte array containing a 16 byte vector</param>
/// <param name="pos">Beginning position in the byte array</param>
	public void FromBytes(byte[] byteArray, int pos) {
		X = SimUtils.BytesToFloatSafepos(byteArray, pos);
		Y = SimUtils.BytesToFloatSafepos(byteArray, pos + 4);
		Z = SimUtils.BytesToFloatSafepos(byteArray, pos + 8);
		W = SimUtils.BytesToFloatSafepos(byteArray, pos + 12);
	}

	/// <summary>
///     Returns the raw bytes for this vector
/// </summary>
/// <returns>A 16 byte array containing X, Y, Z, and W</returns>
	public byte[] GetBytes() {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			baos.write(SimUtils.FloatToBytesSafepos(X));
			baos.write(SimUtils.FloatToBytesSafepos(Y));
			baos.write(SimUtils.FloatToBytesSafepos(Z));
			baos.write(SimUtils.FloatToBytesSafepos(W));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return baos.toByteArray();
	}


	public static Vector4 Add(Vector4 value1, Vector4 value2) {
		return new Vector4(
				value1.W + value2.W,
				value1.X + value2.X,
				value1.Y + value2.Y,
				value1.Z + value2.Z
		);
	}

	public static Vector4 Clamp(Vector4 value1, float min, float max) {
		return new Vector4(
				MathF.Clamp(value1.X, min, max),
				MathF.Clamp(value1.Y, min, max),
				MathF.Clamp(value1.Z, min, max),
				MathF.Clamp(value1.W, min, max));
	}

	public static Vector4 Clamp(Vector4 value1, Vector4 min, Vector4 max) {
		return new Vector4(
				MathF.Clamp(value1.X, min.X, max.X),
				MathF.Clamp(value1.Y, min.Y, max.Y),
				MathF.Clamp(value1.Z, min.Z, max.Z),
				MathF.Clamp(value1.W, min.W, max.W));
	}

	public static float Distance(Vector4 value1, Vector4 value2) {
		return MathF.Sqrt(DistanceSquared(value1, value2));
	}

	public static float DistanceSquared(Vector4 value1, Vector4 value2) {
		return
				(value1.X - value2.X) * (value1.X - value2.X) +
						(value1.Y - value2.Y) * (value1.Y - value2.Y) +
						(value1.Z - value2.Z) * (value1.Z - value2.Z) +
						(value1.W - value2.W) * (value1.W - value2.W);
	}

	public static Vector4 Divide(Vector4 value1, Vector4 value2) {
		return new Vector4(
				value1.X / value2.X,
				value1.Y / value2.Y,
				value1.Z / value2.Z,
				value1.W / value2.W
		);
	}

	public static Vector4 Divide(Vector4 value1, float divider) {
		var factor = 1f / divider;
		return new Vector4(
				value1.X * factor,
				value1.Y * factor,
				value1.Z * factor,
				value1.W * factor
		);
	}

	public static float Dot(Vector4 vector1, Vector4 vector2) {
		return vector1.X * vector2.X + vector1.Y * vector2.Y + vector1.Z * vector2.Z + vector1.W * vector2.W;
	}

	public static Vector4 Lerp(Vector4 value1, Vector4 value2, float amount) {
		return new Vector4(
				SimUtils.Lerp(value1.X, value2.X, amount),
				SimUtils.Lerp(value1.Y, value2.Y, amount),
				SimUtils.Lerp(value1.Z, value2.Z, amount),
				SimUtils.Lerp(value1.W, value2.W, amount));
	}

	public static Vector4 Max(Vector4 value1, Vector4 value2) {
		return new Vector4(
				MathF.Max(value1.X, value2.X),
				MathF.Max(value1.Y, value2.Y),
				MathF.Max(value1.Z, value2.Z),
				MathF.Max(value1.W, value2.W));
	}

	public static Vector4 Min(Vector4 value1, Vector4 value2) {
		return new Vector4(
				MathF.Min(value1.X, value2.X),
				MathF.Min(value1.Y, value2.Y),
				MathF.Min(value1.Z, value2.Z),
				MathF.Min(value1.W, value2.W));
	}

	public static Vector4 Multiply(Vector4 value1, Vector4 value2) {
		return new Vector4(
				value1.X * value2.X,
				value1.Y * value2.Y,
				value1.Z * value2.Z,
				value1.W * value2.W);
	}

	public static Vector4 Multiply(Vector4 value1, float scaleFactor) {
		return new Vector4(
				value1.X * scaleFactor,
				value1.Y * scaleFactor,
				value1.Z * scaleFactor,
				value1.W * scaleFactor);
	}

	public static Vector4 Negate(Vector4 value) {
		return new Vector4(
				-value.X,
				-value.Y,
				-value.Z,
				-value.W);
	}

	public static Vector4 Normalize(Vector4 vector) {
		var factor = vector.LengthSquared();
		if (factor > 1e-6) {
			factor = 1f / MathF.Sqrt(factor);
			return new Vector4(
					vector.X * factor,
					vector.Y * factor,
					vector.Z * factor,
					vector.W * factor);
		}

		return Zero;
	}

	public static Vector4 SmoothStep(Vector4 value1, Vector4 value2, float amount) {
		return new Vector4(
				SimUtils.SmoothStep(value1.X, value2.X, amount),
				SimUtils.SmoothStep(value1.Y, value2.Y, amount),
				SimUtils.SmoothStep(value1.Z, value2.Z, amount),
				SimUtils.SmoothStep(value1.W, value2.W, amount));
	}

	public static Vector4 Subtract(Vector4 value1, Vector4 value2) {
		return new Vector4(
				value1.W - value2.W,
				value1.X - value2.X,
				value1.Y - value2.Y,
				value1.Z - value2.Z);
	}

	public static Vector4 Transform(Vector2 position, Matrix4 matrix) {
		return new Vector4(
				position.X * matrix.M11 + position.Y * matrix.M21 + matrix.M41,
				position.X * matrix.M12 + position.Y * matrix.M22 + matrix.M42,
				position.X * matrix.M13 + position.Y * matrix.M23 + matrix.M43,
				position.X * matrix.M14 + position.Y * matrix.M24 + matrix.M44);
	}

	public static Vector4 Transform(Vector3 position, Matrix4 matrix) {
		return new Vector4(
				position.X * matrix.M11 + position.Y * matrix.M21 + position.Z * matrix.M31 + matrix.M41,
				position.X * matrix.M12 + position.Y * matrix.M22 + position.Z * matrix.M32 + matrix.M42,
				position.X * matrix.M13 + position.Y * matrix.M23 + position.Z * matrix.M33 + matrix.M43,
				position.X * matrix.M14 + position.Y * matrix.M24 + position.Z * matrix.M34 + matrix.M44);
	}

	public static Vector4 Transform(Vector4 vector, Matrix4 matrix) {
		return new Vector4(
				vector.X * matrix.M11 + vector.Y * matrix.M21 + vector.Z * matrix.M31 + vector.W * matrix.M41,
				vector.X * matrix.M12 + vector.Y * matrix.M22 + vector.Z * matrix.M32 + vector.W * matrix.M42,
				vector.X * matrix.M13 + vector.Y * matrix.M23 + vector.Z * matrix.M33 + vector.W * matrix.M43,
				vector.X * matrix.M14 + vector.Y * matrix.M24 + vector.Z * matrix.M34 + vector.W * matrix.M44);
	}


	public boolean Equals(Object obj) {
		if (!(obj instanceof Vector4 other))
			return false;

		if (X != other.X)
			return false;
		if (Y != other.Y)
			return false;
		if (Z != other.Z)
			return false;
		if (W != other.W)
			return false;
		return true;
	}

	public boolean Equals(Vector4 other) {
		if (X != other.X)
			return false;
		if (Y != other.Y)
			return false;
		if (Z != other.Z)
			return false;
		if (W != other.W)
			return false;
		return true;
	}


	public boolean NotEqual(Vector4 other) {
		if (X != other.X)
			return true;
		if (Y != other.Y)
			return true;
		if (Z != other.Z)
			return true;
		if (W != other.W)
			return true;
		return false;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append('<');
		sb.append(X);
		sb.append(", ");
		sb.append(Y);
		sb.append(", ");
		sb.append(Z);
		sb.append(", ");
		sb.append(W);
		sb.append('>');
		return sb.toString();
	}


	/// <summary>A vector with a value of 0,0,0,0</summary>
	public static final Vector4 Zero = new Vector4();

	/// <summary>A vector with a value of 1,1,1,1</summary>
	public static final Vector4 One = new Vector4(1f, 1f, 1f, 1f);

	/// <summary>A vector with a value of 1,0,0,0</summary>
	public static final Vector4 UnitX = new Vector4(1f, 0f, 0f, 0f);

	/// <summary>A vector with a value of 0,1,0,0</summary>
	public static final Vector4 UnitY = new Vector4(0f, 1f, 0f, 0f);

	/// <summary>A vector with a value of 0,0,1,0</summary>
	public static final Vector4 UnitZ = new Vector4(0f, 0f, 1f, 0f);

	/// <summary>A vector with a value of 0,0,0,1</summary>
	public static final Vector4 UnitW = new Vector4(0f, 0f, 0f, 1f);

	public static final Vector4 MinValue = new Vector4(Float.MIN_VALUE);
	public static final Vector4 MaxValue = new Vector4(Float.MAX_VALUE);

	@Override
	public int compareTo(@NotNull Vector4 vector4) {

		if (vector4.X > X) return 1;
		else if (vector4.X < X) return -1;
		if (vector4.Y > Y) return 1;
		else if (vector4.Y < Y) return -1;
		if (vector4.Z > Z) return 1;
		else if (vector4.Z < Z) return -1;
		if (vector4.W > W) return 1;
		else if (vector4.W < W) return -1;


		return 0;

	}
}
