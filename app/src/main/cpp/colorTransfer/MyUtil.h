//
// Created by Administrator on 2017/1/11 0011.
//
#include<android/log.h>

#ifndef BAOZOUPTU_UTIL_H
#define BAOZOUPTU_UTIL_H

#endif //BAOZOUPTU_UTIL_H

#ifndef LOGE
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR,"@",__VA_ARGS__)
#endif

long getCurrentTime();
void logTime();