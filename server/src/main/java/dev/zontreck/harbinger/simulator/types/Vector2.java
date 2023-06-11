package dev.zontreck.harbinger.simulator.types;

import dev.zontreck.harbinger.utils.MathF;
import dev.zontreck.harbinger.utils.SimUtils;

public class Vector2
{
	public float X;
	public float Y;
	/// <summary>A vector with a value of 0,0</summary>
	public static final Vector2 Zero = new Vector2();

	/// <summary>A vector with a value of 1,1</summary>
	public static final Vector2 One = new Vector2(1f, 1f);

	/// <summary>A vector with a value of 1,0</summary>
	public static final Vector2 UnitX = new Vector2(1f, 0f);

	/// <summary>A vector with a value of 0,1</summary>
	public static final Vector2 UnitY = new Vector2(0f, 1f);

	public static final Vector2 MinValue = new Vector2(Float.MIN_VALUE, Float.MIN_VALUE);
	public static final Vector2 MaxValue = new Vector2(Float.MAX_VALUE, Float.MAX_VALUE);

	public Vector2()
	{
		X=0;
		Y=0;
	}

	public Vector2(float x, float y)
	{
		X=x;
		Y=y;
	}

	public Vector2(float val)
	{
		X=val;
		Y=val;
	}

	public Vector2(Vector2 other)
	{
		X= other.X;
		Y= other.Y;
	}

	public void Abs()
	{
		if(X < 0) X = -X;
		if(Y < 0) Y = -Y;
	}

	public void Min(Vector2 v)
	{
		if (v.X < X) X = v.X;
		if (v.Y < Y) Y = v.Y;
	}

	public void Max(Vector2 v)
	{
		if (v.X > X) X = v.X;
		if (v.Y > Y) Y = v.Y;
	}
	public void Add(Vector2 v)
	{
		X += v.X;
		Y += v.Y;
	}
	public void Sub(Vector2 v)
	{
		X -= v.X;
		Y -= v.Y;
	}

	public boolean ApproxEquals(Vector2 vec, float tolerance)
	{
		return SimUtils.ApproxEqual(X, vec.X, tolerance) &&
				SimUtils.ApproxEqual(Y, vec.Y, tolerance);
	}

	public boolean ApproxEquals(Vector2 vec)
	{
		return SimUtils.ApproxEqual(X, vec.X) &&
				SimUtils.ApproxEqual(Y, vec.Y);
	}

	public boolean ApproxZero()
	{
		if (!SimUtils.ApproxZero(X))
			return false;
		if (!SimUtils.ApproxZero(Y))
			return false;
		return true;
	}
	public boolean ApproxZero(float tolerance)
	{
		if (!SimUtils.ApproxZero(X, tolerance))
			return false;
		if (!SimUtils.ApproxZero(Y, tolerance))
			return false;
		return true;
	}

	public boolean IsZero()
	{
		if (X != 0)
			return false;
		if (Y != 0)
			return false;
		return true;
	}
	public boolean IsNotZero()
	{
		return !IsZero();
	}
	public boolean IsFinite()
	{
		return SimUtils.IsFinite(X) && SimUtils.IsFinite(Y);
	}

	public float Length()
	{
		return MathF.Sqrt(X * X + Y * Y);
	}

	public float LengthSquared()
	{
		return X * X + Y * Y;
	}

	public void Normalize()
	{
		var factor = LengthSquared();
		if (factor > 1e-6)
		{
			factor = 1f / MathF.Sqrt(factor);
			X *= factor;
			Y *= factor;
		}
		else
		{
			X = 0f;
			Y = 0f;
		}
	}



	public static Vector2 Add(Vector2 value1, Vector2 value2)
	{
		return new Vector2(value1.X + value2.X, value1.Y + value2.Y);
	}

	public static Vector2 Clamp(Vector2 value1, float min, float max)
	{
		return new Vector2(
				MathF.Clamp(value1.X, min, max),
				MathF.Clamp(value1.Y, min, max));
	}

	public static Vector2 Clamp(Vector2 value1, Vector2 min, Vector2 max)
	{
		return new Vector2(
				MathF.Clamp(value1.X, min.X, max.X),
				MathF.Clamp(value1.Y, min.Y, max.Y));
	}

	public static float Distance(Vector2 value1, Vector2 value2)
	{
		return MathF.Sqrt(DistanceSquared(value1, value2));
	}

	public static float DistanceSquared(Vector2 value1, Vector2 value2)
	{
		return
				(value1.X - value2.X) * (value1.X - value2.X) +
						(value1.Y - value2.Y) * (value1.Y - value2.Y);
	}

	public static Vector2 Divide(Vector2 value1, Vector2 value2)
	{
		return new Vector2(value1.X / value2.X, value1.Y / value2.Y);
	}

	public static Vector2 Divide(Vector2 value1, float divider)
	{
		var factor = 1 / divider;
		return new Vector2(value1.X * factor, value1.Y * factor);
	}

	public static float Dot(Vector2 value1, Vector2 value2)
	{
		return value1.X * value2.X + value1.Y * value2.Y;
	}

	public static Vector2 Lerp(Vector2 value1, Vector2 value2, float amount)
	{
		return new Vector2(
				SimUtils.Lerp(value1.X, value2.X, amount),
				SimUtils.Lerp(value1.Y, value2.Y, amount));
	}

	public static Vector2 Max(Vector2 value1, Vector2 value2)
	{
		return new Vector2(
				MathF.Max(value1.X, value2.X),
				MathF.Max(value1.Y, value2.Y));
	}

	public static Vector2 Min(Vector2 value1, Vector2 value2)
	{
		return new Vector2(
				MathF.Min(value1.X, value2.X),
				MathF.Min(value1.Y, value2.Y));
	}

	public static Vector2 Multiply(Vector2 value1, Vector2 value2)
	{
		return new Vector2(value1.X * value2.X, value1.Y * value2.Y);
	}

	public static Vector2 Multiply(Vector2 value1, float scaleFactor)
	{
		return new Vector2(value1.X * scaleFactor, value1.Y * scaleFactor);
	}

	public static Vector2 Negate(Vector2 value)
	{
		return new Vector2(-value.X, -value.Y);
	}

	public static Vector2 Normalize(Vector2 value)
	{
		var factor = value.LengthSquared();
		if (factor > 1e-6)
		{
			factor = 1f / MathF.Sqrt(factor);
			return new Vector2(value.X * factor, value.Y * factor);
		}

		return new Vector2();
	}


	/// <summary>
	///     Interpolates between two vectors using a cubic equation
	/// </summary>
	public static Vector2 SmoothStep(Vector2 value1, Vector2 value2, float amount)
	{
		return new Vector2(
				SimUtils.SmoothStep(value1.X, value2.X, amount),
				SimUtils.SmoothStep(value1.Y, value2.Y, amount));
	}

	public static Vector2 Subtract(Vector2 value1, Vector2 value2)
	{
		return new Vector2(value1.X - value2.X, value1.Y - value2.Y);
	}

}
