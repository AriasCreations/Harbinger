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
 * - Neither the name of the openmetaverse.org or libomv-java project nor the
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
package libomv.StructuredData;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import libomv.types.Quaternion;
import libomv.types.Vector2;
import libomv.types.Vector3;
import libomv.types.Vector3d;
import libomv.types.Vector4;

public class OSDMap extends OSD implements Map<String, OSD>
{
	private HashMap<String, OSD> value;

	@Override
	// OSD
	public OSDType getType()
	{
		return OSDType.Map;
	}

	public OSDMap()
	{
		this.value = new HashMap<String, OSD>();
	}

	public OSDMap(final int capacity)
	{
		this.value = new HashMap<String, OSD>(capacity);
	}

	public OSDMap(final HashMap<String, OSD> value)
	{
		if (null != value)
		{
			this.value = value;
		}
		else
		{
			this.value = new HashMap<String, OSD>();
		}
	}

	@Override
	// Map
	public final int size()
	{
		return this.value.size();
	}

	@Override
	public boolean isEmpty()
	{
		return 0 == value.size();
	}

	@Override
	public boolean AsBoolean()
	{
		return !this.isEmpty();
	}

	@Override
	public Vector2 AsVector2()
	{
		final Vector2 vec = new Vector2(Vector2.Zero);
		if (this.containsKey("X") && this.containsKey("Y"))
		{
			vec.X = (float) this.get("X").AsReal();
			vec.Y = (float) this.get("Y").AsReal();
		}
		return vec;
	}

	@Override
	public Vector3 AsVector3()
	{
		final Vector3 vec = new Vector3(Vector3.Zero);
		if (this.containsKey("X") && this.containsKey("Y") && this.containsKey("Z"))
		{
			vec.X = (float) this.get("X").AsReal();
			vec.Y = (float) this.get("Y").AsReal();
			vec.Z = (float) this.get("Z").AsReal();
		}
		return vec;
	}

	@Override
	public Vector3d AsVector3d()
	{
		final Vector3d vec = new Vector3d(Vector3d.Zero);
		if (this.containsKey("X") && this.containsKey("Y") && this.containsKey("Z"))
		{
			vec.X = this.get("X").AsReal();
			vec.Y = this.get("Y").AsReal();
			vec.Z = this.get("Z").AsReal();
		}
		return vec;
	}

	@Override
	public Vector4 AsVector4()
	{
		final Vector4 vector = new Vector4(Vector4.Zero);

		if (4 == this.size())
		{
			vector.X = (float) get("X").AsReal();
			vector.Y = (float) get("Y").AsReal();
			vector.Z = (float) get("Z").AsReal();
			vector.S = (float) get("S").AsReal();
		}
		return vector;
	}

	@Override
	public Quaternion AsQuaternion()
	{
		final Quaternion quaternion = new Quaternion(Quaternion.Identity);

		if (4 == this.size())
		{
			quaternion.X = (float) get("X").AsReal();
			quaternion.Y = (float) get("Y").AsReal();
			quaternion.Z = (float) get("Z").AsReal();
			quaternion.W = (float) get("W").AsReal();
		}
		return quaternion;
	}

	@Override
	public int hashCode()
	{
		return this.value.hashCode();
	}
	
	@Override
	public boolean equals(final Object obj)
	{
		return null != obj && obj instanceof OSD && this.equals((OSD)obj);
	}

	public boolean equals(final OSD osd)
	{
		return null != osd && OSDType.Map == osd.getType() && ((OSDMap)osd).value.equals(this.value);
	}

	@Override
	public OSD clone()
	{
		final OSDMap osd = (OSDMap)super.clone();
		osd.value = new HashMap<String, OSD>(value);
		return osd;
	}

	@Override
	public String toString()
	{
		try
		{
			return OSDParser.serializeToString(this, OSDFormat.Notation);
		}
		catch (final IOException e)
		{
			e.printStackTrace();
		}
		return null;
	}

	public final boolean getIsReadOnly()
	{
		return false;
	}

	@Override
	public final Set<String> keySet()
	{
		return this.value.keySet();
	}

	@Override
	public final Collection<OSD> values()
	{
		return this.value.values();
	}

	@Override
	public boolean containsKey(final Object key)
	{
		return this.value.containsKey(key);
	}

	@Override
	public final boolean containsValue(final Object osd)
	{
		return this.value.containsValue(osd);
	}

	@Override
	public final OSD get(final Object key)
	{
		final OSD osd = this.value.get(key);
		return null == osd ? new OSD() : osd;
	}

	@Override
	public final OSD put(final String key, final OSD val)
	{
		return this.value.put(key, val);
	}

	public final OSD put(final Entry<String, OSD> kvp)
	{
		return this.value.put(kvp.getKey(), kvp.getValue());
	}

	@Override
	public final OSD remove(final Object key)
	{
		return this.value.remove(key);
	}

	@Override
	public final void clear()
	{
		this.value.clear();
	}

	@Override
	public Set<Entry<String, OSD>> entrySet()
	{
		return this.value.entrySet();
	}

	@Override
	public void putAll(final Map<? extends String, ? extends OSD> m)
	{
		this.value.putAll(m);
	}

	/**
	 * Uses reflection to deserialize member variables in an object from this
	 * OSDMap
	 * 
	 * @param obj
	 *            Reference to an object to fill with deserialized values
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 */
	public Object deserializeMembers(final Object obj) throws IllegalArgumentException, IllegalAccessException
	{
		final Field[] fields = obj.getClass().getFields();

		for (final Field field : fields)
		{
			if (!Modifier.isTransient(field.getModifiers()))
			{
				final OSD serializedField = this.get(field.getName());
				if (null != serializedField)
				{
					field.set(obj, OSD.toObject(field.getType(), serializedField));
				}
			}
		}
		return obj;
	}
}
