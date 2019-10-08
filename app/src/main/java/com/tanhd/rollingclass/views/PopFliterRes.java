package com.tanhd.rollingclass.views;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tanhd.rollingclass.R;
import com.tanhd.rollingclass.base.BaseListAdapter;
import com.tanhd.rollingclass.base.BaseViewHolder;

import java.util.ArrayList;
import java.util.List;

/**
 * 资源选择筛选
 * Created by YangShlai on 2019-09-23.
 */
public class PopFliterRes extends TopPushPopupWindow<Void> {
    private RecyclerView rv;
    private BaseListAdapter<String> adapter;
    private OnItemClickListener onItemClickListener;
    private LinearLayout root;
    private int nowPos = 0;

    public PopFliterRes(Activity activity) {
        super(activity, activity, null);
    }

    @Override
    protected View generateCustomView(Void aVoid) {
        View view = activity.getLayoutInflater().inflate(R.layout.pop_filter_res,null);
        init(view);
        return view;
    }

    private void init(View view) {
        root = view.findViewById(R.id.root);
        rv = view.findViewById(R.id.rv);
        setWidth((int) activity.getResources().getDimension(R.dimen.dp_90));

        adapter = new BaseListAdapter<String>(activity) {
            @Override
            public int getLayoutId() {
                return R.layout.item_pop_filter;
            }

            @Override
            public void onBindItemHolder(BaseViewHolder holder, int position) {
                TextView tv_name = holder.getTextView(R.id.tv_name);
                tv_name.setText(mDataList.get(position));
                tv_name.setSelected(position == nowPos);
            }
        };
        rv.setAdapter(adapter);

        adapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                nowPos = position;
                if (onItemClickListener != null) onItemClickListener.onItemClick(view,position);
                adapter.notifyDataSetChanged();
                dismiss();
            }
        });
    }

    /**
     * 设置宽度
     * @param px
     */
    public void setRootWidth(int px){
        setWidth(px);
    }

    public void setDatas(List<String> datas) {
        adapter.setDataList(datas);
    }

    public List<String> getDatas() {
        return adapter.getDataList();
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

}
