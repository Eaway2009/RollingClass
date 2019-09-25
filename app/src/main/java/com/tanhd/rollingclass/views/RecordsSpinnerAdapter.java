package com.tanhd.rollingclass.views;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.tanhd.rollingclass.R;
import com.tanhd.rollingclass.server.data.KnowledgeDetailMessage;
import com.tanhd.rollingclass.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class RecordsSpinnerAdapter extends BaseAdapter {
    private boolean isYear;
    private Context ctx;
    private List<KnowledgeDetailMessage.Record> mDataList = new ArrayList<>();

    public RecordsSpinnerAdapter(Context ctx, boolean isYear) {
        this.ctx = ctx;
        this.isYear = isYear;
    }

    public void setDataList(List<KnowledgeDetailMessage.Record> dataList) {
        mDataList.clear();
        for (KnowledgeDetailMessage.Record record : dataList) {
            if (!checkContent(record)) {
                mDataList.add(record);
            }
        }
    }

    private boolean checkContent(KnowledgeDetailMessage.Record checkRecord) {
        if (mDataList.size() > 0) {
            for (KnowledgeDetailMessage.Record record : mDataList) {
                if (isYear && StringUtils.getFormatYear(record.time_record).equals(StringUtils.getFormatYear(checkRecord.time_record))) {
                    return true;
                }
                if (!isYear && StringUtils.getFormatMonth(record.time_record).equals(StringUtils.getFormatMonth(checkRecord.time_record))) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public int getCount() {
        return mDataList.size();
    }

    @Override
    public KnowledgeDetailMessage.Record getItem(int position) {
        return mDataList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = View.inflate(ctx, R.layout.record_spinner, null);
        }
        KnowledgeDetailMessage.Record record = mDataList.get(position);
        TextView textView = convertView.findViewById(R.id.text);
        if (isYear) {
            textView.setText(StringUtils.getFormatYear(record.time_record));
        } else {
            textView.setText(StringUtils.getFormatMonth(record.time_record));
        }
        return convertView;
    }


}