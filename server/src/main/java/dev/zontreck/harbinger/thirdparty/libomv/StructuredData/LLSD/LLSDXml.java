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
import java.io.OutputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.Base64;
import java.util.Date;
import java.util.Map;

import dev.zontreck.harbinger.thirdparty.v1.XmlPullParser;
import dev.zontreck.harbinger.thirdparty.v1.XmlPullParserException;
import dev.zontreck.harbinger.thirdparty.v1.XmlPullParserFactory;
import dev.zontreck.harbinger.thirdparty.v1.XmlSerializer;

import dev.zontreck.harbinger.thirdparty.libomv.StructuredData.OSD;
import dev.zontreck.harbinger.thirdparty.libomv.StructuredData.OSDArray;
import dev.zontreck.harbinger.thirdparty.libomv.StructuredData.OSDMap;
import dev.zontreck.harbinger.thirdparty.libomv.StructuredData.OSDParser;
import dev.zontreck.harbinger.thirdparty.libomv.StructuredData.OSDString;
import dev.zontreck.harbinger.thirdparty.libomv.types.UUID;
import dev.zontreck.harbinger.thirdparty.libomv.utils.Helpers;

public final class LLSDXml extends OSDParser
{
	/*
	 * Possible header formats
	 *    <llsd>
	 *    <?llsd/xml?>
	 *    <? llsd/xml ?>
	 *    <?xml..?>
	 */
	private static final String llsdXmlHeader = "?xml" ;
	private static final String llsdXmlHeader2 = "llsd/xml" ;

	private static final String LLSD_TAG = "llsd";
	private static final String UNDEF_TAG = "undef";
	private static final String BOOLEAN_TAG = "boolean";
	private static final String INTEGER_TAG = "integer";
	private static final String REAL_TAG = "real";
	private static final String STRING_TAG = "string";
	private static final String UUID_TAG = "uuid";
	private static final String DATE_TAG = "date";
	private static final String URI_TAG = "uri";
	private static final String BINARY_TAG = "binary";
	private static final String MAP_TAG = "map";
	private static final String KEY_TAG = "key";
	private static final String ARRAY_TAG = "array";


	public static boolean isFormat(final String string)
	{
		final int character = OSDParser.skipWhiteSpace(string);
		if ('<' == character)
		{
			return OSDParser.isHeader(string, LLSDXml.llsdXmlHeader, '>') ||
					OSDParser.isHeader(string, LLSDXml.llsdXmlHeader2, '>');
		}
		return false;
	}
	
	public static boolean isFormat(final byte[] data, String encoding) throws UnsupportedEncodingException
	{
		final int character = OSDParser.skipWhiteSpace(data);
		if ('<' == character)
		{
			if (null == encoding)
				encoding = OSD.OSDFormat.contentEncodingDefault(OSD.OSDFormat.Xml);
			final String str = new String(data, encoding);
			return OSDParser.isHeader(str, LLSDXml.llsdXmlHeader, '>') ||
					OSDParser.isHeader(str, LLSDXml.llsdXmlHeader2, '>');
		}
		return false;
	}

	/**
	 * Parse an OSD XML reader and convert it into an hierarchical OSD object
	 * 
	 * @param reader The OSD XML reader to parse
	 * @return hierarchical OSD object
	 * @throws IOException
	 * @throws ParseException
	 */
	protected OSD unflatten(final Reader reader, final String encoding) throws IOException, ParseException
	{
		try
		{
			final XmlPullParser parser = XmlPullParserFactory.newInstance().newPullParser();
			parser.setInput(reader);
			return LLSDXml.parse(parser);
		}
		catch (final XmlPullParserException ex)
		{
			throw new ParseException(ex.getMessage(), ex.getLineNumber());
		}
	}

	/**
	 * Parse an OSD XML reader and convert it into an hierarchical OSD object
	 * 
	 * @param stream The OSD XML stream to parse
	 * @return hierarchical OSD object
	 * @throws IOException
	 * @throws ParseException
	 */
	protected OSD unflatten(final InputStream stream, final String encoding) throws IOException, ParseException
	{
		try
		{
			final XmlPullParser parser = XmlPullParserFactory.newInstance().newPullParser();
			parser.setInput(stream, encoding);
			return LLSDXml.parse(parser);
		}
		catch (final XmlPullParserException ex)
		{
			throw new ParseException(ex.getMessage(), ex.getLineNumber());
		}
	}

	/**
	 * Serialize an hierarchical OSD object into an OSD XML writer
	 * 
	 * @param writer The writer to format the serialized data into
	 * @param data The hierarchical OSD object to serialize
	 * @throws IOException
	 */
	protected void flatten(final Writer writer, final OSD data, final boolean prependHeader, final String encoding) throws IOException
	{
		try
		{
			final XmlSerializer xmlWriter = XmlPullParserFactory.newInstance().newSerializer();
			xmlWriter.setOutput(writer);
			LLSDXml.serialize(xmlWriter, data, prependHeader, encoding);
		}
		catch (final XmlPullParserException ex)
		{
			throw new IOException(ex.getMessage());
		}
	}

	/**
	 * Serialize an hierarchical OSD object into an OSD XML writer
	 * 
	 * @param stream The writer to format the serialized data into
	 * @param data The hierarchical OSD object to serialize
	 * @throws IOException
	 */
	protected void flatten(final OutputStream stream, final OSD data, final boolean prependHeader, final String encoding) throws IOException
	{
		try
		{
			final XmlSerializer xmlWriter = XmlPullParserFactory.newInstance().newSerializer();
			xmlWriter.setOutput(stream, encoding);
			LLSDXml.serialize(xmlWriter, data, prependHeader, encoding);
		}
		catch (final XmlPullParserException ex)
		{
			throw new IOException(ex.getMessage());
		}
	}

	private static void serialize(final XmlSerializer writer, final OSD data, final boolean prependHeader, final String encoding) throws IOException
	{
		if (prependHeader)
			writer.startDocument(encoding, null);
		writer.startTag(null, LLSDXml.LLSD_TAG);
		LLSDXml.serializeElement(writer, data);
		writer.endTag(null, LLSDXml.LLSD_TAG);
		writer.endDocument();
		writer.flush();
	}

	private static void serializeElement(final XmlSerializer writer, final OSD data) throws IOException
	{
		switch (data.getType())
		{
			case Unknown:
				writer.startTag(null, LLSDXml.UNDEF_TAG).endTag(null, LLSDXml.UNDEF_TAG);
				break;
			case Boolean:
				writer.startTag(null, LLSDXml.BOOLEAN_TAG).text(data.AsString()).endTag(null, LLSDXml.BOOLEAN_TAG);
				break;
			case Integer:
				writer.startTag(null, LLSDXml.INTEGER_TAG).text(data.AsString()).endTag(null, LLSDXml.INTEGER_TAG);
				break;
			case Real:
				writer.startTag(null, LLSDXml.REAL_TAG).text(data.AsString()).endTag(null, LLSDXml.REAL_TAG);
				break;
			case String:
				writer.startTag(null, LLSDXml.STRING_TAG).text(data.AsString()).endTag(null, LLSDXml.STRING_TAG);
				break;
			case UUID:
				writer.startTag(null, LLSDXml.UUID_TAG).text(data.AsString()).endTag(null, LLSDXml.UUID_TAG);
				break;
			case Date:
				writer.startTag(null, LLSDXml.DATE_TAG).text(data.AsString()).endTag(null, LLSDXml.DATE_TAG);
				break;
			case URI:
				writer.startTag(null, LLSDXml.URI_TAG).text(data.AsString()).endTag(null, LLSDXml.URI_TAG);
				break;
			case Binary:
				writer.startTag(null, LLSDXml.BINARY_TAG).
				       attribute(null, "encoding", "base64").
				       text(Base64.getEncoder().encodeToString(data.AsBinary())).
					   endTag(null, LLSDXml.BINARY_TAG);
				break;
			case Map:
				final OSDMap map = (OSDMap) data;
				writer.startTag(null, LLSDXml.MAP_TAG);
				for (final Map.Entry<String, OSD> kvp : map.entrySet())
				{
					writer.startTag(null, LLSDXml.KEY_TAG).text(kvp.getKey()).endTag(null, LLSDXml.KEY_TAG);
					LLSDXml.serializeElement(writer, kvp.getValue());
				}
				writer.endTag(null, LLSDXml.MAP_TAG);
				break;
			case Array:
				final OSDArray array = (OSDArray) data;
				writer.startTag(null, LLSDXml.ARRAY_TAG);
				for (final OSD osd : array)
				{
					LLSDXml.serializeElement(writer, osd);
				}
				writer.endTag(null, LLSDXml.ARRAY_TAG);
				break;
			default:
				break;
		}
	}

	private static OSD parse(final XmlPullParser parser) throws IOException, XmlPullParserException
	{
		OSD ret = null;
		// lets start pulling...
		parser.nextTag();
		parser.require(XmlPullParser.START_TAG, null, LLSDXml.LLSD_TAG);
		if (!parser.isEmptyElementTag())
		{
			parser.nextTag();
			ret = LLSDXml.parseElement(parser);
		}
		parser.nextTag();
		parser.require(XmlPullParser.END_TAG, null, LLSDXml.LLSD_TAG);
		return ret;
	}

	private static OSD parseElement(final XmlPullParser parser) throws IOException, XmlPullParserException
	{
		parser.require(XmlPullParser.START_TAG, null, null);
		String s = null;
		final String name = parser.getName();
		boolean notEmpty = !parser.isEmptyElementTag();
		OSD ret = null;

		if (name.equals(LLSDXml.BOOLEAN_TAG))
		{
			boolean bool = false;
			if (notEmpty)
			{
				s = parser.nextText().trim();
				bool = (null != s && !s.isEmpty() && ("true".equalsIgnoreCase(s) || "1".equals(s)));
			}
			ret = OSD.FromBoolean(bool);
		}
		else if (name.equals(LLSDXml.INTEGER_TAG))
		{
			int value = 0;
			if (notEmpty)
			{
				value = Helpers.TryParseInt(parser.nextText());
			}
			ret = OSD.FromInteger(value);
		}
		else if (name.equals(LLSDXml.REAL_TAG))
		{
			double real = 0.0d;
			if (notEmpty)
			{
				real = Helpers.TryParseDouble(parser.nextText());
			}
			ret = OSD.FromReal(real);
		}
		else if (name.equals(LLSDXml.UUID_TAG))
		{
			UUID uuid = UUID.Zero;
			if (notEmpty)
			{
				uuid = new UUID(parser.nextText());
			}
			ret = OSD.FromUUID(uuid);
		}
		else if (name.equals(LLSDXml.DATE_TAG))
		{
			Date date = Helpers.Epoch;
			if (notEmpty)
			{
				date = new OSDString(parser.nextText()).AsDate();
			}
			ret = OSD.FromDate(date);
		}
		else if (name.equals(LLSDXml.STRING_TAG))
		{
			if (notEmpty)
			{
				s = parser.nextText();
			}
			ret = OSD.FromString(s);
		}
		else if (name.equals(LLSDXml.BINARY_TAG))
		{
			s = parser.getAttributeValue(null, "encoding");
			if (null != s && !"base64".equals(s))
			{
				throw new XmlPullParserException("Unsupported binary encoding: " + s + " encoding", parser, null);
			}
			byte[] data = Helpers.EmptyBytes;
			if (notEmpty)
			{
				data = Base64.getDecoder().decode(parser.nextText());
			}
			ret = OSD.FromBinary(data);
		}
		else if (name.equals(LLSDXml.URI_TAG))
		{
			try
			{
				URI uri = new URI(Helpers.EmptyString);
				if (notEmpty)
				{
					uri = new URI(parser.nextText());
				}
				ret = OSD.FromUri(uri);
			}
			catch (final URISyntaxException ex)
			{
				throw new XmlPullParserException("Error parsing URI: " + ex.getMessage(), parser, ex);
			}
		}
		else if (name.equals(LLSDXml.MAP_TAG))
		{
			ret = new OSDMap();
			if (notEmpty)
			{
				LLSDXml.parseMap(parser, (OSDMap) ret);
			}
		}
		else if (name.equals(LLSDXml.ARRAY_TAG))
		{
			ret = new OSDArray();
			if (notEmpty)
			{
				LLSDXml.parseArray(parser, (OSDArray) ret);
			}
		}
		else
		{
			if (name.equals(LLSDXml.UNDEF_TAG))
			{
				ret = new OSD();
			}
			parser.nextTag();
			notEmpty = true;
		}
		if (!notEmpty)
		{
			parser.nextTag();
		}
		parser.require(XmlPullParser.END_TAG, null, name);
		return ret;
	}

	private static void parseMap(final XmlPullParser parser, final OSDMap map) throws IOException, XmlPullParserException
	{
		while (XmlPullParser.END_TAG != parser.nextTag())
		{
			parser.require(XmlPullParser.START_TAG, null, LLSDXml.KEY_TAG);
			final String key = parser.nextText();
			parser.require(XmlPullParser.END_TAG, null, LLSDXml.KEY_TAG);
			parser.nextTag();
			map.put(key, LLSDXml.parseElement(parser));
		}
	}

	private static void parseArray(final XmlPullParser parser, final OSDArray array) throws IOException, XmlPullParserException
	{
		while (XmlPullParser.END_TAG != parser.nextTag())
		{
			array.add(LLSDXml.parseElement(parser));
		}
	}
}

/*
 * public static boolean validate(XmlPullParser xmlData, RefObject<String>
 * error) { synchronized (XmlValidationLock) { LastXmlErrors =
 * Helpers.EmptyString; XmlTextReader = xmlData;
 * 
 * createSchema();
 * 
 * XmlReaderSettings readerSettings = new XmlReaderSettings();
 * readerSettings.ValidationType = ValidationType.Schema;
 * readerSettings.Schemas.add(XmlSchema); // TODO TASK: Java has no equivalent
 * to C#-style event wireups: readerSettings.ValidationEventHandler += new
 * ValidationEventHandler(LLSDXmlSchemaValidationHandler);
 * 
 * XmlReader reader = XmlReader.Create(xmlData, readerSettings);
 * 
 * try { while (reader.Read()) { } } catch (XmlException t) { error.argvalue =
 * LastXmlErrors; return false; }
 * 
 * if (LastXmlErrors.equals(Helpers.EmptyString)) { error.argvalue = null;
 * return true; } else { error.argvalue = LastXmlErrors; return false; } } }
 */