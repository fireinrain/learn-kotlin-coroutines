package network

import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request

/**
@Description:
@Author  : fireinrain
@Site    : https://github.com/fireinrain
@File    : OkhttpTest
@Software: IntelliJ IDEA
@Time    : 2022/7/28 9:23 PM
 */

// 并发使用coroutines 请求baidu.com
class OkhttpTest {

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val okhttpTest = OkhttpTest()
            val mutableListOf = mutableListOf<Deferred<String?>>()
            val coroutineScope = CoroutineScope(Dispatchers.IO)
            val job = coroutineScope.launch {
                for (i in 1..10000) {
                    val fetchBaidu = okhttpTest.fetchBaidu(coroutineScope)
                    mutableListOf.add(fetchBaidu)
                }
            }

            runBlocking {
                job.join()
                mutableListOf.forEach { println(it.await()) }
            }

            println("result finish: ${mutableListOf.size}")

        }

    }


    suspend fun fetchBaidu(coroutineScope: CoroutineScope): Deferred<String?> {
        val deferred = coroutineScope.async {
            val url = Request.Builder().url("https://baidu.com").get().build()
            val okHttpClient = OkHttpClient()
            val respStr = okHttpClient.newCall(url).execute().body?.string()
            respStr
        }
        return deferred
    }
}