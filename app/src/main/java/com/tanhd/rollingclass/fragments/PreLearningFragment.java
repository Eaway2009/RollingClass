package com.tanhd.rollingclass.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tanhd.rollingclass.R;
import com.tanhd.rollingclass.server.data.ExternalParam;
import com.tanhd.rollingclass.server.data.KnowledgeData;
import com.tanhd.rollingclass.server.data.LessonSampleData;
import com.tanhd.rollingclass.server.data.SubjectData;

public class PreLearningFragment extends Fragment {
    public static interface PreLearningListener {
        void onCompleted();
    }

    private PreLearningListener mListener;

    public static PreLearningFragment newInstance(PreLearningListener listener) {
        PreLearningFragment fragment = new PreLearningFragment();
        fragment.setListener(listener);
        return fragment;
    }

    public void setListener(PreLearningListener listener) {
        mListener = listener;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pre_learning, container, false);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        showFragment(LessonSampleSelectorFragment.newInstance(new LessonSampleSelectorFragment.OnSelectorLessonSampleListener() {
            @Override
            public void onLessonSampleSelected(KnowledgeData knowledgeData, LessonSampleData lessonSampleData) {
                ExternalParam.getInstance().setLessonSample(lessonSampleData);
                ExternalParam.getInstance().setKnowledge(knowledgeData);
                if (mListener != null)
                    mListener.onCompleted();

                if (getParentFragment() instanceof FrameDialog) {
                    FrameDialog dialog = (FrameDialog) getParentFragment();
                    dialog.dismiss();
                }
            }
        }));
    }

    private void showFragment(Fragment fragment) {
        String fragmentTag = "PreviewTag";
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.replace(R.id.framelayout, fragment, fragmentTag);
        transaction.addToBackStack(fragmentTag);
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        transaction.commit();
    }
}
