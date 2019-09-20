package com.tanhd.rollingclass.fragments.kowledge;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.tanhd.rollingclass.R;
import com.tanhd.rollingclass.db.KeyConstants;
import com.tanhd.rollingclass.fragments.FrameDialog;

public class KnowledgePublishFragment extends Fragment {

    private PublishCallback mListener;
    private int mStatus;
    private CheckBox mPreClassCheckBox;
    private CheckBox mInClassCheckBox;
    private CheckBox mAfterClassCheckBox;
    private int[] checkItem;
    private View mCancelButton;
    private View mCommitButton;

    public static KnowledgePublishFragment newInstance(int status, PublishCallback callback) {
        KnowledgePublishFragment knowledgePublishFragment = new KnowledgePublishFragment();
        knowledgePublishFragment.setListener(callback);
        Bundle args = new Bundle();
        args.putInt("Status", status);
        return knowledgePublishFragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View contentView = inflater.inflate(R.layout.fragment_publish, container);
        initParams();
        initViews(contentView);
        return contentView;
    }

    private void initViews(View contentView) {
        mPreClassCheckBox = contentView.findViewById(R.id.publish_pre_class);
        mInClassCheckBox = contentView.findViewById(R.id.publish_in_class);
        mAfterClassCheckBox = contentView.findViewById(R.id.publish_after_class);
        mCancelButton = contentView.findViewById(R.id.cancel_button);
        mCommitButton = contentView.findViewById(R.id.commit_button);

        mPreClassCheckBox.setOnCheckedChangeListener(onCheckedChangeListener);
        mInClassCheckBox.setOnCheckedChangeListener(onCheckedChangeListener);
        mAfterClassCheckBox.setOnCheckedChangeListener(onCheckedChangeListener);
        mCancelButton.setOnClickListener(onClickListener);
        mCommitButton.setOnClickListener(onClickListener);

        switch (mStatus) {
            case KeyConstants.KnowledgeStatus.FRE_CLASS:
                mPreClassCheckBox.setVisibility(View.GONE);
                break;
            case KeyConstants.KnowledgeStatus.AT_CLASS:
                mInClassCheckBox.setVisibility(View.GONE);
                break;
            case KeyConstants.KnowledgeStatus.AFTER_CLASS:
                mAfterClassCheckBox.setVisibility(View.GONE);
                break;
        }
    }

    private void initParams() {
        Bundle args = getArguments();
        mStatus = args.getInt("Status");
        checkItem = new int[]{1, 1, 1};
    }

    CompoundButton.OnCheckedChangeListener onCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            switch (buttonView.getId()) {
                case R.id.publish_pre_class:
                    checkItem[0] = buttonView.isChecked() ? 1 : 0;
                    break;
                case R.id.publish_in_class:
                    checkItem[1] = buttonView.isChecked() ? 1 : 0;
                    break;
                case R.id.publish_after_class:
                    checkItem[2] = buttonView.isChecked() ? 1 : 0;
                    break;
            }
        }
    };

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.commit_button:
                    mListener.publish(checkItem);
                    dismiss();
                    break;
                case R.id.cancel_button:
                    dismiss();
                    break;
            }
        }
    };

    private void dismiss() {
        if (getParentFragment() instanceof FrameDialog) {
            FrameDialog dialog = (FrameDialog) getParentFragment();
            dialog.dismiss();
        }
    }

    public void setListener(PublishCallback callback) {
        mListener = callback;
    }

    public interface PublishCallback {
        void publish(int[] checkedPublish);
    }
}
