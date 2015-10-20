/*
 * Copyright 2015 Zbynek Vyskovsky http://kvr.znj.cz/ http://github.com/kvr000/
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
import cz.znj.kvr.sw.pof.concurrent.lwfuture.concurrent.AbstractFutureListener;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Warmup;
import org.springframework.util.concurrent.ListenableFutureCallback;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

public class MixedListenerAsyncBenchmark
{
	public static final int         WARMUP_ITERATIONS = BenchmarkSupport.WARMUP_ITERATIONS;
	public static final int         COUNT = BenchmarkSupport.COUNT;

	@Benchmark
	@Warmup(iterations = WARMUP_ITERATIONS)
	@Measurement(iterations = 2, batchSize = 1)
	@Fork(warmups = 1, value = 1)
	public void                     benchmarkJdk() throws Exception
	{
		FutureTask[] array = BenchmarkSupport.populateJdkFutureArray(COUNT);
		for (FutureTask<Integer> f: array) {
			// nothing to add, no listenable
		}
		BenchmarkSupport.threadedRunFutures(array);
		for (FutureTask<Integer> f: array) {
			f.get();
		}
	}

	@Benchmark
	@Warmup(iterations = WARMUP_ITERATIONS)
	@Measurement(iterations = 2, batchSize = 1)
	@Fork(warmups = 1, value = 1)
	public void                     benchmarkLwFuture() throws ExecutionException, InterruptedException
	{
		cz.znj.kvr.sw.pof.concurrent.lwfuture.concurrent.ListenableFutureTask[] array = BenchmarkSupport.populateLwFutureArray(COUNT);
		for (cz.znj.kvr.sw.pof.concurrent.lwfuture.concurrent.ListenableFutureTask f: array) {
			f.addListener(new AbstractFutureListener<Integer>());
		}
		BenchmarkSupport.threadedRunFutures(array);
		for (cz.znj.kvr.sw.pof.concurrent.lwfuture.concurrent.ListenableFutureTask f: array) {
			f.addListener(new AbstractFutureListener<Integer>());
		}
		// skip futures.get() as we already handled in listeners
	}

	@Benchmark
	@Warmup(iterations = WARMUP_ITERATIONS)
	@Measurement(iterations = 2, batchSize = 1)
	@Fork(warmups = 1, value = 1)
	public void                     benchmarkGuava() throws ExecutionException, InterruptedException
	{
		Executor directExecutor = MoreExecutors.directExecutor();
		com.google.common.util.concurrent.ListenableFutureTask[] array = BenchmarkSupport.populateGuavaFutureArray(COUNT);
		for (com.google.common.util.concurrent.ListenableFutureTask f: array) {
			final Future<Integer> ff = f;
			f.addListener(() -> {
				try {
					ff.get();
				}
				catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
				catch (ExecutionException e) {
				}
			}, directExecutor);
		}
		BenchmarkSupport.threadedRunFutures(array);
		for (com.google.common.util.concurrent.ListenableFutureTask f: array) {
			final Future<Integer> ff = f;
			f.addListener(() -> {
				try {
					ff.get();
				}
				catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
				catch (ExecutionException e) {
				}
			}, directExecutor);
		}
		// skip futures.get() as we already handled in listeners
	}

	@Benchmark
	@Warmup(iterations = WARMUP_ITERATIONS)
	@Measurement(iterations = 2, batchSize = 1)
	@Fork(warmups = 1, value = 1)
	public void                     benchmarkSpring() throws ExecutionException, InterruptedException
	{
		org.springframework.util.concurrent.ListenableFutureTask[] array = BenchmarkSupport.populateSpringFutureArray(COUNT);
		for (org.springframework.util.concurrent.ListenableFutureTask f: array) {
			f.addCallback(new ListenableFutureCallback<Integer>() {
				@Override
				public void onFailure(Throwable ex) {
				}

				@Override
				public void onSuccess(Integer result) {
				}
			});
		}
		BenchmarkSupport.threadedRunFutures(array);
		for (org.springframework.util.concurrent.ListenableFutureTask f: array) {
			f.addCallback(new ListenableFutureCallback<Integer>() {
				@Override
				public void onFailure(Throwable ex) {
				}

				@Override
				public void onSuccess(Integer result) {
				}
			});
		}
		// skip futures.get() as we already handled in listeners
	}
}
