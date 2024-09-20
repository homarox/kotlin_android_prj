package com.example.androidtestproject

import com.example.androidtestproject.kotlinOriginal.coroutine.The002FlowMutexBasic
import com.example.androidtestproject.kotlinOriginal.coroutine.The002FlowMutexBasic.TypeOfFlow
import kotlin.system.measureTimeMillis

fun main() {
    val time = measureTimeMillis {
//    The001CoroutineBasic.exceptionInCoroutine07()
        The002FlowMutexBasic.stateAndShareFlow10(TypeOfFlow.STATE_FLOW)
//        The003ChannelsBasic.typeOfChannel05()
    }
    println("\n>> TOTAL TIME: $time ms <<")
}
