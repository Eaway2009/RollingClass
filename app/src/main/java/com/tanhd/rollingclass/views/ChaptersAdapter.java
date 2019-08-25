package com.tanhd.rollingclass.views;

import android.content.Context;
import android.util.SparseArray;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.tanhd.rollingclass.R;
import com.tanhd.rollingclass.server.data.ChaptersResponse;

public class ChaptersAdapter extends MultiLevelAdapter<ChaptersResponse.Chapter, ChaptersResponse.Section> {

    //                用于存放Indicator的集合
    private SparseArray<ImageView> mIndicators;
    public ChaptersAdapter(Context context) {
        super(context, R.layout.chapter_first_adapter, R.layout.chapter_second_adapter);
        mIndicators = new SparseArray<>();
    }

    @Override
    protected void convertGroup(ViewHolder viewHolder, ChaptersResponse.Chapter item, int groupPosition, boolean isExpanded) {
        TextView teachingMaterialNameView = viewHolder.getView(R.id.teaching_material_name);
        ImageView iconView = viewHolder.getView(R.id.expand_imageview);
        TextView chapterName = viewHolder.getView(R.id.chapter_name);
        chapterName.setText(item.ChapterName);
        //      把位置和图标添加到Map
        mIndicators.put(groupPosition, iconView);
        setIndicatorState(groupPosition, isExpanded);

        if(item.teachingMaterial!=null&&item.teachingMaterial.isFirstItem){
            teachingMaterialNameView.setText(item.teachingMaterial.TeachingMaterialName);
            teachingMaterialNameView.setVisibility(View.VISIBLE);
        }else {
            teachingMaterialNameView.setVisibility(View.GONE);
        }
    }

    @Override
    protected void convertChild(ViewHolder viewHolder, ChaptersResponse.Section item, int groupPosition, int childPosition, boolean isLastChild) {
        TextView chapterName = viewHolder.getView(R.id.chapter_name);
        chapterName.setEnabled(!item.isChecked);
        chapterName.setText(item.SectionName);
    }

    //            根据分组的展开闭合状态设置指示器
    public void setIndicatorState(int groupPosition, boolean isExpanded) {
        if (!isExpanded) {
            mIndicators.get(groupPosition).setImageResource(R.drawable.chapter_icon_checked);
        } else {
            mIndicators.get(groupPosition).setImageResource(R.drawable.chapter_icon_uncheck);
        }
    }

    public void resetCheckItem(int group){
        ChaptersResponse.Chapter groupItem = getGroup(group);
        if(groupItem!=null&&groupItem.Sections!=null){
            for (ChaptersResponse.Section section:groupItem.Sections) {
                section.isChecked = false;
            }
        }
    }
}
