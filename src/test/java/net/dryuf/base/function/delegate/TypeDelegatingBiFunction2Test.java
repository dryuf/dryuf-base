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
import net.dryuf.base.function.delegate.TypeDelegatingBiFunction2;
import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import java.util.concurrent.atomic.AtomicInteger;


/**
 * Tests for {@link TypeDelegatingBiFunction2}.
 */
public class TypeDelegatingBiFunction2Test
{
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void			testUndefined()
	{
		Fixture<Object, Object, Object> fixture =
			new Fixture<>(TypeDelegatingBiFunction2.<Object, Object, Object, RuntimeException>callbacksBuilder()
				.build()
		);
		fixture.call(this, new Object());
	}

	@Test
	public void			testDefined()
	{
		AtomicInteger firstCount = new AtomicInteger();
		AtomicInteger secondCount = new AtomicInteger();

		Fixture<Object, Object, Object> fixture =
			new Fixture<>(TypeDelegatingBiFunction2.<Object, Object, Object, RuntimeException>callbacksBuilder()
				.add(FirstImpl.class, (Object o, First i) -> firstCount.incrementAndGet())
				.add(SecondImpl.class, (Object o, Second i) -> secondCount.incrementAndGet())
				.build()
		);
		fixture.call(this, new FirstImpl());

		AssertJUnit.assertEquals(1, firstCount.get());
		AssertJUnit.assertEquals(0, secondCount.get());
	}

	@Test
	public void			testDefinedSecond()
	{
		AtomicInteger firstCount = new AtomicInteger();
		AtomicInteger secondCount = new AtomicInteger();

		Fixture<Object, Object, Object> fixture =
			new Fixture<>(TypeDelegatingBiFunction2.<Object, Object, Object, RuntimeException>callbacksBuilder()
				.add(FirstImpl.class, (Object o, First i) -> firstCount.incrementAndGet())
				.add(SecondImpl.class, (Object o, Second i) -> secondCount.incrementAndGet())
				.build()
		);
		fixture.call(this, new SecondImpl());

		AssertJUnit.assertEquals(0, firstCount.get());
		AssertJUnit.assertEquals(1, secondCount.get());
	}

	@Test
	public void			testDerived()
	{
		AtomicInteger firstCount = new AtomicInteger();
		AtomicInteger secondCount = new AtomicInteger();

		Fixture<Object, Object, Object> fixture =
			new Fixture<>(TypeDelegatingBiFunction2.<Object, Object, Object, RuntimeException>callbacksBuilder()
				.add(First.class, (Object o, First i) -> firstCount.incrementAndGet())
				.add(Second.class, (Object o, Second i) -> secondCount.incrementAndGet())
				.build()
		);
		fixture.call(this, new FirstImpl());

		AssertJUnit.assertEquals(1, firstCount.get());
		AssertJUnit.assertEquals(0, secondCount.get());
	}

	@Test
	public void			testDerivedConflicting()
	{
		AtomicInteger firstCount = new AtomicInteger();
		AtomicInteger secondCount = new AtomicInteger();

		Fixture<Object, Object, Object> fixture =
			new Fixture<>(TypeDelegatingBiFunction2.<Object, Object, Object, RuntimeException>callbacksBuilder()
				.add(First.class, (Object o, First i) -> firstCount.incrementAndGet())
				.add(Second.class, (Object o, Second i) -> secondCount.incrementAndGet())
				.build()
		);
		fixture.call(this, new BothImpl());

		AssertJUnit.assertEquals(1, firstCount.get());
		AssertJUnit.assertEquals(0, secondCount.get());
	}

	private class Fixture<O, I, R>
	{
		public 				Fixture(TypeDelegatingBiFunction2<O, ? super I, ? extends R, RuntimeException> callbacks)
		{
			this.callback = callbacks;
		}

		public R			call(O owner, I input)
		{
			return callback.apply(owner, input);
		}

		private final TypeDelegatingBiFunction2<O, ? super I, ? extends R, RuntimeException> callback;
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
		private static final TypeDelegatingBiFunction2<MyProcessor, Input, Result, RuntimeException> processingFunctions =
			TypeDelegatingBiFunction2.<MyProcessor, Input, Result, RuntimeException>callbacksBuilder()
				.add(First.class, MyProcessor::processFirst)
				.add(Second.class, MyProcessor::processSecond)
				.build();

		public Result process(Input input)
		{
			return processingFunctions.apply(this, input);
		}

		// The First can be also FirstImpl implements First
		private Result processFirst(First input)
		{
			return new Result(input.getFirstValue());
		}

		// The Second can be also SecondImpl implements Second
		private Result processSecond(Second input)
		{
			return new Result(input.getSecondValue());
		}
	}
}
