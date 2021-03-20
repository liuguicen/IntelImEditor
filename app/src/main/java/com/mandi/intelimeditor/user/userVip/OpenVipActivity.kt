package com.mandi.intelimeditor.user.userVip;

import USerVip.PayProblemMeasureActivity
import USerVip.SetMealsAdapter
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.alipay.sdk.app.EnvUtils
import com.mandi.intelimeditor.bean.VipSetMeal
import com.mandi.intelimeditor.common.BaseActivity
import com.mandi.intelimeditor.common.CommonConstant
import com.mandi.intelimeditor.common.appInfo.TheUserUtil
import com.mandi.intelimeditor.common.dataAndLogic.AllData
import com.mandi.intelimeditor.common.util.LogUtil
import com.mandi.intelimeditor.common.util.ToastUtils
import com.mandi.intelimeditor.common.util.Util
import com.mandi.intelimeditor.pay.PayWayUtil
import com.mandi.intelimeditor.pay.alipay.AliPayTools
import com.mandi.intelimeditor.pay.alipay.PayConstants
import com.mandi.intelimeditor.user.US
import com.mandi.intelimeditor.user.userSetting.SettingActivity
import com.mandi.intelimeditor.user.userVip.PayVipPresenterImp
import com.mandi.intelimeditor.user.useruse.AppAgreementActivity
import com.mandi.intelimeditor.BuildConfig
import com.mandi.intelimeditor.R
import kotlinx.android.synthetic.main.activity_open_vip.*

/**
 * 购买VIP 界面
 */
class OpenVipActivity : BaseActivity(), PayVipContract.View {

    private var mSetMealsAdapter: SetMealsAdapter? = null
    private var payWay = PayWayUtil.PAY_WAY_ALIPAY
    private var mPresenter: PayVipPresenterImp? = null

    /**
     * VIP套餐，默认选中第一个
     */
    private var chosenSetMeal: VipSetMeal? = null
    private var vipSetMeals = mutableListOf<VipSetMeal>()

    override fun onCreate(savedInstanceState: Bundle?) {
        //是否开启沙箱模式
        if (BuildConfig.SANBOX) {
            EnvUtils.setEnv(EnvUtils.EnvEnum.SANDBOX)
        }
        super.onCreate(savedInstanceState)
        mPresenter = PayVipPresenterImp(this, this)
        initSetMealList()
        initPayWay()
        initOthers()
        //获取所有的VIP
        mPresenter?.getAllVipSetMeals()
    }

    override fun getLayoutResId(): Int {
        return R.layout.activity_open_vip
    }

    /**
     * 刷新选中状态
     */
    private fun refreshPayView() {
        wXinPayView.findViewById<View>(R.id.payWayCheckView).visibility = View.GONE
        aliPayView.findViewById<View>(R.id.payWayCheckView).visibility = View.VISIBLE
    }

    /**
     * 初始化支付方式
     */
    private fun initPayWay() {
        //初始化微信支付
        wXinPayView.setOnClickListener {
            ToastUtils.show("暂不支持微信支付")
        }
        wXinPayView.findViewById<TextView>(R.id.payWayNameIv).setText(R.string.weixin_pay)
        wXinPayView.findViewById<ImageView>(R.id.payWayIconIv).setImageResource(R.drawable.wx_pay_logo)

        //初始化支付宝支付
        aliPayView.setOnClickListener {
            payWay = PayWayUtil.PAY_WAY_ALIPAY
            refreshPayView()
        }
        aliPayView.findViewById<TextView>(R.id.payWayNameIv).setText(R.string.alipay)
        aliPayView.findViewById<ImageView>(R.id.payWayIconIv).setImageResource(R.drawable.alipay_logo)
        refreshPayView()
    }

    /**
     * 初始化VIP套餐列表
     */
    private fun initSetMealList() {
        chosenSetMeal = if (vipSetMeals.size > 0) vipSetMeals[0] else null
        mSetMealsAdapter = SetMealsAdapter(this, vipSetMeals)
        mSetMealsAdapter?.setOnItemClickListener(object : SetMealsAdapter.OnItemClickListener {
            override fun onItemClick(view: View, position: Int) {
                chosenSetMeal = vipSetMeals[position]
            }
        })
        vipListRv.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)
        vipListRv.adapter = mSetMealsAdapter
    }

    private fun initOthers() {
        vip_service_agreement.setOnClickListener {
            var intent = Intent(this, AppAgreementActivity::class.java)
            intent.action = AppAgreementActivity.INTENT_ACTION_VIP_SERVICE_AGREEMENT
            startActivity(intent)
        }
        btn_to_pay_problem_measure.setOnClickListener {
            startActivity(Intent(this, PayProblemMeasureActivity::class.java))
        }
    }

    /**
     * 立即支付
     *
     * @param view
     */
    fun payVip(view: View) {
        if (Util.DoubleClick.isDoubleClick(2000)) { // 可能支付app反应慢，防止重复点击
            return
        }
        if (AllData.isVip) {
            ToastUtils.show(R.string.has_opened_vip)
            return
        }
        val tradeCode = "BZPT" + System.currentTimeMillis()
        val topicId = chosenSetMeal?.objectId
        if (chosenSetMeal == null) {
            ToastUtils.show("请选择一种会员套餐！")
            return
        }
        chosenSetMeal?.disCountPrice?.let {
            US.putOpenVipEvent(US.CLICK_PAY_FOR_OPEN_VIP)
            US.putOpenVipEvent(US.USER_CHOSE_VIP_PRICE + ": " + chosenSetMeal!!.getDisCountPrice())
            mPresenter?.preparePay(PayConstants.URL_PREPARE_ORDER_VIP, it, payWay, tradeCode, topicId)
        }
    }

    companion object {
        val REQUEST_CODE_FROM_SETTING = 2001
        val REQUEST_CODE_FROM_OTHERS = 11
        val REQUEST_CODE_UNLOCK_RESOURCE = 1001

        fun startOpenVipAc(activity: Activity) {
            if (AllData.isCloseVipFunction) return
            if (TheUserUtil.hasLoggedLastTime()) {
                val intent = Intent(activity, OpenVipActivity::class.java)
                activity.startActivityForResult(intent, OpenVipActivity.REQUEST_CODE_FROM_SETTING)
            } else { // 如果用户还未登录，先登录
                val intent = Intent(activity, SettingActivity::class.java)
                intent.action = CommonConstant.ACTION_LOGIN_FOR_OPEN_VIP
                activity.startActivityForResult(intent, CommonConstant.REQUEST_CODE_LOGIN_FOR_OPEN_VIP)
            }
        }
    }

    /**
     * 支付宝支付返回
     */
    override fun dealPayResult(orderInfo: String?) {
        runOnUiThread {
            AliPayTools.aliPay(this, orderInfo) { code, msg ->
                dealPayResult(this, code, msg, null)
            }
        }
    }

    override fun showAllVipSetMeals(vipSetMeals: MutableList<VipSetMeal>?) {
        vipSetMeals?.let {
            this.vipSetMeals = it
            chosenSetMeal = if (vipSetMeals.size > 0) vipSetMeals[0] else null
            mSetMealsAdapter?.setVipList(vipSetMeals)
        }
    }

    override fun showNoVip() {
    }

    override fun setPresenter(presenter: Any?) {
    }

    /**
     * 9000	订单支付成功
    8000	正在处理中，支付结果未知（有可能已经支付成功），请查询商户订单列表中订单的支付状态
    4000	订单支付失败
    5000	重复请求
    6001	用户中途取消
    6002	网络连接出错
    6004	支付结果未知（有可能已经支付成功），请查询商户订单列表中订单的支付状态
    其它	其它支付错误
     */
    private fun dealPayResult(ctx: Context, code: Int, info: String, onDismiss: DialogInterface.OnDismissListener?) {
        var result = when (code) {
            9000 -> "恭喜您开通会员成功！"
            8000 -> "正在处理中，支付结果未知（有可能已经支付成功）"
            4000 -> "订单支付失败"
            5000 -> "重复请求"
            6001 -> "用户中途取消"
            6002 -> "网络连接出错"
            6004 -> "支付结果未知（有可能已经支付成功），请查询商户订单列表中订单的支付状态"
            else -> "其它支付错误"
        }
        if (code != 9000) {
            result += "\n如果您已付款但开通VIP失败，请点击开通页面右下方按钮联系我们"
        }
        val time = (chosenSetMeal?.time ?: 0) * 24 * 60 * 60 * 1000.toLong()
        LogUtil.d(info)
        if (code == 9000) { // 支付成功
            TheUserUtil.updateVipExpire(AllData.localUserId, (System.currentTimeMillis() + time), null)
            AllData.hasOpenVipJust = true
            setResult(CommonConstant.RESULT_CODE_OPEN_VIP_SUCCESS)
            US.putOpenVipEvent(US.OPEN_VIP_SUCCESS)
            Log.d("---", "支付结果 支付成功")
        } else if (code == 6001) {
            US.putOtherEvent(US.OPEN_VIP_CANCEL_IN_PAY)
        } else {
            US.putOpenVipEvent(US.OPEN_VIP_FAILED_IN_PAY + code)
        }

        if (!isDestroyed) {
            AlertDialog.Builder(ctx)
                    .setTitle("支付结果")
                    .setMessage(result)
                    .setPositiveButton(R.string.confirm) { dialog, which ->
                        if (code == 9000) {
                            Log.d("---", "支付结果 code = " + code)
                            finish()
                        }
                    }
                    .show()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        US.putOpenVipEvent(US.OPEN_VIP_IN_PAY)
    }
}
