package com.mandi.intelimeditor.ptu.deformation;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.jaygoo.widget.OnRangeChangedListener;
import com.jaygoo.widget.RangeSeekBar;
import com.mandi.intelimeditor.ad.tencentAD.InsertAd;
import com.mandi.intelimeditor.common.Constants.EventBusConstants;
import com.mandi.intelimeditor.common.util.BitmapUtil;
import com.mandi.intelimeditor.common.util.FileTool;
import com.mandi.intelimeditor.common.util.ToastUtils;
import com.mandi.intelimeditor.common.util.Util;
import com.mandi.intelimeditor.common.view.DialogFactory;
import com.mandi.intelimeditor.ptu.BasePtuFragment;
import com.mandi.intelimeditor.ptu.PTuActivityInterface;
import com.mandi.intelimeditor.ptu.PtuUtil;
import com.mandi.intelimeditor.ptu.RepealRedoListener;
import com.mandi.intelimeditor.ptu.common.DrawController;
import com.mandi.intelimeditor.ptu.common.PTuUIUtil;
import com.mandi.intelimeditor.ptu.common.PtuBaseChooser;
import com.mandi.intelimeditor.ptu.gif.GifFrame;
import com.mandi.intelimeditor.ptu.repealRedo.CutStepData;
import com.mandi.intelimeditor.ptu.repealRedo.StepData;
import com.mandi.intelimeditor.ptu.tietu.onlineTietu.PicResource;
import com.mandi.intelimeditor.ptu.view.PtuFrameLayout;
import com.mandi.intelimeditor.ptu.view.PtuSeeView;
import com.mandi.intelimeditor.user.US;
import com.mandi.intelimeditor.user.useruse.FirstUseUtil;
import com.mandi.intelimeditor.bean.FunctionInfoBean;
import com.mandi.intelimeditor.R;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

/**
 * Created by Administrator on 2016/7/25.
 */
public class DeformationFragment extends BasePtuFragment {
    private String TAG = "DrawFragment";
    public static final int EDIT_MODE = PtuUtil.EDIT_DRAW;
    private Context mContext;

    private DeformationView deformationView;
    private PTuActivityInterface pTuActivityInterface;
    private PtuSeeView ptuSeeView;
    private RepealRedoListener repealRedoListener;
    private PtuBaseChooser ptuBaseChooser;
    private DeforActionListener deforActionListener;

    @Override
    public void setPTuActivityInterface(PTuActivityInterface ptuActivity) {
        this.pTuActivityInterface = ptuActivity;
        this.ptuSeeView = ptuActivity.getPtuSeeView();
        repealRedoListener = ptuActivity.getRepealRedoListener();
        if (deformationView != null)
            deformationView.setRrListener(repealRedoListener);
        repealRedoListener.canRepeal(false);
        repealRedoListener.canRedo(false);
    }


    public void initBeforeCreateView(DrawController secondFuncControl, PtuFrameLayout ptuFrame) {
        //         gif的二级按钮，已经不使用了
        if (pTuActivityInterface.getGifManager() != null) {
            FirstUseUtil.drawGifSecondarySureGuide((FragmentActivity) pTuActivityInterface);
        }
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();
        EventBus.getDefault().register(this);
    }

    /**
     * 当前布局
     */
    @Override
    public int getLayoutResId() {
        return R.layout.fragment_first_function_normal;
    }

    @Override
    public List<FunctionInfoBean> getFunctionList() {
        pFunctionList.clear();
        pFunctionList.add(new FunctionInfoBean(R.string.example, R.drawable.icon_deformation, PtuUtil.EDIT_CUT));
        pFunctionList.add(new FunctionInfoBean(R.string.size, R.mipmap.fixed_size, PtuUtil.EDIT_CUT));
        pFunctionList.add(new FunctionInfoBean(R.string.compose_gif, R.drawable.ic_gif, PtuUtil.EDIT_CUT));
        return super.getFunctionList();
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mContext = getActivity();
        FirstUseUtil.deformationGuide(getActivity());
        showExampleList();
        //        deformationView.testDeformation();
    }

    @Override
    public void initData() {
        super.initData();
    }

    @Override
    public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
        super.onItemClick(adapter, view, position);
        InsertAd.onClickTarget(getActivity());
        pFunctionAdapter.updateSelectIndex(position);
        switch (pFunctionList.get(position).getTitleResId()) {
            case R.string.example:
                US.putPTuDeforEvent(US.PTU_DEFOR_EXAMPLE);
                showExampleList();
                break;
            case R.string.size:
                US.putPTuDeforEvent(US.PTU_DEFOR_SIZE);
                showSizeWindow();
                break;
            case R.string.compose_gif:
                US.putPTuDeforEvent(US.PTU_DEFOR_TO_GIF);
                composeGif();
                break;
        }
    }

    private void composeGif() {
        //        if (!AllData.hasReadConfig.hasRead_deformation2Gif()) {
        //            final FirstUseDialog firstUseDialog = new FirstUseDialog(getActivity());
        //            firstUseDialog.createDialog(null,
        //                    "将合成gif动图, 无法撤销",
        //                    () -> AllData.hasReadConfig.put_deformation2Gif(true));
        //            return;
        //        }
        if (!deformationView.hasChange()) {
            ToastUtils.show("没有操作，先滑几下变形再试吧");
            return;
        }
        DialogFactory.noTitle(getActivity(), "将合成gif动图, 无法撤销", "合成", "取消", new AlertDialog.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                Bitmap bm1 = deformationView.getOriginalBm(), bm2 = deformationView.getResultBm();
                deforActionListener.deforComposeGif(Arrays.asList(new GifFrame(bm1, 800), new GifFrame(bm2, 500)));
            }
        });

    }

    private void showExampleList() {
        if (ptuBaseChooser == null) {
            ptuBaseChooser = new PtuBaseChooser(mContext, this,
                    pTuActivityInterface, Collections.singletonList("撕图"));
            ptuBaseChooser.setIsUpdateHeat(false);
            ptuBaseChooser.setShowMoreBtn(false);
            ptuBaseChooser.setChooseBgAuto(false);
            ptuBaseChooser.setSecondClass(PicResource.SECOND_CLASS_DEFORMATION);
            ptuBaseChooser.setOnItemClickListener(new PtuBaseChooser.ItemClickListener() {
                @Override
                public void onClickItem(PicResource picRes) {
                    ToastUtils.show(picRes.getTag());
                }
            });
        }
        ptuBaseChooser.show();
    }

    private void showSizeWindow() {
        View contentView = LayoutInflater.from(mContext).inflate(R.layout.seek_bar_layout, null);
        RangeSeekBar radiusBar = ((RangeSeekBar) contentView.findViewById(R.id.seek_bar_popw));
        radiusBar.setRange(0, deformationView.getWidth() / 3f);
        radiusBar.setProgress(deformationView.getDeformationRadius());

        TextView valueTv = contentView.findViewById(R.id.seek_bar_value_tv);
        valueTv.setText(String.valueOf(deformationView.getDeformationRadius()));

        radiusBar.setOnRangeChangedListener(new OnRangeChangedListener() {
            @Override
            public void onRangeChanged(RangeSeekBar view, float leftValue, float rightValue, boolean isFromUser) {
                int intValue = (int) leftValue;
                deformationView.setDeformationRadius(intValue);
                valueTv.setText(String.valueOf(intValue));
            }

            @Override
            public void onStartTrackingTouch(RangeSeekBar view, boolean isLeft) {

            }

            @Override
            public void onStopTrackingTouch(RangeSeekBar view, boolean isLeft) {
                radiusBar.postDelayed(() -> {
                    deformationView.setShowIndicator(false);
                }, 1000);
            }
        });

        PTuUIUtil.addPopOnFunctionLayout(mContext, contentView, pFunctionRcv);
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (ptuSeeView != null) {
            ptuSeeView.setCanDoubleClick(false);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (ptuSeeView != null) {
            ptuSeeView.setCanDoubleClick(true);
        }
        // Fragment动画导致子View还没清除就添加出错的问题，
        // 这是一种处理方式，但是退出动画没了，如果前面一种处理方式不行，再在用这种
        /*if (rootView != null) {
            ViewGroup parentView = (ViewGroup) rootView.getParent();
            if (parentView != null) {
                parentView.removeView(rootView);
            }
        }*/
        EventBus.getDefault().unregister(this);
    }

    public View createDeformationView(Context context, PtuSeeView ptuSeeView) {
        deformationView = new DeformationView(context, ptuSeeView);
        if (repealRedoListener != null) {
            deformationView.setRrListener(repealRedoListener);
        }
        return deformationView;
    }

    @Override
    public void smallRepeal() {
        deformationView.repeal();
    }

    @Override
    public void smallRedo() {
        deformationView.redo();
    }


    public Bitmap getResultBm(float ratio) {
        return deformationView.getResultBm();
    }

    @Override
    public StepData getResultDataAndDraw(float ratio) {
        //获取并保存数据
        Bitmap resultBm = getResultBm(1);
        if (resultBm == null) return null; // 没有操作
        StepData csd = new CutStepData(PtuUtil.EDIT_DEFORMATION);
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
        return csd;
    }

    /**
     * @param ratio
     */
    @Override
    public void generateResultDataInMain(float ratio) {
        //        用户滑动的时候，使用了仅替换方法，PTuSeeView中有些内容没有设置，这里设置
        ptuSeeView.replaceSourceBm(deformationView.getResultBm());
    }

    public static void addBigStep(StepData sd, PTuActivityInterface pTuActivityInterface) {
        PtuSeeView ptuSeeView = pTuActivityInterface.getPtuSeeView();
        Bitmap bm = BitmapUtil.decodeLosslessBitmap(sd.picPath);
        ptuSeeView.post(() -> ptuSeeView.replaceSourceBm(bm));
    }

    @Override
    public void releaseResource() {
        EventBus.getDefault().unregister(this);
    }

    @Override
    public int getEditMode() {
        return EDIT_MODE;
    }

    @Override
    public boolean onSure() {
        return false;
    }

    @Override
    public boolean onBackPressed(boolean isFromKey) {
        // 修改较多，防止误点离开，全面屏手势容易和绘图操作混淆
        // 次数多，且不是第二次点击，就退出
        if (isFromKey && deformationView.getOperationNumber() > 10
                && !Util.DoubleClick.isDoubleClick(1500)) {
            ToastUtils.show(R.string.leave_from_much_edit);
            return true;
        }
        ptuSeeView.replaceSourceBm(deformationView.getOriginalBm());
        return false;
    }

    @Subscribe(threadMode = ThreadMode.MAIN, priority = 100)
    public void onEventMainThread(Integer event) {
        int isVisible = EventBusConstants.GIF_PLAY_CHOSEN.equals(event)
                ? View.VISIBLE : View.INVISIBLE;
        deformationView.setVisibility(isVisible);
    }

    public void setDeforActionListener(DeforActionListener deforActionListener) {
        this.deforActionListener = deforActionListener;
    }

    public interface DeforActionListener {
        void deforComposeGif(List<GifFrame> bmList);
    }
}
