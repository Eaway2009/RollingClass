package com.tanhd.rollingclass.views;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.tanhd.rollingclass.R;
import com.tanhd.rollingclass.base.BaseDialogFragment;
import com.tanhd.rollingclass.utils.AppUtils;
import com.tanhd.rollingclass.utils.annotate.InjectView;

/**
 * 答案解析
 * Created by YangShlai on 2019-09-27.
 */
public class AnalysisDialog extends BaseDialogFragment {
    @InjectView(id = R.id.tv_answer)
    private TextView tv_answer;
    @InjectView(id = R.id.tv_ok)
    private TextView tv_ok;
    @InjectView(id = R.id.webview)
    private WebViewEx webview;
    private String answer,analysisHtml;


    public static AnalysisDialog newInstance(String answer,String analysisHtml) {
        Bundle args = new Bundle();
        args.putString("answer",answer);
        args.putString("analysisHtml",analysisHtml);
        AnalysisDialog fragment = new AnalysisDialog();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected int getContentView() {
        return R.layout.dialog_analysis;
    }

    @Override
    protected void initView(View view) {
        tv_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

    }

    @Override
    protected void initData() {
        if (getArguments() != null){
            answer = getArguments().getString("answer");
            analysisHtml = getArguments().getString("analysisHtml");
        }
        tv_answer.setText(answer);
        String html = AppUtils.dealHtmlText(analysisHtml);
        webview.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null);
    }

    @Override
    protected double getWidthHold() {
        return 0.51;
    }
}
