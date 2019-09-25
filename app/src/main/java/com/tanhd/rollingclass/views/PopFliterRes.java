package com.tanhd.rollingclass.views;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.View;

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
        rv = view.findViewById(R.id.rv);

        adapter = new BaseListAdapter<String>(activity) {
            @Override
            public int getLayoutId() {
                return R.layout.item_pop_filter;
            }

            @Override
            public void onBindItemHolder(BaseViewHolder holder, int position) {
                holder.getTextView(R.id.tv_name).setText(mDataList.get(position));
            }
        };
        rv.setAdapter(adapter);

        adapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                if (onItemClickListener != null) onItemClickListener.onItemClick(view,position);
                dismiss();
            }
        });
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
