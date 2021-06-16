package com.mandi.intelimeditor.ad;

import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;


import com.mandi.intelimeditor.common.dataAndLogic.SPUtil;
import com.mandi.intelimeditor.common.util.LogUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * 广告位的广告策略 <p></p>
 * <p>
 * 同一个广告位可能有多个广告源<p>
 * 用一个字符串来表示广告位显示广告的策略<p>
 * 字符第一段表示策略模式，第二段表示广告源
 * 格式 模式:广告源-广告源-广告源
 *
 * <P></P>
 * 广告源代号：
 * 穿山甲TT，优量汇TX，珊瑚SH，虹采 HY， 注意格式不能错，否则很麻烦，版本兼容，也不能随便改
 *
 * <p></p>>
 * 策略1-平均模式:
 * 格式: average-TT-TX-HY
 * 如果有3个广告，每个都是1/2的概率，如果某个广告展示失败，删掉它所在的位，形成新的组合，再次调用展示方法 <p>
 *
 * <p></p>
 * 策略2-优先模式：
 * 格式： priorx-广告源-广告源-... x表示每个广告展示次数
 * 每一天此广告位展示A广告源广告x次之后,展示下一个广告源广告x次<p>，所有广告都展示过一遍之后，循环
 *
 * <p> </p>
 * 格式： only-广告源-广告源-...
 * 策略3-唯一模式，某一家的广告收益最高，只使用这一家
 * 后面的做备用，出错时显示
 * <p>
 * 注意解析服务器字符串有很多不确定性，一定要把代码try catch保护起来，防止崩溃
 * <P>
 * 出错了，升级版本，现在升级2.7.0要增加一个广告源，但是原来的策略不支持！！！
 * 如果选中了新增的广告源，那么对已有广告源进行判断结果都是不行，最后也就选择到默认的广告源，
 * 这会导致广告收入降低很多
 * 竟然没考虑到！！！
 * <p>
 * 现在的方式，每个版本有哪些广告源会列出来，初始化的时候候选列表里面，就过滤掉不支持的广告源，
 * 这样就不会选中不辞职的广告源
 * </P>
 *
 * <p></p>
 * 经验，一开始写这个地方的功能用的int的位来表示组合，但后面写了一截才发现直接一个01字符串弄起来更方便
 * 我们从c语言学过来会觉得位操作更加厉害，性能好，但对于上层的应用一般情况都不要用这种十分底层的方式来写代码
 * 1、一般的十分底层的方式，编码比起上层来说麻烦很多，因为为了性能没有给足够多的api以及易用性
 * 2、十分底层的方式编码，灵活性一般也不够，因为底层模式比较固定+api较少，影响了编码的灵活性，丰富性
 * 应该吧时间用在更需要的地方
 * <p>
 * 另一个经验，编码中，我们或我自己常常使用数字来做常量，代号之类来写代码，实际上用字符串更好用，
 * 典型的数据库主要类型是字符串，可见字符串具有更好的易用性
 */
public class AdStrategyUtil {
    public static final String TAG = "AdStrategyUtil";
    public static final String DEFAULT_SPLASH_AD_STRATEGY = "only:TX-TT-ZY";
    public static final String DEFAULT_REWARD_VAD_STRATEGY = "prior3:TX-TT-ZY";
    public static final String DEFAULT_PIC_RES_LIST_STRATEGY = "average:TT-TX";
    public static final String DEFAULT_PTU_RESULT_AD_STRATEGY = "average:TT-TX";
    private static final String PATTERN_AVERAGE = "average";
    private static final String PATTERN_ONLY = "only";
    public static final String PATTERN_PRIOR = "prior";
    public static final List<String> CUR_VERSION_SUPPORT_AD_RES = Arrays.asList(AdData.TX_AD_NAME, AdData.TT_AD_NAME, AdData.KJ_AD_NAME);

    private int numberOfPrior = 3; // 优先模式一个广告源曝光的次数
    private List<String> resList = new ArrayList<>();
    private String pattern = PATTERN_AVERAGE;
    private String adSpaceName;


    private String chosenRes = "";

    public AdStrategyUtil(String adSpaceName, String strategy) {
        init(adSpaceName, strategy);
    }

    private void init(String adSpaceName, String strategy) {
        if (strategy == null) return;
        try { // 设计到解析服务器字符串，很可能崩溃，保护起来
            if (LogUtil.debugAdDStrategy) {
                Log.d(TAG, "广告位" + adSpaceName + " 解析策略 " + strategy);
            }
            int divideId = strategy.indexOf(':');
            pattern = strategy.substring(0, divideId);
            strategy = strategy.substring(divideId + 1);
            String[] split = strategy.split("-");
            this.adSpaceName = adSpaceName;
            for (int i = 0; i < split.length; i++) {
                String res = split[i].trim();
                if (CUR_VERSION_SUPPORT_AD_RES.contains(res)) {
                    resList.add(res);
                }
            }
            if (PATTERN_AVERAGE.equals(pattern)) {
                generateChosenRes();
            } else if (pattern.startsWith(PATTERN_PRIOR)) { // 优先模式，后面带了广告展示次数，解析出来
                numberOfPrior = Integer.parseInt(pattern.substring(PATTERN_PRIOR.length()));
                if (numberOfPrior <= 0) numberOfPrior = 3;
                pattern = PATTERN_PRIOR;
            }
        } catch (Exception e) {
            Log.e(TAG, "init: " + e.getMessage());
        }
    }

    private void generateChosenRes() {
        if (resList.size() == 0) {
            chosenRes = "";
            return;
        }
        int randomId = new Random().nextInt(resList.size());
        chosenRes = resList.get(randomId);
        LogUtil.d(TAG, "平均模式 选中广告源 " + chosenRes, LogUtil.debugRewardAd);
    }

    /**
     * 实现原理是，根据不同的广告策略从给予的广告源中选择一个广告源，
     * 然后判断传入的广告源于选中的是否相等
     */
    public boolean isShow(@NonNull String adRes) {
        if (LogUtil.debugAdDStrategy) {
            Log.d(TAG, "\n");
            Log.d(TAG, "广告位： " + adSpaceName);
            LogUtil.d(TAG, "判断是否显示广告源 " + adRes);
        }
        if (PATTERN_AVERAGE.equals(pattern)) {
            if (LogUtil.debugAdDStrategy) {
                Log.d(TAG, "平均模式 判断是否显示广告源 " + adRes + " = " + TextUtils.equals(adRes, chosenRes));
            }
            return TextUtils.equals(adRes, chosenRes);
        } else if (PATTERN_ONLY.equals(pattern)) {
            if (LogUtil.debugAdDStrategy) {
                Log.d(TAG, "唯一模式 判断是否显示广告源 " + adRes + " = " + (resList.indexOf(adRes) == 0));
            }
            return resList.indexOf(adRes) == 0; // 唯一模式就显示第一个广告，如果出错，移除它，后面判断还是第一个
        } else {  // 默认采用优先模式
            if (resList.size() == 0) return false;
            int numberHasExpose = SPUtil.getAdSpaceExposeNumber(adSpaceName);
            if (LogUtil.debugAdDStrategy) {
                Log.d(TAG, "优先模式 判断是否显示广告源: " + adRes + " 优先次数： " + numberOfPrior + " 今日已曝光次数： " + numberHasExpose);
            }
            int numberInOneTurn = resList.size() * numberOfPrior; // 一轮最多支持的次数
            numberHasExpose %= numberInOneTurn; // 比如5个资源，一轮最多播放3*5 = 15次，现在第25次，要把播放次数缩小到一轮以内10次，即如果总次数超过一轮，重新循环
            int chosenResId = numberHasExpose / numberOfPrior;
            String chosenRes = resList.get(chosenResId);
            if (LogUtil.debugAdDStrategy) {
                Log.d(TAG, "优先模式判断是否显示广告源: \" + adRes + \" 一轮允许次数: " + numberInOneTurn + " 选中资源为：  " + chosenRes);
                Log.d(TAG, "是否选中资源 " + adRes + " = " + adRes.equals(chosenRes));
            }
            return adRes.equals(chosenRes);
        }
    }

    /**
     * 某一家的广告失败了，然后去掉
     *
     * @return 返回是否还有可用资源，没有的话本次就不再显示广告了
     */
    public boolean remove(String adRes) {
        resList.remove(adRes);
        if (TextUtils.equals(adRes, chosenRes)) {
            chosenRes = "";
        }
        if (LogUtil.debugAdDStrategy)
            LogUtil.d(TAG, "去掉广告源" + adRes, LogUtil.debugRewardAd);
        if (resList.size() == 0)
            return false;
        if (PATTERN_AVERAGE.equals(pattern)) {
            generateChosenRes();
        }
        return true;
    }

    public String getChosenRes() {
        return chosenRes;
    }
}
