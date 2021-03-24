package com.mandi.intelimeditor.ptu;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.google.android.material.tabs.TabLayout;
import com.mandi.intelimeditor.common.appInfo.IntelImEditApplication;

import com.mandi.intelimeditor.common.util.LogUtil;
import com.mandi.intelimeditor.common.util.MU;
import com.mandi.intelimeditor.common.util.ToastUtils;
import com.mandi.intelimeditor.common.util.Util;
import com.mandi.intelimeditor.common.util.geoutil.GeoUtil;
import com.mandi.intelimeditor.common.util.geoutil.MPoint;
import com.mandi.intelimeditor.common.util.geoutil.MRect;
import com.mandi.intelimeditor.ptu.dig.DigView;
import com.mandi.intelimeditor.ptu.tietu.FloatImageView;
import com.mandi.intelimeditor.ptu.view.PtuSeeView;
import com.mandi.intelimeditor.user.US;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;

/**
 * p图过程中用到的一些工具
 * Created by liuguicen on 2016/8/5.
 *
 * @description
 */
public class PtuUtil {

    // 用的地方多，不要轻易改动
    public static final int EDIT_MAIN = 0;
    public static final int EDIT_CUT = 1;
    public static final int EDIT_TEXT = 2;
    public static final int EDIT_TIETU = 3;
    public static final int EDIT_DRAW = 4;
    public static final int EDIT_DIG = 5;
    public static final int EDIT_REND = 6;
    public static final int EDIT_DEFORMATION = 7;
    public static final int EDIT_GIF = 8;
    public static final int EDIT_TRANSFER = 9;

    // 一些特殊的P图功能，可能在某个二级功能下面，不一定是直接的二级功能
    public static final int CHILD_FUNCTION_DIG_FACE = 103;
    public static final int CHILD_FUNCTION_GIF = 104;

    // P图页图片打开方式
    public static final int NORMAL_OPEN = 1;
    public static final int OPEN_GIF_FILE = 2;
    public static final int VIDEO_2_GI = 3;
    public static final int PIC_LIST_2_GIF = 4;
    public static final int BM_LIST_2_GIF = 5;

    private static int mGetTietuFailedNumber = 0;
    private static Paint sPaint;

    public static String getEditModeName(int editMode) {
        switch (editMode) {
            case EDIT_MAIN:
                return "P图";
            case EDIT_CUT:
                return "编辑";
            case EDIT_TEXT:
                return "文字";
            case EDIT_TIETU:
                return "贴图";
            case EDIT_DRAW:
                return "绘图";
            case EDIT_DIG:
                return "抠脸";
            case EDIT_REND:
                return "撕图";
            case EDIT_DEFORMATION:
                return "变形";
        }
        return "Error Edit Mode";
    }

    public static boolean isSecondEditMode(int mode) {
        return mode == EDIT_CUT || mode == EDIT_TEXT || mode == EDIT_TIETU || mode == EDIT_DRAW ||
                mode == EDIT_DIG || mode == EDIT_REND || mode == EDIT_DEFORMATION;
    }

    /**
     * 这个方法更好，用于蒙层上的点映射到PTuSeeView中显示的图片，在原图片上即srcBm上的点
     * 只需要保证蒙层视图的范围layout和PTuSeeView的一样即可
     */
    public static MPoint getLocationAtBaseBm(float px, float py, PtuSeeView ptuSeeView) {
        if (ptuSeeView == null) return null;
        String[] xy = getLocationAtBaseBm(Float.toString(px), Float.toString(py),
                ptuSeeView.getSrcRect(), ptuSeeView.getDstRect());
        return new MPoint(Float.parseFloat(xy[0]), Float.parseFloat(xy[1]));
    }


    /**
     * 精确计算view中的点在原图片中的位置
     *
     * @param px      在FrameLayout中的相对位置
     * @param py      在FrameLayout中的相对位置
     * @param srcRect 画图片时，从原图片中扣下来的图所在的矩形
     * @param dstRect 画到PtuFrame的frameLayout中的矩形的位置
     * @return 在原图中的位置
     */
    public static float[] getLocationAtBaseBm(float px, float py, Rect srcRect, Rect dstRect) {
        String[] xy = getLocationAtBaseBm(Float.toString(px), Float.toString(py), srcRect, dstRect);
        return new float[]{Float.valueOf(xy[0]), Float.valueOf(xy[1])};
    }

    /**
     * {@link #getLocationAtBaseBm(float, float, Rect, Rect)}
     */
    public static MPoint getPointAtPicture(float px, float py, Rect srcRect, Rect dstRect) {
        String[] xy = getLocationAtBaseBm(Float.toString(px), Float.toString(py), srcRect, dstRect);
        return new MPoint(Float.valueOf(xy[0]), Float.valueOf(xy[1]));
    }

    /**
     * bm上的位置转化到PTuSeeView上面
     * 注意不改变参数的值，返回值才有效
     */
    public static MPoint bmPosition2PtuSeeView(MPoint bmPos, PtuSeeView ptuSeeView) {
        MPoint viewPos = bmPos.numMulti(ptuSeeView.totalRatio);
        Rect dstRect = ptuSeeView.getDstRect();
        viewPos.add_(dstRect.left, dstRect.top);
        return viewPos;
    }

    /**
     * FloatImageView进行了旋转，缩放，将Bm内部的位置转换成View上的位置
     * @return
     */
    public static MPoint bmPosition2FloatImageView(MPoint bmPos, FloatImageView fiv) {
        // 首先获取缩放后的位置
        MPoint newPos = bmPos.numMulti(fiv.bmScaleRatio).add_(FloatImageView.PAD, FloatImageView.PAD);
        newPos.rotate_radius_(fiv.getWidth() / 2f, fiv.getHeight() / 2f, Math.toRadians(fiv.getRotation()));
        return newPos;
    }


    /**
     * 精确计算view中的点在原图片中的位置
     *
     * @param px      在PTuSeeView右上角的相对位置
     * @param py      在PTuSeeView右上角的相对位置
     * @param srcRect 画图片时，从原图片中扣下来的图所在的矩形
     * @param dstRect 画到目标View中的矩形，
     *                就是PTuSeeView中对应的两个矩形
     * @return 在原图中的位置, 字符串的形式
     */
    public static String[] getLocationAtBaseBm(String px, String py, Rect srcRect, Rect dstRect) {
        px = MU.su(px, Float.toString(dstRect.left));
        py = MU.su(py, Float.toString(dstRect.top)); // 首先得到相对于目标矩形右上角的坐标位置
        String x, y;

        String srcWidth = MU.su(Float.toString(srcRect.right), Float.toString(srcRect.left));
        String dstWidth = MU.su(Float.toString(dstRect.right), Float.toString(dstRect.left));
        String ratio = MU.di(srcWidth, dstWidth);  // 再得到反向（原矩形宽除以目标矩形图）的缩放比例
        String px1 = MU.mu(px, ratio);
        String py1 = MU.mu(py, ratio);
        x = MU.add(px1, Float.toString(srcRect.left));
        y = MU.add(py1, Float.toString(srcRect.top));
        return new String[]{x, y};
    }

    public static Path converPath2Picture(Path drawPath, RectF srcRect, RectF dstRect) {
        Matrix matrix = new Matrix();
        matrix.setTranslate(-dstRect.left, -dstRect.top);
        float ratio = srcRect.width() / dstRect.width();
        matrix.postScale(ratio, ratio);
        matrix.setTranslate(srcRect.left, srcRect.top);
        Path picPath = new Path();
        drawPath.transform(matrix, picPath);
        return picPath;
    }

    /**
     * 获取静态的画笔，用与draw等中不用创建对象
     */
    public static Paint getNormalPaint(boolean needReset) {
        if (sPaint == null) {
            sPaint = new Paint();
        } else if (needReset) {
            sPaint.reset();
        }
        return sPaint;
    }


    /**
     * 修改tablayout下划线长度
     *
     * @param tabLayout
     */
    public static void changeTabLayoutIndicator(final TabLayout tabLayout, final int screenW, final int tabCount) {
        //了解源码得知 线的宽度是根据 tabView的宽度来设置的
        tabLayout.post(new Runnable() {
            @Override
            public void run() {
                try {
                    //拿到tabLayout的mTabStrip属性
                    LinearLayout mTabStrip = (LinearLayout) tabLayout.getChildAt(0);
                    for (int i = 0; i < mTabStrip.getChildCount(); i++) {
                        View tabView = mTabStrip.getChildAt(i);
                        //拿到tabView的mTextView属性  tab的字数不固定一定用反射取mTextView
                        Field mTextViewField = tabView.getClass().getDeclaredField("mTextView");
                        mTextViewField.setAccessible(true);
                        TextView mTextView = (TextView) mTextViewField.get(tabView);
                        tabView.setPadding(0, 0, 0, 0);
                        //字多宽线就多宽，所以测量mTextView的宽度
                        int width = 0;
                        width = mTextView.getWidth();
                        if (width == 0) {
                            mTextView.measure(0, 0);
                            width = mTextView.getMeasuredWidth();
                        }
                        //设置tab左右间距为10dp  注意这里不能使用Padding 因为源码中线的宽度是根据 tabView的宽度来设置的
                        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) tabView.getLayoutParams();
                        params.width = width;
                        params.leftMargin = (screenW / -width) / 2;
                        params.rightMargin = (screenW / tabCount - width) / 2;
                        tabView.setLayoutParams(params);
                        tabView.setBackground(null);
                        tabView.invalidate();
                    }

                } catch (NoSuchFieldException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    public static Bitmap addBm2Bm(Bitmap baseBitmap, Bitmap addBitmap, RectF boundRect, float rotateAngle) {
        Canvas c = new Canvas(baseBitmap);
        addBm2Canvas(c, addBitmap, boundRect, rotateAngle);
        return baseBitmap;
    }

    public static Canvas addBm2Canvas(@NotNull Canvas baseCanvas, @Nullable Bitmap addBitmap, RectF boundRect, float rotateAngle) {
        baseCanvas.setDrawFilter(new PaintFlagsDrawFilter(0,
                Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG | Paint.DITHER_FLAG));
        if (addBitmap == null) return baseCanvas;
        baseCanvas.save();
        float centerX = (boundRect.left + boundRect.right) / 2, centerY = (boundRect.bottom + boundRect.top) / 2;
        //将realBm到图上
        // TODO: 2020/7/6 可以不用下面这种方式了，待处理
        BitmapDrawable addDrawable = new BitmapDrawable(IntelImEditApplication.appContext.getResources(), addBitmap);
        addDrawable.setDither(true);
        addDrawable.setAntiAlias(true);
        addDrawable.setFilterBitmap(true);
        baseCanvas.rotate(rotateAngle, centerX, centerY);//旋转
        addDrawable.setBounds(GeoUtil.rectF2Rect(boundRect));
        addDrawable.draw(baseCanvas);
        baseCanvas.restore();
        return baseCanvas;
    }

    public static Canvas combineBitmap(Canvas baseCanvas, Bitmap addBitmap) {
        baseCanvas.setDrawFilter(new PaintFlagsDrawFilter(0,
                Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG | Paint.DITHER_FLAG));
        if (addBitmap == null) return baseCanvas;
        baseCanvas.save();
        Bitmap newBp = Bitmap.createScaledBitmap(addBitmap, baseCanvas.getWidth(), baseCanvas.getHeight(), true);
        baseCanvas.drawBitmap(newBp, 0, 0, null);
        baseCanvas.restore();
        return baseCanvas;
    }

    public static Canvas combineBitmap(Bitmap baseBitmap, Bitmap addBitmap) {
        Canvas baseCanvas = new Canvas(baseBitmap);
        baseCanvas.setDrawFilter(new PaintFlagsDrawFilter(0,
                Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG | Paint.DITHER_FLAG));
        if (addBitmap == null) return baseCanvas;
        baseCanvas.save();
        Bitmap newBp = Bitmap.createScaledBitmap(addBitmap, baseCanvas.getWidth(), baseCanvas.getHeight(), true);
        baseCanvas.drawBitmap(baseBitmap, 0, 0, null);
        baseCanvas.drawBitmap(newBp, 0, 0, null);
        baseCanvas.restore();
        return baseCanvas;
    }

    public static void onNoPicResource(String msg) {
        mGetTietuFailedNumber++;
        ToastUtils.show(msg);
    }


    /**
     * 设置用于放置3级功能按钮的popupWindow的布局
     *
     * @param view   宿主View
     * @param layout PopUpWindow内部作为本身容器的layout
     */
    public static void setPopWindow_for3LevelFunction(PopupWindow popWindow, View view, ViewGroup layout) {
        int[] popWH = new int[2];
        Util.getMesureWH(layout, popWH);
        popWindow.setContentView(layout);
        popWindow.setWidth(view.getWidth());
        popWindow.setHeight(popWH[1]);
        //自己的高度，减去父布局的padding，再减去View的高度
        int top = -view.getHeight()
                - ((ViewGroup) view.getParent()).getPaddingTop()
                - popWH[1];
        popWindow.showAsDropDown(view, 0, top);
    }

    /**
     * 放大View的某个点, 并显示出来，这个方法设计比较麻烦的图形的几何变化，比较难想（对于自己），代码不要随便改了
     * 放大的结果放到传进来的参数bm中
     * 原理是绘制放大区域的bm放入canvas，再将canvas传入view的draw,对canvas进行一定变换，最后就将View某个附近的区域绘制到bm中了
     *
     * @param x，y          触摸点相对于View左上角的位置，View旋转处理了，缩放平移自行处理
     * @param enlargeWidth 放大区域的宽度
     * @param enlargeBm    用于绘制放大点周围区域的bm
     * @param isClearOld   是否清楚旧的，上次绘制的内容
     * @param paintWidth   是否绘制画笔指示图标，是则传入画笔宽度，传入负数表示不绘制
     */
    public static void enlargeViewPoint(@NotNull View view,
                                        float x, float y,
                                        float enlargeWidth,
                                        @NotNull Bitmap enlargeBm,
                                        boolean isClearOld,
                                        float paintWidth,
                                        float blurWidth) {
        int enlargeBmW = enlargeBm.getWidth();
        float r = enlargeBmW / 2f;
        if (isClearOld) {
            enlargeBm.eraseColor(0);
        }
        // 现在的画布相当于在enlargeBm的大小一样，位置也是0,0，bmw,bmw
        Canvas canvas = new Canvas(enlargeBm);
        canvas.save();
        int saveLayer = -Integer.MAX_VALUE;
        if (view.getLayerType() == View.LAYER_TYPE_HARDWARE
                && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // 使用了离屏绘制，canvas也要使用离屏绘制， 不然包含透明度的绘图操作绘制不了
            saveLayer = canvas.saveLayer(null, null);
        }
        // 要让enlargeBm上面画上触摸点x,y附近的图像，怎么办，也就是把enlargeBm放到View的x，y上面，通过平移画布可以，让画布
        // 左上角与View左上角重合，注意此时enlargeBm没动，View会从画布的左上角开始画，这样就把x,y附近的图画到了enlargeBm上面
        canvas.translate(-(x - r), -(y - r));
        // enlargeBm和要放大的区域大小是不同的，可能bm无法画上整个要放大的区域，此时
        // 缩放canvas，也就是把View画在canvas上面的东西缩放，以触摸点x,y为中心，缩放bm宽/放大区域的宽即可
        float scaleRatio = enlargeBmW / enlargeWidth;
        canvas.scale(scaleRatio, scaleRatio, x, y);

        // 最后一步，如果View发生了旋转，上面得到的放大图是以旋转后的方向为正，从用户角度来看，是偏着的，
        // 然后把这个偏着的View放到正着的enlargeView中，最后用户看起来就会是偏着的
        // 根据旋转关系，任意一条直线，原来与x轴是a度，绕q旋转b度之后，与原来的x轴是a+b度
        // 可以推得一条直线，绕其它点旋转，和绕这里的x,y点旋转后，角度变化相同，可以看成x,y附近区域绕x,y旋转了，
        // 所以这里绕着x，y正向或反向旋转a度即可，
        // 这里应该正向，即和用户旋转方向相同
        canvas.rotate(view.getRotation(), x, y);
        if (LogUtil.debugTietuEraser) {
            Log.d(DigView.TAG, String.format("drawEnlargeView: 影响半径%f, bm宽度%d，缩放比%f", enlargeWidth, enlargeBmW, scaleRatio));
            Log.d(DigView.TAG, String.format("drawEnlargeView平移距离: %f %f", -(x - r), -(y - r)));
            Log.d(DigView.TAG, "drawEnlargeView: 被放大的位置" + new MRect(x - r, y - r, x + r, y + r));
            Log.d(DigView.TAG, "drawEnlargeView: canvas的矩形区域" + canvas.getClipBounds());
        }
        view.draw(canvas);
        if (saveLayer != -Integer.MAX_VALUE) {
            canvas.restoreToCount(saveLayer);
        }
        // 画笔宽度也要对应缩放
        if (paintWidth > 0) {
            drawPaintIndicator(canvas, enlargeBmW, paintWidth * scaleRatio, blurWidth * scaleRatio);
        }
        canvas.restore();
    }

    /**
     * 画画笔影响范围的icon
     * 对于模糊半径影响的区域，也表现出来
     */
    private static void drawPaintIndicator(Canvas canvas, float bmW, float paintWidth, float blurWidth) {
        // 模糊区域用半透明的圆环表示，那么圆环宽度就是画笔矩形和整个矩形的中间中间值
        float outCircleWidth = paintWidth + blurWidth / 2;
        float left = (bmW - outCircleWidth) / 2, top = left,
                right = left + outCircleWidth, bottom = top + outCircleWidth;
        Paint paint = getNormalPaint(true);
        paint.setColor(0x22aaaaaa);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(blurWidth);
        paint.setAntiAlias(true);
        paint.setDither(true);

        RectF oval = new RectF(left, top, right, bottom);
        canvas.drawArc(oval, 0, 360,
                false, paint);
        paint.setAntiAlias(false);
        paint.setColor(Color.GRAY);
        paint.setStrokeWidth(1);
        oval.set((bmW - paintWidth) / 2, (bmW - paintWidth) / 2,
                (bmW + paintWidth) / 2, (bmW + paintWidth) / 2);
        canvas.drawArc(oval, 0, 360, false, paint);
    }

    /**
     * @see PTuIntentBuilder
     */
    public static PTuIntentBuilder buildPTuIntent(Activity activity, String url) {
        return new PTuIntentBuilder(activity, url);
    }

    /**
     * PTuAc 要设置的项比较多时，建造器模式，使用起来更清晰一些
     */
    public static class PTuIntentBuilder {
        final Activity activity;
        final Intent intent = new Intent();

        public PTuIntentBuilder(@NotNull Activity activity, @NotNull String url) {
            this.activity = activity;
            intent.setComponent(new ComponentName(activity, PtuActivity.class));
            intent.setAction(PtuActivity.PTU_ACTION_NORMAL);
            intent.putExtra(PtuActivity.INTENT_EXTRA_PIC_PATH, url);
        }

        /**
         * 第二种模式：作为其它功能需要P图的一个中间步骤，这个中间步骤可能需要先进入P图界面的某个子功能，
         * P完图之后，不是保存分享，而是将结果路径或bm设置看，返回原来的功能，
         *
         * @param finishName PTu作为中间步骤结束时的名字 比如完成或者下一步
         */
        public PTuIntentBuilder setAsIntermediate(@Nullable String finishName) {
            intent.setAction(PtuActivity.PTU_ACTION_AS_INTERMEDIATE_PTU);
            intent.putExtra(PtuActivity.INTENT_EXTRA_INTERMEDIATE_PTU_FINISH_NAME, finishName);
            return this;
        }

        /**
         * 用户P图的时候一直显示的提示，可以点击叉关闭
         */
        public PTuIntentBuilder setPtuNotice(String notice) {
            intent.putExtra(PtuActivity.INTENT_EXTRA_PTU_NOTICE, notice);
            return this;
        }

        /**
         * 传入需要进入的P图子功能 {@link PtuUtil#EDIT_CUT 等几个二级功能，或者特殊规定的其它功能，
         * 然后进入P图页面时会直接跳转到该子功能}
         **/
        public PTuIntentBuilder setToChildFunction(int childFunction) {
            intent.putExtra(PtuActivity.INTENT_EXTRA_TO_CHILD_FUNCTION, childFunction);
            return this;
        }


        public void startActivity() {
            activity.startActivity(intent);
        }

        public void startActivityForResult(int requestCode) {
            activity.startActivityForResult(intent, requestCode);
        }

        public Intent getIntent() {
            return intent;
        }

        public PTuIntentBuilder putExtras(Intent other) {
            intent.putExtras(other);
            return this;
        }
    }


    public static String getUSEventByType(int type) {
        switch (type) {
            case EDIT_TRANSFER:
                return US.MAIN_FUNCTION_EDIT;
            case EDIT_REND:
                return US.MAIN_FUNCTION_REND;
            case EDIT_TEXT:
                return US.MAIN_FUNCTION_TEXT;
            case EDIT_TIETU:
                return US.MAIN_FUNCTION_TIETU;
            case EDIT_DIG:
                return US.MAIN_FUNCTION_DIG;
            case EDIT_DRAW:
                return US.MAIN_FUNCTION_DRAW;
            case EDIT_CUT:
                return US.MAIN_FUNCTION_EDIT;
            case EDIT_DEFORMATION:
                return US.MAIN_FUNCTION_DEFORMATION;
        }
        return "error param";
    }
}
