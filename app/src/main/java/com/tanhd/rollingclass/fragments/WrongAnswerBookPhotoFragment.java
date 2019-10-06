package com.tanhd.rollingclass.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tanhd.rollingclass.R;
import com.tanhd.rollingclass.activity.DocumentEditActivity;
import com.tanhd.rollingclass.server.data.KnowledgeModel;

public class WrongAnswerBookPhotoFragment extends Fragment {

    private Callback mCallback;

    public static WrongAnswerBookPhotoFragment newInstance(KnowledgeModel knowledgeModel, Callback callback) {
        Bundle args = new Bundle();
        args.putSerializable(DocumentEditActivity.PARAM_TEACHING_MATERIAL_DATA, knowledgeModel);
        WrongAnswerBookPhotoFragment page = new WrongAnswerBookPhotoFragment();
        page.setArguments(args);
        page.setCallback(callback);
        return page;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.page_wrong_answer_book_photo, container, false);
//        initParams();
//        initViews(view);
//        showFragment();
        return view;
    }


    public void setCallback(Callback callback) {
        this.mCallback = callback;
    }

    public void resetData(KnowledgeModel mKnowledgeModel) {

    }

    public void clearListData() {

    }

    public interface Callback {
        void onBack();
    }
}
