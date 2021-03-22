package app.sosapp.sos.sosapp.uiModules.home

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.lifecycle.LiveData
import androidx.navigation.NavController
import androidx.navigation.ui.NavigationUI.setupActionBarWithNavController
import androidx.navigation.ui.setupActionBarWithNavController
import app.sosapp.sos.sosapp.BuildConfig
import app.sosapp.sos.sosapp.R
import app.sosapp.sos.sosapp.databinding.ActivityHomeBinding
import app.sosapp.sos.sosapp.uiModules.base.BaseAppCompatActivity
import app.sosapp.sos.sosapp.uiModules.gotoSendSOSActivity
import app.sosapp.sos.sosapp.uiModules.moreApps.RepoMoreApps
import app.sosapp.sos.sosapp.uiModules.openAppInPlayStore
import app.sosapp.sos.sosapp.uiModules.openShareAppIntent
import app.sosapp.sos.sosapp.uiModules.openUrlInBrowser
import app.sosapp.sos.sosapp.utils.*

class ActivityHome : BaseAppCompatActivity() {

    companion object{
        private val TAG by lazy { "ActivityHome" }

        fun hasCallPermission(mContext: Context) = ContextCompat.checkSelfPermission(
                mContext,
                Manifest.permission.CALL_PHONE
        ) == PackageManager.PERMISSION_GRANTED

        fun hasSMSPermission(mContext: Context) = ContextCompat.checkSelfPermission(
                mContext,
                Manifest.permission.SEND_SMS
        ) == PackageManager.PERMISSION_GRANTED

        const val REQUEST_PERMISSION_CODE_SMS = 1007
        const val REQUEST_PERMISSION_CODE_SMS_LOCATION = 1008

    }

    private val mDataBinding by binding<ActivityHomeBinding>(R.layout.activity_home)
    private var currentNavController: LiveData<NavController>? = null
    private lateinit var mRepoHome: RepoHome
    private lateinit var mRepoMoreApps: RepoMoreApps

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mRepoHome = RepoHome(applicationContext)
        mRepoMoreApps = RepoMoreApps(applicationContext)

        mDataBinding{
            setSupportActionBar(includeAppBar.toolbar)
            if (savedInstanceState == null) {
                setupBottomNavigationBar()
            }
        }

        mDataBinding.includeSOSBtn.cardSOS.setSafeOnClickListener {
            if (mPrefs.sosContactsModels.isEmpty()){
                toast(SOSAppRes.getString(R.string.err_msg_no_sos_contacts))
                gotoSelItem(R.id.nav_graph_sos_contacts)
                setFirebaseAnalyticsLogEvent("SEND_SOS", bundleOf("SOS_Contacts" to "Empty"))
            }else{
                mDialogs.showCommonConfirmationDialog(
                        SOSAppRes.getString(R.string.title_send_sos),
                        SOSAppRes.getString(R.string.msg_send_sos_to_sos_contacts),
                        SOSAppRes.getString(R.string.action_cancel),
                        SOSAppRes.getString(R.string.action_send_now),
                        lBtnClick = {
                            mDialogs.dismiss()
                        },
                        rBtnClick = {
                            mDialogs.dismiss()
                            setFirebaseAnalyticsLogEvent("SEND_SOS", bundleOf("SOS_Contacts" to "Sending"))
                            gotoSendSOSActivity()
                        })
            }
        }

        checkPermissions()

        mRepoHome.appInfoFetchLD.observe(this){
            if (it != null && it.currentVersion > BuildConfig.VERSION_CODE){
                setFirebaseAnalyticsLogEvent("APP_UPDATE", bundleOf("Available" to "Yes",
                        "this_version" to BuildConfig.VERSION_CODE,
                        "curr_version" to it.currentVersion,
                        "critical_version" to it.criticalVersion))
                mDialogs.showCommonConfirmationDialog(
                        SOSAppRes.getString(R.string.title_update_available),
                        SOSAppRes.getString(R.string.msg_update_available) + if (it.whatsNew.isNullOrBlank()) "" else "\n\n${it.whatsNew}",
                        if (BuildConfig.VERSION_CODE <= it.criticalVersion) null else SOSAppRes.getString(R.string.action_later),
                        SOSAppRes.getString(R.string.action_update_now),
                        lBtnClick = {
                            setFirebaseAnalyticsLogEvent("APP_UPDATE", bundleOf("Action" to "Later"))
                            mDialogs.dismiss()
                        },
                        rBtnClick = {
                            setFirebaseAnalyticsLogEvent("APP_UPDATE", bundleOf("Action" to "Update_Now"))
                            openAppInPlayStore(BuildConfig.APPLICATION_ID)
                        })
            }else if (mPrefs.sosContactsModels.isEmpty()){
                mDialogs.showCommonConfirmationDialog(
                        SOSAppRes.getString(R.string.title_sos_contacts),
                        SOSAppRes.getString(R.string.err_msg_no_sos_contacts),
                        null,
                        SOSAppRes.getString(R.string.action_add_sos_contacts),
                        rBtnClick = {
                            mDialogs.dismiss()
                            gotoSelItem(R.id.nav_graph_sos_contacts)
                        })
            }
        }

        showSupportDialog()
        mRepoHome.pushInstallEvent()

    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        mDataBinding.setupBottomNavigationBar()
    }

    override fun onSupportNavigateUp(): Boolean {
        return currentNavController?.value?.navigateUp() ?: false
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_home, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.menuItemShare -> openShareAppIntent()
            R.id.menuItemRate -> AppReviewUtil.askForReview(this)
            R.id.menuItemWebsite -> openUrlInBrowser(getString(R.string.url_sosapp_dot_in))
        }
        return super.onOptionsItemSelected(item)
    }

    fun gotoSelItem(id: Int){
        mDataBinding.bottomNavigationView.selectedItemId = id
    }

    /**
     * Called on first creation and when restoring state.
     */
    private fun ActivityHomeBinding.setupBottomNavigationBar() {
        val navGraphIds = listOf(
                R.navigation.nav_graph_home,
                R.navigation.nav_graph_sos_contacts,
                R.navigation.nav_graph_settings,
                R.navigation.nav_graph_more_apps)

        // Setup the bottom navigation view with a list of navigation graphs
        val controller = bottomNavigationView.setupWithNavController(
                navGraphIds = navGraphIds,
                fragmentManager = supportFragmentManager,
                containerId = R.id.navFragmentContainer,
                intent = intent
        )

        // Whenever the selected controller changes, setup the action bar.
        controller.observe(this@ActivityHome, { navController ->
            setupActionBarWithNavController(navController)
            navController.addOnDestinationChangedListener { _, destination, _ ->

            }
            if (navController.graph.id == R.id.nav_graph_more_apps){
                val badgeDrawable = bottomNavigationView.getBadge(R.id.nav_graph_more_apps)
                if (badgeDrawable != null) {
                    mPrefs.moreAppsBadgeCount = 0
                    badgeDrawable.isVisible = false
                    badgeDrawable.clearNumber()
                }
            }
        })

        currentNavController = controller

        fun showBadge(count: Int){
            if (count > 0) {
                val badge = bottomNavigationView.getOrCreateBadge(R.id.nav_graph_more_apps)
                badge.isVisible = true
                badge.number = count
            }
        }

        showBadge(mPrefs.moreAppsBadgeCount)

        mRepoMoreApps.moreAppsLD.observe(this@ActivityHome){
            val mSize = mPrefs.arupakamanApps.size
            if (!it.isNullOrEmpty() && it.size > mSize){
                setFirebaseAnalyticsLogEvent("MORE_APPS", bundleOf("New_Count" to "${it.size - mSize}"))
                mPrefs.moreAppsBadgeCount = it.size - mSize
                showBadge(it.size - mSize)
            }
            if (!it.isNullOrEmpty()) mPrefs.arupakamanApps = it
        }
        mRepoMoreApps.fetchMoreApps()
    }

    private fun checkPermissions(){
        if (!hasSMSPermission(this) || !LocationUtil.hasLocationPermissions(this)){
            mDialogs.showCommonConfirmationDialog(
                    SOSAppRes.getString(R.string.title_permission_required),
                    SOSAppRes.getString(R.string.msg_location_sms_permissions_required),
                    null,
                    SOSAppRes.getString(R.string.action_grant_now),
                    rBtnClick = {
                        mDialogs.dismiss()
                        setFirebaseAnalyticsLogEvent("PERMISSION_HOME", bundleOf("Action" to "Grant_Now"))
                        ActivityCompat.requestPermissions(this,
                                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION,
                                        Manifest.permission.SEND_SMS),
                                REQUEST_PERMISSION_CODE_SMS_LOCATION)
                    })
            setFirebaseAnalyticsLogEvent("PERMISSION_HOME", bundleOf("Location" to "${LocationUtil.hasLocationPermissions(this)}",
            "SMS" to "${hasSMSPermission(this)}"))
        }else if (!LocationUtil.isLocationServiceEnabled(this)){
            mDialogs.showCommonConfirmationDialog(
                    SOSAppRes.getString(R.string.title_location_service_required),
                    SOSAppRes.getString(R.string.msg_location_service_enabled_required),
                    SOSAppRes.getString(R.string.action_cancel),
                    SOSAppRes.getString(R.string.action_enable),
                    lBtnClick = {
                        mDialogs.dismiss()
                    },
                    rBtnClick = {
                        mDialogs.dismiss()
                        LocationUtil(this).checkAndRequestGPS {}
                    })
        } else {
            setFirebaseAnalyticsLogEvent("APP_INFO", bundleOf("Action" to "Fetch"))
            mRepoHome.fetchAppInfo()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_PERMISSION_CODE_SMS_LOCATION -> checkPermissions()
        }
    }

    private fun showSupportDialog(){
        val count = mPrefs.supportDialogCount
        val asked = mPrefs.reviewAsked
        if ((!asked && count == 30) || (asked && count == 15)){
            if (!mDialogs.isShowingDialog) {
                setFirebaseAnalyticsLogEvent("SUPPORT_US", bundleOf("Action" to "Shown"))
                mDialogs.showCommonConfirmationDialog(
                        SOSAppRes.getString(R.string.title_support_us),
                        SOSAppRes.getString(R.string.msg_support_us),
                        SOSAppRes.getString(R.string.title_share),
                        SOSAppRes.getString(R.string.title_rate),
                        isCancelable = true,
                        lBtnClick = {
                            mDialogs.dismiss()
                            setFirebaseAnalyticsLogEvent("SUPPORT_US", bundleOf("Action" to "Share"))
                            openShareAppIntent()
                        },
                        rBtnClick = {
                            mDialogs.dismiss()
                            setFirebaseAnalyticsLogEvent("SUPPORT_US", bundleOf("Action" to "Rate"))
                            AppReviewUtil.askForReview(this)
                        })
                mPrefs.supportDialogCount = 0
            }
        }else mPrefs.supportDialogCount = mPrefs.supportDialogCount + 1
    }

}