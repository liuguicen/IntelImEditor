package com.mandi.intelimeditor.ptu;

import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.core.util.Pair;
import androidx.fragment.app.FragmentManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.Target;
import com.mandi.intelimeditor.CertainLeaveDialog;
import com.mandi.intelimeditor.R;
import com.mandi.intelimeditor.ad.AdData;
import com.mandi.intelimeditor.ad.tencentAD.TxBannerAd;
import com.mandi.intelimeditor.ad.ttAD.videoAd.FullScreenVadManager;
import com.mandi.intelimeditor.common.BaseActivity;
import com.mandi.intelimeditor.common.Constants.APPConstants;
import com.mandi.intelimeditor.common.dataAndLogic.AllData;
import com.mandi.intelimeditor.common.dataAndLogic.MyDatabase;
import com.mandi.intelimeditor.common.device.YearClass;
import com.mandi.intelimeditor.common.util.BitmapUtil;
import com.mandi.intelimeditor.common.util.FileTool;

import com.mandi.intelimeditor.common.util.LogUtil;
import com.mandi.intelimeditor.common.util.ProgressCallback;
import com.mandi.intelimeditor.common.util.SimpleObserver;
import com.mandi.intelimeditor.common.util.ToastUtils;
import com.mandi.intelimeditor.common.util.Util;
import com.mandi.intelimeditor.common.view.StatusBarUtil;
import com.mandi.intelimeditor.dialog.LoadingDialog;
import com.mandi.intelimeditor.home.data.MediaInfoScanner;
import com.mandi.intelimeditor.network.NetWorkState;
import com.mandi.intelimeditor.ptu.common.DigController;
import com.mandi.intelimeditor.ptu.common.DrawController;
import com.mandi.intelimeditor.ptu.common.MainFunctionFragment;
import com.mandi.intelimeditor.ptu.common.SecondFuncController;
import com.mandi.intelimeditor.ptu.common.TietuController;
import com.mandi.intelimeditor.ptu.common.TransferController;
import com.mandi.intelimeditor.ptu.cut.CutFragment;
import com.mandi.intelimeditor.ptu.deformation.DeformationFragment;
import com.mandi.intelimeditor.ptu.dig.DigFragment;
import com.mandi.intelimeditor.ptu.draw.DrawFragment;
import com.mandi.intelimeditor.ptu.gif.GifEditFragment;
import com.mandi.intelimeditor.ptu.gif.GifFrame;
import com.mandi.intelimeditor.ptu.gif.GifManager;
import com.mandi.intelimeditor.ptu.gif.MyGifDecoder;
import com.mandi.intelimeditor.ptu.rendpic.RendFragment;
import com.mandi.intelimeditor.ptu.repealRedo.DigStepData;
import com.mandi.intelimeditor.ptu.repealRedo.RepealRedoManager;
import com.mandi.intelimeditor.ptu.repealRedo.StepData;
import com.mandi.intelimeditor.ptu.saveAndShare.PTuResultActivity;
import com.mandi.intelimeditor.ptu.saveAndShare.PTuResultData;
import com.mandi.intelimeditor.ptu.saveAndShare.SaveSetInstance;
import com.mandi.intelimeditor.ptu.saveAndShare.SaveShareManager;
import com.mandi.intelimeditor.ptu.text.FloatTextView;
import com.mandi.intelimeditor.ptu.text.TextFragment;
import com.mandi.intelimeditor.ptu.tietu.TietuFragment;
import com.mandi.intelimeditor.ptu.tietu.onlineTietu.PicResource;
import com.mandi.intelimeditor.ptu.transfer.StyleTransferFragment;
import com.mandi.intelimeditor.ptu.view.PtuFrameLayout;
import com.mandi.intelimeditor.ptu.view.PtuSeeView;
import com.mandi.intelimeditor.ptu.view.PtuToolbar;
import com.mandi.intelimeditor.user.US;
import com.mandi.intelimeditor.user.userVip.VipUtil;
import com.mandi.intelimeditor.user.useruse.FirstUseUtil;
import com.mandi.intelimeditor.user.useruse.tutorial.GuideData;
import com.mandi.intelimeditor.user.useruse.tutorial.Tutorial;
import com.umeng.analytics.MobclickAgent;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import USeruse.tutorial.GuideDialog;
import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

import static com.mandi.intelimeditor.ptu.PtuUtil.EDIT_CUT;
import static com.mandi.intelimeditor.ptu.PtuUtil.EDIT_DEFORMATION;
import static com.mandi.intelimeditor.ptu.PtuUtil.EDIT_DIG;
import static com.mandi.intelimeditor.ptu.PtuUtil.EDIT_DRAW;
import static com.mandi.intelimeditor.ptu.PtuUtil.EDIT_GIF;
import static com.mandi.intelimeditor.ptu.PtuUtil.EDIT_MAIN;
import static com.mandi.intelimeditor.ptu.PtuUtil.EDIT_REND;
import static com.mandi.intelimeditor.ptu.PtuUtil.EDIT_TEXT;
import static com.mandi.intelimeditor.ptu.PtuUtil.EDIT_TIETU;
import static com.mandi.intelimeditor.ptu.PtuUtil.EDIT_TRANSFER;


/**
 * P??? ????????????
 * ????????????????????????P???????????????????????????P?????????????????????????????????????????????????????????P???????????????????????????GI???
 * ??????????????????????????????????????????P??????????????????????????????????????????????????????????????????P??????????????????????????????
 * P????????????????????????????????????????????????????????????bm????????????????????????????????????
 */
public class PtuActivity extends BaseActivity implements PTuActivityInterface, ProgressCallback, PTuContract.View {
    public static final int REQUEST_CODE_CHOOSE_TIETU = 11;
    public static final int REQUEST_CODE_CHOOSE_BASE = 1001;

    // ??????????????? ??????P???
    public static final int RESULT_CODE_CONTINUE_PTU = 101;
    public static final int RESULT_CODE_RETURN_CHOOSE = 100;
    public static final int RESULT_CODE_CHOSE_TIETU = 201;
    public static final int RESULT_CODE_CHOSE_BASE = 203;
    // ???????????????????????????????????????????????????result code???????????????????????????request code ????????????
    public static final int RESULT_CODE_INTERMEDIATE_PTU = 202;

    public static final int REQUEST_CODE_CHOOSE_STYLE = 303;
    public static final int REQUEST_CODE_CHOOSE_CONTENT = 304;

    // ???????????????????????????
    public static final int RESULT_CODE_NORMAL_RETURN = 0;

    // ??????PTuActivity????????????????????????????????????PTuActivity?????????????????????????????????????????????????????????
    public static final String PTU_ACTION_NORMAL = "ptu_action_normal";
    public static final String PTU_ACTION_LATEST_PIC = "latest_pic";
    public static final String PTU_ACTION_PICS_MAKE_GIF = "pics_make_gif";
    public static final String PTU_ACTION_VIDEO_MAKE_GIF = "video_make_gif";
    /**
     * ??????????????????????????????????????????P??????????????????????????????????????????????????????????????????P??????????????????????????????
     * P????????????????????????????????????????????????????????????bm????????????????????????????????????
     */
    public static final String PTU_ACTION_AS_INTERMEDIATE_PTU = "as_intermediate_ptu";

    /**
     * ?????????????????????????????????
     */
    public static final String PTU_ACTION_THIRD_APP = "third_app";

    public static final String INTENT_EXTRA_PIC_PATH = AllData.PACKAGE_NAME + ".pic_path";
    // ??????????????????????????????
    public static final String INTENT_EXTRA_SECOND_PIC_PATH = AllData.PACKAGE_NAME + ".second_pic_path";
    public static final String INTENT_EXTRA_VIDEO_PATH = AllData.PACKAGE_NAME + ".video_path";
    public static final String INTENT_EXTRA_CHOSE_BASE_PIC_RES = AllData.PACKAGE_NAME + ".base_path_key";
    public static final String INTENT_EXTRA_CHOSEN_TAGS = AllData.PACKAGE_NAME + ".chosen_tags";
    /**
     * ??????P?????????????????????????????????????????????????????????
     */
    public static final String INTENT_EXTRA_PTU_NOTICE = AllData.PACKAGE_NAME + ".ptu_notice";
    /**
     * ?????????????????????P???????????? {@link PtuUtil#EDIT_CUT ????????????????????????????????????????????????????????????????????????
     * ????????????P??????????????????????????????????????????}
     **/
    public static final String INTENT_EXTRA_TO_CHILD_FUNCTION = AllData.PACKAGE_NAME + ".ptu_to_child_function";
    public static final String INTENT_EXTRA_IS_STYLE = AllData.PACKAGE_NAME + ".is_style";
    // PTu???????????????????????????
    public static final String INTENT_EXTRA_INTERMEDIATE_PTU_NAME = AllData.PACKAGE_NAME + ".intermediate_ptu_name";
    // PTu???????????????????????????????????? ???????????????????????????
    public static final String INTENT_EXTRA_INTERMEDIATE_PTU_FINISH_NAME = AllData.PACKAGE_NAME + ".intermediate_ptu_finish_name";
    // ??????????????????????????????????????????
    public static final String PTU_DATA_GIF_PIC_LIST = AllData.PACKAGE_NAME + "ptu_data_gif_pic_list";
    private static final String TAG = "PtuActivity";

    public static final String LOAD_FAILED = "load_failed";
    public static int myClickCount = 0;

    /**
     * ?????????????????????
     */
    private int currentEditMode = 0;
    /**
     * ????????????fragment
     */
    MainFunctionFragment mainFrag;
    FragmentManager fm;
    private TextFragment textFrag;
    public TietuFragment tietuFrag;
    private DrawFragment drawFrag;
    private CutFragment cutFrag;
    private DigFragment digFrag;
    private RendFragment rendFrag;
    private DeformationFragment deformationFrag;
    private StyleTransferFragment transferFrag;
    private GifEditFragment gifEditFrag;

    public PtuSeeView ptuSeeView;

    private PtuFrameLayout ptuFrame;
    /**
     * ??????????????????bitmap?????????,0?????????????????????????????????????????????1?????????????????????????????????????????????
     * <p>2????????????????????????3????????????????????????
     */
    private String picPath = null;
    private String originalPath; // ???????????????url???????????????url??????????????????
    private final int MAX_STEP = 10;
    private RepealRedoManager<StepData> repealRedoManager;

    private PtuToolbar mPtuToolbar;

    /**
     * ??????PtuFragment?????????,????????????0,??????????????????????????????????????????
     */
    @NotNull
    private final Rect totalBound = new Rect(0, 0, 0, 0);

    @Nullable
    private SaveShareManager saveSetManager;
    /**
     * ???????????????gif?????????????????????????????????
     */
    private GifManager gifManager;

    /**
     * ??????????????????
     */
    private LoadingDialog dialog;
//    private ProgressDialog mProgressDialog;

    private RepealRedoListener repealRedoListener;
    private SaveSetInstance saveSetInstance;
    /**
     * currentFrag
     * ?????????????????????fragment??????????????????????????????
     */
    private BasePtuFragment lastFrag, currentFrag;
    private boolean isLoadFromGlideCache = false;
    /**
     * ???URL??????????????????????????????Glide??????????????????????????????????????????????????????????????????????????????
     */
    private String urlPicSuffix = BitmapUtil.DEFAULT_SAVE_SUFFIX;
    private int picLoadMode;

    private String mSaveSuffix = null;
    private boolean mIsInLoading;
    private boolean isIntermediatePtu = false;

    private final ArrayList<String> useTagsList = new ArrayList<>();
    private TxBannerAd bannerAd;
    private PopupWindow pop;
    private boolean isStyle;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setHideStatusBar(false);
        super.onCreate(savedInstanceState);
        StatusBarUtil.setColor(this, Color.parseColor("#eeeeee"));
        // TODO: 2017/3/3 0003 ???????????????????????????????????????????????????
        initBaseData();
        setTitle("");
        initView();
        initFragment();
        startLoadpic(null); // ????????????????????????
        LogUtil.printMemoryInfo(TAG + " ?????? ", this);
        // contentTensor = createTensor();
    }

    private FloatBuffer createTensor(int size) {
        FloatBuffer inTensorBuffer = ByteBuffer.allocate(size * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();

        return inTensorBuffer;
        // Tensor tensor = Tensor.fromBlob(inTensorBuffer, new long[]{1, 3, wh[0], wh[1]});
    }

    private void initBaseData() {
        saveSetInstance = new SaveSetInstance();
        repealRedoManager = new RepealRedoManager<>(MAX_STEP);
        myClickCount = 0;
        Intent intent = getIntent();
        if (PTU_ACTION_AS_INTERMEDIATE_PTU.equals(intent.getAction())) {
            isIntermediatePtu = true;
        }
        addUsedTags(true, intent.getStringExtra(INTENT_EXTRA_CHOSEN_TAGS));
        isStyle = getIntent().getBooleanExtra(INTENT_EXTRA_IS_STYLE, false);
    }

    /******************************* AC?????????????????? *******************************/

    @Override
    protected void onRestart() {
        VipUtil.judeShowToOpenVip_forAdClick(this);
        super.onRestart();
    }

    //??????????????????????????????????????????????????????UI??????
    @Override
    public void onStart() {
        LogUtil.d(TAG, "onStart_1");
        if (saveSetManager == null) {
            Observable.create(
                    emitter -> {
                        Log.e(TAG, "call: ?????????saveset Rx");
                        saveSetManager = saveSetInstance.getSaveShare_DialogManager_Instance(PtuActivity.this);
                        emitter.onComplete();
                    })
                    .subscribeOn(Schedulers.io())
                    .subscribe(new SimpleObserver<Object>() {

                        @Override
                        public void onNext(Object o) {

                        }

                        @Override
                        public void onError(Throwable e) {

                        }
                    });
        }
        super.onStart();
        LogUtil.d(TAG, "onStart_2");
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (saveSetInstance.hasShared) { // ???share????????????
            saveSetInstance.hasShared = false; //????????????
            Intent intent = new Intent(this, PTuResultActivity.class);
            intent.setAction(PTuResultData.SHARE_AND_LEAVE);
            String savePath = repealRedoManager.lastSavePath;
            if (savePath == null) { //  ???????????????????????????????????????????????????????????????
                savePath = picPath;
            }
            intent.putExtra(PTuResultData.NEW_PIC_PATH, savePath);
            startPTuResultAc(intent);
        }

        if (gifManager != null) {
            gifManager.startAnimation();
        }
    }

    @Override
    protected void onPause() {
        if (gifManager != null) {
            gifManager.stopAnimation();
        }
        super.onPause();
    }

    @Override
    public void onDestroy() {
        if (ptuSeeView != null)
            ptuSeeView.releaseResource();
        if (repealRedoManager != null)
            repealRedoManager.clear(this);
        if (saveSetManager != null) {
            saveSetManager.dismissDialog();
        }
        if (gifManager != null) {
            gifManager.destroy();
            gifManager = null;
        }
        AdData.destroyPtuResultAd();
        AdData.destroyPtuInsertAd();
        super.onDestroy();
        //??????????????????
        if (mainFrag != null) {
            mainFrag.setPTuActivityInterface(null);
            mainFrag.clear();
            mainFrag = null;
        }
        if (currentFrag != null) {
            currentFrag.clear();
            currentFrag = null;
        }
        if (lastFrag != null) {
            lastFrag.clear();
            lastFrag = null;
        }
        if (bannerAd != null) {
            bannerAd.destroy();
        }
        //popWindow ??????????????????
        if (pop != null) {
            pop.dismiss();
            pop = null;
        }
        AllData.curStyleList = new ArrayList<>(); // ??????????????????
        AllData.curContentList = new ArrayList<>();
        FirstUseUtil.release(this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (saveSetManager != null) { // ?????????????????????????????????????????????saveSetManager???????????????????????????
            saveSetManager.onActivityResult(requestCode, resultCode, data);
        }
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_CODE_RETURN_CHOOSE) {
            setResult(0, data);
            finish();
        } else if (resultCode == RESULT_CODE_CONTINUE_PTU) {
            repealRedoManager.setCurrentAsOperateStart();
        } else if (resultCode == PTuResultActivity.RESULT_CODE_PTU_RECOMMEND) {
            finish();
            PicResource picRes = (PicResource) data.getSerializableExtra(PTuResultActivity.INTENT_EXTRA_RECOMMEND_PIC_RES);
            Intent startAcIntent = getStartAcIntent(this, picRes);
            if (startAcIntent != null) {
                startActivity(startAcIntent);
                finish();
            }
        }
    }


    /******************************* ??????View?????? *******************************/

    /**
     *
     */
    @Override
    public int getLayoutResId() {
        return R.layout.activity_ptu;
    }

    /**
     * ???????????????,????????????????????????
     */
    private void initView() {
        ptuFrame = findViewById(R.id.ptu_frame);
        ptuSeeView = findViewById(R.id.ptu_view);
        ptuSeeView.setPTuActivityInterface(this);
        mPtuToolbar = findViewById(R.id.ptu_toolbar_relative);
        if (isIntermediatePtu) {
            mPtuToolbar.setRightTopText(getIntent().getStringExtra(INTENT_EXTRA_INTERMEDIATE_PTU_FINISH_NAME));
        }
        mPtuToolbar.setOnToolClickListener(v -> {
            switch (v.getId()) {
                case R.id.iv_undo:
                    if (currentEditMode == EDIT_MAIN)
                        bigRepeal();
                    else smallRepeal();
                    break;
                case R.id.iv_redo:
                    if (currentEditMode == EDIT_MAIN)
                        bigRedo();
                    else smallRedo();
                    break;
                case R.id.iv_save:
                    if (!Util.DoubleClick.isDoubleClick()) {
                        if (currentFrag == mainFrag) {
                            repealRedoManager.setBaseBm(ptuSeeView.getSourceBm());
                            switchFragment(EDIT_TRANSFER, null);
                            break;
                        }
                        if (!isIntermediatePtu) {
                            saveSet();
                        } else {
                            finishIntermediatePtu();
                        }
                    }
                    break;
                case R.id.iv_sure:
                    if (currentEditMode != EDIT_MAIN) {
                        if (currentFrag != null) {
                            if (currentFrag.onSure())
                                return;
                        }
                        sure(null);
                    }
                    break;
                case R.id.back_iv:
                    if (currentFrag == mainFrag) {
                        certainCancelEdit();
                    } else {
                        certainLeavePage();
                    }
                    break;
                case R.id.iv_cancel:
                    if (currentFrag != null) {
                        if (currentFrag.onBackPressed(false)) { //  cancel???????????????????????????
                            return;
                        }
                    }
                    returnMain();
                    break;
                case R.id.iv_help:
                    List<String> keyWordList = currentFrag != null ? currentFrag.getGuidKeyword() : null;
                    if (keyWordList == null) keyWordList = GuideData.getKeyword(currentEditMode,
                            getIntent().getIntExtra(INTENT_EXTRA_TO_CHILD_FUNCTION, -1),
                            gifManager != null);
                    if (!AllData.hasLoadGuide) { // ???????????????????????????????????????????????????????????????????????????????????????????????????
                        Tutorial.loadGuideUseFromServer(true); //??????????????????????????????
                        AllData.hasLoadGuide = true;
                    }
                    showGuideDialog(keyWordList);
                    break;
            }
        });
        mPtuToolbar.switchToolbarBtn(false);
    }

    private void showPtuNotice(String ptuNotice) {
        if (ptuNotice == null)
            ptuNotice = getIntent().getStringExtra(INTENT_EXTRA_PTU_NOTICE);

        if (ptuNotice != null) {
            View noticeLayout = findViewById(R.id.ptu_notice_layout);
            noticeLayout.setVisibility(View.VISIBLE);
            TextView noticeTv = noticeLayout.findViewById(R.id.ptu_notice_tv);
            noticeTv.setText(ptuNotice);
            noticeLayout.findViewById(R.id.ptu_notice_cancel).setOnClickListener(v -> noticeLayout.setVisibility(View.GONE));
        }
    }

    public void hidePtuNotice() {
        findViewById(R.id.ptu_notice_layout).setVisibility(View.GONE);
    }


    @Override
    public void showGuideDialog(List<String> keyWordList) {
        GuideDialog.Companion.newInstance().setGuideAdapter(
                GuideData.getGuideUseDataByType(keyWordList))
                .showIt(PtuActivity.this);
    }

    private void initFragment() {
        fm = getSupportFragmentManager();
        repealRedoListener = new RepealRedoListener() {
            @Override
            public void canRedo(boolean canRedo) {
                // Log.e(TAG, "?????????????????????");
                mPtuToolbar.updateRedoBtn(canRedo);
            }

            @Override
            public void canRepeal(boolean canRepeal) {
//                mPtuToolbar.updateRepealBtn(canRepeal);
            }
        };
    }

/*****************************************??????????????????*********************************************/
//    ==============================================================================================
//    ==============================================================================================

    /**
     * ???????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
     * ????????????????????????
     * <p>
     * ?????????????????????P???????????????????????????
     *
     * @param url url????????????????????????url,???????????????????????????????????????Intent??????
     */
    private void startLoadpic(@Nullable String url) {
        if (mIsInLoading) { // ?????????????????????????????????
            return;
        }
        mIsInLoading = true;
        boolean isReplace = url != null;
        picLoadMode = PtuUtil.NORMAL_OPEN;
        Observable
                .create(emitter -> {
                    // ????????????????????????
                    // first ??????????????????url
                    String oldPath = picPath;
                    if (url == null) {
                        Intent sourceIntent = getIntent();
                        String ptuAction = sourceIntent.getAction();
                        Uri uri = sourceIntent.getData();
                        if (uri != null) {
                            //??????????????????????????????????????????
                            LogUtil.d(uri.getPath());
                            picPath = FileTool.getImagePathFromUri(this, uri);
                        } else {
                            if (ptuAction == null || PTU_ACTION_NORMAL.equals(ptuAction)) {
                                // ???????????????
                                picPath = sourceIntent.getStringExtra(INTENT_EXTRA_PIC_PATH);
                            } else if (PtuActivity.PTU_ACTION_LATEST_PIC.equals(ptuAction)) {
                                // ????????????????????????????????????
                                US.putOtherEvent(US.USE_NOTIFY_LATEST_PIC);
                                runOnUiThread(this::showLoadPicLoading);
                                Pair<String, Boolean> latestPair = MediaInfoScanner.getLatestPicPath();
                                picPath = latestPair.first;
                                if (latestPair.second != null && latestPair.second) {
                                    picLoadMode = PtuUtil.VIDEO_2_GI;
                                }
                            } else if (PtuActivity.PTU_ACTION_VIDEO_MAKE_GIF.equals(ptuAction)) {
                                picLoadMode = PtuUtil.VIDEO_2_GI;
                                picPath = sourceIntent.getStringExtra(INTENT_EXTRA_VIDEO_PATH);
                            } else if (PTU_ACTION_PICS_MAKE_GIF.equals(ptuAction)) {
                                picLoadMode = PtuUtil.PIC_LIST_2_GIF;
                                picPath = PTU_ACTION_PICS_MAKE_GIF;
                            } else if (PTU_ACTION_AS_INTERMEDIATE_PTU.equals(ptuAction)) {
                                isIntermediatePtu = true;
                                picPath = sourceIntent.getStringExtra(INTENT_EXTRA_PIC_PATH);
                            }
                        }

                        if (picPath == null) {
                            emitter.onError(new Exception("pic path is null"));
                            return;
                        }
                    } else {
                        picPath = url;
                    }
                    originalPath = picPath;
                    // second??????????????????URL????????????????????????
                    if (FileTool.urlType(picPath) == FileTool.UrlType.URL) {
                        try {
                            // ????????????????????????????????????????????????????????????????????????????????????????????????
                            urlPicSuffix = FileTool.getSuffix(picPath);
                            if (urlPicSuffix == null) {
                                urlPicSuffix = BitmapUtil.DEFAULT_SAVE_SUFFIX;
                            }

                            picPath = Glide.with(PtuActivity.this)
                                    .load(picPath)
                                    .downloadOnly(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                                    .get()
                                    .getAbsolutePath();
                            isLoadFromGlideCache = true;
                        } catch (Exception e) {
                            emitter.onError(new Exception("get path of url failed"));
                            return;
                        }
                    }

                    if (picPath != null && picPath.equals(oldPath)) { // ??????????????????????????????????????????
                        afterLoadSuccess(isReplace, null);
                        emitter.onComplete();
                        return;
                    }
                    if (MyGifDecoder.isGif(picPath))
                        picLoadMode = PtuUtil.OPEN_GIF_FILE;

                    emitter.onNext(picPath);
                    emitter.onComplete();
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new SimpleObserver<Object>() {

                    @Override
                    public void onNext(Object picData) {
                        prepareLoadPicData(picLoadMode, picData, isReplace);
                    }

                    @Override
                    public void onError(Throwable e) {
                        onLoadError(e, isReplace);
                    }
                });

    }

    private void prepareLoadPicData(int picOpenMode, Object srcData, boolean isReplace) {
        // ???????????????????????????????????????????????????????????????????????????post???????????????UI?????????????????????
        ptuFrame.post(() -> {
            totalBound.set(0, 0, ptuFrame.getWidth(), ptuFrame.getHeight());
            ptuSeeView.setTotalBound(totalBound);

            // ??????GIF
            if (picOpenMode == PtuUtil.OPEN_GIF_FILE
                    || picOpenMode == PtuUtil.PIC_LIST_2_GIF
                    || picOpenMode == PtuUtil.BM_LIST_2_GIF
                    || picOpenMode == PtuUtil.VIDEO_2_GI) {
                if (gifManager != null) { // ???????????????gifManager?????????????????????
                    removeGifView(); // ????????????????????????????????????????????????
                    gifManager.destroy();
                    gifManager = null;
                }
                gifManager = new GifManager(PtuActivity.this);
                initGifView(gifManager);
                loadGifData(picOpenMode, srcData, isReplace);
            } else {
                if (gifManager != null) { // ?????????gif????????????gif?????????gif????????????
                    removeGifView();
                    gifManager.destroy();
                    gifManager = null;
                }
                loadBitmapData(isReplace);
            }
        });
    }

    /**
     * @param isReplace ?????????????????????????????????
     */
    private void loadBitmapData(boolean isReplace) {
        Observable
                .create((ObservableOnSubscribe<Bitmap>) emitter -> {
                    int maxSupportSize = isStyle ?
                            (int) (AllData.globalSettings.maxSupportContentSize * AllData.globalSettings.styleContentRatio)
                            : AllData.globalSettings.maxSupportContentSize;
                    maxSupportSize *= maxSupportSize;
                    Bitmap bm = BitmapUtil.decodeLossslessInSize(picPath, maxSupportSize);
                    if (bm == null) {
                        emitter.onError(new Exception("??????????????????"));
                        return;
                    }
                    emitter.onNext(bm);
                    LogUtil.d("???????????????Bitmap??????");
                    emitter.onComplete();
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new SimpleObserver<Bitmap>() {

                    @Override
                    public void onError(Throwable throwable) {
                        onLoadError(throwable, isReplace);
                    }

                    @Override
                    public void onNext(@NotNull Bitmap bitmap) {
                        //TODO ??????????????????
                        if (isDestroyed()) return;
                        if (isStyle)
                            showPtuNotice(getString(R.string.choose_content_for_style));
                        else {
                            ptuSeeView.setBitmapAndInit(bitmap, totalBound);
                            if (ptuSeeView.getSourceBm() != null) { // ????????????????????????????????????
                                repealRedoManager.setBaseBm(bitmap.copy(Bitmap.Config.ARGB_8888, true));
                            } else {
                                LogUtil.e("??????PTuSeeView???????????????");
                            }
                        }
                        afterLoadSuccess(isReplace, bitmap);
                        LogUtil.d("????????????Bitmap??????");
                    }
                });
    }

    private void initGifView(@NotNull GifManager gifManager) {
        if (mainFrag != null) {
            mainFrag.switchFunctionFrag(true, true);
        }
        if (picLoadMode == PtuUtil.PIC_LIST_2_GIF) {
            initProgress("???????????????...", -1);
        } else if (picLoadMode == PtuUtil.VIDEO_2_GI) {
            initProgress("???????????????, ?????????...", -1);
        } else {
            initProgress("??????GIF???...", -1);
        }
        showProgressUiThread(0);
        View gifOperationLayout = findViewById(R.id.gif_operation_layout);
        gifManager.initView(gifOperationLayout);
        FirstUseUtil.gifPreviewGuide(this, findViewById(R.id.gif_play_all), findViewById(R.id.gif_operation_layout));
    }

    private void removeGifView() {
        findViewById(R.id.gif_operation_layout).setVisibility(View.GONE);
        if (mainFrag != null) {
            mainFrag.switchFunctionFrag(false, true);
        }
    }

    private void loadGifData(int picOpenMode, Object srcData, boolean isReplace) {
        if (picOpenMode == PtuUtil.VIDEO_2_GI) {
            // ????????????????????????????????????????????????
            judgeShowAd(picPath);
        }
        Observable
                .create((ObservableOnSubscribe<GifManager>) emitter -> {
                    try {
                        if (picOpenMode == PtuUtil.VIDEO_2_GI) {
                            // ????????????????????????????????????????????????
                            gifManager.loadGifData(picPath, true, this);
                        } else if (picOpenMode == PtuUtil.PIC_LIST_2_GIF) {
                            ArrayList<String> gifPicList = getIntent().getStringArrayListExtra(PTU_DATA_GIF_PIC_LIST);
                            gifManager.loadFromPicList(gifPicList, this);
                        } else if (picOpenMode == PtuUtil.BM_LIST_2_GIF) {
                            gifManager.loadFromGifFrames((List<GifFrame>) srcData, this);
                        } else {
                            gifManager.loadGifData(picPath, false, this);
                        }
                        if (isDestroyed()) return; // ??????activity?????????gifManager??????????????????
                        // Logcat.d("??????gif????????????");
                        emitter.onNext(gifManager);
                        emitter.onComplete();
                    } catch (Exception e) {
                        emitter.onError(e);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new SimpleObserver<GifManager>() {

                    @Override
                    public void onError(Throwable throwable) {
                        onLoadError(throwable, isReplace);
                        MobclickAgent.reportError(PtuActivity.this, throwable);
                        throwable.printStackTrace();
                    }

                    @Override
                    public void onNext(GifManager gifManager) {
                        if (isDestroyed()) return;
                        gifManager.initPtuSeeView(ptuSeeView, totalBound);
                        afterLoadSuccess(isReplace, null);
                    }
                });
    }

    private void judgeShowAd(String picPath) {
        // ???????????????????????????????????????????????????????????????????????????????????????????????????
        Integer integer = MediaInfoScanner.getInstance().getShortVideoMap().get(picPath);
        int duration = integer != null ? integer : 0;
        int maxDur = 14;
        if (AllData.globalSettings.performanceYear > YearClass.PERFORMANCE_6G_8G) {
            maxDur = 12;
        } else if (AllData.globalSettings.performanceYear > YearClass.PERFORMANCE_4G_6G) {
            maxDur = 10;
        } else if (AllData.globalSettings.performanceYear > YearClass.PERFORMANCE_2G_4G) {
            maxDur = 8;
        } else if (AllData.globalSettings.performanceYear < YearClass.PERFORMANCE_2G_4G) {
            maxDur = 6;
        }
        // ????????????????????????????????????????????????????????????
        if (System.currentTimeMillis() < AdData.lastVideoAdShowTime + AdData.VIEDO_AD_SHOW_INTERVAL) {
            maxDur += 1.5;
        }
        maxDur *= 1000;
        if (duration >= maxDur && NetWorkState.detectNetworkType() != NetWorkState.NO_NET
                && !AdData.judgeAdClose(AdData.TT_AD)) {
            // new DialogChooseSeeAd().show(); ?????????
            ToastUtils.show(
                    getString(R.string.long_time_parse_video_so_ad),
                    Toast.LENGTH_LONG);
            getActivityViewRoot().postDelayed(() ->
                    FullScreenVadManager.showFullScreenVad(PtuActivity.this, null,
                            getString(R.string.long_time_parse_video_so_ad)), 2500);
        }
    }

    private void afterLoadSuccess(boolean isReplace, Bitmap bm) {
        if (!isReplace) { // ??????????????????
            int toChildFunction = getIntent().getIntExtra(INTENT_EXTRA_TO_CHILD_FUNCTION, -1);
            if (PtuUtil.isSecondEditMode(toChildFunction)) {
                switchFragment(toChildFunction, null);
            } else if (PtuUtil.CHILD_FUNCTION_GIF == toChildFunction) {
                switchFragment(EDIT_GIF, null);
            } else {
                TransferController transferController = new TransferController(
                        isStyle ? null : bm,
                        isStyle ? null : originalPath,
                        isStyle ? bm : null,
                        isStyle ? originalPath : null);
                switchFragment(EDIT_TRANSFER, transferController);
//                getVggFeature(bm, true);
            }
        }
        loadFinish();
    }

    private void onLoadError(Throwable e, boolean isReplace) {
        if (e != null) {
            e.printStackTrace();
        }
        if (isDestroyed()) return;
        loadFinish();
        String msg = getString(R.string.load_pic_failed_not_exit_or_format_error);
        if (!isReplace) {
            AlertDialog.Builder builder = new AlertDialog.Builder(PtuActivity.this);
            builder.setTitle(msg);
            builder.setPositiveButton("??????", (dialog, which) -> {
                dialog.dismiss();
                Intent resultIntent = new Intent();
                resultIntent.setAction(LOAD_FAILED);
                resultIntent.putExtra(PTuResultData.FAILED_PATH, picPath);
                setResult(0, resultIntent);
                finish();
            });
            Dialog dialog = builder.create();
            dialog.setCancelable(false);
            dialog.show();
        } else {
            ToastUtils.show(this, msg);
        }
    }

    /**
     * ???????????????????????????????????????????????????
     * ??????
     */
    private void loadFinish() {
        mIsInLoading = false;
        judgeShowBannerAd();
        dismissLoading();
        dismissProgress();
    }

    private void judgeShowBannerAd() {
        if (bannerAd != null) return; // ????????????????????????????????????????????????????????????app????????????????????????????????????????????????
        if (AllData.isVip) return;
        Rect picBound = ptuSeeView.getPicBound();
        if (!AdData.judgeAdClose() && picBound.width() > PtuSeeView.REPLACE_LEN // ???????????????????????????
                && totalBound.height() - picBound.height() > totalBound.width() / TxBannerAd.BANNER_WH_RATIO * 1.8
                && findViewById(R.id.ptu_notice_layout).getVisibility() != View.VISIBLE) {
            if (AllData.sRandom.nextDouble() < 1 / 6f) {
                bannerAd = new TxBannerAd(this, findViewById(R.id.banner_ad_container), totalBound.width());
                bannerAd.show();
            }
        }
    }

    @Override
    public void replaceBase(String url) {
        // url ?????????????????????????????????
        if (url == null) return;
        startLoadpic(url);
        // afterSure(new );
    }

    @Override
    public void showTouchPEnlargeView(Bitmap srcBm, float effect_w, float xInEnlargeParent, float yInEnlargeParent) {
        if (bannerAd != null && srcBm != null) {
            bannerAd.destroy();
            bannerAd = null;
        }
        findViewById(R.id.ptu_notice_layout).setVisibility(View.GONE);
        ptuFrame.showTouchPEnlargeView(srcBm, effect_w, xInEnlargeParent, yInEnlargeParent);
    }

    private void showLoadPicLoading() {
        showLoading(getString(R.string.loading));
    }

    /************************************* fragment??????   *****************************************/

    @Override
    public void switchFragment(int function, @Nullable SecondFuncController secondFuncControl) {
        if (function == currentEditMode) return;
        lastFrag = currentFrag;
        if (function == EDIT_MAIN) {
            if (mainFrag == null) {
                mainFrag = new MainFunctionFragment();
                mainFrag.setPTuActivityInterface(this);
            }
            fm.beginTransaction()
                    .setCustomAnimations(R.animator.slide_bottom_in, R.animator.slide_bottom_out,
                            R.animator.slide_bottom_in, R.animator.slide_bottom_out)
                    .replace(R.id.fragment_main_function, mainFrag)
                    .commitAllowingStateLoss();
            currentEditMode = EDIT_MAIN;
            currentFrag = mainFrag;
            ptuSeeView.switchStatus2Main();
            mPtuToolbar.switchToolbarBtn(false);
            mPtuToolbar.setRightTopText(getString(R.string.finish));
            mPtuToolbar.setLeftTopText(getString(R.string.cancel));
            judgeShowBannerAd();
        } else if (function == EDIT_TRANSFER) {
            mPtuToolbar.switch2Transfer();
            mPtuToolbar.setRightTopText(getString(R.string.save_add));
            mPtuToolbar.setLeftTopText(getString(R.string.the_return));
            switch2Transfer(secondFuncControl instanceof TransferController ? (TransferController) secondFuncControl : null);
        } else {
            ui2SecondFunction();
            ptuSeeView.resetShow();
            switch (function) {
                case EDIT_CUT:
                    switch2Cut();
                    break;
                case EDIT_TEXT:
                    switch2Text();
                    break;
                case EDIT_TIETU:
                    switch2Tietu(secondFuncControl instanceof TietuController ? (TietuController) secondFuncControl : null);
                    break;
                case EDIT_DRAW:
                    switch2Draw(secondFuncControl instanceof DrawController ? (DrawController) secondFuncControl : null);
                    break;
                case EDIT_DIG:
                    switch2Dig(secondFuncControl instanceof DigController ? (DigController) secondFuncControl : null);
                    break;
                case EDIT_REND:
                    switch2Rend();
                    break;
                case EDIT_DEFORMATION:
                    switch2Deformation();
                    break;
                case EDIT_GIF:
                    switch2GifEdit();
                    break;

            }
        }
        //        PTuLog.d(TAG, "switchFragment??????");
    }

    /**
     * ????????????
     */
    private void switch2Draw(DrawController drawController) {
        if (drawFrag == null) {
            drawFrag = new DrawFragment();
            drawFrag.setPTuActivityInterface(this);
        }
        drawFrag.initBeforeCreateView(drawController, ptuFrame);
        //?????????????????????
        fm.beginTransaction()
                .setCustomAnimations(R.animator.slide_bottom_in, R.animator.slide_bottom_out,
                        R.animator.slide_bottom_in, R.animator.slide_bottom_out)
                .replace(R.id.fragment_main_function, drawFrag)
                .commitAllowingStateLoss();
        FrameLayout.LayoutParams drawFloatParams =
                new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        drawFloatParams.setMargins(ptuSeeView.getDstRect().left, ptuSeeView.getDstRect().top, ptuSeeView.getDstRect().left, ptuSeeView.getDstRect().top);
        //???????????????????????????
        ptuFrame.addView(drawFrag.createDrawView(this, totalBound, ptuSeeView), drawFloatParams);
        currentFrag = drawFrag;
        currentEditMode = EDIT_DRAW;
        ptuSeeView.switch2Draw();
    }

    private void switch2Tietu(TietuController secondFuncControl) {
        if (tietuFrag == null) {
            tietuFrag = new TietuFragment();
            tietuFrag.setPTuActivityInterface(this);
        }
        tietuFrag.initBeforeCreateView(ptuFrame, ptuSeeView, secondFuncControl);

        fm.beginTransaction()
                .setCustomAnimations(R.animator.slide_bottom_in, R.animator.slide_bottom_out,
                        R.animator.slide_bottom_in, R.animator.slide_bottom_out)
                .replace(R.id.fragment_main_function, tietuFrag)
                .commitAllowingStateLoss();
        currentEditMode = EDIT_TIETU;
        currentFrag = tietuFrag;
    }

    private void switch2Text() {
        if (textFrag == null) {
            textFrag = new TextFragment();
            textFrag.setPTuActivityInterface(this);
        }
        fm.beginTransaction()
                .setCustomAnimations(R.animator.slide_bottom_in, R.animator.slide_bottom_out,
                        R.animator.slide_bottom_in, R.animator.slide_bottom_out)
                .replace(R.id.fragment_main_function, textFrag)
                .commitAllowingStateLoss();
        currentEditMode = EDIT_TEXT;
        textFrag.addRubberView(this, ptuFrame);
        FloatTextView floatTextView = ptuFrame.initAddTextFloat(ptuSeeView.getPicBound());
        // TODO: 2020/4/9 ?????????gif????????????????????????Ptu???view???????????????????????????gif???????????????????????????
        //  ??????????????????????????????????????????????????????????????????ptu???View
        if (gifManager != null && ptuSeeView.getPicBound().left > 0) {
            floatTextView.setExtraBottomMargin(getResources().getDimensionPixelSize(R.dimen.gif_frame_height));
        }
        textFrag.setFloatView(floatTextView);
        currentFrag = textFrag;
    }

    private void switch2Cut() {
        if (cutFrag == null) {
            cutFrag = new CutFragment();
            cutFrag.setPTuActivityInterface(this);
        }
        fm.beginTransaction()
                .setCustomAnimations(R.animator.slide_bottom_in, R.animator.slide_bottom_out,
                        R.animator.slide_bottom_in, R.animator.slide_bottom_out)
                .replace(R.id.fragment_main_function, cutFrag)
                .commitAllowingStateLoss();
        FrameLayout.LayoutParams cutFloatParams =
                new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        cutFloatParams.setMargins(0, 0, 0, 0);
        if (gifManager != null) {
            gifManager.stopAnimation(); // ??????????????????????????????gif??????
        }
        ptuFrame.addView(
                cutFrag.createCutView(this, totalBound, ptuSeeView.getSourceBm())
                , cutFloatParams);
        currentFrag = cutFrag;
        currentEditMode = EDIT_CUT;
    }

    private void switch2Rend() {
        if (rendFrag == null) {
            rendFrag = new RendFragment();
            rendFrag.setPTuActivityInterface(this);
            // ??????????????????????????????????????????Fragement??????
            rendFrag.setRendActionListener(new RendFragment.RendActionListener() {
                @Override
                public void goToAddText() {
                    sure(() -> switchFragment(EDIT_TEXT, null));
                }

                @Override
                public void goToEdit() {
                    sure(() -> switchFragment(EDIT_CUT, null));
                }

                @Override
                public void changePicFormat(String suffix) {
                    mSaveSuffix = suffix;
                }
            });
        }
        rendFrag.initBeforeCreateView(ptuFrame, ptuSeeView, totalBound, picPath);

        fm.beginTransaction()
                .setCustomAnimations(R.animator.slide_bottom_in, R.animator.slide_bottom_out,
                        R.animator.slide_bottom_in, R.animator.slide_bottom_out)
                .replace(R.id.fragment_main_function, rendFrag)
                .commitAllowingStateLoss();
        ptuSeeView.switchStatus2Rend();
        currentEditMode = EDIT_REND;
        currentFrag = rendFrag;
    }

    private void switch2Dig(DigController digController) {
        if (digFrag == null) {
            digFrag = new DigFragment();
            digFrag.setPTuActivityInterface(this);
            digFrag.setDigActionListener(new DigFragment.DigActionListener() {
                @Override
                public void toMakeBaozouFace(StepData sd) {
                    digSwitch2Tie(sd);
                }
            });
        }
        digFrag.initBeforeCreateView(digController);
        fm.beginTransaction()
                .setCustomAnimations(R.animator.slide_bottom_in, R.animator.slide_bottom_out,
                        R.animator.slide_bottom_in, R.animator.slide_bottom_out)
                .replace(R.id.fragment_main_function, digFrag)
                .commitAllowingStateLoss();

        //????????????
        // if (!isMakeTietu) {
        //     mPtuToolbar.removeSure();
        //     mPtuToolbar.addSureTvLayout();
        // }
        FrameLayout.LayoutParams floatParams =
                new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        ptuFrame.addView(digFrag.createDigView(PtuActivity.this, totalBound, ptuSeeView.getSourceBm()), floatParams);
        ptuSeeView.switchStatus2Dig();
        currentEditMode = EDIT_DIG;
        currentFrag = digFrag;
    }

    private void switch2Deformation() {
        if (deformationFrag == null) {
            deformationFrag = new DeformationFragment();
            deformationFrag.setPTuActivityInterface(this);
            deformationFrag.setDeforActionListener(new DeformationFragment.DeforActionListener() {
                @Override
                public void deforComposeGif(List<GifFrame> bmList) {
                    prepareLoadPicData(PtuUtil.BM_LIST_2_GIF, bmList, true);
                    repealRedoManager.setOperateStart(-2);
                    returnMain();
                }
            });
        }
        fm.beginTransaction()
                .setCustomAnimations(R.animator.slide_bottom_in, R.animator.slide_bottom_out,
                        R.animator.slide_bottom_in, R.animator.slide_bottom_out)
                .replace(R.id.fragment_main_function, deformationFrag)
                .commitAllowingStateLoss();
        FrameLayout.LayoutParams params =
                new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        params.setMargins(0, 0, 0, 0);
//        if (gifManager != null) {
//            gifManager.stopAnimation(); // ??????????????????????????????gif??????
//        }
        ptuFrame.addView(
                deformationFrag.createDeformationView(this, ptuSeeView)
                , params);
        currentFrag = deformationFrag;
        currentEditMode = EDIT_DEFORMATION;
    }


    private void switch2Transfer(TransferController transferController) {
        if (transferFrag == null) {
            transferFrag = new StyleTransferFragment();
            transferFrag.setPTuActivityInterface(this);
        }
        //?????????????????????
        transferFrag.initBeforeCreateView(transferController);
        fm.beginTransaction()
                .setCustomAnimations(R.animator.slide_bottom_in, R.animator.slide_bottom_out,
                        R.animator.slide_bottom_in, R.animator.slide_bottom_out)
                .replace(R.id.fragment_main_function, transferFrag)
                .commitAllowingStateLoss();

        currentFrag = transferFrag;
        currentEditMode = EDIT_TRANSFER;
//        ptuSeeView.switchStatus2Main();
    }

    private void switch2GifEdit() {
        if (gifEditFrag == null) {
            gifEditFrag = new GifEditFragment();
            gifEditFrag.setPTuActivityInterface(this);
        }
        fm.beginTransaction()
                .setCustomAnimations(R.animator.slide_bottom_in, R.animator.slide_bottom_out,
                        R.animator.slide_bottom_in, R.animator.slide_bottom_out)
                .replace(R.id.fragment_main_function, gifEditFrag)
                .commitAllowingStateLoss();
        currentFrag = gifEditFrag;
        currentEditMode = EDIT_GIF;
    }


    private void digSwitch2Tie(StepData sd) {
        mSaveSuffix = BitmapUtil.PIC_SUFFIX_PNG; // ?????????????????????????????????PNG
        switchOn2LevelFunction();
        TietuController secondFuncControl = new TietuController();
        secondFuncControl.isChangeFace = true;
        secondFuncControl.needSaveTietu = true;
        secondFuncControl.tietuUrl = sd.picPath;
        switchFragment(EDIT_TIETU, secondFuncControl);
    }

    /**
     * ??????????????????????????????????????????
     * ?????? ???????????????????????????
     */
    private void ui2SecondFunction() {
//        mPtuToolbar.removeReturn();
//        mPtuToolbar.removeSaveSet();
//        mPtuToolbar.addCancel();
//        mPtuToolbar.addSure();
        mPtuToolbar.switchToolbarBtn(true);
        mPtuToolbar.updateRedoBtn(false);
        mPtuToolbar.updateRepealBtn(false);
    }

    /******************************* ?????????????????? *******************************/

    /**
     * ???????????????sure?????????????????????
     * ????????????????????????,??????????????????????????????
     * ???2??????????????????????????????????????????????????????putview??????
     * ??????sure???????????????????????????????????????????????????
     *
     * @param task ??????????????????????????????????????????????????????????????????????????????????????????????????????????????????
     */
    private void sure(@Nullable Runnable task) {
        final String NULL_STEP_DATA = "NUll STEP_DATA";
        showLoading(getString(R.string.generating));
        //????????????????????????????????????????????????,????????????,
        // ?????????????????????????????????????????????????????????????????????????????????????????????????????????
        currentFrag.generateResultDataInMain(1);
        ptuSeeView.post(() -> Observable
                .create(((ObservableOnSubscribe<StepData>) emitter -> {
                    // ????????????????????????????????????????????????
                    StepData sd = currentFrag.getResultDataAndDraw(1);
                    if (sd != null && sd.hasTransparency) {
                        mSaveSuffix = BitmapUtil.PIC_SUFFIX_PNG;
                    }
                    if (sd == null) {  // emitter ?????????NUll???????????????
                        emitter.onError(new Exception(NULL_STEP_DATA));
                    } else {
                        emitter.onNext(sd);
                        emitter.onComplete();
                    }
                }))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<StepData>() {
                    @Override
                    public void onNext(StepData sd) {
                        // ?????????????????????????????????????????????????????????
                        dismissLoading();
                        if (gifManager != null) {
                            gifManager.notifyFrameChanged();
                        }
                        // ??????????????????????????????
                        if (sd instanceof DigStepData && ((DigStepData) sd).isGoChooseBase) {
                            DigStepData dsd = (DigStepData) sd;
                            Runnable realTask = null;
                            realTask = () -> {
                                goChooseBaseAnimation(dsd);
                            };
                            // ????????? ????????????????????????
                            // ????????? ??????????????????
                            commitRRData_ReturnMain(sd, realTask);
                        } else {
                            commitRRData_ReturnMain(sd, task);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {  // emitter?????????NUll???????????????
                        super.onError(e);
                        dismissLoading();
                        if (gifManager != null) {
                            gifManager.notifyFrameChanged();
                        }
                        commitRRData_ReturnMain(null, task);
                    }
                }));
    }

    private void goChooseBaseAnimation(StepData stepData) {
        View contentView = LayoutInflater.from(this).inflate(R.layout.layout_go_chose_base, null);
        pop = new PopupWindow(contentView,
                WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT, true);
        pop.setTouchable(true);
        // ???????????????PopupWindow?????????????????????????????????????????????Back????????????dismiss??????
        // ??????????????????API?????????bug
        pop.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.background_circle_cornerl));

        //???????????????????????????
        //??????????????????????????????show,??????????????????!!!!
        pop.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        pop.showAtLocation(getActivityViewRoot(), Gravity.RIGHT | Gravity.BOTTOM, Util.dp2Px(16), Util.dp2Px(100));

        Util.fadeOut(contentView, 6600, new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                contentView.setVisibility(View.GONE);
                pop.dismiss();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        View goChoseBaseTv = contentView.findViewById(R.id.go_chose_base_tv);
        // goChoseBaseTv.setAnimation();
        goChoseBaseTv.setOnClickListener(v -> {
            if (stepData == null) return;
            finallySaveResult(1, null,
                    AllData.getPTuBmPool().get(stepData.picPath));
            TietuController secondFuncControl = new TietuController();
            secondFuncControl.needChooseBase = true;
            secondFuncControl.tietuUrl = stepData.picPath;
            switchFragment(PtuUtil.EDIT_TIETU, secondFuncControl);
            pop.dismiss();
        });
    }

    /**
     * ???????????????????????????????????????????????????BaseBitmap????????????
     * ???????????????????????????
     *
     * @param task ?????????????????????????????????????????????
     */
    private void commitRRData_ReturnMain(@Nullable StepData sd, @Nullable Runnable task) {
        if (sd != null && gifManager == null) {
            StepData resd = repealRedoManager.commit(sd);
            if (resd != null)//??????????????????????????????????????????????????????
                addBigStep(resd);
            LogUtil.d(TAG, "??????????????????????????????");
        }
        returnMain();
        if (task != null) {
            task.run();
        }
    }

    /**
     * ?????????????????????????????????????????????back????????????sure????????????????????????????????????
     */
    private void returnMain() {
        if (lastFrag != null && lastFrag.isRemoving()) return;
        if (currentFrag != null) {
            currentFrag.releaseResource();
        }
        checkRepealRedo();
        int count = ptuFrame.getChildCount();
        for (int i = count - 1; i > 0; i--) {
            //????????????PtuView???????????????
            ptuFrame.removeViewAt(i);
        }
        if (ptuSeeView.getVisibility() != View.VISIBLE) {
            ptuSeeView.setVisibility(View.VISIBLE);
        }

        if (gifManager != null) {
            gifManager.preview();
        }
        ptuSeeView.resetShow();
        switchFragment(EDIT_MAIN, null);
    }

    /**
     * ??????????????????????????????????????????????????????
     */
    private void switchOn2LevelFunction() {
        if (lastFrag != null && lastFrag.isRemoving()) return;
        if (currentFrag != null) {
            currentFrag.releaseResource();
        }
        int count = ptuFrame.getChildCount();
        for (int i = count - 1; i > 0; i--) {
            //????????????PtuView???????????????
            ptuFrame.removeViewAt(i);
        }
        if (ptuSeeView.getVisibility() != View.VISIBLE) {
            ptuSeeView.setVisibility(View.VISIBLE);
        }
    }
// 2020???3???17?????? 2.3.0???????????????????????????
//    private void goSend() {
    //????????????
//        US.putSaveAndShareEvent(US.SAVE_AND_SHARE_GO_SEND);
//        if (mCurrentEditMode != EDIT_MAIN)
//            sure(() -> finallySaveResult(1, true, null, null));
//    }

    private void saveSet() {
        // new Thread(() -> {
        //     AdData.prepareAdForPtuResult(PtuActivity.this);
        // }).start();
        if (saveSetManager == null)
            saveSetManager = saveSetInstance.getSaveShare_DialogManager_Instance(this);
        assert saveSetManager != null; // ??????????????????????????????
        saveSetManager.init(BitmapUtil.getSize(ptuSeeView.getSourceBm()));
        saveSetManager.createDialog(this);
        saveSetManager.setClickListener(new SaveShareManager.clickListenerInterface() {

            /**
             * @param saveRatio ?????????????????????????????????????????????????????????????????????activity
             */
            @Override
            public void saveResult(float saveRatio) {
                //????????????
                US.putSaveAndShareEvent(US.SAVE_AND_SHARE_SAVE);
                finallySaveResult(saveRatio, null, null);
            }

            @Override
            public void mCancel() {
            }

            @Override
            public void onShareItemClick(float saveRatio, SaveShareManager.ShareTask shareTask) {
                saveSetInstance.hasShared = true;
                finallySaveResult(saveRatio, shareTask, null);
            }
        });
    }

    private void finishIntermediatePtu() {
        finallySaveResult(1, null, null);
    }

    /**
     * ???????????????????????????????????????????????????????????????????????????????????????????????????????????????
     * ???????????????????????? ??????????????????????????????????????????????????????
     *
     * @param digBm ??????????????????????????????????????????????????????????????????
     */
    private void finallySaveResult(float saveRatio,
                                   @Nullable SaveShareManager.ShareTask shareTask,
                                   @Nullable Bitmap digBm) {
        // ?????????????????????????????????????????????????????????
        // ??????????????????????????????????????????????????????????????????????????????????????????
        if (digBm == null) {
            if (gifManager == null) {
                showLoading(getString(R.string.saving));
            } else {
                showLoading(getString(R.string.composing_gif));
            }
        }
        Observable
                .create((ObservableOnSubscribe<String>)
                        emitter -> {
                            // first ?????????????????????
                            String tempSavePath;
                            @NotNull String finalSavePath;

                            // ?????????????????????????????????glide?????????????????????????????????????????????????????????
                            // ????????????????????????
                            if (isLoadFromGlideCache) {
                                tempSavePath = FileTool.createPicturePathDefault(picPath, urlPicSuffix);
                            } else {
                                tempSavePath = FileTool.createPicturePathDefault(picPath);
                            }
                            if (tempSavePath == null) {
                                // Toast.makeText(this, "??????SD???????????????", Toast.LENGTH_LONG).show();
                                throw new IOException();
                            }

                            // second ????????????
                            // ???????????????????????????????????????
                            // ??????????????????????????????????????????
                            if (gifManager == null) {
                                finalSavePath = saveResultBm(saveRatio, tempSavePath, digBm); // ?????????????????????Path
                            } else {
                                if (!tempSavePath.endsWith(".gif")) {
                                    tempSavePath = FileTool.replaceSuffix(tempSavePath, ".gif");
                                }
                                finalSavePath = tempSavePath;
                                gifManager.saveResult(finalSavePath);
                            }
                            emitter.onNext(finalSavePath);
                            emitter.onComplete();
                        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<String>() {
                    @Override
                    public void onNext(@NotNull String finalSavePath) {
                        repealRedoManager.lastSavePath = finalSavePath;
                        Bundle bundle = new Bundle();
                        if (shareTask != null) { //??????
                            shareTask.share(finalSavePath);
                        } else if (isIntermediatePtu) {
                            bundle.putString(PTuResultData.NEW_PIC_PATH, finalSavePath);
                            setReturnResultAndFinish(PTuResultData.FINISH_INTERMEDIATE_PTU, bundle);
                        } else if (digBm != null) { // ??????????????????????????????
                            AllData.getThreadPool_single().execute(() ->
                                    MyDatabase.getInstance().insertMyTietu(finalSavePath, System.currentTimeMillis()));
                            // TODO: 2019/6/13 ??????Path?????????
                            // AllData.getPTuBmPool().putBitmap(finalSavePath, digBm);
                            FirstUseUtil.firstDig(PtuActivity.this);
                        } else {
                            bundle.putString(PTuResultData.NEW_PIC_PATH, finalSavePath);
                            setReturnResultAndFinish(PTuResultData.SAVE_AND_LEAVE, bundle);
                        }
                        dismissLoading();
                    }

                    @Override
                    public void onError(Throwable e) {
                        LogUtil.e("??????P???????????????" + e);
                        ToastUtils.show(PtuActivity.this, e.getLocalizedMessage());
                        // if (shareTask != null) {
                        // } else {
                        // ????????????????????????????????????????????????P???????????????????????????????????????????????????
                        // setReturnResultAndFinish(PTuResultData.SAVE_FAILED_AND_LEAVE, null, false);
                        // }
                        dismissLoading();
                    }
                });
    }

    /**
     * @return ?????????????????????
     */
    @NotNull
    private String saveResultBm(final float saveRatio, String savePath, @Nullable Bitmap
            resultBm) throws IOException {
        BitmapUtil.SaveResult result;
        String saveSuffix;
        if (resultBm == null) {
            resultBm = ptuSeeView.getFinalPicture(saveRatio);
            saveSuffix = mSaveSuffix;
        } else {
            saveSuffix = BitmapUtil.PIC_SUFFIX_PNG;
        }
        result = BitmapUtil.saveBitmap(PtuActivity.this, resultBm, savePath, true, saveSuffix);

        if (BitmapUtil.SaveResult.SAVE_RESULT_FAILED.equals(result.result) || result.data == null) {
            // Toast.makeText(this, "??????SD???????????????", Toast.LENGTH_LONG).show();
            throw new IOException();
        }
        return result.data;
    }

    public void setReturnResultAndFinish(@NotNull String mAction, @Nullable Bundle bundle) {
        Intent resultIntent = new Intent(mAction);
        if (bundle == null) {
            bundle = new Bundle();
        }
        if (PTuResultData.FINISH_INTERMEDIATE_PTU.equals(mAction)) {
            resultIntent.putExtras(bundle);
            setResult(RESULT_CODE_INTERMEDIATE_PTU, resultIntent);
            finish();
            return;
        }
        resultIntent.putExtras(bundle);
        if (PTuResultData.SAVE_AND_LEAVE.equals(mAction)) {
            resultIntent.setComponent(new ComponentName(this, PTuResultActivity.class));
            startPTuResultAc(resultIntent);
        } else {
            setResult(RESULT_CODE_NORMAL_RETURN, resultIntent);
            finish();
        }

    }

    private void startPTuResultAc(Intent resultIntent) {
        resultIntent.putStringArrayListExtra(PTuResultData.INTENT_EXTRA_USED_TAGS_LIST, useTagsList);
        startActivityForResult(resultIntent, PTuResultActivity.REQUEST_CODE_PTU);
    }

    /**
     * ????????????????????????
     */
    private void certainLeavePage() {
        if (repealRedoManager.hasChange()) {
            CertainLeaveDialog certainLeaveDialog = new CertainLeaveDialog(PtuActivity.this);
            certainLeaveDialog.createDialog(null, null,
                    () -> setReturnResultAndFinish(PTuResultData.LEAVE, null));
        } else
            setReturnResultAndFinish(PTuResultData.LEAVE, null);
    }


    private void certainCancelEdit() {
        if (repealRedoManager.hasChange()) {
            CertainLeaveDialog certainLeaveDialog = new CertainLeaveDialog(PtuActivity.this);
            certainLeaveDialog.createDialog("?????????????????????, ??????????????????", " ",
                    () -> {
                        ptuSeeView.replaceSourceBm(repealRedoManager.getBaseBitmap());
                        switchFragment(EDIT_TRANSFER, null);
                    });
        } else
            switchFragment(EDIT_TRANSFER, null);
    }

    /******************************* ?????????????????? *******************************/

    /**
     * ?????????????????????????????????????????????ac-frag-???????????????View
     * ?????????????????????????????????????????????????????????????????????
     * ??????
     */
    private void smallRepeal() {
        if (currentFrag != null) {
            currentFrag.smallRepeal();
        }
    }

    /**
     * ???????????? ??????
     */
    private void smallRedo() {
        if (currentFrag != null) {
            currentFrag.smallRedo();
        }
    }

    /**
     * ??????,???????????????????????????????????????????????????
     */
    private void bigRepeal() {
        LogUtil.d(TAG, "??????????????????");
        LoadingDialog progressDialog = LoadingDialog.newInstance("");
        progressDialog.showIt(this);

        if (repealRedoManager.canRepeal()) {
            Bitmap baseBitmap = repealRedoManager.getBaseBitmap();
            if (baseBitmap == null) { // ???????????????????????????????????????????????????
                LogUtil.e("repeal redo base bm is null");
                return;
            }
            repealRedoManager.repealPrepare();
            ptuSeeView.releaseResource();
            Bitmap newSourceBm = baseBitmap.copy(Bitmap.Config.ARGB_8888, true);
            ptuSeeView.replaceSourceBm(newSourceBm);
            LogUtil.d(TAG, "???????????????????????????");
            // ???????????????????????????????????????????????????
            int index = repealRedoManager.getCurrentIndex();
            for (int i = 0; i <= index; i++) {
                StepData sd = repealRedoManager.getStepdata(i);
                addBigStep(sd);
            }
            LogUtil.d(TAG, "?????????????????????????????????");
            ptuSeeView.resetShow();
        }

        progressDialog.dismissAllowingStateLoss();
        checkRepealRedo();
    }

    public void bigRedo() {
        if (repealRedoManager.canRedo()) {
            StepData sd = repealRedoManager.redo();
            LoadingDialog progressDialog = LoadingDialog.newInstance("");
            progressDialog.showIt(this);
            addBigStep(sd);
            progressDialog.dismissAllowingStateLoss();
            checkRepealRedo();
        }
        //        PTuLog.d(TAG, "????????????");
    }

    /*************************************************************
     * repealRedo??????
     *************************************************/
    private void addBigStep(StepData sd) {
        switch (sd.EDIT_MODE) {
            case EDIT_CUT:
                CutFragment.addBigStep(sd, this);
                break;
            case EDIT_TEXT:
                TextFragment.addBigStep(sd, this);
                break;
            case EDIT_TIETU:
                TietuFragment.addBigStep(sd, this);
                break;
            case EDIT_DRAW:
                DrawFragment.addBigStep(sd, this);
                break;
            case EDIT_DIG:
                DigFragment.addBigStep(sd, this);
                break;
            case EDIT_REND:
                RendFragment.addBigStep(sd, this);
                break;
            case EDIT_DEFORMATION:
                DeformationFragment.addBigStep(sd, this);
                break;
            case EDIT_GIF:
                GifEditFragment.addBigStep(sd);
                break;

        }

    }

    /**
     * ????????????????????????,??????
     */
    private void checkRepealRedo() {
        mPtuToolbar.updateRedoBtn(repealRedoManager.canRedo());
        mPtuToolbar.updateRepealBtn(repealRedoManager.canRepeal());
    }

    /********************************* ?????? *********************************/

    @Override
    public void addUsedTags(boolean isTemplate, String resTags) {
        if (resTags != null && useTagsList.indexOf(resTags) == -1) {
            useTagsList.add(resTags);
            if (LogUtil.debugRecommend) {
                Log.d(TAG, "???????????????tag " + resTags);
            }
        }

    }

    /**
     *
     */
    @Override
    public void onBackPressed() {
        if (currentEditMode != EDIT_MAIN && currentEditMode != EDIT_TRANSFER) {
            if (currentFrag.onBackPressed(true)) return;
            returnMain();
        } else if (currentEditMode == EDIT_MAIN) {
            returnTransfer();
        } else {
            certainLeavePage();
        }
    }

    private void returnTransfer() {
        switchFragment(EDIT_TRANSFER, null);
    }

    private void test() {
        if (getIntent().getBooleanExtra(APPConstants.Test_FLAG, false)) {
            new Handler().postDelayed(() -> {
                Log.e(TAG, "??????????????????");
                switchFragment(EDIT_TIETU, null);
            }, 2500);
        }
    }

    public PtuFrameLayout getPtuFrame() {
        return ptuFrame;
    }

    @UiThread
    private void initProgress(String title, int max) {
        if (dialog == null) {
            dialog = LoadingDialog.newInstance(title);
            dialog.showIt(this);
        }
    }

    /**
     * ???????????????ui?????????????????????????????????
     */
    public void showProgressUiThread(int progress) {
        if (isDestroyed()) return;
        runOnUiThread(() -> {
            LogUtil.d(TAG, "?????????" + progress);
            if (dialog == null) {
                initProgress("?????????????????????...", -1);
            }
            if (!isDestroyed() && dialog.isShowing()) { // ??????????????????????????????????????????????????????
                dialog.setProgress(progress + "%");
            }
        });
    }

    @Override
    public RepealRedoManager<StepData> getRepealRedoManager() {
        return repealRedoManager;
    }

    @Override
    public void setMax(int max) {
//        if (dialog != null) {
//            dialog.setProgress(max + "");
//        }
    }

    @Override
    public void onProgress(int progress) {
        showProgressUiThread(progress);
    }

    public void dismissProgress() {
        if (dialog != null && !isDestroyed()) {
            dialog.dismissAllowingStateLoss();
            dialog = null;
        }
    }

    public void showLoading(String msg) {
        if (isDestroyed()) {
            return;
        }
        if (dialog != null) {
            dialog = null;
        }
        dismissProgress();
        dialog = LoadingDialog.newInstance(msg);
        dialog.setLoadingInfo(msg);
        dialog.showIt(this);
    }

    public void dismissLoading() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismissAllowingStateLoss();
        }
    }

    @Nullable
    public static Intent getStartAcIntent(Context context, PicResource picResource) {
        if (picResource.getUrl() != null) {
            String url = picResource.getUrl().getUrl();
            return getStartAcIntent(context, url, picResource);
        }
        return null;
    }

    public static Intent getStartAcIntent(Context context, String path, PicResource picResource) {
        Intent intent = new Intent(context, PtuActivity.class);
        intent.setAction(PtuActivity.PTU_ACTION_NORMAL);
        intent.putExtra(PtuActivity.INTENT_EXTRA_PIC_PATH, path);
        intent.putExtra(PtuActivity.INTENT_EXTRA_CHOSEN_TAGS, picResource.getTag());
        return intent;
    }

    @Override
    @NotNull
    public View getActivityViewRoot() {
        return findViewById(R.id.ptu_layout);
    }

    @Override
    public GifManager getGifManager() {
        return gifManager;
    }

    @Override
    public RepealRedoListener getRepealRedoListener() {
        return repealRedoListener;
    }

    @Override
    public PtuSeeView getPtuSeeView() {
        return ptuSeeView;
    }

    @Override
    public void setPresenter(Object presenter) {
    }

    @Override
    public String getBasePicPath() {
        return picPath;
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
}
