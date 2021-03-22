package app.sosapp.sos.sosapp.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import app.sosapp.sos.sosapp.R
import app.sosapp.sos.sosapp.SOSApp
import app.sosapp.sos.sosapp.utils.SOSAppRes
import app.sosapp.sos.sosapp.utils.reportException
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.util.concurrent.atomic.AtomicInteger

class MyFirebaseMessagingService: FirebaseMessagingService() {

    companion object {
        private val TAG by lazy { "MyFCMService" }
    }

    private val c: AtomicInteger = AtomicInteger(1000)
    private fun getNewNotificationId() = c.incrementAndGet()

    private fun getNotificationIcon(): Int {
        val useWhiteIcon = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
        return if (useWhiteIcon) R.drawable.ic_notif_icon else R.drawable.ic_app_logo
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate")
    }

    override fun onMessageReceived(p0: RemoteMessage) {
        super.onMessageReceived(p0)

        Log.d(TAG, "onMessageReceived $p0")
        Log.d(TAG, "ContentNotif 1 " + p0.notification?.title + "\n" + p0.notification?.body + "")

        val notification: RemoteMessage.Notification? = p0.notification
        val data: Map<String, String> = p0.data

        Log.d(TAG, "ContentNotif 2 $notification \n $data")

        kotlin.runCatching {
            sendNotification(p0.notification)
        }.onFailure {
            Log.e(TAG, "Notification Send Exc $it")
        }

    }

    override fun onNewToken(p0: String) {
        super.onNewToken(p0)
        Log.d(TAG, "onNewToken FCM_token $p0")
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy")
        super.onDestroy()
    }

    private fun sendNotification(data: RemoteMessage.Notification?) {

        val channelId = getString(R.string.notification_channel_name)

        val title = data?.title?:getString(R.string.app_name)
        var body = data?.body?:""

        var url = ""
        kotlin.runCatching {
            if (body.contains("::")) {
                body.split("::").let {
                    body = it[0]
                    url = if (it[1].startsWith("http")) it[1]
                    else {
                        if (Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=${it[1]}")).resolveActivity(packageManager) != null)
                            "market://details?id=${it[1]}"
                        else "https://play.google.com/store/apps/details?id=${it[1]}"
                    }
                }
            }else url = "https://play.google.com/store/apps/details?id=Arupakaman+Studios"
        }.onFailure {
            Log.e(TAG, "Notification Body Url Exc $it")
            it.reportException("Notification Body Url Exc")
            url = "https://play.google.com/store/apps/details?id=Arupakaman+Studios"
        }
        Log.d(TAG, "Notification Body -> $body Url -> $url")

        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        val pendingIntent = PendingIntent.getActivity(
                this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT
        )

        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
                .setSmallIcon(getNotificationIcon())
                //.setLargeIcon(BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher_foreground))
                .setContentTitle(title)
                .setContentText(body)
                .setTicker(SOSAppRes.getString(R.string.app_name))
                .setOngoing(true)
                .setShowWhen(true)
                .setVibrate(longArrayOf(0, 500, 1000))
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setWhen(System.currentTimeMillis())
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setChannelId(channelId)
                .setContentIntent(pendingIntent)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            notificationBuilder.color = ContextCompat.getColor(baseContext, R.color.colorBlack)


        //initiate notification sending
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        try {

            fun fireNotification() {
                notificationManager.notify(getNewNotificationId(), notificationBuilder.build())
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val notifMan = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                val audioAttributes = AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                        .build()
                with(
                        NotificationChannel(channelId, title, NotificationManager.IMPORTANCE_DEFAULT)
                ) {
                    setShowBadge(true)
                    enableVibration(true)
                    enableLights(false)
                    setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION), audioAttributes)
                    notifMan.createNotificationChannel(this)
                }
            }

            val textStyle = NotificationCompat.BigTextStyle().bigText(body)
            textStyle.setBigContentTitle(title)
            notificationBuilder.setStyle(textStyle)

            fireNotification()
        } catch (e: Exception) {
            Log.e("NotifyingException", "" + e)
        }

    }

}