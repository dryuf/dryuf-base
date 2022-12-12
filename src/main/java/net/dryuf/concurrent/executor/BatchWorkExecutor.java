package net.dryuf.concurrent.executor;

import lombok.AllArgsConstructor;
import net.dryuf.concurrent.FutureUtil;
import net.dryuf.concurrent.function.ThrowingFunction;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;


/**
 * Executor accepting work items, processing items in batches.
 *
 * <pre>
 *         try (WorkExecutor&lt;Integer, Integer&gt; executor = new BatchWorkExecutor&lt;&gt;(delegateExecutor, l -> l.stream().map(v -> v*v).collect(toList())) {
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
public class BatchWorkExecutor<T, R> implements WorkExecutor<T, R>
{
	static int PENDING_MAX = Integer.MAX_VALUE;

	private final CloseableExecutor executor;

	private final ThrowingFunction<List<T>, List<CompletableFuture<R>>> processor;

	private final int batchSize;

	private volatile Node<T, R> pending;

	/** Number of batchStarter operations in progress.  Or-ed by Integer.MIN_VALUE if close is waiting. */
	private volatile int batchPending = 0;

	@SuppressWarnings("rawtypes")
	private static final AtomicReferenceFieldUpdater<BatchWorkExecutor, Node> PENDING_UPDATER =
		AtomicReferenceFieldUpdater.newUpdater(BatchWorkExecutor.class, Node.class, "pending");

	@SuppressWarnings("rawtypes")
	private static final AtomicIntegerFieldUpdater<BatchWorkExecutor> BATCH_PENDING_UPDATER =
		AtomicIntegerFieldUpdater.newUpdater(BatchWorkExecutor.class, "batchPending");

	/**
	 * Creates instance from {@link ExecutorService}, not shutting it down upon close.
	 *
	 * @param executor
	 * 	underlying executor
	 * @param batchSize
	 * 	max number of work items sent at once to processor
	 * @param processor
	 * 	processing function
	 */
	public BatchWorkExecutor(ExecutorService executor, int batchSize, ThrowingFunction<List<T>, List<CompletableFuture<R>>> processor)
	{
		this(new NotClosingExecutor(executor), batchSize, processor);
	}

	/**
	 * Creates instance from {@link CloseableExecutor}, closing it upon close.
	 *
	 * @param executor
	 * 	underlying executor
	 * @param batchSize
	 * 	max number of work items sent at once to processor
	 * @param processor
	 * 	processing function
	 */
	public BatchWorkExecutor(CloseableExecutor executor, int batchSize, ThrowingFunction<List<T>, List<CompletableFuture<R>>> processor)
	{
		this.executor = executor;
		this.batchSize = batchSize;
		this.processor = processor;
	}

	@Override
	public CompletableFuture<R> submit(T work)
	{
		boolean interrupted = false;
		try {
			CompletableFuture<R> future = new CompletableFuture<>();
			for (;;) {
				Node<T, R> oldPending = pending;
				if (oldPending != null && oldPending.count == PENDING_MAX) {
					synchronized (this) {
						oldPending = pending;
						if (oldPending != null && oldPending.count == PENDING_MAX) {
							try {
								wait();
							}
							catch (InterruptedException e) {
								interrupted = true;
							}
						}
						continue;
					}
				}
				Node<T, R> node = new Node<>(oldPending != null ? oldPending.count+1 : 1, oldPending, work, future);
				if (PENDING_UPDATER.compareAndSet(this, oldPending, node)) {
					if (oldPending == null) {
						for (;;) {
							int old = batchPending;
							if ((old&Integer.MIN_VALUE) != 0) {
								throw new RejectedExecutionException("Executor closed");
							}
							if (BATCH_PENDING_UPDATER.compareAndSet(this, old, old+1)) {
								break;
							}
						}
						executor.execute(this::batchStarter);
					}
					break;
				}
			}
			return future;
		}
		finally {
			if (interrupted) {
				Thread.currentThread().interrupt();
			}
		}
	}

	@Override
	public void close()
	{
		boolean interrupted = false;
		for (;;) {
			int old = batchPending;
			if ((old&Integer.MAX_VALUE) == 0) {
				if (BATCH_PENDING_UPDATER.compareAndSet(this, old, Integer.MIN_VALUE)) {
					break;
				}
			}
			else {
				if (BATCH_PENDING_UPDATER.compareAndSet(this, old, old | Integer.MIN_VALUE)) {
					synchronized (this) {
						if ((batchPending&Integer.MAX_VALUE) != 0) {
							try {
								wait();
							}
							catch (InterruptedException e) {
								interrupted = true;
							}
						}
					}
				}
			}
		}
		executor.close();
		if (interrupted) {
			Thread.currentThread().interrupt();
		}
	}

	private void batchStarter()
	{
		@SuppressWarnings("unchecked")
		Node<T, R> last = PENDING_UPDATER.getAndSet(this, null);
		try {
			int size = 0;
			for (Node<T, R> n = last; n != null; ++size, n = n.next) ;
			@SuppressWarnings({ "unchecked", "rawtypes" })
			List<T> works = (List) Arrays.asList(new Object[size]);
			@SuppressWarnings("unchecked")
			List<CompletableFuture<R>> futures = Arrays.asList(new CompletableFuture[size]);
			{
				int i = size-1;
				for (Node<T, R> n = last; n != null; n = n.next, --i) {
					works.set(i, n.work);
					futures.set(i, n.future);
				}
			}
			for (int i = batchSize; i < size; i += batchSize) {
				int s = i, e = Math.min(i+batchSize, size);
				executor.execute(() -> runBatch(works.subList(s, e), futures.subList(s, e)));
			}
			{
				int s = 0, e = Math.min(batchSize, works.size());
				runBatch(works.subList(s, e), futures.subList(s, e));
			}
		}
		catch (Throwable ex) {
			for (Node<T, R> n = last; n != null; n = n.next) {
				n.future.completeExceptionally(ex);
			}
		}
		finally {
			if (last.count == PENDING_MAX) {
				synchronized (this) {
					notifyAll();
				}
			}
			for (;;) {
				int old = batchPending;
				if (BATCH_PENDING_UPDATER.compareAndSet(this, old, old-1)) {
					if ((old&Integer.MIN_VALUE) != 0) {
						synchronized (this) {
							notifyAll();
						}
					}
					break;
				}
			}
		}
	}

	void runBatch(List<T> works, List<CompletableFuture<R>> futures)
	{
		try {
			List<CompletableFuture<R>> results = processor.apply(works);
			for (int j = 0, e = works.size(); j < e; ++j) {
				CompletableFuture<R> future = futures.get(j);
				try {
					results.get(j).handle((v, x) -> FutureUtil.completeOrFail(future, v, x));
				}
				catch (Throwable ex) {
					future.completeExceptionally(ex);
				}
			}
		}
		catch (Throwable ex) {
			for (int j = 0, e = works.size(); j < e; ++j) {
				CompletableFuture<R> future = futures.get(j);
				future.completeExceptionally(ex);
			}
		}
	}

	@AllArgsConstructor
	private static class Node<T, R>
	{
		int count;

		Node<T, R> next;

		final T work;

		final CompletableFuture<R> future;
	}
}
