package com.tanhd.rollingclass.views;

import android.content.Context;
import android.util.SparseArray;
import android.widget.ImageView;
import android.widget.TextView;

import com.tanhd.rollingclass.R;
import com.tanhd.rollingclass.server.data.ClassData;
import com.tanhd.rollingclass.server.data.StudentData;

public class ClassStudentsAdapter extends MultiLevelAdapter<ClassData, StudentData> {

    //                用于存放Indicator的集合
    private SparseArray<ImageView> mIndicators;
    public ClassStudentsAdapter(Context context) {
        super(context, R.layout.class_first_adapter, R.layout.student_second_adapter);
        mIndicators = new SparseArray<>();
    }

    @Override
    protected void convertGroup(ViewHolder viewHolder, ClassData item, int groupPosition, boolean isExpanded) {
        TextView classNameView = viewHolder.getView(R.id.class_name);
        ImageView iconView = viewHolder.getView(R.id.expand_imageview);
        //      把位置和图标添加到Map
        mIndicators.put(groupPosition, iconView);
        setIndicatorState(groupPosition, isExpanded);

        classNameView.setText(item.ClassName);
    }

    @Override
    protected void convertChild(ViewHolder viewHolder, StudentData item, int groupPosition, int childPosition, boolean isLastChild) {
        TextView studentName = viewHolder.getView(R.id.student_name);
        studentName.setText(item.Username);
    }

    //            根据分组的展开闭合状态设置指示器
    public void setIndicatorState(int groupPosition, boolean isExpanded) {
        if (!isExpanded) {
            mIndicators.get(groupPosition).setImageResource(R.drawable.chapter_icon_checked);
        } else {
            mIndicators.get(groupPosition).setImageResource(R.drawable.chapter_icon_uncheck);
        }
    }
}