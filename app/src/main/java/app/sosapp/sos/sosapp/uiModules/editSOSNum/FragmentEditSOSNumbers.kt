package app.sosapp.sos.sosapp.uiModules.editSOSNum

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.os.bundleOf
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import app.sosapp.sos.sosapp.R
import app.sosapp.sos.sosapp.databinding.FragmentEditSosNumbersBinding
import app.sosapp.sos.sosapp.models.ModelContact
import app.sosapp.sos.sosapp.models.ModelEmergencyNumber
import app.sosapp.sos.sosapp.models.ModelEmergencyNumberServer
import app.sosapp.sos.sosapp.uiModules.base.BaseFragment
import app.sosapp.sos.sosapp.uiModules.contactsPicker.FragmentContactsPicker
import app.sosapp.sos.sosapp.uiModules.editSOSMsg.FragmentEditSOSMessage
import app.sosapp.sos.sosapp.uiModules.home.RepoHome
import app.sosapp.sos.sosapp.utils.SOSAppRes
import app.sosapp.sos.sosapp.utils.invoke
import app.sosapp.sos.sosapp.utils.setFirebaseAnalyticsCurrentScreen
import app.sosapp.sos.sosapp.utils.setFirebaseAnalyticsLogEvent
import app.sosapp.sos.sosapp.utils.setSafeOnClickListener
import app.sosapp.sos.sosapp.utils.toast

class FragmentEditSOSNumbers : BaseFragment<FragmentEditSosNumbersBinding>(){

    companion object{
        private val TAG by lazy { "FragEditSOSNumbers" }

        const val EXTRA_KEY_EMG_NUM_UPDATED = "emgNumUpdated"
    }

    override val layoutId = R.layout.fragment_edit_sos_numbers

    private lateinit var navController: NavController

    private lateinit var mRepoHome: RepoHome

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mDataBinding{
            mRepoHome = RepoHome(mActivity.applicationContext)
            navController = findNavController()

            val list = mPrefs.emgNumSavedModels
            if (!list.isNullOrEmpty()){
                repeat(list.size){
                    list[it].run {
                        when(type){
                            ModelEmergencyNumber.TYPE_POLICE -> etPoliceNum.setText(number)
                            ModelEmergencyNumber.TYPE_AMBULANCE -> etAmbulanceNum.setText(number)
                            ModelEmergencyNumber.TYPE_FIRE_DEPT -> etFireDepNum.setText(number)
                            ModelEmergencyNumber.TYPE_TRUSTED_CONTACT -> {
                                etSOSContNum.setText(number)
                                etSOSContNum.tag = numType
                            }
                        }
                    }
                }
            }

            setDataObservers()
            setViewListeners()
            mActivity.setFirebaseAnalyticsCurrentScreen("EditSOSNumbers")
        }
    }

    private fun FragmentEditSosNumbersBinding.setViewListeners(){
        ivSelectContact.setSafeOnClickListener {
            navController.navigate(R.id.action_edit_sos_num_to_pick_contact)
        }

        countrySpinner.setOnCountryChangeListener {
            mDialogs.showProgressDialog(SOSAppRes.getString(R.string.msg_fetching_emg_num))
            mRepoHome.fetchEmgNum(countrySpinner.selectedCountryEnglishName)
        }

        btnSave.setSafeOnClickListener {
            val policeNum = etPoliceNum.text?.toString()?.trim()
            val ambulanceNum = etAmbulanceNum.text?.toString()?.trim()
            val fireNum = etFireDepNum.text?.toString()?.trim()
            val contactNum = etSOSContNum.text?.toString()?.trim()
            val contactName = etSOSContNum.tag?.toString()?.trim()

            val numList = ModelEmergencyNumber.getEmgNumbers(policeNum, ambulanceNum, fireNum, contactNum, contactName)
            if (numList.isEmpty()){
                mActivity.toast(SOSAppRes.getString(R.string.err_msg_enter_emergency_numbers))
            }else {
                if (!policeNum.isNullOrBlank()) mPrefs.policeNumber = policeNum
                mPrefs.emgNumSavedModels = numList
                mPrefs.selectedCountry = countrySpinner.selectedCountryEnglishName
                mActivity.setFirebaseAnalyticsLogEvent("SOS_EMG_NUM", bundleOf("Save_Num" to "Total_${numList.size}"))
                navController{
                    previousBackStackEntry?.savedStateHandle?.set(EXTRA_KEY_EMG_NUM_UPDATED, true)
                    popBackStack()
                }
            }
        }

    }

    private fun setDataObservers(){
        mRepoHome.emgNumFetchLD.observe(viewLifecycleOwner){
            Log.d(TAG, "setDataObservers -> $it")
            mDialogs.dismissProgress()
            if (it?.countryName.isNullOrBlank()){
                mActivity.toast(SOSAppRes.getString(R.string.err_msg_general))
            }else{
                mDataBinding{
                    it?.run {
                        mActivity.setFirebaseAnalyticsLogEvent("SOS_EMG_NUM", bundleOf("GotByCountry" to "Country_${it.countryName}"))
                        etPoliceNum.setText(policeNum?:"")
                        etAmbulanceNum.setText(ambulanceNum?:"")
                        etFireDepNum.setText(fireNum?:"")
                    }
                }
            }
        }

        navController.currentBackStackEntry?.savedStateHandle?.getLiveData<List<ModelContact>?>(
                FragmentContactsPicker.EXTRA_KEY_SELECTED_CONTACTS)?.let { contactsLD->
            contactsLD.observe(viewLifecycleOwner){
                if (!it.isNullOrEmpty()){
                    kotlin.runCatching {
                        mDataBinding {
                            etSOSContNum.setText(it[0].contactNumber)
                            etSOSContNum.tag = it[0].contactName
                        }
                    }
                    contactsLD.postValue(null)
                }
            }
        }

        if (mPrefs.emgNumSavedModels.isNullOrEmpty()){
            mRepoHome.fetchEmgNum(mDataBinding.countrySpinner.selectedCountryEnglishName)
        }

    }

}