import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

/**
@Description:
@Author  : fireinrain
@Site    : https://github.com/fireinrain
@File    : Main
@Software: IntelliJ IDEA
@Time    : 2022/7/28 12:25 PM
 */

suspend fun main(args: Array<String>) {
    // runBlocking {
    //     delay(10000)
    // }

    withContext(Dispatchers.IO) {
        delay(10000)
    }

    println("end")


}