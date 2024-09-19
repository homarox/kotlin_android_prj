// Refer document: "Cùng học Kotlin Coroutine_12 Parts.pdf"

package com.example.androidtestproject.kotlinOriginal.coroutine

import com.example.androidtestproject.kotlinOriginal.ClassHelper.showCurrentFunctionName
import com.example.androidtestproject.kotlinOriginal.ClassHelper.showSubFunctionName
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.coroutines.coroutineContext

object The003ChannelsMutexBasic {
    /**! Channels khá giống với Flow nó cũng giúp chúng ta transfer một luồng giá trị (stream of values).
     * Channels khá giống với BlockingQueue, tức là nó cũng hoạt động như một Queue (hàng đợi) là FIFO (First In First Out)
     * Điểm khác nhau ở đây là BlockingQueue nó sử dụng 2 hàm put (thêm vào queue) và hàm take (lấy từ queue ra) và 2 hàm này là chạy blocking
     * còn Channels sử dụng 2 hàm send (tương tự hàm put) và receive (tương tự hàm take) và 2 hàm này là suspend function
     *
     ** Channels vs Flow (Hot & Cold)
     * Như các bạn đã biết Flow là một nguồn dữ liệu lạnh (cold streams). Điều đó có nghĩa là code bên trong flow{} sẽ không chạy cho đến khi Flow gọi hàm collect.
     * Như vậy nếu ko có ai nhận vé thì nó sẽ ko in vé.
     * Còn Channels thì khác, như mình đã nói ở trên thì khi channel send nó sẽ tạm suspend và chờ đến khi có ai đó receive thì nó mới resume và tiếp tục send.
     * Vậy nên khi in ra 1 vé đầu tiên mà ế quá không có ai nhận thì coroutine nhận nhiệm vụ send sẽ bị suspend vĩnh viễn.
     * Việc này rất nguy hại đến memory nên đây chính là nhược điểm lớn của thằng Channels. Vì vậy Channels còn được gọi là nguồn dữ liệu nóng (hot streams).
     *
     *! Channels ra đời trước Flow, có nghĩa là trước khi có Flow thì chúng ta chỉ có một cách duy nhất để transfer a stream of values là sử dụng Channels.
     *! Có thể nhược điểm lớn này của Channels cũng chính là lý do mà Flow ra đời.
     *! Vì vậy, xét về tính phổ biến, mức độ áp dụng vào các dự án thì Channels ít được áp dụng hơn Flow.
     * */

    /** - val channel = Channel<Int>() : tạo một channel để transfer một luồng giá trị kiểu Int
     * - channel.send(value) : thêm một giá trị vào channel
     * - channel.receive() : trả về giá trị được send sớm nhất (first in), đồng thời remove giá trị đó ra khỏi channel
     * */
    @OptIn(ExperimentalCoroutinesApi::class)
    fun basicChannels01() {
        showCurrentFunctionName()

        runBlocking {
            val channel = Channel<Int>()
            val job = launch {
                for (x in 1..5) {
                    channel.send(x * x)
                }
            }
            // print 5 giá trị, trước khi nhận cho delay 1s
            delay(1000) // delay 1s
            println(channel.receive()) // nhận giá trị thứ 1
            delay(1000) // delay 1s
            println(channel.receive()) // nhận giá trị thứ 2
            delay(1000) // delay 1s
            println(channel.receive()) // nhận giá trị thứ 3
            delay(1000) // delay 1s
            println(channel.receive()) // nhận giá trị thứ 4
            delay(1000) // delay 1s
            println(channel.receive()) // nhận giá trị thứ 5
            /** Ở đây mình cho delay 1s trước khi nhận đễ dễ thấy rằng: channel sau khi in ra 1 vé (channel.send()) mà chưa
             * thấy ai nhận thì coroutine nhận nhiệm vụ in vé đó sẽ suspend lại chờ đến khi có người nhận vé đó (channel.receive())
             * thì nó mới resume trở lại và in tiếp.
             * */
//            println("Out of data")
//            println(channel.receive()) //! Can not run
            // job.isCompleted = false (coroutine chưa hoàn thành task) và job.isActive = true (coroutine còn sống nhăn răng)
            println("Done! Channel is empty?: ${channel.isEmpty} / Coroutine is completed?: ${job.isCompleted} / Coroutine")
        }
    }

    fun receiveFromChannel02() {
        showCurrentFunctionName()

//        showSubFunctionName("Use 2 channels")
//        runBlocking {
//            val channel = Channel<Int>()
//            val job = launch {
//                for (x in 1..5) {
//                    channel.send(x * x)
//                }
//            }
//            for (x in 1..10) {
//                println("Coroutine is completed?: ${job.isCompleted} / Coroutine is active?: ${job.isActive}")
//                println(channel.receive())
//            }
//            println("Done! Run blocking coroutine is active?: $isActive") //! Can not run
//        }

        showSubFunctionName("Use 1 channels")
        /** channel send, thì Send coroutine bị suspend, nó sẽ resume lại khi có có 1 coroutine khác đã receive được cái giá trị nó vừa send.
         * Khi channel gọi hàm receive cũng vậy, Receive coroutine cũng sẽ bị suspend cho đến khi có 1 coroutine khác send giá trị để nó nhận.
         * */
        runBlocking { // Receive coroutine
            val channel = Channel<Int>()
            val job = launch { // Send coroutine
                for (x in 1..5) {
                    println("Start sending...")
                    channel.send(x * x)
                    println("Going to receive...")
                    println(channel.receive())
                }
            }
            println("Done! Receive coroutine is active?: $isActive / Send coroutine is active?: ${job.isActive}")
        }
    }

    fun closeChannel03() {
        showCurrentFunctionName()

        showSubFunctionName("Basic close channel")
        runBlocking {
            val channel = Channel<Int>()
            launch {
                for (x in 1..5) channel.send(x * x)
                channel.close() // we're done sending
            }
            // here we print received values using `for` loop (until the channel is closed)
            for (value in channel) println(value)
            println("Done!")
        }

//        showSubFunctionName("channel đã close nhưng vẫn cố gắng receive thì sẽ throw ClosedReceiveChannelException")
//        runBlocking {
//            val channel = Channel<Int>()
//            launch {
//                for (x in 1..5) channel.send(x * x)
//                channel.close() // sau khi send xong 5 phần tử thì close
//            }
//            for (y in 1..10) println(channel.receive()) // send có 5 mà nhận tới 10
//            println("Done!") //! Can not run
//        }

//        showSubFunctionName("channel đã close nhưng vẫn cố gắng send thì sẽ throw ClosedSendChannelException")
//        runBlocking {
//            val channel = Channel<Int>()
//            launch {
//                for (i in 1..10) {
//                    if (i == 5) channel.close() // nếu i = 5 thì close đi, ko được send nữa.
//                    channel.send(i * i) // nhưng ta vẫn cố send i = 5 -> throw ClosedSendChannelException
//                }
//            }
//            for (y in 1..5) {
//                println(channel.receive())
//            }
//            println("Done!") //! Can not run
//        }

        showSubFunctionName("custome Exception")
        /**Chúng ta có thể custom lại Exception trong hàm close thay vì cho throw ClosedSendChannelException
         * hay ClosedReceiveChannelException bằng hàm close(cause: Throwable?)
         * */
        runBlocking {
            val channel = Channel<Int>()
            launch {
                for (i in 1..10) {
                    if (i == 5) channel.close(Throwable("ta cho lệnh đóng channel lại!")) // nếu i = 5 thì close đi
                    channel.send(i * i) // nhưng ta vẫn cố send i = 5 -> throw Throwable
                }
            }
            for (y in 1..5) {
                println(channel.receive())
            }
            println("Done!")
        }
    }


    // chúng ta có thể sử dụng các hàm produce , actor để đơn giản hóa việc tạo ra một coroutine và một Channel
    private fun CoroutineScope.produceSquares(): ReceiveChannel<Int> = produce {
        // Chúng ta có thể sử dụng các hàm của SendChannel trong block này.
        // Ở đây mình dùng hàm send
        for (x in 1..5) send(x * x)
    }

    fun CoroutineScope.myActor(): SendChannel<Int> = actor {
        // Chúng ta có thể sử dụng các hàm của ReceiveChannel trong block này.
        // Ở đây mình dùng hàm receive
        val value = receive()
        println(value)
    }

    // truyền dữ liệu từ coroutine A sang coroutine B
    fun produceAndActor04() {
        showCurrentFunctionName()

        runBlocking { // coroutine B
            // hàm produceSquares() trả về 1 ReceiveChannel. Nhờ đó mà ta có thể receive giá trị dễ dàng
            val squares: ReceiveChannel<Int> = produceSquares()
            // Hàm consumeEach là 1 extension function của ReceiveChannel.
            // Nó giúp chúng ta receive value và cho phép thực hiện 1 action gì đấy với giá trị vừa receive được.
            squares.consumeEach {
                println("number $it")
            }
            println("Done!")
        }

        runBlocking {
            // hàm myActor() return SendChannel nên ta có thể dùng nó để send data
            val myActor = myActor()
            myActor.send(123)
            println("Done!")
        }
    }


    fun typeOfChannel05() {
        showCurrentFunctionName()

        showSubFunctionName("Buffered Channel")
        /** Ở bài trước, chúng ta đã biết khi channel send mà ko có ai nhận thì con coroutine nhận nhiệm vụ send đó sẽ bị suspend cho đến khi có ai đó nhận giá trị nó vừa send.
         * Tương tự, khi channel receive mà ko có giá trị để receive (channel is empty) thì con coroutine nhận nhiệm vụ receive này cũng bị suspend
         * cho đến khi có ai đó send cho nó giá trị để nó nhận.
         * Channel được mình giới thiệu ở bài trước chính là loại channel mặc định hay còn gọi là Rendezvous channel hay Unbuffered channel.
         * Vì nó ko có buffer nên nó mới bị suspend như vậy. Để biết buffer là gì thì chúng ta đến với loại thứ 2: Buffered Channel
         * */
        runBlocking {
            val channel = Channel<Int>(capacity = 4) // tạo ra 1 buffered channel với capacity = 4
            val sender = launch {
                // launch 1 coroutine để send data
                repeat(10) { // send 10 data
                    channel.send(it) // hàm send sẽ bị suspend khi buffer is full
                    println("Sending $it") // in ra sau khi send
                }
            }
            // cố ý ko nhận data để xem thằng send có bị suspend ko
            delay(2000)
            println("Cancel sender ...")
            sender.cancel() // cancel sender coroutine
        }

        showSubFunctionName("Conflated channel")
        /** Conflated channel Là 1 buffered channel nhưng capacity chỉ bằng 1. Thằng này khác thằng bufferd channel ở chỗ mặc dù chỉ có capacity = 1
         * nhưng khi full nó ko suspend thằng coroutine sender mà cho phép thằng sender tiếp tục send value, giá trị mới nhất sẽ overwrite lên giá trị cũ.
         * */
        runBlocking {
            val channel = Channel<Int>(Channel.CONFLATED)
            val sender = launch {
                repeat(5) { // send 5 giá trị
                    println("Sending $it")
                    channel.send(it)
                }
            }
            delay(1000) // delay 1s để nó send đủ cả 5 giá trị rồi mới nhận
            channel.consumeEach { println("item = $it") } // in ra tất cả giá trị nhận được
            sender.cancel() // cancel sender coroutine
        }

        showSubFunctionName("Unlimited channel")
        /** Nó là 1 buffered channel nhưng capacity = vô tận . Khác với buffered channel lưu trữ buffered data trong 1 Array thì Unlimited channel lưu trữ data trong 1 LinkedList.
         *  Vì là List nên nó lưu trữ vô hạn, tất nhiên khi hết memory để lưu trữ thì nó sẽ throw OutOfMemoryException.
         *  Vì buffer vô hạn nên coroutine sender không bao giờ bị suspend. Cho nó send data vô tư thoải mái luôn.
         *  Nhưng nếu channel is empty, coroutine nhận cũng sẽ bị suspend (giống Unbuffered channel)
         * */
        runBlocking {
            val channel = Channel<Int>(Channel.UNLIMITED)
            val sender = launch {
                repeat(7) { // send 7 data
                    channel.send(it)
                }
            }
            delay(1000)
            // cố tình delay 1s để coroutine receiver ko thể receive value. Xem coroutine sender có bị suspend ko?
            repeat(7) { // nhận 7 data
                val value = channel.receive()
                println("number $value")
            }
            sender.cancel() // cancel sender coroutine
        }
    }

    /** Trong ngữ cảnh coroutines, mutex (viết tắt của mutual exclusion) là một cơ chế đồng bộ hóa được sử dụng để đảm bảo rằng
     * chỉ có một coroutine có thể truy cập vào một tài nguyên chia sẻ tại một thời điểm. Điều này rất quan trọng khi nhiều coroutine
     * cùng thao tác trên cùng một dữ liệu, nhằm tránh các vấn đề về race condition (điều kiện tranh chấp) hoặc sự không nhất quán của dữ liệu.
     * Vì vậy, mutex trong coroutines là công cụ quan trọng để quản lý tài nguyên chia sẻ một cách an toàn trong môi trường lập trình đồng thời.
     ** https://medium.com/mobile-app-development-publication/mutex-for-coroutines-5f4a4ca60763
     * */
    fun mutexControl06() {
        val mutex = Mutex() // Create a Mutex to control access
        var sharedResource = 0 // Shared resource that multiple coroutines will access
        runBlocking {
            // Launch 3 coroutines to increment the shared resource
            // Each coroutine increments 10 times
            val times = 10
            val job1 = launch(CoroutineName("job_1")) {
                repeat(times) {
                    sharedResource = incrementResource(mutex, sharedResource)
                }
            }
            val job2 = launch(CoroutineName("job_2")) {
                repeat(times) {
                    sharedResource = incrementResource(mutex, sharedResource)
                }
            }
            val job3 = launch(CoroutineName("job_3")) {
                repeat(times) {
                    sharedResource = incrementResource(mutex, sharedResource)
                }
            }

            // Wait for all coroutines to finish
            job2.join()
            job1.join()
            job3.join()

            println("Final value of sharedResource: $sharedResource")
        }
    }

    // Function to increment the shared resource
    private suspend fun incrementResource(mutex: Mutex, sharedResource: Int): Int {
        var localSharedResource = sharedResource
        // Use withLock to ensure only one coroutine accesses this block at a time
        mutex.withLock {
            localSharedResource += 1
            println("Resource incremented by ${Thread.currentThread().name} - ${coroutineContext[CoroutineName]?.name}: $localSharedResource")
        }
        return localSharedResource
    }
}