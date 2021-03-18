/*
//
// Created by Administrator on 2017/1/11 0011.
//

#ifndef BAOZOUPTU_COLORTRANSFER_H
#define BAOZOUPTU_COLORTRANSFER_H

#include<vector>
#include <opencv2/core/cvdef.h>
#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc.hpp>

using namespace cv;
using namespace std;


class ColorTransfer {
public:
    Mat resultImg;

    ColorTransfer(Mat &src, Mat &target);
    Mat computeResult();

private:
    //读入的RGB图像
    Mat srcImg_32F;
    Mat targetImg_32F;
    //转换后的Lab空间图像
    Mat srcImg_Lab;
    Mat targetImg_Lab;
    //计算得到的均值和方差
    vector<double> srcMeans;
    vector<double> targetMeans;
    vector<double> srcVariances;
    vector<double> targetVariances;

    //RGB转换到Lab空间
    Mat RGBToLab(Mat &m);

    //Lab转换到RGB空间
    Mat LabToRGB(Mat &m);

    vector<double> computeMeans(Mat &m);

    vector<double> computeVariances(Mat &m, vector<double> &means);
};
*/
/**
 *
 *//*

Mat transferColorSyle(Mat &under, Mat &above);




#endif //BAOZOUPTU_COLORTRANSFER_H
*/
