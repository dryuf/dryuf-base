package net.dryuf.concurrent.collection;


import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;


/**
 * Tests for {@link TypeDelegatingOwnerBiFunction}.
 */
public class TypeDelegatingOwnerBiFunctionTest
{
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void			testUndefined()
	{
		Fixture<Object, Object, Object> fixture = new Fixture<>(TypeDelegatingOwnerBiFunction.callbacksBuilder()
				.build()
		);
		fixture.call(this, new Object());
	}

	@Test
	public void			testDefined()
	{
		AtomicInteger firstCount = new AtomicInteger();
		AtomicInteger secondCount = new AtomicInteger();

		Fixture<Object, Object, Object> fixture = new Fixture<>(TypeDelegatingOwnerBiFunction.callbacksBuilder()
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

		Fixture<Object, Object, Object> fixture = new Fixture<>(TypeDelegatingOwnerBiFunction.callbacksBuilder()
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

		Fixture<Object, Object, Object> fixture = new Fixture<>(TypeDelegatingOwnerBiFunction.callbacksBuilder()
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

		Fixture<Object, Object, Object> fixture = new Fixture<>(TypeDelegatingOwnerBiFunction.callbacksBuilder()
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
		public 				Fixture(Map<Class<? extends I>, BiFunction<O, ? super I, ? extends R>> callbacks)
		{
			this.callback = new TypeDelegatingOwnerBiFunction<O, I, R>(callbacks);
		}

		public R			call(O owner, I input)
		{
			return callback.apply(owner, input);
		}

		private final TypeDelegatingOwnerBiFunction<O, I, R> callback;
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
