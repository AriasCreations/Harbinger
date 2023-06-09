/**
 * Copyright (c) 2006-2014, openmetaverse.org
 * Copyright (c) 2009-2017, Frederick Martian
 * All rights reserved.
 * <p>
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * <p>
 * - Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * - Neither the name of the openmetaverse.org or dev.zontreck.harbinger.thirdparty.libomv-java project nor the
 * names of its contributors may be used to endorse or promote products derived
 * from this software without specific prior written permission.
 * <p>
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
package dev.zontreck.harbinger.thirdparty.libomv.StructuredData;

import dev.zontreck.harbinger.thirdparty.libomv.StructuredData.LLSD.LLSDBinary;
import dev.zontreck.harbinger.thirdparty.libomv.StructuredData.LLSD.LLSDJson;
import dev.zontreck.harbinger.thirdparty.libomv.StructuredData.LLSD.LLSDNotation;
import dev.zontreck.harbinger.thirdparty.libomv.StructuredData.LLSD.LLSDXml;
import dev.zontreck.harbinger.thirdparty.libomv.utils.PushbackInputStream;
import dev.zontreck.harbinger.thirdparty.libomv.utils.PushbackReader;

import java.io.*;
import java.text.ParseException;

public abstract class OSDParser {
	public static OSDParser createInstance ( OSD.OSDFormat format ) {
		switch ( format ) {
			case Binary:
				return new LLSDBinary ( );
			case Notation:
				return new LLSDNotation ( );
			case Xml:
				return new LLSDXml ( );
			case Json:
				return new LLSDJson ( );
			default:
				break;
		}
		return null;
	}

	public static OSDParser createInstance ( String header , String encoding ) throws UnsupportedEncodingException {
		if ( LLSDBinary.isFormat ( header , encoding ) ) {
			return new LLSDBinary ( );
		}
		else if ( LLSDNotation.isFormat ( header ) ) {
			return new LLSDNotation ( );
		}
		else if ( LLSDJson.isFormat ( header ) ) {
			return new LLSDJson ( );
		}
		else if ( LLSDXml.isFormat ( header ) ) {
			return new LLSDXml ( );
		}
		return new LLSDJson ( );
	}

	public static OSDParser createInstance ( byte[] header , String encoding ) throws UnsupportedEncodingException {
		if ( LLSDBinary.isFormat ( header ) ) {
			return new LLSDBinary ( );
		}
		else if ( LLSDNotation.isFormat ( header , encoding ) ) {
			return new LLSDNotation ( );
		}
		else if ( LLSDJson.isFormat ( header , encoding ) ) {
			return new LLSDJson ( );
		}
		else if ( LLSDXml.isFormat ( header , encoding ) ) {
			return new LLSDXml ( );
		}
		return new LLSDJson ( );
	}

	private static OSD deserialize ( OSDParser parser , String string , String encoding ) throws IOException, ParseException {
		PushbackReader push = new PushbackReader ( new StringReader ( string ) );
		try {
			return parser.unflatten ( push , encoding );
		} finally {
			push.close ( );
		}
	}

	public static OSD deserialize ( String string ) throws IOException, ParseException {
		OSDParser parser = createInstance ( string , null );
		if ( null != parser ) {
			return deserialize ( parser , string , null );
		}
		return null;
	}

	public static OSD deserialize ( String string , String encoding ) throws IOException, ParseException {
		OSDParser parser = createInstance ( string , encoding );
		if ( null != parser ) {
			return deserialize ( parser , string , encoding );
		}
		return null;
	}

	public static OSD deserialize ( String string , OSD.OSDFormat format ) throws IOException, ParseException {
		OSDParser parser = createInstance ( format );
		if ( null != parser ) {
			return deserialize ( parser , string , null );
		}
		return null;
	}

	public static OSD deserialize ( String string , OSD.OSDFormat format , String encoding ) throws IOException, ParseException {
		OSDParser parser = createInstance ( format );
		if ( null != parser ) {
			return deserialize ( parser , string , encoding );
		}
		return null;
	}

	private static String header ( Reader reader ) throws IOException {
		int ch = skipWhiteSpace ( reader );
		if ( '<' == ch ) {
			StringBuilder string = new StringBuilder ( );
			string.append ( '<' );
			while ( 0 < ( ch = reader.read ( ) ) ) {
				string.append ( ch );
				if ( '>' == ch )
					break;
			}

			if ( 0 < ch ) {
				return string.toString ( );
			}
		}
		return null;
	}

	public static OSD deserialize ( Reader reader ) throws IOException, ParseException {
		String header = header ( reader );
		if ( null != header ) {
			OSDParser parser = createInstance ( header , null );
			return parser.unflatten ( new PushbackReader ( reader , header.length ( ) ) , null );
		}
		return null;
	}

	public static OSD deserialize ( Reader reader , String encoding ) throws IOException, ParseException {
		String header = header ( reader );
		if ( null != header ) {
			OSDParser parser = createInstance ( header , encoding );
			return parser.unflatten ( new PushbackReader ( reader , header.length ( ) ) , encoding );
		}
		return null;
	}

	public static OSD deserialize ( Reader reader , OSD.OSDFormat format ) throws IOException, ParseException {
		OSDParser parser = createInstance ( format );
		return parser.unflatten ( reader , null );
	}

	public static OSD deserialize ( Reader reader , OSD.OSDFormat format , String encoding ) throws IOException, ParseException {
		OSDParser parser = createInstance ( format );
		return parser.unflatten ( reader , encoding );
	}

	private static OSD deserialize ( OSDParser parser , byte[] bytes , String encoding ) throws IOException, ParseException {
		PushbackInputStream push = new PushbackInputStream ( new ByteArrayInputStream ( bytes ) );
		try {
			return parser.unflatten ( push , encoding );
		} finally {
			push.close ( );
		}
	}

	public static OSD deserialize ( byte[] bytes ) throws IOException, ParseException {
		OSDParser parser = createInstance ( bytes , null );
		if ( null != parser ) {
			return deserialize ( parser , bytes , null );
		}
		return null;
	}

	public static OSD deserialize ( byte[] bytes , String encoding ) throws IOException, ParseException {
		OSDParser parser = createInstance ( bytes , encoding );
		if ( null != parser ) {
			return deserialize ( parser , bytes , encoding );
		}
		return null;
	}

	public static OSD deserialize ( byte[] bytes , OSD.OSDFormat format ) throws IOException, ParseException {
		OSDParser parser = createInstance ( format );
		if ( null != parser ) {
			return deserialize ( parser , bytes , null );
		}
		return null;
	}

	public static OSD deserialize ( byte[] bytes , OSD.OSDFormat format , String encoding ) throws IOException, ParseException {
		OSDParser parser = createInstance ( format );
		if ( null != parser ) {
			return deserialize ( parser , bytes , encoding );
		}
		return null;
	}

	private static byte[] header ( InputStream stream ) throws IOException {
		int ch = skipWhiteSpace ( stream );
		if ( '<' == ch ) {
			ByteArrayOutputStream bytes = new ByteArrayOutputStream ( );
			bytes.write ( ( byte ) '<' );
			while ( 0 < ( ch = stream.read ( ) ) ) {
				bytes.write ( ( byte ) ch );
				if ( '>' == ch )
					break;
			}

			if ( '>' == ch ) {
				return bytes.toByteArray ( );
			}
		}
		return null;
	}

	public static OSD deserialize ( InputStream stream ) throws IOException, ParseException {
		byte[] header = header ( stream );
		if ( null != header ) {
			OSDParser parser = createInstance ( header , null );
			return parser.unflatten ( new PushbackInputStream ( stream , header.length ) , null );
		}
		return null;
	}

	public static OSD deserialize ( InputStream stream , String encoding ) throws IOException, ParseException {
		byte[] header = header ( stream );
		if ( null != header ) {
			OSDParser parser = createInstance ( header , encoding );
			return parser.unflatten ( new PushbackInputStream ( stream , header.length ) , encoding );
		}
		return null;
	}

	public static OSD deserialize ( InputStream stream , OSD.OSDFormat format ) throws IOException, ParseException {
		OSDParser parser = createInstance ( format );
		return parser.unflatten ( stream , null );
	}

	public static OSD deserialize ( InputStream stream , OSD.OSDFormat format , String encoding ) throws IOException, ParseException {
		OSDParser parser = createInstance ( format );
		return parser.unflatten ( stream , encoding );
	}

	public static String serializeToString ( OSD osd , OSD.OSDFormat format ) throws IOException {
		StringWriter writer = new StringWriter ( );
		try {
			serialize ( writer , osd , format , true , null );
			return writer.toString ( );
		} finally {
			writer.close ( );
		}
	}

	public static String serializeToString ( OSD osd , OSD.OSDFormat format , boolean prependHeader ) throws IOException {
		StringWriter writer = new StringWriter ( );
		try {
			serialize ( writer , osd , format , prependHeader , null );
			return writer.toString ( );
		} finally {
			writer.close ( );
		}
	}

	public static String serializeToString ( OSD osd , OSD.OSDFormat format , boolean prependHeader , String encoding ) throws IOException {
		StringWriter writer = new StringWriter ( );
		try {
			serialize ( writer , osd , format , prependHeader , null );
			return writer.toString ( );
		} finally {
			writer.close ( );
		}
	}

	public static void serialize ( Writer writer , OSD osd , OSD.OSDFormat format ) throws IOException {
		serialize ( writer , osd , format , true , null );
	}

	public static void serialize ( Writer writer , OSD osd , OSD.OSDFormat format , boolean prependHeader ) throws IOException {
		serialize ( writer , osd , format , prependHeader , null );
	}

	public static void serialize ( Writer writer , OSD osd , OSD.OSDFormat format , boolean prependHeader , String encoding ) throws IOException {
		OSDParser parser = createInstance ( format );
		parser.flatten ( writer , osd , prependHeader , encoding );
	}

	public static byte[] serializeToBytes ( OSD osd , OSD.OSDFormat format ) throws IOException {
		ByteArrayOutputStream stream = new ByteArrayOutputStream ( );
		try {
			serialize ( stream , osd , format , true , null );
			return stream.toByteArray ( );
		} finally {
			stream.close ( );
		}
	}

	public static byte[] serializeToBytes ( OSD osd , OSD.OSDFormat format , boolean prependHeader ) throws IOException {
		ByteArrayOutputStream stream = new ByteArrayOutputStream ( );
		try {
			serialize ( stream , osd , format , prependHeader , null );
			return stream.toByteArray ( );
		} finally {
			stream.close ( );
		}
	}

	public static byte[] serializeToBytes ( OSD osd , OSD.OSDFormat format , boolean prependHeader , String encoding ) throws IOException {
		ByteArrayOutputStream stream = new ByteArrayOutputStream ( );
		try {
			serialize ( stream , osd , format , prependHeader , encoding );
			return stream.toByteArray ( );
		} finally {
			stream.close ( );
		}
	}

	public static void serialize ( OutputStream stream , OSD osd , OSD.OSDFormat format ) throws IOException {
		serialize ( stream , osd , format , true , null );
	}

	public static void serialize ( OutputStream stream , OSD osd , OSD.OSDFormat format , boolean prependHeader ) throws IOException {
		serialize ( stream , osd , format , prependHeader , null );
	}

	public static void serialize ( OutputStream stream , OSD osd , final OSD.OSDFormat format , final boolean prependHeader , final String encoding ) throws IOException {
		final OSDParser parser = OSDParser.createInstance ( format );
		parser.flatten ( stream , osd , prependHeader , encoding );
	}

	protected static int bufferCharactersEqual ( final PushbackReader reader , final char[] buffer , int offset ) throws IOException {

		boolean charactersEqual = true;
		int character;

		while ( 0 <= ( character = reader.read ( ) ) && offset < buffer.length && charactersEqual ) {
			if ( ( ( char ) character ) != buffer[ offset ] ) {
				charactersEqual = false;
				reader.unread ( character );
				break;
			}
			offset++;
		}
		return offset;
	}

	protected static String getStringDelimitedBy ( final PushbackReader reader , final char delimiter ) throws IOException, ParseException {
		int character;
		boolean foundEscape = false;
		final StringBuilder s = new StringBuilder ( );
		while ( ( 0 <= ( character = reader.read ( ) ) )
				&& ( ( ( char ) character != delimiter ) || ( ( char ) character == delimiter && foundEscape ) ) ) {
			if ( foundEscape ) {
				foundEscape = false;
				switch ( ( char ) character ) {
					case 'b':
						s.append ( '\b' );
						break;
					case 'f':
						s.append ( '\f' );
						break;
					case 'n':
						s.append ( '\n' );
						break;
					case 'r':
						s.append ( '\r' );
						break;
					case 't':
						s.append ( '\t' );
						break;
					case 'u':
						final char[] buf = new char[ 4 ];
						if ( 4 == reader.read ( buf ) ) {
							s.append ( ( char ) Integer.parseInt ( new String ( buf ) , 16 ) );
						}
						else {
							throw new ParseException ( "LLSD parsing: Unexpected end of data in parsing hex string numeric" , reader.getBytePosition ( ) );
						}
						break;
					default:
						s.append ( ( char ) character );
						break;
				}
			}
			else if ( '\\' == ( char ) character ) {
				foundEscape = true;
			}
			else {
				s.append ( ( char ) character );
			}
		}
		if ( 0 > character ) {
			throw new ParseException (
					"LLSD parsing: Can't parse text because unexpected end of stream while expecting a '"
							+ delimiter + "' character." , reader.getBytePosition ( ) );
		}
		return s.toString ( );
	}

	protected static int skipWhiteSpace ( final Reader reader ) throws IOException {
		int character;
		while ( 0 <= ( character = reader.read ( ) ) ) {
			final char c = ( char ) character;
			if ( ' ' != c && '\t' != c && '\n' != c && '\r' != c ) {
				break;
			}
		}
		return character;
	}

	protected static boolean isHeader ( final Reader reader , final String string , final int ending ) throws IOException {
		int ch, pos = 0;
		while ( 0 <= ( ( ch = reader.read ( ) ) ) && ch != ending ) {
			if ( pos < string.length ( ) ) {
				if ( ch == string.charAt ( pos ) ) {
					pos++;
				}
				else {
					pos = 0;
				}
			}
		}
		return pos == string.length ( );
	}

	protected static int skipWhiteSpace ( final InputStream stream ) throws IOException {
		int character;
		while ( 0 <= ( character = stream.read ( ) ) ) {
			final byte b = ( byte ) character;
			if ( ' ' != b && '\t' != b && '\n' != b && '\r' != b ) {
				break;
			}
		}
		return character;
	}

	protected static boolean isHeader ( final InputStream stream , final byte[] data , final int ending ) throws IOException {
		int ch, pos = 0;
		while ( 0 <= ( ( ch = stream.read ( ) ) ) && ch != ending ) {
			if ( pos < data.length ) {
				if ( ch == data[ pos ] ) {
					pos++;
				}
				else {
					pos = 0;
				}
			}
		}
		return pos == data.length;
	}

	protected static int skipWhiteSpace ( final String input ) {
		int off = 0;
		while ( input.length ( ) > off ) {
			final char b = input.charAt ( off );
			off++;
			if ( ' ' != b || '\t' != b || '\n' != b || '\r' != b ) {
				return b;
			}
		}
		return - 1;
	}

	protected static boolean isHeader ( final String input , final String string , final int ending ) {
		int pos = 0, off = 0;
		while ( input.length ( ) > off && input.charAt ( off ) != ending ) {
			if ( pos < string.length ( ) ) {
				if ( input.charAt ( off ) == string.charAt ( pos ) ) {
					pos++;
				}
				else {
					pos = 0;
				}
			}
			off++;
		}
		return pos == string.length ( );
	}

	protected static int skipWhiteSpace ( final byte[] input ) {
		int off = 0;
		while ( input.length > off ) {
			final byte b = input[ off ];
			off++;
			if ( ' ' != b || '\t' != b || '\n' != b || '\r' != b ) {
				return b;
			}
		}
		return - 1;
	}

	protected static boolean isHeader ( final byte[] input , final byte[] data , final int ending ) {
		int pos = 0, off = 0;
		while ( input.length > off && input[ off ] != ending ) {
			if ( pos < data.length ) {
				if ( input[ off ] == data[ pos ] ) {
					pos++;
				}
				else {
					pos = 0;
				}
			}
			off++;
		}
		return pos == data.length;
	}

	protected static byte[] consumeBytes ( final PushbackInputStream stream , final int consumeBytes ) throws IOException, ParseException {
		final byte[] bytes = new byte[ consumeBytes ];
		if ( stream.read ( bytes , 0 , consumeBytes ) < consumeBytes ) {
			throw new ParseException ( "Binary LLSD parsing: Unexpected end of stream." , ( int ) stream.getBytePosition ( ) );
		}
		return bytes;
	}

	protected abstract OSD unflatten ( Reader reader , String encoding ) throws IOException, ParseException;

	protected abstract OSD unflatten ( InputStream reader , String encoding ) throws IOException, ParseException;

	protected abstract void flatten ( Writer writer , OSD osd , boolean prependHeader , String encoding ) throws IOException;

	protected abstract void flatten ( OutputStream stream , OSD osd , boolean prependHeader , String encoding ) throws IOException;
}
