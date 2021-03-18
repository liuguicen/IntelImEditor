package com.mandi.intelimeditor.home.tietuChoose;// package a.baozouptu.chosePicture.tietuChoose;
//
// import android.content.Context;
// import android.support.annotation.NonNull;
// import android.support.annotation.Nullable;
// import android.support.constraint.ConstraintLayout;
// import android.support.constraint.Constraints;
// import android.support.v7.widget.RecyclerView;
// import android.view.LayoutInflater;
// import android.view.View;
// import android.view.ViewGroup;
// import android.widget.ImageView;
// import android.widget.TextView;
//
// import com.bumptech.glide.Glide;
// import com.bumptech.glide.Priority;
// import com.bumptech.glide.load.engine.DiskCacheStrategy;
// import com.bumptech.glide.request.RequestOptions;
// import com.bumptech.glide.request.target.Target;
//
// import org.jetbrains.annotations.NotNull;
//
// import java.util.ArrayList;
// import java.util.List;
//
// import a.baozouptu.MyFrameLayout;
// import a.baozouptu.R;
// import a.baozouptu.ad.AdData;
// import a.baozouptu.ad.tencentAD.AdUtil;
// import a.baozouptu.ad.tencentAD.ListAdPool;
// import a.baozouptu.ad.tencentAD.ListAdStrategyController;
// import a.baozouptu.chosePicture.data.PicListItemType;
// import a.baozouptu.chosePicture.view.TencentADHolder;
// import a.baozouptu.common.util.Logcat;
// import a.baozouptu.common.util.Util;
// import a.baozouptu.ptu.tietu.PicResource;
//
// /**
//  * Created by liuguicen on 2016/8/31.
//  */
// public class TietuGridAdapterOld extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
//
//     private static final float PIC_RESOURCE_MARGIN = 7f; // 注意不要随便改，和广告连在一起的
//
//     /**
//      * 两个广告之间间隔项目的数量，目前设置2屏一个，不足时末尾显示一个
//      */
//     public static String AD_ID;
//
//     private final Context mContext;
//     private final LayoutInflater layoutInflater;
//     private final String mPicFirstClass;
//     private List<PicResourceItemData> imageUrlList = new ArrayList<>();
//     @Nullable
//     private ListAdStrategyController mAdController;
//     private int imagePad;
//
//     private RequestOptions mHighPriorityOption;
//     private RequestOptions mLowPriorityOption;
//     private RequestOptions getRequsetOptions(int position) {
//         if (position < 4) {
//             return mHighPriorityOption;
//         } else {
//             return mLowPriorityOption;
//         }
//     }
//
//
//     public TietuGridAdapterOld(Context context, String firstClass) {
//         mContext = context;
//         this.mPicFirstClass = firstClass;
//         layoutInflater = LayoutInflater.from(context);
//         AD_ID = AdData.getAdIDByPicResourceClass(firstClass);
//
//         mHighPriorityOption = new RequestOptions()
//                 // 既缓存原始图片，又缓存转化后的图片
//                 .diskCacheStrategy(DiskCacheStrategy.ALL)
//                 .priority(Priority.HIGH);// 网络下载会很慢，优先显示几张
//         mLowPriorityOption = new RequestOptions()
//                 // 既缓存原始图片，又缓存转化后的图片
//                 .diskCacheStrategy(DiskCacheStrategy.ALL)
//                 .priority(Priority.LOW); // 网络下载会很慢，优先显示几张
//
//
//         imagePad = Util.dp2Px(PIC_RESOURCE_MARGIN);
//         if (PicResource.FIRST_CLASS_TIETU.equals(firstClass)) {
//             imagePad *= 0.5f;
//         }
//     }
//
//
//     public void initAdData(ListAdPool adPool) {
//         if (adPool == null) return;
//         if (PicResource.FIRST_CLASS_TIETU.equals(mPicFirstClass)) {
//             mAdController = new ListAdStrategyController(mContext, AD_ID, adPool,
//                     1, 13, 24, 20);
//         } else if (PicResource.FIRST_CLASS_TEMPLATE.equals(mPicFirstClass)) {
//             mAdController = new ListAdStrategyController(mContext, AD_ID, adPool,
//                     1, 8, 15, 10);
//         }
//     }
//
//     public List<PicResourceItemData> getImageUrlList() {
//         return imageUrlList;
//     }
//
//     public void setTietuMaterialList(List<PicResource> tietuMaterialList) {
//         if (tietuMaterialList == null) return;
//         imageUrlList.clear();
//         for (int i = 0; i < tietuMaterialList.size(); i++) {
//             if (i == 0) {
//                 imageUrlList.add(new PicResourceItemData(null, PicListItemType.AD));
//                 Logcat.d("add ad Item position = " + imageUrlList.size());
//             }
//             // PicResource picResource = tietuMaterialList.get(i);
//             // PicResourceItemData item = new PicResourceItemData(picResource, PicListItemType.ITEM);
//             // imageUrlList.add(item);
//
//             if (mAdController != null && mAdController.isAddAd(i)) {
//                 imageUrlList.add(new PicResourceItemData(null, PicListItemType.AD));
//                 Logcat.d("add ad Item position = " + imageUrlList.size());
//             }
//         }
//     }
//
//     public void deleteTietuPic(String path) {
//         for (int i = imageUrlList.size() - 1; i >= 0; i--) {
//             PicResource data = imageUrlList.get(i).data;
//             if (data != null && data.getUrl() != null
//                     && data.getUrl().getUrl().equals(path)) {
//                 imageUrlList.remove(i);
//                 return;
//             }
//         }
//     }
//
//     @Override
//     public RecyclerView.ViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
//         // 得出布局，让Item和AdItem使用同样布局，避免出错
//
//         float width, height;
//         if (PicResource.FIRST_CLASS_TEMPLATE.equals(mPicFirstClass)) {
//             width = parent.getWidth() / 2f;
//             height = width * 1.1f;
//         } else {
//             width = parent.getWidth() / 3f;
//             height = width * 1.1f;
//         }
//         MyConstraintLayout layout = new MyConstraintLayout(mContext);
//         RecyclerView.LayoutParams layoutParams =
//                 new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int)height);
//         layout.setLayoutParams(layoutParams);
//
//
//         if (viewType == PicListItemType.AD) {
//             // 创建广告的adLayout
//             int adWidth = (int)(width - (imagePad >> 2));
//             int adHeight = (int) (height - (imagePad >> 2));
//             ConstraintLayout.LayoutParams adContentParams = new ConstraintLayout.LayoutParams(adWidth, adHeight);
//             adContentParams.leftToLeft = ConstraintLayout.LayoutParams.PARENT_ID;
//             adContentParams.topToTop = ConstraintLayout.LayoutParams.PARENT_ID;
//             adContentParams.setMargins(imagePad, imagePad, imagePad, imagePad);
//
//             ConstraintLayout.LayoutParams testParams = new Constraints.LayoutParams(adWidth, adHeight);
//             TextView textView = new TextView(mContext);
//             textView.setText("那个SB写的RecyclerView，自动删除Item");
//             layout.addView(textView, testParams);
//             // 广告的Container
//             MyFrameLayout container = new MyFrameLayout(mContext);
//             container.setMinimumHeight((int)(height - imagePad - imagePad));
//             container.setLayoutParams(adContentParams);
//             Logcat.d("创建了图片资源列表下的广告Item布局，高度为： " + container.getHeight());
//             layout.addView(container, adContentParams);
//             TextView adMarkTv;
//             adMarkTv = AdUtil.createAdMark(mContext);
//             layout.addView(adMarkTv, adMarkTv.getLayoutParams());
//
//             return new TencentADHolder(layout, container, adMarkTv);
//         } else {
//             ConstraintLayout.LayoutParams contentParams = new ConstraintLayout.LayoutParams(
//                 ConstraintLayout.LayoutParams.MATCH_CONSTRAINT, ConstraintLayout.LayoutParams.MATCH_CONSTRAINT);
//             contentParams.leftToLeft = ConstraintLayout.LayoutParams.PARENT_ID;
//             contentParams.topToTop = ConstraintLayout.LayoutParams.PARENT_ID;
//             contentParams.rightToRight = ConstraintLayout.LayoutParams.PARENT_ID;
//             contentParams.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID;
//             contentParams.setMargins(imagePad, imagePad, imagePad, imagePad);
//
//             ImageView imageView = new ImageView(mContext);
//             imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
//
//             layout.addView(imageView, contentParams);
//             final ItemHolder itemHolder = new ItemHolder(layout);
//             itemHolder.iv = imageView;
//             itemHolder.iv.setOnClickListener(v -> clickListener.onItemClick(itemHolder));
//             itemHolder.iv.setOnLongClickListener(v -> longClickListener.onItemLongClick(itemHolder));
//             return itemHolder;
//         }
//     }
//
//     @Override
//     public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
//         if (position >= imageUrlList.size()) return;
//         //如果是分组标题
//         PicResource picResourceData = imageUrlList.get(position).data;
//         String url = null;
//         if (picResourceData != null && picResourceData.getUrl() != null) {
//             url = picResourceData.getUrl().getUrl();
//         }
//         if (holder instanceof ItemHolder) {
//             ((ItemHolder) holder).iv.setImageResource(R.mipmap.instead_icon);
//             RequestOptions override = new RequestOptions()
//                     // 既缓存原始图片，又缓存转化后的图片
//                     .placeholder(R.mipmap.instead_icon)
//                     .diskCacheStrategy(DiskCacheStrategy.ALL)
//                     .priority(Priority.HIGH)
//                     .override(holder.itemView.getWidth(), Target.SIZE_ORIGINAL);// 网络下载会很慢，优先显示几张
//             Glide.with(mContext)
//                     .load(url)
//                     .apply(override)
//                     .into(((ItemHolder) holder).iv);
//         } else if (holder instanceof TencentADHolder) {
//             if (mAdController != null) {
//                 TencentADHolder adHolder = (TencentADHolder) holder;
//                 mAdController.loadAd(position, adHolder,
//                         AdData.getPicResourceAd_PositionName(mPicFirstClass));
//             }
//         }
//     }
//
//     @Override
//     public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
//         super.onViewRecycled(holder);
//         // if (mAdController != null && holder instanceof TencentADHolder) {
//         //     mAdController.onAdHolderRecycled((TencentADHolder) holder);
//         // }
//     }
//
//     @Override
//     public int getItemCount() {
//         return imageUrlList.size();
//     }
//
//     @Override
//     public int getItemViewType(int position) {
//         return imageUrlList.get(position).type;
//     }
//
//     static class ItemHolder extends RecyclerView.ViewHolder {
//         ImageView iv;
//
//         ItemHolder(View itemView) {
//             super(itemView);
//         }
//     }
//
//     public interface ItemClickListener {
//         void onItemClick(ItemHolder itemHolder);
//     }
//
//     public interface LongClickListener {
//         boolean onItemLongClick(ItemHolder itemHolder);
//     }
//
//     private ItemClickListener clickListener;
//     private LongClickListener longClickListener;
//
//     public void setClickListener(ItemClickListener clickListenner) {
//         this.clickListener = clickListenner;
//     }
//
//     public void setLongClickListener(LongClickListener longClickListener) {
//         this.longClickListener = longClickListener;
//     }
//
// }
