package net.dryuf.concurrent;

import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;


/**
 * Instance of ListeningScheduledExecutor, to be shared by meta caches.
 */
public class SharedScheduledExecutorInstance
{
	/** Time (in ns) to safely propagate memory to caches without using explicit memory barrier in user code
	 * (they're used either by JVM during stop-the-world or by operating system when switching context. */
	public static final long	MEMORY_PROPAGATION_DELAY_NS =
			Optional.ofNullable(System.getProperty("net.dryuf.concurrent.memoryPropagationDelay"))
			.map(Long::valueOf)
			.orElse(1_200_000_000L);

	/**
	 * Gets the instance of shared ListeningScheduledExecutor.
	 *
	 * @return
	 * 	instance of shared ListeningScheduledExecutor.
	 */
	public static ListeningScheduledExecutorService getScheduledExecutorService()
	{
		return scheduledExecutorService;
	}

	private static final ListeningScheduledExecutorService scheduledExecutorService =
			ListeningExecutors.listeningDecorator(Executors.newSingleThreadScheduledExecutor(
					new ThreadFactory() {
						@Override
						public Thread newThread(Runnable r) {
							Thread t = Executors.defaultThreadFactory().newThread(r);
							t.setDaemon(true);
							return t;
						}
					}
			));
}
