package dev.zontreck.harbinger.simulator.types;

import dev.zontreck.harbinger.utils.MathF;
import dev.zontreck.harbinger.utils.SimUtils;

public class Vector2 {
	/// <summary>A vector with a value of 0,0</summary>
	public static final Vector2 Zero = new Vector2 ( );
	/// <summary>A vector with a value of 1,1</summary>
	public static final Vector2 One = new Vector2 ( 1.0f , 1.0f );
	/// <summary>A vector with a value of 1,0</summary>
	public static final Vector2 UnitX = new Vector2 ( 1.0f , 0.0f );
	/// <summary>A vector with a value of 0,1</summary>
	public static final Vector2 UnitY = new Vector2 ( 0.0f , 1.0f );
	public static final Vector2 MinValue = new Vector2 ( Float.MIN_VALUE , Float.MIN_VALUE );
	public static final Vector2 MaxValue = new Vector2 ( Float.MAX_VALUE , Float.MAX_VALUE );
	public float X;
	public float Y;

	public Vector2 ( ) {
		this.X = 0;
		this.Y = 0;
	}

	public Vector2 ( final float x , final float y ) {
		this.X = x;
		this.Y = y;
	}

	public Vector2 ( final float val ) {
		this.X = val;
		this.Y = val;
	}

	public Vector2 ( final Vector2 other ) {
		this.X = other.X;
		this.Y = other.Y;
	}

	public static Vector2 Add ( final Vector2 value1 , final Vector2 value2 ) {
		return new Vector2 ( value1.X + value2.X , value1.Y + value2.Y );
	}

	public static Vector2 Clamp ( final Vector2 value1 , final float min , final float max ) {
		return new Vector2 (
				MathF.Clamp ( value1.X , min , max ) ,
				MathF.Clamp ( value1.Y , min , max )
		);
	}

	public static Vector2 Clamp ( final Vector2 value1 , final Vector2 min , final Vector2 max ) {
		return new Vector2 (
				MathF.Clamp ( value1.X , min.X , max.X ) ,
				MathF.Clamp ( value1.Y , min.Y , max.Y )
		);
	}

	public static float Distance ( final Vector2 value1 , final Vector2 value2 ) {
		return MathF.Sqrt ( Vector2.DistanceSquared ( value1 , value2 ) );
	}

	public static float DistanceSquared ( final Vector2 value1 , final Vector2 value2 ) {
		return
				( value1.X - value2.X ) * ( value1.X - value2.X ) +
						( value1.Y - value2.Y ) * ( value1.Y - value2.Y );
	}

	public static Vector2 Divide ( final Vector2 value1 , final Vector2 value2 ) {
		return new Vector2 ( value1.X / value2.X , value1.Y / value2.Y );
	}

	public static Vector2 Divide ( final Vector2 value1 , final float divider ) {
		final var factor = 1 / divider;
		return new Vector2 ( value1.X * factor , value1.Y * factor );
	}

	public static float Dot ( final Vector2 value1 , final Vector2 value2 ) {
		return value1.X * value2.X + value1.Y * value2.Y;
	}

	public static Vector2 Lerp ( final Vector2 value1 , final Vector2 value2 , final float amount ) {
		return new Vector2 (
				SimUtils.Lerp ( value1.X , value2.X , amount ) ,
				SimUtils.Lerp ( value1.Y , value2.Y , amount )
		);
	}

	public static Vector2 Max ( final Vector2 value1 , final Vector2 value2 ) {
		return new Vector2 (
				MathF.Max ( value1.X , value2.X ) ,
				MathF.Max ( value1.Y , value2.Y )
		);
	}

	public static Vector2 Min ( final Vector2 value1 , final Vector2 value2 ) {
		return new Vector2 (
				MathF.Min ( value1.X , value2.X ) ,
				MathF.Min ( value1.Y , value2.Y )
		);
	}

	public static Vector2 Multiply ( final Vector2 value1 , final Vector2 value2 ) {
		return new Vector2 ( value1.X * value2.X , value1.Y * value2.Y );
	}

	public static Vector2 Multiply ( final Vector2 value1 , final float scaleFactor ) {
		return new Vector2 ( value1.X * scaleFactor , value1.Y * scaleFactor );
	}

	public static Vector2 Negate ( final Vector2 value ) {
		return new Vector2 ( - value.X , - value.Y );
	}

	public static Vector2 Normalize ( final Vector2 value ) {
		var factor = value.LengthSquared ( );
		if ( 1.0e-6 < factor ) {
			factor = 1.0f / MathF.Sqrt ( factor );
			return new Vector2 ( value.X * factor , value.Y * factor );
		}

		return new Vector2 ( );
	}

	/// <summary>
	///     Interpolates between two vectors using a cubic equation
	/// </summary>
	public static Vector2 SmoothStep ( final Vector2 value1 , final Vector2 value2 , final float amount ) {
		return new Vector2 (
				SimUtils.SmoothStep ( value1.X , value2.X , amount ) ,
				SimUtils.SmoothStep ( value1.Y , value2.Y , amount )
		);
	}

	public static Vector2 Subtract ( final Vector2 value1 , final Vector2 value2 ) {
		return new Vector2 ( value1.X - value2.X , value1.Y - value2.Y );
	}

	public void Abs ( ) {
		if ( 0 > X ) this.X = - this.X;
		if ( 0 > Y ) this.Y = - this.Y;
	}

	public void Min ( final Vector2 v ) {
		if ( v.X < this.X ) this.X = v.X;
		if ( v.Y < this.Y ) this.Y = v.Y;
	}

	public void Max ( final Vector2 v ) {
		if ( v.X > this.X ) this.X = v.X;
		if ( v.Y > this.Y ) this.Y = v.Y;
	}

	public void Add ( final Vector2 v ) {
		this.X += v.X;
		this.Y += v.Y;
	}

	public void Sub ( final Vector2 v ) {
		this.X -= v.X;
		this.Y -= v.Y;
	}

	public boolean ApproxEquals ( final Vector2 vec , final float tolerance ) {
		return SimUtils.ApproxEqual ( this.X , vec.X , tolerance ) &&
				SimUtils.ApproxEqual ( this.Y , vec.Y , tolerance );
	}

	public boolean ApproxEquals ( final Vector2 vec ) {
		return SimUtils.ApproxEqual ( this.X , vec.X ) &&
				SimUtils.ApproxEqual ( this.Y , vec.Y );
	}

	public boolean ApproxZero ( ) {
		if ( ! SimUtils.ApproxZero ( this.X ) )
			return false;
		return SimUtils.ApproxZero ( this.Y );
	}

	public boolean ApproxZero ( final float tolerance ) {
		if ( ! SimUtils.ApproxZero ( this.X , tolerance ) )
			return false;
		return SimUtils.ApproxZero ( this.Y , tolerance );
	}

	public boolean IsZero ( ) {
		if ( 0 != X )
			return false;
		return 0 == Y;
	}

	public boolean IsNotZero ( ) {
		return ! this.IsZero ( );
	}

	public boolean IsFinite ( ) {
		return SimUtils.IsFinite ( this.X ) && SimUtils.IsFinite ( this.Y );
	}

	public float Length ( ) {
		return MathF.Sqrt ( this.X * this.X + this.Y * this.Y );
	}

	public float LengthSquared ( ) {
		return this.X * this.X + this.Y * this.Y;
	}

	public void Normalize ( ) {
		var factor = this.LengthSquared ( );
		if ( 1.0e-6 < factor ) {
			factor = 1.0f / MathF.Sqrt ( factor );
			this.X *= factor;
			this.Y *= factor;
		}
		else {
			this.X = 0.0f;
			this.Y = 0.0f;
		}
	}

}
