package com.mandi.intelimeditor.dialog

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import com.mandi.intelimeditor.common.util.Util
import com.mandi.intelimeditor.user.userSetting.FeedBackActivity
import com.mandi.intelimeditor.R
import kotlinx.android.synthetic.main.dialog_rate_us.*

class RateUsDialog : IBaseDialog() {

    override fun getLayoutResId(): Int {
        return R.layout.dialog_rate_us
    }

    private var alphaAnimation1 = AlphaAnimation(0.1f, 1.0f)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initListener()
        startAnim()
    }

    private fun toNext(it: View?) {
        it?.let {
            it.postDelayed({
                Util.goToMarket(context, context?.packageName)
                dismiss()
            }, 200)
        }
    }

    private fun finish(it: View?) {
        it?.postDelayed({
            startActivity(Intent(activity, FeedBackActivity::class.java))
            dismiss()
        }, 200)
    }

    private fun startAnim() {
        //闪烁
        alphaAnimation1 = AlphaAnimation(0.2f, 1.0f)
        alphaAnimation1.duration = 300
        alphaAnimation1.repeatCount = Animation.INFINITE
        alphaAnimation1.repeatMode = Animation.REVERSE
        rateStar5.animation = alphaAnimation1
        alphaAnimation1.start()
    }

    override fun onDismiss(dialog: DialogInterface) {
        alphaAnimation1.cancel()
        super.onDismiss(dialog)
    }

    /**
     * 初始化点击时间
     */
    private fun initListener() {
        rateStar5.setOnRatingBarChangeListener { ratingBar, rating, _ ->
            if (rating >= 4) {
                toNext(ratingBar)
            } else {
                finish(ratingBar)
            }
        }

    }

}
