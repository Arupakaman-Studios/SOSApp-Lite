package app.sosapp.sos.sosapp.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import app.sosapp.sos.sosapp.R
import app.sosapp.sos.sosapp.utils.SOSAppRes
import app.sosapp.sos.sosapp.utils.SirenPlayer
import app.sosapp.sos.sosapp.utils.setFirebaseAnalyticsLogEvent


class ServiceSirenPlayer : Service() {

    companion object {

        private val TAG by lazy { "ServiceSirenPlayer" }
        private const val ACTION_STOP = "stop_action"
        const val NOTIFICATION_ID = 101

        var isRunning = false

        fun startOrStopService(mContext: Context, start: Boolean) {
            Log.d(TAG, "startOrStop start -> $start")
            Intent(mContext, ServiceSirenPlayer::class.java).let { intent->
                if (start && !isRunning) {
                    ContextCompat.startForegroundService(mContext, intent)
                } else {
                    mContext.stopService(intent)
                }
                isRunning = start
            }
        }

    }

    private var mSirenPlayer: SirenPlayer? = null

    override fun onBind(p0: Intent?): IBinder? = null

    override fun onCreate() {
        Log.d(TAG, "onCreate")
        super.onCreate()

        mSirenPlayer = SirenPlayer(this)

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

        val stopIntent = Intent(this, ServiceSirenPlayer::class.java)
        stopIntent.action = ACTION_STOP
        val pendingStopIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            PendingIntent.getForegroundService(this, 0, stopIntent, 0)
        } else {
            PendingIntent.getService(this, 0, stopIntent, 0)
        }

        val notification = notifBuilder
                .setContentText(SOSAppRes.getString(R.string.msg_tap_to_turn_off_sos_siren))
                .setContentIntent(pendingStopIntent)
                .setPublicVersion(notifBuilder.build())
                .build()

        startForeground(NOTIFICATION_ID, notification)

        (getSystemService(AUDIO_SERVICE) as AudioManager?)?.let {am->
            am.setStreamVolume(AudioManager.STREAM_MUSIC,
                    am.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0)
        }

        mSirenPlayer?.playRepeatably()
        setFirebaseAnalyticsLogEvent("SOS_Siren_Played", bundleOf("Siren" to "Toggled"))

    }

    private val notifBuilder: NotificationCompat.Builder
        get() = NotificationCompat.Builder(this, SOSAppRes.getString(R.string.notification_channel_name))
                .setContentTitle(SOSAppRes.getString(R.string.msg_playing_siren))
                .setTicker(SOSAppRes.getString(R.string.msg_playing_siren))
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
            mSirenPlayer?.stop()
            startOrStopService(this, false)
        }

        return START_STICKY
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy()")
        isRunning = false
        mSirenPlayer?.stop()
        super.onDestroy()
    }

}