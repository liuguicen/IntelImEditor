package com.mandi.intelimeditor.ptu.transfer;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.Log;

import com.mandi.intelimeditor.common.appInfo.IntelImEditApplication;
import com.mandi.intelimeditor.common.util.BitmapUtil;
import com.mandi.intelimeditor.common.util.LogUtil;

import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.gpu.GpuDelegate;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.Map;

/**
 * 关于参数，
 * 测试结果
 * 线程数量 numberTreads, 对cpu模式有较大影响，测试小米11 8核心 耗时 2 < 1 < 4 < 8
 * 对GPU模式没有影响，
 */
public class StyleTransferTf {
    static final String TAG = "StyleTransferTf";
    private static final int CONTENT_IMAGE_SIZE = 384;
    private static final int STYLE_IMAGE_SIZE = 256;
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
    private Canvas cConvertCanvas;
    private Canvas sConvertCanvas;
    private Bitmap cConvertBm;
    private Rect cRange;
    private ByteBuffer contentArray;
    private ByteBuffer sInput;

    private static class InnerClass {
        private static StyleTransferTf staticInnerClass = new StyleTransferTf(IntelImEditApplication.appContext);

    }

    public static StyleTransferTf getInstance() {
        return StyleTransferTf.InnerClass.staticInnerClass;
    }

    private StyleTransferTf(Context context) {
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
            cConvertBm = Bitmap.createBitmap(CONTENT_IMAGE_SIZE, CONTENT_IMAGE_SIZE, Bitmap.Config.ARGB_8888);
            cConvertCanvas = new Canvas(cConvertBm);
            sConvertBm = Bitmap.createBitmap(STYLE_IMAGE_SIZE, STYLE_IMAGE_SIZE, Bitmap.Config.ARGB_8888);
            sConvertCanvas = new Canvas(sConvertBm);
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
                cRange = ImageUtils.Companion.drawInBm(cBm, cConvertCanvas);
                contentArray = ImageUtils.Companion.bitmapToByteBuffer(cConvertBm, 0, 255);
            }
            if (sBm != null) {
                sConvertCanvas.drawBitmap(sBm, null,
                        new Rect(0, 0, sConvertCanvas.getWidth(), sConvertCanvas.getHeight()),
                        BitmapUtil.getBitmapPaint());
                sInput = ImageUtils.Companion.bitmapToByteBuffer(sConvertBm, 0, 255);
            }

            LogUtil.logTimeConsumeAndRecord("转换bm完成");

            Object[] inputsForPredict = new Object[]{sInput};
            Map<Integer, Object> outputsForPredict = new HashMap<>();
            float[][][][] styleBottleneck = new float[1][1][1][100];

            outputsForPredict.put(0, styleBottleneck);


            // The results of this inference could be reused given the style does not change
            // That would be a good practice in case this was applied to a video stream.
            interpreterPredict.runForMultipleInputsOutputs(inputsForPredict, outputsForPredict);
            LogUtil.logTimeConsumeAndRecord("第一步 风格预测完成 ");

            Object[] inputsForStyleTransfer = new Object[]{contentArray, styleBottleneck};
            Map<Integer, Object> outputsForStyleTransfer = new HashMap<>();
            float[][][][] outputImage = new float[1][CONTENT_IMAGE_SIZE][CONTENT_IMAGE_SIZE][3];
            outputsForStyleTransfer.put(0, outputImage);

            interpreterTransform.runForMultipleInputsOutputs(
                    inputsForStyleTransfer,
                    outputsForStyleTransfer
            );
            LogUtil.logTimeConsumeAndRecord("第二步 风格迁移完成 ");
            Bitmap styledImage =
                    ImageUtils.Companion.convertArrayToBitmap(outputImage, cRange);
            Bitmap.createScaledBitmap(styledImage, styledImage.getWidth() * 2, styledImage.getHeight() * 2, true);
            LogUtil.logTimeConsumeAndRecord("第三步 转换结果图片完成");
            return styledImage;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
