package app.sosapp.sos.sosapp.utils

import android.content.Context
import android.util.Log
import kotlin.system.exitProcess

class DefaultExceptionHandler constructor(private val appContext: Context) : Thread.UncaughtExceptionHandler {

    companion object {
        private val TAG by lazy { "DefaultExceptionHandler" }
    }

    override fun uncaughtException(t: Thread, e: Throwable) {

        Log.e(TAG, "PrintStackTrace : $e")
        exitProcess(2)
    }
}