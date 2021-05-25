package com.mandi.intelimeditor.ptu.cut;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.mandi.intelimeditor.ad.tencentAD.InsertAd;
import com.mandi.intelimeditor.common.util.BitmapUtil;
import com.mandi.intelimeditor.common.util.FileTool;
import com.mandi.intelimeditor.common.util.Util;
import com.mandi.intelimeditor.ptu.BasePtuFragment;
import com.mandi.intelimeditor.ptu.PTuActivityInterface;
import com.mandi.intelimeditor.ptu.PtuUtil;
import com.mandi.intelimeditor.ptu.gif.GifManager;
import com.mandi.intelimeditor.ptu.repealRedo.CutStepData;
import com.mandi.intelimeditor.ptu.repealRedo.StepData;
import com.mandi.intelimeditor.ptu.threeLevelFunction.ThreeLevelToolUtil;
import com.mandi.intelimeditor.ptu.view.PtuSeeView;
import com.mandi.intelimeditor.user.useruse.FirstUseUtil;
import com.mandi.intelimeditor.bean.FunctionInfoBean;
import com.mandi.intelimeditor.R;

import java.util.List;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

public class CutFragment extends BasePtuFragment {
    private PtuSeeView ptuSeeView;
    private CutView cutView;

    private SizeRatioDialog sizeRatioDialog;
    private PTuActivityInterface pTuActivityInterface;

    private static final String[] RATIO_NAMES = new String[]{
            "1:1", "3:2", "2:3", "4:3", "3:4", "16:9", "9:16", "自定义", "自由"
    };
    private static final float[] RATIOS = new float[]{
            1, 3f / 2, 2f / 3, 4f / 3, 3f / 4, 16f / 9, 9f / 16, 1, 1
    };

    @Override
    public void onResume() {
        FirstUseUtil.editMoveUseGuide(mContext);
        super.onResume();
    }

    @Override
    public int getEditMode() {
        return PtuUtil.EDIT_CUT;
    }

    @Override
    public int getLayoutResId() {
        return R.layout.fragment_bottom_function;
    }

    @Override
    public void setPTuActivityInterface(PTuActivityInterface ptuActivity) {
        this.pTuActivityInterface = ptuActivity;
        this.ptuSeeView = ptuActivity.getPtuSeeView();
    }

    @Override
    public boolean onSure() {
        return false;
    }


    public Bitmap getResultBm(float ratio) {
        GifManager gifManager = pTuActivityInterface.getGifManager();
        if (gifManager != null) {
            gifManager.stopAnimation(); // 必须先完全停止动画，否则bm尺寸变了，会出错
            return cutView.getResultBm(gifManager);
        }
        return cutView.getResultBm();
    }

    @Override
    public StepData getResultDataAndDraw(float ratio) {
        //获取并保存数据
        Bitmap resultBm = getResultBm(1);
        if (resultBm == null) return null; // 没有操作
        StepData csd = new CutStepData(PtuUtil.EDIT_CUT);
        String tempPath = FileTool.createTempPicPath();
        BitmapUtil.asySaveTempBm(tempPath, resultBm, new Observer<String>() {
            @Override
            public void onComplete() {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(String realPath) {
                csd.picPath = realPath;
            }
        });
        csd.picPath = tempPath;

        //重新绘制
        ptuSeeView.post(() -> {
            ptuSeeView.replaceSourceBm(resultBm);
            GifManager gifManager = pTuActivityInterface.getGifManager();
            if (gifManager != null) {
                gifManager.startAnimation();
            }
        });
        return csd;
    }

    /**
     * 做一大步功能,
     * 注意，这里的函数可能会在非UI线程调用，做UI线程才能做的动作要post
     */
    public static void addBigStep(StepData sd, PTuActivityInterface pTuActivityInterface) {
        // pTuActivityInterface.replaceBase(sd.picPath);
        PtuSeeView ptuSeeView = pTuActivityInterface.getPtuSeeView();
        Bitmap bm = BitmapUtil.decodeLosslessBitmap(sd.picPath);
        ptuSeeView.post(() -> ptuSeeView.replaceSourceBm(bm));
    }

    @Override
    public void releaseResource() {
        if (sizeRatioDialog != null) {
            sizeRatioDialog.dismissDialog();
        }
        cutView.releaseResource();
        cutView = null; // 每次加入cutFrag会新建，释放时直接空掉
    }

    private void userDefinedRatio() {
        sizeRatioDialog = new SizeRatioDialog(mContext, 1);
        sizeRatioDialog.createDialog();
        sizeRatioDialog.setActionListener((w, h) -> cutView.setFixedRatio(h / w));
    }

    private void userDefinedSize() {
        sizeRatioDialog = new SizeRatioDialog(mContext, 0);
        sizeRatioDialog.createDialog();
        sizeRatioDialog.setActionListener((w, h) -> cutView.setFixedSize((int) (w + 0.5f), (int) (h + 0.5f)));
    }

    private void onClickRotate() {
        cutView.rotate(0, 0, 90);
    }

    public View createCutView(Context context, Rect totalBound, Bitmap sourceBm) {
        cutView = new CutView(context, totalBound, sourceBm);
        cutView.setPTuActivityInterface(pTuActivityInterface);
        cutView.setCanDoubleClick(false);
        cutView.setCanLessThanScreen(false);
        return cutView;
    }

    private TextView createItem(final int pad) {
        TextView tv = new TextView(mContext);
        tv.setGravity(Gravity.CENTER);
        tv.setTextSize(20);
        tv.setTextColor(Util.getColor(R.color.text_deep_black));
        return tv;
    }

    @Override
    public void smallRepeal() {
    }

    @Override
    public void smallRedo() {
    }

    @Override
    public List<FunctionInfoBean> getFunctionList() {
        //设置底部图标数据
        pFunctionList.clear();
        pFunctionList.add(new FunctionInfoBean(R.string.size, R.mipmap.fixed_size,  PtuUtil.EDIT_CUT));
        pFunctionList.add(new FunctionInfoBean(R.string.cut_scale, R.mipmap.scale, PtuUtil.EDIT_CUT));
        pFunctionList.add(new FunctionInfoBean(R.string.cut_rotate, R.mipmap.rotate, PtuUtil.EDIT_CUT));
        pFunctionList.add(new FunctionInfoBean(R.string.cut_flip, R.drawable.flip, PtuUtil.EDIT_CUT));
        for (int i = 0; i < pFunctionList.size(); i++) {
            pFunctionList.get(i).setCanSelected(true);
        }
        return pFunctionList;
    }

    @Override
    public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
        super.onItemClick(adapter, view, position);
        InsertAd.onClickTarget(getActivity());
        pFunctionAdapter.updateSelectIndex(position);
        switch (pFunctionList.get(position).getTitleResId()) {
            case R.string.size:
                showFixSizeWindow(view);
                break;
            case R.string.cut_scale:
                showFixRatioWindow(view);
                break;
            case R.string.cut_rotate:
                onClickRotate();
                break;
            case R.string.cut_flip:
                showFixFlipWindow(view);
                break;
        }
    }

    private void showFixSizeWindow(View view) {
        final int pad = 10;
        final PopupWindow popWindow = ThreeLevelToolUtil.getPopWindow_for3LevelFunction(mContext);
        LinearLayout linearLayout = new LinearLayout(mContext);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setBackgroundColor(Color.WHITE);
        linearLayout.setPadding(0, pad, 0, pad);
        final TextView custom = createItem(pad);
        custom.setText("自定义");
        custom.setOnClickListener(v -> {
            userDefinedSize();
            custom.setTextColor(Util.getColor(R.color.text_checked_color));
            popWindow.dismiss();
        });
        final TextView free = createItem(pad);
        free.setText("自由");
        free.setOnClickListener(v -> {
            free.setTextColor(Util.getColor(R.color.text_checked_color));
            popWindow.dismiss();
        });
        TextView d1 = new TextView(mContext);
        d1.setHeight(pad);
        TextView d2 = new TextView(mContext);
        d2.setHeight(pad);
        TextView d3 = new TextView(mContext);
        d3.setHeight(1);
        d3.setBackground(
                Util.getDrawable(R.drawable.divider_cut_chose));

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        linearLayout.addView(custom, params);
        linearLayout.addView(d1);
        linearLayout.addView(d3);
        linearLayout.addView(d2);
        linearLayout.addView(free, params);

        PtuUtil.setPopWindow_for3LevelFunction(popWindow, view, linearLayout);

    }

    private void showFixRatioWindow(View view) {
        final PopupWindow popupWindow = ThreeLevelToolUtil.getPopWindow_for3LevelFunction(mContext);
        FrameLayout choseRatioLayout = (FrameLayout) LayoutInflater.from(mContext).inflate(R.layout.cut_chose_ratio, null);
        ListView listView = choseRatioLayout.findViewById(R.id.cut_choose_ratio_list);
        listView.setAdapter(new ChoseRatioAdapter(mContext, RATIO_NAMES, view.getWidth()));
        listView.setOnItemClickListener((parent, view1, position, id) -> {
            InsertAd.onClickTarget(getActivity());
            if (position == RATIO_NAMES.length - 2) {
                userDefinedRatio();
            } else if (position == RATIO_NAMES.length - 1) {
                cutView.resetShow();
            } else
                cutView.setFixedRatio(RATIOS[position]);
            popupWindow.dismiss();
        });
        PtuUtil.setPopWindow_for3LevelFunction(popupWindow, view, choseRatioLayout);
    }

    private void showFixFlipWindow(View view) {
        final int pad = 10;
        final PopupWindow popWindow = ThreeLevelToolUtil.getPopWindow_for3LevelFunction(mContext);

        LinearLayout linearLayout = new LinearLayout(mContext);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setBackgroundColor(Color.WHITE);
        linearLayout.setPadding(0, pad, 0, pad);

        final TextView vertical = createItem(pad);
        vertical.setText("垂直");
        vertical.setOnClickListener(v -> {
            vertical.setTextColor(Util.getColor(R.color.text_checked_color));
            cutView.reverse(0);
            popWindow.dismiss();
        });

        final TextView horizontal = createItem(pad);
        horizontal.setText("水平");
        horizontal.setOnClickListener(v -> {
            horizontal.setTextColor(Util.getColor(R.color.text_checked_color));
            cutView.reverse(1);
            popWindow.dismiss();
        });
        TextView d1 = new TextView(mContext);
        d1.setHeight(pad);
        TextView d2 = new TextView(mContext);
        d2.setHeight(pad);
        TextView d3 = new TextView(mContext);
        d3.setHeight(1);
        d3.setBackground(
                Util.getDrawable(R.drawable.divider_cut_chose));


        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        linearLayout.addView(vertical, params);
        linearLayout.addView(d1);
        linearLayout.addView(d3);
        linearLayout.addView(d2);
        linearLayout.addView(horizontal, params);

        PtuUtil.setPopWindow_for3LevelFunction(popWindow, view, linearLayout);
    }
}
