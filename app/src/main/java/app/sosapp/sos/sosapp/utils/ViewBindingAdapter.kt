package app.sosapp.sos.sosapp.utils

import android.graphics.drawable.Drawable
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.databinding.BindingAdapter
import app.sosapp.sos.sosapp.R
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions

object ViewBindingAdapter {

    @JvmStatic
    @BindingAdapter(value = ["bind_isVisible"], requireAll = false)
    fun setViewVisibility(view: View, isVisible: Boolean) {
        view.isVisible = isVisible
    }

    @JvmStatic
    @BindingAdapter(value = ["bind_isUnderlined"], requireAll = true)
    fun setTextUnderline(textView: TextView, isUnderlined: Boolean) {
        textView.isUnderlined = isUnderlined
    }


    @JvmStatic
    @BindingAdapter(value = [
        "bind_glideUrl",
        "bind_glideRes",
        "bind_glideRequestOptions",
        "bind_glideIsCenterCrop",
        "bind_glidePlaceholder",
        "bind_glideError",
        "bind_glideSkipMemoryCache",
        "bind_glideDiskCacheStrategyNone"], requireAll = false)
    fun loadImageWithGlide(
        imgView: ImageView, imageUrl: String?, imageRes: Int?, reqOptions: RequestOptions?, isCenterCrop: Boolean,
        placeholderRes: Int?, errorRes: Int?, skipMemCache: Boolean, isStrategyNone: Boolean
    ) {
        imgView.loadImageWithGlide(imageUrl = imageUrl, imageRes = imageRes, reqOptions = reqOptions,
            isCenterCrop = isCenterCrop, placeholderRes = placeholderRes?: R.color.colorTransparent, errorRes = errorRes?:R.color.colorTransparent,
            skipMemCache = skipMemCache, strategy = (if (isStrategyNone) DiskCacheStrategy.NONE else DiskCacheStrategy.ALL))
    }

    @JvmStatic
    @BindingAdapter(value = ["bind_imageDrawable"], requireAll = true)
    fun setImageDrawable(imgView: ImageView, drawable: Drawable?){
        imgView.setImageDrawable(drawable)
    }

}