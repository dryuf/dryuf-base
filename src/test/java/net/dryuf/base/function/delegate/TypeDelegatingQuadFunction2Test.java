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

package net.dryuf.base.function.delegate;

import lombok.RequiredArgsConstructor;
import net.dryuf.base.function.delegate.TypeDelegatingQuadFunction2;
import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import java.util.concurrent.atomic.AtomicInteger;


/**
 * Tests for {@link TypeDelegatingQuadFunction2}.
 */
public class TypeDelegatingQuadFunction2Test
{
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void			testUndefined()
	{
		Fixture<Object, Object, Object, Object, Object, RuntimeException> fixture =
			new Fixture<>(TypeDelegatingQuadFunction2.<Object, Object, Object, Object, Object, RuntimeException>callbacksBuilder()
				.build()
		);
		fixture.call(this, new Object(), new Object(), new Object());
	}

	@Test
	public void			testDefined()
	{
		AtomicInteger firstCount = new AtomicInteger();
		AtomicInteger secondCount = new AtomicInteger();

		Fixture<Object, Object, Object, Object, Object, RuntimeException> fixture =
			new Fixture<>(TypeDelegatingQuadFunction2.<Object, Object, Object, Object, Object, RuntimeException>callbacksBuilder()
				.add(FirstImpl.class, (Object o, First i, Object p1, Object p2) -> firstCount.incrementAndGet())
				.add(SecondImpl.class, (Object o, Second i, Object p1, Object p2) -> secondCount.incrementAndGet())
				.build()
		);
		fixture.call(this, new FirstImpl(), new Object(), new Object());

		AssertJUnit.assertEquals(1, firstCount.get());
		AssertJUnit.assertEquals(0, secondCount.get());
	}

	@Test
	public void			testDefinedSecond()
	{
		AtomicInteger firstCount = new AtomicInteger();
		AtomicInteger secondCount = new AtomicInteger();

		Fixture<Object, Object, Object, Object, Object, RuntimeException> fixture =
			new Fixture<>(TypeDelegatingQuadFunction2.<Object, Object, Object, Object, Object, RuntimeException>callbacksBuilder()
				.add(FirstImpl.class, (Object o, First i, Object p1, Object p2) -> firstCount.incrementAndGet())
				.add(SecondImpl.class, (Object o, Second i, Object p1, Object p2) -> secondCount.incrementAndGet())
				.build()
		);
		fixture.call(this, new SecondImpl(), new Object(), new Object());

		AssertJUnit.assertEquals(0, firstCount.get());
		AssertJUnit.assertEquals(1, secondCount.get());
	}

	@Test
	public void			testDerived()
	{
		AtomicInteger firstCount = new AtomicInteger();
		AtomicInteger secondCount = new AtomicInteger();

		Fixture<Object, Object, Object, Object, Object, RuntimeException> fixture =
			new Fixture<>(TypeDelegatingQuadFunction2.<Object, Object, Object, Object, Object, RuntimeException>callbacksBuilder()
				.add(First.class, (Object o, First i, Object p1, Object p2) -> firstCount.incrementAndGet())
				.add(Second.class, (Object o, Second i, Object p1, Object p2) -> secondCount.incrementAndGet())
				.build()
		);
		fixture.call(this, new FirstImpl(), new Object(), new Object());

		AssertJUnit.assertEquals(1, firstCount.get());
		AssertJUnit.assertEquals(0, secondCount.get());
	}

	@Test
	public void			testDerivedConflicting()
	{
		AtomicInteger firstCount = new AtomicInteger();
		AtomicInteger secondCount = new AtomicInteger();

		Fixture<Object, Object, Object, Object, Object, RuntimeException> fixture =
			new Fixture<>(TypeDelegatingQuadFunction2.<Object, Object, Object, Object, Object, RuntimeException>callbacksBuilder()
				.add(First.class, (Object o, First i, Object p1, Object p2) -> firstCount.incrementAndGet())
				.add(Second.class, (Object o, Second i, Object p1, Object p2) -> secondCount.incrementAndGet())
				.build()
		);
		fixture.call(this, new BothImpl(), new Object(), new Object());

		AssertJUnit.assertEquals(1, firstCount.get());
		AssertJUnit.assertEquals(0, secondCount.get());
	}

	private class Fixture<O, I, V, W, R, X extends Exception>
	{
		public 				Fixture(
			TypeDelegatingQuadFunction2<O, ? super I, ? super V, ? super W, ? extends R, RuntimeException> callbacks)
		{
			this.callback = callbacks;
		}

		public R			call(O owner, I input, V p2, W p3)
		{
			return callback.apply(owner, input, p2, p3);
		}

		private final TypeDelegatingQuadFunction2<O, ? super I, ? super V, ? super W, ? extends R, RuntimeException> callback;
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

	private static class Additional2
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
		private static final TypeDelegatingQuadFunction2<MyProcessor, Input, Additional, Additional2, Result, RuntimeException> processingFunctions =
				TypeDelegatingQuadFunction2.<MyProcessor, Input, Additional, Additional2, Result, RuntimeException>callbacksBuilder()
					.add(First.class, MyProcessor::processFirst)
					.add(Second.class, MyProcessor::processSecond)
					.build();

		public Result process(Input p1, Additional p2, Additional2 p3)
		{
			return processingFunctions.apply(this, p1, p2, p3);
		}

		// The First can be also FirstImpl implements First
		private Result processFirst(First p1, Additional p2, Additional2 p3)
		{
			return new Result(p1.getFirstValue());
		}

		// The Second can be also SecondImpl implements Second
		private Result processSecond(Second p1, Additional p2, Additional2 p3)
		{
			return new Result(p1.getSecondValue());
		}
	}
}
