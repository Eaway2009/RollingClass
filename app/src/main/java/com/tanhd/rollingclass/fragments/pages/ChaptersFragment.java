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
import com.tanhd.rollingclass.server.data.SubjectData;
import com.tanhd.rollingclass.server.data.TeacherData;
import com.tanhd.rollingclass.server.data.UserData;
import com.tanhd.rollingclass.views.ChaptersAdapter;

public class ChaptersFragment extends Fragment implements ExpandableListView.OnChildClickListener {
    private ExpandableListView mExpandableListView;
    private TextView mTeachingMaterialNameView;
    private ChaptersAdapter mAdapter;
    private ChapterListener mListener;
    private UserData userData;
    private TeacherData teacherData;
    private ChaptersResponse mChapterData;

    public static ChaptersFragment newInstance(ChaptersFragment.ChapterListener listener) {
        Bundle args = new Bundle();
        ChaptersFragment page = new ChaptersFragment();
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
        iniViews(view);
        refreshData();
        return view;
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
            if (userData.isTeacher()) {
                teacherData = (TeacherData) userData.getUserData();
                ChaptersResponse chaptersResponse =
                        ScopeServer.getInstance().QueryTeachingMaterial(teacherData.SchoolID, teacherData.StudysectionCode, 7, teacherData.SubjectCode, 1, 20);
                return chaptersResponse;
            } else {
//                SubjectData subjectData = ExternalParam.getInstance().getSubject();
//                List<TeachingMaterialData> materialDataList =
//                        ScopeServer.getInstance().QueryTeachingMaterial(teacherData.SchoolID,teacherData.StudysectionCode,12, teacherData.SubjectCode,1,20);
            }

            return null;
        }

        @Override
        protected void onPostExecute(ChaptersResponse response) {
            mChapterData = response;
            if (response != null) {
                mTeachingMaterialNameView.setText(response.TeachingMaterialName);
                mAdapter.setDataList(response.Chapters);
                mAdapter.notifyDataSetChanged();
            }
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
                    for (int i = 0; i < mAdapter.getGroupCount(); i++) {
                        mAdapter.resetCheckItem(i);
                    }
                    ChaptersResponse.Section section = chapter.getChildren().get(0);
                    section.isChecked = true;
                    if (mListener != null) {
                        mListener.onCheckChapter(teacherData.SchoolID, teacherData.TeacherID, chapter.ChapterID, chapter.ChapterName, section.SectionID, section.SectionName, mChapterData.SubjectCode, mChapterData.SubjectName, mChapterData.TeachingMaterialID);
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
                mAdapter.resetCheckItem(groupPosition);
                ChaptersResponse.Section section = mAdapter.getGroup(groupPosition).getChildren().get(childPosition);
                section.isChecked = true;
                if (mListener != null) {
                    mListener.onCheckChapter(teacherData.SchoolID, teacherData.TeacherID, chapter.ChapterID, chapter.ChapterName, section.SectionID, section.SectionName, mChapterData.SubjectCode, mChapterData.SubjectName, mChapterData.TeachingMaterialID);
                }
            }
        }
        return false;
    }

    public interface ChapterListener {
        void onCheckChapter(String school_id, String teacher_id, String chapter_id, String chapter_name, String section_id, String section_name, int subject_code, String subject_name, String teaching_material_id);
    }

}
