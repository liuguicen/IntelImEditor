package com.mandi.intelimeditor.home.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import com.mandi.intelimeditor.common.dataAndLogic.AllData
import com.mandi.intelimeditor.common.util.Util
import com.mandi.intelimeditor.home.tietuChoose.PicResourceItemData
import com.mandi.intelimeditor.R
import kotlinx.android.synthetic.main.item_pic_resource.view.*
import util.CoverLoader

class PicGridView : FrameLayout {
    var onClickListener: ((Int) -> Unit)? = null
    var mContext: Context? = null

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        attrs?.let { initView(context, it) }
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        attrs?.let { initView(context, it) }
    }

    private fun initView(context: Context, attrs: AttributeSet) {
        mContext = context
        LayoutInflater.from(context).inflate(R.layout.item_pic_resource, this, true)
        val ta = context.obtainStyledAttributes(attrs, R.styleable.PicGridView)
        ta.recycle()
        //图片
        val layoutParams = picIv.layoutParams
        layoutParams.height = AllData.screenWidth / 3
        layoutParams.width = AllData.screenWidth / 3
        picIv.layoutParams = layoutParams
        //解锁图标
        val size = Util.dp2Px(18f)
        val params = lockView.layoutParams
        params.width = size
        params.height = size
        lockView.layoutParams = params
    }

    fun setPicResource(picResource: PicResourceItemData) {
        if (picResource.isUnlock) {
            lockView.visibility = View.GONE
        } else {
            lockView.visibility = View.VISIBLE
        }
        hotTv.visibility = View.VISIBLE
        tagTv.visibility = View.VISIBLE
        CoverLoader.loadOriginImageView(mContext, picResource.data.url?.url, picIv)
        tagTv.text = picResource.data.tag.toString()
        hotTv.text = Util.showHotInfo(picResource.data)
    }
}