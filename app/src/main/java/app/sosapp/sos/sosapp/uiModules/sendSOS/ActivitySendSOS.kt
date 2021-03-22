package app.sosapp.sos.sosapp.uiModules.sendSOS

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioManager
import android.os.Bundle
import android.os.CountDownTimer
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.KeyEvent
import androidx.core.app.ActivityCompat
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import app.sosapp.sos.sosapp.R
import app.sosapp.sos.sosapp.broadcastReceivers.BroadcastReceiverSMSSender
import app.sosapp.sos.sosapp.databinding.ActivitySendSOSBinding
import app.sosapp.sos.sosapp.models.ModelContact
import app.sosapp.sos.sosapp.uiModules.base.BaseAppCompatActivity
import app.sosapp.sos.sosapp.uiModules.contactsPicker.FragmentContactsPicker
import app.sosapp.sos.sosapp.uiModules.goToAppSettings
import app.sosapp.sos.sosapp.uiModules.home.ActivityHome
import app.sosapp.sos.sosapp.uiModules.openCallIntent
import app.sosapp.sos.sosapp.utils.*
import com.google.android.gms.location.LocationRequest

class ActivitySendSOS : BaseAppCompatActivity() {

    companion object{
        private val TAG by lazy { "ActivitySendSOS" }

        private const val EXTRA_KEY_CONTACT_MODEL = "modelContactCustom"
        fun getIntent(mContext: Context, mModel: ModelContact? = null) =
                Intent(mContext, ActivitySendSOS::class.java).apply {
                    putExtra(EXTRA_KEY_CONTACT_MODEL, mModel)
                }

        fun getCountDownTimeByIndex(index: Int) = when(index){
            0 -> 0L
            2 -> 5000L
            3 -> 10000L
            else -> 3000L
        }

    }

    private val mDataBinding by binding<ActivitySendSOSBinding>(R.layout.activity_send_s_o_s)
    private lateinit var mLocationUtil: LocationUtil

    private var mTimer: CountDownTimer? = null

    private var mModelContact: ModelContact? = null
    private lateinit var mSOSContacts: List<ModelContact>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mDataBinding{

            mLocationUtil = LocationUtil(this@ActivitySendSOS)

            mModelContact = intent?.getParcelableExtra(EXTRA_KEY_CONTACT_MODEL)
            mSOSContacts = mPrefs.sosContactsModels

            tvSOSSentMsg.movementMethod = ScrollingMovementMethod()

            mTimer = object : CountDownTimer(
                    getCountDownTimeByIndex(mPrefs.selectedSOSCountDownIndex),
                    1000
            ){
                override fun onTick(p0: Long) {
                    tvSOSCountDown.text = (p0.toInt() / 1000).toString()
                }

                override fun onFinish() {
                    mTimer = null
                    sendSOS()
                }

            }
            mTimer?.start()

            setViewListeners()

            setFirebaseAnalyticsCurrentScreen("SendSOS")

            if (mModelContact == null && mSOSContacts.isEmpty()){
                setFirebaseAnalyticsLogEvent("SEND_SOS", bundleOf("Action" to "No SOS Contacts"))
                toast(SOSAppRes.getString(R.string.err_msg_no_sos_contacts))
                finish()
            }

        }
    }

    override fun onBackPressed() {
        if (!mDataBinding.tvSOSCountDown.isVisible){
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        mTimer?.cancel()
        super.onDestroy()
    }

    override fun onResume() {
        super.onResume()
        if (mDialogs.isShowingDialog) {
            mDialogs.showCommonConfirmationDialog(
                    SOSAppRes.getString(R.string.action_retry),
                    SOSAppRes.getString(R.string.msg_retry_sending_sos),
                    null,
                    SOSAppRes.getString(R.string.action_retry),
                    rBtnClick = {
                        setFirebaseAnalyticsLogEvent("SEND_SOS", bundleOf("Action" to "Retry_onResume"))
                        mDataBinding.sendSOS()
                    })
        }
    }

    private fun ActivitySendSOSBinding.setViewListeners(){

        btnCancel.setSafeOnClickListener {
            setFirebaseAnalyticsLogEvent("SEND_SOS", bundleOf("Action" to "Send_Cancel"))
            mTimer?.cancel()
            finish()
        }

        btnSendNow.setSafeOnClickListener {
            setFirebaseAnalyticsLogEvent("SEND_SOS", bundleOf("Action" to "Send_Now"))
            mTimer?.cancel()
            sendSOS()
        }

        btnCallPolice.setSafeOnClickListener {
            setFirebaseAnalyticsLogEvent("SEND_SOS", bundleOf("Action" to "Call_Police"))
            openCallIntent(mPrefs.policeNumber?:"191")
        }

    }

    private fun ActivitySendSOSBinding.sendSOS(){

        tvSOSSendingMsg.isVisible = false
        tvSOSCountDown.isVisible = false
        btnCancel.isVisible = false
        btnSendNow.isVisible = false

        val locPerm = ActivityHome.hasSMSPermission(this@ActivitySendSOS)
        setFirebaseAnalyticsLogEvent("SEND_SOS_PERMISSION", bundleOf("Location" to locPerm))
        if (locPerm) {
            val smsPerm = LocationUtil.hasLocationPermissions(this@ActivitySendSOS)
            setFirebaseAnalyticsLogEvent("SEND_SOS_PERMISSION", bundleOf("SMS" to smsPerm))
            if (smsPerm) {
                val locEnabled = LocationUtil.isLocationServiceEnabled(this@ActivitySendSOS)
                setFirebaseAnalyticsLogEvent("SEND_SOS_PERMISSION", bundleOf("Location_Service" to "Enabled_$locEnabled"))
                if (locEnabled) {
                    mDialogs.showProgressDialog(SOSAppRes.getString(R.string.msg_getting_location))
                    mLocationUtil.getSOSMessage(mPrefs) { msg ->
                        mDialogs.dismissProgress()
                        if (msg.isNullOrBlank()) {
                            showGeneralErrorDialog()
                        } else {
                            sendingSOS(msg)
                        }
                    }
                } else {
                    mLocationUtil.checkAndRequestGPS {
                        Log.e(TAG, "checkAndRequestGPS Exc -> $it")
                        if (it != null) showGpsDialog()
                    }
                }
            } else {
                ActivityCompat.requestPermissions(this@ActivitySendSOS, LocationUtil.locPermissions, LocationUtil.PERMISSION_REQUEST_CODE_LOCATION)
            }
        }else{
            ActivityCompat.requestPermissions(this@ActivitySendSOS, arrayOf(Manifest.permission.SEND_SMS), ActivityHome.REQUEST_PERMISSION_CODE_SMS)
        }

    }

    @SuppressLint("SetTextI18n")
    private fun ActivitySendSOSBinding.sendingSOS(msg: String){

        if (mPrefs.sosSilentOn) {
            kotlin.runCatching {
                (getSystemService(Context.AUDIO_SERVICE) as AudioManager?)?.let { am ->
                    am.ringerMode = AudioManager.RINGER_MODE_SILENT
                }
            }.onFailure {
                it.reportException("SOS_SILENT_ON_SOS")
            }
        }

        Log.d(TAG, "Message -> $msg")
        setFirebaseAnalyticsLogEvent("SEND_SOS", bundleOf("Action" to "Sending_Now"))

        val names: String
        val phones = arrayListOf<String>()
        if (mModelContact != null){
            names = mModelContact?.contactName?:""
            phones.add(mModelContact?.contactNumber?:"")
        }else{
            names = mSOSContacts.joinToString(separator = "\n") { it.contactName }
            phones.addAll(mSOSContacts.map { it.contactNumber })
        }

        phones.forEach { phone->
            BroadcastReceiverSMSSender.sendSMS(this@ActivitySendSOS, phone, msg)
        }
        setFirebaseAnalyticsLogEvent("SEND_SOS", bundleOf("Action" to "Sent_To_${phones.size}"))

        tvBackPressWarning.text = SOSAppRes.getString(R.string.msg_warning_msg_sent)
        tvSOSSentMsg.text = SOSAppRes.getString(R.string.msg_sos_sent_to) + "\n\n" + names

        tvSOSSentMsg.isVisible = true
        btnCallPolice.isVisible = true

        mPrefs.policeNumber.let { policeNum->
            if (policeNum.isNullOrBlank()) btnCallPolice.disable() else btnCallPolice.enable()
        }

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when(requestCode){
            LocationUtil.PERMISSION_REQUEST_CODE_LOCATION -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mDataBinding.sendSOS()
                } else {
                    val rational = !ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION) ||
                            !ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    mDialogs.showCommonConfirmationDialog(
                            SOSAppRes.getString(R.string.title_permission_required),
                            SOSAppRes.getString(if (rational) R.string.msg_location_permission_required_rational else R.string.msg_location_permission_required),
                            null,
                            SOSAppRes.getString(R.string.action_grant_now),
                            rBtnClick = {
                                if (rational){
                                    goToAppSettings()
                                }else{
                                    mDataBinding.sendSOS()
                                }
                                setFirebaseAnalyticsLogEvent("SEND_SOS", bundleOf("Action" to "Loc_Perm_Grant_Clk",
                                        "Rational" to rational))
                            })
                }
            }
            ActivityHome.REQUEST_PERMISSION_CODE_SMS -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mDataBinding.sendSOS()
                } else {
                    val rational = !ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.SEND_SMS)
                    mDialogs.showCommonConfirmationDialog(
                            SOSAppRes.getString(R.string.title_permission_required),
                            SOSAppRes.getString(if (rational) R.string.msg_sms_permission_required_rational else R.string.msg_sms_permission_required),
                            null,
                            SOSAppRes.getString(R.string.action_grant_now),
                            rBtnClick = {
                                if (rational){
                                    goToAppSettings()
                                }else{
                                    mDataBinding.sendSOS()
                                }
                                setFirebaseAnalyticsLogEvent("SEND_SOS", bundleOf("Action" to "SMS_Perm_Grant_Clk",
                                        "Rational" to rational))
                            })
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode){
            LocationRequest.PRIORITY_HIGH_ACCURACY -> {
                if (resultCode == Activity.RESULT_OK){
                    mDataBinding.sendSOS()
                    setFirebaseAnalyticsLogEvent("SEND_SOS", bundleOf("Action" to "GPS_Turned_On"))
                }else {
                    showGpsDialog()
                    setFirebaseAnalyticsLogEvent("SEND_SOS", bundleOf("Action" to "GPS_Turned_On_Failed"))
                }
            }
        }
    }

    private fun showGpsDialog(){
        mDialogs.showCommonConfirmationDialog(
                SOSAppRes.getString(R.string.title_location_service_required),
                SOSAppRes.getString(R.string.msg_location_service_enabled_required),
                null,
                SOSAppRes.getString(R.string.action_retry),
                rBtnClick = {
                    mDataBinding.sendSOS()
                })
    }

    private fun showGeneralErrorDialog(){
        setFirebaseAnalyticsLogEvent("SEND_SOS", bundleOf("Action" to "Some_Error_Occured"))
        mDialogs.showCommonConfirmationDialog(
                SOSAppRes.getString(R.string.title_error),
                SOSAppRes.getString(R.string.err_msg_general),
                null,
                SOSAppRes.getString(R.string.action_retry),
                rBtnClick = {
                    mDataBinding.sendSOS()
                })
    }

}