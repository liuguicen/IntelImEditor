package com.mathandintell.intelimedit.ptu

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.mandi.intelimeditor.common.dataAndLogic.AllData
import com.mathandintell.intelimedit.bean.FunctionInfoBean
import com.mathandintell.intelimeditor.R

/**
 * Created by caiyonglong on 2019/7/9.
 * 底部适配器
 */
class FunctionAdapter(val mContext: Context, infos: MutableList<FunctionInfoBean>) : androidx.recyclerview.widget.RecyclerView.Adapter<androidx.recyclerview.widget.RecyclerView.ViewHolder>() {
    private val layoutInflater: LayoutInflater = LayoutInflater.from(mContext)
    private var functionInfoBeans = infos

    private var clickListener: ItemClickListener? = null

    interface ItemClickListener {
        fun onItemClick(view: View, position: Int, isLock: Boolean)
    }

    fun setOnClickListener(clickListenner: ItemClickListener) {
        this.clickListener = clickListenner
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): androidx.recyclerview.widget.RecyclerView.ViewHolder {
        val rootView = layoutInflater.inflate(R.layout.item_func_list, parent, false)
        return ItemHolder(rootView)
    }

    override fun onBindViewHolder(holder: androidx.recyclerview.widget.RecyclerView.ViewHolder, position: Int) {
        if (position >= functionInfoBeans.size) return

        val item = functionInfoBeans[position]
        if (holder is ItemHolder) {
            holder.iconIv.setImageResource(item.iconResId)
            holder.iconIv.setBackgroundResource(item.bg)
            holder.itemView.visibility = if (item.isVisible) View.VISIBLE else View.GONE
            holder.lockIv.visibility = if (item.isLocked) View.VISIBLE else View.GONE
            holder.titleTv.setText(item.titleResId)
            holder.itemView.setOnClickListener { v -> clickListener?.onItemClick(v, position, item.isLocked) }
        }
    }

    override fun getItemCount(): Int {
        return functionInfoBeans.size
    }

    inner class ItemHolder internal constructor(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {
        var iconIv: ImageView = itemView.findViewById(R.id.iconIv)
        var lockIv: ImageView = itemView.findViewById(R.id.lockIv)
        var titleTv: TextView = itemView.findViewById(R.id.titleTv)
    }
}

class BottomFunctionAdapter(list: MutableList<FunctionInfoBean>) : BaseQuickAdapter<FunctionInfoBean, BaseViewHolder>(R.layout.item_func_list, list) {

    private var selectIndex = -1

    /**
     * Implement this method and use the helper to adapt the view to the given item.
     *
     * 实现此方法，并使用 helper 完成 item 视图的操作
     *
     * @param helper A fully initialized helper.
     * @param item   The item that needs to be displayed.
     */
    override fun convert(holder: BaseViewHolder, item: FunctionInfoBean) {
        holder.setImageResource(R.id.iconIv, item.iconResId)
        if (item.bg != -1) {
            holder.setBackgroundResource(R.id.iconIv, item.bg)
        } else {
            holder.getView<ImageView>(R.id.iconIv).background = null
        }
        holder.itemView.visibility = if (item.isVisible) View.VISIBLE else View.GONE
        holder.setVisible(R.id.lockIv, item.isLocked)
        holder.setText(R.id.titleTv, item.titleResId)

        //是否有选中状态
        if (item.isCanSelected) {
            if (selectIndex == holder.adapterPosition) {
                holder.getView<ImageView>(R.id.iconIv).setColorFilter(Color.parseColor("#ffff5722"))
            } else {
                holder.getView<ImageView>(R.id.iconIv).colorFilter = null
            }
        }
        //少于等于5个，等比显示
        if (data.size <= 5) {
            val lp = holder.itemView.layoutParams
            lp?.width = AllData.screenWidth / data.size
            holder.itemView.layoutParams = lp
        }
    }

    /**
     * 更新选中index
     */
    fun updateSelectIndex(index: Int) {
        selectIndex = if (index == selectIndex) -1 else index
        notifyDataSetChanged()
    }
}
