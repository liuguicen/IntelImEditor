package com.mathandintell.intelimedit.home.search

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.mathandintell.intelimeditor.R

/**
 * 作者：yonglong
 */
class SearchHistoryAdapter(suggestions: MutableList<String>) : BaseQuickAdapter<String, BaseViewHolder>(R.layout.item_search_hot, suggestions) {
    override fun convert(holder: BaseViewHolder, item: String) {
        holder.setText(R.id.titleTv, item)
    }
}
