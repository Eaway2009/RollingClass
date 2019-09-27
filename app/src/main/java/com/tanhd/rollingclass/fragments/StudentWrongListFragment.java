package com.tanhd.rollingclass.fragments;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.tanhd.library.mqtthttp.MQTT;
import com.tanhd.library.mqtthttp.PushMessage;
import com.tanhd.rollingclass.R;
import com.tanhd.rollingclass.db.MSG_TYPE;
import com.tanhd.rollingclass.fragments.FrameDialog;
import com.tanhd.rollingclass.fragments.StudentSelectorFragment;
import com.tanhd.rollingclass.fragments.pages.CommentAnswerPage;
import com.tanhd.rollingclass.server.ScopeServer;
import com.tanhd.rollingclass.server.data.AnswerData;
import com.tanhd.rollingclass.server.data.ClassData;
import com.tanhd.rollingclass.server.data.ExternalParam;
import com.tanhd.rollingclass.server.data.GroupData;
import com.tanhd.rollingclass.server.data.QuestionModel;
import com.tanhd.rollingclass.server.data.StudentData;
import com.tanhd.rollingclass.server.data.UserData;
import com.tanhd.rollingclass.utils.ToastUtil;
import com.tanhd.rollingclass.views.QuestionAnswerView;
import com.tanhd.rollingclass.views.StudentListView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class StudentWrongListFragment extends Fragment {
    private QuestionAdapter mQuestionAdapter;
    private ListView mQuestionListView;
    private ProgressBar mProgressBar;
    private StudentListView mStudentListView;

    private HashMap<String, AnswerData> mAnswerMap = new HashMap<>();
    private List<QuestionModel> mQuestionList = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_student_wrong_list, container, false);
        init(view);
        return view;
    }

    private void init(View view) {
        mProgressBar = view.findViewById(R.id.progressbar);
        mStudentListView = view.findViewById(R.id.student_view);
        mStudentListView.enableClass(false);
        mStudentListView.setListener(new StudentListView.StudentListViewListener() {
            @Override
            public void onSelectedItem(StudentData studentData) {
                if (studentData == null)
                    return;

                new LoadDataTask().execute(studentData.StudentID);
            }
        });
        mQuestionListView = view.findViewById(R.id.question_list);
        ClassData classData = ExternalParam.getInstance().getClassData();
        if (classData.Groups == null)
            return;
        TextView classNameView = view.findViewById(R.id.class_name);
        classNameView.setText(classData.ClassName);

        mQuestionAdapter = new QuestionAdapter();
        mQuestionListView.setAdapter(mQuestionAdapter);
    }

    private class LoadDataTask extends AsyncTask<String, Void, Integer> {

        @Override
        protected Integer doInBackground(String... strings) {
            mAnswerMap.clear();
            mQuestionList.clear();

            String studentID = strings[0];
            List<AnswerData> answerList = ScopeServer.getInstance().QureyErrorAnswerv2ByStudentID(studentID);
            if (answerList == null)
                return -1;

            for (AnswerData answerData: answerList) {
                if (answerData.QuestionSetID != null)
                    continue;

                List<QuestionModel> questionList = ScopeServer.getInstance().QureyQuestionByID(answerData.QuestionID);
                if (questionList == null || questionList.isEmpty())
                    continue;
                QuestionModel questionData = questionList.get(0);
                mAnswerMap.put(questionData.question_id, answerData);
                mQuestionList.add(questionData);
            }

            if (mAnswerMap.isEmpty())
                return -2;

            QuestionModel.sort(mQuestionList);
            return 0;
        }

        @Override
        protected void onPreExecute() {
            mProgressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(Integer integer) {
            mProgressBar.setVisibility(View.GONE);
            mQuestionAdapter.notifyDataSetChanged();
            if (integer < 0) {
                ToastUtil.show(R.string.toast_wrong_emtpy);
                return;
            }
        }
    }

    private class QuestionAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            if (mQuestionList == null)
                return 0;

            return mQuestionList.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (view == null) {
                view = getLayoutInflater().inflate(R.layout.item_student_wrong_list_question, parent, false);
            }

            final QuestionModel questionData = mQuestionList.get(position);
            final AnswerData answerData = mAnswerMap.get(questionData.question_id);
            final QuestionAnswerView answerView = view.findViewById(R.id.answer_layout);
            answerView.setData(questionData, answerData);

            view.findViewById(R.id.btn_comment).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    answerView.requestStudentToMark(getChildFragmentManager());
                }
            });

            view.findViewById(R.id.btn_comment_self).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FrameDialog.fullShow(getChildFragmentManager(), CommentAnswerPage.newInstance(questionData, answerData));
                }
            });

            return view;
        }
    }
}
