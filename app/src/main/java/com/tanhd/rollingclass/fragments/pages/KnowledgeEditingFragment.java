package com.tanhd.rollingclass.fragments.pages;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tanhd.rollingclass.R;
import com.tanhd.rollingclass.activity.DatasActivity;

public class KnowledgeEditingFragment extends Fragment {

    private KnowledgeEditingFragment.Callback mListener;

    public static KnowledgeEditingFragment newInstance(int pageId, KnowledgeEditingFragment.Callback callback) {
        Bundle args = new Bundle();
        args.putInt(DatasActivity.PAGE_ID, pageId);
        KnowledgeEditingFragment page = new KnowledgeEditingFragment();
        page.setArguments(args);
        page.setListener(callback);
        return page;
    }

    private void setListener(KnowledgeEditingFragment.Callback listener) {
        mListener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.page_knowledge_controller, container, false);
        initParams();
        showModulePage(mPageId);
        initViews(view);
        return view;
    }

    private void initParams(){

    }

    private void initViews(View view){

    }

    public interface Callback{
        void onBack();
    }
}
