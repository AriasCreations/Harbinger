package dev.zontreck.harbinger.simulator.llsd;

import dev.zontreck.harbinger.simulator.exceptions.OSDException;
import dev.zontreck.harbinger.simulator.types.enums.OSDType;
import dev.zontreck.harbinger.simulator.types.structureddata.OSD;
import dev.zontreck.harbinger.simulator.types.structureddata.OSDArray;
import dev.zontreck.harbinger.simulator.types.structureddata.OSDMap;
import dev.zontreck.harbinger.utils.SimUtils;
import org.json.JSONObject;
import org.json.XML;

import java.io.*;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import static dev.zontreck.harbinger.simulator.types.enums.OSDType.*;

public class OSDParser {
	private static final int initialBufferSize = 128;
	private static final int int32Length = 4;
	private static final int doubleLength = 8;

	private static final String llsdBinaryHead = "<? llsd/binary ?>";
	private static final String llsdBinaryHead2 = "<?llsd/binary?>";
	private static final byte undefBinaryValue = (byte) '!';
	private static final byte trueBinaryValue = (byte) '1';
	private static final byte falseBinaryValue = (byte) '0';
	private static final byte integerBinaryMarker = (byte) 'i';
	private static final byte realBinaryMarker = (byte) 'r';
	private static final byte uuidBinaryMarker = (byte) 'u';
	private static final byte binaryBinaryMarker = (byte) 'b';
	private static final byte stringBinaryMarker = (byte) 's';
	private static final byte uriBinaryMarker = (byte) 'l';
	private static final byte dateBinaryMarker = (byte) 'd';
	private static final byte arrayBeginBinaryMarker = (byte) '[';
	private static final byte arrayEndBinaryMarker = (byte) ']';
	private static final byte mapBeginBinaryMarker = (byte) '{';
	private static final byte mapEndBinaryMarker = (byte) '}';
	private static final byte keyBinaryMarker = (byte) 'k';

	private static final byte[] llsdBinaryHeadBytes = llsdBinaryHead2.getBytes(StandardCharsets.US_ASCII);

	/// <summary>
	///     Deserializes binary LLSD
	/// </summary>
	/// <param name="binaryData">Serialized data</param>
	/// <returns>OSD containting deserialized data</returns>
	public static OSD DeserializeLLSDBinary(byte[] binaryData) {
		ByteArrayInputStream IS = new ByteArrayInputStream(binaryData);
		try {
			return DeserializeLLSDBinary((Stream) IS);
		} catch (OSDException e) {
			throw new RuntimeException(e);
		}
	}

	/// <summary>
	///     Deserializes binary LLSD
	/// </summary>
	/// <param name="stream">Stream to read the data from</param>
	/// <returns>OSD containting deserialized data</returns>
	public static OSD DeserializeLLSDBinary(Stream stream) throws OSDException {
		if (!(stream instanceof InputStream IS))
			throw new OSDException("Cannot deserialize binary LLSD from unseekable streams");




		try {
			SkipWhiteSpace(stream);

			if (!FindString(stream, llsdBinaryHead) && !FindString(stream, llsdBinaryHead2)) {
				//throw new OSDException("Failed to decode binary LLSD");
			}
			SkipWhiteSpace(stream);
			return ParseLLSDBinaryElement(stream);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/// <summary>
	///     Serializes OSD to binary format. It does no prepend header
	/// </summary>
	/// <param name="osd">OSD to serialize</param>
	/// <returns>Serialized data</returns>
	public static byte[] SerializeLLSDBinary(OSD osd) {
		return SerializeLLSDBinary(osd, true);
	}

	/// <summary>
	///     Serializes OSD to binary format
	/// </summary>
	/// <param name="osd">OSD to serialize</param>
	/// <param name="prependHeader"></param>
	/// <returns>Serialized data</returns>
	public static byte[] SerializeLLSDBinary(OSD osd, boolean prependHeader) {
		ByteArrayOutputStream ms = SerializeLLSDBinaryStream(osd, prependHeader);
		return ms.toByteArray();
	}

	/// <summary>
	///     Serializes OSD to binary format. It does no prepend header
	/// </summary>
	/// <param name="data">OSD to serialize</param>
	/// <returns>Serialized data</returns>
	public static ByteArrayOutputStream SerializeLLSDBinaryStream(OSD data) {
		return SerializeLLSDBinaryStream(data, true);
	}

	/// <summary>
	///     Serializes OSD to binary format
	/// </summary>
	/// <param name="data">OSD to serialize</param>
	/// <param name="prependHeader"></param>
	/// <returns>Serialized data</returns>
	public static ByteArrayOutputStream SerializeLLSDBinaryStream(OSD data, boolean prependHeader) {
		var stream = new ByteArrayOutputStream(initialBufferSize);

		if (prependHeader) {
			stream.write(llsdBinaryHeadBytes, 0, llsdBinaryHeadBytes.length);
			stream.write((byte) '\n');
		}

		try {
			SerializeLLSDBinaryElement(stream, data);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (OSDException e) {
			throw new RuntimeException(e);
		}
		return stream;
	}

	private static void SerializeLLSDBinaryElement(ByteArrayOutputStream stream1, OSD osd) throws IOException, OSDException {
		DataOutputStream stream = new DataOutputStream(stream1);
		switch (osd.Type) {
			case OSUnknown:
				stream.writeByte(undefBinaryValue);
				break;
			case OSBoolean:
				stream.write(osd.AsBinary(), 0, 1);
				break;
			case OSInteger:
				stream.writeByte(integerBinaryMarker);
				stream.write(osd.AsBinary(), 0, int32Length);
				break;
			case OSReal:
				stream.writeByte(realBinaryMarker);
				stream.write(osd.AsBinary(), 0, doubleLength);
				break;
			case OSUUID:
				stream.writeByte(uuidBinaryMarker);
				stream.write(osd.AsBinary(), 0, 16);
				break;
			case OSString:
				stream.writeByte(stringBinaryMarker);
				var rawString = osd.AsBinary();
				var stringLengthNetEnd = SimUtils.IntToBytesBig(rawString.length);
				stream.write(stringLengthNetEnd, 0, int32Length);
				stream.write(rawString, 0, rawString.length);
				break;
			case OSBinary:
				stream.writeByte(binaryBinaryMarker);
				var rawBinary = osd.AsBinary();
				var binaryLengthNetEnd = SimUtils.IntToBytesBig(rawBinary.length);
				stream.write(binaryLengthNetEnd, 0, int32Length);
				stream.write(rawBinary, 0, rawBinary.length);
				break;
			case OSDate:
				stream.writeByte(dateBinaryMarker);
				stream.write(osd.AsBinary(), 0, doubleLength);
				break;
			case OSURI:
				stream.writeByte(uriBinaryMarker);
				var rawURI = osd.AsBinary();
				var uriLengthNetEnd = SimUtils.IntToBytesBig(rawURI.length);
				stream.write(uriLengthNetEnd, 0, int32Length);
				stream.write(rawURI, 0, rawURI.length);
				break;
			case OSArray:
				SerializeLLSDBinaryArray(stream1, (OSDArray) osd);
				break;
			case OSMap:
				SerializeLLSDBinaryMap(stream1, (OSDMap) osd);
				break;
			default:
				throw new OSDException("Binary serialization: Not existing element discovered.");
		}
	}

	private static void SerializeLLSDBinaryArray(ByteArrayOutputStream stream1, OSDArray osdArray) throws IOException {
		DataOutputStream stream = new DataOutputStream(stream1);
		stream.writeByte(arrayBeginBinaryMarker);
		var binaryNumElementsHostEnd = SimUtils.IntToBytesBig(osdArray.Count());
		stream.write(binaryNumElementsHostEnd, 0, int32Length);

		for (OSD osd :
				osdArray) {
			try {
				SerializeLLSDBinaryElement(stream1, osd);
				stream.writeByte(arrayEndBinaryMarker);
			} catch (OSDException e) {
				continue;
			}
		}
	}

	private static void SerializeLLSDBinaryMap(ByteArrayOutputStream stream1, OSDMap osdMap) throws IOException {
		DataOutputStream stream = new DataOutputStream(stream1);
		stream.writeByte(mapBeginBinaryMarker);
		var binaryNumElementsNetEnd = SimUtils.IntToBytesBig(osdMap.Count());
		stream.write(binaryNumElementsNetEnd, 0, int32Length);

		for (Map.Entry<String, OSD> kvp :
				osdMap.entrySet()) {
			stream.writeByte(keyBinaryMarker);
			var binKey = kvp.getKey().getBytes(StandardCharsets.UTF_8);
			var binKeyLen = SimUtils.IntToBytesBig(binKey.length);
			stream.write(binKeyLen, 0, int32Length);
			stream.write(binKey, 0, binKeyLen.length);
			try {
				SerializeLLSDBinaryElement(stream1, kvp.getValue());
			} catch (OSDException e) {
				throw new RuntimeException(e);
			}
		}

		stream.writeByte(mapEndBinaryMarker);
	}

	private static OSD ParseLLSDBinaryElement(Stream stream1) throws IOException, OSDException {
		SkipWhiteSpace(stream1);
		OSD osd;
		DataInputStream stream = new DataInputStream((InputStream) stream1);

		var marker = stream.readByte();
		if (marker < 0)
			throw new OSDException("Binary LLSD parsing: Unexpected end of stream.");

		switch ((byte) marker) {
			case undefBinaryValue:
				osd = new OSD();
				break;
			case trueBinaryValue:
				osd = OSD.FromBoolean(true);
				break;
			case falseBinaryValue:
				osd = OSD.FromBoolean(false);
				break;
			case integerBinaryMarker:
				var integer = SimUtils.BytesToIntBig(ConsumeBytes(stream1, int32Length));
				osd = OSD.FromInteger(integer);
				break;
			case realBinaryMarker:
				var dbl = SimUtils.BytesToDoubleBig(ConsumeBytes(stream1, doubleLength));
				osd = OSD.FromReal(dbl);
				break;
			case uuidBinaryMarker:
				byte[] consum = ConsumeBytes(stream1, 8);
				byte[] p2 = ConsumeBytes(stream1, 8);
				osd = OSD.FromUUID(new UUID(SimUtils.BytesToLong(consum), SimUtils.BytesToLong(p2)));
				break;
			case binaryBinaryMarker:
				var binaryLength = SimUtils.BytesToIntBig(ConsumeBytes(stream1, int32Length));
				osd = OSD.FromBinary(ConsumeBytes(stream1, binaryLength));
				break;
			case stringBinaryMarker:
				var stringLength = SimUtils.BytesToIntBig(ConsumeBytes(stream1, int32Length));
				var ss = Arrays.toString(ConsumeBytes(stream1, stringLength));
				osd = OSD.FromString(ss);
				break;
			case uriBinaryMarker:
				var uriLength = SimUtils.BytesToIntBig(ConsumeBytes(stream1, int32Length));
				var sUri = Arrays.toString(ConsumeBytes(stream1, uriLength));
				URI uri;
				try {
					uri = URI.create(sUri);
				} catch (Exception e)
				{
					throw new OSDException("Binary LLSD parsing: Invalid Uri format detected.");
				}

				osd = OSD.FromUri(uri);
				break;
			case dateBinaryMarker:
				var timestamp = SimUtils.BytesToDouble(ConsumeBytes(stream1, doubleLength));
				Instant n = Instant.now();
				n=n.plusSeconds((long) timestamp);

				osd = OSD.FromInstant(n);
				break;
			case arrayBeginBinaryMarker:
				osd = ParseLLSDBinaryArray(stream1);
				break;
			case mapBeginBinaryMarker:
				osd = ParseLLSDBinaryMap(stream1);
				break;
			default:
				throw new OSDException("Binary LLSD parsing: Unknown type marker.");
		}

		return osd;
	}

	private static OSD ParseLLSDBinaryArray(Stream stream) throws OSDException, IOException {
		var numElements = SimUtils.BytesToIntBig(ConsumeBytes(stream, int32Length));
		var crrElement = 0;
		var osdArray = new OSDArray();
		while (crrElement < numElements) {
			osdArray.Add(ParseLLSDBinaryElement(stream));
			crrElement++;
		}

		if (!FindByte(stream, arrayEndBinaryMarker))
			throw new OSDException("Binary LLSD parsing: Missing end marker in array.");

		return osdArray;
	}

	private static OSD ParseLLSDBinaryMap(Stream stream) throws OSDException, IOException {
		var numElements = SimUtils.BytesToIntBig(ConsumeBytes(stream, int32Length));
		var crrElement = 0;
		var osdMap = new OSDMap();
		while (crrElement < numElements) {
			if (!FindByte(stream, keyBinaryMarker))
				throw new OSDException("Binary LLSD parsing: Missing key marker in map.");
			var keyLength = SimUtils.BytesToIntBig(ConsumeBytes(stream, int32Length));
			var key = Arrays.toString(ConsumeBytes(stream, keyLength));
			osdMap.put(key, ParseLLSDBinaryElement(stream));
			crrElement++;
		}

		if (!FindByte(stream, mapEndBinaryMarker))
			throw new OSDException("Binary LLSD parsing: Missing end marker in map.");

		return osdMap;
	}

	/// <summary>
	/// </summary>
	/// <param name="stream"></param>
	public static void SkipWhiteSpace(Stream stream1) throws IOException {

		DataInputStream stream = new DataInputStream((InputStream) stream1);
		int bt;
		while ((bt = stream.readByte()) > 0 &&
				((byte) bt == ' ' || (byte) bt == '\t' ||
						(byte) bt == '\n' || (byte) bt == '\r')
		) {
		}

	}

	/// <summary>
	/// </summary>
	/// <param name="stream"></param>
	/// <param name="toFind"></param>
	/// <returns></returns>
	public static boolean FindByte(Stream stream, byte toFind) throws IOException {
		if (stream instanceof InputStream IS) {
			DataInputStream dis = new DataInputStream(IS);

			var bt = dis.readByte();
			if (bt < 0)
				return false;
			if ((byte) bt == toFind) return true;


			return false;
		} else return false;
	}

	/// <summary>
	/// </summary>
	/// <param name="stream"></param>
	/// <param name="toFind"></param>
	/// <returns></returns>
	public static boolean FindString(Stream stream, String toFind) throws IOException {
		var lastIndexToFind = toFind.length() - 1;
		var crrIndex = 0;
		var found = true;
		int bt;

		DataInputStream stream1 = new DataInputStream((InputStream) stream);

		while (found &&
				(bt = stream1.readByte()) > 0 &&
				crrIndex <= lastIndexToFind
		)
			if (String.valueOf(toFind.charAt(crrIndex)).equals(((char) bt))) {
				found = true;
				crrIndex++;
			} else {
				found = false;
			}


		return false;
	}

	/// <summary>
	/// </summary>
	/// <param name="stream"></param>
	/// <param name="consumeBytes"></param>
	/// <returns></returns>
	public static byte[] ConsumeBytes(Stream stream, int consumeBytes) throws IOException {
		var bytes = new byte[consumeBytes];
		if (stream instanceof InputStream IS) {
			DataInputStream dis = new DataInputStream(IS);
			return dis.readNBytes(consumeBytes);
		} else return new byte[0];
	}


	private static readonly char[] base64Chars =
			{
					'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O',
					'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd',
					'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's',
					't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7',
					'8', '9', '+', '/'
			};

	/// <summary>
	/// </summary>
	/// <param name="xmlData"></param>
	/// <returns></returns>
	public static OSD DeserializeLLSDXml(byte[] xmlData)
	{
		JSONObject obj = XML.toJSONObject(Arrays.toString(xmlData));
		return DeserializeLLSDXml(obj);
	}

	/// <summary>
	/// </summary>
	/// <param name="xmlData"></param>
	/// <returns></returns>
	public static OSD DeserializeLLSDXml(String xmlData)
	{
		return DeserializeLLSDXml(XML.toJSONObject(xmlData));
	}


	public static OSD DeserializeLLSDXml(JSONObject obj)
	{

	}

	/// <summary>
	/// </summary>
	/// <param name="xmlData"></param>
	/// <returns></returns>
	public static OSD DeserializeLLSDXml(XmlTextReader xmlData)
	{
		xmlData.DtdProcessing = DtdProcessing.Ignore;

		try
		{
			xmlData.Read();
			SkipWhitespace(xmlData);

			xmlData.Read();
			var ret = ParseLLSDXmlElement(xmlData);

			return ret;
		}
		catch
		{
			return new OSD();
		}
	}

	public static byte[] SerializeLLSDXmlToBytes(OSD data, bool formal = false)
	{
		var tmp = OSUTF8Cached.Acquire();
		if (formal)
			tmp.Append(osUTF8Const.XMLformalHeaderllsdstart);
		else
			tmp.Append(osUTF8Const.XMLllsdStart);
		SerializeLLSDXmlElement(tmp, data, formal);
		tmp.Append(osUTF8Const.XMLllsdEnd);

		return OSUTF8Cached.GetArrayAndRelease(tmp);
	}

	public static byte[] SerializeLLSDXmlToBytes(OSD data)
	{
		var tmp = OSUTF8Cached.Acquire();

		tmp.Append(osUTF8Const.XMLllsdStart);
		SerializeLLSDXmlElement(tmp, data, false);
		tmp.Append(osUTF8Const.XMLllsdEnd);

		return OSUTF8Cached.GetArrayAndRelease(tmp);
	}

	public static byte[] SerializeInnerLLSDXmlToBytes(OSD data)
	{
		var tmp = OSUTF8Cached.Acquire();
		SerializeLLSDXmlElement(tmp, data, false);

		return OSUTF8Cached.GetArrayAndRelease(tmp);
	}

	public static byte[] SerializeLLSDXmlBytes(OSD data, bool formal = false)
	{
		return SerializeLLSDXmlToBytes(data, formal);
	}

	/// <summary>
	/// </summary>
	/// <param name="data"></param>
	/// <returns></returns>
	public static string SerializeLLSDXmlString(OSD data, bool formal = false)
	{
		var sb = osStringBuilderCache.Acquire();
		if (formal)
			sb.Append("<?xml version=\"1.0\" encoding=\"utf-8\"?>");

		sb.Append("<llsd>");
		SerializeLLSDXmlElement(sb, data, formal);
		sb.Append("</llsd>");

		return osStringBuilderCache.GetStringAndRelease(sb);
	}

	public static string SerializeLLSDInnerXmlString(OSD data, bool formal = false)
	{
		var sb = osStringBuilderCache.Acquire();
		SerializeLLSDXmlElement(sb, data, formal);

		return osStringBuilderCache.GetStringAndRelease(sb);
	}

	/// <summary>
	/// </summary>
	/// <param name="sb"></param>
	/// <param name="data"></param>
	public static void SerializeLLSDXmlElement(StringBuilder sb, OSD data, bool formal)
	{
		switch (data.Type)
		{
			case OSDType.Unknown:
				sb.Append("<undef />");
				break;
			case OSDType.Boolean:
				if (data.AsBoolean())
					sb.Append("<boolean>1</boolean>");
				else
					sb.Append("<boolean>0</boolean>");
				break;
			case OSDType.Integer:
				sb.Append("<integer>");
				sb.Append(data.AsString());
				sb.Append("</integer>");
				break;
			case OSDType.Real:
				sb.Append("<real>");
				sb.Append(data.AsString());
				sb.Append("</real>");
				break;
			case OSDType.String:
				sb.Append("<string>");
				EscapeToXML(data.AsString(), sb);
				sb.Append("</string>");
				break;
			case OSDType.UUID:
				sb.Append("<uuid>");
				sb.Append(data.AsString());
				sb.Append("</uuid>");
				break;
			case OSDType.Date:
				sb.Append("<date>");
				sb.Append(data.AsString());
				sb.Append("</date>");
				break;
			case OSDType.URI:
				sb.Append("<uri>");
				sb.Append(data.AsString());
				sb.Append("</uri>");
				break;
			case OSDType.Binary:
				if (formal)
					sb.Append("<binary encoding=\"base64\">");
				else
					sb.Append("<binary>");
				base64Encode(data.AsBinary(), sb);
				sb.Append("</binary>");
				break;
			case OSDType.Map:
				var map = (OSDMap)data;
				sb.Append("<map>");
				foreach (KeyValuePair<string, OSD> kvp in map)
			{
				sb.Append("<key>");
				sb.Append(kvp.Key);
				sb.Append("</key>");

				SerializeLLSDXmlElement(sb, kvp.Value, formal);
			}

			sb.Append("</map>");
			break;
			case OSDType.Array:
				var array = (OSDArray)data;
				sb.Append("<array>");
				for (var i = 0; i < array.Count; i++) SerializeLLSDXmlElement(sb, array[i], formal);
				sb.Append("</array>");
				break;
			case OSDType.LLSDxml:
				sb.Append(data.AsString());
				break;
		}
	}

	public static void EscapeToXML(string s, StringBuilder sb)
	{
		char c;
		for (var i = 0; i < s.Length; ++i)
		{
			c = s[i];
			switch (c)
			{
				case '<':
					sb.Append("&lt;");
					break;
				case '>':
					sb.Append("&gt;");
					break;
				case '&':
					sb.Append("&amp;");
					break;
				case '"':
					sb.Append("&quot;");
					break;
				case '\\':
					sb.Append("&apos;");
					break;
				default:
					sb.Append(c);
					break;
			}
		}
	}

	public static void EscapeASCIIToXML(osUTF8 ms, string s)
	{
		char c;
		for (var i = 0; i < s.Length; i++)
		{
			c = s[i];
			switch (c)
			{
				case '<':
					ms.Append(osUTF8Const.XMLamp_lt);
					break;
				case '>':
					ms.Append(osUTF8Const.XMLamp_gt);
					break;
				case '&':
					ms.Append(osUTF8Const.XMLamp);
					break;
				case '"':
					ms.Append(osUTF8Const.XMLamp_quot);
					break;
				case '\\':
					ms.Append(osUTF8Const.XMLamp_apos);
					break;
				default:
					ms.AppendASCII(c);
					break;
			}
		}
	}

	public static void EscapeToXML(osUTF8 ms, string s)
	{
		char c;
		for (var i = 0; i < s.Length; i++)
		{
			c = s[i];
			switch (c)
			{
				case '<':
					ms.Append(osUTF8Const.XMLamp_lt);
					break;
				case '>':
					ms.Append(osUTF8Const.XMLamp_gt);
					break;
				case '&':
					ms.Append(osUTF8Const.XMLamp);
					break;
				case '"':
					ms.Append(osUTF8Const.XMLamp_quot);
					break;
				case '\\':
					ms.Append(osUTF8Const.XMLamp_apos);
					break;
				default:
					ms.AppendCharBytes(c, ref s, ref i);
					break;
			}
		}
	}

	public static void EscapeToXML(osUTF8 ms, osUTF8 s)
	{
		byte c;
		for (var i = 0; i < s.Length; i++)
		{
			c = s[i];
			switch (c)
			{
				case (byte)'<':
					ms.Append(osUTF8Const.XMLamp_lt);
					break;
				case (byte)'>':
					ms.Append(osUTF8Const.XMLamp_gt);
					break;
				case (byte)'&':
					ms.Append(osUTF8Const.XMLamp);
					break;
				case (byte)'"':
					ms.Append(osUTF8Const.XMLamp_quot);
					break;
				case (byte)'\\':
					ms.Append(osUTF8Const.XMLamp_apos);
					break;
				default:
					ms.Append(c);
					break;
			}
		}
	}

	public static void SerializeLLSDXmlElement(osUTF8 mb, OSD data, bool formal)
	{
		switch (data.Type)
		{
			case OSDType.Unknown:
				mb.Append(osUTF8Const.XMLundef);
				break;
			case OSDType.Boolean:
				if (data.AsBoolean())
					mb.Append(osUTF8Const.XMLfullbooleanOne);
				else
					mb.Append(osUTF8Const.XMLfullbooleanZero);
				break;
			case OSDType.Integer:
				mb.Append(osUTF8Const.XMLintegerStart);
				mb.AppendInt(data.AsInteger());
				mb.Append(osUTF8Const.XMLintegerEnd);
				break;
			case OSDType.Real:
				mb.Append(osUTF8Const.XMLrealStart);
				mb.AppendASCII(data.ToString());
				mb.Append(osUTF8Const.XMLrealEnd);
				break;
			case OSDType.String:
				mb.Append(osUTF8Const.XMLstringStart);
				EscapeToXML(mb, data);
				mb.Append(osUTF8Const.XMLstringEnd);
				break;
			case OSDType.UUID:
				mb.Append(osUTF8Const.XMLuuidStart);
				mb.AppendUUID(data.AsUUID());
				mb.Append(osUTF8Const.XMLuuidEnd);
				break;
			case OSDType.Date:
				mb.Append(osUTF8Const.XMLdateStart);
				mb.AppendASCII(data.ToString());
				mb.Append(osUTF8Const.XMLdateEnd);
				break;
			case OSDType.URI:
				mb.Append(osUTF8Const.XMLuriStart);
				EscapeToXML(mb, data.ToString());
				mb.Append(osUTF8Const.XMLuriEnd);
				break;
			case OSDType.Binary:
				if (formal)
					mb.Append(osUTF8Const.XMLformalBinaryStart);
				else
					mb.Append(osUTF8Const.XMLbinaryStart);
				base64Encode(data.AsBinary(), mb);
				mb.Append(osUTF8Const.XMLbinaryEnd);
				break;
			case OSDType.Map:
				mb.Append(osUTF8Const.XMLmapStart);
				foreach (KeyValuePair<string, OSD> kvp in (OSDMap)data)
			{
				mb.Append(osUTF8Const.XMLkeyStart);
				mb.Append(kvp.Key);
				mb.Append(osUTF8Const.XMLkeyEnd);

				SerializeLLSDXmlElement(mb, kvp.Value, formal);
			}

			mb.Append(osUTF8Const.XMLmapEnd);
			break;
			case OSDType.Array:
				var array = (OSDArray)data;
				mb.Append(osUTF8Const.XMLarrayStart);
				for (var i = 0; i < array.Count; i++) SerializeLLSDXmlElement(mb, array[i], formal);
				mb.Append(osUTF8Const.XMLarrayEnd);
				break;
			case OSDType.LLSDxml:
				mb.Append(data.AsString());
				break;
		}
	}

	public static unsafe void base64Encode(byte[] data, osUTF8 mb)
	{
		var lenMod3 = data.Length % 3;
		var len = data.Length - lenMod3;

		mb.CheckCapacity(4 * data.Length / 3);

		fixed (byte* d = data, b64 = osUTF8Const.base64Bytes)
		{
			var i = 0;
			while (i < len)
			{
				mb.Append(b64[d[i] >> 2]);
				mb.Append(b64[((d[i] & 0x03) << 4) | ((d[i + 1] & 0xf0) >> 4)]);
				mb.Append(b64[((d[i + 1] & 0x0f) << 2) | ((d[i + 2] & 0xc0) >> 6)]);
				mb.Append(b64[d[i + 2] & 0x3f]);
				i += 3;
			}

			switch (lenMod3)
			{
				case 2:
				{
					i = len;
					mb.Append(b64[d[i] >> 2]);
					mb.Append(b64[((d[i] & 0x03) << 4) | ((d[i + 1] & 0xf0) >> 4)]);
					mb.Append(b64[(d[i + 1] & 0x0f) << 2]);
					mb.Append((byte)'=');
					break;
				}
				case 1:
				{
					i = len;
					mb.Append(b64[d[i] >> 2]);
					mb.Append(b64[(d[i] & 0x03) << 4]);
					mb.Append((byte)'=');
					mb.Append((byte)'=');
					break;
				}
			}
		}
	}

	public static unsafe void base64Encode(byte[] data, int start, int lenght, osUTF8 mb)
	{
		var lenMod3 = lenght % 3;
		var len = lenght - lenMod3;

		fixed (byte* d = &data[start], b64 = osUTF8Const.base64Bytes)
		{
			var i = 0;
			while (i < len)
			{
				mb.Append(b64[d[i] >> 2]);
				mb.Append(b64[((d[i] & 0x03) << 4) | ((d[i + 1] & 0xf0) >> 4)]);
				mb.Append(b64[((d[i + 1] & 0x0f) << 2) | ((d[i + 2] & 0xc0) >> 6)]);
				mb.Append(b64[d[i + 2] & 0x3f]);
				i += 3;
			}

			switch (lenMod3)
			{
				case 2:
				{
					i = len;
					mb.Append(b64[d[i] >> 2]);
					mb.Append(b64[((d[i] & 0x03) << 4) | ((d[i + 1] & 0xf0) >> 4)]);
					mb.Append(b64[(d[i + 1] & 0x0f) << 2]);
					mb.Append((byte)'=');
					break;
				}
				case 1:
				{
					i = len;
					mb.Append(b64[d[i] >> 2]);
					mb.Append(b64[(d[i] & 0x03) << 4]);
					mb.Append((byte)'=');
					mb.Append((byte)'=');
					break;
				}
			}
		}
	}

	public static unsafe void base64Encode(byte[] data, StringBuilder sb)
	{
		var lenMod3 = data.Length % 3;
		var len = data.Length - lenMod3;

		fixed (byte* d = data)
		{
			fixed (char* b64 = base64Chars)
			{
				var i = 0;
				while (i < len)
				{
					sb.Append(b64[d[i] >> 2]);
					sb.Append(b64[((d[i] & 0x03) << 4) | ((d[i + 1] & 0xf0) >> 4)]);
					sb.Append(b64[((d[i + 1] & 0x0f) << 2) | ((d[i + 2] & 0xc0) >> 6)]);
					sb.Append(b64[d[i + 2] & 0x3f]);
					i += 3;
				}

				switch (lenMod3)
				{
					case 2:
					{
						i = len;
						sb.Append(b64[d[i] >> 2]);
						sb.Append(b64[((d[i] & 0x03) << 4) | ((d[i + 1] & 0xf0) >> 4)]);
						sb.Append(b64[(d[i + 1] & 0x0f) << 2]);
						sb.Append('=');
						break;
					}
					case 1:
					{
						i = len;
						sb.Append(b64[d[i] >> 2]);
						sb.Append(b64[(d[i] & 0x03) << 4]);
						sb.Append("==");
						break;
					}
				}
			}
		}
	}


	/// <summary>
	/// </summary>
	/// <param name="reader"></param>
	/// <returns></returns>
	private static OSD ParseLLSDXmlElement(XmlTextReader reader)
	{
		SkipWhitespace(reader);

		if (reader.NodeType != XmlNodeType.Element)
			throw new OSDException("Expected an element");

		var type = reader.LocalName;
		OSD ret;

		switch (type)
		{
			case "undef":
				if (reader.IsEmptyElement)
				{
					reader.Read();
					return new OSD();
				}

				reader.Read();
				SkipWhitespace(reader);
				ret = new OSD();
				break;
			case "boolean":
				if (reader.IsEmptyElement)
				{
					reader.Read();
					return OSD.FromBoolean(false);
				}

				if (reader.Read())
				{
					var s = reader.ReadString().Trim();

					if (!string.IsNullOrEmpty(s) && (s == "true" || s == "1"))
					{
						ret = OSD.FromBoolean(true);
						break;
					}
				}

				ret = OSD.FromBoolean(false);
				break;
			case "integer":
				if (reader.IsEmptyElement)
				{
					reader.Read();
					return OSD.FromInteger(0);
				}

				if (reader.Read())
				{
					int.TryParse(reader.ReadString().Trim(), out var value);
					ret = OSD.FromInteger(value);
					break;
				}

				ret = OSD.FromInteger(0);
				break;
			case "real":
				if (reader.IsEmptyElement)
				{
					reader.Read();
					return OSD.FromReal(0d);
				}

				if (reader.Read())
				{
					double value;
					var str = reader.ReadString().Trim().ToLower();

					if (str == "nan")
						value = double.NaN;
					else
						Utils.TryParseDouble(str, out value);

					ret = OSD.FromReal(value);
					break;
				}

				ret = OSD.FromReal(0d);
				break;
			case "uuid":
				if (reader.IsEmptyElement)
				{
					reader.Read();
					return OSD.FromUUID(UUID.Zero);
				}

				if (reader.Read())
				{
					UUID.TryParse(reader.ReadString().AsSpan(), out var value);
					ret = OSD.FromUUID(value);
					break;
				}

				ret = OSD.FromUUID(UUID.Zero);
				break;
			case "date":
				if (reader.IsEmptyElement)
				{
					reader.Read();
					return OSD.FromDate(Utils.Epoch);
				}

				if (reader.Read())
				{
					if (DateTime.TryParse(reader.ReadString().Trim(), out var value))
						ret = OSD.FromDate(value);
					else
						ret = OSD.FromDate(Utils.Epoch);
					break;
				}

				ret = OSD.FromDate(Utils.Epoch);
				break;
			case "string":
				if (reader.IsEmptyElement)
				{
					reader.Read();
					return OSD.FromString(string.Empty);
				}

				if (reader.Read())
				{
					ret = OSD.FromString(reader.ReadString());
					break;
				}

				ret = OSD.FromString(string.Empty);
				break;
			case "binary":
				if (reader.IsEmptyElement)
				{
					reader.Read();
					return OSD.FromBinary(Array.Empty<byte>());
				}

				if (reader.GetAttribute("encoding") != null && reader.GetAttribute("encoding") != "base64")
					throw new OSDException("Unsupported binary encoding: " + reader.GetAttribute("encoding"));

				if (reader.Read())
					try
					{
						ret = OSD.FromBinary(Convert.FromBase64String(reader.ReadString().Trim()));
						break;
					}
					catch (FormatException ex)
					{
						throw new OSDException("Binary decoding exception: " + ex.Message);
					}

				ret = OSD.FromBinary(Array.Empty<byte>());
				break;
			case "uri":
				if (reader.IsEmptyElement)
				{
					reader.Read();
					return OSD.FromUri(new Uri(string.Empty, UriKind.RelativeOrAbsolute));
				}

				if (reader.Read())
				{
					ret = OSD.FromUri(new Uri(reader.ReadString(), UriKind.RelativeOrAbsolute));
					break;
				}

				ret = OSD.FromUri(new Uri(string.Empty, UriKind.RelativeOrAbsolute));
				break;
			case "map":
				return ParseLLSDXmlMap(reader);
			case "array":
				return ParseLLSDXmlArray(reader);
			default:
				reader.Read();
				ret = null;
				break;
		}

		if (reader.NodeType != XmlNodeType.EndElement || reader.LocalName != type)
			throw new OSDException("Expected </" + type + ">");

		reader.Read();
		return ret;
	}

	private static OSDMap ParseLLSDXmlMap(XmlTextReader reader)
	{
		if (reader.NodeType != XmlNodeType.Element || reader.LocalName != "map")
			throw new NotImplementedException("Expected <map>");

		var map = new OSDMap();

		if (reader.IsEmptyElement)
		{
			reader.Read();
			return map;
		}

		if (reader.Read())
			while (true)
			{
				SkipWhitespace(reader);

				if (reader.NodeType == XmlNodeType.EndElement && reader.LocalName == "map")
				{
					reader.Read();
					break;
				}

				if (reader.NodeType != XmlNodeType.Element || reader.LocalName != "key")
					throw new OSDException("Expected <key>");

				var key = reader.ReadString();

				if (reader.NodeType != XmlNodeType.EndElement || reader.LocalName != "key")
					throw new OSDException("Expected </key>");

				if (reader.Read())
					map[key] = ParseLLSDXmlElement(reader);
				else
					throw new OSDException("Failed to parse a value for key " + key);
			}

		return map;
	}

	private static OSDArray ParseLLSDXmlArray(XmlTextReader reader)
	{
		if (reader.NodeType != XmlNodeType.Element || reader.LocalName != "array")
			throw new OSDException("Expected <array>");

		var array = new OSDArray();

		if (reader.IsEmptyElement)
		{
			reader.Read();
			return array;
		}

		if (reader.Read())
			while (true)
			{
				SkipWhitespace(reader);

				if (reader.NodeType == XmlNodeType.EndElement && reader.LocalName == "array")
				{
					reader.Read();
					break;
				}

				array.Add(ParseLLSDXmlElement(reader));
			}

		return array;
	}

	private static void SkipWhitespace(XML reader)
	{
		XML.
		while (
				reader.NodeType == XmlNodeType.Comment ||
						reader.NodeType == XmlNodeType.Whitespace ||
						reader.NodeType == XmlNodeType.SignificantWhitespace ||
						reader.NodeType == XmlNodeType.XmlDeclaration)
			reader.Read();
	}


	public static OSD DeserializeJson(Stream json)
	{
		using (var streamReader = new StreamReader(json))
		{
			var reader = new JsonReader(streamReader);
			return DeserializeJson(JsonMapper.ToObject(reader));
		}
	}

	public static OSD DeserializeJson(byte[] data)
	{
		return DeserializeJson(JsonMapper.ToObject(Encoding.UTF8.GetString(data)));
	}

	public static OSD DeserializeJson(string json)
	{
		return DeserializeJson(JsonMapper.ToObject(json));
	}

	public static OSD DeserializeJson(JsonData json)
	{
		if (json == null) return new OSD();

		switch (json.GetJsonType())
		{
			case JsonType.Boolean:
				return OSD.FromBoolean((bool)json);
			case JsonType.Int:
				return OSD.FromInteger((int)json);
			case JsonType.Long:
				return OSD.FromLong((long)json);
			case JsonType.Double:
				return OSD.FromReal((double)json);
			case JsonType.String:
				var str = (string)json;
				if (string.IsNullOrEmpty(str))
					return new OSD();
				return OSD.FromString(str);
			case JsonType.Array:
				var array = new OSDArray(json.Count);
				for (var i = 0; i < json.Count; i++)
					array.Add(DeserializeJson(json[i]));
				return array;
			case JsonType.Object:
				var map = new OSDMap(json.Count);
				var e = ((IOrderedDictionary)json).GetEnumerator();
				while (e.MoveNext())
					map.Add((string)e.Key, DeserializeJson((JsonData)e.Value));
				return map;
			case JsonType.None:
			default:
				return new OSD();
		}
	}

	/*
	public static string SerializeJsonString(OSD osd)
	{
		return SerializeJson(osd, false).ToJson();
	}

	public static byte[] SerializeJsonToBytes(OSD osd)
	{
		return Encoding.UTF8.GetBytes(SerializeJson(osd, false).ToJson());
	}

	public static string SerializeJsonString(OSD osd, bool preserveDefaults)
	{
		return SerializeJson(osd, preserveDefaults).ToJson();
	}
	*/
	public static void SerializeJsonString(OSD osd, bool preserveDefaults, ref JsonWriter writer)
	{
		SerializeJson(osd, preserveDefaults).ToJson(writer);
	}

	public static JsonData SerializeJson(OSD osd, bool preserveDefaults)
	{
		switch (osd.Type)
		{
			case OSDType.Boolean:
				return new JsonData(osd.AsBoolean());
			case OSDType.Integer:
				return new JsonData(osd.AsInteger());
			case OSDType.Real:
				return new JsonData(osd.AsReal());
			case OSDType.String:
			case OSDType.Date:
			case OSDType.URI:
			case OSDType.UUID:
			case OSDType.OSDUTF8:
			case OSDType.LLSDxml:
				return new JsonData(osd.AsString());
			case OSDType.Binary:
				var binary = osd.AsBinary();
				var jsonbinarray = new JsonData();
				jsonbinarray.SetJsonType(JsonType.Array);
				for (var i = 0; i < binary.Length; i++)
					jsonbinarray.Add(new JsonData(binary[i]));
				return jsonbinarray;
			case OSDType.Array:
				var jsonarray = new JsonData();
				jsonarray.SetJsonType(JsonType.Array);
				var array = (OSDArray)osd;
				for (var i = 0; i < array.Count; i++)
					jsonarray.Add(SerializeJson(array[i], preserveDefaults));
				return jsonarray;
			case OSDType.Map:
				var jsonmap = new JsonData();
				jsonmap.SetJsonType(JsonType.Object);
				var map = (OSDMap)osd;
				foreach (KeyValuePair<string, OSD> kvp in map)
			{
				var data = preserveDefaults ? SerializeJson(kvp.Value, true) : SerializeJsonNoDefaults(kvp.Value);
				if (data != null)
					jsonmap[kvp.Key] = data;
			}

			return jsonmap;
			case OSDType.Unknown:
			default:
				return new JsonData(null);
		}
	}

	private static JsonData SerializeJsonNoDefaults(OSD osd)
	{
		switch (osd.Type)
		{
			case OSDType.Boolean:
				var b = osd.AsBoolean();
				if (!b)
					return null;

				return new JsonData(b);
			case OSDType.Integer:
				var v = osd.AsInteger();
				if (v == 0)
					return null;

				return new JsonData(v);
			case OSDType.Real:
				var d = osd.AsReal();
				if (d == 0.0d)
					return null;

				return new JsonData(d);
			case OSDType.String:
			case OSDType.Date:
			case OSDType.URI:
			case OSDType.OSDUTF8:
			case OSDType.LLSDxml:
				var str = osd.AsString();
				if (string.IsNullOrEmpty(str))
					return null;

				return new JsonData(str);
			case OSDType.UUID:
				var uuid = osd.AsUUID();
				if (uuid.IsZero())
					return null;

				return new JsonData(uuid.ToString());
			case OSDType.Binary:
				var binary = osd.AsBinary();
				if (binary.Length == 0)
					return null;

				var jsonbinarray = new JsonData();
				jsonbinarray.SetJsonType(JsonType.Array);
				for (var i = 0; i < binary.Length; i++)
					jsonbinarray.Add(new JsonData(binary[i]));
				return jsonbinarray;
			case OSDType.Array:
				var jsonarray = new JsonData();
				jsonarray.SetJsonType(JsonType.Array);
				var array = (OSDArray)osd;
				for (var i = 0; i < array.Count; i++)
					jsonarray.Add(SerializeJson(array[i], false));
				return jsonarray;
			case OSDType.Map:
				var jsonmap = new JsonData();
				jsonmap.SetJsonType(JsonType.Object);
				var map = (OSDMap)osd;
				foreach (KeyValuePair<string, OSD> kvp in map)
			{
				var data = SerializeJsonNoDefaults(kvp.Value);
				if (data != null)
					jsonmap[kvp.Key] = data;
			}

			return jsonmap;
			case OSDType.Unknown:
			default:
				return null;
		}
	}

	public static string SerializeJsonString(OSD osd)
	{
		var sb = OSUTF8Cached.Acquire();
		SerializeJson(osd, sb, false);
		return OSUTF8Cached.GetStringAndRelease(sb);
	}

	public static byte[] SerializeJsonToBytes(OSD osd)
	{
		var sb = OSUTF8Cached.Acquire();
		SerializeJson(osd, sb, false);
		return OSUTF8Cached.GetArrayAndRelease(sb);
	}

	public static string SerializeJsonString(OSD osd, bool preserveDefaults)
	{
		var sb = OSUTF8Cached.Acquire();
		SerializeJson(osd, sb, preserveDefaults);
		return OSUTF8Cached.GetStringAndRelease(sb);
	}

	public static void SerializeJson(OSD osd, osUTF8 sb, bool preserveDefaults)
	{
		int i;
		switch (osd.Type)
		{
			case OSDType.Boolean:
				sb.Append(osd.AsBoolean() ? osUTF8Const.OSUTF8true : osUTF8Const.OSUTF8false);
				break;
			case OSDType.Integer:
				sb.AppendInt(osd.AsInteger());
				break;
			case OSDType.Real:
				var str = Convert.ToString(osd.AsReal(), NumberFormatInfo.InvariantInfo);
				sb.AppendASCII(str);

				if (str.IndexOfAny(new[] { '.', 'E' }) == -1)
			sb.AppendASCII(".0");
			break;
			case OSDType.String:
			case OSDType.URI:
			case OSDType.LLSDxml:
				appendJsonString(osd.AsString(), sb);
				break;
			case OSDType.OSDUTF8:
				var ou8 = ((OSDUTF8)osd).value;
				appendJsonOSUTF8(ou8, sb);
				break;
			case OSDType.UUID:
				sb.AppendASCII('"');
				sb.AppendUUID(osd.AsUUID());
				sb.AppendASCII('"');
				break;
			case OSDType.Date:
				appendJsonString(osd.AsString(), sb);
				break;
			case OSDType.Binary:
				var binary = osd.AsBinary();
				i = 0;
				sb.AppendASCII('[');
				while (i < binary.Length - 1)
				{
					sb.AppendInt(binary[i++]);
					sb.AppendASCII(',');
				}

				if (i < binary.Length)
					sb.AppendInt(binary[i]);
				sb.AppendASCII(']');
				break;
			case OSDType.Array:
				sb.AppendASCII('[');
				var array = (OSDArray)osd;
				i = 0;
				while (i < array.Count - 1)
				{
					SerializeJson(array[i++], sb, preserveDefaults);
					sb.AppendASCII(',');
				}

				if (i < array.Count)
					SerializeJson(array[i], sb, preserveDefaults);
				sb.AppendASCII(']');
				break;
			case OSDType.Map:
				sb.AppendASCII('{');
				var map = (OSDMap)osd;
				i = 0;
				foreach (KeyValuePair<string, OSD> kvp in map)
				if (preserveDefaults)
				{
					if (i++ > 0)
						sb.AppendASCII(',');
					appendJsonString(kvp.Key, sb);
					sb.AppendASCII(':');
					SerializeJson(kvp.Value, sb, true);
				}
				else
				{
					SerializeJsonMapNoDefaults(kvp.Key, kvp.Value, ref i, sb);
				}

				sb.AppendASCII('}');
				break;
			case OSDType.Unknown:
			default:
				sb.Append(osUTF8Const.OSUTF8null);
				break;
		}
	}

	public static void SerializeJsonMapNoDefaults(string name, OSD osd, ref int mapcont, osUTF8 sb)
	{
		int i;
		switch (osd.Type)
		{
			case OSDType.Boolean:
				var ob = osd.AsBoolean();
				if (ob)
				{
					if (mapcont++ > 0)
						sb.AppendASCII(',');
					appendJsonString(name, sb);
					sb.AppendASCII(':');
					sb.Append(ob ? osUTF8Const.OSUTF8true : osUTF8Const.OSUTF8false);
				}

				break;
			case OSDType.Integer:
				var oi = osd.AsInteger();
				if (oi != 0)
				{
					if (mapcont++ > 0)
						sb.AppendASCII(',');
					appendJsonString(name, sb);
					sb.AppendASCII(':');
					sb.AppendInt(oi);
				}

				break;
			case OSDType.Real:
				var od = osd.AsReal();
				if (od != 0)
				{
					if (mapcont++ > 0)
						sb.AppendASCII(',');
					appendJsonString(name, sb);
					sb.AppendASCII(':');
					var str = Convert.ToString(od, NumberFormatInfo.InvariantInfo);
					sb.AppendASCII(str);

					if (str.IndexOfAny(new[] { '.', 'E' }) == -1)
					sb.AppendASCII(".0");
				}

				break;
			case OSDType.String:
			case OSDType.URI:
			case OSDType.LLSDxml:
				var ostr = osd.AsString();
				if (!string.IsNullOrEmpty(ostr))
				{
					if (mapcont++ > 0)
						sb.AppendASCII(',');
					appendJsonString(name, sb);
					sb.AppendASCII(':');
					appendJsonString(ostr, sb);
				}

				break;
			case OSDType.OSDUTF8:
				var ou8 = ((OSDUTF8)osd).value;
				if (ou8 != null && ou8.Length > 0)
				{
					if (mapcont++ > 0)
						sb.AppendASCII(',');
					appendJsonString(name, sb);
					sb.AppendASCII(':');
					appendJsonOSUTF8(ou8, sb);
				}

				break;
			case OSDType.UUID:
				var ou = osd.AsUUID();
				if (ou.IsNotZero())
				{
					if (mapcont++ > 0)
						sb.AppendASCII(',');
					appendJsonString(name, sb);
					sb.AppendASCII(":\"");
					sb.AppendUUID(ou);
					sb.AppendASCII('"');
				}

				break;
			case OSDType.Date:
				if (mapcont++ > 0)
					sb.AppendASCII(',');
				appendJsonString(name, sb);
				sb.AppendASCII(':');
				appendJsonString(osd.AsString(), sb);
				break;
			case OSDType.Binary:
				var binary = osd.AsBinary();
				if (mapcont++ > 0)
					sb.AppendASCII(',');
				appendJsonString(name, sb);
				sb.AppendASCII(":[");
				if (binary != null && binary.Length > 0)
				{
					i = 0;
					while (i < binary.Length - 1)
					{
						sb.AppendInt(binary[i++]);
						sb.AppendASCII(',');
					}

					if (i < binary.Length)
						sb.AppendInt(binary[i]);
				}

				sb.AppendASCII(']');
				break;
			case OSDType.Array:
				if (mapcont++ > 0)
					sb.AppendASCII(",");
				appendJsonString(name, sb);
				sb.AppendASCII(":[");

				var array = (OSDArray)osd;
				if (array != null && array.Count > 0)
				{
					i = 0;
					while (i < array.Count - 1)
					{
						SerializeJson(array[i++], sb, false);
						sb.AppendASCII(',');
					}

					if (i < array.Count)
						SerializeJson(array[i], sb, false);
				}

				sb.AppendASCII(']');
				break;
			case OSDType.Map:
				var map = (OSDMap)osd;
				if (map != null && map.Count > 0)
				{
					if (mapcont++ > 0)
						sb.AppendASCII(',');
					appendJsonString(name, sb);
					sb.AppendASCII(":{");

					i = 0;
					foreach (KeyValuePair<string, OSD> kvp in map)
					SerializeJsonMapNoDefaults(kvp.Key, kvp.Value, ref i, sb);
					sb.AppendASCII('}');
				}

				break;
			case OSDType.Unknown:
			default:
				break;
		}
	}

	public static void appendJsonString(string str, osUTF8 sb)
	{
		sb.AppendASCII('"');
		for (var i = 0; i < str.Length; i++)
		{
			var c = str[i];
			switch (c)
			{
				case '\n':
					sb.AppendASCII("\\n");
					break;

				case '\r':
					sb.AppendASCII("\\r");
					break;

				case '\t':
					sb.AppendASCII("\\t");
					break;

				case '"':
					//case '/':
				case '\\':
					sb.AppendASCII('\\');
					sb.AppendASCII(c);
					break;

				case '\f':
					sb.AppendASCII("\\f");
					break;

				case '\b':
					sb.AppendASCII("\\b");
					break;

				default:
					// Default, turn into a \uXXXX sequence
					if (c >= 32 && c <= 126)
					{
						sb.AppendASCII(c);
					}
					else
					{
						sb.AppendASCII("\\u");
						sb.Append(Utils.NibbleToHexUpper((byte)(c >> 12)));
						sb.Append(Utils.NibbleToHexUpper((byte)(c >> 8)));
						sb.Append(Utils.NibbleToHexUpper((byte)(c >> 4)));
						sb.Append(Utils.NibbleToHexUpper((byte)c));
					}

					break;
			}
		}

		sb.AppendASCII('"');
	}

	public static void appendJsonOSUTF8(osUTF8 str, osUTF8 sb)
	{
		int code;
		sb.AppendASCII('"');
		for (var i = 0; i < str.Length; i++)
		{
			var c = str[i];
			if (c < 0x80)
				switch (c)
				{
					case (byte)'\n':
						sb.AppendASCII("\\n");
						break;

					case (byte)'\r':
						sb.AppendASCII("\\r");
						break;

					case (byte)'\t':
						sb.AppendASCII("\\t");
						break;

					case (byte)'"':
					case (byte)'/':
					case (byte)'\\':
						sb.AppendASCII('\\');
						sb.Append(c);
						break;

					case (byte)'\f':
						sb.AppendASCII("\\f");
						break;

					case (byte)'\b':
						sb.AppendASCII("\\b");
						break;

					default:
						// Default, turn into a \uXXXX sequence
						if (c >= 32 && c <= 126)
						{
							sb.Append(c);
						}
						else
						{
							sb.AppendASCII("\\u00");
							sb.Append(Utils.NibbleToHexUpper((byte)(c >> 4)));
							sb.Append(Utils.NibbleToHexUpper(c));
						}

						break;
				}

			if (c < 0xc0)
				continue; // invalid
			if (c < 0xe0)
			{
				// 2 bytes
				if (i + 1 >= str.Length)
					return;

				code = (c & 0x1f) << 6;
				code |= str[++i] & 0x3f;

				sb.AppendASCII("\\u0");
				sb.Append(Utils.NibbleToHexUpper((byte)(code >> 8)));
				sb.Append(Utils.NibbleToHexUpper((byte)(code >> 4)));
				sb.Append(Utils.NibbleToHexUpper((byte)code));
			}
			else if (c < 0xF0)
			{
				// 3 bytes
				if (i + 2 >= str.Length)
					return;

				// 1110aaaa 10bbbbcc 10ccdddd
				sb.AppendASCII("\\u");
				sb.Append(Utils.NibbleToHexUpper(c));
				c = str[++i];
				sb.Append(Utils.NibbleToHexUpper((byte)(c >> 2)));
				code = (c & 3) << 2;
				c = str[++i];
				code |= (c & 0x30) >> 4;
				sb.Append(Utils.NibbleToHexUpper((byte)code));
				sb.Append(Utils.NibbleToHexUpper(c));
			}
			else if (c < 0xf8)
			{
				if (i + 3 >= str.Length)
					return;

				code = (c & 0x07) << 18;
				code |= (str[++i] & 0x3f) << 6;
				code |= (str[++i] & 0x3f) << 6;
				code |= str[++i] & 0x3f;
				var a = (code >> 10) + 0xd7c0;
				code &= (code & 0x3ff) + 0xdc00;

				sb.AppendASCII("\\u");
				sb.Append(Utils.NibbleToHexUpper((byte)(a >> 12)));
				sb.Append(Utils.NibbleToHexUpper((byte)(a >> 8)));
				sb.Append(Utils.NibbleToHexUpper((byte)(a >> 4)));
				sb.Append(Utils.NibbleToHexUpper((byte)a));
				sb.AppendASCII("\\u");
				sb.Append(Utils.NibbleToHexUpper((byte)(code >> 12)));
				sb.Append(Utils.NibbleToHexUpper((byte)(code >> 8)));
				sb.Append(Utils.NibbleToHexUpper((byte)(code >> 4)));
				sb.Append(Utils.NibbleToHexUpper((byte)code));
			}
		}

		sb.AppendASCII('"');
	}
}
