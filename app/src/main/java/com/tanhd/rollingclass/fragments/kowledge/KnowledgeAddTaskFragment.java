package com.tanhd.rollingclass.fragments.kowledge;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.tanhd.rollingclass.R;
import com.tanhd.rollingclass.activity.DocumentEditActivity;
import com.tanhd.rollingclass.server.RequestCallback;
import com.tanhd.rollingclass.server.ScopeServer;
import com.tanhd.rollingclass.server.data.ExternalParam;
import com.tanhd.rollingclass.server.data.KnowledgeModel;
import com.tanhd.rollingclass.server.data.LessonSampleModel;
import com.tanhd.rollingclass.server.data.ResourceUpload;
import com.tanhd.rollingclass.server.data.TeacherData;
import com.tanhd.rollingclass.server.data.UserData;
import com.tanhd.rollingclass.utils.BitmapUtils;
import com.tanhd.rollingclass.utils.GetFileHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class KnowledgeAddTaskFragment extends Fragment implements View.OnClickListener, RadioGroup.OnCheckedChangeListener {

    private KnowledgeAddTaskFragment.Callback mListener;

    private View mTaskCancelButton;
    private View mTaskSaveButton;
    private View mUploadPptView;
    private View mUploadVideoView;
    private View mUploadExercisesView;
    private View mUploadDocumentsView;
    private View mUploadPhotoView;
    private RadioGroup mEnterTypeRadioGroup;
    private RadioButton mUploadRadioButton;
    private RadioButton mEnterRadioButton;
    private ImageView mEnterUploadPhotoButton;
    private LinearLayout mEnterUploadPhotoLayout;
    private LinearLayout mUploadFilesLayout;
    private LinearLayout mUploadLayout;
    private EditText mTaskDescEditText;
    private EditText mTaskNameEditText;
    private View mTaskEditLayout;
    private LinearLayout mEnterDisplayPhotosLayout;
    private HorizontalScrollView mEnterDisplayPhotosScrollView;
    private ImageView mFirstDisplayPhotoView;

    private KnowledgeModel mKnowledgeModel;
    /**
     * 1. ppt 2. doc 3. image 4. 微课 5. 习题
     */
    private int mResourceCode;
    private List<ResourceUpload> mResourceList = new ArrayList<>();
    private List<String> mExercisesList = new ArrayList<>();
    private List<String> mPPTList = new ArrayList<>();
    private List<String> mWordList = new ArrayList<>();
    private List<String> mImageList = new ArrayList<>();
    private List<String> mVideoList = new ArrayList<>();

    public static KnowledgeAddTaskFragment newInstance(KnowledgeModel knowledgeModel, KnowledgeAddTaskFragment.Callback callback) {
        KnowledgeAddTaskFragment page = new KnowledgeAddTaskFragment();
        page.setListener(callback);
        Bundle args = new Bundle();
        args.putSerializable(DocumentEditActivity.PARAM_KNOWLEDGE_DATA, knowledgeModel);
        page.setArguments(args);
        return page;
    }

    private void setListener(KnowledgeAddTaskFragment.Callback listener) {
        mListener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_add_task, container, false);
        initParams();
        initViews(view);
        return view;
    }

    private void initParams() {
        Bundle args = getArguments();
        mKnowledgeModel = (KnowledgeModel) args.getSerializable(DocumentEditActivity.PARAM_KNOWLEDGE_DATA);
    }

    private void initViews(View view) {
        mTaskCancelButton = view.findViewById(R.id.task_add_cancel_button);
        mTaskSaveButton = view.findViewById(R.id.task_add_save_button);
        mUploadRadioButton = view.findViewById(R.id.upload_file_rb);
        mEnterRadioButton = view.findViewById(R.id.enter_rb);
        mEnterTypeRadioGroup = view.findViewById(R.id.edit_type_rg);
        mUploadLayout = view.findViewById(R.id.upload_layout);
        mEnterUploadPhotoButton = view.findViewById(R.id.enter_upload_photo);
        mEnterUploadPhotoLayout = view.findViewById(R.id.enter_upload_photo_layout);
        mUploadFilesLayout = view.findViewById(R.id.upload_file_layout);
        mTaskDescEditText = view.findViewById(R.id.task_desc_edittext);
        mTaskNameEditText = view.findViewById(R.id.task_name_et);
        mTaskEditLayout = view.findViewById(R.id.task_edit_layout);
        mUploadPptView = view.findViewById(R.id.upload_ppt);
        mUploadVideoView = view.findViewById(R.id.upload_video);
        mUploadExercisesView = view.findViewById(R.id.upload_exercises);
        mUploadDocumentsView = view.findViewById(R.id.upload_documents);
        mUploadPhotoView = view.findViewById(R.id.upload_photo);
        mEnterDisplayPhotosLayout = view.findViewById(R.id.enter_display_photos_layout);
        mEnterDisplayPhotosScrollView = view.findViewById(R.id.enter_display_photos_scrollview);
        mFirstDisplayPhotoView = view.findViewById(R.id.first_image_dis_view);

        mTaskCancelButton.setOnClickListener(this);
        mTaskSaveButton.setOnClickListener(this);
        mEnterUploadPhotoButton.setOnClickListener(this);
        mUploadPptView.setOnClickListener(this);
        mUploadVideoView.setOnClickListener(this);
        mUploadExercisesView.setOnClickListener(this);
        mUploadDocumentsView.setOnClickListener(this);
        mUploadPhotoView.setOnClickListener(this);

        mEnterTypeRadioGroup.setOnCheckedChangeListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.task_add_cancel_button:
                mListener.onBack();
                break;
            case R.id.task_add_save_button:
                if (mTaskNameEditText.getText().toString().trim().isEmpty()) {
                    Toast.makeText(getActivity(), "请输入任务名称再点保存", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (mResourceList.size() == 0) {
                    Toast.makeText(getActivity(), "请先上传文件再点保存", Toast.LENGTH_SHORT).show();
                    return;
                }
                LessonSampleModel lessonSampleModel = new LessonSampleModel();
                lessonSampleModel.doc_set = mWordList;
                lessonSampleModel.ppt_set = mPPTList;
                lessonSampleModel.question_set = mExercisesList;
                lessonSampleModel.image_set = mImageList;
                lessonSampleModel.video_set = mVideoList;
                lessonSampleModel.lesson_sample_name = mTaskNameEditText.getText().toString();
                ScopeServer.getInstance().InsertLessonSample(lessonSampleModel, new RequestCallback() {
                    @Override
                    public void onProgress(boolean b) {

                    }

                    @Override
                    public void onResponse(String body) {

                    }

                    @Override
                    public void onError(String code, String message) {

                    }
                });
                break;
            case R.id.enter_upload_photo:
                uploadFile(3, true);
                break;
            case R.id.upload_ppt:
                uploadFile(1, false);
                break;
            case R.id.upload_video:
                uploadFile(4, false);
                break;
            case R.id.upload_exercises:
                uploadFile(5, false);
                break;
            case R.id.upload_documents:
                uploadFile(2, false);
                break;
            case R.id.upload_photo:
                uploadFile(3, false);
                break;
        }
    }

    private void uploadFile(int resourceCode, boolean isImage) {
        mResourceCode = resourceCode;
        if (isImage) {
            GetFileHelper.imageSelector(getActivity(), this, false, true);
        } else {
            GetFileHelper.fileSelector(getActivity(), this, false, false);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if (requestCode == GetFileHelper.FILE_CHOOSER_REQUEST) {
            Uri result = intent == null || resultCode != Activity.RESULT_OK ? null : intent.getData();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                //Check if response is positive
                if (resultCode == Activity.RESULT_OK) {
                    if (intent != null) {
                        String dataString = intent.getDataString();
                        if (dataString != null) {
                            receiveFilePathCallback(GetFileHelper.getFilePathByUri(getActivity(), Uri.parse(dataString)));
                        }
                    }
                }
            } else {
                receiveFilePathCallback(GetFileHelper.getFilePathByUri(getActivity(), result));
            }
        }
        return;
    }

    private void receiveFilePathCallback(String imagePath) {
        File file = new File(imagePath);

        if (file.exists()) {
            new UploadMarkTask(imagePath, file.getName(), mResourceCode, 1).execute();
        } else {
            Toast.makeText(getActivity(), R.string.select_pic_again, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId) {
            case R.id.enter_rb:
                mEnterUploadPhotoLayout.setVisibility(View.VISIBLE);
                mTaskEditLayout.setVisibility(View.VISIBLE);
                mUploadLayout.setVisibility(View.GONE);
                mUploadFilesLayout.setVisibility(View.GONE);
                break;
            case R.id.upload_file_rb:
                mEnterUploadPhotoLayout.setVisibility(View.GONE);
                mTaskEditLayout.setVisibility(View.GONE);
                mUploadLayout.setVisibility(View.VISIBLE);
                mUploadFilesLayout.setVisibility(View.VISIBLE);

                break;
        }
    }

    private class UploadMarkTask extends AsyncTask<Void, Void, ResourceUpload> {
        private final int resourceType;
        private final int level;
        private final String filePath;
        private final String fileName;

        UploadMarkTask(String filePath, String fileName, int resourceType, int level) {
            this.filePath = filePath;
            this.fileName = fileName;
            this.resourceType = resourceType;
            this.level = level;
        }

        @Override
        protected ResourceUpload doInBackground(Void... strings) {
            UserData userData = ExternalParam.getInstance().getUserData();
            TeacherData teacherData = (TeacherData) userData.getUserData();
            return ScopeServer.getInstance().resourceUpload(filePath, teacherData.TeacherID, fileName, resourceType, level);
        }

        @Override
        protected void onPostExecute(ResourceUpload result) {
            if (result == null) {
                Toast.makeText(getActivity(), "上传失败，请重试", Toast.LENGTH_SHORT).show();
            } else {
                mEnterTypeRadioGroup.setEnabled(false);
                mResourceList.add(result);
                if (mEnterRadioButton.isChecked()) {
                    mEnterDisplayPhotosLayout.setVisibility(View.VISIBLE);
                    mEnterDisplayPhotosScrollView.setVisibility(View.VISIBLE);
                    mFirstDisplayPhotoView.setVisibility(View.VISIBLE);
                    Bitmap bitmap = BitmapUtils.decodeSampledBitmapFromFd(filePath, getResources().getDimensionPixelSize(R.dimen.display_image_width), getResources().getDimensionPixelSize(R.dimen.display_image_height));
                    if (mResourceList.size() == 1) {
                        mFirstDisplayPhotoView.setImageBitmap(bitmap);
                    } else {
                        ImageView showPictureLayout = (ImageView) getLayoutInflater().inflate(R.layout.upload_image_dis_view, null);
                        showPictureLayout.setImageBitmap(bitmap);
                        showPictureLayout.setTag(result);
                        mEnterDisplayPhotosLayout.addView(showPictureLayout);
                    }
                } else {
                    LinearLayout resourceLayout = (LinearLayout) getActivity().getLayoutInflater().inflate(R.layout.layout_resource, null);
                    ImageView iconView = resourceLayout.findViewById(R.id.resource_icon);
                    TextView nameView = resourceLayout.findViewById(R.id.resource_name_view);
                    nameView.setText(result.name);
                    switch (result.resource_type) {
                        case 1:
                            mPPTList.add(result.resource_id);
                            iconView.setImageResource(R.drawable.ppt_icon);
                            break;
                        case 2:
                            mWordList.add(result.resource_id);
                            iconView.setImageResource(R.drawable.word_icon);
                            break;
                        case 3:
                            mImageList.add(result.resource_id);
                            iconView.setImageResource(R.drawable.image_icon);
                            break;
                        case 4:
                            mVideoList.add(result.resource_id);
                            iconView.setImageResource(R.drawable.video_icon);
                            break;
                        case 5:
                            mExercisesList.add(result.resource_id);
                            iconView.setImageResource(R.drawable.pdf_icon);
                            break;
                    }
                    mUploadFilesLayout.addView(resourceLayout);
                }

            }
        }
    }

    public interface Callback {
        void onBack();
    }
}
