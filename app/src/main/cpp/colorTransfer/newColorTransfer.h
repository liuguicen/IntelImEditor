//
// Created by Administrator on 2017/1/13 0013.
//
#include <jni.h>
#include <opencv2/core.hpp>

using namespace cv;

#ifndef BAOZOUTU_NEWCOLORTRANSFER_H
#define BAOZOUPTU_NEWCOLORTRANSFER_H


class NewColorTransfer {
public:
    void colorTransfer(Mat &src, Mat &tar, Mat &dst);

    void compMeanAndVariance(Mat &img, Vec3f &mean3f, Vec3f &variance3f);

};

Mat new_transferColorSyle(_JNIEnv *env, Mat &under, Mat &above, jintArray &in_rect);

#endif //BAOZOUPTU_NEWCOLORTRANSFER_H
