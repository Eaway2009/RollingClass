package com.tanhd.rollingclass.fragments.kowledge;

import android.annotation.SuppressLint;
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
import android.support.v7.view.menu.MenuPopupHelper;
import android.support.v7.widget.PopupMenu;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.tanhd.rollingclass.R;
import com.tanhd.rollingclass.activity.DocumentEditActivity;
import com.tanhd.rollingclass.db.KeyConstants;
import com.tanhd.rollingclass.fragments.FrameDialog;
import com.tanhd.rollingclass.fragments.pages.ResourceSelectorFragment;
import com.tanhd.rollingclass.fragments.resource.QuestionModelFragment;
import com.tanhd.rollingclass.server.RequestCallback;
import com.tanhd.rollingclass.server.ScopeServer;
import com.tanhd.rollingclass.server.data.ExternalParam;
import com.tanhd.rollingclass.server.data.KnowledgeDetailMessage;
import com.tanhd.rollingclass.server.data.KnowledgeLessonSample;
import com.tanhd.rollingclass.server.data.KnowledgeModel;
import com.tanhd.rollingclass.server.data.LessonSampleData;
import com.tanhd.rollingclass.server.data.LessonSampleModel;
import com.tanhd.rollingclass.server.data.OptionData;
import com.tanhd.rollingclass.server.data.QuestionModel;
import com.tanhd.rollingclass.server.data.ResourceBaseModel;
import com.tanhd.rollingclass.server.data.ResourceModel;
import com.tanhd.rollingclass.server.data.SyncSampleToClassRequest;
import com.tanhd.rollingclass.server.data.TeacherData;
import com.tanhd.rollingclass.server.data.UserData;
import com.tanhd.rollingclass.utils.AppUtils;
import com.tanhd.rollingclass.utils.GetFileHelper;
import com.tanhd.rollingclass.utils.StringUtils;
import com.tanhd.rollingclass.views.TaskDisplayView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static android.content.DialogInterface.BUTTON_NEGATIVE;
import static android.content.DialogInterface.BUTTON_POSITIVE;

public class KnowledgeEditingFragment extends Fragment implements View.OnClickListener, KnowledgeAddTaskFragment.Callback {

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
    private ResourceBaseModel mEditingResourceModel;
    private View mEditingView;

    /**
     * @param knowledgeModel         所属教材章节的参数
     * @param knowledgeDetailMessage 所属课时的参数
     * @param status                 1.课前；2.课时；3.课后
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
        if (mKnowledgeDetailMessage.class_before == 1) {
            mSyncFreClassCheckBox.setEnabled(false);
        } else {
            mSyncFreClassCheckBox.setEnabled(true);
        }
        if (mKnowledgeDetailMessage.class_process == 1) {
            mSyncFreClassCheckBox.setEnabled(false);
        } else {
            mSyncFreClassCheckBox.setEnabled(true);
        }
        if (mKnowledgeDetailMessage.class_after == 1) {
            mSyncFreClassCheckBox.setEnabled(false);
        } else {
            mSyncFreClassCheckBox.setEnabled(true);
        }
        mSyncFreClassCheckBox.setOnClickListener(this);
        mSyncInClassCheckBox.setOnClickListener(this);
        mSyncAfterClassCheckBox.setOnClickListener(this);

        mPublishButton.setOnClickListener(this);
        mFinishButton.setOnClickListener(this);
        mKnowledgeNameEditView.setOnClickListener(this);
        mKnowledgeAddButton.setOnClickListener(this);
    }

    private void initData() {
        if (mKnowledgeDetailMessage != null) {
            mKnowledgeNameEditText.setText(mKnowledgeDetailMessage.knowledge_point_name);
            mKnowledgeNameTextView.setText(mKnowledgeDetailMessage.knowledge_point_name);
            if (!TextUtils.isEmpty(mKnowledgeDetailMessage.knowledge_id)) {
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
            case R.id.task_delete:
                showDeleteDialog(v);
                break;
            case R.id.task_edit:
                editFile(v);
                break;
        }
    }

    private void showDeleteDialog(final View v) {
        final Dialog[] mNetworkDialog = new Dialog[1];
        DialogInterface.OnClickListener onDialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case BUTTON_POSITIVE:
                        deleteFile(v);
                        break;
                    case BUTTON_NEGATIVE:
                        mNetworkDialog[0].dismiss();
                        mNetworkDialog[0] = null;
                        break;
                }
            }
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext())
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

    private void editFile(View v) {
        mEditingView = v;
        ResourceBaseModel resourceModel = (ResourceBaseModel) v.getTag();
        mEditingResourceModel = resourceModel;
        if (resourceModel instanceof ResourceModel) {
            showPopupMenu(v);
        } else {
            FrameDialog.show(getChildFragmentManager(), ResourceSelectorFragment.newInstance(new ResourceSelectorFragment.Callback() {
                @Override
                public void cancel() {

                }

                @Override
                public void resourceChecked(ResourceModel resourceModel, QuestionModel questionModel) {
                    editQuestionView(questionModel);
                }
            }, KeyConstants.ResourceType.QUESTION_TYPE));
        }
    }

    private void editFileView(ResourceModel resourceModel) {
        LinearLayout taskView = (LinearLayout) mEditingView.getParent().getParent().getParent().getParent();
        KnowledgeLessonSample knowledgeLessonSample = (KnowledgeLessonSample) taskView.getTag();
        switch (resourceModel.resource_type) {
            case KeyConstants.ResourceType.PPT_TYPE:
                knowledgeLessonSample.ppt_set.remove(mEditingResourceModel);
                knowledgeLessonSample.ppt_set.add(resourceModel);
                break;
            case KeyConstants.ResourceType.IMAGE_TYPE:
                knowledgeLessonSample.image_set.remove(mEditingResourceModel);
                knowledgeLessonSample.image_set.add(resourceModel);
                break;
            case KeyConstants.ResourceType.WORD_TYPE:
                knowledgeLessonSample.doc_set.remove(mEditingResourceModel);
                knowledgeLessonSample.doc_set.add(resourceModel);
                break;
            case KeyConstants.ResourceType.VIDEO_TYPE:
                knowledgeLessonSample.video_set.remove(mEditingResourceModel);
                knowledgeLessonSample.video_set.add(resourceModel);
                break;
        }
        LinearLayout resourceLayout = (LinearLayout) mEditingView.getParent();
        TextView nameView = resourceLayout.findViewById(R.id.resource_name_view);
        TextView deleteView = resourceLayout.findViewById(R.id.task_delete);
        TextView editView = resourceLayout.findViewById(R.id.task_edit);
        deleteView.setTag(resourceModel);
        deleteView.setOnClickListener(this);
        editView.setVisibility(View.GONE);
        nameView.setText(resourceModel.name);
        requestEdit(knowledgeLessonSample);
    }

    private void editQuestionView(QuestionModel questionModel) {
        LinearLayout taskView = (LinearLayout) mEditingView.getParent().getParent().getParent().getParent();
        KnowledgeLessonSample knowledgeLessonSample = (KnowledgeLessonSample) taskView.getTag();

        knowledgeLessonSample.question_set.remove(mEditingResourceModel);
        knowledgeLessonSample.question_set.add(questionModel);
        LinearLayout resourceLayout = (LinearLayout) mEditingView.getParent();
        TextView deleteView = resourceLayout.findViewById(R.id.task_delete);
        TextView editView = resourceLayout.findViewById(R.id.task_edit);
        deleteView.setTag(questionModel);
        deleteView.setOnClickListener(this);
        editView.setTag(questionModel);
        editView.setOnClickListener(this);
        resourceLayout.findViewById(R.id.bottom_selector_layout).setVisibility(View.GONE);
        View questionView = resourceLayout.findViewById(R.id.question_fragment);
        QuestionModelFragment.showQuestionModel(getLayoutInflater(), questionView, questionModel);
        resourceLayout.findViewById(R.id.question_fragment).setVisibility(View.VISIBLE);
        requestEdit(knowledgeLessonSample);
    }

    private void deleteFile(View v) {
        ResourceBaseModel resourceBaseModel = (ResourceBaseModel) v.getTag();
        LinearLayout taskView = (LinearLayout) v.getParent().getParent().getParent().getParent();
        KnowledgeLessonSample knowledgeLessonSample = (KnowledgeLessonSample) taskView.getTag();
        if (resourceBaseModel instanceof ResourceModel) {
            ResourceModel resourceModel = (ResourceModel) resourceBaseModel;
            switch (resourceModel.resource_type) {
                case KeyConstants.ResourceType.PPT_TYPE:
                    knowledgeLessonSample.ppt_set.remove(resourceModel);
                    break;
                case KeyConstants.ResourceType.IMAGE_TYPE:
                    knowledgeLessonSample.image_set.remove(resourceModel);
                    break;
                case KeyConstants.ResourceType.WORD_TYPE:
                    knowledgeLessonSample.doc_set.remove(resourceModel);
                    break;
                case KeyConstants.ResourceType.VIDEO_TYPE:
                    knowledgeLessonSample.video_set.remove(resourceModel);
                    break;
            }
        } else if (resourceBaseModel instanceof QuestionModel) {
            QuestionModel resourceModel = (QuestionModel) resourceBaseModel;
            knowledgeLessonSample.question_set.remove(resourceModel);
        }
        requestEdit(knowledgeLessonSample);
    }

    private void requestEdit(KnowledgeLessonSample knowledgeLessonSample) {
        LessonSampleModel lessonSampleModel = new LessonSampleModel();
        lessonSampleModel.lesson_sample_id = knowledgeLessonSample.lesson_sample_id;
        lessonSampleModel.knowledge_id = knowledgeLessonSample.knowledge_id;
        lessonSampleModel.lesson_type = knowledgeLessonSample.lesson_type;
        lessonSampleModel.number = knowledgeLessonSample.number;
        lessonSampleModel.lesson_sample_name = knowledgeLessonSample.lesson_sample_name;
        lessonSampleModel.status = knowledgeLessonSample.status;

        lessonSampleModel.doc_set = getIdSetFromModelSet(knowledgeLessonSample.doc_set);
        lessonSampleModel.ppt_set = getIdSetFromModelSet(knowledgeLessonSample.ppt_set);
        lessonSampleModel.question_set = getIdSetFromQuestionSet(knowledgeLessonSample.question_set);
        lessonSampleModel.image_set = getIdSetFromModelSet(knowledgeLessonSample.image_set);
        lessonSampleModel.video_set = getIdSetFromModelSet(knowledgeLessonSample.video_set);
        ScopeServer.getInstance().EditLessonSample(lessonSampleModel, requestCallback);

    }

    private List<String> getIdSetFromModelSet(List<ResourceModel> resourceModels) {
        List<String> idSet = new ArrayList<>();
        if (resourceModels != null) {
            for (ResourceModel resourceBaseModel : resourceModels) {
                idSet.add(resourceBaseModel.resource_id);
            }
        }
        return idSet;

    }

    private List<String> getIdSetFromQuestionSet(List<QuestionModel> resourceModels) {
        List<String> idSet = new ArrayList<>();
        if (resourceModels != null) {
            for (QuestionModel resourceBaseModel : resourceModels) {
                idSet.add(resourceBaseModel.question_id);
            }
        }
        return idSet;
    }

    RequestCallback requestCallback = new RequestCallback() {
        @Override
        public void onProgress(boolean b) {

        }

        @Override
        public void onResponse(String body) {
            requestData();
        }

        @Override
        public void onError(String code, String message) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            requestData();
        }
    };

    private void initFilterDialog() {
        FrameDialog.showLittleDialog(getChildFragmentManager(), KnowledgePublishFragment.newInstance(mStatus, new KnowledgePublishFragment.PublishCallback() {
            @Override
            public void publish(int[] checkedPublish) {
                new RequestPublishTask(checkedPublish).execute();
            }
        }));
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
                .setTitle(R.string.please_check_knowledge)
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
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setPositiveButton(R.string.sure, new DialogInterface.OnClickListener() {
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
                    LinearLayout taskLinearLayout = (LinearLayout) getActivity().getLayoutInflater().inflate(R.layout.layout_task_added, null);
                    TextView titleTextView = taskLinearLayout.findViewById(R.id.task_title);
                    titleTextView.setText(lessonSample.lesson_sample_name);
                    taskLinearLayout.setTag(lessonSample);
                    if (lessonSample.doc_set != null) {
                        addResourceDisplayFile(getResources().getString(R.string.documents), taskLinearLayout, lessonSample.doc_set);
                    }
                    if (lessonSample.ppt_set != null) {
                        addResourceDisplayFile(getResources().getString(R.string.ppt), taskLinearLayout, lessonSample.ppt_set);
                    }
                    if (lessonSample.image_set != null) {
                        addResourceDisplayFile(getResources().getString(R.string.photo), taskLinearLayout, lessonSample.image_set);
                    }
                    if (lessonSample.video_set != null) {
                        addResourceDisplayFile(getResources().getString(R.string.micro_course), taskLinearLayout, lessonSample.video_set);
                    }
                    if (lessonSample.question_set != null) {
                        addQuestionDisplayFile(getResources().getString(R.string.exercises), taskLinearLayout, lessonSample.question_set, false);
                        addQuestionDisplayFile(getResources().getString(R.string.answers), taskLinearLayout, lessonSample.question_set, true);
                    }

                    mKnowledgeTasksLayout.addView(taskLinearLayout);
                }
            }
        }
    }

    private void addResourceDisplayFile(String resourceTypeText, LinearLayout taskLinearLayout, List<ResourceModel> resourceList) {
        LinearLayout displayLayout = (LinearLayout) getActivity().getLayoutInflater().inflate(R.layout.layout_resource_item, null);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//                    layoutParams.topMargin = getActivity().getResources().getDimensionPixelSize(R.dimen.activity_vertical_margin);
        displayLayout.setLayoutParams(layoutParams);

        LinearLayout resourcesLayout = displayLayout.findViewById(R.id.files_display_layout);

        TextView resourceTypeView = displayLayout.findViewById(R.id.files_type_tv);
        resourceTypeView.setText(resourceTypeText);
        for (ResourceModel resourceModel : resourceList) {
            addFileView(resourceModel, resourcesLayout);
            displayLayout.setTag(resourceModel.resource_type);
        }
        taskLinearLayout.addView(displayLayout);
    }

    private void addQuestionDisplayFile(String resourceTypeText, LinearLayout taskLinearLayout, List<QuestionModel> resourceList, boolean displayAnswer) {
        LinearLayout displayLayout = (LinearLayout) getActivity().getLayoutInflater().inflate(R.layout.layout_resource_item, null);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//                    layoutParams.topMargin = getActivity().getResources().getDimensionPixelSize(R.dimen.activity_vertical_margin);
        displayLayout.setLayoutParams(layoutParams);

        LinearLayout resourcesLayout = displayLayout.findViewById(R.id.files_display_layout);

        TextView resourceTypeView = displayLayout.findViewById(R.id.files_type_tv);
        resourceTypeView.setText(resourceTypeText);

        if (displayAnswer) {
            AnswerDisplayLayout answerDisplayLayout = resourcesLayout.findViewById(R.id.answer_layout);
            answerDisplayLayout.setVisibility(View.VISIBLE);
            answerDisplayLayout.resetData(resourceList);
            displayLayout.setTag(KeyConstants.ResourceType.ANSWER_TYPE);
        } else {
            for (QuestionModel result : resourceList) {
                onCheckQuestion(result, resourcesLayout);
            }
            displayLayout.setTag(KeyConstants.ResourceType.QUESTION_TYPE);
        }
        taskLinearLayout.addView(displayLayout);
    }

    private void onCheckQuestion(QuestionModel questionModel, LinearLayout linearLayout) {
        LinearLayout resourceLayout = (LinearLayout) getActivity().getLayoutInflater().inflate(R.layout.layout_resource, null);
        resourceLayout.findViewById(R.id.resource_icon).setVisibility(View.GONE);
        resourceLayout.findViewById(R.id.resource_name_view).setVisibility(View.GONE);
        TextView deleteView = resourceLayout.findViewById(R.id.task_delete);
        TextView editView = resourceLayout.findViewById(R.id.task_edit);
        deleteView.setTag(questionModel);
        deleteView.setOnClickListener(this);
        editView.setTag(questionModel);
        editView.setOnClickListener(this);
        resourceLayout.findViewById(R.id.bottom_selector_layout).setVisibility(View.GONE);
        View questionView = resourceLayout.findViewById(R.id.question_fragment);
        QuestionModelFragment.showQuestionModel(getLayoutInflater(), questionView, questionModel);
        resourceLayout.findViewById(R.id.question_fragment).setVisibility(View.VISIBLE);
        linearLayout.addView(resourceLayout);
    }

    private void displayAnswer(QuestionModel questionModel, LinearLayout linearLayout) {
        LinearLayout resourceLayout = (LinearLayout) getActivity().getLayoutInflater().inflate(R.layout.layout_resource, null);
        resourceLayout.findViewById(R.id.resource_icon).setVisibility(View.GONE);
        resourceLayout.findViewById(R.id.task_delete).setVisibility(View.GONE);
        resourceLayout.findViewById(R.id.task_edit).setVisibility(View.GONE);
        resourceLayout.findViewById(R.id.bottom_selector_layout).setVisibility(View.GONE);

        TextView answerTextView = resourceLayout.findViewById(R.id.resource_name_view);
        StringBuffer answerText = new StringBuffer();
        answerText.append(questionModel.context.OrderIndex + ".");
        for (OptionData optionData : questionModel.context.Options) {
            String option = AppUtils.OPTION_NO[optionData.OrderIndex - 1];
            if (option.equals(questionModel.context.Answer)) {
                answerText.append("<font color='#FF0000'>");
            }
            answerText.append("[");
            answerText.append(option);
            answerText.append("]");
            if (option.equals(questionModel.context.Answer)) {
                answerText.append("</font>");
            }
        }
        answerTextView.setText(Html.fromHtml(answerText.toString()));
        linearLayout.addView(resourceLayout);
    }

    private void addFileView(ResourceModel result, LinearLayout linearLayout) {
        LinearLayout resourceLayout = (LinearLayout) getActivity().getLayoutInflater().inflate(R.layout.layout_resource, null);
        ImageView iconView = resourceLayout.findViewById(R.id.resource_icon);
        TextView nameView = resourceLayout.findViewById(R.id.resource_name_view);
        TextView deleteView = resourceLayout.findViewById(R.id.task_delete);
        TextView editView = resourceLayout.findViewById(R.id.task_edit);
        deleteView.setTag(result);
        deleteView.setOnClickListener(this);
        editView.setTag(result);
        editView.setOnClickListener(this);
        nameView.setText(result.name);
        switch (result.resource_type) {
            case KeyConstants.ResourceType.PPT_TYPE:
                iconView.setImageResource(R.drawable.ppt_icon);
                break;
            case KeyConstants.ResourceType.IMAGE_TYPE:
                iconView.setImageResource(R.drawable.image_icon);
                break;
            case KeyConstants.ResourceType.WORD_TYPE:
                iconView.setImageResource(R.drawable.word_icon);
                break;
            case KeyConstants.ResourceType.VIDEO_TYPE:
                iconView.setImageResource(R.drawable.video_icon);
                break;
        }
        linearLayout.addView(resourceLayout);
    }

    @SuppressLint("RestrictedApi")
    private void showPopupMenu(View view) {
        // 这里的view代表popupMenu需要依附的view
        PopupMenu popupMenu = new PopupMenu(getActivity(), view);
        try {
            Field field = popupMenu.getClass().getDeclaredField("mPopup");
            field.setAccessible(true);
            MenuPopupHelper mHelper = (MenuPopupHelper) field.get(popupMenu);
            mHelper.setForceShowIcon(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 获取布局文件
        popupMenu.getMenuInflater().inflate(R.menu.upload_file, popupMenu.getMenu());

        // 通过上面这几行代码，就可以把控件显示出来了
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.from_local:
                        GetFileHelper.fileSelector(getActivity(), KnowledgeEditingFragment.this, false, false);
                        break;
                    case R.id.from_resource:
                        FrameDialog.show(getChildFragmentManager(), ResourceSelectorFragment.newInstance(new ResourceSelectorFragment.Callback() {
                            @Override
                            public void cancel() {

                            }

                            @Override
                            public void resourceChecked(ResourceModel resourceModel, QuestionModel questionModel) {
                                editFileView(resourceModel);
                            }
                        }, ((ResourceModel) mEditingResourceModel).resource_type));
                        break;
                }
                return true;
            }
        });
        popupMenu.setOnDismissListener(new PopupMenu.OnDismissListener() {
            @Override
            public void onDismiss(PopupMenu menu) {
                // 控件消失时的事件
            }
        });
        popupMenu.show();

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
            new UploadMarkTask(imagePath, file.getName(), ((ResourceModel) mEditingResourceModel).resource_type, 1).execute();
        } else {
            Toast.makeText(getActivity(), R.string.select_pic_again, Toast.LENGTH_SHORT).show();
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
            onLoading(true);
        }


        @Override
        protected String doInBackground(Void... strings) {
            UserData userData = ExternalParam.getInstance().getUserData();
            TeacherData teacherData = (TeacherData) userData.getUserData();
            File file = new File(filePath);
            String newFileName = StringUtils.getFormatDate(new Date());
            String suffix = fileName.substring(fileName.lastIndexOf(".") + 1);
            file.renameTo(new File(file.getParent() + "/" + newFileName + "." + suffix));
            return ScopeServer.getInstance().resourceUpload(file.getParent() + "/" + newFileName + "." + suffix, teacherData.TeacherID, mKnowledgeModel.teaching_material_id, fileName, resourceType, level);
        }

        @Override
        protected void onPostExecute(String response) {
            try {
                JSONObject json = new JSONObject(response);
                String errorCode = json.optString("errorCode");
                if (!TextUtils.isEmpty(errorCode) && !errorCode.equals("0")) {
                    Toast.makeText(KnowledgeEditingFragment.this.getContext(), json.optString("errorMessage"), Toast.LENGTH_SHORT).show();
                } else {
                    ResourceModel model = new ResourceModel();
                    model.parse(model, response);

                    onLoading(false);
                    editFileView(model);
                }
            } catch (JSONException e) {
                Toast.makeText(KnowledgeEditingFragment.this.getContext(), R.string.upload_fail, Toast.LENGTH_SHORT).show();
            }
        }
    }

    public interface Callback {
        void onBack();
    }
}
