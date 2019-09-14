package com.tanhd.rollingclass.fragments.pages;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;

import com.tanhd.rollingclass.R;
import com.tanhd.rollingclass.server.data.ChaptersResponse;
import com.tanhd.rollingclass.server.ScopeServer;
import com.tanhd.rollingclass.server.data.ExternalParam;
import com.tanhd.rollingclass.server.data.StudentData;
import com.tanhd.rollingclass.server.data.TeacherData;
import com.tanhd.rollingclass.server.data.UserData;
import com.tanhd.rollingclass.views.ChaptersAdapter;

import java.util.ArrayList;
import java.util.List;

public class ChaptersFragment extends Fragment implements ExpandableListView.OnChildClickListener {
    private ExpandableListView mExpandableListView;
    private ChaptersAdapter mAdapter;
    private ChapterListener mListener;
    private UserData userData;
    private TeacherData teacherData;
    private StudentData studentData;
    private ChaptersResponse mChaptersResponse;
    private TextView mTeachingMaterialNameView;

    public static ChaptersFragment newInstance(ChaptersFragment.ChapterListener listener) {
        ChaptersFragment page = new ChaptersFragment();
        Bundle args = new Bundle();
        page.setArguments(args);
        page.setListener(listener);
        return page;
    }

    public void setListener(ChapterListener listener) {
        mListener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.page_chapters, container, false);
        initParams();
        iniViews(view);
        refreshData();
        return view;
    }

    private void initParams() {
        Bundle args = getArguments();
    }

    private void iniViews(View view) {
        mExpandableListView = view.findViewById(R.id.expandable_listview);
        mTeachingMaterialNameView = view.findViewById(R.id.teaching_material_name);
        mAdapter = new ChaptersAdapter(getActivity());
        mExpandableListView.setVerticalScrollBarEnabled(false);
        mExpandableListView.setGroupIndicator(null);
        mExpandableListView.setHeaderDividersEnabled(false);
        mExpandableListView.setAdapter(mAdapter);
        mExpandableListView.setOnChildClickListener(this);
    }

    public void refreshData() {
        new InitDataTask().execute();
    }

    private class InitDataTask extends AsyncTask<Void, Void, ChaptersResponse> {

        @Override
        protected ChaptersResponse doInBackground(Void... voids) {
            userData = ExternalParam.getInstance().getUserData();
            List<ChaptersResponse> responseList;
            if (userData.isTeacher()) {
                teacherData = (TeacherData) userData.getUserData();
                responseList = ScopeServer.getInstance().QueryTeachingMaterial(teacherData.SchoolID, teacherData.StudysectionCode, teacherData.SubjectCode);
            } else {
                studentData = (StudentData) userData.getUserData();
                responseList = ScopeServer.getInstance().QueryTeachingMaterialByGradeID(studentData.SchoolID, studentData.GradeID);
            }
            if (responseList != null && responseList.size() > 0) {
                return responseList.get(0);
            } else {
                return null;
            }
        }

        @Override
        protected void onPostExecute(ChaptersResponse chapterList) {
            if(chapterList==null){
                Toast.makeText(getActivity().getApplicationContext(), "没有找到对应教材!", Toast.LENGTH_LONG).show();
                return;
            }
            mAdapter.setDataList(chapterList.Chapters);
            mAdapter.notifyDataSetChanged();
            mChaptersResponse = chapterList;
            mTeachingMaterialNameView.setText(mChaptersResponse.TeachingMaterialName + mChaptersResponse.GradeName + mChaptersResponse.SubjectName);
            if (mAdapter.getGroupCount() == 0) {
                Toast.makeText(getActivity().getApplicationContext(), "没有找到章节!", Toast.LENGTH_LONG).show();
                return;
            }

            // 将第一章设置成默认展开,设置默认选中第一节
            ChaptersResponse.Chapter chapter = mAdapter.getGroup(0);
            if (mAdapter.getGroupCount() > 0 && chapter != null) {
                mExpandableListView.expandGroup(0);
                if (chapter.getChildren().size() > 0) {
                    mAdapter.resetCheckItem(0, 0);
                    ChaptersResponse.Section section = chapter.getChildren().get(0);
                    if (mListener != null) {
                        if (teacherData != null) {
                            mListener.onTeacherCheckChapter(teacherData.SchoolID, teacherData.TeacherID, chapter.ChapterID, chapter.ChapterName, section.SectionID, section.SectionName, chapterList.SubjectCode, chapterList.SubjectName, section.TeachingMaterialID);
                        } else if (studentData != null) {
                            mListener.onTeacherCheckChapter(studentData.SchoolID, studentData.ClassID, chapter.ChapterID, chapter.ChapterName, section.SectionID, section.SectionName, chapterList.SubjectCode, chapterList.SubjectName, section.TeachingMaterialID);
                        }
                    }
                }
                mAdapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
        ChaptersResponse.Chapter chapter = mAdapter.getGroup(groupPosition);
        if (chapter != null && chapter.getChildren() != null) {
            if (childPosition < mAdapter.getGroup(groupPosition).getChildren().size()) {
                for (int i = 0; i < mAdapter.getGroupCount(); i++) {
                    mExpandableListView.collapseGroup(i);
                }
                mExpandableListView.expandGroup(groupPosition);
                mAdapter.resetCheckItem(groupPosition, childPosition);
                ChaptersResponse.Section section = mAdapter.getGroup(groupPosition).getChildren().get(childPosition);
                if (mListener != null) {
                    if (teacherData != null) {
                        mListener.onTeacherCheckChapter(teacherData.SchoolID, teacherData.TeacherID, chapter.ChapterID, chapter.ChapterName, section.SectionID, section.SectionName, mChaptersResponse.SubjectCode, mChaptersResponse.SubjectName, section.TeachingMaterialID);
                    } else if (studentData != null) {
                        mListener.onTeacherCheckChapter(studentData.SchoolID, studentData.ClassID, chapter.ChapterID, chapter.ChapterName, section.SectionID, section.SectionName, mChaptersResponse.SubjectCode, mChaptersResponse.SubjectName, section.TeachingMaterialID);
                    }
                }
            }
        }
        return false;
    }

    public interface ChapterListener {
        void onTeacherCheckChapter(String school_id, String teacher_id, String chapter_id, String chapter_name, String section_id, String section_name, int subject_code, String subject_name, String teaching_material_id);

        void onStudentCheckChapter(String school_id, String class_id, String chapter_id, String chapter_name, String section_id, String section_name, int subject_code, String subject_name, String teaching_material_id);
    }

}
