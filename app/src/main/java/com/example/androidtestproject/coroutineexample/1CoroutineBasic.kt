package com.example.androidtestproject.coroutineexample

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlin.system.measureTimeMillis

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

    private enum class Language { Kotlin, Java, Python }

    fun firstCorountine() {
        // Non-blocking
        println(">> Test NON-BLOCKING")
        GlobalScope.launch { // chạy một coroutine
            delay(10000L) // delay 10s nhưng ko làm blocking app
            println("World,") // print từ World ra sau khi hết delay
        }
        println("Hello,") // com.example.androidtestproject.main thread vẫn tiếp tục chạy xuống dòng code này trong khi coroutine vẫn đang bị delay 10
        Thread.sleep(20000L) // block com.example.androidtestproject.main thread 20s
        println("Kotlin")
        // -> Hello, world, Kotlin

        // Blocking
        println(">> Test BLOCKING")
        runBlocking { // chạy một coroutine
            repeat(2) { // launch 100_000 coroutines
                launch {
                    println("Hello")
                    delay(2500)
                }
            }
        }
        println("World")
        // -> Hello Hello World
    }

    fun testJobCorountine() {
        println(">> Test JOIN")
        runBlocking {
            val job = GlobalScope.launch { // launch a new coroutine and keep a reference to its Job
                delay(5000L)
                println("World!")
            }
            println("Hello,")
            job.join() // wait until child coroutine completes
//            delay(7000L)
            println("Kotlin")
        }

        println(">> Test CANCEL")
        runBlocking {
            val job = launch {
                repeat(1000) { i ->
                    println("I'm sleeping $i ...")
                    delay(500L)
                }
            }
            delay(1300L) // delay a bit
            println("com.example.androidtestproject.main: I'm tired of waiting!")
            job.cancel() // cancels the job
            println("com.example.androidtestproject.main: Now I can quit.")
        }

        println(">> Test Coroutine cancellation")
        runBlocking {
            val startTime = System.currentTimeMillis()
            val job = launch(Dispatchers.Default) {
                var nextPrintTime = startTime
                var i = 0
                while (i < 5) {
                    if (System.currentTimeMillis() >= nextPrintTime) {
                        println("job: I'm sleeping ${i++} ...")
                        nextPrintTime += 500L
                    }
                }
            }
            delay(1300L) // delay a bit
            println("com.example.androidtestproject.main: I'm tired of waiting!")
            job.cancel() // cancels the job
            println("com.example.androidtestproject.main: Now I can quit.")
        }

        println(">> Test REWRITE THE CORRECT Coroutine cancellation")
        runBlocking {
            val startTime = System.currentTimeMillis()
            val job = launch(Dispatchers.Default) {
                var nextPrintTime = startTime
                var i = 0
                while (isActive) { // Điều kiện i < 5 đã được thay bằng isActive để ngăn chặn coroutine khi nó đã bị hủy
                    if (System.currentTimeMillis() >= nextPrintTime) {
                        println("job: I'm sleeping ${i++} ...")
                        nextPrintTime += 500L
                    }
                }
            }
            delay(1300L) // delay a bit
            println("com.example.androidtestproject.main: I'm tired of waiting!")
            job.cancel() // cancels the job
        }

        println(">> Test FINALLY to close the resource")
        runBlocking {
            val job = launch {
                try {
                    repeat(1000) { i ->
                        println("I'm sleeping $i ...")
                        delay(500L)
                    }
                } finally {
// Tranh thủ close resource trong này đi nha :D
                    println("I'm running finally")
                }
            }
            delay(1300L) // delay a bit
            println("com.example.androidtestproject.main: I'm tired of waiting!")
            job.cancel() // cancels the job
            println("com.example.androidtestproject.main: Now I can quit.")
        }
    }

    fun testAsyncAwait() {
        println(">> Tuan Tu")
        runBlocking<Unit> {
            val time = measureTimeMillis {
                val one = printOne()
                val two = printTwo()
                println("The answer is ${one + two}")
            }
            println("Completed in $time ms")
        }

        println(">> KHONG Tuan Tu voi Async & Await")
        runBlocking<Unit> {
            val time = measureTimeMillis {
                val one = async { printOne() }
                val two = async { printTwo() }
                println("The answer is ${one.await() + two.await()}")
            }
            println("Completed in $time ms")
        }

        println(">> Lazy Async")
        runBlocking {
            val time = measureTimeMillis {
                val one = async(start = CoroutineStart.LAZY) { printOne() }
                val two = async(start = CoroutineStart.LAZY) { printTwo() }
                one.start() // start the first one
                two.start() // start the second one
                println("The answer is ${one.await() + two.await()}")
            }
            println("Completed in $time ms")
        }
    }

    private suspend fun printOne(): Int {
        delay(1000L)
        return 10
    }

    private suspend fun printTwo(): Int {
        delay(1000L)
        return 20
    }

    fun testParentScope() {
        println(">> Một coroutine cha luôn chờ đợi để tất cả các coroutine con của nó chạy xong hoàn thành nhiệm vụ")
        runBlocking { // scope 1
            launch { // coroutine 1
                delay(200L)
                println("${Thread.currentThread().name} Task from runBlocking") // line code 1
            }
            coroutineScope { // coroutine 2 // scope 2
                launch { // coroutine 3
                    delay(500L)
                    println("${Thread.currentThread().name} Task from nested launch") // line code 2
                }
                delay(100L)
                println("${Thread.currentThread().name} Task from coroutine scope") // line code 3
            }
            println("${Thread.currentThread().name} Coroutine scope is over") // line code 4
        }
    }
}