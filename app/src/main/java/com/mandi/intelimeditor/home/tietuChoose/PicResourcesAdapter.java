package com.mandi.intelimeditor.home.tietuChoose;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
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

import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.mandi.intelimeditor.EventName;
import com.mandi.intelimeditor.ad.ADHolder;
import com.mandi.intelimeditor.ad.AdData;
import com.mandi.intelimeditor.ad.AdStrategyUtil;
import com.mandi.intelimeditor.ad.LockUtil;
import com.mandi.intelimeditor.ad.TTAdConfig;
import com.mandi.intelimeditor.ad.tencentAD.AdUtil;
import com.mandi.intelimeditor.ad.tencentAD.ListAdStrategyController;
import com.mandi.intelimeditor.common.dataAndLogic.AllData;
import com.mandi.intelimeditor.common.dataAndLogic.SPUtil;
import com.mandi.intelimeditor.common.util.BmobUtil;

import com.mandi.intelimeditor.common.util.LogUtil;
import com.mandi.intelimeditor.common.util.Util;
import com.mandi.intelimeditor.home.BasePicAdapter;
import com.mandi.intelimeditor.home.data.PicDirInfo;
import com.mandi.intelimeditor.home.search.PicResSearchSortUtil;
import com.mandi.intelimeditor.home.view.TencentPicADHolder;
import com.mandi.intelimeditor.home.viewHolder.FolderHolder;
import com.mandi.intelimeditor.home.viewHolder.GroupHolder;
import com.mandi.intelimeditor.home.viewHolder.NewFeatureHeaderHolder;
import com.mandi.intelimeditor.ptu.tietu.onlineTietu.PicResGroup;
import com.mandi.intelimeditor.ptu.tietu.onlineTietu.PicResGroupItemData;
import com.mandi.intelimeditor.ptu.tietu.onlineTietu.PicResource;
import com.mandi.intelimeditor.BuildConfig;
import com.mandi.intelimeditor.R;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import com.mandi.intelimeditor.common.util.CoverLoader;

/**
 * Created by liuguicen on 2016/8/31.
 * 服务器上面的图片资源列表的适配器，目前用于模板和不同类别的贴图
 */
public class PicResourcesAdapter extends BasePicAdapter {
    public static final String TAG = "PicResourcesAdapter";

    public static final float PIC_NUMBER_FOR_1_COL = 2f;
    public static final float PIC_NUMBER_FOR_2_COL = 7.5f;
    public static final float PIC_NUMBER_FOR_3_COL = 15; // 一页能显示的贴图数量，目前大概5行，15张左右
    public static final float PIC_NUMBER_FOR_4_COL = 30f;
    public static String AD_ID;
    private final LayoutInflater layoutInflater;

    @Nullable
    private ListAdStrategyController mAdController_feed;

    private List<PicResourceItemData> itemDataList = new ArrayList<>();

    private float numberInOneScreen;

    private RequestOptions mHighPriorityOption;
    private RequestOptions mLowPriorityOption;

    private boolean isAddNewFeatureHeader = false;
    private boolean isShowPreview = false; //是否显示预览图
    private String adPositionName = "图片列表";

    private RequestOptions getRequsetOptions(int position) {
        if (position < 4) {
            return mHighPriorityOption;
        } else {
            return mLowPriorityOption;
        }
    }

    public void setShowPreview(boolean showPreview) {
        isShowPreview = showPreview;
    }

    public PicResourcesAdapter(Context context, int spanCount) {
        super(context);
        layoutInflater = LayoutInflater.from(context);
        mHighPriorityOption = new RequestOptions()
                // 既缓存原始图片，又缓存转化后的图片
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .priority(Priority.HIGH);// 网络下载会很慢，优先显示几张
        mLowPriorityOption = new RequestOptions()
                // 既缓存原始图片，又缓存转化后的图片
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .priority(Priority.LOW); // 网络下载会很慢，优先显示几张

        this.spanCount = spanCount;
        if (spanCount == 3) {
            mItemImagePadding = context.getResources()
                    .getDimensionPixelOffset(R.dimen.pic_resource_list_tietu_margin);
            mIsTietu = true;
            numberInOneScreen = PIC_NUMBER_FOR_3_COL;
        } else if (spanCount == 2) {
            mItemImagePadding = context.getResources()
                    .getDimensionPixelOffset(R.dimen.pic_resource_list_template_margin);
            numberInOneScreen = PIC_NUMBER_FOR_2_COL;
        } else {
            mItemImagePadding = context.getResources()
                    .getDimensionPixelOffset(R.dimen.pic_resource_list_template_margin);
            numberInOneScreen = PIC_NUMBER_FOR_1_COL;
        }
    }


    /**
     * 内部会判断是否关闭对应广告
     *
     * @param isFewPic 是否是少图模式，设置为true会在末尾添加大的信息流广告
     */
    public void initAdData(boolean isFewPic) {
        AdStrategyUtil adStrategyUtil = new AdStrategyUtil(AdData.AdSpaceName.PIC_RES_FEED, AllData.appConfig.pic_res_ad_strategy);
        if (adStrategyUtil.isShow("TX")) {
            buildTxFeed(false, isFewPic);
        } else { // 默认
            buildTTFeed(false, isFewPic);
        }
        SPUtil.addAndPutAdSpaceExposeNumber(AdData.AdSpaceName.PIC_RES_FEED);
    }

    /**
     * @param isOnly   是否只显示这一家的广告
     * @param isFewPic 图片比较少的情况
     */
    private void buildTTFeed(boolean isOnly, boolean isFewPic) {
        if (AdData.judgeAdClose(AdData.TT_AD)) return;

        Log.d(TAG, "AddTTFeed");

        // 广告比较大，要1/2概率首屏显示，那么需要在1.5屏随机显示，以此类推
        float numberInOneScreen = this.numberInOneScreen;
        if (isFewPic) numberInOneScreen *= 0.66;
        mAdController_feed = new ListAdStrategyController(mContext, TTAdConfig.PIC_LIST_FEED_AD_ID,
                AdData.getTTFeedAdPool_picList((Activity) mContext),
                (int) (numberInOneScreen * (isOnly ? 3 : 5)),
                (int) (numberInOneScreen * (isOnly ? 4 : 5)),
                (int) (numberInOneScreen * (isOnly ? 5 : 6)),
                isFewPic);
        mAdController_feed.setMod(mIsTietu ? 3 : 2);
        mAdController_feed.setUmEventName(EventName.pic_resource_ad_tt);
    }


    /**
     * @param isOnly
     * @param isFewPic 图片较少
     */
    private void buildTxFeed(boolean isOnly, boolean isFewPic) {
        if (AdData.judgeAdClose(AdData.TENCENT_AD)) return;
        if (BuildConfig.DEBUG)
            Log.d(TAG, "initTxFeedAd:");

        // 广告比较大，要1/2概率首屏显示，那么需要在1.5屏随机显示，以此类推
        float numberInOneScreen = this.numberInOneScreen;
        if (isFewPic) numberInOneScreen *= 0.66;
        mAdController_feed = new ListAdStrategyController(mContext, AdData.GDT_ID_FEED_PIC_RES_LIST,
                AdData.getTxFeedAdPool(mContext),
                (int) (numberInOneScreen * (isOnly ? 3 : 5)),
                (int) (numberInOneScreen * (isOnly ? 4 : 5)),
                (int) (numberInOneScreen * (isOnly ? 5 : 6)),
                isFewPic);
        mAdController_feed.setMod(mIsTietu ? 3 : 2);
        mAdController_feed.setUmEventName(EventName.pic_resource_ad_tx_feed);
    }

    /**
     * 更新图片数据列表
     * todo 应该将两个方法合并，不要写这种大量重复的代码
     */
    public void setImageUrls(List<?> picList, List<PicDirInfo> folders) {
        if (mAdController_feed != null)
            mAdController_feed.reSet();
        itemDataList.clear();
        int extraSize = 0;

        if (folders != null) {
            //遍历文件夹所有数据，根据数据分类型添加到集合中
            for (int i = 0; i < folders.size(); i++) {
                PicDirInfo picDirInfo = folders.get(i);
                PicResourceItemData item = new PicResourceItemData(picDirInfo, PicResourceItemData.PicListItemType.ITEM_FOLDER);
                itemDataList.add(item);
            }
        }

        //遍历所有数据，根据数据分类型添加到集合中
        for (int i = 0; i < picList.size(); i++) {
            Object obj = picList.get(i);
            PicResourceItemData item;
            if (obj instanceof PicResource) {
                item = new PicResourceItemData((PicResource) obj, PicResourceItemData.PicListItemType.ITEM);
            } else if (obj instanceof PicResGroup) {
                item = new PicResourceItemData((PicResGroup) obj, PicResourceItemData.PicListItemType.GROUP);
            } else if (obj instanceof PicResourceItemData) {
                item = (PicResourceItemData) obj;
            } else {
                continue;
            }
            itemDataList.add(item);

            // 插入信息流大广告
            // 这里isAddAd(groupedList.size() + extraSize) 判断一行的末尾，因为加大广告多占了位置，忽略之
            if (mAdController_feed != null && mAdController_feed.isAddAd(itemDataList.size() + extraSize)) {
                PicResourceItemData adItem = new PicResourceItemData("", PicResourceItemData.PicListItemType.FEED_AD);
                if (LogUtil.debugPicListFeedAd) {
                    Log.d(this.getClass().getSimpleName(), "setImageUrls: 添加信息流正常大小广告, 位置 = " + itemDataList.size());
                }
                // 首屏的直接加在第一个，效果更好
                itemDataList.add(i < numberInOneScreen * 2 ? 0 : itemDataList.size(), adItem);
                // 大广告，一个当一行，会多占用增加line_number - 1个位置 ，这样才能让大广告在完整的行的后面，需要计算到
                int line_number = mIsTietu ? 3 : 2;
                extraSize += line_number - 1;
            }
        }
        if (mAdController_feed != null
                && (mAdController_feed.isAddInEnd(itemDataList.size() + extraSize)
                || (mAdController_feed.isEndAdd() && extraSize == 0))) {//末尾添加模式，且最后一次添加位置不靠近末尾，或者从未添加过，添加
            PicResourceItemData adItem = new PicResourceItemData("", PicResourceItemData.PicListItemType.FEED_AD);
            itemDataList.add(itemDataList.size(), adItem);
        }
        //顶部增加表情换脸功能
        if (isAddNewFeatureHeader) {
            itemDataList.add(0, new PicResourceItemData(mContext.getString(R.string.expression_change_face)));
        }
        notifyDataSetChanged();
    }

    public void clear() {
        itemDataList.clear();
        notifyDataSetChanged();
    }

    @NotNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
        if (viewType == PicResourceItemData.PicListItemType.ITEM_FOLDER) {
            View view = layoutInflater.inflate(R.layout.item_search_folder, parent, false);
            FolderHolder folderHolder = new FolderHolder(view);
            view.setOnClickListener(v -> clickListener.onItemClick(folderHolder, v));
            return folderHolder;
        } else if (viewType == PicResourceItemData.PicListItemType.GROUP) {
            return createGroupHolder(parent);
        } else if (viewType == PicResourceItemData.PicListItemType.NEW_FEATURE_HEADER) {
            return createNewHeaderHolder(parent);
        } else if (viewType == PicResourceItemData.PicListItemType.FEED_AD
                || viewType == PicResourceItemData.PicListItemType.TX_PIC_AD) {
            return createAdHolder(parent, viewType);
        } else {  // item
            View rootView;
            if (isShowPreview) {
                rootView = layoutInflater.inflate(R.layout.item_pic_resource_style, parent, false);
            } else {
                rootView = layoutInflater.inflate(R.layout.item_pic_resource, parent, false);
            }
            final ItemHolder itemHolder = new ItemHolder(rootView);
            itemHolder.updateLayoutParams(parent);
            return itemHolder;
        }
    }

    /**
     * 模版列表头部布局
     */
    private RecyclerView.ViewHolder createNewHeaderHolder(ViewGroup parent) {
        View view = layoutInflater.inflate(R.layout.item_pic_gird_header, parent, false);
        return new NewFeatureHeaderHolder(view);
    }

    private RecyclerView.ViewHolder createAdHolder(ViewGroup parent, int viewType) {
        ConstraintLayout layout;
        if (spanCount == 3) {
            layout = (ConstraintLayout) layoutInflater.inflate(R.layout.item_pic_resource_tietu_list,
                    parent, false);
        } else {
            layout = (ConstraintLayout) layoutInflater.inflate(R.layout.item_pic_resource_template_list,
                    parent, false);
        }

        if (viewType == PicResourceItemData.PicListItemType.TX_PIC_AD) {
            if (LogUtil.debugPicResource) {
                Log.d(TAG, "创建存图片广告View");
            }
            FrameLayout frameLayout = createADContainer(parent);
            layout.addView(frameLayout);

            TextView adMarkTv = AdUtil.createAdMark(mContext);
            layout.addView(adMarkTv, adMarkTv.getLayoutParams());  // 纯图广告和图片资源用相同的layout
            return new TencentPicADHolder(layout, frameLayout, adMarkTv);
        } else {
            return createFeedAdHolder(layoutInflater, parent);
        }
    }

    /**
     * 分组
     */
    private GroupHolder createGroupHolder(ViewGroup parent) {
        View view = layoutInflater.inflate(R.layout.item_pic_gird_group, parent, false);
        GroupHolder headerHolder = new GroupHolder(view);
        headerHolder.moreTv.setOnClickListener(v -> {
            clickListener.onItemClick(headerHolder, v);
        });
        headerHolder.picGridView.setOnClickListener(v -> {
            int position = headerHolder.getLayoutPosition();
            if (position == -1) return;
            checkLock_andExeClick(headerHolder, v, itemDataList.get(position).picResGroup.resItemList.get(0));
        });
        headerHolder.picGridView2.setOnClickListener(v -> {
            int position = headerHolder.getLayoutPosition();
            if (position == -1) return;
            checkLock_andExeClick(headerHolder, v, itemDataList.get(position).picResGroup.resItemList.get(1));
        });
        headerHolder.picGridView3.setOnClickListener(v -> {
            int position = headerHolder.getLayoutPosition();
            if (position == -1) return;
            checkLock_andExeClick(headerHolder, v, itemDataList.get(position).picResGroup.resItemList.get(2));
        });
        return headerHolder;
    }


    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        PicResource picResourceData = itemDataList.get(position).data;
        String url = null;
        if (picResourceData != null && picResourceData.getUrl() != null) {
            url = picResourceData.getUrl().getUrl();
        }
        if (holder instanceof ItemHolder) {
            bindItem((ItemHolder) holder, position, url);
        } else if (holder instanceof ADHolder) {
            bindAd((ADHolder) holder, position);
        } else if (holder instanceof GroupHolder) {
            bindGroup((GroupHolder) holder, position);
        } else if (holder instanceof FolderHolder) {
            bindFolder((FolderHolder) holder, position);
        } else if (holder instanceof NewFeatureHeaderHolder) {
            bindTopChangeFaceHeader((NewFeatureHeaderHolder) holder, position);
        }
    }

    private void bindItem(ItemHolder itemHolder, int position, String url) {
        if (itemDataList.get(position).isUnlock) {
            itemHolder.lockView.setVisibility(View.GONE);
        } else {
            itemHolder.lockView.setVisibility(View.VISIBLE);
        }
        PicResource itemData = itemDataList.get(position).data;
        LogUtil.d(TAG, " rst url =" + BmobUtil.getUrlOfSmallerSize(itemData.getRstUrl()));
        if (itemData != null) {
            String tag = itemData.getTag();
            //只有首页列表显示预览图
            if (isShowPreview && !TextUtils.isEmpty(BmobUtil.getUrlOfSmallerSize(itemData.getRstUrl()))) {
                itemHolder.setViewVisible(itemHolder.previewPicIv, true);
                CoverLoader.INSTANCE.loadOriginImageView(mContext, BmobUtil.getUrlOfSmallerSize(itemData.getRstUrl()), itemHolder.previewPicIv);
            } else {
                itemHolder.setViewVisible(itemHolder.previewPicIv, false);
            }
            if (itemData.getHeat() != null && itemData.getHeat() != 0 && tag != null) {
                itemHolder.hotTv.setVisibility(View.VISIBLE);
                itemHolder.tagTv.setVisibility(View.VISIBLE);
                itemHolder.tagTv.setText(tag.replace("-", " "));
                itemHolder.hotTv.setText(Util.showHotInfo(itemData));
            } else {
                itemHolder.hotTv.setVisibility(View.GONE);
                itemHolder.tagTv.setVisibility(View.GONE);
            }
        } else {
            itemHolder.hotTv.setVisibility(View.GONE);
            itemHolder.tagTv.setVisibility(View.GONE);
            itemHolder.setViewVisible(itemHolder.previewPicIv, false);
        }
        itemHolder.iv.setOnClickListener(v -> {
            checkLock_andExeClick(itemHolder, v, itemDataList.get(position));
        });
        itemHolder.iv.setOnLongClickListener(v -> {
            if (longClickListener != null) {
                longClickListener.onItemLongClick(itemHolder);
            }
            return true;
        });
        CoverLoader.INSTANCE.loadOriginImageView(mContext, BmobUtil.getUrlOfSmallerSize(url), itemHolder.iv);
    }

    /**
     * 在Adapter里面检测是否加锁，并弹出解锁对话框，因为adapter复用，增加代码复用以及避免出错
     * 检测加锁，并执行点击事件
     */
    private void checkLock_andExeClick(RecyclerView.ViewHolder itemHolder, View v, PicResourceItemData item) {
        PicResource picResource = item.data;
        String url = picResource.getUrlString();
        Runnable taskAfterUnlock = () -> { // 相当于一个简便的监听器，监听解锁成功
            item.isUnlock = true;
            notifyDataSetChanged(); // adapter里面设置成已解锁，并更新视图
            if (clickListener != null)
                clickListener.onItemClick(itemHolder, v);  // 解锁成功，相当于点击成功，执行正常的点击任务
        };
        if (LockUtil.checkLock(mContext, url, mIsTietu, taskAfterUnlock, true)) { // 需要解锁的，通过解锁执行点击任务
            picResource.updateHeat();  // 锁住的仍然更新热度
        } else { // 没有被锁住，执行正常的点击任务
            if (clickListener != null)
                clickListener.onItemClick(itemHolder, v);
        }
    }

    private void bindAd(ADHolder holder, int position) {
        if (mAdController_feed != null) { // 信息流
            mAdController_feed.showAd(position, holder, adPositionName);
            if (LogUtil.debugPicResource) {
                Log.d(TAG, "onBindViewHolder: 展示信息流广告， position = " + position);
            }
        }
    }

    /**
     * 设置头部标题
     */
    private void bindTopChangeFaceHeader(NewFeatureHeaderHolder holder, int position) {
        if (!TextUtils.isEmpty(itemDataList.get(position).newFeatureTitle)) {
            holder.changeFaceTitleTv.setText(itemDataList.get(position).newFeatureTitle);
            if (!AllData.hasReadConfig.hasRead_changeFace_acGuide()) {
                holder.changeFaceNewNoticeTv.setVisibility(View.VISIBLE);
            }
            holder.itemView.setOnClickListener(v -> {
                if (clickListener != null) {
                    clickListener.onItemClick(holder, v);
                }
            });
        }
    }

    /**
     * 设置分组标题
     */
    private void bindGroup(GroupHolder holder, int position) {
        PicResGroupItemData picResGroup = itemDataList.get(position).picResGroup;
        LogUtil.d(TAG, "分组 = " + picResGroup.heat + " position =" + position);
        if (!TextUtils.isEmpty(picResGroup.title)) {
            holder.titleTv.setText(picResGroup.title);
            holder.picGridView.setPicResource(picResGroup.resItemList.get(0));
            holder.picGridView2.setPicResource(picResGroup.resItemList.get(1));
            holder.picGridView3.setPicResource(picResGroup.resItemList.get(2));
        }
    }

    /**
     * 设置文件
     */
    private void bindFolder(FolderHolder itemHolder, int position) {
        itemHolder.mTvTitle.setVisibility(View.VISIBLE);
        itemHolder.mTvTitle.setText(itemDataList.get(position).picDirInfo.getDirPath());
        itemHolder.mTvInfo.setText(itemDataList.get(position).picDirInfo.getPicNumInfo());
        String path = itemDataList.get(position).picDirInfo.getRepresentPicPath();
        CoverLoader.INSTANCE.loadImageView(mContext, path, itemHolder.mIvFile);
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
        super.onViewRecycled(holder);
        if (mAdController_feed != null && holder instanceof ADHolder) {
            mAdController_feed.onAdHolderRecycled((ADHolder) holder);
        }
    }

    @Override
    public int getItemCount() {
        return itemDataList.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (position < itemDataList.size()) {
            return itemDataList.get(position).type;
        } else {
            return PicResourceItemData.PicListItemType.ITEM;
        }
    }

    public PicResourceItemData getItem(int position) {
        if (0 <= position && position < itemDataList.size()) {
            return itemDataList.get(position);
        }
        return null;
    }

    public List<PicResourceItemData> getImageUrlList() {
        return itemDataList;
    }

    public void deleteTietuPic(String path) {
        for (int i = itemDataList.size() - 1; i >= 0; i--) {
            PicResource data = itemDataList.get(i).data;
            if (data != null && data.getUrl() != null
                    && data.getUrl().getUrl().equals(path)) {
                itemDataList.remove(i);
                return;
            }
        }
    }

    /**
     * 排序，如果使用sort排序，需要先去除广告ViewHolder,然后再排，最后再插入广告。
     */
    public void sortPicList(List<PicResource> resList, int sortType, boolean isReduce) {
        List<PicResource> newList = new ArrayList<>(resList);
        LogUtil.recordTime();
        PicResSearchSortUtil.sortPicRes(newList, sortType, isReduce);
        LogUtil.logTimeConsumeAndRecord("排序");
        //刷新列表
        setImageUrls(newList, null);
        LogUtil.logTimeConsumeAndRecord("放入数据");
    }

    public static List<PicResource> randomInsertForHeat(List<PicResource> groupedList) {
        for (int i = 0; i < 50 && i < groupedList.size(); i++) {
            if (AllData.sRandom.nextFloat() < 0.3f) {
                int swapID = AllData.sRandom.nextInt(groupedList.size() - i) + i;
                PicResource t = groupedList.get(i);
                groupedList.set(i, groupedList.get(swapID));
                groupedList.set(swapID, t);
            }
        }
        return groupedList;
    }

    public void deleterecent_style(String path) {

    }


    public class ItemHolder extends RecyclerView.ViewHolder {
        public ImageView iv, previewPicIv;
        public ImageView lockView;
        public TextView hotTv;
        public TextView tagTv;
//        public TextView functionTv; 展示不用了

        public ItemHolder(View itemView) {
            super(itemView);
            previewPicIv = itemView.findViewById(R.id.previewPicIv);
            iv = itemView.findViewById(R.id.picIv);
            lockView = itemView.findViewById(R.id.lockView);
            hotTv = itemView.findViewById(R.id.hotTv);
            tagTv = itemView.findViewById(R.id.tagTv);
//            functionTv = itemView.findViewById(R.id.item_function_tv);
        }

        /**
         * 刷新布局参数
         */
        public void updateLayoutParams(ViewGroup parent) {
            RecyclerView.LayoutParams layoutParams = new RecyclerView.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            ViewGroup.LayoutParams ivParams = iv.getLayoutParams();
            int rowWidth = AllData.screenWidth > 100 ? AllData.screenWidth : parent.getWidth();
            ivParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
            ivParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
//          LogUtil.d(TAG, "ivParams = " + ivParams.width + " = " + ivParams.height);
            iv.setLayoutParams(ivParams);

            layoutParams.bottomMargin = Util.dp2Px(7.5f);
            layoutParams.topMargin = Util.dp2Px(5);
            layoutParams.leftMargin = Util.dp2Px(2.5f);
            layoutParams.rightMargin = Util.dp2Px(2.5f);
            //解锁图标
            int size = mIsTietu ? Util.dp2Px(18) : Util.dp2Px(24);
            ViewGroup.LayoutParams params = lockView.getLayoutParams();
            params.width = size;
            params.height = size;
            lockView.setLayoutParams(params);

            itemView.setLayoutParams(layoutParams);
        }

        public void setViewVisible(View view, boolean visible) {
            if (view != null) {
                view.setVisibility(visible ? View.VISIBLE : View.GONE);
            }
        }
    }

    public void setAddNewFeatureHeader(boolean addNewFeatureHeader) {
        isAddNewFeatureHeader = addNewFeatureHeader;
    }
}
