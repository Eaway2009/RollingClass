package com.tanhd.rollingclass.fragments.pages;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.tanhd.library.mqtthttp.MQTT;
import com.tanhd.rollingclass.base.MyMqttService;
import com.tanhd.library.mqtthttp.PushMessage;
import com.tanhd.library.smartpen.MyDot;
import com.tanhd.library.smartpen.SmartPenView;
import com.tanhd.rollingclass.R;
import com.tanhd.rollingclass.db.MSG_TYPE;
import com.tanhd.rollingclass.fragments.FrameDialog;
import com.tanhd.rollingclass.fragments.ShowAnswerCommentFragment;
import com.tanhd.rollingclass.server.ScopeServer;
import com.tanhd.rollingclass.server.data.AnswerData;
import com.tanhd.rollingclass.server.data.ClassData;
import com.tanhd.rollingclass.server.data.ExternalParam;
import com.tanhd.rollingclass.server.data.LessonSampleData;
import com.tanhd.rollingclass.server.data.OptionData;
import com.tanhd.rollingclass.server.data.QuestionModel;
import com.tanhd.rollingclass.server.data.StudentData;
import com.tanhd.rollingclass.server.data.UserData;
import com.tanhd.rollingclass.utils.AppUtils;
import com.tanhd.rollingclass.utils.ResultClass;
import com.tanhd.rollingclass.utils.ToastUtil;
import com.tanhd.rollingclass.views.ObjectiveAnswerView;
import com.tanhd.rollingclass.views.QuestionAnswerView;
import com.tanhd.rollingclass.views.ScoreView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class CommentAnswerPage extends Fragment {
    private QuestionModel mQuestionData;
    private AnswerData mAnswerData;
    private ObjectiveAnswerView mObjectiveView;
    private TextView mAnswerTextView;
    private String mScore = "0";
    private ArrayList<MyDot> mDotCache = new ArrayList<>();

    public static CommentAnswerPage newInstance(QuestionModel questionData, AnswerData answerData) {
        Bundle args = new Bundle();
        args.putSerializable("questionData", questionData);
        args.putSerializable("answerData", answerData);
        CommentAnswerPage page = new CommentAnswerPage();
        page.setArguments(args);
        return page;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mQuestionData = (QuestionModel) getArguments().get("questionData");
        mAnswerData = (AnswerData) getArguments().get("answerData");
        View view = inflater.inflate(R.layout.page_comment_answer, container, false);
        init(view);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mObjectiveView != null)
            mObjectiveView.active();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        MyMqttService.publishMessage(PushMessage.COMMAND.COMMENT_END, (List<String>) null, null);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mObjectiveView.onActivityResult(requestCode, resultCode, data);
    }

    private void init(View view) {
        mObjectiveView = view.findViewById(R.id.objective_view);
        mObjectiveView.setFragment(this);
        mObjectiveView.setSmartPenListener(drawPathListener);
        mAnswerTextView = view.findViewById(R.id.answer_text);
        ImageView imageView = view.findViewById(R.id.answer_image);
        View textLayout = view.findViewById(R.id.text_layout);
        ScoreView scoreView = view.findViewById(R.id.score_layout);
        QuestionAnswerView answerView = view.findViewById(R.id.answer_view);
        answerView.setData(mQuestionData, mAnswerData);

        if (mAnswerData != null) {
            if (TextUtils.isEmpty(mAnswerData.AnswerUrl)) {
                textLayout.setVisibility(View.VISIBLE);
                imageView.setVisibility(View.GONE);
                mAnswerTextView.setText(mAnswerData.AnswerText);
            } else {
                textLayout.setVisibility(View.GONE);
                imageView.setVisibility(View.VISIBLE);
                Glide.with(getContext()).load(ScopeServer.getInstance().getResourceUrl() + mAnswerData.AnswerUrl).into(imageView);
            }
        }

        scoreView.setListener(new ScoreView.ScoreListener() {
            @Override
            public void onClick(String score) {
                mScore = score;
            }
        });

        view.findViewById(R.id.btn_commit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String markurl = null;
                String mark = null;

                int mode = mObjectiveView.getMode();
                if (mode == 0) {
                    List data = mObjectiveView.getSmartPenData();
                    if (data != null) {
                        Size size = mObjectiveView.getSmartPenSize();
                        Bitmap bitmap = SmartPenView.getBitmap(data, size);
                        if (bitmap != null) {
                            String fileName = UUID.randomUUID().toString().replace("-", "").toLowerCase();
                            fileName = AppUtils.saveToFile(getContext(), bitmap, fileName);
                            bitmap.recycle();
                            markurl = fileName;
                        }
                    }
                } else if (mode == 1) {
                    mark = mObjectiveView.getEditText();
                } else {
                    markurl = mObjectiveView.getImagePath();
                }

                new UploadMarkTask().execute(mScore, mark, markurl);
            }
        });

        HashMap<String, String> params = new HashMap<>();
        params.put("Question", mQuestionData.toJSON().toString());
        params.put("Answer", mAnswerData.toJSON().toString());
        MyMqttService.publishMessage(PushMessage.COMMAND.COMMENT_START, (List<String>) null, params);
        mHandler.sendEmptyMessageDelayed(0, 1000);
    }

    private void notifyCommentMessage() {
        UserData userData = ExternalParam.getInstance().getUserData();
        HashMap<String, String> params = new HashMap<>();
        params.put("Question", mQuestionData.toJSON().toString());
        params.put("Answer", mAnswerData.toJSON().toString());
        params.put("type", String.valueOf(MSG_TYPE.COMMENT_RESULT.ordinal()));

        List<String> to = new ArrayList<>();
        if (userData.isTeacher()) {
            //如果正在上课，则将点评结果发送全部，否则，只送个人
            if (ExternalParam.getInstance().getStatus() == 2) {
                ClassData classData = ExternalParam.getInstance().getClassData();
                to.add(classData.ClassID);
            } else {
                to.add(mAnswerData.AnswerUserID);
            }
        } else {
            StudentData studentData = (StudentData) userData.getUserData();
            to.add(studentData.ClassID);
        }

        MyMqttService.publishMessage(PushMessage.COMMAND.MESSAGE, to, params);
    }

    private void showFragment(Fragment fragment) {
        String fragmentTag = "CommentAnswer";
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.replace(R.id.framelayout, fragment, fragmentTag);
        transaction.addToBackStack(fragmentTag);
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        transaction.commit();
    }

    private class UploadMarkTask extends AsyncTask<String, Void, Integer> {

        @Override
        protected void onPostExecute(Integer integer) {
            if (integer == 0) {
                notifyCommentMessage();
                if (getContext() != null)
                    ToastUtil.show( R.string.toast_comment_ok);
                if (getParentFragment() instanceof FrameDialog) {
                    FrameDialog dialog = (FrameDialog) getParentFragment();
                    dialog.dismiss();
                } else {
                    showFragment(ShowAnswerCommentFragment.newInstance(mQuestionData, mAnswerData));
                }
            }
            else {
                if (getContext() != null)
                    Toast.makeText(getContext().getApplicationContext(), "ErrorCode:" + integer, Toast.LENGTH_LONG).show();
            }
        }

        @Override
        protected Integer doInBackground(String... strings) {
            String score = strings[0];
            String mark = strings[1];
            String markUrl = strings[2];

            if (markUrl != null) {
                markUrl = ScopeServer.getInstance().uploadResourceFile(markUrl, 105);
                if (markUrl == null)
                    return -2;
                mark = markUrl;
                markUrl = null;
            }

            if (mark == null)
                mark = "undefine";

            if (markUrl == null)
                markUrl = "undefine";

            mAnswerData.Score = Integer.parseInt(score);
            mAnswerData.Remark = mark;

            UserData userData = ExternalParam.getInstance().getUserData();
            if (userData.isTeacher()) {
                int ret = ScopeServer.getInstance().UpdataAnswerv2ByTeacher(mAnswerData.AnswerID, score, mark, markUrl);
                return ret;
            } else {
                int ret = ScopeServer.getInstance().UpdataAnswerv2ByStudent(mAnswerData.AnswerID, score, mark, markUrl);
                return ret;
            }

        }
    }

    private SmartPenView.DrawPathListener drawPathListener = new SmartPenView.DrawPathListener() {
        @Override
        public void onReceiveDot(MyDot dot) {
            mDotCache.add(dot);
        }

        @Override
        public void onReceivePenHandwritingColor(int color) {
            HashMap<String, String> params = new HashMap<>();
            params.put("color", String.valueOf(color));
            MyMqttService.publishMessage(PushMessage.COMMAND.SMART_PEN_COLOR,  (List<String>) null, params);
        }
    };

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            synchronized (mDotCache) {
                JSONArray array = new JSONArray();
                while (mDotCache.size() > 0) {
                    MyDot dot = mDotCache.remove(0);
                    JSONObject json = new JSONObject();
                    try {
                        json.put("x", dot.x);
                        json.put("fx", dot.fx);
                        json.put("y", dot.y);
                        json.put("fy", dot.fy);
                        json.put("type", dot.type);
                        array.put(json);
                    } catch (JSONException e) {
                    }
                }

                if (array.length() > 0) {
                    HashMap<String, String> params = new HashMap<>();
                    params.put("data", array.toString());
                    MyMqttService.publishMessage(PushMessage.COMMAND.SMART_PEN_DOT, (List<String>) null, params);
                }

                sendEmptyMessageDelayed(0, 1000);
            }
        }
    };
}
