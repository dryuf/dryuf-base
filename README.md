# Dryuf Base

Set of common utilities related to collections, functions and concurrency services.

### Release

```
<dependency>
	<groupId>net.dryuf</groupId>
	<artifactId>dryuf-base</artifactId>
	<version>1.8.0</version>
</dependency>
```

## Instance type based Function

Class wrapping instanceof checks into Function interface, delegating the calls according to type of passed argument.

Instead of writing many ```instanceof``` checks, it allows specifying the Map of types and the callback to call for the matching type. The implementation is smart enough to figure any subclasses, in the order items coming from the Map. There are two ways of specifying the Map of callback functions, one of them wraps only Function objects, the other wraps into BiFunction objects, allowing specifying the owner object first. The latter allows the callback delegator to be shared among multiple calling instances.


### Usage

```
public Callee
{
	// Note the second parameter to add method is BiFunction, so it is real instance method reference, not static reference
	private static ThrowingBiFunction<Callee, Object, Object> ownerCaller = new TypeDelegatingOwnerBiFunction<>(TypeDelegatingOwnerBiFunction.<Callee, Object, Object>callbacksBuilder()
			.add(First.class, Callee::callFirst)
			.add(Second.class, Callee::callSecond)
			.add(Third.class, Callee::callThird)
			.add(Fourth.class, Callee::callFourth)
			.add(Fifth.class, Callee::callFifth)
			.build()
	);

	public Object call(Object input)
	{
		ownerCaller.call(this, input);
	}

	private Object callFirst(First input) { ... }
	private Object callSecond(Second input) { ... }

	...
}
```

## Custom Executor

### CloseableExecutor

Interface allowing automatically closing Executor in try-with-resources statements, mostly to simplify unit tests.

#### ClosingExecutor and NotClosingExecutor

Implementation of the above, wrapping the existing ExecutorService into another Executor and either shutting down in
`close()` method for ClosingExecutor or not shutting it down for NotClosingExecutor.
Both of them wait until all submitted tasks are processed.

#### UncontrolledCloseableExecutor

CloseableExecutor not closing delegated executor, neither executions of current tasks.  This is simplified version when
instance of CloseableExecutor is required but not any additional control because delegated executor is typically shared.

### ResourceClosingExecutor and ResourceNotClosingExecutor

CloseableExecutor implementations, closing also associated AutoCloseable resource, tying lifecycle of executor together
with another resource.  Mostly useful in tests or benchmarks where lifecycle of items is limited to scope.

### SequencingExecutor

Executor executing tasks in order of submission.  This is useful when tasks are tied to specific resource (such as
connection) but delegating executor is shared.

### ResultSequencingExecutor

Executor executing tasks in parallel but finishing the results sequentially in the order of submission.  This is useful
when the tasks can be parallelized but they write to shared resource at the end.

### CapacityResultSequencingExecutor

Executor running tasks in parallel but finishing the results sequentially in the order of submission.  Additionally, it
controls throughput by given capacity and number of parallel tasks.  Typically, the capacity is constrained by memory or
disk size or number of connections.

### FinishingSequencingExecutor

Executor executing tasks in order of submission.  Once there is no tasks pending, it will additionally call `finisher`
function to review the current state.

### SingleConsumerQueue

Queue for submitting tasks and consuming them from single consumer, guaranteed to be executed in unique instance.

### WorkExecutor, SingleWorkExecutor, BatchWorkExecutor

Executor processing work items instead of executing code.  Items are processed either in separate tasks
(SingleWorkExecutor) or batched into groups to optimize throughput (BatchWorkExecutor).


## Synchronization primitives

### RunSingle

Ensures there is only single activity running at the same time.  Typically used when there is repeated activity and new
one should not be started until previous one finished.

### CountDownRunner

Similar to CountDownLatch but instead of actively waiting, it allows registering callback which is executed once the
object reaches target.


## Benchmarks

- [Collections](dryuf-base-benchmark/collection-benchmark/)
- [Executors](dryuf-base-benchmark/collection-benchmark/)


## License

The code is released under version 2.0 of the [Apache License][].

## Stay in Touch

Feel free to contact me at kvr000@gmail.com and http://github.com/kvr000/ and http://github.com/dryuf/ and https://www.linkedin.com/in/zbynek-vyskovsky/

[Apache License]: http://www.apache.org/licenses/LICENSE-2.0

<!--- vim: set tw=120: --->
