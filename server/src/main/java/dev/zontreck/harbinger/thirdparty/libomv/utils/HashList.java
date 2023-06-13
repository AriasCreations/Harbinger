/**
 * Copyright (c) blessedgeek from synthfuljava
 * Copyright (c) 2011-2017, Frederick Martian
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
package dev.zontreck.harbinger.thirdparty.libomv.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HashList<K,V> extends HashMap<K,V>
{
	private static final long serialVersionUID = 1L;

	/** Variable KeyList. */
    protected List<K> keyList = new ArrayList<K>();

    /**
     * Instantiates a new HashList.
     */
    public HashList() 
    {
	}

    /**
     * Instantiates a new HashList.
     * 
     * @param map
     */
    public HashList(final Map<K,V> map)
    {
        putAll(map);
    }

    /**
     * Instantiates a new HashList.
     * 
     * @param t
     */
    public HashList(final Object[][] t)
    {
		this.add(t);
    }

    /**
     * Instantiates a new HashList.
     * 
     * @param initSz
     */
    public HashList(final int initSz)
    {
        super(initSz);
    }

    /**
     * Instantiates a new HashList.
     * 
     * @param initSz
     * @param factor
     */
    public HashList(final int initSz, final float factor)
    {
        super(initSz, factor);
    }

    /**
     * Adds the 2D object array [row][col] to the HashList,
     * where for each row, using col=0 as key and col=1 as value.
     * 
     * @param t
     * 
     * @return Adds the as HashList
     */
    @SuppressWarnings("unchecked")
	public HashList<K, V> add(final Object[][] t)
    {
        if (null != t)
        {
            for (int i = 0; i < t.length; i++)
            {
                if (null == t[i] || 1 > t[i].length)
                    continue;
                final Object key = t[i][0];
                final Object value = 2 <= t[i].length ? t[i][1] : null;

                if (null == key || null == value)
                    continue;

                try
                {
                    put((K)key, (V)value);
                }
                catch (final ClassCastException ex) {}
                catch (final Exception ex) {}
            }
        }
        return this;
    }
    
    /**
     * Gets the String Value by Key.
     * 
     * @param key
     * 
     * @return the String as String
     */
    public String getString(final Object key)
    {
        final Object o = this.get(key);
        if (null == o)
            return "";
        return o.toString();
    }

    /**
     * get stored object by position.
     * 
     * @param position
     *            Position of stored object in the hash List.
     * 
     * @return Object stored at that position.
     */
    public V get(final int position)
    {
        if (position < this.keyList.size() && 0 <= position)
            return get(this.keyList.get(position));

        return null;
    }

    /**
     * get hash key of stored object by position.
     * 
     * @param position
     *            Position of stored object in the hash List.
     * 
     * @return Hash key of object stored at that position.
     */
    public K getKey(final int position)
    {
        if (position < this.keyList.size() && 0 <= position)
            return this.keyList.get(position);
        return null;
    }

    /**
     * Gets the KeyPosition.
     * 
     * @param key
     * 
     * @return the KeyPosition as int
     */
    public int getKeyPosition(final K key)
    {
        for (int i = 0; i < this.keyList.size(); i++)
            if (key.equals(this.keyList.get(i)))
                return i;
        return -1;
    }

    /**
     * Gets the KeyList.
     * 
     * @return the KeyList as List
     */
    public List<K> getKeyList()
    {
    	return this.keyList;
    }

    public V put(final K key, final V value)
    {
        putValue(key, value);
        return value;
    }

    /**
     * Put value.
     * 
     * @param key
     * @param value
     * 
     * @return Put value as HashList
     */
    protected HashList<K, V> putValue(final K key, final V value)
    {
        if (null == key || null == value)
            return this;
        super.put(key, value);
        if (!this.keyList.contains(key))
			this.keyList.add(key);
        return this;
    }

	@Override
    @SuppressWarnings("unchecked")
    public V remove(final Object o)
    {
  		return removeValue((K)o);
    }

    public V removeValue(final K key)
    {
        try 
        {
            final V val = get(key);
            super.remove(key);
            keyList.remove(key);
            return val;
        }
        catch (final Exception e)
        {
            return null;
        }
    }

    public V remove(final int i)
    {
        final K key = keyList.get(i);
        return this.removeValue(key);
    }

    /**
     * @see java.util.Hashtable#clear()
     */
    public void clear()
    {
        super.clear();
		this.keyList.clear();
    }

    /**
     * Converts to Array.
     * 
     * @return Array as Object[]
     */
    public V[] toValueArray(final V[] a)
    {
        return toList().toArray(a);
    }

    /**
     * Converts to Array.
     * 
     * @return Array as Object[]
     */
    public Object[][] toKeyValueArray()
    {
        final Object[][] ar = new Object[size()][2];

        for (int i = 0; i < this.size(); i++)
        {
            ar[i][0] = getKey(i);
                ar[i][1] = get(i);
        }
        return ar;
    }

    /**
     * Converts to List.
     * 
     * @param v
     * 
     * @return List
     */
    public List<V> toValueList(final List<V> v)
    {
        for (int i = 0; i < this.size(); i++)
        {
            v.add(this.get(i));
        }
        return v;
    }

    /**
     * Converts to List.
     * 
     * @return List as List
     */
    public List<V> toList()
    {
        return this.toValueList(new ArrayList<V>(this.size()));
    }
}
