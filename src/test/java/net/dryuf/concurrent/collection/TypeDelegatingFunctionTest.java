package net.dryuf.concurrent.collection;


import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;


/**
 * Tests for {@link TypeDelegatingFunction}.
 */
public class TypeDelegatingFunctionTest
{
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void			testUndefined()
	{
		Fixture<Object, Object> fixture = new Fixture<>(new LinkedHashMap<>());
		fixture.call(new Object());
	}

	@Test
	public void			testDefined()
	{
		AtomicInteger firstCount = new AtomicInteger();
		AtomicInteger secondCount = new AtomicInteger();

		Fixture<Object, Object> fixture = new Fixture<>(TypeDelegatingFunction.callbacksBuilder()
				.add(FirstImpl.class, (First o) -> firstCount.incrementAndGet())
				.add(SecondImpl.class, (Second o) -> secondCount.incrementAndGet())
				.build()
		);
		fixture.call(new FirstImpl());

		AssertJUnit.assertEquals(1, firstCount.get());
		AssertJUnit.assertEquals(0, secondCount.get());
	}

	@Test
	public void			testDefinedSecond()
	{
		AtomicInteger firstCount = new AtomicInteger();
		AtomicInteger secondCount = new AtomicInteger();

		Fixture<Object, Object> fixture = new Fixture<>(TypeDelegatingFunction.callbacksBuilder()
				.add(FirstImpl.class, (First o) -> firstCount.incrementAndGet())
				.add(SecondImpl.class, (Second o) -> secondCount.incrementAndGet())
				.build()
		);
		fixture.call(new SecondImpl());

		AssertJUnit.assertEquals(0, firstCount.get());
		AssertJUnit.assertEquals(1, secondCount.get());
	}

	@Test
	public void			testDerived()
	{
		AtomicInteger firstCount = new AtomicInteger();
		AtomicInteger secondCount = new AtomicInteger();

		Fixture<Object, Object> fixture = new Fixture<>(TypeDelegatingFunction.callbacksBuilder()
				.add(First.class, (First o) -> firstCount.incrementAndGet())
				.add(Second.class, (Second o) -> secondCount.incrementAndGet())
				.build()
		);
		fixture.call(new FirstImpl());

		AssertJUnit.assertEquals(1, firstCount.get());
		AssertJUnit.assertEquals(0, secondCount.get());
	}

	@Test
	public void			testDerivedConflicting()
	{
		AtomicInteger firstCount = new AtomicInteger();
		AtomicInteger secondCount = new AtomicInteger();

		Fixture<Object, Object> fixture = new Fixture<>(TypeDelegatingFunction.callbacksBuilder()
				.add(First.class, (First o) -> firstCount.incrementAndGet())
				.add(Second.class, (Second o) -> secondCount.incrementAndGet())
				.build()
		);
		fixture.call(new BothImpl());

		AssertJUnit.assertEquals(1, firstCount.get());
		AssertJUnit.assertEquals(0, secondCount.get());
	}

	private class Fixture<I, O>
	{
		public 				Fixture(Map<Class<? extends I>, Function<? super I, ? extends O>> callbacks)
		{
			this.callback = new TypeDelegatingFunction<>(callbacks);
		}

		public O			call(I input)
		{
			return callback.apply(input);
		}

		private final TypeDelegatingFunction<I, O> callback;
	}

	private static interface First
	{
	}

	private static interface Second
	{
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
}
