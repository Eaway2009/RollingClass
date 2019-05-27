package com.tanhd.rollingclass.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;

import java.io.FileDescriptor;

/**
 * Created by Sikang on 2017/12/7.
 */

public class SimpleUtils {
    /**
     * 获取指定大小图片
     */
    public static Bitmap getBitmapThumbnail(FileDescriptor fileDescriptor, float width, float height, boolean isKeepScale) {
        try {
            if (width > 0 && height > 0) {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeFileDescriptor(fileDescriptor, null, options);
                options.inSampleSize = 1;
                if (width > options.outWidth || height > options.outHeight) {
                    int widthRatio = Math.round(options.outWidth / width);
                    int heightRatio = Math.round(options.outHeight / height);
                    options.inSampleSize = widthRatio > heightRatio ? widthRatio : heightRatio;
                }
                options.inJustDecodeBounds = false;
                Bitmap bitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor, null, options);
                if (isKeepScale)
                    return bitmap;
                else
                    return resizeImage(bitmap, width, height);
            } else {
                return BitmapFactory.decodeFileDescriptor(fileDescriptor);
            }

        } catch (Exception ex) {
        }
        return null;
    }

    /**
     * 缩放图片
     */
    public static Bitmap resizeImage(Bitmap bitmap, float w, float h) {
        Bitmap BitmapOrg = bitmap;
        int width = BitmapOrg.getWidth();
        int height = BitmapOrg.getHeight();
        float scaleWidth = w / width;
        float scaleHeight = h / height;

        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        Bitmap resizedBitmap = Bitmap.createBitmap(BitmapOrg, 0, 0, width,
                height, matrix, true);
        return resizedBitmap;
    }


}
