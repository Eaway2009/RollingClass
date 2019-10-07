package com.tanhd.rollingclass.fragments.resource;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
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
import com.tanhd.rollingclass.base.BaseListAdapter;
import com.tanhd.rollingclass.base.BaseViewHolder;
import com.tanhd.rollingclass.server.data.OptionData;
import com.tanhd.rollingclass.server.data.QuestionModel;
import com.tanhd.rollingclass.utils.AppUtils;
import com.tanhd.rollingclass.views.AnalysisDialog;
import com.tanhd.rollingclass.views.OnItemClickListener;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * 习题-页面
 */
public class QuestionResourceFragment extends ResourceBaseFragment {
    private List<QuestionModel> mQuestionList = new ArrayList<>();
    private List<QuestionModel> mCheckedQuestionList = new ArrayList<>();

    private RecyclerView recyclerView;
    private QuestionAdapter mAdapter;
    private ListCallback mListListener;
    private Callback mListener;

    private Handler mHandler = new Handler();

    public static QuestionResourceFragment newInstance() {
        QuestionResourceFragment page = new QuestionResourceFragment();
        return page;
    }

    public static QuestionResourceFragment newInstance(List<QuestionModel> questionList) {
        QuestionResourceFragment page = new QuestionResourceFragment();
        page.setListData(questionList);
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
        recyclerView = view.findViewById(R.id.list);
        mAdapter = new QuestionAdapter(getActivity());
        recyclerView.setAdapter(mAdapter);

        //解析
        mAdapter.setAnalysisListener(new OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                QuestionModel questionModel = mAdapter.getDataList().get(position);
                AnalysisDialog.newInstance(questionModel.context.Answer,questionModel.context.Analysis).show(getChildFragmentManager(),"AnalysisDialog");
            }
        });
    }

    public void setListener(ResourceBaseFragment.ListCallback callback) {
        mListListener = callback;
    }

    public void setListener(ResourceBaseFragment.Callback callback) {
        mListener = callback;
    }

    public void setListData(final List resourceList) {
        mQuestionList = resourceList;
        if (mAdapter != null) {
            mAdapter.setDataList(resourceList);
        }else{
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (mAdapter != null) {
                        mAdapter.setDataList(resourceList);
                        mAdapter.notifyDataSetChanged();
                    }
                }
            },1000);
        }
    }

    public void clearListData() {
        if (mAdapter != null) {
            recyclerView.smoothScrollToPosition(0);
            mQuestionList.clear();
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    List getDataList() {
        return mQuestionList;
    }

    /**
     * 适配器
     */
    private class QuestionAdapter extends BaseListAdapter<QuestionModel> {
        private OnItemClickListener analysisListener;

        public QuestionAdapter(Context context) {
            super(context);
        }

        @Override
        public int getLayoutId() {
            return R.layout.item_question_resource;
        }

        public void setAnalysisListener(OnItemClickListener analysisListener) {
            this.analysisListener = analysisListener;
        }

        @Override
        public void onBindItemHolder(BaseViewHolder holder, final int position) {
            final QuestionModel question = mDataList.get(position);

            TextView typeView = holder.getView(R.id.type);
            TextView noView = holder.getView(R.id.no);
            WebView stemView = holder.getView(R.id.stem);
            View overView = holder.getView(R.id.over);
            TextView tv_analysis = holder.getView(R.id.tv_analysis);
            tv_analysis.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (analysisListener != null) analysisListener.onItemClick(v,position);
                }
            });

            CheckBox itemCheckBox = holder.getView(R.id.check_item_cb);
            if (mListener != null || mListListener != null) {
                itemCheckBox.setVisibility(View.VISIBLE);
            } else {
                itemCheckBox.setVisibility(View.GONE);
            }

            if (question.context != null) {
                typeView.setText(String.format("[%s]", question.context.QuestionCategoryName));
                noView.setText(String.format(getResources().getString(R.string.lbl_question_no), question.context.OrderIndex));
                String html = AppUtils.dealHtmlText(question.context.Stem);
                stemView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null);

                LinearLayout optionsLayout = holder.getView(R.id.options);
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
