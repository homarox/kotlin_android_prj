package com.example.androidtestproject

import com.example.androidtestproject.kotlinOriginal.kotlinStandardAlone.The001BasicKotlin
import kotlin.system.measureTimeMillis

fun main() {
    val time = measureTimeMillis {
//    The001CoroutineBasic.checkJobThread09()
//        The002FlowMutexBasic.stateAndShareFlow10(TypeOfFlow.STATE_FLOW)
//        The003ChannelsBasic.typeOfChannel05()
        The001BasicKotlin.higherOrderFunction01()
    }
    println("\n>> TOTAL TIME: $time ms <<")
}
