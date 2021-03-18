package com.mandi.intelimeditor.common.util;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;

import com.mandi.intelimeditor.common.dataAndLogic.AllData;
import com.mandi.intelimeditor.home.data.MediaInfoScanner;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * 主要用于获取图片文件夹下的所有图片的路径，图片大小支取5k-6000k的见方法注解
 *
 * @author acm_lgc
 * @version jdk 1.8, sdk 21
 */
public class FileTool {
    private final static String TAG = "FileTool";
    private static long lastTempTime = System.currentTimeMillis();

    /**
     * @return 包括点
     */
    @Nullable
    public static String getSuffix(String path) {
        if (path == null) return null;
        int dotId = path.lastIndexOf(".");
        if (0 <= dotId && dotId < path.length() - 1) { // 有后缀
            return path.substring(dotId);
        }
        return null;
    }

    public enum UrlType {
        OTHERS,
        URL
    }

    /**
     * 遍历目录,得到所有图片的路径，图片大小在5-6000k之间，注意判空
     * 获取到的数据会直接往添加到第二个参数末尾添加
     *
     * @param dirPath            String 要显示的图片文件夹的路径，
     * @param willOrderedPicList List《String》括号用不了？？？ , 返回的结构， 文件夹下及子文件夹下所有符合条件的图片的路径,
     *                           因为要得到子文件夹路径下的图片路径，所以使用传参赋值更好
     */
    public static void getOrderedPicListInFile(String dirPath, List<String> willOrderedPicList) {
        List<Pair<Long, String>> oderedPaths = new ArrayList<>();

        File file = new File(dirPath);
        if (!file.exists() || !file.isDirectory())
            return;

        File[] fs = file.listFiles();
        if (fs == null)
            return;

        boolean isHowShortVideo = AllData.globalSettings.isShowShortVideo();
        for (File f : fs) {
            if (f == null)
                continue;
            String fPath = f.getAbsolutePath();
            String fName = f.getName(); // 文件名
            String htx = fName.substring(fName.lastIndexOf(".") + 1).toLowerCase(); // 得到扩展名,用求字串的方法

            // 图片
            for (String s : AllData.normalPictureFormat) {
                if (htx.equals(s)) {
                    if (fileSizeValidity(fPath)) {
                        oderedPaths.add(new Pair<>(-f.lastModified(), fPath));
                    }
                    break;
                }
            }
            // 短视频
            if (isHowShortVideo) {
                if (MediaInfoScanner.getInstance().isShortVideo(fPath)) {
                    oderedPaths.add(new Pair<>(-f.lastModified(), fPath));
                }
            }
        }
        Collections.sort(oderedPaths, new Comparator<Pair<Long, String>>() {
            public int compare(Pair<Long, String> o1, Pair<Long, String> o2) {
                //return (o2.getValue() - o1.getValue());
                return o1.first.compareTo(o2.first);
            }
        });
        for (Pair<Long, String> p : oderedPaths)
            willOrderedPicList.add(p.second);
    }

    /**
     * 判断文件大小有效性 如果文件内存在5K-6000K之间则有效，否则返回FALSE
     *
     * @param path 图片的路径
     * @return 图片大小是否符合要求
     */
    private static boolean fileSizeValidity(String path) {
        File f = new File(path);
        //path 有可能是对应文件夹
        if (f.exists() && !f.isDirectory()) {
            int cur = 0;
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(f);

                cur = fis.available();

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (fis != null)
                        fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (cur >= AllData.PIC_FILE_SIZE_MIN && cur <= AllData.PIC_FILE_SIZE_MAX)
                return true;
        }
        return false;
    }

    public static String getFileNameInPath(String path) {
        String name = null;
        try {
            name = path.substring(path.lastIndexOf("/") + 1, path.length());
        } catch (Exception e) {
            // nothing
        }
        return name;
    }

    public static String getApplicationDir(Context context) {
        return context.getApplicationContext().getFilesDir().getAbsolutePath();
    }

    /**
     * @link #createTempPicPath(Context)
     */
    public static String createTempPicPath() {
        return createTempPicPath(AllData.appContext);
    }

    /**
     * @param context
     * @return 后缀是PNG，表示希望PNG格式存储
     */
    public static String createTempPicPath(Context context) {
        String appPath = getApplicationDir(context);
        String tempDir = appPath + "/tempPic";
        File file = new File(tempDir);
        if (!file.exists()) {
            {
                if (!file.mkdirs()) {
                    Log.e("FileTool", "createTempPicPath: 创建文件失败");
                    return null;
                }
            }
        }
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd_HHmmss");
        Date curDate = new Date(lastTempTime++);//获取当前时间
        String time = formatter.format(curDate);
        time += "_" + lastTempTime % 1000;
        String tempPath = tempDir + "/ptu" + time + ".png";
        return tempPath;
    }

    /**
     * 处理路径不存在的情况
     *
     * @param file 文件
     * @return 是否创建成功
     */
    public static boolean createNewFile(File file) {
        //处理目录
        File dir = file.getParentFile();
        if (!dir.exists()) {
            if (!dir.mkdirs())
                return false;
        }
        //文件
        if (!file.exists())
            try {
                return file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        return true;
    }

    /**
     * 递归删除目录下的所有文件及子目录下所有文件
     *
     * @param dir 将要删除的文件目录
     * @return boolean Returns "true" if all deletions were successful.
     * If a deletion fails, the method stops attempting to
     * delete and returns "false".
     */
    public static boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            File[] children = dir.listFiles();
            //递归删除目录中的子目录下
            for (File file : children) {
                boolean success = deleteDir(file);
                if (!success) {
                    return false;
                }
            }
        }
        // 若是目录，此时为空，可以删除
        return dir.delete();
    }

    public static long getFileSize(File dir) {
        long size = 0;
        try {
            if (dir.isDirectory()) {
                File[] children = dir.listFiles();
                //递归删除目录中的子目录下
                for (File file : children) {
                    size += getFileSize(file);
                }
            } else
                size += dir.length();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return size;
    }

    /**
     * 删除文件夹中所有存在的子文件
     */
    public static boolean deleteAllChileFile(File dir) {
        if (dir.isDirectory()) {
            File[] children = dir.listFiles();
            //递归删除目录中的子目录下
            for (File file : children) {
                boolean success = deleteDir(file);
                if (!success) return false;
            }
        }
        return true;
    }

    /**
     * 根据原来的路径创建一个新的路径和名称
     *
     * @param oldPath 以前的路径，用于获取图片后缀
     */
    public static String createPicturePathDefault(@NotNull String oldPath) {
        return createPicturePathDefault(oldPath, null);
    }

    /**
     * 要注意一点，实际用户中很多图片文件竟然没有后缀，
     *
     * @param oldPath 用于获取后缀
     * @param suffix  指定图片文件的后缀
     */
    @Nullable
    public static String createPicturePathDefault(@NotNull String oldPath, String suffix) {
        return createPicPathWithTag(oldPath, "", suffix);
    }

    public static String createPicPathWithTag(@NotNull String oldPath, String tag, String suffix) {
        if (tag == null) tag = "";
        String resPath;
        String dirPath = AllData.picDir;
        try {
            String resSuffix = suffix;
            if (resSuffix == null) { // 获取图片本来的后缀
                int dotId = oldPath.lastIndexOf(".");
                if (dotId != -1) { //
                    resSuffix = oldPath.substring(dotId);
                } else { // 实际用户中很多图片文件竟然没有后缀，此时使用默认后缀
                    resSuffix = BitmapUtil.DEFAULT_SAVE_SUFFIX;
                }
            }

            File dir = new File(dirPath);
            if (!dir.exists()) {
                boolean dirRes = dir.mkdirs(); // 可以忽略，后面保存图片失败会提醒用户
            }

            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MMdd-HHmmss", Locale.CHINA);
            Date curDate = new Date(System.currentTimeMillis());//获取当前时间
            String time = formatter.format(curDate);
            if (TextUtils.isEmpty(tag))
                resPath = dirPath + "baozou" + "-" + time + resSuffix;
            else
                resPath = dirPath + "baozou" + "-" + tag + "-" + time + resSuffix;
        } catch (SecurityException se) {
            return null;
        }
        return resPath;
    }

    /**
     * 根据Uri获取图片绝对路径，解决Android4.4以上版本Uri转换
     */

    public static @Nullable
    String getImagePathFromUri(Activity context, Uri imageUri) {
        if (context == null || imageUri == null)
            return null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT && DocumentsContract.isDocumentUri(context, imageUri)) {
            if (isExternalStorageDocument(imageUri)) {
                String docId = DocumentsContract.getDocumentId(imageUri);
                String[] split = docId.split(":");
                String type = split[0];
                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
            } else if (isDownloadsDocument(imageUri)) {
                String id = DocumentsContract.getDocumentId(imageUri);
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
                return getDataColumn(context, contentUri, null, null);
            } else if (isMediaDocument(imageUri)) {
                String docId = DocumentsContract.getDocumentId(imageUri);
                String[] split = docId.split(":");
                String type = split[0];
                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }
                String selection = MediaStore.Images.Media._ID + "=?";
                String[] selectionArgs = new String[]{split[1]};
                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        } // MediaStore (and general)
        else if ("content".equalsIgnoreCase(imageUri.getScheme())) {
            // Return the remote address
            if (isGooglePhotosUri(imageUri))
                return imageUri.getLastPathSegment();
            return getDataColumn(context, imageUri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(imageUri.getScheme())) {
            return imageUri.getPath();
        }
        return null;
    }

    public static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        String column = MediaStore.Images.Media.DATA;
        String[] projection = {column};
        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    private static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    private static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    private static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    private static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }

    public static String getParentPath(String path) {
        return path.substring(0, path.lastIndexOf('/'));
    }

    /**
     * 获取所有的子文件,不存在时创建
     */
    public static File[] getAllChildFiles(String dirPath) {
        File dir = new File(dirPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir.listFiles();
    }

    public static String replaceSuffix(String path, String suffix) {
        if (path != null && suffix != null) {
            int dotId = path.lastIndexOf('.');
            if (dotId != -1) {
                path = path.substring(0, dotId);
            }
            if (suffix.charAt(0) == '.') {
                path += suffix;
            } else {
                path += '.' + suffix;
            }
        }
        return path;
    }

    public static boolean deletePicFile(String picPath) {
        File file = new File(picPath);
        String dirPath = file.getParent();
        if (file.exists()) {
            if (!file.delete()) {
                return false;
            }
            Intent scanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            scanIntent.setData(Uri.fromFile(new File(picPath)));
            AllData.appContext.sendBroadcast(scanIntent);
            MediaScannerConnection.scanFile(AllData.appContext, new String[]{dirPath}, null, null);
        }
        return true;
    }

    /**
     * 判断Url的类型，目前只支持文件路径和一般url
     *
     * @param url
     * @return
     */
    public static UrlType urlType(@NotNull String url) {
        if (url.contains("://")) {
            return UrlType.URL;
        } else {
            return UrlType.OTHERS;
        }
    }


    /**
     * 获取SD卡缓存目录
     *
     * @param context 上下文
     * @param type    文件夹类型 如果为空则返回 /storage/emulated/0/Android/data/app_package_name/cache
     *                否则返回对应类型的文件夹如Environment.DIRECTORY_PICTURES 对应的文件夹为 .../data/app_package_name/files/Pictures
     *                {@link Environment#DIRECTORY_MUSIC},
     *                {@link Environment#DIRECTORY_PODCASTS},
     *                {@link Environment#DIRECTORY_RINGTONES},
     *                {@link Environment#DIRECTORY_ALARMS},
     *                {@link Environment#DIRECTORY_NOTIFICATIONS},
     *                {@link Environment#DIRECTORY_PICTURES}, or
     *                {@link Environment#DIRECTORY_MOVIES}.or 自定义文件夹名称
     * @return 缓存目录文件夹 或 null（无SD卡或SD卡挂载失败）
     */
    public static File getExternalCacheDirectory(Context context, String type) {
        File appCacheDir = null;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            if (TextUtils.isEmpty(type)) {
                appCacheDir = context.getExternalCacheDir();
            } else {
                appCacheDir = context.getExternalFilesDir(type);
            }

            if (appCacheDir == null) {// 有些手机需要通过自定义目录
                appCacheDir = new File(Environment.getExternalStorageDirectory(), "Android/data/" + context.getPackageName() + "/cache/" + type);
            }

            if (appCacheDir == null) {
                Log.e("getExternalDirectory", "getExternalDirectory fail ,the reason is sdCard unknown exception !");
            } else {
                if (!appCacheDir.exists() && !appCacheDir.mkdirs()) {
                    Log.e("getExternalDirectory", "getExternalDirectory fail ,the reason is make directory fail !");
                }
            }
        } else {
            Log.e("getExternalDirectory", "getExternalDirectory fail ,the reason is sdCard nonexistence or sdCard mount fail !");
        }
        return appCacheDir;
    }
}
