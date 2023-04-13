package net.dryuf.base.collection;

import java.lang.ref.WeakReference;
import java.util.AbstractCollection;
import java.util.Iterator;
import java.util.NoSuchElementException;


/**
 * Array based Collection which detects leaking elements using WeakReference.  If element becomes null, it removes the
 * element and notifies the listener about leakage.
 *
 * The collection is thread safe and represents view of items valid at some point.
 *
 * Performance: The Collection is slow and synchronizing at removing or adding items, it is fast to iterate.  It should
 * be used in situations when updates are infrequent.
 *
 * <pre>
	private void example()
	{
		Collection&lt;Connection&gt; connections =
			new CopyOnWriteWeakLeakingCollection&lt;Connection&gt;(this::leakDetector);
	}

	private void leakDetector()
	{
		log.severe("Unreleased connection detected in: " + this);
	}
 * </pre>
 *
 * @param <E>
 *      type of element
 */
public class CopyOnWriteWeakLeakingCollection<E> extends AbstractCollection<E>
{
	@SuppressWarnings("rawtypes")
	private static final WeakReference[] EMPTY_ARRAY = new WeakReference[0];

	@SuppressWarnings("unchecked")
	private volatile WeakReference<E>[] items = (WeakReference<E>[]) EMPTY_ARRAY;

	private final Runnable leakNotifier;

	/**
	 * Constructs new instance.
	 *
	 * @param leakNotifier
	 *      runnable to notify when leak happens.
	 */
	public CopyOnWriteWeakLeakingCollection(Runnable leakNotifier)
	{
		this.leakNotifier = leakNotifier;
	}

	@SuppressWarnings("ResultOfMethodCallIgnored")
	@Override
	public Iterator<E> iterator()
	{
		return new Iterator<E>()
		{
			private final WeakReference<E>[] currentItems = items;

			private int i = 0;

			private E next;

			@Override
			public boolean hasNext()
			{
				while (i < currentItems.length) {
					next = currentItems[i].get();
					if (next != null) {
						return true;
					}
					else {
						removeReference(currentItems[i]);
						++i;
						if (leakNotifier != null) {
							leakNotifier.run();
						}
					}
				}
				return false;
			}

			@Override
			public E next()
			{
				if (next == null) {
					if (!hasNext()) {
						throw new NoSuchElementException();
					}
				}
				++i;
				E ret = next;
				next = null;
				return ret;
			}

			@Override
			public void remove()
			{
				removeReference(currentItems[i]);
			}
		};
	}

	@Override
	public int size()
	{
		return items.length;
	}

	@Override
	public synchronized boolean add(E e)
	{
		WeakReference<E>[] itemsLocal = items;
		@SuppressWarnings({"MismatchedReadAndWriteOfArray", "unchecked"})
		WeakReference<E>[] itemsNew = new WeakReference[itemsLocal.length + 1];
		System.arraycopy(itemsLocal, 0, itemsNew, 0, itemsLocal.length);
		itemsNew[itemsLocal.length] = new WeakReference<>(e);
		items = itemsNew;
		return false;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void clear()
	{
		items = (WeakReference<E>[]) EMPTY_ARRAY;
	}

	@SuppressWarnings("UnusedReturnValue")
	private synchronized boolean removeReference(WeakReference<E> ref)
	{
		WeakReference<E>[] itemsLocal = items;
		int found;
		for (found = 0; ; ++found) {
			if (found >= itemsLocal.length) {
				return false;
			}
			else if (itemsLocal[found] == ref) {
				break;
			}
		}
		if (itemsLocal.length == 1) {
			clear();
		}
		else {
			@SuppressWarnings({"unchecked"})
			WeakReference<E>[] itemsNew = new WeakReference[itemsLocal.length-1];
			System.arraycopy(itemsLocal, 0, itemsNew, 0, found);
			System.arraycopy(itemsLocal, found+1, itemsNew, found, itemsLocal.length-found-1);
			items = itemsNew;
		}
		return true;
	}
}
