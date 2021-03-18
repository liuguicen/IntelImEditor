package com.mandi.intelimeditor.bean;

import cn.bmob.v3.BmobObject;

/**
 * VIP套餐
 */
public class VipSetMeal extends BmobObject {
    //套餐名
    public String name;
    //打折后的价格
    public double disCountPrice;
    //原始价格
    public double originalPrice;
    //会员时间（天）
    public int time;

    public VipSetMeal(String name, double disCountPrice, double originalPrice, int time) {
        this.name = name;
        this.disCountPrice = disCountPrice;
        this.originalPrice = originalPrice;
        this.time = time;
    }

    public void setOriginalPrice(double originalPrice) {
        this.originalPrice = originalPrice;
    }

    public double getOriginalPrice() {
        return originalPrice;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDisCountPrice(double disCountPrice) {
        this.disCountPrice = disCountPrice;
    }

    public String getName() {
        return name;
    }

    public double getDisCountPrice() {
        return disCountPrice;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }
}
