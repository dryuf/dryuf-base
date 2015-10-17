package cz.znj.kvr.sw.pof.concurrent.lwfuture.concurrent;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.function.Function;


public class AbstractFuture<V> implements ListenableFuture<V>
{
	public                          AbstractFuture()
	{
		this(true);
	}

	public                          AbstractFuture(boolean isRunning)
	{
		this(isRunning ? ST_RUNNING : 0);
	}

	public                          AbstractFuture(int initialStatus)
	{
		this.status = initialStatus;
	}

	@Override
	public boolean			cancel(boolean b)
	{
		int old;
		if ((old = updateStatusFinal(ST_CANCELLED)) < ST_FINISHED) {
			if ((old&ST_RUNNING) != 0)
				interruptTask();
			if ((old&ST_DELAY_CANCEL) == 0)
				onCancelled();
			return true;
		}
		return false;
	}

	@Override
	public boolean			isCancelled()
	{
		return (status&ST_CANCELLED) != 0;
	}

	@Override
	public boolean			isDone()
	{
		return status >= ST_FINISHED;
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
		if ((getStatus()&ST_RUNNING) != 0) {
			// this would hardly ever happen as we expect any get would be run by listener
			synchronized (this) {
				for (;;) {
					int localStatus = getStatus();
					if (localStatus >= ST_FINISHED)
						break;
					if (statusUpdater.compareAndSet(this, localStatus, localStatus|ST_WAITING)) {
						this.wait(timeUnit.toMillis(l));
						if (getStatus() < ST_FINISHED)
							throw new TimeoutException();
					}
				}
			}
		}
		switch (getStatus()) {
		case ST_CANCELLED:
			throw new CancellationException();

		case ST_FINISHED:
			if (excepted != null)
				throw new ExecutionException(excepted);
			return result;

		default:
			throw new RuntimeException("Unknown final status: "+getStatus());
		}
	}

	@Override
	public ListenableFuture<V>	addListener(Runnable listener)
	{
		addListenerNode(new RegularListenerNode<V>() { @Override public void run(Future<V> future) { listener.run(); } });
		return this;
	}

	@Override
	public ListenableFuture<V>	addListener(Function<Future<V>, Void> listener)
	{
		addListenerNode(new RegularListenerNode<V>() { @Override public void run(Future<V> future) { listener.apply(AbstractFuture.this); } });
		return this;
	}

	@Override
	public ListenableFuture<V>      addListener(FutureListener<V> listener)
	{
		addListenerNode(new RegularListenerNode<V>() { @Override public void run(Future<V> future) { listener.run(future); } });
		return this;
	}

	protected void                  interruptTask()
	{
	}

	protected void                  set(V result)
	{
		this.result = result;
		if (updateStatusFinished(ST_FINISHED) < ST_FINISHED)
			onCompleted();
	}

	protected void                  setException(Throwable ex)
	{
		this.excepted = ex;
		if (updateStatusFinished(ST_FINISHED) < ST_FINISHED)
			onExcepted();
	}

	protected boolean               setRunning()
	{
		for (;;) {
			int localStatus = status;
			if (localStatus >= ST_FINISHED)
				return false;
			if (statusUpdater.compareAndSet(this, localStatus, localStatus|ST_RUNNING))
				return true;
		}
	}

	private final int               updateStatusFinished(int finalStatus)
	{
		int old = updateStatusFinal(finalStatus);
		if ((old&(ST_CANCELLED|ST_DELAY_CANCEL)) != 0)
			onCancelled();
		return old;
	}

	private final int               updateStatusFinal(int finalStatus)
	{
		int localStatus;
		for (;;) {
			localStatus = status;
			if (localStatus >= ST_FINISHED)
				return localStatus;
			if (statusUpdater.compareAndSet(this, localStatus, localStatus&~(ST_RUNNING|ST_WAITING)|finalStatus))
				break;
		}
		if ((localStatus&ST_WAITING) != 0) {
			synchronized (this) {
				notifyAll();
			}
		}
		return localStatus;
	}

	protected void                  onFinished()
	{
		processListeners();
	}

	protected void                  onCompleted()
	{
		onFinished();
	}

	protected void                  onExcepted()
	{
		onFinished();
	}

	protected void                  onCancelled()
	{
		processListeners();
	}

	protected final void            addListenerNode(ListenerNode listenerNode)
	{
		for (;;) {
			ListenerNode localListeners = listeners;
			switch (localListeners.getNodeType()) {
			case ListenerNode.NT_REGULAR:
				listenerNode.nextNode = localListeners;
				break;

			case ListenerNode.NT_MARKER_EMPTY:
				// leave the next set to null
				break;

			case ListenerNode.NT_MARKER_PROCESSING:
				// leave the next set to null
				break;

			case ListenerNode.NT_MARKER_FINISHED:
				executeListener(listenerNode);
				return;
			}
			if (listenersUpdater.compareAndSet(this, localListeners, listenerNode))
				return;
		}
	}

	protected final int		getStatus()
	{
		return status;
	}

	protected final void            executeListener(ListenerNode<V> listener)
	{
		try {
			listener.run(this);
		}
		catch (Exception ex) {
			// ignore exceptions from listeners
		}
	}

	private final void              swapAndExecuteListenersQueue(ListenerNode<V> last)
	{
		ListenerNode<V> tail = last;
		ListenerNode<V> next = last.getNextNode();
		tail.nextNode = null;
		for (ListenerNode<V> current = next; current != null; current = next) {
			next = current.getNextNode();
			current.nextNode = tail;
			tail = current;
		}
		for (ListenerNode<V> current = tail; current != null; current = current.getNextNode()) {
			executeListener(current);
		}
	}

	protected final void            processListeners()
	{
		for (;;) {
			ListenerNode lastListener = listeners;
			if (!listenersUpdater.compareAndSet(this, lastListener, MARKER_PROCESSING))
				continue;
			if (lastListener.getNodeType() != ListenerNode.NT_MARKER_EMPTY)
				swapAndExecuteListenersQueue(lastListener);
			if (listenersUpdater.compareAndSet(this, MARKER_PROCESSING, MARKER_FINISHED))
				return;
		}
	}

	public static class ListenerNode<V>
	{
		public static final int         NT_REGULAR                      = 0;
		public static final int         NT_MARKER_EMPTY                 = 1;
		public static final int         NT_MARKER_PROCESSING            = 2;
		public static final int         NT_MARKER_FINISHED              = 3;

		public                          ListenerNode(int nodeType)
		{
			this.nodeType = nodeType;
		}

		public void		        run(Future<V> future)
		{
			throw new UnsupportedOperationException("run not overriden");
		}

		public final ListenerNode	getNextNode()
		{
			return nextNode;
		}

		public final int                getNodeType()
		{
			return nodeType;
		}

		private final int               nodeType;

		protected ListenerNode		nextNode;
	}

	public static class RegularListenerNode<V> extends ListenerNode<V>
	{
		public                          RegularListenerNode()
		{
			super(NT_REGULAR);
		}
	}

	private V                       result = null;

	private Throwable               excepted = null;

	private volatile int		status;

	private volatile ListenerNode   listeners = MARKER_EMPTY;

	protected static final int      ST_DELAY_CANCEL                 = 1;
	protected static final int	ST_RUNNING                      = 4;
	protected static final int	ST_WAITING                      = 8;
	protected static final int	ST_FINISHED                     = 16;
	protected static final int	ST_CANCELLED                    = 32;

	protected static final ListenerNode MARKER_EMPTY = new ListenerNode(ListenerNode.NT_MARKER_EMPTY);
	protected static final ListenerNode MARKER_PROCESSING = new ListenerNode(ListenerNode.NT_MARKER_PROCESSING);
	protected static final ListenerNode MARKER_FINISHED = new ListenerNode(ListenerNode.NT_MARKER_FINISHED);

	protected static final AtomicIntegerFieldUpdater<AbstractFuture> statusUpdater = AtomicIntegerFieldUpdater.newUpdater(AbstractFuture.class, "status");
	protected static final AtomicReferenceFieldUpdater<AbstractFuture, ListenerNode> listenersUpdater = AtomicReferenceFieldUpdater.newUpdater(AbstractFuture.class, ListenerNode.class, "listeners");
}