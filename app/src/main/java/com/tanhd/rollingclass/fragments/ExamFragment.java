package com.tanhd.rollingclass.fragments;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.tanhd.rollingclass.R;
import com.tanhd.rollingclass.fragments.pages.QuestionAnswerPage;
import com.tanhd.rollingclass.fragments.pages.ScantronAnswerPage;
import com.tanhd.rollingclass.server.ScopeServer;
import com.tanhd.rollingclass.server.data.AnswerData;
import com.tanhd.rollingclass.server.data.ExternalParam;
import com.tanhd.rollingclass.server.data.LessonSampleData;
import com.tanhd.rollingclass.server.data.QuestionData;
import com.tanhd.rollingclass.server.data.QuestionSetData;
import com.tanhd.rollingclass.server.data.UserData;
import com.tanhd.rollingclass.utils.AppUtils;
import com.tanhd.rollingclass.utils.ResultClass;
import com.tanhd.rollingclass.views.QuestionAnswerView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ExamFragment extends Fragment {
    public static interface ExamListener {
        void onFinished();
    }

    private Button mNextButton;
    private TextView mTimeView;
    private long mStartTime;
    private TextView mTypeNameView;
    private TextView mPositionView;
    private TextView mTotalView;
    private ViewPager mViewPager;
    private ProgressBar mProgressBar;
    private QuestionPageAdapter mAdapter;
    private List<QuestionData> mQuestionList;
    private HashMap<Integer, ResultClass> mResultMap = new HashMap<>();
    private View mRootView;
    private ExamListener mListener;

    public static ExamFragment newInstance(String teacherID, String questionSetID, ExamListener listener) {
        Bundle args = new Bundle();
        args.putString("teacherID", teacherID);
        if (questionSetID != null)
            args.putString("questionSetID", questionSetID);
        ExamFragment fragment = new ExamFragment();
        fragment.setArguments(args);
        fragment.setListener(listener);
        return fragment;
    }

    public void setListener(ExamListener listener) {
        this.mListener = listener;
    }

    private QuestionAnswerPage.QuestionAnswerListener questionAnswerListener = new QuestionAnswerPage.QuestionAnswerListener() {
        @Override
        public void onFinished(QuestionAnswerPage page) {
            mViewPager.setCurrentItem(mViewPager.getCurrentItem() + 1);
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_exam, container, false);
        mProgressBar = mRootView.findViewById(R.id.progressbar);
        new LoadQuestionTask().execute();
        return mRootView;
    }

    private void init(View view) {
        mNextButton = view.findViewById(R.id.btn_next);
        mNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mViewPager.setCurrentItem(mViewPager.getCurrentItem() + 1);
            }
        });
        mTimeView = view.findViewById(R.id.tv_time);
        mTypeNameView = view.findViewById(R.id.type_name);
        mPositionView = view.findViewById(R.id.tv_sequence);
        mTotalView = view.findViewById(R.id.tv_total_count);
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
                Fragment fragment = mAdapter.getCurrentFragment();
                if (fragment instanceof QuestionAnswerPage) {
                    QuestionAnswerPage page = (QuestionAnswerPage) fragment;
                    page.getData();
                }
                mViewPager.setCurrentItem(mQuestionList.size());
            }
        });

        startCounter();

        mAdapter = new QuestionPageAdapter(getChildFragmentManager());
        mViewPager.setAdapter(mAdapter);
        refreshPage();
    }

    // 开始计时
    public void startCounter() {
        mStartTime = System.currentTimeMillis();
        handler.sendEmptyMessageDelayed(1, 1000);
    }

    //计时器任务
    Handler handler = new Handler(){
        public void handleMessage(Message msg) {
            long count = (System.currentTimeMillis() - mStartTime) / 1000;
            mTimeView.setText(String.format("%02d:%02d", count / 60, count % 60));
            sendEmptyMessageDelayed(0, 1000);
        };
    };

    private void refreshPage() {
        int currentPage = mViewPager.getCurrentItem();
        if (currentPage == mQuestionList.size()) {
            mPositionView.setVisibility(View.GONE);
            mTotalView.setVisibility(View.GONE);
            mTypeNameView.setText("答题情况");
            mNextButton.setVisibility(View.GONE);
            return;
        }

        mNextButton.setVisibility(View.VISIBLE);
        mPositionView.setVisibility(View.VISIBLE);
        mTotalView.setVisibility(View.VISIBLE);
        QuestionData questionData = mQuestionList.get(currentPage);
        mTypeNameView.setText(String.format("[%s]", questionData.Context.QuestionCategoryName));
        mPositionView.setText(String.valueOf(currentPage + 1));
        mTotalView.setText("/" + String.valueOf(mQuestionList.size()));
    }

    private class QuestionPageAdapter extends FragmentStatePagerAdapter {
        private Fragment mCurrentFragment;

        public QuestionPageAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            if (i < mQuestionList.size()) {
                QuestionData questionData = mQuestionList.get(i);
                ResultClass resultClass = mResultMap.get(i);
                QuestionAnswerPage page = QuestionAnswerPage.newInstance(questionData, resultClass);
                page.setListener(questionAnswerListener);
                return page;
            } else {
                ScantronAnswerPage page = ScantronAnswerPage.newInstance(mResultMap, new ScantronAnswerPage.ScantronListener() {
                    @Override
                    public void onScrollToPage(int page) {
                        mViewPager.setCurrentItem(page);
                    }

                    @Override
                    public void onCommintAnswer() {
                        new CommitAnswerTask().execute();
                    }
                });
                return page;
            }
        }

        @Override
        public int getCount() {
            if (mQuestionList == null || mQuestionList.size() == 0)
                return 0;

            return mQuestionList.size() + 1;
        }

        @Override
        public void setPrimaryItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            mCurrentFragment = (Fragment) object;
            if (object instanceof QuestionAnswerPage) {
                QuestionAnswerPage page = (QuestionAnswerPage) object;
                page.active();
            } else {
                ScantronAnswerPage page = (ScantronAnswerPage) object;
                page.refresh();
            }
            super.setPrimaryItem(container, position, object);
        }

        public Fragment getCurrentFragment() {
            return mCurrentFragment;
        }
    }

    private void dismiss() {
        if (mListener != null)
            mListener.onFinished();
        DialogFragment dialog = (DialogFragment) getParentFragment();
        dialog.dismiss();
    }

    private class CommitAnswerTask extends AsyncTask<Void, Void, Integer> {

        @Override
        protected void onPreExecute() {
            mProgressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(Integer result) {
            mProgressBar.setVisibility(View.GONE);
            if (result == 0) {
                Toast.makeText(getActivity().getApplicationContext(), "提交答案成功!", Toast.LENGTH_LONG).show();
                dismiss();
            } else {
                Toast.makeText(getActivity().getApplicationContext(), "提交失败! ErrorCode:" + result, Toast.LENGTH_LONG).show();
            }
        }

        @Override
        protected Integer doInBackground(Void... voids) {
            UserData userData = ExternalParam.getInstance().getUserData();
            String userID = userData.getOwnerID();
            String userName = userData.getOwnerName();
            ArrayList<AnswerData> answerList = new ArrayList<>();
            for (int i=0; i<mQuestionList.size(); i++) {
                ResultClass resultClass = mResultMap.get(i);
                boolean isUrl = false;
                String result = resultClass.getResult(getContext());

                if (result != null && resultClass.isAnswerFile()) {
                    if (!AppUtils.isUrl(result)) {
                        result = ScopeServer.getInstance().uploadResourceFile(result, 105);
                        if (result == null)
                            return -1;
                    }

                    isUrl = true;
                }

                //如果未作答，则不提交答案
                if (TextUtils.isEmpty(result)) {
                    continue;
                }

                QuestionData questionData = mQuestionList.get(i);
                AnswerData answerData = new AnswerData();
                answerData.AnswerName = questionData.Context.QuestionCategoryName;
                answerData.QuestionID = questionData.QuestionID;
                answerData.QuestionName = questionData.Context.Stem;
                answerData.AnswerUserID = userID;
                answerData.AnswerUserName = userName;
                answerData.LessonSampleID = questionData.LessonSampleID;
                answerData.LessonSampleName = questionData.LessonSampleName;
                answerData.TeacherID = questionData.TeacherID;
                answerData.TeacherName = questionData.TeacherName;
                answerData.ClassID = ExternalParam.getInstance().getClassData().ClassID;
                answerData.QuestionCategoryName = questionData.Context.QuestionCategoryName;
                answerData.QuestionCategoryId = questionData.Context.QuestionCategoryId;
                answerData.QuestionType = questionData.QuestionType;
                answerData.QuestionSetID = getArguments().getString("questionSetID");

                if (isUrl) {
                    answerData.AnswerUrl = result;
                } else {
                    answerData.AnswerText = result;
                }
                answerList.add(answerData);
            }

            //提交答案到服务器
            for (AnswerData answerData: answerList) {
                String json = answerData.toJSON().toString();
                int errCode = ScopeServer.getInstance().InsertAnswerv2(json);
                if (errCode != 0)
                    continue;
            }

            return 0;
        }
    }

    private class LoadQuestionTask extends AsyncTask<Void, Void, List> {
        HashMap<String, AnswerData> answerMap = new HashMap<>();

        @Override
        protected void onPreExecute() {
            mProgressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(List list) {
            mProgressBar.setVisibility(View.GONE);
            if (list == null) {
                Toast.makeText(getContext().getApplicationContext(), "读取题目失败!", Toast.LENGTH_LONG).show();
                DialogFragment dialog = (DialogFragment) getParentFragment();
                dialog.dismiss();
                return;
            }

            mQuestionList = list;
            for (int i=0; i<list.size(); i++) {
                QuestionData questionData = mQuestionList.get(i);
                ResultClass resultClass = new ResultClass();
                AnswerData answerData = answerMap.get(questionData.QuestionID);
                if (answerData != null) {
                    if (!TextUtils.isEmpty(answerData.AnswerUrl)) {
                        resultClass.mode = 2;
                        resultClass.imagePath = answerData.AnswerUrl;
                    } else if (!TextUtils.isEmpty(answerData.AnswerText)) {
                        resultClass.mode = 1;
                        resultClass.text = answerData.AnswerText;
                    }
                }

                mResultMap.put(i, resultClass);
            }

            init(mRootView);
        }

        @Override
        protected List doInBackground(Void... strings) {
            String teacherID = getArguments().getString("teacherID");
            String questionSetID = getArguments().getString("questionSetID");

            List<QuestionData> questionList = null;
            if (questionSetID == null) {
                LessonSampleData lessonSampleData = ExternalParam.getInstance().getLessonSample();
                if (lessonSampleData == null)
                    return null;

                questionList = ScopeServer.getInstance().QureyQuestionByLessonSampleID(lessonSampleData.LessonSampleID);
                if (questionList == null)
                    return null;

                String studentID = ExternalParam.getInstance().getUserData().getOwnerID();
                for (QuestionData questionData: questionList) {
                    List<AnswerData> answerList = ScopeServer.getInstance().QureyAnswerv2ByStudentIDAndQuestionID(studentID, questionData.QuestionID);
                    if (answerList == null || answerList.isEmpty())
                        continue;
                    for (AnswerData answerData: answerList) {
                        if (answerData.QuestionSetID == null) {
                            answerMap.put(questionData.QuestionID, answerList.get(0));
                            break;
                        }
                    }
                }
            } else {
                QuestionSetData questionSetData = ScopeServer.getInstance().QureyQuestionSetByTeacherID(teacherID, questionSetID);
                if (questionSetData == null || questionSetData.QuestionList == null)
                    return null;

                questionList = new ArrayList<>();
                for (String questionID: questionSetData.QuestionList) {
                    List<QuestionData> list = ScopeServer.getInstance().QureyQuestionByID(questionID);
                    if (list == null || list.isEmpty())
                        continue;
                    questionList.addAll(list);
                }
            }

            QuestionData.sort(questionList);
            return questionList;
        }
    }
}
