package com.tanhd.rollingclass.fragments.pages;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tanhd.rollingclass.R;
import com.tanhd.rollingclass.fragments.resource.ResourceBaseFragment;
import com.tanhd.rollingclass.server.ScopeServer;
import com.tanhd.rollingclass.server.data.ExternalParam;
import com.tanhd.rollingclass.server.data.OptionData;
import com.tanhd.rollingclass.server.data.QuestionData;
import com.tanhd.rollingclass.server.data.UserData;
import com.tanhd.rollingclass.utils.AppUtils;
import com.tanhd.rollingclass.views.AnalysisDialog;
import com.tanhd.rollingclass.views.WebViewEx;

import java.util.ArrayList;
import java.util.List;

public class StudentResponderFragment extends Fragment {
    private static final String QUESTION_ID = "QUESTION_ID";
    private String mQuestionId;
    private TextView mTypeView;
    private TextView mNoView;
    private WebViewEx mStemView;

    private QuestionData mQuestionModel;
    private LinearLayout mOptionsLayout;
    private LinearLayout mAnswerLayout;
    private ExamListener mListener;

    public static StudentResponderFragment newInstance(String QuestionId, ExamListener listener) {
        StudentResponderFragment page = new StudentResponderFragment();
        Bundle args = new Bundle();
        args.putString(QUESTION_ID, QuestionId);
        page.setArguments(args);
        page.setListener(listener);
        return page;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View contentView = inflater.inflate(R.layout.student_responder_fragment, null);
        initParams();
        initViews(contentView);
        initData();
        return contentView;
    }

    private void initParams() {
        Bundle args = getArguments();
        mQuestionId = args.getString(QUESTION_ID);
    }

    private void initViews(View contentView) {
        mTypeView = contentView.findViewById(R.id.type);
        mNoView = contentView.findViewById(R.id.no);
        mStemView = contentView.findViewById(R.id.stem);
        mOptionsLayout = contentView.findViewById(R.id.options);
        mAnswerLayout = contentView.findViewById(R.id.answer_layout);
    }

    void initData(){
        new InitTask().execute();
    }

    public void setListener(ExamListener listener){
        mListener = listener;
    }

    private class InitTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected Void doInBackground(Void... voids) {

            List<QuestionData> questionList = ScopeServer.getInstance().QureyQuestionByID2(mQuestionId);
            if (questionList == null && questionList.size() == 0)
                return null;
            mQuestionModel = questionList.get(0);
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            if (mQuestionModel!=null && mQuestionModel.Context != null) {
                mTypeView.setVisibility(View.GONE);
                mNoView.setVisibility(View.GONE);
                String html = AppUtils.dealHtmlText(mQuestionModel.Context.Stem);
                mStemView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null);

                if (mQuestionModel.Context.QuestionCategoryId == 1 && mQuestionModel.Context.Options != null) {
                    mOptionsLayout.setVisibility(View.VISIBLE);
                    mOptionsLayout.removeAllViews();

                    try {
                        for (OptionData optionData : mQuestionModel.Context.Options) {
                            View optionRoundView = getLayoutInflater().inflate(R.layout.view_option, mOptionsLayout, false);
                            TextView optionTextView = optionRoundView.findViewById(R.id.option_textview);
                            String answer = AppUtils.OPTION_NO[optionData.OrderIndex - 1];
                            optionTextView.setText(answer);
                            optionTextView.setTag(answer);
                            optionTextView.setOnClickListener(onClickListener);
                            mAnswerLayout.addView(optionTextView);

                            View optionView = getLayoutInflater().inflate(R.layout.layout_question_option, mOptionsLayout, false);
                            mNoView = optionView.findViewById(R.id.no);
                            WebView textView = optionView.findViewById(R.id.option_text);

                            mNoView.setText(AppUtils.OPTION_NO[optionData.OrderIndex - 1] + ".");
                            html = AppUtils.dealHtmlText(optionData.OptionText);
                            textView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null);
                            mOptionsLayout.addView(optionView);
                        }
                    } catch (Exception e) {

                    }

                } else {
                    mOptionsLayout.setVisibility(View.GONE);
                }
            }
        }
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String answer = (String) v.getTag();
            if(mListener!=null){
                mListener.onFinished(answer);
            }
            DialogFragment dialog = (DialogFragment) getParentFragment();
            dialog.dismiss();
        }
    };

    public interface ExamListener {
        void onFinished(String answer);
    }

}
