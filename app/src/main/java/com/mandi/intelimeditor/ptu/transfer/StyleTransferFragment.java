package com.mandi.intelimeditor.ptu.transfer;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
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
import com.mandi.intelimeditor.common.util.ProgressCallback;
import com.mandi.intelimeditor.common.util.SimpleObserver;
import com.mandi.intelimeditor.common.util.ToastUtils;
import com.mandi.intelimeditor.common.util.Util;
import com.mandi.intelimeditor.common.util.WrapContentGridLayoutManager;
import com.mandi.intelimeditor.common.view.ImageDecoration;
import com.mandi.intelimeditor.common.view.PtuConstraintLayout;
import com.mandi.intelimeditor.dialog.FirstUseDialog;
import com.mandi.intelimeditor.home.HomeActivity;
import com.mandi.intelimeditor.home.view.BottomFunctionView;
import com.mandi.intelimeditor.ptu.BasePtuFragment;
import com.mandi.intelimeditor.ptu.PTuActivityInterface;
import com.mandi.intelimeditor.ptu.PtuActivity;
import com.mandi.intelimeditor.ptu.PtuUtil;
import com.mandi.intelimeditor.ptu.RepealRedoListener;
import com.mandi.intelimeditor.ptu.common.PtuBaseChooser;
import com.mandi.intelimeditor.ptu.common.TransferController;
import com.mandi.intelimeditor.ptu.repealRedo.CutStepData;
import com.mandi.intelimeditor.ptu.repealRedo.RepealRedoManager;
import com.mandi.intelimeditor.ptu.repealRedo.StepData;
import com.mandi.intelimeditor.ptu.threeLevelFunction.ThreeLevelToolUtil;
import com.mandi.intelimeditor.ptu.tietu.onlineTietu.ImageMaterialAdapter;
import com.mandi.intelimeditor.ptu.tietu.onlineTietu.PicResource;
import com.mandi.intelimeditor.ptu.tietu.onlineTietu.TietuRecyclerAdapter;
import com.mandi.intelimeditor.ptu.view.PtuSeeView;
import com.mandi.intelimeditor.user.US;

import org.greenrobot.eventbus.EventBus;
import org.jetbrains.annotations.NotNull;

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
    public static final String ALL = "all";
    public static final String CHOOSE_PIC_CATEGORY_STYLE = "action_choose_style";
    public static final String CHOOSE_PIC_CATEGORY_CONTENT = "action_choose_content";
    private String TAG = "StyleTransferFragment";
    static final String TRANS_RESULT_STATE = "TRANS_RESULT_STATE";
    static final String TRANS_RESULT_NO_CONTENT = "no_content_pic";
    static final String TRANS_RESULT_NO_STYLE = "no_style_pic";
    static final String TRANS_RESULT_UNKNOWN_FAIL = "unknown reason";

    public static final int EDIT_MODE = PtuUtil.EDIT_DRAW;

    private Context mContext;

    private PTuActivityInterface pTuActivityInterface;
    private PtuSeeView ptuSeeView;
    private RepealRedoListener repealRedoListener;
    private PtuBaseChooser ptuBaseChooser;
    private RecyclerView chooseRcv;

    private boolean isChooseStyleMode = true;

    private ImageMaterialAdapter chooseListAdapter;
    private PicResource chooseImage;
    private PicResource chooseStyleImage;

    static final int bottonWidth = Util.dp2Px(20);
    private TransferController transferController;
    public static final String MODEL_ADAIN = "adain";
    public static final String MODEL_GOOGLE = "google";
    private String model = MODEL_GOOGLE;

    private Bitmap styleBm;
    private boolean isProcessing = false;
    private int lastStylePos = -1;
    private int lastStyleOffset = -1;
    private int lastContentOffset = -1;
    private String stylePath;
    private String contentPath;
    private int lastContentPos = -1;
    private BottomFunctionView chooseContentBtn;
    private BottomFunctionView chooseStyleBtn;
    private PicResource lastStyle;
    private RepealRedoManager<StepData> repealRedoManager;
    private PicResource lastContent;
    private GridLayoutManager gridLayoutManager;


    @Override
    public void setPTuActivityInterface(PTuActivityInterface ptuActivity) {
        this.pTuActivityInterface = ptuActivity;
        repealRedoManager = pTuActivityInterface.getRepealRedoManager();
        this.ptuSeeView = ptuActivity.getPtuSeeView();
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // boolean lastTransferSuccess = SPUtil.getTransferSuccess();
        // if (!lastTransferSuccess) {
        //     AllData.globalSettings.setMaxSupportContentSize((int) (AllData.globalSettings.maxSupportContentSize * 0.8));
        // } else {
        //     AllData.globalSettings.setMaxSupportContentSize((int) (AllData.globalSettings.maxSupportContentSize * 1.01));
        // }
        mContext = getActivity();
        isProcessing = false;
    }

    /**
     * ????????????
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
        //?????????????????????
        chooseRcv = new RecyclerView(mContext);

        gridLayoutManager = new WrapContentGridLayoutManager(mContext, TietuRecyclerAdapter.DEFAULT_ROW_NUMBER,
                GridLayoutManager.HORIZONTAL, false);
        chooseRcv.setLayoutManager(gridLayoutManager);

        isChooseStyleMode = true;
        if (transferController != null) {
            if (transferController.contentBm != null) {
                isChooseStyleMode = true;
                lastStyle = PicResource.path2PicResource(transferController.contentUrl);
                if (AllData.curContentList != null) {
                    lastContentPos = AllData.curContentList.indexOf(lastStyle) + 1;
                    lastContentOffset = AllData.getScreenWidth() / 2;
                }
            }
            if (transferController.styleBm != null) {
                styleBm = transferController.styleBm;
                isChooseStyleMode = false;
                lastContent = PicResource.path2PicResource(transferController.styleUrl);
                if (AllData.curStyleList != null) {
                    lastStylePos = AllData.curStyleList.indexOf(lastContent) + 1;
                    lastStyleOffset = AllData.getScreenWidth() / 2;
                }
            }
        }

        // private boolean isFirstShowChooseRcv;
        chooseContentBtn = rootView.findViewById(R.id.choose_content);

        chooseContentBtn.setOnClickListener(v -> {
            changeImageOrStyle(v, false);
        });

        chooseStyleBtn = rootView.findViewById(R.id.choose_style);
        chooseStyleBtn.setOnClickListener(v -> {
            changeImageOrStyle(v, true);
        });

        BottomFunctionView chooseModel = rootView.findViewById(R.id.choose_model);
        chooseModel.setOnClickListener(this::showChooseModel);

        if (transferController != null) {
            onChosenBm(transferController.contentBm, transferController.styleBm);
        }
        rootView.findViewById(R.id.go_ptu).setOnClickListener(v -> {
            if (chooseRcv.getParent() != null) {
                ((ViewGroup) chooseRcv.getParent()).removeView(chooseRcv);
            }
            repealRedoManager.setBaseBm(pTuActivityInterface.getPtuSeeView().getSourceBm());
            pTuActivityInterface.switchFragment(PtuUtil.EDIT_MAIN, null);
        });

        chooseListAdapter = new ImageMaterialAdapter();
        chooseListAdapter.setOnItemClickListener(chooseRcvListener);
        prepareListView(view);
        if (isChooseStyleMode) {
            showStyleList();
        } else {
            showContentList();
        }
    }

    /**
     * ???????????????????????????
     */
    private void changeImageOrStyle(View view, boolean isStyle) {
        if (chooseRcv.getParent() != null) { // ?????????????????????????????? ?????????????????????
            ((ViewGroup) chooseRcv.getParent()).removeView(chooseRcv);
            chooseStyleBtn.setChosen(false);
            chooseContentBtn.setChosen(false);
            return;
        }
        if (isStyle) {
            chooseStyleBtn.setChosen(!chooseStyleBtn.getSelectedStatus());
            if (chooseStyleBtn.getSelectedStatus()) {
//                chooseContentBtn.setChosen(false);
                getScollPos(gridLayoutManager);
                US.putPTuDeforEvent(US.PTU_DEFOR_SIZE);
                isChooseStyleMode = true;
                prepareListView(view);
                showStyleList();
            }
        } else {
            chooseContentBtn.setChosen(!chooseContentBtn.getSelectedStatus());
            if (chooseContentBtn.getSelectedStatus()) {
//                chooseStyleBtn.setChosen(false);
                getScollPos(gridLayoutManager);
                US.putPTuDeforEvent(US.PTU_DEFOR_EXAMPLE);
                isChooseStyleMode = false;
                prepareListView(view);
                showContentList();
            }
        }

    }


    private void getScollPos(GridLayoutManager gridLayoutManager) {
        if (!isChooseStyleMode) {
            lastContentPos = gridLayoutManager.findLastVisibleItemPosition();
            View findView = gridLayoutManager.findViewByPosition(lastContentPos);
            lastContentOffset = findView != null ? findView.getLeft() : 0;
        } else {
            lastStylePos = gridLayoutManager.findLastVisibleItemPosition();
            View findView = gridLayoutManager.findViewByPosition(lastStylePos);
            lastStyleOffset = findView != null ? findView.getLeft() : 0;
        }
    }

    private void showChooseModel(View view) {
        final int pad = 10;
        final PopupWindow popWindow = ThreeLevelToolUtil.getPopWindow_for3LevelFunction(mContext);
        LinearLayout linearLayout = new LinearLayout(mContext);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setBackgroundColor(Color.WHITE);
        linearLayout.setPadding(0, pad, 0, pad);
        List<TextView> modelTxtList = new ArrayList<>();
        boolean HDOpen = SPUtil.getHighResolutionMode();
        final TextView dmodel1 = createItem(pad, HDOpen, modelTxtList);
        dmodel1.setText("??????");
        dmodel1.setOnClickListener(v -> {
            new FirstUseDialog(getContext()).createDialog("",
                    "??????????????????????????????????????????\n??????????????????, ???????????????????????????",
                    new FirstUseDialog.ActionListener() {
                        @Override
                        public void onSure() {

                        }
                    });
            SPUtil.putHighResolutionMode(true);
            // model = MODEL_ADAIN;
            clearModelChoose(modelTxtList);
            // dmodel1.setTextColor(Util.getColor(R.color.text_checked_color));
            dmodel1.setTextColor(Util.getColor(R.color.text_checked_color));
            // transfer(styleBm, true, false);
        });
        final TextView model2 = createItem(pad, !HDOpen, modelTxtList);
        model2.setText("??????");
        model2.setOnClickListener(v -> {
            SPUtil.putHighResolutionMode(false);
            // model = MODEL_GOOGLE;
            // clearModelChoose(modelTxtList);
            // model2.setTextColor(Util.getColor(R.color.text_checked_color));
            // transfer(styleBm, true, false);
            clearModelChoose(modelTxtList);
            model2.setTextColor(Util.getColor(R.color.text_checked_color));
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
        linearLayout.addView(dmodel1, params);
        linearLayout.addView(d1);
        linearLayout.addView(d3);
        linearLayout.addView(d2);
        linearLayout.addView(model2, params);

        PtuUtil.setPopWindow_for3LevelFunction(popWindow, view, linearLayout);
    }

    private void clearModelChoose(List<TextView> modelTxtList) {
        for (TextView textView : modelTxtList) {
            textView.setTextColor(Util.getColor(R.color.text_deep_black));
        }
    }

    private TextView createItem(final int pad, boolean isChoose, List<TextView> modelTxtList) {
        TextView tv = new TextView(mContext);
        tv.setGravity(Gravity.CENTER);
        tv.setTextSize(20);
        tv.setTextColor(Util.getColor(isChoose ? R.color.text_checked_color : R.color.text_deep_black));
        modelTxtList.add(tv);
        return tv;
    }

    private boolean isFirstUse = true;

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
                if (oneTietu == null) {
                    ToastUtils.show("???????????????????????????");
                    return;
                }
                if (isChooseStyleMode) {
                    chooseStyleImage = oneTietu;
                    chooseListAdapter.setSelectedItem(chooseStyleImage);
                } else {
                    chooseImage = oneTietu;
                    chooseListAdapter.setSelectedItem(chooseImage);
                }
                if (isChooseStyleMode && oneTietu.equals(lastStyle)) {
                    lastStyle = null;
                    ptuSeeView.replaceSourceBm(repealRedoManager.getBaseBitmap());
                    chooseListAdapter.setSelectedItem(null);
                    return;
                } else if (oneTietu.equals(lastContent)) { // ?????????????????????
                    return;
                }

                if (oneTietu.getUrl() != null) {
                    if (!isChooseStyleMode) lastContent = oneTietu;
                    else lastStyle = oneTietu;
                    String url = oneTietu.getUrl().getUrl();
                    ViewGroup parent = (ViewGroup) chooseRcv.getParent();
                    transfer(url, isChooseStyleMode, !isFirstUse);
                    isFirstUse = false;
                    oneTietu.updateHeat();
                    AllData.getThreadPool_single().execute(() ->
                            MyDatabase.getInstance().updateMyTietu(url, System.currentTimeMillis()));
                } else {
                    Log.e(this.getClass().getSimpleName(), "???????????????????????????");
                }
            }
        }
    };

    // private Bitmap realTransferPt(@NotNull Bitmap bm, boolean isStyle) {
    //     Bitmap contentBm = repealRedoManager.getBaseBitmap();
    //     StyleTransferPytorch transfer = StyleTransferPytorch.getInstance();
    //     Log.d(TAG, "realTransfer: ??????????????????????????????");
    //     getActivity().runOnUiThread(() -> pTuActivityInterface.showProgress(33));
    //     if (!isStyle) {
    //         // contentFeature = transfer.getVggFeature(bm);
    //         if (LogUtil.debugStyleTransfer)
    //             Log.e(TAG, "??????????????????VGG??????, size = " + bm.getWidth() + " * " + bm.getHeight());
    //         // ??????????????????????????????????????????????????????
    //         if (styleBm != null && (styleFeature == null || styleBm.getByteCount() <
    //                 contentBm.getByteCount() * AllData.globalSettings.styleContentRatio)) {
    //             // styleFeature = transfer.getVggFeature(styleBm);
    //             if (LogUtil.debugStyleTransfer) {
    //                 Log.e(TAG, "??????????????????vgg, size = " + styleBm.getWidth() + " * " + styleBm.getHeight());
    //                 // LogUtil.printMemoryInfo(TAG + "??????????????????vgg??????", PtuActivity.this);
    //             }
    //             getActivity().runOnUiThread(() -> pTuActivityInterface.showProgress(66));
    //         }
    //     } else {
    //         // if (contentBm != null && contentFeature == null) {
    //         int cw = contentBm.getWidth();
    //         int ch = contentBm.getHeight();
    //         if (contentBuffer == null) {
    //             contentBuffer = ByteBuffer.allocateDirect(3 * cw * ch * 4)
    //                     .order(ByteOrder.nativeOrder())
    //                     .asFloatBuffer();
    //             Log.e(TAG, "realTransfer: ????????????buffer??????");
    //         }
    //         Log.e(TAG, "??????bm??????floabuffer");
    //         StyleTransferPytorch.bitmapToFloatBuffer(contentBm, 0, 0, cw, ch, TensorImageUtils.TORCHVISION_NORM_MEAN_RGB,
    //                 TensorImageUtils.TORCHVISION_NORM_STD_RGB, contentBuffer, 0);
    //         Log.e(TAG, "??????buffer??????tensor");
    //         Tensor contentTensor = Tensor.fromBlob(contentBuffer, new long[]{1, 3, ch, cw});
    //         Log.e(TAG, "??????buffer??????tensor??????");
    //         contentFeature = transfer.getVggFeature(contentTensor);
    //         if (LogUtil.debugStyleTransfer) {
    //             Log.e(TAG, "??????????????????vgg, size = " + cw
    //                     + " * " + contentBm.getHeight());
    //             // LogUtil.printMemoryInfo(TAG + "??????????????????vgg??????", PtuActivity.this);
    //         }
    //         getActivity().runOnUiThread(() -> pTuActivityInterface.showProgress(33));
    //         // }
    //         // ???????????????
    //         styleBm = bm;
    //         if (styleBm.getByteCount() < contentBm.getByteCount() * AllData.globalSettings.styleContentRatio) {
    //             styleBm = Bitmap.createScaledBitmap(styleBm, styleBm.getWidth(), styleBm.getHeight(), true);
    //         }
    //
    //         int sw = styleBm.getWidth();
    //         int sh = styleBm.getHeight();
    //         if (styleBuffer == null) {
    //             styleBuffer = ByteBuffer.allocateDirect(3 * sw * sh * 4)
    //                     .order(ByteOrder.nativeOrder())
    //                     .asFloatBuffer();
    //             Log.e(TAG, "realTransfer: ????????????buffer");
    //         }
    //         Log.e(TAG, "??????bm??????floabuffer");
    //         StyleTransferPytorch.bitmapToFloatBuffer(styleBm, 0, 0, sw, sh, TensorImageUtils.TORCHVISION_NORM_MEAN_RGB,
    //                 TensorImageUtils.TORCHVISION_NORM_STD_RGB, styleBuffer, 0);
    //         Log.e(TAG, "??????buffer??????tensor");
    //         Tensor styleTensor = Tensor.fromBlob(styleBuffer, new long[]{1, 3, sh, sw});
    //         Log.e(TAG, "??????buffer??????tensor??????");
    //         styleFeature = transfer.getVggFeature(styleTensor);
    //
    //         if (LogUtil.debugStyleTransfer) {
    //             Log.e(TAG, "??????????????????vgg, size = " + styleBm.getWidth() + " * " + styleBm.getHeight());
    //         }
    //         getActivity().runOnUiThread(() -> pTuActivityInterface.showProgress(66));
    //     }
    //     Bitmap res = transfer.transfer(contentFeature, styleFeature, 1f);
    //     // Buffer????????????????????????
    //     // contentBuffer.clear();
    //     // styleBuffer.clear();
    //     // contentBuffer = null;
    //     // styleBuffer = null;
    //     System.gc();
    //     if (LogUtil.debugStyleTransfer) {
    //         LogUtil.d(TAG + "?????????????????? ");
    //     }
    //     return res;
    // }
    private void chooseFromAllPic() {
        US.putPTuTietuEvent(US.PTU_TIETU_MORE);
        Intent intent = new Intent(mContext, HomeActivity.class);
        intent.setAction(HomeActivity.INTENT_ACTION_ONLY_CHOSE_PIC);
        intent.addCategory(isChooseStyleMode ? CHOOSE_PIC_CATEGORY_STYLE : CHOOSE_PIC_CATEGORY_CONTENT);
        intent.putExtra(HomeActivity.INTENT_EXTRA_FRAGMENT_ID, isChooseStyleMode ? HomeActivity.LOCAL_FRAG_ID : HomeActivity.TEMPLATE_FRAG_ID);
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

    /**
     * ???????????????
     * ?????????
     * ??????????????????????????????
     */
    private void prepareListView(View view) {
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
            if (chooseRcv.getItemDecorationCount() == 0) {
                chooseRcv.addItemDecoration(new ImageDecoration(getActivity()));
            }
            chooseRcv.setAdapter(chooseListAdapter);
        }
        // if (isFirstShowChooseRcv) { // ??????????????????????????????????????????????????????????????????????????????????????????
        //     view.post(() -> prepareShowStyleList(view));
        //     view.post(() -> prepareShowStyleList(view));
        // }
        // isFirstShowChooseRcv = false;
    }

    private void showContentList() {
        if (AllData.curContentList.size() != 0) {
            showStyleOrContentList(AllData.curContentList);
        } else { // ???????????????????????????????????????
            AllData.queryLocalPicList(new Emitter<String>() {
                @Override
                public void onNext(@io.reactivex.annotations.NonNull String value) {
                    if (isDetached()) {
                        return;
                    }
                    AllData.curContentList = AllData.sMediaInfoScanner.convertRecentPath2PicResList();
                    showStyleOrContentList(AllData.curContentList);
                }

                @Override
                public void onError(@io.reactivex.annotations.NonNull Throwable error) {
                    if (isDetached()) {
                        return;
                    }
                    ToastUtils.show("??????????????????????????????");
                }

                @Override
                public void onComplete() {

                }
            });
        }
    }

    private void showStyleList() {
        if (AllData.curStyleList.size() != 0) {
            showStyleOrContentList(AllData.curStyleList);
        } else AllData.queryAllPicRes(new Emitter<List<PicResource>>() {

            @Override
            public void onNext(@NotNull List<PicResource> resList) {
                if (isDetached()) {
                    return;
                }
                int size = resList.size();
                if (size == 0) {
                    onNoStylePic();
                    return;
                }
                Log.d("TAG", "onNext: ????????????????????????" + size);
                AllData.curStyleList = resList;

                showStyleOrContentList(resList);
            }

            @Override
            public void onError(@NotNull Throwable throwable) {
                if (isDetached()) {
                    return;
                }
                onNoStylePic();
                LogUtil.e(throwable.getMessage());
                showStyleOrContentList(null);
            }

            @Override
            public void onComplete() {

            }
        });
    }

    private void showStyleOrContentList(List<PicResource> list) {
        if (list == null) list = new ArrayList<>();
        if (isChooseStyleMode) {
            chooseContentBtn.setChosen(false);
            chooseStyleBtn.setChosen(true);
            chooseListAdapter.setList(list, chooseStyleImage);
        } else {
            chooseStyleBtn.setChosen(false);
            chooseContentBtn.setChosen(true);
            chooseListAdapter.setList(list, chooseImage);
        }
        chooseListAdapter.add(0, PicResource.path2PicResource(ALL));
        int pos = !isChooseStyleMode ? lastContentPos : lastStylePos;
        int offset = !isChooseStyleMode ? lastContentOffset : lastStyleOffset;

        // chooseListAdapter.setSelect(lastChoseID);
        if (pos >= 0) {
            ((LinearLayoutManager) chooseRcv.getLayoutManager()).scrollToPositionWithOffset(pos, offset);
        }
    }

    private void onNoStylePic() {
        if (chooseListAdapter != null) { // ????????????????????????????????????tietuListAdapter???????????????????????????????????????????????????GG
            chooseListAdapter.setList(new ArrayList<PicResource>(), null);
            String msg;
            msg = mContext.getString(R.string.no_network_style_notice);
            PtuUtil.onNoPicResource(msg);
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, final Intent data) {
        if (requestCode == PtuActivity.REQUEST_CODE_CHOOSE_CONTENT && data != null) {
            PicResource picRes = (PicResource) data.getSerializableExtra(HomeActivity.INTENT_EXTRA_CHOSEN_PIC_RES);
            transfer(picRes.getUrlString(), false, true);
            lastContentPos = AllData.curContentList.indexOf(picRes) + 1;
            lastContentOffset = AllData.getScreenWidth() / 2;
            showStyleOrContentList(AllData.curContentList);
        }

        if (requestCode == PtuActivity.REQUEST_CODE_CHOOSE_STYLE && data != null) {
            PicResource picRes = (PicResource) data.getSerializableExtra(HomeActivity.INTENT_EXTRA_CHOSEN_PIC_RES);
            transfer(picRes.getUrlString(), true, true);
            lastStylePos = AllData.curStyleList.indexOf(picRes) + 1;
            lastStyleOffset = AllData.getScreenWidth() / 2;
            showStyleOrContentList(AllData.curStyleList);
        }

        // ??????????????????, ????????????????????????????????????????????????????????????????????????adapter???????????????????????????
        if (resultCode == CommonConstant.RESULT_CODE_OPEN_VIP_SUCCESS) {
            if (AllData.isVip) {
                // TODO: 2020/10/22 ???????????????????????????

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
        EventBus.getDefault().unregister(this);
    }

    @Override
    public StepData getResultDataAndDraw(float ratio) {
        //?????????????????????
        Bitmap resultBm = getResultBm(1);
        if (resultBm == null) return null; // ????????????
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
        //        ???????????????????????????????????????????????????PTuSeeView??????????????????????????????????????????

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
        // ??????????????????????????????????????????????????????????????????????????????
        // ????????????????????????????????????????????????
        return false;
    }

    public void initBeforeCreateView(TransferController transferController) {
        this.transferController = transferController;
        isFirstUse = true;
    }

    /**
     * ??????????????????????????????????????????????????????????????????StackOverFlow????????????????????????????????????????????????????????????
     * ??????????????????????????????????????????????????????????????????
     *
     * @param isReuse ???????????????????????????content??????style
     */
    public void transfer(Object obj, boolean isStyle, boolean isReuse) {
        if (isProcessing) {
            ToastUtils.show("?????????????????????");
            return;
        }
        isProcessing = true;
        pTuActivityInterface.hidePtuNotice();
        pTuActivityInterface.showProgressUiThread(0);

        Observable.create((ObservableOnSubscribe<Bitmap>) emitter -> {
            if (LogUtil.debugStyleTransfer) {
                LogUtil.d(TAG + "??????????????????");
            }
            // ???????????????url??????????????????
            // ????????????????????????Bm
            // ??????????????????Bm
            int decodeSize = isStyle ? (int) (AllData.globalSettings.maxSupportContentSize *
                    AllData.globalSettings.styleContentRatio)
                    : AllData.globalSettings.maxSupportContentSize;
            decodeSize *= decodeSize;
            BitmapUtil.decodeFromObj(obj, emitter, decodeSize);
            if (LogUtil.debugStyleTransfer) {
                LogUtil.d(TAG + "?????????????????????bitmap??????");
            }
            emitter.onComplete();
        })
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.computation())
                .map(bm -> {
                    // ??????UI???????????????
                    pTuActivityInterface.showProgressUiThread(10);
                    // ?????????????????????????????????????????????
                    Pair<String, Bitmap> res;
                    if (MODEL_GOOGLE.equals(model)) {
                        res = realTransferTensorflow(bm, isStyle, isReuse);
                    } else {
                        res = transferPytorch(bm, isStyle, false);
                    }
                    if (res.second == null) { // ??????
                        throw new Exception(getErrorMsg(res.first));
                    }
                    return res.second;
                }).subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<Bitmap>() {
                    @Override
                    public void onNext(@io.reactivex.annotations.NonNull Bitmap bitmap) {
                        progressCallback.onProgress(100);
                        ptuSeeView.replaceSourceBm(bitmap);
                        pTuActivityInterface.dismissProgress();
                        isProcessing = false;
                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                        ToastUtils.show(e.getMessage(), Toast.LENGTH_LONG);
                        pTuActivityInterface.dismissProgress();
                        isProcessing = false;
                    }

                    @Override
                    public void onComplete() {
                        super.onComplete();
                        pTuActivityInterface.dismissProgress();
                        isProcessing = false;
                    }
                });

    }

    /**
     * null ???????????????
     */
    private String getErrorMsg(String error) {
        String msg = "";
        if (TRANS_RESULT_NO_CONTENT.equals(error)) {
            msg = getContext().getString(R.string.you_did_not_choose_content);
        } else if (TRANS_RESULT_NO_STYLE.equals(error)) {
            msg = getContext().getString(R.string.you_did_not_choose_style);
        } else if (error != null) {
            msg = error;
        } else {
            msg = getContext().getString(R.string.unkown_error);
        }
        return msg;
    }

    ProgressCallback progressCallback = new ProgressCallback() {
        @Override
        public void setMax(int max) {

        }

        @Override
        public void onProgress(int progress) {
            if (isDetached()) return;
            pTuActivityInterface.showProgressUiThread(progress);
        }
    };

    @NotNull
    private Pair<String, Bitmap> realTransferTensorflow(@NotNull Bitmap bm, boolean isChangeStyle, boolean isReuse) {
        Bitmap baseBitmap = repealRedoManager.getBaseBitmap();
        Bitmap sBm = styleBm, cBm = baseBitmap;
        // ?????????????????????????????????????????????
        if (isChangeStyle) {
            sBm = bm;
            styleBm = sBm;
        } else {
            baseBitmap = cBm = bm;
            repealRedoManager.setBaseBm(bm);
        }

        StyleTransferTensorflow transfer = StyleTransferTensorflow.getInstance();
        if (isChangeStyle) {
            if (transfer.isContentExit() && isReuse) { //  // ???????????? ??????????????????
                cBm = null;
            } else if (cBm == null) {  // ?????????????????????,?????????????????????
                return new Pair<>(TRANS_RESULT_NO_CONTENT, null);
            }
        } else {  // ????????????
            if (transfer.isStyleExist() && isReuse) { // ??????????????????
                sBm = null;
            } else if (sBm == null) { // ?????????????????????????????????????????????
                return new Pair<>(TRANS_RESULT_NO_STYLE, null);
            }
        }
        progressCallback.onProgress(10);
        Bitmap rstBm;
        if (baseBitmap.getWidth() > StyleTransferTensorflow.CONTENT_SIZE * 2.5 || baseBitmap.getHeight() > StyleTransferTensorflow.CONTENT_SIZE * 2.5) {
            rstBm = transfer.transferBigSize(baseBitmap, sBm, progressCallback);
        } else {
            rstBm = transfer.transfer(cBm, sBm, progressCallback);
        }
        String errorMsg = rstBm == null ? "" : null;
        return new Pair<>(errorMsg, rstBm);
    }

    @NotNull
    private Pair<String, Bitmap> transferPytorch(@NotNull Bitmap bm, boolean isChangeStyle, boolean isReuse) {
        SPUtil.putTransferFinish(false);
        Bitmap sBm = styleBm, cBm = repealRedoManager.getBaseBitmap();
        Bitmap rstBm = null;
        try {
            // ?????????????????????????????????????????????
            if (isChangeStyle) {
                sBm = bm;
                styleBm = sBm;
            } else {
                cBm = bm;
                repealRedoManager.setBaseBm(bm);
            }

            // StyleTransferPytorch transfer = StyleTransferPytorch.getInstance();
            // if (isReuse) { // ???????????????????????????????????????????????????????????????
            //     if (isChangeStyle) {
            //         cBm = null;
            //     } else {
            //         sBm = null;
            //     }
            // }
            // return new Pair<>(null, transfer.transfer(cBm, sBm, 1));
        } catch (Throwable e) {
            e.printStackTrace();
            if (e instanceof OutOfMemoryError || e instanceof StackOverflowError || e.getMessage().contains("not enough memory")) { // ???????????????????????????????????????
                if (LogUtil.debugStyleTransfer) {
                    Log.d(TAG, String.format("???????????? %d ??????", AllData.globalSettings.maxSupportContentSize));
                }
                AllData.globalSettings.maxSupportContentSize *= 0.8f;
                Bitmap contentBm = repealRedoManager.getBaseBitmap();
                double ratio = AllData.globalSettings.maxSupportContentSize * 1f / (contentBm.getWidth() * contentBm.getHeight());
                contentBm = Bitmap.createScaledBitmap(contentBm, (int) (ratio * contentBm.getWidth()),
                        (int) (ratio * contentBm.getHeight()), true);
                repealRedoManager.setBaseBm(contentBm);
                SPUtil.putTransferFinish(true);
            } else {
                Log.e(TAG, "transferPt: ?????????????????????????????? ??????Google??????");
                SPUtil.putTransferFinish(true);
                return realTransferTensorflow(bm, isChangeStyle, isReuse);
            }
        }
        // ???????????????????????????????????????
        SPUtil.putTransferFinish(true);
        return new Pair<>(null, rstBm);
    }
}
