package com.tanhd.rollingclass.fragments.kowledge;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.view.menu.MenuPopupHelper;
import android.support.v7.widget.PopupMenu;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.CheckBox;
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
import com.tanhd.rollingclass.db.KeyConstants;
import com.tanhd.rollingclass.db.KeyConstants.ResourceType;
import com.tanhd.rollingclass.fragments.FrameDialog;
import com.tanhd.rollingclass.fragments.pages.AnswerListFragment;
import com.tanhd.rollingclass.fragments.pages.ResourceSelectorFragment;
import com.tanhd.rollingclass.fragments.resource.QuestionModelFragment;
import com.tanhd.rollingclass.server.RequestCallback;
import com.tanhd.rollingclass.server.ScopeServer;
import com.tanhd.rollingclass.server.data.ExternalParam;
import com.tanhd.rollingclass.server.data.KnowledgeDetailMessage;
import com.tanhd.rollingclass.server.data.KnowledgeModel;
import com.tanhd.rollingclass.server.data.LessonSampleModel;
import com.tanhd.rollingclass.server.data.OptionData;
import com.tanhd.rollingclass.server.data.QuestionModel;
import com.tanhd.rollingclass.server.data.ResourceBaseModel;
import com.tanhd.rollingclass.server.data.ResourceModel;
import com.tanhd.rollingclass.server.data.TeacherData;
import com.tanhd.rollingclass.server.data.UserData;
import com.tanhd.rollingclass.utils.AppUtils;
import com.tanhd.rollingclass.utils.BitmapUtils;
import com.tanhd.rollingclass.utils.GetFileHelper;
import com.tanhd.rollingclass.utils.StringUtils;
import com.tanhd.rollingclass.views.PopUploadFile;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 添加任务
 */
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
    private KnowledgeDetailMessage mKnowledgeDetailMessage;
    /**
     * 1. ppt 2. doc 3. image 4. 微课 5. 习题
     */
    private int mResourceCode;
    private List<String> mExercisesList = new ArrayList<>();
    private List<QuestionModel> mQuestionModes = new ArrayList<>();
    private List<String> mPPTList = new ArrayList<>();
    private List<String> mWordList = new ArrayList<>();
    private List<String> mImageList = new ArrayList<>();
    private List<String> mVideoList = new ArrayList<>();

    /**
     * 1.课前；2.课时；3.课后
     */
    private int mStatus;
    private AnswerDisplayLayout answerDisplayLayout;

    /**
     * @param knowledgeModel         所属教材章节的参数
     * @param knowledgeDetailMessage 所属课时的参数
     * @param status                 1.课前；2.课时；3.课后
     * @param callback
     * @return
     */
    public static KnowledgeAddTaskFragment newInstance(KnowledgeModel knowledgeModel, KnowledgeDetailMessage knowledgeDetailMessage, int status, KnowledgeAddTaskFragment.Callback callback) {
        KnowledgeAddTaskFragment page = new KnowledgeAddTaskFragment();
        page.setListener(callback);
        Bundle args = new Bundle();
        args.putSerializable(DocumentEditActivity.PARAM_TEACHING_MATERIAL_DATA, knowledgeModel);
        args.putSerializable(KnowledgeEditingFragment.PARAM_KNOWLEDGE_DETAIL_DATA, knowledgeDetailMessage);
        args.putSerializable(KnowledgeEditingFragment.PARAM_KNOWLEDGE_DETAIL_STATUS, status);
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
        mKnowledgeModel = (KnowledgeModel) args.getSerializable(DocumentEditActivity.PARAM_TEACHING_MATERIAL_DATA);
        mKnowledgeDetailMessage = (KnowledgeDetailMessage) args.getSerializable(KnowledgeEditingFragment.PARAM_KNOWLEDGE_DETAIL_DATA);
        mStatus = args.getInt(KnowledgeEditingFragment.PARAM_KNOWLEDGE_DETAIL_STATUS);
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
                if (mPPTList.size() == 0 && mExercisesList.size() == 0 && mWordList.size() == 0 && mImageList.size() == 0 && mVideoList.size() == 0) {
                    Toast.makeText(getActivity(), "请先上传文件再点保存", Toast.LENGTH_SHORT).show();
                    return;
                }
                LessonSampleModel lessonSampleModel = new LessonSampleModel();
                lessonSampleModel.doc_set = mWordList;
                lessonSampleModel.ppt_set = mPPTList;
                lessonSampleModel.question_set = mExercisesList;
                lessonSampleModel.image_set = mImageList;
                lessonSampleModel.video_set = mVideoList;
                lessonSampleModel.knowledge_id = mKnowledgeDetailMessage.knowledge_id;
                lessonSampleModel.lesson_type = 1;
                lessonSampleModel.number = 0;
                lessonSampleModel.lesson_sample_name = mTaskNameEditText.getText().toString();
                lessonSampleModel.status = mStatus;
                ScopeServer.getInstance().InsertLessonSample(lessonSampleModel, new RequestCallback() {
                    @Override
                    public void onProgress(boolean b) {

                    }

                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject json = new JSONObject(response);
                            String errorCode = json.optString("errorCode");
                            if (!TextUtils.isEmpty(errorCode) && !errorCode.equals("0")) {
                                Toast.makeText(getContext(), errorCode, Toast.LENGTH_SHORT).show();
                                return;
                            }

                            String sampleId = json.optString("result");

                            mListener.onAddSuccess(sampleId);
                        } catch (JSONException e) {
                            Toast.makeText(getContext(), "添加任务失败，请稍后重试", Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(String code, String message) {
                        if (message != null) {
                            Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                break;
            case R.id.enter_upload_photo:
                showPopupMenu(v, KeyConstants.ResourceType.IMAGE_TYPE, true);
                break;
            case R.id.upload_ppt:
                showPopupMenu(v, KeyConstants.ResourceType.PPT_TYPE, false);
                break;
            case R.id.upload_video:
                showPopupMenu(v, KeyConstants.ResourceType.VIDEO_TYPE, false);
                break;
            case R.id.upload_exercises:
                selectResourceFromServer(KeyConstants.ResourceType.QUESTION_TYPE);
                break;
            case R.id.upload_documents:
                showPopupMenu(v, KeyConstants.ResourceType.WORD_TYPE, false);
                break;
            case R.id.upload_photo:
                showPopupMenu(v, KeyConstants.ResourceType.IMAGE_TYPE, false);
                break;
            case R.id.task_delete:
                deleteFile(v);
                break;
        }
    }

    private void deleteFile(View v) {
        ResourceBaseModel resourceBaseModel = (ResourceBaseModel) v.getTag();
        if (resourceBaseModel instanceof ResourceModel) {
            ResourceModel resourceModel = (ResourceModel) resourceBaseModel;
            switch (resourceModel.resource_type) {
                case ResourceType.PPT_TYPE:
                    mPPTList.remove(resourceModel.resource_id);
                    break;
                case ResourceType.IMAGE_TYPE:
                    mImageList.remove(resourceModel.resource_id);
                    break;
                case ResourceType.WORD_TYPE:
                    mWordList.remove(resourceModel.resource_id);
                    break;
                case ResourceType.VIDEO_TYPE:
                    mVideoList.remove(resourceModel.resource_id);
                    break;
            }
        } else if (resourceBaseModel instanceof QuestionModel) {
            QuestionModel resourceModel = (QuestionModel) resourceBaseModel;
            mExercisesList.remove(resourceModel.question_id);
            mQuestionModes.remove(resourceModel);
        }
        LinearLayout resourceLayout = (LinearLayout) v.getParent();
        LinearLayout resourceDisplayLayout = (LinearLayout) resourceLayout.getParent();
        resourceDisplayLayout.removeView(resourceLayout);

        int count = mUploadFilesLayout.getChildCount();
        for (int i = 0; count > i; i++) {
            LinearLayout displayLayout = (LinearLayout) mUploadFilesLayout.getChildAt(i);
            if (resourceBaseModel instanceof ResourceModel) {
                ResourceModel resourceModel = (ResourceModel) resourceBaseModel;
                if ((int) displayLayout.getTag() == resourceModel.resource_type) {
                    switch (resourceModel.resource_type) {
                        case ResourceType.PPT_TYPE:
                            if (mPPTList.size() == 0) {
                                displayLayout.setVisibility(View.GONE);
                            }
                            break;
                        case ResourceType.IMAGE_TYPE:
                            if (mImageList.size() == 0) {
                                displayLayout.setVisibility(View.GONE);
                            }
                            break;
                        case ResourceType.WORD_TYPE:
                            if (mWordList.size() == 0) {
                                displayLayout.setVisibility(View.GONE);
                            }
                            break;
                        case ResourceType.VIDEO_TYPE:
                            if (mVideoList.size() == 0) {
                                displayLayout.setVisibility(View.GONE);
                            }
                            break;
                    }
                }
            } else {
                if ((int) displayLayout.getTag() == ResourceType.QUESTION_TYPE) {
                    if (mExercisesList.size() == 0) {
                        displayLayout.setVisibility(View.GONE);
                    }
                    answerDisplayLayout.resetData(mQuestionModes);
                    if (mQuestionModes.size() == 0) {
                        answerDisplayLayout.setVisibility(View.GONE);
                        LinearLayout view = (LinearLayout) answerDisplayLayout.getParent().getParent();
                        mUploadFilesLayout.removeView(view);
                    }
                }
            }
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
        if (imagePath == null) {
            Toast.makeText(getActivity(), R.string.select_pic_again, Toast.LENGTH_SHORT).show();
            return;
        }
        File file = new File(imagePath);

        if (!file.exists()) {
            Toast.makeText(getActivity(), R.string.select_pic_again, Toast.LENGTH_SHORT).show();
            return;
        }
        new UploadMarkTask(imagePath, file.getName(), mResourceCode, 1).execute();
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

    private class UploadMarkTask extends AsyncTask<Void, Void, String> {
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
        protected void onPreExecute() {
            mListener.onLoading(true);
        }

        @Override
        protected String doInBackground(Void... strings) {
            UserData userData = ExternalParam.getInstance().getUserData();
            TeacherData teacherData = (TeacherData) userData.getUserData();
            File file = new File(filePath);
            String newFileName = StringUtils.getFormatDate(new Date());
            String suffix = fileName.substring(fileName.lastIndexOf(".") + 1);
            file.renameTo(new File(file.getParent() + "/" + newFileName + "." + suffix));
            String response = ScopeServer.getInstance().resourceUpload(file.getParent() + "/" + newFileName + "." + suffix, teacherData.TeacherID, mKnowledgeModel.teaching_material_id, fileName, resourceType, level);
            return response;
        }

        @Override
        protected void onPostExecute(String response) {
            mListener.onLoading(false);
            try {
                JSONObject json = new JSONObject(response);
                String errorCode = json.optString("errorCode");
                if (!TextUtils.isEmpty(errorCode) && !errorCode.equals("0")) {
                    Toast.makeText(KnowledgeAddTaskFragment.this.getContext(), json.optString("errorMessage"), Toast.LENGTH_SHORT).show();
                } else {
                    ResourceModel model = new ResourceModel();
                    model.parse(model, json.optString("result"));
                    onCheckFile(model, filePath);
                }
            } catch (JSONException e) {
                Toast.makeText(KnowledgeAddTaskFragment.this.getContext(), R.string.upload_fail, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void onCheckFile(ResourceBaseModel result, String filePath) {
        if (result == null) {
            Toast.makeText(getActivity(), R.string.upload_fail, Toast.LENGTH_SHORT).show();
        } else {
            mEnterTypeRadioGroup.setEnabled(false);
            if (mEnterRadioButton.isChecked()) {
                mEnterDisplayPhotosLayout.setVisibility(View.VISIBLE);
                mEnterDisplayPhotosScrollView.setVisibility(View.VISIBLE);
                mFirstDisplayPhotoView.setVisibility(View.VISIBLE);
                if (filePath != null) {
                    Bitmap bitmap = BitmapUtils.decodeSampledBitmapFromFd(filePath, getResources().getDimensionPixelSize(R.dimen.display_image_width), getResources().getDimensionPixelSize(R.dimen.display_image_height));
                    ImageView showPictureLayout = (ImageView) getLayoutInflater().inflate(R.layout.upload_image_dis_view, null);
                    showPictureLayout.setImageBitmap(bitmap);
                    showPictureLayout.setTag(result);
                    mEnterDisplayPhotosLayout.addView(showPictureLayout);

                }
            } else {
                int count = mUploadFilesLayout.getChildCount();
                for (int i = 0; count > i; i++) {
                    LinearLayout displayLayout = (LinearLayout) mUploadFilesLayout.getChildAt(i);
                    if (result instanceof ResourceModel) {
                        if ((int) displayLayout.getTag() == ((ResourceModel) result).resource_type) {
                            displayLayout.setVisibility(View.VISIBLE);
                            LinearLayout resourcesLayout = (LinearLayout) displayLayout.getChildAt(1);
                            addFileView((ResourceModel) result, resourcesLayout);
                            return;
                        }
                    } else {
                        if ((int) displayLayout.getTag() == ResourceType.QUESTION_TYPE) {
                            displayLayout.setVisibility(View.VISIBLE);
                            LinearLayout resourcesLayout = (LinearLayout) displayLayout.getChildAt(1);
                            onCheckQuestion((QuestionModel) result, resourcesLayout);
//                            answerListFragment.addData((QuestionModel) result);
                            answerDisplayLayout.addData((QuestionModel) result);
                            return;
                        }
                    }
                }

                LinearLayout displayLayout = (LinearLayout) getActivity().getLayoutInflater().inflate(R.layout.layout_resource_item, null);
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//                    layoutParams.topMargin = getActivity().getResources().getDimensionPixelSize(R.dimen.activity_vertical_margin);
                displayLayout.setLayoutParams(layoutParams);

                LinearLayout resourcesLayout = displayLayout.findViewById(R.id.files_display_layout);
                TextView resourceTypeView = displayLayout.findViewById(R.id.files_type_tv);
                if (result instanceof ResourceModel) {
                    ResourceModel resourceModel = (ResourceModel) result;
                    resourceTypeView.setText(type2Name(resourceModel.resource_type));
                    addFileView(resourceModel, resourcesLayout);
                    displayLayout.setTag(resourceModel.resource_type);
                    mUploadFilesLayout.addView(displayLayout);
                } else {
                    resourceTypeView.setText(getString(R.string.exercises));
                    onCheckQuestion((QuestionModel) result, resourcesLayout);
                    displayLayout.setTag(ResourceType.QUESTION_TYPE);
                    mUploadFilesLayout.addView(displayLayout);
                    addQuestionAnswerLayout((QuestionModel) result);
                }
            }

        }
    }

    private void addQuestionAnswerLayout(QuestionModel questionModel) {
        LinearLayout answerLayout = (LinearLayout) getActivity().getLayoutInflater().inflate(R.layout.layout_resource_item, null);
        answerLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        LinearLayout answersLayout = answerLayout.findViewById(R.id.files_display_layout);
        TextView titleView = answerLayout.findViewById(R.id.files_type_tv);
        titleView.setText(getString(R.string.answers));

        answerDisplayLayout = answersLayout.findViewById(R.id.answer_layout);
        answerDisplayLayout.setVisibility(View.VISIBLE);
        answerDisplayLayout.addData(questionModel);
        answerLayout.setTag(ResourceType.ANSWER_TYPE);
        mUploadFilesLayout.addView(answerLayout);

    }

    private String type2Name(int type) {
        switch (type) {
            case ResourceType.PPT_TYPE:
                return getString(R.string.ppt_u);
            case ResourceType.IMAGE_TYPE:
                return getString(R.string.photo);
            case ResourceType.WORD_TYPE:
                return getString(R.string.documents);
            case ResourceType.VIDEO_TYPE:
                return getString(R.string.micro_course);
            case ResourceType.QUESTION_TYPE:
                return getString(R.string.exercises);
        }
        return "";
    }

    private void addFileView(ResourceModel result, LinearLayout linearLayout) {
        LinearLayout resourceLayout = (LinearLayout) getActivity().getLayoutInflater().inflate(R.layout.layout_resource, null);
        ImageView iconView = resourceLayout.findViewById(R.id.resource_icon);
        TextView nameView = resourceLayout.findViewById(R.id.resource_name_view);
        TextView deleteView = resourceLayout.findViewById(R.id.task_delete);
        TextView editView = resourceLayout.findViewById(R.id.task_edit);
        deleteView.setTag(result);
        deleteView.setOnClickListener(this);
        editView.setVisibility(View.GONE);
        nameView.setText(result.name);
        switch (result.resource_type) {
            case ResourceType.PPT_TYPE:
                mPPTList.add(result.resource_id);
                iconView.setImageResource(R.drawable.ppt_icon);
                break;
            case ResourceType.IMAGE_TYPE:
                mImageList.add(result.resource_id);
                iconView.setImageResource(R.drawable.image_icon);
                break;
            case ResourceType.WORD_TYPE:
                mWordList.add(result.resource_id);
                iconView.setImageResource(R.drawable.word_icon);
                break;
            case ResourceType.VIDEO_TYPE:
                mVideoList.add(result.resource_id);
                iconView.setImageResource(R.drawable.video_icon);
                break;
        }
        linearLayout.addView(resourceLayout);
    }

    @SuppressLint("RestrictedApi")
    private void showPopupMenu(View view, final int resourceCode, final boolean isImage) {
        new PopUploadFile(getActivity()).setLocalListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { //本地
                uploadFile(resourceCode, isImage);
            }
        }).setResourceListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { //资源库
                selectResourceFromServer(resourceCode);
            }
        }).showAsDropDown(view);

    }

    private void selectResourceFromServer(final int resourceCode) {
        FrameDialog.show(getChildFragmentManager(), ResourceSelectorFragment.newInstance(new ResourceSelectorFragment.Callback() {
            @Override
            public void cancel() {

            }

            @Override
            public void resourceChecked(ResourceModel resourceModel, QuestionModel questionModel) {
                if (resourceModel != null) {
                    onCheckFile(resourceModel, null);
                } else if (questionModel != null) {
                    onCheckFile(questionModel, null);
                }
            }
        }, resourceCode));
    }

    private void onCheckQuestion(QuestionModel questionModel, LinearLayout linearLayout) {
        LinearLayout resourceLayout = (LinearLayout) getActivity().getLayoutInflater().inflate(R.layout.layout_resource, null);
        resourceLayout.findViewById(R.id.resource_icon).setVisibility(View.GONE);
        resourceLayout.findViewById(R.id.resource_name_view).setVisibility(View.GONE);
        TextView deleteView = resourceLayout.findViewById(R.id.task_delete);
        TextView editView = resourceLayout.findViewById(R.id.task_edit);
        deleteView.setTag(questionModel);
        deleteView.setOnClickListener(this);
        editView.setVisibility(View.GONE);
        resourceLayout.findViewById(R.id.bottom_selector_layout).setVisibility(View.GONE);
        View questionView = resourceLayout.findViewById(R.id.question_fragment);
        QuestionModelFragment.showQuestionModel(getLayoutInflater(), questionView, questionModel);
        resourceLayout.findViewById(R.id.question_fragment).setVisibility(View.VISIBLE);
        mExercisesList.add(questionModel.question_id);
        linearLayout.addView(resourceLayout);
    }

    public interface Callback {
        void onBack();

        void onAddSuccess(String lessonSampleId);

        void onLoading(boolean loading);
    }
}
