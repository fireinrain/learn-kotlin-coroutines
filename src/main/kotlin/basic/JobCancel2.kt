package basic

import kotlinx.coroutines.*

/**
@Description:
@Author  : fireinrain
@Site    : https://github.com/fireinrain
@File    : JobCancel2
@Software: IntelliJ IDEA
@Time    : 2022/7/31 12:41 PM
 */

fun main(args: Array<String>) = runBlocking {
    val startTime = System.currentTimeMillis()
    val job = launch(Dispatchers.Default) {
        var nextPrintTime = startTime
        var i = 0
        // CoroutineScope 的isActive 也是可以达到效果的
        while (i < 5 && this.isActive) {
            //确保在job没有cancel的时候正常执行
            // 如果被cancel了 job就会被停止 不参与计算
            // ensureActive()

            //yield() 可以达到同样的效果

            if (System.currentTimeMillis() >= nextPrintTime) {
                println("job: hello ${i++} ...")
                nextPrintTime += 500L
            }
        }
    }

    delay(1300L)
    println("main: ready to cancel job")
    job.cancelAndJoin()
    println("main: job has caneled")

}