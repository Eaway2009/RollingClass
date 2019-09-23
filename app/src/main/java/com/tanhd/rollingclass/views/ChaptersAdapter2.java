package com.tanhd.rollingclass.views;

import android.content.Context;
import android.util.SparseArray;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.tanhd.rollingclass.R;
import com.tanhd.rollingclass.base.BaseViewHolder;
import com.tanhd.rollingclass.db.model.Course;
import com.tanhd.rollingclass.server.data.ChaptersResponse;
import com.tanhd.rollingclass.utils.Logger;

/**
 * 左侧三级目录
 */
public class ChaptersAdapter2 extends BaseMultiAdapter<MultiItemEntity> {
    public static final int TYPE_LEVEL_CLASS = 0; //年级
    public static final int TYPE_LEVEL_CHAPTER = 1; //章
    public static final int TYPE_LEVEL_SECTION = 2; //节
    private SelectListener selectListener;

    public ChaptersAdapter2(Context context) {
        super(context);
        addItemType(TYPE_LEVEL_CLASS, R.layout.chapter_first_adapter);
        addItemType(TYPE_LEVEL_CHAPTER, R.layout.chapter_second_adapter);
        addItemType(TYPE_LEVEL_SECTION, R.layout.chapter_third_adapter);

    }

    @Override
    public void onBindItemHolder(BaseViewHolder holder, int position) {
        MultiItemEntity item = getDataList().get(position);
        switch (item.getItemType()) {
            case TYPE_LEVEL_CLASS:
                bindClass(holder, position, (Course) item);
                break;
            case TYPE_LEVEL_CHAPTER:
                bindChapter(holder, position, (Course.ChaptersBean) item);
                break;
            case TYPE_LEVEL_SECTION:
                bindSection(holder, position, (Course.ChaptersBean.SectionsBean) item);
                break;
            default:
                break;
        }
    }

    private void bindClass(BaseViewHolder holder, final int position, final Course item) {
        TextView tv_name = holder.getTextView(R.id.tv_material_name);
        tv_name.setText(item.getGradeName());
        tv_name.setSelected(item.isExpanded());

        tv_name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (item.isExpanded()) {
                    collapse(position, false);
                } else {
                    //展开前先关闭其他所有
                    expand(position, false);
                }
            }
        });
    }

    private void bindChapter(BaseViewHolder holder, final int position, final Course.ChaptersBean item) {
        TextView tv_chapter_name = holder.getTextView(R.id.tv_chapter_name);
        tv_chapter_name.setText(item.getChapterName());
        tv_chapter_name.setSelected(item.isExpanded());

        tv_chapter_name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (item.isExpanded()) {
                    collapse(position, false);
                } else {
                    expand(position, false);
                }
            }
        });
    }

    private void bindSection(BaseViewHolder holder, final int position, final Course.ChaptersBean.SectionsBean item) {
        TextView tv_section_name = holder.getTextView(R.id.tv_section_name);
        tv_section_name.setText(item.getSectionName());
        tv_section_name.setSelected(item.isSelect());

        tv_section_name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectListener != null) selectListener.select(v,position,item);
            }
        });
    }

    public void setSelectListener(SelectListener selectListener) {
        this.selectListener = selectListener;
    }

    public interface SelectListener{
        void select(View view,int pos,Course.ChaptersBean.SectionsBean item);
    }

}
