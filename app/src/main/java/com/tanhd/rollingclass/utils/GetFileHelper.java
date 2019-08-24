package com.tanhd.rollingclass.utils;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;

import java.io.File;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class GetFileHelper {

    public final static int FILE_CHOOSER_REQUEST = 1;
    public final static int REQUEST_PERMISSION = 1;
    private static final int PHONE_STATE_PERMISSION_REQUEST = 126;
    private static String[] permissions = new String[]{
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE};

    public static String imageSelector(Activity mActivity, Fragment fragment, boolean isMultiMode, boolean showCamera) {
        return openFileSelector(mActivity, fragment, "image/*", isMultiMode, showCamera);
    }

    public static String fileSelector(Activity mActivity, Fragment fragment, boolean isMultiMode, boolean showCamera) {
        return openFileSelector(mActivity, fragment, "*/*", isMultiMode, showCamera);
    }

    public static String openFileSelector(Activity activity, Fragment fragment, String fileType, boolean isMultiMode, boolean showCamera) {
        String cameraFilePath = null;
        if (showCamera) {
            final List<String> permissionsList = new ArrayList<String>();

            if (ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED)
                permissionsList.add(Manifest.permission.READ_EXTERNAL_STORAGE);

            if (ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED)
                permissionsList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);

            if (ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED)
                permissionsList.add(Manifest.permission.CAMERA);

            if (permissionsList.size() == 0) {

            } else {
                ActivityCompat.requestPermissions(activity, permissionsList.toArray(new String[permissionsList.size()]),
                        REQUEST_PERMISSION);
                return cameraFilePath;
            }
        }
        Intent chooserIntent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (fileType.contains("image")) {
                chooserIntent = new Intent(Intent.ACTION_PICK, null);
                chooserIntent.setDataAndType(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
            } else {
                chooserIntent = new Intent(Intent.ACTION_GET_CONTENT);
                chooserIntent.addCategory(Intent.CATEGORY_OPENABLE);
                chooserIntent.setType("*/*");
            }
        } else {
            chooserIntent = new Intent(Intent.ACTION_GET_CONTENT);
            chooserIntent.addCategory(Intent.CATEGORY_OPENABLE);
            if (fileType.contains("image")) {
                chooserIntent.setType("image/*");
            } else {
                chooserIntent.setType("*/*");
            }
        }
        if (isMultiMode) {
            chooserIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, isMultiMode);
        }

        Intent chooser = new Intent(Intent.ACTION_CHOOSER);
        chooser.putExtra(Intent.EXTRA_TITLE, "File Chooser");
        chooser.putExtra(Intent.EXTRA_INTENT, chooserIntent);

        if (showCamera) {
            cameraFilePath = getOuterSDPath(activity) + File.separator +
                    System.currentTimeMillis() + ".jpg";
            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {

                // android7.0注意uri的获取方式改变
                Uri photoOutputUri = FileProvider.getUriForFile(
                        activity,
                        "com.bluefin.money.core.fileprovider",
                        new File(cameraFilePath));
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoOutputUri);
            } else {
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(cameraFilePath)));
            }
            chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{cameraIntent});
        }
        fragment.startActivityForResult(chooser, FILE_CHOOSER_REQUEST);
        return cameraFilePath;
    }

    /**
     * 根据URI获取文件真实路径（兼容多张机型）
     *
     * @param context
     * @param uri
     * @return
     */
    public static String getFilePathByUri(Context context, Uri uri) {
        if ("content".equalsIgnoreCase(uri.getScheme())) {

            int sdkVersion = Build.VERSION.SDK_INT;
            if (sdkVersion >= 19) { // api >= 19
                return getRealPathFromUriAboveApi19(context, uri);
            } else { // api < 19
                return getRealPathFromUriBelowAPI19(context, uri);
            }
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }

    /**
     * 适配api19及以上,根据uri获取图片的绝对路径
     *
     * @param context 上下文对象
     * @param uri     图片的Uri
     * @return 如果Uri对应的图片存在, 那么返回该图片的绝对路径, 否则返回null
     */
    @SuppressLint("NewApi")
    private static String getRealPathFromUriAboveApi19(Context context, Uri uri) {
        String filePath = null;
        if (DocumentsContract.isDocumentUri(context, uri)) {
            // 如果是document类型的 uri, 则通过document id来进行处理
            String documentId = DocumentsContract.getDocumentId(uri);
            if (isMediaDocument(uri)) { // MediaProvider
                // 使用':'分割
                String type = documentId.split(":")[0];
                String id = documentId.split(":")[1];

                String selection = MediaStore.Images.Media._ID + "=?";
                String[] selectionArgs = {id};

                //
                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                filePath = getDataColumn(context, contentUri, selection, selectionArgs);
            } else if (isDownloadsDocument(uri)) { // DownloadsProvider
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(documentId));
                filePath = getDataColumn(context, contentUri, null, null);
            } else if (isExternalStorageDocument(uri)) {
                // ExternalStorageProvider
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
                if ("primary".equalsIgnoreCase(type)) {
                    filePath = Environment.getExternalStorageDirectory() + "/" + split[1];
                }
            } else {
                //Log.e("路径错误");
            }
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {
            // 如果是 content 类型的 Uri
            filePath = getDataColumn(context, uri, null, null);
        } else if ("file".equals(uri.getScheme())) {
            // 如果是 file 类型的 Uri,直接获取图片对应的路径
            filePath = uri.getPath();
        }
        return filePath;
    }

    /**
     * 适配api19以下(不包括api19),根据uri获取图片的绝对路径
     *
     * @param context 上下文对象
     * @param uri     图片的Uri
     * @return 如果Uri对应的图片存在, 那么返回该图片的绝对路径, 否则返回null
     */
    private static String getRealPathFromUriBelowAPI19(Context context, Uri uri) {
        return getDataColumn(context, uri, null, null);
    }

    /**
     * 获取数据库表中的 _data 列，即返回Uri对应的文件路径
     *
     * @return
     */
    private static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        String path = null;

        String[] projection = new String[]{MediaStore.Images.Media.DATA};
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndexOrThrow(projection[0]);
                path = cursor.getString(columnIndex);
            }
        } catch (Exception e) {
            if (cursor != null) {
                cursor.close();
            }
        }
        return path;
    }

    /**
     * @param uri the Uri to check
     * @return Whether the Uri authority is MediaProvider
     */
    private static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    private static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri the Uri to check
     * @return Whether the Uri authority is DownloadsProvider
     */
    private static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * 获取外部存储器路径
     * 先判断是否有内置SD卡，若没有再判断是否有可移动
     */
    public static String getOuterSDPath(Context context) {
        /*可拆卸*/
        String sdcardPath = null;
        /*不可拆卸*/
        String emulatedPath = null;
        StorageManager mStorageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
        Class<?> storageVolumeClazz = null;
        try {
            storageVolumeClazz = Class.forName("android.os.storage.StorageVolume");
            Method getVolumeList = mStorageManager.getClass().getMethod("getVolumeList");
            Method getPath = storageVolumeClazz.getMethod("getPath");
            Method isRemovable = storageVolumeClazz.getMethod("isRemovable");
            Object result = getVolumeList.invoke(mStorageManager);
            final int length = Array.getLength(result);
            for (int i = 0; i < length; i++) {
                Object storageVolumeElement = Array.get(result, i);
                String path = (String) getPath.invoke(storageVolumeElement);
                /*mRemovable true:可拆卸（移动SD卡）  false:不可拆卸（内置SD卡）*/
                boolean mRemovable = (Boolean) isRemovable.invoke(storageVolumeElement);
                if (mRemovable && TextUtils.isEmpty(sdcardPath)) {
                    sdcardPath = path;
                }
                if (!mRemovable && TextUtils.isEmpty(emulatedPath)) {
                    emulatedPath = path;
                }
            }

            if (checkPathAvailable(emulatedPath)) {
                return emulatedPath;
            } else if (checkPathAvailable(sdcardPath)) {
                return sdcardPath;
            } else {
                return null;
            }
        } catch (Exception e) {
            return null;
        }
    }


    /**
     * 判断路径的有效性
     */
    private static boolean checkPathAvailable(String path) {
        if (TextUtils.isEmpty(path)) {
            return false;
        }
        File file = new File(path);
        if (file != null && file.exists()) {
            return true;
        } else {
            return false;
        }
    }
}
