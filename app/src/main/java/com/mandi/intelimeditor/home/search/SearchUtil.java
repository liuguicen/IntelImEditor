package com.mandi.intelimeditor.home.search;

import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.Nullable;

import com.mandi.intelimeditor.common.dataAndLogic.AllData;

import com.mandi.intelimeditor.common.util.LogUtil;
import com.mandi.intelimeditor.common.util.Util;
import com.mandi.intelimeditor.ptu.tietu.onlineTietu.PicResource;
import com.mandi.intelimeditor.ptu.tietu.onlineTietu.PicResourceDownloader;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SearchUtil {
    public static final String TAG = "SearchUtil";


    /**
     * 推荐功能用，根据用户使用的tag，搜索类似的图片
     * 现在的实现是：统一资源会匹配不同TAG，匹配次数越多，越靠前，匹配次数相同，按热度排序
     * 这是一个简单的实现，更专业的，就用到算法了，有待处理
     *
     * @param resList         资源列表，已经按照热度排好序了
     * @param noSplitTagsList tag列表，tag还没有拆分,前面的tag重要性更高
     * @param excludeTag      展示这样写，排除用户用过的哪一张图
     */
    public static List<PicResource> searchSimilarResByTags(List<PicResource> resList, List<String> noSplitTagsList, @Nullable String excludeTag) {
        if (resList == null || noSplitTagsList == null) return null;
        HashMap<PicResource, Integer> resultMap = new HashMap<>(); // map统计并加速
        ArrayList<String> usedTags = splitTags_and_addSimilar(noSplitTagsList);
        for (int i = 0; i < 20 && i < usedTags.size(); i++) { // 只搜索靠前x切不超过size的tag
            String usedTag = usedTags.get(i);
            for (PicResource picResource : resList) {
                if (picResource.getTag().contains(usedTag)) {
                    if (TextUtils.equals(excludeTag, picResource.getTag())) continue; // 用过的那张图，排除
                    Integer number = resultMap.get(picResource);
                    int matchNumber = number == null ? 1 : number + 1;
                    resultMap.put(picResource, matchNumber);
                    if (LogUtil.debugRecommend) {
                        Log.d(TAG, "添加图片资源 " + picResource.getTag());
                    }
                }
            }
        }
        return sortResult(resultMap);
    }

    /**
     * 按照匹配度，热度排序
     */
    public static List<PicResource> sortResult(HashMap<PicResource, Integer> resMap) {
        //////借助list实现hashMap排序//////
        //注意 ArrayList<>() 括号里要传入map.entrySet()
        List<Map.Entry<PicResource, Integer>> list = new ArrayList<>(resMap.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<PicResource, Integer>>() {
            @Override
            public int compare(Map.Entry<PicResource, Integer> o1, Map.Entry<PicResource, Integer> o2) {
                int cmp = o2.getValue() - o1.getValue();
                double random = AllData.sRandom.nextDouble();
                if (cmp == 0) {
                    if (random < 0.8) {
                        cmp = o2.getKey().getHeat() - o1.getKey().getHeat();
                    } else {
                        cmp = AllData.sRandom.nextInt(2) - 1; // 概率性的随机顺序
                    }
                }
                //按照value值，从大到小排序
//                return o2.getValue() - o1.getValue();
                return cmp;
            }
        });
        List<PicResource> sortedList = new ArrayList<>(list.size());
        //注意这里遍历的是list，也就是我们将map.Entry放进了list，排序后的集合
        LogUtil.d(TAG, "sortResult: 排序结果", LogUtil.debugRecommend);
        for (Map.Entry<PicResource, Integer> entry : list) {
            if (LogUtil.debugRecommend) {
                Log.d(TAG, entry.getKey().getTag() + " 匹配次数 " + entry.getValue() + "  热度  " + entry.getKey().getHeat());
            }
            sortedList.add(entry.getKey());
        }
        return sortedList;
    }

    /**
     * 拆分tag串并加入每个tag的近义词tag
     * 传入的tag必须以-或者 分开分开
     */
    private static ArrayList<String> splitTags_and_addSimilar(List<String> noSplitList) {
        Set<String> usedTags = new LinkedHashSet<>(); // 用set加速, 并且保持原有顺序
        for (String s : noSplitList) {
            String[] split = s.split("[- ]");
            for (String tag : split) {
                usedTags.add(tag);
                List<String> c = similarTagMap.get(tag);
                if (c != null)
                    usedTags.addAll(c);
            }
        }
        return new ArrayList<>(usedTags);
    }

    static Map<String, List<String>> similarTagMap = new HashMap<>();

    static {
        similarTagMap.put("王者", Collections.singletonList("王者荣耀"));
        similarTagMap.put("爹", Collections.singletonList("爸爸"));
        similarTagMap.put("王者荣耀", Collections.singletonList("王者"));
        similarTagMap.put("吃鸡", Arrays.asList("刺激战场", "绝地求生", "刺激"));
        similarTagMap.put("绝地求生", Arrays.asList("刺激战场", "吃鸡"));
        similarTagMap.put("火影", Collections.singletonList("火影忍者"));
    }


    /**
     * 根据Tag排序，可以有多个TAG， 排在前面的Tag匹配到的数据顺序靠前
     */
    @Nullable
    public static List<PicResource> sortByGivenTag(List<PicResource> resList, List<String> tagList) {
        if (resList == null || tagList == null) return null;
        List<PicResource> sortedList = new ArrayList<>(resList);
        Collections.reverse(sortedList); // 热度倒序，因为后期添加的是后匹配的放到前面，所以先反向
        for (int i = tagList.size() - 1; i >= 0; i--) { // 倒着添加匹配项到底0为，tag在前的匹配项也就添加到了前面
            String tag = tagList.get(i);
            for (int resID = 0; resID < sortedList.size(); resID++) { //
                if (sortedList.get(resID).getTag().contains(tag)) {
                    sortedList.add(0, sortedList.remove(resID));
                }
            }
        }
        return sortedList;
    }

    /**
     * 返回的列表是新创建，可以直接使用和更改
     *
     * @param searchContent 搜索内容
     * @param firstClass    指定类别和secondClass
     */
    @Nullable
    public static void searchResByTags(String searchContent, String firstClass, String secondClass, SearchResultListener resultListener) {
        searchResByTags(Collections.singletonList(searchContent), firstClass, secondClass, resultListener);
    }


    public static void searchResByTags(List<String> tagList, String firstClass, String secondClass, SearchResultListener resultListener) {
        tagList = splitTags_and_addSimilar(tagList);
        Log.d(TAG, "searchResByTags: 获取到的搜索tagList为 " + Util.list2String(tagList));
        List<PicResource> resultList = new ArrayList<>();
        List<PicResource> picResList = PicResourceDownloader.queryFormGlobalData(secondClass);
        if (picResList == null) {
            return;
        }
        for (PicResource picResource : picResList) {
            for (String tag : tagList) {
                if (picResource.getTag().contains(tag)) {
                    resultList.add(picResource);
                    break;
                }
            }
        }
        resultListener.onResult(resultList);
    }

    /**
     * 在线搜索结果，搜索本地缓存的PicResource。
     *
     * @param query
     * @return
     */
    @NotNull
    public static List<PicResource> searchPicByTag(String query) {
        List<PicResource> searchResult = new ArrayList<>();
        List<PicResource> data = new ArrayList<>();
        if (AllData.expressResList != null)
            data.addAll(AllData.expressResList);
        else { // 如果为空，下载到缓存，下次可以直接用
            PicResourceDownloader.onlyDownLoadResToCache(PicResource.FIRST_CLASS_TIETU, PicResource.SECOND_CLASS_EXPRESSION);
            return searchResult;
        }
        if (AllData.propertyResList != null)
            data.addAll(AllData.propertyResList);
        else {
            PicResourceDownloader.onlyDownLoadResToCache(PicResource.FIRST_CLASS_TIETU, PicResource.SECOND_CLASS_EXPRESSION);
            return searchResult;
        }
        if (AllData.templateResList != null)
            data.addAll(AllData.templateResList);
        else {
            PicResourceDownloader.onlyDownLoadResToCache(PicResource.FIRST_CLASS_TEMPLATE, PicResource.SECOND_CLASS_BASE);
            return searchResult;
        }
        for (int i = 0; i < data.size(); i++) {
            //一张图对于多个tag（转义之后更多）
            List<String> tagList = splitTags_and_addSimilar(Arrays.asList(data.get(i).getTag()));
            for (int j = 0; j < tagList.size(); j++) {
                String tag = tagList.get(j);
                //搜索，标签包含了搜素结果，或者搜索结果包含了标签
                if (tag.toUpperCase().contains(query.toUpperCase()) || query.toUpperCase().contains(tag.toUpperCase())) {
                    searchResult.add(data.get(i));
                    //标签搜索到即跳转下一个
                    break;
                }
            }
        }
        return searchResult;
    }

    interface SearchResultListener {
        /**
         * 返回的列表是新创建，可以直接使用和更改
         */
        void onResult(List<PicResource> resourceList);
    }
}
