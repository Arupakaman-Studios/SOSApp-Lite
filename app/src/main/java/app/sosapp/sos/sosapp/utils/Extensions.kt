package app.sosapp.sos.sosapp.utils

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Paint
import android.graphics.Point
import android.net.ConnectivityManager
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import app.sosapp.sos.sosapp.R
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import java.util.concurrent.Future

operator fun <T> T.invoke(block: T.() -> Unit) = block()

/**
 *   Toast
 */

fun Context.toast(mMsg: String) {
    Toast.makeText(this, mMsg, Toast.LENGTH_SHORT).show()
}

fun Context.toast(@StringRes mResId: Int) {
    toast(SOSAppRes.getString(mResId))
}

fun Context.toastLong(mMsg: String) {
    Toast.makeText(this, mMsg, Toast.LENGTH_LONG).show()
}

fun Context.toastLong(@StringRes mResId: Int) {
    toastLong(SOSAppRes.getString(mResId))
}

/**
 *  View Properties
 */

var TextView.isUnderlined: Boolean
    get() = ((paintFlags and Paint.UNDERLINE_TEXT_FLAG) == Paint.UNDERLINE_TEXT_FLAG)
    set(isUnderlined) {
        paintFlags = if(isUnderlined) (paintFlags or Paint.UNDERLINE_TEXT_FLAG) else (paintFlags xor Paint.UNDERLINE_TEXT_FLAG)
    }

fun View.disable(mAlpha: Float = 0.4f){
    isEnabled = false
    alpha = mAlpha
}

fun View.enable(){
    isEnabled = true
    alpha = 1.0f
}

/**
 *   Glide
 */

fun ImageView.loadImageWithGlide(activity: FragmentActivity? = null, fragment: Fragment? = null, imageUrl: String?, @DrawableRes imageRes: Int?,
                                 reqOptions: RequestOptions? = null, isCenterCrop: Boolean = false, @DrawableRes @ColorRes placeholderRes: Int = R.color.colorTransparent,
                                 @DrawableRes @ColorRes  errorRes: Int = R.color.colorTransparent, skipMemCache: Boolean = false, strategy: DiskCacheStrategy = DiskCacheStrategy.ALL) {
    when{
        activity != null -> Glide.with(activity).asBitmap()
        fragment != null -> Glide.with(fragment).asBitmap()
        else -> Glide.with(this).asBitmap()
    }.also {request->
        when {
            !imageUrl.isNullOrBlank() -> request.load(imageUrl)
            imageRes != null -> request.load(imageRes)
            else -> request.load(R.color.colorTransparent)
        }.apply {
            (if (reqOptions == null){
                (if (isCenterCrop) centerCrop() else fitCenter())
                    .placeholder(placeholderRes)
                    .error(errorRes)
                    .skipMemoryCache(skipMemCache)
                    .diskCacheStrategy(strategy)
            }else{
                apply(reqOptions)
            }).into(this@loadImageWithGlide)
        }
    }
}

/**
 *   DataBinding
 */

fun <T : ViewDataBinding> LayoutInflater.inflateBinding(
    @LayoutRes resId: Int,
    container: ViewGroup?,
    attachToParent: Boolean = false
): T {
    return DataBindingUtil.inflate(this, resId, container, attachToParent)
}

/**
 *   Network Connectivity Functions
 */

fun Context.isNetConnected(): Boolean {
    kotlin.runCatching {
        (applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?)?.let { conMan ->
            val capabilities = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                conMan.getNetworkCapabilities(conMan.activeNetwork)
            } else {
                @Suppress("DEPRECATION")
                conMan.activeNetworkInfo
            }
            if (capabilities != null) {
                return true
            }
        }
    }.onFailure {
        Log.e("getNetState", "Exc isNetConnected $it")
    }
    return false
}

/**
 *   Device Info Functions
 */

fun Context.getDisplaySize(): Pair<Int, Int> {
    var pair = Pair(0, 0)
    (getSystemService(Context.WINDOW_SERVICE) as WindowManager?)?.let { windowManager ->
        pair = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val screenSize = Point()
            @Suppress("DEPRECATION")
            display?.getRealSize(screenSize)
            Pair(screenSize.x, screenSize.y)
        } else {
            val screenSize = Point()
            @Suppress("DEPRECATION")
            windowManager.defaultDisplay.getRealSize(screenSize)
            Pair(screenSize.x, screenSize.y)
        }
    }
    return pair
}

@SuppressLint("HardwareIds")
fun Context.getAndroidDeviceId(): String {
    kotlin.runCatching {
        return Settings.Secure.getString(this.contentResolver, Settings.Secure.ANDROID_ID)
    }
    return "NA"
}

/**
 *   Executors
 */

fun Future<*>?.cancelIfActive(){
    this?.runCatching {
        if (!isCancelled || !isDone){
            cancel(true)
        }
    }
}



