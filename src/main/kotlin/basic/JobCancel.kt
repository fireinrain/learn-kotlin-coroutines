package basic

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/**
@Description:
@Author  : fireinrain
@Site    : https://github.com/fireinrain
@File    : JobCancel
@Software: IntelliJ IDEA
@Time    : 2022/7/31 12:34 PM
 */

fun main(args: Array<String>) {
    runBlocking {
        val job = launch {
            repeat(1000) { i ->
                println("job: test $i ...")
                delay(500L)
            }
        }
        delay(1300L)
        println("main: ready to cancel job...")
        // job.cancel()
        // job.join()

        // job.cancelAndJoin()

        // 以下无法取消job
        // job.join()
        // job.cancel()
        println("main: job has been canceled")


    }
}