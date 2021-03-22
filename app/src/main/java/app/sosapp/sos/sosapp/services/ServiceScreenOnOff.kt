package app.sosapp.sos.sosapp.services

import android.app.Service
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import app.sosapp.sos.sosapp.broadcastReceivers.BroadcastReceiverLockButton

class ServiceScreenOnOff : Service() {

    private var mScreenReceiver: BroadcastReceiverLockButton? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        registerScreenStatusReceiver()
    }

    override fun onDestroy() {
        unregisterScreenStatusReceiver()
        super.onDestroy()
    }

    private fun registerScreenStatusReceiver() {
        mScreenReceiver = BroadcastReceiverLockButton()
        val filter = IntentFilter()
        filter.addAction(Intent.ACTION_SCREEN_OFF)
        filter.addAction(Intent.ACTION_SCREEN_ON)
        registerReceiver(mScreenReceiver, filter)
    }

    private fun unregisterScreenStatusReceiver() {
        try {
            if (mScreenReceiver != null) {
                unregisterReceiver(mScreenReceiver)
            }
        } catch (e: IllegalArgumentException) {
        }
    }
}