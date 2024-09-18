package com.example.androidtestproject.kotlinOriginal

object ClassHelper {

    fun showCurrentFunctionName() {
        println(">> Function Testing: ${Thread.currentThread().stackTrace[2].methodName} <<")
    }

    fun showSubFunctionName(string: String) {
        println("\n> $string")
    }
}