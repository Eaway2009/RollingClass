package com.tanhd.rollingclass.views;

import android.app.Activity;
import android.content.Context;
import android.view.View;

import com.tanhd.rollingclass.R;

/**
 * 选择上传、资源
 * Created by YangShlai on 2019-09-23.
 */
public class PopUploadFile extends TopPushPopupWindow<Void> {
    private View.OnClickListener localListener,resourceListener;


    public PopUploadFile(Activity activity) {
        super(activity, activity, null);

    }

    @Override
    protected View generateCustomView(Void aVoid) {
        View view = activity.getLayoutInflater().inflate(R.layout.pop_upload_file,null);
        init(view);
        return view;
    }

    private void init(View view) {
        view.findViewById(R.id.root).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        view.findViewById(R.id.from_local).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (localListener != null) localListener.onClick(v);
                dismiss();
            }
        });

        view.findViewById(R.id.from_resource).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (resourceListener != null) resourceListener.onClick(v);
                dismiss();
            }
        });


    }

    public PopUploadFile setLocalListener(View.OnClickListener localListener) {
        this.localListener = localListener;
        return this;
    }

    public PopUploadFile setResourceListener(View.OnClickListener resourceListener) {
        this.resourceListener = resourceListener;
        return this;
    }
}
