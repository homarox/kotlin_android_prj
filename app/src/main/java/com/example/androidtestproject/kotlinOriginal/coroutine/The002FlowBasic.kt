package com.example.androidtestproject.kotlinOriginal.coroutine

import com.example.androidtestproject.kotlinOriginal.ClassHelper.showCurrentFunctionName
import com.example.androidtestproject.kotlinOriginal.ClassHelper.showSubFunctionName
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.system.measureTimeMillis

// TODO not clear + cold/hot stream
object The002FlowBasic {

    /** -- List nó đợi add xong cả 3 phần tử rồi mới in ra, còn trong ví dụ Sequence thì cứ mỗi giây
     * thì có phần tử được yield và phần tử đó lập tức được in ra ngay mà không phải đợi yield xong cả 3 phần tử.
     * -- Flow khá giống Sequence, thay vì sử dụng hàm yield thì Flow sử dụng hàm emit và nhận các giá trị qua hàm collect,
     *! nhưng khác ở chỗ Sequences xử lý đồng bộ còn Flow xử lý bất đồng bộ.
     *! Các Flow là các luồng lạnh (cold streams) tương tự như các Sequences.
     * Điều đó có nghĩa là code bên trong flow{} sẽ không chạy cho đến khi Flow gọi hàm collect.
     * */
    fun collectionsSequencesFlow01() {
        showCurrentFunctionName()

        runBlocking {
            showSubFunctionName("Collections / List example")
            var time = measureTimeMillis {
                fooCollections().forEach { value -> println(value) }
            }
            println("Done in $time ms")

            showSubFunctionName("Sequences example")
            time = measureTimeMillis {
                fooSequences().forEach { value -> println(value) }
            }
            println("Done in $time ms")

            showSubFunctionName("Flow example")
            time = measureTimeMillis {
                fooFlow().collect { value -> println(value) }
            }
            println("Done in $time ms")
        }

        showSubFunctionName("SEQUENCES vs FLOW: Launch a concurrent coroutine to check if the main thread is blocked")
        runBlocking {
            launch {
                println("_ Current Thread for SEQUENCES: ${Thread.currentThread().name}")
                for (k in 1..3) {
                    delay(900)
                    println("I'm not blocked SEQUENCES $k")
                }
            }

            val time = measureTimeMillis {
                fooSequences().forEach { value -> println("SEQUENCES: $value") }
            }
            println("Done BLOCK MAIN THREAD of Sequences in $time ms")
        }

        runBlocking {
            launch {
                println("_ Current Thread for FLOW: ${Thread.currentThread().name}")
                for (k in 1..3) {
                    delay(900)
                    println("I'm not blocked FLOW $k")
                }
            }
            // Collect the flow
            val time = measureTimeMillis {
                fooFlow().collect { value -> println("FLOW: $value") }
            }
            println("Done BLOCK MAIN THREAD of Flow in $time ms")
        }
    }

    private suspend fun fooCollections(): List<Int> {
        val list = mutableListOf<Int>()
        for (i in 1..3) {
            delay(1000)
            println("Add $i")
            list.add(i)
        }
        return list
    }

    private fun fooSequences(): Sequence<Int> = sequence { // sequence builder
        for (i in 1..3) {
            Thread.sleep(1000)
            println("yield $i")
            yield(i) // yield next value
        }
    }

    private fun fooFlow(testThread: Boolean = false): Flow<Int> = flow {
        for (i in 1..3) {
            if (testThread) {
                Thread.sleep(1000)
            } else {
                delay(1000)
            }
            println("emit $i")
            emit(i) // emit next value
        }
    }

    // Code bên trong flow{} sẽ không chạy cho đến khi Flow gọi hàm collect.
    fun coldStreamFlow02() {
        showCurrentFunctionName()

        runBlocking<Unit> {
            println("Calling foo...")
            val flow = fooFlow()
            println("Calling collect...")
            flow.collect { value -> println(value) }
            println("Calling collect again...")
            flow.collect { value -> println(value) }
        }
    }

    /** Flow tuân thủ việc các nguyên tắc cancellation chung của coroutines. Việc collect của flow chỉ có thể bị hủy
     * khi và chỉ khi flow đang bị suspend (chẳng hạn như gặp hàm delay ) và ngược lại flow không thể bị hủy.
     * */
    fun cancelFlow03() {
        showCurrentFunctionName()

        showSubFunctionName("Test with DELAY")
        runBlocking {
            /** hết thời gian timeout mà flow đang bị suspend vì hàm delay (còn 0.5s nữa thì flow mới
             * hết suspend) nên flow bị cancel và số 3 không được in ra.
             * delay sex check ACTIVE
             * */
            withTimeoutOrNull(2500) {
                fooFlow(testThread = false).collect { value -> println(value) }
            }
            println("Done")
        }

        showSubFunctionName("Test with THREAD SLEEP")
        runBlocking {
            withTimeoutOrNull(2500) { // Timeout after 1s
                fooFlow(testThread = true).collect { value -> println(value) }
            }
            println("Done")
        }
    }

    /** Ngoài cách sử dụng khối flow { } như các đoạn code trên mình đã sử dụng để tạo ra một Flow
     * thì còn có những cách khác để tạo ra đối tượng Flow
     * */
    fun createTheFlow04(){
        showCurrentFunctionName()

        showSubFunctionName("flowOf with multiple Flow elements")
        runBlocking {
            // Define individual flows
            val flow1 = flowOf(1, 2, 3)
            val flow2 = flowOf(4, 5, 6)
            val flow3 = flowOf(7, 8, 9)

            // Create a Flow of Flows
            val flowOfFlows = flowOf(flow1, flow2, flow3)

            // Collect the flows
            flowOfFlows.collect { flow ->
                flow.collect { value ->
                    println("Value of flowOf: $value")
                }
            }
        }

        showSubFunctionName("Creating a Flow of Flow using flow")
        runBlocking {
            // Create a flow that emits other flows
            val flowOfFlows = flow {
                emit(flowOf(1, 2, 3))
                emit(flowOf(4, 5, 6))
                emit(flowOf(7, 8, 9))
            }

            // Collect the flows
            flowOfFlows.collect { flow ->
                flow.collect { value ->
                    println("Value of Flow: $value")
                }
            }
        }

        showSubFunctionName(".asFlow() extension function")
        // Các Collections, Arrays, Sequences hay một kiểu T gì đó đều có thể convert sang Flow thông qua extension function là asFlow()
        runBlocking {
            listOf(1, "abc", 3.4, "def").asFlow().collect { println("Value of asFlow: $it") }
        }
    }
}