package com.tanhd.rollingclass.views;

import android.content.Context;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.tanhd.rollingclass.R;
import com.tanhd.rollingclass.server.ScopeServer;
import com.tanhd.rollingclass.server.data.AnswerData;
import com.tanhd.rollingclass.server.data.OptionData;
import com.tanhd.rollingclass.server.data.QuestionModel;
import com.tanhd.rollingclass.utils.AppUtils;

import org.w3c.dom.Text;

public class QuestionView extends LinearLayout {
    public QuestionView(Context context) {
        super(context);
    }

    public QuestionView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public QuestionView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    public void setData(QuestionModel questionData) {
        setData(questionData, -1);
    }

    public void setData(QuestionModel questionData, int score) {
        WebView mStemView = findViewById(R.id.question_stem);
        if (questionData == null)
            return;

        String html = questionData.htmlText(score);
        mStemView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null);
    }
}
