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

import java.util.ArrayList;
import java.util.List;

public class ResourceAdapter extends BaseAdapter {

    private Activity mContext;
    private List<KnowledgeDetailMessage> mDataList = new ArrayList<>();

    public ResourceAdapter(Activity context) {
        mContext = context;
    }

    public void setData(List<KnowledgeDetailMessage> datas) {
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

        final KnowledgeDetailMessage data = mDataList.get(position);

        TextView statusView = view.findViewById(R.id.document_status_tv);
        TextView titleView = view.findViewById(R.id.document_title_tv);
        TextView editTimeView = view.findViewById(R.id.edit_time_tv);
        ImageView moreView = view.findViewById(R.id.document_more_ib);
        final LinearLayout moreBottomView = view.findViewById(R.id.more_bottom);
        ImageView moreCopyView = view.findViewById(R.id.more_copy_iv);
        ImageView moreShareView = view.findViewById(R.id.more_share_iv);
        ImageView moreEditView = view.findViewById(R.id.more_edit_iv);
        ImageView moreDeleteView = view.findViewById(R.id.more_delete_iv);

        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.layout_content:
                        LearnCasesActivity.startMe(mContext);
                        break;
                    case R.id.more_bottom:
                        moreBottomView.setVisibility(View.GONE);
                        break;
                    case R.id.document_more_ib:
                        moreBottomView.setVisibility(View.VISIBLE);
                        break;
                    case R.id.more_copy_iv:

                        break;
                    case R.id.more_share_iv:

                        break;
                    case R.id.more_edit_iv:
                        KnowledgeModel knowledgeModel = new KnowledgeModel(data.school_id, data.teacher_id, data.chapter_id, data.chapter_name, data.section_id, data.section_name, data.subject_code, data.subject_name, data.teaching_material_id);
                        DocumentEditActivity.startMe(mContext, DocumentEditActivity.PAGE_ID_EDIT_DOCUMENTS, knowledgeModel, data);
                        break;
                    case R.id.more_delete_iv:

                        break;
                }
            }
        };
        view.setOnClickListener(onClickListener);
        moreView.setOnClickListener(onClickListener);
        moreBottomView.setOnClickListener(onClickListener);
        moreCopyView.setOnClickListener(onClickListener);
        moreShareView.setOnClickListener(onClickListener);
        moreEditView.setOnClickListener(onClickListener);
        moreDeleteView.setOnClickListener(onClickListener);

        StringBuffer publishStatus = new StringBuffer();
        if (data.class_before == 0 && data.class_process == 0 && data.class_after == 0) {
            publishStatus.append(mContext.getResources().getString(R.string.not_publish));
        } else if (data.class_before == 1 && data.class_process == 1 && data.class_after == 1) {
            publishStatus.append(mContext.getResources().getString(R.string.class_record));

        } else {
            if (data.class_before != 0) {
                publishStatus.append(mContext.getResources().getString(R.string.fre_class));
            }
            if (publishStatus.length() != 0) {
                publishStatus.append(mContext.getResources().getString(R.string.comma));
            }
            if (data.class_process != 0) {
                publishStatus.append(mContext.getResources().getString(R.string.at_class));
            }
            if (publishStatus.length() != 0) {
                publishStatus.append(mContext.getResources().getString(R.string.comma));
            }
            if (data.class_after != 0) {
                publishStatus.append(mContext.getResources().getString(R.string.after_class));
            }
            if (publishStatus.toString().endsWith(mContext.getResources().getString(R.string.comma))) {
                publishStatus.deleteCharAt(publishStatus.length() - 1);
            }
            publishStatus.append(mContext.getResources().getString(R.string.to_publish));
        }
        statusView.setText(publishStatus);
        statusView.setEnabled(data.class_before == 1 && data.class_process == 1 && data.class_after == 1);
        moreCopyView.setVisibility(data.class_before == 1 && data.class_process == 1 && data.class_after == 1 ? View.VISIBLE : View.GONE);
        moreEditView.setVisibility(data.class_before == 0 || data.class_process == 0 || data.class_after == 0 ? View.VISIBLE : View.GONE);
        titleView.setText(data.knowledge_point_name);
        editTimeView.setText(data.create_time + "");
        return view;
    }
}