package app.sosapp.sos.sosapp.uiModules.base

import android.content.Context
import app.sosapp.sos.sosapp.data.SOSAppSharedPrefs
import com.google.firebase.firestore.FirebaseFirestore

abstract class BaseRepo(protected val mAppContext: Context) {

    protected val mFireStoreDb by lazy { FirebaseFirestore.getInstance() }

    protected val mPrefs by lazy { SOSAppSharedPrefs.getInstance(mAppContext) }


    open fun onClear(){

    }

}