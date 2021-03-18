package com.mandi.intelimeditor.user.userVip;


import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mandi.intelimeditor.bean.OrderPayBean;
import com.mandi.intelimeditor.bean.ResponseResultData;
import com.mandi.intelimeditor.bean.VipSetMeal;
import com.mandi.intelimeditor.common.util.ToastUtils;
import com.mandi.intelimeditor.user.US;
import com.mathandintell.intelimeditor.BuildConfig;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * 支付界面业务逻辑
 * 1、获取商品列表
 * 2、获取支付服务器订单加密json字符串
 */
public class PayVipPresenterImp implements PayVipContract.Presenter {
    private String TAG = "PayVipPresenterImp";
    private PayVipContract.View mView;
    private Context mContext;

    public PayVipPresenterImp(Context context, PayVipContract.View view) {
        this.mView = view;
        this.mContext = context;
    }

    /**
     * 准备支付
     */
    public void preparePay(String url, double amount, int payType, String tradeCode, String topicId) {
        //第一步创建OKHttpClient
        OkHttpClient client = new OkHttpClient.Builder()
                .build();
        //第二步创建RequestBody（Form表达）
        RequestBody body = new FormBody.Builder()
                .add("amount", String.valueOf(amount))
                .add("type", String.valueOf(payType))
                .add("tradeCode", tradeCode)
                .add("topicId", topicId)
                .add("subject", "VIP套餐")
                .add("body", "暴走P图订单")
                .add("sanbox", String.valueOf(BuildConfig.SANBOX))
                .build();
        //第三步创建Rquest
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        //第四步创建call回调对象
        final Call call = client.newCall(request);
        //第五步发起请求
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("onFailure", e.getMessage());
                if (mContext != null) {
                    US.putOpenVipEvent(US.OPEN_VIP_FAILED_IN_PAY + e.getMessage());
                    ((Activity) mContext).runOnUiThread(() -> ToastUtils.show(e.getMessage()));
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String result = response.body().string();
                Log.i("result", result);
                Type object = new TypeToken<ResponseResultData<OrderPayBean>>() {
                }.getType();
                ResponseResultData<OrderPayBean> data = new Gson().fromJson(result, object);
                if (data.getCode() == 200) {
                    Log.i("result", data.getMsg());
                    String sign = data.getData().getSign();
                    if (sign != null) {
                        mView.dealPayResult(sign);
                    }
                } else {
                    if (mContext != null) {
                        Log.e(TAG, "onResponse: " + data.getMsg());
                        US.putOpenVipEvent(US.OPEN_VIP_FAILED_IN_PAY + data.getMsg());
                        ((Activity) mContext).runOnUiThread(() -> ToastUtils.show(data.getMsg()));
                    }
                }
            }
        });
    }

    /**
     * 加载在线所有的VIP套餐列表
     */
    public void getAllVipSetMeals() {
        Log.d(TAG, "getAllVipSetMeals");
        try {
            BmobQuery<VipSetMeal> query = new BmobQuery<>();
            query.findObjects(new FindListener<VipSetMeal>() {
                @Override
                public void done(List<VipSetMeal> list, BmobException e) {
                    if (list != null && list.size() != 0) {
                        Log.d(TAG, "getAllVipSetMeals " + list.size());
                        mView.showAllVipSetMeals(list);
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void start() {

    }
}
