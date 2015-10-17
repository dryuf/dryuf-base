package cz.znj.kvr.sw.pof.concurrent.lwfuture.concurrent;

import java.util.concurrent.Future;
import java.util.function.Function;


public interface ListenableFuture<V> extends Future<V>
{
	ListenableFuture<V>		addListener(Runnable listener);

	ListenableFuture<V>             addListener(Function<Future<V>, Void> listener);

	ListenableFuture<V>             addListener(FutureListener<V> listener);
}
