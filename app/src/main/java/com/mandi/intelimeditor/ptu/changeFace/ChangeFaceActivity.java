package com.mandi.intelimeditor.ptu.changeFace;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.transition.Transition;
import com.mandi.intelimeditor.common.BaseActivity;
import com.mandi.intelimeditor.common.RcvItemClickListener1;
import com.mandi.intelimeditor.common.appInfo.IntelImEditApplication;
import com.mandi.intelimeditor.common.dataAndLogic.AllData;
import com.mandi.intelimeditor.common.dataAndLogic.MyDatabase;
import com.mandi.intelimeditor.common.util.FileTool;

import com.mandi.intelimeditor.common.util.LogUtil;
import com.mandi.intelimeditor.common.util.SimpleGlideTarget;
import com.mandi.intelimeditor.common.util.SimpleObserver;
import com.mandi.intelimeditor.common.util.ToastUtils;
import com.mandi.intelimeditor.common.util.WrapContentGridLayoutManager;
import com.mandi.intelimeditor.common.view.DialogFactory;
import com.mandi.intelimeditor.home.HomeActivity;
import com.mandi.intelimeditor.home.search.SearchUtil;
import com.mandi.intelimeditor.ptu.PtuActivity;
import com.mandi.intelimeditor.ptu.PtuUtil;
import com.mandi.intelimeditor.ptu.saveAndShare.PTuResultData;
import com.mandi.intelimeditor.ptu.tietu.TietuSizeController;
import com.mandi.intelimeditor.ptu.tietu.onlineTietu.PicResource;
import com.mandi.intelimeditor.ptu.tietu.onlineTietu.PicResourceDownloader;
import com.mandi.intelimeditor.ptu.tietu.onlineTietu.TietuRecyclerAdapter;
import com.mandi.intelimeditor.user.US;
import com.mandi.intelimeditor.user.useruse.FirstUseUtil;
import com.mandi.intelimeditor.user.useruse.tutorial.GuideData;
import com.mandi.intelimeditor.home.search.SearchActivity;
import com.mandi.intelimeditor.R;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import USeruse.tutorial.GuideDialog;
import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import util.CoverLoader;

public class ChangeFaceActivity extends BaseActivity {

    public static final String INTENT_EXTRA_EXPRESSION_PATH = "EXPRESSION_PATH";
    public static final String INTENT_EXTRA_NEED_SAVE_FACE = "NEED_SAVE_FACE";
    public static final int REQUEST_CODE_CHOOSE_EXPRESSION_IN_SEARCH = 101;
    private static final int REQUEST_CODE_CHOOSE_FACE_PIC = 201;
    private static final int REQUEST_CODE_PTU_DIG_FACE = 202;

    private static final int REQUEST_CODE_CHOOSE_EXPRESSION_IN_HOME = 301;
    private static final int REQUEST_CODE_PTU_ERASE_EXPRESSION = 302;

    private ImageView faceIv;
    private ImageView expressionIv;
    private TietuRecyclerAdapter faceAdapter;
    private TietuRecyclerAdapter expressionAdapter;
    private RecyclerView chooseFaceRcv;
    private RecyclerView chooseExpressionRcv;
    private String faceUrl;
    private String expressionUrl;
    private boolean needSaveFace = false;
    private Switch colorStyleCb;

    @Nullable
    private LevelsAdjuster levelsAdjuster;
    private TextView goDigFaceTv;
    private TextView chooseExpressionTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bindView();
        initDataAndView();
        US.putMainFunctionEvent(US.MAIN_FUNCTION_CHANGE_FACE);
        if (!AllData.hasReadConfig.hasRead_changeFace_acGuide()) {
            List<String> keyWordList = Arrays.asList("换脸总述");
            GuideDialog.Companion.newInstance().setGuideAdapter(
                    GuideData.getGuideUseDataByType(keyWordList))
                    .showIt(this);
            AllData.hasReadConfig.put_changeFace_acGuide(true);
        }
    }

    @Override
    public int getLayoutResId() {
        return R.layout.activity_change_face;
    }

    @Override
    protected void onResume() {
        super.onResume();
        //        loadFaceData();
    }

    private void bindView() {
        faceIv = findViewById(R.id.face_iv);
        expressionIv = findViewById(R.id.expression_iv);
        chooseExpressionTv = findViewById(R.id.choose_expression);
        chooseFaceRcv = findViewById(R.id.choose_face_rcv);
        chooseExpressionRcv = findViewById(R.id.choose_expression_rcv);
        colorStyleCb = findViewById(R.id.bw_color_checkbox);
        goDigFaceTv = findViewById(R.id.go_dig_face_tv);
        colorStyleCb.setOnCheckedChangeListener((buttonView, isChecked) -> {
            onChangeColorStyle(isChecked);
        });
    }

    private void initDataAndView() {
        setExpression(getIntent().getStringExtra(INTENT_EXTRA_EXPRESSION_PATH));
        initFaceList();
        initExpression();
    }

    private void initFaceList() {
        faceAdapter = new TietuRecyclerAdapter(this, true);
        faceAdapter.setRowNumber(1);
        GridLayoutManager gridLayoutManager = new WrapContentGridLayoutManager(this, 1,
                GridLayoutManager.HORIZONTAL, false);
        chooseFaceRcv.setLayoutManager(gridLayoutManager);
        chooseFaceRcv.setAdapter(faceAdapter);
        faceAdapter.setOnItemClickListener(new RcvItemClickListener1() {
            @Override
            public void onItemClick(RecyclerView.ViewHolder itemHolder, View view) {
                int position = itemHolder.getLayoutPosition();
                if (position == -1) return;
                String url = faceAdapter.get(position).data.getUrlString();
                faceUrl = url;
                needSaveFace = false;
                CoverLoader.INSTANCE.loadOriginImageView(ChangeFaceActivity.this, url, faceIv,
                        R.drawable.add_circle, R.drawable.add_circle);
            }
        });
        loadFaceData();
    }

    private void loadFaceData() {
        Observable
                .create((ObservableOnSubscribe<List<PicResource>>)
                        emitter -> {
                            PicResourceDownloader.queryPicResByCategory(PicResource.FIRST_CLASS_TIETU, PicResource.SECOND_CLASS_MY, emitter);
                        })
                .map(new Function<List<PicResource>, List<PicResource>>() {

                    @Override
                    public List<PicResource> apply(List<PicResource> picResList) throws Exception {
                        return sortForChangeFace(picResList);
                    }
                })
                .subscribe(new SimpleObserver<List<PicResource>>() {

                    @Override
                    public void onError(Throwable throwable) {
                        if (isDestroyed()) {
                            return;
                        }
                        LogUtil.e(throwable.getMessage());
                        ToastUtils.show("获取人脸表情失败");
                    }

                    @Override
                    public void onNext(List<PicResource> picResList) {
                        if (isDestroyed()) {
                            return;
                        }
                        int size = picResList.size();
                        if (size == 0) {
                            //                            ToastUtils.show("暂无表情");
                            return;
                        }
                        //                        Log.d("TAG", "onNext: 获取到的贴图数量" + size);
                        faceAdapter.setList(picResList);
                    }
                });
    }

    private List<PicResource> sortForChangeFace(List<PicResource> picResList) {
        List<PicResource> resultRes = new ArrayList<>();
        for (int i = 0; i < picResList.size(); i++) {
            String fileName = FileTool.getFileNameInPath(picResList.get(i).getUrlString());
            if (fileName != null && fileName.contains(MyDatabase.CHANGE_FACE_FACE_TAG)) {
                resultRes.add(picResList.remove(i));
            }
        }
        return resultRes;
    }

    public void onClickGoDigFace(View view) {
        if (!AllData.hasReadConfig.hasRead_goDigFace()) {
            FirstUseUtil.showGuideView(this, getString(R.string.go_dig_face_notice), () -> {
                AllData.hasReadConfig.put_goDigFace(true);
                goChoseFace();
            });
        } else {
            goChoseFace();
        }
    }

    private void goChoseFace() {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.setAction(HomeActivity.INTENT_ACTION_ONLY_CHOSE_PIC);
        intent.putExtra(HomeActivity.INTENT_EXTRA_FRAGMENT_ID, HomeActivity.LOCAL_FRAG_ID);
        startActivityForResult(intent, REQUEST_CODE_CHOOSE_FACE_PIC);
    }

    private void onChooseFaceReturn(Intent data) {
        if (data == null) return;
        PicResource picRes = (PicResource) data.getSerializableExtra(HomeActivity.INTENT_EXTRA_CHOSEN_PIC_RES);
        if (picRes == null) return;

        String faceUrl = picRes.getUrlString();
        //        setFace(faceUrl); // 不自动设置，用户可能取消
        PtuUtil.buildPTuIntent(this, faceUrl)
                .setAsIntermediate(getString(R.string.next_step))
                .setToChildFunction(PtuUtil.CHILD_FUNCTION_DIG_FACE)
                .setPtuNotice("绕着五官画圈即可")
                .startActivityForResult(REQUEST_CODE_PTU_DIG_FACE);
    }

    private void setFace(String faceUrl) {
        this.faceUrl = faceUrl;
        loadUrl(faceUrl, faceIv);
        goDigFaceTv.setText(R.string.change_one_expression);
    }

    private void onDigFaceReturn(Intent data) {
        if (data == null) return;

        String url = data.getStringExtra(PTuResultData.NEW_PIC_PATH);
        faceUrl = url != null ? url : faceUrl;
        needSaveFace = true;
        // 不管用户选的什么模式，都自动生成色阶数据
        autoAdjustFaceLevel(faceUrl);
    }

    private void autoAdjustFaceLevel(String faceUrl) {
        if (faceUrl == null) return;
        Observable
                .create((ObservableOnSubscribe<Bitmap>) emitter -> { // 获取路径
                    String path = null;
                    if (FileTool.urlType(faceUrl).equals(FileTool.UrlType.OTHERS)) { // 判断是否是本地图片路径
                        path = faceUrl;
                    } else {
                        try {
                            path = Glide.with(this).asFile().load(faceUrl).submit().get().getAbsolutePath();
                        } catch (Exception e) {
                            LogUtil.e("获取贴图本地路径出错: " + e.getMessage());
                            emitter.onError(e);
                            return;
                        }
                    }
                    BitmapFactory.Options options = TietuSizeController.getFitWh(path, false);
                    Glide.with(IntelImEditApplication.appContext).asBitmap().load(path).into(
                            new SimpleGlideTarget<Bitmap>(options.outWidth, options.outHeight) {
                                @Override
                                public void onResourceReady(@NonNull Bitmap srcBitmap, @Nullable Transition<? super Bitmap> transition) {
                                    Log.d(TAG, "onResourceReady: " + srcBitmap.getWidth() + " " + srcBitmap.getHeight());
                                    if (srcBitmap.getWidth() == 0 || srcBitmap.getHeight() == 0) {
                                        ToastUtils.show("获取贴图失败");
                                        emitter.onError(new Exception());
                                        return;
                                    }
                                    emitter.onNext(srcBitmap);
                                    emitter.onComplete();
                                }
                            });
                })
                .map(new Function<Bitmap, LevelsAdjuster>() {
                    @Override
                    public LevelsAdjuster apply(@NotNull Bitmap origBm) throws Exception {
                        if (origBm == null) {
                            throw new Exception("无法获取图片");
                        }
                        LevelsAdjuster levelsAdjuster = new LevelsAdjuster();
                        levelsAdjuster.generateData(origBm, true);
                        boolean result = levelsAdjuster.autoAdjust();
                        if (!result) throw new Exception();
                        return levelsAdjuster;
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<LevelsAdjuster>() {

                    @Override
                    public void onNext(LevelsAdjuster levelsAdjuster) {
                        if (colorStyleCb.isChecked()) {
                            faceIv.setImageBitmap(levelsAdjuster.adjustedBm);
                        } else {
                            faceIv.setImageBitmap(levelsAdjuster.originalBm);
                        }
                        ChangeFaceActivity.this.levelsAdjuster = levelsAdjuster;
                    }

                    @Override
                    public void onError(Throwable e) {
                        ToastUtils.show("抱歉，算法出错了" + e.getMessage());
                        setFace(faceUrl);
                    }
                });
    }

    private void loadUrl(String url, ImageView faceIv) {
        //        CoverLoader.INSTANCE.loadOriginImageView(this, faceUrl, faceIv, R.drawable.add_circle, R.drawable.add_circle);
        Glide.with(this).load(url).placeholder(R.drawable.add_circle).error(R.drawable.add_circle).into(faceIv);
    }


    //--------------------------------------------------------------------------------------------------
    //----------------------------------------------表情-------------------------------------------------
    //--------------------------------------------------------------------------------------------------

    private void initExpression() {
        expressionAdapter = new TietuRecyclerAdapter(this, false);
        GridLayoutManager gridLayoutManager = new WrapContentGridLayoutManager(this, TietuRecyclerAdapter.DEFAULT_ROW_NUMBER,
                GridLayoutManager.HORIZONTAL, false);
        chooseExpressionRcv.setLayoutManager(gridLayoutManager);
        chooseExpressionRcv.setAdapter(expressionAdapter);
        Observable
                .create((ObservableOnSubscribe<List<PicResource>>)
                        emitter -> {
                            PicResourceDownloader.queryPicResByCategory(PicResource.FIRST_CLASS_TEMPLATE, PicResource.SECOND_CLASS_BASE, emitter);
                        })
                .map(new Function<List<PicResource>, List<PicResource>>() {

                    @Override
                    public List<PicResource> apply(List<PicResource> resourceList) throws Exception {
                        return SearchUtil.sortByGivenTag(resourceList, Arrays.asList("熊猫头", "蘑菇头", "换脸"));
                    }
                })
                .subscribe(new SimpleObserver<List<PicResource>>() {

                    @Override
                    public void onError(Throwable throwable) {
                        if (isDestroyed()) {
                            return;
                        }
                        LogUtil.e(throwable.getMessage());
                        ToastUtils.show("获取表情失败");
                    }

                    @Override
                    public void onNext(List<PicResource> picResList) {
                        if (isDestroyed()) {
                            return;
                        }
                        int size = picResList.size();
                        if (size == 0) {
                            ToastUtils.show("获取表情失败");
                            return;
                        }
                        Log.d("TAG", "onNext: 获取到的贴图数量" + size);
                        expressionAdapter.setList(picResList);
                    }
                });
        expressionAdapter.setOnItemClickListener(new RcvItemClickListener1() {
            @Override
            public void onItemClick(RecyclerView.ViewHolder itemHolder, View view) {
                int position = itemHolder.getLayoutPosition();
                if (position == -1) return;
                String url = expressionAdapter.get(position).data.getUrlString();
                onClickExpressionItem(view, url);
            }
        });
    }

    private void onClickExpressionItem(View view, String url) {
        // int eraseFaceAction = AllData.hasReadConfig.get_eraseFaceAction();
        // if (eraseFaceAction == -1) {
        //     DialogWithNotNoticeAgain dialog = new DialogWithNotNoticeAgain("去擦脸", "将表情上的脸部擦除，效果更好哟", "去擦脸", null);
        //     dialog.setNotNoticeListener(new DialogWithNotNoticeAgain.WithNotNoticeListener() {
        //         @Override
        //         public void cancel(boolean isNotNotice) {
        //             if (isNotNotice) {
        //                 AllData.hasReadConfig.put_eraseFaceNotice(1);
        //             }
        //             setExpression(url);
        //         }
        //
        //         @Override
        //         public void verify(boolean isNotNotice) {
        //             if (isNotNotice) {
        //                 AllData.hasReadConfig.put_eraseFaceNotice(0);
        //             }
        //             goEraseExpression(url);
        //         }
        //     });
        //     dialog.showIt(this);
        // } else if (eraseFaceAction == 0) {
        //     goEraseExpression(url);
        // } else {
        setExpression(url);
        // }
    }

    public void setExpression(String url) {
        expressionUrl = url != null ? url : expressionUrl;
        CoverLoader.INSTANCE.loadOriginImageView(ChangeFaceActivity.this, url, expressionIv,
                R.drawable.add_circle, R.drawable.add_circle);
        if (expressionUrl != null) {
            chooseExpressionTv.setText(R.string.change_one_expression);
        }
    }

    public void onClickExpressionIv(View view) {
        if (expressionUrl == null) {
            chooseExpression_InSearch(view);
        }
    }

    public void chooseExpression_InSearch(View view) {
        Intent intent1 = new Intent(this, SearchActivity.class);
        intent1.putExtra(SearchActivity.Companion.getINTENT_EXTRA_SEARCH_CONTENT(), ChangeFaceUtil.getChangeFaceTagsString());
        startActivityForResult(intent1, REQUEST_CODE_CHOOSE_EXPRESSION_IN_SEARCH);
    }

    private void onChoseExpressionInSearch_Return(Intent data) {
        if (data == null) return;
        PicResource picRes = (PicResource) data.getSerializableExtra(SearchActivity.Companion.getINTENT_EXTRA_SEARCH_PIC_RES());
        if (picRes == null) return;

        String expressionUrl = picRes.getUrlString();
        //        setExpression(expressionUrl); // 不自动设置，用户可能取消
        goEraseExpression(expressionUrl);
    }

    private void chooseExpression_InHome() {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.setAction(HomeActivity.INTENT_ACTION_ONLY_CHOSE_PIC);
        intent.putExtra(HomeActivity.INTENT_EXTRA_FRAGMENT_ID, HomeActivity.LOCAL_FRAG_ID);
        startActivityForResult(intent, REQUEST_CODE_CHOOSE_EXPRESSION_IN_HOME);
    }

    private void onChoseExpressionInHome_Return(Intent data) {
        if (data == null) return;
        PicResource picRes = (PicResource) data.getSerializableExtra(HomeActivity.INTENT_EXTRA_CHOSEN_PIC_RES);
        if (picRes == null) return;

        String expressionUrl = picRes.getUrlString();
        //        setExpression(expressionUrl); // 不自动设置，用户可能取消
        goEraseExpression(expressionUrl);
    }

    private void goEraseExpression(String expressionUrl) {
        PtuUtil.buildPTuIntent(this, expressionUrl)
                .setAsIntermediate(getString(R.string.next_step))
                .setToChildFunction(PtuUtil.CHILD_FUNCTION_ERASE_FACE)
                .setPtuNotice("涂抹人脸即可")
                .startActivityForResult(REQUEST_CODE_PTU_ERASE_EXPRESSION);
    }

    public void onClickEraseExpression(View view) {
        //nothing current
        if (expressionUrl != null) {
            goEraseExpression(expressionUrl);
        } else {
            chooseExpression_InSearch(view);
        }
    }

    /**
     * 用户切换人脸色彩模式
     * 如果还没有选择人脸图片，那么没动作
     */
    public void onChangeColorStyle(Boolean isChecked) {
        if (isChecked) {
            if (levelsAdjuster != null) {
                faceIv.setImageBitmap(levelsAdjuster.adjustedBm);
            } else { // 色阶数据每次都自动生成了，如果没有就是失败了，
                setFace(faceUrl);
            }
        } else {
            if (levelsAdjuster != null) {
                faceIv.setImageBitmap(levelsAdjuster.originalBm);
            } else {
                setFace(faceUrl);
            }
        }
    }

    public void onClickFaceIv(View view) {
        if (faceUrl == null || levelsAdjuster == null) {
            onClickGoDigFace(view);
        }
    }

    public void goChangeFace(View view) {
        if (faceUrl == null && levelsAdjuster == null) {
            DialogFactory.noTitle(this, "请在上方选择一张人脸吧", null, null, null);
            return;
        }
        if (expressionUrl == null) {
            DialogFactory.noTitle(this, "请在上方选择一张表情吧", null, null, null);
            return;
        }

        if (!colorStyleCb.isChecked() || levelsAdjuster == null)
            AllData.levelsAdjuster = null; // 彩色，不使用色阶数据

        else AllData.levelsAdjuster = levelsAdjuster;
        Intent intent = new Intent(this, PtuActivity.class);
        intent.setAction(PtuActivity.PTU_ACTION_NORMAL);
        intent.putExtra(PtuActivity.INTENT_EXTRA_PIC_PATH, expressionUrl);
        intent.putExtra(PtuActivity.INTENT_EXTRA_SECOND_PIC_PATH, faceUrl);
        intent.putExtra(INTENT_EXTRA_NEED_SAVE_FACE, needSaveFace);
        intent.putExtra(PtuActivity.INTENT_EXTRA_TO_CHILD_FUNCTION, PtuUtil.CHILD_FUNCTION_CHANGE_FACE);
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_CHOOSE_FACE_PIC && resultCode == RESULT_OK) {
            onChooseFaceReturn(data);
        } else if (requestCode == REQUEST_CODE_CHOOSE_EXPRESSION_IN_HOME) { // 选择表情返回
            onChoseExpressionInHome_Return(data);
        } else if (requestCode == REQUEST_CODE_CHOOSE_EXPRESSION_IN_SEARCH) {
            if (resultCode == RESULT_OK) {
                try {
                    onChoseExpressionInSearch_Return(data);
                } catch (Exception e) {
                    chooseExpression_InHome();
                }
            } else {
                chooseExpression_InHome();
            }
        } else if (requestCode == REQUEST_CODE_PTU_DIG_FACE) {
            if (resultCode == PtuActivity.RESULT_CODE_NORMAL_RETURN) {
                // nothing
            } else {
                onDigFaceReturn(data);
            }
        } else if (requestCode == REQUEST_CODE_PTU_ERASE_EXPRESSION) {
            if (resultCode == PtuActivity.RESULT_CODE_NORMAL_RETURN) {

            } else {
                if (data == null) return;
                String url = data.getStringExtra(PTuResultData.NEW_PIC_PATH);
                setExpression(url);
            }
        }
    }
}