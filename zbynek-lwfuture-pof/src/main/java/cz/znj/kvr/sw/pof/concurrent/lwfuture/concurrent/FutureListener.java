package cz.znj.kvr.sw.pof.concurrent.lwfuture.concurrent;


/**
 * Interface receiving Future completion notifications.
 *
 * @param <V>
 *      Future result type
 */
public interface FutureListener<V>
{
	/**
	 * Method called on successful completion.
	 *
	 * @param result
	 *      result of future
	 */
	void			        onSuccess(V result);

	/**
	 * Method called if future failed due to exception
	 * @param ex
	 *      exception that caused the failure
	 */
	void			        onFailure(Throwable ex);

	/**
	 * Method called if future was cancelled
	 */
	void                            onCancelled();
}
