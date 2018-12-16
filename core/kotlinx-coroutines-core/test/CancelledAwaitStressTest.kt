/*
 * Copyright 2016-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.coroutines

import org.junit.*

class CancelledAwaitStressTest : TestBase() {
    private val n = 1000 * stressTestMultiplier

    /**
     * Tests that memory does not leak from cancelled [Deferred.await]
     */
    @Test
    fun testCancelledAwait() = runTest {
        val d = async {
            delay(Long.MAX_VALUE)
        }
        repeat(n) {
            val waiter = launch(start = CoroutineStart.UNDISPATCHED) {
                val a = ByteArray(10000000) // allocate 10M of memory here
                d.await()
                println(a) // make sure it is kept in state machine
            }
            waiter.cancel() // cancel await
            yield() // complete the waiter job, release its memory
        }
        d.cancel() // done test
    }

    /**
     * Tests that memory does not leak from cancelled [Job.join]
     */
    @Test
    fun testCancelledJoin() = runTest {
        val j = launch {
            delay(Long.MAX_VALUE)
        }
        repeat(n) {
            val joiner = launch(start = CoroutineStart.UNDISPATCHED) {
                val a = ByteArray(10000000) // allocate 10M of memory here
                j.join()
                println(a) // make sure it is kept in state machine
            }
            joiner.cancel() // cancel join
            yield() // complete the joiner job, release its memory
        }
        j.cancel() // done test
    }
}