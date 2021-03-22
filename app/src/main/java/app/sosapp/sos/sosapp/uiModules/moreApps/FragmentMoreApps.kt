package app.sosapp.sos.sosapp.uiModules.moreApps

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import app.sosapp.sos.sosapp.R
import app.sosapp.sos.sosapp.databinding.FragmentMoreAppsBinding
import app.sosapp.sos.sosapp.models.ModelAppInfo
import app.sosapp.sos.sosapp.models.ModelArupakamanApp
import app.sosapp.sos.sosapp.uiModules.base.BaseFragment
import app.sosapp.sos.sosapp.uiModules.openAppInPlayStore
import app.sosapp.sos.sosapp.uiModules.openUrlInBrowser
import app.sosapp.sos.sosapp.utils.SOSAppRes
import app.sosapp.sos.sosapp.utils.invoke
import app.sosapp.sos.sosapp.utils.setFirebaseAnalyticsCurrentScreen
import app.sosapp.sos.sosapp.utils.setFirebaseAnalyticsLogEvent

class FragmentMoreApps : BaseFragment<FragmentMoreAppsBinding>(){

    companion object{
        private val TAG by lazy { "FragmentMoreApps" }
    }

    override val layoutId = R.layout.fragment_more_apps

    private lateinit var navController: NavController

    private lateinit var mAdapterApps: AdapterArupakamanApps
    private lateinit var mRepoMoreApps: RepoMoreApps

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mDataBinding{
            navController = findNavController()
            mRepoMoreApps = RepoMoreApps(mActivity.applicationContext)

            mAdapterApps = AdapterArupakamanApps {
                mActivity.setFirebaseAnalyticsLogEvent("GO_TO_MORE_APP", bundleOf("App" to it.pkgName))
                if (it.fDroid) mActivity.openUrlInBrowser(it.url?:"")
                else mActivity.openAppInPlayStore(it.pkgName?:"Arupakaman+Studios")
            }
            rvMoreApps.adapter = mAdapterApps

            setDataObservers()

            swipeRefreshLayout.setOnRefreshListener {
                mRepoMoreApps.fetchMoreApps()
            }

            if (mAdapterApps.itemCount <= 0 && mPrefs.arupakamanApps.isNullOrEmpty()) {
                mDialogs.showProgressDialog(SOSAppRes.getString(R.string.msg_loading_apps))
                mRepoMoreApps.fetchMoreApps()
            }else setAppsList(mPrefs.arupakamanApps)

            mActivity.setFirebaseAnalyticsCurrentScreen("MoreApps")
        }
    }

    private fun FragmentMoreAppsBinding.setDataObservers(){
        mRepoMoreApps.moreAppsLD.observe(viewLifecycleOwner){
            mDialogs.dismissProgress()
            setAppsList(it)
            if (!it.isNullOrEmpty()) mPrefs.arupakamanApps = it
        }
    }

    private fun FragmentMoreAppsBinding.setAppsList(it: List<ModelArupakamanApp>?){
        swipeRefreshLayout.isRefreshing = false
        if (it.isNullOrEmpty()){
            rvMoreApps.isVisible = false
            tvMoreAppsError.isVisible = true
        }else{
            mAdapterApps.submit(it)
            tvMoreAppsError.isVisible = false
            rvMoreApps.isVisible = true
        }
    }



}