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

package net.dryuf.concurrent.collection.benchmark;

import net.dryuf.base.collection.LazilyBuiltLoadingCache;
import net.dryuf.base.concurrent.future.ScheduledUtil;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Warmup;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;


@Warmup(iterations = 2, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 2, time = 1, timeUnit = TimeUnit.SECONDS, batchSize = 1)
@Fork(value = 1)
public class LazilyBuiltLoadingCacheBenchmark
{
	static {
		System.setProperty("net.dryuf.concurrent.memoryPropagationDelay", "2100000000");

	}

	public static Function<Integer, Integer> populateCache(Function<Integer, Integer> cache)
	{
		lookupCacheInner(cache);
		return cache;
	}

	@State(Scope.Benchmark)
	public static class DirectConcurrentCacheState
	{
		@Setup(Level.Trial)
		public void setup()
		{
			cache = populateCache(new Function<Integer, Integer>() {
				@Override
				public Integer apply(Integer input)
				{
					return store.computeIfAbsent(input, Function.identity());
				}

				private final ConcurrentHashMap<Integer, Integer> store = new ConcurrentHashMap<>();
			});
		}

		public static Function<Integer, Integer> cache;
	}

	@State(Scope.Benchmark)
	public static class ColdCacheState
	{
		@Setup(Level.Iteration)
		public void setup()
		{
			cache = populateCache(new LazilyBuiltLoadingCache<>(Function.identity()));
		}

		@TearDown
		public void teardown() throws NoSuchFieldException, IllegalAccessException
		{
			Field snapshotMapField = LazilyBuiltLoadingCache.class.getDeclaredField("snapshotMap");
			snapshotMapField.setAccessible(true);
			Map<?, ?> snapshotMap = (Map<?, ?>) snapshotMapField.get(cache);
			if (snapshotMap.size() != 0) {
				throw new IllegalStateException("Cold cache populated");
			}
		}


		public static Function<Integer, Integer> cache;
	}

	@State(Scope.Benchmark)
	public static class WarmCacheState
	{
		@Setup(Level.Trial)
		public void setup() throws NoSuchFieldException, IllegalAccessException
		{
			cache = populateCache(new LazilyBuiltLoadingCache<>(Function.identity()));
			try {
				Thread.sleep((ScheduledUtil.MEMORY_PROPAGATION_DELAY_NS*2)/1_000_000+100);
			}
			catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
			Field snapshotMapField = LazilyBuiltLoadingCache.class.getDeclaredField("snapshotMap");
			snapshotMapField.setAccessible(true);
			Map<?, ?> snapshotMap = (Map<?, ?>) snapshotMapField.get(cache);
			if (snapshotMap.size() != 128) {
				throw new IllegalStateException("Warm cache not fully populated");
			}
		}

		public static Function<Integer, Integer> cache;
	}

	@Benchmark
	public void			directConcurrentBenchmark(DirectConcurrentCacheState state)
	{
		lookupCacheBulk(state.cache);
	}

	@Benchmark
	public void			coldLazilyBuiltLoadingCacheBenchmark(ColdCacheState state)
	{
		lookupCacheBulk(state.cache);
	}

	@Benchmark
	public void			warmLazilyBuiltLoadingCacheBenchmark(WarmCacheState state)
	{
		lookupCacheBulk(state.cache);
	}

	private static void		lookupCacheBulk(Function<Integer, Integer> cache)
	{
		for (int i = 0; i < 7812; ++i) {
			lookupCacheInner(cache);
		}
	}

	private static void		lookupCacheInner(Function<Integer, Integer> cache)
	{
		for (int i = 0; i < 128; ++i) {
			cache.apply(i);
		}
	}
}
