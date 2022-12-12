package net.dryuf.concurrent.executor;

import net.dryuf.concurrent.queue.SingleConsumerQueue;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;


/**
 * Executor which serializes the pending tasks, executes them in a batch and then calls finisher.  The finisher runs
 * after every batch execution.
 *
 * The implementation is useful for processes where the subtasks must happen serialized and once finished, there is a
 * review to be executed examining current state.  This simplifies usage of {@link SingleConsumerQueue} .
 *
 * Underlying Runnable tasks must not fail, otherwise full executor would fail.  The submitted Callable task are
 * reported via return CompletableFuture.
 *
 * Usage:
 *
 * <pre>{@code
 *
 * 	Executor executor = FinishingSerializingExecutor(this::finisher, CommonPoolExecutor.getInstance());
 *
 *      executor.execute(() -> { doTaskOne(); });
 *      executor.execute(() -> { doTaskTwo(); });
 *      // at this point, finisher might have been executed one or two times (as soon as tasks are finished).
 *
 *      private void finisher()
 *      {
 *              checkWhatTasksDidAndUpdateState();
 *      }
 * }</pre>
 */
public class FinishingSerializingExecutor implements CloseableExecutor
{
	private final SingleConsumerQueue<Runnable> queue;

	private final CloseableExecutor runExecutor;

	/**
	 * Creates serializing executor from finisher callback.
	 *
	 * @param finisher
	 * 	callback called after added tasks are consumed
	 *
	 * @return
	 * 	executor for submitting tasks.
	 */
	public static Executor createFromFinisher(Runnable finisher)
	{
		return new FinishingSerializingExecutor(finisher, CommonPoolExecutor.getInstance());
	}

	/**
	 * Creates serializing executor from finisher callback.
	 *
	 * @param finisher
	 * 	callback called after added tasks are consumed
	 * @param runExecutor
	 * 	executor used to run the task consumer
	 *
	 * @return
	 * 	executor for submitting tasks.
	 */
	public static Executor createFromFinisher(Runnable finisher, CloseableExecutor runExecutor)
	{
		return new FinishingSerializingExecutor(finisher, runExecutor);
	}

	protected FinishingSerializingExecutor(Runnable finisher, CloseableExecutor runExecutor)
	{
		queue = new SingleConsumerQueue<Runnable>((this0) -> {
			try (SingleConsumerQueue<Runnable>.Consumer consumer = this0.consume()) {
				for (Runnable task; (task = consumer.next()) != null; )
					task.run();
				finisher.run();
			}
		});
		this.runExecutor = runExecutor;
	}

	@Override
	public void execute(Runnable runnable)
	{
		queue.add(runnable);
	}

	@Override
	public <T> CompletableFuture<T> submit(Callable<T> callable)
	{
		return new CompletableFutureTask<T>(callable) {
			{
				execute(this);
			}
		};
	}

	@Override
	public void close()
	{
		runExecutor.close();
	}
}
