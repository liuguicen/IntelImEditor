package com.mandi.intelimeditor.ptu.text

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
import com.mandi.intelimeditor.common.util.SimpleObserver
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.ObservableOnSubscribe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.io.File
import java.io.FileOutputStream
import java.net.URL


/**
 * Created by Administrator on 2016/11/21 0021.
 */

object TypefaceDownloader {


    /**
     * 下载字体，下载完成后更新一些数据
     *
     * @param context      要显示对话框，必须传入Activity的context
     * @param name         一下几种之一 kaiti.TTF
     * @param typefaceList 要更新的列表
     * @param textView     要更新的view
     */
    fun downloadTypefaceFile(
        context: Context,
        ptuTypeface: PtuTypeface,
        success: ((String) -> Unit)? = null
    ) {
        checkNetworkStatus(
            context,
            ptuTypeface.size
        ) {
            toDownloadTypefaceFile(
                context,
                ptuTypeface,
                success
            )
        }
    }

    private fun toDownloadTypefaceFile(
        context: Context,
        ptuTypeface: PtuTypeface,
        success: ((String) -> Unit)? = null
    ) {
        val name = ptuTypeface.nameInFile // 只有名称可能重复
        val bmobfile = ptuTypeface.typeface
        val savePath = AllData.zitiDir + name
        ToastUtils.show("开始下载" + name + "字体，请稍后！")
        Observable
            .create(ObservableOnSubscribe { emitter: ObservableEmitter<String> ->
                val downloadException =
                    downloadFromUrl(
                        bmobfile.url,
                        savePath
                    )
                if (downloadException != null) {
                    emitter.onError(downloadException)
                } else {
                    emitter.onNext("")
                }
            } as ObservableOnSubscribe<String>)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : SimpleObserver<String>() {
                override fun onNext(t: String) {
                    try {
                        success?.invoke(savePath)
                        Toast.makeText(
                            context,
                            ptuTypeface.name + "下载成功了,点击即可使用！",
                            Toast.LENGTH_LONG
                        ).show()
                    } catch (ne: Exception) {
                        ToastUtils.show(R.string.fail_to_download_font)
                    }
                }

                override fun onError(e: Throwable) {
                    super.onError(e)
                    ToastUtils.show("下载字体失败：" + e.message, Toast.LENGTH_LONG);
                }
            })

//        原版本，使用bmob 3.8.4 sdk出错的
//        bmobfile.download(saveFile, object : DownloadFileListener() {
//            override fun done(s: String, e: BmobException?) {
//                if (e == null) {
//        var dir = File(AllData.zitiDir)
//        if (!dir.isDirectory)
//            if (!dir.mkdir()) {
//                ToastUtils.show(R.string.fail_to_make_dir)
//                return
//            }
//                    val typefaceFile = File(savePath)
//                    if (!typefaceFile.exists()) {
//                        Toast.makeText(context, "下载失败,文件无法保存：", Toast.LENGTH_LONG).show()
//                    }
//                    try {
//                        success?.invoke(savePath)
//                        Toast.makeText(context, ptuTypeface.name + "下载成功了,点击即可使用！", Toast.LENGTH_LONG).show()
//                    } catch (ne: Exception) {
//                        ToastUtils.show(R.string.fail_to_download_font)
//                    }
//
//                } else {
//                    Toast.makeText(context, "下载失败：" + e.errorCode + "," + e.message, Toast.LENGTH_LONG).show()
//                    if (saveFile.exists()) saveFile.delete()//删除没下完整的文件
//                }
//            }
//
//            override fun onProgress(integer: Int?, l: Long) {
//
//            }
//        })
    }

    fun downloadFromUrl(link: String, path: String): java.lang.Exception? {
        var saveFile = File(path)
        try {
            var dir = File(AllData.zitiDir)
            if (!dir.isDirectory) {
                dir.delete()
                if (!dir.mkdir()) {
                    return java.lang.Exception("创建文件夹失败")
                }
            }
            URL(link).openStream().use { input ->
                FileOutputStream(saveFile).use { output ->
                    input.copyTo(output)
                }
            }
            return null
        } catch (e: java.lang.Exception) {
            if (saveFile.exists())
                saveFile.delete()//删除没下完整的文件
            return e
        }
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
