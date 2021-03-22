package app.sosapp.sos.sosapp.uiModules.sosContacts

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import app.sosapp.sos.sosapp.R
import app.sosapp.sos.sosapp.databinding.FragmentSosContactsBinding
import app.sosapp.sos.sosapp.models.ModelContact
import app.sosapp.sos.sosapp.uiModules.base.BaseFragment
import app.sosapp.sos.sosapp.uiModules.contactsPicker.AdapterContacts
import app.sosapp.sos.sosapp.uiModules.contactsPicker.FragmentContactsPicker
import app.sosapp.sos.sosapp.uiModules.gotoSendSOSActivity
import app.sosapp.sos.sosapp.utils.SOSAppRes
import app.sosapp.sos.sosapp.utils.disable
import app.sosapp.sos.sosapp.utils.enable
import app.sosapp.sos.sosapp.utils.invoke
import app.sosapp.sos.sosapp.utils.setFirebaseAnalyticsCurrentScreen
import app.sosapp.sos.sosapp.utils.setFirebaseAnalyticsLogEvent
import app.sosapp.sos.sosapp.utils.setSafeOnClickListener
import app.sosapp.sos.sosapp.utils.toast

class FragmentSOSContacts : BaseFragment<FragmentSosContactsBinding>(){

    companion object{
        private val TAG by lazy { "FragmentSOSContacts" }

        //private const val REQUEST_CODE_SELECT_CONTACTS = 1002
    }

    override val layoutId = R.layout.fragment_sos_contacts
    private lateinit var mAdapterContacts: AdapterContacts

    private lateinit var navController: NavController

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mDataBinding{

            navController = findNavController()

            mAdapterContacts = AdapterContacts(isPickContacts = false, onClick = {model, _ ->
                onSendSOS(model)
            }, onDeleteClick = {model->
                onDeleteContact(model)
            })

            val sosContacts = mPrefs.sosContactsModels
            rvContacts.adapter = mAdapterContacts
            setContactsList(sosContacts)

            setDataObservers()
            setViewListeners()
            mActivity.setFirebaseAnalyticsCurrentScreen("SOSContacts")
        }
    }

    private fun FragmentSosContactsBinding.setViewListeners(){
        cardAddSOSContacts.setSafeOnClickListener {
            mActivity.setFirebaseAnalyticsLogEvent("GO_TO_APP_NAV", bundleOf("Nav" to "SOS_Contacts_To_Add_Contacts"))
            navController.navigate(R.id.action_sos_contacts_to_contacts_picker)
        }
    }

    private fun FragmentSosContactsBinding.setContactsList(sosContacts: List<ModelContact>){
        mAdapterContacts.submit(sosContacts)

        if (sosContacts.isEmpty()){
            rvContacts.isVisible = false
            tvContactsError.isVisible = true
        }else{
            tvContactsError.isVisible = false
            rvContacts.isVisible = true
            if (sosContacts.size == 10) cardAddSOSContacts.disable() else cardAddSOSContacts.enable()
        }
    }

    private fun setDataObservers(){
        navController.currentBackStackEntry?.savedStateHandle?.getLiveData<List<ModelContact>?>(
                FragmentContactsPicker.EXTRA_KEY_SELECTED_CONTACTS)?.let { contactsLD->
            contactsLD.observe(viewLifecycleOwner){
                if (it != null){
                    mDataBinding.setContactsList(it)
                    contactsLD.postValue(null)
                }
            }
        }
    }

    private fun onDeleteContact(model: ModelContact){
        mDialogs.showCommonConfirmationDialog(
                SOSAppRes.getString(R.string.title_remove_sos_contact),
                SOSAppRes.getString(R.string.msg_remove_sos_contact) + model.contactName + " - " + model.contactNumber,
                SOSAppRes.getString(R.string.action_cancel),
                SOSAppRes.getString(R.string.action_remove),
                lBtnClick = {
                    mDialogs.dismiss()
                },
                rBtnClick = {
                    val list = mAdapterContacts.mItemsList.filter { it.contactNumber !=  model.contactNumber && it.contactName != model.contactName }
                    mPrefs.sosContactsModels = list
                    mDataBinding.setContactsList(list)
                    mDialogs.dismiss()
                    mActivity.toast(SOSAppRes.getString(R.string.msg_sos_contact_removed))
                }).apply {
                    btnRight.setBackgroundColor(ContextCompat.getColor(mActivity, R.color.colorRedThemeX))
        }
    }

    private fun onSendSOS(model: ModelContact){
        mDialogs.showCommonConfirmationDialog(
                SOSAppRes.getString(R.string.title_send_sos),
                SOSAppRes.getString(R.string.msg_send_sos_individual) + model.contactName + " - " + model.contactNumber,
                SOSAppRes.getString(R.string.action_cancel),
                SOSAppRes.getString(R.string.action_send_now),
                lBtnClick = {
                    mDialogs.dismiss()
                },
                rBtnClick = {
                    mDialogs.dismiss()
                    mActivity.setFirebaseAnalyticsLogEvent("SEND_SOS", bundleOf("Action" to "Manual_to_Contact"))
                    mActivity.gotoSendSOSActivity(model)
                })
    }

}