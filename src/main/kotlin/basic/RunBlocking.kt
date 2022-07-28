package basic

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/**
@Description:
@Author  : fireinrain
@Site    : https://github.com/fireinrain
@File    : Basic2
@Software: IntelliJ IDEA
@Time    : 2022/7/28 8:04 PM
 */

fun main(args: Array<String>) = runBlocking {
    GlobalScope.launch {
        delay(1000)
        println("current thread: ${Thread.currentThread().getName()}")
    }

    println("current thread: ${Thread.currentThread().getName()}")

    delay(2000L)

}