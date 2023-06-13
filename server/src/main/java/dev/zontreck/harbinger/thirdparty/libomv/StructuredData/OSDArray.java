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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import dev.zontreck.harbinger.thirdparty.libomv.types.Color4;
import dev.zontreck.harbinger.thirdparty.libomv.types.Quaternion;
import dev.zontreck.harbinger.thirdparty.libomv.types.Vector2;
import dev.zontreck.harbinger.thirdparty.libomv.types.Vector3;
import dev.zontreck.harbinger.thirdparty.libomv.types.Vector3d;
import dev.zontreck.harbinger.thirdparty.libomv.types.Vector4;

public class OSDArray extends OSD implements List<OSD>
{
	private ArrayList<OSD> value;

	@Override
	public OSDType getType()
	{
		return OSDType.Array;
	}

	public OSDArray()
	{
		this.value = new ArrayList<OSD>();
	}

	public OSDArray(final int capacity)
	{
		this.value = new ArrayList<OSD>(capacity);
	}

	public OSDArray(final ArrayList<OSD> value)
	{
		this.value = new ArrayList<OSD>(value);
	}

	@Override
	public boolean add(final OSD osd)
	{
		return this.value.add(osd);
	}

	@Override
	public void add(final int index, final OSD osd)
	{
		this.value.add(index, osd);
	}

	@Override
	public boolean addAll(final Collection<? extends OSD> coll)
	{
		return this.value.addAll(coll);
	}

	@Override
	public boolean addAll(final int index, final Collection<? extends OSD> coll)
	{
		return this.value.addAll(index, coll);
	}

	@Override
	public final void clear()
	{
		this.value.clear();
	}

	@Override
	public boolean contains(final Object obj)
	{
		return this.value.contains(obj);
	}

	public final boolean contains(final String element)
	{
		for (int i = 0; i < this.value.size(); i++)
		{
			if (OSDType.String == value.get(i).getType() && this.value.get(i).AsString() == element)
			{
				return true;
			}
		}

		return false;
	}

	@Override
	public boolean containsAll(final Collection<?> objs)
	{
		return this.value.containsAll(objs);
	}

	@Override
	public OSD get(final int index)
	{
		return this.value.get(index);
	}

	@Override
	public int indexOf(final Object obj)
	{
		return this.value.indexOf(obj);
	}

	@Override
	public boolean isEmpty()
	{
		return 0 == size();
	}

	@Override
	public Iterator<OSD> iterator()
	{
		return this.value.iterator();
	}

	@Override
	public int lastIndexOf(final Object obj)
	{
		return this.value.lastIndexOf(obj);
	}

	@Override
	public ListIterator<OSD> listIterator()
	{
		return this.value.listIterator();
	}

	@Override
	public ListIterator<OSD> listIterator(final int index)
	{
		return this.value.listIterator(index);
	}

	@Override
	public boolean remove(final Object key)
	{
		return this.value.remove(key);
	}

	public final boolean remove(final OSD osd)
	{
		return this.value.remove(osd);
	}

	@Override
	public OSD remove(final int key)
	{
		return this.value.remove(key);
	}

	@Override
	public boolean removeAll(final Collection<?> values)
	{
		return this.value.removeAll(values);
	}

	@Override
	public boolean retainAll(final Collection<?> values)
	{
		return this.value.retainAll(values);
	}

	@Override
	public OSD set(final int index, final OSD osd)
	{
		return this.value.set(index, osd);
	}

	@Override
	public int size()
	{
		return this.value.size();
	}

	@Override
	public List<OSD> subList(final int from, final int to)
	{
		return this.value.subList(from, to);
	}

	@Override
	public Object[] toArray()
	{
		return this.value.toArray();
	}

	@Override
	public <T> T[] toArray(final T[] arg)
	{
		return this.value.toArray(arg);
	}

	@Override
	public byte[] AsBinary()
	{
		final byte[] binary = new byte[this.value.size()];

		for (int i = 0; i < this.value.size(); i++)
		{
			binary[i] = (byte) this.value.get(i).AsInteger();
		}
		return binary;
	}

	@Override
	public long AsLong()
	{
		final OSDBinary binary = new OSDBinary(this.AsBinary());
		return binary.AsLong();
	}

	@Override
	public long AsULong()
	{
		final OSDBinary binary = new OSDBinary(this.AsBinary());
		return binary.AsULong();
	}

	@Override
	public int AsUInteger()
	{
		final OSDBinary binary = new OSDBinary(this.AsBinary());
		return binary.AsUInteger();
	}

	@Override
	public Vector2 AsVector2()
	{
		final Vector2 vector = new Vector2(Vector2.Zero);

		if (2 == this.size())
		{
			vector.X = (float) get(0).AsReal();
			vector.Y = (float) get(1).AsReal();
		}

		return vector;
	}

	@Override
	public Vector3 AsVector3()
	{
		final Vector3 vector = new Vector3(Vector3.Zero);

		if (3 == this.size())
		{
			vector.X = (float) get(0).AsReal();
			vector.Y = (float) get(1).AsReal();
			vector.Z = (float) get(2).AsReal();
		}

		return vector;
	}

	@Override
	public Vector3d AsVector3d()
	{
		final Vector3d vector = new Vector3d(Vector3d.Zero);

		if (3 == this.size())
		{
			vector.X = get(0).AsReal();
			vector.Y = get(1).AsReal();
			vector.Z = get(2).AsReal();
		}

		return vector;
	}

	@Override
	public Vector4 AsVector4()
	{
		final Vector4 vector = new Vector4(Vector4.Zero);

		if (4 == this.size())
		{
			vector.X = (float) get(0).AsReal();
			vector.Y = (float) get(1).AsReal();
			vector.Z = (float) get(2).AsReal();
			vector.S = (float) get(3).AsReal();
		}
		return vector;
	}

	@Override
	public Quaternion AsQuaternion()
	{
		final Quaternion quaternion = new Quaternion(Quaternion.Identity);

		if (4 == this.size())
		{
			quaternion.X = (float) get(0).AsReal();
			quaternion.Y = (float) get(1).AsReal();
			quaternion.Z = (float) get(2).AsReal();
			quaternion.W = (float) get(3).AsReal();
		}
		return quaternion;
	}

	@Override
	public Color4 AsColor4()
	{
		final Color4 color = new Color4(Color4.Black);

		if (4 == this.size())
		{
			color.R = (float) get(0).AsReal();
			color.G = (float) get(1).AsReal();
			color.B = (float) get(2).AsReal();
			color.A = (float) get(3).AsReal();
		}
		return color;
	}

	@Override
	public boolean AsBoolean()
	{
		return 0 < value.size();
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
		return null != osd && OSDType.Array == osd.getType() && ((OSDArray)osd).value.equals(this.value);
	}

	public OSD clone()
	{
		final OSDArray osd = (OSDArray)super.clone();
		osd.value = new ArrayList<OSD>(value);
		return osd;
	}

	@Override
	public String toString()
	{
		try
		{
			return OSDParser.serializeToString(this, OSDFormat.Notation);
		}
		catch (final IOException ex)
		{
			ex.printStackTrace();
		}
		return null;
	}
}
