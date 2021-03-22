package app.sosapp.sos.sosapp.data

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.text.DateFormat

object JsonPojoParser {

    private val TAG by lazy { "JsonPojoParser" }

    private lateinit var mGson: Gson
    fun getGson(): Gson{
        if (!::mGson.isInitialized){
            mGson= GsonBuilder()
                    .setDateFormat(DateFormat.FULL, DateFormat.FULL).create()
        }
        return mGson
    }

}