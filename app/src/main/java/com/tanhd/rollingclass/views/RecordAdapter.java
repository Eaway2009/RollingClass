package com.tanhd.rollingclass.views;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tanhd.rollingclass.R;
import com.tanhd.rollingclass.server.data.KnowledgeDetailMessage;
import com.tanhd.rollingclass.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class RecordAdapter extends BaseAdapter {

    private static final String TAG = "RecordAdapter";
    private Activity mContext;
    private List<KnowledgeDetailMessage.Record> mDataList = new ArrayList<>();

    public void setData(List<KnowledgeDetailMessage.Record> datas) {
        mDataList = datas;
        notifyDataSetChanged();
    }

    public List<KnowledgeDetailMessage.Record> getDataList() {
        return mDataList;
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
        return mDataList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LinearLayout view = (LinearLayout) convertView;
        if (view == null) {
            view = (LinearLayout) mContext.getLayoutInflater().inflate(R.layout.record_adapter, parent, false);
        }

        final KnowledgeDetailMessage.Record data = mDataList.get(position);

        TextView nameView = view.findViewById(R.id.learning_the_class);
        TextView dateView = view.findViewById(R.id.learning_the_date);
        TextView timeView = view.findViewById(R.id.learning_the_time);
        nameView.setText(data.class_name);
        dateView.setText(StringUtils.getFormatDate2(data.time_record));
        timeView.setText(StringUtils.getFormatTime(data.time_record));

        return view;
    }
}