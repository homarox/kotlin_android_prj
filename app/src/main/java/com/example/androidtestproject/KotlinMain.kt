package com.example.androidtestproject

import com.example.androidtestproject.kotlinOriginal.coroutine.The003ChannelsMutexBasic
import kotlin.system.measureTimeMillis

fun main() {
    val time = measureTimeMillis {
//    The001CoroutineBasic.exceptionInCoroutine07()
//        The002FlowBasic.exceptionOfFlow08()
        The003ChannelsMutexBasic.mutexControl06()
    }
    println("\n>> TOTAL TIME: $time ms <<")
}
