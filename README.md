# zbynek-concurrent-pof

Zbynek's concurrent experiments and proofs of concepts

Currently there is only single project:

## [Lock free ListenableFuture / Future implementation](zbynek-lwfuture-pof/)

The project implements java concurrent (Listenable) Future in very cheap and flexible way.

The performance is for obvious reasons (additional support of listeners) slightly lower than original JDK Future but significantly higher than similar implementations in Guava and Spring.

### Flexibility

Additionally it solves several design issues from which suffer Guava and Spring implementations:
- allows several types of listeners, which can just implement Runnable or receive the original Future or receive directly the result to appropriate method
- distinguishes failure and cancellation when invoking listener
- distinguishes not started and running state
- allows delayed cancel notifications, i.e. the notification about cancel can be postponed until the task really exits, this is useful when task occupies some shared resource like network port

### Performance

The performance comparison looks like (measured on my low voltage i7 x86_64):

#### No listener
```
Benchmark                                    Mode  Cnt    Score   Error  Units
NoListenerAsyncBenchmark.benchmarkGuava     thrpt    2  157.510          ops/s
NoListenerAsyncBenchmark.benchmarkJdk       thrpt    2  241.154          ops/s
NoListenerAsyncBenchmark.benchmarkLwFuture  thrpt    2  279.774          ops/s
NoListenerAsyncBenchmark.benchmarkSpring    thrpt    2  117.028          ops/s
```

It's 78% faster than Guava, 139% faster than Spring and 16% faster than JDK.

#### Single listener set prior to run
```
Benchmark                                           Mode  Cnt    Score   Error  Units
SinglePreListenerAsyncBenchmark.benchmarkGuava     thrpt    2   98.639          ops/s
SinglePreListenerAsyncBenchmark.benchmarkJdk       thrpt    2  239.813          ops/s
SinglePreListenerAsyncBenchmark.benchmarkLwFuture  thrpt    2  204.984          ops/s
SinglePreListenerAsyncBenchmark.benchmarkSpring    thrpt    2   82.673          ops/s
```

It's 108% faster than Guava, 148% faster than Spring and 15% slower than JDK (but JDK test runs without  listener).

## License

The code is released under version 2.0 of the [Apache License][].

## Stay in Touch

Feel free to contact me at kvr@centrum.cz or http://kvr.znj.cz/software/java/ListenableFuture/ and http://github.com/kvr000

[Apache License]: http://www.apache.org/licenses/LICENSE-2.0
