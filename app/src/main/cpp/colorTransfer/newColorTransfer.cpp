//
// Created by Administrator on 2017/1/13 0013.
//

#include "newColorTransfer.h"
#include "MyUtil.h"
#include <opencv2/imgproc.hpp>

using namespace cv;

#define max_uchar(a, b)    (((a) > (b)) ? (a) : (b))
#define min_uchar(a, b)    (((a) < (b)) ? (a) : (b))

Mat toInterceptRect(JNIEnv *env, Mat under, jintArray in_rect) ;

// 颜色转换
void NewColorTransfer::colorTransfer(Mat &src, Mat &tar, Mat &dst) {
    Mat srcLab, tarLab;
    Vec3f srcMean3f, tarMean3f;// 源/目标图像均值
    Vec3f srcVariance3f, tarVariance3f;// 源/目标图像标准差
    Vec3f ratioVariance3f;// 标准差比例
    // BGR空间转Lab空间
    cvtColor(src, srcLab, COLOR_BGR2Lab);
    cvtColor(tar, tarLab, COLOR_BGR2Lab);
    // 计算当前图像与目标图像均值及标准差
    compMeanAndVariance(srcLab, srcMean3f, srcVariance3f);
    compMeanAndVariance(tarLab, tarMean3f, tarVariance3f);

    // 标准差比
    ratioVariance3f[0] = tarVariance3f[0] / srcVariance3f[0];
    ratioVariance3f[1] = tarVariance3f[1] / srcVariance3f[1];
    ratioVariance3f[2] = tarVariance3f[2] / srcVariance3f[2];

    // 计算颜色转换值
    int row = srcLab.rows;
    int col = srcLab.cols;
    uchar *pImg = srcLab.data;//指针的运算也是当做三通道来处理的，
    for (int i = 0; i < row; i++) {
        for (int j = 0; j < col; j++) {
            pImg[3 * j + 0] = (uchar) min_uchar(255, max_uchar(0, ratioVariance3f[0] *
                                                                  (pImg[3 * j + 0] - srcMean3f[0]) +
                                                                  tarMean3f[0]));
            pImg[3 * j + 1] = (uchar) min_uchar(255, max_uchar(0, ratioVariance3f[1] *
                                                                  (pImg[3 * j + 1] - srcMean3f[1]) +
                                                                  tarMean3f[1]));
            pImg[3 * j + 2] = (uchar) min_uchar(255, max_uchar(0, ratioVariance3f[2] *
                                                                  (pImg[3 * j + 2] - srcMean3f[2]) +
                                                                  tarMean3f[2]));
        }
        pImg += srcLab.step;
    }

    // Lab空间转BGR空间
    cvtColor(srcLab, dst, COLOR_Lab2BGR);
}

// 计算当前图像与目标图像均值及标准差
void NewColorTransfer::compMeanAndVariance(Mat &img, Vec3f &mean3f, Vec3f &variance3f) {

    int row = img.rows;
    int col = img.cols;
    int total = row * col;
    float sum[3] = {0.0f};
// 均值
    //图像十三个通道的
    uchar *pImg = img.data;
    for (int i = 0; i < row; i++) {
        for (int j = 0; j < col; j++) {
            sum[0] += pImg[3 * j + 0];
            sum[1] += pImg[3 * j + 1];
            sum[2] += pImg[3 * j + 2];
        }
        pImg += img.step;//到下一行
    }
    mean3f[0] = sum[0] / total;
    mean3f[1] = sum[1] / total;
    mean3f[2] = sum[2] / total;

    memset(sum, 0, sizeof(sum));
// 标准差
    pImg = img.data;
    for (int i = 0; i < row; i++) {
        for (int j = 0; j < col; j++) {
            sum[0] += (pImg[3 * j + 0] - mean3f[0]) * (pImg[3 * j + 0] - mean3f[0]);
            sum[1] += (pImg[3 * j + 1] - mean3f[1]) * (pImg[3 * j + 1] - mean3f[1]);
            sum[2] += (pImg[3 * j + 2] - mean3f[2]) * (pImg[3 * j + 2] - mean3f[2]);
        }
        pImg += img.step;
    }

    variance3f[0] = sqrt(sum[0] / total);
    variance3f[1] = sqrt(sum[1] / total);
    variance3f[2] = sqrt(sum[2] / total);
}

/**
 * 返回的结果已被转换为BGRA形式
 */
Mat new_transferColorSyle(_JNIEnv *env, Mat &under, Mat &above, jintArray &in_rect) {
    LOGE("进入颜色转换");
    under=toInterceptRect(env,under,in_rect);
    cvtColor(under, under, COLOR_RGBA2BGR, 3);
    cvtColor(above, above, COLOR_BGRA2BGR, 3);
    Mat dst;
    NewColorTransfer colorTransfer;
    colorTransfer.colorTransfer(above, under, dst);
    cvtColor(dst, dst, COLOR_BGR2BGRA, 4);
    LOGE("颜色转换完成");
    return dst;
}
Mat toInterceptRect(JNIEnv *env, Mat under, jintArray in_rect) {
    LOGE("开始获取内部的Mat");
    jboolean isCopy = (jboolean) false;
    int *pi = env->GetIntArrayElements(in_rect, &isCopy);
    int in_width = pi[2] - pi[0], in_height = pi[3] - pi[1];
    int left = pi[0], top = pi[1], right = pi[2], bottom = pi[3];
    //将适配颜色的边界调整为为原来边界的一倍
    left = max(0, left - in_width / 6);
    right = min(under.rows, right + in_width / 6);
    top = max(0, top - in_height / 6);
    bottom = min(under.cols, bottom + in_height / 6);

    Mat m_in_under = under(Range(left, right),Range(top, bottom));

    return m_in_under;
}
