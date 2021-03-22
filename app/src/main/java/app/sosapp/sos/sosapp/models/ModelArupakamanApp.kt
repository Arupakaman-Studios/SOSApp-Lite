package app.sosapp.sos.sosapp.models

import android.os.Parcelable
import androidx.annotation.Keep
import com.google.firebase.firestore.DocumentId
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class ModelArupakamanApp(
        @DocumentId val id: String? = null,
        val name: String? = null,
        val url: String? = null,
        val pkgName: String? = null,
        val iconUrl: String? = null,
        val status: Boolean = true,
        val fDroid: Boolean = false,
        val timeStamp: Long = 0
) : Parcelable, BaseModel {

}