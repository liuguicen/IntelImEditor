package com.mandi.intelimeditor.ptu.saveAndShare;

import android.app.Dialog;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.mandi.intelimeditor.ad.ADHolder;
import com.mandi.intelimeditor.ad.AdData;
import com.mandi.intelimeditor.ad.AdStrategyUtil;
import com.mandi.intelimeditor.ad.TTAdConfig;
import com.mandi.intelimeditor.ad.tencentAD.TxFeedAd;
import com.mandi.intelimeditor.ad.ttAD.nativead.TTFeedAd;
import com.mandi.intelimeditor.ad.ttAD.videoAd.FullScreenVadManager;
import com.mandi.intelimeditor.common.BaseActivity;
import com.mandi.intelimeditor.common.dataAndLogic.AllData;
import com.mandi.intelimeditor.common.util.FileTool;

import com.mandi.intelimeditor.common.util.LogUtil;
import com.mandi.intelimeditor.common.util.SimpleObserver;
import com.mandi.intelimeditor.common.util.ToastUtils;
import com.mandi.intelimeditor.common.view.DialogFactory;
import com.mandi.intelimeditor.home.search.SearchUtil;
import com.mandi.intelimeditor.home.tietuChoose.PicResourceItemData;
import com.mandi.intelimeditor.home.tietuChoose.PicResourcesAdapter;
import com.mandi.intelimeditor.ptu.PtuActivity;

import com.mandi.intelimeditor.ptu.tietu.onlineTietu.PicResource;
import com.mandi.intelimeditor.ptu.tietu.onlineTietu.PicResourceDownloader;
import com.mandi.intelimeditor.user.US;
import com.mandi.intelimeditor.user.userVip.VipUtil;
import com.mandi.intelimeditor.user.useruse.FirstUseUtil;
import com.mandi.intelimeditor.dialog.RateUsDialog;
import com.mandi.intelimeditor.R;
import com.qq.e.ads.nativ.ADSize;

import java.util.ArrayList;
import java.util.List;


import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.functions.Function;

public class PTuResultActivity extends BaseActivity {

    /**
     * 目前没有特别用处，做标志用
     */
    public static final int REQUEST_CODE_PTU = 111;
    public static final String INTENT_EXTRA_RECOMMEND_PIC_RES = "INTENT_EXTRA_PTU_RECOMMEND_PATH";
    public static final int RESULT_CODE_PTU_RECOMMEND = 234;
    private String resultPath;
    private ImageView resultImageView;
    private TextView resultTv;
    private Intent srcIntent;
    private boolean isShare;
    private boolean isSave;
    private boolean mCanBackPressed = true;
    private int mBackPressNumber;
    private TextView mPathTv;
    private FrameLayout mAdContainer;
    private RecyclerView mShareRecyclerView;
    private com.mandi.intelimeditor.ad.ttAD.nativead.TTFeedAd TTFeedAd = null;

    private List<ResolveInfo> resolveInfos;
    private ShareRecyclerAdapter shareRecyclerAdapter;
    private List<ShareItemData> shareActivityInfo;
    private MyQQShare myQQShare;
    // 判断第一次点击返回操作，反之广告没有回调到返回不了
    private boolean isFirstReturn = true;
    private Dialog uploadDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        parseIntent();
        bindView();
        initView();
        showRecommendPic();
//        initAd();
        AdData.addPTuResultCount();
        initShareRecyclerView();
    }

    private void showRecommendPic() {
        Observable.create((ObservableOnSubscribe<List<PicResource>>) emitter -> {
            PicResourceDownloader.queryPicResByCategory(PicResource.FIRST_CLASS_TEMPLATE, PicResource.SECOND_CLASS_BASE, emitter);
        }).map(new Function<List<PicResource>, List<PicResource>>() {
            @Override
            public List<PicResource> apply(List<PicResource> srcTagList) throws Exception {
                ArrayList<String> useTagsList = srcIntent.getStringArrayListExtra(PTuResultData.INTENT_EXTRA_USED_TAGS_LIST);
                String excludeTag = (useTagsList != null && useTagsList.size() >= 1) ? useTagsList.get(0) : null;
                List<PicResource> searchedRes = SearchUtil.searchSimilarResByTags(srcTagList, useTagsList, excludeTag);
                if (LogUtil.debugRecommend) {
                    Log.d(TAG, "获取到的推荐列表 size = " + (searchedRes != null ? searchedRes.size() : 0));
                }
                return searchedRes;
            }
        }).subscribe(new SimpleObserver<List<PicResource>>() {
            @Override
            public void onError(Throwable throwable) {
                LogUtil.d(TAG, "网络出错，不能获取贴图 = " + " - " + throwable.getMessage());
                initAd();
            }

            @Override
            public void onNext(List<PicResource> picResList) {
                // TODO: 2020/6/4 显示推荐图片列表
                if (picResList.size() != 0) {
                    showRecommendRes(picResList);
                } else {
                    initAd();
                }
            }
        });
    }

    private void showRecommendRes(List<PicResource> picResList) {
        RecyclerView recommendResRcv = findViewById(R.id.recommend_rcv);
        PicResourcesAdapter picResourcesAdapter = new PicResourcesAdapter(PTuResultActivity.this, 1);
        picResourcesAdapter.initAdData(true);
        picResourcesAdapter.setImageUrls(picResList, null);
        picResourcesAdapter.setClickListener((itemHolder, view) -> {
            int position = itemHolder.getLayoutPosition();
            if (position == -1) return;
            PicResource picRes = picResourcesAdapter.getItem(position).data;
            if (picRes == null) return;
            picRes.updateHeat();
            US.putEditPicEvent(US.EDIT_PIC_FROM_COMMEND);
            if (picRes.getUrl() != null) {
                Intent intent = new Intent();
                intent.putExtra(PTuResultActivity.INTENT_EXTRA_RECOMMEND_PIC_RES, picRes);
                setResult(PTuResultActivity.RESULT_CODE_PTU_RECOMMEND, intent);
                finish();
            }
        });
        int finalSpanCount = 2;
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                if (position <= picResourcesAdapter.getItemCount() &&
                        picResourcesAdapter.getItemViewType(position) == PicResourceItemData.PicListItemType.FEED_AD) {
                    return finalSpanCount;
                }
                return 1;
            }
        });
        recommendResRcv.setLayoutManager(gridLayoutManager);
        recommendResRcv.setAdapter(picResourcesAdapter);
        recommendResRcv.setNestedScrollingEnabled(false);
    }

    @Override
    protected void onRestart() {
        VipUtil.judeShowToOpenVip_forAdClick(this);
        super.onRestart();
    }

    private void initAd() {
        AdStrategyUtil adStrategyUtil = new AdStrategyUtil(AdData.AdSpaceName.PTU_RESULT, AllData.appConfig.ptu_result_ad_strategy);
        mAdContainer.setVisibility(View.VISIBLE);
        if (!AdData.judgeAdClose(AdData.TENCENT_AD) && adStrategyUtil.isShow("TX")) {
            TxFeedAd txFeedAd = new TxFeedAd(this, mAdContainer, AdData.GDT_ID_FEED_PIC_RES_LIST, AdData.AdSpaceName.PTU_RESULT,
                    AdData.AdSpaceName.PTU_RESULT);
            txFeedAd.setAdSize(new ADSize(ADSize.FULL_WIDTH, ADSize.AUTO_HEIGHT));
            // 如果没加载成功，load广告资源，load之后内部可能调用bindData，所以这里判断，load了就不bind
            ADHolder adHolder = new ADHolder(mAdContainer, mAdContainer);
            if (!txFeedAd.isLoadSuccess()) {
                txFeedAd.loadAdResources(adHolder);
            } else {
                txFeedAd.bindData(adHolder);
            }
        } else if (!AdData.judgeAdClose(AdData.TT_AD)) { // 默认
            TTFeedAd = new TTFeedAd(this);
            TTFeedAd.loadExpressAd(mAdContainer, TTAdConfig.PTU_RESULT_FEED_AD_ID, AllData.screenWidth);
        }
    }

    @Override
    public int getLayoutResId() {
        return R.layout.activity_ptu_result;
    }


    private void parseIntent() {
        srcIntent = getIntent();
        resultPath = srcIntent.getStringExtra(PTuResultData.NEW_PIC_PATH);
    }

    private void bindView() {
        mAdContainer = findViewById(R.id.ptu_result_ad_container);
        resultImageView = findViewById(R.id.iv_result_pic);
        resultTv = findViewById(R.id.tv_result);
        mPathTv = findViewById(R.id.result_pic_path);
        mShareRecyclerView = findViewById(R.id.share_rcv);
    }

    private void initView() {
        isShare = isSave = false;
        saveFinish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_save, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            continuePtu();
        } else if (item.getItemId() == R.id.action_finish) {
            returnChoose();
        } else if (item.getItemId() == R.id.action_delete) {
            deleteThePic();
        } else if (item.getItemId() == R.id.action_upload) {
            uploadDialog = DialogFactory.noTitle(this,
                    getString(R.string.upload_works_for_other_to_use),
                    getString(R.string.ok),
                    getString(R.string.drop_it),
                    (dialog, which) -> {
                        uploadResultPic();
                        dialog.dismiss();
                    });
        }
        return true;
    }

    private void saveFinish() {
        if (resultPath != null) {
            Glide.with(this).load(resultPath).into(resultImageView);
        }
        if (PTuResultData.SAVE_AND_LEAVE.equals(srcIntent.getAction())) {
            isShare = true;
            resultTv.setText(R.string.save_finish);
        } else if (PTuResultData.SHARE_AND_LEAVE.equals(srcIntent.getAction())) {
            resultTv.setText(R.string.share_finish);
            isSave = true;
        }
        mPathTv.setText(getString(R.string.path_is, resultPath));
    }

    private void returnChoose() {
        Runnable task = () -> {
            setResult(PtuActivity.RESULT_CODE_RETURN_CHOOSE, srcIntent);
            US.putPTuResultEvent(US.PTU_RESULT_RETURN_CHOOSE);
            finish();
        };
        if (AllData.sRandom.nextDouble() < AdData.PROBABILITY_VAD_PTU_RESULT
                && System.currentTimeMillis() > AdData.lastVideoAdShowTime + AdData.VIEDO_AD_SHOW_INTERVAL
                && !AdData.judgeAdClose(AdData.TT_AD)) {
            FullScreenVadManager.showFullScreenVad(this, task, getString(R.string.support_by_video_ad));
        } else if (show2RateDialog()) {
            // nothing
        } else {
            task.run();
        }
        isFirstReturn = true;
    }


    /**
     * 结果页返回，继续P图
     */
    private void continuePtu() {
        Runnable task = () -> {
            setResult(PtuActivity.RESULT_CODE_CONTINUE_PTU, srcIntent);
            US.putPTuResultEvent(US.PTU_RESULT_COTINUE_PTU);
            finish();
        };
        if (AllData.sRandom.nextDouble() < AdData.PROBABILITY_VAD_PTU_RESULT
                && isFirstReturn
                && System.currentTimeMillis() > AdData.lastVideoAdShowTime + AdData.VIEDO_AD_SHOW_INTERVAL
                && !AdData.judgeAdClose(AdData.TT_AD)) {
            FullScreenVadManager.showFullScreenVad(this, task, getString(R.string.support_by_video_ad));
        } else if (show2RateDialog()) {
            // nothing
        } else {
            task.run();
        }
        isFirstReturn = false;
    }

    /**
     * 让用户去给好评的弹窗
     */
    private boolean show2RateDialog() {
        double probablity = 1.0 / 300;
        if ("OPPO".equals(android.os.Build.BRAND)) {
            probablity *= 2;
        }
        if (AllData.sRandom.nextDouble() < probablity) {
            new RateUsDialog().showIt(this);
            US.putOtherEvent(US.OTHERS_DIALOG_2_STAR);
            return true;
        }
        return false;
    }

    /**
     * 删除图片
     */
    private void deleteThePic() {
        if (FirstUseUtil.deleteResultPic(this)) {
            return;
        } else {
            FileTool.deletePicFile(resultPath);
            resultImageView.setImageBitmap(null);
            resultTv.setText(R.string.delete_finish);
            mPathTv.setVisibility(View.GONE);
            mShareRecyclerView.setVisibility(View.GONE);
            if (isShare) {
                US.putPTuResultEvent(US.PTU_RESULT_SHARE_DELETE);
            } else if (isSave) {
                US.putPTuResultEvent(US.PTU_RESULT_SAVE_DELETE);
            }
        }
    }

    /**
     * 初始化分享列表
     */
    private void initShareRecyclerView() {
        if (shareRecyclerAdapter == null) {
            resolveInfos = ShareUtil.getAcInfo_SupportShare(this, ShareUtil.Type.Image);
            shareActivityInfo = ShareUtil.getShareInfo(this, resolveInfos);
            LinearLayoutManager layoutManager = new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false);
            mShareRecyclerView.setLayoutManager(layoutManager);
            shareRecyclerAdapter = new ShareRecyclerAdapter(this, shareActivityInfo);
            mShareRecyclerView.setAdapter(shareRecyclerAdapter);
            mShareRecyclerView.setNestedScrollingEnabled(false);
            shareRecyclerAdapter.setOnItemClickListener((view, data) -> {
//                LogUtil.d("Savaset", "分享受到点击");
                if (resultPath == null) {
                    ToastUtils.show("文件保存失败，无法分享");
                    return;
                }

                int clickPosition = shareActivityInfo.indexOf(data);
                ResolveInfo resolveInfo = resolveInfos.get(clickPosition);
                myQQShare = ShareUtil.share(this, resolveInfo, resultPath);
            });

        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (myQQShare != null)
            myQQShare.onActivityResult(requestCode, resultCode, data);
    }

    private void uploadResultPic() {
        US.putOtherEvent(US.OTHERS_UPLOAD_PTU_WORK);
    }

    @Override
    public void onBackPressed() {
        continuePtu();
        // US.putPTuResultEvent(US.PTU_RESULT_BACK_PRESSED);
        // if (!mCanBackPressed) {
        //     mBackPressNumber++;
        //     if (mBackPressNumber == 2) {
        //         mDelayTv.setTextColorWithOpacity(Color.RED);
        //     }
        //     if (mBackPressNumber == 3) {
        //         mBackPressNumber = 0;
        //         FirstUseUtil.readAdGuide(this);
        //     }
        // } else {
        //     super.onBackPressed();
        // }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (TTFeedAd != null) {
            TTFeedAd.destroy();
        }
        if (uploadDialog != null) {
            uploadDialog.dismiss();
        }
    }
}
