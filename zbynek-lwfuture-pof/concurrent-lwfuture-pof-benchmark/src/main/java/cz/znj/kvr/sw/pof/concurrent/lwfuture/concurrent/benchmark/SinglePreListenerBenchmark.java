/*
 * Copyright 2015 Zbynek Vyskovsky mailto:kvr@centrum.cz http://kvr.znj.cz/ http://github.com/kvr000/
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

package cz.znj.kvr.sw.pof.concurrent.lwfuture.concurrent.benchmark;

import com.google.common.util.concurrent.MoreExecutors;
import cz.znj.kvr.sw.pof.concurrent.lwfuture.concurrent.DefaultFutureListener;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Warmup;
import org.springframework.util.concurrent.ListenableFutureCallback;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

public class SinglePreListenerBenchmark
{
	public static final int         WARMUP_ITERATIONS = BenchmarkSupport.WARMUP_ITERATIONS;
	public static final int         COUNT = BenchmarkSupport.COUNT;

	@Benchmark
	@Warmup(iterations = WARMUP_ITERATIONS)
	@Measurement(iterations = 2, batchSize = 1)
	@Fork(warmups = 1, value = 1)
	public void                     benchmarkJdk() throws Exception
	{
		for (long i = 0; i < COUNT; ++i) {
			FutureTask<Integer> future = new FutureTask<Integer>(() -> {
				return 0;
			});
			future.run();
			future.get();
		}
	}

	@Benchmark
	@Warmup(iterations = WARMUP_ITERATIONS)
	@Measurement(iterations = 2, batchSize = 1)
	@Fork(warmups = 1, value = 1)
	public void                     benchmarkLwFuture() throws ExecutionException, InterruptedException
	{
		for (long i = 0; i < COUNT; ++i) {
			cz.znj.kvr.sw.pof.concurrent.lwfuture.concurrent.ListenableFutureTask<Integer> future = new cz.znj.kvr.sw.pof.concurrent.lwfuture.concurrent.ListenableFutureTask<Integer>(() -> {
				return 0;
			});
			future.addListener(new DefaultFutureListener<Integer>());
			future.run();
		}
	}

	@Benchmark
	@Warmup(iterations = WARMUP_ITERATIONS)
	@Measurement(iterations = 2, batchSize = 1)
	@Fork(warmups = 1, value = 1)
	public void                     benchmarkGuava() throws ExecutionException, InterruptedException
	{
		for (long i = 0; i < COUNT; ++i) {
			com.google.common.util.concurrent.ListenableFutureTask<Integer> future = com.google.common.util.concurrent.ListenableFutureTask.create(() -> {
				return 0;
			});
			future.addListener(() -> {
				try {
					future.get();
				}
				catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
				catch (ExecutionException e) {
					throw new RuntimeException(e);
				}
			}, MoreExecutors.directExecutor());
			future.run();
		}
	}

	@Benchmark
	@Warmup(iterations = WARMUP_ITERATIONS)
	@Measurement(iterations = 2, batchSize = 1)
	@Fork(warmups = 1, value = 1)
	public void                     benchmarkSpring() throws ExecutionException, InterruptedException
	{
		for (long i = 0; i < COUNT; ++i) {
			org.springframework.util.concurrent.ListenableFutureTask<Integer> future = new org.springframework.util.concurrent.ListenableFutureTask<Integer>(() -> {
				return 0;
			});
			future.addCallback(new ListenableFutureCallback<Integer>() {
				@Override
				public void onFailure(Throwable ex) {
				}

				@Override
				public void onSuccess(Integer result) {
				}
			});
			future.run();
		}
	}
}
