package com.tanhd.rollingclass.views;

import android.annotation.SuppressLint;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.tanhd.rollingclass.R;
import com.tanhd.rollingclass.base.BaseDialogFragment;
import com.tanhd.rollingclass.base.BaseListAdapter;
import com.tanhd.rollingclass.base.BaseViewHolder;
import com.tanhd.rollingclass.server.data.AnswerData;
import com.tanhd.rollingclass.server.data.KnowledgeLessonSample;
import com.tanhd.rollingclass.utils.annotate.InjectView;

import java.util.ArrayList;
import java.util.List;

/**
 * 同步至弹窗
 * Created by YangShlai on 2019-09-24.
 */
@SuppressLint("ValidFragment")
public class SynPointDialog extends BaseDialogFragment implements View.OnClickListener {
    @InjectView(id = R.id.rv)
    private RecyclerView rv;
    @InjectView(id = R.id.tv_cancel, onClick = true)
    private TextView tv_cancel;
    @InjectView(id = R.id.tv_ok, onClick = true)
    private TextView tv_ok;

    private BaseListAdapter<KnowledgeLessonSample> adapter;
    private List<KnowledgeLessonSample> mDataList;
    private Boolean[] booleanList;
    private View.OnClickListener okListener;

    public SynPointDialog(List<KnowledgeLessonSample> mDataList) {
        this.mDataList = mDataList;
        booleanList = new Boolean[mDataList.size()];
        for (int i = 0;i<mDataList.size();i++){
            booleanList[i] = true;
        }
    }


    @Override
    protected int getContentView() {
        return R.layout.dialog_syn_point;
    }

    @Override
    protected void initView(View view) {
        adapter = new BaseListAdapter<KnowledgeLessonSample>(getActivity()) {
            @Override
            public int getLayoutId() {
                return R.layout.item_syn_point;
            }

            @Override
            public void onBindItemHolder(BaseViewHolder holder, final int position) {
                KnowledgeLessonSample kl = mDataList.get(position);
                CheckBox cb_select = holder.getView(R.id.cb_select);
                cb_select.setText(kl.lesson_sample_name);
                cb_select.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                        booleanList.get(position) = isChecked;
                        booleanList[position] = isChecked;
                    }
                });
            }
        };

        rv.setAdapter(adapter);
        adapter.setDataList(mDataList);
    }

    @Override
    protected void initData() {

    }

    /**
     * 获取选中的
     *
     * @return
     */
    public List<KnowledgeLessonSample> getSelectData() {
        List<KnowledgeLessonSample> list = new ArrayList<>();
        for (int i = 0;i<booleanList.length;i++) {
            if (booleanList[i]) {
                list.add(mDataList.get(i));
            }
        }
        return list;
    }

    /**
     * 设置确定按钮点击事件
     *
     * @param okListener
     */
    public void setOkListener(View.OnClickListener okListener) {
        this.okListener = okListener;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_cancel:
                dismissDialog();
                break;
            case R.id.tv_ok:
                if (okListener != null) okListener.onClick(v);
                dismissDialog();
                break;
        }
    }
}
