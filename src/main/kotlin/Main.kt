import kotlinx.coroutines.*

/**
@Description:
@Author  : fireinrain
@Site    : https://github.com/fireinrain
@File    : Main
@Software: IntelliJ IDEA
@Time    : 2022/7/28 12:25 PM
 */

fun main(args: Array<String>) {
    runBlocking {
        val job = CoroutineScope(Dispatchers.IO).launch {
            delay(1000)
            println(Thread.currentThread().getName())
            val test = test()
            println(test)
        }
        job.join()
    }

    println("end")


}

suspend fun test(): String {
    withContext(Dispatchers.Unconfined) {
        delay(1000)
        println(Thread.currentThread().getName() + "    test")
    }
    return "test"
}