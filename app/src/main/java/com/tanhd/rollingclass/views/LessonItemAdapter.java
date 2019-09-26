package com.tanhd.rollingclass.views;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.tanhd.rollingclass.R;
import com.tanhd.rollingclass.server.data.KnowledgeLessonSample;
import com.tanhd.rollingclass.server.data.ResourceModel;

public class LessonItemAdapter  extends MultiLevelAdapter<KnowledgeLessonSample, ResourceModel> {

    private int selectGroupPosition = 0;


    public LessonItemAdapter(Context context) {
        super(context, R.layout.layout_learning_item, R.layout.student_second_adapter);
    }

    @Override
    protected void convertGroup(ViewHolder viewHolder, KnowledgeLessonSample item, final int groupPosition, boolean isExpanded) {
        TextView itemNameView = viewHolder.getView(R.id.item_name);
        itemNameView.setText(item.lesson_sample_name);
        itemNameView.setSelected(selectGroupPosition == groupPosition);
    }

    @Override
    protected void convertChild(ViewHolder viewHolder, ResourceModel item, int groupPosition, int childPosition, boolean isLastChild) {
        TextView studentName = viewHolder.getView(R.id.student_name);
        studentName.setText(item.name);
    }

    /**
     * 选中POS
     * @param selectPos
     */
    public void setSelectPos(int selectPos) {
        this.selectGroupPosition = selectPos;
        notifyDataSetChanged();
    }
}