package app.sosapp.sos.sosapp.utils

import android.app.Activity
import android.util.Log
import androidx.core.os.bundleOf
import app.sosapp.sos.sosapp.BuildConfig
import app.sosapp.sos.sosapp.data.SOSAppSharedPrefs
import app.sosapp.sos.sosapp.uiModules.openAppInPlayStore
import com.google.android.play.core.review.ReviewManagerFactory

object AppReviewUtil {

    fun askForReview(mActivity: Activity){
        val reviewManager = ReviewManagerFactory.create(mActivity)
        mActivity.setFirebaseAnalyticsLogEvent("ASK_FOR_REVIEW", bundleOf("Status" to "Asked"))
        reviewManager.requestReviewFlow().addOnCompleteListener { request ->
            mActivity.setFirebaseAnalyticsLogEvent("ASK_FOR_REVIEW", bundleOf("Status" to "request_${request.isSuccessful}"))
            if (request.isSuccessful) {
                //Received ReviewInfo object
                val reviewInfo = request.result
                Log.d("AppReviewUtil", "reviewInfo -> $reviewInfo")
                kotlin.runCatching {
                    val flow = reviewManager.launchReviewFlow(mActivity, reviewInfo)
                    flow.addOnCompleteListener {
                        mActivity.setFirebaseAnalyticsLogEvent("ASK_FOR_REVIEW", bundleOf("Status" to "flow_${it.isSuccessful}"))
                        Log.d("AppReviewUtil", "CompleteListener -> ${it.isSuccessful} ${it.exception}")
                        if (!it.isSuccessful) {
                            mActivity.openAppInPlayStore(BuildConfig.APPLICATION_ID)
                            mActivity.setFirebaseAnalyticsLogEvent("ASK_FOR_REVIEW", bundleOf("Status" to "manual"))
                        }else {
                            SOSAppSharedPrefs.getInstance(mActivity).reviewAsked = true
                        }
                    }
                }.onFailure {
                    mActivity.openAppInPlayStore(BuildConfig.APPLICATION_ID)
                    mActivity.setFirebaseAnalyticsLogEvent("ASK_FOR_REVIEW", bundleOf("Status" to "manual"))
                }
            }
        }
    }

}