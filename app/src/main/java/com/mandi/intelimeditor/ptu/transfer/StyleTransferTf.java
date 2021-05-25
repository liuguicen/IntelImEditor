package com.mandi.intelimeditor.ptu.transfer;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
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

public class StyleTransferTf {
    static final String TAG = "StyleTransferTf";
    private static final int CONTENT_IMAGE_SIZE = 384;
    private static final int STYLE_IMAGE_SIZE = 256;
    private final String STYLE_PREDICT_INT8_MODEL = "style_predict_quantized_256.tflite";
    private final String STYLE_TRANSFER_INT8_MODEL = "style_transfer_quantized_384.tflite";
    private final String STYLE_PREDICT_FLOAT16_MODEL = "style_predict_f16_256.tflite";
    private final String STYLE_TRANSFER_FLOAT16_MODEL = "style_transfer_f16_384.tflite";


    private Context context;
    GpuDelegate gpuDelegate;
    Interpreter interpreterPredict;
    Interpreter interpreterTransform;
    private boolean useGPU = true;
    private int numberThreads;

    private static class InnerClass {
        private static StyleTransferTf staticInnerClass = new StyleTransferTf(IntelImEditApplication.appContext);

    }

    public static StyleTransferTf getInstance() {
        return StyleTransferTf.InnerClass.staticInnerClass;
    }

    private StyleTransferTf(Context context) {
        try {
            this.context = context;
            if (useGPU) {
                interpreterPredict = getInterpreter(context, STYLE_PREDICT_FLOAT16_MODEL, true);
                interpreterTransform = getInterpreter(context, STYLE_TRANSFER_FLOAT16_MODEL, true);
            } else {
                interpreterPredict = getInterpreter(context, STYLE_PREDICT_INT8_MODEL, false);
                interpreterTransform = getInterpreter(context, STYLE_TRANSFER_INT8_MODEL, false);
            }
            Log.d(TAG, "StyleTransfer: 模型加载完成");
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

    public Bitmap transfer(Bitmap cBm, Bitmap sBm, Context context) {
        try {
            Log.d(TAG, "start transfer google models");

            LogUtil.recordTime();
            // 风格和内容图像变成ByteBuffer
            ByteBuffer contentArray =
                    ImageUtils.Companion.bitmapToByteBuffer(cBm, CONTENT_IMAGE_SIZE, CONTENT_IMAGE_SIZE, 0, 255);

            ByteBuffer input =
                    ImageUtils.Companion.bitmapToByteBuffer(sBm, STYLE_IMAGE_SIZE, STYLE_IMAGE_SIZE, 0, 255);


            Log.d(TAG, "execute: use gpu = " + useGPU);
            Object[] inputsForPredict = new Object[]{input};
            Map<Integer, Object> outputsForPredict = new HashMap<>();
            float[][][][] styleBottleneck = new float[1][1][1][100];

            outputsForPredict.put(0, styleBottleneck);


            // The results of this inference could be reused given the style does not change
            // That would be a good practice in case this was applied to a video stream.
            interpreterPredict.runForMultipleInputsOutputs(inputsForPredict, outputsForPredict);
            LogUtil.logTimeConsumeAndRecord("第一步 预测完成");

            Object[] inputsForStyleTransfer = new Object[]{contentArray, styleBottleneck};
            Map<Integer, Object> outputsForStyleTransfer = new HashMap<>();
            float[][][][] outputImage = new float[1][CONTENT_IMAGE_SIZE][CONTENT_IMAGE_SIZE][3];
            outputsForStyleTransfer.put(0, outputImage);

            interpreterTransform.runForMultipleInputsOutputs(
                    inputsForStyleTransfer,
                    outputsForStyleTransfer
            );

            Bitmap styledImage =
                    ImageUtils.Companion.convertArrayToBitmap(outputImage, CONTENT_IMAGE_SIZE, CONTENT_IMAGE_SIZE);

            return styledImage;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
