package com.tanhd.rollingclass.fragments;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.tanhd.library.mqtthttp.MQTT;
import com.tanhd.library.mqtthttp.MqttListener;
import com.tanhd.library.mqtthttp.PushMessage;
import com.tanhd.rollingclass.R;
import com.tanhd.rollingclass.db.Database;
import com.tanhd.rollingclass.server.ScopeServer;
import com.tanhd.rollingclass.server.data.AnswerData;
import com.tanhd.rollingclass.server.data.ExternalParam;
import com.tanhd.rollingclass.server.data.QuestionSetData;
import com.tanhd.rollingclass.server.data.StudentData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class WaitAnswerFragment extends Fragment {
    private class StudentScore {
        String studentID;
        long time;
        int score;
    }

    private List<StudentData> mStudentList;
    private String mSetID;

    private GridView mGridView;
    private StudentAdapter mAdapter;
    private QuestionSetData mQuestionSetData = null;
    private HashMap<String, StudentScore> mRankMap = new HashMap<>();

    private ListView mRankView;
    private RankAdapter mRankAdapter;

    public static WaitAnswerFragment newInstance(String setID) {
        WaitAnswerFragment fragment = new WaitAnswerFragment();
        Bundle args = new Bundle();
        if (setID != null)
            args.putString("setID", setID);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mSetID = getArguments().getString("setID");
        View view = inflater.inflate(R.layout.fragment_wait_answer, container, false);
        mGridView = view.findViewById(R.id.grid_view);
        if (getParentFragment() instanceof FrameDialog) {
            view.findViewById(R.id.done).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DialogFragment dialog = (DialogFragment) getParentFragment();
                    dialog.dismiss();
                }
            });
        } else {
            view.findViewById(R.id.done).setVisibility(View.GONE);
        }
        mRankView = view.findViewById(R.id.list);

        new QuerySetTask().execute();
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        MQTT.register(mqttListener);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        MQTT.unregister(mqttListener);
    }

    private class StudentAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            if (mStudentList == null)
                return 0;

            return mStudentList.size();
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
                view = getLayoutInflater().inflate(R.layout.item_student_selector, parent, false);
            }

            final StudentData studentData = mStudentList.get(position);
            boolean state = mRankMap.containsKey(studentData.StudentID);

            TextView nameView = view.findViewById(R.id.name);
            nameView.setText(studentData.Username);
            TextView statusView = view.findViewById(R.id.status);

            if (!state) {
                statusView.setText("未完成");
                statusView.setBackgroundColor(getResources().getColor(R.color.offline));
                view.setOnClickListener(null);
            } else {
                statusView.setText("完成");
                statusView.setBackgroundColor(getResources().getColor(R.color.online));
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        FrameDialog.fullShow(getChildFragmentManager(), ExamMarkFragment.newInstance(studentData.StudentID, mSetID, null));
                    }
                });
            }

            return view;
        }
    }

    private void refresh() {
        new QuerySetTask().execute();
    }

    private MqttListener mqttListener = new MqttListener() {
        @Override
        public void messageArrived(PushMessage message) {
            if (message.command != PushMessage.COMMAND.ANSWER_COMPLETED)
                return;
            refresh();
        }

        @Override
        public void networkTimeout(boolean flag) {

        }
    };

    private class QuerySetTask extends AsyncTask<Void, Void, Integer> {

        @Override
        protected Integer doInBackground(Void... voids) {
            if (mQuestionSetData == null) {
                String teacherID = ExternalParam.getInstance().getUserData().getOwnerID();
                List<QuestionSetData> setDataList = ScopeServer.getInstance().QureyQuestionSetByTeacherID(teacherID);
                if (setDataList == null || setDataList.isEmpty())
                    return -1;

                if (mSetID == null) {
                    mQuestionSetData = setDataList.get(0);
                } else {
                    for (int i=0; i<setDataList.size(); i++) {
                        if (setDataList.get(i).QuestionSetID.equals(mSetID)) {
                            mQuestionSetData = setDataList.get(i);
                            break;
                        }
                    }
                }
                if (mQuestionSetData == null)
                    return -1;

//                mStudentList = new ArrayList<>();
//                if (mQuestionSetData.student_list != null && !mQuestionSetData.student_list.isEmpty()) {
//                    for (int i=0; i<mQuestionSetData.student_list.size(); i++) {
//                        String studentID = mQuestionSetData.student_list.get(i);
//                        StudentData studentData = ExternalParam.getInstance().queryStudent(studentID);
//                        if (studentData != null)
//                            mStudentList.add(studentData);
//                    }
//                }
            }


            List<AnswerData> answerDataList = ScopeServer.getInstance().QureyAnswerv2BySetID(mSetID);
            if (answerDataList != null) {
                for (AnswerData answerData: answerDataList) {
                    StudentScore score = mRankMap.get(answerData.AnswerUserID);
                    if (score == null) {
                        score = new StudentScore();
                        score.studentID = answerData.AnswerUserID;
                        score.time = (answerData.CreateTime - mQuestionSetData.CreateTime) / 1000;
                        mRankMap.put(answerData.AnswerUserID, score);
                    }
                    score.score += answerData.Score;
                }
            }

            return 0;
        }

        @Override
        protected void onPostExecute(Integer result) {
            if (result != 0) {
                Toast.makeText(getContext().getApplicationContext(), "加载数据失败!", Toast.LENGTH_LONG).show();
                return;
            }

            mAdapter = new StudentAdapter();
            mGridView.setAdapter(mAdapter);

            mRankAdapter = new RankAdapter();
            mRankView.setAdapter(mRankAdapter);
        }
    }

    private class RankAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            if (mRankMap == null)
                return 0;

            return mRankMap.size();
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
            if (view == null)
                view = getLayoutInflater().inflate(R.layout.item_wait_answer, parent, false);

            ArrayList<StudentScore> list = new ArrayList<>(mRankMap.values());
            StudentScore value = list.get(position);
            final StudentData studentData = ExternalParam.getInstance().queryStudent(value.studentID);
            TextView noView = view.findViewById(R.id.no);
            TextView nameView = view.findViewById(R.id.name);
            TextView timeView = view.findViewById(R.id.time);
            TextView scoreView = view.findViewById(R.id.score);

            noView.setText(String.format("第%d名", position + 1));
            nameView.setText(studentData.Username);
            timeView.setText(String.format("耗时%d秒", value.time));
            scoreView.setText(String.format("得分:%d", value.score));

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FrameDialog.fullShow(getChildFragmentManager(), ExamMarkFragment.newInstance(studentData.StudentID, mSetID, null));
                }
            });

            return view;
        }
    }
}
