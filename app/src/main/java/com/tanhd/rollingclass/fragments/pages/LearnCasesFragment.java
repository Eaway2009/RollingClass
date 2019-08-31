package com.tanhd.rollingclass.fragments.pages;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.tanhd.rollingclass.R;

public class LearnCasesFragment extends Fragment implements OnClickListener {

    private RelativeLayout mRlInnerTitle;
    private RelativeLayout mRlMenuLayout;
    private LinearLayout mLlMenuContainer;
    private TextView mTvInsertResource;
    private TextView mTvExerciseResult;
    private TextView mTvClassBegin;
    private PagesListener mListener;

    private LearnCasesContainerFragment mLearnCasesContainerFragment;

    public static LearnCasesFragment newInstance(LearnCasesFragment.PagesListener listener) {
        Bundle args = new Bundle();
        LearnCasesFragment page = new LearnCasesFragment();
        page.setArguments(args);
        page.setListener(listener);
        return page;
    }

    private void setListener(PagesListener listener) {
        mListener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.page_learncases_pages, container, false);
//        initParams();
        initViews(view);
        return view;
    }

    private void initViews(View view) {
        mRlMenuLayout = view.findViewById(R.id.rl_cases_menu);
        mRlInnerTitle = view.findViewById(R.id.rl_inner_title);
        mLlMenuContainer = view.findViewById(R.id.ll_menu_container);
        mTvInsertResource = view.findViewById(R.id.tv_insert_resource);
        mTvExerciseResult = view.findViewById(R.id.tv_exercise_result);
        mTvClassBegin = view.findViewById(R.id.tv_class_begin);

        mTvInsertResource.setOnClickListener(this);
        mTvExerciseResult.setOnClickListener(this);
        mTvClassBegin.setOnClickListener(this);
        view.findViewById(R.id.back_button).setOnClickListener(this);

        view.findViewById(R.id.ll_ppt).setOnClickListener(this);
        view.findViewById(R.id.ll_target_guidance).setOnClickListener(this);
        view.findViewById(R.id.ll_self_study).setOnClickListener(this);
        view.findViewById(R.id.ll_assist_study).setOnClickListener(this);
        view.findViewById(R.id.ll_coopera_study).setOnClickListener(this);
        view.findViewById(R.id.ll_class_exercises).setOnClickListener(this);
        view.findViewById(R.id.ll_difficult_break).setOnClickListener(this);
        view.findViewById(R.id.ll_class_summary).setOnClickListener(this);
        view.findViewById(R.id.ll_relevant_info).setOnClickListener(this);

        mLearnCasesContainerFragment = LearnCasesContainerFragment.newInstance(1, mPagesListener);
        getFragmentManager().beginTransaction().replace(R.id.content_layout, mLearnCasesContainerFragment).commit();
    }

    LearnCasesContainerFragment.PagesListener mPagesListener = new LearnCasesContainerFragment.PagesListener() {

        @Override
        public void onFullScreen(boolean isFull) {
            if (mListener != null) {
                mListener.onFullScreen(isFull);
                mRlMenuLayout.setVisibility(isFull == true ? View.GONE : View.VISIBLE);
                mRlInnerTitle.setVisibility(isFull == true ? View.GONE : View.VISIBLE);
            }
        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_insert_resource:
                break;
            case R.id.tv_exercise_result:
                break;
            case R.id.tv_class_begin:
                break;
            case R.id.back_button:
                if (mListener != null) {
                    mListener.onBack();
                }
                break;
        }
    }

    public interface PagesListener {

        void onFullScreen(boolean isFull);

        void onPageChange(int id);

        void onBack();
    }

}
