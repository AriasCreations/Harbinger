package dev.zontreck.harbinger.utils;

import dev.zontreck.harbinger.simulator.types.Quaternion;
import dev.zontreck.harbinger.simulator.types.Vector2i;
import dev.zontreck.harbinger.simulator.types.Vector3;

public class Matrix4 {
	/// <summary>A 4x4 matrix containing all zeroes</summary>
	public static final Matrix4 Zero = new Matrix4();

	/// <summary>A 4x4 identity matrix</summary>
	public static final Matrix4 Identity = new Matrix4(
			1.0f, 0.0f, 0.0f, 0.0f,
			0.0f, 1.0f, 0.0f, 0.0f,
			0.0f, 0.0f, 1.0f, 0.0f,
			0.0f, 0.0f, 0.0f, 1.0f);

	public float M11, M12, M13, M14;
	public float M21, M22, M23, M24;
	public float M31, M32, M33, M34;
	public float M41, M42, M43, M44;

	public Vector3 getAtAxis() {
		return new Vector3(this.M11, this.M21, this.M31);
	}

	public void setAtAxis(final Vector3 vect) {
		this.M11 = vect.X;
		this.M21 = vect.Y;
		this.M31 = vect.Z;
	}

	public Vector3 getLeftAxis() {
		return new Vector3(this.M12, this.M22, this.M32);
	}

	public void setLeftAxis(final Vector3 axis) {
		this.M12 = axis.X;
		this.M22 = axis.Y;
		this.M32 = axis.Z;
	}

	public Vector3 getUpAxis() {
		return new Vector3(this.M13, this.M23, this.M33);
	}

	public void setUpAxis(final Vector3 axis) {
		this.M13 = axis.X;
		this.M23 = axis.Y;
		this.M33 = axis.Z;
	}


	public Matrix4(
			final float m11, final float m12, final float m13, final float m14,
			final float m21, final float m22, final float m23, final float m24,
			final float m31, final float m32, final float m33, final float m34,
			final float m41, final float m42, final float m43, final float m44) {
		this.M11 = m11;
		this.M12 = m12;
		this.M13 = m13;
		this.M14 = m14;

		this.M21 = m21;
		this.M22 = m22;
		this.M23 = m23;
		this.M24 = m24;

		this.M31 = m31;
		this.M32 = m32;
		this.M33 = m33;
		this.M34 = m34;

		this.M41 = m41;
		this.M42 = m42;
		this.M43 = m43;
		this.M44 = m44;
	}

	public Matrix4() {

	}

	public Matrix4(final float roll, final float pitch, final float yaw) {
		this.initFromEulers(roll, pitch, yaw);

	}


	public Matrix4(final Matrix4 m) {
		this.M11 = m.M11;
		this.M12 = m.M12;
		this.M13 = m.M13;
		this.M14 = m.M14;

		this.M21 = m.M21;
		this.M22 = m.M22;
		this.M23 = m.M23;
		this.M24 = m.M24;

		this.M31 = m.M31;
		this.M32 = m.M32;
		this.M33 = m.M33;
		this.M34 = m.M34;

		this.M41 = m.M41;
		this.M42 = m.M42;
		this.M43 = m.M43;
		this.M44 = m.M44;
	}


	public float Determinant() {
		return
				this.M14 * this.M23 * this.M32 * this.M41 - this.M13 * this.M24 * this.M32 * this.M41 - this.M14 * this.M22 * this.M33 * this.M41 + this.M12 * this.M24 * this.M33 * this.M41 +
						this.M13 * this.M22 * this.M34 * this.M41 - this.M12 * this.M23 * this.M34 * this.M41 - this.M14 * this.M23 * this.M31 * this.M42 + this.M13 * this.M24 * this.M31 * this.M42 +
						this.M14 * this.M21 * this.M33 * this.M42 - this.M11 * this.M24 * this.M33 * this.M42 - this.M13 * this.M21 * this.M34 * this.M42 + this.M11 * this.M23 * this.M34 * this.M42 +
						this.M14 * this.M22 * this.M31 * this.M43 - this.M12 * this.M24 * this.M31 * this.M43 - this.M14 * this.M21 * this.M32 * this.M43 + this.M11 * this.M24 * this.M32 * this.M43 +
						this.M12 * this.M21 * this.M34 * this.M43 - this.M11 * this.M22 * this.M34 * this.M43 - this.M13 * this.M22 * this.M31 * this.M44 + this.M12 * this.M23 * this.M31 * this.M44 +
						this.M13 * this.M21 * this.M32 * this.M44 - this.M11 * this.M23 * this.M32 * this.M44 - this.M12 * this.M21 * this.M33 * this.M44 + this.M11 * this.M22 * this.M33 * this.M44;
	}

	public float Determinant3x3() {
		var det = 0.0f;

		final var diag1 = this.M11 * this.M22 * this.M33;
		final var diag2 = this.M12 * this.M23 * this.M31;
		final var diag3 = this.M13 * this.M21 * this.M32;
		final var diag4 = this.M31 * this.M22 * this.M13;
		final var diag5 = this.M32 * this.M23 * this.M11;
		final var diag6 = this.M33 * this.M21 * this.M12;

		det = diag1 + diag2 + diag3 - (diag4 + diag5 + diag6);

		return det;
	}

	public float Trace() {
		return this.M11 + this.M22 + this.M33 + this.M44;
	}

	public Vector3 GetEulerAngles(float roll, float pitch, float yaw) {
		float angleX, angleY, angleZ;
		final float cx;  // cosines
		float cy;
		final float cz;
		final float sx;  // sines
		final float sz;

		angleY = MathF.Asin(MathF.Clamp(this.M13, -1.0f, 1.0f));
		cy = MathF.Cos(angleY);

		if (0.005f < MathF.Abs(cy)) {
			// No gimbal lock
			cx = this.M33 / cy;
			sx = -this.M23 / cy;

			angleX = MathF.Atan2(sx, cx);

			cz = this.M11 / cy;
			sz = -this.M12 / cy;

			angleZ = MathF.Atan2(sz, cz);
		} else {
			// Gimbal lock
			angleX = 0;

			cz = this.M22;
			sz = this.M21;

			angleZ = MathF.Atan2(sz, cz);
		}

		// Return only positive angles in [0,360]
		if (0 > angleX) angleX += 360.0f;
		if (0 > angleY) angleY += 360.0f;
		if (0 > angleZ) angleZ += 360.0f;

		roll = angleX;
		pitch = angleY;
		yaw = angleZ;

		return new Vector3(roll, pitch, yaw);
	}

	public Quaternion GetQuaternion() {
		final var quat = new Quaternion();
		final var trace = this.Trace() + 1.0f;

		if (1.0E-6F < trace) {
			final var s = 0.5f / MathF.Sqrt(trace);

			quat.X = (this.M32 - this.M23) * s;
			quat.Y = (this.M13 - this.M31) * s;
			quat.Z = (this.M21 - this.M12) * s;
			quat.W = 0.25f / s;
		} else {
			if (this.M11 > this.M22 && this.M11 > this.M33) {
				final var s = 2.0f * MathF.Sqrt(1.0f + this.M11 - this.M22 - this.M33);

				quat.X = 0.25f * s;
				quat.Y = (this.M12 + this.M21) / s;
				quat.Z = (this.M13 + this.M31) / s;
				quat.W = (this.M23 - this.M32) / s;
			} else if (this.M22 > this.M33) {
				final var s = 2.0f * MathF.Sqrt(1.0f + this.M22 - this.M11 - this.M33);

				quat.X = (this.M12 + this.M21) / s;
				quat.Y = 0.25f * s;
				quat.Z = (this.M23 + this.M32) / s;
				quat.W = (this.M13 - this.M31) / s;
			} else {
				final var s = 2.0f * MathF.Sqrt(1.0f + this.M33 - this.M11 - this.M22);

				quat.X = (this.M13 + this.M31) / s;
				quat.Y = (this.M23 + this.M32) / s;
				quat.Z = 0.25f * s;
				quat.W = (this.M12 - this.M21) / s;
			}
		}

		return quat;
	}

	/*  - TODO: Implement?
	public bool Decompose(out Vector3 scale, out Quaternion rotation, out Vector3 translation)
	{
		translation = new Vector3(M41, M42, M43);

		float xs = Math.Sign(M11 * M12 * M13 * M14) < 0 ? -1 : 1;
		float ys = Math.Sign(M21 * M22 * M23 * M24) < 0 ? -1 : 1;
		float zs = Math.Sign(M31 * M32 * M33 * M34) < 0 ? -1 : 1;

		xs *= MathF.Sqrt(M11 * M11 + M12 * M12 + M13 * M13);
		ys *= MathF.Sqrt(M21 * M21 + M22 * M22 + M23 * M23);
		zs *= MathF.Sqrt(M31 * M31 + M32 * M32 + M33 * M33);

		scale = new Vector3(xs, ys, zs);

		if (xs == 0.0 || ys == 0.0 || zs == 0.0)
		{
			rotation = Quaternion.Identity;
			return false;
		}

		var m1 = new Matrix4(M11 / xs, M12 / xs, M13 / xs, 0,
				M21 / ys, M22 / ys, M23 / ys, 0,
				M31 / zs, M32 / zs, M33 / zs, 0,
				0, 0, 0, 1);

		rotation = Quaternion.CreateFromRotationMatrix(m1);
		return true;
	}*/


	public static Matrix4 Add(final Matrix4 matrix1, final Matrix4 matrix2) {
		final Matrix4 matrix = new Matrix4();
		matrix.M11 = matrix1.M11 + matrix2.M11;
		matrix.M12 = matrix1.M12 + matrix2.M12;
		matrix.M13 = matrix1.M13 + matrix2.M13;
		matrix.M14 = matrix1.M14 + matrix2.M14;

		matrix.M21 = matrix1.M21 + matrix2.M21;
		matrix.M22 = matrix1.M22 + matrix2.M22;
		matrix.M23 = matrix1.M23 + matrix2.M23;
		matrix.M24 = matrix1.M24 + matrix2.M24;

		matrix.M31 = matrix1.M31 + matrix2.M31;
		matrix.M32 = matrix1.M32 + matrix2.M32;
		matrix.M33 = matrix1.M33 + matrix2.M33;
		matrix.M34 = matrix1.M34 + matrix2.M34;

		matrix.M41 = matrix1.M41 + matrix2.M41;
		matrix.M42 = matrix1.M42 + matrix2.M42;
		matrix.M43 = matrix1.M43 + matrix2.M43;
		matrix.M44 = matrix1.M44 + matrix2.M44;
		return matrix;
	}

	public static Matrix4 CreateFromAxisAngle(final Vector3 axis, final float angle) {
		final var matrix = new Matrix4();

		final var x = axis.X;
		final var y = axis.Y;
		final var z = axis.Z;
		final var sin = MathF.Sin(angle);
		final var cos = MathF.Cos(angle);
		final var xx = x * x;
		final var yy = y * y;
		final var zz = z * z;
		final var xy = x * y;
		final var xz = x * z;
		final var yz = y * z;

		matrix.M11 = xx + cos * (1.0f - xx);
		matrix.M12 = xy - cos * xy + sin * z;
		matrix.M13 = xz - cos * xz - sin * y;
		//matrix.M14 = 0f;

		matrix.M21 = xy - cos * xy - sin * z;
		matrix.M22 = yy + cos * (1.0f - yy);
		matrix.M23 = yz - cos * yz + sin * x;
		//matrix.M24 = 0f;

		matrix.M31 = xz - cos * xz + sin * y;
		matrix.M32 = yz - cos * yz - sin * x;
		matrix.M33 = zz + cos * (1.0f - zz);
		//matrix.M34 = 0f;

		//matrix.M41 = matrix.M42 = matrix.M43 = 0f;
		matrix.M44 = 1.0f;

		return matrix;
	}

	/// <summary>
	///     Construct a matrix from euler rotation values in radians
	/// </summary>
	/// <param name="roll">X euler angle in radians</param>
	/// <param name="pitch">Y euler angle in radians</param>
	/// <param name="yaw">Z euler angle in radians</param>
	public static Matrix4 CreateFromEulers(final float roll, final float pitch, final float yaw) {
		final Matrix4 m = new Matrix4();
		m.initFromEulers(roll, pitch, yaw);

		return m;
	}

	private void initFromEulers(final float roll, final float pitch, final float yaw) {

		final float a;
		float b;
		float c;
		float d;
		float e;
		final float f;
		final float ad;
		final float bd;

		a = MathF.Cos(roll);
		b = MathF.Sin(roll);
		c = MathF.Cos(pitch);
		d = MathF.Sin(pitch);
		e = MathF.Cos(yaw);
		f = MathF.Sin(yaw);

		ad = a * d;
		bd = b * d;

		this.M11 = c * e;
		this.M12 = -c * f;
		this.M13 = d;
		this.M14 = 0.0f;

		this.M21 = bd * e + a * f;
		this.M22 = -bd * f + a * e;
		this.M23 = -b * c;
		this.M24 = 0.0f;

		this.M31 = -ad * e + b * f;
		this.M32 = ad * f + b * e;
		this.M33 = a * c;
		this.M34 = 0.0f;

		this.M41 = this.M42 = this.M43 = 0.0f;
		this.M44 = 1.0f;
	}

	public static Matrix4 CreateFromQuaternion(final Quaternion rot) {
		final Matrix4 matrix = new Matrix4();

		final var x2 = rot.X + rot.X;
		final var y2 = rot.Y + rot.Y;
		final var z2 = rot.Z + rot.Z;

		final var wx2 = rot.W * x2;
		final var wy2 = rot.W * y2;
		final var wz2 = rot.W * z2;
		final var xx2 = rot.X * x2;
		final var xy2 = rot.X * y2;
		final var xz2 = rot.X * z2;
		final var yy2 = rot.Y * y2;
		final var yz2 = rot.Y * z2;
		final var zz2 = rot.Z * z2;

		matrix.M11 = 1.0f - yy2 - zz2;
		matrix.M12 = xy2 - wz2;
		matrix.M13 = xz2 + wy2;
		matrix.M14 = 0.0f;

		matrix.M21 = xy2 + wz2;
		matrix.M22 = 1.0f - xx2 - zz2;
		matrix.M23 = yz2 - wx2;
		matrix.M24 = 0.0f;

		matrix.M31 = xz2 - wy2;
		matrix.M32 = yz2 + wx2;
		matrix.M33 = 1.0f - xx2 - yy2;
		matrix.M34 = 0.0f;

		matrix.M43 = matrix.M42 = matrix.M41 = 0.0f;
		matrix.M44 = 1.0f;

		return matrix;
	}

	public static Matrix4 CreateLookAt(final Vector3 cameraPosition, final Vector3 cameraTarget, final Vector3 cameraUpVector) {
		final Matrix4 matrix = new Matrix4();

		final var z = Vector3.Normalize(Vector3.Subtract(cameraPosition, cameraTarget));
		final var x = Vector3.Normalize(Vector3.Cross(cameraUpVector, z));
		final var y = Vector3.Cross(z, x);

		matrix.M11 = x.X;
		matrix.M12 = y.X;
		matrix.M13 = z.X;
		matrix.M14 = 0.0f;

		matrix.M21 = x.Y;
		matrix.M22 = y.Y;
		matrix.M23 = z.Y;
		matrix.M24 = 0.0f;

		matrix.M31 = x.Z;
		matrix.M32 = y.Z;
		matrix.M33 = z.Z;
		matrix.M34 = 0.0f;

		matrix.M41 = -Vector3.Dot(x, cameraPosition);
		matrix.M42 = -Vector3.Dot(y, cameraPosition);
		matrix.M43 = -Vector3.Dot(z, cameraPosition);
		matrix.M44 = 1.0f;

		return matrix;
	}

	public static Matrix4 CreateRotationX(final float radians) {
		final Matrix4 matrix = new Matrix4();

		final var cos = MathF.Cos(radians);
		final var sin = MathF.Sin(radians);

		matrix.M11 = 1.0f;
		matrix.M12 = 0.0f;
		matrix.M13 = 0.0f;
		matrix.M14 = 0.0f;

		matrix.M21 = 0.0f;
		matrix.M22 = cos;
		matrix.M23 = sin;
		matrix.M24 = 0.0f;

		matrix.M31 = 0.0f;
		matrix.M32 = -sin;
		matrix.M33 = cos;
		matrix.M34 = 0.0f;

		matrix.M41 = 0.0f;
		matrix.M42 = 0.0f;
		matrix.M43 = 0.0f;
		matrix.M44 = 1.0f;

		return matrix;
	}

	public static Matrix4 CreateRotationY(final float radians) {
		final Matrix4 matrix = new Matrix4();

		final var cos = MathF.Cos(radians);
		final var sin = MathF.Sin(radians);

		matrix.M11 = cos;
		matrix.M12 = 0.0f;
		matrix.M13 = -sin;
		matrix.M14 = 0.0f;

		matrix.M21 = 0.0f;
		matrix.M22 = 1.0f;
		matrix.M23 = 0.0f;
		matrix.M24 = 0.0f;

		matrix.M31 = sin;
		matrix.M32 = 0.0f;
		matrix.M33 = cos;
		matrix.M34 = 0.0f;

		matrix.M41 = 0.0f;
		matrix.M42 = 0.0f;
		matrix.M43 = 0.0f;
		matrix.M44 = 1.0f;

		return matrix;
	}

	public static Matrix4 CreateRotationZ(final float radians) {
		final Matrix4 matrix = new Matrix4();

		final var cos = MathF.Cos(radians);
		final var sin = MathF.Sin(radians);

		matrix.M11 = cos;
		matrix.M12 = sin;
		matrix.M13 = 0.0f;
		matrix.M14 = 0.0f;

		matrix.M21 = -sin;
		matrix.M22 = cos;
		matrix.M23 = 0.0f;
		matrix.M24 = 0.0f;

		matrix.M31 = 0.0f;
		matrix.M32 = 0.0f;
		matrix.M33 = 1.0f;
		matrix.M34 = 0.0f;

		matrix.M41 = 0.0f;
		matrix.M42 = 0.0f;
		matrix.M43 = 0.0f;
		matrix.M44 = 1.0f;

		return matrix;
	}

	public static Matrix4 CreateScale(final Vector3 scale) {
		final Matrix4 matrix = new Matrix4();

		matrix.M11 = scale.X;
		matrix.M12 = 0.0f;
		matrix.M13 = 0.0f;
		matrix.M14 = 0.0f;

		matrix.M21 = 0.0f;
		matrix.M22 = scale.Y;
		matrix.M23 = 0.0f;
		matrix.M24 = 0.0f;

		matrix.M31 = 0.0f;
		matrix.M32 = 0.0f;
		matrix.M33 = scale.Z;
		matrix.M34 = 0.0f;

		matrix.M41 = 0.0f;
		matrix.M42 = 0.0f;
		matrix.M43 = 0.0f;
		matrix.M44 = 1.0f;

		return matrix;
	}

	public static Matrix4 CreateTranslation(final Vector3 position) {
		final Matrix4 matrix = new Matrix4();

		matrix.M11 = 1.0f;
		matrix.M12 = 0.0f;
		matrix.M13 = 0.0f;
		matrix.M14 = 0.0f;

		matrix.M21 = 0.0f;
		matrix.M22 = 1.0f;
		matrix.M23 = 0.0f;
		matrix.M24 = 0.0f;

		matrix.M31 = 0.0f;
		matrix.M32 = 0.0f;
		matrix.M33 = 1.0f;
		matrix.M34 = 0.0f;

		matrix.M41 = position.X;
		matrix.M42 = position.Y;
		matrix.M43 = position.Z;
		matrix.M44 = 1.0f;

		return matrix;
	}

	public static Matrix4 CreateWorld(final Vector3 position, final Vector3 forward, Vector3 up) {
		final Matrix4 result = new Matrix4();

		// Normalize forward vector
		forward.Normalize();

		// Calculate right vector
		final var right = Vector3.Cross(forward, up);
		right.Normalize();

		// Recalculate up vector
		up = Vector3.Cross(right, forward);
		up.Normalize();

		result.M11 = right.X;
		result.M12 = right.Y;
		result.M13 = right.Z;
		result.M14 = 0.0f;

		result.M21 = up.X;
		result.M22 = up.Y;
		result.M23 = up.Z;
		result.M24 = 0.0f;

		result.M31 = -forward.X;
		result.M32 = -forward.Y;
		result.M33 = -forward.Z;
		result.M34 = 0.0f;

		result.M41 = position.X;
		result.M42 = position.Y;
		result.M43 = position.Z;
		result.M44 = 1.0f;

		return result;
	}

	public static Matrix4 Divide(final Matrix4 matrix1, final Matrix4 matrix2) {
		final Matrix4 matrix = new Matrix4();

		matrix.M11 = matrix1.M11 / matrix2.M11;
		matrix.M12 = matrix1.M12 / matrix2.M12;
		matrix.M13 = matrix1.M13 / matrix2.M13;
		matrix.M14 = matrix1.M14 / matrix2.M14;

		matrix.M21 = matrix1.M21 / matrix2.M21;
		matrix.M22 = matrix1.M22 / matrix2.M22;
		matrix.M23 = matrix1.M23 / matrix2.M23;
		matrix.M24 = matrix1.M24 / matrix2.M24;

		matrix.M31 = matrix1.M31 / matrix2.M31;
		matrix.M32 = matrix1.M32 / matrix2.M32;
		matrix.M33 = matrix1.M33 / matrix2.M33;
		matrix.M34 = matrix1.M34 / matrix2.M34;

		matrix.M41 = matrix1.M41 / matrix2.M41;
		matrix.M42 = matrix1.M42 / matrix2.M42;
		matrix.M43 = matrix1.M43 / matrix2.M43;
		matrix.M44 = matrix1.M44 / matrix2.M44;

		return matrix;
	}

	public static Matrix4 Divide(final Matrix4 matrix1, final float divider) {
		final Matrix4 matrix = new Matrix4();

		final var oodivider = 1.0f / divider;
		matrix.M11 = matrix1.M11 * oodivider;
		matrix.M12 = matrix1.M12 * oodivider;
		matrix.M13 = matrix1.M13 * oodivider;
		matrix.M14 = matrix1.M14 * oodivider;

		matrix.M21 = matrix1.M21 * oodivider;
		matrix.M22 = matrix1.M22 * oodivider;
		matrix.M23 = matrix1.M23 * oodivider;
		matrix.M24 = matrix1.M24 * oodivider;

		matrix.M31 = matrix1.M31 * oodivider;
		matrix.M32 = matrix1.M32 * oodivider;
		matrix.M33 = matrix1.M33 * oodivider;
		matrix.M34 = matrix1.M34 * oodivider;

		matrix.M41 = matrix1.M41 * oodivider;
		matrix.M42 = matrix1.M42 * oodivider;
		matrix.M43 = matrix1.M43 * oodivider;
		matrix.M44 = matrix1.M44 * oodivider;

		return matrix;
	}

	public static Matrix4 Lerp(final Matrix4 matrix1, final Matrix4 matrix2, final float amount) {
		final Matrix4 matrix = new Matrix4();

		matrix.M11 = matrix1.M11 + (matrix2.M11 - matrix1.M11) * amount;
		matrix.M12 = matrix1.M12 + (matrix2.M12 - matrix1.M12) * amount;
		matrix.M13 = matrix1.M13 + (matrix2.M13 - matrix1.M13) * amount;
		matrix.M14 = matrix1.M14 + (matrix2.M14 - matrix1.M14) * amount;

		matrix.M21 = matrix1.M21 + (matrix2.M21 - matrix1.M21) * amount;
		matrix.M22 = matrix1.M22 + (matrix2.M22 - matrix1.M22) * amount;
		matrix.M23 = matrix1.M23 + (matrix2.M23 - matrix1.M23) * amount;
		matrix.M24 = matrix1.M24 + (matrix2.M24 - matrix1.M24) * amount;

		matrix.M31 = matrix1.M31 + (matrix2.M31 - matrix1.M31) * amount;
		matrix.M32 = matrix1.M32 + (matrix2.M32 - matrix1.M32) * amount;
		matrix.M33 = matrix1.M33 + (matrix2.M33 - matrix1.M33) * amount;
		matrix.M34 = matrix1.M34 + (matrix2.M34 - matrix1.M34) * amount;

		matrix.M41 = matrix1.M41 + (matrix2.M41 - matrix1.M41) * amount;
		matrix.M42 = matrix1.M42 + (matrix2.M42 - matrix1.M42) * amount;
		matrix.M43 = matrix1.M43 + (matrix2.M43 - matrix1.M43) * amount;
		matrix.M44 = matrix1.M44 + (matrix2.M44 - matrix1.M44) * amount;

		return matrix;
	}

	public static Matrix4 Multiply(final Matrix4 matrix1, final Matrix4 matrix2) {
		return new Matrix4(
				matrix1.M11 * matrix2.M11 + matrix1.M12 * matrix2.M21 + matrix1.M13 * matrix2.M31 +
						matrix1.M14 * matrix2.M41,
				matrix1.M11 * matrix2.M12 + matrix1.M12 * matrix2.M22 + matrix1.M13 * matrix2.M32 +
						matrix1.M14 * matrix2.M42,
				matrix1.M11 * matrix2.M13 + matrix1.M12 * matrix2.M23 + matrix1.M13 * matrix2.M33 +
						matrix1.M14 * matrix2.M43,
				matrix1.M11 * matrix2.M14 + matrix1.M12 * matrix2.M24 + matrix1.M13 * matrix2.M34 +
						matrix1.M14 * matrix2.M44,
				matrix1.M21 * matrix2.M11 + matrix1.M22 * matrix2.M21 + matrix1.M23 * matrix2.M31 +
						matrix1.M24 * matrix2.M41,
				matrix1.M21 * matrix2.M12 + matrix1.M22 * matrix2.M22 + matrix1.M23 * matrix2.M32 +
						matrix1.M24 * matrix2.M42,
				matrix1.M21 * matrix2.M13 + matrix1.M22 * matrix2.M23 + matrix1.M23 * matrix2.M33 +
						matrix1.M24 * matrix2.M43,
				matrix1.M21 * matrix2.M14 + matrix1.M22 * matrix2.M24 + matrix1.M23 * matrix2.M34 +
						matrix1.M24 * matrix2.M44,
				matrix1.M31 * matrix2.M11 + matrix1.M32 * matrix2.M21 + matrix1.M33 * matrix2.M31 +
						matrix1.M34 * matrix2.M41,
				matrix1.M31 * matrix2.M12 + matrix1.M32 * matrix2.M22 + matrix1.M33 * matrix2.M32 +
						matrix1.M34 * matrix2.M42,
				matrix1.M31 * matrix2.M13 + matrix1.M32 * matrix2.M23 + matrix1.M33 * matrix2.M33 +
						matrix1.M34 * matrix2.M43,
				matrix1.M31 * matrix2.M14 + matrix1.M32 * matrix2.M24 + matrix1.M33 * matrix2.M34 +
						matrix1.M34 * matrix2.M44,
				matrix1.M41 * matrix2.M11 + matrix1.M42 * matrix2.M21 + matrix1.M43 * matrix2.M31 +
						matrix1.M44 * matrix2.M41,
				matrix1.M41 * matrix2.M12 + matrix1.M42 * matrix2.M22 + matrix1.M43 * matrix2.M32 +
						matrix1.M44 * matrix2.M42,
				matrix1.M41 * matrix2.M13 + matrix1.M42 * matrix2.M23 + matrix1.M43 * matrix2.M33 +
						matrix1.M44 * matrix2.M43,
				matrix1.M41 * matrix2.M14 + matrix1.M42 * matrix2.M24 + matrix1.M43 * matrix2.M34 +
						matrix1.M44 * matrix2.M44
		);
	}

	public static Matrix4 Multiply(final Matrix4 matrix1, final float scaleFactor) {
		final Matrix4 matrix = new Matrix4();
		matrix.M11 = matrix1.M11 * scaleFactor;
		matrix.M12 = matrix1.M12 * scaleFactor;
		matrix.M13 = matrix1.M13 * scaleFactor;
		matrix.M14 = matrix1.M14 * scaleFactor;

		matrix.M21 = matrix1.M21 * scaleFactor;
		matrix.M22 = matrix1.M22 * scaleFactor;
		matrix.M23 = matrix1.M23 * scaleFactor;
		matrix.M24 = matrix1.M24 * scaleFactor;

		matrix.M31 = matrix1.M31 * scaleFactor;
		matrix.M32 = matrix1.M32 * scaleFactor;
		matrix.M33 = matrix1.M33 * scaleFactor;
		matrix.M34 = matrix1.M34 * scaleFactor;

		matrix.M41 = matrix1.M41 * scaleFactor;
		matrix.M42 = matrix1.M42 * scaleFactor;
		matrix.M43 = matrix1.M43 * scaleFactor;
		matrix.M44 = matrix1.M44 * scaleFactor;
		return matrix;
	}

	public static Matrix4 Negate(final Matrix4 matrix) {
		final Matrix4 result = new Matrix4();
		result.M11 = -matrix.M11;
		result.M12 = -matrix.M12;
		result.M13 = -matrix.M13;
		result.M14 = -matrix.M14;

		result.M21 = -matrix.M21;
		result.M22 = -matrix.M22;
		result.M23 = -matrix.M23;
		result.M24 = -matrix.M24;

		result.M31 = -matrix.M31;
		result.M32 = -matrix.M32;
		result.M33 = -matrix.M33;
		result.M34 = -matrix.M34;

		result.M41 = -matrix.M41;
		result.M42 = -matrix.M42;
		result.M43 = -matrix.M43;
		result.M44 = -matrix.M44;
		return result;
	}

	public static Matrix4 Subtract(final Matrix4 matrix1, final Matrix4 matrix2) {
		final Matrix4 matrix = new Matrix4();
		matrix.M11 = matrix1.M11 - matrix2.M11;
		matrix.M12 = matrix1.M12 - matrix2.M12;
		matrix.M13 = matrix1.M13 - matrix2.M13;
		matrix.M14 = matrix1.M14 - matrix2.M14;

		matrix.M21 = matrix1.M21 - matrix2.M21;
		matrix.M22 = matrix1.M22 - matrix2.M22;
		matrix.M23 = matrix1.M23 - matrix2.M23;
		matrix.M24 = matrix1.M24 - matrix2.M24;

		matrix.M31 = matrix1.M31 - matrix2.M31;
		matrix.M32 = matrix1.M32 - matrix2.M32;
		matrix.M33 = matrix1.M33 - matrix2.M33;
		matrix.M34 = matrix1.M34 - matrix2.M34;

		matrix.M41 = matrix1.M41 - matrix2.M41;
		matrix.M42 = matrix1.M42 - matrix2.M42;
		matrix.M43 = matrix1.M43 - matrix2.M43;
		matrix.M44 = matrix1.M44 - matrix2.M44;
		return matrix;
	}

	public static Matrix4 Transform(final Matrix4 value, final Quaternion rotation) {
		final Matrix4 matrix = new Matrix4();

		final var x2 = rotation.X + rotation.X;
		final var y2 = rotation.Y + rotation.Y;
		final var z2 = rotation.Z + rotation.Z;

		final var a = 1.0f - rotation.Y * y2 - rotation.Z * z2;
		final var b = rotation.X * y2 - rotation.W * z2;
		final var c = rotation.X * z2 + rotation.W * y2;
		final var d = rotation.X * y2 + rotation.W * z2;
		final var e = 1.0f - rotation.X * x2 - rotation.Z * z2;
		final var f = rotation.Y * z2 - rotation.W * x2;
		final var g = rotation.X * z2 - rotation.W * y2;
		final var h = rotation.Y * z2 + rotation.W * x2;
		final var i = 1.0f - rotation.X * x2 - rotation.Y * y2;

		matrix.M11 = value.M11 * a + value.M12 * b + value.M13 * c;
		matrix.M12 = value.M11 * d + value.M12 * e + value.M13 * f;
		matrix.M13 = value.M11 * g + value.M12 * h + value.M13 * i;
		matrix.M14 = value.M14;

		matrix.M21 = value.M21 * a + value.M22 * b + value.M23 * c;
		matrix.M22 = value.M21 * d + value.M22 * e + value.M23 * f;
		matrix.M23 = value.M21 * g + value.M22 * h + value.M23 * i;
		matrix.M24 = value.M24;

		matrix.M31 = value.M31 * a + value.M32 * b + value.M33 * c;
		matrix.M32 = value.M31 * d + value.M32 * e + value.M33 * f;
		matrix.M33 = value.M31 * g + value.M32 * h + value.M33 * i;
		matrix.M34 = value.M34;

		matrix.M41 = value.M41 * a + value.M42 * b + value.M43 * c;
		matrix.M42 = value.M41 * d + value.M42 * e + value.M43 * f;
		matrix.M43 = value.M41 * g + value.M42 * h + value.M43 * i;
		matrix.M44 = value.M44;

		return matrix;
	}

	public static Matrix4 Transpose(final Matrix4 matrix) {
		final Matrix4 result = new Matrix4();

		result.M11 = matrix.M11;
		result.M12 = matrix.M21;
		result.M13 = matrix.M31;
		result.M14 = matrix.M41;

		result.M21 = matrix.M12;
		result.M22 = matrix.M22;
		result.M23 = matrix.M32;
		result.M24 = matrix.M42;

		result.M31 = matrix.M13;
		result.M32 = matrix.M23;
		result.M33 = matrix.M33;
		result.M34 = matrix.M43;

		result.M41 = matrix.M14;
		result.M42 = matrix.M24;
		result.M43 = matrix.M34;
		result.M44 = matrix.M44;

		return result;
	}

	public static Matrix4 Inverse3x3(final Matrix4 matrix) throws Exception {
		if (0.0f == matrix.Determinant3x3())
			throw new Exception("Singular matrix inverse not possible");

		return Divide(matrix, matrix.Determinant3x3());
	}

	public static Matrix4 Adjoint3x3(final Matrix4 matrix) {
		var adjointMatrix = new Matrix4();
		for (var i = 0; 4 > i; i++)
			for (var j = 0; 4 > j; j++)
				adjointMatrix.set(new Vector2i(i, j), MathF.Pow(-1, i + j) * Matrix4.Minor(matrix, i, j).Determinant3x3());

		adjointMatrix = Matrix4.Transpose(adjointMatrix);
		return adjointMatrix;
	}

	public void set(final Vector2i point, final float value) {
		try {
			this.setValue(point.X, point.Y, value);
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}

	public float get(final Vector2i point) {
		try {
			return this.getValue(point.X, point.Y);
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void set(final int row, final Quaternion value) throws Exception {
		switch (row) {
			case 0:
				this.M11 = value.X;
				this.M12 = value.Y;
				this.M13 = value.Z;
				this.M14 = value.W;
				break;
			case 1:
				this.M21 = value.X;
				this.M22 = value.Y;
				this.M23 = value.Z;
				this.M24 = value.W;
				break;
			case 2:
				this.M31 = value.X;
				this.M32 = value.Y;
				this.M33 = value.Z;
				this.M34 = value.W;
				break;
			case 3:
				this.M41 = value.X;
				this.M42 = value.Y;
				this.M43 = value.Z;
				this.M44 = value.W;
				break;
			default:
				throw new Exception("Matrix4 row index must be from 0-3");
		}
	}

	public Quaternion get(final int row) throws Exception {
		switch (row) {
			case 0:
				return new Quaternion(this.M11, this.M12, this.M13, this.M14);
			case 1:
				return new Quaternion(this.M21, this.M22, this.M23, this.M24);
			case 2:
				return new Quaternion(this.M31, this.M32, this.M33, this.M34);
			case 3:
				return new Quaternion(this.M41, this.M42, this.M43, this.M44);
			default:
				throw new Exception("Matrix4 row index must be from 0-3");
		}
	}


	private float getValue(final int row, final int column) throws Exception {
		switch (row) {
			case 0:
				switch (column) {
					case 0:
						return this.M11;
					case 1:
						return this.M12;
					case 2:
						return this.M13;
					case 3:
						return this.M14;
					default:
						throw new Exception("Matrix4 row and column values must be from 0-3");
				}
			case 1:
				switch (column) {
					case 0:
						return this.M21;
					case 1:
						return this.M22;
					case 2:
						return this.M23;
					case 3:
						return this.M24;
					default:
						throw new Exception("Matrix4 row and column values must be from 0-3");
				}
			case 2:
				switch (column) {
					case 0:
						return this.M31;
					case 1:
						return this.M32;
					case 2:
						return this.M33;
					case 3:
						return this.M34;
					default:
						throw new Exception("Matrix4 row and column values must be from 0-3");
				}
			case 3:
				switch (column) {
					case 0:
						return this.M41;
					case 1:
						return this.M42;
					case 2:
						return this.M43;
					case 3:
						return this.M44;
					default:
						throw new Exception("Matrix4 row and column values must be from 0-3");
				}
			default:
				throw new Exception("Matrix4 row and column values must be from 0-3");
		}
	}

	private void setValue(final int row, final int column, final float value) throws Exception {
		switch (row) {
			case 0:
				switch (column) {
					case 0:
						this.M11 = value;
						return;
					case 1:
						this.M12 = value;
						return;
					case 2:
						this.M13 = value;
						return;
					case 3:
						this.M14 = value;
						return;
					default:
						throw new Exception("Matrix4 row and column values must be from 0-3");
				}
			case 1:
				switch (column) {
					case 0:
						this.M21 = value;
						return;
					case 1:
						this.M22 = value;
						return;
					case 2:
						this.M23 = value;
						return;
					case 3:
						this.M24 = value;
						return;
					default:
						throw new Exception("Matrix4 row and column values must be from 0-3");
				}
			case 2:
				switch (column) {
					case 0:
						this.M31 = value;
						return;
					case 1:
						this.M32 = value;
						return;
					case 2:
						this.M33 = value;
						return;
					case 3:
						this.M34 = value;
						return;
					default:
						throw new Exception("Matrix4 row and column values must be from 0-3");
				}
			case 3:
				switch (column) {
					case 0:
						this.M41 = value;
						return;
					case 1:
						this.M42 = value;
						return;
					case 2:
						this.M43 = value;
						return;
					case 3:
						this.M44 = value;
						return;
					default:
						throw new Exception("Matrix4 row and column values must be from 0-3");
				}
			default:
				throw new Exception("Matrix4 row and column values must be from 0-3");
		}
	}

	public static Matrix4 Inverse(final Matrix4 matrix) throws Exception {
		if (0.0f == matrix.Determinant())
			throw new Exception("Singular matrix inverse not possible");

		return Divide(Matrix4.Adjoint(matrix), matrix.Determinant());
	}

	public static Matrix4 Adjoint(final Matrix4 matrix) {
		var adjointMatrix = new Matrix4();
		for (var i = 0; 4 > i; i++)
			for (var j = 0; 4 > j; j++)
				adjointMatrix.set(new Vector2i(i, j), MathF.Pow(-1, i + j) * Matrix4.Minor(matrix, i, j).Determinant3x3());

		adjointMatrix = Matrix4.Transpose(adjointMatrix);
		return adjointMatrix;
	}

	public static Matrix4 Minor(final Matrix4 matrix, final int row, final int col) {
		final var minor = new Matrix4();
		int m = 0, n = 0;

		for (var i = 0; 4 > i; i++) {
			if (i == row)
				continue;
			n = 0;
			for (var j = 0; 4 > j; j++) {
				if (j == col)
					continue;

				minor.set(new Vector2i(m, n), matrix.get(new Vector2i(i, j)));
				n++;
			}

			m++;
		}

		return minor;
	}

}
