package com.tanhd.rollingclass.utils;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.widget.Toast;

import com.tanhd.rollingclass.db.MSG_TYPE;
import com.tanhd.rollingclass.fragments.FrameDialog;
import com.tanhd.rollingclass.fragments.ShowAnswerCommentFragment;
import com.tanhd.rollingclass.fragments.pages.CommentAnswerPage;
import com.tanhd.rollingclass.server.ScopeServer;
import com.tanhd.rollingclass.server.data.AnswerData;
import com.tanhd.rollingclass.server.data.QuestionModel;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class GlobalWork {
    private final Context mContext;
    private final FragmentManager fragmentManager;

    public GlobalWork(Context context, FragmentManager manager) {
        mContext = context;
        fragmentManager = manager;
    }

    public void dealCommentResult(String question, String answer) {
        QuestionModel questionData = new QuestionModel();
        questionData.parse(questionData, question);
        if (questionData.question_id == null) {
            Toast.makeText(mContext, "读取数据失败!", Toast.LENGTH_LONG).show();
            return;
        }

        AnswerData answerData = new AnswerData();
        answerData.parse(answerData, answer);
        if (answerData.AnswerUserID == null) {
            Toast.makeText(mContext, "读取数据失败!", Toast.LENGTH_LONG).show();
            return;
        }

        FrameDialog.fullShow(fragmentManager, ShowAnswerCommentFragment.newInstance(questionData, answerData));
    }

    public void dealCommentRequest(String question, String answer) {
        QuestionModel questionData = new QuestionModel();
        questionData.parse(questionData, question);
        if (questionData.question_id == null) {
            Toast.makeText(mContext, "读取数据失败!", Toast.LENGTH_LONG).show();
            return;
        }

        AnswerData answerData = new AnswerData();
        answerData.parse(answerData, answer);
        if (answerData.AnswerUserID == null) {
            Toast.makeText(mContext, "读取数据失败!", Toast.LENGTH_LONG).show();
            return;
        }

        FrameDialog.fullShow(fragmentManager, CommentAnswerPage.newInstance(questionData, answerData));
    }

}
