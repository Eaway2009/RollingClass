package com.tanhd.rollingclass.fragments.pages;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.tanhd.rollingclass.R;
import com.tanhd.rollingclass.activity.LearnCasesActivity;
import com.tanhd.rollingclass.db.KeyConstants;
import com.tanhd.rollingclass.fragments.AnswerCardAdapter;
import com.tanhd.rollingclass.server.ScopeServer;
import com.tanhd.rollingclass.server.data.AnswerData;
import com.tanhd.rollingclass.server.data.ExternalParam;
import com.tanhd.rollingclass.server.data.QuestionModel;
import com.tanhd.rollingclass.server.data.ResourceModel;
import com.tanhd.rollingclass.server.data.UserData;
import com.tanhd.rollingclass.utils.AppUtils;
import com.tanhd.rollingclass.utils.ResultClass;

import java.util.ArrayList;
import java.util.List;

public class AnswerListFragment extends Fragment {
    private ListView mAnswerListView;

    private List<QuestionModel> mQuestionModelList;
    private AnswerCardAdapter mAdapter;

    private Handler mHandler = new Handler();
    private int mPageType;
    private Button mCommitButton;
    private String mQuestionSetId;

    public static AnswerListFragment getInstance(int pageType, String knowledgeId, String knowledgeName) {
        AnswerListFragment answerListFragment = new AnswerListFragment();
        Bundle args = new Bundle();
        args.putInt(LearnCasesActivity.PARAM_CLASS_STUDENT_PAGE, pageType);

        args.putString("KnowledgeID", knowledgeId);
        args.putString("KnowledgeName", knowledgeName);
        answerListFragment.setArguments(args);
        return answerListFragment;
    }

    public static AnswerListFragment getInstance(int pageType, String knowledgeId, String knowledgeName, String lessonSampleId, String lessonSampleName) {
        AnswerListFragment answerListFragment = new AnswerListFragment();
        Bundle args = new Bundle();
        args.putInt(LearnCasesActivity.PARAM_CLASS_STUDENT_PAGE, pageType);

        args.putString("KnowledgeID", knowledgeId);
        args.putString("KnowledgeName", knowledgeName);
        args.putString("LessonSampleID", lessonSampleId);
        args.putString("LessonSampleName", lessonSampleName);
        answerListFragment.setArguments(args);
        return answerListFragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View contentView = inflater.inflate(R.layout.fragment_answer_list, null);
        initParams();
        initViews(contentView);
        return contentView;
    }

    private void initParams() {
        Bundle args = getArguments();
        mPageType = args.getInt(LearnCasesActivity.PARAM_CLASS_STUDENT_PAGE);
    }

    private void initViews(View contentView) {
        mAnswerListView = contentView.findViewById(R.id.listview);
        mCommitButton = contentView.findViewById(R.id.commit_button);
        if (mPageType != KeyConstants.ClassPageType.TEACHER_CLASS_PAGE) {
            mCommitButton.setVisibility(View.VISIBLE);
            mCommitButton.setOnClickListener(onClickListener);
        } else {
            mCommitButton.setVisibility(View.GONE);
        }
        mAdapter = new AnswerCardAdapter(getActivity(), mPageType != KeyConstants.ClassPageType.TEACHER_CLASS_PAGE);
        mAnswerListView.setAdapter(mAdapter);
    }

    public void resetData(String questionSetId, List<QuestionModel> questionModelList) {
        mQuestionSetId = questionSetId;
        mQuestionModelList = questionModelList;
        if (mAdapter != null) {
            mAdapter.setData(mQuestionModelList);
        } else {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (mAdapter != null) {
                        mAdapter.setData(mQuestionModelList);
                    }
                }
            }, 500);
        }
    }

    public void clearListData() {
        if (mAdapter != null) {
            mAnswerListView.smoothScrollToPosition(0);
            mAdapter.setData(null);
            mAdapter.notifyDataSetChanged();
        }
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.commit_button:
                    new CommitAnswerTask(mAdapter.getData()).execute();
                    break;
            }
        }
    };


    private class CommitAnswerTask extends AsyncTask<Void, Void, Integer> {

        private List<QuestionModel> mQuestionList;

        CommitAnswerTask(List<QuestionModel> questionModelList) {
            mQuestionList = questionModelList;
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onPostExecute(Integer result) {
            if (result == 0) {
                Toast.makeText(getActivity().getApplicationContext(), "提交答案成功!", Toast.LENGTH_LONG).show();
                mCommitButton.setVisibility(View.GONE);
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
            for (int i = 0; i < mQuestionList.size(); i++) {
                ResultClass resultClass = mQuestionList.get(i).context.resultClass;
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

                QuestionModel questionData = mQuestionList.get(i);
                AnswerData answerData = new AnswerData();
                answerData.AnswerName = questionData.context.QuestionCategoryName;
                answerData.QuestionID = questionData.question_id;
                answerData.QuestionName = questionData.context.Stem;
                answerData.AnswerUserID = userID;
                answerData.AnswerType = 2;
                answerData.AnswerUserName = userName;
                answerData.KnowledgeID = getArguments().getString("KnowledgeID");
                answerData.KnowledgeName = getArguments().getString("KnowledgeName");
                answerData.LessonSampleID = getArguments().getString("LessonSampleID");
                answerData.LessonSampleName = getArguments().getString("LessonSampleName");
                answerData.TeacherID = questionData.teacher_id;
                answerData.TeacherName = questionData.teacher_name;
                answerData.ClassID = ExternalParam.getInstance().getClassData().ClassID;
                answerData.QuestionCategoryName = questionData.context.QuestionCategoryName;
                answerData.QuestionCategoryId = questionData.context.QuestionCategoryId;
                answerData.QuestionType = questionData.QuestionType;
                answerData.QuestionSetID = mQuestionSetId;
                answerData.GoodAnswer = questionData.context.Answer;
                answerData.Analysis = questionData.context.Analysis;

                if (isUrl) {
                    answerData.AnswerUrl = result;
                } else {
                    answerData.AnswerText = result;
                }
                answerList.add(answerData);
            }

            //提交答案到服务器
            for (AnswerData answerData : answerList) {
                String json = answerData.toJSON().toString();
                int errCode = ScopeServer.getInstance().InsertAnswerv2(json);
                if (errCode != 0) {
                    return errCode;
                }
            }

            return 0;
        }
    }
}
