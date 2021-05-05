package com.mandi.intelimeditor.common.util;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import androidx.annotation.ColorRes;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import com.mandi.intelimeditor.common.appInfo.IntelImEditApplication;
import com.mandi.intelimeditor.ptu.tietu.onlineTietu.PicResource;
import com.mandi.intelimeditor.R;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static android.content.Context.ACTIVITY_SERVICE;

/**
 * Created 0 Administrator on 2016/5/19.
 */
public class Util {

    public static int dp2Px(Context context, float dp) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    public static int dp2Px(float dp) {
        final float scale = IntelImEditApplication.appContext.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    public static int px2Dp(Context context, float px) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (px / scale + 0.5f);
    }

    public static int px2Dp(float px) {
        final float scale = IntelImEditApplication.appContext.getResources().getDisplayMetrics().density;
        return (int) (px / scale + 0.5f);
    }

    public static Drawable getDrawable(int id) {
        return ContextCompat.getDrawable(IntelImEditApplication.appContext, id);
    }

    /**
     * @return 返回字符串的最后一个数字，没找到返回-1
     */
    public static int lastDigit(String s) {
        for (int i = s.length() - 1; i >= 0; i--) {
            if ('0' <= s.charAt(i) && s.charAt(i) <= '9') {
                return i;
            }
        }
        return -1;
    }

    /**
     * 点是否在View内部,注意参数值都是绝对坐标
     */
    public static boolean pointInView(float x, float y, View view) {
        if (view == null) return false;
        int[] xy = new int[2];
        view.getLocationOnScreen(xy);
        Rect bound = new Rect(xy[0], xy[1], xy[0] + view.getWidth(), xy[1] + view.getHeight());
        return bound.contains((int) (x + 0.5f), (int) (y + 0.5f));
    }

    /**
     * 隐藏输入法
     */
    public static void hideInputMethod(Context context, TextView tv) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(tv.getApplicationWindowToken(), 0);
    }

    public static final void hideInputMethod(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        View focusView = activity.getCurrentFocus();
        if (focusView != null) {
            imm.hideSoftInputFromWindow(focusView.getWindowToken(), 0); //强制隐藏键盘
        }
    }

    public static String list2String(List<String> list) {
        StringBuilder sb = new StringBuilder();
        for (Object o : list) {
            sb.append(o.toString());
        }
        return sb.toString();
    }

    /**
     * 空格和-分隔替换成正则式的形式
     */
    @Nullable
    public static String getRegexQuery(@NotNull String queryString) {
        String[] split = queryString.split("[- ]");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < split.length; i++) {
            String s = split[i];
            sb.append('(').append(s).append(')');
            if (i < split.length - 1) sb.append('|');
        }
        return sb.toString();
    }

    //显示虚拟键盘
    public static void showInputMethod(View v) {
        InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);

        if (imm != null) {
            imm.showSoftInput(v, InputMethodManager.SHOW_FORCED);
        }

    }

    /**
     * Created by Administrator on 2016/5/8.
     * 用于判断重复点击的，可以设置间隔时间
     */
    public static class DoubleClick {
        public static long sLastTime = -1;
        /**
         * 自定义的
         */
        public long mSelfDefine_lastTime;

        private final long mSelfDefine_TimeInterval;
        /**
         * 注意此方法一般只用于用户点击引起的事件，
         * 如果用于其它事件则可能因为多个不同事件发生时间接近进而导致判断失效
         * 采用默认时间间隔， 目前（2020.4)系统定义的双击正是300毫秒
         */
        public static boolean isDoubleClick() {
            long curTime = System.currentTimeMillis();
            Log.d("double click", "lastTime " + sLastTime);
            //貌似系统定义的双击正是300毫秒 ViewConfiguration.getDoubleTapTimeout()
            if (curTime - sLastTime < ViewConfiguration.getDoubleTapTimeout()) {
                sLastTime = curTime;
                return true;
            } else {
                sLastTime = curTime;
                return false;
            }
        }

        /**
         * 注意此方法一般只用于用户点击引起的事件，
         * 如果用于其它事件则可能因为另一个事件发生时间接近进而导致判断失效
         *
         * @param interval 毫秒，时间不能太长，太长也可能因为另一个事件发生时间接近而导致判断失效
         */
        public static boolean isDoubleClick(long interval) {
            Log.d("double click", "lastTime " + sLastTime);
            long curTime = System.currentTimeMillis();
            if (curTime - sLastTime < interval) {
                sLastTime = curTime;
                return true;
            } else {
                sLastTime = curTime;
                return false;
            }
        }

        public static void cancel() {
            sLastTime = -1;
        }

        /**
         * 自定义双击间隔时间
         *
         * @param time
         */
        public DoubleClick(long time) {
            mSelfDefine_lastTime = -1;
            mSelfDefine_TimeInterval = time;
        }

        public boolean isDoubleClick_m() {
            long curTime = System.currentTimeMillis();
            //貌似系统定义的双击正是300毫秒 ViewConfiguration.getDoubleTapTimeout()
            if (curTime - mSelfDefine_lastTime < mSelfDefine_TimeInterval) {
                mSelfDefine_lastTime = curTime;
                return true;
            } else {
                mSelfDefine_lastTime = curTime;
                return false;
            }
        }

        public void cancel_m() {
            mSelfDefine_lastTime = -1;
        }
    }

    public static void getMesureWH(View v, int[] WH) {
        int width = View.MeasureSpec.makeMeasureSpec((1 << 30) - 1, View.MeasureSpec.AT_MOST);
        int height = View.MeasureSpec.makeMeasureSpec((1 << 30) - 1, View.MeasureSpec.AT_MOST);
        v.measure(width, height);
        WH[0] = v.getMeasuredWidth();
        WH[1] = v.getMeasuredHeight();
    }

    /**
     * 获取两点间的位置
     *
     * @param x1
     * @param y1
     * @param x2
     * @param y2
     * @return
     */
    float getDis(float x1, float y1, float x2, float y2) {
        float dx = x1 - x2, dy = y1 - y2;
        return (float) Math.sqrt(dx * dx + dy * dy);
    }

    public static int getColor(@NotNull Context context, @ColorRes int id) {
        return ContextCompat.getColor(context, id);
    }

    public static int getColor(@ColorRes int id) {
        return ContextCompat.getColor(IntelImEditApplication.appContext, id);
    }


    public static ColorStateList getStateList() {
        return ContextCompat.getColorStateList(IntelImEditApplication.appContext, R.color.imageview_tint_function);
    }

    public static Drawable getStateDrawable(Drawable src, ColorStateList colors, PorterDuff.Mode mode) {
        Drawable drawable = DrawableCompat.wrap(src);
        DrawableCompat.setTintList(drawable, colors);
        DrawableCompat.setTintMode(drawable, mode);
        return drawable;
    }

    public static Drawable getMyChosenIcon(int id) {
        return getStateDrawable(getDrawable(id).mutate(), getStateList(), PorterDuff.Mode.SRC_IN);
    }

    public static boolean isMainThread() {
        return Looper.getMainLooper() == Looper.myLooper();
    }

    public static void fadeOut(View view, long time, Animation.AnimationListener ani) {
        if (view.getVisibility() != View.VISIBLE) return;

        // Since the button is still clickable before fade-out animation
        // ends, we disable the button first to block click.
        // view.setEnabled(false);
        Animation animation = new AlphaAnimation(1F, 0.5F);
        animation.setAnimationListener(ani);
        animation.setDuration(time);
        view.startAnimation(animation);
        // view.setVisibility(View.GONE);
    }

    public static boolean ismMainThread() {
        return Looper.getMainLooper() == Looper.myLooper();
    }


    /**
     * 跳转到应用市场
     */
    public static void goToMarket(Context context, String packageName) {
        try {
            Uri uri = Uri.parse("market://details?id=" + packageName);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (Exception e) {
            ToastUtils.show("您的手机没有安装Android应用市场");
            e.printStackTrace();
        }
    }

    /**
     * 显示热度
     */
    public static String showHotInfo(PicResource picResource) {
        int heat = 0;
        if (picResource != null && picResource.getHeat() != null) {
            heat = picResource.getHeat();
        }
        String hot = "";
        if (heat > 10000) {
            hot = heat / 10000 + "." + heat % 10000 / 1000 + "w";
        } else {
            hot = heat + "";
        }
        return hot;
    }

    public static int idInArray(int[] array, int b) {
        for (int i = 0; i < array.length; i++) {
            if (array[i] == b)
                return i;
        }
        return -1;
    }

    public static void showMemoryStatus() {
        ActivityManager activityManager = (ActivityManager) IntelImEditApplication.appContext.getSystemService(ACTIVITY_SERVICE);
        //最大分配内存
        int memory = activityManager.getMemoryClass();
        LogUtil.d();
        LogUtil.d("内存使用情况");
        LogUtil.d("memory: " + memory);
        //最大分配内存获取方法2
        float maxMemory = (float) (Runtime.getRuntime().maxMemory() * 1.0 / (1024 * 1024));
        //当前分配的总内存
        float totalMemory = (float) (Runtime.getRuntime().totalMemory() * 1.0 / (1024 * 1024));
        //剩余内存
        float freeMemory = (float) (Runtime.getRuntime().freeMemory() * 1.0 / (1024 * 1024));
        LogUtil.d("最大分配内存: " + maxMemory);
        LogUtil.d("totalMemory: " + totalMemory);
        LogUtil.d("freeMemory: " + freeMemory);
    }
}
