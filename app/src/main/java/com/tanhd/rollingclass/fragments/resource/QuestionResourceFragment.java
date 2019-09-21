package com.tanhd.rollingclass.fragments.resource;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.tanhd.rollingclass.R;
import com.tanhd.rollingclass.server.data.OptionData;
import com.tanhd.rollingclass.server.data.QuestionModel;
import com.tanhd.rollingclass.utils.AppUtils;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class QuestionResourceFragment extends ResourceBaseFragment {
    private List<QuestionModel> mQuestionList = new ArrayList<>();
    private List<QuestionModel> mCheckedQuestionList = new ArrayList<>();

    private ListView mListView;
    private QuestionAdapter mAdapter;
    private ListCallback mListListener;
    private Callback mListener;

    private Handler mHandler = new Handler();

    public static QuestionResourceFragment newInstance() {
        QuestionResourceFragment page = new QuestionResourceFragment();
        return page;
    }

    public static QuestionResourceFragment newInstance(ResourceBaseFragment.Callback callback) {
        QuestionResourceFragment page = new QuestionResourceFragment();
        page.setListener(callback);
        return page;
    }

    public static QuestionResourceFragment newInstance(ResourceBaseFragment.ListCallback callback) {
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

    public void setListener(ResourceBaseFragment.ListCallback callback) {
        mListListener = callback;
    }

    public void setListener(ResourceBaseFragment.Callback callback) {
        mListener = callback;
    }

    public void setListData(List resourceList) {
        mQuestionList = resourceList;
        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }else{
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (mAdapter != null) {
                        mAdapter.notifyDataSetChanged();
                    }
                }
            },500);
        }
    }

    public void clearListData() {
        if (mAdapter != null) {
            mListView.smoothScrollToPosition(0);
            mQuestionList.clear();
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    List getDataList() {
        return mQuestionList;
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
            if (mListener != null || mListListener != null) {
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
            itemCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (mListListener != null) {
                        if (isChecked) {
                            mCheckedQuestionList.add(question);
                        } else {
                            if (mCheckedQuestionList.contains(question)) {
                                mCheckedQuestionList.remove(question);
                            }
                        }
                        mListListener.onListChecked(null, mCheckedQuestionList);
                    }
                    if (mListener != null) {
                        mListener.itemChecked(null, question);
                    }
                }
            });
            return view;
        }
    }

    //这里面的resource就是fromhtml函数的第一个参数里面的含有的url
    Html.ImageGetter imgGetter = new Html.ImageGetter() {
        public Drawable getDrawable(String source) {
            Log.i("RG", "source---?>>>" + source);
            Drawable drawable = null;
            URL url;
            try {
                url = new URL(source);
                Log.i("RG", "url---?>>>" + url);
                drawable = Drawable.createFromStream(url.openStream(), ""); // 获取网路图片
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
            drawable.setBounds(0, 0, drawable.getIntrinsicWidth(),
                    drawable.getIntrinsicHeight());
            Log.i("RG", "url---?>>>" + url);
            return drawable;
        }
    };
}
