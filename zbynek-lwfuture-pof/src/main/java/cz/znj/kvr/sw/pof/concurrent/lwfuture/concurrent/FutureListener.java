package cz.znj.kvr.sw.pof.concurrent.lwfuture.concurrent;

import java.util.concurrent.Future;


public interface FutureListener<V>
{
	void                            run(Future<V> future);
}
