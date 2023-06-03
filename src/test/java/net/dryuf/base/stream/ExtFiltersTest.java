package net.dryuf.base.stream;

import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.testng.Assert.assertEquals;


public class ExtFiltersTest
{
	@Test
	void distinctByKeyNonparallel_duplicateKeys_filter()
	{
		List<Integer> numbers = Arrays.asList(12, 25, 38, 15, 30, 43, 18, 25, 38, 55);

		Function<Integer, Integer> keyExtractor = num -> num % 10;
		Predicate<Integer> distinctPredicate = ExtFilters.distinctByKeyNonparallel(keyExtractor);

		List<Integer> filteredNumbers = numbers.stream()
			.filter(distinctPredicate)
			.collect(Collectors.toList());

		List<Integer> expected = Arrays.asList(12, 25, 38, 30, 43);
		assertEquals(filteredNumbers, expected);
	}

	@Test
	void distinctByKeyParallel_duplicateKeys_filter()
	{
		List<Integer> numbers = Arrays.asList(12, 25, 38, 15, 30, 43, 18, 25, 38, 55);

		Function<Integer, Integer> keyExtractor = num -> num % 10;
		Predicate<Integer> distinctPredicate = ExtFilters.distinctByKeyParallel(keyExtractor);

		Set<Integer> filteredNumbers = numbers.stream()
			.parallel()
			.filter(distinctPredicate)
			.collect(Collectors.toSet())
			.stream()
			.map(num -> num %10)
			.collect(Collectors.toSet());

		Set<Integer> expected = new HashSet<>(Arrays.asList(2, 5, 8, 0, 3));
		assertEquals(filteredNumbers, expected.stream().map(num -> num%10).collect(Collectors.toList()));
	}
}
