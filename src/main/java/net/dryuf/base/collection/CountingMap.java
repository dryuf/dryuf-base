package net.dryuf.base.collection;


import java.util.SortedMap;


/**
 * {@link java.util.Map} extended by count functions which provide the number of items prior or after the searched key.
 *
 * @param <K>
 *      type of key
 * @param <V>
 *      type of value
 */
public interface CountingMap<K, V> extends SortedMap<K, V>
{
	int size();

	long sizeBig();

	@Override
	V get(Object key);

	@Override
	V put(K key, V value);

	@Override
	V remove(Object key);

	/**
	 * Counts number of items before key.
	 *
	 * @param key
	 *      key to search
	 *
	 * @return
	 *      number of items before key.
	 */
	long lowerCount(K key);

	/**
	 * Counts number of items up to key (including).
	 *
	 * @param key
	 *      key to search
	 *
	 * @return
	 *      number of items up to key (including).
	 */
	long floorCount(K key);

	/**
	 * Counts number of items since key (including).
	 *
	 * @param key
	 *      key to search
	 *
	 * @return
	 *      number of items since key (including).
	 */
	long ceilingCount(K key);

	/**
	 * Counts number of items after key.
	 *
	 * @param key
	 *      key to search
	 *
	 * @return
	 *      number of items after key.
	 */
	long higherCount(K key);
}
