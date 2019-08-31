package com.tanhd.rollingclass.views;

import android.app.Activity;
import android.view.View;
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
            view = (LinearLayout) mContext.getLayoutInflater().inflate(R.layout.adapter_document, parent, false);
        }

        final ResourceModel data = mDataList.get(position);

        TextView statusView = view.findViewById(R.id.document_status_tv);
        TextView titleView = view.findViewById(R.id.document_title_tv);
        TextView editTimeView = view.findViewById(R.id.edit_time_tv);
        ImageView moreView = view.findViewById(R.id.document_more_ib);
        final LinearLayout moreBottomView = view.findViewById(R.id.more_bottom);
        ImageView moreCopyView = view.findViewById(R.id.more_copy_iv);
        ImageView moreShareView = view.findViewById(R.id.more_share_iv);
        ImageView moreEditView = view.findViewById(R.id.more_edit_iv);
        ImageView moreDeleteView = view.findViewById(R.id.more_delete_iv);

        return view;
    }
}