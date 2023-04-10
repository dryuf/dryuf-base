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

import lombok.RequiredArgsConstructor;
import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import java.util.concurrent.atomic.AtomicInteger;


/**
 * Tests for {@link TypeDelegatingTriFunction3}.
 */
public class TypeDelegatingTriFunction3Test
{
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void			testUndefined()
	{
		Fixture<Object, Object, Object, Object, RuntimeException> fixture =
			new Fixture<>(TypeDelegatingTriFunction3.<Object, Object, Object, Object,
					RuntimeException>callbacksBuilder()
				.build()
		);
		fixture.call(this, new Object(), new Object());
	}

	@Test
	public void			testDefined()
	{
		AtomicInteger firstCount = new AtomicInteger();
		AtomicInteger secondCount = new AtomicInteger();

		Fixture<Object, Object, Object, Object, RuntimeException> fixture =
			new Fixture<>(TypeDelegatingTriFunction3.<Object, Object, Object, Object,
					RuntimeException>callbacksBuilder()
				.add(FirstImpl.class, (Object o, Object p1, First i) -> firstCount.incrementAndGet())
				.add(SecondImpl.class, (Object o, Object p2, Second i) -> secondCount.incrementAndGet())
				.build()
		);
		fixture.call(this, new Object(), new FirstImpl());

		AssertJUnit.assertEquals(1, firstCount.get());
		AssertJUnit.assertEquals(0, secondCount.get());
	}

	@Test
	public void			testDefinedSecond()
	{
		AtomicInteger firstCount = new AtomicInteger();
		AtomicInteger secondCount = new AtomicInteger();

		Fixture<Object, Object, Object, Object, RuntimeException> fixture =
			new Fixture<>(TypeDelegatingTriFunction3.<Object, Object, Object, Object,
					RuntimeException>callbacksBuilder()
				.add(FirstImpl.class, (Object o, Object p1, First i) -> firstCount.incrementAndGet())
				.add(SecondImpl.class, (Object o, Object p2, Second i) -> secondCount.incrementAndGet())
				.build()
		);
		fixture.call(this, new Object(), new SecondImpl());

		AssertJUnit.assertEquals(0, firstCount.get());
		AssertJUnit.assertEquals(1, secondCount.get());
	}

	@Test
	public void			testDerived()
	{
		AtomicInteger firstCount = new AtomicInteger();
		AtomicInteger secondCount = new AtomicInteger();

		Fixture<Object, Object, Object, Object, RuntimeException> fixture =
			new Fixture<>(TypeDelegatingTriFunction3.<Object, Object, Object, Object,
					RuntimeException>callbacksBuilder()
				.add(First.class, (Object o, Object p1, First i) -> firstCount.incrementAndGet())
				.add(Second.class, (Object o, Object p2, Second i) -> secondCount.incrementAndGet())
				.build()
		);
		fixture.call(this, new Object(), new FirstImpl());

		AssertJUnit.assertEquals(1, firstCount.get());
		AssertJUnit.assertEquals(0, secondCount.get());
	}

	@Test
	public void			testDerivedConflicting()
	{
		AtomicInteger firstCount = new AtomicInteger();
		AtomicInteger secondCount = new AtomicInteger();

		Fixture<Object, Object, Object, Object, RuntimeException> fixture =
			new Fixture<>(TypeDelegatingTriFunction3.<Object, Object, Object, Object,
					RuntimeException>callbacksBuilder()
				.add(First.class, (Object o, Object p1, First i) -> firstCount.incrementAndGet())
				.add(Second.class, (Object o, Object p2, Second i) -> secondCount.incrementAndGet())
				.build()
		);
		fixture.call(this, new Object(), new BothImpl());

		AssertJUnit.assertEquals(1, firstCount.get());
		AssertJUnit.assertEquals(0, secondCount.get());
	}

	private class Fixture<O, T, U, R, X extends Exception>
	{
		public 				Fixture(
			TypeDelegatingTriFunction3<O, ? super T, ? super U, ? extends R, RuntimeException> callbacks)
		{
			this.callback = callbacks;
		}

		public R			call(O owner, T p1, U p2)
		{
			return callback.apply(owner, p1, p2);
		}

		private final TypeDelegatingTriFunction3<O, ? super T, ? super U, ? extends R, RuntimeException> callback;
	}

	private static interface Input
	{
	}

	private static interface First extends Input
	{
		default int getFirstValue()
		{
			return 1;
		}
	}

	private static interface Second extends Input
	{
		default int getSecondValue()
		{
			return 2;
		}
	}

	private static class FirstImpl implements First
	{
	}

	private static class SecondImpl implements Second
	{
	}

	private static class BothImpl implements First, Second
	{
	}

	private static class Additional
	{
	}

	@RequiredArgsConstructor
	private static class Result
	{
		private final int value;
	}

	/**
	 * This serves as javadoc example only:
	 */
	public static class MyProcessor
	{
		private static final TypeDelegatingTriFunction3<MyProcessor, Additional, Input, Result, RuntimeException> processingFunctions =
				TypeDelegatingTriFunction3.<MyProcessor, Additional, Input, Result, RuntimeException>callbacksBuilder()
					.add(First.class, MyProcessor::processFirst)
					.add(Second.class, MyProcessor::processSecond)
					.build();

		public Result process(Additional p1, Input input)
		{
			return processingFunctions.apply(this, p1, input);
		}

		// The First can be also FirstImpl implements First
		private Result processFirst(Additional p1, First input)
		{
			return new Result(input.getFirstValue());
		}

		// The Second can be also SecondImpl implements Second
		private Result processSecond(Additional p1, Second input)
		{
			return new Result(input.getSecondValue());
		}
	}
}
