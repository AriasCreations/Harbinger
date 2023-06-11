package dev.zontreck.harbinger.utils;

import dev.zontreck.harbinger.simulator.types.osUTF8ByteCount;

import java.io.UnsupportedEncodingException;
import java.nio.ByteOrder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Random;

public class SimUtils
{

	public static final boolean LITTLE_ENDIAN = !IsBigEndian();
	public static final boolean BIG_ENDIAN = IsBigEndian();

	private static final byte ASCIIzero = (byte)'0';
	private static final byte ASCIIminus = (byte)'-';


	public static boolean IsBigEndian() {
		return !((ByteOrder.nativeOrder()) == ByteOrder.LITTLE_ENDIAN);
	}

	public static boolean ApproxEqual(float a, float b, float tolerance ){
		float diff = Math.abs(a-b);
		if(diff<=tolerance)
			return true;

		a= Math.abs(a);
		b=Math.abs(b);
		if(b>a)
			a=b;

		return diff <= a;
	}

	public static boolean ApproxZero(float a, float tolerance)
	{
		return Math.abs(a) <= tolerance;
	}
	public static boolean ApproxZero(float a)
	{
		return Math.abs(a) <= 1e-6;
	}

	public static boolean ApproxEqual(float a, float b)
	{
		return ApproxEqual(a,b,1e-6f);
	}

	public static int CombineHash(int a, int b)
	{
		return 65599 * a + b;
	}

	public static short BytesToInt16(byte[] bytes)
	{
		//if (bytes.Length < 2 ) return 0;
		return (short)(bytes[0] | (bytes[1] << 8));
	}

	public static String IntToHexString(int i)
	{
		return String.format("%08x", i);
	}

	public static String BytesToString(byte[] arr){
		return BytesToString(arr,0,arr.length);
	}

	public static float BytesToFloatSafepos(byte[] arr, int pos)
	{
		float fTotal = 0f;
		for(int i = pos; i<arr.length; i++)
		{
			fTotal += arr[i];
		}

		return fTotal;
	}

	public static String BytesToString(byte[] arr, int index, int count)
	{
		int ptr=0;
		byte[] splice = new byte[count];
		for(int i=index;i<count;i++){
			splice[i]=arr[ptr];

			ptr++;
		}
		return new String(splice);
	}

	public static String BytesToHexString(byte[] arr, String field)
	{
		return BytesToHexString(arr, arr.length, field);
	}

	public static String BytesToHexString(byte[] arr, int len, String field){
		StringBuilder builder = new StringBuilder();

		for(int i=0;i<len;i+=16){
			if(i != 0) builder.append("\n");

			if(field == null || field.isEmpty()){
				builder.append(field);
				builder.append(": ");
			}

			for(int j=0, k=i; j<16; ++j, ++k)
			{
				if(k>=len)break;

				if(j != 0) builder.append(" ");

				builder.append(String.format("%02X", arr[k]));
			}
		}

		return builder.toString();
	}

	public static byte[] StringToBytesNoTerm(String str){
		return StringToBytesNoTerm(str, str.length());
	}

	public static byte[] StringToBytesNoTerm(String str, int max){
		if(str == null || str.isEmpty()) return new byte[0];

		osUTF8ByteCount Bytes = osUTF8GetBytesCount(str);
		if(Bytes.length == 0) return new byte[0];

		byte[] dest = osUTF8GetBytes(str, Bytes.srcLen);
		return dest;
	}

	public static osUTF8ByteCount osUTF8GetBytesCount(String str)
	{
		return osUTF8GetBytesCount(str, str.length());
	}

	public static osUTF8ByteCount osUTF8GetBytesCount(String str, int max)
	{
		osUTF8ByteCount count = new osUTF8ByteCount();
		count.length = osUTF8GetBytes(str, str.length()).length;
		count.srcLen = max;
		return count;
	}

	public static byte[] osUTF8GetBytes(String src, int strLen)
	{
		byte[] arr = new byte[0];
		try {
			arr = src.substring(0, strLen).getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}

		return arr;
	}

	public static byte[] UIntToStrBytes_reversed(int v)
	{
		int n = 0;
		byte[] arr = new byte[32];
		do{
			byte a = ASCIIzero;
			a += (byte)(v % 10);
			arr[n] = a;
			n++;
			v /= 10;
		} while(v > 0);

		return Arrays.copyOf(arr, n);
	}

	public static byte[] IntToByteString(int v)
	{
		byte[] tmp = UIntToStrBytes_reversed(((v > 0) ? v : -v));
		byte[] dst = new byte[16];

		if(v > 0){
			for(int i = 0, j = tmp.length - 1; i < tmp.length; i++, j-- )
			{
				dst[i] = tmp[j];
			}
		}else {
			for(int i = 1, j = tmp.length - 1; i < tmp.length + 1; i++, j-- )
			{
				dst[i] = tmp[j];
			}
		}

		return dst;
	}

	public static boolean IsFinite(float value)
	{
		return Float.isFinite(value);
	}

	public static float Distance(float value1, float value2)
	{
		return MathF.Abs(value1 - value2);
	}


	public static float Hermite(float value1, float tangent1, float value2, float tangent2, float amount)
	{
		if (amount <= 0f)
			return value1;
		if (amount >= 1f)
			return value2;

		// All transformed to double not to lose precission
		// Otherwise, for high numbers of param:amount the result is NaN instead of Infinity
		double v1 = value1, v2 = value2, t1 = tangent1, t2 = tangent2, s = amount;
		var sSquared = s * s;
		var sCubed = sSquared * s;

		return (float)((2d * v1 - 2d * v2 + t2 + t1) * sCubed +
				(3d * v2 - 3d * v1 - 2d * t1 - t2) * sSquared +
				t1 * s + v1);
	}

	public static int Hermite(int value1, int tangent1, int value2, int tangent2, int amount)
	{
		if (amount <= 0)
			return value1;
		if (amount >= 1)
			return value2;

		// All transformed to double not to lose precission
		// Otherwise, for high numbers of param:amount the result is NaN instead of Infinity
		double v1 = value1, v2 = value2, t1 = tangent1, t2 = tangent2, s = amount;
		var sSquared = s * s;
		var sCubed = sSquared * s;

		return (int)((2 * v1 - 2 * v2 + t2 + t1) * sCubed +
				(3 * v2 - 3 * v1 - 2 * t1 - t2) * sSquared +
				t1 * s + v1);
	}

	public static double Hermite(double value1, double tangent1, double value2, double tangent2, double amount)
	{
		if (amount <= 0d)
			return value1;
		if (amount >= 1f)
			return value2;

		// All transformed to double not to lose precission
		// Otherwise, for high numbers of param:amount the result is NaN instead of Infinity
		double v1 = value1, v2 = value2, t1 = tangent1, t2 = tangent2, s = amount;
		var sSquared = s * s;
		var sCubed = sSquared * s;

		return (2d * v1 - 2d * v2 + t2 + t1) * sCubed +
				(3d * v2 - 3d * v1 - 2d * t1 - t2) * sSquared +
				t1 * s + v1;
	}

	public static float Lerp(float value1, float value2, float amount)
	{
		return value1 + (value2 - value1) * amount;
	}
	public static int Lerp(int value1, int value2, int amount)
	{
		return value1 + (value2 - value1) * amount;
	}

	public static double Lerp(double value1, double value2, double amount)
	{
		return value1 + (value2 - value1) * amount;
	}

	public static float SmoothStep(float value1, float value2, float amount)
	{
		return Hermite(value1, 0f, value2, 0f, amount);
	}
	public static int SmoothStep(int value1, int value2, int amount)
	{
		return Hermite(value1, 0, value2, 0, amount);
	}

	public static double SmoothStep(double value1, double value2, double amount)
	{
		return Hermite(value1, 0f, value2, 0f, amount);
	}

	public static float ToDegrees(float radians)
	{
		// This method uses double precission internally,
		// though it returns single float
		// Factor = 180 / pi
		return (float)(radians * 57.295779513082320876798154814105);
	}
	public static float ToRadians(float degrees)
	{
		// This method uses double precission internally,
		// though it returns single float
		// Factor = pi / 180
		return (float)(degrees * 0.017453292519943295769236907684886);
	}

	public static byte[] MakeHash(String algo, byte[] data) throws NoSuchAlgorithmException {
		MessageDigest md = MessageDigest.getInstance(algo);
		md.update(data);
		return md.digest();
	}

	public static byte[] MD5(byte[] data) throws NoSuchAlgorithmException {
		return MakeHash("MD5", data);

	}
	public static byte[] SHA1(byte[] data) throws NoSuchAlgorithmException {
		return MakeHash("SHA1", data);
	}

	public static String HexString(byte[] arr)
	{

		String finalStr = "";
		for (byte b :
				arr) {
			finalStr += String.format("%02x", b);
		}

		return finalStr;
	}

	public static String SHA1String(String value)
	{
		try {
			byte[] arr = SHA1(value.getBytes());
			return HexString(arr);
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}

	public static byte[] SHA256(byte[] data) throws NoSuchAlgorithmException {
		return MakeHash("SHA256", data);
	}

	public static String SHA256String(String value)
	{
		try{
			byte[] arr = SHA256(value.getBytes());
			return HexString(arr);
		}catch(Exception e){
			throw new RuntimeException();
		}
	}

	/// $1$???
	public static String MD5(String password)
	{
		try{
			byte[] arr = MD5(password.getBytes());
			return "$1$" + HexString(arr);
		}catch(Exception e){
			throw new RuntimeException();
		}
	}

	public static String MD5String(String value)
	{
		try{
			byte[] arr = MD5(value.getBytes());
			return HexString(arr);
		}catch(Exception e){
			throw new RuntimeException();
		}
	}

	public static double RandomDouble()
	{
		Random rng = new Random();
		return rng.nextDouble();
	}


}
