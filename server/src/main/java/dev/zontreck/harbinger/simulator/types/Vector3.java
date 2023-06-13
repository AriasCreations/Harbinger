package dev.zontreck.harbinger.simulator.types;

import dev.zontreck.harbinger.utils.MathF;
import dev.zontreck.harbinger.utils.Matrix4;
import dev.zontreck.harbinger.utils.SimUtils;
import org.jetbrains.annotations.NotNull;

public class Vector3 implements Comparable {
	/// <summary>A vector with a value of 0,0,0</summary>
	public static final Vector3 Zero = new Vector3();

	/// <summary>A vector with a value of 1,1,1</summary>
	public static final Vector3 One = new Vector3(1.0f);

	/// <summary>A unit vector facing forward (X axis), value 1,0,0</summary>
	public static final Vector3 UnitX = new Vector3(1.0f, 0.0f, 0.0f);

	/// <summary>A unit vector facing left (Y axis), value 0,1,0</summary>
	public static final Vector3 UnitY = new Vector3(0.0f, 1.0f, 0.0f);

	/// <summary>A unit vector facing up (Z axis), value 0,0,1</summary>
	public static final Vector3 UnitZ = new Vector3(0.0f, 0.0f, 1.0f);

	public static final Vector3 MinValue = new Vector3(Float.MIN_VALUE);
	public static final Vector3 MaxValue = new Vector3(Float.MAX_VALUE);

	/// <summary>x value</summary>
	public float X;

	/// <summary>Y value</summary>
	public float Y;

	/// <summary>Z value</summary>
	public float Z;

	public Vector3() {
		this.X = 0;
		this.Y = 0;
		this.Z = 0;
	}

	public Vector3(final float x, final float y, final float z) {
		this.X = x;
		this.Y = y;
		this.Z = z;
	}

	public Vector3(final float value) {
		this.X = value;
		this.Y = value;
		this.Z = value;
	}

	public Vector3(final Vector2 value, final float z) {
		this.X = value.X;
		this.Y = value.Y;
		this.Z = z;
	}

	public Vector3(final Vector3 vector) {
		this.X = vector.X;
		this.Y = vector.Y;
		this.Z = vector.Z;
	}

	public Vector3(final byte[] byteArray, final int pos) {
		this.X = SimUtils.BytesToFloatSafepos(byteArray, pos);
		this.Y = SimUtils.BytesToFloatSafepos(byteArray, pos + 4);
		this.Z = SimUtils.BytesToFloatSafepos(byteArray, pos + 8);
	}

	public void Abs() {
		if (0 > X)
			this.X = -this.X;
		if (0 > Y)
			this.Y = -this.Y;
		if (0 > Z)
			this.Z = -this.Z;
	}

	public void Min(final Vector3 v) {
		if (v.X < this.X) this.X = v.X;
		if (v.Y < this.Y) this.Y = v.Y;
		if (v.Z < this.Z) this.Z = v.Z;
	}

	public void Max(final Vector3 v) {
		if (v.X > this.X) this.X = v.X;
		if (v.Y > this.Y) this.Y = v.Y;
		if (v.Z > this.Z) this.Z = v.Z;
	}

	public void Add(final Vector3 v) {
		this.X += v.X;
		this.Y += v.Y;
		this.Z += v.Z;
	}

	public void Sub(final Vector3 v) {
		this.X -= v.X;
		this.Y -= v.Y;
		this.Z -= v.Z;
	}

	public void Clamp(final float min, final float max) {
		if (this.X > max)
			this.X = max;
		else if (this.X < min)
			this.X = min;

		if (this.Y > max)
			this.Y = max;
		else if (this.Y < min)
			this.Y = min;

		if (this.Z > max)
			this.Z = max;
		else if (this.Z < min)
			this.Z = min;
	}

	public float Length() {
		return MathF.Sqrt(this.X * this.X + this.Y * this.Y + this.Z * this.Z);
	}

	public float LengthSquared() {
		return this.X * this.X + this.Y * this.Y + this.Z * this.Z;
	}

	public void Normalize() {
		var factor = this.X * this.X + this.Y * this.Y + this.Z * this.Z;
		if (1.0e-6f < factor) {
			factor = 1.0f / MathF.Sqrt(factor);
			this.X *= factor;
			this.Y *= factor;
			this.Z *= factor;
		} else {
			this.X = 0.0f;
			this.Y = 0.0f;
			this.Z = 0.0f;
		}
	}

	public boolean ApproxEquals(final Vector3 vec) {
		return SimUtils.ApproxEqual(this.X, vec.X) &&
				SimUtils.ApproxEqual(this.Y, vec.Y) &&
				SimUtils.ApproxEqual(this.Z, vec.Z);
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
	public boolean ApproxEquals(final Vector3 vec, final float tolerance) {
		return SimUtils.ApproxEqual(this.X, vec.X, tolerance) &&
				SimUtils.ApproxEqual(this.Y, vec.Y, tolerance) &&
				SimUtils.ApproxEqual(this.Z, vec.Z, tolerance);
	}

	public boolean ApproxZero() {
		if (!SimUtils.ApproxZero(this.X))
			return false;
		if (!SimUtils.ApproxZero(this.Y))
			return false;
		return SimUtils.ApproxZero(this.Z);
	}

	public boolean ApproxZero(final float tolerance) {
		if (!SimUtils.ApproxZero(this.X, tolerance))
			return false;
		if (!SimUtils.ApproxZero(this.Y, tolerance))
			return false;
		return SimUtils.ApproxZero(this.Z, tolerance);
	}

	public boolean IsZero() {
		if (0 != X)
			return false;
		if (0 != Y)
			return false;
		return 0 == Z;
	}

	public boolean IsNotZero() {
		if (0 != X)
			return true;
		if (0 != Y)
			return true;
		return 0 != Z;
	}


	public float Dot(final Vector3 value2) {
		return this.X * value2.X + this.Y * value2.Y + this.Z * value2.Z;
	}

	public float AbsDot(final Vector3 value2) {
		return MathF.Abs(this.X * value2.X) + MathF.Abs(this.Y * value2.Y) + MathF.Abs(this.Z * value2.Z);
	}

	/// <summary>
	///     Test if this vector is composed of all finite numbers
	/// </summary>
	public boolean IsFinite() {
		return SimUtils.IsFinite(this.X) && SimUtils.IsFinite(this.Y) && SimUtils.IsFinite(this.Z);
	}


	/// <summary>
	///     Returns the raw bytes for this vector
	/// </summary>
	/// <returns>A 12 byte array containing X, Y, and Z</returns>
	public byte[] GetBytes() {
		final var dest = new byte[12];

		// TODO

		return dest;
	}


	public void Rotate(final Quaternion rot) {
		var x2 = rot.X + rot.X;
		var y2 = rot.Y + rot.Y;
		var z2 = rot.Z + rot.Z;

		final var wx2 = rot.W * x2;
		final var wy2 = rot.W * y2;
		final var wz2 = rot.W * z2;
		final var xx2 = rot.X * x2;
		final var xy2 = rot.X * y2;
		final var xz2 = rot.X * z2;
		final var yy2 = rot.Y * y2;
		final var yz2 = rot.Y * z2;
		final var zz2 = rot.Z * z2;

		x2 = this.X;
		y2 = this.Y;
		z2 = this.Z;

		this.X = x2 * (1.0f - yy2 - zz2) + y2 * (xy2 - wz2) + z2 * (xz2 + wy2);
		this.Y = x2 * (xy2 + wz2) + y2 * (1.0f - xx2 - zz2) + z2 * (yz2 - wx2);
		this.Z = x2 * (xz2 - wy2) + y2 * (yz2 + wx2) + z2 * (1.0f - xx2 - yy2);
	}

	public void InverseRotate(final Quaternion rot) {
		var x2 = rot.X + rot.X;
		var y2 = rot.Y + rot.Y;
		var z2 = rot.Z + rot.Z;

		final var wx2 = rot.W * x2;
		final var wy2 = rot.W * y2;
		final var wz2 = rot.W * z2;

		final var xx2 = rot.X * x2;
		final var xy2 = rot.X * y2;
		final var xz2 = rot.X * z2;
		final var yy2 = rot.Y * y2;
		final var yz2 = rot.Y * z2;
		final var zz2 = rot.Z * z2;

		x2 = this.X;
		y2 = this.Y;
		z2 = this.Z;

		this.X = x2 * (1.0f - yy2 - zz2) + y2 * (xy2 + wz2) + z2 * (xz2 - wy2);
		this.Y = x2 * (xy2 - wz2) + y2 * (1.0f - xx2 - zz2) + z2 * (yz2 + wx2);
		this.Z = x2 * (xz2 + wy2) + y2 * (yz2 - wx2) + z2 * (1.0f - xx2 - yy2);
	}

	//quaternion must be normalized <0,0,z,w>
	public void RotateByQZ(final Quaternion rot) {
		final var z2 = rot.Z + rot.Z;
		final var zz2 = 1.0f - rot.Z * z2;
		final var wz2 = rot.W * z2;

		final var ox = this.X;
		final var oy = this.Y;

		this.X = ox * zz2 - oy * wz2;
		this.Y = ox * wz2 + oy * zz2;
	}

	//quaternion must be normalized <0,0,z,w>
	public void InverseRotateByQZ(final Quaternion rot) {
		final var z2 = rot.Z + rot.Z;
		final var zz2 = 1.0f - rot.Z * z2;
		final var wz2 = rot.W * z2;

		final var ox = this.X;
		final var oy = this.Y;

		this.X = ox * zz2 + oy * wz2;
		this.Y = oy * zz2 - ox * wz2;
	}

	//shortQuaternion must be normalized <z,w>
	public void RotateByShortQZ(final Vector2 shortQuaternion) {
		final var z2 = shortQuaternion.X + shortQuaternion.X;
		final var zz2 = 1.0f - shortQuaternion.X * z2;
		final var wz2 = shortQuaternion.Y * z2;

		final var ox = this.X;
		final var oy = this.Y;

		this.X = ox * zz2 - oy * wz2;
		this.Y = ox * wz2 + oy * zz2;
	}

	//quaternion must be normalized <0,0,z,w>
	public void InverseRotateByShortQZ(final Vector2 shortQuaternion) {
		final var z2 = shortQuaternion.X + shortQuaternion.X;
		final var zz2 = 1.0f - shortQuaternion.X * z2;
		final var wz2 = shortQuaternion.Y * z2;

		final var ox = this.X;
		final var oy = this.Y;

		this.X = ox * zz2 + oy * wz2;
		this.Y = oy * zz2 - ox * wz2;
	}

	public static Vector3 Add(final Vector3 value1, final Vector3 value2) {
		return new Vector3(value1.X + value2.X, value1.Y + value2.Y, value1.Z + value2.Z);
	}

	public static Vector3 Abs(final Vector3 value1) {
		return new Vector3(MathF.Abs(value1.X), MathF.Abs(value1.Y), MathF.Abs(value1.Z));
	}

	public static Vector3 Clamp(final Vector3 value1, final float min, final float max) {
		return new Vector3(
				MathF.Clamp(value1.X, min, max),
				MathF.Clamp(value1.Y, min, max),
				MathF.Clamp(value1.Z, min, max));
	}

	public static Vector3 Clamp(final Vector3 value1, final Vector3 min, final Vector3 max) {
		return new Vector3(
				MathF.Clamp(value1.X, min.X, max.X),
				MathF.Clamp(value1.Y, min.Y, max.Y),
				MathF.Clamp(value1.Z, min.Z, max.Z));
	}

	public static Vector3 Cross(final Vector3 value1, final Vector3 value2) {
		return new Vector3(
				value1.Y * value2.Z - value2.Y * value1.Z,
				value1.Z * value2.X - value2.Z * value1.X,
				value1.X * value2.Y - value2.X * value1.Y);
	}

	public static float Distance(final Vector3 value1, final Vector3 value2) {
		return MathF.Sqrt(Vector3.DistanceSquared(value1, value2));
	}

	public static float DistanceSquared(final Vector3 value1, final Vector3 value2) {
		final var x = value1.X - value2.X;
		final var y = value1.Y - value2.Y;
		final var z = value1.Z - value2.Z;

		return x * x + y * y + z * z;
	}

	public static Vector3 Divide(final Vector3 value1, final Vector3 value2) {
		return new Vector3(value1.X / value2.X, value1.Y / value2.Y, value1.Z / value2.Z);
	}

	public static Vector3 Divide(final Vector3 value1, final float value2) {
		final var factor = 1.0f / value2;
		return new Vector3(value1.X * factor, value1.Y * factor, value1.Z * factor);
	}

	public static float Dot(final Vector3 value1, final Vector3 value2) {
		return value1.X * value2.X + value1.Y * value2.Y + value1.Z * value2.Z;
	}

	public static float AbsDot(final Vector3 value1, final Vector3 value2) {
		return MathF.Abs(value1.X * value2.X) + MathF.Abs(value1.Y * value2.Y) + MathF.Abs(value1.Z * value2.Z);
	}

	public static Vector3 Lerp(final Vector3 value1, final Vector3 value2, final float amount) {
		return new Vector3(
				SimUtils.Lerp(value1.X, value2.X, amount),
				SimUtils.Lerp(value1.Y, value2.Y, amount),
				SimUtils.Lerp(value1.Z, value2.Z, amount));
	}

	public static float Mag(final Vector3 value) {
		return value.Length();
	}

	public static Vector3 Max(final Vector3 value1, final Vector3 value2) {
		return new Vector3(
				MathF.Max(value1.X, value2.X),
				MathF.Max(value1.Y, value2.Y),
				MathF.Max(value1.Z, value2.Z));
	}

	public static Vector3 Min(final Vector3 value1, final Vector3 value2) {
		return new Vector3(
				MathF.Min(value1.X, value2.X),
				MathF.Min(value1.Y, value2.Y),
				MathF.Min(value1.Z, value2.Z));
	}

	public static Vector3 Multiply(final Vector3 value1, final Vector3 value2) {
		final Vector3 M = new Vector3(value1);
		M.Mul(value2);
		return M;
	}

	public static Vector3 Multiply(final Vector3 value1, final float scaleFactor) {
		return Vector3.Multiply(value1, new Vector3(scaleFactor));
	}

	public static Vector3 Negate(final Vector3 value) {
		return new Vector3(-value.X, -value.Y, -value.Z);
	}

	public static Vector3 Normalize(final Vector3 value) {
		var factor = value.LengthSquared();
		if (1.0e-6f < factor) {
			factor = 1.0f / MathF.Sqrt(factor);
			return Vector3.Multiply(value, factor);
		}

		return new Vector3();
	}

	public static Vector3 SmoothStep(final Vector3 value1, final Vector3 value2, final float amount) {
		return new Vector3(
				SimUtils.SmoothStep(value1.X, value2.X, amount),
				SimUtils.SmoothStep(value1.Y, value2.Y, amount),
				SimUtils.SmoothStep(value1.Z, value2.Z, amount));
	}

	public static Vector3 Subtract(final Vector3 value1, final Vector3 value2) {
		final Vector3 n = new Vector3(value1);
		n.Sub(value2);
		return n;
	}

	public void Mul(final Vector3 other) {
		this.X *= other.X;
		this.Y *= other.Y;
		this.Z *= other.Z;
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
	public static Vector3 Transform(final Vector3 position, final Matrix4 matrix) {
		return new Vector3(
				position.X * matrix.M11 + position.Y * matrix.M21 + position.Z * matrix.M31 + matrix.M41,
				position.X * matrix.M12 + position.Y * matrix.M22 + position.Z * matrix.M32 + matrix.M42,
				position.X * matrix.M13 + position.Y * matrix.M23 + position.Z * matrix.M33 + matrix.M43);
	}

	public static Vector3 TransformNormal(final Vector3 position, final Matrix4 matrix) {
		return new Vector3(
				position.X * matrix.M11 + position.Y * matrix.M21 + position.Z * matrix.M31,
				position.X * matrix.M12 + position.Y * matrix.M22 + position.Z * matrix.M32,
				position.X * matrix.M13 + position.Y * matrix.M23 + position.Z * matrix.M33);
	}

	public static Vector3 Transform(final Vector3 vec, final Quaternion rot) {
		return Vector3.Rotate(vec, rot);
	}

	public static Vector3 Rotate(final Vector3 vec, final Quaternion rot) {
		var x2 = rot.X + rot.X;
		var y2 = rot.Y + rot.Y;
		var z2 = rot.Z + rot.Z;

		final var wx2 = rot.W * x2;
		final var wy2 = rot.W * y2;
		final var wz2 = rot.W * z2;
		final var xx2 = rot.X * x2;
		final var xy2 = rot.X * y2;
		final var xz2 = rot.X * z2;
		final var yy2 = rot.Y * y2;
		final var yz2 = rot.Y * z2;
		final var zz2 = rot.Z * z2;

		x2 = vec.X;
		y2 = vec.Y;
		z2 = vec.Z;

		return new Vector3(
				x2 * (1.0f - yy2 - zz2) + y2 * (xy2 - wz2) + z2 * (xz2 + wy2),
				x2 * (xy2 + wz2) + y2 * (1.0f - xx2 - zz2) + z2 * (yz2 - wx2),
				x2 * (xz2 - wy2) + y2 * (yz2 + wx2) + z2 * (1.0f - xx2 - yy2));
	}

	public static Vector3 InverseRotate(final Vector3 vec, final Quaternion rot) {
		var x2 = rot.X + rot.X;
		var y2 = rot.Y + rot.Y;
		var z2 = rot.Z + rot.Z;

		final var wx2 = rot.W * x2;
		final var wy2 = rot.W * y2;
		final var wz2 = rot.W * z2;
		final var xx2 = rot.X * x2;
		final var xy2 = rot.X * y2;
		final var xz2 = rot.X * z2;
		final var yy2 = rot.Y * y2;
		final var yz2 = rot.Y * z2;
		final var zz2 = rot.Z * z2;

		x2 = vec.X;
		y2 = vec.Y;
		z2 = vec.Z;

		return new Vector3(
				x2 * (1.0f - yy2 - zz2) + y2 * (xy2 + wz2) + z2 * (xz2 - wy2),
				x2 * (xy2 - wz2) + y2 * (1.0f - xx2 - zz2) + z2 * (yz2 + wx2),
				x2 * (xz2 + wy2) + y2 * (yz2 - wx2) + z2 * (1.0f - xx2 - yy2));
	}

	public static Vector3 UnitXRotated(final Quaternion rot) {
		final var y2 = rot.Y + rot.Y;
		final var z2 = rot.Z + rot.Z;

		final var wy2 = rot.W * y2;
		final var wz2 = rot.W * z2;
		final var xy2 = rot.X * y2;
		final var xz2 = rot.X * z2;
		final var yy2 = rot.Y * y2;
		final var zz2 = rot.Z * z2;

		return new Vector3(1.0f - yy2 - zz2, xy2 + wz2, xz2 - wy2);
	}

	public static Vector3 UnitYRotated(final Quaternion rot) {
		final var x2 = rot.X + rot.X;
		final var y2 = rot.Y + rot.Y;
		final var z2 = rot.Z + rot.Z;

		final var wx2 = rot.W * x2;
		final var wz2 = rot.W * z2;
		final var xx2 = rot.X * x2;
		final var xy2 = rot.X * y2;
		final var yz2 = rot.Y * z2;
		final var zz2 = rot.Z * z2;

		return new Vector3(xy2 - wz2, 1.0f - xx2 - zz2, yz2 + wx2);
	}

	public static Vector3 UnitZRotated(final Quaternion rot) {
		final var x2 = rot.X + rot.X;
		final var y2 = rot.Y + rot.Y;
		final var z2 = rot.Z + rot.Z;

		final var wx2 = rot.W * x2;
		final var wy2 = rot.W * y2;
		final var xx2 = rot.X * x2;
		final var xz2 = rot.X * z2;
		final var yy2 = rot.Y * y2;
		final var yz2 = rot.Y * z2;

		return new Vector3(xz2 + wy2, yz2 - wx2, 1.0f - xx2 - yy2);
	}

	//quaternion must be normalized <0,0,z,w>
	public static Vector3 RotateByQZ(final Vector3 vec, final Quaternion rot) {
		final var z2 = rot.Z + rot.Z;
		final var wz2 = rot.W * z2;
		final var zz2 = 1.0f - rot.Z * z2;

		return new Vector3(
				vec.X * zz2 - vec.Y * wz2,
				vec.X * wz2 + vec.Y * zz2,
				vec.Z);

	}

	//quaternion must be normalized <0,0,z,w>
	public static Vector3 InverseRotateByQZ(final Vector3 vec, final Quaternion rot) {
		final var z2 = rot.Z + rot.Z;
		final var wz2 = rot.W * z2;
		final var zz2 = 1.0f - rot.Z * z2;

		return new Vector3(
				vec.X * zz2 + vec.Y * wz2,
				vec.Y * zz2 - vec.X * wz2,
				vec.Z);
	}

	//shortQuaternion must be normalized <z,w>
	public static Vector3 RotateByShortQZ(final Vector3 vec, final Vector2 shortQuaternion) {
		final var z2 = shortQuaternion.X + shortQuaternion.X;
		final var zz2 = 1.0f - shortQuaternion.X * z2;
		final var wz2 = shortQuaternion.Y * z2;

		return new Vector3(
				vec.X * zz2 - vec.Y * wz2,
				vec.X * wz2 + vec.Y * zz2,
				vec.Z);

	}

	//shortQuaternion must be normalized <z,w>
	public static Vector3 InverseRotateByShortQZ(final Vector3 vec, final Vector2 shortQuaternion) {
		final var z2 = shortQuaternion.X + shortQuaternion.X;
		final var zz2 = 1.0f - shortQuaternion.X * z2;
		final var wz2 = shortQuaternion.Y * z2;

		return new Vector3(
				vec.X * zz2 + vec.Y * wz2,
				vec.Y * zz2 - vec.X * wz2,
				vec.Z);
	}

	@Override
	public int compareTo(@NotNull final Object o) {
		return 0;
	}
}
