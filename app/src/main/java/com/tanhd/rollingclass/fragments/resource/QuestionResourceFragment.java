package com.tanhd.rollingclass.fragments.resource;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tanhd.rollingclass.R;
import com.tanhd.rollingclass.db.KeyConstants;
import com.tanhd.rollingclass.fragments.ExamFragment;
import com.tanhd.rollingclass.fragments.QuestionSelectorFragment;
import com.tanhd.rollingclass.server.data.ExternalParam;
import com.tanhd.rollingclass.server.data.LessonSampleData;
import com.tanhd.rollingclass.server.data.OptionData;
import com.tanhd.rollingclass.server.data.QuestionModel;
import com.tanhd.rollingclass.server.data.ResourceModel;
import com.tanhd.rollingclass.utils.AppUtils;

import java.util.ArrayList;
import java.util.List;

public class QuestionResourceFragment extends ResourceBaseFragment {
    private List<QuestionModel> mQuestionList = new ArrayList<>();

    private ListView mListView;
    private QuestionAdapter mAdapter;
    private Callback mListener;

    public static QuestionResourceFragment newInstance() {
        QuestionResourceFragment page = new QuestionResourceFragment();
        return page;
    }

    public static QuestionResourceFragment newInstance(ResourceBaseFragment.Callback callback) {
        QuestionResourceFragment page = new QuestionResourceFragment();
        page.setListener(callback);
        return page;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_question_selector, container, false);
        initViews(view);
        return view;
    }

    private void initViews(View view) {
        view.findViewById(R.id.question_list_title).setVisibility(View.GONE);
        view.findViewById(R.id.lesson_sample).setVisibility(View.GONE);
        view.findViewById(R.id.commit).setVisibility(View.GONE);
        mListView = view.findViewById(R.id.list);
        mAdapter = new QuestionAdapter();
        mListView.setAdapter(mAdapter);
    }

    public void setListener(ResourceBaseFragment.Callback callback) {
        mListener = callback;
    }

    public void setListData(List resourceList) {
        if (resourceList != null && !resourceList.isEmpty() && mAdapter != null) {
            mQuestionList = resourceList;
            mAdapter.notifyDataSetChanged();
        }
    }

    public void clearListData() {
        if (mAdapter != null) {
            mListView.smoothScrollToPosition(0);
            mQuestionList.clear();
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

        public void checkItem(int position) {
            QuestionModel checkItem = mQuestionList.get(position);
            checkItem.isChecked = true;
            notifyDataSetChanged();
        }

        private void clearCheck() {
            for (QuestionModel model : mQuestionList) {
                model.isChecked = false;
            }
            notifyDataSetChanged();
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            final QuestionModel question = mQuestionList.get(position);
            View view = convertView;
            if (view == null) {
                view = getLayoutInflater().inflate(R.layout.item_question_resource, parent, false);
            }

            TextView typeView = view.findViewById(R.id.type);
            TextView noView = view.findViewById(R.id.no);
            WebView stemView = view.findViewById(R.id.stem);
            View overView = view.findViewById(R.id.over);
            CheckBox itemCheckBox = view.findViewById(R.id.check_item_cb);
            if (mListener != null) {
                itemCheckBox.setVisibility(View.VISIBLE);
            } else {
                itemCheckBox.setVisibility(View.GONE);
            }

            if (question.context != null) {
                typeView.setText(String.format("[%s]", question.context.QuestionCategoryName));
                noView.setText(String.format("第%d题:", question.context.OrderIndex));
                String html = AppUtils.dealHtmlText(question.context.Stem);
                stemView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null);

                LinearLayout optionsLayout = view.findViewById(R.id.options);
                if (question.context.QuestionCategoryId == 1 && question.context.Options != null) {
                    optionsLayout.setVisibility(View.VISIBLE);
                    optionsLayout.removeAllViews();

                    try {
                        for (OptionData optionData : question.context.Options) {
                            View optionView = getLayoutInflater().inflate(R.layout.layout_question_option, optionsLayout, false);
                            noView = optionView.findViewById(R.id.no);
                            WebView textView = optionView.findViewById(R.id.option_text);

                            noView.setText(AppUtils.OPTION_NO[optionData.OrderIndex - 1] + ".");
                            html = AppUtils.dealHtmlText(optionData.OptionText);
                            textView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null);
                            optionsLayout.addView(optionView);
                        }
                    } catch (Exception e) {

                    }

                } else {
                    optionsLayout.setVisibility(View.GONE);
                }
            }
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    View overView = v.findViewById(R.id.over);
                    if (overView.getVisibility() == View.GONE) {
                        overView.setVisibility(View.VISIBLE);

                        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) overView.getLayoutParams();
                        params.width = v.getWidth();
                        params.height = v.getHeight();
                        overView.setLayoutParams(params);

                    } else {
                        overView.setVisibility(View.GONE);
                    }

                    if (mListener != null) {
                        if (overView.getVisibility() == View.VISIBLE) {
                            checkItem(position);
                            mListener.itemChecked(null, question);
                        } else {
                            clearCheck();
                            mListener.itemChecked(null, null);
                        }
                    }
                }
            });
            itemCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (mListener != null) {
                        if (isChecked) {
                            checkItem(position);
                        }
                        mListener.itemChecked(null, question);
                    }
                }
            });
            return view;
        }
    }
}
