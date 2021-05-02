package com.mandi.intelimeditor.ptu.transfer;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mandi.intelimeditor.R;
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
import com.mandi.intelimeditor.common.util.Util;
import com.mandi.intelimeditor.common.util.WrapContentGridLayoutManager;
import com.mandi.intelimeditor.common.view.PtuConstraintLayout;
import com.mandi.intelimeditor.home.HomeActivity;
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
    private Button chooseContenBtn;
    private Button chooseStyleBtn;
    static final int bottonWidth = Util.dp2Px(20);
    private TransferController transferController;

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
        isChooseStyle = true;
        if (transferController != null && transferController.contentBm == null) {
            isChooseStyle = false;
        }
        chooseListAdapter = new TietuRecyclerAdapter(mContext, true);
        chooseListAdapter.setOnItemClickListener(chooseRcvListener);
        prepareShowChooseRcv(view, isChooseStyle);

        chooseStyleBtn = rootView.findViewById(R.id.choose_style);
        chooseContenBtn = rootView.findViewById(R.id.choose_pic);
        chooseContenBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isChooseStyle && chooseRcv.getParent() != null) { // 已经选择了内容，那么进入全部图片界面
                    chooseFromAllPic();
                } else {
                    chooseContent(v);
                }
            }
        });

        chooseStyleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isChooseStyle && chooseRcv.getParent() != null) {
                    chooseFromAllPic();
                } else {
                    chooseStyle(v);
                }
            }
        });
        if (transferController != null) {
            onChosenBm(transferController.contentBm, transferController.styleBm);
        }
        rootView.findViewById(R.id.go_ptu).setOnClickListener(v -> {
            pTuActivityInterface.switchFragment(PtuUtil.EDIT_MAIN, null);
        });
    }

    private RcvItemClickListener1 chooseRcvListener = new RcvItemClickListener1() {
        @Override
        public void onItemClick(RecyclerView.ViewHolder itemHolder, View view) {
            int position = itemHolder.getLayoutPosition();
            if (position < 0) return;
            if (position == 0) {
                chooseFromAllPic();
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

    private void chooseFromAllPic() {
        US.putPTuTietuEvent(US.PTU_TIETU_MORE);
        Intent intent = new Intent(mContext, HomeActivity.class);
        intent.setAction(HomeActivity.INTENT_ACTION_ONLY_CHOSE_PIC);
        intent.addCategory(isChooseStyle ? HomeActivity.CHOOSE_PIC_CATEGORY_STYLE : HomeActivity.CHOOSE_PIC_CATEGORY_CONTENT);
        intent.putExtra(HomeActivity.INTENT_EXTRA_FRAGMENT_ID, isChooseStyle ? HomeActivity.TEMPLATE_FRAG_ID : HomeActivity.LOCAL_FRAG_ID);
        startActivityForResult(intent, isChooseStyle ? PtuActivity.REQUEST_CODE_CHOOSE_STYLE : PtuActivity.REQUEST_CODE_CHOOSE_CONTENT);
    }

    @Override
    public void initData() {
        super.initData();
    }

    public void onChosenBm(Bitmap contentBm, Bitmap styleBm) {
        Resources resources = IntelImEditApplication.appContext.getResources();
        if (contentBm != null) {
            chooseContenBtn.setBackground(new BitmapDrawable(resources, getCircleBitmap(contentBm)));
        }
        if (styleBm != null) {
            chooseStyleBtn.setBackground(new BitmapDrawable(resources, getCircleBitmap(styleBm)));
        }
    }

    private void chooseContent(View view) {
        US.putPTuDeforEvent(US.PTU_DEFOR_EXAMPLE);
        isChooseStyle = false;
        prepareShowChooseRcv(view, isChooseStyle);
    }

    private void chooseStyle(View view) {
        US.putPTuDeforEvent(US.PTU_DEFOR_SIZE);
        isChooseStyle = true;
        prepareShowChooseRcv(view, isChooseStyle);
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
            chooseRcv.setAdapter(chooseListAdapter);
        }
        if (isChooseStyle) {
            prepareShowStyle(view);
        } else {
            prepareShowContentList();
        }
        if (isFirstShowChooseRcv) { // 本地贴图第一次加载显示不了，尝试多种办法不行，目前只能用这个
            view.post(() -> prepareShowStyle(view));
            view.post(() -> prepareShowStyle(view));
        }
        isFirstShowChooseRcv = false;
    }

    private void prepareShowContentList() {
        // 如果在注册之前完成扫描，那么不会收到事件, 不能在Frag的onCreate注册，View可能还没初始化
        EventBus.getDefault().register(this);
        // 如果在注册之后的这里完成扫描，那么会收到事件，会调用显示方法两次，但是比放到if判断后面有可能漏掉事件好
        if (AllData.hasInitScanLocalPic) {
            showStyleOrContenList(AllData.contentList);
        }
        // 如果在注册之后的这里完成扫描，调用显示方法一次
    }

    @Subscribe(threadMode = ThreadMode.MAIN, priority = 100)
    public void onEventMainThread(Integer event) {
        if (EventBusConstants.INIT_SCAN_LOCAL_PIC_FINISH.equals(event)) {
            showStyleOrContenList(AllData.contentList);
        }
    }

    private void prepareShowStyle(View view) {
        Observable
                .create((ObservableOnSubscribe<List<PicResource>>) emitter -> {
                    PicResourceDownloader.queryPicResByCategory("", PicResource.CATEGORY_STYLE, emitter);
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

    private void showStyleOrContenList(List<PicResource> list) {
        if (list == null) list = new ArrayList<>();
        chooseListAdapter.setList(list);
        chooseListAdapter.add(0, PicResource.path2PicResource(ALL_STYLE));
    }

    private void onNoStylePic() {
        if (chooseListAdapter != null) { // 这个方法会异步回调，此时tietuListAdapter已经回收置空了，原来自己就没处理，GG
            chooseListAdapter.setList(new ArrayList<>());
            String msg;
            msg = mContext.getString(R.string.no_network_style_notice);
            PtuUtil.onNoPicResource(msg);
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, final Intent data) {
        if (requestCode == PtuActivity.REQUEST_CODE_CHOOSE_STYLE && data != null) {
            PicResource picRes = (PicResource) data.getSerializableExtra(HomeActivity.INTENT_EXTRA_CHOSEN_PIC_RES);
            pTuActivityInterface.transfer(picRes.getUrlString(), true);
            showStyleOrContenList(AllData.curStyleList);
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

    public void initBeforeCreateView(TransferController transferController) {
        this.transferController = transferController;
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
