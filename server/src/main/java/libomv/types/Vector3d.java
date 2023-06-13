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

public class Vector3d
{
	public double X;

	public double Y;

	public double Z;

	public Vector3d(final double val)
	{
		this.X = this.Y = this.Z = val;
	}

	public Vector3d(final Vector3 vec)
	{
		this.X = vec.X;
		this.Y = vec.Y;
		this.Z = vec.Z;
	}

	public Vector3d(final Vector3d vec)
	{
		this.X = vec.X;
		this.Y = vec.Y;
		this.Z = vec.Z;
	}

	public Vector3d(final double x, final double y, final double z)
	{
		this.X = x;
		this.Y = y;
		this.Z = z;
	}

	public Vector3d(final byte[] bytes, final int offset)
	{
		this.X = this.Y = this.Z = 0.0f;
		this.fromBytes(bytes, offset, false);
	}

	public Vector3d(final byte[] bytes, final int offset, final boolean le)
	{
		this.X = this.Y = this.Z = 0.0f;
		this.fromBytes(bytes, offset, le);
	}

	public Vector3d(final ByteBuffer byteArray)
	{
		this.X = byteArray.getDouble();
		this.Y = byteArray.getDouble();
		this.Z = byteArray.getDouble();
	}

    /**
	 * Constructor, builds a vector from an XML reader
	 * 
	 * @param parser
	 *            XML pull parser reader
	 */
    public Vector3d(final XmlPullParser parser) throws XmlPullParserException, IOException
    {
		// entering with event on START_TAG for the tag name identifying the Vector3
    	final int eventType = parser.getEventType();
    	if (XmlPullParser.START_TAG != eventType)
    		throw new XmlPullParserException("Unexpected Tag event " + eventType + " for tag name " + parser.getName(), parser, null);
    	
   		while (XmlPullParser.START_TAG == parser.nextTag())
   		{
   			final String name = parser.getName();
   			if ("X".equalsIgnoreCase(name))
   			{
				this.X = Helpers.TryParseDouble(parser.nextText().trim());
   			}
   			else if ("Y".equalsIgnoreCase(name))
   			{
				this.Y = Helpers.TryParseDouble(parser.nextText().trim());
   			}
   			else if ("Z".equalsIgnoreCase(name))
   			{
				this.Z = Helpers.TryParseDouble(parser.nextText().trim());
   			}
   			else
   			{
   				Helpers.skipElement(parser);
   			}
    	}
    }

	/**
	 * Writes the raw data for this vector to a ByteBuffer
	 * 
	 * @param byteArray buffer to copy the 24 bytes for X, Y, and Z
	 * @throws IOException 
	 */
	public void write(final ByteBuffer byteArray)
	{
		byteArray.putDouble(this.X);
		byteArray.putDouble(this.Y);
		byteArray.putDouble(this.Z);
	}

	/**
	 * Writes the raw data for this vector to a OutputStream
	 * 
	 * @param stream OutputStream to copy the 12 bytes for X, Y, and Z
	 * @param le True for writing little endian data
	 * @throws IOException 
	 */
	public void write(final OutputStream stream, final boolean le) throws IOException
	{
		if (le)
		{
			stream.write(Helpers.DoubleToBytesL(this.X));
			stream.write(Helpers.DoubleToBytesL(this.Y));
			stream.write(Helpers.DoubleToBytesL(this.Z));
		}
		else
		{
			stream.write(Helpers.DoubleToBytesB(this.X));
			stream.write(Helpers.DoubleToBytesB(this.Y));
			stream.write(Helpers.DoubleToBytesB(this.Z));
		}
	}

	public static double distance(final Vector3d value1, final Vector3d value2)
	{
		return Math.sqrt(Vector3d.distanceSquared(value1, value2));
	}

	public static double distanceSquared(final Vector3d value1, final Vector3d value2)
	{
		return (value1.X - value2.X) * (value1.X - value2.X) + (value1.Y - value2.Y) * (value1.Y - value2.Y)
				+ (value1.Z - value2.Z) * (value1.Z - value2.Z);
	}

	public static Vector3d normalize(final Vector3d value)
	{
		return new Vector3d(value).normalize(); 
	}

	public double length()
	{
		return Math.sqrt(Vector3d.distanceSquared(this, Vector3d.Zero));
	}

	public double lengthSquared()
	{
		return Vector3d.distanceSquared(this, Vector3d.Zero);
	}

	public Vector3d normalize()
	{
		final double length = this.length();
		if (Helpers.FLOAT_MAG_THRESHOLD < length)
		{
			return this.divide(length);
		}
		this.X = 0.0f;
		this.Y = 0.0f;
		this.Z = 0.0f;
		return this;
	}

	/**
	 * Builds a vector from a byte array
	 * 
	 * @param bytes
	 *            Byte array containing a 12 byte vector
	 * @param pos
	 *            Beginning position in the byte array
	 * @param le
	 *            is the byte array in little endian format
	 */
	public void fromBytes(final byte[] bytes, final int pos, final boolean le)
	{
		if (le)
		{
			/* Little endian architecture */
			this.X = Helpers.BytesToDoubleL(bytes, pos);
			this.Y = Helpers.BytesToDoubleL(bytes, pos + 8);
			this.Z = Helpers.BytesToDoubleL(bytes, pos + 16);
		}
		else
		{
			this.X = Helpers.BytesToDoubleB(bytes, pos);
			this.Y = Helpers.BytesToDoubleB(bytes, pos + 8);
			this.Z = Helpers.BytesToDoubleB(bytes, pos + 16);
		}
	}

	/**
	 * Writes the raw bytes for this UUID to a byte array
	 * 
	 * @param dest
	 *            Destination byte array
	 * @param pos
	 *            Position in the destination array to start writeing. Must be
	 *            at least 16 bytes before the end of the array
	 */
	public int toBytes(final byte[] dest, final int pos)
	{
		return this.toBytes(dest, pos, false);
	}

	public int toBytes(final byte[] dest, final int pos, final boolean le)
	{
		if (le)
		{
			Helpers.DoubleToBytesL(this.X, dest, pos);
			Helpers.DoubleToBytesL(this.Y, dest, pos + 4);
			Helpers.DoubleToBytesL(this.Z, dest, pos + 8);
		}
		else
		{
			Helpers.DoubleToBytesB(this.X, dest, pos);
			Helpers.DoubleToBytesB(this.Y, dest, pos + 4);
			Helpers.DoubleToBytesB(this.Z, dest, pos + 8);
		}
		return 24;
	}

	public static Vector3d parse(final XmlPullParser parser) throws XmlPullParserException, IOException
	{
		return new Vector3d(parser);
	}
	
	public void serializeXml(final XmlSerializer writer, final String namespace, final String name) throws IllegalArgumentException, IllegalStateException, IOException
	{
		writer.startTag(namespace, name);
		writer.startTag(namespace, "X").text(Double.toString(this.X)).endTag(namespace, "X");
		writer.startTag(namespace, "Y").text(Double.toString(this.Y)).endTag(namespace, "Y");
		writer.startTag(namespace, "Z").text(Double.toString(this.Z)).endTag(namespace, "Z");
		writer.endTag(namespace, name);
	}
	
	public void serializeXml(final XmlSerializer writer, final String namespace, final String name, final Locale locale) throws IllegalArgumentException, IllegalStateException, IOException
	{
		writer.startTag(namespace, name);
		writer.startTag(namespace, "X").text(String.format(locale, "%f", this.X)).endTag(namespace, "X");
		writer.startTag(namespace, "Y").text(String.format(locale, "%f", this.Y)).endTag(namespace, "Y");
		writer.startTag(namespace, "Z").text(String.format(locale, "%f", this.Z)).endTag(namespace, "Z");
		writer.endTag(namespace, name);
	}

	@Override
	public String toString()
	{
		return String.format(Helpers.EnUsCulture, "<%.3f, %.3f, %.3f>", this.X, this.Y, this.Z);
	}

	@Override
	public boolean equals(final Object obj)
	{
		return null != obj && ((obj instanceof Vector3) && this.equals((Vector3)obj) || (obj instanceof Vector3d) && this.equals((Vector3d)obj));
	}

	public boolean equals(final Vector3 val)
	{
		return null != val && this.X == val.X && this.Y == val.Y && this.Z == val.Z;
	}

	public boolean equals(final Vector3d val)
	{
		return null != val && this.X == val.X && this.Y == val.Y && this.Z == val.Z;
	}

	@Override
	public int hashCode()
	{
		return ((Double) this.X).hashCode() * 31 * 31 + ((Double) this.Y).hashCode() * 31 + ((Double) this.Z).hashCode();
	}

	public Vector3d negate()
	{
		this.X = -this.X;
		this.Y = -this.Y;
		this.Z = -this.Z;
		return this;
	}

	public Vector3d add(final Vector3d val)
	{
		this.X += val.X;
		this.Y += val.Y;
		this.Z += val.Z;
		return this;
	}

	public Vector3d subtract(final Vector3d val)
	{
		this.X -= val.X;
		this.Y -= val.Y;
		this.Z -= val.Z;
		return this;
	}

	public Vector3d multiply(final double scaleFactor)
	{
		this.X *= scaleFactor;
		this.Y *= scaleFactor;
		this.Z *= scaleFactor;
		return this;
	}
	
	public Vector3d multiply(final Vector3d value)
	{
		this.X *= value.X;
		this.Y *= value.Y;
		this.Z *= value.Z;
		return this;
	}

	public Vector3d divide(final Vector3d value)
	{
		this.X /= value.X;
		this.Y /= value.Y;
		this.Z /= value.Z;
		return this;
	}

	public Vector3d divide(final double divider)
	{
		final double factor = 1.0d / divider;
		this.X *= factor;
		this.Y *= factor;
		this.Z *= factor;
		return this;
	}
	
	public Vector3d cross(final Vector3d value)
	{
		this.X = this.Y * value.Z - value.Y * this.Z;
		this.Y = this.Z * value.X - value.Z * this.X;
		this.Z = this.X * value.Y - value.X * this.Y;
		return this;
	}

	/** A vector with a value of 0,0,0 */
	public static final Vector3d Zero = new Vector3d(0.0f);
	/** A vector with a value of 1,1,1 */
	public static final Vector3d One = new Vector3d(1.0d, 1.0d, 1.0d);
	/** A unit vector facing forward (X axis), value 1,0,0 */
	public static final Vector3d UnitX = new Vector3d(1.0d, 0.0d, 0.0d);
	/** A unit vector facing left (Y axis), value 0,1,0 */
	public static final Vector3d UnitY = new Vector3d(0.0d, 1.0d, 0.0d);
	/** A unit vector facing up (Z axis), value 0,0,1 */
	public static final Vector3d UnitZ = new Vector3d(0.0d, 0.0d, 1.0d);
}
