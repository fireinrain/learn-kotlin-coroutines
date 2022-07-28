package basic

import kotlinx.coroutines.*

/**
@Description:
@Author  : fireinrain
@Site    : https://github.com/fireinrain
@File    : Dispatchers
@Software: IntelliJ IDEA
@Time    : 2022/7/28 8:19 PM
 */
// Context中的CoroutineDispatcher可以指定协程运行在什么线程上. 可以是一个指定的线程, 线程池, 或者不限.


fun main(args: Array<String>) = runBlocking<Unit> {
    launch {
        println("main runBlocking      : I'm working in thread ${Thread.currentThread().name}")
    }

    // 没有安卓环境无法使用
    // launch (Dispatchers.Main){
    //     println("main runBlocking      : I'm working in thread ${Thread.currentThread().name}")
    // }

    launch(Dispatchers.Unconfined) {
        println("Unconfined            : I'm working in thread ${Thread.currentThread().name}")

    }

    launch(Dispatchers.Default) {
        // will get dispatched to DefaultDispatcher
        println("Default               : I'm working in thread ${Thread.currentThread().name}")
    }
    launch(newSingleThreadContext("MyOwnThread")) {
        // will get its own new thread
        println("newSingleThreadContext: I'm working in thread ${Thread.currentThread().name}")
    }

    //切换线程还可以用withContext, 可以在指定的协程context下运行代码, 挂起直到它结束, 返回结果. 另一种方式是新启一个协程, 然后用join明确地挂起等待.
    val add1 = add1(1, 2)
    val add2 = add2(3, 4)
    println("main thread: ${add1 + add2}")


}


suspend fun add1(a: Int, b: Int): Int {
    withContext(Dispatchers.Default) {
        delay(2000L)
    }
    return a + b
}

suspend fun add2(a: Int, b: Int): Int {
    withContext(Dispatchers.IO) {
        delay(1000L)
    }
    return a + b
}

//因为launch是一个扩展方法, 所以上面例子中默认的receiver是this.
// 这个例子中launch所启动的协程被称作外部协程(runBlocking启动的协程)的child. 这种"parent-child"的关系通过scope传递: child在parent的scope中启动.
// 协程的父子关系:
//
// 当一个协程在另一个协程的scope中被启动时, 自动继承其context, 并且新协程的Job会作为父协程Job的child.
//
// 所以, 关于scope目前有两个关键知识点:
//
// 我们开启一个协程的时候, 总是在一个CoroutineScope里.
// Scope用来管理不同协程之间的父子关系和结构.
//
// 协程的父子关系有以下两个特性:
//
// 父协程被取消时, 所有的子协程都被取消.
// 父协程永远会等待所有的子协程结束.
//
// 值得注意的是, 也可以不启动协程就创建一个新的scope. 创建scope可以用工厂方法: MainScope()或CoroutineScope().
// coroutineScope()方法也可以创建scope. 当我们需要以结构化的方式在suspend函数内部启动新的协程, 我们创建的新的scope, 自动成为suspend函数被调用的外部scope的child.
// 所以上面的父子关系, 可以进一步抽象到, 没有parent协程, 由scope来管理其中所有的子协程.
// (注意: 实际上scope会提供默认job, cancel操作是由scope中的job支持的.)
// Scope在实际应用中解决什么问题呢? 如果我们的应用中, 有一个对象是有自己的生命周期的, 但是这个对象又不是协程, 比如Android应用中的Activity, 其中启动了一些协程来做异步操作, 更新数据等, 当Activity被销毁的时候需要取消所有的协程, 来避免内存泄漏. 我们就可以利用CoroutineScope来做这件事: 创建一个CoroutineScope对象和activity的生命周期绑定, 或者让activity实现CoroutineScope接口.
// 所以, scope的主要作用就是记录所有的协程, 并且可以取消它们.
// A CoroutineScope keeps track of all your coroutines, and it can cancel all of the coroutines started in
//


// Structured Concurrency
// 这种利用scope将协程结构化组织起来的机制, 被称为"structured concurrency".
// 好处是:
//
// scope自动负责子协程, 子协程的生命和scope绑定.
// scope可以自动取消所有的子协程.
// scope自动等待所有的子协程结束. 如果scope和一个parent协程绑定, 父协程会等待这个scope中所有的子协程完成.
//
// 通过这种结构化的并发模式: 我们可以在创建top级别的协程时, 指定主要的context一次, 所有嵌套的协程会自动继承这个context, 只在有需要的时候进行修改即可.
// GlobalScope: daemon
// GlobalScope启动的协程都是独立的, 它们的生命只受到application的限制. 即GlobalScope启动的协程没有parent, 和它被启动时所在的外部的scope没有关系.
// launch(Dispatchers.Default) { ... }和GlobalScope.launch { ... }用的dispatcher是一样的.
// GlobalScope启动的协程并不会保持进程活跃. 它们就像daemon threads(守护线程)一样, 如果JVM发现没有其他一般的线程, 就会关闭.
//

// Key takeaways
//
// Coroutine协程机制: suspend, resume, 简化回调代码.
// suspend方法.
// 启动协程的几种方法.
// Dispatcher指定线程.
// Structured Concurrency: 依靠scope来架构化管理协程.
//


