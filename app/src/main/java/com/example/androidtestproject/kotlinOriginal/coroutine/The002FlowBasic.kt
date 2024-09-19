// Refer document: "Cùng học Kotlin Coroutine_12 Parts.pdf"

package com.example.androidtestproject.kotlinOriginal.coroutine

import com.example.androidtestproject.kotlinOriginal.ClassHelper.showCurrentFunctionName
import com.example.androidtestproject.kotlinOriginal.ClassHelper.showSubFunctionName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.cancellation.CancellationException
import kotlin.system.measureTimeMillis

object The002FlowBasic {

    /** -- List nó đợi add xong cả 3 phần tử rồi mới in ra, còn trong ví dụ Sequence thì cứ mỗi giây
     * thì có phần tử được yield và phần tử đó lập tức được in ra ngay mà không phải đợi yield xong cả 3 phần tử.
     * -- Flow khá giống Sequence, thay vì sử dụng hàm yield thì Flow sử dụng hàm emit và nhận các giá trị qua hàm collect,
     *! nhưng khác ở chỗ Sequences xử lý đồng bộ còn Flow xử lý bất đồng bộ.
     *! Các Flow là các luồng lạnh (cold streams) tương tự như các Sequences. Điều đó có nghĩa là code bên trong flow{} sẽ không chạy cho đến khi Flow gọi hàm collect.
     *
     ** Sequence xử lý đồng bộ. Nó sử dụng Iterator và block main thead trong khi chờ đợi item tiếp theo được yield.
     ** Flow xử lý bất đồng bộ. Nó sử dụng một suspend function collect để không block main thread trong khi chờ đợi item tiếp theo được emit.
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

    /** List nó đợi add xong cả 3 phần tử rồi mới in ra, còn trong ví dụ Sequence thì cứ mỗi giây thì có phần tử được yield
     * và phần tử đó lập tức được in ra ngay mà không phải đợi yield xong cả 3 phần tử.
     */
    private fun fooSequences(): Sequence<Int> = sequence { // sequence builder
        for (i in 1..3) {
            Thread.sleep(1000)
            println("yield $i")
            yield(i) // yield next value
        }
    }

    /** Về cơ bản, Flow khá giống Sequence đúng không nào, thay vì sử dụng hàm yield thì Flow sử dụng hàm emit và nhận các giá trị qua hàm collect */
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

    /** Code bên trong flow{} sẽ không chạy cho đến khi Flow gọi hàm collect. */
    fun coldStreamFlow02() {
        showCurrentFunctionName()

        runBlocking {
            println("Calling foo...")
            val flow = fooFlow()
            println("Calling collect...")
            flow.collect { value -> println(value) }
            println("Calling collect again...")
            flow.collect { value -> println(value) }
        }
    }

    /** Flow tuân thủ việc các nguyên tắc cancellation chung của coroutines. Việc collect của flow chỉ có thể bị hủy
     * khi và chỉ khi flow đang bị suspend (chẳng hạn như gặp hàm delay) và ngược lại flow không thể bị hủy.
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

    /** Ngoài cách sử dụng khối flow{} như các đoạn code trên mình đã sử dụng để tạo ra một Flow
     * thì còn có những cách khác để tạo ra đối tượng Flow: flowOf(), asFlow()
     * */
    fun createTheFlow04() {
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

    /** Các API hữu ích khác:
     * - filter(): Toán tử này giúp chúng ta filter lọc ra các giá trị thỏa mãn điều kiện và bỏ qua các giá trị không thỏa mãn điều kiện từ nguồn phát.
     * - onEach(): Toán tử này dùng khi ta muốn thực hiện một action gì đó trước khi value từ flow được emit.
     * - reduce(): Cực hữu ích khi chúng ta cần tính tổng cộng dồn tất cả giá trị được phát ra từ nguồn phát.
     * - fold(): ~reduce(). reduce tính tổng từ con số 0 còn hàm fold tính tổng từ một giá trị được cho trước.
     * - toList(), toSet(): Toán tử này giúp chúng ta convert một flow thành một ArrayList hoặc LinkedHashSet.
     * - single(), singleOrNull(): Nguồn flow chỉ có một phần tử và nó sẽ return giá trị đó. Trường hợp flow có nhiều hay ít hơn 1 phần tử đều bị throw Exception
     * - zip(): Toán tử này dùng để zip 2 flow lại (giống như hàm zip trong Sequence hay List)
     * - combine(): ~zip. Nhưng có 1 sự khác nhau giữa combine và zip
     * - flatMapConcat(), flatMapMerge(), flatMapLatest()
     * */
    fun apiFlow05() {
        showCurrentFunctionName()

        showSubFunctionName("take()")
        // Sử dụng toán tử take() nếu bạn muốn nguồn thu lấy một lượng giới hạn các phần tử được phát ra từ nguồn phát.
        runBlocking {
            val number = flow {
                try {
                    emit(1)
                    emit(2)
                    println("This line will not execute")
                    emit(3)
                } catch (e: CancellationException) {
                    println("exception")
                } finally {
                    println("close resource here")
                }
            }

            number.take(2) // take only the first two
                .collect { value -> println("Value was taken: $value") }
        }

        showSubFunctionName("transform()")
        // Toán tử này được dùng để biến đổi giá trị được phát ra từ nguồn phát trước khi emit cho nguồn thu nhận nó.
        runBlocking {
            val number = (1..5).asFlow() // a flow of requests
                .transform { value ->
                    if (value % 2 == 0) { // Emit only even values, but twice
                        println("-1st transform emit $value")
                        emit(value * value)
                        println("-2nd transform emit $value")
                        emit(value * value * value)
                    } // Do nothing if odd
                }
            number.collect { response -> println("Value of result (1..5): $response") }
        }

        showSubFunctionName("map()")
        // toán tử transform cho phép ta skip phần tử hoặc emit một phần tử nhiều lần còn toán tử map thì không thể skip hay emit multiple times
        runBlocking {
            val number = (1..3).asFlow()
                .map {
                    println("Map $it")
                    it * it
                } // squares of numbers from 1 to 5
            number.collect { println("Value of result (1..3): $it") }
        }
    }

    /** hàm collect (nguồn thu) được gọi bên trong khối runBlocking (sử dụng context với dispatcher là Dispatchers.Main)
     * nên code trong khối flow chạy trên context này tức là chạy trên Dispatchers.Main.
     * trong một số bài toán (đặc biệt là bài toán long-running CPU-consuming code), chúng ta mong muốn code trong khối flow được chạy với Dispatchers.Default (background thread)
     * và update UI với Dispatchers.Main (main thread). Có thể chúng ta sẽ nghĩ đến ngay hàm withContext.
     * withContext được sử dụng để thay đổi context của coroutine. Tuy nhiên code trong khối flow{} nó lại bảo toàn context, có nghĩa là nó đã chạy với context nào rồi
     * thì mãi chạy trên context đó. Ko thể ép nó đổi context bằng hàm withContext được. Nếu dùng hàm withContext sẽ throw Exception .
     * */
    fun errorChangeContextFlow06() {
        showCurrentFunctionName()

        runBlocking {
            changeContextFlow().collect { value -> println(value) }
        }
    }

    private fun changeContextFlow(): Flow<Int> = flow {
        // The WRONG way to change context for CPU-consuming code in flow builder
        withContext(Dispatchers.Default) {
            for (i in 1..3) {
                Thread.sleep(100) // pretend we are computing it in CPU-consuming way
                emit(i) // emit next value
            }
        }
    }

    /** Toán tử flowOn sẽ cho phép code trong khối flow được chạy trên bất kỳ context nào ta muốn */
    fun solutionChangeContextFlow07() {
        showCurrentFunctionName()

        runBlocking<Unit> {
            correctChangeContextFlow().collect { value ->
                println("Collected $value")
            }
        }
    }

    private fun correctChangeContextFlow(): Flow<Int> = flow {
        for (i in 1..3) {
            Thread.sleep(100) // pretend we are computing it in CPU-consuming way
            println("Emitting $i")
            emit(i) // emit next value
        }
    }.flowOn(Dispatchers.Default) // RIGHT way to change context for CPU-consuming code in flow builder

    // Toán tử catch lại không thể catch Exception xảy ra trong hàm collect { } (nguồn thu) như try/catch
    fun exceptionOfFlow08() {
        showCurrentFunctionName()

        showSubFunctionName("Use try/catch")
        runBlocking {
            try {
                flow {
                    for (i in 3 downTo -3) {
                        println("3 / $i = ${3 / i}") // nơi xảy ra exception trong nguồn phát
                        emit(i) // emit next value
                    }
                }.collect { value ->
                    println("VALUE = $value")
                }
            } catch (e: Throwable) {
                println("Caught $e")
            }
        }

        showSubFunctionName("Use catch before collect")
        runBlocking {
            flow {
                for (i in 3 downTo -3) {
                    println("3 / $i = ${3 / i}") // nơi xảy ra exception trong nguồn phát
                    emit(i.toString()) // emit next value
                }
            }.catch { e -> emit("Caught $e") }
                .collect { value ->
                    println("VALUE = $value")
                }
        }

        showSubFunctionName("Use onEach to show exception in collect")
        runBlocking {
            (1..3).asFlow().onEach { delay(100) }
                .onEach { event -> println("Event: $event") }
                .collect { value ->
                    println("VALUE = $value")
                } // <--- Collecting the flow waits
            println("Done")
        }

        showSubFunctionName("Use launchIn")
        /** Toán tử này truyền vào một param là CoroutineScope và return một biến Job.
         * Biến job này có thể giúp chúng ta cancel code trong flow mà không cancel hết cả coroutine.
         * */
        runBlocking {
            (1..3).asFlow().onEach { delay(100) }
                .onEach { event -> println("Event: $event") }
                .launchIn(this) // <--- Launching the flow in a separate coroutine
            println("Done")
        }

        showSubFunctionName("Use onCompletion without exception")
        /** sau khi tiến trình flow kết thúc nó sẽ chạy vào code trong khối onCompletion. Và chúng ta có thể tận dụng
        chỗ này để hide progressBar chẳng hạn. (gần tương tự với try-finally)
         * */
        runBlocking {
            (1..3).asFlow()
                .onCompletion { println("Done in onCompletion") }
                .collect { value -> println(value) }
        }

        showSubFunctionName("Use onCompletion + exception")
        runBlocking {
            flow {
                emit(1)
                throw RuntimeException()
            }
                .onCompletion { cause -> if (cause != null) println("Flow completed exceptionally") }
                .catch { cause -> println("Caught exception") }
                .collect { value -> println(value) }
        }
    }
}