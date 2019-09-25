package com.tanhd.rollingclass.views;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.tanhd.rollingclass.R;
import com.tanhd.rollingclass.fragments.resource.QuestionModelFragment;
import com.tanhd.rollingclass.server.RequestCallback;
import com.tanhd.rollingclass.server.ScopeServer;
import com.tanhd.rollingclass.server.data.KnowledgeLessonSample;
import com.tanhd.rollingclass.server.data.LessonSampleModel;
import com.tanhd.rollingclass.server.data.QuestionModel;
import com.tanhd.rollingclass.server.data.ResourceBaseModel;
import com.tanhd.rollingclass.server.data.ResourceModel;

import java.util.ArrayList;
import java.util.List;

import static android.content.DialogInterface.BUTTON_NEGATIVE;
import static android.content.DialogInterface.BUTTON_POSITIVE;
import static com.tanhd.rollingclass.db.KeyConstants.ResourceType.PPT_TYPE;
import static com.tanhd.rollingclass.db.KeyConstants.ResourceType.QUESTION_TYPE;
import static com.tanhd.rollingclass.db.KeyConstants.ResourceType.WORD_TYPE;
import static com.tanhd.rollingclass.db.KeyConstants.ResourceType.IMAGE_TYPE;
import static com.tanhd.rollingclass.db.KeyConstants.ResourceType.VIDEO_TYPE;

public class TaskDisplayView implements View.OnClickListener {
    private TextView mTitleView;

    private LinearLayout mLinearLayout;
    private KnowledgeLessonSample mData;
    private LinearLayout mFilesLayout;
    private AppCompatActivity mContext;
    private TaskDisplayEditListener mListener;

    private List<String> mExercisesList = new ArrayList<>();
    private List<String> mPPTList = new ArrayList<>();
    private List<String> mWordList = new ArrayList<>();
    private List<String> mImageList = new ArrayList<>();
    private List<String> mVideoList = new ArrayList<>();

    public TaskDisplayView(AppCompatActivity activity, LinearLayout linearLayout, TaskDisplayEditListener listener) {
        mLinearLayout = linearLayout;
        mListener = listener;
        mContext = activity;
        init(mLinearLayout);
    }

    private void init(LinearLayout linearLayout) {
        mTitleView = linearLayout.findViewById(R.id.task_title);
        mFilesLayout = linearLayout.findViewById(R.id.upload_file_layout);
    }

    public LinearLayout setData(KnowledgeLessonSample data) {
        mData = data;

        mTitleView.setText(mData.lesson_sample_name);

        return mLinearLayout;
    }

    private void addResourceDisplayFile(int resourceType, List<ResourceModel> dataSet, List<QuestionModel> questionSet) {
        LinearLayout displayLayout = (LinearLayout) mContext.getLayoutInflater().inflate(R.layout.layout_resource_item, null);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.topMargin = mContext.getResources().getDimensionPixelSize(R.dimen.activity_vertical_margin);
        displayLayout.setLayoutParams(layoutParams);
        TextView resourceTypeView = displayLayout.findViewById(R.id.files_type_tv);
        resourceTypeView.setText(resourceType);
        LinearLayout resourcesLayout = displayLayout.findViewById(R.id.files_display_layout);
        if (dataSet != null) {
            addFileView(resourcesLayout, dataSet);
        } else if (questionSet != null) {
            addQuestionView(resourcesLayout, questionSet);
        }
        mFilesLayout.addView(displayLayout);
    }

    private void addQuestionView(LinearLayout resourcesLayout, List<QuestionModel> dataSet) {
        for (QuestionModel data : dataSet) {
            LinearLayout resourceLayout = (LinearLayout) mContext.getLayoutInflater().inflate(R.layout.layout_resource, null);

            TextView deleteView = resourceLayout.findViewById(R.id.task_delete);
            TextView editView = resourceLayout.findViewById(R.id.task_edit);
            deleteView.setTag(data);
            deleteView.setOnClickListener(this);
            editView.setTag(data);
            editView.setOnClickListener(this);
            resourceLayout.findViewById(R.id.resource_icon).setVisibility(View.GONE);
            resourceLayout.findViewById(R.id.resource_name_view).setVisibility(View.GONE);
            View questionView = resourceLayout.findViewById(R.id.question_fragment);
            questionView.setVisibility(View.VISIBLE);
            QuestionModelFragment.showQuestionModel(mContext.getLayoutInflater(), questionView, data);
            resourcesLayout.addView(resourceLayout);
        }
    }

    private void addFileView(LinearLayout resourcesLayout, List<ResourceModel> dataSet) {
        for (ResourceModel data : dataSet) {
            LinearLayout resourceLayout = (LinearLayout) mContext.getLayoutInflater().inflate(R.layout.layout_resource, null);

            TextView deleteView = resourceLayout.findViewById(R.id.task_delete);
            TextView editView = resourceLayout.findViewById(R.id.task_edit);
            deleteView.setTag(data);
            deleteView.setOnClickListener(this);
            editView.setTag(data);
            editView.setOnClickListener(this);
            ImageView iconView = resourceLayout.findViewById(R.id.resource_icon);
            TextView nameView = resourceLayout.findViewById(R.id.resource_name_view);
            nameView.setText(data.name);
            switch (data.resource_type) {
                case PPT_TYPE:
                    iconView.setImageResource(R.drawable.ppt_icon);
                    break;
                case WORD_TYPE:
                    iconView.setImageResource(R.drawable.word_icon);
                    break;
                case IMAGE_TYPE:
                    iconView.setImageResource(R.drawable.image_icon);
                    break;
                case VIDEO_TYPE:
                    iconView.setImageResource(R.drawable.video_icon);
                    break;
            }
            resourcesLayout.addView(resourceLayout);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.task_delete:
                showDeleteDialog(v);
                break;
            case R.id.task_edit:
                ResourceBaseModel data = (ResourceBaseModel) v.getTag();
                mListener.onEditTask(TaskDisplayView.this, data, v);
                break;
        }
    }

    private void showDeleteDialog(View v) {
        final ResourceBaseModel data = (ResourceBaseModel) v.getTag();
        new DefaultDialog(v.getResources().getString(R.string.dialog_tile), v.getResources().getString(R.string.delete_task_warning), "", "",null, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteSample(data, null);
            }
        }).show(mContext.getSupportFragmentManager(),"DefaultDialog");
    }

    public void editFile(ResourceBaseModel toRemoveModel, ResourceBaseModel newModel) {
        deleteSample(toRemoveModel, newModel);
    }

    private void deleteSample(ResourceBaseModel toRemoveModel, ResourceBaseModel newModel) {

        final RequestCallback requestCallback = new RequestCallback() {
            @Override
            public void onProgress(boolean b) {

            }

            @Override
            public void onResponse(String body) {
                mListener.onDeleteSuccess();
            }

            @Override
            public void onError(String code, String message) {
                Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
            }
        };
        LessonSampleModel lessonSampleModel = new LessonSampleModel();
        lessonSampleModel.lesson_sample_id = mData.lesson_sample_id;
        lessonSampleModel.knowledge_id = mData.knowledge_id;
        lessonSampleModel.lesson_type = mData.lesson_type;
        lessonSampleModel.number = mData.number;
        lessonSampleModel.lesson_sample_name = mData.lesson_sample_name;
        lessonSampleModel.status = mData.status;
        if (toRemoveModel instanceof QuestionModel) {
            deleteSample((QuestionModel) toRemoveModel, (QuestionModel) newModel);
        } else if (toRemoveModel instanceof ResourceModel) {
            deleteSample((ResourceModel) toRemoveModel, (ResourceModel) newModel);
        }
        lessonSampleModel.doc_set = mWordList;
        lessonSampleModel.ppt_set = mPPTList;
        lessonSampleModel.question_set = mExercisesList;
        lessonSampleModel.image_set = mImageList;
        lessonSampleModel.video_set = mVideoList;
        ScopeServer.getInstance().EditLessonSample(lessonSampleModel, requestCallback);
    }

    private void deleteSample(QuestionModel toRemoveModel, QuestionModel newModel) {
        mExercisesList.remove(toRemoveModel.question_id);
        if (newModel != null) {
            mExercisesList.add(newModel.question_id);
        }
    }

    private void deleteSample(ResourceModel toRemoveModel, ResourceModel newModel) {
        switch (toRemoveModel.resource_type) {
            case PPT_TYPE:
                mPPTList.remove(toRemoveModel.resource_id);
                if (newModel != null) {
                    mPPTList.add(newModel.resource_id);
                }
                break;
            case WORD_TYPE:
                mWordList.remove(toRemoveModel.resource_id);
                if (newModel != null) {
                    mWordList.add(newModel.resource_id);
                }
                break;
            case IMAGE_TYPE:
                mImageList.remove(toRemoveModel.resource_id);
                if (newModel != null) {
                    mImageList.add(newModel.resource_id);
                }
                break;
            case VIDEO_TYPE:
                mVideoList.remove(toRemoveModel.resource_id);
                if (newModel != null) {
                    mVideoList.add(newModel.resource_id);
                }
                break;
        }
    }

    public interface TaskDisplayEditListener {
        void onEditTask(TaskDisplayView displayView, ResourceBaseModel data, View editView);

        void onDeleteSuccess();

        void onLoading(boolean show);
    }
}
