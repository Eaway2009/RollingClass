package com.tanhd.rollingclass.views;

import android.content.Context;
import android.util.SparseArray;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;

import com.tanhd.rollingclass.R;
import com.tanhd.rollingclass.db.ChaptersResponse;

public class ChaptersAdapter extends MultiLevelAdapter<ChaptersResponse.Category, ChaptersResponse.Chapter> {

    //                用于存放Indicator的集合
    private SparseArray<ImageView> mIndicators;
    public ChaptersAdapter(Context context) {
        super(context, R.layout.chapter_first_adapter, R.layout.chapter_second_adapter);
        mIndicators = new SparseArray<>();
    }

    @Override
    protected void convertGroup(ViewHolder viewHolder, ChaptersResponse.Category item, int groupPosition, boolean isExpanded) {
        TextView chapterName = viewHolder.getView(R.id.chapter_name);
        chapterName.setEnabled(!isExpanded);
        chapterName.setText(item.categoryName);
    }

    @Override
    protected void convertChild(ViewHolder viewHolder, ChaptersResponse.Chapter item, int groupPosition, int childPosition, boolean isLastChild) {
        TextView chapterName = viewHolder.getView(R.id.chapter_name);
        chapterName.setEnabled(!item.isChecked);
        chapterName.setText(item.chapterName);
    }
}
