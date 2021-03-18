##设置OpenCv的路径变量
#set(pathToProject F:\\androidProject\\IntelImEdit)
#set(pathToOpenCv F:\\Android\\OpenCV-3.1.0-android-sdk\\OpenCV-android-sdk)
#cmake_minimum_required(VERSION 3.6.0)
##支持-std=gnu++11
#set(CMAKE_VERBOSE_MAKEFILE on)
#set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=gnu++11")
#
##配置加载native依赖
#include_directories(${pathToOpenCv}/sdk/native/jni/include)
##动态方式加载
#add_library(lib_opencv STATIC IMPORTED )
##引入libopencv_java3.so文件
#set_target_properties(lib_opencv
#                       PROPERTIES
#                       IMPORTED_LOCATION ${pathToProject}/app/src/main/jniLibs/${ANDROID_ABI}/libopencv_java3.so
#                       )
#
#add_library(imageSynthesis SHARED
#                    ImageSynthesis.cpp
#                    NewColorTransfer.cpp
#                    MyUtil.cpp
#                    edgeBlur.cpp
# )
#
#target_link_libraries(imageSynthesis log android -ljnigraphics lib_opencv)