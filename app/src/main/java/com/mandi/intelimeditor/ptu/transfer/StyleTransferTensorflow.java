package com.mandi.intelimeditor.ptu.transfer;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.util.Log;

import com.mandi.intelimeditor.common.appInfo.IntelImEditApplication;
import com.mandi.intelimeditor.common.dataAndLogic.SPUtil;
import com.mandi.intelimeditor.common.util.BitmapUtil;
import com.mandi.intelimeditor.common.util.LogUtil;
import com.mandi.intelimeditor.common.util.geoutil.MRect;

import org.jetbrains.annotations.NotNull;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.gpu.GpuDelegate;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 关于参数，
 * 测试结果
 * 线程数量 numberTreads, 对cpu模式有较大影响，测试小米11 8核心 耗时 2 < 1 < 4 < 8
 * 对GPU模式没有影响，
 */
public class StyleTransferTensorflow {
    static final String TAG = "StyleTransferTf";
    public static final int CONTENT_SIZE = 384;
    /**
     * 注意必须小于{@link #CONTENT_SIZE / 2}
     */
    private static final int ADJACENT_LEN = 40;
    public static final int STYLE_SIZE = 256;
    private final String STYLE_PREDICT_INT8_MODEL = "style_predict_quantized_256.tflite";
    private final String STYLE_TRANSFER_INT8_MODEL = "style_transfer_quantized_384.tflite";
    private final String STYLE_PREDICT_FLOAT16_MODEL = "style_predict_f16_256.tflite";
    private final String STYLE_TRANSFER_FLOAT16_MODEL = "style_transfer_f16_384.tflite";
    private Bitmap sConvertBm;


    private Context context;
    GpuDelegate gpuDelegate;
    Interpreter interpreterPredict;
    Interpreter interpreterTransform;
    private boolean useGPU = true;
    private int numberThreads = 2;

    /**
     * 因为这个模型智能处理固定尺寸的图片，这里用来将需要处理的图片画到固定尺寸的bm中，节省资源
     */
    private Canvas c_ConvertCanvas;
    private Canvas sConvertCanvas;
    private int[] sPixArray;
    private Bitmap c_ConvertBm;
    private Rect c_Range;
    private boolean isFirstTransfer = true;
    private float[][][][] sPredict;
    private int[] c_PixArray;
    private ByteBuffer c_Buffer;
    private ByteBuffer sBuffer;
    private float[][][][] outputArray;

    public boolean isFirstTransfer() {
        return isFirstTransfer;
    }

    private static class InnerClass {
        private static StyleTransferTensorflow staticInnerClass = new StyleTransferTensorflow(IntelImEditApplication.appContext);

    }

    public static StyleTransferTensorflow getInstance() {
        return StyleTransferTensorflow.InnerClass.staticInnerClass;
    }

    private StyleTransferTensorflow(Context context) {
        try {
            this.context = context;
            Log.d(TAG, "StyleTransfer: 开始加载模型");
            if (useGPU) {
                interpreterPredict = getInterpreter(context, STYLE_PREDICT_FLOAT16_MODEL, true);
                interpreterTransform = getInterpreter(context, STYLE_TRANSFER_FLOAT16_MODEL, true);
            } else {
                interpreterPredict = getInterpreter(context, STYLE_PREDICT_INT8_MODEL, false);
                interpreterTransform = getInterpreter(context, STYLE_TRANSFER_INT8_MODEL, false);
            }
            Log.d(TAG, "StyleTransfer: 模型加载完成");
            c_ConvertBm = Bitmap.createBitmap(CONTENT_SIZE, CONTENT_SIZE, Bitmap.Config.ARGB_8888);
            c_ConvertCanvas = new Canvas(c_ConvertBm);
            c_PixArray = new int[CONTENT_SIZE * CONTENT_SIZE];
            c_Buffer = ByteBuffer.allocateDirect(1 * CONTENT_SIZE * CONTENT_SIZE * 3 * 4);
            c_Buffer.order(ByteOrder.nativeOrder());
            c_Buffer.rewind();


            sConvertBm = Bitmap.createBitmap(STYLE_SIZE, STYLE_SIZE, Bitmap.Config.ARGB_8888);
            sConvertCanvas = new Canvas(sConvertBm);
            sPixArray = new int[STYLE_SIZE * STYLE_SIZE];
            sPredict = new float[1][1][1][100];
            sBuffer = ByteBuffer.allocateDirect(1 * STYLE_SIZE * STYLE_SIZE * 3 * 4);
            sBuffer.order(ByteOrder.nativeOrder());
            sBuffer.rewind();

            outputArray = new float[1][CONTENT_SIZE][CONTENT_SIZE][3];
            Log.d(TAG, "StyleTransfer: 底图准备完成");
        } catch (IOException e) {
            Log.e("PytorchHelloWorld", "Error reading assets", e);
        }
    }

    private Interpreter getInterpreter(Context context, String modelPath, boolean useGPU) throws IOException {
        Interpreter.Options tfliteOptions = new Interpreter.Options();
        gpuDelegate = null;
        if (useGPU) {
            gpuDelegate = new GpuDelegate();
            tfliteOptions.addDelegate(gpuDelegate);
        }
        tfliteOptions.setNumThreads(numberThreads);
        return new Interpreter(loadModelFile(context, modelPath), tfliteOptions);
    }


    private MappedByteBuffer loadModelFile(Context context, String modelPath) throws IOException {
        AssetFileDescriptor fileDescriptor = context.getAssets().openFd(modelPath);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        MappedByteBuffer retFile = fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
        fileDescriptor.close();
        return retFile;
    }

    /**
     * 注意传入空表示使用上一次的
     */
    public Bitmap transfer(Bitmap cBm, Bitmap sBm, Context context) {
        try {
            Log.d(TAG, "start transfer google models");
            Log.d(TAG, "execute: use gpu = " + useGPU + "  thread number = " + numberThreads);

            LogUtil.recordTime();
            // 风格和内容图像变成ByteBuffer
            if (cBm != null) {
                c_Range = ImageUtils.Companion.drawInBm(cBm, null, c_ConvertCanvas);
                ImageUtils.Companion.bitmapToByteBuffer(c_ConvertBm, 0, 255,
                        c_PixArray, c_Buffer);
            }
            if (sBm != null) {
                ByteBuffer sInput;
                sConvertCanvas.drawBitmap(sBm, null,
                        new Rect(0, 0, sConvertCanvas.getWidth(), sConvertCanvas.getHeight()),
                        BitmapUtil.getBitmapPaint());
                ImageUtils.Companion.bitmapToByteBuffer(sConvertBm, 0, 255,
                        sPixArray, sBuffer);
                Object[] stylePredictInput = new Object[]{sBuffer};
                Map<Integer, Object> stylePredictOutput = new HashMap<>();
                stylePredictOutput.put(0, sPredict);

                // The results of this inference could be reused given the style does not change
                // That would be a good practice in case this was applied to a video stream.
                interpreterPredict.runForMultipleInputsOutputs(stylePredictInput, stylePredictOutput);
                LogUtil.logTimeConsumeAndRecord("第一步 风格预测完成 ");
            }
            LogUtil.logTimeConsumeAndRecord("创建基础数据和风格预测完成");

            LogUtil.logTimeConsumeAndRecord("转换bm完成");
            Object[] transferInput = new Object[]{c_Buffer, sPredict};
            Map<Integer, Object> transferOutput = new HashMap<>();
            transferOutput.put(0, outputArray);

            interpreterTransform.runForMultipleInputsOutputs(
                    transferInput,
                    transferOutput
            );
            LogUtil.logTimeConsumeAndRecord("第二步 风格迁移完成 ");
            Bitmap styledImage =
                    ImageUtils.Companion.convertArrayToBitmap(outputArray, c_Range);
            Bitmap.createScaledBitmap(styledImage, styledImage.getWidth() * 2,
                    styledImage.getHeight() * 2, true);
            LogUtil.logTimeConsumeAndRecord("第三步 转换结果图片完成");
            isFirstTransfer = false;
            return styledImage;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 注意传入空表示使用上一次的
     * 将图片裁剪成一个个的小图再迁移,最后拼接起来，拼接的时候对边缘进行平滑过渡处理，证明是可行的，但是编码相当复杂
     */
    public Bitmap transferBigSize(Bitmap cBm, Bitmap sBm, Context context) {
        try {
            Log.d(TAG, "start transfer google models");
            Log.d(TAG, "use big size, execute: use gpu = " + useGPU + "  thread number = " + numberThreads);

            LogUtil.recordTime();
            if (sBm != null) {
                sConvertCanvas.drawBitmap(sBm, null,
                        new Rect(0, 0, sConvertCanvas.getWidth(), sConvertCanvas.getHeight()),
                        BitmapUtil.getBitmapPaint());
                ImageUtils.Companion.bitmapToByteBuffer(sConvertBm, 0, 255, sPixArray, sBuffer);
                Object[] inputsForPredict = new Object[]{sBuffer};
                Map<Integer, Object> outputsForPredict = new HashMap<>();
                outputsForPredict.put(0, sPredict);

                // The results of this inference could be reused given the style does not change
                // That would be a good practice in case this was applied to a video stream.
                interpreterPredict.runForMultipleInputsOutputs(inputsForPredict, outputsForPredict);
                LogUtil.logTimeConsumeAndRecord("第一步 风格预测完成 ");
            }
            int srcW = cBm.getWidth(), srcH = cBm.getHeight();
            ArrayList<Integer> wPos = getNodePosFill(srcW);
            ArrayList<Integer> hPos = getNodePosFill(srcH);
            Log.d(TAG, "transferBigSize: patch number " + wPos.size() * hPos.size());

            ArrayList<float[][][][]> patchRstBmList = new ArrayList<>();
            ArrayList<Rect> patchRangeList = new ArrayList<>();

            for (int j = 0; j < hPos.size() - 1; j += 2) {
                for (int i = 0; i < wPos.size() - 1; i += 2) {
                    // 第一步 准备数据
                    Rect patchRangeInContent = new Rect(wPos.get(i), hPos.get(j), wPos.get(i + 1), hPos.get(j + 1));
                    Rect patchRangeInOut = ImageUtils.Companion.drawInBm(cBm, patchRangeInContent, c_ConvertCanvas); // 将patch块画到cConvertBm

                    ImageUtils.Companion.bitmapToByteBuffer(c_ConvertBm, 0, 255, c_PixArray, c_Buffer); // 将cConvertBm的数据装入bytebuffer
                    // Log.d(TAG, "transferBigSize: 准备数据完成");
                    // 第二步，模型推理
                    // 下面就是模型的标准运行流程了，装备传入模型的输入输出数据结构，run，然后处理输出即可
                    Log.d(TAG, "patch " + i + " , " + j + " range =  " + patchRangeInContent + "转换bm完成");
                    Object[] inputsForTransfer = new Object[]{c_Buffer, sPredict};
                    Map<Integer, Object> outputsForStyleTransfer = new HashMap<>();
                    float[][][][] outputImage = new float[1][CONTENT_SIZE][CONTENT_SIZE][3];
                    outputsForStyleTransfer.put(0, outputImage);
                    interpreterTransform.runForMultipleInputsOutputs(
                            inputsForTransfer,
                            outputsForStyleTransfer
                    );

                    // 第三步 处理输出结果
                    // 全部统一到输入的content的坐标系下

                    if (patchRangeInOut.width() != patchRangeInContent.width()) {
                        // 如输入模型的图像可能进行了缩放, 需要缩放回来
                        Bitmap styledPatch =
                                ImageUtils.Companion.convertArrayToBitmap(outputImage, patchRangeInOut);
                        Bitmap scaledPatch = Bitmap.createScaledBitmap(styledPatch, patchRangeInContent.width(), patchRangeInContent.height(), true);
                        outputImage = Bitmap2FloatArray(scaledPatch);
                    } else { // 没有缩放的，直接使用输出的数据数组，先变成0-255 减少重复计算加速
                        for (int hid = 0; hid < outputImage[0].length; hid++) {
                            for (int wid = 0; wid < outputImage[0][0].length; wid++) {
                                float[] colors = outputImage[0][hid][wid];
                                colors[0] *= 255;
                                colors[1] *= 255;
                                colors[2] *= 255;
                            }
                        }
                    }

                    // outBm里面就是expendPatch，没有缩放，坐标系一致
                    patchRstBmList.add(outputImage);
                    patchRangeList.add(patchRangeInContent);
                    // rstCanvas.drawRect(patchInContent, LogUtil.getLogPaint());
                    // Log.d(TAG, "第三步 patch" + i + " , " + j + " 模型传输完成");
                    // Log.d(TAG, "transferBigSize: patch in content = " + patchInContent);
                    // Bitmap.createScaledBitmap(outBm, outBm.getWidth() * 2, outBm.getHeight() * 2, true);
                    // Log.d(TAG, "transferBigSize: ");
                }
            }

            BitmapUtil.BitmapPixelsConverter rstConverter = catPicPatch(cBm, patchRstBmList, patchRangeList);

            Log.d(TAG, "第三步 拼接结果图片完成");
            return rstConverter.getBimap();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @NotNull
    private BitmapUtil.BitmapPixelsConverter catPicPatch(Bitmap cBm, ArrayList<float[][][][]> patchRstBmList, ArrayList<Rect> patchRangeList) {
        Log.d(TAG, "transferBigSize: 开始拼接patch块");
        BitmapUtil.BitmapPixelsConverter rstConverter = new BitmapUtil.BitmapPixelsConverter(cBm.getWidth(), cBm.getHeight());
        for (int i = 0; i < patchRstBmList.size(); i++) {
            float[][][][] patchArray = patchRstBmList.get(i);
            Rect patchRange = patchRangeList.get(i);
            MRect noAdjacentRange = getNoAdjacentRange(patchRangeList.get(i), cBm);

            for (int xInContent = patchRange.left; xInContent < patchRange.right; xInContent++) {
                for (int yInContent = patchRange.top; yInContent < patchRange.bottom; yInContent++) {
                    int xInPatch = xInContent - patchRange.left;
                    int yInPatch = yInContent - patchRange.top;
                    int patchPix = getPix(patchArray, xInPatch, yInPatch);

                    List<Float> dis = noAdjacentRange.disEdge(xInContent, yInContent);
                    if (dis.size() == 1 && dis.get(0) <= 0) { // 在内部
                        rstConverter.setPixel(xInContent, yInContent, patchPix);
                    } else {
                        // 对于邻接区，分为两种情况，边相邻，角相邻
                        // 边相邻，比如A B两个块，那么使用距离反比计算每个块的贡献，比如总距离为30，距离A 10 B 20的点，
                        // 计算就是Pa* (30-10) /30 + Pb * (30-20) / 30, 最后总贡献还是与1对齐的
                        // 对于角相邻，涉及到4个块，计算方法类似于线性插值，先分别用边相邻的方法计算A B的中间点m，C，D中间点n，然后m n之间按照边相邻计算
                        // 最后结果就是 横向距离反比，乘以纵向距离反比，如下
                        float ratio = 1;
                        for (final Float d : dis) {
                            ratio *= (ADJACENT_LEN - d) / ADJACENT_LEN;
                        }

                        int pix = rstConverter.getPixel(xInContent, yInContent);
                        float[] patchColor = patchArray[0][yInPatch][xInPatch];
                        float[] baseColor = {(pix >> 16) & 0xFF, (pix >> 8) & 0xFF, pix & 0xFF};

                        // 四个边对应的点
                        int composedColor = composeColor(baseColor, patchColor, ratio);

                        rstConverter.setPixel(xInContent, yInContent, composedColor);

                        // if (340 - 45 < xInContent && xInContent < 340 + 15 && 90 < yInContent && yInContent < 100) {
                        //     LogUtil.d("\n patch id = " + i + " range = " + patchRange
                        //             + "\n 位置:" + xInContent + " " + yInContent
                        //             + "\n " + (dis.size() == 2 ? "角相邻" : "边相邻") + " ratio = " + ratio
                        //             + "\n 原始颜色  = " + Arrays.toString(patchColor)
                        //             + "\n 已填充颜色 = " + Arrays.toString(baseColor)
                        //             + " 组合后的颜色 = "
                        //             + Color.red(composedColor) + "  "
                        //             + Color.green(composedColor) + "  "
                        //             + Color.blue(composedColor));
                        // }
                        // rstConverter.setPixel(xInContent, yInContent, patchPix);
                    }
                }
            }
            // Log.d(TAG, "transferBigSize: patch " + i + "写入完成");
        }
        // rstCanvas.drawBitmap(styledImage, patchInOut, patchInContent, BitmapUtil.getBitmapPaint());
        return rstConverter;
    }

    private float[][][][] Bitmap2FloatArray(Bitmap bm) {
        int height = bm.getHeight();
        int width = bm.getWidth();
        float[][][][] floats = new float[1][height][width][3];
        for (int hid = 0; hid < height; hid++) {
            for (int wid = 0; wid < width; wid++) {
                int color = bm.getPixel(wid, hid);
                float[] rgb = floats[0][hid][wid];
                rgb[0] = (color >> 16) & 0xFF;
                rgb[1] = (color >> 8) & 0xFF;
                rgb[2] = color & 0xFF;
            }
        }
        return floats;
    }

    private Rect getExpendRange(int srcW, int srcH, int left, int top, int right, int bottom) {
        left = Math.max(0, left - ADJACENT_LEN);
        right = Math.min(srcW, right + ADJACENT_LEN);
        top = Math.max(0, top - ADJACENT_LEN);
        bottom = Math.min(srcH, bottom + ADJACENT_LEN);
        return new Rect(left, top, right, bottom);
    }

    /**
     * @param a     0- 1
     * @param b     0-255
     * @param ratio
     * @return
     */
    private int composeColor(float[] a, float[] b, float ratio) {
        // ratio = 1 / 2f;
        if (ratio > 1) LogUtil.e("composeColor: 比例出错了");
        return 0xff000000 | (Math.round(Math.min((a[0] + b[0] * ratio), 255)) << 16)
                | (Math.round(Math.min((a[1] + b[1] * ratio), 255)) << 8)
                | Math.round(Math.min((a[2] + b[2] * ratio), 255));
    }

    private int getPix(float[][][][] imageArray, int wid, int hid) {
        float[] color = imageArray[0][hid][wid];
        return 0xff000000 | ((Math.round(color[0])) << 16)
                | (Math.round((color[1])) << 8)
                | (Math.round((color[2])));
    }

    private MRect getNoAdjacentRange(Rect patchRange, Bitmap cBm) {
        MRect shrinkingRange = new MRect(patchRange);
        if (patchRange.left > 0) { // 左边为0 表示第一块，左边就没有邻接块，就不用拼接，也就没有拼接区域
            shrinkingRange.left += ADJACENT_LEN;
        }
        if (patchRange.right < cBm.getWidth()) {
            shrinkingRange.right -= ADJACENT_LEN;
        }
        if (patchRange.top > 0) {
            shrinkingRange.top += ADJACENT_LEN;
        }
        if (patchRange.bottom < cBm.getHeight()) {
            shrinkingRange.bottom -= ADJACENT_LEN;
        }
        return shrinkingRange;
    }


    public static ArrayList<Integer> getNodePos(int len) {
        ArrayList<Integer> nodePos = new ArrayList<>();
        int nodeNumber = Math.round(1f * len / CONTENT_SIZE); // 约数取整，比如1.4倍，那么就分成一段, 1.6倍，分成两段
        if (nodeNumber == 0) nodeNumber = 1;
        if (nodeNumber > 3) nodeNumber = 3; // 设置上限和下限

        int nodeLen = len / nodeNumber;

        for (int i = 0; i < nodeNumber; i++) {
            nodePos.add(i * nodeLen);
        }
        nodePos.add(len); // 最后的位置是边界
        return nodePos;
    }

    public static ArrayList<Integer> getNodePosFill(int len) {
        ArrayList<Integer> nodePos = new ArrayList<>();

        nodePos.add(0);
        // 对于超过一个contentsize，但是多出的没有超过1/3的，那么下次的块就会小，直接合并到当前这个块，也就是退出for循环
        // 把len位置加进去
        int patchNumber = 2;
        if (SPUtil.getHighResolutionMode()) {
            patchNumber = 5;
        }
        int patchLen = Math.max(len / patchNumber, CONTENT_SIZE); // 目前就只能设置到2倍，多了就很容易丢失整体信息，出现不和谐的块
        for (int pos = patchLen; pos + patchLen / 3 < len; pos += patchLen) {
            nodePos.add(pos); // 这个块的终点
            nodePos.add(pos - ADJACENT_LEN); // 下一个块的起点
        }
        nodePos.add(len); // 最后的位置是边界
        return nodePos;
    }
}
