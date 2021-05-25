package com.mandi.intelimeditor.ptu.transfer;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.mandi.intelimeditor.R;
import com.mandi.intelimeditor.bean.FunctionInfoBean;
import com.mandi.intelimeditor.common.CommonConstant;
import com.mandi.intelimeditor.common.appInfo.IntelImEditApplication;
import com.mandi.intelimeditor.common.dataAndLogic.AllData;
import com.mandi.intelimeditor.common.dataAndLogic.MyDatabase;
import com.mandi.intelimeditor.common.dataAndLogic.SPUtil;
import com.mandi.intelimeditor.common.util.BitmapUtil;
import com.mandi.intelimeditor.common.util.FileTool;
import com.mandi.intelimeditor.common.util.LogUtil;
import com.mandi.intelimeditor.common.util.SimpleObserver;
import com.mandi.intelimeditor.common.util.ToastUtils;
import com.mandi.intelimeditor.common.util.Util;
import com.mandi.intelimeditor.common.util.WrapContentGridLayoutManager;
import com.mandi.intelimeditor.common.view.PtuConstraintLayout;
import com.mandi.intelimeditor.common.view.ImageDecoration;
import com.mandi.intelimeditor.home.HomeActivity;
import com.mandi.intelimeditor.home.view.BottomFunctionView;
import com.mandi.intelimeditor.ptu.BasePtuFragment;
import com.mandi.intelimeditor.ptu.PTuActivityInterface;
import com.mandi.intelimeditor.ptu.PtuActivity;
import com.mandi.intelimeditor.ptu.PtuUtil;
import com.mandi.intelimeditor.ptu.RepealRedoListener;
import com.mandi.intelimeditor.ptu.common.PtuBaseChooser;
import com.mandi.intelimeditor.ptu.common.TransferController;
import com.mandi.intelimeditor.ptu.gif.GifFrame;
import com.mandi.intelimeditor.ptu.repealRedo.CutStepData;
import com.mandi.intelimeditor.ptu.repealRedo.StepData;
import com.mandi.intelimeditor.ptu.threeLevelFunction.ThreeLevelToolUtil;
import com.mandi.intelimeditor.ptu.tietu.onlineTietu.ImageMaterialAdapter;
import com.mandi.intelimeditor.ptu.tietu.onlineTietu.PicResource;
import com.mandi.intelimeditor.ptu.tietu.onlineTietu.TietuRecyclerAdapter;
import com.mandi.intelimeditor.ptu.view.PtuSeeView;
import com.mandi.intelimeditor.user.US;
import com.mandi.intelimeditor.user.useruse.FirstUseUtil;

import org.greenrobot.eventbus.EventBus;
import org.jetbrains.annotations.NotNull;
import org.pytorch.Tensor;
import org.pytorch.torchvision.TensorImageUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Emitter;
import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

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
    private boolean isChooseStyleMode = true;
    private ImageMaterialAdapter chooseListAdapter;
    private boolean isFirstShowChooseRcv;
    private BottomFunctionView choosePicBtn, chooseStyleBtn;
    static final int bottonWidth = Util.dp2Px(20);
    private TransferController transferController;
    public static final String MODEL_ADAIN = "adain";
    public static final String MODEL_GOOGLE = "google";
    private String model = MODEL_GOOGLE;

    private FloatBuffer contentBuffer;
    private FloatBuffer styleBuffer;
    private Tensor styleTensor;
    private Tensor contentFeature;
    private Bitmap styleBm;
    private Tensor styleFeature;
    private boolean isStyle;

    @Override
    public void setPTuActivityInterface(PTuActivityInterface ptuActivity) {
        this.pTuActivityInterface = ptuActivity;
        this.ptuSeeView = ptuActivity.getPtuSeeView();
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();
    }

    /**
     * 当前布局
     */
    @Override
    public int getLayoutResId() {
        return R.layout.fragment_transfer;
    }

    @Override
    public List<FunctionInfoBean> getFunctionList() {
        pFunctionList.clear();
        return pFunctionList;
    }

    @Override
    public void initBottomFun() {

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
        isChooseStyleMode = true;
        if (transferController != null && transferController.contentBm == null) {
            isChooseStyleMode = false;
        }
        chooseListAdapter = new ImageMaterialAdapter();
        chooseListAdapter.setOnItemClickListener(chooseRcvListener);
        prepareShowChooseRcv(view, isChooseStyleMode);

        chooseStyleBtn = rootView.findViewById(R.id.choose_style);
        choosePicBtn = rootView.findViewById(R.id.choose_content);
        choosePicBtn.setOnClickListener(v -> {
            if (!isChooseStyleMode && chooseRcv.getParent() != null) { // 已经选择了内容，那么进入全部图片界面
                chooseFromAllPic();
            } else {
                US.putPTuDeforEvent(US.PTU_DEFOR_EXAMPLE);
                isChooseStyleMode = false;
                prepareShowChooseRcv(view, isChooseStyleMode);
            }
        });
        BottomFunctionView chooseModel = rootView.findViewById(R.id.choose_model);
        chooseModel.setOnClickListener(v -> {
            showChooseModel(v);
        });

        chooseStyleBtn.setOnClickListener(v -> {
            if (isChooseStyleMode && chooseRcv.getParent() != null) {
                chooseFromAllPic();
            } else {
                US.putPTuDeforEvent(US.PTU_DEFOR_SIZE);
                isChooseStyleMode = true;
                prepareShowChooseRcv(view, isChooseStyleMode);
            }
        });
        if (transferController != null) {
            onChosenBm(transferController.contentBm, transferController.styleBm);
        }
        rootView.findViewById(R.id.go_ptu).setOnClickListener(v -> {
            if (chooseRcv.getParent() != null) {
                ((ViewGroup) chooseRcv.getParent()).removeView(chooseRcv);
            }
            pTuActivityInterface.switchFragment(PtuUtil.EDIT_MAIN, null);
        });
    }

    private void showChooseModel(View view) {
        final int pad = 10;
        final PopupWindow popWindow = ThreeLevelToolUtil.getPopWindow_for3LevelFunction(mContext);
        LinearLayout linearLayout = new LinearLayout(mContext);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setBackgroundColor(Color.WHITE);
        linearLayout.setPadding(0, pad, 0, pad);
        List<TextView> modelTxtList = new ArrayList<>();
        final TextView custom = createItem(pad, MODEL_ADAIN.equals(model), modelTxtList);
        custom.setText("adain");
        custom.setOnClickListener(v -> {
            model = MODEL_ADAIN;
            clearModelChoose(modelTxtList);
            custom.setTextColor(Util.getColor(R.color.text_checked_color));
            transfer(styleBm, true);
        });
        final TextView free = createItem(pad, MODEL_GOOGLE.equals(model), modelTxtList);
        free.setText("google");
        free.setOnClickListener(v -> {
            model = MODEL_GOOGLE;
            clearModelChoose(modelTxtList);
            free.setTextColor(Util.getColor(R.color.text_checked_color));
            transfer(styleBm, true);
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

    private void clearModelChoose(List<TextView> modelTxtList) {
        for (TextView textView : modelTxtList) {
            textView.setTextColor(Util.getColor(R.color.text_deep_black));
        }
    }

    private void userDefinedSize() {

    }

    private TextView createItem(final int pad, boolean isChoose, List<TextView> modelTxtList) {
        TextView tv = new TextView(mContext);
        tv.setGravity(Gravity.CENTER);
        tv.setTextSize(20);
        tv.setTextColor(Util.getColor(isChoose ? R.color.text_checked_color : R.color.text_deep_black));
        modelTxtList.add(tv);
        return tv;
    }

    private OnItemClickListener chooseRcvListener = new OnItemClickListener() {
        @Override
        public void onItemClick(@NonNull @NotNull BaseQuickAdapter<?, ?> adapter, @NonNull @NotNull View view, int position) {
            if (position < 0) return;
            if (position == 0) {
                chooseFromAllPic();
                return;
            }
            if (chooseListAdapter != null) {
                PicResource oneTietu = chooseListAdapter.getData().get(position).data;
                if (oneTietu != null && oneTietu.getUrl() != null) {
                    String url = oneTietu.getUrl().getUrl();
                    ViewGroup parent = (ViewGroup) chooseRcv.getParent();
                    FirstUseUtil.tietuGuide(mContext);
                    transfer(url, isChooseStyleMode);
                    MyDatabase.getInstance().updateMyTietu(url, System.currentTimeMillis());
                } else {
                    Log.e(this.getClass().getSimpleName(), "点击贴图后获取失败");
                }
            }
        }
    };

    public void transfer(Object obj, boolean isStyle) {

        pTuActivityInterface.hidePtuNotice();
        pTuActivityInterface.showProgress(0);

        Observable.create((ObservableOnSubscribe<Bitmap>) emitter -> {
            if (LogUtil.debugStyleTransfer) {
                LogUtil.d(TAG + "开始风格迁移");
            }
            // 判断是否是url并解析成路径
            // 第一步，主要解析Bm
            // 将路径解析成Bm
            int decodeSize = isStyle ? (int) (AllData.globalSettings.maxSupportContentSize *
                    AllData.globalSettings.styleContentRatio)
                    : AllData.globalSettings.maxSupportContentSize;
            BitmapUtil.decodeFromObj(obj, emitter, decodeSize);
            if (LogUtil.debugStyleTransfer) {
                LogUtil.d(TAG + "风格迁移，解析bitmap完成");
            }
            emitter.onComplete();
        }).subscribeOn(Schedulers.io())
                .observeOn(Schedulers.computation())
                .map(bm -> {
                    // 更新UI进度和图片
                    getActivity().runOnUiThread(() -> {
                        pTuActivityInterface.showProgress(10);
                        onChosenBm(isStyle ? null : bm, isStyle ? bm : null);
                    });

                    // 第二步，使用合适的尺寸迁移图片
                    if (MODEL_GOOGLE.equals(model)) {
                        return realTransferTf(bm, isStyle);
                    } else {
                        return transferWithSuitSize(bm, isStyle);
                    }
                }).subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<Bitmap>() {
                    @Override
                    public void onNext(@io.reactivex.annotations.NonNull Bitmap bitmap) {
                        ptuSeeView.replaceSourceBm(bitmap);
                        pTuActivityInterface.dismissProgress();
                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                        ToastUtils.show(e.getMessage());
                        pTuActivityInterface.dismissProgress();
                    }

                    @Override
                    public void onComplete() {
                        super.onComplete();
                        pTuActivityInterface.dismissProgress();
                    }
                });

    }

    private Bitmap realTransferTf(@NotNull Bitmap bm, boolean isStyle) {
        if (isStyle) {
            styleBm = bm;
        } else {
            pTuActivityInterface.getRepealRedoRManager().setBaseBm(bm);
        }
        Bitmap contentBm = pTuActivityInterface.getRepealRedoRManager().getBaseBitmap();
        return StyleTransferTf.getInstance().transfer(contentBm, styleBm, getActivity());
    }

    @org.jetbrains.annotations.Nullable
    private Bitmap transferWithSuitSize(@NotNull Bitmap bm, boolean isStyle) {
        Bitmap rstBm = null;
        int testSize = 5;
        while (testSize > 0) { // 尝试找到合适的尺寸
            try {
                // 第二步，使用合适的尺寸迁移图片
                if (MODEL_GOOGLE.equals(model)) {
                    Log.d(TAG, "transferWithSuitSize: use google");
                    rstBm = realTransferTf(bm, isStyle);
                } else {
                    Log.d(TAG, "transferWithSuitSize: use adain");
                    rstBm = realTransfer(bm, isStyle);
                }
                getActivity().runOnUiThread(() -> pTuActivityInterface.showProgress(90));
                testSize = -1;
            } catch (Throwable e) {
                if (e instanceof OutOfMemoryError || e instanceof StackOverflowError) { // 尺寸太大，爆内存，主动调小
                    if (LogUtil.debugStyleTransfer) {
                        Log.d(TAG, String.format("尝试尺寸 %d 失败，剩余 %d 次", AllData.globalSettings.maxSupportContentSize, testSize - 1));
                    }
                    AllData.globalSettings.maxSupportContentSize *= 0.80f;
                    Bitmap contentBm = pTuActivityInterface.getRepealRedoRManager().getBaseBitmap();
                    double ratio = Math.sqrt(AllData.globalSettings.maxSupportContentSize * 1f / (contentBm.getWidth() * contentBm.getHeight()));
                    contentBm = Bitmap.createScaledBitmap(contentBm, (int) (ratio * contentBm.getWidth()),
                            (int) (ratio * contentBm.getHeight()), true);
                    pTuActivityInterface.getRepealRedoRManager().setBaseBm(contentBm);
                    contentFeature = null;
                    styleFeature = null;
                    testSize--;
                } else {
                    testSize = -1;
                }
                e.printStackTrace();
            }
        }
        // 第一次成功，放入合适的尺寸
        if (SPUtil.getContentMaxSupportBmSize() <= 0) {
            SPUtil.putContentMaxSupportBmSize(AllData.globalSettings.maxSupportContentSize);
            if (LogUtil.debugStyleTransfer) {
                Log.e(TAG, "放入风格尺寸，最大尺寸 = " + AllData.globalSettings.maxSupportContentSize);
            }
        }
        Log.e(TAG, "通过Adain和解码器");
        return rstBm;
    }


    private Bitmap realTransfer(@NotNull Bitmap bm, boolean isStyle) {
        Bitmap contentBm = pTuActivityInterface.getRepealRedoRManager().getBaseBitmap();
        StyleTransfer transfer = StyleTransfer.getInstance();
        Log.d(TAG, "realTransfer: 开始运行风格迁移算法");
        if (!isStyle) {
            // contentFeature = transfer.getVggFeature(bm);
            getActivity().runOnUiThread(() -> pTuActivityInterface.showProgress(33));
            if (LogUtil.debugStyleTransfer) {
                Log.e(TAG, "内容图片通过VGG完成, size = " + bm.getWidth() + " * " + bm.getHeight());
                // LogUtil.printMemoryInfo(TAG + "内容图片通过VGG完成", PtuActivity.this);
            }
            pTuActivityInterface.getRepealRedoRManager().setBaseBm(bm);
            // 风格特征不存在，或者风格特征大小不够
            if (styleBm != null && (styleFeature == null || styleBm.getByteCount() <
                    contentBm.getByteCount() * AllData.globalSettings.styleContentRatio)) {
                // styleFeature = transfer.getVggFeature(styleBm);
                if (LogUtil.debugStyleTransfer) {
                    Log.e(TAG, "风格图片通过vgg, size = " + styleBm.getWidth() + " * " + styleBm.getHeight());
                    // LogUtil.printMemoryInfo(TAG + "风格图片通过vgg完成", PtuActivity.this);
                }
                getActivity().runOnUiThread(() -> pTuActivityInterface.showProgress(66));
            }
        } else {
            // if (contentBm != null && contentFeature == null) {
            int cw = contentBm.getWidth();
            int ch = contentBm.getHeight();
            if (contentBuffer == null) {
                contentBuffer = ByteBuffer.allocateDirect(3 * cw * ch * 4)
                        .order(ByteOrder.nativeOrder())
                        .asFloatBuffer();
                Log.e(TAG, "realTransfer: 创建内容buffer完成");
            }
            Log.e(TAG, "内容bm放入floabuffer");
            StyleTransfer.bitmapToFloatBuffer(contentBm, 0, 0, cw, ch, TensorImageUtils.TORCHVISION_NORM_MEAN_RGB,
                    TensorImageUtils.TORCHVISION_NORM_STD_RGB, contentBuffer, 0);
            Log.e(TAG, "内容buffer创建tensor");
            Tensor contentTensor = Tensor.fromBlob(contentBuffer, new long[]{1, 3, ch, cw});
            Log.e(TAG, "内容buffer创建tensor完成");
            contentFeature = transfer.getVggFeature(contentTensor);
            if (LogUtil.debugStyleTransfer) {
                Log.e(TAG, "内容图片通过vgg, size = " + cw
                        + " * " + contentBm.getHeight());
                // LogUtil.printMemoryInfo(TAG + "内容图片通过vgg完成", PtuActivity.this);
            }
            getActivity().runOnUiThread(() -> pTuActivityInterface.showProgress(33));
            // }
            // 比例不对的
            styleBm = bm;
            if (styleBm.getByteCount() < contentBm.getByteCount() * AllData.globalSettings.styleContentRatio) {
                styleBm = Bitmap.createScaledBitmap(styleBm, styleBm.getWidth(), styleBm.getHeight(), true);
            }

            int sw = styleBm.getWidth();
            int sh = styleBm.getHeight();
            if (styleBuffer == null) {
                styleBuffer = ByteBuffer.allocateDirect(3 * sw * sh * 4)
                        .order(ByteOrder.nativeOrder())
                        .asFloatBuffer();
                Log.e(TAG, "realTransfer: 创建风格buffer");
            }
            Log.e(TAG, "风格bm放入floabuffer");
            StyleTransfer.bitmapToFloatBuffer(styleBm, 0, 0, sw, sh, TensorImageUtils.TORCHVISION_NORM_MEAN_RGB,
                    TensorImageUtils.TORCHVISION_NORM_STD_RGB, styleBuffer, 0);
            Log.e(TAG, "风格buffer创建tensor");
            Tensor styleTensor = Tensor.fromBlob(styleBuffer, new long[]{1, 3, sh, sw});
            Log.e(TAG, "风格buffer创建tensor完成");
            styleFeature = transfer.getVggFeature(styleTensor);

            if (LogUtil.debugStyleTransfer) {
                Log.e(TAG, "风格图片通过vgg, size = " + styleBm.getWidth() + " * " + styleBm.getHeight());
            }
            getActivity().runOnUiThread(() -> pTuActivityInterface.showProgress(66));
        }
        Bitmap res = transfer.transfer(contentFeature, styleFeature, 1f);
        // Buffer不能回收，出问题
        // contentBuffer.clear();
        // styleBuffer.clear();
        // contentBuffer = null;
        // styleBuffer = null;
        System.gc();
        if (LogUtil.debugStyleTransfer) {
            LogUtil.d(TAG + "风格迁移完成 ");
        }
        return res;
    }


    private void chooseFromAllPic() {
        US.putPTuTietuEvent(US.PTU_TIETU_MORE);
        Intent intent = new Intent(mContext, HomeActivity.class);
        intent.setAction(HomeActivity.INTENT_ACTION_ONLY_CHOSE_PIC);
        intent.addCategory(isChooseStyleMode ? HomeActivity.CHOOSE_PIC_CATEGORY_STYLE : HomeActivity.CHOOSE_PIC_CATEGORY_CONTENT);
        intent.putExtra(HomeActivity.INTENT_EXTRA_FRAGMENT_ID, isChooseStyleMode ? HomeActivity.TEMPLATE_FRAG_ID : HomeActivity.LOCAL_FRAG_ID);
        startActivityForResult(intent, isChooseStyleMode ? PtuActivity.REQUEST_CODE_CHOOSE_STYLE : PtuActivity.REQUEST_CODE_CHOOSE_CONTENT);
    }

    @Override
    public void initData() {
        super.initData();
    }

    public void onChosenBm(Bitmap contentBm, Bitmap styleBm) {
        Resources resources = IntelImEditApplication.appContext.getResources();
//        if (contentBm != null) {
//            chooseContenBtn.setBackground(new BitmapDrawable(resources, getCircleBitmap(contentBm)));
//        }
//        if (styleBm != null) {
//            chooseStyleBtn.setBackground(new BitmapDrawable(resources, getCircleBitmap(styleBm)));
//        }
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
            chooseRcv.setTag(PtuConstraintLayout.TAG_TIETU_RCV);
            ptuFrameLayout.addView(chooseRcv, layoutParams);
            if (chooseRcv.getItemDecorationCount() == 0) {
                chooseRcv.addItemDecoration(new ImageDecoration(getActivity()));
            }
            chooseRcv.setAdapter(chooseListAdapter);
        }
        if (isChooseStyle) {
            prepareShowStyleList(view);
        } else {
            prepareShowContentList();
        }
        if (isFirstShowChooseRcv) { // 本地贴图第一次加载显示不了，尝试多种办法不行，目前只能用这个
            view.post(() -> prepareShowStyleList(view));
            view.post(() -> prepareShowStyleList(view));
        }
        isFirstShowChooseRcv = false;
    }

    private void prepareShowContentList() {
        if (AllData.contentList.size() != 0) {
            showStyleOrContenList(AllData.contentList);
        } else { // 没有指定，显示最近图片列表
            AllData.queryLocalPicList(new Emitter<String>() {
                @Override
                public void onNext(@io.reactivex.annotations.NonNull String value) {
                    AllData.contentList = AllData.sMediaInfoScanner.convertRecentPath2PicResList();
                    showStyleOrContenList(AllData.contentList);
                }

                @Override
                public void onError(@io.reactivex.annotations.NonNull Throwable error) {

                }

                @Override
                public void onComplete() {

                }
            });
        }
    }

    private void prepareShowStyleList(View view) {
        AllData.queryAllPicRes(new Emitter<List<PicResource>>() {

            @Override
            public void onNext(List<PicResource> resList) {
                if (isDetached()) {
                    return;
                }
                int size = resList.size();
                if (size == 0) {
                    onNoStylePic();
                    return;
                }
                Log.d("TAG", "onNext: 获取到的贴图数量" + size);
                showStyleOrContenList(resList);
            }

            @Override
            public void onError(@NotNull Throwable throwable) {
                if (isDetached()) {
                    return;
                }
                onNoStylePic();
                LogUtil.e(throwable.getMessage());
                showStyleOrContenList(null);
            }

            @Override
            public void onComplete() {

            }
        });
    }

    private void showStyleOrContenList(List<PicResource> list) {
        if (list == null) list = new ArrayList<>();
        chooseListAdapter.setList(list);
        chooseListAdapter.add(0, PicResource.path2PicResource(ALL_STYLE));
    }

    private void onNoStylePic() {
        if (chooseListAdapter != null) { // 这个方法会异步回调，此时tietuListAdapter已经回收置空了，原来自己就没处理，GG
            chooseListAdapter.setList(new ArrayList<PicResource>());
            String msg;
            msg = mContext.getString(R.string.no_network_style_notice);
            PtuUtil.onNoPicResource(msg);
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, final Intent data) {
        if (requestCode == PtuActivity.REQUEST_CODE_CHOOSE_STYLE && data != null) {
            PicResource picRes = (PicResource) data.getSerializableExtra(HomeActivity.INTENT_EXTRA_CHOSEN_PIC_RES);
            transfer(picRes.getUrlString(), true);
            showStyleOrContenList(AllData.curStyleList);
        }

        if (requestCode == PtuActivity.REQUEST_CODE_CHOOSE_CONTENT && data != null) {
            PicResource picRes = (PicResource) data.getSerializableExtra(HomeActivity.INTENT_EXTRA_CHOSEN_PIC_RES);
            transfer(picRes.getUrlString(), false);
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

        return false;
    }

    public void initBeforeCreateView(TransferController transferController) {
        this.transferController = transferController;
    }

    public void setStyleBm(Bitmap styleBm) {
        this.styleBm = styleBm;
    }

    public interface DeforActionListener {
        void deforComposeGif(List<GifFrame> bmList);
    }

    /**
     * 获取圆角位图的方法
     *
     * @param bitmap 需要转化成圆角的位图
     * @return 处理后的圆角位图
     */
    private Bitmap getCircleBitmap(Bitmap bitmap) {
        Bitmap output = Bitmap.createBitmap(bottonWidth, bottonWidth, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        final int color = 0xff424242;
        final Rect dstRect = new Rect(0, 0, bottonWidth, bottonWidth);
        int w = bitmap.getWidth(), h = bitmap.getHeight();
        final Rect srcRect = new Rect(0, (h - w) / 2, w, (h - w) / 2 + w);
        if (bitmap.getWidth() > bitmap.getHeight()) {
            srcRect.set((w - h) / 2, 0, (w - h) / 2, h);
        }
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        int x = bottonWidth;
        //这是圆drawCircle
        canvas.drawCircle(x / 2f, x / 2f, x / 2f, paint);
        //这是圆角drawRoundRect
//        canvas.drawRoundRect(new RectF(rect), pixels, pixels, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, srcRect, dstRect, paint);
        return output;

    }
}
