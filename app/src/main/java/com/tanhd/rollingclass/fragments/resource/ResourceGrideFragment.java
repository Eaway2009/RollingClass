package com.tanhd.rollingclass.fragments.resource;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import com.tanhd.rollingclass.R;
import com.tanhd.rollingclass.server.data.ResourceModel;
import com.tanhd.rollingclass.views.ResourceAdapter;
import java.util.List;

/**
 * 资源下Grid适配的容器
 */
public class ResourceGrideFragment extends ResourceBaseFragment {

    private GridView mGridView;
    private ResourceAdapter mAdapter;
    public static ResourceGrideFragment newInstance() {
        ResourceGrideFragment page = new ResourceGrideFragment();
        return page;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.page_resourcepage_grid, container, false);
        iniViews(view);
        return view;
    }

    private void iniViews(View view) {
        mGridView = view.findViewById(R.id.grid_view);
        mAdapter = new ResourceAdapter(getActivity());
        mGridView.setAdapter(mAdapter);
    }

    public void setListData(List<ResourceModel> resourceList) {
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


}
