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
import com.tanhd.rollingclass.db.KeyConstants;
import com.tanhd.rollingclass.server.data.AnswerData;
import com.tanhd.rollingclass.server.data.AnswerModel;
import com.tanhd.rollingclass.server.data.ExternalParam;
import com.tanhd.rollingclass.server.data.OptionData;
import com.tanhd.rollingclass.server.data.QuestionModel;
import com.tanhd.rollingclass.server.data.UserData;
import com.tanhd.rollingclass.utils.AppUtils;
import com.tanhd.rollingclass.utils.ToastUtil;
import com.tanhd.rollingclass.views.AnalysisDialog;
import com.tanhd.rollingclass.views.OnItemClickListener;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * 错题-页面
 */
public class AnswerDisplayFragment extends ResourceBaseFragment {
    private static final String PARAM_TYPE = "PARAM_TYPE";
    private List<AnswerModel> mQuestionList = new ArrayList<>();

    private RecyclerView recyclerView;
    private QuestionAdapter mAdapter;

    private Handler mHandler = new Handler();
    private int mType;

    public static AnswerDisplayFragment newInstance(int type) {
        AnswerDisplayFragment page = new AnswerDisplayFragment();
        Bundle args = new Bundle();
        args.putInt(PARAM_TYPE, type);
        page.setArguments(args);
        return page;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_question_selector, container, false);
        initParams();
        initViews(view);
        return view;
    }

    private void initParams() {
        Bundle args = getArguments();
        mType = args.getInt(PARAM_TYPE, 0);
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
                AnswerModel answerModel = mAdapter.getDataList().get(position);
                AnalysisDialog.newInstance(answerModel.context.Answer, answerModel.context.Analysis).show(getChildFragmentManager(), "AnalysisDialog");
            }
        });
    }

    public void setListData(final List resourceList) {
        mQuestionList = resourceList;
        if (mAdapter != null) {
            mAdapter.setDataList(resourceList);
        } else {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (mAdapter != null) {
                        mAdapter.setDataList(resourceList);
                        mAdapter.notifyDataSetChanged();
                    }
                }
            }, 1000);
        }
    }

    public void clearListData() {
        if (mAdapter != null) {
            recyclerView.smoothScrollToPosition(0);
            mQuestionList.clear();
            mAdapter.setDataList(mQuestionList);
        }
    }

    @Override
    List getDataList() {
        return mQuestionList;
    }

    /**
     * 适配器
     */
    private class QuestionAdapter extends BaseListAdapter<AnswerModel> {
        private final UserData userData;
        private OnItemClickListener analysisListener;

        public QuestionAdapter(Context context) {
            super(context);
            userData = ExternalParam.getInstance().getUserData();
        }

        @Override
        public int getLayoutId() {
            return R.layout.item_wrong_answer_display;
        }

        public void setAnalysisListener(OnItemClickListener analysisListener) {
            this.analysisListener = analysisListener;
        }

        @Override
        public void onBindItemHolder(BaseViewHolder holder, final int position) {
            final AnswerModel answerModel = mDataList.get(position);

            TextView typeView = holder.getView(R.id.type);
            TextView noView = holder.getView(R.id.no);
            WebView stemView = holder.getView(R.id.stem);
            View overView = holder.getView(R.id.over);
            TextView tv_analysis = holder.getView(R.id.tv_analysis);
            if (!userData.isTeacher() && mType == 0) {
                tv_analysis.setVisibility(View.GONE);
            } else {
                tv_analysis.setVisibility(View.VISIBLE);
            }
            TextView tv_my_answer = holder.getView(R.id.tv_my_answer);
            TextView answerResultTextview = holder.getView(R.id.answer_result_textview);
            tv_analysis.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (analysisListener != null) analysisListener.onItemClick(v, position);
                }
            });
            if (answerModel.context != null) {
                typeView.setText(String.format("[%s]", answerModel.context.QuestionCategoryName));
                noView.setText(String.format(getResources().getString(R.string.lbl_question_no), answerModel.context.OrderIndex));
                String html = AppUtils.dealHtmlText(answerModel.context.Stem);
                stemView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null);

                tv_my_answer.setText(getString(R.string.my_answer, answerModel.myAnswer));
                if (mType == KeyConstants.QuestionDisplayPage.WRONG_ANSWER) {
                    tv_my_answer.setVisibility(View.VISIBLE);
                    answerResultTextview.setVisibility(View.GONE);
                } else {
                    tv_my_answer.setVisibility(View.GONE);
                    answerResultTextview.setVisibility(View.VISIBLE);
                    if (answerModel.answer_status == KeyConstants.AnswerStatus.RIGHT) {
                        answerResultTextview.setText(R.string.answer_right);
                        answerResultTextview.setTextColor(getResources().getColor(R.color.xt_select_ok));
                    } else if (answerModel.answer_status == KeyConstants.AnswerStatus.WRONG) {
                        answerResultTextview.setText(R.string.answer_wrong);
                        answerResultTextview.setTextColor(getResources().getColor(R.color.xt_select_no));
                    } else {
                        answerResultTextview.setText(R.string.answer_unselect);
                        answerResultTextview.setTextColor(getResources().getColor(R.color.xt_select_no));
                    }
                }

                LinearLayout optionsLayout = holder.getView(R.id.options);
                if (answerModel.context.QuestionCategoryId == 1 && answerModel.context.Options != null) {
                    optionsLayout.setVisibility(View.VISIBLE);
                    optionsLayout.removeAllViews();

                    try {
                        for (OptionData optionData : answerModel.context.Options) {
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
