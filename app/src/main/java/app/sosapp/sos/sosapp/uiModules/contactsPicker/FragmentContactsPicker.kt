package app.sosapp.sos.sosapp.uiModules.contactsPicker

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import app.sosapp.sos.sosapp.R
import app.sosapp.sos.sosapp.databinding.FragmentContactsPickerBinding
import app.sosapp.sos.sosapp.databinding.FragmentHomeBinding
import app.sosapp.sos.sosapp.databinding.FragmentSosContactsBinding
import app.sosapp.sos.sosapp.models.ModelContact
import app.sosapp.sos.sosapp.models.ModelEmergencyNumber
import app.sosapp.sos.sosapp.uiModules.base.BaseFragment
import app.sosapp.sos.sosapp.uiModules.editSOSMsg.FragmentEditSOSMessage
import app.sosapp.sos.sosapp.uiModules.goToAppSettings
import app.sosapp.sos.sosapp.utils.*
import com.turingtechnologies.materialscrollbar.AlphabetIndicator

class FragmentContactsPicker : BaseFragment<FragmentContactsPickerBinding>(){

    companion object{
        private val TAG by lazy { "FragContactsPicker" }

        private const val PERMISSIONS_REQUEST_READ_CONTACTS = 1003

        const val EXTRA_KEY_SELECTED_CONTACTS = "selectedContacts"
    }

    override val layoutId = R.layout.fragment_contacts_picker
    private val args: FragmentContactsPickerArgs by navArgs()

    private lateinit var mRepoContacts: RepoContacts
    private lateinit var mAdapterContacts: AdapterContacts

    private lateinit var navController: NavController

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mDataBinding {
            navController = findNavController()
            mRepoContacts = RepoContacts(mActivity.applicationContext)

            bSelected = ""

            mAdapterContacts = AdapterContacts(args.maxSelection, onClick = { _, _ ->
                val selected = mAdapterContacts.getSelectedContacts().size
                if (selected <= 0) btnSelect.disable() else btnSelect.enable()
                bSelected = "${selected}/${args.maxSelection}"
            })

            rvContacts.adapter = mAdapterContacts
            touchScrollBar.setIndicator(AlphabetIndicator(mActivity), true)

            setViewListeners()
            setDataObserver()
            mActivity.setFirebaseAnalyticsCurrentScreen("ContactsPicker")
        }
    }

    override fun onResume() {
        if (!mDialogs.isShowingDialog) loadContacts()
        super.onResume()
    }

    override fun onDestroy() {
        if (::mRepoContacts.isInitialized){
            mRepoContacts.onClear()
        }
        super.onDestroy()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == PERMISSIONS_REQUEST_READ_CONTACTS) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadContacts()
            } else {
                val rational = !ActivityCompat.shouldShowRequestPermissionRationale(mActivity, Manifest.permission.READ_CONTACTS)
                mDialogs.showCommonConfirmationDialog(
                    SOSAppRes.getString(R.string.title_permission_required),
                    SOSAppRes.getString(if (rational) R.string.msg_read_contact_permission_required_rational else R.string.msg_read_contact_permission_required),
                    null,
                    SOSAppRes.getString(R.string.action_grant_now),
                    rBtnClick = {
                        mDialogs.dismiss()
                        if (rational){
                            mActivity.goToAppSettings()
                        }else{
                            loadContacts()
                        }
                    })
                mDialogs.mDialog?.setOnKeyListener { dialogInterface, i, keyEvent ->
                    if (i == KeyEvent.KEYCODE_BACK && keyEvent.action == KeyEvent.ACTION_UP) {
                        dialogInterface.dismiss()
                        navController.popBackStack()
                    }
                    return@setOnKeyListener true
                }
            }
        }
    }

    private fun FragmentContactsPickerBinding.setViewListeners(){
        swipeRefreshLayout.setOnRefreshListener {
            loadContacts()
        }

        btnSelect.setSafeOnClickListener {
            val selContacts = mAdapterContacts.getSelectedContacts()
            if (selContacts.isNullOrEmpty()){
                mActivity.setFirebaseAnalyticsLogEvent("CONTACTS_PICK", bundleOf("Picked" to "Picked_Error"))
                mActivity.toast(SOSAppRes.getString(R.string.err_msg_general))
            }else{
                mActivity.setFirebaseAnalyticsLogEvent("CONTACTS_PICK", bundleOf("Picked" to "Picked_Total_${selContacts.size}"))
                if (args.sosContacts) mPrefs.sosContactsModels = selContacts
                navController{
                    previousBackStackEntry?.savedStateHandle?.set(EXTRA_KEY_SELECTED_CONTACTS, mAdapterContacts.getSelectedContacts())
                    popBackStack()
                }
            }
        }
    }

    private fun loadContacts() {
        mActivity.setFirebaseAnalyticsLogEvent("CONTACTS_PICK", bundleOf("Loading" to "Loading"))
        if (mAdapterContacts.itemCount <= 0) mDialogs.showProgressDialog(SOSAppRes.getString(R.string.msg_loading_contacts))
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                checkSelfPermission(mActivity, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.READ_CONTACTS), PERMISSIONS_REQUEST_READ_CONTACTS)
        } else {
            mRepoContacts.fetchContacts(args.sosContacts)
        }
    }

    private fun setDataObserver(){
        mRepoContacts.contactsLD.observe(viewLifecycleOwner){
            mDataBinding{
                swipeRefreshLayout.isRefreshing = false
                setContactsUi(it)
                mActivity.setFirebaseAnalyticsLogEvent("CONTACTS_PICK", bundleOf("Loading" to "Loading_Completed"))
            }
        }
    }

    private fun FragmentContactsPickerBinding.setContactsUi(numbers: List<ModelContact>?){
        mDialogs.dismissProgress()
        if (!numbers.isNullOrEmpty()){
            mAdapterContacts.submit(numbers)
            tvContactsError.isVisible = false
            rvContacts.isVisible = true
            val selected = mAdapterContacts.getSelectedContacts().size
            bSelected = "${selected}/${args.maxSelection}"
            if (selected <= 0) btnSelect.disable() else btnSelect.enable()
        }else{
            rvContacts.isVisible = false
            tvContactsError.isVisible = true
        }
    }

}