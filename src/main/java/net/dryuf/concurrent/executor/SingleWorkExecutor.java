package net.dryuf.concurrent.executor;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Semaphore;
import java.util.function.Function;


/**
 * Executor accepting work items, processing each item in its run.
 *
 * @param <T>
 *	work item
 * @param <R>
 *      result
 */
public class SingleWorkExecutor<T, R> implements WorkExecutor<T, R>
{
	private final CloseableExecutor executor;

	private final Function<T, R> processor;

	public SingleWorkExecutor(ExecutorService executor, Function<T, R> processor)
	{
		this.executor = new ClosingExecutor(executor);
		this.processor = processor;
	}

	@Override
	public CompletableFuture<R> submit(T work)
	{
		return CompletableFuture.supplyAsync(() -> processor.apply(work), executor);
	}

	@Override
	public void close()
	{
		executor.close();
	}
}
