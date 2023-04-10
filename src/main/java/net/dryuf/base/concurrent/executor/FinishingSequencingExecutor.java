package net.dryuf.base.concurrent.executor;

import net.dryuf.base.concurrent.queue.SingleConsumerQueue;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;


/**
 * Executor which runs the submitted tasks in order of submission.  Once the tasks queue is finished, it calls finisher
 * function to review the current state.
 *
 * The implementation is useful for processes where the subtasks must happen sequentially and once finished, there is a
 * review to be executed examining current state.  This simplifies usage of {@link SingleConsumerQueue} .
 *
 * Underlying Runnable tasks must not fail, otherwise full executor would fail.  The submitted Callable task are
 * reported via returned CompletableFuture.
 *
 * Usage:
 *
 * <pre>{@code
 *
 * 	Executor executor = FinishingSequencingExecutor(this::finisher, CommonPoolExecutor.getInstance());
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
public class FinishingSequencingExecutor implements CloseableExecutor
{
	private final SingleConsumerQueue<Runnable> queue;

	private final CloseableExecutor runExecutor;

	/**
	 * Creates sequencing executor from finisher callback.
	 *
	 * @param finisher
	 * 	callback called after added tasks are consumed
	 *
	 * @return
	 * 	executor for submitting tasks.
	 */
	public static Executor createFromFinisher(Runnable finisher)
	{
		return new FinishingSequencingExecutor(finisher, CommonPoolExecutor.getInstance());
	}

	/**
	 * Creates sequencing executor from finisher callback.
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
		return new FinishingSequencingExecutor(finisher, runExecutor);
	}

	protected FinishingSequencingExecutor(Runnable finisher, CloseableExecutor runExecutor)
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
