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
package dev.zontreck.harbinger.thirdparty.libomv.StructuredData;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.Date;

import dev.zontreck.harbinger.thirdparty.libomv.types.Color4;
import dev.zontreck.harbinger.thirdparty.libomv.types.UUID;
import dev.zontreck.harbinger.thirdparty.libomv.types.Vector2;
import dev.zontreck.harbinger.thirdparty.libomv.types.Vector3;
import dev.zontreck.harbinger.thirdparty.libomv.types.Vector3d;
import dev.zontreck.harbinger.thirdparty.libomv.types.Vector4;
import dev.zontreck.harbinger.thirdparty.libomv.types.Quaternion;
import dev.zontreck.harbinger.thirdparty.libomv.utils.Helpers;

public class OSD implements Cloneable
{
	protected static final String FRACT_DATE_FMT = "yyyy-MM-dd'T'HH:mm:ss.SS'Z'";
	protected static final String WHOLE_DATE_FMT = "yyyy-MM-dd'T'HH:mm:ss'Z'";

	public enum OSDType
	{
		Unknown, Boolean, Integer, Real, String, UUID, Date, URI, Binary, Map, Array
	}

	public enum OSDFormat
	{
		Xml, Json, Notation, Binary;
		
		public static String contentType(final OSDFormat format)
		{
			switch (format)
			{
				case Xml:
					return "application/llsd+xml";
				case Binary:
					return "application/llsd+binary";
				case Notation:
					return "application/llsd+notation";
				default:
					break;
			}
			return "application/llsd+json";
		}
		
		public static String contentEncodingDefault(final OSDFormat format)
		{
			return Helpers.UTF8_ENCODING;
		}
	}
	
	public OSD()
	{
	}

	/** The OSD class implementation */
	public OSDType getType()
	{
		return OSDType.Unknown;
	}

	public boolean AsBoolean()
	{
		return false;
	}

	public int AsInteger()
	{
		return 0;
	}

	public int AsUInteger()
	{
		return 0;
	}

	public long AsLong()
	{
		return 0;
	}

	public long AsULong()
	{
		return 0;
	}

	public double AsReal()
	{
		return 0.0d;
	}

	public String AsString()
	{
		return "";
	}

	public UUID AsUUID()
	{
		return UUID.Zero;
	}

	public Date AsDate()
	{
		return Helpers.Epoch;
	}

	public URI AsUri()
	{
		return null;
	}

	public byte[] AsBinary()
	{
		return Helpers.EmptyBytes;
	}

	public InetAddress AsInetAddress()
	{
		try
		{
			return InetAddress.getByName("0.0.0.0");
		}
		catch (final UnknownHostException e)
		{
			return null;
		}
	}

	public Vector2 AsVector2()
	{
		return Vector2.Zero;
	}

	public Vector3 AsVector3()
	{
		return Vector3.Zero;
	}

	public Vector3d AsVector3d()
	{
		return Vector3d.Zero;
	}

	public Vector4 AsVector4()
	{
		return Vector4.Zero;
	}

	public Quaternion AsQuaternion()
	{
		return Quaternion.Identity;
	}

	public Color4 AsColor4()
	{
		return Color4.Black;
	}

	@Override
	public int hashCode()
	{
		return 0;
	}
	
	@Override
	public boolean equals(final Object obj)
	{
		return null != obj && obj instanceof OSD && this.equals((OSD)obj);
	}

	public boolean equals(final OSD osd)
	{
		return null != osd && OSDType.Unknown == osd.getType();
	}
	
	public OSD clone()
	{
		OSD osd = null;
	    try
	    {
	        osd = (OSD)super.clone();
	    }
	    catch (final CloneNotSupportedException e) { }
	    return osd;
	}

	@Override
	public String toString()
	{
		return "undef";
	}

	public static OSD FromBoolean(final boolean value)
	{
		return new OSDBoolean(value);
	}

	public static OSD FromInteger(final short value)
	{
		return new OSDInteger(value);
	}

	public static OSD FromInteger(final int value)
	{
		return new OSDInteger(value);
	}

	public static OSD FromUInteger(final int value)
	{
		return new OSDBinary(value & 0xffffffff);
	}

	public static OSD FromLong(final long value)
	{
		return new OSDBinary(value);
	}

	public static OSD FromULong(final long value)
	{
		return new OSDBinary(value & 0xffffffffffffffffL);
	}

	public static OSD FromReal(final double value)
	{
		return new OSDReal(value);
	}

	public static OSD FromReal(final float value)
	{
		return new OSDReal(value);
	}

	public static OSD FromString(final String value)
	{
		return new OSDString(value);
	}

	public static OSD FromString(final InetAddress value)
	{
		return new OSDString(value.getHostAddress());
	}

	public static OSD FromUUID(final String value)
	{
		if (36 <= value.length())
		{
			final UUID uuid = UUID.parse(value);
			if (null != uuid)
				return FromUUID(uuid);
		}
		return new OSDString(value);
	}

	public static OSD FromUUID(final UUID value)
	{
		return new OSDUUID(value);
	}

	public static OSD FromDate(final Date value)
	{
		return new OSDDate(value);
	}

	public static OSD FromUri(final URI value)
	{
		return new OSDUri(value);
	}

	public static OSD FromBinary(final byte[] value)
	{
		return new OSDBinary(value);
	}

	public static OSD FromBinary(final long value)
	{
		return new OSDBinary(value);
	}

	public static OSD FromBinary(final InetAddress value)
	{
		return new OSDBinary(value.getAddress());
	}

	public static OSD FromVector2(final Vector2 value)
	{
		final OSDArray array = new OSDArray();
		array.add(FromReal(value.X));
		array.add(FromReal(value.Y));
		return array;
	}

	public static OSD FromVector3(final Vector3 value)
	{
		final OSDArray array = new OSDArray();
		array.add(FromReal(value.X));
		array.add(FromReal(value.Y));
		array.add(FromReal(value.Z));
		return array;
	}

	public static OSD FromVector3d(final Vector3d value)
	{
		final OSDArray array = new OSDArray();
		array.add(FromReal(value.X));
		array.add(FromReal(value.Y));
		array.add(FromReal(value.Z));
		return array;
	}

	public static OSD FromVector4(final Vector4 value)
	{
		final OSDArray array = new OSDArray();
		array.add(FromReal(value.X));
		array.add(FromReal(value.Y));
		array.add(FromReal(value.Z));
		array.add(FromReal(value.S));
		return array;
	}

	public static OSD FromQuaternion(final Quaternion value)
	{
		final OSDArray array = new OSDArray();
		array.add(FromReal(value.X));
		array.add(FromReal(value.Y));
		array.add(FromReal(value.Z));
		array.add(FromReal(value.W));
		return array;
	}

	public static OSD FromColor4(final Color4 value)
	{
		final OSDArray array = new OSDArray();
		array.add(FromReal(value.R));
		array.add(FromReal(value.G));
		array.add(FromReal(value.B));
		array.add(FromReal(value.A));
		return array;
	}

	public static OSD FromObject(final Object value)
	{
		if (null == value)
		{
			return new OSD();
		}
		if (value instanceof OSD)
		{
			return (OSD)value;
		}
		if (value instanceof Boolean)
		{
			return new OSDBoolean((Boolean) value);
		}
		if (value instanceof Integer)
		{
			return new OSDInteger((Integer) value);
		}
		if (value instanceof Short)
		{
			return new OSDInteger(((Short)value).intValue());
		}
		if (value instanceof Byte)
		{
			return new OSDInteger(((Byte)value).intValue());
		}
		if (value instanceof Double)
		{
			return new OSDReal(((Double)value).doubleValue());
		}
		if (value instanceof Float)
		{
			return new OSDReal(((Float)value).doubleValue());
		}
		if (value instanceof String)
		{
			return new OSDString((String)value);
		}
		if (value instanceof UUID)
		{
			return new OSDUUID((UUID)value);
		}
		if (value instanceof Date)
		{
			return new OSDDate((Date)value);
		}
		if (value instanceof URI)
		{
			return new OSDUri((URI)value);
		}
		if (value instanceof byte[])
		{
			return new OSDBinary((byte[])value);
		}
		if (value instanceof Long)
		{
			return new OSDBinary((Long)value);
		}
		if (value instanceof Vector2)
		{
			return OSD.FromVector2((Vector2)value);
		}
		if (value instanceof Vector3)
		{
			return OSD.FromVector3((Vector3)value);
		}
		if (value instanceof Vector3d)
		{
			return OSD.FromVector3d((Vector3d)value);
		}
		if (value instanceof Vector4)
		{
			return OSD.FromVector4((Vector4)value);
		}
		if (value instanceof Quaternion)
		{
			return OSD.FromQuaternion((Quaternion)value);
		}
		if (value instanceof Color4)
		{
			return OSD.FromColor4((Color4)value);
		}
		// We don't know this type
		return new OSD();
	}

	protected static Object toObject(final Class<?> type, final OSD value)
	{
		if (null == type || null == value)
		{
			return null;
		}
		else if (type.isAssignableFrom(Long.class) || long.class == type)
		{
			return value.AsLong();
		}
		else if (type.isAssignableFrom(Integer.class) || int.class == type)
		{
			return value.AsInteger();
		}
		else if (type.isAssignableFrom(Short.class) || short.class == type)
		{
			return (short) value.AsInteger();
		}
		else if (type.isAssignableFrom(Byte.class) || byte.class == type)
		{
			return (byte) value.AsInteger();
		}
		else if (type.isAssignableFrom(Boolean.class) || boolean.class == type)
		{
			return value.AsBoolean();
		}
		else if (type.isAssignableFrom(Double.class) || double.class == type)
		{
			return value.AsReal();
		}
		else if (type.isAssignableFrom(Float.class) || float.class == type)
		{
			return (float) value.AsReal();
		}
		else if (type.isAssignableFrom(String.class))
		{
			return value.AsString();
		}
		else if (type.isAssignableFrom(Date.class))
		{
			return value.AsDate();
		}
		else if (type.isAssignableFrom(URI.class))
		{
			return value.AsUri();
		}
		else if (type.isAssignableFrom(UUID.class))
		{
			return value.AsUUID();
		}
		else if (type.isAssignableFrom(Vector3.class))
		{
			if (OSDType.Array == value.getType())
			{
				return value.AsVector3();
			}
			return Vector3.Zero;
		}
		else if (type.isAssignableFrom(Vector4.class))
		{
			if (OSDType.Array == value.getType())
			{
				return value.AsVector4();
			}
			return Vector4.Zero;
		}
		else if (type.isAssignableFrom(Quaternion.class))
		{
			if (OSDType.Array == value.getType())
			{
				return value.AsQuaternion();
			}
			return Quaternion.Identity;
		}
		else if (type.isAssignableFrom(OSDArray.class))
        {
            final OSDArray newArray = new OSDArray();
            for (final OSD o : (OSDArray)value)
                newArray.add(o);
            return newArray;
        }
		else if (type.isAssignableFrom(OSDMap.class))
		{
			final OSDMap old = (OSDMap)value;
			final OSDMap newMap = new OSDMap();
			for (final String key : ((OSDMap)value).keySet())
                newMap.put(key, old.get(key));
            return newMap;
        }
		// We don't know this type
		return null;
	}

	/**
	 * Uses reflection to create an OSDMap from all of the OSD serializable types
	 * in an object
	 * 
	 * @param obj
	 *            Class or struct containing serializable types
	 * @return An SDMap holding the serialized values from the container object
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 */
	public static OSDMap serializeMembers(final Object obj) throws IllegalArgumentException, IllegalAccessException
	{
		final Field[] fields = obj.getClass().getFields();
		final OSDMap map = new OSDMap(fields.length);
		for (final Field field : fields)
		{
			if (!Modifier.isTransient(field.getModifiers()))
			{
				final OSD serializedField = FromObject(field.get(obj));

				if (OSDType.Unknown != serializedField.getType())
				{
					map.put(field.getName(), serializedField);
				}
			}
		}
		return map;
	}

	/**
	 * Uses reflection to deserialize member variables in an object from an
	 * OSDMap
	 * 
	 * @param obj
	 *            Reference to an object to fill with deserialized values
	 * @param serialized
	 *            Serialized values to put in the target object
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 */
	public static Object deserializeMembers(final Object obj, final OSDMap serialized) throws IllegalArgumentException,
			IllegalAccessException
	{
		for (final Field field : obj.getClass().getFields())
		{
			if (!Modifier.isTransient(field.getModifiers()))
			{
				final OSD serializedField = serialized.get(field.getName());
				if (null != serializedField)
				{
					field.set(obj, OSD.toObject(field.getClass(), serializedField));
				}
			}
		}
		return obj;
	}
}