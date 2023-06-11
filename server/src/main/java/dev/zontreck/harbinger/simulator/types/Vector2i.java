package dev.zontreck.harbinger.simulator.types;

import dev.zontreck.harbinger.utils.MathF;
import dev.zontreck.harbinger.utils.SimUtils;

public class Vector2i
{
	public int X;
	public int Y;
	/// <summary>A vector with a value of 0,0</summary>
	public static final Vector2i Zero = new Vector2i();

	/// <summary>A vector with a value of 1,1</summary>
	public static final Vector2i One = new Vector2i(1, 1);

	/// <summary>A vector with a value of 1,0</summary>
	public static final Vector2i UnitX = new Vector2i(1, 0);

	/// <summary>A vector with a value of 0,1</summary>
	public static final Vector2i UnitY = new Vector2i(0, 1);

	public static final Vector2i MinValue = new Vector2i(Integer.MIN_VALUE, Integer.MIN_VALUE);
	public static final Vector2i MaxValue = new Vector2i(Integer.MAX_VALUE, Integer.MAX_VALUE);

	public Vector2i()
	{
		X=0;
		Y=0;
	}

	public Vector2i(int x, int y)
	{
		X=x;
		Y=y;
	}

	public Vector2i(int val)
	{
		X=val;
		Y=val;
	}

	public Vector2i(Vector2i other)
	{
		X= other.X;
		Y= other.Y;
	}

	public void Abs()
	{
		if(X < 0) X = -X;
		if(Y < 0) Y = -Y;
	}

	public void Min(Vector2i v)
	{
		if (v.X < X) X = v.X;
		if (v.Y < Y) Y = v.Y;
	}

	public void Max(Vector2i v)
	{
		if (v.X > X) X = v.X;
		if (v.Y > Y) Y = v.Y;
	}
	public void Add(Vector2i v)
	{
		X += v.X;
		Y += v.Y;
	}
	public void Sub(Vector2i v)
	{
		X -= v.X;
		Y -= v.Y;
	}

	public boolean ApproxEquals(Vector2i vec, float tolerance)
	{
		return SimUtils.ApproxEqual(X, vec.X, tolerance) &&
				SimUtils.ApproxEqual(Y, vec.Y, tolerance);
	}

	public boolean ApproxEquals(Vector2i vec)
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

	public int LengthSquared()
	{
		return X * X + Y * Y;
	}

	public void Normalize()
	{
		var factor = LengthSquared();
		if (factor > 1e-6)
		{
			factor = 1 / MathF.Sqrt(factor);
			X *= factor;
			Y *= factor;
		}
		else
		{
			X = 0;
			Y = 0;
		}
	}



	public static Vector2i Add(Vector2i value1, Vector2i value2)
	{
		return new Vector2i(value1.X + value2.X, value1.Y + value2.Y);
	}

	public static Vector2i Clamp(Vector2i value1, int min, int max)
	{
		return new Vector2i(
				MathF.Clamp(value1.X, min, max),
				MathF.Clamp(value1.Y, min, max));
	}

	public static Vector2i Clamp(Vector2i value1, Vector2i min, Vector2i max)
	{
		return new Vector2i(
				MathF.Clamp(value1.X, min.X, max.X),
				MathF.Clamp(value1.Y, min.Y, max.Y));
	}

	public static float Distance(Vector2i value1, Vector2i value2)
	{
		return MathF.Sqrt(DistanceSquared(value1, value2));
	}

	public static float DistanceSquared(Vector2i value1, Vector2i value2)
	{
		return
				(value1.X - value2.X) * (value1.X - value2.X) +
						(value1.Y - value2.Y) * (value1.Y - value2.Y);
	}

	public static Vector2i Divide(Vector2i value1, Vector2i value2)
	{
		return new Vector2i(value1.X / value2.X, value1.Y / value2.Y);
	}

	public static Vector2i Divide(Vector2i value1, int divider)
	{
		var factor = 1 / divider;
		return new Vector2i(value1.X * factor, value1.Y * factor);
	}

	public static float Dot(Vector2i value1, Vector2i value2)
	{
		return value1.X * value2.X + value1.Y * value2.Y;
	}

	public static Vector2i Lerp(Vector2i value1, Vector2i value2, int amount)
	{
		return new Vector2i(
				SimUtils.Lerp(value1.X, value2.X, amount),
				SimUtils.Lerp(value1.Y, value2.Y, amount));
	}

	public static Vector2i Max(Vector2i value1, Vector2i value2)
	{
		return new Vector2i(
				MathF.Max(value1.X, value2.X),
				MathF.Max(value1.Y, value2.Y));
	}

	public static Vector2i Min(Vector2i value1, Vector2i value2)
	{
		return new Vector2i(
				MathF.Min(value1.X, value2.X),
				MathF.Min(value1.Y, value2.Y));
	}

	public static Vector2i Multiply(Vector2i value1, Vector2i value2)
	{
		return new Vector2i(value1.X * value2.X, value1.Y * value2.Y);
	}

	public static Vector2i Multiply(Vector2i value1, int scaleFactor)
	{
		return new Vector2i(value1.X * scaleFactor, value1.Y * scaleFactor);
	}

	public static Vector2i Negate(Vector2i value)
	{
		return new Vector2i(-value.X, -value.Y);
	}

	public static Vector2i Normalize(Vector2i value)
	{
		var factor = value.LengthSquared();
		if (factor > 1e-6)
		{
			factor = 1 / MathF.Sqrt(factor);
			return new Vector2i(value.X * factor, value.Y * factor);
		}

		return new Vector2i();
	}


	/// <summary>
	///     Interpolates between two vectors using a cubic equation
	/// </summary>
	public static Vector2i SmoothStep(Vector2i value1, Vector2i value2, int amount)
	{
		return new Vector2i(
				SimUtils.SmoothStep(value1.X, value2.X, amount),
				SimUtils.SmoothStep(value1.Y, value2.Y, amount));
	}

	public static Vector2i Subtract(Vector2i value1, Vector2i value2)
	{
		return new Vector2i(value1.X - value2.X, value1.Y - value2.Y);
	}

}
