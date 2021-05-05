package com.mandi.intelimeditor.home.search;

import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mandi.intelimeditor.common.dataAndLogic.AllData;
import com.mandi.intelimeditor.common.util.LogUtil;
import com.mandi.intelimeditor.common.util.Util;
import com.mandi.intelimeditor.home.tietuChoose.PicResourceItemData;
import com.mandi.intelimeditor.ptu.tietu.onlineTietu.PicResGroup;
import com.mandi.intelimeditor.ptu.tietu.onlineTietu.PicResource;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.reactivex.Emitter;
import io.reactivex.Emitter;

import static com.mandi.intelimeditor.ptu.tietu.onlineTietu.PicResGroup.MIN_GROUP_SIZE;


/**
 * 专门用于PicResource的搜索，排序，分组，过滤的类
 */
public class PicResSearchSortUtil {
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

    public static void searchResByTagList(List<String> tagList, List<PicResource> resList, SearchResultListener resultListener) {
        tagList = splitTags_and_addSimilar(tagList);
        Log.d(TAG, "searchResByTags: 获取到的搜索tagList为 " + Util.list2String(tagList));
        List<PicResource> resultList = new ArrayList<>();
        if (resList == null) {
            return;
        }
        for (PicResource picResource : resList) {
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
     */
    @NotNull
    public static void searchPicResByQueryString(String query, @Nullable String firstClass, Emitter<List<PicResource>> emitter) {
        AllData.queryAllPicRes(new Emitter<List<PicResource>>() {
            @Override
            public void onNext(@NonNull List<PicResource> resList) {

                List<PicResource> searchResult = new ArrayList<>();
                if (query == null) emitter.onNext(searchResult);

                List<String> splitQuery = splitTags_and_addSimilar(Collections.singletonList(query.toUpperCase()));

                for (PicResource res : resList) {
                    if (firstClass != null && !firstClass.equals(res.getResourceClass())) continue;

                    String resTag = res.getTag().toUpperCase();
                    if (resTag == null) {
                        continue;
                    }
                    List<String> resTags = splitTagString(resTag);

                    if (judgeTagListMatch(splitQuery, resTags)) {
                        searchResult.add(res);
                    }
                }
                emitter.onNext(searchResult);
            }

            @Override
            public void onError(@NonNull Throwable error) {
                emitter.onError(error);
            }

            @Override
            public void onComplete() {

            }
        });
    }

    private static boolean judgeTagListMatch(List<String> tagListA, List<String> tagListB) {
        for (String taga : tagListA) {
            for (String tagb : tagListB) {
                //搜索，标签包含了搜素结果，或者搜索结果包含了标签
                if (taga.contains(tagb) || tagb.contains(taga)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static void getHotestTietuList(Emitter<List<PicResource>> emitter) {
        AllData.queryAllPicRes(new Emitter<List<PicResource>>() {
            @Override
            public void onNext(@NonNull List<PicResource> resList) {
                List<PicResource> filterList = filter(resList, PicResource.FIRST_CLASS_TIETU, null);
                sortPicRes(filterList, SORT_TYPE_HOT, true);
                emitter.onNext(filterList);
            }

            @Override
            public void onError(@NonNull Throwable error) {
                emitter.onError(error);
            }

            @Override
            public void onComplete() {

            }
        });
    }

    public static void getNewestTietuList(Emitter<List<PicResource>> emitter) {
        AllData.queryAllPicRes(new Emitter<List<PicResource>>() {
            @Override
            public void onNext(@NonNull List<PicResource> resList) {
                List<PicResource> filterList = filter(resList, PicResource.FIRST_CLASS_TIETU, null);
                sortPicRes(filterList, SORT_TYPE_TIME, true);
                emitter.onNext(filterList);
            }

            @Override
            public void onError(@NonNull Throwable error) {
                emitter.onError(error);
            }

            @Override
            public void onComplete() {

            }
        });
    }

    /**
     * 两级类别过滤，为空表示不过滤，
     * 具体说，
     * 一级为空的时候，使用另一级别过滤，
     * 都不为空，同时起作用,
     * 都为空，不过滤
     */
    public static List<PicResource> filter(List<PicResource> resList, String firstClass, String second_class) {
        ArrayList<PicResource> resultList = new ArrayList<>();
        for (PicResource pic : resList) {
            if ((firstClass == null || firstClass.equals(pic.getResourceClass())) &&
                    (second_class == null || second_class.equals(pic.getCategory()))) {
                resultList.add(pic);
            }
        }
        return resultList;
    }

    interface SearchResultListener {
        /**
         * 返回的列表是新创建，可以直接使用和更改
         */
        void onResult(List<PicResource> resourceList);
    }

    // --------------------------------------------------------------------------------------------
    // 排序分组
    // --------------------------------------------------------------------------------------------
    public static final int SORT_TYPE_DEFAULT = 0; // = hot + 随机
    public static final int SORT_TYPE_HOT = 1;
    public static final int SORT_TYPE_TIME = 2;
    public static final int SORT_TYPE_GROUP = 3;


    public static void sortPicRes(List<PicResource> resList, int sortType, boolean isReduce) {
        Comparator<PicResource> comparator = new Comparator<PicResource>() {
            @Override
            public int compare(PicResource o1, PicResource o2) {
                return originalCompare(o1, o2) * (isReduce ? -1 : 1); // api里面有反向方法，但是低版本sdk不能用
            }

            private int originalCompare(PicResource o1, PicResource o2) {
                if (o2 == null && o1 == null)
                    return 0;
                if (o1 == null)
                    return -1;
                if (o2 == null)
                    return 1;
                //普通列表排序

                if (sortType == SORT_TYPE_HOT) {
                    Integer heatA = o1.getHeat();
                    Integer heatB = o2.getHeat();
                    if (heatA != null && heatB != null) {
                        return heatA - heatB;
                    }
                    return 0;
                }

                if (sortType == SORT_TYPE_TIME) {
                    String time1 = o1.getCreatedAt(), time2 = o2.getCreatedAt();
                    if (time1 != null && time2 != null)  // 可以直接比较， 转换耗时多很多
                        return time1.compareTo(time2);   // 这个耗时是上面整数相减的20倍
                    else return 0;
                }

                return 0;
            }
        };

        Collections.sort(resList, comparator);
    }

    /**
     * 备用方式，实际上和上面的插播多
     */
    public static void sortItemPicRes(List<PicResourceItemData> itemList, int sortType,
                                      boolean isReduce) {
        Comparator<PicResourceItemData> comparator = new Comparator<PicResourceItemData>() {
            @Override
            public int compare(PicResourceItemData o1, PicResourceItemData o2) {
                return originalCompare(o1, o2) * (isReduce ? -1 : 1); // api里面有反向方法，但是低版本sdk不能用
            }

            private int originalCompare(PicResourceItemData o1, PicResourceItemData o2) {
                if (o2 == null && o1 == null)
                    return 0;
                if (o1 == null)
                    return -1;
                if (o2 == null)
                    return 1;
                //普通列表排序

                if (sortType == SORT_TYPE_HOT) {
                    Integer heatA = o1.getHeat();
                    Integer heatB = o2.getHeat();
                    if (heatA != null && heatB != null) {
                        return heatA - heatB;
                    }
                    return 0;
                }

                if (sortType == SORT_TYPE_TIME) {
                    String time1 = o1.getCreatedAt(), time2 = o2.getCreatedAt();
                    if (time1 != null && time2 != null)  // 可以直接比较， 转换耗时多很多
                        return time1.compareTo(time2);   // 这个耗时是上面整数相减的20倍
                    else return 0;
                }

                return 0;
            }
        };

        Collections.sort(itemList, comparator);
    }

    /**
     * 根据标签将图片列表分组
     */
    public static List<PicResGroup> groupByTag(String firstClass, String secondClass, List<PicResource> resList) {
        long start = System.currentTimeMillis();
        if (resList == null) return new ArrayList<>();
        resList = filter(resList, firstClass, secondClass);
        // 先用map将数据按照tag分组
        Map<String, PicResGroup> map = new HashMap<>();
        for (int i = 0; i < resList.size(); i++) {
            PicResource picRes = resList.get(i);

            if (picRes.getTag() == null) {
                LogUtil.d(TAG, "updateAllTagAndGroup 数据异常" + picRes.toString());
                continue;
            }

            try {
                List<String> split_tags = splitTagString(picRes.getTag());
                for (String tag : split_tags) {
                    tag = mergeSimilarTag(tag); // 合同近似tag，比如王者和王者荣耀

                    PicResGroup picResGroup = map.get(tag);
                    if (picResGroup == null) {
                        picResGroup = new PicResGroup(tag);
                    }
                    picResGroup.addPicRes(picRes);
                    map.put(tag, picResGroup);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        Collection<PicResGroup> values = map.values();
        ArrayList<PicResGroup> picResGroups = new ArrayList<>(values.size() / 2);
        for (PicResGroup group : values) {
            if (group.resList.size() > MIN_GROUP_SIZE) {
                picResGroups.add(group);
            }
        }
        Collections.sort(picResGroups, new Comparator<PicResGroup>() {
            @Override
            public int compare(PicResGroup o1, PicResGroup o2) {
                return o2.heat - o1.heat;
            }
        });

        Log.d(TAG, "updateAllTagAndGroup: 分类用时 = " + (System.currentTimeMillis() - start));
        return picResGroups;
    }

    /**
     * 拆分tag串并加入每个tag的近义词tag
     * 传入的tag必须以-或者 分开分开
     * 结果不会包含空串
     *
     * @return
     */
    private static List<String> splitTagString(String noSplitTag) {
        String[] split = noSplitTag.split("[- ]", 100);
        List<String> res = new ArrayList<>();
        for (String s : split) {
            if (!s.trim().isEmpty()) {
                res.add(s);
            }
        }
        return res;
    }


    static Map<String, List<String>> similarTagMap = new HashMap<>();

    static {
        // key 表示主要的名字
        similarTagMap.put("爹", Collections.singletonList("爸爸"));
        similarTagMap.put("王者荣耀", Collections.singletonList("王者"));
        similarTagMap.put("吃鸡", Arrays.asList("刺激战场", "绝地求生", "刺激"));
        similarTagMap.put("火影忍者", Collections.singletonList("火影"));
        similarTagMap.put("暴走脸", Collections.singletonList("暴走"));
    }

    private static String mergeSimilarTag(String tag) {
        for (Map.Entry<String, List<String>> keyValue : similarTagMap.entrySet()) {
            if (keyValue.getValue().contains(tag)) return keyValue.getKey();
        }
        return tag;
    }

    private static List<String> getSimilarTagList(String tag) {
        for (Map.Entry<String, List<String>> keyValue : similarTagMap.entrySet()) {
            if (keyValue.getValue().contains(tag) || keyValue.getKey().equals(tag)) {
                ArrayList<String> strings = new ArrayList<>();
                strings.add(keyValue.getKey());
                strings.addAll(keyValue.getValue());
                return strings;
            }
        }
        return Collections.singletonList(tag);
    }

    /**
     * 拆分tag串并加入每个tag的近义词tag
     * 传入的tag必须以-或者 分开分开
     */
    private static ArrayList<String> splitTags_and_addSimilar(List<String> noSplitList) {
        Set<String> usedTags = new LinkedHashSet<>(); // 用set加速, 并且保持原有顺序
        for (String s : noSplitList) {
            List<String> split = splitTagString(s);
            for (String tag : split) {
                usedTags.addAll(getSimilarTagList(tag));
            }
        }
        return new ArrayList<>(usedTags);
    }
}
