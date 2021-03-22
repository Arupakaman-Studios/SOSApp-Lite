package app.sosapp.sos.sosapp.uiModules.other

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import androidx.annotation.StringRes
import androidx.core.widget.TextViewCompat
import app.sosapp.sos.sosapp.R
import app.sosapp.sos.sosapp.utils.getDisplaySize
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.textview.MaterialTextView

class CenteredTitleToolbar : MaterialToolbar {

    private lateinit var tvTitle: MaterialTextView
    private var screenWidth = 0
    private var centerTitle = true

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    private fun init() {
        screenWidth = context.getDisplaySize().first
        tvTitle = MaterialTextView(context)
        TextViewCompat.setTextAppearance(tvTitle, R.style.Theme_SOSApp_TitleTextAppearance)
        tvTitle.text = ""
        addView(tvTitle)
    }

    @SuppressLint("DrawAllocation")
    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)
        if (centerTitle) {
            val location = IntArray(2)
            tvTitle.getLocationOnScreen(location)
            tvTitle.translationX = tvTitle.translationX + (-location[0] + screenWidth / 2 - tvTitle.width / 2)
        }
    }

    override fun setTitle(title: CharSequence?) {
        tvTitle.text = title
        requestLayout()
    }

    override fun setTitle(@StringRes titleRes: Int) {
        tvTitle.setText(titleRes)
        requestLayout()
    }

    fun setTitleCentered(centered: Boolean) {
        centerTitle = centered
        requestLayout()
    }
}
