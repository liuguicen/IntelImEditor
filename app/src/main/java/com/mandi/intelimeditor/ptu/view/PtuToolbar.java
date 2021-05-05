package com.mandi.intelimeditor.ptu.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.mandi.intelimeditor.common.appInfo.IntelImEditApplication;
import com.mandi.intelimeditor.common.util.Util;
import com.mandi.intelimeditor.R;


/**
 * P图工具栏，负责返回，撤回到上一步，帮助，分享
 *
 * @author yonglong
 * @date 2019/3/12
 */
public class PtuToolbar extends FrameLayout {
    View rootView;
    private ImageView cancelBtn;
    private View backView;
    private ImageView sureBtn;
    private TextView saveBtn;
    private ImageView undoBtn;
    private ImageView redoBtn;
    private ImageView sendBtn;
    private static Bitmap sureBitmap = IconBitmapCreator.createSureBitmap(
            Util.dp2Px(33),
            Color.WHITE);
    private static Bitmap cancelBitmap = IconBitmapCreator.createTietuCancelBm(Util.dp2Px(32),
            Color.WHITE);
    private TextView backTv;

    public PtuToolbar(Context context) {
        super(context);
        initView(context);
    }

    public PtuToolbar(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public PtuToolbar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private void initView(Context context) {
        rootView = LayoutInflater.from(context).inflate(R.layout.layout_ptu_toolbar, this, true);
        undoBtn = rootView.findViewById(R.id.iv_undo);
        redoBtn = rootView.findViewById(R.id.iv_redo);
        saveBtn = rootView.findViewById(R.id.iv_save);
        sureBtn = rootView.findViewById(R.id.iv_sure);
        sureBtn.setImageBitmap(sureBitmap);
        backView = rootView.findViewById(R.id.back_group);
        backTv = rootView.findViewById(R.id.back_tv);
        cancelBtn = rootView.findViewById(R.id.iv_cancel);
        cancelBtn.setImageBitmap(cancelBitmap);
        sendBtn = rootView.findViewById(R.id.iv_help);
    }

    /**
     * 顶部功能条按钮切换
     *
     * @param is2LevelFunction 是否显示
     */
    public void switchToolbarBtn(boolean is2LevelFunction) {
        rootView.findViewById(R.id.iv_help).setVisibility(GONE);
        rootView.findViewById(R.id.iv_redo).setVisibility(VISIBLE);
        rootView.findViewById(R.id.iv_undo).setVisibility(VISIBLE);
        if (is2LevelFunction) {
            cancelBtn.setVisibility(VISIBLE);
            sureBtn.setVisibility(VISIBLE);
            backView.setVisibility(INVISIBLE);
            saveBtn.setVisibility(INVISIBLE);
        } else {
            cancelBtn.setVisibility(GONE);
            sureBtn.setVisibility(GONE);
            backView.setVisibility(VISIBLE);
            saveBtn.setVisibility(VISIBLE);
        }
    }

    public void switch2Transfer() {
        rootView.setBackground(null);
        rootView.findViewById(R.id.iv_help).setVisibility(GONE);
        rootView.findViewById(R.id.iv_redo).setVisibility(GONE);
        rootView.findViewById(R.id.iv_undo).setVisibility(GONE);
        saveBtn.setVisibility(VISIBLE);
        backView.setVisibility(VISIBLE);
    }

    /**
     * @param canRepeal 能否撤销
     */
    public void updateRepealBtn(boolean canRepeal) {
        if (canRepeal) {
            undoBtn.setColorFilter(Util.getColor(R.color.can_repeal_redo));
        } else {
            undoBtn.setColorFilter(Util.getColor(R.color.canot_repeal_redo)); // 如果想恢复彩色显示，设置为null即可
        }
//        LogUtil.d("repeal", "设置颜色完成 canRepeal" + canRepeal);
    }

    /**
     * 能否重做
     */
    public void updateRedoBtn(boolean canRedo) {
        if (canRedo) {
            redoBtn.setColorFilter(Util.getColor(R.color.can_repeal_redo));
        } else {
            redoBtn.setColorFilter(Util.getColor(R.color.canot_repeal_redo));// 如果想恢复彩色显示，设置为null即可
        }
//        LogUtil.d("redo", "设置颜色完成 canRedo=" + canRedo);
    }

    /**
     * 设置Save监听事件
     *
     * @param listener
     */
    public void setOnToolClickListener(OnClickListener listener) {
        sureBtn.setOnClickListener(listener);
        backView.setOnClickListener(listener);
        cancelBtn.setOnClickListener(listener);
        saveBtn.setOnClickListener(listener);
        undoBtn.setOnClickListener(listener);
        redoBtn.setOnClickListener(listener);
        sendBtn.setOnClickListener(listener);
    }

    /**
     * 将SaveSet文字描述等转换成P图作为中间步骤的，所需要的文字描述
     */
    public void setRightTopText(String description) {
        if (description == null) {
            description = IntelImEditApplication.appContext.getString(R.string.finish);
        }
        saveBtn.setText(description);
    }

    public void setLeftTopText(String string) {
        backTv.setText(string);
    }
}
