package com.tanhd.rollingclass.views;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.tanhd.rollingclass.MainActivity;
import com.tanhd.rollingclass.R;
import com.tanhd.rollingclass.server.RequestCallback;
import com.tanhd.rollingclass.server.ScopeServer;
import com.tanhd.rollingclass.server.data.KnowledgeLessonSample;
import com.tanhd.rollingclass.server.data.LessonSampleData;
import com.tanhd.rollingclass.server.data.ResourceModel;

import java.util.List;

import static android.content.DialogInterface.BUTTON_NEGATIVE;
import static android.content.DialogInterface.BUTTON_POSITIVE;

public class TaskDisplayView implements View.OnClickListener {
    private TextView mTitleView;
    private View mDeleteView;
    private View mEditView;

    private LinearLayout mLinearLayout;
    private KnowledgeLessonSample mData;
    private LinearLayout mFilesLayout;
    private Activity mContext;
    private TaskDisplayEditListener mListener;

    public TaskDisplayView(Activity activity, LinearLayout linearLayout,TaskDisplayEditListener listener){
        mLinearLayout = linearLayout;
        mListener = listener;
        mContext = activity;
        init(mLinearLayout);
    }

    private void init(LinearLayout linearLayout) {
        mTitleView = linearLayout.findViewById(R.id.task_title);
        mFilesLayout = linearLayout.findViewById(R.id.upload_file_layout);

        mDeleteView = linearLayout.findViewById(R.id.task_delete);
        mEditView = linearLayout.findViewById(R.id.task_edit);
    }

    public LinearLayout setData(KnowledgeLessonSample data){
        mData = data;

        mTitleView.setText(mData.lesson_sample_name);
        mDeleteView.setTag(data);
        mDeleteView.setOnClickListener(this);
        mEditView.setTag(data);
        mEditView.setOnClickListener(this);
        if(mData.doc_set!=null) {
            addFileView(mData.doc_set);
        }
        if(mData.ppt_set!=null) {
            addFileView(mData.ppt_set);
        }
        if(mData.image_set!=null) {
            addFileView(mData.image_set);
        }
        if(mData.video_set!=null) {
            addFileView(mData.video_set);
        }
        if(mData.question_set!=null) {
            addFileView(mData.question_set);
        }
        return mLinearLayout;
    }

    private void addFileView(List<ResourceModel> dataSet){
        for(ResourceModel data:dataSet) {
            LinearLayout resourceLayout = (LinearLayout) mContext.getLayoutInflater().inflate(R.layout.layout_resource, null);
            ImageView iconView = resourceLayout.findViewById(R.id.resource_icon);
            TextView nameView = resourceLayout.findViewById(R.id.resource_name_view);
            nameView.setText(data.name);
            switch (data.resource_type) {
                case 1:
                    iconView.setImageResource(R.drawable.ppt_icon);
                    break;
                case 2:
                    iconView.setImageResource(R.drawable.word_icon);
                    break;
                case 3:
                    iconView.setImageResource(R.drawable.image_icon);
                    break;
                case 4:
                    iconView.setImageResource(R.drawable.video_icon);
                    break;
                case 5:
                    iconView.setImageResource(R.drawable.pdf_icon);
                    break;
            }
            mFilesLayout.addView(resourceLayout);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
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
        final KnowledgeLessonSample data = (KnowledgeLessonSample) v.getTag();
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
        DialogInterface.OnClickListener onDialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case BUTTON_POSITIVE:
                        ScopeServer.getInstance().DeleteLessonSample(data.lesson_sample_id, requestCallback);
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


    public interface TaskDisplayEditListener{
        void onEditTask();
        void onDeleteSuccess();
    }
}
