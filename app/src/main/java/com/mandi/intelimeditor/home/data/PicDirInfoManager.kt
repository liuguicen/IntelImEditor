package com.mathandintell.intelimedit.home.data

import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import com.mandi.intelimeditor.common.util.FileTool
import com.mandi.intelimeditor.common.util.Util
import com.mandi.intelimeditor.home.data.MediaInfoScanner
import com.mandi.intelimeditor.home.data.PicDirInfo
import com.mathandintell.intelimeditor.R
import java.io.File
import java.util.*

/**
 * Created by LiuGuicen on 2017/1/18 0018.
 */
object PicDirInfoManager {
    /**
     * 文件的信息
     */
    private val picDirInfos: MutableList<PicDirInfo>

    init {
        picDirInfos = ArrayList()
        picDirInfos.add(PicDirInfo("ddd", formatDescribeInfo("0", 0), "sdf"))
    }

    fun updateUsuInfo(usuPaths: List<String?>) {
        // 处理文件信息,将要显示的文件信息获取出来
        //常用图的信息，
        val representPath = if (usuPaths.isEmpty()) null else usuPaths[1]
        val picDirInfo = PicDirInfo("aaaaa",
                formatDescribeInfo("所有图片", usuPaths.size - 3),
                representPath)
        picDirInfos[0] = picDirInfo
    }

    /**
     * 短视频文件夹，显示所有的短视频
     */
    fun updateShortVideoInfo(shortVideoSet: Set<String?>) {
        if (shortVideoSet.isNotEmpty()) {
            val representPath = shortVideoSet.iterator().next()
            val picDirInfo = PicDirInfo(MediaInfoScanner.SHORT_VIDEO_TAG,
                    formatDescribeInfo("短视频(制作GIF）", shortVideoSet.size, "条"),
                    representPath)
            if (picDirInfos.size >= 2) {
                picDirInfos[1] = picDirInfo
            } else {
                picDirInfos.add(picDirInfo)
            }
        } else { // 没有短视频，移除
            if (picDirInfos.size >= 2 && MediaInfoScanner.SHORT_VIDEO_TAG == picDirInfos[1].dirPath) {
                picDirInfos.removeAt(1)
            }
        }
    }
    /**
     * 多处使用，便于统一格式，免得更改时到处改
     *
     * @param name   文件名称
     * @param number 文件数量
     */
    /**
     * 多处使用，便于统一格式，免得更改时到处改
     *
     * @param name   文件名称
     * @param number 文件数量
     */
    private fun formatDescribeInfo(name: String, number: Int, unit: String? = null): SpannableString {
        var unit = unit
        if (unit == null) {
            unit = " 张"
        }
        val infos = " $name\n $number$unit"
        val sps = SpannableString(infos)
        val s = infos.indexOf("\n") + 1
        val t = infos.length
        sps.setSpan(RelativeSizeSpan(0.8f), s, t, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        sps.setSpan(ForegroundColorSpan(Util.getColor(R.color.text_light_black)), s, t, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        return sps
    }

    /**
     * 更新图片文件的信息，在drawer中的，包括文件目录信息，文件中图片数目，最新图片的路径
     *
     * @param picFileNumberMap    文件中图片张数信息
     * @param PicFileRepresentMap 文件代表图片信息
     */
    fun updateAllFileInfo(picFileNumberMap: Map<String, Int>, PicFileRepresentMap: Map<String?, String?>) {
        for ((path, value) in picFileNumberMap) {
            val representPath = PicFileRepresentMap[path]
            val name = path.substring(path.lastIndexOf("/") + 1, path.length)
            val numInfo = formatDescribeInfo(name, value)
            picDirInfos.add(PicDirInfo(path, numInfo, representPath))
        }
    }

    fun clear() {
        picDirInfos.clear()
        picDirInfos.add(PicDirInfo("asdas", formatDescribeInfo("0", 0), "sdfsd"))
    }

    fun getDirPath(pos: Int): String {
        var position = pos
        if (position >= picDirInfos.size) {
            position = picDirInfos.size - 1
        } else if (position < 0) {
            position = 0
        }
        return picDirInfos[position].dirPath
    }

    /**
     * 根据目录路径找到其的位置
     *
     * @param dirPath 目录路径
     */
    private fun findDirPathId(dirPath: String): Int {
        for (i in picDirInfos.indices) {
            if (picDirInfos[i].dirPath == dirPath) return i
        }
        return -1
    }

    /**
     * 新增一张图片时改变相应的目录信息
     *
     * @return 针对添加图片时，图片目录尚不存在的情况，刷新图片列表
     */
    fun onAddNewPic(newPicPath: String): Boolean {
        val parentPath = FileTool.getParentPath(newPicPath)
        val id = findDirPathId(parentPath)
        if (id == -1) { //如果没找到,尚未加入此目录，需要先添加
            addOneDirInfo(newPicPath, parentPath)
            return true
        }
        val info = picDirInfos[id].picNumInfo.toString()
        var number = Integer.valueOf(info.substring(info.indexOf('\n') + 2, Util.lastDigit(info) + 1))
        number++
        val new_info = formatDescribeInfo(info.substring(0, info.indexOf('\n')), number)
        picDirInfos[id] = PicDirInfo(parentPath, new_info, newPicPath)
        return false
    }

    private fun addOneDirInfo(picPath: String, parentPath: String) {
        picDirInfos.add(PicDirInfo(parentPath, formatDescribeInfo(FileTool.getFileNameInPath(parentPath), 1), picPath))
    }

    /**
     * 删除图片文件，并更新目录列表信息
     *
     * 更新文件信息，文件是否还存在，图片张数，最新图片，描述信息的字符串
     *
     * 注意发送删除通知
     *
     * @return 删除成功的列表
     */
    fun deletePicList(pathList: List<String>): List<String> {
        val successList: MutableList<String> = ArrayList()
        if (pathList.size < 1) return successList
        val dirPath = File(pathList[0]).parent // 目前只删除同一个目录下的
        for (picPath in pathList) {
            if (FileTool.deletePicFile(picPath)) {
                successList.add(picPath)
            }
        }

        //更新文件目录信息 // 低效方法，有时间改进
        val id = findDirPathId(dirPath) //图片所在目录的位置id
        if (id != -1) {
            val paths: List<String> = ArrayList()
            FileTool.getOrderedPicListInFile(dirPath, paths)
            if (paths.isEmpty()) {
                picDirInfos.removeAt(id) //如果此目录下面已经没有图片
            } else { //还有图片则更新信息
                val representPath = paths[0]
                val name = dirPath.substring(dirPath.lastIndexOf("/") + 1)
                val info = formatDescribeInfo(name, paths.size)
                picDirInfos[id] = PicDirInfo(dirPath, info, representPath)
            }
        }
        return successList
    }

    fun getAllPicDirInfo(): List<PicDirInfo>? {
        return picDirInfos
    }
}