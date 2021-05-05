package com.mandi.intelimeditor.home.search

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.view.inputmethod.EditorInfo
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.GridLayoutManager.SpanSizeLookup
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.mandi.intelimeditor.common.BaseActivity
import com.mandi.intelimeditor.common.dataAndLogic.AllData
import com.mandi.intelimeditor.common.dataAndLogic.SPUtil
import com.mandi.intelimeditor.common.util.InputMethodUtils
import com.mandi.intelimeditor.common.util.LogUtil
import com.mandi.intelimeditor.common.util.ToastUtils
import com.mandi.intelimeditor.home.data.PicDirInfo
import com.mandi.intelimeditor.home.tietuChoose.PicResourceItemData
import com.mandi.intelimeditor.home.tietuChoose.PicResourcesAdapter
import com.mandi.intelimeditor.home.viewHolder.FolderHolder
import com.mandi.intelimeditor.ptu.tietu.onlineTietu.PicResource
import com.mandi.intelimeditor.home.data.PicDirInfoManager
import com.mandi.intelimeditor.R
import com.mandi.intelimeditor.common.util.FileTool
import io.reactivex.Emitter
import kotlinx.android.synthetic.main.activity_search.*
import kotlinx.android.synthetic.main.toolbar_search_layout.*
import java.util.*

/**
 * 描述:搜索功能
 */
class SearchActivity : BaseActivity() {

    companion object {
        var INTENT_EXTRA_SEARCH_CONTENT: String = AllData.PACKAGE_NAME + ".SEARCH_CONTENT";
        val INTENT_EXTRA_SEARCH_FOLDER_PATH = AllData.PACKAGE_NAME + ".SEARCH_FOLDER_PATH";

        // 注意，是picRes， 不是url
        val INTENT_EXTRA_SEARCH_PIC_RES = AllData.PACKAGE_NAME + ".SEARCH_PIC_PATH";
    }

    /**
     * 搜索信息
     */
    private var queryString: String? = null

    private var mAdapter: PicResourcesAdapter? = null
    private var mHistoryAdapter: SearchHistoryAdapter? = null

    /**
     * 搜索结果
     */
    private var searchHistoryList = mutableListOf<String>()
    private var searchResults = mutableListOf<PicResource>()
    private val searchFolderResults = mutableListOf<PicDirInfo>()

    override fun getLayoutResId(): Int {
        return R.layout.activity_search
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showSearchAnimation()
        initListener()
        updateHistoryPanel(true)
        var searchContent = intent.getStringExtra(INTENT_EXTRA_SEARCH_CONTENT)
        if (!TextUtils.isEmpty(searchContent) && searchContent != null) {
            search(searchContent)
        }
    }

    override fun onBackPressed() {
        setResult(Activity.RESULT_CANCELED, null)
        super.onBackPressed()
    }

    /**
     * 显示搜索弹出动画
     */
    private fun showSearchAnimation() {
        searchEditText.setText(queryString)
        if (TextUtils.isEmpty(queryString) || TextUtils.isEmpty(searchEditText.text)) {
            searchToolbarContainer.translationX = 100f
            searchToolbarContainer.alpha = 0f
            searchToolbarContainer.visibility = View.VISIBLE
            searchToolbarContainer.animate().translationX(0f).alpha(1f).setDuration(200).setInterpolator(DecelerateInterpolator()).start()
        } else {
            searchToolbarContainer.translationX = 0f
            searchToolbarContainer.alpha = 1f
            searchToolbarContainer.visibility = View.VISIBLE
        }

        //显示Loading
        searchPanelView.isEnabled = false
        searchPanelView.isRefreshing = false;
        searchPanelView.setColorSchemeResources(R.color.colorPrimary, R.color.colorAccent, R.color.deepOrange)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_search, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
            R.id.menu_search -> {
                queryString = searchEditText.text.toString().trim { it <= ' ' }
                queryString?.let {
                    search(it);
                }
            }
        }
        return true
    }

    private fun search(query: String) {
        LogUtil.d("Search", "query =$query")
        if (!TextUtils.isEmpty(query)) {
            searchResults.clear()
            searchFolderResults.clear()
            mAdapter?.clear()
            queryString = query
            searchEditText.clearFocus()
            saveSearchHistory(query)
            updateHistoryPanel(false)
            searchPanelView.isRefreshing = true
            searchLocal()
            searchPicRes(query)
        } else {
            ToastUtils.show("请输入搜索关键词")
        }

    }

    private fun searchLocal() {
        val data = PicDirInfoManager.getAllPicDirInfo()
        searchFolderResults.clear()
        InputMethodUtils.showOrHide(this, searchEditText);
        data?.forEach { info ->
            LogUtil.d(TAG, "Pic Info =" + info.dirPath)
            queryString?.let {
                if (FileTool.getFileNameInPath(info.dirPath).toUpperCase(locale = Locale.ROOT).contains(it.toUpperCase(locale = Locale.ROOT))) {
                    LogUtil.d(TAG, "Pic 搜索结果 =" + info.dirPath)
                    searchFolderResults.add(info)
                }
            }
        }
    }

    private fun searchPicRes(queryString: String) {
        var emitter = object : Emitter<List<PicResource>> {
            override fun onNext(resList: List<PicResource>) {
                searchPanelView.isRefreshing = false
                searchResults.addAll(resList)
                updateResultAdapter(searchResults);
            }

            override fun onError(error: Throwable) {
                ToastUtils.show("查询接口异常，请稍后重试！")
                updateResultAdapter(searchResults)
            }

            override fun onComplete() {

            }
        }
        PicResSearchSortUtil.searchPicResByQueryString(queryString, null, emitter);
    }

    private fun showEmptyView() {
        if (searchResults.size == 0 && searchFolderResults.size == 0) {
            emptyTv.visibility = View.VISIBLE
        } else {
            emptyTv.visibility = View.GONE
        }
    }

    /**
     * 更新搜索结果列表
     */
    private fun updateResultAdapter(result: MutableList<PicResource>) {
        updateHistoryPanel(false)
        showEmptyView()
        if (mAdapter == null) {
            mAdapter = PicResourcesAdapter(this, 3)
            mAdapter?.initAdData(true)
            mAdapter?.setClickListener { itemHolder, view ->
                val position = itemHolder.layoutPosition;
                if (position == -1) return@setClickListener;
                val intent = Intent()
                if (itemHolder is FolderHolder) {
                    val data = mAdapter?.imageUrlList
                    data?.let {
                        intent.putExtra(INTENT_EXTRA_SEARCH_FOLDER_PATH, it[position].picDirInfo.dirPath)
                        setResult(Activity.RESULT_OK, intent)
                        finish()
                    }
                } else if (itemHolder is PicResourcesAdapter.ItemHolder) {
                    val data = mAdapter?.imageUrlList
                    data?.let {
                        intent.putExtra(INTENT_EXTRA_SEARCH_PIC_RES, it[position].data)
                        setResult(Activity.RESULT_OK, intent)
                        finish()
                    }
                }
            }
            val gridLayoutManager = GridLayoutManager(this, 3)
            val finalSpanCount = 3
            gridLayoutManager.spanSizeLookup = object : SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    return if (position <= mAdapter?.itemCount ?: 0 && (
                                    mAdapter?.getItemViewType(position) == PicResourceItemData.PicListItemType.FEED_AD ||
                                            mAdapter?.getItemViewType(position) == PicResourceItemData.PicListItemType.ITEM_FOLDER
                                    )) {
                        finalSpanCount
                    } else 1
                }
            }
            mAdapter?.setImageUrls(result, searchFolderResults)
            resultListRcv.adapter = mAdapter
            resultListRcv.layoutManager = gridLayoutManager
        } else {
            mAdapter?.setImageUrls(result, searchFolderResults)
        }
    }

    private fun toPtu(intent: Intent, picRes: PicResource) {
        intent.putExtra(INTENT_EXTRA_SEARCH_FOLDER_PATH, picRes)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    private fun initListener() {
        clearAllIv.setOnClickListener {
            SPUtil.putSearchHistory("")
            mHistoryAdapter?.setList(mutableListOf())
        }
        clearSearchIv.setOnClickListener {
            searchEditText.setText("")
            clearSearchIv.visibility = View.GONE
            updateHistoryPanel(true)
        }
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable) {
                queryString = searchEditText.text.toString()
                clearSearchIv.visibility = View.VISIBLE
                if (TextUtils.isEmpty(queryString)) {
                    clearSearchIv.visibility = View.GONE
                }
            }
        })

        searchEditText.setOnEditorActionListener { _, _, event ->
            if (event != null && (event.keyCode == KeyEvent.KEYCODE_ENTER || event.action == EditorInfo.IME_ACTION_SEARCH)) {
                queryString?.let { search(it) }
                return@setOnEditorActionListener true
            }
            false
        }
    }


    /**
     * 获取搜索记录
     */
    private fun getSearchHistory(): MutableList<String> {
        val longHistory: String = SPUtil.getAllSearchHistory()
        LogUtil.d(TAG, "longHistory = $longHistory")
        val historyList = longHistory.split(",").toMutableList() //split后长度为1有一个空串对象
        if (historyList.size == 1 && historyList[0] == "") { //如果没有搜索记录，split之后第0位是个空串的情况下
            historyList.clear() //清空集合，这个很关键
        }
        return historyList
    }


    override fun finish() {
        super.finish()
        overridePendingTransition(0, 0);
    }


    /**
     * 保存历史搜索记录
     */
    private fun saveSearchHistory(query: String) {
        searchHistoryList = getSearchHistory()
        for (history in searchHistoryList) {
            if (history == query) {
                searchHistoryList.remove(history)
                break
            }
        }
        searchHistoryList.add(0, query) //将新输入的文字添加集合的第0位也就是最前面(2.倒序)
        if (searchHistoryList.size > 20) {
            searchHistoryList.removeAt(searchHistoryList.size - 1) //3.最多保存8条搜索记录 删除最早搜索的那一项
        }
        //逗号拼接
        val sb = StringBuilder()
        for (i in 0 until searchHistoryList.size) {
            if (searchHistoryList.size - 1 == i) {
                sb.append(searchHistoryList[i])
            } else {
                sb.append(searchHistoryList[i] + ",")
            }
        }
        LogUtil.d(TAG, "saveSearchHistory = $sb")
        mHistoryAdapter?.setList(searchHistoryList)
        SPUtil.putSearchHistory(sb.toString())
    }

    /**
     * 更新搜索历史列表
     */
    private fun updateHistoryPanel(isShow: Boolean) {
        if (isShow) {
            historyPanel.visibility = View.VISIBLE
            searchPanelView.visibility = View.GONE
            searchHistoryList = getSearchHistory()
            if (mHistoryAdapter == null) {
                mHistoryAdapter = SearchHistoryAdapter(searchHistoryList)
                mHistoryAdapter?.setOnItemClickListener { _, _, position ->
                    searchEditText.setText(searchHistoryList[position])
                    search(searchHistoryList[position])
                }
                val layoutManager = FlexboxLayoutManager(this)
                layoutManager.flexDirection = FlexDirection.ROW
                layoutManager.justifyContent = JustifyContent.FLEX_START
                historyRcv.layoutManager = layoutManager
                historyRcv.adapter = mHistoryAdapter
            } else {
                mHistoryAdapter?.setDiffNewData(searchHistoryList)
            }
        } else {
            historyPanel.visibility = View.GONE
            searchPanelView.visibility = View.VISIBLE
        }

    }
}
