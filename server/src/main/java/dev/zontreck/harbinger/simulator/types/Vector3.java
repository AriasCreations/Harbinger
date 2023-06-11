package dev.zontreck.harbinger.simulator.types;

import dev.zontreck.harbinger.utils.MathF;
import dev.zontreck.harbinger.utils.SimUtils;

public class Vector3
{

	/// <summary>A vector with a value of 0,0,0</summary>
	public static final Vector3 Zero = new Vector3();

	/// <summary>A vector with a value of 1,1,1</summary>
	public static final Vector3 One = new Vector3(1f);

	/// <summary>A unit vector facing forward (X axis), value 1,0,0</summary>
	public static final Vector3 UnitX = new Vector3(1f, 0f, 0f);

	/// <summary>A unit vector facing left (Y axis), value 0,1,0</summary>
	public static final Vector3 UnitY = new Vector3(0f, 1f, 0f);

	/// <summary>A unit vector facing up (Z axis), value 0,0,1</summary>
	public static final Vector3 UnitZ = new Vector3(0f, 0f, 1f);

	public static final Vector3 MinValue = new Vector3(Float.MIN_VALUE);
	public static final Vector3 MaxValue = new Vector3(Float.MAX_VALUE);

	/// <summary>x value</summary>
	public float X;

	/// <summary>Y value</summary>
	public float Y;

	/// <summary>Z value</summary>
	public float Z;

	public Vector3()
	{
		X=0;
		Y=0;
		Z=0;
	}

	public Vector3(float x, float y, float z)
	{
		X = x;
		Y = y;
		Z = z;
	}

	public Vector3(float value)
	{
		X = value;
		Y = value;
		Z = value;
	}

	public Vector3(Vector2 value, float z)
	{
		X = value.X;
		Y = value.Y;
		Z = z;
	}

	public Vector3(Vector3 vector)
	{
		X = vector.X;
		Y = vector.Y;
		Z = vector.Z;
	}

	public Vector3(byte[] byteArray, int pos)
	{
		X = SimUtils.BytesToFloatSafepos(byteArray, pos);
		Y = SimUtils.BytesToFloatSafepos(byteArray, pos + 4);
		Z = SimUtils.BytesToFloatSafepos(byteArray, pos + 8);
	}

	public void Abs()
	{
		if (X < 0)
			X = -X;
		if (Y < 0)
			Y = -Y;
		if (Z < 0)
			Z = -Z;
	}

	public void Min(Vector3 v)
	{
		if (v.X < X) X = v.X;
		if (v.Y < Y) Y = v.Y;
		if (v.Z < Z) Z = v.Z;
	}

	public void Max(Vector3 v)
	{
		if (v.X > X) X = v.X;
		if (v.Y > Y) Y = v.Y;
		if (v.Z > Z) Z = v.Z;
	}

	public void Add(Vector3 v)
	{
		X += v.X;
		Y += v.Y;
		Z += v.Z;
	}

	public void Sub(Vector3 v)
	{
		X -= v.X;
		Y -= v.Y;
		Z -= v.Z;
	}

	public void Clamp(float min, float max)
	{
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

	public float Length()
{
	return MathF.Sqrt(X * X + Y * Y + Z * Z);
}

	public float LengthSquared()
{
	return X * X + Y * Y + Z * Z;
}

	public void Normalize()
	{
		var factor = X * X + Y * Y + Z * Z;
		if (factor > 1e-6f)
		{
			factor = 1f / MathF.Sqrt(factor);
			X *= factor;
			Y *= factor;
			Z *= factor;
		}
		else
		{
			X = 0f;
			Y = 0f;
			Z = 0f;
		}
	}

	public boolean ApproxEquals(Vector3 vec)
{
	return SimUtils.ApproxEqual(X, vec.X) &&
			SimUtils.ApproxEqual(Y, vec.Y) &&
			SimUtils.ApproxEqual(Z, vec.Z);
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
	public boolean ApproxEquals(Vector3 vec, float tolerance)
{
	return SimUtils.ApproxEqual(X, vec.X, tolerance) &&
			SimUtils.ApproxEqual(Y, vec.Y, tolerance) &&
			SimUtils.ApproxEqual(Z, vec.Z, tolerance);
}

	public boolean ApproxZero()
{
	if (!SimUtils.ApproxZero(X))
		return false;
	if (!SimUtils.ApproxZero(Y))
		return false;
	if (!SimUtils.ApproxZero(Z))
		return false;
	return true;
}

	public boolean ApproxZero(float tolerance)
{
	if (!SimUtils.ApproxZero(X, tolerance))
		return false;
	if (!SimUtils.ApproxZero(Y, tolerance))
		return false;
	if (!SimUtils.ApproxZero(Z, tolerance))
		return false;
	return true;
}

	public boolean IsZero()
{
	if (X != 0)
		return false;
	if (Y != 0)
		return false;
	if (Z != 0)
		return false;
	return true;
}

	public boolean IsNotZero()
{
	if (X != 0)
		return true;
	if (Y != 0)
		return true;
	if (Z != 0)
		return true;
	return false;
}

	/// <summary>
	///     IComparable.CompareTo implementation
	/// </summary>
	public int CompareTo(Vector3 vector)
{
	return LengthSquared().CompareTo(vector.LengthSquared());
}

	public float Dot(Vector3 value2)
{
	return X * value2.X + Y * value2.Y + Z * value2.Z;
}

	public float AbsDot(Vector3 value2)
{
	return MathF.Abs(X * value2.X) + MathF.Abs(Y * value2.Y) + MathF.Abs(Z * value2.Z);
}

	/// <summary>
	///     Test if this vector is composed of all finite numbers
	/// </summary>
	public boolean IsFinite()
{
	return SimUtils.IsFinite(X) && SimUtils.IsFinite(Y) && SimUtils.IsFinite(Z);
}


	/// <summary>
	///     Returns the raw bytes for this vector
	/// </summary>
	/// <returns>A 12 byte array containing X, Y, and Z</returns>
	public byte[] GetBytes()
{
	var dest = new byte[12];

	// TODO

	return dest;
}

	/// <summary>
	///     Writes the raw bytes for this vector to a byte array
	/// </summary>
	/// <param name="dest">Destination byte array</param>
	/// <param name="pos">
	///     Position in the destination array to start
	///     writing. Must be at least 12 bytes before the end of the array
	/// </param>
	public byte[] ToBytes(int pos)
{
	// TODO
}

	public void Rotate(Quaternion rot)
	{
		var x2 = rot.X + rot.X;
		var y2 = rot.Y + rot.Y;
		var z2 = rot.Z + rot.Z;

		var wx2 = rot.W * x2;
		var wy2 = rot.W * y2;
		var wz2 = rot.W * z2;
		var xx2 = rot.X * x2;
		var xy2 = rot.X * y2;
		var xz2 = rot.X * z2;
		var yy2 = rot.Y * y2;
		var yz2 = rot.Y * z2;
		var zz2 = rot.Z * z2;

		x2 = X;
		y2 = Y;
		z2 = Z;

		X = x2 * (1.0f - yy2 - zz2) + y2 * (xy2 - wz2) + z2 * (xz2 + wy2);
		Y = x2 * (xy2 + wz2) + y2 * (1.0f - xx2 - zz2) + z2 * (yz2 - wx2);
		Z = x2 * (xz2 - wy2) + y2 * (yz2 + wx2) + z2 * (1.0f - xx2 - yy2);
	}

	public void InverseRotate(Quaternion rot)
	{
		var x2 = rot.X + rot.X;
		var y2 = rot.Y + rot.Y;
		var z2 = rot.Z + rot.Z;

		var wx2 = rot.W * x2;
		var wy2 = rot.W * y2;
		var wz2 = rot.W * z2;

		var xx2 = rot.X * x2;
		var xy2 = rot.X * y2;
		var xz2 = rot.X * z2;
		var yy2 = rot.Y * y2;
		var yz2 = rot.Y * z2;
		var zz2 = rot.Z * z2;

		x2 = X;
		y2 = Y;
		z2 = Z;

		X = x2 * (1.0f - yy2 - zz2) + y2 * (xy2 + wz2) + z2 * (xz2 - wy2);
		Y = x2 * (xy2 - wz2) + y2 * (1.0f - xx2 - zz2) + z2 * (yz2 + wx2);
		Z = x2 * (xz2 + wy2) + y2 * (yz2 - wx2) + z2 * (1.0f - xx2 - yy2);
	}

	//quaternion must be normalized <0,0,z,w>
	public void RotateByQZ(Quaternion rot)
	{
		var z2 = rot.Z + rot.Z;
		var zz2 = 1.0f - rot.Z * z2;
		var wz2 = rot.W * z2;

		var ox = X;
		var oy = Y;

		X = ox * zz2 - oy * wz2;
		Y = ox * wz2 + oy * zz2;
	}

	//quaternion must be normalized <0,0,z,w>
	public void InverseRotateByQZ(Quaternion rot)
	{
		var z2 = rot.Z + rot.Z;
		var zz2 = 1.0f - rot.Z * z2;
		var wz2 = rot.W * z2;

		var ox = X;
		var oy = Y;

		X = ox * zz2 + oy * wz2;
		Y = oy * zz2 - ox * wz2;
	}

	//shortQuaternion must be normalized <z,w>
	public void RotateByShortQZ(Vector2 shortQuaternion)
	{
		var z2 = shortQuaternion.X + shortQuaternion.X;
		var zz2 = 1.0f - shortQuaternion.X * z2;
		var wz2 = shortQuaternion.Y * z2;

		var ox = X;
		var oy = Y;

		X = ox * zz2 - oy * wz2;
		Y = ox * wz2 + oy * zz2;
	}

	//quaternion must be normalized <0,0,z,w>
	public void InverseRotateByShortQZ(Vector2 shortQuaternion)
	{
		var z2 = shortQuaternion.X + shortQuaternion.X;
		var zz2 = 1.0f - shortQuaternion.X * z2;
		var wz2 = shortQuaternion.Y * z2;

		var ox = X;
		var oy = Y;

		X = ox * zz2 + oy * wz2;
		Y = oy * zz2 - ox * wz2;
	}

	public static Vector3 Add(Vector3 value1, Vector3 value2)
	{
		return new Vector3(value1.X + value2.X, value1.Y + value2.Y, value1.Z + value2.Z);
	}

	public static Vector3 Abs(Vector3 value1)
	{
		return new Vector3(MathF.Abs(value1.X), MathF.Abs(value1.Y), MathF.Abs(value1.Z));
	}

	public static Vector3 Clamp(Vector3 value1, float min, float max)
	{
		return new Vector3(
				MathF.Clamp(value1.X, min, max),
				MathF.Clamp(value1.Y, min, max),
				MathF.Clamp(value1.Z, min, max));
	}

	public static Vector3 Clamp(Vector3 value1, Vector3 min, Vector3 max)
	{
		return new Vector3(
				MathF.Clamp(value1.X, min.X, max.X),
				MathF.Clamp(value1.Y, min.Y, max.Y),
				MathF.Clamp(value1.Z, min.Z, max.Z));
	}

	public static Vector3 Cross(Vector3 value1, Vector3 value2)
	{
		return new Vector3(
				value1.Y * value2.Z - value2.Y * value1.Z,
				value1.Z * value2.X - value2.Z * value1.X,
				value1.X * value2.Y - value2.X * value1.Y);
	}

	public static float Distance(Vector3 value1, Vector3 value2)
	{
		return MathF.Sqrt(DistanceSquared(value1, value2));
	}

	public static float DistanceSquared(Vector3 value1, Vector3 value2)
	{
		var x = value1.X - value2.X;
		var y = value1.Y - value2.Y;
		var z = value1.Z - value2.Z;

		return x * x + y * y + z * z;
	}

	public static Vector3 Divide(Vector3 value1, Vector3 value2)
	{
		return new Vector3(value1.X / value2.X, value1.Y / value2.Y, value1.Z / value2.Z);
	}

	public static Vector3 Divide(Vector3 value1, float value2)
	{
		var factor = 1f / value2;
		return new Vector3(value1.X * factor, value1.Y * factor, value1.Z * factor);
	}

	public static float Dot(Vector3 value1, Vector3 value2)
	{
		return value1.X * value2.X + value1.Y * value2.Y + value1.Z * value2.Z;
	}

	public static float AbsDot(Vector3 value1, Vector3 value2)
	{
		return MathF.Abs(value1.X * value2.X) + MathF.Abs(value1.Y * value2.Y) + MathF.Abs(value1.Z * value2.Z);
	}

	public static Vector3 Lerp(Vector3 value1, Vector3 value2, float amount)
	{
		return new Vector3(
				SimUtils.Lerp(value1.X, value2.X, amount),
				SimUtils.Lerp(value1.Y, value2.Y, amount),
				SimUtils.Lerp(value1.Z, value2.Z, amount));
	}

	public static float Mag(Vector3 value)
	{
		return value.Length();
	}

	public static Vector3 Max(Vector3 value1, Vector3 value2)
	{
		return new Vector3(
				MathF.Max(value1.X, value2.X),
				MathF.Max(value1.Y, value2.Y),
				MathF.Max(value1.Z, value2.Z));
	}

	public static Vector3 Min(Vector3 value1, Vector3 value2)
	{
		return new Vector3(
				MathF.Min(value1.X, value2.X),
				MathF.Min(value1.Y, value2.Y),
				MathF.Min(value1.Z, value2.Z));
	}

	public static Vector3 Multiply(Vector3 value1, Vector3 value2)
	{
		Vector3 M = new Vector3(value1);
		M.Mul(value2);
		return M;
	}

	public static Vector3 Multiply(Vector3 value1, float scaleFactor)
	{
		return Multiply(value1, new Vector3(scaleFactor));
	}

	public static Vector3 Negate(Vector3 value)
	{
		return new Vector3(-value.X, -value.Y, -value.Z);
	}

	public static Vector3 Normalize(Vector3 value)
	{
		var factor = value.LengthSquared();
		if (factor > 1e-6f)
		{
			factor = 1f / MathF.Sqrt(factor);
			return Multiply(value, factor);
		}

		return new Vector3();
	}

	public static Vector3 SmoothStep(Vector3 value1, Vector3 value2, float amount)
	{
		return new Vector3(
				SimUtils.SmoothStep(value1.X, value2.X, amount),
				SimUtils.SmoothStep(value1.Y, value2.Y, amount),
				SimUtils.SmoothStep(value1.Z, value2.Z, amount));
	}

	public static Vector3 Subtract(Vector3 value1, Vector3 value2)
	{
		Vector3 n = new Vector3(value1);
		n.Sub(value2);
		return n;
	}

	public void Mul(Vector3 other)
	{
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
	public static Vector3 Transform(Vector3 position, Matrix4 matrix)
	{
		return new Vector3(
				position.X * matrix.M11 + position.Y * matrix.M21 + position.Z * matrix.M31 + matrix.M41,
				position.X * matrix.M12 + position.Y * matrix.M22 + position.Z * matrix.M32 + matrix.M42,
				position.X * matrix.M13 + position.Y * matrix.M23 + position.Z * matrix.M33 + matrix.M43);
	}

	public static Vector3 TransformNormal(Vector3 position, Matrix4 matrix)
	{
		return new Vector3(
				position.X * matrix.M11 + position.Y * matrix.M21 + position.Z * matrix.M31,
				position.X * matrix.M12 + position.Y * matrix.M22 + position.Z * matrix.M32,
				position.X * matrix.M13 + position.Y * matrix.M23 + position.Z * matrix.M33);
	}

	public static Vector3 Transform(Vector3 vec, Quaternion rot)
	{
		return Rotate(vec, rot);
	}

	public static Vector3 Rotate(Vector3 vec, Quaternion rot)
	{
		var x2 = rot.X + rot.X;
		var y2 = rot.Y + rot.Y;
		var z2 = rot.Z + rot.Z;

		var wx2 = rot.W * x2;
		var wy2 = rot.W * y2;
		var wz2 = rot.W * z2;
		var xx2 = rot.X * x2;
		var xy2 = rot.X * y2;
		var xz2 = rot.X * z2;
		var yy2 = rot.Y * y2;
		var yz2 = rot.Y * z2;
		var zz2 = rot.Z * z2;

		x2 = vec.X;
		y2 = vec.Y;
		z2 = vec.Z;

		return new Vector3(
				x2 * (1.0f - yy2 - zz2) + y2 * (xy2 - wz2) + z2 * (xz2 + wy2),
				x2 * (xy2 + wz2) + y2 * (1.0f - xx2 - zz2) + z2 * (yz2 - wx2),
				x2 * (xz2 - wy2) + y2 * (yz2 + wx2) + z2 * (1.0f - xx2 - yy2));
	}

	public static Vector3 InverseRotate(Vector3 vec, Quaternion rot)
	{
		var x2 = rot.X + rot.X;
		var y2 = rot.Y + rot.Y;
		var z2 = rot.Z + rot.Z;

		var wx2 = rot.W * x2;
		var wy2 = rot.W * y2;
		var wz2 = rot.W * z2;
		var xx2 = rot.X * x2;
		var xy2 = rot.X * y2;
		var xz2 = rot.X * z2;
		var yy2 = rot.Y * y2;
		var yz2 = rot.Y * z2;
		var zz2 = rot.Z * z2;

		x2 = vec.X;
		y2 = vec.Y;
		z2 = vec.Z;

		return new Vector3(
				x2 * (1.0f - yy2 - zz2) + y2 * (xy2 + wz2) + z2 * (xz2 - wy2),
				x2 * (xy2 - wz2) + y2 * (1.0f - xx2 - zz2) + z2 * (yz2 + wx2),
				x2 * (xz2 + wy2) + y2 * (yz2 - wx2) + z2 * (1.0f - xx2 - yy2));
	}

	public static Vector3 UnitXRotated(Quaternion rot)
	{
		var y2 = rot.Y + rot.Y;
		var z2 = rot.Z + rot.Z;

		var wy2 = rot.W * y2;
		var wz2 = rot.W * z2;
		var xy2 = rot.X * y2;
		var xz2 = rot.X * z2;
		var yy2 = rot.Y * y2;
		var zz2 = rot.Z * z2;

		return new Vector3(1.0f - yy2 - zz2, xy2 + wz2, xz2 - wy2);
	}

	public static Vector3 UnitYRotated(Quaternion rot)
	{
		var x2 = rot.X + rot.X;
		var y2 = rot.Y + rot.Y;
		var z2 = rot.Z + rot.Z;

		var wx2 = rot.W * x2;
		var wz2 = rot.W * z2;
		var xx2 = rot.X * x2;
		var xy2 = rot.X * y2;
		var yz2 = rot.Y * z2;
		var zz2 = rot.Z * z2;

		return new Vector3(xy2 - wz2, 1.0f - xx2 - zz2, yz2 + wx2);
	}

	public static Vector3 UnitZRotated(Quaternion rot)
	{
		var x2 = rot.X + rot.X;
		var y2 = rot.Y + rot.Y;
		var z2 = rot.Z + rot.Z;

		var wx2 = rot.W * x2;
		var wy2 = rot.W * y2;
		var xx2 = rot.X * x2;
		var xz2 = rot.X * z2;
		var yy2 = rot.Y * y2;
		var yz2 = rot.Y * z2;

		return new Vector3(xz2 + wy2, yz2 - wx2, 1.0f - xx2 - yy2);
	}

	//quaternion must be normalized <0,0,z,w>
	public static Vector3 RotateByQZ(Vector3 vec, Quaternion rot)
	{
		var z2 = rot.Z + rot.Z;
		var wz2 = rot.W * z2;
		var zz2 = 1.0f - rot.Z * z2;

		return new Vector3(
				vec.X * zz2 - vec.Y * wz2,
				vec.X * wz2 + vec.Y * zz2,
				vec.Z);

	}

	//quaternion must be normalized <0,0,z,w>
	public static Vector3 InverseRotateByQZ(Vector3 vec, Quaternion rot)
	{
		var z2 = rot.Z + rot.Z;
		var wz2 = rot.W * z2;
		var zz2 = 1.0f - rot.Z * z2;

		return new Vector3(
				vec.X * zz2 + vec.Y * wz2,
				vec.Y * zz2 - vec.X * wz2,
				vec.Z);
	}

	//shortQuaternion must be normalized <z,w>
	public static Vector3 RotateByShortQZ(Vector3 vec, Vector2 shortQuaternion)
	{
		var z2 = shortQuaternion.X + shortQuaternion.X;
		var zz2 = 1.0f - shortQuaternion.X * z2;
		var wz2 = shortQuaternion.Y * z2;

		return new Vector3(
				vec.X * zz2 - vec.Y * wz2,
				vec.X * wz2 + vec.Y * zz2,
				vec.Z);

	}

	//shortQuaternion must be normalized <z,w>
	public static Vector3 InverseRotateByShortQZ(Vector3 vec, Vector2 shortQuaternion)
	{
		var z2 = shortQuaternion.X + shortQuaternion.X;
		var zz2 = 1.0f - shortQuaternion.X * z2;
		var wz2 = shortQuaternion.Y * z2;

		return new Vector3(
				vec.X * zz2 + vec.Y * wz2,
				vec.Y * zz2 - vec.X * wz2,
				vec.Z);
	}

}
