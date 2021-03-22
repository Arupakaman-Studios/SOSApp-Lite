package app.sosapp.sos.sosapp.uiModules.other

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.RemoteViews
import androidx.core.os.bundleOf
import app.sosapp.sos.sosapp.R
import app.sosapp.sos.sosapp.services.ServiceSOSFlash
import app.sosapp.sos.sosapp.services.ServiceSirenPlayer
import app.sosapp.sos.sosapp.uiModules.gotoSendSOSActivity
import app.sosapp.sos.sosapp.uiModules.sendSOS.ActivitySendSOS
import app.sosapp.sos.sosapp.utils.setFirebaseAnalyticsLogEvent


class SOSWidgetProvider: AppWidgetProvider() {

    companion object{
        private val TAG by lazy { "SOSWidgetProvider" }

        private const val ACTION_SEND_SOS = "send_sos"
        private const val ACTION_SOS_SIREN = "sos_siren"
        private const val ACTION_SOS_FLASH = "sos_flash"

    }

    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)
        context?.run {
            Log.d(TAG, "onReceive ${intent?.action}")
            setFirebaseAnalyticsLogEvent("SOS_WIDGET", bundleOf("Action" to (intent?.action?:"Null")))
            when (intent?.action) {
                ACTION_SEND_SOS -> {
                    startActivity(ActivitySendSOS.getIntent(this).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    })
                }
                ACTION_SOS_SIREN -> {
                    ServiceSirenPlayer.startOrStopService(this, !ServiceSirenPlayer.isRunning)
                }
                ACTION_SOS_FLASH -> {
                    ServiceSOSFlash.startOrStopService(this, !ServiceSOSFlash.isRunning)
                }
            }
        }
    }

    override fun onEnabled(context: Context?) {
        super.onEnabled(context)
    }

    override fun onUpdate(
        context: Context?,
        appWidgetManager: AppWidgetManager?,
        appWidgetIds: IntArray?
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        context?.run {
            val remoteViews = RemoteViews(packageName, R.layout.widget_sos_home_layout)
            val thisWidget = ComponentName(this, SOSWidgetProvider::class.java)

            remoteViews.setOnClickPendingIntent(R.id.llSOSView, getPendingSelfIntent(this, ACTION_SEND_SOS))
            remoteViews.setOnClickPendingIntent(R.id.btnFlash, getPendingSelfIntent(this, ACTION_SOS_FLASH))
            remoteViews.setOnClickPendingIntent(R.id.btnSiren, getPendingSelfIntent(this, ACTION_SOS_SIREN))

            appWidgetManager?.updateAppWidget(thisWidget, remoteViews)
        }
    }

    private fun getPendingSelfIntent(context: Context, action: String): PendingIntent {
        val intent = Intent(context, javaClass)
        intent.action = action
        return PendingIntent.getBroadcast(context, 0, intent, 0)
    }

}