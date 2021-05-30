package com.mandi.intelimeditor.common.util;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Xfermode;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.util.Pair;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import androidx.exifinterface.media.ExifInterface;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.FutureTarget;
import com.mandi.intelimeditor.common.appInfo.IntelImEditApplication;
import com.mandi.intelimeditor.common.dataAndLogic.AllData;
import com.mandi.intelimeditor.common.util.geoutil.MRect;
import com.mandi.intelimeditor.ptu.draw.MPaint;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * 用于获取图片的bitmap，并且将其缩放到和合适的大小
 *
 * @author acm_lgc
 * @link a.
 */
public class BitmapUtil {
    public static final String PIC_SUFFIX_PNG = ".png";
    public static final String PIC_SUFFIX_JPG = ".jpg";
    public static final String PIC_SUFFIX_webp = ".webp";
    public static final String DEFAULT_SAVE_SUFFIX = PIC_SUFFIX_PNG;
    public static final Bitmap.CompressFormat DEFAULT_COMPRESS_FORMAT = Bitmap.CompressFormat.PNG;
    private static Paint sBmPaint;
    private static PorterDuffXfermode sClearfermode;

    public static class SaveResult {
        public static final String SAVE_RESULT_FAILED = "failed";
        public static final String SAVE_RESULT_SUCCESS = "success";
        public static final String SAVE_RESULT_CHANGE_SUFFIX = "change_suffix";
        public String result;
        public String data;

        public SaveResult(String saveResult, String data) {
            result = saveResult;
            this.data = data;
        }
    }

    public static Point getBmWH(String path) {
        BitmapFactory.Options optsa = new BitmapFactory.Options();
        optsa.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, optsa);
        int width = optsa.outWidth, height = optsa.outHeight;
        int rotateDegree = getSrcBmRotateDegree(path); // 发生了旋转的图片
        if (rotateDegree == 90 || rotateDegree == 270) {
            return new Point(height, width);
        }
        return new Point(width, height);
    }

    // TODO: 2021/5/23 这个方法可以和其它地方类似方法的合并优化

    /**
     * 注意，不能在主线程调用
     *
     * @param decodeSize {@link #decodeLossslessInSize(String, int)}
     * @param emitter    不会调用onComplete();
     */
    @WorkerThread
    public static void decodeFromObj(Object obj, ObservableEmitter<Bitmap> emitter, int decodeSize) {
        if (obj instanceof Bitmap) {
            emitter.onNext((Bitmap) obj);
        } else if (obj instanceof String) {
            String path = (String) obj;
            if (FileTool.urlType(path).equals(FileTool.UrlType.URL)) { // 如果是URL
                FutureTarget<File> future = Glide.with(IntelImEditApplication.appContext)
                        .asFile()
                        .load(obj)
                        .submit();
                try {
                    String inner_path = future.get().getAbsolutePath();
                    Bitmap bitmap = BitmapUtil.decodeLossslessInSize(inner_path, decodeSize);
                    if (bitmap != null) {
                        emitter.onNext(bitmap);
                    } else {
                        emitter.onError(new Throwable("从url解析图片错误"));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    emitter.onError(new Exception(""));
                }
            } else { // 正常路径
                Bitmap bitmap = BitmapUtil.decodeLossslessInSize(path, decodeSize);
                if (bitmap != null) {
                    emitter.onNext(bitmap);
                } else {
                    emitter.onError(new Throwable("从路径解析图片错误"));
                }
            }
        } else {
            emitter.onError(new Throwable("不支持的图片解析对象，支持url， path，bm对象"));
        }
    }


    /**
     * 获取glide加载之后图片的绝对路径
     */
    public static void getBmPathInGlide(Object obj, DecodeCallback callback) {
        // TODO: 2021/5/23 可以和decodeFromObj合并一下
        if (obj == null || callback == null) return;
        // glide似乎不能通通过上面的方式获取本机图片的路径，只能直接用
        Observable.create((ObservableOnSubscribe<String>) emitter -> {
            FutureTarget<File> future = Glide.with(IntelImEditApplication.appContext)
                    .asFile()
                    .load(obj)
                    .submit();
            try {
                String path = future.get().getAbsolutePath();
                emitter.onNext(path);
                emitter.onComplete();
            } catch (Exception e) {
                LogUtil.e("获取贴图本地路径出错: " + e.getMessage());
                emitter.onError(e);
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<String>() {

                    @Override
                    public void onNext(String path) {
                        callback.result(path, null);
                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                        callback.result(null, e.getMessage());
                    }
                });
    }


    /**
     * @see #decodeLossslessInSize(String, int, Bitmap.Config)
     */
    public static @Nullable
    Bitmap decodeLossslessInSize(String path, int needSize) {
        return decodeLossslessInSize(path, needSize, Bitmap.Config.ARGB_8888);
    }

    /**
     * 解析出指定大小的图片 ，返回其Bitmap对象
     *
     * @param path     String 图片，
     * @param needSize 需要的size = w * h, <= 0表示原始尺寸
     * @param config   格式配置
     * @return Bitmap 路径下适应大小的图片
     */
    @Nullable
    public static Bitmap decodeLossslessInSize(String path, int needSize, Bitmap.Config config) {
        BitmapFactory.Options optsa = getLosslessOptions();
        optsa.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, optsa);
        float width = optsa.outWidth, height = optsa.outHeight;

        optsa.inJustDecodeBounds = false;
        /** 不同尺寸图片的缩放比例 */
        if (needSize <= 0) {
            needSize = (int) (height * width);
        }
        optsa.inSampleSize = (int) Math.ceil(Math.sqrt(height * width / needSize));
        if (optsa.inSampleSize < 1) { // 如果小于1，系统会当成1，这里直接变成1，避免后面处理出问题
            optsa.inSampleSize = 1;
        }
        optsa.inPreferredConfig = config;
        optsa.inDither = true;

        int degree = getSrcBmRotateDegree(path);
        return decodeBitmap(path, optsa, degree);
    }

    @Nullable
    public static Bitmap decodeBitmap(String path, BitmapFactory.Options options) {
        int degree = getSrcBmRotateDegree(path);  // 有些图片有旋转角度，必须转过来
        return decodeBitmap(path, options, degree);
    }

    public static int decodeBmSize(String path) {
        BitmapFactory.Options optsa = new BitmapFactory.Options();
        optsa.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, optsa);
        int width = optsa.outWidth, height = optsa.outHeight;
        return width * height;
    }

    /**
     * 将整个图片获取出来，不损失精度
     */
    public static @Nullable
    Bitmap decodeLosslessBitmap(String path) {
        BitmapFactory.Options optsa = getLosslessOptions();
        int degree = getSrcBmRotateDegree(path);  // 有些图片有旋转角度，必须转过来
        return decodeBitmap(path, optsa, degree);
    }

    public static @Nullable
    Bitmap decodeLosslessBitmap(String path, int degree) {
        BitmapFactory.Options optsa = getLosslessOptions();
        return decodeBitmap(path, optsa, degree);
    }

    @Nullable
    private static Bitmap decodeBitmap(String picPath, BitmapFactory.Options optsa, int degree) {
        Bitmap bitmap = BitmapFactory.decodeFile(picPath, optsa);
        if (bitmap == null) return null;
        Bitmap originBm = bitmap;
        if (degree != 0) {
            // 旋转图片
            Matrix m = new Matrix();
            m.postRotate(degree);
            // TODO: 2019/8/16 有可能的话看一下
            // 注释上说下面这个方法创建的Bm不可更改，但用起来用时可以更改的，是否不同的机器上不一样，
            // 但是目前似乎没有收到这样的报错，
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
                    bitmap.getHeight(), m, true);
            originBm.recycle();
        }
        return bitmap;
    }

    private static BitmapFactory.Options getLosslessOptions() {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inDither = true;
        options.inPreferQualityOverSpeed = true;
        options.inDensity = 0;
        options.inTargetDensity = 0;
        options.inScaled = false;
        options.inMutable = true; // 注意是指可变
        return options;
    }

    private static int getSrcBmRotateDegree(String picPath) {
        //根据图片的filepath获取到一个ExifInterface的对象
        ExifInterface exif = null;
        File file = new File(picPath);
        if (!file.exists()) {
            //文件不存在直接退出
            return 0;
        }
        try {
            exif = new ExifInterface(picPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        int degree = 0;
        if (exif != null) {
            // 读取图片中相机方向信息
            int ori = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            degree = getExifOrientationDegrees(ori);
        }
        return degree;
    }


    /**
     * 源于glide
     * Get the # of degrees an image must be rotated to match the given exif orientation.
     *
     * @param exifOrientation The exif orientation [1-8]
     * @return the number of degrees to rotate
     */
    private static int getExifOrientationDegrees(int exifOrientation) {
        final int degreesToRotate;
        switch (exifOrientation) {
            case ExifInterface.ORIENTATION_TRANSPOSE:
            case ExifInterface.ORIENTATION_ROTATE_90:
                degreesToRotate = 90;
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
            case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                degreesToRotate = 180;
                break;
            case ExifInterface.ORIENTATION_TRANSVERSE:
            case ExifInterface.ORIENTATION_ROTATE_270:
                degreesToRotate = 270;
                break;
            default:
                degreesToRotate = 0;
                break;
        }
        return degreesToRotate;
    }


    private static @Nullable
    Bitmap decodeResourceBm(Integer id) {
        if (id == null) {
            return null;
        }
        return BitmapFactory.decodeResource(IntelImEditApplication.appContext.getResources(), id);

    }

    /**
     * 不要要修改它，共用的画笔，避免混乱
     * 获取一支专门用于画bitmap的画笔
     */
    public static Paint getBitmapPaint() {
        if (sBmPaint == null) { // 可能经常用到，用一个全局的
            sBmPaint = new Paint();
        }
        sBmPaint.reset();
        sBmPaint.setAntiAlias(true);
        sBmPaint.setDither(true);
        sBmPaint.setFilterBitmap(true);
        return sBmPaint;
    }

    public static Xfermode getClearXfermode() {
        if (sClearfermode == null) sClearfermode = new PorterDuffXfermode(PorterDuff.Mode.CLEAR);
        return sClearfermode;
    }

    public static Canvas getBitmapCanvas(Bitmap resultBm) {
        Canvas canvas = new Canvas(resultBm);
        canvas.setDrawFilter(new PaintFlagsDrawFilter(0,
                Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG | Paint.DITHER_FLAG));
        return canvas;
    }

    /**
     * 异步保存一个bitmap到临时文件夹下
     *
     * @param bitmap   要保存的bitmap
     * @param observer 结果监听器
     */
    public static void asySaveTempBm(final String tempPath, final Bitmap bitmap, Observer<String> observer) {
        asySaveTempBm(tempPath, bitmap, null, observer);
    }

    /**
     * 异步保存一个bitmap到临时文件夹下
     *
     * @param bitmap   要保存的bitmap
     * @param observer 结果监听器
     */
    public static void asySaveTempBm(final String tempPath, final Bitmap bitmap, String suffix, Observer<String> observer) {
        //处理图片
        Observable
                .create(
                        (ObservableOnSubscribe<String>) emitter -> {
                            if (tempPath == null) {
                                emitter.onError(new Throwable("创建文件件失败"));
                                return;
                            }
                            SaveResult result = saveBitmap(AllData.appContext, bitmap, tempPath, false, suffix);
                            if (SaveResult.SAVE_RESULT_FAILED.equals(result.result)) {
                                emitter.onError(new Throwable("创建文件件失败"));
                            } else {
                                emitter.onNext(result.data);
                                emitter.onComplete();
                            }
                        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer);

    }

    /**
     * 保存bitmap到指定的路径
     *
     * @param path 路径必须不存在，存在时会覆盖文件，返回失败
     * @return 返回字符串代表不同的状态，成功是是返回"创建成功"四个字
     */
    public static SaveResult saveBitmap(Context context, Bitmap bitmap, String path) {
        return saveBitmap(context, bitmap, path, true);
    }


    /**
     * @param isSendBroad 默认为false
     * @return 结果 成功为 "success"
     */
    public static SaveResult saveBitmap(Context context, Bitmap bitmap, String path, boolean isSendBroad) {
        String suffix = path.substring(path.lastIndexOf("."));
        return saveBitmap(context, bitmap, path, isSendBroad, suffix);
    }

    /**
     * @param suffix 指定后缀,通过指定后缀的方式指定图片存储格式，
     *               为null使用原来的后缀，
     *               不管指定的后缀无效还是者原来的后缀无效，若无效，则使用默认后缀
     *               <p>
     *               另外注意，如果解析出这个bm的原图没有透明度，即使指定为PNG的后缀，且bm的格式是8888，
     *               也不能让保存的图片支持透明度，这种情况目前找到的解决办法，只能新建一个8888的bm，然后再画上去
     *               不能使用copy， copy出的8888也无效
     */
    public static SaveResult saveBitmap(Context context, Bitmap bitmap, String path, boolean isSendBroad, String suffix) {
        String res = SaveResult.SAVE_RESULT_SUCCESS;
        if (bitmap == null) return new SaveResult(SaveResult.SAVE_RESULT_FAILED, "要保存的图片为空");
        if (suffix == null) {
            suffix = path.substring(path.lastIndexOf("."));
        } else if (!path.endsWith(suffix)) { // 给定的后缀与原来路径的后缀不一致
            res = SaveResult.SAVE_RESULT_CHANGE_SUFFIX;
        }
        // 解析出这个bm的原图没有透明度，即使bm的格式是8888且指定压缩格式为PNG
        // 也不能让保存的图片支持透明度，这种情况目前找到的解决办法，只能新建一个8888的bm，然后再画上去
        if (PIC_SUFFIX_PNG.equals(suffix)) {
            Bitmap temp = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
            new Canvas(temp).drawBitmap(bitmap, 0, 0, null);
            bitmap = temp;
        }
        Bitmap.CompressFormat bmc;
        switch (suffix) {
            case ".jpg":
            case ".jpeg":
                bmc = Bitmap.CompressFormat.JPEG;
                break;
            case ".png":
                bmc = Bitmap.CompressFormat.PNG;
                break;
            case ".webp":
                bmc = Bitmap.CompressFormat.WEBP;
                break;
            default:
                suffix = DEFAULT_SAVE_SUFFIX;
                bmc = DEFAULT_COMPRESS_FORMAT;
                res = SaveResult.SAVE_RESULT_CHANGE_SUFFIX; // 这里不一定的，会有问题的

                break;
        }
        path = path.substring(0, path.lastIndexOf('.')) + suffix;

        FileOutputStream fo = null;
        try {
            File file = new File(path);
            if (file.exists())//如果文件已存在
            {
                file.delete();
            } else file.createNewFile();
            fo = new FileOutputStream(path);
            bitmap.compress(bmc, 100, fo);
            fo.flush();
            if (isSendBroad) {
                //发送添加图片的广播
                Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                Uri uri = Uri.fromFile(file);
                intent.setData(uri);
                context.sendBroadcast(intent);
            }
        } catch (SecurityException se) {
            return new SaveResult(SaveResult.SAVE_RESULT_FAILED, "安全权限禁止" + se.getMessage());
        } catch (FileNotFoundException e) {
            return new SaveResult(SaveResult.SAVE_RESULT_FAILED, e.getMessage());
        } catch (IOException e) {
            return new SaveResult(SaveResult.SAVE_RESULT_FAILED, e.getMessage());
        } finally {
            if (fo != null)
                try {
                    fo.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
        return new SaveResult(res, path);
    }

    /**
     * 根据版本不同获取的方式去就不同，有些坑
     */
    public static long getSize(Bitmap bitmap) {
        if (bitmap == null) return 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)   //API 19
        {
            return bitmap.getAllocationByteCount();
        }
        return bitmap.getByteCount();
    }

    /**
     * 缩放图片，让图片尺寸最大不超过xx
     */
    public static Bitmap scall2NomoreThan(Bitmap bm, int exceptLen) {
        int sourceWidth = bm.getWidth(), sourceHeight = bm.getHeight();
        int exceptWidth = sourceWidth, exceptHeight = sourceHeight;
        if (sourceWidth > sourceHeight && sourceWidth > exceptLen) {
            exceptWidth = exceptLen;
            exceptHeight = Math.round(sourceHeight * ((float) exceptLen / (float) sourceWidth));
        } else if (sourceHeight > sourceWidth && sourceHeight > exceptLen) {
            exceptHeight = exceptLen;
            exceptWidth = Math.round(sourceWidth * ((float) exceptLen / (float) sourceHeight));
        }
        return Bitmap.createScaledBitmap(bm, exceptWidth, exceptHeight, true);
    }


    /**
     * 在Bitmap中扣取Path所围成的闭合路径的图形的图，会裁剪抠图后多余的部分
     *
     * @param path      注意Path的范围要在图片内，并且Path要闭合
     * @param pathBound 用于获取，路径代表的碎片在src Bitmap中的位置
     */
    public static @NonNull
    Bitmap digBitmap(Bitmap src, Path path, @Nullable MRect pathBound) {
        return digBitmap(src, path, pathBound, false, -1, -1);
    }

    /**
     * 在Bitmap中扣取Path所围成的闭合路径的图形的图，会裁剪抠图后多余的部分
     *
     * @param outPath    注意Path的范围要在图片内，并且Path要闭合, 方法里面不会改变这个Path
     * @param pathBound  用于获取，路径代表的碎片在src Bitmap中的位置
     * @param blurRadius 有模糊效果时，这个必须设置，否则裁剪无法判断模糊边界，调用模糊边界被裁剪掉
     */
    public static Bitmap digBitmap(Bitmap src, @NotNull Path outPath, @Nullable MRect pathBound,
                                   boolean isRound, float paintWidth, float blurRadius) {
        // 获取Path的矩形范围，
        if (pathBound == null) pathBound = new MRect();

        Path path = new Path(outPath);
        path.computeBounds(pathBound, true);
        if (blurRadius > 0) {
            pathBound.expand(Math.ceil(blurRadius + paintWidth));
        }
        MRect bmRect = new MRect(0, 0, src.getWidth(), src.getHeight());
        if (!bmRect.contains(pathBound)) {
            LogUtil.e("裁剪路径范围超过src Bitmap，取范围内的路径");
            pathBound.shrinkInB(bmRect);
        }
        src = Bitmap.createBitmap(src, pathBound.leftInt(), pathBound.topInt(), pathBound.widthInt(), pathBound.heightInt());

        // 路径也进行位移
        Matrix matrix = new Matrix();
        matrix.setTranslate(-pathBound.left, -pathBound.top);
        path.transform(matrix);  // 注意这个技巧，多个点的平行运算，矩阵就来了

        // 参数处理
        Paint paint = getBitmapPaint();
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        if (blurRadius > 0) {
            paint.setMaskFilter(new BlurMaskFilter(blurRadius, BlurMaskFilter.Blur.NORMAL));
        }

        if (paintWidth > 0) {
            paint.setStrokeWidth(paintWidth);
        }

        if (isRound) {
            paint.setStrokeCap(Paint.Cap.ROUND);
            paint.setStrokeJoin(Paint.Join.ROUND);
        }

        return digBitmap(src, path, paint);
    }


    /**
     * 抠图，不裁剪
     * {@link #digBitmap(Bitmap, Path, MRect)}
     */
    public static Bitmap digBitmap(Bitmap src, Path path, Paint paint) {
        int w = src.getWidth(), h = src.getHeight();
        Bitmap pathBm = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(pathBm);

        //  使用离屏绘制,不能直接绘制到屏幕上
        int layerID = canvas.saveLayer(0, 0, w, h, null, Canvas.ALL_SAVE_FLAG);

        canvas.drawPath(path, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(src, 0, 0, paint);
        paint.setXfermode(null);
        canvas.save();
        canvas.restoreToCount(layerID);

        return pathBm;
    }

    public static Bitmap eraseBmByPath(Bitmap originalBm, List<Pair<Path, MPaint>> operateList, boolean isForceCreateBm) {
        if (isForceCreateBm || !originalBm.isMutable()) {
            originalBm = originalBm.copy(Bitmap.Config.ARGB_8888, true);
        }
        Canvas canvas = new Canvas(originalBm);
        for (Pair<Path, MPaint> sd : operateList) {
            drawTransparencyInCanvas(canvas, sd.first, sd.second);
        }
        return originalBm;
    }

    /**
     * 在图片里面画上透明颜色，就是把路径中的图片擦除掉
     *
     * @return
     */
    public static @NonNull
    Bitmap drawTransparencyInBm(Bitmap src, @NotNull Path srcPath,
                                boolean isRound, float paintWidth, float blurRadius) {
        // 参数处理
        Paint temp = getBitmapPaint();
        MPaint mPaint = new MPaint(temp);
        mPaint.setStyle(Paint.Style.STROKE);
        if (blurRadius > 0) {
            mPaint.setMaskFilter(new BlurMaskFilter(blurRadius, BlurMaskFilter.Blur.NORMAL));
        }

        if (paintWidth > 0) {
            mPaint.setStrokeWidth(paintWidth);
        }

        if (isRound) {
            mPaint.setStrokeCap(Paint.Cap.ROUND);
            mPaint.setStrokeJoin(Paint.Join.ROUND);
        }
        return drawTransparencyInBm(src, srcPath, mPaint);
    }

    /**
     * @param paint 用于设置除透明效果外的其它参数，比如宽度
     * @return 注意可能绘制在原来的图上，也可能新建了图（对于不可修改的bm)，没有绘制在原来的图上，原因在于原来的图不符合绘制要求等
     */
    public static Bitmap drawTransparencyInBm(Bitmap src, @NotNull Path srcPath, Paint paint) {
        int w = src.getWidth(), h = src.getHeight();
        if (src.getConfig() != Bitmap.Config.ARGB_8888 || !src.isMutable()) { // 变成支持透明度的bm, 注意图片不可修改
            Bitmap tempBm = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            new Canvas(tempBm).drawBitmap(src, 0, 0, getBitmapPaint());
            src = tempBm;
        }
        Canvas canvas = new Canvas(src);
        drawTransparencyInCanvas(canvas, srcPath, paint);
        return src;
    }

    private static void drawTransparencyInCanvas(Canvas canvas, Path srcPath, Paint paint) {
        // 必须设置成透明颜色
        paint.setColor(Color.TRANSPARENT);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        canvas.drawPath(srcPath, paint);
        paint.setXfermode(null);
    }


    /**
     * 翻转图片
     *
     * @param reverseDic 翻转方向 0 水平 1 竖直
     */
    public static Bitmap flip(@NotNull Bitmap bm, int reverseDic) {
        Matrix matrix = new Matrix();
        if (reverseDic == 0) {
            matrix.postScale(-1, 1);
        } else {
            matrix.postScale(1, -1);
        }
        return Bitmap.createBitmap(bm,
                0, 0, bm.getWidth(), bm.getHeight(),
                matrix, true);
    }

    public interface DecodeCallback {
        void result(String path, @Nullable String msg);
    }

    @Nullable
    public static Rect getBmRect(Bitmap bgBm) {
        if (bgBm == null || bgBm.isRecycled()) return null;
        return new Rect(0, 0, bgBm.getWidth(), bgBm.getHeight());
    }

    public static byte[] bmpToByteArray(final Bitmap bmp, Bitmap.CompressFormat compressFormat, final boolean needRecycle) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        bmp.compress(compressFormat, 100, output);
        if (needRecycle) {
            bmp.recycle();
        }

        byte[] result = output.toByteArray();
        try {
            output.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    /**
     * 注意，bitmapFactory不能直接解析矢量图vector
     */
    public static Bitmap decodeVector(int vectorDrawableId) {
        Bitmap bitmap = null;
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            Drawable vectorDrawable = IntelImEditApplication.appContext.getDrawable(vectorDrawableId);
            if (vectorDrawable == null)
                return null;
            bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(),
                    vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            vectorDrawable.draw(canvas);
        } else {
            bitmap = BitmapFactory.decodeResource(IntelImEditApplication.appContext.getResources(), vectorDrawableId);
        }
        return bitmap;

    }

    /**
     * 用bitmap将颜色显示出来，用于调试
     */
    public static Bitmap showColor(int color) {
        Bitmap bitmap = Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888);
        bitmap.eraseColor(color);
        return bitmap;
    }
    /**
     * 快速访问bm的像素，但是需要消耗2倍内存
     */
    public static class BitmapPixelsConverter {
        private Bitmap image;
        private int width, height;
        private int[] pixels;

        public BitmapPixelsConverter(Bitmap _source) {
            image = _source;
            width = image.getWidth();
            height = image.getHeight();
            pixels = new int[width * height];
            image.getPixels(pixels, 0, width, 0, 0, width, height);
        }

        public BitmapPixelsConverter(int width, int height) {
            this.width = width;
            this.height = height;
            image = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            pixels = new int[width * height];
        }

        public int getPixel(int x, int y) {
            return pixels[x + y * width];
        }

        public void setPixel(int x, int y, int color) {
            pixels[x + y * width] = color;
            // if ((x + y * width) % 50000 == 0) {
            //     LogUtil.d("位置: " + (x + y * width) + " 设置像素 = " + color);
            // }
        }

        public Bitmap getBimap() {
            image.setPixels(pixels, 0, width, 0, 0, width, height);
            return image;
        }

        public int getWidth() {
            return image.getWidth();
        }

        public int getHeight() {
            return image.getHeight();
        }
    }
}