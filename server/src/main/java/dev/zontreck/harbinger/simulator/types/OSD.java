package dev.zontreck.harbinger.simulator.types;


import dev.zontreck.harbinger.simulator.types.enums.OSDType;
import dev.zontreck.harbinger.utils.SimUtils;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;

public class OSD {
	protected static final byte[] trueBinary = {0x31};
	protected static final byte[] falseBinary = {0x30};

	public OSDType Type = OSDType.Unknown;

	// .net4.8 64Bit JIT fails polimorphism
	public boolean AsBoolean() {
		switch (Type) {
			case OSDType.Boolean:
				return ((OSDBoolean) this).value;
			case OSDType.Integer:
				return ((OSDInteger) this).value != 0;
			case OSDType.Real:
				var d = ((OSDReal) this).value;
				return !double.IsNaN(d) && d != 0;
			case OSDType.String:
				var s = ((OSDString) this).value;
				if (string.IsNullOrEmpty(s))
					return false;
				return s != "0" && s.ToLower() != "false";
			return true;
			case OSDType.UUID:
				return !((OSDUUID) this).value.IsZero();
			case OSDType.Map:
				return ((OSDMap) this).dicvalue.Count > 0;
			case OSDType.Array:
				return ((OSDArray) this).value.Count > 0;
			case OSDType.OSDUTF8:
				var u = ((OSDUTF8) this).value;
				if (osUTF8.IsNullOrEmpty(u))
					return false;
				return !u.Equals('0') && !u.ACSIILowerEquals("false");
			return true;

			default:
				return false;
		}
	}

	public int AsInteger() {
		switch (Type) {
			case OSDType.Boolean:
				return ((OSDBoolean) this).value ? 1 : 0;
			case OSDType.Integer:
				return ((OSDInteger) this).value;
			case OSDType.Real:
				var v = ((OSDReal) this).value;
				if (double.IsNaN(v))
					return 0;
				if (v >= int.MaxValue)
					return int.MaxValue;
				if (v <= int.MinValue)
					return int.MinValue;
				return (int) Math.Round(v);
			case OSDType.String:
				var s = ((OSDString) this).value.AsSpan();
				if (double.TryParse(s, out var dbl))
					return (int) Math.Floor(dbl);
				return 0;
			case OSDType.OSDUTF8:
				var us = ((OSDUTF8) this).value.ToString().AsSpan();
				if (double.TryParse(us, out var udbl))
					return (int) Math.Floor(udbl);
				return 0;
			case OSDType.Binary:
				var b = ((OSDBinary) this).value;
				if (b.Length < 4)
					return 0;
				return (b[0] << 24) | (b[1] << 16) | (b[2] << 8) | b[3];
			case OSDType.Array:
				var l = ((OSDArray) this).value;
				if (l.Count < 4)
					return 0;
				return
						((byte) l[0].AsInteger() << 24) |
								((byte) l[1].AsInteger() << 16) |
								((byte) l[2].AsInteger() << 8) |
								l[3].AsInteger();

			case OSDType.Date:
				return (int) Utils.DateTimeToUnixTime(((OSDDate) this).value);
			default:
				return 0;
		}
	}

	public virtual

	long AsLong() {
		switch (Type) {
			case OSDType.Boolean:
				return ((OSDBoolean) this).value ? 1 : 0;
			case OSDType.Integer:
				return ((OSDInteger) this).value;
			case OSDType.Real:
				var v = ((OSDReal) this).value;
				if (double.IsNaN(v))
					return 0;
				if (v > long.MaxValue)
					return long.MaxValue;
				if (v < long.MinValue)
					return long.MinValue;
				return (long) Math.Round(v);
			case OSDType.String:
				var s = ((OSDString) this).value.AsSpan();
				if (double.TryParse(s, out var dbl))
					return (long) Math.Floor(dbl);
				return 0;
			case OSDType.OSDUTF8:
				var us = ((OSDUTF8) this).value.ToString().AsSpan();
				if (double.TryParse(us, out var udbl))
					return (long) Math.Floor(udbl);
				return 0;
			case OSDType.Date:
				return Utils.DateTimeToUnixTime(((OSDDate) this).value);
			case OSDType.Binary: {
				var b = ((OSDBinary) this).value;
				if (b.Length < 8)
					return 0;
				return ((long) b[0] << 56) |
						((long) b[1] << 48) |
						((long) b[2] << 40) |
						((long) b[3] << 32) |
						((long) b[4] << 24) |
						((long) b[5] << 16) |
						((long) b[6] << 8) |
						b[7];
			}
			case OSDType.Array: {
				var l = ((OSDArray) this).value;
				if (l.Count < 8)
					return 0;
				return
						((long) (byte) l[0].AsInteger() << 56) |
								((long) (byte) l[1].AsInteger() << 48) |
								((long) (byte) l[2].AsInteger() << 40) |
								((long) (byte) l[3].AsInteger() << 32) |
								((long) (byte) l[4].AsInteger() << 24) |
								((long) (byte) l[5].AsInteger() << 16) |
								((long) (byte) l[6].AsInteger() << 8) |
								l[7].AsInteger();
			}
			default:
				return 0;
		}
	}

	public double AsReal() {
		switch (Type) {
			case OSDType.Boolean:
				return ((OSDBoolean) this).value ? 1.0 : 0;
			case OSDType.Integer:
				return ((OSDInteger) this).value;
			case OSDType.Real:
				return ((OSDReal) this).value;
			case OSDType.String:
				var s = ((OSDString) this).value.AsSpan();
				if (double.TryParse(s, out var dbl))
					return dbl;
				return 0;
			case OSDType.OSDUTF8:
				var us = ((OSDUTF8) this).value.ToString().AsSpan();
				if (double.TryParse(us, out var udbl))
					return udbl;
				return 0;
			default:
				return 0;
		}
	}

	public String AsString() {
		switch (Type) {
			case OSDType.Boolean:
				return ((OSDBoolean) this).value ? "1" : "0";
			case OSDType.Integer:
				return ((OSDInteger) this).value.ToString();
			case OSDType.Real:
				return ((OSDReal) this).value.ToString("r", Utils.EnUsCulture);
			case OSDType.String:
				return ((OSDString) this).value;
			case OSDType.OSDUTF8:
				return ((OSDUTF8) this).value.ToString();
			case OSDType.UUID:
				return ((OSDUUID) this).value.ToString();
			case OSDType.Date:
				string format;
				var dt = ((OSDDate) this).value;
				if (dt.Millisecond > 0)
					format = "yyyy-MM-ddTHH:mm:ss.ffZ";
				else
					format = "yyyy-MM-ddTHH:mm:ssZ";
				return dt.ToUniversalTime().ToString(format);
			case OSDType.URI:
				var ur = ((OSDUri) this).value;
				if (ur == null)
					return string.Empty;
				if (ur.IsAbsoluteUri)
					return ur.AbsoluteUri;
				return ur.ToString();

			case OSDType.Binary:
				var b = ((OSDBinary) this).value;
				return Convert.ToBase64String(b);
			case OSDType.LLSDxml:
				return ((OSDllsdxml) this).value;
			default:
				return string.Empty;
		}
	}

	public UUID AsUUID() {
		switch (Type) {
			case OSDType.String:
				if (UUID.TryParse(((OSDString) this).value.AsSpan(), out var uuid))
					return uuid;
				return UUID.Zero;
			case OSDType.OSDUTF8:
				UUID ouuid;
				if (UUID.TryParse(((OSDUTF8) this).value.ToString().AsSpan(), out ouuid))
					return ouuid;
				return UUID.Zero;
			case OSDType.UUID:
				return ((OSDUUID) this).value;
			default:
				return new UUID(0, 0);
		}
	}

	public Date AsDate() {
		switch (Type) {
			case OSDType.String:
				Date dt;
				if (Date.TryParse(((OSDString) this).value, out dt))
					return dt;
				return SimUtils.Epoch;
			case OSDType.OSDUTF8:
				Date odt;
				if (Date.TryParse(((OSDUTF8) this).value.ToString(), out odt))
					return odt;
				return SimUtils.Epoch;
			case OSDType.UUID:
			case OSDType.Date:
				return ((OSDDate) this).value;
			default:
				return SimUtils.Epoch;
		}
	}

	public URI AsUri() {
		switch (Type) {
			case OSDType.String:
				URI uri;
				if (URI.TryCreate(((OSDString) this).value, UriKind.RelativeOrAbsolute, out uri))
					return uri;
				return null;
			case OSDType.OSDUTF8:
				URI ouri;
				if (URI.TryCreate(((OSDUTF8) this).value.ToString(), UriKind.RelativeOrAbsolute, out ouri))
					return ouri;
				return null;
			case OSDType.URI:
				return ((OSDUri) this).value;
			default:
				return null;
		}
	}

	public byte[] AsBinary() {
		switch (Type) {
			case OSDType.Boolean:
				return ((OSDBoolean) this).value ? trueBinary : falseBinary;
			case OSDType.Integer:
				return SimUtils.IntToBytesBig(((OSDInteger) this).value);
			case OSDType.Real:
				return SimUtils.DoubleToBytesBig(((OSDReal) this).value);
			case OSDType.String:
				return (((OSDString) this).value).getBytes(StandardCharsets.UTF_8);
			case OSDType.OSDUTF8:
				return ((OSDUTF8) this).value.ToArray();
			case OSDType.UUID:
				return ((OSDUUID) this).value.GetBytes();
			case OSDType.Date:
				var ts = ((OSDDate) this).value.ToUniversalTime() - new Date(1970, 1, 1, 0, 0, 0, DateTimeKind.Utc);
				return SimUtils.DoubleToBytes(ts.TotalSeconds);
			case OSDType.URI:
				return (this.AsString()).getBytes(StandardCharsets.UTF_8);
			case OSDType.Binary:
				return ((OSDBinary) this).value;
			case OSDType.Map:
			case OSDType.Array:
				var l = ((OSDArray) this).value;
				var binary = new byte[l.Count];
				for (var i = 0; i < l.Count; i++)
					binary[i] = (byte) l[i].AsInteger();
				return binary;
			case OSDType.LLSDxml:
				return Encoding.UTF8.GetBytes(((OSDllsdxml) this).value);
			default:
				return Array.Empty < byte>();
		}
	}

	public Vector2 AsVector2() {
		switch (Type) {
			case OSDType.String:
				return Vector2.Parse(((OSDString) this).value);
			case OSDType.OSDUTF8:
				return Vector2.Parse(((OSDUTF8) this).value.ToString());
			case OSDType.Array:
				var l = ((OSDArray) this).value;
				var vector = Vector2.Zero;
				if (l.Count == 2) {
					vector.X = (float) l[0].AsReal();
					vector.Y = (float) l[1].AsReal();
				}

				return vector;
			default:
				return Vector2.Zero;
		}
	}

	public Vector3 AsVector3() {
		switch (Type) {
			case OSDType.String:
				return Vector3.Parse(((OSDString) this).value.AsSpan());
			case OSDType.OSDUTF8:
				return Vector3.Parse(((OSDUTF8) this).value.ToString().AsSpan());
			case OSDType.Array:
				var l = ((OSDArray) this).value;
				if (l.Count == 3)
					return new Vector3(
							(float) l[0].AsReal(),
							(float) l[1].AsReal(),
							(float) l[2].AsReal());
				return Vector3.Zero;
			default:
				return Vector3.Zero;
		}
	}

	public Vector3d AsVector3d() {
		switch (Type) {
			case OSDType.String:
				return Vector3d.Parse(((OSDString) this).value.AsSpan());
			case OSDType.OSDUTF8:
				return Vector3d.Parse(((OSDUTF8) this).value.ToString().AsSpan());
			case OSDType.Array:
				var l = ((OSDArray) this).value;
				var vector = Vector3d.Zero;
				if (l.Count == 3) {
					vector.X = (float) l[0].AsReal();
					vector.Y = (float) l[1].AsReal();
					vector.Z = (float) l[2].AsReal();
				}

				return vector;
			default:
				return Vector3d.Zero;
		}
	}

	public Vector4 AsVector4() {
		switch (Type) {
			case OSDType.String:
				return Vector4.Parse(((OSDString) this).value);
			case OSDType.OSDUTF8:
				return Vector4.Parse(((OSDUTF8) this).value.ToString());
			case OSDType.Array:
				var l = ((OSDArray) this).value;
				var vector = Vector4.Zero;
				if (l.Count == 4) {
					vector.X = (float) l[0].AsReal();
					vector.Y = (float) l[1].AsReal();
					vector.Z = (float) l[2].AsReal();
					vector.W = (float) l[3].AsReal();
				}

				return vector;
			default:
				return Vector4.Zero;
		}
	}

	public Quaternion AsQuaternion() {
		switch (Type) {
			case OSDType.String:
				return Quaternion.Parse(((OSDString) this).value);
			case OSDType.OSDUTF8:
				return Quaternion.Parse(((OSDString) this).value);
			case OSDType.Array:
				var l = ((OSDArray) this).value;
				var q = Quaternion.Identity;
				if (l.Count == 4) {
					q.X = (float) l[0].AsReal();
					q.Y = (float) l[1].AsReal();
					q.Z = (float) l[2].AsReal();
					q.W = (float) l[3].AsReal();
				}

				return q;
			default:
				return Quaternion.Identity;
		}
	}

	public Color4 AsColor4() {
		if (Objects.requireNonNull(Type) == OSDType.OSDType.Array) {
			var l = ((OSDArray) this).value;
			var color = Color4.Black;
			if (l.Count == 4) {
				color.R = (float) l[0].AsReal();
				color.G = (float) l[1].AsReal();
				color.B = (float) l[2].AsReal();
				color.A = (float) l[3].AsReal();
			}

			return color;
		}
		return Color4.Black;
	}

	public void Clear() {
	}

	public OSD Copy() {
		switch (Type) {
			case OSDType.Boolean:
				return new OSDBoolean(((OSDBoolean) this).value);
			case OSDType.Integer:
				return new OSDInteger(((OSDInteger) this).value);
			case OSDType.Real:
				return new OSDReal(((OSDReal) this).value);
			case OSDType.String:
				return new OSDString(((OSDString) this).value);
			case OSDType.OSDUTF8:
				return new OSDUTF8(((OSDUTF8) this).value);
			case OSDType.UUID:
				return new OSDUUID(((OSDUUID) this).value);
			case OSDType.Date:
				return new OSDDate(((OSDDate) this).value);
			case OSDType.URI:
				return new OSDUri(((OSDUri) this).value);
			case OSDType.Binary:
				return new OSDBinary(((OSDBinary) this).value);
			case OSDType.Map:
				return new OSDMap(((OSDMap) this).dicvalue);
			case OSDType.Array:
				return new OSDArray(((OSDArray) this).value);
			case OSDType.LLSDxml:
				return new OSDBoolean(((OSDBoolean) this).value);
			default:
				return new OSD();
		}
	}

	@Override
	public String toString() {
		switch (Type) {
			case OSDType.Boolean:
				return ((OSDBoolean) this).value ? "1" : "0";
			case OSDType.Integer:
				return ((OSDInteger) this).value.toString();
			case OSDType.Real:
				return ((OSDReal) this).value.toString("r", SimUtils.EnUsCulture);
			case OSDType.String:
				return ((OSDString) this).value;
			case OSDType.OSDUTF8:
				return ((OSDUTF8) this).value.toString();
			case OSDType.UUID:
				return ((OSDUUID) this).value.toString();
			case OSDType.Date:
				String format;
				var dt = ((OSDDate) this).value;
				if (dt.Millisecond > 0)
					format = "yyyy-MM-ddTHH:mm:ss.ffZ";
				else
					format = "yyyy-MM-ddTHH:mm:ssZ";
				return dt.ToUniversalTime().ToString(format);
			case OSDType.URI:
				var ur = ((OSDUri) this).value;
				if (ur == null)
					return string.Empty;
				if (ur.IsAbsoluteUri)
					return ur.AbsoluteUri;
				return ur.ToString();
			case OSDType.Binary:
				return Utils.BytesToHexString(((OSDBinary) this).value, null);
			case OSDType.LLSDxml:
				return ((OSDllsdxml) this).value;
			case OSDType.Map:
				return OSDParser.SerializeJsonString((OSDMap) this, true);
			case OSDType.Array:
				return OSDParser.SerializeJsonString((OSDArray) this, true);
			default:
				return "undef";
		}
	}

	public static OSD FromBoolean(boolean value) {
		return new OSDBoolean(value);
	}

	public static OSD FromInteger(int value) {
		return new OSDInteger(value);
	}

	public static OSD FromInteger(short value) {
		return new OSDInteger(value);
	}

	public static OSD FromInteger(byte value) {
		return new OSDInteger(value);
	}

	public static OSD FromLong(long value) {
		return new OSDBinary(value);
	}

	public static OSD FromReal(double value) {
		return new OSDReal(value);
	}

	public static OSD FromReal(float value) {
		return new OSDReal(value);
	}

	public static OSD FromString(String value) {
		return new OSDString(value);
	}

	public static OSD FromUUID(UUID value) {
		return new OSDUUID(value);
	}

	public static OSD FromDate(Date value) {
		return new OSDDate(value);
	}

	public static OSD FromUri(URI value) {
		return new OSDUri(value);
	}

	public static OSD FromBinary(byte[] value) {
		return new OSDBinary(value);
	}

	public static OSD FromVector2(Vector2 value) {
		var array = new OSDArray();
		array.Add(FromReal(value.X));
		array.Add(FromReal(value.Y));
		return array;
	}

	public static OSD FromVector3(Vector3 value) {
		var array = new OSDArray();
		array.Add(FromReal(value.X));
		array.Add(FromReal(value.Y));
		array.Add(FromReal(value.Z));
		return array;
	}

	public static OSD FromVector3d(Vector3d value) {
		var array = new OSDArray();
		array.Add(FromReal(value.X));
		array.Add(FromReal(value.Y));
		array.Add(FromReal(value.Z));
		return array;
	}

	public static OSD FromVector4(Vector4 value) {
		var array = new OSDArray();
		array.Add(FromReal(value.X));
		array.Add(FromReal(value.Y));
		array.Add(FromReal(value.Z));
		array.Add(FromReal(value.W));
		return array;
	}

	public static OSD FromQuaternion(Quaternion value) {
		var array = new OSDArray();
		array.Add(FromReal(value.X));
		array.Add(FromReal(value.Y));
		array.Add(FromReal(value.Z));
		array.Add(FromReal(value.W));
		return array;
	}

	public static OSD FromColor4(Color4 value) {
		var array = new OSDArray();
		array.Add(FromReal(value.R));
		array.Add(FromReal(value.G));
		array.Add(FromReal(value.B));
		array.Add(FromReal(value.A));
		return array;
	}

	public static OSD FromObject(Object value) {
		if (value == null)
			return new OSD();
		if (value is boolean)
		return new OSDBoolean((bool) value);
		if (value is int)
		return new OSDInteger((int) value);
		if (value is uint)
		return new OSDBinary((uint) value);
		if (value is short)
		return new OSDInteger((short) value);
		if (value is ushort)
		return new OSDInteger((ushort) value);
		if (value is sbyte)
		return new OSDInteger((sbyte) value);
		if (value is byte)
		return new OSDInteger((byte) value);
		if (value is double)
		return new OSDReal((double) value);
		if (value is float)
		return new OSDReal((float) value);
		if (value is string)
		return new OSDString((string) value);
		if (value is UUID)
		return new OSDUUID((UUID) value);
		if (value is DateTime)
		return new OSDDate((DateTime) value);
		if (value is Uri)
		return new OSDUri((Uri) value);
		if (value is byte[])
		return new OSDBinary((byte[]) value);
		if (value is long)
		return new OSDBinary((long) value);
		if (value is ulong)
		return new OSDBinary((ulong) value);
		if (value is Vector2)
		return FromVector2((Vector2) value);
		if (value is Vector3)
		return FromVector3((Vector3) value);
		if (value is Vector3d)
		return FromVector3d((Vector3d) value);
		if (value is Vector4)
		return FromVector4((Vector4) value);
		if (value is Quaternion)
		return FromQuaternion((Quaternion) value);
		if (value is Color4)
		return FromColor4((Color4) value);
		return new OSD();
	}

	public static Object ToObject(Class<?> type, OSD value) {
		if (type == typeof(ulong)) {
			if (value.Type == OSDType.Binary) {
				var bytes = value.AsBinary();
				return Utils.BytesToUInt64(bytes);
			}

			return value.AsInteger();
		}

		if (type == typeof(uint)) {
			if (value.Type == OSDType.Binary) {
				var bytes = value.AsBinary();
				return Utils.BytesToUInt(bytes);
			}

			return value.AsInteger();
		}

		if (type == typeof(ushort))
			return value.AsInteger();

		if (type == typeof( byte))
		return (byte) value.AsInteger();

		if (type == typeof( short))
		return (short) value.AsInteger();

		if (type == typeof(string)) return value.AsString();

		if (type == typeof(bool)) return value.AsBoolean();

		if (type == typeof( float))
		return (float) value.AsReal();

		if (type == typeof( double))return value.AsReal();

		if (type == typeof( int))return value.AsInteger();

		if (type == typeof(UUID)) return value.AsUUID();

		if (type == typeof(Vector3)) {
			if (value.Type == OSDType.Array)
				return value.AsVector3();
			return Vector3.Zero;
		}

		if (type == typeof(Vector4)) {
			if (value.Type == OSDType.Array)
				return value.AsVector4();
			return Vector4.Zero;
		}

		if (type == typeof(Quaternion)) {
			if (value.Type == OSDType.Array)
				return value.AsQuaternion();
			return Quaternion.Identity;
		}

		if (type == typeof(OSDArray)) {
			var newArray = new OSDArray();
			foreach(var o in(OSDArray)value)
			newArray.Add(o);
			return newArray;
		}

		if (type == typeof(OSDMap)) {
			var newMap = new OSDMap();
			foreach(KeyValuePair < string, OSD > o in(OSDMap)value)
			newMap.Add(o);
			return newMap;
		}

		return null;
	}

	/// <summary>
	///     Uses reflection to create an SDMap from all of the SD
	///     serializable types in an object
	/// </summary>
	/// <param name="obj">Class or struct containing serializable types</param>
	/// <returns>
	///     An SDMap holding the serialized values from the
	///     container object
	/// </returns>
	public static OSDMap SerializeMembers(Object obj) {
		var t = obj.GetType();
		var fields = t.GetFields();

		var map = new OSDMap(fields.Length);

		for (var i = 0; i < fields.Length; i++) {
			var field = fields[i];
			if (!Attribute.IsDefined(field, typeof(NonSerializedAttribute))) {
				var serializedField = FromObject(field.GetValue(obj));

				if (serializedField.Type != OSDType.Unknown || field.FieldType == typeof(string) ||
						field.FieldType == typeof( byte[]))
				map.Add(field.Name, serializedField);
			}
		}

		return map;
	}

	/// <summary>
	///     Uses reflection to deserialize member variables in an object from
	///     an SDMap
	/// </summary>
	/// <param name="obj">
	///     Reference to an object to fill with deserialized
	///     values
	/// </param>
	/// <param name="serialized">
	///     Serialized values to put in the target
	///     object
	/// </param>
	public static void DeserializeMembers(Object obj, OSDMap serialized) {
		var t = obj.GetType();
		var fields = t.GetFields();

		for (var i = 0; i < fields.Length; i++) {
			var field = fields[i];
			if (!Attribute.IsDefined(field, typeof(NonSerializedAttribute))) {
				OSD serializedField;
				if (serialized.TryGetValue(field.Name, out serializedField))
					field.SetValue(obj, ToObject(field.FieldType, serializedField));
			}
		}
	}


}
