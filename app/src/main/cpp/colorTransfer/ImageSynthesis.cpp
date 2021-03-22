//
// Created by Administrator on 2017/1/11 0011.
//
#include<string>

#include <jni.h>

#include <android/log.h>
#include<android/bitmap.h>
#include <opencv2/imgproc.hpp>
#include <opencv2/photo.hpp>

#include "MyUtil.h"
#include "ColorTransfer.h"
#include "newColorTransfer.h"
#include "edgeBlur.h"

/*Mat synthesisInSeamlessClone(JNIEnv *env, Mat mat, Mat above, jintArray pArray);*/

jintArray returAbove();


using namespace std;
using namespace cv;

JNIEXPORT inline bool transformData(JNIEnv *env, jobject jbitmap,
                                    AndroidBitmapInfo *info,
                                    void *&pixes) {
    LOGE("开始获取bitmap的信息");
    int ret;
//解析bitmap 0xffa1f438
    if ((ret = AndroidBitmap_getInfo(env, jbitmap, info)) < 0) {
        LOGE("AndroidBitmap_getInfo() failed !first error=%d", ret);
        return false;
    }
    if (info->format != ANDROID_BITMAP_FORMAT_RGBA_8888) {
        LOGE("Bitmap format is not RGBA_8888!");
        return false;
    }
//即使有数据也可能发生异常
    if ((ret = AndroidBitmap_lockPixels(env, jbitmap, &pixes)) < 0) {
        LOGE("First Bitmap LockPixels Failed return=%d!", ret);
        return false;
    }
    return true;
}


/*
 * 效果特别好的泊松克隆，不过时间消耗太大。
 * 有相应的实时克隆算法，不过只有论文没有代码
 */

Mat synthesisInSeamlessClone(JNIEnv *env, Mat Sunder, Mat Sabove, jintArray in_rect) {
    Mat  under(Sunder.rows, Sunder.cols, CV_8UC3, Scalar(0, 0, 255));
    Mat  above(Sabove.rows, Sabove.cols, CV_8UC3, Scalar(0, 0, 255));
    cvtColor(Sunder, under, CV_BGRA2BGR, 3);
    cvtColor(Sabove, above, CV_BGRA2BGR, 3);
    LOGE("泊松克隆，开始！");
    Mat mask = 255 * Mat::ones(above.rows, above.cols, above.depth());
    LOGE("创建区域矩形，完成");
    jboolean isCopy = (jboolean) false;
    int *p = env->GetIntArrayElements(in_rect, &isCopy);
    int x = (p[0] + p[2]) / 2;
    int y = (p[1] + p[3]) / 2;
    env->ReleaseIntArrayElements(in_rect, p, JNI_ABORT);
    Point location(50, 50);
    LOGE("中心点获取成功");
    Mat normalClone;
    logTime();
    seamlessClone(above, under, mask, location, normalClone, NORMAL_CLONE);
    LOGE("泊松克隆合成图片完成");
    logTime();
    cvtColor(normalClone, normalClone, COLOR_RGB2BGRA, 4);
    cvtColor(above, above, CV_RGB2BGRA);
    return under;
}

extern "C"
JNIEXPORT jintArray JNICALL
Java_com_mandi_intelimeditor_tietu_tietuImpact_PictureSynthesis_synthesisBm(JNIEnv *env,
                                                                         jobject instance,
                                                                         jobject under,
                                                                         jobject above,
                                                                         jintArray in_rect) {
    LOGE("进入了jni");

    AndroidBitmapInfo underInfo;
    AndroidBitmapInfo aboveInfo;
    void *underPixes, *abovePixes;//
    bool state = transformData(env, under, &underInfo, underPixes);

    state = state && transformData(env, above, &aboveInfo, abovePixes);
    if (!state) {
        LOGE("获取图片bitmap失败");
        return NULL;
    }
   /* LOGE("JNI PictureSynthesis：  under获取到的宽%d", underInfo.width);
    LOGE("JNI PictureSynthesis：  under获取到的高%d", underInfo.height);
    LOGE("JNI PictureSynthesis：  above获取到的宽%d", aboveInfo.width);
    LOGE("JNI PictureSynthesis：  above获取到的高%d", aboveInfo.height);*/

    Mat m_under(underInfo.height, underInfo.width, CV_8UC4, underPixes);
    Mat m_above(aboveInfo.height, aboveInfo.width, CV_8UC4, abovePixes);

    LOGE("Java层传入的图转换为Mat成功");
    Mat mat_result = synthesisInSeamlessClone(env, m_under, m_above, in_rect);
    if (!mat_result.data) {
        LOGE("颜色转换输出结果错误");
        return NULL;
    }


    AndroidBitmap_unlockPixels(env, under);
    AndroidBitmap_unlockPixels(env, above);
    int size = mat_result.rows * mat_result.cols;
    jintArray result = env->NewIntArray(size);
    env->SetIntArrayRegion(result, 0, size, (int *) mat_result.data);
    LOGE("Mat结果转换成输出数组成功");
    mat_result.release();

    return result;
}