/*
 * Copyright 2015 Zbynek Vyskovsky mailto:kvr@centrum.cz http://kvr.znj.cz/ http://github.com/kvr000/
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

package cz.znj.kvr.sw.pof.concurrent.lwfuture.concurrent;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Base implementation of Future.
 *
 * @param <V>
 *      future result type
 *
 * @author
 * 	Zbynek Vyskovsky, mailto:kvr@centrum.cz http://kvr.znj.cz/software/java/ListenableFuture/ http://github.com/kvr000
 */
public class AbstractFuture<V> implements ListenableFuture<V>
{
	/**
	 * Initializes instance of {@link AbstractFuture} with state {@code RUNNING}.
	 */
	protected                       AbstractFuture()
	{
		this(true);
	}

	/**
	 * Initializes instance of {@link AbstractFuture} with provided state.
	 *
	 * @param isRunning
	 *      sets state to {@code RUNNING} if isRunning is true
	 */
	protected                       AbstractFuture(boolean isRunning)
	{
		this(isRunning ? ST_RUNNING : 0);
	}

	/**
	 * Initializes instance of {@link AbstractFuture} with provided state.
	 *
	 * @param initialStatus
	 *      state to set as initial
	 */
	protected                       AbstractFuture(int initialStatus)
	{
		setStatusLazy(initialStatus);
	}

	@Override
	public boolean			cancel(boolean b)
	{
		// optimize for the most common case when we update to final from RUNNING state
		int oldStatus = ST_RUNNING;
		for (;;) {
			int newStatus = oldStatus|ST_CANCELLED;
			if (casStatus(oldStatus, newStatus))
				break;
			oldStatus = getStatusLazy();
			if ((oldStatus&(ST_UNCANCELLABLE|ST_COMPLETING|ST_FINISHED|ST_CANCELLED)) != 0)
				return (oldStatus&ST_CANCELLED) != 0;
		}
		if (b && (oldStatus&ST_RUNNING) != 0) {
			// we need to synchronize with executor thread here so we dont kill it while its already processing something else
			synchronized (this) {
				if ((getStatus()&ST_RUNNING) != 0)
					interruptTask();
			}
		}
		if ((oldStatus&ST_WAITING) != 0) {
			synchronized (this) {
				notifyAll();
			}
		}
		switch (oldStatus&(ST_RUNNING|ST_DELAYED_CANCEL)) {
		case 0:
		case ST_RUNNING:
		case ST_DELAYED_CANCEL:
			processListenersCancelled();
			break;
		}
		return true;
	}

	@Override
	public boolean			isCancelled()
	{
		return (getStatusLazy()&ST_CANCELLED) != 0;
	}

	@Override
	public boolean			isDone()
	{
		return getStatusLazy() >= ST_FINISHED;
	}

	@Override
	public V			get() throws InterruptedException, ExecutionException
	{
		for (;;) {
			try {
				return get(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
			}
			catch (TimeoutException e) {
			}
		}
	}

	@Override
	public V			get(long l, @SuppressWarnings("NullableProblems") TimeUnit timeUnit) throws InterruptedException, ExecutionException, TimeoutException
	{
		int oldStatus = getStatus();
		if (oldStatus < ST_FINISHED) {
			// this would hardly ever happen as we expect any get would be run by listener
			synchronized (this) {
				for (;;) {
					oldStatus = getStatus();
					if (oldStatus >= ST_FINISHED)
						break;
					if (casStatus(oldStatus, oldStatus|ST_WAITING)) {
						long nanos = timeUnit.toNanos(l);
						if (nanos == Long.MAX_VALUE)
							this.wait(timeUnit.toMillis(l));
						else
							this.wait(nanos/1000000, (int)(nanos%1000000));
						oldStatus = getStatus();
						if (oldStatus < ST_FINISHED)
							throw new TimeoutException(String.valueOf(l));
					}
				}
			}
		}
		switch (oldStatus&(ST_CANCELLED|ST_FINISHED)) {
		case ST_CANCELLED:
		case ST_CANCELLED|ST_FINISHED:
			throw new CancellationException();

		case ST_FINISHED:
			if (excepted != null)
				throw new ExecutionException(excepted);
			return result;

		default:
			assert false : "Unexpected final status: "+oldStatus;
			throw new AssertionError("Unexpected final status: "+oldStatus);
		}
	}

	@Override
	public ListenableFuture<V>      setDelayedCancel()
	{
		// optimize for the most common case when we set delayed cancel right after the future was created
		int localStatus = 0;
		for (;;) {
			if (casStatus(localStatus, localStatus|ST_DELAYED_CANCEL))
				return this;
			localStatus = getStatusLazy();
			if ((localStatus&ST_CANCELLED) != 0)
				throw new IllegalStateException("Future delayed cannot be changed once the future was cancelled.");
		}
	}

	@Override
	public ListenableFuture<V>      setUncancellable()
	{
		// optimize for the most common case when we set delayed cancel right after the future was created
		int localStatus = 0;
		for (;;) {
			if (casStatus(localStatus, localStatus|ST_UNCANCELLABLE))
				return this;
			localStatus = getStatusLazy();
			if ((localStatus&ST_CANCELLED) != 0)
				throw new IllegalStateException("Future uncancellable cannot be changed once the future was cancelled.");
		}
	}

	@Override
	public ListenableFuture<V>	addListener(final Runnable listener)
	{
		addListenerNode(new RegularListenerNode<V>() {
			@Override
			public String toString() {
				return listener.toString();
			}

			@Override
			public void executeSet() {
				listener.run();
			}

			@Override
			public void executeExcepted() {
				listener.run();
			}

			@Override
			public void executeCancelled() {
				listener.run();
			}
		});
		return this;
	}

	@Override
	public <FT extends Future<V>> ListenableFuture<V> addListener(final FutureNotifier<FT> listener)
	{
		addListenerNode(new RegularListenerNode<V>() {
			@Override
			public String toString() {
				return listener.toString();
			}

			@Override
			@SuppressWarnings("unchecked")
			public void executeSet() {
				listener.notify((FT) AbstractFuture.this);
			}

			@Override
			@SuppressWarnings("unchecked")
			public void executeExcepted() {
				listener.notify((FT) AbstractFuture.this);
			}

			@Override
			@SuppressWarnings("unchecked")
			public void executeCancelled() {
				listener.notify((FT) AbstractFuture.this);
			}
		});
		return this;
	}

	@Override
	public ListenableFuture<V>      addListener(final FutureListener<V> listener)
	{
		addListenerNode(new RegularListenerNode<V>() {
			@Override
			public String toString() {
				return listener.toString();
			}

			@Override
			public void executeSet() {
				listener.onSuccess(result);
			}

			@Override
			public void executeExcepted() {
				listener.onFailure(excepted);
			}

			@Override
			public void executeCancelled() {
				listener.onCancelled();
			}
		});
		return this;
	}

	@Override
	public ListenableFuture<V>      addListener(final SuccessListener<V> successListener, final FailureListener failureListener, final CancelListener cancelListener)
	{
		addListenerNode(new RegularListenerNode<V>() {
			@Override
			public String toStringSet() {
				return successListener.toString();
			}

			@Override
			public String toStringExcepted() {
				return failureListener.toString();
			}

			@Override
			public String toStringCancelled() {
				return cancelListener.toString();
			}

			@Override
			public void executeSet() {
				if (successListener != null)
					successListener.onSuccess(result);
			}

			@Override
			public void executeExcepted() {
				if (failureListener != null)
					failureListener.onFailure(excepted);
			}

			@Override
			public void executeCancelled() {
				if (cancelListener != null)
					cancelListener.onCancelled();
			}
		});
		return this;
	}

	@Override
	public ListenableFuture<V>	addAsyncListener(final Runnable listener, final Executor executor)
	{
		addListenerNode(new RegularListenerNode<V>() {
			@Override
			public String toString() {
				return listener.toString();
			}

			@Override
			public void executeSet() {
				executor.execute(listener);
			}

			@Override
			public void executeExcepted() {
				executor.execute(listener);
			}

			@Override
			public void executeCancelled() {
				executor.execute(listener);
			}
		});
		return this;
	}

	@Override
	public <FT extends Future<V>> ListenableFuture<V> addAsyncListener(final FutureNotifier<FT> listener, final Executor executor)
	{
		addListenerNode(new RegularListenerNode<V>() {
			@Override
			public String toString() {
				return listener.toString();
			}

			@Override
			@SuppressWarnings("unchecked")
			public void executeSet() {
				executor.execute(new Runnable() {
					@Override
					public void run() {
						listener.notify((FT) AbstractFuture.this);
					}
				});
			}

			@Override
			@SuppressWarnings("unchecked")
			public void executeExcepted() {
				executor.execute(new Runnable() {
					@Override
					public void run() {
						listener.notify((FT) AbstractFuture.this);
					}
				});
			}

			@Override
			@SuppressWarnings("unchecked")
			public void executeCancelled() {
				executor.execute(new Runnable() {
					@Override
					public void run() {
						listener.notify((FT) AbstractFuture.this);
					}
				});
			}
		});
		return this;
	}

	@Override
	public ListenableFuture<V>      addAsyncListener(final FutureListener<V> listener, final Executor executor)
	{
		addListenerNode(new RegularListenerNode<V>() {
			@Override
			public String toString() {
				return listener.toString();
			}

			@Override
			public void executeSet() {
				executor.execute(new Runnable() {
					@Override
					public void run() {
						listener.onSuccess(result);
					}
				});
			}

			@Override
			public void executeExcepted() {
				executor.execute(new Runnable() {
					@Override
					public void run() {
						listener.onFailure(excepted);
					}
				});
			}

			@Override
			public void executeCancelled() {
				executor.execute(new Runnable() {
					@Override
					public void run() {
						listener.onCancelled();
					}
				});
			}
		});
		return this;
	}

	@Override
	public ListenableFuture<V>      addAsyncListener(final SuccessListener<V> successListener, final FailureListener failureListener, final CancelListener cancelListener, final Executor executor)
	{
		addListenerNode(new RegularListenerNode<V>() {
			@Override
			public String toStringSet() {
				return successListener.toString();
			}

			@Override
			public String toStringExcepted() {
				return failureListener.toString();
			}

			@Override
			public String toStringCancelled() {
				return cancelListener.toString();
			}

			@Override
			public void executeSet() {
				if (successListener != null) {
					executor.execute(new Runnable() {
						@Override
						public void run() {
							successListener.onSuccess(result);
						}
					});
				}
			}

			@Override
			public void executeExcepted() {
				if (failureListener != null) {
					executor.execute(new Runnable() {
						@Override
						public void run() {
							failureListener.onFailure(excepted);
						}
					});
				}
			}

			@Override
			public void executeCancelled() {
				if (cancelListener != null) {
					executor.execute(new Runnable() {
						@Override
						public void run() {
							cancelListener.onCancelled();
						}
					});
				}
			}
		});
		return this;
	}

	/**
	 * Customizable method to interrupt running task.
	 *
	 * The method is called only in case the task was running and after the state was
	 * successfully set to {@code CANCELLED}.
	 *
	 * By default this method does nothing.
	 */
	protected void			interruptTask()
	{
	}

	/**
	 * Sets this future to running state.
	 *
	 * This method serves as helper for tasks that run repeatedly.
	 *
	 * @return true
	 *      if the task was not yet cancelled nor finished
	 * @return false
	 *      if the task was already cancelled or finished
	 */
	protected boolean         	setRunning()
	{
		// optimize for the most common case when we update to RUNNING from initial state
		int localStatus = 0;
		for (;;) {
			if (casStatus(localStatus, localStatus|ST_RUNNING))
				return true;
			localStatus = getStatusLazy();
			if (localStatus >= ST_COMPLETING)
				return false;
		}
	}

	/**
	 * Resets this future to original state.
	 *
	 * @return true
	 *      if the reset was successful
	 * @return false
	 *      if the task was already cancelled or finished
	 */
	protected boolean		setRestart()
	{
		int oldStatus = ST_RUNNING;
		for (;;) {
			if (casStatus(oldStatus, oldStatus&~ST_RUNNING))
				return true;
			oldStatus = getStatusLazy();
			if (oldStatus >= ST_COMPLETING)
				break;
		}
		for (;;) {
			if ((oldStatus&(ST_COMPLETING|ST_FINISHED)) != 0)
				return false;
			if (casStatus(oldStatus, (oldStatus&~ST_RUNNING)|ST_FINISHED))
				break;
			oldStatus = getStatusLazy();
		}
		if ((oldStatus&ST_CANCELLED) != 0) {
			synchronized (this) {
				// dummy lock so we stay within the thread until interruptTask() finishes
			}
			if ((oldStatus&(ST_CANCELLED|ST_DELAYED_CANCEL)) == (ST_CANCELLED|ST_DELAYED_CANCEL))
				processListenersCancelled();
		}
		return false;
	}

	/**
	 * Sets the result of this future.
	 *
	 * @param result
	 *      result of this future
	 *
	 * @return true
	 *      if the task was not yet cancelled
	 * @return false
	 *      if the task was already cancelled
	 */
	protected boolean               set(V result)
	{
		if (!updateStatusCompleting())
			return false;
		this.result = result;
		if (updateStatusFinished(ST_FINISHED) < ST_FINISHED) {
			processListenersSet();
			return true;
		}
		return false;
	}

	/**
	 * Sets the result of this future.
	 *
	 * @param ex
	 *      exception that caused this future failure
	 *
	 * @return true
	 *      if the task was not yet cancelled
	 * @return false
	 *      if the task was already cancelled
	 */
	protected boolean               setException(Throwable ex)
	{
		if (ex == null)
			throw new NullPointerException("exception cannot be null");
		if (!updateStatusCompleting())
			return false;
		this.excepted = ex;
		if (updateStatusFinished(ST_FINISHED) < ST_FINISHED) {
			processListenersExcepted();
			return true;
		}
		return false;
	}

	/**
	 * Sets this future to {@code CANCELLED} and finishes it at the same time.
	 *
	 * Method ignores any special settings like {@code UNCANCELLABLE}. This method is provided to executors run method to handle the lifecycle of future.
	 *
	 * @return true
	 *      if the task was not yet cancelled
	 * @return false
	 *      if the task was already cancelled
	 */
	protected boolean               setCancelled()
	{
		int old;
		if ((old = (updateStatusFinished(ST_CANCELLED|ST_FINISHED))&ST_FINISHED) == 0) {
			if ((old&ST_CANCELLED) == 0 || (old&ST_DELAYED_CANCEL) != 0)
				processListenersCancelled();
			return true;
		}
		return false;
	}

	/**
	 * Sets the status to completing state.
	 *
	 * Runs {@code CANCELLED} notification if this future was already cancelled and delayed
	 * cancel notification was configured.
	 *
	 * @param finalStatus
	 * 	the new status value (state part)
	 *
	 * @return
	 *      old status value
	 */
	private final boolean           updateStatusCompleting()
	{
		// optimize for the most common case when we update to final from RUNNING state
		int oldStatus = ST_RUNNING;
		for (;;) {
			int newStatus = oldStatus|ST_COMPLETING;
			if (casStatus(oldStatus, newStatus))
				return true;
			oldStatus = getStatusLazy();
			if ((oldStatus&(ST_COMPLETING|ST_FINISHED)) != 0)
				return false;
		}
	}

	/**
	 * Sets the status to some finished (successful or exception) state.
	 *
	 * Runs {@code CANCELLED} notification if this future was already cancelled and delayed
	 * cancel notification was configured.
	 *
	 * @param finalStatus
	 * 	the new status value (state part)
	 *
	 * @return
	 *      old status value
	 */
	private final int               updateStatusFinished(int finalStatus)
	{
		int old = updateStatusFinal(finalStatus);
		if ((old&ST_CANCELLED) != 0) {
			synchronized (this) {
				// dummy lock so we stay within the thread until interruptTask() finishes
			}
			if ((old&(ST_CANCELLED|ST_DELAYED_CANCEL)) == (ST_CANCELLED|ST_DELAYED_CANCEL))
				processListenersCancelled();
		}
		return old;
	}

	/**
	 * Sets the status to final state (successful, exception, cancelled) state.
	 *
	 * @param finalStatus
	 *      the new status value (state part)
	 *
	 * @return
	 *      old status value
	 */
	private final int               updateStatusFinal(int finalStatus)
	{
		// optimize for the most common case when we update to final from RUNNING state
		int oldStatus = ST_RUNNING|ST_COMPLETING;
		for (;;) {
			int newStatus = oldStatus&~(ST_RUNNING|ST_WAITING|ST_COMPLETING)|finalStatus;
			if (casStatus(oldStatus, newStatus))
				break;
			oldStatus = getStatusLazy();
			if (oldStatus >= ST_FINISHED)
				return oldStatus;
		}
		if ((oldStatus&ST_WAITING) != 0) {
			synchronized (this) {
				notifyAll();
			}
		}
		return oldStatus;
	}

	/**
	 * Adds single listener node.
	 *
	 * The listener is immediately executed if the listeners were already executed.
	 *
	 * @param listenerNode
	 *      listener to be added
	 */
	private final void              addListenerNode(ListenerNode<V> listenerNode)
	{
		// optimize for the most common case when there is only single listener registered for the future
		if (casListeners(null, listenerNode))
			return;
		for (;;) {
			ListenerNode<V> localListeners = getListenersLazy();
			if (localListeners != null && localListeners.getNodeType() != ListenerNode.NT_REGULAR) {
				executeLateListener(listenerNode);
				return;
			}
			listenerNode.nextNode = localListeners;
			if (casListeners(localListeners, listenerNode))
				return;
		}
	}

	/**
	 * Executes lately coming listener.
	 *
	 * @param listener
	 *      listener to be executed
	 */
	private final void              executeLateListener(ListenerNode listener)
	{
		switch (getStatusLazy()&(ST_FINISHED|ST_CANCELLED)) {
		case ST_FINISHED:
			if (excepted != null) {
				try {
					listener.executeExcepted();
				}
				catch (RuntimeException ex) {
					logger.log(Level.SEVERE, "RuntimeException raised by FutureListener.onSuccess() "+listener.toStringExcepted(), ex);
				}
			}
			else {
				try {
					listener.executeSet();
				}
				catch (RuntimeException ex) {
					logger.log(Level.SEVERE, "RuntimeException raised by FutureListener.onFailure() "+listener.toStringSet(), ex);
				}
			}
			break;

		case ST_CANCELLED:
		case ST_CANCELLED|ST_FINISHED:
			try {
				listener.executeCancelled();
			}
			catch (RuntimeException ex) {
				logger.log(Level.SEVERE, "RuntimeException raised by FutureListener.onCancelled() "+listener.toStringCancelled(), ex);
			}
			break;

		default:
			assert false : "Unexpected final status: "+getStatusLazy();
			throw new AssertionError("Unexpected final status: "+getStatusLazy());
		}
	}

	/**
	 * Processes listeners by notifying about successful completion.
	 */
	@SuppressWarnings("unchecked")
	private final void              processListenersSet()
	{
		ListenerNode<V> boundaryListener = null;
		for (;;) {
			ListenerNode<V> lastListener = getListeners();
			for (ListenerNode<V> current = ListenerNode.reverseListenersQueue(lastListener, boundaryListener); current != boundaryListener; current = current.nextNode) {
				try {
					current.executeSet();
				}
				catch (RuntimeException ex) {
					logger.log(Level.SEVERE, "RuntimeException raised by FutureListener.onSuccess() "+current.toStringSet(), ex);
				}
			}
			if (casListeners(lastListener, LN_MARKER_CLOSED))
				return;
			boundaryListener = lastListener;
		}
	}

	/**
	 * Processes listeners by notifying about exception.
	 */
	@SuppressWarnings("unchecked")
	private final void              processListenersExcepted()
	{
		ListenerNode<V> boundaryListener = null;
		for (;;) {
			ListenerNode<V> lastListener = getListeners();
			for (ListenerNode<V> current = ListenerNode.reverseListenersQueue(lastListener, boundaryListener); current != boundaryListener; current = current.nextNode) {
				try {
					current.executeExcepted();
				}
				catch (RuntimeException ex) {
					logger.log(Level.SEVERE, "RuntimeException raised by FutureListener.onExcepted() "+current.toStringExcepted(), ex);
				}
			}
			if (casListeners(lastListener, LN_MARKER_CLOSED))
				return;
			boundaryListener = lastListener;
		}
	}

	/**
	 * Processes listeners by notifying about cancellation
	 */
	@SuppressWarnings("unchecked")
	private final void              processListenersCancelled()
	{
		ListenerNode<V> boundaryListener = null;
		for (;;) {
			ListenerNode<V> lastListener = getListeners();
			for (ListenerNode<V> current = ListenerNode.reverseListenersQueue(lastListener, boundaryListener); current != boundaryListener; current = current.nextNode) {
				try {
					current.executeCancelled();
				}
				catch (RuntimeException ex) {
					logger.log(Level.SEVERE, "RuntimeException raised by FutureListener.onCancelled() "+current.toStringCancelled(), ex);
				}
			}
			if (casListeners(lastListener, LN_MARKER_CLOSED))
				return;
			boundaryListener = lastListener;
		}
	}

	private final int               getStatus()
	{
		return this.status;
	}

	private final int               getStatusLazy()
	{
		return this.status;
	}

	private final void              setStatus(int status)
	{
		this.status = status;
	}

	private final void              setStatusLazy(int status)
	{
		this.status = status;
	}

	private final boolean           casStatus(int expected, int set)
	{
		return statusUpdater.compareAndSet(this, expected, set);
	}

	private final ListenerNode<V>   getListeners()
	{
		return this.listeners;
	}

	private final ListenerNode<V>   getListenersLazy()
	{
		return this.listeners;
	}

	private final void              setListeners(ListenerNode<V> listeners)
	{
		this.listeners = listeners;
	}

	private final void              setListenersLazy(ListenerNode<V> listeners)
	{
		this.listeners = listeners;
	}

	private final boolean           casListeners(ListenerNode<V> expected, ListenerNode<V> set)
	{
		return listenersUpdater.compareAndSet(this, expected, set);
	}

	@SuppressWarnings("unchecked")
	private final ListenerNode<V>   xchgListeners(ListenerNode<V> set)
	{
		return listenersUpdater.getAndSet(this, set);
	}

	/**
	 * Basic Listener node providing linking functions.
	 */
	protected static abstract class ListenerNode<V>
	{
		/** Regular listener */
		public static final int         NT_REGULAR                      = 0;
		/** Queue processing finished and closed */
		public static final int         NT_MARKER_CLOSED                = 1;

		/**
		 * Constructs new instance with specified node type.
		 *
		 * @param nodeType
		 *      node type
		 */
		protected                       ListenerNode(int nodeType)
		{
			this.nodeType = nodeType;
		}

		/**
		 * Reverses listeners queue.
		 *
		 * @param last
		 *      lastly registered listener
		 *
		 * @return
		 *      reversed list
		 */
		public static <V> ListenerNode<V> reverseListenersQueue(ListenerNode<V> last, ListenerNode<V> boundary)
		{
			if (last == null) {
				return last;
			}
			else if (last.getNextNode() == boundary) {
				return last;
			}
			ListenerNode<V> next = last.getNextNode();
			last.nextNode = boundary;
			for (ListenerNode<V> current = next; current != boundary; current = next) {
				next = current.getNextNode();
				current.nextNode = last;
				last = current;
			}
			return last;
		}

		/**
		 * Describes set listener.
		 *
		 * @return
		 *      description of set listener
		 */
		public String                   toStringSet()
		{
			return toString();
		}

		/**
		 * Describes excepted listener.
		 *
		 * @return
		 *      description of excepted listener
		 */
		public String                   toStringExcepted()
		{
			return toString();
		}

		/**
		 * Describes cancelled listener.
		 *
		 * @return
		 *      description of cancelled listener
		 */
		public String                   toStringCancelled()
		{
			return toString();
		}

		/**
		 * Executes set notification.
		 */
		public abstract void		executeSet();

		/**
		 * Executes failure notification.
		 */
		public abstract void		executeExcepted();

		/**
		 * Executes cancelled notification.
		 */
		public abstract void		executeCancelled();

		/**
		 * Gets next node.
		 *
		 * @return
		 *      next node
		 */
		public final ListenerNode<V>	getNextNode()
		{
			return nextNode;
		}

		/**
		 * Gets node type.
		 *
		 * @return
		 *      node type
		 */
		public final int                getNodeType()
		{
			return nodeType;
		}

		private final int               nodeType;

		private ListenerNode<V>		nextNode;
	}

	/**
	 * Listener node providing abstraction over various kind of listeners.
	 */
	protected static abstract class RegularListenerNode<V> extends ListenerNode<V>
	{
		/**
		 * Construct new instance with node type regular.
		 */
		public                          RegularListenerNode()
		{
			super(NT_REGULAR);
		}
	}

	/**
	 * Marker listener node, marking states of processing the listeners queue.
	 * @param <V>
	 */
	protected static class MarkerListenerNode<V> extends ListenerNode<V>
	{
		/**
		 * Constructs new instance with specified node type.
		 *
		 * @param nodeType
		 *      node type
		 */
		public                          MarkerListenerNode(int nodeType)
		{
			super(nodeType);
		}

		@Override
		public void                     executeSet()
		{
			assert false;
		}

		@Override
		public void                     executeExcepted()
		{
			assert false;
		}

		@Override
		public void                     executeCancelled()
		{
			assert false;
		}
	}

	/**
	 * Result of this future. Does not need to be volatile as reads/writes are surrounded by other memory barriers.
	 */
	private V                       result = null;

	/**
	 * Failure cause if not null. Does not need to be volatile as reads/writes are surrounded by other memory barriers.
	 */
	private Throwable               excepted = null;

	/**
	 * Status of this future, may be ORs of ST_* constants.
	 */
	private volatile int		status;

	/**
	 * Listeners queue.
	 */
	private volatile ListenerNode<V> listeners = null;

	/** Delayed cancel notifications flag */
	private static final int        ST_DELAYED_CANCEL               = 1;
	/** Future is uncancellable */
	private static final int        ST_UNCANCELLABLE                = 2;
	/** Waiting flag, indicating there are threads blocked in get() */
	private static final int        ST_WAITING                      = 4;
	/** Running state */
	private static final int        ST_RUNNING                      = 8;
	/** Completing state (just temporary before updating result) */
	private static final int        ST_COMPLETING                   = 16;
	/** Finished state (task really finished) */
	private static final int        ST_FINISHED                     = 32;
	/** Cancelled requested */
	private static final int        ST_CANCELLED                    = 64;

	/** Marks closed listener queue */
	private static final ListenerNode LN_MARKER_CLOSED = new MarkerListenerNode(ListenerNode.NT_MARKER_CLOSED);

	private static final AtomicIntegerFieldUpdater<AbstractFuture> statusUpdater = AtomicIntegerFieldUpdater.newUpdater(AbstractFuture.class, "status");
	private static final AtomicReferenceFieldUpdater<AbstractFuture, ListenerNode> listenersUpdater = AtomicReferenceFieldUpdater.newUpdater(AbstractFuture.class, ListenerNode.class, "listeners");

	private static final Logger     logger = Logger.getLogger(AbstractFuture.class.getName());
}
