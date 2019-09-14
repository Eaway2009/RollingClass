package com.tanhd.rollingclass.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.tanhd.rollingclass.R;
import com.tanhd.rollingclass.server.ScopeServer;
import com.tanhd.rollingclass.views.ZoomImageView;

public class ImageShowFragment extends Fragment {

    private String mUrl;
    private ZoomImageView mImageView;

    public static ImageShowFragment newInstance(String url) {
        Bundle args = new Bundle();
        args.putString("url", url);
        ImageShowFragment fragment = new ImageShowFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private static DisplayImageOptions mImageOptions;

    static {
        mImageOptions = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisk(true).build();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mUrl = getArguments().getString("url");
        View view = inflater.inflate(R.layout.fragment_show_image, container, false);
        mImageView = view.findViewById(R.id.image_resource_view);
        ImageLoader.getInstance().displayImage(ScopeServer.getInstance().getResourceUrl() + mUrl, mImageView, mImageOptions);
        return view;
    }

    public void resetData(String url){
        Bundle args = new Bundle();
        args.putString("url", url);
        setArguments(args);

        mUrl = getArguments().getString("url");
        ImageLoader.getInstance().displayImage(ScopeServer.getInstance().getResourceUrl() + mUrl, mImageView, mImageOptions);
    }
}
