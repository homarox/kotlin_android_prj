import kotlin.system.measureTimeMillis

fun main(args: Array<String>) {
    // Input
    val input1 = " "
    val input2 = 5
    val input3 = intArrayOf(1, 3, 5, 4, 7)
    // val input = intArrayOf(2,7,11,15) val input =
    // intArrayOf(-3,0,1,-3,1,1,1,-3,10,0) val input = "a1" Return result

    var result = ""
    val time = measureTimeMillis {
        result = minSubArrayLen(7, intArrayOf(2, 3, 1, 2, 4, 3)).toString()
    }

    println("========RESULT========\nResult: $result\nTime: $time")
}

/* 209. Minimum Size Subarray Sum [Sliding Window]
Example: minSubArrayLen(7, intArrayOf(2, 3, 1, 2, 4, 3))
*/
fun minSubArrayLen(target: Int, nums: IntArray): Int {
    var minLength = Int.MAX_VALUE
    for (start in nums.indices) {
        var sum = 0
        for (next in start..<nums.size) {
            sum += nums[next]
            if (sum >= target) {
                minLength = Math.min(minLength, next - start + 1)
                break
            }
        }
    }
    return minLength
}

/* 3. Longest Substring Without Repeating Characters [Sliding Window]
[0] Runtime 688ms Beats 5.05% - Memory 42.52MB Beats 5.11% of users with Kotlin
[1] Runtime 195ms Beats 77.22% - Memory 37.00MB Beats 86.15% of users with Kotlin
*/
fun lengthOfLongestSubstring(s: String): Int {
    var maxLength = 0
    var tempCharList: MutableList<String> = arrayListOf()
    for (start in s.split("").indices) {
        for (next in start..<s.length) {
            if (s[next].isWhitespace()) {
                if ("/s" in tempCharList) {
                    if (tempCharList.size > maxLength) {
                        maxLength = tempCharList.size
                        break
                    }
                } else {
                    tempCharList.add("/s")
                }
            } else if (s[next].toString() in tempCharList) {
                if (tempCharList.size > maxLength) {
                    maxLength = tempCharList.size
                }
                tempCharList = arrayListOf()
                break
            } else {
                tempCharList.add(s[next].toString())
            }
        }
    }
    if (tempCharList.size > maxLength) {
        maxLength = tempCharList.size
    }
    return maxLength
}

fun lengthOfLongestSubstring_1(s: String): Int {
    if (s == "") {
        return 0
    }
    val arr = arrayListOf<Char>()
    var max = 1
    var k: Int
    for (i in s.indices) {
        if (s[i] !in arr) {
            arr.add(s[i]) // add to array char
            if (arr.size > max) {
                max = arr.size
            }
        } else {
            k = arr.indexOf(s[i]) // deleting first k symbols
            arr.add(s[i]) // adding for future..
            while (k >= 0) {
                arr.removeAt(k)
                k--
            }
        }
    }
    return max
}

/* 66. Plus One
Runtime 165ms beats 56.86%of users with Kotlin - Memory 34.50MB beats 88.26%of users with Kotlin
*/
fun plusOne(digits: IntArray): String {
    val n: Int = digits.size
    for (i in n - 1 downTo 0) {
        if (digits[i] < 9) {
            digits[i]++
            return digits.joinToString("")
        }
        digits[i] = 0
    }

    val newNumber = IntArray(n + 1)
    newNumber[0] = 1

    return newNumber.joinToString("")
}

/* 412. Fizz Buzz
Runtime: 370 ms, faster than 23.41%. Memory Usage: 43.7 MB, less than 26.50%.
*/
fun fizzBuzz(n: Int): List<String> {
    val result = mutableListOf<String>()
    for (index in 1..n) {
        when {
            (index % 3 == 0 && index % 5 == 0) -> result.add("FizzBuzz")
            (index % 3 == 0) -> result.add("Fizz")
            (index % 5 == 0) -> result.add("Buzz")
            else -> result.add(index.toString())
        }
    }
    return result
}

/* 191. Number of 1 Bits
*/
fun hammingWeight(n: Int): Int {
    var m = n
    var ans = 0
    while (m != 0) {
        m = m and m - 1
        ans++
    }
    return ans
}

/* 674. Longest Continuous Increasing Subsequence
Runtime: 293 ms, faster than 58.82% - Memory Usage: 44.5 MB, less than 14.71%
*/
fun findLengthOfLCIS(nums: IntArray): Int {
    if (nums.isEmpty())
        return 0

    var longest = 0
    var max = Int.MIN_VALUE

    for (i in nums.indices) {
        if (i == 0 || nums[i] > nums[i - 1]) {
            longest++
            max = kotlin.math.max(longest, max)
        } else
            longest = 1
    }

    return max
}

/* 125. Valid Palindrome
*/
// ! Not support in Vscode
fun isPalindrome(s: String): Boolean {
    val sb = StringBuilder()
    // non-alphanumeric
    for (char in s) {
        if (char in 'a'..<'z' || char in 'A'..<'Z' || char in '0'..<'9') {
            sb.append(char)
        }
    }

    val final = sb
        .toString()
        .lowercase()
    println("final: $final")

    var j = final.lastIndex
    // reads the same forward and backward.
    for (i in final.indices) {
        if (final[i] != final[j]) {
            return false
        } else {
            j--
        }
    }

    return true
}

/* 4. Median of Two Sorted Arrays
*/
fun findMedianSortedArrays(nums1: IntArray, nums2: IntArray): Double {
    val sort = nums1
        .plus(nums2)
        .sorted() /// string.plus(string)
    val mid = sort.size / 2
    return if (sort.size % 2 == 0)
        (sort[mid] + sort[mid - 1]).toDouble() / 2
    else
        sort[mid].toDouble()
}

/* 9. Palindrome Number
Runtime: 400 ms, faster than 57.27% - Memory Usage: 42.2 MB, less than 18.65%
*/
fun isPalindrome(x: Int): Boolean {
    val xString = x.toString()
    val arrayCheck = mutableListOf<Char>()

    if (x < 0) {
        return false
    } else if (x in 0..9) {
        return true
    } else {
        val mid = if (xString.length % 2 == 1)
            (xString.length / 2 + 1)
        else
            xString.length / 2
        println(
            "x.size = ${xString.length} -- x.size/2 = ${xString.length / 2} -- mid = $mid"
        )

        for (i in xString.indices) {
            if (i < xString.length / 2) {
                arrayCheck.add(xString[i])
            } else if (i >= mid) {
                println(
                    "arrayCheck= $arrayCheck -- takeLast = ${arrayCheck.last()} -- xString[i] = ${xString[i]}"
                )
                if (arrayCheck.last() != xString[i]) {
                    return false
                } else {
                    // arrayCheck.removeLast()
                    arrayCheck.removeAt(arrayCheck.size - 1)
                }
            }
        }
        return true
    }
}

/* 1. Two Sum
Runtime: 268 ms, faster than 42.07% - Memory Usage: 37.6 MB, less than 61.05%
*/
// ! Not support in Vscode
fun twoSum(nums: IntArray, target: Int): IntArray {
    // Solution 1    val indexedList = nums.withIndex().toList() indexedList.forEach
    // {(indexA, valueA) ->        val subList = indexedList.subList(indexA +1,
    // nums.size)        subList.forEach { (indexB, valueB) ->            if (valueA
    // + valueB == target) {                return intArrayOf(indexA, indexB) } }
    // }
    //
    //    return intArrayOf() My Solution
    var result: IntArray = intArrayOf(0, 0)
    loop@ for (item in 0..<nums.size - 1) {
        for (subItem in item + 1..<nums.size) {
            var check = nums[item] + nums[subItem]
            if (check == target) {
                result[0] = item
                result[1] = subItem
                break@loop
            }
        }
    }

    return result
}

/* 1207. Unique Number of Occurrences
Runtime: 176 ms, faster than 44.44% - Memory Usage: 36.1 MB, less than 5.56%
*/
fun uniqueOccurrences(arr: IntArray): Boolean {
    var checkArray: MutableList<Int> = mutableListOf() /// mutableList
    var previous = arr.sorted()[0]
    val arrSort = arr
        .sorted()
        .drop(1)
    var count = 1
    var result = true

    if (arr.size == 2) {
        if (arr[0] != arr[1]) {
            result = false
        }
    } else {
        for (index in arrSort) {
            if (index == previous) {
                count++
            } else { // reset
                previous = index
                // println(count) println("P: " + previous) add to checkArray
                if (count in checkArray) {
                    result = false
                    break
                } else {
                    checkArray.add(count)
                }
                count = 1
            }
        }
        if (count in checkArray) { // check missing last count
            result = false
        }
    }
    return result
}

/* 1812. Determine Color of a Chessboard Square
Runtime: 132 ms, faster than 93.55% | Memory Usage: 33.5 MB, less than 64.52%
*/
fun squareIsWhite(coordinates: String): Boolean {
    val firstChar = coordinates[0].toString()
    val secondChar = coordinates[1]
        .toString()
        .toInt()

    val result = when (firstChar) {
        "a",
        "c",
        "e",
        "g" -> (1 + secondChar)

        else -> (2 + secondChar)
    }

    return result % 2 != 0
}

/* 1752. Check if Array Is Sorted and Rotated
Runtime: 148 ms, faster than 100.00% Memory Usage: 33.7 MB, less than 77.78%
*/
fun check(nums: IntArray): Boolean {
    var result = false
    var count = 0
    var previousNo = nums[0]

    // first check
    for (i in nums) {
        if (i >= previousNo) {
            if (count > 1)
                break
        } else {
            count++
        }
        previousNo = i
        // println(previousNo)
    }

    // second check if count = 1
    if (count == 1) {
        if (previousNo <= nums[0]) {
            result = true
        }
    } else if (count == 0) {
        result = true
    }

    return result
}

/* 231. Power of Two
Runtime: 144 ms, faster than 26.47% - Memory Usage: 33.5MB, less than 5.88%
*/
fun isPowerOfTwo(n: Int): Boolean {
    var result = false
    var checkNo = n

    if (checkNo == 1) {
        result = true
    } else {
        while (checkNo >= 2) {
            if (checkNo % 2 == 0) {
                // result = if (checkNo == 3) true else false//Ternary Conditional
                if (checkNo == 2) {
                    result = true
                    break
                } else {
                    checkNo /= 2
                }
            } else {
                break
            }
        }
    }

    return result
}

/* 824 - Goat Latin
Runtime: 184 ms, faster than 79.17% - Memory Usage: 36 MB, less than 83.33%
*/
fun toGoatLatin(sentence: String): String {
    var sentenceArray = sentence.split(" ")
    var vowel = arrayListOf("o", "a", "e", "i", "u", "O", "A", "E", "I", "U")
    var result = ""
    var count = 1

    for (i in sentenceArray) {
        var newWord = i
        if (i[0].toString() in vowel) {
            newWord += "ma"
        } else {
            newWord = newWord.drop(1) + i[0] + "ma"
        }
        newWord += "a".repeat(count)
        count++
        result = "$result$newWord "
    }
    return result.dropLast(1)
}

/* 1446. Consecutive Characters
Runtime: 196 ms, faster than 15.38% - Memory Usage: 36.8 MB, less than 7.69%
*/
fun maxPower(s: String): Int {
    var count = 1
    var maxCount = 0
    val sArray: List<Char> = s.toList() // convert string to list
    // println(sArray) println(sArray.sorted()) sArray = sArray.sorted()  not
    // sorting in this case
    var currentChar: Char
    ? = null

    for (i in sArray) {
        if (i == currentChar) {
            // count char
            count++
        } else {
            currentChar = i
            // check max
            if (count > maxCount) {
                maxCount = count
            }
            count = 1
        }
    }

    // re-check max
    if (count > maxCount) {
        maxCount = count
    }

    return maxCount
}

/* 476. Number Complement
Runtime: 132 ms, faster than 50.00% - Memory Usage: 33.7 MB, less than 25.00%
*/
fun findComplement(num: Int): Int {
    var binaryCovert = num.toString(radix = 2) // convert int to binary
    var result = ""
    for (index in binaryCovert) {
        if (index == '0') {
            result += "1"
        } else {
            result += "0"
        }
    }

    return result.toInt(radix = 2)
}

/* 326. Power of Three
Runtime: 232 ms, faster than 75.66% | Memory Usage: 35.6MB, less than 23.81%
*/
fun isPowerOfThree(n: Int): Boolean {
    var result = false
    var checkNo = n

    if (checkNo == 1) {
        result = true
    } else {
        while (checkNo >= 3) {
            if (checkNo % 3 == 0) {
                // result = if (checkNo == 3) true else false//Ternary Conditional
                if (checkNo == 3) {
                    result = true
                    break
                } else {
                    checkNo /= 3
                }
            } else {
                break
            }
        }
    }
    return result
}
