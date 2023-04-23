package net.dryuf.base.string;

import org.testng.annotations.Test;

import java.util.NoSuchElementException;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.expectThrows;


public class StringIteratorTest
{
	@Test
	public void all_whenAny_thenExpected()
	{
		StringIterator si = StringIterator.fromFull("hello");
		assertTrue(si.hasNext()); assertEquals(si.next(), 'h');
		assertTrue(si.hasNext()); assertEquals(si.next(), 'e');
		assertTrue(si.hasNext()); assertEquals(si.next(), 'l');
		assertTrue(si.hasNext()); assertEquals(si.next(), 'l');
		assertTrue(si.hasNext()); assertEquals(si.next(), 'o');

		assertFalse(si.hasNext());

		si.back();
		assertTrue(si.hasNext());
		assertEquals(si.next(), 'o');
	}

	@Test
	public void next_whenOutOfBounds_thenException()
	{
		StringIterator si = StringIterator.fromOffsetEnd("", 0, 0);

		expectThrows(NoSuchElementException.class, () -> si.next());
	}

	@Test
	public void back_whenOutOfBounds_thenException()
	{
		StringIterator si = StringIterator.fromOffsetEnd("", 0, 0);

		expectThrows(NoSuchElementException.class, () -> si.back());
	}

	@Test
	public void fromOffsetLength_whenLimited_thenStayWithinBoundaries()
	{
		StringIterator si = StringIterator.fromOffsetLength("hello", 1, 2);

		assertEquals(si.next(), 'e');
		assertEquals(si.next(), 'l');

		assertFalse(si.hasNext());
		expectThrows(NoSuchElementException.class, () -> si.next());

		si.back();
		si.back();

		expectThrows(NoSuchElementException.class, () -> si.back());
	}

	@Test
	public void fromOffsetEnd_whenLimited_thenStayWithinBoundaries()
	{
		StringIterator si = StringIterator.fromOffsetEnd("hello", 1, 3);

		assertEquals(si.next(), 'e');
		assertEquals(si.next(), 'l');

		assertFalse(si.hasNext());
		expectThrows(NoSuchElementException.class, () -> si.next());

		si.back();
		si.back();

		expectThrows(NoSuchElementException.class, () -> si.back());
	}

	@Test
	public void fromOffsetEnd_whenNegativeOffset_thenException()
	{
		expectThrows(IllegalArgumentException.class, () ->
			StringIterator.fromOffsetEnd("hello", -1, 1));
	}

	@Test
	public void fromOffsetEnd_whenEndBeforeOffset_thenException()
	{
		expectThrows(IllegalArgumentException.class, () ->
			StringIterator.fromOffsetEnd("hello", 2, 1));
	}
}
