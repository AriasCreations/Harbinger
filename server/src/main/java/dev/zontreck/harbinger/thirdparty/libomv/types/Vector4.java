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
 * - Neither the name of the openmetaverse.org or dev.zontreck.harbinger.thirdparty.libomv-java project nor the
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
package dev.zontreck.harbinger.thirdparty.libomv.types;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Locale;

import org.apache.commons.io.input.SwappedDataInputStream;
import dev.zontreck.harbinger.thirdparty.v1.XmlPullParser;
import dev.zontreck.harbinger.thirdparty.v1.XmlPullParserException;
import dev.zontreck.harbinger.thirdparty.v1.XmlSerializer;

import dev.zontreck.harbinger.thirdparty.libomv.utils.Helpers;

public class Vector4
{
	public float X;

	public float Y;

	public float Z;

	public float S;

	public Vector4()
	{
		this.X = this.Y = this.Z = this.S = 0;
	}

	public Vector4(final float val)
	{
		this.X = this.Y = this.Z = this.S = val;
	}

	public Vector4(final ByteBuffer byteArray)
	{
		this.X = byteArray.getFloat();
		this.Y = byteArray.getFloat();
		this.Z = byteArray.getFloat();
		this.S = byteArray.getFloat();
	}

	public Vector4(final float x, final float y, final float z, final float s)
	{
		this.X = x;
		this.Y = y;
		this.Z = z;
		this.S = s;
	}

    /**
	 * Constructor, builds a vector from an XML reader
	 * 
	 * @param parser
	 *            XML pull parser reader
	 */
    public Vector4(final XmlPullParser parser) throws XmlPullParserException, IOException
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
				this.X = Helpers.TryParseFloat(parser.nextText().trim());
   			}
   			else if ("Y".equalsIgnoreCase(name))
   			{
				this.Y = Helpers.TryParseFloat(parser.nextText().trim());
   			}
   			else if ("Z".equalsIgnoreCase(name))
   			{
				this.Z = Helpers.TryParseFloat(parser.nextText().trim());
   			}
			else if ("S".equalsIgnoreCase(name))
			{
				this.S = Helpers.TryParseFloat(parser.nextText().trim());
			}
   			else
   			{
   				Helpers.skipElement(parser);
   			}
    	}
    }

	public Vector4(final byte[] dest, final int pos)
	{
		this.X = this.Y = this.Z = this.S = 0;
		this.fromBytes(dest, pos, false);
	}

	public Vector4(final byte[] dest, final int pos, final boolean le)
	{
		this.X = this.Y = this.Z = this.S = 0;
		this.fromBytes(dest, pos, le);
	}

	public Vector4(final Vector4 v)
	{
		this.X = v.X;
		this.Y = v.Y;
		this.Z = v.Z;
		this.S = v.S;
	}

	/**
	 * Writes the raw data for this vector to a ByteBuffer
	 * 
	 * @param byteArray buffer to copy the 16 bytes for X, Y, Z, and S
	 * @throws IOException 
	 */
	public void write(final ByteBuffer byteArray)
	{
		byteArray.putFloat(this.X);
		byteArray.putFloat(this.Y);
		byteArray.putFloat(this.Z);
		byteArray.putFloat(this.S);
	}

	/**
	 * Writes the raw data for this vector to a OutputStream
	 * 
	 * @param stream OutputStream to copy the 16 bytes for X, Y, Z, and S
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
			stream.write(Helpers.FloatToBytesL(this.S));
		}
		else
		{
			stream.write(Helpers.FloatToBytesB(this.X));
			stream.write(Helpers.FloatToBytesB(this.Y));
			stream.write(Helpers.FloatToBytesB(this.Z));
			stream.write(Helpers.FloatToBytesB(this.S));
		}
	}

	/**
	 * Initializes a vector from a float array
	 * 
	 * @param vec
	 *           the vector to intialize
	 * @param arr
	 *            is the float array
	 * @param pos
	 *            Beginning position in the float array
	 */
	public static Vector4 fromArray(final Vector4 vec, final float[] arr, final int pos)
	{
		if (arr.length >= (pos + 4))
		{		
			vec.X = arr[pos];
			vec.Y = arr[pos + 1];
			vec.Z = arr[pos + 2];
			vec.S = arr[pos + 3];
		}
		return vec;
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
			this.X = Helpers.BytesToFloatL(bytes, pos);
			this.Y = Helpers.BytesToFloatL(bytes, pos + 4);
			this.Z = Helpers.BytesToFloatL(bytes, pos + 8);
			this.S = Helpers.BytesToFloatL(bytes, pos + 12);
		}
		else
		{
			this.X = Helpers.BytesToFloatB(bytes, pos);
			this.Y = Helpers.BytesToFloatB(bytes, pos + 4);
			this.Z = Helpers.BytesToFloatB(bytes, pos + 8);
			this.S = Helpers.BytesToFloatB(bytes, pos + 12);
		}
	}

	/**
	 * Builds a vector from a data stream
	 * 
	 * @param is
	 *            DataInputStream to read the vector from
	 * @throws IOException 
	 */
	public void fromBytes(final DataInputStream is) throws IOException
	{
		this.X = is.readFloat();
		this.Y = is.readFloat();
		this.Z = is.readFloat();
	}

	public void fromBytes(final SwappedDataInputStream is) throws IOException
	{
		this.X = is.readFloat();
		this.Y = is.readFloat();
		this.Z = is.readFloat();
	}

	/**
	 * Serializes this vector into four bytes in a byte array
	 * 
	 * @param dest
	 *            Destination byte array
	 * @param pos
	 *            Position in the destination array to start writing. Must be at
	 *            least 4 bytes before the end of the array
	 * @return number of bytes filled to the byte array
	 */
	public int toBytes(final byte[] dest)
	{
		return this.toBytes(dest, 0, false);
	}

	/**
	 * Serializes this color into four bytes in a byte array
	 * 
	 * @param dest Destination byte array
	 * @param pos Position in the destination array to start writing. Must be at
	 *            least 4 bytes before the end of the array
	 * @return number of bytes filled to the byte array
	 */
	public int toBytes(final byte[] dest, final int pos)
	{
		return this.toBytes(dest, pos, false);
	}
	
	/**
	 * Serializes this color into four bytes in a byte array
	 * 
	 * @param dest Destination byte array
	 * @param pos Position in the destination array to start writing. Must be at
	 *            least 4 bytes before the end of the array
	 * @return number of bytes filled to the byte array
	 */
	public int toBytes(final byte[] dest, int pos, final boolean le)
	{
		if (le)
		{
			pos += Helpers.FloatToBytesL(this.X, dest, pos);
			pos += Helpers.FloatToBytesL(this.Y, dest, pos);
			pos += Helpers.FloatToBytesL(this.Z, dest, pos);
			pos += Helpers.FloatToBytesL(this.S, dest, pos);
		}
		else
		{
			pos += Helpers.FloatToBytesB(this.X, dest, pos);
			pos += Helpers.FloatToBytesB(this.Y, dest, pos);
			pos += Helpers.FloatToBytesB(this.Z, dest, pos);
			pos += Helpers.FloatToBytesB(this.S, dest, pos);
		}
		return 16;
	}

	public static Vector4 parse(final XmlPullParser parser) throws XmlPullParserException, IOException
	{
		return new Vector4(parser);
	}
	
	public void serializeXml(final XmlSerializer writer, final String namespace, final String name) throws IllegalArgumentException, IllegalStateException, IOException
	{
		writer.startTag(namespace, name);
		writer.startTag(namespace, "X").text(Float.toString(this.X)).endTag(namespace, "X");
		writer.startTag(namespace, "Y").text(Float.toString(this.Y)).endTag(namespace, "Y");
		writer.startTag(namespace, "Z").text(Float.toString(this.Z)).endTag(namespace, "Z");
		writer.startTag(namespace, "S").text(Float.toString(this.S)).endTag(namespace, "S");
		writer.startTag(namespace, name);
	}

	public void serializeXml(final XmlSerializer writer, final String namespace, final String name, final Locale locale) throws IllegalArgumentException, IllegalStateException, IOException
	{
		writer.startTag(namespace, name);
		writer.startTag(namespace, "X").text(String.format(locale, "%f", this.X)).endTag(namespace, "X");
		writer.startTag(namespace, "Y").text(String.format(locale, "%f", this.Y)).endTag(namespace, "Y");
		writer.startTag(namespace, "Z").text(String.format(locale, "%f", this.Z)).endTag(namespace, "Z");
		writer.startTag(namespace, "S").text(String.format(locale, "%f", this.S)).endTag(namespace, "S");
		writer.startTag(namespace, name);
	}

	@Override
	public String toString()
	{
		return String.format(Helpers.EnUsCulture, "<%.3f, %.3f, %.3f, %.3f>", this.X, this.Y, this.Z, this.S);
	}

	public boolean equals(final Vector4 val)
	{
		return null != val && this.X == val.X && this.Y == val.Y && this.Z == val.Z && this.S == val.S;
	}

	@Override
	public boolean equals(final Object obj)
	{
		return null != obj && (obj instanceof Vector4) && this.equals((Vector4)obj);
	}

	@Override
	public int hashCode()
	{
		int hashCode = ((Float) this.X).hashCode();
		hashCode = hashCode * 31 + ((Float) this.Y).hashCode();
		hashCode = hashCode * 31 + ((Float) this.Z).hashCode();
		hashCode = hashCode * 31 + ((Float) this.S).hashCode();
		return  hashCode;
	}

	/** A vector with a value of 0,0,0,0 */
	public static final Vector4 Zero = new Vector4(0.0f);
	/** A vector with a value of 1,1,1 */
	public static final Vector4 One = new Vector4(1.0f, 1.0f, 1.0f, 1.0f);
	/** A unit vector facing forward (X axis), value 1,0,0,0 */
	public static final Vector4 UnitX = new Vector4(1.0f, 0.0f, 0.0f, 0.0f);
	/** A unit vector facing left (Y axis), value 0,1,0,0 */
	public static final Vector4 UnitY = new Vector4(0.0f, 1.0f, 0.0f, 0.0f);
	/** A unit vector facing up (Z axis), value 0,0,1,0 */
	public static final Vector4 UnitZ = new Vector4(0.0f, 0.0f, 1.0f, 0.0f);
	/** A unit vector facing up (S axis), value 0,0,0,1 */
	public static final Vector4 UnitS = new Vector4(0.0f, 0.0f, 0.0f, 1.0f);
}
