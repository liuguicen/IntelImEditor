package com.mandi.intelimeditor.network

import android.app.AlertDialog
import android.content.Context
import android.widget.Toast
import cn.bmob.v3.exception.BmobException
import cn.bmob.v3.listener.DownloadFileListener
import com.mandi.intelimeditor.bean.PtuTypeface
import com.mandi.intelimeditor.common.dataAndLogic.AllData
import com.mandi.intelimeditor.common.util.ToastUtils
import com.mandi.intelimeditor.network.NetWorkState
import com.mandi.intelimeditor.R
import java.io.File


/**
 * Created by Administrator on 2016/11/21 0021.
 */

object TypefaceDownloader {

    private val fontDir = AllData.zitiDir
    internal val typefaceLoacalPath = arrayOf("default", fontDir + "kaiti.ttf", fontDir + "banner.ttf", fontDir + "xingshu.ttf", fontDir + "xiaowanzi.ttf")

    /**
     * 下载字体，下载完成后更新一些数据
     *
     * @param context      要显示对话框，必须传入Activity的context
     * @param name         一下几种之一 kaiti.TTF
     * @param typefaceList 要更新的列表
     * @param textView     要更新的view
     */
    fun downloadTypefaceFile(context: Context, ptuTypeface: PtuTypeface, success: ((String) -> Unit)? = null) {
        checkNetworkStatus(context, ptuTypeface.size) {
            toDownloadTypefaceFile(context, ptuTypeface, success)
        }
    }

    private fun toDownloadTypefaceFile(context: Context, ptuTypeface: PtuTypeface, success: ((String) -> Unit)? = null) {
        val name = ptuTypeface.objectId
        val bmobfile = ptuTypeface.typeface
        val savePath = AllData.zitiDir + name
        val saveFile = File(savePath)
        ToastUtils.show("开始下载" + name + "字体，请稍后！")
        bmobfile.download(saveFile, object : DownloadFileListener() {
            override fun done(s: String, e: BmobException?) {
                if (e == null) {
                    val typefaceFile = File(savePath)
                    if (!typefaceFile.exists()) {
                        Toast.makeText(context, "下载失败,文件无法保存：", Toast.LENGTH_LONG).show()
                    }
                    try {
                        success?.invoke(savePath)
                        Toast.makeText(context, ptuTypeface.name + "下载成功了,点击即可使用！", Toast.LENGTH_LONG).show()
                    } catch (ne: Exception) {
                        ToastUtils.show(R.string.fail_to_download_font)
                    }

                } else {
                    Toast.makeText(context, "下载失败：" + e.errorCode + "," + e.message, Toast.LENGTH_LONG).show()
                    if (saveFile.exists()) saveFile.delete()//删除没下完整的文件
                }
            }

            override fun onProgress(integer: Int?, l: Long) {

            }
        })
    }

    /**
     * 检查网络状态
     */
    private fun checkNetworkStatus(context: Context, size: String, success: (() -> Unit)? = null) {
        val state = NetWorkState.detectNetworkType(context)
        val msg: String
        if (state == -1) {//没有网络
            ToastUtils.show("网络未连接，不能下载字体，请稍后再试！")
            return
        }
        msg = if (state == 1) {
            //是wifi
            "已连接到WiFi，字体包大小:" + size + "MB," + "确认下载吗？"
        } else {
            "WiFi未连接，字体包大小：" + size + "MB," + "会产生流量消耗，确定下载吗?"
        }
        val builder = AlertDialog.Builder(context)
        builder.setTitle("字体下载")
                .setMessage(msg)
                .setNegativeButton("下载") { dialog, _ ->
                    success?.invoke()
                    dialog.dismiss()
                }
                .setPositiveButton(R.string.cancel, null)
                .create()
                .show()
    }

}
