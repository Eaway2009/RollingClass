package com.tanhd.rollingclass.views;

import android.app.Activity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tanhd.rollingclass.R;
import com.tanhd.rollingclass.activity.DocumentEditActivity;
import com.tanhd.rollingclass.activity.LearnCasesActivity;
import com.tanhd.rollingclass.server.data.KnowledgeDetailMessage;
import com.tanhd.rollingclass.server.data.KnowledgeModel;
import com.tanhd.rollingclass.server.data.ResourceModel;

import java.util.ArrayList;
import java.util.List;

public class ResourceAdapter extends BaseAdapter {

    private Activity mContext;
    private List<ResourceModel> mDataList = new ArrayList<>();

    public ResourceAdapter(Activity context) {
        mContext = context;
    }

    public void setData(List<ResourceModel> datas) {
        mDataList = datas;
        notifyDataSetChanged();
    }

    public void clearData() {
        mDataList.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mDataList.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LinearLayout view = (LinearLayout) convertView;
        if (view == null) {
            view = (LinearLayout) mContext.getLayoutInflater().inflate(R.layout.adapter_resource, parent, false);
        }

        final ResourceModel data = mDataList.get(position);

        TextView nameView = view.findViewById(R.id.tv_name);
        ImageView thumbView = view.findViewById(R.id.iv_thumb);
        ImageView likeView = view.findViewById(R.id.iv_like);
        LinearLayout collect = view.findViewById(R.id.ll_collect);

        nameView.setText(data.name);

        collect.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: 2019/8/31 收藏请求
            }
        });
        return view;
    }
}