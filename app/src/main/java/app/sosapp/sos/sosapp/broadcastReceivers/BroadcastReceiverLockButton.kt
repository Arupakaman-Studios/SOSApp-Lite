package app.sosapp.sos.sosapp.broadcastReceivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log


class BroadcastReceiverLockButton : BroadcastReceiver() {

    companion object{
        private val TAG by lazy { "LockButtonReceiver" }
    }

    override fun onReceive(context: Context?, intent: Intent) {
        context?.run {
            if (intent.action == Intent.ACTION_SCREEN_OFF) {
                //Take count of the screen off position
            } else if (intent.action == Intent.ACTION_SCREEN_ON) {
                //Take count of the screen on position
            }
            Log.d(TAG, "Action -> ${intent.action}")
        }
    }
}