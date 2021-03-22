package app.sosapp.sos.sosapp

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.multidex.MultiDexApplication
import app.sosapp.sos.sosapp.data.SOSAppSharedPrefs
import app.sosapp.sos.sosapp.uiModules.sendSOS.ActivitySendSOS
import app.sosapp.sos.sosapp.utils.DefaultExceptionHandler
import app.sosapp.sos.sosapp.utils.LocaleHelper
import app.sosapp.sos.sosapp.utils.SOSAppRes

class SOSApp : MultiDexApplication() {

    companion object{

    }

    override fun onCreate() {
        super.onCreate()

        SOSAppRes.setContext(applicationContext)

        if (BuildConfig.DEBUG)
            Thread.setDefaultUncaughtExceptionHandler(DefaultExceptionHandler(this))

        //Init
        val mPrefs = SOSAppSharedPrefs.getInstance(this)

        AppCompatDelegate.setDefaultNightMode(mPrefs.selectedThemeMode)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            ShortcutManagerCompat.addDynamicShortcuts(this, listOf(
                getToggleShortCut(this, "send_sos", SOSAppRes.getString(R.string.title_send_sos), R.drawable.ic_sos,
                    Intent(this, ActivitySendSOS::class.java)
                        .setAction("send_sos"))
            ))
        }
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(LocaleHelper.onAttach(base))
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        LocaleHelper.onAttach(this)
    }

    private fun getToggleShortCut(mContext: Context, id: String, label: String, icon: Int, intent: Intent) =
        ShortcutInfoCompat.Builder(mContext, id)
        .setIntent(intent)
        .setShortLabel(label)
        .setLongLabel(label)
        .setIcon(IconCompat.createWithResource(mContext, icon))
        .setAlwaysBadged()
        .build()

}