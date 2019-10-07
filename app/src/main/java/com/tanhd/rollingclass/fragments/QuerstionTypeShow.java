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
import android.widget.ListView;
import android.widget.TextView;

import com.tanhd.rollingclass.R;
import com.tanhd.rollingclass.server.ScopeServer;
import com.tanhd.rollingclass.server.data.AnswerData;
import com.tanhd.rollingclass.server.data.ExternalParam;
import com.tanhd.rollingclass.server.data.GroupData;
import com.tanhd.rollingclass.server.data.StudentData;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class QuerstionTypeShow extends Fragment {
    public static enum TYPE {
        CORRECT,
        NOANSWER,
        ERROR,
    }

    public static QuerstionTypeShow newInstance(String questionID, String teacherID, TYPE type) {
        Bundle args = new Bundle();
        args.putInt("type", type.ordinal());
        args.putString("questionID", questionID);
        args.putString("teacherID", teacherID);
        QuerstionTypeShow fragment = new QuerstionTypeShow();
        fragment.setArguments(args);
        return fragment;
    }

    private class Item {
        String studentID;
        String studentName;
        int score;
    }

    private ListView listView;
    private List<Item> mAnswerList;
    private StudentAdapter mAdapter;
    private String mTeacherID;
    private String mQuestionID;
    private TYPE mType;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mTeacherID = getArguments().getString("teacherID");
        mQuestionID = getArguments().getString("questionID");
        mType = TYPE.values()[getArguments().getInt("type")];

        View view = inflater.inflate(R.layout.fragment_querstion_type_show, container, false);
        listView = view.findViewById(R.id.list);
        mAdapter = new StudentAdapter();
        listView.setAdapter(mAdapter);
        new LoadDataTask().execute();
        return view;
    }

    private class LoadDataTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            HashSet<String> studentSet = new HashSet<>();
            for (GroupData groupData : ExternalParam.getInstance().getClassData().Groups) {
                for (StudentData studentData: groupData.StudentList) {
                    studentSet.add(studentData.StudentID);
                }
            }

            mAnswerList = new ArrayList<>();
            List<AnswerData> list = ScopeServer.getInstance().QureyAnswerv2ByTeacherIDAndQuestionID(mTeacherID, mQuestionID);
            for (AnswerData answerData: list) {
                if (answerData.QuestionSetID != null)
                    continue;

                if (mType == TYPE.CORRECT) {
                    if (answerData.Score == 5) {
                        Item item = new Item();
                        item.score = answerData.Score;
                        item.studentID = answerData.AnswerUserID;
                        item.studentName = answerData.AnswerUserName;

                        mAnswerList.add(item);
                    }
                } else if (mType == TYPE.NOANSWER) {
                    studentSet.remove(answerData.AnswerUserID);
                } else if (mType == TYPE.ERROR) {
                    if (answerData.Score != 5) {
                        Item item = new Item();
                        item.score = answerData.Score;
                        item.studentID = answerData.AnswerUserID;
                        item.studentName = answerData.AnswerUserName;
                        mAnswerList.add(item);
                    }
                }
            }

            if (mType == TYPE.NOANSWER) {
                for (String studentID: studentSet) {
                    StudentData studentData = ExternalParam.getInstance().queryStudent(studentID);
                    if (studentData != null) {
                        Item item = new Item();
                        item.score = 0;
                        item.studentID = studentData.StudentID;
                        item.studentName = studentData.Username;
                        mAnswerList.add(item);
                    }
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            mAdapter.notifyDataSetChanged();
        }
    }

    private class StudentAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            if (mAnswerList == null)
                return 0;

            return mAnswerList.size();
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
                view = getLayoutInflater().inflate(R.layout.question_type_show_item, parent, false);
            }

            Item item = mAnswerList.get(position);
            TextView nameView = view.findViewById(R.id.name);
            nameView.setText(item.studentName);
            TextView statusView = view.findViewById(R.id.status);
            statusView.setText(item.score + getResources().getString(R.string.lbl_min));

            return view;
        }
    }
}
