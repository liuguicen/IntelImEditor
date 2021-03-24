package com.mandi.intelimeditor.ptu.transfer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;

import com.mandi.intelimeditor.common.appInfo.IntelImEditApplication;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import org.pytorch.IValue;
import org.pytorch.Module;
import org.pytorch.Tensor;
import org.pytorch.torchvision.TensorImageUtils;

public class StyleTransfer {
    private static final String TAG = "StyleTransfer";
    private Module vgg_encoder = null;
    private Module decoder = null;

    private static class InnerClass {
        private static StyleTransfer staticInnerClass = new StyleTransfer(IntelImEditApplication.appContext);

    }

    public static StyleTransfer getInstance() {
        return InnerClass.staticInnerClass;
    }

    private StyleTransfer(Context context) {
        try {
            // app/src/model/assets/model.pt
            vgg_encoder = Module.load(assetFilePath(context, "vgg_encoder.pt"));
            // adain = Module.load(assetFilePath(this, "adain.pt"));
            decoder = Module.load(assetFilePath(context, "adain_decoder.pt"));

        } catch (IOException e) {
            Log.e("PytorchHelloWorld", "Error reading assets", e);
        }
    }


    public Tensor getVggFeature(Bitmap bm) {
        Tensor feature = null;
        try {
            long time = System.currentTimeMillis();
            final Tensor tensor = TensorImageUtils.bitmapToFloat32Tensor(bm,
                    TensorImageUtils.TORCHVISION_NORM_MEAN_RGB, TensorImageUtils.TORCHVISION_NORM_STD_RGB);
            feature = vgg_encoder.forward(IValue.from(tensor)).toTensor();
            Log.d(TAG, "获取特征时间 = " + (System.currentTimeMillis() - time));
        } catch (Exception e) {
            Log.d(TAG, "模型获取VGG特征失败" + e.getMessage());
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
