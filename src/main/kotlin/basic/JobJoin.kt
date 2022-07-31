package basic

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/**
@Description:
@Author  : fireinrain
@Site    : https://github.com/fireinrain
@File    : Join
@Software: IntelliJ IDEA
@Time    : 2022/7/31 12:24 PM
 */

fun main(args: Array<String>) {
    runBlocking {
        launch {
            delay(100)
            println("hello")
            delay(3000)
            println("world")
        }

        println("test1")
        println("test2")

        println("--------------------")
        testJoin()
    }
}

suspend fun testJoin() = runBlocking {
    val job = launch {
        delay(100)
        println("hello2")
        delay(3000)
        println("world2")
    }
    println("test12")
    job.join()
    println("test22")

}