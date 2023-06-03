package net.dryuf.base.stream;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;


public class ExtFilters
{
	/**
	 * Filters elements by unique key.
	 *
	 * @param keyExtractor
	 *      Extracts the key from the object.
	 *
	 * @return
	 *      a Predicate that returns true when key is seen for the first time and false subsequently.
	 *
	 * @param <T>
	 *      type of input object
	 * @param <K>
	 *      type of key
	 *
	 * @implNote
	 *      The filter is not thread safe and must not be used in parallel streams
	 */
	public static <T, K> Predicate<T> distinctByKeyNonparallel(Function<T, K> keyExtractor)
	{
		Set<K> seen = new HashSet<>();
		return (T obj) -> seen.add(keyExtractor.apply(obj));
	}
	/**
	 * Filters elements by unique key.
	 *
	 * @param keyExtractor
	 *      Extracts the key from the object.
	 *
	 * @return
	 *      a Predicate that returns true when key is seen for the first time and false subsequently.
	 *
	 * @param <T>
	 *      type of input object
	 * @param <K>
	 *      type of key
	 *
	 * @implNote
	 *      The filter is thread safe and can be used in parallel streams
	 */
	public static <T, K> Predicate<T> distinctByKeyParallel(Function<T, K> keyExtractor)
	{
		Set<K> seen = ConcurrentHashMap.newKeySet();
		return (T obj) -> seen.add(keyExtractor.apply(obj));
	}
}
