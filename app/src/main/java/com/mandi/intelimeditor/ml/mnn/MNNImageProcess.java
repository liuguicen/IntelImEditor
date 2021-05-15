package com.mandi.intelimeditor.ml.mnn;

import android.graphics.Bitmap;
import android.graphics.Matrix;

public class MNNImageProcess {

    public enum Format {
        /**
         * RGBA
         */
        RGBA(0),
        /**
         * RGB
         */
        RGB(1),
        /**
         * BGR
         */
        BGR(2),
        /**
         * GRAY
         */
        GRAY(3),
        /**
         * BGRA
         */
        BGRA(4),
        /**
         * YUV420
         */
        YUV_420(10),
        /**
         * YUVNV21
         */
        YUV_NV21(11);

        public int type;

        Format(int t) {
            type = t;
        }
    }

    public enum Filter {
        /**
         * NEAREST
         */
        NEAREST(0),
        /**
         * BILINEAL
         */
        BILINEAL(1),
        /**
         * BICUBIC
         */
        BICUBIC(2);

        public int type;

        Filter(int t) {
            type = t;
        }
    }

    public enum Wrap {
        /**
         * CLAMP_TO_EDGE
         */
        CLAMP_TO_EDGE(0),
        /**
         * ZERO
         */
        ZERO(1),
        /**
         * REPEAT
         */
        REPEAT(2);

        public int type;

        Wrap(int t) {
            type = t;
        }
    }


    public static class Config {
        // default
        public float mean[] = {0f, 0f, 0f, 0f};
        public float normal[] = {1f, 1f, 1f, 1f};
        public Format source = Format.RGBA; // MNNImageProcess.Config用来配置源数据格式和目标格式（如果是图片输入不需要指定源格式）
        public Format dest = Format.BGR;
        public Filter filter = Filter.NEAREST;
        public Wrap wrap = Wrap.CLAMP_TO_EDGE;
    }

    /**
     * 设置输入buffer
     *
     * @param buffer 输入的buffer
     * @param tensor 输入的Tensor
     * @param config 配置mean、normal、图片目标格式
     * @param matrix 定义裁剪、缩放、旋转等, 和Android中的matrix用法基本一样
     *               Matrix就是用来对图像做仿射变换用的，需要注意的是，这里的matrix参数是指从目标图像到源图像的变换矩阵。
     *               如果不好理解，可以按源图像变换到目标图像后再取逆。
     *               matrix.invert(matrix); // 因为是目标变换到源的矩阵，需要取逆
     * @return
     */
    public static boolean convertBuffer(byte[] buffer, int width, int height, MNNNetInstance.Session.Tensor tensor, Config config, Matrix matrix) {
        if (matrix == null) {
            matrix = new Matrix();
        }
        float value[] = new float[9];
        matrix.getValues(value);

        return MNNNetNative.nativeConvertBufferToTensor(buffer, width, height, tensor.instance(),
                config.source.type, config.dest.type, config.filter.type, config.wrap.type, value, config.mean, config.normal);
    }

    /**
     * 设置bitmap输入
     *
     * @param sourceBitmap bitmap
     * @param tensor       输入的Tensor
     * @param config       配置mean、normal、图片目标格式
     * @param matrix       定义裁剪、缩放、旋转等, 和Android中的matrix用法基本一样
     *                     Matrix就是用来对图像做仿射变换用的，需要注意的是，这里的matrix参数是指从目标图像到源图像的变换矩阵。
     *                     如果不好理解，可以按源图像变换到目标图像后再取逆。
     * @return
     */
    public static boolean convertBitmap(Bitmap sourceBitmap, MNNNetInstance.Session.Tensor tensor, Config config, Matrix matrix) {
        if (matrix == null) {
            matrix = new Matrix();
        }
        float value[] = new float[9];
        matrix.getValues(value);

        return MNNNetNative.nativeConvertBitmapToTensor(sourceBitmap, tensor.instance(),
                config.dest.type, config.filter.type, config.wrap.type, value, config.mean, config.normal);
    }
}
