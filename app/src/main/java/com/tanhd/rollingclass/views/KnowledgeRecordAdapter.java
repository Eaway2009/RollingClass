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

/**
 * 学习记录
 */
public class KnowledgeRecordAdapter extends BaseAdapter {

    private static final String TAG = "RecordAdapter";
    private Activity mContext;
    private List<KnowledgeDetailMessage.KnowledgeRecord> mDataList = new ArrayList<>();

    public KnowledgeRecordAdapter(Activity mContext) {
        this.mContext = mContext;
    }

    public void setData(List<KnowledgeDetailMessage.KnowledgeRecord> datas) {
        mDataList = datas;
        notifyDataSetChanged();
    }

    public List<KnowledgeDetailMessage.KnowledgeRecord> getDataList() {
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

        final KnowledgeDetailMessage.KnowledgeRecord data = mDataList.get(position);

        TextView tv_data = view.findViewById(R.id.learning_the_class);
        TextView tv_time = view.findViewById(R.id.learning_the_date);
        TextView tv_rate = view.findViewById(R.id.learning_the_time);
        View view_cut = view.findViewById(R.id.view_cut);

        tv_data.setText(data.getDate_time());
        tv_time.setText(data.getLearn_time());
        tv_rate.setText(data.getRate() + "%");
        view_cut.setVisibility(position == getCount() - 1 ? View.GONE : View.VISIBLE);
        return view;
    }
}