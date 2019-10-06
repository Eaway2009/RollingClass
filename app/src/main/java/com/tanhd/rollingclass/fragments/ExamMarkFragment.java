package com.tanhd.rollingclass.fragments;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.tanhd.rollingclass.R;
import com.tanhd.rollingclass.fragments.pages.MarkAnswerPage;
import com.tanhd.rollingclass.fragments.pages.ScantronScorePage;
import com.tanhd.rollingclass.server.ScopeServer;
import com.tanhd.rollingclass.server.data.AnswerData;
import com.tanhd.rollingclass.server.data.ExternalParam;
import com.tanhd.rollingclass.server.data.LessonSampleData;
import com.tanhd.rollingclass.server.data.QuestionModel;
import com.tanhd.rollingclass.server.data.QuestionSetData;
import com.tanhd.rollingclass.utils.ResultClass;
import com.tanhd.rollingclass.utils.ToastUtil;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ExamMarkFragment extends Fragment {
    private TextView mTypeNameView;
    private TextView mPositionView;
    private TextView mTotalView;
    private ViewPager mViewPager;
    private ProgressBar mProgressBar;
    private MarkPageAdapter mAdapter;

    private ArrayList<QuestionModel> mQuestionList;
    private HashMap<String, AnswerData> mAnswerMap = new HashMap<>();

    public static ExamMarkFragment newInstance(String studentID, String questionSetID, ArrayList<String> questionIDList) {
        Bundle args = new Bundle();
        args.putString("studentID", studentID);
        if (questionSetID != null)
            args.putString("questionSetID", questionSetID);
        if (questionIDList != null)
            args.putSerializable("questionIDList", questionIDList);
        ExamMarkFragment fragment = new ExamMarkFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_exam_mark, container, false);
        init(view);
        return view;
    }

    private void init(View view) {
        mTypeNameView = view.findViewById(R.id.type_name);
        mPositionView = view.findViewById(R.id.tv_sequence);
        mTotalView = view.findViewById(R.id.tv_total_count);
        mProgressBar = view.findViewById(R.id.progressbar);
        mViewPager = view.findViewById(R.id.vp);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int i) {

            }

            @Override
            public void onPageScrollStateChanged(int i) {
                refreshPage();
            }
        });

        view.findViewById(R.id.tv_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment dialog = (DialogFragment) getParentFragment();
                dialog.dismiss();
            }
        });

        view.findViewById(R.id.tv_answercard).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mViewPager.setCurrentItem(mQuestionList.size());
            }
        });

        new LoadDataTask().execute();
    }

    private void refreshPage() {
        int currentPage = mViewPager.getCurrentItem();
        if (currentPage == mQuestionList.size()) {
            mPositionView.setVisibility(View.GONE);
            mTotalView.setVisibility(View.GONE);
            mTypeNameView.setText(getResources().getString(R.string.lbl_answer_situation));
            return;
        }

        mPositionView.setVisibility(View.VISIBLE);
        mTotalView.setVisibility(View.VISIBLE);
        QuestionModel questionData = mQuestionList.get(currentPage);
        mTypeNameView.setText(String.format("[%s]", questionData.context.QuestionCategoryName));
        mPositionView.setText(String.valueOf(currentPage + 1));
        mTotalView.setText("/" + String.valueOf(mQuestionList.size()));
    }

    private class MarkPageAdapter extends FragmentStatePagerAdapter {

        public MarkPageAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            if (i == mQuestionList.size()) {
                ScantronScorePage page = ScantronScorePage.newInstance(mQuestionList, mAnswerMap, new ScantronScorePage.ScantronScoreListener() {
                    @Override
                    public void onScrollToPage(int page) {
                        mViewPager.setCurrentItem(page);
                    }

                    @Override
                    public void onDismiss() {
                        DialogFragment dialog = (DialogFragment) getParentFragment();
                        dialog.dismiss();
                    }
                });
                return page;
            }

            QuestionModel questionData = mQuestionList.get(i);
            AnswerData answerData = mAnswerMap.get(questionData.question_id);
            Fragment page = MarkAnswerPage.newInstance(questionData, answerData);
            return page;
        }

        @Override
        public int getCount() {
            if (mQuestionList == null || mQuestionList.size() == 0)
                return 0;

            return mQuestionList.size() + 1;
        }
    }

    private class LoadDataTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            mProgressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            mProgressBar.setVisibility(View.GONE);
            if (mQuestionList == null || mQuestionList.size() == 0) {
                ToastUtil.show(R.string.toast_no_data);
                DialogFragment dialog = (DialogFragment) getParentFragment();
                dialog.dismiss();
                return;
            }

            mAdapter = new MarkPageAdapter(getChildFragmentManager());
            mViewPager.setAdapter(mAdapter);
            refreshPage();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            String questionSetID = getArguments().getString("questionSetID");
            ArrayList<String> questionIDList = (ArrayList<String>) getArguments().getSerializable("questionIDList");
            if (questionIDList != null) {
                mQuestionList = new ArrayList<>();
                for (int i=0; i<questionIDList.size(); i++) {
                    String questionID = questionIDList.get(i);
                    List<QuestionModel> questionList = ScopeServer.getInstance().QureyQuestionByID(questionID);
                    if (questionList == null && questionList.size() == 0)
                        continue;
                    QuestionModel questionData = questionList.get(0);
                    mQuestionList.add(questionData);
                }
            } else {
                if (questionSetID != null) {
                    String teacherID = ExternalParam.getInstance().getUserData().getOwnerID();
                    QuestionSetData questionSetData = ScopeServer.getInstance().QureyQuestionSetByTeacherID(teacherID, questionSetID);
                    if (questionSetData.QuestionList == null)
                        return null;

                    mQuestionList = new ArrayList<>();
                    for (int i=0; i<questionSetData.QuestionList.size(); i++) {
                        String questionID = questionSetData.QuestionList.get(i);
                        List<QuestionModel> questionList = ScopeServer.getInstance().QureyQuestionByID(questionID);
                        if (questionList == null && questionList.size() == 0)
                            continue;
                        QuestionModel questionData = questionList.get(0);
                        mQuestionList.add(questionData);
                    }
                } else {
                    LessonSampleData lessonSampleData = ExternalParam.getInstance().getLessonSample();
                    mQuestionList = (ArrayList<QuestionModel>) ScopeServer.getInstance().QureyQuestionByLessonSampleID(lessonSampleData.LessonSampleID);
                    if (mQuestionList == null)
                        return null;
                }
            }

            QuestionModel.sort(mQuestionList);

            String studentID = getArguments().getString("studentID");
            for (int i=0; i<mQuestionList.size(); i++) {
                QuestionModel questionData = mQuestionList.get(i);
                List<AnswerData> answerList = ScopeServer.getInstance().QureyAnswerv2ByStudentIDAndQuestionID(studentID, questionData.question_id);
                if (answerList == null || answerList.isEmpty())
                    continue;

                for (AnswerData answerData: answerList) {
                    if (questionSetID == null) {
                        if (answerData.QuestionSetID == null)
                            mAnswerMap.put(questionData.question_id, answerData);
                    } else {
                        if (questionSetID.equals(answerData.QuestionSetID))
                            mAnswerMap.put(questionData.question_id, answerData);
                    }
                }
            }

            return null;
        }
    }
}
