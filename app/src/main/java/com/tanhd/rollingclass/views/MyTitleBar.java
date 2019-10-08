package com.tanhd.rollingclass.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tanhd.rollingclass.R;

/**
 * 通用头部顶栏 ---暂未使用
 * Created by YangShlai on 2019-09-30.
 */
@Deprecated
public class MyTitleBar extends RelativeLayout implements View.OnClickListener {
    private TextView tv_ymd,tv_week,tv_name;
    private View ll_user;
    private ImageView iv_head,iv_home,iv_message,iv_setting,iv_pown,iv_en,iv_cn;

    public MyTitleBar(Context context) {
        super(context);
        init(context, null);
    }

    public MyTitleBar(final Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.view_mytitlebar, this, true);

        tv_ymd = findViewById(R.id.tv_ymd);
        tv_week = findViewById(R.id.tv_week);
        tv_name = findViewById(R.id.tv_name);
        ll_user = findViewById(R.id.ll_user);
        iv_head = findViewById(R.id.iv_head);
        iv_message = findViewById(R.id.iv_message);
        iv_setting = findViewById(R.id.iv_setting);
        iv_pown = findViewById(R.id.iv_pown);
        iv_en = findViewById(R.id.iv_cn);

        ll_user.setOnClickListener(this);
        iv_home.setOnClickListener(this);
        iv_message.setOnClickListener(this);
        iv_setting.setOnClickListener(this);
        iv_pown.setOnClickListener(this);
        iv_en.setOnClickListener(this);
        iv_cn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.ll_user: //用户

                break;


        }
    }
}
