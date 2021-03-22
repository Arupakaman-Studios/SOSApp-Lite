package app.sosapp.sos.sosapp.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import app.sosapp.sos.sosapp.R
import app.sosapp.sos.sosapp.uiModules.home.ActivityHome
import app.sosapp.sos.sosapp.utils.SOSAppRes
import app.sosapp.sos.sosapp.utils.SOSFlashUtil
import app.sosapp.sos.sosapp.utils.setFirebaseAnalyticsLogEvent

class ServiceSOSFlash : Service() {

    companion object {

        private val TAG by lazy { "ServiceSOSFlash" }
        private const val ACTION_STOP = "stop_action"
        private const val NOTIFICATION_ID = 102

        var isRunning = false

        fun startOrStopService(mContext: Context, start: Boolean) {
            Log.d(TAG, "startOrStop start -> $start")
            Intent(mContext, ServiceSOSFlash::class.java).let { intent->
                if (start && !isRunning) {
                    ContextCompat.startForegroundService(mContext, intent)
                } else {
                    mContext.stopService(intent)
                }
                isRunning = start
            }
        }

    }

    private var mSOSFlashUtil: SOSFlashUtil? = null

    override fun onBind(p0: Intent?): IBinder? = null

    override fun onCreate() {
        Log.d(TAG, "onCreate")
        super.onCreate()

        mSOSFlashUtil = SOSFlashUtil(this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notifMan = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            with(NotificationChannel(
                    SOSAppRes.getString(R.string.notification_channel_name),
                    SOSAppRes.getString(R.string.app_name),
                    NotificationManager.IMPORTANCE_HIGH
            )) {
                setShowBadge(true)
                enableVibration(false)
                enableLights(false)
                setSound(null, null)
                notifMan.createNotificationChannel(this)
            }
        }

        val stopIntent = Intent(this, ServiceSOSFlash::class.java)
        stopIntent.action = ACTION_STOP
        val pendingStopIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            PendingIntent.getForegroundService(this, 0, stopIntent, 0)
        } else {
            PendingIntent.getService(this, 0, stopIntent, 0)
        }

        val notification = notifBuilder
                .setContentText(SOSAppRes.getString(R.string.msg_tap_to_turn_off_sos_flash))
                .setContentIntent(pendingStopIntent)
                .setPublicVersion(notifBuilder.build())
                .build()

        startForeground(NOTIFICATION_ID, notification)

        mSOSFlashUtil?.startSOSFlash()
        setFirebaseAnalyticsLogEvent("SOS_Flash_Toggled", bundleOf("Flash" to "Toggled"))

    }

    private val notifBuilder: NotificationCompat.Builder
        get() = NotificationCompat.Builder(this, SOSAppRes.getString(R.string.notification_channel_name))
                .setContentTitle(SOSAppRes.getString(R.string.msg_sos_flash_is_on))
                .setTicker(SOSAppRes.getString(R.string.msg_sos_flash_is_on))
                .setSmallIcon(R.drawable.ic_notif_icon)
                .setOngoing(true)
                .setShowWhen(true)
                .setWhen(System.currentTimeMillis())
                .setColor(ContextCompat.getColor(applicationContext, R.color.colorPrimary))
                .setCategory(NotificationCompat.CATEGORY_SERVICE)


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand()")
        if (intent?.action == ACTION_STOP) {
            Log.d(TAG, "Received ACTION_STOP")
            mSOSFlashUtil?.stopFlash()
            startOrStopService(this, false)
        }

        return START_STICKY
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy()")
        isRunning = false
        mSOSFlashUtil?.stopFlash()
        super.onDestroy()
    }

}