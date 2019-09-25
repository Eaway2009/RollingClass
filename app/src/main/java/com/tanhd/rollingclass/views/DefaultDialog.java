package com.tanhd.rollingclass.views;

import android.annotation.SuppressLint;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.tanhd.rollingclass.R;
import com.tanhd.rollingclass.base.BaseDialogFragment;
import com.tanhd.rollingclass.utils.annotate.InjectView;

/**
 * Created by YangShlai on 2019-09-24.
 */
@SuppressLint("ValidFragment")
public class DefaultDialog extends BaseDialogFragment implements View.OnClickListener {
    @InjectView(id = R.id.tv_title)
    private TextView tv_title;
    @InjectView(id = R.id.tv_content)
    private TextView tv_content;
    @InjectView(id = R.id.tv_cancel, onClick = true)
    private TextView tv_cancel;
    @InjectView(id = R.id.tv_ok, onClick = true)
    private TextView tv_ok;
    private View.OnClickListener leftListener, rightListener;
    private String title, content,leftText,rightText;

    public DefaultDialog(String title, String content,String leftText,String rightText,View.OnClickListener leftListener, View.OnClickListener rightListener) {
        this.title = title;
        this.content = content;
        this.leftListener = leftListener;
        this.rightListener = rightListener;
    }

    @Override
    protected int getContentView() {
        return R.layout.dialog_default;
    }

    @Override
    protected void initView(View view) {

    }

    @Override
    protected void initData() {
        if (!TextUtils.isEmpty(title)){
            tv_title.setText(title);
        }
        tv_content.setText(content);
        if (!TextUtils.isEmpty(leftText)){
            tv_cancel.setText(leftText);
        }
        if (!TextUtils.isEmpty(rightText)){
            tv_ok.setText(rightText);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_cancel:
                if (leftListener != null) leftListener.onClick(v);
                dismissDialog();
                break;
            case R.id.tv_ok:
                if (rightListener != null) rightListener.onClick(v);
                dismissDialog();
                break;

        }
    }
}
