package com.example.androidtestproject.coroutineexample

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object CoroutineBasic {
    @OptIn(DelicateCoroutinesApi::class)
    fun basicCoroutine() {
        GlobalScope.launch {
            task2Coroutine2()
        }
        task1()
        println("=====================")

        GlobalScope.launch {
            task2Coroutine1()
        }
        task1()
        Thread.sleep(2000L)
        println("======= END =======")
    }

    private fun task1() {
        println("Hello - Thread: ${Thread.currentThread().name}")
    }

    private suspend fun task2Coroutine1() {
        withContext(Dispatchers.IO) {
            delay(1000L) // Can test remove delay to show difference in ASYNCHRONOUS execution
            println("World1 - Thread: ${Thread.currentThread().name}")
        }
    }

    private suspend fun task2Coroutine2() {
        println("World2 - Thread: ${Thread.currentThread().name}")
    }

    fun checkCoroutineScope() {
        CoroutineScope(CoroutineName("MyScope")).launch {
            val job1 = launch {
//                while (isActive) {
                while (true) {
                    ensureActive()
                    println("Job1 is running in ${Thread.currentThread().name}")
                }
            }
            delay(50L)
            println("Canceling ...")
            job1.cancel()
            println("Job 1 is CANCELLED")
        }
        Thread.sleep(1000L)
        println("======= END =======")
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun channelBasic() {
        val channel = Channel<Language>()

        // Coroutine #1
        GlobalScope.launch(CoroutineName("SENDING")) {
            println("Kotlin sent!")
            channel.send(Language.Kotlin)
            println("Java sent!")
            channel.send(Language.Java)
        }

        // Coroutine #2
        GlobalScope.launch(CoroutineName("RECEIVING")) {
            println("Check Received: ${channel.isClosedForReceive}")
            // Solution 1
//                println("Received: ${channel.receive()}")
//                println("Received: ${channel.receive()}")
            // Solution 2
            channel.consumeEach {
                println("Received: ${it.name}")
            }
            println("Check Received: ${channel.isClosedForReceive}")
        }

        Thread.sleep(5000L)
    }

    fun firstCorountine() {
        // Non-blocking
        GlobalScope.launch { // chạy một coroutine
            delay(10000L) // delay 10s nhưng ko làm blocking app
            println("World,") // print từ World ra sau khi hết delay
        }
        println("Hello,") // main thread vẫn tiếp tục chạy xuống dòng code này trong khi coroutine vẫn đang bị delay 10
        Thread.sleep(20000L) // block main thread 20s
        println("Kotlin")
        // -> Hello, world, Kotlin

        // Blocking
        runBlocking { // chạy một coroutine
            repeat(2) { // launch 100_000 coroutines
                launch {
                    println("Hello")
                    delay(2500)
                }
        }
        println("World")
        // -> Hello Hello World
    }

    private enum class Language { Kotlin, Java, Python }
}