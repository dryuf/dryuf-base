package net.dryuf.concurrent.collection.benchmark;


import net.dryuf.concurrent.SharedScheduledExecutorInstance;
import net.dryuf.concurrent.collection.TypeDelegatingFunction;
import net.dryuf.concurrent.collection.TypeDelegatingOwnerBiFunction;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Function;


/**
 * Benchmark for TypeDelegatingFunction implementations.
 */
@Warmup(iterations = 2, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 2, time = 1, timeUnit = TimeUnit.SECONDS, batchSize = 1)
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
			caller = new Function<Object, Object>()
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

		public Function<Object, Object> caller;
	}

	@State(Scope.Benchmark)
	public static class InstanceCallerCacheState
	{
		@Setup(Level.Trial)
		public void setup(Blackhole blackhole) throws InterruptedException
		{
			caller= new TypeDelegatingFunction<Object, Object>(TypeDelegatingFunction.callbacksBuilder()
					.add(First.class, callee::calleeFirst)
					.add(Second.class, callee::calleeSecond)
					.add(Third.class, callee::calleeThird)
					.add(Fourth.class, callee::calleeFourth)
					.add(Fifth.class, callee::calleeFifth)
					.build()
			);
			callInner(caller, blackhole);
			Thread.sleep((SharedScheduledExecutorInstance.MEMORY_PROPAGATION_DELAY_NS*2)/1_000_000+100);
		}

		public Function<Object, Object> caller;
	}

	@State(Scope.Benchmark)
	public static class OwnerCallerCacheState
	{
		@Setup(Level.Trial)
		public void setup(Blackhole blackhole) throws InterruptedException
		{
			caller = new Function<Object, Object>()
			{
				@Override
				public Object			apply(Object o)
				{
					return ownerCaller.apply(callee, o);
				}

				private BiFunction<Callee, Object, Object> ownerCaller = new TypeDelegatingOwnerBiFunction<>(TypeDelegatingOwnerBiFunction.<Callee, Object, Object>callbacksBuilder()
						.add(First.class, Callee::calleeFirst)
						.add(Second.class, Callee::calleeSecond)
						.add(Third.class, Callee::calleeThird)
						.add(Fourth.class, Callee::calleeFourth)
						.add(Fifth.class, Callee::calleeFifth)
						.build()
				);
			};
			callInner(caller, blackhole);
			Thread.sleep((SharedScheduledExecutorInstance.MEMORY_PROPAGATION_DELAY_NS*2)/1_000_000+100);
		}

		public Function<Object, Object> caller;
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

	private static void		callBulk(Function<Object, Object> caller, Blackhole blackhole)
	{
		for (int i = 0; i < 200000; ++i) {
			callInner(caller, blackhole);
		}
	}

	private static void		callInner(Function<Object, Object> caller, Blackhole blackhole)
	{
		for (Object o: allObjects) {
			caller.apply(o);
		}
	}

	public static class Callee
	{
		public Object calleeFirst (First o)
		{
			return o;
		}

		private Object calleeSecond (Second o)
		{
			return o;
		}

		private Object calleeThird (Third o)
		{
			return o;
		}

		private Object calleeFourth (Fourth o)
		{
			return o;
		}

		private Object calleeFifth (Fifth o)
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
