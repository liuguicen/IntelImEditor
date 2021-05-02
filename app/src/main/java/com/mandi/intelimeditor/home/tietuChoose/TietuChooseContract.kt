package com.mandi.intelimeditor.home.tietuChoose

import com.mandi.intelimeditor.BasePresenter
import com.mandi.intelimeditor.BaseView
import com.mandi.intelimeditor.home.tietuChoose.PicResourceItemData
import com.mandi.intelimeditor.home.tietuChoose.PicResourcesAdapter
import com.mandi.intelimeditor.ptu.tietu.onlineTietu.PicResource

/**
 * <pre>
 *      author : liuguicen
 *      time : 2019/01/31
 *      version : 1.0
 * <pre>
 */
interface TietuChooseContract {
    interface View : BaseView<Presenter> {
        fun refreshCategoryList()
        fun onDownloadStateChange(isSuccess: Boolean, list: MutableList<PicResource>?)
        fun hideCategoryList()
        fun showTagList(data: MutableList<String>)
        fun setCategoryList(cateGoryList: MutableList<PicResourceItemData>)
    }

    interface Presenter : BasePresenter {
        fun createPicAdapter(): PicResourcesAdapter
        fun loadTietuByCategory(secondClass: String)
        fun isDownloadSuccess(): Boolean
        fun deleteOneMyTietu(path: String)
        fun refresh()
        fun loadData()
    }
}