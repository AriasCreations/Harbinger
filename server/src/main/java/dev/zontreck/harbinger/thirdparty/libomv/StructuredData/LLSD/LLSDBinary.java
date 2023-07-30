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

/*
 * This implementation is based upon the description at
 * http://wiki.secondlife.com/wiki/LLSD
 * and (partially) tested against the (supposed) reference implementation at
 * http://svn.secondlife.com/svn/linden/release/indra/lib/python/indra/base/osd.py
 */

import dev.zontreck.harbinger.thirdparty.libomv.StructuredData.OSD;
import dev.zontreck.harbinger.thirdparty.libomv.StructuredData.OSDArray;
import dev.zontreck.harbinger.thirdparty.libomv.StructuredData.OSDMap;
import dev.zontreck.harbinger.thirdparty.libomv.StructuredData.OSDParser;
import dev.zontreck.harbinger.thirdparty.libomv.types.UUID;
import dev.zontreck.harbinger.thirdparty.libomv.utils.Helpers;
import dev.zontreck.harbinger.thirdparty.libomv.utils.PushbackInputStream;
import org.apache.commons.io.input.ReaderInputStream;
import org.apache.commons.io.output.WriterOutputStream;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.Map;

public final class LLSDBinary extends OSDParser {
	private static final int int32Length = 4;
	private static final int doubleLength = 8;

	/*
	 * Possible header formats
	 *    <?llsd/binary?>
	 *    <? llsd/binary ?>
	 */
	private static final byte[] llsdBinaryHeader = { 'l' , 'l' , 's' , 'd' , '/' , 'b' , 'i' , 'n' , 'a' , 'r' , 'y' };
	private static final byte[] llsdBinaryHead = { '<' , '?' , 'l' , 'l' , 's' , 'd' , '/' , 'b' , 'i' , 'n' , 'a' , 'r' , 'y' , '?' , '>' };

	private static final byte undefBinaryValue = ( byte ) '!';
	private static final byte trueBinaryValue = ( byte ) '1';
	private static final byte falseBinaryValue = ( byte ) '0';
	private static final byte integerBinaryMarker = ( byte ) 'i';
	private static final byte realBinaryMarker = ( byte ) 'r';
	private static final byte uuidBinaryMarker = ( byte ) 'u';
	private static final byte binaryBinaryMarker = ( byte ) 'b';
	private static final byte stringBinaryMarker = ( byte ) 's';
	private static final byte uriBinaryMarker = ( byte ) 'l';
	private static final byte dateBinaryMarker = ( byte ) 'd';
	private static final byte arrayBeginBinaryMarker = ( byte ) '[';
	private static final byte arrayEndBinaryMarker = ( byte ) ']';
	private static final byte mapBeginBinaryMarker = ( byte ) '{';
	private static final byte mapEndBinaryMarker = ( byte ) '}';
	private static final byte keyBinaryMarker = ( byte ) 'k';
	private static final byte doubleQuotesNotationMarker = '"';
	private static final byte singleQuotesNotationMarker = '\'';

	public static boolean isFormat ( final String string , String encoding ) throws UnsupportedEncodingException {
		final int character = OSDParser.skipWhiteSpace ( string );
		if ( '<' == character ) {
			if ( null == encoding )
				encoding = OSD.OSDFormat.contentEncodingDefault ( OSD.OSDFormat.Binary );
			return OSDParser.isHeader ( string , new String ( llsdBinaryHeader , encoding ) , '>' );
		}
		return false;
	}

	public static boolean isFormat ( byte[] data ) {
		int character = OSDParser.skipWhiteSpace ( data );
		if ( '<' == character ) {
			return OSDParser.isHeader ( data , llsdBinaryHeader , '>' );
		}
		return false;
	}

	private static void serializeElement ( final OutputStream stream , final OSD osd , final String encoding ) throws IOException {
		switch ( osd.getType ( ) ) {
			case Unknown:
				stream.write ( LLSDBinary.undefBinaryValue );
				break;
			case Boolean:
				stream.write ( osd.AsBinary ( ) , 0 , 1 );
				break;
			case Integer:
				stream.write ( LLSDBinary.integerBinaryMarker );
				stream.write ( osd.AsBinary ( ) , 0 , LLSDBinary.int32Length );
				break;
			case Real:
				stream.write ( LLSDBinary.realBinaryMarker );
				stream.write ( osd.AsBinary ( ) , 0 , LLSDBinary.doubleLength );
				break;
			case UUID:
				stream.write ( LLSDBinary.uuidBinaryMarker );
				stream.write ( osd.AsBinary ( ) , 0 , 16 );
				break;
			case String:
				stream.write ( LLSDBinary.stringBinaryMarker );
				LLSDBinary.serializeString ( stream , osd.AsString ( ) , encoding );
				break;
			case Binary:
				stream.write ( LLSDBinary.binaryBinaryMarker );
				final byte[] bytes = osd.AsBinary ( );
				stream.write ( Helpers.Int32ToBytesB ( bytes.length ) );
				stream.write ( bytes , 0 , bytes.length );
				break;
			case Date:
				stream.write ( LLSDBinary.dateBinaryMarker );
				stream.write ( osd.AsBinary ( ) , 0 , LLSDBinary.doubleLength );
				break;
			case URI:
				stream.write ( LLSDBinary.uriBinaryMarker );
				LLSDBinary.serializeString ( stream , osd.AsString ( ) , encoding );
				break;
			case Array:
				LLSDBinary.serializeArray ( stream , ( OSDArray ) osd , encoding );
				break;
			case Map:
				LLSDBinary.serializeMap ( stream , ( OSDMap ) osd , encoding );
				break;
			default:
				throw new IOException ( "Binary serialization: Not existing element discovered." );
		}
	}

	private static void serializeString ( final OutputStream stream , final String string , final String encoding ) throws IOException {
		final byte[] bytes = string.getBytes ( encoding );
		stream.write ( Helpers.Int32ToBytesB ( bytes.length ) );
		stream.write ( bytes , 0 , bytes.length );
	}

	private static void serializeArray ( final OutputStream stream , final OSDArray osdArray , final String encoding ) throws IOException {
		stream.write ( LLSDBinary.arrayBeginBinaryMarker );
		stream.write ( Helpers.Int32ToBytesB ( osdArray.size ( ) ) );

		for ( final OSD osd : osdArray ) {
			LLSDBinary.serializeElement ( stream , osd , encoding );
		}
		stream.write ( LLSDBinary.arrayEndBinaryMarker );
	}

	private static void serializeMap ( final OutputStream stream , final OSDMap osdMap , final String encoding ) throws IOException {
		stream.write ( LLSDBinary.mapBeginBinaryMarker );
		stream.write ( Helpers.Int32ToBytesB ( osdMap.size ( ) ) );

		for ( final Map.Entry<String, OSD> kvp : osdMap.entrySet ( ) ) {
			stream.write ( LLSDBinary.keyBinaryMarker );
			LLSDBinary.serializeString ( stream , kvp.getKey ( ) , encoding );
			LLSDBinary.serializeElement ( stream , kvp.getValue ( ) , encoding );
		}
		stream.write ( LLSDBinary.mapEndBinaryMarker );
	}

	private static OSD parseElement ( final PushbackInputStream stream , final String encoding ) throws IOException, ParseException {
		final int marker = OSDParser.skipWhiteSpace ( stream );
		if ( 0 > marker ) {
			throw new ParseException ( "Binary LLSD parsing: Unexpected end of stream." , 1 );
		}

		final OSD osd;
		switch ( ( byte ) marker ) {
			case LLSDBinary.undefBinaryValue:
				osd = new OSD ( );
				break;
			case LLSDBinary.trueBinaryValue:
				osd = OSD.FromBoolean ( true );
				break;
			case LLSDBinary.falseBinaryValue:
				osd = OSD.FromBoolean ( false );
				break;
			case LLSDBinary.integerBinaryMarker:
				final int integer = Helpers.BytesToInt32B ( OSDParser.consumeBytes ( stream , LLSDBinary.int32Length ) );
				osd = OSD.FromInteger ( integer );
				break;
			case LLSDBinary.realBinaryMarker:
				final double dbl = Helpers.BytesToDoubleB ( OSDParser.consumeBytes ( stream , LLSDBinary.doubleLength ) , 0 );
				osd = OSD.FromReal ( dbl );
				break;
			case LLSDBinary.uuidBinaryMarker:
				osd = OSD.FromUUID ( new UUID ( OSDParser.consumeBytes ( stream , 16 ) ) );
				break;
			case LLSDBinary.binaryBinaryMarker:
				final int binaryLength = Helpers.BytesToInt32B ( OSDParser.consumeBytes ( stream , LLSDBinary.int32Length ) );
				osd = OSD.FromBinary ( OSDParser.consumeBytes ( stream , binaryLength ) );
				break;
			case LLSDBinary.doubleQuotesNotationMarker:
			case LLSDBinary.singleQuotesNotationMarker:
				throw new ParseException ( "Binary LLSD parsing: LLSD Notation Format strings are not yet supported" , ( int ) stream.getBytePosition ( ) );
			case LLSDBinary.stringBinaryMarker:
				final int stringLength = Helpers.BytesToInt32B ( OSDParser.consumeBytes ( stream , LLSDBinary.int32Length ) );
				osd = OSD.FromString ( new String ( OSDParser.consumeBytes ( stream , stringLength ) , encoding ) );
				break;
			case LLSDBinary.uriBinaryMarker:
				final int uriLength = Helpers.BytesToInt32B ( OSDParser.consumeBytes ( stream , LLSDBinary.int32Length ) );
				final URI uri;
				try {
					uri = new URI ( new String ( OSDParser.consumeBytes ( stream , uriLength ) , encoding ) );
				} catch ( final URISyntaxException ex ) {
					throw new ParseException (
							"Binary LLSD parsing: Invalid Uri format detected: " + ex.getMessage ( ) ,
							( int ) stream.getBytePosition ( )
					);
				}
				osd = OSD.FromUri ( uri );
				break;
			case LLSDBinary.dateBinaryMarker:
				/* LLSD Wiki says that the double is also in network byte order, like the real numbers but Openmetaverse as well as the
				 * LLSDBinaryParser::doParse in llsdserialize.cpp clearly do not do any byteswapping.
				 */
				final double timestamp = Helpers.BytesToDoubleL ( OSDParser.consumeBytes ( stream , LLSDBinary.doubleLength ) , 0 );
				osd = OSD.FromDate ( Helpers.UnixTimeToDateTime ( timestamp ) );
				break;
			case LLSDBinary.arrayBeginBinaryMarker:
				osd = LLSDBinary.parseArray ( stream , encoding );
				break;
			case LLSDBinary.mapBeginBinaryMarker:
				osd = LLSDBinary.parseMap ( stream , encoding );
				break;
			default:
				throw new ParseException ( "Binary LLSD parsing: Unknown type marker." , ( int ) stream.getBytePosition ( ) );
		}
		return osd;
	}

	private static OSD parseArray ( final PushbackInputStream stream , final String encoding ) throws IOException, ParseException {
		final int numElements = Helpers.BytesToInt32B ( OSDParser.consumeBytes ( stream , LLSDBinary.int32Length ) );
		int crrElement = 0;
		final OSDArray osdArray = new OSDArray ( );
		while ( crrElement < numElements ) {
			osdArray.add ( LLSDBinary.parseElement ( stream , encoding ) );
			crrElement++;
		}
		if ( arrayEndBinaryMarker != OSDParser.skipWhiteSpace ( stream ) ) {
			throw new ParseException ( "Binary LLSD parsing: Missing end marker in array." , ( int ) stream.getBytePosition ( ) );
		}

		return osdArray;
	}

	private static OSD parseMap ( final PushbackInputStream stream , final String encoding ) throws IOException, ParseException {
		final int numElements = Helpers.BytesToInt32B ( OSDParser.consumeBytes ( stream , LLSDBinary.int32Length ) );
		int crrElement = 0;
		final OSDMap osdMap = new OSDMap ( );
		while ( crrElement < numElements ) {
			if ( keyBinaryMarker != OSDParser.skipWhiteSpace ( stream ) ) {
				throw new ParseException ( "Binary LLSD parsing: Missing key marker in map." , ( int ) stream.getBytePosition ( ) );
			}
			final int keyLength = Helpers.BytesToInt32B ( OSDParser.consumeBytes ( stream , LLSDBinary.int32Length ) );
			final String key = new String ( OSDParser.consumeBytes ( stream , keyLength ) , encoding );
			osdMap.put ( key , LLSDBinary.parseElement ( stream , encoding ) );
			crrElement++;
		}
		if ( mapEndBinaryMarker != OSDParser.skipWhiteSpace ( stream ) ) {
			throw new ParseException ( "Binary LLSD parsing: Missing end marker in map." , ( int ) stream.getBytePosition ( ) );
		}
		return osdMap;
	}

	/**
	 * Creates an OSD (object structured data) object from a LLSD binary data stream
	 *
	 * @param reader   The reader to read from
	 * @param encoding The encoding to use when reading the reader
	 * @return and OSD object
	 * @throws IOException
	 * @throws ParseException
	 */
	protected OSD unflatten ( Reader reader , String encoding ) throws IOException, ParseException {
		if ( null == encoding )
			encoding = OSD.OSDFormat.contentEncodingDefault ( OSD.OSDFormat.Binary );
		return unflatten ( new ReaderInputStream ( reader , encoding ) , encoding );
	}

	/**
	 * Creates an OSD (object structured data) object from a LLSD binary data stream
	 *
	 * @param stream   The byte stream to read from
	 * @param encoding The encoding to use (not used)
	 * @return and OSD object
	 * @throws IOException
	 * @throws ParseException
	 */
	protected OSD unflatten ( InputStream stream , String encoding ) throws IOException, ParseException {
		PushbackInputStream push = stream instanceof PushbackInputStream ? ( PushbackInputStream ) stream : new PushbackInputStream ( stream );
		int marker = OSDParser.skipWhiteSpace ( push );
		if ( 0 > marker ) {
			return new OSD ( );
		}
		else if ( '<' == marker ) {
			int offset = ( int ) push.getBytePosition ( );
			if ( ! OSDParser.isHeader ( push , llsdBinaryHeader , '>' ) )
				throw new ParseException ( "Failed to decode binary LLSD" , offset );
		}
		else {
			push.unread ( marker );
		}
		if ( null == encoding )
			encoding = OSD.OSDFormat.contentEncodingDefault ( OSD.OSDFormat.Binary );
		return parseElement ( push , encoding );
	}

	/**
	 * Serialize an hierarchical OSD object into an LLSD binary stream
	 *
	 * @param writer        The test writer to write the OSD object into
	 * @param encoding      The encoding to use when streaming the data to the writer
	 * @param data          The hierarchical OSD object to serialize
	 * @param prependHeader Indicates if the format header should be prepended
	 * @throws IOException
	 */
	protected void flatten ( Writer writer , OSD data , boolean prependHeader , String encoding ) throws IOException {
		if ( null == encoding )
			encoding = OSD.OSDFormat.contentEncodingDefault ( OSD.OSDFormat.Binary );
		OutputStream stream = new WriterOutputStream ( writer , encoding );
		flatten ( stream , data , prependHeader , encoding );
		stream.flush ( );
	}

	/**
	 * Serialize an hierarchical OSD object into an LLSD binary stream
	 *
	 * @param stream        The binary byte stream to write the OSD object into
	 * @param encoding      The encoding to use (not used)
	 * @param data          The hierarchical OSD object to serialize
	 * @param prependHeader Indicates if the format header should be prepended
	 * @throws IOException
	 */
	protected void flatten ( OutputStream stream , OSD data , boolean prependHeader , String encoding ) throws IOException {
		if ( prependHeader ) {
			stream.write ( llsdBinaryHead );
			stream.write ( '\n' );
		}
		if ( null == encoding )
			encoding = OSD.OSDFormat.contentEncodingDefault ( OSD.OSDFormat.Binary );
		LLSDBinary.serializeElement ( stream , data , encoding );
	}
}