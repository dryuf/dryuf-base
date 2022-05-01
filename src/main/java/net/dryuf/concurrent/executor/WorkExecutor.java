package net.dryuf.concurrent.executor;

import java.util.concurrent.CompletableFuture;


/**
 * Executor accepting work items.
 *
 * @param <T>
 *	work item
 * @param <R>
 *      result
 */
public interface WorkExecutor<T, R> extends AutoCloseable
{
	/**
	 * Submits work item for processing.
	 *
	 * @param work
	 * 	work item
	 *
	 * @return
	 * 	result future
	 */
	CompletableFuture<R> submit(T work);

	@Override
	void close();
}
