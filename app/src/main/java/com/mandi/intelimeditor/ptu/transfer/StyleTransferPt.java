// package com.mandi.intelimeditor.ptu.transfer;
//
// import android.content.Context;
// import android.graphics.Bitmap;
// import android.graphics.Color;
// import android.util.Log;
//
// import com.mandi.intelimeditor.common.appInfo.IntelImEditApplication;
// import com.mandi.intelimeditor.common.util.LogUtil;
//
// import java.io.File;
// import java.io.FileOutputStream;
// import java.io.IOException;
// import java.io.InputStream;
// import java.io.OutputStream;
// import java.nio.FloatBuffer;
// import java.util.Map;
//
// import org.pytorch.IValue;
// import org.pytorch.Module;
// import org.pytorch.Tensor;
// import org.pytorch.torchvision.TensorImageUtils;
//
// public class StyleTransferPt {
//     private static final String TAG = "StyleTransfer";
//     private Module vgg_encoder = null;
//     private Module decoder = null;
//     private Context context;
//
//     public void clear() {
//         vgg_encoder.destroy();
//         vgg_encoder = null;
//         decoder.destroy();
//         decoder = null;
//     }
//
//     private static class InnerClass {
//         private static StyleTransferPt staticInnerClass = new StyleTransferPt(IntelImEditApplication.appContext);
//
//     }
//
//     public static StyleTransferPt getInstance() {
//         return InnerClass.staticInnerClass;
//     }
//
//     private StyleTransferPt(Context context) {
//         try {
//             this.context = context;
//             vgg_encoder = Module.load(assetFilePath(context, "vgg_encoder.pt"));
//             decoder = Module.load(assetFilePath(context, "adain_decoder.pt"));
//             Log.d(TAG, "StyleTransfer: 模型加载完成");
//         } catch (IOException e) {
//             Log.e("PytorchHelloWorld", "Error reading assets", e);
//         }
//     }
//
//     /**
//      * Writes tensor content from specified {@link android.graphics.Bitmap}, normalized with specified
//      * in parameters mean and std to specified {@link java.nio.FloatBuffer} with specified offset.
//      *
//      * @param bitmap      {@link android.graphics.Bitmap} as a source for Tensor data
//      * @param x           - x coordinate of top left corner of bitmap's area
//      * @param y           - y coordinate of top left corner of bitmap's area
//      * @param width       - width of bitmap's area
//      * @param height      - height of bitmap's area
//      * @param normMeanRGB means for RGB channels normalization, length must equal 3, RGB order
//      * @param normStdRGB  standard deviation for RGB channels normalization, length must equal 3, RGB
//      *                    order
//      */
//     public static void bitmapToFloatBuffer(
//             final Bitmap bitmap,
//             final int x,
//             final int y,
//             final int width,
//             final int height,
//             final float[] normMeanRGB,
//             final float[] normStdRGB,
//             final FloatBuffer outBuffer,
//             final int outBufferOffset) {
//
//         final int pixelsCount = height * width;
//         final int[] pixels = new int[pixelsCount];
//         bitmap.getPixels(pixels, 0, width, x, y, width, height);
//         final int offset_g = pixelsCount;
//         final int offset_b = 2 * pixelsCount;
//         for (int i = 0; i < pixelsCount; i++) {
//             final int c = pixels[i];
//             float r = (((c >> 16) & 0xff) / 255.0f - normMeanRGB[0]) / normStdRGB[0];
//             float g = (((c >> 8) & 0xff) / 255.0f - normMeanRGB[1]) / normStdRGB[1];
//             float b = (((c) & 0xff) / 255.0f - normMeanRGB[2]) / normStdRGB[2];
//             // 这里会出现栈溢出stackoverflow 大图会出现，小图分配floatbuffer，然后置空，然后再分配一个，也会出现
//             // 如果floatbuffer是通过直接分配allocatedirect得到的，似乎不会出现
//             // 综上，原因是直接分配的内存过大所致？？
//             outBuffer.put(outBufferOffset + i, r)
//                     .put(outBufferOffset + offset_g + i, g)
//                     .put(outBufferOffset + offset_b + i, b);
//             if (i % 1000 == 0) {
//                 Log.e(TAG, "bitmapToFloatBuffer: " + i);
//             }
//         }
//     }
//
//     // 规律：1、  floatBuffer = ByteBuffer.allocateDirect分配buff，buff会增加内存，然后利用buff创建tensor，还会增加内存，增加量比buff增加了多一点
//     //      2、  多次使用同一个floatBuffer创建不同的tensor 仍然会增加内存，但是若干次之后垃圾回收器可以将内存回收掉
//     //      3、  第2个条件，但是传入模型之后，就又不回收了
//
//     public Tensor getVggFeature(Tensor tensor) {
//         Tensor feature = null;
//         try {
//             long time = System.currentTimeMillis();
//             // final Tensor tensor = TensorImageUtils.bitmapToFloat32Tensor(bm,
//             //         TensorImageUtils.TORCHVISION_NORM_MEAN_RGB, TensorImageUtils.TORCHVISION_NORM_STD_RGB);
//             if (LogUtil.debugStyleTransfer) {
//                 Log.d(TAG, "准备bm转换tensor");
//             }
//             feature = vgg_encoder.forward(IValue.from(tensor)).toTensor();
//             if (LogUtil.debugStyleTransfer) {
//                 Log.d(TAG, "获取特征时间 = " + (System.currentTimeMillis() - time));
//                 LogUtil.printMemoryInfo(TAG + " 通过Vgg完成", context);
//             }
//         } catch (Exception e) {
//             Log.d(TAG, "模型获取VGG特征失败" + e.getMessage());
//             LogUtil.printMemoryInfo(TAG, context);
//         }
//         return feature;
//     }
//
//     public Bitmap transfer(Tensor contentFeature, Tensor styleFeature, float alpha) {
//         Log.d(TAG, "start transfer adain models");
//         // 执行adain
//         // Tensor res = adain.forward(IValue.from(cFeature), IValue.from(sFeature)).toTensor();
//         long time = System.currentTimeMillis();
//         Map<String, IValue> outTensors = decoder.forward(IValue.from(contentFeature), IValue.from(styleFeature), IValue.from(alpha)).toDictStringKey();
//         final Tensor imTensor = outTensors.get("im").toTensor();
//         Tensor whTensor = outTensors.get("wh").toTensor();
//         int[] rstwh = whTensor.getDataAsIntArray();
//         Log.d(TAG, "adain 和 decoder 用时 " + (System.currentTimeMillis() - time));
//         time = System.currentTimeMillis();
//
//
//         // 图像会调整到卷积核长度的整数倍，比如4
//         int rstW = rstwh[0];
//         int rstH = rstwh[1];
//         Bitmap rstBm = Bitmap.createBitmap(rstW, rstH, Bitmap.Config.ARGB_8888);
//         int[] imArray = imTensor.getDataAsIntArray();
//         for (int i = 0; i < rstH; i++) {
//             for (int j = 0; j < rstW; j++) {
//                 int id = i * rstW + j;
//                 if (id * 3 + 2 < imArray.length) {
//                     rstBm.setPixel(j, i, Color.argb(255, imArray[id * 3], imArray[id * 3 + 1], imArray[id * 3 + 2]));
//                 }
//             }
//         }
//         Log.d(TAG, "图像格式控制耗时" + (System.currentTimeMillis() - time));
//         return rstBm;
//     }
//
//
//     public Bitmap transfer(Bitmap contentBm, Bitmap styleBm, float alpha) {
//         Log.e(TAG, "transfer: Content w = " + contentBm.getWidth() + ", h = " + contentBm.getHeight());
//         Log.e(TAG, "transfer: style w = " + styleBm.getWidth() + ", h = " + styleBm.getHeight());
//
//         Module vgg_encoder = null;
//         // Module adain = null;
//         Module decoder = null;
//         try {
//             // loading serialized torchscript vgg_encoder from packaged into app android asset model.pt,
//             // app/src/model/assets/model.pt
//             vgg_encoder = Module.load(assetFilePath(context, "vgg_encoder.pt"));
//             // adain = Module.load(assetFilePath(this, "adain.pt"));
//             decoder = Module.load(assetFilePath(context, "adain_decoder.pt"));
//         } catch (IOException e) {
//             Log.e("PytorchHelloWorld", "Error reading assets", e);
//         }
//
//
//         // preparing input tensor
//         final Tensor contentTensor = TensorImageUtils.bitmapToFloat32Tensor(contentBm,
//                 TensorImageUtils.TORCHVISION_NORM_MEAN_RGB, TensorImageUtils.TORCHVISION_NORM_STD_RGB);
//         final Tensor styleTensor = TensorImageUtils.bitmapToFloat32Tensor(styleBm,
//                 TensorImageUtils.TORCHVISION_NORM_MEAN_RGB, TensorImageUtils.TORCHVISION_NORM_STD_RGB);
//
//         // running the model
//         // VGG提取特征
//         long time = System.currentTimeMillis();
//         long startTime = time;
//         final Tensor cFeature = vgg_encoder.forward(IValue.from(contentTensor)).toTensor();
//         String msg = "获取特征晚完 time " + (System.currentTimeMillis() - time);
//
//         Log.d(TAG, "onCreate: " + msg);
//         time = System.currentTimeMillis();
//         final Tensor sFeature = vgg_encoder.forward(IValue.from(styleTensor)).toTensor();
//         msg = "获取特征完 time " + (System.currentTimeMillis() - time);
//         Log.d(TAG, "onCreate: " + msg);
//
//         time = System.currentTimeMillis();
//
//         // 执行adain
//         // Tensor res = adain.forward(IValue.from(cFeature), IValue.from(sFeature)).toTensor();
//         Map<String, IValue> outTensors = decoder.forward(IValue.from(cFeature), IValue.from(sFeature), IValue.from(alpha)).toDictStringKey();
//         final Tensor imTensor = outTensors.get("im").toTensor();
//         Tensor whTensor = outTensors.get("wh").toTensor();
//         int[] rstwh = whTensor.getDataAsIntArray();
//         Log.d(TAG, "adain 和 decoder 用时 " + (System.currentTimeMillis() - time));
//         time = System.currentTimeMillis();
//
//
//         // 图像会调整到卷积核长度的整数倍，比如4
//         int rstW = rstwh[0];
//         int rstH = rstwh[1];
//         Bitmap rstBm = Bitmap.createBitmap(rstW, rstH, contentBm.getConfig());
//         int[] imArray = imTensor.getDataAsIntArray();
//         for (int i = 0; i < rstH; i++) {
//             for (int j = 0; j < rstW; j++) {
//                 int id = i * rstW + j;
//                 if (id * 3 + 2 < imArray.length) {
//                     rstBm.setPixel(j, i, Color.rgb(imArray[id * 3], imArray[id * 3 + 1], imArray[id * 3 + 2]));
//                 }
//             }
//         }
//
// //        rstBm.setPixels(intArray, 0, rstBm.getWidth(), 0, 0,
// //                rstBm.getWidth(), rstBm.getHeight());
//
//         Log.d(TAG, "图像格式控制耗时" + (System.currentTimeMillis() - time));
//         time = System.currentTimeMillis();
//         msg = "adain完成 time= " + (System.currentTimeMillis() - time);
//         Log.d(TAG, msg);
//         Log.d(TAG, "总耗时: " + (System.currentTimeMillis() - startTime));
//         return rstBm;
//     }
//
//     /**
//      * Copies specified asset to the file in /files app directory and returns this file absolute path.
//      *
//      * @return absolute file path
//      */
//     public static String assetFilePath(Context context, String assetName) throws IOException {
//         File file = new File(context.getFilesDir(), assetName);
//         if (file.exists() && file.length() > 0) {
//             return file.getAbsolutePath();
//         }
//
//         try (InputStream is = context.getAssets().open(assetName)) {
//             try (OutputStream os = new FileOutputStream(file)) {
//                 byte[] buffer = new byte[4 * 1024];
//                 int read;
//                 while ((read = is.read(buffer)) != -1) {
//                     os.write(buffer, 0, read);
//                 }
//                 os.flush();
//             }
//             return file.getAbsolutePath();
//         }
//     }
// }