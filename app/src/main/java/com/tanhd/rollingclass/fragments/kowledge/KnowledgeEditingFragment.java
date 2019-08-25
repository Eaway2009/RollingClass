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
import android.widget.CompoundButton;
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
import com.tanhd.rollingclass.server.data.InsertKnowledgeResponse;
import com.tanhd.rollingclass.server.data.KnowledgeLessonSample;
import com.tanhd.rollingclass.server.data.KnowledgeModel;
import com.tanhd.rollingclass.views.TaskDisplayView;

import java.util.List;

public class KnowledgeEditingFragment extends Fragment implements View.OnClickListener, CompoundButton.OnCheckedChangeListener, RequestCallback {

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
    private InsertKnowledgeResponse mInsertKnowledgeResponse;

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

    /**
     * @param knowledgeModel          所属教材章节的参数
     * @param insertKnowledgeResponse 所属课时的参数
     * @param status                  1.课前；2.课时；3.课后
     * @param callback
     * @return
     */
    public static KnowledgeEditingFragment newInstance(KnowledgeModel knowledgeModel, InsertKnowledgeResponse insertKnowledgeResponse, int status, KnowledgeEditingFragment.Callback callback) {
        KnowledgeEditingFragment page = new KnowledgeEditingFragment();
        page.setListener(callback);
        Bundle args = new Bundle();
        args.putSerializable(DocumentEditActivity.PARAM_TEACHING_MATERIAL_DATA, knowledgeModel);
        args.putSerializable(PARAM_KNOWLEDGE_DETAIL_DATA, insertKnowledgeResponse);
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
        return view;
    }

    private void initParams() {
        Bundle args = getArguments();
        mKnowledgeModel = (KnowledgeModel) args.getSerializable(DocumentEditActivity.PARAM_TEACHING_MATERIAL_DATA);
        mInsertKnowledgeResponse = (InsertKnowledgeResponse) args.getSerializable(PARAM_KNOWLEDGE_DETAIL_DATA);
        mStatus = args.getInt(PARAM_KNOWLEDGE_DETAIL_STATUS);
    }

    private void addEditingFragment() {
        mIsEditing = true;
        mAddTaskFragment = KnowledgeAddTaskFragment.newInstance(mKnowledgeModel, mInsertKnowledgeResponse, mStatus, new KnowledgeAddTaskFragment.Callback() {
            @Override
            public void onBack() {
                mIsEditing = false;
                getFragmentManager().beginTransaction().remove(mAddTaskFragment);
                mAddFragmentView.setVisibility(View.GONE);
            }

            @Override
            public void onAddSuccess(String lessonSampleId) {
                mIsEditing = false;
                getFragmentManager().beginTransaction().remove(mAddTaskFragment);
                mAddFragmentView.setVisibility(View.GONE);
                requestData();
            }

            @Override
            public void onLoading(boolean loading) {
                changeLoading(loading);
            }
        });
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
        mSyncFreClassCheckBox.setOnCheckedChangeListener(this);
        mSyncInClassCheckBox.setOnCheckedChangeListener(this);
        mSyncAfterClassCheckBox.setOnCheckedChangeListener(this);

        mPublishButton.setOnClickListener(this);
        mFinishButton.setOnClickListener(this);
        mKnowledgeNameEditView.setOnClickListener(this);
        mKnowledgeAddButton.setOnClickListener(this);

        mKnowledgeNameEditText.setText(mKnowledgeModel.knowledge_point_name);
        mKnowledgeNameTextView.setText(mKnowledgeModel.knowledge_point_name);
    }

    private void requestData() {
        if (mRequestListDataTask == null) {
            mRequestListDataTask = new RequestListDataTask();
        }
        mRequestListDataTask.execute();
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
                mKnowledgeNameTextView.setVisibility(View.GONE);
                mKnowledgeNameEditView.setVisibility(View.GONE);
                mKnowledgeNameEditText.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void initFilterDialog() {
        String[] items = new String[2];
        final int[] releaseFre = {0};
        final int[] releasePro = {0};
        final int[] releaseAfter = {0};
        switch (mStatus) {
            case KeyConstants.KnowledgeStatus.FRE_CLASS:
                items[0] = getString(R.string.publish_sync_in_class);
                items[1] = getString(R.string.publish_sync_after_class);
                releaseFre[0] = 1;
                break;
            case KeyConstants.KnowledgeStatus.AT_CLASS:
                items[0] = getString(R.string.publish_sync_fre_class);
                items[1] = getString(R.string.publish_sync_after_class);
                releasePro[0] = 1;
                break;
            case KeyConstants.KnowledgeStatus.AFTER_CLASS:
                items[0] = getString(R.string.publish_sync_fre_class);
                items[1] = getString(R.string.publish_sync_in_class);
                releaseAfter[0] = 1;
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
                                    releasePro[0] = isChecked ? 1 : 0;
                                } else if (which == 1) {
                                    releaseAfter[0] = isChecked ? 1 : 0;
                                }
                                break;
                            case KeyConstants.KnowledgeStatus.AT_CLASS:
                                if (which == 0) {
                                    releaseFre[0] = isChecked ? 1 : 0;
                                } else if (which == 1) {
                                    releaseAfter[0] = isChecked ? 1 : 0;
                                }
                                break;
                            case KeyConstants.KnowledgeStatus.AFTER_CLASS:
                                if (which == 0) {
                                    releaseFre[0] = isChecked ? 1 : 0;
                                } else if (which == 1) {
                                    releasePro[0] = isChecked ? 1 : 0;
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
                        ScopeServer.getInstance().ReleaseKnowledgeToClass(mInsertKnowledgeResponse.knowledge_id, mInsertKnowledgeResponse.teacher_id, releaseFre[0], releasePro[0], releaseAfter[0], KnowledgeEditingFragment.this);
                    }
                }).show();
    }

    public boolean isEditing() {
        return mIsEditing;
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

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
            if (mAddFragmentView.getVisibility() == View.VISIBLE) {
                showDialog(getString(R.string.adding_task_warning));
            } else {

            }
        }
    }

    @Override
    public void onProgress(boolean b) {

    }

    @Override
    public void onResponse(String body) {
        mListener.onBack();
    }

    @Override
    public void onError(String code, String message) {
        if (!"0".equals(code)) {
            Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
        }
    }


    private class RequestListDataTask extends AsyncTask<Void, Void, List<KnowledgeLessonSample>> {

        @Override
        protected void onPreExecute() {
            changeLoading(true);
        }

        @Override
        protected List<KnowledgeLessonSample> doInBackground(Void... strings) {
            return ScopeServer.getInstance().QuerySampleByKnowledge(mInsertKnowledgeResponse.knowledge_id, mStatus);
        }

        @Override
        protected void onPostExecute(List<KnowledgeLessonSample> dataList) {
            changeLoading(false);
            if (dataList != null) {
                for (KnowledgeLessonSample lessonSample : dataList) {
                    TaskDisplayView taskDiplayView = new TaskDisplayView(getActivity(), (LinearLayout) getActivity().getLayoutInflater().inflate(R.layout.layout_task_added, null));
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
