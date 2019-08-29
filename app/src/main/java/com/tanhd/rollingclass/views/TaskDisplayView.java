package com.tanhd.rollingclass.views;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.tanhd.rollingclass.R;
import com.tanhd.rollingclass.server.RequestCallback;
import com.tanhd.rollingclass.server.ScopeServer;
import com.tanhd.rollingclass.server.data.KnowledgeLessonSample;
import com.tanhd.rollingclass.server.data.ResourceModel;

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
        }
        if (mData.ppt_set != null) {
            addFilesDisplayView(R.string.ppt, mData.ppt_set);
        }
        if (mData.image_set != null) {
            addFilesDisplayView(R.string.photo, mData.image_set);
        }
        if (mData.video_set != null) {
            addFilesDisplayView(R.string.micro_course, mData.video_set);
        }
        if (mData.question_set != null) {
            addFilesDisplayView(R.string.exercises, mData.question_set);
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
                mListener.onEditTask();
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
                        deleteSample(data);
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

    private void deleteSample(ResourceModel resourceModel) {
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
        switch (resourceModel.resource_type) {
            case PPT_TYPE:
                mData.ppt_set.remove(resourceModel);
                break;
            case WORD_TYPE:
                mData.ppt_set.remove(resourceModel);
                break;
            case IMAGE_TYPE:
                mData.ppt_set.remove(resourceModel);
                break;
            case VIDEO_TYPE:
                mData.ppt_set.remove(resourceModel);
                break;
            case QUESTION_TYPE:
                mData.question_set.remove(resourceModel);
                break;
        }
        ScopeServer.getInstance().EditLessonSample(mData,requestCallback);
    }

    public interface TaskDisplayEditListener {
        void onEditTask();

        void onDeleteSuccess();
    }
}
