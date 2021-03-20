package com.mandi.intelimeditor.home.view

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import com.mandi.intelimeditor.R
import kotlinx.android.synthetic.main.item_func_list.view.*

class BottomFunctionView : FrameLayout {
    var onClickListener: ((Int) -> Unit)? = null
    var mContext: Context? = null

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        attrs?.let { initView(context, it) }
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        attrs?.let { initView(context, it) }
    }

    private fun initView(context: Context, attrs: AttributeSet) {
        mContext = context
        LayoutInflater.from(context).inflate(R.layout.item_func_list, this, true)
        val ta = context.obtainStyledAttributes(attrs, R.styleable.BottomFunctionView)
        val title = ta.getString(R.styleable.BottomFunctionView_bfv_title)
        val colors = ta.getColorStateList(R.styleable.BottomFunctionView_bfv_color)
        val icon = ta.getDrawable(R.styleable.BottomFunctionView_bfv_icon)
        val iconBg = ta.getDrawable(R.styleable.BottomFunctionView_bfv_icon_bg)
        val isLock = ta.getBoolean(R.styleable.BottomFunctionView_bfv_is_lock, false)
        ta.recycle()

        titleTv.text = title
        iconIv.setImageDrawable(icon)
        if (iconBg != null) {
            iconIv.background = iconBg
        }
        colors?.let {
            titleTv.setTextColor(it)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                iconIv.imageTintList = it
            }
        }
        lockIv.visibility = if (isLock) View.VISIBLE else View.GONE
    }

    fun setTitle(title: String) {
        titleTv.text = title
    }

    fun setIconBackgroundResource(resImageId: Int) {
        iconIv.setBackgroundResource(resImageId)
    }

    fun setTintColor(resColorId: Int) {
        mContext?.let {
            titleTv.setTextColor(ContextCompat.getColorStateList(it, resColorId))
        };
    }
}