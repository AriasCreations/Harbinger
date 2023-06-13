package dev.zontreck.harbinger.simulator.types;

import dev.zontreck.harbinger.utils.MathF;
import dev.zontreck.harbinger.utils.SimUtils;

public class Vector2i {
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

	public Vector2i() {
		this.X = 0;
		this.Y = 0;
	}

	public Vector2i(final int x, final int y) {
		this.X = x;
		this.Y = y;
	}

	public Vector2i(final int val) {
		this.X = val;
		this.Y = val;
	}

	public Vector2i(final Vector2i other) {
		this.X = other.X;
		this.Y = other.Y;
	}

	public void Abs() {
		if (0 > X) this.X = -this.X;
		if (0 > Y) this.Y = -this.Y;
	}

	public void Min(final Vector2i v) {
		if (v.X < this.X) this.X = v.X;
		if (v.Y < this.Y) this.Y = v.Y;
	}

	public void Max(final Vector2i v) {
		if (v.X > this.X) this.X = v.X;
		if (v.Y > this.Y) this.Y = v.Y;
	}

	public void Add(final Vector2i v) {
		this.X += v.X;
		this.Y += v.Y;
	}

	public void Sub(final Vector2i v) {
		this.X -= v.X;
		this.Y -= v.Y;
	}

	public boolean ApproxEquals(final Vector2i vec, final float tolerance) {
		return SimUtils.ApproxEqual(this.X, vec.X, tolerance) &&
				SimUtils.ApproxEqual(this.Y, vec.Y, tolerance);
	}

	public boolean ApproxEquals(final Vector2i vec) {
		return SimUtils.ApproxEqual(this.X, vec.X) &&
				SimUtils.ApproxEqual(this.Y, vec.Y);
	}

	public boolean ApproxZero() {
		if (!SimUtils.ApproxZero(this.X))
			return false;
		return SimUtils.ApproxZero(this.Y);
	}

	public boolean ApproxZero(final float tolerance) {
		if (!SimUtils.ApproxZero(this.X, tolerance))
			return false;
		return SimUtils.ApproxZero(this.Y, tolerance);
	}

	public boolean IsZero() {
		if (0 != X)
			return false;
		return 0 == Y;
	}

	public boolean IsNotZero() {
		return !this.IsZero();
	}

	public boolean IsFinite() {
		return SimUtils.IsFinite(this.X) && SimUtils.IsFinite(this.Y);
	}

	public float Length() {
		return MathF.Sqrt(this.X * this.X + this.Y * this.Y);
	}

	public int LengthSquared() {
		return this.X * this.X + this.Y * this.Y;
	}

	public void Normalize() {
		var factor = this.LengthSquared();
		if (1.0e-6 < factor) {
			factor = 1 / MathF.Sqrt(factor);
			this.X *= factor;
			this.Y *= factor;
		} else {
			this.X = 0;
			this.Y = 0;
		}
	}


	public static Vector2i Add(final Vector2i value1, final Vector2i value2) {
		return new Vector2i(value1.X + value2.X, value1.Y + value2.Y);
	}

	public static Vector2i Clamp(final Vector2i value1, final int min, final int max) {
		return new Vector2i(
				MathF.Clamp(value1.X, min, max),
				MathF.Clamp(value1.Y, min, max));
	}

	public static Vector2i Clamp(final Vector2i value1, final Vector2i min, final Vector2i max) {
		return new Vector2i(
				MathF.Clamp(value1.X, min.X, max.X),
				MathF.Clamp(value1.Y, min.Y, max.Y));
	}

	public static float Distance(final Vector2i value1, final Vector2i value2) {
		return MathF.Sqrt(Vector2i.DistanceSquared(value1, value2));
	}

	public static float DistanceSquared(final Vector2i value1, final Vector2i value2) {
		return
				(value1.X - value2.X) * (value1.X - value2.X) +
						(value1.Y - value2.Y) * (value1.Y - value2.Y);
	}

	public static Vector2i Divide(final Vector2i value1, final Vector2i value2) {
		return new Vector2i(value1.X / value2.X, value1.Y / value2.Y);
	}

	public static Vector2i Divide(final Vector2i value1, final int divider) {
		final var factor = 1 / divider;
		return new Vector2i(value1.X * factor, value1.Y * factor);
	}

	public static float Dot(final Vector2i value1, final Vector2i value2) {
		return value1.X * value2.X + value1.Y * value2.Y;
	}

	public static Vector2i Lerp(final Vector2i value1, final Vector2i value2, final int amount) {
		return new Vector2i(
				SimUtils.Lerp(value1.X, value2.X, amount),
				SimUtils.Lerp(value1.Y, value2.Y, amount));
	}

	public static Vector2i Max(final Vector2i value1, final Vector2i value2) {
		return new Vector2i(
				MathF.Max(value1.X, value2.X),
				MathF.Max(value1.Y, value2.Y));
	}

	public static Vector2i Min(final Vector2i value1, final Vector2i value2) {
		return new Vector2i(
				MathF.Min(value1.X, value2.X),
				MathF.Min(value1.Y, value2.Y));
	}

	public static Vector2i Multiply(final Vector2i value1, final Vector2i value2) {
		return new Vector2i(value1.X * value2.X, value1.Y * value2.Y);
	}

	public static Vector2i Multiply(final Vector2i value1, final int scaleFactor) {
		return new Vector2i(value1.X * scaleFactor, value1.Y * scaleFactor);
	}

	public static Vector2i Negate(final Vector2i value) {
		return new Vector2i(-value.X, -value.Y);
	}

	public static Vector2i Normalize(final Vector2i value) {
		var factor = value.LengthSquared();
		if (1.0e-6 < factor) {
			factor = 1 / MathF.Sqrt(factor);
			return new Vector2i(value.X * factor, value.Y * factor);
		}

		return new Vector2i();
	}


	/// <summary>
	///     Interpolates between two vectors using a cubic equation
	/// </summary>
	public static Vector2i SmoothStep(final Vector2i value1, final Vector2i value2, final int amount) {
		return new Vector2i(
				SimUtils.SmoothStep(value1.X, value2.X, amount),
				SimUtils.SmoothStep(value1.Y, value2.Y, amount));
	}

	public static Vector2i Subtract(final Vector2i value1, final Vector2i value2) {
		return new Vector2i(value1.X - value2.X, value1.Y - value2.Y);
	}

}
