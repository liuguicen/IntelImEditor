package text

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.mandi.intelimeditor.bean.PtuTypeface
import com.mandi.intelimeditor.common.util.Util
import com.mandi.intelimeditor.R

/**
 * Created by caiyonglong on 2019/7/9.
 * 底部适配器
 */
class TextTypeAdapter(val mContext: Context, var infos: MutableList<PtuTypeface>) : androidx.recyclerview.widget.RecyclerView.Adapter<androidx.recyclerview.widget.RecyclerView.ViewHolder>() {
    private val layoutInflater: LayoutInflater = LayoutInflater.from(mContext)
    private var clickListener: ItemClickListener? = null
    //预览字体
    private var typefaceItemList = mutableListOf<TypefaceItemBean?>()
    var selectId = 0

    interface ItemClickListener {
        fun onItemClick(view: View, position: Int)
    }

    fun setOnClickListener(clickListenner: ItemClickListener) {
        this.clickListener = clickListenner
    }

    /**
     *设置字体
     */
    fun setTypeFaceList(typefaceList: MutableList<TypefaceItemBean?>) {
        this.typefaceItemList = typefaceList
        this.notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): androidx.recyclerview.widget.RecyclerView.ViewHolder {
        val rootView = layoutInflater.inflate(R.layout.item_typeface_list, parent, false)
        return ItemHolder(rootView)
    }

    override fun onBindViewHolder(holder: androidx.recyclerview.widget.RecyclerView.ViewHolder, position: Int) {
        //如果是分组标题
        if (holder is ItemHolder) {
            if (position == 0) {
                holder.titleTv.text = "默认"
                holder.titleTv.typeface = null
                holder.lockIcon.visibility = View.GONE
            } else {
                val item = infos[position - 1]
                holder.titleTv.text = item.name

                //更新字体
                var typefaceItem = typefaceItemList[position - 1]
                if (typefaceItem != null && typefaceItem.typeface != null) {
                    holder.titleTv.typeface = typefaceItem.typeface
                }
                if (typefaceItem != null) {
                    if (typefaceItem.hasUnlocked) {
                        holder.lockIcon.visibility = View.GONE
                    } else {
                        holder.lockIcon.visibility = View.VISIBLE
                    }
                }
            }

            //设置字体
//            val typeface = typefaceList?.get(position)
//            if (typeface != null)
//                holder.titleTv.typeface = typeface

            //更新颜色
            if (position == selectId) {
                holder.titleTv.setTextColor(Util.getColor(R.color.text_checked_color))
            } else {
                holder.titleTv.setTextColor(Util.getColor(R.color.text_default_color))
            }
            holder.itemView.setOnClickListener {
                clickListener?.onItemClick(holder.titleTv, position)
            }

        }
    }

    override fun getItemCount(): Int {
        return infos.size + 1
    }

    inner class ItemHolder internal constructor(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {
        var titleTv: TextView = itemView.findViewById(R.id.tv_title)
        var lockIcon: ImageView = itemView.findViewById(R.id.lock_icon)
    }
}

