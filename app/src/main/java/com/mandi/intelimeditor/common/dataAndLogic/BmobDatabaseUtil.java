package com.mandi.intelimeditor.common.dataAndLogic;



import com.mandi.intelimeditor.common.util.LogUtil;

import org.jetbrains.annotations.Nullable;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

import cn.bmob.v3.BmobObject;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.datatype.BmobQueryResult;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.SQLQueryListener;
import io.reactivex.ObservableEmitter;


/**
 * <pre>
 *      author : liuguicen
 *      time : 2019/02/14
 *      version : 1.0
 *      bmob数据库工具
 * <pre>
 */
public class BmobDatabaseUtil {
    /**
     * 获取表格最新更新的时间,一个作用是获取，还有一个作用是用subscribe传递获取到的时间
     * 应用启动一次生命周期内只获取一次，减少网络访问耗时
     */
    public static void getServiceUpdateTime(String tableName, @Nullable final ObservableEmitter<Long> emitter) {
        if (AllData.latestTietuModifyTime != AllData.GET_SERVICE_UPDATE_TIME_FAILED) {  // 已经获取成功了，传递时间后直接返回
            if (emitter != null) {
                emitter.onNext(AllData.latestTietuModifyTime);
                emitter.onComplete();
            }
            return;
        }
        BmobQuery<BmobObject> query = new BmobQuery<>();
        String sql = "select top 1 updatedAt from " + tableName + " order by updatedAt desc";
        query.setSQL(sql);
        query.doSQLQuery(new SQLQueryListener<BmobObject>() {
            @Override
            public void done(BmobQueryResult<BmobObject> result, BmobException e) {
                long time = 0;
                if (e != null || result == null) {
                    time = AllData.GET_SERVICE_UPDATE_TIME_FAILED;
                } else {
                    List<BmobObject> resultList = result.getResults();
                    if (resultList == null || resultList.isEmpty()) {
                        time = AllData.GET_SERVICE_UPDATE_TIME_FAILED;
                    } else {
                        time = convertTime(resultList.get(0).getUpdatedAt());
                    }
                }

                if (emitter != null) {
                    if (time  == AllData.GET_SERVICE_UPDATE_TIME_FAILED) {
                        // 获取服务器更新时间失败，当成发生更新，订阅器发送-1,相当于没有更新，用缓存
                        emitter.onNext(AllData.GET_SERVICE_UPDATE_TIME_FAILED);
                        LogUtil.e("failed to get the service update time");
                    } else {
                        emitter.onNext(time);
                    }
                    emitter.onComplete();
                }
                AllData.latestTietuModifyTime = time;
            }
        });

    }

    private static  long convertTime(String updatedAt) {
        // bmob日期格式 2019-02-02 23:07:44
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        long millionSeconds = 0;
        try {
            millionSeconds = sdf.parse(updatedAt).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return millionSeconds;
    }
}
