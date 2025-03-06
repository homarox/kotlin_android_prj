package com.example.androidtestproject

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.system.measureTimeMillis

suspend fun main() {
    val time = measureTimeMillis {
//    The001CoroutineBasic.checkJobThread09()
//        The002FlowMutexBasic.stateAndShareFlow10(TypeOfFlow.STATE_FLOW)
//        The003ChannelsBasic.typeOfChannel05()
//        The001BasicKotlin.higherOrderFunction01()
        val job = GlobalScope.launch { // launch a new coroutine and keep a reference to its Job
            delay(5000L)
            println("World!")
        }
        println("Hello,")
        job.join() // wait until child coroutine completes
        println("Kotlin")

    }
    println("\n>> TOTAL TIME: $time ms <<")
}
