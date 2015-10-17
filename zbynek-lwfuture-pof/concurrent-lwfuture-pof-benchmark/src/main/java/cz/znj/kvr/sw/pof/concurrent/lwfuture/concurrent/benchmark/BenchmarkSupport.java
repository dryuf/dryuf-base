package cz.znj.kvr.sw.pof.concurrent.lwfuture.concurrent.benchmark;

import com.google.common.util.concurrent.MoreExecutors;
import cz.znj.kvr.sw.pof.concurrent.lwfuture.concurrent.DistinguishFutureListener;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Warmup;
import org.springframework.util.concurrent.ListenableFutureCallback;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableFuture;
import java.util.function.Function;

public class BenchmarkSupport
{
	public static final int         WARMUP_ITERATIONS = 5;
	public static final int         COUNT = 100000;

	public static FutureTask<Integer>[] populateJdkFutureArray(int count)
	{
		Callable<Integer> func = () -> { return 0; };
		FutureTask<Integer>[] array = new FutureTask[count];
		for (int i = 0; i < count; ++i) {
			array[i] = new FutureTask<Integer>(func);
		}
		return array;
	}

	public static cz.znj.kvr.sw.pof.concurrent.lwfuture.concurrent.ListenableFutureTask<Integer>[] populateLwFutureArray(int count)
	{
		Callable<Integer> func = () -> { return 0; };
		cz.znj.kvr.sw.pof.concurrent.lwfuture.concurrent.ListenableFutureTask[] array = new cz.znj.kvr.sw.pof.concurrent.lwfuture.concurrent.ListenableFutureTask[count];
		for (int i = 0; i < count; ++i) {
			array[i] = new cz.znj.kvr.sw.pof.concurrent.lwfuture.concurrent.ListenableFutureTask<Integer>(func);
		}
		return array;
	}

	public static org.springframework.util.concurrent.ListenableFutureTask<Integer>[] populateSpringFutureArray(int count)
	{
		Callable<Integer> func = () -> { return 0; };
		org.springframework.util.concurrent.ListenableFutureTask[] array = new org.springframework.util.concurrent.ListenableFutureTask[count];
		for (int i = 0; i < count; ++i) {
			array[i] = new org.springframework.util.concurrent.ListenableFutureTask(func);
		}
		return array;
	}

	public static com.google.common.util.concurrent.ListenableFutureTask<Integer>[] populateGuavaFutureArray(int count)
	{
		Callable<Integer> func = () -> { return 0; };
		com.google.common.util.concurrent.ListenableFutureTask[] array = new com.google.common.util.concurrent.ListenableFutureTask[count];
		for (int i = 0; i < count; ++i) {
			array[i] = com.google.common.util.concurrent.ListenableFutureTask.create(func);
		}
		return array;
	}

	public static void                     threadedRunFutures(RunnableFuture<Integer>[] array)
	{
		Thread t = new Thread(() -> {
			for (RunnableFuture<Integer> v : array) {
				v.run();
			}
		});
		t.start();
		try {
			t.join();
		}
		catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}
}
