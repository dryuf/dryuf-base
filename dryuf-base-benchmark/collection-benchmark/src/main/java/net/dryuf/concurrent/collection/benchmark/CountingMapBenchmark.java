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

import net.dryuf.base.collection.TreeCountingMap;
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

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


@Warmup(iterations = 2, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS, batchSize = 1)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@BenchmarkMode(Mode.AverageTime)
@Fork(value = 1)
public class CountingMapBenchmark
{
	static Map<Integer, Integer> INPUT = IntStream.range(0, 1_000_000).boxed().collect(Collectors.toMap(i -> i, i -> i*2));

	@State(Scope.Benchmark)
	public static class HashMapRemoveState
	{
		@Setup(Level.Invocation)
		public void setup()
		{
			map = new HashMap<Integer, Integer>(INPUT);
		}

		public static Map<Integer, Integer> map;
	}

	@State(Scope.Benchmark)
	public static class LinkedHashMapRemoveState
	{
		@Setup(Level.Invocation)
		public void setup()
		{
			map = new LinkedHashMap<Integer, Integer>(INPUT);
		}

		public static Map<Integer, Integer> map;
	}

	@State(Scope.Benchmark)
	public static class TreeMapRemoveState
	{
		@Setup(Level.Invocation)
		public void setup()
		{
			map = new TreeMap<Integer, Integer>(INPUT);
		}

		public static Map<Integer, Integer> map;
	}

	@State(Scope.Benchmark)
	public static class TreeCountingMapRemoveState
	{
		@Setup(Level.Invocation)
		public void setup()
		{
			map = new TreeCountingMap<Integer, Integer>();
			map.putAll(INPUT);
		}

		public static Map<Integer, Integer> map;
	}

	@Benchmark
	public void			get_HashMap(Blackhole blackhole, HashMapRemoveState state)
	{
		INPUT.keySet().forEach(state.map::get);
		blackhole.consume(state.map.size());
	}

	@Benchmark
	public void			get_LinkedHashMap(Blackhole blackhole, LinkedHashMapRemoveState state)
	{
		INPUT.keySet().forEach(state.map::get);
		blackhole.consume(state.map.size());
	}

	@Benchmark
	public void			get_TreeMap(Blackhole blackhole, TreeMapRemoveState state)
	{
		INPUT.keySet().forEach(state.map::get);
		blackhole.consume(state.map.size());
	}

	@Benchmark
	public void			get_TreeCountingMap(Blackhole blackhole, TreeCountingMapRemoveState state)
	{
		INPUT.keySet().forEach(state.map::get);
		blackhole.consume(state.map.size());
	}

	@Benchmark
	public void			put_HashMap(Blackhole blackhole)
	{
		Map<Integer, Integer> map = new HashMap<Integer, Integer>();
		INPUT.forEach(map::put);
		blackhole.consume(map.size());
	}

	@Benchmark
	public void			put_LinkedHashMap(Blackhole blackhole)
	{
		Map<Integer, Integer> map = new LinkedHashMap<Integer, Integer>();
		INPUT.forEach(map::put);
		blackhole.consume(map.size());
	}

	@Benchmark
	public void			put_TreeMap(Blackhole blackhole)
	{
		Map<Integer, Integer> map = new TreeMap<Integer, Integer>();
		INPUT.forEach(map::put);
		blackhole.consume(map.size());
	}

	@Benchmark
	public void			put_TreeCountingMap(Blackhole blackhole)
	{
		Map<Integer, Integer> map = new TreeCountingMap<Integer, Integer>();
		INPUT.forEach(map::put);
		blackhole.consume(map.size());
	}

	@Benchmark
	public void			remove_HashMap(Blackhole blackhole, HashMapRemoveState state)
	{
		INPUT.keySet().forEach(state.map::remove);
		blackhole.consume(state.map.size());
	}

	@Benchmark
	public void			remove_LinkedHashMap(Blackhole blackhole, LinkedHashMapRemoveState state)
	{
		INPUT.keySet().forEach(state.map::remove);
		blackhole.consume(state.map.size());
	}

	@Benchmark
	public void			remove_TreeMap(Blackhole blackhole, TreeMapRemoveState state)
	{
		INPUT.keySet().forEach(state.map::remove);
		blackhole.consume(state.map.size());
	}

	@Benchmark
	public void			remove_TreeCountingMap(Blackhole blackhole, TreeCountingMapRemoveState state)
	{
		INPUT.keySet().forEach(state.map::remove);
		blackhole.consume(state.map.size());
	}
}
