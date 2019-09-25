package com.tanhd.rollingclass.views;

import android.annotation.SuppressLint;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.tanhd.rollingclass.R;
import com.tanhd.rollingclass.base.BaseDialogFragment;
import com.tanhd.rollingclass.utils.annotate.InjectView;

/**
 * 是、否 dialog
 * Created by YangShlai on 2019-09-24.
 */
@SuppressLint("ValidFragment")
public class YesNoDialog extends BaseDialogFragment implements View.OnClickListener {
    @InjectView(id = R.id.tv_content)
    private TextView tv_content;
    @InjectView(id = R.id.tv_left, onClick = true)
    private TextView tv_left;
    @InjectView(id = R.id.tv_right, onClick = true)
    private TextView tv_right;

    private String content, leftTxt, rightTxt;
    private View.OnClickListener leftListener, rightListener;

    public YesNoDialog(String content, String leftTxt, String rightTxt, View.OnClickListener leftListener, View.OnClickListener rightListener) {
        this.content = content;
        this.leftTxt = leftTxt;
        this.rightTxt = rightTxt;
        this.rightListener = rightListener;
        this.leftListener = leftListener;
    }

    @Override
    protected int getContentView() {
        return R.layout.dialog_yes_no;
    }

    @Override
    protected void initView(View view) {
        tv_content.setText(content);
        if (!TextUtils.isEmpty(leftTxt)){
            tv_left.setText(leftTxt);
        }
        if (!TextUtils.isEmpty(rightTxt)){
            tv_left.setText(rightTxt);
        }

    }

    @Override
    protected void initData() {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_left:
                if (leftListener != null) leftListener.onClick(v);
                dismissDialog();
                break;
            case R.id.tv_right:
                if (rightListener != null) rightListener.onClick(v);
                dismissDialog();
                break;

        }
    }
}
