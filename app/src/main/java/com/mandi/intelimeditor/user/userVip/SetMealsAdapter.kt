package USerVip

import android.content.Context
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.mandi.intelimeditor.bean.VipSetMeal
import com.mandi.intelimeditor.common.util.Util
import com.mandi.intelimeditor.R

/**
 * 会员列表
 */
class SetMealsAdapter(val mContext: Context, var mSetMealNameList: MutableList<VipSetMeal>) : androidx.recyclerview.widget.RecyclerView.Adapter<androidx.recyclerview.widget.RecyclerView.ViewHolder>() {
    private val layoutInflater: LayoutInflater = LayoutInflater.from(mContext)
    private var clickListener: OnItemClickListener? = null

    private var mChosenPosition: Int = 0

    interface OnItemClickListener {
        fun onItemClick(view: View, position: Int)
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.clickListener = listener
    }

    /**
     * 设置VIP套餐列表
     */
    fun setVipList(viplist: MutableList<VipSetMeal>) {
        mSetMealNameList = viplist
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): androidx.recyclerview.widget.RecyclerView.ViewHolder {
        val rootView = layoutInflater.inflate(R.layout.item_layout_vip_set_meal, parent, false)
        return ItemHolder(rootView)
    }

    override fun onBindViewHolder(holder: androidx.recyclerview.widget.RecyclerView.ViewHolder, position: Int) {
        if (position >= mSetMealNameList.size) return
        val vipSetMeal = mSetMealNameList[position]
        //如果是分组标题
        if (holder is ItemHolder) {
            holder.nameTv.text = vipSetMeal.name
            holder.discountPriceTv.text = mContext.getString(R.string.price_format, vipSetMeal.disCountPrice)
            holder.originalPriceTv.paint.flags = Paint.STRIKE_THRU_TEXT_FLAG //中间横线（删除线）
            holder.originalPriceTv.text = mContext.getString(R.string.price_format, vipSetMeal.originalPrice)
            if (mChosenPosition == position) {
                holder.itemView.background = Util.getDrawable(R.drawable.background_round_corner_vip_color)
            } else {
                holder.itemView.background = null
            }
        }
        //点击事件
        holder.itemView.setOnClickListener {
            mChosenPosition = position
            notifyDataSetChanged()
            clickListener?.onItemClick(it, position)
        }
    }

    override fun getItemCount(): Int {
        return mSetMealNameList.size
    }

    inner class ItemHolder internal constructor(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {
        var nameTv: TextView = itemView.findViewById(R.id.vip_set_meal_name)
        var originalPriceTv: TextView = itemView.findViewById(R.id.vip_set_meal_original_price)
        var discountPriceTv: TextView = itemView.findViewById(R.id.vip_set_meal_discount_price)
    }
}
