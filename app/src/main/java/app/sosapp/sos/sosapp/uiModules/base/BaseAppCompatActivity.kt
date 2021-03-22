package app.sosapp.sos.sosapp.uiModules.base

import android.content.Context
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.view.MenuItem
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import app.sosapp.sos.sosapp.R
import app.sosapp.sos.sosapp.data.SOSAppSharedPrefs
import app.sosapp.sos.sosapp.uiModules.dialogs.SOSAppDialogs
import app.sosapp.sos.sosapp.utils.LocaleHelper

abstract class BaseAppCompatActivity : AppCompatActivity() {

    protected val mPrefs by lazy { SOSAppSharedPrefs.getInstance(applicationContext) }
    protected lateinit var mDialogs: SOSAppDialogs

    protected inline fun <reified T : ViewDataBinding> binding(@LayoutRes resId: Int): Lazy<T>
            = lazy { DataBindingUtil.setContentView<T>(this, resId).apply {
        lifecycleOwner = this@BaseAppCompatActivity
    }}

    override fun onCreate(savedInstanceState: Bundle?) {
        if (mPrefs.selectedThemeMode == AppCompatDelegate.MODE_NIGHT_UNSPECIFIED) setTheme(R.style.Theme_SOSApp_Red)
        else setTheme(R.style.Theme_SOSApp)
        super.onCreate(savedInstanceState)
        mDialogs = SOSAppDialogs(this)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) onBackPressed()
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        if (::mDialogs.isInitialized){
            mDialogs.dismissAll()
        }
        super.onDestroy()
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase))
    }

}