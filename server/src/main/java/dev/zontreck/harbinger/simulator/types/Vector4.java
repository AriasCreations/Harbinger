package dev.zontreck.harbinger.simulator.types;

import dev.zontreck.harbinger.utils.MathF;
import dev.zontreck.harbinger.utils.Matrix4;
import dev.zontreck.harbinger.utils.SimUtils;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class Vector4 implements Comparable<Vector4> {
	/// <summary>A vector with a value of 0,0,0,0</summary>
	public static final Vector4 Zero = new Vector4 ( );
	/// <summary>A vector with a value of 1,1,1,1</summary>
	public static final Vector4 One = new Vector4 ( 1.0f , 1.0f , 1.0f , 1.0f );
	/// <summary>A vector with a value of 1,0,0,0</summary>
	public static final Vector4 UnitX = new Vector4 ( 1.0f , 0.0f , 0.0f , 0.0f );
	/// <summary>A vector with a value of 0,1,0,0</summary>
	public static final Vector4 UnitY = new Vector4 ( 0.0f , 1.0f , 0.0f , 0.0f );
	/// <summary>A vector with a value of 0,0,1,0</summary>
	public static final Vector4 UnitZ = new Vector4 ( 0.0f , 0.0f , 1.0f , 0.0f );
	/// <summary>A vector with a value of 0,0,0,1</summary>
	public static final Vector4 UnitW = new Vector4 ( 0.0f , 0.0f , 0.0f , 1.0f );
	public static final Vector4 MinValue = new Vector4 ( Float.MIN_VALUE );
	public static final Vector4 MaxValue = new Vector4 ( Float.MAX_VALUE );
	/// <summary>X value</summary>
	public float X;
	/// <summary>Y value</summary>
	public float Y;
	/// <summary>Z value</summary>
	public float Z;
	/// <summary>W value</summary>
	public float W;

	public Vector4 ( final float x , final float y , final float z , final float w ) {
		this.X = x;
		this.Y = y;
		this.Z = z;
		this.W = w;
	}

	public Vector4 ( final Vector2 value , final float z , final float w ) {
		this.X = value.X;
		this.Y = value.Y;
		this.Z = z;
		this.W = w;
	}

	public Vector4 ( final Vector3 value , final float w ) {
		this.X = value.X;
		this.Y = value.Y;
		this.Z = value.Z;
		this.W = w;
	}

	public Vector4 ( final float value ) {
		this.X = value;
		this.Y = value;
		this.Z = value;
		this.W = value;
	}

	public Vector4 ( ) {
		this.initV4 ( );
	}

	/// <summary>
///     Constructor, builds a vector from a byte array
/// </summary>
/// <param name="byteArray">Byte array containing four four-byte floats</param>
/// <param name="pos">Beginning position in the byte array</param>
	public Vector4 ( final byte[] byteArray , final int pos ) {
		this.X = SimUtils.BytesToFloatSafepos ( byteArray , pos );
		this.Y = SimUtils.BytesToFloatSafepos ( byteArray , pos + 4 );
		this.Z = SimUtils.BytesToFloatSafepos ( byteArray , pos + 8 );
		this.W = SimUtils.BytesToFloatSafepos ( byteArray , pos + 12 );
	}

	public Vector4 ( final Vector4 value ) {
		this.X = value.X;
		this.Y = value.Y;
		this.Z = value.Z;
		this.W = value.W;
	}

	public static Vector4 Add ( final Vector4 value1 , final Vector4 value2 ) {
		return new Vector4 (
				value1.W + value2.W ,
				value1.X + value2.X ,
				value1.Y + value2.Y ,
				value1.Z + value2.Z
		);
	}

	public static Vector4 Clamp ( final Vector4 value1 , final float min , final float max ) {
		return new Vector4 (
				MathF.Clamp ( value1.X , min , max ) ,
				MathF.Clamp ( value1.Y , min , max ) ,
				MathF.Clamp ( value1.Z , min , max ) ,
				MathF.Clamp ( value1.W , min , max )
		);
	}

	public static Vector4 Clamp ( final Vector4 value1 , final Vector4 min , final Vector4 max ) {
		return new Vector4 (
				MathF.Clamp ( value1.X , min.X , max.X ) ,
				MathF.Clamp ( value1.Y , min.Y , max.Y ) ,
				MathF.Clamp ( value1.Z , min.Z , max.Z ) ,
				MathF.Clamp ( value1.W , min.W , max.W )
		);
	}

	public static float Distance ( final Vector4 value1 , final Vector4 value2 ) {
		return MathF.Sqrt ( Vector4.DistanceSquared ( value1 , value2 ) );
	}

	public static float DistanceSquared ( final Vector4 value1 , final Vector4 value2 ) {
		return
				( value1.X - value2.X ) * ( value1.X - value2.X ) +
						( value1.Y - value2.Y ) * ( value1.Y - value2.Y ) +
						( value1.Z - value2.Z ) * ( value1.Z - value2.Z ) +
						( value1.W - value2.W ) * ( value1.W - value2.W );
	}

	public static Vector4 Divide ( final Vector4 value1 , final Vector4 value2 ) {
		return new Vector4 (
				value1.X / value2.X ,
				value1.Y / value2.Y ,
				value1.Z / value2.Z ,
				value1.W / value2.W
		);
	}

	public static Vector4 Divide ( final Vector4 value1 , final float divider ) {
		final var factor = 1.0f / divider;
		return new Vector4 (
				value1.X * factor ,
				value1.Y * factor ,
				value1.Z * factor ,
				value1.W * factor
		);
	}

	public static float Dot ( final Vector4 vector1 , final Vector4 vector2 ) {
		return vector1.X * vector2.X + vector1.Y * vector2.Y + vector1.Z * vector2.Z + vector1.W * vector2.W;
	}

	public static Vector4 Lerp ( final Vector4 value1 , final Vector4 value2 , final float amount ) {
		return new Vector4 (
				SimUtils.Lerp ( value1.X , value2.X , amount ) ,
				SimUtils.Lerp ( value1.Y , value2.Y , amount ) ,
				SimUtils.Lerp ( value1.Z , value2.Z , amount ) ,
				SimUtils.Lerp ( value1.W , value2.W , amount )
		);
	}

	public static Vector4 Max ( final Vector4 value1 , final Vector4 value2 ) {
		return new Vector4 (
				MathF.Max ( value1.X , value2.X ) ,
				MathF.Max ( value1.Y , value2.Y ) ,
				MathF.Max ( value1.Z , value2.Z ) ,
				MathF.Max ( value1.W , value2.W )
		);
	}

	public static Vector4 Min ( final Vector4 value1 , final Vector4 value2 ) {
		return new Vector4 (
				MathF.Min ( value1.X , value2.X ) ,
				MathF.Min ( value1.Y , value2.Y ) ,
				MathF.Min ( value1.Z , value2.Z ) ,
				MathF.Min ( value1.W , value2.W )
		);
	}

	public static Vector4 Multiply ( final Vector4 value1 , final Vector4 value2 ) {
		return new Vector4 (
				value1.X * value2.X ,
				value1.Y * value2.Y ,
				value1.Z * value2.Z ,
				value1.W * value2.W
		);
	}

	public static Vector4 Multiply ( final Vector4 value1 , final float scaleFactor ) {
		return new Vector4 (
				value1.X * scaleFactor ,
				value1.Y * scaleFactor ,
				value1.Z * scaleFactor ,
				value1.W * scaleFactor
		);
	}

	public static Vector4 Negate ( final Vector4 value ) {
		return new Vector4 (
				- value.X ,
				- value.Y ,
				- value.Z ,
				- value.W
		);
	}

	public static Vector4 Normalize ( final Vector4 vector ) {
		var factor = vector.LengthSquared ( );
		if ( 1.0e-6 < factor ) {
			factor = 1.0f / MathF.Sqrt ( factor );
			return new Vector4 (
					vector.X * factor ,
					vector.Y * factor ,
					vector.Z * factor ,
					vector.W * factor
			);
		}

		return Vector4.Zero;
	}

	public static Vector4 SmoothStep ( final Vector4 value1 , final Vector4 value2 , final float amount ) {
		return new Vector4 (
				SimUtils.SmoothStep ( value1.X , value2.X , amount ) ,
				SimUtils.SmoothStep ( value1.Y , value2.Y , amount ) ,
				SimUtils.SmoothStep ( value1.Z , value2.Z , amount ) ,
				SimUtils.SmoothStep ( value1.W , value2.W , amount )
		);
	}

	public static Vector4 Subtract ( final Vector4 value1 , final Vector4 value2 ) {
		return new Vector4 (
				value1.W - value2.W ,
				value1.X - value2.X ,
				value1.Y - value2.Y ,
				value1.Z - value2.Z
		);
	}

	public static Vector4 Transform ( final Vector2 position , final Matrix4 matrix ) {
		return new Vector4 (
				position.X * matrix.M11 + position.Y * matrix.M21 + matrix.M41 ,
				position.X * matrix.M12 + position.Y * matrix.M22 + matrix.M42 ,
				position.X * matrix.M13 + position.Y * matrix.M23 + matrix.M43 ,
				position.X * matrix.M14 + position.Y * matrix.M24 + matrix.M44
		);
	}

	public static Vector4 Transform ( final Vector3 position , final Matrix4 matrix ) {
		return new Vector4 (
				position.X * matrix.M11 + position.Y * matrix.M21 + position.Z * matrix.M31 + matrix.M41 ,
				position.X * matrix.M12 + position.Y * matrix.M22 + position.Z * matrix.M32 + matrix.M42 ,
				position.X * matrix.M13 + position.Y * matrix.M23 + position.Z * matrix.M33 + matrix.M43 ,
				position.X * matrix.M14 + position.Y * matrix.M24 + position.Z * matrix.M34 + matrix.M44
		);
	}

	public static Vector4 Transform ( final Vector4 vector , final Matrix4 matrix ) {
		return new Vector4 (
				vector.X * matrix.M11 + vector.Y * matrix.M21 + vector.Z * matrix.M31 + vector.W * matrix.M41 ,
				vector.X * matrix.M12 + vector.Y * matrix.M22 + vector.Z * matrix.M32 + vector.W * matrix.M42 ,
				vector.X * matrix.M13 + vector.Y * matrix.M23 + vector.Z * matrix.M33 + vector.W * matrix.M43 ,
				vector.X * matrix.M14 + vector.Y * matrix.M24 + vector.Z * matrix.M34 + vector.W * matrix.M44
		);
	}

	private void initV4 ( ) {

		this.X = 0;
		this.Y = 0;
		this.Z = 0;
		this.W = 0;
	}

	public void Abs ( ) {
		this.X = MathF.Abs ( this.X );
		this.Y = MathF.Abs ( this.Y );
		this.Z = MathF.Abs ( this.Z );
		this.W = MathF.Abs ( this.W );
	}

	public void Add ( final Vector4 v ) {
		this.X += v.X;
		this.Y += v.Y;
		this.Z += v.Z;
		this.W += v.W;
	}

	public void Sub ( final Vector4 v ) {
		this.X -= v.X;
		this.Y -= v.Y;
		this.Z -= v.Z;
		this.W -= v.W;
	}

	public void Clamp ( final float min , final float max ) {
		if ( this.X < min ) this.X = min;
		else if ( this.X > max ) this.X = max;

		if ( this.Y < min ) this.Y = min;
		else if ( this.Y > max ) this.Y = max;

		if ( this.Z < min ) this.Z = min;
		else if ( this.Z > max ) this.Z = max;

		if ( this.W < min ) this.W = min;
		else if ( this.W > max ) this.W = max;
	}

	public void Min ( final Vector4 v ) {
		if ( v.X < this.X ) this.X = v.X;
		if ( v.Y < this.Y ) this.Y = v.Y;
		if ( v.Z < this.Z ) this.Z = v.Z;
		if ( v.W < this.W ) this.W = v.W;
	}

	public void Max ( final Vector4 v ) {
		if ( v.X > this.X ) this.X = v.X;
		if ( v.Y > this.Y ) this.Y = v.Y;
		if ( v.Z > this.Z ) this.Z = v.Z;
		if ( v.W > this.W ) this.W = v.W;
	}

	public float Length ( ) {
		return MathF.Sqrt ( this.X * this.X + this.Y * this.Y + this.Z * this.Z + this.W * this.W );
	}

	public float LengthSquared ( ) {
		return this.X * this.X + this.Y * this.Y + this.Z * this.Z + this.W * this.W;
	}

	public void Normalize ( ) {
		var factor = this.LengthSquared ( );
		if ( 1.0e-6 < factor ) {
			factor = 1.0f / MathF.Sqrt ( factor );
			this.X *= factor;
			this.Y *= factor;
			this.Z *= factor;
			this.W *= factor;
		}
		else {
			this.initV4 ( );
		}
	}

	public boolean ApproxEquals ( final Vector4 vec , final float tolerance ) {
		return SimUtils.ApproxEqual ( this.X , vec.X , tolerance ) &&
				SimUtils.ApproxEqual ( this.Y , vec.Y , tolerance ) &&
				SimUtils.ApproxEqual ( this.Z , vec.Z , tolerance ) &&
				SimUtils.ApproxEqual ( this.W , vec.W , tolerance );
	}

	public boolean ApproxEquals ( final Vector4 vec ) {
		return SimUtils.ApproxEqual ( this.X , vec.X ) &&
				SimUtils.ApproxEqual ( this.Y , vec.Y ) &&
				SimUtils.ApproxEqual ( this.Z , vec.Z ) &&
				SimUtils.ApproxEqual ( this.W , vec.W );
	}

	public boolean IsZero ( ) {
		if ( 0 != X )
			return false;
		if ( 0 != Y )
			return false;
		if ( 0 != Z )
			return false;
		return 0 == W;
	}

	public boolean IsNotZero ( ) {
		if ( 0 != X )
			return true;
		if ( 0 != Y )
			return true;
		if ( 0 != Z )
			return true;
		return 0 != W;
	}

	public float Dot ( final Vector4 value2 ) {
		return this.X * value2.X + this.Y * value2.Y + this.Z * value2.Z + this.W * value2.W;
	}

	/// <summary>
///     Test if this vector is composed of all finite numbers
/// </summary>
	public boolean IsFinite ( ) {
		return SimUtils.IsFinite ( this.X ) && SimUtils.IsFinite ( this.Y ) && SimUtils.IsFinite ( this.Z ) && SimUtils.IsFinite ( this.W );
	}

	/// <summary>
///     Builds a vector from a byte array
/// </summary>
/// <param name="byteArray">Byte array containing a 16 byte vector</param>
/// <param name="pos">Beginning position in the byte array</param>
	public void FromBytes ( final byte[] byteArray , final int pos ) {
		this.X = SimUtils.BytesToFloatSafepos ( byteArray , pos );
		this.Y = SimUtils.BytesToFloatSafepos ( byteArray , pos + 4 );
		this.Z = SimUtils.BytesToFloatSafepos ( byteArray , pos + 8 );
		this.W = SimUtils.BytesToFloatSafepos ( byteArray , pos + 12 );
	}

	/// <summary>
///     Returns the raw bytes for this vector
/// </summary>
/// <returns>A 16 byte array containing X, Y, Z, and W</returns>
	public byte[] GetBytes ( ) {
		final ByteArrayOutputStream baos = new ByteArrayOutputStream ( );
		try {
			baos.write ( SimUtils.FloatToBytesSafepos ( this.X ) );
			baos.write ( SimUtils.FloatToBytesSafepos ( this.Y ) );
			baos.write ( SimUtils.FloatToBytesSafepos ( this.Z ) );
			baos.write ( SimUtils.FloatToBytesSafepos ( this.W ) );
		} catch ( final IOException e ) {
			throw new RuntimeException ( e );
		}
		return baos.toByteArray ( );
	}

	public boolean Equals ( final Object obj ) {
		if ( ! ( obj instanceof final Vector4 other ) )
			return false;

		if ( this.X != other.X )
			return false;
		if ( this.Y != other.Y )
			return false;
		if ( this.Z != other.Z )
			return false;
		return this.W == other.W;
	}

	public boolean Equals ( final Vector4 other ) {
		if ( this.X != other.X )
			return false;
		if ( this.Y != other.Y )
			return false;
		if ( this.Z != other.Z )
			return false;
		return this.W == other.W;
	}

	public boolean NotEqual ( final Vector4 other ) {
		if ( this.X != other.X )
			return true;
		if ( this.Y != other.Y )
			return true;
		if ( this.Z != other.Z )
			return true;
		return this.W != other.W;
	}

	@Override
	public String toString ( ) {
		final String sb = "<" +
				this.X +
				", " +
				this.Y +
				", " +
				this.Z +
				", " +
				this.W +
				'>';
		return sb;
	}

	@Override
	public int compareTo ( @NotNull final Vector4 vector4 ) {

		if ( vector4.X > this.X ) return 1;
		else if ( vector4.X < this.X ) return - 1;
		if ( vector4.Y > this.Y ) return 1;
		else if ( vector4.Y < this.Y ) return - 1;
		if ( vector4.Z > this.Z ) return 1;
		else if ( vector4.Z < this.Z ) return - 1;
		if ( vector4.W > this.W ) return 1;
		else if ( vector4.W < this.W ) return - 1;


		return 0;

	}
}
