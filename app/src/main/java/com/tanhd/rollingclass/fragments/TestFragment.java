package com.tanhd.rollingclass.fragments;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import com.tanhd.rollingclass.R;
import com.tanhd.rollingclass.server.ScopeServer;
import com.tanhd.rollingclass.server.data.OptionData;
import com.tanhd.rollingclass.server.data.QuestionModel;
import com.tanhd.rollingclass.utils.AppUtils;

import java.util.List;

public class TestFragment extends Fragment {
    private WebView mWebView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_test, container, false);
        mWebView = view.findViewById(R.id.webview);
        new LoadTask().execute();
        return view;
    }

    private class LoadTask extends AsyncTask<Void, Void, QuestionModel> {

        @Override
        protected QuestionModel doInBackground(Void... voids) {
            List<QuestionModel> list = ScopeServer.getInstance().QureyQuestionByID("5c86680ae6cf005532c18ba0");
            if (list == null || list.isEmpty())
                return null;

            return list.get(0);
        }

        @Override
        protected void onPostExecute(QuestionModel questionData) {
            if (questionData == null)
                return;

            String html = questionData.htmlText();
            mWebView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null);
        }
    }
}
