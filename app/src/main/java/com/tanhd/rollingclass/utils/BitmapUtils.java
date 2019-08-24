package com.tanhd.rollingclass.utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.SystemClock;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class BitmapUtils {

    private static final String TAG = "BitmapUtils";

    public static Bitmap getCircleBitmap(Bitmap bitmap, int width, int height, float radius, Config config) {

        Bitmap output = Bitmap.createBitmap(width, height, config);
        Canvas canvas = new Canvas(output);

        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(0, 0, width, height);

        paint.setAntiAlias(true);
        canvas.drawCircle(width / 2, height / 2, radius, paint);

        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));

        canvas.drawBitmap(bitmap, rect, rectF, paint);
        return output;
    }

    public static Bitmap getScaleBitmap(Bitmap bitmap, float scale) {
        return getScaleBitmap(bitmap, scale, Config.ARGB_8888);
    }

    /**
     * 得到缩放后的图片
     */
    public static Bitmap getScaleBitmap(Bitmap bitmap, float scale, Config config) {
        if (bitmap == null) {
            return null;
        }
        int width = (int) (bitmap.getWidth() * scale);
        int height = (int) (bitmap.getHeight() * scale);

        Bitmap output = Bitmap.createBitmap(width, height, config);
        Canvas canvas = new Canvas(output);

        final Paint paint = new Paint();
        paint.setAntiAlias(true);
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(0, 0, width, height);

        canvas.drawBitmap(bitmap, rect, rectF, paint);
        return output;
    }

    /**
     *
     * @param inStream
     * @throws IOException
     * @return
     */
    public static byte[] readStream(InputStream inStream) throws IOException {

        long t = SystemClock.elapsedRealtime();

        ByteArrayOutputStream outstream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024]; // 用数据装
        int len = -1;
        while ((len = inStream.read(buffer)) != -1) {
            outstream.write(buffer, 0, len);
        }
        outstream.close();

        return outstream.toByteArray();
    }

    /**
     * 得到手机data目录下的图片
     */
    public static Bitmap getBmpFromFile(Context context, String fileName) {
        try {
            Options opts = new Options();
            opts.inPreferredConfig = Config.RGB_565;
            opts.inJustDecodeBounds = false;
            FileInputStream imgInputStream = context.openFileInput(fileName);
            Bitmap bmp = BitmapFactory.decodeStream(imgInputStream, null, opts);
            // Bitmap bmp = BitmapFactory.decodeStream(imgInputStream);
            return bmp;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Bitmap getBmpFromFile(String filePath, int bmpWidth, Config config) throws Exception {
        return getBmpFromFile(filePath, bmpWidth, config, false);
    }

    public static Bitmap getBmpFromFile(String filePath, int bmpWidth, Config config, boolean isStrengthen) throws Exception {
        return getBmpFromFile(filePath, bmpWidth, config, isStrengthen, true);
    }

    /**
     * TODO
     *
     * @param bmpWidth -1时候不特殊处理图片
     */
    public static Bitmap getBmpFromFile(String filePath, int bmpWidth, Config config,
                                        boolean isStrengthen, boolean isCorrectWidth) throws Exception {

        if (filePath == null) {
            return null;
        }
        File file = new File(filePath);
        if (!file.exists()) {
            return null;
        }
        Options opts = new Options();
        Bitmap bmp = null;
        if (bmpWidth != -1) {
            opts.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(filePath, opts);
            int scaleWidth = opts.outWidth;
            int scaleHeight = opts.outHeight;
//			Log.i("load", "--trueWidth:" + scaleWidth + "| trueHeight:" + opts.outHeight + "|bmpWidth:" + bmpWidth);

            if (isStrengthen) {
                if (scaleWidth > scaleHeight) {
                    int sampleSize = 1;
                    if (scaleWidth > bmpWidth) {
                        int scale = scaleWidth / bmpWidth;
                        if (scale > 1) {
                            sampleSize = scale;
                        }
                    }
                    opts.inSampleSize = sampleSize;
                } else {
                    int sampleSize = 1;
                    if (scaleHeight > bmpWidth) {
                        int scale = scaleHeight / bmpWidth;
                        if (scale > 1) {
                            sampleSize = scale;
                        }
                    }
                    opts.inSampleSize = sampleSize;
                }
            } else {
                int sampleSize = 1;
                if (scaleWidth > bmpWidth) {
                    int scale = scaleWidth / bmpWidth;
                    if (scale > 1) {
                        sampleSize = scale;
                    }
                }
                opts.inSampleSize = sampleSize;
            }

            //Log.d("zz", "SampleSize的大小：" + opts.inSampleSize);
            opts.inPreferredConfig = config;
            opts.inJustDecodeBounds = false;
            bmp = BitmapFactory.decodeFile(filePath, opts);

            if (!isCorrectWidth) {
                return bmp;
            }

            if (bmp == null) {
                Log.v("debug", "getBmpFromFile(scaleSize)is bykk");
                return null;
            }
            //Log.v("zz", "fileScale: " + opts.inSampleSize + " -- opts.outWidth:" + opts.outWidth + "|outHeight:"
            //		+ opts.outHeight + "|bmpWidth:" + bmp.getWidth() + "|bmpHeight:" + bmp.getHeight());

            int width = bmp.getWidth();
            if (width > bmpWidth) {

                float scaleBmp = (float) bmpWidth / width;
                Bitmap scaleBitmap = getScaleBitmap(bmp, scaleBmp, Config.ARGB_8888);
                //Log.v("debug", "scaleBmp:" + scaleBmp);

                bmp.recycle();

                Log.v("debug", "getBmpFromFile(scaleSize 2)is " + bmp);
                return scaleBitmap;
            }
        } else {
            opts.inPreferredConfig = config;
            opts.inJustDecodeBounds = false;
            bmp = BitmapFactory.decodeFile(filePath, opts);

            Log.v("debug", "getBmpFromFile(org)is " + bmp);
        }
        return bmp;
    }

    public static Bitmap getBitmap(String filePath) {
        return getBitmap(filePath, null);
    }

    public static Bitmap getBitmap(String filePath, Config config) {
        if (filePath != null) {
            File file = new File(filePath);
            if (file.exists()) {
                if (config == null) {
                    return BitmapFactory.decodeFile(filePath);
                } else {
                    Options opts = new Options();
                    opts.inPreferredConfig = config;
                    return BitmapFactory.decodeFile(filePath, opts);
                }
            }
        }
        return null;
    }

    /**
     * 得到缩小的图片，这里缩小的是图片质量
     */
    public static Bitmap getCorrectBmp(byte dataBytes[], int bmpWidth, Config config) {

        Options opts = new Options();
        opts.inPreferredConfig = Config.ALPHA_8;
        opts.inJustDecodeBounds = true;
        Bitmap bmp = BitmapFactory.decodeByteArray(dataBytes, 0, dataBytes.length, opts);
        //Log.v("debug", "opts.outWidth:" + opts.outWidth);
        int scaleWidth = opts.outWidth;
        if (opts.outWidth < opts.outHeight) {
            scaleWidth = opts.outHeight;
        }

        int scale = scaleWidth / bmpWidth;
        if (scale > 1) {
            opts.inSampleSize = scale;
        } else if (scale <= 0 && opts.inSampleSize <= 0) {
            opts.inSampleSize = 1;
        }
        opts.inPreferredConfig = config;
        opts.inJustDecodeBounds = false;
        try {
            bmp = BitmapFactory.decodeByteArray(dataBytes, 0, dataBytes.length, opts);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (bmp != null) {
            if (bmp.getWidth() < bmpWidth) {

                float scaleBmp = (float) bmpWidth / bmp.getWidth();
                //Log.v("debug", "scaleBmp:" + scaleBmp);
                Bitmap scaleBitmap = getScaleBitmap(bmp, scaleBmp, Config.ARGB_8888);
                bmp.recycle();
                return scaleBitmap;
            }
        }

        return bmp;
    }

    /**
     * 保存图片到指定位置
     */
    public static boolean saveBmpToJpg(Bitmap bmp, String filePath, int quality) {
        if (filePath == null || "".equals(filePath)) {
            return false;
        }

        int index = filePath.lastIndexOf("/");
        if (index == -1) {
            return false;
        }

        String prePath = filePath.substring(0, index);

        try {
            File dir = new File(prePath);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            File file = new File(filePath);
            if (file.exists()) {
                file.delete();
            }
            file.createNewFile();

            FileOutputStream fileOut = new FileOutputStream(filePath, true);
            bmp.compress(Bitmap.CompressFormat.JPEG, quality, fileOut);
            fileOut.flush();
            fileOut.close();

            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * fuction: 设置固定的宽度，高度随之变化，使图片不会变形
     *
     * @param target 需要转化bitmap参数
     * @param newWidth 设置新的宽度
     */
    private static Bitmap fitBitmap(Bitmap target, int newWidth) {
        if (target == null) {
            return null;
        }

        int width = target.getWidth();
        int height = target.getHeight();
        Matrix matrix = new Matrix();
        float scaleWidth = ((float) newWidth) / width;
        matrix.postScale(scaleWidth, scaleWidth);
        Bitmap bmp = Bitmap.createBitmap(target, 0, 0, width, height, matrix,
                true);
        if (!target.equals(bmp) && !target.isRecycled()) {
            target.recycle();
            target = null;
        }
        return bmp;
    }

    private static int calculateInSampleSize(BitmapFactory.Options options,
                                             int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    // 如果是放大图片，filter决定是否平滑，如果是缩小图片，filter无影响
    private static Bitmap createScaleBitmap(Bitmap src, int dstWidth,
                                            int dstHeight) {
        Bitmap dst = Bitmap.createScaledBitmap(src, dstWidth, dstHeight, false);
        if (src != dst) { // 如果没有缩放，那么不回收
            src.recycle(); // 释放Bitmap的native像素数组
        }
        return dst;
    }

    // 从Resources中加载图片
    public static Bitmap decodeSampledBitmapFromResource(Resources res,
                                                         int resId, int reqWidth, int reqHeight) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options); // 读取图片长款
        options.inSampleSize = calculateInSampleSize(options, reqWidth,
                reqHeight); // 计算inSampleSize
        options.inJustDecodeBounds = false;
        Bitmap src = BitmapFactory.decodeResource(res, resId, options); // 载入一个稍大的缩略图
        return createScaleBitmap(src, reqWidth, reqHeight); // 进一步得到目标大小的缩略图
    }

    // 从sd卡上加载图片
    public static Bitmap decodeSampledBitmapFromFd(String pathName,
                                                   int reqWidth, int reqHeight) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(pathName, options);
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        options.inJustDecodeBounds = false;
        Bitmap src = BitmapFactory.decodeFile(pathName, options);
        return createScaleBitmap(src, reqWidth, reqHeight);
    }
}
