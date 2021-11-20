package app.sosapp.sos.sosapp.uiModules

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.ContactsContract
import android.provider.Settings
import android.telephony.PhoneNumberUtils
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import app.sosapp.sos.sosapp.BuildConfig
import app.sosapp.sos.sosapp.R
import app.sosapp.sos.sosapp.models.ModelContact
import app.sosapp.sos.sosapp.uiModules.home.ActivityHome
import app.sosapp.sos.sosapp.uiModules.sendSOS.ActivitySendSOS
import app.sosapp.sos.sosapp.utils.SOSAppRes
import app.sosapp.sos.sosapp.utils.reportException
import app.sosapp.sos.sosapp.utils.setFirebaseAnalyticsLogEvent
import app.sosapp.sos.sosapp.utils.toast
import java.util.*


fun Context.restartHomeActivity(){
    startActivity(Intent(this, ActivityHome::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
    })
}

fun Context.gotoSendSOSActivity(model: ModelContact? = null){
    startActivity(ActivitySendSOS.getIntent(this, model))
}

/*fun Fragment.openSelectContactsIntent(requestCode: Int){
    Intent(Intent.ACTION_PICK).let { contactsIntent->
        contactsIntent.type = ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE
        if (contactsIntent.resolveActivity(requireContext().packageManager) != null){
            startActivityForResult(contactsIntent, requestCode)
        }else{
            requireContext().toast(SOSAppRes.getString(R.string.err_msg_no_contacts_app))
        }
    }
}*/

fun Context.goToAppSettings() {
    kotlin.runCatching {
        setFirebaseAnalyticsLogEvent("GO_TO_NAV", bundleOf("Nav" to "System_App_Settings"))
        Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", packageName, null)
        ).apply {
            addCategory(Intent.CATEGORY_DEFAULT)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }.also { startActivity(it) }
    }.onFailure {
        it.reportException("GO_TO_NAV System_App_Settings")
    }
}

fun Context.openCallIntent(number: String) {
    kotlin.runCatching {
        @Suppress("DEPRECATION")
        val isEmg = PhoneNumberUtils.isEmergencyNumber(number)
        setFirebaseAnalyticsLogEvent("GO_TO_NAV", bundleOf("Nav" to "Call_Screen", "isEmgNum" to isEmg))
        Log.d("openCallIntent", "isEmg $isEmg $number")
        val callIntent = Intent.createChooser(
            Intent((if (!isEmg && ActivityHome.hasCallPermission(this)) Intent.ACTION_CALL else Intent.ACTION_DIAL)).apply {
                data = Uri.parse("tel:$number")
            },
            SOSAppRes.getString(R.string.call_via))
        if (callIntent.resolveActivity(packageManager) != null) {
            startActivity(callIntent)
        }else{
            toast(SOSAppRes.getString(R.string.err_msg_general))
        }
    }.onFailure {
        Log.e("openCallIntent", "Exc : ", it)
        it.reportException("GO_TO_NAV Call_Screen")
        toast(SOSAppRes.getString(R.string.err_msg_general))
    }
}

/*fun Context.openMapIntent(lat: Double, lon: Double){
    val uri: String = String.format(Locale.ENGLISH, "http://maps.google.com/maps?q=loc:%f,%f", lat, lon)
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
    kotlin.runCatching {
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        }else{
            toast(SOSAppRes.getString(R.string.err_msg_general))
        }
    }.onFailure {
        toast(SOSAppRes.getString(R.string.err_msg_general))
    }
}*/

fun Context.openContactMail(msg: String? = null){
    kotlin.runCatching {
        setFirebaseAnalyticsLogEvent("GO_TO_NAV", bundleOf("Nav" to "Contact_Mail"))
        Intent(Intent.ACTION_SENDTO).let { emailIntent ->
            emailIntent.data = Uri.parse("mailto:")
            emailIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf(getString(R.string.email_publisher)))
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name))
            emailIntent.putExtra(Intent.EXTRA_TEXT, msg ?: SOSAppRes.getString(R.string.msg_enter_your_message))
            val packageManager = packageManager

            if (emailIntent.resolveActivity(packageManager) != null) {
                startActivity(Intent.createChooser(emailIntent, SOSAppRes.getString(R.string.title_send_via)))
            }
        }
    }.onFailure {
        it.reportException("GO_TO_NAV Contact_Mail")
    }
}

fun Context.openShareAppIntent(){
    kotlin.runCatching {
        setFirebaseAnalyticsLogEvent("GO_TO_NAV", bundleOf("Nav" to "Share_SOSApp"))
        Intent(Intent.ACTION_SEND).let { shareIntent ->
            shareIntent.type = "text/plain"
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name))
            var shareMessage = "\n${SOSAppRes.getString(R.string.msg_share_sosapp)}"
            shareMessage = "$shareMessage https://play.google.com/store/apps/details?id=${BuildConfig.APPLICATION_ID}"
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage)
            startActivity(Intent.createChooser(shareIntent, SOSAppRes.getString(R.string.title_share_via)))
        }
    }.onFailure {
        it.reportException("GO_TO_NAV Share_SOSApp")
        Log.e("openShareAppIntent", "shareApp Exc : $it")
    }
}

fun Context.openAppInPlayStore(id: String){
    setFirebaseAnalyticsLogEvent("GO_TO_NAV", bundleOf("Nav" to "OpenAppInPlayStore_$id"))
    val optionalIntent =  Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://play.google.com/store/apps/details?id=$id")
    )
    kotlin.runCatching {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$id"))
        if (intent.resolveActivity(packageManager) != null) startActivity(intent)
        else startActivity(optionalIntent)
    }.onFailure {
        it.reportException("GO_TO_NAV OpenAppInPlayStore_$id")
        startActivity(optionalIntent)
    }
}

fun Context.openUrlInBrowser(url: String){
    setFirebaseAnalyticsLogEvent("GO_TO_NAV", bundleOf("Nav" to "OpenUrlInBrowser_$url"))
    val intent =  Intent(Intent.ACTION_VIEW, Uri.parse(url))
    kotlin.runCatching {
        if (intent.resolveActivity(packageManager) != null) startActivity(intent)
    }.onFailure {
        it.reportException("GO_TO_NAV OpenUrlInBrowser_$url")
    }
}

fun Context.openArupakamanPlayStore(){
    val intent =  Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/developer?id=Arupakaman+Studios"))
    kotlin.runCatching {
        if (intent.resolveActivity(packageManager) != null) startActivity(intent)
    }
}

