package USerVip

import android.os.Bundle
import com.mandi.intelimeditor.common.BaseActivity
import com.mandi.intelimeditor.common.appInfo.TheUserUtil
import com.mandi.intelimeditor.common.dataAndLogic.AllData
import com.mandi.intelimeditor.R
import kotlinx.android.synthetic.main.activity_pay_problem_measure.*

/**
 * 处理已付款但开通会员失败等支付问题的AC
 */
class PayProblemMeasureActivity : BaseActivity() {
    override fun getLayoutResId(): Int {
        return R.layout.activity_pay_problem_measure
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (TheUserUtil.hasLoggedLastTime()) {
            tv_show_user_id.text = AllData.localUserId
        } else {
            tv_show_user_id.setText(R.string.please_login_to_get_id)
        }
    }
}
