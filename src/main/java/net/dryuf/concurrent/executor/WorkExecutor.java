package net.dryuf.concurrent.executor;

import java.util.concurrent.CompletableFuture;


/**
 * Executor accepting work items.
 *
 * <pre>
 *         try (WorkExecutor&lt;Integer, Integer&gt; executor = new SingleWorkExecutor&lt;&gt;(delegateExecutor, v -> v*v)) {
 *         	CompletableFuture&lt;Integer&gt; v1 = executor.submit(1);
 *         	CompletableFuture&lt;Integer&gt; v5 = executor.submit(5);
 *         	assertEquals(v1.get(), 1);
 *         	assertEquals(v5.get(), 25);
 *         }
 *         // at this point, all executions are finished (successfully or not), underlying executor closed if closeable
 * </pre>
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
