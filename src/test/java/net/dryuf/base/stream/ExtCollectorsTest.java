package net.dryuf.base.stream;

import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.testng.Assert.assertEquals;


public class ExtCollectorsTest
{
	static Comparator<Integer> EVEN_COMPARATOR = new Comparator<Integer>()
	{
		@Override
		public int compare(Integer o1, Integer o2)
		{
			return Integer.compare(o1/2, o2/2);
		}
	};

	@Test
	public void toStableSorted_whenEmpty_thenEmpty()
	{
		List<Integer> result = Stream.<Integer>of()
			.collect(ExtCollectors.toStableSorted(EVEN_COMPARATOR))
			.collect(Collectors.toList());

		assertEquals(result, Arrays.<Integer>asList());
	}

	@Test
	public void toStableSorted_whenConflicting_thenKeepOrder()
	{
		List<Integer> result = Stream.<Integer>of(1, 0, 2, 3, 8, 4, 5, 6, 7)
			.collect(ExtCollectors.toStableSorted(EVEN_COMPARATOR))
			.collect(Collectors.toList());

		assertEquals(result, Arrays.<Integer>asList(1, 0, 2, 3, 4, 5, 6, 7, 8));
	}

	@Test
	public void toStableSortedList_whenConflicting_thenKeepOrder()
	{
		List<Integer> result = Stream.<Integer>of(1, 0, 2, 3, 8, 4, 5, 6, 7)
			.collect(ExtCollectors.toStableSortedList(EVEN_COMPARATOR));

		assertEquals(result, Arrays.<Integer>asList(1, 0, 2, 3, 4, 5, 6, 7, 8));
	}
}
