package app.sosapp.sos.sosapp.models

import android.os.Parcelable
import androidx.annotation.DrawableRes
import androidx.annotation.Keep
import app.sosapp.sos.sosapp.R
import app.sosapp.sos.sosapp.utils.SOSAppRes
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class ModelEmergencyNumber(
        @DrawableRes var iconRes: Int,
        var numType: String,
        val number: String,
        var contDesc: String,
        val type: Int
) : Parcelable, BaseModel {

    companion object{

        const val TYPE_UNKNOWN = 0
        const val TYPE_POLICE = 1
        const val TYPE_AMBULANCE = 2
        const val TYPE_FIRE_DEPT = 3
        const val TYPE_TRUSTED_CONTACT = 4

        fun getEmgNumbers(policeNum: String?, ambulanceNum: String?, fireNum: String?, contactNum: String?, contactName: String?): List<ModelEmergencyNumber>{
            val list = arrayListOf<ModelEmergencyNumber>()
            if (!policeNum.isNullOrBlank()) list.add(ModelEmergencyNumber(R.drawable.ic_police,
                    SOSAppRes.getString(R.string.title_police), policeNum, SOSAppRes.getString(R.string.hint_police_number), TYPE_POLICE))
            if (!ambulanceNum.isNullOrBlank()) list.add(ModelEmergencyNumber(R.drawable.ic_ambulance,
                    SOSAppRes.getString(R.string.title_ambulance), ambulanceNum, SOSAppRes.getString(R.string.hint_ambulance_number), TYPE_AMBULANCE))
            if (!fireNum.isNullOrBlank()) list.add(ModelEmergencyNumber(R.drawable.ic_fire,
                    SOSAppRes.getString(R.string.title_fire_department), fireNum, SOSAppRes.getString(R.string.hint_fire_dep_number), TYPE_FIRE_DEPT))
            if (!contactNum.isNullOrBlank()) list.add(ModelEmergencyNumber(R.drawable.ic_friends,
                    contactName?:SOSAppRes.getString(R.string.hint_trusted_contact_number), contactNum, SOSAppRes.getString(R.string.hint_trusted_contact_number), TYPE_TRUSTED_CONTACT))
            return list
        }

        fun fixIcons(list: List<ModelEmergencyNumber>): List<ModelEmergencyNumber>{
            repeat(list.size){
                list[it].apply {
                    when(type){
                        TYPE_POLICE -> {
                            numType = SOSAppRes.getString(R.string.title_police)
                            contDesc = SOSAppRes.getString(R.string.hint_police_number)
                            iconRes = R.drawable.ic_police
                        }
                        TYPE_AMBULANCE -> {
                            numType = SOSAppRes.getString(R.string.title_ambulance)
                            contDesc = SOSAppRes.getString(R.string.hint_ambulance_number)
                            iconRes = R.drawable.ic_ambulance
                        }
                        TYPE_FIRE_DEPT -> {
                            numType = SOSAppRes.getString(R.string.title_fire_department)
                            contDesc = SOSAppRes.getString(R.string.hint_fire_dep_number)
                            iconRes = R.drawable.ic_fire
                        }
                        TYPE_TRUSTED_CONTACT -> {
                            contDesc = SOSAppRes.getString(R.string.hint_trusted_contact_number)
                            iconRes = R.drawable.ic_friends
                        }
                        else -> iconRes = R.drawable.ic_call
                    }
                }
            }
            return list
        }

    }

}

@Keep
@Parcelize
data class ModelEmergencyNumberServer(
        val countryName: String? = null,
        val policeNum: String? = null,
        val ambulanceNum: String? = null,
        val fireNum: String? = null
) : Parcelable, BaseModel {

}