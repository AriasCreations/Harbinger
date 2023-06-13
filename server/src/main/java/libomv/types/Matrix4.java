/**
 * Copyright (c) 2006-2014, openmetaverse.org
 * Copyright (c) 2009-2017, Frederick Martian
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * - Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 * - Neither the name of the openmetaverse.org or libomv-java project nor the
 *   names of its contributors may be used to endorse or promote products derived
 *   from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package libomv.types;

import libomv.utils.Helpers;
import libomv.utils.RefObject;

public final class Matrix4
{
	public float M11, M12, M13, M14;
	public float M21, M22, M23, M24;
	public float M31, M32, M33, M34;
	public float M41, M42, M43, M44;

	public Vector3 getAtAxis()
	{
		return new Vector3(this.M11, this.M21, this.M31);
	}

	public void setAtAxis(final Vector3 value)
	{
		this.M11 = value.X;
		this.M21 = value.Y;
		this.M31 = value.Z;
	}

	public Vector3 getLeftAxis()
	{
		return new Vector3(this.M12, this.M22, this.M32);
	}

	public void setLeftAxis(final Vector3 value)
	{
		this.M12 = value.X;
		this.M22 = value.Y;
		this.M32 = value.Z;
	}

	public Vector3 getUpAxis()
	{
		return new Vector3(this.M13, this.M23, this.M33);
	}

	public void setUpAxis(final Vector3 value)
	{
		this.M13 = value.X;
		this.M23 = value.Y;
		this.M33 = value.Z;
	}

	public Matrix4()
	{
		this.M11 = this.M12 = this.M13 = this.M14 = 0.0f;
		this.M21 = this.M22 = this.M23 = this.M24 = 0.0f;
		this.M31 = this.M32 = this.M33 = this.M34 = 0.0f;
		this.M41 = this.M42 = this.M43 = this.M44 = 0.0f;
	}

	public Matrix4(final float m11, final float m12, final float m13, final float m14, final float m21, final float m22, final float m23, final float m24, final float m31,
				   final float m32, final float m33, final float m34, final float m41, final float m42, final float m43, final float m44)
	{
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

	public Matrix4(final float roll, final float pitch, final float yaw)
	{
		final Matrix4 m = Matrix4.createFromEulers(roll, pitch, yaw);
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

	public Matrix4(final Matrix4 m)
	{
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

	public float determinant()
	{
		return this.M14 * this.M23 * this.M32 * this.M41 - this.M13 * this.M24 * this.M32 * this.M41 - this.M14 * this.M22 * this.M33 * this.M41 + this.M12 * this.M24 * this.M33 * this.M41 + this.M13
				* this.M22 * this.M34 * this.M41 - this.M12 * this.M23 * this.M34 * this.M41 - this.M14 * this.M23 * this.M31 * this.M42 + this.M13 * this.M24 * this.M31 * this.M42 + this.M14 * this.M21
				* this.M33 * this.M42 - this.M11 * this.M24 * this.M33 * this.M42 - this.M13 * this.M21 * this.M34 * this.M42 + this.M11 * this.M23 * this.M34 * this.M42 + this.M14 * this.M22 * this.M31
				* this.M43 - this.M12 * this.M24 * this.M31 * this.M43 - this.M14 * this.M21 * this.M32 * this.M43 + this.M11 * this.M24 * this.M32 * this.M43 + this.M12 * this.M21 * this.M34 * this.M43
				- this.M11 * this.M22 * this.M34 * this.M43 - this.M13 * this.M22 * this.M31 * this.M44 + this.M12 * this.M23 * this.M31 * this.M44 + this.M13 * this.M21 * this.M32 * this.M44 - this.M11
				* this.M23 * this.M32 * this.M44 - this.M12 * this.M21 * this.M33 * this.M44 + this.M11 * this.M22 * this.M33 * this.M44;
	}

	public float determinant3x3()
	{
		float det = 0.0f;

		final float diag1 = this.M11 * this.M22 * this.M33;
		final float diag2 = this.M12 * this.M23 * this.M31;
		final float diag3 = this.M13 * this.M21 * this.M32;
		final float diag4 = this.M31 * this.M22 * this.M13;
		final float diag5 = this.M32 * this.M23 * this.M11;
		final float diag6 = this.M33 * this.M21 * this.M12;

		det = diag1 + diag2 + diag3 - (diag4 + diag5 + diag6);

		return det;
	}

	public float trace()
	{
		return this.M11 + this.M22 + this.M33 + this.M44;
	}

	/**
	 * Convert this matrix to euler rotations
	 * 
	 * @param roll
	 *            X euler angle
	 * @param pitch
	 *            Y euler angle
	 * @param yaw
	 *            Z euler angle
	 */
	public void getEulerAngles(final RefObject<Float> roll, final RefObject<Float> pitch, final RefObject<Float> yaw)
	{
		double angleX, angleY, angleZ;
		final double cx;  // cosines
		double cy;
		final double cz;
		final double sx;  // sines
		final double sz;

		angleY = Math.asin(Helpers.Clamp(this.M13, -1.0f, 1.0f));
		cy = Math.cos(angleY);

		if (0.005f < Math.abs(cy))
		{
			// No gimbal lock
			cx = this.M33 / cy;
			sx = (-this.M23) / cy;

			angleX = (float) Math.atan2(sx, cx);

			cz = this.M11 / cy;
			sz = (-this.M12) / cy;

			angleZ = (float) Math.atan2(sz, cz);
		}
		else
		{
			// Gimbal lock
			angleX = 0;

			cz = this.M22;
			sz = this.M21;

			angleZ = Math.atan2(sz, cz);
		}

		// Return only positive angles in [0, 2*Pi]
		if (0 > angleX)
		{
			angleX += Helpers.TWO_PI;
		}
		if (0 > angleY)
		{
			angleY += Helpers.TWO_PI;
		}
		if (0 > angleZ)
		{
			angleZ += Helpers.TWO_PI;
		}

		roll.argvalue = (float) angleX;
		pitch.argvalue = (float) angleY;
		yaw.argvalue = (float) angleZ;
	}

	/**
	 * Convert this matrix to a quaternion rotation
	 * 
	 * @return A quaternion representation of this rotation matrix
	 */
	public Quaternion getQuaternion()
	{
		return this.getQuaternion(new Quaternion());
	}
		
	/**
	 * Convert this matrix to a quaternion rotation
	 * 
	 * @param quaternion The quaternion to fill the information into
	 * @return A quaternion representation of this rotation matrix
	 */
	public Quaternion getQuaternion(final Quaternion quaternion)
	{
		final float trace = this.trace() + 1.0f;

		if (Helpers.FLOAT_MAG_THRESHOLD < trace)
		{
			final float s = 0.5f / (float) Math.sqrt(trace);

			quaternion.X = (this.M32 - this.M23) * s;
			quaternion.Y = (this.M13 - this.M31) * s;
			quaternion.Z = (this.M21 - this.M12) * s;
			quaternion.W = 0.25f / s;
		}
		else
		{
			if (this.M11 > this.M22 && this.M11 > this.M33)
			{
				final float s = 2.0f * (float) Math.sqrt(1.0f + this.M11 - this.M22 - this.M33);

				quaternion.X = 0.25f * s;
				quaternion.Y = (this.M12 + this.M21) / s;
				quaternion.Z = (this.M13 + this.M31) / s;
				quaternion.W = (this.M23 - this.M32) / s;
			}
			else if (this.M22 > this.M33)
			{
				final float s = 2.0f * (float) Math.sqrt(1.0f + this.M22 - this.M11 - this.M33);

				quaternion.X = (this.M12 + this.M21) / s;
				quaternion.Y = 0.25f * s;
				quaternion.Z = (this.M23 + this.M32) / s;
				quaternion.W = (this.M13 - this.M31) / s;
			}
			else
			{
				final float s = 2.0f * (float) Math.sqrt(1.0f + this.M33 - this.M11 - this.M22);

				quaternion.X = (this.M13 + this.M31) / s;
				quaternion.Y = (this.M23 + this.M32) / s;
				quaternion.Z = 0.25f * s;
				quaternion.W = (this.M12 - this.M21) / s;
			}
		}
		return quaternion;
	}

	public Vector3 getTranslation()
	{
		return new Vector3(this.M41, this.M42, this.M43);
	}
	
    public boolean decompose(final Vector3 scale, Quaternion rotation, final Vector3 translation)
    {
        translation.X = this.M41;
        translation.Y = this.M42;
        translation.Z = this.M43;

        final float xs = (0 > Math.signum(M11 * M12 * M13 * M14)) ? -1 : 1;
        final float ys = (0 > Math.signum(M21 * M22 * M23 * M24)) ? -1 : 1;
        final float zs = (0 > Math.signum(M31 * M32 * M33 * M34)) ? -1 : 1;

        scale.X = xs * (float)Math.sqrt(this.M11 * this.M11 + this.M12 * this.M12 + this.M13 * this.M13);
        scale.Y = ys * (float)Math.sqrt(this.M21 * this.M21 + this.M22 * this.M22 + this.M23 * this.M23);
        scale.Z = zs * (float)Math.sqrt(this.M31 * this.M31 + this.M32 * this.M32 + this.M33 * this.M33);

        if (0.0 == scale.X || 0.0 == scale.Y || 0.0 == scale.Z)
        {
            rotation = Quaternion.Identity;
            return false;
        }

        final Matrix4 m1 = new Matrix4(this.M11 / scale.X, this.M12 / scale.X, this.M13 / scale.X, 0,
				this.M21 / scale.Y, this.M22 / scale.Y, this.M23 / scale.Y, 0,
				this.M31 / scale.Z, this.M32 / scale.Z, this.M33 / scale.Z, 0,
                                 0, 0, 0, 1);

        rotation.setFromRotationMatrix(m1);
        return true;
    }	

    // #endregion Public Methods
	
    // #region Static Methods
	public static Matrix4 createFromAxisAngle(final Vector3 axis, final float angle)
	{
		final Matrix4 matrix = new Matrix4();

		final float x = axis.X;
		final float y = axis.Y;
		final float z = axis.Z;
		final float sin = (float) Math.sin(angle);
		final float cos = (float) Math.cos(angle);
		final float xx = x * x;
		final float yy = y * y;
		final float zz = z * z;
		final float xy = x * y;
		final float xz = x * z;
		final float yz = y * z;

		matrix.M11 = xx + (cos * (1.0f - xx));
		matrix.M12 = (xy - (cos * xy)) + (sin * z);
		matrix.M13 = (xz - (cos * xz)) - (sin * y);
		// matrix.M14 = 0f;

		matrix.M21 = (xy - (cos * xy)) - (sin * z);
		matrix.M22 = yy + (cos * (1.0f - yy));
		matrix.M23 = (yz - (cos * yz)) + (sin * x);
		// matrix.M24 = 0f;

		matrix.M31 = (xz - (cos * xz)) + (sin * y);
		matrix.M32 = (yz - (cos * yz)) - (sin * x);
		matrix.M33 = zz + (cos * (1.0f - zz));
		// matrix.M34 = 0f;

		// matrix.M41 = matrix.M42 = matrix.M43 = 0f;
		matrix.M44 = 1.0f;

		return matrix;
	}

	/**
	 * Construct a matrix from euler rotation values in radians
	 * 
	 * @param roll
	 *            X euler angle in radians
	 * @param pitch
	 *            Y euler angle in radians
	 * @param yaw
	 *            Z euler angle in radians
	 */
	public static Matrix4 createFromEulers(final float roll, final float pitch, final float yaw)
	{
		final Matrix4 m = new Matrix4();

		final float a;
		float b;
		float c;
		float d;
		float e;
		final float f;
		final float ad;
		final float bd;

		a = (float) Math.cos(roll);
		b = (float) Math.sin(roll);
		c = (float) Math.cos(pitch);
		d = (float) Math.sin(pitch);
		e = (float) Math.cos(yaw);
		f = (float) Math.sin(yaw);

		ad = a * d;
		bd = b * d;

		m.M11 = c * e;
		m.M12 = -c * f;
		m.M13 = d;
		m.M14 = 0.0f;

		m.M21 = bd * e + a * f;
		m.M22 = -bd * f + a * e;
		m.M23 = -b * c;
		m.M24 = 0.0f;

		m.M31 = -ad * e + b * f;
		m.M32 = ad * f + b * e;
		m.M33 = a * c;
		m.M34 = 0.0f;

		m.M41 = m.M42 = m.M43 = 0.0f;
		m.M44 = 1.0f;

		return m;
	}

	public static Matrix4 createFromQuaternion(final Quaternion quaternion)
	{
		final float xx = quaternion.X * quaternion.X;
		final float yy = quaternion.Y * quaternion.Y;
		final float zz = quaternion.Z * quaternion.Z;
		final float xy = quaternion.X * quaternion.Y;
		final float zw = quaternion.Z * quaternion.W;
		final float zx = quaternion.Z * quaternion.X;
		final float yw = quaternion.Y * quaternion.W;
		final float yz = quaternion.Y * quaternion.Z;
		final float xw = quaternion.X * quaternion.W;

		return new Matrix4(1.0f - (2.0f * (yy + zz)), 2.0f * (xy + zw), 2.0f * (zx - yw), 0.0f, 2.0f * (xy - zw),
				1.0f - (2.0f * (zz + xx)), 2.0f * (yz + xw), 0.0f, 2.0f * (zx + yw), 2.0f * (yz - xw), 1.0f - (2.0f * (yy + xx)), 0.0f,
				0.0f, 0.0f, 0.0f, 1.0f);
	}

	public static Matrix4 createLookAt(final Vector3 cameraPosition, final Vector3 cameraTarget, final Vector3 cameraUpVector)
	{

		final Vector3 z = Vector3.normalize(Vector3.subtract(cameraPosition, cameraTarget));
		final Vector3 x = Vector3.normalize(Vector3.cross(cameraUpVector, z));
		final Vector3 y = Vector3.cross(z, x);

		return new Matrix4(x.X, y.X, z.X, 0.0f, x.Y, y.Y, z.Y, 0.0f, x.Z, y.Z, z.Z, 0.0f, -Vector3.dot(x, cameraPosition),
				-Vector3.dot(y, cameraPosition), -Vector3.dot(z, cameraPosition), 1.0f);
	}

	public static Matrix4 createRotationX(final float radians)
	{
		final float cos = (float) Math.cos(radians);
		final float sin = (float) Math.sin(radians);

		return new Matrix4(1.0f, 0.0f, 0.0f, 0.0f, 0.0f, cos, sin, 0.0f, 0.0f, -sin, cos, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f);
	}

	public static Matrix4 createRotationY(final float radians)
	{
		final Matrix4 matrix = new Matrix4();

		final float cos = (float) Math.cos(radians);
		final float sin = (float) Math.sin(radians);

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

	public static Matrix4 createRotationZ(final float radians)
	{
		final Matrix4 matrix = new Matrix4();

		final float cos = (float) Math.cos(radians);
		final float sin = (float) Math.sin(radians);

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

	public static Matrix4 createScale(final Vector3 scale)
	{
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

	public static Matrix4 createTranslation(final Vector3 position)
	{
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

	public static Matrix4 createWorld(final Vector3 position, final Vector3 forward, Vector3 up)
	{
		final Matrix4 result = new Matrix4();

		// Normalize forward vector
		forward.normalize();

		// Calculate right vector
		final Vector3 right = Vector3.cross(forward, up);
		right.normalize();

		// Recalculate up vector
		up = Vector3.cross(right, forward);
		up.normalize();

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

	public static Matrix4 lerp(final Matrix4 matrix1, final Matrix4 matrix2, final float amount)
	{
		return new Matrix4(matrix1.M11 + ((matrix2.M11 - matrix1.M11) * amount), matrix1.M12
				+ ((matrix2.M12 - matrix1.M12) * amount), matrix1.M13 + ((matrix2.M13 - matrix1.M13) * amount),
				matrix1.M14 + ((matrix2.M14 - matrix1.M14) * amount),

				matrix1.M21 + ((matrix2.M21 - matrix1.M21) * amount), matrix1.M22
						+ ((matrix2.M22 - matrix1.M22) * amount), matrix1.M23 + ((matrix2.M23 - matrix1.M23) * amount),
				matrix1.M24 + ((matrix2.M24 - matrix1.M24) * amount),

				matrix1.M31 + ((matrix2.M31 - matrix1.M31) * amount), matrix1.M32
						+ ((matrix2.M32 - matrix1.M32) * amount), matrix1.M33 + ((matrix2.M33 - matrix1.M33) * amount),
				matrix1.M34 + ((matrix2.M34 - matrix1.M34) * amount),

				matrix1.M41 + ((matrix2.M41 - matrix1.M41) * amount), matrix1.M42
						+ ((matrix2.M42 - matrix1.M42) * amount), matrix1.M43 + ((matrix2.M43 - matrix1.M43) * amount),
				matrix1.M44 + ((matrix2.M44 - matrix1.M44) * amount));
	}

	public static Matrix4 negate(final Matrix4 matrix)
	{
		return new Matrix4(-matrix.M11, -matrix.M12, -matrix.M13, -matrix.M14, -matrix.M21, -matrix.M22, -matrix.M23,
				-matrix.M24, -matrix.M31, -matrix.M32, -matrix.M33, -matrix.M34, -matrix.M41, -matrix.M42, -matrix.M43,
				-matrix.M44);
	}

	public static Matrix4 add(final Matrix4 matrix1, final Matrix4 matrix2)
	{
		return new Matrix4(matrix1.M11 + matrix2.M11, matrix1.M12 + matrix2.M12, matrix1.M13 + matrix2.M13, matrix1.M14
				+ matrix2.M14, matrix1.M21 + matrix2.M21, matrix1.M22 + matrix2.M22, matrix1.M23 + matrix2.M23,
				matrix1.M24 + matrix2.M24, matrix1.M31 + matrix2.M31, matrix1.M32 + matrix2.M32, matrix1.M33
						+ matrix2.M33, matrix1.M34 + matrix2.M34, matrix1.M41 + matrix2.M41, matrix1.M42 + matrix2.M42,
				matrix1.M43 + matrix2.M43, matrix1.M44 + matrix2.M44);
	}

	public static Matrix4 subtract(final Matrix4 matrix1, final Matrix4 matrix2)
	{
		return new Matrix4(matrix1.M11 - matrix2.M11, matrix1.M12 - matrix2.M12, matrix1.M13 - matrix2.M13, matrix1.M14
				- matrix2.M14, matrix1.M21 - matrix2.M21, matrix1.M22 - matrix2.M22, matrix1.M23 - matrix2.M23,
				matrix1.M24 - matrix2.M24, matrix1.M31 - matrix2.M31, matrix1.M32 - matrix2.M32, matrix1.M33
						- matrix2.M33, matrix1.M34 - matrix2.M34, matrix1.M41 - matrix2.M41, matrix1.M42 - matrix2.M42,
				matrix1.M43 - matrix2.M43, matrix1.M44 - matrix2.M44);
	}

	public static Matrix4 multiply(final Matrix4 matrix1, final Matrix4 matrix2)
	{
		return new Matrix4(matrix1.M11 * matrix2.M11 + matrix1.M12 * matrix2.M21 + matrix1.M13 * matrix2.M31
				+ matrix1.M14 * matrix2.M41, matrix1.M11 * matrix2.M12 + matrix1.M12 * matrix2.M22 + matrix1.M13
				* matrix2.M32 + matrix1.M14 * matrix2.M42, matrix1.M11 * matrix2.M13 + matrix1.M12 * matrix2.M23
				+ matrix1.M13 * matrix2.M33 + matrix1.M14 * matrix2.M43, matrix1.M11 * matrix2.M14 + matrix1.M12
				* matrix2.M24 + matrix1.M13 * matrix2.M34 + matrix1.M14 * matrix2.M44, matrix1.M21 * matrix2.M11
				+ matrix1.M22 * matrix2.M21 + matrix1.M23 * matrix2.M31 + matrix1.M24 * matrix2.M41, matrix1.M21
				* matrix2.M12 + matrix1.M22 * matrix2.M22 + matrix1.M23 * matrix2.M32 + matrix1.M24 * matrix2.M42,
				matrix1.M21 * matrix2.M13 + matrix1.M22 * matrix2.M23 + matrix1.M23 * matrix2.M33 + matrix1.M24
						* matrix2.M43, matrix1.M21 * matrix2.M14 + matrix1.M22 * matrix2.M24 + matrix1.M23
						* matrix2.M34 + matrix1.M24 * matrix2.M44, matrix1.M31 * matrix2.M11 + matrix1.M32
						* matrix2.M21 + matrix1.M33 * matrix2.M31 + matrix1.M34 * matrix2.M41, matrix1.M31
						* matrix2.M12 + matrix1.M32 * matrix2.M22 + matrix1.M33 * matrix2.M32 + matrix1.M34
						* matrix2.M42, matrix1.M31 * matrix2.M13 + matrix1.M32 * matrix2.M23 + matrix1.M33
						* matrix2.M33 + matrix1.M34 * matrix2.M43, matrix1.M31 * matrix2.M14 + matrix1.M32
						* matrix2.M24 + matrix1.M33 * matrix2.M34 + matrix1.M34 * matrix2.M44, matrix1.M41
						* matrix2.M11 + matrix1.M42 * matrix2.M21 + matrix1.M43 * matrix2.M31 + matrix1.M44
						* matrix2.M41, matrix1.M41 * matrix2.M12 + matrix1.M42 * matrix2.M22 + matrix1.M43
						* matrix2.M32 + matrix1.M44 * matrix2.M42, matrix1.M41 * matrix2.M13 + matrix1.M42
						* matrix2.M23 + matrix1.M43 * matrix2.M33 + matrix1.M44 * matrix2.M43, matrix1.M41
						* matrix2.M14 + matrix1.M42 * matrix2.M24 + matrix1.M43 * matrix2.M34 + matrix1.M44
						* matrix2.M44);
	}

	public static Matrix4 multiply(final Matrix4 matrix1, final float scaleFactor)
	{
		return new Matrix4(matrix1.M11 * scaleFactor, matrix1.M12 * scaleFactor, matrix1.M13 * scaleFactor, matrix1.M14
				* scaleFactor, matrix1.M21 * scaleFactor, matrix1.M22 * scaleFactor, matrix1.M23 * scaleFactor,
				matrix1.M24 * scaleFactor, matrix1.M31 * scaleFactor, matrix1.M32 * scaleFactor, matrix1.M33
						* scaleFactor, matrix1.M34 * scaleFactor, matrix1.M41 * scaleFactor, matrix1.M42 * scaleFactor,
				matrix1.M43 * scaleFactor, matrix1.M44 * scaleFactor);
	}

	public static Matrix4 divide(final Matrix4 matrix1, final Matrix4 matrix2)
	{
		return new Matrix4(matrix1.M11 / matrix2.M11, matrix1.M12 / matrix2.M12, matrix1.M13 / matrix2.M13, matrix1.M14
				/ matrix2.M14, matrix1.M21 / matrix2.M21, matrix1.M22 / matrix2.M22, matrix1.M23 / matrix2.M23,
				matrix1.M24 / matrix2.M24, matrix1.M31 / matrix2.M31, matrix1.M32 / matrix2.M32, matrix1.M33
						/ matrix2.M33, matrix1.M34 / matrix2.M34, matrix1.M41 / matrix2.M41, matrix1.M42 / matrix2.M42,
				matrix1.M43 / matrix2.M43, matrix1.M44 / matrix2.M44);
	}

	public static Matrix4 divide(final Matrix4 matrix1, final float divider)
	{
		final float oodivider = 1.0f / divider;
		return new Matrix4(matrix1.M11 * oodivider, matrix1.M12 * oodivider, matrix1.M13 * oodivider, matrix1.M14
				* oodivider, matrix1.M21 * oodivider, matrix1.M22 * oodivider, matrix1.M23 * oodivider, matrix1.M24
				* oodivider, matrix1.M31 * oodivider, matrix1.M32 * oodivider, matrix1.M33 * oodivider, matrix1.M34
				* oodivider, matrix1.M41 * oodivider, matrix1.M42 * oodivider, matrix1.M43 * oodivider, matrix1.M44
				* oodivider);
	}

	public static Matrix4 transform(final Matrix4 value, final Quaternion rotation)
	{
		final float x2 = rotation.X + rotation.X;
		final float y2 = rotation.Y + rotation.Y;
		final float z2 = rotation.Z + rotation.Z;

		final float a = (1.0f - rotation.Y * y2) - rotation.Z * z2;
		final float b = rotation.X * y2 - rotation.W * z2;
		final float c = rotation.X * z2 + rotation.W * y2;
		final float d = rotation.X * y2 + rotation.W * z2;
		final float e = (1.0f - rotation.X * x2) - rotation.Z * z2;
		final float f = rotation.Y * z2 - rotation.W * x2;
		final float g = rotation.X * z2 - rotation.W * y2;
		final float h = rotation.Y * z2 + rotation.W * x2;
		final float i = (1.0f - rotation.X * x2) - rotation.Y * y2;

		return new Matrix4(((value.M11 * a) + (value.M12 * b)) + (value.M13 * c), ((value.M11 * d) + (value.M12 * e))
				+ (value.M13 * f), ((value.M11 * g) + (value.M12 * h)) + (value.M13 * i), value.M14,

		((value.M21 * a) + (value.M22 * b)) + (value.M23 * c), ((value.M21 * d) + (value.M22 * e)) + (value.M23 * f),
				((value.M21 * g) + (value.M22 * h)) + (value.M23 * i), value.M24,

				((value.M31 * a) + (value.M32 * b)) + (value.M33 * c), ((value.M31 * d) + (value.M32 * e))
						+ (value.M33 * f), ((value.M31 * g) + (value.M32 * h)) + (value.M33 * i), value.M34,

				((value.M41 * a) + (value.M42 * b)) + (value.M43 * c), ((value.M41 * d) + (value.M42 * e))
						+ (value.M43 * f), ((value.M41 * g) + (value.M42 * h)) + (value.M43 * i), value.M44);
	}

	public static Matrix4 transpose(final Matrix4 matrix)
	{
		return new Matrix4(matrix.M11, matrix.M21, matrix.M31, matrix.M41, matrix.M12, matrix.M22, matrix.M32,
				matrix.M42, matrix.M13, matrix.M23, matrix.M33, matrix.M43, matrix.M14, matrix.M24, matrix.M34,
				matrix.M44);
	}

	public static Matrix4 inverse3x3(final Matrix4 matrix) throws Exception
	{
		if (0.0f == matrix.determinant3x3())
		{
			throw new Exception("Singular matrix inverse not possible");
		}
		return (divide(Matrix4.adjoint3x3(matrix), matrix.determinant3x3()));
	}

	public static Matrix4 adjoint3x3(final Matrix4 matrix)
	{
		Matrix4 adjointMatrix = new Matrix4();
		for (int i = 0; 4 > i; i++)
		{
			for (int j = 0; 4 > j; j++)
			{
				adjointMatrix.setItem(i, j, (float) (Math.pow(-1, i + j) * (Matrix4.minor(matrix, i, j).determinant3x3())));
			}
		}
		adjointMatrix = Matrix4.transpose(adjointMatrix);
		return adjointMatrix;
	}

	public static Matrix4 inverse(final Matrix4 matrix) throws Exception
	{
		if (0.0f == matrix.determinant())
		{
			throw new Exception("Singular matrix inverse not possible");
		}
		return (divide(Matrix4.adjoint(matrix), matrix.determinant()));
	}

	public static Matrix4 adjoint(final Matrix4 matrix)
	{
		Matrix4 adjointMatrix = new Matrix4();
		for (int i = 0; 4 > i; i++)
		{
			for (int j = 0; 4 > j; j++)
			{
				adjointMatrix.setItem(i, j, (float) (Math.pow(-1, i + j) * ((Matrix4.minor(matrix, i, j)).determinant3x3())));
			}
		}
		adjointMatrix = Matrix4.transpose(adjointMatrix);
		return adjointMatrix;
	}

	public static Matrix4 minor(final Matrix4 matrix, final int row, final int col)
	{
		final Matrix4 minor = new Matrix4();
		int m = 0, n = 0;

		for (int i = 0; 4 > i; i++)
		{
			if (i == row)
			{
				continue;
			}
			n = 0;
			for (int j = 0; 4 > j; j++)
			{
				if (j == col)
				{
					continue;
				}
				minor.setItem(m, n, matrix.getItem(i, j));
				n++;
			}
			m++;
		}

		return minor;
	}

	@Override
	public boolean equals(final Object obj)
	{
		return null != obj && obj instanceof Matrix4 && this.equals((Matrix4)obj);
	}

	public boolean equals(final Matrix4 other)
	{
		return (null != other && this.M11 == other.M11 && this.M12 == other.M12 && this.M13 == other.M13 && this.M14 == other.M14 &&
				this.M21 == other.M21 && this.M22 == other.M22 && this.M23 == other.M23 && this.M24 == other.M24 && this.M31 == other.M31 &&
				this.M32 == other.M32 && this.M33 == other.M33 && this.M34 == other.M34 && this.M41 == other.M41 && this.M42 == other.M42 &&
				this.M43 == other.M43 && this.M44 == other.M44);
	}

	@Override
	public int hashCode()
	{
		int hashCode = ((Float) this.M11).hashCode();
		hashCode = hashCode * 31 + ((Float) this.M12).hashCode();
		hashCode = hashCode * 31 + ((Float) this.M13).hashCode();
		hashCode = hashCode * 31 + ((Float) this.M14).hashCode();
		hashCode = hashCode * 31 + ((Float) this.M21).hashCode();
		hashCode = hashCode * 31 + ((Float) this.M22).hashCode();
		hashCode = hashCode * 31 + ((Float) this.M23).hashCode();
		hashCode = hashCode * 31 + ((Float) this.M24).hashCode();
		hashCode = hashCode * 31 + ((Float) this.M31).hashCode();
		hashCode = hashCode * 31 + ((Float) this.M32).hashCode();
		hashCode = hashCode * 31 + ((Float) this.M33).hashCode();
		hashCode = hashCode * 31 + ((Float) this.M34).hashCode();
		hashCode = hashCode * 31 + ((Float) this.M41).hashCode();
		hashCode = hashCode * 31 + ((Float) this.M42).hashCode();
		hashCode = hashCode * 31 + ((Float) this.M43).hashCode();
		hashCode = hashCode * 31 + ((Float) this.M44).hashCode();
		return  hashCode;
	}

	/**
	 * Get a formatted string representation of the vector
	 * 
	 * @return A string representation of the vector
	 */

	@Override
	public String toString()
	{
		return String.format(Helpers.EnUsCulture,
				"|%f, %f, %f, %f|\n|%f, %f, %f, %f|\n|%f, %f, %f, %f|\n|%f, %f, %f, %f|", this.M11, this.M12, this.M13, this.M14, this.M21, this.M22,
				this.M23, this.M24, this.M31, this.M32, this.M33, this.M34, this.M41, this.M42, this.M43, this.M44);
	}

	public Vector4 getItem(final int row) throws IndexOutOfBoundsException
	{
		switch (row)
		{
			case 0:
				return new Vector4(this.M11, this.M12, this.M13, this.M14);
			case 1:
				return new Vector4(this.M21, this.M22, this.M23, this.M24);
			case 2:
				return new Vector4(this.M31, this.M32, this.M33, this.M34);
			case 3:
				return new Vector4(this.M41, this.M42, this.M43, this.M44);
			default:
				throw new IndexOutOfBoundsException("Matrix4 row index must be from 0-3");
		}
	}

	public void setItem(final int row, final Vector4 value) throws IndexOutOfBoundsException
	{
		switch (row)
		{
			case 0:
				this.M11 = value.X;
				this.M12 = value.Y;
				this.M13 = value.Z;
				this.M14 = value.S;
				break;
			case 1:
				this.M21 = value.X;
				this.M22 = value.Y;
				this.M23 = value.Z;
				this.M24 = value.S;
				break;
			case 2:
				this.M31 = value.X;
				this.M32 = value.Y;
				this.M33 = value.Z;
				this.M34 = value.S;
				break;
			case 3:
				this.M41 = value.X;
				this.M42 = value.Y;
				this.M43 = value.Z;
				this.M44 = value.S;
				break;
			default:
				throw new IndexOutOfBoundsException("Matrix4 row index must be from 0-3");
		}
	}

	public float getItem(final int row, final int column) throws IndexOutOfBoundsException
	{
		switch (row)
		{
			case 0:
				switch (column)
				{
					case 0:
						return this.M11;
					case 1:
						return this.M12;
					case 2:
						return this.M13;
					case 3:
						return this.M14;
					default:
						throw new IndexOutOfBoundsException("Matrix4 row and column values must be from 0-3");
				}
			case 1:
				switch (column)
				{
					case 0:
						return this.M21;
					case 1:
						return this.M22;
					case 2:
						return this.M23;
					case 3:
						return this.M24;
					default:
						throw new IndexOutOfBoundsException("Matrix4 row and column values must be from 0-3");
				}
			case 2:
				switch (column)
				{
					case 0:
						return this.M31;
					case 1:
						return this.M32;
					case 2:
						return this.M33;
					case 3:
						return this.M34;
					default:
						throw new IndexOutOfBoundsException("Matrix4 row and column values must be from 0-3");
				}
			case 3:
				switch (column)
				{
					case 0:
						return this.M41;
					case 1:
						return this.M42;
					case 2:
						return this.M43;
					case 3:
						return this.M44;
					default:
						throw new IndexOutOfBoundsException("Matrix4 row and column values must be from 0-3");
				}
			default:
				throw new IndexOutOfBoundsException("Matrix4 row and column values must be from 0-3");
		}
	}

	public void setItem(final int row, final int column, final float value)
	{
		switch (row)
		{
			case 0:
				switch (column)
				{
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
						throw new IndexOutOfBoundsException("Matrix4 row and column values must be from 0-3");
				}
			case 1:
				switch (column)
				{
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
						throw new IndexOutOfBoundsException("Matrix4 row and column values must be from 0-3");
				}
			case 2:
				switch (column)
				{
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
						throw new IndexOutOfBoundsException("Matrix4 row and column values must be from 0-3");
				}
			case 3:
				switch (column)
				{
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
						throw new IndexOutOfBoundsException("Matrix4 row and column values must be from 0-3");
				}
			default:
				throw new IndexOutOfBoundsException("Matrix4 row and column values must be from 0-3");
		}
	}

	/** A 4x4 matrix containing all zeroes */
	public static final Matrix4 Zero = new Matrix4();

	/** A 4x4 identity matrix */
	public static final Matrix4 Identity = new Matrix4(1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f);
}
