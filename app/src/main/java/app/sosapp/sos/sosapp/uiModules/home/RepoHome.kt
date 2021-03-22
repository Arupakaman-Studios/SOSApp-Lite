package app.sosapp.sos.sosapp.uiModules.home

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import app.sosapp.sos.sosapp.BuildConfig
import app.sosapp.sos.sosapp.R
import app.sosapp.sos.sosapp.constants.FirebaseFireStorePaths
import app.sosapp.sos.sosapp.data.SystemPropertiesProxy
import app.sosapp.sos.sosapp.models.ModelAppInfo
import app.sosapp.sos.sosapp.models.ModelEmergencyNumber
import app.sosapp.sos.sosapp.models.ModelEmergencyNumberServer
import app.sosapp.sos.sosapp.uiModules.base.BaseRepo
import app.sosapp.sos.sosapp.utils.*
import app.sosapp.sos.sosapp.utils.executors.DefaultExecutorSupplier
import java.util.concurrent.Future


class RepoHome(mAppContext: Context): BaseRepo(mAppContext) {

    companion object{
        private val TAG by lazy { "RepoHome" }
    }

    private var mJobEmgNum: Future<*>? = null

    private val emgNumMLD = MutableLiveData<List<ModelEmergencyNumber>?>()
    val emgNumLD: LiveData<List<ModelEmergencyNumber>?> = emgNumMLD

    fun fetchLocalEmgNum(){
        val prefEmgNum = mPrefs.emgNumModels
        Log.d(TAG, "prefEmgNum $prefEmgNum")
        if (prefEmgNum.isNullOrEmpty()){
            mJobEmgNum = DefaultExecutorSupplier.getInstance().forLightWeightBackgroundTasks()
                    .submit {
                        kotlin.runCatching {
                            var idleNumbers = SystemPropertiesProxy.get(mAppContext, "ril.ecclist.eccidlemode")?:""
                            Log.d(TAG, "idleNumbers 1 -> $idleNumbers")
                            SystemPropertiesProxy.get(mAppContext, "ril.ecclist.eccidlemode1")?.let { numbers->
                                if (numbers.length > idleNumbers.length) idleNumbers = numbers
                            }
                            Log.d(TAG, "idleNumbers 2 -> $idleNumbers")
                            SystemPropertiesProxy.get(mAppContext, "ril.ecclist")?.let { numbers->
                                if (numbers.length > idleNumbers.length) idleNumbers = numbers
                            }
                            Log.d(TAG, "idleNumbers 3 -> $idleNumbers")
                            SystemPropertiesProxy.get(mAppContext, "ril.ecclist1")?.let { numbers->
                                if (numbers.length > idleNumbers.length) idleNumbers = numbers
                            }
                            Log.d(TAG, "idleNumbers 4 -> $idleNumbers")
                            SystemPropertiesProxy.get(mAppContext, "ro.ril.ecclist")?.let { numbers->
                                if (numbers.length > idleNumbers.length) idleNumbers = numbers
                            }
                            Log.d(TAG, "idleNumbers 5 -> $idleNumbers")

                            if (idleNumbers.isNotBlank()){
                                idleNumbers = idleNumbers.replace("[", "").replace("]", "")
                                val list = idleNumbers.split(",")
                                val emgNumList = arrayListOf<ModelEmergencyNumber>()
                                repeat(list.size) {
                                    list[it].run {
                                        if (it == 0) mPrefs.policeNumber = this
                                        emgNumList.add(ModelEmergencyNumber(
                                                R.drawable.ic_call,
                                                "",
                                                this,
                                                SOSAppRes.getString(R.string.title_emergency_number) + " $this",
                                                ModelEmergencyNumber.TYPE_UNKNOWN
                                        ))
                                    }
                                }
                                emgNumMLD.postValue(if (emgNumList.size > 4) emgNumList.take(4) else emgNumList)
                            }else emgNumMLD.postValue(null)
                        }.onFailure {
                            Log.e(TAG, "idleNumbers Exc $it")
                            it.reportException("Error loading device emergency numbers")
                            emgNumMLD.postValue(null)
                        }
                    }
        }else emgNumMLD.postValue(prefEmgNum)
    }

    private val emgNumFetchMLD = MutableLiveData<ModelEmergencyNumberServer?>()
    val emgNumFetchLD: LiveData<ModelEmergencyNumberServer?> = emgNumFetchMLD

    fun fetchEmgNum(countryName: String){
        mFireStoreDb.collection(FirebaseFireStorePaths.COL_EMG_NUMBERS).document(countryName).get()
            .addOnCompleteListener {
            if (it.isSuccessful && it.result != null){
                kotlin.runCatching {
                    val model = it.result?.toObject(ModelEmergencyNumberServer::class.java)
                    Log.d(TAG, "fetchEmgNum Server -> $model")
                    emgNumFetchMLD.postValue(model)
                }.onFailure {
                    it.reportException("fetchEmgNum")
                }
            }else {
                Log.d(TAG, "fetchEmgNum Server Exc -> ${it.exception}")
                emgNumFetchMLD.postValue(null)
                FirebaseReporterUtil.reportException("fetchEmgNum")
            }
        }
    }

    private val appInfoFetchMLD = MutableLiveData<ModelAppInfo?>()
    val appInfoFetchLD: LiveData<ModelAppInfo?> = appInfoFetchMLD

    fun fetchAppInfo(){
        mFireStoreDb.collection(FirebaseFireStorePaths.COL_APP_INFO).document(FirebaseFireStorePaths.COL_APP_INFO).get()
            .addOnCompleteListener {
                if (it.isSuccessful && it.result != null){
                    kotlin.runCatching {
                        val model = it.result?.toObject(ModelAppInfo::class.java)
                        Log.d(TAG, "fetchAppInfo Server -> $model")
                        if (!model?.translateUrl.isNullOrBlank()) mPrefs.translateUrl = model?.translateUrl
                        appInfoFetchMLD.postValue(model)
                    }.onFailure {
                        it.reportException("fetchAppInfo")
                    }
                }else {
                    Log.d(TAG, "fetchAppInfo Server Exc -> ${it.exception}")
                    appInfoFetchMLD.postValue(null)
                    FirebaseReporterUtil.reportException("fetchAppInfo")
                }
            }
    }

    fun pushInstallEvent(){
        val pushedVersion = mPrefs.pushedVersion
        if (pushedVersion == 0 || pushedVersion < BuildConfig.VERSION_CODE){
            mFireStoreDb.collection(FirebaseFireStorePaths.COL_INSTALL_EVENT)
                .document(mAppContext.getAndroidDeviceId())
                .set(mapOf("versionCode" to BuildConfig.VERSION_CODE))
                .addOnSuccessListener {
                    mPrefs.pushedVersion = BuildConfig.VERSION_CODE
                }
        }
    }

    override fun onClear() {
        mJobEmgNum.cancelIfActive()
    }

}