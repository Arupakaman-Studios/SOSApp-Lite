package app.sosapp.sos.sosapp.models

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class ModelContact(
        val contactName: String,
        val contactNumber: String,
        var isSOSContact: Boolean = false
) : Parcelable, BaseModel {

}