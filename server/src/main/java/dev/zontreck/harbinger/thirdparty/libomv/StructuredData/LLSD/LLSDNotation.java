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
package dev.zontreck.harbinger.thirdparty.libomv.StructuredData.LLSD;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URI;
import java.text.ParseException;
import java.util.Base64;
import java.util.Map;

import dev.zontreck.harbinger.thirdparty.libomv.StructuredData.OSD;
import dev.zontreck.harbinger.thirdparty.libomv.StructuredData.OSDArray;
import dev.zontreck.harbinger.thirdparty.libomv.StructuredData.OSDMap;
import dev.zontreck.harbinger.thirdparty.libomv.StructuredData.OSDParser;
import dev.zontreck.harbinger.thirdparty.libomv.StructuredData.OSDString;
import dev.zontreck.harbinger.thirdparty.libomv.types.UUID;
import dev.zontreck.harbinger.thirdparty.libomv.utils.Helpers;
import dev.zontreck.harbinger.thirdparty.libomv.utils.PushbackReader;

public final class LLSDNotation extends OSDParser
{
	/*
	 * Possible header formats
	 *    <?llsd/notation?>
	 *    <? llsd/notation ?>
	 */
	private static final String llsdNotationHeader = "llsd/notation";
	private static final String llsdNotationHead = "<?llsd/notation?>";

	private static final String baseIndent = "  ";

	private static final char[] newLine = { '\n' };

	private static final char undefNotationValue = '!';

	private static final char trueNotationValueOne = '1';
	private static final char trueNotationValueTwo = 't';
	private static final char[] trueNotationValueTwoFull = { 't', 'r', 'u', 'e' };
	private static final char trueNotationValueThree = 'T';
	private static final char[] trueNotationValueThreeFull = { 'T', 'R', 'U', 'E' };

	private static final char falseNotationValueOne = '0';
	private static final char falseNotationValueTwo = 'f';
	private static final char[] falseNotationValueTwoFull = { 'f', 'a', 'l', 's', 'e' };
	private static final char falseNotationValueThree = 'F';
	private static final char[] falseNotationValueThreeFull = { 'F', 'A', 'L', 'S', 'E' };

	private static final char integerNotationMarker = 'i';
	private static final char realNotationMarker = 'r';
	private static final char uuidNotationMarker = 'u';
	private static final char binaryNotationMarker = 'b';
	private static final char stringNotationMarker = 's';
	private static final char uriNotationMarker = 'l';
	private static final char dateNotationMarker = 'd';

	private static final char arrayBeginNotationMarker = '[';
	private static final char arrayEndNotationMarker = ']';

	private static final char mapBeginNotationMarker = '{';
	private static final char mapEndNotationMarker = '}';
	private static final char kommaNotationDelimiter = ',';
	private static final char keyNotationDelimiter = ':';

	private static final char sizeBeginNotationMarker = '(';
	private static final char sizeEndNotationMarker = ')';
	private static final char doubleQuotesNotationMarker = '"';
	private static final char singleQuotesNotationMarker = '\'';

	public static boolean isFormat(final String string)
	{
		final int character = OSDParser.skipWhiteSpace(string);
		if ('<' == character)
		{
			return OSDParser.isHeader(string, LLSDNotation.llsdNotationHeader, '>');
		}
		return false;
	}
	
	public static boolean isFormat(final byte[] data, String encoding) throws UnsupportedEncodingException
	{
		final int character = OSDParser.skipWhiteSpace(data);
		if ('<' == character)
		{
			if (null == encoding)
				encoding = OSD.OSDFormat.contentEncodingDefault(OSD.OSDFormat.Notation);
			return isHeader(data, llsdNotationHeader.getBytes(encoding), '>');
		}
		return false;
	}

	/**
	 * Parse an LLSD Notation reader and convert it into an hierarchical OSD object
	 *
	 * @param stream The LLSD Notation stream to parse
	 * @param encoding The encoding to use for the stream, can be null which uses UTF8
	 * @return hierarchical OSD object
	 * @throws IOException
	 * @throws ParseException
	 */
	protected OSD unflatten(InputStream stream, String encoding) throws ParseException, IOException
	{
		if (encoding == null)
			encoding = OSD.OSDFormat.contentEncodingDefault(OSD.OSDFormat.Notation);
		return unflatten(new InputStreamReader(stream, encoding), encoding);
	}

	/**
	 * Parse an LLSD Notation reader and convert it into an hierarchical OSD object
	 *
	 * @param reader The LLSD Notation reader to parse
	 * @param encoding The encoding to use for reader (not used)
	 * @return hierarchical OSD object
	 * @throws IOException
	 * @throws ParseException
	 */
	protected OSD unflatten(Reader reader, String encoding) throws ParseException, IOException
	{
		PushbackReader push = reader instanceof PushbackReader ? (PushbackReader)reader : new PushbackReader(reader);
		int marker = skipWhiteSpace(push);
		if (marker < 0)
		{
			return new OSD();
		}
		else if (marker == '<')
		{
			int offset = push.getBytePosition();
			if (!isHeader(push, llsdNotationHeader, '>'))
				throw new ParseException("Failed to decode binary LLSD", offset);
		}
		else
		{
			push.unread(marker);
		}
		return parseElement(push);
	}

	/**
	 * Serialize an hierarchical OSD object into an LLSD Notation writer
	 *
	 * @param stream The writer to format the serialized data into
	 * @param data The hierarchical OSD object to serialize
	 * @param prependHeader Indicates if the format header should be prepended
	 * @throws IOException
	 */
	protected void flatten(OutputStream stream, OSD data, boolean prependHeader, String encoding) throws IOException
	{
		if (encoding == null)
			encoding = OSD.OSDFormat.contentEncodingDefault(OSD.OSDFormat.Notation);
		final Writer writer = new OutputStreamWriter(stream, encoding);
		this.flatten(writer, data, prependHeader, encoding);
		writer.flush();
	}
	
	/**
	 * Serialize an hierarchical OSD object into an LLSD Notation writer
	 * 
	 * @param writer The writer to format the serialized data into
	 * @param data The hierarchical OSD object to serialize
	 * @param prependHeader Indicates if the format header should be prepended
	 * @throws IOException
	 */
	protected void flatten(final Writer writer, final OSD data, final boolean prependHeader, final String encoding) throws IOException
	{
		if (prependHeader)
		{
			writer.write(LLSDNotation.llsdNotationHead);
			writer.write('\n');
		}
		LLSDNotation.serializeElement(writer, data);
	}
	
	public static String serializeToStringFormatted(final OSD data) throws IOException
	{
		final StringWriter writer = new StringWriter();
		LLSDNotation.serializeElementFormatted(writer, "", data);
		return writer.toString();
	}

	public static void serializeFormatted(final Writer writer, final OSD data) throws IOException
	{
		LLSDNotation.serializeElementFormatted(writer, "", data);
	}

	/**
	 * Read the next LLSD data element in and return the OSD structure for it
	 * 
	 * @param reader
	 *            a pushback reader to read in data from
	 * @return the OSD data corresponding to the LLSD data element
	 * @throws IOException
	 */
	private static OSD parseElement(final PushbackReader reader) throws ParseException, IOException
	{
		final int character = OSDParser.skipWhiteSpace(reader);
		if (0 >= character)
		{
			return new OSD(); // server returned an empty file, so we're going
								// to pass along a null LLSD object
		}

		final int matching;
		switch ((char) character)
		{
			case LLSDNotation.undefNotationValue:
				return new OSD();
			case LLSDNotation.trueNotationValueOne:
				return OSD.FromBoolean(true);
			case LLSDNotation.trueNotationValueTwo:
				matching = OSDParser.bufferCharactersEqual(reader, LLSDNotation.trueNotationValueTwoFull, 1);
				if (1 < matching && matching < LLSDNotation.trueNotationValueTwoFull.length)
				{
					throw new ParseException("Notation LLSD parsing: True value parsing error:",
							reader.getBytePosition());
				}
				return OSD.FromBoolean(true);
			case LLSDNotation.trueNotationValueThree:
				matching = OSDParser.bufferCharactersEqual(reader, LLSDNotation.trueNotationValueThreeFull, 1);
				if (1 < matching && matching < LLSDNotation.trueNotationValueThreeFull.length)
				{
					throw new ParseException("Notation LLSD parsing: True value parsing error:",
							reader.getBytePosition());
				}
				return OSD.FromBoolean(true);
			case LLSDNotation.falseNotationValueOne:
				return OSD.FromBoolean(false);
			case LLSDNotation.falseNotationValueTwo:
				matching = OSDParser.bufferCharactersEqual(reader, LLSDNotation.falseNotationValueTwoFull, 1);
				if (1 < matching && matching < LLSDNotation.falseNotationValueTwoFull.length)
				{
					throw new ParseException("Notation LLSD parsing: True value parsing error:",
							reader.getBytePosition());
				}
				return OSD.FromBoolean(false);
			case LLSDNotation.falseNotationValueThree:
				matching = OSDParser.bufferCharactersEqual(reader, LLSDNotation.falseNotationValueThreeFull, 1);
				if (1 < matching && matching < LLSDNotation.falseNotationValueThreeFull.length)
				{
					throw new ParseException("Notation LLSD parsing: True value parsing error:",
							reader.getBytePosition());
				}
				return OSD.FromBoolean(false);
			case LLSDNotation.integerNotationMarker:
				return LLSDNotation.parseInteger(reader);
			case LLSDNotation.realNotationMarker:
				return LLSDNotation.parseReal(reader);
			case LLSDNotation.uuidNotationMarker:
				final char[] uuidBuf = new char[36];
				if (36 > reader.read(uuidBuf, 0, 36))
				{
					throw new ParseException("Notation LLSD parsing: Unexpected end of stream in UUID.",
							reader.getBytePosition());
				}
				return OSD.FromUUID(new UUID(new String(uuidBuf)));
			case LLSDNotation.binaryNotationMarker:
				byte[] bytes = Helpers.EmptyBytes;
				final int bChar = reader.read();
				if (0 > bChar)
				{
					throw new ParseException("Notation LLSD parsing: Unexpected end of stream in binary.",
							reader.getBytePosition());
				}
				else if (sizeBeginNotationMarker == bChar)
				{
					throw new ParseException("Notation LLSD parsing: Raw binary encoding not supported.",
							reader.getBytePosition());
				}
				else if (Character.isDigit((char) bChar))
				{
					final char[] charsBaseEncoding = new char[2];
					charsBaseEncoding[0] = (char) bChar;
					charsBaseEncoding[1] = (char) reader.read();
					if (0 > charsBaseEncoding[1])
					{
						throw new ParseException("Notation LLSD parsing: Unexpected end of stream in binary.",
								reader.getBytePosition());
					}
					final int baseEncoding = Integer.parseInt(new String(charsBaseEncoding));
					if (64 == baseEncoding)
					{
						if (0 > reader.read())
						{
							throw new ParseException("Notation LLSD parsing: Unexpected end of stream in binary.",
									reader.getBytePosition());
						}
						final String bytes64 = OSDParser.getStringDelimitedBy(reader, LLSDNotation.doubleQuotesNotationMarker);
						bytes = Base64.getDecoder().decode(bytes64);
					}
					else
					{
						throw new ParseException("Notation LLSD parsing: Encoding base" + baseEncoding
								+ " + not supported.", reader.getBytePosition());
					}
				}
				return OSD.FromBinary(bytes);
			case LLSDNotation.stringNotationMarker:
			case LLSDNotation.singleQuotesNotationMarker:
			case LLSDNotation.doubleQuotesNotationMarker:
				final String string = LLSDNotation.getString(reader, character);
				return OSD.FromString(string);
			case LLSDNotation.uriNotationMarker:
				if (0 > reader.read())
				{
					throw new ParseException("Notation LLSD parsing: Unexpected end of stream in string.",
							reader.getBytePosition());
				}
				final URI uri;
				try
				{
					uri = new URI(OSDParser.getStringDelimitedBy(reader, LLSDNotation.doubleQuotesNotationMarker));
				}
				catch (final Throwable t)
				{
					throw new ParseException("Notation LLSD parsing: Invalid Uri format detected.",
							reader.getBytePosition());
				}
				return OSD.FromUri(uri);
			case LLSDNotation.dateNotationMarker:
				if (0 > reader.read())
				{
					throw new ParseException("Notation LLSD parsing: Unexpected end of stream in date.",
							reader.getBytePosition());
				}
				final String date = OSDParser.getStringDelimitedBy(reader, LLSDNotation.doubleQuotesNotationMarker);
				return OSD.FromDate(new OSDString(date).AsDate());
			case LLSDNotation.arrayBeginNotationMarker:
				return LLSDNotation.parseArray(reader);
			case LLSDNotation.mapBeginNotationMarker:
				return LLSDNotation.parseMap(reader);
			default:
		}
		throw new ParseException("Notation LLSD parsing: Unknown type marker '" + (char) character + "'.",
				reader.getBytePosition());
	}

	private static OSD parseInteger(final PushbackReader reader) throws IOException
	{
		int character;
		final StringBuilder s = new StringBuilder();
		if ((0 < (character = reader.read())) && ('-' == (char) character || '+' == (char) character))
		{
			s.append((char) character);
			character = reader.read();
		}
		while (0 < character && Character.isDigit((char) character))
		{
			s.append((char) character);
			character = reader.read();
		}
		if (0 <= character)
		{
			reader.unread(character);
		}
		return OSD.FromInteger(Integer.parseInt(s.toString()));
	}

	private static OSD parseReal(final PushbackReader reader) throws IOException
	{
		int character;
		final StringBuilder s = new StringBuilder();
		if ((0 < (character = reader.read())) && ('-' == (char) character || '+' == (char) character))
		{
			s.append((char) character);
			character = reader.read();
		}
		while ((0 < character)
				&& (Character.isDigit((char) character) || '.' == (char) character || 'e' == (char) character
						|| 'E' == (char) character || '+' == (char) character || '-' == (char) character))
		{
			s.append((char) character);
			character = reader.read();
		}
		if (0 <= character)
		{
			reader.unread(character);
		}
		return OSD.FromReal(Double.parseDouble(s.toString()));
	}

	private static OSD parseArray(final PushbackReader reader) throws IOException, ParseException
	{
		int character;
		final OSDArray osdArray = new OSDArray();
		while ((0 < (character = skipWhiteSpace(reader))) && (arrayEndNotationMarker != (char) character))
		{
			reader.unread(character);
			osdArray.add(LLSDNotation.parseElement(reader));

			character = OSDParser.skipWhiteSpace(reader);
			if (0 > character)
			{
				throw new ParseException("Notation LLSD parsing: Unexpected end of array discovered.",
						reader.getBytePosition());
			}
			else if (arrayEndNotationMarker == (char) character)
			{
				break;
			}
		}
		if (0 > character)
		{
			throw new ParseException("Notation LLSD parsing: Unexpected end of array discovered.",
					reader.getBytePosition());
		}
		return osdArray;
	}

	private static OSD parseMap(final PushbackReader reader) throws ParseException, IOException
	{
		int character;
		final OSDMap osdMap = new OSDMap();
		while ((0 < (character = skipWhiteSpace(reader))) && (mapEndNotationMarker != (char) character))
		{
			if (kommaNotationDelimiter == (char) character)
			{
				character = OSDParser.skipWhiteSpace(reader);
			}
			final String key = LLSDNotation.getString(reader, character);
			character = OSDParser.skipWhiteSpace(reader);
			if (keyNotationDelimiter != (char) character)
			{
				throw new ParseException("Notation LLSD parsing: Invalid key delimiter in map.", reader.getBytePosition());
			}
			osdMap.put(key, LLSDNotation.parseElement(reader));
		}
		if (0 > character)
		{
			throw new ParseException("Notation LLSD parsing: Unexpected end of map discovered.",
					reader.getBytePosition());
		}
		return osdMap;
	}

	private static void serializeElement(final Writer writer, final OSD osd) throws IOException
	{
		switch (osd.getType())
		{
			case Unknown:
				writer.write(LLSDNotation.undefNotationValue);
				break;
			case Boolean:
				if (osd.AsBoolean())
				{
					writer.write(LLSDNotation.trueNotationValueTwo);
				}
				else
				{
					writer.write(LLSDNotation.falseNotationValueTwo);
				}
				break;
			case Integer:
				writer.write(LLSDNotation.integerNotationMarker);
				writer.write(osd.AsString());
				break;
			case Real:
				writer.write(LLSDNotation.realNotationMarker);
				writer.write(osd.AsString());
				break;
			case UUID:
				writer.write(LLSDNotation.uuidNotationMarker);
				writer.write(osd.AsString());
				break;
			case String:
				writer.write(LLSDNotation.singleQuotesNotationMarker);
				writer.write(LLSDNotation.escapeCharacter(osd.AsString(), LLSDNotation.singleQuotesNotationMarker));
				writer.write(LLSDNotation.singleQuotesNotationMarker);
				break;
			case Binary:
				writer.write(LLSDNotation.binaryNotationMarker);
				writer.write("64");
				writer.write(LLSDNotation.doubleQuotesNotationMarker);
				writer.write(Base64.getEncoder().encodeToString(osd.AsBinary()));
				writer.write(LLSDNotation.doubleQuotesNotationMarker);
				break;
			case Date:
				writer.write(LLSDNotation.dateNotationMarker);
				writer.write(LLSDNotation.doubleQuotesNotationMarker);
				writer.write(osd.AsString());
				writer.write(LLSDNotation.doubleQuotesNotationMarker);
				break;
			case URI:
				writer.write(LLSDNotation.uriNotationMarker);
				writer.write(LLSDNotation.doubleQuotesNotationMarker);
				writer.write(LLSDNotation.escapeCharacter(osd.AsString(), LLSDNotation.doubleQuotesNotationMarker));
				writer.write(LLSDNotation.doubleQuotesNotationMarker);
				break;
			case Array:
				LLSDNotation.serializeArray(writer, (OSDArray) osd);
				break;
			case Map:
				LLSDNotation.serializeMap(writer, (OSDMap) osd);
				break;
			default:
				throw new IOException("Notation serialization: Not existing element discovered.");
		}
	}

	private static void serializeArray(final Writer writer, final OSDArray osdArray) throws IOException
	{
		writer.write(LLSDNotation.arrayBeginNotationMarker);
		final int lastIndex = osdArray.size() - 1;

		for (int idx = 0; idx <= lastIndex; idx++)
		{
			LLSDNotation.serializeElement(writer, osdArray.get(idx));
			if (idx < lastIndex)
			{
				writer.write(LLSDNotation.kommaNotationDelimiter);
			}
		}
		writer.write(LLSDNotation.arrayEndNotationMarker);
	}

	private static void serializeMap(final Writer writer, final OSDMap osdMap) throws IOException
	{
		writer.write(LLSDNotation.mapBeginNotationMarker);
		final int lastIndex = osdMap.size() - 1;
		int idx = 0;

		for (Map.Entry<String, OSD> kvp : osdMap.entrySet())
		{
			writer.write(singleQuotesNotationMarker);
			writer.write(escapeCharacter(kvp.getKey(), singleQuotesNotationMarker));
			writer.write(singleQuotesNotationMarker);
			writer.write(keyNotationDelimiter);
			serializeElement(writer, kvp.getValue());
			if (idx < lastIndex)
			{
				writer.write(kommaNotationDelimiter);
			}
			idx++;
		}
		writer.write(mapEndNotationMarker);
	}

	private static void serializeElementFormatted(Writer writer, String indent, OSD osd) throws IOException
	{
		switch (osd.getType())
		{
			case Unknown:
				writer.write(undefNotationValue);
				break;
			case Boolean:
				if (osd.AsBoolean())
				{
					writer.write(trueNotationValueTwo);
				}
				else
				{
					writer.write(falseNotationValueTwo);
				}
				break;
			case Integer:
				writer.write(integerNotationMarker);
				writer.write(osd.AsString());
				break;
			case Real:
				writer.write(realNotationMarker);
				writer.write(osd.AsString());
				break;
			case UUID:
				writer.write(uuidNotationMarker);
				writer.write(osd.AsString());
				break;
			case String:
				writer.write(singleQuotesNotationMarker);
				writer.write(escapeCharacter(osd.AsString(), singleQuotesNotationMarker));
				writer.write(singleQuotesNotationMarker);
				break;
			case Binary:
				writer.write(binaryNotationMarker);
				writer.write("64");
				writer.write(doubleQuotesNotationMarker);
				writer.write(osd.AsString());
				writer.write(doubleQuotesNotationMarker);
				break;
			case Date:
				writer.write(dateNotationMarker);
				writer.write(doubleQuotesNotationMarker);
				writer.write(osd.AsString());
				writer.write(doubleQuotesNotationMarker);
				break;
			case URI:
				writer.write(uriNotationMarker);
				writer.write(doubleQuotesNotationMarker);
				writer.write(escapeCharacter(osd.AsString(), doubleQuotesNotationMarker));
				writer.write(doubleQuotesNotationMarker);
				break;
			case Array:
				serializeArrayFormatted(writer, indent + baseIndent, (OSDArray) osd);
				break;
			case Map:
				serializeMapFormatted(writer, indent + baseIndent, (OSDMap) osd);
				break;
			default:
				throw new IOException("Notation serialization: Not existing element discovered.");

		}
	}

	private static void serializeArrayFormatted(Writer writer, String intend, OSDArray osdArray) throws IOException
	{
		writer.write(newLine);
		writer.write(intend);
		writer.write(arrayBeginNotationMarker);
		int lastIndex = osdArray.size() - 1;

		for (int idx = 0; idx <= lastIndex; idx++)
		{
			OSD.OSDType type = osdArray.get(idx).getType();
			if (type != OSD.OSDType.Array && type != OSD.OSDType.Map)
			{
				writer.write(newLine);
			}
			writer.write(baseIndent + intend);
			serializeElementFormatted(writer, intend, osdArray.get(idx));
			if (idx < lastIndex)
			{
				writer.write(kommaNotationDelimiter);
			}
		}
		writer.write(newLine);
		writer.write(intend);
		writer.write(arrayEndNotationMarker);
	}

	private static void serializeMapFormatted(Writer writer, String intend, OSDMap osdMap) throws IOException
	{
		writer.write(newLine);
		writer.write(intend);
		writer.write(mapBeginNotationMarker);
		writer.write(newLine);
		int lastIndex = osdMap.size() - 1;
		int idx = 0;

		for (final Map.Entry<String, OSD> kvp : osdMap.entrySet())
		{
			writer.write(LLSDNotation.baseIndent + intend);
			writer.write(LLSDNotation.singleQuotesNotationMarker);
			writer.write(LLSDNotation.escapeCharacter(kvp.getKey(), LLSDNotation.singleQuotesNotationMarker));
			writer.write(LLSDNotation.singleQuotesNotationMarker);
			writer.write(LLSDNotation.keyNotationDelimiter);
			LLSDNotation.serializeElementFormatted(writer, intend, kvp.getValue());
			if (idx < lastIndex)
			{
				writer.write(LLSDNotation.newLine);
				writer.write(LLSDNotation.baseIndent + intend);
				writer.write(LLSDNotation.kommaNotationDelimiter);
				writer.write(LLSDNotation.newLine);
			}

			idx++;
		}
		writer.write(LLSDNotation.newLine);
		writer.write(intend);
		writer.write(LLSDNotation.mapEndNotationMarker);
	}

	private static String getString(final PushbackReader reader, final int notationChar) throws IOException, ParseException
	{
		switch (notationChar)
		{
			case LLSDNotation.stringNotationMarker:
				final int numChars = LLSDNotation.getLengthInBrackets(reader);
				final char[] chars = new char[numChars];
				if (0 > reader.read() || reader.read(chars, 0, numChars) < numChars || 0 > reader.read())
				{
					throw new ParseException("Notation LLSD parsing: Unexpected end of stream in string.",
							reader.getBytePosition());
				}
				return new String(chars);
			case LLSDNotation.singleQuotesNotationMarker:
			case LLSDNotation.doubleQuotesNotationMarker:
				return OSDParser.getStringDelimitedBy(reader, (char)notationChar);
			default:
				throw new ParseException("Notation LLSD parsing: Invalid string notation character '" + notationChar + "'.",
						reader.getBytePosition());
		}
	}
	
	private static int getLengthInBrackets(final PushbackReader reader) throws IOException, ParseException
	{
		int character;
		final StringBuilder s = new StringBuilder();
		if ((0 < (character = skipWhiteSpace(reader))) && (sizeBeginNotationMarker == (char) character))
		{
			while (0 <= (character = reader.read()) && Character.isDigit((char)character))
			{
				s.append((char) character);
			}
		}
		if (0 > character)
		{
			throw new ParseException("Notation LLSD parsing: Can't parse length value cause unexpected end of stream.",
					reader.getBytePosition());
		}
		else if (sizeEndNotationMarker != character)
		{
			throw new ParseException("Notation LLSD parsing: Can't parse length value, invalid character.",
					reader.getBytePosition());
		}
		return Integer.parseInt(s.toString());
	}

	private static String escapeCharacter(final String s, final char c)
	{
		final String oldOne = String.valueOf(c);
		final String newOne = "\\" + c;

		final String sOne = s.replace("\\", "\\\\").replace(oldOne, newOne);
		return sOne;
	}
}
