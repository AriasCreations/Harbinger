/**
 * Copyright (c) 2006-2014, openmetaverse.org
 * Copyright (c) 2009-2017, Frederick Martian
 * Copyright (c) 2006, Lateral Arts Limited
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

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Locale;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import libomv.utils.Helpers;
import libomv.utils.RefObject;

public class Quaternion
{
	private static final float DEG_TO_RAD = 0.017453292519943295769236907684886f;
	
	public float X;

	public float Y;

	public float Z;

	public float W;

	public Quaternion()
	{
		this.X = this.Y = this.Z = 0.0f;
		this.W = 1.0f;
	}

	public Quaternion(final float x, final float y, final float z, final float w)
	{
		this.X = x;
		this.Y = y;
		this.Z = z;
		this.W = w;
	}

	/**
	 * Build a quaternion from normalized float values
	 * 
	 * @param x
	 *            X value from -1.0 to 1.0
	 * @param y
	 *            Y value from -1.0 to 1.0
	 * @param z
	 *            Z value from -1.0 to 1.0
	 */
	public Quaternion(final float x, final float y, final float z)
	{
		this.X = x;
		this.Y = y;
		this.Z = z;

		final float xyzsum = 1 - this.X * this.X - this.Y * this.Y - this.Z * this.Z;
		this.W = (0 < xyzsum) ? (float) Math.sqrt(xyzsum) : 0;
	}

	public Quaternion(final Vector3 vectorPart, final float scalarPart)
	{
		this.X = vectorPart.X;
		this.Y = vectorPart.Y;
		this.Z = vectorPart.Z;
		this.W = scalarPart;
	}

	public Quaternion(final Matrix4 mat)
	{
		mat.getQuaternion(this).normalize();
	}
	/**
	 * Constructor, builds a quaternion object from a byte array
	 * 
	 * @param byteArray
	 *            Byte array containing four four-byte floats
	 * @param pos
	 *            Offset in the byte array to start reading at
	 * @param normalized
	 *            Whether the source data is normalized or not. If this is true
	 *            12 bytes will be read, otherwise 16 bytes will be read.
	 */
	public Quaternion(final byte[] byteArray, final int pos, final boolean normalized)
	{
		this.X = this.Y = this.Z = this.W = 0.0f;
		this.FromBytes(byteArray, pos, normalized, false);
	}

	public Quaternion(final byte[] byteArray, final int pos, final boolean normalized, final boolean le)
	{
		this.X = this.Y = this.Z = this.W = 0.0f;
		this.FromBytes(byteArray, pos, normalized, le);
	}

	public Quaternion(final ByteBuffer byteArray, final boolean normalized)
	{
		this.X = byteArray.getFloat();
		this.Y = byteArray.getFloat();
		this.Z = byteArray.getFloat();
		if (!normalized)
		{
			this.W = byteArray.getFloat();
		}
		else
		{
			final float xyzsum = 1.0f - this.X * this.X - this.Y * this.Y - this.Z * this.Z;
			this.W = (0.0f < xyzsum) ? (float) Math.sqrt(xyzsum) : 0;
		}
	}

	/**
	 * Constructor, builds a quaternion from an XML reader
	 * 
	 * @param parser
	 *            XML pull parser reader
	 */
	public Quaternion(final XmlPullParser parser) throws XmlPullParserException, IOException
	{
		// entering with event on START_TAG for the tag name identifying the Quaternion
    	final int eventType = parser.getEventType();
    	if (XmlPullParser.START_TAG != eventType)
    		throw new XmlPullParserException("Unexpected Tag event " + eventType + " for tag name " + parser.getName(), parser, null);
    	
   		while (XmlPullParser.START_TAG == parser.nextTag())
   		{
			final String name = parser.getName();
			if ("X".equals(name))
			{
				this.X = Helpers.TryParseFloat(parser.nextText().trim());
			}
			else if ("Y".equals(name))
			{
				this.Y = Helpers.TryParseFloat(parser.nextText().trim());
			}
			else if ("Z".equals(name))
			{
				this.Z = Helpers.TryParseFloat(parser.nextText().trim());
			}
			else if ("W".equals(name))
			{
				this.W = Helpers.TryParseFloat(parser.nextText().trim());
			}
			else
			{
				Helpers.skipElement(parser);
			}
    	}
	}

	public Quaternion(final Quaternion q)
	{
		this.X = q.X;
		this.Y = q.Y;
		this.Z = q.Z;
		this.W = q.W;
	}

	public boolean approxEquals(final Quaternion quat, final float tolerance)
	{
		final Quaternion diff = this.subtract(quat);
		return (diff.lengthSquared() <= tolerance * tolerance);
	}

	public float length()
	{
		return (float) Math.sqrt(this.lengthSquared());
	}

	public float lengthSquared()
	{
		return (this.X * this.X + this.Y * this.Y + this.Z * this.Z + this.W * this.W);
	}

	/** Normalizes the quaternion */
	public Quaternion normalize()
	{
		final float mag = this.length();
		// Catch very small rounding errors when normalizing
		if (Helpers.FLOAT_MAG_THRESHOLD < mag)
		{
			return this.divide(mag);
		}
		this.X = 0.0f;
		this.Y = 0.0f;
		this.Z = 0.0f;
		this.W = 1.0f;
		return this;
	}

	public Vector3 toVector3()
	{
		if (0 <= W)
		{
			return new Vector3(this.X, this.Y, this.Z);
		}
		return new Vector3(-this.X, -this.Y, -this.Z);
	}
	
	/**
	 * Normalize this quaternion and serialize it to a byte array
	 * 
	 * @return A 12 byte array containing normalized X, Y, and Z floating point
	 *         values in order using little endian byte ordering
	 * @throws Exception
	 */
	public byte[] GetBytes() throws Exception
	{
		final byte[] bytes = new byte[12];
		this.ToBytes(bytes, 0, false);
		return bytes;
	}

	/**
	 * Writes the normalized data for this quaternion to a ByteBuffer
	 * 
	 * @param bytes The ByteBuffer to copy the 12 bytes for X, Y, and Z
	 * @throws IOException 
	 */
	public void write(final ByteBuffer bytes) throws Exception
	{
		float norm = (float) Math.sqrt(this.X * this.X + this.Y * this.Y + this.Z * this.Z + this.W * this.W);

		if (0 != norm)
		{
			norm = 1.0f / norm;

			final float x;
			float y;
			final float z;
			if (0.0f <= W)
			{
				x = this.X;
				y = this.Y;
				z = this.Z;
			}
			else
			{
				x = -this.X;
				y = -this.Y;
				z = -this.Z;
			}
			bytes.putFloat(norm * x);
			bytes.putFloat(norm * y);
			bytes.putFloat(norm * z);
		}
		else
		{
			throw new Exception("Quaternion <" + this.X + "," + this.Y + "," + this.Z + "," + this.W + "> normalized to zero");
		}
	}

	/**
	 * Writes the raw data for this vector to a OutputStream
	 * 
	 * @param stream OutputStream to copy the 16 bytes for X, Y, Z, and W
	 * @param le True for writing little endian data
	 * @throws IOException 
	 */
	public void write(final OutputStream stream, final boolean le) throws IOException
	{
		if (le)
		{
			stream.write(Helpers.FloatToBytesL(this.X));
			stream.write(Helpers.FloatToBytesL(this.Y));
			stream.write(Helpers.FloatToBytesL(this.Z));
			stream.write(Helpers.FloatToBytesL(this.W));
		}
		else
		{
			stream.write(Helpers.FloatToBytesB(this.X));
			stream.write(Helpers.FloatToBytesB(this.Y));
			stream.write(Helpers.FloatToBytesB(this.Z));
			stream.write(Helpers.FloatToBytesB(this.W));
		}
	}

	public static Quaternion parse(final XmlPullParser parser) throws XmlPullParserException, IOException
	{
		return new Quaternion(parser);
	}
	
	public void serializeXml(final XmlSerializer writer, final String namespace, final String name) throws IllegalArgumentException, IllegalStateException, IOException
	{
        writer.startTag(namespace, name);
		writer.startTag(namespace, "X").text(Float.toString(this.X)).endTag(namespace, "X");
		writer.startTag(namespace, "Y").text(Float.toString(this.Y)).endTag(namespace, "Y");
		writer.startTag(namespace, "Z").text(Float.toString(this.Z)).endTag(namespace, "Z");
		writer.startTag(namespace, "W").text(Float.toString(this.W)).endTag(namespace, "W");
        writer.endTag(namespace, name);
	}

	public void serializeXml(final XmlSerializer writer, final String namespace, final String name, final Locale locale) throws IllegalArgumentException, IllegalStateException, IOException
	{
        writer.startTag(namespace, name);
		writer.startTag(namespace, "X").text(String.format(locale,  "%f", this.X)).endTag(namespace, "X");
		writer.startTag(namespace, "Y").text(String.format(locale,  "%f", this.Y)).endTag(namespace, "Y");
		writer.startTag(namespace, "Z").text(String.format(locale,  "%f", this.Z)).endTag(namespace, "Z");
		writer.startTag(namespace, "W").text(String.format(locale,  "%f", this.W)).endTag(namespace, "W");
        writer.endTag(namespace, name);
	}

	/**
	 * Get a formatted string representation of the vector
	 * 
	 * @return A string representation of the vector
	 */
	@Override
	public String toString()
	{
		return String.format(Helpers.EnUsCulture, "<%f, %f, %f, %f>", this.X, this.Y, this.Z, this.W);
	}

	@Override
	public int hashCode()
	{
		int hashCode = ((Float) this.X).hashCode();
		hashCode = hashCode * 31 + ((Float) this.Y).hashCode();
		hashCode = hashCode * 31 + ((Float) this.Z).hashCode();
		hashCode = hashCode * 31 + ((Float) this.W).hashCode();
		return  hashCode;
	}

	/**
	 * Builds a quaternion object from a byte array
	 * 
	 * @param bytes
	 *            The source byte array
	 * @param pos
	 *            Offset in the byte array to start reading at
	 * @param normalized
	 *            Whether the source data is normalized or not. If this is true
	 *            12 bytes will be read, otherwise 16 bytes will be read.
	 */
	public void FromBytes(final byte[] bytes, final int pos, final boolean normalized, final boolean le)
	{
		if (le)
		{
			/* Little endian architecture */
			this.X = Helpers.BytesToFloatL(bytes, pos);
			this.Y = Helpers.BytesToFloatL(bytes, pos + 4);
			this.Z = Helpers.BytesToFloatL(bytes, pos + 8);
			if (!normalized)
			{
				this.W = Helpers.BytesToFloatL(bytes, pos + 12);
			}
		}
		else
		{
			this.X = Helpers.BytesToFloatB(bytes, pos);
			this.Y = Helpers.BytesToFloatB(bytes, pos + 4);
			this.Z = Helpers.BytesToFloatB(bytes, pos + 8);
			if (!normalized)
			{
				this.W = Helpers.BytesToFloatB(bytes, pos + 12);
			}
		}
		if (normalized)
		{
			final float xyzsum = 1.0f - this.X * this.X - this.Y * this.Y - this.Z * this.Z;
			this.W = (0.0f < xyzsum) ? (float) Math.sqrt(xyzsum) : 0.0f;
		}
	}

	/**
	 * Writes the raw bytes for this quaternion to a byte array
	 * 
	 * @param dest
	 *            Destination byte array
	 * @param pos
	 *            Position in the destination array to start writing. Must be at
	 *            least 12 bytes before the end of the array
	 * @throws Exception
	 */
	public void ToBytes(final byte[] dest, final int pos, final boolean le) throws Exception
	{
		float norm = this.X * this.X + this.Y * this.Y + this.Z * this.Z + this.W * this.W;

		if (0.001f <= norm)
		{
			norm = (float)(1 / Math.sqrt(norm));

			final float x;
			float y;
			final float z;
			if (0.0f <= W)
			{
				x = this.X;
				y = this.Y;
				z = this.Z;
			}
			else
			{
				x = -this.X;
				y = -this.Y;
				z = -this.Z;
			}

			if (le)
			{
				Helpers.FloatToBytesL(norm * x, dest, pos);
				Helpers.FloatToBytesL(norm * y, dest, pos + 4);
				Helpers.FloatToBytesL(norm * z, dest, pos + 8);
			}
			else
			{
				Helpers.FloatToBytesB(norm * x, dest, pos);
				Helpers.FloatToBytesB(norm * y, dest, pos + 4);
				Helpers.FloatToBytesB(norm * z, dest, pos + 8);
			}
		}
		else
		{
			throw new Exception(String.format("Quaternion %s normalized to zero", this));
		}
	}

	/**
	 * Convert this quaternion to euler angles
	 * 
	 * Note: according to http://www.euclideanspace.com/maths/geometry/rotations/conversions/quaternionToEuler/
	 * 
	 * @return a Vector with the 3 angles roll, pitch, yaw in this order
	 */
	public Vector3 toEuler()
	{
		final float sqx = this.X * this.X;
		final float sqy = this.Y * this.Y;
		final float sqz = this.Z * this.Z;
		final float sqw = this.W * this.W;

		// Unit will be a correction factor if the quaternion is not normalized
		final float unit = sqx + sqy + sqz + sqw;
        if (0.001 > unit)
        	return Vector3.Zero;
        final double test = this.X * this.Y + this.Z * this.W;

		if (test > 0.499f * unit)
		{
			// Singularity at north pole
			return new Vector3(0.0f, (float) (Math.PI / 2.0), 2.0f * (float)Math.atan2(this.X, this.W));
		}
		else if (test < -0.499f * unit)
		{
			// Singularity at south pole
			return new Vector3(0.0f, -(float)(Math.PI / 2.0), -2.0f * (float)Math.atan2(this.X, this.W));
		}
		return new Vector3((float)Math.atan2(2.0f * this.X * this.W - 2.0f * this.Y * this.Z, -sqx + sqy - sqz + sqw),
					       (float)Math.asin(2.0f * test / unit),
					       (float)Math.atan2(2.0f * this.Y * this.W - 2.0f * this.X * this.Z, sqx - sqy - sqz + sqw));
	}

	/**
	 * Convert this quaternion to an angle around an axis
	 * 
	 * @param axis
	 *            Unit vector describing the axis
	 * @param angle
	 *            Angle around the axis, in radians
	 */
	public void getAxisAngle(final RefObject<Vector3> axis, final RefObject<Float> angle)
	{
		final Quaternion q = normalize();
		final float sin = (float)Math.sqrt(1.0f - q.W * q.W);
		if (0.001 <= sin)
		{
		    float invSin = 1.0f / sin;
		    if (0 > q.W) invSin = -invSin;
		    axis.argvalue = new Vector3(q.X, q.Y, q.Z).multiply(invSin);
		    angle.argvalue = 2.0f * (float)Math.acos(q.W);
		    if (Math.PI < angle.argvalue)
		    angle.argvalue = 2.0f * (float)Math.PI - angle.argvalue;
		 }
		 else
		 {
			 axis.argvalue = Vector3.UnitX;
			 angle.argvalue = 0.0f;
		}
	}

	/**
	 * Build a quaternion from an axis and an angle of rotation around that axis
	 */
	public static Quaternion createFromAxisAngle(final float axisX, final float axisY, final float axisZ, final float angle)
	{
		final Vector3 axis = new Vector3(axisX, axisY, axisZ);
		return Quaternion.createFromAxisAngle(axis, angle);
	}

	/**
	 * Build a quaternion from an axis and an angle of rotation around that axis
	 * 
	 * @param axis
	 *            Axis of rotation
	 * @param angle
	 *            Angle of rotation
	 */
	public static Quaternion createFromAxisAngle(Vector3 axis, float angle)
	{
		axis = Vector3.normalize(axis);

		angle *= 0.5;
		final float s = (float) Math.sin(angle);

		return new Quaternion(axis.X * s, axis.Y * s, axis.Z * s, (float) Math.cos(angle)).normalize();
	}

	/**
	 * Creates a quaternion from a vector containing roll, pitch, and yaw in
	 * radians
	 * 
	 * @param eulers Vector representation of the euler angles in radians
	 * @return Quaternion representation of the euler angles
	 * @throws Exception
	 */
	public static Quaternion createFromEulers(final Vector3 eulers) throws Exception
	{
		return Quaternion.createFromEulers(eulers.X, eulers.Y, eulers.Z);
	}

	/**
	 * Creates a quaternion from roll, pitch, and yaw euler angles in radians
	 * 
	 * @param roll
	 *            X angle in radians
	 * @param pitch
	 *            Y angle in radians
	 * @param yaw
	 *            Z angle in radians
	 * @return Quaternion representation of the euler angles
	 * @throws Exception
	 */
	public static Quaternion createFromEulers(final float roll, final float pitch, final float yaw) throws Exception
	{
		if (Helpers.TWO_PI < roll || Helpers.TWO_PI < pitch || Helpers.TWO_PI < yaw)
		{
			throw new Exception("Euler angles must be in radians");
		}

		final double atCos = Math.cos(roll / 2.0f);
		final double atSin = Math.sin(roll / 2.0f);
		final double leftCos = Math.cos(pitch / 2.0f);
		final double leftSin = Math.sin(pitch / 2.0f);
		final double upCos = Math.cos(yaw / 2.0f);
		final double upSin = Math.sin(yaw / 2.0f);
		final double atLeftCos = atCos * leftCos;
		final double atLeftSin = atSin * leftSin;
		return new Quaternion((float) (atSin * leftCos * upCos + atCos * leftSin * upSin), (float) (atCos * leftSin
				* upCos - atSin * leftCos * upSin), (float) (atLeftCos * upSin + atLeftSin * upCos), (float) (atLeftCos
				* upCos - atLeftSin * upSin));
	}

	public static Quaternion createFromRotationMatrix(final Matrix4 matrix)
	{
		final Quaternion quaternion = new Quaternion();
		quaternion.setFromRotationMatrix(matrix);
		return quaternion;
	}

	public static float dot(final Quaternion quaternion1, final Quaternion quaternion2)
	{
		return quaternion1.dot(quaternion2);
	}

	/**
	 * Conjugates and renormalizes a vector
	 */
	public static Quaternion inverse(Quaternion quaternion)
	{
		final float norm = quaternion.lengthSquared();

		if (0.0f == norm)
		{
			quaternion.X = quaternion.Y = quaternion.Z = quaternion.W = 0.0f;
		}
		else
		{
			final float oonorm = 1.0f / norm;
			quaternion = Quaternion.conjugate(quaternion);

			quaternion.X *= oonorm;
			quaternion.Y *= oonorm;
			quaternion.Z *= oonorm;
			quaternion.W *= oonorm;
		}
		return quaternion;
	}

	// linear interpolation from identity to q
	public static Quaternion lerp(final Quaternion q, final float t)
	{
		return new Quaternion(t * q.X, t * q.Y, t * q.Z, t * (q.Z - 1.0f) + 1.0f).normalize();
	}

	/* linear interpolation between two quaternions */
	public static Quaternion lerp(final Quaternion q1, final Quaternion q2, final float t)
	{
		final float inv_t = 1.0f - t;
		return new Quaternion(t * q2.X + inv_t * q1.X, t * q2.Y + inv_t * q1.Y,
		                              t * q2.Z + inv_t * q1.Z, t * q2.W + inv_t * q1.W).normalize();
	}

	/** Spherical linear interpolation between two quaternions */
	public static Quaternion slerp(Quaternion q1, final Quaternion q2, final float amount)
	{
		float angle = Quaternion.dot(q1, q2);

		if (0.0f > angle)
		{
			q1 = Quaternion.multiply(q1, -1.0f);
			angle *= -1.0f;
		}

		final float scale;
		final float invscale;

		if (0.05f < (angle + 1f))
		{
			if (0.05f <= (1f - angle))
			{
				// slerp
				final float theta = (float) Math.acos(angle);
				final float invsintheta = 1.0f / (float) Math.sin(theta);
				scale = (float) Math.sin(theta * (1.0f - amount)) * invsintheta;
				invscale = (float) Math.sin(theta * amount) * invsintheta;
			}
			else
			{
				// lerp
				scale = 1.0f - amount;
				invscale = amount;
			}
		}
		else
		{
			q2.X = -q1.Y;
			q2.Y = q1.X;
			q2.Z = -q1.W;
			q2.W = q1.Z;

			scale = (float) Math.sin(Math.PI * (0.5f - amount));
			invscale = (float) Math.sin(Math.PI * amount);
		}
		return new Quaternion(q1.X * scale + q2.X * invscale, q1.Y * scale + q2.Y * invscale,
				              q1.Z * scale + q2.Z * invscale, q1.W * scale + q2.W * invscale);
	}

	public static Quaternion normalize(final Quaternion quaternion)
	{
		return new Quaternion(quaternion).normalize();
	}

	public static Quaternion Parse(final String val)
	{
		final String splitChar = ",";
		final String[] split = val.replace("<", "").replace(">", "").split(splitChar);
		if (3 == split.length)
		{
			return new Quaternion(Float.parseFloat(split[0].trim()), Float.parseFloat(split[1].trim()),
					Float.parseFloat(split[2].trim()));
		}
		return new Quaternion(Float.parseFloat(split[0].trim()), Float.parseFloat(split[1].trim()),
				Float.parseFloat(split[2].trim()), Float.parseFloat(split[3].trim()));
	}

	public static boolean TryParse(final String val, final RefObject<Quaternion> result)
	{
		try
		{
			result.argvalue = Quaternion.Parse(val);
			return true;
		}
		catch (final Throwable t)
		{
			result.argvalue = new Quaternion();
			return false;
		}
	}

	@Override
	public boolean equals(final Object obj)
	{
		return null != obj && obj instanceof Quaternion && this.equals((Quaternion)obj);
	}

	public boolean equals(final Quaternion other)
	{
		return null != other && this.W == other.W && this.X == other.X && this.Y == other.Y && this.Z == other.Z;
	}

	public boolean isIdentity()
	{
		return (0.0f == X && 0.0f == Y && 0.0f == Z && 1.0f == W);
	}
	
	public boolean isZero()
	{
		return this.equals(Quaternion.Zero);
	}
	
	public static boolean isZero(final Quaternion q)
	{
		if (null != q)
			return q.equals(Quaternion.Zero);
		return false;
	}
	
	public static boolean isZeroOrNull(final Quaternion q)
	{
		if (null != q)
			return q.equals(Quaternion.Zero);
		return true;
	}

	public Quaternion negate()
	{
		this.X = -this.X;
		this.Y = -this.Y;
		this.Z = -this.Z;
		this.W = -this.W;
		return this;
	}

	/** Returns the conjugate (spatial inverse) of a quaternion */
	public Quaternion conjugate()
	{
		this.X = -this.X;
		this.Y = -this.Y;
		this.Z = -this.Z;
		return this;
	}

	public Quaternion add(final Quaternion quaternion)
	{
		this.X += quaternion.X;
		this.Y += quaternion.Y;
		this.Z += quaternion.Z;
		this.W += quaternion.W;
        return this;
    }

	public Quaternion subtract(final Quaternion quaternion)
	{
		this.X -= quaternion.X;
		this.Y -= quaternion.Y;
		this.Z -= quaternion.Z;
		this.W -= quaternion.W;
        return this;
    }

	public Quaternion multiply(final float scaleFactor)
	{
		this.X *= scaleFactor;
		this.Y *= scaleFactor;
		this.Z *= scaleFactor;
		this.W *= scaleFactor;
        return this;
	}

	public Quaternion multiply(final Quaternion quaternion)
	{
		final float x = (this.W * quaternion.X) + (this.X * quaternion.W) + (this.Y * quaternion.Z) - (this.Z * quaternion.Y);
	    final float y = (this.W * quaternion.Y) - (this.X * quaternion.Z) + (this.Y * quaternion.W) + (this.Z * quaternion.X);
	    final float z = (this.W * quaternion.Z) + (this.X * quaternion.Y) - (this.Y * quaternion.X) + (this.Z * quaternion.W);
	    final float w = (this.W * quaternion.W) - (this.X * quaternion.X) - (this.Y * quaternion.Y) - (this.Z * quaternion.Z);
		this.X = x;
		this.Y = y;
		this.Z = z;
		this.W = w;
        return this;
	}

	public Vector4 multiply(final Vector4 vector)
	{
	    final float rw = -this.X * vector.X - this.Y * vector.Y - this.Z * vector.Z;
	    final float rx = this.W * vector.X + this.Y * vector.Z - this.Z * vector.Y;
	    final float ry = this.W * vector.Y + this.Z * vector.X - this.X * vector.Z;
	    final float rz = this.W * vector.Z + this.X * vector.Y - this.Y * vector.X;

	    final float nx = - rw * this.X +  rx * this.W - ry * this.Z + rz * this.Y;
	    final float ny = - rw * this.Y +  ry * this.W - rz * this.X + rx * this.Z;
	    final float nz = - rw * this.Z +  rz * this.W - rx * this.Y + ry * this.X;
	    return new Vector4(nx, ny, nz, vector.S);
	}

	public Vector3 multiply(final Vector3 vector)
	{
	    final float rw = -this.X * vector.X - this.Y * vector.Y - this.Z * vector.Z;
	    final float rx = this.W * vector.X + this.Y * vector.Z - this.Z * vector.Y;
	    final float ry = this.W * vector.Y + this.Z * vector.X - this.X * vector.Z;
	    final float rz = this.W * vector.Z + this.X * vector.Y - this.Y * vector.X;

	    final float nx = - rw * this.X +  rx * this.W - ry * this.Z + rz * this.Y;
	    final float ny = - rw * this.Y +  ry * this.W - rz * this.X + rx * this.Z;
	    final float nz = - rw * this.Z +  rz * this.W - rx * this.Y + ry * this.X;
	    return new Vector3(nx, ny, nz);
	}

	public Vector3d multiply(final Vector3d vector)
	{
	    final double rw = -this.X * vector.X - this.Y * vector.Y - this.Z * vector.Z;
	    final double rx = this.W * vector.X + this.Y * vector.Z - this.Z * vector.Y;
	    final double ry = this.W * vector.Y + this.Z * vector.X - this.X * vector.Z;
	    final double rz = this.W * vector.Z + this.X * vector.Y - this.Y * vector.X;

	    final double nx = - rw * this.X +  rx * this.W - ry * this.Z + rz * this.Y;
	    final double ny = - rw * this.Y +  ry * this.W - rz * this.X + rx * this.Z;
	    final double nz = - rw * this.Z +  rz * this.W - rx * this.Y + ry * this.X;
	    return new Vector3d(nx, ny, nz);
	}

	public Quaternion divide(float divider)
	{
		divider = 1.0f / divider;
		this.X *= divider;
		this.Y *= divider;
		this.Z *= divider;
		this.W *= divider;
        return this;
	}

	public Quaternion divide(final Quaternion quaternion)
	{
		return this.inverse().multiply(quaternion);
	}

	public float dot(final Quaternion quaternion)
	{
		return (this.X * quaternion.X) + (this.Y * quaternion.Y) + (this.Z * quaternion.Z) + (this.W * quaternion.W);
	}
	
	public Quaternion inverse()
	{
		final float norm = this.lengthSquared();

		if (0.0f == norm)
		{
			this.X = this.Y = this.Z = this.W = 0.0f;
		}
		else
		{
			this.conjugate().divide(norm);
		}
		return this;
	}

	
	public void setFromRotationMatrix(final Matrix4 matrix)
	{
		float num = (matrix.M11 + matrix.M22) + matrix.M33;
		if (0.0f < num)
		{
			num = (float)Math.sqrt(num + 1.0f);
			this.W = num * 0.5f;
			num = 0.5f / num;
			this.X = (matrix.M23 - matrix.M32) * num;
			this.Y = (matrix.M31 - matrix.M13) * num;
			this.Z = (matrix.M12 - matrix.M21) * num;
		}
		else if ((matrix.M11 >= matrix.M22) && (matrix.M11 >= matrix.M33))
		{
			num = (float) Math.sqrt(1.0f + matrix.M11 - matrix.M22 - matrix.M33);
			this.X = 0.5f * num;
			num = 0.5f / num;
			this.Y = (matrix.M12 + matrix.M21) * num;
			this.Z = (matrix.M13 + matrix.M31) * num;
			this.W = (matrix.M23 - matrix.M32) * num;
		}
		else if (matrix.M22 > matrix.M33)
		{
			num = (float) Math.sqrt(1.0f + matrix.M22 - matrix.M11 - matrix.M33);
			this.Y = 0.5f * num;
			num = 0.5f / num;
			this.X = (matrix.M21 + matrix.M12) * num;
			this.Z = (matrix.M32 + matrix.M23) * num;
			this.W = (matrix.M31 - matrix.M13) * num;
		}
		else
		{
			num = (float) Math.sqrt(1.0f + matrix.M33 - matrix.M11 - matrix.M22);
			this.Z = 0.5f * num;
			num = 0.5f / num;
			this.X = (matrix.M31 + matrix.M13) * num;
			this.Y = (matrix.M32 + matrix.M23) * num;
			this.W = (matrix.M12 - matrix.M21) * num;
		}
	}
	
	public static Quaternion negate(final Quaternion quaternion)
	{
		return new Quaternion(quaternion).negate();
	}

	/** Returns the conjugate (spatial inverse) of a quaternion */
	public static Quaternion conjugate(final Quaternion quaternion)
	{
		return new Quaternion(quaternion).conjugate();
	}

	public static Quaternion add(final Quaternion quaternion1, final Quaternion quaternion2)
	{
		
		return new Quaternion(quaternion1).add(quaternion2);
	}

	public static Quaternion subtract(final Quaternion quaternion1, final Quaternion quaternion2)
	{
		return new Quaternion(quaternion1).subtract(quaternion2);
	}

	public static Quaternion multiply(final Quaternion quaternion1, final Quaternion quaternion2)
	{
		return new Quaternion(quaternion1).multiply(quaternion2);
	}

	public static Quaternion multiply(final Quaternion quaternion, final float scaleFactor)
	{	
		return new Quaternion(quaternion).multiply(scaleFactor);
	}

	public static Vector4 multiply(final Quaternion rot, final Vector4 vector)
	{
		return rot.multiply(vector);
	}

	public static Vector3 multiply(final Quaternion rot, final Vector3 vector)
	{
		return rot.multiply(vector);
	}

	public static Vector3d multiply(final Quaternion rot, final Vector3d vector)
	{
		return rot.multiply(vector);
	}

	public static Quaternion divide(final Quaternion quaternion1, final Quaternion quaternion2)
	{
		return new Quaternion(quaternion1).divide(quaternion2);
	}

	// calculate the shortest rotation from a to b
	public static Quaternion shortestArc(final Vector3 a, final Vector3 b)
	{
		// Make a local copy of both vectors.
		final Vector3 vec_a = new Vector3(a);
		final Vector3 vec_b = new Vector3(b);

		// Make sure neither vector is zero length.  Also normalize
		// the vectors while we are at it.
		final float vec_a_mag = vec_a.normalize().mag();
		final float vec_b_mag = vec_b.normalize().mag();
		if (Helpers.FLOAT_MAG_THRESHOLD > vec_a_mag ||
				Helpers.FLOAT_MAG_THRESHOLD > vec_b_mag)
		{
			// Can't calculate a rotation from this.
			// Just return ZERO_ROTATION instead.
			return Quaternion.Identity;
		}

		// Create an axis to rotate around, and the cos of the angle to rotate.
		final Vector3 axis = Vector3.cross(vec_a, vec_b);
		final float cos_theta  = Vector3.dot(vec_a, vec_b);

		// Check the angle between the vectors to see if they are parallel or anti-parallel.
		if (1.0 - Helpers.FLOAT_MAG_THRESHOLD < cos_theta)
		{
			// a and b are parallel.  No rotation is necessary.
			return Quaternion.Identity;
		}
		else if (-1.0 + Helpers.FLOAT_MAG_THRESHOLD > cos_theta)
		{
			// a and b are anti-parallel.
			// Rotate 180 degrees around some orthogonal axis.
			// Find the projection of the x-axis onto a, and try
			// using the vector between the projection and the x-axis
			// as the orthogonal axis.
			final Vector3 proj = vec_a.multiply(vec_a.X / cos_theta);
			Vector3 ortho_axis = Vector3.subtract(Vector3.UnitX, proj);
			
			// Turn this into an orthonormal axis.
			final float ortho_length = ortho_axis.normalize().length();
			// If the axis' length is 0, then our guess at an orthogonal axis
			// was wrong (a is parallel to the x-axis).
			if (Helpers.FLOAT_MAG_THRESHOLD > ortho_length)
			{
				// Use the z-axis instead.
				ortho_axis = Vector3.UnitZ;
			}

			// Construct a quaternion from this orthonormal axis.
			return new Quaternion(ortho_axis.X, ortho_axis.Y, ortho_axis.Z, 0.0f);
		}
		else
		{
			// a and b are NOT parallel or anti-parallel.
			// Return the rotation between these vectors.
			return Quaternion.createFromAxisAngle(axis, (float) Math.acos(cos_theta));
		}
	}

	public enum Order
	{
		XYZ,
		YZX,
		ZXY,
		YXZ,
		XZY,
		ZYX
	}

	/**
	 * Creates a quaternion from maya's rotation representation, which is 3 rotations (in DEGREES)
	 * with specified order.
	 * 
	 * @param xRot X Rotation value
	 * @param yRot Y Rotation value
	 * @param zRot Z Rotation value
	 * @param order the order of the rotational values
	 * @returns a quaternion representing the 3 rotation values in the defined order
	 */
	public static Quaternion mayaQ(final float xRot, final float yRot, final float zRot, final Order order)
	{
		final Quaternion xQ = new Quaternion(new Vector3(1.0f, 0.0f, 0.0f), xRot * Quaternion.DEG_TO_RAD);
		final Quaternion yQ = new Quaternion(new Vector3(0.0f, 1.0f, 0.0f), yRot * Quaternion.DEG_TO_RAD);
		final Quaternion zQ = new Quaternion(new Vector3(0.0f, 0.0f, 1.0f), zRot * Quaternion.DEG_TO_RAD);
		Quaternion ret = null;
		switch (order)
		{
			case XYZ:
				ret = Quaternion.multiply(Quaternion.multiply(xQ, yQ), zQ);
				break;
			case YZX:
				ret = Quaternion.multiply(Quaternion.multiply(yQ, zQ), xQ);
				break;
			case ZXY:
				ret = Quaternion.multiply(Quaternion.multiply(zQ, xQ), yQ);
				break;
			case XZY:
				ret = Quaternion.multiply(Quaternion.multiply(xQ, zQ), yQ);
				break;
			case YXZ:
				ret = Quaternion.multiply(Quaternion.multiply(yQ, xQ), zQ);
				break;
			case ZYX:
				ret = Quaternion.multiply(Quaternion.multiply(zQ, yQ), xQ);
				break;
			default:
				break;
		}
		return ret;
	}

	public static Quaternion mayaQ(final float[] arr, final int pos, final Order order)
	{
		return Quaternion.mayaQ(arr[pos], arr[pos + 1], arr[pos + 2], order);
	}

	public static String OrderToString(final Order order)
	{
		final String p;
		switch (order)
		{
			default:
			case XYZ:
				p = "XYZ";
				break;
			case YZX:
				p = "YZX";
				break;
			case ZXY:
				p = "ZXY";
				break;
			case XZY:
				p = "XZY";
				break;
			case YXZ:
				p = "YXZ";
				break;
			case ZYX:
				p = "ZYX";
				break;
		}
		return p;
	}

	public static Order StringToOrder(final String str)
	{
		if (0 == str.compareToIgnoreCase("XYZ"))
			return Order.XYZ;

		if (0 == str.compareToIgnoreCase("YZX"))
			return Order.YZX;

		if (0 == str.compareToIgnoreCase("ZXY"))
			return Order.ZXY;

		if (0 == str.compareToIgnoreCase("XZY"))
			return Order.XZY;

		if (0 == str.compareToIgnoreCase("YXZ"))
			return Order.YXZ;

		if (0 == str.compareToIgnoreCase("ZYX"))
			return Order.ZYX;

		return Order.XYZ;
	}

	public static Order StringToOrderRev(final String str)
	{
		return Order.values()[5 - Quaternion.StringToOrder(str).ordinal()];
	}

	/** A quaternion with a value of 0,0,0,1 */
	public static final Quaternion Identity = new Quaternion(0.0f, 0.0f, 0.0f, 1.0f);

	/** A quaternion with a value of 0,0,0,0 */
	public static final Quaternion Zero = new Quaternion(0.0f, 0.0f, 0.0f, 0.0f);
}
