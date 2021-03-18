package com.mandi.intelimeditor.home.localPictuture;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.util.TypedValue;
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

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.mandi.intelimeditor.EventName;
import com.mandi.intelimeditor.ad.ADHolder;
import com.mandi.intelimeditor.ad.AdData;
import com.mandi.intelimeditor.ad.AdStrategyUtil;
import com.mandi.intelimeditor.ad.TTAdConfig;
import com.mandi.intelimeditor.ad.tencentAD.AdUtil;
import com.mandi.intelimeditor.ad.tencentAD.ListAdStrategyController;
import com.mandi.intelimeditor.ad.tencentAD.TxFeedAdPool;
import com.mandi.intelimeditor.common.dataAndLogic.AllData;
import com.mandi.intelimeditor.common.util.LogUtil;

import com.mandi.intelimeditor.common.dataAndLogic.SPUtil;
import com.mandi.intelimeditor.common.util.BmobUtil;
import com.mandi.intelimeditor.common.util.FileTool;

import com.mandi.intelimeditor.common.util.Util;
import com.mandi.intelimeditor.home.BasePicAdapter;
import com.mandi.intelimeditor.home.data.MediaInfoScanner;
import com.mandi.intelimeditor.home.data.UsuPathManger;
import com.mandi.intelimeditor.home.tietuChoose.PicResourceItemData;
import com.mandi.intelimeditor.home.view.TencentPicADHolder;
import com.mandi.intelimeditor.home.viewHolder.HeaderHolder;
import com.mandi.intelimeditor.ptu.tietu.onlineTietu.PicResource;
import com.mathandintell.intelimeditor.BuildConfig;
import com.mathandintell.intelimeditor.R;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import util.CoverLoader;

import com.mandi.intelimeditor.home.tietuChoose.PicResourceItemData.PicListItemType;

/**
 * Created by liuguicen on 2016/8/31.
 * 本地图片列表的适配器
 */
public class LocalPicAdapter extends BasePicAdapter {

    public static int PIC_NUMBER_IN_ONE_PAGE = 12;
    private final LayoutInflater layoutInflater;
    private final MediaInfoScanner mMediaInfoScanner;
    private RequestOptions mRequestOption;
    @Nullable
    private ListAdStrategyController mAdController_pic;
    @Nullable
    private ListAdStrategyController mAdController_feed;

    private List<LocalGroupedItemData> groupedList = new ArrayList<>();
    // TODO: 2020/4/9  对于多选图片制作gif的，目前P图界面不能调整顺序，暂时通过这种方法，按选中顺序控制
    /**
     * 对于多选图片制作gif的，目前P图界面不能调整顺序，暂时通过这种方法，按选中顺序控制
     */
    private List<LocalGroupedItemData> chosenList = new ArrayList<>();

    private int mHeaderPadding;

    public LocalPicAdapter(Context context) {
        super(context);
        layoutInflater = LayoutInflater.from(context);
        mRequestOption = new RequestOptions()
                .error(R.mipmap.instead_icon)
                .placeholder(R.mipmap.instead_icon);
        mItemImagePadding = Util.dp2Px(1f);
        mHeaderPadding = Util.dp2Px(10);
        mMediaInfoScanner = MediaInfoScanner.getInstance();
        spanCount = 3;
    }

    public void initAdData(TxFeedAdPool adPool) {
        if (adPool == null) return;
        AdStrategyUtil adStrategyUtil = new AdStrategyUtil(AdData.AdSpaceName.PIC_RES_FEED, AllData.appConfig.pic_res_ad_strategy);
        if (adStrategyUtil.isShow("TX")) {
            buildTxFeed(false);
        } else { // 默认
            buildTTFeed();
        }
        SPUtil.addAndPutAdSpaceExposeNumber(AdData.AdSpaceName.PIC_RES_FEED);
        // 目前只采用腾讯图片广告的方案
        //        if (AllData.appConfig.pic_resources_ad_strategy == AdData.TENCENT_AD) {
        //        initWithOneTTAd(adPool);
        //        }  else {
        //            addMultiFeedAds(adPool);
        //        }
    }

    private void buildTTFeed() {
        if (AdData.judgeAdClose(AdData.TT_AD)) return;
        // 首屏，1/5的概率显示大广告,设置start=x个屏的数量就行了，第一个广告位会是0-x屏的数字，结果就是1/x
        mAdController_feed = new ListAdStrategyController(mContext, TTAdConfig.PIC_LIST_FEED_AD_ID,
                AdData.getTTFeedAdPool_picList((Activity) mContext),
                PIC_NUMBER_IN_ONE_PAGE * 5,
                PIC_NUMBER_IN_ONE_PAGE * 5,
                PIC_NUMBER_IN_ONE_PAGE * 7,
                false
        );
        mAdController_feed.setMod(3);
        mAdController_feed.setUmEventName(EventName.pic_resource_ad_tt);
    }

    /**
     * @param isFewPic 图片较少
     */
    private void buildTxFeed(boolean isFewPic) {
        if (AdData.judgeAdClose(AdData.TENCENT_AD)) return;
        if (BuildConfig.DEBUG)
            Log.d(TAG, "initTxFeedAd:");

        // 广告比较大，要1/2概率首屏显示，那么需要在1.5屏随机显示，以此类推
        mAdController_feed = new ListAdStrategyController(mContext, AdData.GDT_ID_FEED_PIC_RES_LIST,
                AdData.getTxFeedAdPool(mContext),
                PIC_NUMBER_IN_ONE_PAGE * 6,
                PIC_NUMBER_IN_ONE_PAGE * 6,
                PIC_NUMBER_IN_ONE_PAGE * 8,
                isFewPic);
        mAdController_feed.setMod(3);
        mAdController_feed.setUmEventName(EventName.pic_resource_ad_tx_feed);
    }


    private void initWithOneTTAd(TxFeedAdPool adPool) {
        mAdController_pic = new ListAdStrategyController(mContext,
                AdData.getAdIDByPicResourceClass(PicResource.FIRST_CLASS_LOCAL),
                adPool,
                40, 52, 75, false);
        // 只显示一个TTAD
        mAdController_feed = new ListAdStrategyController(mContext, TTAdConfig.PIC_LIST_FEED_AD_ID,
                AdData.getTTFeedAdPool_picList((Activity) mContext),
                PIC_NUMBER_IN_ONE_PAGE * 6,
                Integer.MAX_VALUE - 10,
                Integer.MAX_VALUE,
                false
        );
        mAdController_feed.setMod(3);
    }

    /**
     * @param imageUrlList
     * @param isInUsu      是否是来自uss的列表
     */
    public void setImageUrls(@NotNull List<String> imageUrlList, boolean isInUsu) {
        groupedList.clear();
        if (mAdController_pic != null)
            mAdController_pic.reSet();
        if (mAdController_feed != null) {
            mAdController_feed.reSet();
        }
        for (int i = 0; i < imageUrlList.size(); i++) {
            String url = imageUrlList.get(i);

            // 分组头
            if (UsuPathManger.isHeader(url)) {
                groupedList.add(new LocalGroupedItemData(url, PicResourceItemData.PicListItemType.GROUP_HEADER));
            } else { // 正常Item
                LocalGroupedItemData itemData = new LocalGroupedItemData(url, PicResourceItemData.PicListItemType.ITEM);
                if (mMediaInfoScanner.isShortVideo(url)) {
                    itemData.mediaType = LocalGroupedItemData.MEDIA_SHORT_VIDEO;
                }
                groupedList.add(itemData);
            }

            // 大广告位
            // 显示大的feedAd，
            if (isInUsu && mAdController_feed != null && mAdController_feed.isAddAd(i)) {

                if (LogUtil.debugPicListFeedAd) {
                    Log.d("LocalPicAdapter", "本地图片页面，显示大广告");
                }
                LocalGroupedItemData ttFeedAdItem = new LocalGroupedItemData("------", PicResourceItemData.PicListItemType.FEED_AD);
                // 第一屏的在最上面更和谐
                groupedList.add(groupedList.size() < PIC_NUMBER_IN_ONE_PAGE * 1.5 ? 0 : groupedList.size(), ttFeedAdItem);

                // 如果第一屏就添加了大广告，那么不要小广告, 将其后移
                if (groupedList.size() < PIC_NUMBER_IN_ONE_PAGE && mAdController_pic != null) {
                    mAdController_pic.setAddPosition(PIC_NUMBER_IN_ONE_PAGE * 4 + groupedList.size());
                }
            }

            // 广告位
            // 小广告位
            if (mAdController_pic != null && mAdController_pic.isAddAd(i)) {
                // Logcat.d("插入广告位， 位置 = " + i);
                groupedList.add(new LocalGroupedItemData(url, PicResourceItemData.PicListItemType.TX_PIC_AD));
            }
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
        if (viewType == PicResourceItemData.PicListItemType.GROUP_HEADER) {
            View view = layoutInflater.inflate(R.layout.pic_gird_group_header, parent, false);
            return new HeaderHolder(view);
        } else {
            ConstraintLayout layout = (ConstraintLayout) layoutInflater.inflate(R.layout.item_local_pic_list,
                    parent, false);

            if (viewType == PicResourceItemData.PicListItemType.TX_PIC_AD) {
                FrameLayout frameLayout = createADContainer(parent);
                layout.addView(frameLayout);
                TextView adMarkTv = AdUtil.createAdMark(mContext);
                layout.addView(adMarkTv, adMarkTv.getLayoutParams());
                return new TencentPicADHolder(layout, frameLayout, adMarkTv);
            } else if (viewType == PicResourceItemData.PicListItemType.FEED_AD) {
                FrameLayout adLayout = ((FrameLayout) layoutInflater.inflate(R.layout.item_feed_ad, parent, false));
                // 注意宽度必须设置成值，后面广告渲染需要用到
                int parentWidth = AllData.screenWidth > 100 ? AllData.screenWidth : parent.getWidth();
                RecyclerView.LayoutParams layoutParams = new RecyclerView.LayoutParams(
                        parentWidth, ViewGroup.LayoutParams.WRAP_CONTENT);
                adLayout.setLayoutParams(layoutParams);
                return new ADHolder(adLayout, adLayout);
            } else {
                View view = layoutInflater.inflate(R.layout.item_local_pic_grid, parent, false);
                final ItemHolder itemHolder = new ItemHolder(view);
                itemHolder.iv.setOnClickListener(v -> clickListener.onItemClick(itemHolder, v));
                itemHolder.iv.setOnLongClickListener(v -> longClickListener.onItemLongClick(itemHolder));
                itemHolder.updateLayoutParams(parent);
                return itemHolder;

            }
        }
    }


    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (position < 0 || position >= groupedList.size()) return;
        if (holder instanceof HeaderHolder) {        //如果是分组标题
            String url = groupedList.get(position).url;
            HeaderHolder headerHolder = (HeaderHolder) holder;
            int bigHeaderPad = (int) (mHeaderPadding * 1.2f);
            headerHolder.tv.setVisibility(View.VISIBLE);
            if (UsuPathManger.USED_FLAG.equals(url)) {//// TODO: 2017/3/1 0001 这里有个挺麻烦的地方，header的View和adapter中的数据对不上，
                headerHolder.tv.setPadding(0, bigHeaderPad, 0, bigHeaderPad);
                headerHolder.tv.setText(R.string.latest_use);
                return;
            } else if (url.startsWith(UsuPathManger.RECENT_FLAG)) {
                if (url.equals(UsuPathManger.RECENT_FLAG)) {
                    headerHolder.tv.setPadding(0, (int) (mHeaderPadding * 2f), 0, (int) (mHeaderPadding * 0.3f));
                    headerHolder.tv.setText(R.string.recent_pic);
                } else {
                    String dataMsg = url.substring(UsuPathManger.RECENT_FLAG.length());
                    headerHolder.tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, mContext.getResources()
                            .getDimension(R.dimen.pic_list_header_small));
                    headerHolder.tv.setPadding(0, mHeaderPadding, 0, mHeaderPadding);
                    headerHolder.tv.setText(dataMsg);
                }
                return;
            } else if (UsuPathManger.PREFER_FLAG.equals(url)) {
                // recent_flag 保证position + 1
                // 没有常用图片
                if (position + 1 < groupedList.size() && groupedList.get(position + 1).url.startsWith(UsuPathManger.RECENT_FLAG)) {
                    headerHolder.tv.setVisibility(View.GONE);
                } else {
                    headerHolder.tv.setPadding(0, bigHeaderPad, 0, bigHeaderPad);
                    headerHolder.tv.setText(R.string.prefer_pic);
                }
                return;
            } else {
                headerHolder.tv.setText(" ");
            }
        } else if (holder instanceof ItemHolder) {
            ItemHolder itemHolder = (ItemHolder) holder;
            //            if (isHeader(position)) {//如果数据错位，item位置的数据时header的
            //                itemHolder.iv.setImageResource(R.mipmap.instead_icon);
            //                return;
            //            }
            // 这个地方主义，imageLoader启动了一个新线程获取图片到cacheImage里面，新线程运行，本线程也会运行，
            // 因为新线程耗时，所以本线程已经执行到后面了，先加载了一张预设的图片，然后这个新线程会使用handler类更新UI线程， 妙啊！
            LocalGroupedItemData itemData = groupedList.get(position);
            String path = itemData.url;
            // Logcat.e("加载位置=："+position + " 路径:" + path);
            // 短视频，加载方法不同
            if (itemData.mediaType != LocalGroupedItemData.MEDIA_SHORT_VIDEO) {
                CoverLoader.INSTANCE.loadImageView(mContext, Uri.fromFile(new File(path)), itemHolder.iv);
                itemHolder.videoSign.setVisibility(View.GONE);
            } else {
                if (FileTool.urlType(path) == FileTool.UrlType.URL) {
                    path = BmobUtil.getUrlOfSmallerSize(path); // 列表中使用又拍云服务，统一转换成小体积的webp的url
                }
                Glide.with(mContext)
                        .load(path)
                        .apply(mRequestOption)
                        .into(itemHolder.iv);
                itemHolder.videoSign.setVisibility(View.VISIBLE);
            }
            if (itemData.isChosen) {
                itemHolder.chooserView.setVisibility(View.VISIBLE);
            } else {
                itemHolder.chooserView.setVisibility(View.GONE);
            }
        } else if (holder instanceof TencentPicADHolder) {
            if (mAdController_pic != null) {
                TencentPicADHolder tencentPicAdHolder = (TencentPicADHolder) holder;
                mAdController_pic.showAd(position, tencentPicAdHolder,
                        AdData.getPicResourceAd_PositionName(PicResource.FIRST_CLASS_LOCAL));
                if (BuildConfig.DEBUG) {
                    LogUtil.d("开始展示广告，位置 =  " + position);
                }
            }
        } else if (holder instanceof ADHolder && mAdController_feed != null) {
            mAdController_feed.showAd(position, ((ADHolder) holder),
                    AdData.getPicResourceAd_PositionName(PicResource.FIRST_CLASS_LOCAL));
        }
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
        super.onViewRecycled(holder);
        if (mAdController_pic != null && holder instanceof TencentPicADHolder) {
            mAdController_pic.onAdHolderRecycled((TencentPicADHolder) holder);
        }
    }

    @Override
    public int getItemCount() {
        return groupedList.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (position < 0 || position >= groupedList.size())
            return PicResourceItemData.PicListItemType.ITEM;
        return groupedList.get(position).type;
    }


    @Nullable
    LocalGroupedItemData getItem(int position) {
        if (position < 0 || position >= groupedList.size()) {
            notifyDataSetChanged();
            return null;
        }
        return groupedList.get(position);
    }

    /**
     * 不通知视图刷新
     */
    public void deleteOnePic(String path) {
        for (int i = groupedList.size() - 1; i >= 0; i--) {
            LocalGroupedItemData itemData = groupedList.get(i);
            if (itemData.url.equals(path)) {
                groupedList.remove(i);
            }
        }
    }

    public void deletePreferPic(String preferPath) {
        boolean canDelete = false;
        for (int i = 0; i < groupedList.size(); i++) {
            LocalGroupedItemData itemData = groupedList.get(i);
            if (itemData.type == PicResourceItemData.PicListItemType.GROUP_HEADER) {
                if (UsuPathManger.PREFER_FLAG.equals(itemData.url)) { // 找到preferPic的开头
                    canDelete = true;
                } else {
                    canDelete = false;
                }
            } else {
                if (canDelete && itemData.url.equals(preferPath)) {
                    groupedList.remove(i);
                    notifyItemRemoved(i);
                    notifyDataSetChanged();
                    return;
                }
            }
        }
    }

    public void addPreferPath(String path) {
        for (int i = groupedList.size() - 1; i >= 0; i--) {
            LocalGroupedItemData itemData = groupedList.get(i);
            if (itemData.type == PicResourceItemData.PicListItemType.GROUP_HEADER) {
                if (UsuPathManger.PREFER_FLAG.equals(itemData.url)) { // 找到preferPic的开头
                    groupedList.add(i + 1, new LocalGroupedItemData(path, PicResourceItemData.PicListItemType.ITEM));
                }
            }
        }
    }

    public void switchChooseItem(int adapterPosition) {
        if (adapterPosition < 0 || adapterPosition >= groupedList.size()) {
            notifyDataSetChanged();
            return;
        }
        LocalGroupedItemData item = groupedList.get(adapterPosition);
        item.isChosen = !item.isChosen;
        if (item.isChosen) {
            chosenList.add(item);
        } else {
            chosenList.remove(item);
        }
        notifyItemChanged(adapterPosition);
    }

    /**
     * 取消多选且刷新视图，不负责检查
     */
    public void cancelMultiChoose() {
        for (LocalGroupedItemData itemData : groupedList) {
            itemData.isChosen = false;
        }
        chosenList.clear();
        notifyDataSetChanged();
    }

    @NotNull
    public List<String> getChooseUrlList(boolean isMakeGif) {
        List<String> urlList = new ArrayList<>();
        for (LocalGroupedItemData itemData : chosenList) {
            urlList.add(itemData.url);
        }
        return urlList;
    }

    /**
     * @return 选中数量，只返回0,1,2
     * <p>2代表大于等于2，减少耗时
     */
    public int getLimitedChosenCount() {
        int count = 0;
        for (LocalGroupedItemData localGroupedItemData : groupedList) {
            if (localGroupedItemData.isChosen) {
                count++;
                if (count >= 2) {
                    break;
                }
            }
        }
        return count;
    }

    public String getOnlyChosenPath() {
        for (LocalGroupedItemData itemData : groupedList) {
            if (itemData.isChosen) {
                return itemData.url;
            }
        }
        return "";
    }


    public class ItemHolder extends RecyclerView.ViewHolder {
        public ImageView iv;
        public ImageView videoSign;
        public ImageView chooserView;

        public ItemHolder(View itemView) {
            super(itemView);
            iv = itemView.findViewById(R.id.picIv);
            videoSign = itemView.findViewById(R.id.videoSignIv);
            chooserView = itemView.findViewById(R.id.chooserView);
        }

        /**
         * 刷新布局参数
         */
        public void updateLayoutParams(ViewGroup parent) {
            ConstraintLayout.LayoutParams layoutParams = new ConstraintLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            ViewGroup.LayoutParams layoutParams1 = iv.getLayoutParams();
            int itemWidth = AllData.screenWidth > 100 ? AllData.screenWidth : parent.getWidth();
            layoutParams1.width = itemWidth / spanCount;
            layoutParams1.height = itemWidth / spanCount;
            LogUtil.d(TAG, "layoutParams1 = " + layoutParams1.width + " = " + layoutParams1.height);
            iv.setLayoutParams(layoutParams1);
            layoutParams.rightMargin = Util.dp2Px(1);
            layoutParams.leftMargin = Util.dp2Px(1);
            layoutParams.topMargin = Util.dp2Px(1);
            layoutParams.bottomMargin = Util.dp2Px(1);
            itemView.setLayoutParams(layoutParams);
        }
    }
}
