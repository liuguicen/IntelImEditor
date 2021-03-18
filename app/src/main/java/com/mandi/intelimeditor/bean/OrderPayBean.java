package com.mandi.intelimeditor.bean;

import com.google.gson.annotations.SerializedName;

public class OrderPayBean {
    @SerializedName("appId")
    private String appId;
    @SerializedName("sign")
    private String sign;
    @SerializedName("tradeCode")
    private String tradeCode;
    @SerializedName("timestamp")
    private Long timestamp;

    public OrderPayBean(String appId, String sign, String tradeCode, Long timestamp) {
        this.appId = appId;
        this.sign = sign;
        this.tradeCode = tradeCode;
        this.timestamp = timestamp;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public String getTradeCode() {
        return tradeCode;
    }

    public void setTradeCode(String tradeCode) {
        this.tradeCode = tradeCode;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
}
