package com.mandi.intelimeditor.ptu.tietu.onlineTietu;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.mandi.intelimeditor.ad.AdData;
import com.mandi.intelimeditor.ad.LockUtil;
import com.mandi.intelimeditor.ad.tencentAD.AdUtil;
import com.mandi.intelimeditor.ad.tencentAD.ListAdStrategyController;
import com.mandi.intelimeditor.ad.tencentAD.TxFeedAdPool;
import com.mandi.intelimeditor.ad.ttAD.videoAd.TTRewardVad;
import com.mandi.intelimeditor.common.RcvItemClickListener1;
import com.mandi.intelimeditor.common.dataAndLogic.AllData;
import com.mandi.intelimeditor.common.util.BmobUtil;
import com.mandi.intelimeditor.common.util.Util;
import com.mandi.intelimeditor.home.tietuChoose.PicResourceItemData;
import com.mandi.intelimeditor.home.view.TencentPicADHolder;
import com.mandi.intelimeditor.R;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import util.CoverLoader;


/**
 * Created by liuguicen on 2016/6/17.
 * P图界面的贴图列表的Adapter
 */
public class TietuRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = "TietuRecyclerAdapter";
    private final boolean mIsTietu;
    public static final int DEFAULT_ROW_NUMBER = 1;
    public static final int itemLayoutHorizontalMargin = Util.dp2Px(1.5f);

    private List<PicResourceItemData> groupedList = new ArrayList<>();

    @Nullable
    private ListAdStrategyController mAdController;

    private final LayoutInflater layoutInflater;

    private RcvItemClickListener1 mItemClickListener;

    private Context mContext;
    private int itemMargin;
    private int rowNumber;
    private boolean isUpdateHeat = true;

    public TietuRecyclerAdapter(Context context, boolean isTietu) {
        mContext = context;
        mIsTietu = isTietu;
        rowNumber = DEFAULT_ROW_NUMBER;
        layoutInflater = LayoutInflater.from(context);
        itemMargin = Util.dp2Px(1);
//        if (!AdData.judgeAdClose(AdData.TENCENT_AD)) {
//            initAdData(AdData.getTxPicAdPool_inPTu());
//        }
    }


    public void initAdData(TxFeedAdPool adPool) {
        if (adPool == null) return;
        mAdController = new ListAdStrategyController(mContext,
                AdData.GDT_ID_PURE_PIC_QY_2,
                adPool,
                20, 34, 50, false, false);
    }

    public void setUpdateHeat(boolean updateHeat) {
        isUpdateHeat = updateHeat;
    }

    public void setItemList(List<PicResourceItemData> list) {
        groupedList.clear();
        if (mAdController != null)
            mAdController.reSet();
        for (int i = 0; i < list.size(); i++) {
            list.get(i).type = PicResourceItemData.PicListItemType.ITEM;
            groupedList.add(list.get(i));
            if (mAdController != null && mAdController.isAddAd(i)) {
                // Logcat.d("插入广告位， 位置 = " + i);
                groupedList.add(new PicResourceItemData(null, PicResourceItemData.PicListItemType.TX_PIC_AD));
            }
        }
        // 刷新列表数据，修复友盟bug
        notifyDataSetChanged();
    }

    public void setList(List<PicResource> list) {
        groupedList.clear();
        if (mAdController != null)
            mAdController.reSet();
        for (int i = 0; i < list.size(); i++) {
            PicResource data = list.get(i);

            groupedList.add(new PicResourceItemData(data, PicResourceItemData.PicListItemType.ITEM));
            if (mAdController != null && mAdController.isAddAd(i)) {
                // Logcat.d("插入广告位， 位置 = " + i);
                groupedList.add(new PicResourceItemData(null, PicResourceItemData.PicListItemType.TX_PIC_AD));
            }
        }
        //刷新列表数据，修复友盟bug
        notifyDataSetChanged();
    }

    @NotNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
        ConstraintLayout layout = (ConstraintLayout) layoutInflater.inflate(R.layout.item_tietu_list,
                parent, false);

        if (viewType == PicResourceItemData.PicListItemType.TX_PIC_AD) {
            FrameLayout frameLayout;
            frameLayout = createADContainer(parent);
            layout.addView(frameLayout, frameLayout.getLayoutParams());

            TextView adMarkTv = AdUtil.createAdMark_small(mContext);
            ViewGroup.LayoutParams layoutParams = adMarkTv.getLayoutParams();
            if (layoutParams instanceof ConstraintLayout.LayoutParams) {
                ConstraintLayout.LayoutParams markTvLayoutParams = (ConstraintLayout.LayoutParams) layoutParams;
                markTvLayoutParams.setMargins(0, 0, itemMargin, itemMargin / 2);
            }
            layout.addView(adMarkTv, layoutParams);
            return new TencentPicADHolder(layout, frameLayout, adMarkTv);
        } else {
            // 两种类型可能不一样，代码保留
            if (mIsTietu) {
                ImageView imageView = new ImageView(mContext);
                int itemCount = AllData.getScreenWidth() > 1000 ? 4 : 3;
                // 屏幕宽 - rcvpadding - item布局margin - iv margin - 额外的1
                int width = (int) ((AllData.getScreenWidth() -
                        mContext.getResources().getDimensionPixelSize(R.dimen.ptu_tietu_list_padding) * 2f) / itemCount)
                        - itemMargin - itemLayoutHorizontalMargin - 1;

                ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(width, width);
                params.leftToLeft = ConstraintLayout.LayoutParams.PARENT_ID;
                params.rightToRight = ConstraintLayout.LayoutParams.PARENT_ID;
                params.topToTop = ConstraintLayout.LayoutParams.PARENT_ID;
                params.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID;

                params.setMargins(itemMargin, itemMargin, itemMargin, itemMargin);
                imageView.setLayoutParams(params);
//                imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                layout.addView(imageView);
                // 添加lockView
                int size = Util.dp2Px(11);
                ImageView lockView = TTRewardVad.createLockView(mContext, layout, size);
                layout.addView(lockView);
                MyItemHolder myItemHolder = new MyItemHolder(layout, imageView);
                myItemHolder.lockView = lockView;
                return myItemHolder;
            } else {
                ImageView imageView = new ImageView(mContext);
                // 需要用parent.getLayoutParams().height设置宽高，不能用match_parent之类设置高度，否则无法正常显示，原因未知
                ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(
                        parent.getLayoutParams().height / rowNumber,
                        parent.getLayoutParams().height / rowNumber);
                params.setMargins(itemMargin + itemLayoutHorizontalMargin, itemMargin, itemMargin + itemLayoutHorizontalMargin, itemMargin);
                imageView.setLayoutParams(params);
                imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                layout.addView(imageView);

                // 添加lockView
                int size = Util.dp2Px(18);
                ImageView lockView = TTRewardVad.createLockView(mContext, layout, size);
                layout.addView(lockView);
                MyItemHolder myItemHolder = new MyItemHolder(layout, imageView);
                myItemHolder.lockView = lockView;
                return myItemHolder;
            }
        }
    }


    private FrameLayout createADContainer(ViewGroup parent) {
        FrameLayout container = new FrameLayout(mContext);
        ConstraintLayout.LayoutParams layoutParams = new ConstraintLayout.LayoutParams(
                parent.getHeight() / DEFAULT_ROW_NUMBER - itemMargin, ViewGroup.LayoutParams.MATCH_PARENT);
        layoutParams.setMargins(itemMargin + itemLayoutHorizontalMargin, itemMargin / 2,
                itemMargin + itemLayoutHorizontalMargin, itemMargin / 2);
        container.setLayoutParams(layoutParams);
        return container;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof MyItemHolder) {
            PicResource tietu = groupedList.get(position).data;
            if (tietu != null && tietu.getUrl() != null) {
                String url = tietu.getUrl().getUrl();
                MyItemHolder myItemHolder = (MyItemHolder) holder;
                CoverLoader.INSTANCE.loadOriginImageView(mContext, BmobUtil.getUrlOfSmallerSize(url), myItemHolder.iv);
                // 注意不能用转换后的url
                Boolean isUnlocked = AdData.sUnlockData.get(String.valueOf(url.hashCode()));
                if (isUnlocked != null && !isUnlocked) {
                    myItemHolder.lockView.setVisibility(View.VISIBLE);
                } else {
                    myItemHolder.lockView.setVisibility(View.GONE);
                }
                myItemHolder.itemView.setOnClickListener(v -> {
                    checkLock_and_exeClick(myItemHolder, myItemHolder.itemView, groupedList.get(position));
                });
            }
        } else if (holder instanceof TencentPicADHolder) {
            if (mAdController != null) {
                TencentPicADHolder tencentPicAdHolder = (TencentPicADHolder) holder;
                mAdController.showAd(position, tencentPicAdHolder, "P图贴图广告 ");
                // Logcat.d("开始展示广告，位置 =  " + position);
            }
        }

    }

    private void checkLock_and_exeClick(MyItemHolder itemHolder, View v, PicResourceItemData item) {
        PicResource picResource = item.data;
        String url = picResource.getUrlString();
        Runnable taskAfterUnlock = () -> { // 相当于一个简便的监听器，监听解锁成功
            item.isUnlock = true;
            notifyDataSetChanged(); // adapter里面设置成已解锁，并更新视图
            if (mItemClickListener != null) {
                mItemClickListener.onItemClick(itemHolder, itemHolder.itemView);
            } // 解锁成功，相当于点击成功，执行正常的点击任务
        };
        if (LockUtil.checkLock(mContext, url, mIsTietu, taskAfterUnlock, true)) {
            // nothing
        } else { // 没有被锁住，执行正常的点击任务
            if (mItemClickListener != null) {
                mItemClickListener.onItemClick(itemHolder, itemHolder.itemView);
            }
        }
        if (isUpdateHeat)
            picResource.updateHeat();  // 锁住不锁住的都仍然更新热度
    }


    @Override
    public int getItemCount() {
        return groupedList.size();
    }

    public PicResourceItemData get(int position) {
        if (position >= groupedList.size()) return null;
        return groupedList.get(position);
    }

    public void setRowNumber(int num) {
        this.rowNumber = num;
    }

    public void add(int id, PicResource picRes) {
        groupedList.add(id, new PicResourceItemData(picRes, PicResourceItemData.PicListItemType.ITEM));
    }

    public interface ItemClickListener {
        void onItemClick(MyItemHolder itemHolder, int position);
    }

    public void setOnItemClickListener(RcvItemClickListener1 listener) {
        this.mItemClickListener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        return groupedList.get(position).type;
    }

    /**
     * 其中的ImageView iv会放入路径
     */
    public static class MyItemHolder extends RecyclerView.ViewHolder {
        ImageView iv;
        ImageView lockView;

        public MyItemHolder(View itemView, ImageView iv) {
            super(itemView);
            this.iv = iv;
        }
    }
}

