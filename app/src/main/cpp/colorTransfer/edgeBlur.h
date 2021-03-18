//
// Created by Administrator on 2017/1/16 0016.
//

#ifndef BAOZOUPTU_EDGECLUR_H
#define BAOZOUPTU_EDGECLUR_H

#endif //BAOZOUPTU_EDGECLUR_H
#include <opencv2/core.hpp>
using namespace cv;
Mat edge_blur(bool **vis, Mat src);
uchar ** createArray(int w, int h);
void deleteVisArray(uchar ** &p, int h);
void recordTransparentInfo(Mat &src, uchar **transInfo);
void restoreTransparentInfo(Mat &src, uchar **transInfo);
