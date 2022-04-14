/*
 * Copyright 2015-2022 Zbynek Vyskovsky mailto:kvr000@gmail.com http://github.com/kvr000/
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

package net.dryuf.concurrent.executor;

import lombok.AllArgsConstructor;
import lombok.experimental.Delegate;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;


/**
 * Closeable Executor, waiting for futures upon finish.
 */
@AllArgsConstructor
public class ClosingExecutor implements CloseableExecutor
{
	@Delegate
	private final ExecutorService executor;

	@Override
	public <T> CompletableFuture<T> submit(Callable<T> callable)
	{
		return new CompletableFuture<T>() {
			Future<?> future;

			{
				this.future = executor.submit(() -> {
					try {
						complete(callable.call());
					}
					catch (Throwable ex) {
						completeExceptionally(ex);
					}
				});
			}

			@Override
			public boolean cancel(boolean interrupt)
			{
				super.cancel(interrupt);
				return future.cancel(interrupt);
			}
		};
	}

	@Override
	public void close()
	{
		boolean interrupted = false;
		executor.shutdown();
		for (;;) {
			try {
				if (executor.awaitTermination(Integer.MAX_VALUE, TimeUnit.SECONDS))
					break;
			}
			catch (InterruptedException e) {
				interrupted = true;
			}
			// just repeat the wait
		}
		if (interrupted)
			Thread.currentThread().interrupt();
	}
}
