package com.tanhd.rollingclass.views;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;

import com.tanhd.rollingclass.db.MultiLevelModel;

import java.util.ArrayList;
import java.util.List;

/**
 * 安卓多级列表适配器
 * 1级列表、2级列表、3级列表、4级列表、5级列表、多级列表
 * 一级列表、二级列表、三级列表、四级列表、五级列表、多级列表
 * android multilevel adapter
 * 可伸缩可展开
 */

public abstract class MultiLevelAdapter<Group extends MultiLevelModel, Child> extends BaseExpandableListAdapter {

    protected String TAG = this.getClass().getSimpleName();
    private List<Group> mDataList;
    protected Context mContext;
    protected int mGroupLayoutId;
    protected int mChildLayoutId;

    public MultiLevelAdapter(Context context, @LayoutRes int groupLayoutId, @LayoutRes int childLayoutId) {
        this.mContext = context;
        this.mGroupLayoutId = groupLayoutId;
        this.mChildLayoutId = childLayoutId;
    }

    public List<Group> getDataList() {
        return mDataList;
    }

    /**
     * 添加数据
     */
    public void addMore(List<Group> list) {
        if (mDataList == null) {
            mDataList = new ArrayList<Group>();
        }
        mDataList.addAll(list);
        this.notifyDataSetInvalidated();
    }

    /**
     * 重置数据
     */
    public void setDataList(List<Group> list) {
        this.mDataList = list;
        this.notifyDataSetInvalidated();
    }

    //        获取分组的个数
    @Override
    public int getGroupCount() {
        return mDataList == null ? 0 : mDataList.size();
    }

    //        获取指定分组中的子选项的个数
    @Override
    public int getChildrenCount(int groupPosition) {
        if (mDataList != null && mDataList.size() > groupPosition && mDataList.get(groupPosition) != null && mDataList.get(groupPosition).getChildren() != null) {
            return mDataList.get(groupPosition).getChildren().size();
        } else {
            return 0;
        }
    }

    //        获取指定的分组数据
    @Override
    public Group getGroup(int groupPosition) {
        if (mDataList != null && mDataList.size() > groupPosition) {
            return mDataList.get(groupPosition);
        } else {
            return null;
        }
    }

    //        获取指定分组中的指定子选项数据
    @Override
    public Child getChild(int groupPosition, int childPosition) {
        if (mDataList != null && mDataList.size() > groupPosition && mDataList.get(groupPosition) != null && mDataList.get(groupPosition).getChildren() != null && mDataList.get(groupPosition).getChildren().size() > childPosition) {
            return (Child) mDataList.get(groupPosition).getChildren().get(childPosition);
        } else {
            return null;
        }
    }

    //        获取指定分组的ID, 这个ID必须是唯一的
    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    //        获取子选项的ID, 这个ID必须是唯一的
    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    //        分组和子选项是否持有稳定的ID, 就是说底层数据的改变会不会影响到它们。
    @Override
    public boolean hasStableIds() {
        return true;
    }

    //        获取显示指定分组的视图
    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = ViewHolder.getViewHolder(mContext, groupPosition, convertView, parent, mGroupLayoutId);
        Group item = null;
        try {
            item = getGroup(groupPosition);
        } catch (Exception e) {
        }
        convertGroup(viewHolder, item, groupPosition, isExpanded);
        return viewHolder.getConvertView();
    }

    //        获取显示指定分组中的指定子选项的视图
    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = ViewHolder.getViewHolder(mContext, groupPosition, convertView, parent, mChildLayoutId);
        Child item = null;
        try {
            item = getChild(groupPosition, childPosition);

        } catch (Exception e) {
        }
        convertChild(viewHolder, item, groupPosition, childPosition, isLastChild);
        return viewHolder.getConvertView();
    }

    protected abstract void convertGroup(ViewHolder viewHolder, Group item, int groupPosition, boolean isExpanded);

    protected abstract void convertChild(ViewHolder viewHolder, Child item, int groupPosition, int childPosition, boolean isLastChild);

    //        指定位置上的子元素是否可选中
    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}