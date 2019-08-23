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
import com.tanhd.rollingclass.db.Document;

import java.util.ArrayList;
import java.util.List;

public class DocumentAdapter  extends BaseAdapter implements View.OnClickListener {

    private Activity mContext;
    private List<Document> mDataList = new ArrayList<>();

    public DocumentAdapter(Activity context){
        mContext =context;
    }

    public void setData(List<Document> datas){
        mDataList = datas;
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
        RelativeLayout view = (RelativeLayout) convertView;
        if (view == null) {
            view = (RelativeLayout) mContext.getLayoutInflater().inflate(R.layout.adapter_document, parent, false);
        }
        view.setOnClickListener(null);
        final Document data = mDataList.get(position);

        TextView statusView = view.findViewById(R.id.document_status_tv);
        TextView titleView = view.findViewById(R.id.document_title_tv);
        TextView editTimeView = view.findViewById(R.id.edit_time_tv);
        ImageView moreView = view.findViewById(R.id.document_more_ib);

        statusView.setText(data.statusText);
        statusView.setEnabled(data.status==1);
        titleView.setText(data.documentName);
        editTimeView.setText(data.editTime);
        moreView.setOnClickListener(this);
        return view;
    }

    @Override
    public void onClick(View v) {

    }
}
