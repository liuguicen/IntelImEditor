package com.mandi.intelimeditor.dialog

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import com.mandi.intelimeditor.EventName
import com.mandi.intelimeditor.ad.ADHolder
import com.mandi.intelimeditor.ad.AdData
import com.mandi.intelimeditor.ad.AdStrategyUtil
import com.mandi.intelimeditor.ad.tencentAD.AdUtil
import com.mandi.intelimeditor.ad.tencentAD.TxFeedAd
import com.mandi.intelimeditor.ad.tencentAD.TxRewardVad
import com.mandi.intelimeditor.ad.ttAD.videoAd.TTRewardVad
import com.mandi.intelimeditor.ad.ttAD.videoAd.TTRewardVadManager
import com.mandi.intelimeditor.ad.ttAD.videoAd.VadListener
import com.mandi.intelimeditor.common.dataAndLogic.AllData
import com.mandi.intelimeditor.common.util.LogUtil
import com.mandi.intelimeditor.common.util.ToastUtils
import com.mandi.intelimeditor.common.util.Util
import com.mandi.intelimeditor.user.US
import com.mandi.intelimeditor.R
import com.mandi.intelimeditor.common.appInfo.AppConfig
import kotlinx.android.synthetic.main.dialog_unlock_layout.*
import kotlinx.android.synthetic.main.layout_to_open_vip.*

/**
 * 激励视频广告
 */
class UnlockDialog : IBaseDialog() {
    var isRewardSuccess = false
    var mAdPositionName: String? = null
    var unlockListener: ((Boolean) -> Unit)? = null
    var toOpenVipListener: View.OnClickListener? = null
    var rewardVadListener: VadListener? = null
    var adStrategyUtil: AdStrategyUtil? = null
    private var mAdRes: String? = ""
    private var mTitle: String? = null
    private var btnText: String? = null

    companion object {
        fun newInstance(): UnlockDialog {
            val args = Bundle()
            val fragment = UnlockDialog()
            fragment.arguments = args
            return fragment
        }
    }

    fun setAdPositionName(name: String) {
        mAdPositionName = name
    }

    fun setTitle(title1: String) {
        mTitle = title1
    }

    fun setBtnText(text: String) {
        btnText = text
    }

    override fun getLayoutResId(): Int {
        return R.layout.dialog_unlock_layout
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        view?.setOnClickListener {
            dismissAllowingStateLoss()
        }
        unlock_title.text = mTitle
        if (btnText != null)
            btn_unlock.text = btnText
        to_open_vip_title.text = mContext?.getString(R.string.vip_resources_floor_price, AllData.floor_vip_price)

        adStrategyUtil = AdStrategyUtil(AdData.AdSpaceName.REWARD_VAD, AllData.appConfig.reward_vad_strategy)
        rewardVadListener = object : VadListener {
            override fun onVideoPlayComplete() {
                putEvent(US.VIDEO_PLAY_COMPLETE, mAdRes)
            }

            override fun onAdClose() {
                unlockListener?.invoke(isRewardSuccess) //  获取奖励成功，回调解锁
            }

            override fun onAdShow() {
                putEvent(US.EXPOSURE, mAdRes)
                AdData.onRewardVadShow();
                dismissAllowingStateLoss()
            }

            override fun onVideoAdBarClick() {
                putEvent(US.CLICK_VIDEO_BAR, mAdRes)
            }

            override fun onRewardVerify(isGetReward: Boolean, var2: Int, var3: String?) {
                isRewardSuccess = isGetReward
            }

            override fun onError(errorCode: Int, msg: String, res: String) {
                val eventKey: String = US.FAILED + " " + errorCode;
                adStrategyUtil?.remove(res) // 出错了，去掉出错的这个广告，再次显示
                chooseRewardVad2Show(adStrategyUtil, rewardVadListener)
                putEvent(eventKey, mAdRes)
                // 重要，激励视频出错必须调用
                AdData.isRewardVadError = true;
                ToastUtils.show(R.string.network_error_try_latter)
                LogUtil.e("$mAdRes error", errorCode.toString() + msg);
            }

            override fun onVideoError() {
                val event: String = US.FAILED + " video error"
                // 视频出错，展示不再次显示
                putEvent(event, mAdRes)
            }

            override fun startDownload() {
                putEvent(US.START_DOWNLOAD, mAdRes)
            }

            override fun downLoadFinish() {
                putEvent(US.DOWNLOAD_COMPLETE, mAdRes)
            }

            override fun onInstallSuccess() {
                putEvent(US.INSTALL_SUCCESS, mAdRes)
            }

            override fun onSkipVideo() {

            }
        }
        btn_unlock.setOnClickListener {
            ToastUtils.show("视频加载中...")
            chooseRewardVad2Show(adStrategyUtil, rewardVadListener)
        }
        if (AppConfig.isCloseVipFunction) {
            replaceOpenVipWithAD()
        }
        to_open_vip_layout.setOnClickListener {
            if (!Util.DoubleClick.isDoubleClick(2500)) { // 启动较慢，防止多次点击
                US.putOpenVipEvent(US.CLICK_LOCKED_RESOURCES_TO_VIP)
                toOpenVipListener?.onClick(it)
            }
            dismissAllowingStateLoss()
        }
    }

    /**
     * 增加打点统计
     */
    private fun putEvent(eventKey: String, mAdRes: String?) {
        when (mAdPositionName) {
            AdData.TYPEFACE_REWARD_AD -> US.putTypefaceRewardAd("${mAdRes}-$eventKey")
            AdData.REWARD_AD_NAME_TEMPLATE -> US.putTemplateRewardAd("${mAdRes}-$eventKey")
            AdData.REWARD_AD_NAME_TIETU -> US.putTietuRewardAd("${mAdRes}-$eventKey")
        }
    }

    private fun chooseRewardVad2Show(adStrategyUtil: AdStrategyUtil?, rewardVadListener: VadListener?) {
        if (adStrategyUtil!!.isShow("TT")) {
            mAdRes = "TT"
            TTRewardVadManager.getInstance().setRewardVadListener(rewardVadListener)
            TTRewardVadManager.getInstance().loadAd(activity)
        } else if (adStrategyUtil.isShow("TX")) {
            mAdRes = "TX"
            val txRewardVideoAd = TxRewardVad(activity)
            txRewardVideoAd.setRewardVadListener(rewardVadListener)
            txRewardVideoAd.loadAd()
        } else if ("TT" != mAdRes) { // 默认的,注意要没选过这个才选，已经选过是因为它出错了才调用这里，不能再次调用，否则死循环！！
            mAdRes = "TT"
            TTRewardVadManager.getInstance().setRewardVadListener(rewardVadListener)
            TTRewardVadManager.getInstance().loadAd(activity)
        }
        Log.d("RewardAD", "choose " + mAdRes)
        putEvent(US.CHOOSE_REWARD_VAD, mAdRes)
    }

    /**
     *显示出对话框
     */
    override fun showIt(context: Context?) {
        super.showIt(context)
        LogUtil.d(TTRewardVad.TAG, "显示解锁对话框")
        putEvent(US.SHOW_UNLOCK_DIALOG, mAdRes)
    }

    fun showVideoAd() {
        val rewardVideoAdManager = TTRewardVadManager()
    }

    private fun replaceOpenVipWithAD() {
        to_open_vip_layout.visibility = View.GONE
        ad_in_unlock_dialog.visibility = View.VISIBLE
        val dp4 = Util.dp2Px(4f)
        unlock_title.setPadding(dp4, dp4, dp4, dp4)
        showTencentAd()
    }

    private fun showTencentAd() {
        // 没装满，并且大的一端没找到，直接新建，
        // 或者，装满了，但都没找到，删除并新建
        var adLayout = mContext?.findViewById<FrameLayout>(R.id.ad_in_unlock_dialog)
        val adMarkTv = AdUtil.createAdMark(mContext)
        adLayout?.addView(adMarkTv, adMarkTv.layoutParams)
        val newAd = TxFeedAd(ad_container,
                "4090687462043122", EventName.ad_in_unlock_dialog,
                EventName.ad_in_unlock_dialog)
        newAd.setAdMarkTv(adMarkTv)
        // 如果没加载成功，load广告资源，load之后内部可能调用bindData，所以这里判断，load了就不bind
        var holder = ADHolder(ad_container, ad_container)
        if (!newAd.isLoadSuccess()) {
            newAd.loadAdResources(holder)
        } else {
            newAd.bindData(holder)
        }
    }
}