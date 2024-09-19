package com.example.androidtestproject

import com.example.androidtestproject.kotlinOriginal.coroutine.The002FlowBasic
import com.example.androidtestproject.kotlinOriginal.coroutine.The003ChannelsBasic
import kotlin.system.measureTimeMillis

fun main() {
    val time = measureTimeMillis {
//    The001CoroutineBasic.exceptionInCoroutine07()
//        The002FlowBasic.exceptionOfFlow08()
        The003ChannelsBasic.basicChannels01()
    }
    println("\n>> TOTAL TIME: $time ms <<")
}
