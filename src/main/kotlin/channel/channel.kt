package channel

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/**
@Description:
@Author  : fireinrain
@Site    : https://github.com/fireinrain
@File    : channel
@Software: IntelliJ IDEA
@Time    : 2022/8/6 9:08 PM
 */

fun main(args: Array<String>) = runBlocking {
    val channel = Channel<Int>()
    launch(Dispatchers.IO) {
        for (i in 0 until 10) {
            channel.send(i)
        }
    }

    repeat(5) {
        println(channel.receive())
    }

    println("done")


}