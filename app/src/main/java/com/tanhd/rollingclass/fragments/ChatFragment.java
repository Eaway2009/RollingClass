package com.tanhd.rollingclass.fragments;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.tanhd.library.mqtthttp.MQTT;
import com.tanhd.library.mqtthttp.MqttListener;
import com.tanhd.library.mqtthttp.PushMessage;
import com.tanhd.rollingclass.R;
import com.tanhd.rollingclass.db.Database;
import com.tanhd.rollingclass.db.MSG_TYPE;
import com.tanhd.rollingclass.db.Message;
import com.tanhd.rollingclass.fragments.pages.CommentAnswerPage;
import com.tanhd.rollingclass.fragments.pages.QuestionAnswerPage;
import com.tanhd.rollingclass.server.RequestCallback;
import com.tanhd.rollingclass.server.ScopeServer;
import com.tanhd.rollingclass.server.ServerRequest;
import com.tanhd.rollingclass.server.data.AnswerData;
import com.tanhd.rollingclass.server.data.ClassData;
import com.tanhd.rollingclass.server.data.ExternalParam;
import com.tanhd.rollingclass.server.data.LessonSampleData;
import com.tanhd.rollingclass.server.data.QuestionData;
import com.tanhd.rollingclass.server.data.StudentData;
import com.tanhd.rollingclass.server.data.TeacherData;
import com.tanhd.rollingclass.server.data.UserData;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ChatFragment extends Fragment {
    private TextView mTitleView;
    private ListView mListView;
    private EditText mInputView;
    private ProgressBar mProgressBar;
    private List<Message> mMessageList;
    private MessageAdapter mAdapter;
    private String mChatID;

    public static ChatFragment newInstance(String chatID) {
        Bundle args = new Bundle();
        args.putString("chatID", chatID);
        ChatFragment fragment = new ChatFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mChatID = getArguments().getString("chatID");
        View view = inflater.inflate(R.layout.fragment_chat, container, false);
        mTitleView = view.findViewById(R.id.title);
        mListView = view.findViewById(R.id.list);
        mInputView = view.findViewById(R.id.input);
        mProgressBar = view.findViewById(R.id.progressbar);
        view.findViewById(R.id.send).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                send();
            }
        });
        init();
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        MQTT.register(mqttListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        MQTT.unregister(mqttListener);
    }

    private MqttListener mqttListener = new MqttListener() {
        @Override
        public void messageArrived(PushMessage message) {
            if (message.command == PushMessage.COMMAND.MESSAGE) {
                refresh();
            }
        }

        @Override
        public void networkTimeout(boolean flag) {

        }
    };

    private void init() {
        mMessageList = Database.getInstance().readChatMessage(mChatID);
        mAdapter = new MessageAdapter();
        mListView.setAdapter(mAdapter);
        mTitleView.setText(mChatID);

        new LoadDataTask().execute();
    }

    private void refresh() {
        mMessageList = Database.getInstance().readChatMessage(mChatID);
        mAdapter.notifyDataSetChanged();
    }

    private void send() {
        String text = mInputView.getText().toString();
        HashMap<String, String> params = new HashMap<>();
        params.put("content", text);
        params.put("type", String.valueOf(MSG_TYPE.TEXT.ordinal()));
        MQTT.publishMessage(PushMessage.COMMAND.MESSAGE, mChatID, params);
        Database.getInstance().newMessage(ExternalParam.getInstance().getUserData().getOwnerID(), mChatID, MSG_TYPE.TEXT, text, 1);
        mInputView.setText(null);
        refresh();
    }

    private class MessageAdapter extends BaseAdapter {

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
            LinearLayout view = (LinearLayout) convertView;
            if (view == null) {
                view = (LinearLayout) getLayoutInflater().inflate(R.layout.item_chat_message, parent, false);
            }
            view.setOnClickListener(null);
            final Message message = mMessageList.get(position);

            TextView contentView = view.findViewById(R.id.content);
            CardView cardView = view.findViewById(R.id.card);

            if (!message.fromId.equals(mChatID)) {
                view.setGravity(Gravity.RIGHT);
                cardView.setCardBackgroundColor(0xFF44FF33);
                contentView.setTextColor(0xFFFFFFFF);
            } else {
                view.setGravity(Gravity.LEFT);
                cardView.setCardBackgroundColor(0xFFFFFFFF);
                contentView.setTextColor(0xFF000000);
            }

            contentView.setText(message.content);
            return view;
        }
    }

    private class LoadDataTask extends AsyncTask<Void, Void, Void> {
        TeacherData teacherData;
        StudentData studentData;

        @Override
        protected Void doInBackground(Void... voids) {
            UserData userData = ExternalParam.getInstance().getUserData();
            if (!userData.isTeacher())
                teacherData = ScopeServer.getInstance().QureyTeacherByTeacherID(mChatID);
            else
                studentData = ScopeServer.getInstance().getStudentData(mChatID);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (teacherData != null) {
                mTitleView.setText(teacherData.Username);
            } else if (studentData != null) {
                mTitleView.setText(studentData.Username);
            }
        }
    }
}
