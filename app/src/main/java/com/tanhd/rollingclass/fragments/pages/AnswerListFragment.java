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
import android.widget.TextView;

import com.tanhd.rollingclass.base.MyMqttService;
import com.tanhd.library.mqtthttp.PushMessage;
import com.tanhd.rollingclass.R;
import com.tanhd.rollingclass.activity.LearnCasesActivity;
import com.tanhd.rollingclass.db.KeyConstants;
import com.tanhd.rollingclass.fragments.AnswerCardAdapter;
import com.tanhd.rollingclass.server.ScopeServer;
import com.tanhd.rollingclass.server.data.AnswerData;
import com.tanhd.rollingclass.server.data.ExternalParam;
import com.tanhd.rollingclass.server.data.QuestionModel;
import com.tanhd.rollingclass.server.data.UserData;
import com.tanhd.rollingclass.utils.AppUtils;
import com.tanhd.rollingclass.utils.ResultClass;
import com.tanhd.rollingclass.utils.ToastUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 答题卡列表
 */
public class AnswerListFragment extends Fragment {
    private ListView mAnswerListView;
    private TextView tv_answer_card;

    private List<QuestionModel> mQuestionModelList;
    private AnswerCardAdapter mAdapter;

    private Handler mHandler = new Handler();
    private int mPageType;
    private Button mCommitButton;
    private String mQuestionSetId;
    private View mShowAnswerButton;
    private Map<String, String> mParameters;
    private ExamListener mListener;
    private String mLessonSampleId;
    private String mLessonSampleName;
    private String mKnowledgeId;
    private String mKnowledgeName;
    private boolean isSubmitAnswer; //是否提交过答案

    public static AnswerListFragment getInstance(int pageType, String knowledgeId, String knowledgeName) {
        AnswerListFragment answerListFragment = new AnswerListFragment();
        Bundle args = new Bundle();
        args.putInt(LearnCasesActivity.PARAM_CLASS_STUDENT_PAGE, pageType);

        args.putString(LearnCasesActivity.PARAM_KNOWLEDGE_ID, knowledgeId);
        args.putString(LearnCasesActivity.PARAM_KNOWLEDGE_NAME, knowledgeName);
        answerListFragment.setArguments(args);
        return answerListFragment;
    }

    public static AnswerListFragment getInstance(int pageType, String knowledgeId, String knowledgeName, String lessonSampleId, String lessonSampleName,boolean isSubmitAnser) {
        AnswerListFragment answerListFragment = new AnswerListFragment();
        Bundle args = new Bundle();
        args.putInt(LearnCasesActivity.PARAM_CLASS_STUDENT_PAGE, pageType);

        args.putString(LearnCasesActivity.PARAM_KNOWLEDGE_ID, knowledgeId);
        args.putString(LearnCasesActivity.PARAM_KNOWLEDGE_NAME, knowledgeName);
        args.putString(LearnCasesActivity.PARAM_LESSON_SAMPLE_ID, lessonSampleId);
        args.putString(LearnCasesActivity.PARAM_LESSON_SAMPLE_NAME, lessonSampleName);
        args.putBoolean(LearnCasesActivity.PARAM_IS_SUBMIT_ANSWER,isSubmitAnser);
        answerListFragment.setArguments(args);
        return answerListFragment;
    }

    public static AnswerListFragment getInstance(int pageType, String knowledgeId, String knowledgeName, String lessonSampleId, String lessonSampleName, ExamListener listener) {
        AnswerListFragment answerListFragment = new AnswerListFragment();
        Bundle args = new Bundle();
        args.putInt(LearnCasesActivity.PARAM_CLASS_STUDENT_PAGE, pageType);

        args.putString(LearnCasesActivity.PARAM_KNOWLEDGE_ID, knowledgeId);
        args.putString(LearnCasesActivity.PARAM_KNOWLEDGE_NAME, knowledgeName);
        args.putString(LearnCasesActivity.PARAM_LESSON_SAMPLE_ID, lessonSampleId);
        args.putString(LearnCasesActivity.PARAM_LESSON_SAMPLE_NAME, lessonSampleName);
        answerListFragment.setArguments(args);
        answerListFragment.setListener(listener);
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
        mLessonSampleId = args.getString(LearnCasesActivity.PARAM_LESSON_SAMPLE_ID);
        mLessonSampleName = args.getString(LearnCasesActivity.PARAM_LESSON_SAMPLE_NAME);
        mKnowledgeId = args.getString(LearnCasesActivity.PARAM_KNOWLEDGE_ID);
        mKnowledgeName = args.getString(LearnCasesActivity.PARAM_KNOWLEDGE_NAME);
        isSubmitAnswer = args.getBoolean(LearnCasesActivity.PARAM_IS_SUBMIT_ANSWER,false);
    }

    private void initViews(View contentView) {
        tv_answer_card = contentView.findViewById(R.id.tv_answer_card);
        mAnswerListView = contentView.findViewById(R.id.listview);
        mCommitButton = contentView.findViewById(R.id.commit_button);
        if (mPageType != KeyConstants.ClassPageType.TEACHER_CLASS_PAGE) {
            mCommitButton.setVisibility(View.VISIBLE);
            tv_answer_card.setVisibility(View.VISIBLE);
            mCommitButton.setOnClickListener(onClickListener);
        } else {
            mCommitButton.setVisibility(View.GONE);
        }
        mAdapter = new AnswerCardAdapter(getActivity(), mPageType != KeyConstants.ClassPageType.TEACHER_CLASS_PAGE);
        mAnswerListView.setAdapter(mAdapter);

        mShowAnswerButton = contentView.findViewById(R.id.show_answers_button);
        UserData userData = ExternalParam.getInstance().getUserData();
        if (userData.isTeacher()) {
            mShowAnswerButton.setVisibility(View.VISIBLE);
        } else {
            mShowAnswerButton.setVisibility(View.GONE);
        }
        mShowAnswerButton.setOnClickListener(onClickListener);
    }

    public void setListener(ExamListener listener) {
        mListener = listener;
    }

    public void setShowAnswer(boolean setShowAnswer) {
        mAdapter.setShowAnswer(setShowAnswer);
    }

    public void resetData(String questionSetId, List<QuestionModel> questionModelList) {
        mParameters = new HashMap<>();
        mParameters.put(PushMessage.PARAM_LESSON_SAMPLE_ID, mLessonSampleId);
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
                case R.id.commit_button: //提交答案
                    new CommitAnswerTask(mAdapter.getData()).execute();
                    break;
                case R.id.show_answers_button: //公布答案
                    MyMqttService.publishMessage(PushMessage.COMMAND.SHOW_RIGHT_ANSWER, (List<String>) null, mParameters);
                    ToastUtil.show(R.string.toast_change_ok);
                    break;
            }
        }
    };


    /**
     * 提交答案
     */
    private class CommitAnswerTask extends AsyncTask<Void, Void, Integer> {
        private List<QuestionModel> mQuestionList;
        CommitAnswerTask(List<QuestionModel> questionModelList) {
            if (questionModelList == null) questionModelList = new ArrayList<>();
            mQuestionList = questionModelList;
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onPostExecute(Integer result) {
            if (result == 0) {
                ToastUtil.show(R.string.toast_answer_ok);
                mAdapter.setAnswerCommitted(true);
                mCommitButton.setVisibility(View.GONE);
                if (mListener != null) {
                    mListener.onFinished(mAdapter.getAnswer());
                }
            } else if (result == -2) {
                ToastUtil.show(R.string.toast_answer_all);
            } else {
                ToastUtil.show(R.string.toast_answer_fail);
            }
        }

        @Override
        protected Integer doInBackground(Void... voids) {
            UserData userData = ExternalParam.getInstance().getUserData();
            String userID = "";
            String userName = "";
            if (userData != null) {
                userID = userData.getOwnerID();
                userName = userData.getOwnerName();
            }
            ArrayList<AnswerData> answerList = new ArrayList<>();
            for (int i = 0; i < mQuestionList.size(); i++) {
                ResultClass resultClass = mQuestionList.get(i).context.resultClass;
                boolean isUrl = false;
                if (resultClass == null) {
                    return -2;
                }
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
                    return -2;
                }

                QuestionModel questionData = mQuestionList.get(i);
                AnswerData answerData = new AnswerData();
                answerData.AnswerName = questionData.context.QuestionCategoryName;
                answerData.QuestionID = questionData.question_id;
                answerData.QuestionName = questionData.context.Stem;
                answerData.AnswerUserID = userID;
                answerData.AnswerType = 2;
                answerData.AnswerUserName = userName;
                answerData.KnowledgeID = mKnowledgeId;
                answerData.KnowledgeName = mKnowledgeName;
                answerData.LessonSampleID = TextUtils.isEmpty(mLessonSampleId) ? "" : mLessonSampleId;
                ;
                answerData.LessonSampleName = mLessonSampleName;
                answerData.TeacherID = questionData.teacher_id;
                answerData.TeacherName = questionData.teacher_name;
                answerData.ClassID = ExternalParam.getInstance().getClassData().ClassID;
                answerData.QuestionCategoryName = questionData.context.QuestionCategoryName;
                answerData.QuestionCategoryId = questionData.context.QuestionCategoryId;
                answerData.QuestionType = questionData.QuestionType;
                answerData.QuestionSetID = TextUtils.isEmpty(mQuestionSetId) ? "undefine" : mQuestionSetId;
                answerData.GoodAnswer = questionData.context.Answer;
                answerData.Analysis = questionData.context.Analysis;
                answerData.Modify = 2;

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


    public static interface ExamListener {
        void onFinished(String answer);
    }
}
