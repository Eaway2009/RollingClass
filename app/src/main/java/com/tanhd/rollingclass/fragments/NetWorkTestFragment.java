package com.tanhd.rollingclass.fragments;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.tanhd.library.mqtthttp.MQTT;
import com.tanhd.library.mqtthttp.MqttListener;
import com.tanhd.library.mqtthttp.MyMqttService;
import com.tanhd.library.mqtthttp.PushMessage;
import com.tanhd.rollingclass.R;
import com.tanhd.rollingclass.server.data.ExternalParam;

import java.util.HashMap;

public class NetWorkTestFragment extends Fragment {
    private TextView mMessageView;
    private String mTargetID;
    private String mTargetName;
    private String mSenderName;
    private long mSequenceNo = 0;
    private long mReceiveNo = 0;
    private long mMissCount = 0;
    private boolean mIsRun = false;
    private Button mButtonView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mTargetName = ExternalParam.getInstance().getUserData().getOwnerName();
        View view = inflater.inflate(R.layout.fragment_network_test, container, false);
        mMessageView = view.findViewById(R.id.message);
        mButtonView = view.findViewById(R.id.button);
        mButtonView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Button button = (Button) v;
                String text = button.getText().toString();
                if (text.equals(getResources().getString(R.string.lbl_stop))) {
                    button.setText(getResources().getString(R.string.lbl_start_test));
                    mIsRun = false;
                    mSequenceNo = 0;
                    mReceiveNo = 0;
                    mMissCount = 0;
                    mHandler.removeMessages(0);
                } else {
                    button.setText(getResources().getString(R.string.lbl_stop));
                    mIsRun = true;
                    mHandler.sendEmptyMessage(0);
                }
            }
        });
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
        mHandler.removeMessages(0);
        MQTT.unregister(mqttListener);
    }

    private MqttListener mqttListener = new MqttListener() {
        @Override
        public void messageArrived(PushMessage message) {
            switch (message.command) {
                case PING_TEST_REPLY: {
                    mTargetID = message.from;
                    String sequenceNo = message.parameters.get("sequenceNo");
                    mSenderName = message.parameters.get("UserName");
                    mReceiveNo++;
                    break;
                }
                case PING_TEST: {
                    String sequenceNo = message.parameters.get("sequenceNo");
                    mSenderName =  message.parameters.get("UserName");
                    mMessageView.setText(String.format("【%s】正在测试网络....%s", mSenderName, sequenceNo));
                    message.parameters.put("UserName", mTargetName);
                    MyMqttService.publishMessage(PushMessage.COMMAND.PING_TEST_REPLY, message.from, message.parameters);
                    mButtonView.setVisibility(View.GONE);
                    break;
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
            if (!mIsRun)
                return;

            mMissCount = mSequenceNo - mReceiveNo;
            mSequenceNo++;
            HashMap<String, String> params = new HashMap<>();
            params.put("sequenceNo",  mSequenceNo + "");
            params.put("UserName", mTargetName);
            MyMqttService.publishMessage(PushMessage.COMMAND.PING_TEST, mTargetID, params);

            mMessageView.setText(String.format("测试终端:%s 发出:%d 收到:%d 丢失:%d", mSenderName, mSequenceNo, mReceiveNo, mMissCount));

            sendEmptyMessageDelayed(0, 500);
        }
    };
}
