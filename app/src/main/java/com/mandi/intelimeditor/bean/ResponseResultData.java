package com.mandi.intelimeditor.bean;

import com.google.gson.annotations.SerializedName;

public class ResponseResultData<T> {
    //返回码
    @SerializedName("code")
    private int code;
    //消息
    @SerializedName("msg")
    private String msg;
    //数据
    @SerializedName("data")
    private T data;

    public ResponseResultData() {
    }

    public ResponseResultData(int code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public int getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
