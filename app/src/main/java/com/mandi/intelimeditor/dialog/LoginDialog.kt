package com.mandi.intelimeditor.dialog

import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.mandi.intelimeditor.R

/**
 * 作者：yonglong
 * 包名：com.mandi.intelimeditor.widget
 * 时间：2019/4/3 13:21
 * 描述：
 */
class LoginDialog : IBaseDialog() {
    private val tv: TextView? = null
    var qqLoginListener: (() -> Unit)? = null

    override fun getLayoutResId(): Int {
        return R.layout.layout_user_login_choose
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        rootView?.setOnClickListener { dismissAllowingStateLoss() }
        rootView?.findViewById<View>(R.id.fab_qq_login)?.setOnClickListener {
            qqLoginListener?.invoke()
        }
    }

    companion object {
        private val TAG = "LoginDialog"

        fun newInstance(): LoginDialog {
            val args = Bundle()
            val fragment = LoginDialog()
            fragment.arguments = args
            return fragment
        }
    }


}
