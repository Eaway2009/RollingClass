package com.tanhd.rollingclass.fragments.pages;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tanhd.rollingclass.R;
import com.tanhd.rollingclass.db.KeyConstants;
import com.tanhd.rollingclass.db.KeyConstants.ResourceType;
import com.tanhd.rollingclass.fragments.FrameDialog;
import com.tanhd.rollingclass.fragments.resource.ResourceBaseFragment;
import com.tanhd.rollingclass.fragments.resource.ResourcesPageFragment;
import com.tanhd.rollingclass.fragments.statistics.StatisticsPageFragment;
import com.tanhd.rollingclass.server.data.KnowledgeModel;
import com.tanhd.rollingclass.server.data.QuestionData;
import com.tanhd.rollingclass.server.data.QuestionModel;
import com.tanhd.rollingclass.server.data.ResourceModel;

public class ResourceSelectorFragment extends Fragment implements View.OnClickListener, ChaptersFragment.ChapterListener, ResourceBaseFragment.Callback {

    private ChaptersFragment mChapterFragment;
    private ResourcesPageFragment mResourceFragment;
    private KnowledgeModel mKnowledgeFragment;
    private KnowledgeModel mKnowledgeModel;
    private View mSureButton;
    private View mCancalButton;
    private ResourceModel mResourceModel;
    private QuestionModel mQuestionModel;
    private Callback mListener;
    public static final String PARAM_RESOURCE_TYPE = "RESOURCE_TYPE";
    private int resourceType;

    public static ResourceSelectorFragment newInstance(Callback callback, int resourceCode) {
        Bundle args = new Bundle();
        args.putInt(PARAM_RESOURCE_TYPE, resourceCode);
        ResourceSelectorFragment fragment = new ResourceSelectorFragment();
        fragment.setListener(callback);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_resource_selector, container, false);
        initParams();
        initViews(view);
        initFragments();
        return view;
    }

    private void initParams() {
        resourceType = getArguments().getInt(PARAM_RESOURCE_TYPE, ResourceType.PPT_TYPE);
    }

    public void setListener(Callback callback) {
        mListener = callback;
    }

    private void initViews(View view) {
        mCancalButton = view.findViewById(R.id.cancel_button);
        mSureButton = view.findViewById(R.id.commit_button);

        mCancalButton.setOnClickListener(this);
        mSureButton.setOnClickListener(this);
    }

    private void initFragments() {
        mChapterFragment = ChaptersFragment.newInstance(this);
        getFragmentManager().beginTransaction().replace(R.id.chapters_layout, mChapterFragment).commit();

        mResourceFragment = ResourcesPageFragment.newInstance(mKnowledgeFragment, resourceType,this);
        getFragmentManager().beginTransaction().replace(R.id.framelayout, mResourceFragment).commit();
    }

    @Override
    public void onTeacherCheckChapter(String school_id, String teacher_id, String chapter_id, String chapter_name, String section_id, String section_name, int subject_code, String subject_name, String teaching_material_id) {
        mKnowledgeModel = new KnowledgeModel(school_id, teacher_id, chapter_id, chapter_name, section_id, section_name, subject_code, subject_name, teaching_material_id, null);
        if (mResourceFragment != null) {
            mResourceFragment.resetData(mKnowledgeModel);
        }
    }

    @Override
    public void onStudentCheckChapter(String school_id, String class_id, String chapter_id, String chapter_name, String section_id, String section_name, int subject_code, String subject_name, String teaching_material_id) {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.cancel_button:
                if (mListener != null) {
                    mListener.cancel();

                    if (getParentFragment() instanceof FrameDialog) {
                        FrameDialog dialog = (FrameDialog) getParentFragment();
                        dialog.dismiss();
                    }
                }
                break;
            case R.id.commit_button:
                if (mListener != null) {
                    if (mResourceModel != null || mQuestionModel != null) {
                        mListener.resourceChecked(mResourceModel, mQuestionModel);

                        if (getParentFragment() instanceof FrameDialog) {
                            FrameDialog dialog = (FrameDialog) getParentFragment();
                            dialog.dismiss();
                        }
                    }
                }
                break;
        }
    }

    @Override
    public void itemChecked(ResourceModel resourceModel, QuestionModel questionModel) {
        mResourceModel = resourceModel;
        mQuestionModel = questionModel;
    }

    public interface Callback {
        public void cancel();

        public void resourceChecked(ResourceModel resourceModel, QuestionModel questionModel);
    }
}
