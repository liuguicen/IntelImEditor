package com.mandi.intelimeditor.ptu.imageProcessing;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.Log;

import androidx.annotation.Nullable;

import com.mandi.intelimeditor.common.appInfo.IntelImEditApplication;

import com.mandi.intelimeditor.common.util.LogUtil;
import com.mandi.intelimeditor.common.util.Util;
import com.mandi.intelimeditor.common.util.geoutil.MPoint;
import com.mandi.intelimeditor.mnn.MNNForwardType;
import com.mandi.intelimeditor.mnn.MNNImageProcess;
import com.mandi.intelimeditor.mnn.MNNNetInstance;
import com.mandi.intelimeditor.ptu.gif.GifDecoderFromVideo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Locale;

public class FaceFeatureDetector {
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
    private static MNNNetInstance.Session.Tensor mInputTensor;
    private final Context context;
    private FaceSDKNative faceSDKNative;

    public FaceFeatureDetector(Context context) {
        this.context = context;
    }

    public static MPoint kp2Point(float[] landmark, int id) {
        return new MPoint(landmark[id * 2], landmark[id * 2 + 1]);
    }

    /**
     * 注意bm的宽高必须都超过200
     * 第0个元素表示人脸张数，后面是人脸box
     * 不支持565 ，改了{@link GifDecoderFromVideo#getFrameBm(long, int, int)}
     * 中的代码，后面找到支持的要改回去，
     */
    public float[] detectFace(Bitmap bm) {
        Log.d(TAG, "detectFace: 开始");
        if (bm == null || bm.isRecycled()) {
            Log.e(TAG, "detectFace: 图片出错");
            return null;
        }
        if (bm.getWidth() < 200 && bm.getHeight() < 200) {
            Log.d(TAG, "detectFace: 图片太小，检测精度不够，不检测");
            return null;
        }

        if (bm.getConfig() != Bitmap.Config.ARGB_8888) {
            Log.e(TAG, "detectFace: 无法检测非8888格式的图片，退出检测");
            // TODO: 2020/10/20 后面能否找到支持的，然后 GifDecoderFromVideo.getFrameBm处的代码也要改掉，
            // 不要
            return null;
        }

        //copy model
        if (faceSDKNative == null) {
            faceSDKNative = new FaceSDKNative();
        }
        // 测试出的下面几个模型似乎都没什么区别
        //RFB-320-quant-ADMM-32
//            copyBigDataToSD(context, "RFB-320.mnn");
//            copyBigDataToSD("RFB-320-quant-ADMM-32.mnn");
//            copyBigDataToSD("RFB-320-quant-KL-5792.mnn");
//            copyBigDataToSD("slim-320.mnn");
        String modelPath = assetToFile(context, "slim-320-quant-ADMM-50.mnn");
        faceSDKNative.FaceDetectionModelInit(modelPath);


        int width = bm.getWidth();
        int height = bm.getHeight();
        byte[] imageDate = getPixelsRGBA(bm);

        long timeDetectFace = System.currentTimeMillis();
        //do FaceDetect

//        Log.d(TAG, "detectFace: width " + width + " height " + height);
//        Log.d(TAG, "detectFace: size " + imageDate.length);
        Bitmap.Config bmConfig = bm.getConfig();
//        Log.d(TAG, "detectFace: bmConfig " + bmConfig);
        int imageChannel = 4;

        int[] faceInfo = faceSDKNative.FaceDetect(imageDate, bm.getWidth(), bm.getHeight(), imageChannel);
        timeDetectFace = System.currentTimeMillis() - timeDetectFace;
        if (faceInfo == null) {
            Log.e(TAG, "detectFace: 检测人脸出错 ");
            return null;
        }
        if (faceInfo.length < 4) {
            Log.d(TAG, "detectFace:未检测到人脸");
        } else {
            Log.d(TAG, String.format(Locale.CANADA, "detectFace: 检测到人脸 = %d 张", faceInfo[0]));
        }
        float[] position = new float[faceInfo.length];
        for (int i = 0; i < faceInfo.length; i++) {
            position[i] = faceInfo[i];
        }
        return position;
    }


    private static byte[] getPixelsRGBA(Bitmap bm) {
        // calculate how many byteCount our bm consists of
        int byteCount = bm.getByteCount();
        ByteBuffer buffer = ByteBuffer.allocate(byteCount); // Create a new buffer
        bm.copyPixelsToBuffer(buffer); // Move the byte data to the buffer
        return buffer.array();
    }

    /**
     * @param faceBoxs 人脸的box
     * @return 返回关键点位置，相当于输入bm的
     */
    @Nullable
    public float[] faceLandmark(Bitmap srcBm, float[] faceBoxs) {
        Log.d(TAG, "faceLandmark: 开始检测人脸关键点");
        if (faceBoxs == null || faceBoxs.length < 5) {
            Log.d(TAG, "faceLandmark: 没有人脸，退出");
            return null;
        }
        Log.d(TAG, "人脸位置:" + Arrays.toString(faceBoxs));
        Matrix matrix = new Matrix();
        int faceW = (int) (faceBoxs[3] - faceBoxs[1]);
        int faceH = (int) (faceBoxs[4] - faceBoxs[2]);
        float extendRatio = 0f;
        // box需要扩展，不然关键点检测效果有问题
        int left = (int) Math.max(faceBoxs[1] - faceW * extendRatio, 0);
        int top = (int) Math.max(faceBoxs[2] - faceH * extendRatio, 0);
        int right = (int) Math.min(faceBoxs[3] + faceW * extendRatio, srcBm.getWidth());
        int bottom = (int) Math.min(faceBoxs[4] + faceH * extendRatio, srcBm.getHeight());

        Bitmap faceBm = Bitmap.createBitmap(srcBm, left, top, right - left, bottom - top, matrix, true);
        matrix.postScale(160f / faceW, 160f / faceH);
        if (LogUtil.debugFace) {
            Log.d(TAG, "人脸长宽 = " + faceW + " " + faceH);
//        pytorchLandmark();

            // getting tensor content as java array of floats
            Log.d(TAG, "bm格式 " + faceBm.getConfig());
        }
        final float[] position = mnnFaceLandmark(faceBm);
        if (LogUtil.debugFace) {
            Log.d(TAG, "人脸关键点 原始值 " + Arrays.toString(position));
        }
        int fw = faceBm.getWidth(), fh = faceBm.getHeight();
        for (int i = 0; i < position.length - 1; i += 2) {
            position[i] = position[i] * fw + faceBoxs[1];
            position[i + 1] = position[i + 1] * fh + faceBoxs[2];
        }
        if (LogUtil.debugFace) {
            Log.d(TAG, "人脸关键点, 图片中的值 " + Arrays.toString(position));
        }
        return position;
    }

    private float[] mnnFaceLandmark(Bitmap bm) {
        /*
         *  convert data to input tensor
         */
        prepareMobileNet(IntelImEditApplication.appContext);
        final MNNImageProcess.Config config = new MNNImageProcess.Config();
        // normalization params

//          config.mean = new float[]{103.94f, 116.78f, 123.68f};
//          config.normal = new float[]{0.017f, 0.017f, 0.017f};
        // 这里的归一化貌似和pytorch的不一样，不知道它是什么原理，
        // mean需要设置到127附件然后结果才会是 -1~ 1
        config.mean = new float[]{127f, 127f, 127f};
        config.normal = new float[]{0.017f, 0.017f, 0.017f};
//            config.mean = new float[]{127.5f, 127.5f, 127.5f};
//            config.normal = new float[]{2.0f / 255.0f, 2.0f / 255.0f, 2.0f / 255.0f};
//            config.mean = new float[]{0.485f, 0.456f, 0.406f};
//            config.normal = new float[]{0.229f, 0.224f, 0.225f};
        // input data format
        config.dest = MNNImageProcess.Format.BGR;
        // bitmap transform
        Matrix matrix = new Matrix();
        int bmW = bm.getWidth();
        int bmH = bm.getHeight();
        Log.d(TAG, "bmw = " + bmW);
        matrix.postScale(160 / (float) bmW, 160 / (float) bmH);
        matrix.invert(matrix); // mnn的特殊要求，变换是反向的，要取逆变换

        MNNImageProcess.convertBitmap(bm, mInputTensor, config, matrix);
        final long startTimestamp = System.nanoTime();
        /**
         * inference
         */
        mSession.run();

        final long endTimestamp = System.nanoTime();
        final float inferenceTimeCost = (endTimestamp - startTimestamp) / 1000000.0f;

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
        float[] result = output.getFloatData();// get float results

        return result;
    }

    public float[] detectFaceAndLandMark(Bitmap bm) {
        return faceLandmark(bm, detectFace(bm));
    }

    public static FaceFeature analysisFaceFeature(float[] landMark, RectF faceBox) {
        FaceFeature faceFeature = new FaceFeature();
        MPoint lEye = kp2Point(landMark, KP_L_EYE);
        MPoint rEye = kp2Point(landMark, KP_R_EYE);
//        faceFeature.face_size = GeoUtil.getDis(lEye, rEye);
        faceFeature.faceWidth = faceBox.width();
        MPoint line = rEye.sub(lEye);
        faceFeature.angleY = (float) Math.toDegrees(Math.atan2(line.y, line.x));
        return faceFeature;
    }

    private static void copyBigDataToSD(Context context, String strOutFileName) throws IOException {
        Log.i(TAG, "start copy file " + strOutFileName);
        File sdDir = context.getFilesDir();//get root dir
        File file = new File(sdDir.toString() + "/facesdk/");
        if (!file.exists()) {
            file.mkdir();
        }

        String tmpFile = sdDir.toString() + "/facesdk/" + strOutFileName;
        File f = new File(tmpFile);
        if (f.exists()) {
            Log.i(TAG, "file exists " + strOutFileName);
            return;
        }
        InputStream myInput;
        OutputStream myOutput = new FileOutputStream(sdDir.toString() + "/facesdk/" + strOutFileName);
        myInput = context.getAssets().open(strOutFileName);
        byte[] buffer = new byte[1024];
        int length = myInput.read(buffer);
        while (length > 0) {
            myOutput.write(buffer, 0, length);
            length = myInput.read(buffer);
        }
        myOutput.flush();
        myInput.close();
        myOutput.close();
        Log.i(TAG, "end copy file " + strOutFileName);

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

//    private static void pytorchLandmark(Context context, Bitmap bm) {
//        Module module;
//        // preparing input tensor
//        // app/src/model/assets/model.pt
////      module = Module.load(assetFilePath(this, "model.pt"));
//        module = Module.load(assetFilePath(context, "face_landmak_model.pt"));
//        Bitmap inputBm = Bitmap.createScaledBitmap(bm, 160, 160, true);
//
//        float[] floats = detectFace(context, bm);
//        if (floats != null) {
//            Log.d(TAG, "getFaceLandmark: 人脸范围");
//            for (float aFloat : floats) {
//                Log.d(TAG, "" + aFloat);
//            }
//        }
//        final Tensor inputTensor = TensorImageUtils.bitmapToFloat32Tensor(inputBm,
//                TensorImageUtils.TORCHVISION_NORM_MEAN_RGB, TensorImageUtils.TORCHVISION_NORM_STD_RGB);
//        // running the model
//        Log.d(TAG, "输入数据 = " + Arrays.toString(inputTensor.getDataAsFloatArray()));
//        final Tensor outputTensor = module.forward(IValue.from(inputTensor)).toTensor();
//        outputTensor.getDataAsFloatArray();
//    }


    private void prepareMobileNet(Context context) {

//        String modelPath = getCacheDir() + "mobilenet.mnn";

        String modelFileName = "slim_160_latest.mnn";
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
        mInputTensor = mSession.getInput(null);
    }

    public void drawFaceBox(Bitmap bm, float[] faceBoxes) {
        Canvas canvas = new Canvas(bm);
        if (faceBoxes != null) {
            Paint paint = new Paint();
            paint.setColor(Color.RED);
            paint.setStrokeWidth(Util.dp2Px(1));
            for (int i = 1; i < faceBoxes.length; i += 4) {
                if (i + 3 < faceBoxes.length) {
                    float left = faceBoxes[i + 0],
                            top = faceBoxes[i + 1],
                            right = faceBoxes[i + 2],
                            bottom = faceBoxes[i + 3];
                    canvas.drawLine(left, top, left, bottom, paint);
                    canvas.drawLine(left, top, right, top, paint);
                    canvas.drawLine(right, top, right, bottom, paint);
                    canvas.drawLine(left, bottom, right, bottom, paint);
                }
            }
        }
    }

    public static void drawLandmark(Bitmap src, float[] faceLandmark) {
        Canvas canvas = new Canvas(src);
        Paint paint = new Paint();
        paint.setColor(Color.GREEN);
        for (int i = 0; i < faceLandmark.length - 1; i += 2) {
            if (Util.idInArray(FaceFeatureDetector.KP_ID, i / 2) >= 0) {
                paint.setStrokeWidth(src.getWidth() / 20f);
            } else {
                paint.setStrokeWidth(src.getWidth() / 50f);
            }
            canvas.drawPoint(faceLandmark[i], faceLandmark[i + 1], paint);
        }
    }

    public void releaseResource() {
        if (mNetInstance != null)
            mNetInstance.release();
        if (faceSDKNative != null)
            faceSDKNative.FaceDetectionModelUnInit();
    }

    public static float[] get6Landmark(float[] faceLandmark) {
        float[] floats = new float[KP_ID.length * 2];
        for (int i = 0; i < KP_ID.length; i++) {
            floats[i] = faceLandmark[KP_ID[i] * 2];
            floats[i + 1] = faceLandmark[KP_ID[i] * 2 + 1];
        }
        return floats;
    }
}
