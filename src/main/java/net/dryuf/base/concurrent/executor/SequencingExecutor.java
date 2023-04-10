/*
 * Copyright 2015-2022 Zbynek Vyskovsky mailto:kvr000@gmail.com http://github.com/kvr000/ https://github.com/dryuf/ https://www.linkedin.com/in/zbynek-vyskovsky/
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.dryuf.base.concurrent.executor;

import lombok.AllArgsConstructor;
import lombok.extern.java.Log;
import net.dryuf.base.concurrent.future.FutureUtil;

import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.logging.Level;


/**
 * Executor sequentially executed the tasks.
 *
 * The tasks are run in order of submissions and they run sequentially, no two running at the same time.  It waits for
 * submitted task to finish upon close.
 *
 * Usage:
 *
 * <pre>
 *         try (SequencingExecutor executor = new SequencingExecutor()) {
 *            	CompletableFuture future = executor.submit(() -> System.out.println("25"));
 *            	CompletableFuture future = executor.submit(() -> System.out.println("36"));
 *         }
 *         // the above tasks will be completed at this point.
 *         // the above will always print 25 and then 36, the tasks will never run in parallel
 * </pre>
 */
@Log
public class SequencingExecutor extends AbstractCloseableExecutor
{
	/**
	 * Creates instance from executor, closing it upon close.
	 *
	 * @param executor
	 * 	underlying executor
	 */
	public SequencingExecutor(CloseableExecutor executor)
	{
		this.executor = executor;
	}

	/**
	 * Creates instance from executor, not closing it upon close.
	 *
	 * @param executor
	 * 	underlying executor
	 */
	public SequencingExecutor(Executor executor)
	{
		this(new UncontrolledCloseableExecutor(executor));
	}

	/**
	 * Creates instance from common pool executor.
	 */
	public SequencingExecutor()
	{
		this(CommonPoolExecutor.getInstance());
	}

	@Override
	protected void execute0(Runnable runnable)
	{
		// All:
		// Any update to locked state is protected by synchronized (lock) .
		// Producer:
		// If empty (synchronized), it acquires a LOCK, runs the executor and after that inserts the task into pending queue.
		// The above is required to not attempt to modify pending queue until sure that executor will run.
		// Consumer:
		// It synchronizes at the beginning to wait until task is inserted into queue.
		// It takes the current queue, reverses the order and executes tasks one by one.
		// At the end, it compares the pending tasks and if new was added, it repeats the above steps.
		// If no task was added, it will update pending to null and exits.
		// If lock is set, it will notify any waiting threads (which should be only one running close() ).
		Objects.requireNonNull(runnable, "runnable must not be null");
		for (;;) {
			Node next = pending;
			if (next == null) {
				synchronized (lock) {
					if (PENDING_UPDATER.compareAndSet(this, null, lock)) {
						try {
							executor.execute(this::executePending);
							pending = new Node(null, runnable);
							break;
						}
						catch (RejectedExecutionException ex) {
							lock.rejected = ex;
							lock.closed |= 1;
							throw ex;
						}
						catch (Throwable ex) {
							pending = null;
							throw ex;
						}
					}
				}
			}
			else if (next == lock) {
				if (lock.closed > 0) {
					throw new RejectedExecutionException("Executor closed", lock.rejected);

				}
				synchronized (lock) {
					// just waiting for the thread currently holding the lock to finish its update
				}
			}
			else {
				Node current = new Node(next, runnable);
				if (PENDING_UPDATER.compareAndSet(this, next, current)) {
					break;
				}
			}
		}
	}

	private void executePending()
	{
		synchronized (lock) {
		}
		try {
			Node end = null;
			for (;;) {
				Node newEnd = pending;
				Node first = newEnd;
				Node next = first.next;
				if (first == lock) {
					first = next;
					if (first == end) {
						synchronized (lock) {
							lock.closed |= 2;
							lock.notify();
							break;
						}
					}
					next = first.next;
				}
				first.next = null;
				for (; next != end; ) {
					Node nextNext = next.next;
					next.next = first;
					first = next;
					next = nextNext;
				}

				while (first != null) {
					try {
						first.task.run();
					}
					catch (Throwable ex) {
						// ignore failure in runnable, should be reported by Future inside instead
					}
					first = first.next;
				}

				if (newEnd == lock) {
					synchronized (lock) {
						lock.closed |= 2;
						lock.notify();
					}
					break;
				}
				else if (PENDING_UPDATER.compareAndSet(this, newEnd, null)) {
					break;
				}
				end = newEnd;
			}
		}
		catch (Throwable ex) {
			log.log(Level.SEVERE, "Unexpected error in executePending, SequencingExecutor state is unstable", ex);
		}
	}

	@Override
	public void close()
	{
		synchronized (lock) {
			if ((lock.closed&4) == 0) {
				lock.closed |= 4;
				for (;;) {
					Node next = pending;
					if (next == lock) {
						if (lock.next != null && (lock.closed&2) == 0) {
							FutureUtil.waitUninterruptiblyKeepInterrupt(lock);
						}
						break;
					}
					else {
						lock.next = next;
						if (PENDING_UPDATER.compareAndSet(this, next, lock)) {
							if (next != null) {
								FutureUtil.waitUninterruptiblyKeepInterrupt(lock);
							}
							break;
						}
					}
				}
				executor.close();
			}
		}
	}

	@AllArgsConstructor
	private static class Node
	{
		Node next;

		final Runnable task;
	}

	private static class Lock extends Node
	{
		/** 0: running, 1: rejecting, 2: exited, 4: closed */
		int closed;

		volatile RejectedExecutionException rejected = null;

		private Lock()
		{
			super(null, null);
		}
	}

	private final CloseableExecutor executor;

	/** List of pending tasks in opposite order.  null means the executor is not running now. */
	private volatile Node pending = null;

	private final Lock lock = new Lock();

	private static final AtomicReferenceFieldUpdater<SequencingExecutor, Node> PENDING_UPDATER =
		AtomicReferenceFieldUpdater.newUpdater(SequencingExecutor.class, Node.class, "pending");
}
