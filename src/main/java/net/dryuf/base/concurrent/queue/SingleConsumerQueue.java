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

package net.dryuf.base.concurrent.queue;

import lombok.AllArgsConstructor;
import net.dryuf.base.concurrent.executor.CommonPoolExecutor;

import java.io.Closeable;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;


/**
 * Queue which guarantees reading by single consumer.
 *
 * <pre>{@code
 *
 * 	SingleConsumerQueue<Runnable> queue = new SingleConsumerQueue<>(() -> CompletableFuture.runAsync(() -> this::consumer));
 *
 *      ...
 *      queue.add(this::myTask);
 *      ...
 *
 *      private void consumer()
 *      {
 *              try (SingleConsumerQueue<Runnable>.Consumer consumer = queue.consume()) {
 *             	        for (Runnable task; (task = consumer.next()) != null; ) {
 *             	    		task.run();
 *             	    	}
 *             }
 *      }
 * }</pre>
 *
 * @implNote Implementation is lock-free.
 *
 * @author
 * Copyright 2015-2022 Zbynek Vyskovsky mailto:kvr000@gmail.com http://github.com/kvr000/ https://github.com/dryuf/ https://www.linkedin.com/in/zbynek-vyskovsky/
 */
public class SingleConsumerQueue<T>
{
	@SuppressWarnings("rawtypes")
	private static final AtomicReferenceFieldUpdater<SingleConsumerQueue, Node> STACK_UPDATER =
		AtomicReferenceFieldUpdater.newUpdater(SingleConsumerQueue.class, Node.class, "stack");

	/** Special Node value indicating the consumer holds the lock. */
	private static final Node<?> LOCK = new Node<>(null, null);

	/** Function to be called to initiate the consumer. */
	private final java.util.function.Consumer<SingleConsumerQueue<T>> consumerCallback;

	private final Executor runExecutor;

	/** Last item added or {@link #LOCK} when consumer holds the lock. */
	volatile private Node<T> stack;

	/** Pending items to be processed, already in FIFO order. */
	private Node<T> pending;

	/**
	 * Constructs new {@link SingleConsumerQueue}.
	 *
	 * @param consumerCallback
	 * 	function to call when item is pending and no consumer is running.  Note it is run directly when either
	 * 	adding an item or closing the consumer, therefore it is supposed to schedule consumer asynchronously to
	 * 	avoid recursion.  It can be synchronous only when using {@link Consumer#nextOrClose()}.
	 */
	public SingleConsumerQueue(Runnable consumerCallback)
	{
		this(consumerCallback, CommonPoolExecutor.getInstance());
	}

	/**
	 * Constructs new {@link SingleConsumerQueue}.
	 *
	 * @param consumerCallback
	 * 	function to call when item is pending and no consumer is running.  Note it is run directly when either
	 * 	adding an item or closing the consumer, therefore it is supposed to schedule consumer asynchronously to
	 * 	avoid recursion.  It can be synchronous only when using {@link Consumer#nextOrClose()}.
	 * @param runExecutor
	 * 	the executor to run callback
	 */
	public SingleConsumerQueue(Runnable consumerCallback, Executor runExecutor)
	{
		this((this0) -> consumerCallback.run(), runExecutor);
	}

	/**
	 * Constructs new {@link SingleConsumerQueue}.
	 *
	 * @param consumerCallback
	 * 	function to call when item is pending and no consumer is running.  Note it is run directly when either
	 * 	adding an item or closing the consumer, therefore it is supposed to schedule consumer asynchronously to
	 * 	avoid recursion.  It can be synchronous only when using {@link Consumer#nextOrClose()}.
	 * @param runExecutor
	 * 	the executor to run callback
	 */
	public SingleConsumerQueue(
			java.util.function.Consumer<SingleConsumerQueue<T>> consumerCallback,
			Executor runExecutor
	)
	{
		this.consumerCallback = consumerCallback;
		this.runExecutor = runExecutor;
	}

	/**
	 * Constructs new {@link SingleConsumerQueue}.
	 *
	 * @param consumerCallback
	 * 	function to call when item is pending and no consumer is running.  Note it is run directly when either
	 * 	adding an item or closing the consumer, therefore it is supposed to schedule consumer asynchronously to
	 * 	avoid recursion.  It can be synchronous only when using {@link Consumer#nextOrClose()}.
	 */
	public SingleConsumerQueue(java.util.function.Consumer<SingleConsumerQueue<T>> consumerCallback)
	{
		this(consumerCallback, CommonPoolExecutor.getInstance());
	}

	/**
	 * Consumes the pending item from queue.
	 *
	 * @return
	 * 	queue reader
	 */
	public Consumer consume()
	{
		return new Consumer();
	}

	/**
	 * Adds new item to queue.
	 *
	 * @param item
	 * 	item to be added
	 */
	public void add(T item)
	{
		Objects.requireNonNull(item, "item must not be null");
		for (;;) {
			Node<T> last = stack;
			Node<T> node = new Node<T>(item, last == LOCK ? null : last);
			if (STACK_UPDATER.compareAndSet(this, last, node)) {
				if (last == null) {
					CompletableFuture.runAsync(() -> consumerCallback.accept(this), runExecutor);
				}
				break;
			}
		}
	}

	/**
	 * Queue consumer.
	 */
	public class Consumer implements Closeable
	{
		private boolean closed = false;

		/**
		 * Reads next item from queue.
		 *
		 * @return
		 * 	next item from queue.
		 */
		public T next()
		{
			if (closed) {
				throw new IllegalStateException("Consumer closed");
			}
			if (pending == null) {
				@SuppressWarnings("unchecked")
				Node<T> next = STACK_UPDATER.getAndSet(SingleConsumerQueue.this, LOCK);
				if (next == null)
					return null;
				Node<T> last = null;
				for (;;) {
					Node<T> previous = next.next;
					next.next = last;
					if (previous == null)
						break;
					last = next;
					next = previous;
				}
				if (pending == null) {
					pending = next;
				}
				else {
					Node<T> tail = pending;
					while (tail.next != null)
						tail = tail.next;
					tail.next = next;
				}
			}
			T item = pending.item;
			pending = pending.next;
			return item;
		}

		/**
		 * Returns next item or closes the consumer, so new consumer can start running.
		 *
		 * @return
		 * 	next item or null of consumer was closed.
		 */
		public T nextOrClose()
		{
			for (;;) {
				T next = next();
				if (next == null) {
					if (!STACK_UPDATER.compareAndSet(SingleConsumerQueue.this, LOCK, null)) {
						continue;
					}
					closed = true;
				}
				return next;
			}
		}

		/**
		 * {@inheritDoc}
		 *
		 * Closes the reader and unregisters the consumer, so another consumer can start running.
		 */
		@Override
		public void close()
		{
			if (!closed) {
				closed = true;
				if (!STACK_UPDATER.compareAndSet(SingleConsumerQueue.this, LOCK, null)) {
					consumerCallback.accept(SingleConsumerQueue.this);
				}
			}
		}
	}

	/**
	 * Stack node.
	 *
	 * @param <T>
	 *	type of item
	 */
	@AllArgsConstructor
	private static class Node<T>
	{
		private final T item;

		private Node<T> next;
	}
}
