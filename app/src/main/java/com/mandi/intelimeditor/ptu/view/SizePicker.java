package com.mandi.intelimeditor.ptu.view;//package a.baozouptu.ptu.view;
//
//import android.content.Context;
//import android.graphics.drawable.Drawable;
//import android.support.v4.content.ContextCompat;
//import android.util.AttributeSet;
//import android.view.Gravity;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.WindowManager;
//import android.widget.FrameLayout;
//import android.widget.ImageView;
//import android.widget.LinearLayout;
//import android.widget.PopupWindow;
//import android.widget.SeekBar;
//
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//
//import a.baozouptu.R;
//import a.baozouptu.common.dataAndLogic.AllData;
//import a.baozouptu.common.util.Util;
//import a.baozouptu.ptu.draw.DrawView;
//
//public class SizePicker extends FrameLayout {
//    private ImageView strokeImageView, strokeAlphaImage, strokeAbsorbColorIv;
//    private SeekBar strokeAlphaSeekBar, strokeSeekBar;
//    private DrawToolChangeListener drawToolChangeListener;
//
//    private int size;
//
//    public SizePicker(@NonNull Context context) {
//        this(context, null);
//    }
//
//    public SizePicker(@NonNull Context context, AttributeSet attrs) {
//        this(context, null, 0);
//    }
//
//    public SizePicker(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
//        super(context, attrs, defStyleAttr);
//        init(context);
//    }
//
//    public void init(Context context) {
//        LayoutInflater.from(context).inflate(R.layout.popup_sketch_stroke, this, true);
//        strokeImageView = findViewById(R.id.stroke_circle);
//        strokeAlphaImage = findViewById(R.id.stroke_alpha_circle);
//        strokeSeekBar = findViewById(R.id.stroke_seekbar);
//        strokeAlphaSeekBar = findViewById(R.id.stroke_alpha_seek_bar);
//        strokeAbsorbColorIv = findViewById(R.id.color_picker_absorb);
//        initListener();
//
//        //画笔宽度缩放基准参数
//        Drawable circleDrawable = Util.getDrawable(R.drawable.circle);
//        assert circleDrawable != null;
//        size = circleDrawable.getIntrinsicWidth();
//
//        strokeSeekBar.setMax(AllData.screenWidth > DrawView.DEFAULT_MAX_PAINT_STROKE_SIZE * 3 ?
//                AllData.screenWidth / 3 : DrawView.DEFAULT_MAX_PAINT_STROKE_SIZE);
//        setSeekBarProgress(Util.dp2Px(8));
//    }
//
//    private void initListener() {
//        strokeAlphaSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
//            @Override
//            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//                int alpha = (progress * 255) / 100;//百分比转换成256级透明度
//                strokeAlphaImage.setImageAlpha(alpha);
//                if (drawToolChangeListener != null) {
//                    drawToolChangeListener.onAlphaChanged(alpha);
//                }
//            }
//
//            @Override
//            public void onStartTrackingTouch(SeekBar seekBar) {
//
//            }
//
//            @Override
//            public void onStopTrackingTouch(SeekBar seekBar) {
//
//            }
//        });
//        //画笔宽度拖动条
//        strokeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
//            @Override
//            public void onStopTrackingTouch(SeekBar seekBar) {
//            }
//
//
//            @Override
//            public void onStartTrackingTouch(SeekBar seekBar) {
//            }
//
//
//            @Override
//            public void onProgressChanged(SeekBar seekBar, int progress,
//                                          boolean fromUser) {
//                setSeekBarProgress(progress);
//            }
//        });
//    }
//
//    /**
//     * @param progress 不是像素单位，怎么算的？
//     */
//    protected void setSeekBarProgress(int progress) {
//        int calcProgress = progress > 1 ? progress : 1;
//        int newSize = Math.round((size / 100f) * calcProgress);
//        int offset = Math.round((size - newSize) / 2f);
//        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(newSize, newSize);
//        lp.setMargins(offset, offset, offset, offset);
//        strokeImageView.setLayoutParams(lp);
//
//        if (drawToolChangeListener != null) {
//            drawToolChangeListener.onSizeChanged(newSize);
//        }
//    }
//
//    public void setDrawToolChangeListener(DrawToolChangeListener drawToolChangeListener) {
//        this.drawToolChangeListener = drawToolChangeListener;
//    }
//
//    public void setCurrentData(int currentSize, int currentAlpha) {
////        strokeAlphaSeekBar.setProgress(currentAlpha);
//    }
//
//    public interface DrawToolChangeListener {
//        void onSizeChanged(int size);
//
//        void onAlphaChanged(int alpha);
//    }
//
//
//    public void showInDefaultLocation(Context acContext, int height, View view) {
//        PopupWindow pop = new PopupWindow(this,
//                WindowManager.LayoutParams.MATCH_PARENT,
//                height, true);
//        pop.setTouchable(true);
//        // 如果不设置PopupWindow的背景，无论是点击外部区域还是Back键都无法dismiss弹框
//        // 我觉得这里是API的一个bug
//        // pop.setAnimationStyle(R.style.mypopwindow_anim_style); // 设置动画之后部分机型不显示popupWindow,难改，暂不设置
//        pop.setBackgroundDrawable(ContextCompat.getDrawable(acContext, R.drawable.text_popup_window_background));
//
//        //防止与虚拟按键冲突
//        //一定设置好参数之后再show,注意注意注意!!!!
//        pop.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
//
//        pop.showAtLocation(view, Gravity.LEFT | Gravity.BOTTOM, 0, 0);
//    }
//}
