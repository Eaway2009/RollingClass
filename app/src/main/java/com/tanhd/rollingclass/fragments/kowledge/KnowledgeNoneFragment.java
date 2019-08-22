package com.tanhd.rollingclass.fragments.kowledge;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
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
import com.tanhd.rollingclass.server.RequestCallback;
import com.tanhd.rollingclass.server.ScopeServer;
import com.tanhd.rollingclass.server.data.KnowledgeData;

public class KnowledgeNoneFragment extends Fragment implements View.OnClickListener {

    private String KnowledgeID;
    private String ChapterName;
    private String SectionName;
    private String TeachingMaterialID;
    private String Remark;

    private KnowledgeNoneFragment.Callback mListener;
    private EditText mKnowledgeNameEditText;
    private TextView mKnowledgeAddButton;
    private View mAddButtonsLayout;

    public static KnowledgeNoneFragment newInstance(String KnowledgeID, String ChapterName, String SectionName, String TeachingMaterialID, String Remark, KnowledgeNoneFragment.Callback callback) {
        Bundle args = new Bundle();
        args.putString(DocumentEditActivity.PARAM_KNOWLEDGE_ID, KnowledgeID);
        args.putString(DocumentEditActivity.PARAM_CHAPTER_NAME, ChapterName);
        args.putString(DocumentEditActivity.PARAM_SECTION_NAME, SectionName);
        args.putString(DocumentEditActivity.PARAM_TEACHING_MATERIAL_ID, TeachingMaterialID);
        args.putString(DocumentEditActivity.PARAM_REMARK, Remark);
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
        KnowledgeID = args.getString(DocumentEditActivity.PARAM_KNOWLEDGE_ID);
        ChapterName = args.getString(DocumentEditActivity.PARAM_CHAPTER_NAME);
        SectionName = args.getString(DocumentEditActivity.PARAM_SECTION_NAME);
        TeachingMaterialID = args.getString(DocumentEditActivity.PARAM_TEACHING_MATERIAL_ID);
        Remark = args.getString(DocumentEditActivity.PARAM_REMARK);
    }

    private void initViews(View view) {
        mKnowledgeNameEditText = view.findViewById(R.id.knowledge_name_et);
        mKnowledgeAddButton = view.findViewById(R.id.knowledge_add_button);
        mAddButtonsLayout = view.findViewById(R.id.knowledge_add_button_layout);

        mKnowledgeAddButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.knowledge_add_button:
                onClickAddTask();
                break;
        }

    }

    private void onClickAddTask(){
        if (TextUtils.isEmpty(mKnowledgeNameEditText.getText().toString().trim())) {
            Toast.makeText(getActivity(), "请先输入课时名称再添加任务，谢谢", Toast.LENGTH_LONG).show();
        } else {
            KnowledgeData knowledgeData = new KnowledgeData();
            knowledgeData.KnowledgeID = KnowledgeID;
            knowledgeData.KnowledgePointName = mKnowledgeNameEditText.getText().toString();
            knowledgeData.ChapterName = ChapterName;
            knowledgeData.SectionName = SectionName;
            knowledgeData.TeachingMaterialID = TeachingMaterialID;
            knowledgeData.Remark = Remark;

//            ScopeServer.getInstance().InsertKnowledge(knowledgeData, new RequestCallback() {
//                @Override
//                public void onProgress(boolean b) {
//
//                }
//
//                @Override
//                public void onResponse(String body) {
            if (mListener != null) {
                mListener.onAddSuccess(mKnowledgeNameEditText.getText().toString());
            }
//                }
//
//                @Override
//                public void onError(String code, String message) {
//                    Toast.makeText(getActivity().getApplicationContext(), "课时名称添加失败，请稍候重试 " + message, Toast.LENGTH_LONG).show();
//                }
//            });
        }
    }

    public interface Callback {
        void onAddSuccess(String KnowledgePointName);
    }
}
