package com.tanhd.rollingclass.fragments.statistics;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.Toast;

import com.tanhd.rollingclass.R;
import com.tanhd.rollingclass.server.ScopeServer;
import com.tanhd.rollingclass.server.data.ClassData;
import com.tanhd.rollingclass.server.data.ExternalParam;
import com.tanhd.rollingclass.server.data.StudentData;
import com.tanhd.rollingclass.server.data.TeacherData;
import com.tanhd.rollingclass.server.data.UserData;
import com.tanhd.rollingclass.views.ClassStudentsAdapter;

import java.util.List;

public class ClassStudentsFragment extends Fragment implements ExpandableListView.OnChildClickListener, ExpandableListView.OnGroupClickListener {
    private ExpandableListView mExpandableListView;
    private ClassStudentsAdapter mAdapter;
    private Callback mListener;
    private UserData userData;
    private TeacherData teacherData;

    public static ClassStudentsFragment newInstance(Callback listener) {
        Bundle args = new Bundle();
        ClassStudentsFragment page = new ClassStudentsFragment();
        page.setArguments(args);
        page.setListener(listener);
        return page;
    }

    public void setListener(Callback listener) {
        mListener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.page_chapters, container, false);
        iniViews(view);
        refreshData();
        return view;
    }

    private void iniViews(View view) {
        mExpandableListView = view.findViewById(R.id.expandable_listview);
        mAdapter = new ClassStudentsAdapter(getActivity());
        mExpandableListView.setVerticalScrollBarEnabled(false);
        mExpandableListView.setGroupIndicator(null);
        mExpandableListView.setHeaderDividersEnabled(false);
        mExpandableListView.setAdapter(mAdapter);
        mExpandableListView.setOnChildClickListener(this);
    }


    public void refreshData() {
        UserData userData = ExternalParam.getInstance().getUserData();
        if (userData.isTeacher()) {
            TeacherData teacherData = (TeacherData) userData.getUserData();
            List<ClassData> classDataList = ExternalParam.getInstance().getTeachingClass();
            if (classDataList != null) {
                refreshTeachingClass(classDataList);
            } else {
                new InitDataTask(teacherData).execute();
            }
        }
    }

    private class InitDataTask extends AsyncTask<Void, Void, List<ClassData>> {

        private final TeacherData mTeacherData;

        public InitDataTask(TeacherData teacherData) {
            mTeacherData = teacherData;
        }

        @Override
        protected List<ClassData> doInBackground(Void... voids) {
            List<ClassData> teachingClass = ScopeServer.getInstance().getTeachingClass(mTeacherData.SchoolID, mTeacherData.TeacherID);
            ExternalParam.getInstance().setTeachingClass(teachingClass);
            return teachingClass;
        }

        @Override
        protected void onPostExecute(List<ClassData> classDataList) {
            if (ExternalParam.getInstance().getTeachingClass() != null) {
                refreshTeachingClass(classDataList);
            }
        }
    }

    private void refreshTeachingClass(List<ClassData> classDataList) {
        if (classDataList == null || classDataList.size() == 0) {
            try {
                Toast.makeText(getActivity().getApplicationContext(), "没有找到章节!", Toast.LENGTH_LONG).show();
            } catch (Exception e) {
            }
            return;
        }
        mAdapter.setDataList(classDataList);
        if (mAdapter.getGroupCount() > 0) {
            mExpandableListView.expandGroup(0);
            if(mAdapter.getChild(0, 0)!=null){
                if(mListener!=null){
                    mListener.onCheckStudent(mAdapter.getGroup(0), mAdapter.getChild(0,0));
                }
            }
            mAdapter.notifyDataSetChanged();
        }
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
        if (mListener != null) {
            mListener.onCheckClass(mAdapter.getGroup(groupPosition));
        }
        return false;
    }

    @Override
    public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
        if (mListener != null) {
            mListener.onCheckStudent(mAdapter.getGroup(groupPosition), mAdapter.getChild(groupPosition, childPosition));
        }
        return false;
    }

    public interface Callback {
        void onCheckClass(ClassData classData);

        void onCheckStudent(ClassData classData, StudentData studentData);
    }

}
