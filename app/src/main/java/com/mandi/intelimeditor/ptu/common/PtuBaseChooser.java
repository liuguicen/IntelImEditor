package com.mandi.intelimeditor.ptu.common;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.PopupWindow;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mandi.intelimeditor.common.dataAndLogic.AllData;

import com.mandi.intelimeditor.common.util.LogUtil;
import com.mandi.intelimeditor.common.util.SimpleObserver;
import com.mandi.intelimeditor.common.util.ToastUtils;
import com.mandi.intelimeditor.common.util.WrapContentGridLayoutManager;
import com.mandi.intelimeditor.home.HomeActivity;
import com.mandi.intelimeditor.home.search.SearchUtil;
import com.mandi.intelimeditor.ptu.BasePtuFragment;
import com.mandi.intelimeditor.ptu.PTuActivityInterface;
import com.mandi.intelimeditor.ptu.PtuActivity;
import com.mandi.intelimeditor.ptu.PtuUtil;
import com.mandi.intelimeditor.ptu.tietu.onlineTietu.PicResource;
import com.mandi.intelimeditor.ptu.tietu.onlineTietu.PicResourceDownloader;
import com.mandi.intelimeditor.ptu.tietu.onlineTietu.TietuRecyclerAdapter;
import com.mandi.intelimeditor.user.US;
import com.mandi.intelimeditor.R;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.functions.Function;

/**
 * <pre>
 *      author : liuguicen
 *      time : 2019/06/04
 *      version : 1.0
 * <pre>
 *     用来选择背景图进行切换的
 */
public class PtuBaseChooser {
    private final PTuActivityInterface mPTuActivityInterface;
    private Context mContext;
    private boolean isUpdateHeat = false;
    private boolean isShowMoreBtn = true;
    private View mMoreBtn;
    private boolean isChooseBgAuto;
    private BasePtuFragment mBasePtuFragment;
    private RecyclerView mTemplateRcv;
    private TietuRecyclerAdapter templateListAdapter;
    private List<PicResource> mTemplateDataList;
    private PopupWindow mPopupWindow;
    private ViewGroup mViewRoot;
    private int mPopY;
    private View mPopAnchor;

    /**
     * 用于让包含指定TAG的图片排在前面
     */
    private List<String> priorTagList;
    private String secondClass = PicResource.SECOND_CLASS_BASE;
    private ItemClickListener itemClickListener;

    /**
     * 注意要调用{@link #show()}开始显示
     */
    public PtuBaseChooser(Context context, BasePtuFragment fragment,
                          @NotNull PTuActivityInterface pTuActivityInterface, List<String> priorTagList) {
        mContext = context;
        mBasePtuFragment = fragment;
        this.mPTuActivityInterface = pTuActivityInterface;
        isChooseBgAuto = true;
        this.priorTagList = priorTagList;
    }

    public void show() {
        initView();
        loadTemplateRes();
    }

    public void setShowMoreBtn(boolean showMoreBtn) {
        isShowMoreBtn = showMoreBtn;
    }

    public void setSecondClass(String secondClass) {
        this.secondClass = secondClass;
    }

    /**
     * 用于让包含指定TAG的图片排在前面
     */
    public void setPriorTagList(List<String> priorTagList) {
        this.priorTagList = priorTagList;
    }

    public void setOnItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    /**
     * 初始化视图
     */
    private void initView() {
        mViewRoot = (ViewGroup) LayoutInflater.from(mContext).inflate(R.layout.popwindow_ptu_base_choose, null);
        initPopWindow();
        showChooser();

        mTemplateRcv = mViewRoot.findViewById(R.id.ptu_pic_resources_choose_rv);
        mMoreBtn = mViewRoot.findViewById(R.id.ptu_pic_resources_choose_more);
        if (!isShowMoreBtn) {
            mMoreBtn.setVisibility(View.GONE);
        }
        GridLayoutManager gridLayoutManager = new WrapContentGridLayoutManager(mContext, 2,
                GridLayoutManager.HORIZONTAL, false);
        mTemplateRcv.setLayoutManager(gridLayoutManager);
        templateListAdapter = new TietuRecyclerAdapter(mContext, false);
//        if (!AdData.judgeAdClose(AdData.TENCENT_AD)) { // 价值太低了，丢弃
//            templateListAdapter.initAdData(AdData.getTxPicAdPool_inPTu());
//        }
        templateListAdapter.setOnItemClickListener((itemHolder, view) -> {
            int position = itemHolder.getLayoutPosition();
            if (position == -1) return;
            US.putOtherEvent(US.CHOOSE_BASE);
            PicResource oneTietu = templateListAdapter.get(position).data;
            if (oneTietu != null && oneTietu.getUrl() != null) {
                if (itemClickListener != null) {
                    itemClickListener.onClickItem(oneTietu);
                    return;
                }
                String url = oneTietu.getUrl().getUrl();
                isChooseBgAuto = false; // 点击成功之后不需要下载完成自动添加底图了
                mPTuActivityInterface.addUsedTags(true, oneTietu.getTag());
                mPTuActivityInterface.replaceBase(url);
                templateListAdapter.notifyItemChanged(position);
                hideChooser();
            } else {
                ToastUtils.show(mContext.getString(R.string.get_pic_failed));
                Log.e(this.getClass().getSimpleName(), "点击贴图后获取失败");
                hideChooser();
            }
        });
        templateListAdapter.setUpdateHeat(isUpdateHeat);
        mTemplateRcv.setAdapter(templateListAdapter);
        mMoreBtn.setOnClickListener(v -> {
            Intent intent = new Intent(mContext, HomeActivity.class);
            intent.setAction(HomeActivity.PTU_ACTION_CHOOSE_BASE);
            mBasePtuFragment.startActivityForResult(intent, PtuActivity.REQUEST_CODE_CHOOSE_BASE);
        });
    }

    private void loadTemplateRes() {
        Observable
                .create((ObservableOnSubscribe<List<PicResource>>)
                        emitter -> PicResourceDownloader.queryPicResByCategory(PicResource.FIRST_CLASS_TEMPLATE, secondClass, emitter))
                .map(new Function<List<PicResource>, List<PicResource>>() {
                    @Override
                    public List<PicResource> apply(List<PicResource> oldList) throws Exception {
                        // 将包含撕图标签的放到前面去
                        if (priorTagList != null) {
                            return SearchUtil.sortByGivenTag(oldList, priorTagList);
                        }
                        return oldList;
                    }
                })
                .subscribe(new SimpleObserver<List<PicResource>>() {
                    @Override
                    public void onError(Throwable throwable) {
                        //                        ToastUtils.makeText("网络出错，不能获取贴图");
                        String eMsg = "获取图片资源分类列表下出错";
                        // 一定提醒用户，不然点了都是空白，极其影响使用体验
                        PtuUtil.onNoPicResource(mContext.getString(R.string.no_template_resource));
                        LogUtil.e(eMsg + throwable.getMessage());
                        templateListAdapter.setList(new ArrayList<>());
                    }

                    @Override
                    public void onNext(List<PicResource> picResource_list) {
                        int size = picResource_list.size();
                        if (size == 0) {
                            PtuUtil.onNoPicResource(mContext.getString(R.string.no_template_resource));
                            return;
                        }
                        // Log.d("TAG", "onNext: 获取到的贴图数量" + size);
                        if (isChooseBgAuto) { // 初始化时自动添加底图
                            int id = 0;
                            if (priorTagList == null) {
                                id = AllData.sRandom.nextInt(size);
                            }
                            mPTuActivityInterface.replaceBase(picResource_list.get(id).getUrl().getUrl());
                        }
                        mTemplateDataList = picResource_list;
                        templateListAdapter.setList(picResource_list);
                    }
                })
        ;
    }


    public void switchPtuBaseChooseView() {
        if (mPopupWindow == null) return;
        if (mPopupWindow.isShowing()) {
            hideChooser();
        } else {
            showChooser();
            if (mTemplateDataList == null) { // 数据没有加载成功，重新加载
                loadTemplateRes();
            }
        }
    }

    public void updateDefault() {
        if (!isUpdateHeat) { // 更新统计数据，默认加的这个，使用了才算热度++
            if (mTemplateDataList != null && mTemplateDataList.size() >= 1) {
                mTemplateDataList.get(0).updateHeat();
            }
        }
    }


    public void releaseResources() {
        hideChooser();
    }

    public void setChooseBgAuto(boolean isNeed) {
        this.isChooseBgAuto = isNeed;
    }

    void initPopWindow() {
        if (mPTuActivityInterface != null) {
            mPopAnchor = mPTuActivityInterface.getActivityViewRoot().findViewById(R.id.fragment_main_function);
        }
        //20201025 修复友盟bug，mPopAnchor 空指针异常崩溃
        if (mPopAnchor != null) {
            mPopY = -mContext.getResources().getDimensionPixelOffset(R.dimen.ptu_choose_base_height) - mPopAnchor.getHeight();
        }
        mPopupWindow = new PopupWindow(mViewRoot,
                WindowManager.LayoutParams.MATCH_PARENT,
                mContext.getResources().getDimensionPixelOffset(R.dimen.ptu_choose_base_height),
                true);
        mPopupWindow.setTouchable(true);
        // 如果不设置PopupWindow的背景，无论是点击外部区域还是Back键都无法dismiss弹框
        // 我觉得这里是API的一个bug
        mPopupWindow.setBackgroundDrawable(ContextCompat.getDrawable(mContext, R.drawable.white));

        //防止与虚拟按键冲突
        //一定设置好参数之后再show,注意注意注意!!!!
        mPopupWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
    }

    private void showChooser() {
        if (mPopAnchor != null) {
            mPopupWindow.showAsDropDown(
                    mPopAnchor,
                    0, mPopY);
        }
    }

    private void hideChooser() {
        if (mPopupWindow.isShowing()) {
            mPopupWindow.dismiss();
        }
    }

    public void setIsUpdateHeat(boolean isUpdateHeat) {
        this.isUpdateHeat = isUpdateHeat;
        if (templateListAdapter != null) {
            templateListAdapter.setUpdateHeat(false);
        }
    }

    public interface ItemClickListener {
        void onClickItem(PicResource picRes);
    }
}
