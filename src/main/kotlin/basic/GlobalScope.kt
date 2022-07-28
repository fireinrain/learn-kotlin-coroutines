package basic

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

/**
@Description:
@Author  : fireinrain
@Site    : https://github.com/fireinrain
@File    : Basic1
@Software: IntelliJ IDEA
@Time    : 2022/7/28 7:56 PM
 */

fun main(args: Array<String>) {
    // 在全局scope下得协程不能取消
    GlobalScope.launch {
        delay(1000L)
        println("hello---${Thread.currentThread().getName()}")
    }
    println("world---${Thread.currentThread().getName()}")
    TimeUnit.SECONDS.sleep(2)
}

//suspend方法只能在协程或者另一个suspend方法中被调用.
suspend fun compute() {
    delay(2000L)
    println("finish computing....")
}