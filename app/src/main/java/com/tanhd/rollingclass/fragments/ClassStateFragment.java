package com.tanhd.rollingclass.fragments;

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
import android.widget.Button;
import android.widget.TextView;

import com.tanhd.library.mqtthttp.MQTT;
import com.tanhd.library.mqtthttp.MqttListener;
import com.tanhd.library.mqtthttp.MyMqttService;
import com.tanhd.library.mqtthttp.PushMessage;
import com.tanhd.rollingclass.R;
import com.tanhd.rollingclass.server.data.ClassData;
import com.tanhd.rollingclass.server.data.ExternalParam;
import com.tanhd.rollingclass.server.data.GroupData;
import com.tanhd.rollingclass.server.data.StudentData;

import java.util.HashMap;
import java.util.List;

public class ClassStateFragment extends Fragment {
    private HashMap<String, View> mViewMap = new HashMap<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_class_state, container, false);
        init(view);
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        MQTT.register(mqttListener);
        mHandler.sendEmptyMessage(0);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        MQTT.unregister(mqttListener);
        mHandler.removeMessages(0);
    }

    private void init(View view) {
        ViewGroup layoutView = view.findViewById(R.id.layout);
        ClassData classData = ExternalParam.getInstance().getClassData();

        layoutView.removeAllViews();
        TextView classNameView = view.findViewById(R.id.class_name);

        classNameView.setText(classData.ClassName);
        if (classData.Groups == null)
            return;

        for (GroupData groupData: classData.Groups) {
            ViewGroup viewGroup = (ViewGroup) getLayoutInflater().inflate(R.layout.item_student_selector_group, layoutView, false);
            TextView groupName = viewGroup.findViewById(R.id.group_name);
            groupName.setText(groupData.GroupName);

            for (final StudentData studentData: groupData.StudentList) {
                if (studentData == null)
                    continue;
                ViewGroup layout = viewGroup.findViewById(R.id.container);
                View v = getLayoutInflater().inflate(R.layout.item_student_selector, layout, false);
                updateView(studentData, v);
                mViewMap.put(studentData.StudentID, v);
                layout.addView(v);
            }

            layoutView.addView(viewGroup);
        }
    }

    private void updateView(StudentData studentData, View v) {
        v.setTag(studentData);
        TextView nameView = v.findViewById(R.id.name);
        nameView.setText(studentData.Username);
        TextView statusView = v.findViewById(R.id.status);
        if (studentData.Status == 1) {
            statusView.setBackgroundColor(getResources().getColor(R.color.online));
            statusView.setText("在线");
        } else {
            statusView.setBackgroundColor(getResources().getColor(R.color.offline));
            statusView.setText("离线");
        }
    }

    private MqttListener mqttListener = new MqttListener() {
        @Override
        public void messageArrived(PushMessage message) {
            switch (message.command) {
                case OFFLINE:
                case ONLINE:
                    View view = mViewMap.get(message.from);
                    if (view == null)
                        return;
                    StudentData studentData = (StudentData) view.getTag();
                    studentData.Status = (message.command == PushMessage.COMMAND.ONLINE ? 1 : 0);
                    updateView(studentData, view);
                    break;
            }
        }

        @Override
        public void networkTimeout(boolean flag) {

        }
    };

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (!isAdded())
                return;

            MyMqttService.publishMessage(PushMessage.COMMAND.QUERY_STATUS,  (List<String>) null, null);
            sendEmptyMessageDelayed(0, 1000);
        }
    };
}
