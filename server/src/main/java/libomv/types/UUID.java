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
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import libomv.utils.Helpers;
import libomv.utils.RefObject;

// A 128-bit Universally Unique Identifier, used throughout SL and OpenSim
public class UUID implements Serializable
{
	private static final long serialVersionUID = 1L;

	private byte[] data;

	private static byte[] makeNewGuid()
	{
		final SecureRandom rand = new SecureRandom();
		final byte[] guid = new byte[16];
		rand.nextBytes(guid);
		return guid;
	}

	/**
	 * Constructor that creates a new random UUID representation
	 */
	public UUID()
	{
		this.data = UUID.makeNewGuid();
	}

	/**
	 * Constructor that takes a string UUID representation
	 * 
	 * @param string
	 *            A string representation of a UUID, case insensitive and can
	 *            either be hyphenated or non-hyphenated
	 *            <example>UUID("11f8aa9c-b071-4242-836b-13b7abe0d489"
	 *            )</example>
	 */
	public UUID(final String string)
	{
		this.fromString(string);
	}

	/**
	 * Constructor that takes a ByteBuffer containing a UUID
	 * 
	 * @param byteArray
	 *            ByteBuffer containing a 16 byte UUID
	 */
	public UUID(final ByteBuffer byteArray)
	{
		this.data = new byte[16];
		byteArray.get(this.data);
	}

	/**
	 * Constructor that takes a byte array containing a UUID
	 * 
	 * @param byteArray
	 *            Byte array containing a 16 byte UUID
	 */
	public UUID(final byte[] byteArray)
	{
		this(byteArray, 0);
	}

	public UUID(final byte[] byteArray, final int pos)
	{
		this.data = new byte[16];
		System.arraycopy(byteArray, pos, this.data, 0, Math.min(byteArray.length, 16));
	}

	/**
	 * Constructor that takes an unsigned 64-bit unsigned integer to convert to
	 * a UUID
	 * 
	 * @param value
	 *            64-bit unsigned integer to convert to a UUID
	 */
	public UUID(final long value)
	{
		this(value, false);
	}

	public UUID(final long value, final boolean le)
	{
		this.data = new byte[16];
		if (le)
		{
			Helpers.UInt64ToBytesL(value, this.data, 0);
		}
		else
		{
			Helpers.UInt64ToBytesB(value, this.data, 8);
		}
	}

	public UUID(final boolean randomize)
	{
		if (randomize)
		{
			this.data = UUID.makeNewGuid();
		}
		else
		{
			this.data = new byte[16];
		}
	}

	public UUID(final XmlPullParser parser) throws XmlPullParserException, IOException
	{
		// entering with event on START_TAG for the tag name identifying the UUID
		// call nextTag() to proceed to inner <UUID> or <Guid> element
		final int eventType = parser.next();
		switch (eventType)
		{
			case XmlPullParser.START_TAG:
				if ("GUID".equalsIgnoreCase(parser.getName()) || "UUID".equalsIgnoreCase(parser.getName()))
				{
					// we got apparently an UUID, try to create it from the string
					this.fromString(parser.nextText());
				}
				else
				{
					// apperently not an UUID, skip entire element and generate UUID.Zero  
					Helpers.skipElement(parser);
				}
				parser.nextTag(); // Advance to outer end tag
				break;
			case XmlPullParser.TEXT:
				this.fromString(parser.getText());
				parser.nextTag(); // Advance to end tag
				break;
			case XmlPullParser.END_TAG:
				// empty outer tag, generate UUID.Zero  
			    break;
			default:
	    		throw new XmlPullParserException("Unexpected Tag event " + eventType + " for tag name " + parser.getName(), parser, null);
		}
		if (null == data)
			this.data = new byte[16];
	}

	/**
	 * Copy constructor
	 * 
	 * @param val
	 *            UUID to copy
	 */
	public UUID(final UUID val)
	{
		this.data = new byte[16];
		System.arraycopy(val.data, 0, this.data, 0, 16);
	}

	/**
	 * Parses a string UUID representation and assigns its value to the object
	 * <example
	 * >uuid.FromString("11f8aa9c-b071-4242-836b-13b7abe0d489")</example>
	 * 
	 * @param string
	 *            A string representation of a UUID, case insensitive and can
	 *            either be hyphenated or non-hyphenated
	 * @return true when successful, false otherwise
	 */
	private boolean fromString(String string)
	{
		// Always create new data array to prevent overwriting byref data
		this.data = new byte[16];

		if (38 <= string.length() && '{' == string.charAt(0) && '}' == string.charAt(37))
		{
			string = string.substring(1, 37);
		}
		else if (36 < string.length())
		{
			string = string.substring(0, 36);
		}
		// Any valid string is now either 32 or 36 bytes long
		if (36 == string.length() && '-' == string.charAt(8) && '-' == string.charAt(13) &&
				'-' == string.charAt(18) && '-' == string.charAt(23))
		{
			string = string.substring(0, 36).replaceAll("-", "");
		}

		// Any valid string contains now only hexadecimal characters in its first 32 bytes	
		if (32 <= string.length() && string.substring(0, 32).matches("[0-9A-Fa-f]+"))
		{
			try
			{
				for (int i = 0; 16 > i; ++i)
				{
					this.data[i] = (byte) Integer.parseInt(string.substring(i * 2, (i * 2) + 2), 16);
				}
				return true;
			}
			catch (final NumberFormatException ex)
			{}
		}
		return false;
	}

	/**
	 * Returns a copy of the raw bytes for this UUID
	 * 
	 * @return A 16 byte array containing this UUID
	 */
	public byte[] getBytes()
	{
		return this.data;
	}

	/**
	 * Copies the raw bytes for this UUID into a ByteBuffer
	 * 
	 * @param bytes
	 *            The ByteBuffer in which the 16 byte of this UUID are copied
	 */
	public void write(final ByteBuffer bytes)
	{
		bytes.put(this.data);
	}

	/**
	 * Copies the raw bytes for this UUID into an OutputStreaam
	 * 
	 * @param stream
	 *            The OutputStream in which the 16 byte of this UUID are copied
	 */
	public void write(final OutputStream stream) throws IOException
	{
		stream.write(this.data);
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
		final int length = Math.min(this.data.length, dest.length - pos);
		System.arraycopy(this.data, 0, dest, pos, length);
		return length;
	}

	public long AsLong()
	{
		return this.AsLong(false);
	}

	public long AsLong(final boolean le)
	{
		if (le)
			return Helpers.BytesToUInt64L(this.data);

		return Helpers.BytesToUInt64B(this.data);
	}

	/**
	 * Calculate an LLCRC (cyclic redundancy check) for this LLUUID
	 * 
	 * @returns The CRC checksum for this UUID
	 */
	public long CRC()
	{
		long retval = 0;

		retval += ((this.data[3] << 24) + (this.data[2] << 16) + (this.data[1] << 8) + this.data[0]);
		retval += ((this.data[7] << 24) + (this.data[6] << 16) + (this.data[5] << 8) + this.data[4]);
		retval += ((this.data[11] << 24) + (this.data[10] << 16) + (this.data[9] << 8) + this.data[8]);
		retval += ((this.data[15] << 24) + (this.data[14] << 16) + (this.data[13] << 8) + this.data[12]);

		return retval;
	}

	public static UUID GenerateUUID()
	{
		return new UUID(UUID.makeNewGuid());
	}
	
	public void serializeXml(final XmlSerializer writer, final String namespace, final String name) throws IllegalArgumentException, IllegalStateException, IOException
	{
        writer.startTag(namespace, name);
	    writer.startTag(namespace, "UUID").text(this.toString()).endTag(namespace, "UUID");
        writer.endTag(namespace, name);
	}

	/**
	 * Return a hash code for this UUID
	 * 
	 * @return An integer composed of all the UUID bytes XORed together
	 */
	@Override
	public int hashCode()
	{
		return Arrays.hashCode(this.data);
	}

	/**
	 * Comparison function
	 * 
	 * @param obj
	 *            An object to compare to this UUID
	 * @return True if the object is a UUID and both UUIDs are equal
	 */
	@Override
	public boolean equals(final Object obj)
	{
		return null != obj && obj instanceof UUID && this.equals((UUID)obj);
	}

	/**
	 * Comparison function
	 * 
	 * @param uuid
	 *            UUID to compare to
	 * @return True if the UUIDs are equal, otherwise false
	 */
	public boolean equals(final UUID uuid)
	{
		if (null != uuid)
		{
			if (null == uuid.data && null == this.data)
				return true;
			
			if (null != uuid.data && null != this.data)
				return Arrays.equals(data, uuid.data);
		}
		return false;
	}

	/**
	 * Generate a UUID from a string
	 * 
	 * @param val A string representation of a UUID, case insensitive and can
	 *            either be hyphenated or non-hyphenated
	 *            example: UUID.Parse("11f8aa9c-b071-4242-836b-13b7abe0d489")
	 * @returns a new UUID if successful, null otherwise
	 */
	public static UUID parse(final String val)
	{
		final UUID uuid = new UUID(false);
		if (uuid.fromString(val))
			return uuid;
		return null;
	}

	/**
	 * Generate a UUID from a string
	 * 
	 * @param val
	 *            A string representation of a UUID, case insensitive and can
	 *            either be hyphenated or non-hyphenated
	 * @param result
	 *            Will contain the parsed UUID if successful, otherwise null
	 * @return True if the string was successfully parse, otherwise false
	 *         <example>UUID.TryParse("11f8aa9c-b071-4242-836b-13b7abe0d489",
	 *         result)</example>
	 */
	public static boolean TryParse(final String val, final RefObject<UUID> result)
	{
		if (null == val || 0 == val.length() || ('{' == val.charAt(0) && 38 > val.length()) || (36 > val.length() && 32 != val.length()))
		{
			result.argvalue = UUID.Zero;
			return false;
		}

		try
		{
			result.argvalue = UUID.parse(val);
			return true;
		}
		catch (final Throwable t)
		{
			result.argvalue = UUID.Zero;
			return false;
		}
	}

	/**
	 * Combine two UUIDs together by taking the MD5 hash of a byte array
	 * containing both UUIDs
	 * 
	 * @param first
	 *            First UUID to combine
	 * @param second
	 *            Second UUID to combine
	 * @return The UUID product of the combination
	 */
	public static UUID Combine(final UUID first, final UUID second)
	{
		final MessageDigest md;
		try
		{
			md = MessageDigest.getInstance("MD5");
		}
		catch (final NoSuchAlgorithmException e)
		{
			return null;
		}

		// Construct the buffer that MD5ed
		final byte[] input = new byte[32];
		first.toBytes(input, 0);
		second.toBytes(input, 16);
		return new UUID(md.digest(input));
	}

	/**
	 * XOR two UUIDs together
	 * 
	 * @param uuid
	 *            UUID to combine
	 */
	public void XOr(final UUID uuid)
	{
		int i = 0;
		for (final byte b : uuid.getBytes())
		{
			this.data[i] ^= b;
			i++;
		}
	}

	public static UUID XOr(final UUID first, final UUID second)
	{
		final UUID uuid = new UUID(first);
		uuid.XOr(second);
		return uuid;
	}

	/**
	 * Get a hyphenated string representation of this UUID
	 * 
	 * @return A string representation of this UUID, lowercase and with hyphens
	 *         <example>11f8aa9c-b071-4242-836b-13b7abe0d489</example>
	 */
	@Override
	public String toString()
	{
		if (null == data)
		{
			return UUID.ZeroString;
		}

		final StringBuffer uuid = new StringBuffer(36);

		for (int i = 0; 16 > i; ++i)
		{
			final byte value = this.data[i];
			uuid.append(String.format("%02x", value & 0xFF));
			if (3 == i || 5 == i || 7 == i || 9 == i)
			{
				uuid.append("-");
			}
		}
		return uuid.toString();
	}

	public boolean isZero()
	{
		return this.equals(UUID.Zero);
	}
	
	public static boolean isZero(final UUID uuid)
	{
		if (null != uuid)
			return uuid.equals(UUID.Zero);
		return false;
	}
	
	public static boolean isZeroOrNull(final UUID uuid)
	{
		if (null != uuid)
			return uuid.equals(UUID.Zero);
		return true;
	}

	/** An UUID with a value of all zeroes */
	public static final UUID Zero = new UUID(false);

	/** A cache of UUID.Zero as a string to optimize a common path */
	private static final String ZeroString = UUID.Zero.toString();
}
