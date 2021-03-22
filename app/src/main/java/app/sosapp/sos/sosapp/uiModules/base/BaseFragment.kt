package app.sosapp.sos.sosapp.uiModules.base

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import app.sosapp.sos.sosapp.data.SOSAppSharedPrefs
import app.sosapp.sos.sosapp.uiModules.dialogs.SOSAppDialogs
import app.sosapp.sos.sosapp.utils.inflateBinding

abstract class BaseFragment<B : ViewDataBinding> : Fragment() {

    protected lateinit var mActivity: Activity

    protected val mPrefs by lazy { SOSAppSharedPrefs.getInstance(mActivity.applicationContext) }
    protected val mDialogs by lazy { SOSAppDialogs(mActivity) }

    protected lateinit var mDataBinding: B
    protected abstract val layoutId: Int

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mActivity = context as Activity
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mDataBinding = inflater.inflateBinding(layoutId, container)
        mDataBinding.lifecycleOwner = viewLifecycleOwner
        return mDataBinding.root
    }

    override fun onDestroy() {
        mDialogs.dismissAll()
        super.onDestroy()
    }

}