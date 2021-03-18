package com.mandi.intelimeditor.user.useruse.tutorial;

import android.text.TextUtils;


import com.mandi.intelimeditor.common.util.LogUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import cn.bmob.v3.BmobObject;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.datatype.BmobQueryResult;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.SQLQueryListener;

public class Tutorial extends BmobObject {
    public static final String TAG = "Tutorial";
    private String title;//功能名
    private String content;//引导描述
    private String keyword; // 引导类型
    private int level; // 引导级别
    private BmobFile gifFile;//动图引导图在线地址
    private boolean isShowMore;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public BmobFile getGifFile() {
        return gifFile;
    }

    public void setGifFile(BmobFile gifFile) {
        this.gifFile = gifFile;
    }

    public boolean isShowMore() {
        return isShowMore;
    }

    public void setShowMore(boolean showMore) {
        isShowMore = showMore;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public Tutorial(String title, String content, BmobFile gifFile, boolean isShowMore) {
        this.title = title;
        this.content = content;
        this.gifFile = gifFile;
        this.isShowMore = isShowMore;
    }

    public Tutorial(String title, String keyword, String content) {
        this.title = title;
        this.content = content;
        this.keyword = keyword;
        this.isShowMore = false;
    }

    public static void loadGuideUseFromServer(boolean isOnlyFromNet) {
//        LogUtil.d("GuideUseBean", "loadGuideUseFromServer");
        //判断是否有缓存，该方法必须放在查询条件（如果有的话）都设置完之后再来调用才有效，就像这里一样。
        String sql = "select * " +
                " from Tutorial " +
                " order by level desc" +
                " limit " + 500;
        LogUtil.d("loadGuideUseFromServer", "sql =" + sql);
        BmobQuery<Tutorial> query = new BmobQuery<>();
        query.setSQL(sql);
        // todo 有个问题，升级之后有新教程，用户不能立即获取到，无法立即明白新教程
        query.setMaxCacheAge(TimeUnit.DAYS.toMillis(5));//此表示缓存
        boolean hasCache = query.hasCachedResult(Tutorial.class);
        if (hasCache && !isOnlyFromNet) {
            query.setCachePolicy(BmobQuery.CachePolicy.CACHE_ELSE_NETWORK);   // 如果有缓存的话，则设置策略为CACHE_ELSE_NETWORK
            LogUtil.d("loadGuideUseFromServer", "use cache");
        } else {
            query.setCachePolicy(BmobQuery.CachePolicy.NETWORK_ELSE_CACHE);   // 如果没有缓存的话，则设置策略为NETWORK_ELSE_CACHE
            LogUtil.d("loadGuideUseFromServer", "from net");
        }
        query.doSQLQuery(
                new SQLQueryListener<Tutorial>() {
                    @Override
                    public void done(BmobQueryResult<Tutorial> bmobQueryResult, BmobException e) {
                        if (e == null && bmobQueryResult != null) { // 查询成功
                            List<Tutorial> list = bmobQueryResult.getResults();
                            List<Tutorial> result = new ArrayList<>();
                            //过滤文本
                            if (list != null) {
                                for (int i = 0; i < list.size(); i++) {
                                    if (!TextUtils.isEmpty(list.get(i).title) && !TextUtils.isEmpty(list.get(i).keyword)) {
                                        result.add(list.get(i));
                                    }
                                }
                                LogUtil.d("GuideUseBean", "list = " + list.size());
                                GuideData.allGuideUseData = result;
                            }

                        }
                    }
                });
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }
}
