package USeruse.tutorial

import android.os.Bundle
import android.view.View
import com.mandi.intelimeditor.common.util.LogUtil
import com.mandi.intelimeditor.user.useruse.tutorial.GuideAdapter
import com.mandi.intelimeditor.user.useruse.tutorial.Tutorial
import com.mathandintell.intelimedit.dialog.IBaseDialog
import com.mathandintell.intelimeditor.R
import kotlinx.android.synthetic.main.dialog_tutorial.*
import util.CoverLoader

/**
 * 作者：yonglong
 * 包名：a.baozouptu.widget
 * 时间：2019/4/3 13:21
 * 描述：
 */
class GuideDialog : IBaseDialog() {
    private var index = 0
    //功能点
    private var functions = mutableListOf<Tutorial>()
    private var mAdapter: GuideAdapter? = null;

    fun setGuideAdapter(guides: MutableList<Tutorial>): GuideDialog {
        functions = guides;
        LogUtil.d(TAG, "setGuideAdapter " + functions.size)
        return this
    }

    override fun getLayoutResId(): Int {
        return R.layout.dialog_tutorial
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        closeIv?.setOnClickListener {
            if (previewIv.visibility == View.VISIBLE) {
                previewIv.visibility = View.GONE
                tutorialRcv.visibility = View.VISIBLE
            } else {
                dismissAllowingStateLoss()
            }
        }
        mAdapter = GuideAdapter(activity, functions)
        LogUtil.d(TAG, "TutorialAdapter " + functions.size)
        emptyView.visibility = if (functions.size == 0) View.VISIBLE else View.GONE
        mAdapter?.setItemClickListener { view, position ->
            previewIv.visibility = View.VISIBLE
            tutorialRcv.visibility = View.GONE
            CoverLoader.loadOriginImageView(mContext, functions[position].gifFile.url, previewIv, R.drawable.loading)
        }
        tutorialRcv.adapter = mAdapter;
        tutorialRcv.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(activity)
    }

    companion object {
        private val TAG = "TutorialDialog"
        fun newInstance(): GuideDialog {
            val args = Bundle()
            val fragment = GuideDialog()
            fragment.arguments = args
            return fragment
        }
    }
}
