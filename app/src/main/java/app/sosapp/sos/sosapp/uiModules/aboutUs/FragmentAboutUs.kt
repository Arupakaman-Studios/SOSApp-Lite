package app.sosapp.sos.sosapp.uiModules.aboutUs

import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import app.sosapp.sos.sosapp.R
import app.sosapp.sos.sosapp.databinding.FragmentAboutUsBinding
import app.sosapp.sos.sosapp.uiModules.base.BaseFragment
import app.sosapp.sos.sosapp.uiModules.home.ActivityHome
import app.sosapp.sos.sosapp.uiModules.openContactMail
import app.sosapp.sos.sosapp.uiModules.openShareAppIntent
import app.sosapp.sos.sosapp.uiModules.openUrlInBrowser
import app.sosapp.sos.sosapp.utils.AppReviewUtil
import app.sosapp.sos.sosapp.utils.invoke
import app.sosapp.sos.sosapp.utils.setFirebaseAnalyticsCurrentScreen
import app.sosapp.sos.sosapp.utils.setFirebaseAnalyticsLogEvent
import app.sosapp.sos.sosapp.utils.setSafeOnClickListener

class FragmentAboutUs : BaseFragment<FragmentAboutUsBinding>(){

    companion object{
        private val TAG by lazy { "FragmentAboutUs" }
    }

    override val layoutId = R.layout.fragment_about_us

    private lateinit var navController: NavController

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mDataBinding {
            navController = findNavController()

            tvAboutDevMsg.movementMethod = LinkMovementMethod.getInstance()

            setViewListeners()

            mActivity.setFirebaseAnalyticsCurrentScreen("AboutUs")

        }
    }

    private fun FragmentAboutUsBinding.setViewListeners(){
        btnShareApp.setSafeOnClickListener {
            mActivity.openShareAppIntent()
        }

        btnRateApp.setSafeOnClickListener {
            AppReviewUtil.askForReview(mActivity)
        }

        btnMoreApps.setSafeOnClickListener {
            mActivity.setFirebaseAnalyticsLogEvent("GO_TO_APP_NAV", bundleOf("Nav" to "AboutUs_To_MoreApps"))
            (mActivity as ActivityHome).gotoSelItem(R.id.nav_graph_more_apps)
        }

        tvContactMail.setSafeOnClickListener {
            mActivity.openContactMail()
        }

        btnTranslate.isVisible = !mPrefs.translateUrl.isNullOrBlank()
        btnTranslate.setSafeOnClickListener {
            mActivity.openUrlInBrowser(mPrefs.translateUrl?:"")
        }

    }


}