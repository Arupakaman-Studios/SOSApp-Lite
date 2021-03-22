package app.sosapp.sos.sosapp.models

import android.os.Parcelable
import androidx.annotation.DrawableRes
import androidx.annotation.Keep
import app.sosapp.sos.sosapp.R
import app.sosapp.sos.sosapp.utils.SOSAppRes
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class ModelSOSTool(
    @DrawableRes val iconRes: Int,
    val name: String,
    val contDesc: String
) : Parcelable, BaseModel {

    companion object{

        fun getSOSTools() = listOf(
            ModelSOSTool(R.drawable.ic_flash, SOSAppRes.getString(R.string.title_sos_flash), SOSAppRes.getString(R.string.title_sos_flash)),
            ModelSOSTool(R.drawable.ic_sirens, SOSAppRes.getString(R.string.title_sos_siren), SOSAppRes.getString(R.string.title_sos_siren))
        )

    }

}