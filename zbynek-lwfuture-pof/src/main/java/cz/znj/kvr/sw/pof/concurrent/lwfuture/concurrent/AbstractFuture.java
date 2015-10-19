package cz.znj.kvr.sw.pof.concurrent.lwfuture.concurrent;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Base implementation of Future.
 *
 * @param <V>
 *      future result type
 */
public class AbstractFuture<V> implements ListenableFuture<V>
{
	/**
	 * Initializes instance of AbstractFuture with state ST_RUNNING.
	 */
	protected                       AbstractFuture()
	{
		this(true);
	}

	/**
	 * Initializes instance of AbstractFuture with provided state.
	 *
	 * @param isRunning
	 *      sets state to ST_RUNNING if isRunning is true
	 */
	protected                       AbstractFuture(boolean isRunning)
	{
		this(isRunning ? ST_RUNNING : 0);
	}

	/**
	 * Initializes instance of AbstractFuture with provided state.
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
		int old;
		if ((old = updateStatusFinal(ST_CANCELLED)) < ST_FINISHED) {
			if (b && (old&ST_RUNNING) != 0)
				interruptTask();
			if ((old&ST_DELAYED_CANCEL) == 0)
				processListenersCancelled();
			return true;
		}
		return false;
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
	public V			get(long l, TimeUnit timeUnit) throws InterruptedException, ExecutionException, TimeoutException
	{
		int localStatus = getStatus();
		if (localStatus < ST_FINISHED) {
			// this would hardly ever happen as we expect any get would be run by listener
			synchronized (this) {
				for (;;) {
					localStatus = getStatus();
					if (localStatus >= ST_FINISHED)
						break;
					if (casStatus(localStatus, localStatus|ST_WAITING)) {
						this.wait(timeUnit.toMillis(l));
						if (localStatus < ST_FINISHED)
							throw new TimeoutException();
					}
				}
			}
		}
		switch (localStatus) {
		case ST_CANCELLED:
			throw new CancellationException();

		case ST_FINISHED:
			if (excepted != null)
				throw new ExecutionException(excepted);
			return result;

		default:
			throw new Error("Unknown final status: "+localStatus);
		}
	}

	@Override
	public ListenableFuture<V>      setDelayedCancel()
	{
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
	public ListenableFuture<V>	addListener(final Runnable listener)
	{
		addListenerNode(new RegularListenerNode() {
			@Override
			public String toString() { return listener.toString(); }

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
	public ListenableFuture<V>	addListener(final Function<Future<V>, Void> listener)
	{
		addListenerNode(new RegularListenerNode() {
			@Override
			public String toString() { return listener.toString(); }

			@Override
			public void executeSet() {
				listener.apply(AbstractFuture.this);
			}

			@Override
			public void executeExcepted() {
				listener.apply(AbstractFuture.this);
			}

			@Override
			public void executeCancelled() {
				listener.apply(AbstractFuture.this);
			}
		});
		return this;
	}

	@Override
	public ListenableFuture<V>      addListener(final FutureListener<V> listener)
	{
		addListenerNode(new RegularListenerNode() {
			@Override
			public String toString() { return listener.toString(); }

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

	/**
	 * Customizable method to interrupt running task.
	 *
	 * The method is called only in case the task was running and after the state was successfully set to CANCELLED.
	 */
	protected void                  interruptTask()
	{
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
		this.excepted = ex;
		if (updateStatusFinished(ST_FINISHED) < ST_FINISHED) {
			processListenersExcepted();
			return true;
		}
		return false;
	}

	/**
	 * Sets this future to running state.
	 *
	 * @return true
	 *      if the task was not yet cancelled nor finished
	 * @return false
	 *      if the task was already cancelled or finished
	 */
	protected final boolean         setRunning()
	{
		int localStatus = 0;
		for (;;) {
			if (casStatus(localStatus, localStatus|ST_RUNNING))
				return true;
			localStatus = getStatusLazy();
			if (localStatus >= ST_FINISHED)
				return false;
		}
	}

	/**
	 * Sets the status to some finished (successful or exception) state.
	 *
	 * Runs cancelled notification if this future was already cancelled and delayed cancel notification was configured.
	 *
	 * @param finalStatus
	 *      the new status value (state part)
	 *
	 * @return
	 *      old status value
	 */
	private final int               updateStatusFinished(int finalStatus)
	{
		int old = updateStatusFinal(finalStatus);
		if ((old&(ST_CANCELLED|ST_DELAYED_CANCEL)) != 0)
			processListenersCancelled();
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
		int localStatus = ST_RUNNING;
		for (;;) {
			int newStatus = localStatus&~(ST_RUNNING|ST_WAITING)|finalStatus;
			if (casStatus(localStatus, newStatus))
				break;
			localStatus = newStatus;
			if (localStatus >= ST_FINISHED)
				return localStatus;
		}
		if ((localStatus&ST_WAITING) != 0) {
			synchronized (this) {
				notifyAll();
			}
		}
		return localStatus;
	}

	/**
	 * Adds single listener node.
	 *
	 * The listener is immediately executed if the listeners were already executed.
	 *
	 * @param listenerNode
	 *      listener to be added
	 */
	private final void              addListenerNode(ListenerNode listenerNode)
	{
		if (casListeners(null, listenerNode))
			return;
		for (;;) {
			ListenerNode localListeners = getListenersLazy();
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
			if (excepted != null)
				listener.executeExcepted();
			else
				listener.executeSet();
			break;

		case ST_CANCELLED:
		case ST_CANCELLED|ST_FINISHED:
			listener.executeCancelled();
			break;

		default:
			throw new IllegalStateException("Unexpected status when running late listener: "+getStatusLazy());
		}
	}

	/**
	 * Processes listeners by notifying about successful completion.
	 */
	private final void              processListenersSet()
	{
		ListenerNode boundaryListener = null;
		for (;;) {
			ListenerNode lastListener = getListeners();
			for (ListenerNode current = ListenerNode.reverseListenersQueue(lastListener, boundaryListener); current != boundaryListener; current = current.nextNode) {
				try {
					current.executeSet();
				}
				catch (Exception ex) {
					// ignore exceptions from listeners
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
	private final void              processListenersExcepted()
	{
		ListenerNode boundaryListener = null;
		for (;;) {
			ListenerNode lastListener = getListeners();
			for (ListenerNode current = ListenerNode.reverseListenersQueue(lastListener, boundaryListener); current != boundaryListener; current = current.nextNode) {
				try {
					current.executeExcepted();
				}
				catch (Exception ex) {
					// ignore exceptions from listeners
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
	private final void              processListenersCancelled()
	{
		ListenerNode boundaryListener = null;
		for (;;) {
			ListenerNode lastListener = getListeners();
			for (ListenerNode current = ListenerNode.reverseListenersQueue(lastListener, boundaryListener); current != boundaryListener; current = current.nextNode) {
				try {
					current.executeCancelled();
				}
				catch (RuntimeException ex) {
					logger.log(Level.SEVERE, "RuntimeException raised by FutureListener "+current, ex);
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
		public static <V> ListenerNode<V> reverseListenersQueue(ListenerNode last, ListenerNode boundary)
		{
			if (last == null) {
				return last;
			}
			else if (last.getNextNode() == boundary) {
				return last;
			}
			ListenerNode next = last.getNextNode();
			last.nextNode = boundary;
			for (ListenerNode current = next; current != boundary; current = next) {
				next = current.getNextNode();
				current.nextNode = last;
				last = current;
			}
			return last;
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
		public final ListenerNode	getNextNode()
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

		private ListenerNode		nextNode;
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
			throw new UnsupportedOperationException("executeSet called on MarkerListenerNode.");
		}

		@Override
		public void                     executeExcepted()
		{
			throw new UnsupportedOperationException("executeExcepted called on MarkerListenerNode.");
		}

		@Override
		public void                     executeCancelled()
		{
			throw new UnsupportedOperationException("executeCancelled called on MarkerListenerNode.");
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
	/** Waiting flag, indicating there are threads blocked in get() */
	private static final int        ST_WAITING                      = 2;
	/** Running state */
	private static final int        ST_RUNNING                      = 4;
	/** Finished state (task really finished) */
	private static final int        ST_FINISHED                     = 8;
	/** Cancelled requested */
	private static final int        ST_CANCELLED                    = 16;

	/** Marks closed listener queue */
	private static final ListenerNode LN_MARKER_CLOSED = new MarkerListenerNode(ListenerNode.NT_MARKER_CLOSED);

	private static final AtomicIntegerFieldUpdater<AbstractFuture> statusUpdater = AtomicIntegerFieldUpdater.newUpdater(AbstractFuture.class, "status");
	private static final AtomicReferenceFieldUpdater<AbstractFuture, ListenerNode> listenersUpdater = AtomicReferenceFieldUpdater.newUpdater(AbstractFuture.class, ListenerNode.class, "listeners");

	private static final Logger     logger = Logger.getLogger(AbstractFuture.class.getName());
}
