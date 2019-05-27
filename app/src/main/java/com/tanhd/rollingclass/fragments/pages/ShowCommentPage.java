package com.tanhd.rollingclass.fragments.pages;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tanhd.library.mqtthttp.MQTT;
import com.tanhd.library.mqtthttp.MqttListener;
import com.tanhd.library.mqtthttp.PushMessage;
import com.tanhd.library.smartpen.MyDot;
import com.tanhd.library.smartpen.SmartPenView;
import com.tanhd.rollingclass.R;
import com.tanhd.rollingclass.fragments.FrameDialog;
import com.tanhd.rollingclass.server.data.AnswerData;
import com.tanhd.rollingclass.server.data.QuestionData;
import com.tanhd.rollingclass.views.QuestionAnswerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ShowCommentPage extends Fragment {
    private QuestionData mQuestionData;
    private AnswerData mAnswerData;
    private ArrayList<MyDot> mDotCache = new ArrayList<>();
    private SmartPenView mSmartPenView;

    public static ShowCommentPage newInstance(QuestionData questionData, AnswerData answerData) {
        Bundle args = new Bundle();
        args.putSerializable("questionData", questionData);
        args.putSerializable("answerData", answerData);
        ShowCommentPage page = new ShowCommentPage();
        page.setArguments(args);
        return page;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mQuestionData = (QuestionData) getArguments().get("questionData");
        mAnswerData = (AnswerData) getArguments().get("answerData");
        View view = inflater.inflate(R.layout.page_show_comment, container, false);
        QuestionAnswerView questionAnswerView = view.findViewById(R.id.question_view);
        questionAnswerView.setData(mQuestionData, mAnswerData);
        mSmartPenView = view.findViewById(R.id.smartpen);
        mHandler.sendEmptyMessage(0);
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

    private MqttListener mqttListener = new MqttListener() {
        @Override
        public void messageArrived(PushMessage message) {
            if (message.command == PushMessage.COMMAND.SMART_PEN_DOT) {
                String data = message.parameters.get("data");
                try {
                    JSONArray array = new JSONArray(data);
                    for (int i=0; i<array.length(); i++) {
                        JSONObject json = array.optJSONObject(i);
                        MyDot dot = new MyDot();
                        dot.x = json.getInt("x");
                        dot.fx = json.getInt("fx");
                        dot.y = json.getInt("y");
                        dot.fy = json.getInt("fy");
                        dot.type = json.getInt("type");
                        synchronized (mDotCache) {
                            mDotCache.add(dot);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else if (message.command == PushMessage.COMMAND.SMART_PEN_COLOR) {
                int color = Integer.parseInt(message.parameters.get("color"));
                mSmartPenView.setPenColor(color);
            } else if (message.command == PushMessage.COMMAND.COMMENT_END) {
                if (getParentFragment() instanceof FrameDialog) {
                    DialogFragment dialog = (DialogFragment) getParentFragment();
                    dialog.dismiss();
                }
            }
        }

        @Override
        public void networkTimeout(boolean flag) {

        }
    };

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            synchronized (mDotCache) {
                while (mDotCache.size() > 0) {
                    MyDot dot = mDotCache.remove(0);
                    mSmartPenView.drawDot(dot);
                }

                sendEmptyMessageDelayed(0, 1000);
            }
        }
    };
}
