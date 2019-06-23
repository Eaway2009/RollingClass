package com.tanhd.rollingclass.views;

import android.content.Context;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.tanhd.library.mqtthttp.MQTT;
import com.tanhd.library.mqtthttp.PushMessage;
import com.tanhd.rollingclass.R;
import com.tanhd.rollingclass.db.MSG_TYPE;
import com.tanhd.rollingclass.fragments.FrameDialog;
import com.tanhd.rollingclass.fragments.StudentSelectorFragment;
import com.tanhd.rollingclass.server.ScopeServer;
import com.tanhd.rollingclass.server.data.AnswerData;
import com.tanhd.rollingclass.server.data.ExternalParam;
import com.tanhd.rollingclass.server.data.OptionData;
import com.tanhd.rollingclass.server.data.QuestionData;
import com.tanhd.rollingclass.server.data.QuestionSetData;
import com.tanhd.rollingclass.server.data.StudentData;
import com.tanhd.rollingclass.server.data.UserData;
import com.tanhd.rollingclass.utils.AppUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class QuestionAnswerView extends ScrollView {
    private AnswerData mAnswerData;
    private QuestionData mQuestionData;

    public QuestionAnswerView(Context context) {
        super(context);
    }

    public QuestionAnswerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public QuestionAnswerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setData(QuestionData questionData, AnswerData answerData) {
        mAnswerData = answerData;
        mQuestionData = questionData;
        QuestionView questionView = findViewById(R.id.question_title);
        ImageView imageView = findViewById(R.id.answer_image);
        TextView answerTextView = findViewById(R.id.answer_text);
        View textLayout = findViewById(R.id.text_layout);
        if (answerData == null)
            questionView.setData(mQuestionData);
        else
            questionView.setData(mQuestionData, answerData.Score);

        if (answerData != null) {
            if (TextUtils.isEmpty(answerData.AnswerUrl)) {
                imageView.setVisibility(View.GONE);
                textLayout.setVisibility(View.VISIBLE);

                answerTextView.setText(answerData.AnswerText);
            } else {
                imageView.setVisibility(View.VISIBLE);
                textLayout.setVisibility(View.GONE);

                Glide.with(getContext()).load(ScopeServer.getInstance().getResourceUrl() + answerData.AnswerUrl).into(imageView);
            }
        } else {
            imageView.setVisibility(View.GONE);
            textLayout.setVisibility(View.VISIBLE);

            answerTextView.setText("未答");
        }

    }

    public void requestStudentToMark(FragmentManager manager) {
        if (mAnswerData == null) {
            Toast.makeText(getContext().getApplicationContext(), "该题未作答或查找回答失败, 无法发起点评!", Toast.LENGTH_LONG).show();
            return;
        }

        FrameDialog.show(manager, StudentSelectorFragment.newInstance(true, null, new StudentSelectorFragment.StudentSelectListener() {
            @Override
            public void onStudentSelected(ArrayList<StudentData> list) {
                StudentData studentData = list.get(0);
                HashMap<String, String> params = new HashMap<>();
                params.put("Question", mQuestionData.toJSON().toString());
                params.put("Answer", mAnswerData.toJSON().toString());
                params.put("type", MSG_TYPE.COMMENT_REQUEST.ordinal() + "");

                MQTT.publishMessage(PushMessage.COMMAND.MESSAGE, studentData.StudentID, params);
                Toast.makeText(getContext().getApplicationContext(), "发起点评成功！ 指派点评人：" + studentData.Username, Toast.LENGTH_LONG).show();
            }
        }));
    }
}
