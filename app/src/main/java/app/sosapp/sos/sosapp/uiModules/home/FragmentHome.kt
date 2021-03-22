package app.sosapp.sos.sosapp.uiModules.home

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import app.sosapp.sos.sosapp.R
import app.sosapp.sos.sosapp.databinding.FragmentHomeBinding
import app.sosapp.sos.sosapp.models.ModelEmergencyNumber
import app.sosapp.sos.sosapp.services.ServiceSOSFlash
import app.sosapp.sos.sosapp.services.ServiceSirenPlayer
import app.sosapp.sos.sosapp.uiModules.base.BaseFragment
import app.sosapp.sos.sosapp.uiModules.contactsPicker.FragmentContactsPicker
import app.sosapp.sos.sosapp.uiModules.editSOSMsg.FragmentEditSOSMessage
import app.sosapp.sos.sosapp.uiModules.editSOSNum.FragmentEditSOSNumbers
import app.sosapp.sos.sosapp.uiModules.goToAppSettings
import app.sosapp.sos.sosapp.uiModules.openCallIntent
import app.sosapp.sos.sosapp.utils.SOSAppRes
import app.sosapp.sos.sosapp.utils.invoke
import app.sosapp.sos.sosapp.utils.setFirebaseAnalyticsCurrentScreen
import app.sosapp.sos.sosapp.utils.setFirebaseAnalyticsLogEvent
import app.sosapp.sos.sosapp.utils.setSafeOnClickListener
import app.sosapp.sos.sosapp.utils.toast

class FragmentHome : BaseFragment<FragmentHomeBinding>(){

    companion object{
        private val TAG by lazy { "FragmentHome" }

    }

    override val layoutId = R.layout.fragment_home

    private lateinit var mAdapterEmgNum: AdapterEmergencyNumbers
    private lateinit var mAdapterSOSTools: AdapterSOSTools
    private lateinit var mRepoHome: RepoHome

    private lateinit var navController: NavController

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mDataBinding{

            mRepoHome = RepoHome(mActivity.applicationContext)
            navController = findNavController()

            updateSOSMsg()

            mAdapterEmgNum = AdapterEmergencyNumbers{
                mActivity.openCallIntent(it.number)
            }

            rvEmgNum.adapter = mAdapterEmgNum

            mAdapterSOSTools = AdapterSOSTools {
                mActivity.setFirebaseAnalyticsLogEvent("SOS_TOOLS", bundleOf("Tool_Index" to it))
                sosToolsClick(it)
            }

            rvSOSTools.adapter = mAdapterSOSTools

            setViewListeners()
            setDataObserver()

            updateEmgNumbers()
            mActivity.setFirebaseAnalyticsCurrentScreen("Home")
        }
    }

    override fun onDestroy() {
        if (::mRepoHome.isInitialized)
            mRepoHome.onClear()
        super.onDestroy()
    }

    private fun FragmentHomeBinding.updateEmgNumbers(){

        when{
            !mPrefs.emgNumSavedModels.isNullOrEmpty() -> setEmgNumUi(mPrefs.emgNumSavedModels)
            !mPrefs.emgNumModels.isNullOrEmpty() -> setEmgNumUi(mPrefs.emgNumModels)
            else -> mRepoHome.fetchLocalEmgNum()
        }
    }

    private fun FragmentHomeBinding.setViewListeners(){
        tvSOSMsgEdit.setSafeOnClickListener {
            navController.navigate(R.id.action_home_to_edit_sos_msg)
            mActivity.setFirebaseAnalyticsLogEvent("GO_TO_APP_NAV", bundleOf("Nav" to "Home_to_Edit_SOS_Msg"))
        }

        etSOSMsg.setSafeOnClickListener {
            navController.navigate(R.id.action_home_to_edit_sos_msg)
            mActivity.setFirebaseAnalyticsLogEvent("GO_TO_APP_NAV", bundleOf("Nav" to "Home_to_Edit_SOS_Msg_Et"))
        }

        tvEmgNumEdit.setSafeOnClickListener {
            navController.navigate(R.id.action_home_to_edit_sos_num)
            mActivity.setFirebaseAnalyticsLogEvent("GO_TO_APP_NAV", bundleOf("Nav" to "Home_to_Edit_SOS_Numbers"))
        }

    }

    private fun FragmentHomeBinding.updateSOSMsg(){
        mPrefs.sosMessage.let { msg->
            etSOSMsg.setText(if (msg.isNullOrBlank()) SOSAppRes.getString(R.string.msg_default_sos_msg) else msg)
        }
    }

    private fun setDataObserver(){
        mRepoHome.emgNumLD.observe(viewLifecycleOwner){
            mDataBinding.setEmgNumUi(it)
            mActivity.setFirebaseAnalyticsLogEvent("SOS_EMG_NUM", bundleOf("Status" to "Loaded_${it?.size?:0}"))
        }

        navController.currentBackStackEntry?.savedStateHandle?.let { savedStateHandle ->
            savedStateHandle.getLiveData<Boolean>(FragmentEditSOSMessage.EXTRA_KEY_SOS_MSG_SAVED).let { sosMsgStackLD ->
                sosMsgStackLD.observe(viewLifecycleOwner) {
                    if (it) {
                        mDataBinding.updateSOSMsg()
                        sosMsgStackLD.postValue(false)
                    }
                }
            }

            savedStateHandle.getLiveData<Boolean>(FragmentEditSOSNumbers.EXTRA_KEY_EMG_NUM_UPDATED).let { sosNumStackLD ->
                sosNumStackLD.observe(viewLifecycleOwner) {
                    if (it) {
                        mDataBinding.updateEmgNumbers()
                        sosNumStackLD.postValue(false)
                    }
                }
            }
        }
    }

    private fun FragmentHomeBinding.setEmgNumUi(numbers: List<ModelEmergencyNumber>?){
        if (!numbers.isNullOrEmpty()){
            mAdapterEmgNum.submit(ModelEmergencyNumber.fixIcons(numbers))
            tvEmgNumError.isVisible = false
            rvEmgNum.isVisible = true
        }else if (mAdapterEmgNum.itemCount <= 0){
            rvEmgNum.isVisible = false
            tvEmgNumError.isVisible = true
        }
    }

    private fun sosToolsClick(pos: Int){
        when(pos){
            0 -> ServiceSOSFlash.startOrStopService(mActivity, !ServiceSOSFlash.isRunning)
            1 -> ServiceSirenPlayer.startOrStopService(mActivity, !ServiceSirenPlayer.isRunning)
        }
    }

}