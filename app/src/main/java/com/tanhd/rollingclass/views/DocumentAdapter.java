package com.tanhd.rollingclass.views;

import android.app.Activity;
import android.support.v7.widget.CardView;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tanhd.rollingclass.R;
import com.tanhd.rollingclass.server.data.KnowledgeDetailMessage;
import com.tanhd.rollingclass.activity.LearnCasesActivity;

import java.util.ArrayList;
import java.util.List;

public class DocumentAdapter  extends BaseAdapter implements View.OnClickListener {

    private Activity mContext;
    private List<KnowledgeDetailMessage> mDataList = new ArrayList<>();

    public DocumentAdapter(Activity context){
        mContext =context;
    }

    public void setData(List<KnowledgeDetailMessage> datas){
        mDataList = datas;
        notifyDataSetChanged();
    }

    public void clearData(){
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
        view.setOnClickListener(this);
        final KnowledgeDetailMessage data = mDataList.get(position);

        TextView statusView = view.findViewById(R.id.document_status_tv);
        TextView titleView = view.findViewById(R.id.document_title_tv);
        TextView editTimeView = view.findViewById(R.id.edit_time_tv);
        ImageView moreView = view.findViewById(R.id.document_more_ib);

        statusView.setText(data.section_name);
        statusView.setEnabled(data.status==1);
        titleView.setText(data.knowledge_point_name);
        editTimeView.setText(data.chapter_name);
        moreView.setOnClickListener(this);
        return view;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.layout_content) {
            LearnCasesActivity.startMe(mContext);
        }
    }
}
