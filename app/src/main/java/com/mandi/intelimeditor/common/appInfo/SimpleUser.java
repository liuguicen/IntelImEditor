package com.mandi.intelimeditor.common.appInfo;

import android.util.Log;

import com.mandi.intelimeditor.common.dataAndLogic.AllData;

import java.util.List;

import cn.bmob.v3.BmobObject;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UpdateListener;

/**
 * Created by LiuGuicen on 2016/12/25 0025.
 */

public class SimpleUser extends BmobObject {
    private String userIdentify;
    private String appVersion;

    /**
     * 使用过的天数
     */
    private Integer usedDays;

    public SimpleUser() {
        appVersion = String.valueOf(AppConfig.CUR_DATABASE_VERSION);
        userIdentify = UserExclusiveIdentify.getExclusiveIndentify();
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public String getUserIdentify() {
        return userIdentify;
    }

    public void setUserIdentify(String userIdentify) {
        this.userIdentify = userIdentify;
    }

    public Integer getUsedDays() {
        return usedDays;
    }

    public void setUsedDays(Integer usedDays) {
        this.usedDays = usedDays;
    }

    /**
     * 在每次判断是否需要更新时查看是否已添加到服务器 ，若没有添加到服务器，则这个时候调用此函数进行添加
     */
    private void create() {

        save(new SaveListener<String>() {
            @Override
            public void done(String objectId, BmobException e) {
                if (e == null) {
                    Log.e("服务器保存成功：", objectId);
                    //服务器添加成功才写入用户信息
                    AllData.appConfig.writeConfig_LastUsedData(System.currentTimeMillis());
                } else {
                    Log.e("bmob", "服务器保存失败：" + e.getMessage() + "," + e.getErrorCode());
                }
            }
        });
    }

    public void myUpdate() {
        if (!isUpdateNeeded()) return;//不需要更新，返回

        BmobQuery<SimpleUser> query = new BmobQuery<>();
        query.addWhereEqualTo("userIdentify", userIdentify);
        //先查询，查到了更新
        query.findObjects(
                new FindListener<SimpleUser>() {
                    @Override
                    public void done(List<SimpleUser> list, BmobException e) {
                        if (e == null) {
                            if (list.size() == 0) return;
                            SimpleUser simpleUser = list.get(0);

                            String objectId = simpleUser.getObjectId();
                            //更新使用天数
                            if (simpleUser.getUsedDays() != null) {
                                simpleUser.setUsedDays(simpleUser.getUsedDays() + 1);
                            } else {
                                simpleUser.setUsedDays(1);
                            }

                            simpleUser.update(objectId,
                                    new UpdateListener() {

                                        @Override
                                        public void done(BmobException ee) {
                                            if (ee == null) {
                                                AllData.appConfig.writeConfig_LastUsedData(System.currentTimeMillis());
                                                Log.e("bmob", "更新成功");
                                            } else {
                                                Log.e("bmob", "更新失败：" + ee.getMessage() + "," + ee.getErrorCode());
                                            }
                                        }
                                    });
                        } else {
                            Log.e("bmob", "查询失败：" + e.getMessage() + "," + e.getErrorCode());
                        }
                    }
                });

    }

    /**
     * 查看是否需要更新，并且会查看是否添加到服务器，没有添加时则由这里添加
     */
    private boolean isUpdateNeeded() {
        if (AllData.appConfig == null) {
            AllData.appConfig = new AppConfig(IntelImEditApplication.appContext);
        }
        long data = System.currentTimeMillis() / 24 / 3600 / 1000;//日为单位
        long lastData = AllData.appConfig.readConfig_LastUseData() / 24 / 3600 / 1000;//日为单位
        if (lastData == 0)//网络端还为创建或者没有创建成功,则重新创建
        {
            create();
            return false;
        } else if (data > lastData)//大于上次使用日期
        {
            return true;
        }
        return false;
    }
}
