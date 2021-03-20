package com.mandi.intelimeditor.ptu.draw;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.BlurMaskFilter;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.EmbossMaskFilter;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathDashPathEffect;
import android.graphics.PathEffect;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import com.mandi.intelimeditor.R;


public abstract class BaseDrawView extends View {

    public static final int DRAW_PAINT_STYLE_DEFAULT = 0;
    public static final int DRAW_PAINT_STYLE_1 = 1;
    public static final int DRAW_PAINT_STYLE_2 = 2;
    public static final int DRAW_PAINT_STYLE_3 = 3;
    public static final int DRAW_PAINT_STYLE_4 = 4;
    public static final int PAINT_STYLE_MOSAIC = 5;
    public static final int DRAW_PAINT_STYLE_6 = 6;
    public static final int PAINT_STYLE_CLEAR_DRAW = 9;
    public static final float ERASE_WIDTH = 50;

    public MPaint mPaint;// 真实的画笔

    public int currentColor = Color.RED;
    public int currentSize = 30;
    public int currentAlpha = 255;
    public int currentStyle = 0;

    public Path pathEffect = new Path();
    public PathEffect effect;
    public Shader shader;
    public EmbossMaskFilter emboss;
    public BlurMaskFilter blur;
    public Shader mShader;

    public BaseDrawView(Context context) {
        super(context);
    }

    public BaseDrawView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public BaseDrawView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    //以下为样式修改内容
    //设置画笔样式
    public void selectPaintStyle(int style) {
        currentStyle = style;
        setPaintStyle();
    }

    //选择画笔大小
    public void setPaintSize(int size) {
        this.currentSize = size;
        mPaint.setStrokeWidth(size);
    }

    //设置画笔颜色
    public void setPaintColor(int which) {
        this.currentColor = which;
        setPaintStyle();
    }

    //设置画笔透明度
    public void setStrokeAlpha(int alpha) {
        this.currentAlpha = alpha;
        mPaint.setAlpha(alpha);
    }

    /**
     * Paint类样式说明
     * setMaskFilter(MaskFilter maskfilter); 设置MaskFilter，可以用不同的MaskFilter实现滤镜的效果，如滤化，立体等
     */
    /*
      MaskFilter类可以为Paint分配边缘效果。
     对MaskFilter的扩展可以对一个Paint边缘的alpha通道应用转换。Android包含了下面几种MaskFilter：
     BlurMaskFilter   指定了一个模糊的样式和半径来处理Paint的边缘。
     EmbossMaskFilter  指定了光源的方向和环境光强度来添加浮雕效果。
     要应用一个MaskFilter，可以使用setMaskFilter方法，并传递给它一个MaskFilter对象。下面的例子是对一个已经存在的Paint应用一个EmbossMaskFilter：
*/
    //初始化画笔样式
    public void setPaintStyle() {
        mPaint = new MPaint();

        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);// 设置外边缘
        mPaint.setStrokeCap(Paint.Cap.ROUND);// 形状
        mPaint.setAntiAlias(true);//设置是否使用抗锯齿功能，会消耗较大资源，绘制图形速度会变慢。
        mPaint.setDither(true); //设定是否使用图像抖动处理，会使绘制出来的图片颜色更加平滑和饱满，图像更加清晰
        //初始
        mPaint.setStrokeWidth(currentSize);
        mPaint.setAlpha(currentAlpha);
        mPaint.setColor(currentColor);

        // Shader.TileMode三种模式
        // REPEAT:沿着渐变方向循环重复
        // CLAMP:如果在预先定义的范围外画的话，就重复边界的颜色
        // MIRROR:与REPEAT一样都是循环重复，但这个会对称重复

        switch (currentStyle) {
            case DRAW_PAINT_STYLE_DEFAULT:
                //初始
                mPaint.setStrokeWidth(currentSize);
                mPaint.setColor(currentColor);
                break;
            case DRAW_PAINT_STYLE_1:
                // 应用mask
                if (emboss == null) {
                    // 设置光源的方向
                    float[] direction = new float[]{1.5f, 1.5f, 1.5f};
                    //设置环境光亮度
                    float light = 0.6f;
                    // 选择要应用的反射等级
                    float specular = 6;
                    // 向mask应用一定级别的模糊
                    float mask_blur = 4.2f;
                    //浮雕
                    emboss = new EmbossMaskFilter(direction, light, specular, mask_blur);
                }
                mPaint.setMaskFilter(emboss);
                break;
            case DRAW_PAINT_STYLE_2:
                // 应用mask
                //模糊
                if (blur == null)
                    blur = new BlurMaskFilter(8, BlurMaskFilter.Blur.NORMAL);
                mPaint.setMaskFilter(blur);
                break;
            case DRAW_PAINT_STYLE_4:
                /*
                 * LinearGradient shader = new LinearGradient(0, 0, endX, endY, new
                 * int[]{startColor, midleColor, endColor},new float[]{0 , 0.5f,
                 * 1.0f}, TileMode.MIRROR);
                 * 参数一为渐变起初点坐标x位置，参数二为y轴位置，参数三和四分辨对应渐变终点
                 * 其中参数new int[]{startColor, midleColor,endColor}是参与渐变效果的颜色集合，
                 * 其中参数new float[]{0 , 0.5f, 1.0f}是定义每个颜色处于的渐变相对位置， 这个参数可以为null，如果为null表示所有的颜色按顺序均匀的分布
                 */
                if (mShader == null)
                    mShader = new LinearGradient(0, 0, 100, 100,
                            new int[]{Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW},
                            null, Shader.TileMode.REPEAT);
                mPaint.setShader(mShader);
                break;
            case DRAW_PAINT_STYLE_3:
                shader = new BitmapShader(BitmapFactory.decodeResource(getResources(), R.mipmap.ma), Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
                mPaint.setShader(shader);
                break;
            case PAINT_STYLE_MOSAIC:
                shader = new BitmapShader(BitmapFactory.decodeResource(getResources(), R.drawable.imitate_transparent), Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
                mPaint.setShader(shader);
                break;
            case DRAW_PAINT_STYLE_6:
                effect = new DashPathEffect(new float[]{20f, currentSize * 2, 10f}, 0);
                mPaint.setPathEffect(effect);
                break;
            case 7:
                pathEffect.reset();
                pathEffect.addCircle(10, 10, 5, Path.Direction.CCW);
                effect = new PathDashPathEffect(pathEffect, 20, 0, PathDashPathEffect.Style.ROTATE);
                mPaint.setPathEffect(effect);
                break;
            case 8:
                pathEffect.reset();
                pathEffect.moveTo((float) (0.5 * currentSize), (float) (0.17 * currentSize));
                pathEffect.cubicTo((float) (0.15 * currentSize), (float) (-0.35 * currentSize), (float) (-0.4 * currentSize), (float) (0.45 * currentSize), (float) (0.5 * currentSize), currentSize);
                pathEffect.moveTo((float) (0.5 * currentSize), currentSize);
                pathEffect.cubicTo((float) (currentSize + 0.4 * currentSize), (float) (0.45 * currentSize), (float) (currentSize - 0.15 * currentSize), (float) (-0.35 * currentSize), (float) (0.5 * currentSize), (float) (0.17 * currentSize));
                pathEffect.close();
                effect = new PathDashPathEffect(pathEffect, currentSize * 2, 0, PathDashPathEffect.Style.ROTATE);
                mPaint.setPathEffect(effect);
                break;
            case PAINT_STYLE_CLEAR_DRAW:
                //橡皮擦
                mPaint.setAlpha(0);
                mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
                mPaint.setColor(Color.TRANSPARENT);
                mPaint.setStrokeWidth(ERASE_WIDTH);
                mPaint.isErase = true;
                break;
        }
        dealTransparentColor();
    }

    public abstract void dealTransparentColor();
}
