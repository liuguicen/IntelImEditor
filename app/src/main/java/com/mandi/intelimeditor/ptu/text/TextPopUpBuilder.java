package com.mandi.intelimeditor.ptu.text;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.jaygoo.widget.OnRangeChangedListener;
import com.jaygoo.widget.RangeSeekBar;
import com.mandi.intelimeditor.ad.tencentAD.InsertAd;
import com.mandi.intelimeditor.common.dataAndLogic.AllData;
import com.mandi.intelimeditor.common.util.Util;
import com.mandi.intelimeditor.common.view.MySwitchButton;
import com.mandi.intelimeditor.dialog.FirstUseDialog;
import com.mandi.intelimeditor.ptu.common.SimpleEraser;
import com.mandi.intelimeditor.ptu.threeLevelFunction.ThreeLevelToolUtil;
import com.mandi.intelimeditor.ptu.view.ColorPicker;
import com.mandi.intelimeditor.R;

import text.TypefacePopWindow;

import java.lang.ref.WeakReference;

/**
 * 创建添加文字模块功能区的功能操作视图
 * Created by Administrator on 2016/5/24.
 */
public class TextPopUpBuilder {
    private FloatTextView floatTextView;
    private Activity activity;
    // null 代表系统默认字体，原来的代码都有问题，强制设置成Android默认字体了，但是用户会改字体的，这样就不一样，有问题了
    public Typeface curTypeface = null;
    // 控制粗体，斜体，阴影
    private boolean isBold = false, isItalic = false, hasShadow = false;
    private TextFragment textFragment;
    private SimpleEraser eraser;

    /**
     * 使用弱引用持有字体功能视图，节省内存
     */
    private WeakReference<TypefacePopWindow> weakTypefacePopup;

    public TextPopUpBuilder(Activity activity, FloatTextView floatTextView, TextFragment textFragment) {
        this.activity = activity;
        this.floatTextView = floatTextView;
        this.textFragment = textFragment;
    }

    void setTypefacePopWindow(View v) {
        if (weakTypefacePopup == null || weakTypefacePopup.get() == null) {
            weakTypefacePopup = new WeakReference<>(new TypefacePopWindow(activity, floatTextView, this));
        }
//        注意这里contentView的监听器是持有TypefacePopWindow的，contentView被window持有，
//      window消失之后监听器当做强引用方式回收，这时相当于TypefacePopWindow没被引用了那样回收
        //既FunctionPopWindowBuilder还在，TypefacePopWindow相当于不存在了
        View contentView = weakTypefacePopup.get().createTypefacePopWindow();
        ThreeLevelToolUtil.showThreeLevelFunctionPop(activity, v, contentView);
    }

    //    风格
    private View getStylePopView() {
        View contentView = LayoutInflater.from(activity).inflate(R.layout.popwindow_text_style, null);
        return contentView;
    }

    void setStylePopWindow(View v) {
        View contentView = getStylePopView();
        //粗体
        MySwitchButton switchBold = contentView.findViewById(R.id.switch_button_bold);

        switchBold.setState(isBold);
        switchBold.setOnSlideListener(new MySwitchButton.SlideListener() {
            @Override
            public void open() {
                InsertAd.onClickTarget(activity);
                if (isItalic) {
                    // floatTextView.setTypeface(floatTextView.getTypeface(), Typeface.BOLD_ITALIC);
                    // 不能用上面的方法，floatTextView.getTypeface()的到的字体已经加上了字体的风格，然后内部判断字体相同然后就不更新，有问题
                    floatTextView.setTypeface(curTypeface, Typeface.BOLD_ITALIC);
                    floatTextView.updateSize();
                } else {
                    floatTextView.setTypeface(curTypeface, Typeface.BOLD);
                    floatTextView.updateSize();
                }
                isBold = true;
            }

            @Override
            public void close() {
                InsertAd.onClickTarget(activity);
                if (isItalic) {
                    floatTextView.setTypeface(curTypeface, Typeface.ITALIC);
                    floatTextView.updateSize();
                } else {
                    floatTextView.setTypeface(curTypeface, Typeface.NORMAL);
                    floatTextView.updateSize();
                }
                isBold = false;
            }
        });
        //斜体
        MySwitchButton switchItalic = (MySwitchButton) contentView.findViewById(R.id.switch_button_text_italic);
        switchItalic.setState(isItalic);
        switchItalic.setOnSlideListener(new MySwitchButton.SlideListener() {
            @Override
            public void open() {
                if (isBold) {
                    floatTextView.setTypeface(curTypeface, Typeface.BOLD_ITALIC);//斜体，中文有效
                    floatTextView.updateSize();
                } else {
                    floatTextView.setTypeface(curTypeface, Typeface.ITALIC);//斜体，中文有效
                    floatTextView.updateSize();
                }
                isItalic = true;
            }

            @Override
            public void close() {
                if (isBold) {
                    floatTextView.setTypeface(curTypeface, Typeface.BOLD);
                } else {
                    floatTextView.setTypeface(curTypeface, Typeface.NORMAL);
                    floatTextView.updateSize();
                }
                isItalic = false;
            }
        });
        MySwitchButton switchShadow = (MySwitchButton) contentView.findViewById(R.id.switch_button_text_shadow);
        switchShadow.setState(hasShadow);
        switchShadow.setOnSlideListener(new MySwitchButton.SlideListener() {
            @Override
            public void open() {
                floatTextView.setShadowLayer(5, 5, 5, Color.GRAY);
                floatTextView.updateSize();
                hasShadow = true;
            }

            @Override
            public void close() {
                floatTextView.setShadowLayer(0, 0, 0, Color.GRAY);
                floatTextView.updateSize();
                hasShadow = false;
            }
        });
        ThreeLevelToolUtil.showThreeLevelFunctionPop(activity, v, contentView);
    }

    //颜色
    void setColorPopWindow(View v, ColorPicker.ColorTarget colorTarget) {
        ColorPicker colorPicker = new ColorPicker(activity);
        colorPicker.setColorTarget(colorTarget);
        colorPicker.addViewToGetColor(textFragment.ptuSeeView, textFragment.ptuSeeView.getSourceBm(),
                textFragment.ptuSeeView.getSrcRect(), textFragment.ptuSeeView.getDstRect());
        colorPicker.setAbsorbListener(new ColorPicker.AbsorbListener() {
            @Override
            public void startAbsorb(ColorPicker colorPicker) {
                if (!AllData.hasReadConfig.hasRead_absorb()) {
                    FirstUseDialog firstUseDialog = new FirstUseDialog(activity);
                    firstUseDialog.createDialog("吸取颜色", "可在图片中吸取想要的颜色，吸取之后点击其它地方即可使用", new FirstUseDialog.ActionListener() {
                        @Override
                        public void onSure() {
                            AllData.hasReadConfig.put_absorb(true);
                        }
                    });
                }
            }

            @Override
            public boolean stopAbsorbColor() {
                return false;
            }
        });
        ColorPicker.showInDefaultLocation(activity, colorPicker, v.getHeight() + Util.dp2Px(5), v.getRootView());
    }


    //透明度
    void setToumingduPopWindow(View v) {
        View contentView = createTransparencyPopView();
        ThreeLevelToolUtil.showThreeLevelFunctionPop(activity, v, contentView);
    }

    /**
     * 透明度
     */
    private View createTransparencyPopView() {
        View contentView = LayoutInflater.from(activity).inflate(R.layout.seek_bar_layout, null);
        RangeSeekBar rangeSeekBar = contentView.findViewById(R.id.seek_bar_popw);
        TextView valueTv = contentView.findViewById(R.id.seek_bar_value_tv);
        rangeSeekBar.setRange(0, 255);
        if (floatTextView.isClickable()) {
            rangeSeekBar.setProgress(floatTextView.getTextTransparency());
            valueTv.setText(String.valueOf(floatTextView.getTextTransparency()));
        } else {
            rangeSeekBar.setProgress(eraser.getRubberWidth());
            valueTv.setText(String.valueOf(eraser.getRubberWidth()));
        }
        rangeSeekBar.setOnRangeChangedListener(new OnRangeChangedListener() {
            @Override
            public void onRangeChanged(RangeSeekBar view, float leftValue, float rightValue, boolean isFromUser) {
                int intValue = (int) leftValue;
                if (floatTextView.isClickable()) {
//                    Log.d("---", "onRangeChanged: " + leftValue);
                    floatTextView.setTextTransparency(intValue);
                } else eraser.setRubberWidth(intValue);
                valueTv.setText(String.valueOf(intValue));
            }

            @Override
            public void onStartTrackingTouch(RangeSeekBar view, boolean isLeft) {

            }

            @Override
            public void onStopTrackingTouch(RangeSeekBar view, boolean isLeft) {

            }
        });
//        rangeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
//            @Override
//            public void onProgressChanged(SeekBar rangeSeekBar, int progress, boolean fromUser) {
//                if (floatTextView.isClickable())
//                    floatTextView.setTextTransparency(progress);
//                else eraser.setRubberWidth(progress);
//            }
//
//            @Override
//            public void onStartTrackingTouch(SeekBar rangeSeekBar) {
//            }
//
//            @Override
//            public void onStopTrackingTouch(SeekBar rangeSeekBar) {
//            }
//        });
        return contentView;
    }

    public void setEraser(SimpleEraser eraser) {
        this.eraser = eraser;
    }
}