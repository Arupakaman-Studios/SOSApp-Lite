package app.sosapp.sos.sosapp.models

import androidx.annotation.Keep
import app.sosapp.sos.sosapp.BuildConfig

@Keep
data class ModelAppInfo(
    val currentVersion: Int = BuildConfig.VERSION_CODE - 1,
    val criticalVersion: Int = BuildConfig.VERSION_CODE,
    val whatsNew: String? = null,
    val translateUrl: String? = null
): BaseModel {
}