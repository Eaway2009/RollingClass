package com.tanhd.rollingclass.fragments.resource;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import com.tanhd.rollingclass.R;
import com.tanhd.rollingclass.server.data.ResourceModel;
import com.tanhd.rollingclass.views.ResourceAdapter;
import java.util.List;

import static com.tanhd.rollingclass.activity.DocumentEditActivity.NEED_CHECK_ITEM;

/**
 * 资源下Grid适配的容器
 */
public class ResourceGrideFragment extends ResourceBaseFragment implements AdapterView.OnItemClickListener {

    private GridView mGridView;
    private ResourceAdapter mAdapter;
    private Callback mListener;
    private boolean mNeedCheckItem;

    public static ResourceGrideFragment newInstance(boolean needCheckItem, ResourceBaseFragment.Callback callback) {
        Bundle args = new Bundle();
        args.putBoolean(NEED_CHECK_ITEM, needCheckItem);
        ResourceGrideFragment page = new ResourceGrideFragment();
        page.setListener(callback);
        page.setArguments(args);
        return page;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.page_resourcepage_grid, container, false);
        initParams();
        iniViews(view);
        return view;
    }

    private void initParams() {
        Bundle args = getArguments();
        mNeedCheckItem = args.getBoolean(NEED_CHECK_ITEM);
    }

    public void setListener(ResourceBaseFragment.Callback callback){
        mListener = callback;
    }

    private void iniViews(View view) {
        mGridView = view.findViewById(R.id.grid_view);
        mAdapter = new ResourceAdapter(getActivity(),mNeedCheckItem);
        mGridView.setAdapter(mAdapter);
        mGridView.setOnItemClickListener(this);
    }

    public void setListData(List resourceList) {
        if (resourceList != null && !resourceList.isEmpty() && mAdapter != null) {
            mAdapter.setData(resourceList);
        }
    }

    public void clearListData() {
        if (mAdapter != null) {
            mGridView.smoothScrollToPositionFromTop(0,0);
            mAdapter.clearData();
        }
    }

    @Override
    List<ResourceModel> getDataList() {
        return mAdapter.getDataList();
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        mAdapter.checkItem(position);
        if(mListener!=null){
            mListener.itemChecked((ResourceModel) mAdapter.getItem(position), null);
        }
    }
}
