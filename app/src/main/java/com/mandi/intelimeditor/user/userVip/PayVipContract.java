package com.mandi.intelimeditor.user.userVip;

import com.mandi.intelimeditor.BasePresenter;
import com.mandi.intelimeditor.BaseView;
import com.mandi.intelimeditor.bean.VipSetMeal;

import java.util.List;

/**
 * 支付界面业务
 */
interface PayVipContract {

    interface Presenter extends BasePresenter {

        /**
         * 准备支付
         *
         * @param payType
         */
        void preparePay(String url, double amount, int payType, String tradeCode, String topicId);


        void getAllVipSetMeals();
    }

    interface View extends BaseView {

        void showAllVipSetMeals(List<VipSetMeal> vipSetMeals);

        void showNoVip();

        void dealPayResult(String orderInfo);
    }

}
