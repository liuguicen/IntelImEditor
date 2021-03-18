package common

import android.graphics.Bitmap
import android.view.View
import com.chad.library.adapter.base.BaseQuickAdapter
import com.mandi.intelimeditor.bean.GifPlayFrameEvent
import com.mandi.intelimeditor.common.util.LogUtil
import com.mandi.intelimeditor.ptu.BasePtuFragment
import com.mandi.intelimeditor.ptu.PTuActivityInterface
import com.mandi.intelimeditor.ptu.PtuUtil
import com.mandi.intelimeditor.ptu.repealRedo.StepData
import com.mandi.intelimeditor.user.US
import com.mathandintell.intelimedit.bean.FunctionInfoBean
import com.mathandintell.intelimeditor.R
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * Created by Administrator on 2016/4/29.
 */
class MainFunctionFragment : BasePtuFragment() {
    private var pTuActivityInterface: PTuActivityInterface? = null

    fun getResultBm(ratio: Float): Bitmap? {
        return null
    }

    override fun getResultDataAndDraw(ratio: Float): StepData? {
        return null
    }

    override fun releaseResource() {

    }

    override fun getLayoutResId(): Int {
        return R.layout.fragment_bottom_function;
    }

    override fun getEditMode(): Int {
        return PtuUtil.EDIT_MAIN
    }

    override fun setPTuActivityInterface(ptuActivity: PTuActivityInterface?) {
        this.pTuActivityInterface = ptuActivity
    }

    override fun onSure(): Boolean {
        return false
    }

    /**
     * 底部功能点击
     */
    override fun onItemClick(adapter: BaseQuickAdapter<*, *>, view: View, position: Int) {
        super.onItemClick(adapter, view, position)
        LogUtil.d(TAG, "mFunctions[position] =${pFunctionList[position].toString()}")
        var key = PtuUtil.getUSEventByType(pFunctionList[position].type);
        if (pTuActivityInterface?.gifManager != null) {
            key += US.MAIN_FUNCTION_GIF_SUFFIX
        }
        US.putMainFunctionEvent(key)
        pTuActivityInterface?.switchFragment(pFunctionList[position].type, null)
    }

    /**
     * 获取底部功能列表
     */
    override fun getFunctionList(): MutableList<FunctionInfoBean> {
        val isGif = pTuActivityInterface?.gifManager != null
        switchFunctionFrag(isGif = isGif)
        return pFunctionList;
    }

    /**
     * 底部操作列表
     * @param isGif 是否制作GIF
     * @param refresh 是否主动刷新，创建时会刷新列表，当需要切换时需要传入refresh
     */
    fun switchFunctionFrag(isGif: Boolean = false, refresh: Boolean = false) {
        pFunctionList.clear()
        if (!isGif) {
            pFunctionList.add(FunctionInfoBean(R.string.rend_pic, R.mipmap.rend_pic, R.drawable.function_background_text_yellow, PtuUtil.EDIT_REND))
        } else {
            pFunctionList.add(FunctionInfoBean(R.string.gif_en, R.drawable.ic_gif, R.drawable.function_background_text_yellow, PtuUtil.EDIT_GIF))
        }
        pFunctionList.add(FunctionInfoBean(R.string.text, R.mipmap.text, R.drawable.function_background_text_yellow, PtuUtil.EDIT_TEXT))
        pFunctionList.add(FunctionInfoBean(R.string.tietu, R.mipmap.tietu, R.drawable.function_background_tietu_green, PtuUtil.EDIT_TIETU))
        pFunctionList.add(FunctionInfoBean(R.string.dig_face, R.mipmap.dig_face, R.drawable.function_background__dig_sliver, PtuUtil.EDIT_DIG))

        if (!isGif) {
            pFunctionList.add(FunctionInfoBean(R.string.deformation, R.drawable.icon_deformation,
                    R.drawable.function_background_text_yellow, PtuUtil.EDIT_DEFORMATION));
        }

        pFunctionList.add(FunctionInfoBean(R.string.draw, R.mipmap.draw, R.drawable.function_background_draw_pink, PtuUtil.EDIT_DRAW))

        pFunctionList.add(FunctionInfoBean(R.string.edit, R.mipmap.edit, R.drawable.function_background_cut_blue, PtuUtil.EDIT_CUT))
        if (pFunctionAdapter != null) {
            pFunctionAdapter.notifyDataSetChanged()
        }
    }

    override fun initView() {
        EventBus.getDefault().register(this)
//        if (AllData.hasReadConfig._mainFunctionInBack_readCount < 2) {
//            scroll_back_function.visibility = View.VISIBLE
//            scroll_back_function.setOnClickListener(View.OnClickListener {
//                AllData.hasReadConfig.put_mainFunctionInBack_readCount(AllData.hasReadConfig._mainFunctionInBack_readCount + 1)
//                pFunctionRcv.smoothScrollToPosition(pFunctionList.size - 1)
//                scroll_back_function.visibility = View.GONE
//            })
//        }
        super.initView()
    }

    override fun onDestroyView() {
        EventBus.getDefault().unregister(this)
        super.onDestroyView()
    }

    /**
     * @param frameEvent 传入gif当前播放的帧
     */
    @Subscribe(threadMode = ThreadMode.MAIN, priority = 10)
    fun onPlayGifFrameEvent(frameEvent: GifPlayFrameEvent) {
        // nothing 消耗eventbus的事件
    }
}