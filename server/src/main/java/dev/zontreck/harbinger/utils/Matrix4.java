package dev.zontreck.harbinger.utils;

import dev.zontreck.harbinger.simulator.types.Quaternion;
import dev.zontreck.harbinger.simulator.types.Vector2i;
import dev.zontreck.harbinger.simulator.types.Vector3;

public class Matrix4 {
	/// <summary>A 4x4 matrix containing all zeroes</summary>
	public static final Matrix4 Zero = new Matrix4();

	/// <summary>A 4x4 identity matrix</summary>
	public static final Matrix4 Identity = new Matrix4(
			1f, 0f, 0f, 0f,
			0f, 1f, 0f, 0f,
			0f, 0f, 1f, 0f,
			0f, 0f, 0f, 1f);

	public float M11, M12, M13, M14;
	public float M21, M22, M23, M24;
	public float M31, M32, M33, M34;
	public float M41, M42, M43, M44;

	public Vector3 getAtAxis() {
		return new Vector3(M11, M21, M31);
	}

	public void setAtAxis(Vector3 vect) {
		M11 = vect.X;
		M21 = vect.Y;
		M31 = vect.Z;
	}

	public Vector3 getLeftAxis() {
		return new Vector3(M12, M22, M32);
	}

	public void setLeftAxis(Vector3 axis) {
		M12 = axis.X;
		M22 = axis.Y;
		M32 = axis.Z;
	}

	public Vector3 getUpAxis() {
		return new Vector3(M13, M23, M33);
	}

	public void setUpAxis(Vector3 axis) {
		M13 = axis.X;
		M23 = axis.Y;
		M33 = axis.Z;
	}


	public Matrix4(
			float m11, float m12, float m13, float m14,
			float m21, float m22, float m23, float m24,
			float m31, float m32, float m33, float m34,
			float m41, float m42, float m43, float m44) {
		M11 = m11;
		M12 = m12;
		M13 = m13;
		M14 = m14;

		M21 = m21;
		M22 = m22;
		M23 = m23;
		M24 = m24;

		M31 = m31;
		M32 = m32;
		M33 = m33;
		M34 = m34;

		M41 = m41;
		M42 = m42;
		M43 = m43;
		M44 = m44;
	}

	public Matrix4() {

	}

	public Matrix4(float roll, float pitch, float yaw) {
		initFromEulers(roll, pitch, yaw);

	}


	public Matrix4(Matrix4 m) {
		M11 = m.M11;
		M12 = m.M12;
		M13 = m.M13;
		M14 = m.M14;

		M21 = m.M21;
		M22 = m.M22;
		M23 = m.M23;
		M24 = m.M24;

		M31 = m.M31;
		M32 = m.M32;
		M33 = m.M33;
		M34 = m.M34;

		M41 = m.M41;
		M42 = m.M42;
		M43 = m.M43;
		M44 = m.M44;
	}


	public float Determinant() {
		return
				M14 * M23 * M32 * M41 - M13 * M24 * M32 * M41 - M14 * M22 * M33 * M41 + M12 * M24 * M33 * M41 +
						M13 * M22 * M34 * M41 - M12 * M23 * M34 * M41 - M14 * M23 * M31 * M42 + M13 * M24 * M31 * M42 +
						M14 * M21 * M33 * M42 - M11 * M24 * M33 * M42 - M13 * M21 * M34 * M42 + M11 * M23 * M34 * M42 +
						M14 * M22 * M31 * M43 - M12 * M24 * M31 * M43 - M14 * M21 * M32 * M43 + M11 * M24 * M32 * M43 +
						M12 * M21 * M34 * M43 - M11 * M22 * M34 * M43 - M13 * M22 * M31 * M44 + M12 * M23 * M31 * M44 +
						M13 * M21 * M32 * M44 - M11 * M23 * M32 * M44 - M12 * M21 * M33 * M44 + M11 * M22 * M33 * M44;
	}

	public float Determinant3x3() {
		var det = 0f;

		var diag1 = M11 * M22 * M33;
		var diag2 = M12 * M23 * M31;
		var diag3 = M13 * M21 * M32;
		var diag4 = M31 * M22 * M13;
		var diag5 = M32 * M23 * M11;
		var diag6 = M33 * M21 * M12;

		det = diag1 + diag2 + diag3 - (diag4 + diag5 + diag6);

		return det;
	}

	public float Trace() {
		return M11 + M22 + M33 + M44;
	}

	public Vector3 GetEulerAngles(float roll, float pitch, float yaw) {
		float angleX, angleY, angleZ;
		float cx, cy, cz; // cosines
		float sx, sz; // sines

		angleY = MathF.Asin(MathF.Clamp(M13, -1f, 1f));
		cy = MathF.Cos(angleY);

		if (MathF.Abs(cy) > 0.005f) {
			// No gimbal lock
			cx = M33 / cy;
			sx = -M23 / cy;

			angleX = MathF.Atan2(sx, cx);

			cz = M11 / cy;
			sz = -M12 / cy;

			angleZ = MathF.Atan2(sz, cz);
		} else {
			// Gimbal lock
			angleX = 0;

			cz = M22;
			sz = M21;

			angleZ = MathF.Atan2(sz, cz);
		}

		// Return only positive angles in [0,360]
		if (angleX < 0) angleX += 360f;
		if (angleY < 0) angleY += 360f;
		if (angleZ < 0) angleZ += 360f;

		roll = angleX;
		pitch = angleY;
		yaw = angleZ;

		return new Vector3(roll, pitch, yaw);
	}

	public Quaternion GetQuaternion() {
		var quat = new Quaternion();
		var trace = Trace() + 1f;

		if (trace > 1E-6F) {
			var s = 0.5f / MathF.Sqrt(trace);

			quat.X = (M32 - M23) * s;
			quat.Y = (M13 - M31) * s;
			quat.Z = (M21 - M12) * s;
			quat.W = 0.25f / s;
		} else {
			if (M11 > M22 && M11 > M33) {
				var s = 2.0f * MathF.Sqrt(1.0f + M11 - M22 - M33);

				quat.X = 0.25f * s;
				quat.Y = (M12 + M21) / s;
				quat.Z = (M13 + M31) / s;
				quat.W = (M23 - M32) / s;
			} else if (M22 > M33) {
				var s = 2.0f * MathF.Sqrt(1.0f + M22 - M11 - M33);

				quat.X = (M12 + M21) / s;
				quat.Y = 0.25f * s;
				quat.Z = (M23 + M32) / s;
				quat.W = (M13 - M31) / s;
			} else {
				var s = 2.0f * MathF.Sqrt(1.0f + M33 - M11 - M22);

				quat.X = (M13 + M31) / s;
				quat.Y = (M23 + M32) / s;
				quat.Z = 0.25f * s;
				quat.W = (M12 - M21) / s;
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


	public static Matrix4 Add(Matrix4 matrix1, Matrix4 matrix2) {
		Matrix4 matrix = new Matrix4();
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

	public static Matrix4 CreateFromAxisAngle(Vector3 axis, float angle) {
		var matrix = new Matrix4();

		var x = axis.X;
		var y = axis.Y;
		var z = axis.Z;
		var sin = MathF.Sin(angle);
		var cos = MathF.Cos(angle);
		var xx = x * x;
		var yy = y * y;
		var zz = z * z;
		var xy = x * y;
		var xz = x * z;
		var yz = y * z;

		matrix.M11 = xx + cos * (1f - xx);
		matrix.M12 = xy - cos * xy + sin * z;
		matrix.M13 = xz - cos * xz - sin * y;
		//matrix.M14 = 0f;

		matrix.M21 = xy - cos * xy - sin * z;
		matrix.M22 = yy + cos * (1f - yy);
		matrix.M23 = yz - cos * yz + sin * x;
		//matrix.M24 = 0f;

		matrix.M31 = xz - cos * xz + sin * y;
		matrix.M32 = yz - cos * yz - sin * x;
		matrix.M33 = zz + cos * (1f - zz);
		//matrix.M34 = 0f;

		//matrix.M41 = matrix.M42 = matrix.M43 = 0f;
		matrix.M44 = 1f;

		return matrix;
	}

	/// <summary>
	///     Construct a matrix from euler rotation values in radians
	/// </summary>
	/// <param name="roll">X euler angle in radians</param>
	/// <param name="pitch">Y euler angle in radians</param>
	/// <param name="yaw">Z euler angle in radians</param>
	public static Matrix4 CreateFromEulers(float roll, float pitch, float yaw) {
		Matrix4 m = new Matrix4();
		m.initFromEulers(roll, pitch, yaw);

		return m;
	}

	private void initFromEulers(float roll, float pitch, float yaw) {

		float a, b, c, d, e, f;
		float ad, bd;

		a = MathF.Cos(roll);
		b = MathF.Sin(roll);
		c = MathF.Cos(pitch);
		d = MathF.Sin(pitch);
		e = MathF.Cos(yaw);
		f = MathF.Sin(yaw);

		ad = a * d;
		bd = b * d;

		M11 = c * e;
		M12 = -c * f;
		M13 = d;
		M14 = 0f;

		M21 = bd * e + a * f;
		M22 = -bd * f + a * e;
		M23 = -b * c;
		M24 = 0f;

		M31 = -ad * e + b * f;
		M32 = ad * f + b * e;
		M33 = a * c;
		M34 = 0f;

		M41 = M42 = M43 = 0f;
		M44 = 1f;
	}

	public static Matrix4 CreateFromQuaternion(Quaternion rot) {
		Matrix4 matrix = new Matrix4();

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

		matrix.M11 = 1.0f - yy2 - zz2;
		matrix.M12 = xy2 - wz2;
		matrix.M13 = xz2 + wy2;
		matrix.M14 = 0f;

		matrix.M21 = xy2 + wz2;
		matrix.M22 = 1.0f - xx2 - zz2;
		matrix.M23 = yz2 - wx2;
		matrix.M24 = 0f;

		matrix.M31 = xz2 - wy2;
		matrix.M32 = yz2 + wx2;
		matrix.M33 = 1.0f - xx2 - yy2;
		matrix.M34 = 0f;

		matrix.M43 = matrix.M42 = matrix.M41 = 0f;
		matrix.M44 = 1f;

		return matrix;
	}

	public static Matrix4 CreateLookAt(Vector3 cameraPosition, Vector3 cameraTarget, Vector3 cameraUpVector) {
		Matrix4 matrix = new Matrix4();

		var z = Vector3.Normalize(Vector3.Subtract(cameraPosition, cameraTarget));
		var x = Vector3.Normalize(Vector3.Cross(cameraUpVector, z));
		var y = Vector3.Cross(z, x);

		matrix.M11 = x.X;
		matrix.M12 = y.X;
		matrix.M13 = z.X;
		matrix.M14 = 0f;

		matrix.M21 = x.Y;
		matrix.M22 = y.Y;
		matrix.M23 = z.Y;
		matrix.M24 = 0f;

		matrix.M31 = x.Z;
		matrix.M32 = y.Z;
		matrix.M33 = z.Z;
		matrix.M34 = 0f;

		matrix.M41 = -Vector3.Dot(x, cameraPosition);
		matrix.M42 = -Vector3.Dot(y, cameraPosition);
		matrix.M43 = -Vector3.Dot(z, cameraPosition);
		matrix.M44 = 1f;

		return matrix;
	}

	public static Matrix4 CreateRotationX(float radians) {
		Matrix4 matrix = new Matrix4();

		var cos = MathF.Cos(radians);
		var sin = MathF.Sin(radians);

		matrix.M11 = 1f;
		matrix.M12 = 0f;
		matrix.M13 = 0f;
		matrix.M14 = 0f;

		matrix.M21 = 0f;
		matrix.M22 = cos;
		matrix.M23 = sin;
		matrix.M24 = 0f;

		matrix.M31 = 0f;
		matrix.M32 = -sin;
		matrix.M33 = cos;
		matrix.M34 = 0f;

		matrix.M41 = 0f;
		matrix.M42 = 0f;
		matrix.M43 = 0f;
		matrix.M44 = 1f;

		return matrix;
	}

	public static Matrix4 CreateRotationY(float radians) {
		Matrix4 matrix = new Matrix4();

		var cos = MathF.Cos(radians);
		var sin = MathF.Sin(radians);

		matrix.M11 = cos;
		matrix.M12 = 0f;
		matrix.M13 = -sin;
		matrix.M14 = 0f;

		matrix.M21 = 0f;
		matrix.M22 = 1f;
		matrix.M23 = 0f;
		matrix.M24 = 0f;

		matrix.M31 = sin;
		matrix.M32 = 0f;
		matrix.M33 = cos;
		matrix.M34 = 0f;

		matrix.M41 = 0f;
		matrix.M42 = 0f;
		matrix.M43 = 0f;
		matrix.M44 = 1f;

		return matrix;
	}

	public static Matrix4 CreateRotationZ(float radians) {
		Matrix4 matrix = new Matrix4();

		var cos = MathF.Cos(radians);
		var sin = MathF.Sin(radians);

		matrix.M11 = cos;
		matrix.M12 = sin;
		matrix.M13 = 0f;
		matrix.M14 = 0f;

		matrix.M21 = -sin;
		matrix.M22 = cos;
		matrix.M23 = 0f;
		matrix.M24 = 0f;

		matrix.M31 = 0f;
		matrix.M32 = 0f;
		matrix.M33 = 1f;
		matrix.M34 = 0f;

		matrix.M41 = 0f;
		matrix.M42 = 0f;
		matrix.M43 = 0f;
		matrix.M44 = 1f;

		return matrix;
	}

	public static Matrix4 CreateScale(Vector3 scale) {
		Matrix4 matrix = new Matrix4();

		matrix.M11 = scale.X;
		matrix.M12 = 0f;
		matrix.M13 = 0f;
		matrix.M14 = 0f;

		matrix.M21 = 0f;
		matrix.M22 = scale.Y;
		matrix.M23 = 0f;
		matrix.M24 = 0f;

		matrix.M31 = 0f;
		matrix.M32 = 0f;
		matrix.M33 = scale.Z;
		matrix.M34 = 0f;

		matrix.M41 = 0f;
		matrix.M42 = 0f;
		matrix.M43 = 0f;
		matrix.M44 = 1f;

		return matrix;
	}

	public static Matrix4 CreateTranslation(Vector3 position) {
		Matrix4 matrix = new Matrix4();

		matrix.M11 = 1f;
		matrix.M12 = 0f;
		matrix.M13 = 0f;
		matrix.M14 = 0f;

		matrix.M21 = 0f;
		matrix.M22 = 1f;
		matrix.M23 = 0f;
		matrix.M24 = 0f;

		matrix.M31 = 0f;
		matrix.M32 = 0f;
		matrix.M33 = 1f;
		matrix.M34 = 0f;

		matrix.M41 = position.X;
		matrix.M42 = position.Y;
		matrix.M43 = position.Z;
		matrix.M44 = 1f;

		return matrix;
	}

	public static Matrix4 CreateWorld(Vector3 position, Vector3 forward, Vector3 up) {
		Matrix4 result = new Matrix4();

		// Normalize forward vector
		forward.Normalize();

		// Calculate right vector
		var right = Vector3.Cross(forward, up);
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

	public static Matrix4 Divide(Matrix4 matrix1, Matrix4 matrix2) {
		Matrix4 matrix = new Matrix4();

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

	public static Matrix4 Divide(Matrix4 matrix1, float divider) {
		Matrix4 matrix = new Matrix4();

		var oodivider = 1f / divider;
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

	public static Matrix4 Lerp(Matrix4 matrix1, Matrix4 matrix2, float amount) {
		Matrix4 matrix = new Matrix4();

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

	public static Matrix4 Multiply(Matrix4 matrix1, Matrix4 matrix2) {
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

	public static Matrix4 Multiply(Matrix4 matrix1, float scaleFactor) {
		Matrix4 matrix = new Matrix4();
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

	public static Matrix4 Negate(Matrix4 matrix) {
		Matrix4 result = new Matrix4();
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

	public static Matrix4 Subtract(Matrix4 matrix1, Matrix4 matrix2) {
		Matrix4 matrix = new Matrix4();
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

	public static Matrix4 Transform(Matrix4 value, Quaternion rotation) {
		Matrix4 matrix = new Matrix4();

		var x2 = rotation.X + rotation.X;
		var y2 = rotation.Y + rotation.Y;
		var z2 = rotation.Z + rotation.Z;

		var a = 1f - rotation.Y * y2 - rotation.Z * z2;
		var b = rotation.X * y2 - rotation.W * z2;
		var c = rotation.X * z2 + rotation.W * y2;
		var d = rotation.X * y2 + rotation.W * z2;
		var e = 1f - rotation.X * x2 - rotation.Z * z2;
		var f = rotation.Y * z2 - rotation.W * x2;
		var g = rotation.X * z2 - rotation.W * y2;
		var h = rotation.Y * z2 + rotation.W * x2;
		var i = 1f - rotation.X * x2 - rotation.Y * y2;

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

	public static Matrix4 Transpose(Matrix4 matrix) {
		Matrix4 result = new Matrix4();

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

	public static Matrix4 Inverse3x3(Matrix4 matrix) throws Exception {
		if (matrix.Determinant3x3() == 0f)
			throw new Exception("Singular matrix inverse not possible");

		return Matrix4.Divide(matrix, matrix.Determinant3x3());
	}

	public static Matrix4 Adjoint3x3(Matrix4 matrix) {
		var adjointMatrix = new Matrix4();
		for (var i = 0; i < 4; i++)
			for (var j = 0; j < 4; j++)
				adjointMatrix.set(new Vector2i(i, j), MathF.Pow(-1, i + j) * Minor(matrix, i, j).Determinant3x3());

		adjointMatrix = Transpose(adjointMatrix);
		return adjointMatrix;
	}

	public void set(Vector2i point, float value) {
		try {
			setValue(point.X, point.Y, value);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public float get(Vector2i point) {
		try {
			return getValue(point.X, point.Y);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void set(int row, Quaternion value) throws Exception {
		switch (row) {
			case 0:
				M11 = value.X;
				M12 = value.Y;
				M13 = value.Z;
				M14 = value.W;
				break;
			case 1:
				M21 = value.X;
				M22 = value.Y;
				M23 = value.Z;
				M24 = value.W;
				break;
			case 2:
				M31 = value.X;
				M32 = value.Y;
				M33 = value.Z;
				M34 = value.W;
				break;
			case 3:
				M41 = value.X;
				M42 = value.Y;
				M43 = value.Z;
				M44 = value.W;
				break;
			default:
				throw new Exception("Matrix4 row index must be from 0-3");
		}
	}

	public Quaternion get(int row) throws Exception {
		switch (row) {
			case 0:
				return new Quaternion(M11, M12, M13, M14);
			case 1:
				return new Quaternion(M21, M22, M23, M24);
			case 2:
				return new Quaternion(M31, M32, M33, M34);
			case 3:
				return new Quaternion(M41, M42, M43, M44);
			default:
				throw new Exception("Matrix4 row index must be from 0-3");
		}
	}


	private float getValue(int row, int column) throws Exception {
		switch (row) {
			case 0:
				switch (column) {
					case 0:
						return M11;
					case 1:
						return M12;
					case 2:
						return M13;
					case 3:
						return M14;
					default:
						throw new Exception("Matrix4 row and column values must be from 0-3");
				}
			case 1:
				switch (column) {
					case 0:
						return M21;
					case 1:
						return M22;
					case 2:
						return M23;
					case 3:
						return M24;
					default:
						throw new Exception("Matrix4 row and column values must be from 0-3");
				}
			case 2:
				switch (column) {
					case 0:
						return M31;
					case 1:
						return M32;
					case 2:
						return M33;
					case 3:
						return M34;
					default:
						throw new Exception("Matrix4 row and column values must be from 0-3");
				}
			case 3:
				switch (column) {
					case 0:
						return M41;
					case 1:
						return M42;
					case 2:
						return M43;
					case 3:
						return M44;
					default:
						throw new Exception("Matrix4 row and column values must be from 0-3");
				}
			default:
				throw new Exception("Matrix4 row and column values must be from 0-3");
		}
	}

	private void setValue(int row, int column, float value) throws Exception {
		switch (row) {
			case 0:
				switch (column) {
					case 0:
						M11 = value;
						return;
					case 1:
						M12 = value;
						return;
					case 2:
						M13 = value;
						return;
					case 3:
						M14 = value;
						return;
					default:
						throw new Exception("Matrix4 row and column values must be from 0-3");
				}
			case 1:
				switch (column) {
					case 0:
						M21 = value;
						return;
					case 1:
						M22 = value;
						return;
					case 2:
						M23 = value;
						return;
					case 3:
						M24 = value;
						return;
					default:
						throw new Exception("Matrix4 row and column values must be from 0-3");
				}
			case 2:
				switch (column) {
					case 0:
						M31 = value;
						return;
					case 1:
						M32 = value;
						return;
					case 2:
						M33 = value;
						return;
					case 3:
						M34 = value;
						return;
					default:
						throw new Exception("Matrix4 row and column values must be from 0-3");
				}
			case 3:
				switch (column) {
					case 0:
						M41 = value;
						return;
					case 1:
						M42 = value;
						return;
					case 2:
						M43 = value;
						return;
					case 3:
						M44 = value;
						return;
					default:
						throw new Exception("Matrix4 row and column values must be from 0-3");
				}
			default:
				throw new Exception("Matrix4 row and column values must be from 0-3");
		}
	}

	public static Matrix4 Inverse(Matrix4 matrix) throws Exception {
		if (matrix.Determinant() == 0f)
			throw new Exception("Singular matrix inverse not possible");

		return Matrix4.Divide(Adjoint(matrix), matrix.Determinant());
	}

	public static Matrix4 Adjoint(Matrix4 matrix) {
		var adjointMatrix = new Matrix4();
		for (var i = 0; i < 4; i++)
			for (var j = 0; j < 4; j++)
				adjointMatrix.set(new Vector2i(i, j), MathF.Pow(-1, i + j) * Minor(matrix, i, j).Determinant3x3());

		adjointMatrix = Transpose(adjointMatrix);
		return adjointMatrix;
	}

	public static Matrix4 Minor(Matrix4 matrix, int row, int col) {
		var minor = new Matrix4();
		int m = 0, n = 0;

		for (var i = 0; i < 4; i++) {
			if (i == row)
				continue;
			n = 0;
			for (var j = 0; j < 4; j++) {
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
