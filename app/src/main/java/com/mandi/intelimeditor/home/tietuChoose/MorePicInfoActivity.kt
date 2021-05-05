package com.mandi.intelimeditor.home.tietuChoose

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.mandi.intelimeditor.common.BaseActivity
import com.mandi.intelimeditor.home.tietuChoose.PicResourcesFragment
import com.mandi.intelimeditor.ptu.tietu.onlineTietu.PicResource
import com.mandi.intelimeditor.R

/**
 * 点击更多打开分组更多界面，增加一个界面，保留首页界面状态，避免点击分组打乱了上一列表状态
 */
class MorePicInfoActivity : BaseActivity() {

    private var mFirstClass = ""
    private var mSecondClass = ""
    private var mCategory = ""
    private var position = 0

    companion object {
        const val TAG_POSITION = "tag_position";

        /**
         * 打开更多图片界面
         */
        fun start(context: AppCompatActivity, firstClass: String, secondClass: String, category: String, position: Int) {
            val intent = Intent(context, MorePicInfoActivity::class.java)
            intent.putExtra(PicResourcesFragment.FIRST_CLASS, firstClass)
            intent.putExtra(PicResourcesFragment.SECOND_CLASS, secondClass)
            intent.putExtra(TAG_POSITION, position)
            context.startActivity(intent)
        }
    }

    override fun getLayoutResId(): Int {
        return R.layout.activity_more_pic_info
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        position = intent.getIntExtra(TAG_POSITION, 0)
        mFirstClass = intent.getStringExtra(PicResourcesFragment.FIRST_CLASS).toString()
        mSecondClass = intent.getStringExtra(PicResourcesFragment.SECOND_CLASS).toString()

//        tagTitles = PicResourceDownloader.getTagListByCate(mSecondClass)

        title = when {
            PicResource.SECOND_CLASS_BASE.equals(mSecondClass) ->
                "模版分组"
            PicResource.SECOND_CLASS_EXPRESSION.equals(mSecondClass) ->
                "表情分组"
            PicResource.SECOND_CLASS_PROPERTY.equals(mSecondClass) ->
                "道具分组"
            else -> ""
        }

    }

}