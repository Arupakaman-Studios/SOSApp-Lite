package app.sosapp.sos.sosapp.utils

import android.content.Context
import android.content.res.Configuration
import android.util.Log
import app.sosapp.sos.sosapp.R
import app.sosapp.sos.sosapp.data.SOSAppSharedPrefs
import java.util.Locale

object LocaleHelper {

    fun onAttach(mContext: Context, defaultLanguage: String? = null, defaultLanguageName: String? = null): Context {
        val mPrefs = SOSAppSharedPrefs.getInstance(mContext)
        val lang = mPrefs.selectedLanguageCode?:defaultLanguage?:Locale.getDefault().language
        val name = mPrefs.selectedLanguageName?:defaultLanguageName?:Locale.getDefault().displayLanguage
        return setLocale(mContext, lang, name)
    }

    //Always use applicationContext for mContext
    fun setLocale(mContext: Context, languageCode: String, languageName: String): Context {
        setPersistedData(mContext, languageCode, languageName)
        val context = updateResources(mContext, languageCode)
        SOSAppRes.setContext(context)
        return context
    }

    private fun setPersistedData(mContext: Context, languageCode: String, languageName: String) {
        with(SOSAppSharedPrefs.getInstance(mContext)){
            selectedLanguageCode = languageCode
            selectedLanguageName = languageName
        }
    }

    private fun updateResources(mContext: Context, language: String): Context {
        val locale = Locale(language)
        Locale.setDefault(locale)
        mContext.resources.let { res ->
            val config = Configuration(res.configuration)
            config.setLocale(locale)
            return mContext.createConfigurationContext(config)
        }
    }


    fun getSelectedLanguageCodePosition(mContext: Context): Int{
        kotlin.runCatching {
            val langCodes = mContext.resources.getStringArray(R.array.arr_languages_codes)
            val pos = langCodes.indexOf(SOSAppSharedPrefs.getInstance(mContext).selectedLanguageCode)
            Log.d("getLangCodePosition", "$pos ${langCodes[pos]}")
            return pos
        }.onFailure {
            Log.e("getLangCodePosition", "Get selLangPos Exc : $it")
        }
        return 0
    }

    fun getLanguageByPosition(mContext: Context, pos: Int): Pair<String, String>{
        val langCodes = mContext.resources.getStringArray(R.array.arr_languages_codes)
        val lang = mContext.resources.getStringArray(R.array.arr_languages)
        if (pos >= 0 && pos < lang.size){
            return Pair(langCodes[pos], lang[pos])
        }
        return Pair(langCodes[0], lang[0])
    }

}