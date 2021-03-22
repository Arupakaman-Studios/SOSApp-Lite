package app.sosapp.sos.sosapp.uiModules.settings

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import app.sosapp.sos.sosapp.R
import app.sosapp.sos.sosapp.databinding.FragmentSettingsBinding
import app.sosapp.sos.sosapp.uiModules.base.BaseFragment
import app.sosapp.sos.sosapp.uiModules.contactsPicker.FragmentContactsPicker
import app.sosapp.sos.sosapp.uiModules.home.ActivityHome
import app.sosapp.sos.sosapp.uiModules.restartHomeActivity
import app.sosapp.sos.sosapp.utils.LocaleHelper
import app.sosapp.sos.sosapp.utils.SOSAppRes
import app.sosapp.sos.sosapp.utils.disable
import app.sosapp.sos.sosapp.utils.enable
import app.sosapp.sos.sosapp.utils.invoke
import app.sosapp.sos.sosapp.utils.setFirebaseAnalyticsCurrentScreen
import app.sosapp.sos.sosapp.utils.setFirebaseAnalyticsLogEvent
import app.sosapp.sos.sosapp.utils.setSafeOnClickListener

class FragmentSettings : BaseFragment<FragmentSettingsBinding>(){

    companion object{
        private val TAG by lazy { "FragmentSettings" }

        private const val PERMISSIONS_REQUEST_CALL = 1004
    }

    override val layoutId = R.layout.fragment_settings

    private lateinit var navController: NavController

    private var selLangPos = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mDataBinding{
            navController = findNavController()

            selLangPos = LocaleHelper.getSelectedLanguageCodePosition(mActivity)
            updateSelLangUi()
            updateSelCountDownUi()

            switchSOSAddress.isChecked = mPrefs.sosAddressOn
            switchSilent.isChecked = mPrefs.sosSilentOn

            setSelectedTheme()
            setViewListeners()
            mActivity.setFirebaseAnalyticsCurrentScreen("Settings")
        }
    }

    override fun onResume() {
        super.onResume()
        mDataBinding.updateCallPermUi()
    }

     override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
         if (requestCode == PERMISSIONS_REQUEST_CALL) {
             if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                 mDataBinding.updateCallPermUi()
                 mActivity.setFirebaseAnalyticsLogEvent("DIRECT_CALL_PERM", bundleOf("Action" to "Granted"))
             }
         }
     }

    @SuppressLint("SetTextI18n")
    private fun FragmentSettingsBinding.setViewListeners(){
        llSystemTheme.setSafeOnClickListener {
            updateTheme(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            mActivity.setFirebaseAnalyticsLogEvent("THEME_CHANGED", bundleOf("Theme" to "System"))
        }
        llLightTheme.setSafeOnClickListener {
            updateTheme(AppCompatDelegate.MODE_NIGHT_NO)
            mActivity.setFirebaseAnalyticsLogEvent("THEME_CHANGED", bundleOf("Theme" to "Light"))
        }
        llDarkTheme.setSafeOnClickListener {
            updateTheme(AppCompatDelegate.MODE_NIGHT_YES)
            mActivity.setFirebaseAnalyticsLogEvent("THEME_CHANGED", bundleOf("Theme" to "Dark"))
        }
        llSOSTheme.setSafeOnClickListener {
            updateTheme(AppCompatDelegate.MODE_NIGHT_UNSPECIFIED)
            mActivity.setFirebaseAnalyticsLogEvent("THEME_CHANGED", bundleOf("Theme" to "SOS"))
        }

        clLanguage.setSafeOnClickListener {
            mDialogs.showLanguageSelectDialog(selLangPos){
                selLangPos = it
                updateSelLangUi()
                mActivity.restartHomeActivity()
            }
        }

        clSOSCountDown.setSafeOnClickListener {
            mDialogs.showCountDownSelectDialog(mPrefs.selectedSOSCountDownIndex){
                mPrefs.selectedSOSCountDownIndex = it
                updateSelCountDownUi()
            }
        }

        clCallPerm.setSafeOnClickListener {
            if (ContextCompat.checkSelfPermission(mActivity, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.CALL_PHONE), PERMISSIONS_REQUEST_CALL)
            }else updateCallPermUi()
        }

        clAboutUs.setSafeOnClickListener {
            mActivity.setFirebaseAnalyticsLogEvent("GO_TO_APP_NAV", bundleOf("Nav" to "Settings_To_About_Us"))
            navController.navigate(R.id.action_settings_to_about_us)
        }

        switchSOSAddress.setOnCheckedChangeListener { _, b ->
            mPrefs.sosAddressOn = b
        }

        switchSilent.setOnCheckedChangeListener { _, b ->
            mPrefs.sosSilentOn = b
        }

    }

    @SuppressLint("SetTextI18n")
    private fun FragmentSettingsBinding.updateSelLangUi(){
        tvLanguageDesc.text = SOSAppRes.getString(R.string.desc_current_language_colon) + " " + LocaleHelper.getLanguageByPosition(mActivity, selLangPos).second
    }

    @SuppressLint("SetTextI18n")
    private fun FragmentSettingsBinding.updateSelCountDownUi(){
        tvSOSCountDownDesc.text = SOSAppRes.getString(R.string.desc_current_countdown_time_colon) + " " + resources.getStringArray(R.array.arr_sos_countdowns)[mPrefs.selectedSOSCountDownIndex]
    }

    private fun FragmentSettingsBinding.updateCallPermUi(){
        val isGranted = ActivityHome.hasCallPermission(mActivity)
        if (isGranted) clCallPerm.disable() else clCallPerm.enable()
        tvCallPermDesc.text = SOSAppRes.getString(if (isGranted) R.string.desc_call_permission_granted else R.string.desc_call_permission)
    }

    private fun FragmentSettingsBinding.setSelectedTheme(){
        when(mPrefs.selectedThemeMode){
            AppCompatDelegate.MODE_NIGHT_NO -> {
                llSystemTheme.setBackgroundResource(R.color.colorTransparent)
                llDarkTheme.setBackgroundResource(R.color.colorTransparent)
                llSOSTheme.setBackgroundResource(R.color.colorTransparent)
                llLightTheme.setBackgroundResource(R.drawable.bg_variant_rounded_rectangle_theme)
            }
            AppCompatDelegate.MODE_NIGHT_YES -> {
                llSystemTheme.setBackgroundResource(R.color.colorTransparent)
                llLightTheme.setBackgroundResource(R.color.colorTransparent)
                llSOSTheme.setBackgroundResource(R.color.colorTransparent)
                llDarkTheme.setBackgroundResource(R.drawable.bg_variant_rounded_rectangle_theme)
            }
            AppCompatDelegate.MODE_NIGHT_UNSPECIFIED -> {
                llLightTheme.setBackgroundResource(R.color.colorTransparent)
                llDarkTheme.setBackgroundResource(R.color.colorTransparent)
                llSystemTheme.setBackgroundResource(R.color.colorTransparent)
                llSOSTheme.setBackgroundResource(R.drawable.bg_variant_rounded_rectangle_theme)
            }
            else -> {
                llLightTheme.setBackgroundResource(R.color.colorTransparent)
                llDarkTheme.setBackgroundResource(R.color.colorTransparent)
                llSOSTheme.setBackgroundResource(R.color.colorTransparent)
                llSystemTheme.setBackgroundResource(R.drawable.bg_variant_rounded_rectangle_theme)
            }
        }
    }

    private fun updateTheme(themeMode: Int){
        val prevTheme = mPrefs.selectedThemeMode
        mPrefs.selectedThemeMode = themeMode
        (mActivity as AppCompatActivity){
            AppCompatDelegate.setDefaultNightMode(themeMode)
            if (themeMode == AppCompatDelegate.MODE_NIGHT_UNSPECIFIED) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                recreate()
            } else {
                AppCompatDelegate.setDefaultNightMode(themeMode)
                delegate.applyDayNight()
                if (prevTheme == AppCompatDelegate.MODE_NIGHT_UNSPECIFIED) recreate()
            }
        }
        mDataBinding.setSelectedTheme()
    }

}