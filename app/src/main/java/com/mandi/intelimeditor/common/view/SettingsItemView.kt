package com.mandi.intelimeditor.common.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import com.mandi.intelimeditor.R

/**
 * 设置界面条目
 */
class SettingsItemView : FrameLayout {
    private var mContext: Context? = null
    private var listener: OnItemClickListener? = null
    private var subTitle = ""

    constructor(context: Context) : super(context, null) {}

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initView(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initView(context, attrs)
    }

    private fun initView(context: Context, attrs: AttributeSet?) {
        val ta = context.obtainStyledAttributes(attrs, R.styleable.SettingsItemView)
        val title = ta.getString(R.styleable.SettingsItemView_siv_title)
        val subTitle = ta.getString(R.styleable.SettingsItemView_siv_subTitle)
        val showSwitch = ta.getBoolean(R.styleable.SettingsItemView_siv_showSwitch, false)
        ta.recycle()

        mContext = context
        val mView = LayoutInflater.from(context).inflate(R.layout.item_setting_view, this, true)
        this.subTitle = subTitle ?: ""
        mView.findViewById<TextView>(R.id.tv_title).text = title
        mView.findViewById<TextView>(R.id.tv_subTitle).text = subTitle
        mView.findViewById<SwitchCompat>(R.id.sw).isClickable = false
        mView.findViewById<SwitchCompat>(R.id.sw).visibility = if (showSwitch) View.VISIBLE else View.GONE
        mView.findViewById<TextView>(R.id.tv_subTitle).visibility = if (showSwitch) View.GONE else View.VISIBLE
        if (showSwitch) {
            mView.setOnClickListener { view ->
                mView.findViewById<SwitchCompat>(R.id.sw).isChecked = !mView.findViewById<SwitchCompat>(R.id.sw).isChecked
                listener?.click(view)
            }
        }
    }

    fun getSubTitle(): String {
        return this.subTitle
    }

    fun setSubTitle(title: String) {
        this.subTitle = title
        findViewById<TextView>(R.id.tv_subTitle).text = subTitle
    }

    fun isChecked(): Boolean {
        return findViewById<SwitchCompat>(R.id.sw).isChecked
    }

    fun setChecked(isChecked: Boolean) {
        findViewById<SwitchCompat>(R.id.sw).isChecked = isChecked
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.listener = listener
    }

    interface OnItemClickListener {
        fun click(view: View)
    }
}
