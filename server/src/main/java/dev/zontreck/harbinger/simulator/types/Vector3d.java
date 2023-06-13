package dev.zontreck.harbinger.simulator.types;

import dev.zontreck.harbinger.utils.MathF;
import dev.zontreck.harbinger.utils.Matrix4;
import dev.zontreck.harbinger.utils.SimUtils;
import org.jetbrains.annotations.NotNull;

public class Vector3d implements Comparable {
	/// <summary>A vector with a value of 0,0,0</summary>
	public static final Vector3d Zero = new Vector3d ( );

	/// <summary>A vector with a value of 1,1,1</summary>
	public static final Vector3d One = new Vector3d ( 1.0f );

	/// <summary>A unit vector facing forward (X axis), value 1,0,0</summary>
	public static final Vector3d UnitX = new Vector3d ( 1.0f , 0.0f , 0.0f );

	/// <summary>A unit vector facing left (Y axis), value 0,1,0</summary>
	public static final Vector3d UnitY = new Vector3d ( 0.0f , 1.0f , 0.0f );

	/// <summary>A unit vector facing up (Z axis), value 0,0,1</summary>
	public static final Vector3d UnitZ = new Vector3d ( 0.0f , 0.0f , 1.0f );

	public static final Vector3d MinValue = new Vector3d ( Float.MIN_VALUE );
	public static final Vector3d MaxValue = new Vector3d ( Float.MAX_VALUE );

	/// <summary>x value</summary>
	public double X;

	/// <summary>Y value</summary>
	public double Y;

	/// <summary>Z value</summary>
	public double Z;

	public Vector3d ( ) {
		this.X = 0;
		this.Y = 0;
		this.Z = 0;
	}

	public Vector3d ( final double x , final double y , final double z ) {
		this.X = x;
		this.Y = y;
		this.Z = z;
	}

	public Vector3d ( final double value ) {
		this.X = value;
		this.Y = value;
		this.Z = value;
	}

	public Vector3d ( final Vector2 value , final double z ) {
		this.X = value.X;
		this.Y = value.Y;
		this.Z = z;
	}

	public Vector3d ( final Vector3d vector ) {
		this.X = vector.X;
		this.Y = vector.Y;
		this.Z = vector.Z;
	}

	public Vector3d ( final byte[] byteArray , final int pos ) {
		this.X = SimUtils.BytesToFloatSafepos ( byteArray , pos );
		this.Y = SimUtils.BytesToFloatSafepos ( byteArray , pos + 4 );
		this.Z = SimUtils.BytesToFloatSafepos ( byteArray , pos + 8 );
	}

	public static Vector3d Add ( final Vector3d value1 , final Vector3d value2 ) {
		return new Vector3d ( value1.X + value2.X , value1.Y + value2.Y , value1.Z + value2.Z );
	}

	public static Vector3d Abs ( final Vector3d value1 ) {
		return new Vector3d ( Math.abs ( value1.X ) , Math.abs ( value1.Y ) , Math.abs ( value1.Z ) );
	}

	public static Vector3d Clamp ( final Vector3d value1 , final double min , final double max ) {
		return new Vector3d (
				MathF.Clamp ( value1.X , min , max ) ,
				MathF.Clamp ( value1.Y , min , max ) ,
				MathF.Clamp ( value1.Z , min , max )
		);
	}

	public static Vector3d Clamp ( final Vector3d value1 , final Vector3d min , final Vector3d max ) {
		return new Vector3d (
				MathF.Clamp ( value1.X , min.X , max.X ) ,
				MathF.Clamp ( value1.Y , min.Y , max.Y ) ,
				MathF.Clamp ( value1.Z , min.Z , max.Z )
		);
	}

	public static Vector3d Cross ( final Vector3d value1 , final Vector3d value2 ) {
		return new Vector3d (
				value1.Y * value2.Z - value2.Y * value1.Z ,
				value1.Z * value2.X - value2.Z * value1.X ,
				value1.X * value2.Y - value2.X * value1.Y
		);
	}

	public static double Distance ( final Vector3d value1 , final Vector3d value2 ) {
		return Math.sqrt ( Vector3d.DistanceSquared ( value1 , value2 ) );
	}

	public static double DistanceSquared ( final Vector3d value1 , final Vector3d value2 ) {
		final var x = value1.X - value2.X;
		final var y = value1.Y - value2.Y;
		final var z = value1.Z - value2.Z;

		return x * x + y * y + z * z;
	}

	public static Vector3d Divide ( final Vector3d value1 , final Vector3d value2 ) {
		return new Vector3d ( value1.X / value2.X , value1.Y / value2.Y , value1.Z / value2.Z );
	}

	public static Vector3d Divide ( final Vector3d value1 , final double value2 ) {
		final var factor = 1.0f / value2;
		return new Vector3d ( value1.X * factor , value1.Y * factor , value1.Z * factor );
	}

	public static Vector3d Lerp ( final Vector3d value1 , final Vector3d value2 , final double amount ) {
		return new Vector3d (
				SimUtils.Lerp ( value1.X , value2.X , amount ) ,
				SimUtils.Lerp ( value1.Y , value2.Y , amount ) ,
				SimUtils.Lerp ( value1.Z , value2.Z , amount )
		);
	}

	public static double Mag ( final Vector3d value ) {
		return value.Length ( );
	}

	public static Vector3d Max ( final Vector3d value1 , final Vector3d value2 ) {
		return new Vector3d (
				Math.max ( value1.X , value2.X ) ,
				Math.max ( value1.Y , value2.Y ) ,
				Math.max ( value1.Z , value2.Z )
		);
	}

	public static Vector3d Min ( final Vector3d value1 , final Vector3d value2 ) {
		return new Vector3d (
				Math.min ( value1.X , value2.X ) ,
				Math.min ( value1.Y , value2.Y ) ,
				Math.min ( value1.Z , value2.Z )
		);
	}

	public static Vector3d Multiply ( final Vector3d value1 , final Vector3d value2 ) {
		final Vector3d M = new Vector3d ( value1 );
		M.Mul ( value2 );
		return M;
	}

	public static Vector3d Multiply ( final Vector3d value1 , final double scaleFactor ) {
		return Vector3d.Multiply ( value1 , new Vector3d ( scaleFactor ) );
	}

	public static Vector3d Negate ( final Vector3d value ) {
		return new Vector3d ( - value.X , - value.Y , - value.Z );
	}

	public static Vector3d Normalize ( final Vector3d value ) {
		var factor = value.LengthSquared ( );
		if ( 1.0e-6f < factor ) {
			factor = 1.0d / Math.sqrt ( factor );
			return Vector3d.Multiply ( value , factor );
		}

		return new Vector3d ( );
	}

	public static Vector3d SmoothStep ( final Vector3d value1 , final Vector3d value2 , final double amount ) {
		return new Vector3d (
				SimUtils.SmoothStep ( value1.X , value2.X , amount ) ,
				SimUtils.SmoothStep ( value1.Y , value2.Y , amount ) ,
				SimUtils.SmoothStep ( value1.Z , value2.Z , amount )
		);
	}

	public static Vector3d Subtract ( final Vector3d value1 , final Vector3d value2 ) {
		final Vector3d n = new Vector3d ( value1 );
		n.Sub ( value2 );
		return n;
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
	public static Vector3d Transform ( final Vector3d position , final Matrix4 matrix ) {
		return new Vector3d (
				position.X * matrix.M11 + position.Y * matrix.M21 + position.Z * matrix.M31 + matrix.M41 ,
				position.X * matrix.M12 + position.Y * matrix.M22 + position.Z * matrix.M32 + matrix.M42 ,
				position.X * matrix.M13 + position.Y * matrix.M23 + position.Z * matrix.M33 + matrix.M43
		);
	}

	public static Vector3d TransformNormal ( final Vector3d position , final Matrix4 matrix ) {
		return new Vector3d (
				position.X * matrix.M11 + position.Y * matrix.M21 + position.Z * matrix.M31 ,
				position.X * matrix.M12 + position.Y * matrix.M22 + position.Z * matrix.M32 ,
				position.X * matrix.M13 + position.Y * matrix.M23 + position.Z * matrix.M33
		);
	}

	public static Vector3d UnitXRotated ( final Quaternion rot ) {
		final var y2 = rot.Y + rot.Y;
		final var z2 = rot.Z + rot.Z;

		final var wy2 = rot.W * y2;
		final var wz2 = rot.W * z2;
		final var xy2 = rot.X * y2;
		final var xz2 = rot.X * z2;
		final var yy2 = rot.Y * y2;
		final var zz2 = rot.Z * z2;

		return new Vector3d ( 1.0f - yy2 - zz2 , xy2 + wz2 , xz2 - wy2 );
	}

	public static Vector3d UnitYRotated ( final Quaternion rot ) {
		final var x2 = rot.X + rot.X;
		final var y2 = rot.Y + rot.Y;
		final var z2 = rot.Z + rot.Z;

		final var wx2 = rot.W * x2;
		final var wz2 = rot.W * z2;
		final var xx2 = rot.X * x2;
		final var xy2 = rot.X * y2;
		final var yz2 = rot.Y * z2;
		final var zz2 = rot.Z * z2;

		return new Vector3d ( xy2 - wz2 , 1.0f - xx2 - zz2 , yz2 + wx2 );
	}

	public static Vector3d UnitZRotated ( final Quaternion rot ) {
		final var x2 = rot.X + rot.X;
		final var y2 = rot.Y + rot.Y;
		final var z2 = rot.Z + rot.Z;

		final var wx2 = rot.W * x2;
		final var wy2 = rot.W * y2;
		final var xx2 = rot.X * x2;
		final var xz2 = rot.X * z2;
		final var yy2 = rot.Y * y2;
		final var yz2 = rot.Y * z2;

		return new Vector3d ( xz2 + wy2 , yz2 - wx2 , 1.0f - xx2 - yy2 );
	}

	//quaternion must be normalized <0,0,z,w>
	public static Vector3d RotateByQZ ( final Vector3d vec , final Quaternion rot ) {
		final var z2 = rot.Z + rot.Z;
		final var wz2 = rot.W * z2;
		final var zz2 = 1.0f - rot.Z * z2;

		return new Vector3d (
				vec.X * zz2 - vec.Y * wz2 ,
				vec.X * wz2 + vec.Y * zz2 ,
				vec.Z
		);

	}

	//quaternion must be normalized <0,0,z,w>
	public static Vector3d InverseRotateByQZ ( final Vector3d vec , final Quaternion rot ) {
		final var z2 = rot.Z + rot.Z;
		final var wz2 = rot.W * z2;
		final var zz2 = 1.0f - rot.Z * z2;

		return new Vector3d (
				vec.X * zz2 + vec.Y * wz2 ,
				vec.Y * zz2 - vec.X * wz2 ,
				vec.Z
		);
	}

	//shortQuaternion must be normalized <z,w>
	public static Vector3d RotateByShortQZ ( final Vector3d vec , final Vector2 shortQuaternion ) {
		final var z2 = shortQuaternion.X + shortQuaternion.X;
		final var zz2 = 1.0f - shortQuaternion.X * z2;
		final var wz2 = shortQuaternion.Y * z2;

		return new Vector3d (
				vec.X * zz2 - vec.Y * wz2 ,
				vec.X * wz2 + vec.Y * zz2 ,
				vec.Z
		);

	}

	//shortQuaternion must be normalized <z,w>
	public static Vector3d InverseRotateByShortQZ ( final Vector3d vec , final Vector2 shortQuaternion ) {
		final var z2 = shortQuaternion.X + shortQuaternion.X;
		final var zz2 = 1.0f - shortQuaternion.X * z2;
		final var wz2 = shortQuaternion.Y * z2;

		return new Vector3d (
				vec.X * zz2 + vec.Y * wz2 ,
				vec.Y * zz2 - vec.X * wz2 ,
				vec.Z
		);
	}

	public void Abs ( ) {
		if ( 0 > X )
			this.X = - this.X;
		if ( 0 > Y )
			this.Y = - this.Y;
		if ( 0 > Z )
			this.Z = - this.Z;
	}

	public void Min ( final Vector3d v ) {
		if ( v.X < this.X ) this.X = v.X;
		if ( v.Y < this.Y ) this.Y = v.Y;
		if ( v.Z < this.Z ) this.Z = v.Z;
	}

	public void Max ( final Vector3d v ) {
		if ( v.X > this.X ) this.X = v.X;
		if ( v.Y > this.Y ) this.Y = v.Y;
		if ( v.Z > this.Z ) this.Z = v.Z;
	}

	public void Add ( final Vector3d v ) {
		this.X += v.X;
		this.Y += v.Y;
		this.Z += v.Z;
	}

	public void Sub ( final Vector3d v ) {
		this.X -= v.X;
		this.Y -= v.Y;
		this.Z -= v.Z;
	}

	public void Clamp ( final double min , final double max ) {
		if ( this.X > max )
			this.X = max;
		else if ( this.X < min )
			this.X = min;

		if ( this.Y > max )
			this.Y = max;
		else if ( this.Y < min )
			this.Y = min;

		if ( this.Z > max )
			this.Z = max;
		else if ( this.Z < min )
			this.Z = min;
	}

	public double Length ( ) {
		return Math.sqrt ( this.X * this.X + this.Y * this.Y + this.Z * this.Z );
	}

	public double LengthSquared ( ) {
		return this.X * this.X + this.Y * this.Y + this.Z * this.Z;
	}

	public void Normalize ( ) {
		var factor = this.X * this.X + this.Y * this.Y + this.Z * this.Z;
		if ( 1.0e-6f < factor ) {
			factor = 1.0f / Math.sqrt ( factor );
			this.X *= factor;
			this.Y *= factor;
			this.Z *= factor;
		}
		else {
			this.X = 0;
			this.Y = 0;
			this.Z = 0;
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
	public boolean ApproxEquals ( final Vector3d vec , final double tolerance ) {
		final var diff = Vector3d.Subtract ( this , vec );
		return diff.LengthSquared ( ) <= tolerance * tolerance;
	}

	/// <summary>
	///     Test if this vector is composed of all finite numbers
	/// </summary>
	public boolean IsFinite ( ) {
		return SimUtils.IsFinite ( this.X ) && SimUtils.IsFinite ( this.Y ) && SimUtils.IsFinite ( this.Z );
	}

	/// <summary>
	///     Returns the raw bytes for this vector
	/// </summary>
	/// <returns>A 12 byte array containing X, Y, and Z</returns>
	public byte[] GetBytes ( ) {
		final var dest = new byte[ 12 ];

		// TODO

		return dest;
	}

	//quaternion must be normalized <0,0,z,w>
	public void RotateByQZ ( final Quaternion rot ) {
		final var z2 = rot.Z + rot.Z;
		final var zz2 = 1.0f - rot.Z * z2;
		final var wz2 = rot.W * z2;

		final var ox = this.X;
		final var oy = this.Y;

		this.X = ox * zz2 - oy * wz2;
		this.Y = ox * wz2 + oy * zz2;
	}

	//quaternion must be normalized <0,0,z,w>
	public void InverseRotateByQZ ( final Quaternion rot ) {
		final var z2 = rot.Z + rot.Z;
		final var zz2 = 1.0f - rot.Z * z2;
		final var wz2 = rot.W * z2;

		final var ox = this.X;
		final var oy = this.Y;

		this.X = ox * zz2 + oy * wz2;
		this.Y = oy * zz2 - ox * wz2;
	}

	//shortQuaternion must be normalized <z,w>
	public void RotateByShortQZ ( final Vector2 shortQuaternion ) {
		final var z2 = shortQuaternion.X + shortQuaternion.X;
		final var zz2 = 1.0f - shortQuaternion.X * z2;
		final var wz2 = shortQuaternion.Y * z2;

		final var ox = this.X;
		final var oy = this.Y;

		this.X = ox * zz2 - oy * wz2;
		this.Y = ox * wz2 + oy * zz2;
	}

	//quaternion must be normalized <0,0,z,w>
	public void InverseRotateByShortQZ ( final Vector2 shortQuaternion ) {
		final var z2 = shortQuaternion.X + shortQuaternion.X;
		final var zz2 = 1.0f - shortQuaternion.X * z2;
		final var wz2 = shortQuaternion.Y * z2;

		final var ox = this.X;
		final var oy = this.Y;

		this.X = ox * zz2 + oy * wz2;
		this.Y = oy * zz2 - ox * wz2;
	}

	public void Mul ( final Vector3d other ) {
		this.X *= other.X;
		this.Y *= other.Y;
		this.Z *= other.Z;
	}

	@Override
	public int compareTo ( @NotNull final Object o ) {
		return 0;
	}
}
