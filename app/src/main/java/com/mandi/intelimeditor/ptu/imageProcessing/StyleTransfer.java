package com.mandi.intelimeditor.ptu.imageProcessing;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.Log;

import com.mandi.intelimeditor.common.appInfo.IntelImEditApplication;
import com.mandi.intelimeditor.common.util.Util;
import com.mandi.intelimeditor.common.util.geoutil.MPoint;
import com.mandi.intelimeditor.mnn.MNNForwardType;
import com.mandi.intelimeditor.mnn.MNNImageProcess;
import com.mandi.intelimeditor.mnn.MNNNetInstance;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

public class StyleTransfer {
    public static final String TAG = "FaceLandmarkDetector";
    // 136关键点下的
    public static final int KP_CHIN = 8; // 下巴
    public static final int KP_NOSE = 30; // 鼻尖
    public static final int KP_L_EYE = 36; // 左眼角
    public static final int KP_R_EYE = 45; // 右眼角
    public static final int KP_L_MOUSE = 48; // 左嘴角
    public static final int KP_R_MOUSE = 54; // 右嘴角
    public static final int[] KP_ID = new int[]{KP_CHIN, KP_NOSE, KP_L_EYE, KP_R_EYE, KP_L_MOUSE, KP_R_MOUSE};

    private static MNNNetInstance mNetInstance;
    private static MNNNetInstance.Session mSession;
    private static MNNNetInstance.Session.Tensor mInputTensorA;
    private static MNNNetInstance.Session.Tensor mInputTensorB;
    private final Context context;

    public StyleTransfer(Context context) {
        this.context = context;
    }

    public static MPoint kp2Point(float[] landmark, int id) {
        return new MPoint(landmark[id * 2], landmark[id * 2 + 1]);
    }

    private static byte[] getPixelsRGBA(Bitmap bm) {
        // calculate how many byteCount our bm consists of
        int byteCount = bm.getByteCount();
        ByteBuffer buffer = ByteBuffer.allocate(byteCount); // Create a new buffer
        bm.copyPixelsToBuffer(buffer); // Move the byte data to the buffer
        return buffer.array();
    }

    public float[] mnnTransfer(Bitmap content, Bitmap style) {
        /*
         *  convert data to input tensor
         */
        prepareMobileNet(IntelImEditApplication.appContext);
        final long startTimestamp = System.currentTimeMillis();
        Log.d(TAG, "mnnTransfer: 图像尺寸 = " + content.getWidth() + " " + content.getHeight());
        processInput(content);
//        processInput(style);
        /**
         * inference
         */
        mSession.run();

        final long endTimestamp = System.nanoTime();

        /**
         * also you can use runWithCallback if you concern about some outputs of the middle layers,
         * this method execute inference and also return middle Tensor outputs synchronously.
         * 可以用回调的方式输出中间层的内容
         */
        //                MNNNetInstance.Session.Tensor[] tensors =  mSession.runWithCallback(new String[]{"conv1"});

        /**
         * get output tensor
         */
        MNNNetInstance.Session.Tensor output = mSession.getOutput(null);
        Log.d(TAG, "mnnTransfer: 获取特征时间 = " + (System.currentTimeMillis() - startTimestamp));
        Log.d(TAG, "mnnTransfer: 结果数组长度 = " + output.getIntData().length);
        float[] result = output.getFloatData();// get float results

        return result;
    }

    private void processInput(Bitmap bm) {
        final MNNImageProcess.Config config = new MNNImageProcess.Config();
        config.mean = new float[]{127f, 127f, 127f};
        config.normal = new float[]{0.017f, 0.017f, 0.017f};
        config.dest = MNNImageProcess.Format.BGR;
        Matrix matrix = new Matrix();
        int bmW = bm.getWidth();
        int bmH = bm.getHeight();
        Log.d(TAG, "bmw = " + bmW);
        matrix.postScale(160 / (float) bmW, 160 / (float) bmH); // 固定尺寸
        matrix.invert(matrix); // mnn的特殊要求，变换是反向的，要取逆变换

        MNNImageProcess.convertBitmap(bm, mInputTensorA, config, matrix);
    }

    /**
     * Copies specified asset to the file in /files app directory and returns this file absolute path.
     *
     * @return absolute file path
     */
    public static String assetToFile(Context context, String assetName) {
        File file = new File(context.getFilesDir(), assetName);
        if (file.exists() && file.length() > 0) {
            return file.getAbsolutePath();
        }

        try (InputStream is = context.getAssets().open(assetName)) {
            try (OutputStream os = new FileOutputStream(file)) {
                byte[] buffer = new byte[4 * 1024];
                int read;
                while ((read = is.read(buffer)) != -1) {
                    os.write(buffer, 0, read);
                }
                os.flush();
            }
            return file.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    private void prepareMobileNet(Context context) {

        //        String modelPath = getCacheDir() + "mobilenet.mnn";

        String modelFileName = "vgg_encoder.mnn";
        String modelPath = assetToFile(context, modelFileName);
        // create net instance
        if (mNetInstance == null) {
            mNetInstance = MNNNetInstance.createFromFile(modelPath);
        }
        if (mNetInstance == null) return;

        // create session with config
        MNNNetInstance.Config config = new MNNNetInstance.Config();
        config.numThread = 4;// set threads
        config.forwardType = MNNForwardType.FORWARD_CPU.type;// set CPU/GPU
        /**
         * config middle layer names, if you concern about the output of the middle layer.
         * use session.getOutput("layer name") to get the output of the middle layer.
         */
        //        config.saveTensors = new String[]{"conv1"};
        mSession = mNetInstance.createSession(config);

        // get input tensor, 应该就是这里直接创建一个就行，后面将数据填充到里面
        mInputTensorA = mSession.getInput(null);
//        mInputTensorB = mSession.getInput(null);
    }

    public void releaseResource() {
        if (mNetInstance != null)
            mNetInstance.release();
    }
}
