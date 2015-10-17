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
}
