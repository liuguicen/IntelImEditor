package com.mandi.intelimeditor.ptu.gif;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

/**
 * <pre>
 *      author : liuguicen
 *      time : 2019/04/26
 *      version : 1.0
 * <pre>
 */
public class MyGifDecoder extends StandardGifDecoder {

    public static boolean isGif(String path) {
        InputStream in = null;
        try {
            in = new FileInputStream(new File(path));
            //根据文件头判断是否GIF图片
            StringBuilder id = new StringBuilder(8);
            for (int i = 0; i < 6; i++) {
                id.append((char)in.read());
            }
            if (!id.toString().toUpperCase().startsWith("GIF")) {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            if(in != null) {
                try {
                    in.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return true;
    }

    public void stopDecode() {
        super.status = STATUS_FORMAT_ERROR;
        if (frameList != null) {
            for (GifFrame allFrame : frameList) {
                if (allFrame.bm != null) {
                    allFrame.bm.recycle();
                }
            }
        }
    }
}
