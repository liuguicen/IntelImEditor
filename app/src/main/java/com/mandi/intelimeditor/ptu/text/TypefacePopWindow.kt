package text

import android.content.Context
import android.graphics.Typeface
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ProgressBar
import cn.bmob.v3.BmobQuery
import cn.bmob.v3.exception.BmobException
import cn.bmob.v3.listener.FindListener
import com.mandi.intelimeditor.ad.AdData
import com.mandi.intelimeditor.bean.PtuTypeface
import com.mandi.intelimeditor.common.dataAndLogic.AllData
import com.mandi.intelimeditor.common.dataAndLogic.SPUtil
import com.mandi.intelimeditor.common.util.LogUtil
import com.mandi.intelimeditor.user.US
import com.mandi.intelimeditor.dialog.UnlockDialog
import com.mandi.intelimeditor.common.util.ToastUtils
import com.mandi.intelimeditor.network.NetWorkState
import com.mandi.intelimeditor.ptu.text.FloatTextView
import com.mandi.intelimeditor.ptu.text.TextPopUpBuilder
import com.mandi.intelimeditor.ptu.text.TypefaceDownloader
import com.mandi.intelimeditor.R
import java.io.File
import java.util.concurrent.TimeUnit


/**
 * Created by LiuGuicen on 2017/1/20 0020.
 * 注意使用弱引用的方式持有，外部都是弱引用，不要在内部被反向向持有了
 * 注意这里contentView的监听器是持有TypefacePopWindow的，contentView被window持有，
 *
 *
 *
 * window消失之后监听器当做强引用方式回收，这时相当于TypefacePopWindow没被引用了那样回收
 * 既FunctionPopWindowBuilder还在，TypefacePopWindow相当于不存在了
 */

class TypefacePopWindow(val acContext: Context, val floatTextView: FloatTextView, val upBuilderText: TextPopUpBuilder) {

    private var typefaceList = mutableListOf<TypefaceItemBean?>()
    private var typeAdapter: TextTypeAdapter? = null
    private val TAG = "TypefacePop"

    /**
     * 新建字体选择view
     *
     * @return
     */
    fun createTypefacePopWindow(): View {
        val contentView = LayoutInflater.from(acContext).inflate(R.layout.popwindow_text_typeface, null)
        val recyclerView = contentView.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rcv_text_type)
        val loadingView = contentView.findViewById<ProgressBar>(R.id.typeface_pb_loading)

        //从bmob上加载字体列表
        loadAllTypeface(success = {
            loadingView.visibility = View.GONE
            initTypeList(it)
            initAdapter(recyclerView, it)
        })
        return contentView
    }

    // todo 此处需要优化，问题有点严重，字体直接全部加载进来的，有的字体文件几十M，内存会出问题的
    // 使用图片的方式展示字体
    /**
     * 初始化字体列表
     */
    private fun initTypeList(it: MutableList<PtuTypeface>) {
        typefaceList.clear()
        var id = 0
        it.forEach {
            val path = AllData.zitiDir + it.nameInFile

            val hasUnlocked: Boolean = when {
                AdData.judgeAdClose(AdData.TT_AD_ID) -> true
                id == 0 -> true
                else -> SPUtil.getTypefaceUnlock(id.toString())
            }
            id++
            try {
                val typeface = Typeface.createFromFile(path)
                typefaceList.add(TypefaceItemBean(typeface, hasUnlocked))
            } catch (e: Throwable) {
                //如果是损坏的文件，删除它
                val file = File(path)
                if (file.exists())
                    file.delete()
                //为空设置null,等待下载添加
                typefaceList.add(TypefaceItemBean(null, hasUnlocked))
            }
        }
    }

    /**
     * 字体选择列表
     */
    private fun initAdapter(recyclerView: androidx.recyclerview.widget.RecyclerView, it: MutableList<PtuTypeface>) {
        typeAdapter = TextTypeAdapter(acContext, it)
        typeAdapter?.setTypeFaceList(typefaceList)
        typeAdapter?.setOnClickListener(object : TextTypeAdapter.ItemClickListener {
            override fun onItemClick(view: View, position: Int) {
                //position 默认添加了1,所以在实际的typefaceList 集合中需要减一
                if (position == 0) {
                    //点击默认
                    onChosen(null, 0)
                } else {
                    val typefaceItem = typefaceList[position - 1]
                    if (typefaceItem?.typeface == null) {
                        //如果字体为空，则弹出下载弹窗，下载成功切换字体。否则不切换
                        try {
                            TypefaceDownloader.downloadTypefaceFile(acContext, it[position - 1]) { path ->
                                LogUtil.d(TAG, "下载成功 :$path ,更新ui")
                                val newTypeface = Typeface.createFromFile(path)
                                typefaceItem?.typeface = newTypeface
                                //更新字体
//                                floatTextView.typeface = typefaceItem?.typeface
//                                floatTextView.updateSize()
//                                typeAdapter?.selectId = position
//                                typeAdapter?.notifyDataSetChanged()
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    } else if (!typefaceItem.hasUnlocked) { // 没有解锁, 显示视频广告
                        US.putTypefaceRewardAd(US.CLICK);
                        val videoAdDialog = UnlockDialog.newInstance()
                        videoAdDialog.setTitle(acContext.getString(R.string.unlock_title_resource))
                        videoAdDialog.setAdPositionName(AdData.TYPEFACE_REWARD_AD)
                        videoAdDialog.unlockListener = { isReward ->
                            if (isReward) {
                                LogUtil.d(TAG, "video isReward \$isReward")
                                SPUtil.putTypefaceUnlock((position - 1).toString(), true)
                                SPUtil.addAndPutAdSpaceExposeNumber(AdData.AdSpaceName.REWARD_VAD) // 设置广告源策略需要的
                                typefaceItem.hasUnlocked = true
                                typeAdapter!!.notifyItemChanged(position)
                                onChosen(typefaceItem.typeface, position)
                            }
                        }
                        if (acContext is androidx.fragment.app.FragmentActivity) {
                            videoAdDialog.showIt(acContext)
                        }
                        return
                    } else {
                        onChosen(typefaceItem.typeface, position)
                    }
                }
                //刷新列表
                typeAdapter?.notifyDataSetChanged()
            }

            private fun onChosen(typeface: Typeface?, position: Int) {
                floatTextView.typeface = typeface
                upBuilderText.curTypeface = typeface
                floatTextView.updateSize()
                typeAdapter?.selectId = position
            }

        })
        recyclerView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(acContext, androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL, false)
        recyclerView.adapter = typeAdapter
    }


    /**
     * 加载在线所有的字体
     */
    private fun loadAllTypeface(success: ((MutableList<PtuTypeface>) -> Unit)) {
        Log.d(TAG, "getAllTypeface")
        try {
            val query = BmobQuery<PtuTypeface>()
            if (query.hasCachedResult(PtuTypeface::class.java)) {
                query.cachePolicy = BmobQuery.CachePolicy.CACHE_ONLY
            } else {
                query.cachePolicy = BmobQuery.CachePolicy.NETWORK_ONLY
                query.maxCacheAge = TimeUnit.DAYS.toMillis(1) * 7
                // 不跳过，查一遍，没什么影响，防止这个检测网络方法出问题
                if (NetWorkState.detectNetworkType() == NetWorkState.NO_NET) {
                    ToastUtils.show(R.string.no_net_to_download_typeface)
                }
            }
            query.findObjects(object : FindListener<PtuTypeface>() {
                override fun done(list: MutableList<PtuTypeface>?, e: BmobException?) {
                    val data = mutableListOf<PtuTypeface>()
                    list?.let {
                        data.addAll(it)
                    }
                    success.invoke(data)
                }
            })
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }
}
