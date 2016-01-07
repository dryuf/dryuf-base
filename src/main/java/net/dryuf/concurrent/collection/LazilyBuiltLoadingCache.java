package net.dryuf.concurrent.collection;

import net.dryuf.concurrent.SharedScheduledExecutorInstance;

import java.lang.ref.SoftReference;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.function.Function;


/**
 * The {@link LazilyBuiltLoadingCache} serves as thread safe but still lock-free implementation of Map. Common usage of
 * this class is caching of program metadata where the structure is built over short period at the beginning and later
 * becomes effectively fixed.
 *
 * The implementation performs slower in the first stage (couple of seconds while the cache is populated) but has
 * HashMap performance for the rest of application lifetime which is about 4-5 times faster on x86_64 and may be even
 * more significant on more memory relaxed architectures.
 *
 * @param <K>
 * 	type of map key
 * @param <V>
 *      type of map value
 *
 * @apiNote thread safe
 */
public class LazilyBuiltLoadingCache<K, V> implements Function<K, V>
{
	/**
	 * Constructs new instance of {@link LazilyBuiltLoadingCache}.
	 *
	 * @param loader
	 * 	the underlying function providing mapping between key and value.
	 */
	public				LazilyBuiltLoadingCache(Function<K, V> loader)
	{
		this.loader = loader;
	}

	/**
	 * @inheritDoc
	 *
	 * The method will never return null.
	 */
	@Override
	public V			apply(K key)
	{
		V value;

		if ((value = snapshotMap.get(key)) != null) {
			return value;
		}

		if ((value = latestMap.computeIfAbsent(key, loader)) == null) {
			throw new IllegalStateException("Provider function returned null value");
		}

		if (snapshotStatusUpdater.getAndSet(this, SNAPSHOT_DIRTY) == SNAPSHOT_DONE) {
			scheduleUpdate(BUILD_DELAY_NS);
		}

		return value;
	}

	/**
	 * Schedules the next propagation of latest changes to lock-free snapshot.
	 *
	 * @param delayMs
	 * 	delay in milliseconds
	 */
	private void			scheduleUpdate(long delayMs)
	{
		SharedScheduledExecutorInstance.getScheduledExecutorService().schedule(
				() -> snapshotBuilder(new SoftReference<>(LazilyBuiltLoadingCache.this)),
				delayMs,
				TimeUnit.NANOSECONDS
		);
	}

	/**
	 * Updates the snapshot. Checks the current status first and either updates pendingMap or propagates to
	 * snapshotMap.
	 *
	 * There is always only one scheduled snapshotBuilder at a time. It's either scheduled by apply() method when
	 * the status is DONE. Or, it's rescheduled by snapshotBuilder() when setting next step or status is updated
	 * concurrently by apply().
	 *
	 * The SoftReference is used to eliminate memory footprint in case this data structure is used only as
	 * temporary object. That way, only scheduled task will remain in queue until it is executed.
	 */
	private static <K, V> void	snapshotBuilder(SoftReference<LazilyBuiltLoadingCache<K, V>> selfReference)
	{
		LazilyBuiltLoadingCache<K, V> self = selfReference.get();
		if (self == null) {
			System.err.println("REFERENCE IS DEAD\nDEAD\nDEAD\nDEAD");
			return;
		}

		for (;;) {
			switch (self.snapshotStatus) {
			case SNAPSHOT_DONE:
			case SNAPSHOT_PROGRESS:
				return;

			case SNAPSHOT_DIRTY:
				self.pendingMap = new HashMap<>(self.latestMap);
				if (!snapshotStatusUpdater.compareAndSet(self, SNAPSHOT_DIRTY, SNAPSHOT_PROGRESS))
					continue;
				if (snapshotStatusUpdater.getAndSet(self, SNAPSHOT_BUILT) == SNAPSHOT_DIRTY) {
					self.snapshotStatus = SNAPSHOT_DIRTY;
					self.scheduleUpdate(BUILD_DELAY_NS);
					return;
				}
				self.scheduleUpdate(UPDATE_DELAY_NS);
				return;

			case SNAPSHOT_BUILT:
				self.snapshotMap = self.pendingMap;
				if (snapshotStatusUpdater.compareAndSet(self, SNAPSHOT_BUILT, SNAPSHOT_DONE)) {
					return;
				}
				self.scheduleUpdate(BUILD_DELAY_NS);
				return;

			default:
				assert false: "Unexpected value in snapshotStatus"+self.snapshotStatus;
			}
		}
	}

	/** No updates to snapshot required. */
	private static final int	SNAPSHOT_DONE = 0;
	/** Snapshot update currently in progress, temporary value to mark the ongoing work. */
	private static final int	SNAPSHOT_PROGRESS = 1;
	/** Data is dirty, latestMap contains newest version. */
	private static final int	SNAPSHOT_DIRTY = 2;
	/** Data is built to pendingMap, needs to be propagated to snapshotMap. */
	private static final int	SNAPSHOT_BUILT = 3;

	/** Data loader. */
	private Function<K, V>		loader;

	/** The immutable snapshot map, lock-free. */
	private Map<K, V>		snapshotMap = Collections.emptyMap();

	/** Temporary map containing latest data but not yet propagated. */
	private Map<K, V>		pendingMap;

	/** Latest data, mutable and thread-safe. */
	private ConcurrentHashMap<K, V>	latestMap = new ConcurrentHashMap<>();

	/** Current propagation status. */
	private volatile int		snapshotStatus = SNAPSHOT_DONE;

	/** Delay until snapshotMap is set from pendingMap. Must be long enough to propagate (invalidate) pendingMap
	 * content among caches. */
	static final long		UPDATE_DELAY_NS = SharedScheduledExecutorInstance.MEMORY_PROPAGATION_DELAY_NS;

	/** Delay until pendingMap is built from work data. */
	static final long		BUILD_DELAY_NS = Math.min(1000, Math.max(SharedScheduledExecutorInstance.MEMORY_PROPAGATION_DELAY_NS/10, 1));

	/** Updater to snapshotStatus instance variable. */
	private static final AtomicIntegerFieldUpdater<LazilyBuiltLoadingCache> snapshotStatusUpdater = AtomicIntegerFieldUpdater.newUpdater(LazilyBuiltLoadingCache.class, "snapshotStatus");
}
