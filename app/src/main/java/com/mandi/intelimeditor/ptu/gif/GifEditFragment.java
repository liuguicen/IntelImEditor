package com.mandi.intelimeditor.ptu.gif;

import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.jaygoo.widget.OnRangeChangedListener;
import com.jaygoo.widget.RangeSeekBar;

import com.mandi.intelimeditor.common.util.LogUtil;
import com.mandi.intelimeditor.common.util.ToastUtils;
import com.mandi.intelimeditor.common.view.DialogFactory;
import com.mandi.intelimeditor.home.HomeActivity;
import com.mandi.intelimeditor.ptu.BasePtuFragment;
import com.mandi.intelimeditor.ptu.PTuActivityInterface;
import com.mandi.intelimeditor.ptu.PtuActivity;
import com.mandi.intelimeditor.ptu.PtuUtil;
import com.mandi.intelimeditor.ptu.RepealRedoListener;
import com.mandi.intelimeditor.ptu.common.PTuUIUtil;
import com.mandi.intelimeditor.ptu.imageProcessing.MathUtil;
import com.mandi.intelimeditor.ptu.repealRedo.StepData;
import com.mandi.intelimeditor.ptu.repealRedo.TextStepData;
import com.mandi.intelimeditor.ptu.tietu.TietuFragment;
import com.mandi.intelimeditor.ptu.tietu.onlineTietu.PicResource;
import com.mandi.intelimeditor.ptu.view.PtuSeeView;
import com.mandi.intelimeditor.user.US;
import com.mandi.intelimeditor.bean.FunctionInfoBean;
import com.mandi.intelimeditor.dialog.UnlockDialog;
import com.mandi.intelimeditor.R;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;
import java.util.Locale;


/**
 * 添加文字功能的fragment
 * Created by Administrator on 2016/5/1.
 */
public class GifEditFragment extends BasePtuFragment {
    private String TAG = "TextFragment";
    RepealRedoListener repealRedoListener;
    PtuSeeView ptuSeeView;
    private PTuActivityInterface pTuActivityInterface;
    private GifManager gifManager;

    @Override
    public void onResume() {

        super.onResume();
    }

    @Override
    public int getEditMode() {
        return PtuUtil.EDIT_TEXT;
    }

    @Override
    public int getLayoutResId() {
        return R.layout.fragment_first_function_normal;
    }

    @Override
    public List<FunctionInfoBean> getFunctionList() {
        //设置底部图标数据
        pFunctionList.clear();
        pFunctionList.add(new FunctionInfoBean(R.string.speed, R.drawable.ic_speed, R.drawable.function_background_text_yellow, PtuUtil.EDIT_GIF));
        pFunctionList.add(new FunctionInfoBean(R.string.add_pic, R.drawable.ic_add, R.drawable.function_background_text_yellow, PtuUtil.EDIT_GIF));
        pFunctionList.add(new FunctionInfoBean(R.string.del_pic, R.drawable.ic_remove, R.drawable.function_background_text_yellow, PtuUtil.EDIT_GIF));
        return pFunctionList;
    }

    @Override
    public void setPTuActivityInterface(PTuActivityInterface ptuActivity) {
        this.pTuActivityInterface = ptuActivity;
        this.ptuSeeView = ptuActivity.getPtuSeeView();
        this.repealRedoListener = ptuActivity.getRepealRedoListener();
        gifManager = ptuActivity.getGifManager();
        repealRedoListener.canRepeal(false);
        repealRedoListener.canRedo(false);
    }

    @Override
    public boolean onSure() {
        return false;
    }

    /**
     * 获取addTextFragment上的view组件
     */
    @Override
    public void initView() {
        super.initView();
        EventBus.getDefault().register(this);
    }


    @Override
    public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
        super.onItemClick(adapter, view, position);
        if (pFunctionList.get(position).isLocked()) {
            UnlockDialog unlockDialog = UnlockDialog.Companion.newInstance();
            unlockDialog.setUnlockListener(result -> {
                LogUtil.d(TAG, "video result $result");
                if (result) {
                    pFunctionList.get(position).setLocked(false);
                    pFunctionAdapter.notifyItemChanged(position);
                }
                return null;
            });
            unlockDialog.showIt(getActivity());
            return;
        }
        int id = pFunctionList.get(position).getTitleResId();
        switch (id) {
            case R.string.speed:
                US.putGifEvent(US.GIF_ADJUST_SPEED);
                adjustSpeed();
                break;
            case R.string.add_pic:
                US.putGifEvent(US.GIF_ADD_PIC);
                addFrame();
                break;
            case R.string.del_pic:
                US.putGifEvent(US.GIF_DEL_PIC);
                delFrame();
                break;
            default:
                break;
        }
    }

    private void delFrame() {
        DialogFactory.noTitle(getActivity(), "将要删除选中的帧", "删除", "取消", (dialog, which) -> {
            gifManager.delChosenFrame();
        });
    }

    private void addFrame() {
        Intent intent = new Intent(mContext, HomeActivity.class);
        intent.setAction(HomeActivity.PTU_ACTION_CHOOSE_TIETU);
        intent.putExtra(HomeActivity.INTENT_EXTRA_FRAGMENT_ID, HomeActivity.LOCAL_FRAG_ID);
        startActivityForResult(intent, PtuActivity.REQUEST_CODE_CHOOSE_TIETU);
    }

    private void adjustSpeed() {
        View contentView = LayoutInflater.from(mContext).inflate(R.layout.seek_bar_layout, null);
        RangeSeekBar radiusBar = ((RangeSeekBar) contentView.findViewById(R.id.seek_bar_popw));
        radiusBar.setRange(0, 100);
        float value = gifManager.getMeanDaley();
        Log.d(TAG, "平均帧时延 = " + value);
        float progress = delay2Progress(value) * 100;
        radiusBar.setProgress(progress);
        TextView valueTv = contentView.findViewById(R.id.seek_bar_value_tv);
        valueTv.setText(String.valueOf((int) progress));

        radiusBar.setOnRangeChangedListener(new OnRangeChangedListener() {
//            float leftValue =

            @Override
            public void onRangeChanged(RangeSeekBar view, float leftValue, float rightValue, boolean isFromUser) {
//                this.leftValue = leftValue;
//                float leftValue = view.getLeftSeekBar().getProgress();
                // 手指不太起来，下面的代码也不会奏效，待处理，现在写到了下面的onStopTrackingTouch方法中
//                if (LogUtil.debugGif) {
//                    Log.d(TAG, "progress =  " + leftValue);
//                }
//                float delay = progress2Daley(leftValue / 100);
//                if (delay > 1.2 * 1000) {
//                    ToastUtils.show(String.format(Locale.CHINA, "进入极慢模式%.0fS/帧 ", delay / 1000));
//                }
//                gifManager.adjustDaley(delay);
//                valueTv.setText(String.valueOf((int) leftValue));
            }

            @Override
            public void onStartTrackingTouch(RangeSeekBar view, boolean isLeft) {
            }

            @Override
            public void onStopTrackingTouch(RangeSeekBar view, boolean isLeft) {
                float leftValue = view.getLeftSeekBar().getProgress();
                if (LogUtil.debugGif) {
                    Log.d(TAG, "progress =  " + leftValue);
                }
                float delay = progress2Daley(leftValue / 100);
                if (delay > 1.2 * 1000) {
                    ToastUtils.show(String.format(Locale.CHINA, "进入极慢模式%.0fS/帧 ", delay / 1000));
                }
                gifManager.adjustDaley(delay);
                valueTv.setText(String.valueOf((int) leftValue));
            }
        });

        PTuUIUtil.addPopOnFunctionLayout(mContext, contentView, pFunctionRcv);

    }

    float[] needY = new float[]{20 * 1000f, 3f * 1000f, 1000f, 120f, 50f, 5f};
    float[] seg = new float[]{0, 0.1f, 0.2f, 0.5f, 0.8f, 1f}; //归一化
    float[] a = calcua(needY, seg);


    /**
     * progress是0-100
     * 照顾用户习惯，右边表示gif播放快，对应的时延短
     * 另外： 如果时延是0.01-100，如果用户拖动距离与时延变化采用关系的话，时延小的时候，如0.01-0.1，用户立即就拖过去了，不行， 所以这里采用分段的线性变化
     * 画个图分析下，横坐标拖动距离，纵坐时延变化，分段线性函数,知道分段的位置和分段处y的值，得出斜率，写出表达式然后代码
     * <pre class="prettyprint">
     * 斜率 a0 = (needy1 - needy0) / (seg1 - seg0) , ...
     *      a0 * (x -seg0) + needy0, 0 < x < seg1
     * y =  a1 * (x - seg1) + needy1, seg1 < x < seg2
     *      ....
     *
     *      (y - needy0) / a0 + seg0,  y in (needy0, needy1)
     * x =  (y - needy1) / a1 + seg1,  y in (needy1, needy2)
     *      ....
     * </pre>
     */
    private float delay2Progress(float value) {
        for (int i = 0; i < needY.length - 1; i++) {
            if (MathUtil.isInRange(value, needY[i], needY[i + 1])) {
                return seg[i] + (value - needY[i]) / a[i];
            }
        }
        return seg[seg.length - 1];
    }


    /**
     * @param progress 比例，归一化
     * @see #delay2Progress(float)
     */
    private float progress2Daley(float progress) {
        for (int i = 0; i < seg.length + 1; i++) {
            if (MathUtil.isInRange(progress, seg[i], seg[i + 1])) {
                return needY[i] + a[i] * (progress - seg[i]);
            }
        }
        return needY[needY.length - 1];
    }

    private float[] calcua(float[] needY, float[] seg) {
        float[] a = new float[needY.length];
        for (int i = 0; i < needY.length - 1; i++) {
            a[i] = (needY[i + 1] - needY[i]) / (seg[i + 1] - seg[i]);
        }
        return a;
    }

    public void releaseResource() {
        EventBus.getDefault().unregister(this);
    }

    public void setRepealRedoListener(RepealRedoListener repealRedoListener) {
        this.repealRedoListener = repealRedoListener;
        repealRedoListener.canRepeal(false);
        repealRedoListener.canRedo(false);
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        pFunctionAdapter = null; // 这个中途会变化，如果不置空，第二次进入不会重新创建，导出出错
        EventBus.getDefault().unregister(this); // 再反注册一次,防止没有反注册到
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void smallRepeal() {
    }

    @Override
    public void smallRedo() {
    }

    @Override
    public void generateResultDataInMain(float ratio) {
    }

    @Override
    public StepData getResultDataAndDraw(float ratio) {
        return null;
    }

    public static void addBigStep(StepData sd) {

    }

    /**
     * 已经存在bitmap的情况下，更快速的添加
     *
     * @param textAddBm 要添加的图片
     */
    private void addBigStep(TextStepData tsd, Bitmap textAddBm) {

    }

    @Subscribe(threadMode = ThreadMode.MAIN, priority = 100)
    public void onEventMainThread(Integer event) {

    }

    public boolean onBackPressed(boolean isFromKey) {
        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, final Intent data) {
        if (requestCode == PtuActivity.REQUEST_CODE_CHOOSE_TIETU && data != null) {
            PicResource picResource = (PicResource) data.getSerializableExtra(TietuFragment.INTENT_EXTRA_CHOSE_TIETU_RES);
            if (picResource == null) return;
            gifManager.addFrameByUrl(picResource.getUrlString());
        }
    }
}