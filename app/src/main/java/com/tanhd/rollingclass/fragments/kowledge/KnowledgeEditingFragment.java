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
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.tanhd.rollingclass.R;
import com.tanhd.rollingclass.activity.DocumentEditActivity;
import com.tanhd.rollingclass.base.BaseFragment;
import com.tanhd.rollingclass.db.KeyConstants;
import com.tanhd.rollingclass.db.model.EventTag;
import com.tanhd.rollingclass.fragments.FrameDialog;
import com.tanhd.rollingclass.fragments.pages.ResourceSelectorFragment;
import com.tanhd.rollingclass.fragments.resource.QuestionModelFragment;
import com.tanhd.rollingclass.server.RequestCallback;
import com.tanhd.rollingclass.server.ScopeServer;
import com.tanhd.rollingclass.server.data.ExternalParam;
import com.tanhd.rollingclass.server.data.KnowledgeDetailMessage;
import com.tanhd.rollingclass.server.data.KnowledgeLessonSample;
import com.tanhd.rollingclass.server.data.KnowledgeModel;
import com.tanhd.rollingclass.server.data.LessonSampleModel;
import com.tanhd.rollingclass.server.data.QuestionModel;
import com.tanhd.rollingclass.server.data.ResourceBaseModel;
import com.tanhd.rollingclass.server.data.ResourceModel;
import com.tanhd.rollingclass.server.data.SyncSampleToClassRequest;
import com.tanhd.rollingclass.server.data.TeacherData;
import com.tanhd.rollingclass.server.data.UserData;
import com.tanhd.rollingclass.utils.GetFileHelper;
import com.tanhd.rollingclass.utils.StringUtils;
import com.tanhd.rollingclass.utils.ToastUtil;
import com.tanhd.rollingclass.views.DefaultDialog;
import com.tanhd.rollingclass.views.PopUploadFile;
import com.tanhd.rollingclass.views.SynPointDialog;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 编辑 课前| 新增任务
 */
public class KnowledgeEditingFragment extends BaseFragment implements View.OnClickListener, KnowledgeAddTaskFragment.Callback {

    public static final String PARAM_KNOWLEDGE_DETAIL_DATA = "PARAM_KNOWLEDGE_DETAIL_DATA";
    public static final String PARAM_KNOWLEDGE_DETAIL_STATUS = "PARAM_KNOWLEDGE_DETAIL_STATUS";
    private KnowledgeEditingFragment.Callback mListener;

    private KnowledgeAddTaskFragment mAddTaskFragment;

    private TextView mPublishButton;
    private TextView mFinishButton;
    private TextView mKnowledgeNameEditView;
    private EditText mKnowledgeNameEditText;
    private TextView mKnowledgeAddButton;
    private LinearLayout mKnowledgeTasksLayout;
    private View mAddFragmentView;
    private ScrollView scrollView;

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

    private List<KnowledgeLessonSample> mDataList;
    private ResourceBaseModel mEditingResourceModel;
    private View mEditingView;
    private boolean isEdit; //是否编辑


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
        mKnowledgeNameEditText = view.findViewById(R.id.knowledge_name_et);
        mKnowledgeNameEditView = view.findViewById(R.id.knowledge_name_edit);
        mKnowledgeTasksLayout = view.findViewById(R.id.knowledge_tasks_layout);
        mKnowledgeAddButton = view.findViewById(R.id.knowledge_add_button);
        mAddFragmentView = view.findViewById(R.id.fragment_add_task);
        mProgressBar = view.findViewById(R.id.progressbar);
        scrollView = view.findViewById(R.id.scrollView);

        mSyncFreClassCheckBox = view.findViewById(R.id.sync_fre_class_cb);
        mSyncInClassCheckBox = view.findViewById(R.id.sync_in_class_cb);
        mSyncAfterClassCheckBox = view.findViewById(R.id.sync_after_class_cb);

        refreshStautsView();

        mSyncFreClassCheckBox.setOnClickListener(this);
        mSyncInClassCheckBox.setOnClickListener(this);
        mSyncAfterClassCheckBox.setOnClickListener(this);

        mPublishButton.setOnClickListener(this);
        mFinishButton.setOnClickListener(this);
        mKnowledgeNameEditView.setOnClickListener(this);
        mKnowledgeAddButton.setOnClickListener(this);
        setViewStatus();
    }

    /**
     * 刷新状态 View
     */
    private void refreshStautsView() {
        if (mStatus == KeyConstants.KnowledgeStatus.FRE_CLASS) { //前
            mSyncFreClassCheckBox.setVisibility(View.GONE);
            mSyncInClassCheckBox.setVisibility(View.VISIBLE);
            mSyncAfterClassCheckBox.setVisibility(View.VISIBLE);
        }
        if (mStatus == KeyConstants.KnowledgeStatus.AT_CLASS) { //中
            mSyncFreClassCheckBox.setVisibility(View.VISIBLE);
            mSyncInClassCheckBox.setVisibility(View.GONE);
            mSyncAfterClassCheckBox.setVisibility(View.VISIBLE);
        }
        if (mStatus == KeyConstants.KnowledgeStatus.AFTER_CLASS) { //后
            mSyncFreClassCheckBox.setVisibility(View.VISIBLE);
            mSyncInClassCheckBox.setVisibility(View.VISIBLE);
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

    private void setViewStatus() {
        if ((mStatus == KeyConstants.KnowledgeStatus.FRE_CLASS && mKnowledgeDetailMessage.class_before == 1)
                || (mStatus == KeyConstants.KnowledgeStatus.AT_CLASS && mKnowledgeDetailMessage.class_process == 1)
                || (mStatus == KeyConstants.KnowledgeStatus.AFTER_CLASS && mKnowledgeDetailMessage.class_after == 1)) {
            mKnowledgeAddButton.setVisibility(View.GONE);
            mSyncAfterClassCheckBox.setVisibility(View.GONE);
            mSyncInClassCheckBox.setVisibility(View.GONE);
            mSyncFreClassCheckBox.setVisibility(View.GONE);
            mKnowledgeNameEditView.setVisibility(View.GONE);
        } else {
            mKnowledgeAddButton.setVisibility(View.VISIBLE);
            mSyncAfterClassCheckBox.setVisibility(View.VISIBLE);
            mSyncInClassCheckBox.setVisibility(View.VISIBLE);
            mSyncFreClassCheckBox.setVisibility(View.VISIBLE);
            mKnowledgeNameEditView.setVisibility(View.VISIBLE);
        }
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
            mSyncInClassCheckBox.setEnabled(false);
        } else {
            mSyncInClassCheckBox.setEnabled(true);
        }
        if (mKnowledgeDetailMessage.class_after == 1) {
            mSyncAfterClassCheckBox.setEnabled(false);
        } else {
            mSyncAfterClassCheckBox.setEnabled(true);
        }
    }

    private void initData() {
        if (mKnowledgeDetailMessage != null) {
            mKnowledgeNameEditText.setEnabled(false);
            mKnowledgeNameEditText.setBackgroundResource(R.color.transparent);
            mKnowledgeNameEditText.setText(mKnowledgeDetailMessage.knowledge_point_name);
            if (!TextUtils.isEmpty(mKnowledgeDetailMessage.knowledge_id)) {
                requestData();
            }

            setViewStatus();
        }
    }

    public void resetData(KnowledgeModel knowledgeModel, KnowledgeDetailMessage insertKnowledgeResponse, int status) {
        Bundle args = new Bundle();
        args.putSerializable(DocumentEditActivity.PARAM_TEACHING_MATERIAL_DATA, knowledgeModel);
        args.putSerializable(PARAM_KNOWLEDGE_DETAIL_DATA, insertKnowledgeResponse);
        args.putSerializable(PARAM_KNOWLEDGE_DETAIL_STATUS, status);
        setArguments(args);

        initParams();
        refreshStautsView();
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
                isEdit = !isEdit;

                if (isEdit) { //是编辑态
                    mKnowledgeNameEditView.setText(R.string.finish);
                    mKnowledgeNameEditText.setEnabled(true);
                    mKnowledgeNameEditText.setBackgroundResource(R.drawable.edittext_bg);
                } else {
                    mKnowledgeNameEditText.setEnabled(false);
                    mKnowledgeNameEditText.setBackgroundResource(R.color.transparent);
                    editTitle();
                }
                break;
            case R.id.knowledge_add_button: //+任务
                if (mAddFragmentView.getVisibility() == View.VISIBLE) {
                    showDialog(getString(R.string.adding_task_warning));
                } else {
                    addEditingFragment();
                }
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        scrollView.fullScroll(ScrollView.FOCUS_DOWN);
                    }
                });
                break;
            case R.id.sync_fre_class_cb:
                if (mAddFragmentView.getVisibility() == View.VISIBLE) {
                    showDialog(getString(R.string.adding_task_warning));
                } else {
                    mSyncFreClassCheckBox.setChecked(true);
                    showLessonSampleDialog(KeyConstants.KnowledgeStatus.FRE_CLASS);
                }
                break;
            case R.id.sync_in_class_cb:
                if (mAddFragmentView.getVisibility() == View.VISIBLE) {
                    showDialog(getString(R.string.adding_task_warning));
                } else {
                    mSyncInClassCheckBox.setChecked(true);
                    showLessonSampleDialog(KeyConstants.KnowledgeStatus.AT_CLASS);
                }
                break;
            case R.id.sync_after_class_cb:
                if (mAddFragmentView.getVisibility() == View.VISIBLE) {
                    showDialog(getString(R.string.adding_task_warning));
                } else {
                    mSyncAfterClassCheckBox.setChecked(true);
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
        new DefaultDialog(getResources().getString(R.string.dialog_tile), getResources().getString(R.string.delete_task_warning), "", "",null, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteFile(v);
            }
        }).show(getChildFragmentManager(),"DefaultDialog");
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
        questionView.setBackgroundColor(getResources().getColor(R.color.transparent));
        QuestionModelFragment.showQuestionModel(getLayoutInflater(), questionView, questionModel);
        questionView.setVisibility(View.VISIBLE);
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
        if ((knowledgeLessonSample.ppt_set != null && knowledgeLessonSample.ppt_set.size() > 0)
                || (knowledgeLessonSample.image_set != null && knowledgeLessonSample.image_set.size() > 0)
                || (knowledgeLessonSample.doc_set != null && knowledgeLessonSample.doc_set.size() > 0)
                || (knowledgeLessonSample.video_set != null && knowledgeLessonSample.video_set.size() > 0)
                || (knowledgeLessonSample.question_set != null && knowledgeLessonSample.question_set.size() > 0)) {
            requestEdit(knowledgeLessonSample);
        } else {
            ScopeServer.getInstance().DeleteLessonSample(knowledgeLessonSample.lesson_sample_id, requestCallback);
        }
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
            ToastUtil.show(message);
            requestData();
        }
    };

    private void initFilterDialog() {
        FrameDialog.showLittleDialog(getChildFragmentManager(), KnowledgePublishFragment.newInstance(mStatus, new int[]{mKnowledgeDetailMessage.class_before, mKnowledgeDetailMessage.class_process, mKnowledgeDetailMessage.class_after},
                new KnowledgePublishFragment.PublishCallback() {
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

        final SynPointDialog synPointDialog = new SynPointDialog(mDataList);
        synPointDialog.setOkListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<KnowledgeLessonSample> selectData = synPointDialog.getSelectData();
                if (selectData.isEmpty()) {
                    Toast.makeText(getActivity(), "请至少选择一项进行同步", Toast.LENGTH_SHORT).show();
                    return;
                }

                for (int i = 0; i < selectData.size(); i++) {
                    checkedIdList.add(selectData.get(i).lesson_sample_id);
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
        });
        synPointDialog.show(getChildFragmentManager(),"synPointDialog");
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
                mKnowledgeNameEditView.setText(R.string.edit);
                EventBus.getDefault().post(new EventTag(EventTag.REFRESH_CASE));
            }

            @Override
            public void onError(String code, String message) {
                ToastUtil.show(message);
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
                Toast.makeText(getActivity(), resultCode, Toast.LENGTH_SHORT).show();
            } else {
                EventBus.getDefault().post(new EventTag(EventTag.REFRESH_CASE));
                ToastUtil.show(R.string.publish_success);
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
            if (TextUtils.isEmpty(resultCode)) {
                ToastUtil.show(R.string.sync_success);
            } else {
                ToastUtil.show(resultCode);
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
                setViewStatus();

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

        if ((mStatus == KeyConstants.KnowledgeStatus.FRE_CLASS && mKnowledgeDetailMessage.class_before == 1)
                || (mStatus == KeyConstants.KnowledgeStatus.AT_CLASS && mKnowledgeDetailMessage.class_process == 1)
                || (mStatus == KeyConstants.KnowledgeStatus.AFTER_CLASS && mKnowledgeDetailMessage.class_after == 1)) {
            deleteView.setVisibility(View.GONE);
            editView.setVisibility(View.GONE);
        }
        resourceLayout.findViewById(R.id.bottom_selector_layout).setVisibility(View.GONE);
        View questionView = resourceLayout.findViewById(R.id.question_fragment);
        questionView.setBackgroundColor(getResources().getColor(R.color.transparent));
        QuestionModelFragment.showQuestionModel(getLayoutInflater(), questionView, questionModel);
        questionView.setVisibility(View.VISIBLE);
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

        if ((mStatus == KeyConstants.KnowledgeStatus.FRE_CLASS && mKnowledgeDetailMessage.class_before == 1)
                || (mStatus == KeyConstants.KnowledgeStatus.AT_CLASS && mKnowledgeDetailMessage.class_process == 1)
                || (mStatus == KeyConstants.KnowledgeStatus.AFTER_CLASS && mKnowledgeDetailMessage.class_after == 1)) {
            deleteView.setVisibility(View.GONE);
            editView.setVisibility(View.GONE);
        }
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
        new PopUploadFile(getActivity()).setLocalListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { //本地
                GetFileHelper.fileSelector(getActivity(), KnowledgeEditingFragment.this, false, false);
            }
        }).setResourceListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { //资源库
                FrameDialog.show(getChildFragmentManager(), ResourceSelectorFragment.newInstance(new ResourceSelectorFragment.Callback() {
                    @Override
                    public void cancel() {

                    }

                    @Override
                    public void resourceChecked(ResourceModel resourceModel, QuestionModel questionModel) {
                        editFileView(resourceModel);
                    }
                }, ((ResourceModel) mEditingResourceModel).resource_type));
            }
        }).showAsDropDown(view);

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
                    ToastUtil.show(json.optString("errorMessage"));
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
