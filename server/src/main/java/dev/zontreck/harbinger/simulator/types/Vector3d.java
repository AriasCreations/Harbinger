package dev.zontreck.harbinger.simulator.types;

import dev.zontreck.harbinger.utils.MathF;
import dev.zontreck.harbinger.utils.Matrix4;
import dev.zontreck.harbinger.utils.SimUtils;
import org.jetbrains.annotations.NotNull;

public class Vector3d implements Comparable {
	/// <summary>A vector with a value of 0,0,0</summary>
	public static final Vector3d Zero = new Vector3d();

	/// <summary>A vector with a value of 1,1,1</summary>
	public static final Vector3d One = new Vector3d(1f);

	/// <summary>A unit vector facing forward (X axis), value 1,0,0</summary>
	public static final Vector3d UnitX = new Vector3d(1f, 0f, 0f);

	/// <summary>A unit vector facing left (Y axis), value 0,1,0</summary>
	public static final Vector3d UnitY = new Vector3d(0f, 1f, 0f);

	/// <summary>A unit vector facing up (Z axis), value 0,0,1</summary>
	public static final Vector3d UnitZ = new Vector3d(0f, 0f, 1f);

	public static final Vector3d MinValue = new Vector3d(Float.MIN_VALUE);
	public static final Vector3d MaxValue = new Vector3d(Float.MAX_VALUE);

	/// <summary>x value</summary>
	public double X;

	/// <summary>Y value</summary>
	public double Y;

	/// <summary>Z value</summary>
	public double Z;

	public Vector3d() {
		X = 0;
		Y = 0;
		Z = 0;
	}

	public Vector3d(double x, double y, double z) {
		X = x;
		Y = y;
		Z = z;
	}

	public Vector3d(double value) {
		X = value;
		Y = value;
		Z = value;
	}

	public Vector3d(Vector2 value, double z) {
		X = value.X;
		Y = value.Y;
		Z = z;
	}

	public Vector3d(Vector3d vector) {
		X = vector.X;
		Y = vector.Y;
		Z = vector.Z;
	}

	public Vector3d(byte[] byteArray, int pos) {
		X = SimUtils.BytesToFloatSafepos(byteArray, pos);
		Y = SimUtils.BytesToFloatSafepos(byteArray, pos + 4);
		Z = SimUtils.BytesToFloatSafepos(byteArray, pos + 8);
	}

	public void Abs() {
		if (X < 0)
			X = -X;
		if (Y < 0)
			Y = -Y;
		if (Z < 0)
			Z = -Z;
	}

	public void Min(Vector3d v) {
		if (v.X < X) X = v.X;
		if (v.Y < Y) Y = v.Y;
		if (v.Z < Z) Z = v.Z;
	}

	public void Max(Vector3d v) {
		if (v.X > X) X = v.X;
		if (v.Y > Y) Y = v.Y;
		if (v.Z > Z) Z = v.Z;
	}

	public void Add(Vector3d v) {
		X += v.X;
		Y += v.Y;
		Z += v.Z;
	}

	public void Sub(Vector3d v) {
		X -= v.X;
		Y -= v.Y;
		Z -= v.Z;
	}

	public void Clamp(double min, double max) {
		if (X > max)
			X = max;
		else if (X < min)
			X = min;

		if (Y > max)
			Y = max;
		else if (Y < min)
			Y = min;

		if (Z > max)
			Z = max;
		else if (Z < min)
			Z = min;
	}

	public double Length() {
		return Math.sqrt(X * X + Y * Y + Z * Z);
	}

	public double LengthSquared() {
		return X * X + Y * Y + Z * Z;
	}

	public void Normalize() {
		var factor = X * X + Y * Y + Z * Z;
		if (factor > 1e-6f) {
			factor = 1f / Math.sqrt(factor);
			X *= factor;
			Y *= factor;
			Z *= factor;
		} else {
			X = 0;
			Y = 0;
			Z = 0;
		}
	}

	/// <summary>
	///     Test if this vector is equal to another vector, within a given
	///     tolerance range
	/// </summary>
	/// <param name="vec">Vector to test against</param>
	/// <param name="tolerance">
	///     The acceptable magnitude of difference
	///     between the two vectors
	/// </param>
	/// <returns>
	///     True if the magnitude of difference between the two vectors
	///     is less than the given tolerance, otherwise false
	/// </returns>
	public boolean ApproxEquals(Vector3d vec, double tolerance) {
		var diff = Subtract(this, vec);
		return diff.LengthSquared() <= tolerance * tolerance;
	}


	/// <summary>
	///     Test if this vector is composed of all finite numbers
	/// </summary>
	public boolean IsFinite() {
		return SimUtils.IsFinite(X) && SimUtils.IsFinite(Y) && SimUtils.IsFinite(Z);
	}


	/// <summary>
	///     Returns the raw bytes for this vector
	/// </summary>
	/// <returns>A 12 byte array containing X, Y, and Z</returns>
	public byte[] GetBytes() {
		var dest = new byte[12];

		// TODO

		return dest;
	}


	//quaternion must be normalized <0,0,z,w>
	public void RotateByQZ(Quaternion rot) {
		var z2 = rot.Z + rot.Z;
		var zz2 = 1.0f - rot.Z * z2;
		var wz2 = rot.W * z2;

		var ox = X;
		var oy = Y;

		X = ox * zz2 - oy * wz2;
		Y = ox * wz2 + oy * zz2;
	}

	//quaternion must be normalized <0,0,z,w>
	public void InverseRotateByQZ(Quaternion rot) {
		var z2 = rot.Z + rot.Z;
		var zz2 = 1.0f - rot.Z * z2;
		var wz2 = rot.W * z2;

		var ox = X;
		var oy = Y;

		X = ox * zz2 + oy * wz2;
		Y = oy * zz2 - ox * wz2;
	}

	//shortQuaternion must be normalized <z,w>
	public void RotateByShortQZ(Vector2 shortQuaternion) {
		var z2 = shortQuaternion.X + shortQuaternion.X;
		var zz2 = 1.0f - shortQuaternion.X * z2;
		var wz2 = shortQuaternion.Y * z2;

		var ox = X;
		var oy = Y;

		X = ox * zz2 - oy * wz2;
		Y = ox * wz2 + oy * zz2;
	}

	//quaternion must be normalized <0,0,z,w>
	public void InverseRotateByShortQZ(Vector2 shortQuaternion) {
		var z2 = shortQuaternion.X + shortQuaternion.X;
		var zz2 = 1.0f - shortQuaternion.X * z2;
		var wz2 = shortQuaternion.Y * z2;

		var ox = X;
		var oy = Y;

		X = ox * zz2 + oy * wz2;
		Y = oy * zz2 - ox * wz2;
	}

	public static Vector3d Add(Vector3d value1, Vector3d value2) {
		return new Vector3d(value1.X + value2.X, value1.Y + value2.Y, value1.Z + value2.Z);
	}

	public static Vector3d Abs(Vector3d value1) {
		return new Vector3d(Math.abs(value1.X), Math.abs(value1.Y), Math.abs(value1.Z));
	}

	public static Vector3d Clamp(Vector3d value1, double min, double max) {
		return new Vector3d(
				MathF.Clamp(value1.X, min, max),
				MathF.Clamp(value1.Y, min, max),
				MathF.Clamp(value1.Z, min, max));
	}

	public static Vector3d Clamp(Vector3d value1, Vector3d min, Vector3d max) {
		return new Vector3d(
				MathF.Clamp(value1.X, min.X, max.X),
				MathF.Clamp(value1.Y, min.Y, max.Y),
				MathF.Clamp(value1.Z, min.Z, max.Z));
	}

	public static Vector3d Cross(Vector3d value1, Vector3d value2) {
		return new Vector3d(
				value1.Y * value2.Z - value2.Y * value1.Z,
				value1.Z * value2.X - value2.Z * value1.X,
				value1.X * value2.Y - value2.X * value1.Y);
	}

	public static double Distance(Vector3d value1, Vector3d value2) {
		return Math.sqrt(DistanceSquared(value1, value2));
	}

	public static double DistanceSquared(Vector3d value1, Vector3d value2) {
		var x = value1.X - value2.X;
		var y = value1.Y - value2.Y;
		var z = value1.Z - value2.Z;

		return x * x + y * y + z * z;
	}

	public static Vector3d Divide(Vector3d value1, Vector3d value2) {
		return new Vector3d(value1.X / value2.X, value1.Y / value2.Y, value1.Z / value2.Z);
	}

	public static Vector3d Divide(Vector3d value1, double value2) {
		var factor = 1f / value2;
		return new Vector3d(value1.X * factor, value1.Y * factor, value1.Z * factor);
	}

	public static Vector3d Lerp(Vector3d value1, Vector3d value2, double amount) {
		return new Vector3d(
				SimUtils.Lerp(value1.X, value2.X, amount),
				SimUtils.Lerp(value1.Y, value2.Y, amount),
				SimUtils.Lerp(value1.Z, value2.Z, amount));
	}

	public static double Mag(Vector3d value) {
		return value.Length();
	}

	public static Vector3d Max(Vector3d value1, Vector3d value2) {
		return new Vector3d(
				Math.max(value1.X, value2.X),
				Math.max(value1.Y, value2.Y),
				Math.max(value1.Z, value2.Z));
	}

	public static Vector3d Min(Vector3d value1, Vector3d value2) {
		return new Vector3d(
				Math.min(value1.X, value2.X),
				Math.min(value1.Y, value2.Y),
				Math.min(value1.Z, value2.Z));
	}

	public static Vector3d Multiply(Vector3d value1, Vector3d value2) {
		Vector3d M = new Vector3d(value1);
		M.Mul(value2);
		return M;
	}

	public static Vector3d Multiply(Vector3d value1, double scaleFactor) {
		return Multiply(value1, new Vector3d(scaleFactor));
	}

	public static Vector3d Negate(Vector3d value) {
		return new Vector3d(-value.X, -value.Y, -value.Z);
	}

	public static Vector3d Normalize(Vector3d value) {
		var factor = value.LengthSquared();
		if (factor > 1e-6f) {
			factor = 1d / Math.sqrt(factor);
			return Multiply(value, factor);
		}

		return new Vector3d();
	}

	public static Vector3d SmoothStep(Vector3d value1, Vector3d value2, double amount) {
		return new Vector3d(
				SimUtils.SmoothStep(value1.X, value2.X, amount),
				SimUtils.SmoothStep(value1.Y, value2.Y, amount),
				SimUtils.SmoothStep(value1.Z, value2.Z, amount));
	}

	public static Vector3d Subtract(Vector3d value1, Vector3d value2) {
		Vector3d n = new Vector3d(value1);
		n.Sub(value2);
		return n;
	}

	public void Mul(Vector3d other) {
		X *= other.X;
		Y *= other.Y;
		Z *= other.Z;
	}

	/*
	[MethodImpl(MethodImplOptions.AggressiveInlining)]
	public unsafe static Vector3 SubtractS(Vector3 value1, Vector3 value2)
	{
		if (Sse2.IsSupported)
		{
			Vector128<float> ma = Sse2.LoadScalarVector128((double*)&value1.X).AsSingle();
			ma = Sse2.Shuffle(ma, Sse2.LoadScalarVector128((float*)&value1.Z), 0x44);

			Vector128<float>  mb = Sse2.LoadScalarVector128((double*)&value2.X).AsSingle();
			mb = Sse2.Shuffle(mb, Sse2.LoadScalarVector128((float*)&value2.Z), 0x44);

			ma = Sse.Subtract(ma, mb);
			Vector3 ret = new();
			Sse2.StoreScalar((double*)&ret.X, ma.AsDouble());
			Sse2.StoreScalar(&ret.Z, Sse2.Shuffle(ma.AsInt32(), 0x02).AsSingle());
			return ret;
		}
		else
			return Subtract(value1, value2);
	}

	[MethodImpl(MethodImplOptions.AggressiveInlining)]
	public static Vector3 AddS(Vector3 value1, Vector3 value2)
	{
		if (Sse2.IsSupported)
		{
			unsafe
			{
				Vector128<float> ma = Sse2.LoadScalarVector128((double*)&value1.X).AsSingle();
				ma = Sse2.Shuffle(ma, Sse2.LoadScalarVector128(&value1.Z), 0x44);

				Vector128<float> mb = Sse2.LoadScalarVector128((double*)&value2.X).AsSingle();
				mb = Sse2.Shuffle(mb, Sse2.LoadScalarVector128(&value2.Z), 0x44);

				ma = Sse.Add(ma, mb);
				Vector3 ret = new();
				Sse2.StoreScalar((double*)&ret.X, ma.AsDouble());
				Sse2.StoreScalar(&ret.Z, Sse2.Shuffle(ma.AsInt32(), 0x02).AsSingle());
				return ret;
			}
		}
		else
			return Subtract(value1, value2);
	}
	*/
	public static Vector3d Transform(Vector3d position, Matrix4 matrix) {
		return new Vector3d(
				position.X * matrix.M11 + position.Y * matrix.M21 + position.Z * matrix.M31 + matrix.M41,
				position.X * matrix.M12 + position.Y * matrix.M22 + position.Z * matrix.M32 + matrix.M42,
				position.X * matrix.M13 + position.Y * matrix.M23 + position.Z * matrix.M33 + matrix.M43);
	}

	public static Vector3d TransformNormal(Vector3d position, Matrix4 matrix) {
		return new Vector3d(
				position.X * matrix.M11 + position.Y * matrix.M21 + position.Z * matrix.M31,
				position.X * matrix.M12 + position.Y * matrix.M22 + position.Z * matrix.M32,
				position.X * matrix.M13 + position.Y * matrix.M23 + position.Z * matrix.M33);
	}

	public static Vector3d UnitXRotated(Quaternion rot) {
		var y2 = rot.Y + rot.Y;
		var z2 = rot.Z + rot.Z;

		var wy2 = rot.W * y2;
		var wz2 = rot.W * z2;
		var xy2 = rot.X * y2;
		var xz2 = rot.X * z2;
		var yy2 = rot.Y * y2;
		var zz2 = rot.Z * z2;

		return new Vector3d(1.0f - yy2 - zz2, xy2 + wz2, xz2 - wy2);
	}

	public static Vector3d UnitYRotated(Quaternion rot) {
		var x2 = rot.X + rot.X;
		var y2 = rot.Y + rot.Y;
		var z2 = rot.Z + rot.Z;

		var wx2 = rot.W * x2;
		var wz2 = rot.W * z2;
		var xx2 = rot.X * x2;
		var xy2 = rot.X * y2;
		var yz2 = rot.Y * z2;
		var zz2 = rot.Z * z2;

		return new Vector3d(xy2 - wz2, 1.0f - xx2 - zz2, yz2 + wx2);
	}

	public static Vector3d UnitZRotated(Quaternion rot) {
		var x2 = rot.X + rot.X;
		var y2 = rot.Y + rot.Y;
		var z2 = rot.Z + rot.Z;

		var wx2 = rot.W * x2;
		var wy2 = rot.W * y2;
		var xx2 = rot.X * x2;
		var xz2 = rot.X * z2;
		var yy2 = rot.Y * y2;
		var yz2 = rot.Y * z2;

		return new Vector3d(xz2 + wy2, yz2 - wx2, 1.0f - xx2 - yy2);
	}

	//quaternion must be normalized <0,0,z,w>
	public static Vector3d RotateByQZ(Vector3d vec, Quaternion rot) {
		var z2 = rot.Z + rot.Z;
		var wz2 = rot.W * z2;
		var zz2 = 1.0f - rot.Z * z2;

		return new Vector3d(
				vec.X * zz2 - vec.Y * wz2,
				vec.X * wz2 + vec.Y * zz2,
				vec.Z);

	}

	//quaternion must be normalized <0,0,z,w>
	public static Vector3d InverseRotateByQZ(Vector3d vec, Quaternion rot) {
		var z2 = rot.Z + rot.Z;
		var wz2 = rot.W * z2;
		var zz2 = 1.0f - rot.Z * z2;

		return new Vector3d(
				vec.X * zz2 + vec.Y * wz2,
				vec.Y * zz2 - vec.X * wz2,
				vec.Z);
	}

	//shortQuaternion must be normalized <z,w>
	public static Vector3d RotateByShortQZ(Vector3d vec, Vector2 shortQuaternion) {
		var z2 = shortQuaternion.X + shortQuaternion.X;
		var zz2 = 1.0f - shortQuaternion.X * z2;
		var wz2 = shortQuaternion.Y * z2;

		return new Vector3d(
				vec.X * zz2 - vec.Y * wz2,
				vec.X * wz2 + vec.Y * zz2,
				vec.Z);

	}

	//shortQuaternion must be normalized <z,w>
	public static Vector3d InverseRotateByShortQZ(Vector3d vec, Vector2 shortQuaternion) {
		var z2 = shortQuaternion.X + shortQuaternion.X;
		var zz2 = 1.0f - shortQuaternion.X * z2;
		var wz2 = shortQuaternion.Y * z2;

		return new Vector3d(
				vec.X * zz2 + vec.Y * wz2,
				vec.Y * zz2 - vec.X * wz2,
				vec.Z);
	}

	@Override
	public int compareTo(@NotNull Object o) {
		return 0;
	}
}
