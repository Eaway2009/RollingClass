package com.tanhd.rollingclass.fragments;

import android.annotation.SuppressLint;
import android.app.Application;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tanhd.rollingclass.R;
import com.tanhd.rollingclass.server.ScopeServer;
import com.tanhd.rollingclass.server.data.ExternalParam;
import com.tanhd.rollingclass.server.data.LessonSampleData;
import com.tanhd.rollingclass.server.data.OptionData;
import com.tanhd.rollingclass.server.data.QuestionModel;
import com.tanhd.rollingclass.utils.AppUtils;
import com.tanhd.rollingclass.views.LessonSampleSelectorView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class QuestionSelectorFragment extends Fragment {
    public static interface QuestionSelectListener {
        void onQuestionSelected(List<QuestionModel> questionList);
    }

    private ListView mListView;
    private QuestionAdapter mAdapter;
    private List<QuestionModel> mQuestionList = new ArrayList<>();
    private HashMap<String, QuestionModel> mSelectedMap = new HashMap<>();
    private QuestionSelectListener mListener;
    private LessonSampleSelectorView mLessonSampleView;

    public static QuestionSelectorFragment newInstance(QuestionSelectListener listener) {
        QuestionSelectorFragment fragment = new QuestionSelectorFragment();
        fragment.setListener(listener);
        return fragment;
    }

    public void setListener(QuestionSelectListener listener) {
        this.mListener = listener;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_question_selector, container, false);
        mListView = view.findViewById(R.id.list);
        mLessonSampleView = view.findViewById(R.id.lesson_sample);
        view.findViewById(R.id.commit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null && !mSelectedMap.isEmpty()) {
                    ArrayList<QuestionModel> list = new ArrayList<>(mSelectedMap.values());
                    mListener.onQuestionSelected(list);
                }

                if (getParentFragment() instanceof FrameDialog) {
                    DialogFragment dialog = (DialogFragment) getParentFragment();
                    dialog.dismiss();
                }
            }
        });
        init();
        return view;
    }

    private void init() {
        mAdapter = new QuestionAdapter();
        mListView.setAdapter(mAdapter);

        LessonSampleData lessonSampleData = ExternalParam.getInstance().getLessonSample();
        mLessonSampleView.setListener(new LessonSampleSelectorView.SelectorListener() {
            @Override
            public void onLessonSampleSelected(LessonSampleData lessonSampleData) {
                new LoadQuestionTask().execute(lessonSampleData.LessonSampleID);
            }
        }, lessonSampleData.LessonSampleID);

        new LoadQuestionTask().execute(lessonSampleData.LessonSampleID);
    }

    private class LoadQuestionTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... strings) {
            String lessonSampleID = strings[0];
            List<QuestionModel> list = ScopeServer.getInstance().QureyQuestionByLessonSampleID(lessonSampleID);
            if (list == null)
                return null;

            mQuestionList = list;
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            mAdapter.notifyDataSetChanged();
        }
    }

    private class QuestionAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mQuestionList.size();
        }

        @Override
        public Object getItem(int position) {
            return mQuestionList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final QuestionModel question = mQuestionList.get(position);
            View view = convertView;
            if (view == null) {
                view = getLayoutInflater().inflate(R.layout.item_question_selector, parent, false);
            }

            TextView typeView = view.findViewById(R.id.type);
            TextView noView = view.findViewById(R.id.no);
            WebView stemView = view.findViewById(R.id.stem);
            View overView = view.findViewById(R.id.over);

            if (mSelectedMap.containsKey(question.question_id)) {
                overView.setVisibility(View.VISIBLE);
            } else {
                overView.setVisibility(View.GONE);
            }

            typeView.setText(String.format("[%s]", question.context.QuestionCategoryName));
            noView.setText(String.format(getString(R.string.lbl_question_no), question.context.OrderIndex));
            String html = AppUtils.dealHtmlText(question.context.Stem);
            stemView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null);

            LinearLayout optionsLayout = view.findViewById(R.id.options);
            if (question.context.QuestionCategoryId == 1 && question.context.Options != null) {
                optionsLayout.setVisibility(View.VISIBLE);
                optionsLayout.removeAllViews();

                try {
                    for (OptionData optionData: question.context.Options) {
                        View optionView = getLayoutInflater().inflate(R.layout.layout_question_option, optionsLayout, false);
                        noView = optionView.findViewById(R.id.no);
                        WebView textView = optionView.findViewById(R.id.option_text);

                        noView.setText(AppUtils.OPTION_NO[optionData.OrderIndex-1] + ".");
                        html = AppUtils.dealHtmlText(optionData.OptionText);
                        textView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null);
                        optionsLayout.addView(optionView);
                    }
                } catch (Exception e) {

                }

            } else {
                optionsLayout.setVisibility(View.GONE);
            }

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    View overView = v.findViewById(R.id.over);
                    if (overView.getVisibility() == View.GONE) {
                        overView.setVisibility(View.VISIBLE);
//
//                        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) overView.getLayoutParams();
//                        params.width = v.getWidth();
//                        params.height = v.getHeight();
//                        overView.setLayoutParams(params);

                        mSelectedMap.put(question.question_id, question);
                    } else {
                        overView.setVisibility(View.GONE);
                        mSelectedMap.remove(question.question_id);
                    }
                }
            });
            return view;
        }
    }
}
