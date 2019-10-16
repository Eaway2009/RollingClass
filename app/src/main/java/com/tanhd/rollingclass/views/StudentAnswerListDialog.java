package com.tanhd.rollingclass.views;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.tanhd.rollingclass.R;
import com.tanhd.rollingclass.base.BaseDialogFragment;
import com.tanhd.rollingclass.base.BaseListAdapter;
import com.tanhd.rollingclass.base.BaseViewHolder;
import com.tanhd.rollingclass.server.data.AnswerData;
import com.tanhd.rollingclass.server.data.QuestionInfo;
import com.tanhd.rollingclass.utils.annotate.InjectView;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

/**
 * 查看作答详情
 * Created by YangShlai on 2019-10-16.
 */
public class StudentAnswerListDialog extends BaseDialogFragment implements View.OnClickListener {
    @InjectView(id = R.id.tv_ok, onClick = true)
    private TextView tv_ok;
    @InjectView(id = R.id.tv_err, onClick = true)
    private TextView tv_err;
    @InjectView(id = R.id.tv_null, onClick = true)
    private TextView tv_null;
    @InjectView(id = R.id.rv)
    private RecyclerView rv;
    private BaseListAdapter<AnswerData> adapter;
    private QuestionInfo questionInfo;

    private List<TextView> textViewList = new ArrayList<>();
    private int postion = 0;
    private List<AnswerData> list = new ArrayList<>();

    public static StudentAnswerListDialog newInstance(int position,QuestionInfo questionInfo) {
        Bundle args = new Bundle();
        args.putInt("position", position);
        args.putSerializable("info",questionInfo);
        StudentAnswerListDialog fragment = new StudentAnswerListDialog();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected double getWidthHold() {
        return 0.19;
    }

    @Override
    protected int getContentView() {
        return R.layout.dialog_student_answer_list;
    }

    @Override
    protected void initView(View view) {
        postion = getArguments().getInt("position", 0);
        if (getArguments().getSerializable("info") != null) {
            questionInfo = (QuestionInfo) getArguments().getSerializable("info");
        }

        textViewList.add(tv_ok);
        textViewList.add(tv_err);
        textViewList.add(tv_null);

        adapter = new BaseListAdapter<AnswerData>(getActivity()) {
            @Override
            public int getLayoutId() {
                return R.layout.item_answer_respond_list;
            }

            @Override
            public void onBindItemHolder(BaseViewHolder holder, int position) {
                AnswerData item = mDataList.get(position);
                holder.getTextView(R.id.tv_name).setText(item.AnswerUserName);
                holder.getTextView(R.id.tv_answer).setText(item.AnswerText);

                View view_cut = holder.getView(R.id.cut_line);
                view_cut.setVisibility(position == mDataList.size() - 1 ? View.GONE : View.VISIBLE);
            }
        };
        rv.setAdapter(adapter);

    }

    @Override
    protected void initData() {
        setTab(postion);
    }

    public void setTab(int pos) {
        if (pos == 0){
            list = questionInfo.correct_set != null ? questionInfo.correct_set : new ArrayList<AnswerData>();
        }else if (pos == 1){
            list = questionInfo.error_set != null ? questionInfo.error_set : new ArrayList<AnswerData>();
        }else{
            list = questionInfo.unanswer_set != null ? questionInfo.unanswer_set : new ArrayList<AnswerData>();
        }
        adapter.setDataList(list);

        for (int i = 0; i < textViewList.size(); i++) {
            TextView tv = textViewList.get(i);
            if (i == pos) {
                tv.setSelected(true);
                tv.setBackgroundColor(getResources().getColor(R.color.count_class_title_color));
            } else {
                tv.setSelected(false);
                tv.setBackgroundColor(getResources().getColor(R.color.white));
            }
        }
        this.postion = pos;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_ok:
                setTab(0);
                break;
            case R.id.tv_err:
                setTab(1);
                break;
            case R.id.tv_null:
                setTab(2);
                break;
        }
    }
}
