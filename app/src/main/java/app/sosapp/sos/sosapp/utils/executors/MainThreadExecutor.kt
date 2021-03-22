package app.sosapp.sos.sosapp.utils.executors

import android.os.Handler
import android.os.Looper
import java.util.concurrent.Executor

class MainThreadExecutor : Executor {
    private val handler: Handler = Handler(Looper.getMainLooper())

    override fun execute(p0: Runnable?) {
        p0?.let { handler.post(p0) }
    }

}