package com.tanhd.rollingclass.fragments.kowledge;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.tanhd.rollingclass.R;
import com.tanhd.rollingclass.activity.DocumentEditActivity;
import com.tanhd.rollingclass.server.data.KnowledgeDetailMessage;
import com.tanhd.rollingclass.server.data.KnowledgeModel;
import com.tanhd.rollingclass.server.RequestCallback;
import com.tanhd.rollingclass.server.ScopeServer;
import com.tanhd.rollingclass.utils.ToastUtil;

/**
 * 新建学案-课前
 */
public class KnowledgeNoneFragment extends Fragment implements View.OnClickListener {

    private KnowledgeModel mKnowledgeModel;

    private KnowledgeNoneFragment.Callback mListener;
    private EditText mKnowledgeNameEditText;
    private TextView mKnowledgeAddButton;
    private View mAddButtonsLayout;

    private boolean mRequesting=false;

    public static KnowledgeNoneFragment newInstance(KnowledgeModel knowledgeModel, KnowledgeNoneFragment.Callback callback) {
        Bundle args = new Bundle();
        args.putSerializable(DocumentEditActivity.PARAM_TEACHING_MATERIAL_DATA, knowledgeModel);
        KnowledgeNoneFragment page = new KnowledgeNoneFragment();
        page.setArguments(args);
        page.setListener(callback);
        return page;
    }

    private void setListener(KnowledgeNoneFragment.Callback listener) {
        mListener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.page_none_knowledge, container, false);
        initParams();
        initViews(view);
        return view;
    }

    private void initParams() {
        Bundle args = getArguments();
        mKnowledgeModel = (KnowledgeModel) args.getSerializable(DocumentEditActivity.PARAM_TEACHING_MATERIAL_DATA);
    }

    private void initViews(View view) {
        mKnowledgeNameEditText = view.findViewById(R.id.knowledge_name_et);
        mKnowledgeAddButton = view.findViewById(R.id.knowledge_add_button);
        mAddButtonsLayout = view.findViewById(R.id.knowledge_add_button_layout);

        mKnowledgeAddButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.knowledge_add_button:     //添加任务
                onClickAddTask();
                break;
        }

    }

    private void onClickAddTask() {
        if (TextUtils.isEmpty(mKnowledgeNameEditText.getText().toString().trim())) {
            ToastUtil.show(R.string.toast_class_name_empty);
        } else {
            mKnowledgeModel.knowledge_point_name = mKnowledgeNameEditText.getText().toString();
            mKnowledgeAddButton.setEnabled(false);
            request();
        }
    }

    private  void request(){
        if(mRequesting){
            return;
        }
        mRequesting = true;
        ScopeServer.getInstance().InsertKnowledge(mKnowledgeModel, new RequestCallback() {
            @Override
            public void onProgress(boolean b) {

            }

            @Override
            public void onResponse(String body) {
                KnowledgeDetailMessage response = (KnowledgeDetailMessage) ScopeServer.getInstance().jsonToModel(KnowledgeDetailMessage.class.getName(),body);
                mListener.onAddSuccess(mKnowledgeModel, response);
                mRequesting = false;
                mKnowledgeAddButton.setEnabled(true);
            }

            @Override
            public void onError(String code, String message) {
                Toast.makeText(getActivity().getApplicationContext(), "课时名称添加失败，请稍候重试 ", Toast.LENGTH_LONG).show();
                mRequesting = false;
                mKnowledgeAddButton.setEnabled(true);
            }
        });}

    public interface Callback {
        void onAddSuccess(KnowledgeModel model, KnowledgeDetailMessage response);
    }
}
