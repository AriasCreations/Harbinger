package dev.zontreck.harbinger.simulator.llsd;

import dev.zontreck.harbinger.simulator.exceptions.OSDException;
import dev.zontreck.harbinger.simulator.types.enums.OSDType;
import dev.zontreck.harbinger.simulator.types.structureddata.OSD;
import dev.zontreck.harbinger.simulator.types.structureddata.OSDArray;
import dev.zontreck.harbinger.simulator.types.structureddata.OSDMap;
import dev.zontreck.harbinger.utils.SimUtils;

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
}
