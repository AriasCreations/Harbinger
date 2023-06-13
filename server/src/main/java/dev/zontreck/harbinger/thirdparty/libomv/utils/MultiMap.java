package dev.zontreck.harbinger.thirdparty.libomv.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * <p>
 * A variation of Map which allows multiple values to be stored per key.
 * </p>
 * 
 * <p>
 * Where a collection of values is returned, the collection is unmodifiable. If
 * changes are to be made to the MultiMap, they should be made through one of
 * the MultiMap's methods.
 * </p>
 * 
 * <p>
 * This implementation uses an internal Map to hold Collections of values mapped
 * to the keys. The default is a HashMap but other maps can be supplied to the
 * constructor.
 * </p>
 * 
 * <p>
 * By default, the collections of values are stored in ArrayLists, one ArrayList
 * for each key. The ArrayLists are created from a CollectionFactory.
 * Constructors exist to allow a differentCollectionFactory to be supplied. This
 * allows flexibility in the precise behaviour of the MultiMap. Supplying a
 * factory which creates sets will prevent duplicates from being stored. Using
 * TreeSets will keep the values in order, etc...
 * </p>
 * 
 * <p>
 * </p>
 * 
 * @author chris
 * 
 * @param <K>
 *            Key
 * @param <V>
 *            Value
 */
public class MultiMap<K, V>
{
	private final Map<K, List<V>> inner;
	private int valueCount;

	/**
	 * Creates a MultiMap which uses a HashMap as the underlying map and
	 * ArrayLists to store the values.
	 */
	public MultiMap()
	{
		this(new HashMap<K, List<V>>());
	}

	/**
	 * Creates a MultiMap which allows the programmer to specify the underlying
	 * map and uses ArrayLists to store the values.
	 * 
	 * @param innerMap
	 */
	public MultiMap(final Map<K, List<V>> innerMap)
	{
		inner = innerMap;
		this.valueCount = 0;
		for (final List<V> values : innerMap.values())
		{
			this.valueCount += values.size();
		}
	}

	public MultiMap(final MultiMap<K, V> innerMap)
	{
		inner = new HashMap<K, List<V>>();
		this.valueCount = 0;
		for (final Map.Entry<K, List<V>> values : innerMap.entrySet())
		{
			inner.put(values.getKey(), values.getValue());
			this.valueCount += values.getValue().size();
		}
	}

	/**
	 * Retrieves all the values associated with the supplied key.
	 * 
	 * @param key
	 * @return a Collection containing each of the values associated with the
	 *         key, or null if no values are associated with this key.
	 */
	public List<V> get(final Object key)
	{
		return this.get(key, false);
	}

	/**
	 * Retrieves all the values associated with the supplied key.
	 * 
	 * @param key
	 * @param returnEmptyList
	 * @return a List containing each of the values associated with the
	 *         key, or null if no values are associated with this key.
	 */
	public List<V> get(final Object key, final boolean returnEmptyList)
	{
		final List<V> values = this.inner.get(key);
		if (null == values)
		{
			if (returnEmptyList)
			    return new ArrayList<V>();
			else
			    return null;
		}
		return this.inner.get(key);
	}

	/**
	 * Returns the number of values stored in this MultiMap. This method is
	 * synonymous with the valueCount() method and was included for consistency
	 * with the standard Java Collections.
	 * 
	 * @return the number of values stored in the MultiMap.
	 */
	public int size()
	{
		return this.valueCount;
	}

	/**
	 * Returns the number of keys in this MultiMap.
	 * 
	 * @return the number of keys.
	 */
	public int keyCount()
	{
		return this.inner.size();
	}

	/**
	 * Returns the number of values stored in this MultiMap. This method is
	 * synonymous with the size() method and was included for clarity.
	 * 
	 * @return the number of values stored in the MultiMap.
	 */
	public int valueCount()
	{
		return this.valueCount;
	}

    /**
     * Gets an iterator for the collection mapped to the specified key.
     * 
     * @param key  the key to get an iterator for
     * @return the iterator of the collection at the key, empty iterator if key not in map
     * @since Commons Collections 3.1
     */
    public Iterator<V> iterator(final K key)
    {
        return new MultiMapIterator(key, this.inner.get(key));
    }

    /**
	 * Associates the supplied value with the supplied key. MultiMaps allow
	 * multiple values per key but duplicates are only allowed if the underlying
	 * Collection supplied to the constructor also allows them. The default
	 * ArrayList does allow duplicates.
	 * 
	 * @param key
	 * @param value
	 */
	public void put(final K key, final V value)
	{
		List<V> values = this.inner.get(key);
		if (null == values)
		{
			values = new ArrayList<V>();
			this.inner.put(key, values);
		}
		if (values.add(value))
		{
			this.valueCount++;
		}
	}

	/**
	 * Removes the supplied key from the MultiMap and returns all of the values
	 * previously associated with it.
	 * 
	 * @param key
	 * @return a collections containing the values previously associated with
	 *         the key, or null if no values were previously associated with it.
	 */
	public List<V> remove(final Object key)
	{
		final List<V> removed = this.inner.remove(key);
		if (null == removed)
		{
			return null;
		}
		this.valueCount -= removed.size();
		return removed;
	}

	/**
	 * Removes the mapping between the supplied key and value. The return value
	 * reports the success of the operation.
	 * 
	 * @param key
	 * @param value
	 * @return true if the value was successfully removed, false otherwise.
	 */
	public boolean remove(final Object key, final Object value)
	{

		List<V> values = this.inner.get(key);

		if (null == values)
		{
			return false;
		}
		if (!values.remove(value))
		{
			return false;
		}

		if (values.isEmpty())
		{
			this.inner.remove(key);
		}
		this.valueCount--;
		return true;
	}

	/**
	 * Empties the Multimap.
	 */
	public void clear()
	{
		this.inner.clear();
		this.valueCount = 0;
	}

	/**
	 * Checks if the MultiMap is empty.
	 * 
	 * @return true if it is empty, false otherwise.
	 */
	public boolean isEmpty()
	{
		return 0 == valueCount;
	}

	/**
	 * Checks if any values are associated with the supplied key.<br>
	 * <br>
	 * Note: If the key was previously added to the MultiMap and all of its
	 * values have subsequently been removed, this method will return false.
	 * 
	 * @param key
	 * @return true if values are associated with this key, false otherwise.
	 */
	public boolean containsKey(final Object key)
	{
		return this.inner.containsKey(key);
	}

	/**
	 * Checks if this value is associated with any keys.
	 * 
	 * @param value
	 * @return true if the value is contained in the MultiMap, false otherwise.
	 */
	public boolean containsValue(final Object value)
	{
		for (final List<V> values : this.inner.values())
		{
			if (values.contains(value))
			{
				return true;
			}
		}
		return false;
	}

	/**
	 * Adds all the mappings in the supplied Map to this MultiMap.
	 * 
	 * @param m
	 *            the map containing the key-value pairs to be added.
	 */
	public void putAll(final Map<? extends K, ? extends V> m)
	{
		for (final Map.Entry<? extends K, ? extends V> entry : m.entrySet())
		{
			this.put(entry.getKey(), entry.getValue());
		}
	}

	/**
	 * Adds all the mappings in the supplied MultiMap to this MultiMap.
	 * 
	 * @param m
	 *            the MultiMap containing the key-value pairs to be added.
	 */
	public void putAll(final MultiMap<? extends K, ? extends V> m)
	{
		for (final K key : m.inner.keySet())
		{
			for (final V value : m.get(key))
			{
				this.put(key, value);
			}
		}
	}

	/**
	 * Retrieves all the keys in the MulitMap.
	 * 
	 * @return a Set containing all the keys in the multimap.
	 */
	public Set<K> keySet()
	{
		return this.inner.keySet();
	}

	/**
	 * Retrieves all the values contained in the MultiMap. This method is
	 * supplied as a convenience but is likely to have poor performance.
	 * 
	 * @return a Collection containing all the values in the MultiMap.
	 */
	public List<V> values()
	{
		final List<V> allValues = new ArrayList<V>(this.valueCount());
		for (final List<V> values : this.inner.values())
		{
			allValues.addAll(values);
		}
		return allValues;
	}

	/**
	 * A standard Map-like entry set. Each key is mapped to a collection
	 * containing all its values.
	 * 
	 * @return the entry set.
	 */
	public Set<Map.Entry<K, List<V>>> entrySet()
	{
		return this.inner.entrySet();
	}

	/**
	 * Returns an entry set where each key is mapped to one value. As a
	 * consequence of this, each key may appear multiple times in the entry set.
	 * This method is supplied as a convenience and may have poor performance.
	 * 
	 * @return
	 */
	public List<MultiMapEntry> expandedEntries()
	{
		final List<MultiMapEntry> entries = new ArrayList<MultiMapEntry>(this.valueCount());
		for (final Map.Entry<K, List<V>> e : this.entrySet())
		{
			K key = e.getKey();
			for (final V value : e.getValue())
			{
				entries.add(new MultiMapEntry(key, value));
			}
		}
		return entries;
	}

	@Override
	public String toString()
	{
		String string = String.format("(%d keys, %d values\n", this.inner.size(), this.valueCount);
		for (final Map.Entry<K, List<V>> e : this.entrySet())
		{
			string = string + " " + e.getKey().toString() + ", count: " + e.getValue().size() + " " + e.getValue() + "\n";
		}
		return string + ")";
	}
	
	/**
	 * Holds key-value pairs, used as a return type for the expandedEntries()
	 * method.
	 * 
	 * @author chris
	 * 
	 */
	public class MultiMapEntry
	{

		private final K key;
		private final V value;

		private MultiMapEntry(final K key, final V value)
		{
			this.key = key;
			this.value = value;
		}

		/**
		 * 
		 * @return the key.
		 */
		public K getKey()
		{
			return this.key;
		}

		/**
		 * 
		 * @return the value.
		 */
		public V getValue()
		{
			return this.value;
		}

	}
	private class MultiMapIterator implements Iterator<V>
	{
		Iterator<V> iter;
		K key;
		
		public MultiMapIterator(final K key, final List<V> list)
		{
			iter = null != list ? list.iterator() : null;
			this.key = key;
		}

		@Override
		public boolean hasNext()
		{
			return null != iter && this.iter.hasNext();
		}

		@Override
		public V next()
		{
			return null != iter ? this.iter.next() : null;
		}

		@Override
		public void remove()
		{
			if (null != iter)
			{
				this.iter.remove();
				MultiMap.this.valueCount--;
				if (!this.iter.hasNext())
					MultiMap.this.inner.remove(this.key);
			}
			else
			{
			    throw new UnsupportedOperationException();
			}
		}
	}
}
