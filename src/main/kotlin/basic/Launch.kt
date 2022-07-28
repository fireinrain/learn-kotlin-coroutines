package basic

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/**
@Description:
@Author  : fireinrain
@Site    : https://github.com/fireinrain
@File    : Launch
@Software: IntelliJ IDEA
@Time    : 2022/7/28 8:09 PM
 */

fun main(args: Array<String>) = runBlocking {
    // launch返回Job, 代表一个协程, 我们可以用Job的join()方法来显式地等待这个协程结束:

    var job = GlobalScope.launch {
        delay(1000)
        println("job current scope: ${Thread.currentThread().getName()}")
    }

    println("now current thread: ${Thread.currentThread().getName()}")
    job.join()

}