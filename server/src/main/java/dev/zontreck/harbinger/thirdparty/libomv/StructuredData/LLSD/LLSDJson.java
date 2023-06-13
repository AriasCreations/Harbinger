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
package dev.zontreck.harbinger.thirdparty.libomv.StructuredData.LLSD;

import dev.zontreck.harbinger.thirdparty.libomv.StructuredData.*;
import dev.zontreck.harbinger.thirdparty.libomv.utils.Helpers;
import dev.zontreck.harbinger.thirdparty.libomv.utils.PushbackReader;

import java.io.*;
import java.text.ParseException;
import java.util.Date;
import java.util.Map;

public final class LLSDJson extends OSDParser {
	private static final String llsdJsonKey = "json";
	private static final String llsdJsonHeader = "<?llsd/json?>";

	private static final String baseIndent = "  ";

	private static final char[] newLine = { '\n' };

	private static final char[] nullNotationValue = { 'n' , 'u' , 'l' , 'l' };
	private static final char[] trueNotationValue = { 't' , 'r' , 'u' , 'e' };
	private static final char[] falseNotationValue = { 'f' , 'a' , 'l' , 's' , 'e' };

	private static final char arrayBeginNotationMarker = '[';
	private static final char arrayEndNotationMarker = ']';

	private static final char mapBeginNotationMarker = '{';
	private static final char mapEndNotationMarker = '}';
	private static final char kommaNotationDelimiter = ',';
	private static final char keyNotationDelimiter = ':';

	private static final char doubleQuotesNotationMarker = '"';

	public static boolean isFormat ( final String string ) {
		final int character = OSDParser.skipWhiteSpace ( string );
		if ( '<' == character ) {
			return OSDParser.isHeader ( string , LLSDJson.llsdJsonKey , '>' );
		}
		return false;
	}

	public static boolean isFormat ( final byte[] data , String encoding ) throws UnsupportedEncodingException {
		final int character = OSDParser.skipWhiteSpace ( data );
		if ( '<' == character ) {
			if ( null == encoding )
				encoding = OSD.OSDFormat.contentEncodingDefault ( OSD.OSDFormat.Json );
			return OSDParser.isHeader ( data , llsdJsonKey.getBytes ( encoding ) , '>' );
		}
		return false;
	}

	/**
	 * Read the next LLSD data element in and return the OSD structure for it
	 *
	 * @param reader a pushback reader to read in data from
	 * @return the OSD data corresponding to the LLSD data element
	 * @throws IOException
	 */
	private static OSD parseElement ( final PushbackReader reader ) throws ParseException, IOException {
		final int character = OSDParser.skipWhiteSpace ( reader );
		if ( 0 >= character ) {
			return new OSD ( ); // server returned an empty file, so we're going
			// to pass along a null LLSD object
		}

		switch ( ( char ) character ) {
			case 'n':
				if ( LLSDJson.BufferCharactersEqual ( reader , LLSDJson.nullNotationValue , 1 ) == LLSDJson.nullNotationValue.length ) {
					return new OSD ( );
				}
				break;
			case 'f':
				if ( LLSDJson.BufferCharactersEqual ( reader , LLSDJson.falseNotationValue , 1 ) == LLSDJson.falseNotationValue.length ) {
					return OSD.FromBoolean ( false );
				}
				break;
			case 't':
				if ( LLSDJson.BufferCharactersEqual ( reader , LLSDJson.trueNotationValue , 1 ) == LLSDJson.trueNotationValue.length ) {
					return OSD.FromBoolean ( true );
				}
				break;
			case '-':
			case '+':
			case '0':
			case '1':
			case '2':
			case '3':
			case '4':
			case '5':
			case '6':
			case '7':
			case '8':
			case '9':
				return LLSDJson.parseNumber ( reader , character );
			case LLSDJson.doubleQuotesNotationMarker:
				return LLSDJson.parseString ( reader );
			case LLSDJson.arrayBeginNotationMarker:
				return LLSDJson.parseArray ( reader );
			case LLSDJson.mapBeginNotationMarker:
				return LLSDJson.parseMap ( reader );
			default:
				break;
		}
		throw new ParseException (
				"LLSD JSON parsing: Unexpected character '" + ( char ) character + "'." ,
				reader.getBytePosition ( )
		);
	}

	private static OSD parseNumber ( final PushbackReader reader , int character ) throws IOException {
		final StringBuilder s = new StringBuilder ( );
		if ( '-' == ( char ) character || '+' == ( char ) character ) {
			s.append ( ( char ) character );
			character = reader.read ( );
		}
		boolean isReal = false;
		while ( ( 0 <= character )
				&& (
				Character.isDigit ( ( char ) character ) || '.' == ( char ) character || 'e' == ( char ) character
						|| 'E' == ( char ) character || '+' == ( char ) character || '-' == ( char ) character
		) ) {
			if ( '.' == ( char ) character )
				isReal = true;
			s.append ( ( char ) character );
			character = reader.read ( );
		}
		if ( 0 <= character ) {
			reader.unread ( character );
		}
		if ( isReal )
			return OSD.FromReal ( Double.parseDouble ( s.toString ( ) ) );
		return OSD.FromInteger ( Integer.parseInt ( s.toString ( ) ) );

	}

	private static OSD parseString ( final PushbackReader reader ) throws IOException, ParseException {
		final String string = OSDParser.getStringDelimitedBy ( reader , LLSDJson.doubleQuotesNotationMarker );
		final OSD osd = OSD.FromUUID ( string );
		if ( 16 < string.length ( ) ) {
			final Date date = osd.AsDate ( );
			if ( ! date.equals ( Helpers.Epoch ) )
				return OSD.FromDate ( date );
		}
		return osd;
	}

	private static OSD parseArray ( final PushbackReader reader ) throws IOException, ParseException {
		int character = LLSDJson.kommaNotationDelimiter;
		final OSDArray osdArray = new OSDArray ( );
		while ( ( kommaNotationDelimiter == ( char ) character ) && ( 0 < ( character = OSDParser.skipWhiteSpace ( reader ) ) ) && arrayEndNotationMarker != character ) {
			reader.unread ( character );
			osdArray.add ( LLSDJson.parseElement ( reader ) );
			character = OSDParser.skipWhiteSpace ( reader );
		}
		if ( 0 > character ) {
			throw new ParseException (
					"LLSD JSON parsing: Unexpected end of array discovered." ,
					reader.getBytePosition ( )
			);
		}
		else if ( arrayEndNotationMarker != character ) {
			throw new ParseException (
					"LLSD JSON parsing: Array end expected." ,
					reader.getBytePosition ( )
			);
		}
		return osdArray;
	}

	private static OSD parseMap ( final PushbackReader reader ) throws ParseException, IOException {
		int character = LLSDJson.kommaNotationDelimiter;
		final OSDMap osdMap = new OSDMap ( );
		while ( ( kommaNotationDelimiter == ( char ) character ) && ( 0 < ( character = OSDParser.skipWhiteSpace ( reader ) ) ) && ( mapEndNotationMarker != ( char ) character ) ) {
			if ( doubleQuotesNotationMarker != character ) {
				throw new ParseException ( "LLSD JSON parsing: Invalid key in map" , reader.getBytePosition ( ) );
			}
			final String key = OSDParser.getStringDelimitedBy ( reader , LLSDJson.doubleQuotesNotationMarker );
			character = OSDParser.skipWhiteSpace ( reader );
			if ( keyNotationDelimiter != ( char ) character ) {
				throw new ParseException (
						"LLSD JSON parsing: Invalid key delimiter in map." ,
						reader.getBytePosition ( )
				);
			}
			osdMap.put ( key , LLSDJson.parseElement ( reader ) );
			character = OSDParser.skipWhiteSpace ( reader );
		}
		if ( 0 > character ) {
			throw new ParseException (
					"Json LLSD parsing: Unexpected end of map discovered." ,
					reader.getBytePosition ( )
			);
		}
		return osdMap;
	}

	private static void serializeElement ( final Writer writer , final OSD osd ) throws IOException {
		switch ( osd.getType ( ) ) {
			case Unknown:
				writer.write ( LLSDJson.nullNotationValue );
				break;
			case Boolean:
				if ( osd.AsBoolean ( ) ) {
					writer.write ( LLSDJson.trueNotationValue );
				}
				else {
					writer.write ( LLSDJson.falseNotationValue );
				}
				break;
			case Real:
				if ( Double.isNaN ( osd.AsReal ( ) ) || Double.isInfinite ( osd.AsReal ( ) ) ) {
					writer.write ( LLSDJson.nullNotationValue );
				}
				else {
					final String str = osd.AsString ( );
					writer.write ( str );
					if ( - 1 == str.indexOf ( '.' ) && - 1 == str.indexOf ( 'E' ) )
						writer.write ( ".0" );
				}
				break;
			case Integer:
				writer.write ( osd.AsString ( ) );
				break;
			case String:
			case UUID:
			case Date:
			case URI:
				LLSDJson.serializeString ( writer , osd.AsString ( ) );
				break;
			case Binary:
				LLSDJson.serializeBinary ( writer , ( OSDBinary ) osd );
				break;
			case Array:
				LLSDJson.serializeArray ( writer , ( OSDArray ) osd );
				break;
			case Map:
				LLSDJson.serializeMap ( writer , ( OSDMap ) osd );
				break;
			default:
				throw new IOException ( "Json serialization: Not existing element discovered." );
		}
	}

	private static void serializeString ( final Writer writer , final String string ) throws IOException {
		writer.write ( LLSDJson.doubleQuotesNotationMarker );
		if ( null != string && 0 < string.length ( ) ) {
			char b, c = 0;
			String hhhh;
			int i;
			final int len = string.length ( );

			for ( i = 0; i < len ; i += 1 ) {
				b = c;
				c = string.charAt ( i );
				switch ( c ) {
					case '\\':
					case '"':
						writer.write ( '\\' );
						writer.write ( c );
						break;
					case '/':
						if ( '<' == b ) {
							writer.write ( '\\' );
						}
						writer.write ( c );
						break;
					case '\b':
						writer.write ( "\\b" );
						break;
					case '\t':
						writer.write ( "\\t" );
						break;
					case '\n':
						writer.write ( "\\n" );
						break;
					case '\f':
						writer.write ( "\\f" );
						break;
					case '\r':
						writer.write ( "\\r" );
						break;
					default:
						if ( ' ' > c || ( '\u0080' <= c && '\u00a0' > c )
								|| ( '\u2000' <= c && '\u2100' > c ) ) {
							hhhh = "000" + Integer.toHexString ( c );
							writer.write ( "\\u" + hhhh.substring ( hhhh.length ( ) - 4 ) );
						}
						else {
							writer.write ( c );
						}
				}
			}
		}
		writer.write ( LLSDJson.doubleQuotesNotationMarker );
	}

	private static void serializeBinary ( final Writer writer , final OSDBinary osdBinary ) throws IOException {
		writer.write ( LLSDJson.arrayBeginNotationMarker );
		final byte[] bytes = osdBinary.AsBinary ( );
		final int lastIndex = bytes.length;

		for ( int idx = 0 ; idx < lastIndex ; idx++ ) {
			if ( 0 < idx ) {
				writer.write ( LLSDJson.kommaNotationDelimiter );
			}
			writer.write ( Integer.toString ( bytes[ idx ] ) );
		}
		writer.write ( LLSDJson.arrayEndNotationMarker );
	}

	private static void serializeArray ( final Writer writer , final OSDArray osdArray ) throws IOException {
		writer.write ( LLSDJson.arrayBeginNotationMarker );
		final int lastIndex = osdArray.size ( ) - 1;

		for ( int idx = 0 ; idx <= lastIndex ; idx++ ) {
			LLSDJson.serializeElement ( writer , osdArray.get ( idx ) );
			if ( idx < lastIndex ) {
				writer.write ( LLSDJson.kommaNotationDelimiter );
			}
		}
		writer.write ( LLSDJson.arrayEndNotationMarker );
	}

	private static void serializeMap ( final Writer writer , final OSDMap osdMap ) throws IOException {
		writer.write ( LLSDJson.mapBeginNotationMarker );
		final int lastIndex = osdMap.size ( ) - 1;
		int idx = 0;

		for ( Map.Entry<String, OSD> kvp : osdMap.entrySet ( ) ) {
			serializeString ( writer , kvp.getKey ( ) );
			writer.write ( keyNotationDelimiter );
			serializeElement ( writer , kvp.getValue ( ) );
			if ( idx < lastIndex ) {
				writer.write ( kommaNotationDelimiter );
			}
			idx++;
		}
		writer.write ( mapEndNotationMarker );
	}

	private static void serializeElementFormatted ( Writer writer , String indent , OSD osd ) throws IOException {
		switch ( osd.getType ( ) ) {
			case Unknown:
				writer.write ( nullNotationValue );
				break;
			case Boolean:
				if ( osd.AsBoolean ( ) ) {
					writer.write ( trueNotationValue );
				}
				else {
					writer.write ( falseNotationValue );
				}
				break;
			case Integer:
			case Real:
				writer.write ( osd.AsString ( ) );
				break;
			case String:
			case UUID:
			case Date:
			case URI:
				serializeString ( writer , osd.AsString ( ) );
				break;
			case Binary:
				serializeBinary ( writer , ( OSDBinary ) osd );
				break;
			case Array:
				serializeArrayFormatted ( writer , indent + baseIndent , ( OSDArray ) osd );
				break;
			case Map:
				serializeMapFormatted ( writer , indent + baseIndent , ( OSDMap ) osd );
				break;
			default:
				throw new IOException ( "Json serialization: Not existing element discovered." );

		}
	}

	private static void serializeArrayFormatted ( Writer writer , String indent , OSDArray osdArray ) throws IOException {
		writer.write ( arrayBeginNotationMarker );
		int lastIndex = osdArray.size ( ) - 1;

		for ( int idx = 0 ; idx <= lastIndex ; idx++ ) {
			writer.write ( newLine );
			writer.write ( indent );
			serializeElementFormatted ( writer , indent + baseIndent , osdArray.get ( idx ) );
			if ( idx < lastIndex ) {
				writer.write ( kommaNotationDelimiter );
			}
		}
		writer.write ( newLine );
		writer.write ( indent );
		writer.write ( arrayEndNotationMarker );
	}

	private static void serializeMapFormatted ( Writer writer , String indent , OSDMap osdMap ) throws IOException {
		writer.write ( mapBeginNotationMarker );
		int lastIndex = osdMap.size ( ) - 1;
		int idx = 0;

		for ( final Map.Entry<String, OSD> kvp : osdMap.entrySet ( ) ) {
			writer.write ( LLSDJson.newLine );
			writer.write ( indent );
			LLSDJson.serializeString ( writer , kvp.getKey ( ) );
			writer.write ( LLSDJson.keyNotationDelimiter );
			LLSDJson.serializeElementFormatted ( writer , indent + LLSDJson.baseIndent , kvp.getValue ( ) );
			if ( idx < lastIndex ) {
				writer.write ( LLSDJson.kommaNotationDelimiter );
			}

			idx++;
		}
		writer.write ( LLSDJson.newLine );
		writer.write ( indent );
		writer.write ( LLSDJson.mapEndNotationMarker );
	}

	private static int BufferCharactersEqual ( final PushbackReader reader , final char[] buffer , int offset ) throws IOException {

		boolean charactersEqual = true;
		int character;

		while ( offset < buffer.length && 0 <= ( character = reader.read ( ) ) && charactersEqual ) {
			if ( ( ( char ) character ) != buffer[ offset ] ) {
				charactersEqual = false;
				reader.unread ( character );
				break;
			}
			offset++;
		}
		return offset;
	}

	/**
	 * Parse an JSON byte stream and convert it into an hierarchical OSD
	 * object
	 *
	 * @param reader   The JSON reader to parse from
	 * @param encoding The text encoding to use (not used)
	 * @return hierarchical OSD object
	 * @throws IOException
	 * @throws ParseException
	 */
	protected OSD unflatten ( Reader reader , String encoding ) throws ParseException, IOException {
		PushbackReader push = reader instanceof PushbackReader ? ( PushbackReader ) reader : new PushbackReader ( reader );
		int marker = OSDParser.skipWhiteSpace ( push );
		if ( 0 > marker ) {
			return new OSD ( );
		}
		else if ( '<' == marker ) {
			int offset = push.getBytePosition ( );
			if ( ! OSDParser.isHeader ( push , llsdJsonKey , '>' ) )
				throw new ParseException ( "Failed to decode Json LLSD" , offset );
		}
		else {
			push.unread ( marker );
		}
		return parseElement ( push );
	}

	/**
	 * Parse an JSON byte stream and convert it into an hierarchical OSD
	 * object
	 *
	 * @param stream   The JSON byte stream to parse
	 * @param encoding The text encoding to use when converting the stream to text
	 * @return hierarchical OSD object
	 * @throws IOException
	 * @throws ParseException
	 */
	protected OSD unflatten ( InputStream stream , String encoding ) throws ParseException, IOException {
		if ( null == encoding )
			encoding = OSD.OSDFormat.contentEncodingDefault ( OSD.OSDFormat.Json );
		PushbackReader push = new PushbackReader ( new InputStreamReader ( stream , encoding ) );
		int marker = OSDParser.skipWhiteSpace ( push );
		if ( 0 > marker ) {
			return new OSD ( );
		}
		else if ( '<' == marker ) {
			int offset = push.getBytePosition ( );
			if ( ! OSDParser.isHeader ( push , llsdJsonKey , '>' ) )
				throw new ParseException ( "Failed to decode Json LLSD" , offset );
		}
		else {
			push.unread ( marker );
		}
		return parseElement ( push );
	}

	/**
	 * Serialize an hierarchical OSD object into an JSON writer
	 *
	 * @param writer        The writer to format the serialized data into
	 * @param data          The hierarchical OSD object to serialize
	 * @param prependHeader Indicates if the format header should be prepended
	 * @param encoding      The text encoding to use (not used)
	 * @throws IOException
	 */
	protected void flatten ( Writer writer , OSD data , boolean prependHeader , String encoding ) throws IOException {
		if ( prependHeader ) {
			writer.write ( llsdJsonHeader );
			writer.write ( '\n' );
		}
		serializeElement ( writer , data );
	}

	/**
	 * Serialize an hierarchical OSD object into an JSON writer
	 *
	 * @param stream        The output stream to write the serialized data into
	 * @param data          The hierarchical OSD object to serialize
	 * @param prependHeader Indicates if the format header should be prepended
	 * @throws IOException
	 */
	protected void flatten ( OutputStream stream , OSD data , boolean prependHeader , String encoding ) throws IOException {
		if ( null == encoding )
			encoding = OSD.OSDFormat.contentEncodingDefault ( OSD.OSDFormat.Json );
		final Writer writer = new OutputStreamWriter ( stream , encoding );
		if ( prependHeader ) {
			writer.write ( LLSDJson.llsdJsonHeader );
			writer.write ( '\n' );
		}
		LLSDJson.serializeElement ( writer , data );
	}
}
