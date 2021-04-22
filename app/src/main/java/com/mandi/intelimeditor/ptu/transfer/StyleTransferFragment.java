package com.mandi.intelimeditor.ptu.transfer;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.jaygoo.widget.OnRangeChangedListener;
import com.jaygoo.widget.RangeSeekBar;
import com.mandi.intelimeditor.R;
import com.mandi.intelimeditor.ad.tencentAD.InsertAd;
import com.mandi.intelimeditor.bean.FunctionInfoBean;
import com.mandi.intelimeditor.common.CommonConstant;
import com.mandi.intelimeditor.common.Constants.EventBusConstants;
import com.mandi.intelimeditor.common.RcvItemClickListener1;
import com.mandi.intelimeditor.common.appInfo.IntelImEditApplication;
import com.mandi.intelimeditor.common.dataAndLogic.AllData;
import com.mandi.intelimeditor.common.dataAndLogic.MyDatabase;
import com.mandi.intelimeditor.common.util.BitmapUtil;
import com.mandi.intelimeditor.common.util.FileTool;
import com.mandi.intelimeditor.common.util.LogUtil;
import com.mandi.intelimeditor.common.util.SimpleObserver;
import com.mandi.intelimeditor.common.util.ToastUtils;
import com.mandi.intelimeditor.common.util.Util;
import com.mandi.intelimeditor.common.util.WrapContentGridLayoutManager;
import com.mandi.intelimeditor.common.view.PtuConstraintLayout;
import com.mandi.intelimeditor.home.HomeActivity;
import com.mandi.intelimeditor.ptu.BasePtuFragment;
import com.mandi.intelimeditor.ptu.PTuActivityInterface;
import com.mandi.intelimeditor.ptu.PtuActivity;
import com.mandi.intelimeditor.ptu.PtuUtil;
import com.mandi.intelimeditor.ptu.RepealRedoListener;
import com.mandi.intelimeditor.ptu.common.PTuUIUtil;
import com.mandi.intelimeditor.ptu.common.PtuBaseChooser;
import com.mandi.intelimeditor.ptu.gif.GifFrame;
import com.mandi.intelimeditor.ptu.repealRedo.CutStepData;
import com.mandi.intelimeditor.ptu.repealRedo.StepData;
import com.mandi.intelimeditor.ptu.tietu.TietuSizeController;
import com.mandi.intelimeditor.ptu.tietu.onlineTietu.PicResource;
import com.mandi.intelimeditor.ptu.tietu.onlineTietu.PicResourceDownloader;
import com.mandi.intelimeditor.ptu.tietu.onlineTietu.TietuRecyclerAdapter;
import com.mandi.intelimeditor.ptu.view.PtuSeeView;
import com.mandi.intelimeditor.user.US;
import com.mandi.intelimeditor.user.useruse.FirstUseUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

public class StyleTransferFragment extends BasePtuFragment {
    public static final String ALL_STYLE = "all_style";
    public static final String ALL_PIC = "all_pic";
    private String TAG = "DrawFragment";
    public static final int EDIT_MODE = PtuUtil.EDIT_DRAW;
    private Context mContext;

    private PTuActivityInterface pTuActivityInterface;
    private PtuSeeView ptuSeeView;
    private RepealRedoListener repealRedoListener;
    private PtuBaseChooser ptuBaseChooser;
    private RecyclerView chooseRcv;
    private boolean isChooseStyle = true;
    private TietuRecyclerAdapter chooseListAdapter;
    private boolean isFirstShowChooseRcv;

    @Override
    public void setPTuActivityInterface(PTuActivityInterface ptuActivity) {
        this.pTuActivityInterface = ptuActivity;
        this.ptuSeeView = ptuActivity.getPtuSeeView();
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
        pFunctionList.add(new FunctionInfoBean(R.string.choose_pic, R.drawable.icon_deformation, R.drawable.function_background_text_yellow, PtuUtil.EDIT_TRANSFER));
        pFunctionList.add(new FunctionInfoBean(R.string.choose_style, R.drawable.ic_gif, R.drawable.function_background_draw_pink, PtuUtil.EDIT_TRANSFER));
        return pFunctionList;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mContext = getActivity();
        FirstUseUtil.deformationGuide(getActivity());
        //底部的贴图列表
        chooseRcv = new RecyclerView(mContext);

        GridLayoutManager gridLayoutManager = new WrapContentGridLayoutManager(mContext, TietuRecyclerAdapter.DEFAULT_ROW_NUMBER,
                GridLayoutManager.HORIZONTAL, false);
        chooseRcv.setLayoutManager(gridLayoutManager);
        isChooseStyle = true;
        chooseListAdapter = new TietuRecyclerAdapter(mContext, true);
        chooseListAdapter.setOnItemClickListener(chooseRcvListener);
        prepareShowChooseRcv(view, isChooseStyle);
    }

    private RcvItemClickListener1 chooseRcvListener = new RcvItemClickListener1() {
        @Override
        public void onItemClick(RecyclerView.ViewHolder itemHolder, View view) {
            int position = itemHolder.getLayoutPosition();
            if (position < 0) return;
            if (position == 0) {
                US.putPTuTietuEvent(US.PTU_TIETU_MORE);
                Intent intent = new Intent(mContext, HomeActivity.class);
                intent.setAction(HomeActivity.INTENT_ACTION_ONLY_CHOSE_PIC);
                intent.addCategory(isChooseStyle ? HomeActivity.CHOOSE_PIC_CATEGORY_STYLE : HomeActivity.CHOOSE_PIC_CATEGORY_CONTENT);
                intent.putExtra(HomeActivity.INTENT_EXTRA_FRAGMENT_ID, HomeActivity.TEMPLATE_FRAG_ID);
                startActivityForResult(intent, isChooseStyle ? PtuActivity.REQUEST_CODE_CHOOSE_STYLE : PtuActivity.REQUEST_CODE_CHOOSE_CONTENT);
                return;
//                String thePath = Environment.getExternalStorageDirectory() + "/test1.jpg";
//                pTuActivityInterface.transfer(thePath, true);
            }
            if (chooseListAdapter != null) {
                PicResource oneTietu = chooseListAdapter.get(position).data;
                if (oneTietu != null && oneTietu.getUrl() != null) {
                    String url = oneTietu.getUrl().getUrl();
                    ViewGroup parent = (ViewGroup) chooseRcv.getParent();
                    FirstUseUtil.tietuGuide(mContext);
                    pTuActivityInterface.transfer(url, isChooseStyle);
                    MyDatabase.getInstance().updateMyTietu(url, System.currentTimeMillis());
                } else {
                    Log.e(this.getClass().getSimpleName(), "点击贴图后获取失败");
                }
            }
        }
    };

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
            case R.string.choose_pic:
                US.putPTuDeforEvent(US.PTU_DEFOR_EXAMPLE);
                isChooseStyle = false;
                prepareShowChooseRcv(view, isChooseStyle);
                break;
            case R.string.choose_style:
                US.putPTuDeforEvent(US.PTU_DEFOR_SIZE);
                isChooseStyle = true;
                prepareShowChooseRcv(view, isChooseStyle);
                break;
        }
    }


    private void prepareShowChooseRcv(View view, boolean isChooseStyle) {
        FirstUseUtil.myTietuGuide(mContext);
        ViewParent parent = view.getParent();
        while (parent != null && !(parent instanceof PtuConstraintLayout)) {
            parent = parent.getParent();
        }
        if (!(parent instanceof PtuConstraintLayout)) {
            return;
        }
        PtuConstraintLayout ptuFrameLayout = (PtuConstraintLayout) parent;

        if (ptuFrameLayout.indexOfChild(chooseRcv) == -1) { //
            ConstraintLayout.LayoutParams layoutParams = new ConstraintLayout.LayoutParams(
                    ConstraintLayout.LayoutParams.MATCH_CONSTRAINT, Util.dp2Px(100));
            layoutParams.leftToLeft = ConstraintLayout.LayoutParams.PARENT_ID;
            layoutParams.rightToRight = ConstraintLayout.LayoutParams.PARENT_ID;
            layoutParams.bottomToTop = R.id.fragment_main_function;
            layoutParams.setMargins(0, 0, 0, Util.dp2Px(4f));
            ptuFrameLayout.addView(chooseRcv, layoutParams);
            chooseRcv.setAdapter(chooseListAdapter);
        }
        if (isChooseStyle) {
            prepareShowStyle(view);
        } else {
            showStyleOrContenList(AllData.styleList);
        }
        if (isFirstShowChooseRcv) { // 本地贴图第一次加载显示不了，尝试多种办法不行，目前只能用这个
            view.post(() -> prepareShowStyle(view));
            view.post(() -> prepareShowStyle(view));
        }
        isFirstShowChooseRcv = false;
    }

    private void showStyleOrContenList(List<PicResource> styleList) {
        if (styleList == null) styleList = new ArrayList<>();
        chooseListAdapter.setList(styleList);
        chooseListAdapter.add(0, PicResource.path2PicResource(ALL_STYLE));
    }

    private void prepareShowStyle(View view) {
        Observable
                .create((ObservableOnSubscribe<List<PicResource>>) emitter -> {
                    PicResourceDownloader.queryPicResByCategory("", PicResource.category_style, emitter);
                })
                .subscribe(new SimpleObserver<List<PicResource>>() {

                    @Override
                    public void onError(Throwable throwable) {
                        if (isDetached()) {
                            return;
                        }
                        onNoStylePic();
                        LogUtil.e(throwable.getMessage());
                        showStyleOrContenList(null);
                    }

                    @Override
                    public void onNext(List<PicResource> tietumaterials) {
                        if (isDetached()) {
                            return;
                        }
                        int size = tietumaterials.size();
                        if (size == 0) {
                            onNoStylePic();
                            return;
                        }
                        Log.d("TAG", "onNext: 获取到的贴图数量" + size);
                        showStyleOrContenList(tietumaterials);
                    }
                });
    }


    private void onNoStylePic() {
        if (chooseListAdapter != null) { // 这个方法会异步回调，此时tietuListAdapter已经回收置空了，原来自己就没处理，GG
            chooseListAdapter.setList(new ArrayList<>());
            String msg;
            msg = mContext.getString(R.string.no_network_style_notice);
            PtuUtil.onNoPicResource(msg);
        }
    }

    private void showSizeWindow() {
        View contentView = LayoutInflater.from(mContext).inflate(R.layout.seek_bar_layout, null);
        RangeSeekBar radiusBar = ((RangeSeekBar) contentView.findViewById(R.id.seek_bar_popw));
        radiusBar.setRange(0, 100);
        radiusBar.setProgress(1);

        TextView valueTv = contentView.findViewById(R.id.seek_bar_value_tv);
        valueTv.setText(String.valueOf(1));

        radiusBar.setOnRangeChangedListener(new OnRangeChangedListener() {
            @Override
            public void onRangeChanged(RangeSeekBar view, float leftValue, float rightValue, boolean isFromUser) {
                int intValue = (int) leftValue;

                valueTv.setText(String.valueOf(intValue));
            }

            @Override
            public void onStartTrackingTouch(RangeSeekBar view, boolean isLeft) {

            }

            @Override
            public void onStopTrackingTouch(RangeSeekBar view, boolean isLeft) {
                radiusBar.postDelayed(() -> {

                }, 1000);
            }
        });

        PTuUIUtil.addPopOnFunctionLayout(mContext, contentView, pFunctionRcv);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, final Intent data) {
        if (requestCode == PtuActivity.REQUEST_CODE_CHOOSE_STYLE && data != null) {
            PicResource picRes = (PicResource) data.getSerializableExtra(HomeActivity.INTENT_EXTRA_CHOSEN_PIC_RES);
            pTuActivityInterface.transfer(picRes.getUrlString(), true);
            showStyleOrContenList(AllData.styleList);
        }

        if (requestCode == PtuActivity.REQUEST_CODE_CHOOSE_CONTENT && data != null) {
            PicResource picRes = (PicResource) data.getSerializableExtra(HomeActivity.INTENT_EXTRA_CHOSEN_PIC_RES);
            pTuActivityInterface.transfer(picRes.getUrlString(), false);
            showStyleOrContenList(AllData.contentList);
        }

        // 开通会员解锁, 开通成功后隐藏列表，用户重新点击，重新将列表加入adapter，一尺排除广告数据
        if (resultCode == CommonConstant.RESULT_CODE_OPEN_VIP_SUCCESS) {
            if (AllData.isVip) {
                // TODO: 2020/10/22 隐藏贴图列表对话框

            }
        }
        super.onActivityResult(requestCode, resultCode, data);
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

    private Bitmap getResultBm(int i) {
        return null;
    }

    /**
     * @param ratio
     */
    @Override
    public void generateResultDataInMain(float ratio) {
        //        用户滑动的时候，使用了仅替换方法，PTuSeeView中有些内容没有设置，这里设置

    }

    public static void addBigStep(StepData sd, PTuActivityInterface pTuActivityInterface) {
        PtuSeeView ptuSeeView = pTuActivityInterface.getPtuSeeView();
        Bitmap bm = BitmapUtil.getLosslessBitmap(sd.picPath);
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

        return false;
    }

    @Subscribe(threadMode = ThreadMode.MAIN, priority = 100)
    public void onEventMainThread(Integer event) {
        int isVisible = EventBusConstants.GIF_PLAY_CHOSEN.equals(event)
                ? View.VISIBLE : View.INVISIBLE;

    }

    public interface DeforActionListener {
        void deforComposeGif(List<GifFrame> bmList);
    }
}
