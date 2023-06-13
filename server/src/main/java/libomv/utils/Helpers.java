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
package libomv.utils;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.CodeSource;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import libomv.StructuredData.OSD;
import libomv.StructuredData.OSDParser;
import libomv.types.UUID;
import libomv.types.Vector3;
import libomv.types.Vector3d;

public enum Helpers
{
	;
	public static final double DOUBLE_MAG_THRESHOLD = 1.0E-14f;
	public static final float FLOAT_MAG_THRESHOLD = 1.0E-7f;
//	public static final float E = (float) Math.E;
	public static final float LOG10E = 0.4342945f;
	public static final float LOG2E = 1.442695f;
//	public static final float PI = (float) Math.PI;
	public static final float TWO_PI = (float) (Math.PI * 2.0d);
	public static final float PI_OVER_TWO = (float) (Math.PI / 2.0d);
	public static final float PI_OVER_FOUR = (float) (Math.PI / 4.0d);

	/** Used for converting radians to degrees */
	public static final float RAD_TO_DEG = (float) (180.0d / Math.PI);

	/**
	 * Provide a single instance of the Locale class to help parsing in
	 * situations where the grid assumes an en-us culture
	 */
	public static final Locale EnUsCulture = new Locale("en", "us");

	/** Default encoding (UTF-8) */
	public static final String UTF8_ENCODING = "UTF-8";
	public static final String ASCII_ENCODING = "ASCII";

	public static final byte[] EmptyBytes = new byte[0];
	public static final String EmptyString = "";

	public static final String NewLine = System.getProperty("line.separator");
	/** UNIX epoch in DateTime format */
	public static final Date Epoch = new Date(0);

	protected static final String FRACT_DATE_FMT = "yyyy-MM-DD'T'hh:mm:ss.SS'Z'";
	protected static final String WHOLE_DATE_FMT = "yyyy-MM-DD'T'hh:mm:ss'Z'";

	/**
	 * Calculate the MD5 hash of a given string
	 * 
	 * @param password
	 *            The password to hash
	 * @return An MD5 hash in string format, with $1$ prepended
	 */
	public static String MD5Password(final String password)
	{
		final StringBuilder digest = new StringBuilder(32);
		try
		{
			final MessageDigest md = MessageDigest.getInstance("MD5");
			final byte[] hash = md.digest(password.getBytes(StandardCharsets.US_ASCII));

			// Convert the hash to a hex string
			for (final byte b : hash)
			{
				digest.append(String.format(Helpers.EnUsCulture, "%02x", b));
			}
			return "$1$" + digest;
		}
		catch (final Exception e)
		{
		}
		return Helpers.EmptyString;
	}

	/**
	 * Clamp a given value between a range
	 * 
	 * @param value
	 *            Value to clamp
	 * @param min
	 *            Minimum allowable value
	 * @param max
	 *            Maximum allowable value
	 * @return A value inclusively between lower and upper
	 */
	public static float Clamp(float value, final float min, final float max)
	{
		// First we check to see if we're greater than the max
		value = (value > max) ? max : value;

		// Then we check to see if we're less than the min.
		value = (value < min) ? min : value;

		// There's no check to see if min > max.
		return value;
	}

	/**
	 * Clamp a given value between a range
	 * 
	 * @param value
	 *            Value to clamp
	 * @param min
	 *            Minimum allowable value
	 * @param max
	 *            Maximum allowable value
	 * @return A value inclusively between lower and upper
	 */
	public static double Clamp(double value, final double min, final double max)
	{
		// First we check to see if we're greater than the max
		value = (value > max) ? max : value;

		// Then we check to see if we're less than the min.
		value = (value < min) ? min : value;

		// There's no check to see if min > max.
		return value;
	}

	/**
	 * Clamp a given value between a range
	 * 
	 * @param value
	 *            Value to clamp
	 * @param min
	 *            Minimum allowable value
	 * @param max
	 *            Maximum allowable value
	 * @return A value inclusively between lower and upper
	 */
	public static int Clamp(int value, final int min, final int max)
	{
		// First we check to see if we're greater than the max
		value = (value > max) ? max : value;

		// Then we check to see if we're less than the min.
		value = (value < min) ? min : value;

		// There's no check to see if min > max.
		return value;
	}

	/**
	 * Round a floating-point value away from zero to the nearest integer
	 * 
	 * @param val
	 *            Floating point number to round
	 * @return Integer
	 */
	public static int roundFromZero(final float val)
	{
		if (0 > val)
			return (int) Math.ceil(val - 0.5f);
		return (int) Math.floor(val + 0.5f);
	}

	public static int roundFromZero(final double val)
	{
		if (0 > val)
			return (int) Math.ceil(val - 0.5f);
		return (int) Math.floor(val + 0.5f);
	}

	/** Test if a single precision float is a finite number */
	public static boolean IsFinite(final float value)
	{
		return !(Float.isNaN(value) || Float.isInfinite(value));
	}

	/** Test if a double precision float is a finite number */
	public static boolean IsFinite(final double value)
	{
		return !(Double.isNaN(value) || Double.isInfinite(value));
	}

	/**
	 * Get the distance between two floating-point values
	 * 
	 * @param value1
	 *            First value
	 * @param value2
	 *            Second value
	 * @return The distance between the two values
	 */
	public static float Distance(final float value1, final float value2)
	{
		return Math.abs(value1 - value2);
	}

	public static float Hermite(final float value1, final float tangent1, final float value2, final float tangent2, final float amount)
	{
		// All transformed to double not to lose precision
		// Otherwise, for high numbers of param:amount the result is NaN instead
		// of Infinity
		final double v1 = value1;
		final double v2 = value2;
		final double t1 = tangent1;
		final double t2 = tangent2;
		final double s = amount;
		final double result;
		final double sCubed = s * s * s;
		final double sSquared = s * s;

		if (0.0f == amount)
		{
			result = value1;
		}
		else if (1.0f == amount)
		{
			result = value2;
		}
		else
		{
			result = (2.0d * v1 - 2.0d * v2 + t2 + t1) * sCubed + (3.0d * v2 - 3.0d * v1 - 2.0d * t1 - t2) * sSquared + t1 * s
					+ v1;
		}
		return (float) result;
	}

	public static double Hermite(final double value1, final double tangent1, final double value2, final double tangent2, final double amount)
	{
		// All transformed to double not to lose precision
		// Otherwise, for high numbers of param:amount the result is NaN instead
		// of Infinity
		final double v1 = value1;
		final double v2 = value2;
		final double t1 = tangent1;
		final double t2 = tangent2;
		final double s = amount;
		final double result;
		final double sCubed = s * s * s;
		final double sSquared = s * s;

		if (0.0d == amount)
		{
			result = value1;
		}
		else if (1.0f == amount)
		{
			result = value2;
		}
		else
		{
			result = (2.0d * v1 - 2.0d * v2 + t2 + t1) * sCubed + (3.0d * v2 - 3.0d * v1 - 2.0d * t1 - t2) * sSquared + t1 * s
					+ v1;
		}
		return result;
	}

	public static float Lerp(final float value1, final float value2, final float amount)
	{
		return value1 + (value2 - value1) * amount;
	}

	public static double Lerp(final double value1, final double value2, final double amount)
	{
		return value1 + (value2 - value1) * amount;
	}

	public static float SmoothStep(final float value1, final float value2, final float amount)
	{
		// It is expected that 0 < amount < 1
		// If amount < 0, return value1
		// If amount > 1, return value2
		final float result = Helpers.Clamp(amount, 0.0f, 1.0f);
		return Helpers.Hermite(value1, 0.0f, value2, 0.0f, result);
	}

	public static double SmoothStep(final double value1, final double value2, final double amount)
	{
		// It is expected that 0 < amount < 1
		// If amount < 0, return value1
		// If amount > 1, return value2
		final double result = Helpers.Clamp(amount, 0.0f, 1.0f);
		return Helpers.Hermite(value1, 0.0f, value2, 0.0f, result);
	}

	public static float ToDegrees(final float radians)
	{
		// This method uses double precission internally,
		// though it returns single float
		// Factor = 180 / pi
		return (float) (radians * 57.295779513082320876798154814105);
	}

	public static float ToRadians(final float degrees)
	{
		// This method uses double precission internally,
		// though it returns single float
		// Factor = pi / 180
		return (float) (degrees * 0.017453292519943295769236907684886);
	}

	// Packs to 32-bit unsigned integers in to a 64-bit unsigned integer
	//
	// <param name="a">The left-hand (or X) value</param>
	// <param name="b">The right-hand (or Y) value</param>
	// <returns>A 64-bit integer containing the two 32-bit input
	// values</returns>
	public static long IntsToLong(final int a, final int b)
	{
		return (((long) a << 32) + b);
	}

	// // Unpacks two 32-bit unsigned integers from a 64-bit unsigned integer//
	// // <param name="a">The 64-bit input integer</param>// <param name="b">The
	// left-hand (or X) output value</param>// <param name="c">The right-hand
	// (or Y) output value</param>
	public static void LongToUInts(final long a, final int[] b)
	{
		b[0] = (int) (a >> 32);
		b[1] = (int) (a & 0x00000000FFFFFFFFL);
	}

	/**
	 * Converts a floating point number to a terse string format used for
	 * transmitting numbers in wearable asset files
	 * 
	 * @param val
	 *            Floating point number to convert to a string
	 * @return A terse string representation of the input number
	 */
	public static String FloatToTerseString(final float val)
	{
		if (0 == val)
		{
			return ".00";
		}
		String s = String.format(Locale.ENGLISH, "%f", val);

		// Trim trailing zeroes
		int i = s.length();
		while ('0' == s.charAt(i - 1))
			i--;
		s = s.substring(0, i);

		// Remove superfluous decimal places after the trim
		if ('.' == s.charAt(i - 1))
		{
			--i;
			s = s.substring(0, i);
		}
		// Remove leading zeroes after a negative sign
		else if ('-' == s.charAt(0) && '0' == s.charAt(1))
		{
			s = "-" + s.substring(2, i);
		}
		// Remove leading zeroes in positive numbers
		else if ('0' == s.charAt(0))
		{
			s = s.substring(1, i);
		}
		return s;
	}

	// Convert a variable length field (byte array) to a String.
	//
	// <remarks>If the byte array has unprintable characters in it, a
	// hex dump will be put in the String instead</remarks>
	// <param name="bytes">The byte array to convert to a String</param>
	// <returns>A UTF8 String, minus the null terminator</returns>
	public static String FieldToString(final byte[] bytes) throws Exception
	{
		return Helpers.FieldToString(bytes, "");
	}

	// Convert a variable length field (byte array) to a String, with a
	// field name prepended to each line of the output.
	//
	// <remarks>If the byte array has unprintable characters in it, a
	// hex dump will be put in the String instead</remarks>
	// <param name="bytes">The byte array to convert to a String</param>
	// <param name="fieldName">A field name to prepend to each line of
	// output</param>
	// <returns>A UTF8 String, minus the null terminator</returns>
	public static String FieldToString(final byte[] bytes, final String fieldName) throws Exception
	{
		String output = "";
		boolean printable = true;

		for (final byte element : bytes)
		{
			// Check if there are any unprintable characters in the array
			if ((0x20 > element || 0x7E < element) && 0x09 != element && 0x0D != element && 0x0A != element
					&& 0x00 != element)
			{
				printable = false;
				break;
			}
		}

		if (printable)
		{
			final int length = bytes.length;
			if (0 < length)
			{
				output += fieldName + ": ";
			}
			if (0 == bytes[length - 1])
				output += new String(bytes, 0, length - 1, StandardCharsets.UTF_8);
			else
				output += new String(bytes, StandardCharsets.UTF_8);
		}
		else
		{
			for (int i = 0; i < bytes.length; i += 16)
			{
				if (0 != i)
				{
					output += "\n";
				}
				if ("" != fieldName)
				{
					output += fieldName + ": ";
				}

				for (int j = 0; 16 > j; j++)
				{
					if ((i + j) < bytes.length)
					{
						String s = Integer.toHexString(bytes[i + j]);
						// String s = String.Format("{0:X} ", bytes[i + j]);
						if (2 == s.length())
						{
							s = "0" + s;
						}

						output += s;
					}
					else
					{
						output += "   ";
					}
				}

				for (int j = 0; 16 > j && (i + j) < bytes.length; j++)
				{
					if (0x20 <= bytes[i + j] && 0x7E > bytes[i + j])
					{
						output += (char) bytes[i + j];
					}
					else
					{
						output += ".";
					}
				}
			}
		}

		return output;
	}

	/**
	 * Convert a UTF8 String to a byte array
	 * 
	 * @param str
	 *            The String to convert to a byte array
	 * @return A null-terminated byte array
	 * @throws Exception
	 */
	public static byte[] StringToField(String str) throws Exception
	{
		if (!str.endsWith("\0"))
		{
			str += "\0";
		}
		return str.getBytes(StandardCharsets.UTF_8);
	}

	/**
	 * Converts a struct or class object containing fields only into a key value
	 * separated string
	 * 
	 * @param t
	 *            The struct object
	 * @return A string containing the struct fields as the keys, and the field
	 *         value as the value separated <example> <code>
	 *  // Add the following code to any struct or class containing only fields to override the toString()
	 *  // method to display the values of the passed object
	 * 
	 *  /** Print the struct data as a string
	 *   *  @return A string containing the field names, and field values
	 *   * /
	 * @Override public override string toString() { return
	 *           Helpers.StructToString(this); } </code> </example>
	 */
	public static String StructToString(final Object t)
	{
		final StringBuilder result = new StringBuilder();
		final java.lang.Class<?> structType = t.getClass();

		for (final Field field : structType.getDeclaredFields())
		{
			try
			{
				result.append(field.getName() + ": " + field.get(t).toString() + " ");
			}
			catch (final IllegalArgumentException e)
			{
				e.printStackTrace();
			}
			catch (final IllegalAccessException e)
			{
				e.printStackTrace();
			}
		}
		result.append("\n");
		return result.toString().trim();
	}

	public static double getUnixTime()
	{
		return Helpers.Epoch.getTime() / 1000.0;
	}

	public static double DateTimeToUnixTime(final Date date)
	{
		return date.getTime() / 1000.0;
	}

	public static Date UnixTimeToDateTime(final double time)
	{
		return new Date(Math.round(time * 1000.0));
	}

	public static Date StringToDate(final String string)
	{
		final SimpleDateFormat df = new SimpleDateFormat(Helpers.FRACT_DATE_FMT);
		try
		{
			return df.parse(string);
		}
		catch (final ParseException ex1)
		{
			try
			{
				df.applyPattern(Helpers.WHOLE_DATE_FMT);
				return df.parse(string);
			}
			catch (final ParseException ex2)
			{
			}
		}
		return Epoch;
	}

	/**
	 * Given an X/Y location in absolute (grid-relative) terms, a region handle
	 * is returned along with the local X/Y location in that region
	 * 
	 * @param globalX
	 *            The absolute X location, a number such as 255360.35
	 * @param globalY
	 *            The absolute Y location, a number such as 255360.35
	 * @param locals
	 *            [0] The returned sim-local X position of the global X
	 * @param locals
	 *            [1] The returned sim-local Y position of the global Y
	 * @return A 64-bit region handle that can be used to teleport to
	 */
	public static long GlobalPosToRegionHandle(final float globalX, final float globalY, final float[] locals)
	{
		final int x = ((int) globalX >> 8) << 8;
		final int y = ((int) globalY >> 8) << 8;
		locals[0] = globalX - x;
		locals[1] = globalY - y;
		return Helpers.IntsToLong(x, y);
	}

	public static Vector3d RegionHandleToGlobalPos(final long regionHandle, final Vector3 local)
	{
		final int[] globals = new int[2];
		Helpers.LongToUInts(regionHandle, globals);
		return new Vector3d(globals[0] + local.X, globals[1] + local.Y, local.Z);
	}

	// Calculates the CRC (cyclic redundancy check) needed to upload inventory.
	//
	// <param name="creationDate">Creation date</param>
	// <param name="saleType">Sale type</param>
	// <param name="invType">Inventory type</param>
	// <param name="type">Type</param>
	// <param name="assetID">Asset ID</param>
	// <param name="groupID">Group ID</param>
	// <param name="salePrice">Sale price</param>
	// <param name="ownerID">Owner ID</param>
	// <param name="creatorID">Creator ID</param>
	// <param name="itemID">Item ID</param>
	// <param name="folderID">Folder ID</param>
	// <param name="everyoneMask">Everyone mask (permissions)</param>
	// <param name="flags">Flags</param>
	// <param name="nextOwnerMask">Next owner mask (permissions)</param>
	// <param name="groupMask">Group mask (permissions)</param>
	// <param name="ownerMask">Owner mask (permisions)</param>
	// <returns>The calculated CRC</returns>
	public static int InventoryCRC(final int creationDate, final byte saleType, final byte invType, final byte type, final UUID assetID,
								   final UUID groupID, final int salePrice, final UUID ownerID, final UUID creatorID, final UUID itemID, final UUID folderID, final int everyoneMask,
								   final int flags, final int nextOwnerMask, final int groupMask, final int ownerMask)
	{
		int CRC = 0;

		// IDs
		CRC += assetID.CRC(); // AssetID
		CRC += folderID.CRC(); // FolderID
		CRC += itemID.CRC(); // ItemID

		// Permission stuff
		CRC += creatorID.CRC(); // CreatorID
		CRC += ownerID.CRC(); // OwnerID
		CRC += groupID.CRC(); // GroupID

		// CRC += another 4 words which always seem to be zero -- unclear if
		// this is a LLUUID or what
		CRC += ownerMask;
		CRC += nextOwnerMask;
		CRC += everyoneMask;
		CRC += groupMask;

		// The rest of the CRC fields
		CRC += flags; // Flags
		CRC += invType; // InvType
		CRC += type; // Type
		CRC += creationDate; // CreationDate
		CRC += salePrice; // SalePrice
		CRC += (saleType * 0x07073096); // SaleType

		return CRC;
	}

	public static String toHexText(final byte[] raw_digest)
	{
		// and convert it to hex-text
		final StringBuffer checksum = new StringBuffer(raw_digest.length * 2);
		for (final byte element : raw_digest)
		{
			final int b = element & 0xFF;
			final String hex_value = Integer.toHexString(b);
			if (1 == hex_value.length())
			{
				// the Java function returns a single digit if hex is < 0x10
				checksum.append("0");
			}
			checksum.append(hex_value);
		}
		return checksum.toString();
	}

	public static Vector<String> split(final String s, final String c)
	{
		final Vector<String> v = new Vector<String>();
		final StringTokenizer tokens = new StringTokenizer(s, c);
		while (tokens.hasMoreTokens())
		{
			v.addElement(tokens.nextToken());
		}
		return v;
	}

	public static String replaceAll(final String line, final String from, final String to)
	{
		if (null == line)
		{
			return null;
		}
		if (null == from || "".equals(from))
		{
			return "";
		}
		final StringBuffer buf = new StringBuffer();
		int line_pos = 0;
		do
		{
			int pos = line.indexOf(from, line_pos);
			if (-1 == pos)
			{
				pos = line.length();
			}
			final String chunk = line.substring(line_pos, pos);
			buf.append(chunk);
			if (pos != line.length())
			{
				buf.append(to);
			}
			line_pos += chunk.length() + from.length();
		} while (line_pos < line.length());
		return buf.toString();
	}

	public static String join(final String delimiter, final String[] strings)
	{
		if (0 == strings.length)
			return Helpers.EmptyString;
		int capacity = (strings.length - 1) * delimiter.length();
		for (final String s : strings)
		{
			capacity += s.length();
		}

		final StringBuilder buffer = new StringBuilder(capacity);
		for (final String s : strings)
		{
			if (0 > capacity)
				buffer.append(delimiter);
			buffer.append(s);
			capacity = -1;
		}
		return buffer.toString();
	}

	/**
	 * Convert the first two bytes starting in the byte array in little endian
	 * ordering to a signed short integer
	 * 
	 * @param bytes
	 *            An array two bytes or longer
	 * @return A signed short integer, will be zero if a short can't be read at
	 *         the given position
	 */
	public static short BytesToInt16L(final byte[] bytes)
	{
		return Helpers.BytesToInt16L(bytes, 0);
	}

	/**
	 * Convert the first two bytes starting at the given position in little
	 * endian ordering to a signed short integer
	 * 
	 * @param bytes
	 *            An array two bytes or longer
	 * @param pos
	 *            Position in the array to start reading
	 * @return A signed short integer, will be zero if a short can't be read at
	 *         the given position
	 */
	public static short BytesToInt16L(final byte[] bytes, final int pos)
	{
		if (bytes.length < pos + 2)
		{
			return 0;
		}
		return (short) (((bytes[pos] & 0xff) << 0) + ((bytes[pos + 1] & 0xff) << 8));
	}

	public static short BytesToInt16B(final byte[] bytes)
	{
		return Helpers.BytesToInt16B(bytes, 0);
	}

	public static short BytesToInt16B(final byte[] bytes, final int pos)
	{
		if (bytes.length < pos + 2)
		{
			return 0;
		}
		return (short) (((bytes[pos] & 0xff) << 8) + ((bytes[pos + 1] & 0xff) << 0));
	}

	/**
	 * Convert the first four bytes of the given array in little endian ordering
	 * to a signed integer
	 * 
	 * @param bytes
	 *            An array four bytes or longer
	 * @return A signed integer, will be zero if the array contains less than
	 *         four bytes
	 */
	public static int BytesToInt32L(final byte[] bytes)
	{
		return Helpers.BytesToInt32L(bytes, 0);
	}

	/**
	 * Convert the first four bytes starting at the given position in little
	 * endian ordering to a signed integer
	 * 
	 * @param bytes
	 *            An array four bytes or longer
	 * @param pos
	 *            Position to start reading the int from
	 * @return A signed integer, will be zero if an int can't be read at the
	 *         given position
	 */
	public static int BytesToInt32L(final byte[] bytes, final int pos)
	{
		if (bytes.length < pos + 4)
		{
			return 0;
		}
		return ((bytes[pos] & 0xff) + ((bytes[pos + 1] & 0xff) << 8) + ((bytes[pos + 2] & 0xff) << 16) + ((bytes[pos + 3] & 0xff) << 24));
	}

	public static int BytesToInt32B(final byte[] bytes)
	{
		return Helpers.BytesToInt32B(bytes, 0);
	}

	public static int BytesToInt32B(final byte[] bytes, final int pos)
	{
		if (bytes.length < pos + 4)
		{
			return 0;
		}
		return (((bytes[pos] & 0xff) << 24) + ((bytes[pos + 1] & 0xff) << 16) + ((bytes[pos + 2] & 0xff) << 8) + (bytes[pos + 3] & 0xff));
	}

	/**
	 * Convert the first eight bytes of the given array in little endian
	 * ordering to a signed long integer
	 * 
	 * @param bytes
	 *            An array eight bytes or longer
	 * @return A signed long integer, will be zero if the array contains less
	 *         than eight bytes
	 */
	public static long BytesToInt64L(final byte[] bytes)
	{
		return Helpers.BytesToInt64L(bytes, 0);
	}

	/**
	 * Convert the first eight bytes starting at the given position in little
	 * endian ordering to a signed long integer
	 * 
	 * @param bytes
	 *            An array eight bytes or longer
	 * @param pos
	 *            Position to start reading the long from
	 * @return A signed long integer, will be zero if a long can't be read at
	 *         the given position
	 */
	public static long BytesToInt64L(final byte[] bytes, final int pos)
	{
		if (8 > bytes.length)
		{
			return 0;
		}
		final long low = ((bytes[pos] & 0xff) + ((bytes[pos + 1] & 0xff) << 8) + ((bytes[pos + 2] & 0xff) << 16) + ((long) (bytes[pos + 3] & 0xff) << 24));
		final long high = ((bytes[pos + 4] & 0xff) + ((bytes[pos + 5] & 0xff) << 8) + ((bytes[pos + 6] & 0xff) << 16) + ((long) (bytes[pos + 7] & 0xff) << 24));
		return (high << 32) + (low & 0xffffffffL);
	}

	public static long BytesToInt64B(final byte[] bytes)
	{
		return Helpers.BytesToInt64B(bytes, 0);
	}

	public static long BytesToInt64B(final byte[] bytes, final int pos)
	{
		if (8 > bytes.length)
		{
			return 0;
		}
		final long high = (((long) (bytes[pos] & 0xff) << 24) + ((bytes[pos + 1] & 0xff) << 16) + ((bytes[pos + 2] & 0xff) << 8) + (bytes[pos + 3] & 0xff));
		final long low = (((long) (bytes[pos + 4] & 0xff) << 24) + ((bytes[pos + 5] & 0xff) << 16) + ((bytes[pos + 6] & 0xff) << 8) + (bytes[pos + 7] & 0xff));
		return (high << 32) + (low & 0xffffffffL);
	}

	/**
	 * Convert two bytes in little endian ordering to an int
	 * 
	 * @param bytes
	 *            Byte array containing the ushort
	 * @return An int, will be zero if a ushort can't be read
	 */
	public static int BytesToUInt16L(final byte[] bytes)
	{
		return Helpers.BytesToUInt16L(bytes, 0);
	}

	/**
	 * Convert the first two bytes starting at the given position in little
	 * endian ordering to an int
	 * 
	 * @param bytes
	 *            Byte array containing the ushort
	 * @param pos
	 *            Position to start reading the ushort from
	 * @return An int, will be zero if a ushort can't be read at the given
	 *         position
	 */
	public static int BytesToUInt16L(final byte[] bytes, final int pos)
	{
		if (bytes.length < pos + 2)
		{
			return 0;
		}
		return ((bytes[pos] & 0xff) + ((bytes[pos + 1] & 0xff) << 8));
	}

	public static int BytesToUInt16B(final byte[] bytes)
	{
		return Helpers.BytesToUInt16B(bytes, 0);
	}

	public static int BytesToUInt16B(final byte[] bytes, final int pos)
	{
		if (bytes.length < pos + 2)
		{
			return 0;
		}
		return (((bytes[pos] & 0xff) << 8) + (bytes[pos + 1] & 0xff));
	}

	/**
	 * Convert the first four bytes of the given array in little endian ordering
	 * to long
	 * 
	 * @param bytes
	 *            An array four bytes or longer
	 * @return An unsigned integer, will be zero if the array contains less than
	 *         four bytes
	 */
	public static long BytesToUInt32L(final byte[] bytes)
	{
		return Helpers.BytesToUInt32L(bytes, 0);
	}

	/**
	 * Convert the first four bytes starting at the given position in little
	 * endian ordering to a long
	 * 
	 * @param bytes
	 *            Byte array containing the uint
	 * @param pos
	 *            Position to start reading the uint from
	 * @return An unsigned integer, will be zero if a uint can't be read at the
	 *         given position
	 */
	public static long BytesToUInt32L(final byte[] bytes, final int pos)
	{
		if (bytes.length < pos + 4)
		{
			return 0;
		}
		final long low = ((bytes[pos] & 0xff) + ((bytes[pos + 1] & 0xff) << 8) + ((bytes[pos + 2] & 0xff) << 16));
		final long high = bytes[pos + 3] & 0xff;
		return (high << 24) + (0xffffffffL & low);
	}

	public static long BytesToUInt32B(final byte[] bytes)
	{
		return Helpers.BytesToUInt32B(bytes, 0);
	}

	public static long BytesToUInt32B(final byte[] bytes, final int pos)
	{
		if (bytes.length < pos + 4)
		{
			return 0;
		}
		final long low = ((bytes[pos + 3] & 0xff) + ((bytes[pos + 2] & 0xff) << 8) + ((bytes[pos + 1] & 0xff) << 16));
		final long high = bytes[pos] & 0xff;
		return (high << 24) + (0xffffffffL & low);
	}

	/**
	 * Convert the first eight bytes of the given array in little endian
	 * ordering to a constrained long
	 * 
	 * @param bytes
	 *            An array eight bytes or longer
	 * @return An unsigned 64-bit integer, will be zero if the array contains
	 *         less than eight bytes
	 */
	public static long BytesToUInt64L(final byte[] bytes)
	{
		return Helpers.BytesToUInt64L(bytes, 0);
	}

	/**
	 * Convert the first eight bytes starting at the given position in little
	 * endian ordering to a constrained long
	 * 
	 * @param bytes
	 *            An array eight bytes or longer
	 * @return A long integer, will be zero if the array contains less than
	 *         eight bytes and 0x7fffffff if the resulting value would exceed
	 *         the positive limit of a long
	 */
	public static long BytesToUInt64L(final byte[] bytes, final int pos)
	{
		if (8 > bytes.length)
		{
			return 0;
		}

		if (0 > (bytes[pos + 7] & 0xff))
		{
			return 0x7fffffff;
		}
		final long low = ((bytes[pos] & 0xff) + ((bytes[pos + 1] & 0xff) << 8) + ((bytes[pos + 2] & 0xff) << 16) + ((long) (bytes[pos + 3] & 0xff) << 24));
		final long high = ((bytes[pos + 4] & 0xff) + ((bytes[pos + 5] & 0xff) << 8) + ((bytes[pos + 6] & 0xff) << 16) + ((long) (bytes[pos + 7] & 0xff) << 24));
		return (high << 32) + (low & 0xffffffff);
	}

	public static long BytesToUInt64B(final byte[] bytes)
	{
		return Helpers.BytesToUInt64B(bytes, 0);
	}

	public static long BytesToUInt64B(final byte[] bytes, final int pos)
	{
		if (8 > bytes.length)
		{
			return 0;
		}

		if (0 > (bytes[pos] & 0xff))
		{
			return 0x7fffffff;
		}
		final long high = (((long) (bytes[pos] & 0xff) << 24) + ((bytes[pos + 1] & 0xff) << 16) + ((bytes[pos + 2] & 0xff) << 8) + (bytes[pos + 3] & 0xff));
		final long low = (((long) (bytes[pos + 4] & 0xff) << 24) + ((bytes[pos + 5] & 0xff) << 16) + ((bytes[pos + 6] & 0xff) << 8) + (bytes[pos + 7] & 0xff));
		return (high << 32) + (low & 0xffffffff);
	}

	/**
	 * Convert four bytes starting at the given position in little endian
	 * ordering to a floating point value
	 * 
	 * @param bytes
	 *            Byte array containing a little ending floating point value
	 * @param pos
	 *            Starting position of the floating point value in the byte
	 *            array
	 * @return Single precision value
	 */
	public static float BytesToFloatL(final byte[] bytes, final int pos)
	{
		return Float.intBitsToFloat(Helpers.BytesToInt32L(bytes, pos));
	}

	public static float BytesToFloatB(final byte[] bytes, final int pos)
	{
		return Float.intBitsToFloat(Helpers.BytesToInt32B(bytes, pos));
	}

	/**
	 * Convert eight bytes starting at the given position in little endian
	 * ordering to a double floating point value
	 * 
	 * @param bytes
	 *            Byte array containing a little ending double floating point
	 *            value
	 * @param pos
	 *            Starting position of the double floating point value in the
	 *            byte array
	 * @return Double precision value
	 */
	public static double BytesToDoubleL(final byte[] bytes, final int pos)
	{
		return Double.longBitsToDouble(Helpers.BytesToInt64L(bytes, pos));
	}

	public static double BytesToDoubleB(final byte[] bytes, final int pos)
	{
		return Double.longBitsToDouble(Helpers.BytesToInt64B(bytes, pos));
	}

	/**
	 * Convert a fixed point binary value in a byte array into a floating point value
	 * 
	 * Note: This is a specific floating point format used by the Second Life protocol
	 * and works somewhat different for signed numbers, than what IEEE-754 derived schemes
	 * would use.
	 * 
	 * number          SL-FP               IEEE-754
	 *   MIN_NUM       000000000           11111111111
	 *   0.0           100000000           00000000000
	 *   MAX_NUM       111111111           01111111111
	 *   
	 * @param fixedVal
	 * @param signed
	 * @param intBits
	 * @param fracBits
	 * @return
	 */
	private static float FixedToFloat(final long fixedVal, final boolean signed, final int intBits, final int fracBits)
	{
		final int maxVal = 1 << intBits;
		double floatVal = fixedVal / (1L << fracBits);

		if (signed)
		{
			floatVal -= maxVal;
		}
		return (float)floatVal;
	}

	public static float BytesToFixedL(final byte[] bytes, final int pos, final boolean signed, final int intBits, final int fracBits)
	{
		int totalBits = intBits + fracBits;
		final long fixedVal;
		final long mask;

		if (signed)
		{
			totalBits++;
		}

		mask = (1L << totalBits) - 1;
		if (8 >= totalBits)
		{
			fixedVal = bytes[pos] & mask;
		}
		else if (16 >= totalBits)
		{
			fixedVal = Helpers.BytesToUInt16L(bytes, pos) & mask;
		}
		else if (32 >= totalBits)
		{
			fixedVal = Helpers.BytesToUInt32L(bytes, pos) & mask;
		}
		else
		{
			return 0.0f;
		}
		return Helpers.FixedToFloat(fixedVal, signed, intBits, fracBits);
	}

	public static float BytesToFixedB(final byte[] bytes, final int pos, final boolean signed, final int intBits, final int fracBits)
	{
		int totalBits = intBits + fracBits;
		final long fixedVal;
		final long mask;

		if (signed)
		{
			totalBits++;
		}

		mask = (1L << totalBits) - 1;
		if (8 >= totalBits)
		{
			fixedVal = bytes[pos] & mask;
		}
		else if (16 >= totalBits)
		{
			fixedVal = Helpers.BytesToUInt16B(bytes, pos) & mask;
		}
		else if (32 >= totalBits)
		{
			fixedVal = Helpers.BytesToUInt32B(bytes, pos) & mask;
		}
		else
		{
			return 0.0f;
		}
		return Helpers.FixedToFloat(fixedVal, signed, intBits, fracBits);
	}

	public static int Int8ToBytes(final byte value, final byte[] dest, final int pos)
	{
		dest[pos] = value;
		return 1;
	}

	public static int UInt8ToBytes(final byte value, final byte[] dest, final int pos)
	{
		dest[pos] = value;
		return 1;
	}

	/**
	 * Convert a short to a byte array in little endian format
	 * 
	 * @param value
	 *            The short to convert
	 * @return A four byte little endian array
	 */
	public static byte[] Int16ToBytesL(final short value)
	{
		final byte[] bytes = new byte[2];
		Helpers.Int16ToBytesL(value, bytes, 0);
		return bytes;
	}

	public static int Int16ToBytesL(final short value, final byte[] dest, final int pos)
	{
		dest[pos] = (byte) ((value >> 0) & 0xff);
		dest[pos + 1] = (byte) ((value >> 8) & 0xff);
		return 2;
	}

	public static byte[] Int16ToBytesB(final short value)
	{
		final byte[] bytes = new byte[2];
		Helpers.Int16ToBytesB(value, bytes, 0);
		return bytes;
	}

	public static int Int16ToBytesB(final short value, final byte[] dest, final int pos)
	{
		dest[pos] = (byte) ((value >> 8) & 0xff);
		dest[pos + 1] = (byte) ((value >> 0) & 0xff);
		return 2;
	}

	public static byte[] UInt16ToBytesL(final int value)
	{
		final byte[] bytes = new byte[2];
		Helpers.UInt16ToBytesL(value, bytes, 0);
		return bytes;
	}

	public static int UInt16ToBytesL(final int value, final byte[] dest, final int pos)
	{
		dest[pos] = (byte) ((value >> 0) & 0xff);
		dest[pos + 1] = (byte) ((value >> 8) & 0xff);
		return 2;
	}

	public static byte[] UInt16ToBytesB(final int value)
	{
		final byte[] bytes = new byte[2];
		Helpers.UInt16ToBytesB(value, bytes, 0);
		return bytes;
	}

	public static int UInt16ToBytesB(final int value, final byte[] dest, final int pos)
	{
		dest[pos] = (byte) ((value >> 8) & 0xff);
		dest[pos + 1] = (byte) ((value >> 0) & 0xff);
		return 2;
	}

	/**
	 * Convert an integer to a byte array in little endian format
	 * 
	 * @param value
	 *            The integer to convert
	 * @return A four byte little endian array
	 */
	public static byte[] Int32ToBytesL(final int value)
	{
		final byte[] bytes = new byte[4];
		Helpers.Int32ToBytesL(value, bytes, 0);
		return bytes;
	}

	public static int Int32ToBytesL(final int value, final byte[] dest, final int pos)
	{
		dest[pos] = (byte) ((value >> 0) & 0xff);
		dest[pos + 1] = (byte) ((value >> 8) & 0xff);
		dest[pos + 2] = (byte) ((value >> 16) & 0xff);
		dest[pos + 3] = (byte) ((value >> 24) & 0xff);
		return 4;
	}

	public static byte[] Int32ToBytesB(final int value)
	{
		final byte[] bytes = new byte[4];
		Helpers.Int32ToBytesB(value, bytes, 0);
		return bytes;
	}

	public static int Int32ToBytesB(final int value, final byte[] dest, final int pos)
	{
		dest[pos] = (byte) ((value >> 24) & 0xff);
		dest[pos + 1] = (byte) ((value >> 16) & 0xff);
		dest[pos + 2] = (byte) ((value >> 8) & 0xff);
		dest[pos + 3] = (byte) ((value >> 0) & 0xff);
		return 4;
	}

	public static byte[] UInt32ToBytesL(final long value)
	{
		final byte[] bytes = new byte[4];
		Helpers.UInt32ToBytesL(value, bytes, 0);
		return bytes;
	}

	public static int UInt32ToBytesL(final long value, final byte[] dest, final int pos)
	{
		dest[pos] = (byte) ((value >> 0) & 0xff);
		dest[pos + 1] = (byte) ((value >> 8) & 0xff);
		dest[pos + 2] = (byte) ((value >> 16) & 0xff);
		dest[pos + 3] = (byte) ((value >> 24) & 0xff);
		return 4;
	}

	public static byte[] UInt32ToBytesB(final long value)
	{
		final byte[] bytes = new byte[4];
		Helpers.UInt32ToBytesB(value, bytes, 0);
		return bytes;
	}

	public static int UInt32ToBytesB(final long value, final byte[] dest, final int pos)
	{
		dest[pos] = (byte) ((value >> 24) & 0xff);
		dest[pos + 1] = (byte) ((value >> 16) & 0xff);
		dest[pos + 2] = (byte) ((value >> 8) & 0xff);
		dest[pos + 3] = (byte) ((value >> 0) & 0xff);
		return 4;
	}

	/**
	 * Convert a 64-bit integer to a byte array in little endian format
	 * 
	 * @param value
	 *            The value to convert
	 * @return An 8 byte little endian array
	 */
	public static byte[] Int64ToBytesL(final long value)
	{
		final byte[] bytes = new byte[8];
		Helpers.Int64ToBytesL(value, bytes, 0);
		return bytes;
	}

	public static int Int64ToBytesL(final long value, final byte[] dest, final int pos)
	{
		dest[pos] = (byte) ((value >> 0) & 0xff);
		dest[pos + 1] = (byte) ((value >> 8) & 0xff);
		dest[pos + 2] = (byte) ((value >> 16) & 0xff);
		dest[pos + 3] = (byte) ((value >> 24) & 0xff);
		dest[pos + 4] = (byte) ((value >> 32) & 0xff);
		dest[pos + 5] = (byte) ((value >> 40) & 0xff);
		dest[pos + 6] = (byte) ((value >> 48) & 0xff);
		dest[pos + 7] = (byte) ((value >> 56) & 0xff);
		return 8;
	}

	public static byte[] Int64ToBytesB(final long value)
	{
		final byte[] bytes = new byte[8];
		Helpers.Int64ToBytesB(value, bytes, 0);
		return bytes;
	}

	public static int Int64ToBytesB(final long value, final byte[] dest, final int pos)
	{
		dest[pos] = (byte) ((value >> 56) & 0xff);
		dest[pos + 1] = (byte) ((value >> 48) & 0xff);
		dest[pos + 2] = (byte) ((value >> 40) & 0xff);
		dest[pos + 3] = (byte) ((value >> 32) & 0xff);
		dest[pos + 4] = (byte) ((value >> 24) & 0xff);
		dest[pos + 5] = (byte) ((value >> 16) & 0xff);
		dest[pos + 6] = (byte) ((value >> 8) & 0xff);
		dest[pos + 7] = (byte) ((value >> 0) & 0xff);
		return 8;
	}

	/**
	 * Convert a 64-bit unsigned integer to a byte array in little endian format
	 * 
	 * @param value
	 *            The value to convert
	 * @return An 8 byte little endian array
	 */
	public static byte[] UInt64ToBytesL(final long value)
	{
		final byte[] bytes = new byte[8];
		Helpers.UInt64ToBytesL(value, bytes, 0);
		return bytes;
	}

	public static int UInt64ToBytesL(final long value, final byte[] dest, final int pos)
	{
		dest[pos] = (byte) ((value >> 0) & 0xff);
		dest[pos + 1] = (byte) ((value >> 8) & 0xff);
		dest[pos + 2] = (byte) ((value >> 16) & 0xff);
		dest[pos + 3] = (byte) ((value >> 24) & 0xff);
		dest[pos + 4] = (byte) ((value >> 32) & 0xff);
		dest[pos + 5] = (byte) ((value >> 40) & 0xff);
		dest[pos + 6] = (byte) ((value >> 48) & 0xff);
		dest[pos + 7] = (byte) ((value >> 56) & 0xff);
		return 8;
	}

	public static byte[] UInt64ToBytesB(final long value)
	{
		final byte[] bytes = new byte[8];
		Helpers.UInt64ToBytesB(value, bytes, 0);
		return bytes;
	}

	public static int UInt64ToBytesB(final long value, final byte[] dest, final int pos)
	{
		dest[pos] = (byte) ((value >> 56) & 0xff);
		dest[pos + 1] = (byte) ((value >> 48) & 0xff);
		dest[pos + 2] = (byte) ((value >> 40) & 0xff);
		dest[pos + 3] = (byte) ((value >> 32) & 0xff);
		dest[pos + 4] = (byte) ((value >> 24) & 0xff);
		dest[pos + 5] = (byte) ((value >> 16) & 0xff);
		dest[pos + 6] = (byte) ((value >> 8) & 0xff);
		dest[pos + 7] = (byte) ((value >> 0) & 0xff);
		return 8;
	}

	/**
	 * Convert a floating point value to four bytes in little endian ordering
	 * 
	 * @param value
	 *            A floating point value
	 * @return A four byte array containing the value in little endian ordering
	 */
	public static byte[] FloatToBytesL(final float value)
	{
		final byte[] bytes = new byte[4];
		Helpers.Int32ToBytesL(Float.floatToIntBits(value), bytes, 0);
		return bytes;
	}

	public static int FloatToBytesL(final float value, final byte[] dest, final int pos)
	{
		return Helpers.Int32ToBytesL(Float.floatToIntBits(value), dest, pos);
	}

	public static byte[] FloatToBytesB(final float value)
	{
		final byte[] bytes = new byte[4];
		Helpers.Int32ToBytesB(Float.floatToIntBits(value), bytes, 0);
		return bytes;
	}

	public static int FloatToBytesB(final float value, final byte[] dest, final int pos)
	{
		return Helpers.Int32ToBytesB(Float.floatToIntBits(value), dest, pos);
	}

	public static byte[] DoubleToBytesL(final double value)
	{
		final byte[] bytes = new byte[8];
		Helpers.Int64ToBytesL(Double.doubleToLongBits(value), bytes, 0);
		return bytes;
	}

	public static int DoubleToBytesL(final double value, final byte[] dest, final int pos)
	{
		return Helpers.Int64ToBytesL(Double.doubleToLongBits(value), dest, pos);
	}

	public static byte[] DoubleToBytesB(final double value)
	{
		final byte[] bytes = new byte[8];
		Helpers.Int64ToBytesB(Double.doubleToLongBits(value), bytes, 0);
		return bytes;
	}

	public static int DoubleToBytesB(final double value, final byte[] dest, final int pos)
	{
		return Helpers.Int64ToBytesB(Double.doubleToLongBits(value), dest, pos);
	}

	private static float FloatToFixed(final float data, final boolean isSigned, final int intBits, final int fracBits)
	{
		int min;
		final int max = 1 << intBits;

		if (isSigned)
		{
			min = 1 << intBits;
			min *= -1;
		}
		else
		{
			min = 0;
		}

		float fixedVal = Helpers.Clamp(data, min, max);
		if (isSigned)
		{
			fixedVal += max;
		}
		fixedVal *= 1 << fracBits;
		return fixedVal;
	}

	public static int FixedToBytesL(final byte[] dest, final int pos, final float data, final boolean isSigned, final int intBits, final int fracBits)
	{
		int totalBits = intBits + fracBits;
		if (isSigned)
		{
			totalBits++;
		}

		if (8 >= totalBits)
		{
			dest[pos] = (byte) Helpers.FloatToFixed(data, isSigned, intBits, fracBits);
			return 1;
		}
		else if (16 >= totalBits)
		{
			Helpers.UInt16ToBytesL((int) Helpers.FloatToFixed(data, isSigned, intBits, fracBits), dest, pos);
			return 2;
		}
		else if (31 >= totalBits)
		{
			Helpers.UInt32ToBytesL((long) Helpers.FloatToFixed(data, isSigned, intBits, fracBits), dest, pos);
			return 4;
		}
		else
		{
			Helpers.UInt64ToBytesL((long) Helpers.FloatToFixed(data, isSigned, intBits, fracBits), dest, pos);
			return 8;
		}
	}

	public static int FixedToBytesB(final byte[] dest, final int pos, final float data, final boolean isSigned, final int intBits, final int fracBits)
	{
		int totalBits = intBits + fracBits;
		if (isSigned)
		{
			totalBits++;
		}

		if (8 >= totalBits)
		{
			dest[pos] = (byte) Helpers.FloatToFixed(data, isSigned, intBits, fracBits);
			return 1;
		}
		else if (16 >= totalBits)
		{
			Helpers.UInt16ToBytesB((int) Helpers.FloatToFixed(data, isSigned, intBits, fracBits), dest, pos);
			return 2;
		}
		else if (31 >= totalBits)
		{
			Helpers.UInt32ToBytesB((long) Helpers.FloatToFixed(data, isSigned, intBits, fracBits), dest, pos);
			return 4;
		}
		else
		{
			Helpers.UInt64ToBytesB((long) Helpers.FloatToFixed(data, isSigned, intBits, fracBits), dest, pos);
			return 8;
		}
	}

	/**
	 * Packs two 32-bit unsigned integers in to a 64-bit unsigned integer
	 * 
	 * @param a
	 *            The left-hand (or X) value
	 * @param b
	 *            The right-hand (or Y) value
	 * @return A 64-bit integer containing the two 32-bit input values
	 */
	public static long UIntsToLong(final int a, final int b)
	{
		return ((long) a << 32) | b;
	}

	/**
	 * Unpacks two 32-bit unsigned integers from a 64-bit unsigned integer
	 * 
	 * @param a
	 *            The 64-bit input integer
	 * @param b
	 *            The left-hand (or X) output value
	 * @param c
	 *            The right-hand (or Y) output value
	 */
	public static void LongToUInts(final long a, final RefObject<Integer> b, final RefObject<Integer> c)
	{
		b.argvalue = (int) (a >> 32);
		c.argvalue = (int) (a & 0x00000000FFFFFFFF);
	}

	/**
	 * Swaps the high and low nibbles in a byte. Converts aaaabbbb to bbbbaaaa
	 * 
	 * @param value
	 *            Byte to swap the nibbles in
	 * @return Byte value with the nibbles swapped
	 */
	public static byte SwapNibbles(final byte value)
	{
		return (byte) (((value & 0xF0) >> 4) | ((value & 0x0F) << 4));
	}

	/**
	 * Converts an unsigned integer to a hexadecimal string
	 * 
	 * @param i
	 *            An unsigned integer to convert to a string
	 * @return A hexadecimal string 10 characters long
	 *         <example>0x7fffffff</example>
	 */
	public static String UInt32ToHexString(final long i)
	{
		return String.format("%#08x", i);
	}

	public static String LocalIDToString(final int localID)
	{
		return Long.toString(localID & 0xFFFFFFFFL);
	}

	/**
	 * read a variable length UTF8 byte array to a string, consuming  len characters
	 * 
	 * @param is
	 *            The UTF8 encoded byte array to convert
	 * @return The decoded string
	 * @throws UnsupportedEncodingException
	 */
	public static String readString(final InputStream is, final int len) throws IOException
	{
		final byte[] bytes = new byte[len];
		is.read(bytes);
		return Helpers.BytesToString(bytes, 0, len, Helpers.UTF8_ENCODING);
	}
	
	/**
	 * Convert a variable length UTF8 byte array to a string
	 * 
	 * @param bytes
	 *            The UTF8 encoded byte array to convert
	 * @return The decoded string
	 * @throws UnsupportedEncodingException
	 */
	public static String BytesToString(final byte[] bytes) throws UnsupportedEncodingException
	{
		return Helpers.BytesToString(bytes, 0, bytes.length, Helpers.UTF8_ENCODING);
	}

	/**
	 * Convert a variable length UTF8 byte array to a string
	 * 
	 * @param bytes
	 *            The UTF8 encoded byte array to convert
	 * @param offset
	 *            The offset into the byte array from which to start
	 * @param length
	 *            The number of bytes to consume < 0 will search for null
	 *            terminating byte starting from offset
	 * @return The decoded string
	 * @throws UnsupportedEncodingException
	 */
	public static String BytesToString(final byte[] bytes, final int offset, final int length) throws UnsupportedEncodingException
	{
		return Helpers.BytesToString(bytes, offset, length, Helpers.UTF8_ENCODING);
	}
	
	public static String BytesToString(final byte[] bytes, final int offset, int length, final String encoding) throws UnsupportedEncodingException
	{
		if (null != bytes)
		{
			if (0 > length)
			{
				/* Search for the null terminating byte */
				for (length = 0; 0 != bytes[offset + length]; length++)
					;
			}
			else if (0 < length)
			{
				/* Backtrack possible null terminating bytes */
				for (; 0 < length && 0 == bytes[offset + length - 1]; length--)
					;
			}

			if (0 == length)
				return Helpers.EmptyString;

			return new String(bytes, offset, length, encoding);
		}
		return null;
	}

	/**
	 * Converts a byte array to a string containing hexadecimal characters
	 * 
	 * @param bytes
	 *            The byte array to convert to a string
	 * @param fieldName
	 *            The name of the field to prepend to each line of the string
	 * @return A string containing hexadecimal characters on multiple lines.
	 *         Each line is prepended with the field name
	 */
	public static String BytesToHexString(final byte[] bytes, final String fieldName)
	{
		return Helpers.BytesToHexString(bytes, 0, bytes.length, fieldName);
	}

	/**
	 * Converts a byte array to a string containing hexadecimal characters
	 * 
	 * @param bytes
	 *            The byte array to convert to a string
	 * @param offset
	 *            The offset into the byte array from which to start
	 * @param length
	 *            Number of bytes in the array to parse
	 * @param fieldName
	 *            A string to prepend to each line of the hex dump
	 * @return A string containing hexadecimal characters on multiple lines.
	 *         Each line is prepended with the field name
	 */
	public static String BytesToHexString(final byte[] bytes, final int offset, final int length, final String fieldName)
	{
		final StringBuilder output = new StringBuilder();

		for (int i = 0; i < length; i += 16)
		{
			if (0 != i)
			{
				output.append('\n');
			}

			if (0 < fieldName.length())
			{
				output.append(fieldName);
				output.append(": ");
			}

			for (int j = 0; 16 > j; j++)
			{
				if ((i + j) < length)
				{
					if (0 != j)
					{
						output.append(' ');
					}
					output.append(String.format("%2x", bytes[offset + i + j]));
				}
			}
		}
		return output.toString();
	}

	/**
	 * Convert a string to a UTF8 encoded byte array
	 * 
	 * @param str
	 *            The string to convert
	 * @return A null-terminated UTF8 byte array
	 */
	public static byte[] StringToBytes(final String str)
	{
		return Helpers.StringToBytes(str, Helpers.UTF8_ENCODING);
	}
	
	public static byte[] StringToBytes(final String str, final String encoding)
	{
		if (isEmpty(str))
		{
			return EmptyBytes;
		}

		try
		{
			final byte[] string = str.getBytes(encoding);
			final byte[] bytes = new byte[string.length + 1];
			System.arraycopy(string, 0, bytes, 0, string.length);
			return bytes;
		}
		catch (final UnsupportedEncodingException ex)
		{
			return new byte[0];
		}
	}

	/**
	 * Converts a string containing hexadecimal characters to a byte array
	 * 
	 * @param hexString
	 *            String containing hexadecimal characters
	 * @param handleDirty
	 *            If true, gracefully handles null, empty and uneven strings as
	 *            well as stripping unconvertable characters
	 * @return The converted byte array
	 * @throws Exception
	 */
	public static byte[] HexStringToBytes(String hexString, final boolean handleDirty) throws Exception
	{
		if (isEmpty(hexString))
		{
			return EmptyBytes;
		}

		if (handleDirty)
		{
			final StringBuilder stripped = new StringBuilder(hexString.length());
			char c;

			// remove all non A-F, 0-9, characters
			for (int i = 0; i < hexString.length(); i++)
			{
				c = hexString.charAt(i);
				if (Helpers.IsHexDigit(c))
				{
					stripped.append(c);
				}
			}

			hexString = stripped.toString();

			// if odd number of characters, discard last character
			if (0 != hexString.length() % 2)
			{
				hexString = hexString.substring(0, hexString.length() - 1);
			}
		}

		final int byteLength = hexString.length() / 2;
		final byte[] bytes = new byte[byteLength];
		int j = 0;

		for (int i = 0; i < bytes.length; i++)
		{
			bytes[i] = Helpers.HexToByte(hexString.substring(j, 2));
			j += 2;
		}
		return bytes;
	}

	/**
	 * Returns true if c is a hexadecimal digit (A-F, a-f, 0-9)
	 * 
	 * @param c
	 *            Character to test
	 * @return true if hex digit, false if not
	 */
	private static boolean IsHexDigit(final char c)
	{
		return 0 <= Character.digit(c, 16);
	}

	/**
	 * Converts 1 or 2 character string into equivalant byte value
	 * 
	 * @param hex
	 *            1 or 2 character string
	 * @return byte
	 * @throws Exception
	 */
	private static byte HexToByte(final String hex) throws Exception
	{
		if (isEmpty(hex) || 2 < hex.length() || 0 >= hex.length())
		{
			throw new Exception("hex must be 1 or 2 characters in length");
		}
		return Byte.parseByte(hex, 16);
	}

	/**
	 * Convert a float value to an unsigned byte value given a minimum and maximum range
	 * 
	 * @param val Value to convert to a byte
	 * @param lower Minimum value range
	 * @param upper Maximum value range
	 * @return A single byte representing the original float value
	 */
	public static byte FloatToByte(float val, final float lower, final float upper)
	{
		val = Clamp(val, lower, upper);
		// Normalize the value
		val -= lower;
		val /= (upper - lower);

		return (byte) Math.floor(val * 255);
	}

	/**
	 * Convert an unsigned byte to a float value given a minimum and maximum range
	 * 
	 * @param bytes Byte array to get the unsigned byte from
	 * @param pos Position in the byte array the desired byte is at
	 * @param lower Minimum value range
	 * @param upper Maximum value range
	 * @return A float value inclusively between lower and upper
	 */
	public static float ByteToFloat(final byte[] bytes, final int pos, final float lower, final float upper)
	{
		if (bytes.length <= pos)
		{
			return 0;
		}
		return Helpers.ByteToFloat(bytes[pos] & 0xFF, lower, upper);
	}

	/**
	 * Convert a unsigned byte to a float value given a minimum and maximum range
	 * 
	 * @param val Unsigned byte to convert to a float value
	 * @param lower Minimum value range
	 * @param upper Maximum value range
	 * @return A float value inclusively between lower and upper
	 */
	public static float ByteToFloat(final int val, final float lower, final float upper)
	{
		float fval = val / 255.0f;
		final float delta = (upper - lower);
		fval *= delta;
		fval += lower;

		// Test for values very close to zero
		final float error = delta / 255.0f;
		if (Math.abs(fval) < error)
		{
			fval = 0.0f;
		}

		return fval;
	}

	/**
	 * Convert an unsigned short to a float value given a minimum and maximum range
	 * 
	 * @param bytes Byte array to get the unsigned short from
	 * @param lower Minimum value range
	 * @param upper Maximum value range
	 * @return A float value inclusively between lower and upper
	 */
	public static float UInt16ToFloatL(final byte[] bytes, final int pos, final float lower, final float upper)
	{
		final int val = Helpers.BytesToUInt16L(bytes, pos);
		return Helpers.UInt16ToFloat(val, lower, upper);
	}

	/**
	 * Convert a unsigned short to a float value given a minimum and maximum range
	 * 
	 * @param val Unsigned short to convert to a float value
	 * @param lower Minimum value range
	 * @param upper Maximum value range
	 * @return A float value inclusively between lower and upper
	 */
	public static float UInt16ToFloat(final int val, final float lower, final float upper)
	{
		float fval = val / 65535.0f;
		final float delta = upper - lower;
		fval *= delta;
		fval += lower;

		// Make sure zeroes come through as zero
		final float maxError = delta / 65535.0f;
		if (Math.abs(fval) < maxError)
		{
			fval = 0.0f;
		}
		return fval;
	}

	public static int FloatToUInt16(float value, final float lower, final float upper)
	{
		final float delta = upper - lower;
		value -= lower;
		value /= delta;
		value *= 65535.0f;
		return (int) value;
	}

	public static byte[] TEOffsetShort(float offset)
	{
		offset = Clamp(offset, -1.0f, 1.0f);
		offset *= 32767.0f;
		return Helpers.Int16ToBytesL((short) Helpers.roundFromZero(offset));
	}

	public static float TEOffsetFloat(final byte[] bytes, final int pos)
	{
		final float offset = Helpers.BytesToInt16L(bytes, pos);
		return offset / 32767.0f;
	}

	public static byte[] TERotationShort(final float rotation)
	{
		final double TWO_PI = 6.283185307179586476925286766559d;
		final double remainder = Math.IEEEremainder(rotation, TWO_PI);
		return Helpers.Int16ToBytesL((short) Helpers.roundFromZero((remainder / TWO_PI) * 32767.0f));
	}

	public static float TERotationFloat(final byte[] bytes, final int pos)
	{
		final float TWO_PI = 6.283185307179586476925286766559f;
		final int tmp = Helpers.BytesToInt16L(bytes, pos);
		return tmp * TWO_PI / 32767.0f ;
	}

	public static byte TEGlowByte(final float glow)
	{
		return (byte) (glow * 255.0f);
	}

	public static float TEGlowFloat(final byte[] bytes, final int pos)
	{
		return bytes[pos] / 255.0f;
	}

	public static void skipElement(final XmlPullParser parser) throws XmlPullParserException, IOException
	{
		int depth = 1;
		do
		{
			final int tag = parser.next();
			if (XmlPullParser.START_TAG == tag)
			{
				depth++;
			}
			else if (XmlPullParser.END_TAG == tag)
			{
				depth--;
			}
		} while (0 < depth);
	}

	public static boolean TryParseBoolean(final String s)
	{
		if (null != s && !s.isEmpty())
		{
			try
			{
				return Boolean.parseBoolean(s);
			}
			catch (final Throwable t)	{ }
		}
		return false;
	}

	public static int TryParseInt(final String s)
	{
		if (null != s && !s.isEmpty())
		{
			try
			{
				return Integer.parseInt(s);
			}
			catch (final Throwable t) { }
		}
		return 0;
	}

	public static float TryParseFloat(String s)
	{
		if (null != s && !s.isEmpty())
		{
			try
			{
				return Float.parseFloat(s);
			}
			catch (final Throwable t)
			{
				s = s.toLowerCase();
				if	("nan".equals(s))
					return Float.NaN;
				else if	(s.contains("inf"))
					if ('-' == s.charAt(0))
						return Float.NEGATIVE_INFINITY;
					return Float.POSITIVE_INFINITY;
			}
		}
		return 0.0f;
	}

	public static double TryParseDouble(String s)
	{
		if (null != s && !s.isEmpty())
		{
			try
			{
				return Double.parseDouble(s);
			}
			catch (final Throwable t)
			{
				s = s.toLowerCase();
				if	("nan".equals(s))
					return Double.NaN;
				else if	(s.contains("inf"))
					if ('-' == s.charAt(0))
						return Double.NEGATIVE_INFINITY;
					return Double.POSITIVE_INFINITY;
			}
		}
		return 0.0d;
	}

	/**
	 * Tries to parse an unsigned 32-bit integer from a hexadecimal string
	 * 
	 * @param s
	 *            String to parse
	 *
	 * @return True if the parse was successful, otherwise false
	 */
	public static long TryParseHex(final String s)
	{
		if (null != s && !s.isEmpty())
		{
			try
			{
				return Long.parseLong(s, 16);
			}
			catch (final Throwable t) { }
		}
		return 0L;
	}

	public static long TryParseLong(final String s)
	{
		if (null != s && !s.isEmpty())
		{
			try
			{
				return Long.parseLong(s, 10);
			}
			catch (final Throwable t) { }
		}
		return 0L;
	}

	/**
	 * Returns text specified in EnumInfo attribute of the enumerator To add the
	 * text use [EnumInfo(Text = "Some nice text here")] before declaration of
	 * enum values
	 * 
	 * @param value
	 *            Enum value
	 * @return Text representation of the enum
	 */
	public static String EnumToText(final Enum<?> value)
	{
		// Get the type
		final Class<?> type = value.getClass();
		if (!type.isEnum())
		{
			return Helpers.EmptyString;
		}
		return value.toString();
	}

	/**
	 * <p>
	 * Find the first index of any of a set of potential substrings.
	 * </p>
	 * 
	 * @param str
	 *            the String to check, may be null
	 * @param searchStrs
	 *            the Strings to search for, may be null
	 * @return the first index of the searchStrs in str, -1 if no match
	 */
	public static int indexOfAny(final String str, final String[] searchStrs)
	{
		if ((null == str) || (null == searchStrs))
		{
			return -1;
		}
		final int sz = searchStrs.length;

		// String's can't have a MAX_VALUEth index.
		int ret = Integer.MAX_VALUE;

		int tmp = 0;
		for (int i = 0; i < sz; i++)
		{
			final String search = searchStrs[i];
			if (null == search)
			{
				continue;
			}
			tmp = str.indexOf(search);
			if (-1 == tmp)
			{
				continue;
			}

			if (tmp < ret)
			{
				ret = tmp;
			}
		}

		return (Integer.MAX_VALUE == ret) ? -1 : ret;
	}

	/**
	 * <p>
	 * Find the first index of any of a set of potential chars.
	 * </p>
	 * 
	 * @param str
	 *            the String to check, may be null
	 * @param searchChars
	 *            the Strings to search for, may be null
	 * @return the first index of any of the searchStrs in str, -1 if no match
	 */
	public static int indexOfAny(final String str, final char[] searchChars)
	{
		if ((null == str) || (null == searchChars))
		{
			return -1;
		}
		final int sz = searchChars.length;

		// String's can't have a MAX_VALUEth index.
		int ret = Integer.MAX_VALUE;

		int tmp = 0;
		for (int i = 0; i < sz; i++)
		{
			tmp = str.indexOf(searchChars[i]);
			if (-1 == tmp)
			{
				continue;
			}

			if (tmp < ret)
			{
				ret = tmp;
			}
		}

		return (Integer.MAX_VALUE == ret) ? -1 : ret;
	}

	/**
	 * <p>
	 * Checks if a character array is empty or {@code null}.
	 * </p>
	 * 
	 * @param array
	 *            the array to test
	 * @return {@code true} if the array is empty or <code>null</code>
	 */
	public static boolean isEmpty(final char[] array)
	{
		return ((null == array) || (0 == array.length));
	}

	/**
	 * Checks if a String is empty ("") or null.
	 * 
	 * @param str
	 *            the String to check, may be null
	 *
	 * @return {@code true} if the String is empty or null
	 */
	public static boolean isEmpty(final String str)
	{
		return ((null == str) || str.isEmpty());
	}

	/**
	 * Generates a String with n times the input string str repeated
	 * 
	 * @param str
	 *            the String to repeat n times
	 * @param n
	 *            the number of repetitions of string str
	 *            
	 * @return {@code true} if the String is empty or null
	 */
	public static String repeat(final String str, final int n)
	{
		if (0 > n || null == str)
		    throw new IllegalArgumentException(
		        "the given repetition count is smaller than zero!");
		else if (0 == n)
		    return "";
		else if (1 == n)
		    return str;
		else if (0 == n % 2)
		{
		    final String s = Helpers.repeat(str, n / 2);
		    return s + s;
		}
		else
		{
		    return str + Helpers.repeat(str, n - 1);
		}
	}
	/**
	 * Get current OS
	 * 
	 * @return either "win", "mac", "lnx", "bsd", "sun", or "unx"
	 */
	public static String getPlatform()
	{
		final String platform = System.getProperty("os.name").toLowerCase();
		
		if (platform.contains("windows"))
			return "win";
		else if (platform.contains("mac"))
			return "mac";
		else if (platform.contains("sun") || platform.contains("solaris"))
			return "sun";
		else if (platform.contains("linux"))
		    return "lnx";
		else if (platform.contains("freebsd"))
			return "bsd";
		else if (platform.contains("unix") || platform.contains("aix") || platform.contains("hp-ux"))
			return "unx";
		return "unk";
	}

	public static String getPlatformVersion()
	{
		return System.getProperty("os.version");
	}
	
	public static OSD ZDecompressOSD(final InputStream in) throws IOException, ParseException
	{
		final InflaterInputStream inflate = new InflaterInputStream(in);
		try
		{
			return OSDParser.deserialize(inflate);
		}
		finally
		{
			inflate.close();
		}
	}

	public static byte[] ZCompressOSD(final OSD osd) throws IOException
	{
		final ByteArrayOutputStream out = new ByteArrayOutputStream();
		final DeflaterOutputStream deflate = new DeflaterOutputStream(out);
		try
		{
			OSDParser.serialize(deflate, osd, OSD.OSDFormat.Binary);
			return out.toByteArray();
		}
		finally
		{
			deflate.close();
		}
	}

	/**
	 * Get clients default Mac Address
	 * 
	 * @return A string containing the first found Mac Address
	 * @throws SocketException
	 */
	public static String getMAC()
	{
		try
		{
			final StringBuilder sb = new StringBuilder();

			/*
			 * Extract each array of mac address and convert it to hexa with the
			 * following format 08:00:27:DC:4A:9E.
			 */
			for (final Enumeration<NetworkInterface> nis = NetworkInterface.getNetworkInterfaces(); nis.hasMoreElements();)
			{
				final NetworkInterface ni = nis.nextElement();
				if (null != ni)
				{
					final byte[] mac = ni.getHardwareAddress();
					if (null != mac && 6 <= mac.length)
					{
						for (int i = 0; i < mac.length; i++)
						{
							sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? ":" : ""));
						}
						break;
					}
					if (nis.hasMoreElements())
					{
						sb.append(" ");
					}
				}
			}
			return sb.toString().trim();
		}
		catch (final SocketException ex)
		{
			return Helpers.EmptyString;
		}
	}

	/**
	 * Get the file name suffix of the filename if any.
	 * 
	 * @param fileName The filename to get the suffix from
	 * @return The suffix if it exists or null otherwise
	 */
	public static String getFileExtension(final String fileName)
	{
		return Helpers.getFileExtension(fileName, '.');
	}

	public static String getFileExtension(final String fileName, final char seperator)
	{
	    if (null == fileName)
	    {
	        throw new IllegalArgumentException("file name == null");
	    }
	    final int pos = fileName.lastIndexOf(seperator) + 1;
	    if (0 <= pos && pos < fileName.length())
	    {
	        return fileName.substring(pos);
	    }
	    return null;
	}

	public static String skipElementDebug(final XmlPullParser parser) throws XmlPullParserException, IOException
	{
		final StringBuilder sb = new StringBuilder();
		int depth = 0;
		int tag = parser.getEventType();
		do
		{
			switch (tag)
			{
			    case XmlPullParser.START_TAG:
			    	if (!parser.isEmptyElementTag())
			    	{
			    		sb.append("<" + parser.getName() + ">");
				    	depth++;
			    	}
			    	else
			    	{
			    		sb.append("<" + parser.getName() + " />");			    		
			    	}
				    break;
			    case XmlPullParser.TEXT:
			    	sb.append(parser.nextText());
			    	break;
			    case XmlPullParser.END_TAG:
				    sb.append("</" + parser.getName() + ">\n");
				    depth--;
				    break;
			}
			tag = parser.next();
		} while (0 < depth);
		return sb.toString();
	}

	/**
	 * Get the file name without suffix of the filename if any.
	 * 
	 * @param fileName The filename to strip the suffix from
	 * @return The filename without suffix if a suffix exists or the entire name otherwise
	 */
	public static String getBaseFileName(final String fileName) throws IllegalArgumentException
	{
		return Helpers.getBaseFileName(fileName, '.');
	}
	
	public static String getBaseFileName(final String fileName, final char separater) throws IllegalArgumentException
	{
	    if (null == fileName)
	    {
	        throw new IllegalArgumentException("file name == null");
	    }
	    final int pos = fileName.lastIndexOf(separater);
	    if (0 < pos && pos < fileName.length() - 1)
	    {
	        return fileName.substring(0, pos);
	    }
	    return fileName;
	}

	/**
	 * Get the base path of the source for the code containing the specified class.
	 * This can be both a path to a file directory (usually the bin directroy) when
	 * running in the IDE, or the actual JAR file the code is located in.
	 * Note: Security manager limitations may apply!
	 *
	 * @param clazz the class to get the code source for
	 * @return the URL for the code source
	 */
	public static URL getBaseFileURL(final Class<?> clazz) throws SecurityException
	{
		final Class<?> temp = null != clazz ?  clazz : Helpers.class;
		final CodeSource codeSource = temp.getProtectionDomain().getCodeSource();
		return codeSource.getLocation();
	}
	
	/**
	 * Get the base directory of the source for the code containing the specified class.
	 * This should be always a file directory for disk based applications, returning 
	 * the parent directory of the code source (e.g. where the jar file resides).
	 * Note: Security manager limitations may apply!
	 *
	 * @param clazz the class to get the code source for
	 * @return the File for the parent of the code source
	 * @throws URISyntaxException
	 */
	public static File getBaseDirectory(final Class<?> clazz) throws URISyntaxException
	{
		final URL baseUrl = Helpers.getBaseFileURL(clazz);
		return new File(baseUrl.toURI()).getParentFile();
	}

	/**
	 * List directory contents for a resource folder. Not recursive. This is
	 * basically a brute-force implementation. Works for regular files and also JARs.
	 * 
	 * @author Greg Briggs
	 * @param clazz Any java class that lives in the same place as the resources you want.
	 * @param regex A regex which the resource names must match or null to return all file names 
	 * @param excludeDirs If true only file entries will be returned	
	 * @param path The relative directory path to enumerate. Should end with "/", but not start with one.
	 * @return Just the name of each member item, not the full paths.
	 * @throws URISyntaxException
	 * @throws IOException
	 */
	public static String[] getResourceListing(final Class<?> clazz, String regex, boolean excludeDirs, final String path) throws URISyntaxException, IOException
	{
		// File filter checking the file names to be part of the hostname
		final FileFilter filter = new FileFilter()
		{
			@Override
			public boolean accept(final File file)
			{
				if (excludeDirs && file.isDirectory())
					return false;
				return null == regex || file.getName().matches(regex);
			}
		};

		URL dirURL = Helpers.getBaseFileURL(clazz);
		if (null != dirURL && "file".equals(dirURL.getProtocol()))
		{
			/* A file path: easy enough */
			final File filepath = new File(dirURL.toURI().resolve(path));
			if (excludeDirs && null == regex)
				return filepath.list();
			
			final File[] files = filepath.listFiles(filter);
			final String[] names = new String[files.length];
			for (int i = 0; i < files.length; i++)
			{
				names[i] = files[i].getName();
			}
			return names;
		}

		if (null == dirURL)
		{
			/*
			 * In case of a jar file, we can't actually find a directory. Have
			 * to assume the same jar as clazz.
			 */
			final String me = clazz.getName().replace(".", "/") + ".class";
			dirURL = clazz.getClassLoader().getResource(me);
		}

		if ("jar".equals(dirURL.getProtocol()))
		{
			/* A JAR path, strip out only the JAR file */
			final String jarPath = dirURL.getPath().substring(5, dirURL.getPath().indexOf('!'));
			final JarFile jar = new JarFile(URLDecoder.decode(jarPath, StandardCharsets.UTF_8));
			final Enumeration<JarEntry> entries = jar.entries(); // gives ALL entries in jar
			final Set<String> result = new HashSet<String>();    // avoid duplicates in case it is a subdirectory
			while (entries.hasMoreElements())
			{
				final String name = entries.nextElement().getName();
				if (name.startsWith(path))
				{ // filter according to the path
					String entry = name.substring(path.length());
					final int checkSubdir = entry.indexOf('/');
					if (0 <= checkSubdir)
					{
						if (excludeDirs)
							continue;
						
						// if it is a subdirectory, we just return the directory name
						entry = entry.substring(0, checkSubdir);
					}
					if (null == regex || entry.matches(regex))
						result.add(entry);
				}
			}
			jar.close();
			return result.toArray(new String[result.size()]);
		}
		throw new UnsupportedOperationException("Cannot list files for URL " + dirURL);
	}

	/**
	 * Retrieves the default keystore
	 * 
	 * @return The current KeyStore
	 * @throws IOException
	 * @throws KeyStoreException
	 * @throws CertificateException
	 * @throws NoSuchAlgorithmException
	 */
	public static KeyStore getExtendedKeyStore() throws KeyStoreException, IOException, NoSuchAlgorithmException,
			CertificateException
	{
		KeyStore ks = null;

		File file = new File("jssecacerts");
		if (!file.isFile())
		{
			final char SEP = File.separatorChar;
			final File dir = new File(System.getProperty("java.home") + SEP + "lib" + SEP + "security");
			file = new File(dir, "jssecacerts");
			if (!file.isFile())
			{
				file = new File(dir, "cacerts");
			}
		}

		ks = KeyStore.getInstance(KeyStore.getDefaultType());
		final InputStream in = new FileInputStream(file);
		try
		{
			ks.load(in, null);
		}
		catch (final IOException ex)
		{
			throw ex;
		}
		catch (final NoSuchAlgorithmException ex)
		{
			throw ex;
		}
		catch (final CertificateException ex)
		{
			throw ex;
		}
		finally
		{
			in.close();
		}
		return ks;
	}

	public static X509Certificate getCertificate(String hostname) throws CertificateException, IOException,
			URISyntaxException
	{
		final CertificateFactory cf = CertificateFactory.getInstance("X.509");
		X509Certificate cert = null;
		final String[] names = Helpers.getResourceListing(Helpers.class, ".+\\.cert", true, "res/");

		for (final String name : names)
		{
			// - 5 is to remove the .cert extension
			if (hostname.contains(name.substring(0, name.length() - 5)))
			{
				final InputStream is = Helpers.class.getClassLoader().getResourceAsStream("res/" + name);
				final BufferedInputStream bis = new BufferedInputStream(is);
				try
				{
					cert = (X509Certificate) cf.generateCertificate(bis);
				}
				catch (final CertificateException ex)
				{
					throw ex;
				}
				finally
				{
					bis.close();
					is.close();
				}
				break;
			}
		}
		return cert;
	}
}
