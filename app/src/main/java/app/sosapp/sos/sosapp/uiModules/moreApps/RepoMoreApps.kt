package app.sosapp.sos.sosapp.uiModules.moreApps

import android.content.Context
import android.util.Log
import androidx.core.os.bundleOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import app.sosapp.sos.sosapp.constants.FirebaseFireStorePaths
import app.sosapp.sos.sosapp.models.ModelArupakamanApp
import app.sosapp.sos.sosapp.uiModules.base.BaseRepo
import app.sosapp.sos.sosapp.utils.FirebaseReporterUtil
import app.sosapp.sos.sosapp.utils.reportException
import app.sosapp.sos.sosapp.utils.setFirebaseAnalyticsLogEvent

class RepoMoreApps(mAppContext: Context): BaseRepo(mAppContext) {

    companion object{
        private val TAG by lazy { "RepoMoreApps" }
    }

    private val moreAppsMLD = MutableLiveData<List<ModelArupakamanApp>?>()
    val moreAppsLD: LiveData<List<ModelArupakamanApp>?> = moreAppsMLD

    fun fetchMoreApps(){
        mPrefs.arupakamanApps.let{ if (it.isNotEmpty()) moreAppsMLD.postValue(it) }
        mFireStoreDb.collection(FirebaseFireStorePaths.COL_MORE_APPS).get()
                .addOnCompleteListener {
                    if (it.isSuccessful && it.result != null){
                        kotlin.runCatching {
                            val list = arrayListOf<ModelArupakamanApp>()
                            it.result?.documents?.forEach {doc->
                                doc.toObject(ModelArupakamanApp::class.java)?.let { model-> list.add(model) }
                            }
                            Log.d(TAG, "fetchMoreApps Server -> $list")
                            if (list.isNotEmpty()){
                                val sortedList = list.sortedByDescending {item-> item.timeStamp }
                                mAppContext.setFirebaseAnalyticsLogEvent("FETCH_MORE_APPS", bundleOf("Apps" to "Total_${sortedList.size}"))
                                moreAppsMLD.postValue(sortedList)
                            }else{
                                moreAppsMLD.postValue(mPrefs.arupakamanApps)
                            }
                        }.onFailure {
                            it.reportException("fetchMoreApps")
                        }
                    }else {
                        Log.d(TAG, "fetchMoreApps Server Exc -> ${it.exception}")
                        moreAppsMLD.postValue(mPrefs.arupakamanApps)
                        FirebaseReporterUtil.reportException("fetchMoreApps")
                    }
                }
    }

}