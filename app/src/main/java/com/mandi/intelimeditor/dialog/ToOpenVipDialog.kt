package com.mandi.intelimeditor.dialog

import android.os.Bundle
import android.view.View
import com.mandi.intelimeditor.ad.ttAD.videoAd.TTRewardVad
import com.mandi.intelimeditor.common.dataAndLogic.AllData
import com.mandi.intelimeditor.common.util.LogUtil
import com.mandi.intelimeditor.common.util.Util
import com.mandi.intelimeditor.user.US
import com.mandi.intelimeditor.user.userVip.OpenVipActivity
import com.mandi.intelimeditor.R
import kotlinx.android.synthetic.main.dialog_to_open_vip.*
import kotlinx.android.synthetic.main.layout_to_open_vip.*

/**
 * 激励视频广告
 */
class ToOpenVipDialog : IBaseDialog() {
    var mAdName: String? = null
    private var mTitle: String? = null
    lateinit var mActivity: androidx.fragment.app.FragmentActivity

    companion object {
        fun newInstance(activity: androidx.fragment.app.FragmentActivity): ToOpenVipDialog {
            val args = Bundle()
            val fragment = ToOpenVipDialog()
            fragment.setCanceledOnTouchOutSize(false)
            fragment.arguments = args
            fragment.mActivity = activity
            return fragment
        }
    }

    fun setAdName(name: String) {
        mAdName = name
    }

    fun setTitle(title1: String) {
        mTitle = title1
    }

    override fun getLayoutResId(): Int {
        return R.layout.dialog_to_open_vip
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        view?.setOnClickListener {
            dismissAllowingStateLoss()
        }
        to_open_vip_notice.visibility = View.GONE
        to_open_vip_title.text = mContext?.getString(R.string.vip_free_ad_floor_price, AllData.floor_vip_price)
//        to_open_vip_bg.setOnClickListener {
//            // 用来阻止点击对话小时的，其他方法没找到可行的
//        }
        to_open_vip_include.setOnClickListener {
            toOpenVip(it)
        }
        btn_to_open_vip.setOnClickListener {
            toOpenVip(it)
        }
    }

    private fun toOpenVip(view: View) {
        if (!Util.DoubleClick.isDoubleClick(2500)) { // 启动较慢，防止多次点击
            US.putOpenVipEvent(US.CLICK_AD_TO_VIP)
            OpenVipActivity.startOpenVipAc(mActivity)
        }
        dismissAllowingStateLoss()
    }

    /**
     *显示出对话框
     */
    fun showIt() {
        if (AllData.isCloseVipFunction) return;
        super.showIt(mActivity)
        LogUtil.d(TTRewardVad.TAG, "显示去开通VIP对话框")
    }
}