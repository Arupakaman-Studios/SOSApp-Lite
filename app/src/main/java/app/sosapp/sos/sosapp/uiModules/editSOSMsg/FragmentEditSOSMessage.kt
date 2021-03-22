package app.sosapp.sos.sosapp.uiModules.editSOSMsg

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.widget.doOnTextChanged
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import app.sosapp.sos.sosapp.R
import app.sosapp.sos.sosapp.utils.invoke
import app.sosapp.sos.sosapp.databinding.FragmentEditSosMessageBinding
import app.sosapp.sos.sosapp.uiModules.base.BaseFragment
import app.sosapp.sos.sosapp.utils.SOSAppRes
import app.sosapp.sos.sosapp.utils.setFirebaseAnalyticsCurrentScreen
import app.sosapp.sos.sosapp.utils.setFirebaseAnalyticsLogEvent
import app.sosapp.sos.sosapp.utils.setSafeOnClickListener
import app.sosapp.sos.sosapp.utils.toast

class FragmentEditSOSMessage : BaseFragment<FragmentEditSosMessageBinding>(){

    companion object{
        private val TAG by lazy { "FragEditSOSMessage" }

        const val EXTRA_KEY_SOS_MSG_SAVED = "sosMsgUpdated"
    }

    override val layoutId = R.layout.fragment_edit_sos_message

    private lateinit var navController: NavController

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mDataBinding {

            navController = findNavController()
            tvSOSMsgPreview.movementMethod = ScrollingMovementMethod()

            mPrefs.sosMessage.let { msg->
                val fMsg = if (msg.isNullOrBlank()) SOSAppRes.getString(R.string.msg_default_sos_msg) else msg
                etSOSMsg.setText(fMsg)
                tvSOSMsgPreview.text = fMsg + "\n\n" + SOSAppRes.getString(R.string.msg_sos_msg_my_last_loc) + "http://maps.google.com/maps?q=loc:35.3606237,138.7098538"
            }

            etSOSMsg.doOnTextChanged { text, _, _, _ ->
                tvSOSMsgPreview.text = text.toString() + "\n\n" + SOSAppRes.getString(R.string.msg_sos_msg_my_last_loc)
            }

            setViewListeners()
            mActivity.setFirebaseAnalyticsCurrentScreen("EditSOSMessage")
        }
    }

    private fun FragmentEditSosMessageBinding.setViewListeners(){
        btnSave.setSafeOnClickListener {
            val msg = etSOSMsg.text?.toString()?.trim()
            if (msg.isNullOrBlank()){
                mActivity.toast(SOSAppRes.getString(R.string.err_msg_empty_sos_msg))
            }else {
                mPrefs.sosMessage = msg
                mActivity.toast(SOSAppRes.getString(R.string.msg_sos_msg_saved))
                mActivity.setFirebaseAnalyticsLogEvent("SOS_MESSAGE", bundleOf("Saved" to "Msg_Size_${msg.length}"))
                navController.apply {
                    previousBackStackEntry?.savedStateHandle?.set(EXTRA_KEY_SOS_MSG_SAVED, true)
                    popBackStack()
                }
            }
        }
    }

}