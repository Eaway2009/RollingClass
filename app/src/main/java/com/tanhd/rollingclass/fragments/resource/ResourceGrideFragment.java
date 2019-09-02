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

/**
 * 资源下Grid适配的容器
 */
public class ResourceGrideFragment extends ResourceBaseFragment implements AdapterView.OnItemClickListener {

    private GridView mGridView;
    private ResourceAdapter mAdapter;
    private Callback mListener;

    private ResourceModel mCheckModel;

    public static ResourceGrideFragment newInstance() {
        ResourceGrideFragment page = new ResourceGrideFragment();
        return page;
    }
    
    public static ResourceGrideFragment newInstance(ResourceBaseFragment.Callback callback) {
        ResourceGrideFragment page = new ResourceGrideFragment();
        page.setListener(callback);
        return page;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.page_resourcepage_grid, container, false);
        iniViews(view);
        return view;
    }

    public void setListener(ResourceBaseFragment.Callback callback){
        mListener = callback;
    }

    private void iniViews(View view) {
        mGridView = view.findViewById(R.id.grid_view);
        mAdapter = new ResourceAdapter(getActivity());
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
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        mAdapter.checkItem(position);
        if(mListener!=null){
            mListener.itemChecked((ResourceModel) mAdapter.getItem(position), null);
        }
    }
}
