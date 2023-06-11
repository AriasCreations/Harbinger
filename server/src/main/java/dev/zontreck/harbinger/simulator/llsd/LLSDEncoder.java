package dev.zontreck.harbinger.simulator.llsd;

import dev.zontreck.harbinger.simulator.types.osUTF8;
import dev.zontreck.harbinger.simulator.types.osUTF8Consts;

import java.time.Instant;
import java.util.Date;

public class LLSDEncoder
{

	public static final Instant epoch = Instant.EPOCH;
	public static final int MAXDATASIZE = 128 * 1024;

	public static void AddStart(osUTF8 buf, boolean addversion)
	{
		if(addversion)
			buf.Append(osUTF8Consts.XMLformalHeaderllsdstart);
		else
			buf.Append(osUTF8Consts.XMLllsdStart);

	}
	public static void AddStart(osUTF8 buf)
	{
		AddStart(buf,false);
	}

	public static osUTF8 Start()
	{
		return Start(MAXDATASIZE);
	}

	public static osUTF8 Start(int size)
	{
		return Start(size, false);
	}

	public static osUTF8 Start(int size, boolean addversion)
	{
		osUTF8 inst = new osUTF8(size);
		LLSDEncoder.AddStart(inst, addversion);

		return inst;
	}

	public static void AddEnd(osUTF8 buf)
	{
		buf.Append(osUTF8Consts.XMLllsdEnd);
	}

	public static String End(osUTF8 buf)
	{
		AddEnd(buf);
		return buf.toString();
	}

	public static byte[] EndToBytes(osUTF8 buf)
	{
		AddEnd(buf);
		return buf.ToArray();
	}

	public static void AddMap(osUTF8 sb)
	{
		sb.Append(osUTF8Consts.XMLmapStart);
	}

	public static void AddEndMap(osUTF8 sb)
	{
		sb.Append(osUTF8Consts.XMLmapEnd);
	}

	public static void AddEmptyMap(osUTF8 sb)
	{
		sb.Append(osUTF8Consts.XMLmapEmpty);
	}

	public static void AddArray(osUTF8 sb)
	{
		sb.Append(osUTF8Consts.XMLarrayStart);
	}

	public static void AddEndArray(osUTF8 sb)
	{
		sb.Append(osUTF8Consts.XMLarrayEnd);
	}

	public static void AddEndMapAndArray(osUTF8 sb)
	{
		sb.Append(osUTF8Consts.XMLmapEndarrayEnd);
	}

	public static void AddEmptyArray(osUTF8 sb)
	{
		sb.Append(osUTF8Consts.XMLarrayEmpty);
	}

	public static void AddEndArrayAndMap(osUTF8 sb)
	{
		sb.Append(osUTF8Consts.XMLarrayEndmapEnd);
	}

	// undefined or null
	public static void AddUnknownElem(osUTF8 sb)
	{
		sb.Append(osUTF8Consts.XMLundef);
	}

	public static void AddElem(boolean e, osUTF8 sb)
	{
		if (e)
			sb.Append(osUTF8Consts.XMLfullbooleanOne);
		else
			sb.Append(osUTF8Consts.XMLfullbooleanZero);
	}

	public static void AddElem(byte e, osUTF8 sb)
	{
		if (e == 0)
		{
			sb.Append(osUTF8Consts.XMLintegerEmpty);
		}
		else
		{
			sb.Append(osUTF8Consts.XMLintegerStart);
			sb.AppendInt(e);
			sb.Append(osUTF8Consts.XMLintegerEnd);
		}
	}

	public static void AddElem(byte[] e, osUTF8 sb)
	{
		if (e == null || e.length == 0)
		{
			sb.Append(osUTF8Consts.XMLbinaryEmpty);
		}
		else
		{
			sb.Append(osUTF8Consts.XMLbinaryStart); // encode64 is default
			OSDParser.base64Encode(e, sb);
			sb.Append(osUTF8Consts.XMLbinaryEnd);
		}
	}

	public static void AddElem(byte[] e, int start, int length, osUTF8 sb)
	{
		if (start + length >= e.length)
			length = e.length - start;

		if (e == null || e.length == 0 || length <= 0)
		{
			sb.Append(osUTF8Consts.XMLbinaryEmpty);
		}
		else
		{
			sb.Append(osUTF8Consts.XMLbinaryStart); // encode64 is default
			OSDParser.base64Encode(e, start, length, sb);
			sb.Append(osUTF8Consts.XMLbinaryEnd);
		}
	}

	public static void AddElem(int e, osUTF8 sb)
	{
		if (e == 0)
		{
			sb.Append(osUTF8Consts.XMLintegerEmpty);
		}
		else
		{
			sb.Append(osUTF8Consts.XMLintegerStart);
			sb.AppendInt(e);
			sb.Append(osUTF8Consts.XMLintegerEnd);
		}
	}

	public static void AddElem(int e, osUTF8 sb)
	{
		AddElem(uintToByteArray(e), sb);
	}

	public static void AddElem(long e, osUTF8 sb)
	{
		AddElem(ulongToByteArray(e), sb);
	}

	public static void AddElem(float e, osUTF8 sb)
	{
		if (e == 0)
		{
			sb.Append(osUTF8Consts.XMLrealZero);
		}
		else
		{
			sb.Append(osUTF8Consts.XMLrealStart);
			sb.AppendASCII(e.toString(CultureInfo.InvariantCulture));
			sb.Append(osUTF8Consts.XMLrealEnd);
		}
	}

	public static void AddElem(Vector2 e, osUTF8 sb)
	{
		if (e.X == 0)
		{
			sb.Append(osUTF8Consts.XMLarrayStartrealZero);
		}
		else
		{
			sb.Append(osUTF8Consts.XMLarrayStartrealStart);
			sb.AppendASCII(e.X.ToString(CultureInfo.InvariantCulture));
			sb.Append(osUTF8Consts.XMLrealEnd);
		}

		if (e.Y == 0)
		{
			sb.Append(osUTF8Consts.XMLrealZeroarrayEnd);
		}
		else
		{
			sb.Append(osUTF8Consts.XMLrealStart);
			sb.AppendASCII(e.Y.ToString(CultureInfo.InvariantCulture));
			sb.Append(osUTF8Consts.XMLrealEndarrayEnd);
		}
	}

	public static void AddElem(Vector3 e, osUTF8 sb)
	{
		if (e.X == 0)
		{
			sb.Append(osUTF8Const.XMLarrayStartrealZero);
		}
		else
		{
			sb.Append(osUTF8Const.XMLarrayStartrealStart);
			sb.AppendASCII(e.X.ToString(CultureInfo.InvariantCulture));
			sb.Append(osUTF8Const.XMLrealEnd);
		}

		if (e.Y == 0)
		{
			sb.Append(osUTF8Const.XMLrealZero);
		}
		else
		{
			sb.Append(osUTF8Const.XMLrealStart);
			sb.AppendASCII(e.Y.ToString(CultureInfo.InvariantCulture));
			sb.Append(osUTF8Const.XMLrealEnd);
		}

		if (e.Z == 0)
		{
			sb.Append(osUTF8Const.XMLrealZeroarrayEnd);
		}
		else
		{
			sb.Append(osUTF8Const.XMLrealStart);
			sb.AppendASCII(e.Z.ToString(CultureInfo.InvariantCulture));
			sb.Append(osUTF8Const.XMLrealEndarrayEnd);
		}
	}

	public static void AddElem(Quaternion e, osUTF8 sb)
	{
		if (e.X == 0)
		{
			sb.Append(osUTF8Const.XMLarrayStartrealZero);
		}
		else
		{
			sb.Append(osUTF8Const.XMLarrayStartrealStart);
			sb.AppendASCII(e.X.ToString(CultureInfo.InvariantCulture));
			sb.Append(osUTF8Const.XMLrealEnd);
		}

		if (e.Y == 0)
		{
			sb.Append(osUTF8Const.XMLrealZero);
		}
		else
		{
			sb.Append(osUTF8Const.XMLrealStart);
			sb.AppendASCII(e.Y.ToString(CultureInfo.InvariantCulture));
			sb.Append(osUTF8Const.XMLrealEnd);
		}

		if (e.Z == 0)
		{
			sb.Append(osUTF8Const.XMLrealZero);
		}
		else
		{
			sb.Append(osUTF8Const.XMLrealStart);
			sb.AppendASCII(e.Z.ToString(CultureInfo.InvariantCulture));
			sb.Append(osUTF8Const.XMLrealEnd);
		}

		if (e.W == 0)
		{
			sb.Append(osUTF8Const.XMLrealZeroarrayEnd);
		}
		else
		{
			sb.Append(osUTF8Const.XMLrealStart);
			sb.AppendASCII(e.W.ToString(CultureInfo.InvariantCulture));
			sb.Append(osUTF8Const.XMLrealEndarrayEnd);
		}
	}

	public static void AddElem(double e, osUTF8 sb)
	{
		if (e == 0)
		{
			sb.Append(osUTF8Const.XMLrealZero);
		}
		else
		{
			sb.Append(osUTF8Const.XMLrealStart);
			sb.AppendASCII(e.ToString(CultureInfo.InvariantCulture));
			sb.Append(osUTF8Const.XMLrealEnd);
		}
	}

	public static void AddElem(UUID e, osUTF8 sb)
	{
		if (e.IsZero())
		{
			sb.Append(osUTF8Const.XMLuuidEmpty);
		}
		else
		{
			sb.Append(osUTF8Const.XMLuuidStart);
			sb.AppendUUID(e);
			sb.Append(osUTF8Const.XMLuuidEnd);
		}
	}

	public static void AddElem(string e, osUTF8 sb)
	{
		if (string.IsNullOrEmpty(e))
		{
			sb.Append(osUTF8Const.XMLstringEmpty);
		}
		else
		{
			sb.Append(osUTF8Const.XMLstringStart);
			OSDParser.EscapeToXML(sb, e);
			sb.Append(osUTF8Const.XMLstringEnd);
		}
	}

	public static void AddElem(osUTF8 e, osUTF8 sb)
	{
		if (osUTF8.IsNullOrEmpty(e))
		{
			sb.Append(osUTF8Const.XMLstringEmpty);
		}
		else
		{
			sb.Append(osUTF8Const.XMLstringStart);
			OSDParser.EscapeToXML(sb, e);
			sb.Append(osUTF8Const.XMLstringEnd);
		}
	}

	public static void AddRawElem(string e, osUTF8 sb)
	{
		if (!string.IsNullOrEmpty(e))
			sb.Append(e);
	}

	public static void AddRawElem(byte[] e, osUTF8 sb)
	{
		if (e != null && e.Length >= 0)
			sb.Append(e);
	}

	public static void AddElem(Uri e, osUTF8 sb)
	{
		if (e == null)
		{
			sb.Append(osUTF8Const.XMLuriEmpty);
			return;
		}

		string s;
		if (e.IsAbsoluteUri)
			s = e.AbsoluteUri;
		else
			s = e.ToString();

		if (string.IsNullOrEmpty(s))
		{
			sb.Append(osUTF8Const.XMLuriEmpty);
		}
		else
		{
			sb.Append(osUTF8Const.XMLuriStart);
			sb.Append(s);
			sb.Append(osUTF8Const.XMLuriEnd);
		}
	}

	public static void AddElem(DateTime e, osUTF8 sb)
	{
		var u = e.ToUniversalTime();
		if (u == depoch)
		{
			sb.Append(osUTF8Const.XMLdateEmpty);
			return;
		}

		string format;
		if (u.Hour == 0 && u.Minute == 0 && u.Second == 0)
			format = "yyyy-MM-dd";
		else if (u.Millisecond > 0)
			format = "yyyy-MM-ddTHH:mm:ss.ffZ";
		else
			format = "yyyy-MM-ddTHH:mm:ssZ";
		sb.Append(osUTF8Const.XMLdateStart);
		sb.AppendASCII(u.ToString(format, CultureInfo.InvariantCulture));
		sb.Append(osUTF8Const.XMLdateEnd);
	}

//************ key value *******************
// assumes name is a valid llsd key

	public static void AddMap(string name, osUTF8 sb)
	{
		sb.Append(osUTF8Const.XMLkeyStart);
		sb.AppendASCII(name);
		sb.Append(osUTF8Const.XMLkeyEndmapStart);
	}

	public static void AddEmptyMap(string name, osUTF8 sb)
	{
		sb.Append(osUTF8Const.XMLkeyStart);
		sb.AppendASCII(name);
		sb.Append(osUTF8Const.XMLkeyEndmapEmpty);
	}

	// array == a list values
	public static void AddArray(string name, osUTF8 sb)
	{
		sb.Append(osUTF8Const.XMLkeyStart);
		sb.AppendASCII(name);
		sb.Append(osUTF8Const.XMLkeyEndarrayStart);
	}

	public static void AddEmptyArray(string name, osUTF8 sb)
	{
		sb.Append(osUTF8Const.XMLkeyStart);
		sb.AppendASCII(name);
		sb.Append(osUTF8Const.XMLkeyEndarrayEmpty);
	}

	public static void AddArrayAndMap(string name, osUTF8 sb)
	{
		sb.Append(osUTF8Const.XMLkeyStart);
		sb.AppendASCII(name);
		sb.Append(osUTF8Const.XMLkeyEndarrayStartmapStart);
	}


	// undefined or null
	public static void AddUnknownElem(string name, osUTF8 sb)
	{
		sb.Append(osUTF8Const.XMLkeyStart);
		sb.AppendASCII(name);
		sb.Append(osUTF8Const.XMLkeyEndundef);
	}

	public static void AddElem(string name, bool e, osUTF8 sb)
	{
		sb.Append(osUTF8Const.XMLkeyStart);
		sb.AppendASCII(name);
		sb.Append(osUTF8Const.XMLkeyEnd);

		if (e)
			sb.Append(osUTF8Const.XMLfullbooleanOne);
		else
			sb.Append(osUTF8Const.XMLfullbooleanZero);
	}

	public static void AddElem(string name, byte e, osUTF8 sb)
	{
		sb.Append(osUTF8Const.XMLkeyStart);
		sb.AppendASCII(name);
		sb.Append(osUTF8Const.XMLkeyEnd);

		if (e == 0)
		{
			sb.Append(osUTF8Const.XMLintegerEmpty);
		}
		else
		{
			sb.Append(osUTF8Const.XMLintegerStart);
			sb.AppendInt(e);
			sb.Append(osUTF8Const.XMLintegerEnd);
		}
	}

	public static void AddElem(string name, byte[] e, osUTF8 sb)
	{
		sb.Append(osUTF8Const.XMLkeyStart);
		sb.AppendASCII(name);
		sb.Append(osUTF8Const.XMLkeyEnd);

		if (e == null || e.Length == 0)
		{
			sb.Append(osUTF8Const.XMLbinaryEmpty);
		}
		else
		{
			sb.Append(osUTF8Const.XMLbinaryStart); // encode64 is default
			OSDParser.base64Encode(e, sb);
			sb.Append(osUTF8Const.XMLbinaryEnd);
		}
	}

	public static void AddElem(string name, byte[] e, int start, int length, osUTF8 sb)
	{
		sb.Append(osUTF8Const.XMLkeyStart);
		sb.AppendASCII(name);
		sb.Append(osUTF8Const.XMLkeyEnd);

		if (start + length >= e.Length)
			length = e.Length - start;

		if (e == null || e.Length == 0 || length <= 0)
		{
			sb.Append(osUTF8Const.XMLbinaryEmpty);
		}
		else
		{
			sb.Append(osUTF8Const.XMLbinaryStart); // encode64 is default
			OSDParser.base64Encode(e, start, length, sb);
			sb.Append(osUTF8Const.XMLbinaryEnd);
		}
	}

	public static void AddElem(string name, int e, osUTF8 sb)
	{
		sb.Append(osUTF8Const.XMLkeyStart);
		sb.AppendASCII(name);
		sb.Append(osUTF8Const.XMLkeyEnd);

		if (e == 0)
		{
			sb.Append(osUTF8Const.XMLintegerEmpty);
		}
		else
		{
			sb.Append(osUTF8Const.XMLintegerStart);
			sb.AppendInt(e);
			sb.Append(osUTF8Const.XMLintegerEnd);
		}
	}

	public static void AddElem(string name, uint e, osUTF8 sb)
	{
		AddElem(name, uintToByteArray(e), sb);
	}

	public static void AddElem(string name, ulong e, osUTF8 sb)
	{
		AddElem(name, ulongToByteArray(e), sb);
	}

	public static void AddElem(string name, float e, osUTF8 sb)
	{
		sb.Append(osUTF8Const.XMLkeyStart);
		sb.AppendASCII(name);
		sb.Append(osUTF8Const.XMLkeyEnd);

		if (e == 0)
		{
			sb.Append(osUTF8Consts.XMLrealZero);
		}
		else
		{
			sb.Append(osUTF8Consts.XMLrealStart);
			sb.AppendASCII(e.toString(CultureInfo.InvariantCulture));
			sb.Append(osUTF8Consts.XMLrealEnd);
		}
	}

	public static void AddElem(String name, Vector2 e, osUTF8 sb)
	{
		sb.Append(osUTF8Consts.XMLkeyStart);
		sb.AppendASCII(name);

		if (e.X == 0)
		{
			sb.Append(osUTF8Consts.XMLkeyEndarrayStartrealZero);
		}
		else
		{
			sb.Append(osUTF8Consts.XMLkeyEndarrayStartrealZero);
			sb.AppendASCII(e.X.ToString(CultureInfo.InvariantCulture));
			sb.Append(osUTF8Consts.XMLrealEnd);
		}

		if (e.Y == 0)
		{
			sb.Append(osUTF8Consts.XMLrealZeroarrayEnd);
		}
		else
		{
			sb.Append(osUTF8Consts.XMLrealStart);
			sb.AppendASCII(e.Y.ToString(CultureInfo.InvariantCulture));
			sb.Append(osUTF8Consts.XMLrealEndarrayEnd);
		}
	}

	public static void AddElem(String name, Vector3 e, osUTF8 sb)
	{
		sb.Append(osUTF8Consts.XMLkeyStart);
		sb.AppendASCII(name);

		if (e.X == 0)
		{
			sb.Append(osUTF8Consts.XMLkeyEndarrayStartrealZero);
		}
		else
		{
			sb.Append(osUTF8Consts.XMLkeyEndarrayStartrealStart);
			sb.AppendASCII(e.X.ToString(CultureInfo.InvariantCulture));
			sb.Append(osUTF8Consts.XMLrealEnd);
		}

		if (e.Y == 0)
		{
			sb.Append(osUTF8Consts.XMLrealZero);
		}
		else
		{
			sb.Append(osUTF8Consts.XMLrealStart);
			sb.AppendASCII(e.Y.ToString(CultureInfo.InvariantCulture));
			sb.Append(osUTF8Consts.XMLrealEnd);
		}

		if (e.Z == 0)
		{
			sb.Append(osUTF8Consts.XMLrealZeroarrayEnd);
		}
		else
		{
			sb.Append(osUTF8Consts.XMLrealStart);
			sb.AppendASCII(e.Z.ToString(CultureInfo.InvariantCulture));
			sb.Append(osUTF8Consts.XMLrealEndarrayEnd);
		}
	}

	public static void AddElem(String name, Quaternion e, osUTF8 sb)
	{
		sb.Append(osUTF8Const.XMLkeyStart);
		sb.AppendASCII(name);

		if (e.X == 0)
		{
			sb.Append(osUTF8Const.XMLkeyEndarrayStartrealZero);
		}
		else
		{
			sb.Append(osUTF8Const.XMLkeyEndarrayStartrealStart);
			sb.AppendASCII(e.X.ToString(CultureInfo.InvariantCulture));
			sb.Append(osUTF8Const.XMLrealEnd);
		}

		if (e.Y == 0)
		{
			sb.Append(osUTF8Const.XMLrealZero);
		}
		else
		{
			sb.Append(osUTF8Const.XMLrealStart);
			sb.AppendASCII(e.Y.ToString(CultureInfo.InvariantCulture));
			sb.Append(osUTF8Const.XMLrealEnd);
		}

		if (e.Z == 0)
		{
			sb.Append(osUTF8Const.XMLrealZero);
		}
		else
		{
			sb.Append(osUTF8Const.XMLrealStart);
			sb.AppendASCII(e.Z.ToString(CultureInfo.InvariantCulture));
			sb.Append(osUTF8Const.XMLrealEnd);
		}

		if (e.W == 0)
		{
			sb.Append(osUTF8Const.XMLrealZeroarrayEnd);
		}
		else
		{
			sb.Append(osUTF8Const.XMLrealStart);
			sb.AppendASCII(e.W.ToString(CultureInfo.InvariantCulture));
			sb.Append(osUTF8Const.XMLrealEndarrayEnd);
		}
	}

	public static void AddElem(string name, double e, osUTF8 sb)
	{
		sb.Append(osUTF8Const.XMLkeyStart);
		sb.AppendASCII(name);
		sb.Append(osUTF8Const.XMLkeyEnd);

		if (e == 0)
		{
			sb.Append(osUTF8Const.XMLrealZero);
		}
		else
		{
			sb.Append(osUTF8Const.XMLrealStart);
			sb.AppendASCII(e.ToString(CultureInfo.InvariantCulture));
			sb.Append(osUTF8Const.XMLrealEnd);
		}
	}

	public static void AddElem(string name, UUID e, osUTF8 sb)
	{
		sb.Append(osUTF8Const.XMLkeyStart);
		sb.AppendASCII(name);
		sb.Append(osUTF8Const.XMLkeyEnd);

		if (e.IsZero())
		{
			sb.Append(osUTF8Const.XMLuuidEmpty);
		}
		else
		{
			sb.Append(osUTF8Const.XMLuuidStart);
			sb.AppendUUID(e);
			sb.Append(osUTF8Const.XMLuuidEnd);
		}
	}

	public static void AddElem(string name, string e, osUTF8 sb)
	{
		sb.Append(osUTF8Const.XMLkeyStart);
		sb.AppendASCII(name);
		sb.Append(osUTF8Const.XMLkeyEnd);

		if (string.IsNullOrEmpty(e))
		{
			sb.Append(osUTF8Const.XMLstringEmpty);
		}
		else
		{
			sb.Append(osUTF8Const.XMLstringStart);
			OSDParser.EscapeToXML(sb, e);
			sb.Append(osUTF8Const.XMLstringEnd);
		}
	}

	public static void AddElem(string name, osUTF8 e, osUTF8 sb)
	{
		sb.Append(osUTF8Const.XMLkeyStart);
		sb.AppendASCII(name);
		sb.Append(osUTF8Const.XMLkeyEnd);

		if (osUTF8.IsNullOrEmpty(e))
		{
			sb.Append(osUTF8Const.XMLstringEmpty);
		}
		else
		{
			sb.Append(osUTF8Const.XMLstringStart);
			OSDParser.EscapeToXML(sb, e);
			sb.Append(osUTF8Const.XMLstringEnd);
		}
	}

	public static void AddRawElem(string name, string e, osUTF8 sb)
	{
		if (string.IsNullOrEmpty(e))
			return;

		sb.Append(osUTF8Const.XMLkeyStart);
		sb.AppendASCII(name);
		sb.Append(osUTF8Const.XMLkeyEnd);
		sb.Append(e);
	}

	public static void AddElem(string name, Uri e, osUTF8 sb)
	{
		sb.Append(osUTF8Const.XMLkeyStart);
		sb.AppendASCII(name);
		sb.Append(osUTF8Const.XMLkeyEnd);

		if (e == null)
		{
			sb.Append(osUTF8Const.XMLuriEmpty);
			return;
		}

		string s;
		if (e.IsAbsoluteUri)
			s = e.AbsoluteUri;
		else
			s = e.ToString();

		if (string.IsNullOrEmpty(s))
		{
			sb.Append(osUTF8Const.XMLuriEmpty);
		}
		else
		{
			sb.Append(osUTF8Const.XMLuriStart);
			sb.Append(s);
			sb.Append(osUTF8Const.XMLuriEnd);
		}
	}

	public static void AddElem(String name, Date e, osUTF8 sb)
	{
		sb.Append(osUTF8Consts.XMLkeyStart);
		sb.AppendASCII(name);
		sb.Append(osUTF8Consts.XMLkeyEnd);

		var u = e.toInstant();
		if (u == epoch)
		{
			sb.Append(osUTF8Consts.XMLdateEmpty);
			return;
		}

		string format;
		if (u.Hour == 0 && u.Minute == 0 && u.Second == 0)
			format = "yyyy-MM-dd";
		else if (u.Millisecond > 0)
			format = "yyyy-MM-ddTHH:mm:ss.ffZ";
		else
			format = "yyyy-MM-ddTHH:mm:ssZ";
		sb.Append(osUTF8Consts.XMLdateStart);
		sb.AppendASCII(u.ToString(format, CultureInfo.InvariantCulture));
		sb.Append(osUTF8Consts.XMLdateEnd);
	}

	public static void AddLLSD(String e, osUTF8 sb)
	{
		sb.Append(e);
	}

	public static void AddLLSD(String name, String e, osUTF8 sb)
	{
		sb.Append(osUTF8Consts.XMLkeyStart);
		sb.AppendASCII(name);
		sb.Append(osUTF8Consts.XMLkeyEnd);
		sb.Append(e);
	}

	public static void AddElem_name(String s, osUTF8 sb)
	{
		if (s == null || s.isEmpty())
		{
			sb.Append(osUTF8Consts.XMLelement_name_Empty);
		}
		else
		{
			sb.Append(osUTF8Consts.XMLelement_name_Start);
			OSDParser.EscapeToXML(sb, s);
			sb.Append(osUTF8Consts.XMLstringEnd);
		}
	}

	public static void AddElem_agent_id(UUID e, osUTF8 sb)
	{
		if (e.IsZero())
		{
			sb.Append(osUTF8Consts.XMLelement_agent_id_Empty);
		}
		else
		{
			sb.Append(osUTF8Consts.XMLelement_agent_id_Start);
			sb.AppendUUID(e);
			sb.Append(osUTF8Consts.XMLuuidEnd);
		}
	}

	public static void AddElem_owner_id(UUID e, osUTF8 sb)
	{
		if (e.IsZero())
		{
			sb.Append(osUTF8Consts.XMLelement_owner_id_Empty);
		}
		else
		{
			sb.Append(osUTF8Consts.XMLelement_owner_id_Start);
			sb.AppendUUID(e);
			sb.Append(osUTF8Consts.XMLuuidEnd);
		}
	}

	public static void AddElem_parent_id(UUID e, osUTF8 sb)
	{
		if (e.IsZero())
		{
			sb.Append(osUTF8Consts.XMLelement_parent_id_Empty);
		}
		else
		{
			sb.Append(osUTF8Consts.XMLelement_parent_id_Start);
			sb.AppendUUID(e);
			sb.Append(osUTF8Consts.XMLuuidEnd);
		}
	}

	public static void AddElem_folder_id(UUID e, osUTF8 sb)
	{
		if (e.IsZero())
		{
			sb.Append(osUTF8Consts.XMLelement_folder_id_Empty);
		}
		else
		{
			sb.Append(osUTF8Consts.XMLelement_folder_id_Start);
			sb.AppendUUID(e);
			sb.Append(osUTF8Consts.XMLuuidEnd);
		}
	}

	public static void AddElem_asset_id(UUID e, osUTF8 sb)
	{
		if (e.IsZero())
		{
			sb.Append(osUTF8Consts.XMLelement_asset_id_Empty);
		}
		else
		{
			sb.Append(osUTF8Consts.XMLelement_asset_id_Start);
			sb.AppendUUID(e);
			sb.Append(osUTF8Consts.XMLuuidEnd);
		}
	}

	public static void AddElem_item_id(UUID e, osUTF8 sb)
	{
		if (e.IsZero())
		{
			sb.Append(osUTF8Const.XMLelement_item_id_Empty);
		}
		else
		{
			sb.Append(osUTF8Consts.XMLelement_item_id_Start);
			sb.AppendUUID(e);
			sb.Append(osUTF8Consts.XMLuuidEnd);
		}
	}

	public static void AddElem_category_id(UUID e, osUTF8 sb)
	{
		if (e.IsZero())
		{
			sb.Append(osUTF8Consts.XMLelement_category_id_Empty);
		}
		else
		{
			sb.Append(osUTF8Consts.XMLelement_category_id_Start);
			sb.AppendUUID(e);
			sb.Append(osUTF8Consts.XMLuuidEnd);
		}
	}

	public static void AddElem_creator_id(UUID e, osUTF8 sb)
	{
		if (e.IsZero())
		{
			sb.Append(osUTF8Consts.XMLelement_creator_id_Empty);
		}
		else
		{
			sb.Append(osUTF8Consts.XMLelement_creator_id_Start);
			sb.AppendUUID(e);
			sb.Append(osUTF8Consts.XMLuuidEnd);
		}
	}

	public static void AddElem_group_id(UUID e, osUTF8 sb)
	{
		if (e.IsZero())
		{
			sb.Append(osUTF8Consts.XMLelement_group_id_Empty);
		}
		else
		{
			sb.Append(osUTF8Consts.XMLelement_group_id_Start);
			sb.AppendUUID(e);
			sb.Append(osUTF8Consts.XMLuuidEnd);
		}
	}

	public static void AddElem_version(int v, osUTF8 sb)
	{
		if (v == 0)
		{
			sb.Append(osUTF8Consts.XMLelement_version_Empty);
		}
		else
		{
			sb.Append(osUTF8Consts.XMLelement_version_Start);
			sb.AppendInt(v);
			sb.Append(osUTF8Consts.XMLintegerEnd);
		}
	}

	public static void AddElem_sale_info(int price, byte type, osUTF8 sb)
	{
		if (price == 0 && type == 0)
		{
			sb.Append(osUTF8Consts.XMLelement_sale_info_Empty);
		}
		else
		{
			sb.Append(osUTF8Consts.XMLelement_sale_info_Start);
			sb.AppendInt(price);
			sb.Append(osUTF8Consts.XMLelement_sale_info_Mid);
			sb.AppendInt(type);
			sb.Append(osUTF8Consts.XMLelement_sale_info_End);
		}
	}


	public static byte[] ulongToByteArray(ulong uLongValue)
	{
		return new byte[8]
		{
			(byte)(uLongValue >> 56),
					(byte)(uLongValue >> 48),
					(byte)(uLongValue >> 40),
					(byte)(uLongValue >> 32),
					(byte)(uLongValue >> 24),
					(byte)(uLongValue >> 16),
					(byte)(uLongValue >> 8),
					(byte)uLongValue
		};
	}

	public static byte[] uintToByteArray(int value)
	{
		return new byte[4]
		(
			(byte)(value >> 24),
					(byte)(value >> 16),
					(byte)(value >> 8),
					(byte)value
		);
	}
}
