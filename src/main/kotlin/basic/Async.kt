package basic

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking

/**
@Description:
@Author  : fireinrain
@Site    : https://github.com/fireinrain
@File    : Async
@Software: IntelliJ IDEA
@Time    : 2022/7/28 8:12 PM
 */

// async开启协程, 返回Deferred<T>, Deferred<T>是Job的子类, 有一个await()函数, 可以返回协程的结果.
fun main(args: Array<String>) = runBlocking {
    var result = GlobalScope.async {
        val addBigNumber = addBigNumber(1, 3)
        addBigNumber
    }
    println("waiting result from addBigNumber: ${Thread.currentThread().getName()}")
    println("result is: ${result.await()}")


}

suspend fun addBigNumber(a: Int, b: Int): Int {
    println("loading..." + Thread.currentThread().name)
    delay(3000L)
    // suspend @coroutine#2
    println("loaded!" + Thread.currentThread().name)
    return a + b
}