package com.tanhd.rollingclass.fragments.kowledge;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tanhd.rollingclass.R;
import com.tanhd.rollingclass.activity.DatasActivity;
import com.tanhd.rollingclass.activity.DocumentEditActivity;

public class KnowledgeEditingFragment extends Fragment implements View.OnClickListener {

    private KnowledgeEditingFragment.Callback mListener;
    private TextView mPublishButton;
    private TextView mFinishButton;
    private TextView mKnowledgeNameTextView;
    private TextView mKnowledgeNameEditView;
    private EditText mKnowledgeNameEditText;
    private TextView mKnowledgeAddButton;
    private LinearLayout mKnowledgeTasksLayout;

    private String mKnowledgePointName;

    public static KnowledgeEditingFragment newInstance(String KnowledgePointName, KnowledgeEditingFragment.Callback callback) {
        KnowledgeEditingFragment page = new KnowledgeEditingFragment();
        page.setListener(callback);
        Bundle args = new Bundle();
        args.putString(DocumentEditActivity.PARAM_KNOWLEDGE_POINT_NAME, KnowledgePointName);
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
        mKnowledgePointName = args.getString(DocumentEditActivity.PARAM_KNOWLEDGE_POINT_NAME);
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
        mKnowledgeNameTextView.setOnClickListener(this);
        mKnowledgeNameEditView.setOnClickListener(this);
        mKnowledgeAddButton.setOnClickListener(this);

        mKnowledgeNameEditText.setText(mKnowledgePointName);
        mKnowledgeNameTextView.setText(mKnowledgePointName);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.knowledge_name_edit:
                mKnowledgeNameTextView.setVisibility(View.GONE);
                mKnowledgeNameEditView.setVisibility(View.GONE);
                mKnowledgeNameEditText.setVisibility(View.VISIBLE);
                break;
        }
    }

    public interface  Callback {
        void onBack();
    }
}
