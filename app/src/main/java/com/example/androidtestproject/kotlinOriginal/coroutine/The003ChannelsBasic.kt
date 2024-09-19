// Refer document: "Cùng học Kotlin Coroutine_12 Parts.pdf"

package com.example.androidtestproject.kotlinOriginal.coroutine

import com.example.androidtestproject.kotlinOriginal.ClassHelper.showCurrentFunctionName
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

object The003ChannelsBasic {

    /**! Channels khá giống với Flow nó cũng giúp chúng ta transfer một luồng giá trị (stream of values).
     * Channels khá giống với BlockingQueue, tức là nó cũng hoạt động như một Queue (hàng đợi) là FIFO (First In First Out)
     * Điểm khác nhau ở đây là BlockingQueue nó sử dụng 2 hàm put (thêm vào queue) và hàm take (lấy từ queue ra) và 2 hàm này là chạy blocking
     * còn Channels sử dụng 2 hàm send (tương tự hàm put) và receive (tương tự hàm take) và 2 hàm này là suspend function
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
            println("Done! Channel is empty?: ${channel.isEmpty} / Coroutine is completed?: ${job.isCompleted} / Coroutine")
        }
    }
}