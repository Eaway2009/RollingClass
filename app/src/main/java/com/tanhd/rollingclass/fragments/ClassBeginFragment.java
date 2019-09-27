package com.tanhd.rollingclass.fragments;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.tanhd.rollingclass.R;
import com.tanhd.rollingclass.server.ScopeServer;
import com.tanhd.rollingclass.server.data.ClassData;
import com.tanhd.rollingclass.server.data.ExternalParam;
import com.tanhd.rollingclass.server.data.KnowledgeData;
import com.tanhd.rollingclass.server.data.LessonSampleData;
import com.tanhd.rollingclass.utils.ToastUtil;

public class ClassBeginFragment extends Fragment {
    public static interface ClassBeginListener {
        void onCompleted();
    }

    private ClassBeginListener mListener;
    private boolean showStudentState = true;

    public static ClassBeginFragment newInstance(boolean show, ClassBeginListener listener) {
        Bundle args = new Bundle();
        args.putBoolean("show", show);
        ClassBeginFragment fragment = new ClassBeginFragment();
        fragment.setArguments(args);
        fragment.setListener(listener);
        return fragment;
    }
    public void setListener(ClassBeginListener listener) {
        mListener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        showStudentState = getArguments().getBoolean("show");
        View view = inflater.inflate(R.layout.fragment_class_begin, container, false);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        showFragment(ClassSelectorFragment.newInstance(new ClassSelectorFragment.OnClassListener() {
            @Override
            public void onClassSelected(ClassData classData) {
                ExternalParam.getInstance().setClassData(classData);
                showFragment(LessonSampleSelectorForTeacherFragment.newInstance(new LessonSampleSelectorForTeacherFragment.OnSelectorLessonSampleListener() {
                    @Override
                    public void onLessonSampleSelected(LessonSampleData lessonSampleData) {
                        ExternalParam.getInstance().setLessonSample(lessonSampleData);
                        new LoadDataTask().execute();
                    }
                }));
            }
        }));
    }

    private void showFragment(Fragment fragment) {
        String fragmentTag = "ClassBeginTag";
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.replace(R.id.framelayout, fragment, fragmentTag);
        transaction.addToBackStack(fragmentTag);
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        transaction.commit();
    }

    private class  LoadDataTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            LessonSampleData lessonSampleData = ExternalParam.getInstance().getLessonSample();
            KnowledgeData knowledgeData = ScopeServer.getInstance().QureyKnowledgeByID(lessonSampleData.KnowledgeID);
            ExternalParam.getInstance().setKnowledge(knowledgeData);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (ExternalParam.getInstance().getKnowledge() != null) {
                if (mListener != null)
                    mListener.onCompleted();
            } else {
                ToastUtil.show(R.string.toast_load_fail);
            }

            if (showStudentState) {
                showFragment(new ClassStateFragment());
            } else if (getParentFragment() instanceof FrameDialog) {
                FrameDialog dialog = (FrameDialog) getParentFragment();
                dialog.dismiss();
            }
        }
    }
}
