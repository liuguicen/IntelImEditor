package com.mandi.intelimeditor.home.view;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.mandi.intelimeditor.common.util.Util;
import com.mathandintell.intelimeditor.R;


/**
 * <pre>
 *      author : liuguicen
 *      time : 2019/08/06
 *      version : 1.0
 * <pre>
 *  图片资源列表（本地，模板、贴图）长按弹出的菜单
 */
public class PopupMenu {
    private final ChoosePopWindowCallback mCallback;
    private final LinearLayout mLinearLayout;
    private final ViewGroup mParentlayout;
    private final TextView mUsuTv;
    private final TextView mTietuTv;
    private final TextView mDeleteTv;
    private final TextView mGifTv;


    /**
     * @param parent 父视图
     */
    public PopupMenu(final ChoosePopWindowCallback callback,
                     final Context context,
                     ViewGroup parent) {
        mLinearLayout = new LinearLayout(context);
        mParentlayout = parent;
        mCallback = callback;

        mLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
        mLinearLayout.setBackground(Util.getDrawable(R.drawable.background_pic_operation));
        mLinearLayout.setGravity(Gravity.CENTER);
        mLinearLayout.setDividerPadding(10);
        mLinearLayout.setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE);
        mLinearLayout.setDividerDrawable(Util.getDrawable(R.drawable.divider_picture_opration));
        mLinearLayout.setLayoutParams(
                new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                        WindowManager.LayoutParams.WRAP_CONTENT));
        mLinearLayout.setPadding(Util.dp2Px(2), Util.dp2Px(2), Util.dp2Px(2), Util.dp2Px(2));
        mUsuTv = createAndAddOneTv(mLinearLayout, context);
        mTietuTv = createAndAddOneTv(mLinearLayout, context);
        mDeleteTv = createAndAddOneTv(mLinearLayout, context);
        mGifTv = createAndAddOneTv(mLinearLayout, context);

        mDeleteTv.setText("删除");
        mDeleteTv.setOnClickListener(v -> callback.requireDeleteChosenPics());
        mGifTv.setText(context.getString(R.string.make_gif));
        mGifTv.setOnClickListener(v->mCallback.toMakeGif());

        int[] popWH = new int[2];
        Util.getMesureWH(mLinearLayout, popWH);
        ConstraintLayout.LayoutParams layoutParams = new ConstraintLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.leftToLeft = ConstraintLayout.LayoutParams.PARENT_ID;
        layoutParams.rightToRight = ConstraintLayout.LayoutParams.PARENT_ID;
        layoutParams.topToTop = ConstraintLayout.LayoutParams.PARENT_ID;
        layoutParams.topMargin = Util.dp2Px(12);
        parent.addView(mLinearLayout, layoutParams);
    }

    private TextView createAndAddOneTv(LinearLayout linearLayout, Context context) {
        TextView textView = new TextView(context);
        textView.setGravity(Gravity.CENTER);
        textView.setGravity(Gravity.CENTER_HORIZONTAL);
        textView.setTextSize(22);
        textView.setTextColor(Util.getColor(R.color.text_deep_black));
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(Util.dp2Px(8), 0, Util.dp2Px(8), 0);
        linearLayout.addView(textView, layoutParams);
        return textView;
    }

    public void hide() {
        mParentlayout.removeView(mLinearLayout);
    }

    public void show() {
        if (mLinearLayout.getParent() == null) {
            mParentlayout.addView(mLinearLayout);
        }
    }

    public void showTietu(String picPath) {
        mTietuTv.setVisibility(View.VISIBLE);
        if (mCallback.isInMyTietu(picPath)) {
            mTietuTv.setText("取消贴图");
            mTietuTv.setOnClickListener(v -> mCallback.deleteMyTietu(picPath));
        } else {
            mTietuTv.setText("加贴图");
            mTietuTv.setOnClickListener(v -> mCallback.addMyTietu(picPath));
        }
    }

    public void hideTietu() {
        mTietuTv.setVisibility(View.GONE);
    }

    public void showPrefer(String picPath) {
        mUsuTv.setVisibility(View.VISIBLE);
        if (mCallback.isInPrefer(picPath)) {
            mUsuTv.setText("取消");
            mUsuTv.setOnClickListener(v -> mCallback.deletePreferPath(picPath));
        } else {
            mUsuTv.setText("常用");
            mUsuTv.setOnClickListener(v -> mCallback.addPreferPath(picPath));
        }
    }

    public void hidePrefer() {
        mUsuTv.setVisibility(View.GONE);
    }

    public void showGifBtn() {
        mGifTv.setVisibility(View.VISIBLE);
    }

    public void hideGifBtn() {
        mGifTv.setVisibility(View.GONE);
    }

    public void onOneChosen(String chosenPath) {
        show();
        showPrefer(chosenPath);
        showTietu(chosenPath);
        hideGifBtn();
    }

    public interface ChoosePopWindowCallback {

        boolean isInPrefer(String path);

        void deletePreferPath(String path);

        void addPreferPath(String path);

        boolean isInMyTietu(String path);

        void addMyTietu(String path);

        void deleteMyTietu(String path);

        void requireDeleteChosenPics();

        void toMakeGif();
    }
}
