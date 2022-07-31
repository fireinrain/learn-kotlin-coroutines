# learn-kotlin-coroutines

learn basic concept of kotlin coroutines

kotlin version: 1.7.1
kotlin coroutines version: 1.6.4

一、协程的一些前置知识
1.1 进程和线程
1.1.1基本定义
进程
进程是一个具有一定独立功能的程序在一个数据集上的一次动态执行的过程，是操作系统进行资源分配和调度的一个独立单位，是应用程序运行的载体。
进程是资源分配的最小单位，在单核CPU中，同一时刻只有一个程序在内存中被CPU调用运行。

线程
基本的CPU执行单元，程序执行过程中的最小单元，由 线程ID、程序计数器、寄存器组合和堆栈 共同组成。
线程的引入减小了程序并发执行时的开销，提高了操作系统的并发性能。

1.1.2为什么要有线程
单个进程只能干一件事，进程中的代码依旧是串行执行。
执行过程如果堵塞，整个进程就会挂起，即使进程中某些工作不依赖于正在等待的资源，也不会执行。
多个进程间的内存无法共享，进程间通讯比较麻烦
1.1.3 进程与线程的区别
一个程序至少有一个进程，一个进程至少有一个线程，可以把进程理解做 线程的容器；
进程在执行过程中拥有 独立的内存单元，该进程里的多个线程 共享内存；
进程可以拓展到 多机，线程最多适合 多核；
每个独立线程有一个程序运行的入口、顺序执行列和程序出口，但不能独立运行，需依存于应用程序中，由应用程序提供多个线程执行控制；
「进程」是「资源分配」的最小单位，「线程」是 「CPU调度」的最小单位
进程和线程都是一个时间段的描述，是 CPU工作时间段的描述，只是颗粒大小不同。
1.2 协作式与抢占式
1.2.1 协作式
早期的操作系统采用的就是协作式多任务, 即：由进程主动让出执行权，如当前进程需等待IO操作，主动让出CPU，由系统调度下一个进程。
问题：

流氓应用进程一直占用cpu，不让出资源
某个进程程序健壮性较差，出现死循环、死锁等问题，导致整个系统瘫痪。
1.2.2 抢占式
操作系统决定执行权，操作系统具有从任何一个进程取走控制权和使另一个进程获得控制权的能力。系统公平合理地为每个进程分配时间片，进程用完就休眠，甚至时间片没用完，但有更紧急的事件要优先执行，也会强制让进程休眠。

有了进程设计的经验，线程也做成了抢占式多任务，但也带来了新的——线程安全问题，这个一般通过加锁的方式来解决，这里就不展开了。

1.3 协程
Go、Python 等很多变成语言在语言层面上都实现协程，java 也有三方库实现协程，只是不常用， Kotlin 在语言层面上实现协程，对比
java， 主要还是用来解决异步任务线程切换的痛点。

协程基于线程，但相对于线程轻量很多，可理解为在用户层模拟线程操作；
每创建一个协程，都有一个内核态线程动态绑定，用户态下实现调度、切换，真正执行任务的还是内核线程。
线程的上下文切换都需要内核参与，而协程的上下文切换，完全由用户去控制，避免了大量的中断参与，减少了线程上下文切换与调度消耗的资源。
线程是操作系统层面的概念，协程是语言层面的概念

线程与协程最大的区别在于：线程是被动挂起恢复，协程是主动挂起恢复。

一种非抢占式(协作式)的任务调度模式，程序可以主动挂起或者恢复执行。

本质上，协程是轻量级的线程。 —— kotlin 中文文档

我觉得这个概念有点模糊---------把人带入误区。后面再说。

"假"协程，Kotlin在语言级别并没有实现一种同步机制(锁)，还是依靠Kotlin-JVM的提供的Java关键字(如synchronized)，即锁的实现还是交给线程处理
因而Kotlin协程本质上只是一套基于原生Java线程池 的封装。
Kotlin 协程的核心竞争力在于：它能简化异步并发任务,以同步方式写异步代码。

二、 Kotlin 协程的基本使用
讲概念之前，先讲用法。

场景： 开启工作线程执行一段耗时任务，然后在主线程对结果进行处理。

常见的处理方式：

自己定义回调，进行处理

使用 线程/线程池， Callable
线程 Thread(FeatureTask(Callable)).start
线程池 submit(Callable)

Android: Handler、 AsyncTask、 Rxjava

使用协程：

coroutineScope.launch(Dispatchers.Main) { // 在主线程启动一个协程
val result = withContext(Dispatchers.Default) { // 切换到子线程执行
doSomething()  // 耗时任务
}
handResult(result)  // 切回到主线程执行
}
这里需要注意的是： Dispatchers.Main 是 Android 里面特有的，如果是java程序里面是用则会抛出异常。

2.1 创建协程的三种方式
使用 runBlocking 顶层函数创建：
runBlocking {
...
}
使用 GlobalScope 单例对象创建
GlobalScope.launch {
...
}
自行通过 CoroutineContext 创建一个 CoroutineScope 对象
val coroutineScope = CoroutineScope(context)
coroutineScope.launch {
...
}
方法一通常适用于单元测试的场景，而业务开发中不会用到这种方法，因为它是线程阻塞的。
方法二和使用 runBlocking 的区别在于不会阻塞线程。但在 Android 开发中同样不推荐这种用法，因为它的生命周期会只受整个应用程序的生命周期限制，且不能取消。
方法三是比较推荐的使用方法，我们可以通过 context 参数去管理和控制协程的生命周期（这里的 context 和 Android
里的不是一个东西，是一个更通用的概念，会有一个 Android 平台的封装来配合使用）。
2.2 等待一个作业
先看一个示例：

fun main() = runBlocking {
launch {
delay(100)
println("hello")
delay(300)
println("world")
}
println("test1")
println("test2")
}
执行结果如下：

test1
test2
hello
world
我们启动了一个协程之后，可以保持对它的引用，显示地等待它执行结束，注意这里的等待是非阻塞的，不会将当前线程挂起。

fun main() = runBlocking {
val job = launch {
delay(100)
println("hello")
delay(300)
println("world")
}
println("test1")
job.join()
println("test2")
}
输出结果:

test1
hello
world
test2
类比 java 线程，也有 join 方法。但是线程是操作系统界别的，在某些 cpu 上，可能 join 方法不生效。

2.3 协程的取消
与线程类比，java 线程其实没有提供任何机制来安全地终止线程。
Thread 类提供了一个方法 interrupt() 方法，用于中断线程的执行。调用interrupt（）方法并不意味着立即停止目标线程正在进行的工作，而只是传递了请求中断的消息。然后由线程在下一个合适的时机中断自己。

但是协程提供了一个 cancel() 方法来取消作业。

fun main() = runBlocking {
val job = launch {
repeat(1000) { i ->
println("job: test $i ...")
delay(500L)
}
}
delay(1300L) // 延迟一段时间
println("main: ready to cancel!")
job.cancel() // 取消该作业
job.join() // 等待作业执行结束
println("main: Now cancel.")
}
输出结果：

job: test 0 ...
job: test 1 ...
job: test 2 ...
main: ready to cancel!
main: Now cancel.
也可以使用函数 cancelAndJoin, 它合并了对 cancel 以及 join 的调用。

问题：
如果先调用 job.join() 后调用 job.cancel() 是是什么情况？

取消是协作的
协程并不是一定能取消，协程的取消是协作的。一段协程代码必须协作才能被取消。
所有 kotlinx.coroutines 中的挂起函数都是 可被取消的 。它们检查协程的取消， 并在取消时抛出 CancellationException。
如果协程正在执行计算任务，并且没有检查取消的话，那么它是不能被取消的。

fun main() = runBlocking {
val startTime = System.currentTimeMillis()
val job = launch(Dispatchers.Default) {
var nextPrintTime = startTime
var i = 0
while (i < 5) { // 一个执行计算的循环，只是为了占用 CPU
// 每秒打印消息两次
if (System.currentTimeMillis() >= nextPrintTime) {
println("job: hello ${i++} ...")
nextPrintTime += 500L
}
}
}
delay(1300L) // 等待一段时间
println("main: ready to cancel!")
job.cancelAndJoin() // 取消一个作业并且等待它结束
println("main: Now cancel.")
}
此时的打印结果：

job: hello 0 ...
job: hello 1 ...
job: hello 2 ...
main: ready to cancel!
job: hello 3 ...
job: hello 4 ...
main: Now cancel.
可见协程并没有被取消。为了能真正停止协程工作，我们需要定期检查协程是否处于 active 状态。

检查 job 状态
一种方法是在 while(i<5) 中添加检查协程状态的代码
代码如下：

while (i < 5 && isActive)
这样意味着只有当协程处于 active 状态时，我们工作的才会执行。

另一种方法使用协程标准库中的函数 ensureActive()， 它的实现是这样的：

public fun Job.ensureActive(): Unit {
if (!isActive) throw getCancellationException()
}
代码如下：

while (i < 5) { // 一个执行计算的循环，只是为了占用 CPU
ensureActive()
...
}
ensureActive() 在协程不在 active 状态时会立即抛出异常。

使用 yield()
yield() 和 ensureActive 使用方式一样。
yield 会进行的第一个工作就是检查任务是否完成，如果 Job 已经完成的话，就会抛出 CancellationException 来结束协程。yield
应该在定时检查中最先被调用。

while (i < 5) { // 一个执行计算的循环，只是为了占用 CPU
yield()
...
}
2.4 等待协程的执行的结果
对于无返回值的的协程使用 launch 函数创建，如果需要返回值，则通过 async 函数创建。
使用 async 方法启动 Deferred （也是一种 job）， 可以调用它的 await() 方法获取执行的结果。
形如下面代码：

val asyncDeferred = async {
...
}

val result = asyncDeferred.await()
deferred 也是可以取消的，对于已经取消的 deferred 调用 await() 方法，会抛出
JobCancellationException 异常。

同理，在 deferred.await 之后调用 deferred.cancel(), 那么什么都不会发生，因为任务已经结束了。

关于 async 的具体用法后面异步任务再讲。

2.5 协程的异常处理
由于协程被取消时会抛出 CancellationException ，所以我们可以把挂起函数包裹在 try/catch 代码块中，这样就可以在 finally
代码块中进行资源清理操作了。

fun main() = runBlocking {
val job = launch {
try {
delay(100)
println("try...")
} catch (e: Exception) {
println("exception: ${e.message}")
} finally {
println("finally...")
}
}
delay(50)
println("cancel")
job.cancel()
print("Done")
}
结果：

cancel
Doneexception: StandaloneCoroutine was cancelled
finally...
2.6 协程的超时
在实践中绝大多数取消一个协程的理由是它有可能超时。 当你手动追踪一个相关 Job 的引用并启动，使用 withTimeout 函数。

fun main() = runBlocking {
withTimeout(300) {
println("start...")
delay(100)
println("progress 1...")
delay(100)
println("progress 2...")
delay(100)
println("progress 3...")
delay(100)
println("progress 4...")
delay(100)
println("progress 5...")
println("end")
}
}
结果：

start...
progress 1...
progress 2...
Exception in thread "main" kotlinx.coroutines.TimeoutCancellationException: Timed out waiting for 300 ms

withTimeout 抛出了 TimeoutCancellationException，它是 CancellationException 的子类。 我们之前没有在控制台上看到堆栈跟踪信息的打印。这是因为在被取消的协程中
CancellationException 被认为是协程执行结束的正常原因。 然而，在这个示例中我们在 main 函数中正确地使用了
withTimeout。如果有必要，我们需要主动 catch 异常进行处理。

当然，还有另一种方式： 使用 withTimeoutOrNull。

withTimeout 是可以由返回值的，执行 withTimeout 函数，会阻塞并等待执行完返回结果或者超时抛出异常。withTimeoutOrNull 用法与
withTimeout 一样，只是在超时后返回 null 。

三、并发与挂起函数
3.1 使用 async 并发
考虑一个场景： 开启多个任务，并发执行，所有任务执行完之后，返回结果，再汇总结果继续往下执行。
针对这种场景，解决方案有很多，比如 java 的 FeatureTask， concurrent 包里面的 CountDownLatch、Semaphore, Rxjava 提供的 Zip
变换操作等。

前面提到有返回值的协程，我们通常使用 async 函数来启动。

这里看一段代码：

fun main() = runBlocking {
val time = measureTimeMillis {
val a = async(Dispatchers.IO) {
printWithThreadInfo()
delay(1000) // 模拟耗时操作
1
}
val b = async(Dispatchers.IO) {
printWithThreadInfo()
delay(2000) // 模拟耗时操作
2
}
printWithThreadInfo("${a.await() + b.await()}")
printWithThreadInfo("end")
}
printWithThreadInfo("time: $time")
}
执行结果：

thread id: 12, thread name: DefaultDispatcher-worker-1 --->
thread id: 14, thread name: DefaultDispatcher-worker-3 --->
thread id: 1, thread name: main ---> 3
thread id: 1, thread name: main ---> end
thread id: 1, thread name: main ---> time: 2051
async 启动一个协程后，调用 await 方法后，会阻塞，等待结果的返回，同样能达到效果。

3.2 惰性启动 async
async 可以通过将 start 参数设置为 CoroutineStart.LAZY 变成惰性的。在这个模式下，调用 await 获取协程执行结果的时候，或者调用
Job 的 start 方法时，协程才会启动。

fun main() = runBlocking {
val time = measureTimeMillis {
val a = async(Dispatchers.IO, CoroutineStart.LAZY) {
printWithThreadInfo()
delay(1000) // 模拟耗时操作
1
}
val b = async(Dispatchers.IO, CoroutineStart.LAZY) {
printWithThreadInfo()
delay(2000) // 模拟耗时操作
2
}
a.start()
b.start()
printWithThreadInfo("${a.await() + b.await()}")
printWithThreadInfo("end")
}
printWithThreadInfo("time: $time")
}
执行结果：

thread id: 14, thread name: DefaultDispatcher-worker-3 --->
thread id: 12, thread name: DefaultDispatcher-worker-1 --->
thread id: 1, thread name: main ---> 3
thread id: 1, thread name: main ---> end
thread id: 1, thread name: main ---> time: 2037
试想，如果没有显示调用 start() 方法，结果会怎样？

3.3 挂起函数
还是上面的例子，加入我们把任务 a 的计算过程提取成一个函数。如下：

fun main() = runBlocking {
val time = measureTimeMillis {
val a = async(Dispatchers.IO) {
calA()
}
val b = async(Dispatchers.IO) {
printWithThreadInfo()
delay(2000) // 模拟耗时操作
2
}
printWithThreadInfo("${a.await() + b.await()}")
printWithThreadInfo("end")
}
printWithThreadInfo("time: $time")
}

fun calA(): Int {
printWithThreadInfo()
delay(1000) // 模拟耗时操作
return 1
}
此时会发现，编译器报错了。

delay(1000) // 模拟耗时操作
该行报错为：Suspend function 'delay' should be called only from a coroutine or another suspend function
挂起函数 delay 应该在另一个挂起函数调用。

查看 delay 函数源码：

public suspend fun delay(timeMillis: Long) {
if (timeMillis <= 0) return // don't delay
return suspendCancellableCoroutine sc@ { cont: CancellableContinuation<Unit> ->
// if timeMillis == Long.MAX_VALUE then just wait forever like awaitCancellation, don't schedule.
if (timeMillis < Long.MAX_VALUE) {
cont.context.delay.scheduleResumeAfterDelay(timeMillis, cont)
}
}
}
可以看到，方法签名用 suspend 修饰，表示该函数是一个挂起函数。解决这个异常，只需要将我们定义的 calA() 方法也用 suspend
修饰，使其变成一个挂起函数。

使用 suspend 关键字修饰的函数成为挂起函数，挂起函数只能在另一个挂起函数，或者协程中被调用。在挂起函数中可以调用普通函数（非挂起函数）。

3.4 协程和挂起的本质
3.4.1 协程到底是什么
kotlin 中文文档中说，本质上，协程是轻量级的线程。我前面说，这个概念有点模糊，kotlin 协程的实现是借助线程，可以理解为对线程的一个封装框架。启动一个协程，使用
launch 或者 async 函数，启动的是函数中闭包代码块，好比启动一个线程，实现上是执行 run 方法中的代码，所以协程可以理解为是这个代码块。
协程的核心点就是函数或者一段程序能够被挂起，稍后再在挂起的位置恢复。

3.4.2 挂起是什么意思
那协程中挂起是什么意思？
suspend 翻译过来是，中断、暂停的意思。刚开始接触到这个概念的时候，觉得挂起，就是代码执行到这里停下来了，这是不对的。

我们在协程中应该理解为：当线程执行到协程的 suspend
函数的时候，暂时不继续执行协程代码了。这个挂起，是针对当前线程来说的，从当前线程挂起，就是这个协程从执行它的线程上脱离，并不是说协程停下来了，而是当前线程不再管这个协程要去做什么了。

当协程执行到挂起函数时，从当前线程脱离，然后继续执行，这个时候在哪个线程执行，由协程调度器所指定，挂起函数执行完之后，又会重新切回到它原先的线程来。这个就是协程的优势所在。

理解一下协程和线程的区别：

线程一旦开始执行就不会暂停，直到任务结束，这个过程是连续的，线程是抢占式的调度，不存在协作的问题。
协程程序能够自己挂起和恢复，程序自己处理挂起恢复实现程序执行流程的协作式调度。
Kotlin 中所谓的挂起，就是一个稍后会被自动切回来的线程调度操作，这个 resume
功能是协程的，如果不在协程里面调用，那它就没法恢复。所以挂起函数必须在协程或者另一个挂起函数里面被调用。总是直接或者间接地在协程里被调用。

3.5 如何实现挂起函数
实现挂起的的目的是让程序脱离当前的线程，也就是要切线程，kotlin 协程提供了一个 withContext() 方法，来实现线程切换。

private suspend fun calB(): Int {
withContext(Dispatchers.IO) {
printWithThreadInfo()
}
return 2
}
withContext() 本身也是一个挂起函数，它接收一个 Dispatcher参数，依赖这个参数，协程被挂起，切到别的线程。所以想要自己写一个挂起函数，除了加上
suspend 关键字加以休市以外，还需要函数内部直接或者间接的调用 Kotlin 协程框架自带的挂起函数才行。比如前面调用的 delay
函数，框架内部实际上进行了切线程的操作。

3.5.1 suspend 的意义
suspend 并不能切换线程。切线程依赖的是挂起函数里面的实际代码，这个关键字，只是一个提醒作用。如果我创建一个 suspend
函数，内部不包含其它挂起函数，编译器同样会提示这个修饰符是多余的。

suspend 表明这个函数时挂起函数，限制了它只能在协程或者其它挂起函数里面调用。

其它语言，比如 C#，使用的 async 关键字。

3.5.2 如何定义挂起函数
如果一个函数比较耗时，那么就可以把它定义成挂起函数。耗时一般有两种情况： I/O 操作和CPU 计算工作。
另外还有延时操作也可以把它定义成挂起函数，代码本身执行不耗时，但是需要延时一段时间。

写法
给函数加上 suspend 关键字，如果是耗时操作在 withContext 把函数的内容操作就可以了。如果是延时操作，则调用 delay 函数即可。
延时操作：

suspend fun testA() {
...
delay(1000)
...
}
耗时操作：

suspend fun testB() {
withContext(Dispatchers.IO) {
...
}
}
也可以写成：

suspend fun testB() = withContext(Dispatchers.IO) {
...
}
四、协程的上下文和作用域
两个概念：

CoroutineContext 协程的上下文
CoroutineScope 协程的作用域
4.1 协程上下文 CoroutineContext
协程总是运行在一些以 CoroutineContext 类型为代表的上下文中。协程上下文是各种不同元素的集合。其中主元素是协程中的 Job
以及它的调度器。

协程上下文包含当前协程scope的信息， 比如的Job, ContinuationInterceptor, CoroutineName
和CoroutineId。在CoroutineContext中，是用map来存这些信息的，
map的键是这些类的伴生对象，值是这些类的一个实例，你可以这样子取得context的信息:

val job = context[Job]
val continuationInterceptor = context[ContinuationInterceptor]
Job继承了CoroutineContext.Element，CoroutineContext.Element继承了 CoroutineContext。 他是协程上下文的一部分。 Job 一个重要的子类
———— AbstractCoroutine，即协程。使用launch 或者async方法都会实例化出一个AbstractCoroutine 的协程对象。一个协程的协程上下文的Job值就是他本身。

val job = mScope.launch {
printWithThreadInfo("job: ${this.coroutineContext[Job]}")
}
printWithThreadInfo("job2: $job")
printWithThreadInfo("job3: ${job[Job]}")
输出：

thread id: 1, thread name: main ---> job2: StandaloneCoroutine{Active}@1ee0005
thread id: 12, thread name: test_dispatcher ---> job: StandaloneCoroutine{Active}@1ee0005
thread id: 1, thread name: main ---> job3: StandaloneCoroutine{Active}@1ee0005
协程上下文包含一个 协程调度器
（CoroutineDispatcher）它确定了相关的协程在哪个线程或哪些线程上执行。协程调度器可以将协程限制在一个特定的线程执行，或将它分派到一个线程池，亦或是让它不受限地运行。
所有的协程构建器诸如 launch 和 async 接收一个可选的 CoroutineContext 参数，它可以被用来显式的为一个新协程或其它上下文元素指定一个调度器。
当调用 launch { …… } 时不传参数，它从启动了它的 CoroutineScope 中承袭了上下文（以及调度器）。

CoroutineContext最重要的两个信息是 Dispatcher 和 Job, 而 Dispatcher 和 Job 本身又实现了 CoroutineContext 的接口。是其子类。
这个设计就很有意思了。

有时我们需要在协程上下文中定义多个元素。我们可以使用 + 操作符来实现。 比如说，我们可以显式指定一个调度器来启动协程并且同时显式指定一个命名：

launch(Dispatchers.Default + CoroutineName("test")) {
println("I'm working in thread ${Thread.currentThread().name}")
}
这得益于 CoroutineContext 重载了操作符 +。

4.2 协程作用域 CoroutineScope
CoroutineScope 即协程运行的作用域,它的源码如下：

public interface CoroutineScope {
public val coroutineContext: CoroutineContext
}
可以看出CoroutineScope的代码很简单，主要作用是提供 CoroutineContext， 启动协程需要 CoroutineContext。
作用域可以管理其域内的所有协程。一个CoroutineScope可以有许多的子scope。协程内部是通过 CoroutineScope.coroutineContext
自动继承自父协程的上下文。而 CoroutineContext 就是在作用域内为协程进行线程切换的快捷方式。

注意：当使用 GlobalScope 来启动一个协程时，则新协程的作业没有父作业。 因此它与这个启动的作用域无关且独立运作。GlobalScope
包含的是 EmptyCoroutineContext。

一个父协程总是等待所有的子协程执行结束。父协程并不显式的跟踪所有子协程的启动，并且不必使用 Job.join 在最后的时候等待它们。
取消父协程会取消所有的子协程。所以使用 Scope 来管理协程的生命周期。
默认情况下，协程内，某个子协程抛出一个非 CancellationException 异常，未被捕获，会传递到父协程，任何一个子协程异常退出，那么整体都将退出
4.3 创建 CoroutineScope
创建一个 CoroutineScope, 只需调用 public fun CoroutineScope(context: CoroutineContext) 方法，传入一个 CoroutineContext
对象。

在协程作用域内，启动一个子协程，默认自动继承父协程的上下文，但在启动时，我们可以指定传入上下文。

val dispatcher = Executors.newFixedThreadPool(1).asCoroutineDispatcher()
val myScope = CoroutineScope(dispatcher)
myScope.launch {
...
}
4.4 SupervisorJob
启动一个协程，默认是实例化的是 Job 类型。该类型下，协程内，某个子协程抛出一个非 CancellationException
异常，未被捕获，会传递到父协程，任何一个子协程异常退出，那么整体都将退出。
为了解决上述问题，可以使用SupervisorJob替代Job，SupervisorJob与Job基本类似，区别在于不会被子协程的异常所影响。

private val svJob = SupervisorJob()
private val mDispatcher = newSingleThreadContext("test_dispatcher")

private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
printWithThreadInfo("exceptionHandler: throwable: $throwable")
}

private val svScope = CoroutineScope(svJob + mDispatcher + exceptionHandler)
private val mScope = CoroutineScope(Job() + mDispatcher + exceptionHandler)

svScope.launch {
...
}

// 或者
supervisorScope {
launch {
...
}
}
4.5 如何在 Android 中使用协程
4.5.1 自定义 coroutineScope
不要使用 GlobalScope 去启动协程，因为 GlobalScope 启动的协程生命周期与应用程序的生命周期一致，无法取消。官方建议在 Android
中自定义协程作用域。当然Kotlin 给我们提供了 MainScope，我们可以直接使用。

public fun MainScope(): CoroutineScope = ContextScope(SupervisorJob() + Dispatchers.Main)
然后让 Activity 实现该作用域：

class BasicCorotineActivity : AppCompatActivity(), CoroutineScope by MainScope() {
...
}
然后再通过 launch 或者 async 启动协程

private fun loadAndShow() {
launch {
val task = async(Dispatchers.IO) {
// load 过程
delay(3000)
...
"hello, kotlin"
}
tvShow.setText(task.await())
}
}
最后别忘了，在 Activity onDestory 时取消协程。

override fun onDestroy() {
cancel()
super.onDestroy()
}
4.5.2 ViewModelScope
如果你使用了 ViewModel + LiveData 实现 MVVM 架构，根本就不会在 Activity 上书写任何逻辑代码，更别说启动协程了。这个时候大部分工作就要交给
ViewModel 了。那么如何在 ViewModel 中定义协程作用域呢？直接把上面的 MainScope() 搬过来就可以了。

class ViewModelOne : ViewModel() {

    private val viewModelJob = SupervisorJob()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    val mMessage: MutableLiveData<String> = MutableLiveData()

    fun getMessage(message: String) {
        uiScope.launch {
            val deferred = async(Dispatchers.IO) {
                delay(2000)
                "post $message"
            }
            mMessage.value = deferred.await()
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

}
这里的 uiScope 其实就等同于 MainScope。调用 getMessage() 方法和之前的 loadAndShow() 效果也是一样的，记得在 ViewModel 的
onCleared() 回调里取消协程。

你可以定义一个 BaseViewModel 来处理这些逻辑，避免重复书写模板代码。

然而，Kotlin 提供了 viewmodel-ktx 来了。引入下面的依赖：

implementation "androidx.lifecycle:lifecycle-viewmodel-ktx:2.2.0-alpha03"
然后直接使用协程作用域 viewModelScope 就可以了。viewModelScope 是 ViewModel 的一个扩展属性，定义如下：

val ViewModel.viewModelScope: CoroutineScope
get() {
val scope: CoroutineScope? = this.getTag(JOB_KEY)
if (scope != null) {
return scope
}
return setTagIfAbsent(JOB_KEY,
CloseableCoroutineScope(SupervisorJob() + Dispatchers.Main))
}
所以，直接使用 viewModelScope 就是最好的选择。

4.5.3 LifecycleScope
与 viewModelScope 配套的 还有 LifecycleScope, 引入依赖：

implementation "androidx.lifecycle:lifecycle-runtime-ktx:2.2.0-alpha03"
lifecycle-runtime-ktx 给每个 LifeCycle 对象通过扩展属性定义了协程作用域 lifecycleScope 。可以通过
lifecycle.coroutineScope 或者 lifecycleOwner.lifecycleScope 进行访问。示例代码如下：

lifecycleOwner.lifecycleScope.launch {
val deferred = async(Dispatchers.IO) {
getMessage("LifeCycle Ktx")
}
mMessage.value = deferred.await()
}
当 LifeCycle 回调 onDestroy() 时，协程作用域 lifecycleScope 会自动取消。

五、协程并发中的数据同步问题
5.1 线程的数据安全问题
经典例子：

var flag = true

fun main() {
Thread {
Thread.sleep(1000)
flag = false
}.start()
while (flag) {
}
}
程序并没有像我们所期待的那样，在一秒之后，退出，而是一直处于循环中。

给 flag 加上 volatile 关键修饰:

@Volatile
var flag = true
没有用 volatile 修饰 flag 之前，改变了不具有可见性，一个线程将它的值改变后，另一个线程却 “不知道”，所以程序没有退出。当把变量声明为
volatile 类型后，编译器与运行时都会注意到这个变量是共享的，因此不会将该变量上的操作与其他内存操作一起重排序。volatile
变量不会被缓存在寄存器或者对其他处理器不可见的地方，因此在读取volatile类型的变量时总会返回最新写入的值。

在访问volatile变量时不会执行加锁操作，因此也就不会使执行线程阻塞，因此volatile变量是一种比sychronized关键字更轻量级的同步机制。

当对非 volatile 变量进行读写的时候，每个线程先从内存拷贝变量到CPU缓存中。如果计算机有多个CPU，每个线程可能在不同的CPU上被处理，这意味着每个线程可以拷贝到不同的
CPU cache 中。

而声明变量是 volatile 的，JVM 保证了每次读变量都从内存中读，跳过 CPU cache 这一步。

volatile 修饰的遍历具有如下特性：

保证此变量对所有的线程的可见性，当一个线程修改了这个变量的值，volatile
保证了新值能立即同步到主内存，以及每次使用前立即从主内存刷新。但普通变量做不到这点，普通变量的值在线程间传递均需要通过主内存（详见：Java内存模型）来完成。
禁止指令重排序优化。
不会阻塞线程。
如果在 while 循环里加一行打印，即使去掉 volatile 修饰，也可以退出程序，查看 println() 源码，最终发现，里面有同步代码块，

synchronized (this) {
ensureOpen();
textOut.newLine();
textOut.flushBuffer();
charOut.flushBuffer();
if (autoFlush)
out.flush();
}
那么问题来了，synchronized 到底干了什么。。
按理说，synchronized 只会保证该同步块中的变量的可见性，发生变化后立即同步到主存，但是，flag
变量并不在同步块中，实际上，JVM对于现代的机器做了最大程度的优化，也就是说，最大程度的保障了线程和主存之间的及时的同步，也就是相当于虚拟机尽可能的帮我们加了个volatile，但是，当CPU被一直占用的时候，同步就会出现不及时，也就出现了后台线程一直不结束的情况。

5.2 协程中的数据同步问题
看如下例子：

class Test {
private var count = 0
suspend fun test() = withContext(Dispatchers.IO) {
repeat(100) {
launch {
repeat(1000) {
count++
}
}
}
launch {
delay(3000)
printWithThreadInfo("end count: $count")
}
}
}

fun main() = runBlocking<Unit> {
Test().test()
}
执行输出结果：

thread id: 15, thread name: DefaultDispatcher-worker-4 ---> end count: 58059
并不是我们期待的 100000。很明显，协程并发过程中数据不同步造成的。

5.2.1 volatile 无效？
很显然，有人肯定也想着，使用 volatile 修饰变量，就可以解决，真的是这样吗？其实不然。我们给 count 变量用 volatile
修饰也依然得不到期望的结果。
volatile 在并发中保证可见性，但是不保证原子性。 count++ 该运算，包含读、写操作，并非一次原子操作。这样并发情况下，自然得不到期望的结果。

5.2.2 使用线程安全的数据结构
一种解决办法是使用线程安全地数据结构。们可以使用具有 incrementAndGet 原子操作的 AtomicInteger 类：

class Test {
private var count = AtomicInteger()
suspend fun test() = withContext(Dispatchers.IO) {
repeat(100) {
launch {
repeat(1000) {
count.incrementAndGet()
}
}
}
launch {
delay(3000)
printWithThreadInfo("end count: ${count.get()}")
}
}
}

fun main() = runBlocking<Unit> {
Test().test()
}
输出结果：

thread id: 35, thread name: DefaultDispatcher-worker-24 ---> end count: 100000
5.2.3 同步操作
对数据的增加进行同步操作。可以同步计数自增的代码块：

class Test {

    private val obj = Any()

    private var count = 0
    suspend fun test() = withContext(Dispatchers.IO) {
        repeat(100) {
            launch {
                repeat(1000) {
                    synchronized(obj) {  // 同步代码块
                        count++
                    }
                }
            }
        }
        launch {
            delay(3000)
            printWithThreadInfo("end count: $count")
        }
    }

}
或者使用 ReentrantLock 操作。

class Test {

    private val mLock = ReentrantLock()

    private var count = 0
    suspend fun test() = withContext(Dispatchers.IO) {
        repeat(100) {
            launch {
                repeat(1000) {
                    mLock.lock()
                    try{
                        count++
                    } finally {
                        mLock.unlock()
                    }
                }
            }
        }
        launch {
            delay(3000)
            printWithThreadInfo("end count: $count")
        }
    }

}

fun main() = runBlocking<Unit> {
val cos = measureTimeMillis {
Test().test()
}
printWithThreadInfo("cos time: ${cos.toString()}")
}
输出结果为：

thread id: 60, thread name: DefaultDispatcher-worker-49 ---> end count: 100000
thread id: 1, thread name: main ---> cos time: 3127
在协程中的替代品叫做 Mutex， 它具有 lock 和 unlock 方法，关键的区别在于， Mutex.lock() 是一个挂起函数，它不会阻塞当前线程。还有
withLock 扩展函数，可以方便的替代常用的 mutex.lock(); 、try { …… } finally { mutex.unlock() } 模式：

class Test {

    private val mutex = Mutex()

    private var count = 0
    suspend fun test() = withContext(Dispatchers.IO) {
        repeat(100) {
            launch {
                repeat(1000) {
                    mutex.withLock {
                        count++
                    }
                }
            }
        }
        launch {
            delay(3000)
            printWithThreadInfo("end count: $count")
        }
    }

}
5.2.4 限制线程
在同一个线程中进行计数自增，就不会存在数据同步问题。每次进行自增操作时，切换到单一线程。如同 Android，UI 刷新必须切换到主线程一般。

class Test {

    private val countContext = newSingleThreadContext("CountContext")

    private var count = 0
    suspend fun test() = withContext(countContext) {
        repeat(100) {
            launch {
                repeat(1000) {
                    count++
                }
            }
        }
        launch {
            delay(3000)
            printWithThreadInfo("end count: $count")
        }
    }

}
5.2.5 使用 Actors
一个 actor 是由协程、 被限制并封装到该协程中的状态以及一个与其它协程通信的 通道 组合而成的一个实体。一个简单的 actor
可以简单的写成一个函数， 但是一个拥有复杂状态的 actor 更适合由类来表示。

有一个 actor 协程构建器，它可以方便地将 actor 的邮箱通道组合到其作用域中（用来接收消息）、组合发送 channel 与结果集对象，这样对
actor 的单个引用就可以作为其句柄持有。

使用 actor 的第一步是定义一个 actor 要处理的消息类。 Kotlin 的密封类很适合这种场景。 我们使用 IncCounter 消息（用来递增计数器）和
GetCounter 消息（用来获取值）来定义 CounterMsg 密封类。 后者需要发送回复。CompletableDeferred 通信原语表示未来可知（可传达）的单个值，
这里被用于此目的。

// 计数器 Actor 的各种类型
sealed class CounterMsg
object IncCounter : CounterMsg() // 递增计数器的单向消息
class GetCounter(val response: CompletableDeferred<Int>) : CounterMsg() // 携带回复的请求
接下来定义一个函数，使用 actor 协程构建器来启动一个 actor：

// 这个函数启动一个新的计数器 actor
fun CoroutineScope.counterActor() = actor<CounterMsg> {
var counter = 0 // actor 状态
for (msg in channel) { // 即将到来消息的迭代器
when (msg) {
is IncCounter -> counter++
is GetCounter -> msg.response.complete(counter)
}
}
}
主要代码：

class Test {

    suspend fun test() = withContext(Dispatchers.IO) {
        val counterActor = counterActor() // 创建该 actor
        repeat(100) {
            launch {
                repeat(1000) {
                    counterActor.send(IncCounter)
                }
            }
        }
        launch {
            delay(3000)
            // 发送一条消息以用来从一个 actor 中获取计数值
            val response = CompletableDeferred<Int>()
            counterActor.send(GetCounter(response))
            println("Counter = ${response.await()}")
            counterActor.close() // 关闭该actor
        }
    }

}
actor 本身执行时所处上下文（就正确性而言）无关紧要。一个 actor 是一个协程，而一个协程是按顺序执行的，因此将状态限制到特定协程可以解决共享可变状态的问题。实际上，actor
可以修改自己的私有状态， 但只能通过消息互相影响（避免任何锁定）。
actor 在高负载下比锁更有效，因为在这种情况下它总是有工作要做，而且根本不需要切换到不同的上下文。

实际上， CoroutineScope.actor()方法返回的是一个 SendChannel对象。Channel 也是 Kotlin 协程中的一部分。后面的文章会详细介绍。