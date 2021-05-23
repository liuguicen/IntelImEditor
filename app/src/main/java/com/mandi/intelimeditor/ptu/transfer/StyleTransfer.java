package com.mandi.intelimeditor.ptu.transfer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.mandi.intelimeditor.common.appInfo.IntelImEditApplication;
import com.mandi.intelimeditor.common.util.LogUtil;
import com.mandi.intelimeditor.ml.mnn.MNNNetInstance;
import com.mandi.intelimeditor.ptu.PtuActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Map;

import org.pytorch.IValue;
import org.pytorch.Module;
import org.pytorch.Tensor;
import org.pytorch.torchvision.TensorImageUtils;

public class StyleTransfer {
    private static final String TAG = "StyleTransfer";
    private Module vgg_encoder = null;
    private Module decoder = null;
    private Context context;

    private static class InnerClass {
        private static StyleTransfer staticInnerClass = new StyleTransfer(IntelImEditApplication.appContext);

    }

    public static StyleTransfer getInstance() {
        return InnerClass.staticInnerClass;
    }

    private StyleTransfer(Context context) {
        try {
            this.context = context;
            vgg_encoder = Module.load(assetFilePath(context, "vgg_encoder.pt"));
            decoder = Module.load(assetFilePath(context, "adain_decoder.pt"));
            Log.d(TAG, "StyleTransfer: 模型加载完成");
        } catch (IOException e) {
            Log.e("PytorchHelloWorld", "Error reading assets", e);
        }
    }


    // 规律：1、  floatBuffer = ByteBuffer.allocateDirect分配buff，buff会增加内存，然后利用buff创建tensor，还会增加内存，增加量比buff增加了多一点
    //      2、  多次使用同一个floatBuffer创建不同的tensor 仍然会增加内存，但是垃圾回收器可以将内存回收掉

    public Tensor getVggFeature(Bitmap bm) {
        Tensor feature = null;
        try {
            long time = System.currentTimeMillis();
            final Tensor tensor = TensorImageUtils.bitmapToFloat32Tensor(bm,
                    TensorImageUtils.TORCHVISION_NORM_MEAN_RGB, TensorImageUtils.TORCHVISION_NORM_STD_RGB);
            if (LogUtil.debugStyleTransfer) {
                Log.d(TAG, "准备bm转换tensor");
            }
            feature = vgg_encoder.forward(IValue.from(tensor)).toTensor();
            if (LogUtil.debugStyleTransfer) {
                Log.d(TAG, "获取特征时间 = " + (System.currentTimeMillis() - time));
                LogUtil.printMemoryInfo(TAG  + " 通过Vgg完成", context);
            }
        } catch (Exception e) {
            Log.d(TAG, "模型获取VGG特征失败" + e.getMessage());
            LogUtil.printMemoryInfo(TAG, context);
        }
        return feature;
    }

    public Bitmap transfer(Tensor contentFeature, Tensor styleFeature, float alpha) {
        // 执行adain
        // Tensor res = adain.forward(IValue.from(cFeature), IValue.from(sFeature)).toTensor();
        long time = System.currentTimeMillis();
        Map<String, IValue> outTensors = decoder.forward(IValue.from(contentFeature), IValue.from(styleFeature), IValue.from(alpha)).toDictStringKey();
        final Tensor imTensor = outTensors.get("im").toTensor();
        Tensor whTensor = outTensors.get("wh").toTensor();
        int[] rstwh = whTensor.getDataAsIntArray();
        Log.d(TAG, "adain 和 decoder 用时 " + (System.currentTimeMillis() - time));
        time = System.currentTimeMillis();


        // 图像会调整到卷积核长度的整数倍，比如4
        int rstW = rstwh[0];
        int rstH = rstwh[1];
        Bitmap rstBm = Bitmap.createBitmap(rstW, rstH, Bitmap.Config.ARGB_8888);
        int[] imArray = imTensor.getDataAsIntArray();
        for (int i = 0; i < rstH; i++) {
            for (int j = 0; j < rstW; j++) {
                int id = i * rstW + j;
                if (id * 3 + 2 < imArray.length) {
                    rstBm.setPixel(j, i, Color.argb(255, imArray[id * 3], imArray[id * 3 + 1], imArray[id * 3 + 2]));
                }
            }
        }
        Log.d(TAG, "图像格式控制耗时" + (System.currentTimeMillis() - time));
        return rstBm;
    }


    public Bitmap transfer(Bitmap contentBm, Bitmap styleBm, float alpha) {
        Log.e(TAG, "transfer: Content w = " + contentBm.getWidth() + ", h = " + contentBm.getHeight());
        Log.e(TAG, "transfer: style w = " + styleBm.getWidth() + ", h = " + styleBm.getHeight());

        Module vgg_encoder = null;
        // Module adain = null;
        Module decoder = null;
        try {
            // loading serialized torchscript vgg_encoder from packaged into app android asset model.pt,
            // app/src/model/assets/model.pt
            vgg_encoder = Module.load(assetFilePath(context, "vgg_encoder.pt"));
            // adain = Module.load(assetFilePath(this, "adain.pt"));
            decoder = Module.load(assetFilePath(context, "adain_decoder.pt"));
        } catch (IOException e) {
            Log.e("PytorchHelloWorld", "Error reading assets", e);
        }


        // preparing input tensor
        final Tensor contentTensor = TensorImageUtils.bitmapToFloat32Tensor(contentBm,
                TensorImageUtils.TORCHVISION_NORM_MEAN_RGB, TensorImageUtils.TORCHVISION_NORM_STD_RGB);
        final Tensor styleTensor = TensorImageUtils.bitmapToFloat32Tensor(styleBm,
                TensorImageUtils.TORCHVISION_NORM_MEAN_RGB, TensorImageUtils.TORCHVISION_NORM_STD_RGB);

        // running the model
        // VGG提取特征
        long time = System.currentTimeMillis();
        long startTime = time;
        final Tensor cFeature = vgg_encoder.forward(IValue.from(contentTensor)).toTensor();
        String msg = "获取特征晚完 time " + (System.currentTimeMillis() - time);

        Log.d(TAG, "onCreate: " + msg);
        time = System.currentTimeMillis();
        final Tensor sFeature = vgg_encoder.forward(IValue.from(styleTensor)).toTensor();
        msg = "获取特征完 time " + (System.currentTimeMillis() - time);
        Log.d(TAG, "onCreate: " + msg);

        time = System.currentTimeMillis();

        // 执行adain
        // Tensor res = adain.forward(IValue.from(cFeature), IValue.from(sFeature)).toTensor();
        Map<String, IValue> outTensors = decoder.forward(IValue.from(cFeature), IValue.from(sFeature), IValue.from(alpha)).toDictStringKey();
        final Tensor imTensor = outTensors.get("im").toTensor();
        Tensor whTensor = outTensors.get("wh").toTensor();
        int[] rstwh = whTensor.getDataAsIntArray();
        Log.d(TAG, "adain 和 decoder 用时 " + (System.currentTimeMillis() - time));
        time = System.currentTimeMillis();


        // 图像会调整到卷积核长度的整数倍，比如4
        int rstW = rstwh[0];
        int rstH = rstwh[1];
        Bitmap rstBm = Bitmap.createBitmap(rstW, rstH, contentBm.getConfig());
        int[] imArray = imTensor.getDataAsIntArray();
        for (int i = 0; i < rstH; i++) {
            for (int j = 0; j < rstW; j++) {
                int id = i * rstW + j;
                if (id * 3 + 2 < imArray.length) {
                    rstBm.setPixel(j, i, Color.rgb(imArray[id * 3], imArray[id * 3 + 1], imArray[id * 3 + 2]));
                }
            }
        }

//        rstBm.setPixels(intArray, 0, rstBm.getWidth(), 0, 0,
//                rstBm.getWidth(), rstBm.getHeight());

        Log.d(TAG, "图像格式控制耗时" + (System.currentTimeMillis() - time));
        time = System.currentTimeMillis();
        msg = "adain完成 time= " + (System.currentTimeMillis() - time);
        Log.d(TAG, msg);
        Log.d(TAG, "总耗时: " + (System.currentTimeMillis() - startTime));
        return rstBm;
    }

    /**
     * Copies specified asset to the file in /files app directory and returns this file absolute path.
     *
     * @return absolute file path
     */
    public static String assetFilePath(Context context, String assetName) throws IOException {
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
        }
    }
}
