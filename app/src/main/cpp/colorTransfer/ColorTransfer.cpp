/*
//
// Created by Administrator on 2017/1/11 0011.
//
#include <android/log.h>
#include <opencv2/core.hpp>
#include <opencv2/imgproc.hpp>
#include "ColorTransfer.h"
#include "MyUtil.h"

using namespace cv;
using namespace std;

ColorTransfer::ColorTransfer(Mat &temp_src, Mat &temp_target) {
    LOGE("进入了颜色转换的构造器");
    Mat src, target;
    LOGE("系统时间开始：%ld", getCurrentTime());
    cvtColor(temp_src, src, COLOR_BGRA2BGR);
    cvtColor(temp_target, target, COLOR_BGRA2BGR);
    LOGE("系统时间结束：%ld", getCurrentTime());
    src.convertTo(srcImg_32F, CV_32FC3, 1.0f / 255.f);//这里切记要类型转换下
    target.convertTo(targetImg_32F, CV_32FC3, 1.0f / 255.0f);
    LOGE("转换成浮点型成功");
    resultImg = srcImg_32F;  //将结果先初始化为源图像

    LOGE("自定义开始：%ld", getCurrentTime());
    srcImg_Lab = RGBToLab(srcImg_32F);
    targetImg_Lab = RGBToLab(targetImg_32F);
    LOGE("自定义结束：%ld", getCurrentTime());
    LOGE("转换mat成浮点格式数据成功");
    resultImg = srcImg_32F;  //将结果先初始化为源图像

    LOGE("RGB转换到lab空间成功");
    srcMeans = computeMeans(srcImg_Lab);
    targetMeans = computeMeans(targetImg_Lab);
    LOGE("计算图像均值和标准差成功一");
    srcVariances = computeVariances(srcImg_Lab, srcMeans);
    targetVariances = computeVariances(targetImg_Lab, targetMeans);
    LOGE("计算图像均值和标准差成功二");
}

//重RGB空间转换到lab空间
Mat ColorTransfer::RGBToLab(Mat &m) {
    Mat_<Vec3f> I = m;//定义一个Mat_型的mat，便于运算
    for (int i = 0; i < I.rows; ++i) {
        for (int j = 0; j < I.cols; ++j) {//矩阵运算，一行三列的RGB*[3*3]的参数矩阵
            double L = 0.3811 * I(i, j)[0] + 0.5783 * I(i, j)[1] + 0.0402 * I(i, j)[2];
            double M = 0.1967 * I(i, j)[0] + 0.7244 * I(i, j)[1] + 0.0782 * I(i, j)[2];
            double S = 0.0241 * I(i, j)[0] + 0.1288 * I(i, j)[1] + 0.8444 * I(i, j)[2];
            if (L == 0) L = 1;
            if (M == 0) M = 1;
            if (S == 0) S = 1;
            L = log(L);//求自然对数底
            M = log(M);
            S = log(S);
            I(i, j)[0] = (float) ((L + M + S) / sqrt(3.0));//运算一下重新赋值
            I(i, j)[1] = (float) ((L + M - 2 * S) / sqrt(6.0));
            I(i, j)[2] = (float) ((L - M) / sqrt(2.0));
        }
    }
    return I;
}

//Lab转换到RGB空间
Mat  ColorTransfer::LabToRGB(Mat &m) {
    Mat_<Vec3f> I = m;
    for (int i = 0; i < I.rows; ++i)
        for (int j = 0; j < I.cols; ++j) {
            double L = I(i, j)[0] / sqrt(3.0) + I(i, j)[1] / sqrt(6.0) + I(i, j)[2] / sqrt(2.0);
            double M = I(i, j)[0] / sqrt(3.0) + I(i, j)[1] / sqrt(6.0) - I(i, j)[2] / sqrt(2.0);
            double S = I(i, j)[0] / sqrt(3.0) - 2 * I(i, j)[1] / sqrt(6.0);
            L = exp(L);
            M = exp(M);
            S = exp(S);
            I(i, j)[0] = 4.4679 * L - 3.5873 * M + 0.1193 * S;
            I(i, j)[1] = -1.2186 * L + 2.3809 * M - 0.1624 * S;
            I(i, j)[2] = 0.0497 * L - 0.2439 * M + 1.2045 * S;
        }
    return I;
}

*/
/**
 * 分别计算源图像和目标图像均值及标准差
 *//*

vector<double>  ColorTransfer::computeMeans(Mat &m) {
    double sum[3] = {};
    int pixes = m.cols * m.rows;
    vector<double> means;
    means.resize(3);
    Mat_<Vec3f> I = m;
    for (int i = 0; i < I.rows; ++i)
        for (int j = 0; j < I.cols; ++j) {
            for (int k = 0; k < 3; k++) {
                sum[k] += I(i, j)[k];//三个颜色分量各自的总和加到sum元素中
            }
        }
    for (int i = 0; i < 3; i++) {
        means[i] = sum[i] / pixes;
    }
    return means;
}

*/
/**
 * 分别计算源图像和目标图像均值及标准差
 *//*

vector<double>  ColorTransfer::computeVariances(Mat &m, vector<double> &means) {
    double sum[3] = {};
    int pixes = m.cols * m.rows;
    Mat_<Vec3f> I = m;
    vector<double> variances;
    variances.resize(3);
    for (int i = 0; i < I.rows; ++i)
        for (int j = 0; j < I.cols; ++j) {
            for (int chanel = 0; chanel < 3; chanel++) {
                sum[chanel] += abs(I(i, j)[chanel] - means[chanel]);//每个颜色通道的值减去计算出的means值
            }
        }
    for (int i = 0; i < 3; i++) {
        variances[i] = sqrt(sum[i] / pixes);
    }
    return variances;
}

Mat  ColorTransfer::computeResult() {
    Mat_<Vec3f> I = ColorTransfer::resultImg;
    double dataTemp[3] = {};
    for (int chanel = 0; chanel < 3; chanel++) {
        dataTemp[chanel] = targetVariances[chanel] / srcVariances[chanel];
    }//三个通道的targetVariance的商
    for (int i = 0; i < I.rows; ++i)
        for (int j = 0; j < I.cols; ++j) {
            for (int chanel = 0; chanel < 3; chanel++) {
                I(i, j)[chanel] = (float) (dataTemp[chanel] * (I(i, j)[chanel] - srcMeans[chanel]) +
                                           targetMeans[chanel]);
            }//再是每个像素的每个通道对前面算出的记过进行一定的计算结合
        }
    resultImg = LabToRGB(resultImg);
    Mat result;
    cvtColor(resultImg,result,COLOR_RGB2RGBA);
    return result;
}

Mat transferColorSyle(Mat &under, Mat &above) {
    ColorTransfer clt(under, above);

    return clt.computeResult();
}
*/
