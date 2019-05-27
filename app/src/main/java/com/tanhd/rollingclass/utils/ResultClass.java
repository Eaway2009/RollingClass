package com.tanhd.rollingclass.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Size;

import com.tanhd.library.smartpen.SmartPenView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ResultClass implements Serializable {
    public int mode;
    public String text;
    public List smartPenViewData;
    public int smartPenViewWidth;
    public int smartPenViewHeight;
    public String imagePath;

    public boolean isAnswerFile() {
        switch (mode) {
            case 0:
            case 2:
                return true;
        }

        return false;
    }

    public String getResult(Context context) {
        switch (mode) {
            case 0: {
                if (smartPenViewData == null)
                    return null;

                if (smartPenViewData.isEmpty())
                    return null;

                Size size = new Size(smartPenViewWidth, smartPenViewHeight);
                Bitmap bitmap = SmartPenView.getBitmap(smartPenViewData, size);
                String fileName = UUID.randomUUID().toString().replace("-", "").toLowerCase();
                fileName = AppUtils.saveToFile(context, bitmap, fileName);
                bitmap.recycle();
                return fileName;
            }
            case 2: {
                return imagePath;
            }
        }

        return text;
    }

    public boolean isEmpty()  {
        switch (mode) {
            case 0: {
                if (smartPenViewData == null)
                    return true;

                return smartPenViewData.isEmpty();
            }
            case 2: {
                return imagePath == null;
            }
        }

        return TextUtils.isEmpty(text);
    }
}
