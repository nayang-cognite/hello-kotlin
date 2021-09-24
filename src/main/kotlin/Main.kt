import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*

fun hello() {
    println("Hello World!")
    val name = readLine()
    println("Hello $name!")
}

fun Log(log: String, TimeMillis: Long = 0) {
    val sdf = SimpleDateFormat("dd/M/yyyy hh:mm:ss.SSS")
    var dt = if (TimeMillis == 0.toLong()) Date() else Date(TimeMillis)
    val currentDate = sdf.format(dt)
    println("$currentDate $log")
}

suspend fun doWorld() {
    delay(1000L)
    Log("World!")
}

suspend fun doWorld2() = coroutineScope {
    launch {
        delay(1000L)
        Log("Thread 1")
    }
    launch {
        delay(1000L)
        Log("Thread 2")
    }
    val job = launch {
        delay(1000L)
        Log("Thread 3")
    }
    Log("Hello")
    job.join()
    Log("Done")
}

fun main() = runBlocking {
    launch {
        doWorld()
    }
    Log("Hello")
}

fun main2() = runBlocking {
    doWorld2()
}

fun main3() = runBlocking {
    repeat(5) { index ->
        launch {
            delay(5000L)
            Log("$index")
        }
    }
    Log("Hello")
}

suspend fun doWorld4() = coroutineScope {
    val job = launch {
        repeat(1000) { i ->
            Log("job $i")
            delay(500L)
        }
    }
    Log("main: start")
    delay(1300L)
    Log("main: i'm tired of waiting")
//    job.cancel()
//    job.join()
    job.cancelAndJoin()
    Log("main: now i can quit")
}

fun main4() = runBlocking {
    doWorld4()
}

suspend fun doWorld5() = coroutineScope {
    val startTime = System.currentTimeMillis()
    Log("main: start", startTime)
    val job = launch(Dispatchers.Default) {
        var nextPrintTime = startTime
        var i = 0
        while (i < 5) { // computation loop, just wastes CPU
//        while (isActive) { // computation loop, just wastes CPU
            // print a message twice a second
            if (System.currentTimeMillis() >= nextPrintTime) {
                Log("job: I'm sleeping ${i++} ...")
                nextPrintTime += 500L
            }
        }
    }
    delay(1300L) // delay a bit
    Log("main: I'm tired of waiting!")
    job.cancelAndJoin() // cancels the job and waits for its completion
    Log("main: Now I can quit.")
}

fun main5() = runBlocking {
    doWorld5()
}

suspend fun doWorld6() = coroutineScope {
    val job = launch {
        try {
            repeat(1000) { i ->
                Log("job: I'm sleeping $i ...")
                delay(500L)
            }
        } finally {
            Log("job: I'm running finally")
            delay(1000L)
            Log("job: And I've just delayed for 1 sec ***")
        }
    }
    Log("main: start")
    delay(1300L) // delay a bit
    Log("main: I'm tired of waiting!")
    job.cancelAndJoin() // cancels the job and waits for its completion
    Log("main: Now I can quit.")
}

fun main6() = runBlocking {
    doWorld6()
}

suspend fun doWorld7() = coroutineScope {
    val job = launch {
        try {
            repeat(1000) { i ->
                Log("job: I'm sleeping $i ...")
                delay(500L)
            }
        } finally {
            withContext(NonCancellable) {
                Log("job: I'm running finally")
                delay(1000L)
                Log("job: And I've just delayed for 1 sec because I'm non-cancellable")
            }
        }
    }
    Log("main: start")
    delay(1300L) // delay a bit
    Log("main: I'm tired of waiting!")
    job.cancelAndJoin() // cancels the job and waits for its completion
    Log("main: Now I can quit.")
}

fun main7() = runBlocking {
    doWorld7()
}

fun main8() {
    try {
        runBlocking {
            withTimeout(1300L) {
                repeat(1000) { i ->
                    Log("I'm sleeping $i ...")
                    delay(500L)
                }
            }
        }
    } catch (e: TimeoutCancellationException) {
        Log("caught TimeoutCancellationException")
    } finally {
        Log("done")
    }
}

fun main9() {
    runBlocking {
        Log("start")
        val result = withTimeoutOrNull(500L) {
            repeat(2) { i ->
                delay(500L)
                Log("I'm sleeping $i ...")
            }
            "Done"
        }
        Log("start2")
        Log("Result is $result")
    }
}

fun main10() {
    var acquired = 0

    class Resource {
        init {
            acquired++
        } // Acquire the resource

        fun close() {
            acquired--
        } // Release the resource
    }

    runBlocking {
        repeat(100_000) { // Launch 100K coroutines
            launch {
                var resource: Resource? = null // Not acquired yet
                try {
                    withTimeout(60) { // Timeout of 60 ms
                        delay(50) // Delay for 50 ms
                        resource = Resource() // Store a resource to the variable if acquired
                    }
                    // We can do something else with the resource here
                } finally {
                    resource?.close() // Release the resource if it was acquire
                }
            }
        }
    }
    // Outside of runBlocking all coroutines have completed
    Log("final acquired=$acquired") // Print the number of resources still acquired
}

fun main11() {
    var acquired = 0

    class Resource {
        init {
            acquired++
        } // Acquire the resource

        fun close() {
            acquired--
        } // Release the resource
    }

    runBlocking {
        repeat(1000) { // Launch 100K coroutines
            launch {
                val resource = withTimeout(60) { // Timeout of 60 ms
                    delay(10) // Delay for 50 ms
                    Resource() // Acquire a resource and return it from withTimeout block
                }
                Log("*")
                resource.close() // Release the resource
            }
        }
    }
    // Outside of runBlocking all coroutines have completed
    println(acquired) // Print the number of resources still acquired
}

fun function() {
    val items = listOf(1, 2, 3, 4)

    infix fun Int.mult(x: Int) : Int {
//        println(this)
        return (this * x)
    }

    var accu: Int = 1
    for (e:Int in items) {
        print("$e ")
        accu = accu mult e
    }
    println()
    println("accu=$accu")

    val product = items.fold(1, Int::mult)

    val combine = {acc: Int, elem: Int -> acc * elem}
    val product2 = items.fold(1, combine)

    val combine3 = fun(accu:Int, elem: Int) : Int { return accu * elem}
    val product3 = items.fold(1, combine3)

    println(product)
    println(product2)
    println(product3)
}

fun function2() {
    val sum: Int.(Int) -> Int = { other -> plus(other) }
    println(5.sum(1))

    val repeatFun : String.(Int) -> String = {times -> this.repeat(times)}
    println("a".repeatFun(5))

    val repeatFun2 : (String, Int) -> String = {a, b -> a.repeat(b)}
    println(repeatFun2("a", 5))

    fun runRepeatFun(f: (String, Int) -> String) : String {
        return f("a", 10)
    }
    println(runRepeatFun(repeatFun))
    println(runRepeatFun(repeatFun2))
}

fun foo() {
    listOf(1, 2, 3, 4, 5).forEach {
        if (it == 3) return // non-local return directly to the caller of foo()
        println(it)
    }
    println("this point is unreachable")
}

fun foo2() {
    var a = listOf(5, 1, 2, 3).filter {
        val shouldFilter = it > 2
        shouldFilter
    }.forEach{println(it)}
}

fun main(args: Array<String>) {
    foo2()
}


