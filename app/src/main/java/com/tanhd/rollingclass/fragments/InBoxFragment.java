package com.tanhd.rollingclass.fragments;

import android.annotation.SuppressLint;
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
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.tanhd.library.mqtthttp.MQTT;
import com.tanhd.library.mqtthttp.PushMessage;
import com.tanhd.rollingclass.R;
import com.tanhd.rollingclass.db.Database;
import com.tanhd.rollingclass.db.MSG_TYPE;
import com.tanhd.rollingclass.db.Message;
import com.tanhd.rollingclass.fragments.pages.CommentAnswerPage;
import com.tanhd.rollingclass.server.ScopeServer;
import com.tanhd.rollingclass.server.data.AnswerData;
import com.tanhd.rollingclass.server.data.ExternalParam;
import com.tanhd.rollingclass.server.data.QuestionData;
import com.tanhd.rollingclass.server.data.StudentData;
import com.tanhd.rollingclass.server.data.TeacherData;
import com.tanhd.rollingclass.server.data.UserData;
import com.tanhd.rollingclass.utils.AppUtils;
import com.tanhd.rollingclass.utils.GlobalWork;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class InBoxFragment extends Fragment {
    public static interface SelectorListener {
        void onMessageSelected(Message message);
    }
    private ListView mListView;
    private TextView mNoMessageView;
    private DataAdapter mAdapter;
    private List<Message> mMessageList;
    private SelectorListener mListener;

    public static InBoxFragment newInstance(SelectorListener listener) {
        InBoxFragment fragment = new InBoxFragment();
        fragment.setListener(listener);
        return fragment;
    }

    private void setListener(SelectorListener listener) {
        mListener = listener;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_inbox, container, false);
        mListView = view.findViewById(R.id.list);
        mNoMessageView = view.findViewById(R.id.no_message);
        init();
        return view;
    }

    private void init() {
        mMessageList = Database.getInstance().readAllMessage();
        mAdapter = new DataAdapter();
        mListView.setAdapter(mAdapter);

        if (mMessageList.size() == 0) {
            mNoMessageView.setVisibility(View.VISIBLE);
        } else {
            mNoMessageView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (mListener != null)
            mListener.onMessageSelected(null);
    }

    private class DataAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mMessageList.size();
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
                view = getLayoutInflater().inflate(R.layout.item_inbox_message, parent, false);
            }

            final Message message = mMessageList.get(position);
            TextView titleView = view.findViewById(R.id.title);
            TextView fromView = view.findViewById(R.id.from);
            ImageView iconView = view.findViewById(R.id.iv_icon);
            TextView contentView = view.findViewById(R.id.content);
            TextView timeView = view.findViewById(R.id.time);

            if (ExternalParam.getInstance().getUserData().isTeacher()) {
                StudentData studentData = ExternalParam.getInstance().queryStudent(message.fromId);
                if (studentData != null)
                    fromView.setText(getResources().getString(R.string.lbl_from) + studentData.Username);
            } else {
                fromView.setText(getResources().getString(R.string.lbl_from) + message.fromId);
                new QueryTeacherTask(fromView).execute(message.fromId);
            }
            timeView.setText(getResources().getString(R.string.lbl_time) + AppUtils.dateToString(message.time));

            switch (message.type) {
                case TEXT: {
                    titleView.setText(getResources().getString(R.string.lbl_message));
                    contentView.setText(message.content);
                    view.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            openMessage(message);
                        }
                    });
                    break;
                }
                case EXERCISE_COMPLETED: {
                    try {
                        JSONObject json = new JSONObject(message.content);
                        final String studentID = json.optString("StudentID");
                        String studentName = json.optString("StudentName");
                        String lessonSampleName = json.optString("LessonSampleName");
                        final JSONArray ids = json.optJSONArray("ids");

                        titleView.setText(String.format(getResources().getString(R.string.lbl_end_lx)));
                        String text = String.format(getResources().getString(R.string.inbox_tj_hint), studentName, lessonSampleName);
                        contentView.setText(text);

                        view.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Database.getInstance().resetMessage(message._id);
                                exerciseCompleted(studentID, ids);
                            }
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    break;
                }
            }

            return view;
        }
    }

    private void openMessage(Message message) {
        if (mListener != null) {
            mListener.onMessageSelected(message);
        }

        DialogFragment dialog = (DialogFragment) getParentFragment();
        if (dialog != null) {
            dialog.dismiss();
        }
    }

    private void exerciseCompleted(String studentID, JSONArray ids) {

    }

    private class QueryTeacherTask extends AsyncTask<String, Void, String> {
        private final TextView nameView;
        public QueryTeacherTask(TextView view) {
            nameView = view;
        }

        @Override
        protected String doInBackground(String... strings) {
            TeacherData teacherData = ScopeServer.getInstance().QureyTeacherByTeacherID(strings[0]);
            if (teacherData != null)
                return teacherData.Username;
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            if (s != null)
                nameView.setText(s);
            else
                nameView.setText("未知");
        }
    }
}
