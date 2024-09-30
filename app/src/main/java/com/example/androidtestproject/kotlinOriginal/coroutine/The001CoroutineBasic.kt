// Refer document: "Cùng học Kotlin Coroutine_12 Parts.pdf"
// Refer document: "Kotlin Coroutines by Tutorials _ 3rd [2022].pdf"

package com.example.androidtestproject.kotlinOriginal.coroutine

import com.example.androidtestproject.kotlinOriginal.ClassHelper.showCurrentFunctionName
import com.example.androidtestproject.kotlinOriginal.ClassHelper.showSubFunctionName
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlin.system.measureTimeMillis

object The001CoroutineBasic {

    /** Bloc launch{} là một coroutine builder. Nó phóng một coroutine chạy đồng thời (concurrently)
     * với các phần code còn lại. Đó là lý do từ "Hello" được print ra đầu tiên.
     * GlobalScope là coroutine scope. Chúng ta không thể launch một coroutine nếu nó không có scope.
     * Hàm delay() nhìn thì có vẻ giống hàm Thread.sleep() nhưng chúng rất khác nhau.
     * Bởi vì hàm delay() là một suspend function, nó sẽ không block thread (non-blocking thread)
     * còn hàm Thread.sleep() thì block thread.
     * */
    @OptIn(DelicateCoroutinesApi::class)
    fun firstCoroutine01() {
        showCurrentFunctionName()

        showSubFunctionName("GlobalScope.launch")
        GlobalScope.launch { // chạy một coroutine
            delay(1000L) // delay 1s nhưng ko làm blocking app
            println("World,") // print từ World ra sau khi hết delay
        }
        println("Hello,") // main thread vẫn tiếp tục chạy xuống dòng code này trong khi coroutine vẫn đang bị delay 1s
        Thread.sleep(2000L) // block main thread 2s
        println("Kotlin")

        showSubFunctionName("runBlocking")
        runBlocking(){
            println("Hello,")
            delay(1000L)
            println("World,")
        }
    }

    /** Các dòng lệnh không nhất thiết phải lúc nào cũng phải thực hiện một cách tuần tự (sequential)
     * và đồng bộ (synchronous) với nhau.
     * Các dòng lệnh phía sau được chạy ngay sau khi dòng lệnh phía trước được gọi mà không cần đợi
     * cho tới khi dòng lệnh phía trước chạy xong. Để thực hiện mô hình Non-Blocking, người ta có những
     * cách để thực hiện khác nhau, nhưng về cơ bản vẫn dựa vào việc dùng nhiều Thread (luồng) khác
     * nhau trong cùng một Process (tiến trình), hay thậm chí nhiều Process khác nhau
     * (inter-process communication – IPC) để thực hiện.
     *
     * Job và Dispatcher là 2 element chính trong CoroutineContext:
     * - Job: nắm giữ thông tin về lifecycle của coroutine
     * - Dispatcher: Quyết định thread nào mà coroutine sẽ chạy trên đó. Có các loại dispatcher sau:
     * -- Dispatchers.Main: chạy trên main UI thread
     * -- Dispatchers.IO: chạy trên background thread của thread pool. Thường được dùng khi Read, write files, Database, Networking
     * -- Dispatchers.Default: chạy trên background thread của thread pool. Thường được dùng khi sorting a list, parse Json, DiffUtils
     * -- newSingleThreadContext("name_thread"): chạy trên một thread do mình đặt tên
     * -- newFixedThreadPoolContext(3, "name_thread"): sử dụng 3 threads trong shared background thread pool
     * */
    @OptIn(ExperimentalCoroutinesApi::class, DelicateCoroutinesApi::class)
    fun blockAndNonBlock02() {
        showCurrentFunctionName()

        newSingleThreadContext("thread1").use { ctx1 ->
            // tạo một context là ctx1 chứ chưa launch coroutine. ctx1 sẽ có 1 element là dispatcher quyết định coroutine sẽ chạy trên 1 thread tên là thread1
            println("ctx1 - ${Thread.currentThread().name}")
            newSingleThreadContext("thread2").use { ctx2 ->
                // tạo một context là ctx2 chứ vẫn chưa launch coroutine. ctx2 sẽ có 1 element là dispatcher quyết định coroutine sẽ chạy trên 1 thread tên là thread2
                println("ctx2 - ${Thread.currentThread().name}")
                // bắt đầu chạy coroutine với context là ctx1
                runBlocking(ctx1) {
                    // coroutine đang chạy trên context ctx1 và trên thread thread1
                    println("Started in ctx1 - ${Thread.currentThread().name}")
                    // sử dụng hàm withContext để chuyển đổi context từ ctx1 qua ctx2
                    withContext(ctx2) {
                        // coroutine đang chạy với context ctx2 và trên thread thread2
                        println("Working in ctx2 - ${Thread.currentThread().name}")
                    }
                    // coroutine đã thoát ra block withContext nên sẽ chạy lại với context ctx1 và trên thread thre
                    println("Back to ctx1 - ${Thread.currentThread().name}")
                }
            }
            println("out of ctx2 block - ${Thread.currentThread().name}")
        }
        println("out of ctx1 block - ${Thread.currentThread().name}")
    }

    fun joinAndCancel03() {
        showCurrentFunctionName()

        runBlocking {
            // Khi một coroutine gọi hàm join() này thì tiến trình phải đợi coroutine này chạy xong task của mình rồi mới chạy tiếp.
            val job = launch { // launch a new coroutine and keep a reference to its Job
                delay(5000L)
                println("World!")
            }
            println("Hello,")
            job.join() // wait until child coroutine completes
            println("Kotlin")
        }

        println("---------------")

        runBlocking {
            val startTime = System.currentTimeMillis()
            val job = launch(Dispatchers.Default) {
                try {
                    var nextPrintTime = startTime
                    var i = 0
//                while (i < 5){
                    // ! quá trình hủy bỏ coroutine có tính hợp tác (Coroutine cancellation is cooperative).
                    // Một coroutine khi bị cancel thì nó sẽ chỉ set lại một property có tên là isActive trong đối tượng Job từ true thành false
                    // (job.isActive = false), còn tiến trình của nó đang chạy thì sẽ vẫn chạy bất chấp cho đến hết mà không bị dừng lại.
                    while (isActive) {
                        if (System.currentTimeMillis() >= nextPrintTime) {
                            println("job: I'm sleeping ${i++} ...")
                            nextPrintTime += 500L
                        }
                    }
                } finally {
                    // Tranh thủ close resource trong này
                    println("I'm running finally")
                    // Coroutine vẫn có thể chết trong khối finally
                    delay(1000L) // hàm delay được thêm vào khối finally
                    /** Tại sao coroutine chạy vào khối finally in ra được câu "I'm running finally" nhưng lại không thể tiếp tục
                     * chạy xuống code dưới để in ra câu "Print me please!". Tất cả tại thằng hàm delay(). Như mình đã nói ở trên,
                     * hàm delay() nó riêng hay tất cả hàm suspend function nói chung có khả năng check xem coroutine còn sống
                     * không. Nếu nó đã chết thì tiến trình lập tức bị dừng lại ngay khi chạy vào hàm delay() này.
                     * Vậy thì câu "Print me please!" tất nhiên sẽ không được in ra rồi
                     * */
                    println("Print me please!")
                }
            }
            // Để dừng và hủy bỏ một coroutine đang chạy. Ta có thể dùng method cancel() của biến Job
            delay(1300L) // delay a bit
            println("main: I'm tired of waiting!")
            job.cancel() // cancels the job
            println("main: Now I can quit.")
        }

        println("---------------")

        /**
         * - Hàm withContext() có tác dụng điều chỉnh lại context của coroutine. Cụ thể trước đó coroutine lúc mới
         * được sinh ra thì bản thân nó default là Cancellable (có thể hủy bỏ được) nhưng khi coroutine chạy được một
         * lúc rồi mình lại muốn nó đổi context thành NonCancellable (không thể hủy bỏ được). Khi đó hàm
         * withContext() sẽ giúp chúng ta thực hiện việc điều chỉnh đó. Công dụng khác của hàm withContext() có
         * thể kể đến như một coroutine thực thi task dưới background thread ( Dispatchers.IO ) và sau khi xong task
         * thì cho nó chạy tiếp trên main thread withContext(Dispatchers.Main) để update UI chẳng hạn.
         * - NonCancellable là một element trong tập context của coroutine. Công dụng của nó là khiến cho coroutine
         * trở nên bất tử, không thứ gì có thể khiến nó dừng lại cho đến khi nó hoàn thành xong task
         * */
        runBlocking {
            val job = launch {
                try {
                    repeat(1000) { i ->
                        println("I'm sleeping $i ...")
                        delay(500L)
                    }
                } finally {
                    withContext(NonCancellable) { // Nhờ có em NonCancellable mà anh được phép chạy bất chấp đấy
                        println("I'm running finally")
                        delay(1000L)
                        println("I'm non-cancellable")
                    }
                }
            }
            delay(1300L) // delay a bit
            println("main: I'm tired of waiting!")
            job.cancel() // cancels the job
            println("main: Now I can quit.")
        }
    }

    /** runBlocking để launch 1 coroutine duy nhất và chạy tuần tự từ trên xuống dưới. Coroutine nó chạy xong hàm
     * printOne() rồi mới chạy tiếp hàm printTwo() , sau đó print ra tổng 2 số đó. Ở đây mình sử dụng hàm
     * measureTimeMillis để đo kết quả thực hiện bài toán này khi sử dụng 1 coroutine duy nhất.
     *
     * Ngoài 2 thằng dùng để launch coroutine mà mình đã biết là runBlocking{} và GlobalScope.launch{}, 2 thằng này nó return về kiểu Job.
     * Nay mình sẽ biết thêm một thằng mới cũng để launch coroutine mà không return về kiểu Job nữa, đó là async{} .
     * */
    fun runBlockingForSequentially04() {
        showCurrentFunctionName()

        runBlocking {
            val time = measureTimeMillis {
                val one = printOne()
                val two = printTwo()
                println("The answer is ${one + two}")
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

    /**-- Thứ nhất: async{} nó cũng như runBlocking{} hay launch{} vì nó cũng được để launch 1 coroutine.
     * Điểm khác biệt là khi sử dụng async để launch 1 coroutine thì coroutine đó cho phép bạn return về 1 giá trị kiểu
     * Int , String , Unit , ... kiểu gì cũng được còn 2 thằng kia thì luôn return kiểu Job mà thằng Job này chỉ có thể quản lý
     * lifecycle của coroutine chứ không mang được giá trị kết quả gì (Job does not carry any resulting value).
     * -- Thứ hai là Deferred<T>: để ý khi bạn return về kiểu Int trong khối block của coroutine thì kết quả trả về
     * của async là kiểu Deferred<Int>, return kiểu String thì trả về kiểu Deferred<String>, không return gì
     * cả thì nó sẽ trả về kiểu Deferred<Unit>. Deferred nó cũng giống Job vậy, nó cũng có thể quản lý lifecycle
     * của coroutine nhưng ngon hơn thằng Job ở chỗ nó mang theo được giá trị kết quả trả về của coroutine.
     * -- Thứ ba là await(): như đã giải thích ở trên, await() là một member function của Deferred dùng để get
     * giá trị kết quả trả về. Ví dụ biến kiểu Deferred<Int> thì gọi hàm await() sẽ trả về giá trị kiểu Int.
     * */
    fun asyncAndAwait05() {
        showCurrentFunctionName()

        runBlockingForSequentially04()
        showSubFunctionName("Async Compare with #runBlockingForSequentially04")
        runBlocking {
            val time = measureTimeMillis {
                val one = async { printOne() }
                val two = async { printTwo() }
                println("The answer is ${one.await() + two.await()}")
            }
            println("Completed in $time ms")
        }

        /**Khi khai báo async kiểu lazy thì coroutine sẽ không chạy ngay. Nó sẽ chỉ chạy code trong block
         * khi có lệnh từ hàm start(). Để khai báo async theo kiểu lazy cũng rất dễ, chỉ cần truyền
         * CoroutineStart.LAZY vào param start trong hàm async là được.
         * */
        showSubFunctionName("Add Lazy Async")
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

    /**- Tất cả coroutine builder mà trong các bài trước mình đã giới thiệu như launch{} hay async{} đều là những
     * extension function của lớp CoroutineScope. Chính vì vậy bạn không thể gọi các hàm launch{} và async{} bên
     * ngoài một CoroutineScope được. Riêng runBlocking{} thì không phải là extension function của CoroutineScope
     * mà nó nhận CoroutineScope như một tham số truyền vào nên nó thể được gọi ngoài CoroutineScope.
     * - Bản thân runBlocking{} nhờ nhận CoroutineScope như 1 param nên nó tạo ra 1 scope để có thể chạy được các coroutine
     * bên trong đó.
     * - Vậy hãy ghi nhớ, không thể launch 1 coroutine nếu nó không có scope. Hay nói cách khác,
     * ngoài vùng CoroutineScope thì không thể launch coroutine nào cả.
     *
     * -> Khi một coroutine A được phóng trong CoroutineScope của một coroutine B khác thì A là con của B.
     * Coroutine con sẽ sử dụng scope và context của coroutine cha. Nếu coroutine con đó được khai báo trong 1 scope riêng
     * với context riêng thì nó sẽ ưu tiên sử dụng scope đó thay vì của cha nó.
     * -> Nay chúng ta đã biết thêm một coroutine builder nữa là coroutineScope{}. Nó cũng chạy tuần tự như runBlocking{} vậy,
     * chỉ khác là nó là một suspend function nên chỉ có thể tạo ra bên trong một suspend function khác hoặc trong một coroutine scope.
     * -> Khi coroutine cha bị hủy, tất cả các con của nó cũng bị hủy theo.
     * Chúng ta đã biết khi cancel coroutine cha thì tất cả coroutine con bị cancel. Tuy nhiên nếu coroutine con đó có
     * scope là GlobalScope thì nó sẽ không bị cancel khi coroutine cha bị hủy.
     * */
    @OptIn(DelicateCoroutinesApi::class)
    fun coroutineScope06() {
        showCurrentFunctionName()

        showSubFunctionName("Run coroutineScope")
        runBlocking { // scope 1
            launch { // coroutine 1
                delay(200L)
                println("Task from runBlocking") // line code 1
            }
            coroutineScope { // coroutine 2 // scope 2
                launch { // coroutine 3
                    delay(500L)
                    println("Task from nested launch") // line code 2
                }
                delay(100L)
                println("Task from coroutine scope") // line code 3
            }
            println("Coroutine scope is over") // line code 4
        }

        showSubFunctionName("Run GlobalScope")
        runBlocking {
            val request = launch {
                // it spawns two other jobs, one with GlobalScope
                GlobalScope.launch {
                    println("job1: GlobalScope and execute independently!")
                    delay(1000)
                    println("job1: I am not affected by cancellation") // line code 1 này vẫn được in ra mặc dù bị delay 1
                }
                // and the other inherits the parent context
                launch {
                    delay(100)
                    println("job2: I am a child of the request coroutine")
                    delay(1000)
                    println("job2: I will not execute this line if my parent request is cancelled")
                }
            }
            delay(500)
            request.cancel() // cancel processing of the request
            delay(1000) // delay a second to see what happens
            println("main: Who has survived request cancellation?")
        }
    }

    /** launch{} gặp Exception thì throw luôn, còn async{} khi gặp Exception thì nó đóng gói Exception đó
     *  vào biến deferred. Chỉ khi biến deferred này gọi hàm await() thì Exception mới được throw ra.
     * */
    @OptIn(DelicateCoroutinesApi::class)
    fun exceptionInCoroutine07() {
        showCurrentFunctionName()

        showSubFunctionName("understand Coroutine exception")
        runBlocking {
            GlobalScope.launch {
                println("Throwing exception from launch")
                throw IndexOutOfBoundsException()
                println("Unreached")
            }

            val deferred = GlobalScope.async {
                println("Throwing exception from async")
                throw ArithmeticException()
                println("Unreached")
            }
//            deferred.await() // ArithmeticException đã được throw ra khi gặp hàm await()
        }

        showSubFunctionName("CoroutineExceptionHandler")
        /** CoroutineExceptionHandler được sử dụng như một generic catch block của tất cả coroutine.
         * Exception nếu xảy ra sẽ được bắt và trả về cho một hàm callback là override
         * fun handleException(context: CoroutineContext, exception: Throwable) và chúng ta sẽ dễ dàng log hoặc handle exception trong hàm đó.
         * */
        runBlocking {
            val handler = CoroutineExceptionHandler { _, exception ->
                println("Caught $exception")
            }
            val job = GlobalScope.launch(handler) {
                throw AssertionError()
            }
            val deferred = GlobalScope.async(handler) {
                throw ArithmeticException() // Nothing will be printed, relying on user to call deferred.await()
                println("Unreached")
            }
            joinAll(job, deferred)

            /** Chúng ta thấy AssertionError trong khối launch{} đã bị catch và được print ra. Vì chúng ta không gọi deferred.await()
             * nên ArithmeticException trong khối async{} sẽ không xảy ra. Mà cho dù chúng ta có gọi deferred.await()
             * thì CoroutineExceptionHandler cũng sẽ không catch được Exception này vì CoroutineExceptionHandler không thể
             * catch được những Exception được đóng gói vào biến Deferred. Vậy nên bạn phải tự catch Exception.
             * Và thêm một chú ý nữa là CoroutineExceptionHandler cũng không thể catch Exception xảy ra trong khối runBlocking{}
             * */
            try {
                deferred.await()
                println("Unreached AFTER try-catch")
            } catch (e: ArithmeticException) {
                println("Caught ArithmeticException AFTER try-catch")
            }
        }

    }

    /** The CoroutineStart is the mode in which you can start a coroutine. Options are:
     * • DEFAULT: Immediately schedules a coroutine for execution according to its context.
     * • LAZY: Starts coroutine lazily.
     * • ATOMIC: Same as DEFAULT but cannot be cancelled before it starts.
     * • UNDISPATCHED: Runs the coroutine until its first suspension point.
     * */
    @OptIn(DelicateCoroutinesApi::class)
    fun dependentJobsInAction08(){
        showCurrentFunctionName()

        showSubFunctionName("Dependent Jobs in Action")
        val job1 = GlobalScope.launch(start = CoroutineStart.LAZY) {
            delay(200)
            println("Pong")
            delay(200)
        }
        GlobalScope.launch {
            delay(200)
            println("Ping")
            job1.join() // start the LAZY job
            println("Ping")
            delay(200)
        }
        Thread.sleep(1000)

        showSubFunctionName("Managing Job Hierarchy")
        with(GlobalScope) {
            val parentJob = launch {
                delay(200)
                println("I’m the parent")
                delay(200)
            }
            launch(context = parentJob) {
                delay(200)
                println("I’m a child")
                delay(200)
            }
            if (parentJob.children.iterator().hasNext()) {
                println("The Job has children!")
            } else {
                println("The Job has NO children")
            }
            Thread.sleep(1000)
        }
    }
}