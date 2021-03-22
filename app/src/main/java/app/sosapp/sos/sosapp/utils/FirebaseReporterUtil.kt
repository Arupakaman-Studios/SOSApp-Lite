package app.sosapp.sos.sosapp.utils

import android.content.Context
import android.os.Bundle
import androidx.core.os.bundleOf
import app.sosapp.sos.sosapp.BuildConfig
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics

object FirebaseReporterUtil{

    fun reportException(message: String){
        if (!BuildConfig.DEBUG) FirebaseCrashlytics.getInstance().log("MANUAL_LOG : $message\nExc : $this")
    }

}

fun Throwable.reportException(message: String){
    if (!BuildConfig.DEBUG) FirebaseCrashlytics.getInstance().log("$message\nExc : $this")
}

fun Context.setFirebaseAnalyticsCurrentScreen(params: Bundle){
    if (!BuildConfig.DEBUG) FirebaseAnalytics.getInstance(this).logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, params)
}

fun Context.setFirebaseAnalyticsCurrentScreen(screenName: String){
    setFirebaseAnalyticsCurrentScreen(bundleOf(FirebaseAnalytics.Param.SCREEN_NAME to screenName))
}

fun Context.setFirebaseAnalyticsLogEvent(eventName: String, params: Bundle){
    if (!BuildConfig.DEBUG) FirebaseAnalytics.getInstance(this).logEvent(eventName, params)
}