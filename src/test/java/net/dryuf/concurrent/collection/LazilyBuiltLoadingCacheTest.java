package net.dryuf.concurrent.collection;


import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;


/**
 * Tests for {@link LazilyBuiltLoadingCache}.
 */
public class LazilyBuiltLoadingCacheTest
{
	@Test
	public void			testInitial()
	{
		AtomicBoolean newValues = new AtomicBoolean(false);

		Fixture<Integer, Integer> fixture = new Fixture<>((Integer v) -> newValues.get() ? -1 : v);

		AssertJUnit.assertEquals(1, (int)fixture.cache.apply(1));
		AssertJUnit.assertEquals(2, (int)fixture.cache.apply(2));
		AssertJUnit.assertEquals(3, (int)fixture.cache.apply(3));
		AssertJUnit.assertEquals(4, (int)fixture.cache.apply(4));
		AssertJUnit.assertEquals(5, (int)fixture.cache.apply(5));

		newValues.set(true);

		AssertJUnit.assertEquals(1, (int)fixture.cache.apply(1));
		AssertJUnit.assertEquals(2, (int)fixture.cache.apply(2));
		AssertJUnit.assertEquals(3, (int)fixture.cache.apply(3));
		AssertJUnit.assertEquals(4, (int)fixture.cache.apply(4));
		AssertJUnit.assertEquals(5, (int)fixture.cache.apply(5));

		AssertJUnit.assertEquals(-1, (int)fixture.cache.apply(6));
	}

	private class Fixture<I, O>
	{
		public 				Fixture(Function<I, O> loader)
		{
			this.cache = new LazilyBuiltLoadingCache<>(loader);
		}

		private final LazilyBuiltLoadingCache<I, O> cache;
	}
}
