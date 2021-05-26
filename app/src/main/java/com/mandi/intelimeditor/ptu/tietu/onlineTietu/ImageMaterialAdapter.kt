package com.mandi.intelimeditor.ptu.tietu.onlineTietu

import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.mandi.intelimeditor.R
import com.mandi.intelimeditor.ad.LockUtil
import com.mandi.intelimeditor.ad.tencentAD.ListAdStrategyController
import com.mandi.intelimeditor.home.tietuChoose.PicResourceItemData
import util.CoverLoader

/**
 * P图界面的贴图列表的Adapter
 */
class ImageMaterialAdapter :
    BaseMultiItemQuickAdapter<PicResourceItemData, BaseViewHolder> {
    private var groupedList = mutableListOf<PicResourceItemData>();

    private val mAdController: ListAdStrategyController? = null
    var isUpdateHeat = true

    constructor() : super() {
        addItemType(PicResourceItemData.PicListItemType.TX_PIC_AD, R.layout.item_tietu_list);
        addItemType(PicResourceItemData.PicListItemType.ITEM, R.layout.item_tietu);
    }

    override fun convert(holder: BaseViewHolder, item: PicResourceItemData) {
        if (holder.itemViewType == PicResourceItemData.PicListItemType.TX_PIC_AD) {
            val frameLayout = createADContainer(holder)
            holder.getView<ConstraintLayout>(R.id.container).addView(frameLayout, frameLayout.layoutParams)
        } else {
            CoverLoader.loadImageView(context, item.data.url?.url, holder.getView<ImageView>(R.id.iv_pic))
            // 注意不能用转换后的url
            val isUnlocked = LockUtil.sUnlockData[item.data.url?.url.hashCode().toString()]
            if (isUnlocked != null && !isUnlocked) {
                holder.getView<View>(R.id.iv_lock).visibility = View.VISIBLE
            } else {
                holder.getView<View>(R.id.iv_lock).visibility = View.GONE
            }
        }
    }

    //
    private fun createADContainer(parent: BaseViewHolder): FrameLayout {
        val container = FrameLayout(context)
        val layoutParams = ConstraintLayout.LayoutParams(
            parent.itemView.height / TietuRecyclerAdapter.DEFAULT_ROW_NUMBER - 4,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        layoutParams.setMargins(
            4 + TietuRecyclerAdapter.itemLayoutHorizontalMargin, 4 / 2,
            4 + TietuRecyclerAdapter.itemLayoutHorizontalMargin, 4 / 2
        )
        container.layoutParams = layoutParams
        return container
    }

    fun setItemList(list: List<PicResourceItemData>) {
        groupedList.clear()
        mAdController?.reSet()
        for (i in list.indices) {
            list[i].type = PicResourceItemData.PicListItemType.ITEM
            groupedList.add(list[i])
            if (mAdController != null && mAdController.isAddAd(i)) {
                // Logcat.d("插入广告位， 位置 = " + i);
                groupedList.add(
                    PicResourceItemData(
                        "",
                        PicResourceItemData.PicListItemType.TX_PIC_AD
                    )
                )
            }
        }
        //刷新列表数据，修复友盟bug
//        notifyDataSetChanged()
        setList(groupedList)
    }

    fun setList(list: List<PicResource>) {
        groupedList.clear()
        mAdController?.reSet()
        for (i in list.indices) {
            val data = list[i]
            groupedList.add(PicResourceItemData(data, PicResourceItemData.PicListItemType.ITEM))
            if (mAdController != null && mAdController.isAddAd(i)) {
                // Logcat.d("插入广告位， 位置 = " + i);
                groupedList.add(
                    PicResourceItemData(
                        "",
                        PicResourceItemData.PicListItemType.TX_PIC_AD
                    )
                )
            }
        }
        setList(groupedList)
//        setNewInstance(groupedList)
        //刷新列表数据，修复友盟bug
//        notifyDataSetChanged()
    }

    fun add(id: Int, picRes: PicResource?) {
        groupedList.add(id, PicResourceItemData(picRes, PicResourceItemData.PicListItemType.ITEM))
    }


}