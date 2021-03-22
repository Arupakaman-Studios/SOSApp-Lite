package app.sosapp.sos.sosapp.utils

import android.annotation.SuppressLint
import android.content.Context
import androidx.annotation.StringRes

/**
 *   Resource Wrapper
 */

@SuppressLint("StaticFieldLeak")
object SOSAppRes {

    private lateinit var mContext: Context

    fun setContext(context: Context){
        mContext = context
    }

    fun getString(@StringRes resId: Int) = mContext.getString(resId)

}