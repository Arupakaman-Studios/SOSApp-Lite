package app.sosapp.sos.sosapp.utils.executors

import android.os.Process
import java.util.concurrent.Executor
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadFactory
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

/**
 * Singleton class for default executor supplier
 *
 * https://blog.mindorks.com/threadpoolexecutor-in-android-8e9d22330ee3
 *
 */

class DefaultExecutorSupplier private constructor(){

    companion object{
        /**
         * an instance of DefaultExecutorSupplier
         */
        private var mInstance: DefaultExecutorSupplier? = null

        /*
        * returns the instance of DefaultExecutorSupplier
        */
        @Synchronized
        fun getInstance(): DefaultExecutorSupplier {
            if (mInstance == null) {
                mInstance = DefaultExecutorSupplier()
            }
            return mInstance!!
        }
    }

    /*
    * Number of cores to decide the number of threads
    */
    private val NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors()

    /*
    * thread pool executor for background tasks
    */
    private var mForBackgroundTasks: ThreadPoolExecutor

    /*
    * thread pool executor for light weight background tasks
    */
    private var mForLightWeightBackgroundTasks: ThreadPoolExecutor

    /*
    * thread pool executor for main thread tasks
    */
    private var mMainThreadExecutor: Executor

    init {
        // setting the thread factory

        // setting the thread factory
        val backgroundPriorityThreadFactory: ThreadFactory = PriorityThreadFactory(Process.THREAD_PRIORITY_BACKGROUND)

        // setting the thread pool executor for mForBackgroundTasks;

        // setting the thread pool executor for mForBackgroundTasks;
        mForBackgroundTasks = ThreadPoolExecutor(
                NUMBER_OF_CORES * 2,
                NUMBER_OF_CORES * 2,
                60L,
                TimeUnit.SECONDS,
                LinkedBlockingQueue<Runnable>(),
                backgroundPriorityThreadFactory
        )

        // setting the thread pool executor for mForLightWeightBackgroundTasks;

        // setting the thread pool executor for mForLightWeightBackgroundTasks;
        mForLightWeightBackgroundTasks = ThreadPoolExecutor(
                NUMBER_OF_CORES * 2,
                NUMBER_OF_CORES * 2,
                60L,
                TimeUnit.SECONDS,
                LinkedBlockingQueue<Runnable>(),
                backgroundPriorityThreadFactory
        )

        // setting the thread pool executor for mMainThreadExecutor;

        // setting the thread pool executor for mMainThreadExecutor;
        mMainThreadExecutor = MainThreadExecutor()
    }


    /**
     * returns the thread pool executor for background task
     */
    fun forBackgroundTasks(): ThreadPoolExecutor {
        return mForBackgroundTasks
    }

    /**
     * returns the thread pool executor for light weight background task
     */
    fun forLightWeightBackgroundTasks(): ThreadPoolExecutor {
        return mForLightWeightBackgroundTasks
    }

    /**
     * returns the thread pool executor for main thread task
     */
    fun forMainThreadTasks(): Executor {
        return mMainThreadExecutor
    }

}