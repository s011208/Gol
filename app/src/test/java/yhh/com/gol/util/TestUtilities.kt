package yhh.com.gol.util

import io.reactivex.Scheduler
import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.functions.Function
import io.reactivex.internal.schedulers.ExecutorScheduler
import io.reactivex.plugins.RxJavaPlugins
import java.util.concurrent.Callable
import java.util.concurrent.Executor

class TestUtilities {

    companion object {
        fun initRxSchedulers() {
            val immediate = object : Scheduler() {
                override fun createWorker(): Worker {
                    return ExecutorScheduler.ExecutorWorker(Executor { it.run() }, true)
                }
            }
            val function = Function<Callable<Scheduler>, Scheduler> { immediate }
            RxJavaPlugins.setInitIoSchedulerHandler(function)
            RxJavaPlugins.setInitComputationSchedulerHandler(function)
            RxJavaPlugins.setInitNewThreadSchedulerHandler(function)
            RxJavaPlugins.setInitSingleSchedulerHandler(function)
            RxAndroidPlugins.setInitMainThreadSchedulerHandler(function)
        }
    }
}