/*
 * Copyright 2015-2022 Zbynek Vyskovsky mailto:kvr000@gmail.com http://github.com/kvr000/ https://github.com/dryuf/ https://www.linkedin.com/in/zbynek-vyskovsky/
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
