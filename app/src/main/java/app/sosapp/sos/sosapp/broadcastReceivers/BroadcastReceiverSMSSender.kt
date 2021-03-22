package app.sosapp.sos.sosapp.broadcastReceivers

import android.app.Activity
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Handler
import android.os.Looper
import android.telephony.SmsManager
import android.util.Log
import androidx.core.os.bundleOf
import app.sosapp.sos.sosapp.utils.FirebaseReporterUtil
import app.sosapp.sos.sosapp.utils.reportException
import app.sosapp.sos.sosapp.utils.setFirebaseAnalyticsLogEvent


class BroadcastReceiverSMSSender: BroadcastReceiver() {

    companion object{
        private const val SENT_SMS_ACTION_NAME = "SMS_SENT"
        private const val DELIVERED_SMS_ACTION_NAME = "SMS_DELIVERED"

        private val TAG by lazy { "SMSSender" }

        private fun canSendSMS(mContext: Context): Boolean {
            return mContext.packageManager.hasSystemFeature(PackageManager.FEATURE_TELEPHONY)
        }

        fun sendSMS(mContext: Context, phoneNumber: String?, message: String?) {
            if (!canSendSMS(mContext)) {
                Log.d(TAG, "Device can\'t send SMS!")
                FirebaseReporterUtil.reportException("Device can\'t send SMS!")
                return
            }
            val sentPI = PendingIntent.getBroadcast(mContext, 0, Intent(SENT_SMS_ACTION_NAME), 0)
            val deliveredPI = PendingIntent.getBroadcast(mContext, 0, Intent(DELIVERED_SMS_ACTION_NAME), 0)
            val smsUtils = BroadcastReceiverSMSSender()
            //register for sending and delivery
            mContext.registerReceiver(smsUtils, IntentFilter(SENT_SMS_ACTION_NAME))
            mContext.registerReceiver(smsUtils, IntentFilter(DELIVERED_SMS_ACTION_NAME))

            val sms = SmsManager.getDefault()
            val parts = sms.divideMessage(message)

            val sendList: ArrayList<PendingIntent> = ArrayList()
            sendList.add(sentPI)

            val deliverList: ArrayList<PendingIntent> = ArrayList()
            deliverList.add(deliveredPI)

            sms.sendMultipartTextMessage(phoneNumber, null, parts, sendList, deliverList)

            //we unsubscribed in 10 seconds
            Handler(Looper.getMainLooper()).postDelayed({
                try {
                    mContext.unregisterReceiver(smsUtils)
                } catch (e: IllegalStateException) {
                    e.reportException("SMS Sender Handler Error")
                }
            }, 10000)
        }

    }

    override fun onReceive(p0: Context?, p1: Intent?) {
        if (p1?.action == SENT_SMS_ACTION_NAME) {
            p0?.setFirebaseAnalyticsLogEvent("SOS_SMS_Sent", bundleOf("Status" to "Result_Code_$resultCode"))
            when (resultCode) {
                Activity.RESULT_OK -> {
                    Log.d(TAG, "SMS Sent")
                }
                SmsManager.RESULT_ERROR_GENERIC_FAILURE -> {
                    Log.d(TAG, "SMS Not Sent")
                }
                SmsManager.RESULT_ERROR_NO_SERVICE -> {
                    Log.d(TAG, "SMS Not Sent No Service!")
                }
                SmsManager.RESULT_ERROR_NULL_PDU -> {
                    Log.d(TAG, "SMS Not Sent")
                }
                SmsManager.RESULT_ERROR_RADIO_OFF -> {
                    Log.d(TAG, "SMS Not Sent")
                }
                else -> {
                    Log.d(TAG, "SMS Not Sent")
                }
            }
        }else if (p1?.action == DELIVERED_SMS_ACTION_NAME){
            p0?.setFirebaseAnalyticsLogEvent("SOS_SMS_Delivered", bundleOf("Status" to "Result_Code_$resultCode"))
            when (resultCode) {
                Activity.RESULT_OK -> {
                    Log.d(TAG, "SMS Delivered")
                }
                Activity.RESULT_CANCELED -> {
                    Log.d(TAG, "SMS Not Delivered")
                }
            }
        }
    }

}