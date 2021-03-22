package app.sosapp.sos.sosapp.data

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import app.sosapp.sos.sosapp.models.ModelArupakamanApp
import app.sosapp.sos.sosapp.models.ModelContact
import app.sosapp.sos.sosapp.models.ModelEmergencyNumber
import com.google.gson.reflect.TypeToken
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class SOSAppSharedPrefs private constructor(mPrefs: SharedPreferences){

    var selectedLanguageCode by mPrefs.dataNullable<String?>()

    var selectedLanguageName by mPrefs.dataNullable<String?>()

    var selectedThemeMode by mPrefs.data(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)


    var sosMessage by mPrefs.dataNullable<String?>()

    var selectedCountry by mPrefs.dataNullable<String?>()

    var selectedSOSCountDownIndex by mPrefs.data(1)

    var supportDialogCount by mPrefs.data(0)

    var policeNumber by mPrefs.dataNullable<String?>()

    var reviewAsked by mPrefs.data(false)

    var moreAppsBadgeCount by mPrefs.data(0)

    var translateUrl by mPrefs.dataNullable<String?>()

    var pushedVersion by mPrefs.data(0)

    var sosAddressOn by mPrefs.data(true)

    var sosSilentOn by mPrefs.data(false)

    /**
     *   Json for models
     */

    var emgNumModels by mPrefs.modelNullable<List<ModelEmergencyNumber>?>()

    var emgNumSavedModels by mPrefs.modelNullable<List<ModelEmergencyNumber>?>()

    var sosContactsModels by mPrefs.model<List<ModelContact>>(emptyList())

    var arupakamanApps by mPrefs.model<List<ModelArupakamanApp>>(emptyList())


    companion object{

        private var mPrefs: SOSAppSharedPrefs? = null

        fun getInstance(mContext: Context): SOSAppSharedPrefs {
            if (mPrefs == null)
                mPrefs = SOSAppSharedPrefs(mContext.getSharedPreferences("SOSAppSharedPrefs", Context.MODE_PRIVATE))
            return mPrefs!!
        }

    }

}

inline fun <reified T: Any> SharedPreferences.data(defaultValue: T):
        ReadWriteProperty<Any, T> = object : ReadWriteProperty<Any, T> {

    override fun getValue(thisRef: Any, property: KProperty<*>) = getData(property.name, defaultValue)

    override fun setValue(thisRef: Any, property: KProperty<*>, value: T) = putData(property.name, value)
}

inline fun <reified T: Any?> SharedPreferences.dataNullable(defaultValue: T? = null):
        ReadWriteProperty<Any, T?> = object : ReadWriteProperty<Any, T?> {

    override fun getValue(thisRef: Any, property: KProperty<*>) = getDataNullable(property.name, defaultValue)

    override fun setValue(thisRef: Any, property: KProperty<*>, value: T?) = putDataNullable(property.name, value)
}

/**
 *  Get Data From Pref
 */

inline fun <reified T> SharedPreferences.getData(
    key: String,
    default: T
): T {
    @Suppress("UNCHECKED_CAST")
    return when (default) {
        is String -> getString(key, default) as T
        is Int -> getInt(key, default) as T
        is Long -> getLong(key, default) as T
        is Boolean -> getBoolean(key, default) as T
        is Float -> getFloat(key, default) as T
        is Set<*> -> getStringSet(key, default as Set<String>) as T
        is MutableSet<*> -> getStringSet(key, default as MutableSet<String>) as T
        else -> throw IllegalArgumentException("generic type not handled")
    }
}

inline fun <reified T> SharedPreferences.getDataNullable(
    key: String,
    default: T? = null
): T? {
    @Suppress("UNCHECKED_CAST")
    return when (default) {
        is String? -> getString(key, default) as T?
        is Int? -> getInt(key, default?:0) as T?
        is Long? -> getLong(key, default?:0L) as T?
        is Boolean? -> getBoolean(key, default?:false) as T?
        is Float? -> getFloat(key, default?:0f) as T?
        is Set<*>? -> getStringSet(key, default as Set<String>) as T?
        is MutableSet<*>? -> getStringSet(key, default as MutableSet<String>) as T?
        else -> throw IllegalArgumentException("generic type not handled")
    }
}

/**
 *  Put Data Into Pref
 */

inline fun <reified T> SharedPreferences.putData(
    key: String,
    data: T
) {
    @Suppress("UNCHECKED_CAST")
    this.edit().apply {
        when (data) {
            is String -> putString(key, data)
            is Int -> putInt(key, data)
            is Long -> putLong(key, data)
            is Boolean -> putBoolean(key, data)
            is Float -> putFloat(key, data)
            is Set<*> -> putStringSet(key, data as Set<String>)
            is MutableSet<*> -> putStringSet(key, data as MutableSet<String>)
            else -> throw IllegalArgumentException("generic type not handled")
        }
    }.apply()
}

inline fun <reified T> SharedPreferences.putDataNullable(
    key: String,
    data: T? = null
) {
    @Suppress("UNCHECKED_CAST")
    this.edit().apply {
        when (data) {
            is String? -> putString(key, data)
            is Int? -> putInt(key, data?:0)
            is Long? -> putLong(key, data?:0L)
            is Boolean? -> putBoolean(key, data?:false)
            is Float? -> putFloat(key, data?:0f)
            is Set<*>? -> putStringSet(key, data as Set<String>?)
            is MutableSet<*>? -> putStringSet(key, data as MutableSet<String>?)
            else -> throw IllegalArgumentException("generic type not handled")
        }
    }.apply()
}

/**
 *   Models x Prefs
 */

inline fun <reified T> SharedPreferences.getModelFromPref(key: String, default: T?): T?{
    runCatching {
        getString(key, null)?.let{ return JsonPojoParser.getGson().fromJson(it, object : TypeToken<T>() {}.type) }
    }.onFailure {
        Log.e("getModelFromPref", "Exc : $it")
    }
    return default
}

inline fun <reified T> SharedPreferences.putModelToPref(key: String, model: T?){
    runCatching {
        if (model != null) return edit().putString(key, JsonPojoParser.getGson().toJson(model)).apply()
    }.onFailure {
        Log.e("putModelToPref", "Exc : $it")
    }
}

inline fun <reified T> SharedPreferences.model(defaultValue: T):
        ReadWriteProperty<Any, T> = object : ReadWriteProperty<Any, T> {

    override fun getValue(thisRef: Any, property: KProperty<*>): T {
        return getModelFromPref(property.name, defaultValue)?:defaultValue
    }

    override fun setValue(thisRef: Any, property: KProperty<*>, value: T) {
        putModelToPref(property.name, value)
    }

}

inline fun <reified T> SharedPreferences.modelNullable(defaultValue: T? = null):
        ReadWriteProperty<Any, T?> = object : ReadWriteProperty<Any, T?> {

    override fun getValue(thisRef: Any, property: KProperty<*>): T? {
        return getModelFromPref(property.name, defaultValue)?:defaultValue
    }

    override fun setValue(thisRef: Any, property: KProperty<*>, value: T?) {
        putModelToPref(property.name, value)
    }

}


