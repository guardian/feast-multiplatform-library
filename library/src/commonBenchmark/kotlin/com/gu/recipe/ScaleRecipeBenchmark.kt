package com.gu.recipe

import kotlinx.benchmark.*

@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
class MyBenchmark {
    init {
        println("!!! BENCHMARK CLASS LOADED !!!")
    }
    private val list = (1..1000).toList()

    @Benchmark
    fun testSum(bh: Blackhole): Unit {
        bh.consume(list.sum())
    }
}