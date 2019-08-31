package com.tanhd.rollingclass.views;

import android.content.Context;
import android.widget.TextView;

import com.tanhd.rollingclass.R;
import com.tanhd.rollingclass.server.data.KnowledgeLessonSample;
import com.tanhd.rollingclass.server.data.ResourceModel;

public class LessonItemAdapter  extends MultiLevelAdapter<KnowledgeLessonSample, ResourceModel> {

    public LessonItemAdapter(Context context) {
        super(context, R.layout.layout_learning_item, R.layout.student_second_adapter);
    }

    @Override
    protected void convertGroup(ViewHolder viewHolder, KnowledgeLessonSample item, int groupPosition, boolean isExpanded) {
        TextView itemNameView = viewHolder.getView(R.id.item_name);
        itemNameView.setText(item.lesson_sample_name);
    }

    @Override
    protected void convertChild(ViewHolder viewHolder, ResourceModel item, int groupPosition, int childPosition, boolean isLastChild) {
        TextView studentName = viewHolder.getView(R.id.student_name);
        studentName.setText(item.name);
    }
}