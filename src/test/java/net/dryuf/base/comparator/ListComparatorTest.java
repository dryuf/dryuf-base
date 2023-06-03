package net.dryuf.base.comparator;

import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import static org.testng.Assert.assertEquals;


public class ListComparatorTest {

	@Test
	void naturalOrder_numbers_sorted() {
		List<Integer> list1 = Arrays.asList(1, 2, 3);
		List<Integer> list2 = Arrays.asList(1, 2, 3);
		List<Integer> list3 = Arrays.asList(1, 2, 4);
		List<Integer> list4 = Arrays.asList(1, 2, 4, 0);

		ListComparator<List<Integer>, Integer> comparator = ListComparator.natural();

		assertEquals(comparator.compare(list1, list2), 0);
		assertEquals(comparator.compare(list1, list3), -1);
		assertEquals(comparator.compare(list3, list1), 1);
		assertEquals(comparator.compare(list3, list4), -1);
	}

	@Test
	void customOrder_nonNatural_sorted() {
		List<String> list1 = Arrays.asList("apple", "banana", "cherry");
		List<String> list2 = Arrays.asList("apple", "banana", "cherry");
		List<String> list3 = Arrays.asList("apple", "banana", "date");

		Comparator<String> customComparator = Comparator.comparing(String::length);
		ListComparator<List<String>, String> comparator = ListComparator.of(customComparator);

		assertEquals(comparator.compare(list1, list2), 0);
		assertEquals(comparator.compare(list1, list3), 1);
		assertEquals(comparator.compare(list3, list1), -1);
	}

	@Test
	void natural_emptyLists_equal() {
		List<String> emptyList1 = new ArrayList<>();
		List<String> emptyList2 = new ArrayList<>();

		ListComparator<List<String>, String> comparator = ListComparator.natural();

		assertEquals(comparator.compare(emptyList1, emptyList2), 0);
	}
}
