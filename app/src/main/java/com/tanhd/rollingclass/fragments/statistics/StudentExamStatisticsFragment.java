package com.tanhd.rollingclass.fragments.statistics;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.tanhd.rollingclass.R;
import com.tanhd.rollingclass.activity.DocumentEditActivity;
import com.tanhd.rollingclass.fragments.FrameDialog;
import com.tanhd.rollingclass.fragments.QuerstionTypeShow;
import com.tanhd.rollingclass.fragments.resource.AnswerDisplayFragment;
import com.tanhd.rollingclass.server.ScopeServer;
import com.tanhd.rollingclass.server.data.AnswerModel;
import com.tanhd.rollingclass.server.data.AnswerSet;
import com.tanhd.rollingclass.server.data.CountClassLessonSampleData;
import com.tanhd.rollingclass.server.data.ExternalParam;
import com.tanhd.rollingclass.server.data.KnowledgeDetailMessage;
import com.tanhd.rollingclass.server.data.KnowledgeModel;
import com.tanhd.rollingclass.server.data.QuestionInfo;
import com.tanhd.rollingclass.server.data.QuestionModel;
import com.tanhd.rollingclass.server.data.QuestionStatistics;
import com.tanhd.rollingclass.server.data.StudentData;
import com.tanhd.rollingclass.server.data.UserData;
import com.tanhd.rollingclass.server.data.WrongAnswerList;
import com.tanhd.rollingclass.utils.MyValueFormatter;
import com.tanhd.rollingclass.views.BarChartView;

import java.util.ArrayList;
import java.util.List;

public class StudentExamStatisticsFragment extends Fragment{

    private static final int MODULE_ID_QUESTION_LIST = 0;
    private static final int ROOT_LAYOUT_ID = R.id.framelayout;
    private KnowledgeModel mKnowledgeModel;
    private KnowledgeDetailMessage mKnowledgeDetailMessage;

    private int mPageSize = 100;
    private boolean mIsRequesting = false;

    private AnswerDisplayFragment mQuestionResourceFragment;
    private int mCurrentShowModuleId = -1;

    private ArrayList<BarEntry> yVals;
    private List<String> xAxisValue;
    private BarChartView mBarChartView;

    public static StudentExamStatisticsFragment newInstance(KnowledgeModel knowledgeModel) {
        Bundle args = new Bundle();
        args.putSerializable(DocumentEditActivity.PARAM_TEACHING_MATERIAL_DATA, knowledgeModel);
        StudentExamStatisticsFragment page = new StudentExamStatisticsFragment();
        page.setArguments(args);
        return page;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_student_answer_statistics, container, false);
        initParams();
        initViews(view);
        initFragment();
        initData();
        return view;
    }

    private void initParams() {
        Bundle args = getArguments();
        mKnowledgeModel = (KnowledgeModel) args.getSerializable(DocumentEditActivity.PARAM_TEACHING_MATERIAL_DATA);
    }

    private void initViews(View view) {
        mBarChartView = view.findViewById(R.id.chartView);
        mBarChartView.setData(null, new String[]{"正确", "错误", "未提交"}, yVals, new MyValueFormatter("第", "题"), new MyValueFormatter("", "人"), "人");
        mBarChartView.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                String questionID = (String) e.getData();
                int stackIndex = h.getStackIndex();
                QuerstionTypeShow.TYPE type = QuerstionTypeShow.TYPE.CORRECT;
                if (stackIndex == 1)
                    type = QuerstionTypeShow.TYPE.ERROR;
                else if (stackIndex == 2)
                    type = QuerstionTypeShow.TYPE.NOANSWER;

                QuerstionTypeShow fragment = QuerstionTypeShow.newInstance(questionID, ExternalParam.getInstance().getUserData().getOwnerID(), type);
                FrameDialog.show(getChildFragmentManager(), fragment);
            }

            @Override
            public void onNothingSelected() {

            }
        });
    }

    private void initData() {
    }

    public void resetData(KnowledgeModel module, KnowledgeDetailMessage knowledgeDetailMessage) {
        mIsRequesting = true;
        mKnowledgeModel = module;
        mKnowledgeDetailMessage = knowledgeDetailMessage;
        if (mQuestionResourceFragment != null) {
            mQuestionResourceFragment.clearListData();
        }
        new InitQuestionDataTask().execute();
        new InitClassDataTask().execute();
    }

    private void initFragment() {
        showModulePage(MODULE_ID_QUESTION_LIST);
    }

    /**
     * [展示指定Id的页面]<BR>
     */
    public void showModulePage(int moduleId) {
        if (mCurrentShowModuleId == moduleId) {
            return;
        }
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        Fragment moduleFragment = null;
        if (moduleId == MODULE_ID_QUESTION_LIST) { //习题
            if (mQuestionResourceFragment == null) {
                mQuestionResourceFragment = AnswerDisplayFragment.newInstance(2);
                transaction.add(ROOT_LAYOUT_ID, mQuestionResourceFragment);
            }
            moduleFragment = mQuestionResourceFragment;
        }
        transaction.show(moduleFragment);
        transaction.commitAllowingStateLoss();

        mCurrentShowModuleId = moduleId;
    }

    private class InitQuestionDataTask extends AsyncTask<Void, Void, List<AnswerModel>> {

        @Override
        protected void onPreExecute() {
            mIsRequesting = true;
        }

        @Override
        protected List<AnswerModel> doInBackground(Void... voids) {
            UserData userData = ExternalParam.getInstance().getUserData();
            if (!userData.isTeacher()) {
                StudentData studentData = (StudentData) userData.getUserData();
                if (mKnowledgeModel != null) {
                    WrongAnswerList wrongAnswerList = ScopeServer.getInstance().QureyAnswerv2ByStudentIDAndCourseID(
                            studentData.StudentID, mKnowledgeDetailMessage.knowledge_id);
                    List<AnswerModel> questions = new ArrayList<>();
                    if(wrongAnswerList.questions!=null&&wrongAnswerList.questions.size()>0) {
                        for (AnswerModel answerModel : wrongAnswerList.questions) {
                            for (AnswerSet answerSet : wrongAnswerList.correct_set) {
                                if (answerModel.question_id.equals(answerSet.QuestionID)) {
                                    answerModel.answer_right = true;
                                }
                            }
                            questions.add(answerModel);
                        }
                    }
                    return questions;
                }
            } else {
                return null;
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<AnswerModel> documentList) {
            if (mQuestionResourceFragment == null) {
                mIsRequesting = false;
                return;
            }
            if (documentList != null && documentList.size() > 0) {
                mQuestionResourceFragment.setListData(documentList);
            } else {
                mQuestionResourceFragment.clearListData();
            }
            mIsRequesting = false;
        }
    }

    private class InitClassDataTask extends AsyncTask<Void, Void, List<AnswerModel>> {

        @Override
        protected void onPreExecute() {
            mIsRequesting = true;
        }

        @Override
        protected List<AnswerModel> doInBackground(Void... voids) {
            UserData userData = ExternalParam.getInstance().getUserData();
            if (!userData.isTeacher()) {
                StudentData studentData = (StudentData) userData.getUserData();
                if (mKnowledgeModel != null) {
                    QuestionStatistics statistics = ScopeServer.getInstance().QureyAnswerv2ByClassIDAndCourseID(
                            studentData.ClassID, mKnowledgeDetailMessage.knowledge_id);
                    if (statistics != null && statistics.question_info != null) {
                        yVals = new ArrayList<>();
                        xAxisValue = new ArrayList<>();
                        for (int i = 0; i < statistics.question_info.size(); i++) {
                            QuestionInfo data = statistics.question_info.get(i);
                            AnswerModel answerModel = data.question;
                            BarEntry entry = new BarEntry(answerModel.context.OrderIndex, new float[]{data.correct_cnt, data.error_cnt, data.unanswer_cnt}, answerModel.context.OrderIndex + "");
                            yVals.add(entry);
                            entry.setData(answerModel.question_id);
                            xAxisValue.add(String.format("第%d题", answerModel.context.OrderIndex));
                        }
                    }
                }
            } else {
                return null;
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<AnswerModel> documentList) {
            mBarChartView.invalidate();
        }
    }
}
