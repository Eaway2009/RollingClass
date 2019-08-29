package com.tanhd.rollingclass.fragments.kowledge;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.tanhd.rollingclass.R;
import com.tanhd.rollingclass.activity.DocumentEditActivity;
import com.tanhd.rollingclass.db.KeyConstants;
import com.tanhd.rollingclass.server.RequestCallback;
import com.tanhd.rollingclass.server.ScopeServer;
import com.tanhd.rollingclass.server.data.KnowledgeDetailMessage;
import com.tanhd.rollingclass.server.data.KnowledgeLessonSample;
import com.tanhd.rollingclass.server.data.KnowledgeModel;
import com.tanhd.rollingclass.server.data.SyncSampleToClassRequest;
import com.tanhd.rollingclass.views.TaskDisplayView;

import java.util.ArrayList;
import java.util.List;

public class KnowledgeEditingFragment extends Fragment implements View.OnClickListener, TaskDisplayView.TaskDisplayEditListener, KnowledgeAddTaskFragment.Callback {

    public static final String PARAM_KNOWLEDGE_DETAIL_DATA = "PARAM_KNOWLEDGE_DETAIL_DATA";
    public static final String PARAM_KNOWLEDGE_DETAIL_STATUS = "PARAM_KNOWLEDGE_DETAIL_STATUS";
    private KnowledgeEditingFragment.Callback mListener;

    private KnowledgeAddTaskFragment mAddTaskFragment;

    private TextView mPublishButton;
    private TextView mFinishButton;
    private TextView mKnowledgeNameTextView;
    private TextView mKnowledgeNameEditView;
    private EditText mKnowledgeNameEditText;
    private TextView mKnowledgeAddButton;
    private LinearLayout mKnowledgeTasksLayout;
    private View mAddFragmentView;

    private KnowledgeModel mKnowledgeModel;
    private KnowledgeDetailMessage mKnowledgeDetailMessage;

    /**
     * 1.课前；2.课时；3.课后
     */
    private int mStatus;
    private boolean mIsEditing = false;
    private CheckBox mSyncAfterClassCheckBox;
    private CheckBox mSyncInClassCheckBox;
    private CheckBox mSyncFreClassCheckBox;
    private ProgressBar mProgressBar;
    private RequestListDataTask mRequestListDataTask;
    private List<KnowledgeLessonSample> mDataList;

    /**
     * @param knowledgeModel          所属教材章节的参数
     * @param knowledgeDetailMessage 所属课时的参数
     * @param status                  1.课前；2.课时；3.课后
     * @param callback
     * @return
     */
    public static KnowledgeEditingFragment newInstance(KnowledgeModel knowledgeModel, KnowledgeDetailMessage knowledgeDetailMessage, int status, KnowledgeEditingFragment.Callback callback) {
        KnowledgeEditingFragment page = new KnowledgeEditingFragment();
        page.setListener(callback);
        Bundle args = new Bundle();
        args.putSerializable(DocumentEditActivity.PARAM_TEACHING_MATERIAL_DATA, knowledgeModel);
        args.putSerializable(PARAM_KNOWLEDGE_DETAIL_DATA, knowledgeDetailMessage);
        args.putSerializable(PARAM_KNOWLEDGE_DETAIL_STATUS, status);
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
        addEditingFragment();
        initData();
        return view;
    }

    private void initParams() {
        Bundle args = getArguments();
        mKnowledgeModel = (KnowledgeModel) args.getSerializable(DocumentEditActivity.PARAM_TEACHING_MATERIAL_DATA);
        mKnowledgeDetailMessage = (KnowledgeDetailMessage) args.getSerializable(PARAM_KNOWLEDGE_DETAIL_DATA);
        mStatus = args.getInt(PARAM_KNOWLEDGE_DETAIL_STATUS);
    }

    private void addEditingFragment() {
        mIsEditing = true;
        mAddTaskFragment = KnowledgeAddTaskFragment.newInstance(mKnowledgeModel, mKnowledgeDetailMessage, mStatus, this);
        getFragmentManager().beginTransaction().replace(R.id.fragment_add_task, mAddTaskFragment).commit();
        mAddFragmentView.setVisibility(View.VISIBLE);
    }

    private void initViews(View view) {
        mPublishButton = view.findViewById(R.id.knowledge_publish_button);
        mFinishButton = view.findViewById(R.id.knowledge_finish_button);
        mKnowledgeNameTextView = view.findViewById(R.id.knowledge_name);
        mKnowledgeNameEditText = view.findViewById(R.id.knowledge_name_et);
        mKnowledgeNameEditView = view.findViewById(R.id.knowledge_name_edit);
        mKnowledgeTasksLayout = view.findViewById(R.id.knowledge_tasks_layout);
        mKnowledgeAddButton = view.findViewById(R.id.knowledge_add_button);
        mAddFragmentView = view.findViewById(R.id.fragment_add_task);
        mProgressBar = view.findViewById(R.id.progressbar);

        mSyncFreClassCheckBox = view.findViewById(R.id.sync_fre_class_cb);
        mSyncInClassCheckBox = view.findViewById(R.id.sync_in_class_cb);
        mSyncAfterClassCheckBox = view.findViewById(R.id.sync_after_class_cb);
        if (mStatus == KeyConstants.KnowledgeStatus.FRE_CLASS) {
            mSyncFreClassCheckBox.setVisibility(View.GONE);
        }
        if (mStatus == KeyConstants.KnowledgeStatus.AT_CLASS) {
            mSyncInClassCheckBox.setVisibility(View.GONE);
        }
        if (mStatus == KeyConstants.KnowledgeStatus.AFTER_CLASS) {
            mSyncAfterClassCheckBox.setVisibility(View.GONE);
        }
        mSyncFreClassCheckBox.setOnClickListener(this);
        mSyncInClassCheckBox.setOnClickListener(this);
        mSyncAfterClassCheckBox.setOnClickListener(this);

        mPublishButton.setOnClickListener(this);
        mFinishButton.setOnClickListener(this);
        mKnowledgeNameEditView.setOnClickListener(this);
        mKnowledgeAddButton.setOnClickListener(this);
    }

    private void initData(){
        if(mKnowledgeDetailMessage !=null) {
            mKnowledgeNameEditText.setText(mKnowledgeDetailMessage.knowledge_point_name);
            mKnowledgeNameTextView.setText(mKnowledgeDetailMessage.knowledge_point_name);
            if(!TextUtils.isEmpty(mKnowledgeDetailMessage.knowledge_id)) {
                requestData();
            }
        }
    }

    public void resetData(KnowledgeModel knowledgeModel, KnowledgeDetailMessage insertKnowledgeResponse, int status) {
        Bundle args = new Bundle();
        args.putSerializable(DocumentEditActivity.PARAM_TEACHING_MATERIAL_DATA, knowledgeModel);
        args.putSerializable(PARAM_KNOWLEDGE_DETAIL_DATA, insertKnowledgeResponse);
        args.putSerializable(PARAM_KNOWLEDGE_DETAIL_STATUS, status);
        setArguments(args);

        initParams();
        initData();
    }

    private void requestData() {
        new RequestListDataTask().execute();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.knowledge_publish_button:
                if (mAddFragmentView.getVisibility() == View.VISIBLE) {
                    showDialog(getString(R.string.adding_task_warning));
                } else {
                    initFilterDialog();
                }
                break;
            case R.id.knowledge_finish_button:
                if (mAddFragmentView.getVisibility() == View.VISIBLE) {
                    showDialog(getString(R.string.adding_task_warning));
                } else {
                    mListener.onBack();
                }
                break;
            case R.id.knowledge_name_edit:
                if (mKnowledgeNameTextView.getVisibility() == View.VISIBLE) {
                    mKnowledgeNameTextView.setVisibility(View.GONE);
                    mKnowledgeNameEditView.setText(R.string.finish);
                    mKnowledgeNameEditText.setVisibility(View.VISIBLE);
                } else {
                    editTitle();
                }
                break;
            case R.id.knowledge_add_button:
                if (mAddFragmentView.getVisibility() == View.VISIBLE) {
                    showDialog(getString(R.string.adding_task_warning));
                } else {
                    addEditingFragment();
                }
                break;
            case R.id.sync_fre_class_cb:
                mSyncFreClassCheckBox.setChecked(true);
                if (mAddFragmentView.getVisibility() == View.VISIBLE) {
                    showDialog(getString(R.string.adding_task_warning));
                } else {
                    showLessonSampleDialog(KeyConstants.KnowledgeStatus.FRE_CLASS);
                }
                break;
            case R.id.sync_in_class_cb:
                mSyncInClassCheckBox.setChecked(true);
                if (mAddFragmentView.getVisibility() == View.VISIBLE) {
                    showDialog(getString(R.string.adding_task_warning));
                } else {
                    showLessonSampleDialog(KeyConstants.KnowledgeStatus.AT_CLASS);
                }
                break;
            case R.id.sync_after_class_cb:
                mSyncAfterClassCheckBox.setChecked(true);
                if (mAddFragmentView.getVisibility() == View.VISIBLE) {
                    showDialog(getString(R.string.adding_task_warning));
                } else {
                    showLessonSampleDialog(KeyConstants.KnowledgeStatus.AFTER_CLASS);
                }
                break;
        }
    }

    private void initFilterDialog() {
        String[] items = new String[2];
        final int[] checkedPublish = new int[]{1, 1, 1};
        switch (mStatus) {
            case KeyConstants.KnowledgeStatus.FRE_CLASS:
                items[0] = getString(R.string.publish_sync_in_class);
                items[1] = getString(R.string.publish_sync_after_class);
                break;
            case KeyConstants.KnowledgeStatus.AT_CLASS:
                items[0] = getString(R.string.publish_sync_fre_class);
                items[1] = getString(R.string.publish_sync_after_class);
                break;
            case KeyConstants.KnowledgeStatus.AFTER_CLASS:
                items[0] = getString(R.string.publish_sync_fre_class);
                items[1] = getString(R.string.publish_sync_in_class);
                break;
        }
        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.publish_warning)
                .setMultiChoiceItems(items, new boolean[]{true, true}, new DialogInterface.OnMultiChoiceClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        switch (mStatus) {
                            case KeyConstants.KnowledgeStatus.FRE_CLASS:
                                if (which == 0) {
                                    checkedPublish[1] = isChecked ? 1 : 0;
                                } else if (which == 1) {
                                    checkedPublish[2] = isChecked ? 1 : 0;
                                }
                                break;
                            case KeyConstants.KnowledgeStatus.AT_CLASS:
                                if (which == 0) {
                                    checkedPublish[0] = isChecked ? 1 : 0;
                                } else if (which == 1) {
                                    checkedPublish[2] = isChecked ? 1 : 0;
                                }
                                break;
                            case KeyConstants.KnowledgeStatus.AFTER_CLASS:
                                if (which == 0) {
                                    checkedPublish[0] = isChecked ? 1 : 0;
                                } else if (which == 1) {
                                    checkedPublish[1] = isChecked ? 1 : 0;
                                }
                                break;
                        }

                    }
                })
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new RequestPublishTask(checkedPublish).execute();
                    }
                }).show();
    }

    private void showLessonSampleDialog(final int syncTo) {
        if (mDataList == null) {
            return;
        }
        String[] sampleNameItems = new String[mDataList.size()];
        final String[] sampleIdItems = new String[mDataList.size()];
        boolean[] checkedItems = new boolean[mDataList.size()];
        final List<String> checkedIdList = new ArrayList<>();
        for (int i = 0; i < mDataList.size(); i++) {
            sampleNameItems[i] = mDataList.get(i).lesson_sample_name;
            sampleIdItems[i] = mDataList.get(i).lesson_sample_id;
            checkedItems[i] = true;
        }
        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.publish_warning)
                .setMultiChoiceItems(sampleNameItems, checkedItems, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        if (isChecked) {
                            sampleIdItems[which] = mDataList.get(which).lesson_sample_id;
                        } else {
                            sampleIdItems[which] = "";
                        }
                    }
                })
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        for (int i = 0; i < sampleIdItems.length; i++) {
                            if (!TextUtils.isEmpty(sampleIdItems[i])) {
                                checkedIdList.add(sampleIdItems[i]);
                            }
                        }
                        SyncSampleToClassRequest request = new SyncSampleToClassRequest();
                        switch (syncTo) {
                            case KeyConstants.KnowledgeStatus.AFTER_CLASS:
                                request.class_after_task = checkedIdList;
                                break;
                            case KeyConstants.KnowledgeStatus.AT_CLASS:
                                request.class_process_task = checkedIdList;
                                break;
                            case KeyConstants.KnowledgeStatus.FRE_CLASS:
                                request.class_before_task = checkedIdList;
                                break;
                        }
                        request.cur_status = mStatus;
                        new RequestSyncTask(request).execute();
                    }
                }).show();
    }

    public boolean isEditing() {
        return mIsEditing;
    }

    private void editTitle() {
        ScopeServer.getInstance().UpdateKnowledgeName(mKnowledgeNameEditText.getText().toString(), mKnowledgeDetailMessage.knowledge_id, new RequestCallback() {
            @Override
            public void onProgress(boolean b) {

            }

            @Override
            public void onResponse(String body) {
                mKnowledgeNameTextView.setText(mKnowledgeNameEditText.getText());

                mKnowledgeNameTextView.setVisibility(View.VISIBLE);
                mKnowledgeNameEditView.setText(R.string.edit);
                mKnowledgeNameEditText.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onError(String code, String message) {
                Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onBack() {
        mIsEditing = false;
        getFragmentManager().beginTransaction().remove(mAddTaskFragment);
        mAddFragmentView.setVisibility(View.GONE);
    }

    @Override
    public void onAddSuccess(String lessonSampleId) {
        requestData();
    }

    @Override
    public void onLoading(boolean loading) {
        changeLoading(loading);
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

    private void changeLoading(boolean show) {
        if (show) {
            mProgressBar.setVisibility(View.VISIBLE);
        } else {
            mProgressBar.setVisibility(View.GONE);
        }
    }

    private class RequestPublishTask extends AsyncTask<Void, Void, String> {
        int releaseFre;
        int releasePro;
        int releaseAfter;

        public RequestPublishTask(int[] checkedPublish) {
            this.releaseFre = checkedPublish[0];
            this.releasePro = checkedPublish[1];
            this.releaseAfter = checkedPublish[2];
        }

        @Override
        protected void onPreExecute() {
            changeLoading(true);
        }

        @Override
        protected String doInBackground(Void... strings) {
            return ScopeServer.getInstance().ReleaseKnowledgeToClass(mKnowledgeDetailMessage.knowledge_id, mKnowledgeDetailMessage.teacher_id, releaseFre, releasePro, releaseAfter);
        }

        @Override
        protected void onPostExecute(String resultCode) {
            changeLoading(false);
            if (!TextUtils.isEmpty(resultCode)) {
                Toast.makeText(getActivity(), R.string.publish_success, Toast.LENGTH_SHORT).show();
            } else {
                mListener.onBack();
            }
        }
    }


    private class RequestSyncTask extends AsyncTask<Void, Void, String> {
        SyncSampleToClassRequest request;

        RequestSyncTask(SyncSampleToClassRequest request) {
            this.request = request;
        }

        @Override
        protected void onPreExecute() {
            changeLoading(true);
        }

        @Override
        protected String doInBackground(Void... strings) {
            return ScopeServer.getInstance().SyncSampleToClass(request);
        }

        @Override
        protected void onPostExecute(String resultCode) {
            changeLoading(false);
            if (!TextUtils.isEmpty(resultCode)) {
                Toast.makeText(getActivity(), R.string.sync_success, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getActivity(), resultCode, Toast.LENGTH_SHORT).show();
            }

        }
    }

    @Override
    public void onEditTask() {

    }

    @Override
    public void onDeleteSuccess() {
        requestData();
    }

    private class RequestListDataTask extends AsyncTask<Void, Void, List<KnowledgeLessonSample>> {

        @Override
        protected void onPreExecute() {
            changeLoading(true);
        }

        @Override
        protected List<KnowledgeLessonSample> doInBackground(Void... strings) {
            return ScopeServer.getInstance().QuerySampleByKnowledge(mKnowledgeDetailMessage.knowledge_id, mStatus);
        }

        @Override
        protected void onPostExecute(List<KnowledgeLessonSample> dataList) {
            changeLoading(false);
            mKnowledgeTasksLayout.removeAllViews();
            mDataList = dataList;

            if (mDataList != null) {

                mIsEditing = false;
                getFragmentManager().beginTransaction().remove(mAddTaskFragment);
                mAddFragmentView.setVisibility(View.GONE);
                for (KnowledgeLessonSample lessonSample : mDataList) {
                    TaskDisplayView taskDiplayView = new TaskDisplayView(getActivity(), (LinearLayout) getActivity().getLayoutInflater().inflate(R.layout.layout_task_added, null), KnowledgeEditingFragment.this);
                    LinearLayout taskDisplayView = taskDiplayView.setData(lessonSample);
                    mKnowledgeTasksLayout.addView(taskDisplayView);
                }
            }
        }
    }

    public interface Callback {
        void onBack();
    }
}
