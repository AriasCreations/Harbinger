package dev.zontreck.harbinger.simulator.llsd;

import dev.zontreck.harbinger.simulator.types.structureddata.OSD;

import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

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
		using(var ms = new MemoryStream(binaryData))
		{
			return DeserializeLLSDBinary(ms);
		}
	}

	/// <summary>
	///     Deserializes binary LLSD
	/// </summary>
	/// <param name="stream">Stream to read the data from</param>
	/// <returns>OSD containting deserialized data</returns>
	public static OSD DeserializeLLSDBinary(Stream stream) {
		if (!stream.CanSeek)
			throw new OSDException("Cannot deserialize binary LLSD from unseekable streams");

		SkipWhiteSpace(stream);

		if (!FindString(stream, llsdBinaryHead) && !FindString(stream, llsdBinaryHead2)) {
			//throw new OSDException("Failed to decode binary LLSD");
		}

		SkipWhiteSpace(stream);

		return ParseLLSDBinaryElement(stream);
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
		using(var ms = SerializeLLSDBinaryStream(osd, prependHeader))
		{
			return ms.ToArray();
		}
	}

	/// <summary>
	///     Serializes OSD to binary format. It does no prepend header
	/// </summary>
	/// <param name="data">OSD to serialize</param>
	/// <returns>Serialized data</returns>
	public static MemoryStream SerializeLLSDBinaryStream(OSD data) {
		return SerializeLLSDBinaryStream(data, true);
	}

	/// <summary>
	///     Serializes OSD to binary format
	/// </summary>
	/// <param name="data">OSD to serialize</param>
	/// <param name="prependHeader"></param>
	/// <returns>Serialized data</returns>
	public static MemoryStream SerializeLLSDBinaryStream(OSD data, boolean prependHeader) {
		var stream = new MemoryStream(initialBufferSize);

		if (prependHeader) {
			stream.Write(llsdBinaryHeadBytes, 0, llsdBinaryHeadBytes.Length);
			stream.WriteByte((byte) '\n');
		}

		SerializeLLSDBinaryElement(stream, data);
		return stream;
	}

	private static void SerializeLLSDBinaryElement(MemoryStream stream, OSD osd) {
		switch (osd.Type) {
			case OSDType.Unknown:
				stream.WriteByte(undefBinaryValue);
				break;
			case OSDType.Boolean:
				stream.Write(osd.AsBinary(), 0, 1);
				break;
			case OSDType.Integer:
				stream.WriteByte(integerBinaryMarker);
				stream.Write(osd.AsBinary(), 0, int32Length);
				break;
			case OSDType.Real:
				stream.WriteByte(realBinaryMarker);
				stream.Write(osd.AsBinary(), 0, doubleLength);
				break;
			case OSDType.UUID:
				stream.WriteByte(uuidBinaryMarker);
				stream.Write(osd.AsBinary(), 0, 16);
				break;
			case OSDType.String:
				stream.WriteByte(stringBinaryMarker);
				var rawString = osd.AsBinary();
				var stringLengthNetEnd = Utils.IntToBytesBig(rawString.Length);
				stream.Write(stringLengthNetEnd, 0, int32Length);
				stream.Write(rawString, 0, rawString.Length);
				break;
			case OSDType.Binary:
				stream.WriteByte(binaryBinaryMarker);
				var rawBinary = osd.AsBinary();
				var binaryLengthNetEnd = Utils.IntToBytesBig(rawBinary.Length);
				stream.Write(binaryLengthNetEnd, 0, int32Length);
				stream.Write(rawBinary, 0, rawBinary.Length);
				break;
			case OSDType.Date:
				stream.WriteByte(dateBinaryMarker);
				stream.Write(osd.AsBinary(), 0, doubleLength);
				break;
			case OSDType.URI:
				stream.WriteByte(uriBinaryMarker);
				var rawURI = osd.AsBinary();
				var uriLengthNetEnd = Utils.IntToBytesBig(rawURI.Length);
				stream.Write(uriLengthNetEnd, 0, int32Length);
				stream.Write(rawURI, 0, rawURI.Length);
				break;
			case OSDType.Array:
				SerializeLLSDBinaryArray(stream, (OSDArray) osd);
				break;
			case OSDType.Map:
				SerializeLLSDBinaryMap(stream, (OSDMap) osd);
				break;
			default:
				throw new OSDException("Binary serialization: Not existing element discovered.");
		}
	}

	private static void SerializeLLSDBinaryArray(MemoryStream stream, OSDArray osdArray) {
		stream.WriteByte(arrayBeginBinaryMarker);
		var binaryNumElementsHostEnd = Utils.IntToBytesBig(osdArray.Count);
		stream.Write(binaryNumElementsHostEnd, 0, int32Length);

		foreach(var osd in osdArray)
		SerializeLLSDBinaryElement(stream, osd);
		stream.WriteByte(arrayEndBinaryMarker);
	}

	private static void SerializeLLSDBinaryMap(MemoryStream stream, OSDMap osdMap) {
		stream.WriteByte(mapBeginBinaryMarker);
		var binaryNumElementsNetEnd = Utils.IntToBytesBig(osdMap.Count);
		stream.Write(binaryNumElementsNetEnd, 0, int32Length);

		foreach(KeyValuePair < string, OSD > kvp in osdMap)
		{
			stream.WriteByte(keyBinaryMarker);
			var binaryKey = Encoding.UTF8.GetBytes(kvp.Key);
			var binaryKeyLength = Utils.IntToBytesBig(binaryKey.Length);
			stream.Write(binaryKeyLength, 0, int32Length);
			stream.Write(binaryKey, 0, binaryKey.Length);
			SerializeLLSDBinaryElement(stream, kvp.Value);
		}

		stream.WriteByte(mapEndBinaryMarker);
	}

	private static OSD ParseLLSDBinaryElement(Stream stream) {
		SkipWhiteSpace(stream);
		OSD osd;

		var marker = stream.ReadByte();
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
				var integer = Utils.BytesToIntBig(ConsumeBytes(stream, int32Length));
				osd = OSD.FromInteger(integer);
				break;
			case realBinaryMarker:
				var dbl = Utils.BytesToDoubleBig(ConsumeBytes(stream, doubleLength));
				osd = OSD.FromReal(dbl);
				break;
			case uuidBinaryMarker:
				osd = OSD.FromUUID(new UUID(ConsumeBytes(stream, 16), 0));
				break;
			case binaryBinaryMarker:
				var binaryLength = Utils.BytesToIntBig(ConsumeBytes(stream, int32Length));
				osd = OSD.FromBinary(ConsumeBytes(stream, binaryLength));
				break;
			case stringBinaryMarker:
				var stringLength = Utils.BytesToIntBig(ConsumeBytes(stream, int32Length));
				var ss = Encoding.UTF8.GetString(ConsumeBytes(stream, stringLength));
				osd = OSD.FromString(ss);
				break;
			case uriBinaryMarker:
				var uriLength = Utils.BytesToIntBig(ConsumeBytes(stream, int32Length));
				var sUri = Encoding.UTF8.GetString(ConsumeBytes(stream, uriLength));
				Uri uri;
				try {
					uri = new Uri(sUri, UriKind.RelativeOrAbsolute);
				} catch
			{
				throw new OSDException("Binary LLSD parsing: Invalid Uri format detected.");
			}

			osd = OSD.FromUri(uri);
			break;
			case dateBinaryMarker:
				var timestamp = Utils.BytesToDouble(ConsumeBytes(stream, doubleLength));
				var dateTime = DateTime.SpecifyKind(Utils.Epoch, DateTimeKind.Utc);
				dateTime = dateTime.AddSeconds(timestamp);
				osd = OSD.FromDate(dateTime.ToLocalTime());
				break;
			case arrayBeginBinaryMarker:
				osd = ParseLLSDBinaryArray(stream);
				break;
			case mapBeginBinaryMarker:
				osd = ParseLLSDBinaryMap(stream);
				break;
			default:
				throw new OSDException("Binary LLSD parsing: Unknown type marker.");
		}

		return osd;
	}

	private static OSD ParseLLSDBinaryArray(Stream stream) {
		var numElements = Utils.BytesToIntBig(ConsumeBytes(stream, int32Length));
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

	private static OSD ParseLLSDBinaryMap(Stream stream) {
		var numElements = Utils.BytesToIntBig(ConsumeBytes(stream, int32Length));
		var crrElement = 0;
		var osdMap = new OSDMap();
		while (crrElement < numElements) {
			if (!FindByte(stream, keyBinaryMarker))
				throw new OSDException("Binary LLSD parsing: Missing key marker in map.");
			var keyLength = Utils.BytesToIntBig(ConsumeBytes(stream, int32Length));
			var key = Encoding.UTF8.GetString(ConsumeBytes(stream, keyLength));
			osdMap[key] = ParseLLSDBinaryElement(stream);
			crrElement++;
		}

		if (!FindByte(stream, mapEndBinaryMarker))
			throw new OSDException("Binary LLSD parsing: Missing end marker in map.");

		return osdMap;
	}

	/// <summary>
	/// </summary>
	/// <param name="stream"></param>
	public static void SkipWhiteSpace(Stream stream) {
		int bt;
		while ((bt = stream.ReadByte()) > 0 &&
				((byte) bt == ' ' || (byte) bt == '\t' ||
						(byte) bt == '\n' || (byte) bt == '\r')
		) {
		}

		if (stream.Position > 0)
			stream.Seek(-1, SeekOrigin.Current);
	}

	/// <summary>
	/// </summary>
	/// <param name="stream"></param>
	/// <param name="toFind"></param>
	/// <returns></returns>
	public static boolean FindByte(Stream stream, byte toFind) {
		var bt = stream.ReadByte();
		if (bt < 0)
			return false;
		if ((byte) bt == toFind) return true;

		stream.Seek(-1L, SeekOrigin.Current);
		return false;
	}

	/// <summary>
	/// </summary>
	/// <param name="stream"></param>
	/// <param name="toFind"></param>
	/// <returns></returns>
	public static boolean FindString(Stream stream, String toFind) {
		var lastIndexToFind = toFind.Length - 1;
		var crrIndex = 0;
		var found = true;
		int bt;
		var lastPosition = stream.Position;

		while (found &&
				(bt = stream.ReadByte()) > 0 &&
				crrIndex <= lastIndexToFind
		)
			if (toFind[crrIndex].ToString().Equals(((char) bt).ToString(), StringComparison.InvariantCultureIgnoreCase)) {
				found = true;
				crrIndex++;
			} else {
				found = false;
			}

		if (found && crrIndex > lastIndexToFind) {
			stream.Seek(-1L, SeekOrigin.Current);
			return true;
		}

		stream.Position = lastPosition;
		return false;
	}

	/// <summary>
	/// </summary>
	/// <param name="stream"></param>
	/// <param name="consumeBytes"></param>
	/// <returns></returns>
	public static byte[] ConsumeBytes(Stream stream, int consumeBytes) {
		var bytes = new byte[consumeBytes];
		if (stream.Read(bytes, 0, consumeBytes) < consumeBytes)
			throw new OSDException("Binary LLSD parsing: Unexpected end of stream.");
		return bytes;
	}
}
