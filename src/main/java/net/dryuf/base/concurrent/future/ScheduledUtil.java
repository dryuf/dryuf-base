package net.dryuf.base.concurrent.future;

import net.dryuf.base.concurrent.sync.RunSingle;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;


public class ScheduledUtil
{
	/** Time (in ns) to safely propagate memory to caches without using explicit memory barrier in user code
	 * (they're used either by JVM during stop-the-world or by operating system when switching context. */
	public static final long	MEMORY_PROPAGATION_DELAY_NS =
		Optional.ofNullable(System.getProperty("net.dryuf.concurrent.memoryPropagationDelay"))
			.map(Long::valueOf)
			.orElse(1_200_000_000L);

	/**
	 * Gets instance of shared {@link ScheduledExecutorService} .  This instance should be used for non-blocking
	 * calls only.
	 *
	 * @return
	 *      shared {@link ScheduledExecutorService} .
	 */
	public static ScheduledExecutorService sharedExecutor()
	{
		return sharedExecutor;
	}

	/**
	 * Schedules a callable until it succeeds.  This is useful for implementing retries.
	 *
	 * There will be only one callable running at a time.  If there is one running, the others will be ignored
	 * until it finishes.  Note that {@code null} is a valid result.
	 *
	 * <pre>
	        CompletableFuture&lt;Connection&gt; connection = ScheduleUtil.scheduleWithFixedDelayUntilSuccess(
	                ScheduleUtil.sharedExecutor(),
	                () -> getPooledConnectionNowOrThrow()
	                1,
	                TimeUnit.SECONDS
	        );
	 * </pre>
	 *
	 * @param executor
	 *      executor to execute callable on
	 * @param callable
	 *      callable producing the result
	 * @param period
	 *      period to wait between attempts
	 * @param timeUnit
	 *      time unit for period
	 *
	 * @return
	 *      future to be completed once callable succeeds.
	 *
	 * @param <R>
	 *      type of returned future
	 */
	public static <R> CompletableFuture<R> scheduleWithFixedDelayUntilSuccess(
		ScheduledExecutorService executor,
		Callable<R> callable,
		long period,
		TimeUnit timeUnit
	)
	{
		CompletableFuture<R> future = new CompletableFuture<R>();
		executor.scheduleWithFixedDelay(
			() -> {
				if (future.isDone())
					throw new CancellationException();
				try {
					future.complete(callable.call());
				}
				catch (Throwable ex) {
					return;
				}
				throw new CancellationException();
			},
			0,
			period,
			timeUnit
		);
		return future;
	}

	/**
	 * Schedules a callable until it returns successfully completed future.  This is useful for implementing
	 * retries happening asynchronously.
	 *
	 * There will be only one callable running at a time.  If there is one running, the others will be ignored
	 * until its result completes.  The callable must return valid CompletableFuture, either failed or succeeded.
	 *
	 * <pre>
		CompletableFuture&lt;Connection&gt; connection = ScheduleUtil.scheduleWithFixedDelayUntilComposedSuccess(
	                ScheduleUtil.sharedExecutor(),
	                () -> attemptConnectAsync(),
	                1,
	                TimeUnit.SECONDS
	        );
	 * </pre>
	 *
	 * @param executor
	 *      executor to execute callable on
	 * @param callable
	 *      callable producing the result
	 * @param period
	 *      period to wait between attempts
	 * @param timeUnit
	 *      time unit for period
	 *
	 * @return
	 *      future to be completed once callable succeeds.
	 *
	 * @param <R>
	 *      type of returned future
	 * @param <F>
	 *      type of callable return
	 */
	public static <R, F extends CompletionStage<R>> CompletableFuture<R> scheduleWithFixedDelayUntilComposedSuccess(
		ScheduledExecutorService executor,
		Callable<F> callable,
		long period,
		TimeUnit timeUnit
	)
	{
		RunSingle runSingle = new RunSingle();
		CompletableFuture<R> future = new CompletableFuture<R>();
		executor.scheduleWithFixedDelay(
			() -> {
				runSingle.composeStage(() -> {
						if (future.isDone())
							throw new CancellationException();
						try {
							return Objects.requireNonNull(callable.call());
						}
						catch (Throwable ex) {
							return FutureUtil.exception(ex);
						}
					})
					.thenAccept(future::complete);
			},
			0,
			period,
			timeUnit
		);
		return future;
	}

	private ScheduledUtil()
	{
	}

	private static final ScheduledExecutorService sharedExecutor =
		Executors.newScheduledThreadPool(
			Runtime.getRuntime().availableProcessors(),
			new ThreadFactory() {
				AtomicInteger counter = new AtomicInteger();

				@Override
				public Thread newThread(Runnable r) {
					Thread t = Executors.defaultThreadFactory().newThread(r);
					t.setName(ScheduledUtil.class.getName() + -counter.incrementAndGet());
					t.setDaemon(true);
					return t;
				}
			}
		);
}
