package com.tanhd.rollingclass.fragments.pages;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.tanhd.rollingclass.R;
import com.tanhd.rollingclass.fragments.FrameDialog;
import com.tanhd.rollingclass.server.ScopeServer;
import com.tanhd.rollingclass.server.data.AnswerData;
import com.tanhd.rollingclass.server.data.ExternalParam;
import com.tanhd.rollingclass.server.data.OptionData;
import com.tanhd.rollingclass.server.data.QuestionModel;
import com.tanhd.rollingclass.server.data.UserData;
import com.tanhd.rollingclass.utils.AppUtils;
import com.tanhd.rollingclass.utils.ToastUtil;
import com.tanhd.rollingclass.views.QuestionAnswerView;
import com.tanhd.rollingclass.views.ScoreView;

public class MarkAnswerPage extends Fragment {
    private QuestionModel mQuestionData;
    private AnswerData mAnswerData;

    public static MarkAnswerPage newInstance(QuestionModel questionData, AnswerData answerData) {
        Bundle args = new Bundle();
        args.putSerializable("questionData", questionData);
        args.putSerializable("answerData", answerData);
        MarkAnswerPage page = new MarkAnswerPage();
        page.setArguments(args);
        return page;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mQuestionData = (QuestionModel) getArguments().get("questionData");
        mAnswerData = (AnswerData) getArguments().get("answerData");
        View view = inflater.inflate(R.layout.page_mark_answer, container, false);
        init(view);
        return view;
    }

    private void init(View view) {
        final QuestionAnswerView answerView = view.findViewById(R.id.answer_view);
        answerView.setData(mQuestionData, mAnswerData);

        view.findViewById(R.id.mark_layout).setVisibility(View.VISIBLE);
        view.findViewById(R.id.mark).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FrameDialog.fullShow(getChildFragmentManager(), CommentAnswerPage.newInstance(mQuestionData, mAnswerData));
            }
        });

        view.findViewById(R.id.student_mark).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                answerView.requestStudentToMark(getChildFragmentManager());
            }
        });

        ScoreView scoreView = view.findViewById(R.id.score_layout);
        if (mAnswerData != null) {
            scoreView.setScore(mAnswerData.Score + "");
            scoreView.setEnabled(mAnswerData.Modify != 1);
            scoreView.setListener(new ScoreView.ScoreListener() {
                @Override
                public void onClick(String score) {
                    mAnswerData.Score = Integer.valueOf(score);
                    new UploadMarkTask().execute(score);
                }
            });
        } else {
            scoreView.setEnabled(false);
        }
    }

    private class UploadMarkTask extends AsyncTask<String, Void, Integer> {

        @Override
        protected void onPostExecute(Integer integer) {
            if (integer == 0)
                ToastUtil.show(R.string.toast_mark_ok);
            else
                ToastUtil.show(R.string.toast_mark_fail);
        }

        @Override
        protected Integer doInBackground(String... strings) {
            String score = strings[0];

            UserData userData = ExternalParam.getInstance().getUserData();
            if (userData.isTeacher()) {
                int ret = ScopeServer.getInstance().UpdataAnswerv2ByTeacher(mAnswerData.AnswerID, score, mAnswerData.Remark, mAnswerData.Remark);
                return ret;
            }

            return -1;
        }
    }
}
