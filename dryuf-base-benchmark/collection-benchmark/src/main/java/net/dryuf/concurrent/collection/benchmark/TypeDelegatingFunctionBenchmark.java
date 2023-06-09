package net.dryuf.concurrent.collection.benchmark;


import net.dryuf.base.concurrent.future.ScheduledUtil;
import net.dryuf.base.function.ThrowingFunction;
import net.dryuf.base.function.delegate.TypeDelegatingBiFunction2;
import net.dryuf.base.function.delegate.TypeDelegatingFunction;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.TimeUnit;
import java.util.function.Function;


/**
 * Benchmark for TypeDelegatingFunction implementations.
 */
@Warmup(iterations = 2, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS, batchSize = 1)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@BenchmarkMode(Mode.AverageTime)
@Fork(value = 1)
public class TypeDelegatingFunctionBenchmark
{
	public static Callee		callee = new Callee();

	private static Object[]		allObjects = new Object[]{
			new FirstImpl(),
			new SecondImpl(),
			new ThirdImpl(),
			new FourthImpl(),
			new FifthImpl(),
	};

	@State(Scope.Benchmark)
	public static class DirectConcurrentCacheState
	{
		@Setup(Level.Trial)
		public void setup()
		{
			caller = new ThrowingFunction<Object, Object, RuntimeException>()
			{
				@Override
				public Object			apply(Object input)
				{
					if (input instanceof First)
						return callee.calleeFirst((First) input);
					if (input instanceof Second)
						return callee.calleeSecond((Second) input);
					if (input instanceof Third)
						return callee.calleeThird((Third) input);
					if (input instanceof Fourth)
						return callee.calleeFourth((Fourth) input);
					if (input instanceof Fifth)
						return callee.calleeFifth((Fifth) input);
					throw new IllegalArgumentException("Unknown object: "+input.getClass());
				}
			};
		}

		public ThrowingFunction<Object, Object, RuntimeException> caller;
	}

	@State(Scope.Benchmark)
	public static class InstanceCallerCacheState
	{
		@Setup(Level.Trial)
		public void setup(Blackhole blackhole) throws InterruptedException
		{
			caller = TypeDelegatingFunction.<Object, Object, RuntimeException>callbacksBuilder()
					.add(First.class, callee::calleeFirst)
					.add(Second.class, callee::calleeSecond)
					.add(Third.class, callee::calleeThird)
					.add(Fourth.class, callee::calleeFourth)
					.add(Fifth.class, callee::calleeFifth)
					.build();
			callInner(caller, blackhole);
			Thread.sleep((ScheduledUtil.MEMORY_PROPAGATION_DELAY_NS*2)/1_000_000+100);
		}

		public ThrowingFunction<Object, Object, RuntimeException> caller;
	}

	@State(Scope.Benchmark)
	public static class OwnerCallerCacheState
	{
		@Setup(Level.Trial)
		public void setup(Blackhole blackhole) throws InterruptedException
		{
			caller = new ThrowingFunction<Object, Object, RuntimeException>()
			{
				@Override
				public Object			apply(Object o)
				{
					return ownerCaller.apply(callee, o);
				}

				private TypeDelegatingBiFunction2<Callee, Object, Object, RuntimeException> ownerCaller =
					TypeDelegatingBiFunction2.<Callee, Object, Object, RuntimeException>callbacksBuilder()
						.add(First.class, Callee::calleeFirst)
						.add(Second.class, Callee::calleeSecond)
						.add(Third.class, Callee::calleeThird)
						.add(Fourth.class, Callee::calleeFourth)
						.add(Fifth.class, Callee::calleeFifth)
						.build();
			};
			callInner(caller, blackhole);
			Thread.sleep((ScheduledUtil.MEMORY_PROPAGATION_DELAY_NS*2)/1_000_000+100);
		}

		public ThrowingFunction<Object, Object, RuntimeException> caller;
	}

	@Benchmark
	public void			directInstanceofBenchmark(DirectConcurrentCacheState state, Blackhole blackhole)
	{

		callBulk(state.caller, blackhole);
	}

	@Benchmark
	public void			instanceCallerBenchmark(InstanceCallerCacheState state, Blackhole blackhole) throws InterruptedException
	{
		callBulk(state.caller, blackhole);
	}

	@Benchmark
	public void			ownerCallerBenchmark(OwnerCallerCacheState state, Blackhole blackhole) throws InterruptedException
	{
		callBulk(state.caller, blackhole);
	}

	private static void		callBulk(ThrowingFunction<Object, Object, RuntimeException> caller, Blackhole blackhole)
	{
		callInner(caller, blackhole);
	}

	private static void		callInner(ThrowingFunction<Object, Object, RuntimeException> caller, Blackhole blackhole)
	{
		for (Object o: allObjects) {
			caller.apply(o);
		}
	}

	public static class Callee
	{
		public Object calleeFirst(First o)
		{
			return o;
		}

		private Object calleeSecond(Second o)
		{
			return o;
		}

		private Object calleeThird(Third o)
		{
			return o;
		}

		private Object calleeFourth(Fourth o)
		{
			return o;
		}

		private Object calleeFifth(Fifth o)
		{
			return o;
		}
	}

	private static interface First
	{
	}

	private static interface Second
	{
	}

	private static interface Third
	{
	}

	private static interface Fourth
	{
	}

	private static interface Fifth
	{
	}

	private static class FirstImpl implements First
	{
	}

	private static class SecondImpl implements Second
	{
	}

	private static class ThirdImpl implements Third
	{
	}

	private static class FourthImpl implements Fourth
	{
	}

	private static class FifthImpl implements Fifth
	{
	}
}
