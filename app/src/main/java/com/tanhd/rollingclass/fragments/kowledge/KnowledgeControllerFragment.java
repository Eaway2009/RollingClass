package com.tanhd.rollingclass.fragments.kowledge;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.tanhd.rollingclass.R;
import com.tanhd.rollingclass.activity.DocumentEditActivity;
import com.tanhd.rollingclass.db.KeyConstants;
import com.tanhd.rollingclass.server.data.KnowledgeDetailMessage;
import com.tanhd.rollingclass.server.data.KnowledgeModel;

/**
 * 课前 课中 课后
 */
public class KnowledgeControllerFragment extends Fragment implements View.OnClickListener, KnowledgeNoneFragment.Callback, KnowledgeEditingFragment.Callback {

    private static final int MODULE_ID_NEW_KNOWLEDGE = 1;
    private static final int MODULE_ID_EDIT_TASKS = 2;

    private KnowledgeModel mKnowledgeModel;

    private int mCurrentShowModuleId = -1;
    private KnowledgeNoneFragment mKnowledgeNoneFragment;
    private KnowledgeEditingFragment mKnowledgeEditingFragment;

    private View mBackButton;
    private View mFreClassItemView;
    private View mAtClassItemView;
    private View mAfterClassItemView;
    private Callback mCallback;
    private int mStatus = 1;
    private KnowledgeDetailMessage mKnowledgeDetailMessage;

    public static KnowledgeControllerFragment newInstance(KnowledgeModel knowledgeModel, Callback callback) {
        Bundle args = new Bundle();
        args.putSerializable(DocumentEditActivity.PARAM_TEACHING_MATERIAL_DATA, knowledgeModel);
        KnowledgeControllerFragment page = new KnowledgeControllerFragment();
        page.setArguments(args);
        page.setCallback(callback);
        return page;
    }

    public static KnowledgeControllerFragment newInstance(KnowledgeModel knowledgeModel, KnowledgeDetailMessage knowledgeDetailMessage, Callback callback) {
        Bundle args = new Bundle();
        args.putSerializable(DocumentEditActivity.PARAM_TEACHING_MATERIAL_DATA, knowledgeModel);
        args.putSerializable(DocumentEditActivity.PARAM_KNOWLEDGE_DETAIL_DATA, knowledgeDetailMessage);
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
        initViews(view);
        showFragment();
        return view;
    }

    private void initParams() {
        Bundle args = getArguments();
        mKnowledgeModel = (KnowledgeModel) args.getSerializable(DocumentEditActivity.PARAM_TEACHING_MATERIAL_DATA);
        mKnowledgeDetailMessage = (KnowledgeDetailMessage) args.getSerializable(DocumentEditActivity.PARAM_KNOWLEDGE_DETAIL_DATA);

        if (mKnowledgeDetailMessage != null) {
            if (mKnowledgeDetailMessage.class_before != 1) {
                mStatus = KeyConstants.KnowledgeStatus.FRE_CLASS;
            } else if (mKnowledgeDetailMessage.class_process != 1) {
                mStatus = KeyConstants.KnowledgeStatus.AT_CLASS;
            } else {
                mStatus = KeyConstants.KnowledgeStatus.AFTER_CLASS;
            }
        }
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

    private void showFragment() {
        if (mKnowledgeDetailMessage != null) {
            if (mKnowledgeDetailMessage.class_before != 1) {
                changeClassStatus(KeyConstants.KnowledgeStatus.FRE_CLASS);
            } else if (mKnowledgeDetailMessage.class_process != 1) {
                changeClassStatus(KeyConstants.KnowledgeStatus.AT_CLASS);
            } else {
                changeClassStatus(KeyConstants.KnowledgeStatus.AFTER_CLASS);
            }
            showEditingFragment(mKnowledgeModel, mKnowledgeDetailMessage);
        } else {
            showKnowledgeNoneFragment();
        }
    }

    public void showKnowledgeNoneFragment() {
        if (mCurrentShowModuleId == MODULE_ID_NEW_KNOWLEDGE) {
            return;
        }
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        if (mKnowledgeNoneFragment == null) {
            mKnowledgeNoneFragment = KnowledgeNoneFragment.newInstance(mKnowledgeModel, this);
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
    public void showEditingFragment(KnowledgeModel knowledgeModel, KnowledgeDetailMessage insertKnowledgeResponse) {
        if (mCurrentShowModuleId == MODULE_ID_EDIT_TASKS) {
            return;
        }
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        if (mKnowledgeEditingFragment == null) {
            mKnowledgeEditingFragment = KnowledgeEditingFragment.newInstance(knowledgeModel, insertKnowledgeResponse, mStatus, this);
            transaction.add(R.id.content_layout, mKnowledgeEditingFragment);
        }
        if (mKnowledgeNoneFragment != null) {
            transaction.hide(mKnowledgeNoneFragment);
        }
        transaction.show(mKnowledgeEditingFragment);
        transaction.commitAllowingStateLoss();

        mCurrentShowModuleId = MODULE_ID_EDIT_TASKS;
    }

    private void resetStatus() {
        mKnowledgeEditingFragment.resetData(mKnowledgeModel, mKnowledgeDetailMessage, mStatus);
    }

    @Override
    public void onAddSuccess(KnowledgeModel model, KnowledgeDetailMessage response) {
        mKnowledgeDetailMessage = response;
        showEditingFragment(model, response);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back_button:
                if (mCurrentShowModuleId == MODULE_ID_NEW_KNOWLEDGE) {
                    if (mCallback != null) {
                        mCallback.onBack();
                    }
                } else {
                    if (mKnowledgeEditingFragment.isEditing()) {
                        Toast.makeText(getActivity(), getString(R.string.adding_task_warning), Toast.LENGTH_SHORT).show();
                    } else {
                        if (mCallback != null) {
                            mCallback.onBack();
                        }
                    }
                }
                break;
            case R.id.fre_class_item:
                if (mCurrentShowModuleId == MODULE_ID_EDIT_TASKS && mKnowledgeEditingFragment.isEditing()) {
                    Toast.makeText(getActivity(), R.string.adding_task_warning, Toast.LENGTH_SHORT).show();
                    return;
                }
                changeClassStatus(KeyConstants.KnowledgeStatus.FRE_CLASS);
                resetStatus();
                break;
            case R.id.at_class_item:
                if (mCurrentShowModuleId == MODULE_ID_EDIT_TASKS && mKnowledgeEditingFragment.isEditing()) {
                    Toast.makeText(getActivity(), R.string.adding_task_warning, Toast.LENGTH_SHORT).show();
                    return;
                }
                changeClassStatus(KeyConstants.KnowledgeStatus.AT_CLASS);
                resetStatus();
                break;
            case R.id.after_class_item:
                if (mCurrentShowModuleId == MODULE_ID_EDIT_TASKS && mKnowledgeEditingFragment.isEditing()) {
                    Toast.makeText(getActivity(), R.string.adding_task_warning, Toast.LENGTH_SHORT).show();
                    return;
                }
                changeClassStatus(KeyConstants.KnowledgeStatus.AFTER_CLASS);
                resetStatus();
                break;
        }
    }

    private void changeClassStatus(int status) {
        switch (status) {
            case KeyConstants.KnowledgeStatus.FRE_CLASS:
                mFreClassItemView.setEnabled(false);
                mAtClassItemView.setEnabled(true);
                mAfterClassItemView.setEnabled(true);
                break;
            case KeyConstants.KnowledgeStatus.AT_CLASS:
                mFreClassItemView.setEnabled(true);
                mAtClassItemView.setEnabled(false);
                mAfterClassItemView.setEnabled(true);
                break;
            case KeyConstants.KnowledgeStatus.AFTER_CLASS:
                mFreClassItemView.setEnabled(true);
                mAtClassItemView.setEnabled(true);
                mAfterClassItemView.setEnabled(false);
                break;
        }
        mStatus = status;
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
