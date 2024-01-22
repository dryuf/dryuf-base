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


# Comparison of Map implementations

Running 1M gets, puts or removes on `HashMap`, `LinkedHashMap`, `TreeMap`, `TreeCountingMap` .


<!--- benchmark:table:CountingMap:filter=CountingMapBenchmark\.[^_]%2b_.%2b&key=method-benchmark_run&multiply=0.000001&order=TreeCountingMap&order=TreeMap&order=HashMap&order=LinkedHashMap&compare=TreeCountingMap: --->

|Benchmark|Mode|Units|TreeCountingMap|TreeMap|HashMap|LinkedHashMap|TreeCountingMap%|TreeMap%|HashMap%|LinkedHashMap%|
|:--------|:---|:----|--------------:|------:|------:|------------:|---------------:|-------:|-------:|-------------:|
|get      |avgt|ns/op|         76.720| 80.059|  4.098|        4.360|              +0|      +4|     -94|           -94|
|put      |avgt|ns/op|        228.178|227.783| 44.440|       69.635|              +0|      +0|     -80|           -69|
|remove   |avgt|ns/op|        122.947| 50.154|  5.301|        7.170|              +0|     -59|     -95|           -94|


# Raw data

<!--- benchmark:data:collection:all: --->

```
Benchmark                                                              Mode  Cnt     Score      Error  Units
LazilyBuiltLoadingCacheBenchmark.coldLazilyBuiltLoadingCacheBenchmark  avgt    3  7083.211 ± 5204.968  ns/op
LazilyBuiltLoadingCacheBenchmark.directConcurrentBenchmark             avgt    3  4669.566 ±  393.256  ns/op
LazilyBuiltLoadingCacheBenchmark.warmLazilyBuiltLoadingCacheBenchmark  avgt    3  2241.048 ±  411.522  ns/op
TypeDelegatingFunctionBenchmark.directInstanceofBenchmark              avgt    3   150.269 ±   17.362  ns/op
TypeDelegatingFunctionBenchmark.instanceCallerBenchmark                avgt    3    47.790 ±    2.654  ns/op
TypeDelegatingFunctionBenchmark.ownerCallerBenchmark                   avgt    3    52.267 ±    1.263  ns/op
```

<!--- benchmark:data:CountingMap:all: --->
```
Benchmark                                    Mode  Cnt          Score           Error  Units
CountingMapBenchmark.get_HashMap             avgt    3    4098244.575 ±    696408.202  ns/op
CountingMapBenchmark.get_LinkedHashMap       avgt    3    4360012.962 ±   1429266.402  ns/op
CountingMapBenchmark.get_TreeCountingMap     avgt    3   76720226.250 ±   4884276.666  ns/op
CountingMapBenchmark.get_TreeMap             avgt    3   80059254.000 ±  38167143.184  ns/op
CountingMapBenchmark.put_HashMap             avgt    3   44439881.025 ±  21067230.319  ns/op
CountingMapBenchmark.put_LinkedHashMap       avgt    3   69634583.253 ± 128794282.912  ns/op
CountingMapBenchmark.put_TreeCountingMap     avgt    3  228177588.600 ± 267801604.909  ns/op
CountingMapBenchmark.put_TreeMap             avgt    3  227782887.333 ±  38223129.438  ns/op
CountingMapBenchmark.remove_HashMap          avgt    3    5301473.313 ±    321164.712  ns/op
CountingMapBenchmark.remove_LinkedHashMap    avgt    3    7170490.838 ±    974543.129  ns/op
CountingMapBenchmark.remove_TreeCountingMap  avgt    3  122947374.889 ±  42191928.548  ns/op
CountingMapBenchmark.remove_TreeMap          avgt    3   50153870.233 ±  13672615.080  ns/op
```

(Measured on Graviton-3 AWS c7g.medium, ARM 1 vCPU 2 GB RAM)
