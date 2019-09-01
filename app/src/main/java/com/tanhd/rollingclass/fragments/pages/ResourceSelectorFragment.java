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
import com.tanhd.rollingclass.fragments.resource.ResourcesPageFragment;
import com.tanhd.rollingclass.fragments.statistics.StatisticsPageFragment;
import com.tanhd.rollingclass.server.data.KnowledgeModel;
import com.tanhd.rollingclass.server.data.QuestionData;
import com.tanhd.rollingclass.server.data.QuestionModel;
import com.tanhd.rollingclass.server.data.ResourceModel;

public class ResourceSelectorFragment extends Fragment implements View.OnClickListener, ChaptersFragment.ChapterListener {

    private ChaptersFragment mChapterFragment;
    private ResourcesPageFragment mResourceFragment;
    private KnowledgeModel mKnowledgeFragment;
    private KnowledgeModel mKnowledgeModel;
    private View mSureButton;
    private View mCancalButton;

    public static ResourceSelectorFragment newInstance() {
        ResourceSelectorFragment fragment = new ResourceSelectorFragment();
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_resource_selector, container, false);
        initViews(view);
        initFragments();
        return view;
    }

    private void initViews(View view) {
        mCancalButton = view.findViewById(R.id.cancel_button);
        mSureButton = view.findViewById(R.id.sure_button);

        mCancalButton.setOnClickListener(this);
        mSureButton.setOnClickListener(this);
    }

    private void initFragments(){
        mChapterFragment = ChaptersFragment.newInstance(this);
        getFragmentManager().beginTransaction().replace(R.id.chapters_layout, mChapterFragment).commit();

        mResourceFragment = ResourcesPageFragment.newInstance(mKnowledgeFragment);
        getFragmentManager().beginTransaction().replace(R.id.framelayout, mResourceFragment).commit();
    }

    @Override
    public void onCheckChapter(String school_id, String teacher_id, String chapter_id, String chapter_name, String section_id, String section_name, int subject_code, String subject_name, String teaching_material_id) {
        mKnowledgeModel = new KnowledgeModel(school_id, teacher_id, chapter_id, chapter_name, section_id, section_name, subject_code, subject_name, teaching_material_id);
        if(mResourceFragment!=null){
            mResourceFragment.resetData(mKnowledgeModel);
        }
    }

    @Override
    public void onClick(View v) {

    }

    public interface Callback{
        public void cancel();
        public void resourceChecked(ResourceModel resourceModel, QuestionModel questionModel);
    }
}
