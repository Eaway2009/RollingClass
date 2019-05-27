package com.tanhd.rollingclass.fragments;

import android.annotation.SuppressLint;
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
import com.tanhd.library.mqtthttp.PushMessage;
import com.tanhd.rollingclass.R;
import com.tanhd.rollingclass.server.data.ExternalParam;

import java.io.Serializable;
import java.util.Map;

public class ClassPromptFragment extends DialogFragment {
    public static interface ClassPromptListener {
        void onEnter(String url);
    }
    private static class Args implements Serializable {
        public Map<String, String> map;
    }

    private Button mEnterButton;
    private Map<String, String> mMap;
    private ClassPromptListener mListener;

    public static ClassPromptFragment newInstance(Map<String, String> map, ClassPromptListener listener) {
        Args data = new Args();
        data.map = map;

        Bundle args = new Bundle();
        args.putSerializable("args", data);
        ClassPromptFragment fragment = new ClassPromptFragment();
        fragment.setArguments(args);
        fragment.setListener(listener);
        return fragment;
    }

    public void setListener(ClassPromptListener listener) {
        this.mListener = listener;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Args args = (Args) getArguments().get("args");
        mMap = args.map;
        View view = inflater.inflate(R.layout.fragment_class_prompt, container, false);
        TextView classView = view.findViewById(R.id.class_name);
        classView.setText(mMap.get("ClassName"));
        TextView subjectView = view.findViewById(R.id.subject);
        subjectView.setText(mMap.get("SubjectName"));
        TextView teacherView = view.findViewById(R.id.teacher_name);
        teacherView.setText(mMap.get("TeacherName"));
        TextView knowledgeView = view.findViewById(R.id.knowledage_name);
        knowledgeView.setText(mMap.get("KnowledgePointName"));
        TextView lessonSampleView = view.findViewById(R.id.lesson_sample);
        lessonSampleView.setText(mMap.get("LessonSampleName"));

        mEnterButton = view.findViewById(R.id.btn_enter);
        mEnterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ExternalParam.getInstance().getStatus() == 1) {
                    mListener.onEnter(mMap.get("UrlContent"));
                }
                mHandler.removeMessages(0);
                dismiss();
            }
        });

        mHandler.sendEmptyMessage(0);
        return view;
    }

    private Handler mHandler = new Handler() {
        int count = 10;
        @Override
        public void handleMessage(Message msg) {
            if (ExternalParam.getInstance().getStatus() != 1) {
                dismiss();
                return;
            }

            try {
                mEnterButton.setText(getResources().getString(R.string.into_class) + "(" + count + ")");
                count--;
                if (count == 0) {
                    if (isAdded())
                        mEnterButton.callOnClick();
                } else {
                    sendEmptyMessageDelayed(0, 1000);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

}
