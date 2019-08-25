package com.tanhd.rollingclass.fragments.kowledge;

import android.app.Dialog;
import android.content.DialogInterface;
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
import com.tanhd.rollingclass.fragments.ShowPageFragment;
import com.tanhd.rollingclass.server.data.InsertKnowledgeResponse;
import com.tanhd.rollingclass.server.data.KnowledgeModel;
import com.tanhd.rollingclass.server.data.ResourceUpload;
import com.tanhd.rollingclass.utils.GetFileHelper;

import java.io.File;

public class KnowledgeEditingFragment extends Fragment implements View.OnClickListener {

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

    private KnowledgeModel mKnowledgeModel;
    private InsertKnowledgeResponse mInsertKnowledgeResponse;
    /**
     * 1. ppt 2. doc 3. image 4. 微课 5. 习题
     */
    private int mResourceCode;
    private ResourceUpload mResourceModel;

    /**
     * 1.课前；2.课时；3.课后
     */
    private int mStatus;

    /**
     *
     * @param knowledgeModel 所属教材章节的参数
     * @param insertKnowledgeResponse 所属课时的参数
     * @param status 1.课前；2.课时；3.课后
     * @param callback
     * @return
     */

    public static KnowledgeEditingFragment newInstance(KnowledgeModel knowledgeModel, InsertKnowledgeResponse insertKnowledgeResponse, int status, KnowledgeEditingFragment.Callback callback) {
        KnowledgeEditingFragment page = new KnowledgeEditingFragment();
        page.setListener(callback);
        Bundle args = new Bundle();
        args.putSerializable(DocumentEditActivity.PARAM_KNOWLEDGE_DATA, knowledgeModel);
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
        initFragment();
        return view;
    }

    private void initParams() {
        Bundle args = getArguments();
        mKnowledgeModel = (KnowledgeModel) args.getSerializable(DocumentEditActivity.PARAM_KNOWLEDGE_DATA);
        mInsertKnowledgeResponse = (InsertKnowledgeResponse) args.getSerializable(PARAM_KNOWLEDGE_DETAIL_DATA);
        mStatus = args.getInt(PARAM_KNOWLEDGE_DETAIL_STATUS);
    }

    private void initFragment() {
        mAddTaskFragment = KnowledgeAddTaskFragment.newInstance(mKnowledgeModel, mInsertKnowledgeResponse,mStatus,new KnowledgeAddTaskFragment.Callback() {
            @Override
            public void onBack() {
                getFragmentManager().beginTransaction().hide(mAddTaskFragment).commit();
            }
        });
        getFragmentManager().beginTransaction().replace(R.id.fragment_add_task, mAddTaskFragment).commit();
    }

    private void initViews(View view) {
        mPublishButton = view.findViewById(R.id.knowledge_publish_button);
        mFinishButton = view.findViewById(R.id.knowledge_finish_button);
        mKnowledgeNameTextView = view.findViewById(R.id.knowledge_name);
        mKnowledgeNameEditText = view.findViewById(R.id.knowledge_name_et);
        mKnowledgeNameEditView = view.findViewById(R.id.knowledge_name_edit);
        mKnowledgeTasksLayout = view.findViewById(R.id.knowledge_tasks_layout);
        mKnowledgeAddButton = view.findViewById(R.id.knowledge_add_button);

        mPublishButton.setOnClickListener(this);
        mFinishButton.setOnClickListener(this);
        mKnowledgeNameEditView.setOnClickListener(this);
        mKnowledgeAddButton.setOnClickListener(this);

        mKnowledgeNameEditText.setText(mKnowledgeModel.knowledge_point_name);
        mKnowledgeNameTextView.setText(mKnowledgeModel.knowledge_point_name);
    }

    private void requestData(){

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.knowledge_publish_button:

                break;
            case R.id.knowledge_finish_button:
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

    public interface Callback {
        void onBack();
    }
}
