package com.mandi.intelimeditor.ptu;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.mandi.intelimeditor.common.BaseFragment;
import com.mathandintell.intelimedit.bean.FunctionInfoBean;
import com.mathandintell.intelimedit.ptu.BottomFunctionAdapter;
import com.mathandintell.intelimeditor.R;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.List;


/**
 * P图底部操作Fragment基类
 * 1、抽离公共基础代码和方法，减少重复代码
 * 2、公共方法和公共变量
 * {@linkplain BasePtuFragment#pFunctionRcv} 底部通用列表
 * {@linkplain BasePtuFragment#pFunctionList} 底部通用列表数据集合，初始化列表之前赋值。重写{@link BasePtuFragment#getFunctionList()}方法
 * {@linkplain BasePtuFragment#pFunctionAdapter} 底部通用列表适配器
 * {@linkplain BasePtuFragment#onItemClick(BaseQuickAdapter, View, int)} 底部通用列表点击事件回调
 */
public abstract class BasePtuFragment extends BaseFragment implements BasePtuFunction, OnItemClickListener {
    /**
     * 底部功能列表
     */
    public RecyclerView pFunctionRcv;
    public List<FunctionInfoBean> pFunctionList = new ArrayList<>();
    public BottomFunctionAdapter pFunctionAdapter;

    /**
     * 底部滑动指示器（只有底部超过五个功能以上才会显示）
     */
    public View mIndicatorView;
    public View mIndicatorLayout;

    /**
     * 初始化View
     */
    @Override
    public void initView() {
        super.initView();
        if (rootView != null) {
            pFunctionRcv = rootView.findViewById(R.id.main_function_rcv);
            mIndicatorView = rootView.findViewById(R.id.indicatorView);
            mIndicatorLayout = rootView.findViewById(R.id.indicatorLayout);
            initBottomFun();
        }
    }

    @Override
    public boolean onBackPressed(boolean isFromKey) {
        return false;
    }

    @Override
    public void clear() {
    }

    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart(PtuUtil.getEditModeName(getEditMode()));
//        LogUtil.d(PtuUtil.getEditModeName(getEditMode()));
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(PtuUtil.getEditModeName(getEditMode()));
//        LogUtil.d(PtuUtil.getEditModeName(getEditMode()));
    }

    @Override
    public void generateResultDataInMain(float ratio) {
    }

    public void initBottomFun() {
        if (pFunctionRcv != null) {
            if (mIndicatorLayout != null && getFunctionList().size() > 5) {
                mIndicatorLayout.setVisibility(View.VISIBLE);
            } else if (mIndicatorLayout != null) {
                mIndicatorLayout.setVisibility(View.GONE);
            }
            pFunctionAdapter = new BottomFunctionAdapter(getFunctionList());
            pFunctionAdapter.setOnItemClickListener(this);
            pFunctionRcv.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
            pFunctionRcv.setAdapter(pFunctionAdapter);
            pFunctionRcv.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    if (mIndicatorLayout != null) {
                        //当前RcyclerView显示区域的高度。水平列表屏幕从左侧到右侧显示范围
                        int extent = recyclerView.computeHorizontalScrollExtent();
                        //整体的高度，注意是整体，包括在显示区域之外的。
                        int range = recyclerView.computeHorizontalScrollRange();
                        //已经滚动的距离，为0时表示已处于顶部。
                        int offset = recyclerView.computeHorizontalScrollOffset();
                        //计算出溢出部分的宽度，即屏幕外剩下的宽度
                        float maxEndX = range - extent;
                        //计算比例
                        float proportion = offset / maxEndX;
                        int layoutWidth = mIndicatorLayout.getWidth();
                        int indicatorViewWidth = mIndicatorView.getWidth();
                        //可滑动的距离
                        int scrollableDistance = layoutWidth - indicatorViewWidth;
                        //设置滚动条移动
                        mIndicatorView.setTranslationX(scrollableDistance * proportion);
                    }
                }
            });
        }
    }


    /**
     * 获取当前功能列表
     */
    public List<FunctionInfoBean> getFunctionList() {
        return pFunctionList;
    }

    /**
     * 获取当前fragment编辑模式
     */
    public abstract int getEditMode();

    @Override
    public void smallRepeal() {
    }

    @Override
    public void smallRedo() {
    }

    /**
     * 注意参数的创建的对象很常用，创建Fragment之后必须立即调用，不然空指针
     */
    public abstract void setPTuActivityInterface(PTuActivityInterface ptuActivity);

    /**
     * 预留接口，目前不采用这种模式，似乎不如点击sure之后将整个一级的完成返回的好
     * 一级功能内部的sure，类似于返回键，如果返回了true就表示消耗这个sure
     */
    public abstract boolean onSure();

    public List<String> getGuidKeyword() {
        return null;
    }

    /**
     * Callback method to be invoked when an item in this RecyclerView has
     * been clicked.
     */
    @Override
    public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
    }
}
