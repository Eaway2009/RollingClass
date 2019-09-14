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

    private class InitDataTask extends AsyncTask<Void, Void, List<ChaptersResponse.Chapter>> {

        @Override
        protected List<ChaptersResponse.Chapter> doInBackground(Void... voids) {
            userData = ExternalParam.getInstance().getUserData();
            List<ChaptersResponse> responseList;
            if (userData.isTeacher()) {
                teacherData = (TeacherData) userData.getUserData();
                responseList = ScopeServer.getInstance().QueryTeachingMaterial(teacherData.SchoolID, teacherData.StudysectionCode, teacherData.SubjectCode);
            } else {
                studentData = (StudentData) userData.getUserData();
                responseList = ScopeServer.getInstance().QueryTeachingMaterialByGradeID(studentData.SchoolID, studentData.GradeID);
            }
            return getChapterDatas(responseList);
        }

        @Override
        protected void onPostExecute(List<ChaptersResponse.Chapter> chapterList) {
            mAdapter.setDataList(chapterList);
            mAdapter.notifyDataSetChanged();
            if (mAdapter.getGroupCount() == 0) {
                try {
                    Toast.makeText(getActivity().getApplicationContext(), "没有找到章节!", Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                }
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
                            mListener.onTeacherCheckChapter(teacherData.SchoolID, teacherData.TeacherID, chapter.ChapterID, chapter.ChapterName, section.SectionID, section.SectionName, chapter.teachingMaterial.SubjectCode, chapter.teachingMaterial.SubjectName, section.TeachingMaterialID);
                        } else if (studentData != null) {
                            mListener.onTeacherCheckChapter(studentData.SchoolID, studentData.ClassID, chapter.ChapterID, chapter.ChapterName, section.SectionID, section.SectionName, chapter.teachingMaterial.SubjectCode, chapter.teachingMaterial.SubjectName, section.TeachingMaterialID);
                        }
                    }
                }
                mAdapter.notifyDataSetChanged();
            }
        }
    }

    private List<ChaptersResponse.Chapter> getChapterDatas(List<ChaptersResponse> responseList) {
        List<ChaptersResponse.Chapter> lastChapters = new ArrayList<>();
        if (responseList != null) {
            for (int i = 0; i < responseList.size(); i++) {
                ChaptersResponse chaptersResponse = responseList.get(i);
                ChaptersResponse.TeachingMaterial teachingMaterial = new ChaptersResponse.TeachingMaterial(chaptersResponse.GradeCode,
                        chaptersResponse.GradeName, chaptersResponse.StudySectionCode, chaptersResponse.StudySectionName, chaptersResponse.SubjectCode,
                        chaptersResponse.SubjectName, chaptersResponse.TeachingMaterialCode, chaptersResponse.TeachingMaterialID, chaptersResponse.TeachingMaterialName);
                if (chaptersResponse.Chapters != null && chaptersResponse.Chapters.size() > 0) {
                    for (int j = 0; j < chaptersResponse.Chapters.size(); j++) {
                        ChaptersResponse.Chapter chapter = chaptersResponse.Chapters.get(j);
                        if(j==0) {
                            chapter.teachingMaterial = teachingMaterial;
                        }
                        lastChapters.add(chapter);
                    }
                }
            }
        }
        return lastChapters;
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
                        mListener.onTeacherCheckChapter(teacherData.SchoolID, teacherData.TeacherID, chapter.ChapterID, chapter.ChapterName, section.SectionID, section.SectionName, chapter.teachingMaterial.SubjectCode, chapter.teachingMaterial.SubjectName, section.TeachingMaterialID);
                    } else if (studentData != null) {
                        mListener.onTeacherCheckChapter(studentData.SchoolID, studentData.ClassID, chapter.ChapterID, chapter.ChapterName, section.SectionID, section.SectionName, chapter.teachingMaterial.SubjectCode, chapter.teachingMaterial.SubjectName, section.TeachingMaterialID);
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
