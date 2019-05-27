package com.tanhd.rollingclass.views;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Size;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;
import com.tanhd.library.smartpen.SmartPenView;
import com.tanhd.rollingclass.R;
import com.tanhd.rollingclass.server.ScopeServer;
import com.tanhd.rollingclass.utils.AppUtils;
import com.tanhd.rollingclass.utils.ResultClass;

import java.io.File;
import java.util.List;

public class ObjectiveAnswerView extends RelativeLayout {
    public static interface ResultChangeListener {
        void onResultChanged();
    }
    private final static int CHOOSE_REQUEST = 0x01;
    private View mGalleryView;
    private SmartPenView mSmartPenView;
    private EditText mAnswerEditView;
    private View mUsbView;
    private ImageView mSelImageView;
    private Fragment mFragment;
    private RadioButton mUsbButton;
    private RadioButton mSoftButton;
    private RadioButton mImageButton;
    private String mImagePath;
    private int mMode;
    private ResultChangeListener mListener;

    public ObjectiveAnswerView(Context context) {
        super(context);
    }

    public ObjectiveAnswerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ObjectiveAnswerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setListener(ResultChangeListener listener) {
        mListener = listener;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        init();
    }

    public void setFragment(Fragment fragment) {
        mFragment = fragment;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK && requestCode == CHOOSE_REQUEST) {
            Uri imageUri = data.getData();
            mSelImageView.setImageURI(imageUri);

            String[] filePathColumn = { MediaStore.Images.Media.DATA };
            Cursor cursor = getContext().getContentResolver().query(imageUri, filePathColumn, null, null, null);
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
           mImagePath = cursor.getString(columnIndex);

           if (mListener != null)
               mListener.onResultChanged();
        }
    }

    private void init() {
        mSmartPenView = findViewById(R.id.answer_image);
        mAnswerEditView = findViewById(R.id.answer_edit);
        mAnswerEditView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (mListener != null)
                    mListener.onResultChanged();
            }
        });
        mUsbView = findViewById(R.id.usbview);
        mSelImageView = findViewById(R.id.sel_image);
        mGalleryView = findViewById(R.id.gallery);

        mGalleryView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                mFragment.startActivityForResult(i, CHOOSE_REQUEST);
            }
        });
        mUsbButton = findViewById(R.id.usb_pen);
        mUsbButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    smartPenMode();
                }
            }
        });
        mUsbButton.setChecked(true);

        mSoftButton = findViewById(R.id.soft_key);
        mSoftButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    softInputMode();
                }
            }
        });

        mImageButton = findViewById(R.id.load_image);
        mImageButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    loadImageMode();
                }
            }
        });
    }

    private void smartPenMode() {
        mMode = 0;
        mUsbView.setVisibility(View.VISIBLE);
        mSmartPenView.setVisibility(View.VISIBLE);
        mAnswerEditView.setVisibility(View.GONE);
        mSmartPenView.setActive(true);
        mSmartPenView.setImageListener(new SmartPenView.ImagePathListener() {
            @Override
            public void onPathChanged() {
                if (mListener != null)
                    mListener.onResultChanged();
            }
        });

        mGalleryView.setVisibility(View.GONE);
        mAnswerEditView.setVisibility(View.GONE);
        mSelImageView.setVisibility(View.GONE);

        if (mListener != null)
            mListener.onResultChanged();
    }

    private void softInputMode() {
        mMode = 1;
        mAnswerEditView.setVisibility(View.VISIBLE);
        mGalleryView.setVisibility(View.GONE);
        mUsbView.setVisibility(View.GONE);
        mSmartPenView.setActive(false);
        mSelImageView.setVisibility(View.GONE);
        if (mListener != null)
            mListener.onResultChanged();
    }

    private void loadImageMode() {
        mMode = 2;
        mSelImageView.setVisibility(View.VISIBLE);
        mGalleryView.setVisibility(View.VISIBLE);
        mAnswerEditView.setVisibility(View.GONE);
        mUsbView.setVisibility(View.GONE);
        mSmartPenView.setActive(false);


        if (mImagePath != null) {
            if (AppUtils.isUrl(mImagePath))
                Glide.with(getContext()).load(ScopeServer.RESOURCE_URL + mImagePath).into(mSelImageView);
            else
                Glide.with(getContext()).load(new File(mImagePath)).into(mSelImageView);
        }

        if (mListener != null)
            mListener.onResultChanged();
    }

    public void refresh() {
        switch (mMode) {
            case 0:
                mUsbButton.setChecked(true);
                break;
            case 1:
                mSoftButton.setChecked(true);
                break;
            case 2:
                mImageButton.setChecked(true);
                break;
        }
    }

    public String getEditText() {
        return mAnswerEditView.getText().toString();
    }

    public int getMode() {
        return mMode;
    }

    public String getImagePath() {
        return mImagePath;
    }

    public List getSmartPenData() {
        return mSmartPenView.getData();
    }

    public Size getSmartPenSize() {
        return mSmartPenView.getSize();
    }

    public void initData(int mode, Object data, Size size) {
        mMode = mode;
        switch (mMode) {
            case 0:
                mSmartPenView.initData((List) data, size);
                break;
            case 1:
                mAnswerEditView.setText((String)data);
                break;
            case 2:
                mImagePath = (String) data;
                break;
        }
        refresh();
    }

    public void active() {
        if (mSmartPenView != null)
            mSmartPenView.setActive(true);
    }

    public void setSmartPenListener(SmartPenView.DrawPathListener listener) {
        if (mSmartPenView != null)
            mSmartPenView.setListener(listener);
    }
}
