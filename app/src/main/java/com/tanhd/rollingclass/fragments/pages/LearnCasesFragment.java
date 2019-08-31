package com.tanhd.rollingclass.fragments.pages;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tanhd.rollingclass.R;
import com.tanhd.rollingclass.activity.LearnCasesActivity;
import com.tanhd.rollingclass.fragments.ShowDocumentFragment;
import com.tanhd.rollingclass.fragments.kowledge.KnowledgeEditingFragment;
import com.tanhd.rollingclass.server.ScopeServer;
import com.tanhd.rollingclass.server.data.ExternalParam;
import com.tanhd.rollingclass.server.data.KnowledgeDetailMessage;
import com.tanhd.rollingclass.server.data.KnowledgeLessonSample;
import com.tanhd.rollingclass.server.data.KnowledgeModel;
import com.tanhd.rollingclass.server.data.ResourceModel;
import com.tanhd.rollingclass.server.data.UserData;
import com.tanhd.rollingclass.views.ClassStudentsAdapter;
import com.tanhd.rollingclass.views.LessonItemAdapter;

import java.util.ArrayList;
import java.util.List;

public class LearnCasesFragment extends Fragment implements OnClickListener, ExpandableListView.OnChildClickListener {

    private RelativeLayout mRlInnerTitle;
    private RelativeLayout mRlMenuLayout;
    private LinearLayout mLlMenuContainer;
    private TextView mTvInsertResource;
    private TextView mTvExerciseResult;
    private TextView mTvClassBegin;
    private PagesListener mListener;

    private LearnCasesContainerFragment mLearnCasesContainerFragment;
    private KnowledgeDetailMessage mKnowledgeDetailMessage;
    private TextView mKnowledgeNameTextView;
    private ExpandableListView mExpandableListView;
    private LessonItemAdapter mAdapter;

    public static LearnCasesFragment newInstance(KnowledgeDetailMessage data, LearnCasesFragment.PagesListener listener) {
        Bundle args = new Bundle();
        LearnCasesFragment page = new LearnCasesFragment();
        args.putSerializable(LearnCasesActivity.PARAM_KNOWLEDGE_DETAIL_MESSAGE, data);
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
        initParams();
        initViews(view);
        setData();
        return view;
    }

    private void initParams() {
        Bundle args = getArguments();
        mKnowledgeDetailMessage = (KnowledgeDetailMessage) args.getSerializable(LearnCasesActivity.PARAM_KNOWLEDGE_DETAIL_MESSAGE);
    }

    private void initViews(View view) {
        mKnowledgeNameTextView = view.findViewById(R.id.knowledge_name_tv);
        mRlMenuLayout = view.findViewById(R.id.rl_cases_menu);
        mRlInnerTitle = view.findViewById(R.id.rl_inner_title);
        mLlMenuContainer = view.findViewById(R.id.ll_menu_container);
        mTvInsertResource = view.findViewById(R.id.tv_insert_resource);
        mTvExerciseResult = view.findViewById(R.id.tv_exercise_result);
        mTvClassBegin = view.findViewById(R.id.tv_class_begin);
        initListViews(view);

        mTvInsertResource.setOnClickListener(this);
        mTvExerciseResult.setOnClickListener(this);
        mTvClassBegin.setOnClickListener(this);
        view.findViewById(R.id.back_button).setOnClickListener(this);

        mLearnCasesContainerFragment = LearnCasesContainerFragment.newInstance(1, mPagesListener);
        getFragmentManager().beginTransaction().replace(R.id.content_layout, mLearnCasesContainerFragment).commit();
    }

    private void initListViews(View view) {
        mExpandableListView = view.findViewById(R.id.expandable_listview);
        mAdapter = new LessonItemAdapter(getActivity());
        mExpandableListView.setVerticalScrollBarEnabled(false);
        mExpandableListView.setGroupIndicator(null);
        mExpandableListView.setHeaderDividersEnabled(false);
        mExpandableListView.setAdapter(mAdapter);
        mExpandableListView.setOnChildClickListener(this);
    }


    private void setData() {
        if (mKnowledgeDetailMessage != null) {
            mKnowledgeNameTextView.setText(mKnowledgeDetailMessage.knowledge_point_name);
            new InitDataTask().execute();
        }
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

    @Override
    public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
        mLearnCasesContainerFragment.showResource(mAdapter.getGroup(groupPosition).getChildren().get(childPosition));
        return false;
    }


    private class InitDataTask extends AsyncTask<Void, Void, List<KnowledgeLessonSample>> {

        @Override
        protected List<KnowledgeLessonSample> doInBackground(Void... voids) {
            return ScopeServer.getInstance().QuerySampleByKnowledge(mKnowledgeDetailMessage.knowledge_id, 2);
        }

        @Override
        protected void onPostExecute(List<KnowledgeLessonSample> documentList) {
            if (documentList != null && documentList.size() > 0) {
                mAdapter.setDataList(documentList);
            }
        }
    }

    public interface PagesListener {

        void onFullScreen(boolean isFull);

        void onPageChange(int id);

        void onBack();
    }

}
