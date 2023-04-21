# Comparing CloseableExecutor to core Java Executor

<!--- benchmark:table:executor:key=class&multiply=0.001&order=JavaExecutorBenchmark&order=ClosingExecutorBenchmark&compare=JavaExecutorBenchmark: --->

|Benchmark |Mode|Units|JavaExecutorBenchmark|ClosingExecutorBenchmark|JavaExecutorBenchmark%|ClosingExecutorBenchmark%|
|:---------|:---|:----|--------------------:|-----------------------:|---------------------:|------------------------:|
|b0_execute|avgt|ns/op|               66.698|                  78.108|                    +0|                      +17|
|b0_submit |avgt|ns/op|              117.385|                 128.351|                    +0|                       +9|


# Raw data

<!--- benchmark:data:executor:all:: --->

```
Benchmark                            Mode  Cnt       Score   Error  Units
ClosingExecutorBenchmark.b0_execute  avgt    2   78107.811          ns/op
ClosingExecutorBenchmark.b0_submit   avgt    2  128351.491          ns/op
JavaExecutorBenchmark.b0_execute     avgt    2   66697.839          ns/op
JavaExecutorBenchmark.b0_submit      avgt    2  117384.907          ns/op
```
