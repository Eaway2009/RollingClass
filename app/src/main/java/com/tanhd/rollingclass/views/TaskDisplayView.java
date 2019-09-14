package com.tanhd.rollingclass.views;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.view.menu.MenuPopupHelper;
import android.support.v7.widget.PopupMenu;
import android.util.AttributeSet;
import android.util.Log;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.tanhd.rollingclass.MainActivity;
import com.tanhd.rollingclass.R;
import com.tanhd.rollingclass.fragments.resource.QuestionResourceFragment;
import com.tanhd.rollingclass.server.RequestCallback;
import com.tanhd.rollingclass.server.ScopeServer;
import com.tanhd.rollingclass.server.data.ExternalParam;
import com.tanhd.rollingclass.server.data.KnowledgeLessonSample;
import com.tanhd.rollingclass.server.data.LessonSampleModel;
import com.tanhd.rollingclass.server.data.QuestionModel;
import com.tanhd.rollingclass.server.data.ResourceModel;
import com.tanhd.rollingclass.server.data.ResourceUpload;
import com.tanhd.rollingclass.server.data.TeacherData;
import com.tanhd.rollingclass.server.data.UserData;
import com.tanhd.rollingclass.utils.GetFileHelper;

import java.io.File;
import java.lang.reflect.Field;
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
    private Activity mContext;
    private TaskDisplayEditListener mListener;

    private List<String> mExercisesList = new ArrayList<>();
    private List<String> mPPTList = new ArrayList<>();
    private List<String> mWordList = new ArrayList<>();
    private List<String> mImageList = new ArrayList<>();
    private List<String> mVideoList = new ArrayList<>();

    public TaskDisplayView(Activity activity, LinearLayout linearLayout, TaskDisplayEditListener listener) {
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
        if (mData.doc_set != null) {
            addFilesDisplayView(R.string.documents, mData.doc_set);
            for (ResourceModel resourceModel:mData.doc_set){
                mWordList.add(resourceModel.resource_id);
            }
        }
        if (mData.ppt_set != null) {
            addFilesDisplayView(R.string.ppt, mData.ppt_set);
            for (ResourceModel resourceModel:mData.ppt_set){
                mPPTList.add(resourceModel.resource_id);
            }
        }
        if (mData.image_set != null) {
            addFilesDisplayView(R.string.photo, mData.image_set);
            for (ResourceModel resourceModel:mData.image_set){
                mImageList.add(resourceModel.resource_id);
            }
        }
        if (mData.video_set != null) {
            addFilesDisplayView(R.string.micro_course, mData.video_set);
            for (ResourceModel resourceModel:mData.video_set){
                mVideoList.add(resourceModel.resource_id);
            }
        }
        if (mData.question_set != null) {
            addQuetionsDisplayView(R.string.exercises, mData.question_set);
            for (QuestionModel resourceModel:mData.question_set){
                mExercisesList.add(resourceModel.question_id);
            }
        }
        return mLinearLayout;
    }

    private void addFilesDisplayView(int resourceType, List<ResourceModel> dataSet) {
        LinearLayout displayLayout = (LinearLayout) mContext.getLayoutInflater().inflate(R.layout.layout_resource_item, null);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.topMargin = mContext.getResources().getDimensionPixelSize(R.dimen.activity_vertical_margin);
        displayLayout.setLayoutParams(layoutParams);
        TextView resourceTypeView = displayLayout.findViewById(R.id.files_type_tv);
        resourceTypeView.setText(resourceType);
        LinearLayout resourcesLayout = displayLayout.findViewById(R.id.files_display_layout);
        addFileView(resourcesLayout, dataSet);
        mFilesLayout.addView(displayLayout);
    }

    private void addQuetionsDisplayView(int resourceType, List<QuestionModel> dataSet) {
        LinearLayout displayLayout = (LinearLayout) mContext.getLayoutInflater().inflate(R.layout.layout_resource_item, null);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.topMargin = mContext.getResources().getDimensionPixelSize(R.dimen.activity_vertical_margin);
        displayLayout.setLayoutParams(layoutParams);
        TextView resourceTypeView = displayLayout.findViewById(R.id.files_type_tv);
        resourceTypeView.setText(resourceType);
        LinearLayout resourcesLayout = displayLayout.findViewById(R.id.files_display_layout);
//        QuestionResourceFragment questionResourceFragment = QuestionResourceFragment.newInstance();
//        mContext.getFragmentManager().beginTransaction().replace(R.id.framelayout, questionResourceFragment).commit();
        mFilesLayout.addView(displayLayout);
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
                case QUESTION_TYPE:
                    iconView.setImageResource(R.drawable.pdf_icon);
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
                ResourceModel data = (ResourceModel) v.getTag();
                mListener.onEditTask(TaskDisplayView.this, data, v);
                break;
        }
    }

    private void showDeleteDialog(View v) {
        final Dialog[] mNetworkDialog = new Dialog[1];
        final ResourceModel data = (ResourceModel) v.getTag();
        DialogInterface.OnClickListener onDialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case BUTTON_POSITIVE:
                        deleteSample(data, null);
                        break;
                    case BUTTON_NEGATIVE:
                        mNetworkDialog[0].dismiss();
                        mNetworkDialog[0] = null;
                        break;
                }
            }
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext)
                .setMessage(R.string.delete_task_warning)
                .setTitle(R.string.dialog_tile)
                .setPositiveButton(R.string.sure, onDialogClickListener)
                .setNegativeButton(R.string.cancel, onDialogClickListener)
                .setCancelable(false)
                .setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        mNetworkDialog[0] = null;
                    }
                });
        mNetworkDialog[0] = builder.create();
        mNetworkDialog[0].show();
    }

    public void editFile(ResourceModel toRemoveModel, ResourceModel newModel) {
        deleteSample(toRemoveModel, newModel);
    }

    private void deleteSample(ResourceModel toRemoveModel, ResourceModel newModel) {
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
            case QUESTION_TYPE:
                mExercisesList.remove(toRemoveModel.resource_id);
                if (newModel != null) {
                    mExercisesList.add(newModel.resource_id);
                }
                break;
        }
        LessonSampleModel lessonSampleModel = new LessonSampleModel();
        lessonSampleModel.doc_set = mWordList;
        lessonSampleModel.ppt_set = mPPTList;
        lessonSampleModel.question_set = mExercisesList;
        lessonSampleModel.image_set = mImageList;
        lessonSampleModel.video_set = mVideoList;
        lessonSampleModel.lesson_sample_id = mData.lesson_sample_id;
        lessonSampleModel.knowledge_id = mData.knowledge_id;
        lessonSampleModel.lesson_type = mData.lesson_type;
        lessonSampleModel.number = mData.number;
        lessonSampleModel.lesson_sample_name = mData.lesson_sample_name;
        lessonSampleModel.status = mData.status;
        ScopeServer.getInstance().EditLessonSample(lessonSampleModel, requestCallback);
    }

    public interface TaskDisplayEditListener {
        void onEditTask(TaskDisplayView displayView, ResourceModel data, View editView);

        void onDeleteSuccess();

        void onLoading(boolean show);
    }
}
