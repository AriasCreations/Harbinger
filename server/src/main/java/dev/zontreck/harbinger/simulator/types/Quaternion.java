package dev.zontreck.harbinger.simulator.types;

import dev.zontreck.harbinger.utils.MathF;
import dev.zontreck.harbinger.utils.SimUtils;

public class Quaternion {
	public static final Quaternion Identity = new Quaternion ( 0.0f , 0.0f , 0.0f , 1.0f );
	/// <summary>X value</summary>
	public float X;

	/// <summary>Y value</summary>
	public float Y;

	/// <summary>Z value</summary>
	public float Z;

	/// <summary>W value</summary>
	public float W;

	public Quaternion ( ) {

	}

	public Quaternion ( final float x , final float y , final float z , final float w ) {
		this.X = x;
		this.Y = y;
		this.Z = z;
		this.W = w;
	}

	public Quaternion ( final Vector3 vectorPart , final float scalarPart ) {
		this.X = vectorPart.X;
		this.Y = vectorPart.Y;
		this.Z = vectorPart.Z;
		this.W = scalarPart;
	}

	public Quaternion ( final float x , final float y , final float z ) {
		this.X = x;
		this.Y = y;
		this.Z = z;

		final var xyzsum = 1.0f - this.X * this.X - this.Y * this.Y - this.Z * this.Z;
		this.W = 1.0e-6f < xyzsum ? MathF.Sqrt ( xyzsum ) : 0;
	}

	public Quaternion ( final byte[] byteArray , final int pos , final boolean normalized ) {
		this.X = this.Y = this.Z = 0;
		this.W = 1;
		this.FromBytes ( byteArray , pos , normalized );
	}

	public Quaternion ( final Quaternion q ) {
		W = q.W;
		X = q.X;
		Z = q.Z;
		Y = q.Y;
	}

	public Quaternion ( final MainAxis BaseAxis , final float angle ) {
		switch ( BaseAxis ) {
			case X:
				this.W = MathF.Cos ( 0.5f * angle );
				this.X = MathF.Sqrt ( 1.0f - this.W * this.W );
				this.Y = 0;
				this.Z = 0;
				break;
			case Y:
				this.W = MathF.Cos ( 0.5f * angle );
				this.Y = MathF.Sqrt ( 1.0f - this.W * this.W );
				this.X = 0;
				this.Z = 0;
				break;
			case Z:
				this.W = MathF.Cos ( 0.5f * angle );
				this.Z = MathF.Sqrt ( 1.0f - this.W * this.W );
				this.X = 0;
				this.Y = 0;
				break;
			default: //error
				this.X = 0;
				this.Y = 0;
				this.Z = 0;
				this.W = 1;
				break;
		}
	}

	public final boolean ApproxEquals ( final Quaternion other ) {
		return this.ApproxEquals ( other , 1.0e-6f );
	}

	public final boolean ApproxEquals ( final Quaternion other , final float tolerance ) {
		return MathF.Abs ( this.W - other.W ) < tolerance &&
				MathF.Abs ( this.Z - other.Z ) < tolerance &&
				MathF.Abs ( this.X - other.X ) < tolerance;

	}

	public final boolean IsIdentity ( ) {
		return 1.0f - 1.0e-6f < MathF.Abs ( W );
	}

	public final boolean IsIdentityOrZero ( ) {
		if ( 0 != X ) return false;
		if ( 0 != Y ) return false;
		return 0 == Z;
	}

	@Override
	public String toString ( ) {
		return String.format ( "<%f, %f, %f, %f>" , this.X , this.Y , this.Z , this.W );
	}

	public final boolean Equals ( final Quaternion other ) {
		if ( this.X != other.X )
			return false;
		if ( this.Y != other.Y )
			return false;
		if ( this.Z != other.Z )
			return false;
		return this.W == other.W;
	}

	public final boolean NotEqual ( final Quaternion other ) {
		return ! this.Equals ( other );
	}

	public final boolean Equals ( final Object obj ) {
		if ( obj instanceof Quaternion )
			return this.Equals ( ( Quaternion ) obj );
		return false;
	}

	public void FromBytes ( final byte[] byteArray , final int pos , final boolean normalized ) {
		this.X = SimUtils.BytesToFloatSafepos ( byteArray , pos );
		this.Y = SimUtils.BytesToFloatSafepos ( byteArray , pos + 4 );
		this.Z = SimUtils.BytesToFloatSafepos ( byteArray , pos + 8 );
		if ( normalized ) {
			final var xyzsum = 1.0f - this.X * this.X - this.Y * this.Y - this.Z * this.Z;
			this.W = 1.0e-6f < xyzsum ? MathF.Sqrt ( xyzsum ) : 0.0f;
		}
		else {
			this.W = SimUtils.BytesToFloatSafepos ( byteArray , pos + 12 );
		}
	}


	public enum MainAxis {
		X,
		Y,
		Z
	}
}
