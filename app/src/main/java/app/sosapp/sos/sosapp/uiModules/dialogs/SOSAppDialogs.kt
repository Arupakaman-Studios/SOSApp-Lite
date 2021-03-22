package app.sosapp.sos.sosapp.uiModules.dialogs

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.Window
import androidx.annotation.LayoutRes
import androidx.core.os.bundleOf
import androidx.databinding.ViewDataBinding
import app.sosapp.sos.sosapp.R
import app.sosapp.sos.sosapp.databinding.DialogCommonConfirmationLayoutBinding
import app.sosapp.sos.sosapp.databinding.DialogProgressLayoutBinding
import app.sosapp.sos.sosapp.utils.LocaleHelper
import app.sosapp.sos.sosapp.utils.SOSAppRes
import app.sosapp.sos.sosapp.utils.inflateBinding
import app.sosapp.sos.sosapp.utils.setFirebaseAnalyticsLogEvent
import app.sosapp.sos.sosapp.utils.setSafeOnClickListener
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class SOSAppDialogs(private val mActivity: Activity) {

    companion object{
        private val TAG by lazy { "SOSAppDialogs" }

        private const val BOTTOM_DIALOG_Y_POSITION = 300
    }

    var mDialog: Dialog? = null
    var mProgressDialog: Dialog? = null

    private fun isShowing(dialog: Dialog?) = dialog?.isShowing ?: false

    val isShowingDialog: Boolean
        get() {
            return isShowing(mDialog)
        }

    val isShowingProgress: Boolean
        get() {
            return isShowing(mProgressDialog)
        }

    private fun show(dialog: Dialog?) {
        //dismiss(dialog)
        dialog?.runCatching {
            if (!mActivity.isFinishing) show()
        }
    }

    fun showDialog(){
        show(mDialog)
    }

    fun showProgress(){
        show(mProgressDialog)
    }

    private fun dismiss(dialog: Dialog?) {
        dialog?.runCatching {
            if (isShowing) dismiss()
        }
    }

    fun dismiss() {
        dismiss(mDialog)
        mDialog = null
    }

    fun dismissProgress() {
        dismiss(mProgressDialog)
        mProgressDialog = null
    }

    fun dismissAll() {
        dismiss()
        dismissProgress()
    }

    private fun setGravity(mDialog: Dialog, mGravity: Int, xPos: Int = 0, yPos: Int = 0){
        mDialog.apply {
            val attrs = window?.attributes
            attrs?.apply {
                gravity = mGravity
                x = xPos
                y = yPos
            }
            window?.attributes = attrs
        }
    }


    /**
     *   Init Dialog
     */

    fun <T : ViewDataBinding> initNewDialog(@LayoutRes layout: Int, isProgress: Boolean = false): T {
        if (isProgress) dismissProgress() else dismiss()

        val binding: T
        Dialog(mActivity).apply {
            window?.let { win ->
                win.requestFeature(Window.FEATURE_NO_TITLE)
                win.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            }
            binding = mActivity.layoutInflater.inflateBinding(layout, null)
            setContentView(binding.root)
        }.also { dialog ->
            if (isProgress) mProgressDialog = dialog else {
                setGravity(dialog, Gravity.BOTTOM, yPos = BOTTOM_DIALOG_Y_POSITION)
                mDialog = dialog
            }
        }
        return binding
    }

    fun showProgressDialog(msg: String = SOSAppRes.getString(R.string.msg_please_wait), isCancelable: Boolean = false): DialogProgressLayoutBinding{
        return initNewDialog<DialogProgressLayoutBinding>(R.layout.dialog_progress_layout, true).apply {
            mProgressDialog?.setCancelable(isCancelable)
            bMsg = msg
            showProgress()
        }
    }

    fun showCommonConfirmationDialog(title: String, msg: String, lBtnTxt: String? = null, rBtnTxt: String?, isCancelable: Boolean = false,
                                     lBtnClick: (()-> Unit)? = null, rBtnClick: (()-> Unit)? = null): DialogCommonConfirmationLayoutBinding {
        return initNewDialog<DialogCommonConfirmationLayoutBinding>(R.layout.dialog_common_confirmation_layout, false).apply {
            mDialog?.setCancelable(isCancelable)
            bTitle = title
            bMsg = msg
            bLeftBtnTxt = lBtnTxt
            bRightBtnTxt = rBtnTxt
            btnLeft.setSafeOnClickListener {
                lBtnClick?.invoke()
            }
            btnRight.setSafeOnClickListener {
                rBtnClick?.invoke()
            }
            showDialog()
        }
    }

    fun showLanguageSelectDialog(mSelLangPos: Int, onSelect: (Int) -> Unit){
        var selLangPos = mSelLangPos
        mDialog = MaterialAlertDialogBuilder(mActivity)
                .setTitle(SOSAppRes.getString(R.string.title_select_language_colon))
                .setSingleChoiceItems(R.array.arr_languages, LocaleHelper.getSelectedLanguageCodePosition(mActivity)){ _, which ->
                    selLangPos = which
                }
                .setPositiveButton(R.string.action_select){ dialog, _ ->
                    val selLang = LocaleHelper.getLanguageByPosition(mActivity, selLangPos)
                    LocaleHelper.setLocale(mActivity, selLang.first, selLang.second)
                    mActivity.setFirebaseAnalyticsLogEvent("LANGUAGE_SELECT", bundleOf("Language_Code" to selLang.first,
                            "Language_Name" to selLang.second))
                    onSelect(selLangPos)
                    dismiss()
                }
                .setNegativeButton(R.string.action_cancel){ dialog, _ ->
                    dismiss()
                }.create()

        showDialog()
    }

    fun showCountDownSelectDialog(mSelCountDownPos: Int, onSelect: (Int) -> Unit){
        var selCountDownPos = mSelCountDownPos
        mDialog = MaterialAlertDialogBuilder(mActivity)
                .setTitle(SOSAppRes.getString(R.string.title_select_countdown_time_colon))
                .setSingleChoiceItems(R.array.arr_sos_countdowns, selCountDownPos){ _, which ->
                    selCountDownPos = which
                }
                .setPositiveButton(R.string.action_select){ dialog, _ ->
                    onSelect(selCountDownPos)
                    mActivity.setFirebaseAnalyticsLogEvent("SOS_COUNT_DOWN_SELECT",
                            bundleOf("CountDown" to mActivity.resources.getStringArray(R.array.arr_sos_countdowns)[selCountDownPos]))
                    dismiss()
                }
                .setNegativeButton(R.string.action_cancel){ dialog, _ ->
                    dismiss()
                }.create()

        showDialog()
    }

}