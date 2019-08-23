package com.tanhd.rollingclass.fragments.kowledge;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.tanhd.rollingclass.R;
import com.tanhd.rollingclass.activity.DocumentEditActivity;
import com.tanhd.rollingclass.server.ScopeServer;
import com.tanhd.rollingclass.server.data.KnowledgeModel;
import com.tanhd.rollingclass.server.data.ResourceModel;
import com.tanhd.rollingclass.utils.GetFileHelper;

import java.io.File;

public class KnowledgeEditingFragment extends Fragment implements View.OnClickListener {

    private KnowledgeEditingFragment.Callback mListener;
    private TextView mPublishButton;
    private TextView mFinishButton;
    private TextView mKnowledgeNameTextView;
    private TextView mKnowledgeNameEditView;
    private EditText mKnowledgeNameEditText;
    private TextView mKnowledgeAddButton;
    private LinearLayout mKnowledgeTasksLayout;
    private View mTaskCancelButton;
    private View mTaskSaveButton;
    private View mUploadPptView;
    private View mUploadVideoView;
    private View mUploadExercisesView;
    private View mUploadDocumentsView;
    private View mUploadPhotoView;

    private KnowledgeModel mKnowledgeModel;

    public static KnowledgeEditingFragment newInstance(KnowledgeModel knowledgeModel, KnowledgeEditingFragment.Callback callback) {
        KnowledgeEditingFragment page = new KnowledgeEditingFragment();
        page.setListener(callback);
        Bundle args = new Bundle();
        args.putSerializable(DocumentEditActivity.PARAM_KNOWLEDGE_DATA, knowledgeModel);
        page.setArguments(args);
        return page;
    }

    private void setListener(KnowledgeEditingFragment.Callback listener) {
        mListener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.page_edit_knowledge, container, false);
        initParams();
        initViews(view);
        return view;
    }

    private void initParams() {
        Bundle args = getArguments();
        mKnowledgeModel = (KnowledgeModel) args.getSerializable(DocumentEditActivity.PARAM_KNOWLEDGE_DATA);
    }

    private void initViews(View view) {
        mPublishButton = view.findViewById(R.id.knowledge_publish_button);
        mFinishButton = view.findViewById(R.id.knowledge_finish_button);
        mKnowledgeNameTextView = view.findViewById(R.id.knowledge_name);
        mKnowledgeNameEditText = view.findViewById(R.id.knowledge_name_et);
        mKnowledgeNameEditView = view.findViewById(R.id.knowledge_name_edit);
        mKnowledgeTasksLayout = view.findViewById(R.id.knowledge_tasks_layout);
        mKnowledgeAddButton = view.findViewById(R.id.knowledge_add_button);
        mTaskCancelButton = view.findViewById(R.id.task_add_cancel_button);
        mTaskSaveButton = view.findViewById(R.id.task_add_save_button);

        mUploadPptView = view.findViewById(R.id.upload_ppt);
        mUploadVideoView = view.findViewById(R.id.upload_video);
        mUploadExercisesView = view.findViewById(R.id.upload_exercises);
        mUploadDocumentsView = view.findViewById(R.id.upload_documents);
        mUploadPhotoView = view.findViewById(R.id.upload_photo);

        mPublishButton.setOnClickListener(this);
        mFinishButton.setOnClickListener(this);
        mKnowledgeNameEditView.setOnClickListener(this);
        mKnowledgeAddButton.setOnClickListener(this);
        mTaskCancelButton.setOnClickListener(this);
        mTaskSaveButton.setOnClickListener(this);

        mKnowledgeNameEditText.setText(mKnowledgeModel.knowledge_point_name);
        mKnowledgeNameTextView.setText(mKnowledgeModel.knowledge_point_name);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.knowledge_publish_button:

                break;
            case R.id.knowledge_finish_button:
                break;
            case R.id.task_add_cancel_button:
                break;
            case R.id.task_add_save_button:
                break;
            case R.id.upload_ppt:
            case R.id.upload_video:
            case R.id.upload_exercises:
            case R.id.upload_documents:
            case R.id.upload_photo:
                GetFileHelper.fileSelector(getActivity(), false, false);
                break;
            case R.id.knowledge_name_edit:
                mKnowledgeNameTextView.setVisibility(View.GONE);
                mKnowledgeNameEditView.setVisibility(View.GONE);
                mKnowledgeNameEditText.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void showDialog(String message) {
        final Dialog[] mNetworkDialog = new Dialog[1];
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setMessage(message)
                .setPositiveButton("关闭", null)
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
            new UploadMarkTask(imagePath, )
        } else {
            Toast.makeText(getActivity(), R.string.select_pic_again, Toast.LENGTH_SHORT).show();
        }
    }

    private class UploadMarkTask extends AsyncTask<Void, Void, String> {

        ResourceModel resourceModel;
        String filePath;

        UploadMarkTask(String filePath, ResourceModel model){
            this.resourceModel = model;
            this.filePath = filePath;
        }

        @Override
        protected void onPostExecute(String integer) {
            Toast.makeText(getActivity(), integer, Toast.LENGTH_SHORT).show();
        }

        @Override
        protected String doInBackground(Void... strings) {
            return ScopeServer.getInstance().resourceUpload(filePath, resourceModel);
        }
    }

    public interface Callback {
        void onBack();
    }
}
