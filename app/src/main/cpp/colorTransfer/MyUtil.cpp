//
// Created by Administrator on 2017/1/11 0011.
//
#include<stdio.h>
#include<sys/time.h>
#include "MyUtil.h"


long getCurrentTime()
{
    struct timeval tv;
    gettimeofday(&tv,NULL);
    return tv.tv_sec * 1000 + tv.tv_usec / 1000;
}
void logTime(){
    LOGE("锁住bitmap数组时间开始：%ld", getCurrentTime());
}
