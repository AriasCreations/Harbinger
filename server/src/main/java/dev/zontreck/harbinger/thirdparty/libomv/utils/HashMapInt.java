/**
 * Copyright 2002-2004 The Apache Software Foundation.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * Note: originally released under the GNU LGPL v2.1,
 * but rereleased by the original author under the ASF license (above).
 */
package dev.zontreck.harbinger.thirdparty.libomv.utils;

/**
 * <p>
 * A hash map that uses primitive ints for the value rather than objects.
 * </p>
 *
 * <p>
 * Note that this class is for internal optimization purposes only, and may not
 * be supported in future releases of Jakarta Commons Lang. Utilities of this
 * sort may be included in future releases of Jakarta Commons Collections.
 * </p>
 *
 * @author Justin Couch
 * @author Alex Chaffee (alex@apache.org)
 * @author Stephen Colebourne
 * @version $Revision: 1.1 $
 * @see java.util.HashMap
 * @since 2.0
 */
public class HashMapInt <K> {
	/**
	 * The default initial capacity - MUST be a power of two.
	 */
	private static final int DEFAULT_INITIAL_CAPACITY = 16;

	/**
	 * The maximum capacity, used if a higher value is implicitly specified
	 * by either of the constructors with arguments.
	 * MUST be a power of two <= 1<<30.
	 */
	private static final int MAXIMUM_CAPACITY = 1 << 30;

	/**
	 * The load factor used when none specified in constructor.
	 */
	private static final float DEFAULT_LOAD_FACTOR = 0.75f;
	/**
	 * The load factor for the hashtable.
	 *
	 * @serial
	 */
	private final float loadFactor;
	/**
	 * The hash table data.
	 */
	private Entry<K>[] table;
	/**
	 * The total number of entries in the hash table.
	 */
	private int size;
	/**
	 * The table is rehashed when its size exceeds this threshold. (The value of
	 * this field is (int)(capacity * loadFactor).)
	 *
	 * @serial
	 */
	private int threshold;

	/**
	 * <p>
	 * Constructs a new, empty hashtable with a default capacity and load
	 * factor, which is {@code 20} and {@code 0.75} respectively.
	 * </p>
	 */
	public HashMapInt ( ) {
		this ( HashMapInt.DEFAULT_INITIAL_CAPACITY , HashMapInt.DEFAULT_LOAD_FACTOR );
	}

	/**
	 * <p>
	 * Constructs a new, empty hashtable with the specified initial capacity and
	 * default load factor, which is {@code 0.75}.
	 * </p>
	 *
	 * @param initialCapacity the initial capacity of the hashtable.
	 * @throws IllegalArgumentException if the initial capacity is less than zero.
	 */
	public HashMapInt ( final int initialCapacity ) {
		this ( initialCapacity , HashMapInt.DEFAULT_LOAD_FACTOR );
	}

	/**
	 * <p>
	 * Constructs a new, empty hashtable with the specified initial capacity and
	 * the specified load factor.
	 * </p>
	 *
	 * @param initialCapacity the initial capacity of the hashtable.
	 * @param loadFactor      the load factor of the hashtable.
	 * @throws IllegalArgumentException if the initial capacity is less than zero, or if the load
	 *                                  factor is nonpositive.
	 */
	@SuppressWarnings ("unchecked")
	public HashMapInt ( int initialCapacity , final float loadFactor ) {
		if ( 0 > initialCapacity ) {
			throw new IllegalArgumentException ( "Illegal Capacity: " + initialCapacity );
		}
		if ( 0 >= loadFactor ) {
			throw new IllegalArgumentException ( "Illegal Load: " + loadFactor );
		}
		if ( 0 == initialCapacity ) {
			initialCapacity = 1;
		}

		this.loadFactor = loadFactor;
		this.table = new Entry[ initialCapacity ];
		this.threshold = ( int ) ( initialCapacity * loadFactor );
	}

	/**
	 * Applies a supplemental hash function to a given hashCode, which
	 * defends against poor quality hash functions.  This is critical
	 * because HashMap uses power-of-two length hash tables, that
	 * otherwise encounter collisions for hashCodes that do not differ
	 * in lower bits. Note: Null keys always map to hash 0, thus index 0.
	 */
	static int hash ( int h ) {
		// This function ensures that hashCodes that differ only by
		// constant multiples at each bit position have a bounded
		// number of collisions (approximately 8 at default load factor).
		h ^= ( h >>> 20 ) ^ ( h >>> 12 );
		return h ^ ( h >>> 7 ) ^ ( h >>> 4 );
	}

	/**
	 * Returns index for hash code h.
	 */
	static int indexFor ( final int h , final int length ) {
		return h & ( length - 1 );
	}

	/**
	 * <p>
	 * Returns the number of keys in this hashtable.
	 * </p>
	 *
	 * @return the number of keys in this hashtable.
	 */
	public int size ( ) {
		return this.size;
	}

	/**
	 * <p>
	 * Tests if this hashtable maps no keys to values.
	 * </p>
	 *
	 * @return {@code true} if this hashtable maps no keys to values;
	 * {@code false} otherwise.
	 */
	public boolean isEmpty ( ) {
		return 0 == size;
	}

	/**
	 * <p>
	 * Returns {@code true} if this HashMap maps one or more keys to this
	 * value.
	 * </p>
	 *
	 * <p>
	 * Note that this method is identical in functionality to contains (which
	 * predates the Map interface).
	 * </p>
	 *
	 * @param value value whose presence in this HashMap is to be tested.
	 * @see java.util.Map
	 * @since JDK1.2
	 */
	public boolean containsValue ( final int value ) {
		for ( int i = this.table.length ; 0 < i ; ) {
			i--;
			for ( Entry<K> e = this.table[ i ] ; null != e ; e = e.next ) {
				if ( e.value == value ) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * <p>
	 * Tests if the specified object is a key in this hashtable.
	 * </p>
	 *
	 * @param key possible key.
	 * @return {@code true} if and only if the specified object is a key in
	 * this hashtable, as determined by the <tt>equals</tt> method;
	 * {@code false} otherwise.
	 */
	public boolean containsKey ( final Object key ) {
		final int hash = ( null == key ) ? 0 : HashMapInt.hash ( key.hashCode ( ) );
		for ( Entry<K> e = this.table[ HashMapInt.indexFor ( hash , this.table.length ) ] ; null != e ; e = e.next ) {
			final K k;
			if ( e.hash == hash && ( ( k = e.key ) == key || key.equals ( k ) ) )
				return true;
		}
		return false;
	}

	/**
	 * <p>
	 * Returns the value to which the specified key is mapped in this map.
	 * </p>
	 *
	 * @param key a key in the hashtable.
	 * @return the value to which the key is mapped in this hashtable;
	 * {@code null} if the key is not mapped to any value in this
	 * hashtable.
	 */
	public int get ( final Object key ) {
		final int hash = ( null == key ) ? 0 : HashMapInt.hash ( key.hashCode ( ) );
		for ( Entry<K> e = this.table[ HashMapInt.indexFor ( hash , this.table.length ) ] ; null != e ; e = e.next ) {
			final K k;
			if ( e.hash == hash && ( ( k = e.key ) == key || key.equals ( k ) ) )
				return e.value;
		}
		return - 1;
	}

	/**
	 * <p>
	 * Increases the capacity of and internally reorganizes this hashtable, in
	 * order to accommodate and access its entries more efficiently.
	 * </p>
	 *
	 * <p>
	 * This method is called automatically when the number of keys in the
	 * hashtable exceeds this hashtable's capacity and load factor.
	 * </p>
	 */
	@SuppressWarnings ("unchecked")
	protected void resize ( final int newCapacity ) {
		final Entry<K>[] oldTable = this.table;
		final int oldCapacity = oldTable.length;
		if ( MAXIMUM_CAPACITY == oldCapacity ) {
			this.threshold = Integer.MAX_VALUE;
			return;
		}
		this.table = new Entry[ newCapacity ];

		for ( int k = 0 ; k < oldCapacity ; k++ ) {
			Entry<K> e = oldTable[ k ];
			if ( null != e ) {
				oldTable[ k ] = null;
				do {
					final Entry<K> next = e.next;
					final int i = HashMapInt.indexFor ( e.hash , newCapacity );
					e.next = this.table[ i ];
					this.table[ i ] = e;
					e = next;
				} while ( null != e );
			}
		}
		this.threshold = ( int ) ( newCapacity * this.loadFactor );
	}

	/**
	 * <p>
	 * Maps the specified {@code key} to the specified {@code value}
	 * in this hashtable. The key cannot be {@code null}.
	 * </p>
	 *
	 * <p>
	 * The value can be retrieved by calling the {@code get} method with a
	 * key that is equal to the original key.
	 * </p>
	 *
	 * @param key   the hashtable key.
	 * @param value the value.
	 * @return the previous value of the specified key in this hashtable, or
	 * {@code null} if it did not have one.
	 * @throws NullPointerException if the key is {@code null}.
	 */
	public int put ( final K key , final int value ) {
		// Makes sure the key is not already in the hashtable.
		final int hash = ( null == key ) ? 0 : HashMapInt.hash ( key.hashCode ( ) );
		final int index = HashMapInt.indexFor ( hash , this.table.length );
		for ( Entry<K> e = this.table[ index ] ; null != e ; e = e.next ) {
			final K k;
			if ( e.hash == hash && ( ( k = e.key ) == key || key.equals ( k ) ) ) {
				final int old = e.value;
				e.value = value;
				return old;
			}
		}

		// Creates the new entry.
		this.table[ index ] = new Entry<K> ( hash , key , value , this.table[ index ] );
		if ( this.size >= this.threshold )
			this.resize ( 2 * this.table.length );
		this.size++;

		return - 1;
	}

	/**
	 * <p>
	 * Removes the key (and its corresponding value) from this hashtable.
	 * </p>
	 *
	 * <p>
	 * This method does nothing if the key is not present in the hashtable.
	 * </p>
	 *
	 * @param key the key that needs to be removed.
	 * @return the value to which the key had been mapped in this hashtable, or
	 * {@code null} if the key did not have a mapping.
	 */
	public int remove ( final Object key ) {
		final int hash = ( null == key ) ? 0 : HashMapInt.hash ( key.hashCode ( ) );
		final int i = HashMapInt.indexFor ( hash , this.table.length );
		for ( Entry<K> e = this.table[ i ], prev = null ; null != e ; prev = e , e = e.next ) {
			final Object k;
			if ( e.hash == hash && ( ( k = e.key ) == key || ( null != key && key.equals ( k ) ) ) ) {
				if ( null != prev ) {
					prev.next = e.next;
				}
				else {
					this.table[ i ] = e.next;
				}
				this.size--;
				final int oldValue = e.value;
				e.value = 0;
				return oldValue;
			}
		}
		return - 1;
	}

	/**
	 * <p>
	 * Clears this hashtable so that it contains no keys.
	 * </p>
	 */
	public synchronized void clear ( ) {
		final Entry<K>[] tab = this.table;
		for ( int index = tab.length ; 0 <= index ; ) {
			tab[ index ] = null;
			-- index;
		}
		this.size = 0;
	}

	/**
	 * <p>
	 * Innerclass that acts as a datastructure to create a new entry in the
	 * table.
	 * </p>
	 */
	private static class Entry <K> {
		int hash;

		K key;

		int value;

		Entry<K> next;

		/**
		 * <p>
		 * Create a new entry with the given values.
		 * </p>
		 *
		 * @param hash  The code used to hash the object with
		 * @param key   The key used to enter this in the table
		 * @param value The value for this key
		 * @param next  A reference to the next entry in the table
		 */
		protected Entry ( final int hash , final K key , final int value , final Entry<K> next ) {
			this.hash = hash;
			this.key = key;
			this.value = value;
			this.next = next;
		}
	}

}
