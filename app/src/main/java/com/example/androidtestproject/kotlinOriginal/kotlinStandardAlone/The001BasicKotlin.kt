package com.example.androidtestproject.kotlinOriginal.kotlinStandardAlone

import com.example.androidtestproject.kotlinOriginal.ClassHelper.showCurrentFunctionName
import com.example.androidtestproject.kotlinOriginal.ClassHelper.showSubFunctionName

object The001BasicKotlin {

    /**
     * Higher-order function là một hàm có thể nhận một hoặc nhiều hàm khác làm đối số hoặc trả về một hàm.
     * Higher-order function là một khái niệm quan trọng trong lập trình hàm (functional programming)
     * */
    fun higherOrderFunction01() {
        showCurrentFunctionName()

        showSubFunctionName("Hàm nhận hàm khác làm đối số")
        val numbers = listOf(1, 2, 3, 4, 5)
        numbers.forEach { number -> println(number) }

        showSubFunctionName("Hàm trả về một hàm khác")
        fun getGreetingFunction(name: String): () -> String {
            return { "Hello, $name" }
        }
        val greetingFunction = getGreetingFunction("Alice")
        val greeting = greetingFunction() // Prints "Hello, Alice"
        println(greeting)

        showSubFunctionName("Higher-order functions are functions that can accept other functions as parameters and/or return functions as results.")
        fun operation(x: Int, y: Int, op: (Int, Int) -> Int): Int {
            return op(x, y)
        }

        val resultAdd = operation(5, 3) { a, b -> a + b } // outside the parent
        val resultSubtract = operation(5, 3, {a, b -> a - b}) // inside the parent
        println("Result of addition: $resultAdd")        // Output: Result of addition: 8
        println("Result of subtraction: $resultSubtract") // Output: Result of subtraction: 2


    }

    /** Delegate trong Kotlin là một cơ chế cho phép chúng ta tự động chuyển tiếp (delegate) các phương thức
     * gọi từ một đối tượng đến một đối tượng khác. Kotlin hỗ trợ delegation thông qua từ khóa "by".
     * Có hai loại delegation chính trong Kotlin: Class Delegation và Property Delegation.
     * - Class Delegation cho phép chúng ta sử dụng một đối tượng nhất định để
     * cung cấp một phần hoặc toàn bộ chức năng của một lớp khác,
     * - Property Delegation cho phép chúng ta tạo ra các thuộc tính có logic phức tạp mà không cần
     * phải viết lại logic đó ở mỗi nơi thuộc tính được sử dụng.
     * */
    fun delegate02() {
        showCurrentFunctionName()
        val consolePrinter = ConsolePrinter()
        val document = Document(consolePrinter)
        document.printDocument("Hello, world!")
    }

    private interface Printer {
        fun print(text: String)
    }

    private class ConsolePrinter : Printer {
        override fun print(text: String) {
            println("Printing: $text")
        }
    }

    private class Document(private val printer: Printer) : Printer by printer {
        fun printDocument(text: String) {
            // Đối tượng Document delegate tất cả các cuộc gọi phương thức của Printer cho đối tượng printer
            printer.print(text)
        }
    }


    // Biến top-level
    val PI = 3.14
    /** Trong Kotlin, không có từ khóa static giống như trong Java. Thay vào đó:
     *  - Top-level declarations: Khai báo các biến, hàm, class bên ngoài class hoặc object.
     *  Các khai báo này có thể được truy cập trực tiếp mà không cần khởi tạo đối tượng.
     *  - Companion objects: Đây là một đối tượng đồng hành (companion) được khai báo bên trong một
     *  class và có thể chứa các thuộc tính và phương thức tĩnh. Các thành viên của companion object
     *  có thể được truy cập trực tiếp từ tên class mà không cần khởi tạo đối tượng.
     *  - Object declarations: Khai báo một đối tượng đơn lẻ (singleton) mà có thể chứa các thuộc tính và phương thức tĩnh.
     *  Các thành viên của object có thể được truy cập trực tiếp từ tên đối tượng đơn lẻ đó.
     * */
    fun notStatic03() {
        showCurrentFunctionName()

        showSubFunctionName("Top-level declarations")
        // Hàm top-level
        fun calculateArea(radius: Double): Double {
            return PI * radius * radius
        }
        println("Area: ${calculateArea(5.0)}")

        showSubFunctionName("Companion objects")
        // Truy cập thành viên của companion object
        val areaClass = Circle.calculateArea(5.0)
        println("Area: $areaClass")

        showSubFunctionName("Object declarations")
        // Truy cập thành viên của object
        val areaObject = Math.calculateArea(5.0)
        println("Area: $areaObject")
    }

    private class Circle {
        companion object {
            val PI = 3.14

            fun calculateArea(radius: Double): Double {
                return PI * radius * radius
            }
        }
    }

    private object Math {
        val PI = 3.14

        fun calculateArea(radius: Double): Double {
            return PI * radius * radius
        }
    }

    /** The primary purpose of sealed classes is to enable exhaustive pattern matching,
     * especially in combination with the when expression. */
    fun sealClassInKotlin04() {
        showCurrentFunctionName()
    }

    // Style 1
    sealed class Result
    class Success(val data: String) : Result()
    class Error(val message: String) : Result()
    // Style 2
    sealed class Errors(val message: String) {
        class NetworkError : Errors("Network failure")
        class DatabaseError : Errors("Database cannot be reached")
        class UnknownError : Errors("An unknown error has occurred")
    }
}