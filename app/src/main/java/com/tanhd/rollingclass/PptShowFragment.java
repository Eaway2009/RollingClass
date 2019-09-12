package com.tanhd.rollingclass;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.itsrts.pptviewer.PPTViewer;

public class PptShowFragment extends Fragment {

    private String mUrl;
    private PPTViewer mPptViewer;

    public static PptShowFragment newInstance(String url) {
        Bundle args = new Bundle();
        args.putString("url", url);
        PptShowFragment fragment = new PptShowFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mUrl = getArguments().getString("url");
        View view = inflater.inflate(R.layout.fragment_show_ppt, container, false);
        mPptViewer = view.findViewById(R.id.pptviewer);
//        mPptViewer.setNext_img(R.drawable.next).setPrev_img(R.drawable.prev)
//                .setSettings_img(R.drawable.settings)
//                .setZoomin_img(R.drawable.zoomin)
//                .setZoomout_img(R.drawable.zoomout);
        mPptViewer.loadPPT(getActivity(), mUrl);
        return view;
    }
}
