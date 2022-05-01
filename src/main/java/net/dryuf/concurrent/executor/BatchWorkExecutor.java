package net.dryuf.concurrent.executor;

import lombok.AllArgsConstructor;
import net.dryuf.concurrent.FutureUtil;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.function.Function;


/**
 * Executor accepting work items, processing items in batches.
 *
 * @param <T>
 *	work item
 * @param <R>
 *      result
 */
public class BatchWorkExecutor<T, R> implements WorkExecutor<T, R>
{
	private final CloseableExecutor executor;

	private final Function<List<T>, List<CompletableFuture<R>>> processor;

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

	public BatchWorkExecutor(CloseableExecutor executor, int batchSize, Function<List<T>, List<CompletableFuture<R>>> processor)
	{
		this.executor = executor;
		this.batchSize = batchSize;
		this.processor = processor;
	}

	@Override
	public CompletableFuture<R> submit(T work)
	{
		CompletableFuture<R> future = new CompletableFuture<>();
		for (;;) {
			Node<T, R> oldPending = pending;
			Node<T, R> node = new Node<>(pending, work, future);
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
					CompletableFuture.runAsync(this::batchStarter, executor);
				}
				break;
			}
		}
		return future;
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
		try {
			@SuppressWarnings("unchecked")
			Node<T, R> node = PENDING_UPDATER.getAndSet(this, null);
			int count = 0;
			for (Node<T, R> n = node; n != null; ++count, n = n.next) ;
			@SuppressWarnings("unchecked")
			List<T> works = (List) Arrays.asList(new Object[count]);
			@SuppressWarnings("unchecked")
			List<CompletableFuture<R>> futures = Arrays.asList(new CompletableFuture[count]);
			{
				int i = count-1;
				for (Node<T, R> n = node; n != null; n = n.next, --i) {
					works.set(i, n.work);
					futures.set(i, n.future);
				}
			}
			for (int i = 0; i < count; i += batchSize) {
				int s = i, e = Math.min(i+batchSize, count);
				executor.execute(() -> {
					try {
						List<CompletableFuture<R>> results = processor.apply(works.subList(s, e));
						for (int j = 0; j < e-s; ++j) {
							CompletableFuture<R> future = futures.get(s+j);
							try {
								results.get(j).handle((v, x) -> FutureUtil.completeOrFail(future, v, x));
							}
							catch (Throwable ex) {
								future.completeExceptionally(ex);
							}
						}
					}
					catch (Throwable ex) {
						for (int j = 0; j < e-s; ++j) {
							CompletableFuture<R> future = futures.get(s+j);
							future.completeExceptionally(ex);
						}
					}
				});
			}
		}
		finally {
			for (; ; ) {
				int old = batchPending;
				if (BATCH_PENDING_UPDATER.compareAndSet(this, old, old-1)) {
					if ((old&Integer.MIN_VALUE) != 0) {
						synchronized (this) {
							notify();
						}
					}
					break;
				}
			}
		}
	}

	@AllArgsConstructor
	private static class Node<T, R>
	{
		Node<T, R> next;

		final T work;

		final CompletableFuture<R> future;
	}
}
