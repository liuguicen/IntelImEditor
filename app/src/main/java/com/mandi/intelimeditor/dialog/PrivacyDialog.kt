package com.mandi.intelimeditor.dialog

import android.content.Intent
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.mandi.intelimeditor.common.appInfo.IntelImEditApplication
import com.mandi.intelimeditor.common.util.Util
import com.mandi.intelimeditor.user.useruse.AppAgreementActivity
import com.mandi.intelimeditor.R
import kotlinx.android.synthetic.main.dialog_privacy.*

/**
 * 作者：yonglong
 * 描述：启动对话框
 */
class PrivacyDialog : IBaseDialog() {
    private val tv: TextView? = null
    var agreeListener: ((agree: Boolean) -> Unit)? = null

    override fun getLayoutResId(): Int {
        return R.layout.dialog_privacy
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initAppAgreement()
        agreeTv.setOnClickListener {
            dismiss()
            agreeListener?.invoke(true)
        }
        disagreeTv.setOnClickListener {
            dismiss()
            agreeListener?.invoke(false)
        }
    }

    private fun initAppAgreement() {
        var permissionUseStatement = mContext?.getString(R.string.permission_use_statement);
        val text = "欢迎使用暴走P图应用！为了更好保障您个人权益，在使用本产品前请认真阅读我们的《用户协议》和《隐私政策》\n" +
                permissionUseStatement +
                "如您同意，请您点击“同意”开始接受我们的服务\n"
        val spannableString = SpannableString(text)
        val clickableSpan: ClickableSpan = object : ClickableSpan() {
            override fun onClick(view: View) {
                val intent = Intent(activity, AppAgreementActivity::class.java)
                intent.action = AppAgreementActivity.INTENT_ACTION_USER_AGREEMENT
                startActivity(intent)
            }

            override fun updateDrawState(ds: TextPaint) {
                ds.color = ContextCompat.getColor(IntelImEditApplication.appContext, R.color.colorPrimary) //设置颜色
                ds.isUnderlineText = false // 去掉下划线
            }
        }
        try {
            var start = text.indexOf("用户")
            var end = text.indexOf('议') + 1
            spannableString.setSpan(clickableSpan, start, end, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
            val colorSpan = ForegroundColorSpan(ContextCompat.getColor(IntelImEditApplication.appContext, R.color.colorPrimary))
            spannableString.setSpan(colorSpan, start, end, Spannable.SPAN_EXCLUSIVE_INCLUSIVE)

            val clickableSpan2: ClickableSpan = object : ClickableSpan() {
                override fun onClick(view: View) {
                    val intent = Intent(IntelImEditApplication.appContext, AppAgreementActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    intent.action = AppAgreementActivity.INTENT_ACTION_PRIVACY_POLICY
                    startActivity(intent)
                }

                override fun updateDrawState(ds: TextPaint) {
                    ds.color = ContextCompat.getColor(IntelImEditApplication.appContext, R.color.colorPrimary) //设置颜色
                    ds.isUnderlineText = false // 去掉下划线
                }
            }
            start = text.indexOf('隐')
            end = text.indexOf("策") + 1
            spannableString.setSpan(clickableSpan2, start, end, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
            val colorSpan2 = ForegroundColorSpan(ContextCompat.getColor(IntelImEditApplication.appContext, R.color.colorPrimary))
            spannableString.setSpan(colorSpan2, start, end, Spannable.SPAN_EXCLUSIVE_INCLUSIVE)

            start = text.indexOf("定位")
            end = text.indexOf("我们只会") - 1
            if (end > start) {
                spannableString.setSpan(StyleSpan(Typeface.BOLD), start, end, Spannable.SPAN_EXCLUSIVE_INCLUSIVE)
            }
            start = text.indexOf("通话")
            end = text.indexOf("我们需要") - 1

            if (end > start) {
                spannableString.setSpan(StyleSpan(Typeface.BOLD), start, end, Spannable.SPAN_EXCLUSIVE_INCLUSIVE)
            }
            if (Build.VERSION.SDK_INT < 21) { // 5.0 以下机器屏幕太小
                use_statement_content.maxHeight = Util.dp2Px(200f);
            }
            use_statement_content.text = spannableString
            use_statement_content.movementMethod = LinkMovementMethod.getInstance();
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

}