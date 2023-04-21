# Comparison of lazy collections and TypeDelegatingFunction to Java code and structures

<!--- benchmark:table:collection:filter=LazilyBuiltLoadingCacheBenchmark\..*&multiply=.001&order=warmLazilyBuiltLoadingCacheBenchmark&order=directConcurrentBenchmark&order=coldLazilyBuiltLoadingCacheBenchmark:: --->

|Benchmark                                                            |Mode|Units|  all|
|:--------------------------------------------------------------------|:---|:----|----:|
|LazilyBuiltLoadingCacheBenchmark.coldLazilyBuiltLoadingCacheBenchmark|avgt|ns/op|7.083|
|LazilyBuiltLoadingCacheBenchmark.directConcurrentBenchmark           |avgt|ns/op|4.670|
|LazilyBuiltLoadingCacheBenchmark.warmLazilyBuiltLoadingCacheBenchmark|avgt|ns/op|2.241|

<!--- benchmark:table:collection:filter=TypeDelegatingFunctionBenchmark\..*&order=ownerCallerBenchmark&order=instanceCallerBenchmark&order=directConcurrentBenchmark:: --->

|Benchmark                                                |Mode|Units|    all|
|:--------------------------------------------------------|:---|:----|------:|
|TypeDelegatingFunctionBenchmark.directInstanceofBenchmark|avgt|ns/op|150.269|
|TypeDelegatingFunctionBenchmark.instanceCallerBenchmark  |avgt|ns/op| 47.790|
|TypeDelegatingFunctionBenchmark.ownerCallerBenchmark     |avgt|ns/op| 52.267|


# Raw data

<!--- benchmark:data:collection:all:: --->

```
Benchmark                                                              Mode  Cnt     Score      Error  Units
LazilyBuiltLoadingCacheBenchmark.coldLazilyBuiltLoadingCacheBenchmark  avgt    3  7083.211 ± 5204.968  ns/op
LazilyBuiltLoadingCacheBenchmark.directConcurrentBenchmark             avgt    3  4669.566 ±  393.256  ns/op
LazilyBuiltLoadingCacheBenchmark.warmLazilyBuiltLoadingCacheBenchmark  avgt    3  2241.048 ±  411.522  ns/op
TypeDelegatingFunctionBenchmark.directInstanceofBenchmark              avgt    3   150.269 ±   17.362  ns/op
TypeDelegatingFunctionBenchmark.instanceCallerBenchmark                avgt    3    47.790 ±    2.654  ns/op
TypeDelegatingFunctionBenchmark.ownerCallerBenchmark                   avgt    3    52.267 ±    1.263  ns/op
```
