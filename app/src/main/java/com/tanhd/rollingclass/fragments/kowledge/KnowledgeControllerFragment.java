package com.tanhd.rollingclass.fragments.kowledge;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tanhd.rollingclass.R;
import com.tanhd.rollingclass.activity.DocumentEditActivity;

public class KnowledgeControllerFragment extends Fragment implements View.OnClickListener, KnowledgeNoneFragment.Callback, KnowledgeEditingFragment.Callback {

    private static final int MODULE_ID_NEW_KNOWLEDGE = 1;
    private static final int MODULE_ID_EDIT_TASKS = 2;

    private String KnowledgeID;
    private String ChapterName;
    private String SectionName;
    private String TeachingMaterialID;
    private String Remark;

    private int mCurrentShowModuleId = -1;
    private KnowledgeNoneFragment mKnowledgeNoneFragment;
    private KnowledgeEditingFragment mKnowledgeEditingFragment;

    private View mBackButton;
    private View mFreClassItemView;
    private View mAtClassItemView;
    private View mAfterClassItemView;
    private Callback mCallback;

    public static KnowledgeControllerFragment newInstance(String KnowledgeID, String ChapterName, String SectionName, String TeachingMaterialID, String Remark, Callback callback) {
        Bundle args = new Bundle();
        args.putString(DocumentEditActivity.PARAM_KNOWLEDGE_ID, KnowledgeID);
        args.putString(DocumentEditActivity.PARAM_CHAPTER_NAME, ChapterName);
        args.putString(DocumentEditActivity.PARAM_SECTION_NAME, SectionName);
        args.putString(DocumentEditActivity.PARAM_TEACHING_MATERIAL_ID, TeachingMaterialID);
        args.putString(DocumentEditActivity.PARAM_REMARK, Remark);
        KnowledgeControllerFragment page = new KnowledgeControllerFragment();
        page.setArguments(args);
        page.setCallback(callback);
        return page;
    }

    public void setCallback(Callback callback) {
        this.mCallback = callback;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.page_knowledge_controller, container, false);
        initParams();
        showKnowledgeNoneFragment();
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
        mBackButton = view.findViewById(R.id.back_button);
        mFreClassItemView = view.findViewById(R.id.fre_class_item);
        mAtClassItemView = view.findViewById(R.id.at_class_item);
        mAfterClassItemView = view.findViewById(R.id.after_class_item);

        mBackButton.setOnClickListener(this);
        mFreClassItemView.setOnClickListener(this);
        mAtClassItemView.setOnClickListener(this);
        mAfterClassItemView.setOnClickListener(this);
    }

    public void showKnowledgeNoneFragment() {
        if (mCurrentShowModuleId == MODULE_ID_NEW_KNOWLEDGE) {
            return;
        }
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        if (mKnowledgeNoneFragment == null) {
            mKnowledgeNoneFragment = KnowledgeNoneFragment.newInstance(KnowledgeID, ChapterName, SectionName, TeachingMaterialID, Remark, this);
            transaction.add(R.id.content_layout, mKnowledgeNoneFragment);
        }
        if (mKnowledgeEditingFragment != null) {
            transaction.hide(mKnowledgeEditingFragment);
        }
        transaction.show(mKnowledgeNoneFragment);
        transaction.commitAllowingStateLoss();
        mCurrentShowModuleId = MODULE_ID_NEW_KNOWLEDGE;
    }

    /**
     * [展示指定Id的页面]<BR>
     */
    public void showEditingFragment(String KnowledgePointName) {
        if (mCurrentShowModuleId == MODULE_ID_EDIT_TASKS) {
            return;
        }
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        if (mKnowledgeEditingFragment == null) {
            mKnowledgeEditingFragment = KnowledgeEditingFragment.newInstance(KnowledgePointName, this);
            transaction.add(R.id.content_layout, mKnowledgeEditingFragment);
        }
        if (mKnowledgeNoneFragment != null) {
            transaction.hide(mKnowledgeNoneFragment);
        }
        transaction.show(mKnowledgeEditingFragment);
        transaction.commitAllowingStateLoss();

        mCurrentShowModuleId = MODULE_ID_EDIT_TASKS;
    }

    @Override
    public void onAddSuccess(String KnowledgePointName) {
        showEditingFragment(KnowledgePointName);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back_button:
                if (mCurrentShowModuleId == MODULE_ID_NEW_KNOWLEDGE) {
                    if (mCallback != null) {
                        mCallback.onBack();
                    }
                }
                break;
            case R.id.fre_class_item:
                break;
            case R.id.at_class_item:
                break;
            case R.id.after_class_item:
                break;
        }
    }

    @Override
    public void onBack() {
        if (mCallback != null) {
            mCallback.onBack();
        }
    }

    public interface Callback {
        void onBack();
    }
}
