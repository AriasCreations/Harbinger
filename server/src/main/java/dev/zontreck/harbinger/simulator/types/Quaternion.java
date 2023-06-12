package dev.zontreck.harbinger.simulator.types;

import dev.zontreck.harbinger.utils.MathF;
import dev.zontreck.harbinger.utils.SimUtils;

public class Quaternion {
	public static final Quaternion Identity = new Quaternion(0f, 0f, 0f, 1f);
	/// <summary>X value</summary>
	public float X;

	/// <summary>Y value</summary>
	public float Y;

	/// <summary>Z value</summary>
	public float Z;

	/// <summary>W value</summary>
	public float W;

	public enum MainAxis {
		X,
		Y,
		Z
	}

	public Quaternion() {

	}

	public Quaternion(float x, float y, float z, float w) {
		X = x;
		Y = y;
		Z = z;
		W = w;
	}

	public Quaternion(Vector3 vectorPart, float scalarPart) {
		X = vectorPart.X;
		Y = vectorPart.Y;
		Z = vectorPart.Z;
		W = scalarPart;
	}

	public Quaternion(float x, float y, float z) {
		X = x;
		Y = y;
		Z = z;

		var xyzsum = 1f - X * X - Y * Y - Z * Z;
		W = xyzsum > 1e-6f ? MathF.Sqrt(xyzsum) : 0;
	}

	public Quaternion(byte[] byteArray, int pos, boolean normalized) {
		X = Y = Z = 0;
		W = 1;
		FromBytes(byteArray, pos, normalized);
	}

	public Quaternion(Quaternion q) {
		this.W = q.W;
		this.X = q.X;
		this.Z = q.Z;
		this.Y = q.Y;
	}

	public Quaternion(MainAxis BaseAxis, float angle) {
		switch (BaseAxis) {
			case X:
				W = MathF.Cos(0.5f * angle);
				X = MathF.Sqrt(1.0f - W * W);
				Y = 0;
				Z = 0;
				break;
			case Y:
				W = MathF.Cos(0.5f * angle);
				Y = MathF.Sqrt(1.0f - W * W);
				X = 0;
				Z = 0;
				break;
			case Z:
				W = MathF.Cos(0.5f * angle);
				Z = MathF.Sqrt(1.0f - W * W);
				X = 0;
				Y = 0;
				break;
			default: //error
				X = 0;
				Y = 0;
				Z = 0;
				W = 1;
				break;
		}
	}

	public final boolean ApproxEquals(Quaternion other) {
		return ApproxEquals(other, 1e-6f);
	}

	public final boolean ApproxEquals(Quaternion other, float tolerance) {
		return MathF.Abs(W - other.W) < tolerance &&
				MathF.Abs(Z - other.Z) < tolerance &&
				MathF.Abs(X - other.X) < tolerance;

	}

	public final boolean IsIdentity() {
		return MathF.Abs(W) > 1.0f - 1e-6f;
	}

	public final boolean IsIdentityOrZero() {
		if (X != 0) return false;
		if (Y != 0) return false;
		return Z == 0;
	}

	@Override
	public String toString() {
		return String.format("<%f, %f, %f, %f>", X, Y, Z, W);
	}

	public final boolean Equals(Quaternion other) {
		if (X != other.X)
			return false;
		if (Y != other.Y)
			return false;
		if (Z != other.Z)
			return false;
		return W == other.W;
	}

	public final boolean NotEqual(Quaternion other) {
		return !Equals(other);
	}

	public final boolean Equals(Object obj) {
		if (obj instanceof Quaternion)
			return Equals((Quaternion) obj);
		return false;
	}


	public void FromBytes(byte[] byteArray, int pos, boolean normalized) {
		X = SimUtils.BytesToFloatSafepos(byteArray, pos);
		Y = SimUtils.BytesToFloatSafepos(byteArray, pos + 4);
		Z = SimUtils.BytesToFloatSafepos(byteArray, pos + 8);
		if (normalized) {
			var xyzsum = 1f - X * X - Y * Y - Z * Z;
			W = xyzsum > 1e-6f ? MathF.Sqrt(xyzsum) : 0f;
		} else {
			W = SimUtils.BytesToFloatSafepos(byteArray, pos + 12);
		}
	}
}
